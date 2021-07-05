package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別品目情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CropInfoOfFarm extends Model {

    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 年
     */
    public short year;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 収穫量
     */
    public double shukakuRyo;
    /**
     * 前年収穫量
     */
    public double shukakuRyoPrev;
    /**
     * 月間作業時間
     */
    public double workTimeMonth;
    /**
     * 前年作業時間
     */
    public double workTimeMonthPrev;

    public static Finder<Long, CropInfoOfFarm> find = new Finder<Long, CropInfoOfFarm>(Long.class, CropInfoOfFarm.class);

}
