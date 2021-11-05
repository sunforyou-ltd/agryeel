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
 * 【AGRYEEL】育苗計画ライン情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class IkubyoPlanLine extends Model {

    /**
     * 育苗計画ID
     */
    public double ikubyoPlanId;
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

    public static Finder<Long, IkubyoPlanLine> find = new Finder<Long, IkubyoPlanLine>(Long.class, IkubyoPlanLine.class);

    /**
     * 対象期間の育苗計画ラインを取得する
     * @param farmId
     * @param startDate
     * @param endDate
     * @return 育苗ライン情報
     */
    public static List<IkubyoPlanLine> getIkubyoPlanLineOfFarm(double farmId, Date startDate,Date endDate) {

      List<IkubyoPlanLine> aryIkubyoPlanLine = IkubyoPlanLine.find.where().eq("farm_id", farmId).between("work_date", startDate, endDate).orderBy("work_date desc, work_id desc, update_time desc").findList();

      return aryIkubyoPlanLine;

    }
    /**
     * 対象期間の育苗計画ラインを取得する
     * @param naeNo
     * @param startDate
     * @param endDate
     * @return 育苗ライン情報
     */
    public static List<IkubyoPlanLine> getIkubyoPlanLineOfRange(String naeNo, Date startDate,Date endDate) {

    	List<IkubyoPlanLine> aryIkubyoPlanLine = IkubyoPlanLine.find.where().eq("nae_no", naeNo).between("work_date", startDate, endDate).orderBy("work_date desc, work_id desc, update_time desc").findList();

    	return aryIkubyoPlanLine;

    }
    public static List<IkubyoPlanLine> getIkubyoPlanLineOfAccount(String accountid, double farmId, Date startDate,Date endDate) {

      List<IkubyoPlanLine> aryIkubyoLine;
      if (accountid.equals(AgryeelConst.SpecialAccount.ALLACOUNT)) {
        aryIkubyoLine = IkubyoPlanLine.find.where().between("work_date", startDate, endDate).eq("farm_id", farmId).orderBy("work_date desc, update_time desc").findList();
      }
      else {
        String[] accounts = accountid.split(",");
        if (accounts.length > 1) {
          List<String> accountKeys = new ArrayList<String>();
          for (String key : accounts) {
            accountKeys.add(key);
          }
          aryIkubyoLine = IkubyoPlanLine.find.where().disjunction().add(Expr.in("account_id", accountKeys)).add(Expr.eq("account_id", "")).endJunction().eq("farm_id", farmId).between("work_date", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
        else {
          aryIkubyoLine = IkubyoPlanLine.find.where().eq("account_id", accountid).between("work_date", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
      }

      return aryIkubyoLine;

    }
}
