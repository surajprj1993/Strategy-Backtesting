/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.appdevclaymaster.strategybacktesting;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Suraj Prajapati (Claymaster)
 */
public class StrategyBacktesting {

    public static List<OHLC> readCSV(String filePath) throws IOException {
        List<OHLC> ohlcList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try (CSVParser parser = new CSVParser(new FileReader(filePath), CSVFormat.DEFAULT.withHeader())) {
            for (CSVRecord record : parser) {
                Date date = sdf.parse(record.get("datetime"));
                String symbol = record.get("symbol");
                double open = Double.parseDouble(record.get("open"));
                double high = Double.parseDouble(record.get("high"));
                double low = Double.parseDouble(record.get("low"));
                double close = Double.parseDouble(record.get("close"));
                double volume = Double.parseDouble(record.get("volume"));
                ohlcList.add(new OHLC(date, symbol, open, high, low, close, volume));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ohlcList;
    }

    public static double[] calculateRSI(List<Double> prices, int period) {
        double[] rsi = new double[prices.size()];
        double gain = 0, loss = 0;

        for (int i = 1; i <= period; i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gain += change;
            } else {
                loss -= change;
            }
        }

        gain /= period;
        loss /= period;
        double rs = gain / loss;
        rsi[period] = 100 - (100 / (1 + rs));

        for (int i = period + 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gain = ((gain * (period - 1)) + change) / period;
                loss = ((loss * (period - 1))) / period;
            } else {
                gain = ((gain * (period - 1))) / period;
                loss = ((loss * (period - 1)) - change) / period;
            }
            rs = gain / loss;
            rsi[i] = 100 - (100 / (1 + rs));
        }

        return rsi;
    }

    public static void main(String[] args) {

        String filePath = "src\\main\\resources\\30min_OHLC_data\\RELIANCE.csv";
        try {
            List<OHLC> ohlcList = readCSV(filePath);
            List<Double> closingPrices = new ArrayList<>();
            for (OHLC ohlc : ohlcList) {
                closingPrices.add(ohlc.close);
            }

            double[] rsi = calculateRSI(closingPrices, 14); // Using a 14-period RSI

            double openingRangeHigh = 0.0, openingRangeLow = 0.0;
            boolean isLong = false;
            boolean isShort = false;
            double buyPrice = 0, sellPrice = 0, stopLoss = 0;
            int quantity = 0;
            List<Trade> tradeList = new ArrayList<>();;
            double maxLossPerTrade = 5000.0;

            for (int i = 1; i < ohlcList.size(); i++) {

                OHLC currentCandle = ohlcList.get(i);
                OHLC previousCandle = ohlcList.get(i - 1);

                int hrs = currentCandle.date.getHours();
                int min = currentCandle.date.getMinutes();

                if (hrs == 9 && min == 15) {
                    System.out.println("Date Time is " + currentCandle.date);
                    openingRangeHigh = currentCandle.high;
                    openingRangeLow = currentCandle.low;
                    System.out.println("opening candle high is " + openingRangeHigh + " & Low is " + openingRangeLow);
                    quantity = (int) Math.round(maxLossPerTrade / (openingRangeHigh - openingRangeLow));
                }

                if (hrs == 15 && min == 15) {
                    System.out.println("closing candle");
                    if (isLong) {
                        System.out.println("Square off at " + currentCandle.open);
                        tradeList.add(new Trade(buyPrice, currentCandle.open, quantity));
                    }
                    if (isShort) {
                        System.out.println("Square off at " + currentCandle.open);
                        tradeList.add(new Trade(currentCandle.open, sellPrice, quantity));
                    }
                    isLong = false;
                    isShort = false;
                    openingRangeHigh = 0.0;
                    openingRangeLow = 0.0;
                    buyPrice = 0.0;
                    sellPrice = 0.0;
                    stopLoss = 0.0;
                    quantity = 0;

                }

                if (!isLong) {

                    if (currentCandle.high > openingRangeHigh && rsi[i] > 55) {
                        isLong = true;
                        buyPrice = currentCandle.close;
                        System.out.println("Buy at " + buyPrice + " with Stop Loss at " + openingRangeLow);
                    }
                }

                if (!isShort) {
                    if (currentCandle.low < openingRangeLow && rsi[i] < 45) {
                        isShort = true;
                        sellPrice = currentCandle.close;
                        System.out.println("Sell at " + sellPrice + " with Stop Loss at " + openingRangeHigh);
                    }
                }

                if (isLong) {
                    if (currentCandle.low < openingRangeLow && rsi[i] < 45) {
                        isLong = false;
                        System.out.println("Buy stoploss hit at " + currentCandle.date + " @" + openingRangeLow);
                        tradeList.add(new Trade(buyPrice, openingRangeLow, quantity));
                    }
                }
                if (isShort) {
                    if (currentCandle.high > openingRangeHigh && rsi[i] > 55) {
                        isShort = false;
                        System.out.println("Sell stoploss hit at " + currentCandle.date + " @" + openingRangeHigh);
                        tradeList.add(new Trade(openingRangeHigh, sellPrice, quantity));
                    }
                }

            }

            StrategyPerformance.analyzePerformance(tradeList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
