package models;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】センスプラウト機器マスタ
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SsKikiData extends Model {
  /**
   * 区画ID
   */
  public double kukakuId;
  /**
   * 計測日時
   */
  public Timestamp keisokuDayTime;
  /**
   * 10cm体積含水率
   */
  public double vmc10;
  /**
   * 20cm体積含水率
   */
  public double vmc20;
  /**
   * 電流
   */
  public double current;
  /**
   * 電源電圧
   */
  public double voltage;
  /**
   * 地表温度
   */
  public double gt;
  public static Finder<Long, SsKikiData> find = new Finder<Long, SsKikiData>(Long.class, SsKikiData.class);
}
