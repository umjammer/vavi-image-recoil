// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class Tre1Stream extends RleStream
{
	private int lastRgb = -1;

	protected @Override boolean readCommand()
	{
		this.repeatCount = readByte();
		if (this.repeatCount <= 0)
			return false;
		if (this.repeatCount == 255) {
			if (this.contentOffset + 1 >= this.contentLength)
				return false;
			this.repeatCount = 255 + ((this.content[this.contentOffset] & 0xff) << 8) + (this.content[this.contentOffset + 1] & 0xff);
			this.contentOffset += 2;
		}
		this.repeatValue = this.lastRgb;
		this.lastRgb = -1;
		return true;
	}

	@Override int readValue()
	{
		if (this.contentOffset + 1 >= this.contentLength)
			return -1;
		this.lastRgb = RECOIL.getFalconTrueColor(this.content, this.contentOffset);
		this.contentOffset += 2;
		return this.lastRgb;
	}
}
