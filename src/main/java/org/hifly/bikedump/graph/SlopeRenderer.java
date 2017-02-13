package org.hifly.bikedump.graph;

import org.jfree.chart.renderer.xy.XYAreaRenderer2;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.ArrayList;

public class SlopeRenderer extends XYAreaRenderer2 {

    private boolean singleColor = false;

    public SlopeRenderer(XYSeriesCollection dataset,boolean singleColor) {
        this.dataset = dataset;
        this.singleColor = singleColor;
    }

    private XYSeriesCollection dataset;
    private ArrayList<Color> mapSlopeColor = new ArrayList<Color>();
    {
        mapSlopeColor.add(new Color(152, 230, 0));
        mapSlopeColor.add(new Color(0, 77, 233));
        mapSlopeColor.add(new Color(254, 252, 0));
        mapSlopeColor.add(new Color(254, 0, 0));
        mapSlopeColor.add(new Color(77, 0, 0));
    }

    private ArrayList<Color> mapSlopeSingleColor = new ArrayList<Color>();
    {
        mapSlopeSingleColor.add(new Color(254, 0, 0));
    }

    public java.awt.Paint getItemPaint(int series, int item) {
        if(singleColor)
            return mapSlopeSingleColor.get(0);

        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1))
            y1 = 0.0;

        int itemCount = dataset.getItemCount(series);
        double x2 = dataset.getXValue(series, Math.min(
                item + 10, itemCount - 1));
        double y2 = dataset.getYValue(series, Math.min(
                item + 10, itemCount - 1));
        if (Double.isNaN(y2))
            y2 = 0.0;

        Color result = mapSlopeColor.get(0);
        double deltaHeight = y2 - y1;
        double deltaDistance = (x2 - x1) * 1000;
        if (deltaHeight > 0) {
            int colorIndex = (int) (100 * (deltaHeight / deltaDistance));
            if(colorIndex <= 4)
                result = mapSlopeColor.get(0);
            else if(colorIndex > 4 && colorIndex <= 8)
                result = mapSlopeColor.get(1);
            else if(colorIndex > 8 && colorIndex <= 10)
                result = mapSlopeColor.get(2);
            else if(colorIndex > 10 && colorIndex <= 15)
                result = mapSlopeColor.get(3);
            else
                result = mapSlopeColor.get(4);
        }

        return result;

    }
}
