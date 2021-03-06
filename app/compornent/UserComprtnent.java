package compornent;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import models.Account;
import models.AccountIkubyoStatus;
import models.AccountStatus;
import models.Compartment;
import models.Farm;
import models.FarmGroup;
import models.FarmStatus;
import models.FieldGroup;
import models.FieldGroupList;
import models.MessageOfAccount;
import models.SystemMessage;
import models.Work;
import models.WorkChain;
import play.libs.Json;
import util.StringU;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】アカウントコンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class UserComprtnent implements AgryellInterface{

	/** ログイン認証成功 */
	public static final int 	LOGIN_SUCCESS				=  0;
	/** ログイン認証エラー（アカウント未存在） */
	public static final int 	LOGIN_NOTFOUND				=  1;
	/** ログイン認証エラー（パスワード不一致） */
	public static final int 	LOGIN_PASSWORDMISSMATCH		=  2;

	/** アカウント存在チェック（存在なし） */
	public static final int 	EXISTS_ACCOUNTCHECK_SUCCESS	=  0;
	/** アカウント存在チェック（存在あり） */
	public static final int 	EXISTS_ACCOUNTCHECK_NG		= -1;

	/** アカウント作成（成功） */
	public static final int 	MAKE_ACCOUNT_SUCCESS		=  0;
	/** アカウント作成（失敗） */
	public static final int 	MAKE_ACCOUNT_NG				= -1;

	/** 対象圃場作成（成功） */
	public static final int 	MAKE_FIELDGROUP_SUCCESS		=  0;
	/** 対象圃場作成（失敗） */
	public static final int 	MAKE_FIELDGROUP_NG				= -1;

	/** アカウント情報 */
	public Account	      accountData;
  public AccountStatus  accountStatusData;
    public AccountIkubyoStatus  accountIkubyoStatusData;
	public FarmGroup  farmGroupData;
	public FarmStatus farmStatusData;
  public Farm       farmData;

	/**
	 * コンストラクタ
	 */
	public UserComprtnent() {

		accountData       = null;
		accountStatusData = null;
		accountIkubyoStatusData = null;
		farmGroupData = null;
		farmStatusData = null;
		farmData      = null;

	}


	/**
	 * アカウント情報を取得する
	 * @param accountId アカウントＩＤ
	 * @return 取得結果
	 */
	public int GetAccountData(String accountId) {

		/** 戻り値 */
		int	result	=	GET_SUCCESS;

		//アカウントIDをキーにアカウント情報を取得する
		Account account = Account.find.where().eq("account_id", accountId).findUnique();

    if (account != null) { //アカウントが取得できた場合

    	accountData = account;
    	AccountStatus accountStatus = AccountStatus.find.where().eq("account_id", accountId).findUnique();
    	if (accountStatus != null) {
    	  accountStatusData = accountStatus;
    	}
    	AccountIkubyoStatus accountIkubyoStatus = AccountIkubyoStatus.find.where().eq("account_id", accountId).findUnique();
    	if (accountIkubyoStatus != null) {
    	  accountIkubyoStatusData = accountIkubyoStatus;
    	}
    	result = getFarmDataFromAccount();       //生産者情報を取得する
      result = getFarmGroupDataFromFarmData(); //生産者グループ情報を取得する
      result = getFarmStatusDataFromFarmData();  //生産者ステータス情報を取得する

    }
    else {
    	result	=	GET_ERROR;
    }

    return result;

	}

	/**
	 * アカウント情報を取得する
	 * @param accountId アカウントＩＤ
	 * @return アカウント情報リスト
	 */
	private List<Account> GetAccountInfo(String accountId) {

	    return Account.find.where().eq("account_id", accountId).eq("delete_flag", 0).findList();

	}

	/**
	 * アカウント情報から生産者情報を取得する
	 * @return
	 */
	private int getFarmDataFromAccount() {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    if (accountData != null) { //アカウント情報が存在する

      Farm farm = Farm.find.where().eq("farm_id", accountData.farmId).findUnique();

      if (farm != null) { //生産者情報が取得できた場合
        farmData = farm;
      }
      else {
        result  = GET_ERROR;
      }

    }
    else {
      result  = GET_ERROR;
    }

    return result;

	}
  /**
   * 生産者情報から生産者グループ情報を取得する
   * @return
   */
  private int getFarmGroupDataFromFarmData() {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    if (farmData != null) { //生産者情報が存在する

      FarmGroup farmGroup = FarmGroup.find.where().eq("farm_group_id", farmData.farmGroupId).findUnique();

      if (farmGroup != null) { //生産者グループ情報が取得できた場合
        farmGroupData = farmGroup;
      }
      else {
        result  = GET_ERROR;
      }

    }
    else {
      result  = GET_ERROR;
    }

    return result;

  }
  /**
   * 生産者情報から生産者ステータス情報を取得する
   * @return
   */
  private int getFarmStatusDataFromFarmData() {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    if (farmData != null) { //生産者情報が存在する

      FarmStatus farmStatus = FarmStatus.find.where().eq("farm_id", farmData.farmId).findUnique();

      if (farmStatus != null) { //生産者グループ情報が取得できた場合
        farmStatusData = farmStatus;
      }
      else {
        result  = GET_ERROR;
      }

    }
    else {
      result  = GET_ERROR;
    }

    return result;

  }

  /**
   * アカウント画像をBASE64で取得する
   * @return
   */
  public String getAccountPicture() {
    String result = "";

    if (accountData != null) { //アカウント情報が存在する場合
      if (accountData.accountPicture != null && accountData.accountPicture.length > 0) { //アカウント画像が存在する場合
        String base64str = DatatypeConverter.printBase64Binary(accountData.accountPicture);
        StringBuilder sb = new StringBuilder();
        sb.append("data:");
        sb.append("image/png");
        sb.append(";base64,");
        sb.append(base64str);
        result = sb.toString();
      }
    }

    return result;

  }

  /**
   * アカウント情報をJSONに格納する
   * @param pJson
   * @return
   */
  public int getAccountJson(ObjectNode pJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat sts = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    if (accountData != null) { //アカウント情報が存在する
      //----- アカウント情報 -----
      pJson.put("id"        , accountData.accountId);
      pJson.put("name"      , accountData.acountName);
      pJson.put("manager"   , accountData.managerRole);
      pJson.put("menu"      , accountData.menuRole);
      pJson.put("firstPage" , accountData.firstPage);
      pJson.put("field"     , accountData.fieldId);
      pJson.put("work"      , accountData.workId);
      if (accountData.workStartTime == null) {
        pJson.put("start"    , "");
      }
      else {
        pJson.put("start"    , sts.format(accountData.workStartTime));
      }

      String picture = getAccountPicture();
      if ("".equals(picture)) {
        pJson.put("picture"    , JSON_NONE);
      }
      else {
        pJson.put("picture"    , picture);
      }
      pJson.put("planId"    , accountData.workPlanId);

      //----- アカウントステータス情報 -----
      if (accountStatusData.selectStartDate == null) {
        pJson.put("selectStartDate"      , "");
      }
      else {
        pJson.put("selectStartDate"      , sdf.format(accountStatusData.selectStartDate));
      }
      if (accountStatusData.selectEndDate == null) {
        pJson.put("selectEndDate"      , "");
      }
      else {
        pJson.put("selectEndDate"      , sdf.format(accountStatusData.selectEndDate));
      }
      pJson.put("selectWorkId"      , accountStatusData.selectWorkId);
      pJson.put("selectAccountId"   , accountStatusData.selectAccountId);
      pJson.put("selectWorking"     , accountStatusData.selectWorking);
      pJson.put("selectFieldGroupId", accountStatusData.selectFieldGroupId);
      pJson.put("sskKukakuName"     , accountStatusData.sskKukakuName);
      pJson.put("sskMultiKukaku"    , accountStatusData.sskMultiKukaku);
      pJson.put("sskHinsyu"         , accountStatusData.sskHinsyu);
      pJson.put("sskSeiikuF"        , accountStatusData.sskSeiikuF);
      pJson.put("sskSeiikuT"        , accountStatusData.sskSeiikuT);
      pJson.put("displayStatus"     , accountStatusData.displayStatus);
      pJson.put("selectKukakuId"    , accountStatusData.selectKukakuId);
      pJson.put("selectCropId"      , accountStatusData.selectCropId);
      pJson.put("selectHinsyuId"    , accountStatusData.selectHinsyuId);
      pJson.put("workStartPrompt"   , accountStatusData.workStartPrompt);
      pJson.put("workChangeDisplay" , accountStatusData.workChangeDisplay);
      pJson.put("radius"            , accountStatusData.radius);
      pJson.put("workPlanInitId"    , accountStatusData.workPlanInitId);

      //----- アカウント育苗ステータス情報 -----
      if (accountIkubyoStatusData.selectStartDate == null) {
        pJson.put("selectStartDateIb"      , "");
      }
      else {
        pJson.put("selectStartDateIb"      , sdf.format(accountIkubyoStatusData.selectStartDate));
      }
      if (accountIkubyoStatusData.selectEndDate == null) {
        pJson.put("selectEndDateIb"      , "");
      }
      else {
        pJson.put("selectEndDateIb"      , sdf.format(accountIkubyoStatusData.selectEndDate));
      }
      pJson.put("selectWorkIdIb"    , accountIkubyoStatusData.selectWorkId);
      pJson.put("selectAccountIdIb" , accountIkubyoStatusData.selectAccountId);
      pJson.put("selectCropIdIb"    , accountIkubyoStatusData.selectCropId);
      pJson.put("selectHinsyuIdIb"  , accountIkubyoStatusData.selectHinsyuId);
      pJson.put("selectWorkingIb"   , accountIkubyoStatusData.selectWorking);
      pJson.put("ssnCrop"           , accountIkubyoStatusData.ssnCrop);
      pJson.put("ssnHinsyu"         , accountIkubyoStatusData.ssnHinsyu);
      pJson.put("ssnSeiikuF"        , accountIkubyoStatusData.ssnSeiikuF);
      pJson.put("ssnSeiikuT"        , accountIkubyoStatusData.ssnSeiikuT);

      //----- 生産者情報 -----
      if (farmData != null) { //生産者情報が存在する
        pJson.put("farmid"      , farmData.farmId);
        pJson.put("farmname"    , farmData.farmName);
      }
      else {
        pJson.put("farmid"            , JSON_NONE);
        pJson.put("farmname"          , JSON_NONE);
      }

      //----- 生産者グループ -----
      if (farmGroupData != null) { //生産者情報が存在する
        pJson.put("farmgroupid"      , farmGroupData.farmGroupId);
        pJson.put("farmgroupname"    , farmGroupData.farmGroupName);
      }
      else {
        pJson.put("farmgroupid"       , JSON_NONE);
        pJson.put("farmgroupname"     , JSON_NONE);
      }

      //----- 生産者ステータス -----
      if (farmStatusData != null) { //生産者ステータス情報が存在する
        pJson.put("contractplan"      , farmStatusData.contractPlan);
        pJson.put("ikubyoFunction"    , farmStatusData.ikubyoFunction);
        pJson.put("kukakuSelectMethod", farmStatusData.kukakuSelectMethod);
      }
      else {
        pJson.put("contractplan"      , JSON_NONE);
        pJson.put("ikubyoFunction"    , JSON_NONE);
        pJson.put("kukakuSelectMethod", JSON_NONE);
      }

      //----- 担当者情報 -----
      int status = 0;
      List<Account> accounts = Account.getAccountOfWorking(farmData.farmId);
      for (Account account : accounts) {
        if (account.messageIcon != AgryeelConst.MessageIcon.NONE) {
          status = 1;
        }
      }
      pJson.put("status"       , status);
      //----- 生産者内担当者 -----
      accounts = Account.getAccountOfFarm(farmData.farmId);
      String ids = "";
      for (Account account : accounts) {
        if (!"".equals(ids)) {
          ids += ",";
        }
        ids += account.accountId;
      }
      pJson.put("ids"               , ids);
      pJson.put("workDiaryDiscription" , accountStatusData.workDiaryDiscription);
    }
    else {
      //----- アカウント情報 -----
      pJson.put("id"                , JSON_NONE);
      pJson.put("name"              , JSON_NONE);
      pJson.put("manager"           , JSON_NONE);
      pJson.put("menu"              , JSON_NONE);
      pJson.put("firstPage"         , JSON_NONE);
      pJson.put("picture"           , JSON_NONE);
      pJson.put("farmid"            , JSON_NONE);
      pJson.put("farmname"          , JSON_NONE);
      pJson.put("farmgroupid"       , JSON_NONE);
      pJson.put("farmgroupname"     , JSON_NONE);
      pJson.put("selectStartDate"   , JSON_NONE);
      pJson.put("selectEndDate"     , JSON_NONE);
      pJson.put("selectWorkId"      , JSON_NONE);
      pJson.put("selectAccountId"   , JSON_NONE);
      pJson.put("selectWorking"     , JSON_NONE);
      pJson.put("selectFieldGroupId", JSON_NONE);
      pJson.put("sskKukakuName"     , JSON_NONE);
      pJson.put("sskMultiKukaku"    , JSON_NONE);
      pJson.put("sskHinsyu"         , JSON_NONE);
      pJson.put("sskSeiikuF"        , JSON_NONE);
      pJson.put("sskSeiikuT"        , JSON_NONE);
      pJson.put("displayStatus"     , JSON_NONE);
      pJson.put("status"            , JSON_NONE);
      pJson.put("selectKukakuId"    , JSON_NONE);
      pJson.put("selectCropId"      , JSON_NONE);
      pJson.put("selectHinsyuId"    , JSON_NONE);
      pJson.put("radius"            , JSON_NONE);
      pJson.put("workPlanInitId"    , JSON_NONE);
      pJson.put("ids"               , JSON_NONE);
      pJson.put("workDiaryDiscription" , JSON_NONE);
      result  = GET_ERROR;
    }

    return result;

  }

  /**
   * アカウント情報をJSONに格納する
   * @param pJson
   * @return
   */
  public int getAccountJsonAPI(ObjectNode pJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat sts = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    if (accountData != null) { //アカウント情報が存在する
      //----- アカウント情報 -----
      pJson.put("id"        , accountData.accountId);
      pJson.put("name"      , accountData.acountName);
      pJson.put("manager"   , accountData.managerRole);
      pJson.put("menu"      , accountData.menuRole);
      pJson.put("firstPage" , accountData.firstPage);

      pJson.put("field"   , accountData.fieldId);
      Compartment cp = Compartment.getCompartmentInfo(accountData.fieldId);
      if(cp != null){
    	  FieldGroup fg = FieldGroupList.getFieldGroup(cp.fieldId);
    	  pJson.put("fieldnm"    , cp.kukakuName);
    	  pJson.put("fieldcolor" , fg.fieldGroupColor);
      }else{
    	  pJson.put("fieldnm"    , JSON_NONE);
    	  pJson.put("fieldcolor" , JSON_NONE);
      }
      pJson.put("work"    , accountData.workId);
      Work wk = Work.getWork(accountData.workId);
      if(wk != null){
    	  pJson.put("worknm"    , wk.workName);
    	  pJson.put("workcolor" , wk.workColor);
      }else{
    	  pJson.put("worknm"    , JSON_NONE);
    	  pJson.put("workcolor" , JSON_NONE);
      }
      if (accountData.workStartTime == null) {
        pJson.put("start"    , "");
      }
      else {
        pJson.put("start"    , sts.format(accountData.workStartTime));
      }
      pJson.put("workPlanId"    , accountData.workPlanId);
      pJson.put("heartRateUpLimit"   , accountData.heartRateUpLimit);
      pJson.put("heartRateDownLimit"   , accountData.heartRateDownLimit);

      //----- アカウントステータス情報 -----
      pJson.put("selectChainId"      , accountStatusData.selectChainId);
      if (accountStatusData.selectStartDate == null) {
        pJson.put("selectStartDate"      , "");
      }
      else {
        pJson.put("selectStartDate"      , sdf.format(accountStatusData.selectStartDate));
      }
      if (accountStatusData.selectEndDate == null) {
        pJson.put("selectEndDate"      , "");
      }
      else {
        pJson.put("selectEndDate"      , sdf.format(accountStatusData.selectEndDate));
      }
      if (accountStatusData.selectWorkId == null) {
    	  pJson.put("selectWorkId"      , "");
      }else{
    	  pJson.put("selectWorkId"      , accountStatusData.selectWorkId);
      }
      if (accountStatusData.selectAccountId == null) {
    	  pJson.put("selectAccountId"      , "");
      }else{
    	  pJson.put("selectAccountId"      , accountStatusData.selectAccountId);
      }
      pJson.put("selectWorking"     , accountStatusData.selectWorking);
      pJson.put("selectFieldGroupId", accountStatusData.selectFieldGroupId);
      if (accountStatusData.sskKukakuName == null) {
    	  pJson.put("sskKukakuName"      , "");
      }else{
    	  pJson.put("sskKukakuName"      , accountStatusData.sskKukakuName);
      }
      if (accountStatusData.sskMultiKukaku == null) {
    	  pJson.put("sskMultiKukaku"      , "");
      }else{
    	  pJson.put("sskMultiKukaku"      , accountStatusData.sskMultiKukaku);
      }
      if (accountStatusData.sskHinsyu == null) {
    	  pJson.put("sskHinsyu"      , "");
      }else{
    	  pJson.put("sskHinsyu"      , accountStatusData.sskHinsyu);
      }
      pJson.put("sskSeiikuF"        , accountStatusData.sskSeiikuF);
      pJson.put("sskSeiikuT"        , accountStatusData.sskSeiikuT);
      pJson.put("displayStatus"     , accountStatusData.displayStatus);
      if (accountStatusData.selectKukakuId == null) {
    	  pJson.put("selectKukakuId"      , "");
      }else{
    	  pJson.put("selectKukakuId"      , accountStatusData.selectKukakuId);
      }
      if (accountStatusData.selectCropId == null) {
    	  pJson.put("selectCropId"      , "");
      }else{
    	  pJson.put("selectCropId"      , accountStatusData.selectCropId);
      }
      if (accountStatusData.selectHinsyuId == null) {
    	  pJson.put("selectHinsyuId"      , "");
      }else{
    	  pJson.put("selectHinsyuId"      , accountStatusData.selectHinsyuId);
      }
      pJson.put("workStartPrompt"   , accountStatusData.workStartPrompt);
      pJson.put("workChangeDisplay" , accountStatusData.workChangeDisplay);

      //----- アカウント育苗ステータス情報 -----
      if (accountIkubyoStatusData.selectStartDate == null) {
        pJson.put("selectStartDateIb"      , "");
      }
      else {
        pJson.put("selectStartDateIb"      , sdf.format(accountIkubyoStatusData.selectStartDate));
      }
      if (accountIkubyoStatusData.selectEndDate == null) {
        pJson.put("selectEndDateIb"      , "");
      }
      else {
        pJson.put("selectEndDateIb"      , sdf.format(accountIkubyoStatusData.selectEndDate));
      }
      if (accountIkubyoStatusData.selectWorkId == null) {
    	  pJson.put("selectWorkIdIb"      , "");
      }else{
    	  pJson.put("selectWorkIdIb"      , accountIkubyoStatusData.selectWorkId);
      }
      if (accountIkubyoStatusData.selectAccountId == null) {
    	  pJson.put("selectAccountIdIb"      , "");
      }else{
    	  pJson.put("selectAccountIdIb"      , accountIkubyoStatusData.selectAccountId);
      }
      if (accountIkubyoStatusData.selectCropId == null) {
    	  pJson.put("selectCropIdIb"      , "");
      }else{
    	  pJson.put("selectCropIdIb"      , accountIkubyoStatusData.selectCropId);
      }
      if (accountIkubyoStatusData.selectHinsyuId == null) {
    	  pJson.put("selectHinsyuIdIb"      , "");
      }else{
    	  pJson.put("selectHinsyuIdIb"      , accountIkubyoStatusData.selectHinsyuId);
      }
      pJson.put("selectWorkingIb"     , accountIkubyoStatusData.selectWorking);
      if (accountIkubyoStatusData.ssnCrop == null) {
    	  pJson.put("ssnCrop"      , "");
      }else{
    	  pJson.put("ssnCrop"      , accountIkubyoStatusData.ssnCrop);
      }
      if (accountIkubyoStatusData.ssnHinsyu == null) {
    	  pJson.put("ssnHinsyu"      , "");
      }else{
    	  pJson.put("ssnHinsyu"      , accountIkubyoStatusData.ssnHinsyu);
      }
      pJson.put("ssnSeiikuF"        , accountIkubyoStatusData.ssnSeiikuF);
      pJson.put("ssnSeiikuT"        , accountIkubyoStatusData.ssnSeiikuT);

      //----- 生産者情報 -----
      if (farmData != null) { //生産者情報が存在する
        pJson.put("farmid"      , farmData.farmId);
        pJson.put("farmname"    , farmData.farmName);
      }
      else {
        pJson.put("farmid"            , JSON_NONE);
        pJson.put("farmname"          , JSON_NONE);
      }

      //----- 生産者グループ -----
      if (farmGroupData != null) { //生産者情報が存在する
        pJson.put("farmgroupid"      , farmGroupData.farmGroupId);
        pJson.put("farmgroupname"    , farmGroupData.farmGroupName);
      }
      else {
        pJson.put("farmgroupid"       , JSON_NONE);
        pJson.put("farmgroupname"     , JSON_NONE);
      }

      //----- 生産者ステータス -----
      if (farmStatusData != null) { //生産者ステータス情報が存在する
        pJson.put("contractplan"      , farmStatusData.contractPlan);
        pJson.put("ikubyoFunction"    , farmStatusData.ikubyoFunction);
        pJson.put("kukakuSelectMethod", farmStatusData.kukakuSelectMethod);
      }
      else {
        pJson.put("contractplan"      , JSON_NONE);
        pJson.put("ikubyoFunction"    , JSON_NONE);
        pJson.put("kukakuSelectMethod", JSON_NONE);
      }
    }
    else {
      //----- アカウント情報 -----
      pJson.put("id"                , JSON_NONE);
      pJson.put("name"              , JSON_NONE);
      pJson.put("farmid"            , JSON_NONE);
      pJson.put("farmname"          , JSON_NONE);
      pJson.put("farmgroupid"       , JSON_NONE);
      pJson.put("farmgroupname"     , JSON_NONE);
      result  = GET_ERROR;
    }

    return result;

  }

  /**
   * 生産者ID、アカウントIDよりメニュー権限一覧を取得し、JSONに格納する
   * @param pListJson
   * @return
   */
  public static int getMenuRoleSelJson(double pFarmId, String accountId, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    Account account = Account.find.where().eq("account_id", accountId).findUnique();
    for (int idx=0; idx < 32; idx++) {

        ObjectNode jd = Json.newObject();

        if(AgryeelConst.AccountSetting.menuRoleNme[idx] == ""){
        	break;
        }
        jd.put("id"   , idx);
        jd.put("name" , AgryeelConst.AccountSetting.menuRoleNme[idx]);
        if((account.menuRole & AgryeelConst.AccountSetting.menuRolePtn[idx]) == 0){
           jd.put("flag" , 0);
        }else{
    	   jd.put("flag" , 1);
        }
        pListJson.put(String.valueOf(idx), jd);
    }

    return result;
  }

  /**
   * 未確認システムメッセージを取得し、JSONに格納します
   * @param pListJson
   * @return
   */
  public int getSystemMessageJson(ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    if (accountData != null) { //アカウント情報が存在する

      List<SystemMessage> messages = MessageOfAccount.getMessageByAccountId(accountData.accountId);

      if (messages.size() > 0) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        for (SystemMessage message : messages) {
          ObjectNode jd = Json.newObject();

          jd.put("id" , sdf.format(message.updateTime));
          jd.put("msg", sdf.format(message.message));

        }

      }
      else {
        result  = GET_ERROR;
      }

    }
    else {
      result  = GET_ERROR;
    }

    return result;

  }

  /**
   * チェック済みのシステムメッセージを削除します
   * @param pKey
   * @return
   */
  public int checkSystemMessage(String pKey) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    if (accountData != null) { //アカウント情報が存在する

      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      java.sql.Timestamp key;
      int update = 0;
      try {
        key = new Timestamp(sdf.parse(pKey).getTime());
        update = MessageOfAccount.checkMessage(accountData.accountId, key);
      } catch (ParseException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
        update = 1;
      }

      if (update == 1) { //UPDATEエラーの場合
        result  = GET_ERROR;
      }
    }
    else {
      result  = GET_ERROR;
    }

    return result;

  }

	/**
	 * ログイン認証
	 * @param accountId アカウントＩＤ
	 * @param password  パスワード
	 * @return 認証結果
	 */
	public int Login(String accountId, String password) {

		/* 戻り値 */
		int	result	=	LOGIN_SUCCESS;

	    /* アカウント情報よりデータを取得する */
	    List<Account> accountInfo = GetAccountInfo(accountId);

        if (accountInfo.size() == 0) { 																						//アカウント情報が存在しない場合
    		result	=	LOGIN_NOTFOUND;																						//アカウント未存在
        }
        else {
            Account account = accountInfo.get(0);																			//アカウント情報を取り出す
            if (!StringU.setNullTrim(account.password).equals(password)) {													//パスワードが不一致
        		result				=	LOGIN_PASSWORDMISSMATCH;															//パスワード不一致
            }
            else {																											//アカウントログイン成功
        		result				=	LOGIN_SUCCESS;																		//ログイン認証成功
        		this.accountData	= account;																				//アカウント情報を設定
            }
        }

        return result;

	}

	/**
	 * アカウント存在チェック
	 * @param accountId アカウントＩＤ
	 * @return アカウント存在チェック結果
	 */
	public int ExistsAccountCheck(String accountId) {

		/* 戻り値 */
		int	result	=	EXISTS_ACCOUNTCHECK_SUCCESS;

	    /* アカウント情報よりデータを取得する */
	    List<Account> accountInfo = GetAccountInfo(accountId);

        if (accountInfo.size() > 0) { 																						//アカウント情報が存在する場合

			result	=	EXISTS_ACCOUNTCHECK_NG;																				//アカウント存在チェックＮＧ

        }

        return result;
	}

	/**
	 * アカウント作成
	 * @param accountId アカウントＩＤ
	 * @param password パスワード
	 * @param acountName 氏名
	 * @param farmId 農場ＩＤ
	 * @return アカウント作成結果
	 */
	public int MakeAccount(String accountId, String password, String acountName, double farmId) {

		/* 戻り値 */
		int	result	=	MAKE_ACCOUNT_SUCCESS;

		List<Account> accounts = Account.getAccountOfFarm(farmId);	//対象農業の全アカウント取得

		/* アカウントモデルの作成 */
		Account account = new Account();

		account.accountId 			= accountId;						//アカウントＩＤ
		account.password			= password;							//パスワード
		account.acountName			= acountName;						//氏名
		account.farmId				= farmId;							//農場ＩＤ
		if (accounts.size() > 0) {
			account.managerRole 	= 0;								//管理者権限
		} else {
			account.managerRole 	= 2;								//管理者権限
		}

		try
		{
			account.save();												//新規アカウントの作成
			AccountStatus as = new AccountStatus();
			as.accountId = account.accountId;
			//生産者のワークチェーンを取得する
			List<WorkChain> wcs = WorkChain.getWorkChainOfFarm(farmId);
			for (WorkChain wc : wcs) {
			  as.selectChainId = wc.workChainId;
			  break;
			}
			as.save();

			AccountIkubyoStatus ais = new AccountIkubyoStatus();
			ais.accountId = account.accountId;
			ais.save();
		}
		catch (Exception ex) {

			ex.printStackTrace();
			result	=	MAKE_ACCOUNT_NG;

		}

        return result;
	}

	/**
	 * 圃場グループ検索条件を生成する
	 * @param accountId アカウントＩＤ
	 * @return 対象圃場作成結果
	 */
	public int MakeFieldGroup(String accountId) {

		/* 戻り値 */
		int	result	=	MAKE_FIELDGROUP_SUCCESS;

		if (GetAccountData(accountId) == GET_ERROR) {

			result	=	MAKE_FIELDGROUP_NG;

		}
		else {

      List<FieldGroup> fields = FieldGroup.getFieldGroupOfFarm(this.accountData.farmId);

			try
			{
			  String sfg = "";
				for (FieldGroup field : fields) {											                   //取得した圃場件数分処理を行う
				  if (!"".equals(sfg)) {
				    sfg += ",";
				  }
				  sfg += field.fieldGroupId;
				}
				this.accountStatusData.selectFieldGroupId = sfg;
				this.accountStatusData.update();
			}
			catch (Exception ex) {

				ex.printStackTrace();
				result	=	MAKE_FIELDGROUP_NG;

			}

		}

    return result;

	}

	/**
	 * GoogleIDよりアカウントを取得する
	 * @param googleId グーグルＩＤ
	 * @return 取得結果
	 */
	public int GetGoogleAccountData(String googleId) {

		/** 戻り値 */
		int	result	=	GET_SUCCESS;

		//アカウントIDをキーにアカウント情報を取得する
		Account account = Account.find.where().eq("google_id", googleId).findUnique();

        if (account != null) { //アカウントが取得できた場合
        	accountData = account;
        }
        else {
    		result	=	LOGIN_NOTFOUND;
        }

        return result;

	}
  public int GetHashAccountData(String hashValue) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    //アカウントIDをキーにアカウント情報を取得する
    Account account = Account.find.where().eq("hash_value", hashValue).findUnique();

    if (account != null) { //アカウントが取得できた場合
      accountData = account;
    }
    else {
      result  = LOGIN_NOTFOUND;
    }

    return result;

  }

	/**
	 * ログインカウント回数を更新する
	 */
	public void LoginCountUP() {

		this.accountData.loginCount++;
		this.accountData.update();

	}
	/**
	 * 入力カウント回数を更新する
	 */
	public void InputCountUP() {

		this.accountData.inputCount++;
		this.accountData.update();

	}

	/**
	 * クリップモードを切り替える
	 * @return 切替後クリップモード
	 */
	public int ChangeClipMode() {

		if (this.accountData.clipMode == 0) {
			this.accountData.clipMode = 1;
		}
		else {
			this.accountData.clipMode = 0;
		}

		this.accountData.update();

		return this.accountData.clipMode;

	}
	/**
	 * クリップモードを更新する
	 * @return 更新後クリップモード
	 */
	public int UpdateClipMode(int clipMode) {

		this.accountData.clipMode = clipMode;
		this.accountData.update();

		return this.accountData.clipMode;

	}

	/**
	 * 圃場IDから作業中情報をJSONに格納する
	 * @param pFieldId
	 * @param pListJson
	 * @return
	 */
	public int getNowWorkingByField(double pFieldId, ObjectNode pListJson) {

    int result  = GET_SUCCESS;

    List<Account> accounts = Account.getAccountOfField(pFieldId);

    if (accounts.size() > 0) { //該当データが存在する場合
      SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
      for (Account account : accounts) {
        ObjectNode jd = Json.newObject();

        jd.put("id"     , account.accountId);
        jd.put("name"   , account.acountName);
        jd.put("start"  , sdf.format(account.workStartTime));
        Work work = account.getWork();
        if (work != null) {
          jd.put("workid"     , work.workId);
          jd.put("workname"   , work.workName);
          jd.put("workcolor"  , work.workColor);
        }
        else {
          jd.put("workid"     , JSON_NONE);
          jd.put("workname"   , JSON_NONE);
          jd.put("workcolor"  , JSON_NONE);
        }
        pListJson.put(account.accountId, jd);
      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
	}
	/**
	 * 圃場IDから作業中情報をJSONに格納する(Array)
	 * @param pFieldId
	 * @param pListJson
	 * @return
	 */
	public int getNowWorkingByFieldArray(double pFieldId, ArrayNode pListJson) {

    int result  = GET_SUCCESS;

    List<Account> accounts = Account.getAccountOfField(pFieldId);

    if (accounts.size() > 0) { //該当データが存在する場合
      SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
      for (Account account : accounts) {
        ObjectNode jd = Json.newObject();

        jd.put("id"     , account.accountId);
        jd.put("name"   , account.acountName);
        jd.put("start"  , sdf.format(account.workStartTime));
        Work work = account.getWork();
        if (work != null) {
          jd.put("workid"     , work.workId);
          jd.put("workname"   , work.workName);
          jd.put("workcolor"  , work.workColor);
        }
        else {
          jd.put("workid"     , JSON_NONE);
          jd.put("workname"   , JSON_NONE);
          jd.put("workcolor"  , JSON_NONE);
        }
        pListJson.add(jd);
      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
	}
	/**
	 * 渡された圃場グループがアカウント選択中圃場グループに存在するか
	 * @param pFieldGroupId
	 * @return
	 */
  public boolean checkFieldGroupSelect(double pFieldGroupId) {

    /** 戻り値 */
    boolean result  = false;

    if (accountStatusData != null) {
      String[] fgis = this.accountStatusData.selectFieldGroupId.split(",");
      for (String fgi : fgis) {
        try{
          if (Double.parseDouble(fgi) == pFieldGroupId) {
            result  = true;
            break;
          }
        }
        catch (NumberFormatException ne) {

        }
      }
    }

    return result;

  }

  public static int getAccountOfFarmJson(double pFarmId, ObjectNode pListJson) {

    int result  = GET_SUCCESS;

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

        pListJson.put(String.valueOf(account.accountId), jd);
      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
  }
  public static int getAccountOfFarmAllJson(double pFarmId, ObjectNode pListJson) {

    int result  = GET_SUCCESS;

    getAccountOfFarmJson(pFarmId, pListJson);
    ObjectNode jd = Json.newObject();

    jd.put("id"   , AgryeelConst.SpecialAccount.ALLACOUNT);
    jd.put("name" , "担当者未選択");
    jd.put("flag" , 0);

    pListJson.put(AgryeelConst.SpecialAccount.ALLACOUNT, jd);

    return result;
  }
}
