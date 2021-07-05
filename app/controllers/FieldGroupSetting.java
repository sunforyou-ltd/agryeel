package controllers;

import java.util.List;

import models.Account;
import models.AccountStatus;
import models.Field;
import models.FieldGroup;
import models.FieldGroupList;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.FieldComprtnent;

import consts.AgryeelConst;

public class FieldGroupSetting extends Controller {

    public static Result fieldGroupSettingMove(double farmId, double fieldGroupId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.FIELDGROUPID
                , String.valueOf(fieldGroupId));								//圃場グループIDをセッションに格納

        return ok(views.html.fieldGroupSetting.render(""));
    }

    /**
     * 【AGRYEEL】圃場グループ情報設定初期表示データ取得
     * @return
     */
    public static Result fieldGroupSettingInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* 圃場グループIDの取得 */
        Double fieldGroupId = Double.parseDouble(session(AgryeelConst.SessionKey.FIELDGROUPID));

        resultJson.put("farmId"				, farmId);						//生産者ID
        resultJson.put("fieldGroupId"		, fieldGroupId);				//圃場グループID
        if(fieldGroupId != 0){														//編集の場合
	        /* 生産者ID、圃場グループIDから圃場グループ情報を取得する */
	        FieldGroup groupData = FieldGroup.getFieldGroup(fieldGroupId);
	        if (groupData != null) {												//圃場グループ情報が存在する場合
	            resultJson.put("fieldGroupName"		, groupData.fieldGroupName);	//圃場グループ名
	            resultJson.put("fieldGroupColor"	, "#" + groupData.fieldGroupColor);	//圃場グループ名カラー
              resultJson.put("fieldGroupSequenceId"  , groupData.sequenceId); //並び順
                resultJson.put("workPlanAutoCreate"  , groupData.workPlanAutoCreate); //作業指示自動生成
	        }
	        else {
	            return notFound();													//圃場グループ情報が存在しない場合、エラーとする
	        }

	        /* 圃場グループに所属する圃場リストの取得 */
	        listJson   = Json.newObject();
	        List<Field> itemList = FieldGroupList.getField(fieldGroupId);

	        for (Field itemData : itemList) {										//圃場情報をJSONデータに格納する

	            ObjectNode fieldJson	= Json.newObject();
	            fieldJson.put("fieldId"			, itemData.fieldId);				//圃場ID
	            fieldJson.put("fieldName"		, itemData.fieldName);				//圃場名

	            listJson.put(String.valueOf(itemData.fieldId), fieldJson);

	        }
	        resultJson.put(AgryeelConst.FieldGroup.DataList.GROUPLIST, listJson);
        }else{
            resultJson.put("fieldGroupColor"	, "#e53935");						//圃場グループ名カラー
            resultJson.put("workPlanAutoCreate"  , 0);								//作業指示自動生成
        }
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】圃場グループ情報登録/更新実行
     * @return 圃場グループ登録/更新結果JSON
     */
    public static Result submitFieldGroup() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();
        FieldGroup fieldGroup = null;

        JsonNode 	fieldInput 	= request().body().asJson();
        Double		farmId = Double.parseDouble(fieldInput.get("farmId").asText());
        Double		fieldGroupId = Double.parseDouble(fieldInput.get("fieldGroupId").asText());
        String 		fieldGroupName = fieldInput.get("fieldGroupName").asText();
        String 		fieldGroupColor = fieldInput.get("fieldGroupColor").asText();
        int       fieldGroupSequenceId = fieldInput.get("fieldGroupSequenceId").asInt();
        short       workPlanAutoCreate = (short)fieldInput.get("workPlanAutoCreate").asInt();

        if(fieldGroupId == 0){		//登録
            fieldGroup = FieldComprtnent.makeFieldGroup(fieldGroupName, fieldGroupColor, farmId, fieldGroupSequenceId, workPlanAutoCreate);
            /* 区画状況照会検索条件に登録グループ追加 */
            List<Account> accounts = Account.getAccountOfFarm(farmId);
            for (Account account: accounts) {
              AccountStatus status = AccountStatus.find.where().eq("account_id", account.accountId).findUnique();
              if(!"".equals(status.selectFieldGroupId)) {
                status.selectFieldGroupId += ",";
              }
              status.selectFieldGroupId += String.valueOf(fieldGroup.fieldGroupId);
              status.update();
            }
        }else{						//更新
            fieldGroup = FieldComprtnent.updateFieldGroup(fieldGroupId, fieldGroupName, fieldGroupColor, farmId, fieldGroupSequenceId, workPlanAutoCreate);
        }

        //----- 圃場グループ明細を作成します -----
        String fieldString = fieldInput.get("field").asText();
        Logger.info("fields [" + fieldString + "]");
        String[] fields = fieldString.split(",");
        FieldComprtnent.updateFieldGroupList(fieldGroup.fieldGroupId, fields);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】圃場グループ情報削除処理
     * @return
     */
    public static Result deleteFieldGroup() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double fieldGroupId = Double.parseDouble(input.get("fieldGroupId").asText());

        /* 圃場グループ情報を削除する */
        FieldComprtnent.deleteFieldGroup(fieldGroupId);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
