package org.hifly.geomapviewer.gui.marker;

import org.openstreetmap.gui.jmapviewer.MapObjectImpl;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

import java.awt.*;

import java.util.List;

/**
 * @author
 * @date 04/02/14
 */
public class LineMarker extends MapPolygonImpl {

    protected Color color;

    public LineMarker(List<? extends ICoordinate> points, Color color) {
        super(points);
        this.color = color;
    }

    @Override
    public void paint(Graphics g, List<Point> points) {
        Graphics2D g2d = (Graphics2D) g.create();
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            if (i + 1 < points.size()) {
                Point p2 = points.get(i + 1);
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
                i++;
            }
        }

    }

    @Override
    public void paint(Graphics g, Polygon polygon) {
        // Prepare graphics
        Color oldColor = g.getColor();
        g.setColor(getColor());

        Stroke oldStroke = null;
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            oldStroke = g2.getStroke();
            g2.setStroke(getStroke());
        }
        // Draw
        g.drawPolygon(polygon);
        if (g instanceof Graphics2D && getBackColor() != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g2.fillPolygon(polygon);
            g2.setComposite(oldComposite);
        }
        // Restore graphics
        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
        Rectangle rec = polygon.getBounds();
        Point corner = rec.getLocation();
        Point p = new Point(corner.x + (rec.width / 2), corner.y + (rec.height / 2));
        if (getLayer() == null || getLayer().isVisibleTexts()) paintText(g, p);
    }
}
