package compornent;

import java.util.List;

import models.Landlord;
import models.Sequence;
import play.libs.Json;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYEEL】地主コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class LandlordComprtnent implements AgryellInterface{

  List<Landlord> landloads;
  public Landlord landload;

	/**
	 * コンストラクタ
	 */
	public LandlordComprtnent() {

	  landloads = Landlord.find.orderBy("landlord_id").findList();
	  landload = null;

	}
	/**
	 * コンストラクタ
	 * @param farmId
	 */
	public LandlordComprtnent(double farmId) {

	  landloads = Landlord.find.where().eq("farm_id", farmId).orderBy("landlord_id").findList();
	  landload = null;

	}

	/**
	 * 生産者IDより地主一覧を取得し、JSONに格納する
	 * @param pListJson
	 * @return
	 */
	public static int getLandlordOfFarmSelJson(double pFarmId, double plandlordId, ObjectNode pListJson) {
	    /** 戻り値 */
	    int result  = GET_SUCCESS;
	    int exist = 0;

	    List<Landlord> landlord = Landlord.getLandlordOfFarm(pFarmId);
	    for (Landlord landlordData : landlord) {
	      ObjectNode jd = Json.newObject();

	      jd.put("id"   , landlordData.landlordId);
	      jd.put("name" , landlordData.landlordName);
	      /* 自圃場の地主IDの判定 */
	      if(landlordData.landlordId == plandlordId){
		   	 jd.put("flag" , 1);
	      }else{
		   	 jd.put("flag" , 0);
	      }

	      pListJson.put(String.valueOf(landlordData.landlordId), jd);
	  }

	  return result;
	}

  /**
   * 地主情報を生成する
   * @param pInput
   * @return
   */
  public static Landlord makeLandlord(JsonNode pInput) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.LANDLORDID);       //最新シーケンス値の取得

    Landlord landlord = new Landlord();
    landlord.landlordId    	= sequence.sequenceValue;
    landlord.landlordName  	= pInput.get("landlordName").asText();
    landlord.farmId		   	= Double.parseDouble(pInput.get("farmId").asText());
    landlord.postNo        	= pInput.get("postNo").asText();
    landlord.prefectures   	= pInput.get("prefectures").asText();
    landlord.address   	   	= pInput.get("address").asText();
    landlord.tel   	       	= pInput.get("tel").asText();
    landlord.responsibleMobileTel = pInput.get("responsibleMobileTel").asText();
    landlord.fax   	      	= pInput.get("fax").asText();
    landlord.mailAddressPC 	= pInput.get("mailAddressPC").asText();
    landlord.mailAddressMobile = pInput.get("mailAddressMobile").asText();
    landlord.bankName 		= pInput.get("mailAddressMobile").asText();
    landlord.accountType 	= Long.parseLong(pInput.get("accountType").asText());
    landlord.accountNumber	= pInput.get("accountNumber").asText();
    landlord.paymentDate 	= pInput.get("paymentDate").asText();
    landlord.save();

    return landlord;

  }

  /**
   * 地主情報を更新する
   * @param pInput
   * @return
   */
  public static Landlord updateLandlord(JsonNode pInput) {

	Landlord landlord = Landlord.getLandlordInfo(Double.parseDouble(pInput.get("landlordId").asText()));
    landlord.landlordName  	= pInput.get("landlordName").asText();
    landlord.farmId		   	= Double.parseDouble(pInput.get("farmId").asText());
    landlord.postNo        	= pInput.get("postNo").asText();
    landlord.prefectures   	= pInput.get("prefectures").asText();
    landlord.address   	   	= pInput.get("address").asText();
    landlord.tel   	       	= pInput.get("tel").asText();
    landlord.responsibleMobileTel = pInput.get("responsibleMobileTel").asText();
    landlord.fax   	      	= pInput.get("fax").asText();
    landlord.mailAddressPC 	= pInput.get("mailAddressPC").asText();
    landlord.mailAddressMobile = pInput.get("mailAddressMobile").asText();
    landlord.bankName 		= pInput.get("mailAddressMobile").asText();
    landlord.accountType 	= Long.parseLong(pInput.get("accountType").asText());
    landlord.accountNumber	= pInput.get("accountNumber").asText();
    landlord.paymentDate 	= pInput.get("paymentDate").asText();
    landlord.update();

    return landlord;

  }

    /**
     * 地主を削除します
     * @param pCropGroupId
     * @return
     */
    public static int deleteLandlord(double pLandlordId) {

      /** 戻り値 */
      int result  = UPDATE_SUCCESS;

      //----- 地主を削除 ----
      Ebean.createSqlUpdate("DELETE FROM landlord WHERE landlord_id = " + pLandlordId).execute();

      return result;

    }
}
