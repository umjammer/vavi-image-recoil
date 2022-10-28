// Generated automatically with "cito". Do not edit.
package net.sf.recoil;
import java.util.Arrays;

abstract class GtiaRenderer
{
	final byte[] playerHpos = new byte[4];
	final byte[] missileHpos = new byte[4];
	final byte[] playerSize = new byte[4];
	final byte[] missileSize = new byte[4];
	private final byte[] playerSizeCounter = new byte[4];
	private final byte[] missileSizeCounter = new byte[4];
	final byte[] playerGraphics = new byte[4];
	int missileGraphics;
	private final byte[] playerShiftRegister = new byte[4];
	private int missileShiftRegister;
	final byte[] colors = new byte[9];
	int prior;

	final void setPlayerSize(int i, int size)
	{
		size &= 3;
		this.playerSize[i] = (byte) (size == 2 ? 1 : size + 1);
	}

	static void setSpriteSizes(byte[] sizes, int value)
	{
		for (int i = 0; i < 4; i++) {
			int size = value >> (i << 1) & 3;
			sizes[i] = (byte) (size == 2 ? 1 : size + 1);
		}
	}

	final void poke(int addr, int value)
	{
		switch (addr) {
		case 0:
		case 1:
		case 2:
		case 3:
			this.playerHpos[addr] = (byte) value;
			break;
		case 4:
		case 5:
		case 6:
		case 7:
			this.missileHpos[addr - 4] = (byte) value;
			break;
		case 8:
		case 9:
		case 10:
		case 11:
			setPlayerSize(addr - 8, value);
			break;
		case 12:
			setSpriteSizes(this.missileSize, value);
			break;
		case 13:
		case 14:
		case 15:
		case 16:
			this.playerGraphics[addr - 13] = (byte) value;
			break;
		case 17:
			this.missileGraphics = value;
			break;
		case 18:
		case 19:
		case 20:
		case 21:
		case 22:
		case 23:
		case 24:
		case 25:
		case 26:
			this.colors[addr - 18] = (byte) (value & 254);
			break;
		case 27:
			this.prior = value;
			break;
		default:
			break;
		}
	}

	final void processPlayerDma(byte[] content, int playersOffset)
	{
		for (int i = 0; i < 4; i++)
			this.playerGraphics[i] = (byte) (content[playersOffset + (i << 8)] & 0xff);
	}

	final void processSpriteDma(byte[] content, int missileOffset)
	{
		this.missileGraphics = content[missileOffset] & 0xff;
		processPlayerDma(content, missileOffset + 256);
	}

	private static final int P0 = 1;

	private static final int P1 = 2;

	private static final int P01 = 3;

	private static final int P2 = 4;

	private static final int P3 = 8;

	private static final int P23 = 12;

	private static final int P_F0 = 16;

	private static final int P_F1 = 32;

	private static final int P_F01 = 48;

	private static final int P_F2 = 64;

	private static final int P_F3 = 128;

	private static final int P_F23 = 192;

	private int getPmg(int hpos, int objects)
	{
		for (int i = 0; i < 4; i++) {
			if ((this.playerHpos[i] & 0xff) == hpos) {
				this.playerShiftRegister[i] |= this.playerGraphics[i] & 0xff;
				this.playerSizeCounter[i] = (byte) (this.playerSize[i] & 0xff);
			}
			if ((this.missileHpos[i] & 0xff) == hpos) {
				this.missileShiftRegister |= this.missileGraphics & 3 << (i << 1);
				this.missileSizeCounter[i] = (byte) (this.missileSize[i] & 0xff);
			}
		}
		if ((this.prior & 16) != 0 && (this.missileShiftRegister & 170) != 0)
			objects |= 128;
		for (int i = 0; i < 4; i++) {
			if ((this.playerShiftRegister[i] & 128) != 0 || ((this.prior & 16) == 0 && (this.missileShiftRegister & 2 << (i << 1)) != 0))
				objects |= 1 << i;
			if ((--this.playerSizeCounter[i] & 0xff) == 0) {
				this.playerShiftRegister[i] = (byte) ((this.playerShiftRegister[i] & 0xff) << 1);
				this.playerSizeCounter[i] = (byte) (this.playerSize[i] & 0xff);
			}
			if ((--this.missileSizeCounter[i] & 0xff) == 0) {
				int mask = 1 << (i << 1);
				this.missileShiftRegister = (this.missileShiftRegister & ~(mask * 3)) | (this.missileShiftRegister & mask) << 1;
				this.missileSizeCounter[i] = (byte) (this.missileSize[i] & 0xff);
			}
		}
		return objects;
	}

