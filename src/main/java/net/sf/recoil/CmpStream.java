// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class CmpStream extends RleStream
{
	int escape;

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b == this.escape) {
			this.repeatCount = readByte() + 1;
			b = readByte();
		}
		else
			this.repeatCount = 1;
		this.repeatValue = b;
		return b >= 0;
	}
}
