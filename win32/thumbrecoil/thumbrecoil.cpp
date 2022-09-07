/*
 * thumbrecoil.cpp - Windows thumbnail provider for RECOIL
 *
 * Copyright (C) 2011-2022  Piotr Fusik
 *
 * This file is part of RECOIL (Retro Computer Image Library),
 * see http://recoil.sourceforge.net
 *
 * RECOIL is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * RECOIL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RECOIL; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/* There are two separate implementations for different Windows versions:
   IPersistFile+IExtractImage for Windows 2000 and XP.
   IInitializeWithStream+IThumbnailProvider for Windows Vista+
   (even though the Windows 2000/XP interfaces could work). */

#include <windows.h>
#include <objidl.h>
#include <shobjidl.h>
#include <stdint.h>

#if THUMBRECOIL_VISTA
#include <thumbcache.h>
#include <limits.h>
#include <stdlib.h>
#endif

#include "recoil-win32.h"
#include "formats.h"

static const char extensions[][6] = { THUMBRECOIL_EXTS };

static HINSTANCE g_hDll;
static LONG g_cRef = 0;

static void DllAddRef()
{
	InterlockedIncrement(&g_cRef);
}

static void DllRelease()
{
	InterlockedDecrement(&g_cRef);
}

#define CLSID_RECOILThumbProvider_str "{3C450D81-B6BD-4D8C-923C-FC659ABB27D3}"
static const GUID CLSID_RECOILThumbProvider =
	{ 0x3c450d81, 0xb6bd, 0x4d8c, { 0x92, 0x3c, 0xfc, 0x65, 0x9a, 0xbb, 0x27, 0xd3 } };

class CRECOILThumbProvider : IPersistFile, IExtractImage
#if THUMBRECOIL_VISTA
	, IInitializeWithStream, IThumbnailProvider
