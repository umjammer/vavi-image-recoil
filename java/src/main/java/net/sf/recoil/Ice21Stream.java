// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class Ice21Stream
{
	byte[] content;
	int contentOffset;
	int contentStart;
	protected int bits;

	final int getUnpackedLength()
	{
		if (this.contentStart + 16 > this.contentOffset || !RECOIL.isStringAt(this.content, this.contentStart, "Ice!") || RECOIL.get32BigEndian(this.content, this.contentStart + 4) != this.contentOffset - this.contentStart)
			return -1;
		return RECOIL.get32BigEndian(this.content, this.contentStart + 8);
	}

	protected final int readBit()
	{
		int b = this.bits;
		int next = b & 2147483647;
		if (next == 0) {
			this.contentOffset -= 4;
			if (this.contentOffset < this.contentStart)
				return -1;
			b = RECOIL.get32BigEndian(this.content, this.contentOffset);
			this.bits = (b & 2147483647) << 1 | 1;
		}
		else
			this.bits = next << 1;
		return b >> 31 & 1;
	}

	protected final int readBits(int count)
	{
		int result = 0;
		while (--count >= 0) {
			int bit = readBit();
			if (bit < 0)
				return -1;
			result = result << 1 | bit;
		}
		return result;
	}

	private int countOnes(int max)
	{
		for (int result = 0; result < max; result++) {
			switch (readBit()) {
			case -1:
				return -1;
			case 0:
				return result;
			default:
				break;
			}
		}
		return max;
	}

	private int readLiteralLength()
	{
		int o = 1;
		for (int n = 0;; n++) {
			int c = READ_LITERAL_LENGTH_BITS[n] & 0xff;
			int b = readBits(c);
			if (b < 0)
				return -1;
			c = (1 << c) - 1;
			if (b < c || n == 5)
				return o + b;
			o += c;
		}
	}

	private int readEncoded(int maxCount, byte[] extraBits, int[] offsets)
	{
		int n = countOnes(maxCount);
		if (n < 0)
			return -1;
		int b = readBits(extraBits[n] & 0xff);
		if (b < 0)
			return -1;
		return offsets[n] + b;
	}

	final boolean unpack(byte[] unpacked, int unpackedStart, int unpackedEnd)
	{
		this.contentStart += 12;
		this.contentOffset -= 4;
		this.bits = RECOIL.get32BigEndian(this.content, this.contentOffset);
		for (int unpackedOffset = unpackedEnd; unpackedOffset > unpackedStart;) {
			int length;
			switch (readBit()) {
			case -1:
				return false;
			case 1:
				length = readLiteralLength();
				if (length > unpackedOffset - unpackedStart)
					length = unpackedOffset - unpackedStart;
				this.contentOffset -= length;
				if (this.contentOffset < this.contentStart)
					return false;
				unpackedOffset -= length;
				System.arraycopy(this.content, this.contentOffset, unpacked, unpackedOffset, length);
				if (unpackedOffset == unpackedStart)
					return true;
				break;
			default:
				break;
			}
			length = readEncoded(4, UNPACK_LENGTH_EXTRA_BITS, UNPACK_LENGTH_OFFSETS);
			int offset;
			switch (length) {
			case -1:
				return false;
			case 0:
				switch (readBit()) {
				case -1:
					return false;
				case 0:
					offset = readBits(6);
					if (offset < 0)
						return false;
					break;
				default:
					offset = readBits(9);
					if (offset < 0)
						return false;
					offset += 64;
					break;
				}
				break;
			default:
				offset = readEncoded(2, UNPACK_OFFSET_EXTRA_BITS, UNPACK_OFFSET_OFFSETS);
				if (offset < 0)
					return false;
				break;
			}
			length += 2;
			offset += length;
			if (unpackedOffset + offset > unpackedEnd)
				return false;
			if (length > unpackedOffset - unpackedStart)
				length = unpackedOffset - unpackedStart;
			unpackedOffset -= length;
			System.arraycopy(unpacked, unpackedOffset + offset, unpacked, unpackedOffset, length);
		}
		return true;
	}

	private static final byte[] READ_LITERAL_LENGTH_BITS = { 1, 2, 2, 3, 8, 15 };

	private static final byte[] UNPACK_LENGTH_EXTRA_BITS = { 0, 0, 1, 2, 10 };

	private static final int[] UNPACK_LENGTH_OFFSETS = { 0, 1, 2, 4, 8 };

	private static final byte[] UNPACK_OFFSET_EXTRA_BITS = { 8, 5, 12 };

	private static final int[] UNPACK_OFFSET_OFFSETS = { 32, 0, 288 };
}
