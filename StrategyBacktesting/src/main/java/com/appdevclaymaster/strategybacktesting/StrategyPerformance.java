/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.appdevclaymaster.strategybacktesting;

import java.util.List;

/**
 *
 * @author Suraj Prajapati (Claymaster)
 */
public class StrategyPerformance {
    
    public static void analyzePerformance(List<Trade> trades) {
        double totalProfitLoss = 0;
        int wins = 0;
        int losses = 0;
        double maxDrawdown = 0;
        double peakEquity = 0;
        double equity = 0;

        for (Trade trade : trades) {
            double profit = trade.getProfit();
            totalProfitLoss += profit;
            equity += profit;
            peakEquity = Math.max(peakEquity, equity);
            maxDrawdown = Math.min(maxDrawdown, equity - peakEquity);
            if (trade.getProfit()>0) {
                wins++;
            } else {
                losses++;
            }
        }

        double winRate = wins / (double) trades.size();
        double averageProfitLoss = totalProfitLoss / trades.size();
        double profitFactor = wins == 0 ? 0 : (totalProfitLoss + (losses * averageProfitLoss)) / (-totalProfitLoss + (wins * averageProfitLoss));

        System.out.printf("Total Profit/Loss: %.2f\n", totalProfitLoss);
        System.out.printf("Win Rate: %.2f%%\n", winRate * 100);
        System.out.printf("Average Profit/Loss per Trade: %.2f\n", averageProfitLoss);
        System.out.printf("Maximum Drawdown: %.2f\n", maxDrawdown);
        System.out.printf("Profit Factor: %.2f\n", profitFactor);
    }
    
}
