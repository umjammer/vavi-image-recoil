// Generated automatically with "cito". Do not edit.
package net.sf.recoil;
import java.util.Arrays;

class InflateStream extends Stream
{
	private int bits;

	private int readBit()
	{
		if (this.bits <= 1) {
			if (this.contentOffset >= this.contentLength)
				return -1;
			this.bits = this.content[this.contentOffset++] & 0xff | 256;
		}
		int result = this.bits & 1;
		this.bits >>= 1;
		return result;
	}

	private int readBits(int count)
	{
		int result = 0;
		for (int rank = 0; rank < count; rank++) {
			switch (readBit()) {
			case -1:
				return -1;
			case 1:
				result |= 1 << rank;
				break;
			default:
				break;
			}
		}
		return result;
	}

	private static final int PRIMARY_SYMBOLS = 288;

	private static final int DISTANCE_SYMBOLS = 30;

	private static final int ALL_SYMBOLS = 318;
	private final byte[] symbolCodeLength = new byte[318];

	private static final int TREE_SIZE = 16;

	private static final int PRIMARY_TREE = 0;

	private static final int DISTANCE_TREE = 16;
	private final int[] nBitCodeCount = new int[32];
	private final int[] nBitCodeOffset = new int[32];
	private final short[] codeToSymbol = new short[318];

	private void buildHuffmanTrees()
	{
		Arrays.fill(this.nBitCodeCount, 0);
		for (int i = 0; i < 318; i++)
			this.nBitCodeCount[this.symbolCodeLength[i] & 0xff]++;
		int offset = 0;
		for (int i = 0; i < 32; i++) {
			this.nBitCodeOffset[i] = offset;
			offset += this.nBitCodeCount[i];
		}
		for (int i = 0; i < 318; i++)
			this.codeToSymbol[this.nBitCodeOffset[this.symbolCodeLength[i] & 0xff]++] = (short) i;
	}

	private int fetchCode(int tree)
	{
		int code = 0;
		do {
			int bit = readBit();
			if (bit < 0)
				return -1;
			code = (code << 1) + bit - this.nBitCodeCount[++tree];
			if (code < 0)
				return this.codeToSymbol[this.nBitCodeOffset[tree] + code];
		}
		while ((tree & 15) != 15);
		return -1;
	}

	private int inflate(byte[] unpacked, int unpackedLength)
	{
		int unpackedOffset = 0;
		this.bits = 0;
		int lastBlock;
		do {
			lastBlock = readBit();
			int count;
			switch (readBits(2)) {
			case 0:
				this.bits = 0;
				count = readBits(16);
				if (readBits(16) != (count ^ 65535))
					return -1;
				if (count > unpackedLength - unpackedOffset)
					count = unpackedLength - unpackedOffset;
				if (!readBytes(unpacked, unpackedOffset, count))
					return -1;
				unpackedOffset += count;
				if (unpackedOffset == unpackedLength)
					return unpackedOffset;
				continue;
			case 1:
				for (int i = 0; i < 144; i++)
					this.symbolCodeLength[i] = 8;
				for (int i = 144; i < 256; i++)
					this.symbolCodeLength[i] = 9;
				for (int i = 256; i < 280; i++)
					this.symbolCodeLength[i] = 7;
				for (int i = 280; i < 288; i++)
					this.symbolCodeLength[i] = 8;
				for (int i = 288; i < 318; i++)
					this.symbolCodeLength[i] = 21;
				break;
			case 2:
				int primaryCodes = 257 + readBits(5);
				int codes = 289 + readBits(5);
				if (codes > 318)
					return -1;
				int temporaryCodes = readBits(4);
				if (temporaryCodes < 0)
					return -1;
				temporaryCodes += 4;
				Arrays.fill(this.symbolCodeLength, (byte) 0);
				for (int i = 0; i < temporaryCodes; i++) {
					int bits = readBits(3);
					if (bits < 0)
						return -1;
					this.symbolCodeLength[INFLATE_TEMP_SYMBOLS[i] & 0xff] = (byte) bits;
				}
				buildHuffmanTrees();
				int length = 0;
				count = 1;
				for (int i = 0; i < codes; i++) {
					if (--count == 0) {
						int symbol = fetchCode(0);
						switch (symbol) {
						case -1:
							return -1;
						case 16:
							count = readBits(2);
							if (count < 0)
								return -1;
							count += 3;
							break;
						case 17:
							length = 0;
							count = readBits(3);
							if (count < 0)
								return -1;
							count += 3;
							break;
						case 18:
							length = 0;
							count = readBits(7);
							if (count < 0)
								return -1;
							count += 11;
							break;
						default:
							length = symbol;
							count = 1;
							break;
						}
					}
					if (i == primaryCodes)
						i = 288;
					this.symbolCodeLength[i] = (byte) (i < 288 ? length : 16 + length);
				}
				break;
			default:
				return -1;
			}
			buildHuffmanTrees();
			for (;;) {
				int symbol = fetchCode(0);
				if (symbol < 0)
					return -1;
				else if (symbol < 256)
					unpacked[unpackedOffset++] = (byte) symbol;
				else if (symbol == 256)
					break;
				else {
					switch (symbol) {
					case 257:
					case 258:
					case 259:
					case 260:
					case 261:
					case 262:
					case 263:
					case 264:
						count = symbol - 254;
						break;
					case 285:
						count = 258;
						break;
					case 286:
					case 287:
						return -1;
					default:
						symbol -= 261;
						count = readBits(symbol >> 2);
						if (count < 0)
							return -1;
						count += ((4 + (symbol & 3)) << (symbol >> 2)) + 3;
						break;
					}
					symbol = fetchCode(16);
					int distance;
					switch (symbol) {
					case -1:
						return -1;
					case 288:
					case 289:
					case 290:
					case 291:
						distance = symbol - 287;
						break;
					default:
						symbol -= 290;
						distance = readBits(symbol >> 1);
						if (distance < 0)
							return -1;
						distance += ((2 + (symbol & 1)) << (symbol >> 1)) + 1;
						break;
					}
					if (count > unpackedLength - unpackedOffset)
						count = unpackedLength - unpackedOffset;
					if (!RECOIL.copyPrevious(unpacked, unpackedOffset, distance, count))
						return -1;
					unpackedOffset += count;
				}
				if (unpackedOffset == unpackedLength)
					return unpackedOffset;
			}
		}
		while (lastBlock == 0);
		return unpackedOffset;
	}

	final int uncompress(byte[] unpacked, int unpackedLength)
	{
		int b0 = readByte();
		if ((b0 & 143) != 8)
			return -1;
		int b1 = readByte();
		if ((b1 & 32) != 0 || (b0 << 8 | b1) % 31 != 0)
			return -1;
		return inflate(unpacked, unpackedLength);
	}

	private static final byte[] INFLATE_TEMP_SYMBOLS = { 16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2,
		14, 1, 15 };
}
