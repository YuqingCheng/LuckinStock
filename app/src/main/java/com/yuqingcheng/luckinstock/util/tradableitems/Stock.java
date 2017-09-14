package com.yuqingcheng.luckinstock.util.tradableitems;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.LinkedList;

import com.yuqingcheng.luckinstock.util.DateParser;
import com.yuqingcheng.luckinstock.util.PriceRecord;
import com.yuqingcheng.luckinstock.util.dataretriever.StockDataRetriever;
import com.yuqingcheng.luckinstock.util.dataretriever.WebStockDataRetriever;

/**
 * a class includes all methods for a single stock.
 */
public class Stock implements TradableItem {

  String stockSymbol;
  StockDataRetriever retriever;

  /**
   * a constructor to initialize a stock.
   *
   * @param stockSymbol symbol that represents a stock
   * @throws IllegalArgumentException Invalid input of stock symbol.
   */

  public Stock(String stockSymbol) throws IllegalArgumentException {
    this.retriever = new WebStockDataRetriever(); //data retriever in use
    if (!isValidStockSymbol(stockSymbol)) {
      throw new IllegalArgumentException("Invalid input of stock symbol.");
    }
    this.stockSymbol = stockSymbol;

  }

  /**
   * a default constructor.
   */
  public Stock() {
    this.stockSymbol = null;
    this.retriever = new WebStockDataRetriever();
  }

  public String getName() throws IllegalArgumentException{
    try{
      return retriever.getName(this.stockSymbol);
    }catch(Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Stock symbol is not valid.");
    }
  }

  /**
   * decide whether a given string is a valid stock symbol.
   *
   * @param stockSymbol a given string which represents a stock symbol
   * @return true or flase
   */
  public boolean isValidStockSymbol(String stockSymbol) {
    try {
      String temp = retriever.getName(stockSymbol);

      if (temp.equals("N/A")) {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * gets the historical prices for a single stock.
   *
   * @param fromDate the start date
   * @param toDate   the end date
   * @return a map represents the information about price of single stock for specific days
   */
  public Map<Integer, PriceRecord> getHistoricalPrices(Object fromDate, Object toDate)
          throws IllegalArgumentException {


    int fromDateAsInteger = DateParser.parseDateToInteger(fromDate);

    int toDateAsInteger = DateParser.parseDateToInteger(toDate);

    int fromDay = fromDateAsInteger % 100;
    fromDateAsInteger /= 100;
    int fromMonth = fromDateAsInteger % 100;
    fromDateAsInteger /= 100;
    int fromYear = fromDateAsInteger;

    int toDay = toDateAsInteger % 100;
    toDateAsInteger /= 100;
    int toMonth = toDateAsInteger % 100;
    toDateAsInteger /= 100;
    int toYear = toDateAsInteger;

    try {
      return retriever.getHistoricalPrices(
              this.stockSymbol, fromDay, fromMonth, fromYear, toDay, toMonth, toYear);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException("invalid input of date.");
    }
  }

  /**
   * get the price record of a single stocks for a certain day.
   *
   * @param date specific date
   * @return price record for a single stock
   */
  public PriceRecord getPriceOnCertainDay(Object date) {
    // Sample date: 20170608
    int dateAsInteger = DateParser.parseDateToInteger(date);
    return this.getHistoricalPrices(date, date).get(dateAsInteger);
  }

  /**
   * calculates the X-day moving average ends in a specific day.
   *
   * @param date the end day
   * @param x    the number of days
   * @return average price as a double
   */
  public double xDayMovingAverage(Object date, int x) {
    if (x <= 0) {
      throw new IllegalArgumentException("x must be positive.");
    }
    double sum = 0;
    int start = DateParser.dateAddBusinessDays(date, -2 * x);
    int dateAsInteger = DateParser.parseDateToInteger(date);
    Map<Integer, PriceRecord> hp = this.getHistoricalPrices(start, dateAsInteger);

    if (!hp.containsKey(dateAsInteger)) {
      throw new IllegalArgumentException("Invalid input of date.");
    }

    for (int i = 0; i < x; i++) {
      if (hp.containsKey(dateAsInteger)) {
        sum += hp.get(dateAsInteger).getClosePrice();
      } else {
        i--;
      }
      dateAsInteger = DateParser.dateAddBusinessDays(dateAsInteger, -1);
    }
    return sum / x;
  }

  /**
   * gets the historical Xday moving average trend for a specific stock.
   *
   * @param fromDate the start date
   * @param toDate   the end date
   * @param x        number of days
   * @return the historical Xday moving average trend for a specific stock
   */
  public Map<Integer, Double> getHistoricalXDayMovingAverage(
          Object fromDate, Object toDate, int x) {

    if (x <= 0) {
      throw new IllegalArgumentException("x must be positive.");
    }
    int fromDateAsInteger = DateParser.parseDateToInteger(fromDate);

    int toDateAsInteger = DateParser.parseDateToInteger(toDate);

    Map<Integer, PriceRecord> rangePrices
            =
            this.getHistoricalPrices(fromDateAsInteger, toDateAsInteger);

    while (!rangePrices.containsKey(fromDateAsInteger)) {
      fromDateAsInteger = DateParser.dateAddBusinessDays(fromDateAsInteger, 1);
    }

    double sum = 0;
    int start = DateParser.dateAddBusinessDays(fromDateAsInteger, -2 * x);

    Map<Integer, PriceRecord> historicalPrices = this.getHistoricalPrices(start, fromDateAsInteger);

    Map<Integer, PriceRecord> hp = new HashMap<>();

    for (Map.Entry<Integer, PriceRecord> each : historicalPrices.entrySet()) {
      hp.put(each.getKey(), each.getValue());
    }

    LinkedList<Double> queue = new LinkedList<>();

    int dateAsInteger = fromDateAsInteger;

    for (int i = 0; i < x; i++) {
      if (hp.containsKey(dateAsInteger)) {
        queue.addFirst(hp.get(dateAsInteger).getClosePrice());
        sum += hp.get(dateAsInteger).getClosePrice();
      } else {
        i--;
      }
      dateAsInteger = DateParser.dateAddBusinessDays(dateAsInteger, -1);
    }

    Map<Integer, Double> res = new TreeMap<>();

    res.put(fromDateAsInteger, sum / x);

    for (Map.Entry<Integer, PriceRecord> each : rangePrices.entrySet()) {
      if (each.getKey() > fromDateAsInteger) {
        sum -= queue.pollFirst();
        sum += each.getValue().getClosePrice();
        queue.offer(each.getValue().getClosePrice());
        res.put(each.getKey(), sum / x);
      }
    }

    return res;

  }

}
