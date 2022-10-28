// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class ImgStream extends RleStream
{
	private int patternRepeatCount = 0;

	private static final int KEEP = 256;

	final int getLineRepeatCount()
	{
		if (this.repeatCount == 0 && this.contentOffset < this.contentLength - 4 && this.content[this.contentOffset] == 0 && this.content[this.contentOffset + 1] == 0 && this.content[this.contentOffset + 2] == -1) {
			this.contentOffset += 4;
			return (this.content[this.contentOffset - 1] & 0xff) + 1;
		}
		return 1;
	}

	protected @Override boolean readCommand()
	{
		if (this.patternRepeatCount > 1) {
			this.patternRepeatCount--;
			this.repeatCount = (this.content[6] & 0xff) << 8 | this.content[7] & 0xff;
			this.contentOffset -= this.repeatCount;
			return true;
		}
		int b = readByte();
		switch (b) {
		case -1:
			return false;
		case 0:
			b = readByte();
			if (b < 0)
				return false;
			if (b == 0) {
				b = readByte();
				if (b < 0)
					return false;
				this.repeatCount = b + 1;
				this.repeatValue = 256;
				return true;
			}
			this.patternRepeatCount = b;
			this.repeatCount = (this.content[6] & 0xff) << 8 | this.content[7] & 0xff;
			this.repeatValue = -1;
			return true;
		case 128:
			this.repeatCount = readByte();
			if (this.repeatCount < 0)
				return false;
			if (this.repeatCount == 0)
				this.repeatCount = 256;
			this.repeatValue = -1;
			return true;
		default:
			this.repeatCount = b & 127;
			this.repeatValue = b >= 128 ? 255 : 0;
			return true;
		}
	}
}
