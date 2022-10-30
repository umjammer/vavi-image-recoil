// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class SpsStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b < 128) {
			this.repeatCount = b + 3;
			this.repeatValue = readByte();
		}
		else {
			this.repeatCount = b - 127;
			this.repeatValue = -1;
		}
		return true;
	}
}
