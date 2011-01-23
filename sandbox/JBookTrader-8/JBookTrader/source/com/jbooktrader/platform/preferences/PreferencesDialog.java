package com.jbooktrader.platform.preferences;

import com.jbooktrader.platform.c2.*;
import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PreferencesDialog extends JBTDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 25);
    private final PreferencesHolder prefs;
    private JTextField hostText, portText, webAccessUser, ntpTimeServer;
    private JSpinner clientIDSpin, webAccessPortSpin;
    private JPasswordField webAccessPasswordField, c2PasswordField;
    private JComboBox webAccessCombo;
    private C2TableModel c2TableModel;

    public PreferencesDialog(JFrame parent) throws JBookTraderException {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(parent);
        setModal(true);
        setVisible(true);
    }

    private void add(JPanel panel, JBTPreferences pref, JTextField textField) {
        textField.setText(prefs.get(pref));
        genericAdd(panel, pref, textField);
    }

    private void add(JPanel panel, JBTPreferences pref, JSpinner spinner) {
        spinner.setValue(prefs.getInt(pref));
        genericAdd(panel, pref, spinner);
    }

    private void add(JPanel panel, JBTPreferences pref, JComboBox comboBox) {
        comboBox.setSelectedItem(prefs.get(pref));
        genericAdd(panel, pref, comboBox);
    }


    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp, Dimension dimension) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(comp);
        comp.setPreferredSize(dimension);
        comp.setMaximumSize(dimension);
        panel.add(fieldNameLabel);
        panel.add(comp);
    }


    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp) {
        genericAdd(panel, pref, comp, FIELD_DIMENSION);
    }

    private void init() throws JBookTraderException {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Preferences");

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("TWS Connection", connectionTab);
        hostText = new JTextField();
        portText = new JTextField();
        clientIDSpin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portText);
        add(connectionTab, ClientID, clientIDSpin);
        SpringUtilities.makeCompactGrid(connectionTab, 3, 2, 12, 12, 8, 8);
        setWidth(connectionTab, clientIDSpin, 45);

        JPanel webAcessTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Web Access", webAcessTab);
        webAccessCombo = new JComboBox(new String[] {"disabled", "enabled"});
        webAccessPortSpin = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        webAccessUser = new JTextField();
        webAccessPasswordField = new JPasswordField();
        add(webAcessTab, WebAccess, webAccessCombo);
        add(webAcessTab, WebAccessPort, webAccessPortSpin);
        add(webAcessTab, WebAccessUser, webAccessUser);
        add(webAcessTab, WebAccessPassword, webAccessPasswordField);
        SpringUtilities.makeCompactGrid(webAcessTab, 4, 2, 12, 12, 8, 8);

        JPanel c2Tab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Collective2", c2Tab);
        JPanel passwordPanel = new JPanel(new SpringLayout());
        c2PasswordField = new JPasswordField();
        add(passwordPanel, Collective2Password, c2PasswordField);
        SpringUtilities.makeCompactGrid(passwordPanel, 1, 2, 0, 8, 4, 0);
        JScrollPane scrollPane = new JScrollPane();
        c2Tab.add(passwordPanel);
        c2Tab.add(scrollPane);
        SpringUtilities.makeCompactGrid(c2Tab, 2, 1, 12, 12, 12, 12);
        c2TableModel = new C2TableModel();
        JTable c2Table = new JTable(c2TableModel);
        c2Table.setShowGrid(false);
        scrollPane.getViewport().add(c2Table);

        JPanel timeServerTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Time Server", timeServerTab);
        ntpTimeServer = new JTextField();
        add(timeServerTab, NTPTimeServer, ntpTimeServer);
        SpringUtilities.makeCompactGrid(timeServerTab, 1, 2, 12, 12, 8, 8);

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    prefs.set(Host, hostText.getText());
                    prefs.set(Port, portText.getText());
                    prefs.set(ClientID, clientIDSpin.getValue().toString());

                    prefs.set(WebAccess, (String) webAccessCombo.getSelectedItem());
                    prefs.set(WebAccessPort, webAccessPortSpin.getValue().toString());
                    prefs.set(WebAccessUser, webAccessUser.getText());
                    prefs.set(WebAccessPassword, new String(webAccessPasswordField.getPassword()));

                    prefs.set(Collective2Password, new String(c2PasswordField.getPassword()));
                    prefs.set(Collective2Strategies, c2TableModel.getStrategies());

                    prefs.set(NTPTimeServer, ntpTimeServer.getText());

                    String msg = "Some of the preferences will not take effect until " + JBookTrader.APP_NAME + " is restarted.";
                    MessageDialog.showMessage(msg);

                    dispose();
                } catch (Exception ex) {
                    MessageDialog.showError(ex);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        setPreferredSize(new Dimension(600, 380));
    }

    private void setWidth(JPanel p, Component c, int width) throws JBookTraderException {
        try {
            SpringLayout layout = (SpringLayout) p.getLayout();
            SpringLayout.Constraints spinLayoutConstraint = layout.getConstraints(c);
            spinLayoutConstraint.setWidth(Spring.constant(width));
        } catch (ClassCastException exc) {
            throw new JBookTraderException("The first argument to makeGrid must use SpringLayout.");
        }
    }
}
