package controllers;

import java.text.DecimalFormat;

import models.Youki;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.YoukiComprtnent;

import consts.AgryeelConst;

public class YoukiSetting extends Controller {

    public static Result youkiSettingMove(double farmId, double youkiId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.YOUKIID
                , String.valueOf(youkiId));										//容器IDをセッションに格納

        return ok(views.html.youkiSetting.render(""));
    }

    /**
     * 【AGRYEEL】容器情報設定初期表示データ取得
     * @return
     */
    public static Result youkiSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 容器IDの取得 */
        Double youkiId = Double.parseDouble(session(AgryeelConst.SessionKey.YOUKIID));

        DecimalFormat df = new DecimalFormat("###0");

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("youkiId"			, youkiId);								//容器ID
        if(youkiId != 0){															//編集の場合
	        /* 容器IDから容器情報を取得する */
	        Youki youkiData = Youki.getYoukiInfo(youkiId);
	        if (youkiData != null) {												//容器情報が存在する場合
	            resultJson.put("youkiName"				, youkiData.youkiName);		//容器名
	            resultJson.put("youkiKind"				, youkiData.youkiKind);		//容器種別
	            resultJson.put("unitKind"				, youkiData.unitKind);		//単位種別
	            resultJson.put("kosu"					, df.format(youkiData.kosu));	//個数
	            resultJson.put("kingaku"				, youkiData.kingaku);		//金額
	        }
	        else {
	            return notFound();													//農肥情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】容器情報登録/更新実行
     * @return 容器登録/更新結果JSON
     */
    public static Result submitYouki() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Youki youki = null;

        JsonNode 	youkiInput 	= request().body().asJson();
        Double		youkiId = Double.parseDouble(youkiInput.get("youkiId").asText());

        if(youkiId == 0){			//登録
        	youki = YoukiComprtnent.makeYouki(youkiInput);
        }else{						//更新
        	youki = YoukiComprtnent.updateYouki(youkiInput);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】容器情報削除処理
     * @return
     */
    public static Result deleteYouki() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double youkiId = Double.parseDouble(input.get("youkiId").asText());

        /* 容器情報を削除する */
        YoukiComprtnent.deleteYouki(youkiId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
