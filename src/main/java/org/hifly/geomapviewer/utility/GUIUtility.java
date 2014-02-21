package org.hifly.geomapviewer.utility;

import java.awt.*;
import java.util.AbstractMap;
import java.util.Map;

/**
 * @author
 * @date 07/02/14
 */
public class GUIUtility {

    public static Map.Entry<Integer,Integer> getScreenDimension() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new AbstractMap.SimpleImmutableEntry<Integer, Integer>(
                (int)screenSize.getWidth(), (int)screenSize.getHeight());
    }
}
