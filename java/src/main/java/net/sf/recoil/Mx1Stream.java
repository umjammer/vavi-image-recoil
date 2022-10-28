// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class Mx1Stream extends X68KPicStream
{
	Mx1Stream()
	{
		int d = 0;
		for (int e = 0; e < 256; e++) {
			if ((e >= '!' && e <= '~' && e != '"' && e != '\'' && e != ',' && e != '@' && e != '\\' && e != '`') || (e >= 161 && e <= 200))
				this.decodeTable[e] = (byte) d++;
			else
				this.decodeTable[e] = (byte) 128;
		}
	}
	private final byte[] decodeTable = new byte[256];

	final boolean findImage()
	{
		for (;;) {
			int lineOffset = this.contentOffset;
			for (;;) {
				int c = readByte();
				if (c < 0)
					return false;
				if (c == '\r' || c == '\n')
					break;
			}
			if (this.contentOffset - lineOffset >= 17 && RECOIL.isStringAt(this.content, lineOffset, "@@@ ") && RECOIL.isStringAt(this.content, this.contentOffset - 11, "lines) @@@")) {
				this.bits = 0;
				return true;
			}
		}
	}

	@Override int readBit()
	{
		if ((this.bits & 63) == 0) {
			int e = readNl3Char(true);
			if (e < 0)
				return -1;
			int d = this.decodeTable[e] & 0xff;
			if (d >= 128)
				return -1;
			this.bits = d << 1 | 1;
		}
		else
			this.bits <<= 1;
		return this.bits >> 7 & 1;
	}
}
