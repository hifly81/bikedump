package org.hifly.geomapviewer.controller;

import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.gps.GPSDocument;
import org.hifly.geomapviewer.gps.GPX10Document;
import org.hifly.geomapviewer.gps.GPXDocument;
import org.hifly.geomapviewer.gps.TCX2Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 12/02/14
 */
public class GPSController {

    protected static Logger log = LoggerFactory.getLogger(GPSController.class);

    public static Map.Entry<Track,StringBuffer> extractTrackFromGpx(String filename, ProfileSetting profileSetting) {
        Track track = null;
        StringBuffer sb = new StringBuffer();
        List<Track> tracks = null;
        GPSDocument doc = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder;
            //FIXME the evalutaion via XPATH is really slow
            Document docForXpath;
            String gpxVersion = null;
            builder = factory.newDocumentBuilder();
            //TODO consider URI from external sources
            docForXpath = builder.parse("file://"+filename);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expr =
                    xpath.compile("//@version");
            Object evaluation = expr.evaluate(docForXpath, XPathConstants.STRING);
            if(evaluation!=null) {
                gpxVersion = (String) evaluation;
            }
            else {
                log.error("file is not GPX:"+filename);
            }


            if(gpxVersion.equals("1.1")) {
                doc = new GPXDocument(profileSetting);
            }
            else if(gpxVersion.equals("1.0")) {
                doc = new GPX10Document(profileSetting);
            }
            else {
                String error = "version of GPX is not compatible ["+gpxVersion+"]:"+filename;
                log.error(error);
                sb.append(error);
            }

            long time1 = System.currentTimeMillis();
            tracks = doc.extractTrack(filename);
            long time2 = System.currentTimeMillis();
            System.out.println("Extract info from Track Duration ["+filename+"]:" + (time2 - time1));


            //TODO manage a list of tracks: a single file can contain multiple tracks
            if (tracks != null && !tracks.isEmpty()) {
                track = tracks.get(0);
            }
        }
        catch (SAXParseException sax) {
            log.error("file is not GPX ["+sax.getMessage()+"]:"+filename);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            //TODO some tracks are not loaded, explore exception cause
            log.error("can't load [" + ex.getMessage() + "]:" + filename);
            sb.append("can't load:"+filename);
        }

        return new AbstractMap.SimpleImmutableEntry<Track, StringBuffer>(track, sb);
    }

    public static Map.Entry<Track,StringBuffer> extractTrackFromTcx(String filename, ProfileSetting profileSetting) {
        Track track = null;
        StringBuffer sb = new StringBuffer();
        TCX2Document doc = new TCX2Document(profileSetting);
        List<Track> tracks = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder;
            //FIXME xpath evalutaion is really slow: optimize
            Document docForXpath = null;
            builder = factory.newDocumentBuilder();
            docForXpath = builder.parse(filename);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expr = xpath.compile("//TrainingCenterDatabase");
            Object result = expr.evaluate(docForXpath, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            if(nodes==null) {
                log.error("file is not TCX:"+filename);
            }

            long time1 = System.currentTimeMillis();
            tracks = doc.extractTrack(filename);
            long time2 = System.currentTimeMillis();
            System.out.println("Extract info from Track Duration ["+filename+"]:" + (time2 - time1));

            //TODO manage a list of tracks: a single file can contain multiple tracks
            if (tracks != null && !tracks.isEmpty()) {
                track = tracks.get(0);
            }
        }
        catch (SAXParseException sax) {
            log.error("file is not TCX ["+sax.getMessage()+"]:"+filename);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            //TODO some tracks are not loaded, explore exception cause
            log.error("can't load [" + ex.getMessage() + "]:" + filename);
            sb.append("can't load:"+filename);
        }



        //TODO manage a list of tracks: a single file can contain multiple tracks
        if (tracks != null && !tracks.isEmpty()) {
            track = tracks.get(0);
        }
        return new AbstractMap.SimpleImmutableEntry<Track, StringBuffer>(track, sb);
    }

    public static Map.Entry<Track,StringBuffer> extractTrackFromKml(String filename, ProfileSetting profileSetting) {
        //TODO KML implementation
        return null;
    }
}
