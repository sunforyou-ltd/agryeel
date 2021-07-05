package models;

import java.util.ArrayList;
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
public class FieldGroupList extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -4386204688888118729L;
    /**
     * 圃場グループID
     */
    public double fieldGroupId;
    /**
     * 圃場ID
     */
    public double fieldId;
    /**
     * シーケンスID
     */
    public long sequenceId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, FieldGroupList> find = new Finder<Long, FieldGroupList>(Long.class, FieldGroupList.class);

    /**
     * 圃場グループID、圃場IDから圃場明細情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static FieldGroupList getFieldUnique(double pFieldGroupId, double pFieldId) {
    	return FieldGroupList.find.where().eq("field_group_id", pFieldGroupId).eq("field_id", pFieldId).findUnique();
    }

    /**
     * 圃場グループIDから所属している圃場情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static List<Field> getField(double pFieldGroupId) {
      List<Field> result = new  ArrayList<Field>();

      List<FieldGroupList> fieldGroupList = FieldGroupList.find.where().eq("field_group_id", pFieldGroupId).order("sequence_id").findList();
      for (FieldGroupList fgl : fieldGroupList) {
        Field field = Field.getFieldInfo(fgl.fieldId);
        if (field != null) {
          result.add(field);
        }
      }
      return result;
    }

    /**
     * 圃場ＩＤより圃場グループ情報を取得する
     * @param pfieldId
     * @return
     */
    public static FieldGroup getFieldGroup(double pfieldId) {
      FieldGroup result = null;

      List<FieldGroupList> fieldGroupList = FieldGroupList.find.where().eq("field_id", pfieldId).order("field_group_id").findList();
      for (FieldGroupList fgl : fieldGroupList) {
        FieldGroup fieldGroup = FieldGroup.getFieldGroup(fgl.fieldGroupId);
        if (fieldGroup != null) {
          result = fieldGroup;
          break;
        }
      }
      return result;
    }

    /**
     * 圃場グループIDから圃場グループリスト情報を取得します
     * @param pFieldGroupId
     * @return
     */
    public static List<FieldGroupList> getFieldGroupListForGroupId(double pFieldGroupId) {
      return FieldGroupList.find.where().eq("field_group_id", pFieldGroupId).findList();
    }

    /**
     * 圃場IDから圃場グループリスト情報を取得します
     * @param pFieldId
     * @return
     */
    public static List<FieldGroupList> getFieldGroupListForFieldId(double pFieldId) {
      return FieldGroupList.find.where().eq("field_id", pFieldId).findList();
    }
}
