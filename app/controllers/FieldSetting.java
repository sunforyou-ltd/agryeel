package controllers;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import models.Common;
import models.Compartment;
import models.Farm;
import models.FarmStatus;
import models.Field;
import models.Landlord;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.FieldComprtnent;

import consts.AgryeelConst;

public class FieldSetting extends Controller {

    public static Result fieldSettingMove(double farmId, double fieldId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.FIELDID
                , String.valueOf(fieldId));										//圃場IDをセッションに格納

        return ok(views.html.fieldSetting.render(""));
    }

    /**
     * 【AGRYEEL】圃場情報設定初期表示データ取得
     * @return
     */
    public static Result fieldSettingInit() {

    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 圃場IDの取得 */
        Double fieldId = Double.parseDouble(session(AgryeelConst.SessionKey.FIELDID));

        Farm farm = Farm.getFarm(farmId);
        FarmStatus farmStatus = farm.getFarmStatus();
        DecimalFormat df = new DecimalFormat("#,##0.00");

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("fieldId"			, fieldId);								//圃場ID
        resultJson.put("areaUnit"            , Common.GetCommonValue(Common.ConstClass.AREAUNIT, farmStatus.areaUnit, true)); //区画面積単位
        if(fieldId != 0){															//編集の場合
	        /* 生産者ID、圃場IDから圃場情報を取得する */
	        Field fieldData = Field.getFieldInfo(fieldId);
	        if (fieldData != null) {												//圃場情報が存在する場合
	            resultJson.put("fieldName"				, fieldData.fieldName);		//圃場名
	            resultJson.put("landlordId"				, fieldData.landlordId);	//地主ID
	            if(fieldData.landlordId != 0){
	            	resultJson.put("landlordName"			, Landlord.getLandlordName(fieldData.landlordId));	//地主ID
	            }else{
	            	resultJson.put("landlordName"			, "未選択");			//地主ID
	            }
	            if(fieldData.postNo != null){
		            resultJson.put("postNo"				, fieldData.postNo);		//郵便番号
	            }else{
		            resultJson.put("postNo"				, "");						//郵便番号
	            }
	            if(fieldData.prefectures != null){
		            resultJson.put("prefectures"		, fieldData.prefectures);	//都道府県
	            }else{
		            resultJson.put("prefectures"		, "");						//都道府県
	            }
	            if(fieldData.address != null){
	            	resultJson.put("address"			, fieldData.address);		//市町村など
	            }else{
	            	resultJson.put("address"			, "");						//市町村など
	            }
	            resultJson.put("geography"				, fieldData.geography);		//地目

	            double area = fieldData.area;
	            if (farmStatus.areaUnit == 1) { //平方メートルの場合
	              area = area * 100;
	            }
	            else if (farmStatus.areaUnit == 2) { //坪の場合
	              area = area * 30.25;
	            }
	            resultJson.put("area"                , df.format(area));                        //区画面積

	            resultJson.put("soilQuality"			, fieldData.soilQuality);	//土質
	            /* 契約日 */
	            String contractday = "";
	            if(String.valueOf(fieldData.contractDate) != "null"){
	            	contractday = dateFormat.format(fieldData.contractDate);
	            }else{
	            	contractday = "2015-01-01";
	            }
	            resultJson.put("contractDate"			, contractday);
	            /* 契約終了日 */
	            String contractEndday = "";
	            if(String.valueOf(fieldData.contractEndDate) != "null"){
	            	contractEndday = dateFormat.format(fieldData.contractEndDate);
	            }else{
	            	contractEndday = "2015-01-01";
	            }
	            resultJson.put("contractEndDate"		, contractEndday);
	            resultJson.put("contractType"			, fieldData.contractType);	//契約形態
	            resultJson.put("rent"					, fieldData.rent);			//賃借料

		        /* 地主リストの取得 */
		        listJson   = Json.newObject();
		        List<Landlord> itemList = Landlord.getLandlordOfFarm(farmId);

		        for (Landlord itemData : itemList) {								//圃場情報をJSONデータに格納する

		            ObjectNode landlordJson	= Json.newObject();
		            landlordJson.put("fieldId"			, itemData.landlordId);		//地主ID
		            landlordJson.put("fieldName"		, itemData.landlordName);	//地主名

		            listJson.put(String.valueOf(itemData.landlordId), landlordJson);

		        }
		        resultJson.put(AgryeelConst.Field.DataList.LANDLORDLIST, listJson);

		        /* 圃場に所属する区画リストの取得 */
		        listJson   = Json.newObject();
		        List<Compartment> aryCompartment = Compartment.find.where().eq("farm_id", farmId).eq("field_id", fieldId).orderBy("kukaku_id asc").findList();

		        for (Compartment kukakuData : aryCompartment) {										//圃場情報をJSONデータに格納する

		            ObjectNode fieldJson	= Json.newObject();
		            fieldJson.put("kukakuId"			, kukakuData.kukakuId);		//圃場ID
		            fieldJson.put("kukakuName"		, kukakuData.kukakuName);		//圃場名

		            listJson.put(String.valueOf(kukakuData.kukakuId), fieldJson);

		        }
		        resultJson.put(AgryeelConst.Field.DataList.KUKAKULIST, listJson);
	        }
	        else {
	            return notFound();													//圃場情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】圃場情報登録/更新実行
     * @return 圃場登録/更新結果JSON
     */
    public static Result submitField() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Field field = null;

        JsonNode 	fieldInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(fieldInput.get("farmId").asText());
        Double		fieldId = Double.parseDouble(fieldInput.get("fieldId").asText());

        if(fieldId == 0){		//登録
        	field = FieldComprtnent.makeField(fieldInput);
        }else{						//更新
        	field = FieldComprtnent.updateField(fieldInput);

            //----- 区画情報を作成します -----
            String kukakuString = fieldInput.get("compartment").asText();
            Logger.info("compartment [" + kukakuString + "]");
            String[] kukakus = kukakuString.split(",");
            FieldComprtnent.updateCropCompartmentField(farmId, fieldId, kukakus);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】圃場情報削除処理
     * @return
     */
    public static Result deleteField() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double fieldId = Double.parseDouble(input.get("fieldId").asText());

        /* 地主情報を削除する */
        FieldComprtnent.deleteField(farmId, fieldId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
