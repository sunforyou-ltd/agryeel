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
 * 【AGRYEEL】育苗ライン情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class IkubyoLine extends Model {

    /**
     * 育苗記録ID
     */
    public double ikubyoDiaryId;
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
<<<<<<< HEAD
     * 作業記録ID
     */
    public double workDiaryId;
    /**
=======
>>>>>>> e747d9c9b47c3d59e92c6b368bbb246cc6564120
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
     * 苗No
     */
    public String naeNo;
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

    public static Finder<Long, IkubyoLine> find = new Finder<Long, IkubyoLine>(Long.class, IkubyoLine.class);

    /**
     * 対象期間の育苗ラインを取得する
     * @param farmId
     * @param startDate
     * @param endDate
     * @return 育苗ライン情報
     */
    public static List<IkubyoLine> getIkubyoLineOfFarm(double farmId, Date startDate,Date endDate) {

      List<IkubyoLine> aryIkubyoLine = IkubyoLine.find.where().eq("farm_id", farmId).between("work_date", startDate, endDate).orderBy("work_date desc, work_id desc, update_time desc").findList();

      return aryIkubyoLine;

    }
    /**
     * 対象期間の育苗ラインを取得する
     * @param naeNo
     * @param startDate
     * @param endDate
     * @return 育苗ライン情報
     */
    public static List<IkubyoLine> getIkubyoLineOfRange(String naeNo, Date startDate,Date endDate) {

    	List<IkubyoLine> aryIkubyoLine = IkubyoLine.find.where().eq("nae_no", naeNo).between("work_date", startDate, endDate).orderBy("work_date desc, work_id desc, update_time desc").findList();

    	return aryIkubyoLine;

    }
    public static List<IkubyoLine> getIkubyoLineOfAccount(String accountid, double farmId, Date startDate,Date endDate) {

      List<IkubyoLine> aryIkubyoLine;
      if (accountid.equals(AgryeelConst.SpecialAccount.ALLACOUNT)) {
        aryIkubyoLine = IkubyoLine.find.where().between("work_date", startDate, endDate).eq("farm_id", farmId).orderBy("work_date desc, update_time desc").findList();
      }
      else {
        String[] accounts = accountid.split(",");
        if (accounts.length > 1) {
          List<String> accountKeys = new ArrayList<String>();
          for (String key : accounts) {
            accountKeys.add(key);
          }
          aryIkubyoLine = IkubyoLine.find.where().disjunction().add(Expr.in("account_id", accountKeys)).add(Expr.eq("account_id", "")).endJunction().eq("farm_id", farmId).between("work_date", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
        else {
          aryIkubyoLine = IkubyoLine.find.where().eq("account_id", accountid).between("work_date", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
      }

      return aryIkubyoLine;

    }
}
