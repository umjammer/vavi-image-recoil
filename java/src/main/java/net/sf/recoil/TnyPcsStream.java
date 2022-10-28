// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

abstract class TnyPcsStream extends RleStream
{

	protected final boolean readTnyCommand()
	{
		int b = readByte();
		if (b < 0)
			return false;
		if (b < 128) {
			if (b == 0 || b == 1) {
				if (this.contentOffset >= this.contentLength - 1)
					return false;
				this.repeatCount = (this.content[this.contentOffset] & 0xff) << 8 | this.content[this.contentOffset + 1] & 0xff;
				this.contentOffset += 2;
			}
			else
				this.repeatCount = b;
			this.repeatValue = b == 1 ? -1 : readValue();
		}
		else {
			this.repeatCount = 256 - b;
			this.repeatValue = -1;
		}
		return true;
	}
}
