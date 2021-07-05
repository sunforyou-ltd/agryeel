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
public class PosttoPoint extends Model {

  /**
   *
   */
  private static final long serialVersionUID = -6105412411794562647L;
  /**
   * 郵便番号
   */
  public String postNo;
  /**
   * 地点ID
   */
  public String pointId;
  /**
   * 気象庁内県コード
   */
  public long precNo;
  /**
   * 気象庁内地域コード
   */
  public long blockNo;


  public static Finder<Long, PosttoPoint> find = new Finder<Long, PosttoPoint>(Long.class, PosttoPoint.class);

  public static String getPointId(String pPostNo) {
    String result = "";

    PosttoPoint pp = PosttoPoint.find.where().eq("post_no", pPostNo).findUnique();

    if (pp != null) {
      result = pp.pointId;
    }

    return result;
  }


}
