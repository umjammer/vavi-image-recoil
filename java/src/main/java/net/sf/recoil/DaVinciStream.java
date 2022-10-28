// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class DaVinciStream extends PgcStream
{

	@Override int readValue()
	{
		if (this.contentOffset + 3 > this.contentLength)
			return -1;
		this.contentOffset += 3;
		return (this.content[this.contentOffset - 2] & 0xff) << 16 | (this.content[this.contentOffset - 1] & 0xff) << 8 | this.content[this.contentOffset - 3] & 0xff;
	}
}
