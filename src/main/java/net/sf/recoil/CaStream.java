// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class CaStream extends RleStream
{
	private int escape;
	private int defaultValue;

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b != this.escape) {
			this.repeatCount = 1;
			this.repeatValue = b;
			return true;
		}
		int c = readByte();
		if (c < 0)
			return false;
		if (c == this.escape) {
			this.repeatCount = 1;
			this.repeatValue = c;
			return true;
		}
		b = readByte();
		if (b < 0)
			return false;
		switch (c) {
		case 0:
			this.repeatCount = b + 1;
			this.repeatValue = readByte();
			break;
		case 1:
			c = readByte();
			if (c < 0)
				return false;
			this.repeatCount = (b << 8) + c + 1;
			this.repeatValue = readByte();
			break;
		case 2:
			if (b == 0)
				this.repeatCount = 32000;
			else {
				c = readByte();
				if (c < 0)
					return false;
				this.repeatCount = (b << 8) + c + 1;
			}
			this.repeatValue = this.defaultValue;
			break;
		default:
			this.repeatCount = c + 1;
			this.repeatValue = b;
			break;
		}
		return true;
	}

	final boolean unpackCa(byte[] unpacked, int unpackedOffset)
	{
		if (this.contentOffset > this.contentLength - 4)
			return false;
		this.escape = this.content[this.contentOffset] & 0xff;
		this.defaultValue = this.content[this.contentOffset + 1] & 0xff;
		int unpackedStep = (this.content[this.contentOffset + 2] & 0xff) << 8 | this.content[this.contentOffset + 3] & 0xff;
		if (unpackedStep >= 32000)
			return false;
		this.repeatCount = 0;
		if (unpackedStep == 0) {
			this.repeatCount = 32000;
			this.repeatValue = this.defaultValue;
			unpackedStep = 1;
		}
		this.contentOffset += 4;
		return unpackColumns(unpacked, unpackedOffset, unpackedStep, unpackedOffset + 32000);
	}

	static boolean unpackDel(byte[] content, int contentLength, byte[] unpacked, int blocks)
	{
		final CaStream rle = new CaStream();
		rle.content = content;
		rle.contentOffset = blocks << 2;
		if (rle.contentOffset >= contentLength)
			return false;
		for (int block = 0; block < blocks; block++) {
			rle.contentLength = rle.contentOffset + RECOIL.get32BigEndian(content, block << 2);
			if (rle.contentLength > contentLength || rle.contentLength < rle.contentOffset || !rle.unpackCa(unpacked, block * 32000))
				return false;
			rle.contentOffset = rle.contentLength;
		}
		if (blocks == 2) {
			rle.contentLength = contentLength;
			return rle.unpackCa(unpacked, 64000);
		}
		return true;
	}
}
