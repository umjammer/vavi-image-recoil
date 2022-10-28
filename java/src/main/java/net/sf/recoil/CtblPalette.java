// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class CtblPalette extends MultiPalette
{
	int colors;

	@Override void setLinePalette(RECOIL recoil, int y)
	{
		recoil.setOcsPalette(this.content, this.contentOffset + (y * this.colors << 1), this.colors);
	}
}
