package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(条日別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriLineD extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -6226871080100338917L;
  /**
	 * 条ID
	 */
	public double lineId;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriLineD> find = new Finder<Long, SriLineD>(Long.class, SriLineD.class);

}
