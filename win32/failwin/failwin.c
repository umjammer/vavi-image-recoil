/*
 * failwin.c - Windows API port of FAIL
 *
 * Copyright (C) 2009  Piotr Fusik and Adrian Matoga
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

#include <windows.h>
#include <string.h>

#include "fail.h"
#include "pngsave.h"
#include "failwin.h"

#define APP_TITLE        "FAILWin"
#define WND_CLASS_NAME   "FAILWin"

static HWND hWnd;
static char current_filename[MAX_PATH] = "";
static int width = 0;
static int height;
static int colors;
static byte pixels[FAIL_PIXELS_MAX];
static byte palette[FAIL_PALETTE_MAX];

static void ShowError(const char *message)
{
	MessageBox(hWnd, message, APP_TITLE, MB_OK | MB_ICONERROR);
}

static BOOL LoadFile(const char *filename, byte *buffer, int *len)
{
	HANDLE fh;
	BOOL ok;
	fh = CreateFile(filename, GENERIC_READ, 0, NULL, OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL | FILE_FLAG_SEQUENTIAL_SCAN, NULL);
	if (fh == INVALID_HANDLE_VALUE) {
		ShowError("Cannot open file");
		return FALSE;
	}
	ok = ReadFile(fh, buffer, *len, (LPDWORD) len, NULL);
	CloseHandle(fh);
	return ok;
}

static void OpenImage(void)
{
	byte image[FAIL_IMAGE_MAX];
	int image_len;
	image_len = sizeof(image);
	if (!LoadFile(current_filename, image, &image_len))
		return;
	if (!FAIL_DecodeImage(current_filename, image, image_len, NULL, &width, &height, &colors, pixels, palette)) {
		width = 0;
		ShowError("Decoding error");
		return;
	}
	InvalidateRect(hWnd, NULL, TRUE);
}

static void SelectAndOpenImage(void)
{
	static OPENFILENAME ofn = {
		sizeof(OPENFILENAME),
		NULL,
		0,
		"All supported\0"
		"*.ap3;*.apc;*.cci;*.cin;*.cpr;*.gr8;*.gr9;*.hip;*.hr;*.ilc;*.inp;*.int;*.mic;*.pic;*.plm;*.rip;*.tip\0"
		"\0",
		NULL,
		0,
		1,
		current_filename,
		MAX_PATH,
		NULL,
		0,
		NULL,
		"Select 8-bit Atari image",
		OFN_ENABLESIZING | OFN_EXPLORER | OFN_HIDEREADONLY | OFN_FILEMUSTEXIST | OFN_PATHMUSTEXIST,
		0,
		0,
		NULL,
		0,
		NULL,
		NULL
	};
	ofn.hwndOwner = hWnd;
	if (GetOpenFileName(&ofn))
		OpenImage();
}

static void SelectAndSaveImage(void)
{
	static char png_filename[MAX_PATH];
	static OPENFILENAME ofn = {
		sizeof(OPENFILENAME),
		NULL,
		0,
		"PNG images (*.png)\0*.png\0\0",
		NULL,
		0,
		0,
		png_filename,
		MAX_PATH,
		NULL,
		0,
		NULL,
		"Select output file",
		OFN_ENABLESIZING | OFN_EXPLORER | OFN_OVERWRITEPROMPT,
		0,
		0,
		"png",
		0,
		NULL,
		NULL
	};
	ofn.hwndOwner = hWnd;
	if (!GetSaveFileName(&ofn))
		return;
	if (!PNG_Save(png_filename, width, height, colors, pixels, palette))
		ShowError("Error writing file");
}

static void SwapRedAndBlue(void)
{
	byte *p;
	for (p = pixels + width * height * 3; (p -= 3) >= pixels; ) {
		byte t = p[0];
		p[0] = p[2];
		p[2] = t;
	}
}

static LRESULT CALLBACK MainWndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	int idc;
	PCOPYDATASTRUCT pcds;
	switch (msg) {
	case WM_PAINT:
		if (width > 0) {
			PAINTSTRUCT ps;
			HDC hdc;
			RECT rect;
			struct {
				BITMAPINFOHEADER bmiHeader;
				RGBQUAD bmiColors[256];
			} bmi;
			int i;
			bmi.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
			bmi.bmiHeader.biWidth = width;
			bmi.bmiHeader.biHeight = -height;
			bmi.bmiHeader.biPlanes = 1;
			bmi.bmiHeader.biBitCount = colors <= 256 ? 8 : 24;
			bmi.bmiHeader.biCompression = BI_RGB;
			bmi.bmiHeader.biSizeImage = 0;
			bmi.bmiHeader.biXPelsPerMeter = 1000;
			bmi.bmiHeader.biYPelsPerMeter = 1000;
			bmi.bmiHeader.biClrUsed = 0;
			bmi.bmiHeader.biClrImportant = 0;
			if (colors <= 256) {
				for (i = 0; i < colors; i++) {
					bmi.bmiColors[i].rgbRed = palette[3 * i];
					bmi.bmiColors[i].rgbGreen = palette[3 * i + 1];
					bmi.bmiColors[i].rgbBlue = palette[3 * i + 2];
				}
			}
			else
				SwapRedAndBlue();
			hdc = BeginPaint(hWnd, &ps);
			GetClientRect(hWnd, &rect);
			StretchDIBits(hdc, 0, 0, rect.right, rect.bottom, 0, 0, width, height,
				pixels, (CONST BITMAPINFO *) &bmi, DIB_RGB_COLORS, SRCCOPY);
			EndPaint(hWnd, &ps);
			if (colors > 256)
				SwapRedAndBlue();
		}
		break;
	case WM_COMMAND:
		idc = LOWORD(wParam);
		switch (idc) {
		case IDM_OPEN:
			SelectAndOpenImage();
			break;
		case IDM_SAVEAS:
			SelectAndSaveImage();
			break;
		case IDM_EXIT:
			PostQuitMessage(0);
			break;
		case IDM_ABOUT:
			MessageBox(hWnd,
				FAIL_CREDITS
				"\n"
				FAIL_COPYRIGHT,
				APP_TITLE " " FAIL_VERSION,
				MB_OK | MB_ICONINFORMATION);
			break;
		default:
			break;
		}
		break;
	case WM_DESTROY:
		PostQuitMessage(0);
		break;
	case WM_COPYDATA:
		pcds = (PCOPYDATASTRUCT) lParam;
		if (pcds->dwData == 'O' && pcds->cbData <= sizeof(current_filename)) {
			memcpy(current_filename, pcds->lpData, pcds->cbData);
			OpenImage();
		}
		break;
	default:
		return DefWindowProc(hWnd, msg, wParam, lParam);
	}
	return 0;
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow)
{
	char *pb;
	char *pe;
	WNDCLASS wc;
	HACCEL hAccel;
	MSG msg;

	for (pb = lpCmdLine; *pb == ' ' || *pb == '\t'; pb++);
	for (pe = pb; *pe != '\0'; pe++);
	while (--pe > pb && (*pe == ' ' || *pe == '\t'));
	/* Now pb and pe point at respectively the first and last non-blank
	   character in lpCmdLine. If pb > pe then the command line is blank. */
	if (*pb == '"' && *pe == '"')
		pb++;
	else
		pe++;
	*pe = '\0';
	/* Now pb contains the filename, if any, specified on the command line. */

	hWnd = FindWindow(WND_CLASS_NAME, NULL);
	if (hWnd != NULL) {
		/* an instance of FAILWin is already running */
		if (*pb != '\0') {
			/* pass the filename */
			COPYDATASTRUCT cds = { 'O', (DWORD) (pe + 1 - pb), pb };
			SendMessage(hWnd, WM_COPYDATA, (WPARAM) NULL, (LPARAM) &cds);
		}
		else {
			/* bring the open dialog to top */
			HWND hChild = GetLastActivePopup(hWnd);
			if (hChild != hWnd)
				SetForegroundWindow(hChild);
		}
		return 0;
	}

	wc.style = CS_OWNDC | CS_VREDRAW | CS_HREDRAW;
	wc.lpfnWndProc = MainWndProc;
	wc.cbClsExtra = 0;
	wc.cbWndExtra = 0;
	wc.hInstance = hInstance;
	wc.hIcon = NULL; // TODO LoadIcon(hInstance, MAKEINTRESOURCE(IDI_APP));
	wc.hCursor = LoadCursor(NULL, IDC_ARROW);
	wc.hbrBackground = (HBRUSH) (COLOR_WINDOW + 1);
	wc.lpszMenuName = MAKEINTRESOURCE(IDR_MENU);
	wc.lpszClassName = WND_CLASS_NAME;
	RegisterClass(&wc);

	hWnd = CreateWindow(WND_CLASS_NAME,
		APP_TITLE,
		WS_VISIBLE | WS_OVERLAPPEDWINDOW,
		CW_USEDEFAULT,
		CW_USEDEFAULT,
		CW_USEDEFAULT,
		CW_USEDEFAULT,
		NULL,
		NULL,
		hInstance,
		NULL
	);

	hAccel = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDR_ACCELERATORS));

	if (*pb != '\0') {
		memcpy(current_filename, pb, pe + 1 - pb);
		OpenImage();
	}
	else
		SelectAndOpenImage();

	while (GetMessage(&msg, NULL, 0, 0)) {
		if (!TranslateAccelerator(hWnd, hAccel, &msg)) {
			TranslateMessage(&msg);
			DispatchMessage(&msg);
		}
	}
	return 0;
}
