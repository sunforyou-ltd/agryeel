package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】勤務記録
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class KinmuDiary extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1L;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 勤務ID
     */
    public long kinmuId;
    /**
     * 勤務フラグ
     */
    public short kinmuFlag;
    /**
     * メッセージ
     */
    public String message;
    /**
     * 日時
     */
    public Timestamp dayTime;
    /**
     * 年
     */
    public short year;
    /**
     * 月
     */
    public short month;
    /**
     * 日
     */
    public short day;


    public static Finder<Long, KinmuDiary> find = new Finder<Long, KinmuDiary>(Long.class, KinmuDiary.class);
}
