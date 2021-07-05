package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別質情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ShituOfFarm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -6602272896239821944L;
    /**
     * 質ID
     */
    public double shituId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, ShituOfFarm> find = new Finder<Long, ShituOfFarm>(Long.class, ShituOfFarm.class);

    /**
     * 対象生産者の質情報を取得する
     * @param farmId
     * @return
     */
    public static List<ShituOfFarm> getShituOfFarm(double farmId) {

    	List<ShituOfFarm> aryShitu = ShituOfFarm.find.where().eq("farm_id", farmId).orderBy("shitu_id").findList();

    	return aryShitu;

    }
}
