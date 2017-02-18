package org.hifly.bikedump.utility;

import junit.framework.Assert;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class FileStream {

    //@org.junit.Test
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


    public static void main(String [] args) throws Exception {
        String test = "LA Mattina #3 riceve la #due";
        String aaa = test(test);
        System.out.println(aaa);
    }

    public static String test(String test) throws Exception {
        if(test == null || test.length() == 0)
            return "";
        else {
            int index = test.indexOf(' ');
            if(index == -1) {
                return URLEncoder.encode(test, "UTF-8");
            } else {
                return URLEncoder.encode(test.substring(0, index), "UTF-8") + " " + test(test.substring(index + 1));
            }

        }
    }
}
