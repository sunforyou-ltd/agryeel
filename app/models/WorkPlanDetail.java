package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業計画詳細情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkPlanDetail extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = 4524797792300493877L;
    /**
     * 作業計画ID
     */
    public double workPlanId;
    /**
     * 作業記録シーケンス
     */
    public double workDiarySequence;
    /**
     * 作業詳細種別
     */
    public int workDetailKind;
    /**
     * 数量
     */
    public int suryo;
    /**
     * 資材ID
     */
    public double sizaiId;
    /**
     * 内容
     */
    public String comment;
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
     * 収穫規格個数
     */
    public double syukakuKosu;
    /**
     * 収穫量
     */
    public double shukakuRyo;
    /**
     * 収穫箱数
     */
    public double syukakuHakosu;
    /**
     * 収穫人数
     */
    public int syukakuNinzu;
    /**
     * 苗No
     */
    public String naeNo;
    /**
     * 個数
     */
    public double kosu;
    /**
     * 列数
     */
    public double retusu;
    /**
     * 条間
     */
    public double joukan;
    /**
     * 条数
     */
    public double jousu;
    /**
     * 作付距離
     */
    public double plantingDistance;

    public static Finder<Long, WorkPlanDetail> find = new Finder<Long, WorkPlanDetail>(Long.class, WorkPlanDetail.class);

    public static List<WorkPlanDetail> getWorkPlanDetailList(double pWorkPlanId) {
      return WorkPlanDetail.find.where().eq("work_plan_id", pWorkPlanId).order("work_diary_sequence asc").findList();
    }

}
