package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】クリップグループ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ClipGroup extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -5809961402887340005L;
    /**
     * クリップグループID
     */
    public double clipGroupId;
    /**
     * クリップグループ名
     */
    public String clipGroupName;
    /**
     * 生産者ID
     */
    public double farmId;

    public static Finder<Long, ClipGroup> find = new Finder<Long, ClipGroup>(Long.class, ClipGroup.class);

}
