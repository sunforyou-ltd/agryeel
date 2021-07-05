package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(株作付別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriStockS extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -7755912205826438259L;
  /**
	 * 株ID
	 */
	public double stockId;
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

    public static Finder<Long, SriStockS> find = new Finder<Long, SriStockS>(Long.class, SriStockS.class);

}
