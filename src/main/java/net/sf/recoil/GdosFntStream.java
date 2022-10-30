// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class GdosFntStream extends Stream
{
	boolean bigEndian;
	int bitmapWidth;
	int width;
	int leftX;
	int rightX;
	int nextX;

	/**
	 * Reads a 16-bit unsigned integer.
	 * Returns -1 on EOF.
	 */
	final int readWord()
	{
		if (this.contentOffset + 1 >= this.contentLength)
			return -1;
		int first = this.content[this.contentOffset] & 0xff;
		int second = this.content[this.contentOffset + 1] & 0xff;
		this.contentOffset += 2;
		return this.bigEndian ? first << 8 | second : first | second << 8;
	}

	/**
	 * Reads a 32-bit integer assuming there's enough data.
	 */
	final int readInt()
	{
		int value = this.bigEndian ? RECOIL.get32BigEndian(this.content, this.contentOffset) : RECOIL.get32LittleEndian(this.content, this.contentOffset);
		this.contentOffset += 4;
		return value;
	}

	final boolean fitRow()
	{
		this.leftX = this.rightX;
		do {
			this.rightX = this.nextX;
			this.nextX = readWord();
			if (this.nextX < this.rightX)
				return this.nextX < 0;
			if (this.nextX > this.bitmapWidth) {
				this.nextX = -1;
				return true;
			}
		}
		while (this.nextX - this.leftX <= this.width);
		return true;
	}
}
