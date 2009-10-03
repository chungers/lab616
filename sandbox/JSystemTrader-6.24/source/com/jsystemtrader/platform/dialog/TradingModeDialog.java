package com.jsystemtrader.platform.dialog;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import static com.jsystemtrader.platform.preferences.JSTPreferences.*;
import com.jsystemtrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class TradingModeDialog extends JDialog {
    private static final Dimension MIN_SIZE = new Dimension(700, 120);
    private JTextField fileNameText;
    private JComboBox reportCombo;
    private JLabel fileNameLabel, reportLabel;
    private JRadioButton backtestModeRadio, tradingModeRadio;
    private int action;
    private JButton cancelButton, okButton, selectFileButton;
    private final PreferencesHolder preferences;

    public TradingModeDialog(JFrame parent) throws JSystemTraderException {
        super(parent);
        preferences = PreferencesHolder.getInstance();
        init();
        setModal(true);
        pack();
        assignListeners();
        setLocationRelativeTo(null);
        action = JOptionPane.CANCEL_OPTION;
    }

    private void assignListeners() {
        ActionListener radioButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JRadioButton button = (JRadioButton) evt.getSource();
                boolean isEnabled = (button == backtestModeRadio);
                fileNameLabel.setEnabled(isEnabled);
                fileNameText.setEnabled(isEnabled);
                reportLabel.setEnabled(isEnabled);
                reportCombo.setEnabled(isEnabled);
                selectFileButton.setEnabled(isEnabled);
            }
        };

        backtestModeRadio.addActionListener(radioButtonListener);
        tradingModeRadio.addActionListener(radioButtonListener);


        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                action = JOptionPane.CANCEL_OPTION;
                cancel();
            }
        });

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (backtestModeRadio.isSelected()) {
                        String fileName = getFileName();
                        preferences.set(BackTesterFileName, fileName);
                        preferences.set(BackTesterReportComboIndex, reportCombo.getSelectedIndex());
                        if (fileName == null || fileName.length() == 0) {
                            String msg = "File name must be specified.";
                            MessageDialog.showError(TradingModeDialog.this, msg);
                            return;
                        }
                    }

                    action = JOptionPane.OK_OPTION;
                    cancel();
                } catch (Exception ex) {
                    Dispatcher.getReporter().report(ex);
                    MessageDialog.showError(TradingModeDialog.this, ex.getMessage());
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

    }

    private void init() {

        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Select Strategy Running Mode");

        getContentPane().setLayout(new BorderLayout());

        JPanel optionsPanel = new JPanel(new SpringLayout());
        JPanel backTestOptionsPanel = new JPanel(new SpringLayout());
        JPanel tradingOptionsPanel = new JPanel(new SpringLayout());

        backtestModeRadio = new JRadioButton("Backtest mode");
        backtestModeRadio.setSelected(true);
        tradingModeRadio = new JRadioButton("Real time mode (with actual or paper trading account)");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(backtestModeRadio);
        buttonGroup.add(tradingModeRadio);

        fileNameLabel = new JLabel("Historical data file:", JLabel.TRAILING);
        fileNameText = new JTextField();
        fileNameText.setText(preferences.get(BackTesterFileName));

        selectFileButton = new JButton("...");
        Dimension buttonSize = new Dimension(22, 22);
        selectFileButton.setMaximumSize(buttonSize);
        selectFileButton.setPreferredSize(buttonSize);
        fileNameLabel.setLabelFor(fileNameText);

        reportLabel = new JLabel("Reports:", JLabel.TRAILING);
        reportCombo = new JComboBox(new String[]{"Disable", "Enable"});
        reportCombo.setMaximumSize(new Dimension(80, 20));
        reportCombo.setSelectedIndex(preferences.getInt(BackTesterReportComboIndex));
        reportLabel.setLabelFor(reportCombo);

        backTestOptionsPanel.add(backtestModeRadio);
        backTestOptionsPanel.add(reportLabel);
        backTestOptionsPanel.add(reportCombo);
        backTestOptionsPanel.add(fileNameLabel);
        backTestOptionsPanel.add(fileNameText);
        backTestOptionsPanel.add(selectFileButton);


        SpringUtilities.makeOneLineGrid(backTestOptionsPanel, 6);

        tradingOptionsPanel.add(tradingModeRadio);
        SpringUtilities.makeOneLineGrid(tradingOptionsPanel, 1);


        optionsPanel.add(backTestOptionsPanel);
        optionsPanel.add(tradingOptionsPanel);
        // rows, cols, initX, initY, xPad, yPad
        SpringUtilities.makeCompactGrid(optionsPanel, 2, 1, 0, 12, 0, 0);


        JPanel controlPanel = new JPanel();

        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('C');
        okButton = new JButton("OK");
        okButton.setMnemonic('K');
        controlPanel.add(okButton);
        controlPanel.add(cancelButton);

        getContentPane().add(optionsPanel, BorderLayout.NORTH);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
        getContentPane().setPreferredSize(MIN_SIZE);
        getContentPane().setMinimumSize(getContentPane().getPreferredSize());
    }

    private void cancel() {
        dispose();
    }

    public void selectFileAction(ActionListener actionListener) {
        selectFileButton.addActionListener(actionListener);
    }


    public boolean isReportEnabled() {
        return (reportCombo.getSelectedIndex() == 1);
    }

    public String getFileName() {
        return fileNameText.getText();
    }

    public Dispatcher.Mode getMode() {
        return backtestModeRadio.isSelected() ? Dispatcher.Mode.BACK_TEST : Dispatcher.Mode.TRADE;
    }


    public void setFileName(String fileName) {
        fileNameText.setText(fileName);
    }

    public int getAction() {
        return action;
    }


}

