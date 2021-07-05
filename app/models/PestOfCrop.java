package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】害虫情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class PestOfCrop extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -925764874344684719L;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 害虫ID
     */
    public double pestId;

    public static Finder<Long, PestOfCrop> find = new Finder<Long, PestOfCrop>(Long.class, PestOfCrop.class);

    /**
     * 生産物ＩＤより害虫情報を取得します（世代は最大を対象）
     * @param pCropId
     * @return
     */
    public static List<Pest> getPestGeneration(double pCropId) {
      List<Pest> pests = new ArrayList<Pest>();
      List<PestOfCrop> pocs = PestOfCrop.find.where().eq("crop_id", pCropId).orderBy("pest_id").findList();

      for (PestOfCrop poc : pocs) {
        List<Pest> datas = Pest.getPestGeneration(poc.pestId);
        if (datas.size() > 0) {
          pests.add(datas.get((datas.size() - 1)));
        }
      }

      return pests;
    }

}
