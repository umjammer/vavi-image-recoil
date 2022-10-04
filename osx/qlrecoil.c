/*
 * qlrecoil.c - RECOIL plugin for macOS QuickLook
 *
 * Copyright (C) 2014-2022  Petri Pyy and Piotr Fusik
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

#include <stdio.h>
#include <libkern/OSAtomic.h>
#include <CoreFoundation/CFPlugInCOM.h>
#include <QuickLook/QuickLook.h>

#include "recoil-stdio.h"

static CGImageRef CreateImage(CFURLRef url)
{
	char filename[FILENAME_MAX];
	if (!CFURLGetFileSystemRepresentation(url, false, (UInt8 *) filename, sizeof(filename)))
		return NULL;

	CFDataRef data;
	SInt32 errorCode;
	if (!CFURLCreateDataAndPropertiesFromResource(NULL, url, &data, NULL, NULL, &errorCode))
		return NULL;

	RECOIL *recoil = RECOILStdio_New();
	if (recoil == NULL) {
		CFRelease(data);
		return NULL;
	}
	if (!RECOIL_Decode(recoil, filename, CFDataGetBytePtr(data), CFDataGetLength(data))) {
		RECOIL_Delete(recoil);
		CFRelease(data);
		return NULL;
	}
	CFRelease(data);

	CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
	if (colorSpace == NULL) {
		RECOIL_Delete(recoil);
		return NULL;
	}
	int width = RECOIL_GetWidth(recoil);
	int height = RECOIL_GetHeight(recoil);
	CGContextRef context = CGBitmapContextCreate((void *) RECOIL_GetPixels(recoil), width, height, 8, width << 2, colorSpace, kCGBitmapByteOrder32Host | kCGImageAlphaNoneSkipFirst);
	CFRelease(colorSpace);
	if (context == NULL) {
		RECOIL_Delete(recoil);
		return NULL;
	}
	CGImageRef image = CGBitmapContextCreateImage(context);
	CFRelease(context);
	RECOIL_Delete(recoil);
	return image;
}

typedef struct
{
	const QLGeneratorInterfaceStruct *vtbl;
	int refCount;
	CFUUIDRef factoryID;
} Plugin;

static ULONG AddRef(void *thisInstance)
{
	Plugin *plugin = (Plugin *) thisInstance;
	return OSAtomicIncrement32(&plugin->refCount);
}

static HRESULT QueryInterface(void *thisInstance, REFIID iid, LPVOID *ppv)
{
	CFUUIDRef interfaceID = CFUUIDCreateFromUUIDBytes(kCFAllocatorDefault, iid);
	if (CFEqual(interfaceID, kQLGeneratorCallbacksInterfaceID)) {
		CFRelease(interfaceID);
		AddRef(thisInstance);
		*ppv = thisInstance;
		return S_OK;
	}
	CFRelease(interfaceID);
	*ppv = NULL;
	return E_NOINTERFACE;
}

static void Dealloc(Plugin *plugin)
{
	CFUUIDRef factoryID = plugin->factoryID;
	CFPlugInRemoveInstanceForFactory(factoryID);
	CFRelease(factoryID);
	free(plugin);
}

static ULONG Release(void *thisInstance)
{
	Plugin *plugin = (Plugin *) thisInstance;
	ULONG r = OSAtomicDecrement32(&plugin->refCount);
	if (r == 0)
		Dealloc(plugin);
	return r;
}

static OSStatus GenerateThumbnailForURL(void *thisInstance, QLThumbnailRequestRef thumbnail, CFURLRef url, CFStringRef contentTypeUTI, CFDictionaryRef options, CGSize maxSize)
{
	CGImageRef image = CreateImage(url);
	if (image != NULL) {
		QLThumbnailRequestSetImage(thumbnail, image, NULL);
		CFRelease(image);
	}
	return noErr;
}

static void CancelThumbnailGeneration(void* thisInstance, QLThumbnailRequestRef thumbnail)
{
}

static OSStatus GeneratePreviewForURL(void *thisInstance, QLPreviewRequestRef preview, CFURLRef url, CFStringRef contentTypeUTI, CFDictionaryRef options)
{
	CGImageRef image = CreateImage(url);
	if (image == NULL)
		return noErr;

	size_t width = CGImageGetWidth(image);
	size_t height = CGImageGetHeight(image);
	CGContextRef context = QLPreviewRequestCreateContext(preview, CGSizeMake(width, height), true, NULL);
	if (context == NULL) {
		CGImageRelease(image);
		return noErr;
	}
	CGContextDrawImage(context, CGRectMake(0, 0, width, height), image);
	CGImageRelease(image);
	QLPreviewRequestFlushContext(preview, context);
	CFRelease(context);
	return noErr;
}

static void CancelPreviewGeneration(void *thisInstance, QLPreviewRequestRef preview)
{
}

void *QuickLookGeneratorPluginFactory(CFAllocatorRef allocator, CFUUIDRef typeID)
{
	if (CFEqual(typeID, kQLGeneratorTypeID)) {
		Plugin *plugin = (Plugin *) malloc(sizeof(Plugin));
		if (plugin == NULL)
			return NULL;
		static const QLGeneratorInterfaceStruct pluginVtbl = {
			NULL,
			QueryInterface,
			AddRef,
			Release,
			GenerateThumbnailForURL,
			CancelThumbnailGeneration,
			GeneratePreviewForURL,
			CancelPreviewGeneration
		};
		plugin->vtbl = &pluginVtbl;
		plugin->refCount = 1;
		plugin->factoryID = CFUUIDCreateFromString(kCFAllocatorDefault, CFSTR("B4EBAF99-E681-49A5-91CA-78459C948EEA"));
		CFPlugInAddInstanceForFactory(plugin->factoryID);
		return plugin;
	}
	return NULL;
}