	private int getColor(int objects)
	{
		if (objects == 0)
			return this.colors[8] & 0xff;
		int prior = this.prior;
		int color = 0;
		if ((objects & 3) != 0) {
			if (((objects & 48) == 0 || (prior & 12) == 0) && ((objects & 192) == 0 || (prior & 4) == 0)) {
				if ((objects & 1) != 0) {
					color = this.colors[0] & 0xff;
					if ((objects & 2) != 0 && (prior & 32) != 0)
						color |= this.colors[1] & 0xff;
				}
				else
					color = this.colors[1] & 0xff;
			}
		}
		else if ((objects & 12) != 0) {
			if (((objects & 192) == 0 || (prior & 6) == 0) && ((objects & 48) == 0 || (prior & 1) != 0)) {
				if ((objects & 4) != 0) {
					color = this.colors[2] & 0xff;
					if ((objects & 8) != 0 && (prior & 32) != 0)
						color |= this.colors[3] & 0xff;
				}
				else
					color = this.colors[3] & 0xff;
			}
		}
		if ((objects & 192) != 0 && ((objects & 12) == 0 || (prior & 9) == 0) && ((objects & 3) == 0 || (prior & 4) != 0)) {
			return color | this.colors[(objects & 128) != 0 ? 7 : 6] & 0xff;
		}
		if ((objects & 48) != 0 && ((objects & 12) == 0 || (prior & 1) == 0) && ((objects & 3) == 0 || (prior & 3) == 0)) {
			return color | this.colors[(objects & 16) != 0 ? 4 : 5] & 0xff;
		}
		return color;
	}

	final void startLine(int startHpos)
	{
		Arrays.fill(this.playerShiftRegister, (byte) 0);
		this.missileShiftRegister = 0;
		for (int hpos = startHpos - 31; hpos < startHpos; hpos++)
			getPmg(hpos, 0);
	}
	byte[] content;
	int playfieldColumns;

	protected int getHiresColor(int c)
	{
		return (c & 240) + (this.colors[5] & 14);
	}

	/**
	 * Fetches playfield data at the given location.
	 * Returns playfield byte, plus bit 8 for character code bit 7 ("inverse").
	 */
	protected abstract int getPlayfieldByte(int y, int column);

	final int drawSpan(int y, int hpos, int untilHpos, int anticMode, byte[] frame, int width, int yOffset)
	{
		int gtiaMode = this.prior >> 6;
		for (; hpos < untilHpos; hpos++) {
			int x = hpos;
			int objects = 0;
			int playfield = 0;
			if (gtiaMode == 2) {
				x--;
				objects = 1;
			}
			if (anticMode != AnticMode.BLANK) {
				int column = (x >> 2) + (this.playfieldColumns >> 1) - 32;
				if (column >= 0 && column < this.playfieldColumns) {
					playfield = getPlayfieldByte(y, column);
					boolean inverseChar = playfield >= 256;
					if (inverseChar && anticMode == AnticMode.HI_RES)
						playfield = 511 - playfield;
					if (gtiaMode == 0) {
						playfield = playfield >> ((~x & 3) << 1) & 3;
						objects = anticMode == AnticMode.HI_RES ? 64 : anticMode == AnticMode.FIVE_COLOR && playfield == 3 && inverseChar ? 128 : 8 << playfield & 112;
					}
					else {
						if ((x & 2) == 0)
							playfield >>= 4;
						playfield &= 15;
						if (gtiaMode == 2) {
							objects = DRAW_SPAN_GTIA10_OBJECTS[playfield] & 0xff;
						}
					}
				}
			}
			objects = getPmg(hpos, objects);
			int c = getColor(objects);
			int frameOffset = (yOffset + y) * width + ((hpos + (width >> 2) - 128) << 1);
			switch (gtiaMode) {
			case 0:
				if (anticMode != AnticMode.HI_RES)
					break;
				frame[frameOffset] = (byte) ((playfield & 2) == 0 ? c : getHiresColor(c));
				frame[frameOffset + 1] = (byte) ((playfield & 1) == 0 ? c : getHiresColor(c));
				continue;
			case 2:
				break;
			default:
				if ((objects & 15) != 0)
					break;
				assert objects == 0 || objects == 128;
				if (gtiaMode == 1)
					c |= playfield;
				else if (playfield == 0)
					c &= 240;
				else
					c |= playfield << 4;
				break;
			}
			frame[frameOffset + 1] = frame[frameOffset] = (byte) c;
		}
		return hpos;
	}

	final int drawSpan(int y, int hpos, int untilHpos, int anticMode, byte[] frame, int width)
	{
		return drawSpan(y, hpos, untilHpos, anticMode, frame, width, 0);
	}

	final void setG2fColors(int contentOffset, int contentStride, int count, int gtiaMode)
	{
		for (int i = 0; i < count; i++)
			this.colors[(gtiaMode & 192) == 128 ? i : SET_G2F_COLORS_NORMAL_REGISTERS[i] & 0xff] = (byte) (this.content[contentOffset + i * contentStride] & 254);
	}

	final int advanceCpuCycles(int hpos, int cpuCycles, boolean nonBlank)
	{
		for (;;) {
			hpos += 2;
			int x = (hpos - 118) >> 1;
			if ((x & 1) != 0 ? nonBlank && x >= -this.playfieldColumns && x < this.playfieldColumns : x >= -36 && x < 0 && (x & 2) != 0) {
			}
			else if (--cpuCycles == 0)
				return hpos;
		}
	}

	private static final byte[] DRAW_SPAN_GTIA10_OBJECTS = { 1, 2, 4, 8, 16, 32, 64, -128, 0, 0, 0, 0, 16, 32, 64, -128 };

	private static final byte[] SET_G2F_COLORS_NORMAL_REGISTERS = { 8, 4, 5, 6, 7, 0, 1, 2, 3 };
}
