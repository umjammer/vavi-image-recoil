// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class BlazingPaddlesBoundingBox
{
	int left;
	int top;
	int right;
	int bottom;

	final boolean calculate(byte[] content, int contentLength, int index, int startAddress)
	{
		index <<= 1;
		if (index + 1 >= contentLength)
			return false;
		int contentOffset = (content[index] & 0xff) + ((content[index + 1] & 0xff) << 8) - startAddress;
		if (contentOffset < 0)
			return false;
		this.left = this.top = this.right = this.bottom = 0;
		int x = 0;
		int y = 0;
		while (contentOffset < contentLength) {
			int control = content[contentOffset++] & 0xff;
			if (control == 8)
				return true;
			int len = (control >> 4) + 1;
			switch (control & 3) {
			case 0:
				x += len;
				if (this.right < x)
					this.right = x;
				break;
			case 1:
				x -= len;
				if (this.left > x)
					this.left = x;
				break;
			case 2:
				y -= len;
				if (this.top > y)
					this.top = y;
				break;
			case 3:
				y += len;
				if (this.bottom < y)
					this.bottom = y;
				break;
			default:
				throw new AssertionError();
			}
		}
		return false;
	}
}
