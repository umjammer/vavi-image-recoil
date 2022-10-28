// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class Nl3Stream extends RleStream
{

	@Override int readValue()
	{
		int e = readNl3Char(false);
		if (e < 32)
			return -1;
		if (e < 127)
			return e - 32;
		if (e < 160)
			return -1;
		if (e < 224)
			return e - 65;
		if (e == 253)
			return 159;
		if (e == 254)
			return 160;
		return -1;
	}

	protected @Override boolean readCommand()
	{
		int b = readValue();
		if (b < 0 || b > 127)
			return false;
		this.repeatValue = b & 63;
		if (b < 64)
			this.repeatCount = 1;
		else {
			b = readValue();
			if (b < 0)
				return false;
			this.repeatCount = b + 2;
		}
		return true;
	}
}
