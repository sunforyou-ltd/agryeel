package controllers;

import models.Size;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SizeComprtnent;

import consts.AgryeelConst;

public class SizeSetting extends Controller {

    public static Result sizeSettingMove(double farmId, double sizeId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.SIZEID
                , String.valueOf(sizeId));										//サイズIDをセッションに格納

        return ok(views.html.sizeSetting.render(""));
    }

    /**
     * 【AGRYEEL】サイズ情報設定初期表示データ取得
     * @return
     */
    public static Result sizeSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* サイズIDの取得 */
        Double sizeId = Double.parseDouble(session(AgryeelConst.SessionKey.SIZEID));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("sizeId"			, sizeId);									//サイズID
        if(sizeId != 0){															//編集の場合
	        /* サイズIDからサイズ情報を取得する */
	        Size sizeData = Size.getSizeInfo(sizeId);
	        if (sizeData != null) {													//サイズ情報が存在する場合
	            resultJson.put("sizeName"	, sizeData.sizeName);					//サイズ名
	        }
	        else {
	            return notFound();													//サイズ情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】サイズ情報登録/更新実行
     * @return サイズ登録/更新結果JSON
     */
    public static Result submitSize() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Size size = null;

        JsonNode 	sizeInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(sizeInput.get("farmId").asText());
        Double		sizeId = Double.parseDouble(sizeInput.get("sizeId").asText());
        String 		sizeName = sizeInput.get("sizeName").asText();

        if(sizeId == 0){			//登録
        	size = SizeComprtnent.makeSize(sizeName);

            //----- 生産者別サイズを作成します -----
        	SizeComprtnent.makeSizeOfFarm(size.sizeId, farmId);
        }else{						//更新
        	size = SizeComprtnent.updateSize(sizeId, sizeName);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】サイズ情報削除処理
     * @return
     */
    public static Result deleteSize() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double sizeId = Double.parseDouble(input.get("sizeId").asText());

        /* サイズ情報を削除する */
        SizeComprtnent.deleteSize(sizeId, farmId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
