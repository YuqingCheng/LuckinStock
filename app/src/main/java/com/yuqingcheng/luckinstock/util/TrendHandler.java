package com.yuqingcheng.luckinstock.util;

import com.yuqingcheng.luckinstock.util.TrendType;

import java.util.Map;
import java.util.TreeMap;
import java.util.Arrays;

/**
 * this class provides some necessary helper methods for trend method.
 */
public class TrendHandler {

  private final double STRONG_DECLINE_LIMIT = -0.15;

  private final double DECLINE_LIMIT = -0.05;

  private final double INCLINE_LIMIT = 0.05;

  private final double STRONG_INCLINE_LIMIT = 0.15;

  /**
   * decides the trend given a historical prices.
   *
   * @param historicalPrice a map represents the information about price of single stock for
   *                        specific days
   * @return trend type:STRONG_DECLINE, DECLINE, FLAT, INCLINE, STRONG_INCLINE
   */
  public TrendType analyzeTrend(Map<Integer, Double> historicalPrice) {

    double diff = 0;

    if (historicalPrice instanceof TreeMap) {
      double a = historicalPrice.get(((TreeMap) historicalPrice).lastKey());
      double b = historicalPrice.get(((TreeMap) historicalPrice).firstKey());
      diff = (a - b) / b;
    } else {
      double[] keys = new double[historicalPrice.size()];
      Arrays.sort(keys);
      double a = historicalPrice.get(keys[keys.length - 1]);
      double b = historicalPrice.get(keys[0]);
      diff = (a - b) / b;
    }

    if (diff < STRONG_DECLINE_LIMIT) {
      return TrendType.STRONG_DECLINE;
    } else if (diff < DECLINE_LIMIT) {
      return TrendType.DECLINE;
    } else if (diff < INCLINE_LIMIT) {
      return TrendType.FLAT;
    } else if (diff < STRONG_INCLINE_LIMIT) {
      return TrendType.INCLINE;
    } else {
      return TrendType.STRONG_INCLINE;
    }

  }

}
