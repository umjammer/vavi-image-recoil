// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class DeepStream extends PackBitsStream
{
	int components = 0;
	private final int[] componentShift = new int[6];
	private int currentByte;
	int[] line = null;

	final boolean setDpel(int chunkOffset, int chunkLength)
	{
		if (chunkLength < 8 || this.content[chunkOffset + 8] != 0 || this.content[chunkOffset + 9] != 0 || this.content[chunkOffset + 10] != 0)
			return false;
		this.components = this.content[chunkOffset + 11] & 0xff;
		if (this.components > 6 || chunkLength != (this.components + 1) << 2)
			return false;
		for (int c = 0; c < this.components; c++) {
			int offset = chunkOffset + 12 + c * 4;
			if (this.content[offset] != 0 || this.content[offset + 2] != 0 || this.content[offset + 3] != 8)
				return false;
			int shift;
			switch (this.content[offset + 1]) {
			case 1:
				shift = 16;
				break;
			case 2:
				shift = 8;
				break;
			case 3:
				shift = 0;
				break;
			case 4:
			case 9:
			case 10:
			case 11:
			case 17:
				shift = -1;
				break;
			default:
				return false;
			}
			this.componentShift[c] = shift;
		}
		return true;
	}

	@Override int readValue()
	{
		int rgb = 0;
		for (int c = 0; c < this.components; c++) {
			int b = readByte();
			if (b < 0)
				return -1;
			int shift = this.componentShift[c];
			if (shift >= 0)
				rgb |= b << shift;
		}
		return rgb;
	}

	private int readNibble()
	{
		if (this.currentByte < 0) {
			this.currentByte = readByte();
			if (this.currentByte < 0)
				return -1;
			return this.currentByte >> 4;
		}
		int result = this.currentByte & 15;
		this.currentByte = -1;
		return result;
	}

	final boolean readDeltaLine(int width, int tvdcOffset)
	{
		if (this.line == null)
			this.line = new int[width];
		for (int c = 0; c < this.components; c++) {
			int count = 0;
			int value = 0;
			this.currentByte = -1;
			for (int x = 0; x < width; x++) {
				if (count == 0) {
					int i = readNibble();
					if (i < 0)
						return false;
					int delta = this.content[tvdcOffset + i * 2 + 1] & 0xff;
					if (delta == 0) {
						if (this.content[tvdcOffset + i * 2] == 0) {
							count = readNibble();
							if (count < 0)
								return false;
						}
					}
					else
						value = (value + delta) & 255;
				}
				else
					count--;
				int rgb = c == 0 ? 0 : this.line[x];
				int shift = this.componentShift[c];
				if (shift >= 0)
					rgb |= value << shift;
				this.line[x] = rgb;
			}
		}
		return true;
	}
}
