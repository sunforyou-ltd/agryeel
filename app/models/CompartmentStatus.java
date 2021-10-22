package models;

import java.sql.Date;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】区画状況情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CompartmentStatus extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -4373762063447225066L;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 作業年
     */
    public long workYear;
    /**
     * 年内回転数
     */
    public int rotationSpeedOfYear;
    /**
     * 品種ID
     */
    public String hinsyuId;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 品種名
     */
    public String hinsyuName;
    /**
     * 播種日
     */
    public Date hashuDate;
    /**
     * 生育日数
     */
    public int seiikuDayCount;
    /**
     * 現在完了作業
     */
    public String nowEndWork;
    /**
     * 最終消毒日
     */
    public Date finalDisinfectionDate;
    /**
     * 最終潅水日
     */
    public Date finalKansuiDate;
    /**
     * 最終追肥日
     */
    public Date finalTuihiDate;
    /**
     * 収穫開始日
     */
    public Date shukakuStartDate;
    /**
     * 収穫終了日
     */
    public Date shukakuEndDate;
    /**
     * 合計消毒量
     */
    public int totalDisinfectionCount;
    /**
     * 合計潅水量
     */
    public int totalKansuiCount;
    /**
     * 合計追肥量
     */
    public int totalTuihiCount;
    /**
     * 合計収穫量
     */
    public double totalShukakuCount;
    /**
     * 合計積算日射量
     */
    public double totalSolarRadiation;
    /**
     * 合計消毒回数
     */
    public long totalDisinfectionNumber;
    /**
     * 合計潅水回数
     */
    public long totalKansuiNumber;
    /**
     * 合計追肥回数
     */
    public long totalTuihiNumber;
    /**
     * 合計収穫回数
     */
    public long totalShukakuNumber;
    /**
     * 去年消毒量
     */
    public int oldDisinfectionCount;
    /**
     * 去年潅水量
     */
    public int oldKansuiCount;
    /**
     * 去年追肥量
     */
    public int oldTuihiCount;
    /**
     * 去年収穫量
     */
    public double oldShukakuCount;
    /**
     * 合計積算日射量
     */
    public double oldSolarRadiation;
    /**
     * 現在作業モード
     */
    public short nowWorkMode;
    /**
     * 終了作業ID
     */
    public double endWorkId;
    /**
     * 最終作業日
     */
    public Date finalEndDate;
    /**
     * 次回作業ID
     */
    public double nextWorkId;
    /**
     * 作業カラー
     */
    public String workColor;
    /**
     * 片付け日
     */
    public Date katadukeDate;
    /**
     * 播種回数
     */
    public int hashuCount;
    /**
     * 現在予測ポイント
     */
    public double nowPredictionPoint;
    /**
     * 現在予測収穫量
     */
    public double nowPredictionShukaku;
    /**
     * 予測収穫可能日
     */
    public Date predictionShukakuStartDate;
    /**
     * 予測収穫量
     */
    public double predictionShukakuRyo;
    /**
     * 害虫ID
     */
    public double pestId;
    /**
     * 害虫世代
     */
    public int pestGeneration;
    /**
     * 害虫積算温度
     */
    public double pestIntegratedKion;
    /**
     * 前回算出日
     */
    public Date prevCalcDate;
    /**
     * 害虫発生予測日
     */
    public Date pestPredictDate;
    /**
     * 該当防除日
     */
    public Date targetSanpuDate;
    /**
     * 削除フラグ
     */
    public short deleteFlag;
    /**
     * 苗No
     */
    public String naeNo;

    public static Finder<Long, CompartmentStatus> find = new Finder<Long, CompartmentStatus>(Long.class, CompartmentStatus.class);

    /**
     * 対象区画の区画状況情報を取得する
     * @param kukakuId
     * @return
     */
    public static List<CompartmentStatus> getStatusOfCompartment(double kukakuId) {

    	List<CompartmentStatus> aryStatus = CompartmentStatus.find.where().eq("kukaku_id", kukakuId).orderBy("hinsyu_id desc, crop_id desc").findList();

    	return aryStatus;

    }

    /**
     * 対象区画のワークチェイン状況を取得します
     * @return
     */
    public CompartmentWorkChainStatus getWorkChainStatus() {
      return CompartmentWorkChainStatus.find.where().eq("kukaku_id", this.kukakuId).findUnique();
    }

}
