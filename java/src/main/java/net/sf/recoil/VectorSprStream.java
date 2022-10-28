// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class VectorSprStream extends PgcStream
{

	@Override int readValue()
	{
		if (this.contentOffset <= 16)
			return -1;
		return this.content[--this.contentOffset] & 0xff;
	}

	protected @Override int readCommandByte()
	{
		return readValue();
	}
}
