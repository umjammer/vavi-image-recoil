/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.JFrame;

import net.sf.recoil.RECOIL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.ByteUtil;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2022-10-28 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property
    String dir;

    @Property
    String image = "src/test/resources/test.img";

    @Property
    String unknownImage = "src/test/resources/test.img";

    @Property
    String mkiImage;

    @Property
    String picImage;

    @Property
    String magImage;

    @Property
    String piImage;

    @Property
    String davinchiImage;

    @Property
    String art88Image;

    @BeforeEach
    void setup() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    @DisplayName("zim, prototype")
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

    /** using cdl cause junit stops awt thread suddenly */
    void show(BufferedImage image, String format) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        JFrame frame = new JFrame(format);
        JComponent panel = new JComponent() {
            @Override public void paintComponent(Graphics g) { g.drawImage(image, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, image.getWidth(), image.getHeight(), this); }
        };
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { cdl.countDown(); }
        });
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) { panel.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight())); }
        });
        frame.getContentPane().add(panel);
Debug.printf("image: %dx%d", image.getWidth(), image.getHeight());
        frame.getContentPane().setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.pack();
        frame.setVisible(true);
        cdl.await();
    }

    @Test
    @DisplayName("zim, spi specifying format type")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {

        ImageReader ir = ImageIO.getImageReadersByFormatName("recoil").next();
        ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(Paths.get(this.image)));
        ir.setInput(iis);
        BufferedImage image = ir.read(0);

        show(image, "ZIM");
    }

    @Test
    @DisplayName("zim, spi w/ image read param via system property")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test3() throws Exception {
        String type = "ZIM";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.image));

        show(image, type);
    }

    @Test
    @DisplayName("find unknown format")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test4() throws Exception {
        Path path = Paths.get(unknownImage);
Debug.println("path: " + unknownImage);
        guess(path);
    }

    /** find unknown format */
    void guess(Path path) throws Exception {
        RECOIL recoil = new RECOIL();
        Set<String> types = recoil.trialDecode(Files.readAllBytes(path), (int) Files.size(path));
Debug.println("done: " + types);
        if (types.isEmpty()) throw new IllegalStateException("cannot decode");
        for (String type : types) {
            String ext;
            if (type.contains(",")) {
                ext = type.split(",")[1].toUpperCase();
            } else {
                ext = type.toUpperCase();
            }
Debug.println(ext);
            System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", ext);
            BufferedImage image = null;
            try {
                image = ImageIO.read(path.toFile());
            } catch (Exception e) {
Debug.println(Level.WARNING, ext + ": " + e.toString());
                continue;
            }
            if (image == null || image.getWidth() < 640) { // expect pc88/98 image so 640x400 or 640x200
Debug.println(Level.WARNING, ext + ": failed");
                continue;
            }
Debug.println(type + " / " + types);
            show(image, type);
        }
    }

    @Test
    @DisplayName("mag, unknown format via spi w/ guess")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test5() throws Exception {
        String type = "MAG";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.magImage));

        show(image, type);
    }

    @Test
    @DisplayName("guess files in dir")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test6() throws Exception {
        String ext = "PIC";
        Path dirPath = Paths.get(dir);
        Files.list(dirPath)
                .filter(path ->
                        path.getFileName().toString().endsWith("." + ext.toLowerCase()) ||
                        path.getFileName().toString().endsWith("." + ext.toUpperCase()))
                .forEach(path -> {
            try {
Debug.println("path: " + path);
                guess(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    @DisplayName("maki, spi w/ image read param via system property")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test7() throws Exception {
        String type = "MKI";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.mkiImage));

        show(image, type);
    }

    @Test
    @DisplayName("pic, spi w/ image read param via system property")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test8() throws Exception {
        String type = "PIC";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.picImage));

        show(image, type);
    }

    @Test
    @DisplayName("pi, spi w/ image read param via system property")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test9() throws Exception {
        String type = "pi";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.piImage));

        show(image, type);
    }

    @Test
    @DisplayName("files in dir")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test10() throws Exception {
//        String type = "PIC";
        String type = "IMG";
        Path dirPath = Paths.get(dir);
        Files.list(dirPath)
                .filter(path ->
                        path.getFileName().toString().endsWith("." + type.toLowerCase()) ||
                        path.getFileName().toString().endsWith("." + type.toUpperCase()))
                .forEach(path -> {
            try {
Debug.println("path: " + path);
                System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
                BufferedImage image = ImageIO.read(path.toFile());

                show(image, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    @DisplayName("davinchi, spi w/ image read param via system property")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test11() throws Exception {
        String type = "img";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.davinchiImage));

        show(image, type);
    }

    @Test
    @DisplayName("type: method mapping table")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test12() throws Exception {
        Files.readAllLines(Paths.get("tmp/recoil.tsv")).forEach(l -> {
            String[] kv = l.split("\t");
            String kt = kv[0].startsWith("0x") ? kv[0].substring(2) : String.format("%x", Integer.parseInt(kv[0]));
            List<Byte> bl =  ByteUtil.toList(ByteUtil.hexStringToBytes(kt));
            Collections.reverse(bl);
            String k = new String(ByteUtil.toByteArray(bl), StandardCharsets.US_ASCII);
            String[] vs = null;
            if (kv.length > 1) {
                vs = kv[1].split(" ");
            }
            System.err.println("\"" + k + "\": " + Arrays.toString(vs));
        });
    }

    @Test
    @DisplayName("artmaster88, spi w/ image read param via system property")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test13() throws Exception {
        String type = "img";
        System.setProperty("vavix.imageio.recoil.RecoilImageReadParam.type", type);
        BufferedImage image = ImageIO.read(new File(this.art88Image));

        show(image, type);
    }
}
