// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class MspStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b == 0) {
			this.repeatCount = readByte();
			this.repeatValue = readByte();
			return this.repeatValue >= 0;
		}
		this.repeatCount = b;
		this.repeatValue = -1;
		return true;
	}
}
