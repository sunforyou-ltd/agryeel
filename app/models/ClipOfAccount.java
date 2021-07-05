package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】アカウント別クリップ情報
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ClipOfAccount extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = 9079220706440275714L;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * クリップグループID
     */
    public double clipGroupId;

    public static Finder<Long, ClipOfAccount> find = new Finder<Long, ClipOfAccount>(Long.class, ClipOfAccount.class);

}
