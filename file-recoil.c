/*
 * file-recoil.c - RECOIL plugin for GIMP
 *
 * Copyright (C) 2021  Piotr Fusik
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

#include <string.h>
#include <libgimp/gimp.h>

#include "recoil-stdio.h"
#include "formats.h"

#define LOAD_PROC "file-recoil-load"

static gint32 load_image(const gchar *filename)
{
	RECOIL *recoil = RECOILStdio_New();
	if (!RECOILStdio_Load(recoil, filename)) {
		RECOIL_Delete(recoil);
		return -1;
	}
	gegl_init(NULL, NULL);
	int width = RECOIL_GetWidth(recoil);
	int height = RECOIL_GetHeight(recoil);
	gint32 image = gimp_image_new(width, height, GIMP_RGB);
	if (image != -1) {
		gimp_image_set_filename(image, filename);
		gint32 layer = gimp_layer_new(image, "Background", width, height, GIMP_RGB_IMAGE, 100, GIMP_NORMAL_MODE);
		gimp_image_insert_layer(image, layer, -1, 0);
		uint8_t *line = (uint8_t *) malloc(width * 3);
		if (line != NULL) {
			const int *pixels = RECOIL_GetPixels(recoil);
			GeglBuffer *buffer = gimp_drawable_get_buffer(layer);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int rgb = pixels[y * width + x];
					line[x * 3] = (uint8_t) (rgb >> 16);
					line[x * 3 + 1] = (uint8_t) (rgb >> 8);
					line[x * 3 + 2] = (uint8_t) rgb;
				}
				gegl_buffer_set(buffer, GEGL_RECTANGLE (0, y, width, 1), 0, NULL, line, GEGL_AUTO_ROWSTRIDE);
			}
			g_object_unref(buffer);
			free(line);
		}
		else {
			gimp_image_delete(image);
			image = -1;
		}
	}
	RECOIL_Delete(recoil);
	return image;
}

static void query(void)
{
	static const GimpParamDef load_args[] = {
		{ GIMP_PDB_INT32,  "run-mode",     "The run mode { RUN-INTERACTIVE (0), RUN-NONINTERACTIVE (1) }" },
		{ GIMP_PDB_STRING, "filename",     "The name of the file to load" },
		{ GIMP_PDB_STRING, "raw-filename", "The name entered"             }
	};
	static const GimpParamDef load_return_vals[] =
	{
		{ GIMP_PDB_IMAGE, "image", "Output image" }
	};
	gimp_install_procedure(LOAD_PROC,
		"Loads files using Retro Computer Image Library (RECOIL)",
		"Loads files using Retro Computer Image Library (RECOIL)",
		"Piotr Fusik <fox@scene.pl>",
		"Piotr Fusik <fox@scene.pl>",
		"2021",
		NULL,
		NULL,
		GIMP_PLUGIN,
		G_N_ELEMENTS(load_args),
		G_N_ELEMENTS(load_return_vals),
		load_args,
		load_return_vals);
	gimp_register_load_handler(LOAD_PROC, GIMP_RECOIL_EXTS, "");
}

static void run(const gchar *name, gint nparams, const GimpParam *param, gint *nreturn_vals, GimpParam **return_vals)
{
	static GimpParam values[2];
	*return_vals = values;
	*nreturn_vals = 1;
	values[0].type = GIMP_PDB_STATUS;
	if (strcmp(name, LOAD_PROC) == 0) {
		gint32 image = load_image(param[1].data.d_string);
		if (image != -1) {
			values[0].data.d_status = GIMP_PDB_SUCCESS;
			values[1].type = GIMP_PDB_IMAGE;
			values[1].data.d_image = image;
			*nreturn_vals = 2;
		}
		else
			values[0].data.d_status = GIMP_PDB_EXECUTION_ERROR;
	}
	else
		values[0].data.d_status = GIMP_PDB_CALLING_ERROR;
}

const GimpPlugInInfo PLUG_IN_INFO = {
	NULL,
	NULL,
	query,
	run
};

MAIN()
