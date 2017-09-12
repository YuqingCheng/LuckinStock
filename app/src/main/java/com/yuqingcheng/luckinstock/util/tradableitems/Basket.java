
package com.yuqingcheng.luckinstock.util.tradableitems;

import com.yuqingcheng.luckinstock.util.DateParser;
import com.yuqingcheng.luckinstock.util.PriceRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * a class inculds all methods for a basket of stocks.
 */
public class Basket implements TradableItem {

  HashMap<String, Integer> shares;

  HashMap<String, Stock> stock;

  int date;

  /**
   * constructs a basket of stocks.
   *
   * @param stockShareMapForBasket a map contains information about shares of every stock
   */

  public Basket(Map<String, Integer> stockShareMapForBasket, Object date) {
    shares = new HashMap<>();
    stock = new HashMap<>();
    for (String key : stockShareMapForBasket.keySet()) {
      shares.put(key, stockShareMapForBasket.get(key));
      stock.put(key, new Stock(key));
    }
    this.date = DateParser.parseDateToInteger(date);
  }

  /**
   * gets the historical prices for a basket of stocks.
   *
   * @param fromDate the start date
   * @param toDate   the end date
   * @return a map contains information about price of the basket for specific days
   */
  public Map<Integer, PriceRecord> getHistoricalPrices(Object fromDate, Object toDate) {
    // Sample date: 20170608
    int fromDateAsIneteger = DateParser.parseDateToInteger(fromDate);
    int toDateAsInteger = DateParser.parseDateToInteger(toDate);
    if (toDateAsInteger < this.date) {
      throw new IllegalArgumentException(
              "Cannot get price during time range before basket creation date");
    }
    fromDateAsIneteger = Math.max(this.date, fromDateAsIneteger);
    Map<Integer, PriceRecord> res = new TreeMap<>();
    for (String key : stock.keySet()) {
      Map<Integer, PriceRecord> stockData =
              stock.get(key).getHistoricalPrices(fromDateAsIneteger, toDateAsInteger);

      for (Integer date : stockData.keySet()) {
        if (res.containsKey(date)) {
          res.put(date, res.get(date).add(stockData.get(date).times(shares.get(key))));
        } else {
          res.put(date, stockData.get(date).times(shares.get(key)));
        }
      }
    }

    return res;
  }

  /**
   * get the price record of basket of stocks for a certain day.
   *
   * @param date specific date
   * @return price record for a basket of stocks
   */
  public PriceRecord getPriceOnCertainDay(Object date) {
    // Sample date: 20170608

    if (DateParser.parseDateToInteger(date) < this.date) {
      throw new IllegalArgumentException("Cannot get price on a day before basket creation date");
    }

    int dateAsInteger = DateParser.parseDateToInteger(date);

    return this.getHistoricalPrices(date, date).get(dateAsInteger);

  }

  /**
   * calculates the X-day moving average ends in a specific day.
   *
   * @param date the end day
   * @param x    the number of days
   * @return averarge price as a double
   */
  public double xDayMovingAverage(Object date, int x) {
    if (DateParser.parseDateToInteger(date) < this.date) {
      throw new IllegalArgumentException("Cannot get price on a day before basket creation date");
    }
    double res = 0;
    for (String key : stock.keySet()) {
      res = shares.get(key) * stock.get(key).xDayMovingAverage(date, x);
    }
    return res;
  }

  /**
   * gets the historical Xday moving average trend for a specific basket.
   *
   * @param fromDate the start date
   * @param toDate   the end date
   * @param x        number of days
   * @return the historical Xday moving average trend for a specific basket
   */
  public Map<Integer, Double> getHistoricalXDayMovingAverage(
          Object fromDate, Object toDate, int x) {

    int fromDateAsIneteger = DateParser.parseDateToInteger(fromDate);
    int toDateAsInteger = DateParser.parseDateToInteger(toDate);
    if (toDateAsInteger < this.date) {
      throw new IllegalArgumentException(
              "Cannot get price during time range before basket creation date");
    }
    fromDateAsIneteger = Math.max(this.date, fromDateAsIneteger);
    Map<Integer, Double> res = new TreeMap<>();
    for (String key : stock.keySet()) {
      Map<Integer, Double> stockData =
              stock.get(key).getHistoricalXDayMovingAverage(fromDateAsIneteger, toDateAsInteger, x);

      for (Integer date : stockData.keySet()) {
        if (res.containsKey(date)) {
          res.put(date, res.get(date) + stockData.get(date) * shares.get(key));
        } else {
          res.put(date, stockData.get(date) * shares.get(key));
        }
      }
    }
    return res;
  }


}
