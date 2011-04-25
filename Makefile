PREFIX = /usr/local
MAGICK_VERSION = `MagickCore-config --version`
MAGICK_CFLAGS = `MagickCore-config --cflags --cppflags`
MAGICK_LDFLAGS = `MagickCore-config --ldflags`
MAGICK_LIBS = `MagickCore-config --libs`
MAGICK_CODER_PATH = `MagickCore-config --coder-path`
MAGICK_CONFIG_PATH = `MagickCore-config --exec-prefix`/etc/ImageMagick
TEST_MAGICK = -x "`which MagickCore-config`"

CC = gcc 
CFLAGS = -s -O2 -Wall
INSTALL = install
INSTALL_PROGRAM = $(INSTALL)
INSTALL_DATA = $(INSTALL) -m 644

FORMATS = HIP MIC INT TIP INP HR GR9 PIC CPR CIN CCI APC PLM AP3 ILC RIP FNT SXS MCP GHG HR2 MCH IGE 256 AP2 JGP DGP ESC PZM IST RAW RGB

all: fail2png fail.so

fail2png: fail2png.c pngsave.c fail.c pngsave.h fail.h palette.h
	$(CC) $(CFLAGS) fail2png.c pngsave.c fail.c -lpng -lz -lm -o $@

fail.so: fail.c failmagick.c fail.h palette.h
	@if [ $(TEST_MAGICK) ]; then \
		if [ -z "$(MAGICK_INCLUDE_PATH)" ]; then \
			echo "\nDetected ImageMagick version $(MAGICK_VERSION) on your system."; \
			echo "To build FAIL coder for ImageMagick,\nspecify path to ImageMagick sources, e.g.:"; \
			echo "$ make MAGICK_INCLUDE_PATH=/path/to/im/source\n"; \
		else \
			echo $(CC) $(CFLAGS) $(MAGICK_CFLAGS) -I"$(MAGICK_INCLUDE_PATH)" fail.c failmagick.c \
				-shared $(MAGICK_LDFLAGS) -ldl $(MAGICK_LIBS) -o $@; \
			$(CC) $(CFLAGS) $(MAGICK_CFLAGS) -I"$(MAGICK_INCLUDE_PATH)" fail.c failmagick.c \
				-shared $(MAGICK_LDFLAGS) -ldl $(MAGICK_LIBS) -o $@; \
		fi; \
	fi;

palette.h: raw2c.pl jakub.act
	perl raw2c.pl jakub.act >$@

README.html: README INSTALL
	asciidoc -o $@ -a failsrc README
	perl -pi -e 's/527bbd;/800080;/' $@

clean:
	rm -f fail2png palette.h fail.so coder.xml.new

install: fail2png install-magick
	mkdir -p $(PREFIX)/bin
	$(INSTALL_PROGRAM) fail2png $(PREFIX)/bin/fail2png

uninstall: uninstall-magick
	rm -f $(PREFIX)/bin/fail2png

install-magick: fail.so
	if [ $(TEST_MAGICK) -a -n "$(MAGICK_CODER_PATH)" -a -n "$(MAGICK_CONFIG_PATH)" ]; then \
		perl addcoders.pl $(FORMATS) <$(MAGICK_CONFIG_PATH)/coder.xml >coder.xml.new; \
		mkdir -p "$(MAGICK_CODER_PATH)"; \
		$(INSTALL) fail.so "$(MAGICK_CODER_PATH)/fail.so"; \
		echo "dlname='fail.so'" >"$(MAGICK_CODER_PATH)/fail.la"; \
		mv coder.xml.new "$(MAGICK_CONFIG_PATH)"/coder.xml; \
	fi

uninstall-magick:
	if [ $(TEST_MAGICK) ]; then \
		perl delcoders.pl <$(MAGICK_CONFIG_PATH)/coder.xml >coder.xml.new; \
		rm -f "$(MAGICK_CODER_PATH)/fail.la" "$(MAGICK_CODER_PATH)/fail.so"; \
		mv coder.xml.new "$(MAGICK_CONFIG_PATH)"/coder.xml; \
	fi

.PHONY: all clean install uninstall install-magick uninstall-magick

.DELETE_ON_ERROR:

