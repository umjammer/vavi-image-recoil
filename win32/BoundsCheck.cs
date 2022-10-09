// BoundsCheck.cs - check out-of-bounds accesses for truncated files
//
// Copyright (C) 2015-2022  Piotr Fusik
//
// This file is part of RECOIL (Retro Computer Image Library),
// see http://recoil.sourceforge.net
//
// RECOIL is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published
// by the Free Software Foundation; either version 2 of the License,
// or (at your option) any later version.
//
// RECOIL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with RECOIL; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

using System;
using System.IO;
using System.Threading.Tasks;

using Recoil;

class FileRECOIL : RECOIL
{
	protected override int ReadFile(string filename, byte[] content, int contentLength)
	{
		try {
			using (FileStream s = File.OpenRead(filename)) {
				return s.Read(content, 0, contentLength);
			}
		}
		catch (FileNotFoundException) {
			return -1;
		}
	}
}

public class BoundsChecker
{
	public static void Main(string[] args)
	{
		if (args.Length != 1)
			throw new ArgumentException("Usage: BoundsCheck DIRECTORY");
		Parallel.ForEach(Directory.GetFiles(args[0]), filename => {
			RECOIL recoil = new FileRECOIL();
			Console.WriteLine(filename);
			byte[] content = File.ReadAllBytes(filename);
			int length = content.Length;
			if (recoil.Decode(filename, content, length)) {
				if (length > 6000)
					length = 6000;
				while (length > 0) {
					byte[] smaller = new byte[--length];
					Array.Copy(content, smaller, length);
					content = smaller;
					if (content.Length % 2000 == 0)
						Console.WriteLine(length);
					recoil.Decode(filename, content, length);
				}
			}
			else {
				Console.WriteLine("Cannot decode");
			}
		});
	}
}
