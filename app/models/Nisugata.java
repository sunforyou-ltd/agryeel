package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】荷姿情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Nisugata extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -1493326856843761892L;
    /**
     * 荷姿ID
     */
    public double nisugataId;
    /**
     * 荷姿名
     */
    public String nisugataName;
    /**
     * 荷姿種別
     */
    public long nisugataKind;
    /**
     * 内容量
     */
    public double capacity;
    /**
     * 金額
     */
    public double kingaku;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Nisugata> find = new Finder<Long, Nisugata>(Long.class, Nisugata.class);

    public static String getNisugataName(double nisugataId) {

    	String result = "未選択";

    	Nisugata nisugata = Nisugata.find.where().eq("nisugata_id", nisugataId).findUnique();

    	if ( nisugata != null ) {

    		result = nisugata.nisugataName;

    	}

    	return result;

    }

    /**
     * 荷姿情報を取得します
     * @param pNisugataId
     * @return
     */
    public static Nisugata getNisugataInfo(double pNisugataId) {
    	return Nisugata.find.where().eq("nisugata_id", pNisugataId).findUnique();
    }
}
