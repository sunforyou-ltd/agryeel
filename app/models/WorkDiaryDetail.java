package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業記録情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkDiaryDetail extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -986091194000382686L;
    /**
     * 作業記録ID
     */
    public double workDiaryId;
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

    public static Finder<Long, WorkDiaryDetail> find = new Finder<Long, WorkDiaryDetail>(Long.class, WorkDiaryDetail.class);

    public static List<WorkDiaryDetail> getWorkDiaryDetailList(double pWorkDiaryId) {
      return WorkDiaryDetail.find.where().eq("work_diary_id", pWorkDiaryId).order("work_diary_sequence asc").findList();
    }
    public static List<WorkPlanDetail> getWorkPlanDetailList(double pWorkPlanId) {
      return WorkPlanDetail.find.where().eq("work_plan_id", pWorkPlanId).order("work_diary_sequence asc").findList();
    }

}
