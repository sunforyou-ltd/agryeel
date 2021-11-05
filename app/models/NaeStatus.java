package models;

import java.sql.Date;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】苗状況情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NaeStatus extends Model {

    /**
     * 苗No
     */
    public String naeNo;
    /**
<<<<<<< HEAD
=======
     * 生産者ID
     */
    public double farmId;
    /**
>>>>>>> e747d9c9b47c3d59e92c6b368bbb246cc6564120
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
     * 在庫数量
     */
    public double zaikoSuryo;
    /**
     * 個数
     */
    public double kosu;
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

    public static Finder<Long, NaeStatus> find = new Finder<Long, NaeStatus>(Long.class, NaeStatus.class);

    /**
     * 対象生産者の苗状況情報を取得する
     * @param naeNo
     * @return
     */
    public static List<NaeStatus> getStatusOfFarm(double farmId) {

    	List<NaeStatus> aryNaeStatus = NaeStatus.find.where().eq("farm_id", farmId).orderBy("nae_no, crop_id, hinsyu_id").findList();

    	return aryNaeStatus;

    }

    /**
     * 対象苗Noの苗状況情報を取得する
     * @param naeNo
     * @return
     */
    public static NaeStatus getStatusOfNae(String naeNo) {

    	NaeStatus naeStatus = NaeStatus.find.where().eq("nae_no", naeNo).orderBy("hinsyu_id desc, crop_id desc").findUnique();

    	return naeStatus;
    }
}
