// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class VplStream extends Stream
{
	private final int[] palette = new int[128];

	private int skipWhitespace()
	{
		while (this.contentOffset < this.contentLength) {
			int b = this.content[this.contentOffset] & 0xff;
			switch (b) {
			case ' ':
			case '\t':
			case '\r':
				this.contentOffset++;
				break;
			default:
				return b;
			}
		}
		return -1;
	}

	final int decode()
	{
		int colors = 0;
		for (;;) {
			switch (skipWhitespace()) {
			case -1:
				return colors;
			case '#':
			case '\n':
				skipUntilByte('\n');
				break;
			default:
				if (colors >= 128)
					return -1;
				int r = parseInt(16, 255);
				if (r < 0)
					return -1;
				skipWhitespace();
				int g = parseInt(16, 255);
				if (g < 0)
					return -1;
				skipWhitespace();
				int b = parseInt(16, 255);
				if (b < 0)
					return -1;
				skipWhitespace();
				if (parseInt(16, 15) < 0)
					return -1;
				this.palette[colors++] = r << 16 | g << 8 | b;
				switch (skipWhitespace()) {
				case -1:
					return colors;
				case '\n':
					this.contentOffset++;
					break;
				default:
					return -1;
				}
				break;
			}
		}
	}

	final void copyTo(int[] palette, int colors)
	{
		System.arraycopy(this.palette, 0, palette, 0, colors);
	}
}
