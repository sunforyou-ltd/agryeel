package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(株日別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriStockD extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -6943666829060492447L;
  /**
	 * 株ID
	 */
	public double stockId;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriStockD> find = new Finder<Long, SriStockD>(Long.class, SriStockD.class);

}
