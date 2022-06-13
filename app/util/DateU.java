package util;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import play.Logger;

public class DateU {

  public static class TimeType {

    public static final int  FROM = 0;
    public static final int  TO   = 1;

  }

  /**
   * 分数の差分を取得します
   * @param from
   * @param to
   * @return 経過日数
   */
  public static long GetDiffMin(Date from, Date to) {

    long diffDay = (to.getTime() - from.getTime()) / 1000;

    return diffDay;

  }

  /**
   * 日数の差分（期間）を取得します
   * @param from
   * @param to
   * @return 経過日数
   */
  public static java.sql.Date GetDateBase(java.sql.Date pBase, java.sql.Date from, java.sql.Date to) {

    Calendar cBase = Calendar.getInstance();
    Calendar cStart = Calendar.getInstance();
    Calendar cEnd = Calendar.getInstance();

    cStart.setTime(from);
    cStart.set(Calendar.YEAR, 2000);
    cEnd.setTime(to);
    cEnd.set(Calendar.YEAR, 2000);

    long diffDay = Math.abs(cEnd.getTimeInMillis() - cStart.getTimeInMillis()) / (1000 * 60 * 60 * 24);

    cBase.setTime(pBase);
    cBase.add(Calendar.DATE, (int)diffDay);

    return new java.sql.Date(cBase.getTimeInMillis());

  }
  /**
   * 日数の差分（期間）を取得します
   * @param from
   * @param to
   * @return 経過日数
   */
  public static long GetDiffDateKikan(Date from, Date to) {

    Calendar cStart = Calendar.getInstance();
    Calendar cEnd = Calendar.getInstance();

    cStart.setTime(from);
    cStart.set(Calendar.YEAR, 2000);
    cEnd.setTime(to);
    cEnd.set(Calendar.YEAR, 2000);

    long diffDay = Math.abs(cEnd.getTimeInMillis() - cStart.getTimeInMillis()) / (1000 * 60 * 60 * 24);

    return diffDay;

  }
	/**
	 * 日数の差分を取得します
	 * @param from
	 * @param to
	 * @return 経過日数
	 */
	public static long GetDiffDate(Date from, Date to) {

		long diffDay = (to.getTime() - from.getTime()) / (1000 * 60 * 60 * 24);

		return diffDay;

	}

	public static java.sql.Date GetNullDate() {

		Calendar nullCalender = Calendar.getInstance();
		nullCalender.set(1900, 0, 1, 0, 0, 0);
		java.sql.Date date = new java.sql.Date(nullCalender.getTime().getTime());

		return date;

	}
  public static java.sql.Timestamp GetNullTimeStamp() {

    Calendar nullCalender = Calendar.getInstance();
    nullCalender.set(1900, 0, 1, 0, 0, 0);
    java.sql.Timestamp date = new java.sql.Timestamp(nullCalender.getTime().getTime());

    return date;

  }
  public static java.sql.Timestamp getSystemTimeStamp() {

    Calendar nullCalender = Calendar.getInstance();
    java.sql.Timestamp date = new java.sql.Timestamp(nullCalender.getTime().getTime());

    return date;

  }
  /**
   * 渡されたカレンダーをタイムスタンプに変換します
   * @param pCal
   * @return
   */
  public static java.sql.Timestamp convTimeStamp(Calendar pCal) {

    java.sql.Timestamp date = new java.sql.Timestamp(pCal.getTime().getTime());

    return date;

  }

  /**
   * 渡されたタイプスタンプの指定項目を加減算します
   * @param pTs
   * @param pField
   * @param pValue
   */
  public static void addCalendar(java.sql.Timestamp pTs, int pField, int pValue) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(pTs.getTime());
    DateU.putTimeLog(new java.sql.Timestamp(cal.getTimeInMillis()));
    cal.add(pField, pValue);
    DateU.putTimeLog(new java.sql.Timestamp(cal.getTimeInMillis()));
    pTs.setTime(cal.getTimeInMillis());
    DateU.putTimeLog(pTs);
  }

  /**
   * 時刻をログに出力します
   * @param pTs
   */
  public static void putTimeLog(java.sql.Timestamp pTs) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    Logger.info("[TIME LOG] {} ", sdf.format(pTs.getTime()));
  }

	/**
	 * カレンダーの時刻を指定された形式に設定します
	 * @param pDt
	 * @param pType
	 */
	public static void setTime(Calendar pDt, int pType) {

	  if (pType == TimeType.FROM) {
	    pDt.set(Calendar.HOUR, 0);
      pDt.set(Calendar.MINUTE, 0);
      pDt.set(Calendar.SECOND, 0);
      pDt.set(Calendar.MILLISECOND, 0);
	  }
	  else {
      pDt.set(Calendar.HOUR, 23);
      pDt.set(Calendar.MINUTE, 59);
      pDt.set(Calendar.SECOND, 59);
      pDt.set(Calendar.MILLISECOND, 999);
	  }

	}

	public static String getDayOfWeek(Calendar cal) {
	  String datWeek = "";
	  switch (cal.get(Calendar.DAY_OF_WEEK)) {
    case Calendar.SUNDAY:     // Calendar.SUNDAY:1 （値。意味はない）
        datWeek = "Sun";
        break;
    case Calendar.MONDAY:     // Calendar.MONDAY:2
      datWeek = "Mon";
        break;
    case Calendar.TUESDAY:    // Calendar.TUESDAY:3
      datWeek = "Tue";
        break;
    case Calendar.WEDNESDAY:  // Calendar.WEDNESDAY:4
      datWeek = "Wed";
        break;
    case Calendar.THURSDAY:   // Calendar.THURSDAY:5
      datWeek = "Thu";
        break;
    case Calendar.FRIDAY:     // Calendar.FRIDAY:6
      datWeek = "Fri";
        break;
    case Calendar.SATURDAY:   // Calendar.SATURDAY:7
      datWeek = "Sat";
        break;
	  }
	  return datWeek;
	}
	public static void main(String[] args) {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    System.out.println(sdf.format(new java.sql.Timestamp(cal.getTimeInMillis())));
    cal.add(Calendar.MILLISECOND, -100);
    System.out.println(sdf.format(new java.sql.Timestamp(cal.getTimeInMillis())));
	}
}
