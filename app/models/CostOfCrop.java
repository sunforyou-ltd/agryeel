package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産物別経費モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CostOfCrop extends Model {

  /**
   * 生産者ID
   */
  public double farmId;
  /**
   * 生産物ID
   */
  public double cropId;
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


  public static Finder<Long, CostOfCrop> find = new Finder<Long, CostOfCrop>(Long.class, CostOfCrop.class);

}
