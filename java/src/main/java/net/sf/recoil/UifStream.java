// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class UifStream extends UflStream
{

	@Override int readValue()
	{
		if (this.contentOffset <= 2)
			return -1;
		return this.content[--this.contentOffset] & 0xff;
	}

	final boolean startSifFrame(int contentOffset)
	{
		if (contentOffset + 5 >= this.contentLength)
			return false;
		int packedLength = (this.content[contentOffset] & 0xff) + ((this.content[contentOffset + 1] & 0xff) << 8) - 37890;
		if (packedLength <= 0 || contentOffset + 2 + packedLength > this.contentLength)
			return false;
		this.contentOffset = contentOffset + 2 + packedLength;
		this.escape = this.content[contentOffset + 2] & 0xff;
		return true;
	}
}
