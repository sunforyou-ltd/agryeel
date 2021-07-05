package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(畝年別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriHillY extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = 7541715699105144118L;
  /**
	 * 畝ID
	 */
	public double hillId;
    /**
     * 作業年
     */
    public int workYear;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriHillY> find = new Finder<Long, SriHillY>(Long.class, SriHillY.class);

}
