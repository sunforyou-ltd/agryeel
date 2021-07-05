package controllers;

import models.Nisugata;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.NisugataComprtnent;

import consts.AgryeelConst;

public class NisugataSetting extends Controller {

    public static Result nisugataSettingMove(double farmId, double nisugataId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.NISUGATAID
                , String.valueOf(nisugataId));									//荷姿IDをセッションに格納

        return ok(views.html.nisugataSetting.render(""));
    }

    /**
     * 【AGRYEEL】荷姿情報設定初期表示データ取得
     * @return
     */
    public static Result nisugataSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 荷姿IDの取得 */
        Double nisugataId = Double.parseDouble(session(AgryeelConst.SessionKey.NISUGATAID));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("nisugataId"			, nisugataId);							//荷姿ID
        if(nisugataId != 0){														//編集の場合
	        /* 品種IDから品種情報を取得する */
	        Nisugata nisugataData = Nisugata.getNisugataInfo(nisugataId);
	        if (nisugataData != null) {												//荷姿情報が存在する場合
	            resultJson.put("nisugataName"	, nisugataData.nisugataName);		//荷姿名
	            resultJson.put("nisugataKind"	, nisugataData.nisugataKind);		//荷姿種別
	            resultJson.put("capacity"		, nisugataData.capacity);			//内容量
	            resultJson.put("kingaku"		, nisugataData.kingaku);			//金額
	        }
	        else {
	            return notFound();													//荷姿情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】荷姿情報登録/更新実行
     * @return 荷姿登録/更新結果JSON
     */
    public static Result submitNisugata() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Nisugata nisugata = null;

        JsonNode 	nisugataInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(nisugataInput.get("farmId").asText());
        Double		nisugataId = Double.parseDouble(nisugataInput.get("nisugataId").asText());
        String 		nisugataName = nisugataInput.get("nisugataName").asText();
        long		nisugataKind = Long.parseLong(nisugataInput.get("nisugataKind").asText());
        Double		capacity = Double.parseDouble(nisugataInput.get("capacity").asText());
        Double		kingaku = Double.parseDouble(nisugataInput.get("kingaku").asText());

        if(nisugataId == 0){			//登録
        	nisugata = NisugataComprtnent.makeNisugata(nisugataName, nisugataKind, capacity, kingaku);

            //----- 生産者別荷姿を作成します -----
        	NisugataComprtnent.makeNisugataOfFarm(nisugata.nisugataId, farmId);
        }else{						//更新
        	nisugata = NisugataComprtnent.updateNisugata(nisugataId, nisugataName, nisugataKind, capacity, kingaku);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】荷姿情報削除処理
     * @return
     */
    public static Result deleteNisugata() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double nisugataId = Double.parseDouble(input.get("nisugataId").asText());

        /* 荷姿情報を削除する */
        NisugataComprtnent.deleteNisugata(nisugataId, farmId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
