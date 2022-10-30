// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class CpiStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (this.contentOffset + 1 < this.contentLength && (this.content[this.contentOffset] & 0xff) == b) {
			this.contentOffset++;
			this.repeatCount = 1 + (this.content[this.contentOffset++] & 0xff);
		}
		else
			this.repeatCount = 1;
		this.repeatValue = b;
		return true;
	}
}
