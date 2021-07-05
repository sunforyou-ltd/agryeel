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
public class WorkDiarySanpu extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -3232472151826064762L;
    /**
     * 作業記録ID
     */
    public double workDiaryId;
    /**
     * 作業記録シーケンス
     */
    public double workDiarySequence;
    /**
     * 散布方法
     */
    public int sanpuMethod;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * アタッチメントID
     */
    public double attachmentId;
    /**
     * 農肥ID
     */
    public double nouhiId;
    /**
     * 倍率
     */
    public double bairitu;
    /**
     * 散布量
     */
    public double sanpuryo;
    /**
     * 区画状況照会反映フラグ
     */
    public int kukakuStatusUpdate;
    /**
     * 元帳照会反映フラグ
     */
    public int motochoUpdate;

    public static Finder<Long, WorkDiarySanpu> find = new Finder<Long, WorkDiarySanpu>(Long.class, WorkDiarySanpu.class);

    public static List<WorkDiarySanpu> getWorkDiarySanpuList(double pWorkDiaryId) {
      return WorkDiarySanpu.find.where().eq("work_diary_id", pWorkDiaryId).order("work_diary_sequence asc").findList();
    }
    public static List<WorkPlanSanpu> getWorkPlanSanpuList(double pWorkPlanId) {
      return WorkPlanSanpu.find.where().eq("work_plan_id", pWorkPlanId).order("work_diary_sequence asc").findList();
    }

}
