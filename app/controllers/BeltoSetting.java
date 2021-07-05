package controllers;

import models.Belto;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.BeltoComprtnent;

import consts.AgryeelConst;

public class BeltoSetting extends Controller {

    public static Result beltoSettingMove(double farmId, double beltoId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.BELTOID
                , String.valueOf(beltoId));										//ベルトIDをセッションに格納

        return ok(views.html.beltoSetting.render(""));
    }

    /**
     * 【AGRYEEL】ベルト情報設定初期表示データ取得
     * @return
     */
    public static Result beltoSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* ベルトIDの取得 */
        Double beltoId = Double.parseDouble(session(AgryeelConst.SessionKey.BELTOID));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("beltoId"			, beltoId);								//ベルトID
        if(beltoId != 0){															//編集の場合
	        /* 品種IDから品種情報を取得する */
	        Belto beltoData = Belto.getBeltoInfo(beltoId);
	        if (beltoData != null) {												//ベルト情報が存在する場合
	            resultJson.put("beltoName"	, beltoData.beltoName);					//ベルト名
	        }
	        else {
	            return notFound();													//ベルト情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】ベルト情報登録/更新実行
     * @return ベルト登録/更新結果JSON
     */
    public static Result submitBelto() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Belto belto = null;

        JsonNode 	beltoInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(beltoInput.get("farmId").asText());
        Double		beltoId = Double.parseDouble(beltoInput.get("beltoId").asText());
        String 		beltoName = beltoInput.get("beltoName").asText();

        if(beltoId == 0){			//登録
        	belto = BeltoComprtnent.makeBelto(beltoName);

            //----- 生産者別ベルトを作成します -----
            BeltoComprtnent.makeBeltoOfFarm(belto.beltoId, farmId);
        }else{						//更新
        	belto = BeltoComprtnent.updateBelto(beltoId, beltoName);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】ベルト情報削除処理
     * @return
     */
    public static Result deleteBelto() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double beltoId = Double.parseDouble(input.get("beltoId").asText());

        /* ベルト情報を削除する */
        BeltoComprtnent.deleteBelto(beltoId, farmId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
