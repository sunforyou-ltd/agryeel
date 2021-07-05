package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産物農肥情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NouhiOfCrop extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 187678745646846101L;
    /**
     * ワークチェインID
     */
    public double workChainId;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 農肥ID
     */
    public double nouhiId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, NouhiOfCrop> find = new Finder<Long, NouhiOfCrop>(Long.class, NouhiOfCrop.class);

}
