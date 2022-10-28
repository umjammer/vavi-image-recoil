// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class Lz4Stream extends Stream
{
	byte[] unpacked;
	int unpackedOffset;
	int unpackedLength;

	final boolean copy(int count)
	{
		if (this.unpackedOffset + count > this.unpackedLength || !readBytes(this.unpacked, this.unpackedOffset, count))
			return false;
		this.unpackedOffset += count;
		return true;
	}

	final int readCount(int count)
	{
		if (count == 15) {
			int b;
			do {
				b = readByte();
				if (b < 0)
					return -1;
				count += b;
			}
			while (b == 255);
		}
		return count;
	}
}
