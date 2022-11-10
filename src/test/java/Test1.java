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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.JFrame;

import net.sf.recoil.RECOIL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-28 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class Test1 {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String image = "src/test/resources/test.img";

    @Property
    String unknownImage = "src/test/resources/test.img";

    @BeforeEach
    void setup() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
        RECOIL recoil = new RECOIL();
        Path in = Paths.get(image);
        String format = "ZIM";
        boolean r = recoil.decode("." + format, Files.readAllBytes(in), (int) Files.size(in));
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
        show(image, format);
    }

    /** */
    void show(BufferedImage image, String format) {
        JFrame frame = new JFrame(format);
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
        while (true) Thread.yield();
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {

        ImageReader ir = ImageIO.getImageReadersByFormatName("recoil").next();
        ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(Paths.get(this.image)));
        ir.setInput(iis);
        BufferedImage image = ir.read(0);

        show(image, "ZIM");
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test3() throws Exception {
        String type = "ZIM";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.image));

        show(image, type);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test4() throws Exception {
        RECOIL recoil = new RECOIL();
        Path in = Paths.get(unknownImage);
        String type = recoil.trialDecode(Files.readAllBytes(in), (int) Files.size(in));
Debug.println("done: " + type);
        if (type == null) throw new IllegalStateException("cannot decode");
        int w = recoil.getWidth();
        int h = recoil.getHeight();
Debug.println("size: " + w + "x" + h);
        int[] pixels = recoil.getPixels();
Debug.println("pixels: " + pixels.length + ", " + w * h);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] b = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int i = 0;
        for (int p : pixels) {
            b[i++] = 0xff000000 | p & 0xff0000 | p & 0xff00 | p & 0xff;
if (i >= b.length) break;
        }
        show(image, type);
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test5() throws Exception {
        String type = "PIC";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.unknownImage));

        show(image, type);
    }
}
