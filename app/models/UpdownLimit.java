package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】上下限情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class UpdownLimit extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = 4545295604674209975L;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * 項目名
     */
    public int itemNumericN;
    /**
     * 上限値
     */
    public double upLimit;
    /**
     * 下限値
     */
    public double downLimit;

    public static Finder<Long, UpdownLimit> find = new Finder<Long, UpdownLimit>(Long.class, UpdownLimit.class);

}
