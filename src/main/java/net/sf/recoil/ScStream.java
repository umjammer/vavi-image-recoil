// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class ScStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b < 128) {
			this.repeatCount = b;
			this.repeatValue = readByte();
		}
		else {
			this.repeatCount = b - 128;
			this.repeatValue = -1;
		}
		return true;
	}
}
