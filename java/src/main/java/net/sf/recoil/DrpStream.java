// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class DrpStream extends RleStream
{
	int escape;

	protected @Override boolean readCommand()
	{
		int b = readByte();
		if (b == this.escape) {
			this.repeatCount = readByte();
			b = readByte();
		}
		else
			this.repeatCount = 1;
		this.repeatValue = b;
		return b >= 0;
	}

	static byte[] unpackFile(byte[] content, int contentLength, String signature, byte[] unpacked, int unpackedLength)
	{
		if (contentLength > 16 && RECOIL.isStringAt(content, 2, signature)) {
			final DrpStream rle = new DrpStream();
			rle.content = content;
			rle.contentOffset = 16;
			rle.contentLength = contentLength;
			rle.escape = content[15] & 0xff;
			return rle.unpack(unpacked, 2, 1, unpackedLength) ? unpacked : null;
		}
		if (contentLength != unpackedLength)
			return null;
		return content;
	}
}
