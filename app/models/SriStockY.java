package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(株年別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriStockY extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -201003858482074478L;
  /**
	 * 株ID
	 */
	public double stockId;
    /**
     * 作業年
     */
    public int workYear;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriStockY> find = new Finder<Long, SriStockY>(Long.class, SriStockY.class);

}
