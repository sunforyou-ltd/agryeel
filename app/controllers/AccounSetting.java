package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import models.Account;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.StringU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;

import consts.AgryeelConst;
public class AccounSetting extends Controller {

    @Security.Authenticated(SessionCheckComponent.class)
    public static Result accountSettingMove(double farmId, String accountId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.ACCOUNTID_SEL
                , String.valueOf(accountId));									//アカウントIDをセッションに格納

        return ok(views.html.accountSetting.render(""));
    }

    /**
     * 【AGRYEEL】アカウント情報設定初期表示データ取得
     * @return
     */
    public static Result accountSettingInit() {

    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        resultJson.put("farmId", session(AgryeelConst.SessionKey.FARMID));

        /* アカウントIDの取得 */
        resultJson.put("accountId", session(AgryeelConst.SessionKey.ACCOUNTID_SEL));

        /*自身の権限を取得*/
        UserComprtnent accountComprtnent = new UserComprtnent();
        accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        resultJson.put("selfrole" , accountComprtnent.accountData.managerRole); //権限
        resultJson.put("contractplan" , accountComprtnent.farmStatusData.contractPlan); //契約プラン

        /* アカウントIDからアカウント情報を取得する */
        Account accountData = Account.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID_SEL)).findUnique();
        if (accountData != null) {														//アカウント情報が存在する場合
            resultJson.put("password"			, accountData.password);				//パスワード
            resultJson.put("acountName"			, accountData.acountName);				//氏名
            if(accountData.acountKana != null){
	            resultJson.put("acountKana"				, accountData.acountKana);		//かな
            }else{
	            resultJson.put("acountKana"				, "");							//かな
            }
            if(accountData.remark != null){
	            resultJson.put("remark"				, accountData.remark);				//備考
            }else{
	            resultJson.put("remark"				, "");								//備考
            }
            if(accountData.mailAddress != null){
	            resultJson.put("mailAddress"				, accountData.mailAddress);	//メールアドレス
            }else{
	            resultJson.put("mailAddress"				, "");						//メールアドレス
            }
            //生年月日
            String birthday = "";
            if(String.valueOf(accountData.birthday) != "null"){
            	birthday = dateFormat.format(accountData.birthday);
            }else{
            	birthday = "1990-01-01";
            }
            resultJson.put("birthday"			, birthday);
            resultJson.put("managerRole"		, accountData.managerRole);				//管理者権限
            resultJson.put("firstPage"			, accountData.firstPage);				//初期表示ページ
            resultJson.put("heartRateUpLimit"	, accountData.heartRateUpLimit);		//心拍数上限
            resultJson.put("heartRateDownLimit"	, accountData.heartRateDownLimit);		//心拍数下限

            //---------- アカウントステータス情報の取得 ----------
            UserComprtnent uc = new UserComprtnent();
            uc.GetAccountData(accountData.accountId);
            resultJson.put("workTargetDisplay"    , uc.accountStatusData.workTargetDisplay);       //作業対象表示
            resultJson.put("workCommitAfter"      , uc.accountStatusData.workCommitAfter);         //作業記録後
            resultJson.put("displayChain"         , uc.accountStatusData.displayChain);            //ワークチェーン表示方法
            resultJson.put("nisugataRireki"         , uc.accountStatusData.nisugataRireki);        //荷姿履歴値参照
            resultJson.put("compartmentStatusSkip"  , uc.accountStatusData.compartmentStatusSkip); //区画状況照会SKIP
            resultJson.put("workDateAutoSet"        , uc.accountStatusData.workDateAutoSet);       //作業日付自動設定
            resultJson.put("workStartPrompt"        , uc.accountStatusData.workStartPrompt);       //作業開始確認
            resultJson.put("workChangeDisplay"      , uc.accountStatusData.workChangeDisplay);     //作業切替表示
            resultJson.put("radius"                 , uc.accountStatusData.radius);                //付近区画距離
            resultJson.put("workPlanInitId"         , uc.accountStatusData.workPlanInitId);        //作業指示初期担当者
            resultJson.put("workDiaryDiscription"   , uc.accountStatusData.workDiaryDiscription);  //作業記録注釈

	        /* メニュー権限リストの取得 */
	        listJson   = Json.newObject();

	        for (int idx=0; idx < 32; idx++) {											//設定済メニュー権限情報をJSONデータに格納する

	            if(AgryeelConst.AccountSetting.menuRoleNme[idx] == ""){
	            	break;
	            }
	            if((accountData.menuRole & AgryeelConst.AccountSetting.menuRolePtn[idx]) == 0){
	            	break;
	            }
	            ObjectNode fieldJson	= Json.newObject();
	            fieldJson.put("menuId"		, idx);											//メニューID
	            fieldJson.put("menuName"	, AgryeelConst.AccountSetting.menuRoleNme[idx]);//メニュー名

	            listJson.put(String.valueOf(idx), fieldJson);

	        }
	        resultJson.put(AgryeelConst.AccountSetting.DataList.MENUROLE, listJson);
        }
        else {
            return notFound();															//アカウント情報が存在しない場合、エラーとする
        }

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アカウントID選択処理
     * @return
     */
    public static Result selectAccount() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        String accountID = input.get("accountId").asText();		//アカウントIDの取得

        //セッションに選択アカウントIDを保存
        session(AgryeelConst.SessionKey.ACCOUNTID_SEL, StringU.setNullTrim(accountID));

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アカウント情報保存処理
     * @return
     */
    public static Result submitAccount() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double	farmId = Double.parseDouble(input.get("farmId").asText());
        String	accountId = input.get("accountId").asText();

        /*-- 取得した項目をアカウント情報に保存する --*/
        SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");

        Account accountData = Account.find.where().eq("account_id", accountId).findUnique();
        accountData.password		=	input.get("password").asText();												//パスワード
        accountData.acountName		=	input.get("accountName").asText();											//氏名
        accountData.acountKana		=	input.get("accountKana").asText();											//かな
        accountData.remark			=	input.get("remark").asText();												//備考
        accountData.mailAddress		=	input.get("mailAddress").asText();											//メールアドレス
        try {
        	accountData.birthday	=	new java.sql.Date(sdf.parse(input.get("birthday").asText().replace("/", "")).getTime());	//生年月日
        } catch (ParseException e) {
        	accountData.birthday	=	null;
        }
      	accountData.managerRole		=	Integer.parseInt(input.get("managerRole").asText());						//管理者権限
        /* メニュー権限リストの取得 */
        String menuString = input.get("menuRole").asText();
        Logger.info("menuRole [" + menuString + "]");
        String[] menuRoles = menuString.split(",");

        accountData.menuRole = 0;
        for (int index = 0; index < menuRoles.length; index++) {

            int  id = Integer.parseInt(menuRoles[index]);
            accountData.menuRole = accountData.menuRole | AgryeelConst.AccountSetting.menuRolePtn[id];
        }
      	accountData.firstPage		=	Integer.parseInt(input.get("firstPage").asText());							//初期表示ページ
        accountData.heartRateUpLimit = Integer.parseInt(input.get("heartRateUpLimit").asText());					//心拍数上限
        accountData.heartRateDownLimit = Integer.parseInt(input.get("heartRateDownLimit").asText());				//心拍数下限

        accountData.update();																						//アカウント情報更新

        //---------- アカウントステータス情報の更新 ----------
        UserComprtnent uc = new UserComprtnent();
        uc.GetAccountData(accountData.accountId);
        uc.accountStatusData.workTargetDisplay      = input.get("workTargetDisplay").asInt();
        uc.accountStatusData.workCommitAfter        = input.get("workCommitAfter").asInt();
        uc.accountStatusData.displayChain           = input.get("displayChain").asInt();
        uc.accountStatusData.nisugataRireki         = input.get("nisugataRireki").asInt();
        uc.accountStatusData.compartmentStatusSkip  = input.get("compartmentStatusSkip").asInt();
        uc.accountStatusData.workDateAutoSet        = input.get("workDateAutoSet").asInt();
        uc.accountStatusData.workStartPrompt        = input.get("workStartPrompt").asInt();
        uc.accountStatusData.workChangeDisplay      = input.get("workChangeDisplay").asInt();
        uc.accountStatusData.radius                 = input.get("radius").asInt();
        uc.accountStatusData.workPlanInitId         = input.get("workPlanInitId").asInt();
        uc.accountStatusData.workDiaryDiscription   = input.get("workDiaryDiscription").asInt();
        uc.accountStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アカウント情報削除処理
     * @return
     */
    public static Result deleteAccount() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        String	accountId = input.get("accountId").asText();

        /* アカウント情報を削除する */
        //Ebean.createSqlUpdate("DELETE FROM account WHERE account_id = '" + accountId +"'").execute();

        /* アカウントステータス情報を削除する */
        //Ebean.createSqlUpdate("DELETE FROM account_status WHERE account_id = '" + accountId +"'").execute();

        Account accountData = Account.find.where().eq("account_id", accountId).findUnique();
        accountData.deleteFlag = 1;
        accountData.update();																						//アカウント情報更新

        //---------- アカウントステータス情報の更新 ----------
        UserComprtnent uc = new UserComprtnent();
        uc.GetAccountData(accountData.accountId);
        uc.accountStatusData.deleteFlag = 1;
        uc.accountStatusData.update();

        //---------- アカウントス育苗テータス情報の更新 ----------
        uc.accountIkubyoStatusData.deleteFlag = 1;
        uc.accountIkubyoStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }


    /**
     * 【AGRYEEL】アカウント情報戻し処理
     * @return
     */
    public static Result returnAccount() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        //セッションにログインアカウントIDを保存
        session(AgryeelConst.SessionKey.ACCOUNTID_SEL, session(AgryeelConst.SessionKey.ACCOUNTID));

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
