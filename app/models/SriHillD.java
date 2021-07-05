package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(畝日別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriHillD extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = -6836138551979757453L;
  /**
	 * 畝ID
	 */
	public double hillId;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriHillD> find = new Finder<Long, SriHillD>(Long.class, SriHillD.class);

}
