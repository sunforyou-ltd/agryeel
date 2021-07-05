package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】圃場グループ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class FieldGroup extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 6301043482347034154L;
    /**
     * 圃場グループID
     */
    public double fieldGroupId;
    /**
     * 圃場グループ名
     */
    public String fieldGroupName;
    /**
     * 圃場グループ名カラー
     */
    public String fieldGroupColor;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * シーケンスID
     */
    public long sequenceId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;
    /**
     * 作業指示自動生成
     */
    public short workPlanAutoCreate;

    public static Finder<Long, FieldGroup> find = new Finder<Long, FieldGroup>(Long.class, FieldGroup.class);

    /**
     * 対象生産者の圃場グループ情報を取得する
     * @param farmId
     * @return
     */
    public static List<FieldGroup> getFieldGroupOfFarm(double farmId) {

    	List<FieldGroup> aryFieldGroup = FieldGroup.find.where().eq("farm_id", farmId).orderBy("sequence_id, field_group_id").findList();

    	return aryFieldGroup;

    }
    /**
     * 対象生産者の圃場グループIDを取得する
     * @param farmId
     * @return
     */
    public static String[] getFieldGroupIdOfFarm(double farmId) {

    	List<FieldGroup> aryFieldGroup = FieldGroup.find.where().eq("farm_id", farmId).orderBy("sequence_id, field_group_id").findList();
        String[] fgs = new String[aryFieldGroup.size()];
        int idx = 0;

        for (FieldGroup fg : aryFieldGroup) {
          fgs[idx] = Double.toString(fg.fieldGroupId);
          idx++;
        }

    	return fgs;
    }
    /**
     * 圃場グループIDより圃場グループ情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static FieldGroup getFieldGroup(double pFieldGroupId) {
      return FieldGroup.find.where().eq("field_group_id", pFieldGroupId).findUnique();
    }
}
