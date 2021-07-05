package controllers;

import models.Landlord;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.LandlordComprtnent;

import consts.AgryeelConst;

public class LandlordSetting extends Controller {

    public static Result landlordSettingMove(double farmId, double landlordId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.LANDLORDID
                , String.valueOf(landlordId));									//地主IDをセッションに格納

        return ok(views.html.landlordSetting.render(""));
    }

    /**
     * 【AGRYEEL】地主情報設定初期表示データ取得
     * @return
     */
    public static Result landlordSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 地主IDの取得 */
        Double landlordId = Double.parseDouble(session(AgryeelConst.SessionKey.LANDLORDID));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("landlordId"			, landlordId);							//地主ID
        if(landlordId != 0){														//編集の場合
	        /* 生産者ID、地主IDから地主情報を取得する */
	        Landlord landlordData = Landlord.getLandlordInfo(landlordId);
	        if (landlordData != null) {															//生産物グループ情報が存在する場合
	            resultJson.put("landlordName"			, landlordData.landlordName);			//地主名
	            resultJson.put("postNo"					, landlordData.postNo);					//郵便番号
	            resultJson.put("prefectures"			, landlordData.prefectures);			//都道府県
	            resultJson.put("address"				, landlordData.address);				//市町村など
	            resultJson.put("tel"					, landlordData.tel);					//電話番号
	            resultJson.put("responsibleMobileTel"	, landlordData.responsibleMobileTel);	//責任者携帯電話番号
	            resultJson.put("fax"					, landlordData.fax);					//FAX
	            resultJson.put("mailAddressPC"			, landlordData.mailAddressPC);			//メールアドレス（パソコン）
	            resultJson.put("mailAddressMobile"		, landlordData.mailAddressMobile);		//メールアドレス（携帯）
	            resultJson.put("bankName"				, landlordData.bankName);				//振込先銀行名
	            resultJson.put("accountType"			, landlordData.accountType);			//振込先口座種別
	            resultJson.put("accountNumber"			, landlordData.accountNumber);			//振込先口座番号
	            resultJson.put("paymentDate"			, landlordData.paymentDate);			//支払日
	        }
	        else {
	            return notFound();													//地主情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】地主情報登録/更新実行
     * @return 地主登録/更新結果JSON
     */
    public static Result submitLandlord() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Landlord landlord = null;

        JsonNode 	landlordInput 	= request().body().asJson();
        Double		landlordId = Double.parseDouble(landlordInput.get("landlordId").asText());

        if(landlordId == 0){		//登録
        	landlord = LandlordComprtnent.makeLandlord(landlordInput);
        }else{						//更新
        	landlord = LandlordComprtnent.updateLandlord(landlordInput);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】地主情報削除処理
     * @return
     */
    public static Result deleteLandlord() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double landlordId = Double.parseDouble(input.get("landlordId").asText());

        /* 地主情報を削除する */
        //LandlordComprtnent.deleteLandlord(landlordId);
        Landlord landlordData = Landlord.getLandlordInfo(landlordId);
        landlordData.deleteFlag = 1;
        landlordData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
