package compornent;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import play.libs.Json;
import util.DateU;

import models.Soil;
import models.Sequence;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 【AGRYEEL】土コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SoilComprtnent implements AgryellInterface{

  List<Soil> soils;

	/**
	 * コンストラクタ
	 */
	public SoilComprtnent() {

		soils = Soil.find.orderBy("soil_id").findList();

	}

  /**
   * 土情報を生成する
   * @param pInput
   * @return
   */
  public static Soil makeSoil(JsonNode pInput) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.SOILID);       //最新シーケンス値の取得

    Soil soil = new Soil();
    soil.soilId     = sequence.sequenceValue;
    soil.soilName  	= pInput.get("soilName").asText();
    soil.soilKind   = Integer.parseInt(pInput.get("soilKind").asText());
    soil.farmId	    = Double.parseDouble(pInput.get("farmId").asText());
    soil.unitKind   = Integer.parseInt(pInput.get("unitKind").asText());
    soil.kingaku    = Double.parseDouble(pInput.get("kingaku").asText());
    soil.save();

    return soil;

  }

  /**
   * 土情報を更新する
   * @param pInput
   * @return
   */
  public static Soil updateSoil(JsonNode pInput) {

	  Soil soil = Soil.getSoilInfo(Double.parseDouble(pInput.get("soilId").asText()));
      soil.soilName   = pInput.get("soilName").asText();
      soil.soilKind   = Integer.parseInt(pInput.get("soilKind").asText());
      soil.farmId     = Double.parseDouble(pInput.get("farmId").asText());
      soil.unitKind   = Integer.parseInt(pInput.get("unitKind").asText());
      soil.kingaku    = Double.parseDouble(pInput.get("kingaku").asText());
      soil.update();

    return soil;

  }

  /**
  * 土を削除します
  * @param pSoilId
  * @return
  */
 public static int deleteSoil(double pSoilId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- 土を削除 ----
   Soil soil = Soil.getSoilInfo(pSoilId);
   soil.deleteFlag = 1;
   soil.update();

   return result;

 }

}
