package com.yuqingcheng.luckinstock.model.trader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;

import com.yuqingcheng.luckinstock.util.TrendType;
import com.yuqingcheng.luckinstock.util.TrendHandler;
import com.yuqingcheng.luckinstock.util.tradableitems.Stock;
import com.yuqingcheng.luckinstock.util.tradableitems.Basket;
import com.yuqingcheng.luckinstock.util.DateParser;

import com.yuqingcheng.luckinstock.util.PriceRecord;
import com.yuqingcheng.luckinstock.util.strategy.Strategy;
import com.yuqingcheng.luckinstock.util.strategy.AutoRebalanceStrategy;

/**
 * This class is the key model for analysing stock and basket data.
 */
public class MyStockAnalyzer implements StockAnalyzer {

  private Map<String, Map<String, Integer>> baskets;

  private Map<String, Integer> basketSetDates;

  private Map<String, Map<Integer, Double>> displayedItems;

  private Map<String, List<Integer>> xMap;

  private Map<String, List<Double>> yMap;

  private Map<String, Integer> curveUpdateMap; // used to detect updated curves compared to view

  private Map<String, Integer> maUpdateMap;

  private Map<String, Strategy> strategyMap;

  /**
   * a constructor to initialize the data field.
   */
  public MyStockAnalyzer() {
    this.baskets = new HashMap<>();
    this.basketSetDates = new HashMap<>();
    this.displayedItems = new HashMap<>();
    this.xMap = new HashMap<>();
    this.yMap = new HashMap<>();
    this.curveUpdateMap = new HashMap<>();
    this.maUpdateMap = new HashMap<>();
    this.strategyMap = new HashMap<>();
  }


  /**
   * Add new basket to the MyStockAnalyzer object.
   *
   * @param basketName name of the basket as a String.
   * @param setDate    set-up date of the basket.
   */
  public void addNewBasket(String basketName, Object setDate) throws IllegalArgumentException {

    if (this.baskets.containsKey(basketName)) {
      throw new IllegalArgumentException("Basket already exists.");
    }

    int dateAsInteger = DateParser.parseDateToInteger(setDate);
    Map<String, Integer> temp = new HashMap<>();
    this.baskets.put(basketName, temp);
    this.basketSetDates.put(basketName, dateAsInteger);
  }

  /**
   * Determines whether the given basket name exists in this MyStockAnalyzer object
   *
   * @param basketName name of the basket as a String.
   */
  public boolean basketExists(String basketName) {
    return this.baskets.containsKey(basketName);
  }

  /**
   * determines if the displayed items contain the consumed name.
   *
   * @param name of curve (stock/basket/moving average/strategy curve) as string.
   * @return true if the displayed items contain this name, false otherwise.
   */
  public boolean displayedItemsContain(String name) {
    for (String each : this.displayedItems.keySet()) {
      String[] strs = each.split("-MA-");
      if (name.equals(strs[0])) {
        return true;
      }
    }
    return false;
  }

  /**
   * add shares of stocks to existing basket, no matter whether the basket already contains the
   * stock.
   *
   * @param basketName   basket to add shares of stock.
   * @param shareOfStock a Map maintaining the data of stocks and shares, keys are stock symbol,
   *                     values are number of shares.
   */
  public void addStockOrSharesToBasket(String basketName, Map<String, Integer> shareOfStock)
          throws IllegalArgumentException {

    if (!this.baskets.containsKey(basketName)) {
      throw new IllegalArgumentException("Given basket name doesn't exist.");
    }

    for (String stockSymbol : shareOfStock.keySet()) {
      if (!isValidStockSymbol(stockSymbol)) {
        throw new IllegalArgumentException("Specified stock symbol doesn't exist.");
      }
    }

    Map<String, Integer> basket = this.baskets.get(basketName);

    for (Map.Entry<String, Integer> each : shareOfStock.entrySet()) {
      String key = each.getKey();
      if (basket.containsKey(key)) {
        basket.put(key, basket.get(key) + shareOfStock.get(key));
      } else {
        basket.put(key, shareOfStock.get(key));
      }
    }
  }

  public Map<String, List<Integer>> getXMap() { return this.xMap; }

  public Map<String, List<Double>> getYMap() { return this.yMap; }

  public Map<String, Integer> getCurveUpdateMap() { return this.curveUpdateMap; }

  @Override
  public Map<String, Integer> getMaUpdateMap() {
    return maUpdateMap;
  }

  /**
   * Get the content of the basket in the form of a Map.
   *
   * @param basketName name of the basket as a string.
   * @return content of the basket as a map, keys are stock symbol, values are number of shares.
   */

