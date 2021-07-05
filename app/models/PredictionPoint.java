package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】郵便番号地点変換モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class PredictionPoint extends Model {

  /**
   * 
   */
  private static final long serialVersionUID = 5118978020749235913L;
  /**
   * 生産者ID
   */
  public double farmId;
  /**
   * 品種ID
   */
  public String hinsyuId;
  /**
   * 予測ポイント
   */
  public double predictionPoint;
  /**
   * 予測ポイント収穫量
   */
  public double predictionPointShukaku;
  /**
   * 積算温度
   */
  public double integratedTemp;

  public static Finder<Long, PredictionPoint> find = new Finder<Long, PredictionPoint>(Long.class, PredictionPoint.class);

}
