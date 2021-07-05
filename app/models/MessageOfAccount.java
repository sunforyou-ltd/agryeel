package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import consts.AgryeelConst;

@Entity
/**
 * 【AGRYEEL】アカウント別メッセージ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class MessageOfAccount extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -7168071548201211408L;
    /**
     * 更新日時
     */
    public Timestamp updateTime;
    /**
     * アカウントID
     */
    public String accountId;

    public static Finder<Long, MessageOfAccount> find = new Finder<Long, MessageOfAccount>(Long.class, MessageOfAccount.class);

    /**
     * アカウントIDが一致するワンメッセージを取得します
     * @param pAccountId
     * @return
     */
    public static List<SystemMessage> getOneMessageByAccountId(String pAccountId) {

      List<MessageOfAccount> messages = MessageOfAccount.find.where().eq("account_id", pAccountId).findList();
      List<java.sql.Timestamp> keys = new ArrayList<Timestamp>();

      for (MessageOfAccount message : messages) {
        keys.add(message.updateTime);
      }

      return SystemMessage.find.where().eq("message_kind", AgryeelConst.MessageKind.ONE).in("update_time", keys).orderBy("update_time desc").findList();

    }
    /**
     * アカウントIDが一致するシステムメッセージを取得します
     * @param pAccountId
     * @return
     */
    public static List<SystemMessage> getMessageByAccountId(String pAccountId) {

      List<MessageOfAccount> messages = MessageOfAccount.find.where().eq("account_id", pAccountId).findList();
      List<java.sql.Timestamp> keys = new ArrayList<Timestamp>();
      Calendar cal = Calendar.getInstance();
      java.sql.Date system = new java.sql.Date(cal.getTimeInMillis());

      for (MessageOfAccount message : messages) {
        keys.add(message.updateTime);
      }

      return SystemMessage.find.where().ne("message_kind", AgryeelConst.MessageKind.ONE).in("update_time", keys).le("release_date", system).orderBy("release_date desc").findList();

    }

    /**
     * チェック済みのシステムメッセージを削除します
     * @param pAccountId
     * @param pKey
     * @return
     */
    public static int checkMessage(String pAccountId, Timestamp pKey) {

      int result = 0;

      result = Ebean.createSqlUpdate("DELETE FROM message_of_account WHERE account_id = :accountId AND update_time = :key").setParameter("accountId", pAccountId).setParameter("key", pKey).execute();

      return result;

    }

}
