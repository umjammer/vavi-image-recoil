/*
 * thumbfail.cpp - Windows thumbnail provider for FAIL
 *
 * Copyright (C) 2011-2013  Piotr Fusik and Adrian Matoga
 *
 * This file is part of FAIL (First Atari Image Library),
 * see http://fail.sourceforge.net
 *
 * FAIL is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * FAIL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FAIL; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/* There are two separate implementations for different Windows versions:
   IPersistFile+IExtractImage for Windows 2000 and XP.
   IInitializeWithStream+IThumbnailProvider for Windows Vista+
   (even though the Windows 2000/XP interfaces could work). */

#include <windows.h>
#include <objidl.h>

#undef INTERFACE

#ifdef __MINGW64__

#include <shobjidl.h>

#else
// missing just in 32-bit MinGW

#define IEIFLAG_CACHE 2

#define INTERFACE IExtractImage
DECLARE_INTERFACE_(IExtractImage, IUnknown)
{
	STDMETHOD(QueryInterface)(THIS_ REFIID, PVOID *) PURE;
	STDMETHOD_(ULONG, AddRef)(THIS) PURE;
	STDMETHOD_(ULONG, Release)(THIS) PURE;
	STDMETHOD(GetLocation)(THIS_ LPWSTR, DWORD, DWORD *, const SIZE *, DWORD, DWORD *) PURE;
	STDMETHOD(Extract)(THIS_ HBITMAP *) PURE;
};
#undef INTERFACE

static const IID IID_IInitializeWithStream =
	{ 0xb824b49d, 0x22ac, 0x4161, { 0xac, 0x8a, 0x99, 0x16, 0xe8, 0xfa, 0x3f, 0x7f } };
#define INTERFACE IInitializeWithStream
DECLARE_INTERFACE_(IInitializeWithStream, IUnknown)
{
	STDMETHOD(QueryInterface)(THIS_ REFIID, PVOID *) PURE;
	STDMETHOD_(ULONG, AddRef)(THIS) PURE;
	STDMETHOD_(ULONG, Release)(THIS) PURE;
	STDMETHOD(Initialize)(THIS_ IStream *, DWORD) PURE;
};
#undef INTERFACE

#endif

const IID IID_IExtractImage =
	{ 0xbb2e617c, 0x0920, 0x11d1, { 0x9a, 0x0b, 0x00, 0xc0, 0x4f, 0xc2, 0xd6, 0xc1 } };

enum WTS_ALPHATYPE
{
	WTSAT_UNKNOWN = 0,
	WTSAT_RGB = 1,
	WTSAT_ARGB = 2
};

static const IID IID_IThumbnailProvider =
	{ 0xe357fccd, 0xa995, 0x4576, { 0xb0, 0x1f, 0x23, 0x46, 0x30, 0x15, 0x4e, 0x96 } };
#define INTERFACE IThumbnailProvider
DECLARE_INTERFACE_(IThumbnailProvider, IUnknown)
{
	STDMETHOD(QueryInterface)(THIS_ REFIID, PVOID *) PURE;
	STDMETHOD_(ULONG, AddRef)(THIS) PURE;
	STDMETHOD_(ULONG, Release)(THIS) PURE;
	STDMETHOD(GetThumbnail)(THIS_ UINT, HBITMAP *, WTS_ALPHATYPE *) PURE;
};
#undef INTERFACE

#include "fail.h"

