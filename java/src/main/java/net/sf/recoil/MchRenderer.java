// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class MchRenderer extends GtiaRenderer
{
	boolean dliPlus;

	protected @Override int getPlayfieldByte(int y, int column)
	{
		int offset = ((y >> 3) * this.playfieldColumns + column) * 9;
		int shift = this.dliPlus && (y & 4) != 0 ? 2 : 1;
		return ((this.content[offset] & 0xff) << shift & 256) | this.content[offset + 1 + (y & 7)] & 0xff;
	}
}
