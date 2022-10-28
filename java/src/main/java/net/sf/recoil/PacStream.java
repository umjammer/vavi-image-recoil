// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PacStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b == (this.content[4] & 0xff)) {
			b = readByte();
			if (b < 0)
				return false;
			this.repeatCount = b + 1;
			this.repeatValue = this.content[5] & 0xff;
		}
		else if (b == (this.content[6] & 0xff)) {
			this.repeatValue = readByte();
			if (this.repeatValue < 0)
				return false;
			b = readByte();
			if (b < 0)
				return false;
			this.repeatCount = b + 1;
		}
		else {
			this.repeatCount = 1;
			this.repeatValue = b;
		}
		return true;
	}
}
