package com.yuqingcheng.luckinstock.util;


import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * this class deals with input dates.
 */
public class DateParser {

  /**
   * determines invalid and valid date and changes a input date into Integer.
   *
   * @param date input date
   * @return an integer represents formatted date
   */
  public static int parseDateToInteger(Object date) throws IllegalArgumentException {
    if (date instanceof String) {
      try {
        int dateAsInteger = Integer.parseInt((String) date);
        return dateAsInteger;
      } catch (Exception e) {
        //not do anything
      }
      String[] splitdate = ((String) date).split("/");
      int actualDate = 0;
      int actualYear = 0;
      int actualMonth = 0;
      try {
        actualDate = Integer.parseInt(splitdate[2]);
        try {
          actualMonth = Integer.parseInt(splitdate[1]);
        } catch (Exception e) {
          actualMonth = toMonth(splitdate[1]);
        }
        actualYear = Integer.parseInt(splitdate[0]);
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("Invalid input of date. "
                +
                "String should be in format of yyyy/mm/dd or yyyy/mon/dd");
      }
      if (actualYear <= Calendar.getInstance().get(Calendar.YEAR) % 100) {
        actualYear = Calendar.getInstance().get(Calendar.YEAR) / 100 * 100 + actualYear;
      } else if (actualYear < 100) {
        actualYear = (Calendar.getInstance().get(Calendar.YEAR) / 100 - 1) * 100 + actualYear;
      }
      if (actualYear > Calendar.getInstance().get(Calendar.YEAR) || actualMonth <= 0
              ||
              actualMonth > 12 || actualDate <= 0 || actualDate > 31) {
        throw new IllegalArgumentException("Invalid input of date.");
      }
      return (actualYear * 100 + actualMonth) * 100 + actualDate;
    }
    if (date instanceof Integer) {
      return (Integer) date;
    }
    throw new IllegalArgumentException("Invalid input of date.");
  }

  /**
   * helper function for parseDateToInteger.
   *
   * @param month a string represents a month
   * @return an integer represents a month
   */
  private static int toMonth(String month) {
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
   * finds a date that's certain business days after a specific date.
   *
   * @param date a given date.
   * @param n    the number of a certain business being added
   * @return an integer represents the resulting date
   */
  public static int dateAddBusinessDays(Object date, int n) {
    int dateAsInteger = parseDateToInteger(date);
    int dayOfMonth = dateAsInteger % 100;
    dateAsInteger /= 100;
    int month = dateAsInteger % 100;
    dateAsInteger /= 100;
    int year = dateAsInteger;
    GregorianCalendar calendar = new GregorianCalendar(year, month - 1, dayOfMonth);
    int start = 0;
    int m = (n > 0) ? 1 : -1;
    while (start != n) {
      calendar.add(calendar.DAY_OF_WEEK, m);
      int weekday = calendar.get(Calendar.DAY_OF_WEEK);
      if (weekday >= 2 && weekday <= 6) {
        start += m;
      }
    }
    year = calendar.get(Calendar.YEAR);
    month = calendar.get(Calendar.MONTH) + 1;
    dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

    return (year * 100 + month) * 100 + dayOfMonth;

  }


}
