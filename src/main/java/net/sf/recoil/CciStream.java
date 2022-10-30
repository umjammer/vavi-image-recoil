// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class CciStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b < 128) {
			this.repeatCount = b + 1;
			this.repeatValue = -1;
		}
		else {
			this.repeatCount = b - 127;
			this.repeatValue = readByte();
		}
		return true;
	}

	final boolean unpackGr15(byte[] unpacked, int unpackedOffset)
	{
		this.contentOffset += 4;
		this.repeatCount = 0;
		for (int x = 0; x < 40; x++) {
			if (!unpack(unpacked, unpackedOffset + x, 80, 7680))
				return false;
		}
		return true;
	}
}
