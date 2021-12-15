package models;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

import com.avaje.ebean.annotation.UpdatedTimestamp;

import consts.AgryeelConst;

@Entity
/**
 * 【AGRYEEL】アカウント情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Account extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1212727762535542219L;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * パスワード
     */
    public String password;
    /**
     * 氏名
     */
    public String acountName;
    /**
     * かな
     */
    public String acountKana;
    /**
     * 役割分担
     */
    public long part;
    /**
     * 備考
     */
    public String remark;
    /**
     * メールアドレス
     */
    public String mailAddress;
    /**
     * 生年月日
     */
    public Date birthday;
    /**
     * 管理者権限
     */
    public int managerRole;
    /**
     * メニュー権限
     */
    public long menuRole;
    /**
     * GoogleID
     */
    public String googleID;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * アカウント画像
     */
	public byte[] accountPicture;
    /**
     * 初期表示ページ
     */
    public int firstPage;
    /**
     * ログイン回数
     */
    public double loginCount;
    /**
     * 入力回数
     */
    public double inputCount;
    /**
     * クリップモード
     */
    public int clipMode;
    /**
     * 心拍数上限
     */
    public int heartRateUpLimit;
    /**
     * 心拍数下限
     */
    public int heartRateDownLimit;
    /**
     * 圃場ID
     */
    public double fieldId;
    /**
     * 作業開始時間
     */
    @UpdatedTimestamp
    public Timestamp workStartTime;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * メッセージアイコン
     */
    public int messageIcon;
    /**
     * 通知メッセージ
     */
    public String notificationMessage;
    /**
     * ハッシュ値
     */
    public String hashValue;
    /**
     * 作業計画ID
     */
    public double workPlanId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Account> find = new Finder<Long, Account>(Long.class, Account.class);

    public void setWorkingInfo(double pWorkId, double pFieldId, Timestamp pWorkStartTime, double pWorkPlanId) {
      this.workId         = pWorkId;
      this.fieldId        = pFieldId;
      this.workStartTime  = pWorkStartTime;
      this.workPlanId     = pWorkPlanId;
    }
    public void clearWorkingInfo() {
      this.workId               = 0;
      this.fieldId              = 0;
      this.workStartTime        = null;
      this.workPlanId           = 0;
      this.notificationMessage  = "";
      this.messageIcon          = AgryeelConst.MessageIcon.NONE;
    }

    /**
     * 対象生産者のアカウント情報を取得する
     * @param farmId
     * @return
     */
    public static List<Account> getAccountOfFarm(double farmId) {

    	List<Account> aryAccount = Account.find.where().eq("farm_id", farmId).orderBy("account_id").findList();

    	return aryAccount;

    }

    /**
     * 対象圃場のアカウント情報を取得する
     * @param farmId
     * @param fieldId
     * @return
     */
    public static List<Account> getAccountOfField(double fieldId) {

    	List<Account> aryAccount = Account.find.where().eq("field_id", fieldId).orderBy("account_id").findList();

    	return aryAccount;

    }

    /**
     * 作業中の作業情報を取得します
     * @return
     */
    public Work getWork() {

      Work work = null;

      if (workId != 0) {
        work = Work.find.where().eq("work_id", workId).findUnique();
      }

      return work;
    }

    public static List<Account> getAccountOfWorking(double farmId) {

      List<Account> aryAccount = Account.find.where().eq("farm_id", farmId).ne("work_id", 0).orderBy("work_start_time desc").findList();

      return aryAccount;

    }

    public static Account getAccount(String accountId) {
      return Account.find.where().eq("account_id", accountId).findUnique();
    }


}
