package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import play.Logger;
import play.db.ebean.Model;
import util.ListrU;

import com.avaje.ebean.annotation.CreatedTimestamp;

import consts.AgryeelConst;

@Entity
/**
 * 【AGRYEEL】プランライン情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class PlanLine extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1668968605280219240L;
    /**
     * 作業計画ID
     */
    public double workPlanId;
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
     * プランラインカラー
     */
    public String planLineColor;
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

    public static Finder<Long, PlanLine> find = new Finder<Long, PlanLine>(Long.class, PlanLine.class);

    /**
     * 対象期間のプランラインを取得する
     * @param startDate
     * @param endDate
     * @returnaccountidaccountidaccountid
     */
    public static List<PlanLine> getPlanLineOfRange(double kukakuId, Date startDate,Date endDate) {

    	List<PlanLine> aryPlanLine = PlanLine.find.where().eq("kukaku_id", kukakuId).between("work_date", startDate, endDate).orderBy("work_date desc, work_id desc, update_time desc").findList();

    	return aryPlanLine;

    }
    public static List<PlanLine> getPlanLineOfAccount(String accountid, double farmId, Date startDate,Date endDate) {

      List<PlanLine> aryPlanLine;
      if (accountid.equals(AgryeelConst.SpecialAccount.ALLACOUNT)) {
    	  aryPlanLine = PlanLine.find.where().between("work_date", startDate, endDate).eq("farm_id", farmId).orderBy("work_date desc, update_time desc").findList();
      }
      else {
        String[] accounts = accountid.split(",");
        if (accounts.length > 1) {
          List<String> accountKeys = new ArrayList<String>();
          for (String key : accounts) {
            if ("AgryeelConst.SpecialAccount.ALLACOUNT".equals(key)) {
              key = "";
            }
            accountKeys.add(key);
          }
          aryPlanLine = PlanLine.find.where().in("account_id", accountKeys).eq("farm_id", farmId).between("work_date", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
        else {
          String account = accountid;
          if ("AgryeelConst.SpecialAccount.ALLACOUNT".equals(account)) {
            account = "";
          }
          aryPlanLine = PlanLine.find.where().eq("account_id", accountid).eq("farm_id", farmId).between("work_date", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
      }

      return aryPlanLine;

    }
    public static List<PlanLine> getPlanLineOfSearch(String accountid, String workid, String kukakuid, double farmId, Date startDate,Date endDate) {

      List<PlanLine> aryPlanLine;
      List<String> accountKeys  = ListrU.makeInKeyString(accountid);
      List<String> accountKeys2  = new ArrayList<String>();
      if (accountKeys.size() == 0) {
        List<Account> accounts = models.Account.getAccountOfFarm(farmId);
        for (Account account : accounts) {
          accountKeys.add(account.accountId);
        }
      }
      else {
        for (String key: accountKeys) {
          if (AgryeelConst.SpecialAccount.ALLACOUNT.equals(key)) {
            key = "";
          }
          accountKeys2.add(key);
        }
        for (String key: accountKeys2) {
          Logger.info("ACCOUNT = {}",key);
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
     aryPlanLine = PlanLine.find.where().eq("farm_id", farmId).between("work_date", startDate, endDate).in("account_id", accountKeys2)
                                                       .in("work_id", workKeys).in("kukaku_id", kukakuKeys).orderBy("work_date desc, update_time desc").findList();

      return aryPlanLine;

    }
}
