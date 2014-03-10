package org.hifly.geomapviewer.gui.events;

import org.hifly.geomapviewer.domain.gps.SlopeSegment;
import org.hifly.geomapviewer.domain.gps.Waypoint;
import org.hifly.geomapviewer.graph.WaypointElevationGraph;
import org.hifly.geomapviewer.graph.WaypointGraph;
import org.hifly.geomapviewer.gui.dialog.GraphViewer;
import org.hifly.geomapviewer.storage.ClimbStorage;

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
public class LinkAdapter extends MouseAdapter implements MouseMotionListener {

    private JFrame currentFrame;
    protected List<SlopeSegment> slopes;
    protected Map<Integer,List<SlopeSegment>> mapSlopes;

    public LinkAdapter(JFrame currentFrame) {
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
                        SlopeSegment slope = null;
                        //get list of waypoints
                        List<Waypoint> slopeWaypoints = null;
                        String indexSlope = href.substring(href.lastIndexOf("=")+1);
                        if(mapSlopes!=null) {
                            int index = href.lastIndexOf("trackIndex=");
                            String trackIndex = href.substring(index+11,index+12);
                            slope =  mapSlopes.get(Integer.valueOf(trackIndex)).get(Integer.valueOf(indexSlope));
                            slopeWaypoints =
                                    slope.getWaypoints();
                        }
                        else {
                            slope = slopes.get(Integer.valueOf(indexSlope));
                            //get list of waypoints
                            slopeWaypoints = slope.getWaypoints();
                        }

                        if(href.contains("save")) {
                            String climbName = (String)JOptionPane.showInputDialog(
                                    currentFrame,
                                    "Name for the climb",
                                    "Save Climb",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    null,
                                    "");
                            //TODO verify climbname
                            slope.setName(climbName);
                            ClimbStorage.saveClimb(slope,climbName);
                        }
                        else {
                            //open graph
                            WaypointGraph waypointElevationGraph =
                                new WaypointElevationGraph(slopeWaypoints,true,false);
                            GraphViewer graphViewer =
                                new GraphViewer(currentFrame,
                                        Arrays.asList(waypointElevationGraph));
                        }
                    } catch (Exception ev) {
                        System.err.println(ev.getMessage());
                    }
                }
            }
        }
    }

    public List<SlopeSegment> getSlopes() {
        return slopes;
    }

    public void setSlopes(List<SlopeSegment> slopes) {
        this.slopes = slopes;
    }

    public Map<Integer, List<SlopeSegment>> getMapSlopes() {
        return mapSlopes;
    }

    public void setMapSlopes(Map<Integer, List<SlopeSegment>> mapSlopes) {
        this.mapSlopes = mapSlopes;
    }
}
