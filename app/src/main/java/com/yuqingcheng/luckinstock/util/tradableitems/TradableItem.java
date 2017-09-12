package com.yuqingcheng.luckinstock.util.tradableitems;

import java.util.Map;

import com.yuqingcheng.luckinstock.util.PriceRecord;

/**
 * an interface for Basket and Stock class.
 */
public interface TradableItem {

  Map<Integer, PriceRecord> getHistoricalPrices(Object fromDate, Object toDate);

  PriceRecord getPriceOnCertainDay(Object date);

  double xDayMovingAverage(Object date, int x);

  Map<Integer, Double> getHistoricalXDayMovingAverage(Object fromDate, Object toDate, int x);
}
