// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class ZimStream extends Stream
{
	private final byte[] flags1 = new byte[1];
	final byte[] flags2 = new byte[8];

	final int readWord()
	{
		if (this.contentOffset + 1 >= this.contentLength)
			return -1;
		int result = this.content[this.contentOffset] & 0xff | (this.content[this.contentOffset + 1] & 0xff) << 8;
		this.contentOffset += 2;
		return result;
	}

	final int readUnpacked(byte[] flags, int unpackedOffset)
	{
		return ((flags[unpackedOffset >> 3] & 0xff) >> (~unpackedOffset & 7) & 1) != 0 ? readByte() : 0;
	}

	final boolean unpack(byte[] flags, byte[] unpacked, int unpackedLength)
	{
		boolean enough = true;
		for (int unpackedOffset = 0; unpackedOffset < unpackedLength; unpackedOffset++) {
			int b = readUnpacked(flags, unpackedOffset);
			if (b < 0) {
				enough = false;
				b = 0;
			}
			unpacked[unpackedOffset] = (byte) b;
		}
		return enough;
	}

	final boolean unpackFlags2()
	{
		int b = readByte();
		if (b < 0)
			return false;
		this.flags1[0] = (byte) b;
		return unpack(this.flags1, this.flags2, 8);
	}
}
