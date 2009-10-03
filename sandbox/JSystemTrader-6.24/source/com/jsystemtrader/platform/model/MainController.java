package com.jsystemtrader.platform.model;

//import com.birosoft.liquid.LiquidLookAndFeel;
import com.birosoft.liquid.LiquidLookAndFeel;
import com.jsystemtrader.platform.backdata.*;
import com.jsystemtrader.platform.backtest.*;
import com.jsystemtrader.platform.chart.*;
import com.jsystemtrader.platform.dialog.*;
import com.jsystemtrader.platform.opentick.*;
import com.jsystemtrader.platform.optimizer.*;
import com.jsystemtrader.platform.preferences.PreferencesDialog;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import static com.jsystemtrader.platform.preferences.JSTPreferences.*;
import com.jsystemtrader.platform.startup.*;
import com.jsystemtrader.platform.strategy.*;
import com.jsystemtrader.platform.util.*;
import javax.swing.*;
import javax.swing.plaf.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Acts as a controller in the Model-View-Controller pattern
 */
public class MainController {
    private final MainFrame mainFrame;
    private final TradingModeDialog tradingModeDialog;
    private final PreferencesHolder preferences = PreferencesHolder.getInstance();

    public MainController() throws JSystemTraderException, IOException
    {
    	boolean lookAndFeelMacTitle = preferences.getBool(LookAndFeelMacStyle); 
        if(lookAndFeelMacTitle)
            LiquidLookAndFeel.setLiquidDecorations(true, "mac");
     
        String lookAndFeelClassName = preferences.get(LookAndFeelClassName);
        _setLookAndFeel(lookAndFeelClassName);

        mainFrame         = new MainFrame();
        tradingModeDialog = new TradingModeDialog(mainFrame);
        
        mainFrame.setSelectedLookAndFeel(lookAndFeelClassName);
        mainFrame.setMacWindowTitle(lookAndFeelMacTitle);

        assignListeners();
    }

