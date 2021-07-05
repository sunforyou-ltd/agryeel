package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別サイズ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SizeOfFarm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 2786947662402450591L;
    /**
     * サイズID
     */
    public double sizeId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, SizeOfFarm> find = new Finder<Long, SizeOfFarm>(Long.class, SizeOfFarm.class);

    /**
     * 対象生産者のサイズ情報を取得する
     * @param farmId
     * @return
     */
    public static List<SizeOfFarm> getSizeOfFarm(double farmId) {

    	List<SizeOfFarm> arySize = SizeOfFarm.find.where().eq("farm_id", farmId).orderBy("size_id").findList();

    	return arySize;

    }
}
