package com.jsystemtrader.platform.optimizer;

import com.jsystemtrader.platform.model.*;
import com.jsystemtrader.platform.strategy.*;

import java.util.*;

/**
 * Optimization results table model
 */
public class ResultsTableModel extends TableDataModel {
    private static final String[] SCHEMA = {"P&L", "Max drawdown", "Trades", "Profit Factor", "Kelly", "Trade Distribution"};

    public ResultsTableModel() {
        setSchema(SCHEMA);
    }

    public void updateSchema(Strategy strategy) {
        List<String> paramNames = new ArrayList<String>();
        for (StrategyParam param : strategy.getParams().getAll()) {
            paramNames.add(param.getName());
        }

        for (String paramName : SCHEMA) {
            paramNames.add(paramName);
        }

        setSchema(paramNames.toArray(new String[paramNames.size()]));
    }


    @Override
    public Class<?> getColumnClass(int c) {
        return Double.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public synchronized Object getValueAt(int row, int column) {
        return super.getValueAt(row, column);
    }

    public synchronized void setResults(List<Result> results) {

        removeAllData();

        for (Result result : results) {
            Object[] item = new Object[getColumnCount() + 1];

            StrategyParams params = result.getParams();

            int index = -1;
            for (StrategyParam param : params.getAll()) {
                item[++index] = param.getValue();
            }

            item[++index] = result.getTotalProfit();
            item[++index] = result.getMaxDrawdown();
            item[++index] = result.getTrades();
            item[++index] = result.getProfitFactor();
            item[++index] = result.getKelly();
            item[++index] = result.getTradeDistribution();

            addRow(item);
        }
    }
}
