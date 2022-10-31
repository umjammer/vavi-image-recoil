[![Release](https://jitpack.io/v/vavi-image-recoil/${artifactId}.svg)](https://jitpack.io/#vavi-image-recoil/${artifactId})
[![Java CI](https://github.com/vavi-image-recoil/${artifactId}/actions/workflows/maven.yml/badge.svg)](https://github.com/vavi-image-recoil/${artifactId}/actions/workflows/maven.yml)
[![CodeQL](https://github.com/vavi-image-recoil/${artifactId}/actions/workflows/codeql.yml/badge.svg)](https://github.com/vavi-image-recoil/${artifactId}/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)

# vavi-image-recoil

<img src="https://sourceforge.net/p/recoil/code/ci/master/tree/recoil-512x512.png?format=raw" width="100" /><sub>© recoil</sub>

Java ImageIO SPI for retro images powered by [recoil](https://sourceforge.net/projects/recoil/) based on 0eaca83b836e84bdf7843f08c3579341e8c819f6

recoil supports [over 500 image formats](https://recoil.sourceforge.net/formats.html)!

## Install

https://jitpack.io/#umjammer/vavi-image-recoil

## Usage

```java
    BufferedImage image = ImageIO.read(Paths.get("/foo/bar.zim").toFile());
```


## TODO

 * ~~ImageIO SPI~~

## References

 * https://github.com/pfusik/cito (.ci to .java (or many) compiler)

---

ORIGINAL

RECOIL - Retro Computer Image Library
=====================================

RECOIL is a viewer of pictures in native formats of vintage computers:
Amiga, Amstrad CPC, Apple II, Atari 8-bit, Atari Portfolio, Atari ST/TT/Falcon,
BBC Micro, Commodore VIC-20, Commodore 64, Commodore 16/116/Plus4,
Commodore 128, Electronika BK, FM Towns, HP 48, Macintosh 128K, MSX,
NEC PC-80/88/98, Oric, Psion Series 3, SAM Coupé, Sharp X68000, Tandy 1000,
Timex 2048, TRS-80, TRS-80 Color Computer, Vector-06C, ZX81 and ZX Spectrum.

RECOIL opens over 500 different file formats
and is available on Android, Windows, macOS, Linux and HTML 5 browsers.

RECOIL is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published
by the Free Software Foundation; either version 2 of the License,
or (at your option) any later version.

See the INSTALL file for build instructions.

For more information, visit the website: http://recoil.sourceforge.net/
