package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別ベルト情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class BeltoOfFarm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -5642794914027890043L;
    /**
     * ベルトID
     */
    public double beltoId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, BeltoOfFarm> find = new Finder<Long, BeltoOfFarm>(Long.class, BeltoOfFarm.class);

    /**
     * 対象生産者のベルト情報を取得する
     * @param farmId
     * @return
     */
    public static List<BeltoOfFarm> getBeltoOfFarm(double farmId) {

    	List<BeltoOfFarm> aryBelto = BeltoOfFarm.find.where().eq("farm_id", farmId).orderBy("belto_id").findList();

    	return aryBelto;

    }
}
