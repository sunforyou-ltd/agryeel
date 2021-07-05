package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(条月別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriLineM extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = 9185534637401589749L;
  /**
	 * 条ID
	 */
	public double lineId;
    /**
     * 作業年月
     */
    public Date workYearMonth;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriLineM> find = new Finder<Long, SriLineM>(Long.class, SriLineM.class);

}
