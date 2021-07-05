package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(条作付別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriLineS extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = 5067239041435846009L;
  /**
	 * 条ID
	 */
	public double lineId;
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

    public static Finder<Long, SriLineS> find = new Finder<Long, SriLineS>(Long.class, SriLineS.class);

}
