package controllers;

import java.util.List;

import models.Crop;
import models.CropGroup;
import models.CropGroupList;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CropComprtnent;

import consts.AgryeelConst;

public class CropGroupSetting extends Controller {

    public static Result cropGroupSettingMove(double farmId, double cropGroupId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.CROPGROUP
                , String.valueOf(cropGroupId));									//生産物グループIDをセッションに格納

        return ok(views.html.cropGroupSetting.render(""));
    }

    /**
     * 【AGRYEEL】生産物グループ情報設定初期表示データ取得
     * @return
     */
    public static Result cropGroupSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 生産物グループIDの取得 */
        Double cropGroupId = Double.parseDouble(session(AgryeelConst.SessionKey.CROPGROUP));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("cropGroupId"		, cropGroupId);							//生産物グループID
        if(cropGroupId != 0){														//編集の場合
	        /* 生産者ID、生産物グループIDから生産物グループ情報を取得する */
	        CropGroup groupData = CropGroup.getCropGroup(cropGroupId);
	        if (groupData != null) {												//生産物グループ情報が存在する場合
	            resultJson.put("cropGroupName"		, groupData.cropGroupName);		//生産物グループ名
	        }
	        else {
	            return notFound();													//生産物グループ情報が存在しない場合、エラーとする
	        }

	        /* 生産物グループに所属する圃場リストの取得 */
	        listJson   = Json.newObject();
	        List<Crop> itemList = CropGroupList.getCrop(cropGroupId);

	        for (Crop itemData : itemList) {										//生産物情報をJSONデータに格納する

	            ObjectNode cropJson	= Json.newObject();
	            cropJson.put("cropId"		, itemData.cropId);						//生産物ID
	            cropJson.put("cropName"		, itemData.cropName);					//生産物名

	            listJson.put(String.valueOf(itemData.cropId), cropJson);

	        }
	        resultJson.put(AgryeelConst.CropGroup.DataList.GROUPLIST, listJson);
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】生産物グループ情報登録/更新実行
     * @return 生産物グループ登録/更新結果JSON
     */
    public static Result submitCropGroup() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        CropGroup cropGroup = null;

        JsonNode 	cropInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(cropInput.get("farmId").asText());
        Double		cropGroupId = Double.parseDouble(cropInput.get("cropGroupId").asText());
        String 		cropGroupName = cropInput.get("cropGroupName").asText();

        if(cropGroupId == 0){		//登録
        	cropGroup = CropComprtnent.makeCropGroup(cropGroupName, farmId);
        }else{						//更新
        	cropGroup = CropComprtnent.updateCropGroup(cropGroupId, cropGroupName);
        }

        //----- 生産物グループ明細を作成します -----
        String cropString = cropInput.get("crop").asText();
        Logger.info("crop [" + cropString + "]");
        String[] crops = cropString.split(",");
        CropComprtnent.updateCropGroupList(cropGroup.cropGroupId, crops);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】生産物グループ情報削除処理
     * @return
     */
    public static Result deleteCropGroup() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double cropGroupId = Double.parseDouble(input.get("cropGroupId").asText());

        /* 生産物グループ情報を削除する */
        CropComprtnent.deleteCropGroup(cropGroupId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
