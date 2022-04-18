package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業計画散布情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkPlanSanpu extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -9053165922072064821L;
    /**
     * 作業計画ID
     */
    public double workPlanId;
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
    /**
     * 有効成分
     */
    public String yukoSeibun;

    public static Finder<Long, WorkPlanSanpu> find = new Finder<Long, WorkPlanSanpu>(Long.class, WorkPlanSanpu.class);

    public static List<WorkPlanSanpu> getWorkPlanSanpuList(double pWorkPlanId) {
      return WorkPlanSanpu.find.where().eq("work_plan_id", pWorkPlanId).order("work_diary_sequence asc").findList();
    }

}
