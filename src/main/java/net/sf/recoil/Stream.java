// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

/**
 * Read-only stream backed by a byte array.
 */
class Stream
{
	byte[] content;
	int contentOffset;
	int contentLength;

	/**
	 * Returns the next byte or -1 on EOF.
	 */
	final int readByte()
	{
		if (this.contentOffset >= this.contentLength)
			return -1;
		return this.content[this.contentOffset++] & 0xff;
	}

	/**
	 * Advances the stream until after the first byte with the given value.
	 * Returns <code>false</code> on EOF.
	 */
	final boolean skipUntilByte(int expected)
	{
		for (;;) {
			int b = readByte();
			if (b < 0)
				return false;
			if (b == expected)
				return true;
		}
	}

	final boolean expect(String s)
	{
		for (int _i = 0; _i < s.length(); _i++) {
			int c = s.charAt(_i);
			if (readByte() != c)
				return false;
		}
		return true;
	}

	/**
	 * Reads <code>count</code> bytes to <code>dest</code> starting at <code>destOffset</code>.
	 * Returns <code>true</code> on success, <code>false</code> if not enough data.
	 */
	protected final boolean readBytes(byte[] dest, int destOffset, int count)
	{
		int nextOffset = this.contentOffset + count;
		if (nextOffset > this.contentLength)
			return false;
		System.arraycopy(this.content, this.contentOffset, dest, destOffset, count);
		this.contentOffset = nextOffset;
		return true;
	}

	/**
	 * Reads a hexadecimal ASCII digit and returns its value.
	 * If there's no digit at the current stream position,
	 * leaves the position intact and returns -1.
	 */
	private int readHexDigit()
	{
		if (this.contentOffset >= this.contentLength)
			return -1;
		int c = this.content[this.contentOffset++] & 0xff;
		if (c >= '0' && c <= '9')
			return c - '0';
		if (c >= 'A' && c <= 'F')
			return c - 55;
		if (c >= 'a' && c <= 'f')
			return c - 87;
		this.contentOffset--;
		return -1;
	}

	final int readHexByte()
	{
		int hi = readHexDigit();
		if (hi < 0)
			return -1;
		int lo = readHexDigit();
		if (lo < 0)
			return -1;
		return hi << 4 | lo;
	}

	/**
	 * Reads an integer in range <code>0</code>..<code>maxValue</code>
	 * in base <code>b</code>.
	 * Returns -1 on error.
	 */
	final int parseInt(int b, int maxValue)
	{
		int r = readHexDigit();
		if (r < 0 || r >= b)
			return -1;
		do {
			int d = readHexDigit();
			if (d < 0)
				return r;
			if (d >= b)
				return -1;
			r = r * b + d;
		}
		while (r <= maxValue);
		return -1;
	}

	/**
	 * Reads a decimal integer in range 0-32000
	 * stored as ASCII digits followed by CRLF.
	 * Returns -1 on error.
	 */
	final int parseDaliInt()
	{
		int r = parseInt(10, 32000);
		if (r < 0 || readByte() != '\r' || readByte() != '\n')
			return -1;
		return r;
	}
}
