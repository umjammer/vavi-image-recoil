// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class BldStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		this.repeatValue = b;
		if (b == 0 || b == 255) {
			b = readByte();
			if (b < 0)
				return false;
			this.repeatCount = b + 1;
		}
		else
			this.repeatCount = 1;
		return true;
	}
}
