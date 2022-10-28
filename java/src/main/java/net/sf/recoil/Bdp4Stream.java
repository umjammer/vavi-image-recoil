// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class Bdp4Stream extends RleStream
{

	protected @Override boolean readCommand()
	{
		switch (readByte()) {
		case 255:
			this.repeatCount = readByte();
			if (this.repeatCount == 0)
				this.repeatCount = 256;
			this.repeatValue = readByte();
			return this.repeatValue >= 0;
		case 254:
			int lo = readByte();
			int hi = readByte();
			if (hi < 0)
				return false;
			this.repeatCount = lo | hi << 8;
			this.repeatValue = -1;
			return true;
		default:
			return false;
		}
	}
}
