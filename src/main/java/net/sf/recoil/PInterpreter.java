// Generated automatically with "cito". Do not edit.
package net.sf.recoil;
import java.util.Arrays;

class PInterpreter extends Stream
{
	final byte[] screen = new byte[768];
	private int screenOffset;
	private boolean newLineWorks;
	private int bottomOffset;

	private static final int LET_S = 1;

	private static final int LET_D = 2;

	private static final int FOR_F = 4;

	private static final int POKE_D = 8;
	private int bottomCode;

	private int readNumber()
	{
		for (;;) {
			switch (readByte()) {
			case 21:
			case 22:
			case 27:
			case 28:
			case 29:
			case 30:
			case 31:
			case 32:
			case 33:
			case 34:
			case 35:
			case 36:
			case 37:
			case 42:
				break;
			case 126:
				if (this.contentOffset > this.contentLength - 5)
					return -1;
				int exp = readByte();
				int m0 = readByte();
				int m1 = readByte();
				this.contentOffset += 2;
				if (exp > 144 || m0 >= 128)
					return -1;
				if (exp <= 128)
					return 0;
				return ((m0 | 128) << 8 | m1) >> (144 - exp);
			default:
				return -1;
			}
		}
	}

	private int printString(int offset)
	{
		for (;;) {
			if (offset >= this.contentLength)
				return -1;
			int c = this.content[offset++] & 0xff;
			if (c == 11)
				break;
			if (this.screenOffset >= 768)
				return -1;
			if (c == 192)
				c = 11;
			else if ((c & 127) >= 64)
				return -1;
			this.screen[this.screenOffset++] = (byte) c;
			this.newLineWorks = (this.screenOffset & 31) != 0;
		}
		return offset;
	}

	private boolean print()
	{
		for (;;) {
			switch (readByte()) {
			case 11:
				this.contentOffset = printString(this.contentOffset);
				if (this.contentOffset < 0)
					return false;
				break;
			case 193:
				int row = readNumber();
				if (row < 0 || row > 21 || readByte() != 26)
					return false;
				int column = readNumber();
				if (column < 0 || column > 31)
					return false;
				this.screenOffset = row << 5 | column;
				this.newLineWorks = true;
				break;
			case 0:
			case 25:
				break;
			case 118:
				this.contentOffset--;
				if (this.content[this.contentOffset - 1] != 25) {
					if (this.newLineWorks)
						this.screenOffset = (this.screenOffset & -32) + 32;
					this.newLineWorks = true;
				}
				return true;
			default:
				return false;
			}
		}
	}

	private boolean dPeek(int expectedX, int expectedAddress)
	{
		return readByte() == 20 && readNumber() == expectedX && readByte() == 21 && readByte() == 211 && readNumber() == expectedAddress && readByte() == 21 && readNumber() == 256 && readByte() == 23 && readByte() == 211 && readNumber() == expectedAddress + 1;
	}

	private boolean let()
	{
		switch (readByte()) {
		case 38:
			if (readByte() != 13 || readByte() != 20 || readByte() != 11)
				return false;
			this.bottomOffset = this.contentOffset;
			for (;;) {
				switch (readByte()) {
				case -1:
					return false;
				case 11:
					return true;
				default:
					break;
				}
			}
		case 56:
			this.bottomCode |= 1;
			return dPeek(3, 16400);
		case 41:
			this.bottomCode |= 2;
			return dPeek(727, 16396);
		default:
			return false;
		}
	}

	private boolean doIf()
	{
		return readByte() == 198 && readByte() == 38 && readByte() == 13 && readByte() == 221 && readNumber() == 64 && readByte() == 222 && readByte() == 227;
	}

	private boolean doFor()
	{
		this.bottomCode |= 4;
		return readByte() == 43 && readByte() == 20 && readNumber() == 0 && readByte() == 223 && readNumber() == 63;
	}

	private boolean poke()
	{
		this.bottomCode |= 8;
		return readByte() == 41 && readByte() == 21 && readByte() == 43 && readByte() == 21 && readByte() == 16 && readByte() == 43 && readByte() == 18 && readNumber() == 31 && readByte() == 17 && readByte() == 26 && readByte() == 211 && readByte() == 16 && readByte() == 56 && readByte() == 21 && readByte() == 43 && readByte() == 17;
	}

	private boolean next()
	{
		if (readByte() == 43 && this.bottomOffset > 0 && this.bottomCode == 15) {
			this.screenOffset = 704;
			return printString(this.bottomOffset) >= 0;
		}
		return false;
	}

	final boolean run()
	{
		this.contentOffset = 116;
		Arrays.fill(this.screen, (byte) 0);
		this.screenOffset = 0;
		this.newLineWorks = true;
		this.bottomOffset = -1;
		this.bottomCode = 0;
		for (;;) {
			if (this.contentOffset > this.contentLength - 8)
				return false;
			if (readByte() == 118)
				return true;
			this.contentOffset += 3;
			switch (readByte()) {
			case 228:
			case 229:
			case 251:
			case 253:
				break;
			case 227:
			case 236:
			case 242:
				return true;
			case 245:
				if (!print())
					return false;
				break;
			case 241:
				if (!let())
					return false;
				break;
			case 250:
				if (!doIf())
					return false;
				break;
			case 235:
				if (!doFor())
					return false;
				break;
			case 244:
				if (!poke())
					return false;
				break;
			case 243:
				if (!next())
					return false;
				break;
			default:
				return false;
			}
			if (readByte() != 118)
				return false;
		}
	}
}
