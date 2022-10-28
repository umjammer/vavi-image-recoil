// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class IcnParser extends Stream
{

	private boolean skipWhitespaceAndComments()
	{
		boolean got = false;
		while (this.contentOffset < this.contentLength) {
			switch (this.content[this.contentOffset]) {
			case 32:
			case 9:
			case 13:
			case 10:
				this.contentOffset++;
				got = true;
				break;
			case 47:
				if (this.contentOffset >= this.contentLength - 3 || this.content[this.contentOffset + 1] != 42)
					return false;
				this.contentOffset += 3;
				do {
					if (++this.contentOffset > this.contentLength)
						return false;
				}
				while (this.content[this.contentOffset - 2] != 42 || this.content[this.contentOffset - 1] != 47);
				got = true;
				break;
			default:
				return got;
			}
		}
		return true;
	}

	final boolean expectAfterWhitespace(String s)
	{
		return skipWhitespaceAndComments() && expect(s);
	}

	final int parseHex()
	{
		if (!expectAfterWhitespace("0x"))
			return -1;
		return parseInt(16, 65535);
	}

	final int parseDefine(String s)
	{
		if (!expectAfterWhitespace("#define") || !expectAfterWhitespace(s))
			return -1;
		return parseHex();
	}
}
