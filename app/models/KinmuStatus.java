package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class KinmuStatus extends Model {

    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 勤務状態
     */
    public short kinmuStatus;
    /**
     * メッセージ
     */
    public String message;
    /**
     * 年
     */
    public short year;
    /**
     * 年間勤務時間
     */
    public double kinmuYear;
    /**
     * 月間勤務時間1
     */
    public double kinmuMonth1;
    /**
     * 月間勤務時間2
     */
    public double kinmuMonth2;
    /**
     * 月間勤務時間3
     */
    public double kinmuMonth3;
    /**
     * 月間勤務時間4
     */
    public double kinmuMonth4;
    /**
     * 月間勤務時間5
     */
    public double kinmuMonth5;
    /**
     * 月間勤務時間6
     */
    public double kinmuMonth6;
    /**
     * 月間勤務時間7
     */
    public double kinmuMonth7;
    /**
     * 月間勤務時間8
     */
    public double kinmuMonth8;
    /**
     * 月間勤務時間9
     */
    public double kinmuMonth9;
    /**
     * 月間勤務時間10
     */
    public double kinmuMonth10;
    /**
     * 月間勤務時間11
     */
    public double kinmuMonth11;
    /**
     * 月間勤務時間12
     */
    public double kinmuMonth12;
    /**
     * 生産者ID
     */
    public double farmId;


    public static Finder<Long, KinmuStatus> find = new Finder<Long, KinmuStatus>(Long.class, KinmuStatus.class);

    public static int updateStatus(double farmId,String accountId, short year, short month, short day) {
      boolean update = true;
      int result = 0;
      KinmuStatus ks = KinmuStatus.find.where().eq("account_id", accountId).eq("year", year).findUnique();
      if (ks == null) {
        ks            = new KinmuStatus();
        ks.accountId  = accountId;
        ks.year       = year;
        ks.farmId     = farmId;
        update        = false;
      }
      List<KinmuDiary> kds = KinmuDiary.find.where().eq("account_id", accountId).eq("year", year).eq("month", month).eq("day", day).orderBy("kinmu_id desc").findList();
      for (KinmuDiary kd : kds) {
        if (kd.kinmuFlag == 4) {  /* 休憩終了の場合 */
          ks.kinmuStatus  = 1;    /* 状態を出勤中に戻す */
        }
        else {
          ks.kinmuStatus  = kd.kinmuFlag;
        }
        ks.message      = kd.message;
        break;
      }
      if (update) {
        ks.update();
      }
      else {
        ks.save();
      }
      return result;
    }
}
