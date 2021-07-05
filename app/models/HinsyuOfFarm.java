package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別種情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class HinsyuOfFarm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -1595133175695111278L;
    /**
     * 品種ID
     */
    public double hinsyuId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 使用回数
     */
    public int useCount;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, HinsyuOfFarm> find = new Finder<Long, HinsyuOfFarm>(Long.class, HinsyuOfFarm.class);

    /**
     * 対象生産者の品種情報を取得する
     * @param farmId
     * @return
     */
    public static List<HinsyuOfFarm> getHinsyuOfFarm(double farmId) {

    	List<HinsyuOfFarm> aryHinsyu = HinsyuOfFarm.find.where().eq("farm_id", farmId).orderBy("use_count DESC, hinsyu_id ASC").findList();

    	return aryHinsyu;

    }
}
