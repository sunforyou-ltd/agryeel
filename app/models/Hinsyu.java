package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】種情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Hinsyu extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1849969140025058112L;
    /**
     * 品種ID
     */
    public double hinsyuId;
    /**
     * 品種名
     */
    public String hinsyuName;
    /**
     * 生産物ID
     */
    public double cropId;
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

    public static Finder<Long, Hinsyu> find = new Finder<Long, Hinsyu>(Long.class, Hinsyu.class);

    public static String getHinsyuName(double hinsyuId) {

    	String result = "未選択";

    	Hinsyu hinsyu = Hinsyu.find.where().eq("hinsyu_id", hinsyuId).findUnique();

    	if ( hinsyu != null ) {

    		result = hinsyu.hinsyuName;

    	}

    	return result;

    }

    /**
     * 品種情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static Hinsyu getHinsyuInfo(double pHinsyuId) {
    	return Hinsyu.find.where().eq("hinsyu_id", pHinsyuId).findUnique();
    }

    /**
     * 品種ID、生産物IDから品種情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static Hinsyu getHinsyuUnique(double pHinsyuId, double pCropId) {
    	return Hinsyu.find.where().eq("hinsyu_id", pHinsyuId).eq("crop_id", pCropId).findUnique();
    }

    /**
     * 生産物IDから品種情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static List<Hinsyu> getHinsyuOfCrop(double pCropId) {
    	return Hinsyu.find.where().eq("crop_id", pCropId).orderBy("hinsyu_id").findList();
    }
    /**
     * 複数品種名称を一括取得する
     * @param pHinsyuId
     * @return
     */
    public static String getMultiHinsyuName(String pHinsyuId) {
      String result = "";
      if (pHinsyuId == null) {
        return result;
      }
      String[] hinsyuIds = pHinsyuId.split(",");
      for (String hinsyuId: hinsyuIds) {
        if(!"".equals(result)) {
          result += "、";
        }
        result += Hinsyu.getHinsyuName(Double.parseDouble(hinsyuId));
      }
      return result;
    }
    /***
     * 複数品種IDから生産物IDを取得する
     * @param pHinsyuId
     * @return
     */
    public static double getMultiHinsyuCropId(String pHinsyuId) {
      double result = 0;
      if (pHinsyuId == null) {
        return result;
      }
      String[] hinsyuIds = pHinsyuId.split(",");
      for (String hinsyuId: hinsyuIds) {
        Hinsyu hinsyu = getHinsyuInfo(Double.parseDouble(hinsyuId));
        if (hinsyu != null) {
          result = hinsyu.cropId;
        }
      }
      return result;
    }
}
