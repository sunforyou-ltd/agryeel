package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産物グループ明細情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CropGroupList extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1851525462431685035L;
    /**
     * 生産物グループID
     */
    public double cropGroupId;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, CropGroupList> find = new Finder<Long, CropGroupList>(Long.class, CropGroupList.class);

    /**
     * 生産物グループID、生産物IDから生産物明細情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static CropGroupList getCropUnique(double pCropGroupId, double pCropId) {
    	return CropGroupList.find.where().eq("crop_group_id", pCropGroupId).eq("crop_id", pCropId).findUnique();
    }

    /**
     * 生産物グループIDから所属している生産物情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static List<Crop> getCrop(double pCropGroupId) {
      List<Crop> result = new  ArrayList<Crop>();

      List<CropGroupList> cropGroupList = CropGroupList.find.where().eq("crop_group_id", pCropGroupId).order("crop_id").findList();
      for (CropGroupList cgl : cropGroupList) {
    	  Crop crop = Crop.getCropInfo(cgl.cropId);
        if (crop != null) {
          result.add(crop);
        }
      }
      return result;
    }
}
