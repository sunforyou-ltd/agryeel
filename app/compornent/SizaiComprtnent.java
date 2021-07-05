package compornent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import models.Sequence;
import models.Sizai;
import models.SizaiOfFarm;
import play.libs.Json;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYEEL】資材コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SizaiComprtnent implements AgryellInterface{

  List<Sizai> sizaiList;

	/**
	 * コンストラクタ
	 */
	public SizaiComprtnent() {

		sizaiList = Sizai.find.orderBy("sizai_id").findList();

	}

  /**
   * 資材情報を生成する
   * @param pInput
   * @return
   */
  public static Sizai makeSizai(JsonNode pInput) {
	java.sql.Date defaultDate	= DateU.GetNullDate();
    SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");
    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.SIZAIID);       //最新シーケンス値の取得

    Sizai sizai = new Sizai();
    sizai.sizaiId    	= sequence.sequenceValue;
    sizai.sizaiName   	= pInput.get("sizaiName").asText();
    sizai.sizaiKind 	= Integer.parseInt(pInput.get("sizaiKind").asText());
    sizai.unitKind 		= Integer.parseInt(pInput.get("unitKind").asText());
    sizai.ryo		 	= Double.parseDouble(pInput.get("ryo").asText());
    sizai.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
    try {
    	sizai.purchaseDate	=	new java.sql.Date(sdf.parse(pInput.get("purchaseDate").asText().replace("/", "")).getTime());	//購入日
    } catch (ParseException e) {
    	sizai.purchaseDate	=	defaultDate;
    }
    sizai.serviceLife	= Long.parseLong(pInput.get("serviceLife").asText());
    sizai.save();

    return sizai;

  }

  /**
   * 生産者別資材情報を生成する
   * @param pSizaiId
   * @param pFarmId
   * @return
   */
  public static SizaiOfFarm makeSizaiOfFarm(double pSizaiId, double pFarmId) {

	SizaiOfFarm sizai = new SizaiOfFarm();
	sizai.sizaiId = pSizaiId;
	sizai.farmId  = pFarmId;
	sizai.save();

    return sizai;

  }

  /**
   * 資材情報を更新する
   * @param pInput
   * @return
   */
  public static Sizai updateSizai(JsonNode pInput) {

	java.sql.Date defaultDate	= DateU.GetNullDate();
	SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");
	double sizaiId = Double.parseDouble(pInput.get("sizaiId").asText());
	Sizai sizai 		= Sizai.getSizaiInfo(sizaiId);
    sizai.sizaiName   	= pInput.get("sizaiName").asText();
    sizai.sizaiKind 	= Integer.parseInt(pInput.get("sizaiKind").asText());
    sizai.unitKind 		= Integer.parseInt(pInput.get("unitKind").asText());
    sizai.ryo		 	= Double.parseDouble(pInput.get("ryo").asText());
    sizai.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
    try {
    	sizai.purchaseDate	=	new java.sql.Date(sdf.parse(pInput.get("purchaseDate").asText().replace("/", "")).getTime());	//購入日
    } catch (ParseException e) {
    	sizai.purchaseDate	=	defaultDate;
    }
    sizai.serviceLife	= Long.parseLong(pInput.get("serviceLife").asText());
    sizai.update();

    return sizai;

  }

  /**
  * 資材を削除します
  * @param pSizaiId
  * @param pFarmId
  * @return
  */
 public static int deleteSizai(double pSizaiId, double pFarmId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- 資材を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM sizai WHERE sizai_id = " + pSizaiId).execute();
   Sizai sizai = Sizai.getSizaiInfo(pSizaiId);
   sizai.deleteFlag = 1;
   sizai.update();

   //----- 生産者別資材を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM sizai_of_farm WHERE farm_id = " + pFarmId + "AND sizai_id = " + pSizaiId).execute();
   SizaiOfFarm sof = SizaiOfFarm.find.where().eq("farm_id", pFarmId).eq("sizai_id", pSizaiId).findUnique();
   sof.deleteFlag = 1;
   sof.update();

   return result;

 }

  /**
   * 資材種別より資材を取得する
   * @param pFarmId
   * @param pKind
   * @param pListJson
   * @return
   */
  public static int getSizaiJson(double pFarmId, int pKind, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    List<SizaiOfFarm> datas = SizaiOfFarm.getSizaiOfFarm(pFarmId);

    for (SizaiOfFarm data : datas) {
      if (data.deleteFlag == 1) { // 削除済みの場合
        continue;
      }

      Sizai model = Sizai.getSizaiInfo(data.sizaiId);
      if (model != null) {
        if (model.sizaiKind != pKind) { // 対象種別以外の場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , model.sizaiId);
        jd.put("name" , model.sizaiName);

        pListJson.put(String.valueOf(model.sizaiId), jd);
      }
    }

    return result;

  }

  /**
   * 資材種別より資材を取得する
   * @param pFarmId
   * @param pKind
   * @param pListJson
   * @return
   */
  public static int getSizaiJsonArray(double pFarmId, int pKind, ArrayNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    List<SizaiOfFarm> datas = SizaiOfFarm.getSizaiOfFarm(pFarmId);

    for (SizaiOfFarm data : datas) {
      if (data.deleteFlag == 1) { // 削除済みの場合
        continue;
      }

      Sizai model = Sizai.getSizaiInfo(data.sizaiId);
      if (model != null) {
        if (model.sizaiKind != pKind) { // 対象種別以外の場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , model.sizaiId);
        jd.put("name" , model.sizaiName);

        pListJson.add(jd);
      }
    }

    return result;

  }
}
