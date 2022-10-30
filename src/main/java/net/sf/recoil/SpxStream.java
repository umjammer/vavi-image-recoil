// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class SpxStream extends Ice21Stream
{

	private int readCount()
	{
		int b = readBits(2);
		if (b < 0)
			return -1;
		return readBits((b + 1) << 2);
	}

	final boolean unpackV2(byte[] unpacked, int unpackedLength)
	{
		this.bits = 0;
		for (int unpackedOffset = unpackedLength; unpackedOffset > 0;) {
			int count;
			switch (readBit()) {
			case -1:
				return false;
			case 0:
				count = readCount();
				if (count <= 0)
					return false;
				if (count > unpackedOffset)
					count = unpackedOffset;
				for (int i = 0; i < count; i++) {
					int b = readBits(8);
					if (b < 0)
						return false;
					unpacked[--unpackedOffset] = (byte) b;
				}
				if (unpackedOffset == 0)
					return true;
				if (count == 65535)
					continue;
				break;
			default:
				break;
			}
			int distance = readCount();
			if (distance <= 0 || unpackedOffset + distance > unpackedLength)
				return false;
			count = readCount();
			if (count < 0)
				return false;
			count += 3;
			if (count > unpackedOffset)
				count = unpackedOffset;
			do {
				unpackedOffset--;
				unpacked[unpackedOffset] = (byte) (unpacked[unpackedOffset + distance] & 0xff);
			}
			while (--count > 0);
		}
		return true;
	}
}
