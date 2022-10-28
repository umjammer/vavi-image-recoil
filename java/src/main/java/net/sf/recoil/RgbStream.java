// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class RgbStream extends RleStream
{

	@Override int readValue()
	{
		return readBits(12);
	}

	protected @Override boolean readCommand()
	{
		int b = readBits(4);
		if (b < 0)
			return false;
		boolean rle;
		if (b < 8)
			rle = true;
		else {
			b -= 8;
			rle = false;
		}
		if (b == 0) {
			b = readBits(4);
			if (b < 0)
				return false;
			b += 7;
		}
		if (rle) {
			this.repeatValue = readValue();
			b++;
		}
		else
			this.repeatValue = -1;
		this.repeatCount = b;
		return true;
	}
}
