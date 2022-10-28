// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class SpcStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b < 128) {
			this.repeatCount = b + 1;
			this.repeatValue = -1;
		}
		else {
			this.repeatCount = 258 - b;
			this.repeatValue = readByte();
		}
		return true;
	}
}
