// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class ArtPalette extends MultiPalette
{

	@Override void setLinePalette(RECOIL recoil, int y)
	{
		if ((y & 1) == 0)
			recoil.setStPalette(this.content, 32768 + (y << 4), 16);
	}
}