static const char extensions[][5] =
	{ ".256", ".4mi", ".4pl", ".4pm", ".a4r", ".acs", ".agp", ".all", ".ap2", ".ap3",
	  ".apc", ".apl", ".app", ".apv", ".art", ".bg9", ".bkg", ".bl1", ".bl2", ".bl3",
	  ".bru", ".ca1", ".ca2", ".ca3", ".cci", ".chr", ".cin", ".cpr", ".cpt", ".dc1",
	  ".del", ".dg1", ".dgc", ".dgp", ".dgu", ".din", ".dlm", ".doo", ".dph", ".drg",
	  ".esc", ".fnt", ".ftc", ".fwa", ".g09", ".g10", ".g11", ".gfb", ".ghg", ".god",
	  ".gr7", ".gr8", ".gr9", ".hip", ".hpm", ".hr",  ".hr2", ".icn", ".iff", ".ige",
	  ".ilc", ".imn", ".ing", ".inp", ".int", ".ip2", ".ipc", ".ir2", ".irg", ".ist",
	  ".jgp", ".max", ".mbg", ".mch", ".mcp", ".mcs", ".mgp", ".mic", ".mis", ".neo",
	  ".nlq", ".pac", ".pc1", ".pc2", ".pc3", ".pcs", ".pgc", ".pgf", ".pi1", ".pi2",
	  ".pi3", ".pi4", ".pi9", ".pic", ".pla", ".plm", ".pmd", ".pzm", ".raw", ".rgb",
	  ".rip", ".rm0", ".rm1", ".rm2", ".rm3", ".rm4", ".sge", ".shc", ".shp", ".spc",
	  ".sps", ".spu", ".sxs", ".tip", ".tn1", ".tn2", ".tn3", ".tny", ".trp", ".tru",
	  ".vzi", ".wnd", ".xlp" };
#define N_EXTS (int) (sizeof(extensions) / sizeof(extensions[0]))

static HINSTANCE g_hDll;
static LONG g_cRef = 0;

static void DllAddRef(void)
{
	InterlockedIncrement(&g_cRef);
}

static void DllRelease(void)
{
	InterlockedDecrement(&g_cRef);
}

#define CLSID_FAILThumbProvider_str "{3C450D81-B6BD-4D8C-923C-FC659ABB27D3}"
static const GUID CLSID_FAILThumbProvider =
	{ 0x3c450d81, 0xb6bd, 0x4d8c, { 0x92, 0x3c, 0xfc, 0x65, 0x9a, 0xbb, 0x27, 0xd3 } };

class CFAILThumbProvider : IPersistFile, IExtractImage, IInitializeWithStream, IThumbnailProvider
{
	LONG m_cRef;
	IStream *m_pstream;
	FAIL *m_pFail;
	LPWSTR m_filename;
	int m_contentLen;
	unsigned char m_content[FAIL_MAX_CONTENT_LENGTH];

	HRESULT Decode(LPCWSTR pszFilename, HBITMAP *phBitmap)
	{
		// filename to ASCII
		int cbFilename = WideCharToMultiByte(CP_ACP, 0, pszFilename, -1, NULL, 0, NULL, NULL);
		char filename[cbFilename];
		if (WideCharToMultiByte(CP_ACP, 0, pszFilename, -1, filename, cbFilename, NULL, NULL) <= 0)
			return HRESULT_FROM_WIN32(GetLastError());

		// decode
		if (!FAIL_Decode(m_pFail, filename, m_content, m_contentLen))
			return E_FAIL;
		int width = FAIL_GetWidth(m_pFail);
		int height = FAIL_GetHeight(m_pFail);
		const int *pixels = FAIL_GetPixels(m_pFail);

		// convert to bitmap
		BITMAPINFO bmi = {};
		bmi.bmiHeader.biSize = sizeof(bmi.bmiHeader);
		bmi.bmiHeader.biWidth = width;
		bmi.bmiHeader.biHeight = -height;
		bmi.bmiHeader.biPlanes = 1;
		bmi.bmiHeader.biBitCount = 32;
		bmi.bmiHeader.biCompression = BI_RGB;
		int *pBits;
		HBITMAP hbmp = CreateDIBSection(NULL, &bmi, DIB_RGB_COLORS, (void **) &pBits, NULL, 0);
		if (hbmp == NULL)
			return E_OUTOFMEMORY;
		int pixels_count = width * height;
		for (int i = 0; i < pixels_count; i++)
			pBits[i] = pixels[i] | 0xff000000;
		*phBitmap = hbmp;
		return S_OK;
	}

public:

