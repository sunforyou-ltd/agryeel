package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別経費モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CostOfFarm extends Model {

  /**
   * 生産者ID
   */
  public double farmId;
  /**
   * 年
   */
  public short year;
  /**
   * 月
   */
  public short month;
  /**
   * 原価
   */
  public double cost;


  public static Finder<Long, CostOfFarm> find = new Finder<Long, CostOfFarm>(Long.class, CostOfFarm.class);

}
