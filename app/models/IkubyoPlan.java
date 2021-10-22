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
 * 【AGRYEEL】育苗計画情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class IkubyoPlan extends Model {

    /**
     * 育苗計画ID
     */
    public double ikubyoPlanId;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * 苗No
     */
    public String naeNo;
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
     * 苗状況照会反映フラグ
     */
    public int naeStatusUpdate;
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
     * 苗数量
     */
    public double naeSuryo;
    /**
     * 個数
     */
    public double kosu;
    /**
     * 容器ID
     */
    public double youkiId;
    /**
     * 培土ID
     */
    public double baidoId;
    /**
     * 培土数量
     */
    public double baidoSuryo;
    /**
     * 覆土ID
     */
    public double fukudoId;
    /**
     * 覆土数量
     */
    public double fukudoSuryo;
    /**
     * 剪定高
     */
    public double senteiHeight;
    /**
     * 廃棄量
     */
    public double haikiRyo;

    public static Finder<Long, IkubyoPlan> find = new Finder<Long, IkubyoPlan>(Long.class, IkubyoPlan.class);

    /**
     * 育苗計画を全て取得する
     * @param naeNo
     * @return
     */
    public static List<IkubyoPlan> getIkubyoPlan(String naeNo) {

    	List<IkubyoPlan> aryIkubyoPlan = IkubyoPlan.find.where().eq("nae_no", naeNo).orderBy("work_date asc, work_id asc").findList();

    	return aryIkubyoPlan;

    }

    /**
     * 対象作業の育苗計画を取得する
     * @param workId
     * @param naeNo
     * @return
     */
    public static List<IkubyoPlan> getIkubyoPlanOfWork(double workId, String naeNo) {

    	List<IkubyoPlan> aryIkubyoPlan = IkubyoPlan.find.where().eq("work_id", workId).eq("nae_no", naeNo).orderBy("work_date").findList();

    	return aryIkubyoPlan;

    }

    /**
     * 対象作業の育苗計画を取得する
     * @param workId
     * @param naeNo
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<IkubyoPlan> getIkubyoPlanOfWork(double workId, String naeNo, Date startDate,Date endDate) {

      Calendar cStart = Calendar.getInstance();
      Calendar cEnd   = Calendar.getInstance();
      cStart.setTimeInMillis(startDate.getTime());
      cEnd.setTimeInMillis(endDate.getTime());

      DateU.setTime(cStart, DateU.TimeType.FROM);
      DateU.setTime(cEnd, DateU.TimeType.FROM);

    	List<IkubyoPlan> aryIkubyoPlan = IkubyoPlan.find.where().eq("work_id", workId).eq("nae_no", naeNo).between("work_date", new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis())).orderBy("work_date").findList();

    	return aryIkubyoPlan;

    }

    /**
     * 対象作業の育苗計画を取得する
     * @param aryWorkId
     * @param naeNo
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<IkubyoPlan> getIkubyoPlanOfWork(List<Integer> aryWorkId, String naeNo, Date startDate,Date endDate) {

      Calendar cStart = Calendar.getInstance();
      Calendar cEnd   = Calendar.getInstance();
      cStart.setTimeInMillis(startDate.getTime());
      cEnd.setTimeInMillis(endDate.getTime());

      DateU.setTime(cStart, DateU.TimeType.FROM);
      DateU.setTime(cEnd, DateU.TimeType.FROM);

    	List<IkubyoPlan> aryIkubyoPlan = IkubyoPlan.find.where().eq("nae_no", naeNo).between("work_date", new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis())).in("work_id", aryWorkId).orderBy("work_date").findList();

    	return aryIkubyoPlan;

    }

    /**
     * 対象苗Noの育苗計画を取得する
     * @param naeNo
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<IkubyoPlan> getIkubyoPlanOfWork(String naeNo, Date startDate,Date endDate) {

      Calendar cStart = Calendar.getInstance();
      Calendar cEnd   = Calendar.getInstance();
      cStart.setTimeInMillis(startDate.getTime());
      cEnd.setTimeInMillis(endDate.getTime());

      DateU.setTime(cStart, DateU.TimeType.FROM);
      DateU.setTime(cEnd, DateU.TimeType.FROM);

    	List<IkubyoPlan> aryIkubyoPlan = IkubyoPlan.find.where().eq("nae_no", naeNo).between("work_date", new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis())).orderBy("work_date").findList();

    	return aryIkubyoPlan;

    }

    /**
     * 作業記録IDから育苗計画を取得する
     * @param ikubyoPlanId
     * @return
     */
    public static IkubyoPlan getIkubyoPlanById(double ikubyoPlanId) {
      return IkubyoPlan.find.where().eq("ikubyo_plan_id", ikubyoPlanId).findUnique();
    }
}
