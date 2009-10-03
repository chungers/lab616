package com.jsystemtrader.platform.opentick;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.report.*;
import com.opentick.*;

import java.util.*;

/**
 */
public class OpenTickClient extends OTClient {
    private final OTBackDataDownloader downloader;
    private final Report eventReport;


    public OpenTickClient(OTBackDataDownloader downloader) {
        this.downloader = downloader;
        eventReport = Dispatcher.getReporter();

        addHost("feed1.opentick.com", 10010);
        addHost("feed1.opentick.com", 10015);
        addHost("feed2.opentick.com", 10010);
    }

    @Override
    public void onRestoreConnection() {
        eventReport.report("OpenTick connection restored");
    }

    @Override
    public void onError(OTError error) {
        eventReport.report("OpenTick Error: " + error);
        downloader.error(error);
    }

    @Override
    public void onStatusChanged(int status) {
        eventReport.report("OpenTick login status " + status);
    }

    @Override
    public void onLogin() {
        eventReport.report("Logged in to OpenTick server");
        downloader.getExchanges();
    }

    @Override
    public void onMessage(OTMessage message) {
        eventReport.report("OpenTick Message: " + message);
        String description = message.getDescription();
        if (description.equalsIgnoreCase("End of data")) {
            downloader.responseCompleted();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onListExchanges(List exchanges) {
        eventReport.report("Received list of " + exchanges.size() + " OpenTick exchanges");
        exchanges.remove(0);
        downloader.setExchanges(exchanges);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onListSymbols(List symbols) {
        eventReport.report("Received list of " + symbols.size() + " OpenTick symbols for specified exchange");
        downloader.setSecurities(symbols);
    }

    @Override
    public void onHistOHLC(OTOHLC bar) {
        downloader.addBar(bar);
    }

    @Override
    public void onHistTrade(OTTrade trade) {
        if (trade.getPrice() > 0) {
            downloader.addTrade(trade);
        }
    }

}
