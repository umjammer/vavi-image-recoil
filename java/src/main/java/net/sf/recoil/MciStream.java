// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class MciStream extends RleStream
{

	protected @Override boolean readCommand()
	{
		if (this.contentOffset < 2)
			return false;
		int b = this.content[this.contentOffset--] & 0xff;
		for (int e = 8; e >= 0; e--) {
			if (b == (this.content[129 + e] & 0xff)) {
				switch (this.content[120 + e]) {
				case 0:
					this.repeatCount = 2;
					this.repeatValue = this.content[133 + e] & 0xff;
					return true;
				case 7:
					if (this.contentOffset < 2)
						return false;
					this.repeatCount = 2 + (this.content[this.contentOffset--] & 0xff);
					this.repeatValue = this.content[79] & 0xff;
					return true;
				case 11:
					this.repeatCount = 3;
					this.repeatValue = this.content[79] & 0xff;
					return true;
				case 17:
					if (this.contentOffset < 3)
						return false;
					this.repeatCount = 2 + (this.content[this.contentOffset--] & 0xff);
					this.repeatValue = this.content[this.contentOffset--] & 0xff;
					return true;
				case 23:
					if (this.contentOffset < 2)
						return false;
					this.repeatCount = 3;
					this.repeatValue = this.content[this.contentOffset--] & 0xff;
					return true;
				case 25:
					if (this.contentOffset < 2)
						return false;
					b = this.content[this.contentOffset--] & 0xff;
					break;
				default:
					break;
				}
				break;
			}
		}
		this.repeatCount = 1;
		this.repeatValue = b;
		return true;
	}
}
