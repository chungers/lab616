/**
 * Original author Eugene Kononov <nonlinear5@yahoo.com> 
 * Adapted for JST by Florent Guiliani <florent@guiliani.fr>
 */
package com.jsystemtrader.platform.preferences;

import com.jsystemtrader.platform.model.JSystemTraderException;
import static com.jsystemtrader.platform.preferences.JSTPreferences.*;
import com.jsystemtrader.platform.startup.JSystemTrader;
import com.jsystemtrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class PreferencesDialog extends JDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 22);
    private JCheckBox emailSMTPSAuthCheck, emailSMTPSQuitWaitCheck, advisorAccountCheck,
                      backtestShowNumberOfBarsCheck, portfolioSyncCheck;
    private JTextField exchangesText, currenciesText, hostText, portText,
                       advisorAccountNumberText, emailTransportProtocolText, emailHostText,
                       emailUserText, emailRecipientText, emailSubjectText;
    private JSpinner  optimizerSpin, timeLagSpin, clientIdSpin;
    private JComboBox reportRendererCombo, reportRecyclingCombo;
    private JPasswordField emailPasswordField;
    
    private final PreferencesHolder prefs;

    public PreferencesDialog(JFrame parent) throws JSystemTraderException {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void add(JPanel panel, JSTPreferences pref, JTextField textField) {
        textField.setText(prefs.get(pref));
        commonAdd(panel, pref, textField);
    }

    private void add(JPanel panel, JSTPreferences pref, JComboBox comboBox) {
        comboBox.setSelectedItem(prefs.get(pref));
        commonAdd(panel, pref, comboBox);
    }

    private void add(JPanel panel, JSTPreferences pref, JSpinner spinner) {
        spinner.setValue(prefs.getInt(pref));
        commonAdd(panel, pref, spinner);
    }

    private void add(JPanel panel, JSTPreferences pref, JCheckBox checkBox) {
        checkBox.setSelected(prefs.getBool(pref));
        commonAdd(panel, pref, checkBox);
    }
    
    private void commonAdd(JPanel panel, JSTPreferences pref, Component component) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":", JLabel.TRAILING);
        fieldNameLabel.setLabelFor(component);
        component.setPreferredSize(FIELD_DIMENSION);
        component.setMaximumSize(FIELD_DIMENSION);
        panel.add(fieldNameLabel);
        panel.add(component);
    }

    private void init() throws JSystemTraderException {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Preferences");

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel  buttonsPanel = new JPanel();
        JButton okButton     = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        okButton.setMnemonic('O');
        cancelButton.setMnemonic('C');
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        
        JPanel  noticePanel = new JPanel();        
        JLabel  noticeLabel = new JLabel("Some of the preferences will not take effect until " + JSystemTrader.APP_NAME + " is restarted.");
        noticeLabel.setForeground(Color.red);
        noticePanel.add(noticeLabel);       

        getContentPane().add(noticePanel , BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);
        
        // TWS Connection
        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("TWS Connection", connectionTab);
        hostText                 = new JTextField();
        portText                 = new JTextField();
        clientIdSpin             = new JSpinner();
        advisorAccountCheck      = new JCheckBox();
        advisorAccountNumberText = new JTextField();
        portfolioSyncCheck       = new JCheckBox();
        timeLagSpin              = new JSpinner();
        add(connectionTab, Host                , hostText);
        add(connectionTab, Port                , portText);
        add(connectionTab, ClientID            , clientIdSpin);
        add(connectionTab, AdvisorAccount      , advisorAccountCheck);
        add(connectionTab, AdvisorAccountNumber, advisorAccountNumberText);
        add(connectionTab, PortfolioSync       , portfolioSyncCheck );
        add(connectionTab, TimeLagAllowed      , timeLagSpin);
        SpringUtilities.makeCompactGrid(connectionTab, 7, 2, 12, 12, 8, 5);
        setWidth(connectionTab,clientIdSpin,45);
        setWidth(connectionTab,timeLagSpin ,45);
        
        // Reporting
        JPanel reportingTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Reporting", reportingTab);
        reportRendererCombo = new JComboBox();
        try {
        	ClassLoader cl = getClass().getClassLoader();
            ClassFinder classFinder = new ClassFinder();
            List<Class<?>> reportrenderers;

            if (cl.getClass().getSimpleName().equals("JNLPClassLoader")) {
            	ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
            	classList.add( cl.loadClass("com.jsystemtrader.platform.report.HTMLReportRenderer") );
            	classList.add( cl.loadClass("com.jsystemtrader.platform.report.CSVReportRenderer")  );
            	classList.add( cl.loadClass("com.jsystemtrader.platform.report.TextReportRenderer") );
            	reportrenderers = classList;            	
            }
            else {            
            	reportrenderers = classFinder.getInterfaces("com.jsystemtrader.platform.report", "com.jsystemtrader.platform.report.ReportRenderer");
            }
            for (Class<?> reportrendererClass : reportrenderers) {
            	reportRendererCombo.addItem(reportrendererClass.getName());
            }
        } catch (Exception e) {
            throw new JSystemTraderException("Could not populate reportrenderers: " + e.getMessage());
        }        
        reportRecyclingCombo = new JComboBox(new String[]{"Append", "Override"});
        add(reportingTab, ReportRenderer , reportRendererCombo);
        add(reportingTab, ReportRecycling, reportRecyclingCombo);
        SpringUtilities.makeCompactGrid(reportingTab, 2, 2, 12, 12, 8, 5);
        setWidth(reportingTab,reportRecyclingCombo,85);
        
        
        // BackTest & Optimizer
        JPanel backtestTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Backtests", backtestTab);
        backtestShowNumberOfBarsCheck = new JCheckBox();
        optimizerSpin                 = new JSpinner();
        add(backtestTab, BacktestShowNumberOfBar, backtestShowNumberOfBarsCheck);
        add(backtestTab, OptimizerMaxThread     , optimizerSpin);
        SpringUtilities.makeCompactGrid(backtestTab, 2, 2, 12, 12, 8, 5);
        setWidth(backtestTab,optimizerSpin,45);
        

        // IB Back data downloader properties
        JPanel ibBackdataTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("IB Backdata", ibBackdataTab);
        exchangesText  = new JTextField();
        currenciesText = new JTextField();
        add(ibBackdataTab, Exchanges , exchangesText);
        add(ibBackdataTab, Currencies, currenciesText);        
        SpringUtilities.makeCompactGrid(ibBackdataTab, 2, 2, 12, 12, 8, 5);

        
        // Emails
        JPanel emailTab = new JPanel(new SpringLayout());
        tabbedPane1.addTab("Emails", emailTab);
        emailTransportProtocolText = new JTextField();
        emailSMTPSAuthCheck        = new JCheckBox();
        emailSMTPSQuitWaitCheck    = new JCheckBox();
        emailHostText              = new JTextField();
        emailUserText              = new JTextField();
        emailPasswordField         = new JPasswordField();
        emailSubjectText           = new JTextField();
        emailRecipientText         = new JTextField();
        add(emailTab, MailTransportProtocol, emailTransportProtocolText);
        add(emailTab, MailSMTPSAuth        , emailSMTPSAuthCheck);
        add(emailTab, MailSMTPSQuitWair    , emailSMTPSQuitWaitCheck);
        add(emailTab, MailHost             , emailHostText);
        add(emailTab, MailUser             , emailUserText);
        add(emailTab, MailPassword         , emailPasswordField);
        add(emailTab, MailSubject          , emailSubjectText);
        add(emailTab, MailRecipient        , emailRecipientText);
        SpringUtilities.makeCompactGrid(emailTab, 8, 2, 12, 12, 8, 5);
        
        
        // Set values...
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // TWS Connection
                    prefs.set(Host                , hostText.getText());
                    prefs.set(Port                , portText.getText());
                    prefs.set(ClientID            , String.valueOf(clientIdSpin.getValue()));
                    prefs.set(AdvisorAccount      , advisorAccountCheck.isSelected());
                    prefs.set(AdvisorAccountNumber, advisorAccountNumberText.getText());
                    prefs.set(PortfolioSync       , portfolioSyncCheck.isSelected());
                    prefs.set(TimeLagAllowed      , String.valueOf(timeLagSpin.getValue()));

                    // Reporting
                    prefs.set(ReportRenderer  , (String) reportRendererCombo.getSelectedItem());
                    prefs.set(ReportRecycling , (String) reportRecyclingCombo.getSelectedItem());
                    
                    // Backtest & Optimizer
                    prefs.set(BacktestShowNumberOfBar, backtestShowNumberOfBarsCheck.isSelected());
                    prefs.set(OptimizerMaxThread     , String.valueOf(optimizerSpin.getValue()));

                	// IB back data
                	prefs.set(Exchanges , exchangesText.getText());
                	prefs.set(Currencies, currenciesText.getText());

                	// Emails
                	prefs.set(MailTransportProtocol, emailTransportProtocolText.getText());
                	prefs.set(MailSMTPSAuth        , emailSMTPSAuthCheck.isSelected());
                	prefs.set(MailSMTPSQuitWair    , emailSMTPSQuitWaitCheck.isSelected());
                	prefs.set(MailHost             , emailHostText.getText());
                	prefs.set(MailUser             , emailUserText.getText());
                	prefs.set(MailPassword         , new String(emailPasswordField.getPassword()));
                	prefs.set(MailSubject          , emailSubjectText.getText());
                	prefs.set(MailRecipient        , emailRecipientText.getText());
                    
                    dispose();
                } catch (Exception ex) {
                    MessageDialog.showError(PreferencesDialog.this, ex.getMessage());
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        setPreferredSize(new Dimension(500, 350));

    }
    
    private void setWidth(JPanel p, Component c, int width) throws JSystemTraderException
    {
        SpringLayout layout;
        try {
            layout = (SpringLayout)p.getLayout();
            SpringLayout.Constraints spinLayoutConstraint = layout.getConstraints(c);
            spinLayoutConstraint.setWidth(Spring.constant(width));
        } catch (ClassCastException exc) {
            throw new JSystemTraderException("The first argument to makeGrid must use SpringLayout.");
        }           
    }
}
