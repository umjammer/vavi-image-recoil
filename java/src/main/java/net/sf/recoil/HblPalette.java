// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class HblPalette extends MultiPalette
{

	private boolean hasPalette(int row)
	{
		return this.content[row << 1] != -1 || this.content[row << 1 | 1] != -1;
	}

	final boolean init()
	{
		if (!hasPalette(0))
			return false;
		this.contentOffset = 896;
		for (int row = 1; row < 50; row++) {
			if (hasPalette(row))
				this.contentOffset += 48;
		}
		return this.contentOffset <= this.contentLength;
	}

	@Override void setLinePalette(RECOIL recoil, int y)
	{
		if ((y & 3) == 0 && hasPalette(y >> 2)) {
			this.contentOffset -= 48;
			int bitplanes = recoil.getWidth() == 320 || y == 0 ? 4 : 2;
			for (int c = 0; c < 1 << bitplanes; c++) {
				int offset = this.contentOffset + c * 3;
				int rgb = ((this.content[offset] & 0xff) << 16 | (this.content[offset + 1] & 0xff) << 8 | this.content[offset + 2] & 0xff) & 460551;
				recoil.setStVdiColor(c, rgb << 5 | rgb << 2 | (rgb >> 1 & 197379), bitplanes);
			}
		}
	}
}
