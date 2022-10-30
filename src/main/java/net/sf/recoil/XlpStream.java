// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class XlpStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		boolean rle;
		if (b < 128)
			rle = false;
		else {
			b -= 128;
			rle = true;
		}
		this.repeatCount = b;
		if (b >= 64) {
			b = readByte();
			if (b < 0)
				return false;
			this.repeatCount = (this.repeatCount - 64) << 8 | b;
		}
		this.repeatValue = rle ? readByte() : -1;
		return true;
	}
}
