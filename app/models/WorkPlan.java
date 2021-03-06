package models;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;
import util.DateU;

@Entity
/**
 * 【AGRYEEL】作業計画情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkPlan extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1443372075535666265L;
    /**
     * 作業計画ID
     */
    public double workPlanId;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 畝ID
     */
    public double hillId;
    /**
     * 条ID
     */
    public double lineId;
    /**
     * 株ID
     */
    public double stockId;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * 作業時間
     */
    public int workTime;
    /**
     * 収穫量
     */
    public double shukakuRyo;
    /**
     * 詳細情報設定種別
     */
    public short detailSettingKind;
    /**
     * コンビID
     */
    public double combiId;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * アタッチメントID
     */
    public double attachmentId;
    /**
     * 品種ID
     */
    public String hinsyuId;
    /**
     * ベルトID
     */
    public double beltoId;
    /**
     * 株間
     */
    public double kabuma;
    /**
     * 条間
     */
    public double joukan;
    /**
     * 条数
     */
    public double jousu;
    /**
     * 深さ
     */
    public double hukasa;
    /**
     * 潅水部分
     */
    public int kansuiPart;
    /**
     * 潅水間隔
     */
    public double kansuiSpace;
    /**
     * 潅水方法
     */
    public int kansuiMethod;
    /**
     * 潅水量
     */
    public double kansuiRyo;
    /**
     * 収穫規格荷姿
     */
    public int syukakuNisugata;
    /**
     * 収穫規格質
     */
    public int syukakuSitsu;
    /**
     * 収穫規格サイズ
     */
    public int syukakuSize;
    /**
     * 区画状況照会反映フラグ
     */
    public int kukakuStatusUpdate;
    /**
     * 元帳照会反映フラグ
     */
    public int motochoUpdate;
    /**
     * 作業備考
     */
    public String workRemark;
    /**
     * 作業開始時間
     */
    public Timestamp workStartTime;
    /**
     * 作業終了時間
     */
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
     * 使用マルチ
     */
    public double useMulti;
    /**
     * 列数
     */
    public double retusu;
    /**
     * 使用苗枚数
     */
    public int naemaisu;
    /**
     * 使用穴数
     */
    public int useHole;
    /**
     * 枚数
     */
    public int maisu;
    /**
     * 使用培土
     */
    public double useBaido;
    /**
     * 剪定高
     */
    public double senteiHeight;
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
    /**
     * 仕立本数
     */
    public double shitateHonsu;
    /**
     * 日長
     */
    public double nicho;
    /**
     * 廃棄量
     */
    public double haikiRyo;

    public static Finder<Long, WorkPlan> find = new Finder<Long, WorkPlan>(Long.class, WorkPlan.class);

    /**
     * 作業計画を全て取得する
     * @param kukakuId
     * @return
     */
    public static List<WorkPlan> getWorkPlan(double kukakuId) {

    	List<WorkPlan> aryWorkPlan = WorkPlan.find.where().eq("kukaku_id", kukakuId).orderBy("work_date asc, work_id asc").findList();

    	return aryWorkPlan;

    }

    /**
     * 対象作業の作業計画を取得する
     * @param workId
     * @param kukakuId
     * @return
     */
    public static List<WorkPlan> getWorkPlanOfWork(double workId, double kukakuId) {

    	List<WorkPlan> aryWorkPlan = WorkPlan.find.where().eq("work_id", workId).eq("kukaku_id", kukakuId).orderBy("work_date").findList();

    	return aryWorkPlan;

    }

    /**
     * 対象作業の作業計画を取得する
     * @param workId
     * @param kukakuId
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<WorkPlan> getWorkPlanOfWork(double workId, double kukakuId, Date startDate,Date endDate) {

      Calendar cStart = Calendar.getInstance();
      Calendar cEnd   = Calendar.getInstance();
      cStart.setTimeInMillis(startDate.getTime());
      cEnd.setTimeInMillis(endDate.getTime());

      DateU.setTime(cStart, DateU.TimeType.FROM);
      DateU.setTime(cEnd, DateU.TimeType.FROM);

    	List<WorkPlan> aryWorkPlan = WorkPlan.find.where().eq("work_id", workId).eq("kukaku_id", kukakuId).between("work_date", new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis())).orderBy("work_date").findList();

    	return aryWorkPlan;

    }

    /**
     * 対象作業の作業計画を取得する
     * @param aryWorkId
     * @param kukakuId
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<WorkPlan> getWorkPlanOfWork(List<Integer> aryWorkId, double kukakuId, Date startDate,Date endDate) {

      Calendar cStart = Calendar.getInstance();
      Calendar cEnd   = Calendar.getInstance();
      cStart.setTimeInMillis(startDate.getTime());
      cEnd.setTimeInMillis(endDate.getTime());

      DateU.setTime(cStart, DateU.TimeType.FROM);
      DateU.setTime(cEnd, DateU.TimeType.FROM);

    	List<WorkPlan> aryWorkPlan = WorkPlan.find.where().eq("kukaku_id", kukakuId).between("work_date", new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis())).in("work_id", aryWorkId).orderBy("work_date").findList();

    	return aryWorkPlan;

    }

    /**
     * 対象作業の作業計画を取得する
     * @param kukakuId
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<WorkPlan> getWorkPlanOfWork(double kukakuId, Date startDate,Date endDate) {

      Calendar cStart = Calendar.getInstance();
      Calendar cEnd   = Calendar.getInstance();
      cStart.setTimeInMillis(startDate.getTime());
      cEnd.setTimeInMillis(endDate.getTime());

      DateU.setTime(cStart, DateU.TimeType.FROM);
      DateU.setTime(cEnd, DateU.TimeType.FROM);

    	List<WorkPlan> aryWorkPlan = WorkPlan.find.where().eq("kukaku_id", kukakuId).between("work_date", new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis())).orderBy("work_date").findList();

    	return aryWorkPlan;

    }

    /**
     * 作業記録IDから作業計画を取得する
     * @param workPlanId
     * @return
     */
    public static WorkPlan getWorkPlanById(double workPlanId) {
      return WorkPlan.find.where().eq("work_plan_id", workPlanId).findUnique();
    }
}
