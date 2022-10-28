// Generated automatically with "cito". Do not edit.
package net.sf.recoil;
import java.util.Arrays;

class FanoTree
{
	/**
	 * Count[n] == number of codes of bit length n.
	 */
	private final int[] count = new int[16];
	/**
	 * Values sorted by code length.
	 */
	private final byte[] values = new byte[256];

	final void create(byte[] content, int contentOffset, int codeCount)
	{
		Arrays.fill(this.count, 0);
		for (int code = 0; code < codeCount; code++)
			this.count[RECOIL.getNibble(content, contentOffset, code)]++;
		final int[] positions = new int[16];
		int position = 0;
		for (int bits = 0; bits < 16; bits++) {
			positions[bits] = position;
			position += this.count[bits];
		}
		for (int code = 0; code < codeCount; code++)
			this.values[positions[RECOIL.getNibble(content, contentOffset, code)]++] = (byte) code;
	}

	final int readCode(BitStream bitStream)
	{
		int code = 0;
		int valuesOffset = this.count[0];
		for (int bits = 1; bits < 16; bits++) {
			int bit = bitStream.readBit();
			if (bit < 0)
				return -1;
			code = code << 1 | bit;
			int count = this.count[bits];
			if (code < count)
				return this.values[valuesOffset + code] & 0xff;
			code -= count;
			valuesOffset += count;
		}
		return -1;
	}
}
