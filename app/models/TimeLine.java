package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import com.avaje.ebean.Expr;
import com.avaje.ebean.annotation.CreatedTimestamp;
import consts.AgryeelConst;

import play.db.ebean.Model;
import util.ListrU;

@Entity
/**
 * 【AGRYEEL】タイムライン情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class TimeLine extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -8184948097382924533L;
    /**
     * タイムラインID
     */
    public double timeLineId;
    /**
     * 更新日時
     */
    public Timestamp updateTime;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * メッセージ
     */
    public String message;
    /**
     * 作業記録ID
     */
    public double workDiaryId;
    /**
     * タイムラインカラー
     */
    public String timeLineColor;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * 作業名
     */
    public String workName;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 区画名
     */
    public String kukakuName;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 氏名
     */
    public String accountName;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 作業開始時間
     */
    @CreatedTimestamp
    public Timestamp workStartTime;
    /**
     * 作業終了時間
     */
    @CreatedTimestamp
    public Timestamp workEndTime;
    /**
     * 歩数
     */
    public long numberOfSteps;
    /**
     * 距離
     */
    public double distance;
    /**
     * カロリー
     */
    public int calorie;
    /**
     * 心拍数
     */
    public int heartRate;
    /**
     * 作業計画フラグ
     */
    public int workPlanFlag;
    /**
     * 作業計画作成ＵＵＩＤ
     */
    public String workPlanUUID;
    /**
     * 同一日内指示番号
     */
    public long uuidOfDay;

    public static Finder<Long, TimeLine> find = new Finder<Long, TimeLine>(Long.class, TimeLine.class);

    /**
     * 対象期間のタイムラインを取得する
     * @param startDate
     * @param endDate
     * @returnaccountidaccountidaccountid
     */
    public static List<TimeLine> getTimeLineOfFarm(double farmId, Date startDate,Date endDate) {

      List<TimeLine> aryTimeLine = TimeLine.find.where().eq("farm_id", farmId).between("work_date", startDate, endDate).orderBy("work_date desc, work_id desc, update_time desc").findList();

      return aryTimeLine;

    }
    /**
     * 対象期間のタイムラインを取得する
     * @param startDate
     * @param endDate
     * @returnaccountidaccountidaccountid
     */
    public static List<TimeLine> getTimeLineOfRange(double kukakuId, Date startDate,Date endDate) {

    	List<TimeLine> aryTimeLine = TimeLine.find.where().eq("kukaku_id", kukakuId).between("work_date", startDate, endDate).orderBy("work_date desc, work_id desc, update_time desc").findList();

    	return aryTimeLine;

    }
    public static List<TimeLine> getTimeLineOfAccount(String accountid, double farmId, Date startDate,Date endDate) {

      List<TimeLine> aryTimeLine;
      if (accountid.equals(AgryeelConst.SpecialAccount.ALLACOUNT)) {
        aryTimeLine = TimeLine.find.where().between("work_date", startDate, endDate).eq("farm_id", farmId).orderBy("work_date desc, update_time desc").findList();
      }
      else {
        String[] accounts = accountid.split(",");
        if (accounts.length > 1) {
          List<String> accountKeys = new ArrayList<String>();
          for (String key : accounts) {
            accountKeys.add(key);
          }
          aryTimeLine = TimeLine.find.where().disjunction().add(Expr.in("account_id", accountKeys)).add(Expr.eq("account_id", "")).endJunction().eq("farm_id", farmId).between("work_date", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
        else {
          aryTimeLine = TimeLine.find.where().eq("account_id", accountid).between("work_date", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
      }

      return aryTimeLine;

    }
    public static List<TimeLine> getTimeLineOfSearch(String accountid, String workid, String kukakuid, double farmId, Date startDate,Date endDate) {

      List<TimeLine> aryTimeLine;
      List<String> accountKeys  = ListrU.makeInKeyString(accountid);
      if (accountKeys.size() == 0) {
        List<Account> accounts = models.Account.getAccountOfFarm(farmId);
        for (Account account : accounts) {
          accountKeys.add(account.accountId);
        }
      }
      List<Double> workKeys     = ListrU.makeInKeyDouble(workid);
      if (workKeys.size() == 0) {
        List<Work> works = models.Work.getWorkOfBaseFarm(farmId);
        for (Work work : works) {
          workKeys.add(work.workId);
        }
      }
      List<Double> kukakuKeys   = ListrU.makeInKeyDouble(kukakuid);
      if (kukakuKeys.size() == 0) {
        List<Compartment> kukakus = models.Compartment.getCompartmentOfFarm(farmId);
        for (Compartment kukaku : kukakus) {
          kukakuKeys.add(kukaku.kukakuId);
        }
      }
      aryTimeLine = TimeLine.find.where().eq("farm_id", farmId).between("work_date", startDate, endDate).disjunction().add(Expr.in("account_id", accountKeys)).add(Expr.eq("account_id", "")).endJunction()
                                          .in("work_id", workKeys).in("kukaku_id", kukakuKeys).orderBy("work_date desc, update_time desc").findList();

      return aryTimeLine;

    }
}
