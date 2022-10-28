// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class VhiStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int c;
		switch (readByte()) {
		case 0:
			c = readByte();
			this.repeatValue = -1;
			break;
		case 1:
			c = readByte();
			this.repeatValue = readByte();
			break;
		default:
			return false;
		}
		this.repeatCount = c == 0 ? 256 : c;
		return true;
	}
}
