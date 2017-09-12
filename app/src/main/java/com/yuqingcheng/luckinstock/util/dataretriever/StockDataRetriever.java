package com.yuqingcheng.luckinstock.util.dataretriever;

import com.yuqingcheng.luckinstock.util.PriceRecord;

import java.util.Map;

/**
 * This interface represents all the operations offered by a component that,
 * can be used to get stock data.
 */
public interface StockDataRetriever {

  double getCurrentPrice(String stockSymbol) throws Exception;

  String getName(String stockSymbol) throws Exception;

  Map<Integer, PriceRecord> getHistoricalPrices(
          String stockSymbol,
          int fromDate,
          int fromMonth,
          int fromYear,
          int toDate,
          int toMonth,
          int toYear) throws Exception;


}
