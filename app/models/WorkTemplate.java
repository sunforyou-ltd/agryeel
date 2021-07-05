package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業テンプレート情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkTemplate extends Model {

    /**
   * 
   */
  private static final long serialVersionUID = -925764874344684719L;
    /**
     * テンプレートID
     */
    public double workTemplateId;
    /**
     * テンプレート名
     */
    public String workTemplateName;

    public static Finder<Long, WorkTemplate> find = new Finder<Long, WorkTemplate>(Long.class, WorkTemplate.class);

    /**
     * 作業テンプレート情報を取得します
     * @param pWorkTemplateId
     * @return
     */
    public static WorkTemplate getWorkTemplate(double pWorkTemplateId) {
      return WorkTemplate.find.where().eq("work_template_id", pWorkTemplateId).findUnique();
    }

}
