package org.hifly.geomapviewer.gui.marker;

import org.hifly.geomapviewer.domain.gps.WaypointSegment;
import org.hifly.geomapviewer.gui.panel.MapViewer;
import org.hifly.geomapviewer.utility.GpsUtility;
import org.hifly.geomapviewer.utility.TimeUtility;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author
 * @date 03/02/14
 */
public class TooltipMarker extends MapMarkerDot  {
    protected String text=null;
    protected WaypointSegment waypoint;
    protected MapViewer mapViewer;
    private double x,y;

    public TooltipMarker(double lat, double lon, double x, double y,WaypointSegment waypoint, MapViewer viewer) {
        super(lat,lon);

        text += "Time relevation:" + TimeUtility.convertToString("dd/MM/yyyy HH:mm:ss", waypoint.getTimeSpent()) + "\n";
        //text += "Time lap:" + TimeUtility.toStringFromTimeDiff(waypoint.getTimeIncrement()) + "\n";
        //text += "Avg speed:" + GpsUtility.roundDoubleStat(waypoint.getAvgSpeed()) + " km/h\n";
        //text += "Elevation gained:"+GpsUtility.roundDoubleStat(waypoint.getEleGained());

        this.waypoint = waypoint;
        this.mapViewer = viewer;
        this.x = x;
        this.y = y;
    }

    public TooltipMarker(Color color, double lat, double lon) {
        super(color,lat,lon);
    }

    @Override
    public void paint(Graphics g, Point position, int radio) {
        if (g instanceof Graphics2D) {
            System.out.println("draw rectangle for marker:" + waypoint.getKm());
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.draw(new Rectangle2D.Double(x, y, 80, 80));

            String text = this.text;

            // Draw centered text
            FontMetrics fm = g.getFontMetrics();
            g.setFont(new Font("TimesRoman", Font.PLAIN, 8));
            double textWidth = fm.getStringBounds(text, g).getWidth();
            g.setColor(Color.BLACK);
            int positionX =  (int) (position.x - textWidth/2);
            int positionY =   (position.y + fm.getMaxAscent() / 2);

            int startX = (int)(x + ( ( 80 - textWidth ) / 2 ));
            int startY = (int)(y + ( ( 80 + fm.getHeight() ) / 2 ));


            //g.drawString(text,positionX,positionY);
            g.drawString(text,startX,startY);
            g2.setComposite(oldComposite);

        }


    }


}
