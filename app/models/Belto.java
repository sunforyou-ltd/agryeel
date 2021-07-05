package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】ベルト情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Belto extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -8865575760804785695L;
    /**
     * ベルトID
     */
    public double beltoId;
    /**
     * ベルト名
     */
    public String beltoName;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Belto> find = new Finder<Long, Belto>(Long.class, Belto.class);

    public static String getBeltoName(double beltoId) {

    	String result = "未選択";

    	Belto belto = Belto.find.where().eq("belto_id", beltoId).findUnique();

    	if ( belto != null ) {

    		result = belto.beltoName;

    	}

    	return result;

    }

    /**
     * ベルト情報を取得します
     * @param pBeltoId
     * @return
     */
    public static Belto getBeltoInfo(double pBeltoId) {
    	return Belto.find.where().eq("belto_id", pBeltoId).findUnique();
    }
}
