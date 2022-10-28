// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PgcStream extends RleStream
{

	protected int readCommandByte()
	{
		return readByte();
	}

	protected @Override boolean readCommand()
	{
		int b = readCommandByte();
		if (b < 0)
			return false;
		if (b < 128) {
			this.repeatCount = b;
			this.repeatValue = -1;
		}
		else {
			this.repeatCount = b - 128;
			this.repeatValue = readValue();
		}
		return true;
	}
}
