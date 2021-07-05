package models;

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
public class Pest extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -925764874344684719L;
    /**
     * 害虫ID
     */
    public double pestId;
    /**
     * 害虫名称
     */
    public String pestName;
    /**
     * 害虫世代
     */
    public int pestGeneration;
    /**
     * 害虫積算温度
     */
    public double pestIntegratedKion;
    /**
     * 害虫積算降水量
     */
    public double pestIntegratedRain;
    /**
     * 害虫経過日数
     */
    public double pestDiffDate;

    public static Finder<Long, Pest> find = new Finder<Long, Pest>(Long.class, Pest.class);

    /**
     * 害虫ＩＤより全世代の害虫情報を取得します
     * @param pPestId
     * @return
     */
    public static List<Pest> getPestGeneration(double pPestId) {
      return Pest.find.where().eq("pest_id", pPestId).orderBy("pest_generation").findList();
    }
    /**
     * 害虫ＩＤと世代より害虫情報を取得します
     * @param pPestId
     * @param pPestGeneration
     * @return
     */
    public static Pest getPestGeneration(double pPestId, int pPestGeneration) {
      return Pest.find.where().eq("pest_id", pPestId).eq("pest_generation", pPestGeneration).findUnique();
    }

}
