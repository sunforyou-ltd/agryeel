package controllers;

import java.util.List;

import models.Crop;
import models.Hinsyu;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CropComprtnent;

import consts.AgryeelConst;

public class CropSetting extends Controller {

    public static Result cropSettingMove(double farmId, double cropId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.CROPID
                , String.valueOf(cropId));								//圃場グループIDをセッションに格納

        return ok(views.html.cropSetting.render(""));
    }

    /**
     * 【AGRYEEL】生産物情報設定初期表示データ取得
     * @return
     */
    public static Result cropSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 生産物IDの取得 */
        Double cropId = Double.parseDouble(session(AgryeelConst.SessionKey.CROPID));

        resultJson.put("farmId"	, farmId);											//生産者ID
        resultJson.put("cropId"	, cropId);											//生産物ID
        if(cropId != 0){															//編集の場合
	        /* 生産物情報を取得する */
        	Crop cropData = Crop.getCropInfo(cropId);
	        if (cropData != null) {													//生産物情報が存在する場合
	            resultJson.put("cropName"	, cropData.cropName);					//生産物名
	            resultJson.put("cropColor"	, "#" + cropData.cropColor);			//生産物名カラー
	        }
	        else {
	            return notFound();													//生産物情報が存在しない場合、エラーとする
	        }

	        /* 生産物に所属する種リストの取得 */
	        listJson   = Json.newObject();
	        List<Hinsyu> itemList = Hinsyu.getHinsyuOfCrop(cropId);

	        for (Hinsyu itemData : itemList) {										//圃場情報をJSONデータに格納する

	            ObjectNode hinsyuJson	= Json.newObject();
	            hinsyuJson.put("hinsyuId"		, itemData.hinsyuId);				//品種ID
	            hinsyuJson.put("hinsyuName"		, itemData.hinsyuName);				//品種名

	            listJson.put(String.valueOf(itemData.hinsyuId), hinsyuJson);

	        }
	        resultJson.put(AgryeelConst.Crop.DataList.HINSYU, listJson);
        }else{
            resultJson.put("cropColor"	, "#e53935");								//生産物名カラー
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】生産物情報登録/更新実行
     * @return 生産物登録/更新結果JSON
     */
    public static Result submitCrop() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Crop crop = null;

        JsonNode 	cropInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(cropInput.get("farmId").asText());
        Double		cropId = Double.parseDouble(cropInput.get("cropId").asText());
        String 		cropName = cropInput.get("cropName").asText();
        String 		cropColor = cropInput.get("cropColor").asText();

        if(cropId == 0){		//登録
        	crop = CropComprtnent.makeCrop(cropName, cropColor);
        }else{						//更新
        	crop = CropComprtnent.updateCrop(cropId, cropName, cropColor);
        }

        //----- 品種情報を更新します -----
        String hinsyuString = cropInput.get("hinsyu").asText();
        Logger.info("hinsyus [" + hinsyuString + "]");
        String[] hinsyus = hinsyuString.split(",");
        CropComprtnent.updateHinsyuOfCrop(farmId, hinsyus, crop.cropId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

//    /**
//     * 【AGRYEEL】生産物情報削除処理
//     * @return
//     */
//    public static Result deleteCrop() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//
//        /* JSONデータを取得 */
//        JsonNode input = request().body().asJson();
//        Double cropId = Double.parseDouble(input.get("cropId").asText());
//
//        /* 生産物情報を削除する */
//        CropComprtnent.deleteCrop(cropId);
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//
//        return ok(resultJson);
//    }
}
