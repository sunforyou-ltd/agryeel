package controllers;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.UserComprtnent;
import compornent.FarmComprtnent;

import consts.AgryeelConst;

public class AccounMake extends Controller {

    public static Result accountMakeMove() {
        return ok(views.html.accountMake.render(""));
    }

    /**
     * 【AGRYEEL】アカウントIDチェック
     * @return アカウントIDチェック結果JSON
     */
    public static Result accountIdCheck() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode 	accountInpput 	= request().body().asJson();
        String 		accountId 		= accountInpput.get("accountId").asText();
        int			result			= UserComprtnent.EXISTS_ACCOUNTCHECK_SUCCESS;

        /** アカウントコンポーネント */
		UserComprtnent accountComprtnent = new UserComprtnent();

		/** アカウント存在チェックを行う */
		result	= accountComprtnent.ExistsAccountCheck(accountId);

        if (result == UserComprtnent.EXISTS_ACCOUNTCHECK_SUCCESS) { 									//アカウント情報が存在しない場合

            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);					//アカウント存在チェックＯＫ

        }
        else {

            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ACCOUNTIDMATCH);				//アカウント存在チェックＮＧ

        }

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】レジストレーションコード存在チェック
     * @return ジストレーションコード存在チェック結果JSON
     */
    public static Result registrationCodeCheck() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode 	registCodeInpput 	= request().body().asJson();
        String 		registCode 			= registCodeInpput.get("registrationCode").asText();
        int			result				= FarmComprtnent.REGIST_CHECK_SUCCESS;

        /** 農場コンポーネント */
        FarmComprtnent farmComprtnent 	= new FarmComprtnent();

		/** レジストレーション認証を行う */
		result	= farmComprtnent.RegistrationCheck(registCode);

        if (result == FarmComprtnent.REGIST_CHECK_NG) {													//レジストレーション認証エラー
            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.REGCODEUNMATCH);
        }
        else {																							//レジストレーション認証成功
            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        }

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アカウント登録上限チェック
     * @return アカウント登録上限チェック結果JSON
     */
    public static Result accountRegistLimitCheck() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode 	registCodeInpput 	= request().body().asJson();
        String 		registCode 			= registCodeInpput.get("registrationCode").asText();
        int			result				= FarmComprtnent.REGIST_CHECK_SUCCESS;

        /** 農場コンポーネント */
        FarmComprtnent farmComprtnent 	= new FarmComprtnent();

		/** アカウント登録数上限チェックを行う */
		result	= farmComprtnent.AccountRegistLimitCheck(registCode);

        if (result == FarmComprtnent.REGIST_CHECK_NG) {													//アカウント上限エラー
            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ACCOUNTLIMMIT);
        }
        else {																							//成功
            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        }

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アカウント作成
     * @return アカウント作成結果JSON
     */
    public static Result accountMake() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode 	resultJson = Json.newObject();

      /* JSONデータを取得 */
      JsonNode jsonData 	= request().body().asJson();
      String 	accountId 	= jsonData.get("accountId").asText();				/* アカウントID */
      String 	password 	= jsonData.get("password").asText();				/* パスワード   */
      String 	accountName = jsonData.get("accountName").asText();				/* 氏名         */
      String 	registCode 	= jsonData.get("registrationCode").asText();		/* レジストレーションコード */
      int		result		= FarmComprtnent.REGIST_CHECK_SUCCESS;

      /* アカウントコンポーネント */
      UserComprtnent accountComprtnent = new UserComprtnent();

      /* 農場コンポーネント */
      FarmComprtnent farmComprtnent 	= new FarmComprtnent();

  		/* レジストレーション認証を行う */
  		result	= farmComprtnent.RegistrationCheck(registCode);

  		double	farmId		= farmComprtnent.farmData.farmId;					//農場ＩＤ

  		/* アカウントの作成を行う */
  		result	= accountComprtnent.MakeAccount(accountId, password, accountName, farmId);

  		if (result == UserComprtnent.MAKE_ACCOUNT_SUCCESS) {
  			/* 圃場グループ条件検索を作成する */
  			result	= accountComprtnent.MakeFieldGroup(accountId);

  			if (result == UserComprtnent.MAKE_FIELDGROUP_SUCCESS) {

  		        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

  			}
  			else {

  		        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);

  			}

  		}
  		else {

  	        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);

  		}

      return ok(resultJson);
    }
}
