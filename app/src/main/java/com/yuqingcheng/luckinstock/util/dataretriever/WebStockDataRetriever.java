package com.yuqingcheng.luckinstock.util.dataretriever;

import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.Scanner;

import com.yuqingcheng.luckinstock.util.PriceRecord;

import java.util.TreeMap;

/**
 * This class represents a stock retriever module. It is a singleton, and so to
 * get the one (and only) object call getStockDataRetriever().
 */
public class WebStockDataRetriever implements StockDataRetriever {

  /**
   * default construct.
   */

  public WebStockDataRetriever() {
    // iniatialz the constructor.
  }

  /**
   * gets the current price of a single stock.
   *
   * @param stockSymbol a string that represents a stock
   * @return the price of a given stock
   * @throws Exception whether the stockSymbol is valid or not
   */

  public double getCurrentPrice(String stockSymbol) throws Exception {
    URL url = new URL("https://download.finance.yahoo.com/d/quotes.csv?"
            +
            "s=" + stockSymbol + "&f=l1&e=.csv");

    String output = new Scanner(url.openStream()).next();

    return Double.parseDouble(output);
  }

  /**
   * gets the name of a specific stock.
   *
   * @param stockSymbol a string that represents a stock
   * @return stock's name
   * @throws Exception whether the stockSymbol is valid or not
   */
  public String getName(String stockSymbol) throws Exception {

    String output = "";
    try {
      URL url = new URL("https://download.finance.yahoo.com/d/quotes.csv?"
              +
              "s=" + stockSymbol + "&f=n&e=.csv");
      output = new Scanner(url.openStream()).nextLine();
      output = output.substring(1, output.length()-1);
      System.out.println(output);
    }catch(Exception e) {
      throw new IllegalArgumentException("Invalid url format or scanner reading error.");
    }

    return output;
  }

  /**
   * gets the historical prices of a single stock.
   *
   * @param stockSymbol a string that represents a stock
   * @param fromDate    start date
   * @param fromMonth   start month
   * @param fromYear    start year
   * @param toDate      end date
   * @param toMonth     end month
   * @param toYear      end year
   * @return a map represents the information about price of single stock for specific days
   */
  public Map<Integer, PriceRecord> getHistoricalPrices(
          String stockSymbol,
          int fromDate,
          int fromMonth,
          int fromYear,
          int toDate,
          int toMonth,
          int toYear)
          throws
          Exception {


    URL url = new URL("https://finance.google"
            +
            ".com/finance/historical?output=csv&q=" + stockSymbol + "&startdate="
            + fromMonth + "+" + fromDate + "+" + fromYear
            + "&enddate=" + toMonth + "+" + toDate + "+" + toYear);

    String output = "";
    Map<Integer, PriceRecord> prices = new TreeMap<Integer, PriceRecord>();
    Scanner sc = new Scanner(url.openStream());
    //get first line of labels
    output = sc.next();

    while (sc.hasNext()) {
      output = sc.next();
      String[] data = output.split(",");

      try {

        PriceRecord record = new PriceRecord(
                Double.parseDouble(data[1]),
                Double.parseDouble(data[4]),
                Double.parseDouble(data[3]),
                Double.parseDouble(data[2])
        );
        //date is index 0
        Integer date = getDate(data[0]);
        prices.put(date, record);
      }catch(NumberFormatException e) {
        continue;
      }
    }
    return prices;

  }

  /**
   * a helper method to change a string into a valin integer that represents month.
   *
   * @param month a string that represents month
   * @return an integer that represents month
   */
  private int toMonth(String month) {
    switch (month) {
      case "Jan":
        return 1;
      case "Feb":
        return 2;
      case "Mar":
        return 3;
      case "Apr":
        return 4;
      case "May":
        return 5;
      case "Jun":
        return 6;
      case "Jul":
        return 7;
      case "Aug":
        return 8;
      case "Sep":
        return 9;
      case "Oct":
        return 10;
      case "Nov":
        return 11;
      case "Dec":
        return 12;
      default:
        return -1;
    }
  }

  /**
   * a helper method to change a string into a valin integer that represents date.
   *
   * @param date a string that represents date
   * @return an integer that represents date
   */
  private Integer getDate(String date) {
    String[] splitdate = date.split("-");
    int actualDate = Integer.parseInt(splitdate[0]);
    int actualYear = Integer.parseInt(splitdate[2]);
    int actualMonth = toMonth(splitdate[1]);
    if (actualYear <= Calendar.getInstance().get(Calendar.YEAR) % 100) {
      actualYear = Calendar.getInstance().get(Calendar.YEAR) / 100 * 100 + actualYear;
    } else {
      actualYear = (Calendar.getInstance().get(Calendar.YEAR) / 100 - 1) * 100 + actualYear;
    }
    return (actualYear * 100 + actualMonth) * 100 + actualDate;
  }

}
