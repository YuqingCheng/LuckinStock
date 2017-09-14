package com.yuqingcheng.luckinstock.model.trader;

import com.yuqingcheng.luckinstock.util.TrendType;

import java.util.List;
import java.util.Map;

/**
 * an interface of a stock analyzer.
 */
public interface StockAnalyzer {

  double findStockPriceOnCertainDay(String stockSymbol, Object date);

  boolean basketExists(String basketName);

  boolean displayedItemsContain(String name);

  void addNewBasket(String basketName, Object setDate);

  void addStockOrSharesToBasket(String basketName, Map<String, Integer> shareOfStock)
          throws IllegalArgumentException;

  Map<String, List<Integer>> getXMap();

  Map<String, List<Double>> getYMap();

  Map<String, Integer> getCurveUpdateMap();

  Map<String, Integer> getBasketContent(String basketName) throws IllegalArgumentException;

  void addStockOrBasketHistoricalDataToDisplayedItems(String name, Object fromDate, Object toDate);

  Map<String, Map<Integer, Double>> getDisplayedItems();

  void removeDisplayedItem(String itemName);

  void addMovingAverageForExistingDisplayedItems(String name, int x);

  boolean isBuyOpportunityDay(String stockSymbol, Object date);

  boolean isValidStockSymbol(String stockSymbol);

  boolean isValidDateRange(String fromDate, String toDate);

  String getStockName(String symbol) throws IllegalArgumentException;

  void generateAutoRebalanceStrategy(String basketName, double invest, int period, Object endDate);

  double simulatingProfit();

  Map<Integer, Double> historicalPricesUsingStrategy();

  Map<Integer, Double> getHistoricalPrices(String stockSymbol, Object fromDate, Object toDate);

  Map<Integer, Double> getHistoricalPrices(
          Map<String, Integer> stockShareMapForBasket,
          Object setUpDate, Object fromDate, Object toDate);

  double findBasketPriceOnCertainDay(
          Map<String, Integer> stockShareMapForBasket, Object setUpDate, Object date);


  TrendType findTrend(String stockSymbol, Object fromDate, Object toDate);

  TrendType findTrend(
          Map<String, Integer> stockShareMapForBasket,
          Object setUpDate, Object fromDate, Object toDate);

}
