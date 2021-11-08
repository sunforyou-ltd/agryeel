package controllers;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import models.Account;
import models.AccountStatus;
import models.ActiveLog;
import models.Attachment;
import models.Belto;
import models.BeltoOfFarm;
import models.Common;
import models.Compartment;
import models.CompartmentWorkChainStatus;
import models.Crop;
import models.CropGroup;
import models.CropGroupList;
import models.Farm;
import models.FieldGroup;
import models.Hinsyu;
import models.HinsyuOfFarm;
import models.IkubyoPlan;
import models.IkubyoPlanLine;
import models.Kiki;
import models.KikiOfFarm;
import models.MessageOfAccount;
import models.MotochoBase;
import models.NaeStatus;
import models.Nisugata;
import models.NisugataOfFarm;
import models.Nouhi;
import models.PlanLine;
import models.Shitu;
import models.ShituOfFarm;
import models.Sizai;
import models.Size;
import models.SizeOfFarm;
import models.Soil;
import models.SystemMessage;
import models.TimeLine;
import models.Work;
import models.WorkChain;
import models.WorkChainItem;
import models.WorkDiary;
import models.Youki;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;
import util.ListrU;
import util.StringU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CommentCompornent;
import compornent.CommonComprtnent;
import compornent.CommonWorkDiary;
import compornent.CommonWorkDiaryWork;
import compornent.DachakuCompornent;
import compornent.FarmComprtnent;
import compornent.FieldComprtnent;
import compornent.HaikiCompornent;
import compornent.HashuCompornent;
import compornent.HiryoSanpuCompornent;
import compornent.KaishuCompornent;
import compornent.KansuiCompornent;
import compornent.MabikiCompornent;
import compornent.MotochoCompornent;
import compornent.MultiCompornent;
import compornent.NaehashuCompornent;
import compornent.NaeStatusCompornent;
import compornent.NichoChoseiCompornent;
import compornent.NomalCompornent;
import compornent.NoukouCompornent;
import compornent.SenkaCompornent;
import compornent.SenteiCompornent;
import compornent.ShukakuCompornent;
import compornent.ShukakuofAccount;
import compornent.SizaiComprtnent;
import compornent.TeishokuCompornent;
import compornent.UserComprtnent;
import compornent.WorkChainCompornent;
import compornent.WorktimeAccount;

import consts.AgryeelConst;

/**
 * OpenAPI共通コントローラ
 * @author kimura
 *
 */
public class APIController extends Controller {

    /**
     * 【AGRYEEL】アカウント認証
     * @return アカウント認証結果JSON
     */
    public static Result accountlogin(String accountId, String password) {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        int	result	= UserComprtnent.LOGIN_SUCCESS;

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
		Account account = accountComprtnent.accountData;													//アカウント情報を取り出す
		accountComprtnent.LoginCountUP();															//ログイン回数を加算

		/** 農場コンポーネント */
		FarmComprtnent farmComprtnent = new FarmComprtnent();
		/** 農場情報を取得 */
		result	= farmComprtnent.GetFarmData(account.farmId);

    		switch (result) {
    		case FarmComprtnent.GET_ERROR:

    			//農場情報が取得失敗
			session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(0));										//農場グループIDをセッションに格納
    			break;
    		default:
			Farm farm = farmComprtnent.farmData;														//農場情報を取り出す

			//アカウント情報の取得
			int getAccount = accountComprtnent.GetAccountData(accountId);
			result = accountComprtnent.getAccountJsonAPI(resultJson);
    			break;
    		}

		resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
		break;
	}

        return ok(resultJson);
    }

    /**
     * 全生産者一覧を取得する
     * @return
     */
    public static Result getallfarm() {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

	    List<Farm> farms = Farm.find.orderBy("farm_id asc").findList();

	    if (farms.size() > 0) { //該当データが存在する場合
	      for (Farm farm : farms) {

	        ObjectNode jd = Json.newObject();

	        jd.put("id"   , farm.farmId);
	        jd.put("name" , farm.farmName);
	        jd.put("flag" , 0);

	        listJson.add(jd);
	      }
	    }

	    resultJson.put("datalist" , listJson);
	    return ok(resultJson);
	}

    /**
     * 生産者IDから全作業一覧を取得する
     * @return
     */
    public static Result getallworkoffarm(double pFarmId) {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

	    List<Work> works = Work.getWorkOfFarm(0);

	    if (works.size() > 0) { //該当データが存在する場合
	      for (Work work : works) {
            if (work.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

	        ObjectNode jd = Json.newObject();

	        jd.put("id"   , work.workId);
	        jd.put("name" , work.workName);
	        jd.put("flag" , 0);

	        listJson.add(jd);
	      }
	    }

	    works = Work.getWorkOfFarm(pFarmId);

	    if (works.size() > 0) { //該当データが存在する場合
	      for (Work work : works) {
	        ObjectNode jd = Json.newObject();

	        jd.put("id"   , work.workId);
	        jd.put("name" , work.workName);
	        jd.put("flag" , 0);

	        listJson.add(jd);
	      }
	    }

	    resultJson.put("datalist" , listJson);
	    return ok(resultJson);
	}

    /**
     * 生産者IDから作業一覧を取得する
     * @return
     */
    public static Result getworkoffarm(String accountId) {
      /* 戻り値用JSONデータの生成 */
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ObjectNode resultJson = Json.newObject();
      ArrayNode listJson = mapper.createArrayNode();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(accountId);

      int result = WorkChainCompornent.getWorkOfFarmJsonArray(accountComprtnent.accountData.farmId, accountComprtnent.accountStatusData.selectChainId, listJson, -1, -1, accountComprtnent.accountStatusData.radius);

      resultJson.put("datalist" , listJson);

      return ok(resultJson);
    }
    public static Result getworkoffarmRadius(String accountId, double pLat, double pLng) {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(accountId);

        int result = WorkChainCompornent.getWorkOfFarmJsonArray(accountComprtnent.accountData.farmId, accountComprtnent.accountStatusData.selectChainId, listJson, pLat, pLng, accountComprtnent.accountStatusData.radius);

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }
    /**
     * 生産者IDから育苗作業一覧を取得する
     * @return
     */
    public static Result getworkofikubyo(String accountId) {
      /* 戻り値用JSONデータの生成 */
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ObjectNode resultJson = Json.newObject();
      ArrayNode listJson = mapper.createArrayNode();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(accountId);

      int result = WorkChainCompornent.getWorkOfKukakuJsonArray(accountComprtnent.accountData.farmId, AgryeelConst.IkubyoInfo.WORKCHAINID, listJson);

      resultJson.put("datalist" , listJson);

      return ok(resultJson);
    }
    /**
     * 生産者IDから全担当者一覧を取得する
     * @return
     */
    public static Result getallaccountoffarm(double pFarmId) {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

        List<Account> accounts = Account.getAccountOfFarm(pFarmId);

        if (accounts.size() > 0) { //該当データが存在する場合
          for (Account account : accounts) {
            if (account.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode jd = Json.newObject();

            jd.put("id"   , account.accountId);
            jd.put("name" , account.acountName);
            jd.put("flag" , 0);

            listJson.add(jd);
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }
    /**
     * 生産者IDから全圃場グループ情報一覧を取得する
     * @return
     */
    public static Result getallfieldgrouptoffarm(double pFarmId) {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

        List<FieldGroup> fgs = FieldGroup.getFieldGroupOfFarm(pFarmId);

        if (fgs.size() > 0) { //該当データが存在する場合
          for (FieldGroup fg : fgs) {
            ObjectNode jd = Json.newObject();

            jd.put("id"   , fg.fieldGroupId);
            jd.put("name" , fg.fieldGroupName);
            jd.put("flag" , 0);

            listJson.add(jd);
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }
    /**
     * 生産者IDから全区画情報一覧を取得する
     * @return
     */
    public static Result getallcompartmentoffarm(double pFarmId) {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

        List<Compartment> compartments = Compartment.getCompartmentOfFarmSort(pFarmId);

        if (compartments.size() > 0) { //該当データが存在する場合
          for (Compartment compartment : compartments) {
            if (compartment.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode jd = Json.newObject();

            jd.put("id"   , compartment.kukakuId);
            jd.put("name" , compartment.kukakuName);
            jd.put("flag" , 0);

            listJson.add(jd);
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }
	/**
	 * 生産者IDよりワークチェイン一覧を取得する
	 * @param pFarmId
	 * @param pListJson
	 * @return
	 */
    public static Result getworkchainoffarm(double pFarmId) {

      /* 戻り値用JSONデータの生成 */
  	  ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ObjectNode resultJson = Json.newObject();
      ArrayNode listJson = mapper.createArrayNode();

      List<WorkChain> works = WorkChain.getWorkChainOfFarm(pFarmId);

      if (works.size() > 0) { //該当データが存在する場合
        for (WorkChain work : works) {
          if (work.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          ObjectNode jd = Json.newObject();

          jd.put("chainId"   , work.workChainId);
          jd.put("chainName" , work.workChainName);

          listJson.add(jd);
        }
      }
      resultJson.put("chainlist" , listJson);

      return ok(resultJson);
    }

    /**
     * 作業IDから区画一覧を取得する
     * @return
     */
    public static Result getkukakuoffarm(double farmId) {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

        int result = FieldComprtnent.getCompartmentOfFarmJsonArray(farmId, listJson);

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 作業IDから区画一覧を取得する（座標指定）
     * @return
     */
    public static Result getkukakuoffarmRadius(String accountId, double workId, double pLat, double pLng) {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(accountId);

        int result = FieldComprtnent.getCompartmentOfFarmRadius(accountComprtnent.accountData.farmId, workId, pLat, pLng, accountComprtnent.accountStatusData.radius, listJson);

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * ワークチェイン選択変更時
     * @return
     */
    public static Result changeworkchain(String accountId, double chainId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(accountId);

        accountComprtnent.accountStatusData.selectChainId = chainId;
        accountComprtnent.accountStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

	/**
	 * 作業中情報を取得する
	 * @param accountId アカウントＩＤ
	 * @return 取得結果
	 */
    public static Result getworking(String accountId) {
        SimpleDateFormat sts = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

		//アカウントIDをキーにアカウント情報を取得する
		Account account = Account.find.where().eq("account_id", accountId).findUnique();

    	if (account != null) { //アカウントが取得できた場合

      		resultJson.put("field"   , account.fieldId);
            if (account.workPlanId != 0) {
              List<models.WorkPlan> workplans = models.WorkPlan.find.where().eq("work_plan_id", account.workPlanId).eq("account_id", account.accountId).orderBy("work_plan_id").findList();
              String sKukaku = "";
              for (models.WorkPlan workplan: workplans) {
                Compartment cd = FieldComprtnent.getCompartment(workplan.kukakuId);
                if (cd != null) {
                  if (!"".equals(sKukaku)) {
                    sKukaku += ",";
                  }
                  sKukaku += cd.kukakuName;
                }
              }
              resultJson.put("fieldnm", sKukaku);
            }
            else {
              Compartment cp = Compartment.getCompartmentInfo(account.fieldId);
              if(cp != null){
                resultJson.put("fieldnm"    , cp.kukakuName);
              }else{
                resultJson.put("fieldnm"    , "");
              }
            }
      		resultJson.put("work"    , account.workId);
      		Work wk = Work.getWork(account.workId);
      		if(wk != null){
    	  		resultJson.put("worknm"    , wk.workName);
      		}else{
    	  		resultJson.put("worknm"    , "");
      		}
      		if (account.workStartTime == null) {
        		resultJson.put("start"    , "");
      		}
      		else {
        		resultJson.put("start"    , sts.format(account.workStartTime));
      		}
      		resultJson.put("workPlanId"    , account.workPlanId);
    	}

        return ok(resultJson);
	}

    /**
     * 作業開始要求
     * @return
     */
    public static Result workingstart(String accountId, double workId, double kukakuId) {

        ObjectNode  resultJson = Json.newObject();

        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(accountId);
        Account ac = accountComprtnent.accountData;

        ac.workId   = workId;
        ac.fieldId  = kukakuId;
        ac.workStartTime = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
        ac.update();

        session(AgryeelConst.SessionKey.WORKING_ACTION, "display");
        accountComprtnent.getAccountJson(resultJson);

        return ok(resultJson);
    }

    /**
     * 作業終了要求
     * @return
     */
    public static Result workingend(String accountId, double farmId, double workId, double kukakuId, int steps, double distance, int calorie, int heartRate) {

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = Json.parse("{\"workId\":\"" + (int)workId + "\", \"workKukaku\":\"" + kukakuId + "\", \"workDate\":\"\", \"workAccount\":\"" + accountId + "\", \"steps\":\"" + steps + "\", \"distance\":\"" + distance + "\", \"calorie\":\"" + calorie + "\", \"heartRate\":\"" + heartRate + "\", \"mode\":10}");

        controllers.WorkDiary.updateWorkDiary(resultJson, input, 0);

        return ok(resultJson);
    }

    /**
     * 作業中断要求
     * @return
     */
    public static Result workingstop(String accountId) {

        session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        return controllers.Working.workingstop();
    }

    /**
     * 作業計画開始要求
     * @return
     */
    public static Result startworkplan(String accountId, double workPlanId) {

        session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        return controllers.WorkPlanController.startWorkPlan(workPlanId);
    }

    /**
     * 作業中メッセージ取得
     * @return
     */
    public static Result getworkingmessage(String accountId) {

    	ObjectNode  resultJson = Json.newObject();
        ObjectNode  listJson   = Json.newObject();

        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(accountId);

        Account account = Account.getAccount(accountId);

        SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");


        double timelineId = 0;
        Work wk = account.getWork();

        Compartment cpt = Compartment.getCompartmentInfo(account.fieldId);

        Calendar cal = Calendar.getInstance();
        long to   = cal.getTime().getTime();
        long from = account.workStartTime.getTime();

        long diff = ( to - from  ) / (1000 * 60 );

        timelineId--;
        resultJson.put("timeLineId"   , timelineId);                                //タイムラインID
        resultJson.put("workdate"     , sdf2.format(account.workStartTime));        //作業日
        resultJson.put("updateTime"   , sdf.format(account.workStartTime));         //更新日時
        if (account.workPlanId != 0) {
          PlanLine pl = PlanLine.find.where().eq("work_plan_id", account.workPlanId).findUnique();
          if (pl != null) {
        	  resultJson.put("message"      , pl.message);                          //メッセージ
          }
          else {
        	  resultJson.put("message"      , account.notificationMessage);         //メッセージ
          }
        }
        else {
        	resultJson.put("message"      , account.notificationMessage);           //メッセージ
        }
        resultJson.put("timeLineColor", wk.workColor);                              //タイムラインカラー
        resultJson.put("workDiaryId"  , timelineId);                                //作業記録ID
        resultJson.put("workName"     , wk.workName);                               //作業名
        resultJson.put("kukakuName"   , cpt.kukakuName);                            //区画名
        resultJson.put("accountName"  , account.acountName);                        //アカウント名
        resultJson.put("workId"       , wk.workId);                                 //作業ＩＤ
        resultJson.put("worktime"     , diff);                                      //作業時間

        return ok(resultJson);
    }

    /**
     * ハウス状況照会を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getcompartment(String accountId, double farmId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));
    	session(AgryeelConst.SessionKey.API, "true");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        return controllers.Application.getCompartment();
    }

    /**
     * ハウス状況照会を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getfieldgroupSPD(String accountId, double farmId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));
    	session(AgryeelConst.SessionKey.API, "true");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        return controllers.MainController.getFieldGroupSPD();
    }

    /**
     * ハウス状況照会を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getfieldgroupSP(String accountId, double farmId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));
    	session(AgryeelConst.SessionKey.API, "true");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        return controllers.MainController.getFieldGroupSP();
    }

    /**
     * ハウス状況照会区画情報を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getkukakuSP(String accountId, double farmId, double groupId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));
    	session(AgryeelConst.SessionKey.API, "true");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        return controllers.MainController.getKukakuSP(groupId);
    }

    /**
     * 圃場詳細情報取得処理
     */
    public static Result getfielddetail(String accountId, double farmId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));
    	session(AgryeelConst.SessionKey.API, "true");

        return controllers.Application.getFieldDetail();
    }

    /**
     * 区画詳細情報取得処理
     */
    public static Result getkukakudetail(String accountId, double farmId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));
    	session(AgryeelConst.SessionKey.API, "true");

        return controllers.Application.getKukakuDetail();
    }

    /**
     * 元帳一覧取得
     * @return
     */
    public static Result getmotochohistry(double kukakuId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        int result = MotochoCompornent.getMotochoHistryArray(kukakuId, listJson);

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }
    /**
     * 状況照会取得
     * @return
     */
    public static Result getdisplaystatus() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJson = mapper.createArrayNode();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJsonArray(Common.ConstClass.DISPLAYSTAUS, listJson);

      resultJson.put("datalist" , listJson);

      return ok(resultJson);
    }

    /**
     * アカウントID別圃場グループ取得
     * @return
     */
    public static Result getfieldgroupofaccount(String accountId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJson = mapper.createArrayNode();

      UserComprtnent uc = new UserComprtnent();
      uc.GetAccountData(accountId);

      List<FieldGroup> fgs = FieldGroup.getFieldGroupOfFarm(uc.accountData.farmId);
      if (fgs.size() > 0) {
        for (FieldGroup fg : fgs) {
          if (fg.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          ObjectNode jd = Json.newObject();

          jd.put("id"   , fg.fieldGroupId);
          jd.put("name" , fg.fieldGroupName);
          if (uc.checkFieldGroupSelect(fg.fieldGroupId)) {
            jd.put("flag" , 1);
          }
          else {
            jd.put("flag" , 0);
          }
          listJson.add(jd);

        }
      }

      resultJson.put("datalist" , listJson);

      return ok(resultJson);
    }

    /**
     * 区画検索条件をアカウント状況に反映する
     * @return
     */
    public static Result kukakusearchcommit(String accountId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        return controllers.CommonController.kukakusearchCommit();
    }

    /**
     * 区画生産物変更要求
     * @return
     */
    public static Result selectkukakucrop(String accountId) {

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        controllers.Application.selectKukakuCrop();

        return ok(resultJson);
    }

    /**
     * 複数区画生産物変更要求
     * @return
     */
    public static Result selectmultikukakucrop(String accountId) {

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        controllers.Application.selectMultiKukakuCrop();

        return ok(resultJson);
    }

    /**
     * 区画ワークチェイン変更要求
     * @return
     */
    public static Result selectkukakuworkchain(String accountId) {

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        controllers.Application.selectKukakuWorkChain();

        return ok(resultJson);
    }

    /**
     * 作業別機器情報取得
     * @return
     */
    public static Result getkikiofwork(double farmId, double workId, double kukakuId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        //----- 区画ワークチェーン状況を取得 -----
        CompartmentWorkChainStatus cwcs = WorkChainCompornent.getCompartmentWorkChainStatus(kukakuId);
        if (cwcs != null) {
          WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(cwcs.workChainId, workId);
          if (wci != null) {
            //----- 生産者別機器情報を取得 -----
            List<KikiOfFarm> kofs = KikiOfFarm.getKikiOfFarm(farmId);
            List<Double>kikis = new ArrayList<Double>();
            for (KikiOfFarm kof : kofs) {
              if (kof.deleteFlag == 1) { // 削除済みの場合
                continue;
              }
              kikis.add(kof.kikiId);
            }
            //----- 作業単位の使用可能機器種別を取得 -----
            if (wci.onUseKikiKind != null && !"".equals(wci.onUseKikiKind)) {
              String[] ukks = wci.onUseKikiKind.split(",");
              List<Double>kks = new ArrayList<Double>();
              for (String ukk : ukks) {
                kks.add(Double.parseDouble(ukk));
              }
              //----- 対象機器の取得 -----
              List<Kiki> kikiList = Kiki.find.where().in("kiki_id", kikis).in("kiki_kind", kks).order("kiki_id").findList();
              for (Kiki kiki : kikiList) {

                ObjectNode jd = Json.newObject();

                jd.put("id"   , kiki.kikiId);
                jd.put("name" , kiki.kikiName);
                jd.put("flag" , 0);

                listJson.add(jd);

              }
            }
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }


    /**
     * 作業別機器情報取得
     * @return
     */
    public static Result getkikiofworkchain(double farmId, double workId, double chainId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(chainId, workId);
        if (wci != null) {
          //----- 生産者別機器情報を取得 -----
          List<KikiOfFarm> kofs = KikiOfFarm.getKikiOfFarm(farmId);
          List<Double>kikis = new ArrayList<Double>();
          for (KikiOfFarm kof : kofs) {
            if (kof.deleteFlag == 1) { // 削除済みの場合
              continue;
            }
            kikis.add(kof.kikiId);
          }
          //----- 作業単位の使用可能機器種別を取得 -----
          if (wci.onUseKikiKind != null && !"".equals(wci.onUseKikiKind)) {
            String[] ukks = wci.onUseKikiKind.split(",");
            List<Double>kks = new ArrayList<Double>();
            for (String ukk : ukks) {
              kks.add(Double.parseDouble(ukk));
            }
            //----- 対象機器の取得 -----
            List<Kiki> kikiList = Kiki.find.where().in("kiki_id", kikis).in("kiki_kind", kks).order("kiki_id").findList();
            for (Kiki kiki : kikiList) {

              ObjectNode jd = Json.newObject();

              jd.put("id"   , kiki.kikiId);
              jd.put("name" , kiki.kikiName);
              jd.put("flag" , 0);

              listJson.add(jd);

            }
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 機器別アタッチメント情報取得
     * @return
     */
    public static Result getattachmentofkiki(double kikiId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        Kiki kiki = Kiki.getKikiInfo(kikiId);
        if (kiki != null) {
          //----- 機器別の使用可能アタッチメントを取得する -----
          if (kiki.onUseAttachmentId != null && !"".equals(kiki.onUseAttachmentId)) {
            String[] atats = kiki.onUseAttachmentId.split(",");
            List<Double>atas = new ArrayList<Double>();
            for (String ata : atats) {
              atas.add(Double.parseDouble(ata));
            }
            //----- 対象機器の取得 -----
            List<Attachment> attachments = Attachment.find.where().in("attachment_id", atas).order("attachment_id").findList();
            for (Attachment attachment : attachments) {
              if (attachment.deleteFlag == 1) { // 削除済みの場合
                continue;
              }

              ObjectNode jd = Json.newObject();

              jd.put("id"   , attachment.attachmentId);
              jd.put("name" , attachment.attachementName);
              jd.put("flag" , 0);

              listJson.add(jd);

            }
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 作業別農肥情報取得
     * @return
     */
    public static Result getnouhiofwork(double farmId, double workId, double kukakuId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

      //----- 区画ワークチェーン状況を取得 -----
        CompartmentWorkChainStatus cwcs = WorkChainCompornent.getCompartmentWorkChainStatus(kukakuId);
        if (cwcs != null) {
          WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(cwcs.workChainId, workId);
          if (wci != null) {
            List<Nouhi> nouhiList = Nouhi.find.where().in("farm_id", farmId).eq("nouhi_kind", wci.nouhiKind).order("use_count desc, nouhi_id asc").findList();
            for (Nouhi nouhi : nouhiList) {
              if (nouhi.deleteFlag == 1) { // 削除済みの場合
                continue;
              }

              ObjectNode jd = Json.newObject();

              jd.put("id"   , nouhi.nouhiId);
              jd.put("name" , nouhi.nouhiName);
              jd.put("flag" , 0);

              listJson.add(jd);

            }
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * ワークチェイン別農肥情報取得
     * @return
     */
    public static Result getnouhiofworkchain(double farmId, double workId, double chainId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(chainId, workId);
        if (wci != null) {
          List<Nouhi> nouhiList = Nouhi.find.where().in("farm_id", farmId).eq("nouhi_kind", wci.nouhiKind).order("use_count desc, nouhi_id asc").findList();
          for (Nouhi nouhi : nouhiList) {
            if (nouhi.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode jd = Json.newObject();

            jd.put("id"   , nouhi.nouhiId);
            jd.put("name" , nouhi.nouhiName);
            jd.put("flag" , 0);

            listJson.add(jd);

          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 生産者別ベルト情報取得
     * @return
     */
    public static Result getbeltooffarm(double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

      //----- 生産者別機器情報を取得 -----
        List<BeltoOfFarm> bofs = BeltoOfFarm.getBeltoOfFarm(farmId);
        List<Double>beltos = new ArrayList<Double>();
        for (BeltoOfFarm bof : bofs) {
          if (bof.deleteFlag == 1) { // 削除済みの場合
            continue;
          }
          beltos.add(bof.beltoId);
        }
        //----- 対象機器の取得 -----
        List<Belto> beltoList = Belto.find.where().in("belto_id", beltos).order("belto_id").findList();
        for (Belto belto : beltoList) {

          ObjectNode jd = Json.newObject();

          jd.put("id"   , belto.beltoId);
          jd.put("name" , belto.beltoName);
          jd.put("flag" , 0);

          listJson.add(jd);

        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 生産物別品種情報取得
     * @return
     */
    public static Result gethinsyuofcroptocrop(double farmId, double cropId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        //----- 生産者別機器情報を取得 -----
        List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(farmId);
        List<Double>hinsyus = new ArrayList<Double>();
        for (HinsyuOfFarm hof : hofs) {
          if (hof.deleteFlag == 1) { // 削除済みの場合
            continue;
          }
          hinsyus.add(hof.hinsyuId);
        }
        //----- 対象機器の取得 -----
        List<Hinsyu> hinsyuList = Hinsyu.find.where().in("hinsyu_id", hinsyus).eq("crop_id", cropId).order("hinsyu_id ASC").findList();
        for (Double key : hinsyus) {
          for (Hinsyu hinsyu : hinsyuList) {
            if (key.doubleValue() == hinsyu.hinsyuId) {
              ObjectNode jd = Json.newObject();

              jd.put("id"   , hinsyu.hinsyuId);
              jd.put("name" , hinsyu.hinsyuName);
              jd.put("flag" , 0);

              listJson.add(jd);
              break;
            }
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 生産物別品種情報取得
     * @return
     */
    public static Result gethinsyuofcrop(double farmId, double kukakuId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

      //----- 区画ワークチェーン状況を取得 -----
        CompartmentWorkChainStatus cwcs = WorkChainCompornent.getCompartmentWorkChainStatus(kukakuId);
        if (cwcs != null) {
          //----- 生産者別機器情報を取得 -----
          List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(farmId);
          List<Double>hinsyus = new ArrayList<Double>();
          for (HinsyuOfFarm hof : hofs) {
            if (hof.deleteFlag == 1) { // 削除済みの場合
              continue;
            }
            hinsyus.add(hof.hinsyuId);
          }
          //----- 対象機器の取得 -----
          Logger.info("[ GET HINSYU ] CROPID={}", cwcs.cropId);
          List<Hinsyu> hinsyuList = Hinsyu.find.where().in("hinsyu_id", hinsyus).eq("crop_id", cwcs.cropId).order("hinsyu_id ASC").findList();
          for (Double key : hinsyus) {
            for (Hinsyu hinsyu : hinsyuList) {
              if (key.doubleValue() == hinsyu.hinsyuId) {
                ObjectNode jd = Json.newObject();

                jd.put("id"   , hinsyu.hinsyuId);
                jd.put("name" , hinsyu.hinsyuName);
                jd.put("flag" , 0);

                listJson.add(jd);
                break;
              }
            }
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 生産者別品種取得
     * @return
     */
    public static Result gethinsyuoffarm(double farmId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJson = mapper.createArrayNode();

      //----- 生産者別機器情報を取得 -----
      List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(farmId);
      List<Double>hinsyus = new ArrayList<Double>();
      for (HinsyuOfFarm hof : hofs) {
        if (hof.deleteFlag == 1) { // 削除済みの場合
          continue;
        }
        hinsyus.add(hof.hinsyuId);
      }
      //----- 対象機器の取得 -----
      List<Hinsyu> hinsyuList = Hinsyu.find.where().in("hinsyu_id", hinsyus).order("hinsyu_id ASC").findList();
      for (Double key : hinsyus) {
        for (Hinsyu hinsyu : hinsyuList) {
          if (key.doubleValue() == hinsyu.hinsyuId) {
            ObjectNode jd = Json.newObject();

            jd.put("id"   , hinsyu.hinsyuId);
            jd.put("name" , hinsyu.hinsyuName);
            jd.put("flag" , 0);

            listJson.add(jd);
            break;
          }
        }
      }

      resultJson.put("datalist" , listJson);

      return ok(resultJson);
    }

    /**
     * 生産者別荷姿情報取得
     * @return
     */
    public static Result getnisugataoffarm(double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        List<NisugataOfFarm> datas = NisugataOfFarm.getNisugataOfFarm(farmId);

        for (NisugataOfFarm data : datas) {
          if (data.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          Nisugata model = Nisugata.getNisugataInfo(data.nisugataId);
          if (model != null) {
            ObjectNode jd = Json.newObject();

            jd.put("id"   , model.nisugataId);
            jd.put("name" , model.nisugataName);
            jd.put("flag" , 0);

            listJson.add(jd);
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
      }

    /**
     * 生産者別質情報取得
     * @return
     */
	  public static Result getshituoffarm(double farmId) {

	    /* 戻り値用JSONデータの生成 */
		ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        List<ShituOfFarm> datas = ShituOfFarm.getShituOfFarm(farmId);

        for (ShituOfFarm data : datas) {
          if (data.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          Shitu model = Shitu.getShituInfo(data.shituId);
          if (model != null) {
            ObjectNode jd = Json.newObject();

            jd.put("id"   , model.shituId);
            jd.put("name" , model.shituName);
            jd.put("flag" , 0);

            listJson.add(jd);
          }
        }

		resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

	    /**
	     * 生産者別サイズ情報取得
	     * @return
	     */
      public static Result getsizeoffarm(double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        List<SizeOfFarm> datas = SizeOfFarm.getSizeOfFarm(farmId);

        for (SizeOfFarm data : datas) {
          if (data.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          Size model = Size.getSizeInfo(data.sizeId);
          if (model != null) {
            ObjectNode jd = Json.newObject();

            jd.put("id"   , model.sizeId);
            jd.put("name" , model.sizeName);
            jd.put("flag" , 0);

            listJson.add(jd);
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
      }

	  /**
	   * 資材情報取得
	   * @return
	   */
      public static Result getsizai() {

	    /* 戻り値用JSONデータの生成 */
	    ObjectNode resultJson = Json.newObject();
	    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

	    CommonComprtnent cc = new CommonComprtnent();

	    int result = cc.getCommonJsonArray(Common.ConstClass.ITOSIZAI, listJson);

	    resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

	  /**
	   * 生産者別資材情報取得
	   * @return
	   */
      public static Result getsizaioffarm(double farmId) {

	    /* 戻り値用JSONデータの生成 */
	    ObjectNode resultJson = Json.newObject();
	    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        int result = SizaiComprtnent.getSizaiJsonArray(farmId, Sizai.SizaiKindClass.SIZAI, listJson);

	    resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

	  /**
	   * マルチ情報取得
	   * @return
	   */
	  public static Result getmulti() {

	    /* 戻り値用JSONデータの生成 */
	    ObjectNode resultJson = Json.newObject();
	    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

	    CommonComprtnent cc = new CommonComprtnent();

	    int result = cc.getCommonJsonArray(Common.ConstClass.ITOMULTI, listJson);

	    resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

	  /**
	   * 生産者別マルチ情報取得
	   * @return
	   */
      public static Result getmultioffarm(double farmId) {

	    /* 戻り値用JSONデータの生成 */
	    ObjectNode resultJson = Json.newObject();
	    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        int result = SizaiComprtnent.getSizaiJsonArray(farmId, Sizai.SizaiKindClass.MULTI, listJson);

	    resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

	  /**
	   * 使用培土情報取得
	   * @return
	   */
	  public static Result getusebaido() {

	    /* 戻り値用JSONデータの生成 */
	    ObjectNode resultJson = Json.newObject();
	    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

	    CommonComprtnent cc = new CommonComprtnent();

	    int result = cc.getCommonJsonArray(Common.ConstClass.ITOBAIDO, listJson);

	    resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

	  /**
	   * 生産者別培土情報取得
	   * @return
	   */
      public static Result getbaidooffarm(double farmId) {

	    /* 戻り値用JSONデータの生成 */
	    ObjectNode resultJson = Json.newObject();
	    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        int result = SizaiComprtnent.getSizaiJsonArray(farmId, Sizai.SizaiKindClass.BAIDO, listJson);

	    resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

	  /**
	   * 散布方法取得
	   * @return
	   */
      public static Result getsanpu() {

	    /* 戻り値用JSONデータの生成 */
	    ObjectNode resultJson = Json.newObject();
	    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

	    CommonComprtnent cc = new CommonComprtnent();

	    int result = cc.getCommonJsonArray(Common.ConstClass.SANPUMETHOD, listJson);

	    resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

	  /**
	   * 潅水方法取得
	   * @return
	   */
      public static Result getkansui() {

	    /* 戻り値用JSONデータの生成 */
	    ObjectNode resultJson = Json.newObject();
	    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

	    CommonComprtnent cc = new CommonComprtnent();

	    int result = cc.getCommonJsonArray(Common.ConstClass.KANSUI, listJson);

	    resultJson.put("datalist" , listJson);

	    return ok(resultJson);
	  }

    /**
     * 作業計画初期表示データ取得
     * @return
     */
    public static Result workplaninit(String accountId, String accountName, double farmId, double workPlanId) {
        session(AgryeelConst.SessionKey.WORKPLANID, String.valueOf(workPlanId));
        return workdiaryinit(accountId, accountName, farmId, 0, 0, -99999);
    }

    /**
     * 作業日誌初期表示データ取得
     * @return
     */
    public static Result workdiaryinit(String accountId, String accountName, double farmId, double workId, double kukakuId, double workDiaryId) {

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.ACCOUNTNAME, accountName);							//アカウント名をセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));
        if(workId != 0){
    	    session(AgryeelConst.SessionKey.WORKID, String.valueOf(workId));
        }else{
    	    session(AgryeelConst.SessionKey.WORKID, "");
        }
        if(kukakuId != 0){
    	    session(AgryeelConst.SessionKey.KUKAKUID, String.valueOf(kukakuId));
        }else{
    	    session(AgryeelConst.SessionKey.KUKAKUID, "");
        }
        if(workDiaryId != 0 && workDiaryId != -99999){
            session(AgryeelConst.SessionKey.WORKDIARYID, String.valueOf(workDiaryId));
        }else{
            session(AgryeelConst.SessionKey.WORKDIARYID, "");
        }
        if(workDiaryId != -99999){
            session(AgryeelConst.SessionKey.WORKPLANID, "");
        }
        session(AgryeelConst.SessionKey.API, "true");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /*-------------------------------------------------------------------*/
        /* アカウント情報の取得
        /*-------------------------------------------------------------------*/
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(accountId);
        AccountStatus status = accountComprtnent.accountStatusData;
        resultJson.put("workdateautoset", status.workDateAutoSet);

        /*-------------------------------------------------------------------*/
        /* 生産者情報の取得
        /*-------------------------------------------------------------------*/
        FarmComprtnent fc = new FarmComprtnent();
        fc.GetFarmData(accountComprtnent.accountData.farmId);

        CommonWorkDiary cwd	   = new CommonWorkDiary(session(), resultJson);//作業記録共通項目
        boolean api  = false;

        try{
          api  = Boolean.parseBoolean(session().get(AgryeelConst.SessionKey.API));
        }catch (Exception ex) {
          api  = false;
        }
        cwd.apiFlg = api;


        cwd.init();															//初期処理

        /*-------------------------------------------------------------------*/
        /* 各一覧表情報の取得                                                */
        /*-------------------------------------------------------------------*/
        CommonWorkDiaryWork cwdk = null;				//作業記録作業別項目

        /*----- 作業別項目 -----*/
        //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
        switch ((int)cwd.work.workTemplateId) {
        case AgryeelConst.WorkTemplate.NOMAL:
          cwdk = new NomalCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.SANPU:
          cwdk = new HiryoSanpuCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.HASHU:
          cwdk = new HashuCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.SHUKAKU:
          cwdk = new ShukakuCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.NOUKO:
          cwdk = new NoukouCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.KANSUI:
          cwdk = new KansuiCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.KAISHU:
          cwdk = new KaishuCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.DACHAKU:
          cwdk = new DachakuCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.COMMENT:
          cwdk = new CommentCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.MALTI:
          cwdk = new MultiCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.TEISHOKU:
          cwdk = new TeishokuCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.NAEHASHU:
          cwdk = new NaehashuCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.SENTEI:
          cwdk = new SenteiCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.MABIKI:
          cwdk = new MabikiCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.NICHOCHOSEI:
          cwdk = new NichoChoseiCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.SENKA:
          cwdk = new SenkaCompornent(session(), resultJson, true);
          break;
        case AgryeelConst.WorkTemplate.HAIKI:
          cwdk = new HaikiCompornent(session(), resultJson, true);
          break;
        }

        if (cwdk != null) {								//作業項目別コンポーネントが生成できた場合

            cwdk.kukakuId   = cwd.kukakuId;
            cwdk.workId     = cwd.workId;
            cwdk.workDiary	= cwd.workDiary;
            cwdk.workPlan   = cwd.workPlan;
            cwdk.init();								//初期処理

        }
        /*-------------------------------------------------------------------*/

        resultJson.put("syukakuinputcount", fc.farmStatusData.syukakuInputCount);
        resultJson.put("backmode", session(AgryeelConst.SessionKey.BACK_MODE));
        resultJson.put("backaccount", session(AgryeelConst.SessionKey.BACK_ACCOUNT));
        resultJson.put("backdate", session(AgryeelConst.SessionKey.BACK_DATE));

        return ok(resultJson);
    }


    /**
     * 【AGRYEEL】作業日誌記録処理
     * @return
     */
    public static Result submitworkdiary(String accountId, double farmId, double workDiaryId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));

        controllers.WorkDiary.updateWorkDiary(resultJson, input, workDiaryId, true);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】作業計画削除処理
     * @return
     */
    public static Result workplandelete(String accountId, double farmId, double workPlanId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));

        controllers.WorkPlanController.workPlanDelete(workPlanId);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】作業日誌記録処理
     * @return
     */
    public static Result workdiarydelete(String accountId, double farmId, double workDiaryId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));

        controllers.WorkDiary.workDiaryDelete(workDiaryId);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】作業計画記録処理
     * @return
     */
    public static Result submitworkplan(String accountId, double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));

        controllers.WorkDiary.updateWorkPlan(resultJson, input, true);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】作業計画記録完了処理
     * @return
     */
    public static Result plantodiary(String accountId) {

      Logger.info("[ PLANTODIARY_API ] ACCOUNTID={}", accountId);
      ObjectNode  resultJson = Json.newObject();

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	Account ac = Account.getAccount(accountId);
    	if (ac != null) {
        session(AgryeelConst.SessionKey.FARMID, String.valueOf(ac.farmId));
    	}
    	else {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
        return ok(resultJson);
    	}

      return controllers.WorkDiary.planToDiary();
    }
    /**
     * 【AGRYEEL】作業計画記録完了処理(時間計測なし)
     * @return
     */
    public static Result plantodiarycommit(String accountId) {

      Logger.info("[ PLANTODIARY_API ] ACCOUNTID={}", accountId);
      ObjectNode  resultJson = Json.newObject();

      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      Account ac = Account.getAccount(accountId);
      if (ac != null) {
        session(AgryeelConst.SessionKey.FARMID, String.valueOf(ac.farmId));
      }
      else {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
        return ok(resultJson);
      }

      return controllers.WorkDiary.planToDiaryTimeCommit();
    }

    /**
     * 初期検索タイムラインデータを取得する
     * @return
     */
    public static Result gettimelineinitdata(String accountId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        if (getAccount == UserComprtnent.GET_ERROR) {
          resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
          resultJson.put("result"   , "ERROR");
          return ok(resultJson);
        }

        /* アカウント情報からタイムライン検索条件を取得する */
        UserComprtnent uc = new UserComprtnent();
        uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        Calendar cStart = Calendar.getInstance();
        Calendar cEnd = Calendar.getInstance();

        cStart.add(Calendar.MONTH, -1);
        DateU.setTime(cStart, DateU.TimeType.FROM);
        DateU.setTime(cEnd, DateU.TimeType.TO);

        SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");

        double timelineId = 0;
        List<Account> workings = Account.getAccountOfWorking(accountComprtnent.accountData.farmId);
        for (Account working : workings) {                                        //作業情報をJSONデータに格納する

          Work wk = working.getWork();

          if (wk == null) { continue; }

          Compartment cpt = Compartment.getCompartmentInfo(working.fieldId);

          Calendar cal = Calendar.getInstance();
          long to   = cal.getTime().getTime();
          long from = working.workStartTime.getTime();

          long diff = ( to - from  ) / (1000 * 60 );

          ObjectNode timeLineJson         = Json.newObject();
          timelineId--;
          timeLineJson.put("timeLineId"   , timelineId);                                //タイムラインID
          timeLineJson.put("workdate"     , sdf2.format(working.workStartTime));        //作業日
          timeLineJson.put("updateTime"   , sdf.format(working.workStartTime));         //更新日時
          if (working.workPlanId != 0) {
            PlanLine pl = PlanLine.find.where().eq("work_plan_id", working.workPlanId).findUnique();
            if (pl != null) {
              timeLineJson.put("message"      , pl.message);                            //メッセージ
            }
            else {
              timeLineJson.put("message"      , "【現在作業中】");                      //メッセージ
            }
          }
          else {
            timeLineJson.put("message"      , "【現在作業中】");                        //メッセージ
          }
          timeLineJson.put("timeLineColor", "aaaaaa");                                  //タイムラインカラー
          timeLineJson.put("workDiaryId"  , timelineId);                                //作業記録ID
          timeLineJson.put("workName"     , wk.workName);                               //作業名
          timeLineJson.put("kukakuName"   , cpt.kukakuName);                            //区画名
          timeLineJson.put("accountName"  , working.acountName);                        //アカウント名
          timeLineJson.put("workId"       , wk.workId);                                 //作業ＩＤ
          timeLineJson.put("worktime"     , diff);                                      //作業時間
          timeLineJson.put("workDiaryId"  , timelineId);                                //ＩＤ

          listJson.add(timeLineJson);

        }

        /* アカウント情報からタイムライン情報を取得する */
        List<TimeLine> timeLine = TimeLine.find.where().eq("farm_id", uc.accountData.farmId).between("work_date", new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis())).orderBy("work_date desc, update_time desc").findList();

        for (TimeLine timeLineData : timeLine) {                                        //作業情報をJSONデータに格納する

          WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

          if (workd == null) { continue; }

          ObjectNode timeLineJson         = Json.newObject();
          timeLineJson.put("timeLineId"   , timeLineData.timeLineId);                   //タイムラインID
          timeLineJson.put("workdate"     , sdf2.format(workd.workDate));               //作業日
          timeLineJson.put("updateTime"   , sdf.format(timeLineData.updateTime));       //更新日時
          timeLineJson.put("message"      , StringU.setNullTrim(timeLineData.message)); //メッセージ
          timeLineJson.put("timeLineColor", timeLineData.timeLineColor);                //タイムラインカラー
          timeLineJson.put("workDiaryId"  , timeLineData.workDiaryId);                  //作業記録ID
          timeLineJson.put("workName"     , timeLineData.workName);                     //作業名
          timeLineJson.put("kukakuName"   , timeLineData.kukakuName);                   //区画名
          timeLineJson.put("accountName"  , timeLineData.accountName);                  //アカウント名
          timeLineJson.put("workId"       , timeLineData.workId);                       //作業ＩＤ
          timeLineJson.put("worktime"     , workd.workTime);                            //作業時間
          timeLineJson.put("workDiaryId"  , workd.workDiaryId);                         //ＩＤ

          listJson.add(timeLineJson);

        }

        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
        resultJson.put("result"   , "SUCCESS");

        return ok(resultJson);
    }

    /**
     * タイムライン検索条件をもとにタイムライン情報を取得する
     * @return
     */
    public static Result gettimelinedata(String accountId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        /* アカウント情報からタイムライン検索条件を取得する */
        UserComprtnent uc = new UserComprtnent();
        uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        String[] kukakus = uc.accountStatusData.selectKukakuId.split(",");
        List<Double>      aryKukaku     = new ArrayList<Double>();
        for (String data : kukakus) {
          try {
            aryKukaku.add(Double.parseDouble(data));
          }
          catch (NumberFormatException nfe) {

          }
          finally {

          }
        }
        if (aryKukaku.size() == 0) {
          List<Compartment> compartments = Compartment.getCompartmentOfFarm(uc.accountData.farmId);
          for (Compartment data: compartments) {
            if ("\'\'".equals(data)) {
              continue;
            }
            aryKukaku.add(data.kukakuId);
          }
        }
        String[] works = uc.accountStatusData.selectWorkId.split(",");
        List<Double>      aryWork     = new ArrayList<Double>();
        for (String data : works) {
          try {
            if ("".equals(data) || "\'\'".equals(data)) {
              continue;
            }
            aryWork.add(Double.parseDouble(data));
          }
          catch (NumberFormatException nfe) {

          }
          finally {

          }
        }
        if (aryWork.size() == 0) {
          List<Work> workds = Work.getWorkOfFarm(uc.accountData.farmId);
          for (Work data: workds) {
            if ("".equals(data) || "\'\'".equals(data)) {
              continue;
            }
            aryWork.add(data.workId);
          }
        }
        String[] accounts = uc.accountStatusData.selectAccountId.split(",");
        List<String>      aryAccount     = new ArrayList<String>();
        for (String data : accounts) {
          try {
            if ("".equals(data) || "\'\'".equals(data)) {
              continue;
            }
            aryAccount.add(data);
          }
          finally {

          }
        }
        if (aryAccount.size() == 0) {
          List<Account> accountds = Account.getAccountOfFarm(uc.accountData.farmId);
          for (Account data: accountds) {
            aryAccount.add(data.accountId);
          }
        }
        //----- 生産物 -----
        String[] crops = uc.accountStatusData.selectCropId.split(",");
        List<Double>      aryCrop     = new ArrayList<Double>();
        for (String data : crops) {
          try {
            if ("".equals(data) || "\'\'".equals(data)) {
              continue;
            }
            aryCrop.add(Double.parseDouble(data));
          }
          catch (NumberFormatException nfe) {

          }
          finally {

          }
        }
        //----- 品種 -----
        String[] hinsyus = uc.accountStatusData.selectHinsyuId.split(",");
        List<Double>      aryHinsyu     = new ArrayList<Double>();
        for (String data : hinsyus) {
          try {
            if ("".equals(data) || "\'\'".equals(data)) {
              continue;
            }
            aryHinsyu.add(Double.parseDouble(data));
          }
          catch (NumberFormatException nfe) {

          }
          finally {

          }
        }

        SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");

        if ((uc.accountStatusData.selectWorking == 1)
            || (uc.accountStatusData.selectWorking == 2)) {

          double timelineId = 0;
          List<Account> workings = Account.getAccountOfWorking(accountComprtnent.accountData.farmId);
          for (Account working : workings) {                                        //作業情報をJSONデータに格納する

            Work wk = working.getWork();

            if (wk == null) { continue; }

            Compartment cpt = Compartment.getCompartmentInfo(working.fieldId);

            Calendar cal = Calendar.getInstance();
            long to   = cal.getTime().getTime();
            long from = working.workStartTime.getTime();

            long diff = ( to - from  ) / (1000 * 60 );

            ObjectNode timeLineJson         = Json.newObject();
            timelineId--;
            timeLineJson.put("timeLineId"   , timelineId);                                //タイムラインID
            timeLineJson.put("workdate"     , sdf2.format(working.workStartTime));        //作業日
            timeLineJson.put("updateTime"   , sdf.format(working.workStartTime));         //更新日時
            if (working.workPlanId != 0) {
              PlanLine pl = PlanLine.find.where().eq("work_plan_id", working.workPlanId).findUnique();
              if (pl != null) {
                timeLineJson.put("message"      , pl.message);                            //メッセージ
              }
              else {
                timeLineJson.put("message"      , "【現在作業中】");                      //メッセージ
              }
            }
            else {
              timeLineJson.put("message"      , "【現在作業中】");                        //メッセージ
            }
            timeLineJson.put("timeLineColor", "aaaaaa");                                  //タイムラインカラー
            timeLineJson.put("workDiaryId"  , timelineId);                                //作業記録ID
            timeLineJson.put("workName"     , wk.workName);                               //作業名
            timeLineJson.put("kukakuName"   , cpt.kukakuName);                            //区画名
            timeLineJson.put("accountName"  , working.acountName);                        //アカウント名
            timeLineJson.put("workId"       , wk.workId);                                 //作業ＩＤ
            timeLineJson.put("worktime"     , diff);                                      //作業時間
            timeLineJson.put("workDiaryId"  , timelineId);                                //ＩＤ

            listJson.add(timeLineJson);

          }

        }

        if ((uc.accountStatusData.selectWorking == 1)
            || (uc.accountStatusData.selectWorking == 3)) {

          /* アカウント情報からタイムライン情報を取得する */
          List<TimeLine> timeLine = TimeLine.find.where().eq("farm_id", uc.accountData.farmId).in("kukaku_id", aryKukaku).in("account_id", aryAccount).in("work_id", aryWork).between("work_date", uc.accountStatusData.selectStartDate, uc.accountStatusData.selectEndDate).orderBy("work_date desc, update_time desc").findList();

          for (TimeLine timeLineData : timeLine) {                                        //作業情報をJSONデータに格納する

            WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

            if (workd == null) { continue; }

            //----- 生産物などのチェック -----
            if ((aryCrop.size() > 0) || (aryHinsyu.size() > 0) ) {
              List<MotochoBase> bases = MotochoBase.find.where().eq("kukaku_id", timeLineData.kukakuId).le("work_start_day", timeLineData.workDate).ge("work_end_day", timeLineData.workDate).findList();
              if (bases.size() == 0) { //生産物指定または品種指定ありで、元帳データなしは対象外とする
                continue;
              }
              MotochoBase base = bases.get(0);
              if (aryCrop.size() > 0) {
                if (!ListrU.keyCheck(aryCrop, base.cropId)) {
                  continue;
                }
              }
              if (aryHinsyu.size() > 0) {
                if (!ListrU.keyChecks(aryHinsyu, base.hinsyuId)) {
                  continue;
                }
              }
            }

            ObjectNode timeLineJson         = Json.newObject();
            timeLineJson.put("timeLineId"   , timeLineData.timeLineId);                   //タイムラインID
            timeLineJson.put("workdate"     , sdf2.format(workd.workDate));               //作業日
            timeLineJson.put("updateTime"   , sdf.format(timeLineData.updateTime));       //更新日時
            timeLineJson.put("message"      , StringU.setNullTrim(timeLineData.message)); //メッセージ
            timeLineJson.put("timeLineColor", timeLineData.timeLineColor);                //タイムラインカラー
            timeLineJson.put("workDiaryId"  , timeLineData.workDiaryId);                  //作業記録ID
            timeLineJson.put("workName"     , timeLineData.workName);                     //作業名
            timeLineJson.put("kukakuName"   , timeLineData.kukakuName);                   //区画名
            timeLineJson.put("accountName"  , timeLineData.accountName);                  //アカウント名
            timeLineJson.put("workId"       , timeLineData.workId);                       //作業ＩＤ
            timeLineJson.put("worktime"     , workd.workTime);                            //作業時間
            timeLineJson.put("workDiaryId"  , workd.workDiaryId);                         //ＩＤ

            listJson.add(timeLineJson);

          }

        }

        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
        resultJson.put("result"   , "SUCCESS");

        return ok(resultJson);
    }

    /**
     * タイムライン検索条件をアカウント状況に反映する
     * @return
     */
    public static Result timelinestatuscommit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();


        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(input.get("accountId").asText());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        try {

          Calendar cFrom = Calendar.getInstance();
          cFrom.setTimeInMillis(sdf.parse(input.get("from").asText()).getTime());
          DateU.setTime(cFrom, DateU.TimeType.FROM);

          Calendar cTo = Calendar.getInstance();
          cTo.setTimeInMillis(sdf.parse(input.get("to").asText()).getTime());
          DateU.setTime(cTo, DateU.TimeType.TO);

          java.sql.Timestamp from = new java.sql.Timestamp(cFrom.getTimeInMillis());
          java.sql.Timestamp to   = new java.sql.Timestamp(cTo.getTimeInMillis());

          accountComprtnent.accountStatusData.selectStartDate = from;
          accountComprtnent.accountStatusData.selectEndDate   = to;

          accountComprtnent.accountStatusData.selectWorkId    = input.get("work").asText();
          accountComprtnent.accountStatusData.selectAccountId = input.get("account").asText();
          accountComprtnent.accountStatusData.selectWorking   = input.get("working").asInt();
          accountComprtnent.accountStatusData.selectKukakuId  = input.get("kukaku").asText();
          accountComprtnent.accountStatusData.selectCropId    = input.get("crop").asText();
          accountComprtnent.accountStatusData.selectHinsyuId  = input.get("hinsyu").asText();
          accountComprtnent.accountStatusData.update();

          resultJson.put("result"   , "SUCCESS");
        } catch (ParseException e) {
          e.printStackTrace();
          resultJson.put("result"   , "ERROR");
        }

        return ok(resultJson);
    }

    public static Result gettimelineworking() {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();

        CommonComprtnent cc = new CommonComprtnent();

        List<Common> commons = Common.GetCommonList(Common.ConstClass.DISPLAYWORKING);

        if (commons.size() > 0) {
        	for (Common data : commons) {

                ObjectNode jd = Json.newObject();

                jd.put("id"   , data.commonSeq);
                jd.put("name" , data.commonName);
                jd.put("flag" , 0);
                listJson.add(jd);
            }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * Push通知要求
     * @return
     */
    public static Result postNotifications(double farmId, String message) {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
    	try {

    		   String jsonResponse;
    		   int farmid = (int)farmId;

    		   URL url = new URL("https://onesignal.com/api/v1/notifications");
    		   HttpURLConnection con = (HttpURLConnection)url.openConnection();
    		   con.setUseCaches(false);
    		   con.setDoOutput(true);
    		   con.setDoInput(true);

    		   con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    		   con.setRequestProperty("Authorization", "Basic NWUwNWFiZjQtY2M0Yy00MzZjLTkwYjktNzk5NjEyMjQ4ZGFl");
    		   con.setRequestMethod("POST");

    		   String strJsonBody = "{"
    		                      +   "\"app_id\": \"c7d10155-6fc7-48fe-8268-02ec169de608\","
    		                      +   "\"filters\": [{\"field\": \"tag\", \"key\": \"tag1\", \"relation\": \"=\", \"value\": \"farm" + String.valueOf(farmid) + "\"}],"
    		                      +   "\"data\": {\"foo\": \"bar\"},"
    		                      //+   "\"ios_badgeType\":  \"Increase\","
    		                      +   "\"ios_badgeType\":  \"SetTo\","
    		                      +   "\"ios_badgeCount\":  1,"
    		                      +   "\"subtitle\":  {\"en\": \"お知らせ\"},"
    		                      +   "\"contents\": {\"en\": \"" + message + "\"}"
    		                      + "}";


    		   System.out.println("strJsonBody:\n" + strJsonBody);

    		   byte[] sendBytes = strJsonBody.getBytes("UTF-8");
    		   con.setFixedLengthStreamingMode(sendBytes.length);

    		   OutputStream outputStream = con.getOutputStream();
    		   outputStream.write(sendBytes);

    		   int httpResponse = con.getResponseCode();
    		   System.out.println("httpResponse: " + httpResponse);

    		   if (  httpResponse >= HttpURLConnection.HTTP_OK
    		      && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
    		      Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
    		      jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
    		      scanner.close();
    		   }
    		   else {
    		      Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
    		      jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
    		      scanner.close();
    		   }
    		   System.out.println("jsonResponse:\n" + jsonResponse);

    		} catch(Throwable t) {
    		   t.printStackTrace();
    		}
        resultJson.put("result"   , "SUCCESS");

        return ok(resultJson);
    }

    /**
     * 【AGRYELL】システムメッセージ登録
     * @return システムメッセージ登録結果JSON
     */
    public static Result systemmessagemake() {

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
    			systemMessage.save();												//新規メッセージの作成

          //----- 対象生産者のアカウント情報を取得する -----
          List<Account> accountList = Account.getAccountOfFarm(Double.parseDouble(jsonData.get("farmId").asText()));
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

    /**
     * 【AGRYEEL】システムメッセージを取得する
     * @return 検索条件JSON
     */
    public static Result getsystemmessage(String accountId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        /* アカウント情報を取得する */
        UserComprtnent accountComprtnent = new UserComprtnent();
        accountComprtnent.GetAccountData(accountId);

        List<SystemMessage> sms = MessageOfAccount.getMessageByAccountId(accountId);
        if(sms.size() > 0) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
          SimpleDateFormat tmf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
          Calendar cal = Calendar.getInstance();
          java.sql.Date system = new java.sql.Date(cal.getTimeInMillis());

          for(SystemMessage sm : sms) {
            ObjectNode  mssageJson = Json.newObject();
            mssageJson.put("key", tmf.format(sm.updateTime));
            mssageJson.put("message", sm.message);
            mssageJson.put("date", sdf.format(sm.releaseDate));

            long diff = DateU.GetDiffDate(sm.releaseDate, system);

            if ( 0 <= diff && diff < 5) {
              mssageJson.put("diff", diff);
            }
            else {
              mssageJson.put("diff", 5);
            }
            if (diff == 0) {
              mssageJson.put("new", 1);
            }
            else {
              mssageJson.put("new", 0);
            }

            listJson.add(mssageJson);
          }
          resultJson.put("datalist", listJson);
          resultJson.put("browse", 1);
        }
        else {
          resultJson.put("browse", 0);
        }

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】システムメッセージ更新処理
     */
    public static Result commitsystemmessage() {
    	/* JSONデータを取得 */
        JsonNode jsonData 	= request().body().asJson();

    	session(AgryeelConst.SessionKey.ACCOUNTID, jsonData.get("accountId").asText());								//アカウントIDをセッションに格納
        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        controllers.Application.commitSystemMessage(jsonData.get("key").asText());
        resultJson.put("result"   , "SUCCESS");
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】ワンメッセージ取得処理
     */
    public static Result getonemessage(String accountId) {
    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.API, "true");

        return controllers.CommonController.getOneMessage();
    }

	/**
	 * 担当者別収量一覧
	 * @param farmId 生産者ID
	 * @param start 検索開始日付
	 * @param end   検索収量日付
	 * @return
	 */
    public static Result tantousyuryo(double farmId, String start, String end) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectNode listJson = Json.newObject();

        List<Account> accounts = Account.find.where().eq("farm_id", farmId).orderBy("acount_kana").findList();
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
        try {
          java.sql.Date dStart  = new java.sql.Date(sdf.parse(start).getTime());
          java.sql.Date dEnd  = new java.sql.Date(sdf.parse(end).getTime());
          if (accounts.size() > 0) {

            /* 取得した担当者毎に収穫量を取得する */
            for (Account account : accounts) {
              ShukakuofAccount sa = new ShukakuofAccount(account.farmId);
                List<models.WorkDiary> workDiarys = models.WorkDiary.find.where().eq("account_id", account.accountId).eq("work_id", AgryeelConst.WorkInfo.SHUKAKU).between("work_date", dStart, dEnd).findList();

                for (models.WorkDiary workDiary : workDiarys) {
                    /* 該当収穫時期の生産物を元帳照会から取得する */
                    models.MotochoBase base = MotochoBase.find.where().eq("kukaku_id", workDiary.kukakuId).le("shukaku_start_date", workDiary.workDate).ge("shukaku_end_date", workDiary.workDate).findUnique();
                    if (base != null) {
                      sa.updateHarvest(base.cropId, workDiary.shukakuRyo);
                    }
                }
                  ObjectNode dataJson = sa.getAccountHarvest();
                  ObjectNode accountJson = Json.newObject();
                  accountJson.put("id", account.accountId);
                  accountJson.put("name", account.acountName);
                  accountJson.put("data", dataJson);
                  listJson.put(account.accountId, accountJson);
            }

          ShukakuofAccount sa = new ShukakuofAccount(farmId);
              ObjectNode dataJson = sa.getCropList();
              resultJson.put("cropList", dataJson);
              resultJson.put("dataList", listJson);

          }
          else {
            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
          }
        } catch (ParseException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        }
        return ok(resultJson);
    }
    /**
     * 担当者別作業時間一覧
     * @param farmId 生産者ID
     * @param start 検索開始日付
     * @param end   検索収量日付
     * @return
     */
      public static Result tantouworktime(double farmId, String start, String end) {

          /* 戻り値用JSONデータの生成 */
          ObjectNode resultJson = Json.newObject();
          ObjectNode listJson = Json.newObject();

          List<Account> accounts = Account.find.where().eq("farm_id", farmId).orderBy("acount_kana").findList();
          SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
          try {
            java.sql.Date dStart  = new java.sql.Date(sdf.parse(start).getTime());
            java.sql.Date dEnd  = new java.sql.Date(sdf.parse(end).getTime());
            if (accounts.size() > 0) {

              /* 取得した担当者毎に収穫量を取得する */
              for (Account account : accounts) {
                WorktimeAccount wa = new WorktimeAccount(account.farmId);
                  List<models.WorkDiary> workDiarys = models.WorkDiary.find.where().eq("account_id", account.accountId).between("work_date", dStart, dEnd).findList();

                  for (models.WorkDiary workDiary : workDiarys) {
                    wa.updateWorkTime(workDiary.workId, workDiary.workTime);
                  }
                  ObjectNode dataJson = wa.getAccountWorktime();
                  ObjectNode accountJson = Json.newObject();
                  accountJson.put("id", account.accountId);
                  accountJson.put("name", account.acountName);
                  accountJson.put("data", dataJson);
                  listJson.put(account.accountId, accountJson);
              }

              WorktimeAccount wa = new WorktimeAccount(farmId);
              ObjectNode dataJson = wa.getWorkList();
              resultJson.put("workList", dataJson);
              resultJson.put("dataList", listJson);

            }
            else {
              resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
            }
          } catch (ParseException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
          }
          return ok(resultJson);
      }
	/**
	 * 作業担当者一覧取得
	 * @param farmId 生産者ID
	 * @return
	 */
    public static Result getaccount(double farmId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      List<Account> accounts = Account.find.where().eq("farm_id", farmId).orderBy("acount_kana").findList();
  	  if (accounts.size() > 0) {

  	    /* 取得した担当者毎に収穫量を取得する */
      	for (Account account : accounts) {
      	  ObjectNode accountJson = Json.newObject();
      	  accountJson.put("id"	, account.accountId);
      	  accountJson.put("name", account.acountName);
      	  listJson.put(account.accountId, accountJson);
      	}
      	resultJson.put("dataList", listJson);
  	  }
  	  else {
  	    resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
  	  }

	    return ok(resultJson);
    }
  	/**
  	 * 生産物一覧取得
  	 * @param farmId 生産者ID
  	 * @return
  	 */
      public static Result getcropoffarm(double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        CropGroup cg = CropGroup.find.where().eq("farm_id", farmId).orderBy("crop_group_id").findUnique();

        if (cg != null) { //生産物が存在する場合

            List<CropGroupList> cgl = CropGroupList.find.where().eq("crop_group_id", cg.cropGroupId).order("crop_id").findList();

            for (CropGroupList cropGroupList : cgl) {

              Crop crop = Crop.find.where().eq("crop_id", cropGroupList.cropId).findUnique();

              if (crop != null) {
                if (crop.deleteFlag == 1) { // 削除済みの場合
                  continue;
                }

                ObjectNode jd = Json.newObject();

                jd.put("id"   , crop.cropId);
                jd.put("name" , crop.cropName);
                jd.put("flag" , 0);

                listJson.add(jd);
              }
            }
            resultJson.put("datalist", listJson);
        }
        else {
        	resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
        }

  	    return ok(resultJson);
      }
      public static Result plandata(double kukakuId, double cropId, String hahsu, int prev, int next, int prevyear) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson   = Json.newObject();
        ObjectNode listJson     = Json.newObject();
        List<MotochoBase> bases = new ArrayList<MotochoBase>();

        SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");

        try {

          Calendar cStart   = Calendar.getInstance();
          Calendar cEnd     = Calendar.getInstance();
          int baseYear = cStart.get(Calendar.YEAR);

          for (int i = prevyear; i < 0; i--) {
            cStart.setTime(sdf.parse(hahsu));
            cEnd.setTime(sdf.parse(hahsu));
            cStart.set(Calendar.YEAR, baseYear - i);
            cEnd.set(Calendar.YEAR, baseYear - i);

            cStart.add(Calendar.DATE, (-1 * prev));
            cEnd.add(Calendar.DATE, next);

            java.sql.Date dStart  = new java.sql.Date(cStart.getTime().getTime());
            java.sql.Date dEnd  = new java.sql.Date(cEnd.getTime().getTime());

            List<MotochoBase> motochos = MotochoBase.find.where().eq("kukaku_id", kukakuId).eq("crop_id", cropId).between("hashu_date", dStart, dEnd).findList();
            for (MotochoBase motocho : motochos) {
              bases.add(motocho);
            }

          }

        } catch (ParseException e) {
          // TODO 自動生成された catch ブロック
          e.printStackTrace();
        }

        int seq = 0;
        SimpleDateFormat sdf2  = new SimpleDateFormat("yyyy/MM/dd");
        for (MotochoBase motocho : bases) {

          ObjectNode dataJson     = Json.newObject();
          seq++;

          dataJson.put("no", seq);
          dataJson.put("hinsyu", motocho.hinsyuName);
          dataJson.put("works", sdf2.format(motocho.workStartDay));
          dataJson.put("hashu", sdf2.format(motocho.hashuDate));
          dataJson.put("shukakus", sdf2.format(motocho.shukakuStartDate));
          dataJson.put("shukakue", sdf2.format(motocho.shukakuEndDate));
          dataJson.put("shukakur", sdf2.format(motocho.totalShukakuNumber));

          listJson.put(String.valueOf(seq), dataJson);

        }

        resultJson.put("datalist", listJson);

        return ok(resultJson);
      }

    /**
     * 総収穫分析情報出力
     */
    public static Result totalshukakudataoutput(double farmId, String dateFrom, String dateTo) {
    	session(AgryeelConst.SessionKey.API, "true");

        return controllers.AICAController.TotalShukakuDataOutput(farmId, dateFrom, dateTo);
    }

    /**
     * 収穫分析情報出力
     */
    public static Result shukakudataoutput(double farmId, String dateFrom, String dateTo, String cropId) {
    	session(AgryeelConst.SessionKey.API, "true");

        return controllers.AICAController.ShukakuDataOutput(farmId, dateFrom, dateTo, cropId);
    }

    /**
     * 収穫分析情報出力(区画)
     */
    public static Result shukakukukakudataoutput(double kukakuId, String dateTo) {
    	session(AgryeelConst.SessionKey.API, "true");

        return controllers.AICAController.ShukakuKukakuDataOutput(kukakuId, dateTo);
    }

    /**
     * 作業分析情報出力
     */
    public static Result totalworkdataoutput(double farmId, String dateFrom, String dateTo, String accounts, String works, String crops) {
    	session(AgryeelConst.SessionKey.API, "true");

        return controllers.AICAController.TotalWorkDataOutput(farmId, dateFrom, dateTo, accounts, works, crops);
    }

    /**
     * 作業計画取得
     */
    public static Result getWorkPlan(String targetDate, String accountId) {

      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      session(AgryeelConst.SessionKey.API, "true");

      return controllers.WorkPlanController.getWorkPlan(targetDate, accountId, 0);
    }

    /**
     * 作業計画取得（複数）
     */
    public static Result getWorkPlans(String targetDate, String accountId, String accounts) {

      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      session(AgryeelConst.SessionKey.API, "true");

      return controllers.WorkPlanController.getWorkPlan(targetDate, accounts, 0);
    }

    /**
     * 作業計画順序変更
     */
    public static Result workplanuuidondaychange(String accountId) {

      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      Logger.info("[ WORKPLANUUIDCHANGE_API ] ACCOUNTID={}", accountId);
      return controllers.WorkPlanController.workPlanUuidOnDayChange();
    }

    /**
     * AI栽培管理予測結果
     * @return 栽培管理予測結果JSON
     */
    public static Result aicasaibaimanage(String accountId, double kukakuId) {
        session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.API, "true");
        return controllers.AICAController.aicaSaibaiManage(kukakuId);
    }

    /**
     * アクティブログ取得
     */
    public static Result getactivelog(String startDate, String endDate) {

      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper   = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJson    = mapper.createArrayNode();

      //----------------------------------------------------------------------------
      //- アクティブログを取得する
      //----------------------------------------------------------------------------
      SimpleDateFormat sdfp = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat sdff2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

      Calendar start    = Calendar.getInstance();
      Calendar end      = Calendar.getInstance();
      try {

        start.setTime(sdfp.parse(startDate));
        end.setTime(sdfp.parse(endDate));
        DateU.setTime(start, DateU.TimeType.FROM);
        DateU.setTime(end, DateU.TimeType.TO);

    	List<ActiveLog> als = ActiveLog.find.where().between("active_time",  new java.sql.Timestamp(start.getTimeInMillis()), new java.sql.Timestamp(end.getTimeInMillis())).orderBy("active_time desc").findList();

        int idx = 0;

        for (ActiveLog al : als) {
          ObjectNode jd = Json.newObject();
          jd.put("activeTime"      , sdff2.format(al.activeTime));                           //更新日時
          jd.put("accountId"       , al.accountId);                                          //アカウントID
          jd.put("screenId"        , al.screenId);                                           //画面ID
          jd.put("actionMessage"   , al.actionMessage);                                      //アクションメッセージ

          listJson.add(jd);
        }
      } catch (ParseException e) {
        e.printStackTrace();
      }

      resultJson.put("datalist" , listJson);

      return ok(resultJson);
    }

    /**
     * 全生産者の作業記録情報を取得する
     * @return
     */
    public static Result getallfarmworkdiaryinfo() {

        /* 戻り値用JSONデータの生成 */
    	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode resultJson = Json.newObject();
        ArrayNode listJson = mapper.createArrayNode();
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        List<Farm> farmList = Farm.find.order("farm_id ASC").findList();

        if (farmList.size() > 0) { //該当データが存在する場合
          for (Farm farm : farmList) {
            //該当生産者のタイムラインを取得
            List<TimeLine> tls = TimeLine.find.where().eq("farm_id", farm.farmId).order("update_time DESC").setMaxRows(1).findList();
            int count = Ebean.find(TimeLine.class).where().eq("farm_id", farm.farmId).findRowCount();

            for (TimeLine tl : tls) {

              ObjectNode jd = Json.newObject();

              jd.put("id"         , farm.farmId);
              jd.put("name"       , farm.farmName);
              jd.put("count"      , count);
              jd.put("updateTime" , sdf.format(tl.updateTime));

              listJson.add(jd);
              break;
            }
          }
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 生産者情報取得
     */
    public static Result getfarminfo(double pFarmId) {

      session(AgryeelConst.SessionKey.FARMID, String.valueOf(pFarmId));

      return controllers.FarmSetting.farmSettingInit();
    }

    /**
     * アカウント情報取得
     */
    public static Result getaccountinfo(double pFarmId, String accountId) {

      session(AgryeelConst.SessionKey.FARMID, String.valueOf(pFarmId));
      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      session(AgryeelConst.SessionKey.ACCOUNTID_SEL, accountId);

      return controllers.AccounSetting.accountSettingInit();
    }

    /**
     * 【AGRYEEL】生産者契約プラン保存処理
     * @return
     */
    public static Result submitContractPlan(double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        //---------- 生産者ステータス情報の更新 ----------
        FarmComprtnent fc = new FarmComprtnent();
        fc.GetFarmData(farmId);
        fc.farmStatusData.contractPlan = input.get("contractPlan").asInt();
        fc.farmStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】生産者データ使用許可保存処理
     * @return
     */
    public static Result submitDataUsePermission(double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        //---------- 生産者ステータス情報の更新 ----------
        FarmComprtnent fc = new FarmComprtnent();
        fc.GetFarmData(farmId);
        fc.farmStatusData.dataUsePermission = input.get("dataUsePermission").asInt();
        fc.farmStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }


    /**
     * 【AICA】生産者データ支払情報保存処理
     * @return
     */
    public static Result submitPaymentInfo(double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        FarmComprtnent fc = new FarmComprtnent();

        //---------- 生産者ステータス情報の更新 ----------
        fc.GetFarmData(farmId);
        try {
            fc.farmStatusData.lastCollectionDate  = new java.sql.Date(sdf.parse(input.get("lastCollectionDate").asText().replace("/", "")).getTime());
            fc.farmStatusData.nextCollectionDate  = new java.sql.Date(sdf.parse(input.get("nextCollectionDate").asText().replace("/", "")).getTime());
        } catch (ParseException e) {
            Calendar cal = Calendar.getInstance();
            fc.farmStatusData.lastCollectionDate  = new java.sql.Date(cal.getTime().getTime());
            fc.farmStatusData.nextCollectionDate  = new java.sql.Date(cal.getTime().getTime());
        }
        fc.farmStatusData.paymentKubun = input.get("paymentKubun").asInt();
        fc.farmStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
    /**
     * 担当者別作業時間の取得
     * @param accountId
     * @param targetDate
     * @return
     */
    public static Result getWorkingAccountData(String accountId, String targetDate) {
      //必要な情報をセッションに格納
      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);
      session(AgryeelConst.SessionKey.API, "true");

      return WorkingAccount.init(targetDate);
    }
    public static Result getWorkPlanAccount(String accountId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();

      ObjectMapper mapper     = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode    apiJson    = mapper.createArrayNode();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(accountId);

      List<Account> accounts = Account.getAccountOfFarm(accountComprtnent.accountData.farmId);

      if (accountComprtnent.accountData.managerRole == AgryeelConst.ManegerRoll.TANTO) {                                //担当の場合、自分のみ
        ObjectNode jd = Json.newObject();

        jd.put("id"   , accountComprtnent.accountData.accountId);
        jd.put("name" , accountComprtnent.accountData.acountName);
        jd.put("flag" , 0);

        apiJson.add(jd);
      }
      else {                                                                                                            //担当以外は、全員＋担当者未選択
        if (accounts.size() > 0) { //該当データが存在する場合
          for (Account account : accounts) {
            if (account.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode jd = Json.newObject();

            jd.put("id"   , account.accountId);
            jd.put("name" , account.acountName);
            jd.put("flag" , 0);

            apiJson.add(jd);

          }
        }
        ObjectNode jd = Json.newObject();

        jd.put("id"   , AgryeelConst.SpecialAccount.ALLACOUNT);
        jd.put("name" , "担当者未選択");
        jd.put("flag" , 0);

        apiJson.add(jd);
      }

      resultJson.put("data", apiJson);
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }

    /**
     * 育苗作業情報を取得する
     * @return 育苗作業情報JSON
     */
    public static Result getikubyowork() {
      session(AgryeelConst.SessionKey.API, "true");

      return controllers.IkubyoController.getIkubyoWork();
    }

    /**
     * 苗状況照会を取得する
     * @return 苗状況照会JSON
     */
    public static Result getnaesp(String accountId) {
      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      session(AgryeelConst.SessionKey.API, "true");

      return controllers.IkubyoController.getNaeSP();
    }

    /**
     * 苗詳細情報を取得する
     * @return 苗状況照会JSON
     */
    public static Result getnaedetail(String accountId) {
      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      session(AgryeelConst.SessionKey.API, "true");

      return controllers.IkubyoController.getNaeDetail();
    }

    /**
     * 初期検索育苗ラインデータを取得する
     */
    public static Result getikubyolineinitdata(String accountId) {

      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      session(AgryeelConst.SessionKey.API, "true");

      return controllers.IkubyoController.getIkubyoLineInitData();
    }

    /**
     * 育苗ライン検索条件をもとに育苗ライン情報を取得する
     */
    public static Result getikubyolinedata(String accountId) {

      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);                //アカウントIDをセッションに格納
      session(AgryeelConst.SessionKey.API, "true");

      return controllers.IkubyoController.getIkubyoLineData();
    }

    /**
     * 苗状況照会検索条件をアカウント状況に反映する
     * @return
     */
    public static Result naesearchcommit(String accountId) {
      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

      return controllers.IkubyoController.naesearchCommit();
    }

    /**
     * 育苗ライン検索条件をアカウント状況に反映する
     * @return
     */
    public static Result ikubyolinestatuscommit(String accountId) {
      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

      return controllers.IkubyoController.ikubyolineStatusCommit();
    }

    /**
     * 生産者IDから苗情報一覧を取得する
     * @return
     */
    public static Result getnaeoffarm(double pFarmId) {

      /* 戻り値用JSONデータの生成 */
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ObjectNode resultJson = Json.newObject();
      ArrayNode listJson = mapper.createArrayNode();

      int result = NaeStatusCompornent.getNaeOfFarmJsonArray(pFarmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
    }

    /**
     * 苗Noから苗情報一覧を取得する
     * @return
     */
    public static Result getnaeinfolist(String naeNo) {

      /* 戻り値用JSONデータの生成 */
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ObjectNode resultJson = Json.newObject();
      ArrayNode listJson = mapper.createArrayNode();

      int result = NaeStatusCompornent.getNaeInfoJsonArray(naeNo, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
    }

    /**
     * 育苗計画初期表示データ取得
     * @return
     */
    public static Result ikubyoplaninit(String accountId, String accountName, double farmId, double ikubyoPlanId) {
        session(AgryeelConst.SessionKey.IKUBYOPLANID, String.valueOf(ikubyoPlanId));
        return ikubyodiaryinit(accountId, accountName, farmId, 0, "NONE", -99999);
    }

    /**
     * 【AGRYEEL】育苗日誌初期表示データ取得
     * @return
     */
    public static Result ikubyodiaryinit(String accountId, String accountName, double farmId, double workId, String naeNo, double ikubyoDiaryId) {

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.ACCOUNTNAME, accountName);							//アカウント名をセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));
        if(workId != 0){
    	    session(AgryeelConst.SessionKey.WORKID, String.valueOf(workId));
        }else{
    	    session(AgryeelConst.SessionKey.WORKID, "");
        }
        session(AgryeelConst.SessionKey.NAENO, naeNo.replace("NONE", ""));
        if(ikubyoDiaryId != 0 && ikubyoDiaryId != -99999){
            session(AgryeelConst.SessionKey.IKUBYODIARYID, String.valueOf(ikubyoDiaryId));
        }else{
            session(AgryeelConst.SessionKey.IKUBYODIARYID, "");
        }
        if(ikubyoDiaryId != -99999){
            session(AgryeelConst.SessionKey.IKUBYOPLANID, "");
        }
        session(AgryeelConst.SessionKey.API, "true");

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
/*
        resultJson = controllers.IkubyoDiary.ikubyoDiaryInit();

        return ok(resultJson);
*/
        return controllers.IkubyoDiary.ikubyoDiaryInit();
    }

    /**
     * 【AGRYEEL】育苗日誌記録処理
     * @return
     */
    public static Result submitikubyodiary(String accountId, double farmId, double ikubyoDiaryId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));

        controllers.IkubyoDiary.updateIkubyoDiary(resultJson, input, ikubyoDiaryId, true);

        return ok(resultJson);
    }

    /**
     * 育苗作業中断要求
     * @return
     */
    public static Result workingikubyostop(String accountId) {

        session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納

        return controllers.WorkingIkubyo.workingstop();
    }

    /**
     * 【AGRYEEL】育苗日誌削除処理
     * @return
     */
    public static Result ikubyodiarydelete(String accountId, double farmId, double ikubyoDiaryId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));

        controllers.IkubyoDiary.ikubyoDiaryDelete(ikubyoDiaryId);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】育苗計画記録処理
     * @return
     */
    public static Result submitikubyoplan(String accountId, double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

    	session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
    	session(AgryeelConst.SessionKey.FARMID, String.valueOf(farmId));

        controllers.IkubyoDiary.updateIkubyoPlan(resultJson, input, true);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】育苗計画記録完了処理
     * @return
     */
    public static Result plantodiaryikubyo(String accountId) {

      Logger.info("[ PLANTODIARYIKUBYO_API ] ACCOUNTID={}", accountId);
      ObjectNode  resultJson = Json.newObject();

      session(AgryeelConst.SessionKey.ACCOUNTID, accountId);								//アカウントIDをセッションに格納
      Account ac = Account.getAccount(accountId);
      if (ac != null) {
        session(AgryeelConst.SessionKey.FARMID, String.valueOf(ac.farmId));
      }
      else {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
        return ok(resultJson);
      }

      return controllers.IkubyoDiary.planToDiaryIkubyo();
    }

    /**
     * 生産者別容器情報取得
     * @return
     */
    public static Result getyoukioffarm(double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        List<Youki> datas = Youki.getYoukiOfFarm(farmId);

        for (Youki data : datas) {
          if (data.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          ObjectNode jd = Json.newObject();

          jd.put("id"   , data.youkiId);
          jd.put("name" , data.youkiName);
          jd.put("flag" , 0);

          listJson.add(jd);
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }

    /**
     * 生産者別土情報取得
     * @return
     */
    public static Result getsoiloffarm(double farmId, int soilKind) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJson = mapper.createArrayNode();

        List<Soil> datas = Soil.getSoilOfFarm(farmId);

        for (Soil data : datas) {
          if (data.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          if (data.soilKind != soilKind) {
            continue;
          }

          ObjectNode jd = Json.newObject();

          jd.put("id"   , data.soilId);
          jd.put("name" , data.soilName);
          jd.put("flag" , 0);

          listJson.add(jd);
        }

        resultJson.put("datalist" , listJson);

        return ok(resultJson);
    }


	/**
	 * 育苗作業中情報を取得する
	 * @param accountId アカウントＩＤ
	 * @return 取得結果
	 */
    public static Result getikubyoworking(String accountId) {
        SimpleDateFormat sts = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

		//アカウントIDをキーにアカウント情報を取得する
		Account account = Account.find.where().eq("account_id", accountId).findUnique();

    	if (account != null) { //アカウントが取得できた場合

      		resultJson.put("field"   , account.fieldId);
            if (account.workPlanId != 0) {
              List<models.WorkPlan> workplans = models.WorkPlan.find.where().eq("work_plan_id", account.workPlanId).eq("account_id", account.accountId).orderBy("work_plan_id").findList();
              String sKukaku = "";
              for (models.WorkPlan workplan: workplans) {
                Compartment cd = FieldComprtnent.getCompartment(workplan.kukakuId);
                if (cd != null) {
                  if (!"".equals(sKukaku)) {
                    sKukaku += ",";
                  }
                  sKukaku += cd.kukakuName;
                }
              }
              resultJson.put("fieldnm", sKukaku);
            }
            else {
              Compartment cp = Compartment.getCompartmentInfo(account.fieldId);
              if(cp != null){
                resultJson.put("fieldnm"    , cp.kukakuName);
              }else{
                resultJson.put("fieldnm"    , "");
              }
            }
      		resultJson.put("work"    , account.workId);
      		Work wk = Work.getWork(account.workId);
      		if(wk != null){
    	  		resultJson.put("worknm"    , wk.workName);
      		}else{
    	  		resultJson.put("worknm"    , "");
      		}
      		if (account.workStartTime == null) {
        		resultJson.put("start"    , "");
      		}
      		else {
        		resultJson.put("start"    , sts.format(account.workStartTime));
      		}
      		resultJson.put("workPlanId"    , account.workPlanId);

            IkubyoPlanLine ipl = IkubyoPlanLine.find.where().eq("ikubyo_plan_id", account.workPlanId).findUnique();
            String[] sNaeNos = ipl.naeNo.split("-");
            NaeStatus ns = NaeStatus.find.where().eq("nae_no", ipl.naeNo).findUnique();
            if (ns != null) {
              resultJson.put("naeName"   , ns.hinsyuName + "(" + sNaeNos[1] + ")");                             //苗名
            }
            else {
              IkubyoPlan ip = IkubyoPlan.find.where().eq("ikubyo_plan_id", ipl.ikubyoPlanId).findUnique();
              resultJson.put("naeName"   ,  Hinsyu.getMultiHinsyuName(ip.hinsyuId) + "(" + sNaeNos[1] + ")");   //苗名
            }

    	}

        return ok(resultJson);
	}
}
