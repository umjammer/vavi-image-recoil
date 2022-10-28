// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class Bdp5Stream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b == (this.content[10] & 0xff) || b == (this.content[11] & 0xff)) {
			this.repeatCount = readByte();
			if (b == (this.content[11] & 0xff))
				this.repeatCount |= readByte() << 8;
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
