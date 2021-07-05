package models;

import java.sql.Date;
import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】自動潅水モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class AutoKansui extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -8909465356484652107L;
    /**
     * 自動潅水ID
     */
    public double autoKansuiId;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 作業ID
     */
    public double workId;

    public static Finder<Long, AutoKansui> find = new Finder<Long, AutoKansui>(Long.class, AutoKansui.class);
}
