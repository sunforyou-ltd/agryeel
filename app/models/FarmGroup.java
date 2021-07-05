package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者グループ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class FarmGroup extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -1720927856595235680L;
    /**
     * 生産者グループID
     */
    public double farmGroupId;
    /**
     * 生産者グループ名
     */
    public String farmGroupName;

    public static Finder<Long, FarmGroup> find = new Finder<Long, FarmGroup>(Long.class, FarmGroup.class);

}
