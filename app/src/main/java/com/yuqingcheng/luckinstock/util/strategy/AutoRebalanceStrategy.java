package com.yuqingcheng.luckinstock.util.strategy;

import com.yuqingcheng.luckinstock.util.PriceRecord;
import com.yuqingcheng.luckinstock.util.tradableitems.Stock;
import com.yuqingcheng.luckinstock.util.DateParser;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * This class implements the automatic rebalancing strategy in investing stocks.
 */
public class AutoRebalanceStrategy implements Strategy {

  private Map<String, Integer> stockShareMap;

  private Map<String, Map<Integer, PriceRecord>> stockHistoricalPrices;

  private Map<Integer, Double> basketHistoricalPrice;

  private TreeSet<Integer> dates;

  private int period;

  private double originalPrice;

  private double profit;

  private Map<String, Double> proportion;

  /**
   * This constructor initializes the strategy object with basket information and strategy
   * information as input, according to automatic rebalancing strategy, the invest is used to buy
   * shares of stocks to this basket in creation of this object, and the historical data of price
   * in executing this strategy is also obtained in this process and stored in the object's data
   * field, which could save time for data retrieving in future call of methods,
   * **NOTE**
   * 1) the strategy is assumed to be executed on the creation date of the basket;
   * 2) the proportion is determined by the initial price and shares of stock in the basket, instead
   * of user input.
   *
   * @param stockShareMap a Map with each stock's symbol and shares.
   * @param setDate       creation date of the basket.
   * @param invest        amount of invest.
   * @param period        number of days of rebalancing period.
   * @param endingDate    ending date to determine profit.
   */

  public AutoRebalanceStrategy(Map<String, Integer> stockShareMap, Object setDate, double invest,
                               int period, Object endingDate) throws IllegalArgumentException {

    if (period <= 0) {
      throw new IllegalArgumentException("Period should be positive.");
    }
    int setUpDate = DateParser.parseDateToInteger(setDate);

    int endDate = DateParser.parseDateToInteger(endingDate);

    this.stockShareMap = new HashMap<>();

    this.stockHistoricalPrices = new HashMap<>();

    boolean dateUnset = true;

    for (Map.Entry<String, Integer> each : stockShareMap.entrySet()) {
      String stockSymbol = each.getKey();
      Integer shares = each.getValue();
      Stock stock = new Stock(stockSymbol);
      Map<Integer, PriceRecord> hp = stock.getHistoricalPrices(setUpDate, endDate);
      if (dateUnset) {
        this.dates = new TreeSet(hp.keySet());
        dateUnset = false;
      }
      this.stockHistoricalPrices.put(stockSymbol, hp);
      this.stockShareMap.put(stockSymbol, shares);
    }

    double sum = 0;

    this.proportion = new HashMap<>();

    for (Map.Entry<String, Integer> each : this.stockShareMap.entrySet()) {
      String stockSymbol = each.getKey();
      Integer shares = each.getValue();
      Double price = stockHistoricalPrices.get(stockSymbol).get(dates.first()).getClosePrice();
      sum += price * shares;
      this.proportion.put(stockSymbol, shares * price);
    }

    if (sum > 0) {
      for (String stockSymbol : this.proportion.keySet()) {
        this.proportion.put(stockSymbol, this.proportion.get(stockSymbol) / sum);
      }
    }

    for (String stockSymbol : this.stockShareMap.keySet()) {

      Double price = stockHistoricalPrices.get(stockSymbol).get(dates.first()).getClosePrice();

      Integer newShares = (int)(Math.round(invest * proportion.get(stockSymbol) / price));

      this.stockShareMap.put(stockSymbol, this.stockShareMap.get(stockSymbol) + newShares);
    }

    this.period = period;

    this.profit = 0.0;

    this.originalPrice = 0;

    for (String name : this.stockShareMap.keySet()) {
      int shares = this.stockShareMap.get(name);
      this.originalPrice +=
              stockHistoricalPrices.get(name).get(dates.first()).getClosePrice() * shares;
    }

    this.basketHistoricalPrice = new TreeMap<>();

    generateHistoricalPriceRecord();

  }

  /**
   * according to rebalancing period, adjust the number of shares in the basket with respect to
   * time.
   */
  private void generateHistoricalPriceRecord() {
    int count = 0;
    for (Integer date : this.dates) {
      count++;
      if (count % this.period == 0) {
        resetShares(date);
        count = 0;
      }
      basketHistoricalPrice.put(date, 0.0);
      for (String stockSymbol : this.stockHistoricalPrices.keySet()) {
        int shares = this.stockShareMap.get(stockSymbol);
        double price = this.stockHistoricalPrices.get(stockSymbol).get(date).getClosePrice();
        basketHistoricalPrice.put(date, basketHistoricalPrice.get(date) + price * shares);
      }
    }
  }

  /**
   * adjust the number of shares in the basket at specific date, the thumb of rule is to calculate
   * a target amount of money by dividing current basket price with the original proportion, and add
   * or subtract proper number of shares to get as close to the target as possible, since one share
   * is not dividable, the number of shares are rounded to closest integer.
   *
   * @param date date to reset shares.
   */
  private void resetShares(int date) {

    double basketPrice = 0;

    for (String name : stockShareMap.keySet()) {
      int shares = stockShareMap.get(name);
      basketPrice += stockHistoricalPrices.get(name).get(date).getClosePrice() * shares;
    }

    for (String name : stockShareMap.keySet()) {
      int shares = stockShareMap.get(name);
      double stockPrice = stockHistoricalPrices.get(name).get(date).getClosePrice();
      double target = basketPrice * proportion.get(name);
      int changeInShares = 0;
      if (target - stockPrice * shares >= 0) {
        changeInShares = (int)(Math.round((target - stockPrice * shares) / stockPrice));
      } else {
        changeInShares = -(int)(Math.round((stockPrice * shares - target) / stockPrice));
      }

      this.profit -= changeInShares * stockPrice;

      this.stockShareMap.put(name, shares + changeInShares);
    }

  }

  /**
   * get the total profit made by executing this strategy.
   *
   * @return total profit made as a double.
   */
  public Map<Integer, Double> basketHistoricalPrice() {

    return this.basketHistoricalPrice;

  }

  /**
   * get the basket's price data in the form of a map when executing the strategy, in which the key
   * is the date, the value is closing price on this date.
   *
   * @return basket historical price-date data as a Map.
   */
  public double totalProfit() {
    double currPrice = 0.0;
    for (String name : stockShareMap.keySet()) {
      int shares = stockShareMap.get(name);
      currPrice += stockHistoricalPrices.get(name).get(dates.last()).getClosePrice() * shares;
    }

    return currPrice - originalPrice + profit;
  }

}
