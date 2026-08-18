/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.imageio.recoil;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import net.sf.recoil.RECOIL;
import vavi.imageio.WrappedImageInputStream;

import static java.lang.System.getLogger;


/**
 * RecoilImageReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 221029 nsano initial version <br>
 */
public class RecoilImageReader extends ImageReader {

    private static final Logger logger = getLogger(RecoilImageReader.class.getName());

    /** */
    private IIOMetadata metadata;

    /** */
    private BufferedImage image;

    /** */
    public RecoilImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IIOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IIOException {
        return image.getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IIOException {
        return image.getHeight();
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {

        if (param == null) {
            param = getDefaultReadParam();
        }

        InputStream stream = new WrappedImageInputStream((ImageInputStream) input);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[8192];
            while (true) {
                int r = stream.read(b, 0, b.length);
                if (r < 0) break;
                baos.write(b, 0, r);
            }
            int l = baos.size();
logger.log(Level.DEBUG, "size: " + l);

            RECOIL recoil = new RECOIL();
            boolean r = recoil.decode("." + ((RecoilImageReadParam) param).getType(), baos.toByteArray(), baos.size());
            int w = recoil.getWidth();
            int h = recoil.getHeight();
logger.log(Level.DEBUG, "size: " + w + "x" + h);
            int[] pixels = recoil.getPixels();
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] buf = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            int i = 0;
            for (int p : pixels) {
                buf[i++] = 0xff000000 | p & 0xff0000 | p & 0xff00 | p & 0xff;
            }

            return image;
        } catch (IOException e) {
            throw new IIOException(e.getMessage(), e);
        }
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IIOException {
        return metadata;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        return metadata;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
logger.log(Level.DEBUG, "here");
        ImageTypeSpecifier specifier = null;
        java.util.List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new RecoilImageReadParam();
    }
}
