package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(株月別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriStockM extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = 8184255027593257573L;
  /**
	 * 株ID
	 */
	public double stockId;
    /**
     * 作業年月
     */
    public Date workYearMonth;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriStockM> find = new Finder<Long, SriStockM>(Long.class, SriStockM.class);

}
