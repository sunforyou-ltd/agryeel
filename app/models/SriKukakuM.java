package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(区画月別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriKukakuM extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -7082814414755104737L;
  /**
	 * 区画ID
	 */
	public double kukakuId;
    /**
     * 作業年月
     */
    public Date workYearMonth;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriKukakuM> find = new Finder<Long, SriKukakuM>(Long.class, SriKukakuM.class);

}
