// Generated automatically with "cito". Do not edit.
package net.sf.recoil;

class MppPaletteStream extends BitStream
{

	final int read()
	{
		int rgb;
		switch (this.content[4] & 3) {
		case 0:
			rgb = readBits(9);
			rgb = (rgb & 448) << 10 | (rgb & 56) << 5 | (rgb & 7);
			return rgb << 5 | rgb << 2 | (rgb >> 1 & 197379);
		case 1:
			rgb = readBits(12);
			rgb = (rgb & 1792) << 9 | (rgb & 2160) << 5 | (rgb & 135) << 1 | (rgb & 8) >> 3;
			return rgb << 4 | rgb;
		case 3:
			return RECOIL.getSteInterlacedColor(readBits(15));
		default:
			return 0;
		}
	}
}
