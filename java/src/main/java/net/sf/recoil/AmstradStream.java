// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class AmstradStream extends RleStream
{
	private int blockLength;

	protected @Override boolean readCommand()
	{
		while (this.blockLength <= 0) {
			if (readByte() != 'M' || readByte() != 'J' || readByte() != 'H')
				return false;
			int lo = readByte();
			if (lo < 0)
				return false;
			int hi = readByte();
			if (hi < 0)
				return false;
			this.blockLength = hi << 8 | lo;
		}
		int b = readByte();
		if (b < 0)
			return false;
		if (b == 1) {
			this.repeatCount = readByte();
			if (this.repeatCount == 0)
				this.repeatCount = 256;
			this.repeatValue = readByte();
		}
		else {
			this.repeatCount = 1;
			this.repeatValue = b;
		}
		this.blockLength -= this.repeatCount;
		return true;
	}

	static boolean unpackFile(byte[] content, int contentOffset, int contentLength, byte[] unpacked, int unpackedLength)
	{
		final AmstradStream rle = new AmstradStream();
		rle.content = content;
		rle.contentOffset = contentOffset;
		rle.contentLength = contentLength;
		rle.blockLength = 0;
		return rle.unpack(unpacked, 0, 1, unpackedLength);
	}
}
