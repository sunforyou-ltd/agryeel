package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(区画年別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriKukakuY extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = 960847142336856817L;
  /**
	 * 区画ID
	 */
	public double kukakuId;
    /**
     * 作業年
     */
    public int workYear;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriKukakuY> find = new Finder<Long, SriKukakuY>(Long.class, SriKukakuY.class);

}
