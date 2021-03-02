/*
 * by-platform.c - organize images into platform directories
 *
 * Copyright (C) 2017-2021  Piotr Fusik
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
#include <windows.h>

#include "recoil-win32.h"

int main(int argc, char **argv)
{
	WIN32_FIND_DATA find;
	HANDLE h = FindFirstFile("../../examples/*", &find);
	if (h == INVALID_HANDLE_VALUE)
		return 1;
	RECOIL *recoil = RECOIL_New();
	do {
		const char *filename = find.cFileName;
		char path[MAX_PATH];
		snprintf(path, sizeof(path), "../../examples/%s", filename);
		//puts(path);
		if (RECOILWin32_LoadA(recoil, path)) {
			const char *platform = RECOIL_GetPlatform(recoil);
			char target[MAX_PATH];
			snprintf(target, sizeof(target), "../../by-platform/%s", platform);
			CreateDirectory(target, NULL);
			snprintf(target, sizeof(target), "../../by-platform/%s/%s", platform, filename);
			CopyFile(path, target, FALSE);
		}
		else {
			fprintf(stderr, "%s: cannot decode\n", path);
		}
	} while (FindNextFile(h, &find));
	return 0;
}
