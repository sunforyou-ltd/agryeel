package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別機器情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class KikiOfFarm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -4469265845149177283L;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, KikiOfFarm> find = new Finder<Long, KikiOfFarm>(Long.class, KikiOfFarm.class);

    /**
     * 対象生産者の機器情報を取得する
     * @param farmId
     * @return
     */
    public static List<KikiOfFarm> getKikiOfFarm(double farmId) {

    	List<KikiOfFarm> aryKiki = KikiOfFarm.find.where().eq("farm_id", farmId).orderBy("kiki_id").findList();

    	return aryKiki;

    }
}