    private void assignListeners() {
        mainFrame.IBHistoricalDataAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Dispatcher.setTradingMode();
                    new BackDataDialog(mainFrame);
                } catch (Throwable t) {
                    Dispatcher.getReporter().report(t);
                    MessageDialog.showError(mainFrame, t.getMessage());
                } finally {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainFrame.OTHistoricalDataAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    new OTBackDataDialog(mainFrame);
                } catch (Throwable t) {
                    Dispatcher.getReporter().report(t);
                    MessageDialog.showError(mainFrame, t.getMessage());
                } finally {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });


        tradingModeDialog.selectFileAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(JSystemTrader.getAppPath());
                fileChooser.setDialogTitle("Select Historical Data File");

                String filename = tradingModeDialog.getFileName();
                if (filename.length() != 0) {
                    fileChooser.setSelectedFile(new File(filename));
                }

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    tradingModeDialog.setFileName(file.getAbsolutePath());
                }
            }
        });


        mainFrame.runStrategiesAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    ArrayList<Strategy> selectedStrategies = mainFrame.getTradingTableModel().getSelectedStrategies();
                    if (selectedStrategies.size() == 0) {
                        MessageDialog.showError(mainFrame, "At least one strategy must be selected to run.");
                        return;
                    }

                    tradingModeDialog.setVisible(true);
                    if (tradingModeDialog.getAction() == JOptionPane.CANCEL_OPTION) {
                        return;
                    }

                    mainFrame.getTradingTableModel().saveStrategyStatus();
                    mainFrame.getTradingTableModel().reset();


                    Dispatcher.setActiveStrategies(selectedStrategies.size());
                    Dispatcher.Mode mode = tradingModeDialog.getMode();
                    if (mode == Dispatcher.Mode.BACK_TEST) {
                        Dispatcher.setBackTestingMode(tradingModeDialog);
                        for (Strategy strategy : selectedStrategies) {
                            new BackTestStrategyRunner(strategy).start();
                        }
                    }

                    if (mode == Dispatcher.Mode.TRADE) {
                        Dispatcher.setTradingMode();
                        for (Strategy strategy : selectedStrategies) {
                            new Thread(new StrategyRunner(strategy)).start();
                        }
                    }
                } catch (Throwable t) {
                    Dispatcher.getReporter().report(t);
                    MessageDialog.showError(mainFrame, t.toString());
                } finally {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        mainFrame.optimizerAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    OptimizerDialog optimizerDialog = new OptimizerDialog(mainFrame);
                    optimizerDialog.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosed(WindowEvent arg0) {
                            saveOptimizerDimensions(arg0);
                        }

                        @Override
                        public void windowClosing(WindowEvent arg0) {
                            saveOptimizerDimensions(arg0);
                        }

                        private void saveOptimizerDimensions(WindowEvent arg0) {
                            preferences.set(OptimizerHeight, arg0.getWindow().getHeight());
                            preferences.set(OptimizerWidth, arg0.getWindow().getWidth());
                            preferences.set(OptimizerX, arg0.getWindow().getX());
                            preferences.set(OptimizerY, arg0.getWindow().getY());
                        }
                    });
                } catch (Throwable t) {
                    Dispatcher.getReporter().report(t);
                    MessageDialog.showError(mainFrame, t.getMessage());
                } finally {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });


        mainFrame.userGuideAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String url = "http://www.myjavaserver.com/~nonlinear/JSystemTrader/UserManual.pdf";
                    Browser.openURL(url);
                } catch (Browser.BrowserUnavailableException t) {
                    Dispatcher.getReporter().report(t);
                    MessageDialog.showError(mainFrame, t.getMessage());
                }
            }
        });


        mainFrame.discussionAction(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    String url = "http://groups.google.com/group/jsystemtrader/topics?gvc=2";
                    Browser.openURL(url);
                } catch (Browser.BrowserUnavailableException t) {
                    Dispatcher.getReporter().report(t);
                    MessageDialog.showError(mainFrame, t.getMessage());
                }
            }
        });


        mainFrame.projectHomeAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String url = "http://code.google.com/p/jsystemtrader/";
                    Browser.openURL(url);
                } catch (Browser.BrowserUnavailableException t) {
                    Dispatcher.getReporter().report(t);
                    MessageDialog.showError(mainFrame, t.getMessage());
                }
            }
        });


        mainFrame.exitAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        mainFrame.exitAction(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            	exit();
            }
        });

        mainFrame.aboutAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AboutDialog(mainFrame);
            }
        });


        mainFrame.strategyChartAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    TradingTableModel ttm = mainFrame.getTradingTableModel();
                    int selectedRow = mainFrame.getTradingTable().getSelectedRow();
                    if (selectedRow < 0) {
                        String message = "No strategy is selected.";
                        MessageDialog.showError(mainFrame, message);
                        return;
                    }
                    Strategy strategy = ttm.getStrategyForRow(mainFrame.getTradingTable().getSelectedRow());
                    StrategyPerformanceChart spChart = new StrategyPerformanceChart(strategy);
                    JFrame chartFrame = spChart.getChartFrame(mainFrame);

                    chartFrame.setVisible(true);
                } catch (Exception ex) {
                    Dispatcher.getReporter().report(ex);
                    MessageDialog.showError(mainFrame, ex.toString());
                } finally {
                    mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }

            }
        });

        mainFrame.doubleClickTableAction(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    try {
                        mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        TradingTableModel ttm = mainFrame.getTradingTableModel();
                        int selectedRow = mainFrame.getTradingTable().getSelectedRow();
                        if (selectedRow < 0) {
                            return;
                        }
                        Strategy strategy = ttm.getStrategyForRow(mainFrame.getTradingTable().getSelectedRow());
                        StrategyPerformanceChart spChart = new StrategyPerformanceChart(strategy);
                        JFrame chartFrame = spChart.getChartFrame(mainFrame);

                        chartFrame.setVisible(true);
                    } catch (Exception ex) {
                        Dispatcher.getReporter().report(ex);
                        MessageDialog.showError(mainFrame, ex.toString());
                    } finally {
                        mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        });

        mainFrame.lookAndFeelAction( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    String newLook = mainFrame.getSelectedLookAndFeel();
                    preferences.set(LookAndFeelClassName, newLook);
                    _setLookAndFeel(newLook);
                }
            }
        });
        
        mainFrame.macWindowTitleAction( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                preferences.set(LookAndFeelMacStyle, e.getStateChange()==ItemEvent.SELECTED);
                MessageDialog.showMessage(mainFrame, "Liquid mac decoration will take effect after restarting JST");
            }
        });
        
        mainFrame.preferencesAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                	mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    JDialog preferencesDialog = new PreferencesDialog(mainFrame);
                    preferencesDialog.setVisible(true);
                } catch (Throwable t) {
                    MessageDialog.showError(mainFrame, t.getMessage());
                } finally {
                	mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        
    }
    
    private void _setLookAndFeel(String lookAndFeelName)
    {        
        try
        {
            UIManager.setLookAndFeel(lookAndFeelName);
        }         
        catch (Throwable t)
        {
            MessageDialog.showMessage(null, t.getMessage() + ": Unable to set custom look & feel. The default L&F will be used.");
        }
        
        // Set the color scheme explicitly
        ColorUIResource color = new ColorUIResource(102, 102, 153);
        UIManager.put("Label.foreground", color);
        UIManager.put("TitledBorder.titleColor", color);
        
        if(mainFrame!=null)
        {
            SwingUtilities.updateComponentTreeUI(mainFrame);
            mainFrame.pack();
        }
        
        if(tradingModeDialog!=null)
        {
            SwingUtilities.updateComponentTreeUI(tradingModeDialog);
            tradingModeDialog.pack();
        }        
    }
    
    private void exit()
    {
    	preferences.set(MainWindowHeight, mainFrame.getSize().height);
    	preferences.set(MainWindowWidth , mainFrame.getSize().width);
    	preferences.set(MainWindowX     , mainFrame.getX());
    	preferences.set(MainWindowY     , mainFrame.getY());
        Dispatcher.exit();    	
    }
    
}
