package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業履歴共通情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkHistryBase extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -6825087259998349184L;
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
     * 散布方法
     */
    public int sanpuMethod;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * アタッチメントID
     */
    public double attachmentId;
    /**
     * 農肥ID
     */
    public double nouhiId;
    /**
     * 倍率
     */
    public double bairitu;
    /**
     * 散布量
     */
    public double sanpuryo;

    public static Finder<Long, WorkHistryBase> find = new Finder<Long, WorkHistryBase>(Long.class, WorkHistryBase.class);

    /**
     * 作業前回散布情報を取得する
     * @param pWorkId
     * @param pFarmId
     * @param pCropId
     * @return
     */
    public static WorkHistryBase getWorkHistryBase(double pWorkId, double pFarmId , double pCropId) {
      return WorkHistryBase.find.where().eq("work_id", pWorkId).eq("farm_id", pFarmId).eq("crop_id", pCropId).findUnique();
    }


}
