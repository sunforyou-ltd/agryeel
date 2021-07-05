package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業対象区画情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkCompartment extends Model {

	/**
   * 
   */
  private static final long serialVersionUID = 6418050603382771414L;
  /**
	 * アカウントID
	 */
	public String accountId;
	/**
	 * 区画ID
	 */
	public double kukakuId;
	/**
	 * 作業対象フラグ
	 */
	public int workTarget;

	public static Finder<Long, WorkCompartment> find = new Finder<Long, WorkCompartment>(Long.class, WorkCompartment.class);

}
