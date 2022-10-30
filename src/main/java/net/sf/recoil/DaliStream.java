// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class DaliStream extends Stream
{

	final boolean decode(int countLength, RECOIL recoil, int paletteOffset, int mode)
	{
		final byte[] unpacked = new byte[32000];
		int valueOffset = this.contentOffset + countLength - 4;
		int count = 1;
		for (int x = 0; x < 160; x += 4) {
			for (int unpackedOffset = x; unpackedOffset < 32000; unpackedOffset += 160) {
				if (--count <= 0) {
					if (valueOffset + 7 >= this.contentLength)
						return false;
					count = this.content[this.contentOffset++] & 0xff;
					if (count == 0)
						return false;
					valueOffset += 4;
				}
				System.arraycopy(this.content, valueOffset, unpacked, unpackedOffset, 4);
			}
		}
		return recoil.decodeSt(unpacked, 0, this.content, paletteOffset, mode, 0);
	}
}
