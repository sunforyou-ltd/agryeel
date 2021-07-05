package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産物グループ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CropGroup extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 4328994657370694966L;
    /**
     * 生産物グループID
     */
    public double cropGroupId;
    /**
     * 生産物グループ名
     */
    public String cropGroupName;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, CropGroup> find = new Finder<Long, CropGroup>(Long.class, CropGroup.class);

    /**
     * 対象生産者の生産物グループ情報を取得する
     * @param farmId
     * @return
     */
    public static List<CropGroup> getCropGroupOfFarm(double farmId) {

    	List<CropGroup> aryCropGroup = CropGroup.find.where().eq("farm_id", farmId).orderBy("crop_group_id desc").findList();

    	return aryCropGroup;

    }

    /**
     * 生産物グループIDより生産物グループ情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static CropGroup getCropGroup(double pCropGroupId) {
      return CropGroup.find.where().eq("crop_group_id", pCropGroupId).findUnique();
    }
}
