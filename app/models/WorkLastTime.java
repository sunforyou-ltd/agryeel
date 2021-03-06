package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業前回情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkLastTime extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 4942540540223910131L;
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
     * 作業時間
     */
    public int workTime;
    /**
     * 潅水量
     */
    public double kansuiRyo;
    /**
     * 収穫量
     */
    public double shukakuRyo;
    /**
     * 株間
     */
    public double kabuma;
    /**
     * 条間
     */
    public double joukan;
    /**
     * 条数
     */
    public double jousu;
    /**
     * 深さ
     */
    public double hukasa;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * アタッチメントID
     */
    public double attachmentId;
    /**
     * 品種ID
     */
    public String hinsyuId;
    /**
     * ベルトID
     */
    public double beltoId;
    /**
     * 潅水部分
     */
    public int kansuiPart;
    /**
     * 潅水間隔
     */
    public double kansuiSpace;
    /**
     * 潅水方法
     */
    public int kansuiMethod;
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
     * 使用マルチ
     */
    public double useMulti;
    /**
     * 列数
     */
    public double retusu;
    /**
     * 使用苗枚数
     */
    public int naemaisu;
    /**
     * 使用穴数
     */
    public int useHole;
    /**
     * 枚数
     */
    public int maisu;
    /**
     * 使用培土
     */
    public double useBaido;
    /**
     * 剪定高
     */
    public double senteiHeight;
    /**
     * 仕立本数
     */
    public double shitateHonsu;
    /**
     * 日長
     */
    public double nicho;
    /**
     * 廃棄量
     */
    public double haikiRyo;

    public static Finder<Long, WorkLastTime> find = new Finder<Long, WorkLastTime>(Long.class, WorkLastTime.class);

    /**
     * 作業前回情報を取得する
     * @param pWorkId
     * @param pFarmId
     * @param pCropId
     * @return
     */
    public static WorkLastTime getWorkLastTime(double pWorkId, double pFarmId , double pCropId) {
      return WorkLastTime.find.where().eq("work_id", pWorkId).eq("farm_id", pFarmId).eq("crop_id", pCropId).findUnique();
    }

}
