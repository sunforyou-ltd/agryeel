package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業履歴詳細情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkHistryDetail extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = 5793269922595972080L;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 作業履歴シーケンス
     */
    public double workHistrySequence;
    /**
     * 作業詳細種別
     */
    public int workDetailKind;
    /**
     * 数量
     */
    public int suryo;
    /**
     * 資材ID
     */
    public double sizaiId;
    /**
     * 内容
     */
    public String comment;
    /**
     * 収穫規格荷姿
     */
    public int syukakuNisugata;
    /**
     * 収穫規格質
     */
    public int syukakuSitsu;
    /**
     * 収穫規格サイズ
     */
    public int syukakuSize;
    /**
     * 収穫規格個数
     */
    public double syukakuKosu;
    /**
     * 収穫量
     */
    public double shukakuRyo;
    /**
     * 収穫箱数
     */
    public double syukakuHakosu;
    /**
     * 収穫人数
     */
    public int syukakuNinzu;
    /**
     * 苗No
     */
    public String naeNo;
    /**
     * 個数
     */
    public double kosu;
    /**
     * 列数
     */
    public double retusu;
    /**
     * 条間
     */
    public double joukan;
    /**
     * 条数
     */
    public double jousu;
    /**
     * 作付距離
     */
    public double plantingDistance;

    public static Finder<Long, WorkHistryDetail> find = new Finder<Long, WorkHistryDetail>(Long.class, WorkHistryDetail.class);

    public static WorkHistryDetail getWorkHistryBase(double pWorkId, double pFarmId , double pCropId) {
      return WorkHistryDetail.find.where().eq("work_id", pWorkId).eq("farm_id", pFarmId).eq("crop_id", pCropId).findUnique();
    }


}
