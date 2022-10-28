// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class RastaRenderer extends GtiaRenderer
{

	protected @Override int getPlayfieldByte(int y, int column)
	{
		return this.content[y * 40 + column] & 0xff;
	}
}
