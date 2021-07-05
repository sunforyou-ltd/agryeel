package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(区画日別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriKukakuD extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = 51313787433943357L;
  /**
	 * 区画ID
	 */
	public double kukakuId;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriKukakuD> find = new Finder<Long, SriKukakuD>(Long.class, SriKukakuD.class);

}
