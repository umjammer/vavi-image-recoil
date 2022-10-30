// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

/**
 * Readable in-memory stream of bits, most-significant bit first.
 */
class BitStream extends Stream
{
	protected int bits = 0;

	/**
	 * Reads one bit (0 or 1).
	 * Returns -1 on end of stream.
	 */
	int readBit()
	{
		if ((this.bits & 127) == 0) {
			if (this.contentOffset >= this.contentLength)
				return -1;
			this.bits = (this.content[this.contentOffset++] & 0xff) << 1 | 1;
		}
		else
			this.bits <<= 1;
		return this.bits >> 8 & 1;
	}

	/**
	 * Reads the requested number of bits and returns them
	 * as an unsigned integer with the first bit read as the most significant.
	 * Returns -1 on end of stream.
	 */
	final int readBits(int count)
	{
		int result = 0;
		while (--count >= 0) {
			int bit = readBit();
			if (bit < 0)
				return -1;
			result = result << 1 | bit;
		}
		return result;
	}

	protected final int readNl3Char(boolean skipSpace)
	{
		int e;
		do
			e = readByte();
		while (e == '\r' || e == '\n' || (skipSpace && e == ' '));
		if (e != 239)
			return e;
		if (this.contentOffset + 1 >= this.contentLength)
			return -1;
		switch (this.content[this.contentOffset++]) {
		case -67:
			e = this.content[this.contentOffset++] & 0xff;
			if (e >= 160 && e <= 191)
				return e;
			break;
		case -66:
			e = this.content[this.contentOffset++] & 0xff;
			if (e >= 128 && e <= 159)
				return e + 64;
			break;
		default:
			break;
		}
		return -1;
	}
}
