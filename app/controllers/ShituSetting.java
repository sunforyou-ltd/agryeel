package controllers;

import models.Shitu;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.ShituComprtnent;

import consts.AgryeelConst;

public class ShituSetting extends Controller {

    public static Result shituSettingMove(double farmId, double shituId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.SHITUID
                , String.valueOf(shituId));										//質IDをセッションに格納

        return ok(views.html.shituSetting.render(""));
    }

    /**
     * 【AGRYEEL】質情報設定初期表示データ取得
     * @return
     */
    public static Result shituSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 質IDの取得 */
        Double shituId = Double.parseDouble(session(AgryeelConst.SessionKey.SHITUID));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("shituId"			, shituId);								//質ID
        if(shituId != 0){															//編集の場合
	        /* 質IDから質情報を取得する */
	        Shitu shituData = Shitu.getShituInfo(shituId);
	        if (shituData != null) {												//質情報が存在する場合
	            resultJson.put("shituName"	, shituData.shituName);					//質名
	        }
	        else {
	            return notFound();													//質情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】質情報登録/更新実行
     * @return 質登録/更新結果JSON
     */
    public static Result submitShitu() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Shitu shitu = null;

        JsonNode 	shituInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(shituInput.get("farmId").asText());
        Double		shituId = Double.parseDouble(shituInput.get("shituId").asText());
        String 		shituName = shituInput.get("shituName").asText();

        if(shituId == 0){			//登録
        	shitu = ShituComprtnent.makeShitu(shituName);

            //----- 生産者別質を作成します -----
        	ShituComprtnent.makeShituOfFarm(shitu.shituId, farmId);
        }else{						//更新
        	shitu = ShituComprtnent.updateShitu(shituId, shituName);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】質情報削除処理
     * @return
     */
    public static Result deleteShitu() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double shituId = Double.parseDouble(input.get("shituId").asText());

        /* 質情報を削除する */
        ShituComprtnent.deleteShitu(shituId, farmId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