  public Map<String, Integer> getBasketContent(String basketName) throws IllegalArgumentException {
    if (!basketExists(basketName)) {
      throw new IllegalArgumentException("Basket doesn't exist.");
    }
    return this.baskets.get(basketName);
  }

  /**
   * decide whether a given string is a valid stock symbol.
   *
   * @param stockSymbol a given string which represents a stock symbol
   * @return true or false
   */
  public boolean isValidStockSymbol(String stockSymbol) {

    return new Stock().isValidStockSymbol(stockSymbol);

  }

  /**
   * decide whether a given input date range is in valid form.
   *
   * @param fromDate from date.
   * @param toDate to date.
   * @return whether a given input date range is in valid form.
   */
  public boolean isValidDateRange(String fromDate, String toDate) {

    int fromDateAsInt = 0;
    int toDateAsInt = 0;

    try{
      fromDateAsInt = DateParser.parseDateToInteger(fromDate);
      toDateAsInt = DateParser.parseDateToInteger(toDate);

      PriceRecord temp = new Stock("AAPL").getPriceOnCertainDay(toDateAsInt);

    } catch(Exception e) {
      return false;
    }

    return fromDateAsInt <= toDateAsInt;

  }

  @Override
  public String getStockName(String symbol) throws IllegalArgumentException {
    try{
      return new Stock(symbol).getName();
    }catch(IllegalArgumentException e) {
      throw new IllegalArgumentException("Symbol is not valid.");
    }
  }

  /**
   * add historical price data of basket or stocks to displayed items.
   *
   * @param name     of basket or stock symbol.
   * @param fromDate starting date of the historical data.
   * @param toDate   ending date of the historical data.
   */
  public void addStockOrBasketHistoricalDataToDisplayedItems(String name, Object fromDate,
                                                             Object toDate) {

    Map<Integer, Double> hp;

    if (baskets.containsKey(name)) {
      hp = getHistoricalPrices(
              baskets.get(name), basketSetDates.get(name), fromDate, toDate);
    } else {
      hp = getHistoricalPrices(name, fromDate, toDate);
    }

    if(curveUpdateMap.containsKey(name)) {
      curveUpdateMap.put(name, curveUpdateMap.get(name)+1);
    }else{
      curveUpdateMap.put(name, 0);
    }

    List<Integer> xList = new ArrayList<>();
    List<Double> yList = new ArrayList<>();

    for(Map.Entry<Integer, Double> entry : hp.entrySet()) {
      xList.add(entry.getKey());
      yList.add(entry.getValue());
    }

    xMap.put(name, xList);
    yMap.put(name, yList);

    this.displayedItems.put(name, hp);
  }

  private void addStrategySimulationToDisplayedItems(String simulationName) {

    Map<Integer, Double> hp = historicalPricesUsingStrategy(simulationName);

    if(curveUpdateMap.containsKey(simulationName)) {
      curveUpdateMap.put(simulationName, curveUpdateMap.get(simulationName)+1);
    }else{
      curveUpdateMap.put(simulationName, 0);
    }

    List<Integer> xList = new ArrayList<>();
    List<Double> yList = new ArrayList<>();

    for(Map.Entry<Integer, Double> entry : hp.entrySet()) {
      xList.add(entry.getKey());
      yList.add(entry.getValue());
    }

    xMap.put(simulationName, xList);
    yMap.put(simulationName, yList);

    this.displayedItems.put(simulationName, hp);
  }

  /**
   * get current displayed items as a Map.
   *
   * @return current items to be displayed.
   */
  public Map<String, Map<Integer, Double>> getDisplayedItems() {
    return this.displayedItems;
  }

  /**
   * remove a specific plot from displayed items.
   *
   * @param itemName name of plot.
   */
  public void removeDisplayedItem(String itemName) {

    HashSet<String> names = new HashSet<>(this.displayedItems.keySet());

    for (String each : names) {
      String[] strs = each.split("-");
      if (each.equals(itemName) || strs[0].equals(itemName)) {
        this.displayedItems.remove(each);
        this.curveUpdateMap.remove(each);
        this.yMap.remove(each);
        this.xMap.remove(each);
      }
    }
  }

  @Override
  public void removeMovingAverage(String symbol, int days) {
    String movingAverageName = symbol+"-MA-"+days;
    this.maUpdateMap.remove(movingAverageName);
    this.xMap.remove(movingAverageName);
    this.yMap.remove(movingAverageName);
  }

