/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.imageio.recoil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import net.sf.recoil.RECOIL;
import vavi.util.Debug;


/**
 * RecoilImageReaderSpi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 221029 nsano initial version <br>
 */
public class RecoilImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "http://www.vavi.com";
    private static final String Version = "6.3.1";
    private static final String ReaderClassName =
        "vavix.imageio.recoil.RecoilImageReader";
    private static final String[] Names = {
        "RECOIL", "recoil"
    };
    private static final String[] Suffixes = {
        "zim", "ZIM", "img"
    };
    private static final String[] mimeTypes = {
        "image/x-zim"
    };
    static final String[] WriterSpiNames = {
        /*"vavix.imageio.recoil.ReccoilMasterWriterSpi"*/
    };
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "recoil";
    private static final String NativeImageMetadataFormatClassName =
        /*"vavi.imageio.recoil.RecoilMasterMetaData"*/ null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public RecoilImageReaderSpi() {
        super(VendorName,
              Version,
              Names,
              Suffixes,
              mimeTypes,
              ReaderClassName,
              new Class[] { ImageInputStream.class, InputStream.class },
              WriterSpiNames,
              SupportsStandardStreamMetadataFormat,
              NativeStreamMetadataFormatName,
              NativeStreamMetadataFormatClassName,
              ExtraStreamMetadataFormatNames,
              ExtraStreamMetadataFormatClassNames,
              SupportsStandardImageMetadataFormat,
              NativeImageMetadataFormatName,
              NativeImageMetadataFormatClassName,
              ExtraImageMetadataFormatNames,
              ExtraImageMetadataFormatClassNames);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Recoil";
    }

    @Override
    public boolean canDecodeInput(Object obj) throws IOException {

        if (obj instanceof ImageInputStream) {
            ImageInputStream stream = (ImageInputStream) obj;
            stream.mark();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[8192];
            while (true) {
                int r = stream.read(b, 0, b.length);
                if (r < 0) break;
                baos.write(b, 0, r);
            }
            int l = baos.size();
Debug.println(Level.FINE, "size: " + l);
            RECOIL recoil = new RECOIL();
            String format = "ZIM"; // TODO how to deal???
            return recoil.decode("." + format, baos.toByteArray(), baos.size());
        } else {
Debug.println(Level.FINE, obj);
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new RecoilImageReader(this);
    }
}

/* */
