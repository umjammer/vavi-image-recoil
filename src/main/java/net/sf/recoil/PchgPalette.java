// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PchgPalette extends MultiPalette
{
	private boolean ocs;
	private int startLine;
	private int lineCount;
	private byte[] havePaletteChange;
	private int treeOffset;
	private int treeLastOffset;
	private boolean compressed;

	private int readHuffman()
	{
		int offset = this.treeLastOffset;
		for (;;) {
			switch (readBit()) {
			case 0:
				offset -= 2;
				if (offset < this.treeOffset)
					return -1;
				if ((this.content[offset] & 129) == 1)
					return this.content[offset + 1] & 0xff;
				break;
			case 1:
				int hi = this.content[offset] & 0xff;
				int lo = this.content[offset + 1] & 0xff;
				if (hi < 128)
					return lo;
				offset += (hi - 256) << 8 | lo;
				if (offset < this.treeOffset)
					return -1;
				break;
			default:
				return -1;
			}
		}
	}

	final boolean init()
	{
		if (this.contentOffset + 20 > this.contentLength || this.content[this.contentOffset] != 0)
			return false;
		switch (this.content[this.contentOffset + 3] & 3) {
		case 1:
			this.ocs = true;
			break;
		case 2:
			this.ocs = false;
			break;
		default:
			return false;
		}
		this.startLine = (this.content[this.contentOffset + 4] & 0xff) << 8 | this.content[this.contentOffset + 5] & 0xff;
		this.lineCount = (this.content[this.contentOffset + 6] & 0xff) << 8 | this.content[this.contentOffset + 7] & 0xff;
		int havePaletteChangeLength = (this.lineCount + 31) >> 5 << 2;
		this.havePaletteChange = new byte[havePaletteChangeLength];
		switch (this.content[this.contentOffset + 1]) {
		case 0:
			this.contentOffset += 20;
			if (!readBytes(this.havePaletteChange, 0, havePaletteChangeLength))
				return false;
			this.compressed = false;
			break;
		case 1:
			this.treeOffset = this.contentOffset + 28;
			if (this.treeOffset > this.contentLength)
				return false;
			int treeLength = RECOIL.get32BigEndian(this.content, this.contentOffset + 20);
			if (treeLength < 2 || treeLength > 1022)
				return false;
			this.contentOffset = this.treeOffset + treeLength;
			this.treeLastOffset = this.contentOffset - 2;
			for (int i = 0; i < havePaletteChangeLength; i++) {
				int b = readHuffman();
				if (b < 0)
					return false;
				this.havePaletteChange[i] = (byte) b;
			}
			this.compressed = true;
			break;
		default:
			return false;
		}
		return true;
	}

	private int unpackByte()
	{
		return this.compressed ? readHuffman() : readByte();
	}

	private void setOcsColors(RECOIL recoil, int paletteOffset, int count)
	{
		while (--count >= 0) {
			int rr = unpackByte();
			if (rr < 0)
				return;
			int gb = unpackByte();
			if (gb < 0)
				return;
			recoil.setOcsColor(paletteOffset + (rr >> 4), rr, gb);
		}
	}

	@Override void setLinePalette(RECOIL recoil, int y)
	{
		y -= this.startLine;
		if (y < 0 || y >= this.lineCount)
			return;
		if (((this.havePaletteChange[y >> 3] & 0xff) >> (~y & 7) & 1) == 0)
			return;
		int count = unpackByte();
		if (count < 0)
			return;
		int count2 = unpackByte();
		if (count2 < 0)
			return;
		if (this.ocs) {
			setOcsColors(recoil, 0, count);
			setOcsColors(recoil, 16, count2);
		}
		else {
			count = count << 8 | count2;
			while (--count >= 0) {
				if (unpackByte() != 0)
					return;
				int c = unpackByte();
				if (c < 0 || unpackByte() < 0)
					return;
				int r = unpackByte();
				if (r < 0)
					return;
				int b = unpackByte();
				if (b < 0)
					return;
				int g = unpackByte();
				if (g < 0)
					return;
				recoil.contentPalette[c] = r << 16 | g << 8 | b;
			}
		}
	}
}
