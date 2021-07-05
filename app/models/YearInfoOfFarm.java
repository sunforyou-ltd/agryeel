package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別年間情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class YearInfoOfFarm extends Model {

    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 年
     */
    public short year;
    /**
     * 月
     */
    public short month;
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

    public static Finder<Long, YearInfoOfFarm> find = new Finder<Long, YearInfoOfFarm>(Long.class, YearInfoOfFarm.class);

}
