// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class SrStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		switch (b) {
		case -1:
			return false;
		case 0:
			this.repeatCount = readByte();
			if (this.repeatCount == 0)
				this.repeatCount = 256;
			this.repeatValue = readByte();
			return true;
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
		case 13:
		case 14:
		case 15:
			this.repeatCount = b;
			this.repeatValue = readByte();
			return true;
		default:
			this.repeatCount = 1;
			this.repeatValue = b;
			return true;
		}
	}
}
