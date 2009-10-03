package com.jsystemtrader.platform.report;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.preferences.JSTPreferences;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import com.jsystemtrader.platform.startup.*;

import java.io.*;
import java.text.*;
import java.util.*;

public final class Report {
    private final String fieldStart;
    private final String fieldEnd;
    private final String rowStart;
    private final String rowEnd;
    private final String fieldBreak;
    private final ReportRenderer renderer;
    private final PreferencesHolder preferences;

    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd/yy z");
    private PrintWriter writer;
    private static boolean isDisabled;
    private final static String FILE_SEP = System.getProperty("file.separator");
    private final static String REPORT_DIR = JSystemTrader.getAppPath() + FILE_SEP + "reports" + FILE_SEP;

    public Report(String fileName) throws IOException, JSystemTraderException {
        preferences = PreferencesHolder.getInstance();
        String reportRendererClass = preferences.get(JSTPreferences.ReportRenderer);

        try {
            Class<? extends ReportRenderer> clazz = Class.forName(reportRendererClass).asSubclass(ReportRenderer.class);
            renderer = clazz.newInstance();
        } catch (Exception e) {
            throw new JSystemTraderException(e);
        }

        fieldStart = renderer.getFieldStart();
        fieldEnd = renderer.getFieldEnd();
        rowStart = renderer.getRowStart();
        rowEnd = renderer.getRowEnd();
        fieldBreak = renderer.getFieldBreak();
        String emphasisStart = renderer.getEmphasisStart();
        String emphasisEnd = renderer.getEmphasisEnd();
        String rootStart = renderer.getRootStart();
        String fileExtension = renderer.getFileExtension();


        if (isDisabled) {
            return;
        }

        File reportDir = new File(REPORT_DIR);
        if (!reportDir.exists()) {
            reportDir.mkdir();
        }

        String fullFileName = REPORT_DIR + fileName + "." + fileExtension;
        writer = new PrintWriter(new BufferedWriter(new FileWriter(fullFileName, preferences.get(JSTPreferences.ReportRecycling).equals("Append"))));
        StringBuilder s = new StringBuilder();
        s.append(emphasisStart).append("New Report Started: ").append(df.format(getDate())).append(emphasisEnd);
        s.append(rootStart);
        reportDescription(s.toString());
    }

    public ReportRenderer getRenderer() {
        return renderer;
    }

    public static void disable() {
        isDisabled = true;
    }

    public static void enable() {
        isDisabled = false;
    }

    public void report(StringBuilder message) {
        StringBuilder s = new StringBuilder();
        s.append(rowStart);
        s.append(fieldStart).append(df.format(getDate())).append(fieldEnd);
        s.append(fieldStart).append(message).append(fieldEnd);
        s.append(rowEnd);
        write(s);
    }


    public void report(String message) {
        report(new StringBuilder(message));
    }

    public void report(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        boolean saved = isDisabled;
        isDisabled = false; //always report exceptions
        report(sw.toString());
        isDisabled = saved;
    }

    public void reportDescription(String message) {
        StringBuilder s = new StringBuilder();
        s.append(message).append(fieldBreak);
        write(s);
    }

    public void report(List<?> columns) {
        StringBuilder s = new StringBuilder();
        s.append(rowStart);
        for (Object column : columns) {
            s.append(fieldStart).append(column).append(fieldEnd);
        }
        s.append(rowEnd);
        write(s);
    }

    public void report(List<?> columns, Calendar strategyCalendar) {
        StringBuilder s = new StringBuilder();
        s.append(rowStart);

        s.append(fieldStart);
        df.setTimeZone(strategyCalendar.getTimeZone());
        s.append(df.format(strategyCalendar.getTime()));
        s.append(fieldEnd);

        for (Object column : columns) {
            s.append(fieldStart).append(column).append(fieldEnd);
        }

        s.append(rowEnd);
        write(s);
    }

    private Date getDate() {
        return Calendar.getInstance(TimeZone.getDefault()).getTime();
    }

    private synchronized void write(StringBuilder s) {
        if (!isDisabled) {
            writer.println(s);
            writer.flush();
        }
    }


}
