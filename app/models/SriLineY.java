package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(条年別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriLineY extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -1552746607494553868L;
  /**
	 * 条ID
	 */
	public double lineId;
    /**
     * 作業年
     */
    public int workYear;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriLineY> find = new Finder<Long, SriLineY>(Long.class, SriLineY.class);

}
