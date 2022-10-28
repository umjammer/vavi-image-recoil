// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class ShamLacePalette extends MultiPalette
{

	@Override void setLinePalette(RECOIL recoil, int y)
	{
		recoil.setOcsPalette(this.content, this.contentOffset + (y >> 1 << 5), 16);
	}
}
