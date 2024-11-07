
package com.appdevclaymaster.strategybacktesting;

/**
 *
 * @author Suraj Prajapati (Claymaster)
 */
public class Trade {

    double buyPrice;
    double sellPrice;
    int quantity;

    public Trade(double buyPrice, double sellPrice, int quantity) {
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.quantity = quantity;
        
        System.out.println("New Trade : Buy Price "+ buyPrice + ", SellPrice=" + sellPrice + ", quantity=" + quantity + ", Profit=" + getProfit());
    }

    @Override
    public String toString() {
        return "Trades{" + "BuyPrice=" + buyPrice + ", SellPrice=" + sellPrice + ", quantity=" + quantity + '}';
    }

    public double getProfit() {
        return (sellPrice - buyPrice) * quantity;
    }

}
