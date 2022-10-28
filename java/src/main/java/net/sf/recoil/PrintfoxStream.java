// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PrintfoxStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b == 155) {
			this.repeatCount = readByte();
			if (this.content[0] != 80)
				this.repeatCount += readByte() << 8;
			else if (this.repeatCount == 0)
				this.repeatCount = 256;
			b = readByte();
		}
		else
			this.repeatCount = 1;
		this.repeatValue = b;
		return b >= 0;
	}
}
