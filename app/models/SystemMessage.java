package models;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】システムメッセージ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SystemMessage extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 6158861252804512901L;
    /**
     * 更新日時
     */
    public Timestamp updateTime;
    /**
     * 公開日
     */
    public Date releaseDate;
    /**
     * メッセージ
     */
    public String message;
    /**
     * メッセージ種別
     */
    public int messageKind = 0;

    public static Finder<Long, SystemMessage> find = new Finder<Long, SystemMessage>(Long.class, SystemMessage.class);

    /**
     * 対象期間のメッセージを取得する
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<SystemMessage> getSystemMessageOfRange(Date startDate,Date endDate) {

    	List<SystemMessage> arySystemMessage = SystemMessage.find.where().between("release_date", startDate, endDate).orderBy("release_date desc, update_time desc").findList();

    	return arySystemMessage;

    }
    public static int makeOneMessage(double pFarmId, String pMessage) {
      int result = 0;

//      Calendar cal = Calendar.getInstance();
//      SystemMessage systemMessage = new SystemMessage();
//      systemMessage.message       = pMessage;
//      systemMessage.releaseDate   = new java.sql.Date(cal.getTime().getTime());
//      systemMessage.updateTime    = new java.sql.Timestamp(cal.getTime().getTime());
//      systemMessage.messageKind   = AgryeelConst.MessageKind.ONE;
//      systemMessage.save();
//
//      List<Account> accountList = Account.find.where().eq("farm_id", pFarmId).orderBy("account_id").findList();
//      for (Account account : accountList) {
//        MessageOfAccount moa = new MessageOfAccount();
//        moa.accountId = account.accountId;
//        moa.updateTime = new java.sql.Timestamp(cal.getTime().getTime());
//        moa.save();
//      }

      return result;
    }
}
