// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class VbmStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b == (this.content[9] & 0xff)) {
			this.repeatValue = readByte();
			this.repeatCount = readByte();
			return this.repeatCount > 0;
		}
		if (b == (this.content[10] & 0xff)) {
			this.repeatValue = 0;
			this.repeatCount = readByte();
			return this.repeatCount > 0;
		}
		if (b == (this.content[11] & 0xff)) {
			this.repeatValue = 255;
			this.repeatCount = readByte();
			return this.repeatCount > 0;
		}
		if (b == (this.content[12] & 0xff)) {
			this.repeatValue = 0;
			this.repeatCount = 2;
			return true;
		}
		if (b == (this.content[13] & 0xff)) {
			this.repeatValue = 255;
			this.repeatCount = 2;
			return true;
		}
		this.repeatValue = b;
		this.repeatCount = 1;
		return true;
	}
}
