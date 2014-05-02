package org.hifly.geomapviewer.controller;

import org.apache.xmlbeans.XmlException;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.gps.GPSDocument;
import org.hifly.geomapviewer.gps.GPX10Document;
import org.hifly.geomapviewer.gps.GPXDocument;
import org.hifly.geomapviewer.gps.TCX2Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

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
            Document docForXpath = null;
            String gpxVersion = null;
            builder = factory.newDocumentBuilder();
            docForXpath = builder.parse(filename);
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

            tracks = doc.extractTrack(filename);


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

            tracks = doc.extractTrack(filename);
        }
        catch (SAXParseException sax) {
            log.error("file is not TCX ["+sax.getMessage()+"]:"+filename);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            sb.append("can't load:"+filename);
        }



        //TODO manage a list of tracks: a single file can contain multiple tracks
        if (tracks != null && !tracks.isEmpty()) {
            track = tracks.get(0);
        }
        return new AbstractMap.SimpleImmutableEntry<Track, StringBuffer>(track, sb);
    }

    public static Map.Entry<Track,StringBuffer> extractTrackFromKml(String filename, ProfileSetting profileSetting) {
        //TODO implementation
        return null;
    }
}
