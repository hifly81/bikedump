package org.hifly.bikedump.utility;

import java.awt.*;
import java.util.AbstractMap;
import java.util.Map;

public class GUIUtility {

    public static Map.Entry<Integer,Integer> getScreenDimension() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new AbstractMap.SimpleImmutableEntry<>(
                (int)screenSize.getWidth()-100, (int)screenSize.getHeight()-100);
    }
}
