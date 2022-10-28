// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class PcsStream extends TnyPcsStream
{
	private int commandCount;
	private boolean palette;

	protected @Override boolean readCommand()
	{
		if (this.commandCount <= 0)
			return false;
		this.commandCount--;
		return readTnyCommand();
	}

	@Override int readValue()
	{
		if (!this.palette)
			return readByte();
		if (this.contentOffset >= this.contentLength - 1)
			return -1;
		int value = (this.content[this.contentOffset] & 0xff) << 8 | this.content[this.contentOffset + 1] & 0xff;
		this.contentOffset += 2;
		return value;
	}

	private boolean startBlock()
	{
		if (this.contentOffset >= this.contentLength - 1)
			return false;
		this.commandCount = (this.content[this.contentOffset] & 0xff) << 8 | this.content[this.contentOffset + 1] & 0xff;
		this.contentOffset += 2;
		return true;
	}

	private boolean endBlock()
	{
		while (this.repeatCount > 0 || this.commandCount > 0) {
			if (readRle() < 0)
				return false;
		}
		return true;
	}

	private static final int UNPACKED_LENGTH = 51136;

	final boolean unpackPcs(byte[] unpacked)
	{
		this.palette = false;
		if (!startBlock() || !unpack(unpacked, 0, 1, 32000) || !endBlock())
			return false;
		this.palette = true;
		if (!startBlock())
			return false;
		for (int unpackedOffset = 32000; unpackedOffset < 51136; unpackedOffset += 2) {
			int b = readRle();
			if (b < 0)
				return false;
			unpacked[unpackedOffset] = (byte) (b >> 8);
			unpacked[unpackedOffset + 1] = (byte) b;
		}
		return endBlock();
	}
}
