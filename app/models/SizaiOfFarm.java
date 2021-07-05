package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別資材情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SizaiOfFarm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 5395686204924166153L;
    /**
     * 資材ID
     */
    public double sizaiId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, SizaiOfFarm> find = new Finder<Long, SizaiOfFarm>(Long.class, SizaiOfFarm.class);

    /**
     * 対象生産者の資材情報を取得する
     * @param farmId
     * @return
     */
    public static List<SizaiOfFarm> getSizaiOfFarm(double farmId) {

    	List<SizaiOfFarm> arySizai = SizaiOfFarm.find.where().eq("farm_id", farmId).orderBy("sizai_id").findList();

    	return arySizai;

    }
}
