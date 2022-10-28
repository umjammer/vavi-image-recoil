// Generated automatically with "cito". Do not edit.
package net.sf.recoil;
import java.util.Arrays;

class A4rStream extends Stream
{
	private int outerFlags = 0;
	private int innerFlags = 0;

	private int readFlag()
	{
		if ((this.innerFlags & 127) == 0) {
			if ((this.outerFlags & 127) == 0) {
				if (this.contentOffset >= this.contentLength)
					return -1;
				this.outerFlags = (this.content[this.contentOffset++] & 0xff) << 1 | 1;
			}
			else
				this.outerFlags <<= 1;
			if ((this.outerFlags & 256) == 0)
				this.innerFlags = 1;
			else {
				if (this.contentOffset >= this.contentLength)
					return -1;
				this.innerFlags = (this.content[this.contentOffset++] & 0xff) << 1 | 1;
			}
		}
		else
			this.innerFlags <<= 1;
		return this.innerFlags >> 8 & 1;
	}

	private static final int MIN_ADDRESS = 19984;

	private static final int MAX_ADDRESS = 31231;

	private static final int UNPACKED_LENGTH = 11248;
	final byte[] unpacked = new byte[11248];
	private int unpackedOffset;

	private boolean copyByte()
	{
		int b = readByte();
		if (b < 0 || this.unpackedOffset < 0 || this.unpackedOffset >= 11248)
			return false;
		this.unpacked[this.unpackedOffset++] = (byte) b;
		return true;
	}

	private boolean copyBlock(int distance, int count)
	{
		if (this.unpackedOffset < 0)
			return false;
		int nextOffset = this.unpackedOffset + count;
		if (nextOffset > 11248 || !RECOIL.copyPrevious(this.unpacked, this.unpackedOffset, distance, count))
			return false;
		this.unpackedOffset = nextOffset;
		return true;
	}

	final boolean unpackA4r()
	{
		Arrays.fill(this.unpacked, (byte) 0);
		this.unpackedOffset = -1;
		for (;;) {
			switch (readFlag()) {
			case 0:
				if (!copyByte())
					return false;
				break;
			case 1:
				int b = readByte();
				switch (b) {
				case -1:
					return false;
				case 0:
					if (this.contentOffset >= this.contentLength - 2)
						return false;
					b = readByte();
					this.unpackedOffset = b + (readByte() << 8) + 128 - 19984;
					if (!copyByte())
						return false;
					break;
				case 1:
					b = readByte();
					switch (b) {
					case -1:
						return false;
					case 0:
						return true;
					default:
						if (!copyBlock(1, b + 2))
							return false;
						break;
					}
					break;
				default:
					if (!copyBlock(128 - (b >> 1), 2 + (b & 1)))
						return false;
					break;
				}
				break;
			default:
				return false;
			}
		}
	}
}
