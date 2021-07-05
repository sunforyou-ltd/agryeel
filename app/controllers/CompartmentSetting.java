package controllers;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import models.Common;
import models.Compartment;
import models.Farm;
import models.FarmStatus;
import models.Field;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.FieldComprtnent;

import consts.AgryeelConst;

public class CompartmentSetting extends Controller {

    public static Result compartmentSettingMove(double farmId, double kukakuId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.KUKAKUID
                , String.valueOf(kukakuId));										//区画IDをセッションに格納

        return ok(views.html.compartmentSetting.render(""));
    }

    /**
     * 【AGRYEEL】区画情報設定初期表示データ取得
     * @return
     */
    public static Result compartmentSettingInit() {

    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 区画IDの取得 */
        Double kukakuId = Double.parseDouble(session(AgryeelConst.SessionKey.KUKAKUID));
        Farm farm = Farm.getFarm(farmId);
        FarmStatus farmStatus = farm.getFarmStatus();
        DecimalFormat df = new DecimalFormat("#,##0.00");

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("kukakuId"			, kukakuId);							//区画ID
        resultJson.put("areaUnit"            , Common.GetCommonValue(Common.ConstClass.AREAUNIT, farmStatus.areaUnit, true)); //区画面積単位
        if(kukakuId != 0){															//編集の場合
	        /* 生産者ID、区画IDから圃場情報を取得する */
	        Compartment kukakuData = Compartment.getCompartmentInfo(kukakuId);
	        if (kukakuData != null) {												//生産物グループ情報が存在する場合
	            resultJson.put("kukakuName"				, kukakuData.kukakuName);	//区画名
	            resultJson.put("fieldId"				, kukakuData.fieldId);		//圃場ID
	            resultJson.put("fieldName"				, Field.getFieldName(kukakuData.fieldId));	//区画名
              double area = kukakuData.area;
              if (farmStatus.areaUnit == 1) { //平方メートルの場合
                area = area * 100;
              }
              else if (farmStatus.areaUnit == 2) { //坪の場合
                area = area * 30.25;
              }
              resultJson.put("area"                , df.format(area));                        //区画面積
	            resultJson.put("soilQuality"			, kukakuData.soilQuality);	//土質
	            resultJson.put("frontage"				, kukakuData.frontage);		//間口
	            resultJson.put("depth"					, kukakuData.depth);		//奥行
	            resultJson.put("kansuiMethod"			, kukakuData.kansuiMethod);	//潅水方法
	            resultJson.put("kansuiRyo"				, kukakuData.kansuiRyo);	//潅水量
	            resultJson.put("kansuiTime"				, kukakuData.kansuiTime);	//潅水時間
	            resultJson.put("kansuiOrder"			, kukakuData.kansuiOrder);	//潅水順番
	            resultJson.put("kukakuKind"				, kukakuData.kukakuKind);	//区画種別
	            if(kukakuData.houseName != null){
		            resultJson.put("houseName"			, kukakuData.houseName);	//ハウス名
	            }else{
		            resultJson.put("houseName"			, "");						//ハウス名
	            }
	            resultJson.put("kingaku"				, kukakuData.kingaku);		//金額
	            //購入日
	            String purchaseday = "";
	            if(String.valueOf(kukakuData.purchaseDate) != "null"){
	            	purchaseday = dateFormat.format(kukakuData.purchaseDate);
	                if(purchaseday.equals("1900-01-01")){
	                	purchaseday = null;
	                }
	            }else{
	            	purchaseday = null;
	            }
	            resultJson.put("purchaseDate"			, purchaseday);
	            resultJson.put("serviceLife"			, kukakuData.serviceLife);	//耐用年数
              resultJson.put("sequenceId"       , kukakuData.sequenceId);   //並び順
              resultJson.put("lat"              , kukakuData.lat);          //緯度
              resultJson.put("lng"              , kukakuData.lng);          //経度
	        }
	        else {
	            return notFound();													//圃場情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】区画情報登録/更新実行
     * @return 区画登録/更新結果JSON
     */
    public static Result submitCompartment() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Compartment kukaku = null;

        JsonNode 	kukakuInput 	= request().body().asJson();
        Double		kukakuId = Double.parseDouble(kukakuInput.get("kukakuId").asText());

        if(kukakuId == 0){		//登録
        	kukaku = FieldComprtnent.makeCompartment(kukakuInput);
        }else{						//更新
        	kukaku = FieldComprtnent.updateCompartment(kukakuInput);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】区画情報削除処理
     * @return
     */
    public static Result deleteCompartment() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double kukakuId = Double.parseDouble(input.get("kukakuId").asText());

        /* 区画情報を削除する */
        FieldComprtnent.deleteCompartment(kukakuId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
