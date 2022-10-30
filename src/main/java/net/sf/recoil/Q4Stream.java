// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class Q4Stream extends RleStream
{

	final int startChunk()
	{
		if (this.contentOffset + 6 > this.contentLength)
			return -1;
		int chunkLength = this.content[this.contentOffset] & 0xff | (this.content[this.contentOffset + 1] & 0xff) << 8;
		this.contentOffset += 6;
		this.contentLength = this.contentOffset + chunkLength;
		return this.contentLength;
	}
	private int codeBits;

	private int readCode()
	{
		do {
			int value = readBits(this.codeBits);
			switch (value) {
			case -1:
			case 0:
				return -1;
			case 1:
				break;
			default:
				return value - 2;
			}
		}
		while (++this.codeBits <= 15);
		return -1;
	}
	private final byte[] unpacked = new byte[65536];

	final boolean unpackQ4()
	{
		this.bits = 0;
		this.codeBits = 3;
		int unpackedLength = 0;
		final int[] offsets = new int[16384];
		for (int codes = 17; codes < 16384; codes++) {
			int code = readCode();
			if (code < 0 || code >= codes) {
				this.content = this.unpacked;
				this.contentOffset = 0;
				this.contentLength = unpackedLength;
				this.lastRepeatValue = 0;
				return true;
			}
			if (unpackedLength >= 65536)
				return false;
			offsets[codes] = unpackedLength;
			if (code <= 16) {
				this.unpacked[unpackedLength++] = (byte) code;
			}
			else {
				int sourceOffset = offsets[code];
				int endOffset = offsets[code + 1];
				if (unpackedLength + endOffset - sourceOffset >= 65536)
					return false;
				do
					this.unpacked[unpackedLength++] = (byte) (this.unpacked[sourceOffset++] & 0xff);
				while (sourceOffset <= endOffset);
			}
		}
		return false;
	}
	private int lastRepeatValue;

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b < 16) {
			this.repeatCount = 1;
			this.repeatValue = b;
			return true;
		}
		b = readByte();
		if (b == 0) {
			this.lastRepeatValue = readByte();
			if (this.lastRepeatValue < 0 || this.lastRepeatValue >= 16)
				return false;
			b = readByte();
		}
		if (b < 0)
			return false;
		this.repeatCount = b * 17;
		b = readByte();
		if (b < 0)
			return false;
		this.repeatCount += b;
		this.repeatValue = this.lastRepeatValue;
		return true;
	}
}
