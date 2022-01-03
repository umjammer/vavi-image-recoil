﻿/*
 * RecoilPaintDotNet.cs - Paint.NET file type plugin
 *
 * Copyright (C) 2013-2022  Piotr Fusik
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

using System;
using System.Reflection;
using System.Runtime.InteropServices;

using PaintDotNet;
using Recoil;

[assembly: AssemblyTitle("Paint.NET RECOIL plugin")]
//[assembly: AssemblyDescription("Decodes images in native formats of vintage computers")]
[assembly: AssemblyCompany("Piotr Fusik")]
[assembly: AssemblyProduct("RECOIL")]
[assembly: AssemblyCopyright("Copyright © 2013-2022")]
[assembly: AssemblyVersion(RECOIL.Version + ".0")]
[assembly: AssemblyFileVersion(RECOIL.Version + ".0")]

namespace Recoil.PaintDotNet
{
	// Paint.NET gives us a Stream without the filename, so I create a distinct FileType object for each extension.
	// This could be optimized by considering alias extensions.

	class RecoilFileType : FileType
	{
		readonly string Extension;

		public RecoilFileType(string ext, string name) : base(name, new FileTypeOptions { LoadExtensions = new string[] { ext } })
		{
			this.Extension = ext;
		}

		protected override Document OnLoad(System.IO.Stream input)
		{
			// Read.
			long longLength = input.Length;
			if (longLength > 0x7fffffc7) // max byte array length according to https://docs.microsoft.com/en-us/dotnet/api/system.array?view=net-6.0
				throw new Exception("File too long");
			int contentLength = (int) longLength;
			byte[] content = new byte[contentLength];
			contentLength = input.Read(content, 0, contentLength);

			// Decode.
			RECOIL recoil = new RECOIL();
			if (!recoil.Decode(this.Extension, content, contentLength))
				throw new Exception("Decoding error");
			int width = recoil.GetWidth();
			int height = recoil.GetHeight();
			int[] pixels = recoil.GetPixels();

			// Pass to Paint.NET.
			Document document = new Document(width, height);
			BitmapLayer layer = Layer.CreateBackgroundLayer(width, height);
			Surface surface = layer.Surface;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					surface[x, y] = ColorBgra.FromOpaqueInt32(pixels[y * width + x]);
			}
			float xDpi = recoil.GetXPixelsPerInch();
			if (xDpi != 0) {
				document.DpuUnit = MeasurementUnit.Inch;
				document.DpuX = xDpi;
				document.DpuY = recoil.GetYPixelsPerInch();
			}
			document.Layers.Add(layer);
			return document;
		}
	}
}
