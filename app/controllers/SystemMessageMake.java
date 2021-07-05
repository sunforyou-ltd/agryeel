package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import models.Account;
import models.MessageOfAccount;
import models.SystemMessage;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.FarmComprtnent;

import consts.AgryeelConst;

public class SystemMessageMake extends Controller {

    public static Result systemMessageMakeMove() {
        return ok(views.html.systemMessageMake.render(""));
    }

    /**
     * 【AGRYELL】システムメッセージ登録
     * @return システムメッセージ登録結果JSON
     */
    public static Result systemMessageMakeSubmit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode jsonData 	= request().body().asJson();

        int		result		= FarmComprtnent.REGIST_CHECK_SUCCESS;

    		try
    		{
          /*-- 取得した項目をアカウント情報に保存する --*/
          SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");

          /* システムメッセージモデルの作成 */
          SystemMessage systemMessage = new SystemMessage();

          /* システムメッセージ新規作成 */
          try {
            systemMessage.releaseDate = new java.sql.Date(sdf.parse(jsonData.get("updateTime").asText().replace("/", "")).getTime()); //更新日付
          } catch (ParseException e) {
            systemMessage.releaseDate = null;
          }
          systemMessage.message   = jsonData.get("message").asText();       /* メッセージ */

    		  Calendar cal = Calendar.getInstance();
    		  systemMessage.updateTime = new java.sql.Timestamp(cal.getTime().getTime());
          systemMessage.messageKind = AgryeelConst.MessageKind.SYSTEM;  //メッセージ種別をシステムメッセージに設定
    			systemMessage.save();												//新規メッセージの作成

          //----- アカウント情報を取得する -----
          List<Account> accountList = Account.find.orderBy("account_id").findList();
          for (Account account : accountList) {
            MessageOfAccount moa = new MessageOfAccount();
            moa.accountId = account.accountId;
            moa.updateTime = new java.sql.Timestamp(cal.getTime().getTime());
            moa.save();
          }

    			resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
    		}
    		catch (Exception ex) {

    			ex.printStackTrace();
    	        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);
    		}

        return ok(resultJson);
    }
}
