package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別荷姿情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NisugataOfFarm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -3506090897061442968L;
    /**
     * 荷姿ID
     */
    public double nisugataId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, NisugataOfFarm> find = new Finder<Long, NisugataOfFarm>(Long.class, NisugataOfFarm.class);

    /**
     * 対象生産者の荷姿情報を取得する
     * @param farmId
     * @return
     */
    public static List<NisugataOfFarm> getNisugataOfFarm(double farmId) {

    	List<NisugataOfFarm> aryNisugata = NisugataOfFarm.find.where().eq("farm_id", farmId).orderBy("nisugata_id").findList();

    	return aryNisugata;

    }
}