  /**
   * Add moving average plot to displayed items.
   *
   * @param name of basket or stock symbol.
   * @param x    number of days of moving average.
   */
  public void addMovingAverageForExistingDisplayedItems(String name, int x) {

    int fromDate = 0;
    int toDate = 0;

    if (displayedItems.get(name) instanceof TreeMap) {
      fromDate = new TreeMap<>(displayedItems.get(name)).firstKey();
      toDate = new TreeMap<>(displayedItems.get(name)).lastKey();
    }

    Map<Integer, Double> result;
    List<Integer> xValues = new ArrayList<>();
    List<Double> yValues = new ArrayList<>();


    if (baskets.containsKey(name)) {
      result = getHistoricalXDayMovingAverage(
              baskets.get(name), basketSetDates.get(name), fromDate, toDate, x);
    } else {
      result = getHistoricalXDayMovingAverage(name, fromDate, toDate, x);
    }

    for(Map.Entry<Integer, Double> entry : result.entrySet()) {
      xValues.add(entry.getKey());
      yValues.add(entry.getValue());
    }
    String maSymbol = name+"-MA-"+x;

    if(!maUpdateMap.containsKey(maSymbol)) {
      maUpdateMap.put(maSymbol, 0);
    }else{
      maUpdateMap.put(maSymbol, maUpdateMap.get(maSymbol)+1);
    }

    displayedItems.put(maSymbol, result);
    xMap.put(maSymbol, xValues);
    yMap.put(maSymbol, yValues);
  }

  /**
   * This method is called if automatic rebalancing strategy is chosen to execute, each strategy is
   * specific to one basket, and strategy information should be consumed as input, then the strategy
   * in data field is initialized according to the information in this call,
   * **NOTE**
   * 1) the strategy is assumed to be executed on the creation date of the basket;
   * 2) the proportion is determined by the initial price and shares of stock in the basket, instead
   * of user input.
   *
   * @param basketName name of basket of interest.
   * @param invest     total amount to invest.
   * @param period     number of days as period of rebalancing.
   * @param endDate    ending date to determine total profit.
   */
  public void generateAutoRebalanceStrategy(String simulationName, String basketName, double invest, int period,
                                            Object endDate) {

    this.strategyMap.put(simulationName, new AutoRebalanceStrategy(baskets.get(basketName),
            basketSetDates.get(basketName), invest, period, endDate));
    addStrategySimulationToDisplayedItems(simulationName);

  }

  /**
   * get the total profit made by executing current strategy (no matter which strategy).
   *
   * @return total profit made.
   */
  public double simulatingProfit(String simulationName) {
    return this.strategyMap.get(simulationName).totalProfit();
  }

  /**
   * get the historical prices data of basket when executing the current strategy.
   *
   * @return historical prices data of basket as a Map.
   */
  public Map<Integer, Double> historicalPricesUsingStrategy(String simulationName) {
    return this.strategyMap.get(simulationName).basketHistoricalPrice();
  }

  /**
   * get the price record of a single stock on a certain day, date must be a business day.
   *
   * @param stockSymbol a given string which represents a stock symbol
   * @param date        specific date
   * @return price record for a single stock
   */
  @Override
  public double findStockPriceOnCertainDay(String stockSymbol, Object date) {

    PriceRecord price = new Stock(stockSymbol).getPriceOnCertainDay(date);

    if (price == null) {
      throw new IllegalArgumentException("Invalid input of date.");
    }

    return price.getClosePrice();

  }

  /**
   * determines whether a certain day is a buy opportunity day, date must be a business day.
   *
   * @param stockSymbol a given string which represents a stock symbol
   * @param date        specific date
   * @return true or false
   */
  @Override
  public boolean isBuyOpportunityDay(String stockSymbol, Object date) {

    int date2 = 0;

    boolean foundBusinessDay = false;

    Stock stock = new Stock(stockSymbol);

    while (!foundBusinessDay) {
      try {
        date2 = DateParser.dateAddBusinessDays(date, -1);
        stock.getPriceOnCertainDay(date2).getClosePrice();
        foundBusinessDay = true;
      } catch (NullPointerException e) {
        //not return anything.
      }
    }

    double ma250 = stock.xDayMovingAverage(date2, 50);
    double ma2200 = stock.xDayMovingAverage(date2, 200);
    double ma50 = stock.xDayMovingAverage(date, 50);
    double ma200 = stock.xDayMovingAverage(date, 200);

    return foundBusinessDay && ma250 < ma2200 && ma50 >= ma200;
  }

