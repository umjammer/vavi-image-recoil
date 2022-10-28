// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PackBitsStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b < 128) {
			this.repeatCount = b + 1;
			this.repeatValue = -1;
		}
		else {
			this.repeatCount = 257 - b;
			this.repeatValue = readValue();
		}
		return true;
	}

	final boolean unpackBitplaneLines(byte[] unpacked, int width, int height, int bitplanes, boolean compressed, boolean hasMask)
	{
		int bytesPerBitplane = (width + 15) >> 4 << 1;
		int bytesPerLine = bitplanes * bytesPerBitplane;
		for (int y = 0; y < height; y++) {
			for (int bitplane = 0; bitplane < bitplanes; bitplane++) {
				for (int w = bitplane << 1; w < bytesPerLine; w += bitplanes << 1) {
					for (int x = 0; x < 2; x++) {
						int b = compressed ? readRle() : readByte();
						if (b < 0)
							return false;
						unpacked[y * bytesPerLine + w + x] = (byte) b;
					}
				}
			}
			if (hasMask) {
				for (int x = 0; x < bytesPerBitplane; x++) {
					int b = compressed ? readRle() : readByte();
					if (b < 0)
						return false;
				}
			}
		}
		return true;
	}
}
