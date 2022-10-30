// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class HcmRenderer extends GtiaRenderer
{

	protected @Override int getPlayfieldByte(int y, int column)
	{
		return this.content[2064 + (y << 5) + column] & 0xff;
	}
}
