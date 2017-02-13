package org.hifly.bikedump.utility;

import java.util.Properties;

public class PropUtility {

    public static void insertInProperties(Properties prop, String key, String value) {
        if(value!=null && !value.equals(""))
            prop.setProperty(key, value);

    }
}
