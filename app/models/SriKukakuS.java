package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(区画作付別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriKukakuS extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -9099854390462272050L;
  /**
	 * 区画ID
	 */
	public double kukakuId;
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

    public static Finder<Long, SriKukakuS> find = new Finder<Long, SriKukakuS>(Long.class, SriKukakuS.class);

}
