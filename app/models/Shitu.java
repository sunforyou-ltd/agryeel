package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】質情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Shitu extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -8430117451068948670L;
    /**
     * 質ID
     */
    public double shituId;
    /**
     * 質名
     */
    public String shituName;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Shitu> find = new Finder<Long, Shitu>(Long.class, Shitu.class);

    public static String getShituName(double shituId) {

    	String result = "未選択";

    	Shitu shitu = Shitu.find.where().eq("shitu_id", shituId).findUnique();

    	if ( shitu != null ) {

    		result = shitu.shituName;

    	}

    	return result;

    }

    /**
     * 質情報を取得します
     * @param pShituId
     * @return
     */
    public static Shitu getShituInfo(double pShituId) {
    	return Shitu.find.where().eq("shitu_id", pShituId).findUnique();
    }
}
