package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産物情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Crop extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -9160266865089338314L;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 生産物名
     */
    public String cropName;
    /**
     * 生産物カラー
     */
    public String cropColor;
    /**
     * 有効積算温度上限
     */
    public double yukoKionUpper;
    /**
     * 有効積算温度下限
     */
    public double yukoKionLower;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Crop> find = new Finder<Long, Crop>(Long.class, Crop.class);

    /**
     * 生産物情報を取得します
     * @param pField
     * @return
     */
    public static Crop getCropInfo(double pCrop) {
      return Crop.find.where().eq("crop_id", pCrop).findUnique();
    }

    /**
     * 対象生産者の生産物情報を取得する
     * @param farmId
     * @return
     */
    public static List<Crop> getCropOfFarm(double farmId) {

    	List<Crop> aryCrop = Crop.find.where().eq("farm_id", farmId).orderBy("crop_id asc").findList();

    	return aryCrop;

    }
    /**
     * 名称からIDへ変換を行います
     * @param pName
     * @return
     */
    public static double convertId(String pName) {
      double result = 0;

      List<Crop> crops = Crop.find.where()
                             .eq("crop_name", pName)
                             .findList();
      if (crops.size() == 1) { //該当レコードが１件のみ
        result = crops.get(0).cropId;
      }

      return result;
    }
}
