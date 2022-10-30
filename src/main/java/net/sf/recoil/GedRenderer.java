// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class GedRenderer extends GtiaRenderer
{

	protected @Override int getPlayfieldByte(int y, int column)
	{
		return this.content[3302 + y * 40 + column] & 0xff;
	}
}
