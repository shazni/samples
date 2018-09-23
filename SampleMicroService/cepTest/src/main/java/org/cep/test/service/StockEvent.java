package org.cep.test.service;

public class StockEvent {
    private String symbol;
    private double openPrice;
    private double closePrice;
    private double highPrice;
    private double lowPrice;

    public StockEvent(String symbol, double openPrice, double closePrice,
             double highPrice, double lowPrice) {
        this.symbol = symbol;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }
}
