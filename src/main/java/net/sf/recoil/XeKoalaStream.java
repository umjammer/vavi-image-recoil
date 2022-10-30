// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class XeKoalaStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		boolean rle;
		if (b < 128)
			rle = true;
		else {
			b -= 128;
			rle = false;
		}
		if (b == 0) {
			int hi = readByte();
			if (hi < 0)
				return false;
			b = readByte();
			if (b < 0)
				return false;
			b |= hi << 8;
		}
		this.repeatCount = b;
		this.repeatValue = rle ? readByte() : -1;
		return true;
	}
	final byte[] unpacked = new byte[7680];

	final boolean unpackRaw(int type, int unpackedLength)
	{
		switch (type) {
		case 0:
			if (this.contentLength - this.contentOffset != unpackedLength)
				return false;
			System.arraycopy(this.content, this.contentOffset, this.unpacked, 0, unpackedLength);
			return true;
		case 1:
			for (int x = 0; x < 40; x++) {
				for (int unpackedOffset = x; unpackedOffset < 80; unpackedOffset += 40) {
					if (!unpack(this.unpacked, unpackedOffset, 80, unpackedLength))
						return false;
				}
			}
			return true;
		case 2:
			return unpack(this.unpacked, 0, 1, unpackedLength);
		default:
			return false;
		}
	}

	final boolean unpackWrapped(int unpackedLength)
	{
		if (readByte() != 255 || readByte() != 128 || readByte() != 201 || readByte() != 199)
			return false;
		int offset = readByte();
		if (offset < 26 || readByte() != 0 || readByte() != 1)
			return false;
		int type = readByte();
		if (readByte() != 14 || readByte() != 0 || readByte() != 40 || readByte() != 0 || readByte() != 192)
			return false;
		this.contentOffset += 7;
		if (readByte() != 0 || readByte() != 0)
			return false;
		this.contentOffset += offset - 21;
		return unpackRaw(type, unpackedLength);
	}
}
