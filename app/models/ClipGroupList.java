package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】クリップグループ明細情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ClipGroupList extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -819253028196754503L;
    /**
     * クリップグループID
     */
    public double clipGroupId;
    /**
     * 区画ID
     */
    public double kukakuId;

    public static Finder<Long, ClipGroupList> find = new Finder<Long, ClipGroupList>(Long.class, ClipGroupList.class);

}
