package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】収穫量情報(畝月別)モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SriHillM extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = 4457377556554073880L;
  /**
	 * 畝ID
	 */
	public double hillId;
    /**
     * 作業年月
     */
    public Date workYearMonth;
    /**
     * 合計収穫量
     */
    public int totalShukakuCount;

    public static Finder<Long, SriHillM> find = new Finder<Long, SriHillM>(Long.class, SriHillM.class);

}
