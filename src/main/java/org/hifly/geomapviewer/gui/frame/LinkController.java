package org.hifly.geomapviewer.gui.frame;

import org.hifly.geomapviewer.domain.gps.Waypoint;
import org.hifly.geomapviewer.graph.WaypointElevationGraph;
import org.hifly.geomapviewer.graph.WaypointGraph;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 25/02/14
 */
public class LinkController extends MouseAdapter implements MouseMotionListener {

    private JFrame currentFrame;
    protected List<List<Waypoint>> waypoints;
    protected Map<Integer,List<List<Waypoint>>> trackWaypoints;

    public LinkController(JFrame currentFrame) {
        super();
        this.currentFrame = currentFrame;
    }


    public void mouseClicked(MouseEvent e) {
        JTextPane editor = (JTextPane) e.getSource();
        Document doc = editor.getDocument();
        Point pt = new Point(e.getX(), e.getY());
        int pos = editor.viewToModel(pt);
        if (pos >= 0) {
            if (doc instanceof DefaultStyledDocument) {
                DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
                Element el = hdoc.getCharacterElement(pos);
                AttributeSet a = el.getAttributes();
                String href = (String) a.getAttribute(HTML.Attribute.HREF);
                if (href != null) {
                    try {
                        //get list of waypoints
                        List<Waypoint> slopeWaypoints = null;
                        String indexWaypoint = href.substring(href.lastIndexOf("=")+1);
                        if(trackWaypoints!=null) {
                            int index = href.lastIndexOf("trackIndex=");
                            String trackIndex = href.substring(index+11,index+12);
                            slopeWaypoints = trackWaypoints.get(Integer.valueOf(trackIndex)).get(Integer.valueOf(indexWaypoint));
                        }
                        else {
                            //get list of waypoints
                            slopeWaypoints = waypoints.get(Integer.valueOf(indexWaypoint));
                        }
                        //open graph
                        WaypointGraph waypointElevationGraph =
                                new WaypointElevationGraph(slopeWaypoints,true);
                        GraphViewer graphViewer =
                                new GraphViewer(currentFrame,
                                        Arrays.asList(waypointElevationGraph));
                    } catch (Exception ev) {
                        System.err.println(ev.getMessage());
                    }
                }
            }
        }
    }

    public List<List<Waypoint>> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<List<Waypoint>> waypoints) {
        this.waypoints = waypoints;
    }

    public Map<Integer, List<List<Waypoint>>> getTrackWaypoints() {
        return trackWaypoints;
    }

    public void setTrackWaypoints(Map<Integer, List<List<Waypoint>>> trackWaypoints) {
        this.trackWaypoints = trackWaypoints;
    }
}
