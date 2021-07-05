package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(担当者年別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriAccountY extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -805861888679339652L;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 作業年
     */
    public int workYear;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriAccountY> find = new Finder<Long, SriAccountY>(Long.class, SriAccountY.class);

}
