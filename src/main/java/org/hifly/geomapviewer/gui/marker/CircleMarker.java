package org.hifly.geomapviewer.gui.marker;

import org.hifly.geomapviewer.domain.gps.WaypointKm;
import org.hifly.geomapviewer.gui.MapViewer;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @date 03/02/14
 */
//TODO find a method to link info to circle mouse click event
public class CircleMarker extends MapMarkerDot  {
    protected String text=null;
    protected WaypointKm waypoint;
    protected MapViewer mapViewer;

    public CircleMarker(double lat, double lon, WaypointKm waypoint,MapViewer viewer) {
        super(lat,lon);
        this.text = String.valueOf(waypoint.getKm());
        this.waypoint = waypoint;
        this.mapViewer = viewer;
    }

    public CircleMarker(Color color, double lat, double lon) {
        super(color,lat,lon);
    }

    @Override
    public void paint(Graphics g, Point position, int radio) {


        int size_h = radio;
        int size = size_h * 2;

        if (g instanceof Graphics2D && getBackColor()!=null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g.fillOval(position.x - size_h, position.y - size_h, size, size);

            String text = this.text;

            // Draw centered text
            FontMetrics fm = g.getFontMetrics();
            double textWidth = fm.getStringBounds(text, g).getWidth();
            g.setColor(Color.BLACK);
            int positionX =  (int) (position.x - textWidth/2);
            int positionY =   (position.y + fm.getMaxAscent() / 2);

            g.drawString(text,positionX,positionY);
            g2.setComposite(oldComposite);

            //add to map
            mapViewer.mapCircleCoordinates.put(String.valueOf(position.x) + "-" + String.valueOf(position.y), this.waypoint);

            //mapViewer.mapCircleCoordinates.put(String.valueOf(position.x) + "-" + String.valueOf(position.y), this.waypoint);


        }
        g.setColor(getColor());
        g.drawOval(position.x - size_h, position.y - size_h, size, size);

        if(getLayer()==null||getLayer().isVisibleTexts()) paintText(g, position);

    }


}
