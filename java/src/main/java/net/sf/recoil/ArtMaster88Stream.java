// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class ArtMaster88Stream extends RleStream
{
	private int escape = -1;

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b == this.escape) {
			b = readByte();
			if (b < 0)
				return false;
			this.repeatCount = (b - 1) & 255;
			this.escape = -1;
		}
		else {
			this.repeatCount = 1;
			this.escape = this.repeatValue = b;
		}
		return true;
	}

	final boolean skipChunk()
	{
		if (this.contentOffset + 1 >= this.contentLength)
			return false;
		int length = this.content[this.contentOffset] & 0xff | (this.content[this.contentOffset + 1] & 0xff) << 8;
		if (length < 2)
			return false;
		this.contentOffset += length;
		return true;
	}
	final byte[][] planes = new byte[4][32000];

	final boolean readPlanes(int planes, int planeLength)
	{
		for (int plane = 0; plane < planes; plane++) {
			if (!unpack(this.planes[plane], 0, 1, planeLength))
				return false;
		}
		return true;
	}
}
