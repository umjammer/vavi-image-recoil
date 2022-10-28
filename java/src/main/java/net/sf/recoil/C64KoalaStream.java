// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class C64KoalaStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b == 254) {
			this.repeatValue = readByte();
			this.repeatCount = readByte();
			return this.repeatCount >= 0;
		}
		this.repeatValue = b;
		this.repeatCount = 1;
		return true;
	}
}
