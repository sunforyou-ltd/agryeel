package controllers;

import java.util.UUID;

import models.Account;
import models.Farm;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StringU;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import compornent.SessionCheckComponent;
import compornent.UserComprtnent;
import compornent.FarmComprtnent;

import consts.AgryeelConst;

public class Login extends Controller {

    /**
     * 【AGRYEEL】トップページ
     * @return インデックスレンダー
     */
    public static Result move() {
        return ok(views.html.index.render(""));
    }

    /**
     * 【AGRYEEL】セッションチェック有効時
     * @return インデックスレンダー
     */
    public static Result sessionOK() {

//        int			result			=	UserComprtnent.LOGIN_SUCCESS;
//
//    	//認証成功
//        Account account = SessionCheckComponent.getUserInfoFromSession(request());										//アカウント情報を取り出す
//        if (account != null) {
//            session(AgryeelConst.SessionKey.ACCOUNTID, StringU.setNullTrim(account.accountId));								//アカウントIDをセッションに格納
//            session(AgryeelConst.SessionKey.ACCOUNTNAME, StringU.setNullTrim(account.acountName));							//アカウント名をセッションに格納
//            session(AgryeelConst.SessionKey.ACCOUNTID_SEL, StringU.setNullTrim(account.accountId));							//アカウントID（アカウント情報選択用）をセッションに格納
//            session(AgryeelConst.SessionKey.FARMID, String.valueOf(account.farmId));										//農場IDをセッションに格納
//            session(AgryeelConst.SessionKey.FARMBASEID, String.valueOf(account.farmId));									//農場基本IDをセッションに格納
//            /** 農場コンポーネント */
//            FarmComprtnent farmComprtnent = new FarmComprtnent();
//            /** 農場情報を取得 */
//    		result	= farmComprtnent.GetFarmData(account.farmId);
//
//    		switch (result) {
//    		case FarmComprtnent.GET_ERROR:
//
//    			//農場情報が取得失敗
//                session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(0));											//農場グループIDをセッションに格納
//
//    			break;
//    		default:
//
//                Farm farm = farmComprtnent.farmData;																		//農場情報を取り出す
//                session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(farm.farmGroupId));								//農場グループIDをセッションに格納
//    			break;
//    		}
//
//            return redirect("/menuMove");
//        }

        return redirect("/move");

    }

    /**
     * 【AGRYEEL】アカウント認証
     * @return アカウント認証結果JSON
     */
    public static Result accountLogin() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode 	accountInpput = request().body().asJson();
        String		accountId		=	accountInpput.get("accountId").asText();
        String		password		=	accountInpput.get("password").asText();
        int			result			  =	UserComprtnent.LOGIN_SUCCESS;

        /** アカウントコンポーネント */
    		UserComprtnent accountComprtnent = new UserComprtnent();

    		/** ログイン認証の実行 */
    		result	= accountComprtnent.Login(accountId, password);

    		switch (result) {
    		case UserComprtnent.LOGIN_NOTFOUND:

    			//アカウントＩＤ未存在
                resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
    			break;

    		case UserComprtnent.LOGIN_PASSWORDMISSMATCH:

    			//パスワード不一致
                resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.PASSWORDUNMATCH);
    			break;

    		default:

    			//認証成功
                Account account = accountComprtnent.accountData;																//アカウント情報を取り出す
                session(AgryeelConst.SessionKey.ACCOUNTID, StringU.setNullTrim(account.accountId));								//アカウントIDをセッションに格納
                session(AgryeelConst.SessionKey.ACCOUNTNAME, StringU.setNullTrim(account.acountName));							//アカウント名をセッションに格納
                session(AgryeelConst.SessionKey.ACCOUNTID_SEL, StringU.setNullTrim(account.accountId));							//アカウントID（アカウント情報選択用）をセッションに格納
                session(AgryeelConst.SessionKey.FARMID, String.valueOf(account.farmId));										//農場IDをセッションに格納
                session(AgryeelConst.SessionKey.FARMBASEID, String.valueOf(account.farmId));									//農場基本IDをセッションに格納

                accountComprtnent.LoginCountUP();																				//ログイン回数を加算

          			/*----- セッション情報の生成 -----*/
              	final String userToken = UUID.randomUUID().toString(); 															         /* ランダムトークンの生成 */
                Logger.info("[ MAKE SESSION ] ID:{} NAME:{} TOKEN:{} ", account.accountId, account.acountName, userToken);
              	SessionCheckComponent.registerLoginSession(ctx(), userToken, account);											 /* セッション情報を登録する */

                /** 農場コンポーネント */
                FarmComprtnent farmComprtnent = new FarmComprtnent();
                /** 農場情報を取得 */
            		result	= farmComprtnent.GetFarmData(account.farmId);

            		switch (result) {
            		case FarmComprtnent.GET_ERROR:

            			//農場情報が取得失敗
                        session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(0));											//農場グループIDをセッションに格納

            			break;
            		default:

