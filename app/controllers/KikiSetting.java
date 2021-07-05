package controllers;

import java.text.SimpleDateFormat;

import models.Attachment;
import models.Kiki;

import org.apache.commons.lang3.StringUtils;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.KikiComprtnent;

import consts.AgryeelConst;

public class KikiSetting extends Controller {

    public static Result kikiSettingMove(double farmId, double kikiId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.KIKIID
                , String.valueOf(kikiId));										//機器IDをセッションに格納

        return ok(views.html.kikiSetting.render(""));
    }

    /**
     * 【AGRYEEL】機器情報設定初期表示データ取得
     * @return
     */
    public static Result kikiSettingInit() {

       	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 機器IDの取得 */
        Double kikiId = Double.parseDouble(session(AgryeelConst.SessionKey.KIKIID));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("kikiId"				, kikiId);								//機器ID
        if(kikiId != 0){															//編集の場合
	        /* 機器IDから機器情報を取得する */
	        Kiki kikiData = Kiki.getKikiInfo(kikiId);
	        if (kikiData != null) {													//機器情報が存在する場合
	            resultJson.put("kikiName"	, kikiData.kikiName);					//機器名
	            resultJson.put("katasiki"			, StringUtils.defaultString(kikiData.katasiki));			//型式
	            resultJson.put("maker"				, StringUtils.defaultString(kikiData.maker));				//メーカー
	            resultJson.put("kikiKind"			, kikiData.kikiKind);			//機器種別
	            resultJson.put("kingaku"				, kikiData.kingaku);		//金額
	            //購入日
	            String purchaseday = "";
	            if(String.valueOf(kikiData.purchaseDate) != "null"){
	            	purchaseday = dateFormat.format(kikiData.purchaseDate);
	                if(purchaseday.equals("1900-01-01")){
	                	purchaseday = null;
	                }
	            }else{
	            	purchaseday = null;
	            }
	            resultJson.put("purchaseDate"			, purchaseday);
	            resultJson.put("days"			, kikiData.days);					//日数
	            resultJson.put("serviceLife"			, kikiData.serviceLife);	//耐用年数

		        /* 使用可能アタッチメント種別リストの取得 */
		        listJson   = Json.newObject();
		        if(!StringUtils.isEmpty(kikiData.onUseAttachmentId)){
			        String[] attachs = kikiData.onUseAttachmentId.split(",");

			        for (int index = 0; index < attachs.length; index++) {

			            double  id        = Double.parseDouble(attachs[index]);
			            Attachment attach = Attachment.getAttachmentInfo(id);

			            ObjectNode attachJson	= Json.newObject();
			            attachJson.put("attachmentId"	, attach.attachmentId);			//アタッチメントID
			            attachJson.put("attachementName", attach.attachementName);		//アタッチメント名

			            listJson.put(String.valueOf(attach.attachmentId), attachJson);
			        }
		        }
		        resultJson.put("attachDataList", listJson);
	        }
	        else {
	            return notFound();													//機器情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】機器情報登録/更新実行
     * @return 機器登録/更新結果JSON
     */
    public static Result submitKiki() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Kiki kiki = null;

        JsonNode 	kikiInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(kikiInput.get("farmId").asText());
        Double		kikiId = Double.parseDouble(kikiInput.get("kikiId").asText());

        if(kikiId == 0){			//登録
        	kiki = KikiComprtnent.makeKiki(kikiInput);

            //----- 生産者別機器を作成します -----
            KikiComprtnent.makeKikiOfFarm(kiki.kikiId, farmId);
        }else{						//更新
        	kiki = KikiComprtnent.updateKiki(kikiInput);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】機器情報削除処理
     * @return
     */
    public static Result deleteKiki() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double kikiId = Double.parseDouble(input.get("kikiId").asText());

        /* アタッチメント情報を削除する */
        KikiComprtnent.deleteKiki(kikiId, farmId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
