// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class SfdnStream extends BitStream
{

	final boolean unpack(byte[] unpacked, int unpackedLength)
	{
		if (this.contentLength < 22 + (unpackedLength >> 1) || !RECOIL.isStringAt(this.content, 0, "S101") || (this.content[4] & 0xff | (this.content[5] & 0xff) << 8) != unpackedLength)
			return false;
		this.contentOffset = 22;
		int current = readBits(4);
		int hi = -1;
		for (int unpackedOffset = 0;;) {
			if (hi < 0)
				hi = current;
			else {
				unpacked[unpackedOffset++] = (byte) (hi << 4 | current);
				if (unpackedOffset >= unpackedLength)
					return true;
				hi = -1;
			}
			int code;
			int bit;
			for (code = 0;; code += 2) {
				bit = readBit();
				if (bit == 0)
					break;
				if (bit < 0 || code >= 14)
					return false;
			}
			bit = readBit();
			if (bit < 0)
				return false;
			code += bit;
			current = (current - (this.content[6 + code] & 0xff)) & 15;
		}
	}
}
