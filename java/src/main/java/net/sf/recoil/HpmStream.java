// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class HpmStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b == 0) {
			this.repeatValue = readByte();
			b = readByte();
		}
		else
			this.repeatValue = -1;
		this.repeatCount = b;
		return b >= 0;
	}
}