                        Farm farm = farmComprtnent.farmData;																		//農場情報を取り出す
                        session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(farm.farmGroupId));								//農場グループIDをセッションに格納

                        //アカウント情報の取得
                        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

                        result = accountComprtnent.getAccountJson(resultJson);

                        Logger.info("[ NEW LOGIN ] ID:{} NAME:{} FARM:{} FARMNAME:{}", account.accountId, account.acountName, farm.farmId, farm.farmName);

            			break;
            		}


                resultJson.put("accountId", account.accountId);
                resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

    			break;
    		}

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】Googleログイン認証
     * @return Googleログイン認証結果JSON
     */
    public static Result googleLogin() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode 	accountInpput = request().body().asJson();
        String		googleId		=	accountInpput.get("googleId").asText();
        int			result			=	UserComprtnent.LOGIN_SUCCESS;

        /** アカウントコンポーネント */
		UserComprtnent accountComprtnent = new UserComprtnent();

		/** ログイン認証の実行 */
		result	= accountComprtnent.GetGoogleAccountData(googleId);

		switch (result) {
		case UserComprtnent.LOGIN_NOTFOUND:

			//アカウントＩＤ未存在
            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
			break;

		default:

			//認証成功
            Account account = accountComprtnent.accountData;																//アカウント情報を取り出す
            session(AgryeelConst.SessionKey.ACCOUNTID, StringU.setNullTrim(account.accountId));								//アカウントIDをセッションに格納
            session(AgryeelConst.SessionKey.ACCOUNTNAME, StringU.setNullTrim(account.acountName));							//アカウント名をセッションに格納
            session(AgryeelConst.SessionKey.ACCOUNTID_SEL, StringU.setNullTrim(account.accountId));							//アカウントID（アカウント情報選択用）をセッションに格納
            session(AgryeelConst.SessionKey.FARMID, String.valueOf(account.farmId));										//農場IDをセッションに格納
            session(AgryeelConst.SessionKey.FARMBASEID, String.valueOf(account.farmId));									//農場基本IDをセッションに格納

            /** 農場コンポーネント */
            FarmComprtnent farmComprtnent = new FarmComprtnent();
            /** 農場情報を取得 */
    		result	= farmComprtnent.GetFarmData(account.farmId);

    		switch (result) {
    		case FarmComprtnent.GET_ERROR:

    			//農場情報が取得失敗
                session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(0));											//農場グループIDをセッションに格納

    			break;
    		default:

                Farm farm = farmComprtnent.farmData;																		//農場情報を取り出す
                session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(farm.farmGroupId));								//農場グループIDをセッションに格納
    			break;
    		}

            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

			break;
		}

        return ok(resultJson);
    }
    public static Result hashLogin(String hashValue) {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        int     result      = UserComprtnent.LOGIN_SUCCESS;

        /** アカウントコンポーネント */
        UserComprtnent accountComprtnent = new UserComprtnent();

        /** ログイン認証の実行 */
        result  = accountComprtnent.GetAccountData(hashValue);

        switch (result) {
        case UserComprtnent.GET_SUCCESS:
          /** ログイン認証の実行 */
          result	= accountComprtnent.Login(accountComprtnent.accountData.accountId, accountComprtnent.accountData.password);
          if (result == UserComprtnent.LOGIN_NOTFOUND ||
              result == UserComprtnent.LOGIN_PASSWORDMISSMATCH) {
              //アカウントＩＤ未存在
              //パスワード不一致
              controllers.Application.userLogout();
              resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
              return ok(resultJson);
          }

          //認証成功
          Account account = accountComprtnent.accountData;                                //アカウント情報を取り出す
          session(AgryeelConst.SessionKey.ACCOUNTID, StringU.setNullTrim(account.accountId));               //アカウントIDをセッションに格納
          session(AgryeelConst.SessionKey.ACCOUNTNAME, StringU.setNullTrim(account.acountName));              //アカウント名をセッションに格納
          session(AgryeelConst.SessionKey.ACCOUNTID_SEL, StringU.setNullTrim(account.accountId));             //アカウントID（アカウント情報選択用）をセッションに格納
          session(AgryeelConst.SessionKey.FARMID, String.valueOf(account.farmId));                    //農場IDをセッションに格納
          session(AgryeelConst.SessionKey.FARMBASEID, String.valueOf(account.farmId));                  //農場基本IDをセッションに格納

          accountComprtnent.accountData.hashValue = hashValue;
          accountComprtnent.LoginCountUP();                                       //ログイン回数を加算

          /*----- セッション情報の生成 -----*/
          final String userToken = UUID.randomUUID().toString();                                       /* ランダムトークンの生成 */
          Logger.info("[ MAKE SESSION ] ID:{} NAME:{} TOKEN:{} HASH:{}", account.accountId, account.acountName, userToken, accountComprtnent.accountData.hashValue);
          SessionCheckComponent.registerLoginSession(ctx(), userToken, account);                       /* セッション情報を登録する */

          /** 農場コンポーネント */
          FarmComprtnent farmComprtnent = new FarmComprtnent();
          /** 農場情報を取得 */
          result  = farmComprtnent.GetFarmData(account.farmId);

          switch (result) {
          case FarmComprtnent.GET_ERROR:

            //農場情報が取得失敗
                  session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(0));                      //農場グループIDをセッションに格納

            break;
          default:

                  Farm farm = farmComprtnent.farmData;                                    //農場情報を取り出す
                  session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(farm.farmGroupId));               //農場グループIDをセッションに格納

                  //アカウント情報の取得
                  int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

                  result = accountComprtnent.getAccountJson(resultJson);

                  Logger.info("[ NEW LOGIN ] ID:{} NAME:{} FARM:{} FARMNAME:{}", account.accountId, account.acountName, farm.farmId, farm.farmName);

            break;
          }


          resultJson.put("accountId", account.accountId);
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

          break;

        default:

          //アカウントＩＤ未存在
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
          break;

        }

        return ok(resultJson);
    }
}
