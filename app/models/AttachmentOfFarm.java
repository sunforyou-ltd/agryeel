package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者別アタッチメント情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class AttachmentOfFarm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -4545037084156091702L;
    /**
     * アタッチメントID
     */
    public double attachmentId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, AttachmentOfFarm> find = new Finder<Long, AttachmentOfFarm>(Long.class, AttachmentOfFarm.class);

    /**
     * 対象生産者のアタッチメント情報を取得する
     * @param farmId
     * @return
     */
    public static List<AttachmentOfFarm> getAttachmentOfFarm(double farmId) {

    	List<AttachmentOfFarm> aryAttach = AttachmentOfFarm.find.where().eq("farm_id", farmId).orderBy("attachment_id").findList();

    	return aryAttach;

    }
}
