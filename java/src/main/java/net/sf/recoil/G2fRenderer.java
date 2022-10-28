// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class G2fRenderer extends GtiaRenderer
{
	int fontOffset;
	int inverse2Offset;
	int vbxeOffset;

	protected @Override int getHiresColor(int c)
	{
		return this.vbxeOffset >= 0 ? this.colors[5] & 0xff : (c & 240) + (this.colors[5] & 14);
	}

	protected @Override int getPlayfieldByte(int y, int column)
	{
		if (this.vbxeOffset >= 0) {
			int colorOffset = this.vbxeOffset + 3 + ((24 - (this.playfieldColumns >> 1) + column) * 240 + y / (this.content[this.vbxeOffset + 2] & 0xff)) * 12 + 2;
			this.colors[4] = (byte) (this.content[colorOffset] & 0xff);
			this.colors[5] = (byte) (this.content[colorOffset + 2] & 0xff);
			this.colors[6] = (byte) (this.content[colorOffset + 4] & 0xff);
		}
		int charOffset = (y >> 3) * this.playfieldColumns + column;
		int ch = this.content[3 + charOffset] & 0xff;
		int inverse = this.inverse2Offset >= 0 && (y & 4) != 0 ? this.content[this.inverse2Offset + charOffset] & 0xff : ch;
		return (inverse & 128) << 1 | this.content[this.fontOffset + ((ch & 127) << 3) + (y & 7)] & 0xff;
	}

	static boolean setSprite(byte[] hpos, byte[] sizes, int i, byte[] content, int spriteOffset)
	{
		spriteOffset += i << 10;
		int value = content[spriteOffset + 1] & 0xff;
		if (value >= 128) {
			hpos[i] = 0;
			return true;
		}
		value &= 15;
		switch (value) {
		case 0:
			value = 1;
			break;
		case 1:
		case 2:
		case 4:
			break;
		default:
			return false;
		}
		sizes[i] = (byte) value;
		hpos[i] = (byte) (32 + (content[spriteOffset] & 0xff));
		return true;
	}
}
