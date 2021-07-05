package models;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import play.Logger;
import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】アクティブログ
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ActiveLog extends Model {

    /**
   *
   */
    /**
     * 更新日時
     */
    public Timestamp activeTime;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 画面ID
     */
    public String screenId;
    /**
     * アクションID
     */
    public String actionId;
    /**
     * アクションメッセージ
     */
    public String actionMessage;

    public static Finder<Long, ActiveLog> find = new Finder<Long, ActiveLog>(Long.class, ActiveLog.class);

    public static void commit(String pAccountId, String pScreenId, String pActionId) {
      commit(pAccountId, pScreenId, pActionId, "");
    }
    public static void commit(String pAccountId, String pScreenId, String pActionId, String pActionMessage) {

      try {
        /* 10ミリ秒停止する事でキー重複を防ぐ */
        Thread.sleep(10);
      } catch (InterruptedException e) {
      }

      try {
        ActiveLog log       = new ActiveLog();
        log.accountId       = pAccountId;
        log.screenId        = pScreenId;
        log.actionId        = pActionId;
        log.actionMessage   = pActionMessage;
        Calendar system     = Calendar.getInstance();
        log.activeTime      = new java.sql.Timestamp(system.getTimeInMillis());
        log.save();
      } catch (Exception e) {
        Logger.error("[ AVTIVE LOG ] SQL ERROR > {}", e.getMessage(), e);
        e.printStackTrace();
      }

    }

}
