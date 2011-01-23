package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.optimizer.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


/**
 * Contour plot of optimization results
 */
public class OptimizationMap {
    private static final Dimension MIN_SIZE = new Dimension(720, 550);// minimum frame size
    private final PreferencesHolder prefs;
    private final PerformanceMetric sortPerformanceMetric;
    private final Strategy strategy;
    private final JDialog parent;
    private final List<OptimizationResult> optimizationResults;
    private JFreeChart chart;
    private JComboBox horizontalCombo, verticalCombo, caseCombo, colorMapCombo;
    private double min, max;
    private ChartPanel chartPanel;


    public OptimizationMap(JDialog parent, Strategy strategy, List<OptimizationResult> optimizationResults,
                           PerformanceMetric sortPerformanceMetric) {
        prefs = PreferencesHolder.getInstance();
        this.parent = parent;
        this.strategy = strategy;
        this.optimizationResults = optimizationResults;
        this.sortPerformanceMetric = sortPerformanceMetric;
        chart = createChart();
    }

    public JDialog getChartFrame() {
        final JDialog chartFrame = new JDialog(parent);
        chartFrame.setTitle("Optimization Map - " + strategy);
        chartFrame.setModal(true);

        JPanel northPanel = new JPanel(new SpringLayout());
        JPanel centerPanel = new JPanel(new SpringLayout());
        JPanel chartOptionsPanel = new JPanel(new SpringLayout());

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder border = BorderFactory.createTitledBorder(etchedBorder, "Optimization Map Options");
        border.setTitlePosition(TitledBorder.TOP);
        chartOptionsPanel.setBorder(border);

        JLabel horizontalLabel = new JLabel("Horizontal:", SwingConstants.TRAILING);
        horizontalCombo = new JComboBox();
        horizontalLabel.setLabelFor(horizontalCombo);

        JLabel verticalLabel = new JLabel("Vertical:", SwingConstants.TRAILING);
        verticalCombo = new JComboBox();
        verticalLabel.setLabelFor(verticalCombo);

        StrategyParams params = optimizationResults.get(0).getParams();
        for (StrategyParam param : params.getAll()) {
            horizontalCombo.addItem(param.getName());
            verticalCombo.addItem(param.getName());
        }

        horizontalCombo.setSelectedIndex(0);
        verticalCombo.setSelectedIndex(1);

        JLabel caseLabel = new JLabel("Case:", SwingConstants.TRAILING);
        caseCombo = new JComboBox(new String[] {"Best", "Worst"});
        caseCombo.setSelectedIndex(0);
        caseLabel.setLabelFor(caseCombo);


        JLabel colorMapLabel = new JLabel("Color map:", SwingConstants.TRAILING);
        colorMapCombo = new JComboBox(new String[] {"Heat", "Gray"});
        colorMapLabel.setLabelFor(colorMapCombo);

        chartOptionsPanel.add(horizontalLabel);
        chartOptionsPanel.add(horizontalCombo);
        chartOptionsPanel.add(verticalLabel);
        chartOptionsPanel.add(verticalCombo);
        chartOptionsPanel.add(caseLabel);
        chartOptionsPanel.add(caseCombo);
        chartOptionsPanel.add(colorMapLabel);
        chartOptionsPanel.add(colorMapCombo);

        SpringUtilities.makeOneLineGrid(chartOptionsPanel);
        northPanel.add(chartOptionsPanel);
        SpringUtilities.makeTopOneLineGrid(northPanel);

        chartPanel = new ChartPanel(chart);
        TitledBorder chartBorder = BorderFactory.createTitledBorder(etchedBorder, "Optimization Map");
        chartBorder.setTitlePosition(TitledBorder.TOP);
        chartPanel.setBorder(chartBorder);

        centerPanel.add(chartPanel);
        SpringUtilities.makeOneLineGrid(centerPanel);

        int chartWidth = prefs.getInt(OptimizationMapWidth);
        int chartHeight = prefs.getInt(OptimizationMapHeight);
        int chartX = prefs.getInt(OptimizationMapX);
        int chartY = prefs.getInt(OptimizationMapY);


        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                prefs.set(OptimizationMapWidth, chartFrame.getWidth());
                prefs.set(OptimizationMapHeight, chartFrame.getHeight());
                prefs.set(OptimizationMapX, chartFrame.getX());
                prefs.set(OptimizationMapY, chartFrame.getY());
                chartFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            }
        });

        horizontalCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });

        verticalCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });

        caseCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });

        colorMapCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });


        repaint();
        chartFrame.getContentPane().add(northPanel, BorderLayout.NORTH);
        chartFrame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        chartFrame.getContentPane().setMinimumSize(MIN_SIZE);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(null);
        if (chartX >= 0 && chartY >= 0 && chartHeight > 0 && chartWidth > 0) {
            chartFrame.setBounds(chartX, chartY, chartWidth, chartHeight);
        }


        return chartFrame;
    }

    private void repaint() {
        chart = createChart();
        chartPanel.setChart(chart);
    }


    private double getMetric(OptimizationResult optimizationResult) {
        return optimizationResult.get(sortPerformanceMetric);
    }


    private XYZDataset createOptimizationDataset() {
        double[] x, y, z;

        synchronized (optimizationResults) {
            int size = optimizationResults.size();
            x = new double[size];
            y = new double[size];
            z = new double[size];

            Map<String, Double> values = new HashMap<String, Double>();


            int xParameterIndex = (horizontalCombo == null) ? 0 : horizontalCombo.getSelectedIndex();
            int yParameterIndex = (verticalCombo == null) ? 1 : verticalCombo.getSelectedIndex();

            int index = 0;
            min = max = getMetric(optimizationResults.get(index));
            int selectedsCase = (caseCombo == null) ? 0 : caseCombo.getSelectedIndex();

            for (OptimizationResult optimizationResult : optimizationResults) {
                StrategyParams params = optimizationResult.getParams();

                x[index] = params.get(xParameterIndex).getValue();
                y[index] = params.get(yParameterIndex).getValue();
                z[index] = getMetric(optimizationResult);

                String key = x[index] + "," + y[index];
                Double value = values.get(key);


                if (value != null) {
                    if (selectedsCase == 0) {
                        z[index] = Math.max(value, z[index]);
                    } else if (selectedsCase == 1) {
                        z[index] = Math.min(value, z[index]);
                    }
                }

                values.put(key, z[index]);


                min = Math.min(min, z[index]);
                max = Math.max(max, z[index]);
                index++;
            }
        }

        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries("optimization", new double[][] {x, y, z});

        return dataset;
    }

    private class HeatPaintScale implements PaintScale {
        public Paint getPaint(double z) {
            double normalizedZ = (z - min) / (max - min);
            double brightness = 1;
            double saturation = Math.max(0.1, Math.abs(2 * normalizedZ - 1));
            double red = 0;
            double blue = 0.7;
            double hue = blue - normalizedZ * (blue - red);
            return Color.getHSBColor((float) hue, (float) saturation, (float) brightness);
        }

        public double getUpperBound() {
            return max;
        }

        public double getLowerBound() {
            return min;
        }
    }

    public class GrayPaintScale implements PaintScale {
        public Paint getPaint(double z) {
            double normalizedZ = z - min;
            double clrs = 255.0 / (max - min);
            int color = (int) (255 - normalizedZ * clrs);
            return new Color(color, color, color, 255);
        }

        public double getUpperBound() {
            return max;
        }

        public double getLowerBound() {
            return min;
        }
    }


    private JFreeChart createChart() {
        XYZDataset dataset = createOptimizationDataset();

        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRangeIncludesZero(false);

        xAxis.setLabel(horizontalCombo == null ? null : (String) horizontalCombo.getSelectedItem());
        yAxis.setLabel(verticalCombo == null ? null : (String) verticalCombo.getSelectedItem());


        XYBlockRenderer renderer = new XYBlockRenderer();
        int paintScaleIndex = (colorMapCombo == null) ? 0 : colorMapCombo.getSelectedIndex();
        PaintScale paintScale = (paintScaleIndex == 0) ? new HeatPaintScale() : new GrayPaintScale();
        renderer.setPaintScale(paintScale);


        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        chart = new JFreeChart(plot);

        chart.removeLegend();
        chart.getPlot().setOutlineStroke(new BasicStroke(1.0f));
        NumberAxis scaleAxis = new NumberAxis(sortPerformanceMetric.getName());
        scaleAxis.setRange(min, max);
        PaintScaleLegend legend = new PaintScaleLegend(paintScale, scaleAxis);
        legend.setFrame(new BlockBorder(Color.GRAY));
        legend.setPadding(new RectangleInsets(5, 5, 5, 5));
        legend.setMargin(new RectangleInsets(4, 6, 40, 6));
        legend.setPosition(RectangleEdge.RIGHT);
        chart.addSubtitle(legend);

        return chart;
    }
}
