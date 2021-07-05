package controllers;

import java.text.DecimalFormat;

import models.Nouhi;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.NouhiComprtnent;

import consts.AgryeelConst;

public class NouhiSetting extends Controller {

    public static Result nouhiSettingMove(double farmId, double nouhiId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.NOUHIID
                , String.valueOf(nouhiId));										//農肥IDをセッションに格納

        return ok(views.html.nouhiSetting.render(""));
    }

    /**
     * 【AGRYEEL】農肥情報設定初期表示データ取得
     * @return
     */
    public static Result nouhiSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 農肥IDの取得 */
        Double nouhiId = Double.parseDouble(session(AgryeelConst.SessionKey.NOUHIID));

        DecimalFormat df = new DecimalFormat("###0");

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("nouhiId"			, nouhiId);								//農肥ID
        if(nouhiId != 0){															//編集の場合
	        /* 農肥IDから農肥情報を取得する */
	        Nouhi nouhiData = Nouhi.getNouhiInfo(nouhiId);
	        if (nouhiData != null) {												//農肥情報が存在する場合
	            resultJson.put("nouhiName"				, nouhiData.nouhiName);		//農肥名
	            resultJson.put("nouhiKind"				, nouhiData.nouhiKind);		//農肥種別
	            resultJson.put("bairitu"				, nouhiData.bairitu);		//倍率
	            resultJson.put("sanpuryo"				, df.format(nouhiData.sanpuryo));		//散布量
	            resultJson.put("unitKind"				, nouhiData.unitKind);		//単位種別
	            resultJson.put("n"						  , nouhiData.n);				//N
	            resultJson.put("p"						  , nouhiData.p);				//P
	            resultJson.put("k"						  , nouhiData.k);				//K
              resultJson.put("mg"             , nouhiData.mg);      //Mg
	            resultJson.put("lower"					, nouhiData.lower);			//倍率下限
	            resultJson.put("upper"					, nouhiData.upper);			//倍率上限
	            resultJson.put("finalDay"				, nouhiData.finalDay);		//最終経過日数
	            resultJson.put("sanpuCount"				, nouhiData.sanpuCount);	//散布回数
	            resultJson.put("useWhen"				, nouhiData.useWhen);		//使用時期
	            resultJson.put("kingaku"				, nouhiData.kingaku);		//金額
	            resultJson.put("nouhiOfficialName"		, nouhiData.nouhiOfficialName);	//農肥正式名
	            resultJson.put("registNumber"			, nouhiData.registNumber);		//登録番号

	        }
	        else {
	            return notFound();													//農肥情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】農肥情報登録/更新実行
     * @return 農肥登録/更新結果JSON
     */
    public static Result submitNouhi() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Nouhi nouhi = null;

        JsonNode 	nouhiInput 	= request().body().asJson();
        Double		nouhiId = Double.parseDouble(nouhiInput.get("nouhiId").asText());

        if(nouhiId == 0){		//登録
        	nouhi = NouhiComprtnent.makeNouhi(nouhiInput);
        }else{						//更新
        	nouhi = NouhiComprtnent.updateNouhi(nouhiInput);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】農肥情報削除処理
     * @return
     */
    public static Result deleteNouhi() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double nouhiId = Double.parseDouble(input.get("nouhiId").asText());

        /* 農肥情報を削除する */
        NouhiComprtnent.deleteNouhi(nouhiId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
