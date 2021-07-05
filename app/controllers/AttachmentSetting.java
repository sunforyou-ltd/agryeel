package controllers;

import models.Attachment;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.AttachmentComprtnent;

import consts.AgryeelConst;

public class AttachmentSetting extends Controller {

    public static Result attachmentSettingMove(double farmId, double attachmentId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.ATTACHMENTID
                , String.valueOf(attachmentId));								//アタッチメントIDをセッションに格納

        return ok(views.html.attachmentSetting.render(""));
    }

    /**
     * 【AGRYEEL】アタッチメント情報設定初期表示データ取得
     * @return
     */
    public static Result attachmentSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* アタッチメントIDの取得 */
        Double attachmentId = Double.parseDouble(session(AgryeelConst.SessionKey.ATTACHMENTID));

        resultJson.put("farmId"				, farmId);								//生産者ID
        resultJson.put("attachmentId"		, attachmentId);						//アタッチメントID
        if(attachmentId != 0){														//編集の場合
	        /* アタッチメントIDからアタッチメント情報を取得する */
	        Attachment attachData = Attachment.getAttachmentInfo(attachmentId);
	        if (attachData != null) {												//生産物グループ情報が存在する場合
	            resultJson.put("attachementName"	, attachData.attachementName);	//アタッチメント名
	            resultJson.put("katasiki"			, attachData.katasiki);			//型式
	            resultJson.put("attachmentKind"		, attachData.attachmentKind);	//アタッチメント種別
	        }
	        else {
	            return notFound();													//アタッチメント情報が存在しない場合、エラーとする
	        }
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アタッチメント情報登録/更新実行
     * @return アタッチメント登録/更新結果JSON
     */
    public static Result submitAttachment() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        Attachment attach = null;

        JsonNode 	attachInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(attachInput.get("farmId").asText());
        Double		attachId = Double.parseDouble(attachInput.get("attachmentId").asText());

        if(attachId == 0){			//登録
        	attach = AttachmentComprtnent.makeAttachment(attachInput);

            //----- 生産者別アタッチメントを作成します -----
            AttachmentComprtnent.makeAttachmentOfFarm(attach.attachmentId, farmId);
        }else{						//更新
        	attach = AttachmentComprtnent.updateAttachment(attachInput);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アタッチメント情報削除処理
     * @return
     */
    public static Result deleteAttachment() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double farmId = Double.parseDouble(input.get("farmId").asText());
        Double attachId = Double.parseDouble(input.get("attachmentId").asText());

        /* アタッチメント情報を削除する */
        AttachmentComprtnent.deleteAttachment(attachId, farmId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
