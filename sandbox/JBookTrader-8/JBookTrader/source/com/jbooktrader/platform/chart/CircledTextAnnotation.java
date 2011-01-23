package com.jbooktrader.platform.chart;

import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.text.*;
import org.jfree.ui.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Defines the shape of the markers which show strategy positions on the
 * performance chart. In this implementation, the shape of a marker is a
 * solid circle with a number inside, designating the strategy position
 * at a point in time.
 */
public class CircledTextAnnotation extends XYTextAnnotation {
    private static final Font ANNOTATION_FONT = new Font("SansSerif", Font.BOLD, 11);
    private static final int ANNOTATION_RADIUS = 6;
    private final Stroke circleStroke = new BasicStroke(1);
    private final Paint circleColor = new Color(250, 240, 150);
    private Color color;

    public CircledTextAnnotation(int quantity, double x, double y) {
        super(String.valueOf(Math.abs(quantity)), x, y);

        if (quantity > 0) {
            color = Color.GREEN;
        } else if (quantity < 0) {
            color = Color.RED;
        } else {
            color = Color.YELLOW;
        }

        setFont(ANNOTATION_FONT);
        setPaint(Color.BLACK);
        setTextAnchor(TextAnchor.CENTER);
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

        double x = anchorX - ANNOTATION_RADIUS;
        double y = anchorY - ANNOTATION_RADIUS;
        double width = ANNOTATION_RADIUS * 2.0;
        double height = ANNOTATION_RADIUS * 2.0;

        g2.setColor(color);
        g2.fill(new Ellipse2D.Double(x, y, width, height));

        g2.setPaint(circleColor);
        g2.setStroke(circleStroke);
        g2.draw(new Ellipse2D.Double(x, y, width, height));

        g2.setFont(getFont());
        g2.setPaint(getPaint());
        long quantity = Long.valueOf(getText());
        // todo: stock quantity such as 100 do not fit
        if (quantity >= 25000) {
            quantity /= 25000;
        }
        TextUtilities.drawRotatedString(String.valueOf(quantity), g2, anchorX, anchorY, getTextAnchor(),
                getRotationAngle(), getRotationAnchor());
    }
}
