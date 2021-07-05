package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】AIDADATA
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class AicaData extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -3938080319353601492L;
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
    public double hinsyuId;
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
     * 収穫量
     */
    public double shukakuRyo;
    /**
     * 収穫開始日
     */
    public Date shukakuStartDate;
    /**
     * 収穫終了日
     */
    public Date shukakuEndDate;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 生産物名
     */
    public String cropName;
    /**
     * 作業開始日
     */
    public Date workStartDay;
    /**
     * 作業終了日
     */
    public Date workEndDay;
    /**
     * クリッピング１
     */
    public short cliping1;
    /**
     * クリッピング２
     */
    public short cliping2;
    /**
     * クリッピング３
     */
    public short cliping3;
    /**
     * クリッピング４
     */
    public short cliping4;
    /**
     * クリッピング５
     */
    public short cliping5;
    /**
     * 元帳履歴フラグ
     */
    public short motochoFlag;
    /**
     * 積算温度
     */
    public double integratedTemp;
    /**
     * 作業時間
     */
    public int workTime;
    /**
     * 面積
     */
    public double area;

    public static Finder<Long, AicaData> find = new Finder<Long, AicaData>(Long.class, AicaData.class);

}
