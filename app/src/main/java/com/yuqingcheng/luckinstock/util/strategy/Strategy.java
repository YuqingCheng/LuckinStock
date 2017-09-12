package com.yuqingcheng.luckinstock.util.strategy;

import java.util.Map;

/**
 * This interface indicates all the common features strategy classes own.
 */
public interface Strategy {

  /**
   * get the total profit made by executing this strategy.
   *
   * @return total profit made as a double.
   */
  double totalProfit();

  /**
   * get the basket's price data in the form of a map when executing the strategy, in which the key
   * is the date, the value is closing price on this date.
   *
   * @return basket historical price-date data as a Map.
   */
  Map<Integer, Double> basketHistoricalPrice();

}
