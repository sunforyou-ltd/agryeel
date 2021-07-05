package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】元帳照会肥料情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class MotochoHiryo extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -4495528697147721478L;
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
     * 肥料番号
     */
    public double hiryoNo;
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
    /**
     * Mg
     */
    public double mg;

    public static Finder<Long, MotochoHiryo> find = new Finder<Long, MotochoHiryo>(Long.class, MotochoHiryo.class);

}
