package controllers;

import models.Hinsyu;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CropComprtnent;

import consts.AgryeelConst;

public class HinsyuSetting extends Controller {

    public static Result hinsyuSettingMove(double farmId, double hinsyuId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.HINSYUID
                , String.valueOf(hinsyuId));									//品種IDをセッションに格納

        return ok(views.html.hinsyuSetting.render(""));
    }

    /**
     * 【AGRYEEL】品種情報設定初期表示データ取得
     * @return
     */
    public static Result hinsyuSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 品種IDの取得 */
        Double hinsyuId = Double.parseDouble(session(AgryeelConst.SessionKey.HINSYUID));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("hinsyuId"			, hinsyuId);							//品種ID
        if(hinsyuId != 0){															//編集の場合
	        /* 品種IDから品種情報を取得する */
	        Hinsyu hinsyuData = Hinsyu.getHinsyuInfo(hinsyuId);
	        if (hinsyuData != null) {												//品種情報が存在する場合
	            resultJson.put("hinsyuName"		, hinsyuData.hinsyuName);			//品種名
	        }
	        else {
	            return notFound();													//生産物グループ情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】品種情報登録/更新実行
     * @return 品種登録/更新結果JSON
     */
    public static Result submitHinsyu() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Hinsyu hinsyu = null;

        JsonNode 	hinsyuInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(hinsyuInput.get("farmId").asText());
        Double		hinsyuId = Double.parseDouble(hinsyuInput.get("hinsyuId").asText());
        String 		hinsyuName = hinsyuInput.get("hinsyuName").asText();

        if(hinsyuId == 0){			//登録
        	hinsyu = CropComprtnent.makeHinsyu(hinsyuName);

        //----- 生産者別品種を作成します -----
        CropComprtnent.makeHinsyuOfFarm(hinsyu.hinsyuId, farmId);
        }else{						//更新
        	hinsyu = CropComprtnent.updateHinsyu(hinsyuId, hinsyuName);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】品種情報削除処理
     * @return
     */
    public static Result deleteHinsyu() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double hinsyuId = Double.parseDouble(input.get("hinsyuId").asText());

        /* 品種情報を削除する */
        CropComprtnent.deleteHinsyu(hinsyuId, farmId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
