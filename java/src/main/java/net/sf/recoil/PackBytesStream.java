// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PackBytesStream extends Stream
{
	private int count = 1;
	private int pattern = 0;

	final int readUnpacked()
	{
		if (--this.count == 0) {
			if (this.contentOffset >= this.contentLength)
				return -1;
			int b = this.content[this.contentOffset++] & 0xff;
			this.count = (b & 63) + 1;
			if (b >= 128)
				this.count <<= 2;
			this.pattern = READ_UNPACKED_PATTERNS[b >> 6] & 0xff;
		}
		else if ((this.count & (this.pattern - 1)) == 0)
			this.contentOffset -= this.pattern;
		return readByte();
	}

	private static final byte[] READ_UNPACKED_PATTERNS = { 0, 1, 4, 1 };
}
