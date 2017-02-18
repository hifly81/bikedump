package org.hifly.bikedump.utility;


import java.io.File;
import java.net.URLEncoder;

public class StreamUtility {

    public static String encodeFilenameOmittingWhiteSpaces(String filename, String encoding) throws Exception {

        if (filename == null || filename.length() == 0)
            return "";
        else {
            int index = filename.indexOf(' ');
            if (index == -1) {
                return encodeFilename(filename, encoding);
            } else {
                return URLEncoder.encode(filename.substring(0, index), encoding) + " " + encodeFilename(filename.substring(index + 1), encoding);
            }

        }
    }

    public static String encodeFilename(String filename, String encoding) throws Exception {
        String splChars = ".*[#$%?@].*";
        if (filename.matches(splChars))
            return URLEncoder.encode(filename, encoding);

        return filename;
    }

    public static String getPathFromAbsoulutePath(String absoulutePath) throws Exception {
        return new File(absoulutePath).getParent() + File.separator;
    }
}
