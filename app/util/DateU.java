package util;
import java.util.Calendar;
import java.util.Date;

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
}
