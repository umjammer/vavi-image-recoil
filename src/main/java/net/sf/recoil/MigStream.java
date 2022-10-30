// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class MigStream extends BitStream
{

	private static final int MAX_UNPACKED_LENGTH = 108800;

	final int unpack(byte[] unpacked)
	{
		this.contentOffset = 15;
		for (int unpackedOffset = 0; unpackedOffset < 108800;) {
			int c = readBit();
			if (c < 0)
				return -1;
			int b = readByte();
			if (b < 0)
				return -1;
			if (c == 0)
				unpacked[unpackedOffset++] = (byte) b;
			else {
				if (b >= 128) {
					c = readBits(4);
					if (c < 0)
						return -1;
					b += (c - 1) << 7;
				}
				int distance = b + 1;
				if (unpackedOffset - distance < 0)
					return -1;
				c = -1;
				do {
					b = readBit();
					if (b < 0)
						return -1;
					c++;
				}
				while (b != 0);
				int length = readBits(c);
				if (length < 0)
					return -1;
				if (c >= 16) {
					this.contentOffset += 4;
					if (this.contentOffset >= this.contentLength)
						return unpackedOffset;
					this.bits = 0;
				}
				else {
					length += (1 << c) + 1;
					if (unpackedOffset + length > 108800)
						return -1;
					do {
						unpacked[unpackedOffset] = (byte) (unpacked[unpackedOffset - distance] & 0xff);
						unpackedOffset++;
					}
					while (--length > 0);
				}
			}
		}
		return -1;
	}
}
