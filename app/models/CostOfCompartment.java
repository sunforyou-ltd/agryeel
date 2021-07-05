package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】区画別経費モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CostOfCompartment extends Model {

  /**
   * 区画ID
   */
  public double kukakuId;
  /**
   * 年
   */
  public short year;
  /**
   * 月
   */
  public short month;
  /**
   * 人件費
   */
  public double costPeople;
  /**
   * 肥料原価
   */
  public double costHiryo;
  /**
   * 農薬原価
   */
  public double costNouyaku;


  public static Finder<Long, CostOfCompartment> find = new Finder<Long, CostOfCompartment>(Long.class, CostOfCompartment.class);

}
