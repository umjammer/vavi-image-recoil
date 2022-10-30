// Generated automatically with "cito". Do not edit.
package net.sf.recoil;
import java.util.Arrays;

class RastaStream extends Stream
{
	private final RastaRenderer gtia = new RastaRenderer();
	private final byte[] cpuRegisters = new byte[3];

	private boolean endLine()
	{
		for (;;) {
			switch (readByte()) {
			case ' ':
			case '\t':
			case '\r':
				break;
			case '\n':
				return true;
			case ';':
				return skipUntilByte('\n');
			default:
				return false;
			}
		}
	}

	private int readCpuRegister()
	{
		switch (readByte()) {
		case 'a':
			return 0;
		case 'x':
			return 1;
		case 'y':
			return 2;
		default:
			return -1;
		}
	}

	private int parseHexByte()
	{
		return parseInt(16, 255);
	}

	private int readDigit(int offset, int maxValue)
	{
		int digit = readByte() - '0';
		return digit >= 0 && digit <= maxValue ? offset + digit : -1;
	}

	private int readGtiaRegister()
	{
		switch (readByte()) {
		case 'C':
			if (readByte() != 'O' || readByte() != 'L')
				return -1;
			switch (readByte()) {
			case 'B':
				return readByte() == 'A' && readByte() == 'K' ? 26 : -1;
			case 'O':
				return readByte() == 'R' ? readDigit(22, 2) : -1;
			case 'P':
				return readByte() == 'M' ? readDigit(18, 3) : -1;
			default:
				return -1;
			}
		case 'H':
			return expect("POSP") ? readDigit(0, 3) : -1;
		default:
			return -1;
		}
	}

	private static boolean isLetter(int b)
	{
		if (b == '_')
			return true;
		b &= -33;
		return b >= 'A' && b <= 'Z';
	}

	private boolean skipUntilMnemonic()
	{
		for (;;) {
			int b = readByte();
			switch (b) {
			case -1:
				return false;
			case ';':
				if (!skipUntilByte('\n'))
					return false;
				break;
			case '\r':
			case '\n':
				break;
			case '\t':
				return true;
			default:
				if (isLetter(b)) {
					do
						b = readByte();
					while (isLetter(b) || (b >= '0' && b <= '9'));
					if (endLine())
						break;
				}
				return false;
			}
		}
	}
	private int cpuRegisterIndex;
	private int gtiaRegisterIndex;

	private int executeUntilPoke()
	{
		int cpuCycles = 4;
		while (skipUntilMnemonic()) {
			switch (readByte()) {
			case 'l':
				if (readByte() != 'd')
					return -1;
				int reg = readCpuRegister();
				if (reg < 0 || !expect(" #$"))
					return -1;
				int value = parseHexByte();
				if (value < 0 || !endLine())
					return -1;
				this.cpuRegisters[reg] = (byte) value;
				break;
			case 's':
				if (readByte() != 't')
					return -1;
				this.cpuRegisterIndex = readCpuRegister();
				if (this.cpuRegisterIndex < 0 || readByte() != ' ')
					return -1;
				this.gtiaRegisterIndex = readGtiaRegister();
				if (this.gtiaRegisterIndex < 0 || !endLine())
					return -1;
				return cpuCycles;
			case 'n':
				if (readByte() != 'o' || readByte() != 'p' || !endLine())
					return -1;
				break;
			default:
				this.contentOffset--;
				return 0;
			}
			cpuCycles += 2;
		}
		return -1;
	}

	private void poke()
	{
		this.gtia.poke(this.gtiaRegisterIndex, this.cpuRegisters[this.cpuRegisterIndex] & 0xff);
	}

	final boolean readIni()
	{
		Arrays.fill(this.gtia.playerHpos, (byte) 0);
		Arrays.fill(this.gtia.colors, (byte) 0);
		this.contentOffset = 0;
		for (;;) {
			switch (executeUntilPoke()) {
			case -1:
				return false;
			case 0:
				return expect(":2 sta wsync") && endLine();
			default:
				poke();
				break;
			}
		}
	}
	private final byte[] players = new byte[1024];

	final boolean readPmg()
	{
		this.contentOffset = 0;
		if (!skipUntilMnemonic() || !expect(".ds $100") || !endLine())
			return false;
		boolean beginLine = true;
		for (int i = 0; i < 1024; i++) {
			if (beginLine) {
				if (!skipUntilMnemonic() || !expect(".he "))
					return false;
				beginLine = false;
			}
			int b = parseHexByte();
			if (b < 0)
				return false;
			this.players[i] = (byte) b;
			if (readByte() != ' ') {
				this.contentOffset--;
				if (!endLine())
					return false;
				beginLine = true;
			}
		}
		return true;
	}

	private boolean expectCmpZp()
	{
		return expect("cmp byt2") && endLine();
	}

	final boolean readRp(byte[] bitmap, int height, byte[] frame)
	{
		this.contentOffset = 0;
		for (int i = 0; i < 4; i++) {
			if (!skipUntilMnemonic() || !expect("nop") || !endLine())
				return false;
		}
		if (!skipUntilMnemonic() || !expectCmpZp())
			return false;
		Arrays.fill(this.gtia.missileHpos, (byte) 0);
		GtiaRenderer.setSpriteSizes(this.gtia.playerSize, 255);
		GtiaRenderer.setSpriteSizes(this.gtia.missileSize, 0);
		this.gtia.missileGraphics = 0;
		this.gtia.prior = 20;
		this.gtia.content = bitmap;
		this.gtia.playfieldColumns = 40;
		for (int y = 0; y < height; y++) {
			this.gtia.processPlayerDma(this.players, 8 + y);
			this.gtia.startLine(48);
			for (int hpos = 7;;) {
				int cpuCycles = executeUntilPoke();
				if (cpuCycles > 0) {
					int untilHpos = this.gtia.advanceCpuCycles(hpos, cpuCycles, true);
					this.gtia.drawSpan(y, hpos >= 48 ? hpos : 48, untilHpos < 208 ? untilHpos : 208, AnticMode.FOUR_COLOR, frame, 320);
					hpos = untilHpos;
					poke();
					continue;
				}
				if (cpuCycles == 0 && expectCmpZp()) {
					this.gtia.drawSpan(y, hpos >= 48 ? hpos : 48, 208, AnticMode.FOUR_COLOR, frame, 320);
					break;
				}
				return false;
			}
		}
		return true;
	}
}
