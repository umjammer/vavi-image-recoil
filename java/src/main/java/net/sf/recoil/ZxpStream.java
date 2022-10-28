// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class ZxpStream extends Stream
{

	final int readChar()
	{
		int c = readByte();
		if (c == '\r' && this.contentOffset < this.contentLength && this.content[this.contentOffset] == 10) {
			this.contentOffset++;
			return '\n';
		}
		return c;
	}

	final boolean isEof()
	{
		return this.contentOffset >= this.contentLength;
	}
}
