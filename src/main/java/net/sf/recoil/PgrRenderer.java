// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PgrRenderer extends GtiaRenderer
{
	int screenOffset;

	protected @Override int getPlayfieldByte(int y, int column)
	{
		return this.content[this.screenOffset + column] & 0xff;
	}
}
