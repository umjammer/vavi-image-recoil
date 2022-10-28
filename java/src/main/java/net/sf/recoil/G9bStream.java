// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class G9bStream extends BitStream
{

	private static final int BLOCK_END = -2;

	private int readLength()
	{
		for (int length = 1; length < 65536;) {
			switch (readBit()) {
			case 0:
				return length + 1;
			case 1:
				break;
			default:
				return -1;
			}
			length <<= 1;
			switch (readBit()) {
			case 0:
				break;
			case 1:
				length++;
				break;
			default:
				return -1;
			}
		}
		return -2;
	}

	final boolean unpack(byte[] unpacked, int headerLength, int unpackedLength)
	{
		this.contentOffset = headerLength + 3;
		for (int unpackedOffset = headerLength; unpackedOffset < unpackedLength;) {
			int b;
			switch (readBit()) {
			case 0:
				b = readByte();
				if (b < 0)
					return false;
				unpacked[unpackedOffset++] = (byte) b;
				break;
			case 1:
				int length = readLength();
				if (length == -2) {
					this.contentOffset += 2;
					this.bits = 0;
					break;
				}
				if (length < 0 || unpackedOffset + length > unpackedLength)
					return false;
				int distance = readByte();
				if (distance < 0)
					return false;
				if (distance >= 128) {
					b = readBits(4);
					if (b < 0)
						return false;
					distance += (b - 1) << 7;
				}
				distance++;
				if (unpackedOffset - distance < headerLength)
					return false;
				do {
					unpacked[unpackedOffset] = (byte) (unpacked[unpackedOffset - distance] & 0xff);
					unpackedOffset++;
				}
				while (--length > 0);
				break;
			default:
				return false;
			}
		}
		return true;
	}
}
