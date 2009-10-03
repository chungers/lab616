/**
 * Original author Eugene Kononov <nonlinear5@yahoo.com> 
 * Adapted for JST by Florent Guiliani <florent@guiliani.fr>
 */

package com.jsystemtrader.platform.preferences;


public enum JSTPreferences {
       
    // TWS connection
    Host("Host", "localhost"),
    Port("Port", "7496"),
    ClientID("Client ID", "0"),
    AdvisorAccount("Enable advisor account", "true"),
    AdvisorAccountNumber("Advisor account number", ""),
    PortfolioSync("Enable portfolio sync (experimental)","true"),
    TimeLagAllowed("Time lag allowed","2"),
 
    // Reporting
    ReportRenderer("Report renderer", "com.jsystemtrader.platform.report.HTMLReportRenderer"),
    ReportRecycling("Report recycling", "Append"),

    // Backtest & Optimizer
    BacktestShowNumberOfBar("Show number of loaded bars","true"),    
    BackTesterFileName("backTester.dataFileName", ""),
    BackTesterReportComboIndex("backTester.report.comboindex","0"),
    OptimizerMaxThread("Number of threads used by the optimizer","2"),    
    OptimizerFileName("optimizer.dataFileName", ""),
    OptimizerMinTrades("optimizer.minTrades", "20"),
    OptimizerSelectBy("optimizer.selectBy", ""),
    OptimizerStrategyName("optimizer.strategy.name",""),
    OptimizerWidth("optimizer.width", "-1"),
    OptimizerHeight("optimizer.height", "-1"),
    OptimizerX("optimizer.x", "-1"),
    OptimizerY("optimizer.y", "-1"),    

    // IB Back data downloader properties
	Exchanges("Exchanges","SMART,GLOBEX,ECBOT,CBOE,NYSE,NASDAQ,AMEX,NYMEX,LIFFE,IDEALPRO,DTB,SGX,KSE,HKFE"),
    Currencies("Currencies","USD,EUR,GBP,CHF,JPY,AUD,KRW,HKD"),
    IBBackDataStartDate("IBBackDataStartDate",""),
    IBBackDataEndDate("IBBackDataEndDate",""),
    IBBackDataTicker("IBBackDataTicker","GOOG"),
    IBBackDataFileName("IBBackDataFileName",""),
    IBBackDataSecType("IBBackDataSecType","0"),
    IBBackDataExpirationMonth("IBBackDataExpirationMonth","0"),
    IBBackDataExpirationYear("IBBackDataExpirationYear","0"),
    IBBackDataExchange("IBBackDataExchange","0"),
    IBBackDataCurrency("IBBackDataCurrency","0"),
    IBBackDataBarSize("IBBackDataBarSize","0"),
    IBBackDataRTHOnly("IBBackDataRTHOnly","0"),
    
    // EMail properties
    MailTransportProtocol("Mail Transport Protocol","smtps"),
    MailSMTPSAuth("Enable SMTPS Auth","true"),
    MailSMTPSQuitWair("Enable SMTPS Quitwait","false"),
    MailHost("Mail server name or ip","smtp.gmail.com"),
    MailUser("Mail account login","absdefg@abcdefg.com"),
    MailPassword("Mail account password","EmailPassword"),
    MailSubject("Mail subject","JSystemTrader Notification"),
    MailRecipient("Mail Recipient","EMailRecipient"),
    
    // Main window
    MainWindowWidth("mainwindow.width", "-1"),
    MainWindowHeight("mainwindow.height", "-1"),
    MainWindowX("mainwindow.x", "-1"),
    MainWindowY("mainwindow.y", "-1"),
    
    // Chart
    ChartWidth("chart.width", "-1"),
    ChartHeight("chart.height", "-1"),
    ChartX("chart.x", "-1"),
    ChartY("chart.y", "-1"),
    ChartState("chart.state", "-1"),
    
    //OpenTick
    OpenTickUserName("opentick.username",""),
    OpenTickPassword("opentick.password",""),
    OpenTickSecurity("opentick.security",""),
    OpenTickExchange("opentick.exchange",""),
    OpenTickBarsize("opentick.barsize","0"),
    OpenTickDateStart("opentick.datestart","0"),
    OpenTickDateEnd("opentick.dateend","0"),
    OpenTickFileName("opentick.filename",""),
    
   
    // Look and feel
    LookAndFeelClassName("lookAndFeel.className","com.birosoft.liquid.LiquidLookAndFeel"),
    LookAndFeelMacStyle("lookAndFeel.alaMacWindowTitle","false");
    

    private final String name, defaultValue;

    JSTPreferences(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getDefault() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }
}
