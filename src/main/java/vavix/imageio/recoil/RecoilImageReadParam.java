/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.imageio.recoil;

import java.awt.Dimension;
import java.util.logging.Level;
import javax.imageio.ImageReadParam;

import vavi.util.Debug;


/**
 * RecoilImageReadParam.
 * <p>
 * system property
 * <li>"vavix.imageio.recoil.RecoilImageReadParam.type" ... type e.g "ZIM"</li>
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2022/10/31 nsano initial version <br>
 */
public class RecoilImageReadParam extends ImageReadParam {

    /** */
    private String type;

    {
        type = System.getProperty("vavix.imageio.recoil.RecoilImageReadParam.type", "ZIM");
Debug.println(Level.INFO, "wrong syntax: " + type);
    }

    /** */
    public String getType() {
        return type;
    }
}

/* */
