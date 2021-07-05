package controllers;

import java.text.SimpleDateFormat;

import models.Sizai;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SizaiComprtnent;

import consts.AgryeelConst;

public class SizaiSetting extends Controller {

    public static Result sizaiSettingMove(double farmId, double sizaiId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));									//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.SIZAIID
                , String.valueOf(sizaiId));									//資材IDをセッションに格納

        return ok(views.html.sizaiSetting.render(""));
    }

    /**
     * 【AGRYEEL】資材情報設定初期表示データ取得
     * @return
     */
    public static Result sizaiSettingInit() {

    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 資材IDの取得 */
        Double sizaiId = Double.parseDouble(session(AgryeelConst.SessionKey.SIZAIID));

        resultJson.put("farmId"				, farmId);							//生産者ID
        resultJson.put("sizaiId"			, sizaiId);							//資材ID
        if(sizaiId != 0){														//編集の場合
	        /* 資材IDから資材情報を取得する */
	        Sizai sizaiData = Sizai.getSizaiInfo(sizaiId);
	        if (sizaiData != null) {											//資材情報が存在する場合
	            resultJson.put("sizaiName"	, sizaiData.sizaiName);				//資材名
	            resultJson.put("sizaiKind"	, sizaiData.sizaiKind);				//資材種別
	            resultJson.put("unitKind"	, sizaiData.unitKind);				//単位種別
	            resultJson.put("ryo"		, sizaiData.ryo);					//量
	            resultJson.put("kingaku"	, sizaiData.kingaku);				//金額
	            //購入日
	            String purchaseday = "";
	            if(String.valueOf(sizaiData.purchaseDate) != "null"){
	            	purchaseday = dateFormat.format(sizaiData.purchaseDate);
	                if(purchaseday.equals("1900-01-01")){
	                	purchaseday = null;
	                }
	            }else{
	            	purchaseday = null;
	            }
	            resultJson.put("purchaseDate"			, purchaseday);
	            resultJson.put("serviceLife"			, sizaiData.serviceLife);	//耐用年数
	        }
	        else {
	            return notFound();													//資材情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】資材情報登録/更新実行
     * @return 資材登録/更新結果JSON
     */
    public static Result submitSizai() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Sizai sizai = null;

        JsonNode 	sizaiInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(sizaiInput.get("farmId").asText());
        Double		sizaiId = Double.parseDouble(sizaiInput.get("sizaiId").asText());

        if(sizaiId == 0){			//登録
        	sizai = SizaiComprtnent.makeSizai(sizaiInput);

            //----- 生産者別資材を作成します -----
        	SizaiComprtnent.makeSizaiOfFarm(sizai.sizaiId, farmId);
        }else{						//更新
        	sizai = SizaiComprtnent.updateSizai(sizaiInput);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】資材情報削除処理
     * @return
     */
    public static Result deleteSizai() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double sizaiId = Double.parseDouble(input.get("sizaiId").asText());

        /* 資材情報を削除する */
        SizaiComprtnent.deleteSizai(sizaiId, farmId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