	CFAILThumbProvider() : m_cRef(1), m_pstream(NULL), m_filename(NULL)
	{
		DllAddRef();
		m_pFail = FAIL_New();
	}

	~CFAILThumbProvider()
	{
		if (m_pstream != NULL)
			m_pstream->Release();
		free(m_filename);
		FAIL_Delete(m_pFail);
		DllRelease();
	}

	STDMETHODIMP QueryInterface(REFIID riid, void **ppv)
	{
		if (riid == IID_IUnknown || riid == IID_IPersistFile) {
			*ppv = (IPersistFile *) this;
			AddRef();
			return S_OK;
		}
		if (riid == IID_IExtractImage) {
			*ppv = (IExtractImage *) this;
			AddRef();
			return S_OK;
		}
		if (riid == IID_IInitializeWithStream) {
			*ppv = (IInitializeWithStream *) this;
			AddRef();
			return S_OK;
		}
		if (riid == IID_IThumbnailProvider) {
			*ppv = (IThumbnailProvider *) this;
			AddRef();
			return S_OK;
		}
		*ppv = NULL;
		return E_NOINTERFACE;
	}

	STDMETHODIMP_(ULONG) AddRef()
	{
		return InterlockedIncrement(&m_cRef);
	}

	STDMETHODIMP_(ULONG) Release()
	{
		ULONG r = InterlockedDecrement(&m_cRef);
		if (r == 0)
			delete this;
		return r;
	}

	// IPersistFile

	STDMETHODIMP GetClassID(CLSID *pClassID)
	{
		*pClassID = CLSID_FAILThumbProvider;
		return S_OK;
	}

	STDMETHODIMP IsDirty()
	{
		return S_FALSE;
	}

	STDMETHODIMP Load(LPCOLESTR pszFileName, DWORD dwMode)
	{
		if (m_pFail == NULL)
			return E_OUTOFMEMORY;

		HANDLE fh = CreateFileW(pszFileName, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL | FILE_FLAG_SEQUENTIAL_SCAN, NULL);
		if (fh == INVALID_HANDLE_VALUE)
			return HRESULT_FROM_WIN32(GetLastError());
		free(m_filename);
		m_filename = NULL;
		if (!ReadFile(fh, m_content, sizeof(m_content), (LPDWORD) &m_contentLen, NULL)) {
			HRESULT hr = HRESULT_FROM_WIN32(GetLastError());
			CloseHandle(fh);
			return hr;
		}
		CloseHandle(fh);

		m_filename = _wcsdup(pszFileName);
		if (m_filename == NULL)
			return E_OUTOFMEMORY;

		return S_OK;
	}

	STDMETHODIMP Save(LPCOLESTR pszFileName, BOOL fRemember)
	{
		return E_NOTIMPL;
	}

	STDMETHODIMP SaveCompleted(LPCOLESTR pszFileName)
	{
		return S_OK;
	}

	STDMETHODIMP GetCurFile(LPOLESTR *ppszFileName)
	{
		return E_NOTIMPL;
	}

	// IExtractImage

	STDMETHODIMP GetLocation(LPWSTR pszPathBuffer, DWORD cchMax, DWORD *pdwPriority, const SIZE *prgSize, DWORD pdwRecClrDepth, DWORD *pdwFlags)
	{
		if (m_filename == NULL)
			return E_UNEXPECTED;
		if (pszPathBuffer != NULL)
			lstrcpynW(pszPathBuffer, m_filename, cchMax);
		if (pdwFlags != NULL)
			*pdwFlags = IEIFLAG_CACHE;
		return S_OK;
	}

	STDMETHODIMP Extract(HBITMAP *phBmpImage)
	{
		if (m_filename == NULL)
			return E_UNEXPECTED;
		return Decode(m_filename, phBmpImage);
	}

	// IInitializeWithStream

	STDMETHODIMP Initialize(IStream *pstream, DWORD grfMode)
	{
		if (m_pstream != NULL)
			return E_UNEXPECTED;
		m_pstream = pstream;
		pstream->AddRef();
		return S_OK;
	}

