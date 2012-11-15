/*
 * failimagine.c - FAIL coder for Imagine
 *
 * Copyright (C) 2012  Piotr Fusik and Adrian Matoga
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
#include <malloc.h>

#include "ImagPlug.h"
#include "fail.h"

#define VERSION_NUMBER ((FAIL_VERSION_MAJOR<<24)|(FAIL_VERSION_MINOR<<16)|(FAIL_VERSION_MICRO<<8))

static BOOL IMAGINEAPI checkFile(IMAGINEPLUGINFILEINFOTABLE *fileInfoTable, IMAGINELOADPARAM *loadParam, int flags)
{
	return TRUE; /* TODO? */
}

static LPIMAGINEBITMAP IMAGINEAPI loadFile(IMAGINEPLUGINFILEINFOTABLE *fileInfoTable, IMAGINELOADPARAM *loadParam, int flags)
{
	const IMAGINEPLUGININTERFACE *iface = fileInfoTable->iface;
	char *filename;
	FAIL_ImageInfo image_info;
	byte *pixels;
	LPIMAGINEBITMAP bitmap;
	IMAGINECALLBACKPARAM param;
	int bpl;
	int y;

	if (iface == NULL)
		return NULL;

	if (iface->lpVtbl->IsUnicode()) {
		int cch = lstrlenW((LPCWSTR) loadParam->fileName) + 1;
		filename = (char *) alloca(cch * 2);
		if (filename == NULL) {
			loadParam->errorCode = IMAGINEERROR_OUTOFMEMORY;
			return NULL;
		}
		if (WideCharToMultiByte(CP_ACP, 0, (LPCWSTR) loadParam->fileName, -1, filename, cch, NULL, NULL) <= 0) {
			loadParam->errorCode = IMAGINEERROR_FILENOTFOUND;
			return NULL;
		}
	}
	else
		filename = (char *) loadParam->fileName;

	pixels = malloc(FAIL_PIXELS_MAX);
	if (pixels == NULL) {
		loadParam->errorCode = IMAGINEERROR_OUTOFMEMORY;
		return NULL;
	}

	if (!FAIL_DecodeImage(filename, loadParam->buffer, loadParam->length, NULL, &image_info, pixels)) {
		free(pixels);
		loadParam->errorCode = IMAGINEERROR_UNSUPPORTEDTYPE;
		return NULL;
	}

	bitmap = iface->lpVtbl->Create(image_info.width, image_info.height, 24, flags);
	if (bitmap == NULL) {
		free(pixels);
		loadParam->errorCode = IMAGINEERROR_OUTOFMEMORY;
		return NULL;
	}
	if ((flags & IMAGINELOADPARAM_GETINFO) != 0) {
		free(pixels);
		return bitmap;
	}

	param.dib = bitmap;
	param.param = loadParam->callback.param;
	param.overall = image_info.height - 1;
	param.message = NULL;
	bpl = image_info.width * 3;
	for (y = 0; y < image_info.height; y++) {
		LPBYTE src = pixels + y * bpl;
		LPBYTE dest = iface->lpVtbl->GetLineBits(bitmap, y);
		int x;
		for (x = 0; x < bpl; x += 3) {
			/* RGB -> BGR */
			dest[x + 2] = src[x];
			dest[x + 1] = src[x + 1];
			dest[x] = src[x + 2];
		}
		if ((flags & IMAGINELOADPARAM_CALLBACK) != 0) {
			param.current = y;
			if (!loadParam->callback.proc(&param)) {
				free(pixels);
				loadParam->errorCode = IMAGINEERROR_ABORTED;
				return bitmap;
			}
		}
	}
	free(pixels);
	return bitmap;
}

static BOOL IMAGINEAPI registerProcA(const IMAGINEPLUGININTERFACE *iface)
{
	static const IMAGINEFILEINFOITEM fileInfoItemA = {
		checkFile,
		loadFile,
		NULL,
		(LPCTSTR) "Atari Computer Image (FAIL)",
		(LPCTSTR) "GR8\0HIP\0MIC\0INT\0TIP\0INP\0HR\0GR9\0PIC\0CPR\0CIN\0CCI\0APC\0PLM\0AP3\0ILC\0RIP\0FNT\0SXS\0MCP\0GHG\0HR2\0MCH\0IGE\0" "256\0AP2\0JGP\0DGP\0ESC\0PZM\0IST\0RAW\0RGB\0MGP\0WND\0CHR\0SHP\0MBG\0FWA\0RM0\0RM1\0RM2\0RM3\0RM4\0XLP\0MAX\0SHC\0ALL\0APP\0SGE\0DLM\0BKG\0G09\0BG9\0APV\0SPC\0APL\0GR7\0G10\0G11\0ART\0DRG\0AGP\0PLA\0MIS\04PL\04MI\04PM\0PGF\0PGC\0PI1\0PI2\0PI3\0PC1\0PC2\0PC3\0NEO\0DOO\0SPU\0TNY\0TN1\0TN2\0TN3\0CA1\0CA2\0CA3\0ING\0PAC\0SPS\0GFB\0"
	};
	return iface->lpVtbl->RegisterFileType(&fileInfoItemA) != NULL;
}

static BOOL IMAGINEAPI registerProcW(const IMAGINEPLUGININTERFACE *iface)
{
	static const IMAGINEFILEINFOITEM fileInfoItemW = {
		checkFile,
		loadFile,
		NULL,
		(LPCTSTR) L"Atari Computer Image (FAIL)",
		(LPCTSTR) L"GR8\0HIP\0MIC\0INT\0TIP\0INP\0HR\0GR9\0PIC\0CPR\0CIN\0CCI\0APC\0PLM\0AP3\0ILC\0RIP\0FNT\0SXS\0MCP\0GHG\0HR2\0MCH\0IGE\0" "256\0AP2\0JGP\0DGP\0ESC\0PZM\0IST\0RAW\0RGB\0MGP\0WND\0CHR\0SHP\0MBG\0FWA\0RM0\0RM1\0RM2\0RM3\0RM4\0XLP\0MAX\0SHC\0ALL\0APP\0SGE\0DLM\0BKG\0G09\0BG9\0APV\0SPC\0APL\0GR7\0G10\0G11\0ART\0DRG\0AGP\0PLA\0MIS\04PL\04MI\04PM\0PGF\0PGC\0PI1\0PI2\0PI3\0PC1\0PC2\0PC3\0NEO\0DOO\0SPU\0TNY\0TN1\0TN2\0TN3\0CA1\0CA2\0CA3\0ING\0PAC\0SPS\0GFB\0"
	};
	return iface->lpVtbl->RegisterFileType(&fileInfoItemW) != NULL;
}

__declspec(dllexport) BOOL IMAGINEAPI ImaginePluginGetInfoA(IMAGINEPLUGININFOA *dest)
{
	static const IMAGINEPLUGININFOA pluginInfoA = {
		sizeof(pluginInfoA),
		registerProcA,
		VERSION_NUMBER,
		"FAIL Plugin",
		IMAGINEPLUGININTERFACE_VERSION
	};
	*dest = pluginInfoA;
	return TRUE;
}

__declspec(dllexport) BOOL IMAGINEAPI ImaginePluginGetInfoW(IMAGINEPLUGININFOW *dest)
{
	static const IMAGINEPLUGININFOW pluginInfoW = {
		sizeof(pluginInfoW),
		registerProcW,
		VERSION_NUMBER,
		L"FAIL Plugin",
		IMAGINEPLUGININTERFACE_VERSION
	};
	*dest = pluginInfoW;
	return TRUE;
}