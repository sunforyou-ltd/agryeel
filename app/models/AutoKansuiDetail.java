package models;

import java.sql.Timestamp;
import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】自動潅水明細モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class AutoKansuiDetail extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -3804352060683732422L;
    /**
     * 自動潅水ID
     */
    public double autoKansuiId;
    /**
     * 自動潅水シーケンス
     */
    public double autoKansuiSeq;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 潅水方法
     */
    public int kansuiMethod;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * 潅水時間
     */
    public int kansuiTime;
    /**
     * 潅水量
     */
    public double kansuiRyo;
    /**
     * 自動潅水開始時間
     */
    public Timestamp autoKansuiStartTime;
    /**
     * 自動潅水終了時間
     */
    public Timestamp autoKansuiEndTime;
    /**
     * 潅水順番
     */
    public int kansuiOrder;
    /**
     * 自動潅水状態
     */
    public int autoKansuiStatus;

    public static Finder<Long, AutoKansuiDetail> find = new Finder<Long, AutoKansuiDetail>(Long.class, AutoKansuiDetail.class);
}
