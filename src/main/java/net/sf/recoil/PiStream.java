// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PiStream extends BitStream
{
	byte[] indexes;
	private final byte[] recentColors = new byte[65536];

	private int readInt(int bits, int maxBits)
	{
		for (; bits < maxBits; bits++) {
			int b = readBit();
			if (b == 0)
				break;
			if (b < 0)
				return -1;
		}
		return 1 << bits | readBits(bits);
	}

	private boolean unpackLiteral(int indexesOffset, int depth)
	{
		int offset;
		switch (readBit()) {
		case 1:
			offset = readBit();
			break;
		case 0:
			offset = readInt(1, depth - 1);
			break;
		default:
			return false;
		}
		if (offset < 0)
			return false;
		int recentOffset = indexesOffset == 0 ? 0 : (this.indexes[indexesOffset - 1] & 0xff) << 8;
		offset += recentOffset;
		int c = this.recentColors[offset] & 0xff;
		for (; offset > recentOffset; offset--)
			this.recentColors[offset] = (byte) (this.recentColors[offset - 1] & 0xff);
		this.recentColors[offset] = (byte) c;
		this.indexes[indexesOffset] = (byte) c;
		return true;
	}

	private boolean unpackTwoLiterals(int indexesOffset, int indexesLength, int depth)
	{
		if (!unpackLiteral(indexesOffset, depth))
			return false;
		return indexesOffset + 1 >= indexesLength || unpackLiteral(indexesOffset + 1, depth);
	}

	private int readPosition()
	{
		int position = readBits(2);
		if (position != 3)
			return position;
		position = readBit();
		if (position < 0)
			return -1;
		return 3 + position;
	}

	final boolean unpack(int width, int height, int depth)
	{
		int colors = 1 << depth;
		for (int i = 0; i < colors; i++)
			for (int j = 0; j < colors; j++)
				this.recentColors[i << 8 | j] = (byte) ((i - j) & (colors - 1));
		int indexesLength = width * height;
		this.indexes = new byte[indexesLength];
		if (!unpackTwoLiterals(0, indexesLength, depth))
			return false;
		int lastPosition = -1;
		for (int indexesOffset = 0; indexesOffset < indexesLength;) {
			int position = readPosition();
			if (position < 0)
				return false;
			if (position == lastPosition) {
				do {
					if (!unpackTwoLiterals(indexesOffset, indexesLength, depth))
						return false;
					indexesOffset += 2;
				}
				while (indexesOffset < indexesLength && readBit() == 1);
				lastPosition = -1;
			}
			else {
				int length = readInt(0, 23);
				if (length < 0)
					return false;
				lastPosition = position;
				switch (position) {
				case 0:
					position = indexesOffset == 0 ? 0 : indexesOffset - 2;
					position = (this.indexes[position] & 0xff) == (this.indexes[position + 1] & 0xff) ? 2 : 4;
					break;
				case 1:
					position = width;
					break;
				case 2:
					position = width << 1;
					break;
				case 3:
					position = width - 1;
					break;
				case 4:
					position = width + 1;
					break;
				default:
					throw new AssertionError();
				}
				int copyEnd = indexesOffset + (length << 1);
				if (copyEnd > indexesLength)
					copyEnd = indexesLength;
				for (; indexesOffset < copyEnd; indexesOffset++) {
					int sourceOffset = indexesOffset - position;
					if (sourceOffset < 0)
						sourceOffset &= 1;
					this.indexes[indexesOffset] = (byte) (this.indexes[sourceOffset] & 0xff);
				}
			}
		}
		return true;
	}
}
