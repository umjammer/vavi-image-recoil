// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class RastPalette extends MultiPalette
{
	int colors;

	@Override void setLinePalette(RECOIL recoil, int y)
	{
		int paletteLength = (1 + this.colors) << 1;
		for (int offset = this.contentOffset; offset <= this.contentLength - paletteLength; offset += paletteLength) {
			if (y == ((this.content[offset] & 0xff) << 8 | this.content[offset + 1] & 0xff)) {
				recoil.setStPalette(this.content, offset + 2, this.colors);
				break;
			}
		}
	}
}