#endif
{
	LONG m_cRef = 1;
#if THUMBRECOIL_VISTA
	IStream *m_pstream = nullptr;
#endif
	RECOIL * const m_pRecoil;
	LPWSTR m_filename = nullptr;
	bool m_loaded = false;

	HRESULT CreateBitmap(HBITMAP *phBitmap)
	{
		int width = RECOIL_GetWidth(m_pRecoil);
		int height = RECOIL_GetHeight(m_pRecoil);
		const int *pixels = RECOIL_GetPixels(m_pRecoil);

		BITMAPINFO bmi {};
		bmi.bmiHeader.biSize = sizeof(bmi.bmiHeader);
		bmi.bmiHeader.biWidth = width;
		bmi.bmiHeader.biHeight = -height;
		bmi.bmiHeader.biPlanes = 1;
		bmi.bmiHeader.biBitCount = 32;
		bmi.bmiHeader.biCompression = BI_RGB;
		int *pBits;
		HBITMAP hbmp = CreateDIBSection(nullptr, &bmi, DIB_RGB_COLORS, reinterpret_cast<void **>(&pBits), nullptr, 0);
		if (hbmp == nullptr)
			return E_OUTOFMEMORY;
		int pixelsLen = width * height;
		for (int i = 0; i < pixelsLen; i++)
			pBits[i] = pixels[i] | 0xff000000;
		*phBitmap = hbmp;
		return S_OK;
	}

public:

	CRECOILThumbProvider() : m_pRecoil(RECOIL_New())
	{
		DllAddRef();
	}

	virtual ~CRECOILThumbProvider()
	{
#if THUMBRECOIL_VISTA
		if (m_pstream != nullptr)
			m_pstream->Release();
#endif
		free(m_filename);
		RECOIL_Delete(m_pRecoil);
		DllRelease();
	}

	STDMETHODIMP QueryInterface(REFIID riid, void **ppv)
	{
		if (ppv == nullptr)
			return E_POINTER;
		if (riid == __uuidof(IUnknown) || riid == __uuidof(IPersistFile)) {
			*ppv = static_cast<IPersistFile *>(this);
			AddRef();
			return S_OK;
		}
		if (riid == __uuidof(IExtractImage)) {
			*ppv = static_cast<IExtractImage *>(this);
			AddRef();
			return S_OK;
		}
#if THUMBRECOIL_VISTA
		if (riid == __uuidof(IInitializeWithStream)) {
			*ppv = static_cast<IInitializeWithStream *>(this);
			AddRef();
			return S_OK;
		}
		if (riid == __uuidof(IThumbnailProvider)) {
			*ppv = static_cast<IThumbnailProvider *>(this);
			AddRef();
			return S_OK;
		}
#endif
		*ppv = nullptr;
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
		*pClassID = CLSID_RECOILThumbProvider;
		return S_OK;
	}

	STDMETHODIMP IsDirty()
	{
		return S_FALSE;
	}

	STDMETHODIMP Load(LPCOLESTR pszFileName, DWORD dwMode)
	{
		if (m_pRecoil == nullptr)
			return E_OUTOFMEMORY;

		m_loaded = false;
		free(m_filename);
		m_filename = _wcsdup(pszFileName);
		if (m_filename == nullptr)
			return E_OUTOFMEMORY;

		if (!RECOILWin32_LoadW(m_pRecoil, m_filename))
			return E_FAIL;

		m_loaded = true;
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
		if (m_filename == nullptr)
			return E_UNEXPECTED;
		if (pszPathBuffer != nullptr)
			lstrcpynW(pszPathBuffer, m_filename, cchMax);
		if (pdwFlags != nullptr)
			*pdwFlags = IEIFLAG_CACHE;
		return S_OK;
	}

	STDMETHODIMP Extract(HBITMAP *phBmpImage)
	{
		if (!m_loaded)
			return E_UNEXPECTED;
		return CreateBitmap(phBmpImage);
	}

#if THUMBRECOIL_VISTA

	// IInitializeWithStream

	STDMETHODIMP Initialize(IStream *pstream, DWORD grfMode)
	{
		if (m_pstream != nullptr)
			return E_UNEXPECTED;
		m_pstream = pstream;
		pstream->AddRef();
		return S_OK;
	}

	// IThumbnailProvider

	STDMETHODIMP GetThumbnail(UINT cx, HBITMAP *phbmp, WTS_ALPHATYPE *pdwAlpha)
	{
		if (m_pstream == nullptr)
			return E_UNEXPECTED;
		if (m_pRecoil == nullptr)
			return E_OUTOFMEMORY;

		// get filename and length
		STATSTG statstg;
		HRESULT hr = m_pstream->Stat(&statstg, STATFLAG_DEFAULT);
		if (FAILED(hr))
			return hr;
		if (statstg.cbSize.QuadPart > INT_MAX) {
			CoTaskMemFree(statstg.pwcsName);
			return E_FAIL;
		}

		// get contents
		int contentLen = statstg.cbSize.u.LowPart;
		uint8_t *content = (uint8_t *) malloc(contentLen);
		if (content == NULL) {
			CoTaskMemFree(statstg.pwcsName);
			return E_OUTOFMEMORY;
		}
		hr = m_pstream->Read(content, contentLen, reinterpret_cast<ULONG *>(&contentLen));
		if (FAILED(hr)) {
			CoTaskMemFree(statstg.pwcsName);
			return hr;
		}

		// decode
		bool ok = RECOILWin32_DecodeW(m_pRecoil, statstg.pwcsName, content, contentLen);
		free(content);
		CoTaskMemFree(statstg.pwcsName);
		if (!ok)
			return E_FAIL;

		hr = CreateBitmap(phbmp);
		*pdwAlpha = WTSAT_RGB;
		return hr;
	}

#endif
};

class CRECOILThumbProviderFactory : IClassFactory
{
public:

	STDMETHODIMP QueryInterface(REFIID riid, void **ppv)
	{
		if (ppv == nullptr)
			return E_POINTER;
		if (riid == __uuidof(IUnknown) || riid == __uuidof(IClassFactory)) {
			*ppv = static_cast<IClassFactory *>(this);
			DllAddRef();
			return S_OK;
		}
		*ppv = nullptr;
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
		*ppv = nullptr;
		if (punkOuter != nullptr)
			return CLASS_E_NOAGGREGATION;
		CRECOILThumbProvider *punk = new CRECOILThumbProvider;
		if (punk == nullptr)
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
	}
};

STDAPI_(BOOL) DllMain(HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved)
{
	if (dwReason == DLL_PROCESS_ATTACH)
		g_hDll = hInstance;
	return TRUE;
}

static bool MyRegCreateKey(HKEY hk1, LPCSTR subkey, PHKEY hk2)
{
	return RegCreateKeyEx(hk1, subkey, 0, nullptr, 0, KEY_WRITE, nullptr, hk2, nullptr) == ERROR_SUCCESS;
}

static bool MyRegSetValueString(HKEY hk1, LPCSTR name, LPCSTR data)
{
	return RegSetValueEx(hk1, name, 0, REG_SZ, reinterpret_cast<const BYTE *>(data), strlen(data) + 1) == ERROR_SUCCESS;
}