	// IThumbnailProvider

	STDMETHODIMP GetThumbnail(UINT cx, HBITMAP *phbmp, WTS_ALPHATYPE *pdwAlpha)
	{
		if (m_pstream == NULL)
			return E_UNEXPECTED;
		if (m_pFail == NULL)
			return E_OUTOFMEMORY;

		// get filename
		STATSTG statstg;
		HRESULT hr = m_pstream->Stat(&statstg, STATFLAG_DEFAULT);
		if (FAILED(hr))
			return hr;

		// get contents
		hr = m_pstream->Read(m_content, sizeof(m_content), (ULONG *) &m_contentLen);
		if (FAILED(hr)) {
			CoTaskMemFree(statstg.pwcsName);
			return hr;
		}

		// decode
		hr = Decode(statstg.pwcsName, phbmp);
		CoTaskMemFree(statstg.pwcsName);
		*pdwAlpha = WTSAT_RGB;
		return hr;
	}
};

class CFAILThumbProviderFactory : IClassFactory
{
public:

	STDMETHODIMP QueryInterface(REFIID riid, void **ppv)
	{
		if (riid == IID_IUnknown || riid == IID_IClassFactory) {
			*ppv = (IClassFactory *) this;
			DllAddRef();
			return S_OK;
		}
		*ppv = NULL;
		return E_NOINTERFACE;
	}

	STDMETHODIMP_(ULONG) AddRef()
	{
		DllAddRef();
		return 2;
	}

	STDMETHODIMP_(ULONG) Release()
	{
		DllRelease();
		return 1;
	}

	STDMETHODIMP CreateInstance(LPUNKNOWN punkOuter, REFIID riid, void **ppv)
	{
		*ppv = NULL;
		if (punkOuter != NULL)
			return CLASS_E_NOAGGREGATION;
		CFAILThumbProvider *punk = new CFAILThumbProvider;
		if (punk == NULL)
			return E_OUTOFMEMORY;
		HRESULT hr = punk->QueryInterface(riid, ppv);
		punk->Release();
		return hr;
	}

	STDMETHODIMP LockServer(BOOL fLock)
	{
		if (fLock)
			DllAddRef();
		else
			DllRelease();
		return S_OK;
	};
};

STDAPI_(BOOL) DllMain(HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved)
{
	if (dwReason == DLL_PROCESS_ATTACH)
		g_hDll = hInstance;
	return TRUE;
}

static BOOL RegisterCLSID(HKEY hk1, LPCSTR subkey)
{
	HKEY hk2;
	if (RegCreateKeyEx(hk1, subkey, 0, NULL, 0, KEY_WRITE, NULL, &hk2, NULL) != ERROR_SUCCESS)
		return FALSE;
	static const char CLSID_FAILThumbProvider_str2[] = CLSID_FAILThumbProvider_str;
	BOOL ok = RegSetValueEx(hk2, NULL, 0, REG_SZ, (CONST BYTE *) CLSID_FAILThumbProvider_str2, sizeof(CLSID_FAILThumbProvider_str2)) == ERROR_SUCCESS;
	RegCloseKey(hk2);
	return ok;
}

