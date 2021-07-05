package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(担当者日別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriAccountD extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -8053183568664843070L;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriAccountD> find = new Finder<Long, SriAccountD>(Long.class, SriAccountD.class);

}
