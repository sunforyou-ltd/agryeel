package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(担当者月別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriAccountM extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -8665607087828714010L;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 作業年月
     */
    public Date workYearMonth;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriAccountM> find = new Finder<Long, SriAccountM>(Long.class, SriAccountM.class);

}
