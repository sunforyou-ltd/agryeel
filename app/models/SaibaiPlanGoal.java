package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】栽培計画目標
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SaibaiPlanGoal extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 7750338831469394975L;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 作業年
     */
    public int workYear;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * １月
     */
    public double month1;
    /**
     * ２月
     */
    public double month2;
    /**
     * ３月
     */
    public double month3;
    /**
     * ４月
     */
    public double month4;
    /**
     * ５月
     */
    public double month5;
    /**
     * ６月
     */
    public double month6;
    /**
     * ７月
     */
    public double month7;
    /**
     * ８月
     */
    public double month8;
    /**
     * ９月
     */
    public double month9;
    /**
     * １０月
     */
    public double month10;
    /**
     * １１月
     */
    public double month11;
    /**
     * １２月
     */
    public double month12;

    public static Finder<Long, SaibaiPlanGoal> find = new Finder<Long, SaibaiPlanGoal>(Long.class, SaibaiPlanGoal.class);

}
