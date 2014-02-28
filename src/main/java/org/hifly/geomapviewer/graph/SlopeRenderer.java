package org.hifly.geomapviewer.graph;

import org.jfree.chart.renderer.xy.XYAreaRenderer2;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author
 * @date 27/02/14
 */
public class SlopeRenderer extends XYAreaRenderer2 {

    private XYSeriesCollection dataset;
    private ArrayList<Color> mapSlopeColor = new ArrayList<Color>();
    {
        mapSlopeColor.add(new Color(0, 0, 127));
        mapSlopeColor.add(new Color(0, 1, 178));
        mapSlopeColor.add(new Color(0, 77, 233));
        mapSlopeColor.add(new Color(0, 137, 252));
        mapSlopeColor.add(new Color(0, 178, 254));
        mapSlopeColor.add(new Color(0, 233, 232));
        mapSlopeColor.add(new Color(0, 204, 125));
        mapSlopeColor.add(new Color(152, 230, 0));
        mapSlopeColor.add(new Color(254, 252, 0));
        mapSlopeColor.add(new Color(254, 202, 0));
        mapSlopeColor.add(new Color(254, 152, 0));
        mapSlopeColor.add(new Color(254, 0, 0));
        mapSlopeColor.add(new Color(151, 0, 0));
        mapSlopeColor.add(new Color(77, 0, 0));
    }



    public SlopeRenderer(XYSeriesCollection dataset) {
       this.dataset = dataset;
    }


    public java.awt.Paint getItemPaint(int series, int item) {
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1)) {
            y1 = 0.0;
        }

        int itemCount = dataset.getItemCount(series);
        double x2 = dataset.getXValue(series, Math.min(
                item + 10, itemCount - 1));
        double y2 = dataset.getYValue(series, Math.min(
                item + 10, itemCount - 1));
        if (Double.isNaN(y2)) {
            y2 = 0.0;
        }

        Color result = mapSlopeColor.get(0);
        double deltaHeight = y2 - y1;
        double deltaDistance = (x2 - x1) * 1000;
        if (deltaHeight > 0) {
            int colorIndex = (int) (100 * (deltaHeight / deltaDistance));
            int index = Math.min(colorIndex, mapSlopeColor.size() - 1);
            result = mapSlopeColor.get(index);
        }

        return result;

    }
}
