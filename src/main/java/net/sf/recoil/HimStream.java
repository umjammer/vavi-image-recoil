// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class HimStream extends RleStream
{

	@Override int readValue()
	{
		if (this.contentOffset < 18)
			return -1;
		return this.content[this.contentOffset--] & 0xff;
	}

	protected @Override boolean readCommand()
	{
		int b = readValue();
		switch (b) {
		case -1:
			return false;
		case 0:
			this.repeatCount = readValue();
			if (this.repeatCount <= 0)
				return false;
			this.repeatValue = readValue();
			return true;
		default:
			this.repeatCount = b - 1;
			this.repeatValue = -1;
			return true;
		}
	}
}
