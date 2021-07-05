package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(畝作付別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriHillS extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -2789842694682782003L;
  /**
	 * 畝ID
	 */
	public double hillId;
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

    public static Finder<Long, SriHillS> find = new Finder<Long, SriHillS>(Long.class, SriHillS.class);

}
