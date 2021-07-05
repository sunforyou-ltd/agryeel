package models;

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
public class SsKikiMaster extends Model {
  /**
   * ノードID
   */
  public String nodeId;
  /**
   * 生産者ID
   */
  public double farmId;
  /**
   * ユーザID
   */
  public String userId;
  /**
   * パスワード
   */
  public String password;
  /**
   * 区画ID
   */
  public double kukakuId;
  public static Finder<Long, SsKikiMaster> find = new Finder<Long, SsKikiMaster>(Long.class, SsKikiMaster.class);
}
