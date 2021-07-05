package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】元帳照会農薬情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class MotochoNouyaku extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -1337277245993735441L;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 作業年
     */
    public int workYear;
    /**
     * 年内回転数
     */
    public int rotationSpeedOfYear;
    /**
     * 農肥ID
     */
    public double nouhiId;
    /**
     * 農薬番号
     */
    public double nouyakuNo;
    /**
     * 農肥名
     */
    public String nouhiName;
    /**
     * 農肥グループID
     */
    public double nouhiGroupId;
    /**
     * 農肥グループ名
     */
    public String nouhiGroupName;
    /**
     * 散布日
     */
    public Date sanpuDate;
    /**
     * 倍率
     */
    public double bairitu;
    /**
     * 散布量
     */
    public double sanpuryo;
    /**
     * 散布方法
     */
    public int sanpuMethod;
    /**
     * 有支成分
     */
    public String yushiSeibun;
    /**
     * g当り
     */
    public String gUnitValue;
    /**
     * N
     */
    public double n;
    /**
     * P
     */
    public double p;
    /**
     * K
     */
    public double k;
    /**
     * 単位
     */
    public String unit;

    public static Finder<Long, MotochoNouyaku> find = new Finder<Long, MotochoNouyaku>(Long.class, MotochoNouyaku.class);

}
