package org.hifly.geomapviewer.gui.marker;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

import java.awt.*;

/**
 * @author
 * @date 03/02/14
 */
public class CircleMarker extends MapMarkerDot {
    protected String text=null;

    public CircleMarker(double lat, double lon, String text) {
        super(lat,lon);
        this.text = text;
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
            g.drawString(text, (int) (position.x - textWidth/2),
                    (int) (position.y + fm.getMaxAscent() / 2));


            g2.setComposite(oldComposite);
        }
        g.setColor(getColor());
        g.drawOval(position.x - size_h, position.y - size_h, size, size);



        if(getLayer()==null||getLayer().isVisibleTexts()) paintText(g, position);
    }
}
