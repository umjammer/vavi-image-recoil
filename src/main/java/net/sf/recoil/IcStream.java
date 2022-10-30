// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class IcStream extends RleStream
{

	private boolean readCount()
	{
		this.repeatCount = 257;
		while (readByte() == 1)
			this.repeatCount += 256;
		int b = readByte();
		if (b < 0)
			return false;
		this.repeatCount += b;
		return true;
	}

	protected @Override boolean readCommand()
	{
		int b = readByte();
		int escape = this.content[66] & 0xff;
		if (b != escape) {
			this.repeatCount = 1;
			this.repeatValue = b;
			return true;
		}
		b = readByte();
		if (b == escape) {
			this.repeatCount = 1;
			this.repeatValue = b;
			return true;
		}
		switch (b) {
		case -1:
			return false;
		case 0:
			b = readByte();
			if (b < 0)
				return false;
			this.repeatCount = b + 1;
			break;
		case 1:
			if (!readCount())
				return false;
			break;
		case 2:
			b = readByte();
			switch (b) {
			case -1:
				return false;
			case 0:
				this.repeatCount = 32000;
				break;
			case 1:
				if (!readCount())
					return false;
				break;
			case 2:
				while (readByte() > 0) {
				}
				this.repeatCount = 0;
				break;
			default:
				this.repeatCount = b + 1;
				break;
			}
			this.repeatValue = 0;
			return true;
		default:
			this.repeatCount = b + 1;
			break;
		}
		this.repeatValue = readByte();
		return true;
	}
}
