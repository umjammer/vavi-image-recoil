#include <windows.h>

#include <stdio.h>
#include <stdlib.h>

#include "fail.h"

#define API __stdcall
#define DLL_EXPORT __declspec(dllexport)

#define GFP_RGB	0
#define GFP_BGR	1

#define GFP_READ 0x0001
#define GFP_WRITE 0x0002

typedef struct {
	unsigned char red[256];
	unsigned char green[256];
	unsigned char blue[256];
} GFP_COLORMAP;

typedef struct {
	byte image[FAIL_IMAGE_MAX];
	int image_len;
	int width;
	int height;
	int colors;
	byte pixels[FAIL_PIXELS_MAX];
//	byte palette[FAIL_PALETTE_MAX];
	int bytes_per_line;
} FailData;

#ifdef __cplusplus
extern "C"
{
#endif

DLL_EXPORT BOOL API gfpGetPluginInfo(DWORD version, LPSTR label, INT label_max_size, LPSTR extension, INT extension_max_size, INT *support);
DLL_EXPORT void * API gfpLoadPictureInit(LPCSTR filename);
DLL_EXPORT BOOL API gfpLoadPictureGetInfo(void * ptr, INT * pictype, INT * width, INT * height, INT * dpi, INT * bits_per_pixel, INT * bytes_per_line, BOOL * has_colormap, LPSTR label, INT label_max_size);
DLL_EXPORT BOOL API gfpLoadPictureGetLine(void * ptr, INT line, unsigned char * buffer);
DLL_EXPORT BOOL API gfpLoadPictureGetColormap(void * ptr, GFP_COLORMAP * cmap);
DLL_EXPORT void API gfpLoadPictureExit(void * ptr);
DLL_EXPORT BOOL API gfpSavePictureIsSupported(INT width, INT height, INT bits_per_pixel, BOOL has_colormap);
DLL_EXPORT void * API gfpSavePictureInit(LPCSTR filename, INT width, INT height, INT bits_per_pixel, INT dpi, INT * picture_type, LPSTR label, INT label_max_size);
DLL_EXPORT BOOL API gfpSavePicturePutLine(void * ptr, INT line, const unsigned char * buffer);
DLL_EXPORT void API gfpSavePictureExit(void * ptr);

#ifdef __cplusplus
}
#endif


DLL_EXPORT BOOL API gfpGetPluginInfo(DWORD version, LPSTR label, INT label_max_size, LPSTR extension, INT extension_max_size, INT *support)
{
	if (version != 0x0002)
		return FALSE;

	strncpy(label, "First Atari Image Library", label_max_size);
	strncpy(extension, "rip;gr8;mic;hip;tip;int;inp;apc;ap3;gr9;pic;cpr;cin;cci;hr;plm;ilc", extension_max_size);

	*support = GFP_READ;

	return TRUE;
}

DLL_EXPORT void * API gfpLoadPictureInit(LPCSTR filename)
{
	FailData *fail;
	FILE *fp;
	
	fp = fopen(filename, "rb");
	if (fp == NULL)
		return NULL;
	
	fail = malloc(sizeof(FailData));
	if (fail != NULL) {
		fail->image_len = fread(fail->image, 1, sizeof(fail->image), fp);
		if (!FAIL_DecodeImage(filename, fail->image, fail->image_len, NULL,
			&fail->width, &fail->height, &fail->colors,
			fail->pixels, NULL /* fail->palette */)) {
			fclose(fp);
			free(fail);
			return NULL;
		}
	}

	fclose(fp);
	return fail;
}

DLL_EXPORT BOOL API gfpLoadPictureGetInfo(
	void *ptr, INT *pictype, INT *width, INT *height,
	INT *dpi, INT *bits_per_pixel, INT *bytes_per_line,
	BOOL *has_colormap, LPSTR label, INT label_max_size)
{
	FailData *fail = (FailData*)ptr;

	*pictype = GFP_RGB;
	*width = fail->width;
	*height = fail->height;
	*dpi = 68;
	*bits_per_pixel = 24; // fail->colors <= 256 ? 8 : 24;
	fail->bytes_per_line = *bytes_per_line = 
		*bits_per_pixel/8 * *width;
	*has_colormap = FALSE; // fail->colors <= 256 ? TRUE : FALSE;

	strncpy(label, "FAIL", label_max_size);

	return TRUE;
}

DLL_EXPORT BOOL API gfpLoadPictureGetLine(void *ptr, INT line, unsigned char *buffer)
{
	FailData *fail = (FailData*)ptr;

	memcpy(buffer, fail->pixels + line * fail->bytes_per_line, fail->bytes_per_line);
	return TRUE;
}

DLL_EXPORT BOOL API gfpLoadPictureGetColormap(void *ptr, GFP_COLORMAP *cmap)
{
/*	FailData *fail = (FailData*)ptr;

	if (fail->colors <= 256) {
		int i;
		for (i = 0; i < 256; i++) {
			cmap->red[i] = fail->palette[i * 3];
			cmap->green[i] = fail->palette[i * 3 + 1];
			cmap->blue[i] = fail->palette[i * 3 + 2];
		}
		return TRUE;
	}
	else */
		return FALSE;
}

DLL_EXPORT void API gfpLoadPictureExit(void *ptr)
{
	free(ptr);
}

DLL_EXPORT BOOL API gfpSavePictureIsSupported(INT width, INT height, INT bits_per_pixel, BOOL has_colormap)
{
	return FALSE;
}

DLL_EXPORT void * API gfpSavePictureInit(LPCSTR filename, INT width, INT height, INT bits_per_pixel, INT dpi, INT * picture_type, LPSTR label, INT label_max_size)
{
	return NULL;
}

DLL_EXPORT BOOL API gfpSavePicturePutLine(void *ptr, INT line, const unsigned char *buffer)
{
	return FALSE;
}

DLL_EXPORT void API gfpSavePictureExit(void *ptr)
{
}

BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved)
{
	return TRUE;
}
