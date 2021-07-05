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

import models.Youki;
import models.Sequence;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 【AGRYEEL】容器コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class YoukiComprtnent implements AgryellInterface{

  List<Youki> youkis;

	/**
	 * コンストラクタ
	 */
	public YoukiComprtnent() {

		youkis = Youki.find.orderBy("youki_id").findList();

	}

  /**
   * 容器情報を生成する
   * @param pInput
   * @return
   */
  public static Youki makeYouki(JsonNode pInput) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.YOUKIID);       //最新シーケンス値の取得

    Youki youki = new Youki();
    youki.youkiId       = sequence.sequenceValue;
    youki.youkiName  	= pInput.get("youkiName").asText();
    youki.youkiKind    	= Integer.parseInt(pInput.get("youkiKind").asText());
    youki.farmId		= Double.parseDouble(pInput.get("farmId").asText());
    youki.unitKind    	= Integer.parseInt(pInput.get("unitKind").asText());
    youki.kosu			= Double.parseDouble(pInput.get("kosu").asText());
    youki.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
    youki.save();

    return youki;

  }

  /**
   * 容器情報を更新する
   * @param pInput
   * @return
   */
  public static Youki updateYouki(JsonNode pInput) {

	  Youki youki = Youki.getYoukiInfo(Double.parseDouble(pInput.get("youkiId").asText()));
      youki.youkiName  	= pInput.get("youkiName").asText();
      youki.youkiKind   = Integer.parseInt(pInput.get("youkiKind").asText());
      youki.farmId		= Double.parseDouble(pInput.get("farmId").asText());
      youki.unitKind    = Integer.parseInt(pInput.get("unitKind").asText());
      youki.kosu		= Double.parseDouble(pInput.get("kosu").asText());
      youki.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
      youki.update();

    return youki;

  }

  /**
  * 容器を削除します
  * @param pYoukiId
  * @return
  */
 public static int deleteYouki(double pYoukiId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- 容器を削除 ----
   Youki youki = Youki.getYoukiInfo(pYoukiId);
   youki.deleteFlag = 1;
   youki.update();

   return result;

 }

}
