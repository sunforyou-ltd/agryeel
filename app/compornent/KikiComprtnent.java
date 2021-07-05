package compornent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import models.Attachment;
import models.AttachmentOfFarm;
import models.CompartmentWorkChainStatus;
import models.Kiki;
import models.KikiOfFarm;
import models.Sequence;
import models.WorkChainItem;
import play.libs.Json;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYEEL】機器コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class KikiComprtnent implements AgryellInterface{

  List<Kiki> kikis;

	/**
	 * コンストラクタ
	 */
	public KikiComprtnent() {

		kikis = Kiki.find.orderBy("kiki_id").findList();

	}

	/**
	 * 生産者IDよりアタッチメント一覧を取得し、JSONに格納する
	 * @param pListJson
	 * @return
	 */
	public static int getAttachmentOfFarmSelJson(double pFarmId, double pKikiId, ObjectNode pListJson) {
	    /** 戻り値 */
	    int result  = GET_SUCCESS;
	    int exist = 0;

	    List<AttachmentOfFarm> attach = AttachmentOfFarm.getAttachmentOfFarm(pFarmId);
	    for (AttachmentOfFarm attachData : attach) {
          if (attachData.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

	      ObjectNode jd = Json.newObject();

	      jd.put("id"   , attachData.attachmentId);
	      jd.put("name" , Attachment.getAttachmentName(attachData.attachmentId));
	      Kiki kiki = Kiki.getKikiInfo(pKikiId);
	      if(kiki != null){
	    	  /* 自機器の設定済アタッチメントIDの判定 */
	    	 if((kiki.onUseAttachmentId == null) || (kiki.onUseAttachmentId.isEmpty())){
		    	 jd.put("flag" , 0);
	    	 }else{
		    	 String[] attachList = kiki.onUseAttachmentId.split(",", 0);
		    	 exist = 0;
		    	 for(int idx = 0; idx < attachList.length; idx++){
		    		 if(attachData.attachmentId == Double.parseDouble(attachList[idx])){
		    			 exist = 1;
		    			 break;
		    		 }
		    	 }
		    	 if(exist == 1){
		           jd.put("flag" , 1);
		    	 }else{
			       jd.put("flag" , 0);
		    	 }
	    	 }
	      }else{
	    	 jd.put("flag" , 0);
	      }
	      pListJson.put(String.valueOf(attachData.attachmentId), jd);
	  }

	  return result;
	}
  /**
   * 機器情報を生成する
   * @param pInput
   * @return
   */
  public static Kiki makeKiki(JsonNode pInput) {
	java.sql.Date defaultDate	= DateU.GetNullDate();
    SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");
    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.KIKIID);       //最新シーケンス値の取得

    Kiki kiki = new Kiki();
    kiki.kikiId    	= sequence.sequenceValue;
    kiki.kikiName   = pInput.get("kikiName").asText();
    kiki.katasiki  	= pInput.get("katasiki").asText();
    kiki.maker  	= pInput.get("maker").asText();
    kiki.kikiKind 	= Integer.parseInt(pInput.get("kikiKind").asText());
    kiki.onUseAttachmentId	= pInput.get("onUseAttachmentId").asText();
    kiki.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
    try {
    	kiki.purchaseDate	=	new java.sql.Date(sdf.parse(pInput.get("purchaseDate").asText().replace("/", "")).getTime());	//購入日
    } catch (ParseException e) {
    	kiki.purchaseDate	=	defaultDate;
    }
    kiki.days	= Long.parseLong(pInput.get("days").asText());
    kiki.serviceLife	= Long.parseLong(pInput.get("serviceLife").asText());
    kiki.save();

    return kiki;

  }

  /**
   * 生産者別機器情報を生成する
   * @param pBeltoId
   * @param pFarmId
   * @return
   */
  public static KikiOfFarm makeKikiOfFarm(double pKikiId, double pFarmId) {

	  KikiOfFarm kiki = new KikiOfFarm();
	  kiki.kikiId = pKikiId;
	  kiki.farmId  = pFarmId;
	  kiki.save();

    return kiki;

  }

  /**
   * 機器情報を更新する
   * @param pInput
   * @return
   */
  public static Kiki updateKiki(JsonNode pInput) {
	java.sql.Date defaultDate	= DateU.GetNullDate();
	SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");
	double kikiId = Double.parseDouble(pInput.get("kikiId").asText());
	Kiki kiki 		= Kiki.getKikiInfo(kikiId);
    kiki.kikiName   = pInput.get("kikiName").asText();
    kiki.katasiki  	= pInput.get("katasiki").asText();
    kiki.maker  	= pInput.get("maker").asText();
    kiki.kikiKind 	= Integer.parseInt(pInput.get("kikiKind").asText());
    kiki.onUseAttachmentId	= pInput.get("onUseAttachmentId").asText();
    kiki.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
    try {
    	kiki.purchaseDate	=	new java.sql.Date(sdf.parse(pInput.get("purchaseDate").asText().replace("/", "")).getTime());	//購入日
    } catch (ParseException e) {
    	kiki.purchaseDate	=	defaultDate;
    }
    kiki.days	= Long.parseLong(pInput.get("days").asText());
    kiki.serviceLife	= Long.parseLong(pInput.get("serviceLife").asText());
    kiki.update();

    return kiki;

  }

    /**
     * 機器を削除します
     * @param pCropGroupId
     * @return
     */
    public static int deleteKiki(double pKikiId, double pFarmId) {

      /** 戻り値 */
      int result  = UPDATE_SUCCESS;

      //----- 機器を削除 ----
      //Ebean.createSqlUpdate("DELETE FROM kiki WHERE kiki_id = " + pKikiId).execute();
      Kiki kiki = Kiki.getKikiInfo(pKikiId);
      kiki.deleteFlag = 1;
      kiki.update();

      //----- 生産者別機器を削除 ----
      //Ebean.createSqlUpdate("DELETE FROM kiki_of_farm WHERE farm_id = " + pFarmId + "AND kiki_id = " + pKikiId).execute();
      KikiOfFarm kof = KikiOfFarm.find.where().eq("farm_id", pFarmId).eq("kiki_id", pKikiId).findUnique();
      kof.deleteFlag = 1;
      kof.update();

      return result;

    }
    /**
     * 作業IDから使用可能な機器を取得する
     * @param pFarmId
     * @param pWorkId
     * @param pKukakuId
     * @param pListJson
     * @return
     */
    public static int getKikiOfWorkJson(double pFarmId, double pWorkId, double pKukakuId, ObjectNode pListJson) {

      //----- 区画ワークチェーン状況を取得 -----
      CompartmentWorkChainStatus cwcs = WorkChainCompornent.getCompartmentWorkChainStatus(pKukakuId);
      if (cwcs != null) {
        WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(cwcs.workChainId, pWorkId);
        if (wci != null) {
          //----- 生産者別機器情報を取得 -----
          List<KikiOfFarm> kofs = KikiOfFarm.getKikiOfFarm(pFarmId);
          List<Double>kikis = new ArrayList<Double>();
          for (KikiOfFarm kof : kofs) {
            if (kof.deleteFlag == 1) { // 削除済みの場合
              continue;
            }
            kikis.add(kof.kikiId);
          }
          //----- 作業単位の使用可能機器種別を取得 -----
          if (wci.onUseKikiKind != null && !"".equals(wci.onUseKikiKind)) {
            String[] ukks = wci.onUseKikiKind.split(",");
            List<Double>kks = new ArrayList<Double>();
            for (String ukk : ukks) {
              kks.add(Double.parseDouble(ukk));
            }
            //----- 対象機器の取得 -----
            List<Kiki> kikiList = Kiki.find.where().in("kiki_id", kikis).in("kiki_kind", kks).order("kiki_id").findList();
            for (Kiki kiki : kikiList) {

              ObjectNode jd = Json.newObject();

              jd.put("id"   , kiki.kikiId);
              jd.put("name" , kiki.kikiName);

              pListJson.put(String.valueOf(kiki.kikiId), jd);

            }
          }
        }
      }

      return GET_SUCCESS;

    }
    public static int getAttachmentOfKikiJson(double pKikiId, ObjectNode pListJson) {


      Kiki kiki = Kiki.getKikiInfo(pKikiId);
      if (kiki != null) {
        //----- 機器別の使用可能アタッチメントを取得する -----
        if (kiki.onUseAttachmentId != null && !"".equals(kiki.onUseAttachmentId)) {
          String[] atats = kiki.onUseAttachmentId.split(",");
          List<Double>atas = new ArrayList<Double>();
          for (String ata : atats) {
            atas.add(Double.parseDouble(ata));
          }
          //----- 対象機器の取得 -----
          List<Attachment> attachments = Attachment.find.where().in("attachment_id", atas).order("attachment_id").findList();
          for (Attachment attachment : attachments) {
            if (attachment.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode jd = Json.newObject();

            jd.put("id"   , attachment.attachmentId);
            jd.put("name" , attachment.attachementName);

            pListJson.put(String.valueOf(attachment.attachmentId), jd);

          }
        }
      }

      return GET_SUCCESS;

    }
}