  /**
   * gets the historical prices for a single stock.
   *
   * @param stockSymbol a given string which represents a stock symbol
   * @param fromDate    the start date
   * @param toDate      the end date
   * @return a map represents the information about price of single stock for specific days
   */
  @Override
  public Map<Integer, Double> getHistoricalPrices(
          String stockSymbol, Object fromDate, Object toDate) {

    Map<Integer, PriceRecord> hp = new Stock(stockSymbol).getHistoricalPrices(fromDate, toDate);

    Map<Integer, Double> res = new TreeMap<>();

    for (Map.Entry<Integer, PriceRecord> each : hp.entrySet()) {
      try {
        res.put(each.getKey(), each.getValue().getClosePrice());
      } catch (NullPointerException e) {
        throw new IllegalArgumentException("Invalid input of date range.");
      }
    }

    if (res.size() == 0) {
      throw new IllegalArgumentException("Invalid input of date.");
    }

    return res;

  }

  /**
   * gets the historical prices for a basket of stocks.
   *
   * @param stockShareMapForBasket a map contains information about shares of every stock
   * @param fromDate               the start date
   * @param toDate                 the end date
   * @return a map contains information about price of the basket for specific days
   */

  @Override
  public Map<Integer, Double> getHistoricalPrices(
          Map<String, Integer> stockShareMapForBasket,
          Object setUpDate, Object fromDate, Object toDate) {

    Map<Integer, PriceRecord> hp
            = new Basket(stockShareMapForBasket, setUpDate).getHistoricalPrices(fromDate, toDate);

    Map<Integer, Double> res = new TreeMap<>();

    for (Map.Entry<Integer, PriceRecord> each : hp.entrySet()) {
      try {
        res.put(each.getKey(), each.getValue().getClosePrice());
      } catch (NullPointerException e) {
        throw new IllegalArgumentException("Invalid input of date range.");
      }
    }

    if (res.size() == 0) {
      throw new IllegalArgumentException("Invalid input of date.");
    }

    return res;

  }

  /**
   * get the price record of basket of stocks for a certain day, date must be a business day.
   *
   * @param stockShareMapForBasket a map contains information about shares of every stock
   * @param date                   specific date
   * @return the desired price
   */
  @Override
  public double findBasketPriceOnCertainDay(
          Map<String, Integer> stockShareMapForBasket, Object setUpDate, Object date) {

    PriceRecord price = new Basket(stockShareMapForBasket, setUpDate).getPriceOnCertainDay(date);

    if (price == null) {
      throw new IllegalArgumentException("Invalid input of date.");
    }

    return price.getClosePrice();

  }

  /**
   * finds out the price changing trend for a single stock during given date range.
   *
   * @param stockSymbol a given string which represents a stock symbol
   * @param fromDate    the start date
   * @param toDate      the end date
   * @return situations for trend of stock prices
   */
  @Override
  public TrendType findTrend(String stockSymbol, Object fromDate, Object toDate) {

    return new TrendHandler().analyzeTrend(this.getHistoricalPrices(stockSymbol, fromDate, toDate));
  }

  /**
   * finds out the price changeing trend for a basket of stocks.
   *
   * @param stockShareMapForBasket a map contains information about shares of every stock
   * @param fromDate               the start date
   * @param toDate                 the end date
   * @return situations for trend of stock prices
   */
  @Override
  public TrendType findTrend(
          Map<String, Integer> stockShareMapForBasket,
          Object setUpDate, Object fromDate, Object toDate) {

    return new TrendHandler().analyzeTrend(this.getHistoricalPrices(
            stockShareMapForBasket, setUpDate, fromDate, toDate));

  }

  /**
   * gets the historical Xday moving average trend for a specific stock.
   *
   * @param name     name of the stock
   * @param fromDate the start date
   * @param toDate   the end date
   * @param x        number of days
   * @return the historical Xday moving average trend for a specific stock
   */
  public Map<Integer, Double> getHistoricalXDayMovingAverage(
          String name, Object fromDate, Object toDate, int x) {

    return new Stock(name).getHistoricalXDayMovingAverage(fromDate, toDate, x);

  }

  /**
   * gets the historical Xday moving average trend for a specific basket.
   *
   * @param stockShareMapForBasket share map for the basket
   * @param setUpDate              set up the adding date for a specific stock
   * @param fromDate               the start date
   * @param toDate                 the end date
   * @param x                      number of days
   * @return the historical Xday moving average trend for a specific basket
   */
  public Map<Integer, Double> getHistoricalXDayMovingAverage(
          Map<String, Integer> stockShareMapForBasket,
          Object setUpDate, Object fromDate, Object toDate, int x) {

    return new Basket(
            stockShareMapForBasket, setUpDate).getHistoricalXDayMovingAverage(fromDate, toDate, x);

  }

}
