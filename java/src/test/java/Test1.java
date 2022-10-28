/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JComponent;
import javax.swing.JFrame;

import net.sf.recoil.RECOIL;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-28 nsano initial version <br>
 */
class Test1 {

    @Test
    void test1() throws Exception {
        RECOIL recoil = new RECOIL();
        Path in = Paths.get("/Users/nsano/src/vavi/vavi-image/src/test/resources/test.zim");
        boolean r = recoil.decode(in.getFileName().toString(), Files.readAllBytes(in), (int) Files.size(in));
Debug.println("done: " + r);
        int w = recoil.getWidth();
        int h = recoil.getHeight();
Debug.println("size: " + w + "x" + h);
        // [__rrggbb] aa is 0
        int[] pixels = recoil.getPixels();
Debug.println("pixels: " + pixels.length + ", " + w * h);
        int[] palette = recoil.toPalette();
//        int colors = palette.length;
//Debug.println("colors: " + colors);
//        int bits = (int) (Math.log10(colors) / Math.log10(2));
//Debug.println("bits: " + bits);
//        byte[] rs = new byte[colors];
//        byte[] gs = new byte[colors];
//        byte[] bs = new byte[colors];
//        for (int i = 0; i < colors; i++) {
//            rs[i] = (byte) (palette[i] >> 16);
//            gs[i] = (byte) (palette[i] >> 8);
//            bs[i] = (byte) (palette[i] >> 0);
//        }
//        IndexColorModel icm = new IndexColorModel(bits, colors, rs, bs, gs);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] b = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int i = 0;
        for (int p : pixels) {
            b[i++] = 0xff000000 | p & 0xff0000 | p & 0xff00 | p & 0xff;
        }
        show(image);
        while (true) Thread.yield();
    }

    void show(BufferedImage image) {
        JFrame frame = new JFrame("ZIM");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent panel = new JComponent() {
            @Override public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
