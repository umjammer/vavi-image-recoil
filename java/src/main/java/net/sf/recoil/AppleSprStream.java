// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class AppleSprStream extends Stream
{

	private static final int WIDTH = 320;

	private static final int HEIGHT = 200;

	/**
	 * Reads a decimal, hexadecimal (with <code>$</code> prefix)
	 * or binary (with <code>%</code> prefix) integer.
	 * Skips leading whitespace.
	 * Returns -1 on error.
	 */
	final int readSprInt()
	{
		while (this.contentOffset < this.contentLength) {
			int c = this.content[this.contentOffset] & 0xff;
			switch (c) {
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				this.contentOffset++;
				break;
			case '$':
				this.contentOffset++;
				return parseInt(16, 319);
			case '%':
				this.contentOffset++;
				return parseInt(2, 319);
			default:
				return parseInt(10, 319);
			}
		}
		return -1;
	}
}