STDAPI __declspec(dllexport) DllRegisterServer(void)
{
	HKEY hk1;
	if (RegCreateKeyEx(HKEY_CLASSES_ROOT, "CLSID\\" CLSID_FAILThumbProvider_str, 0, NULL, 0, KEY_WRITE, NULL, &hk1, NULL) != ERROR_SUCCESS)
		return E_FAIL;
	HKEY hk2;
	if (RegCreateKeyEx(hk1, "InProcServer32", 0, NULL, 0, KEY_WRITE, NULL, &hk2, NULL) != ERROR_SUCCESS) {
		RegCloseKey(hk1);
		return E_FAIL;
	}
	char szModulePath[MAX_PATH];
	DWORD nModulePathLen = GetModuleFileName(g_hDll, szModulePath, MAX_PATH);
	static const char szThreadingModel[] = "Both";
	if (RegSetValueEx(hk2, NULL, 0, REG_SZ, (CONST BYTE *) szModulePath, nModulePathLen) != ERROR_SUCCESS
	 || RegSetValueEx(hk2, "ThreadingModel", 0, REG_SZ, (CONST BYTE *) szThreadingModel, sizeof(szThreadingModel)) != ERROR_SUCCESS) {
		RegCloseKey(hk2);
		RegCloseKey(hk1);
		return E_FAIL;
	}
	RegCloseKey(hk2);
	RegCloseKey(hk1);

	for (int i = 0; i < N_EXTS; i++) {
		if (RegCreateKeyEx(HKEY_CLASSES_ROOT, extensions[i], 0, NULL, 0, KEY_WRITE, NULL, &hk1, NULL) != ERROR_SUCCESS)
			return E_FAIL;
		if (!RegisterCLSID(hk1, "ShellEx\\{bb2e617c-0920-11d1-9a0b-00c04fc2d6c1}") // IPersistFile+IExtractImage
		 || !RegisterCLSID(hk1, "ShellEx\\{e357fccd-a995-4576-b01f-234630154e96}")) // IInitializeWithStream+IThumbnailProvider
		{
			RegCloseKey(hk1);
			return E_FAIL;
		}
		RegCloseKey(hk1);
	}

	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Shell Extensions\\Approved", 0, KEY_SET_VALUE, &hk1) != ERROR_SUCCESS)
		return E_FAIL;
	static const char szDescription[] = "FAIL Thumbnail Handler";
	if (RegSetValueEx(hk1, CLSID_FAILThumbProvider_str, 0, REG_SZ, (CONST BYTE *) szDescription, sizeof(szDescription)) != ERROR_SUCCESS) {
		RegCloseKey(hk1);
		return E_FAIL;
	}
	RegCloseKey(hk1);
	return S_OK;
}

STDAPI __declspec(dllexport) DllUnregisterServer(void)
{
	HKEY hk1;
	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Shell Extensions\\Approved", 0, KEY_SET_VALUE, &hk1) == ERROR_SUCCESS) {
		RegDeleteValue(hk1, CLSID_FAILThumbProvider_str);
		RegCloseKey(hk1);
	}
	for (int i = 0; i < N_EXTS; i++) {
		if (RegOpenKeyEx(HKEY_CLASSES_ROOT, extensions[i], 0, DELETE, &hk1) == ERROR_SUCCESS) {
			RegDeleteKey(hk1, "ShellEx\\{bb2e617c-0920-11d1-9a0b-00c04fc2d6c1}"); // IPersistFile+IExtractImage
			RegDeleteKey(hk1, "ShellEx\\{e357fccd-a995-4576-b01f-234630154e96}"); // IInitializeWithStream+IThumbnailProvider
			RegCloseKey(hk1);
		}
		RegDeleteKey(HKEY_CLASSES_ROOT, extensions[i]);
	}
	RegDeleteKey(HKEY_CLASSES_ROOT, "CLSID\\" CLSID_FAILThumbProvider_str "\\InProcServer32");
	RegDeleteKey(HKEY_CLASSES_ROOT, "CLSID\\" CLSID_FAILThumbProvider_str);
	return S_OK;
}

STDAPI __declspec(dllexport) DllGetClassObject(REFCLSID rclsid, REFIID riid, LPVOID* ppv)
{
	if (ppv == NULL)
		return E_INVALIDARG;
	if (rclsid == CLSID_FAILThumbProvider) {
		static CFAILThumbProviderFactory g_ClassFactory;
		return g_ClassFactory.QueryInterface(riid, ppv);
	}
	*ppv = NULL;
	return CLASS_E_CLASSNOTAVAILABLE;
}

STDAPI __declspec(dllexport) DllCanUnloadNow(void)
{
	return g_cRef == 0 ? S_OK : S_FALSE;
}
