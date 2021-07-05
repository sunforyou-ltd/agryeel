package controllers;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Account;
import models.Common;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.MessageOfAccount;
import models.MotochoBase;
import models.Nisugata;
import models.PlanLine;
import models.Sizai;
import models.SystemManage;
import models.SystemMessage;
import models.TimeLine;
import models.Work;
import models.WorkDiary;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;
import util.ListrU;
import util.StringU;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import compornent.CommonComprtnent;
import compornent.CompartmentStatusCompornent;
import compornent.CropComprtnent;
import compornent.DachakuCompornent;
import compornent.FieldComprtnent;
import compornent.HashuCompornent;
import compornent.KikiComprtnent;
import compornent.LandlordComprtnent;
import compornent.MotochoCompornent;
import compornent.MultiCompornent;
import compornent.NaehashuCompornent;
import compornent.NouhiComprtnent;
import compornent.ShukakuCompornent;
import compornent.SizaiComprtnent;
import compornent.UserComprtnent;
import compornent.WorkChainCompornent;

import consts.AgryeelConst;

/**
 * テストコントローラー
 * @author kimura
 *
 */
public class CommonController extends Controller {

  /**
   * メニュー権限一覧を取得する
   * @return
   */
  public static Result getMenuRoleSel(double farmId, String accountId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = UserComprtnent.getMenuRoleSelJson(farmId, accountId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産物一覧を取得する
   * @return
   */
  public static Result getCrop() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CropComprtnent cp = new CropComprtnent();
      int result = cp.getCropJson(listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産物一覧を取得する
   * @return
   */
  public static Result getCropOfFarm(double farmId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = CropComprtnent.getCropOfFarmJson(farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産物一覧を取得する（選択なし追加版）
   * @return
   */
  public static Result getCropOfFarmAll(double farmId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = CropComprtnent.getCropOfFarmAllJson(farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産物一覧を取得する
   * @return
   */
  public static Result getCropOfFarmSel(double farmId, double cropGroupId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = CropComprtnent.getCropOfFarmSelJson(farmId, cropGroupId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 品種一覧を取得する
   * @return
   */
  public static Result getHinsyu() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = CropComprtnent.getHinsyuOfFarmJson(accountComprtnent.accountData.farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 品種一覧を取得する
   * @return
   */
  public static Result getHinsyuOfFarmSel(double farmId, double cropId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = CropComprtnent.getHinsyuOfFarmSelJson(farmId, cropId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 地主一覧を取得する
   * @return
   */
  public static Result getLandlordOfFarmSel(double farmId, double landlordId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = LandlordComprtnent.getLandlordOfFarmSelJson(farmId, landlordId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 圃場グループ一覧を取得する
   * @return
   */
  @SuppressWarnings("unused")
  public static Result getFieldGroup() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = FieldComprtnent.getFieldGroupWhere(accountComprtnent.accountData.accountId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 圃場一覧を取得する
   * @return
   */
  public static Result getFieldOfFarmSel(double farmId, double fieldGroupId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = FieldComprtnent.getFieldOfFarmSelJson(farmId, fieldGroupId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 区画一覧を取得する（自圃場所属＋未所属）
   * @return
   */
  public static Result getCompartmentOfFieldSel(double farmId, double fieldId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = FieldComprtnent.getCompartmentOfFieldSel(farmId, fieldId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * アタッチメント一覧を取得する
   * @return
   */
  public static Result getAttachmentOfFarmSel(double farmId, double kikiId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = KikiComprtnent.getAttachmentOfFarmSelJson(farmId, kikiId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 1);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * アカウント情報を取得する
   * @return
   */
  @SuppressWarnings("unused")
  public static Result getAccountInfo() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = accountComprtnent.getAccountJson(resultJson);

      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産者IDから作業一覧を取得する
   * @return
   */
  public static Result getWorkOfFarm() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = WorkChainCompornent.getWorkOfFarmJson(accountComprtnent.accountData.farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産者IDから作業一覧を取得する
   * @return
   */
  public static Result getUniqueWorkOfFarmJson() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      ArrayNode workList = WorkChainCompornent.getUniqueWorkOfFarmJson(accountComprtnent.accountData.farmId);

      resultJson.put("datalist" , workList);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産者IDから担当者一覧を取得する
   * @return
   */
  public static Result getAccountOfFarm() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = UserComprtnent.getAccountOfFarmJson(accountComprtnent.accountData.farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産者IDから担当者一覧を取得する
   * @return
   */
  public static Result getAccountOfFarmAll() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = UserComprtnent.getAccountOfFarmAllJson(accountComprtnent.accountData.farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 生産者IDから区画情報一覧を取得する
   * @return
   */
  public static Result getCompartmentOfFarm() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = CompartmentStatusCompornent.getCompartmentOfFarmJson(accountComprtnent.accountData.farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   *
   * @return
   */
  public static Result getWorkChainOfFarm() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    //アカウント情報の取得
    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    int result = WorkChainCompornent.getWorkChainOfFarmJson(accountComprtnent.accountData.farmId, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  /**
   * 散布方法を取得する
   * @return
   */
  public static Result getSanpu() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.SANPUMETHOD, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  public static Result getKansui() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    CommonComprtnent cc = new CommonComprtnent();

    int result = cc.getCommonJson(Common.ConstClass.KANSUI, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  public static Result getNisugata() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    int result = ShukakuCompornent.getNisugataJson(accountComprtnent.accountData.farmId, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  public static Result getNisugataInfo(double nisugataId) {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();

    Nisugata n = Nisugata.getNisugataInfo(nisugataId);
    if (n != null) {
      resultJson.put("nisugataId"   , n.nisugataId);
      resultJson.put("nisugataName" , n.nisugataName);
      resultJson.put("nisugataKind" , n.nisugataKind);
      resultJson.put("capacity"     , n.capacity);
      resultJson.put("kingaku"      , n.kingaku);
      resultJson.put("result"       , AgryeelConst.Result.SUCCESS);
    }
    else {
      resultJson.put("result"       , AgryeelConst.Result.NOTFOUND);
    }

    return ok(resultJson);
  }
  public static Result getShitu() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    int result = ShukakuCompornent.getShituJson(accountComprtnent.accountData.farmId, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  public static Result getSize() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    int result = ShukakuCompornent.getSizeJson(accountComprtnent.accountData.farmId, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  public static Result getTimeLineWorking() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    CommonComprtnent cc = new CommonComprtnent();

    int result = cc.getCommonJson(Common.ConstClass.DISPLAYWORKING, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  public static Result getSizai() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    //CommonComprtnent cc = new CommonComprtnent();

    //int result = cc.getCommonJson(Common.ConstClass.ITOSIZAI, listJson);
    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    int result = SizaiComprtnent.getSizaiJson(accountComprtnent.accountData.farmId, Sizai.SizaiKindClass.SIZAI, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  public static Result getMulti() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    //CommonComprtnent cc = new CommonComprtnent();

    //int result = cc.getCommonJson(Common.ConstClass.ITOMULTI, listJson);
    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    int result = SizaiComprtnent.getSizaiJson(accountComprtnent.accountData.farmId, Sizai.SizaiKindClass.MULTI, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  public static Result getUseBaido() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    //CommonComprtnent cc = new CommonComprtnent();

    //int result = cc.getCommonJson(Common.ConstClass.ITOBAIDO, listJson);
    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    int result = SizaiComprtnent.getSizaiJson(accountComprtnent.accountData.farmId, Sizai.SizaiKindClass.BAIDO, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  /**
   * 生産物一覧を取得する
   * @return
   */
  public static Result getMotochoHistry(double kukakuId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = MotochoCompornent.getMotochoHistry(kukakuId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 機器情報を取得する
   * @return
   */
  public static Result getKikiOfWorkJson(double workId, double kukakuId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      KikiComprtnent.getKikiOfWorkJson(accountComprtnent.accountData.farmId, workId, kukakuId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  public static Result getAttachmentOfKikiJson(double kikiId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = KikiComprtnent.getAttachmentOfKikiJson(kikiId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  public static Result getNouhiOfWorkJson(double workId, double kukakuId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = NouhiComprtnent.getNouhiOfFarmJson(workId, accountComprtnent.accountData.farmId, kukakuId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  public static Result getBeltoOfFarmJson() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      HashuCompornent.getBeltoOfFarmJson(accountComprtnent.accountData.farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 指定した生産物より品種を取得します
   * @param cropId
   * @return
   */
  public static Result getHinsyuOfCropToCropJson(double cropId) {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    //アカウント情報の取得
    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    HashuCompornent.getHinsyuOfCropToCropJson(accountComprtnent.accountData.farmId, cropId, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  /**
   * 区画の品目に紐づく品種の一覧を取得する(作業記録時に使用)
   * @param kukakuId
   * @return
   */
  public static Result getHinsyuOfCropJson(double kukakuId) {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    //アカウント情報の取得
    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    HashuCompornent.getHinsyuOfCropJson(accountComprtnent.accountData.farmId, kukakuId, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  /**
   * 生産者単位の品種の一覧を取得する（タイムラインなど）
   * @return
   */
  public static Result getHinsyuOfFarmJson() {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();
    ObjectNode listJson = Json.newObject();

    //アカウント情報の取得
    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    HashuCompornent.getHinsyuOfFarmJson(accountComprtnent.accountData.farmId, listJson);

    resultJson.put("datalist" , listJson);
    resultJson.put("flag"     , 0);
    resultJson.put("result"   , "SUCCESS");

    return ok(resultJson);
  }
  /**
   * 状況照会を取得する
   * @return
   */
  public static Result getDisplayStatus() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.DISPLAYSTAUS, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業対象表示を取得する
   * @return
   */
  public static Result getWorkTargetDisplay() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKTARGETDISPLAY, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業記録後を取得する
   * @return
   */
  public static Result getWorkCommitAfter() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKCOMMITAFTER, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * ワークチェーン表示方法を取得する
   * @return
   */
  public static Result getDisplayChain() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.DISPLAYCHAIN, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 荷姿履歴値参照を取得する
   * @return
   */
  public static Result getNisugataRireki() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.NISUGATARIREKI, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 区画状況照会SKIPを取得する
   * @return
   */
  public static Result getCompartmentStatusSkip() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.COMPARTMENTSTATUSSKIP, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業日付自動設定を取得する
   * @return
   */
  public static Result getWorkDateAutoSet() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKDATESUTOSET, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業開始確認を取得する
   * @return
   */
  public static Result getWorkStartPrompt() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKSTARTPROMPT, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業切替指示を取得する
   * @return
   */
  public static Result getWorkChangeDisplay() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKCHANGEDISPLAY, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 付近区画距離を取得する
   * @return
   */
  public static Result getRadius() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.RADIUS, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業指示初期担当者を取得する
   * @return
   */
  public static Result getWorkPlanInitId() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKPLANINITID, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 面積単位を取得する
   * @return
   */
  public static Result getAreaUnit() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.AREAUNIT, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 期初を取得する
   * @return
   */
  public static Result getKisyo() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.KISYO, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 農肥チェックを取得する
   * @return
   */
  public static Result getNouhiCheck() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.NOUHICHECK, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業指示自動移動を取得する
   * @return
   */
  public static Result getWorkPlanAutoMove() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKPLANAUTOMOVE, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 契約プランを取得する
   * @return
   */
  public static Result getContractPlan() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.CONTRACTPLAN, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * データ使用許可を取得する
   * @return
   */
  public static Result getDataUsePermission() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.DATAUSEPERMISSION, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * メッセージ種別を取得する
   * @return
   */
  public static Result getMessageKind() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.MESSAGEKIND, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 履歴参照を取得する
   * @return
   */
  public static Result getHistoryReference() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.HISTORYREFERENCE, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業指示自動生成を取得する
   * @return
   */
  public static Result getWorkPlanAutoCreate() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKPLANAUTOCREATE, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 作業記録注釈を取得する
   * @return
   */
  public static Result getWorkDiaryDiscription() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.WORKDIARYDISCRIPTION, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }

  /**
   * 収穫入力数を取得する
   * @return
   */
  public static Result getSyukauInputCount() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      CommonComprtnent cc = new CommonComprtnent();

      int result = cc.getCommonJson(Common.ConstClass.SYUKAKUINPUTCOUNT, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }

  /**
   * 該当区画のワークチェーン作業一覧を取得する
   * @return
   */
  public static Result getWorkOfKukaku(String kukakuId) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      if (kukakuId != null && !"".equals(kukakuId)) {
        String[] kukakus = kukakuId.split(",");

        for (String id : kukakus) {

          double kukaku    = Double.parseDouble(id);
          CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(kukaku);
          CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();

          WorkChainCompornent.getWorkOfKukakuJson(accountComprtnent.accountData.farmId, cws.workChainId, listJson);
        }

      }


      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 区画検索条件をアカウント状況に反映する
   * @return
   */
  public static Result kukakusearchCommit() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      /* JSONデータを取得 */
      JsonNode input = request().body().asJson();

      accountComprtnent.accountStatusData.sskKukakuName       = input.get("name").asText();
      accountComprtnent.accountStatusData.selectFieldGroupId  = input.get("fg").asText();
      accountComprtnent.accountStatusData.sskMultiKukaku      = input.get("mk").asText();
      accountComprtnent.accountStatusData.sskHinsyu           = input.get("sh").asText();
      accountComprtnent.accountStatusData.sskSeiikuF          = input.get("from").asInt();
      accountComprtnent.accountStatusData.sskSeiikuT          = input.get("to").asInt();
      accountComprtnent.accountStatusData.displayStatus       = input.get("display").asInt();
      accountComprtnent.accountStatusData.update();

      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * タイムライン検索条件をアカウント状況に反映する
   * @return
   */
  public static Result timelineStatusCommit() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      /* JSONデータを取得 */
      JsonNode input = request().body().asJson();

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

        Logger.info("[TEST] KEY={} FROM={} TO={}", session(AgryeelConst.SessionKey.ACCOUNTID), from, to);

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
  /**
   * 初期検索タイムラインデータを取得する
   * @return
   */
  public static Result getTimeLineInitData() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      if (getAccount == UserComprtnent.GET_ERROR) {
        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
        resultJson.put("result"   , "ERROR");
        return ok(resultJson);
      }

      listJson   = Json.newObject();  //リスト形式JSONを初期化する
      /* アカウント情報からタイムライン検索条件を取得する */
      UserComprtnent uc = new UserComprtnent();
      uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      Calendar cStart = Calendar.getInstance();
      Calendar cEnd = Calendar.getInstance();

//    cStart.add(Calendar.DAY_OF_MONTH, -2);
//    cStart.add(Calendar.MONTH, -1);
      cStart.add(Calendar.DAY_OF_MONTH, -7);
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
            timeLineJson.put("message"      , "【現在作業中】");                          //メッセージ
          }
        }
        else {
          timeLineJson.put("message"      , "【現在作業中】");                             //メッセージ
        }
        timeLineJson.put("timeLineColor", "aaaaaa");                                  //タイムラインカラー
        timeLineJson.put("workDiaryId"  , timelineId);                                //作業記録ID
        timeLineJson.put("workName"     , wk.workName);                               //作業名
        //単一区画表記に変更する
//        if (working.workPlanId != 0) {
//          List<models.WorkPlan> workplans = models.WorkPlan.find.where().eq("work_id", working.workId).eq("account_id", working.accountId).orderBy("work_plan_id").findList();
//          String sKukaku = "";
//          for (models.WorkPlan workplan: workplans) {
//            Compartment cd = FieldComprtnent.getCompartment(workplan.kukakuId);
//            if (cd != null) {
//              if (!"".equals(sKukaku)) {
//                sKukaku += ",";
//              }
//              sKukaku += cd.kukakuName;
//            }
//          }
//          timeLineJson.put("kukakuName", sKukaku);
//        }
//        else {
//          timeLineJson.put("kukakuName"   , cpt.kukakuName);                            //区画名
//        }
        timeLineJson.put("kukakuName"   , cpt.kukakuName);                            //区画名
        timeLineJson.put("accountName"  , working.acountName);                        //アカウント名
        timeLineJson.put("workId"       , wk.workId);                                 //作業ＩＤ
        timeLineJson.put("worktime"     , diff);                                      //作業時間
        timeLineJson.put("workDiaryId"  , timelineId);                                //ＩＤ
        timeLineJson.put("fieldGroupId" , cpt.getFieldGroupInfo().fieldGroupId);      //圃場グループID

        listJson.put(Double.toString(timelineId), timeLineJson);

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
        timeLineJson.put("kukakuId"     , timeLineData.kukakuId);                     //区画ＩＤ
        Compartment cpt = Compartment.getCompartmentInfo(timeLineData.kukakuId);
        timeLineJson.put("fieldId"      , cpt.fieldId);                               //圃場ID
        timeLineJson.put("fieldGroupId" , cpt.getFieldGroupInfo().fieldGroupId);      //圃場グループID

        listJson.put(Double.toString(timeLineData.timeLineId), timeLineJson);

      }

      resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * タイムライン検索条件をもとにタイムライン情報を取得する
   * @return
   */
  public static Result getTimeLineData() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      listJson   = Json.newObject();  //リスト形式JSONを初期化する
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
              timeLineJson.put("message"      , "【現在作業中】");                          //メッセージ
            }
          }
          else {
            timeLineJson.put("message"      , "【現在作業中】");                             //メッセージ
          }
          timeLineJson.put("timeLineColor", "aaaaaa");                                  //タイムラインカラー
          timeLineJson.put("workDiaryId"  , timelineId);                                //作業記録ID
          timeLineJson.put("workName"     , wk.workName);                               //作業名
          timeLineJson.put("kukakuName"   , cpt.kukakuName);                            //区画名
          timeLineJson.put("fieldGroupId" , cpt.getFieldGroupInfo().fieldGroupId);      //圃場グループID
          timeLineJson.put("accountName"  , working.acountName);                        //アカウント名
          timeLineJson.put("workId"       , wk.workId);                                 //作業ＩＤ
          timeLineJson.put("worktime"     , diff);                                      //作業時間
          timeLineJson.put("workDiaryId"  , timelineId);                                //ＩＤ

          listJson.put(Double.toString(timelineId), timeLineJson);

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
          timeLineJson.put("kukakuId"     , timeLineData.kukakuId);                     //区画ＩＤ
          Compartment cpt = Compartment.getCompartmentInfo(timeLineData.kukakuId);
          timeLineJson.put("fieldId"      , cpt.fieldId);                               //圃場ID
          timeLineJson.put("fieldGroupId" , cpt.getFieldGroupInfo().fieldGroupId);      //圃場グループID

          listJson.put(Double.toString(timeLineData.timeLineId), timeLineJson);

        }

      }

      resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 担当者作業別年間作業時間の取得
   * @return
   */
  public static Result getWorkTimeOfYear(String accountId, int year) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //----- 年間範囲の作成 -----
      Calendar cal = Calendar.getInstance();
      //FROM
      cal.set(year, 0, 1, 0, 0, 0);
      java.sql.Date start = new Date(cal.getTimeInMillis());
      //TO
      cal.set(year, 11, 31, 23, 59, 59);
      java.sql.Date end = new Date(cal.getTimeInMillis());

      //----- タイムラインの取得 -----
      List<TimeLine> timeLine;
      if (accountId.equals(AgryeelConst.SpecialAccount.ALLACOUNT)) {
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        timeLine = TimeLine.find.where().eq("farm_id", accountComprtnent.accountData.farmId).between("work_date", start, end).orderBy("work_id").findList();
      }
      else {
        timeLine = TimeLine.find.where().eq("account_id", accountId).between("work_date", start, end).orderBy("work_id").findList();
      }

      DecimalFormat df = new DecimalFormat("0000.0");

      double oldId    = 0;
      double workTime = 0;
      double yearTime = 0;

      for (TimeLine timeLineData : timeLine) {                                        //作業情報をJSONデータに格納する

        WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

        if (workd == null) { continue; }

        if (oldId != workd.workId) {
          if (oldId != 0) {
            Work work = Work.getWork(oldId);
            if (work != null) {
              ObjectNode workJson         = Json.newObject();
              workJson.put("workId"       , work.workId);
              workJson.put("workName"     , work.workName);
              workJson.put("workColor"    , work.workColor);
              workJson.put("workTime"     , df.format(workTime / 60));
              listJson.put(Double.toString(work.workId), workJson);
              workTime = 0;
            }
          }
        }
        oldId     = workd.workId;
        workTime += workd.workTime;
        yearTime += workd.workTime;
      }

      if (oldId != 0) {
        Work work = Work.getWork(oldId);
        if (work != null) {
          ObjectNode workJson         = Json.newObject();
          workJson.put("workId"       , work.workId);
          workJson.put("workName"     , work.workName);
          workJson.put("workColor"    , work.workColor);
          workJson.put("workTime"     , df.format(workTime / 60));
          listJson.put(Double.toString(work.workId), workJson);
        }
      }

      resultJson.put("datalist"   , listJson);
      resultJson.put("totaltime"  , df.format(yearTime / 60));
      resultJson.put("result"     , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * 担当者作業別月間作業時間の取得
   * @return
   */
  public static Result getWorkTimeOfMonth(String accountId, int year, int month) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //----- 年間範囲の作成 -----
      Calendar cal = Calendar.getInstance();
      //FROM
      cal.set(year, (month - 1), 1, 0, 0, 0);
      java.sql.Date start = new Date(cal.getTimeInMillis());
      //TO
      cal.set(year, (month - 1), cal.getActualMaximum(Calendar.DATE), 23, 59, 59);
      java.sql.Date end = new Date(cal.getTimeInMillis());

      DecimalFormat df = new DecimalFormat("0000.0");

      //----- タイムラインの取得 -----
      List<TimeLine> timeLine;
      if (accountId.equals(AgryeelConst.SpecialAccount.ALLACOUNT)) {
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        timeLine = TimeLine.find.where().eq("farm_id", accountComprtnent.accountData.farmId).between("work_date", start, end).orderBy("work_id").findList();
      }
      else {
        timeLine = TimeLine.find.where().eq("account_id", accountId).between("work_date", start, end).orderBy("work_id").findList();
      }

      double oldId    = 0;
      double workTime = 0;
      double yearTime = 0;

      for (TimeLine timeLineData : timeLine) {                                        //作業情報をJSONデータに格納する

        WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

        if (workd == null) { continue; }

        if (oldId != workd.workId) {
          if (oldId != 0) {
            Work work = Work.getWork(oldId);
            if (work != null) {
              ObjectNode workJson         = Json.newObject();
              workJson.put("workId"       , work.workId);
              workJson.put("workName"     , work.workName);
              workJson.put("workColor"    , work.workColor);
              workJson.put("workTime"     , df.format(workTime / 60));
              listJson.put(Double.toString(work.workId), workJson);
              workTime = 0;
            }
          }
        }
        oldId     = workd.workId;
        workTime += workd.workTime;
        yearTime += workd.workTime;
      }

      if (oldId != 0) {
        Work work = Work.getWork(oldId);
        if (work != null) {
          ObjectNode workJson         = Json.newObject();
          workJson.put("workId"       , work.workId);
          workJson.put("workName"     , work.workName);
          workJson.put("workColor"    , work.workColor);
          workJson.put("workTime"     , df.format(workTime / 60));
          listJson.put(Double.toString(work.workId), workJson);
        }
      }

      resultJson.put("datalist"   , listJson);
      resultJson.put("totaltime"  , df.format(yearTime / 60));
      resultJson.put("result"     , "SUCCESS");

      return ok(resultJson);
  }
  /**
   * ワンメッセージを取得する
   * @return 検索条件JSON
   */
  public static Result getOneMessage() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();
      ObjectNode  listJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonApi = mapper.createArrayNode();
      boolean api = false;
      if (session(AgryeelConst.SessionKey.API) != null) {
      	api = true;
      }

      /* アカウント情報を取得する */
      String accountId = session(AgryeelConst.SessionKey.ACCOUNTID);
      UserComprtnent accountComprtnent = new UserComprtnent();
      accountComprtnent.GetAccountData(accountId);

      List<SystemMessage> sms = MessageOfAccount.getOneMessageByAccountId(accountId);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat tmf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      Calendar cal = Calendar.getInstance();
      java.sql.Date system = new java.sql.Date(cal.getTimeInMillis());

      for(SystemMessage sm : sms) {
        ObjectNode  mssageJson = Json.newObject();
        mssageJson.put("key", tmf.format(sm.updateTime));
        mssageJson.put("message", sm.message);

        long diff = DateU.GetDiffMin(sm.updateTime, system);

        if (86400 <= diff) {
          //日に換算する
          diff = diff / 86400;
          mssageJson.put("keika", diff + "日前");
        }
        else if ((3600 <= diff) && (diff < 86400)) {
          //時間に換算する
          diff = diff / 3600;
          mssageJson.put("keika", diff + "時間前");
        }
        else if ((60 <= diff) && (diff < 3600)) {
          //分に換算する
          diff = diff / 60;
          mssageJson.put("keika", diff + "分前");
        }
        else {
          //秒に換算する
          mssageJson.put("keika", diff + "秒前");
        }
        listJson.put(tmf.format(sm.updateTime), mssageJson);
        listJsonApi.add(mssageJson);

        //Jsonに格納したメッセージは瞬時に削除する
        MessageOfAccount.checkMessage(accountId, sm.updateTime);

      }
      if(api){
        resultJson.put("datalist", listJsonApi);
      }else{
        resultJson.put("datalist", listJson);
      }

      return ok(resultJson);
  }
  /**
   * システム管理情報を取得する
   * @return 検索条件JSON
   */
  public static Result getSystemManage(int systemKind) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();

      SystemManage sm = SystemManage.getSystemManage(systemKind);

      if (sm != null) {
        resultJson.put("result", 0);
        resultJson.put("systemKind", sm.systemKind);
        resultJson.put("majorVersion", sm.majorVersion);
        resultJson.put("minorVersion", sm.minorVersion);
        resultJson.put("patchVersion", sm.patchVersion);
        resultJson.put("vuuid", sm.vuuid);
        resultJson.put("compUpdate", sm.compUpdate);
      }
      else {
        resultJson.put("result", 1);
        resultJson.put("systemKind", 0);
        resultJson.put("majorVersion", 0);
        resultJson.put("minorVersion", 0);
        resultJson.put("patchVersion", 0);
        resultJson.put("vuuid", "");
        resultJson.put("compUpdate", 0);
      }

      return ok(resultJson);
  }
  /**
   * システム管理情報を設定する
   * @return 検索条件JSON
   */
  public static Result commitSystemManage(int systemKind, int majorVersion, int minorVersion, int patchVersion, int compUpdate) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();

      SystemManage sm = SystemManage.getSystemManage(systemKind);
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

      if (sm != null) {
        sm.majorVersion = majorVersion;
        sm.minorVersion = minorVersion;
        sm.patchVersion = patchVersion;
        sm.vuuid = "V" + sm.majorVersion + "." + sm.minorVersion + "." + sm.patchVersion + "." + sdf.format(Calendar.getInstance().getTime()) ;
        sm.compUpdate = compUpdate;
        sm.update();
      }
      else {
        sm = new SystemManage();
        sm.systemKind = systemKind;
        sm.majorVersion = majorVersion;
        sm.minorVersion = minorVersion;
        sm.patchVersion = patchVersion;
        sm.vuuid = "V" + sm.majorVersion + "." + sm.minorVersion + "." + sm.patchVersion + "." + sdf.format(Calendar.getInstance().getTime()) ;
        sm.compUpdate = compUpdate;
        sm.save();
      }

      return ok(resultJson);
  }
}
