// Generated automatically with "cito". Do not edit.
package net.sf.recoil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;

import vavi.util.Debug;


/**
 * Decoder of images in formats native to vintage computers.
 * Decodes file contents passed as a byte array
 * into a 24-bit RGB bitmap.
 */
public class RECOIL
{
	/**
	 * Constructs a decoder of images.
	 * The decoder can be used for several images, one after another.
	 */
	public RECOIL()
	{
		setNtsc(false);
	}

	/**
	 * RECOIL version - major part.
	 */
	public static final int VERSION_MAJOR = 6;

	/**
	 * RECOIL version - minor part.
	 */
	public static final int VERSION_MINOR = 3;

	/**
	 * RECOIL version - micro part.
	 */
	public static final int VERSION_MICRO = 1;

	/**
	 * RECOIL version as a string.
	 */
	public static final String VERSION = "6.3.1";

	/**
	 * Years RECOIL was created in.
	 */
	public static final String YEARS = "2009-2022";

	/**
	 * Short credits for RECOIL.
	 */
	public static final String CREDITS = "Retro Computer Image Library (C) 2009-2022 Piotr Fusik\n";

	/**
	 * Short license notice.
	 * Display after the credits.
	 */
	public static final String COPYRIGHT = "This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.";

	/**
	 * Maximum length of a supported platform palette file.
	 */
	public static final int MAX_PLATFORM_PALETTE_CONTENT_LENGTH = 32768;

	/**
	 * Maximum number of pixels in a decoded image.
	 */
	private static final int MAX_PIXELS_LENGTH = 134217728;
	/**
	 * Decoded image width.
	 */
	private int width;
	/**
	 * Decoded image height.
	 */
	private int height;
	/**
	 * Decoded image pixels as 0xRRGGBB.
	 */
	private int[] pixels;
	/**
	 * Length of the currently allocated <code>Pixels</code>.
	 */
	private int pixelsLength = 0;
	/**
	 * Platform and pixel ratio.
	 */
	private int resolution;
	/**
	 * Number of frames (normally 1; 2 or 3 for flickering pictures).
	 */
	private int frames;
	/**
	 * <code>true</code> if NTSC is preferred over PAL.
	 */
	private boolean ntsc;
	private final int[] c64Palette = new int[16];
	private final int[] c16Palette = new int[128];
	private final int[] atari8Palette = new int[256];
	private int leftSkip;

	/**
	 * Maximum length of a string returned by <code>GetPlatform()</code>.
	 */
	public static final int MAX_PLATFORM_LENGTH = 22;

	/**
	 * Selects the PAL/NTSC video standard for applicable platforms.
	 * Resets all platform palettes loaded with <code>SetPlatformPalette</code> to default.
	 * @param ntsc <code>true</code> for NTSC, <code>false</code> for PAL
	 */
	public final void setNtsc(boolean ntsc)
	{
		this.ntsc = ntsc;
		this.c64Palette[0] = 0;
		this.c64Palette[1] = 16777215;
		this.c64Palette[2] = 6829867;
		this.c64Palette[3] = 7382194;
		this.c64Palette[4] = 7290246;
		this.c64Palette[5] = 5803331;
		this.c64Palette[6] = 3483769;
		this.c64Palette[7] = 12109679;
		this.c64Palette[8] = 7294757;
		this.c64Palette[9] = 4405504;
		this.c64Palette[10] = 10119001;
		this.c64Palette[11] = 4473924;
		this.c64Palette[12] = 7105644;
		this.c64Palette[13] = 10146436;
		this.c64Palette[14] = 7102133;
		this.c64Palette[15] = 9803157;
		decodeR8G8B8Colors(CiResource.getByteArray("c16.pal", 384), 0, 128, this.c16Palette, 0);
		decodeR8G8B8Colors(this.ntsc ? CiResource.getByteArray("altirrantsc.pal", 768) : CiResource.getByteArray("altirrapal.pal", 768), 0, 256, this.atari8Palette, 0);
	}

	/**
	 * Returns <code>true</code> if NTSC video standard is selected.
	 */
	public final boolean isNtsc()
	{
		return this.ntsc;
	}

	private static int packExt(String ext)
	{
		return ext.length() == 0 || ext.length() > 4 ? 0 : (ext.charAt(0) + (ext.length() >= 2 ? ext.charAt(1) << 8 : 0) + (ext.length() >= 3 ? ext.charAt(2) << 16 : 0) + (ext.length() >= 4 ? ext.charAt(3) << 24 : 0)) | 538976288;
	}

	private static int getPackedExt(String filename)
	{
		int ext = 0;
		for (int i = filename.length(); --i >= 0;) {
			int c = Character.toLowerCase(filename.charAt(i));
			if (c == '.') {
Debug.printf(Level.FINER, "%08x", ext | 0x20202020);
				return ext | 0x20202020;
			}
			if (c <= ' ' || c > 'z' || ext >= 0x1000000)
				return 0;
			ext = (ext << 8) + c;
		}
		return 0;
	}

	/**
	 * Sets a custom platform palette.
	 * 
	 * <ul>
	 * <li>768-byte <code>ACT</code>/<code>PAL</code> file with an Atari 8-bit palette</li>
	 * <li><code>VPL</code> VICE Palette file with a C64 or C16 palette</li>
	 * </ul>
	 * <p>Returns <code>true</code> on success.
	 * @param filename Name of the file to decode. Only the extension is processed, for format recognition.
	 * @param content File contents.
	 * @param contentLength File length.
	 */
	public final boolean setPlatformPalette(String filename, byte[] content, int contentLength)
	{
		switch (getPackedExt(filename)) {
		case 544498529:
		case 543973744:
			if (contentLength != 768)
				return false;
			decodeR8G8B8Colors(content, 0, 256, this.atari8Palette, 0);
			return true;
		case 543977590:
			final VplStream vpl = new VplStream();
			vpl.content = content;
			vpl.contentOffset = 0;
			vpl.contentLength = contentLength;
			switch (vpl.decode()) {
			case 16:
				vpl.copyTo(this.c64Palette, 16);
				return true;
			case 128:
				vpl.copyTo(this.c16Palette, 128);
				return true;
			default:
				return false;
			}
		default:
			return false;
		}
	}

	/**
	 * Initializes decoded image size and resolution.
	 */
	private boolean setSize(int width, int height, int resolution, int frames)
	{
		if (width <= 0 || height <= 0 || height > 134217728 / width / frames)
			return false;
		this.width = width;
		this.height = height;
		this.resolution = resolution;
		this.frames = frames;
		this.colors = -1;
		this.leftSkip = 0;
		int pixelsLength = width * height * frames;
		if (this.pixelsLength < pixelsLength) {
			this.pixels = null;
			this.pixels = new int[pixelsLength];
			this.pixelsLength = pixelsLength;
		}
		return true;
	}

	/**
	 * Initializes decoded image size and resolution.
	 */
	private boolean setSize(int width, int height, int resolution)
	{
		return setSize(width, height, resolution, 1);
	}

	private boolean setSizeStOrFalcon(int width, int height, int bitplanes, boolean squarePixels)
	{
		int resolution = RECOILResolution.FALCON1X1;
		switch (bitplanes) {
		case 1:
			if (width <= 640 && height <= 400)
				resolution = RECOILResolution.ST1X1;
			break;
		case 2:
			if (!squarePixels && width == 640 && height == 200) {
				height <<= 1;
				resolution = RECOILResolution.STE1X2;
			}
			break;
		case 4:
			if (width <= 320 && height <= 200)
				resolution = RECOILResolution.STE1X1;
			break;
		case 8:
			if (!squarePixels && width == 320 && height == 480) {
				width <<= 1;
				resolution = RECOILResolution.TT2X1;
			}
			break;
		default:
			break;
		}
		return setSize(width, height, resolution);
	}

	private boolean setScaledSize(int width, int height, int resolution)
	{
		switch (resolution) {
		case RECOILResolution.AMIGA2X1:
		case RECOILResolution.FALCON2X1:
		case RECOILResolution.MSX22X1I:
		case RECOILResolution.MSX2_PLUS2X1I:
			width <<= 1;
			break;
		case RECOILResolution.AMIGA4X1:
			width <<= 2;
			break;
		case RECOILResolution.AMIGA8X1:
			width <<= 3;
			break;
		case RECOILResolution.AMIGA1X2:
		case RECOILResolution.AMIGA_DCTV1X2:
		case RECOILResolution.ST1X2:
		case RECOILResolution.STE1X2:
		case RECOILResolution.MSX21X2:
		case RECOILResolution.PC801X2:
		case RECOILResolution.PC881X2:
			height <<= 1;
			break;
		case RECOILResolution.AMIGA1X4:
			height <<= 2;
			break;
		default:
			break;
		}
		return setSize(width, height, resolution);
	}

	private void setScaledPixel(int x, int y, int rgb)
	{
		int offset = y * this.width;
		switch (this.resolution) {
		case RECOILResolution.AMIGA2X1:
		case RECOILResolution.AMIGA_HAME2X1:
		case RECOILResolution.AMSTRAD2X1:
		case RECOILResolution.TT2X1:
		case RECOILResolution.FALCON2X1:
		case RECOILResolution.BBC2X1:
		case RECOILResolution.MSX22X1I:
		case RECOILResolution.MSX2_PLUS2X1I:
			offset += x << 1;
			this.pixels[offset + 1] = this.pixels[offset] = rgb;
			break;
		case RECOILResolution.AMIGA4X1:
			offset += x << 2;
			this.pixels[offset + 3] = this.pixels[offset + 2] = this.pixels[offset + 1] = this.pixels[offset] = rgb;
			break;
		case RECOILResolution.AMIGA8X1:
			offset += x << 3;
			for (x = 0; x < 8; x++)
				this.pixels[offset + x] = rgb;
			break;
		case RECOILResolution.AMIGA1X2:
		case RECOILResolution.AMIGA_DCTV1X2:
		case RECOILResolution.APPLE_I_I_G_S1X2:
		case RECOILResolution.ST1X2:
		case RECOILResolution.STE1X2:
		case RECOILResolution.MC05151X2:
		case RECOILResolution.PC801X2:
		case RECOILResolution.PC881X2:
		case RECOILResolution.MSX21X2:
		case RECOILResolution.SAM_COUPE1X2:
		case RECOILResolution.TRS1X2:
			offset = (offset << 1) + x;
			this.pixels[offset + this.width] = this.pixels[offset] = rgb;
			break;
		case RECOILResolution.AMIGA1X4:
			offset = (offset << 2) + x;
			this.pixels[offset + this.width * 3] = this.pixels[offset + this.width * 2] = this.pixels[offset + this.width] = this.pixels[offset] = rgb;
			break;
		default:
			this.pixels[offset + x] = rgb;
			break;
		}
	}

	/**
	 * Reads a 32-bit big endian integer from a byte array.
	 */
	static int get32BigEndian(byte[] content, int contentOffset)
	{
		return (content[contentOffset] & 0xff) << 24 | (content[contentOffset + 1] & 0xff) << 16 | (content[contentOffset + 2] & 0xff) << 8 | content[contentOffset + 3] & 0xff;
	}

	/**
	 * Reads a 32-bit little endian integer from a byte array.
	 */
	static int get32LittleEndian(byte[] content, int contentOffset)
	{
		return content[contentOffset] & 0xff | (content[contentOffset + 1] & 0xff) << 8 | (content[contentOffset + 2] & 0xff) << 16 | (content[contentOffset + 3] & 0xff) << 24;
	}

	static int getNibble(byte[] content, int contentOffset, int index)
	{
		int b = content[contentOffset + (index >> 1)] & 0xff;
		return (index & 1) == 0 ? b >> 4 : b & 15;
	}

	static boolean isStringAt(byte[] content, int contentOffset, String s)
	{
		for (int _i = 0; _i < s.length(); _i++) {
			int c = s.charAt(_i);
			if ((content[contentOffset++] & 0xff) != c)
				return false;
		}
		return true;
	}

	static boolean copyPrevious(byte[] unpacked, int unpackedOffset, int distance, int count)
	{
		if (distance > unpackedOffset)
			return false;
		do {
			unpacked[unpackedOffset] = (byte) (unpacked[unpackedOffset - distance] & 0xff);
			unpackedOffset++;
		}
		while (--count > 0);
		return true;
	}

	private boolean applyBlend()
	{
		int pixelsLength = this.width * this.height;
		for (int i = 0; i < pixelsLength; i++) {
			int rgb1 = this.pixels[i];
			int rgb2 = this.pixels[pixelsLength + i];
			this.pixels[i] = (rgb1 & rgb2) + ((rgb1 ^ rgb2) >> 1 & 8355711);
		}
		return true;
	}

	/**
	 * Reads a companion file to the specified byte array.
	 * Implement this method in a subclass to enable support for multi-file images.
	 * Returns the number of bytes read (up to <code>contentLength</code>) or -1 on error.
	 * @param filename Name of the file to read.
	 * @param content Out: target for the file contents.
	 * @param contentLength Maximum number of bytes to read.
	 */
	protected int readFile(String filename, byte[] content, int contentLength)
	{
		return -1;
	}

	private int readCompanionFile(String baseFilename, String upperExt, String lowerExt, byte[] content, int contentLength)
	{
		int i = baseFilename.length();
		boolean lower = false;
		for (;;) {
			int c = baseFilename.charAt(--i);
			if (c >= 'a')
				lower = true;
			else if (c == '.')
				break;
		}
		String filename = baseFilename.substring(0, i + 1);
		filename += lower ? lowerExt : upperExt;
		return readFile(filename, content, contentLength);
	}

	private int readSiblingFile(String baseFilename, String newFilename, byte[] content, int contentLength)
	{
		int i = baseFilename.length();
		while (i > 0 && baseFilename.charAt(i - 1) != '/' && baseFilename.charAt(i - 1) != '\\')
			i--;
		String filename = baseFilename.substring(0, i);
		filename += newFilename;
		return readFile(filename, content, contentLength);
	}

	private boolean decodeBru(byte[] content, int contentLength)
	{
		if (contentLength != 64)
			return false;
		setSize(8, 8, RECOILResolution.ST1X1);
		for (int i = 0; i < 64; i++) {
			switch (content[i]) {
			case 0:
				this.pixels[i] = 0;
				break;
			case 1:
				this.pixels[i] = 16777215;
				break;
			default:
				return false;
			}
		}
		return true;
	}
	/**
	 * RGB palette decoded from the image file.
	 */
	final int[] contentPalette = new int[256];

	private void decodeBytes(byte[] content, int contentOffset)
	{
		int width = getOriginalWidth();
		int height = getOriginalHeight();
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				setScaledPixel(x, y, this.contentPalette[content[contentOffset + y * width + x] & 0xff]);
	}

	private void decodeNibbles(byte[] content, int contentOffset, int contentStride)
	{
		int width = getOriginalWidth();
		int height = getOriginalHeight();
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				setScaledPixel(x, y, this.contentPalette[getNibble(content, contentOffset + y * contentStride, x)]);
	}

	private static int getR8G8B8Color(byte[] content, int contentOffset)
	{
		return (content[contentOffset] & 0xff) << 16 | (content[contentOffset + 1] & 0xff) << 8 | content[contentOffset + 2] & 0xff;
	}

	private static void decodeR8G8B8Colors(byte[] content, int contentOffset, int count, int[] destination, int destinationOffset)
	{
		for (int i = 0; i < count; i++)
			destination[destinationOffset + i] = getR8G8B8Color(content, contentOffset + i * 3);
	}

	private static int getB5G5R5Color(int c)
	{
		c = (c & 31) << 19 | (c & 992) << 6 | (c >> 7 & 248);
		return c | (c >> 5 & 460551);
	}

	private static int getR5G5B5Color(int c)
	{
		c = (c & 31744) << 9 | (c & 992) << 6 | (c & 31) << 3;
		return c | (c >> 5 & 460551);
	}

	private static int getG6R5B5Color(int c)
	{
		c = (c & 992) << 14 | (c & 64512) | (c & 31) << 3;
		return c | (c >> 5 & 458759) | (c >> 6 & 768);
	}

	private static int get729Color(int c)
	{
		int r = c / 81;
		int g = c / 9 % 9;
		int b = c % 9;
		return r * 255 >> 3 << 16 | g * 255 >> 3 << 8 | b * 255 >> 3;
	}

	static int getFalconTrueColor(byte[] content, int contentOffset)
	{
		int rg = content[contentOffset] & 0xff;
		int gb = content[contentOffset + 1] & 0xff;
		int rgb = (rg & 248) << 16 | (rg & 7) << 13 | (gb & 224) << 5 | (gb & 31) << 3;
		rgb |= (rgb >> 5 & 458759) | (rgb >> 6 & 768);
		return rgb;
	}

	private static int getBitplanePixel(byte[] content, int contentOffset, int x, int bitplanes, int bytesPerBitplane)
	{
		int bit = ~x & 7;
		int c = 0;
		for (int bitplane = bitplanes; --bitplane >= 0;)
			c = c << 1 | ((content[contentOffset + bitplane * bytesPerBitplane] & 0xff) >> bit & 1);
		return c;
	}

	private boolean decodeAmigaPlanar(byte[] content, int contentOffset, int width, int height, int resolution, int bitplanes, int[] palette)
	{
		if (!setScaledSize(width, height, resolution))
			return false;
		int bytesPerLine = (width + 15) >> 4 << 1;
		int bitplaneLength = height * bytesPerLine;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int c = getBitplanePixel(content, contentOffset + y * bytesPerLine + (x >> 3), x, bitplanes, bitplaneLength);
				setScaledPixel(x, y, palette[c]);
			}
		}
		return true;
	}

	private static int getBitplaneWordsPixel(byte[] content, int contentOffset, int x, int bitplanes)
	{
		return getBitplanePixel(content, contentOffset + (x >> 3 & -2) * bitplanes + (x >> 3 & 1), x, bitplanes, 2);
	}

	/**
	 * Decodes Atari ST/Falcon interleaved bitplanes.
	 * Each 16 pixels are encoded in N consecutive 16-bit words.
	 */
	private void decodeBitplanes(byte[] content, int contentOffset, int contentStride, int bitplanes, int pixelsOffset, int width, int height)
	{
		while (--height >= 0) {
			for (int x = 0; x < width; x++) {
				int c = getBitplaneWordsPixel(content, contentOffset, x, bitplanes);
				this.pixels[pixelsOffset + x] = this.contentPalette[c];
			}
			contentOffset += contentStride;
			pixelsOffset += this.width;
		}
	}

	private void decodeScaledBitplanes(byte[] content, int contentOffset, int width, int height, int bitplanes, boolean ehb, MultiPalette multiPalette)
	{
		int contentStride = ((width + 15) >> 4 << 1) * bitplanes;
		for (int y = 0; y < height; y++) {
			if (multiPalette != null)
				multiPalette.setLinePalette(this, y);
			if (ehb) {
				for (int c = 0; c < 32; c++)
					this.contentPalette[32 + c] = this.contentPalette[c] >> 1 & 8355711;
			}
			for (int x = 0; x < width; x++) {
				int c = getBitplaneWordsPixel(content, contentOffset, x, bitplanes);
				setScaledPixel(x, y, this.contentPalette[c]);
			}
			contentOffset += contentStride;
		}
	}

	private boolean decodeMono(byte[] content, int contentOffset, int contentLength, boolean wordAlign)
	{
		int contentStride = (this.width + 7) >> 3;
		if (wordAlign)
			contentStride += contentStride & 1;
		if (contentLength != contentOffset + contentStride * this.height)
			return false;
		decodeBitplanes(content, contentOffset, contentStride, 1, 0, this.width, this.height);
		return true;
	}

	private boolean decodeBlackAndWhite(byte[] content, int contentOffset, int contentLength, boolean wordAlign, int backgroundColor)
	{
		this.contentPalette[0] = backgroundColor;
		this.contentPalette[1] = backgroundColor ^ 16777215;
		return decodeMono(content, contentOffset, contentLength, wordAlign);
	}

	private boolean decodeRleBlackAndWhite(RleStream rle, int backgroundColor)
	{
		int width = getOriginalWidth();
		int height = getOriginalHeight();
		for (int y = 0; y < height; y++) {
			int b = 0;
			for (int x = 0; x < width; x++) {
				if ((x & 7) == 0) {
					b = rle.readRle();
					if (b < 0)
						return false;
				}
				setScaledPixel(x, y, (b >> (~x & 7) & 1) == 0 ? backgroundColor : backgroundColor ^ 16777215);
			}
		}
		return true;
	}

	private void decodeBlackAndWhiteFont(byte[] content, int contentOffset, int contentLength, int fontHeight)
	{
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < 256; x++) {
				int row = y % fontHeight;
				int offset = contentOffset + ((y - row) << 5) + (x >> 3) * fontHeight + row;
				int c;
				if (offset < contentLength) {
					c = (content[offset] & 0xff) >> (~x & 7) & 1;
					if (c != 0)
						c = 16777215;
				}
				else
					c = 0;
				this.pixels[(y << 8) + x] = c;
			}
		}
	}

	private boolean decodePgf(byte[] content, int contentLength)
	{
		setSize(240, 64, RECOILResolution.PORTFOLIO1X1);
		return decodeBlackAndWhite(content, 0, contentLength, false, 16777215);
	}

	private boolean decodePgc(byte[] content, int contentLength)
	{
		if (contentLength < 33 || content[0] != 80 || content[1] != 71 || content[2] != 1)
			return false;
		setSize(240, 64, RECOILResolution.PORTFOLIO1X1);
		final PgcStream rle = new PgcStream();
		rle.content = content;
		rle.contentOffset = 3;
		rle.contentLength = contentLength;
		return decodeRleBlackAndWhite(rle, 16777215);
	}

	private boolean decodePsion3Pic(byte[] content, int contentLength)
	{
		if (contentLength < 22 || content[0] != 80 || content[1] != 73 || content[2] != 67 || content[3] != -36 || content[4] != 48 || content[5] != 48 || (content[6] == 0 && content[7] == 0))
			return false;
		int width = content[10] & 0xff | (content[11] & 0xff) << 8;
		int height = content[12] & 0xff | (content[13] & 0xff) << 8;
		int bitmapLength = content[14] & 0xff | (content[15] & 0xff) << 8;
		int stride = (width + 15) >> 4 << 1;
		if (bitmapLength != height * stride)
			return false;
		int bitmapOffset = 20 + RECOIL.get32LittleEndian(content, 16);
		if (bitmapOffset < 20 || contentLength < bitmapOffset + bitmapLength)
			return false;
		if (!setSize(width, height, RECOILResolution.PSION31X1))
			return false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				this.pixels[y * width + x] = ((content[bitmapOffset + (x >> 3)] & 0xff) >> (x & 7) & 1) == 0 ? 16777215 : 0;
			}
			bitmapOffset += stride;
		}
		return true;
	}

	private boolean decodeGrb(byte[] content, int contentLength)
	{
		if (contentLength < 19)
			return false;
		if (isStringAt(content, 0, "HPHP48-") && content[8] == 30 && content[9] == 43 && (content[10] & 15) == 0) {
			int nibbles = (content[10] & 0xff) >> 4 | (content[11] & 0xff) << 4 | (content[12] & 0xff) << 12;
			int height = content[13] & 0xff | (content[14] & 0xff) << 8 | (content[15] & 15) << 16;
			int width = (content[15] & 0xff) >> 4 | (content[16] & 0xff) << 4 | (content[17] & 0xff) << 12;
			int bytesPerLine = (width + 7) >> 3;
			if (nibbles != (contentLength << 1) - 21 || contentLength != 18 + height * bytesPerLine || !setSize(width, height, RECOILResolution.HP481X1))
				return false;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					this.pixels[y * width + x] = ((content[18 + y * bytesPerLine + (x >> 3)] & 0xff) >> (x & 7) & 1) == 0 ? 16777215 : 0;
			}
			return true;
		}
		if (contentLength >= 31 && isStringAt(content, 0, "%%HP: T(0)A(D)F(.);\rGROB ")) {
			final Stream s = new Stream();
			s.content = content;
			s.contentOffset = 25;
			s.contentLength = contentLength;
			int width = s.parseInt(10, 65535);
			if (width <= 0 || s.readByte() != ' ')
				return false;
			int height = s.parseInt(10, 65535);
			if (height <= 0 || s.readByte() != '\r' || !setSize(width, height, RECOILResolution.HP481X1))
				return false;
			int b = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int bit = x & 7;
					if (bit == 0) {
						b = s.readHexByte();
						if (b < 0)
							return false;
					}
					this.pixels[y * width + x] = (b >> (bit ^ 4) & 1) == 0 ? 16777215 : 0;
				}
			}
			return true;
		}
		return false;
	}

	private boolean decodeTrsHr(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 19200:
		case 19328:
		case 19456:
			break;
		default:
			return false;
		}
		setSize(640, 480, RECOILResolution.TRS1X2);
		for (int y = 0; y < 240; y++) {
			for (int x = 0; x < 640; x++) {
				int c = (content[y * 80 + (x >> 3)] & 0xff) >> (~x & 7) & 1;
				if (c != 0)
					c = 16777215;
				int pixelsOffset = y * 1280 + x;
				this.pixels[pixelsOffset + 640] = this.pixels[pixelsOffset] = c;
			}
		}
		return true;
	}

	private boolean decodeTrsShr(byte[] content, int contentLength)
	{
		setSize(640, 480, RECOILResolution.TRS1X2);
		final PgcStream rle = new PgcStream();
		rle.content = content;
		rle.contentOffset = 0;
		rle.contentLength = contentLength;
		return decodeRleBlackAndWhite(rle, 0);
	}

	private boolean decodeRle(byte[] content, int contentLength)
	{
		if (contentLength < 520 || content[0] != 27 || content[1] != 71 || content[2] != 72)
			return false;
		setSize(256, 192, RECOILResolution.TRS1X1);
		int count = 0;
		int contentOffset = 3;
		int c = 16777215;
		for (int pixelsOffset = 0; pixelsOffset < 49152; pixelsOffset++) {
			while (count == 0) {
				if (contentOffset >= contentLength)
					return false;
				count = content[contentOffset++] & 0xff;
				if (count < 32 || count > 127) {
					if (pixelsOffset == 49151) {
						this.pixels[pixelsOffset] = c;
						return true;
					}
					return false;
				}
				c ^= 16777215;
				count -= 32;
			}
			this.pixels[pixelsOffset] = c;
			count--;
		}
		return true;
	}

	private boolean decodeCocoClp(byte[] content, int contentLength)
	{
		if (contentLength != 306 || content[305] != 100)
			return false;
		for (int i = 0; i < 25; i++) {
			if ((content[i] & 0xff) != (DECODE_COCO_CLP_HEADER[i] & 0xff))
				return false;
		}
		setSize(40, 56, RECOILResolution.COCO1X1);
		return decodeBlackAndWhite(content, 25, 305, false, 16777215);
	}

	private boolean decodeCocoMax(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 6154:
		case 6155:
		case 6272:
		case 7168:
			break;
		default:
			return false;
		}
		if (content[0] != 0 || content[1] != 24 || (content[2] & 0xff) > 1 || content[3] != 14 || content[4] != 0)
			return false;
		setSize(256, 192, RECOILResolution.COCO1X1);
		return decodeBlackAndWhite(content, 5, 6149, false, 0);
	}

	private boolean decodeP11(byte[] content, int contentLength)
	{
		if ((contentLength != 3083 && contentLength != 3243) || content[0] != 0 || content[1] != 12 || content[3] != 14 || content[4] != 0)
			return false;
		setSize(256, 192, RECOILResolution.COCO2X2);
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x++) {
				int c = (content[5 + ((y & -2) << 4) + (x >> 3)] & 0xff) >> (~x & 6) & 3;
				this.pixels[(y << 8) + x] = DECODE_P11_PALETTE[c];
			}
		}
		return true;
	}

	private boolean decodeMac(byte[] content, int contentLength)
	{
		if (contentLength < 512)
			return false;
		int contentOffset = isStringAt(content, 65, "PNTG") ? 128 : 0;
		if (content[contentOffset] != 0 || content[contentOffset + 1] != 0 || content[contentOffset + 2] != 0 || (content[contentOffset + 3] & 0xff) > 3)
			return false;
		setSize(576, 720, RECOILResolution.MACINTOSH1X1);
		final PackBitsStream rle = new PackBitsStream();
		rle.content = content;
		rle.contentOffset = contentOffset + 512;
		rle.contentLength = contentLength;
		return decodeRleBlackAndWhite(rle, 16777215);
	}

	private static void decodePlayStation(byte[] content, int contentOffset, int[] pixels, int pixelsLength)
	{
		for (int i = 0; i < pixelsLength; i++)
			pixels[i] = getB5G5R5Color(content[contentOffset + (i << 1)] & 0xff | (content[contentOffset + (i << 1) + 1] & 0xff) << 8);
	}

	private int decodeTimPalette(byte[] content, int contentLength, int colors)
	{
		if ((content[16] & 0xff | (content[17] & 0xff) << 8) != colors)
			return -1;
		int paletteCount = content[18] & 0xff | (content[19] & 0xff) << 8;
		if (paletteCount == 0)
			return -1;
		int bitmapOffset = 20 + (paletteCount * colors << 1);
		if (get32LittleEndian(content, 8) != bitmapOffset - 8 || contentLength < bitmapOffset + 12)
			return -1;
		int width = (content[bitmapOffset + 8] & 0xff | (content[bitmapOffset + 9] & 0xff) << 8) << 1;
		int height = content[bitmapOffset + 10] & 0xff | (content[bitmapOffset + 11] & 0xff) << 8;
		if (contentLength < bitmapOffset + 12 + width * height)
			return -1;
		if (colors == 16)
			width <<= 1;
		if (!setSize(width, height, RECOILResolution.PLAY_STATION1X1))
			return -1;
		decodePlayStation(content, 20, this.contentPalette, colors);
		return bitmapOffset + 12;
	}

	private boolean decodeTim(byte[] content, int contentLength)
	{
		if (contentLength < 20 || get32LittleEndian(content, 0) != 16)
			return false;
		int pixelsLength;
		int bitmapOffset;
		switch (content[4] & 15) {
		case 2:
			int width = content[16] & 0xff | (content[17] & 0xff) << 8;
			int height = content[18] & 0xff | (content[19] & 0xff) << 8;
			pixelsLength = width * height;
			if (contentLength < 20 + (pixelsLength << 1) || !setSize(width, height, RECOILResolution.PLAY_STATION1X1))
				return false;
			decodePlayStation(content, 20, this.pixels, pixelsLength);
			return true;
		case 8:
			bitmapOffset = decodeTimPalette(content, contentLength, 16);
			if (bitmapOffset < 0)
				return false;
			pixelsLength = this.width * this.height;
			for (int i = 0; i < pixelsLength; i++) {
				int b = content[bitmapOffset + (i >> 1)] & 0xff;
				this.pixels[i] = this.contentPalette[(i & 1) == 0 ? b & 15 : b >> 4];
			}
			return true;
		case 9:
			bitmapOffset = decodeTimPalette(content, contentLength, 256);
			if (bitmapOffset < 0)
				return false;
			decodeBytes(content, bitmapOffset);
			return true;
		default:
			return false;
		}
	}

	private static final int[] BBC_PALETTE = { 0, 16711680, 65280, 16776960, 255, 16711935, 65535, 16777215, 0, 16711680, 65280, 16776960, 255, 16711935, 65535, 16777215 };

	private static final int[] BBC_PALETTE2_BIT = { 0, 16711680, 16776960, 16777215 };

	private static final int[] BBC_PALETTE1_BIT = { 0, 16777215 };

	private boolean decodeBb0(byte[] content, int contentLength, int[] palette)
	{
		if (contentLength != 20480)
			return false;
		setSize(640, 512, RECOILResolution.BBC1X2);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 640; x++) {
				int c = (content[(y & -8) * 80 + (x & -8) + (y & 7)] & 0xff) >> (~x & 7) & 1;
				int pixelsOffset = y * 1280 + x;
				this.pixels[pixelsOffset + 640] = this.pixels[pixelsOffset] = palette[c];
			}
		}
		return true;
	}

	private boolean decodeBb1(byte[] content, int contentLength, int[] palette)
	{
		if (contentLength != 20480)
			return false;
		setSize(320, 256, RECOILResolution.BBC1X1);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 320; x++) {
				int c = (content[(y & -8) * 80 + ((x & -4) << 1) + (y & 7)] & 0xff) >> (~x & 3);
				this.pixels[y * 320 + x] = palette[(c >> 3 & 2) + (c & 1)];
			}
		}
		return true;
	}

	private boolean decodeBb2(byte[] content, int contentLength, int[] palette)
	{
		if (contentLength != 20480)
			return false;
		setSize(320, 256, RECOILResolution.BBC2X1);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 160; x++) {
				int c = (content[(y & -8) * 80 + ((x & -2) << 2) + (y & 7)] & 0xff) >> (~x & 1);
				int pixelsOffset = (y * 160 + x) << 1;
				this.pixels[pixelsOffset + 1] = this.pixels[pixelsOffset] = palette[(c >> 3 & 8) + (c >> 2 & 4) + (c >> 1 & 2) + (c & 1)];
			}
		}
		return true;
	}

	private boolean decodeBb4(byte[] content, int contentLength, int[] palette)
	{
		if (contentLength != 10240)
			return false;
		setSize(320, 256, RECOILResolution.BBC1X1);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 320; x++) {
				int c = (content[(y & -8) * 40 + (x & -8) + (y & 7)] & 0xff) >> (~x & 7) & 1;
				this.pixels[y * 320 + x] = palette[c];
			}
		}
		return true;
	}

	private boolean decodeBb5(byte[] content, int contentLength, int[] palette)
	{
		if (contentLength != 10240)
			return false;
		setSize(320, 256, RECOILResolution.BBC2X1);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 160; x++) {
				int c = (content[(y & -8) * 40 + ((x & -4) << 1) + (y & 7)] & 0xff) >> (~x & 3);
				int pixelsOffset = (y * 160 + x) << 1;
				this.pixels[pixelsOffset + 1] = this.pixels[pixelsOffset] = palette[(c >> 3 & 2) + (c & 1)];
			}
		}
		return true;
	}

	private boolean decodeBbg(byte[] content, int contentLength)
	{
		final BbgStream rle = new BbgStream();
		rle.content = content;
		rle.contentOffset = 0;
		rle.contentLength = contentLength;
		rle.valueBits = rle.readBitsReverse(8);
		if (rle.valueBits < 1 || rle.valueBits > 8)
			return false;
		int mode = rle.readBitsReverse(8);
		int unpackedLength;
		switch (mode) {
		case 0:
		case 1:
		case 2:
			unpackedLength = 20480;
			break;
		case 4:
		case 5:
			unpackedLength = 10240;
			break;
		default:
			return false;
		}
		for (int i = 15; i >= 0; i--) {
			int c = rle.readBitsReverse(4);
			if (c < 0)
				return false;
			this.contentPalette[i] = BBC_PALETTE[c];
		}
		int unpackedStep = rle.readBitsReverse(8);
		if (unpackedStep <= 0)
			return false;
		rle.countBits = rle.readBitsReverse(8);
		if (rle.countBits < 1 || rle.countBits > 8)
			return false;
		final byte[] unpacked = new byte[20480];
		for (int x = unpackedStep - 1; x >= 0; x--) {
			if (!rle.unpack(unpacked, x, unpackedStep, unpackedLength))
				return false;
		}
		switch (mode) {
		case 0:
			return decodeBb0(unpacked, unpackedLength, this.contentPalette);
		case 1:
			return decodeBb1(unpacked, unpackedLength, this.contentPalette);
		case 2:
			return decodeBb2(unpacked, unpackedLength, this.contentPalette);
		case 4:
			return decodeBb4(unpacked, unpackedLength, this.contentPalette);
		case 5:
			return decodeBb5(unpacked, unpackedLength, this.contentPalette);
		default:
			return false;
		}
	}

	private static int getOricHeader(byte[] content, int contentLength)
	{
		if (contentLength < 26 || content[0] != 22 || content[1] != 22 || content[2] != 22 || content[3] != 36 || content[4] != 0 || content[5] != 0 || content[6] != -128 || content[7] != 0 || content[12] != 0)
			return 0;
		int contentOffset = 13;
		while (content[contentOffset++] != 0) {
			if (contentOffset >= 26)
				return 0;
		}
		return contentOffset;
	}

	private boolean decodeChs(byte[] content, int contentLength)
	{
		int contentOffset = getOricHeader(content, contentLength);
		switch (contentLength - contentOffset) {
		case 768:
		case 769:
			break;
		default:
			return false;
		}
		setSize(256, 24, RECOILResolution.ORIC1X1);
		decodeBlackAndWhiteFont(content, contentOffset, contentLength, 8);
		return true;
	}

	private boolean decodeHrs(byte[] content, int contentLength)
	{
		int contentOffset = getOricHeader(content, contentLength);
		if (contentOffset + 8000 != contentLength)
			return false;
		setSize(240, 200, RECOILResolution.ORIC1X1);
		for (int y = 0; y < 200; y++) {
			int paper = 0;
			int ink = 7;
			for (int col = 0; col < 40; col++) {
				int offset = y * 40 + col;
				int b = content[contentOffset + offset] & 0xff;
				int inverse = b >= 128 ? 7 : 0;
				switch (b & 120) {
				case 0:
					ink = b & 7;
					b = 0;
					break;
				case 8:
				case 24:
					b = 0;
					break;
				case 16:
					paper = b & 7;
					b = 0;
					break;
				default:
					break;
				}
				for (int x = 0; x < 6; x++)
					this.pixels[offset * 6 + x] = BBC_PALETTE[((b >> (5 - x) & 1) == 0 ? paper : ink) ^ inverse];
			}
		}
		return true;
	}

	private static int getAmstradHeader(byte[] content, int contentLength)
	{
		if (contentLength < 128 || (content[24] & 0xff | (content[25] & 0xff) << 8) != contentLength - 128 || (content[64] & 0xff) != (content[24] & 0xff) || (content[65] & 0xff) != (content[25] & 0xff) || content[66] != 0)
			return 0;
		int sum = 0;
		for (int i = 0; i < 67; i++)
			sum += content[i] & 0xff;
		if ((content[67] & 0xff | (content[68] & 0xff) << 8) != sum)
			return 0;
		return 128;
	}

	private boolean decodeAmstradFnt(byte[] content, int contentLength)
	{
		int contentOffset = getAmstradHeader(content, contentLength);
		if (contentLength != contentOffset + 768 && (contentLength != 896 || contentOffset != 0))
			return false;
		setSize(256, 24, RECOILResolution.AMSTRAD1X1);
		decodeBlackAndWhiteFont(content, contentOffset, contentLength, 8);
		return true;
	}

	private boolean decodeAmstradMode2(byte[] content, int contentOffset, int width, int height)
	{
		setSize(width, height << 1, RECOILResolution.AMSTRAD1X2);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = (y * width << 1) + x;
				int c = (content[contentOffset + ((y & 7) << 11) + (((y >> 3) * width + x) >> 3)] & 0xff) >> (~x & 7) & 1;
				this.pixels[offset + width] = this.pixels[offset] = this.contentPalette[c];
			}
		}
		return true;
	}

	private boolean decodeHgb(byte[] content, int contentLength)
	{
		int contentOffset = getAmstradHeader(content, contentLength);
		if (contentLength != contentOffset + 16384)
			return false;
		this.contentPalette[0] = 0;
		this.contentPalette[1] = 16777215;
		return decodeAmstradMode2(content, contentOffset, 512, 256);
	}

	private static final int[] AMSTRAD_PALETTE = { 8421504, 8421504, 65408, 16777088, 128, 16711808, 32896, 16744576, 16711808, 16777088, 16776960, 16777215, 16711680, 16711935, 16744448, 16744703,
		128, 65408, 65280, 65535, 0, 255, 32768, 33023, 8388736, 8454016, 8453888, 8454143, 8388608, 8388863, 8421376, 8421631 };

	private int setAmstradPalette(String filename)
	{
		final byte[] pal = new byte[368];
		int palLength = readCompanionFile(filename, "PAL", "pal", pal, 368);
		int palOffset = getAmstradHeader(pal, palLength);
		if (palLength != palOffset + 239)
			return -1;
		for (int i = 0; i < 16; i++) {
			int c = pal[palOffset + 3 + i * 12] & 0xff;
			if (c < 64 || c > 95)
				return -1;
			this.contentPalette[i] = AMSTRAD_PALETTE[c - 64];
		}
		return pal[palOffset] & 0xff;
	}

	private void decodeAmstradMode0Line(byte[] content, int lineOffset, int y)
	{
		int skip = this.resolution == RECOILResolution.AMSTRAD1X1 ? (y ^ (y >= this.height ? 1 : 0)) & 1 : 0;
		for (int x = 0; x < this.width; x++) {
			int i = x + skip;
			int b = i >= this.width ? 0 : content[lineOffset + (i >> 2)] & 0xff;
			if ((i & 2) == 0)
				b >>= 1;
			this.pixels[y * this.width + x] = this.contentPalette[((b & 1) << 3) + (b >> 2 & 4) + (b >> 1 & 2) + (b >> 6 & 1)];
		}
	}

	private void decodeAmstradMode1Line(byte[] content, int lineOffset, int y)
	{
		for (int x = 0; x < this.width; x++) {
			int b = (content[lineOffset + (x >> 2)] & 0xff) >> (~x & 3);
			this.pixels[y * this.width + x] = this.contentPalette[((b & 1) << 1) + (b >> 4 & 1)];
		}
	}

	private boolean decodeAmstradScr(String filename, byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[16384];
		int contentOffset = getAmstradHeader(content, contentLength);
		switch (contentLength - contentOffset) {
		case 16336:
		case 16384:
			break;
		default:
			if (!AmstradStream.unpackFile(content, contentOffset, contentLength, unpacked, 16384))
				return false;
			content = unpacked;
			contentOffset = 0;
			break;
		}
		switch (setAmstradPalette(filename)) {
		case 0:
			setSize(320, 200, RECOILResolution.AMSTRAD2X1);
			for (int y = 0; y < 200; y++)
				decodeAmstradMode0Line(content, contentOffset + ((y & 7) << 11) + (y >> 3) * 80, y);
			return true;
		case 1:
			setSize(320, 200, RECOILResolution.AMSTRAD1X1);
			for (int y = 0; y < 200; y++)
				decodeAmstradMode1Line(content, contentOffset + ((y & 7) << 11) + (y >> 3) * 80, y);
			return true;
		case 2:
			return decodeAmstradMode2(content, contentOffset, 640, 200);
		default:
			return false;
		}
	}

	private boolean decodeWin(String filename, byte[] content, int contentLength)
	{
		if (contentLength < 6)
			return false;
		int width = content[contentLength - 4] & 0xff | (content[contentLength - 3] & 0xff) << 8;
		if (width == 0 || width > 640)
			return false;
		int height = content[contentLength - 2] & 0xff;
		if (height == 0 || height > 200)
			return false;
		int bytesPerLine = (width + 7) >> 3;
		final byte[] unpacked = new byte[16000];
		int contentOffset = getAmstradHeader(content, contentLength);
		if (contentLength != contentOffset + bytesPerLine * height + 5) {
			if (!AmstradStream.unpackFile(content, contentOffset, contentLength, unpacked, bytesPerLine * height))
				return false;
			content = unpacked;
			contentOffset = 0;
		}
		if (setAmstradPalette(filename) != 0)
			return false;
		width >>= 1;
		setSize(width, height, RECOILResolution.AMSTRAD2X1);
		for (int y = 0; y < height; y++)
			decodeAmstradMode0Line(content, contentOffset + y * bytesPerLine, y);
		return true;
	}

	private boolean decodeCm5(String filename, byte[] content, int contentLength)
	{
		if (contentLength != 2049)
			return false;
		final byte[] gfx = new byte[18433];
		if (readCompanionFile(filename, "GFX", "gfx", gfx, 18433) != 18432)
			return false;
		setSize(288, 256, RECOILResolution.AMSTRAD1X1);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 288; x++) {
				int c;
				switch ((gfx[y * 72 + (x >> 2)] & 0xff) >> (~x & 3) & 17) {
				case 0:
					c = 3 + (y << 3) + x / 48;
					break;
				case 1:
					c = 1 + (y << 3);
					break;
				case 16:
					c = 2 + (y << 3);
					break;
				default:
					c = 0;
					break;
				}
				c = content[c] & 0xff;
				if (c < 64 || c > 95)
					return false;
				this.pixels[y * 288 + x] = AMSTRAD_PALETTE[c - 64];
			}
		}
		return true;
	}

	private boolean decodeSgx(byte[] content, int contentLength)
	{
		int width = 0;
		int height = 0;
		int chunkLeft = 0;
		int rowHeight = 0;
		for (int contentOffset = 0; contentOffset + 3 < contentLength;) {
			int chunkStride = content[contentOffset] & 0xff;
			if (chunkStride == 0)
				break;
			if (chunkStride == 255) {
				if (width == 0)
					width = chunkLeft;
				else if (chunkLeft != width)
					return false;
				chunkLeft = 0;
				contentOffset += 3;
			}
			else {
				int chunkWidth;
				int chunkHeight;
				if (chunkStride <= 63) {
					chunkWidth = content[contentOffset + 1] & 0xff;
					if ((chunkWidth + 3) >> 2 != chunkStride)
						return false;
					chunkHeight = content[contentOffset + 2] & 0xff;
					contentOffset += 3;
				}
				else if (chunkStride == 64) {
					if (contentOffset + 8 >= contentLength || content[contentOffset + 1] != 5)
						return false;
					chunkStride = content[contentOffset + 2] & 0xff | (content[contentOffset + 3] & 0xff) << 8;
					chunkWidth = content[contentOffset + 4] & 0xff | (content[contentOffset + 5] & 0xff) << 8;
					if ((chunkWidth + 1) >> 1 != chunkStride)
						return false;
					chunkHeight = content[contentOffset + 6] & 0xff | (content[contentOffset + 7] & 0xff) << 8;
					contentOffset += 8;
				}
				else
					return false;
				if (chunkLeft == 0)
					height += rowHeight = chunkHeight;
				else if (chunkHeight != rowHeight)
					return false;
				chunkLeft += chunkWidth;
				contentOffset += chunkHeight * chunkStride;
				if (contentOffset > contentLength)
					return false;
			}
		}
		if (width == 0)
			width = chunkLeft;
		else if (chunkLeft != width)
			return false;
		if (!setSize(width, height, RECOILResolution.AMSTRAD1X1))
			return false;
		chunkLeft = 0;
		int chunkTop = 0;
		rowHeight = 0;
		for (int contentOffset = 0; contentOffset + 3 < contentLength;) {
			int chunkStride = content[contentOffset] & 0xff;
			if (chunkStride == 0)
				break;
			if (chunkStride == 255) {
				chunkLeft = 0;
				chunkTop += rowHeight;
				contentOffset += 3;
			}
			else {
				int chunkWidth;
				if (chunkStride <= 63) {
					chunkWidth = content[contentOffset + 1] & 0xff;
					rowHeight = content[contentOffset + 2] & 0xff;
					contentOffset += 3;
					for (int y = 0; y < rowHeight; y++) {
						for (int x = 0; x < chunkWidth; x++) {
							int b = (content[contentOffset + (x >> 2)] & 0xff) >> (~x & 3);
							this.pixels[(chunkTop + y) * width + chunkLeft + x] = DECODE_SGX_PALETTE4[(b >> 3 & 2) + (b & 1)];
						}
						contentOffset += chunkStride;
					}
				}
				else {
					chunkStride = content[contentOffset + 2] & 0xff | (content[contentOffset + 3] & 0xff) << 8;
					chunkWidth = content[contentOffset + 4] & 0xff | (content[contentOffset + 5] & 0xff) << 8;
					rowHeight = content[contentOffset + 6] & 0xff | (content[contentOffset + 7] & 0xff) << 8;
					contentOffset += 8;
					for (int y = 0; y < rowHeight; y++) {
						for (int x = 0; x < chunkWidth; x++) {
							int c = getNibble(content, contentOffset, x);
							this.pixels[(chunkTop + y) * width + chunkLeft + x] = DECODE_SGX_PALETTE16[c];
						}
						contentOffset += chunkStride;
					}
				}
				chunkLeft += chunkWidth;
			}
		}
		return true;
	}

	private boolean setAmstradFirmwarePalette(byte[] content, int contentOffset, int count)
	{
		for (int i = 0; i < count; i++) {
			int c = content[contentOffset + i] & 0xff;
			if (c > 26)
				return false;
			this.contentPalette[i] = (SET_AMSTRAD_FIRMWARE_PALETTE_TRI_LEVEL[c / 3 % 3] & 0xff) << 16 | (SET_AMSTRAD_FIRMWARE_PALETTE_TRI_LEVEL[c / 9] & 0xff) << 8 | SET_AMSTRAD_FIRMWARE_PALETTE_TRI_LEVEL[c % 3] & 0xff;
		}
		return true;
	}

	private boolean setAmstradFirmwarePalette16(byte[] content)
	{
		return content[5] == 1 && setAmstradFirmwarePalette(content, 6, 16);
	}

	private boolean decodePphFrame(String filename, String upperExt, String lowerExt, byte[] bitmap, byte[] pph, int yOffset)
	{
		int bitmapStride = this.width >> 2;
		int bitmapLength = this.height * bitmapStride;
		if (readCompanionFile(filename, upperExt, lowerExt, bitmap, bitmapLength + 1) != bitmapLength)
			return false;
		if (pph[0] == 5) {
			int paletteOffset = 6;
			int paletteLines = 0;
			for (int y = 0; y < this.height; y++) {
				if (paletteLines == 0) {
					if (!setAmstradFirmwarePalette(pph, paletteOffset, 4))
						return false;
					paletteOffset += 4;
					if (paletteOffset < (1 + (pph[5] & 0xff)) * 5) {
						paletteLines = pph[paletteOffset++] & 0xff;
						if (paletteLines == 0)
							return false;
					}
					else
						paletteLines = 272;
				}
				decodeAmstradMode1Line(bitmap, y * bitmapStride, yOffset + y);
				paletteLines--;
			}
		}
		else {
			for (int y = 0; y < this.height; y++)
				decodeAmstradMode0Line(bitmap, y * bitmapStride, yOffset + y);
		}
		return true;
	}

	private boolean decodePph(String filename, byte[] content, int contentLength)
	{
		if (contentLength < 10)
			return false;
		int resolution;
		switch (content[0]) {
		case 3:
			if (contentLength != 22 || !setAmstradFirmwarePalette16(content))
				return false;
			resolution = RECOILResolution.AMSTRAD1X1;
			break;
		case 4:
			if (contentLength != 22 || !setAmstradFirmwarePalette16(content))
				return false;
			resolution = RECOILResolution.AMSTRAD2X1;
			break;
		case 5:
			if (contentLength != (1 + (content[5] & 0xff)) * 5)
				return false;
			resolution = RECOILResolution.AMSTRAD1X1;
			break;
		default:
			return false;
		}
		int width = content[1] & 0xff | (content[2] & 0xff) << 8;
		if (width == 0 || width > 384 || (width & 3) != 0)
			return false;
		int height = content[3] & 0xff | (content[4] & 0xff) << 8;
		if (height == 0 || height > 272)
			return false;
		setSize(width, height, resolution, 2);
		final byte[] bitmap = new byte[26113];
		return decodePphFrame(filename, "ODD", "odd", bitmap, content, 0) && decodePphFrame(filename, "EVE", "eve", bitmap, content, height) && applyBlend();
	}

	private boolean decodeZx81(byte[] screen)
	{
		setSize(256, 192, RECOILResolution.ZX811X1);
		byte[] font = CiResource.getByteArray("zx81.fnt", 512);
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x++) {
				int c = screen[y >> 3 << 5 | x >> 3] & 0xff;
				int b = (font[(c & 63) << 3 | (y & 7)] & 0xff) >> (~x & 7) & 1;
				this.pixels[(y << 8) + x] = b == c >> 7 ? 16777215 : 0;
			}
		}
		return true;
	}

	private boolean decodeZx81Raw(byte[] content, int contentLength)
	{
		if (contentLength != 792)
			return false;
		final byte[] screen = new byte[768];
		for (int y = 0; y < 24; y++) {
			if (content[y * 33 + 32] != 118)
				return false;
			System.arraycopy(content, y * 33, screen, y * 32, 32);
		}
		return decodeZx81(screen);
	}

	private boolean decodeZp1(byte[] content, int contentLength)
	{
		final byte[] screen = new byte[768];
		final Stream s = new Stream();
		s.content = content;
		s.contentOffset = 0;
		s.contentLength = contentLength;
		for (int i = 0; i < 768; i++) {
			int b = s.readHexByte();
			if (b < 0)
				return false;
			screen[i] = (byte) b;
		}
		return decodeZx81(screen);
	}

	private boolean decodeP(byte[] content, int contentLength)
	{
		final PInterpreter interp = new PInterpreter();
		interp.content = content;
		interp.contentLength = contentLength;
		return interp.run() && decodeZx81(interp.screen);
	}

	private static int getZxColor(int c)
	{
		return (c >> 1 & 1) * 16711680 | (c >> 2 & 1) * 65280 | (c & 1) * 255;
	}

	private void setZxPalette()
	{
		for (int i = 0; i < 64; i++) {
			int rgb = getZxColor(i);
			if ((i & 16) == 0)
				rgb &= 13487565;
			this.contentPalette[i] = rgb;
		}
	}

	private boolean setZxSize(int width, int height, int resolution, int frames)
	{
		if (!setSize(width, height, resolution, frames))
			return false;
		setZxPalette();
		return true;
	}

	private boolean setZxSize(int width, int height, int resolution)
	{
		return setZxSize(width, height, resolution, 1);
	}

	private void setZx(int resolution, int frames)
	{
		setZxSize(256, 192, resolution, frames);
	}

	private void setZx(int resolution)
	{
		setZx(resolution, 1);
	}

	private static int getG3R3B2Color(int c)
	{
		return (c & 28) * 73 >> 3 << 16 | (c >> 5) * 73 >> 1 << 8 | (c & 3) * 85;
	}

	private void setUlaPlus(byte[] content, int paletteOffset)
	{
		setSize(256, 192, RECOILResolution.SPECTRUM_ULA_PLUS1X1);
		for (int i = 0; i < 64; i++)
			this.contentPalette[i] = getG3R3B2Color(content[paletteOffset + i] & 0xff);
	}

	private static final int ZX_BITMAP_CHECKERBOARD = -3;

	private static final int ZX_BITMAP_HLR = -2;

	private static final int ZX_BITMAP_LINEAR = -1;

	private static final int ZX_ATTRIBUTES_NONE = -3;

	private static final int ZX_ATTRIBUTES_MG1 = -2;

	private static final int ZX_ATTRIBUTES_TIMEX = -1;

	private static final int ZX_ATTRIBUTES8X1 = 0;

	private static final int ZX_ATTRIBUTES8X2 = 1;

	private static final int ZX_ATTRIBUTES8X4 = 2;

	private static final int ZX_ATTRIBUTES8X8 = 3;

	private static int getZxLineOffset(int y)
	{
		return ((y & 192) << 5) + ((y & 7) << 8) + ((y & 56) << 2);
	}

	private void decodeZx(byte[] content, int bitmapOffset, int attributesOffset, int attributesMode, int pixelsOffset)
	{
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x++) {
				int col = x >> 3;
				int c;
				switch (bitmapOffset) {
				case -3:
					c = x ^ y;
					break;
				case -2:
					c = (content[84 + (y & 7)] & 0xff) >> (~x & 7);
					break;
				case -1:
					c = (content[y << 5 | col] & 0xff) >> (~x & 7);
					break;
				default:
					c = (content[bitmapOffset + getZxLineOffset(y) + col] & 0xff) >> (~x & 7);
					break;
				}
				c &= 1;
				if (attributesMode == -3) {
					if (c != 0)
						c = 16777215;
				}
				else {
					int a;
					switch (attributesMode) {
					case -2:
						if (col < 8)
							a = attributesOffset + (y >> 3 << 4);
						else if (col < 24)
							a = (attributesOffset == 18688 ? 12536 : 15608) + (y << 4);
						else
							a = attributesOffset + (y >> 3 << 4) - 16;
						break;
					case -1:
						a = attributesOffset + getZxLineOffset(y);
						break;
					default:
						a = attributesOffset + (y >> attributesMode << 5);
						break;
					}
					a = content[a + col] & 0xff;
					c = this.contentPalette[(a >> 2 & 48) | (c == 0 ? 8 | (a >> 3 & 7) : a & 7)];
				}
				this.pixels[pixelsOffset + (y << 8) + x] = c;
			}
		}
	}

	private void decodeTimexHires(byte[] content, int contentOffset, int pixelsOffset)
	{
		int inkColor = getZxColor((content[contentOffset + 12288] & 0xff) >> 3);
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 512; x++) {
				int c = (content[contentOffset + (x & 8) * 768 + getZxLineOffset(y) + (x >> 4)] & 0xff) >> (~x & 7) & 1;
				int offset = pixelsOffset + (y << 10) + x;
				this.pixels[offset + 512] = this.pixels[offset] = c == 0 ? inkColor ^ 16777215 : inkColor;
			}
		}
	}

	private boolean decodeHrg(byte[] content, int contentLength)
	{
		if (contentLength != 24578)
			return false;
		setSize(512, 384, RECOILResolution.TIMEX1X2, 2);
		decodeTimexHires(content, 0, 0);
		decodeTimexHires(content, 12289, 196608);
		return applyBlend();
	}

	private boolean decodeZxIfl(byte[] content, int contentLength)
	{
		if (contentLength != 9216)
			return false;
		setZx(RECOILResolution.SPECTRUM1X1);
		decodeZx(content, 0, 6144, 1, 0);
		return true;
	}

	private boolean decodeMcMlt(byte[] content, int contentLength, int bitmapOffset)
	{
		if (contentLength != 12288)
			return false;
		setZx(RECOILResolution.SPECTRUM1X1);
		decodeZx(content, bitmapOffset, 6144, 0, 0);
		return true;
	}

	private boolean decodeZxImg(byte[] content, int contentLength)
	{
		if (contentLength != 13824)
			return false;
		setZx(RECOILResolution.SPECTRUM1X1, 2);
		decodeZx(content, 0, 6144, 3, 0);
		decodeZx(content, 6912, 13056, 3, 49152);
		return applyBlend();
	}

	private boolean decodeMg(byte[] content, int contentLength)
	{
		if (contentLength < 14080 || content[0] != 77 || content[1] != 71 || content[2] != 72 || content[3] != 1)
			return false;
		int attributesMode;
		switch (content[4]) {
		case 1:
			if (contentLength != 19456)
				return false;
			setZx(RECOILResolution.SPECTRUM1X1, 2);
			decodeZx(content, 256, 18688, -2, 0);
			decodeZx(content, 6400, 19072, -2, 49152);
			return applyBlend();
		case 2:
			attributesMode = 1;
			break;
		case 4:
			attributesMode = 2;
			break;
		case 8:
			attributesMode = 3;
			break;
		default:
			return false;
		}
		if (contentLength != 12544 + (12288 >> attributesMode))
			return false;
		setZx(RECOILResolution.SPECTRUM1X1, 2);
		decodeZx(content, 256, 12544, attributesMode, 0);
		decodeZx(content, 6400, 12544 + (6144 >> attributesMode), attributesMode, 49152);
		return applyBlend();
	}

	private boolean decodeAtr(byte[] content, int contentLength)
	{
		if (contentLength != 768)
			return false;
		setZx(RECOILResolution.SPECTRUM1X1);
		decodeZx(content, -3, 0, 3, 0);
		return true;
	}

	private boolean decodeHlr(byte[] content, int contentLength)
	{
		if (contentLength != 1628 || content[0] != 118 || content[1] != -81 || content[2] != -45 || content[3] != -2 || content[4] != 33 || content[5] != 0 || content[6] != 88)
			return false;
		setZx(RECOILResolution.SPECTRUM1X1, 2);
		decodeZx(content, -2, 92, 3, 0);
		decodeZx(content, -2, 860, 3, 49152);
		return applyBlend();
	}

	private boolean decodeStl(byte[] content, int contentLength)
	{
		if (contentLength != 3072)
			return false;
		setSize(256, 192, RECOILResolution.SPECTRUM4X4, 2);
		for (int f = 0; f < 2; f++) {
			for (int y = 0; y < 192; y++) {
				for (int x = 0; x < 256; x++) {
					int b = content[(y & -4) << 4 | (x >> 2 & -4) | f << 1 | (x >> 3 & 1)] & 0xff;
					int rgb = getZxColor((x & 4) == 0 ? b >> 3 : b);
					if ((b & 64) == 0)
						rgb &= 13487565;
					this.pixels[((f * 192 + y) << 8) + x] = rgb;
				}
			}
		}
		return applyBlend();
	}

	private boolean decodeZxRgb3(byte[] content, int contentLength, byte[] frameComponents)
	{
		if (contentLength != 18432)
			return false;
		setSize(256, 192, RECOILResolution.SPECTRUM1X1);
		this.frames = 3;
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x++) {
				int offset = getZxLineOffset(y) + (x >> 3);
				int c = 0;
				for (int frame = 0; frame < 3; frame++) {
					if (((content[frame * 6144 + offset] & 0xff) >> (~x & 7) & 1) != 0)
						c |= 255 << (frameComponents[frame] & 0xff);
				}
				this.pixels[(y << 8) + x] = c;
			}
		}
		return true;
	}

	private boolean decodeZxRgb(byte[] content, int contentLength)
	{
		return decodeZxRgb3(content, contentLength, DECODE_ZX_RGB_FRAME_COMPONENTS);
	}

	private boolean decode3(byte[] content, int contentLength)
	{
		return decodeZxRgb3(content, contentLength, DECODE3_FRAME_COMPONENTS);
	}

	private boolean decodeZxs(byte[] content, int contentLength)
	{
		if (contentLength != 2452 || !isStringAt(content, 0, "ZX_SSCII") || content[8] != -108 || content[9] != 9 || content[10] != 0 || content[11] != 0)
			return false;
		setZx(RECOILResolution.SPECTRUM1X1);
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x++) {
				int offset = (x & -8) * 3 + (y >> 3);
				int c = content[908 + offset] & 0xff;
				if (c >= 112)
					return false;
				c = (content[12 + (c << 3) + (y & 7)] & 0xff) >> (~x & 7) & 1;
				int a = content[1676 + offset] & 0xff;
				this.pixels[(y << 8) + x] = this.contentPalette[(a >> 2 & 48) | (c == 0 ? 8 | (a >> 3 & 7) : a & 7)];
			}
		}
		return true;
	}

	private boolean decodeSev(byte[] content, int contentLength)
	{
		if (contentLength < 23 || content[0] != 83 || content[1] != 101 || content[2] != 118 || content[3] != 0 || content[6] != 1 || content[7] != 0)
			return false;
		int width = content[10] & 0xff | (content[11] & 0xff) << 8;
		int height = content[12] & 0xff | (content[13] & 0xff) << 8;
		int columns = (width + 7) >> 3;
		int rows = (height + 7) >> 3;
		if (contentLength < 14 + rows * columns * 9 || !setZxSize(width, height, RECOILResolution.SPECTRUM1X1))
			return false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = 14 + ((y >> 3) * columns + (x >> 3)) * 9;
				int c = (content[offset + (y & 7)] & 0xff) >> (~x & 7) & 1;
				int a = content[offset + 8] & 0xff;
				this.pixels[y * width + x] = this.contentPalette[(a >> 2 & 48) | (c == 0 ? 8 | (a >> 3 & 7) : a & 7)];
			}
		}
		return true;
	}

	private boolean decodeCh8(byte[] content, int contentLength)
	{
		int height;
		switch (contentLength) {
		case 768:
			height = 24;
			break;
		case 2048:
			height = 64;
			break;
		default:
			return false;
		}
		setSize(256, height, RECOILResolution.SPECTRUM1X1);
		decodeBlackAndWhiteFont(content, 0, contentLength, 8);
		return true;
	}

	private static int getChxTileOffset(byte[] content, int tile)
	{
		return content[5 + (tile << 1)] & 0xff | (content[6 + (tile << 1)] & 0xff) << 8;
	}

	private boolean decodeChx(byte[] content, int contentLength)
	{
		if (contentLength < 517 || content[0] != 67 || content[1] != 72 || content[2] != 88 || content[3] != 0 || content[4] != 0)
			return false;
		int maxColumns = 0;
		int maxRows = 0;
		for (int tile = 0; tile < 256; tile++) {
			int offset = getChxTileOffset(content, tile);
			if (offset == 0)
				continue;
			if (offset + 2 >= contentLength)
				return false;
			int transparent = content[offset] & 0xff;
			if (transparent > 1)
				return false;
			int columns = content[offset + 1] & 0xff;
			int rows = content[offset + 2] & 0xff;
			if (offset + 3 + rows * columns * (9 - transparent) > contentLength)
				return false;
			if (maxColumns < columns)
				maxColumns = columns;
			if (maxRows < rows)
				maxRows = rows;
		}
		int width = maxColumns << 7;
		int height = maxRows << 7;
		if (!setZxSize(width, height, RECOILResolution.SPECTRUM1X1))
			return false;
		for (int y = 0; y < height; y++) {
			int row = y >> 3;
			int rowTile = row / maxRows << 4;
			row %= maxRows;
			for (int x = 0; x < width; x++) {
				int column = x >> 3;
				int tile = rowTile + column / maxColumns;
				column %= maxColumns;
				int c = ~x ^ y;
				int a = 56;
				int offset = getChxTileOffset(content, tile);
				if (offset > 0) {
					int columns = content[offset + 1] & 0xff;
					if (column < columns && row < (content[offset + 2] & 0xff)) {
						int transparent = content[offset] & 0xff;
						offset += 3 + (row * columns + column) * (9 - transparent);
						c = (content[offset + (y & 7)] & 0xff) >> (~x & 7);
						if (transparent == 0)
							a = content[offset + 8] & 0xff;
					}
				}
				this.pixels[y * width + x] = this.contentPalette[(a >> 2 & 48) | ((c & 1) == 0 ? 8 | (a >> 3 & 7) : a & 7)];
			}
		}
		return true;
	}

	private boolean decodeZxp(byte[] content, int contentLength)
	{
		if (contentLength < 25 || !isStringAt(content, 0, "ZX-Paintbrush "))
			return false;
		final ZxpStream s = new ZxpStream();
		s.content = content;
		s.contentOffset = isStringAt(content, 14, "extended ") ? 23 : 14;
		s.contentLength = contentLength;
		if (!s.expect("image") || s.readChar() != '\n' || s.readChar() != '\n')
			return false;
		int bitmapOffset = s.contentOffset;
		int width = 0;
		int height = 0;
		for (int x = 0;;) {
			switch (s.readChar()) {
			case '*':
			case '0':
			case '1':
				x++;
				continue;
			case '\n':
				break;
			default:
				return false;
			}
			if (x == 0)
				break;
			if (height == 0)
				width = x;
			else if (x != width)
				return false;
			height++;
			x = 0;
		}
		int attributesOffset = s.contentOffset;
		int attributesHeight = 0;
		int resolution = RECOILResolution.SPECTRUM1X1;
		setZxPalette();
		while (!s.isEof()) {
			int b = content[s.contentOffset] & 0xff;
			if (b == '\r' || b == '\n') {
				for (int i = 0; i < 64; i++) {
					if (s.readChar() != (i == 0 ? '\n' : ' '))
						return false;
					b = s.readHexByte();
					if (b < 0)
						return false;
					this.contentPalette[i] = getG3R3B2Color(b);
				}
				if (s.readChar() != '\n' || !s.isEof())
					return false;
				resolution = RECOILResolution.SPECTRUM_ULA_PLUS1X1;
				break;
			}
			if (s.readHexByte() < 0)
				return false;
			switch (s.readChar()) {
			case ' ':
				break;
			case '\n':
				attributesHeight++;
				break;
			default:
				return false;
			}
		}
		if ((attributesHeight != height && attributesHeight != (height + 7) >> 3) || !setSize(width, height, resolution))
			return false;
		s.contentOffset = attributesOffset;
		for (int y = 0; y < height; y++) {
			if (attributesHeight != height) {
				if ((y & 7) == 0)
					attributesOffset = s.contentOffset;
				else
					s.contentOffset = attributesOffset;
			}
			int a = 0;
			for (int x = 0; x < width; x++) {
				if ((x & 7) == 0) {
					if (x > 0 && s.readChar() != ' ')
						return false;
					a = s.readHexByte();
				}
				this.pixels[y * width + x] = this.contentPalette[(a >> 2 & 48) | (content[bitmapOffset++] != 49 ? 8 | (a >> 3 & 7) : a & 7)];
			}
			if (content[bitmapOffset] == 13)
				bitmapOffset++;
			if (content[bitmapOffset++] != 10 || s.readChar() != '\n')
				return false;
		}
		return true;
	}

	private boolean decodeBsc(byte[] content, int contentLength)
	{
		int borderOffset;
		switch (contentLength) {
		case 11136:
			borderOffset = 6912;
			break;
		case 11904:
			borderOffset = 7680;
			break;
		default:
			return false;
		}
		setSize(384, 304, RECOILResolution.SPECTRUM1X1);
		for (int y = 0; y < 304; y++) {
			int c = 0;
			for (int x = 0; x < 384; x++) {
				if (y >= 64 && y < 256 && x >= 64 && x < 320) {
					int bY = y - 64;
					int col = (x >> 3) - 8;
					int a = 6144 + (bY >> 3 << 5) + col;
					if (contentLength == 11904 && (bY & 4) != 0)
						a += 768;
					c = a = content[a] & 0xff;
					if (((content[getZxLineOffset(bY) + col] & 0xff) >> (~x & 7) & 1) == 0)
						c >>= 3;
					c = getZxColor(c);
					if ((a & 64) == 0)
						c &= 13487565;
				}
				else if ((x & 7) == 0) {
					c = content[borderOffset] & 0xff;
					if ((x & 8) != 0) {
						borderOffset++;
						c >>= 3;
					}
					c = getZxColor(c) & 13487565;
				}
				this.pixels[y * 384 + x] = c;
			}
		}
		return true;
	}

	private boolean decodeChrd(byte[] content, int contentLength)
	{
		if (contentLength < 15 || !isStringAt(content, 0, "chr$"))
			return false;
		int columns = content[4] & 0xff;
		int rows = content[5] & 0xff;
		int bytesPerCell = content[6] & 0xff;
		int cells = rows * columns;
		int frames;
		switch (bytesPerCell) {
		case 9:
			frames = 1;
			break;
		case 18:
			frames = 2;
			break;
		default:
			return false;
		}
		if (contentLength != 7 + cells * bytesPerCell || !setSize(columns << 3, rows << 3, RECOILResolution.SPECTRUM1X1, frames))
			return false;
		int contentOffset = 7;
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				for (int frame = 0; frame < frames; frame++) {
					int a = content[contentOffset + 8] & 0xff;
					for (int y = 0; y < 8; y++) {
						for (int x = 0; x < 8; x++) {
							int c = (content[contentOffset + y] & 0xff) >> (7 - x) & 1;
							c = getZxColor(c == 0 ? a >> 3 : a);
							if ((a & 64) == 0)
								c &= 13487565;
							this.pixels[(((((frame * rows + row) << 3) + y) * columns + column) << 3) + x] = c;
						}
					}
					contentOffset += 9;
				}
			}
		}
		if (frames == 2)
			applyBlend();
		return true;
	}

	private static int getBspBitmapPixel(byte[] content, int bitmapOffset, int x, int y)
	{
		int col = x >> 3;
		int a = content[bitmapOffset + 6144 + (y >> 3 << 5) + col] & 0xff;
		int c = a;
		if (((content[bitmapOffset + getZxLineOffset(y) + col] & 0xff) >> (~x & 7) & 1) == 0)
			c >>= 3;
		c = getZxColor(c);
		if ((a & 64) == 0)
			c &= 13487565;
		return c;
	}

	private boolean decodeBspFrame(int pixelsOffset, byte[] content, int contentLength, int bitmapOffset, int borderOffset)
	{
		for (int y = 0; y < this.height; y++) {
			int c = 0;
			int b = 1;
			for (int x = 0; x < this.width; x++) {
				if (borderOffset < 0)
					c = getBspBitmapPixel(content, bitmapOffset, x, y);
				else if (x >= 64 && x < 320 && y >= 64 && y < 256) {
					c = getBspBitmapPixel(content, bitmapOffset, x - 64, y - 64);
					b = 1;
				}
				else if (b > 0) {
					if (--b == 0) {
						if (borderOffset >= contentLength)
							return false;
						b = content[borderOffset++] & 0xff;
						c = getZxColor(b) & 13487565;
						b >>= 3;
						switch (b) {
						case 0:
							break;
						case 1:
							if (borderOffset >= contentLength)
								return false;
							b = content[borderOffset++] & 0xff;
							break;
						case 2:
							b = 12;
							break;
						default:
							b += 13;
							break;
						}
						b <<= 1;
					}
				}
				this.pixels[pixelsOffset + y * this.width + x] = c;
			}
		}
		return true;
	}

	private boolean decodeBsp(byte[] content, int contentLength)
	{
		if (contentLength < 6982)
			return false;
		if ((content[3] & 64) == 0) {
			if ((content[3] & 0xff) < 128) {
				setSize(256, 192, RECOILResolution.SPECTRUM1X1);
				return decodeBspFrame(0, content, contentLength, 70, -1);
			}
			return contentLength == 13894 && setSize(256, 192, RECOILResolution.SPECTRUM1X1, 2) && decodeBspFrame(0, content, contentLength, 70, -1) && decodeBspFrame(49152, content, contentLength, 6982, -1) && applyBlend();
		}
		if ((content[3] & 0xff) < 128) {
			setSize(384, 304, RECOILResolution.SPECTRUM1X1);
			return decodeBspFrame(0, content, contentLength, 70, 6982);
		}
		setSize(384, 304, RECOILResolution.SPECTRUM1X1, 2);
		return decodeBspFrame(0, content, contentLength, 72, 13896) && decodeBspFrame(116736, content, contentLength, 6984, content[70] & 0xff | (content[71] & 0xff) << 8) && applyBlend();
	}

	private boolean decodeNxi(byte[] content, int contentLength)
	{
		if (contentLength != 49664)
			return false;
		for (int i = 0; i < 256; i++) {
			int c = content[i << 1] & 0xff;
			this.contentPalette[i] = (c >> 5) * 73 >> 1 << 16 | (c >> 2 & 7) * 73 >> 1 << 8 | ((c & 3) << 1 | (content[(i << 1) + 1] & 1)) * 73 >> 1;
		}
		setSize(256, 192, RECOILResolution.SPECTRUM_NEXT1X1);
		decodeBytes(content, 512);
		return true;
	}

	private boolean decodeProfiGrf(byte[] content, int contentLength)
	{
		if (contentLength != 30848 || content[0] != 0 || content[1] != 2 || content[2] != -16 || content[3] != 0 || content[4] != 4 || content[5] != 0 || content[6] != -128 || content[7] != 0 || content[8] != 1 || content[9] != 19)
			return false;
		setSize(512, 480, RECOILResolution.SPECTRUM_PROFI1X2);
		for (int c = 0; c < 16; c++)
			this.contentPalette[c] = getG3R3B2Color(content[10 + c] & 0xff);
		for (int y = 0; y < 480; y++) {
			for (int x = 0; x < 512; x++) {
				int offset = 128 + (y >> 1 << 7) + (x >> 3 << 1);
				int b = (content[offset] & 0xff) >> (~x & 7) & 1;
				int a = content[offset + 1] & 0xff;
				this.pixels[(y << 9) + x] = this.contentPalette[b == 0 ? (a >> 4 & 8) | (a >> 3 & 7) : (a >> 3 & 8) | (a & 7)];
			}
		}
		return true;
	}

	private boolean decodeSxg(byte[] content, int contentLength)
	{
		if (contentLength < 19 || content[0] != 127 || content[1] != 83 || content[2] != 88 || content[3] != 71 || content[6] != 0)
			return false;
		int width = content[8] & 0xff | (content[9] & 0xff) << 8;
		int height = content[10] & 0xff | (content[11] & 0xff) << 8;
		if (!setSize(width, height, RECOILResolution.ZX_EVOLUTION1X1))
			return false;
		int paletteOffset = 14 + (content[12] & 0xff) + ((content[13] & 0xff) << 8);
		int bitmapOffset = 16 + (content[14] & 0xff) + ((content[15] & 0xff) << 8);
		int paletteLength = bitmapOffset - paletteOffset;
		if ((paletteLength & 1) != 0 || paletteLength > 512 || contentLength < paletteOffset + paletteLength)
			return false;
		Arrays.fill(this.contentPalette, 0);
		int colors = paletteLength >> 1;
		for (int i = 0; i < colors; i++) {
			int offset = paletteOffset + (i << 1);
			int c = content[offset] & 0xff | (content[offset + 1] & 0xff) << 8;
			if (c < 32768) {
				int r = c >> 10;
				int g = c >> 5 & 31;
				int b = c & 31;
				if (r > 24 || g > 24 || b > 24)
					return false;
				c = r * 255 / 24 << 16 | g * 255 / 24 << 8 | b * 255 / 24;
			}
			else
				c = getR5G5B5Color(c);
			this.contentPalette[i] = c;
		}
		switch (content[7]) {
		case 1:
			if ((width & 1) != 0 || contentLength != bitmapOffset + (width >> 1) * height)
				return false;
			decodeNibbles(content, bitmapOffset, width >> 1);
			return true;
		case 2:
			if (contentLength != bitmapOffset + width * height)
				return false;
			decodeBytes(content, bitmapOffset);
			return true;
		default:
			return false;
		}
	}

	private void setMsx1Palette()
	{
		this.contentPalette[0] = 2048;
		this.contentPalette[1] = 1024;
		this.contentPalette[2] = 3849027;
		this.contentPalette[3] = 7394167;
		this.contentPalette[4] = 5528023;
		this.contentPalette[5] = 8092648;
		this.contentPalette[6] = 11756363;
		this.contentPalette[7] = 6414311;
		this.contentPalette[8] = 13920851;
		this.contentPalette[9] = 16289399;
		this.contentPalette[10] = 13092697;
		this.contentPalette[11] = 14275713;
		this.contentPalette[12] = 3581243;
		this.contentPalette[13] = 11561902;
		this.contentPalette[14] = 13095109;
		this.contentPalette[15] = 16449528;
	}

	private static final byte[] MSX2_DEFAULT_PALETTE = { 0, 0, 0, 0, 17, 6, 51, 7, 23, 1, 39, 3, 81, 1, 39, 6,
		113, 1, 115, 3, 97, 6, 100, 6, 17, 4, 101, 2, 85, 5, 119, 7 };

	private static int getMsxHeader(byte[] content)
	{
		if (content[1] != 0 || content[2] != 0 || content[5] != 0 || content[6] != 0)
			return -1;
		return content[3] & 0xff | (content[4] & 0xff) << 8;
	}

	private static boolean isMsxPalette(byte[] content, int contentOffset)
	{
		int ored = 0;
		for (int i = 0; i < 16; i++) {
			int rb = content[contentOffset + (i << 1)] & 0xff;
			int g = content[contentOffset + (i << 1) + 1] & 0xff;
			if ((rb & 136) != 0 || (g & 248) != 0)
				return false;
			ored |= rb | g;
		}
		return ored != 0;
	}

	private void setMsxPalette(byte[] content, int contentOffset, int colors)
	{
		for (int i = 0; i < colors; i++) {
			int rb = content[contentOffset + (i << 1)] & 0xff;
			int g = content[contentOffset + (i << 1) + 1] & 0xff;
			int rgb = (rb & 112) << 12 | (rb & 7) | (g & 7) << 8;
			this.contentPalette[i] = rgb << 5 | rgb << 2 | (rgb >> 1 & 197379);
		}
	}

	private void setMsx2Palette(byte[] content, int contentOffset, int contentLength)
	{
		if (contentLength >= contentOffset + 32)
			setMsxPalette(content, contentOffset, 16);
		else
			setMsxPalette(MSX2_DEFAULT_PALETTE, 0, 16);
	}

	private void decodeSc2Sc4(byte[] content, int contentOffset, int resolution)
	{
		setSize(256, 192, resolution);
		for (int y = 0; y < 192; y++) {
			int fontOffset = contentOffset + ((y & 192) << 5) + (y & 7);
			for (int x = 0; x < 256; x++) {
				int b = fontOffset + ((content[contentOffset + 6144 + ((y & -8) << 2) + (x >> 3)] & 0xff) << 3);
				int c = content[8192 + b] & 0xff;
				this.pixels[(y << 8) + x] = this.contentPalette[((content[b] & 0xff) >> (~x & 7) & 1) == 0 ? c & 15 : c >> 4];
			}
		}
	}

	private void decodeMsxSprites(byte[] content, int mode, int attributesOffset, int patternsOffset)
	{
		int height = mode <= 4 ? 192 : 212;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < 256; x++) {
				int color = 0;
				boolean enableOr = false;
				int spritesPerLine = mode >= 4 ? 8 : 4;
				for (int sprite = 0; sprite < 32; sprite++) {
					int attributeOffset = attributesOffset + (sprite << 2);
					int spriteY = content[attributeOffset] & 0xff;
					if (spriteY == (mode >= 4 ? 216 : 208))
						break;
					int row = (y - spriteY - 1) & 255;
					if (row >= 16)
						continue;
					if (--spritesPerLine < 0)
						break;
					int c = content[mode >= 4 ? attributesOffset - 512 + (sprite << 4) + row : attributeOffset + 3] & 0xff;
					if (mode < 4 || (c & 64) == 0) {
						if (color != 0)
							break;
						enableOr = true;
					}
					else if (!enableOr)
						continue;
					int column = x - (content[attributeOffset + 1] & 0xff);
					if (c >= 128)
						column += 32;
					if (column < 0 || column >= 16)
						continue;
					int pattern = patternsOffset + ((content[attributeOffset + 2] & 252) << 3) + row;
					if (((content[pattern + ((column & 8) << 1)] & 0xff) >> (~column & 7) & 1) == 0)
						continue;
					color |= c;
					if (mode >= 4)
						color |= 16;
				}
				if (color != 0) {
					int offset;
					switch (mode) {
					case 6:
						offset = (y << 10) + (x << 1);
						this.pixels[offset + 512] = this.pixels[offset] = this.contentPalette[color >> 2 & 3];
						this.pixels[offset + 513] = this.pixels[offset + 1] = this.contentPalette[color & 3];
						break;
					case 7:
						offset = (y << 10) + (x << 1);
						this.pixels[offset + 513] = this.pixels[offset + 512] = this.pixels[offset + 1] = this.pixels[offset] = this.contentPalette[color & 15];
						break;
					default:
						this.pixels[(y << 8) + x] = this.contentPalette[color & 15];
						break;
					}
				}
			}
		}
	}

	private boolean decodeSc2(byte[] content, int contentLength)
	{
		if (contentLength < 14343 || content[0] != -2 || getMsxHeader(content) < 14335)
			return false;
		if (isMsxPalette(content, 7047)) {
			setMsxPalette(content, 7047, 16);
			decodeSc2Sc4(content, 7, RECOILResolution.MSX21X1);
		}
		else {
			setMsx1Palette();
			decodeSc2Sc4(content, 7, RECOILResolution.MSX11X1);
		}
		if (contentLength == 16391)
			decodeMsxSprites(content, 2, 6919, 14343);
		return true;
	}

	private void decodeSc3Screen(byte[] content, int contentOffset, boolean isLong)
	{
		setSize(256, 192, RECOILResolution.MSX14X4);
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x++) {
				int c = isLong ? content[2055 + ((y & -8) << 2) + (x >> 3)] & 0xff : (y & 224) + (x >> 3);
				c = (content[contentOffset + (c << 3) + (y >> 2 & 7)] & 0xff) >> (~x & 4) & 15;
				this.pixels[(y << 8) + x] = this.contentPalette[c];
			}
		}
	}

	private boolean decodeSc3(byte[] content, int contentLength)
	{
		if (contentLength < 1543 || content[0] != -2 || getMsxHeader(content) < 1535)
			return false;
		if (contentLength >= 8263 && isMsxPalette(content, 8231))
			setMsxPalette(content, 8231, 16);
		else
			setMsx1Palette();
		decodeSc3Screen(content, 7, contentLength >= 2823);
		if (contentLength == 16391)
			decodeMsxSprites(content, 3, 6919, 14343);
		return true;
	}

	private boolean decodeSc4(byte[] content, int contentLength)
	{
		if (contentLength < 14343 || content[0] != -2 || getMsxHeader(content) < 14335)
			return false;
		if (isMsxPalette(content, 7047))
			setMsxPalette(content, 7047, 16);
		else
			setMsxPalette(MSX2_DEFAULT_PALETTE, 0, 16);
		decodeSc2Sc4(content, 7, RECOILResolution.MSX21X1);
		if (contentLength >= 16391)
			decodeMsxSprites(content, 4, 7687, 14343);
		return true;
	}

	private int getMsx128Height(byte[] content, int contentLength)
	{
		if (contentLength < 135 || content[0] != -2)
			return -1;
		int header = getMsxHeader(content);
		if (header < 127)
			return -1;
		int height = (header + 1) >> 7;
		if (contentLength < 7 + (height << 7))
			return -1;
		return height < 212 ? height : 212;
	}

	private void decodeMsxScreen(byte[] content, int contentOffset, byte[] interlace, int height, int mode, int interlaceMask)
	{
		if (interlaceMask != 0) {
			setSize(512, height << 1, mode >= 10 ? RECOILResolution.MSX2_PLUS2X1I : mode >> 1 == 3 ? RECOILResolution.MSX21X1I : RECOILResolution.MSX22X1I);
			this.frames = 2;
		}
		else if (mode >> 1 == 3)
			setSize(512, height << 1, RECOILResolution.MSX21X2);
		else
			setSize(256, height, mode >= 10 ? RECOILResolution.MSX2_PLUS1X1 : RECOILResolution.MSX21X1);
		for (int y = 0; y < this.height; y++) {
			byte[] screen = (y & interlaceMask) == 0 ? content : interlace;
			int screenOffset = (y & interlaceMask) == 0 || content != interlace ? contentOffset : contentOffset + (mode <= 6 ? 27143 : 54279);
			for (int x = 0; x < this.width; x++) {
				int rgb = 0;
				switch (mode) {
				case 5:
					rgb = this.contentPalette[getNibble(screen, screenOffset + (y >> interlaceMask << 7), x >> interlaceMask)];
					break;
				case 6:
					rgb = this.contentPalette[(screen[screenOffset + (y >> 1 << 7) + (x >> 2)] & 0xff) >> ((~x & 3) << 1) & 3];
					break;
				case 7:
					rgb = this.contentPalette[getNibble(screen, screenOffset + (y >> 1 << 8), x)];
					break;
				case 8:
					rgb = this.contentPalette[screen[screenOffset + (y >> interlaceMask << 8) + (x >> interlaceMask)] & 0xff];
					break;
				case 10:
					rgb = decodeMsxYjk(screen, screenOffset + (y >> interlaceMask << 8), x >> interlaceMask, true);
					break;
				case 12:
					rgb = decodeMsxYjk(screen, screenOffset + (y >> interlaceMask << 8), x >> interlaceMask, false);
					break;
				default:
					throw new AssertionError();
				}
				this.pixels[y * this.width + x] = rgb;
			}
		}
	}

	private boolean decodeMsxSc(String filename, byte[] content, int contentOffset, String upperExt, String lowerExt, int height, int mode)
	{
		if (filename != null) {
			final byte[] interlace = new byte[54279];
			int interlaceLength = 7 + (height << (mode <= 6 ? 7 : 8));
			if (readCompanionFile(filename, upperExt, lowerExt, interlace, interlaceLength) == interlaceLength && interlace[0] == -2 && getMsxHeader(interlace) >= interlaceLength - 8) {
				decodeMsxScreen(content, contentOffset, interlace, height, mode, 1);
				return true;
			}
		}
		decodeMsxScreen(content, contentOffset, null, height, mode, 0);
		return false;
	}

	private boolean decodeSc5(String filename, byte[] content, int contentLength)
	{
		int height = getMsx128Height(content, contentLength);
		if (height <= 0)
			return false;
		setMsx2Palette(content, 30343, contentLength);
		if (!decodeMsxSc(filename, content, 7, "S15", "s15", height, 5) && contentLength == 32775)
			decodeMsxSprites(content, 5, 30215, 30727);
		return true;
	}

	private void setMsxCompanionPalette(String filename, String upperExt, String lowerExt)
	{
		final byte[] palette = new byte[32];
		setMsxPalette(readCompanionFile(filename, upperExt, lowerExt, palette, 32) == 32 ? palette : MSX2_DEFAULT_PALETTE, 0, 16);
	}

	private boolean decodeSr5(String filename, byte[] content, int contentLength)
	{
		int height = getMsx128Height(content, contentLength);
		if (height <= 0)
			return false;
		setMsxCompanionPalette(filename, "PL5", "pl5");
		setSize(256, height, RECOILResolution.MSX21X1);
		decodeNibbles(content, 7, 128);
		return true;
	}

	private boolean decodeMsx6(byte[] content, int contentOffset)
	{
		int height = getOriginalHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < this.width; x++) {
				int offset = y * this.width + x;
				int b = content[contentOffset + (offset >> 2)] & 0xff;
				setScaledPixel(x, y, this.contentPalette[b >> ((~offset & 3) << 1) & 3]);
			}
		}
		return true;
	}

	private void setMsx6DefaultPalette()
	{
		this.contentPalette[0] = 0;
		this.contentPalette[1] = 2396708;
		this.contentPalette[2] = 2415396;
		this.contentPalette[3] = 7208813;
	}

	private boolean decodeSc6(String filename, byte[] content, int contentLength)
	{
		int height = getMsx128Height(content, contentLength);
		if (height <= 0)
			return false;
		if (contentLength >= 30351)
			setMsxPalette(content, 30343, 4);
		else
			setMsx6DefaultPalette();
		if (!decodeMsxSc(filename, content, 7, "S16", "s16", height, 6) && contentLength == 32775)
			decodeMsxSprites(content, 6, 30215, 30727);
		return true;
	}

	private void setMsx6Palette(String filename)
	{
		final byte[] palette = new byte[8];
		if (readCompanionFile(filename, "PL6", "pl6", palette, 8) == 8)
			setMsxPalette(palette, 0, 4);
		else
			setMsx6DefaultPalette();
	}

	private boolean decodeSr6(String filename, byte[] content, int contentLength)
	{
		int height = getMsx128Height(content, contentLength);
		if (height <= 0)
			return false;
		setSize(512, height << 1, RECOILResolution.MSX21X2);
		setMsx6Palette(filename);
		return decodeMsx6(content, 7);
	}

	private boolean decodeGl6(String filename, byte[] content, int contentLength)
	{
		if (contentLength < 5)
			return false;
		int width = content[0] & 0xff | (content[1] & 0xff) << 8;
		int height = content[2] & 0xff | (content[3] & 0xff) << 8;
		if (contentLength < 4 + ((width * height + 3) >> 2) || !setSize(width, height << 1, RECOILResolution.MSX21X2))
			return false;
		if (filename != null)
			setMsx6Palette(filename);
		else {
			this.contentPalette[0] = 16777215;
			this.contentPalette[1] = 0;
			this.contentPalette[3] = this.contentPalette[2] = 0;
		}
		return decodeMsx6(content, 4);
	}

	private static byte[] unpackSr(byte[] content, int contentLength, byte[] unpacked)
	{
		if (contentLength < 7)
			return null;
		switch (content[0]) {
		case -2:
			if (contentLength < 54279 || getMsxHeader(content) < 54271)
				return null;
			return content;
		case -3:
			if (7 + getMsxHeader(content) != contentLength)
				return null;
			final SrStream rle = new SrStream();
			rle.content = content;
			rle.contentOffset = 7;
			rle.contentLength = contentLength;
			rle.unpack(unpacked, 7, 1, 54279);
			return unpacked;
		default:
			return null;
		}
	}

	private boolean decodeSc7(String filename, byte[] content, int contentLength)
	{
		if (contentLength < 54279 || content[0] != -2 || getMsxHeader(content) < 54271)
			return false;
		setMsx2Palette(content, 64135, contentLength);
		if (!decodeMsxSc(filename, content, 7, "S17", "s17", 212, 7) && contentLength == 64167)
			decodeMsxSprites(content, 7, 64007, 61447);
		return true;
	}

	private boolean decodeSri(String filename, byte[] content, int contentLength)
	{
		if (contentLength != 108544)
			return false;
		setMsxCompanionPalette(filename, "PL7", "pl7");
		setSize(512, 424, RECOILResolution.MSX21X1I);
		this.frames = 2;
		decodeNibbles(content, 0, 256);
		return true;
	}

	private boolean decodeSr7(String filename, byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[54279];
		content = unpackSr(content, contentLength, unpacked);
		if (content == null)
			return false;
		setMsxCompanionPalette(filename, "PL7", "pl7");
		setSize(512, 424, RECOILResolution.MSX21X2);
		decodeNibbles(content, 7, 256);
		return true;
	}

	private boolean decodeGl16(String filename, byte[] content, int contentLength, int resolution, String upperExt, String lowerExt)
	{
		if (contentLength < 5)
			return false;
		int width = content[0] & 0xff | (content[1] & 0xff) << 8;
		int height = content[2] & 0xff | (content[3] & 0xff) << 8;
		if (contentLength < 4 + ((width * height + 1) >> 1) || !setScaledSize(width, height, resolution))
			return false;
		setMsxCompanionPalette(filename, upperExt, lowerExt);
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				setScaledPixel(x, y, this.contentPalette[getNibble(content, 4, y * width + x)]);
		return true;
	}

	private boolean decodeGl5(String filename, byte[] content, int contentLength)
	{
		return decodeGl16(filename, content, contentLength, RECOILResolution.MSX21X1, "PL5", "pl5");
	}

	private boolean decodeGl7(String filename, byte[] content, int contentLength)
	{
		return decodeGl16(filename, content, contentLength, RECOILResolution.MSX21X2, "PL7", "pl7");
	}

	private void setSc8Palette()
	{
		for (int c = 0; c < 256; c++) {
			int rgb = (c & 28) << 14 | (c & 224) << 3 | SET_SC8_PALETTE_BLUES[c & 3] & 0xff;
			this.contentPalette[c] = rgb << 5 | rgb << 2 | (rgb >> 1 & 197379);
		}
	}

	private boolean decodeSc8(String filename, byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[54279];
		content = unpackSr(content, contentLength, unpacked);
		if (content == null)
			return false;
		setSc8Palette();
		if (!decodeMsxSc(filename, content, 7, "S18", "s18", 212, 8) && contentLength == 64167) {
			setMsxPalette(DECODE_SC8_SPRITE_PALETTE, 0, 16);
			decodeMsxSprites(content, 8, 64007, 61447);
		}
		return true;
	}

	private boolean decodeGl8(byte[] content, int contentLength)
	{
		if (contentLength < 5)
			return false;
		int width = content[0] & 0xff | (content[1] & 0xff) << 8;
		int height = content[2] & 0xff | (content[3] & 0xff) << 8;
		if (contentLength != 4 + width * height || !setSize(width, height, RECOILResolution.MSX21X1))
			return false;
		setSc8Palette();
		decodeBytes(content, 4);
		return true;
	}

	private boolean decodePct(byte[] content, int contentLength)
	{
		if (contentLength < 384 || (!isStringAt(content, 0, "DYNAMIC") && !isStringAt(content, 0, "E U R O")) || !isStringAt(content, 7, " PUBLISHER "))
			return false;
		int height;
		int contentOffset;
		if (isStringAt(content, 18, "SCREEN")) {
			height = 704;
			contentOffset = 384;
		}
		else if (isStringAt(content, 18, "FONT")) {
			height = 160;
			contentOffset = 512;
		}
		else
			return false;
		setSize(512, height << 1, RECOILResolution.MSX21X2);
		final CciStream rle = new CciStream();
		rle.content = content;
		rle.contentOffset = contentOffset;
		rle.contentLength = contentLength;
		for (int y = 0; y < height; y++) {
			int b = 0;
			for (int x = 0; x < 512; x++) {
				if ((x & 7) == 0) {
					b = rle.readRle();
					if (b < 0)
						return false;
				}
				int offset = (y << 10) + x;
				this.pixels[offset + 512] = this.pixels[offset] = (b >> ((x ^ 3) & 7) & 1) == 0 ? 16777215 : 0;
			}
		}
		return true;
	}

	private boolean decodeDdGraph(String filename, byte[] content, int contentLength)
	{
		if (contentLength < 4)
			return false;
		int width = content[0] & 0xff;
		if (width == 0 || width > 128)
			return false;
		int height = content[1] & 0xff;
		if (height == 0 || height > 212)
			return false;
		int verbatimLines = content[2] & 0xff;
		if (verbatimLines == 0 || verbatimLines > height)
			return false;
		final ZimStream stream = new ZimStream();
		stream.content = content;
		stream.contentOffset = 3;
		stream.contentLength = contentLength;
		int flagsOffset = 64;
		final byte[] unpacked = new byte[27136];
		int unpackedOffset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (flagsOffset >= 64) {
					if (!stream.unpackFlags2())
						return false;
					flagsOffset = 0;
				}
				int b = stream.readUnpacked(stream.flags2, flagsOffset++);
				if (b < 0)
					return false;
				if (y >= verbatimLines)
					b ^= unpacked[unpackedOffset - width] & 0xff;
				unpacked[unpackedOffset++] = (byte) b;
			}
		}
		if (stream.contentOffset != stream.contentLength)
			return false;
		setMsxCompanionPalette(filename, "PL5", "pl5");
		setSize(width << 1, height, RECOILResolution.MSX21X1);
		decodeNibbles(unpacked, 0, width);
		return true;
	}

	private int decodeMsxYjk(byte[] content, int contentOffset, int x, boolean usePalette)
	{
		int y = (content[contentOffset + x] & 0xff) >> 3;
		if (usePalette && (y & 1) != 0)
			return this.contentPalette[y >> 1];
		int rgb;
		if ((x | 3) >= this.width) {
			rgb = y * 65793;
		}
		else {
			x = contentOffset + (x & -4);
			int k = (content[x] & 7) | (content[x + 1] & 7) << 3;
			int j = (content[x + 2] & 7) | (content[x + 3] & 7) << 3;
			k -= (k & 32) << 1;
			j -= (j & 32) << 1;
			int r = y + j;
			if (r < 0)
				r = 0;
			else if (r > 31)
				r = 31;
			int g = y + k;
			if (g < 0)
				g = 0;
			else if (g > 31)
				g = 31;
			int b = (((5 * y - k) >> 1) - j) >> 1;
			if (b < 0)
				b = 0;
			else if (b > 31)
				b = 31;
			rgb = r << 16 | g << 8 | b;
		}
		return rgb << 3 | (rgb >> 2 & 460551);
	}

	private void decodeMsxYjkScreen(byte[] content, int contentOffset, boolean usePalette)
	{
		int width = getOriginalWidth();
		for (int y = 0; y < this.height; y++)
			for (int x = 0; x < width; x++)
				setScaledPixel(x, y, decodeMsxYjk(content, contentOffset + y * width, x, usePalette));
	}

	private void decodeSccSca(String filename, byte[] content, int contentLength, int height, boolean usePalette)
	{
		if (!decodeMsxSc(filename, content, 7, usePalette ? "S1A" : "S1C", usePalette ? "s1a" : "s1c", height, usePalette ? 10 : 12) && contentLength == 64167 && content[0] == -2) {
			setMsxPalette(content, 64135, 16);
			decodeMsxSprites(content, 12, 64007, 61447);
		}
	}

	private boolean decodeScc(String filename, byte[] content, int contentLength)
	{
		int height;
		if (contentLength >= 49159 && content[0] == -2 && getMsxHeader(content) == 49151)
			height = 192;
		else {
			final byte[] unpacked = new byte[54279];
			content = unpackSr(content, contentLength, unpacked);
			if (content == null)
				return false;
			height = 212;
		}
		decodeSccSca(filename, content, contentLength, height, false);
		return true;
	}

	private boolean decodeSca(String filename, byte[] content, int contentLength)
	{
		if (contentLength < 64167 || content[0] != -2 || getMsxHeader(content) < 54271)
			return false;
		setMsxPalette(content, 64135, 16);
		decodeSccSca(filename, content, contentLength, 212, true);
		return true;
	}

	private boolean decodeGlYjk(String filename, byte[] content, int contentLength)
	{
		if (contentLength < 8)
			return false;
		int width = content[0] & 0xff | (content[1] & 0xff) << 8;
		int height = content[2] & 0xff | (content[3] & 0xff) << 8;
		if (contentLength != 4 + width * height || !setSize(width, height, RECOILResolution.MSX2_PLUS1X1))
			return false;
		if (filename != null)
			setMsxCompanionPalette(filename, "PLA", "pla");
		decodeMsxYjkScreen(content, 4, filename != null);
		return true;
	}

	private boolean setG9bPalette(byte[] content, int colors)
	{
		for (int c = 0; c < colors; c++) {
			int offset = 16 + c * 3;
			int rgb = (content[offset] & 0xff) << 16 | (content[offset + 1] & 0xff) << 8 | content[offset + 2] & 0xff;
			if ((rgb & 14737632) != 0)
				return false;
			this.contentPalette[c] = rgb << 3 | (rgb >> 2 & 460551);
		}
		return true;
	}

	private static final int G9B_YUV = 0;

	private void decodeG9bUnpacked(byte[] content, int depth)
	{
		int pixelsLength;
		switch (depth) {
		case 4:
			decodeNibbles(content, 64, (this.width + 1) >> 1);
			break;
		case 8:
			decodeBytes(content, 208);
			break;
		case 0:
			pixelsLength = this.width * this.height;
			for (int i = 0; i < pixelsLength; i++) {
				int y = (content[16 + i] & 0xff) >> 3;
				int x = 16 + (i & -4);
				int v = (content[x] & 7) | (content[x + 1] & 7) << 3;
				int u = (content[x + 2] & 7) | (content[x + 3] & 7) << 3;
				u -= (u & 32) << 1;
				v -= (v & 32) << 1;
				int r = y + u;
				if (r < 0)
					r = 0;
				else if (r > 31)
					r = 31;
				int g = (((5 * y - v) >> 1) - u) >> 1;
				if (g < 0)
					g = 0;
				else if (g > 31)
					g = 31;
				int b = y + v;
				if (b < 0)
					b = 0;
				else if (b > 31)
					b = 31;
				int rgb = r << 16 | g << 8 | b;
				this.pixels[i] = rgb << 3 | (rgb >> 2 & 460551);
			}
			break;
		case 16:
			pixelsLength = this.width * this.height;
			for (int i = 0; i < pixelsLength; i++) {
				int c = content[16 + (i << 1)] & 0xff | (content[17 + (i << 1)] & 0xff) << 8;
				int rgb = (c & 992) << 14 | (c & 31744) << 1 | (c & 31) << 3;
				this.pixels[i] = rgb | (rgb >> 5 & 460551);
			}
			break;
		default:
			throw new AssertionError();
		}
	}

	private boolean decodeG9b(byte[] content, int contentLength)
	{
		if (contentLength < 17 || content[0] != 71 || content[1] != 57 || content[2] != 66 || content[3] != 11 || content[4] != 0)
			return false;
		int depth = content[5] & 0xff;
		int headerLength = 16 + (content[7] & 0xff) * 3;
		int width = content[8] & 0xff | (content[9] & 0xff) << 8;
		int height = content[10] & 0xff | (content[11] & 0xff) << 8;
		if (contentLength <= headerLength || !setSize(width, height, RECOILResolution.MSX_V99901X1))
			return false;
		int unpackedLength = headerLength + ((width * depth + 7) >> 3) * height;
		switch (depth) {
		case 4:
			if (content[7] != 16 || !setG9bPalette(content, 16))
				return false;
			break;
		case 8:
			switch (content[7]) {
			case 0:
				if (content[6] != -64 || (width & 3) != 0)
					return false;
				depth = 0;
				break;
			case 64:
				if (!setG9bPalette(content, 64))
					return false;
				Arrays.fill(this.contentPalette, 64, 256, 0);
				break;
			default:
				return false;
			}
			break;
		case 16:
			break;
		default:
			return false;
		}
		switch (content[12]) {
		case 0:
			if (contentLength != unpackedLength)
				return false;
			decodeG9bUnpacked(content, depth);
			return true;
		case 1:
			byte[] unpacked = new byte[unpackedLength];
			final G9bStream s = new G9bStream();
			s.content = content;
			s.contentLength = contentLength;
			boolean ok = s.unpack(unpacked, headerLength, unpackedLength);
			if (ok)
				decodeG9bUnpacked(unpacked, depth);
			return ok;
		default:
			return false;
		}
	}

	private static int getMigMode(int reg0, int reg1, int reg19, int length)
	{
		return (reg0 & 14) | (reg1 & 24) << 1 | (reg19 & 24) << 3 | length << 8;
	}

	private boolean decodeMig(byte[] content, int contentLength)
	{
		if (contentLength < 16 || !isStringAt(content, 0, "MSXMIG") || get32LittleEndian(content, 6) != contentLength - 6)
			return false;
		final byte[] unpacked = new byte[108800];
		final MigStream s = new MigStream();
		s.content = content;
		s.contentLength = contentLength;
		int unpackedLength = s.unpack(unpacked);
		int colors = 0;
		final byte[] registers = new byte[256];
		for (int unpackedOffset = 0; unpackedOffset < unpackedLength;) {
			switch (unpacked[unpackedOffset]) {
			case 0:
				if (unpackedOffset + 1 >= unpackedLength)
					return false;
				int c = unpacked[unpackedOffset + 1] & 0xff;
				if (unpackedOffset + 2 + c * 3 > unpackedLength)
					return false;
				for (int i = 0; i < c; i++) {
					int offset = unpackedOffset + 2 + i * 3;
					int r = unpacked[offset] & 0xff;
					int m = unpacked[offset + 2] & 0xff;
					registers[r] = (byte) ((registers[r] & 0xff & ~m) | (unpacked[offset + 1] & 0xff & m));
				}
				unpackedOffset += 2 + c * 3;
				break;
			case 1:
				if (unpackedOffset + 2 >= unpackedLength || unpacked[unpackedOffset + 1] != 0)
					return false;
				colors = unpacked[unpackedOffset + 2] & 0xff;
				if (unpackedOffset + 3 + (colors << 1) > unpackedLength)
					return false;
				setMsxPalette(unpacked, unpackedOffset + 3, colors);
				unpackedOffset += 3 + (colors << 1);
				break;
			case 2:
				if (unpackedOffset + 7 >= unpackedLength || unpacked[unpackedOffset + 1] != 0 || unpacked[unpackedOffset + 2] != 0 || unpacked[unpackedOffset + 3] != 0 || unpacked[unpackedOffset + 4] != 0 || unpacked[unpackedOffset + 6] != 0)
					return false;
				int length = unpacked[unpackedOffset + 5] & 0xff;
				unpackedOffset += 7;
				int interlaceMask;
				switch (registers[9] & 12) {
				case 0:
					if (unpackedOffset + (length << 8) + 1 != unpackedLength)
						return false;
					interlaceMask = 0;
					break;
				case 12:
					if (unpackedOffset + (length << 9) + 7 + 1 != unpackedLength || unpacked[unpackedOffset + (length << 8)] != 2 || unpacked[unpackedOffset + (length << 8) + 1] != 0 || unpacked[unpackedOffset + (length << 8) + 4] != 0 || (unpacked[unpackedOffset + (length << 8) + 5] & 0xff) != length || unpacked[unpackedOffset + (length << 8) + 6] != 0)
						return false;
					interlaceMask = 1;
					break;
				default:
					return false;
				}
				int mode;
				switch (getMigMode(registers[0] & 0xff, registers[1] & 0xff, registers[25] & 0xff, length)) {
				case 14338:
					if (colors < 16 || interlaceMask != 0)
						return false;
					decodeSc2Sc4(unpacked, unpackedOffset, RECOILResolution.MSX21X1);
					return true;
				case 1552:
					if (colors < 16 || interlaceMask != 0)
						return false;
					decodeSc3Screen(unpacked, unpackedOffset, false);
					return true;
				case 27142:
					if (colors < 16)
						return false;
					mode = 5;
					break;
				case 27144:
					if (colors < 4)
						return false;
					mode = 6;
					break;
				case 54282:
					if (colors < 16)
						return false;
					mode = 7;
					break;
				case 54286:
					setSc8Palette();
					mode = 8;
					break;
				case 54478:
					if (colors < 16)
						return false;
					mode = 10;
					break;
				case 54350:
					mode = 12;
					break;
				default:
					return false;
				}
				decodeMsxScreen(unpacked, unpackedOffset, unpacked, 212, mode, interlaceMask);
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	private boolean decodeHgr(byte[] content, int contentLength)
	{
		if (contentLength < 8184)
			return false;
		setSize(280, 192, RECOILResolution.APPLE_I_I1X1);
		for (int y = 0; y < 192; y++) {
			int lineOffset = (y & 7) << 10 | (y & 56) << 4 | (y >> 6) * 40;
			for (int x = 0; x < 280; x++) {
				int c = (content[lineOffset + x / 7] & 0xff) >> x % 7 & 1;
				if (c != 0)
					c = 16777215;
				this.pixels[y * 280 + x] = c;
			}
		}
		return true;
	}

	private boolean decodeAppleIIDhr(byte[] content, int contentLength)
	{
		if (contentLength != 16384)
			return false;
		setSize(560, 384, RECOILResolution.APPLE_I_IE1X2);
		for (int y = 0; y < 192; y++) {
			int lineOffset = (y & 7) << 10 | (y & 56) << 4 | (y >> 6) * 40;
			for (int x = 0; x < 560; x++) {
				int b = x / 7;
				int c = (content[((b & 1) << 13) + lineOffset + (b >> 1)] & 0xff) >> x % 7 & 1;
				if (c != 0)
					c = 16777215;
				int pixelsOffset = y * 1120 + x;
				this.pixels[pixelsOffset + 560] = this.pixels[pixelsOffset] = c;
			}
		}
		return true;
	}

	private void setAppleIIGSPalette(byte[] content, int contentOffset, int reverse)
	{
		for (int c = 0; c < 16; c++) {
			int offset = contentOffset + ((c ^ reverse) << 1);
			int gb = content[offset] & 0xff;
			int r = content[offset + 1] & 15;
			int g = gb >> 4;
			int b = gb & 15;
			int rgb = r << 16 | g << 8 | b;
			rgb |= rgb << 4;
			this.contentPalette[c] = rgb;
		}
	}

	private void decodeShrLine(byte[] content, int y)
	{
		for (int x = 0; x < 320; x++)
			this.pixels[y * 320 + x] = this.contentPalette[getNibble(content, y * 160, x)];
	}

	private boolean decodeAppleIIShrUnpacked(byte[] content)
	{
		setSize(320, 200, RECOILResolution.APPLE_I_I_G_S1X1);
		for (int y = 0; y < 200; y++) {
			setAppleIIGSPalette(content, 32256 + ((content[32000 + y] & 15) << 5), 0);
			decodeShrLine(content, y);
		}
		return true;
	}

	private boolean decodeAppleIIShr(byte[] content, int contentLength)
	{
		if (contentLength == 32768)
			return decodeAppleIIShrUnpacked(content);
		final byte[] unpacked = new byte[32768];
		final PackBytesStream stream = new PackBytesStream();
		stream.content = content;
		stream.contentOffset = 0;
		stream.contentLength = contentLength;
		for (int unpackedOffset = 0; unpackedOffset < 32768; unpackedOffset++) {
			int b = stream.readUnpacked();
			if (b < 0)
				return false;
			unpacked[unpackedOffset] = (byte) b;
		}
		return stream.readUnpacked() < 0 && decodeAppleIIShrUnpacked(unpacked);
	}

	private boolean decodeSh3(byte[] content, int contentLength)
	{
		if (contentLength != 38400)
			return false;
		setSize(320, 200, RECOILResolution.APPLE_I_I_G_S1X1);
		for (int y = 0; y < 200; y++) {
			setAppleIIGSPalette(content, 32000 + (y << 5), 15);
			decodeShrLine(content, y);
		}
		return true;
	}

	private void drawSprByte(int x1, int y, int b)
	{
		for (int x = 0; x < 8; x++) {
			if ((b >> (7 - x) & 1) != 0)
				this.pixels[y * 320 + x1 + x] = 16777215;
		}
	}

	private boolean decodeAppleSpr(byte[] content, int contentLength)
	{
		setSize(320, 200, RECOILResolution.APPLE_I_I1X1);
		Arrays.fill(this.pixels, 0, 64000, 0);
		final AppleSprStream s = new AppleSprStream();
		s.content = content;
		s.contentOffset = 0;
		s.contentLength = contentLength;
		for (;;) {
			int cols = s.readSprInt();
			if (cols < 0)
				return false;
			int rows = s.readSprInt();
			if (rows < 0)
				return false;
			int order = s.readSprInt();
			if (order < 0)
				return false;
			int x = s.readSprInt();
			if (x < 0)
				return false;
			int y = s.readSprInt();
			if (y < 0)
				return false;
			if (rows == 0)
				break;
			if (cols == 0 || x + (cols << 3) > 320 || y + rows > 200)
				return false;
			if (order == 2) {
				for (int col = 0; col < cols; col++) {
					for (int row = 0; row < rows; row++) {
						int b = s.readSprInt();
						if (b < 0)
							return false;
						drawSprByte(x + (col << 3), y + row, b);
					}
				}
			}
			else {
				for (int row = 0; row < rows; row++) {
					for (int col = 0; col < cols; col++) {
						int b = s.readSprInt();
						if (b < 0)
							return false;
						drawSprByte(x + (col << 3), y + row, b);
					}
				}
			}
		}
		return true;
	}

	private boolean decodePackBytes(PackBytesStream stream, int pixelsOffset, int unpackedBytes)
	{
		for (int x = 0; x < unpackedBytes; x++) {
			int b = stream.readUnpacked();
			if (b < 0)
				return false;
			if (this.resolution == RECOILResolution.APPLE_I_I_G_S1X2) {
				int offset = (pixelsOffset << 1) + (x << 2);
				this.pixels[offset + this.width] = this.pixels[offset] = this.contentPalette[8 + (b >> 6)];
				this.pixels[offset + this.width + 1] = this.pixels[offset + 1] = this.contentPalette[12 + (b >> 4 & 3)];
				this.pixels[offset + this.width + 2] = this.pixels[offset + 2] = this.contentPalette[b >> 2 & 3];
				this.pixels[offset + this.width + 3] = this.pixels[offset + 3] = this.contentPalette[4 + (b & 3)];
			}
			else {
				this.pixels[pixelsOffset + (x << 1)] = this.contentPalette[b >> 4];
				this.pixels[pixelsOffset + (x << 1) + 1] = this.contentPalette[b & 15];
			}
		}
		return true;
	}

	private boolean decodePaintworks(byte[] content, int contentLength)
	{
		if (contentLength < 1041)
			return false;
		setSize(320, 396, RECOILResolution.APPLE_I_I_G_S1X1);
		setAppleIIGSPalette(content, 0, 0);
		final PackBytesStream stream = new PackBytesStream();
		stream.content = content;
		stream.contentOffset = 546;
		stream.contentLength = contentLength;
		return decodePackBytes(stream, 0, 63360);
	}

	private boolean decode3201(byte[] content, int contentLength)
	{
		if (contentLength < 6654 || content[0] != -63 || content[1] != -48 || content[2] != -48 || content[3] != 0)
			return false;
		setSize(320, 200, RECOILResolution.APPLE_I_I_G_S1X1);
		final PackBytesStream stream = new PackBytesStream();
		stream.content = content;
		stream.contentOffset = 6404;
		stream.contentLength = contentLength;
		for (int y = 0; y < 200; y++) {
			setAppleIIGSPalette(content, 4 + (y << 5), 15);
			if (!decodePackBytes(stream, y * 320, 160))
				return false;
		}
		return true;
	}

	private boolean decodeApfShr(byte[] content, int contentLength)
	{
		if (contentLength < 1249 || content[4] != 4 || !isStringAt(content, 5, "MAIN") || content[14] != 0)
			return false;
		int paletteCount = content[13] & 0xff;
		if (paletteCount > 16)
			return false;
		int dirOffset = 17 + (paletteCount << 5);
		if (dirOffset >= contentLength)
			return false;
		int mode = content[9] & 240;
		int width = content[11] & 0xff | (content[12] & 0xff) << 8;
		int height = content[dirOffset - 2] & 0xff | (content[dirOffset - 1] & 0xff) << 8;
		int bytesPerLine;
		switch (mode) {
		case 0:
			if ((width & 1) != 0 || !setSize(width, height, RECOILResolution.APPLE_I_I_G_S1X1))
				return false;
			bytesPerLine = width >> 1;
			break;
		case 128:
			if ((width & 3) != 0 || !setSize(width, height << 1, RECOILResolution.APPLE_I_I_G_S1X2))
				return false;
			bytesPerLine = width >> 2;
			break;
		default:
			return false;
		}
		int multipalOffset = -1;
		int contentOffset = 0;
		if (height == 200) {
			for (int chunkLength = get32LittleEndian(content, 0);;) {
				if (chunkLength <= 0)
					return false;
				contentOffset += chunkLength;
				if (contentOffset < 0 || contentOffset + 6415 > contentLength)
					break;
				chunkLength = get32LittleEndian(content, contentOffset);
				if (chunkLength == 6415 && content[contentOffset + 4] == 8 && isStringAt(content, contentOffset + 5, "MULTIPAL") && content[contentOffset + 13] == -56 && content[contentOffset + 14] == 0) {
					multipalOffset = contentOffset + 15;
					break;
				}
			}
		}
		contentOffset = dirOffset + (height << 2);
		if (contentOffset >= contentLength)
			return false;
		final PackBytesStream stream = new PackBytesStream();
		stream.content = content;
		stream.contentOffset = contentOffset;
		stream.contentLength = contentLength;
		for (int y = 0; y < height; y++) {
			if (multipalOffset >= 0)
				setAppleIIGSPalette(content, multipalOffset + (y << 5), 0);
			else {
				int lineMode = content[dirOffset + (y << 2) + 2] & 0xff;
				int palette = lineMode & 15;
				if ((lineMode & 240) != mode || palette >= paletteCount || content[dirOffset + (y << 2) + 3] != 0)
					return false;
				setAppleIIGSPalette(content, 15 + (palette << 5), 0);
			}
			if (!decodePackBytes(stream, y * width, bytesPerLine))
				return false;
		}
		return true;
	}

	private static int getSamCoupeColor(int c)
	{
		int rgb = 0;
		if ((c & 1) != 0)
			rgb |= 73;
		if ((c & 2) != 0)
			rgb |= 4784128;
		if ((c & 4) != 0)
			rgb |= 18688;
		if ((c & 8) != 0)
			rgb |= 2368548;
		if ((c & 16) != 0)
			rgb |= 146;
		if ((c & 32) != 0)
			rgb |= 9568256;
		if ((c & 64) != 0)
			rgb |= 37376;
		return rgb;
	}

	private void setSamCoupePalette(byte[] content, int colors)
	{
		for (int i = 0; i < colors; i++)
			this.contentPalette[i] = getSamCoupeColor(content[24576 + i] & 0xff);
	}

	private void decodeSamCoupeMode4(byte[] content)
	{
		setSamCoupePalette(content, 16);
		setSize(256, 192, RECOILResolution.SAM_COUPE1X1);
		decodeNibbles(content, 0, 128);
	}

	private boolean decodeScs4(byte[] content, int contentLength)
	{
		if (contentLength != 24617 || content[24616] != -1)
			return false;
		decodeSamCoupeMode4(content);
		return true;
	}

	private void setSamCoupeAttrPalette(byte[] content, int contentOffset)
	{
		for (int i = 0; i < 64; i++)
			this.contentPalette[i] = getSamCoupeColor(content[contentOffset + (i >> 1 & 8) + (i & 7)] & 0xff);
	}

	private boolean decodeSsx(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 6928:
			setSamCoupeAttrPalette(content, 6912);
			setSize(256, 192, RECOILResolution.SAM_COUPE1X1);
			decodeZx(content, 0, 6144, 3, 0);
			return true;
		case 12304:
			setSamCoupeAttrPalette(content, 12288);
			setSize(256, 192, RECOILResolution.SAM_COUPE1X1);
			decodeZx(content, -1, 6144, 0, 0);
			return true;
		case 24580:
			setSamCoupePalette(content, 4);
			setSize(512, 384, RECOILResolution.SAM_COUPE1X2);
			for (int y = 0; y < 192; y++) {
				for (int x = 0; x < 512; x++) {
					int c = (content[y << 7 | x >> 2] & 0xff) >> ((~x & 3) << 1) & 3;
					this.pixels[(y << 10) + x + 512] = this.pixels[(y << 10) + x] = this.contentPalette[c];
				}
			}
			return true;
		case 24592:
			decodeSamCoupeMode4(content);
			return true;
		case 98304:
			for (int c = 0; c < 128; c++)
				this.contentPalette[c] = getSamCoupeColor(c);
			setSize(512, 384, RECOILResolution.SAM_COUPE1X2);
			for (int y = 0; y < 192; y++) {
				for (int x = 0; x < 512; x++) {
					int c = content[(y << 9) + x] & 0xff;
					if (c >= 128)
						return false;
					this.pixels[(y << 10) + x + 512] = this.pixels[(y << 10) + x] = this.contentPalette[c];
				}
			}
			return true;
		default:
			return false;
		}
	}

	private static int getX68KColor(int color)
	{
		int rgb = (color & 1984) << 13 | (color & 63488) | (color & 62) << 2;
		if ((color & 1) != 0)
			rgb |= 263172;
		return rgb | (rgb >> 6 & 197379);
	}

	private boolean decodeX68KPicChain(BitStream stream, int pixelsOffset, int color)
	{
		for (;;) {
			switch (stream.readBits(2)) {
			case 0:
				switch (stream.readBit()) {
				case 0:
					return true;
				case 1:
					break;
				default:
					return false;
				}
				switch (stream.readBit()) {
				case 0:
					pixelsOffset -= 2;
					break;
				case 1:
					pixelsOffset += 2;
					break;
				default:
					return false;
				}
				break;
			case 1:
				pixelsOffset--;
				break;
			case 2:
				break;
			case 3:
				pixelsOffset++;
				break;
			default:
				return false;
			}
			pixelsOffset += this.width;
			if (pixelsOffset >= this.width * this.height)
				return false;
			this.pixels[pixelsOffset] = color;
		}
	}

	private boolean decodeX68KPicScreen(X68KPicStream stream, int pixelsLength, int platform, int depth)
	{
		for (int pixelsOffset = 0; pixelsOffset < pixelsLength; pixelsOffset++)
			this.pixels[pixelsOffset] = -1;
		final RecentInts colors = new RecentInts();
		int color = 0;
		for (int pixelsOffset = -1;;) {
			int length = stream.readLength();
			if (length < 0)
				return false;
			while (--length > 0) {
				int got = this.pixels[++pixelsOffset];
				if (got < 0)
					this.pixels[pixelsOffset] = color;
				else
					color = got;
				if (pixelsOffset >= pixelsLength - 1)
					return true;
			}
			if (depth <= 8) {
				color = stream.readBits(depth);
				if (color < 0)
					return false;
				color = this.contentPalette[color];
			}
			else {
				switch (stream.readBit()) {
				case 0:
					color = stream.readBits(depth);
					if (color < 0)
						return false;
					switch (platform) {
					case 0:
						if (depth == 15)
							color <<= 1;
						color = getX68KColor(color);
						break;
					case 17:
						color = getG6R5B5Color(color);
						break;
					default:
						break;
					}
					colors.add(color);
					break;
				case 1:
					color = stream.readBits(7);
					if (color < 0)
						return false;
					color = colors.get(color);
					break;
				default:
					return false;
				}
			}
			this.pixels[++pixelsOffset] = color;
			if (pixelsOffset >= pixelsLength - 1)
				return true;
			switch (stream.readBit()) {
			case 0:
				break;
			case 1:
				if (!decodeX68KPicChain(stream, pixelsOffset, color))
					return false;
				break;
			default:
				return false;
			}
		}
	}

	private boolean decodeX68KPic(byte[] content, int contentLength)
	{
		if (contentLength < 14 || content[0] != 80 || content[1] != 73 || content[2] != 67)
			return false;
		int resolution = isStringAt(content, 3, "/MM/") ? RECOILResolution.MSX21X1 : RECOILResolution.X68_K1X1;
		final X68KPicStream stream = new X68KPicStream();
		stream.content = content;
		stream.contentOffset = 3;
		stream.contentLength = contentLength;
		for (;;) {
			int b = stream.readByte();
			if (b < 0)
				return false;
			if (b == 26)
				break;
			if (b == '/' && resolution == RECOILResolution.MSX21X1 && stream.contentOffset + 4 < contentLength && content[stream.contentOffset] == 77 && content[stream.contentOffset + 1] == 83) {
				b = content[stream.contentOffset + 2] & 0xff;
				if (b >= 'A' && b <= 'C')
					resolution = RECOILResolution.MSX2_PLUS1X1;
			}
		}
		if (!stream.skipUntilByte(0))
			return false;
		int platform = stream.readBits(16);
		int depth = stream.readBits(16);
		int width = stream.readBits(16);
		int height = stream.readBits(16);
		int pixelsLength = width * height;
		switch (platform) {
		case 0:
			if (!setSize(width, height, resolution))
				return false;
			break;
		case 17:
			return depth == 16 && setSize(width, height, RECOILResolution.PC88_VA1X1) && decodeX68KPicScreen(stream, pixelsLength, 17, 16);
		case 33:
			if (depth != 16 || !setSize(width, height << 1, RECOILResolution.PC88_VA1X1) || !decodeX68KPicScreen(stream, pixelsLength, 33, 16))
				return false;
			for (int offset = pixelsLength - 1; offset >= 0; offset--) {
				int color = this.pixels[offset];
				this.pixels[(offset << 1) + 1] = getG3R3B2Color(color >> 8);
				this.pixels[offset << 1] = getG3R3B2Color(color & 255);
			}
			return true;
		case 2:
			if (!setSize(width, height, RECOILResolution.FM_TOWNS1X1))
				return false;
			stream.contentOffset += 6;
			break;
		case 31:
			if (!setSize(width, height, RECOILResolution.X68_K1X1))
				return false;
			stream.contentOffset += 6;
			break;
		default:
			return false;
		}
		switch (depth) {
		case 4:
		case 8:
			for (int c = 0; c < 1 << depth; c++) {
				int color = stream.readBits(16);
				if (color < 0)
					return false;
				this.contentPalette[c] = getX68KColor(color);
			}
			break;
		case 15:
		case 16:
			break;
		default:
			return false;
		}
		return decodeX68KPicScreen(stream, pixelsLength, 0, depth);
	}

	private void setPc88EightPixels(int column, int y, int b)
	{
		for (int x = 0; x < 8; x++) {
			int offset = y * 1280 + column + x;
			this.pixels[offset + 640] = this.pixels[offset] = (b >> (7 - x) & 65793) * 255;
		}
	}

	private boolean decodeDaVinci(byte[] content, int contentLength)
	{
// too strict for my image
//		if ((contentLength & 255) != 0)
//			return false;
		setSize(640, 400, RECOILResolution.PC881X2);
		final DaVinciStream rle = new DaVinciStream();
		rle.content = content;
		rle.contentOffset = 0;
		rle.contentLength = contentLength;
		for (int y = 0; y < 200; y++) {
			for (int column = 0; column < 640; column += 8) {
				int b = rle.readRle();
				if (b < 0)
					return false;
				setPc88EightPixels(column, y, b);
			}
		}
        boolean r = rle.repeatCount == 0 && contentLength - rle.contentOffset < 256;
Debug.println("(rle.repeatCount: " + rle.repeatCount + ", contentLength - rle.contentOffset: " + (contentLength - rle.contentOffset) + ", result: " + r);
		return r;
	}

	private boolean decodeArtMaster88(byte[] content, int contentLength)
	{
		if (contentLength < 42 || !isStringAt(content, 0, "SS_SIF    0.0") || content[19] != 66 || content[20] != 82 || content[21] != 71 || content[24] != -128 || content[25] != 2)
			return false;
		final ArtMaster88Stream rle = new ArtMaster88Stream();
		rle.content = content;
		rle.contentOffset = 40;
		rle.contentLength = contentLength;
		if (content[16] == 73 && !rle.skipChunk())
			return false;
		switch (content[26] & 0xff | (content[27] & 0xff) << 8) {
		case 200:
			setSize(640, 400, RECOILResolution.PC881X2);
			if (content[18] == 66 && !rle.skipChunk())
				return false;
			if (!rle.readPlanes(3, 16000))
				return false;
			for (int y = 0; y < 200; y++) {
				for (int column = 0; column < 80; column++) {
					int offset = y * 80 + column;
					setPc88EightPixels(column << 3, y, (rle.planes[1][offset] & 0xff) << 16 | (rle.planes[2][offset] & 0xff) << 8 | rle.planes[0][offset] & 0xff);
				}
			}
			break;
		case 400:
			setSize(640, 400, RECOILResolution.PC981X1);
			if (content[17] != 82 || rle.contentOffset + 50 >= contentLength || content[rle.contentOffset + 1] != 0)
				return false;
			int paletteLength = content[rle.contentOffset] & 0xff;
			int planes;
			switch (paletteLength) {
			case 50:
				planes = 3;
				break;
			case 98:
				planes = 4;
				break;
			default:
				return false;
			}
			if (rle.contentOffset + paletteLength >= contentLength)
				return false;
			for (int c = 0; c < 1 << planes; c++) {
				int offset = rle.contentOffset + 2 + c * 6;
				int rgb = (content[offset] & 0xff) << 16 | (content[offset + 2] & 0xff) << 8 | content[offset + 4] & 0xff;
				if ((rgb & 15790320) != 0 || content[offset + 1] != 0 || content[offset + 3] != 0 || content[offset + 5] != 0)
					return false;
				this.contentPalette[c] = rgb * 17;
			}
			rle.contentOffset += paletteLength;
			if (content[18] == 66 && !rle.skipChunk())
				return false;
			if (!rle.readPlanes(planes, 32000))
				return false;
			for (int y = 0; y < 400; y++) {
				for (int column = 0; column < 80; column++) {
					int offset = y * 80 + column;
					for (int x = 0; x < 8; x++) {
						int c = 0;
						for (int plane = 0; plane < planes; plane++)
							c |= ((rle.planes[plane][offset] & 0xff) >> (7 - x) & 1) << plane;
						this.pixels[y * 640 + (column << 3) + x] = this.contentPalette[c];
					}
				}
			}
			break;
		default:
			return false;
		}
		return true;
	}

	private boolean decodeEbd(byte[] content, int contentLength)
	{
		if (contentLength < 368 || contentLength % 320 != 48)
			return false;
		for (int c = 0; c < 16; c++) {
			int rgb = (content[c * 3] & 0xff) << 16 | (content[c * 3 + 1] & 0xff) << 8 | content[c * 3 + 2] & 0xff;
			if (((rgb >> 4 ^ rgb) & 986895) != 0) {
				if ((rgb & 15790320) != 0)
					return false;
				rgb *= 17;
			}
			this.contentPalette[c] = rgb;
		}
		return decodeAmigaPlanar(content, 48, 640, contentLength / 320, RECOILResolution.PC981X1, 4, this.contentPalette);
	}

	private boolean decodeNl3(byte[] content, int contentLength)
	{
		final Nl3Stream stream = new Nl3Stream();
		stream.content = content;
		stream.contentOffset = 0;
		stream.contentLength = contentLength;
		for (int i = 0; i < 64; i++) {
			int c = stream.readValue();
			if (c < 0 || c > 127)
				return false;
			c |= stream.readValue() << 7;
			if (c < 0 || c >= 729)
				return false;
			this.contentPalette[i] = get729Color(c);
		}
		setSize(160, 100, RECOILResolution.PC981X1);
		for (int x = 0; x < 160; x++) {
			for (int y = 0; y < 100; y++) {
				int b = stream.readRle();
				if (b < 0)
					return false;
				this.pixels[y * 160 + x] = this.contentPalette[b];
			}
		}
		return true;
	}

	private boolean decodeMl1Chain(X68KPicStream stream, int pixelsOffset, int rgb)
	{
		for (;;) {
			switch (stream.readBit()) {
			case 0:
				break;
			case 1:
				switch (stream.readBits(2)) {
				case 0:
					pixelsOffset++;
					break;
				case 1:
					pixelsOffset--;
					break;
				case 2:
					return true;
				case 3:
					switch (stream.readBit()) {
					case 0:
						pixelsOffset += 2;
						break;
					case 1:
						pixelsOffset -= 2;
						break;
					default:
						return false;
					}
					break;
				default:
					return false;
				}
				break;
			default:
				return false;
			}
			pixelsOffset += this.width;
			if (pixelsOffset < 0 || pixelsOffset >= this.width * this.height)
				return false;
			this.pixels[pixelsOffset] = rgb;
		}
	}

	private static final int ML1_SINGLE_IMAGE = -1;

	private int decodeMl1Mx1(X68KPicStream stream, int imageOffset)
	{
		if (stream.readBits(32) != 825241626 || stream.readBits(32) < 0 || stream.readBits(16) < 0)
			return -1;
		int left = stream.readBits(16);
		int top = stream.readBits(16);
		int width = stream.readBits(16) - left + 1;
		int height = stream.readBits(16) - top + 1;
		for (int i = 0; i < 624; i++) {
			if (stream.readBit() < 0)
				return -1;
		}
		int mode = stream.readBits(2);
		int lastColor;
		switch (mode) {
		case 0:
			lastColor = 127;
			break;
		case 1:
		case 2:
			lastColor = stream.readBits(7);
			break;
		default:
			return -1;
		}
		if (imageOffset == -1) {
			if (!setSize(width, height, RECOILResolution.PC981X1))
				return -1;
			imageOffset = 0;
		}
		Arrays.fill(this.contentPalette, 0);
		for (int i = 0; i <= lastColor; i++) {
			int j = 0;
			if (mode > 0)
				j = stream.readBits(7);
			int c = stream.readBits(10);
			if (c < 0 || c >= 729)
				return -1;
			this.contentPalette[mode == 1 ? j : i] = get729Color(c);
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				this.pixels[imageOffset + y * this.width + x] = 1;
		}
		int distance = 1;
		int rgb = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixelsOffset = imageOffset + y * this.width + x;
				if (--distance > 0) {
					int old = this.pixels[pixelsOffset];
					if (old == 1)
						this.pixels[pixelsOffset] = rgb;
					else
						rgb = old;
				}
				else {
					distance = stream.readLength();
					if (distance < 0)
						return -1;
					int c = mode == 2 ? stream.readLength() - 1 : stream.readBits(7);
					if (c < 0 || c >= 128)
						return -1;
					rgb = this.contentPalette[c];
					switch (stream.readBit()) {
					case 0:
						break;
					case 1:
						if (!decodeMl1Chain(stream, pixelsOffset, rgb))
							return -1;
						break;
					default:
						return -1;
					}
					this.pixels[pixelsOffset] = rgb;
				}
			}
		}
		return distance == 1 && stream.readLength() == width * height + 1 ? height : -1;
	}

	private boolean decodeMl1(byte[] content, int contentLength)
	{
		final X68KPicStream stream = new X68KPicStream();
		stream.content = content;
		stream.contentOffset = 0;
		stream.contentLength = contentLength;
		return decodeMl1Mx1(stream, -1) > 0;
	}

	private boolean decodeMx1Tiles(Mx1Stream stream, int width, int height, int shift)
	{
		if (!setSize(width << shift, height << shift, RECOILResolution.PC981X1))
			return false;
		for (int y = 0; y < this.height; y += height) {
			for (int x = 0; x < this.width; x += width) {
				if (!stream.findImage() || decodeMl1Mx1(stream, y * this.width + x) < 0)
					return false;
			}
		}
		return true;
	}

	private boolean decodeMx1(byte[] content, int contentLength)
	{
		final Mx1Stream stream = new Mx1Stream();
		stream.content = content;
		stream.contentOffset = 0;
		stream.contentLength = contentLength;
		if (!stream.findImage() || decodeMl1Mx1(stream, -1) < 0)
			return false;
		if (!stream.findImage())
			return true;
		int sameSizeImages = 1;
		int width = this.width;
		int height = this.height;
		do {
			if (decodeMl1Mx1(stream, -1) < 0)
				return false;
			if (sameSizeImages > 0 && this.width == width && this.height == height)
				sameSizeImages++;
			else {
				if (width < this.width)
					width = this.width;
				if (sameSizeImages > 0) {
					height *= sameSizeImages;
					sameSizeImages = 0;
				}
				height += this.height;
			}
		}
		while (stream.findImage());
		stream.contentOffset = 0;
		switch (sameSizeImages) {
		case 4:
			return decodeMx1Tiles(stream, width, height, 1);
		case 16:
			return decodeMx1Tiles(stream, width, height, 2);
		case 0:
			break;
		default:
			height *= sameSizeImages;
			break;
		}
		if (!setSize(width, height, RECOILResolution.PC981X1))
			return false;
		Arrays.fill(this.pixels, 0, width * height, 0);
		int imageOffset = 0;
		while (stream.findImage())
			imageOffset += decodeMl1Mx1(stream, imageOffset) * width;
		return true;
	}

	private boolean decodeZim(byte[] content, int contentLength)
	{
		if (contentLength < 700 || !isStringAt(content, 0, "FORMAT-A"))
			return false;
		int contentOffset = 512 + ((content[506] & 0xff | (content[507] & 0xff) << 8) << 1);
		if (contentOffset + 26 > contentLength || content[contentOffset] != 0 || content[contentOffset + 1] != 0 || content[contentOffset + 2] != 0 || content[contentOffset + 3] != 0 || content[contentOffset + 20] != 1 || content[contentOffset + 21] != 0)
			return false;
		int width = (content[contentOffset + 4] & 0xff) + ((content[contentOffset + 5] & 0xff) << 8) + 1;
		int height = (content[contentOffset + 6] & 0xff) + ((content[contentOffset + 7] & 0xff) << 8) + 1;
		if (!setSize(width, height, RECOILResolution.PC981X1))
			return false;
		contentOffset += 24;
		if (content[contentOffset - 2] != 0 || content[contentOffset - 1] != 0) {
			if (contentOffset + 66 > contentLength)
				return false;
			for (int c = 0; c < 16; c++) {
				this.contentPalette[c] = (content[contentOffset + 1] & 0xff) << 16 | (content[contentOffset + 2] & 0xff) << 8 | content[contentOffset] & 0xff;
				contentOffset += 4;
			}
		}
		else {
			for (int c = 0; c < 16; c++)
				this.contentPalette[c] = getZxColor(c);
			this.contentPalette[8] = 16777215;
		}
		int pixelsLength = width * height;
		for (int pixelsOffset = 0; pixelsOffset < pixelsLength; pixelsOffset++)
			this.pixels[pixelsOffset] = this.contentPalette[0];
		byte[] flags3 = new byte[64];
		byte[] data = new byte[512];
		ZimStream stream = new ZimStream();
		stream.content = content;
Debug.printf(Level.FINER, "pos: %1$d, %1$08x", stream.contentOffset);
		stream.contentOffset = contentOffset;
		stream.contentLength = contentLength;
		int skip = stream.readWord();
		stream.contentOffset += skip << 1;
Debug.printf(Level.FINER, "pos: %1$d, %1$08x", stream.contentOffset);
		for (;;) {
			int dot = stream.readWord();
			switch (dot) {
			case -1:
				return false;
			case 0:
				return true;
			default:
				break;
			}
			int x = stream.readWord();
			if (x < 0 || x >= width)
				return false;
			int y = stream.readWord();
//Debug.println("y: " + y);
			if (y < 0 || y >= height)
				return false;
			int len = stream.readWord();
			if (len < 0)
				return false;
			stream.contentLength = stream.contentOffset + len;
			if (stream.contentLength >= contentLength)
				return false;
			int size = stream.readWord();
			if (size > 512 || (size & 3) != 0 || size << 1 < dot)
				return false;
			int pixelsOffset = y * width + x;
			if (pixelsOffset + dot > pixelsLength)
				return false;
			stream.unpackFlags2();
			stream.unpack(stream.flags2, flags3, 64);
			stream.unpack(flags3, data, size);
			stream.contentLength = contentLength;
			for (int i = 1; i < size; i++)
				data[i] ^= data[i - 1] & 0xff;
			for (int i = 2; i < size; i++)
				data[i] ^= data[i - 2] & 0xff;
			size >>= 2;
			for (int i = 0; i < dot; i++) {
				int bit = ~i & 7;
				int c = ((data[i >> 3] & 0xff) >> bit & 1) << 3
						| ((data[size + (i >> 3)] & 0xff) >> bit & 1) << 2
						| ((data[2 * size + (i >> 3)] & 0xff) >> bit & 1) << 1
						| ((data[3 * size + (i >> 3)] & 0xff) >> bit & 1);
				this.pixels[pixelsOffset + i] = this.contentPalette[c];
			}
		}
	}

	private boolean decodeQ4(byte[] content, int contentLength)
	{
		if (contentLength < 22 || (content[2] != 2 && ((content[1] & 0xff) > 1 || (content[3] & 0xff) > 1)) || (content[8] & 0xff) + ((content[9] & 0xff) << 8) != contentLength || !isStringAt(content, 11, "MAJYO"))
			return false;
		final Q4Stream rle = new Q4Stream();
		rle.content = content;
		rle.contentOffset = 16;
		rle.contentLength = contentLength;
		int nextChunkOffset = rle.startChunk();
		if (nextChunkOffset < 0 || !rle.unpackQ4())
			return false;
		for (int i = 0; i < 16; i++) {
			int rgb = 0;
			for (int c = 0; c < 3; c++) {
				if (rle.readRle() < 0)
					return false;
				int b = rle.readRle();
				if (b < 0)
					return false;
				rgb = rgb << 8 | b * 17;
			}
			this.contentPalette[(i & 8) | (i & 1) << 2 | (i >> 1 & 3)] = rgb;
		}
		setSize(640, 400, RECOILResolution.PC981X1);
		int chunkPixels = 0;
		for (int i = 0; i < 256000; i++) {
			if (--chunkPixels <= 0) {
				chunkPixels = (content[nextChunkOffset + 4] & 0xff | (content[nextChunkOffset + 5] & 0xff) << 8) << 1;
				rle.content = content;
				rle.contentOffset = nextChunkOffset;
				rle.contentLength = contentLength;
				nextChunkOffset = rle.startChunk();
				if (nextChunkOffset < 0 || !rle.unpackQ4())
					return false;
			}
			int b = rle.readRle();
			if (b < 0)
				b = 0;
			this.pixels[i] = this.contentPalette[b];
		}
		return true;
	}

	private static int packPiPlatform(String s)
	{
		return s.charAt(0) | s.charAt(1) << 8 | s.charAt(2) << 16 | s.charAt(3) << 24;
	}

	private static int getPiPlatform(byte[] content, int contentOffset, boolean highPixel)
	{
		switch (get32LittleEndian(content, contentOffset)) {
		case 1314344788:
			return RECOILResolution.FM_TOWNS1X1;
		case 808993616:
			return RECOILResolution.PC801X2;
		case 943211344:
			return RECOILResolution.PC881X2;
		case 1096172368:
			return highPixel ? RECOILResolution.PC881X2 : RECOILResolution.PC88_VA1X1;
		case 827872077:
		case 844649293:
		case 1347965773:
		case 1381520205:
			return highPixel ? RECOILResolution.MSX21X2 : RECOILResolution.MSX21X1;
		case 1261975128:
			return RECOILResolution.X68_K1X1;
		default:
			return highPixel ? RECOILResolution.PC881X2 : RECOILResolution.PC981X1;
		}
	}

	private boolean decodePi(byte[] content, int contentLength)
	{
		if (contentLength < 18 || content[0] != 80 || content[1] != 105)
			return false;
		final PiStream s = new PiStream();
		s.content = content;
		s.contentOffset = 2;
		s.contentLength = contentLength;
		if (!s.skipUntilByte(26) || !s.skipUntilByte(0))
			return false;
		int contentOffset = s.contentOffset;
		if (contentOffset + 14 > contentLength || content[contentOffset] != 0)
			return false;
		int depth = content[contentOffset + 3] & 0xff;
		if (depth != 4 && depth != 8)
			return false;
		int resolution = getPiPlatform(content, contentOffset + 4, content[contentOffset + 1] == 2 && content[contentOffset + 2] == 1);
		contentOffset += 8 + ((content[contentOffset + 8] & 0xff) << 8) + (content[contentOffset + 9] & 0xff);
		if (contentOffset + 6 >= contentLength)
			return false;
		int width = (content[contentOffset + 2] & 0xff) << 8 | content[contentOffset + 3] & 0xff;
		int height = (content[contentOffset + 4] & 0xff) << 8 | content[contentOffset + 5] & 0xff;
		if (!setScaledSize(width, height, resolution))
			return false;
		s.contentOffset = contentOffset + 6 + (3 << depth);
		if (s.unpack(width, height, depth)) {
			decodeR8G8B8Colors(content, contentOffset + 6, 1 << depth, this.contentPalette, 0);
			decodeBytes(s.indexes, 0);
			return true;
		}
		return false;
	}

	private void setMagPalette(byte[] content, int paletteOffset, int colors)
	{
		for (int c = 0; c < colors; c++) {
			int offset = paletteOffset + c * 3;
			this.contentPalette[c] = (content[offset + 1] & 0xff) << 16 | (content[offset] & 0xff) << 8 | content[offset + 2] & 0xff;
		}
	}

	private boolean decodeMaki1(byte[] content, int contentLength)
	{
		if (contentLength < 1096 || content[40] != 0 || content[41] != 0 || content[42] != 0 || content[43] != 0 || content[44] != 2 || content[45] != -128 || content[46] != 1 || content[47] != -112)
			return false;
		setSize(640, 400, getPiPlatform(content, 8, false));
		setMagPalette(content, 48, 16);
		int contentOffset = 1096;
		final int[] haveBuffer = new int[8000];
		final BitStream haveBlock = new BitStream();
		haveBlock.content = content;
		haveBlock.contentOffset = 96;
		haveBlock.contentLength = 1096;
		for (int i = 0; i < 8000; i++) {
			int have = 0;
			if (haveBlock.readBit() == 1) {
				if (contentOffset + 1 >= contentLength)
					return false;
				have = (content[contentOffset] & 0xff) << 8 | content[contentOffset + 1] & 0xff;
				contentOffset += 2;
			}
			haveBuffer[i] = have;
		}
		int xorYOffset = content[6] == 65 ? 2 : 0;
		final byte[] indexBuffer = new byte[1280];
		for (int y = 0; y < 400; y++) {
			for (int x = 0; x < 320; x++) {
				int row = y & 3;
				int index = indexBuffer[(row ^ xorYOffset) * 320 + x] & 0xff;
				if ((haveBuffer[(y & -4) * 20 + (x >> 2)] >> (15 - (row << 2) - (x & 3)) & 1) != 0) {
					if (contentOffset >= contentLength)
						return false;
					index ^= content[contentOffset++] & 0xff;
				}
				indexBuffer[row * 320 + x] = (byte) index;
				int pixelsOffset = (y * 320 + x) << 1;
				this.pixels[pixelsOffset] = this.contentPalette[index >> 4];
				this.pixels[pixelsOffset + 1] = this.contentPalette[index & 15];
			}
		}
		return true;
	}

	private static boolean unpackMag(byte[] content, int headerOffset, int contentLength, int bytesPerLine, int height, byte[] unpacked)
	{
		final BitStream haveDelta = new BitStream();
		haveDelta.content = content;
		haveDelta.contentOffset = headerOffset + get32LittleEndian(content, headerOffset + 12);
		haveDelta.contentLength = contentLength;
		int deltaOffset = headerOffset + get32LittleEndian(content, headerOffset + 16);
		int colorOffset = headerOffset + get32LittleEndian(content, headerOffset + 24);
		if (haveDelta.contentOffset < 0 || deltaOffset < 0 || colorOffset < 0)
			return false;
		byte[] deltas = new byte[(bytesPerLine + 3) >> 2];
		Arrays.fill(deltas, 0, (bytesPerLine + 3) >> 2, (byte) 0);
		for (int y = 0; y < height; y++) {
			int delta = 0;
			for (int x = 0; x < bytesPerLine; x++) {
				if ((x & 1) == 0) {
					delta = deltas[x >> 2] & 0xff;
					if ((x & 2) == 0) {
						switch (haveDelta.readBit()) {
						case 0:
							break;
						case 1:
							if (deltaOffset >= contentLength)
								return false;
							delta ^= content[deltaOffset++] & 0xff;
							deltas[x >> 2] = (byte) delta;
							break;
						default:
							return false;
						}
						delta >>= 4;
					}
					else
						delta &= 15;
				}
				if (delta == 0) {
					if (colorOffset >= contentLength)
						return false;
					unpacked[y * bytesPerLine + x] = (byte) (content[colorOffset++] & 0xff);
				}
				else {
					int sourceX = x - (UNPACK_MAG_DELTA_X[delta] & 0xff);
					int sourceY = y - (UNPACK_MAG_DELTA_Y[delta] & 0xff);
					if (sourceX < 0 || sourceY < 0)
						return false;
					unpacked[y * bytesPerLine + x] = (byte) (unpacked[sourceY * bytesPerLine + sourceX] & 0xff);
				}
			}
			if ((bytesPerLine & 1) != 0 && delta == 0)
				colorOffset++;
			if (((bytesPerLine + 1) & 2) != 0 && (deltas[bytesPerLine >> 2] & 15) == 0)
				colorOffset += 2;
		}
		return true;
	}

	private boolean decodeMag(byte[] content, int contentLength)
	{
		if (contentLength < 8)
			return false;
		if (isStringAt(content, 0, "MAKI01A ") || isStringAt(content, 0, "MAKI01B "))
			return decodeMaki1(content, contentLength);
		if (!isStringAt(content, 0, "MAKI02  "))
			return false;
		int headerOffset = 0;
		do {
			if (headerOffset >= contentLength)
				return false;
		}
		while (content[headerOffset++] != 26);
		if (headerOffset + 80 > contentLength || content[headerOffset] != 0)
			return false;
		int width = (content[headerOffset + 8] & 0xff) - (content[headerOffset + 4] & 0xff) + (((content[headerOffset + 9] & 0xff) - (content[headerOffset + 5] & 0xff)) << 8) + 1;
		int height = (content[headerOffset + 10] & 0xff) - (content[headerOffset + 6] & 0xff) + (((content[headerOffset + 11] & 0xff) - (content[headerOffset + 7] & 0xff)) << 8) + 1;
		int bytesPerLine;
		int colors;
		if ((content[headerOffset + 3] & 0xff) < 128) {
			bytesPerLine = (width + 1) >> 1;
			colors = 16;
		}
		else {
			if (headerOffset + 800 >= contentLength)
				return false;
			bytesPerLine = width;
			colors = 256;
		}
		int resolution;
		int msxMode = 0;
		switch (content[headerOffset + 1]) {
		case 0:
		case -120:
			resolution = (content[headerOffset + 3] & 1) == 0 ? RECOILResolution.PC88_VA1X1 : RECOILResolution.PC881X2;
			break;
		case 3:
			msxMode = content[headerOffset + 2] & 252;
			switch (msxMode) {
			case 0:
			case 20:
			case 84:
				resolution = RECOILResolution.MSX21X1;
				break;
			case 4:
				resolution = RECOILResolution.MSX21X2;
				break;
			case 16:
			case 80:
				resolution = RECOILResolution.MSX22X1I;
				break;
			case 32:
			case 64:
				if (colors == 16)
					width >>= 1;
				resolution = RECOILResolution.MSX2_PLUS2X1I;
				break;
			case 36:
			case 68:
				if (colors == 16)
					width >>= 1;
				resolution = RECOILResolution.MSX2_PLUS1X1;
				break;
			case 96:
				width = bytesPerLine << 2;
				resolution = RECOILResolution.MSX21X1I;
				break;
			case 100:
				width = bytesPerLine << 2;
				resolution = RECOILResolution.MSX21X2;
				break;
			default:
				return false;
			}
			break;
		case 98:
		case 112:
			resolution = RECOILResolution.PC981X1;
			break;
		case 104:
			resolution = RECOILResolution.X68_K1X1;
			break;
		case -128:
			resolution = RECOILResolution.PC801X2;
			break;
		case -103:
			resolution = RECOILResolution.MACINTOSH1X1;
			break;
		default:
			resolution = (content[headerOffset + 3] & 1) == 0 ? RECOILResolution.MSX21X1 : RECOILResolution.MSX21X2;
			break;
		}
		if (!setScaledSize(width, height, resolution))
			return false;
		byte[] unpacked = new byte[bytesPerLine * height];
		if (!unpackMag(content, headerOffset, contentLength, bytesPerLine, height, unpacked))
			return false;
		setMagPalette(content, headerOffset + 32, colors);
		switch (msxMode) {
		case 32:
		case 36:
			decodeMsxYjkScreen(unpacked, 0, true);
			break;
		case 64:
		case 68:
			decodeMsxYjkScreen(unpacked, 0, false);
			break;
		case 96:
		case 100:
			decodeMsx6(unpacked, 0);
			break;
		default:
			if (colors == 16)
				decodeNibbles(unpacked, 0, bytesPerLine);
			else
				decodeBytes(unpacked, 0);
			break;
		}
		return true;
	}

	private boolean decodeVbm(byte[] content, int contentLength)
	{
		if (contentLength < 9 || content[0] != 66 || content[1] != 77 || content[2] != -53)
			return false;
		int width = (content[4] & 0xff) << 8 | content[5] & 0xff;
		int height = (content[6] & 0xff) << 8 | content[7] & 0xff;
		if (!setSize(width, height, RECOILResolution.C1281X1))
			return false;
		switch (content[3]) {
		case 2:
			return decodeBlackAndWhite(content, 8, contentLength, false, 16777215);
		case 3:
			if (contentLength < 19)
				return false;
			int contentOffset = 18 + ((content[16] & 0xff) << 8) + (content[17] & 0xff);
			if (content[8] == 0)
				return decodeBlackAndWhite(content, contentOffset, contentLength, false, 0);
			final VbmStream rle = new VbmStream();
			rle.content = content;
			rle.contentOffset = contentOffset;
			rle.contentLength = contentLength;
			return decodeRleBlackAndWhite(rle, 0);
		default:
			return false;
		}
	}

	private boolean decodeBrus(byte[] content, int contentLength)
	{
		if (contentLength < 20 || !isStringAt(content, 2, "BRUS") || content[6] != 4 || content[10] != 1 || content[11] != 2)
			return false;
		int columns = content[12] & 0xff;
		if (columns == 0 || columns > 90)
			return false;
		int height = content[13] & 0xff | (content[14] & 0xff) << 8;
		if (height == 0 || height > 700)
			return false;
		int width = columns << 3;
		setSize(width, height, RECOILResolution.C1281X1);
		int bitmapLength = height * columns;
		final byte[] bitmap = new byte[63000];
		final PgcStream rle = new PgcStream();
		rle.content = content;
		rle.contentOffset = 18;
		rle.contentLength = contentLength;
		if (!rle.unpack(bitmap, 0, 1, bitmapLength))
			return false;
		int contentOffset = rle.contentOffset;
		if (contentOffset + 4 >= contentLength || !isStringAt(content, contentOffset, "COLR")) {
			return decodeBlackAndWhite(bitmap, 0, bitmapLength, false, 16777215);
		}
		rle.contentOffset = contentOffset + 4;
		final byte[] colors = new byte[180];
		for (int y = 0; y < height; y++) {
			if ((y & 7) == 0 && !rle.unpack(colors, 0, 1, columns << 1))
				return false;
			for (int x = 0; x < width; x++) {
				int column = x >> 3;
				int c = colors[(y & 1) * columns + column] & 0xff;
				if (((bitmap[y * columns + column] & 0xff) >> (~x & 7) & 1) == 0)
					c >>= 4;
				this.pixels[y * width + x] = DECODE_BRUS_PALETTE[c & 15];
			}
		}
		return true;
	}

	private static final int[] VIC20_PALETTE = { 0, 16777215, 7152423, 10551032, 9321623, 8313461, 2433936, 16777094, 10773563, 16763041, 15902635, 14417919, 16758015, 14155726, 10328831, 16777161 };

	private boolean decodeBp(byte[] content, int contentLength)
	{
		if (contentLength != 4083 || content[0] != 0 || content[1] != 17)
			return false;
		setSize(160, 192, RECOILResolution.VIC201X1);
		int screenColor = (content[4082] & 0xff) >> 4;
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 160; x++) {
				int c;
				if (((content[2 + (((x >> 3) * 12 + (y >> 4)) << 4) + (y & 15)] & 0xff) >> (~x & 7) & 1) == 0)
					c = screenColor;
				else {
					c = content[3842 + (y >> 4) * 20 + (x >> 3)] & 15;
					if (c >= 8)
						return false;
				}
				this.pixels[y * 160 + x] = VIC20_PALETTE[c];
			}
		}
		return true;
	}

	private boolean decodePic0(String filename, byte[] content, int contentLength)
	{
		if (contentLength != 3890 || content[0] != 0 || content[1] != 13 || content[3876] != -106 || content[3877] != 23 || content[3879] != -116)
			return false;
		final byte[] colors = new byte[245];
		if (readCompanionFile(filename, "PIC1", "pic1", colors, 245) != 244)
			return false;
		setSize(176, 176, RECOILResolution.VIC202X1);
		for (int y = 0; y < 176; y++) {
			for (int x = 0; x < 176; x++) {
				int charOffset = (y >> 4) * 22 + (x >> 3);
				int c = colors[2 + charOffset] & 0xff;
				if ((c & 8) == 0)
					return false;
				switch ((content[2 + (charOffset << 4) + (y & 15)] & 0xff) >> (~x & 6) & 3) {
				case 0:
					c = (content[3889] & 0xff) >> 4;
					break;
				case 1:
					c = content[3889] & 7;
					break;
				case 2:
					c &= 7;
					break;
				default:
					c = (content[3888] & 0xff) >> 4;
					break;
				}
				this.pixels[y * 176 + x] = VIC20_PALETTE[c];
			}
		}
		return true;
	}

	private boolean decodeP4i(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 10050:
			if (isStringAt(content, 1020, "MULT")) {
				setSize(320, 200, RECOILResolution.C162X1);
				for (int y = 0; y < 200; y++) {
					for (int x = 0; x < 320; x++) {
						int offset = (y & -8) * 40 + (x & -8) + (y & 7);
						int c = (content[2050 + offset] & 0xff) >> (~x & 6) & 3;
						switch (c) {
						case 0:
							c = (content[1025] & 7) << 4 | (content[1025] & 0xff) >> 4;
							break;
						case 1:
							offset >>= 3;
							c = (content[2 + offset] & 7) << 4 | (content[1026 + offset] & 0xff) >> 4;
							break;
						case 2:
							offset >>= 3;
							c = (content[2 + offset] & 112) | (content[1026 + offset] & 15);
							break;
						default:
							c = (content[1024] & 7) << 4 | (content[1024] & 0xff) >> 4;
							break;
						}
						this.pixels[y * 320 + x] = this.c16Palette[c];
					}
				}
			}
			else {
				setSize(320, 200, RECOILResolution.C161X1);
				for (int y = 0; y < 200; y++) {
					for (int x = 0; x < 320; x++) {
						int offset = (y & -8) * 40 + (x & -8) + (y & 7);
						int c = (content[2050 + offset] & 0xff) >> (~x & 7) & 1;
						offset >>= 3;
						if (c == 0)
							c = (content[2 + offset] & 112) | (content[1026 + offset] & 15);
						else
							c = (content[2 + offset] & 7) << 4 | (content[1026 + offset] & 0xff) >> 4;
						this.pixels[y * 320 + x] = this.c16Palette[c];
					}
				}
			}
			break;
		case 2050:
			setSize(256, 64, RECOILResolution.C162X1);
			for (int y = 0; y < 64; y++) {
				for (int x = 0; x < 256; x++) {
					int c = DECODE_P4I_LOGO_COLORS[(content[2 + ((x & -8) << 3) + y] & 0xff) >> (~x & 6) & 3] & 0xff;
					this.pixels[(y << 8) + x] = this.c16Palette[c];
				}
			}
			break;
		default:
			return false;
		}
		return true;
	}

	private boolean decodeZs(byte[] content, int contentLength)
	{
		if (contentLength != 1026 || content[0] != -80 || content[1] != -16)
			return false;
		setSize(256, 32, RECOILResolution.C641X1);
		for (int y = 0; y < 32; y++) {
			for (int x = 0; x < 256; x++) {
				int c = y >> 3 << 5 | x >> 3;
				if (c < 113) {
					c = (content[3 + c * 9 + (y & 7)] & 0xff) >> (~x & 7) & 1;
					if (c != 0)
						c = 16777215;
				}
				else
					c = 0;
				this.pixels[(y << 8) + x] = c;
			}
		}
		return true;
	}

	private boolean decodeG(byte[] content, int contentLength)
	{
		if (contentLength != 514 || content[0] != 66 || content[1] != 0)
			return false;
		setSize(256, 16, RECOILResolution.C641X1);
		decodeBlackAndWhiteFont(content, 2, 514, 8);
		return true;
	}

	private boolean decode64c(byte[] content, int contentLength)
	{
		if (contentLength < 10 || contentLength > 2050 || content[0] != 0)
			return false;
		setSize(256, (contentLength + 253) >> 8 << 3, RECOILResolution.C641X1);
		decodeBlackAndWhiteFont(content, 2, contentLength, 8);
		return true;
	}

	private boolean decodePrintfox(byte[] content, int contentLength)
	{
		if (contentLength < 4)
			return false;
		int columns;
		int rows;
		final PrintfoxStream rle = new PrintfoxStream();
		rle.content = content;
		rle.contentOffset = 1;
		rle.contentLength = contentLength;
		switch (content[0]) {
		case 66:
			columns = 40;
			rows = 25;
			break;
		case 71:
			columns = 80;
			rows = 50;
			break;
		case 80:
			columns = content[2] & 0xff;
			rows = content[1] & 0xff;
			if (!rle.skipUntilByte(0))
				return false;
			break;
		default:
			return false;
		}
		if (!setSize(columns << 3, rows << 3, RECOILResolution.C641X1))
			return false;
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				for (int y = 0; y < 8; y++) {
					int b = rle.readRle();
					if (b < 0)
						return false;
					for (int x = 0; x < 8; x++)
						this.pixels[((((row << 3) + y) * columns + column) << 3) + x] = (b >> (7 - x) & 1) == 0 ? 16777215 : 0;
				}
			}
		}
		return true;
	}

	private void decodeC64FourColor(byte[] content, int contentOffset, int frame)
	{
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < 320; x++) {
				int i = x - frame;
				int c = i < 0 ? 0 : (content[contentOffset + (y & -8) * 40 + (i & -8) + (y & 7)] & 0xff) >> (~i & 6) & 3;
				this.pixels[(frame * this.height + y) * 320 + x] = this.contentPalette[c];
			}
		}
	}

	private boolean decodeCle(byte[] content, int contentLength)
	{
		if (contentLength != 8194)
			return false;
		setSize(320, 200, RECOILResolution.C641X1);
		this.contentPalette[0] = this.c64Palette[content[8004] & 15];
		this.contentPalette[1] = this.c64Palette[(content[8002] & 0xff) >> 4];
		this.contentPalette[2] = this.c64Palette[content[8002] & 15];
		this.contentPalette[3] = this.c64Palette[content[8003] & 15];
		decodeC64FourColor(content, 2, 0);
		return true;
	}

	private boolean decodeIle(byte[] content, int contentLength)
	{
		if (contentLength != 4098)
			return false;
		setSize(320, 48, RECOILResolution.C641X1, 2);
		for (int i = 0; i < 4; i++)
			this.contentPalette[i] = this.c64Palette[content[4094 + i] & 0xff & (i < 3 ? 15 : 7)];
		decodeC64FourColor(content, 2050, 0);
		decodeC64FourColor(content, 2, 1);
		return applyBlend();
	}

	private static final int FLI_BUG_CHARACTERS = 3;

	private static final int FLI_WIDTH = 296;

	private boolean decodeCfli(byte[] content, int contentLength)
	{
		if (contentLength != 8170)
			return false;
		setSize(296, 200, RECOILResolution.C641X1);
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 296; x++)
				this.pixels[y * 296 + x] = this.c64Palette[(content[5 + ((y & 7) << 10) + (y & -8) * 5 + (x >> 3)] & 0xff) >> ((~x & 1) << 2) & 15];
		}
		return true;
	}

	private static boolean isGodot(byte[] content, int contentLength)
	{
		return content[0] == 71 && content[1] == 79 && content[2] == 68 && content[contentLength - 1] == -83;
	}

	private boolean decodeGodot(int width, int height, byte[] content, int contentOffset, int contentLength, boolean compressed)
	{
		if (!setSize(width, height, RECOILResolution.C641X1))
			return false;
		final UflStream rle = new UflStream();
		rle.content = content;
		rle.contentOffset = contentOffset;
		rle.contentLength = contentLength;
		rle.escape = 173;
		for (int row = 0; row < height; row += 8) {
			for (int column = 0; column < width; column += 8) {
				for (int y = 0; y < 8; y++) {
					int pixelsOffset = (row + y) * width + column;
					for (int x = 0; x < 4; x++) {
						int b = compressed ? rle.readRle() : rle.readByte();
						if (b < 0)
							return false;
						this.pixels[pixelsOffset++] = this.c64Palette[DECODE_GODOT_BY_BRIGHTNESS[b >> 4] & 0xff];
						this.pixels[pixelsOffset++] = this.c64Palette[DECODE_GODOT_BY_BRIGHTNESS[b & 15] & 0xff];
					}
				}
			}
		}
		return true;
	}

	private boolean decode4bt(byte[] content, int contentLength)
	{
		if (contentLength < 5)
			return false;
		if (contentLength == 32002 && content[0] == 0)
			return decodeGodot(320, 200, content, 2, contentLength, false);
		return content[3] == 48 && isGodot(content, contentLength) && decodeGodot(320, 200, content, 4, contentLength - 1, true);
	}

	private boolean decodeGodotClp(byte[] content, int contentLength)
	{
		return contentLength >= 9 && content[3] == 49 && isGodot(content, contentLength) && decodeGodot((content[6] & 0xff) << 3, (content[7] & 0xff) << 3, content, 8, contentLength - 1, true);
	}

	private void decodeC64HiresFrame(byte[] content, int bitmapOffset, int videoMatrixOffset, boolean afli, int stride, int pixelsOffset)
	{
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				int offset = (y & -8) * stride + (x & -8) + (y & 7);
				int c = (content[bitmapOffset + offset] & 0xff) >> (~x & 7) & 1;
				int v;
				if (videoMatrixOffset >= 0) {
					offset >>= 3;
					if (afli)
						offset += (y & 7) << 10;
					v = content[videoMatrixOffset + offset] & 0xff;
				}
				else
					v = -videoMatrixOffset;
				c = c == 0 ? v & 15 : v >> 4;
				this.pixels[pixelsOffset + y * this.width + x] = this.c64Palette[c];
			}
		}
	}

	private boolean decodeC64Hires(byte[] content, int bitmapOffset, int videoMatrixOffset)
	{
		setSize(320, 200, RECOILResolution.C641X1);
		decodeC64HiresFrame(content, bitmapOffset, videoMatrixOffset, false, 40, 0);
		return true;
	}

	private boolean decodeRpo(byte[] content, int contentLength)
	{
		return contentLength == 8002 && decodeC64Hires(content, 2, -1);
	}

	private boolean decodeGr(byte[] content, int contentLength)
	{
		if (contentLength < 10)
			return false;
		int columns = content[0] & 0xff;
		int height = (content[1] & 0xff) << 3;
		if (contentLength != 2 + columns * height || !setSize(columns << 3, height, RECOILResolution.C641X1))
			return false;
		decodeC64HiresFrame(content, 2, -1, false, columns, 0);
		return true;
	}

	private boolean decodeC64Hir(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 8002:
		case 8194:
			return decodeC64Hires(content, 2, -16);
		default:
			return false;
		}
	}

	private boolean decodeIph(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 9002:
		case 9003:
		case 9009:
			return decodeC64Hires(content, 2, 8002);
		default:
			return false;
		}
	}

	private boolean decodeHed(byte[] content)
	{
		return decodeC64Hires(content, 2, 8194);
	}

	private boolean decodeJj(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[9024];
		final C64KoalaStream rle = new C64KoalaStream();
		rle.content = content;
		rle.contentOffset = 2;
		rle.contentLength = contentLength;
		return rle.unpack(unpacked, 0, 1, 9024) && decodeC64Hires(unpacked, 1024, 0);
	}

	private boolean decodeDd(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 9026:
		case 9217:
		case 9218:
		case 9346:
			return decodeC64Hires(content, 1026, 2);
		default:
			return decodeJj(content, contentLength);
		}
	}

	private int getC64Multicolor(byte[] content, int bitmapOffset, int videoMatrixOffset, int colorOffset, int background, boolean bottomBfli, int x, int y)
	{
		x += this.leftSkip;
		if (x < 0)
			return background;
		int charOffset = (y & -8) * 5 + (x >> 3);
		if (bottomBfli)
			charOffset = (charOffset - 21) & 1023;
		switch ((content[bitmapOffset + (charOffset << 3) + (y & 7)] & 0xff) >> (~x & 6) & 3) {
		case 1:
			return (content[videoMatrixOffset + charOffset] & 0xff) >> 4;
		case 2:
			return content[videoMatrixOffset + charOffset] & 0xff;
		case 3:
			return colorOffset < 0 ? content[-colorOffset] & 0xff : content[colorOffset + charOffset] & 0xff;
		default:
			return background;
		}
	}

	private void decodeC64MulticolorLine(byte[] content, int bitmapOffset, int videoMatrixOffset, int colorOffset, int background, boolean bottomBfli, int y, int pixelsOffset)
	{
		for (int x = 0; x < this.width; x++)
			this.pixels[pixelsOffset + x] = this.c64Palette[getC64Multicolor(content, bitmapOffset, videoMatrixOffset, colorOffset, background, bottomBfli, x, y) & 15];
	}

	private void decodeC64MulticolorFrame(byte[] content, int bitmapOffset, int videoMatrixOffset, int colorOffset, int backgroundOffset, int pixelsOffset)
	{
		int background = backgroundOffset < 0 ? 0 : content[backgroundOffset] & 0xff;
		for (int y = 0; y < 200; y++)
			decodeC64MulticolorLine(content, bitmapOffset, videoMatrixOffset, colorOffset, background, false, y, pixelsOffset + y * 320);
	}

	private boolean decodeC64Multicolor(byte[] content, int bitmapOffset, int videoMatrixOffset, int colorOffset, int backgroundOffset)
	{
		setSize(320, 200, RECOILResolution.C642X1);
		decodeC64MulticolorFrame(content, bitmapOffset, videoMatrixOffset, colorOffset, backgroundOffset, 0);
		return true;
	}

	private boolean decodeOcp(byte[] content, int contentLength)
	{
		return contentLength == 10018 && decodeC64Multicolor(content, 2, 8002, 9018, 9003);
	}

	private boolean decodeBpl(byte[] content, int contentLength)
	{
		return contentLength == 10242 && decodeC64Multicolor(content, 2, 8194, 9218, 8066);
	}

	private void decodeC64MulticolorFliFrame(byte[] content, int bitmapOffset, int videoMatrixOffset, int colorOffset, int background, boolean bottomBfli, int pixelsOffset)
	{
		for (int y = 0; y < 200; y++)
			decodeC64MulticolorLine(content, bitmapOffset, videoMatrixOffset + ((y & 7) << 10), colorOffset, background, bottomBfli, y, pixelsOffset + y * 296);
	}

	private void decodeC64MulticolorFliBarsFrame(byte[] content, int bitmapOffset, int videoMatrixOffset, int colorOffset, int backgroundOffset, int pixelsOffset)
	{
		for (int y = 0; y < 200; y++)
			decodeC64MulticolorLine(content, bitmapOffset, videoMatrixOffset + ((y & 7) << 10), colorOffset, content[backgroundOffset + y] & 0xff, false, y, pixelsOffset + y * 296);
	}

	private boolean decodeC64MulticolorFli(byte[] content, int bitmapOffset, int videoMatrixOffset, int colorOffset, int background)
	{
		setSize(296, 200, RECOILResolution.C642X1);
		decodeC64MulticolorFliFrame(content, bitmapOffset + 24, videoMatrixOffset + 3, colorOffset + 3, background, false, 0);
		return true;
	}

	private boolean decodeC64MulticolorFliBars(byte[] content, int bitmapOffset, int videoMatrixOffset, int colorOffset, int backgroundOffset)
	{
		setSize(296, 200, RECOILResolution.C642X1);
		decodeC64MulticolorFliBarsFrame(content, bitmapOffset + 24, videoMatrixOffset + 3, colorOffset + 3, backgroundOffset, 0);
		return true;
	}

	private boolean decodeHfc(byte[] content, int contentLength)
	{
		if (contentLength != 16386)
			return false;
		setSize(296, 112, RECOILResolution.C641X1);
		decodeC64HiresFrame(content, 26, 8197, true, 40, 0);
		return true;
	}

	private boolean decodeAfl(byte[] content, int contentLength)
	{
		if (contentLength != 16385)
			return false;
		setSize(296, 200, RECOILResolution.C641X1);
		decodeC64HiresFrame(content, 8218, 5, true, 40, 0);
		return true;
	}

	private boolean decodePmg(byte[] content, int contentLength)
	{
		return contentLength == 9332 && decodeC64Multicolor(content, 116, 8308, -8119, 8116);
	}

	private boolean decodeKoa(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 10001:
			return decodeC64Multicolor(content, 0, 8000, 9000, 10000);
		case 10003:
		case 10006:
			return decodeC64Multicolor(content, 2, 8002, 9002, 10002);
		default:
			return decodeGg(content, contentLength);
		}
	}

	private boolean decodeZom(byte[] content, int contentLength)
	{
		if (contentLength < 6)
			return false;
		final byte[] unpacked = new byte[10001];
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentOffset = contentLength - 1;
		rle.escape = content[contentLength - 1] & 0xff;
		return rle.unpackBackwards(unpacked, 0, 10000) && decodeKoa(unpacked, 10001);
	}

	private boolean decodeKoaPacked(RleStream rle)
	{
		final byte[] unpacked = new byte[10001];
		return rle.unpack(unpacked, 0, 1, 10001) && decodeKoa(unpacked, 10001);
	}

	private boolean decodeGg(byte[] content, int contentLength)
	{
		if (contentLength < 2)
			return false;
		final C64KoalaStream rle = new C64KoalaStream();
		rle.content = content;
		rle.contentOffset = 2;
		rle.contentLength = contentLength;
		return decodeKoaPacked(rle);
	}

	private boolean decodeDol(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 10241:
		case 10242:
		case 10050:
			return decodeC64Multicolor(content, 2050, 1026, 2, 2026);
		default:
			return false;
		}
	}

	private boolean decodeAmi(byte[] content, int contentLength)
	{
		if (contentLength < 2)
			return false;
		final DrpStream rle = new DrpStream();
		rle.content = content;
		rle.contentOffset = 2;
		rle.contentLength = contentLength;
		rle.escape = 194;
		return decodeKoaPacked(rle);
	}

	private boolean decodeBdp(byte[] content, int contentLength)
	{
		if (contentLength < 13)
			return false;
		if (content[2] == 2 && content[3] == 4 && content[4] == 16 && content[5] == 54 && content[6] == 48 && content[7] == 48) {
			final UflStream rle = new UflStream();
			rle.content = content;
			rle.contentOffset = 10;
			rle.contentLength = contentLength;
			rle.escape = content[8] & 0xff;
			return decodeKoaPacked(rle);
		}
		if (isStringAt(content, 2, "BDP 5.00")) {
			final Bdp5Stream rle = new Bdp5Stream();
			rle.content = content;
			rle.contentOffset = 12;
			rle.contentLength = contentLength;
			return decodeKoaPacked(rle);
		}
		else {
			final Bdp4Stream rle = new Bdp4Stream();
			rle.content = content;
			rle.contentOffset = 2;
			rle.contentLength = contentLength;
			return decodeKoaPacked(rle);
		}
	}

	private boolean decodeMwi(byte[] content, int contentLength)
	{
		if (contentLength < 7)
			return false;
		int width = (content[3] & 0xff) << 1;
		int height = content[4] & 0xff;
		if (!setSize(width, height, RECOILResolution.C642X1))
			return false;
		int charactersPerRow = (width + 7) >> 3;
		int left = (content[1] & 3) << 1;
		if (left != 0)
			charactersPerRow++;
		int rows = (height + 7) >> 3;
		int top = content[2] & 7;
		if (top != 0)
			rows++;
		if (contentLength != 5 + rows * charactersPerRow * 10)
			return false;
		for (int y = 0; y < height; y++) {
			int screenY = top + y;
			for (int x = 0; x < width; x++) {
				int screenX = left + x;
				int offset = 5 + ((screenY >> 3) * charactersPerRow + (screenX >> 3)) * 10;
				int c;
				switch ((content[offset + 2 + (screenY & 7)] & 0xff) >> (~screenX & 6) & 3) {
				case 1:
					c = (content[offset] & 0xff) >> 4;
					break;
				case 2:
					c = content[offset] & 0xff;
					break;
				case 3:
					c = content[offset + 1] & 0xff;
					break;
				default:
					c = 0;
					break;
				}
				this.pixels[y * width + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private boolean decodeC64Shp(byte[] content, int contentLength)
	{
		if (contentLength < 8)
			return false;
		int columns = 40;
		int height = 200;
		final UflStream rle = new UflStream();
		switch (content[2]) {
		case 0:
			rle.escape = content[3] & 0xff;
			rle.contentOffset = 5;
			break;
		case -128:
			rle.escape = content[3] & 0xff;
			rle.contentOffset = 4;
			break;
		case -89:
			if (content[3] != 25)
				return false;
			columns = 39;
			rle.escape = content[4] & 0xff;
			rle.contentOffset = 5;
			break;
		case -88:
			height = (content[3] & 0xff) << 3;
			if (height == 0 || height > 200)
				return false;
			rle.escape = content[4] & 0xff;
			rle.contentOffset = 5;
			break;
		case -24:
			if (content[3] != 25)
				return false;
			rle.escape = content[5] & 0xff;
			rle.contentOffset = 6;
			break;
		default:
			return false;
		}
		rle.content = content;
		rle.contentLength = contentLength;
		int bitmapLength = height * columns;
		final byte[] unpacked = new byte[10001];
		if (!rle.unpack(unpacked, 0, 1, bitmapLength))
			return false;
		rle.escape = 0;
		if (!rle.unpack(unpacked, bitmapLength, 1, (bitmapLength >> 3) * 9))
			return false;
		switch (content[2]) {
		case 0:
			rle.escape = 255;
			break;
		case -24:
			if (rle.readByte() != 255)
				return false;
			rle.escape = 216;
			break;
		default:
			if (rle.contentOffset != contentLength)
				return false;
			setSize(columns << 3, height, RECOILResolution.C641X1);
			decodeC64HiresFrame(unpacked, 0, bitmapLength, false, columns, 0);
			return true;
		}
		unpacked[10000] = (byte) (content[4] & 0xff);
		return rle.unpack(unpacked, 9000, 1, 10000) && rle.contentOffset == contentLength && decodeKoa(unpacked, 10001);
	}

	private boolean decodeC64HiresInterlace(byte[] content, int bitmap1Offset, int videoMatrix1Offset, int bitmap2Offset, int videoMatrix2Offset)
	{
		setSize(320, 200, RECOILResolution.C641X1, 2);
		decodeC64HiresFrame(content, bitmap1Offset, videoMatrix1Offset, false, 40, 0);
		decodeC64HiresFrame(content, bitmap2Offset, videoMatrix2Offset, false, 40, 64000);
		return applyBlend();
	}

	private boolean decodeIhe(byte[] content, int contentLength)
	{
		return contentLength == 16194 && decodeC64HiresInterlace(content, 2, -12, 8194, -12);
	}

	private boolean decodeHlf(byte[] content, int contentLength)
	{
		return contentLength == 24578 && decodeC64HiresInterlace(content, 2, 10242, 16386, 9218);
	}

	private boolean decodeHle(byte[] content, int contentLength)
	{
		return contentLength == 32770 && decodeC64HiresInterlace(content, 2, 8194, 24578, 16386);
	}

	private boolean decodeVhi(byte[] content, int contentLength)
	{
		if (contentLength == 17389)
			return decodeC64HiresInterlace(content, 2, 16386, 8194, 16386);
		final byte[] unpacked = new byte[17384];
		final VhiStream rle = new VhiStream();
		rle.content = content;
		rle.contentOffset = 2;
		rle.contentLength = contentLength;
		return rle.unpack(unpacked, 0, 1, 17384) && decodeC64HiresInterlace(unpacked, 0, 16384, 8192, 16384);
	}

	private boolean decodeMciLike(byte[] content, int bitmap1Offset, int videoMatrix1Offset, int bitmap2Offset, int videoMatrix2Offset, int colorOffset, int backgroundOffset, int shift)
	{
		setSize(320, 200, shift == 0 ? RECOILResolution.C642X1 : RECOILResolution.C641X1, 2);
		decodeC64MulticolorFrame(content, bitmap1Offset, videoMatrix1Offset, colorOffset, backgroundOffset, 0);
		this.leftSkip = -shift;
		decodeC64MulticolorFrame(content, bitmap2Offset, videoMatrix2Offset, colorOffset, backgroundOffset, 64000);
		return applyBlend();
	}

	private boolean decodeFp(byte[] content, int contentLength)
	{
		return contentLength == 19266 && decodeMciLike(content, 3074, 1026, 11266, 2050, 2, 11074, 1);
	}

	private boolean decodeMciUnpacked(byte[] content)
	{
		return decodeMciLike(content, 1026, 2, 9218, 17410, 18434, 1002, 1);
	}

	private boolean decodeMci(byte[] content, int contentLength)
	{
		if (contentLength == 19434)
			return decodeMciUnpacked(content);
		if (contentLength < 142)
			return false;
		final byte[] unpacked = new byte[19434];
		final MciStream rle = new MciStream();
		rle.content = content;
		rle.contentOffset = contentLength - 1;
		return rle.unpackBackwards(unpacked, 2, 19433) && decodeMciUnpacked(unpacked);
	}

	private boolean decodeDrz(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[10051];
		content = DrpStream.unpackFile(content, contentLength, "DRAZPAINT 1.4", unpacked, 10051);
		return content != null && decodeC64Multicolor(content, 2050, 1026, 2, 10050);
	}

	private boolean decodeDrl(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[18242];
		content = DrpStream.unpackFile(content, contentLength, "DRAZLACE! 1.0", unpacked, 18242);
		if (content == null)
			return false;
		int shift = content[10052] & 0xff;
		return shift <= 1 && decodeMciLike(content, 2050, 1026, 10242, 1026, 2, 10050, shift);
	}

	private void decodeMleFrame(byte[] content, int contentOffset, int pixelsOffset)
	{
		for (int y = 0; y < 56; y++) {
			for (int x = 0; x < 320; x++) {
				int c = 0;
				int i = x + this.leftSkip;
				if (i >= 0) {
					int ch = (y >> 3) * 40 + (i >> 3);
					if (ch < 256) {
						c = DECODE_MLE_FRAME_COLORS[(content[contentOffset + (ch << 3) + (y & 7)] & 0xff) >> (~i & 6) & 3] & 0xff;
					}
				}
				this.pixels[pixelsOffset + y * this.width + x] = this.c64Palette[c];
			}
		}
	}

	private boolean decodeMle(byte[] content, int contentLength)
	{
		if (contentLength != 4098)
			return false;
		setSize(320, 56, RECOILResolution.C641X1, 2);
		decodeMleFrame(content, 2050, 0);
		this.leftSkip = -1;
		decodeMleFrame(content, 2, 17920);
		return applyBlend();
	}

	private boolean decodeHcb(byte[] content, int contentLength)
	{
		if (contentLength != 12148)
			return false;
		setSize(296, 200, RECOILResolution.C642X1);
		for (int y = 0; y < 200; y++)
			decodeC64MulticolorLine(content, 4122, 2053 + ((y & 4) << 8), 2053 + ((y & 4) << 8), content[12098 + (y >> 2)] & 0xff, false, y, y * 296);
		return true;
	}

	private boolean decodeFed(byte[] content, int contentLength)
	{
		return contentLength == 17665 && decodeC64MulticolorFliBars(content, 9474, 1282, 258, 8);
	}

	private boolean decodeHimUnpacked(byte[] content)
	{
		setSize(296, 192, RECOILResolution.C641X1);
		decodeC64HiresFrame(content, 346, 8237, true, 40, 0);
		return true;
	}

	private boolean decodeHim(byte[] content, int contentLength)
	{
		if (contentLength < 18 || content[0] != 0 || content[1] != 64)
			return false;
		if (content[3] == -1)
			return contentLength == 16385 && decodeHimUnpacked(content);
		if ((content[2] & 0xff) + ((content[3] & 0xff) << 8) != 16381 + contentLength || content[4] != -14 || content[5] != 127)
			return false;
		final byte[] unpacked = new byte[16372];
		final HimStream rle = new HimStream();
		rle.content = content;
		rle.contentOffset = contentLength - 1;
		return rle.unpackBackwards(unpacked, 322, 16371) && decodeHimUnpacked(unpacked);
	}

	private boolean decodeEci(byte[] content, int contentLength)
	{
		if (contentLength != 32770)
			return decodeEcp(content, contentLength);
		setSize(296, 200, RECOILResolution.C641X1, 2);
		decodeC64HiresFrame(content, 26, 8197, true, 40, 0);
		decodeC64HiresFrame(content, 16410, 24581, true, 40, 59200);
		return applyBlend();
	}

	private boolean decodeEcp(byte[] content, int contentLength)
	{
		if (contentLength < 6)
			return false;
		final byte[] unpacked = new byte[32770];
		final DrpStream rle = new DrpStream();
		rle.content = content;
		rle.contentOffset = 3;
		rle.contentLength = contentLength;
		rle.escape = content[2] & 0xff;
		return rle.unpack(unpacked, 2, 1, 32770) && decodeEci(unpacked, 32770);
	}

	private boolean decodeFli(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 17218:
		case 17409:
			return decodeC64MulticolorFli(content, 9218, 1026, 2, 0);
		default:
			return false;
		}
	}

	private boolean decodeFbi(byte[] content, int contentLength)
	{
		if (contentLength < 19)
			return false;
		final byte[] unpacked = new byte[17216];
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentOffset = contentLength;
		rle.escape = content[2] & 0xff;
		return rle.unpackBackwards(unpacked, 0, 17215) && decodeC64MulticolorFli(unpacked, 9216, 1024, 0, 0);
	}

	private boolean decodeBml(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 17474:
		case 17665:
		case 17666:
			return decodeC64MulticolorFliBars(content, 9474, 1282, 258, 2);
		default:
			if (contentLength < 5)
				return false;
			final byte[] unpacked = new byte[17472];
			final UifStream rle = new UifStream();
			rle.content = content;
			rle.contentOffset = contentLength;
			rle.escape = content[2] & 0xff;
			return rle.unpackBackwards(unpacked, 0, 17471) && decodeC64MulticolorFliBars(unpacked, 9472, 1280, 256, 0);
		}
	}

	private boolean decodeEmc(byte[] content, int contentLength)
	{
		if (contentLength != 17412)
			return false;
		setSize(296, 192, RECOILResolution.C642X1);
		for (int y = 0; y < 192; y++)
			decodeC64MulticolorLine(content, 8218, 5 + (((y + 4) & 7) << 10), 16389, content[17411] & 0xff, false, y + 4, y * 296);
		return true;
	}

	private boolean decodeFlm(byte[] content, int contentLength)
	{
		if (contentLength == 17410)
			return decodeC64MulticolorFli(content, 9218, 1026, 2, content[17281] & 0xff);
		if (contentLength < 6)
			return false;
		final byte[] unpacked = new byte[17280];
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentOffset = contentLength;
		rle.escape = rle.readValue();
		if (!rle.unpackBackwards(unpacked, 0, 17279))
			return false;
		for (int i = 0; i < 1000; i++) {
			if (unpacked[i] != 0)
				return decodeC64MulticolorFli(unpacked, 9216, 1024, 0, unpacked[17279] & 0xff);
		}
		setSize(296, 200, RECOILResolution.C641X1);
		decodeC64HiresFrame(unpacked, 9240, 1027, true, 40, 0);
		return true;
	}

	private boolean decodeFfli(byte[] content, int contentLength)
	{
		if (contentLength != 26115 || content[2] != 102)
			return false;
		setSize(296, 200, RECOILResolution.C642X1, 2);
		decodeC64MulticolorFliBarsFrame(content, 9499, 1286, 262, 3, 0);
		decodeC64MulticolorFliBarsFrame(content, 9499, 17670, 262, 25859, 59200);
		return applyBlend();
	}

	private boolean decodeIfli(byte[] content, int bitmap1Offset, int bitmap2Offset, int videoMatrix1Offset, int videoMatrix2Offset, int colorOffset, int background)
	{
		setSize(296, 200, RECOILResolution.C641X1, 2);
		decodeC64MulticolorFliFrame(content, bitmap1Offset + 24, videoMatrix1Offset + 3, colorOffset + 3, background, false, 0);
		this.leftSkip = -1;
		decodeC64MulticolorFliFrame(content, bitmap2Offset + 24, videoMatrix2Offset + 3, colorOffset + 3, background, false, 59200);
		return applyBlend();
	}

	private boolean decodePpUnpacked(byte[] content)
	{
		return decodeIfli(content, 9218, 25602, 1026, 17410, 2, content[17281] & 0xff);
	}

	private boolean decodePp(byte[] content, int contentLength)
	{
		if (contentLength < 8)
			return false;
		if (content[2] == 16 && content[3] == 16 && content[4] == 16) {
			final byte[] unpacked = new byte[33602];
			final CmpStream rle = new CmpStream();
			rle.content = content;
			rle.contentOffset = 6;
			rle.contentLength = contentLength;
			rle.escape = content[5] & 0xff;
			return rle.unpack(unpacked, 2, 1, 33602) && rle.readRle() < 0 && decodePpUnpacked(unpacked);
		}
		return contentLength == 33602 && decodePpUnpacked(content);
	}

	private boolean decodeFunUnpacked(byte[] content)
	{
		return decodeIfli(content, 8210, 25594, 18, 17402, 16402, 0);
	}

	private boolean decodeC64Fun(byte[] content, int contentLength)
	{
		if (contentLength < 18 || !isStringAt(content, 2, "FUNPAINT (MT) "))
			return false;
		if (content[16] != 0) {
			final byte[] unpacked = new byte[33694];
			final DrpStream rle = new DrpStream();
			rle.content = content;
			rle.contentOffset = 18;
			rle.contentLength = contentLength;
			rle.escape = content[17] & 0xff;
			return rle.unpack(unpacked, 18, 1, 33694) && rle.readRle() < 0 && decodeFunUnpacked(unpacked);
		}
		return contentLength == 33694 && decodeFunUnpacked(content);
	}

	private void decodeGunFrame(byte[] content, int bitmapOffset, int videoMatrixOffset, int pixelsOffset)
	{
		for (int y = 0; y < 200; y++) {
			int background = content[y < 177 ? 16209 + y : y < 197 ? 18233 + y : 18429] & 0xff;
			decodeC64MulticolorLine(content, bitmapOffset + 24, videoMatrixOffset + 3 + ((y & 7) << 10), 16389, background, false, y, pixelsOffset + y * 296);
		}
	}

	private boolean decodeGun(byte[] content, int contentLength)
	{
		if (contentLength != 33602 && contentLength != 33603)
			return false;
		setSize(296, 200, RECOILResolution.C641X1, 2);
		decodeGunFrame(content, 8194, 2, 0);
		this.leftSkip = -1;
		decodeGunFrame(content, 25602, 17410, 59200);
		return applyBlend();
	}

	private boolean decodeBfli(byte[] content, int contentLength)
	{
		if (contentLength != 33795 || content[2] != 98)
			return false;
		setSize(296, 400, RECOILResolution.C642X1);
		decodeC64MulticolorFliFrame(content, 9243, 1030, 6, 0, false, 0);
		decodeC64MulticolorFliFrame(content, 25603, 17411, 3, 0, true, 59200);
		return true;
	}

	private boolean decodeLp3(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 4098:
		case 4174:
			break;
		default:
			return false;
		}
		if (content[0] != 0 || content[1] != 24)
			return false;
		setSize(320, 400, RECOILResolution.C642X1);
		final byte[] colors = new byte[4];
		if (contentLength == 4174) {
			colors[0] = (byte) (content[2045] & 15);
			colors[1] = (byte) (content[2047] & 15);
			colors[2] = (byte) (content[2048] & 15);
			colors[3] = (byte) (content[2046] & 7);
		}
		else {
			colors[0] = 0;
			colors[1] = 10;
			colors[2] = 2;
			colors[3] = 1;
		}
		for (int y = 0; y < 400; y++) {
			for (int x = 0; x < 320; x++) {
				int c = content[2 + (y >> 3) * 40 + (x >> 3)] & 0xff;
				c = (content[2050 + (c << 3) + (y & 7)] & 0xff) >> (~x & 6) & 3;
				this.pixels[y * 320 + x] = this.c64Palette[colors[c] & 0xff];
			}
		}
		return true;
	}

	private boolean decodeMilPacked(byte[] content, int contentLength, boolean vertical)
	{
		final byte[] unpacked = new byte[10000];
		final XeKoalaStream rle = new XeKoalaStream();
		rle.content = content;
		rle.contentOffset = 22;
		rle.contentLength = contentLength;
		if (!rle.unpack(unpacked, 0, 1, 10000))
			return false;
		setSize(320, 200, RECOILResolution.C642X1);
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 320; x++) {
				int column = x >> 3;
				int charOffset = vertical ? column * 25 + (y >> 3 & 1) * 13 + (y >> 4) : (y & -8) * 5 + column;
				int c;
				switch ((unpacked[2000 + (vertical ? column * 200 + (y & 1) * 100 + (y >> 1) : y * 40 + column)] & 0xff) >> (~x & 6) & 3) {
				case 1:
					c = (unpacked[charOffset] & 0xff) >> 4;
					break;
				case 2:
					c = unpacked[charOffset] & 0xff;
					break;
				case 3:
					c = unpacked[1000 + charOffset] & 0xff;
					break;
				default:
					c = content[8] & 0xff;
					break;
				}
				this.pixels[y * 320 + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private boolean decodeMil(byte[] content, int contentLength)
	{
		if (contentLength < 26)
			return false;
		switch (content[7]) {
		case 0:
			return contentLength == 10022 && decodeC64Multicolor(content, 2022, 22, 1022, 8);
		case 1:
			return decodeMilPacked(content, contentLength, true);
		case 2:
			return decodeMilPacked(content, contentLength, false);
		default:
			return false;
		}
	}

	private boolean decodeVic(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 9002:
		case 9003:
		case 9009:
			return decodeIph(content, contentLength);
		case 10018:
			return decodeOcp(content, contentLength);
		case 10241:
		case 10242:
			return decodeDol(content, contentLength);
		case 17218:
		case 17409:
			return decodeFli(content, contentLength);
		case 17410:
			return decodeFlm(content, contentLength);
		case 17474:
		case 17665:
		case 17666:
			return decodeBml(content, contentLength);
		case 18242:
			return decodeDrl(content, contentLength);
		case 33602:
		case 33603:
			return decodeGun(content, contentLength);
		case 33694:
			return decodeC64Fun(content, contentLength);
		default:
			return false;
		}
	}

	private boolean decodeA(byte[] content, int contentLength)
	{
		if (contentLength != 8130 || content[0] != 66 || content[1] != 0)
			return false;
		setSize(416, 182, RECOILResolution.C642X1);
		for (int y = 0; y < 182; y++) {
			for (int x = 0; x < 416; x++) {
				int c = 11;
				int row = y % 23;
				if (row < 21) {
					int column = x % 26;
					if (column < 24) {
						int spriteNo = x / 26 + (y / 23 << 4);
						if (spriteNo < 127) {
							int spriteOffset = 2 + (spriteNo << 6);
							switch ((content[spriteOffset + row * 3 + (column >> 3)] & 0xff) >> (~column & 6) & 3) {
							case 1:
								c = 0;
								break;
							case 2:
								c = content[spriteOffset + 63] & 15;
								break;
							case 3:
								c = 1;
								break;
							default:
								break;
							}
						}
					}
				}
				this.pixels[y * 416 + x] = this.c64Palette[c];
			}
		}
		return true;
	}

	private boolean decodeSpd(byte[] content, int contentLength)
	{
		if (contentLength < 67)
			return false;
		int headerLength;
		int spriteCount;
		if (content[0] == 83 && content[1] == 80 && content[2] == 68 && content[3] == 1) {
			headerLength = 6;
			spriteCount = (content[4] & 0xff) + 1;
			if (contentLength < 9 + (spriteCount << 6))
				return false;
		}
		else {
			if ((contentLength & 63) != 3 || (content[0] & 0xff) > 15 || (content[1] & 0xff) > 15 || (content[2] & 0xff) > 15)
				return false;
			headerLength = 0;
			spriteCount = contentLength >> 6;
		}
		int resolution = RECOILResolution.C642X1;
		for (int spriteNo = 0; spriteNo < spriteCount; spriteNo++) {
			if ((content[headerLength + 66 + (spriteNo << 6)] & 0xff) < 128) {
				resolution = RECOILResolution.C641X1;
				break;
			}
		}
		int width;
		int height;
		if (spriteCount <= 16) {
			width = spriteCount * 26 - 2;
			height = 21;
		}
		else {
			width = 414;
			height = ((spriteCount + 15) >> 4) * 23 - 2;
		}
		if (!setSize(width, height, resolution))
			return false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int c = 0;
				int row = y % 23;
				if (row < 21) {
					int column = x % 26;
					if (column < 24) {
						int spriteNo = x / 26 + (y / 23 << 4);
						if (spriteNo < spriteCount) {
							int spriteOffset = 3 + (spriteNo << 6);
							int b = content[headerLength + spriteOffset + row * 3 + (column >> 3)] & 0xff;
							if ((content[headerLength + spriteOffset + 63] & 0xff) < 128) {
								if ((b >> (~column & 7) & 1) != 0)
									c = spriteOffset + 63;
							}
							else {
								switch (b >> (~column & 6) & 3) {
								case 1:
									c = 1;
									break;
								case 2:
									c = spriteOffset + 63;
									break;
								case 3:
									c = 2;
									break;
								default:
									break;
								}
							}
						}
					}
				}
				this.pixels[y * width + x] = this.c64Palette[content[headerLength + c] & 15];
			}
		}
		return true;
	}

	private boolean decodePetScreen(byte[] content, int screenOffset, int colorsOffset, int backgroundOffset, int columns, int rows)
	{
		int width = columns << 3;
		int height = rows << 3;
		if (!setSize(width, height, RECOILResolution.C641X1))
			return false;
		byte[] font = CiResource.getByteArray("c64.fnt", 2048);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = (y >> 3) * columns + (x >> 3);
				if (((font[(content[screenOffset + offset] & 0xff) << 3 | (y & 7)] & 0xff) >> (~x & 7) & 1) == 0)
					offset = backgroundOffset;
				else
					offset += colorsOffset;
				this.pixels[y * width + x] = this.c64Palette[content[offset] & 15];
			}
		}
		return true;
	}

	private boolean decodePet(byte[] content, int contentLength)
	{
		return contentLength == 2026 && decodePetScreen(content, 2, 1026, 1003, 40, 25);
	}

	private boolean decodePdr(byte[] content, int contentLength)
	{
		return contentLength == 2029 && decodePetScreen(content, 5, 1029, 3, 40, 25);
	}

	private boolean decodeScrCol(String filename, byte[] content, int contentLength)
	{
		if (contentLength != 1002)
			return false;
		final byte[] colors = new byte[1003];
		if (readCompanionFile(filename, "COL", "col", colors, 1003) != 1002)
			return false;
		setSize(320, 200, RECOILResolution.C641X1);
		byte[] font = CiResource.getByteArray("c64.fnt", 2048);
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 320; x++) {
				int offset = 2 + (y & -8) * 5 + (x >> 3);
				int c;
				if (((font[(content[offset] & 0xff) << 3 | (y & 7)] & 0xff) >> (~x & 7) & 1) == 0)
					c = 0;
				else
					c = colors[offset] & 15;
				this.pixels[y * 320 + x] = this.c64Palette[c];
			}
		}
		return true;
	}

	private boolean decodeFpr(byte[] content, int contentLength)
	{
		if (contentLength != 18370)
			return false;
		setSize(320, 200, RECOILResolution.C642X1);
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 320; x++) {
				int offset = (y >> 3) * 40 + (x >> 3);
				int c = x < 24 ? 255 : content[2178 + offset + ((y & 7) << 10)] & 0xff;
				switch ((content[10370 + (offset << 3) + (y & 7)] & 0xff) >> (~x & 6) & 3) {
				case 0:
					c = 0;
					break;
				case 1:
					c >>= 4;
					break;
				case 3:
					c = x < 24 ? (content[898 + y] & 0xff) >> 4 : content[1154 + offset] & 0xff;
					break;
				default:
					break;
				}
				if (x < 24) {
					switch ((content[2 + (((((y + 1) & 2) != 0 ? 5 : 0) + y / 42) << 6) + (y >> 1) % 21 * 3 + (x >> 3)] & 0xff) >> (~x & 6) & 3) {
					case 1:
						c = content[642 + y] & 0xff;
						break;
					case 2:
						c = content[1098] & 0xff;
						break;
					case 3:
						c = content[1099] & 0xff;
						break;
					default:
						break;
					}
				}
				this.pixels[y * 320 + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private boolean decodeCtm(byte[] content, int contentLength)
	{
		if (contentLength < 30 || content[0] != 67 || content[1] != 84 || content[2] != 77 || content[3] != 5)
			return false;
		int colorMethod = content[8] & 0xff;
		if (colorMethod > 2)
			return false;
		int flags = content[9] & 0xff;
		boolean tiles = (flags & 1) != 0;
		if (colorMethod == 1 && !tiles)
			return false;
		boolean charex = (flags & 2) != 0;
		boolean multi = (flags & 4) != 0;
		int charCount = (content[10] & 0xff) + ((content[11] & 0xff) << 8) + 1;
		int tileCount = tiles ? (content[12] & 0xff) + ((content[13] & 0xff) << 8) + 1 : 0;
		int tileWidth = tiles ? content[14] & 0xff : 1;
		int tileHeight = tiles ? content[15] & 0xff : 1;
		if (tileWidth == 0 || tileHeight == 0)
			return false;
		int mapWidth = content[16] & 0xff | (content[17] & 0xff) << 8;
		int mapHeight = content[18] & 0xff | (content[19] & 0xff) << 8;
		int tilesOffset = 20 + charCount * 9;
		int tileColorsOffset = charex ? tilesOffset : tilesOffset + tileCount * (tileWidth * tileHeight << 1);
		int mapOffset = colorMethod == 1 ? tileColorsOffset + tileCount : tileColorsOffset;
		if (contentLength != mapOffset + (mapWidth * mapHeight << 1))
			return false;
		int width = mapWidth * tileWidth << 3;
		int height = mapHeight * tileHeight << 3;
		if (!setSize(width, height, multi ? RECOILResolution.C642X1 : RECOILResolution.C641X1))
			return false;
		for (int y = 0; y < height; y++) {
			int mapRowOffset = mapOffset + ((y >> 3) / tileHeight * mapWidth << 1);
			for (int x = 0; x < width; x++) {
				int mapTileOffset = mapRowOffset + ((x >> 3) / tileWidth << 1);
				int tile = content[mapTileOffset] & 0xff | (content[mapTileOffset + 1] & 0xff) << 8;
				int ch;
				if (tiles) {
					if (tile >= tileCount)
						return false;
					ch = (tile * tileHeight + (y >> 3) % tileHeight) * tileWidth + (x >> 3) % tileWidth;
					if (!charex) {
						int tileOffset = tilesOffset + (ch << 1);
						ch = content[tileOffset] & 0xff | (content[tileOffset + 1] & 0xff) << 8;
					}
				}
				else
					ch = tile;
				if (ch >= charCount)
					return false;
				int foregroundOffset;
				switch (colorMethod) {
				case 1:
					foregroundOffset = tileColorsOffset + tile;
					break;
				case 2:
					foregroundOffset = 20 + (charCount << 3) + ch;
					break;
				default:
					foregroundOffset = 7;
					break;
				}
				int c = content[20 + (ch << 3) + (y & 7)] & 0xff;
				if (multi) {
					c = c >> (~x & 6) & 3;
					if (c == 3)
						c = content[foregroundOffset] & 7;
					else
						c = content[4 + c] & 0xff;
				}
				else {
					c = c >> (~x & 7) & 1;
					c = content[c == 0 ? 4 : foregroundOffset] & 0xff;
				}
				this.pixels[y * width + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private void decodeEshFrame(byte[] content, int bitmapOffset, int spriteOffset, int pixelsOffset)
	{
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 192; x++) {
				int bit = ~x & 7;
				int column = x >> 3;
				int c;
				if (((content[spriteOffset + (((y / 21 << 3) + column / 3) << 6) + y % 21 * 3 + column % 3] & 0xff) >> bit & 1) != 0)
					c = content[20443 + column / 3] & 0xff;
				else {
					int offset = (y & -8) * 3 + column;
					c = (content[19843 + offset] & 0xff) >> (((content[bitmapOffset + (offset << 3) + (y & 7)] & 0xff) >> bit & 1) << 2);
				}
				this.pixels[pixelsOffset + y * 192 + x] = this.c64Palette[c & 15];
			}
		}
	}

	private boolean decodeEshUnpacked(byte[] content)
	{
		setSize(192, 200, RECOILResolution.C641X1, 2);
		decodeEshFrame(content, 3, 9603, 0);
		decodeEshFrame(content, 4803, 14723, 38400);
		return applyBlend();
	}

	private boolean decodeEsh(byte[] content, int contentLength)
	{
		if (contentLength < 5)
			return false;
		if (content[2] == 0)
			return contentLength == 20454 && decodeEshUnpacked(content);
		final byte[] unpacked = new byte[20452];
		final PgcStream rle = new PgcStream();
		rle.content = content;
		rle.contentOffset = 3;
		rle.contentLength = contentLength;
		return rle.unpack(unpacked, 3, 1, 20452) && decodeEshUnpacked(unpacked);
	}

	private boolean decodeShx(byte[] content, int contentLength)
	{
		if (contentLength == 15362) {
			setSize(144, 168, RECOILResolution.C641X1);
			for (int y = 0; y < 168; y++) {
				for (int x = 0; x < 144; x++) {
					int bit = ~x & 7;
					int column = x >> 3;
					int spriteInLine = column / 3;
					int spriteNo = ((y - 1) & 7) * 15 + spriteInLine;
					if (spriteNo < 105)
						spriteNo += ((spriteInLine + 1) >> 1) * 3;
					int c;
					if (((content[8194 + (spriteNo << 6) + y % 21 * 3 + column % 3] & 0xff) >> bit & 1) != 0)
						c = content[1003] & 0xff;
					else {
						int offset = (y & -8) * 5 + column;
						c = (content[13 + ((y & 7) << 10) + offset] & 0xff) >> (((content[8282 + (offset << 3) + (y & 7)] & 0xff) >> bit & 1) << 2);
					}
					this.pixels[y * 144 + x] = this.c64Palette[c & 15];
				}
			}
			return true;
		}
		if (contentLength < 6)
			return false;
		final byte[] unpacked = new byte[9168];
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentOffset = contentLength;
		rle.escape = rle.readValue();
		if (!rle.unpackBackwards(unpacked, 0, 9167))
			return false;
		setSize(144, 168, RECOILResolution.C641X1);
		for (int y = 0; y < 168; y++) {
			for (int x = 0; x < 144; x++) {
				int bit = ~x & 7;
				int offset = y * 18 + (x >> 3);
				int c;
				if (((unpacked[offset] & 0xff) >> bit & 1) != 0)
					c = unpacked[3025] & 0xff;
				else
					c = (unpacked[6144 + offset] & 0xff) >> (((unpacked[3072 + offset] & 0xff) >> bit & 1) << 2);
				this.pixels[y * 144 + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private static int getShsPixel(byte[] content, int x, int y)
	{
		int bit = ~x & 7;
		int column = x >> 3;
		if (y >= 17 && y < 185 && column >= 2 && column < 14) {
			int spriteColumn = column - 2;
			int spriteInLine = spriteColumn / 3;
			int spriteY = y - 17;
			int spriteOffset = (((spriteInLine << 3) + spriteY / 21) << 6) + spriteY % 21 * 3 + spriteColumn % 3;
			if (((content[10242 + spriteOffset] & 0xff) >> bit & 1) != 0)
				return content[8006 + spriteInLine] & 15;
			if (((content[8194 + spriteOffset] & 0xff) >> bit & 1) != 0)
				return content[8002 + spriteInLine] & 15;
		}
		int offset = (y >> 3) * 40 + column;
		return (content[13314 + offset] & 0xff) >> (((content[2 + (offset << 3) + (y & 7)] & 0xff) >> bit & 1) << 2);
	}

	private boolean decodeShs(byte[] content, int contentLength)
	{
		if (contentLength != 14338)
			return false;
		setSize(320, 200, RECOILResolution.C641X1);
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 320; x++)
				this.pixels[y * 320 + x] = this.c64Palette[getShsPixel(content, x, y) & 15];
		}
		return true;
	}

	private static int getShSpriteOffset(int x, int y, int rowShift)
	{
		return (((y / 21 << rowShift) + x / 24) << 6) + y % 21 * 3 + (x >> 3) % 3;
	}

	private void decodeSh1Frame(byte[] content, int bitmapOffset, int videoMatrixOffset, int screenStride, int foreSpriteOffset, int backSpriteOffset, int foreColorOffset, int backColorOffset, int rowShift, int pixelsOffset)
	{
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < 96; x++) {
				int bit = ~x & 7;
				int spriteOffset = rowShift == 0 ? (x >> 3) * 168 + y : getShSpriteOffset(x, y, rowShift);
				int c;
				if (((content[foreSpriteOffset + spriteOffset] & 0xff) >> bit & 1) != 0)
					c = (content[foreColorOffset + x / 24] & 0xff) >> (foreColorOffset == backColorOffset ? 4 : 0);
				else if (((content[backSpriteOffset + spriteOffset] & 0xff) >> bit & 1) != 0)
					c = content[backColorOffset + x / 24] & 0xff;
				else {
					int offset = (y >> 3) * screenStride + (x >> 3);
					c = (content[videoMatrixOffset + offset] & 0xff) >> (((content[bitmapOffset + (offset << 3) + (y & 7)] & 0xff) >> bit & 1) << 2);
				}
				this.pixels[pixelsOffset + y * 96 + x] = this.c64Palette[c & 15];
			}
		}
	}

	private boolean decodeSh1Unpacked(byte[] content, int bitmapOffset, int videoMatrixOffset, int screenStride, int foreSpriteOffset, int backSpriteOffset, int foreColorOffset, int backColorOffset, int rowShift)
	{
		setSize(96, 168, RECOILResolution.C641X1);
		decodeSh1Frame(content, bitmapOffset, videoMatrixOffset, screenStride, foreSpriteOffset, backSpriteOffset, foreColorOffset, backColorOffset, rowShift, 0);
		return true;
	}

	private static boolean unpackSh(byte[] content, int contentLength, byte[] unpacked, int unpackedLength)
	{
		if (contentLength < 3)
			return false;
		final DrpStream rle = new DrpStream();
		rle.content = content;
		rle.contentOffset = 1;
		rle.contentLength = contentLength;
		rle.escape = content[0] & 0xff;
		return rle.unpack(unpacked, 0, 1, unpackedLength);
	}

	private boolean decodeSh1(byte[] content, int contentLength)
	{
		if (contentLength == 14770)
			return decodeSh1Unpacked(content, 6690, 448, 40, 4530, 2482, 430, 426, 2);
		final byte[] unpacked = new byte[6304];
		return unpackSh(content, contentLength, unpacked, 6304) && decodeSh1Unpacked(unpacked, 0, 6048, 12, 4032, 2016, 6300, 6300, 0);
	}

	private boolean decodeShiUnpacked(byte[] content)
	{
		setSize(96, 200, RECOILResolution.C641X1, 2);
		decodeSh1Frame(content, 3, 15043, 12, 4803, 5059, 15343, 15347, 3, 0);
		decodeSh1Frame(content, 2403, 15043, 12, 9923, 10179, 15343, 15347, 3, 19200);
		return applyBlend();
	}

	private boolean decodeShi(byte[] content, int contentLength)
	{
		if (contentLength == 15355 && content[2] == 0)
			return decodeShiUnpacked(content);
		final byte[] unpacked = new byte[15355];
		final PgcStream rle = new PgcStream();
		rle.content = content;
		rle.contentOffset = 3;
		rle.contentLength = contentLength;
		return rle.unpack(unpacked, 3, 1, 15355) && decodeShiUnpacked(unpacked);
	}

	private static final int SH2_SPRITE_OFFSET = 4032;

	private boolean decodeSuperHires2(byte[] content, int bitmapOffset, int videoMatrixOffset, int screenStride, int spritesOffset, int spritesY, int spriteColorsOffset)
	{
		setSize(192, 168, RECOILResolution.C641X1);
		for (int y = 0; y < 168; y++) {
			for (int x = 0; x < 192; x++) {
				int bit = ~x & 7;
				int c;
				if (y >= spritesY && ((content[spritesOffset + (spritesOffset == 4032 ? (x >> 3) * 168 + y : getShSpriteOffset(x, y - spritesY, 3))] & 0xff) >> bit & 1) != 0)
					c = content[spriteColorsOffset + x / 24] & 0xff;
				else {
					int offset = (y >> 3) * screenStride + (x >> 3);
					c = (content[videoMatrixOffset + offset] & 0xff) >> (((content[bitmapOffset + (offset << 3) + (y & 7)] & 0xff) >> bit & 1) << 2);
				}
				this.pixels[y * 192 + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private boolean decodeSh2(byte[] content, int contentLength)
	{
		if (contentLength == 14770)
			return decodeSuperHires2(content, 6642, 442, 40, 2482, 0, 426);
		final byte[] unpacked = new byte[8576];
		return unpackSh(content, contentLength, unpacked, 8576) && decodeSuperHires2(unpacked, 0, 8064, 24, 4032, 0, 8568);
	}

	private static int getShe1Pixel(byte[] content, int x, int y)
	{
		int bit = ~x & 7;
		if (y < 84) {
			int spriteOffset = getShSpriteOffset(x, y, 3);
			if (((content[1446 + spriteOffset] & 0xff) >> bit & 1) != 0)
				return content[3239] & 0xff;
			if (((content[1190 + spriteOffset] & 0xff) >> bit & 1) != 0)
				return content[3238] & 0xff;
		}
		int offset = (y >> 3) * 12 + (x >> 3);
		return (content[1058 + offset] & 0xff) >> (((content[2 + (offset << 3) + (y & 7)] & 0xff) >> bit & 1) << 2);
	}

	private boolean decodeShe(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 3250:
			setSize(96, 88, RECOILResolution.C641X1);
			for (int y = 0; y < 88; y++) {
				for (int x = 0; x < 96; x++)
					this.pixels[y * 96 + x] = this.c64Palette[getShe1Pixel(content, x, y) & 15];
			}
			return true;
		case 8642:
			return decodeSuperHires2(content, 2, 8130, 24, 4034, 1, 8634);
		default:
			return false;
		}
	}

	private static int getIshPixel(byte[] content, int contentOffset, int x, int y)
	{
		int bit = ~x & 7;
		int column = x >> 3;
		if (column < 12) {
			int spriteInLine = column / 3;
			int spriteOffset = contentOffset + ((spriteInLine * 10 + y / 21) << 6) + y % 21 * 3 + column % 3;
			if (((content[10752 + spriteOffset] & 0xff) >> bit & 1) != 0)
				return content[contentOffset + 15364 + spriteInLine] & 0xff;
			if (((content[8192 + spriteOffset] & 0xff) >> bit & 1) != 0)
				return content[contentOffset + 15360 + spriteInLine] & 0xff;
		}
		int offset = (y & -8) * 5 + column;
		return (content[contentOffset + 13312 + column] & 0xff) >> (((content[contentOffset + (offset << 3) + (y & 7)] & 0xff) >> bit & 1) << 2);
	}

	private void decodeIshFrame(byte[] content, int contentOffset, int pixelsOffset)
	{
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 320; x++)
				this.pixels[pixelsOffset + y * 320 + x] = this.c64Palette[getIshPixel(content, contentOffset, x, y) & 15];
		}
	}

	private boolean decodeIsh(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 9194:
			return decodeHed(content);
		case 30738:
			setSize(320, 200, RECOILResolution.C641X1, 2);
			decodeIshFrame(content, 2, 0);
			decodeIshFrame(content, 15370, 64000);
			return applyBlend();
		default:
			return false;
		}
	}

	private static int getShfPixel(byte[] content, int x, int y)
	{
		int column = x >> 3;
		int bit = ~x & 7;
		if (column < 12) {
			int spriteIndex = ((y & 7) << 3) + column / 3;
			int spriteOffset = y % 21 * 3 + column % 3;
			if (((content[2 + ((GET_SHF_PIXEL_SPRITES[spriteIndex] & 0xff) << 6) + spriteOffset] & 0xff) >> bit & 1) != 0)
				return content[1002] & 0xff;
			if (((content[2 + ((GET_SHF_PIXEL_SPRITES[spriteIndex + 4] & 0xff) << 6) + spriteOffset] & 0xff) >> bit & 1) != 0)
				return content[1003] & 0xff;
		}
		y++;
		int offset = (y & -8) * 5 + column;
		return (content[16 + ((y & 7) << 10) + offset] & 0xff) >> (((content[8306 + (offset << 3) + (y & 7)] & 0xff) >> bit & 1) << 2);
	}

	private void decodeShfFrame(byte[] content, int foreColor, int backColor, int pixelsOffset)
	{
		for (int y = 0; y < 167; y++) {
			for (int x = 0; x < 96; x++) {
				int offset = y * 12 + (x >> 3);
				int bit = ~x & 7;
				int c;
				if (((content[offset] & 0xff) >> bit & 1) != 0)
					c = foreColor;
				else if (((content[2048 + offset] & 0xff) >> bit & 1) != 0)
					c = backColor;
				else
					c = (content[6144 + offset] & 0xff) >> (((content[4096 + offset] & 0xff) >> bit & 1) << 2);
				this.pixels[pixelsOffset + y * 96 + x] = this.c64Palette[c & 15];
			}
		}
	}

	private boolean decodeShf(byte[] content, int contentLength)
	{
		if (contentLength == 15874) {
			setSize(208, 167, RECOILResolution.C641X1);
			for (int y = 0; y < 167; y++) {
				for (int x = 0; x < 208; x++)
					this.pixels[y * 208 + x] = this.c64Palette[getShfPixel(content, x, y) & 15];
			}
			return true;
		}
		if (contentLength < 6)
			return false;
		final byte[] unpacked = new byte[8170];
		final UflStream rle = new UflStream();
		rle.content = content;
		rle.contentOffset = 3;
		rle.contentLength = contentLength;
		rle.escape = content[2] & 0xff;
		if (!rle.unpack(unpacked, 0, 1, 8170))
			return false;
		setSize(96, 167, RECOILResolution.C641X1);
		decodeShfFrame(unpacked, unpacked[8168] & 0xff, unpacked[8168] & 0xff, 0);
		return true;
	}

	private boolean decodeC64Sif(byte[] content, int contentLength)
	{
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentLength = contentLength;
		if (!rle.startSifFrame(0))
			return false;
		int frame2Offset = rle.contentOffset;
		final byte[] unpacked = new byte[8176];
		if (!rle.unpackBackwards(unpacked, 0, 8175) || !rle.startSifFrame(frame2Offset) || contentLength != rle.contentOffset + 4)
			return false;
		setSize(96, 167, RECOILResolution.C641X1, 2);
		decodeShfFrame(unpacked, content[contentLength - 4] & 0xff, content[contentLength - 3] & 0xff, 0);
		if (!rle.unpackBackwards(unpacked, 0, 8175))
			return false;
		decodeShfFrame(unpacked, content[contentLength - 2] & 0xff, content[contentLength - 1] & 0xff, 16032);
		return applyBlend();
	}

	private void decodeUflFrame(byte[] content, int bitmapOffset, int videoMatrixOffset, int spritesOffset, int spriteColorsOffset, int pixelsOffset)
	{
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 288; x++) {
				int column = x >> 3;
				int offset = 3 + (y & -8) * 5 + column;
				int c = content[videoMatrixOffset + ((y & 6) << 9) + offset] & 0xff;
				if (((content[bitmapOffset + (offset << 3) + (y & 7)] & 0xff) >> (~x & 7) & 1) != 0)
					c >>= 4;
				else if (((content[spritesOffset + ((y / 40 * 12 + (y & 2) * 3 + column / 6) << 6) + ((y + 1) >> 1) % 21 * 3 + (column >> 1) % 3] & 0xff) >> (~x >> 1 & 7) & 1) != 0)
					c = content[spriteColorsOffset == 4084 ? 4084 + column / 6 : spriteColorsOffset] & 0xff;
				this.pixels[pixelsOffset + y * 288 + x] = this.c64Palette[c & 15];
			}
		}
	}

	private boolean decodeUflUnpacked(byte[] content)
	{
		setSize(288, 200, RECOILResolution.C641X1);
		decodeUflFrame(content, 8194, 4098, 2, content[4081] == 1 ? 4084 : 4082, 0);
		return true;
	}

	private boolean decodeUfl(byte[] content, int contentLength)
	{
		if (contentLength == 16194)
			return decodeUflUnpacked(content);
		if (contentLength < 6)
			return false;
		final byte[] unpacked = new byte[16194];
		final UflStream rle = new UflStream();
		rle.content = content;
		rle.contentOffset = 3;
		rle.contentLength = contentLength;
		rle.escape = content[2] & 0xff;
		return rle.unpack(unpacked, 2, 1, 16194) && decodeUflUnpacked(unpacked);
	}

	private boolean decodeUif(byte[] content, int contentLength)
	{
		if (contentLength < 6)
			return false;
		final byte[] unpacked = new byte[32576];
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentOffset = contentLength;
		rle.escape = content[2] & 0xff;
		if (!rle.unpackBackwards(unpacked, 0, 32575))
			return false;
		setSize(288, 200, RECOILResolution.C641X1, 2);
		decodeUflFrame(unpacked, 8192, 0, 4096, 4080, 0);
		decodeUflFrame(unpacked, 24576, 16384, 20480, 4080, 57600);
		return applyBlend();
	}

	private boolean decodeXfl(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[16192];
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentOffset = contentLength;
		rle.escape = content[2] & 0xff;
		if (!rle.unpackBackwards(unpacked, 0, 16191))
			return false;
		final byte[] spriteColors = new byte[16];
		System.arraycopy(unpacked, 8174, spriteColors, 1, 10);
		setSize(192, 167, RECOILResolution.C641X1);
		for (int y = 0; y < 167; y++) {
			if (y >= 3 && (y & 1) != 0) {
				int offset = (y - 3) >> 1;
				offset = (offset / 28 << 10) + offset % 28;
				spriteColors[unpacked[988 + offset] & 15] = (byte) (unpacked[960 + offset] & 0xff);
			}
			for (int x = 0; x < 192; x++) {
				int column = x >> 3;
				int offset = ((1 + y) & -8) * 5 + 16 + column;
				int c;
				if (((unpacked[8192 + (offset << 3) + ((1 + y) & 7)] & 0xff) >> (~x & 7) & 1) != 0)
					c = (unpacked[(((y - 1) & 7) << 10) + offset] & 0xff) >> 4;
				else {
					int spriteInLine = column / 3;
					switch ((unpacked[((DECODE_XFL_SPRITES[((y & 7) << 3) + spriteInLine] & 0xff) << 6) + (y >> 1) % 21 * 3 + column % 3] & 0xff) >> (~x & 6) & 3) {
					case 1:
						c = spriteColors[1] & 0xff;
						break;
					case 2:
						c = spriteColors[3 + spriteInLine] & 0xff;
						break;
					case 3:
						c = spriteColors[2] & 0xff;
						break;
					default:
						c = unpacked[(((y - 1) & 7) << 10) + offset] & 0xff;
						break;
					}
				}
				this.pixels[y * 192 + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private static int getNufVideoMatrixOffset(int y)
	{
		if (y < 128)
			return 8194 + ((~y >> 1 & 7) << 10);
		else
			return 2 + ((((y >> 1) + (y >= 168 ? 1 : 0)) & 3) << 10);
	}

	private static int getMufTableOffset(int spriteInLine)
	{
		return ((spriteInLine & 6) << 9) + ((spriteInLine & 1) << 7);
	}

	private boolean decodeMufFrame(byte[] content, int pixelsOffset)
	{
		for (int y = 0; y < 200; y++) {
			int videoMatrixOffset = getNufVideoMatrixOffset(y) - 256;
			for (int x = 0; x < 296; x++) {
				int column = x >> 3;
				int offset = (y & -8) * 5 + 3 + column;
				int c = content[videoMatrixOffset + offset] & 0xff;
				if (((content[16130 - ((y & 128) << 7) + (offset << 3) + (y & 7)] & 0xff) >> (~x & 7) & 1) != 0)
					c >>= 4;
				else if (column < 36) {
					int spriteInLine = column / 6;
					offset = 124 - ((y & 128) << 1) + (content[videoMatrixOffset + 1017 + spriteInLine] & 0xff);
					if (offset < 0 || offset >= 340)
						return false;
					int b = content[2 + (offset << 6) + ((((y + 47) >> 1) * 3 + (column >> 1) % 3 + (y < 123 ? 0 : y < 165 ? 1 : 2)) & 63)] & 0xff;
					offset = (y + 1) >> 1;
					if (((content[4610 + offset] & 0xff) >> spriteInLine & 2) != 0) {
						switch (b >> (~x >> 1 & 6) & 3) {
						case 1:
							c = content[7923] & 0xff;
							break;
						case 2:
							c = content[770 + getMufTableOffset(spriteInLine) + offset] & 0xff;
							break;
						case 3:
							c = content[7922] & 0xff;
							break;
						default:
							break;
						}
					}
					else if ((b >> (~x >> 1 & 7) & 1) != 0)
						c = content[770 + getMufTableOffset(spriteInLine) + offset] & 0xff;
				}
				this.pixels[pixelsOffset + y * 296 + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private boolean decodeMuf(byte[] content, int contentLength)
	{
		if (contentLength != 21826)
			return false;
		setSize(296, 200, RECOILResolution.C641X1);
		return decodeMufFrame(content, 0);
	}

	private boolean decodeMup(byte[] content, int contentLength)
	{
		if (contentLength < 6)
			return false;
		final byte[] unpacked = new byte[21762];
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentOffset = contentLength;
		rle.escape = content[2] & 0xff;
		return rle.unpackBackwards(unpacked, 2, 21761) && decodeMuf(unpacked, 21826);
	}

	private boolean decodeMui(byte[] content, int contentLength)
	{
		if (contentLength != 44034)
			return false;
		setSize(296, 200, RECOILResolution.C641X1, 2);
		if (!decodeMufFrame(content, 0))
			return false;
		final byte[] second = new byte[21762];
		System.arraycopy(content, 32770, second, 2, 11264);
		System.arraycopy(content, 22018, second, 11266, 10496);
		return decodeMufFrame(second, 59200) && applyBlend();
	}

	private boolean decodeNuf(byte[] content, int contentLength)
	{
		if (contentLength != 23042)
			return false;
		setSize(320, 200, RECOILResolution.C641X1);
		final byte[] spriteColors = new byte[10];
		spriteColors[0] = (byte) (content[8185] & 0xff);
		for (int i = 0; i < 6; i++)
			spriteColors[1 + i] = (byte) (content[1026 + getMufTableOffset(i)] & 0xff);
		spriteColors[7] = (byte) (content[8179] & 0xff);
		spriteColors[8] = (byte) (content[8184] & 0xff);
		spriteColors[9] = (byte) (content[8178] & 0xff);
		for (int y = 0; y < 200; y++) {
			if (y > 0) {
				for (int i = 0; i < 6; i++) {
					int b = content[1026 + getMufTableOffset(i) + ((y + 1) >> 1)] & 0xff;
					switch (b >> 4) {
					case 0:
						if ((y & 1) != 0)
							spriteColors[1 + i] = (byte) b;
						break;
					case 5:
						if ((y & 1) == (i == 0 ? 1 : 0))
							spriteColors[7] = (byte) b;
						break;
					case 6:
						if ((y & 1) == (i == 0 ? 1 : 0))
							spriteColors[9] = (byte) b;
						break;
					case 7:
						if ((y & 1) == (i == 0 ? 1 : 0))
							spriteColors[0] = (byte) b;
						break;
					case 14:
						if ((y & 1) == (i == 0 ? 1 : 0))
							spriteColors[8] = (byte) b;
						break;
					default:
						break;
					}
				}
			}
			int videoMatrixOffset = getNufVideoMatrixOffset(y);
			for (int x = 0; x < 320; x++) {
				int column = x >> 3;
				int offset = (y & -8) * 5 + column;
				int c = column >= 3 || (y & 6) == 0 ? content[videoMatrixOffset + offset] & 0xff : 255;
				if (((content[(y < 128 ? 16386 : 2) + (offset << 3) + (y & 7)] & 0xff) >> (~x & 7) & 1) != 0)
					c >>= 4;
				else if (column < 39) {
					int spriteInLine = (3 + column) / 6;
					offset = 128 - ((y & 128) << 1) + (content[videoMatrixOffset + 1016 + spriteInLine] & 0xff);
					if (offset < 0 || offset >= 360)
						return false;
					int spriteX = column < 3 ? x : ((x - 24) >> 1) % 24;
					int b = content[2 + (offset << 6) + ((((y + 47) >> 1) * 3 + (spriteX >> 3) + (y < 123 ? 0 : y < 165 ? 1 : 2)) & 63)] & 0xff;
					if ((b >> (~spriteX & 7) & 1) != 0)
						c = spriteColors[spriteInLine] & 0xff;
					else if (column < 3) {
						offset = 128 - ((y & 128) << 1) + (content[videoMatrixOffset + 1023] & 0xff);
						if (offset < 0 || offset >= 360)
							return false;
						b = (content[2 + (offset << 6) + ((((y + 47) >> 1) * 3 + column + (y < 123 ? 0 : y < 165 ? 1 : 2)) & 63)] & 0xff) >> (~x & 6) & 3;
						if (b != 0)
							c = spriteColors[6 + b] & 0xff;
					}
				}
				this.pixels[y * 320 + x] = this.c64Palette[c & 15];
			}
		}
		return true;
	}

	private boolean decodeNup(byte[] content, int contentLength)
	{
		if (contentLength < 7 || content[2] != -3)
			return false;
		final byte[] unpacked = new byte[23042];
		final UifStream rle = new UifStream();
		rle.content = content;
		rle.contentOffset = contentLength;
		rle.escape = content[3] & 0xff;
		return rle.unpackBackwards(unpacked, 2, 23041) && decodeNuf(unpacked, 23042);
	}

	private boolean decodeDoo(byte[] content, int contentLength)
	{
		setSize(640, 400, RECOILResolution.ST1X1);
		return decodeBlackAndWhite(content, 0, contentLength, false, 16777215);
	}

	private boolean decodeDa4(byte[] content, int contentLength)
	{
		setSize(640, 800, RECOILResolution.ST1X1);
		return decodeBlackAndWhite(content, 0, contentLength, false, 16777215);
	}

	private boolean decodeStCmp(byte[] content, int contentLength)
	{
		if (contentLength < 5)
			return false;
		switch (content[1]) {
		case 0:
			setSize(640, 400, RECOILResolution.ST1X1);
			break;
		case -56:
			setSize(640, 800, RECOILResolution.ST1X1);
			break;
		default:
			return false;
		}
		final CmpStream rle = new CmpStream();
		rle.content = content;
		rle.contentOffset = 2;
		rle.contentLength = contentLength;
		rle.escape = content[0] & 0xff;
		return decodeRleBlackAndWhite(rle, 16777215);
	}

	private boolean decodeBld(byte[] content, int contentLength)
	{
		if (contentLength < 5)
			return false;
		int width = (content[0] & 0xff) << 8 | content[1] & 0xff;
		int height = (content[2] & 0xff) << 8 | content[3] & 0xff;
		if ((content[0] & 0xff) < 128)
			return setSize(width, height, RECOILResolution.ST1X1) && decodeBlackAndWhite(content, 4, contentLength, false, 16777215);
		if (!setSize(65536 - width, height, RECOILResolution.ST1X1))
			return false;
		final BldStream rle = new BldStream();
		rle.content = content;
		rle.contentOffset = 4;
		rle.contentLength = contentLength;
		return decodeRleBlackAndWhite(rle, 16777215);
	}

	private boolean decodeCrg(byte[] content, int contentLength)
	{
		if (contentLength < 43 || !isStringAt(content, 0, "CALAMUSCRG") || content[10] != 3 || content[11] != -24 || content[12] != 0 || content[13] != 2)
			return false;
		int width = get32BigEndian(content, 20);
		int height = get32BigEndian(content, 24);
		if (!setSize(width, height, RECOILResolution.ST1X1))
			return false;
		final CciStream rle = new CciStream();
		rle.content = content;
		rle.contentOffset = 42;
		rle.contentLength = contentLength;
		return decodeRleBlackAndWhite(rle, 16777215);
	}

	private boolean decodePac(byte[] content, int contentLength)
	{
		if (contentLength < 8 || content[0] != 112 || content[1] != 77 || content[2] != 56)
			return false;
		int unpackedStride;
		switch (content[3]) {
		case 53:
			unpackedStride = 1;
			break;
		case 54:
			unpackedStride = 80;
			break;
		default:
			return false;
		}
		final PacStream rle = new PacStream();
		rle.content = content;
		rle.contentOffset = 7;
		rle.contentLength = contentLength;
		final byte[] unpacked = new byte[32000];
		return rle.unpackColumns(unpacked, 0, unpackedStride, 32000) && decodeDoo(unpacked, 32000);
	}

	private static void fillPscLine(byte[] unpacked, int unpackedOffset, int unpackedStride, int value)
	{
		for (int i = 0; i < unpackedStride; i++)
			unpacked[unpackedOffset + i] = (byte) value;
	}

	private static int copyPscLines(byte[] unpacked, int unpackedOffset, int unpackedStride, int unpackedLength, int count)
	{
		if (unpackedOffset < unpackedStride || unpackedOffset + count * unpackedStride > unpackedLength)
			return -1;
		do {
			System.arraycopy(unpacked, unpackedOffset - unpackedStride, unpacked, unpackedOffset, unpackedStride);
			unpackedOffset += unpackedStride;
		}
		while (--count > 0);
		return unpackedOffset;
	}

	private boolean decodePsc(byte[] content, int contentLength)
	{
		if (contentLength < 18 || !isStringAt(content, 0, "tm89") || content[8] != 2 || content[9] != 1)
			return false;
		int width = ((content[10] & 0xff) << 8) + (content[11] & 0xff) + 1;
		int height = ((content[12] & 0xff) << 8) + (content[13] & 0xff) + 1;
		if (width > 640 || height > 400)
			return false;
		setSize(width, height, RECOILResolution.ST1X1);
		int unpackedStride = (width + 7) >> 3;
		int unpackedLength = unpackedStride * height;
		if (content[14] == 99 && contentLength == 16 + unpackedLength && content[15 + unpackedLength] == -1)
			return decodeBlackAndWhite(content, 15, contentLength - 1, false, 16777215);
		final byte[] unpacked = new byte[32000];
		int contentOffset = 14;
		for (int unpackedOffset = 0; unpackedOffset < unpackedLength;) {
			if (contentOffset + 1 >= contentLength)
				return false;
			switch (content[contentOffset++]) {
			case 0:
				fillPscLine(unpacked, unpackedOffset, unpackedStride, 0);
				unpackedOffset += unpackedStride;
				break;
			case 10:
				unpackedOffset = copyPscLines(unpacked, unpackedOffset, unpackedStride, unpackedLength, 1 + (content[contentOffset++] & 0xff));
				if (unpackedOffset < 0)
					return false;
				break;
			case 12:
				unpackedOffset = copyPscLines(unpacked, unpackedOffset, unpackedStride, unpackedLength, 257 + (content[contentOffset++] & 0xff));
				if (unpackedOffset < 0)
					return false;
				break;
			case 100:
				fillPscLine(unpacked, unpackedOffset, unpackedStride, content[contentOffset++] & 0xff);
				unpackedOffset += unpackedStride;
				break;
			case 102:
				if (contentOffset + 2 >= contentLength)
					return false;
				for (int i = 0; i < unpackedStride; i++)
					unpacked[unpackedOffset + i] = (byte) (content[contentOffset + (i & 1)] & 0xff);
				contentOffset += 2;
				unpackedOffset += unpackedStride;
				break;
			case 110:
				if (contentOffset + unpackedStride >= contentLength)
					return false;
				System.arraycopy(content, contentOffset, unpacked, unpackedOffset, unpackedStride);
				contentOffset += unpackedStride;
				unpackedOffset += unpackedStride;
				break;
			case -56:
				fillPscLine(unpacked, unpackedOffset, unpackedStride, 255);
				unpackedOffset += unpackedStride;
				break;
			default:
				return false;
			}
		}
		if (contentOffset >= contentLength || content[contentOffset] != -1)
			return false;
		return decodeBlackAndWhite(unpacked, 0, unpackedLength, false, 16777215);
	}

	private boolean decodeCp3(byte[] content, int contentLength)
	{
		if (contentLength < 4)
			return false;
		int countLength = (1 + ((content[0] & 0xff) << 8) + (content[1] & 0xff)) << 2;
		if (contentLength <= countLength)
			return false;
		int valueOffset = countLength;
		final byte[] unpacked = new byte[32000];
		int unpackedOffset = 0;
		int count;
		for (int countOffset = 4; countOffset < countLength; countOffset += 4) {
			count = ((content[countOffset] & 0xff) << 8 | content[countOffset + 1] & 0xff) << 3;
			if (valueOffset + count + 8 > contentLength || unpackedOffset + count > 32000)
				return false;
			System.arraycopy(content, valueOffset, unpacked, unpackedOffset, count);
			valueOffset += count;
			unpackedOffset += count;
			count = ((content[countOffset + 2] & 0xff) << 8 | content[countOffset + 3] & 0xff) << 3;
			if (unpackedOffset + count > 32000)
				return false;
			for (int offset = 0; offset < count; offset += 8)
				System.arraycopy(content, valueOffset, unpacked, unpackedOffset + offset, 8);
			valueOffset += 8;
			unpackedOffset += count;
		}
		count = 32000 - unpackedOffset;
		if (valueOffset + count != contentLength)
			return false;
		System.arraycopy(content, valueOffset, unpacked, unpackedOffset, count);
		return decodeDoo(unpacked, 32000);
	}

	private boolean decodeStFnt(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 2050:
			setSize(256, 64, RECOILResolution.ST1X1);
			break;
		case 4096:
		case 4098:
			setSize(256, 128, RECOILResolution.ST1X1);
			break;
		default:
			return false;
		}
		if ((contentLength & 2) != 0) {
			if (content[contentLength - 2] != 0 || (content[contentLength - 2] & 0xff) > 1)
				return false;
			contentLength -= 2;
		}
		decodeBlackAndWhiteFont(content, 0, contentLength, 16);
		return true;
	}

	private boolean decodeGdosFnt(byte[] content, int contentLength)
	{
		if (contentLength < 88 || content[62] != 85 || content[63] != 85)
			return false;
		final GdosFntStream stream = new GdosFntStream();
		if (content[3] == 0) {
			if (content[2] == 0)
				return false;
			stream.bigEndian = false;
		}
		else if (content[2] == 0)
			stream.bigEndian = true;
		else
			return false;
		stream.content = content;
		stream.contentLength = contentLength;
		stream.contentOffset = 36;
		int firstCharacter = stream.readWord();
		int lastCharacter = stream.readWord();
		if (firstCharacter > lastCharacter)
			return false;
		stream.contentOffset = 72;
		int characterOffset = stream.readInt();
		if (characterOffset <= 0 || characterOffset >= contentLength)
			return false;
		int bitmapOffset = stream.readInt();
		if (bitmapOffset < 0 || bitmapOffset >= contentLength)
			return false;
		int bytesPerLine = stream.readWord();
		if (bytesPerLine == 0)
			return false;
		int height = stream.readWord();
		if (height == 0 || bitmapOffset + height * bytesPerLine > contentLength)
			return false;
		int characterEndOffset = characterOffset + ((lastCharacter - firstCharacter + 2) << 1);
		if (characterEndOffset > bitmapOffset)
			return false;
		int width = height << 4;
		if (width > 3840)
			width = 3840;
		stream.contentOffset = characterOffset;
		stream.contentLength = characterEndOffset;
		stream.bitmapWidth = bytesPerLine << 3;
		stream.width = width;
		stream.nextX = stream.rightX = 0;
		int row;
		for (row = 0; stream.nextX >= 0; row += height) {
			if (!stream.fitRow())
				return false;
		}
		if (!setSize(width, row, RECOILResolution.ST1X1))
			return false;
		stream.contentOffset = characterOffset;
		stream.nextX = stream.rightX = 0;
		for (row = 0; stream.nextX >= 0; row += height) {
			stream.fitRow();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int fontX = stream.leftX + x;
					int c;
					if (fontX < stream.rightX) {
						int offset = bitmapOffset + y * bytesPerLine + (fontX >> 3);
						c = ((content[offset] & 0xff) >> (~fontX & 7) & 1) == 0 ? 16777215 : 0;
					}
					else
						c = 16777215;
					this.pixels[(row + y) * width + x] = c;
				}
			}
		}
		return true;
	}

	private static boolean isStePalette(byte[] content, int contentOffset, int colors)
	{
		while (--colors >= 0) {
			if ((content[contentOffset] & 8) != 0 || (content[contentOffset + 1] & 136) != 0)
				return true;
			contentOffset += 2;
		}
		return false;
	}

	private int getStColor(byte[] content, int contentOffset)
	{
		int r = content[contentOffset] & 0xff;
		int gb = content[contentOffset + 1] & 0xff;
		int rgb;
		switch (this.resolution) {
		case RECOILResolution.ST1X1:
		case RECOILResolution.ST1X2:
			rgb = (r & 7) << 16 | (gb & 112) << 4 | (gb & 7);
			return rgb << 5 | rgb << 2 | (rgb >> 1 & 197379);
		default:
			rgb = (r & 7) << 17 | (r & 8) << 13 | (gb & 112) << 5 | (gb & 135) << 1 | (gb & 8) >> 3;
			return rgb << 4 | rgb;
		}
	}

	final void setStPalette(byte[] content, int contentOffset, int colors)
	{
		for (int c = 0; c < colors; c++)
			this.contentPalette[c] = getStColor(content, contentOffset + c * 2);
	}

	static int getSteInterlacedColor(int rgb)
	{
		rgb = (rgb & 1792) << 10 | (rgb & 2160) << 6 | (rgb & 16519) << 2 | (rgb & 8192) >> 5 | (rgb & 8) >> 2 | (rgb & 4096) >> 12;
		return rgb << 3 | (rgb >> 2 & 460551);
	}

	private static int getStVdiColor(byte[] content, int contentOffset)
	{
		int rgb = 0;
		for (int i = 0; i < 6; i += 2) {
			int c = (content[contentOffset + i] & 0xff) << 8 | content[contentOffset + i + 1] & 0xff;
			c = c < 1000 ? c * 255 / 1000 : 255;
			rgb = rgb << 8 | c;
		}
		return rgb;
	}

	final void setStVdiColor(int i, int rgb, int bitplanes)
	{
		switch (i) {
		case 1:
			i = (1 << bitplanes) - 1;
			break;
		case 2:
			i = 1;
			break;
		case 3:
			i = 2;
			break;
		case 5:
			i = 6;
			break;
		case 6:
			i = 3;
			break;
		case 7:
			i = 5;
			break;
		case 8:
			i = 7;
			break;
		case 9:
			i = 8;
			break;
		case 10:
			i = 9;
			break;
		case 11:
			i = 10;
			break;
		case 13:
			i = 14;
			break;
		case 14:
			i = 11;
			break;
		case 15:
			i = 13;
			break;
		case 255:
			i = 15;
			break;
		default:
			break;
		}
		this.contentPalette[i] = rgb;
	}

	private void setStVdiPalette(byte[] content, int contentOffset, int colors, int bitplanes)
	{
		for (int i = 0; i < colors; i++)
			setStVdiColor(i, getStVdiColor(content, contentOffset + i * 6), bitplanes);
	}

	private static int getStLowPixel(byte[] content, int contentOffset, int x)
	{
		return getBitplaneWordsPixel(content, contentOffset, x, 4);
	}

	private boolean decodeStLowWithStride(byte[] bitmap, int bitmapOffset, int bitmapStride, byte[] palette, int paletteOffset, int width, int height, int frames)
	{
		setSize(width, height, isStePalette(palette, paletteOffset, 16) ? RECOILResolution.STE1X1 : RECOILResolution.ST1X1, frames);
		setStPalette(palette, paletteOffset, 16);
		decodeBitplanes(bitmap, bitmapOffset, bitmapStride, 4, 0, width, height);
		return true;
	}

	private boolean decodeStLow(byte[] bitmap, int bitmapOffset, byte[] palette, int paletteOffset, int width, int height, int frames)
	{
		return decodeStLowWithStride(bitmap, bitmapOffset, (width + 15) >> 4 << 3, palette, paletteOffset, width, height, frames);
	}

	private boolean decodeStLow(byte[] bitmap, int bitmapOffset, byte[] palette, int paletteOffset, int width, int height)
	{
		return decodeStLow(bitmap, bitmapOffset, palette, paletteOffset, width, height, 1);
	}

	private void decodeStMedium(byte[] bitmap, int bitmapOffset, byte[] palette, int paletteOffset, int width, int height, int frames)
	{
		setSize(width, height << 1, isStePalette(palette, paletteOffset, 4) ? RECOILResolution.STE1X2 : RECOILResolution.ST1X2, frames);
		setStPalette(palette, paletteOffset, 4);
		decodeScaledBitplanes(bitmap, bitmapOffset, width, height * frames, 2, false, null);
	}

	private boolean decodeSrt(byte[] content, int contentLength)
	{
		if (contentLength != 32038 || !isStringAt(content, 32000, "JHSy") || content[32004] != 0 || content[32005] != 1)
			return false;
		decodeStMedium(content, 0, content, 32006, 640, 200, 1);
		return true;
	}

	final boolean decodeSt(byte[] bitmap, int bitmapOffset, byte[] palette, int paletteOffset, int mode, int doubleHeight)
	{
		switch (mode) {
		case 0:
			return decodeStLow(bitmap, bitmapOffset, palette, paletteOffset, 320, 200 << doubleHeight);
		case 1:
			decodeStMedium(bitmap, bitmapOffset, palette, paletteOffset, 640, 200 << doubleHeight, 1);
			return true;
		case 2:
			setSize(640, 400 << doubleHeight, RECOILResolution.ST1X1);
			return decodeBlackAndWhite(bitmap, bitmapOffset, bitmapOffset + (32000 << doubleHeight), false, 16777215);
		default:
			return false;
		}
	}

	private boolean decodeStPi(byte[] content, int contentLength)
	{
		if (contentLength < 32034 || content[0] != 0)
			return false;
		switch (contentLength) {
		case 32034:
		case 32066:
		case 32128:
			return decodeSt(content, 34, content, 2, content[1] & 0xff, 0);
		case 38434:
			return content[1] == 4 && decodeStLow(content, 34, content, 2, 320, 240);
		case 44834:
			return content[1] == 0 && decodeStLow(content, 34, content, 2, 320, 280);
		case 64034:
			return decodeSt(content, 34, content, 2, content[1] & 0xff, 1);
		case 116514:
			return content[1] == 0 && decodeStLow(content, 34, content, 2, 416, 560);
		case 153606:
			if (content[1] != 6)
				return false;
			setSize(1280, 960, RECOILResolution.TT1X1);
			return decodeBlackAndWhite(content, 6, contentLength, false, 16777215);
		case 153634:
			if (content[1] != 4)
				return false;
			setSize(640, 480, RECOILResolution.TT1X1);
			setStPalette(content, 2, 16);
			decodeBitplanes(content, 34, 320, 4, 0, 640, 480);
			return true;
		case 154114:
			if (content[1] != 7)
				return false;
			setSize(640, 480, RECOILResolution.TT2X1);
			setStPalette(content, 2, 256);
			decodeScaledBitplanes(content, 514, 320, 480, 8, false, null);
			return true;
		default:
			return false;
		}
	}

	private boolean decodePc(byte[] content, int contentLength)
	{
		if (contentLength < 68 || content[0] != -128 || (content[1] & 0xff) > 2)
			return false;
		int bitplanes = 4 >> (content[1] & 0xff);
		final PackBitsStream rle = new PackBitsStream();
		rle.content = content;
		rle.contentOffset = 34;
		rle.contentLength = contentLength;
		final byte[] unpacked = new byte[32000];
		return rle.unpackBitplaneLines(unpacked, 320 << (content[1] & 0xff), 200, bitplanes, true, false) && decodeSt(unpacked, 0, content, 2, content[1] & 0xff, 0);
	}

	private boolean decodeEza(byte[] content, int contentLength)
	{
		if (contentLength < 44 || content[0] != 69 || content[1] != 90 || content[2] != 0 || content[3] != -56)
			return false;
		final PackBitsStream rle = new PackBitsStream();
		rle.content = content;
		rle.contentOffset = 44;
		rle.contentLength = contentLength;
		final byte[] unpacked = new byte[32000];
		return rle.unpackBitplaneLines(unpacked, 320, 200, 4, true, false) && decodeStLow(unpacked, 0, content, 4, 320, 200);
	}

	private boolean decodeNeo(String filename, byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 32128:
			if (content[0] != 0 || content[1] != 0 || content[2] != 0)
				return false;
			if (content[3] == 0) {
				final byte[] rst = new byte[6801];
				if (readCompanionFile(filename, "RST", "rst", rst, 6801) == 6800) {
					setSize(320, 200, RECOILResolution.ST1X1);
					setStPalette(content, 4, 16);
					final RastPalette palette = new RastPalette();
					palette.content = rst;
					palette.contentOffset = 0;
					palette.contentLength = 6800;
					palette.colors = 16;
					decodeScaledBitplanes(content, 128, 320, 200, 4, false, palette);
					return true;
				}
			}
			return decodeSt(content, 128, content, 4, content[3] & 0xff, 0);
		case 128128:
			return content[0] == -70 && content[1] == -66 && content[2] == 0 && content[3] == 0 && decodeStLow(content, 128, content, 4, 640, 400);
		default:
			return false;
		}
	}

	private boolean decodeArtDirector(byte[] content, int contentLength)
	{
		return contentLength == 32512 && (content[32287] & 0xff) < 8 && decodeStLow(content, 0, content, 32000 + ((content[32287] & 0xff) << 5), 320, 200);
	}

	private boolean decodeSsb(byte[] content, int contentLength)
	{
		return contentLength == 32768 && decodeStLow(content, 0, content, 32000, 320, 200);
	}

	private boolean decodeGfaArtist(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 32032:
			return decodeStLow(content, 32, content, 0, 320, 200);
		case 34360:
			final GfaArtistPalette palette = new GfaArtistPalette();
			palette.content = content;
			setSize(320, 200, RECOILResolution.ST1X1);
			decodeScaledBitplanes(content, 4, 320, 200, 4, false, palette);
			return true;
		default:
			return false;
		}
	}

	private boolean decodeBil(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 32032:
			return decodeStLow(content, 32, content, 0, 320, 200);
		case 32034:
			return content[0] == 0 && content[1] == 0 && decodeStLow(content, 34, content, 2, 320, 200);
		default:
			return false;
		}
	}

	private boolean decodePaletteMaster(byte[] content, int contentLength)
	{
		if (contentLength != 36864 || content[35968] != -1 || content[35969] != -1)
			return false;
		setSize(320, 200, RECOILResolution.ST1X1);
		final ArtPalette palette = new ArtPalette();
		palette.content = content;
		decodeScaledBitplanes(content, 0, 320, 200, 4, false, palette);
		return true;
	}

	private boolean decodeCel(byte[] content, int contentLength)
	{
		if (contentLength < 128 || content[0] != -1 || content[1] != -1 || content[2] != 0 || content[3] != 0)
			return false;
		int width = (content[58] & 0xff) << 8 | content[59] & 0xff;
		int height = (content[60] & 0xff) << 8 | content[61] & 0xff;
		return contentLength == 128 + ((width + 15) >> 4 << 3) * height && decodeStLow(content, 128, content, 4, width, height);
	}

	private boolean decodeMur(String filename, byte[] content, int contentLength)
	{
		if (contentLength != 32000)
			return false;
		final byte[] pal = new byte[97];
		if (readCompanionFile(filename, "PAL", "pal", pal, 97) != 96)
			return false;
		setSize(320, 200, RECOILResolution.STE1X1);
		setStVdiPalette(pal, 0, 16, 4);
		decodeBitplanes(content, 0, 160, 4, 0, 320, 200);
		return true;
	}

	private boolean decodeKid(byte[] content, int contentLength)
	{
		return contentLength == 63054 && content[0] == 75 && content[1] == 68 && decodeStLowWithStride(content, 34, 230, content, 2, 448, 274, 1);
	}

	private boolean decodeStPpp(byte[] content, int contentLength)
	{
		return contentLength == 32079 && isStringAt(content, 0, "PABLO PACKED PICTURE: Groupe CDND \r\n32036\r\n") && content[44] == 0 && content[45] == 125 && content[46] == 36 && decodeSt(content, 79, content, 47, content[43] & 0xff, 0);
	}

	private boolean decodeStRgb(byte[] content, int contentLength)
	{
		if (contentLength != 96102)
			return false;
		setSize(320, 200, RECOILResolution.STE1X1);
		this.frames = 3;
		for (int i = 0; i < 64000; i++) {
			int rgb = getStLowPixel(content, 34, i) << 16 | getStLowPixel(content, 32068, i) << 8 | getStLowPixel(content, 64102, i);
			this.pixels[i] = rgb * 17;
		}
		return true;
	}

	private boolean decodeSd(byte[] content, int contentLength, int mode)
	{
		return contentLength == 32128 && decodeSt(content, 128, content, 4, mode, 0);
	}

	private boolean decodeObj(byte[] content, int contentLength)
	{
		if (contentLength < 8)
			return false;
		if (content[4] == 0 && content[5] == 1) {
			return setSize(((content[0] & 0xff) << 8) + (content[1] & 0xff) + 1, ((content[2] & 0xff) << 8) + (content[3] & 0xff) + 1, RECOILResolution.ST1X1) && decodeBlackAndWhite(content, 6, contentLength, true, 16777215);
		}
		final Stream s = new Stream();
		s.content = content;
		s.contentOffset = 0;
		s.contentLength = contentLength;
		for (int i = 0; i < 16; i++) {
			int rgb = s.parseDaliInt();
			if (rgb < 0 || rgb > 1911)
				return false;
			rgb = (rgb & 1792) << 8 | (rgb & 112) << 4 | (rgb & 7);
			this.contentPalette[i] = rgb << 5 | rgb << 2 | (rgb >> 1 & 197379);
		}
		int contentOffset = s.contentOffset;
		if (contentOffset + 6 >= contentLength || content[contentOffset + 2] != 0 || content[contentOffset + 4] != 0 || content[contentOffset + 5] != 4)
			return false;
		int width = ((content[contentOffset] & 0xff) << 8) + (content[contentOffset + 1] & 0xff) + 1;
		int height = (content[contentOffset + 3] & 0xff) + 1;
		int stride = (width + 15) >> 4 << 3;
		if (contentOffset + 6 + height * stride != contentLength || !setSize(width, height, RECOILResolution.ST1X1))
			return false;
		decodeBitplanes(content, contentOffset + 6, stride, 4, 0, width, height);
		return true;
	}

	private boolean decodeIc(byte[] content, int contentLength)
	{
		if (contentLength < 68 || !isStringAt(content, 0, "IMDC") || content[4] != 0 || content[64] != -56 || content[65] != 2)
			return false;
		final byte[] unpacked = new byte[32000];
		final IcStream rle = new IcStream();
		rle.content = content;
		rle.contentOffset = 67;
		rle.contentLength = contentLength;
		return rle.unpackColumns(unpacked, 0, 160, 32000) && decodeSt(unpacked, 0, content, 6, content[5] & 0xff, 0);
	}

	private boolean decodeGraphicsProcessor(byte[] content, int contentLength)
	{
		if (contentLength < 493 || content[0] != 0)
			return false;
		int mode = content[1] & 0xff;
		switch (mode) {
		case 0:
		case 1:
		case 2:
			return contentLength == 32331 && decodeSt(content, 331, content, 2, mode, 0);
		case 10:
		case 11:
		case 12:
			break;
		default:
			return false;
		}
		mode -= 10;
		int bitplanes = 4 >> mode;
		final byte[] unpacked = new byte[32000];
		int contentOffset = 333;
		int count = 0;
		for (int unpackedOffset = 0; unpackedOffset < 32000; unpackedOffset += bitplanes) {
			if (count == 0) {
				if (contentOffset + bitplanes >= contentLength)
					return false;
				count = content[contentOffset] & 0xff;
				if (count == 0)
					return false;
				contentOffset += 1 + bitplanes;
			}
			System.arraycopy(content, contentOffset - bitplanes, unpacked, unpackedOffset, bitplanes);
			count--;
		}
		return decodeSt(unpacked, 0, content, 2, mode, 0);
	}

	private boolean decodeDaliCompressed(byte[] content, int contentLength, int mode)
	{
		final DaliStream stream = new DaliStream();
		stream.content = content;
		stream.contentOffset = 32;
		stream.contentLength = contentLength;
		int countLength = stream.parseDaliInt();
		return countLength > 0 && stream.parseDaliInt() > 0 && stream.decode(countLength, this, 0, mode);
	}

	private boolean decodeRgh(byte[] content, int contentLength)
	{
		if (contentLength < 14 || !isStringAt(content, 0, "(c)F.MARCHAL"))
			return false;
		final DaliStream stream = new DaliStream();
		stream.content = content;
		stream.contentOffset = 12;
		stream.contentLength = contentLength;
		int countLength = stream.parseDaliInt();
		int paletteOffset = stream.contentOffset;
		stream.contentOffset = paletteOffset + 32;
		return stream.decode(countLength, this, paletteOffset, 0);
	}

	private boolean decodeSc(byte[] content, int contentLength)
	{
		if (contentLength < 128 || !isStringAt(content, 54, "ANvisionA"))
			return false;
		int flags = content[63] & 0xff;
		int doubleHeight;
		switch (flags & 15) {
		case 0:
			doubleHeight = 1;
			break;
		case 1:
		case 2:
			doubleHeight = 0;
			break;
		default:
			return false;
		}
		int bitmapLength = 32000 << doubleHeight;
		int mode = flags >> 4 & 3;
		if (flags >= 128) {
			final byte[] unpacked = new byte[64000];
			final ScStream rle = new ScStream();
			rle.content = content;
			rle.contentOffset = 128;
			rle.contentLength = contentLength;
			int bytesPer16Pixels = 8 >> mode;
			for (int bitplane = 0; bitplane < bytesPer16Pixels; bitplane += 2) {
				if (!rle.unpackWords(unpacked, bitplane, bytesPer16Pixels, bitmapLength))
					return false;
			}
			return decodeSt(unpacked, 0, content, 4, mode, doubleHeight);
		}
		return contentLength >= 128 + bitmapLength && decodeSt(content, 128, content, 4, mode, doubleHeight);
	}

	private boolean decodeGfb(byte[] content, int contentLength)
	{
		if (contentLength < 20 || !isStringAt(content, 0, "GF25"))
			return false;
		int bitplanes;
		switch (get32BigEndian(content, 4)) {
		case 2:
			bitplanes = 1;
			break;
		case 4:
			bitplanes = 2;
			break;
		case 16:
			bitplanes = 4;
			break;
		case 256:
			bitplanes = 8;
			break;
		default:
			return false;
		}
		int width = get32BigEndian(content, 8);
		if (width <= 0)
			return false;
		int height = get32BigEndian(content, 12);
		if (height <= 0)
			return false;
		int bitmapLength = get32BigEndian(content, 16);
		if (bitmapLength <= 0)
			return false;
		if (1556 + bitmapLength != contentLength || bitmapLength != ((width + 15) >> 4 << 1) * bitplanes * height || !setSizeStOrFalcon(width, height, bitplanes, false))
			return false;
		setStVdiPalette(content, 20 + bitmapLength, 1 << bitplanes, bitplanes);
		decodeScaledBitplanes(content, 20, width, height, bitplanes, false, null);
		return true;
	}

	private boolean decodeCa(byte[] content, int contentLength)
	{
		if (contentLength < 8 || content[0] != 67 || content[1] != 65)
			return false;
		int contentOffset;
		switch (content[3]) {
		case 0:
			contentOffset = 36;
			break;
		case 1:
			contentOffset = 12;
			break;
		case 2:
			contentOffset = 4;
			break;
		default:
			return false;
		}
		switch (content[2]) {
		case 0:
			return contentOffset + 32000 == contentLength && decodeSt(content, contentOffset, content, 4, content[3] & 0xff, 0);
		case 1:
			{
				final byte[] unpacked = new byte[32000];
				final CaStream rle = new CaStream();
				rle.content = content;
				rle.contentOffset = contentOffset;
				rle.contentLength = contentLength;
				return rle.unpackCa(unpacked, 0) && decodeSt(unpacked, 0, content, 4, content[3] & 0xff, 0);
			}
		default:
			return false;
		}
	}

	private boolean decodeTny(byte[] content, int contentLength)
	{
		if (contentLength < 42)
			return false;
		int mode = content[0] & 0xff;
		int contentOffset;
		if (mode > 2) {
			if (mode > 5)
				return false;
			mode -= 3;
			contentOffset = 4;
		}
		else
			contentOffset = 0;
		int controlLength = (content[contentOffset + 33] & 0xff) << 8 | content[contentOffset + 34] & 0xff;
		int valueLength = ((content[contentOffset + 35] & 0xff) << 8 | content[contentOffset + 36] & 0xff) << 1;
		if (contentOffset + 37 + controlLength + valueLength > contentLength)
			return false;
		final TnyStream rle = new TnyStream();
		rle.content = content;
		rle.contentOffset = contentOffset + 37;
		rle.valueOffset = rle.contentLength = contentOffset + 37 + controlLength;
		rle.valueLength = contentOffset + 37 + controlLength + valueLength;
		final byte[] unpacked = new byte[32000];
		for (int bitplane = 0; bitplane < 8; bitplane += 2) {
			for (int x = bitplane; x < 160; x += 8) {
				for (int unpackedOffset = x; unpackedOffset < 32000; unpackedOffset += 160) {
					int b = rle.readRle();
					if (b < 0)
						return false;
					unpacked[unpackedOffset] = (byte) (b >> 8);
					unpacked[unpackedOffset + 1] = (byte) b;
				}
			}
		}
		return decodeSt(unpacked, 0, content, contentOffset + 1, mode, 0);
	}

	private boolean decodeCptFul(byte[] content, int contentOffset, int contentLength, HblPalette palette)
	{
		if (contentLength < contentOffset + 40 || content[contentOffset + 32] != 0)
			return false;
		int mode = content[contentOffset + 33] & 0xff;
		if (mode > 2)
			return false;
		int bitplanes = 4 >> mode;
		final byte[] unpacked = new byte[32000];
		final byte[] isFilled = new byte[16000];
		contentOffset += 34;
		for (;;) {
			int nextContentOffset = contentOffset + 4 + bitplanes * 2;
			if (nextContentOffset > contentLength)
				return false;
			int repeatCount = (content[contentOffset] & 0xff) << 8 | content[contentOffset + 1] & 0xff;
			if (repeatCount == 65535) {
				contentOffset = nextContentOffset;
				break;
			}
			int offset = ((content[contentOffset + 2] & 0xff) << 8 | content[contentOffset + 3] & 0xff) * bitplanes;
			do {
				if (offset >= 16000)
					return false;
				System.arraycopy(content, contentOffset + 4, unpacked, offset << 1, bitplanes << 1);
				isFilled[offset] = 1;
				offset += bitplanes;
			}
			while (--repeatCount >= 0);
			contentOffset = nextContentOffset;
		}
		for (int offset = 0; offset < 16000; offset += bitplanes) {
			if (isFilled[offset] == 0) {
				int nextContentOffset = contentOffset + bitplanes * 2;
				if (nextContentOffset > contentLength)
					return false;
				System.arraycopy(content, contentOffset, unpacked, offset << 1, bitplanes << 1);
				contentOffset = nextContentOffset;
			}
		}
		if (palette == null)
			return decodeSt(unpacked, 0, content, 0, mode, 0);
		if (mode == 0)
			setSize(320, 200, RECOILResolution.ST1X1);
		else
			setSize(640, 400, RECOILResolution.ST1X2);
		decodeScaledBitplanes(unpacked, 0, 320 << mode, 200, bitplanes, false, palette);
		return true;
	}

	private boolean decodeCpt(String filename, byte[] content, int contentLength)
	{
		if (contentLength < 40)
			return false;
		if ((content[33] & 0xff) <= 1) {
			final byte[] hbl = new byte[3249];
			int hblLength = readCompanionFile(filename, "HBL", "hbl", hbl, 3249);
			if (hblLength >= 896 && hblLength <= 3248) {
				final HblPalette palette = new HblPalette();
				palette.content = hbl;
				palette.contentLength = hblLength;
				if (palette.init())
					return decodeCptFul(content, 0, contentLength, palette);
			}
		}
		return decodeCptFul(content, 0, contentLength, null);
	}

	private boolean decodeFul(byte[] content, int contentLength)
	{
		if (contentLength < 1544)
			return false;
		final HblPalette palette = new HblPalette();
		palette.content = content;
		palette.contentLength = contentLength;
		return palette.init() && palette.contentOffset + 641 < contentLength && (content[palette.contentOffset + 641] & 0xff) <= 1 && decodeCptFul(content, palette.contentOffset + 608, contentLength, palette);
	}

	private void setDefaultStPalette(int bitplanes)
	{
		this.contentPalette[0] = 16777215;
		if (bitplanes >= 2) {
			this.contentPalette[1] = 16711680;
			this.contentPalette[2] = 65280;
			if (bitplanes == 4) {
				this.contentPalette[3] = 16776960;
				this.contentPalette[4] = 255;
				this.contentPalette[5] = 16711935;
				this.contentPalette[6] = 65535;
				this.contentPalette[7] = 11184810;
				this.contentPalette[8] = 5592405;
				this.contentPalette[9] = 11141120;
				this.contentPalette[10] = 43520;
				this.contentPalette[11] = 11184640;
				this.contentPalette[12] = 170;
				this.contentPalette[13] = 11141290;
				this.contentPalette[14] = 43690;
			}
		}
		this.contentPalette[(1 << bitplanes) - 1] = 0;
	}

	private static boolean isTimg(byte[] content)
	{
		if (!isStringAt(content, 16, "TIMG") || content[20] != 0 || content[21] != 3)
			return false;
		for (int i = 22; i < 28; i += 2) {
			if (content[i] != 0 || content[i + 1] != 5)
				return false;
		}
		return true;
	}

	private static boolean isXimg(byte[] content)
	{
		return isStringAt(content, 16, "XIMG") && content[20] == 0 && content[21] == 0;
	}

	private static boolean isSttt(byte[] content, int bitplanes)
	{
		int colors = (content[20] & 0xff) << 8 | content[21] & 0xff;
		return isStringAt(content, 16, "STTT") && colors == 1 << bitplanes;
	}

	private boolean decodeStImg(byte[] content, int contentLength)
	{
		if (contentLength < 17 || content[0] != 0 || content[1] == 0 || (content[1] & 0xff) > 3 || content[4] != 0)
			return false;
		int headerLength = ((content[2] & 0xff) << 8 | content[3] & 0xff) << 1;
		if (headerLength < 16 || headerLength >= contentLength)
			return false;
		int bitplanes = content[5] & 0xff;
		int width = (content[12] & 0xff) << 8 | content[13] & 0xff;
		int height = (content[14] & 0xff) << 8 | content[15] & 0xff;
		if (headerLength == 18 && content[16] == 0 && content[17] == 3) {
			if (!setSize(width, height, RECOILResolution.FALCON1X1))
				return false;
			int contentOffset = 18;
			int pixelsLength = width * height;
			int count = 0;
			for (int i = 0; i < pixelsLength; i++) {
				if (count == 0) {
					if (contentOffset + 1 >= contentLength)
						return false;
					if (content[contentOffset++] != -128)
						return false;
					count = content[contentOffset++] & 0xff;
					if (count == 0)
						return false;
				}
				if (contentOffset + 2 >= contentLength)
					return false;
				this.pixels[i] = (content[contentOffset + 2] & 0xff) << 16 | (content[contentOffset + 1] & 0xff) << 8 | content[contentOffset] & 0xff;
				contentOffset += 3;
				count--;
			}
			return true;
		}
		int xRatio = (content[8] & 0xff) << 8 | content[9] & 0xff;
		int yRatio = (content[10] & 0xff) << 8 | content[11] & 0xff;
		if (bitplanes <= 2 && width <= 640 && height <= 200 && yRatio * 2 > xRatio * 3)
			setSize(width, height << 1, RECOILResolution.ST1X2);
		else if (bitplanes <= 8 && width <= 320 && height <= 480 && xRatio * 2 > yRatio * 3)
			setSize(width << 1, height, RECOILResolution.TT2X1);
		else if (!setSizeStOrFalcon(width, height, bitplanes, true))
			return false;
		switch (bitplanes) {
		case 1:
		case 2:
		case 4:
		case 8:
			if (headerLength == 22 + (6 << bitplanes) && isXimg(content)) {
				for (int i = 0; i < 1 << bitplanes; i++)
					this.contentPalette[i] = getStVdiColor(content, 22 + i * 6);
			}
			else if (headerLength == 22 + (2 << bitplanes) && isSttt(content, bitplanes))
				setStPalette(content, 22, 1 << bitplanes);
			else if (bitplanes == 8) {
				int rgb = 16777215;
				for (int c = 0; c < 256; c++) {
					this.contentPalette[c] = rgb;
					for (int mask = 8421504;; mask >>= 1) {
						rgb ^= mask;
						if ((rgb & mask) == 0)
							break;
					}
				}
			}
			else if (headerLength == 50 && content[16] == 0 && content[17] == -128)
				setStPalette(content, 18, 16);
			else
				setDefaultStPalette(bitplanes);
			break;
		case 15:
			if (headerLength != 28 || !isTimg(content))
				return false;
			break;
		case 16:
		case 24:
		case 32:
			break;
		default:
			return false;
		}
		final ImgStream rle = new ImgStream();
		rle.content = content;
		rle.contentOffset = headerLength;
		rle.contentLength = contentLength;
		int bytesPerBitplane = (width + 7) >> 3;
		if (bitplanes == 24)
			bytesPerBitplane = (bytesPerBitplane + 1) & -2;
		int bytesPerLine = bitplanes * bytesPerBitplane;
		byte[] unpacked = new byte[bytesPerLine];
		for (int y = 0; y < height;) {
			int lineRepeatCount = rle.getLineRepeatCount();
			if (lineRepeatCount > height - y)
				lineRepeatCount = height - y;
			for (int x = 0; x < bytesPerLine; x++) {
				int b = rle.readRle();
				if (b < 0)
					return false;
				if (b != 256)
					unpacked[x] = (byte) b;
				else if (y == 0)
					unpacked[x] = 0;
			}
			for (int x = 0; x < width; x++) {
				int c;
				switch (bitplanes) {
				case 16:
					c = getFalconTrueColor(unpacked, x << 1);
					break;
				case 24:
					c = getR8G8B8Color(unpacked, x * 3);
					break;
				case 32:
					c = getR8G8B8Color(unpacked, (x << 2) + 1);
					break;
				default:
					c = getBitplanePixel(unpacked, x >> 3, x, bitplanes, bytesPerBitplane);
					c = bitplanes == 15 ? getB5G5R5Color(c) : this.contentPalette[c];
					break;
				}
				for (int i = 0; i < lineRepeatCount; i++)
					setScaledPixel(x, y + i, c);
			}
			y += lineRepeatCount;
		}
		return true;
	}

	private boolean decodeStLowBlend(byte[] bitmap, int bitmapOffset, byte[] palette, int paletteOffset, int width, int height)
	{
		decodeStLow(bitmap, bitmapOffset, palette, paletteOffset, width, height, 2);
		decodeBitplanes(bitmap, bitmapOffset + (width >> 1) * height, width >> 1, 4, width * height, width, height);
		return applyBlend();
	}

	private boolean decodeDuo(byte[] content, int contentLength)
	{
		return contentLength == 113600 && decodeStLowBlend(content, 32, content, 0, 416, 273);
	}

	private boolean decodeDu2(byte[] content, int contentLength)
	{
		if (contentLength != 113576 && contentLength != 113600)
			return false;
		decodeStMedium(content, 8, content, 0, 832, 273, 2);
		return applyBlend();
	}

	private boolean decodeP3c(byte[] content, int contentLength)
	{
		final CaStream rle = new CaStream();
		rle.content = content;
		rle.contentOffset = 0;
		rle.contentLength = contentLength;
		int compressedLength = rle.parseDaliInt();
		if (compressedLength < 0)
			return false;
		int paletteOffset = rle.contentOffset;
		rle.contentLength = paletteOffset + 32 + compressedLength;
		if (rle.contentLength >= contentLength)
			return false;
		rle.contentOffset = paletteOffset + 32;
		final byte[] unpacked = new byte[64000];
		if (!rle.unpackCa(unpacked, 0))
			return false;
		rle.contentLength = contentLength;
		compressedLength = rle.parseDaliInt();
		if (compressedLength < 0 || rle.contentOffset + compressedLength != contentLength)
			return false;
		rle.contentLength = contentLength;
		return rle.unpackCa(unpacked, 32000) && decodeStLowBlend(unpacked, 0, content, paletteOffset, 320, 200);
	}

	private boolean unpackLz4(byte[] content, int contentLength, byte[] unpacked, int unpackedLength)
	{
		if (contentLength < 11 || content[0] != 4 || content[1] != 34 || content[2] != 77 || content[3] != 24 || (content[4] & 195) != 64)
			return false;
		final Lz4Stream stream = new Lz4Stream();
		stream.content = content;
		stream.contentOffset = 7;
		if ((content[4] & 8) != 0)
			stream.contentOffset += 8;
		stream.unpacked = unpacked;
		stream.unpackedOffset = 0;
		stream.unpackedLength = unpackedLength;
		for (;;) {
			if (stream.contentOffset + 4 > contentLength)
				return false;
			int blockSize = get32LittleEndian(content, stream.contentOffset);
			stream.contentOffset += 4;
			stream.contentLength = contentLength;
			if (blockSize == 0)
				break;
			if (blockSize >> 31 != 0) {
				if (!stream.copy(blockSize & 2147483647))
					return false;
				continue;
			}
			stream.contentLength = stream.contentOffset + blockSize;
			if (stream.contentLength > contentLength)
				return false;
			for (;;) {
				int token = stream.readByte();
				if (token < 0)
					return false;
				int count = stream.readCount(token >> 4);
				if (count < 0 || !stream.copy(count))
					return false;
				if (stream.contentOffset == stream.contentLength)
					break;
				if (stream.contentOffset > stream.contentLength - 2)
					return false;
				int distance = stream.readByte();
				distance += stream.readByte() << 8;
				if (distance == 0)
					return false;
				count = stream.readCount(token & 15);
				if (count < 0)
					return false;
				count += 4;
				int nextOffset = stream.unpackedOffset + count;
				if (nextOffset > unpackedLength || !copyPrevious(unpacked, stream.unpackedOffset, distance, count))
					return false;
				stream.unpackedOffset = nextOffset;
			}
			if ((content[4] & 16) != 0)
				stream.contentOffset += 4;
		}
		if ((content[4] & 4) != 0)
			stream.contentOffset += 4;
		return stream.contentOffset == contentLength && stream.unpackedOffset == unpackedLength;
	}

	private boolean decodePl4(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[64070];
		if (!unpackLz4(content, contentLength, unpacked, 64070) || unpacked[0] != 0 || unpacked[1] != 0 || unpacked[32036] != 0 || unpacked[32037] != 0)
			return false;
		setSize(320, 200, isStePalette(unpacked, 2, 16) || isStePalette(unpacked, 32038, 16) ? RECOILResolution.STE1X1 : RECOILResolution.ST1X1, 2);
		setStPalette(unpacked, 2, 16);
		decodeBitplanes(unpacked, 34, 160, 4, 0, 320, 200);
		setStPalette(unpacked, 32038, 16);
		decodeBitplanes(unpacked, 32070, 160, 4, 64000, 320, 200);
		return applyBlend();
	}

	private boolean decodeSpuScreen(byte[] content, int bitmapOffset, int height, boolean enhanced)
	{
		int paletteOffset = bitmapOffset + height * 160;
		if (!setSize(320, height, enhanced || isStePalette(content, paletteOffset, height * 48) ? RECOILResolution.STE1X1 : RECOILResolution.ST1X1))
			return false;
		if (enhanced)
			this.frames = 2;
		int pixelsOffset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < 320; x++) {
				int c = getStLowPixel(content, bitmapOffset, pixelsOffset);
				int x1 = c * 10 + 1 - (c & 1) * 6;
				if (x >= x1 + 160)
					c += 32;
				else if (x >= x1)
					c += 16;
				int colorOffset = paletteOffset + y * 96 + (c << 1);
				this.pixels[pixelsOffset++] = enhanced ? getSteInterlacedColor((content[colorOffset] & 0xff) << 8 | content[colorOffset + 1] & 0xff) : getStColor(content, colorOffset);
			}
		}
		return true;
	}

	private boolean decodeSpu(byte[] content, int contentLength)
	{
		return contentLength == 51104 && decodeSpuScreen(content, 160, 199, isStringAt(content, 0, "5BIT"));
	}

	private static boolean unpackSpc(RleStream rle, byte[] unpacked)
	{
		for (int bitplane = 0; bitplane < 8; bitplane += 2) {
			if (!rle.unpackWords(unpacked, 160 + bitplane, 8, 32000))
				return false;
		}
		return true;
	}

	private boolean decodeStSpc(byte[] content, int contentLength)
	{
		if (contentLength < 12 || content[0] != 83 || content[1] != 80)
			return false;
		final byte[] unpacked = new byte[51104];
		final SpcStream rle = new SpcStream();
		rle.content = content;
		rle.contentOffset = 12;
		rle.contentLength = contentLength;
		if (!unpackSpc(rle, unpacked))
			return false;
		int contentOffset = 12 + get32BigEndian(content, 4);
		if (contentOffset < 12)
			return false;
		for (int unpackedOffset = 32000; unpackedOffset < 51104;) {
			if (contentOffset >= contentLength - 1)
				return false;
			int got = (content[contentOffset] & 127) << 8 | content[contentOffset + 1] & 0xff;
			contentOffset += 2;
			for (int i = 0; i < 16; i++) {
				if ((got >> i & 1) == 0) {
					unpacked[unpackedOffset] = 0;
					unpacked[unpackedOffset + 1] = 0;
				}
				else {
					if (contentOffset >= contentLength - 1)
						return false;
					unpacked[unpackedOffset] = (byte) (content[contentOffset] & 0xff);
					unpacked[unpackedOffset + 1] = (byte) (content[contentOffset + 1] & 0xff);
					contentOffset += 2;
				}
				unpackedOffset += 2;
			}
		}
		return decodeSpuScreen(unpacked, 160, 199, false);
	}

	private boolean decodeSps(byte[] content, int contentLength)
	{
		if (contentLength < 13 || content[0] != 83 || content[1] != 80 || content[2] != 0 || content[3] != 0)
			return false;
		final byte[] unpacked = new byte[51104];
		final SpsStream rle = new SpsStream();
		rle.content = content;
		rle.contentOffset = 12;
		rle.contentLength = contentLength;
		if ((content[contentLength - 1] & 1) == 0) {
			for (int bitplane = 0; bitplane < 8; bitplane += 2) {
				for (int x = 0; x < 40; x++) {
					if (!rle.unpack(unpacked, 160 + ((x & -2) << 2) + bitplane + (x & 1), 160, 32000))
						return false;
				}
			}
		}
		else if (!unpackSpc(rle, unpacked))
			return false;
		final BitStream bitStream = new BitStream();
		bitStream.content = content;
		bitStream.contentOffset = 12 + get32BigEndian(content, 4);
		if (bitStream.contentOffset < 12)
			return false;
		bitStream.contentLength = contentLength;
		for (int unpackedOffset = 32000; unpackedOffset < 51104;) {
			int got = bitStream.readBits(14);
			if (got < 0)
				return false;
			got <<= 1;
			for (int i = 15; i >= 0; i--) {
				int rgb;
				if ((got >> i & 1) == 0)
					rgb = 0;
				else {
					rgb = bitStream.readBits(9);
					if (rgb < 0)
						return false;
				}
				unpacked[unpackedOffset] = (byte) (rgb >> 6);
				unpacked[unpackedOffset + 1] = (byte) ((rgb & 63) + (rgb & 56));
				unpackedOffset += 2;
			}
		}
		return decodeSpuScreen(unpacked, 160, 199, false);
	}

	private boolean unpackAndDecodeSpx(SpxStream stream, int contentLength, int height, byte[] unpacked)
	{
		byte[] content = stream.content;
		int bitmapLength = height * 160;
		int paletteOffset = stream.contentOffset;
		int paletteLength = get32BigEndian(content, stream.contentStart - 4);
		if (content[4] == 0)
			System.arraycopy(content, stream.contentStart + 160, unpacked, 0, bitmapLength);
		else if (content[3] == 1) {
			if (!stream.unpack(unpacked, 0, bitmapLength))
				return false;
		}
		else if (content[5] != 1)
			return false;
		else {
			int packedLength = get32BigEndian(content, stream.contentStart + 4);
			int packedEnd = stream.contentStart + 8 + packedLength;
			if (get32BigEndian(content, stream.contentStart - 8) != 8 + packedLength || packedEnd > contentLength || packedEnd < 0)
				return false;
			stream.contentStart += 8;
			stream.contentOffset = packedEnd;
			if (!stream.unpackV2(unpacked, height << 8))
				return false;
		}
		if (content[5] == 0) {
			if (paletteLength != height * 96)
				return false;
			System.arraycopy(content, paletteOffset, unpacked, bitmapLength, paletteLength);
		}
		else if (content[3] == 1) {
			stream.contentStart = paletteOffset;
			stream.contentOffset = paletteOffset + paletteLength;
			if (stream.contentOffset > contentLength || stream.getUnpackedLength() != height * 96 || !stream.unpack(unpacked, bitmapLength, height << 8))
				return false;
		}
		return decodeSpuScreen(unpacked, 0, height, false);
	}

	private boolean decodeSpx(byte[] content, int contentLength)
	{
		if (contentLength < 12 || content[0] != 83 || content[1] != 80 || content[2] != 88)
			return false;
		int contentOffset = 10;
		for (int zerosToSkip = 2;;) {
			if (contentOffset + 16 >= contentLength)
				return false;
			if (content[contentOffset++] == 0) {
				if (--zerosToSkip == 0)
					break;
			}
		}
		int bitmapLength = get32BigEndian(content, contentOffset);
		final SpxStream stream = new SpxStream();
		stream.content = content;
		stream.contentStart = contentOffset + 8;
		stream.contentOffset = contentOffset + 8 + bitmapLength;
		if (stream.contentOffset > contentLength)
			return false;
		switch (content[4]) {
		case 0:
			bitmapLength -= 160;
			break;
		case 1:
			switch (content[3]) {
			case 1:
				bitmapLength = stream.getUnpackedLength() - 160;
				break;
			case 2:
				bitmapLength = get32BigEndian(content, contentOffset + 4) - 320;
				break;
			default:
				return false;
			}
			break;
		default:
			return false;
		}
		if (bitmapLength <= 0 || bitmapLength % 160 != 0)
			return false;
		int height = bitmapLength / 160;
		byte[] unpacked = new byte[height << 8];
		return unpackAndDecodeSpx(stream, contentLength, height, unpacked);
	}

	private static int getStLowSeparateBitplanes(byte[] content, int contentOffset, int bitplaneLength, int x)
	{
		return getBitplanePixel(content, contentOffset + (x >> 3), x, 4, bitplaneLength);
	}

	private boolean decodePci(byte[] content, int contentLength)
	{
		if (contentLength != 115648)
			return false;
		setSize(352, 278, isStePalette(content, 97856, 8896) ? RECOILResolution.STE1X1 : RECOILResolution.ST1X1, 2);
		int bitmapOffset = 0;
		for (int y = 0; y < 556; y++) {
			if (y == 278)
				bitmapOffset = 48928;
			setStPalette(content, 97856 + (y << 5), 16);
			for (int x = 0; x < 352; x++)
				this.pixels[y * 352 + x] = this.contentPalette[getStLowSeparateBitplanes(content, bitmapOffset, 12232, x)];
			bitmapOffset += 44;
		}
		return applyBlend();
	}

	private void decodePcsScreen(byte[] unpacked, int pixelsOffset)
	{
		for (int y = 0; y < 199; y++) {
			for (int x = 0; x < 320; x++) {
				int c = getStLowSeparateBitplanes(unpacked, 40 + y * 40, 8000, x) << 1;
				if (x >= c * 2) {
					if (c < 28) {
						if (x >= c * 2 + 76) {
							if (x >= 176 + c * 5 - (c & 2) * 3)
								c += 32;
							c += 32;
						}
					}
					else if (x >= c * 2 + 92)
						c += 32;
					c += 32;
				}
				this.pixels[pixelsOffset++] = getStColor(unpacked, 32000 + y * 96 + c);
			}
		}
	}

	private boolean decodePcs(byte[] content, int contentLength)
	{
		if (contentLength < 18 || content[0] != 1 || content[1] != 64 || content[2] != 0 || content[3] != -56)
			return false;
		final PcsStream rle = new PcsStream();
		rle.content = content;
		rle.contentOffset = 6;
		rle.contentLength = contentLength;
		final byte[] unpacked1 = new byte[51136];
		if (!rle.unpackPcs(unpacked1))
			return false;
		if (content[4] == 0) {
			setSize(320, 199, isStePalette(unpacked1, 32000, 9616) ? RECOILResolution.STE1X1 : RECOILResolution.ST1X1);
			decodePcsScreen(unpacked1, 0);
			return true;
		}
		final byte[] unpacked2 = new byte[51136];
		if (!rle.unpackPcs(unpacked2))
			return false;
		if ((content[4] & 1) == 0) {
			for (int i = 0; i < 32000; i++)
				unpacked2[i] ^= unpacked1[i] & 0xff;
		}
		if ((content[4] & 2) == 0) {
			for (int i = 32000; i < 51136; i++)
				unpacked2[i] ^= unpacked1[i] & 0xff;
		}
		setSize(320, 199, isStePalette(unpacked1, 32000, 9616) || isStePalette(unpacked2, 32000, 9616) ? RECOILResolution.STE1X1 : RECOILResolution.ST1X1, 2);
		decodePcsScreen(unpacked1, 0);
		decodePcsScreen(unpacked2, 63680);
		return applyBlend();
	}

	private byte[] unpackPbx(byte[] content, int contentLength, byte[] unpacked, int bitmapOffset, int bytesPer16Pixels, int unpackedLength)
	{
		if (content[4] != -128 || content[5] != 1)
			return contentLength == unpackedLength ? content : null;
		final PackBitsStream rle = new PackBitsStream();
		rle.content = content;
		rle.contentOffset = 128;
		rle.contentLength = contentLength;
		if (!rle.unpack(unpacked, 128, 1, bitmapOffset))
			return null;
		for (int bitplane = 0; bitplane < bytesPer16Pixels; bitplane += 2) {
			for (int x = bitplane; x < 160; x += bytesPer16Pixels) {
				if (!rle.unpackWords(unpacked, bitmapOffset + x, 160, unpackedLength))
					return null;
			}
		}
		return unpacked;
	}

	private boolean decodePbx01(byte[] content, int contentLength, int bitplanes, int lineHeight)
	{
		final byte[] unpacked = new byte[32512];
		content = unpackPbx(content, contentLength, unpacked, 512, bitplanes << 1, 32512);
		if (content == null || content[161] != 0)
			return false;
		int paletteOffset = 128;
		for (int y = 0; y < 200; y++) {
			if (paletteOffset < 512 && y == (content[paletteOffset + 33] & 0xff)) {
				setStPalette(content, paletteOffset, 16);
				do
					paletteOffset += 48;
				while (paletteOffset < 512 && content[paletteOffset + 34] == 0 && content[paletteOffset + 35] == 0);
			}
			decodeBitplanes(content, 512 + y * 160, 0, bitplanes, y * lineHeight * this.width, this.width, lineHeight);
		}
		return true;
	}

	private void decodePbx8(byte[] content, int paletteOffset, int bitmapOffset, int pixelsOffset)
	{
		for (int y = 0; y < 200; y++) {
			for (int x = 0; x < 320; x++) {
				int c = getStLowPixel(content, bitmapOffset + y * 160, x);
				if (x >= (c > 7 ? 88 : 76) + c * 10 - (c & 1) * 6)
					c += 16;
				this.pixels[pixelsOffset + x] = getStColor(content, paletteOffset + (y << 6) + (c << 1));
			}
			pixelsOffset += 320;
		}
	}

	private boolean decodePbx(byte[] content, int contentLength)
	{
		if (contentLength < 128 || content[0] != 0 || content[1] != 0 || content[2] != 0)
			return false;
		switch (content[3]) {
		case 0:
			setSize(320, 200, RECOILResolution.ST1X1);
			return decodePbx01(content, contentLength, 4, 1);
		case 1:
			setSize(640, 400, RECOILResolution.ST1X2);
			return decodePbx01(content, contentLength, 2, 2);
		case -128:
			{
				final byte[] unpacked = new byte[44928];
				content = unpackPbx(content, contentLength, unpacked, 12928, 8, 44928);
				if (content == null)
					return false;
				setSize(320, 200, RECOILResolution.ST1X1);
				decodePbx8(content, 128, 12928, 0);
				return true;
			}
		case -127:
			{
				final byte[] unpacked = new byte[57728];
				content = unpackPbx(content, contentLength, unpacked, 25728, 8, 57728);
				if (content == null)
					return false;
				setSize(320, 200, RECOILResolution.ST1X1, 2);
				decodePbx8(content, 128, 25728, 0);
				decodePbx8(content, 12928, 25728, 64000);
				return applyBlend();
			}
		default:
			return false;
		}
	}

	private void decodeMppScreen(byte[] content, int paletteOffset, int paletteLength, int pixelsOffset)
	{
		int mode = content[3] & 0xff;
		int bitmapOffset = paletteOffset + paletteLength;
		final int[] palette = new int[16];
		final MppPaletteStream paletteStream = new MppPaletteStream();
		paletteStream.content = content;
		paletteStream.contentOffset = paletteOffset;
		paletteStream.contentLength = bitmapOffset;
		for (int y = 0; y < this.height; y++) {
			for (int c = mode == 3 ? 6 : 1; c < 16; c++)
				palette[c] = paletteStream.read();
			int changeX = DECODE_MPP_SCREEN_FIRST_CHANGE_X[mode] & 0xff;
			int changeColor = 0;
			for (int x = 0; x < this.width; x++) {
				if (x == changeX) {
					palette[changeColor & 15] = changeColor == (DECODE_MPP_SCREEN_RIGHT_BORDER_COLOR[mode] & 0xff) ? 0 : paletteStream.read();
					switch (mode) {
					case 0:
					case 3:
						switch (changeColor) {
						case 15:
							changeX += mode == 0 ? 88 : 112;
							break;
						case 31:
							changeX += 12;
							break;
						case 37:
							changeX += 100;
							break;
						default:
							changeX += 4;
							break;
						}
						break;
					case 1:
						changeX += (changeColor & 1) == 0 ? 4 : 16;
						break;
					case 2:
						changeX += 8;
						break;
					default:
						throw new AssertionError();
					}
					changeColor++;
				}
				this.pixels[pixelsOffset + x] = palette[getStLowPixel(content, bitmapOffset, x)];
			}
			bitmapOffset += this.width >> 1;
			pixelsOffset += this.width;
		}
	}

	private boolean decodeMpp(byte[] content, int contentLength)
	{
		if (contentLength < 12 || content[0] != 77 || content[1] != 80 || content[2] != 80)
			return false;
		int mode = content[3] & 0xff;
		if (mode > 3)
			return false;
		int width = mode < 3 ? 320 : 416;
		int height = mode < 3 ? 199 : 273;
		int frames = 1 + ((content[4] & 0xff) >> 2 & 1);
		int paletteLength = (DECODE_MPP_MODE_COLORS_PER_LINE[mode] & 0xff) * height;
		switch (content[4] & 3) {
		case 0:
			setSize(width, height, RECOILResolution.ST1X1, frames);
			paletteLength *= 9;
			break;
		case 1:
			setSize(width, height, RECOILResolution.STE1X1, frames);
			paletteLength *= 12;
			break;
		case 3:
			setSize(width, height, RECOILResolution.STE1X1, frames);
			this.frames = 2;
			paletteLength *= 15;
			break;
		default:
			return false;
		}
		paletteLength = (paletteLength + 15) >> 4 << 1;
		int paletteOffset = 12 + get32BigEndian(content, 8);
		if (paletteOffset < 12)
			return false;
		int pixelsLength = width * height;
		if (contentLength != paletteOffset + (paletteLength + (pixelsLength >> 1)) * frames)
			return false;
		decodeMppScreen(content, paletteOffset, paletteLength, 0);
		if (frames == 1)
			return true;
		decodeMppScreen(content, paletteOffset + paletteLength + (pixelsLength >> 1), paletteLength, pixelsLength);
		return applyBlend();
	}

	private boolean decodeHrm(byte[] content, int contentLength)
	{
		if (contentLength != 92000)
			return false;
		setSize(640, 400, RECOILResolution.STE1X2);
		this.frames = 2;
		for (int y = 0; y < 400; y++) {
			for (int x = 0; x < 640; x++) {
				int offset = y * 160 + (x >> 2 & -4) + (x >> 3 & 1);
				int bit = ~x & 7;
				int c = ((content[offset] & 0xff) >> bit & 1) | ((content[offset + 2] & 0xff) >> bit & 1) << 1;
				c += ((x + (DECODE_HRM_COLOR_OFFSETS[c] & 0xff)) / 80 << 2) - 1;
				int rgb = getStColor(content, 64000 + y * 70 + (c << 1));
				int pixelsOffset = y * 640 + x;
				if ((y & 1) == 0)
					this.pixels[pixelsOffset] = rgb;
				else {
					int rgb1 = this.pixels[pixelsOffset - 640];
					this.pixels[pixelsOffset] = this.pixels[pixelsOffset - 640] = (rgb1 & rgb) + ((rgb1 ^ rgb) >> 1 & 8355711);
				}
			}
		}
		return true;
	}

	private boolean decodeStIcn(byte[] content, int contentLength)
	{
		final IcnParser parser = new IcnParser();
		parser.content = content;
		parser.contentOffset = 0;
		parser.contentLength = contentLength;
		int width = parser.parseDefine("ICON_W");
		if (width <= 0 || width >= 256)
			return false;
		int height = parser.parseDefine("ICON_H");
		if (height <= 0 || height >= 256)
			return false;
		int size = parser.parseDefine("ICONSIZE");
		if (size != ((width + 15) >> 4) * height)
			return false;
		if (!parser.expectAfterWhitespace("int") || !parser.expectAfterWhitespace("image[ICONSIZE]") || !parser.expectAfterWhitespace("=") || !parser.expectAfterWhitespace("{"))
			return false;
		final byte[] bitmap = new byte[8192];
		for (int i = 0;;) {
			int value = parser.parseHex();
			if (value < 0)
				return false;
			bitmap[i * 2] = (byte) (value >> 8);
			bitmap[i * 2 + 1] = (byte) value;
			if (++i >= size)
				break;
			if (parser.contentOffset >= contentLength)
				return false;
			if (content[parser.contentOffset++] != 44)
				return false;
		}
		if (!parser.expectAfterWhitespace("};"))
			return false;
		setSize(width, height, RECOILResolution.ST1X1);
		return decodeBlackAndWhite(bitmap, 0, size << 1, true, 16777215);
	}

	private boolean decodeCe(byte[] content, int contentLength)
	{
		if (contentLength < 192022 || !isStringAt(content, 0, "EYES") || content[4] != 0)
			return false;
		switch (content[5]) {
		case 0:
			if (contentLength != 192022)
				return false;
			setSize(320, 200, RECOILResolution.ST1X1);
			for (int y = 0; y < 200; y++) {
				for (int x = 0; x < 320; x++) {
					int offset = 22 + x * 200 + y;
					int rgb = (content[offset] & 0xff) << 16 | (content[64000 + offset] & 0xff) << 8 | content[128000 + offset] & 0xff;
					if ((rgb & 12632256) != 0)
						return false;
					this.pixels[y * 320 + x] = rgb << 2 | (rgb >> 4 & 197379);
				}
			}
			return true;
		case 1:
			if (contentLength != 256022)
				return false;
			setSize(640, 400, RECOILResolution.ST1X2);
			for (int y = 0; y < 200; y++) {
				for (int x = 0; x < 640; x++) {
					int offset = (11 + x * 200 + y) << 1;
					int c = (content[offset] & 0xff) << 8 | content[offset + 1] & 0xff;
					if (c >= 32768)
						return false;
					offset = y * 1280 + x;
					this.pixels[offset + 640] = this.pixels[offset] = getR5G5B5Color(c);
				}
			}
			return true;
		case 2:
			if (contentLength != 256022)
				return false;
			setSize(640, 400, RECOILResolution.ST1X1);
			for (int y = 0; y < 400; y++) {
				for (int x = 0; x < 640; x++) {
					int b = content[22 + x * 400 + (y & 1) * 200 + (y >> 1)] & 0xff;
					if (b > 191)
						return false;
					this.pixels[y * 640 + x] = b * 4 / 3 * 65793;
				}
			}
			return true;
		default:
			return false;
		}
	}

	private boolean decodeIbi(byte[] content, int contentLength)
	{
		if ((contentLength != 704 && contentLength != 1600) || content[0] != 73 || content[1] != 67 || content[2] != 66 || (content[3] != 73 && content[3] != 51) || content[8] != 0 || content[9] != 32 || content[10] != 0 || content[11] != 32)
			return false;
		setSize(32, 32, RECOILResolution.FALCON1X1);
		setDefaultStPalette(4);
		decodeBitplanes(content, 64, 16, 4, 0, 32, 32);
		return true;
	}

	private boolean decodeFalconGrayscale(byte[] content, int contentOffset, int contentLength, int width, int height)
	{
		int pixelsLength = width * height;
		if (contentLength != contentOffset + pixelsLength || !setSize(width, height, RECOILResolution.FALCON1X1))
			return false;
		for (int i = 0; i < pixelsLength; i++)
			this.pixels[i] = (content[contentOffset + i] & 0xff) * 65793;
		return true;
	}

	private boolean decodeBw(byte[] content, int contentLength)
	{
		if (contentLength < 11 || !isStringAt(content, 0, "B&W256"))
			return false;
		int width = (content[6] & 0xff) << 8 | content[7] & 0xff;
		int height = (content[8] & 0xff) << 8 | content[9] & 0xff;
		return decodeFalconGrayscale(content, 10, contentLength, width, height);
	}

	private boolean decodeFalconHir(byte[] content, int contentLength)
	{
		if (contentLength < 11 || content[0] != 15 || content[1] != 15 || content[2] != 0 || content[3] != 1 || content[8] != 0 || content[9] != 1)
			return false;
		int width = (content[4] & 0xff) << 8 | content[5] & 0xff;
		int height = (content[6] & 0xff) << 8 | content[7] & 0xff;
		int pixelsLength = width * height;
		if (contentLength != 10 + pixelsLength || !setSize(width, height, RECOILResolution.FALCON1X1))
			return false;
		for (int i = 0; i < pixelsLength; i++) {
			int b = content[10 + i] & 0xff;
			if (b >= 128)
				return false;
			this.pixels[i] = b * 131586;
		}
		return true;
	}

	private boolean decodeRw(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 64000:
			setSize(320, 200, RECOILResolution.FALCON1X1);
			break;
		case 128000:
			setSize(640, 200, RECOILResolution.FALCON1X1);
			break;
		case 256000:
			setSize(640, 400, RECOILResolution.FALCON1X1);
			break;
		default:
			return false;
		}
		for (int i = 0; i < contentLength; i++)
			this.pixels[i] = (255 - (content[i] & 0xff)) * 65793;
		return true;
	}

	private void decodeR8G8G8X8Colors(byte[] content, int contentOffset, int count)
	{
		for (int i = 0; i < count; i++)
			this.pixels[i] = getR8G8B8Color(content, contentOffset + (i << 2));
	}

	private boolean decodeIim(byte[] content, int contentLength)
	{
		if (contentLength < 17 || !isStringAt(content, 0, "IS_IMAGE") || content[8] != 0)
			return false;
		int width = (content[12] & 0xff) << 8 | content[13] & 0xff;
		int height = (content[14] & 0xff) << 8 | content[15] & 0xff;
		int pixelsLength = width * height;
		switch (content[9]) {
		case 0:
			return setSize(width, height, RECOILResolution.FALCON1X1) && decodeBlackAndWhite(content, 16, contentLength, false, 16777215);
		case 1:
			return decodeFalconGrayscale(content, 16, contentLength, width, height);
		case 4:
			if (contentLength != 16 + pixelsLength * 3 || !setSize(width, height, RECOILResolution.FALCON1X1))
				return false;
			decodeR8G8B8Colors(content, 16, pixelsLength, this.pixels, 0);
			return true;
		case 5:
			if (contentLength != 16 + (pixelsLength << 2) || !setSize(width, height, RECOILResolution.FALCON1X1))
				return false;
			decodeR8G8G8X8Colors(content, 17, pixelsLength);
			return true;
		default:
			return false;
		}
	}

	private void setFalconPalette(byte[] content, int contentOffset)
	{
		for (int i = 0; i < 256; i++) {
			int offset = contentOffset + (i << 2);
			this.contentPalette[i] = (content[offset] & 0xff) << 16 | (content[offset + 1] & 0xff) << 8 | content[offset + 3] & 0xff;
		}
	}

	private void decodeFalconPalette(byte[] content, int bitplanesOffset, int paletteOffset, int width, int height)
	{
		setFalconPalette(content, paletteOffset);
		setSize(width, height, RECOILResolution.FALCON1X1);
		decodeBitplanes(content, bitplanesOffset, width, 8, 0, width, height);
	}

	private boolean decodeFuckpaint(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 65024:
			decodeFalconPalette(content, 1024, 0, 320, 200);
			return true;
		case 77824:
			decodeFalconPalette(content, 1024, 0, 320, 240);
			return true;
		case 308224:
			decodeFalconPalette(content, 1024, 0, 640, 480);
			return true;
		default:
			return false;
		}
	}

	private boolean decodeDg1(byte[] content, int contentLength)
	{
		if (contentLength != 65032 || content[0] != 68 || content[1] != 71 || content[2] != 85 || content[3] != 1 || content[4] != 1 || content[5] != 64 || content[6] != 0 || content[7] != -56)
			return false;
		decodeFalconPalette(content, 1032, 8, 320, 200);
		return true;
	}

	private boolean decodeDc1(byte[] content, int contentLength)
	{
		if (contentLength < 1042 || content[0] != 68 || content[1] != 71 || content[2] != 67 || content[4] != 1 || content[5] != 64 || content[6] != 0 || content[7] != -56)
			return false;
		int compression = content[3] & 0xff;
		if (compression == 0) {
			if (contentLength != 65034)
				return false;
			decodeFalconPalette(content, 1034, 10, 320, 200);
			return true;
		}
		if (compression > 3)
			return false;
		final byte[] unpacked = new byte[64000];
		int contentOffset = 1038;
		int valueBytes = 1 << (compression - 1);
		int repeatCount = 0;
		for (int bitplane = 0; bitplane < 16; bitplane += 2) {
			for (int unpackedOffset = bitplane; unpackedOffset < 64000; unpackedOffset += 16) {
				for (int x = 0; x < 2; x++) {
					if (repeatCount == 0) {
						int nextContentOffset = contentOffset + compression * 2;
						if (nextContentOffset > contentLength) {
							unpacked[unpackedOffset + x] = 0;
							continue;
						}
						switch (compression) {
						case 1:
							repeatCount = (content[contentOffset] & 0xff) + 1;
							break;
						case 2:
							repeatCount = (((content[contentOffset] & 0xff) << 8) + (content[contentOffset + 1] & 0xff) + 1) << 1;
							break;
						case 3:
							repeatCount = (((content[contentOffset] & 0xff) << 8) + (content[contentOffset + 1] & 0xff) + 1) << 2;
							break;
						default:
							throw new AssertionError();
						}
						contentOffset = nextContentOffset;
					}
					unpacked[unpackedOffset + x] = (byte) (content[contentOffset - (--repeatCount & (valueBytes - 1)) - 1] & 0xff);
				}
			}
		}
		setFalconPalette(content, 10);
		setSize(320, 200, RECOILResolution.FALCON1X1);
		decodeBitplanes(unpacked, 0, 320, 8, 0, 320, 200);
		return true;
	}

	private boolean decodeDel(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[96000];
		return CaStream.unpackDel(content, contentLength, unpacked, 2) && decodeFuckpaint(unpacked, 77824);
	}

	private boolean decodeDph(byte[] content, int contentLength)
	{
		byte[] unpacked = new byte[320000];
		if (!CaStream.unpackDel(content, contentLength, unpacked, 10))
			return false;
		setFalconPalette(unpacked, 0);
		setSize(640, 480, RECOILResolution.FALCON1X1);
		decodeBitplanes(unpacked, 1024, 320, 8, 0, 320, 240);
		decodeBitplanes(unpacked, 77824, 320, 8, 320, 320, 240);
		decodeBitplanes(unpacked, 154624, 320, 8, 153600, 320, 240);
		decodeBitplanes(unpacked, 231424, 320, 8, 153920, 320, 240);
		return true;
	}

	private boolean decodeFalconTrueColor(byte[] content, int contentOffset, int width, int height, int resolution)
	{
		if (!setScaledSize(width, height, resolution))
			return false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				setScaledPixel(x, y, getFalconTrueColor(content, contentOffset));
				contentOffset += 2;
			}
		}
		return true;
	}

	private boolean decodeFalconTrueColorVariable(byte[] content, int contentLength, int widthOffset, int dataOffset)
	{
		int width = (content[widthOffset] & 0xff) << 8 | content[widthOffset + 1] & 0xff;
		int height = (content[widthOffset + 2] & 0xff) << 8 | content[widthOffset + 3] & 0xff;
		return dataOffset + width * height * 2 == contentLength && decodeFalconTrueColor(content, dataOffset, width, height, RECOILResolution.FALCON1X1);
	}

	private boolean decodeFtc(byte[] content, int contentLength)
	{
		return contentLength == 184320 && decodeFalconTrueColor(content, 0, 384, 240, RECOILResolution.FALCON1X1);
	}

	private boolean decodeXga(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 153600:
			return decodeFalconTrueColor(content, 0, 320, 240, RECOILResolution.FALCON1X1);
		case 368640:
			return decodeFalconTrueColor(content, 0, 384, 480, RECOILResolution.FALCON2X1);
		default:
			return false;
		}
	}

	private boolean decodeGod(byte[] content, int contentLength)
	{
		return contentLength > 6 && decodeFalconTrueColorVariable(content, contentLength, 2, 6);
	}

	private boolean decodeTrp(byte[] content, int contentLength)
	{
		return contentLength >= 9 && (isStringAt(content, 0, "TRUP") || isStringAt(content, 0, "tru?")) && decodeFalconTrueColorVariable(content, contentLength, 4, 8);
	}

	private boolean decodeTru(byte[] content, int contentLength)
	{
		return contentLength >= 256 && isStringAt(content, 0, "Indy") && decodeFalconTrueColorVariable(content, contentLength, 4, 256);
	}

	private boolean decodeTg1(byte[] content, int contentLength)
	{
		return contentLength >= 20 && isStringAt(content, 0, "COKE format.") && content[16] == 0 && content[17] == 18 && decodeFalconTrueColorVariable(content, contentLength, 12, 18);
	}

	private boolean decodeTcp(byte[] content, int contentLength)
	{
		return contentLength >= 218 && isStringAt(content, 0, "TRUECOLR") && content[12] == 0 && content[13] == 18 && content[14] == 0 && content[15] == 1 && content[16] == 0 && content[17] == 1 && isStringAt(content, 18, "PICT") && decodeFalconTrueColorVariable(content, contentLength, 28, 216);
	}

	private boolean decodeTre(byte[] content, int contentLength)
	{
		if (contentLength < 13 || !isStringAt(content, 0, "tre1"))
			return false;
		int width = (content[4] & 0xff) << 8 | content[5] & 0xff;
		int height = (content[6] & 0xff) << 8 | content[7] & 0xff;
		if (!setSize(width, height, RECOILResolution.FALCON1X1))
			return false;
		final Tre1Stream rle = new Tre1Stream();
		rle.content = content;
		rle.contentOffset = 12;
		rle.contentLength = contentLength;
		int pixelsLength = width * height;
		for (int i = 0; i < pixelsLength; i++) {
			int rgb = rle.readRle();
			if (rgb < 0)
				return false;
			this.pixels[i] = rgb;
		}
		return true;
	}

	private boolean decodeRag(byte[] content, int contentLength)
	{
		if (contentLength < 55 || !isStringAt(content, 0, "RAG-D!") || content[6] != 0 || content[7] != 0 || content[16] != 0)
			return false;
		int width = (content[12] & 0xff) << 8 | content[13] & 0xff;
		if ((width & 15) != 0)
			return false;
		int height = ((content[14] & 0xff) << 8) + (content[15] & 0xff) + 1;
		int bitplanes = content[17] & 0xff;
		int paletteLength = get32BigEndian(content, 18);
		switch (bitplanes) {
		case 1:
		case 2:
		case 4:
		case 8:
			switch (paletteLength) {
			case 32:
				if (bitplanes > 4)
					return false;
				break;
			case 1024:
				break;
			default:
				return false;
			}
			int bytesPerLine = (width >> 3) * bitplanes;
			if (30 + paletteLength + height * bytesPerLine > contentLength || !setSize(width, height, RECOILResolution.FALCON1X1))
				return false;
			if (paletteLength == 32)
				setStPalette(content, 30, 16);
			else
				setFalconPalette(content, 30);
			decodeBitplanes(content, 30 + paletteLength, bytesPerLine, bitplanes, 0, width, height);
			return true;
		case 16:
			return paletteLength == 1024 && contentLength >= 1054 + width * height * 2 && decodeFalconTrueColor(content, 1054, width, height, RECOILResolution.FALCON1X1);
		default:
			return false;
		}
	}

	private boolean decodeFalconFun(byte[] content, int contentLength)
	{
		if (contentLength < 14 || content[0] != 0 || content[1] != 10 || content[2] != -49 || content[3] != -30 || content[8] != 0)
			return false;
		int width = (content[4] & 0xff) << 8 | content[5] & 0xff;
		if ((width & 15) != 0)
			return false;
		int height = (content[6] & 0xff) << 8 | content[7] & 0xff;
		int bitplanes = content[9] & 0xff;
		switch (bitplanes) {
		case 1:
		case 2:
		case 4:
		case 8:
			int bytesPerLine = (width >> 3) * bitplanes;
			int paletteOffset = 25 + height * bytesPerLine;
			int colors = 1 << bitplanes;
			if (contentLength != paletteOffset + colors * 6 || !setSizeStOrFalcon(width, height, bitplanes, false))
				return false;
			if (bitplanes == 1)
				setDefaultStPalette(1);
			else
				setStVdiPalette(content, paletteOffset, colors, bitplanes);
			decodeScaledBitplanes(content, 13, width, height, bitplanes, false, null);
			return true;
		case 16:
			return contentLength >= 13 + width * height * 2 && decodeFalconTrueColor(content, 13, width, height, RECOILResolution.FALCON1X1);
		default:
			return false;
		}
	}

	private boolean decodeEsm(byte[] content, int contentLength)
	{
		if (contentLength < 812 || content[0] != 84 || content[1] != 77 || content[2] != 83 || content[3] != 0 || content[4] != 3 || content[5] != 44 || content[10] != 0)
			return false;
		int width = (content[6] & 0xff) << 8 | content[7] & 0xff;
		int height = (content[8] & 0xff) << 8 | content[9] & 0xff;
		if (!setSize(width, height, RECOILResolution.FALCON1X1))
			return false;
		int pixelsLength = width * height;
		switch (content[11]) {
		case 1:
			return decodeBlackAndWhite(content, 812, contentLength, false, 16777215);
		case 8:
			if (contentLength != 812 + pixelsLength)
				return false;
			for (int i = 0; i < 256; i++)
				this.contentPalette[i] = (content[36 + i] & 0xff) << 16 | (content[292 + i] & 0xff) << 8 | content[548 + i] & 0xff;
			for (int i = 0; i < pixelsLength; i++)
				this.pixels[i] = this.contentPalette[content[812 + i] & 0xff];
			return true;
		case 24:
			if (contentLength != 812 + pixelsLength * 3)
				return false;
			decodeR8G8B8Colors(content, 812, pixelsLength, this.pixels, 0);
			return true;
		default:
			return false;
		}
	}

	private boolean decodeFalconPix(byte[] content, int contentLength)
	{
		if (contentLength < 15 || !isStringAt(content, 0, "PIXT") || content[4] != 0)
			return false;
		int contentOffset;
		switch (content[5]) {
		case 1:
			contentOffset = 14;
			break;
		case 2:
			contentOffset = 16;
			break;
		default:
			return false;
		}
		int width = (content[8] & 0xff) << 8 | content[9] & 0xff;
		if ((width & 15) != 0)
			return false;
		int bitplanes = content[7] & 0xff;
		int height = (content[10] & 0xff) << 8 | content[11] & 0xff;
		int pixelsLength;
		switch (bitplanes) {
		case 1:
			return content[6] == 1 && setSizeStOrFalcon(width, height, 1, false) && decodeBlackAndWhite(content, contentOffset, contentLength, true, 16777215);
		case 2:
		case 4:
			int bitmapOffset = contentOffset + (3 << bitplanes);
			if (content[6] != 1 || contentLength != bitmapOffset + (width >> 3) * bitplanes * height || !setSizeStOrFalcon(width, height, bitplanes, false))
				return false;
			decodeR8G8B8Colors(content, contentOffset, 1 << bitplanes, this.contentPalette, 0);
			decodeScaledBitplanes(content, bitmapOffset, width, height, bitplanes, false, null);
			return true;
		case 8:
			if (content[6] != 0 || contentLength != contentOffset + 768 + width * height || !setSize(width, height, RECOILResolution.FALCON1X1))
				return false;
			decodeR8G8B8Colors(content, contentOffset, 256, this.contentPalette, 0);
			decodeBytes(content, contentOffset + 768);
			return true;
		case 16:
			return content[6] == 1 && contentLength == contentOffset + width * height * 2 && decodeFalconTrueColor(content, contentOffset, width, height, RECOILResolution.FALCON1X1);
		case 24:
			pixelsLength = width * height;
			if (content[6] != 1 || contentLength != contentOffset + pixelsLength * 3 || !setSize(width, height, RECOILResolution.FALCON1X1))
				return false;
			decodeR8G8B8Colors(content, contentOffset, pixelsLength, this.pixels, 0);
			return true;
		case 32:
			pixelsLength = width * height;
			if (contentLength != contentOffset + (pixelsLength << 2) || !setSize(width, height, RECOILResolution.FALCON1X1))
				return false;
			decodeR8G8G8X8Colors(content, contentOffset + 1, pixelsLength);
			return true;
		default:
			return false;
		}
	}

	private boolean decodePntUnpacked(byte[] content, byte[] bitmap, int bitmapOffset, int width, int height)
	{
		int bitplanes = content[13] & 0xff;
		switch (bitplanes) {
		case 1:
		case 2:
		case 4:
		case 8:
			if (!setSizeStOrFalcon(width, height, bitplanes, false))
				return false;
			int paletteLength = (content[6] & 0xff) << 8 | content[7] & 0xff;
			setStVdiPalette(content, 128, paletteLength, bitplanes);
			decodeScaledBitplanes(bitmap, bitmapOffset, width, height, bitplanes, false, null);
			return true;
		case 16:
			return decodeFalconTrueColor(bitmap, bitmapOffset, width, height, RECOILResolution.FALCON1X1);
		case 24:
			if (!setSize(width, height, RECOILResolution.FALCON1X1))
				return false;
			for (int y = 0; y < height; y++) {
				decodeR8G8B8Colors(bitmap, bitmapOffset, width, this.pixels, y * width);
				bitmapOffset += ((width + 15) & -16) * 3;
			}
			return true;
		default:
			return false;
		}
	}

	private boolean decodeFalconPnt(byte[] content, int contentLength)
	{
		if (contentLength < 128 || content[0] != 80 || content[1] != 78 || content[2] != 84 || content[3] != 0 || content[4] != 1 || content[5] != 0 || content[12] != 0 || content[14] != 0)
			return false;
		int paletteLength = (content[6] & 0xff) << 8 | content[7] & 0xff;
		int bitmapOffset = 128 + paletteLength * 6;
		int bitmapLength = get32BigEndian(content, 16);
		if (bitmapLength <= 0 || contentLength < bitmapOffset + bitmapLength)
			return false;
		int width = (content[8] & 0xff) << 8 | content[9] & 0xff;
		int height = (content[10] & 0xff) << 8 | content[11] & 0xff;
		int bitplanes = content[13] & 0xff;
		int unpackedLength = ((width + 15) >> 4 << 1) * height * bitplanes;
		switch (content[15]) {
		case 0:
			return bitmapLength == unpackedLength && decodePntUnpacked(content, content, bitmapOffset, width, height);
		case 1:
			byte[] unpacked = new byte[unpackedLength];
			final PackBitsStream rle = new PackBitsStream();
			rle.content = content;
			rle.contentOffset = bitmapOffset;
			rle.contentLength = contentLength;
			return rle.unpackBitplaneLines(unpacked, width, height, bitplanes, true, false) && decodePntUnpacked(content, unpacked, 0, width, height);
		default:
			return false;
		}
	}

	final void setOcsColor(int c, int r, int gb)
	{
		int rgb = (r & 15) << 16 | (gb & 240) << 4 | (gb & 15);
		this.contentPalette[c] = rgb * 17;
	}

	final void setOcsPalette(byte[] content, int contentOffset, int colors)
	{
		for (int c = 0; c < colors; c++) {
			int r = content[contentOffset++] & 0xff;
			int gb = content[contentOffset++] & 0xff;
			setOcsColor(c, r, gb);
		}
	}

	private boolean decodeInfo(byte[] content, int contentLength)
	{
		if (contentLength < 98 || content[0] != -29 || content[1] != 16 || content[2] != 0 || content[3] != 1)
			return false;
		int[] palette;
		switch (get32BigEndian(content, 44)) {
		case 0:
			palette = DECODE_INFO_OS1_PALETTE;
			break;
		case 1:
			palette = DECODE_INFO_OS2_PALETTE;
			break;
		default:
			return false;
		}
		int contentOffset = get32BigEndian(content, 66) == 0 ? 78 : 134;
		int width = (content[contentOffset + 4] & 0xff) << 8 | content[contentOffset + 5] & 0xff;
		int height = (content[contentOffset + 6] & 0xff) << 8 | content[contentOffset + 7] & 0xff;
		int bitplanes = (content[contentOffset + 8] & 0xff) << 8 | content[contentOffset + 9] & 0xff;
		switch (bitplanes) {
		case 2:
			break;
		case 3:
			if (palette == DECODE_INFO_OS1_PALETTE)
				return false;
			break;
		default:
			return false;
		}
		int bytesPerLine = (width + 15) >> 4 << 1;
		int bitplaneLength = height * bytesPerLine;
		contentOffset += 20;
		return contentLength >= contentOffset + bitplanes * bitplaneLength && decodeAmigaPlanar(content, contentOffset, width, height, RECOILResolution.AMIGA1X2, bitplanes, palette);
	}

	private boolean decodeAbkSprite(byte[] content, int contentLength)
	{
		if (content[10] != 0)
			return false;
		int bitplanes = content[11] & 0xff;
		if (bitplanes == 0 || bitplanes > 5)
			return false;
		int width = (content[6] & 0xff) << 8 | content[7] & 0xff;
		int height = (content[8] & 0xff) << 8 | content[9] & 0xff;
		int bitplaneLength = width * height << 1;
		int paletteOffset = 16 + bitplanes * bitplaneLength;
		if (paletteOffset + 64 > contentLength)
			return false;
		setOcsPalette(content, paletteOffset, 32);
		return decodeAmigaPlanar(content, 16, width << 4, height, RECOILResolution.AMIGA1X1, bitplanes, this.contentPalette);
	}

	private boolean decodeAbk(byte[] content, int contentLength)
	{
		if (contentLength < 82 || content[0] != 65 || content[1] != 109)
			return false;
		switch (content[2]) {
		case 83:
			return content[3] == 112 && decodeAbkSprite(content, contentLength);
		case 73:
			return content[3] == 99 && decodeAbkSprite(content, contentLength);
		case 66:
			break;
		default:
			return false;
		}
		if (content[3] != 107 || contentLength < 135 || !isStringAt(content, 12, "Pac.Pic") || content[110] != 6 || content[111] != 7 || content[112] != 25 || content[113] != 99 || content[124] != 0)
			return false;
		int width = (content[118] & 0xff) << 8 | content[119] & 0xff;
		int lumps = (content[120] & 0xff) << 8 | content[121] & 0xff;
		int lumpLines = (content[122] & 0xff) << 8 | content[123] & 0xff;
		int height = lumps * lumpLines;
		int bitplanes = content[125] & 0xff;
		if (bitplanes == 0 || bitplanes > 5 || !setSize(width << 3, height, RECOILResolution.AMIGA1X1))
			return false;
		int rleOffset = 110 + get32BigEndian(content, 126);
		if (rleOffset < 0 || rleOffset >= contentLength)
			return false;
		int pointsOffset = 110 + get32BigEndian(content, 130);
		if (pointsOffset < 0)
			return false;
		byte[] unpacked = new byte[bitplanes * width * height];
		int picOffset = 135;
		int pic = content[134] & 0xff;
		int rleBits = (content[rleOffset++] & 0xff) << 8 | 128;
		int pointsBits = 0;
		for (int bitplane = 0; bitplane < bitplanes; bitplane++) {
			for (int lump = 0; lump < lumps; lump++) {
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < lumpLines; y++) {
						rleBits <<= 1;
						if ((rleBits & 255) == 0) {
							pointsBits <<= 1;
							if ((pointsBits & 255) == 0) {
								if (pointsOffset >= contentLength)
									return false;
								pointsBits = (content[pointsOffset++] & 0xff) << 1 | 1;
							}
							if ((pointsBits >> 8 & 1) != 0) {
								if (rleOffset >= contentLength)
									return false;
								rleBits = (content[rleOffset++] & 0xff) << 1 | 1;
							}
							else {
								rleBits >>= 8;
							}
						}
						if ((rleBits >> 8 & 1) != 0) {
							pic = content[picOffset++] & 0xff;
						}
						unpacked[((bitplane * lumps + lump) * lumpLines + y) * width + x] = (byte) pic;
					}
				}
			}
		}
		setOcsPalette(content, 46, 32);
		return decodeAmigaPlanar(unpacked, 0, width << 3, height, RECOILResolution.AMIGA1X1, bitplanes, this.contentPalette);
	}

	private static int getAmigaAspectRatio(int xRatio, int yRatio, int resolution)
	{
		if (xRatio <= 0 || yRatio <= 0)
			return resolution;
		if (xRatio > yRatio * 6)
			return RECOILResolution.AMIGA8X1;
		if (xRatio > yRatio * 3)
			return RECOILResolution.AMIGA4X1;
		if (xRatio * 2 > yRatio * 3)
			return RECOILResolution.AMIGA2X1;
		if (yRatio > xRatio * 3)
			return RECOILResolution.AMIGA1X4;
		if (yRatio * 2 > xRatio * 3)
			return resolution == RECOILResolution.AMIGA1X1 ? RECOILResolution.AMIGA1X2 : RECOILResolution.ST1X2;
		return resolution;
	}

	private static int getCamgAspectRatio(int camg, int resolution)
	{
		int log;
		switch (camg & -61440) {
		case 0:
		case 69632:
		case 135168:
		case 462848:
		case 790528:
		case 856064:
			camg &= 32812;
			log = 0;
			break;
		case 266240:
			return RECOILResolution.AMIGA1X1;
		case 331776:
		case 528384:
		case 724992:
		case 921600:
			camg &= 32812;
			log = -1;
			break;
		case 200704:
		case 397312:
			camg &= 32805;
			log = -1;
			break;
		case 593920:
		case 659456:
			camg &= 33285;
			log = 0;
			break;
		default:
			return resolution;
		}
		switch (camg & 33312) {
		case 0:
			break;
		case 32768:
			log++;
			break;
		case 32800:
			log += 2;
			break;
		case 512:
			log--;
			break;
		default:
			return resolution;
		}
		switch (camg & 13) {
		case 0:
			break;
		case 4:
			log--;
			break;
		case 5:
			log -= 2;
			break;
		case 8:
			log++;
			break;
		default:
			return resolution;
		}
		switch (log) {
		case 0:
			return RECOILResolution.AMIGA1X1;
		case -1:
			return RECOILResolution.AMIGA2X1;
		case -2:
			return RECOILResolution.AMIGA4X1;
		case -3:
			return RECOILResolution.AMIGA8X1;
		case 1:
			return RECOILResolution.AMIGA1X2;
		case 2:
			return RECOILResolution.AMIGA1X4;
		default:
			return resolution;
		}
	}

	private boolean decodeDeep(byte[] content, int contentLength)
	{
		int width = 0;
		int height = 0;
		int compression = 0;
		int resolution = RECOILResolution.AMIGA1X1;
		final DeepStream rle = new DeepStream();
		rle.content = content;
		int tvdcOffset = -1;
		for (int contentOffset = 12; contentOffset < contentLength - 7;) {
			int chunkLength = get32BigEndian(content, contentOffset + 4);
			int chunkEndOffset = contentOffset + 8 + chunkLength;
			if (chunkEndOffset > contentLength || chunkEndOffset < contentOffset + 8)
				break;
			if (isStringAt(content, contentOffset, "DGBL")) {
				if (chunkLength < 8 || content[contentOffset + 12] != 0)
					return false;
				width = (content[contentOffset + 8] & 0xff) << 8 | content[contentOffset + 9] & 0xff;
				height = (content[contentOffset + 10] & 0xff) << 8 | content[contentOffset + 11] & 0xff;
				compression = content[contentOffset + 13] & 0xff;
				resolution = getAmigaAspectRatio(content[contentOffset + 14] & 0xff, content[contentOffset + 15] & 0xff, resolution);
			}
			else if (isStringAt(content, contentOffset, "DPEL")) {
				if (!rle.setDpel(contentOffset, chunkLength))
					return false;
			}
			else if (isStringAt(content, contentOffset, "DLOC")) {
				if (chunkLength < 4)
					return false;
				width = (content[contentOffset + 8] & 0xff) << 8 | content[contentOffset + 9] & 0xff;
				height = (content[contentOffset + 10] & 0xff) << 8 | content[contentOffset + 11] & 0xff;
			}
			else if (isStringAt(content, contentOffset, "TVDC")) {
				if (chunkLength != 32)
					return false;
				tvdcOffset = contentOffset + 8;
			}
			else if (isStringAt(content, contentOffset, "DBOD")) {
				if (chunkEndOffset + 8 < contentLength && isStringAt(content, chunkEndOffset, "DBOD"))
					return false;
				if (rle.components <= 0 || !setScaledSize(width, height, resolution))
					return false;
				rle.contentOffset = contentOffset + 8;
				rle.contentLength = chunkEndOffset;
				for (int y = 0; y < height; y++) {
					if (compression == 5) {
						if (tvdcOffset < 0 || !rle.readDeltaLine(width, tvdcOffset))
							return false;
					}
					for (int x = 0; x < width; x++) {
						int rgb;
						switch (compression) {
						case 0:
							rgb = rle.readValue();
							break;
						case 1:
							rgb = rle.readRle();
							break;
						case 5:
							rgb = rle.line[x];
							break;
						default:
							return false;
						}
						if (rgb < 0)
							return false;
						setScaledPixel(x, y, rgb);
					}
				}
				return true;
			}
			contentOffset = (chunkEndOffset + 1) & -2;
		}
		return false;
	}

	private boolean decodeRgbn(byte[] content, int contentOffset, int contentLength, int width, int height, boolean rgb8)
	{
		int rgb = 0;
		int count = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (count == 0) {
					if (rgb8) {
						if (contentOffset > contentLength - 4)
							return false;
						rgb = getR8G8B8Color(content, contentOffset);
						count = content[contentOffset + 3] & 127;
						contentOffset += 4;
					}
					else {
						if (contentOffset > contentLength - 2)
							return false;
						rgb = content[contentOffset] & 0xff;
						count = content[contentOffset + 1] & 0xff;
						rgb = (((rgb & 240) << 4 | (rgb & 15)) << 8 | count >> 4) * 17;
						count &= 7;
						contentOffset += 2;
					}
					if (count == 0) {
						if (contentOffset >= contentLength)
							return false;
						count = content[contentOffset++] & 0xff;
						if (count == 0) {
							if (contentOffset > contentLength - 2)
								return false;
							count = (content[contentOffset] & 0xff) << 8 | content[contentOffset + 1] & 0xff;
							contentOffset += 2;
						}
					}
				}
				setScaledPixel(x, y, rgb);
				count--;
			}
		}
		return true;
	}

	private boolean decodeRast(byte[] content, int contentOffset, int contentLength, byte[] unpacked, int width, int height, int bitplanes)
	{
		if (!setSizeStOrFalcon(width, height, bitplanes, false))
			return false;
		final RastPalette rast = new RastPalette();
		rast.content = content;
		rast.contentOffset = contentOffset + 8;
		rast.contentLength = contentLength;
		rast.colors = 1 << bitplanes;
		decodeScaledBitplanes(unpacked, 0, width, height, bitplanes, false, rast);
		return true;
	}

	private void decodeHam(byte[] unpacked, int width, int height, int bitplanes, MultiPalette multiPalette)
	{
		int bytesPerLine = ((width + 15) >> 4 << 1) * bitplanes;
		int holdBits = bitplanes > 6 ? 6 : 4;
		for (int y = 0; y < height; y++) {
			if (multiPalette != null)
				multiPalette.setLinePalette(this, y);
			int rgb = this.contentPalette[0];
			for (int x = 0; x < width; x++) {
				int c = getBitplaneWordsPixel(unpacked, y * bytesPerLine, x, bitplanes);
				switch (c >> holdBits) {
				case 0:
					rgb = this.contentPalette[c];
					break;
				case 1:
					c = c << (8 - holdBits) & 255;
					c |= c >> holdBits;
					rgb = (rgb & 16776960) | c;
					break;
				case 2:
					c = c << (8 - holdBits) & 255;
					c |= c >> holdBits;
					rgb = (rgb & 65535) | c << 16;
					break;
				case 3:
					c = c << (8 - holdBits) & 255;
					c |= c >> holdBits;
					rgb = (rgb & 16711935) | c << 8;
					break;
				default:
					throw new AssertionError();
				}
				setScaledPixel(x, y, rgb);
			}
		}
	}

	private int getHameNibble(byte[] content, int contentOffset, int x)
	{
		int c = getStLowPixel(content, contentOffset, x);
		int rgb = this.contentPalette[c];
		return (rgb >> 20 & 8) | (rgb >> 13 & 4) | (rgb >> 6 & 2) | (rgb >> 4 & 1);
	}

	private int getHameByte(byte[] content, int contentOffset, int x)
	{
		return getHameNibble(content, contentOffset, x << 1) << 4 | getHameNibble(content, contentOffset, (x << 1) + 1);
	}

	private boolean isHame(byte[] content, int contentOffset)
	{
		for (int i = 0; i < 7; i++) {
			if (getHameByte(content, contentOffset, i) != (IS_HAME_MAGIC[i] & 0xff))
				return false;
		}
		switch (getHameByte(content, contentOffset, 7)) {
		case 20:
		case 24:
			return true;
		default:
			return false;
		}
	}

	private void decodeHame(byte[] content, int width)
	{
		final int[] palette = new int[512];
		final int[] paletteLength = new int[2];
		boolean hame = false;
		for (int y = 0; y < this.height; y++) {
			int lineOffset = y * width;
			int paletteOffset = this.resolution == RECOILResolution.AMIGA_HAME2X1 && (y & 1) != 0 ? 256 : 0;
			if (isHame(content, lineOffset)) {
				paletteOffset += paletteLength[paletteOffset >> 8];
				for (int c = 0; c < 64; c++) {
					int offset = 8 + c * 3;
					palette[paletteOffset + c] = getHameByte(content, lineOffset, offset) << 16 | getHameByte(content, lineOffset, offset + 1) << 8 | getHameByte(content, lineOffset, offset + 2);
				}
				paletteLength[paletteOffset >> 8] = (paletteLength[paletteOffset >> 8] + 64) & 255;
				hame = getHameByte(content, lineOffset, 7) == 24;
				Arrays.fill(this.pixels, y * this.width, y * this.width + this.width, 0);
			}
			else {
				int paletteBank = 0;
				int rgb = 0;
				for (int x = 0; x < width; x++) {
					int c = getHameByte(content, lineOffset, x);
					if (hame) {
						switch (c >> 6) {
						case 0:
							if (c < 60)
								rgb = palette[paletteOffset + paletteBank + c];
							else
								paletteBank = (c - 60) << 6;
							break;
						case 1:
							rgb = (c & 63) << 2 | (rgb & 16776960);
							break;
						case 2:
							rgb = (c & 63) << 18 | (rgb & 65535);
							break;
						default:
							rgb = (c & 63) << 10 | (rgb & 16711935);
							break;
						}
					}
					else
						rgb = palette[paletteOffset + c];
					setScaledPixel(x, y, rgb);
				}
			}
		}
	}

	private static final int DCTV_MAX_WIDTH = 2048;

	private int getDctvValue(byte[] content, int contentOffset, int x, int bitplanes)
	{
		int c = getBitplaneWordsPixel(content, contentOffset, x, bitplanes);
		int rgb = this.contentPalette[c];
		return (rgb << 2 & 64) | (rgb >> 19 & 16) | (rgb >> 5 & 4) | (rgb >> 15 & 1);
	}

	private boolean isDctv(byte[] content, int contentOffset, int bitplanes)
	{
		if (getDctvValue(content, contentOffset, 0, bitplanes) >> 6 != 0)
			return false;
		int r = 125;
		for (int x = 1; x < 256; x++) {
			if (getDctvValue(content, contentOffset, x, bitplanes) >> 6 == (r & 1))
				return false;
			if ((r & 1) != 0)
				r ^= 390;
			r >>= 1;
		}
		return true;
	}

	private static int clampByte(int x)
	{
		return x <= 0 ? 0 : x >= 255 ? 255 : x;
	}

	private boolean decodeDctv(byte[] content, int width, int height, int resolution, int bitplanes)
	{
		if (!isDctv(content, 0, bitplanes))
			return false;
		int interlace;
		int bytesPerLine = ((width + 15) >> 4 << 1) * bitplanes;
		if (resolution == RECOILResolution.AMIGA1X2) {
			interlace = 0;
			height--;
			resolution = RECOILResolution.AMIGA_DCTV1X2;
		}
		else {
			if (!isDctv(content, bytesPerLine, bitplanes))
				return false;
			interlace = 1;
			height -= 2;
			resolution = RECOILResolution.AMIGA_DCTV1X1;
		}
		setScaledSize(width, height, resolution);
		int contentOffset = bytesPerLine << interlace;
		final int[] chroma = new int[2048];
		for (int y = 0; y < height; y++) {
			int odd = y >> interlace & 1;
			int rgb = 0;
			int o = 0;
			int p = 0;
			for (int x = 0; x < width; x++) {
				if ((x & 1) == odd) {
					int n = x + 1 < width ? getDctvValue(content, contentOffset, x, bitplanes) << 1 | getDctvValue(content, contentOffset, x + 1, bitplanes) : 0;
					int i = (o + n) >> 1;
					i = i <= 64 ? 0 : i >= 224 ? 255 : (i - 64) * 8 / 5;
					int u = n + p - (o << 1);
					if (u < 0)
						u += 3;
					u >>= 2;
					if (((x + 1) & 2) == 0)
						u = -u;
					int chromaOffset = (x & -2) | (y & interlace);
					int v = y > interlace ? chroma[chromaOffset] : 0;
					chroma[chromaOffset] = u;
					if (odd == 0) {
						u = v;
						v = chroma[chromaOffset];
					}
					p = o;
					o = n;
					int r = i + v * 4655 / 2560;
					int b = i + u * 8286 / 2560;
					int g = i - (v * 2372 + u * 1616) / 2560;
					rgb = clampByte(r) << 16 | clampByte(g) << 8 | clampByte(b);
				}
				setScaledPixel(x, y, rgb);
			}
			contentOffset += bytesPerLine;
		}
		return true;
	}

	private boolean decodeIffUnpacked(byte[] unpacked, int width, int height, int resolution, int bitplanes, int colors, int camg, MultiPalette multiPalette)
	{
		if (!setScaledSize(width, height, resolution))
			return false;
		if (bitplanes <= 8) {
			if (colors == 0) {
				colors = 1 << bitplanes;
				for (int c = 0; c < colors; c++)
					this.contentPalette[c] = c * 255 / colors * 65793;
			}
			if ((camg & 2048) != 0 || (bitplanes == 6 && colors == 16)) {
				decodeHam(unpacked, width, height, bitplanes, multiPalette);
			}
			else if (width >= 400 && (resolution == RECOILResolution.AMIGA1X2 || resolution == RECOILResolution.AMIGA1X1) && bitplanes == 4 && multiPalette == null && isHame(unpacked, 0)) {
				if (resolution == RECOILResolution.AMIGA1X2)
					setSize(width >> 1, height, RECOILResolution.AMIGA_HAME1X1);
				else
					setSize(width, height, RECOILResolution.AMIGA_HAME2X1);
				decodeHame(unpacked, width >> 1);
			}
			else if (width >= 256 && width <= 2048 && height >= 3 && (resolution == RECOILResolution.AMIGA1X2 || resolution == RECOILResolution.AMIGA1X1) && multiPalette == null && decodeDctv(unpacked, width, height, resolution, bitplanes)) {
			}
			else {
				decodeScaledBitplanes(unpacked, 0, width, height, bitplanes, bitplanes == 6 && ((camg & 128) != 0 || colors == 32), multiPalette);
			}
		}
		else {
			int bytesPerBitplane = (width + 15) >> 4 << 1;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int offset = (y * bytesPerBitplane + (x >> 3 & -2)) * bitplanes + (x >> 3 & 1);
					int c = getBitplanePixel(unpacked, offset, x, 24, 2);
					setScaledPixel(x, y, (c & 255) << 16 | (c & 65280) | c >> 16);
				}
			}
		}
		return true;
	}

	private boolean decodeIff(byte[] content, int contentLength, int resolution)
	{
		if (contentLength < 56 || !isStringAt(content, 0, "FORM"))
			return false;
		if (isStringAt(content, 8, "DEEP") || isStringAt(content, 8, "TVPP"))
			return decodeDeep(content, contentLength);
		int contentOffset = 8;
		if (isStringAt(content, 8, "DPSTDPAH") && get32BigEndian(content, 16) == 24 && isStringAt(content, 44, "FORM"))
			contentOffset = 52;
		else if (isStringAt(content, 8, "ANIMFORM"))
			contentOffset = 20;
		int type;
		if (isStringAt(content, contentOffset, "ILBM"))
			type = IffType.ILBM;
		else if (isStringAt(content, contentOffset, "PBM "))
			type = IffType.PBM;
		else if (isStringAt(content, contentOffset, "ACBM"))
			type = IffType.ACBM;
		else if (isStringAt(content, contentOffset, "RGB8"))
			type = IffType.RGB8;
		else if (isStringAt(content, contentOffset, "RGBN"))
			type = IffType.RGBN;
		else
			return false;
		contentOffset += 4;
		int width = 0;
		int height = 0;
		int bitplanes = 0;
		boolean hasMask = false;
		int compression = 0;
		boolean ocsPalette = false;
		int colors = 0;
		int camg = 0;
		final CtblPalette ctbl = new CtblPalette();
		final ShamLacePalette sham = new ShamLacePalette();
		final PchgPalette pchg = new PchgPalette();
		MultiPalette multiPalette = null;
		while (contentOffset < contentLength - 7) {
			int chunkLength = get32BigEndian(content, contentOffset + 4);
			int chunkEndOffset = contentOffset + 8 + chunkLength;
			if (chunkEndOffset > contentLength || chunkEndOffset < contentOffset + 8) {
				chunkEndOffset = contentLength;
				chunkLength = contentLength - contentOffset - 8;
			}
			if (isStringAt(content, contentOffset, "BMHD") && chunkLength >= 16) {
				width = (content[contentOffset + 8] & 0xff) << 8 | content[contentOffset + 9] & 0xff;
				height = (content[contentOffset + 10] & 0xff) << 8 | content[contentOffset + 11] & 0xff;
				bitplanes = content[contentOffset + 16] & 0xff;
				hasMask = content[contentOffset + 17] == 1;
				compression = content[contentOffset + 18] & 0xff;
				switch (type) {
				case IffType.PBM:
					if (bitplanes != 8 || compression > 1)
						return false;
					break;
				case IffType.RGB8:
					if (bitplanes != 25 || compression != 4)
						return false;
					break;
				case IffType.RGBN:
					if (bitplanes != 13 || compression != 4)
						return false;
					break;
				default:
					if (bitplanes == 0 || (bitplanes > 8 && bitplanes != 24 && bitplanes != 32) || compression > 2)
						return false;
					break;
				}
				int pixelsCount = width * height;
				if (pixelsCount <= 0 || pixelsCount > 134217728)
					return false;
				ocsPalette = content[contentOffset + 19] != -128;
				resolution = getAmigaAspectRatio(content[contentOffset + 22] & 0xff, content[contentOffset + 23] & 0xff, resolution);
			}
			else if (isStringAt(content, contentOffset, "CMAP")) {
				colors = chunkLength / 3;
				if (colors > 256)
					return false;
				if (colors > 32)
					ocsPalette = false;
				for (int c = 0; c < colors; c++) {
					this.contentPalette[c] = getR8G8B8Color(content, contentOffset + 8 + c * 3);
					if ((this.contentPalette[c] & 986895) != 0)
						ocsPalette = false;
				}
				if (ocsPalette) {
					for (int c = 0; c < colors; c++)
						this.contentPalette[c] |= this.contentPalette[c] >> 4;
				}
				Arrays.fill(this.contentPalette, colors, colors + 256 - colors, 0);
			}
			else if (isStringAt(content, contentOffset, "CAMG") && chunkLength >= 4) {
				camg = get32BigEndian(content, contentOffset + 8);
				resolution = getCamgAspectRatio(camg, resolution);
			}
			else if ((isStringAt(content, contentOffset, "CTBL") || isStringAt(content, contentOffset, "BEAM")) && height > 0) {
				ctbl.colors = (chunkLength >> 1) / height;
				if (ctbl.colors <= 32) {
					ctbl.content = content;
					ctbl.contentOffset = contentOffset + 8;
					multiPalette = ctbl;
				}
			}
			else if (isStringAt(content, contentOffset, "SHAM") && chunkLength >= 2 && content[contentOffset + 8] == 0 && content[contentOffset + 9] == 0) {
				if (chunkLength == 2 + (height << 5)) {
					ctbl.content = content;
					ctbl.contentOffset = contentOffset + 10;
					ctbl.colors = 16;
					multiPalette = ctbl;
				}
				else if (chunkLength == 2 + (height >> 1 << 5)) {
					sham.content = content;
					sham.contentOffset = contentOffset + 10;
					multiPalette = sham;
				}
			}
			else if (isStringAt(content, contentOffset, "PCHG")) {
				pchg.content = content;
				pchg.contentOffset = contentOffset + 8;
				pchg.contentLength = chunkEndOffset;
				if (!pchg.init())
					return false;
				multiPalette = pchg;
			}
			else if (isStringAt(content, contentOffset, "BODY")) {
				if (width == 0)
					return false;
				if (compression == 4)
					return setScaledSize(width, height, resolution) && decodeRgbn(content, contentOffset + 8, chunkEndOffset, width, height, type == IffType.RGB8);
				int bytesPerLine = ((width + 15) >> 4 << 1) * bitplanes;
				byte[] unpacked;
				if (compression == 2) {
					unpacked = new byte[bytesPerLine * height];
					final VdatStream rle = new VdatStream();
					rle.content = content;
					rle.contentOffset = contentOffset + 8;
					for (int bitplane = 0; bitplane < bitplanes; bitplane++) {
						if (rle.contentOffset + 14 > chunkEndOffset || !isStringAt(content, rle.contentOffset, "VDAT"))
							return false;
						int nextContentOffset = rle.contentOffset + 8 + get32BigEndian(content, rle.contentOffset + 4);
						if (nextContentOffset > chunkEndOffset)
							return false;
						rle.valueOffset = rle.contentLength = rle.contentOffset + 8 + ((content[rle.contentOffset + 8] & 0xff) << 8) + (content[rle.contentOffset + 9] & 0xff);
						rle.valueLength = nextContentOffset;
						rle.contentOffset += 10;
						for (int x = bitplane << 1; x < bytesPerLine; x += bitplanes << 1) {
							int unpackedOffset = x;
							for (int y = 0; y < height; y++) {
								int b = rle.readRle();
								if (b < 0)
									return false;
								unpacked[unpackedOffset] = (byte) (b >> 8);
								unpacked[unpackedOffset + 1] = (byte) b;
								unpackedOffset += bytesPerLine;
							}
						}
						rle.contentOffset = nextContentOffset;
					}
					resolution = resolution == RECOILResolution.AMIGA1X2 ? RECOILResolution.ST1X2 : RECOILResolution.ST1X1;
				}
				else {
					final PackBitsStream rle = new PackBitsStream();
					rle.content = content;
					rle.contentOffset = contentOffset + 8;
					rle.contentLength = chunkEndOffset;
					if (type == IffType.PBM) {
						if (colors == 0 || !setScaledSize(width, height, resolution))
							return false;
						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								int b = compression == 0 ? rle.readByte() : rle.readRle();
								if (b < 0)
									return false;
								setScaledPixel(x, y, this.contentPalette[b]);
							}
							if ((width & 1) != 0 && (compression == 0 ? rle.readByte() : rle.readRle()) < 0)
								return false;
						}
						return true;
					}
					unpacked = new byte[bytesPerLine * height];
					if (!rle.unpackBitplaneLines(unpacked, width, height, bitplanes, compression == 1, hasMask))
						return false;
				}
				if (bitplanes <= 8 && chunkEndOffset < contentLength - 8) {
					int nextChunkOffset = (chunkEndOffset + 1) & -2;
					if (isStringAt(content, nextChunkOffset, "RAST"))
						return decodeRast(content, nextChunkOffset, contentLength, unpacked, width, height, bitplanes);
					if (isStringAt(content, chunkEndOffset, "RAST")) {
						return decodeRast(content, chunkEndOffset, contentLength, unpacked, width, height, bitplanes);
					}
				}
				return decodeIffUnpacked(unpacked, width, height, resolution, bitplanes, colors, camg, multiPalette);
			}
			else if (isStringAt(content, contentOffset, "ABIT")) {
				if (width == 0 || chunkLength != ((width + 15) >> 4 << 1) * height * bitplanes)
					return false;
				contentOffset += 8;
				byte[] unpacked = new byte[chunkLength];
				for (int bitplane = 0; bitplane < bitplanes; bitplane++) {
					for (int unpackedOffset = bitplane << 1; unpackedOffset < chunkLength; unpackedOffset += bitplanes << 1) {
						unpacked[unpackedOffset] = (byte) (content[contentOffset++] & 0xff);
						unpacked[unpackedOffset + 1] = (byte) (content[contentOffset++] & 0xff);
					}
				}
				return decodeIffUnpacked(unpacked, width, height, resolution, bitplanes, colors, camg, multiPalette);
			}
			contentOffset = (chunkEndOffset + 1) & -2;
		}
		return false;
	}

	private static int parseAtari8ExecutableHeader(byte[] content, int contentOffset)
	{
		if (content[contentOffset] != -1 || content[contentOffset + 1] != -1)
			return -1;
		int startAddress = content[contentOffset + 2] & 0xff | (content[contentOffset + 3] & 0xff) << 8;
		int endAddress = content[contentOffset + 4] & 0xff | (content[contentOffset + 5] & 0xff) << 8;
		return endAddress - startAddress + 1;
	}

	private static int getAtari8ExecutableOffset(byte[] content, int contentLength)
	{
		if (contentLength >= 7) {
			int blockLength = parseAtari8ExecutableHeader(content, 0);
			if (blockLength > 0 && 6 + blockLength == contentLength)
				return 6;
		}
		return 0;
	}

	private boolean setAtari8RawSize(byte[] content, int contentLength, int resolution)
	{
		int contentOffset = getAtari8ExecutableOffset(content, contentLength);
		int height = (contentLength - contentOffset) / 40;
		if (height == 0 || height > 240)
			return false;
		setSize(320, height, resolution);
		return true;
	}
	private final byte[] gtiaColors = new byte[16];

	private void setGtiaColor(int reg, int value)
	{
		value &= 254;
		switch (reg) {
		case 0:
		case 1:
		case 2:
		case 3:
			this.gtiaColors[reg] = (byte) value;
			break;
		case 4:
		case 5:
		case 6:
		case 7:
			this.gtiaColors[8 + reg] = this.gtiaColors[reg] = (byte) value;
			break;
		case 8:
			this.gtiaColors[11] = this.gtiaColors[10] = this.gtiaColors[9] = this.gtiaColors[8] = (byte) value;
			break;
		default:
			throw new AssertionError();
		}
	}

	private void setPM123PF0123Bak(byte[] content, int contentOffset)
	{
		for (int i = 0; i < 8; i++)
			setGtiaColor(1 + i, content[contentOffset + i] & 0xff);
	}

	private void setGtiaColors(byte[] content, int contentOffset)
	{
		this.gtiaColors[0] = (byte) (content[contentOffset] & 254);
		setPM123PF0123Bak(content, contentOffset + 1);
	}

	private void setPF21(byte[] content, int contentOffset)
	{
		this.gtiaColors[6] = (byte) (content[contentOffset] & 254);
		this.gtiaColors[5] = (byte) (content[contentOffset + 1] & 254);
	}

	private void setXeOsDefaultColors()
	{
		this.gtiaColors[8] = 0;
		this.gtiaColors[4] = 40;
		this.gtiaColors[5] = (byte) 202;
		this.gtiaColors[6] = (byte) 148;
		this.gtiaColors[7] = 70;
	}

	private void setGr15DefaultColors()
	{
		this.gtiaColors[8] = 0;
		this.gtiaColors[4] = 4;
		this.gtiaColors[5] = 8;
		this.gtiaColors[6] = 12;
	}

	private void setBakPF012(byte[] content, int contentOffset, int contentStride)
	{
		for (int i = 0; i < 4; i++)
			this.gtiaColors[i == 0 ? 8 : 3 + i] = (byte) (content[contentOffset + i * contentStride] & 254);
	}

	private void setBakPF0123(byte[] content, int contentOffset)
	{
		for (int i = 0; i < 5; i++)
			this.gtiaColors[i == 0 ? 8 : 3 + i] = (byte) (content[contentOffset + i] & 254);
	}

	private void setPF012Bak(byte[] content, int contentOffset)
	{
		for (int i = 0; i < 4; i++)
			this.gtiaColors[i == 3 ? 8 : 4 + i] = (byte) (content[contentOffset + i] & 254);
	}

	private void setPF0123Bak(byte[] content, int contentOffset)
	{
		for (int i = 0; i < 5; i++)
			this.gtiaColors[4 + i] = (byte) (content[contentOffset + i] & 254);
	}

	private void setPF0123Even(byte[] content, int contentOffset)
	{
		for (int i = 0; i < 4; i++)
			this.gtiaColors[4 + i] = (byte) (content[contentOffset + i * 2] & 254);
	}

	private void decodeAtari8Gr8(byte[] content, int contentOffset, byte[] frame, int frameOffset, int height)
	{
		final byte[] colors = new byte[2];
		colors[0] = (byte) (this.gtiaColors[6] & 0xff);
		colors[1] = (byte) ((this.gtiaColors[6] & 240) | (this.gtiaColors[5] & 14));
		frameOffset -= this.leftSkip;
		for (int y = 0; y < height; y++) {
			int x;
			for (x = this.leftSkip; x < this.width; x++) {
				int c = (content[contentOffset + (x >> 3)] & 0xff) >> (~x & 7) & 1;
				frame[frameOffset + x] = (byte) (colors[c] & 0xff);
			}
			for (; x < this.width + this.leftSkip; x++)
				frame[frameOffset + x] = (byte) (this.gtiaColors[8] & 0xff);
			contentOffset += (this.width + 7) >> 3;
			frameOffset += this.width;
		}
	}

	private void decodeAtari8Gr15(byte[] content, int contentOffset, int contentStride, byte[] frame, int frameOffset, int frameStride, int height)
	{
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < this.width; x++) {
				int c = (content[contentOffset + (x >> 3)] & 0xff) >> (~x & 6) & 3;
				frame[frameOffset + x] = (byte) (this.gtiaColors[c == 0 ? 8 : c + 3] & 0xff);
			}
			contentOffset += contentStride;
			frameOffset += frameStride;
		}
	}

	private void decodeAtari8Gr7(byte[] content, int contentOffset, byte[] frame, int frameOffset, int height)
	{
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < this.width; x++) {
				int c = (content[contentOffset + (x >> 3)] & 0xff) >> (~x & 6) & 3;
				frame[frameOffset + x + this.width] = frame[frameOffset + x] = (byte) (this.gtiaColors[c == 0 ? 8 : c + 3] & 0xff);
			}
			contentOffset += this.width >> 3;
			frameOffset += this.width << 1;
		}
	}

	private void decodeAtari8Gr3(byte[] content, byte[] frame)
	{
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				int c = (content[(y >> 3) * (this.width >> 5) + (x >> 5)] & 0xff) >> (~(x >> 2) & 6) & 3;
				frame[y * this.width + x] = (byte) (this.gtiaColors[c == 0 ? 8 : c + 3] & 0xff);
			}
		}
	}

	private void decodeAtari8Gr9(byte[] content, int contentOffset, int contentStride, byte[] frame, int frameOffset, int frameStride, int width, int height)
	{
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = x + this.leftSkip;
				int c = i < 0 || i >= width ? 0 : (content[contentOffset + (i >> 3)] & 0xff) >> (~i & 4) & 15;
				frame[frameOffset + x] = (byte) (this.gtiaColors[8] & 0xff | c);
			}
			contentOffset += contentStride;
			frameOffset += frameStride;
		}
	}

	private void decodeAtari8Gr11(byte[] content, int contentOffset, byte[] frame, int frameOffset, int frameStride, int height)
	{
		frameOffset -= this.leftSkip;
		for (int y = 0; y < height; y++) {
			int x;
			for (x = this.leftSkip; x < this.width; x++) {
				int c = (content[contentOffset + (x >> 3)] & 0xff) << (x & 4) & 240;
				c = c == 0 ? this.gtiaColors[8] & 240 : this.gtiaColors[8] & 0xff | c;
				frame[frameOffset + x] = (byte) c;
			}
			for (; x < this.width + this.leftSkip; x++)
				frame[frameOffset + x] = (byte) (this.gtiaColors[8] & 240);
			contentOffset += this.width >> 3;
			frameOffset += frameStride;
		}
	}

	private void decodeAtari8Gr10(byte[] content, int contentOffset, byte[] frame, int frameOffset, int frameStride, int height)
	{
		frameOffset += 2 - this.leftSkip;
		for (int y = 0; y < height; y++) {
			int x;
			for (x = this.leftSkip - 2; x < 0; x++)
				frame[frameOffset + x] = (byte) (this.gtiaColors[0] & 0xff);
			for (; x < this.width + this.leftSkip - 2; x++) {
				int c = (content[contentOffset + (x >> 3)] & 0xff) >> (~x & 4) & 15;
				frame[frameOffset + x] = (byte) (this.gtiaColors[c] & 0xff);
			}
			contentOffset += this.width >> 3;
			frameOffset += frameStride;
		}
	}

	private void decodeAtari8Gr11PalBlend(byte[] content, int contentOffset, int contentStride, byte[] frame, int y)
	{
		for (; y < this.height; y += 2) {
			int frameOffset = y * this.width - this.leftSkip;
			for (int x = this.leftSkip; x < this.width; x++) {
				int c = (content[contentOffset + (x >> 3)] & 0xff) << (x & 4) & 240;
				int i = ((y == 0 ? 0 : frame[frameOffset - this.width + x] & 15) + (y == this.height - 1 ? 0 : frame[frameOffset + this.width + x] & 15)) >> 1;
				frame[frameOffset + x] = (byte) (c | i);
				if (y < this.height - 1)
					frame[frameOffset + this.width + x] = (byte) (c | (frame[frameOffset + this.width + x] & 15));
			}
			Arrays.fill(frame, frameOffset + this.width, frameOffset + this.width + this.leftSkip, (byte) 0);
			contentOffset += contentStride;
		}
	}

	private static int toAtari8Char(int ascii)
	{
		switch (ascii & 96) {
		case 0:
			return ascii + 64;
		case 32:
		case 64:
			return ascii - 32;
		default:
			return ascii;
		}
	}

	private void decodeAtari8Gr0Line(byte[] characters, int charactersOffset, byte[] font, int fontOffset, byte[] frame, int frameOffset, int lines)
	{
		final byte[] colors = new byte[2];
		colors[0] = (byte) (this.gtiaColors[6] & 0xff);
		colors[1] = (byte) ((this.gtiaColors[6] & 240) | (this.gtiaColors[5] & 14));
		for (int y = 0; y < lines; y++) {
			for (int x = 0; x < this.width; x++) {
				int ch = charactersOffset + (x >> 3);
				if (characters != null)
					ch = characters[ch] & 0xff;
				int b = font[fontOffset + ((ch & 127) << 3) + (y & 7)] & 0xff;
				if (lines == 10) {
					switch (((ch & 96) + y) >> 1) {
					case 4:
					case 20:
					case 36:
					case 48:
						b = 0;
						break;
					default:
						break;
					}
				}
				if (ch >= 128)
					b ^= 255;
				frame[frameOffset + x] = (byte) (colors[b >> (~x & 7) & 1] & 0xff);
			}
			frameOffset += this.width;
		}
	}

	private void decodeAtari8Gr0(byte[] characters, int charactersOffset, int charactersStride, byte[] font, int fontOffset, byte[] frame)
	{
		this.gtiaColors[6] = 0;
		this.gtiaColors[5] = 14;
		for (int y = 0; y < this.height; y += 8)
			decodeAtari8Gr0Line(characters, charactersOffset + (y >> 3) * charactersStride, font, fontOffset, frame, y * this.width, 8);
	}

	private void decodeAtari8Gr1Line(byte[] content, int charactersOffset, byte[] font, int fontOffset, byte[] frame, int frameOffset, int doubleLine)
	{
		for (int y = 0; y < 8 << doubleLine; y++) {
			for (int x = 0; x < this.width; x++) {
				int ch = content[charactersOffset + (x >> 4)] & 0xff;
				int b = (font[fontOffset + ((ch & 63) << 3) + (y >> doubleLine)] & 0xff) >> (~(x >> 1) & 7) & 1;
				frame[frameOffset + x] = (byte) (this.gtiaColors[b == 0 ? 8 : 4 + (ch >> 6)] & 0xff);
			}
			frameOffset += this.width;
		}
	}

	private void decodeAtari8Gr12Line(byte[] characters, int charactersOffset, byte[] font, int fontOffset, byte[] frame, int frameOffset, int doubleLine)
	{
		for (int y = 0; y < 8 << doubleLine; y++) {
			for (int x = 0; x < this.width; x++) {
				int ch = x >> 3;
				if (characters != null)
					ch = characters[charactersOffset + ch] & 0xff;
				int c = (font[fontOffset + ((ch & 127) << 3) + (y >> doubleLine)] & 0xff) >> (~x & 6) & 3;
				int gr12Registers = ch >= 128 ? 30024 : 25928;
				frame[frameOffset + x] = (byte) (this.gtiaColors[gr12Registers >> (c << 2) & 15] & 0xff);
			}
			frameOffset += this.width;
		}
	}

	private void decodeAtari8Player(byte[] content, int contentOffset, int color, byte[] frame, int frameOffset, int height, boolean multi)
	{
		color &= 254;
		for (int y = 0; y < height; y++) {
			int b = content[contentOffset + y] & 0xff;
			for (int x = 0; x < 8; x++) {
				int c = b >> (7 - x) & 1;
				if (c != 0)
					frame[frameOffset + x * 2 + 1] = frame[frameOffset + x * 2] = (byte) (multi ? frame[frameOffset + x * 2] & 0xff | color : color);
			}
			frameOffset += this.width;
		}
	}

	private boolean applyAtari8Palette(byte[] frame)
	{
		int pixelsLength = this.width * this.height;
		for (int i = 0; i < pixelsLength; i++)
			this.pixels[i] = this.atari8Palette[frame[i] & 0xff];
		return true;
	}

	private boolean applyAtari8PaletteBlend(byte[] frame1, byte[] frame2)
	{
		int pixelsLength = this.width * this.height;
		this.frames = 2;
		for (int i = 0; i < pixelsLength; i++) {
			int rgb1 = this.atari8Palette[frame1[i] & 0xff];
			int rgb2 = this.atari8Palette[frame2[i] & 0xff];
			this.pixels[i] = (rgb1 & rgb2) + ((rgb1 ^ rgb2) >> 1 & 8355711);
		}
		return true;
	}

	private boolean applyAtari8PaletteBlend3(byte[] frame1, byte[] frame2, byte[] frame3)
	{
		int pixelsLength = this.width * this.height;
		this.frames = 3;
		for (int i = 0; i < pixelsLength; i++) {
			int rgb1 = this.atari8Palette[frame1[i] & 0xff];
			int rgb2 = this.atari8Palette[frame2[i] & 0xff];
			int rgb3 = this.atari8Palette[frame3[i] & 0xff];
			this.pixels[i] = ((rgb1 >> 16) + (rgb2 >> 16) + (rgb3 >> 16)) / 3 << 16 | ((rgb1 >> 8 & 255) + (rgb2 >> 8 & 255) + (rgb3 >> 8 & 255)) / 3 << 8 | ((rgb1 & 255) + (rgb2 & 255) + (rgb3 & 255)) / 3;
		}
		return true;
	}

	private boolean decodeGr8(byte[] content, int contentLength)
	{
		if (!setAtari8RawSize(content, contentLength, RECOILResolution.XE1X1))
			return false;
		int contentOffset = getAtari8ExecutableOffset(content, contentLength);
		if (contentLength == 7682) {
			this.gtiaColors[6] = (byte) (content[7680] & 14);
			this.gtiaColors[5] = (byte) (content[7681] & 14);
		}
		else {
			this.gtiaColors[6] = 0;
			this.gtiaColors[5] = 14;
		}
		final byte[] frame = new byte[76800];
		decodeAtari8Gr8(content, contentOffset, frame, 0, this.height);
		return applyAtari8Palette(frame);
	}

	private boolean decodeDrg(byte[] content, int contentLength)
	{
		return contentLength == 6400 && decodeGr8(content, contentLength);
	}

	private boolean decodeGr8Raw(byte[] content, int contentLength, int width, int height)
	{
		setSize(width, height, RECOILResolution.XE1X1);
		this.contentPalette[0] = this.atari8Palette[0];
		this.contentPalette[1] = this.atari8Palette[14];
		return decodeMono(content, 0, contentLength, false);
	}

	private boolean decodePsf(byte[] content, int contentLength)
	{
		if (contentLength < 572 || contentLength > 640)
			return false;
		setSize(88, 52, RECOILResolution.XE1X1);
		this.contentPalette[0] = this.atari8Palette[14];
		this.contentPalette[1] = this.atari8Palette[0];
		decodeBitplanes(content, 0, 11, 1, 0, 88, 52);
		return true;
	}

	private boolean decodeMonoArt(byte[] content, int contentLength)
	{
		if (contentLength < 4)
			return false;
		int columns = (content[0] & 0xff) + 1;
		int height = (content[1] & 0xff) + 1;
		if (columns > 30 || height > 64 || contentLength != 3 + columns * height)
			return false;
		setSize(columns << 3, height, RECOILResolution.XE1X1);
		this.contentPalette[0] = this.atari8Palette[14];
		this.contentPalette[1] = this.atari8Palette[0];
		decodeBitplanes(content, 2, columns, 1, 0, columns << 3, height);
		return true;
	}

	private boolean decodeGhg(byte[] content, int contentLength)
	{
		if (contentLength < 4)
			return false;
		int width = content[0] & 0xff | (content[1] & 0xff) << 8;
		int height = content[2] & 0xff;
		if (width == 0 || width > 320 || height == 0 || height > 200 || contentLength != 3 + ((width + 7) >> 3) * height)
			return false;
		setSize(width, height, RECOILResolution.XE1X1);
		final byte[] frame = new byte[64000];
		this.gtiaColors[6] = 12;
		this.gtiaColors[5] = 2;
		decodeAtari8Gr8(content, 3, frame, 0, height);
		return applyAtari8Palette(frame);
	}

	private boolean decodeCpr(byte[] content, int contentLength)
	{
		if (contentLength < 2)
			return false;
		final XeKoalaStream rle = new XeKoalaStream();
		rle.content = content;
		rle.contentOffset = 1;
		rle.contentLength = contentLength;
		if (!rle.unpackRaw(content[0] & 0xff, 7680))
			return false;
		this.gtiaColors[6] = 12;
		this.gtiaColors[5] = 0;
		setSize(320, 192, RECOILResolution.XE1X1);
		final byte[] frame = new byte[61440];
		decodeAtari8Gr8(rle.unpacked, 0, frame, 0, 192);
		return applyAtari8Palette(frame);
	}

	private boolean decodeSg3(byte[] content, int contentLength)
	{
		if (contentLength != 240)
			return false;
		setSize(320, 192, RECOILResolution.XE8X8);
		final byte[] frame = new byte[61440];
		setXeOsDefaultColors();
		decodeAtari8Gr3(content, frame);
		return applyAtari8Palette(frame);
	}

	private boolean decodeGr3(byte[] content, int contentLength)
	{
		if (contentLength != 244)
			return false;
		setSize(320, 192, RECOILResolution.XE8X8);
		final byte[] frame = new byte[61440];
		setBakPF012(content, 240, 1);
		decodeAtari8Gr3(content, frame);
		return applyAtari8Palette(frame);
	}

	private boolean decodeDit(byte[] content, int contentLength)
	{
		if (contentLength != 3845)
			return false;
		setSize(320, 192, RECOILResolution.XE2X2);
		final byte[] frame = new byte[61440];
		setPF0123Bak(content, 3840);
		decodeAtari8Gr7(content, 0, frame, 0, 96);
		return applyAtari8Palette(frame);
	}

	private boolean decodeVisualizer(byte[] content)
	{
		setSize(320, 158, RECOILResolution.XE2X2);
		final byte[] frame = new byte[50560];
		setPF0123Bak(content, 0);
		decodeAtari8Gr7(content, 5, frame, 0, 79);
		return applyAtari8Palette(frame);
	}

	private boolean decodeGr7(byte[] content, int contentOffset, int contentSize)
	{
		if (contentSize > 4804 || contentSize % 40 != 4)
			return false;
		int height = contentSize / 40;
		setSize(320, height * 2, RECOILResolution.XE2X2);
		final byte[] frame = new byte[76800];
		setBakPF012(content, contentOffset + contentSize - 4, 1);
		decodeAtari8Gr7(content, contentOffset, frame, 0, height);
		return applyAtari8Palette(frame);
	}

	private boolean decodeRys(byte[] content, int contentLength)
	{
		if (contentLength != 3840)
			return false;
		setSize(320, 192, RECOILResolution.XE2X2);
		final byte[] frame = new byte[61440];
		setXeOsDefaultColors();
		decodeAtari8Gr7(content, 0, frame, 0, 96);
		return applyAtari8Palette(frame);
	}

	private boolean decodeBkg(byte[] content, int contentLength)
	{
		return contentLength == 3856 && decodeGr7(content, 0, 3844);
	}

	private boolean decodeAtari8Artist(byte[] content, int contentLength)
	{
		if (contentLength != 3206 || content[0] != 7)
			return false;
		setSize(320, 160, RECOILResolution.XE2X2);
		final byte[] frame = new byte[51200];
		setPF0123Bak(content, 1);
		decodeAtari8Gr7(content, 6, frame, 0, 80);
		return applyAtari8Palette(frame);
	}

	private boolean decodeGr9(byte[] content, int contentLength)
	{
		if (!setAtari8RawSize(content, contentLength, RECOILResolution.XE4X1))
			return false;
		this.gtiaColors[8] = 0;
		int contentOffset = getAtari8ExecutableOffset(content, contentLength);
		final byte[] frame = new byte[76800];
		decodeAtari8Gr9(content, contentOffset, 40, frame, 0, 320, 320, this.height);
		return applyAtari8Palette(frame);
	}

	private boolean decodeRap(byte[] content, int contentLength)
	{
		if (contentLength != 7681)
			return false;
		this.gtiaColors[8] = (byte) (content[7680] & 254);
		setSize(320, 192, RECOILResolution.XE4X1);
		final byte[] frame = new byte[61440];
		decodeAtari8Gr9(content, 0, 40, frame, 0, 320, 320, 192);
		return applyAtari8Palette(frame);
	}

	private boolean decodeTxe(byte[] content, int contentLength)
	{
		if (contentLength != 3840)
			return false;
		setSize(320, 192, RECOILResolution.XE4X2);
		final byte[] frame = new byte[61440];
		this.gtiaColors[8] = 0;
		decodeAtari8Gr9(content, 0, 40, frame, 320, 640, 320, 96);
		decodeAtari8Gr9(content, 0, 40, frame, 0, 640, 320, 96);
		return applyAtari8Palette(frame);
	}

	private boolean decodeGr9x4(byte[] content, int contentOffset, int width, int height)
	{
		if (!setSize(width, height, RECOILResolution.XE4X4))
			return false;
		byte[] frame = new byte[width * height];
		this.gtiaColors[8] = 0;
		for (int y = 0; y < 4; y++)
			decodeAtari8Gr9(content, contentOffset, width >> 3, frame, y * width, width << 2, width, height >> 2);
		applyAtari8Palette(frame);
		return true;
	}

	private boolean decodeZm4(byte[] content, int contentLength)
	{
		return contentLength == 2048 && decodeGr9x4(content, 0, 256, 256);
	}

	private boolean decodeGr9p(byte[] content, int contentLength)
	{
		return contentLength == 2400 && decodeGr9x4(content, 0, 320, 240);
	}

	private boolean decodeFge(byte[] content, int contentLength)
	{
		return contentLength == 1286 && decodeGr9x4(content, 6, 256, 160);
	}

	private boolean decode16x16x16(byte[] content, int contentOffset, int colbak)
	{
		setSize(64, 64, RECOILResolution.XE4X4);
		for (int y = 0; y < 64; y++) {
			for (int x = 0; x < 64; x++) {
				int c = content[contentOffset + ((y & -4) << 2) + (x >> 2)] & 0xff;
				if (c > 15)
					return false;
				this.pixels[(y << 6) + x] = this.atari8Palette[colbak | c];
			}
		}
		return true;
	}

	private boolean decodeTx0(byte[] content, int contentLength)
	{
		return contentLength == 257 && decode16x16x16(content, 0, content[256] & 254);
	}

	private boolean decodeTxs(byte[] content, int contentLength)
	{
		return contentLength == 262 && content[0] == -1 && content[1] == -1 && content[2] == 0 && content[3] == 6 && content[4] == -1 && content[5] == 6 && decode16x16x16(content, 6, 0);
	}

	private boolean decodeA4r(byte[] content, int contentLength)
	{
		final A4rStream a4r = new A4rStream();
		a4r.content = content;
		a4r.contentOffset = 0;
		a4r.contentLength = contentLength;
		if (!a4r.unpackA4r())
			return false;
		setSize(320, 256, RECOILResolution.XE4X1);
		final byte[] frame = new byte[81920];
		this.gtiaColors[8] = 0;
		decodeAtari8Gr9(a4r.unpacked, 512, 40, frame, 0, 320, 320, 256);
		return applyAtari8Palette(frame);
	}

	private boolean decodeG11(byte[] content, int contentLength)
	{
		if (!setAtari8RawSize(content, contentLength, RECOILResolution.XE4X1))
			return false;
		this.gtiaColors[8] = 6;
		int contentOffset = getAtari8ExecutableOffset(content, contentLength);
		final byte[] frame = new byte[76800];
		decodeAtari8Gr11(content, contentOffset, frame, 0, 320, this.height);
		return applyAtari8Palette(frame);
	}

	private boolean decodeG10(byte[] content, int contentLength)
	{
		if (!setAtari8RawSize(content, contentLength, RECOILResolution.XE4X1))
			return false;
		int contentOffset = getAtari8ExecutableOffset(content, contentLength);
		if ((contentLength - contentOffset) % 40 != 9)
			return false;
		this.leftSkip = 2;
		setGtiaColors(content, contentLength - 9);
		final byte[] frame = new byte[76800];
		decodeAtari8Gr10(content, contentOffset, frame, 0, 320, this.height);
		return applyAtari8Palette(frame);
	}

	private boolean decodeG09(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 7680:
			return decodeGr9(content, contentLength);
		case 15360:
			break;
		default:
			return false;
		}
		setSize(640, 192, RECOILResolution.XE4X1);
		this.gtiaColors[8] = 0;
		final byte[] frame = new byte[122880];
		decodeAtari8Gr9(content, 0, 40, frame, 0, 640, 320, 192);
		decodeAtari8Gr9(content, 7680, 40, frame, 320, 640, 320, 192);
		return applyAtari8Palette(frame);
	}

	private boolean decodeSkp(byte[] content, int contentLength)
	{
		if (contentLength != 7680)
			return false;
		this.gtiaColors[8] = 38;
		this.gtiaColors[4] = 40;
		this.gtiaColors[5] = 0;
		this.gtiaColors[6] = 12;
		setSize(320, 192, RECOILResolution.XE2X1);
		final byte[] frame = new byte[61440];
		decodeAtari8Gr15(content, 0, 40, frame, 0, 320, 192);
		return applyAtari8Palette(frame);
	}

	private boolean decodeKss(byte[] content, int contentLength)
	{
		if (contentLength != 6404)
			return false;
		setSize(320, 160, RECOILResolution.XE2X1);
		final byte[] frame = new byte[51200];
		setBakPF012(content, 6400, 1);
		decodeAtari8Gr15(content, 0, 40, frame, 0, 320, 160);
		return applyAtari8Palette(frame);
	}

	private boolean decodeMic(String filename, byte[] content, int contentLength)
	{
		if (contentLength == 15872) {
			contentLength = 7680;
			setPF012Bak(content, 7680);
		}
		else {
			switch (contentLength % 40) {
			case 0:
			case 3:
				setGr15DefaultColors();
				break;
			case 4:
				setBakPF012(content, contentLength - 4, 1);
				break;
			case 5:
				setPF012Bak(content, contentLength - 5);
				break;
			default:
				return false;
			}
		}
		int height = contentLength / 40;
		if (height == 0 || height > 240)
			return false;
		setSize(320, height, RECOILResolution.XE2X1);
		final byte[] frame = new byte[76800];
		if (filename != null) {
			if (contentLength % 40 == 0) {
				final byte[] asm = new byte[131072];
				int asmLength = readCompanionFile(filename, "RP.INI", "rp.ini", asm, 131072);
				if (asmLength > 0) {
					final RastaStream s = new RastaStream();
					s.content = asm;
					s.contentLength = asmLength;
					if (!s.readIni())
						return false;
					s.contentLength = readCompanionFile(filename, "PMG", "pmg", asm, 131072);
					if (!s.readPmg())
						return false;
					s.contentLength = readCompanionFile(filename, "RP", "rp", asm, 131072);
					return s.readRp(content, height, frame) && applyAtari8Palette(frame);
				}
			}
			if (height == 240) {
				final byte[] col = new byte[1281];
				switch (readCompanionFile(filename, "COL", "col", col, 1281)) {
				case 1024:
				case 1280:
					for (int y = 0; y < 240; y++) {
						setBakPF012(col, y, 256);
						decodeAtari8Gr15(content, y * 40, 40, frame, y * 320, 320, 1);
					}
					return applyAtari8Palette(frame);
				default:
					break;
				}
			}
		}
		decodeAtari8Gr15(content, 0, 40, frame, 0, 320, height);
		return applyAtari8Palette(frame);
	}

	private boolean decodePi8(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 7680:
			return decodeMic(null, content, contentLength);
		case 7685:
			return decodeGr8(content, contentLength);
		default:
			return false;
		}
	}

	private boolean decodeHpm(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[7684];
		final HpmStream rle = new HpmStream();
		rle.content = content;
		rle.contentOffset = 0;
		rle.contentLength = contentLength;
		if (!rle.unpack(unpacked, 0, 1, 7680))
			return false;
		switch (rle.readByte()) {
		case 52:
		case 53:
			unpacked[7680] = 0;
			unpacked[7681] = 52;
			unpacked[7682] = (byte) (contentLength == 3494 ? 56 : 200);
			unpacked[7683] = (byte) (contentLength == 3494 ? 60 : 124);
			break;
		case 81:
			unpacked[7680] = (byte) 164;
			unpacked[7681] = 81;
			unpacked[7682] = (byte) 185;
			unpacked[7683] = 124;
			break;
		case 228:
			unpacked[7680] = 0;
			unpacked[7681] = (byte) 228;
			unpacked[7682] = (byte) 200;
			unpacked[7683] = (byte) 190;
			break;
		case 4:
			unpacked[7680] = 6;
			unpacked[7681] = 4;
			unpacked[7682] = 0;
			unpacked[7683] = 10;
			break;
		case 48:
			unpacked[7680] = 14;
			unpacked[7681] = 48;
			unpacked[7682] = (byte) 199;
			unpacked[7683] = 123;
			break;
		case 116:
			unpacked[7680] = 0;
			unpacked[7681] = 116;
			unpacked[7682] = 88;
			unpacked[7683] = 126;
			break;
		default:
			unpacked[7680] = 0;
			unpacked[7681] = 4;
			unpacked[7682] = 8;
			unpacked[7683] = 12;
			break;
		}
		return decodeMic(null, unpacked, 7684);
	}

	private boolean decodeCpi(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[7936];
		final CpiStream rle = new CpiStream();
		rle.content = content;
		rle.contentOffset = 0;
		rle.contentLength = contentLength;
		if (!rle.unpack(unpacked, 0, 1, 7936))
			return false;
		setSize(320, 192, RECOILResolution.XE2X1);
		final byte[] frame = new byte[61440];
		this.gtiaColors[8] = 0;
		this.gtiaColors[4] = 12;
		this.gtiaColors[5] = 8;
		this.gtiaColors[6] = 4;
		decodeAtari8Gr15(unpacked, 0, 40, frame, 0, 320, 192);
		return applyAtari8Palette(frame);
	}

	private boolean decodeWnd(byte[] content, int contentLength)
	{
		if (contentLength != 3072)
			return false;
		int width = (content[0] & 0xff) + 1;
		int contentStride = (width + 3) >> 2;
		int height = content[1] & 0xff;
		if (contentStride > 40 || height == 0 || height > 192 || contentStride * height > 3070)
			return false;
		setSize(width << 1, height, RECOILResolution.XE2X1);
		final byte[] frame = new byte[61440];
		this.gtiaColors[8] = 0;
		this.gtiaColors[4] = 70;
		this.gtiaColors[5] = (byte) 136;
		this.gtiaColors[6] = 14;
		decodeAtari8Gr15(content, 2, contentStride, frame, 0, width << 1, height);
		return applyAtari8Palette(frame);
	}

	private boolean decodeAtari8Koala(byte[] content, int contentOffset, int contentLength)
	{
		final XeKoalaStream rle = new XeKoalaStream();
		rle.content = content;
		rle.contentOffset = contentOffset;
		rle.contentLength = contentLength;
		if (!rle.unpackWrapped(7680))
			return false;
		setSize(320, 192, RECOILResolution.XE2X1);
		setPF0123Bak(content, contentOffset + 13);
		final byte[] frame = new byte[61440];
		decodeAtari8Gr15(rle.unpacked, 0, 40, frame, 0, 320, 192);
		return applyAtari8Palette(frame);
	}

	private boolean decodeAtari8Pix(byte[] content, int contentLength)
	{
		return contentLength >= 30 && content[0] == 27 && content[1] == 77 && 4 + (content[2] & 0xff | (content[3] & 0xff) << 8) == contentLength && decodeAtari8Koala(content, 4, contentLength);
	}

	private static final int AT800_SPRITE_GAP = 2;

	private void decodeAt800Players(byte[] content, byte[] frame)
	{
		for (int i = 0; i < 4; i++)
			decodeAtari8Player(content, 4 + i * 240, content[i] & 0xff, frame, i * 10 * 2, 240, false);
	}

	private void decodeAt800Missiles(byte[] content, int contentOffset, byte[] frame, int frameOffset)
	{
		for (int y = 0; y < 240; y++) {
			for (int i = 0; i < 4; i++) {
				int b = (content[contentOffset + y] & 0xff) >> (i << 1);
				int offset = frameOffset + i * 4 * 2;
				frame[offset + 1] = frame[offset] = (byte) ((b & 2) == 0 ? 0 : content[i] & 0xff);
				frame[offset + 3] = frame[offset + 2] = (byte) ((b & 1) == 0 ? 0 : content[i] & 0xff);
			}
			frameOffset += this.width;
		}
	}

	private boolean decodePla(byte[] content, int contentLength)
	{
		if (contentLength != 241)
			return false;
		setSize(16, 240, RECOILResolution.XE2X1);
		final byte[] frame = new byte[3840];
		decodeAtari8Player(content, 1, content[0] & 0xff, frame, 0, 240, false);
		return applyAtari8Palette(frame);
	}

	private boolean decodeMis(byte[] content, int contentLength)
	{
		if (contentLength != 61 && contentLength != 241)
			return false;
		setSize(4, 240, RECOILResolution.XE2X1);
		final byte[] frame = new byte[960];
		for (int y = 0; y < 240; y++) {
			int b = (content[1 + (y >> 2)] & 0xff) >> ((~y & 3) << 1);
			frame[y * 4 + 1] = frame[y * 4] = (byte) ((b & 2) == 0 ? 0 : content[0] & 0xff);
			frame[y * 4 + 3] = frame[y * 4 + 2] = (byte) ((b & 1) == 0 ? 0 : content[0] & 0xff);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decode4pl(byte[] content, int contentLength)
	{
		if (contentLength != 964)
			return false;
		setSize(80, 240, RECOILResolution.XE2X1);
		final byte[] frame = new byte[19200];
		decodeAt800Players(content, frame);
		return applyAtari8Palette(frame);
	}

	private boolean decode4mi(byte[] content, int contentLength)
	{
		if (contentLength != 244)
			return false;
		setSize(32, 240, RECOILResolution.XE2X1);
		final byte[] frame = new byte[7680];
		decodeAt800Missiles(content, 4, frame, 0);
		return applyAtari8Palette(frame);
	}

	private boolean decode4pm(byte[] content, int contentLength)
	{
		if (contentLength != 1204)
			return false;
		setSize(112, 240, RECOILResolution.XE2X1);
		final byte[] frame = new byte[26880];
		decodeAt800Players(content, frame);
		decodeAt800Missiles(content, 964, frame, 80);
		return applyAtari8Palette(frame);
	}

	private boolean decodeAtari8Spr(byte[] content, int contentLength)
	{
		if (contentLength < 3 || contentLength > 42)
			return false;
		int height = content[0] & 0xff;
		if (2 + height != contentLength)
			return false;
		setSize(16, height, RECOILResolution.XE2X1);
		final byte[] frame = new byte[640];
		decodeAtari8Player(content, 2, content[1] & 0xff, frame, 0, height, false);
		return applyAtari8Palette(frame);
	}

	private boolean decodeMsl(byte[] content, int contentLength)
	{
		if (contentLength < 3 || contentLength > 36)
			return false;
		int height = content[0] & 0xff;
		if (2 + height != contentLength)
			return false;
		setSize(4, height, RECOILResolution.XE2X1);
		final byte[] frame = new byte[136];
		for (int y = 0; y < height; y++) {
			int b = content[2 + y] & 0xff;
			if (b > 3)
				return false;
			frame[y * 4 + 1] = frame[y * 4] = (byte) ((b & 2) == 0 ? 0 : content[1] & 0xff);
			frame[y * 4 + 3] = frame[y * 4 + 2] = (byte) ((b & 1) == 0 ? 0 : content[1] & 0xff);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeMpl(byte[] content, int contentLength)
	{
		if (contentLength < 13)
			return false;
		int height = content[0] & 0xff;
		if (height == 0 || height > 40)
			return false;
		int bitmapOffset = contentLength - (height << 2);
		if (bitmapOffset != 9 && bitmapOffset != 14)
			return false;
		int minX = 255;
		int maxX = 0;
		for (int i = 1; i < 5; i++) {
			int x = content[i] & 0xff;
			if (minX > x)
				minX = x;
			if (maxX < x)
				maxX = x;
		}
		if (maxX + 8 > 56)
			return false;
		setSize((maxX + 8 - minX) << 1, height, RECOILResolution.XE2X1);
		final byte[] frame = new byte[4480];
		for (int i = 3; i >= 0; i--)
			decodeAtari8Player(content, bitmapOffset + i * height, content[5 + i] & 0xff, frame, ((content[1 + i] & 0xff) - minX) << 1, height, false);
		return applyAtari8Palette(frame);
	}

	private boolean decodeLdm(byte[] content, int contentLength)
	{
		if (contentLength < 281)
			return false;
		for (int i = 0; i < 21; i++)
			if ((content[i] & 0xff) != "Ludek Maker data file".charAt(i) + 128)
				return false;
		int shapes = (content[24] & 0xff) - (content[23] & 0xff);
		if (shapes <= 0 || shapes > 100 || contentLength < 281 + shapes * 120)
			return false;
		int rows = (shapes + 7) >> 3;
		if (rows == 1)
			setSize(shapes * 40, 30, RECOILResolution.XE2X1);
		else
			setSize(320, rows * 32 - 2, RECOILResolution.XE2X1);
		final byte[] frame = new byte[132480];
		for (int shape = 0; shape < shapes; shape++) {
			int contentOffset = 281 + shape * 120;
			int frameOffset = (shape >> 3) * 32 * 320 + (shape & 7) * 40;
			decodeAtari8Player(content, contentOffset, content[21] & 0xff, frame, frameOffset, 30, true);
			decodeAtari8Player(content, contentOffset + 30, content[22] & 0xff, frame, frameOffset, 30, true);
			decodeAtari8Player(content, contentOffset + 60, content[21] & 0xff, frame, frameOffset + 16, 30, true);
			decodeAtari8Player(content, contentOffset + 90, content[22] & 0xff, frame, frameOffset + 16, 30, true);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodePmd(byte[] content, int contentLength)
	{
		if (contentLength < 12 || content[0] != -16 || content[1] != -19 || content[2] != -28)
			return false;
		int sprites = content[7] & 0xff;
		int shapes = (content[8] & 0xff) * (content[9] & 0xff);
		int totalShapes = sprites * shapes;
		int height = content[10] & 0xff;
		if (sprites == 0 || sprites > 4 || shapes == 0 || shapes > 160 || height == 0 || height > 48 || 11 + totalShapes * height != contentLength)
			return false;
		if (true)
			totalShapes >>= 1;
		int rows = (totalShapes + 15) >> 4;
		if (rows == 1)
			setSize(totalShapes * 20, height, RECOILResolution.XE2X1);
		else {
			int totalHeight = rows * (height + 2) - 2;
			if (totalHeight > 560)
				return false;
			setSize(320, totalHeight, RECOILResolution.XE2X1);
		}
		final byte[] frame = new byte[179200];
		for (int shape = 0; shape < totalShapes; shape++) {
			int frameOffset = (shape >> 4) * (height + 2) * 320 + (shape & 15) * 20;
			if (true) {
				int spritePair = shape / shapes;
				int contentOffset = 11 + (spritePair * shapes + shape) * height;
				decodeAtari8Player(content, contentOffset, content[3 + spritePair * 2] & 0xff, frame, frameOffset, height, true);
				decodeAtari8Player(content, contentOffset + shapes * height, content[4 + spritePair * 2] & 0xff, frame, frameOffset, height, true);
			}
			else
				decodeAtari8Player(content, 11 + shape * height, content[3 + shape / shapes] & 0xff, frame, frameOffset, height, false);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeApl(byte[] content, int contentLength)
	{
		if (contentLength != 1677 || content[0] != -102 || content[1] != -8 || content[2] != 57 || content[3] != 33)
			return false;
		int frames = content[4] & 0xff;
		int height = content[5] & 0xff;
		int gap = content[6] & 0xff;
		if (frames == 0 || frames > 16 || height == 0 || height > 48 || gap > 8)
			return false;
		int frameWidth = (8 + gap + 2) << 1;
		setSize(frames * frameWidth, height, RECOILResolution.XE2X1);
		final byte[] frame = new byte[27648];
		for (int f = 0; f < frames; f++) {
			decodeAtari8Player(content, 42 + f * 48, content[7 + f] & 0xff, frame, f * frameWidth, height, true);
			decodeAtari8Player(content, 858 + f * 48, content[24 + f] & 0xff, frame, f * frameWidth + gap * 2, height, true);
		}
		return applyAtari8Palette(frame);
	}

	private static boolean getSprEdPixel(byte[] content, int bitmapOffset, int bitmapStride, int x)
	{
		if (x < 0 || x >= 10)
			return false;
		int grafp = (content[bitmapOffset] & 0xff) << 2;
		if ((content[10] & 1) != 0)
			grafp |= content[bitmapOffset + 4 * bitmapStride] & 3;
		return (grafp >> (9 - x) & 1) != 0;
	}

	private static int getSprEdPair(byte[] content, int bitmapOffset, int bitmapStride, int colorOffset, int colorStride, int x, int gap01)
	{
		boolean p1 = getSprEdPixel(content, bitmapOffset + bitmapStride, bitmapStride, x - gap01);
		if (getSprEdPixel(content, bitmapOffset, bitmapStride, x)) {
			int c = content[colorOffset] & 0xff;
			if (p1)
				c |= content[colorOffset + colorStride] & 0xff;
			return c;
		}
		if (p1)
			return content[colorOffset + colorStride] & 0xff;
		return -1;
	}

	private boolean decodeSprEd(byte[] content, int contentLength)
	{
		if (contentLength < 23 || !isStringAt(content, 0, "Spr!"))
			return false;
		int doubleLine = content[9] & 0xff;
		int mergeMode = content[10] & 0xff;
		int gap01;
		int gap23 = content[12] & 0xff;
		int gap02 = content[13] & 0xff;
		boolean dli = (content[14] & 1) != 0;
		int frames = content[16] & 0xff;
		int height;
		if (frames == 0) {
			gap01 = content[11] & 0xff;
			frames = content[17] & 0xff;
			height = content[18] & 0xff;
		}
		else {
			height = content[17] & 0xff;
			gap01 = content[18] & 0xff;
		}
		int unitWidth = 8 + ((mergeMode & 1) << 1);
		int players = (mergeMode & 4) != 0 ? 4 : 2;
		int dliOffset = 19 + players * frames * (1 + (height << (mergeMode & 1)));
		int bitmapStride = frames * height;
		if (doubleLine > 1 || gap01 > unitWidth || gap23 > unitWidth || gap02 > unitWidth << 1 || contentLength < (dli ? dliOffset + 5 * bitmapStride : dliOffset))
			return false;
		int frameWidth = unitWidth + gap01;
		if (players == 4) {
			int width23 = gap02 + unitWidth + gap23;
			if (frameWidth < width23)
				frameWidth = width23;
		}
		frameWidth += 2;
		int width = frames * frameWidth - 2;
		if (!setSize(width << 1, height << doubleLine, doubleLine == 0 ? RECOILResolution.XE2X1 : RECOILResolution.XE2X2))
			return false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int frame = x / frameWidth;
				int h = x % frameWidth;
				int colorOffset = 19 + frame;
				int colorStride = frames;
				if (dli) {
					colorOffset = dliOffset + bitmapStride + frame * height + y;
					colorStride = bitmapStride;
				}
				int bitmapOffset = 19 + players * frames + frame * height + y;
				int c = getSprEdPair(content, bitmapOffset, bitmapStride, colorOffset, colorStride, h, gap01);
				if (c < 0 && players == 4)
					c = getSprEdPair(content, bitmapOffset + 2 * bitmapStride, bitmapStride, colorOffset + 2 * colorStride, colorStride, h - gap02, gap23);
				if (c < 0)
					c = content[dli ? colorOffset - bitmapStride : 6] & 0xff;
				c = this.atari8Palette[c & 254];
				int pixelsOffset = ((y * width << doubleLine) + x) << 1;
				this.pixels[pixelsOffset + 1] = this.pixels[pixelsOffset] = c;
				if (doubleLine != 0)
					this.pixels[pixelsOffset + (width << 1) + 1] = this.pixels[pixelsOffset + (width << 1)] = c;
			}
		}
		return true;
	}

	private boolean decodeAtari8Hr(byte[] content, int contentLength)
	{
		if (contentLength != 16384)
			return false;
		setSize(256, 239, RECOILResolution.XE1X1);
		this.gtiaColors[6] = 0;
		this.gtiaColors[5] = 14;
		final byte[] frame1 = new byte[61184];
		decodeAtari8Gr8(content, 0, frame1, 0, 239);
		final byte[] frame2 = new byte[61184];
		decodeAtari8Gr8(content, 8192, frame2, 0, 239);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeMcppVariable(byte[] content, int bitmapOffset, int colorsOffset, int width, int height)
	{
		if (!setSize(width, height, RECOILResolution.XE2X2))
			return false;
		byte[] frame = new byte[width * height];
		setPF012Bak(content, colorsOffset);
		decodeAtari8Gr15(content, bitmapOffset, width >> 3, frame, 0, width << 1, height >> 1);
		setPF012Bak(content, colorsOffset + 4);
		decodeAtari8Gr15(content, bitmapOffset + (width * height >> 4), width >> 3, frame, width, width << 1, height >> 1);
		applyAtari8Palette(frame);
		return true;
	}

	private boolean decodeMcpp(byte[] content, int contentLength)
	{
		return contentLength == 8008 && decodeMcppVariable(content, 0, 8000, 320, 200);
	}

	private boolean decodeIld(byte[] content, int contentLength)
	{
		if (contentLength != 8195)
			return false;
		setSize(256, 128, RECOILResolution.XE2X1);
		this.gtiaColors[8] = 0;
		this.gtiaColors[4] = 6;
		this.gtiaColors[5] = 2;
		this.gtiaColors[6] = 10;
		final byte[] frame1 = new byte[32768];
		decodeAtari8Gr15(content, 0, 32, frame1, 0, 256, 128);
		final byte[] frame2 = new byte[32768];
		decodeAtari8Gr15(content, 4096, 32, frame2, 0, 256, 128);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeInp(byte[] content, int contentLength)
	{
		if (contentLength < 16004)
			return false;
		setSize(320, 200, RECOILResolution.XE2X1);
		setBakPF012(content, 16000, 1);
		final byte[] frame1 = new byte[64000];
		decodeAtari8Gr15(content, 0, 40, frame1, 0, 320, 200);
		final byte[] frame2 = new byte[64000];
		decodeAtari8Gr15(content, 8000, 40, frame2, 0, 320, 200);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeIge(byte[] content, int contentLength)
	{
		if (contentLength != 6160 || content[0] != -1 || content[1] != -1 || content[2] != -10 || content[3] != -93 || content[4] != -1 || content[5] != -69 || content[6] != -1 || content[7] != 95)
			return false;
		setSize(256, 96, RECOILResolution.XE2X1);
		final byte[] frame1 = new byte[24576];
		setBakPF012(content, 8, 1);
		decodeAtari8Gr15(content, 16, 32, frame1, 0, 256, 96);
		final byte[] frame2 = new byte[24576];
		setBakPF012(content, 12, 1);
		decodeAtari8Gr15(content, 3088, 32, frame2, 0, 256, 96);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeInt(byte[] content, int contentLength)
	{
		if (contentLength < 18 || !isStringAt(content, 0, "INT95a") || content[8] != 15 || content[9] != 43)
			return false;
		int contentStride = content[6] & 0xff;
		int height = content[7] & 0xff;
		if (contentStride == 0 || contentStride > 320 || height == 0 || height > 239 || 18 + contentStride * height * 2 != contentLength)
			return false;
		int width = contentStride << 3;
		setSize(width, height, RECOILResolution.XE2X1);
		final byte[] frame1 = new byte[76480];
		setBakPF012(content, 10, 1);
		decodeAtari8Gr15(content, 18, contentStride, frame1, 0, width, height);
		final byte[] frame2 = new byte[76480];
		setBakPF012(content, 14, 1);
		decodeAtari8Gr15(content, 18 + contentStride * height, contentStride, frame2, 0, width, height);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeIst(byte[] content, int contentLength)
	{
		if (contentLength != 17184)
			return false;
		setSize(320, 200, RECOILResolution.XE2X1);
		final byte[] frame1 = new byte[64000];
		final byte[] frame2 = new byte[64000];
		for (int y = 0; y < 200; y++) {
			setBakPF012(content, 16384 + y, 200);
			decodeAtari8Gr15(content, 16 + y * 40, 0, frame1, y * 320, 320, 1);
			decodeAtari8Gr15(content, 8208 + y * 40, 0, frame2, y * 320, 320, 1);
		}
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeGr15Blend(byte[] content, int bitmapOffset, int colorsOffset, int height)
	{
		setSize(320, height, RECOILResolution.XE2X1);
		final byte[] frame1 = new byte[64000];
		setPF012Bak(content, colorsOffset);
		decodeAtari8Gr15(content, bitmapOffset, 80, frame1, 0, 640, height >> 1);
		setPF012Bak(content, colorsOffset + 4);
		decodeAtari8Gr15(content, bitmapOffset + 40, 80, frame1, 320, 640, height >> 1);
		final byte[] frame2 = new byte[64000];
		decodeAtari8Gr15(content, bitmapOffset + height * 40, 80, frame2, 0, 640, height >> 1);
		setPF012Bak(content, colorsOffset);
		decodeAtari8Gr15(content, bitmapOffset + height * 40 + 40, 80, frame2, 320, 640, height >> 1);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeMcp(byte[] content, int contentLength)
	{
		return contentLength == 16008 && decodeGr15Blend(content, 0, 16000, 200);
	}

	private boolean decodeAtari8Raw(byte[] content, int contentLength)
	{
		return contentLength == 15372 && isStringAt(content, 0, "XLPB") && decodeGr15Blend(content, 4, 15364, 192);
	}

	private boolean decodeXlp(byte[] content, int contentLength)
	{
		final XlpStream rle = new XlpStream();
		rle.content = content;
		rle.contentLength = contentLength;
		final byte[] unpacked = new byte[16000];
		int height;
		int colorsOffset;
		if (contentLength >= 10 && isStringAt(content, 0, "XLPC")) {
			Arrays.fill(unpacked, (byte) 0);
			rle.contentOffset = 8;
			rle.unpackColumns(unpacked, 0, 40, 15360);
			height = 192;
			colorsOffset = 4;
		}
		else {
			rle.contentOffset = 4;
			if (rle.unpackColumns(unpacked, 0, 40, 16000))
				height = 200;
			else {
				rle.contentOffset = 4;
				if (rle.unpackColumns(unpacked, 0, 40, 15360))
					height = 192;
				else
					return false;
			}
			colorsOffset = 0;
		}
		setSize(320, height, RECOILResolution.XE2X1);
		setPF012Bak(content, colorsOffset);
		final byte[] frame1 = new byte[64000];
		decodeAtari8Gr15(unpacked, 0, 40, frame1, 0, 320, height);
		final byte[] frame2 = new byte[64000];
		decodeAtari8Gr15(unpacked, height * 40, 40, frame2, 0, 320, height);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeAtari8Max(byte[] content, int contentLength)
	{
		if (contentLength < 1732 || !isStringAt(content, 0, "XLPM"))
			return false;
		final XlpStream rle = new XlpStream();
		rle.content = content;
		rle.contentOffset = 1732;
		rle.contentLength = contentLength;
		final byte[] unpacked = new byte[15360];
		if (!rle.unpackColumns(unpacked, 0, 40, 15360))
			return false;
		setSize(320, 192, RECOILResolution.XE2X1);
		final byte[] frame1 = new byte[61440];
		final byte[] frame2 = new byte[61440];
		for (int y = 0; y < 192; y++) {
			setBakPF012(content, 772 + y, 192);
			decodeAtari8Gr15(unpacked, y * 40, 40, frame1, y * 320, 320, 1);
			setBakPF012(content, 4 + y, 192);
			decodeAtari8Gr15(unpacked, 7680 + y * 40, 40, frame2, y * 320, 320, 1);
		}
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeHr2(byte[] content, int contentLength)
	{
		if (contentLength != 16006)
			return false;
		setSize(320, 200, RECOILResolution.XE1X1);
		final byte[] frame1 = new byte[64000];
		setPF21(content, 16000);
		decodeAtari8Gr8(content, 0, frame1, 0, 200);
		final byte[] frame2 = new byte[64000];
		setBakPF012(content, 16002, 1);
		decodeAtari8Gr15(content, 8000, 40, frame2, 0, 320, 200);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeLum(String filename, byte[] content, int contentLength)
	{
		if (contentLength != 4766)
			return false;
		setSize(320, 238, RECOILResolution.XE4X2);
		final byte[] frame = new byte[76160];
		this.gtiaColors[8] = 0;
		decodeAtari8Gr9(content, 6, 40, frame, 320, 640, 320, 119);
		final byte[] col = new byte[4767];
		if (readCompanionFile(filename, "COL", "col", col, 4767) == 4766) {
			decodeAtari8Gr11PalBlend(col, 6, 40, frame, 0);
		}
		else {
			decodeAtari8Gr9(content, 6, 40, frame, 0, 640, 320, 119);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeApc(byte[] content, int contentLength)
	{
		if (contentLength != 7680 && contentLength != 7720)
			return false;
		setSize(320, 192, RECOILResolution.XE4X2);
		final byte[] frame = new byte[61440];
		this.gtiaColors[8] = 0;
		decodeAtari8Gr9(content, 40, 80, frame, 320, 640, 320, 96);
		decodeAtari8Gr11PalBlend(content, 0, 80, frame, 0);
		return applyAtari8Palette(frame);
	}

	private boolean decode256(byte[] content, int contentLength)
	{
		if (contentLength != 7680 && contentLength != 7684)
			return false;
		setSize(320, 192, RECOILResolution.XE4X2);
		final byte[] frame = new byte[61440];
		this.gtiaColors[8] = 0;
		decodeAtari8Gr9(content, 3840, 40, frame, 320, 640, 320, 96);
		decodeAtari8Gr11PalBlend(content, 0, 40, frame, 0);
		return applyAtari8Palette(frame);
	}

	private boolean decodeMga(byte[] content, int contentLength)
	{
		if (contentLength != 7856)
			return false;
		setSize(320, 192, RECOILResolution.XE4X2);
		final byte[] frame = new byte[61440];
		this.gtiaColors[8] = 0;
		decodeAtari8Gr9(content, 0, 80, frame, 320, 640, 320, 96);
		decodeAtari8Gr11PalBlend(content, 40, 80, frame, 0);
		return applyAtari8Palette(frame);
	}

	private boolean decodeAp3(byte[] content, int contentLength)
	{
		int gr11Offset;
		switch (contentLength) {
		case 15360:
		case 15362:
			gr11Offset = 7680;
			break;
		case 15872:
			gr11Offset = 8192;
			break;
		default:
			return false;
		}
		setSize(320, 192, RECOILResolution.XE4X1);
		this.gtiaColors[8] = 0;
		final byte[] frame1 = new byte[61440];
		decodeAtari8Gr9(content, 0, 80, frame1, 0, 640, 320, 96);
		decodeAtari8Gr11PalBlend(content, gr11Offset + 40, 80, frame1, 1);
		final byte[] frame2 = new byte[61440];
		decodeAtari8Gr9(content, 40, 80, frame2, 320, 640, 320, 96);
		decodeAtari8Gr11PalBlend(content, gr11Offset, 80, frame2, 0);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeBgp(byte[] content, int contentLength)
	{
		if (contentLength < 19163 || !isStringAt(content, 0, "BUGBITER_APAC239I_PICTURE_V1.0") || content[30] != -1 || content[31] != 80 || content[32] != -17)
			return false;
		int textLength = (content[37] & 0xff) + ((content[38] & 0xff) << 8);
		if (contentLength != 19163 + textLength || content[39 + textLength] != 88 || content[40 + textLength] != 37 || content[9601 + textLength] != 88 || content[9602 + textLength] != 37)
			return false;
		setSize(320, 239, RECOILResolution.XE4X1);
		this.gtiaColors[8] = 0;
		final byte[] frame1 = new byte[76480];
		decodeAtari8Gr9(content, 41 + textLength, 80, frame1, 0, 640, 320, 120);
		decodeAtari8Gr11PalBlend(content, 9643 + textLength, 80, frame1, 1);
		final byte[] frame2 = new byte[76480];
		decodeAtari8Gr9(content, 81 + textLength, 80, frame2, 320, 640, 320, 119);
		decodeAtari8Gr11PalBlend(content, 9603 + textLength, 80, frame2, 0);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeHip(byte[] content, int contentLength)
	{
		if (contentLength < 80)
			return false;
		final byte[] frame1 = new byte[76800];
		final byte[] frame2 = new byte[76800];
		int frameLength = parseAtari8ExecutableHeader(content, 0);
		if (frameLength > 0 && frameLength % 40 == 0 && 12 + frameLength * 2 == contentLength && parseAtari8ExecutableHeader(content, 6 + frameLength) == frameLength) {
			int height = frameLength / 40;
			if (height > 240)
				return false;
			setSize(320, height, RECOILResolution.XE2X1);
			this.leftSkip = 1;
			setGtiaColors(DECODE_HIP_GR10_COLORS, 0);
			decodeAtari8Gr10(content, 6, frame1, 0, 320, height);
			this.gtiaColors[8] = 0;
			decodeAtari8Gr9(content, 12 + frameLength, 40, frame2, 0, 320, 320, height);
		}
		else {
			int height = contentLength / 80;
			if (height > 240)
				return false;
			setSize(320, height, RECOILResolution.XE2X1);
			this.leftSkip = 1;
			this.gtiaColors[8] = 0;
			decodeAtari8Gr9(content, 0, 40, frame1, 0, 320, 320, height);
			if (contentLength % 80 == 9)
				setGtiaColors(content, contentLength - 9);
			else
				setGtiaColors(DECODE_HIP_GR10_COLORS, 0);
			decodeAtari8Gr10(content, height * 40, frame2, 0, 320, height);
		}
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeG9s(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[7680];
		final SfdnStream s = new SfdnStream();
		s.content = content;
		s.contentLength = contentLength;
		return s.unpack(unpacked, 7680) && decodeGr9(unpacked, 7680);
	}

	private boolean decodeIns(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[16004];
		final SfdnStream s = new SfdnStream();
		s.content = content;
		s.contentLength = contentLength;
		return s.unpack(unpacked, 16004) && decodeInp(unpacked, 16004);
	}

	private boolean decodePls(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[7680];
		final SfdnStream s = new SfdnStream();
		s.content = content;
		s.contentLength = contentLength;
		return s.unpack(unpacked, 7680) && decodeApc(unpacked, 7680);
	}

	private boolean decodeAps(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[7720];
		final SfdnStream s = new SfdnStream();
		s.content = content;
		s.contentLength = contentLength;
		return s.unpack(unpacked, 7720) && decodeApc(unpacked, 7720);
	}

	private boolean decodeIls(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[15360];
		final SfdnStream s = new SfdnStream();
		s.content = content;
		s.contentLength = contentLength;
		return s.unpack(unpacked, 15360) && decodeAp3(unpacked, 15360);
	}

	private boolean decodeApp(byte[] content, int contentLength)
	{
		final byte[] unpacked = new byte[15872];
		final SfdnStream s = new SfdnStream();
		s.content = content;
		s.contentLength = contentLength;
		return s.unpack(unpacked, 15872) && decodeAp3(unpacked, 15872);
	}

	private boolean decodeHps(byte[] content, int contentLength)
	{
		byte[] unpacked = new byte[16009];
		final SfdnStream s = new SfdnStream();
		s.content = content;
		s.contentLength = contentLength;
		return s.unpack(unpacked, 16009) && decodeHip(unpacked, 16009);
	}

	private boolean decodeTip(byte[] content, int contentLength)
	{
		if (contentLength < 129 || content[0] != 84 || content[1] != 73 || content[2] != 80 || content[3] != 1 || content[4] != 0)
			return false;
		int width = content[5] & 0xff;
		int height = content[6] & 0xff;
		if (width > 160 || (width & 3) != 0 || height > 119)
			return false;
		int contentStride = width >> 2;
		int frameLength = content[7] & 0xff | (content[8] & 0xff) << 8;
		if (frameLength != contentStride * height || contentLength != 9 + 3 * frameLength)
			return false;
		setSize(width << 1, height << 1, RECOILResolution.XE2X2);
		this.leftSkip = 1;
		setGtiaColors(DECODE_TIP_COLORS, 0);
		byte[] frame1 = new byte[76160];
		decodeAtari8Gr9(content, 9, contentStride, frame1, width << 1, width << 2, width << 1, height);
		decodeAtari8Gr11PalBlend(content, 9 + 2 * frameLength, contentStride, frame1, 0);
		byte[] frame2 = new byte[76160];
		decodeAtari8Gr10(content, 9 + frameLength, frame2, width << 1, width << 2, height);
		decodeAtari8Gr11PalBlend(content, 9 + 2 * frameLength, contentStride, frame2, 0);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeCin(byte[] content, int contentLength)
	{
		int height;
		switch (contentLength) {
		case 15360:
			setGr15DefaultColors();
			height = 192;
			break;
		case 16004:
			setBakPF012(content, 16000, 1);
			height = 200;
			break;
		case 16384:
			height = 192;
			break;
		default:
			return false;
		}
		setSize(320, height, RECOILResolution.XE2X1);
		byte[] frame1 = new byte[64000];
		byte[] frame2 = new byte[64000];
		for (int y = 0; y < height; y++) {
			if (contentLength == 16384)
				setBakPF012(content, 15360 + y, 256);
			decodeAtari8Gr15(content, y * 40, 40, (y & 1) == 0 ? frame1 : frame2, y * 320, 320, 1);
		}
		decodeAtari8Gr11PalBlend(content, 40 * height + 40, 80, frame1, 1);
		decodeAtari8Gr11PalBlend(content, 40 * height, 80, frame2, 0);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeCci(byte[] content, int contentLength)
	{
		if (contentLength < 24 || !isStringAt(content, 0, "CIN 1.2 "))
			return false;
		final CciStream rle = new CciStream();
		rle.content = content;
		rle.contentOffset = 8;
		rle.contentLength = contentLength;
		final byte[] unpacked = new byte[16384];
		if (!rle.unpackGr15(unpacked, 0) || !rle.unpackGr15(unpacked, 40))
			return false;
		rle.contentOffset += 4;
		rle.repeatCount = 0;
		if (!rle.unpackColumns(unpacked, 7680, 40, 15360))
			return false;
		rle.contentOffset += 4;
		rle.repeatCount = 0;
		return rle.unpack(unpacked, 15360, 1, 16384) && decodeCin(unpacked, 16384);
	}

	private boolean decodeAgs(byte[] content, int contentLength)
	{
		if (contentLength < 17 || content[0] != 65 || content[1] != 71 || content[2] != 83)
			return false;
		int width = content[4] & 0xff;
		int height = content[5] & 0xff | (content[6] & 0xff) << 8;
		if (contentLength != 16 + (width * height << 1))
			return false;
		switch (content[3]) {
		case 11:
			return decodeMcppVariable(content, 16, 7, width << 3, height << 1);
		case 19:
			return decodeGr9x4(content, 16, width << 3, height << 2);
		default:
			return false;
		}
	}

	private boolean unpackRip(byte[] content, int contentOffset, int contentLength, byte[] unpacked, int unpackedLength)
	{
		if (contentOffset + 304 > contentLength || !isStringAt(content, contentOffset, "PCK"))
			return false;
		final FanoTree lengthTree = new FanoTree();
		lengthTree.create(content, contentOffset + 16, 64);
		final FanoTree distanceTree = new FanoTree();
		distanceTree.create(content, contentOffset + 16 + 32, 256);
		final FanoTree literalTree = new FanoTree();
		literalTree.create(content, contentOffset + 16 + 32 + 128, 256);
		final BitStream bitStream = new BitStream();
		bitStream.content = content;
		bitStream.contentOffset = contentOffset + 16 + 288;
		bitStream.contentLength = contentLength;
		for (int unpackedOffset = 0; unpackedOffset < unpackedLength;) {
			switch (bitStream.readBit()) {
			case 0:
				int literal = literalTree.readCode(bitStream);
				if (literal < 0)
					return false;
				unpacked[unpackedOffset++] = (byte) literal;
				break;
			case 1:
				int distance = distanceTree.readCode(bitStream);
				if (distance < 0)
					return false;
				distance += 2;
				int count = lengthTree.readCode(bitStream);
				if (count < 0)
					return false;
				count += 2;
				if (count > unpackedLength - unpackedOffset)
					count = unpackedLength - unpackedOffset;
				if (!copyPrevious(unpacked, unpackedOffset, distance, count))
					return false;
				unpackedOffset += count;
				if (unpackedOffset >= unpackedLength)
					return true;
				break;
			default:
				return false;
			}
		}
		return true;
	}

	private boolean decodeRip(byte[] content, int contentLength)
	{
		if (contentLength < 34 || content[0] != 82 || content[1] != 73 || content[2] != 80 || content[18] != 84 || content[19] != 58)
			return false;
		int headerLength = content[11] & 0xff | (content[12] & 0xff) << 8;
		int contentStride = content[13] & 0xff;
		int height = content[15] & 0xff;
		int textLength = content[17] & 0xff;
		if (headerLength >= contentLength || contentStride == 0 || contentStride > 80 || (contentStride & 1) != 0 || height == 0 || height > 239 || 33 + textLength >= contentLength || content[20 + textLength] != 9 || !isStringAt(content, 21 + textLength, "CM:"))
			return false;
		if ((content[7] & 0xff) < 16)
			contentStride >>= 1;
		int unpackedLength = contentStride * height;
		if (content[7] == 48)
			unpackedLength += (height + 1) >> 1 << 3;
		final byte[] unpacked = new byte[20076];
		switch (content[9]) {
		case 0:
			if (headerLength + unpackedLength > contentLength)
				return false;
			System.arraycopy(content, headerLength, unpacked, 0, unpackedLength);
			break;
		case 1:
			unpackRip(content, headerLength, contentLength, unpacked, unpackedLength);
			break;
		default:
			return false;
		}
		setGtiaColors(content, 24 + textLength);
		contentStride = (content[13] & 0xff) >> 1;
		int width = contentStride << 3;
		final byte[] frame1 = new byte[76480];
		final byte[] frame2 = new byte[76480];
		switch (content[7]) {
		case 14:
			setSize(width, height, RECOILResolution.XE2X1);
			decodeAtari8Gr15(unpacked, 0, contentStride, frame1, 0, width, height);
			return applyAtari8Palette(frame1);
		case 15:
			setSize(width, height, RECOILResolution.XE1X1);
			decodeAtari8Gr8(unpacked, 0, frame1, 0, height);
			return applyAtari8Palette(frame1);
		case 79:
			setSize(width, height, RECOILResolution.XE4X1);
			decodeAtari8Gr9(unpacked, 0, contentStride, frame1, 0, width, width, height);
			return applyAtari8Palette(frame1);
		case -113:
			setSize(width, height, RECOILResolution.XE4X1);
			this.leftSkip = 2;
			decodeAtari8Gr10(unpacked, 0, frame1, 0, width, height);
			return applyAtari8Palette(frame1);
		case -49:
			setSize(width, height, RECOILResolution.XE4X1);
			decodeAtari8Gr11(content, 0, frame1, 0, width, height);
			return applyAtari8Palette(frame1);
		case 30:
			setSize(width, height, RECOILResolution.XE2X1);
			decodeAtari8Gr15(unpacked, 0, contentStride, frame1, 0, width, height);
			decodeAtari8Gr15(unpacked, height * contentStride, contentStride, frame2, 0, width, height);
			return applyAtari8PaletteBlend(frame1, frame2);
		case 16:
			setSize(width, height, RECOILResolution.XE2X1);
			setBakPF012(content, 28 + textLength, 1);
			decodeAtari8Gr15(unpacked, 0, contentStride << 1, frame1, 0, width << 1, height >> 1);
			setBakPF012(content, 24 + textLength, 1);
			decodeAtari8Gr15(unpacked, contentStride, contentStride << 1, frame1, width, width << 1, height >> 1);
			decodeAtari8Gr15(unpacked, height * contentStride, contentStride << 1, frame2, 0, width << 1, height >> 1);
			setBakPF012(content, 28 + textLength, 1);
			decodeAtari8Gr15(unpacked, (height + 1) * contentStride, contentStride << 1, frame2, width, width << 1, height >> 1);
			return applyAtari8PaletteBlend(frame1, frame2);
		case 32:
			setSize(width, height, RECOILResolution.XE2X1);
			this.leftSkip = 1;
			decodeAtari8Gr10(unpacked, 0, frame1, 0, width, height);
			this.gtiaColors[8] = 0;
			decodeAtari8Gr9(unpacked, height * contentStride, contentStride, frame2, 0, width, width, height);
			return applyAtari8PaletteBlend(frame1, frame2);
		case 48:
			setSize(width, height, RECOILResolution.XE2X1);
			this.leftSkip = 1;
			this.gtiaColors[0] = 0;
			int colorsOffset = height * contentStride << 1;
			for (int y = 0; y < height; y += 2) {
				setPM123PF0123Bak(unpacked, colorsOffset + (y << 2));
				decodeAtari8Gr10(unpacked, y * contentStride, frame1, y * width, width, y + 1 < height ? 2 : 1);
			}
			this.gtiaColors[8] = 0;
			decodeAtari8Gr9(unpacked, height * contentStride, contentStride, frame2, 0, width, width, height);
			return applyAtari8PaletteBlend(frame1, frame2);
		default:
			return false;
		}
	}

	private boolean decodeVzi(byte[] content, int contentLength)
	{
		if (contentLength != 16000)
			return false;
		setSize(320, 200, RECOILResolution.XE2X1);
		final byte[] frame1 = new byte[64000];
		this.leftSkip = -1;
		decodeAtari8Gr9(content, 0, 40, frame1, 0, 320, 320, 200);
		this.leftSkip = 1;
		final byte[] frame2 = new byte[64000];
		decodeAtari8Gr9(content, 8000, 40, frame2, 0, 320, 320, 200);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeRmUnpacked(byte[] content, int colorsOffset, int dliOffset, byte[] bitmap, int mode, int resolution)
	{
		final boolean[] dliPresent = new boolean[192];
		for (int i = 0; i < 128; i++) {
			int y = content[dliOffset + i] & 0xff;
			switch (y) {
			case 0:
				break;
			case 1:
			case 2:
			case 4:
			case 5:
				return false;
			default:
				if (mode == 0) {
					if (y >= 101)
						return false;
					if (y == 3)
						y = 0;
					else
						y -= 5;
				}
				else {
					if (y == 100 || y == 101 || y >= 198)
						return false;
					if (y == 3)
						y = 1;
					else if (y < 100)
						y -= 4;
					else
						y -= 6;
				}
				dliPresent[y] = true;
				break;
			}
		}
		setSize(320, 192, resolution);
		if (mode == 2)
			this.leftSkip = 2;
		if (mode == 1)
			this.gtiaColors[8] = (byte) (content[colorsOffset + 8] & 240);
		else
			setGtiaColors(content, colorsOffset);
		int height = mode == 0 ? 96 : 192;
		final byte[] frame = new byte[61440];
		for (int y = 0; y < height; y++) {
			switch (mode) {
			case 0:
				decodeAtari8Gr7(bitmap, y * 40, frame, y * 640, 1);
				break;
			case 1:
				decodeAtari8Gr9(bitmap, y * 40, 40, frame, y * 320, 320, 320, 1);
				break;
			case 2:
				decodeAtari8Gr10(bitmap, y * 40, frame, y * 320, 320, 1);
				break;
			case 3:
				decodeAtari8Gr11(bitmap, y * 40, frame, y * 320, 320, 1);
				break;
			case 4:
				decodeAtari8Gr15(bitmap, y * 40, 40, frame, y * 320, 320, 1);
				break;
			default:
				throw new AssertionError();
			}
			if (dliPresent[y]) {
				int vcount = mode == 0 ? 16 + y : 16 + ((y - 1) >> 1);
				int reg = content[dliOffset + 128 + vcount] & 0xff;
				if (reg < 9)
					setGtiaColor(reg, content[dliOffset + 256 + vcount] & 0xff);
				else if (reg != 128)
					return false;
			}
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeRm(byte[] content, int contentLength, int mode, int resolution)
	{
		final XeKoalaStream rle = new XeKoalaStream();
		rle.content = content;
		rle.contentOffset = 0;
		rle.contentLength = contentLength - 464;
		if (rle.unpackWrapped(mode == 0 ? 3840 : 7680)) {
			return decodeRmUnpacked(content, contentLength - 464, contentLength - 384, rle.unpacked, mode, resolution);
		}
		else if (contentLength == 8192) {
			return decodeRmUnpacked(content, 7680, 7808, content, mode, resolution);
		}
		return false;
	}

	private boolean decodeAgp(byte[] content, int contentLength)
	{
		if (contentLength != 7690)
			return false;
		setGtiaColors(content, 1);
		final byte[] frame = new byte[61440];
		switch (content[0]) {
		case 8:
			setSize(320, 192, RECOILResolution.XE1X1);
			decodeAtari8Gr8(content, 10, frame, 0, 192);
			break;
		case 9:
			setSize(320, 192, RECOILResolution.XE4X1);
			decodeAtari8Gr9(content, 10, 40, frame, 0, 320, 320, 192);
			break;
		case 10:
			setSize(320, 192, RECOILResolution.XE4X1);
			this.leftSkip = 2;
			decodeAtari8Gr10(content, 10, frame, 0, 320, 192);
			break;
		case 11:
			setSize(320, 192, RECOILResolution.XE4X1);
			decodeAtari8Gr11(content, 10, frame, 0, 320, 192);
			break;
		case 15:
			setSize(320, 192, RECOILResolution.XE2X1);
			decodeAtari8Gr15(content, 10, 40, frame, 0, 320, 192);
			break;
		default:
			return false;
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeShc(byte[] content, int contentLength)
	{
		if (contentLength != 17920)
			return false;
		setSize(320, 192, RECOILResolution.XE1X1);
		final byte[] frame1 = new byte[61440];
		final byte[] frame2 = new byte[61440];
		int col1 = 15360;
		int col2 = 16640;
		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 320; x++) {
				int i = 320 * y + x;
				int bit = ~x & 7;
				switch (x) {
				case 94:
				case 166:
				case 214:
				case 262:
				case 306:
					col1++;
					break;
				case 46:
				case 142:
				case 190:
				case 238:
				case 286:
					col2++;
					break;
				default:
					break;
				}
				frame1[i] = (byte) (content[col1] & 0xff & (((content[i >> 3] & 0xff) >> bit & 1) != 0 ? 240 : 254));
				frame2[i] = (byte) (content[col2] & 0xff & (((content[7680 + (i >> 3)] & 0xff) >> bit & 1) != 0 ? 240 : 254));
			}
			col1++;
			col2++;
		}
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeMgp(byte[] content, int contentLength)
	{
		if (contentLength != 3845)
			return false;
		setSize(320, 192, RECOILResolution.XE2X2);
		final byte[] frame = new byte[61440];
		setPF0123Bak(content, 0);
		int rainbow = content[5] & 0xff;
		final byte[] bitmap = new byte[3840];
		System.arraycopy(content, 6, bitmap, 0, 3839);
		bitmap[3839] = 0;
		for (int y = 0; y < 96; y++) {
			if (rainbow < 4) {
				this.gtiaColors[rainbow == 0 ? 8 : 3 + rainbow] = (byte) ((16 + y) & 254);
			}
			decodeAtari8Gr7(bitmap, y * 40, frame, y * 640, 1);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeGad(byte[] content, int contentLength)
	{
		if (contentLength != 4325)
			return false;
		setSize(320, 192, RECOILResolution.XE2X2);
		final byte[] frame = new byte[61440];
		setPF0123Bak(content, 0);
		for (int y = 0; y < 96; y++) {
			decodeAtari8Gr7(content, 5 + y * 40, frame, y * 640, 1);
			if ((content[3845 + y] & 0xff) < 128) {
				for (int i = 0; i < 4; i++)
					this.gtiaColors[i == 3 ? 8 : 4 + i] = (byte) (content[3941 + i * 96 + y] & 254);
			}
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeFwa(byte[] content, int contentLength)
	{
		if (contentLength < 7960 || content[0] != -2 || content[1] != -2 || content[6] != 112 || content[7] != 112 || content[8] != 112 || content[11] != 80 || content[115] != 96 || content[205] != 65 || 7960 + (content[7958] & 0xff) + ((content[7959] & 0xff) << 8) != contentLength)
			return false;
		setSize(320, 192, RECOILResolution.XE2X1);
		final byte[] frame = new byte[61440];
		setBakPF012(content, 2, 1);
		int dlOffset = 9;
		int dliOffset = 7960;
		for (int y = 0; y < 192; y++) {
			decodeAtari8Gr15(content, 262 + 40 * y + (y >= 102 ? 16 : 0), 40, frame, y * 320, 320, 1);
			int dlInstr = content[dlOffset] & 0xff;
			if (dlOffset == 9 || dlOffset == 113) {
				if ((dlInstr & 127) != 78 || content[dlOffset + 1] != 0)
					return false;
				dlOffset += 3;
			}
			else {
				if ((dlInstr & 127) != 14)
					return false;
				dlOffset++;
			}
			if (dlInstr >= 128) {
				if (dliOffset + 14 > contentLength || content[dliOffset] != 72 || content[dliOffset + 1] != -118 || content[dliOffset + 2] != 72 || content[dliOffset + 3] != -87 || content[dliOffset + 5] != -115 || content[dliOffset + 6] != 10 || content[dliOffset + 7] != -44)
					return false;
				int a = content[dliOffset + 4] & 0xff;
				dliOffset += 8;
				while (content[dliOffset] != 32) {
					switch (content[dliOffset]) {
					case -87:
						a = content[dliOffset + 1] & 0xff;
						dliOffset += 2;
						break;
					case -115:
						if (content[dliOffset + 2] != -48)
							return false;
						int lo = content[dliOffset + 1] & 0xff;
						switch (lo) {
						case 22:
						case 23:
						case 24:
						case 26:
							this.gtiaColors[lo - 18] = (byte) (a & 254);
							break;
						default:
							return false;
						}
						dliOffset += 3;
						break;
					default:
						return false;
					}
					if (dliOffset + 3 > contentLength)
						return false;
				}
				if (content[dliOffset + 1] != -54 || content[dliOffset + 2] != 6)
					return false;
				dliOffset += 3;
			}
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeAtari8Font(byte[] characters, byte[] font, int fontOffset)
	{
		setSize(256, 32, RECOILResolution.XE1X1);
		final byte[] frame = new byte[8192];
		decodeAtari8Gr0(characters, 0, 32, font, fontOffset, frame);
		return applyAtari8Palette(frame);
	}

	private boolean decodeAtari8Fnt(byte[] content, int contentLength)
	{
		int contentOffset;
		switch (contentLength) {
		case 1024:
		case 1025:
		case 1026:
			contentOffset = 0;
			break;
		case 1030:
			if (parseAtari8ExecutableHeader(content, 0) != 1024)
				return false;
			contentOffset = 6;
			break;
		default:
			return false;
		}
		return decodeAtari8Font(null, content, contentOffset);
	}

	private boolean decodeF80(byte[] content, int contentLength)
	{
		if (contentLength != 512)
			return false;
		setSize(128, 32, RECOILResolution.XE1X1);
		final byte[] frame = new byte[4096];
		decodeAtari8Gr0(null, 0, 16, content, 0, frame);
		return applyAtari8Palette(frame);
	}

	private boolean decodeSxs(byte[] content, int contentLength)
	{
		if (contentLength != 1030 || parseAtari8ExecutableHeader(content, 0) != 1024)
			return false;
		final byte[] characters = new byte[128];
		for (int i = 0; i < 128; i++)
			characters[i] = (byte) ((i & 65) | (i >> 4 & 2) | (i & 30) << 1);
		return decodeAtari8Font(characters, content, 6);
	}

	private boolean decodeFn2(byte[] content, int contentLength)
	{
		if (contentLength != 2048)
			return false;
		setSize(256, 64, RECOILResolution.XE1X1);
		this.gtiaColors[6] = 0;
		this.gtiaColors[5] = 14;
		final byte[] frame = new byte[16384];
		for (int y = 0; y < 64; y += 8)
			decodeAtari8Gr0Line(null, y >> 4 << 5, content, (y & 8) << 7, frame, y << 8, 8);
		return applyAtari8Palette(frame);
	}

	private boolean decodeOdf(byte[] content, int contentLength)
	{
		if (contentLength != 1280)
			return false;
		setSize(256, 40, RECOILResolution.XE1X1);
		final byte[] frame = new byte[10240];
		for (int y = 0; y < 40; y++) {
			for (int x = 0; x < 256; x++)
				frame[(y << 8) + x] = (byte) (((content[y / 10 * 320 + (x >> 3) * 10 + y % 10] & 0xff) >> (~x & 7) & 1) == 0 ? 0 : 14);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeNlq(byte[] content, int contentLength)
	{
		if (contentLength < 379 || !isStringAt(content, 0, "DAISY-DOT NLQ FONT") || content[18] != -101)
			return false;
		setSize(320, 96, RECOILResolution.XE1X1);
		final byte[] frame = new byte[30720];
		int contentOffset = 19;
		for (int i = 0; i < 91; i++) {
			if (contentOffset >= contentLength)
				return false;
			int width = content[contentOffset] & 0xff;
			if (width == 0 || width > 19)
				return false;
			int nextContentOffset = contentOffset + (width + 1) * 2;
			if (nextContentOffset > contentLength || content[nextContentOffset - 1] != -101)
				return false;
			int c = i < 64 ? i : i < 90 ? i + 1 : 92;
			for (int y = 0; y < 16; y++) {
				for (int x = 0; x < width; x++) {
					int b = (content[contentOffset + 1 + (y & 1) * width + x] & 0xff) >> (7 - (y >> 1)) & 1;
					frame[((c & 240) | y) * 320 + (c & 15) * 20 + x] = (byte) (b == 0 ? 0 : 14);
				}
			}
			contentOffset = nextContentOffset;
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeGr1(byte[] content, int contentLength, int doubleHeight)
	{
		int charsLength = 480 >> doubleHeight;
		switch (contentLength - charsLength) {
		case 0:
			setXeOsDefaultColors();
			break;
		case 5:
			setBakPF0123(content, charsLength);
			break;
		default:
			return false;
		}
		setSize(320, 192, doubleHeight == 0 ? RECOILResolution.XE2X1 : RECOILResolution.XE2X2);
		final byte[] frame = new byte[61440];
		byte[] font = CiResource.getByteArray("atari8.fnt", 1024);
		for (int offset = 0; offset < charsLength; offset += 20)
			decodeAtari8Gr1Line(content, offset, font, 0, frame, offset << (7 + doubleHeight), doubleHeight);
		return applyAtari8Palette(frame);
	}

	private boolean decodeAcs(byte[] content, int contentLength)
	{
		if (contentLength != 1028)
			return false;
		setBakPF012(content, 0, 1);
		setSize(128, 64, RECOILResolution.XE2X1);
		final byte[] frame = new byte[8192];
		for (int y = 0; y < 8; y++)
			decodeAtari8Gr12Line(null, 0, content, 4 + (y << 7), frame, y << 10, 0);
		return applyAtari8Palette(frame);
	}

	private boolean decodeJgp(byte[] content, int contentLength)
	{
		if (contentLength != 2054 || parseAtari8ExecutableHeader(content, 0) != 2048)
			return false;
		setGr15DefaultColors();
		setSize(256, 64, RECOILResolution.XE2X1);
		final byte[] frame = new byte[16384];
		for (int y = 0; y < 8; y++)
			decodeAtari8Gr12Line(null, 0, content, 6 + ((y & 6) << 7) + ((y & 1) << 10), frame, y << 11, 0);
		return applyAtari8Palette(frame);
	}

	private boolean decodeLeo(byte[] content, int contentLength)
	{
		if (contentLength != 2580)
			return false;
		setSize(256, 64, RECOILResolution.XE2X1);
		final byte[] frame = new byte[16384];
		final byte[] characters = new byte[32];
		setPF0123Bak(content, 2560);
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 32; x++)
				characters[x] = (byte) (content[2048 + ((x & 1) << 7) + ((y & 1) << 6) + ((y & 6) << 3) + (x >> 1)] & 0xff);
			decodeAtari8Gr12Line(characters, 0, content, (y & 1) << 10, frame, y << 11, 0);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeAtari8Sif(byte[] content, int contentLength)
	{
		if (contentLength != 2048)
			return false;
		setSize(256, 32, RECOILResolution.XE2X1);
		this.gtiaColors[4] = 76;
		this.gtiaColors[5] = (byte) 204;
		this.gtiaColors[6] = (byte) 140;
		this.gtiaColors[8] = 0;
		final byte[] frame1 = new byte[8192];
		final byte[] frame2 = new byte[8192];
		for (int y = 0; y < 4; y++) {
			decodeAtari8Gr12Line(null, 0, content, y << 8, frame1, y << 11, 0);
			decodeAtari8Gr12Line(null, 0, content, 1024 + (y << 8), frame2, y << 11, 0);
		}
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeDlm(byte[] content, int contentLength)
	{
		if (contentLength != 256)
			return false;
		final byte[] characters = new byte[176];
		for (int y = 0; y < 16; y++)
			for (int x = 0; x < 11; x++)
				characters[y * 11 + x] = (byte) toAtari8Char(content[y * 16 + 5 + x] & 0xff);
		setSize(88, 128, RECOILResolution.XE1X1);
		final byte[] frame = new byte[11264];
		decodeAtari8Gr0(characters, 0, 11, CiResource.getByteArray("atari8.fnt", 1024), 0, frame);
		return applyAtari8Palette(frame);
	}

	private void decodeAtari8Gr0Screen(byte[] content, byte[] font)
	{
		setSize(320, 192, RECOILResolution.XE1X1);
		final byte[] frame = new byte[61440];
		decodeAtari8Gr0(content, 0, 40, font, 0, frame);
		applyAtari8Palette(frame);
	}

	private boolean decodeGr0(byte[] content, int contentLength)
	{
		if (contentLength != 960 && contentLength != 1024)
			return false;
		decodeAtari8Gr0Screen(content, CiResource.getByteArray("atari8.fnt", 1024));
		return true;
	}

	private boolean decodeSge(byte[] content, int contentLength)
	{
		if (contentLength != 960)
			return false;
		final byte[] font = new byte[1024];
		System.arraycopy(CiResource.getByteArray("atari8.fnt", 1024), 0, font, 0, 1024);
		for (int i = 0; i < 4; i++) {
			font[1004 + i] = font[728 + i] = 15;
			font[1000 + i] = font[732 + i] = (byte) 240;
		}
		decodeAtari8Gr0Screen(content, font);
		return true;
	}

	private boolean decodeAn2(byte[] content, int contentLength)
	{
		if (contentLength < 3)
			return false;
		int columns = (content[0] & 0xff) + 1;
		int rows = (content[1] & 0xff) + 1;
		if (columns > 40 || rows > 24 || contentLength != 2 + columns * rows)
			return false;
		setSize(columns << 3, rows << 3, RECOILResolution.XE1X1);
		final byte[] frame = new byte[61440];
		decodeAtari8Gr0(content, 2, columns, CiResource.getByteArray("atari8.fnt", 1024), 0, frame);
		return applyAtari8Palette(frame);
	}

	private boolean decodeAn4(byte[] content, int contentLength, int doubleHeight)
	{
		if (contentLength < 8)
			return false;
		int columns;
		int rows;
		int charactersOffset;
		if (contentLength << doubleHeight == 960) {
			columns = 40;
			rows = 24 >> doubleHeight;
			setXeOsDefaultColors();
			charactersOffset = 0;
		}
		else {
			columns = (content[0] & 0xff) + 1;
			rows = (content[1] & 0xff) + 1;
			if (columns > 40 || rows << doubleHeight > 24 || contentLength != 7 + columns * rows)
				return false;
			setBakPF0123(content, 2);
			charactersOffset = 7;
		}
		if (doubleHeight == 0)
			setSize(columns << 3, rows << 3, RECOILResolution.XE2X1);
		else
			setSize(columns << 3, rows << 4, RECOILResolution.XE2X2);
		final byte[] frame = new byte[61440];
		byte[] font = CiResource.getByteArray("atari8.fnt", 1024);
		for (int y = 0; y < rows; y++)
			decodeAtari8Gr12Line(content, charactersOffset + y * columns, font, 0, frame, y * columns << (6 + doubleHeight), doubleHeight);
		return applyAtari8Palette(frame);
	}

	private boolean decodeTl4(byte[] content, int contentLength)
	{
		if (contentLength < 11)
			return false;
		int columns = content[0] & 0xff;
		int rows = content[1] & 0xff;
		if (columns == 0 || columns > 4 || rows == 0 || rows > 5 || contentLength != 2 + columns * rows * 9)
			return false;
		setSize(columns << 3, rows << 3, RECOILResolution.XE2X1);
		final byte[] frame = new byte[1280];
		for (int y = 0; y < rows << 3; y++) {
			for (int x = 0; x < columns << 3; x++) {
				int offset = 2 + ((y >> 3) * columns + (x >> 3)) * 9;
				int c;
				switch ((content[offset + (y & 7)] & 0xff) >> (~x & 6) & 3) {
				case 1:
					c = 40;
					break;
				case 2:
					c = 202;
					break;
				case 3:
					c = content[offset + 8] == 0 ? 148 : 70;
					break;
				default:
					c = 0;
					break;
				}
				frame[(y * columns << 3) + x] = (byte) c;
			}
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeAsciiArtEditor(byte[] content, int contentLength)
	{
		if (contentLength <= 0 || content[contentLength - 1] != -101)
			return false;
		final byte[] characters = new byte[1536];
		int columns = 1;
		int x = 0;
		int y = 0;
		for (int contentOffset = 0; contentOffset < contentLength; contentOffset++) {
			int ch = content[contentOffset] & 0xff;
			if (y >= 24)
				return false;
			if (ch == 155) {
				if (columns < x)
					columns = x;
				while (x < 64)
					characters[y * 64 + x++] = 0;
				x = 0;
				y++;
			}
			else {
				if (x >= 64)
					return false;
				characters[y * 64 + x++] = (byte) toAtari8Char(ch);
			}
		}
		setSize(columns << 3, y << 3, RECOILResolution.XE1X1);
		final byte[] frame = new byte[98304];
		decodeAtari8Gr0(characters, 0, 64, CiResource.getByteArray("atari8.fnt", 1024), 0, frame);
		return applyAtari8Palette(frame);
	}

	private boolean decodeAll(byte[] content, int contentLength)
	{
		if ((contentLength & 1023) != 989)
			return false;
		setPF0123Bak(content, contentLength - 5);
		setSize(320, 192, RECOILResolution.XE2X1);
		final byte[] frame = new byte[61440];
		for (int y = 0; y < 24; y++) {
			int fontOffset = 24 + ((content[y] & 0xff) << 10);
			if (fontOffset >= contentLength - 965)
				return false;
			decodeAtari8Gr12Line(content, contentLength - 965 + y * 40, content, fontOffset, frame, y * 2560, 0);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeKpr(byte[] content, int contentLength)
	{
		if (contentLength < 11 || getAtari8ExecutableOffset(content, contentLength) != 6)
			return false;
		int frames = content[8] & 0xff;
		int cols = content[9] & 0xff;
		int rows = content[10] & 0xff;
		int tiles = frames * cols * rows;
		if (contentLength < 11 + tiles || !setSize(frames * cols << 3, rows << 3, RECOILResolution.XE2X1))
			return false;
		int pixelsOffset = 0;
		for (int y = 0; y < rows << 3; y++) {
			for (int f = 0; f < frames; f++) {
				for (int x = 0; x < cols << 3; x++) {
					int c = content[11 + (f * rows + (y >> 3)) * cols + (x >> 3)] & 0xff;
					c = 11 + tiles + (c << 3) + (y & 7);
					if (c >= contentLength)
						return false;
					c = (content[c] & 0xff) >> (~x & 6) & 3;
					this.pixels[pixelsOffset++] = this.atari8Palette[c << 2];
				}
			}
		}
		return true;
	}

	private boolean decodeEnvisionCommon(byte[] content, int mode, int columns, int rows, int charactersOffset, int[] fontId2Offset)
	{
		int charWidth;
		int charHeight;
		int resolution;
		switch (mode) {
		case 2:
			charWidth = 8;
			charHeight = 8;
			resolution = RECOILResolution.XE1X1;
			break;
		case 3:
			charWidth = 8;
			charHeight = 10;
			resolution = RECOILResolution.XE1X1;
			break;
		case 4:
			charWidth = 8;
			charHeight = 8;
			resolution = RECOILResolution.XE2X1;
			break;
		case 5:
			charWidth = 8;
			charHeight = 16;
			resolution = RECOILResolution.XE2X2;
			break;
		case 6:
			charWidth = 16;
			charHeight = 8;
			resolution = RECOILResolution.XE2X1;
			break;
		case 7:
			charWidth = 16;
			charHeight = 16;
			resolution = RECOILResolution.XE2X2;
			break;
		default:
			return false;
		}
		if (!setSize(columns * charWidth, rows * charHeight, resolution))
			return false;
		byte[] frame = new byte[this.width * this.height];
		for (int row = 0; row < rows; row++) {
			int fontOffset;
			if (fontId2Offset != null) {
				fontOffset = fontId2Offset[content[8 + columns * rows + 256 + row] & 0xff];
				if (fontOffset == 0)
					return false;
			}
			else {
				fontOffset = 10 + columns * rows;
			}
			int frameOffset = row * charHeight * this.width;
			switch (mode >> 1) {
			case 1:
				decodeAtari8Gr0Line(content, charactersOffset, content, fontOffset, frame, frameOffset, charHeight);
				break;
			case 2:
				decodeAtari8Gr12Line(content, charactersOffset, content, fontOffset, frame, frameOffset, mode & 1);
				break;
			case 3:
				decodeAtari8Gr1Line(content, charactersOffset, content, fontOffset, frame, frameOffset, mode & 1);
				break;
			default:
				throw new AssertionError();
			}
			charactersOffset += columns;
		}
		applyAtari8Palette(frame);
		return true;
	}

	private boolean decodeEnvision(byte[] content, int contentLength)
	{
		if (contentLength < 1505)
			return false;
		int columns = (content[1] & 0xff) + 1;
		int rows = (content[2] & 0xff) + 1;
		if (rows > 204)
			return false;
		int fontOffset = 8 + columns * rows + 463;
		if (contentLength < fontOffset || contentLength != fontOffset + (content[fontOffset - 1] & 0xff) * 1033)
			return false;
		final int[] fontId2Offset = new int[256];
		for (; fontOffset < contentLength; fontOffset += 1033)
			fontId2Offset[content[fontOffset] & 0xff] = fontOffset + 1 + 8;
		setPF0123Bak(content, 3);
		return decodeEnvisionCommon(content, content[0] & 127, columns, rows, 8, fontId2Offset);
	}

	private boolean decodeEnvisionPC(byte[] content, int contentLength)
	{
		if (contentLength < 1035 || (content[2] & 0xff) >= 128)
			return false;
		int columns = content[1] & 0xff | (content[2] & 0xff) << 8;
		int rows = content[3] & 0xff | (content[4] & 0xff) << 8;
		int contentOffset = 10 + columns * rows + 1024;
		while (contentOffset < contentLength) {
			switch (content[contentOffset++]) {
			case 0:
				break;
			case 1:
				if (contentOffset + 6 >= contentLength || (content[contentOffset + 1] & 0xff) >= 5 || (content[contentOffset + 3] & 0xff) >= 5 || (content[contentOffset + 5] & 0xff) >= 5)
					return false;
				contentOffset += ((content[contentOffset] & 0xff) + ((content[contentOffset + 1] & 0xff) << 8)) * ((content[contentOffset + 2] & 0xff) + ((content[contentOffset + 3] & 0xff) << 8)) * ((content[contentOffset + 4] & 0xff) + ((content[contentOffset + 5] & 0xff) << 8) + 1);
				break;
			case 2:
				contentOffset += columns * rows;
				break;
			case 3:
				contentOffset += 1027;
				break;
			default:
				return false;
			}
		}
		if (contentOffset > contentLength)
			return false;
		setBakPF0123(content, 5);
		return decodeEnvisionCommon(content, content[0] & 0xff, columns, rows, 10, null);
	}

	private boolean decodeMcs(byte[] content, int contentLength)
	{
		if (contentLength != 10185)
			return false;
		setSize(320, 192, RECOILResolution.XE2X1);
		final byte[] frame = new byte[61440];
		for (int y = 0; y < 192; y++) {
			int fontOffset = 9 + (y / 24 << 10);
			for (int x = 0; x < 320; x++) {
				int ch = content[8201 + (y >> 3) * 40 + (x >> 3)] & 0xff;
				int c;
				switch ((content[fontOffset + ((ch & 127) << 3) + (y & 7)] & 0xff) >> (~x & 6) & 3) {
				case 0:
					c = x / 80;
					int pmgBit = (x >> 3) % 10;
					int pmgOffset;
					if (pmgBit < 8) {
						pmgBit = 7 - pmgBit;
						pmgOffset = 9305 + (c << 7);
					}
					else {
						pmgBit = c << 1 | (pmgBit ^ 9);
						pmgOffset = 9177;
					}
					if (((content[pmgOffset + (y >> 1)] & 0xff) >> pmgBit & 1) == 0)
						c = 8;
					break;
				case 1:
					c = 4;
					break;
				case 2:
					c = 5;
					break;
				default:
					c = 6 + (ch >> 7);
					break;
				}
				frame[y * 320 + x] = (byte) (content[c] & 254);
			}
		}
		return applyAtari8Palette(frame);
	}

	private static int gr12GtiaNibbleToGr8(int nibble, int ch, boolean gtia10)
	{
		switch (nibble) {
		case 0:
		case 1:
		case 4:
		case 5:
			return 0;
		case 2:
		case 6:
			return 1;
		case 3:
		case 7:
			return (ch & 128) == 0 ? 2 : 3;
		case 8:
			return gtia10 ? 8 : 4;
		case 9:
			return 4;
		case 10:
			return 5;
		case 11:
			return (ch & 128) == 0 ? 6 : 7;
		case 12:
			return gtia10 || (ch & 128) == 0 ? 8 : 12;
		case 13:
			return (ch & 128) == 0 ? 8 : 12;
		case 14:
			return (ch & 128) == 0 ? 9 : 13;
		case 15:
			return (ch & 128) == 0 ? 10 : 15;
		default:
			return 0;
		}
	}

	private static int gr12GtiaByteToGr8(int b, int ch, boolean gtia10)
	{
		return gr12GtiaNibbleToGr8(b >> 4, ch, gtia10) << 4 | gr12GtiaNibbleToGr8(b & 15, ch, gtia10);
	}

	private static final int ICE_FONT_FRAME1 = -1;

	private static final int ICE_FONT_FRAME2 = -2;

	private void decodeIceFrame(byte[] content, int charactersOffset, int fontOffset, byte[] frame, int mode)
	{
		int doubleLine;
		switch (mode) {
		case IceFrameMode.GR13_GTIA9:
		case IceFrameMode.GR13_GTIA10:
		case IceFrameMode.GR13_GTIA11:
			doubleLine = 1;
			break;
		default:
			doubleLine = 0;
			break;
		}
		int frameOffset = 0;
		final byte[] bitmap = new byte[40];
		for (int y = 0; y < this.height; y++) {
			for (int col = 0; col < this.width >> 3; col++) {
				int ch;
				switch (charactersOffset) {
				case -1:
					ch = (DECODE_ICE_FRAME_ROW2CHAR1[y >> (3 + doubleLine)] & 0xff) + col;
					break;
				case -2:
					ch = (DECODE_ICE_FRAME_ROW2CHAR2[y >> (3 + doubleLine)] & 0xff) + col;
					break;
				default:
					ch = (y / 24 << 8) + (content[charactersOffset + (y >> 3) * 40 + col] & 0xff);
					break;
				}
				int b = content[fontOffset + ((ch & -129) << 3) + (y >> doubleLine & 7)] & 0xff;
				switch (mode) {
				case IceFrameMode.GR0:
				case IceFrameMode.GR0_GTIA9:
				case IceFrameMode.GR0_GTIA10:
				case IceFrameMode.GR0_GTIA11:
					if (charactersOffset < 0 && (ch & 128) != 0)
						b ^= 255;
					bitmap[col] = (byte) b;
					break;
				case IceFrameMode.GR12:
					for (int x = col == 0 ? this.leftSkip : 0; x < 8; x++) {
						int c = b >> (~x & 6) & 3;
						int gr12Registers = (ch & 128) == 0 ? 25928 : 30024;
						frame[frameOffset + (col << 3) + x - this.leftSkip] = (byte) (this.gtiaColors[gr12Registers >> (c << 2) & 15] & 0xff);
					}
					break;
				case IceFrameMode.GR12_GTIA9:
				case IceFrameMode.GR12_GTIA11:
				case IceFrameMode.GR13_GTIA9:
				case IceFrameMode.GR13_GTIA11:
					bitmap[col] = (byte) gr12GtiaByteToGr8(b, ch, false);
					break;
				case IceFrameMode.GR12_GTIA10:
				case IceFrameMode.GR13_GTIA10:
					bitmap[col] = (byte) gr12GtiaByteToGr8(b, ch, true);
					break;
				}
			}
			switch (mode) {
			case IceFrameMode.GR0:
				decodeAtari8Gr8(bitmap, 0, frame, frameOffset, 1);
				break;
			case IceFrameMode.GR12:
				for (int x = this.width; x < this.width + this.leftSkip; x++)
					frame[frameOffset + x] = (byte) (this.gtiaColors[8] & 0xff);
				break;
			case IceFrameMode.GR0_GTIA9:
			case IceFrameMode.GR12_GTIA9:
			case IceFrameMode.GR13_GTIA9:
				decodeAtari8Gr9(bitmap, 0, 0, frame, frameOffset, 0, this.width, 1);
				break;
			case IceFrameMode.GR0_GTIA10:
			case IceFrameMode.GR12_GTIA10:
			case IceFrameMode.GR13_GTIA10:
				decodeAtari8Gr10(bitmap, 0, frame, frameOffset, 0, 1);
				break;
			case IceFrameMode.GR0_GTIA11:
			case IceFrameMode.GR12_GTIA11:
			case IceFrameMode.GR13_GTIA11:
				decodeAtari8Gr11(bitmap, 0, frame, frameOffset, 0, 1);
				break;
			}
			frameOffset += this.width;
		}
	}

	private boolean verifyIce(byte[] content, int contentLength, boolean font, int fontLength, int imageLength, int resolution)
	{
		if (font) {
			if (contentLength != fontLength)
				return false;
			setSize(256, 128, resolution);
		}
		else {
			if (contentLength != imageLength || content[0] != 1)
				return false;
			setSize(320, 192, resolution);
		}
		return true;
	}

	private void decodeIce20Frame(byte[] content, boolean second, int fontOffset, byte[] frame, int mode)
	{
		final byte[] bitmap = new byte[32];
		for (int y = 0; y < 288; y++) {
			int row = y >> 5;
			int c = (second ? row / 3 : row % 3) + 1;
			for (int col = 0; col < 32; col++) {
				int ch = ((y & 24) << 1) + (col >> 1);
				int b = content[fontOffset + (ch << 3) + (y & 7)] & 0xff;
				b = (col & 1) == 0 ? b >> 4 : b & 15;
				b = ((b & 8) << 3 | (b & 4) << 2 | (b & 2) << 1 | (b & 1)) * c;
				if (mode == 10) {
					if ((b & 112) == 64)
						b = 128 + (b & 15);
					if ((b & 7) == 4)
						b = (b & 240) + 8;
				}
				bitmap[col] = (byte) b;
			}
			switch (mode) {
			case 9:
				decodeAtari8Gr9(bitmap, 0, 0, frame, y << 8, 0, 256, 1);
				break;
			case 10:
				decodeAtari8Gr10(bitmap, 0, frame, y << 8, 0, 1);
				break;
			case 11:
				decodeAtari8Gr11(bitmap, 0, frame, y << 8, 0, 1);
				break;
			default:
				throw new AssertionError();
			}
		}
	}

	private boolean decodeAtari8Ice(byte[] content, int contentLength, boolean font, int mode)
	{
		final byte[] frame1 = new byte[73728];
		final byte[] frame2 = new byte[73728];
		switch (mode) {
		case 0:
			if (contentLength != 2053)
				return false;
			setSize(256, 128, RECOILResolution.XE1X1);
			this.gtiaColors[5] = (byte) (content[1] & 254);
			this.gtiaColors[6] = (byte) (content[3] & 254);
			decodeIceFrame(content, -1, 5, frame1, IceFrameMode.GR0);
			this.gtiaColors[5] = (byte) (content[2] & 254);
			this.gtiaColors[6] = (byte) (content[4] & 254);
			decodeIceFrame(content, -2, 1029, frame2, IceFrameMode.GR0);
			break;
		case 1:
			if (!verifyIce(content, contentLength, font, 2054, 18310, RECOILResolution.XE2X1))
				return false;
			setBakPF0123(content, 1);
			decodeIceFrame(content, font ? -1 : 16390, 6, frame1, IceFrameMode.GR12);
			decodeIceFrame(content, font ? -2 : 17350, 1030, frame2, IceFrameMode.GR12);
			break;
		case 2:
			if (!verifyIce(content, contentLength, font, 2058, 18314, RECOILResolution.XE2X1))
				return false;
			this.gtiaColors[8] = (byte) (content[1] & 254);
			setPF0123Even(content, 2);
			decodeIceFrame(content, font ? -1 : 16394, 10, frame1, IceFrameMode.GR12);
			setPF0123Even(content, 3);
			decodeIceFrame(content, font ? -2 : 17354, 1034, frame2, IceFrameMode.GR12);
			break;
		case 3:
			if (font) {
				if (contentLength != 2055)
					return false;
				setSize(256, 128, RECOILResolution.XE1X1);
			}
			else {
				if (contentLength != 17351 || content[0] != 3)
					return false;
				setSize(320, 192, RECOILResolution.XE1X1);
			}
			setPF21(content, 1);
			decodeIceFrame(content, font ? -1 : 16391, 7, frame1, IceFrameMode.GR0);
			setBakPF0123(content, 2);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, font ? -2 : 16391, 1031, frame2, IceFrameMode.GR12);
			break;
		case 4:
			if (contentLength != 2058)
				return false;
			setSize(256, 128, RECOILResolution.XE4X1);
			this.leftSkip = 2;
			setGtiaColors(content, 1);
			decodeIceFrame(content, -1, 10, frame1, IceFrameMode.GR0_GTIA10);
			decodeIceFrame(content, -2, 1034, frame2, IceFrameMode.GR0_GTIA10);
			break;
		case 5:
			if (contentLength != 2065 && contentLength != 2066)
				return false;
			setSize(256, 128, RECOILResolution.XE4X1);
			this.leftSkip = 2;
			this.gtiaColors[0] = (byte) (content[1] & 254);
			for (int i = 0; i < 8; i++)
				setGtiaColor(i + 1, content[2 + i * 2] & 0xff);
			if (contentLength == 2065) {
				decodeIceFrame(content, -1, 17, frame1, IceFrameMode.GR0_GTIA10);
				for (int i = 0; i < 7; i++)
					setGtiaColor(i + 1, content[3 + i * 2] & 0xff);
				decodeIceFrame(content, -2, 1041, frame2, IceFrameMode.GR0_GTIA10);
			}
			else {
				decodeIceFrame(content, -1, 18, frame1, IceFrameMode.GR0_GTIA10);
				for (int i = 0; i < 8; i++)
					setGtiaColor(i + 1, content[3 + i * 2] & 0xff);
				decodeIceFrame(content, -2, 1042, frame2, IceFrameMode.GR0_GTIA10);
			}
			break;
		case 6:
			if (contentLength != 2051)
				return false;
			setSize(256, 128, RECOILResolution.XE4X1);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 3, frame1, IceFrameMode.GR0_GTIA9);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIceFrame(content, -2, 1027, frame2, IceFrameMode.GR0_GTIA9);
			break;
		case 7:
			if (contentLength != 2051)
				return false;
			setSize(256, 128, RECOILResolution.XE4X1);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 3, frame1, IceFrameMode.GR0_GTIA11);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIceFrame(content, -2, 1027, frame2, IceFrameMode.GR0_GTIA11);
			break;
		case 8:
			if (contentLength != 2058)
				return false;
			setSize(256, 128, RECOILResolution.XE2X1);
			this.leftSkip = 1;
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 10, frame1, IceFrameMode.GR0_GTIA9);
			setGtiaColors(content, 1);
			decodeIceFrame(content, -2, 1034, frame2, IceFrameMode.GR0_GTIA10);
			break;
		case 9:
			if (contentLength != 2058)
				return false;
			setSize(256, 128, RECOILResolution.XE2X1);
			this.leftSkip = 1;
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 10, frame1, IceFrameMode.GR0_GTIA11);
			this.gtiaColors[0] = 0;
			setPM123PF0123Bak(content, 2);
			decodeIceFrame(content, -2, 1034, frame2, IceFrameMode.GR0_GTIA10);
			break;
		case 10:
			if (contentLength != 2051)
				return false;
			setSize(256, 128, RECOILResolution.XE4X1);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 3, frame1, IceFrameMode.GR0_GTIA9);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIceFrame(content, -2, 1027, frame2, IceFrameMode.GR0_GTIA11);
			break;
		case 11:
			if (contentLength != 2051)
				return false;
			setSize(256, 128, RECOILResolution.XE1X1);
			this.gtiaColors[6] = 0;
			this.gtiaColors[5] = (byte) (content[2] & 254);
			decodeIceFrame(content, -1, 3, frame1, IceFrameMode.GR0);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -2, 1027, frame2, IceFrameMode.GR0_GTIA11);
			break;
		case 12:
			if (contentLength != 2051)
				return false;
			setSize(256, 128, RECOILResolution.XE1X1);
			setPF21(content, 1);
			decodeIceFrame(content, -1, 3, frame1, IceFrameMode.GR0);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -2, 1027, frame2, IceFrameMode.GR0_GTIA9);
			break;
		case 13:
			if (contentLength != 2059)
				return false;
			setSize(256, 128, RECOILResolution.XE1X1);
			setPF21(content, 1);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 11, frame1, IceFrameMode.GR0);
			this.leftSkip = 2;
			this.gtiaColors[0] = (byte) (content[1] & 254);
			setPM123PF0123Bak(content, 3);
			decodeIceFrame(content, -2, 1035, frame2, IceFrameMode.GR0_GTIA10);
			break;
		case 14:
			if (contentLength != 2054)
				return false;
			setSize(256, 128, RECOILResolution.XE2X1);
			setBakPF0123(content, 1);
			decodeIceFrame(content, -2, 1030, frame2, IceFrameMode.GR12_GTIA11);
			this.gtiaColors[8] = 0;
			decodeIceFrame(content, -1, 6, frame1, IceFrameMode.GR12);
			break;
		case 15:
			if (contentLength != 2054)
				return false;
			setSize(256, 128, RECOILResolution.XE2X1);
			setBakPF0123(content, 1);
			decodeIceFrame(content, -1, 6, frame1, IceFrameMode.GR12);
			decodeIceFrame(content, -2, 1030, frame2, IceFrameMode.GR12_GTIA9);
			break;
		case 16:
			if (contentLength != 2058)
				return false;
			setSize(256, 128, RECOILResolution.XE2X1);
			this.leftSkip = 2;
			setGtiaColors(content, 1);
			decodeIceFrame(content, -2, 1034, frame2, IceFrameMode.GR12_GTIA10);
			this.leftSkip = 0;
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 10, frame1, IceFrameMode.GR12);
			break;
		case 17:
			if (!verifyIce(content, contentLength, font, 2054, 17350, RECOILResolution.XE2X1))
				return false;
			setBakPF0123(content, 1);
			decodeIceFrame(content, font ? -2 : 16390, 1030, frame2, IceFrameMode.GR0_GTIA11);
			this.gtiaColors[8] = 0;
			decodeIceFrame(content, font ? -1 : 16390, 6, frame1, IceFrameMode.GR12);
			break;
		case 18:
			if (!verifyIce(content, contentLength, font, 2054, 17350, RECOILResolution.XE2X1))
				return false;
			setBakPF0123(content, 1);
			decodeIceFrame(content, font ? -1 : 16390, 6, frame1, IceFrameMode.GR12);
			decodeIceFrame(content, font ? -2 : 16390, 1030, frame2, IceFrameMode.GR0_GTIA9);
			break;
		case 19:
			if (!verifyIce(content, contentLength, font, 2058, 17354, RECOILResolution.XE2X1))
				return false;
			setPF0123Bak(content, 5);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, font ? -1 : 16394, 10, frame1, IceFrameMode.GR12);
			this.leftSkip = 2;
			setGtiaColors(content, 1);
			decodeIceFrame(content, font ? -2 : 16394, 1034, frame2, IceFrameMode.GR0_GTIA10);
			break;
		case 22:
			if (contentLength != 2058)
				return false;
			setSize(256, 256, RECOILResolution.XE4X2);
			this.leftSkip = 2;
			setGtiaColors(content, 1);
			decodeIceFrame(content, -1, 10, frame1, IceFrameMode.GR13_GTIA10);
			decodeIceFrame(content, -2, 1034, frame2, IceFrameMode.GR13_GTIA10);
			break;
		case 23:
			if (contentLength != 2065)
				return false;
			setSize(256, 256, RECOILResolution.XE4X2);
			this.leftSkip = 2;
			this.gtiaColors[0] = (byte) (content[1] & 254);
			for (int i = 0; i < 8; i++)
				setGtiaColor(i + 1, content[2 + i * 2] & 0xff);
			decodeIceFrame(content, -1, 17, frame1, IceFrameMode.GR13_GTIA10);
			for (int i = 0; i < 7; i++)
				setGtiaColor(i + 1, content[3 + i * 2] & 0xff);
			decodeIceFrame(content, -2, 1041, frame2, IceFrameMode.GR13_GTIA10);
			break;
		case 24:
			if (contentLength != 2051)
				return false;
			setSize(256, 256, RECOILResolution.XE4X2);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 3, frame1, IceFrameMode.GR13_GTIA9);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIceFrame(content, -2, 1027, frame2, IceFrameMode.GR13_GTIA9);
			break;
		case 25:
			if (contentLength != 2051)
				return false;
			setSize(256, 256, RECOILResolution.XE4X2);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 3, frame1, IceFrameMode.GR13_GTIA11);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIceFrame(content, -2, 1027, frame2, IceFrameMode.GR13_GTIA11);
			break;
		case 26:
			if (contentLength != 2058)
				return false;
			setSize(256, 256, RECOILResolution.XE2X2);
			this.leftSkip = 1;
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 10, frame1, IceFrameMode.GR13_GTIA9);
			setGtiaColors(content, 1);
			decodeIceFrame(content, -2, 1034, frame2, IceFrameMode.GR13_GTIA10);
			break;
		case 27:
			if (contentLength != 2058)
				return false;
			setSize(256, 256, RECOILResolution.XE2X2);
			this.leftSkip = 1;
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 10, frame1, IceFrameMode.GR13_GTIA11);
			this.gtiaColors[0] = 0;
			setPM123PF0123Bak(content, 2);
			decodeIceFrame(content, -2, 1034, frame2, IceFrameMode.GR13_GTIA10);
			break;
		case 28:
			if (contentLength != 2051)
				return false;
			setSize(256, 256, RECOILResolution.XE4X2);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIceFrame(content, -1, 3, frame1, IceFrameMode.GR13_GTIA9);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIceFrame(content, -2, 1027, frame2, IceFrameMode.GR13_GTIA11);
			break;
		case 31:
			if (contentLength != 1032)
				return false;
			setSize(256, 288, RECOILResolution.XE4X1);
			this.leftSkip = 2;
			for (int i = 0; i < 7; i++)
				setGtiaColor(DECODE_ATARI8_ICE_ICE20_GTIA11_COLORS[i] & 0xff, content[1 + i] & 0xff);
			decodeIce20Frame(content, false, 8, frame1, 10);
			decodeIce20Frame(content, true, 520, frame2, 10);
			break;
		case 32:
			if (contentLength != 1038)
				return false;
			setSize(256, 288, RECOILResolution.XE4X1);
			this.leftSkip = 2;
			this.gtiaColors[0] = (byte) (content[1] & 254);
			for (int i = 1; i < 7; i++)
				setGtiaColor(DECODE_ATARI8_ICE_ICE20_GTIA11_COLORS[i] & 0xff, content[i * 2] & 0xff);
			decodeIce20Frame(content, false, 14, frame1, 10);
			for (int i = 1; i < 7; i++)
				setGtiaColor(DECODE_ATARI8_ICE_ICE20_GTIA11_COLORS[i] & 0xff, content[1 + i * 2] & 0xff);
			decodeIce20Frame(content, true, 526, frame2, 10);
			break;
		case 33:
			if (contentLength != 1027)
				return false;
			setSize(256, 288, RECOILResolution.XE4X1);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIce20Frame(content, false, 3, frame1, 9);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIce20Frame(content, true, 515, frame2, 9);
			break;
		case 34:
			if (contentLength != 1027)
				return false;
			setSize(256, 288, RECOILResolution.XE4X1);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIce20Frame(content, false, 3, frame1, 11);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIce20Frame(content, true, 515, frame2, 11);
			break;
		case 35:
			if (contentLength != 1032)
				return false;
			setSize(256, 288, RECOILResolution.XE2X1);
			this.leftSkip = 1;
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIce20Frame(content, false, 8, frame1, 9);
			for (int i = 0; i < 7; i++)
				setGtiaColor(DECODE_ATARI8_ICE_ICE20_GTIA11_COLORS[i] & 0xff, content[1 + i] & 0xff);
			decodeIce20Frame(content, true, 520, frame2, 10);
			break;
		case 36:
			if (contentLength != 1032)
				return false;
			setSize(256, 288, RECOILResolution.XE2X1);
			this.leftSkip = 1;
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIce20Frame(content, false, 8, frame1, 11);
			this.gtiaColors[0] = 0;
			for (int i = 1; i < 7; i++)
				setGtiaColor(DECODE_ATARI8_ICE_ICE20_GTIA11_COLORS[i] & 0xff, content[1 + i] & 0xff);
			decodeIce20Frame(content, true, 520, frame2, 10);
			break;
		case 37:
			if (contentLength != 1027)
				return false;
			setSize(256, 288, RECOILResolution.XE4X1);
			this.gtiaColors[8] = (byte) (content[1] & 254);
			decodeIce20Frame(content, false, 3, frame1, 9);
			this.gtiaColors[8] = (byte) (content[2] & 254);
			decodeIce20Frame(content, true, 515, frame2, 11);
			break;
		default:
			return false;
		}
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private boolean decodeIp2(byte[] content, int contentLength)
	{
		if (contentLength != 17358 || content[0] != 1)
			return false;
		setSize(320, 192, RECOILResolution.XE2X1);
		this.gtiaColors[8] = (byte) (content[1] & 254);
		this.gtiaColors[4] = (byte) (content[5] & 254);
		this.gtiaColors[5] = (byte) (content[7] & 254);
		this.gtiaColors[6] = (byte) (content[9] & 254);
		this.gtiaColors[7] = (byte) (content[11] & 254);
		final byte[] frame1 = new byte[61440];
		decodeIceFrame(content, 16398, 14, frame1, IceFrameMode.GR12);
		this.leftSkip = 2;
		for (int i = 0; i < 4; i++) {
			this.gtiaColors[i] = (byte) (content[1 + i] & 254);
			setGtiaColor(4 + i, content[6 + i * 2] & 0xff);
		}
		setGtiaColor(8, content[13] & 0xff);
		final byte[] frame2 = new byte[61440];
		decodeIceFrame(content, 16398, 1038, frame2, IceFrameMode.GR0_GTIA10);
		return applyAtari8PaletteBlend(frame1, frame2);
	}

	private void decodeAtari8RgbScreen(byte[] screens, int screensOffset, int color, byte[] frame)
	{
		if (this.resolution == RECOILResolution.XE4X1) {
			this.gtiaColors[8] = (byte) color;
			decodeAtari8Gr9(screens, screensOffset, 40, frame, 0, this.width, this.width, this.height);
		}
		else {
			this.gtiaColors[8] = 0;
			this.gtiaColors[4] = (byte) (color | 4);
			this.gtiaColors[5] = (byte) (color | 10);
			this.gtiaColors[6] = (byte) (color | 14);
			decodeAtari8Gr15(screens, screensOffset, 40, frame, 0, this.width, this.height);
		}
	}

	private boolean decodeAtari8Rgb(byte[] content, int contentLength)
	{
		if (contentLength < 9 || !isStringAt(content, 0, "RGB1"))
			return false;
		int titleLength = content[4] & 0xff;
		if (contentLength < 9 + titleLength)
			return false;
		int width = content[6 + titleLength] & 0xff;
		int height = content[7 + titleLength] & 0xff;
		if (width == 0 || (width & 1) != 0 || width > 80 || height == 0 || height > 192 || content[8 + titleLength] != 1)
			return false;
		switch (content[5 + titleLength]) {
		case 9:
			setSize(width << 2, height, RECOILResolution.XE4X1);
			break;
		case 15:
			setSize(width << 2, height, RECOILResolution.XE2X1);
			break;
		default:
			return false;
		}
		final int[] leftRgbs = new int[192];
		final byte[] screens = new byte[23040];
		final RgbStream rle = new RgbStream();
		rle.content = content;
		rle.contentOffset = 9 + titleLength;
		rle.contentLength = contentLength;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = rle.readRle();
				if (rgb < 0)
					return false;
				if ((x & 1) == 0)
					leftRgbs[y] = rgb;
				else {
					int leftRgb = leftRgbs[y];
					int screenOffset = y * 40 + (x >> 1);
					screens[screenOffset] = (byte) ((leftRgb >> 4 & 240) | rgb >> 8);
					screens[7680 + screenOffset] = (byte) ((leftRgb & 240) | (rgb >> 4 & 15));
					screens[15360 + screenOffset] = (byte) ((leftRgb << 4 & 240) | (rgb & 15));
				}
			}
		}
		final byte[] frame1 = new byte[61440];
		decodeAtari8RgbScreen(screens, 0, 48, frame1);
		final byte[] frame2 = new byte[61440];
		decodeAtari8RgbScreen(screens, 7680, 192, frame2);
		final byte[] frame3 = new byte[61440];
		decodeAtari8RgbScreen(screens, 15360, 112, frame3);
		return applyAtari8PaletteBlend3(frame1, frame2, frame3);
	}

	private boolean drawBlazingPaddlesVector(byte[] content, int contentLength, byte[] frame, int frameOffset, int index, int startAddress)
	{
		if (index * 2 + 1 >= contentLength)
			return false;
		int contentOffset = (content[index * 2] & 0xff) + ((content[index * 2 + 1] & 0xff) << 8) - startAddress;
		if (contentOffset < 0)
			return false;
		while (contentOffset < contentLength) {
			int control = content[contentOffset++] & 0xff;
			if (control == 8)
				return true;
			for (; control >= 0; control -= 16) {
				if ((control & 4) == 0)
					frame[frameOffset + 1] = frame[frameOffset] = 14;
				switch (control & 3) {
				case 0:
					frameOffset += 2;
					break;
				case 1:
					frameOffset -= 2;
					break;
				case 2:
					frameOffset -= this.width;
					break;
				case 3:
					frameOffset += this.width;
					break;
				default:
					throw new AssertionError();
				}
			}
		}
		return false;
	}

	private boolean decodeBlazingPaddlesVectors(byte[] content, int contentLength, int startAddress)
	{
		int x = 0;
		int y = 0;
		int i;
		int lineI = 0;
		int lineTop = 0;
		int lineBottom = 0;
		final int[] xs = new int[256];
		final int[] ys = new int[256];
		int width = 0;
		final BlazingPaddlesBoundingBox box = new BlazingPaddlesBoundingBox();
		for (i = 0; i < 256; i++) {
			if (!box.calculate(content, contentLength, i, startAddress))
				break;
			int shapeWidth = box.right - box.left + 2;
			if (x + shapeWidth > 160) {
				y -= lineTop;
				while (lineI < i)
					ys[lineI++] = y;
				if (width < x)
					width = x;
				x = 0;
				y += lineBottom + 2;
				lineTop = box.top;
				lineBottom = box.bottom;
			}
			xs[i] = x - box.left;
			x += shapeWidth;
			if (lineTop > box.top)
				lineTop = box.top;
			if (lineBottom < box.bottom)
				lineBottom = box.bottom;
		}
		y -= lineTop;
		while (lineI < i)
			ys[lineI++] = y;
		if (width < x)
			width = x;
		y += lineBottom + 1;
		if (i == 0 || y > 240)
			return false;
		setSize(width << 1, y, RECOILResolution.XE2X1);
		final byte[] frame = new byte[76800];
		for (i = 0; i < 256; i++) {
			if (!drawBlazingPaddlesVector(content, contentLength, frame, (ys[i] * width + xs[i]) * 2, i, startAddress))
				break;
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeChr(byte[] content, int contentLength)
	{
		return contentLength == 3072 && decodeBlazingPaddlesVectors(content, contentLength, 28672);
	}

	private boolean decodeShp(byte[] content, int contentLength)
	{
		if (decodeC64Shp(content, contentLength))
			return true;
		switch (contentLength) {
		case 1024:
			return decodeBlazingPaddlesVectors(content, contentLength, 31744);
		case 4384:
			return decodeGr7(content, 528, 3844);
		case 10018:
			return decodeOcp(content, contentLength);
		default:
			return false;
		}
	}

	private static void drawSpcChar(byte[] pixels, int x1, int y1, int ch)
	{
		if (ch < 32 || ch > 95)
			return;
		byte[] font = CiResource.getByteArray("atari8.fnt", 1024);
		int fontOffset = (ch - 32) << 3;
		for (int y = 0; y < 8 && y1 + y < 192; y++) {
			for (int x = 0; x < 4 && x1 + x < 160; x++)
				pixels[(y1 + y) * 160 + x1 + x] = (byte) ((font[fontOffset + y] & 0xff) >> (6 - x * 2) & 3);
		}
	}

	private static void drawSpcLine(byte[] pixels, int x1, int y1, int x2, int y2, int color)
	{
		int dx = x2 - x1;
		int dy = y2 - y1;
		if (dx < 0)
			dx = -dx;
		if (dy < 0)
			dy = -dy;
		if (dx >= dy) {
			int e = dx;
			if (x2 < x1) {
				int ty = y1;
				x1 = x2;
				x2 += dx;
				y1 = y2;
				y2 = ty;
			}
			for (; x1 <= x2; x1++) {
				if (x1 < 160 && y1 < 192)
					pixels[160 * y1 + x1] = (byte) color;
				e -= dy * 2;
				if (e < 0) {
					e += dx * 2;
					y1 += y1 < y2 ? 1 : -1;
				}
			}
		}
		else {
			int e = dy;
			if (y2 < y1) {
				int tx = x1;
				x1 = x2;
				x2 = tx;
				y1 = y2;
				y2 += dy;
			}
			for (; y1 <= y2; y1++) {
				if (x1 < 160 && y1 < 192)
					pixels[160 * y1 + x1] = (byte) color;
				e -= dx * 2;
				if (e < 0) {
					e += dy * 2;
					x1 += x1 < x2 ? 1 : -1;
				}
			}
		}
	}

	private static void plotSpcPattern(byte[] pixels, int x, int y, int pattern)
	{
		pixels[y * 160 + x] = (byte) (pattern >> (((~y & 1) << 3) + ((~x & 3) << 1)) & 3);
	}

	private static void drawSpcBrush(byte[] pixels, int x1, int y1, int brush, int pattern)
	{
		for (int y = 0; y < 16 && y1 + y < 192; y++) {
			int brushShape = DRAW_SPC_BRUSH_BRUSHES[brush * 16 + y] & 0xff;
			for (int x = 0; x < 8 && x1 + x < 160; x++) {
				if ((brushShape >> (7 - x) & 1) != 0)
					plotSpcPattern(pixels, x, y, pattern);
			}
		}
	}

	private static boolean fillSpc(byte[] pixels, int x, int y, int pattern)
	{
		if (x >= 160 || y >= 192)
			return false;
		while (y >= 0 && pixels[y * 160 + x] == 0)
			y--;
		while (++y < 192 && pixels[y * 160 + x] == 0) {
			do
				x--;
			while (x >= 0 && pixels[y * 160 + x] == 0);
			int x1 = x;
			while (x < 159) {
				if (pixels[y * 160 + ++x] != 0)
					break;
				plotSpcPattern(pixels, x, y, pattern);
			}
			x = x1 + ((x - x1 + 1) >> 1);
		}
		return true;
	}

	private boolean decodeAtari8Spc(byte[] content, int contentLength)
	{
		if (contentLength < 3 || contentLength != (content[0] & 0xff) + ((content[1] & 0xff) << 8) + 3 || content[contentLength - 1] != 0)
			return false;
		final byte[] pixels = new byte[30720];
		final int[] lineColors = new int[96];
		int textX = 0;
		int textY = 0;
		int lineX = 0;
		int lineY = 0;
		int brush = 0;
		int pattern = 8840;
		int lineColor = 3;
		int x;
		int y;
		for (int contentOffset = 2; content[contentOffset] != 0;) {
			switch (content[contentOffset]) {
			case 16:
				if (contentOffset + 3 >= contentLength)
					return false;
				textX = content[contentOffset + 1] & 0xff;
				textY = content[contentOffset + 2] & 0xff;
				contentOffset += 3;
				break;
			case 32:
			case 33:
			case 34:
			case 35:
				if (contentOffset + 1 >= contentLength)
					return false;
				lineColor = content[contentOffset] & 3;
				contentOffset++;
				break;
			case 48:
			case 80:
				if (contentOffset + 2 >= contentLength)
					return false;
				drawSpcChar(pixels, textX, textY, content[contentOffset + 1] & 0xff);
				textX += 4;
				contentOffset += 2;
				break;
			case 64:
			case 65:
			case 66:
			case 67:
			case 68:
			case 69:
			case 70:
			case 71:
				if (contentOffset + 1 >= contentLength)
					return false;
				brush = content[contentOffset] & 7;
				contentOffset++;
				break;
			case 96:
				if (contentOffset + 2 >= contentLength)
					return false;
				pattern = content[contentOffset + 1] & 0xff;
				if (pattern >= 71)
					return false;
				pattern = DECODE_ATARI8_SPC_PATTERNS[pattern];
				contentOffset += 2;
				break;
			case 112:
				if (contentOffset + 7 >= contentLength)
					return false;
				for (y = content[contentOffset + 1] & 0xff; y <= (content[contentOffset + 2] & 0xff); y++) {
					if (y >= 96)
						return false;
					lineColors[y] = contentOffset + 3;
				}
				contentOffset += 7;
				break;
			case -128:
				if (contentOffset + 3 >= contentLength)
					return false;
				lineX = content[contentOffset + 1] & 0xff;
				lineY = content[contentOffset + 2] & 0xff;
				contentOffset += 3;
				break;
			case -96:
				if (contentOffset + 3 >= contentLength)
					return false;
				x = content[contentOffset + 1] & 0xff;
				y = content[contentOffset + 2] & 0xff;
				drawSpcLine(pixels, lineX, lineY, x, y, lineColor);
				lineX = x;
				lineY = y;
				contentOffset += 3;
				break;
			case -64:
				if (contentOffset + 3 >= contentLength)
					return false;
				drawSpcBrush(pixels, content[contentOffset + 1] & 0xff, content[contentOffset + 2] & 0xff, brush, pattern);
				contentOffset += 3;
				break;
			case -32:
				if (contentOffset + 3 >= contentLength)
					return false;
				if (!fillSpc(pixels, content[contentOffset + 1] & 0xff, content[contentOffset + 2] & 0xff, pattern))
					return false;
				contentOffset += 3;
				break;
			default:
				return false;
			}
		}
		setSize(320, 192, RECOILResolution.XE2X1);
		final byte[] frame = new byte[61440];
		for (y = 0; y < 192; y++) {
			int colorsOffset = lineColors[y >> 1];
			byte[] colors = colorsOffset == 0 ? DECODE_ATARI8_SPC_DEFAULT_COLORS : content;
			for (x = 0; x < 160; x++) {
				int offset = y * 320 + x * 2;
				frame[offset + 1] = frame[offset] = (byte) (colors[colorsOffset + (pixels[y * 160 + x] & 0xff)] & 254);
			}
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeHcm(byte[] content, int contentLength)
	{
		if (contentLength != 8208 || !isStringAt(content, 0, "HCMA8") || content[5] != 1)
			return false;
		final HcmRenderer gtia = new HcmRenderer();
		int leftSprite;
		switch (content[6]) {
		case 0:
			leftSprite = 2;
			gtia.prior = 0;
			break;
		case 2:
			leftSprite = 1;
			gtia.prior = 36;
			break;
		default:
			return false;
		}
		gtia.playerHpos[3] = gtia.playerHpos[3 - leftSprite] = 104;
		gtia.missileHpos[leftSprite] = gtia.missileHpos[0] = (byte) 136;
		gtia.missileHpos[3] = gtia.missileHpos[3 - leftSprite] = (byte) 144;
		for (int i = 0; i < 4; i++)
			gtia.missileSize[i] = gtia.playerSize[i] = 4;
		gtia.colors[8] = (byte) (content[7] & 254);
		gtia.colors[3 - leftSprite] = gtia.colors[0] = (byte) (content[8] & 254);
		gtia.colors[3] = gtia.colors[leftSprite] = (byte) (content[9] & 254);
		gtia.colors[4] = (byte) (content[10] & 254);
		gtia.colors[5] = (byte) (content[11] & 254);
		gtia.colors[6] = (byte) (content[12] & 254);
		gtia.content = content;
		gtia.playfieldColumns = 32;
		setSize(256, 192, RECOILResolution.XE2X1);
		final byte[] frame = new byte[49152];
		for (int y = 0; y < 192; y++) {
			gtia.playerHpos[leftSprite] = gtia.playerHpos[0] = 72;
			gtia.processSpriteDma(content, 816 + y);
			gtia.startLine(64);
			gtia.drawSpan(y, 64, 128, AnticMode.FOUR_COLOR, frame, 256);
			gtia.playerHpos[leftSprite] = gtia.playerHpos[0] = (byte) 152;
			gtia.playerGraphics[0] = (byte) (content[48 + y] & 0xff);
			gtia.playerGraphics[leftSprite] = (byte) (content[304 + y] & 0xff);
			gtia.drawSpan(y, 128, 192, AnticMode.FOUR_COLOR, frame, 256);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodeGed(byte[] content, int contentLength)
	{
		if (contentLength != 11302 || content[0] != -1 || content[1] != -1 || content[2] != 48 || content[3] != 83 || content[4] != 79 || content[5] != 127)
			return false;
		int cycle = content[3300] & 0xff;
		if (cycle > 7)
			return false;
		final GedRenderer gtia = new GedRenderer();
		GtiaRenderer.setSpriteSizes(gtia.missileSize, content[3291] & 0xff);
		gtia.colors[7] = (byte) (content[3293] & 254);
		gtia.colors[8] = (byte) (content[3294] & 254);
		int prior = content[3292] & 0xff;
		gtia.prior = prior;
		for (int i = 0; i < 4; i++) {
			gtia.setPlayerSize(i, (content[3290] & 0xff) >> ((3 - i) << 1));
			gtia.playerHpos[i] = (byte) (48 + (content[3295 + i] & 0xff));
			gtia.missileHpos[i] = (byte) ((prior & 16) == 0 ? (gtia.playerHpos[i] & 0xff) + ((gtia.playerSize[i] & 0xff) << 3) : i == 0 ? 48 + (content[3299] & 0xff) : (gtia.missileHpos[i - 1] & 0xff) + ((gtia.missileSize[i - 1] & 0xff) << 1));
			gtia.colors[i] = (byte) (content[3286 + i] & 254);
		}
		gtia.content = content;
		gtia.playfieldColumns = 40;
		setSize(320, 200, RECOILResolution.XE2X1);
		final byte[] frame = new byte[64000];
		for (int y = 0; y < 200; y++) {
			gtia.processSpriteDma(content, 2034 + y);
			gtia.poke(content[206 + y] & 31, content[6 + y] & 0xff);
			gtia.colors[4] = (byte) (content[406 + y] & 254);
			gtia.colors[5] = (byte) (content[606 + y] & 254);
			gtia.colors[6] = (byte) (content[806 + y] & 254);
			gtia.startLine(48);
			int hpos = gtia.drawSpan(y, 48, 63 + (cycle << 3), AnticMode.FOUR_COLOR, frame, 320);
			gtia.colors[4] = (byte) (content[1006 + y] & 254);
			hpos = gtia.drawSpan(y, hpos, cycle < 4 ? hpos + 32 : 107 + (cycle << 2), AnticMode.FOUR_COLOR, frame, 320);
			gtia.colors[5] = (byte) (content[1206 + y] & 254);
			hpos = gtia.drawSpan(y, hpos, 123 + (cycle << 2), AnticMode.FOUR_COLOR, frame, 320);
			gtia.colors[6] = (byte) (content[1406 + y] & 254);
			hpos = gtia.drawSpan(y, hpos, hpos + 24, AnticMode.FOUR_COLOR, frame, 320);
			gtia.colors[4] = (byte) (content[1606 + y] & 254);
			hpos = gtia.drawSpan(y, hpos, hpos + 24, AnticMode.FOUR_COLOR, frame, 320);
			gtia.colors[5] = (byte) (content[1806 + y] & 254);
			gtia.drawSpan(y, hpos, 208, AnticMode.FOUR_COLOR, frame, 320);
		}
		return applyAtari8Palette(frame);
	}

	private boolean decodePgr(byte[] content, int contentLength)
	{
		if (contentLength < 1776 || 6 + parseAtari8ExecutableHeader(content, 0) != contentLength || content[2] != 6 || content[3] != -126 || !isStringAt(content, 8, "PowerGFX"))
			return false;
		final PgrRenderer gtia = new PgrRenderer();
		for (int i = 0; i < 14; i++) {
			gtia.poke(i, content[504 + i] & 0xff);
			gtia.poke(14 + i, content[760 + i] & 0xff);
		}
		gtia.content = content;
		int dmaCtl = content[774] & 0xff;
		switch (dmaCtl & 243) {
		case 49:
			gtia.playfieldColumns = 32;
			break;
		case 50:
			gtia.playfieldColumns = 40;
			break;
		default:
			return false;
		}
		int dlOffset = 16;
		int rasterOffset = (content[6] & 0xff) + ((content[7] & 0xff) << 8) - 33280;
		if (rasterOffset < 1536)
			return false;
		gtia.screenOffset = -1;
		int a = 0;
		final byte[] frame = new byte[80640];
		int resolution = RECOILResolution.XE4X1;
		for (int y = 0; y < 240; y++) {
			int hpos = -11;
			int dlOp = content[dlOffset++] & 0xff;
			switch (dlOp) {
			case 0:
			case 14:
			case 15:
				break;
			case 65:
				dlOffset--;
				break;
			case 78:
			case 79:
				hpos += 4;
				gtia.screenOffset = (content[dlOffset] & 0xff) + ((content[dlOffset + 1] & 0xff) << 8) - 33280;
				dlOffset += 2;
				break;
			default:
				return false;
			}
			int anticMode;
			switch (dlOp & 15) {
			case 14:
				anticMode = AnticMode.FOUR_COLOR;
				if (resolution == RECOILResolution.XE4X1 && gtia.prior < 64)
					resolution = RECOILResolution.XE2X1;
				break;
			case 15:
				anticMode = AnticMode.HI_RES;
				if (gtia.prior < 64)
					resolution = RECOILResolution.XE1X1;
				break;
			default:
				anticMode = AnticMode.BLANK;
				break;
			}
			if (anticMode != AnticMode.BLANK && (gtia.screenOffset < 1536 || gtia.screenOffset + 40 > contentLength))
				return false;
			gtia.startLine(44);
			if ((dmaCtl & 12) != 0) {
				hpos += 2;
				if ((dmaCtl & 4) != 0)
					gtia.missileGraphics = content[264 + y] & 0xff;
				if ((dmaCtl & 8) != 0) {
					hpos += 8;
					gtia.processPlayerDma(content, 520 + y);
				}
			}
			for (int cpuCycles = 1;;) {
				if (rasterOffset >= contentLength)
					return false;
				int rasterOp = content[rasterOffset++] & 0xff;
				if ((rasterOp & 32) != 0) {
					if (rasterOffset >= contentLength)
						return false;
					cpuCycles += 2;
					a = content[rasterOffset++] & 0xff;
				}
				int addr = rasterOp & 31;
				if (addr <= 27) {
					int untilHpos = gtia.advanceCpuCycles(hpos, cpuCycles, anticMode != AnticMode.BLANK);
					gtia.drawSpan(y, hpos >= 44 ? hpos : 44, untilHpos < 212 ? untilHpos : 212, anticMode, frame, 336);
					hpos = untilHpos;
					gtia.poke(addr, a);
					if (rasterOp >= 128)
						break;
					cpuCycles = 4;
				}
				else {
					int nops = (rasterOp >> 6 & 3) | (rasterOp & 3) << 2;
					if (nops == 0)
						break;
					cpuCycles += nops << 1;
				}
			}
			gtia.drawSpan(y, hpos >= 44 ? hpos : 44, 212, anticMode, frame, 336);
			if (anticMode != AnticMode.BLANK)
				gtia.screenOffset += gtia.playfieldColumns;
		}
		setSize(336, 240, resolution);
		return applyAtari8Palette(frame);
	}

	private static boolean hasG2fRaster(byte[] content, int contentOffset, int count, int hitClr)
	{
		do {
			switch (content[contentOffset]) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 65:
			case 66:
			case 67:
			case 97:
			case 98:
			case 99:
				break;
			case -127:
			case -126:
			case -125:
				if ((content[contentOffset + 1] & 0xff) != hitClr)
					return true;
				break;
			default:
				return true;
			}
			contentOffset += 2;
		}
		while (--count > 0);
		return false;
	}

	private boolean decodeMch(byte[] content, int contentLength)
	{
		int columns;
		switch (contentLength) {
		case 9840:
		case 28673:
			columns = 32;
			break;
		case 12000:
		case 30833:
			columns = 40;
			break;
		case 14160:
		case 32993:
			columns = 48;
			break;
		default:
			return false;
		}
		int bitmapLength = columns * 270;
		boolean sprites = contentLength > bitmapLength + 1200;
		int anticMode;
		switch (content[0] & 3) {
		case 0:
			if (sprites && hasG2fRaster(content, bitmapLength + 6080, 6960, 30))
				return false;
			anticMode = AnticMode.FIVE_COLOR;
			break;
		case 1:
			anticMode = AnticMode.FIVE_COLOR;
			break;
		case 2:
			if (sprites && hasG2fRaster(content, bitmapLength + 6080, 6960, 30))
				return false;
			anticMode = AnticMode.FOUR_COLOR;
			break;
		default:
			return false;
		}
		int resolution;
		int gtiaMode = 0;
		switch (content[0] & 60) {
		case 0:
			anticMode = AnticMode.HI_RES;
			resolution = RECOILResolution.XE1X1;
			break;
		case 4:
			resolution = RECOILResolution.XE2X1;
			break;
		case 8:
			anticMode = AnticMode.HI_RES;
			resolution = RECOILResolution.XE4X1;
			gtiaMode = 64;
			break;
		case 24:
			anticMode = AnticMode.HI_RES;
			resolution = RECOILResolution.XE4X1;
			gtiaMode = 128;
			break;
		case 40:
			anticMode = AnticMode.HI_RES;
			resolution = RECOILResolution.XE4X1;
			gtiaMode = 192;
			break;
		default:
			return false;
		}
		setSize(336, 240, resolution);
		final byte[] frame = new byte[80640];
		final MchRenderer gtia = new MchRenderer();
		Arrays.fill(gtia.playerHpos, (byte) 0);
		Arrays.fill(gtia.missileHpos, (byte) 0);
		Arrays.fill(gtia.colors, (byte) 0);
		gtia.prior = gtiaMode;
		gtia.content = content;
		gtia.playfieldColumns = columns;
		gtia.dliPlus = false;
		for (int i = 0; i < bitmapLength; i += 9) {
			if ((content[i] & 64) != 0) {
				gtia.dliPlus = true;
				break;
			}
		}
		for (int y = 0; y < 240; y++) {
			int colorsOffset = bitmapLength + y;
			gtia.setG2fColors(colorsOffset, 240, sprites ? 9 : 5, gtiaMode);
			if (sprites) {
				for (int i = 0; i < 4; i++) {
					gtia.playerHpos[i] = (byte) (content[colorsOffset + (9 + i) * 240] & 0xff);
					gtia.missileHpos[i] = (byte) (content[colorsOffset + (13 + i) * 240] & 0xff);
				}
				GtiaRenderer.setSpriteSizes(gtia.playerSize, content[colorsOffset + 4080] & 0xff);
				GtiaRenderer.setSpriteSizes(gtia.missileSize, content[colorsOffset + 4320] & 0xff);
				gtia.prior = gtiaMode | content[colorsOffset + 4560] & 0xff;
				gtia.processSpriteDma(content, colorsOffset + 4800);
			}
			gtia.startLine(44);
			gtia.drawSpan(y, 44, 212, anticMode, frame, 336);
		}
		return applyAtari8Palette(frame);
	}

	private static boolean g2fHasRaster(byte[] content, int contentOffset)
	{
		return hasG2fRaster(content, contentOffset, 2880, (content[0] & 0xff) < 128 ? 22 : 30);
	}

	private boolean decodeG2fUnpacked(byte[] content, int contentLength, byte[] frame, int yOffset)
	{
		if (contentLength < 155711)
			return false;
		int columns = content[0] & 127;
		switch (columns) {
		case 32:
		case 40:
		case 48:
			break;
		default:
			return false;
		}
		int fontsOffset = 3 + 30 * columns;
		int fontNumberOffset = fontsOffset + (((content[2] & 127) + 1) << 10);
		if (contentLength < fontNumberOffset + 153724)
			return false;
		boolean charMode;
		int vbxeOffset = fontNumberOffset + 155231;
		int inverse2Offset = -1;
		switch (content[fontNumberOffset + 147679] & 127) {
		case 1:
			if (g2fHasRaster(content, fontNumberOffset + 147934))
				return false;
			charMode = true;
			break;
		case 2:
			charMode = true;
			break;
		case 3:
			if (g2fHasRaster(content, fontNumberOffset + 147934))
				return false;
			charMode = false;
			break;
		case 66:
			charMode = true;
			inverse2Offset = vbxeOffset + 138244;
			if (contentLength < inverse2Offset + 30 * columns)
				return false;
			break;
		default:
			return false;
		}
		if (contentLength < vbxeOffset + 138243)
			vbxeOffset = -1;
		else {
			switch (content[vbxeOffset]) {
			case 0:
				vbxeOffset = -1;
				break;
			case 1:
				if (content[vbxeOffset + 1] != 8 || content[vbxeOffset + 2] == 0)
					return false;
				this.resolution = RECOILResolution.VBXE2X1;
				break;
			default:
				return false;
			}
		}
		final G2fRenderer gtia = new G2fRenderer();
		gtia.content = content;
		gtia.playfieldColumns = columns;
		gtia.inverse2Offset = inverse2Offset;
		gtia.vbxeOffset = vbxeOffset;
		for (int y = 0; y < 240; y++) {
			int row = y >> 3;
			gtia.fontOffset = fontsOffset + ((content[fontNumberOffset + row] & 127) << 10);
			if (gtia.fontOffset >= fontNumberOffset)
				return false;
			int spriteOffset = fontNumberOffset + 2334 + (y << 1);
			int prior = (content[spriteOffset + 1] & 0xff) >> 4 & 7;
			if (prior >= 5)
				return false;
			prior = DECODE_G2F_UNPACKED_PRIORS[prior] & 0xff | (content[spriteOffset + 1025] & 48);
			int anticMode;
			switch (content[fontNumberOffset + 153694 + row]) {
			case 1:
				this.resolution = this.resolution == RECOILResolution.VBXE2X1 || this.resolution == RECOILResolution.VBXE1X1 ? RECOILResolution.VBXE1X1 : RECOILResolution.XE1X1;
				anticMode = AnticMode.HI_RES;
				break;
			case 2:
				if (this.resolution == RECOILResolution.XE4X1)
					this.resolution = RECOILResolution.XE2X1;
				anticMode = charMode ? AnticMode.FIVE_COLOR : AnticMode.FOUR_COLOR;
				break;
			case 4:
				prior |= DECODE_G2F_UNPACKED_GTIA_MODES[content[1] & 7] & 0xff;
				anticMode = AnticMode.HI_RES;
				break;
			case -1:
				anticMode = AnticMode.BLANK;
				break;
			default:
				return false;
			}
			int colorsOffset = fontNumberOffset + 30 + y;
			gtia.setG2fColors(colorsOffset, 256, 9, prior);
			int missileGraphics = 0;
			for (int i = 0; i < 4; i++) {
				if (!G2fRenderer.setSprite(gtia.playerHpos, gtia.playerSize, i, content, spriteOffset) || !G2fRenderer.setSprite(gtia.missileHpos, gtia.missileSize, i, content, spriteOffset + 512))
					return false;
				gtia.playerGraphics[i] = (byte) (content[colorsOffset + 6400 + (i << 9)] & 0xff);
				missileGraphics |= (content[colorsOffset + 6656 + (i << 9)] & 0xff) >> 6 << (i << 1);
			}
			gtia.missileGraphics = missileGraphics;
			gtia.prior = prior;
			gtia.startLine(44);
			gtia.drawSpan(y, 44, 212, anticMode, frame, 336, yOffset);
		}
		return true;
	}

	private static final int G2F_MAX_UNPACKED_LENGTH = 327078;

	private boolean decodeG2fFrame(byte[] content, int contentLength, byte[] frame, int yOffset)
	{
		if (contentLength < 11)
			return false;
		if (isStringAt(content, 0, "G2FZLIB")) {
			byte[] unpacked = new byte[327078];
			final InflateStream stream = new InflateStream();
			stream.content = content;
			stream.contentOffset = 7;
			stream.contentLength = contentLength;
			contentLength = stream.uncompress(unpacked, 327078);
			return decodeG2fUnpacked(unpacked, contentLength, frame, yOffset);
		}
		return decodeG2fUnpacked(content, contentLength, frame, yOffset);
	}

	private boolean decodeG2f(byte[] content, int contentLength)
	{
		final byte[] frame = new byte[80640];
		this.resolution = RECOILResolution.XE4X1;
		if (!decodeG2fFrame(content, contentLength, frame, 0))
			return false;
		setSize(336, 240, this.resolution);
		return applyAtari8Palette(frame);
	}

	private boolean decodeVsc(String vscFilename, byte[] content, int contentLength)
	{
		final Stream s = new Stream();
		s.content = content;
		s.contentOffset = 0;
		s.contentLength = contentLength;
		int height = 0;
		for (;;) {
			int c = s.readByte();
			if (c < 0)
				break;
			switch (c) {
			case '\r':
				if (s.readByte() != '\n')
					return false;
				height += 240;
				break;
			case '/':
			case '\\':
				return false;
			default:
				if (c < ' ' || c > '~')
					return false;
				break;
			}
		}
		byte[] frame = new byte[336 * height];
		byte[] g2f = new byte[327078];
		s.contentOffset = 0;
		this.resolution = RECOILResolution.XE4X1;
		for (height = 0;; height += 240) {
			int filenameOffset = s.contentOffset;
			if (!s.skipUntilByte('\n'))
				break;
			String filename = new String(content, filenameOffset, s.contentOffset - 2 - filenameOffset, StandardCharsets.UTF_8);
			contentLength = readSiblingFile(vscFilename, filename, g2f, 327078);
			if (!decodeG2fFrame(g2f, contentLength, frame, height))
				return false;
		}
		return setSize(336, height, this.resolution) && applyAtari8Palette(frame);
	}

	private boolean decodeDap(byte[] content, int contentLength)
	{
		if (contentLength != 77568)
			return false;
		setSize(320, 240, RECOILResolution.VBXE1X1);
		for (int i = 0; i < 256; i++)
			this.contentPalette[i] = (content[76800 + i] & 0xff) << 16 | (content[77056 + i] & 0xff) << 8 | content[77312 + i] & 0xff;
		decodeBytes(content, 0);
		return true;
	}

	private boolean decodeTandyPnt(byte[] content, int contentLength)
	{
		if (contentLength < 236 || content[0] != 19 || content[1] != 80 || content[2] != 78 || content[3] != 84)
			return false;
		setSize(312, 176, RECOILResolution.TANDY1X1);
		if (contentLength == 27478) {
			System.arraycopy(DECODE_TANDY_PNT_PALETTE, 0, this.contentPalette, 0, 16);
			decodeNibbles(content, 22, 156);
			return true;
		}
		int contentOffset = 22;
		int repeatCount = 1;
		int repeatValue = 0;
		for (int pixelsOffset = 0; pixelsOffset < 54912; pixelsOffset += 2) {
			if (--repeatCount == 0) {
				if (contentOffset + 1 >= contentLength)
					return false;
				repeatCount = content[contentOffset + 1] & 0xff;
				if (repeatCount == 0)
					return false;
				repeatValue = content[contentOffset] & 0xff;
				contentOffset += 2;
			}
			this.pixels[pixelsOffset] = DECODE_TANDY_PNT_PALETTE[repeatValue >> 4];
			this.pixels[pixelsOffset + 1] = DECODE_TANDY_PNT_PALETTE[repeatValue & 15];
		}
		return contentOffset == contentLength;
	}

	private boolean decodeHs2(byte[] content, int contentLength)
	{
		if (contentLength % 105 != 0)
			return false;
		return setSize(840, contentLength / 105, RECOILResolution.PC1X1) && decodeBlackAndWhite(content, 0, contentLength, false, 0);
	}

	private boolean decodeImage72Fnt(byte[] content, int contentLength)
	{
		if (contentLength < 4 || content[0] != 0 || content[1] != 8)
			return false;
		int fontHeight = content[2] & 0xff;
		if (contentLength != 3 + (fontHeight << 8))
			return false;
		setSize(256, fontHeight << 3, RECOILResolution.PC1X1);
		decodeBlackAndWhiteFont(content, 3, contentLength, fontHeight);
		return true;
	}

	private boolean decodeMsp(byte[] content, int contentLength)
	{
		if (contentLength < 32)
			return false;
		int width = content[4] & 0xff | (content[5] & 0xff) << 8;
		int height = content[6] & 0xff | (content[7] & 0xff) << 8;
		if (isStringAt(content, 0, "DanM"))
			return setSize(width, height, RECOILResolution.PC1X1) && decodeBlackAndWhite(content, 32, contentLength, false, 0);
		if (isStringAt(content, 0, "LinS") && setSize(width, height, RECOILResolution.PC1X1)) {
			final MspStream rle = new MspStream();
			rle.content = content;
			rle.contentOffset = 32 + (height << 1);
			rle.contentLength = contentLength;
			return decodeRleBlackAndWhite(rle, 0);
		}
		return false;
	}

	private boolean decodeAwbmPalette(byte[] content, int contentLength, int paletteOffset, int colors)
	{
		if (contentLength < paletteOffset + 4 + colors * 3 || !isStringAt(content, paletteOffset, "RGB "))
			return false;
		for (int i = 0; i < colors; i++) {
			int rgb = getR8G8B8Color(content, paletteOffset + 4 + i * 3);
			this.contentPalette[i] = (rgb & 4144959) << 2 | (rgb >> 4 & 197379);
		}
		return true;
	}

	private boolean decodeAwbm(byte[] content, int contentLength)
	{
		int width = content[4] & 0xff | (content[5] & 0xff) << 8;
		int height = content[6] & 0xff | (content[7] & 0xff) << 8;
		int planeStride = (width + 7) >> 3;
		boolean colors256;
		if (decodeAwbmPalette(content, contentLength, 8 + width * height, 256))
			colors256 = true;
		else if (decodeAwbmPalette(content, contentLength, 8 + (height * planeStride << 2), 16))
			colors256 = false;
		else
			return false;
		if (!setSize(width, height, RECOILResolution.PC1X1))
			return false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int c;
				if (colors256)
					c = content[8 + y * width + x] & 0xff;
				else {
					int offset = 8 + (y * planeStride << 2) + (x >> 3);
					c = 0;
					for (int bit = 0; bit < 4; bit++) {
						c |= ((content[offset] & 0xff) >> (~x & 7) & 1) << bit;
						offset += planeStride;
					}
				}
				this.pixels[y * width + x] = this.contentPalette[c];
			}
		}
		return true;
	}

	private static final int[] CGA_PALETTE = { 0, 170, 43520, 43690, 11141120, 11141290, 11162880, 11184810, 5592405, 5592575, 5635925, 5636095, 16733525, 16733695, 16777045, 16777215 };

	private boolean decodeEpa(byte[] content, int contentLength)
	{
		if (contentLength < 17)
			return false;
		if (isStringAt(content, 0, "AWBM"))
			return decodeAwbm(content, contentLength);
		int columns = content[0] & 0xff;
		int rows = content[1] & 0xff;
		if (columns > 80 || rows > 25 || contentLength != 2 + columns * rows * 15 + 70)
			return false;
		int width = columns * 8;
		int height = rows * 14;
		setSize(width, height, RECOILResolution.PC1X1);
		int bitmapOffset = 2 + columns * rows;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int ch = y / 14 * columns + (x >> 3);
				int attribute = content[2 + ch] & 0xff;
				int b = (content[bitmapOffset + ch * 14 + y % 14] & 0xff) >> (~x & 7) & 1;
				this.pixels[y * width + x] = CGA_PALETTE[b == 0 ? attribute >> 4 : attribute & 15];
			}
		}
		return true;
	}

	private void decodeBkMono(byte[] content, int height)
	{
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < 512; x++) {
				int c = (content[(y >> 1 << 6) + (x >> 3)] & 0xff) >> (x & 7) & 1;
				if (c != 0)
					c = 16777215;
				this.pixels[(y << 9) + x] = c;
			}
		}
	}

	private void decodeBkColorFrame(byte[] content, int paletteOffset, int frame)
	{
		paletteOffset <<= 2;
		for (int i = 0; i < 65536; i++) {
			this.pixels[(frame << 16) + i] = DECODE_BK_COLOR_FRAME_PALETTE[paletteOffset + ((content[(frame << 14) + (i >> 2)] & 0xff) >> ((i & 3) << 1) & 3)];
		}
	}

	private boolean decodeBkColor(byte[] content, int palette)
	{
		setSize(256, 256, RECOILResolution.BK1X1);
		decodeBkColorFrame(content, palette, 0);
		return true;
	}

	private boolean decodeBks(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 16384:
			if (!setSize(512, 512, RECOILResolution.BK1X2))
				return false;
			decodeBkMono(content, 512);
			return true;
		case 16385:
			int palette = content[16384] & 0xff;
			if (palette > 15)
				return false;
			return decodeBkColor(content, palette);
		case 32768:
			if (!setSize(512, 512, RECOILResolution.BK1X2, 2))
				return false;
			decodeBkMono(content, 1024);
			return applyBlend();
		case 32770:
			int palette1 = content[32768] & 0xff;
			int palette2 = content[32769] & 0xff;
			if (palette1 > 15 || palette2 > 15)
				return false;
			setSize(256, 256, RECOILResolution.BK1X1, 2);
			decodeBkColorFrame(content, palette1, 0);
			decodeBkColorFrame(content, palette2, 1);
			return applyBlend();
		default:
			return false;
		}
	}

	private boolean decodeVectorSpr(byte[] content, int contentLength)
	{
		final VectorSprStream rle = new VectorSprStream();
		rle.content = content;
		rle.contentOffset = contentLength;
		final byte[] unpacked = new byte[32768];
		if (!rle.unpack(unpacked, 0, 1, 32768))
			return false;
		for (int i = 0; i < 16; i++) {
			int c = content[i] & 0xff;
			this.contentPalette[i] = (c & 7) * 73 >> 1 << 16 | (c >> 3 & 7) * 72 >> 1 << 8 | (c >> 6) * 85;
		}
		setSize(256, 256, RECOILResolution.VECTOR1X1);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++)
				this.pixels[(y << 8) + x] = this.contentPalette[getBitplanePixel(unpacked, (31 - (x >> 3)) << 8 | y, x, 4, 8192)];
		}
		return true;
	}

	private boolean decodePi9(byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 7684:
		case 7808:
		case 7936:
			return decodeGr9(content, 7680);
		case 7720:
			return decodeApc(content, contentLength);
		default:
			return decodeFuckpaint(content, contentLength);
		}
	}

	private boolean decodePic(byte[] content, int contentLength)
	{
		if (decodePsion3Pic(content, contentLength) || decodeX68KPic(content, contentLength) || decodeAtari8Koala(content, 0, contentLength))
			return true;
		switch (contentLength) {
		case 3325:
			return decodeVisualizer(content);
		case 4325:
			return decodeGad(content, contentLength);
		case 7680:
			return decodeGr8(content, contentLength);
		case 7681:
		case 7682:
		case 7683:
		case 7684:
		case 7685:
			return decodeMic(null, content, contentLength);
		case 16384:
			return decodeBkColor(content, 0);
		case 32000:
			return decodeDoo(content, contentLength);
		case 32768:
			return decodeAppleIIShr(content, contentLength);
		default:
			return decodeStPi(content, contentLength) || decodeSc8(null, content, contentLength);
		}
	}

	private boolean decodeScr(String filename, byte[] content, int contentLength)
	{
		switch (contentLength) {
		case 960:
			return decodeGr0(content, contentLength);
		case 1002:
			return decodeScrCol(filename, content, contentLength);
		case 6144:
			setZx(RECOILResolution.SPECTRUM1X1);
			decodeZx(content, 0, -1, -3, 0);
			return true;
		case 6912:
		case 6913:
			setZx(RECOILResolution.SPECTRUM1X1);
			decodeZx(content, 0, 6144, 3, 0);
			return true;
		case 6976:
			setUlaPlus(content, 6912);
			decodeZx(content, 0, 6144, 3, 0);
			return true;
		case 12288:
			setZx(RECOILResolution.TIMEX1X1);
			decodeZx(content, 0, 6144, -1, 0);
			return true;
		case 12289:
			setSize(512, 384, RECOILResolution.TIMEX1X2);
			decodeTimexHires(content, 0, 0);
			return true;
		case 12352:
			setUlaPlus(content, 12288);
			decodeZx(content, 0, 6144, -1, 0);
			return true;
		case 16000:
			setSize(640, 400, RECOILResolution.MC05151X2);
			this.contentPalette[0] = 0;
			this.contentPalette[1] = 16777215;
			decodeScaledBitplanes(content, 0, 640, 200, 1, false, null);
			return true;
		case 32768:
			return decodeAppleIIShr(content, contentLength);
		default:
			return decodeAmstradScr(filename, content, contentLength);
		}
	}

	private boolean decodeFlfFont(byte[] content, int contentOffset, int contentLength, int columns, int rows, int resolution, int[] palette, int colors, int xMask, int cMask)
	{
		if (contentLength != contentOffset + columns * rows * 12)
			return false;
		int width = columns << 3;
		int height = rows << 3;
		setSize(width, height, resolution);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = contentOffset + ((y >> 3) * columns + (x >> 3)) * 12;
				int c = (content[offset + (y & 7)] & 0xff) >> (x & xMask) & cMask;
				c = content[offset + 8 + c] & 0xff;
				if (c >= colors)
					return false;
				this.pixels[y * width + x] = palette[c];
			}
		}
		return true;
	}

	private boolean decodeFlfBytes(byte[] content, int contentLength)
	{
		int colorsOffset = 14 + 320 * this.height;
		if (contentLength < colorsOffset + 6)
			return false;
		int colors = content[colorsOffset - 1] & 0xff;
		if (colors == 0)
			colors = 256;
		switch (contentLength - colorsOffset - colors * 3) {
		case 0:
		case 256:
			break;
		default:
			return false;
		}
		decodeR8G8B8Colors(content, colorsOffset, colors, this.contentPalette, 0);
		decodeBytes(content, 13);
		return true;
	}

	private boolean decodeFlf(byte[] content, int contentLength)
	{
		if (contentLength < 20 || !isStringAt(content, 0, "FLUFF64"))
			return false;
		Arrays.fill(this.contentPalette, 0);
		switch (content[11]) {
		case 1:
			return decodeFlfFont(content, 15, contentLength, 40, 25, RECOILResolution.C642X1, this.c64Palette, 16, 6, 3);
		case 4:
		case 5:
			return decodeFlfFont(content, 18, contentLength, 40, 25, RECOILResolution.C642X1, this.c64Palette, 16, 6, 3);
		case 6:
			return decodeFlfFont(content, 18, contentLength, 40, 25, RECOILResolution.C641X1, this.c64Palette, 16, 7, 1);
		case 7:
			int columns = content[15] & 0xff;
			int rows = content[16] & 0xff;
			int length = columns * rows;
			return contentLength >= 45 + (length << 1) && decodePetScreen(content, 29 + length, 29, 13, columns, rows);
		case 9:
			if (content[12] != 6)
				return false;
			return decodeFlfFont(content, 20, contentLength, content[18] & 0xff, content[19] & 0xff, RECOILResolution.VIC202X1, VIC20_PALETTE, 8, 6, 3);
		case 11:
			if (contentLength != 64269)
				return false;
			int c;
			switch (content[12]) {
			case 2:
				c = 1;
				break;
			case 3:
				c = 9;
				break;
			case 4:
				c = 0;
				break;
			case 5:
				c = 8;
				break;
			default:
				return false;
			}
			this.contentPalette[1] = CGA_PALETTE[c + 2];
			this.contentPalette[2] = CGA_PALETTE[c + 4];
			this.contentPalette[3] = CGA_PALETTE[c + 6];
			setSize(320, 200, RECOILResolution.PC1X1);
			decodeBytes(content, 13);
			return true;
		case 12:
			setSize(320, 200, RECOILResolution.AMIGA1X1);
			return decodeFlfBytes(content, contentLength);
		case 13:
			setSize(320, 256, RECOILResolution.AMIGA1X1);
			return decodeFlfBytes(content, contentLength);
		case 22:
			setSize(320, 200, RECOILResolution.ST1X1);
			return decodeFlfBytes(content, contentLength);
		case 24:
			if (content[12] != 11 || contentLength != 32269)
				return false;
			setAmstradFirmwarePalette(content, 32205, 16);
			setSize(320, 200, RECOILResolution.AMSTRAD2X1);
			decodeBytes(content, 13);
			return true;
		case 25:
			if (content[12] != 11 || contentLength < 282)
				return false;
			int width = get32LittleEndian(content, 13);
			int height = get32LittleEndian(content, 17);
			if (get32LittleEndian(content, 21) != 0 || !setSize(width << 1, height, RECOILResolution.AMSTRAD2X1) || contentLength != 281 + width * height)
				return false;
			setAmstradFirmwarePalette(content, contentLength - 64, 16);
			decodeBytes(content, 25);
			return true;
		case 26:
			if (content[12] != 12)
				return false;
			switch (content[13]) {
			case 4:
				if (contentLength != 82190)
					return false;
				this.contentPalette[1] = 16777215;
				setSize(320, 256, RECOILResolution.BBC1X1);
				decodeBytes(content, 13);
				return true;
			case 5:
				if (contentLength != 41230)
					return false;
				System.arraycopy(BBC_PALETTE2_BIT, 0, this.contentPalette, 0, 4);
				setSize(320, 256, RECOILResolution.BBC2X1);
				decodeBytes(content, 13);
				return true;
			default:
				return false;
			}
		case 27:
			setSize(320, 200, RECOILResolution.PC1X1);
			return decodeFlfBytes(content, contentLength);
		case 28:
			if (content[12] != 14 || contentLength < 49165)
				return false;
			setZx(RECOILResolution.SPECTRUM1X1);
			decodeBytes(content, 13);
			return true;
		default:
			return false;
		}
	}

	/**
	 * Checks whether the filename extension is supported by RECOIL.
	 * <code>true</code> doesn't necessarily mean that the file contents is valid for RECOIL.
	 * With this function you can avoid reading files which are known to be unsupported.
	 * @param filename Name of the file to be checked.
	 */
	public static boolean isOurFile(String filename)
	{
		switch (getPackedExt(filename)) {
		case 540423474:
		case 538976307:
		case 825242163:
		case 544498228:
		case 543780148:
		case 543977524:
		case 544043060:
		case 543372342:
		case 538976353:
		case 544355425:
		case 540292705:
		case 543648119:
		case 544432481:
		case 543715433:
		case 543519336:
		case 543908449:
		case 544432993:
		case 543975009:
		case 544237409:
		case 544434017:
		case 543976545:
		case 543780193:
		case 540175969:
		case 540307041:
		case 540372577:
		case 540176481:
		case 540242017:
		case 544632929:
		case 543778660:
		case 544237412:
		case 543388517:
		case 543386729:
		case 544045680:
		case 543256673:
		case 543387745:
		case 544042096:
		case 543977569:
		case 544239713:
		case 544436321:
		case 544502369:
		case 544633441:
		case 544371809:
		case 544679522:
		case 544702306:
		case 540041826:
		case 540107362:
		case 540172898:
		case 540303970:
		case 540369506:
		case 543646306:
		case 544236642:
		case 543975010:
		case 1768711778:
		case 540632930:
		case 543975778:
		case 1937076834:
		case 538996841:
		case 1952672112:
		case 540618855:
		case 544237410:
		case 543648610:
		case 544435042:
		case 540109922:
		case 540175458:
		case 540240994:
		case 543452258:
		case 543976802:
		case 543648870:
		case 538996834:
		case 543977570:
		case 544567906:
		case 538997602:
		case 538993255:
		case 538994544:
		case 543388514:
		case 878931298:
		case 544240482:
		case 540107107:
		case 540172643:
		case 540238179:
		case 543777635:
		case 544564323:
		case 540108131:
		case 540173667:
		case 540239203:
		case 543974755:
		case 1768711779:
		case 539256931:
		case 540305507:
		case 540436579:
		case 540567651:
		case 543516771:
		case 544368739:
		case 544434275:
		case 544761955:
		case 544106851:
		case 543517795:
		case 544238691:
		case 540372323:
		case 544238947:
		case 540242019:
		case 543780963:
		case 544370787:
		case 544501859:
		case 543650403:
		case 544044131:
		case 544503139:
		case 543651683:
		case 540303716:
		case 544235876:
		case 540107620:
		case 538993764:
		case 544236644:
		case 544499048:
		case 543715442:
		case 543974756:
		case 540108644:
		case 1919379556:
		case 544368740:
		case 544106852:
		case 544500068:
		case 544042084:
		case 543977316:
		case 543450466:
		case 543451510:
		case 544173924:
		case 543715428:
		case 543650404:
		case 543978084:
		case 544238692:
		case 544895588:
		case 544240228:
		case 540112228:
		case 544175460:
		case 540177764:
		case 543449701:
		case 543777637:
		case 544236389:
		case 543386981:
		case 543256677:
		case 543716197:
		case 544043877:
		case 543259237:
		case 540031078:
		case 543777382:
		case 544236390:
		case 544501862:
		case 540173414:
		case 543450470:
		case 543975014:
		case 1768711782:
		case 543516518:
		case 543583334:
		case 543779942:
		case 544042086:
		case 540175974:
		case 544501350:
		case 538996838:
		case 540176486:
		case 544370790:
		case 543388774:
		case 543978854:
		case 544109926:
		case 543258470:
		case 538976359:
		case 540029287:
		case 540094823:
		case 543568487:
		case 543308135:
		case 544422247:
		case 543450739:
		case 543450471:
		case 543319655:
		case 538994535:
		case 543647847:
		case 543648103:
		case 540372071:
		case 540437607:
		case 540503143:
		case 540568679:
		case 543255655:
		case 543386727:
		case 544435303:
		case 543453031:
		case 538997351:
		case 538997603:
		case 540045927:
		case 543388513:
		case 540111463:
		case 540176999:
		case 540242535:
		case 540504679:
		case 540570215:
		case 540635751:
		case 1882813031:
		case 543322727:
		case 544174695:
		case 543584871:
		case 544240231:
		case 538997607:
		case 1936157033:
		case 543896115:
		case 544109927:
		case 544039528:
		case 544434022:
		case 544108397:
		case 543449959:
		case 543318888:
		case 544039784:
		case 543450472:
		case 543385192:
		case 543450728:
		case 543319912:
		case 544368488:
		case 544041320:
		case 544237928:
		case 544369000:
		case 543517800:
		case 543583336:
		case 543517032:
		case 544369768:
		case 543387752:
		case 543780968:
		case 544043112:
		case 544436328:
		case 538997352:
		case 540177000:
		case 543777640:
		case 543650408:
		case 544043624:
		case 544436840:
		case 540177256:
		case 540238441:
		case 543777385:
		case 540107625:
		case 540173161:
		case 540238697:
		case 543515497:
		case 544105321:
		case 543581801:
		case 1835164513:
		case 1835099490:
		case 544498532:
		case 1987339108:
		case 1885693284:
		case 538997348:
		case 544039272:
		case 913138024:
		case 946692456:
		case 544039532:
		case 1835166825:
		case 538996845:
		case 945973106:
		case 1851942770:
		case 1835100275:
		case 543975017:
		case 543516521:
		case 543516777:
		case 544041321:
		case 543452265:
		case 543517801:
		case 544435305:
		case 543649129:
		case 1735223668:
		case 1735223672:
		case 544107881:
		case 1868983913:
		case 543649385:
		case 544239209:
		case 544435817:
		case 544501353:
		case 540176489:
		case 543387753:
		case 544501865:
		case 543519340:
		case 540177001:
		case 543650409:
		case 543716201:
		case 544043881:
		case 544502633:
		case 544237418:
		case 538995306:
		case 544761451:
		case 543451499:
		case 543255659:
		case 543256427:
		case 544043122:
		case 544370795:
		case 544437099:
		case 544040044:
		case 544171372:
		case 540242028:
		case 543912044:
		case 543912045:
		case 543912040:
		case 544044396:
		case 543383917:
		case 1735683696:
		case 543646061:
		case 543779693:
		case 544235885:
		case 544760173:
		case 543646317:
		case 538993517:
		case 543712109:
		case 543777645:
		case 544236397:
		case 1886413677:
		case 544433005:
		case 540108653:
		case 540174189:
		case 540305261:
		case 540567405:
		case 543254381:
		case 544237421:
		case 543385965:
		case 543648109:
		case 543975789:
		case 544434541:
		case 540109933:
		case 543517805:
		case 544500845:
		case 543977581:
		case 544239725:
		case 543978349:
		case 544240493:
		case 543585645:
		case 543782253:
		case 544241005:
		case 544372077:
		case 543782765:
		case 1852405613:
		case 540113005:
		case 544171374:
		case 540241006:
		case 544304238:
		case 543585646:
		case 544241006:
		case 543783022:
		case 543842927:
		case 544236399:
		case 543780973:
		case 1667854445:
		case 543581295:
		case 538976368:
		case 540094832:
		case 543372144:
		case 540095600:
		case 543765616:
		case 540292720:
		case 544828518:
		case 540238192:
		case 543383920:
		case 544760432:
		case 540107632:
		case 540173168:
		case 540238704:
		case 543777648:
		case 544433008:
		case 544498544:
		case 544367728:
		case 544499056:
		case 540108656:
		case 540174192:
		case 540239728:
		case 543385456:
		case 543582064:
		case 544368496:
		case 538995056:
		case 540109168:
		case 540174704:
		case 540240240:
		case 540371312:
		case 540436848:
		case 543716723:
		case 540305776:
		case 540502384:
		case 540567920:
		case 540633456:
		case 543385968:
		case 811821424:
		case 544762224:
		case 540306544:
		case 543255664:
		case 544435312:
		case 543452528:
		case 543649136:
		case 544501360:
		case 538996848:
		case 543715440:
		case 544239728:
		case 543388528:
		case 543585136:
		case 538981489:
		case 543646066:
		case 544235890:
		case 544694642:
		case 543319922:
		case 543713138:
		case 544237938:
		case 543517810:
		case 540044658:
		case 540110194:
		case 540175730:
		case 540241266:
		case 540306802:
		case 538996850:
		case 544174194:
		case 543713639:
		case 543717234:
		case 543979378:
		case 544438642:
		case 544366963:
		case 540044387:
		case 540109923:
		case 540175459:
		case 540043120:
		case 540042099:
		case 540107635:
		case 540173171:
		case 540238707:
		case 540304243:
		case 540369779:
		case 540370279:
		case 540435315:
		case 540500851:
		case 540501351:
		case 540566387:
		case 540566887:
		case 540570227:
		case 543253363:
		case 543384435:
		case 540042355:
		case 540107891:
		case 540173427:
		case 544630131:
		case 544436851:
		case 543910521:
		case 544367475:
		case 879977331:
		case 540308339:
		case 540239731:
		case 543516531:
		case 544761715:
		case 540108915:
		case 540174451:
		case 540239987:
		case 808464947:
		case 543385715:
		case 543516787:
		case 543582323:
		case 543778931:
		case 544237683:
		case 544368755:
		case 544761971:
		case 543582579:
		case 544238451:
		case 543387763:
		case 543453299:
		case 544370803:
		case 544436339:
		case 544567411:
		case 544764019:
		case 540373619:
		case 540439155:
		case 540504691:
		case 543781491:
		case 544502387:
		case 543322995:
		case 544764787:
		case 543978611:
		case 544240755:
		case 543651955:
		case 544438387:
		case 544236404:
		case 540108660:
		case 544041332:
		case 544237940:
		case 540306548:
		case 540110452:
		case 540175988:
		case 540241524:
		case 540307060:
		case 540372596:
		case 540438132:
		case 544829044:
		case 543780980:
		case 543519348:
		case 544240244:
		case 544567924:
		case 540047476:
		case 543520884:
		case 544438388:
		case 543975029:
		case 543582581:
		case 544039542:
		case 538996066:
		case 543778934:
		case 543385974:
		case 543388534:
		case 543783542:
		case 544106871:
		case 543452791:
		case 543975032:
		case 543254392:
		case 544238712:
		case 544041338:
		case 540306810:
		case 544042874:
		case 540110970:
		case 538997626:
		case 544241786:
		case 544438394:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Decodes a picture file to an RGB bitmap.
	 * Returns <code>true</code> on success.
	 * @param filename Name of the file to decode. Only the extension is processed, for format recognition.
	 * @param content File contents.
	 * @param contentLength File length.
	 */
	public final boolean decode(String filename, byte[] content, int contentLength)
	{
		switch (getPackedExt(filename)) {
		case 540423474:
			return decodeIff(content, contentLength, RECOILResolution.AMIGA1X1) || decode256(content, contentLength);
		case 538976307:
			return decode3(content, contentLength);
		case 825242163:
			return decode3201(content, contentLength);
		case 544498228:
			return decode4bt(content, contentLength);
		case 543780148:
			return decode4mi(content, contentLength);
		case 543977524:
			return decode4pl(content, contentLength);
		case 544043060:
			return decode4pm(content, contentLength);
		case 543372342:
			return decode64c(content, contentLength);
		case 538976353:
			return decodeA(content, contentLength);
		case 544355425:
			return decodeA4r(content, contentLength);
		case 540292705:
		case 543648119:
			return contentLength == 10242 && decodeC64Multicolor(content, 2, 8194, 9218, 10241);
		case 544432481:
		case 543715433:
		case 543519336:
			return decodeIph(content, contentLength);
		case 543908449:
			return decodeAbk(content, contentLength);
		case 544432993:
			return decodeAcs(content, contentLength);
		case 543975009:
			return decodeAfl(content, contentLength);
		case 544237409:
			return decodeAgp(content, contentLength);
		case 544434017:
			return decodeAgs(content, contentLength);
		case 543976545:
			return decodeAll(content, contentLength);
		case 543780193:
			return decodeAmi(content, contentLength);
		case 540175969:
			return decodeAn2(content, contentLength);
		case 540307041:
			return decodeAn4(content, contentLength, 0);
		case 540372577:
			return decodeAn4(content, contentLength, 1);
		case 540176481:
			return decode256(content, contentLength);
		case 540242017:
		case 544632929:
		case 543778660:
		case 544237412:
		case 543388517:
		case 543386729:
		case 544045680:
			return decodeAp3(content, contentLength);
		case 543256673:
		case 543387745:
		case 544042096:
			return decodeApc(content, contentLength);
		case 543977569:
			return decodeApl(content, contentLength);
		case 544239713:
			return decodeApp(content, contentLength);
		case 544436321:
			return decodeAps(content, contentLength);
		case 544502369:
			return decodeIph(content, contentLength) || decodeArtDirector(content, contentLength) || decodeGfaArtist(content, contentLength) || decodeDoo(content, contentLength) || decodePaletteMaster(content, contentLength) || decodeAtari8Artist(content, contentLength) || decodeMonoArt(content, contentLength) || decodeAsciiArtEditor(content, contentLength);
		case 544633441:
			return decodeArtMaster88(content, contentLength);
		case 544371809:
			return decodeAtr(content, contentLength);
		case 544679522:
		case 544702306:
			return decodeBw(content, contentLength);
		case 540041826:
			return decodeBb0(content, contentLength, BBC_PALETTE1_BIT);
		case 540107362:
			return decodeBb1(content, contentLength, BBC_PALETTE2_BIT);
		case 540172898:
			return decodeBb2(content, contentLength, BBC_PALETTE);
		case 540303970:
			return decodeBb4(content, contentLength, BBC_PALETTE1_BIT);
		case 540369506:
			return decodeBb5(content, contentLength, BBC_PALETTE2_BIT);
		case 543646306:
			return decodeBbg(content, contentLength);
		case 544236642:
			return decodeBdp(content, contentLength);
		case 543975010:
		case 1768711778:
			return decodeBfli(content, contentLength);
		case 540632930:
		case 540618855:
			return decodeG09(content, contentLength);
		case 544237410:
			return decodeBgp(content, contentLength);
		case 543975778:
			return decodeBil(content, contentLength);
		case 543648610:
			return decodeBkg(content, contentLength);
		case 544435042:
			return decodeBks(content, contentLength);
		case 1937076834:
		case 538996841:
		case 1952672112:
			return decodeBrus(content, contentLength);
		case 540109922:
		case 540175458:
		case 540240994:
			return decodeIff(content, contentLength, RECOILResolution.ST1X1);
		case 543452258:
			return decodeBld(content, contentLength);
		case 543976802:
		case 543648870:
			return decodeBml(content, contentLength);
		case 538996834:
			return decodeBp(content, contentLength);
		case 543977570:
			return decodeBpl(content, contentLength);
		case 544567906:
			return decodeBru(content, contentLength);
		case 538997602:
		case 538993255:
		case 538994544:
			return decodePrintfox(content, contentLength);
		case 543388514:
		case 878931298:
			return decodeBsc(content, contentLength);
		case 544240482:
			return decodeBsp(content, contentLength);
		case 540107107:
		case 540172643:
		case 540238179:
			return decodeCa(content, contentLength);
		case 543777635:
			return decodeCci(content, contentLength);
		case 544564323:
			return contentLength == 10277 && decodeC64Multicolor(content, 275, 8275, 9275, 10275);
		case 540108131:
		case 540173667:
		case 540239203:
			return decodeCe(content, contentLength);
		case 543974755:
			return decodeCel(content, contentLength);
		case 1768711779:
			return decodeCfli(content, contentLength);
		case 539256931:
			return decodeChrd(content, contentLength);
		case 540305507:
		case 540436579:
		case 540567651:
			return decodeCh8(content, contentLength);
		case 543516771:
			return contentLength == 20482 && decodeC64Multicolor(content, 2, 16898, 18434, 20479);
		case 544368739:
			return decodeChr(content, contentLength);
		case 544434275:
			return decodeChs(content, contentLength);
		case 544761955:
			return decodeChx(content, contentLength);
		case 544106851:
			return decodeCin(content, contentLength);
		case 543517795:
			return decodeCle(content, contentLength);
		case 544238691:
			return decodeGodotClp(content, contentLength) || decodeCocoClp(content, contentLength);
		case 540372323:
			return decodeCm5(filename, content, contentLength);
		case 544238947:
			return decodeDdGraph(filename, content, contentLength) || decodeStCmp(content, contentLength);
		case 540242019:
			return decodeCp3(content, contentLength);
		case 543780963:
			return decodeCpi(content, contentLength);
		case 544370787:
			return decodeCpr(content, contentLength);
		case 544501859:
			return decodeCpt(filename, content, contentLength);
		case 543650403:
			return decodeCrg(content, contentLength);
		case 544044131:
			return decodeCtm(content, contentLength);
		case 544503139:
			return decodeGr8Raw(content, contentLength, 96, 99);
		case 543651683:
			return contentLength == 10007 && decodeC64Multicolor(content, 2, 8002, 9002, 10003);
		case 540303716:
			return decodeDa4(content, contentLength);
		case 544235876:
			return decodeDap(content, contentLength);
		case 540107620:
			return decodeDc1(content, contentLength);
		case 538993764:
		case 544236644:
		case 544499048:
		case 543715442:
			return decodeDd(content, contentLength);
		case 543974756:
			return decodeDel(content, contentLength);
		case 540108644:
			return decodeDg1(content, contentLength);
		case 1919379556:
			return decodeAppleIIDhr(content, contentLength);
		case 544368740:
			return decodeIff(content, contentLength, RECOILResolution.AMIGA1X1) || decodeAppleIIDhr(content, contentLength);
		case 544106852:
			return decodeAtari8Ice(content, contentLength, false, 3);
		case 544500068:
			return decodeDit(content, contentLength);
		case 544042084:
			return decodeDlm(content, contentLength);
		case 543977316:
		case 543450466:
		case 543451510:
			return decodeDol(content, contentLength);
		case 544173924:
			return decodeDoo(content, contentLength);
		case 543715428:
			return decodeDph(content, contentLength);
		case 543650404:
			return decodeDrg(content, contentLength);
		case 543978084:
		case 544238692:
			return decodeDrl(content, contentLength);
		case 544895588:
		case 544240228:
			return decodeDrz(content, contentLength);
		case 540112228:
		case 544175460:
			return decodeDuo(content, contentLength);
		case 540177764:
			return decodeDu2(content, contentLength);
		case 543449701:
			return decodeEbd(content, contentLength);
		case 543777637:
			return decodeEci(content, contentLength);
		case 544236389:
			return decodeEcp(content, contentLength);
		case 543386981:
			return decodeEmc(content, contentLength);
		case 543256677:
			return decodeEpa(content, contentLength);
		case 543716197:
			return decodeEsh(content, contentLength);
		case 544043877:
			return decodeEsm(content, contentLength);
		case 543259237:
			return decodeEza(content, contentLength);
		case 540031078:
			return decodeF80(content, contentLength);
		case 543777382:
			return decodeFbi(content, contentLength);
		case 544236390:
		case 544501862:
			return contentLength == 10004 && decodeC64Multicolor(content, 2, 8002, 9002, 10002);
		case 540173414:
			return decodeFli(content, contentLength);
		case 543450470:
			return decodeFed(content, contentLength);
		case 543975014:
		case 1768711782:
			return decodeFfli(content, contentLength);
		case 543516518:
			return decodeFge(content, contentLength);
		case 543583334:
			return decodeFlf(content, contentLength);
		case 543779942:
			return decodeFli(content, contentLength) || decodeBml(content, contentLength);
		case 544042086:
			return decodeFlm(content, contentLength);
		case 540175974:
			return decodeFn2(content, contentLength);
		case 544501350:
			return decodePct(content, contentLength) || decodeGdosFnt(content, contentLength) || decodeAtari8Fnt(content, contentLength) || decodeStFnt(content, contentLength) || decodeAmstradFnt(content, contentLength) || decodeImage72Fnt(content, contentLength);
		case 538996838:
			return decodeFp(content, contentLength);
		case 540176486:
			return decodeC64Fun(content, contentLength);
		case 544370790:
			return decodeFpr(content, contentLength);
		case 543388774:
			return decodeFtc(content, contentLength);
		case 543978854:
			return decodeFul(content, contentLength);
		case 544109926:
			return decodeC64Fun(content, contentLength) || decodeFalconFun(content, contentLength);
		case 543258470:
			return decodeFwa(content, contentLength);
		case 538976359:
			return decodeG(content, contentLength);
		case 540029287:
			return decodeG10(content, contentLength);
		case 540094823:
			return decodeG11(content, contentLength);
		case 543568487:
			return decodeG2f(content, contentLength);
		case 543308135:
			return decodeG9b(content, contentLength);
		case 544422247:
		case 543450739:
			return decodeG9s(content, contentLength);
		case 543450471:
			return decodeGed(content, contentLength);
		case 543319655:
			return decodeGfb(content, contentLength);
		case 538994535:
			return decodeGg(content, contentLength);
		case 543647847:
			return decodeGhg(content, contentLength);
		case 543648103:
			return decodeIph(content, contentLength) || decodeKoa(content, contentLength);
		case 540372071:
			return decodeGl5(filename, content, contentLength);
		case 540437607:
			return decodeGl6(filename, content, contentLength);
		case 540503143:
			return decodeGl7(filename, content, contentLength);
		case 540568679:
			return decodeGl8(content, contentLength);
		case 543255655:
			return decodeGlYjk(filename, content, contentLength);
		case 543386727:
		case 544435303:
			return decodeGlYjk(null, content, contentLength);
		case 543453031:
			return decodeGod(content, contentLength);
		case 538997351:
		case 538997603:
			return decodeGr(content, contentLength);
		case 540045927:
		case 543388513:
			return decodeGr0(content, contentLength);
		case 540111463:
			return decodeGr1(content, contentLength, 0);
		case 540176999:
			return decodeGr1(content, contentLength, 1);
		case 540242535:
			return decodeGr3(content, contentLength);
		case 540504679:
			return decodeGr7(content, 0, contentLength);
		case 540570215:
			return decodeGr8(content, contentLength);
		case 540635751:
			return decodeGr9(content, contentLength);
		case 1882813031:
			return decodeGr9p(content, contentLength);
		case 543322727:
		case 544174695:
			return decodeGrb(content, contentLength);
		case 543584871:
			return decodeCocoMax(content, contentLength) || decodeProfiGrf(content, contentLength);
		case 544240231:
			return decodeSc2(content, contentLength);
		case 538997607:
		case 1936157033:
		case 543896115:
			return decodeApfShr(content, contentLength);
		case 544109927:
			return decodeGun(content, contentLength);
		case 544039528:
		case 544434022:
		case 544108397:
		case 543449959:
			return decodeC64Hir(content, contentLength);
		case 543318888:
			return decodeHcb(content, contentLength);
		case 544039784:
			return decodeHcm(content, contentLength);
		case 543450472:
			return contentLength == 9218 && decodeHed(content);
		case 543385192:
		case 543450728:
			return decodeHfc(content, contentLength);
		case 543319912:
			return decodeHgb(content, contentLength);
		case 544368488:
			return decodeHgr(content, contentLength);
		case 544041320:
			return decodeHim(content, contentLength);
		case 544237928:
			return decodeHip(content, contentLength);
		case 544369000:
			return decodeFalconHir(content, contentLength) || decodeC64Hir(content, contentLength) || decodeHrs(content, contentLength);
		case 543517800:
			return decodeHle(content, contentLength);
		case 543583336:
		case 543517032:
			return decodeHlf(content, contentLength);
		case 544369768:
			return decodeHlr(content, contentLength);
		case 543387752:
			return decodeIph(content, contentLength);
		case 543780968:
			return decodeC64Hir(content, contentLength) || decodeIph(content, contentLength);
		case 544043112:
			return decodeHpm(content, contentLength);
		case 544436328:
			return decodeHps(content, contentLength);
		case 538997352:
			return decodeTrsHr(content, contentLength) || decodeAtari8Hr(content, contentLength);
		case 540177000:
		case 543777640:
			return decodeHr2(content, contentLength);
		case 543650408:
			return decodeHrg(content, contentLength);
		case 544043624:
			return decodeHrm(content, contentLength);
		case 544436840:
			return decodeHrs(content, contentLength);
		case 540177256:
			return decodeHs2(content, contentLength);
		case 540238441:
		case 543777385:
			return decodeIbi(content, contentLength);
		case 540107625:
		case 540173161:
		case 540238697:
			return decodeIc(content, contentLength);
		case 543515497:
			return contentLength > 1024 && decodeAtari8Ice(content, contentLength, true, content[0] & 0xff);
		case 544105321:
			return decodeStIcn(content, contentLength) || decodePsion3Pic(content, contentLength) || decodeAtari8Ice(content, contentLength, false, 17);
		case 543581801:
		case 1835164513:
		case 1835099490:
		case 544498532:
		case 1987339108:
		case 1885693284:
		case 538997348:
		case 544039272:
		case 913138024:
		case 946692456:
		case 544039532:
		case 1835166825:
		case 538996845:
		case 945973106:
		case 1851942770:
		case 1835100275:
			return decodeIff(content, contentLength, RECOILResolution.AMIGA1X1);
		case 543975017:
			return decodeGun(content, contentLength) || decodeZxIfl(content, contentLength);
		case 543516521:
			return decodeIge(content, contentLength);
		case 543516777:
			return decodeIhe(content, contentLength);
		case 544041321:
			return decodeIim(content, contentLength);
		case 543452265:
			return decodeIld(content, contentLength);
		case 543517801:
			return decodeIle(content, contentLength);
		case 544435305:
			return decodeIls(content, contentLength);
		case 0x20676D69: // ' gmi'
			return decodeStImg(content, contentLength) || decodeZxImg(content, contentLength) || decodeArtMaster88(content, contentLength) || decodeDaVinci(content, contentLength);
		case 1735223668:
		case 1735223672:
			return decodeStImg(content, contentLength);
		case 544107881:
			return decodeAtari8Ice(content, contentLength, false, 18);
		case 1868983913:
			return decodeInfo(content, contentLength);
		case 543649385:
		case 544239209:
			return decodeInp(content, contentLength);
		case 544435817:
			return decodeIns(content, contentLength);
		case 544501353:
			return decodeInt(content, contentLength) || decodeInp(content, contentLength);
		case 540176489:
			return decodeIp2(content, contentLength);
		case 543387753:
			return decodeAtari8Ice(content, contentLength, false, 19);
		case 544501865:
		case 543519340:
			return contentLength == 10003 && decodeC64Multicolor(content, 2, 8002, 9002, 10002);
		case 540177001:
			return decodeAtari8Ice(content, contentLength, false, 2);
		case 543650409:
			return decodeAtari8Ice(content, contentLength, false, 1);
		case 543716201:
			return decodeIsh(content, contentLength);
		case 544043881:
			return contentLength == 10218 && decodeC64Multicolor(content, 1026, 9218, 2, 9217);
		case 544502633:
			return decodeIst(content, contentLength);
		case 544237418:
			return decodeJgp(content, contentLength);
		case 538995306:
			return decodeJj(content, contentLength);
		case 544761451:
			return decodeGr8Raw(content, contentLength, 56, 60);
		case 543451499:
			return decodeKid(content, contentLength);
		case 543255659:
		case 543256427:
		case 544043122:
			return decodeKoa(content, contentLength);
		case 544370795:
			return decodeKpr(content, contentLength);
		case 544437099:
			return decodeKss(content, contentLength);
		case 544040044:
			return decodeLdm(content, contentLength);
		case 544171372:
			return decodeLeo(content, contentLength);
		case 540242028:
			return decodeLp3(content, contentLength);
		case 543912044:
			return decodeDaliCompressed(content, contentLength, 0);
		case 543912045:
			return decodeDaliCompressed(content, contentLength, 1);
		case 543912040:
			return decodeDaliCompressed(content, contentLength, 2);
		case 544044396:
			return decodeLum(filename, content, contentLength);
		case 543383917:
		case 1735683696:
			return decodeMac(content, contentLength);
		case 543646061:
		case 543779693:
			return decodeMag(content, contentLength);
		case 544235885:
			return decodeEnvision(content, contentLength) || decodeEnvisionPC(content, contentLength);
		case 544760173:
			return decodeMag(content, contentLength) || decodeAtari8Max(content, contentLength) || decodeCocoMax(content, contentLength);
		case 543646317:
			return decodeGr8Raw(content, contentLength, 512, 256);
		case 538993517:
			return decodeMcMlt(content, contentLength, -1);
		case 543712109:
			return decodeMch(content, contentLength);
		case 543777645:
			return decodeMci(content, contentLength);
		case 544236397:
			return decodeMcp(content, contentLength);
		case 1886413677:
			return decodeMcpp(content, contentLength);
		case 544433005:
			return decodeMcs(content, contentLength);
		case 540108653:
		case 540174189:
		case 540305261:
		case 540567405:
			return decodeMg(content, contentLength);
		case 543254381:
			return decodeMga(content, contentLength);
		case 544237421:
			return decodeMgp(content, contentLength);
		case 543385965:
			return decodeMic(filename, content, contentLength);
		case 543648109:
			return decodeMig(content, contentLength);
		case 543975789:
			return decodeMil(content, contentLength);
		case 544434541:
			return decodeMis(content, contentLength);
		case 540109933:
			return decodeMl1(content, contentLength);
		case 543517805:
			return decodeMle(content, contentLength);
		case 544500845:
			return decodeMcMlt(content, contentLength, 0);
		case 543977581:
			return decodeMpl(content, contentLength);
		case 544239725:
			return decodeMpp(content, contentLength);
		case 543978349:
			return decodeMsl(content, contentLength);
		case 544240493:
			return decodeMsp(content, contentLength);
		case 543585645:
			return decodeMuf(content, contentLength);
		case 543782253:
			return decodeMui(content, contentLength);
		case 544241005:
			return decodeMup(content, contentLength);
		case 544372077:
			return decodeMur(filename, content, contentLength);
		case 543782765:
		case 1852405613:
			return decodeMwi(content, contentLength);
		case 540113005:
			return decodeMx1(content, contentLength);
		case 544171374:
			return decodeNeo(filename, content, contentLength) || decodeIff(content, contentLength, RECOILResolution.STE1X1);
		case 540241006:
			return decodeNl3(content, contentLength);
		case 544304238:
			return decodeNlq(content, contentLength);
		case 543585646:
			return decodeNuf(content, contentLength);
		case 544241006:
			return decodeNup(content, contentLength);
		case 543783022:
			return decodeNxi(content, contentLength);
		case 543842927:
			return decodeObj(content, contentLength);
		case 544236399:
		case 543780973:
		case 1667854445:
			return decodeOcp(content, contentLength);
		case 543581295:
			return decodeOdf(content, contentLength);
		case 538976368:
			return decodeP(content, contentLength);
		case 540094832:
			return decodeP11(content, contentLength);
		case 543372144:
			return decodeP3c(content, contentLength);
		case 540095600:
			return decodeCocoMax(content, contentLength);
		case 543765616:
			return decodeP4i(content, contentLength);
		case 540292720:
		case 544828518:
			return contentLength == 10050 && decodeC64Multicolor(content, 2050, 1026, 2, 2049);
		case 540238192:
			return decodeStPpp(content, contentLength);
		case 543383920:
			return decodePac(content, contentLength);
		case 544760432:
			return decodePbx(content, contentLength);
		case 540107632:
		case 540173168:
		case 540238704:
			return decodePc(content, contentLength);
		case 543777648:
			return decodePci(content, contentLength);
		case 544433008:
			return decodePcs(content, contentLength);
		case 544498544:
			return decodePct(content, contentLength);
		case 544367728:
			return decodePdr(content, contentLength);
		case 544499056:
			return decodePet(content, contentLength);
		case 540108656:
		case 540174192:
			return decodeSc(content, contentLength) || decodeGraphicsProcessor(content, contentLength);
		case 540239728:
			return decodeGraphicsProcessor(content, contentLength);
		case 543385456:
			return decodePgc(content, contentLength);
		case 543582064:
			return decodePgf(content, contentLength);
		case 544368496:
			return decodePgr(content, contentLength);
		case 538995056:
			return decodePi(content, contentLength) || decodeBpl(content, contentLength);
		case 540109168:
		case 540174704:
		case 540240240:
		case 540371312:
		case 540436848:
		case 543716723:
			return decodeStPi(content, contentLength);
		case 540305776:
			return decodeFuckpaint(content, contentLength) || decodeStPi(content, contentLength);
		case 540502384:
			return decodeFuckpaint(content, contentLength);
		case 540567920:
			return decodePi8(content, contentLength);
		case 540633456:
			return decodePi9(content, contentLength);
		case 543385968:
			return decodePic(content, contentLength);
		case 811821424:
			return decodePic0(filename, content, contentLength);
		case 544762224:
			return decodeFalconPix(content, contentLength) || decodeCocoMax(content, contentLength) || decodeAtari8Pix(content, contentLength);
		case 540306544:
			return decodePl4(content, contentLength);
		case 543255664:
			return decodePla(content, contentLength);
		case 544435312:
			return decodePls(content, contentLength);
		case 543452528:
			return decodePmd(content, contentLength);
		case 543649136:
			return decodePmg(content, contentLength);
		case 544501360:
			return decodeFalconPnt(content, contentLength) || decodeTandyPnt(content, contentLength) || decodeApfShr(content, contentLength) || decodeMac(content, contentLength) || decodeAppleIIShr(content, contentLength) || decodePaintworks(content, contentLength);
		case 538996848:
			return decodePp(content, contentLength);
		case 543715440:
			return decodePph(filename, content, contentLength);
		case 544239728:
			return decodeStPpp(content, contentLength) || decodePp(content, contentLength);
		case 543388528:
			return decodePsc(content, contentLength);
		case 543585136:
			return decodePsf(content, contentLength);
		case 538981489:
			return decodeQ4(content, contentLength);
		case 543646066:
			return decodeRag(content, contentLength);
		case 544235890:
			return decodeRap(content, contentLength);
		case 544694642:
			return decodeZx81Raw(content, contentLength) || decodeAtari8Raw(content, contentLength) || decodeRw(content, contentLength);
		case 543319922:
			return decodeStRgb(content, contentLength) || decodeAtari8Rgb(content, contentLength) || decodeZxRgb(content, contentLength);
		case 543713138:
			return decodeRgh(content, contentLength);
		case 544237938:
			return decodeRip(content, contentLength);
		case 543517810:
			return decodeRle(content, contentLength);
		case 540044658:
			return decodeRm(content, contentLength, 0, RECOILResolution.XE2X2);
		case 540110194:
			return decodeRm(content, contentLength, 1, RECOILResolution.XE4X1);
		case 540175730:
			return decodeRm(content, contentLength, 2, RECOILResolution.XE4X1);
		case 540241266:
			return decodeRm(content, contentLength, 3, RECOILResolution.XE4X1);
		case 540306802:
			return decodeRm(content, contentLength, 4, RECOILResolution.XE2X1);
		case 538996850:
			return contentLength == 10242 && decodeC64Multicolor(content, 1026, 2, 9218, -1);
		case 544174194:
		case 543713639:
			return decodeRpo(content, contentLength);
		case 543717234:
		case 543979378:
			return decodeRw(content, contentLength);
		case 544438642:
			return decodeRys(content, contentLength);
		case 544366963:
			return contentLength == 10219 && decodeC64Multicolor(content, 1026, 2, 9218, 1010);
		case 540044387:
		case 540109923:
		case 540175459:
		case 540043120:
		case 540042099:
		case 540107635:
			return decodeSc(content, contentLength);
		case 540173171:
			return decodeSc(content, contentLength) || decodeSc2(content, contentLength);
		case 540238707:
			return decodeSc3(content, contentLength);
		case 540304243:
			return decodeSc4(content, contentLength);
		case 540369779:
		case 540370279:
			return decodeSc5(filename, content, contentLength);
		case 540435315:
			return decodeSc6(filename, content, contentLength);
		case 540500851:
		case 540501351:
			return decodeSc7(filename, content, contentLength);
		case 540566387:
		case 540566887:
		case 540570227:
			return decodeSc8(filename, content, contentLength);
		case 543253363:
			return decodeSca(filename, content, contentLength);
		case 543384435:
		case 544436851:
		case 543910521:
			return decodeScc(filename, content, contentLength);
		case 544367475:
			return decodeScr(filename, content, contentLength);
		case 879977331:
		case 540308339:
			return decodeScs4(content, contentLength);
		case 540042355:
			return decodeSd(content, contentLength, 0);
		case 540107891:
			return decodeSd(content, contentLength, 1);
		case 540173427:
			return decodeSd(content, contentLength, 2);
		case 544630131:
			return decodeSev(content, contentLength);
		case 540239731:
			return decodeSg3(content, contentLength);
		case 543516531:
			return decodeSge(content, contentLength);
		case 544761715:
			return decodeSgx(content, contentLength);
		case 540108915:
			return decodeSh1(content, contentLength);
		case 540174451:
			return decodeSh2(content, contentLength);
		case 540239987:
		case 808464947:
			return decodeApfShr(content, contentLength) || decode3201(content, contentLength) || decodeSh3(content, contentLength) || decodeAppleIIShr(content, contentLength);
		case 543385715:
			return decodeShc(content, contentLength);
		case 543516787:
			return decodeShe(content, contentLength);
		case 543582323:
			return decodeShf(content, contentLength);
		case 543778931:
			return decodeShi(content, contentLength);
		case 544237683:
			return decodeShp(content, contentLength);
		case 544368755:
			return decodeApfShr(content, contentLength) || decodeAppleIIShr(content, contentLength) || decodeSh3(content, contentLength) || decodeTrsShr(content, contentLength);
		case 544434291:
			return decodeShs(content, contentLength);
		case 544761971:
			return decodeShx(content, contentLength);
		case 543582579:
			return decodeAtari8Sif(content, contentLength) || decodeC64Sif(content, contentLength);
		case 544238451:
			return decodeSkp(content, contentLength);
		case 543387763:
			return decodeStSpc(content, contentLength) || decodeAtari8Spc(content, contentLength);
		case 543453299:
			return decodeSpd(content, contentLength);
		case 544370803:
			return decodeSprEd(content, contentLength) || decodeAppleSpr(content, contentLength) || decodeAtari8Spr(content, contentLength) || decodeVectorSpr(content, contentLength);
		case 544436339:
			return decodeSps(content, contentLength);
		case 544567411:
			return decodeSpu(content, contentLength);
		case 544764019:
			return decodeSpx(content, contentLength);
		case 540373619:
			return decodeSr5(filename, content, contentLength);
		case 540439155:
			return decodeSr6(filename, content, contentLength);
		case 540504691:
			return decodeSr7(filename, content, contentLength);
		case 543781491:
			return decodeSri(filename, content, contentLength);
		case 544502387:
			return decodeSrt(content, contentLength);
		case 543322995:
			return decodeSsb(content, contentLength);
		case 544764787:
			return decodeSsx(content, contentLength);
		case 543978611:
			return decodeStl(content, contentLength);
		case 544240755:
			return decodeGl6(null, content, contentLength);
		case 543651955:
			return decodeSxg(content, contentLength);
		case 544438387:
			return decodeSxs(content, contentLength);
		case 544236404:
			return decodeTcp(content, contentLength);
		case 540108660:
			return decodeTg1(content, contentLength);
		case 544041332:
			return decodeTim(content, contentLength);
		case 544237940:
			return decodeTip(content, contentLength);
		case 540306548:
			return decodeTl4(content, contentLength);
		case 540110452:
		case 540175988:
		case 540241524:
		case 540307060:
		case 540372596:
		case 540438132:
		case 544829044:
			return decodeTny(content, contentLength);
		case 543780980:
			return decodeFalconPnt(content, contentLength);
		case 543519348:
			return decodeTre(content, contentLength);
		case 544240244:
			return decodeTrp(content, contentLength);
		case 544567924:
			return decodeTru(content, contentLength);
		case 540047476:
			return decodeTx0(content, contentLength);
		case 543520884:
			return decodeTxe(content, contentLength);
		case 544438388:
			return decodeTxs(content, contentLength);
		case 543975029:
			return decodeUfl(content, contentLength);
		case 543582581:
			return decodeUif(content, contentLength);
		case 544039542:
		case 538996066:
			return decodeVbm(content, contentLength);
		case 543778934:
			return decodeVhi(content, contentLength);
		case 543385974:
			return decodeVic(content, contentLength);
		case 543388534:
			return decodeVsc(filename, content, contentLength);
		case 543783542:
			return decodeVzi(content, contentLength);
		case 544106871:
			return decodeWin(filename, content, contentLength);
		case 543452791:
			return decodeWnd(content, contentLength);
		case 543975032:
			return decodeXfl(content, contentLength);
		case 543254392:
			return decodeXga(content, contentLength);
		case 544238712:
			return decodeXlp(content, contentLength);
		case 0x206D697A: // " miz"
			return decodeZim(content, contentLength);
		case 540306810:
			return decodeZm4(content, contentLength);
		case 544042874:
			return decodeZom(content, contentLength);
		case 540110970:
			return decodeZp1(content, contentLength);
		case 538997626:
			return decodeZs(content, contentLength);
		case 544241786:
			return decodeZxp(content, contentLength);
		case 544438394:
			return decodeZxs(content, contentLength);
		default:
			return false;
		}
	}

	/** */
	public String trialDecode(byte[] content, int contentLength) {
		boolean r;
		r = decodeIff(content, contentLength, RECOILResolution.AMIGA1X1) || decode256(content, contentLength);
		if (r) return "Iff";
		r = decode3(content, contentLength);
		if (r) return "3";
		r = decode3201(content, contentLength);
		if (r) return "3201";
		r = decode4bt(content, contentLength);
		if (r) return "4bt";
		r = decode4mi(content, contentLength);
        if (r) return "4mi";
		r = decode4pl(content, contentLength);
        if (r) return "4pl";
		r = decode4pm(content, contentLength);
        if (r) return "4pm";
		r = decode64c(content, contentLength);
        if (r) return "64c";
		r = decodeA(content, contentLength);
        if (r) return "A";
		r = decodeA4r(content, contentLength);
        if (r) return "A4r";
		r = contentLength == 10242 && decodeC64Multicolor(content, 2, 8194, 9218, 10241);
		if (r) return "C64Multicolor";
//		r = decodeIph(content, contentLength);
//        if (r) return "Iph";
		r = decodeAbk(content, contentLength);
        if (r) return "Abk";
		r = decodeAcs(content, contentLength);
        if (r) return "Acs";
		r = decodeAfl(content, contentLength);
        if (r) return "Afl";
		r = decodeAgp(content, contentLength);
        if (r) return "Agp";
		r = decodeAgs(content, contentLength);
        if (r) return "Ags";
		r = decodeAll(content, contentLength);
        if (r) return "All";
//		r = decodeAmi(content, contentLength);
//        if (r) return "Ami";
		r = decodeAn2(content, contentLength);
        if (r) return "An2";
		r = decodeAn4(content, contentLength, 0);
		if (r) return "An4";
		r = decodeAn4(content, contentLength, 1);
		if (r) return "An4";
		r = decode256(content, contentLength);
        if (r) return "256";
		r = decodeAp3(content, contentLength);
        if (r) return "Ap3";
		r = decodeApc(content, contentLength);
        if (r) return "Apc";
		r = decodeApl(content, contentLength);
        if (r) return "Apl";
		r = decodeApp(content, contentLength);
        if (r) return "App";
		r = decodeAps(content, contentLength);
        if (r) return "Aps";
//		r = decodeIph(content, contentLength) || decodeArtDirector(content, contentLength) || decodeGfaArtist(content, contentLength) || decodeDoo(content, contentLength) || decodePaletteMaster(content, contentLength) || decodeAtari8Artist(content, contentLength) || decodeMonoArt(content, contentLength) || decodeAsciiArtEditor(content, contentLength);
//		if (r) return "Iph";
		r = decodeArtMaster88(content, contentLength);
        if (r) return "ArtMaster88";
		r = decodeAtr(content, contentLength);
        if (r) return "Atr";
		r = decodeBw(content, contentLength);
        if (r) return "Bw";
		r = decodeBb0(content, contentLength, BBC_PALETTE1_BIT);
		if (r) return "Bb0";
		r = decodeBb1(content, contentLength, BBC_PALETTE2_BIT);
		if (r) return "Bb1";
		r = decodeBb2(content, contentLength, BBC_PALETTE);
		if (r) return "Bb2";
		r = decodeBb4(content, contentLength, BBC_PALETTE1_BIT);
		if (r) return "Bb4";
		r = decodeBb5(content, contentLength, BBC_PALETTE2_BIT);
		if (r) return "Bb5";
		r = decodeBbg(content, contentLength);
        if (r) return "Bbg";
		r = decodeBdp(content, contentLength);
        if (r) return "Bdp";
		r = decodeBfli(content, contentLength);
        if (r) return "Bfli";
		r = decodeG09(content, contentLength);
        if (r) return "G09";
		r = decodeBgp(content, contentLength);
        if (r) return "Bgp";
		r = decodeBil(content, contentLength);
        if (r) return "Bil";
		r = decodeBkg(content, contentLength);
        if (r) return "Bkg";
		r = decodeBks(content, contentLength);
        if (r) return "Bks";
		r = decodeBrus(content, contentLength);
        if (r) return "Brus";
		r = decodeIff(content, contentLength, RECOILResolution.ST1X1);
		if (r) return "Iff";
//		r = decodeBld(content, contentLength);
//        if (r) return "Bld";
//		r = decodeBml(content, contentLength);
//        if (r) return "Bml";
		r = decodeBp(content, contentLength);
        if (r) return "Bp";
		r = decodeBpl(content, contentLength);
        if (r) return "Bpl";
		r = decodeBru(content, contentLength);
        if (r) return "Bru";
		r = decodePrintfox(content, contentLength);
        if (r) return "Printfox";
		r = decodeBsc(content, contentLength);
        if (r) return "Bsc";
//		r = decodeBsp(content, contentLength);
//        if (r) return "Bsp";
		r = decodeCa(content, contentLength);
        if (r) return "Ca";
		r = decodeCci(content, contentLength);
        if (r) return "Cci";
		r = contentLength == 10277 && decodeC64Multicolor(content, 275, 8275, 9275, 10275);
		if (r) return "C64Multicolor";
		r = decodeCe(content, contentLength);
        if (r) return "Ce";
		r = decodeCel(content, contentLength);
        if (r) return "Cel";
		r = decodeCfli(content, contentLength);
        if (r) return "Cfli";
		r = decodeChrd(content, contentLength);
        if (r) return "Chrd";
		r = decodeCh8(content, contentLength);
        if (r) return "Ch8";
		r = contentLength == 20482 && decodeC64Multicolor(content, 2, 16898, 18434, 20479);
		if (r) return "C64Multicolor";
		r = decodeChr(content, contentLength);
        if (r) return "Chr";
		r = decodeChs(content, contentLength);
        if (r) return "Chs";
		r = decodeChx(content, contentLength);
        if (r) return "Chx";
		r = decodeCin(content, contentLength);
        if (r) return "Cin";
		r = decodeCle(content, contentLength);
        if (r) return "Cle";
		r = decodeGodotClp(content, contentLength) || decodeCocoClp(content, contentLength);
		if (r) return "GodotClp";
//		r = decodeCm5(filename, content, contentLength);
//		if (r) return true;
//		r = decodeDdGraph(filename, content, contentLength) || decodeStCmp(content, contentLength);
//		if (r) return true;
		r = decodeCp3(content, contentLength);
        if (r) return "Cp3";
//		r = decodeCpi(content, contentLength);
//        if (r) return "Cpi";
		r = decodeCpr(content, contentLength);
        if (r) return "Cpr";
//		r = decodeCpt(filename, content, contentLength);
//		if (r) return true;
		r = decodeCrg(content, contentLength);
        if (r) return "Crg";
		r = decodeCtm(content, contentLength);
        if (r) return "Ctm";
		r = decodeGr8Raw(content, contentLength, 96, 99);
		if (r) return "Gr8Raw";
		r = contentLength == 10007 && decodeC64Multicolor(content, 2, 8002, 9002, 10003);
		if (r) return "C64Multicolor";
		r = decodeDa4(content, contentLength);
        if (r) return "Da4";
		r = decodeDap(content, contentLength);
        if (r) return "Dap";
		r = decodeDc1(content, contentLength);
        if (r) return "Dc1";
//		r = decodeDd(content, contentLength);
//        if (r) return "Dd";
		r = decodeDel(content, contentLength);
        if (r) return "Del";
		r = decodeDg1(content, contentLength);
        if (r) return "Dg1";
		r = decodeAppleIIDhr(content, contentLength);
        if (r) return "AppleIIDhr";
		r = decodeIff(content, contentLength, RECOILResolution.AMIGA1X1) || decodeAppleIIDhr(content, contentLength);
		if (r) return "Iff";
		r = decodeAtari8Ice(content, contentLength, false, 3);
		if (r) return "Atari8Ice";
		r = decodeDit(content, contentLength);
        if (r) return "Dit";
		r = decodeDlm(content, contentLength);
        if (r) return "Dlm";
		r = decodeDol(content, contentLength);
        if (r) return "Dol";
		r = decodeDoo(content, contentLength);
        if (r) return "Doo";
		r = decodeDph(content, contentLength);
        if (r) return "Dph";
		r = decodeDrg(content, contentLength);
        if (r) return "Drg";
		r = decodeDrl(content, contentLength);
        if (r) return "Drl";
		r = decodeDrz(content, contentLength);
        if (r) return "Drz";
		r = decodeDuo(content, contentLength);
        if (r) return "Duo";
		r = decodeDu2(content, contentLength);
        if (r) return "Du2";
		r = decodeEbd(content, contentLength);
        if (r) return "Ebd";
//		r = decodeEci(content, contentLength);
//        if (r) return "Eci";
//		r = decodeEcp(content, contentLength);
//        if (r) return "Ecp";
		r = decodeEmc(content, contentLength);
        if (r) return "Emc";
		r = decodeEpa(content, contentLength);
        if (r) return "Epa";
		r = decodeEsh(content, contentLength);
        if (r) return "Esh";
		r = decodeEsm(content, contentLength);
        if (r) return "Esm";
		r = decodeEza(content, contentLength);
        if (r) return "Eza";
		r = decodeF80(content, contentLength);
        if (r) return "F80";
//		r = decodeFbi(content, contentLength);
//        if (r) return "Fbi";
		r = contentLength == 10004 && decodeC64Multicolor(content, 2, 8002, 9002, 10002);
		if (r) return "C64Multicolor";
		r = decodeFli(content, contentLength);
        if (r) return "Fli";
		r = decodeFed(content, contentLength);
        if (r) return "Fed";
		r = decodeFfli(content, contentLength);
        if (r) return "Ffli";
		r = decodeFge(content, contentLength);
        if (r) return "Fge";
		r = decodeFlf(content, contentLength);
        if (r) return "Flf";
//		r = decodeFli(content, contentLength) || decodeBml(content, contentLength);
//		if (r) return "Fli|Bml";
//		r = decodeFlm(content, contentLength);
//        if (r) return "Flm";
		r = decodeFn2(content, contentLength);
        if (r) return "Fn2";
		r = decodePct(content, contentLength) || decodeGdosFnt(content, contentLength) || decodeAtari8Fnt(content, contentLength) || decodeStFnt(content, contentLength) || decodeAmstradFnt(content, contentLength) || decodeImage72Fnt(content, contentLength);
		if (r) return "Pct|Fnt|Atari8Fnt";
		r = decodeFp(content, contentLength);
        if (r) return "Fp";
		r = decodeC64Fun(content, contentLength);
        if (r) return "C64Fun";
		r = decodeFpr(content, contentLength);
        if (r) return "Fpr";
		r = decodeFtc(content, contentLength);
        if (r) return "Ftc";
		r = decodeFul(content, contentLength);
        if (r) return "Ful";
		r = decodeC64Fun(content, contentLength) || decodeFalconFun(content, contentLength);
		if (r) return "C64Fun";
		r = decodeFwa(content, contentLength);
        if (r) return "Fwa";
		r = decodeG(content, contentLength);
        if (r) return "G";
		r = decodeG10(content, contentLength);
        if (r) return "G10";
		r = decodeG11(content, contentLength);
        if (r) return "G11";
		r = decodeG2f(content, contentLength);
        if (r) return "G2f";
		r = decodeG9b(content, contentLength);
        if (r) return "G9b";
		r = decodeG9s(content, contentLength);
        if (r) return "G9s";
		r = decodeGed(content, contentLength);
        if (r) return "Ged";
		r = decodeGfb(content, contentLength);
        if (r) return "Gfb";
//		r = decodeGg(content, contentLength);
//        if (r) return "Gg";
		r = decodeGhg(content, contentLength);
        if (r) return "Ghg";
//		r = decodeIph(content, contentLength) || decodeKoa(content, contentLength);
//		if (r) return "Iph";
//		r = decodeGl5(filename, content, contentLength);
//		if (r) return true;
//		r = decodeGl6(filename, content, contentLength);
//		if (r) return true;
//		r = decodeGl7(filename, content, contentLength);
//		if (r) return true;
		r = decodeGl8(content, contentLength);
        if (r) return "Gl8";
//		r = decodeGlYjk(filename, content, contentLength);
//		if (r) return true;
		r = decodeGlYjk(null, content, contentLength);
		if (r) return "GlYjk";
		r = decodeGod(content, contentLength);
        if (r) return "God";
		r = decodeGr(content, contentLength);
        if (r) return "Gr";
		r = decodeGr0(content, contentLength);
        if (r) return "Gr0";
		r = decodeGr1(content, contentLength, 0);
		if (r) return "Gr1";
		r = decodeGr1(content, contentLength, 1);
		if (r) return "Gr1";
		r = decodeGr3(content, contentLength);
        if (r) return "Gr3";
		r = decodeGr7(content, 0, contentLength);
		if (r) return "Gr7";
		r = decodeGr8(content, contentLength);
        if (r) return "Gr8";
		r = decodeGr9(content, contentLength);
        if (r) return "Gr9";
		r = decodeGr9p(content, contentLength);
        if (r) return "Gr9p";
		r = decodeGrb(content, contentLength);
        if (r) return "Grb";
		r = decodeCocoMax(content, contentLength) || decodeProfiGrf(content, contentLength);
		if (r) return "CocoMax";
		r = decodeSc2(content, contentLength);
        if (r) return "Sc2";
		r = decodeApfShr(content, contentLength);
        if (r) return "ApfShr";
		r = decodeGun(content, contentLength);
        if (r) return "Gun";
		r = decodeC64Hir(content, contentLength);
        if (r) return "C64Hir";
		r = decodeHcb(content, contentLength);
        if (r) return "Hcb";
		r = decodeHcm(content, contentLength);
        if (r) return "Hcm";
		r = contentLength == 9218 && decodeHed(content);
		if (r) return "Hed";
		r = decodeHfc(content, contentLength);
        if (r) return "Hfc";
		r = decodeHgb(content, contentLength);
        if (r) return "Hgb";
//		r = decodeHgr(content, contentLength);
//        if (r) return "Hgr";
		r = decodeHim(content, contentLength);
        if (r) return "Him";
		r = decodeHip(content, contentLength);
        if (r) return "Hip";
		r = decodeFalconHir(content, contentLength) || decodeC64Hir(content, contentLength) || decodeHrs(content, contentLength);
		if (r) return "FalconHir";
		r = decodeHle(content, contentLength);
        if (r) return "Hle";
		r = decodeHlf(content, contentLength);
        if (r) return "Hlf";
		r = decodeHlr(content, contentLength);
        if (r) return "Hlr";
//		r = decodeIph(content, contentLength);
//        if (r) return "Iph";
		r = decodeC64Hir(content, contentLength) || decodeIph(content, contentLength);
		if (r) return "C64Hir";
//		r = decodeHpm(content, contentLength);
//        if (r) return "Hpm";
		r = decodeHps(content, contentLength);
        if (r) return "Hps";
		r = decodeTrsHr(content, contentLength) || decodeAtari8Hr(content, contentLength);
		if (r) return "TrsHr";
		r = decodeHr2(content, contentLength);
        if (r) return "Hr2";
		r = decodeHrg(content, contentLength);
        if (r) return "Hrg";
		r = decodeHrm(content, contentLength);
        if (r) return "Hrm";
		r = decodeHrs(content, contentLength);
        if (r) return "Hrs";
		r = decodeHs2(content, contentLength);
        if (r) return "Hs2";
		r = decodeIbi(content, contentLength);
        if (r) return "Ibi";
		r = decodeIc(content, contentLength);
        if (r) return "Ic";
		r = contentLength > 1024 && decodeAtari8Ice(content, contentLength, true, content[0] & 0xff);
		if (r) return "Atari8Ice";
		r = decodeStIcn(content, contentLength) || decodePsion3Pic(content, contentLength) || decodeAtari8Ice(content, contentLength, false, 17);
		if (r) return "StIcn";
		r = decodeIff(content, contentLength, RECOILResolution.AMIGA1X1);
		if (r) return "Iff";
		r = decodeGun(content, contentLength) || decodeZxIfl(content, contentLength);
		if (r) return "Gun";
		r = decodeIge(content, contentLength);
        if (r) return "Ige";
		r = decodeIhe(content, contentLength);
        if (r) return "Ihe";
		r = decodeIim(content, contentLength);
        if (r) return "Iim";
		r = decodeIld(content, contentLength);
        if (r) return "Ild";
		r = decodeIle(content, contentLength);
        if (r) return "Ile";
		r = decodeIls(content, contentLength);
        if (r) return "Ils";
		r = decodeStImg(content, contentLength) || decodeZxImg(content, contentLength) || decodeArtMaster88(content, contentLength) || decodeDaVinci(content, contentLength);
		if (r) return "StImg";
		r = decodeStImg(content, contentLength);
        if (r) return "StImg";
		r = decodeAtari8Ice(content, contentLength, false, 18);
		if (r) return "Atari8Ice";
		r = decodeInfo(content, contentLength);
        if (r) return "Info";
//		r = decodeInp(content, contentLength);
//        if (r) return "Inp";
		r = decodeIns(content, contentLength);
        if (r) return "Ins";
//		r = decodeInt(content, contentLength) || decodeInp(content, contentLength);
//		if (r) return "Int";
		r = decodeIp2(content, contentLength);
        if (r) return "Ip2";
		r = decodeAtari8Ice(content, contentLength, false, 19);
		if (r) return "Atari8Ice";
		r = contentLength == 10003 && decodeC64Multicolor(content, 2, 8002, 9002, 10002);
		if (r) return "decodeC64Multicolor";
		r = decodeAtari8Ice(content, contentLength, false, 2);
		if (r) return "Atari8Ice2";
		r = decodeAtari8Ice(content, contentLength, false, 1);
		if (r) return "Atari8Ice1";
		r = decodeIsh(content, contentLength);
        if (r) return "Ish";
		r = contentLength == 10218 && decodeC64Multicolor(content, 1026, 9218, 2, 9217);
		if (r) return "C64Multicolor";
		r = decodeIst(content, contentLength);
        if (r) return "Ist";
		r = decodeJgp(content, contentLength);
        if (r) return "Jgp";
//		r = decodeJj(content, contentLength);
//        if (r) return "Jj";
		r = decodeGr8Raw(content, contentLength, 56, 60);
		if (r) return "Gr8Raw";
		r = decodeKid(content, contentLength);
        if (r) return "Kid";
//		r = decodeKoa(content, contentLength);
//        if (r) return "Koa";
		r = decodeKpr(content, contentLength);
        if (r) return "Kpr";
		r = decodeKss(content, contentLength);
        if (r) return "Kss";
		r = decodeLdm(content, contentLength);
        if (r) return "Ldm";
		r = decodeLeo(content, contentLength);
        if (r) return "Leo";
		r = decodeLp3(content, contentLength);
        if (r) return "Lp3";
		r = decodeDaliCompressed(content, contentLength, 0);
		if (r) return "DaliCompressed0";
		r = decodeDaliCompressed(content, contentLength, 1);
		if (r) return "DaliCompressed1";
		r = decodeDaliCompressed(content, contentLength, 2);
		if (r) return "DaliCompressed2";
//		r = decodeLum(filename, content, contentLength);
//		if (r) return true;
		r = decodeMac(content, contentLength);
        if (r) return "Mac";
		r = decodeMag(content, contentLength);
        if (r) return "Mag";
		r = decodeEnvision(content, contentLength) || decodeEnvisionPC(content, contentLength);
		if (r) return "Envision";
		r = decodeMag(content, contentLength) || decodeAtari8Max(content, contentLength) || decodeCocoMax(content, contentLength);
		if (r) return "Mag";
		r = decodeGr8Raw(content, contentLength, 512, 256);
		if (r) return "Gr8Raw";
		r = decodeMcMlt(content, contentLength, -1);
		if (r) return "McMlt";
		r = decodeMch(content, contentLength);
        if (r) return "Mch";
//		r = decodeMci(content, contentLength);
//        if (r) return "Mci";
		r = decodeMcp(content, contentLength);
        if (r) return "Mcp";
		r = decodeMcpp(content, contentLength);
        if (r) return "Mcpp";
		r = decodeMcs(content, contentLength);
        if (r) return "Mcs";
		r = decodeMg(content, contentLength);
        if (r) return "Mg";
		r = decodeMga(content, contentLength);
        if (r) return "Mga";
		r = decodeMgp(content, contentLength);
        if (r) return "Mgp";
//		r = decodeMic(filename, content, contentLength);
//		if (r) return true;
		r = decodeMig(content, contentLength);
        if (r) return "Mig";
		r = decodeMil(content, contentLength);
        if (r) return "Mil";
		r = decodeMis(content, contentLength);
        if (r) return "Mis";
		r = decodeMl1(content, contentLength);
        if (r) return "Ml1";
		r = decodeMle(content, contentLength);
        if (r) return "Mle";
		r = decodeMcMlt(content, contentLength, 0);
		if (r) return "McMlt";
		r = decodeMpl(content, contentLength);
        if (r) return "Mpl";
		r = decodeMpp(content, contentLength);
        if (r) return "Mpp";
		r = decodeMsl(content, contentLength);
        if (r) return "Msl";
		r = decodeMsp(content, contentLength);
        if (r) return "Msp";
		r = decodeMuf(content, contentLength);
        if (r) return "Muf";
		r = decodeMui(content, contentLength);
        if (r) return "Mui";
		r = decodeMup(content, contentLength);
        if (r) return "Mup";
//		r = decodeMur(filename, content, contentLength);
//		if (r) return true;
		r = decodeMwi(content, contentLength);
        if (r) return "Mwi";
		r = decodeMx1(content, contentLength);
        if (r) return "Mx1";
//		r = decodeNeo(filename, content, contentLength) || decodeIff(content, contentLength, RECOILResolution.STE1X1);
//		if (r) return true;
		r = decodeNl3(content, contentLength);
        if (r) return "Nl3";
		r = decodeNlq(content, contentLength);
        if (r) return "Nlq";
		r = decodeNuf(content, contentLength);
        if (r) return "Nuf";
		r = decodeNup(content, contentLength);
        if (r) return "Nup";
		r = decodeNxi(content, contentLength);
        if (r) return "Nxi";
		r = decodeObj(content, contentLength);
        if (r) return "Obj";
		r = decodeOcp(content, contentLength);
        if (r) return "Ocp";
		r = decodeOdf(content, contentLength);
        if (r) return "Odf";
		r = decodeP(content, contentLength);
        if (r) return "P";
		r = decodeP11(content, contentLength);
        if (r) return "P11";
		r = decodeP3c(content, contentLength);
        if (r) return "P3c";
		r = decodeCocoMax(content, contentLength);
        if (r) return "CocoMax";
		r = decodeP4i(content, contentLength);
        if (r) return "P4i";
		r = contentLength == 10050 && decodeC64Multicolor(content, 2050, 1026, 2, 2049);
		if (r) return "C64Multicolor";
		r = decodeStPpp(content, contentLength);
        if (r) return "StPpp";
		r = decodePac(content, contentLength);
        if (r) return "Pac";
		r = decodePbx(content, contentLength);
        if (r) return "Pbx";
		r = decodePc(content, contentLength);
        if (r) return "Pc";
		r = decodePci(content, contentLength);
        if (r) return "Pci";
		r = decodePcs(content, contentLength);
        if (r) return "Pcs";
		r = decodePct(content, contentLength);
        if (r) return "Pct";
		r = decodePdr(content, contentLength);
        if (r) return "Pdr";
		r = decodePet(content, contentLength);
        if (r) return "Pet";
		r = decodeSc(content, contentLength) || decodeGraphicsProcessor(content, contentLength);
		if (r) return "Sc|GraphicsProcessor";
		r = decodeGraphicsProcessor(content, contentLength);
        if (r) return "GraphicsProcessor";
		r = decodePgc(content, contentLength);
        if (r) return "Pgc";
		r = decodePgf(content, contentLength);
        if (r) return "Pgf";
		r = decodePgr(content, contentLength);
        if (r) return "Pgr";
		r = decodePi(content, contentLength) || decodeBpl(content, contentLength);
		if (r) return "Pi";
		r = decodeStPi(content, contentLength);
        if (r) return "StPi";
		r = decodeFuckpaint(content, contentLength) || decodeStPi(content, contentLength);
		if (r) return "Fuckpaint|StPi";
		r = decodeFuckpaint(content, contentLength);
        if (r) return "Fuckpaint";
		r = decodePi8(content, contentLength);
        if (r) return "Pi8";
		r = decodePi9(content, contentLength);
        if (r) return "Pi9";
		r = decodePic(content, contentLength);
        if (r) return "Pic";
//		r = decodePic0(filename, content, contentLength);
//		if (r) return true;
		r = decodeFalconPix(content, contentLength) || decodeCocoMax(content, contentLength) || decodeAtari8Pix(content, contentLength);
		if (r) return "FalconPix";
		r = decodePl4(content, contentLength);
        if (r) return "Pl4";
		r = decodePla(content, contentLength);
        if (r) return "Pla";
		r = decodePls(content, contentLength);
        if (r) return "Pls";
		r = decodePmd(content, contentLength);
        if (r) return "Pmd";
		r = decodePmg(content, contentLength);
        if (r) return "Pmg";
//		r = decodeFalconPnt(content, contentLength) || decodeTandyPnt(content, contentLength) || decodeApfShr(content, contentLength) || decodeMac(content, contentLength) || decodeAppleIIShr(content, contentLength) || decodePaintworks(content, contentLength);
//		if (r) return "FalconPnt";
		r = decodePp(content, contentLength);
        if (r) return "Pp";
//		r = decodePph(filename, content, contentLength);
//		if (r) return true;
		r = decodeStPpp(content, contentLength) || decodePp(content, contentLength);
		if (r) return "StPpp";
		r = decodePsc(content, contentLength);
        if (r) return "Psc";
		r = decodePsf(content, contentLength);
        if (r) return "Psf";
		r = decodeQ4(content, contentLength);
        if (r) return "Q4";
		r = decodeRag(content, contentLength);
        if (r) return "Rag";
		r = decodeRap(content, contentLength);
        if (r) return "Rap";
		r = decodeZx81Raw(content, contentLength) || decodeAtari8Raw(content, contentLength) || decodeRw(content, contentLength);
		if (r) return "Zx81Raw";
		r = decodeStRgb(content, contentLength) || decodeAtari8Rgb(content, contentLength) || decodeZxRgb(content, contentLength);
		if (r) return "StRgb";
		r = decodeRgh(content, contentLength);
        if (r) return "Rgh";
		r = decodeRip(content, contentLength);
        if (r) return "Rip";
		r = decodeRle(content, contentLength);
        if (r) return "Rle";
		r = decodeRm(content, contentLength, 0, RECOILResolution.XE2X2);
		if (r) return "Rm0";
		r = decodeRm(content, contentLength, 1, RECOILResolution.XE4X1);
		if (r) return "Rm1";
		r = decodeRm(content, contentLength, 2, RECOILResolution.XE4X1);
		if (r) return "Rm2";
		r = decodeRm(content, contentLength, 3, RECOILResolution.XE4X1);
		if (r) return "Rm3";
		r = decodeRm(content, contentLength, 4, RECOILResolution.XE2X1);
		if (r) return "Rm4";
		r = contentLength == 10242 && decodeC64Multicolor(content, 1026, 2, 9218, -1);
		if (r) return "C64Multicolor";
		r = decodeRpo(content, contentLength);
        if (r) return "Rpo";
		r = decodeRw(content, contentLength);
        if (r) return "Rw";
		r = decodeRys(content, contentLength);
        if (r) return "Rys";
		r = contentLength == 10219 && decodeC64Multicolor(content, 1026, 2, 9218, 1010);
		if (r) return "C64Multicolor";
		r = decodeSc(content, contentLength);
        if (r) return "Sc";
		r = decodeSc(content, contentLength) || decodeSc2(content, contentLength);
		if (r) return "Sc2";
		r = decodeSc3(content, contentLength);
        if (r) return "Sc3";
		r = decodeSc4(content, contentLength);
        if (r) return "Sc4";
//		r = decodeSc5(filename, content, contentLength);
//		if (r) return true;
//		r = decodeSc6(filename, content, contentLength);
//		if (r) return true;
//		r = decodeSc7(filename, content, contentLength);
//		if (r) return true;
//		r = decodeSc8(filename, content, contentLength);
//		if (r) return true;
//		r = decodeSca(filename, content, contentLength);
//		if (r) return true;
//		r = decodeScc(filename, content, contentLength);
//		if (r) return true;
//		r = decodeScr(filename, content, contentLength);
//		if (r) return true;
		r = decodeScs4(content, contentLength);
        if (r) return "Scs4";
		r = decodeSd(content, contentLength, 0);
		if (r) return "Sd";
		r = decodeSd(content, contentLength, 1);
		if (r) return "Sd";
		r = decodeSd(content, contentLength, 2);
		if (r) return "Sd";
		r = decodeSev(content, contentLength);
        if (r) return "Sev";
		r = decodeSg3(content, contentLength);
        if (r) return "Sg3";
		r = decodeSge(content, contentLength);
        if (r) return "Sge";
		r = decodeSgx(content, contentLength);
        if (r) return "Sgx";
//		r = decodeSh1(content, contentLength);
//        if (r) return "Sh1";
//		r = decodeSh2(content, contentLength);
//        if (r) return "Sh2";
//		r = decodeApfShr(content, contentLength) || decode3201(content, contentLength) || decodeSh3(content, contentLength) || decodeAppleIIShr(content, contentLength);
//		if (r) return "ApfShr";
		r = decodeShc(content, contentLength);
        if (r) return "Shc";
		r = decodeShe(content, contentLength);
        if (r) return "She";
//		r = decodeShf(content, contentLength);
//        if (r) return "Shf";
//		r = decodeShi(content, contentLength);
//        if (r) return "Shi";
		r = decodeShp(content, contentLength);
        if (r) return "Shp";
//		r = decodeApfShr(content, contentLength) || decodeAppleIIShr(content, contentLength) || decodeSh3(content, contentLength) || decodeTrsShr(content, contentLength);
//		if (r) return "ApfShr";
		r = decodeShs(content, contentLength);
        if (r) return "Shs";
//		r = decodeShx(content, contentLength);
//        if (r) return "Shx";
		r = decodeAtari8Sif(content, contentLength) || decodeC64Sif(content, contentLength);
		if (r) return "Atari8Sif";
		r = decodeSkp(content, contentLength);
        if (r) return "Skp";
		r = decodeStSpc(content, contentLength) || decodeAtari8Spc(content, contentLength);
		if (r) return "StSpc";
		r = decodeSpd(content, contentLength);
        if (r) return "Spd";
//		r = decodeSprEd(content, contentLength) || decodeAppleSpr(content, contentLength) || decodeAtari8Spr(content, contentLength) || decodeVectorSpr(content, contentLength);
//		if (r) return "SprEd";
		r = decodeSps(content, contentLength);
        if (r) return "Sps";
		r = decodeSpu(content, contentLength);
        if (r) return "Spu";
		r = decodeSpx(content, contentLength);
        if (r) return "Spx";
//		r = decodeSr5(filename, content, contentLength);
//		if (r) return "Sr5";
//		r = decodeSr6(filename, content, contentLength);
//		if (r) return "Sr6";
//		r = decodeSr7(filename, content, contentLength);
//		if (r) return "Sr7";
//		r = decodeSri(filename, content, contentLength);
//		if (r) return "Sri";
		r = decodeSrt(content, contentLength);
        if (r) return "Srt";
		r = decodeSsb(content, contentLength);
        if (r) return "Ssb";
		r = decodeSsx(content, contentLength);
        if (r) return "Ssx";
		r = decodeStl(content, contentLength);
        if (r) return "Stl";
		r = decodeGl6(null, content, contentLength);
		if (r) return "Gl6";
		r = decodeSxg(content, contentLength);
        if (r) return "Sxg";
		r = decodeSxs(content, contentLength);
        if (r) return "Sxs";
		r = decodeTcp(content, contentLength);
        if (r) return "Tcp";
		r = decodeTg1(content, contentLength);
        if (r) return "Tg1";
		r = decodeTim(content, contentLength);
        if (r) return "Tim";
		r = decodeTip(content, contentLength);
        if (r) return "Tip";
		r = decodeTl4(content, contentLength);
        if (r) return "Tl4";
		r = decodeTny(content, contentLength);
        if (r) return "Tny";
		r = decodeFalconPnt(content, contentLength);
        if (r) return "FalconPnt";
		r = decodeTre(content, contentLength);
        if (r) return "Tre";
		r = decodeTrp(content, contentLength);
        if (r) return "Trp";
		r = decodeTru(content, contentLength);
        if (r) return "Tru";
		r = decodeTx0(content, contentLength);
        if (r) return "Tx0";
		r = decodeTxe(content, contentLength);
        if (r) return "Txe";
		r = decodeTxs(content, contentLength);
        if (r) return "Txs";
//		r = decodeUfl(content, contentLength);
//        if (r) return "Ufl";
//		r = decodeUif(content, contentLength);
//        if (r) return "Uif";
		r = decodeVbm(content, contentLength);
        if (r) return "Vbm";
		r = decodeVhi(content, contentLength);
        if (r) return "Vhi";
		r = decodeVic(content, contentLength);
        if (r) return "Vic";
//		r = decodeVsc(filename, content, contentLength);
//		if (r) return true;
		r = decodeVzi(content, contentLength);
        if (r) return "Vzi";
//		r = decodeWin(filename, content, contentLength);
//		if (r) return true;
		r = decodeWnd(content, contentLength);
        if (r) return "Wnd";
//		r = decodeXfl(content, contentLength);
//        if (r) return "Xfl";
		r = decodeXga(content, contentLength);
        if (r) return "Xga";
//		r = decodeXlp(content, contentLength);
//        if (r) return "Xlp";
		r = decodeZim(content, contentLength);
        if (r) return "Zim";
		r = decodeZm4(content, contentLength);
        if (r) return "Zm4";
//		r = decodeZom(content, contentLength);
//        if (r) return "Zom";
		r = decodeZp1(content, contentLength);
        if (r) return "Zp1";
		r = decodeZs(content, contentLength);
        if (r) return "Zs";
		r = decodeZxp(content, contentLength);
        if (r) return "Zxp";
		r = decodeZxs(content, contentLength);
		if (r) return "Zxs";
		return null;
	}

	/**
	 * Returns decoded image width.
	 */
	public final int getWidth()
	{
		return this.width;
	}

	/**
	 * Returns decoded image height.
	 */
	public final int getHeight()
	{
		return this.height;
	}

	/**
	 * Returns pixels of the decoded image, top-down, left-to-right.
	 * Each pixel is a 24-bit integer 0xRRGGBB.
	 */
	public final int[] getPixels()
	{
		return this.pixels;
	}

	/**
	 * Returns the computer family of the decoded file format.
	 */
	public final String getPlatform()
	{
		switch (this.resolution) {
		case RECOILResolution.AMIGA1X1:
		case RECOILResolution.AMIGA2X1:
		case RECOILResolution.AMIGA4X1:
		case RECOILResolution.AMIGA8X1:
		case RECOILResolution.AMIGA1X2:
		case RECOILResolution.AMIGA1X4:
			return "Amiga";
		case RECOILResolution.AMIGA_DCTV1X1:
		case RECOILResolution.AMIGA_DCTV1X2:
			return "Amiga DCTV";
		case RECOILResolution.AMIGA_HAME1X1:
		case RECOILResolution.AMIGA_HAME2X1:
			return "Amiga HAM-E";
		case RECOILResolution.AMSTRAD1X1:
		case RECOILResolution.AMSTRAD2X1:
		case RECOILResolution.AMSTRAD1X2:
			return "Amstrad CPC";
		case RECOILResolution.APPLE_I_I1X1:
			return "Apple II";
		case RECOILResolution.APPLE_I_IE1X2:
			return "Apple IIe";
		case RECOILResolution.APPLE_I_I_G_S1X1:
		case RECOILResolution.APPLE_I_I_G_S1X2:
			return "Apple IIGS";
		case RECOILResolution.MACINTOSH1X1:
			return "Apple Macintosh";
		case RECOILResolution.XE1X1:
		case RECOILResolution.XE2X1:
		case RECOILResolution.XE4X1:
		case RECOILResolution.XE2X2:
		case RECOILResolution.XE4X2:
		case RECOILResolution.XE4X4:
		case RECOILResolution.XE8X8:
			return "Atari 8-bit";
		case RECOILResolution.VBXE1X1:
		case RECOILResolution.VBXE2X1:
			return "Atari 8-bit VBXE";
		case RECOILResolution.PORTFOLIO1X1:
			return "Atari Portfolio";
		case RECOILResolution.ST1X1:
		case RECOILResolution.ST1X2:
			return "Atari ST";
		case RECOILResolution.STE1X1:
		case RECOILResolution.STE1X2:
			return "Atari STE";
		case RECOILResolution.TT1X1:
		case RECOILResolution.TT2X1:
			return "Atari TT";
		case RECOILResolution.FALCON1X1:
		case RECOILResolution.FALCON2X1:
			return "Atari Falcon";
		case RECOILResolution.BBC1X1:
		case RECOILResolution.BBC2X1:
		case RECOILResolution.BBC1X2:
			return "BBC Micro";
		case RECOILResolution.VIC201X1:
		case RECOILResolution.VIC202X1:
			return "Commodore VIC-20";
		case RECOILResolution.C161X1:
		case RECOILResolution.C162X1:
			return "Commodore 16/116/Plus4";
		case RECOILResolution.C641X1:
		case RECOILResolution.C642X1:
			return "Commodore 64";
		case RECOILResolution.C1281X1:
			return "Commodore 128";
		case RECOILResolution.BK1X1:
		case RECOILResolution.BK1X2:
			return "Electronika BK";
		case RECOILResolution.MC05151X2:
			return "Electronika MC 0515";
		case RECOILResolution.FM_TOWNS1X1:
			return "FM Towns";
		case RECOILResolution.HP481X1:
			return "HP 48";
		case RECOILResolution.MSX11X1:
		case RECOILResolution.MSX14X4:
			return "MSX";
		case RECOILResolution.MSX21X1:
		case RECOILResolution.MSX21X2:
		case RECOILResolution.MSX21X1I:
		case RECOILResolution.MSX22X1I:
			return "MSX2";
		case RECOILResolution.MSX2_PLUS1X1:
		case RECOILResolution.MSX2_PLUS2X1I:
			return "MSX2+";
		case RECOILResolution.MSX_V99901X1:
			return "MSX V9990 VDP";
		case RECOILResolution.ORIC1X1:
			return "Oric";
		case RECOILResolution.PC1X1:
			return "PC";
		case RECOILResolution.PC801X2:
			return "NEC PC-80";
		case RECOILResolution.PC881X2:
			return "NEC PC-88";
		case RECOILResolution.PC88_VA1X1:
			return "NEC PC-88 VA";
		case RECOILResolution.PC981X1:
			return "NEC PC-98";
		case RECOILResolution.PLAY_STATION1X1:
			return "PlayStation";
		case RECOILResolution.PSION31X1:
			return "Psion Series 3";
		case RECOILResolution.SAM_COUPE1X1:
		case RECOILResolution.SAM_COUPE1X2:
			return "SAM Coupe";
		case RECOILResolution.X68_K1X1:
			return "Sharp X68000";
		case RECOILResolution.TANDY1X1:
			return "Tandy 1000";
		case RECOILResolution.VECTOR1X1:
			return "Vector-06C";
		case RECOILResolution.ZX811X1:
			return "ZX81";
		case RECOILResolution.SPECTRUM1X1:
		case RECOILResolution.SPECTRUM4X4:
			return "ZX Spectrum";
		case RECOILResolution.SPECTRUM_PROFI1X2:
			return "ZX Spectrum Profi";
		case RECOILResolution.SPECTRUM_ULA_PLUS1X1:
			return "ZX Spectrum ULAplus";
		case RECOILResolution.ZX_EVOLUTION1X1:
			return "ZX Evolution";
		case RECOILResolution.SPECTRUM_NEXT1X1:
			return "ZX Spectrum Next";
		case RECOILResolution.TIMEX1X1:
		case RECOILResolution.TIMEX1X2:
			return "Timex 2048";
		case RECOILResolution.TRS1X1:
		case RECOILResolution.TRS1X2:
			return "TRS-80";
		case RECOILResolution.COCO1X1:
		case RECOILResolution.COCO2X2:
			return "TRS-80 Color Computer";
		default:
			return "Unknown";
		}
	}

	/**
	 * Returns original width of the decoded image (informational).
	 */
	public final int getOriginalWidth()
	{
		switch (this.resolution) {
		case RECOILResolution.AMIGA2X1:
		case RECOILResolution.AMIGA_HAME2X1:
		case RECOILResolution.AMSTRAD2X1:
		case RECOILResolution.XE2X1:
		case RECOILResolution.XE2X2:
		case RECOILResolution.VBXE2X1:
		case RECOILResolution.TT2X1:
		case RECOILResolution.FALCON2X1:
		case RECOILResolution.BBC2X1:
		case RECOILResolution.VIC202X1:
		case RECOILResolution.C162X1:
		case RECOILResolution.C642X1:
		case RECOILResolution.COCO2X2:
		case RECOILResolution.MSX22X1I:
		case RECOILResolution.MSX2_PLUS2X1I:
			return this.width >> 1;
		case RECOILResolution.AMIGA4X1:
		case RECOILResolution.MSX14X4:
		case RECOILResolution.SPECTRUM4X4:
		case RECOILResolution.XE4X1:
		case RECOILResolution.XE4X2:
		case RECOILResolution.XE4X4:
			return this.width >> 2;
		case RECOILResolution.AMIGA8X1:
		case RECOILResolution.XE8X8:
			return this.width >> 3;
		default:
			return this.width;
		}
	}

	/**
	 * Returns original height of the decoded image (informational).
	 */
	public final int getOriginalHeight()
	{
		switch (this.resolution) {
		case RECOILResolution.AMIGA1X2:
		case RECOILResolution.AMSTRAD1X2:
		case RECOILResolution.APPLE_I_IE1X2:
		case RECOILResolution.APPLE_I_I_G_S1X2:
		case RECOILResolution.XE2X2:
		case RECOILResolution.XE4X2:
		case RECOILResolution.ST1X2:
		case RECOILResolution.STE1X2:
		case RECOILResolution.BBC1X2:
		case RECOILResolution.BK1X2:
		case RECOILResolution.MC05151X2:
		case RECOILResolution.MSX21X2:
		case RECOILResolution.PC801X2:
		case RECOILResolution.PC881X2:
		case RECOILResolution.SAM_COUPE1X2:
		case RECOILResolution.SPECTRUM_PROFI1X2:
		case RECOILResolution.TIMEX1X2:
		case RECOILResolution.TRS1X2:
		case RECOILResolution.COCO2X2:
			return this.height >> 1;
		case RECOILResolution.AMIGA1X4:
		case RECOILResolution.MSX14X4:
		case RECOILResolution.SPECTRUM4X4:
		case RECOILResolution.XE4X4:
			return this.height >> 2;
		case RECOILResolution.XE8X8:
			return this.height >> 3;
		default:
			return this.height;
		}
	}

	private static final int PAL_TV_Y_PIXELS_PER_METER = 2624;

	private static final int NTSC_TV_Y_PIXELS_PER_METER = 2624;

	private static final int PAL_SQUARE_DOT_CLOCK = 14750000;

	private static final int NTSC_SQUARE_DOT_CLOCK = 12272727;

	private static final int PORTFOLIO_PIXELS_PER_METER = 2123;

	/**
	 * Returns horizontal pixel density per meter or zero if unknown.
	 */
	public final int getXPixelsPerMeter()
	{
		switch (this.resolution) {
		case RECOILResolution.APPLE_I_I1X1:
			return 1530;
		case RECOILResolution.XE1X1:
		case RECOILResolution.XE2X1:
		case RECOILResolution.XE4X1:
		case RECOILResolution.XE2X2:
		case RECOILResolution.XE4X2:
		case RECOILResolution.XE4X4:
		case RECOILResolution.XE8X8:
		case RECOILResolution.VBXE1X1:
		case RECOILResolution.VBXE2X1:
			return this.ntsc ? 1530 : 1261;
		case RECOILResolution.PORTFOLIO1X1:
			return 2123;
		case RECOILResolution.BBC1X1:
		case RECOILResolution.BBC2X1:
			return this.ntsc ? 1710 : 1423;
		case RECOILResolution.BBC1X2:
			return this.ntsc ? 3421 : 2846;
		case RECOILResolution.VIC201X1:
		case RECOILResolution.VIC202X1:
			return this.ntsc ? 1749 : 1574;
		case RECOILResolution.C161X1:
		case RECOILResolution.C162X1:
			return this.ntsc ? 1530 : 1261;
		case RECOILResolution.C641X1:
		case RECOILResolution.C642X1:
			return this.ntsc ? 1749 : 1402;
		case RECOILResolution.MSX11X1:
		case RECOILResolution.MSX14X4:
		case RECOILResolution.MSX21X1:
		case RECOILResolution.MSX2_PLUS1X1:
			return 1148;
		case RECOILResolution.MSX21X2:
		case RECOILResolution.MSX21X1I:
		case RECOILResolution.MSX22X1I:
		case RECOILResolution.MSX2_PLUS2X1I:
			return 2296;
		case RECOILResolution.ORIC1X1:
			return 1067;
		case RECOILResolution.VECTOR1X1:
			return 1067;
		case RECOILResolution.ZX811X1:
			return 1156;
		case RECOILResolution.SPECTRUM1X1:
		case RECOILResolution.SPECTRUM4X4:
		case RECOILResolution.SPECTRUM_ULA_PLUS1X1:
		case RECOILResolution.ZX_EVOLUTION1X1:
		case RECOILResolution.SPECTRUM_NEXT1X1:
		case RECOILResolution.TIMEX1X1:
			return 1245;
		case RECOILResolution.TIMEX1X2:
			return 2490;
		default:
			return 0;
		}
	}

	/**
	 * Returns vertical pixel density per meter or zero if unknown.
	 */
	public final int getYPixelsPerMeter()
	{
		switch (this.resolution) {
		case RECOILResolution.APPLE_I_I1X1:
			return 1312;
		case RECOILResolution.XE1X1:
		case RECOILResolution.XE2X1:
		case RECOILResolution.XE4X1:
		case RECOILResolution.XE2X2:
		case RECOILResolution.XE4X2:
		case RECOILResolution.XE4X4:
		case RECOILResolution.XE8X8:
		case RECOILResolution.VBXE1X1:
		case RECOILResolution.VBXE2X1:
			return this.ntsc ? 1312 : 1312;
		case RECOILResolution.PORTFOLIO1X1:
			return 2123;
		case RECOILResolution.BBC1X1:
		case RECOILResolution.BBC2X1:
			return this.ntsc ? 1312 : 1312;
		case RECOILResolution.BBC1X2:
			return this.ntsc ? 2624 : 2624;
		case RECOILResolution.VIC201X1:
		case RECOILResolution.VIC202X1:
		case RECOILResolution.C161X1:
		case RECOILResolution.C162X1:
			return this.ntsc ? 1312 : 1312;
		case RECOILResolution.C641X1:
		case RECOILResolution.C642X1:
			return this.ntsc ? 1312 : 1312;
		case RECOILResolution.MSX11X1:
		case RECOILResolution.MSX14X4:
		case RECOILResolution.MSX21X1:
		case RECOILResolution.MSX2_PLUS1X1:
			return 1312;
		case RECOILResolution.MSX21X2:
		case RECOILResolution.MSX21X1I:
		case RECOILResolution.MSX22X1I:
		case RECOILResolution.MSX2_PLUS2X1I:
			return 2624;
		case RECOILResolution.ORIC1X1:
			return 1312;
		case RECOILResolution.VECTOR1X1:
			return 1312;
		case RECOILResolution.ZX811X1:
			return 1312;
		case RECOILResolution.SPECTRUM1X1:
		case RECOILResolution.SPECTRUM4X4:
		case RECOILResolution.SPECTRUM_ULA_PLUS1X1:
		case RECOILResolution.ZX_EVOLUTION1X1:
		case RECOILResolution.SPECTRUM_NEXT1X1:
		case RECOILResolution.TIMEX1X1:
			return 1312;
		case RECOILResolution.TIMEX1X2:
			return 2624;
		default:
			return 0;
		}
	}

	/**
	 * Returns horizontal pixel density per inch or zero if unknown.
	 */
	public final float getXPixelsPerInch()
	{
		return (float) (getXPixelsPerMeter() * 0.0254);
	}

	/**
	 * Returns vertical pixel density per inch or zero if unknown.
	 */
	public final float getYPixelsPerInch()
	{
		return (float) (getYPixelsPerMeter() * 0.0254);
	}

	/**
	 * Returns the number of alternating frames the pictures is composed of.
	 * 
	 * <ul>
	 * <li>1 means the picture doesn't flicker.</li>
	 * <li>2 means the picture is displayed by quickly alternating two sub-pictures.</li>
	 * <li>3 means the picture is displayed by alternating three sub-pictures.</li>
	 * </ul>
	 */
	public final int getFrames()
	{
		return this.frames;
	}
	private byte[] colorInUse = null;
	private int colors;

	private static final int UNKNOWN_COLORS = -1;
	private final int[] palette = new int[256];
	private byte[] indexes = null;
	private int indexesLength = 0;

	/**
	 * Calculates palette for the decoded picture.
	 */
	private void calculatePalette()
	{
		if (this.colorInUse == null)
			this.colorInUse = new byte[2097152];
		Arrays.fill(this.colorInUse, 0, 2097152, (byte) 0);
		this.colors = 0;
		Arrays.fill(this.palette, 0);
		int pixelsCount = this.width * this.height;
		for (int pixelsOffset = 0; pixelsOffset < pixelsCount; pixelsOffset++) {
			int rgb = this.pixels[pixelsOffset];
			int i = rgb >> 3;
			int mask = 1 << (rgb & 7);
			if ((this.colorInUse[i] & 0xff & mask) == 0) {
				this.colorInUse[i] |= mask;
				if (this.colors < 256)
					this.palette[this.colors] = rgb;
				this.colors++;
			}
		}
	}

	/**
	 * Returns number of unique colors in the decoded picture.
	 */
	public final int getColors()
	{
		if (this.colors == -1)
			calculatePalette();
		return this.colors;
	}

	/**
	 * Find the index of the <code>rgb</code> color in the sorted palette.
	 */
	private int findInSortedPalette(int rgb)
	{
		int left = 0;
		int right = this.colors;
		while (left < right) {
			int index = (left + right) >> 1;
			int paletteRgb = this.palette[index];
			if (rgb == paletteRgb)
				return index;
			if (rgb < paletteRgb)
				right = index;
			else
				left = index + 1;
		}
		return 0;
	}

	/**
	 * Converts the decoded picture to palette-indexed.
	 * Returns palette of 256 0xRRGGBB entries.
	 * Call <code>GetColors()</code> for the actual number of colors.
	 * Returns <code>null</code> if conversion fails,
	 * because there are more than 256 colors.
	 */
	public final int[] toPalette()
	{
		if (this.colors == -1)
			calculatePalette();
		if (this.colors > 256)
			return null;
		Arrays.sort(this.palette, 0, this.colors);
		int pixelsLength = this.width * this.height;
		if (this.indexesLength < pixelsLength) {
			this.indexesLength = pixelsLength;
			this.indexes = null;
			this.indexes = new byte[pixelsLength];
		}
		for (int i = 0; i < pixelsLength; i++)
			this.indexes[i] = (byte) findInSortedPalette(this.pixels[i]);
		return this.palette;
	}

	/**
	 * Returns the palette-indexed picture,
	 * as a bitmap of <code>GetHeight()</code> rows of <code>GetWidth()</code> pixels.
	 * Call after <code>ToPalette()</code> returns non-null.
	 */
	public final byte[] getIndexes()
	{
		return this.indexes;
	}

	private static final byte[] DECODE_COCO_CLP_HEADER = { 0, 0, 0, 3, 1, 94, 0, 0, 32, 0, 32, 1, 1, 44, 0, 10,
		0, 56, 0, 32, 0, 56, 0, 32, 5 };

	private static final int[] DECODE_P11_PALETTE = { 524032, 16776960, 3868927, 13369403 };

	private static final int[] DECODE_SGX_PALETTE4 = { 16777215, 11184810, 0, 5592405 };

	private static final int[] DECODE_SGX_PALETTE16 = { 16777088, 0, 16744448, 8388608, 65535, 128, 8421631, 255, 16777215, 32768, 65280, 16711935, 16776960, 8421504, 16744576, 16711680 };

	private static final byte[] SET_AMSTRAD_FIRMWARE_PALETTE_TRI_LEVEL = { 0, -128, -1 };

	private static final byte[] DECODE_ZX_RGB_FRAME_COMPONENTS = { 16, 8, 0 };

	private static final byte[] DECODE3_FRAME_COMPONENTS = { 0, 16, 8 };

	private static final byte[] SET_SC8_PALETTE_BLUES = { 0, 2, 4, 7 };

	private static final byte[] DECODE_SC8_SPRITE_PALETTE = { 0, 0, 2, 0, 48, 0, 50, 0, 0, 3, 2, 3, 48, 3, 50, 3,
		114, 4, 7, 0, 112, 0, 119, 0, 0, 7, 7, 7, 112, 7, 119, 7 };

	private static final byte[] UNPACK_MAG_DELTA_X = { 0, 2, 4, 8, 0, 2, 0, 2, 4, 0, 2, 4, 0, 2, 4, 0 };

	private static final byte[] UNPACK_MAG_DELTA_Y = { 0, 0, 0, 0, 1, 1, 2, 2, 2, 4, 4, 4, 8, 8, 8, 16 };

	private static final int[] DECODE_BRUS_PALETTE = { 0, 5592405, 170, 5592575, 43520, 5635925, 43690, 5636095, 11141120, 16733525, 11141290, 16733695, 11162880, 16777045, 11184810, 16777215 };

	private static final byte[] DECODE_P4I_LOGO_COLORS = { 0, 49, 81, 113 };

	private static final byte[] DECODE_GODOT_BY_BRIGHTNESS = { 0, 6, 9, 11, 2, 4, 8, 12, 14, 10, 5, 15, 3, 7, 13, 1 };

	private static final byte[] DECODE_MLE_FRAME_COLORS = { 0, 9, 8, 5 };

	private static final byte[] GET_SHF_PIXEL_SPRITES = { -128, -124, -123, -119, -118, -114, -113, -109, -108, -104, -103, -99, -98, -94, -93, -89,
		-88, -84, -83, -79, -78, -74, -73, -69, -68, -64, -63, -59, -58, -54, -53, -49,
		-48, -44, -43, -39, -38, -34, -33, -29, -28, -24, -23, -22, -21, -20, -19, -18,
		-17, -16, -15, -14, -13, -12, -11, -10, -9, 30, 46, 62, 78, 94, 110, 126 };

	private static final byte[] DECODE_XFL_SPRITES = { -28, -27, -23, -22, -21, -20, -19, -18, -17, -16, -15, -14, -13, -10, -9, -8,
		14, 30, 46, 62, 78, 94, 110, 126, -128, -127, -123, -122, -118, -117, -113, -112,
		-108, -107, -103, -102, -98, -97, -93, -92, -88, -87, -83, -82, -78, -77, -73, -72,
		-68, -67, -63, -62, -58, -57, -53, -52, -48, -47, -43, -42, -38, -37, -33, -32 };

	private static final byte[] DECODE_MPP_SCREEN_FIRST_CHANGE_X = { 33, 9, 4, 69 };

	private static final byte[] DECODE_MPP_SCREEN_RIGHT_BORDER_COLOR = { 32, 16, 32, 127 };

	private static final byte[] DECODE_MPP_MODE_COLORS_PER_LINE = { 52, 46, 54, 48 };

	private static final byte[] DECODE_HRM_COLOR_OFFSETS = { 80, 72, 40, 32 };

	private static final int[] DECODE_INFO_OS1_PALETTE = { 5614335, 16777215, 0, 16746496 };

	private static final int[] DECODE_INFO_OS2_PALETTE = { 9803157, 0, 16777215, 3893154, 8092539, 11513775, 11178108, 16755095 };

	private static final byte[] IS_HAME_MAGIC = { -94, -11, -124, -36, 109, -80, 127 };

	private static final byte[] DECODE_HIP_GR10_COLORS = { 0, 0, 2, 4, 6, 8, 10, 12, 14 };

	private static final byte[] DECODE_TIP_COLORS = { 0, 2, 4, 6, 8, 10, 12, 14, 0 };

	private static final byte[] DECODE_ICE_FRAME_ROW2CHAR1 = { 64, 0, 32, 96, -64, -128, -96, -32, 64, 0, 32, 96, -64, -128, -96, -32 };

	private static final byte[] DECODE_ICE_FRAME_ROW2CHAR2 = { 64, 0, 32, 96, -64, -128, -96, -32, -64, -128, -96, -32, 64, 0, 32, 96 };

	private static final byte[] DECODE_ATARI8_ICE_ICE20_GTIA11_COLORS = { 0, 1, 2, 3, 5, 7, 8 };

	private static final byte[] DRAW_SPC_BRUSH_BRUSHES = { 0, 0, 0, 0, 0, 0, 16, 16, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 48, 48, 48, 48, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 16, 56, 56, 56, 56, 16, 0, 0, 0, 0, 0, 0,
		0, 0, 16, 56, 56, 124, 124, 124, 124, 56, 56, 16, 0, 0, 0, 0,
		0, 24, 24, 60, 60, 126, 126, 126, 126, 60, 60, 24, 24, 0, 0, 0,
		16, 56, 124, 124, 124, -2, -2, -2, -2, 124, 124, 124, 56, 16, 0, 0,
		0, 0, 16, 40, 40, 80, 60, 120, 20, 40, 40, 16, 0, 0, 0, 0,
		16, 40, 84, 40, 84, -70, 124, 124, -70, 84, 40, 84, 40, 16, 0, 0 };

	private static final int[] DECODE_ATARI8_SPC_PATTERNS = { 0, 21845, 43690, 65535, 4420, 8840, 13260, 26265, 30685, 48110, 5457, 10914, 16371, 16388, 27302, 32759,
		32776, 38233, 49147, 49164, 54621, 60078, 21896, 8908, 13124, 17561, 17629, 30617, 35054, 34918, 39406, 52343,
		52411, 56763, 7089, 5465, 5469, 38237, 16392, 16396, 32780, 27308, 10926, 27298, 32763, 16379, 49143, 21892,
		8900, 13128, 17553, 17617, 30609, 35042, 34914, 39393, 52339, 52403, 56755, 21900, 8904, 13132, 17565, 17625,
		30621, 35046, 34926, 39397, 52347, 52407, 56759 };

	private static final byte[] DECODE_ATARI8_SPC_DEFAULT_COLORS = { 0, 21, -107, 54 };

	private static final byte[] DECODE_G2F_UNPACKED_PRIORS = { 4, 2, 1, 8, 0 };

	private static final byte[] DECODE_G2F_UNPACKED_GTIA_MODES = { 64, 64, 64, 64, 64, -128, -64, 64 };

	private static final int[] DECODE_TANDY_PNT_PALETTE = { 0, 153, 39168, 3381657, 10027008, 13382604, 13395456, 10066329, 10053171, 6697983, 3394560, 6737100, 16764108, 16751103, 16776960, 16777215 };

	private static final int[] DECODE_BK_COLOR_FRAME_PALETTE = { 0, 255, 65280, 16711680, 0, 16776960, 16711935, 16711680, 0, 65535, 255, 16711935, 0, 65280, 65535, 16776960,
		0, 16711935, 65535, 16777215, 0, 16777215, 16777215, 16777215, 0, 12582912, 8388608, 16711680, 0, 65280, 65535, 16776960,
		0, 12583104, 8388863, 16711935, 0, 16776960, 8388863, 12582912, 0, 16776960, 12583104, 16711680, 0, 65535, 16776960, 16711680,
		0, 16711680, 65280, 65535, 0, 65535, 16776960, 16777215, 0, 16776960, 65280, 16777215, 0, 65535, 65280, 16777215 };
}
