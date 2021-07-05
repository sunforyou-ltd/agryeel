package compornent;

import java.util.List;

import models.Attachment;
import models.AttachmentOfFarm;
import models.Sequence;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 【AGRYEEL】アタッチメントコンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class AttachmentComprtnent implements AgryellInterface{

  List<Attachment> attachments;

	/**
	 * コンストラクタ
	 */
	public AttachmentComprtnent() {

		attachments = Attachment.find.orderBy("attachment_id").findList();

	}

  /**
   * アタッチメント情報を生成する
   * @param pInput
   * @return
   */
  public static Attachment makeAttachment(JsonNode pInput) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.ATTACHMENTID);       //最新シーケンス値の取得

    Attachment attachment = new Attachment();
    attachment.attachmentId    	= sequence.sequenceValue;
    attachment.attachementName  = pInput.get("attachementName").asText();
    attachment.katasiki  	= pInput.get("katasiki").asText();
    attachment.attachmentKind 	= Integer.parseInt(pInput.get("attachmentKind").asText());
    attachment.save();

    return attachment;

  }

  /**
   * 生産者別アタッチメント情報を生成する
   * @param pBeltoId
   * @param pFarmId
   * @return
   */
  public static AttachmentOfFarm makeAttachmentOfFarm(double pAttachmentId, double pFarmId) {

	  AttachmentOfFarm attachment = new AttachmentOfFarm();
	  attachment.attachmentId = pAttachmentId;
	  attachment.farmId  = pFarmId;
	  attachment.save();

    return attachment;

  }

  /**
   * アタッチメント情報を更新する
   * @param pInput
   * @return
   */
  public static Attachment updateAttachment(JsonNode pInput) {

	double attachmentId = Double.parseDouble(pInput.get("attachmentId").asText());
	Attachment attachment 		= Attachment.getAttachmentInfo(attachmentId);
	attachment.attachementName  = pInput.get("attachementName").asText();
	attachment.katasiki        	= pInput.get("katasiki").asText();
	attachment.attachmentKind 	= Integer.parseInt(pInput.get("attachmentKind").asText());
	attachment.update();

    return attachment;

  }

  /**
   * アタッチメントを削除します
   * @param pCropGroupId
   * @return
   */
  public static int deleteAttachment(double pAttachmentId, double pFarmId) {

    /** 戻り値 */
    int result  = UPDATE_SUCCESS;

    //----- アタッチメントを削除 ----
    //Ebean.createSqlUpdate("DELETE FROM attachment WHERE attachment_id = " + pAttachmentId).execute();
    Attachment attachment = Attachment.getAttachmentInfo(pAttachmentId);
    attachment.deleteFlag = 1;
    attachment.update();

    //----- 生産者別アタッチメントを削除 ----
    //Ebean.createSqlUpdate("DELETE FROM attachment_of_farm WHERE farm_id = " + pFarmId + "AND attachment_id = " + pAttachmentId).execute();
    AttachmentOfFarm aof = AttachmentOfFarm.find.where().eq("farm_id", pFarmId).eq("attachment_id", pAttachmentId).findUnique();
    aof.deleteFlag = 1;
    aof.update();

    return result;

  }
}
