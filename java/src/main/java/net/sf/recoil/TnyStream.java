// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class TnyStream extends TnyPcsStream
{
	int valueOffset;
	int valueLength;

	protected @Override boolean readCommand()
	{
		return readTnyCommand();
	}

	@Override int readValue()
	{
		if (this.valueOffset + 1 >= this.valueLength)
			return -1;
		int value = (this.content[this.valueOffset] & 0xff) << 8 | this.content[this.valueOffset + 1] & 0xff;
		this.valueOffset += 2;
		return value;
	}
}
