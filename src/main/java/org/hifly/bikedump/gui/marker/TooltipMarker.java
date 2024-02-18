package org.hifly.bikedump.gui.marker;

import org.hifly.bikedump.domain.gps.WaypointSegment;
import org.hifly.bikedump.gui.panel.MapViewer;
import org.hifly.bikedump.utility.GPSUtility;
import org.hifly.bikedump.utility.TimeUtility;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import java.awt.*;

public class TooltipMarker extends MapMarkerDot  {
    protected String text,text2,text3,text4,text5=null;
    protected WaypointSegment waypoint;
    protected MapViewer mapViewer;
    private double x,y;

    public TooltipMarker(double lat, double lon, double x, double y,WaypointSegment waypoint, MapViewer viewer) {
        super(lat,lon);

        text =  "Lap: "+waypoint.getUnit();
        text2 = "timestamp: " + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", waypoint.getTimeSpent());
        text3 = "time spent: " + TimeUtility.toStringFromTimeDiff(waypoint.getTimeIncrement());
        text4 = "avg speed: " + GPSUtility.roundDoubleStat(waypoint.getAvgSpeed()) + " km/h";
        text5 = "elevation gained: " + GPSUtility.roundDoubleStat(waypoint.getEleGained())+ " m";

        this.waypoint = waypoint;
        this.mapViewer = viewer;
        this.x = x;
        this.y = y;
    }

    @Override
    public void paint(Graphics g, Point position, int radio) {
        if (g instanceof Graphics2D) {

            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(new Color(220, 220, 220));
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            rh.put(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHints(rh);
            g2d.fillRect((int) x, (int) y, 280, 80);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.setColor(Color.BLUE);
            g.drawString(text,(int)x+5,(int)y+15);
            g.drawString(text2,(int)x+5,(int)y+30);
            g.drawString(text3,(int)x+5,(int)y+45);
            g.drawString(text4,(int)x+5,(int)y+60);
            g.drawString(text5,(int)x+5,(int)y+75);

        }
    }


}
