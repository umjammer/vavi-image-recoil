// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class X68KPicStream extends BitStream
{

	final int readLength()
	{
		for (int bits = 1; bits < 21; bits++) {
			switch (readBit()) {
			case 0:
				int length = readBits(bits);
				if (length < 0)
					return -1;
				return length + (1 << bits) - 1;
			case 1:
				break;
			default:
				return -1;
			}
		}
		return -1;
	}
}
