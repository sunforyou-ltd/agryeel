package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】元帳照会基本情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class MotochoBase extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 7750338831469394975L;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 区画名
     */
    public String kukakuName;
    /**
     * 区画グループカラー
     */
    public String kukakuGroupColor;
    /**
     * 作業年
     */
    public int workYear;
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
     * 生産物名
     */
    public String cropName;
    /**
     * 播種日
     */
    public Date hashuDate;
    /**
     * 生育日数
     */
    public int seiikuDayCount;
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
     * 作業開始日
     */
    public Date workStartDay;
    /**
     * 作業終了日
     */
    public Date workEndDay;
    /**
     * 面積
     */
    public double area;
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
     * 苗No
     */
    public String naeNo;

    public static Finder<Long, MotochoBase> find = new Finder<Long, MotochoBase>(Long.class, MotochoBase.class);

}