static bool RegisterCLSID(HKEY hk1, LPCSTR subkey)
{
	HKEY hk2;
	if (!MyRegCreateKey(hk1, subkey, &hk2))
		return false;
	bool ok = MyRegSetValueString(hk2, nullptr, CLSID_RECOILThumbProvider_str);
	RegCloseKey(hk2);
	return ok;
}

STDAPI __declspec(dllexport) DllRegisterServer()
{
	char szModulePath[MAX_PATH];
	if (GetModuleFileName(g_hDll, szModulePath, MAX_PATH) == 0)
		return E_FAIL;
	HKEY hk1;
	if (!MyRegCreateKey(HKEY_CLASSES_ROOT, "CLSID\\" CLSID_RECOILThumbProvider_str, &hk1))
		return E_FAIL;
	HKEY hk2;
	if (!MyRegCreateKey(hk1, "InProcServer32", &hk2)) {
		RegCloseKey(hk1);
		return E_FAIL;
	}
	bool ok = MyRegSetValueString(hk2, nullptr, szModulePath)
		&& MyRegSetValueString(hk2, "ThreadingModel", "Both");
	RegCloseKey(hk2);
	RegCloseKey(hk1);
	if (!ok)
		return E_FAIL;

	for (LPCSTR ext : extensions) {
		if (!MyRegCreateKey(HKEY_CLASSES_ROOT, ext, &hk1))
			return E_FAIL;
		ok = RegisterCLSID(hk1, "ShellEx\\{bb2e617c-0920-11d1-9a0b-00c04fc2d6c1}") // IPersistFile+IExtractImage
#if THUMBRECOIL_VISTA
			&& RegisterCLSID(hk1, "ShellEx\\{e357fccd-a995-4576-b01f-234630154e96}") // IInitializeWithStream+IThumbnailProvider
#endif
			&& MyRegSetValueString(hk1, "PerceivedType", "image");
		RegCloseKey(hk1);
		if (!ok)
			return E_FAIL;
	}

	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Shell Extensions\\Approved", 0, KEY_SET_VALUE, &hk1) != ERROR_SUCCESS)
		return E_FAIL;
	ok = MyRegSetValueString(hk1, CLSID_RECOILThumbProvider_str, "RECOIL Thumbnail Handler");
	RegCloseKey(hk1);
	if (!ok)
		return E_FAIL;
	return S_OK;
}

STDAPI __declspec(dllexport) DllUnregisterServer()
{
	HKEY hk1;
	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Shell Extensions\\Approved", 0, KEY_SET_VALUE, &hk1) == ERROR_SUCCESS) {
		RegDeleteValue(hk1, CLSID_RECOILThumbProvider_str);
		RegCloseKey(hk1);
	}
	for (LPCSTR ext : extensions) {
		if (RegOpenKeyEx(HKEY_CLASSES_ROOT, ext, 0, DELETE, &hk1) == ERROR_SUCCESS) {
			RegDeleteKey(hk1, "ShellEx\\{bb2e617c-0920-11d1-9a0b-00c04fc2d6c1}"); // IPersistFile+IExtractImage
#if THUMBRECOIL_VISTA
			RegDeleteKey(hk1, "ShellEx\\{e357fccd-a995-4576-b01f-234630154e96}"); // IInitializeWithStream+IThumbnailProvider
#endif
			RegCloseKey(hk1);
		}
		RegDeleteKey(HKEY_CLASSES_ROOT, ext);
	}
	RegDeleteKey(HKEY_CLASSES_ROOT, "CLSID\\" CLSID_RECOILThumbProvider_str "\\InProcServer32");
	RegDeleteKey(HKEY_CLASSES_ROOT, "CLSID\\" CLSID_RECOILThumbProvider_str);
	return S_OK;
}

STDAPI __declspec(dllexport) DllGetClassObject(REFCLSID rclsid, REFIID riid, LPVOID *ppv)
{
	if (ppv == nullptr)
		return E_INVALIDARG;
	if (rclsid == CLSID_RECOILThumbProvider) {
		static CRECOILThumbProviderFactory g_ClassFactory;
		return g_ClassFactory.QueryInterface(riid, ppv);
	}
	*ppv = nullptr;
	return CLASS_E_CLASSNOTAVAILABLE;
}

STDAPI __declspec(dllexport) DllCanUnloadNow()
{
	return g_cRef == 0 ? S_OK : S_FALSE;
}
