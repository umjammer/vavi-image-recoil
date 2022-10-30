// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class BbgStream extends RleStream
{
	int valueBits;
	int countBits;

	/**
	 * Reads the requested number of bits and returns them
	 * as an unsigned integer with the first bit read as the least significant.
	 * Returns -1 on end of stream.
	 */
	final int readBitsReverse(int count)
	{
		int result = 0;
		for (int i = 0; i < count; i++) {
			switch (readBit()) {
			case 0:
				break;
			case 1:
				result |= 1 << i;
				break;
			default:
				return -1;
			}
		}
		return result;
	}

	protected @Override boolean readCommand()
	{
		switch (readBit()) {
		case 0:
			this.repeatCount = 1;
			break;
		case 1:
			this.repeatCount = readBitsReverse(this.countBits);
			if (this.repeatCount <= 0)
				return false;
			break;
		default:
			return false;
		}
		this.repeatValue = readBitsReverse(this.valueBits);
		return true;
	}
}
