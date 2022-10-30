// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

/**
 * Readable in-memory Run-Length-Encoded stream.
 * This class contains the compression logic.
 * Subclasses must implement <code>ReadCommand()</code>
 * and are allowed to override <code>ReadValue()</code>.
 */
abstract class RleStream extends BitStream
{
	/**
	 * Block length.
	 */
	int repeatCount = 0;
	/**
	 * Value for an RLE block, -1 for a block of literals.
	 */
	protected int repeatValue;

	/**
	 * Decodes a block from the stream.
	 * Fills <code>RepeatCount</code> with the length of the block.
	 * Sets <code>RepeatValue</code> to the RLE value
	 * or -1 if the block consists of <code>RepeatCount</code> literals.
	 * Returns <code>false</code> on end of stream.
	 */
	protected abstract boolean readCommand();

	int readValue()
	{
		return readByte();
	}

	/**
	 * Returns the next uncompressed byte or -1 on error.
	 */
	final int readRle()
	{
		while (this.repeatCount == 0) {
			if (!readCommand())
				return -1;
		}
		this.repeatCount--;
		if (this.repeatValue >= 0)
			return this.repeatValue;
		return readValue();
	}

	/**
	 * Uncompresses bytes to <code>unpacked[unpackedOffset]</code>,<code>unpacked[unpackedOffset + unpackedStride]</code>,<code>unpacked[unpackedOffset + 2 * unpackedStride]</code>,
	 * ... as long as indexes are smaller than <code>unpackedEnd</code>.
	 * Returns <code>true</code> on success, <code>false</code> on error.
	 */
	final boolean unpack(byte[] unpacked, int unpackedOffset, int unpackedStride, int unpackedEnd)
	{
		for (; unpackedOffset < unpackedEnd; unpackedOffset += unpackedStride) {
			int b = readRle();
			if (b < 0)
				return false;
			unpacked[unpackedOffset] = (byte) b;
		}
		return true;
	}

	/**
	 * Uncompresses laiding out bytes vertically column by column,<code>unpackedStride</code> being line width.
	 * Returns <code>true</code> on success, <code>false</code> on error.
	 */
	final boolean unpackColumns(byte[] unpacked, int unpackedOffset, int unpackedStride, int unpackedEnd)
	{
		for (int x = 0; x < unpackedStride; x++) {
			if (!unpack(unpacked, unpackedOffset + x, unpackedStride, unpackedEnd))
				return false;
		}
		return true;
	}

	/**
	 * Uncompresses bytes to <code>unpacked[unpackedOffset]</code>, <code>unpacked[unpackedOffset + 1]</code>,<code>unpacked[unpackedOffset + unpackedStride]</code>, <code>unpacked[unpackedOffset + unpackedStride + 1]</code>,<code>unpacked[unpackedOffset + 2 * unpackedStride]</code>, <code>unpacked[unpackedOffset + 2 * unpackedStride + 1]</code>,
	 * ... as long as indexes are smaller than <code>unpackedEnd</code>.
	 * Returns <code>true</code> on success, <code>false</code> on error.
	 */
	final boolean unpackWords(byte[] unpacked, int unpackedOffset, int unpackedStride, int unpackedEnd)
	{
		for (; unpackedOffset < unpackedEnd; unpackedOffset += unpackedStride) {
			int b = readRle();
			if (b < 0)
				return false;
			unpacked[unpackedOffset] = (byte) b;
			b = readRle();
			if (b < 0)
				return false;
			unpacked[unpackedOffset + 1] = (byte) b;
		}
		return true;
	}

	/**
	 * Uncompresses writing first byte to <code>unpacked[unpackedEnd]</code>, <code>unpacked[unpackedEnd - 1]</code>, ...
	 * until <code>unpacked[unpackedOffset]</code>.
	 * Returns <code>true</code> on success, <code>false</code> on error.
	 */
	final boolean unpackBackwards(byte[] unpacked, int unpackedOffset, int unpackedEnd)
	{
		while (unpackedEnd >= unpackedOffset) {
			int b = readRle();
			if (b < 0)
				return false;
			unpacked[unpackedEnd--] = (byte) b;
		}
		return true;
	}
}
