package com.yuqingcheng.luckinstock.util;

/**
 * This class represents the record of a price of a single stock item in a day.
 */
public class PriceRecord {

  private final double open;
  private final double close;
  private final double highest;
  private final double lowest;

  /**
   * constructs a record of prices for a certain stock.
   *
   * @param open    the open price for a certain stock
   * @param close   the close price for a certain stock
   * @param lowest  the loweset price for a certain stock
   * @param highest the highest price for a certain stock
   */
  public PriceRecord(double open, double close, double lowest, double highest) {
    this.open = open;
    this.close = close;
    this.highest = highest;
    this.lowest = lowest;
  }

  /**
   * getters function to get open,close, lowest and highest prices.
   *
   * @return the required prices
   */
  public double getOpenPrice() {
    return open;
  }

  public double getClosePrice() {
    return close;
  }

  public double getLowestDayPrice() {
    return lowest;
  }

  public double getHighestDayPrice() {
    return highest;
  }

  /**
   * adds another stock's pricerecord to this stock.
   *
   * @param other another stock's pricerecord
   * @return the added pricerecord
   */
  public PriceRecord add(PriceRecord other) {
    return new PriceRecord(this.open + other.getOpenPrice(),
            this.close + other.getClosePrice(),
            this.lowest + other.getLowestDayPrice(),
            this.highest + other.getHighestDayPrice());
  }

  /**
   * returns the total price record when there is more than one share.
   *
   * @param multiplyer number of shares
   * @return the desires price record
   */
  public PriceRecord times(int multiplyer) {
    return new PriceRecord(this.open * multiplyer,
            this.close * multiplyer,
            this.lowest * multiplyer,
            this.highest * multiplyer);
  }


}

