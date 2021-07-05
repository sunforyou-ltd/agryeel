package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(担当者作付別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriAccountS extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -6148066802278687571L;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 作業年
     */
    public int workYear;
    /**
     * 年内回転数
     */
    public int rotationSpeedOfYear;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriAccountS> find = new Finder<Long, SriAccountS>(Long.class, SriAccountS.class);

}
