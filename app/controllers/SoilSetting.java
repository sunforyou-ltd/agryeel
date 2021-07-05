package controllers;

import java.text.DecimalFormat;

import models.Soil;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SoilComprtnent;

import consts.AgryeelConst;

public class SoilSetting extends Controller {

    public static Result soilSettingMove(double farmId, double soilId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.SOILID
                , String.valueOf(soilId));										//土IDをセッションに格納

        return ok(views.html.soilSetting.render(""));
    }

    /**
     * 【AGRYEEL】土情報設定初期表示データ取得
     * @return
     */
    public static Result soilSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 土IDの取得 */
        Double soilId = Double.parseDouble(session(AgryeelConst.SessionKey.SOILID));

        DecimalFormat df = new DecimalFormat("###0");

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("soilId"				, soilId);								//土ID
        if(soilId != 0){															//編集の場合
	        /* 土IDから容器情報を取得する */
	        Soil soilData = Soil.getSoilInfo(soilId);
	        if (soilData != null) {													//土情報が存在する場合
	            resultJson.put("soilName"				, soilData.soilName);		//土名
	            resultJson.put("soilKind"				, soilData.soilKind);		//土種別
	            resultJson.put("unitKind"				, soilData.unitKind);		//単位種別
	            resultJson.put("kingaku"				, soilData.kingaku);		//金額
	        }
	        else {
	            return notFound();													//土情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】土情報登録/更新実行
     * @return 土登録/更新結果JSON
     */
    public static Result submitSoil() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Soil soil = null;

        JsonNode 	soilInput 	= request().body().asJson();
        Double		soilId = Double.parseDouble(soilInput.get("soilId").asText());

        if(soilId == 0){			//登録
        	soil = SoilComprtnent.makeSoil(soilInput);
        }else{						//更新
        	soil = SoilComprtnent.updateSoil(soilInput);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】土情報削除処理
     * @return
     */
    public static Result deleteSoil() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double soilId = Double.parseDouble(input.get("soilId").asText());

        /* 土情報を削除する */
        SoilComprtnent.deleteSoil(soilId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
