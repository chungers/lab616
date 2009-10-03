package com.jsystemtrader.platform.chart;

import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.text.*;
import org.jfree.ui.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Defines the shape of the markers which show order executions on the
 * strategy performance chart. In this implementation, the shape of a marker
 * is a solid circle with a letter "L", "S", or "F" inside the circle,
 * designating the "long", "short", and "flat" positions resulting from
 * order executions.
 */
public class CircledTextAnnotation extends XYTextAnnotation {
    private final int radius;
    private Color color;
    private final Stroke circleStroke = new BasicStroke(1);
    private final Paint circleColor = new Color(250, 240, 150);

    public CircledTextAnnotation(String text, double x, double y, int radius) {
        super(text, x, y);
        this.radius = radius;
    }

    public void setBkColor(Color color) {
        this.color = color;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {

        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

        float anchorX = (float) domainAxis.valueToJava2D(getX(), dataArea, domainEdge);
        float anchorY = (float) rangeAxis.valueToJava2D(getY(), dataArea, rangeEdge);

        if (orientation.equals(PlotOrientation.HORIZONTAL)) {
            float tempAnchor = anchorX;
            anchorX = anchorY;
            anchorY = tempAnchor;
        }

        double x = anchorX - radius;
        double y = anchorY - radius;
        double width = radius * 2.0;
        double height = radius * 2.0;

        g2.setColor(color);
        g2.fill(new Ellipse2D.Double(x, y, width, height));

        g2.setPaint(circleColor);
        g2.setStroke(circleStroke);
        g2.draw(new Ellipse2D.Double(x, y, width, height));

        g2.setFont(getFont());
        g2.setPaint(getPaint());

        String quantity = getText();
        if(!quantity.equals("A"))
        {
            long intquantity = Long.valueOf(quantity);
            if (intquantity >= 25000) {
                intquantity /= 25000;
            }
            quantity = String.valueOf(intquantity);
        }
        TextUtilities.drawRotatedString(quantity, g2, anchorX, anchorY, getTextAnchor(), getRotationAngle(), getRotationAnchor());
    }
}
