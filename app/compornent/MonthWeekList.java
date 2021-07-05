package compornent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import play.Logger;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 週別栽培計画状況リスト
 * @author kimura
 *
 */
public class MonthWeekList {
  /**
   * 週別栽培計画状況
   * @author kimura
   *
   */
  public class MonthWeek {
    public int month  = 0;
    public int week   = 0;
    public int mode   = 0;
    public int moder  = 0;
  }
  //-------------------------------------------------------------------------
  //
  //-------------------------------------------------------------------------
  List<MonthWeek> monthWList    = new ArrayList<MonthWeek>();
  List<Integer>   weekCountList = new ArrayList<Integer>();
  /**
   * コンストラクタ
   * @param year
   */
  public MonthWeekList(int year) {
    monthWList = new ArrayList<MonthWeek>();
    weekCountList = new ArrayList<Integer>();
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    for (int month = 0; month < 12; month++) {
      cal.set(Calendar.MONTH, month);
      int weekCount = cal.getActualMaximum(Calendar.WEEK_OF_MONTH);
      weekCountList.add(weekCount);
      for (int week=0; week<weekCount; week++) {
        MonthWeek mw = new MonthWeek();
        mw.month  = month + 1;
        mw.week   = week + 1;
        monthWList.add(mw);
      }
    }
  }
  /**
   * 対象となる栽培計画状況を取得する
   * @param month
   * @param week
   * @return
   */
  public MonthWeek get(int month, int week) {
    MonthWeek mw = null;
    for (MonthWeek data : this.monthWList) {
      if (data.month == month && data.week == week) {
        mw = data;
        break;
      }
    }
    return mw;
  }
  /**
   * 栽培計画状況を更新する
   * @param month
   * @param week
   * @param mode
   */
  public void update(int month, int week, int mode) {
    MonthWeek mw = get(month, week);
    if (mw != null) {
      if (mw.mode == 0) {
        switch (mode) {
        case 1: //作付
          mw.mode  = 1;
          mw.moder = 1;
          break;

        case 2: //管理
          mw.mode  = 3;
          mw.moder = 3;
          break;

        case 3: //収穫
          mw.mode  = 5;
          mw.moder = 5;
          break;
        default:
          break;
        }
      }
      else {
        switch (mode) {
        case 1: //作付
          if (mw.mode == 0 || mw.mode == 1) {
            mw.moder = 1;
          }
          break;

        case 2: //管理
          if (mw.mode == 1 || mw.mode == 3) {
            mw.mode  = 3;
            mw.moder = 3;
          }
          break;

        case 3: //収穫
          if (mw.mode == 3 || mw.mode == 5) {
            mw.mode  = 5;
            mw.moder = 5;
          }
          break;
        default:
          break;
        }
      }
    }
  }
  /**
   * 週別栽培計画状況をJSON化する
   * @return
   */
  public ObjectNode outToJson() {
    ObjectNode monthWeekListJson = Json.newObject();

    int idx   = 0;
    int oMode = 0;
    boolean shukaku = false;
    for (MonthWeek mw : monthWList) {
      ObjectNode weekJson = Json.newObject();
      weekJson.put("month", mw.month);
      weekJson.put("week" , mw.week);
      Logger.info("[OMODE]{} [MODE]{} [RMODE]{}", oMode, mw.mode, mw.moder);
      if (oMode == 1 && mw.mode == 0) {
        if (!shukaku) {
          weekJson.put("mode"   , 1);
          weekJson.put("moder"  , 1);
          oMode = 1;
        }
      }
      else if (oMode == 3 && mw.mode == 0) {
        if (!shukaku) {
          weekJson.put("mode"   , 3);
          weekJson.put("moder"  , 3);
          oMode = 3;
        }
      }
      else {
        weekJson.put("mode"   , mw.mode);
        weekJson.put("moder"  , mw.moder);
        oMode = mw.mode;
      }
      if (mw.mode == 5 || mw.mode == 6) {
        shukaku = true;
      }
      idx++;
      monthWeekListJson.put(String.valueOf(idx), weekJson);
    }

    return monthWeekListJson;
  }
}
