package org.hifly.bikedump.utility;

import junit.framework.Assert;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class FileStream {

    @org.junit.Test
    public void testOpenFileWithSpecialCharsInName() throws Exception {
        try {
            URL location = new URL("file://./routes/" + URLEncoder.encode("Mattina #3.gpx", "UTF-8"));
            URLConnection connect = location.openConnection();
            if (!(connect instanceof HttpURLConnection)) {
                InputStream stream = connect.getInputStream();
                Assert.assertNotNull(stream);
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

}
