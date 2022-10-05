package org.hifly.bikedump.controller;

import org.hifly.bikedump.domain.Track;
import org.hifly.bikedump.gps.GPSDocument;
import org.hifly.bikedump.gps.GPX10Document;
import org.hifly.bikedump.gps.GPXDocument;
import org.hifly.bikedump.gps.TCX2Document;
import org.hifly.bikedump.utility.StreamUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;


public class GPSController {

    protected static Logger log = LoggerFactory.getLogger(GPSController.class);
    private static DocumentBuilderFactory factory;
    private static XPath xpath;
    private static final String FILE_URI = "file://";

    static {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        xpath = XPathFactory.newInstance().newXPath();
    }

    public static Map.Entry<Track, StringBuffer> extractTrackFromGpx(String filename) {
        Track track = null;
        StringBuffer sb = new StringBuffer();
        List<Track> tracks;
        GPSDocument doc = null;

        try {
            String gpxVersion = null;
            Document docForXpath = getXmlDocumentFromFileName(filename);
            Object evaluation = xpath.compile("//@version").evaluate(docForXpath, XPathConstants.STRING);
            if (evaluation != null)
                gpxVersion = (String) evaluation;
            else
                log.error("file is not GPX:" + filename);

            switch (gpxVersion) {
                case "1.1":
                    doc = new GPXDocument();
                    break;
                case "1.0":
                    doc = new GPX10Document();
                    break;
                default:
                    String error = "version of GPX is not compatible [" + gpxVersion + "]:" + filename;
                    log.error(error);
                    sb.append(error);
                    break;
            }

            long time1 = System.currentTimeMillis();
            tracks = doc.extractTrack(filename);
            long time2 = System.currentTimeMillis();
            log.debug("Extract info from Track Duration [" + filename + "]:" + (time2 - time1));

            //TODO manage a list of tracks: a single file can contain multiple tracks
            if (tracks != null && !tracks.isEmpty())
                track = tracks.get(0);
        } catch (SAXParseException sax) {
            log.error("file is not GPX [" + sax.getMessage() + "]:" + filename);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("can't load [" + ex.getMessage() + "]:" + filename);
            sb.append("can't load:").append(filename);
        }

        return new AbstractMap.SimpleImmutableEntry<>(track, sb);
    }

    public static Map.Entry<Track, StringBuffer> extractTrackFromTcx(String filename) {
        Track track = null;
        StringBuffer sb = new StringBuffer();
        TCX2Document doc = new TCX2Document();
        List<Track> tracks = null;
        try {
            Document docForXpath = getXmlDocumentFromFileName(filename);
            Object result = xpath.compile("//TrainingCenterDatabase").evaluate(docForXpath, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            if (nodes == null)
                log.error("file is not TCX:" + filename);

            long time1 = System.currentTimeMillis();
            tracks = doc.extractTrack(filename);
            long time2 = System.currentTimeMillis();
            log.debug("Extract info from Track Duration [" + filename + "]:" + (time2 - time1));

            //TODO manage a list of tracks: a single file can contain multiple tracks
            if (tracks != null && !tracks.isEmpty())
                track = tracks.get(0);
        } catch (SAXParseException sax) {
            log.error("file is not TCX [" + sax.getMessage() + "]:" + filename);
        } catch (Exception ex) {
            log.error("can't load [" + ex.getMessage() + "]:" + filename);
            sb.append("can't load:").append(filename);
        }

        //TODO manage a list of tracks: a single file can contain multiple tracks
        if (tracks != null && !tracks.isEmpty())
            track = tracks.get(0);
        return new AbstractMap.SimpleImmutableEntry<>(track, sb);
    }

    private static Document getXmlDocumentFromFileName(String filename) throws Exception {
        DocumentBuilder builder = factory.newDocumentBuilder();
        Path path = Paths.get(filename);
        String filenamePart = path.getFileName().toString();
        //TODO consider URI from external sources
        return builder.parse(FILE_URI + StreamUtility.getPathFromAbsoulutePath(filename) + StreamUtility.encodeFilenameOmittingWhiteSpaces(filenamePart, "UTF-8"));

    }
}
