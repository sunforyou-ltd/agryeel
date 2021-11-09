package controllers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Account;
import models.AccountStatus;
import models.ActiveLog;
import models.Attachment;
import models.Common;
import models.Hinsyu;
import models.IkubyoDiaryDetail;
import models.IkubyoDiarySanpu;
import models.IkubyoLine;
import models.IkubyoPlanLine;
import models.IkubyoPlanSanpu;
import models.Kiki;
import models.Nouhi;
import models.Sequence;
import models.Soil;
import models.Work;
import models.Youki;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CommonGetWorkDiaryData;
import compornent.CommonIkubyoDiary;
import compornent.CommonIkubyoDiaryWork;
import compornent.FarmComprtnent;
import compornent.NouhiComprtnent;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;

import consts.AgryeelConst;

public class IkubyoDiary extends Controller {

    private static final String SCREENID     = "IKUBYODIARY";
    private static final String ACTIONMOVE   = "MOVE";
    private static final String ACTIONSUBMIT = "SUBMIT";
    private static final String ACTIONDELETE = "DELETE";

    /**
     * 【AICA】育苗日誌画面遷移
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result ikubyoDiaryMove(String workid, String naeNo) {

        session(AgryeelConst.SessionKey.WORKID
                , workid);									//作業IDをセッションに格納
        session(AgryeelConst.SessionKey.NAENO
                , naeNo.replace("NONE", ""));				//苗Noをセッションに格納
        session(AgryeelConst.SessionKey.IKUBYODIARYID
                , "");										//育苗記録IDをセッションに格納
        session(AgryeelConst.SessionKey.IKUBYOPLANID
                , "");										//育苗計画IDをセッションに格納

        session(AgryeelConst.SessionKey.BACK_MODE, "");
        session(AgryeelConst.SessionKey.BACK_ACCOUNT, "");
        session(AgryeelConst.SessionKey.BACK_DATE, "");

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONMOVE);

        return ok(views.html.ikubyoDiary.render(""));

    }

    /**
     * 【AICA】育苗日誌画面編集遷移
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result ikubyoDiaryEditMove(double ikubyoDiaryId) {

        session(AgryeelConst.SessionKey.WORKID
                , "");										//作業IDをセッションに格納
        session(AgryeelConst.SessionKey.NAENO
                , "");										//苗Noをセッションに格納
        session(AgryeelConst.SessionKey.IKUBYODIARYID
                , String.valueOf(ikubyoDiaryId));			//育苗記録IDをセッションに格納
        session(AgryeelConst.SessionKey.IKUBYOPLANID
                , "");										//育苗計画IDをセッションに格納

        session(AgryeelConst.SessionKey.BACK_MODE, "");
        session(AgryeelConst.SessionKey.BACK_ACCOUNT, "");
        session(AgryeelConst.SessionKey.BACK_DATE, "");

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONMOVE);

        return ok(views.html.ikubyoDiary.render(""));

    }

    /**
     * 【AICA】育苗日誌初期表示データ取得
     * @return
     */
    public static Result ikubyoDiaryInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();
        boolean     api        = false;
        if (session(AgryeelConst.SessionKey.API) != null) {
            api = true;
        }

        /*-------------------------------------------------------------------*/
        /* アカウント情報の取得
        /*-------------------------------------------------------------------*/
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        AccountStatus status = accountComprtnent.accountStatusData;
        resultJson.put("workdateautoset", status.workDateAutoSet);

        /*-------------------------------------------------------------------*/
        /* 生産者情報の取得
        /*-------------------------------------------------------------------*/
        FarmComprtnent fc = new FarmComprtnent();
        fc.GetFarmData(accountComprtnent.accountData.farmId);

        /*-------------------------------------------------------------------*/
        /* 作業情報の取得
        /*-------------------------------------------------------------------*/
        CommonIkubyoDiary cid	   = new CommonIkubyoDiary(session(), resultJson);	//作業記録共通項目

        cid.init();																	//初期処理

        /*-------------------------------------------------------------------*/
        /* 各一覧表情報の取得                                                */
        /*-------------------------------------------------------------------*/
        CommonIkubyoDiaryWork cidk = null;
        if(api){
          cidk = CommonIkubyoDiaryWork.getCommonIkubyoDiaryWorkApi((int)cid.work.workTemplateId, session(), resultJson);     //育苗記録作業別項目
        }
        else {
          cidk = CommonIkubyoDiaryWork.getCommonIkubyoDiaryWork((int)cid.work.workTemplateId, session(), resultJson);        //育苗記録作業別項目
        }

        if (cidk != null) {								//作業項目別コンポーネントが生成できた場合

            cidk.naeNo       = cid.naeNo;
            cidk.workId      = cid.workId;
            cidk.ikubyoDiary = cid.ikubyoDiary;
            cidk.ikubyoPlan  = cid.ikubyoPlan;
            cidk.init();								//初期処理

        }
        /*-------------------------------------------------------------------*/

        resultJson.put("backmode", session(AgryeelConst.SessionKey.BACK_MODE));
        resultJson.put("backaccount", session(AgryeelConst.SessionKey.BACK_ACCOUNT));
        resultJson.put("backdate", session(AgryeelConst.SessionKey.BACK_DATE));

        return ok(resultJson);
    }
    /**
     * 【AICA】育苗日誌記録処理
     * @return
     */
    public static Result submitIkubyoDiary() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        updateIkubyoDiary(resultJson, input, 0);

        return ok(resultJson);
    }
    /**
     * 【AICA】育苗日誌記録処理(編集時)
     * @return
     */
    public static Result submitIkubyoDiaryEdit(double ikubyoDiaryId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        updateIkubyoDiary(resultJson, input, ikubyoDiaryId);

        return ok(resultJson);
    }

    public static void updateIkubyoDiary(ObjectNode resultJson, JsonNode input, double editIkubyoDiaryId) {
      updateIkubyoDiary(resultJson, input, editIkubyoDiaryId, false);
    }
    public static void updateIkubyoDiary(ObjectNode resultJson, JsonNode input, double editIkubyoDiaryId, boolean api) {

      try {

        IkubyoDiaryThread  idt  = new IkubyoDiaryThread();
        Ebean.beginTransaction();
        int workId = Integer.parseInt(input.get("workId").asText());	//作業IDの取得

        /*-- 取得した項目を作業日誌に格納する --*/
        Work work = Work.find.where().eq("work_id", workId).findUnique();                 //作業情報モデルの取得
        String sNaeNo = input.get("workNae").asText();                                    //複数苗同時育苗記録
        resultJson.put("naes", sNaeNo);                                                   //連続作業入力用に苗をJSONに格納
        String[] sNaeNos = sNaeNo.split(",");
        double ikubyoDiaryId = 0;
        DecimalFormat df = new DecimalFormat("#,##0.00");

        for (String naeNo : sNaeNos) {
          models.IkubyoDiary ikubyoDiary = new models.IkubyoDiary();                      //育苗日誌モデルの生成
          CommonIkubyoDiary cid          = new CommonIkubyoDiary(session(), resultJson);  //育苗記録共通項目
          ArrayList<IkubyoDiarySanpu> idsps = new ArrayList<IkubyoDiarySanpu>();

          /*-------------------------------------------------------------------*/
          /* 編集時は対象となる育苗記録を一旦削除する                          */
          /*-------------------------------------------------------------------*/
          if (editIkubyoDiaryId != 0) {
            models.IkubyoDiary id =models.IkubyoDiary.getIkubyoDiaryById(editIkubyoDiaryId);
            if (id != null) {
              ikubyoDiary.workStartTime = id.workStartTime;
              ikubyoDiary.workEndTime   = id.workEndTime;
            }
            ikubyoDiaryDeleteCommit(editIkubyoDiaryId);
            if (sNaeNos.length > 1) {    //複数苗の場合
              editIkubyoDiaryId = 0;     //新規育苗記録IDを発行する為、育苗記録IDをリセットする
            }
          }

          /*-------------------------------------------------------------------*/
          /* 共通項目の反映                                                    */
          /*-------------------------------------------------------------------*/
          ikubyoDiary.hinsyuId = "0"; //品種IDの初期値を設定
          cid.commit(input, ikubyoDiary, work, editIkubyoDiaryId, naeNo);

          /*-------------------------------------------------------------------*/
          /* 作業別項目の反映                                                  */
          /*-------------------------------------------------------------------*/
          CommonIkubyoDiaryWork cidk = CommonIkubyoDiaryWork.getCommonIkubyoDiaryWork((int)cid.work.workTemplateId, session(), resultJson);        //育苗記録作業別項目

          if (cidk != null) {         //作業項目別コンポーネントが生成できた場合
            cidk.apiFlg = api;
            cidk.commit(input, ikubyoDiary, work);    //作業別項目の生成

          }

          /* 育苗記録をこのタイミングで保存する */
          ikubyoDiary.save();

          /* -- 育苗ラインを作成する -- */
          IkubyoLine ikubyoLine = new IkubyoLine();                                       //育苗ラインモデルの生成
          Account account = Account.find.where().eq("account_id", ikubyoDiary.accountId).findUnique();        //アカウント情報モデルの取得

          ikubyoLine.ikubyoDiaryId = ikubyoDiary.ikubyoDiaryId;                                   //育苗記録ID

          ikubyoLine.updateTime       = DateU.getSystemTimeStamp();
          //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
          ikubyoLine.message        = "【" + work.workName + "情報】<br>";
          switch ((int)cid.work.workTemplateId) {
          case AgryeelConst.WorkTemplate.NOMAL:
            ikubyoLine.message        += "";                                 //メッセージ
            break;
          case AgryeelConst.WorkTemplate.SANPU:

            List<IkubyoDiarySanpu> idss = IkubyoDiarySanpu.getIkubyoDiarySanpuList(ikubyoDiary.ikubyoDiaryId);

            for (IkubyoDiarySanpu ids : idss) {

              /* 農肥IDから農肥情報を取得する */
              Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  ids.nouhiId).findUnique();

              String sanpuName  = "";

              if (ids.sanpuMethod != 0) {
                  sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, ids.sanpuMethod) + "]";
              }

              String unit = nouhi.getUnitString();

              ikubyoLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df.format(ids.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
              ikubyoLine.message        +=  "--------------------------------------------------<br>";

              idsps.add(ids);

            }

            break;
          case AgryeelConst.WorkTemplate.KANSUI:
            ikubyoLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, ikubyoDiary.kansuiMethod) + "<br>";
            ikubyoLine.message       += "<機器> " + Kiki.getKikiName(ikubyoDiary.kikiId) + "<br>";
            ikubyoLine.message       += "<潅水量> " + ikubyoDiary.kansuiRyo + "L" + "<br>";
            ikubyoLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.NAEHASHUIK:
            /* 容器IDから容器情報を取得する */
            Youki youki = Youki.find.where().eq("youki_id",  ikubyoDiary.youkiId).findUnique();
            /* 土IDから培土情報を取得する */
            Soil baido = Soil.find.where().eq("soil_id",  ikubyoDiary.baidoId).findUnique();
            /* 土IDから覆土情報を取得する */
            Soil fukudo = Soil.find.where().eq("soil_id",  ikubyoDiary.fukudoId).findUnique();

            ikubyoLine.message       += "<品種> " + Hinsyu.getMultiHinsyuName(ikubyoDiary.hinsyuId) + "<br>";
            ikubyoLine.message       += "<容器> " + Youki.getYoukiName(ikubyoDiary.youkiId) + "<br>";
            ikubyoLine.message       += "<数量> " + ikubyoDiary.naeSuryo + youki.getUnitString() + " (" + ikubyoDiary.kosu + "個)<br>";
            if (ikubyoDiary.baidoId != 0) {
              ikubyoLine.message       += "<培土> " + Soil.getSoilName(ikubyoDiary.baidoId)  + "  " + ikubyoDiary.baidoSuryo + baido.getUnitString() + "<br>";
            } else {
              ikubyoLine.message       += "<培土> 未選択<br>";
            }
            if (ikubyoDiary.fukudoId != 0) {
              ikubyoLine.message       += "<覆土> " + Soil.getSoilName(ikubyoDiary.fukudoId)  + "  " + ikubyoDiary.fukudoSuryo + fukudo.getUnitString() + "<br>";
            } else {
              ikubyoLine.message       += "<覆土> 未選択<br>";
            }
            ikubyoLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.KARITORIIK:
            ikubyoLine.message       += "<刈取高> " + ikubyoDiary.senteiHeight + "cm" + "<br>";
            ikubyoLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.HAIKIIK:
            ikubyoLine.message       += "<廃棄量> " + ikubyoDiary.haikiRyo + "個" + "<br>";
            ikubyoLine.message       +=  "--------------------------------------------------<br>";
            break;
          }

          //作業時間をメッセージに追加する
          SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
          if (ikubyoDiary.workStartTime != null) {
            ikubyoLine.message += "【 開始時間 】 " + sdf.format(ikubyoDiary.workStartTime);
          }
          if (ikubyoDiary.workEndTime != null) {
            ikubyoLine.message += "【 終了時間 】 " + sdf.format(ikubyoDiary.workEndTime);
          }
          //ヘルスケアデータをメッセージに追加する
          if (
              (ikubyoDiary.numberOfSteps  != 0)
              || (ikubyoDiary.distance    != 0)
              || (ikubyoDiary.calorie     != 0)
              || (ikubyoDiary.heartRate   != 0)
              ) {
            ikubyoLine.message += "<br>【 歩数 】 " + ikubyoDiary.numberOfSteps + "歩<br>";
            ikubyoLine.message += "【 距離 】 " + ikubyoDiary.distance + "m<br>";
            ikubyoLine.message += "【 カロリー 】 " + ikubyoDiary.calorie + "kcal<br>";
            ikubyoLine.message += "【 心拍数 】 " + ikubyoDiary.heartRate + "/bpm<br>";
            ikubyoLine.message       +=  "--------------------------------------------------<br>";
          }

          ikubyoLine.timeLineColor = work.workColor;                      //タイムラインカラー
          ikubyoLine.workId        = work.workId;                         //作業ID
          ikubyoLine.workName      = work.workName;                       //作業名
          ikubyoLine.workDate      = ikubyoDiary.workDate;                //作業日
          ikubyoLine.numberOfSteps = ikubyoDiary.numberOfSteps;           //歩数
          ikubyoLine.distance      = ikubyoDiary.distance;                //距離
          ikubyoLine.calorie       = ikubyoDiary.calorie;                 //カロリー
          ikubyoLine.heartRate     = ikubyoDiary.heartRate;               //心拍数
          ikubyoLine.naeNo         = ikubyoDiary.naeNo;                   //区画ID
          ikubyoLine.workStartTime = ikubyoDiary.workStartTime;           //作業開始時間
          ikubyoLine.workEndTime   = ikubyoDiary.workEndTime;             //作業終了時間
          ikubyoLine.naeNo         = ikubyoDiary.naeNo;                   //区画ID
          ikubyoLine.accountId     = account.accountId;                   //アカウントID
          ikubyoLine.accountName   = account.acountName;                  //アカウント名
          ikubyoLine.farmId        = account.farmId;                      //農場ID

          ikubyoLine.save();                                              //育苗ラインを追加

          idt.account   = account;
          idt.idspss.add(idsps);
          idt.ikubyoDiarys.add(ikubyoDiary);

          if (ikubyoDiaryId == 0) {
            ikubyoDiaryId = ikubyoDiary.ikubyoDiaryId;
          }

        }

        //全て正常終了の場合、作業中情報を初期化
        UserComprtnent accountComprtnent = new UserComprtnent();                          //入力回数を更新する
        accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        accountComprtnent.InputCountUP();

        //作業記録後の判定
        resultJson.put("ikubyoDiaryId", ikubyoDiaryId);
        resultJson.put("workCommitAfter", accountComprtnent.accountStatusData.workCommitAfter);

        int mode = input.get("mode").asInt();
        if (mode == AgryeelConst.WorkDiaryMode.WORKING) { // 作業中の場合のみ
          int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
          Account ac = accountComprtnent.accountData;

          ac.workId   = 0;
          ac.fieldId  = 0;
          ac.workStartTime = null;
          ac.notificationMessage = "";
          ac.messageIcon = AgryeelConst.MessageIcon.NONE;
          ac.workPlanId = 0;
          ac.update();
        }

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONSUBMIT, String.format("[ikubyoDiaryId]=%f", ikubyoDiaryId));

        Ebean.commitTransaction();
        idt.start();
      }
      catch (Exception e) {
        Logger.error(e.getMessage(),e);
        e.printStackTrace();
        Ebean.rollbackTransaction();
      }
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
    }

    /**
     * 【AGRYEEL】育苗計画記録開始
     * @return
     */
    public static Result submitIkubyoPlan() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        updateIkubyoPlan(resultJson, input);

        return ok(resultJson);
    }
    public static void updateIkubyoPlan(ObjectNode resultJson, JsonNode input) {
      updateIkubyoPlan(resultJson, input, false);
    }
    public static void updateIkubyoPlan(ObjectNode resultJson, JsonNode input, boolean api) {

      try {

        Ebean.beginTransaction();
        int workId = Integer.parseInt(input.get("workId").asText());              //作業IDの取得
        int ikubyoPlanId = Integer.parseInt(input.get("ikubyoPlanId").asText());  //育苗計画ＩＤの取得

        /*-- 取得した項目を作業日誌に格納する --*/
        Work work = Work.find.where().eq("work_id", workId).findUnique();         //作業情報モデルの取得
        String sNaeNo = input.get("workNae").asText();                            //複数苗同時育苗記録
        String[] sNaeNos = sNaeNo.split(",");
        double ikubyoDiaryId = 0;
        DecimalFormat df = new DecimalFormat("#,##0.00");

        //----- 作業指示関係 -----
        SimpleDateFormat sdfu = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        DecimalFormat    dff  = new DecimalFormat("000000");
        int ikubyoPlanType = input.get("ikubyoPlanType").asInt();
        UserComprtnent myac = new UserComprtnent();
        myac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        Calendar system = Calendar.getInstance();

        for (String naeNo : sNaeNos) {
          models.IkubyoPlan ikubyoplan = new models.IkubyoPlan();                               //育苗日誌モデルの生成
          CommonIkubyoDiary cid       = new CommonIkubyoDiary(session(), resultJson);           //育苗記録共通項目

          /*-------------------------------------------------------------------*/
          /* 編集時は対象となる育苗記録を一旦削除する                          */
          /*-------------------------------------------------------------------*/
          if (ikubyoPlanId != 0) {
            ikubyoPlanDelete(ikubyoPlanId);
          }

          /*-------------------------------------------------------------------*/
          /* 共通項目の反映                                                    */
          /*-------------------------------------------------------------------*/
          ikubyoplan.hinsyuId = "0";
          cid.plan(input, ikubyoplan, work, naeNo);

          /*-------------------------------------------------------------------*/
          /* 作業別項目の反映                                                  */
          /*-------------------------------------------------------------------*/
          CommonIkubyoDiaryWork cidk = CommonIkubyoDiaryWork.getCommonIkubyoDiaryWork((int)cid.work.workTemplateId, session(), resultJson);        //育苗記録作業別項目

          if (cidk != null) {               //作業項目別コンポーネントが生成できた場合
            cidk.apiFlg = api;
            cidk.plan(input, ikubyoplan, work);    //作業別項目の生成

          }

          /* 作業記録をこのタイミングで保存する */
          ikubyoplan.save();

          /* -- 育苗プランラインを作成する -- */
          IkubyoPlanLine planLine = new IkubyoPlanLine();                                                  //タイムラインモデルの生成
          Account account = Account.find.where().eq("account_id", ikubyoplan.accountId).findUnique();      //アカウント情報モデルの取得

          planLine.ikubyoPlanId = ikubyoplan.ikubyoPlanId;

          planLine.updateTime       = DateU.getSystemTimeStamp();
          //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
          planLine.message        = "【" + work.workName + "情報】<br>";
          switch ((int)cid.work.workTemplateId) {
          case AgryeelConst.WorkTemplate.NOMAL:
            planLine.message        += "";                                 //メッセージ
            break;
          case AgryeelConst.WorkTemplate.SANPU:

            List<IkubyoPlanSanpu> ipss = IkubyoPlanSanpu.getIkubyoPlanSanpuList(ikubyoplan.ikubyoPlanId);

            for (IkubyoPlanSanpu ips : ipss) {

              /* 農肥IDから農肥情報を取得する */
              Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  ips.nouhiId).findUnique();

              String sanpuName  = "";

              if (ips.sanpuMethod != 0) {
                  sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, ips.sanpuMethod) + "]";
              }

              String unit = nouhi.getUnitString();

              planLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df.format(ips.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
              planLine.message       +=  "--------------------------------------------------<br>";

            }

            break;
          case AgryeelConst.WorkTemplate.KANSUI:
            planLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, ikubyoplan.kansuiMethod) + "<br>";
            planLine.message       += "<機器> " + Kiki.getKikiName(ikubyoplan.kikiId) + "<br>";
            planLine.message       += "<潅水量> " + ikubyoplan.kansuiRyo + "L" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.NAEHASHUIK:
            /* 容器IDから容器情報を取得する */
            Youki youki = Youki.find.where().eq("youki_id",  ikubyoplan.youkiId).findUnique();
            /* 土IDから培土情報を取得する */
            Soil baido = Soil.find.where().eq("soil_id",  ikubyoplan.baidoId).findUnique();
            /* 土IDから覆土情報を取得する */
            Soil fukudo = Soil.find.where().eq("soil_id",  ikubyoplan.fukudoId).findUnique();

            planLine.message       += "<品種> " + Hinsyu.getMultiHinsyuName(ikubyoplan.hinsyuId) + "<br>";
            if (youki != null) {
              planLine.message     += "<容器> " + Youki.getYoukiName(ikubyoplan.youkiId) + "<br>";
            }
            planLine.message       += "<数量> " + ikubyoplan.naeSuryo + youki.getUnitString() + " (" + ikubyoplan.kosu + "個)<br>";
            if (baido != null) {
              planLine.message     += "<培土> " + Soil.getSoilName(ikubyoplan.baidoId)  + "  " + ikubyoplan.baidoSuryo + baido.getUnitString() + "<br>";
            }
            if (fukudo != null) {
              planLine.message     += "<覆土> " + Soil.getSoilName(ikubyoplan.fukudoId)  + "  " + ikubyoplan.fukudoSuryo + fukudo.getUnitString() + "<br>";
            }
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.KARITORIIK:
            planLine.message       += "<刈取高> " + ikubyoplan.senteiHeight + "cm" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.HAIKIIK:
            planLine.message       += "<廃棄量> " + ikubyoplan.haikiRyo + "個" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          }

          //作業時間をメッセージに追加する
          SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
          if (ikubyoplan.workStartTime != null && (ikubyoPlanType == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH || ikubyoPlanType == AgryeelConst.WORKPLANFLAG.AICAPLANWATCH)) {
            planLine.message += "【 開始時間 】 " + sdf.format(ikubyoplan.workStartTime);
          }
          //ヘルスケアデータをメッセージに追加する
          if (
              (ikubyoplan.numberOfSteps  != 0)
              || (ikubyoplan.distance    != 0)
              || (ikubyoplan.calorie     != 0)
              || (ikubyoplan.heartRate   != 0)
              ) {
            planLine.message += "<br>【 歩数 】 " + ikubyoplan.numberOfSteps + "歩<br>";
            planLine.message += "【 距離 】 " + ikubyoplan.distance + "m<br>";
            planLine.message += "【 カロリー 】 " + ikubyoplan.calorie + "kcal<br>";
            planLine.message += "【 心拍数 】 " + ikubyoplan.heartRate + "/bpm<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
          }

          planLine.timeLineColor  = work.workColor;                                 //プランラインカラー
          planLine.workId         = work.workId;                                    //作業ID
          planLine.workName       = work.workName;                                  //作業名
          planLine.workDate       = ikubyoplan.workDate;                            //作業日
          planLine.numberOfSteps  = ikubyoplan.numberOfSteps;                       //歩数
          planLine.distance       = ikubyoplan.distance;                            //距離
          planLine.calorie        = ikubyoplan.calorie;                             //カロリー
          planLine.heartRate      = ikubyoplan.heartRate;                           //心拍数
          planLine.naeNo          = ikubyoplan.naeNo;                               //苗No
          planLine.workStartTime  = ikubyoplan.workStartTime;                       //作業開始時間
          planLine.workEndTime    = ikubyoplan.workEndTime;                         //作業終了時間
          planLine.accountId      = account.accountId;                              //アカウントID
          planLine.accountName    = account.acountName;                             //アカウント名
          planLine.farmId         = account.farmId;                                 //農場ID
          planLine.workStartTime  = ikubyoplan.workStartTime;                       //作業開始時間
          planLine.workEndTime    = ikubyoplan.workEndTime;                         //作業収量時間

          planLine.save();                                                          //タイムラインを追加

          if (ikubyoPlanType == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH || ikubyoPlanType == AgryeelConst.WORKPLANFLAG.AICAPLANWATCH) {
            UserComprtnent accountComprtnent = new UserComprtnent();
            int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
            Account ac = accountComprtnent.accountData;

            ac.workId   = work.workId;
            ac.fieldId  = 0;
            ac.workStartTime = ikubyoplan.workStartTime;
            ac.workPlanId = ikubyoplan.ikubyoPlanId;
            ac.update();

            Work        wk = Work.getWork(ac.workId);

            Logger.info("[ IKUBYO WORKING START ] ID:{} NAME:{} NAENO:{} WORKID:{} WORKNAME:{} STARTTIME:{}", ac.accountId, ac.acountName, ikubyoplan.naeNo, wk.workId, wk.workName, sdf.format(ac.workStartTime));

            session(AgryeelConst.SessionKey.WORKING_ACTION, "display");
            accountComprtnent.getAccountJson(resultJson);
          }
          ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONSUBMIT, String.format("[ikubyoPlanId]=%f", planLine.ikubyoPlanId));
        }
        Ebean.commitTransaction();
      }
      catch (Exception e) {
        Logger.error(e.getMessage(), e);
        e.printStackTrace();
        Ebean.rollbackTransaction();
      }

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

  }

    /**
     * 【AGRYEEL】農肥情報を取得します
     * @return
     */
    public static Result getNouhiValueIkubyo() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        double nouhiId = Double.parseDouble(input.get("nouhiId").asText());	//農肥IDの取得
        DecimalFormat df = new DecimalFormat("##0.00");

        /* 農肥IDから農肥情報を取得する */
        Nouhi nouhi = (Nouhi)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.NOUHI, "nouhi_id = " + nouhiId);
        double hosei = 1;
        if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
          hosei = 0.001;
        }
        double sanpuryo = nouhi.sanpuryo * hosei;

        resultJson.put("bairitu"	, nouhi.bairitu);				//倍率
        resultJson.put("sanpuryo"	, df.format(sanpuryo));			//散布量
        resultJson.put("unit"		, nouhi.unitKind);				//単位種別
        resultJson.put("lower"		, nouhi.lower);					//倍率下限
        resultJson.put("upper"		, nouhi.upper);					//倍率上限

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】育苗計画記録完了
     * @return
     */
    public static Result planToDiaryIkubyo() {
      return planToDiaryCommit(true);
    }
    /**
     * 【AGRYEEL】育苗計画記録完了
     * @return
     */
    public static Result planToDiaryTimeCommitIkubyo() {
      return planToDiaryCommit(false);
    }
    /**
     * 【AGRYEEL】育苗計画記録完了
     * @return
     */
    public static Result planToDiaryCommit(boolean timeFlag) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        try {

          Ebean.beginTransaction();
          IkubyoDiaryThread  idt  = new IkubyoDiaryThread();
          UserComprtnent accountComprtnent = new UserComprtnent();
          int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
          Account ac = accountComprtnent.accountData;
          String naeNo = "";
          int workId   = 0;

          models.IkubyoPlan ikubyoPlan = models.IkubyoPlan.find.where().eq("ikubyo_plan_id", Double.parseDouble(input.get("planId").asText())).findUnique();
          if (ikubyoPlan != null) {
            workId = (int)ikubyoPlan.workId;
            naeNo = ikubyoPlan.naeNo;
          }

          Work wk = Work.getWork(workId);
          ArrayList<IkubyoDiarySanpu> idsps = new ArrayList<IkubyoDiarySanpu>();
          DecimalFormat df = new DecimalFormat("#,##0.00");

          Logger.info("[ IKUBYO PLANTODIARY ] ACCOUNTID={} WORKID={} PLANID={}", session(AgryeelConst.SessionKey.ACCOUNTID), workId, ikubyoPlan.ikubyoPlanId);
          Logger.info("[ IKUBYO PLANTODIARY ] NAENO={} ", ikubyoPlan.naeNo);

          /*-- 取得した項目を作業日誌に格納する --*/
          Work work = Work.find.where().eq("work_id", workId).findUnique();                       //作業情報モデルの取得
          double ikubyoDiaryId = 0;
          Calendar cal = Calendar.getInstance();
          java.sql.Timestamp jst = new Timestamp(cal.getTimeInMillis());

          List<models.IkubyoPlan> ikubyoplans;
          if (timeFlag) { //時間計測ありの場合
            ikubyoplans = models.IkubyoPlan.find.where().eq("work_id", workId).eq("account_id", ac.accountId).orderBy("ikubyo_plan_id").findList();
          }
          else {
            ikubyoplans = models.IkubyoPlan.find.where().eq("ikubyo_plan_id", Double.parseDouble(input.get("planId").asText())).findList();
          }

          long to   = cal.getTime().getTime();

          for (models.IkubyoPlan ikubyoplan: ikubyoplans) {
            models.IkubyoDiary ikubyoDiary = new models.IkubyoDiary();
            if (ikubyoplan == null) {
              Logger.error("[ WORKING IKUBYO PLAN NODATA ] ID:{} NAME:{} WORKID:{} WORKNAME:{}", ac.accountId, ac.acountName, wk.workId, wk.workName);
            }
            if (timeFlag && ikubyoplan.workStartTime == null) {
              continue;
            }
            Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.IKUBYODIARYID);              //最新シーケンス値の取得
            ikubyoDiaryId       = sequence.sequenceValue;

            //workDiary項目の設定
            ikubyoDiary.ikubyoDiaryId       = ikubyoDiaryId;
            ikubyoDiary.workId              = ikubyoplan.workId;
            ikubyoDiary.naeNo               = ikubyoplan.naeNo;
            ikubyoDiary.accountId           = ikubyoplan.accountId;
            ikubyoDiary.workDate            = ikubyoplan.workDate;
            if (timeFlag) { //時間計測ありの場合
              //作業時間の算出
              long from = ikubyoplan.workStartTime.getTime();
              long diff = ( to - from  ) / (1000 * 60 );
              long time = diff / ikubyoplans.size();
              ikubyoDiary.workTime            = (int)time;
            }
            else {
              ikubyoDiary.workTime          = ikubyoplan.workTime;
            }
            ikubyoDiary.detailSettingKind   = ikubyoplan.detailSettingKind;
            ikubyoDiary.combiId             = ikubyoplan.combiId;
            ikubyoDiary.kikiId              = ikubyoplan.kikiId;
            ikubyoDiary.attachmentId        = ikubyoplan.attachmentId;
            ikubyoDiary.hinsyuId            = ikubyoplan.hinsyuId;
            ikubyoDiary.beltoId             = ikubyoplan.beltoId;
            ikubyoDiary.kansuiPart          = ikubyoplan.kansuiPart;
            ikubyoDiary.kansuiSpace         = ikubyoplan.kansuiSpace;
            ikubyoDiary.kansuiMethod        = ikubyoplan.kansuiMethod;
            ikubyoDiary.kansuiRyo           = ikubyoplan.kansuiRyo;
            ikubyoDiary.naeStatusUpdate     = ikubyoplan.naeStatusUpdate;
            ikubyoDiary.workRemark          = ikubyoplan.workRemark;
            if (timeFlag) { //時間計測ありの場合
              ikubyoDiary.workStartTime     = ikubyoplan.workStartTime;
            }
            else {
              Calendar calStart = Calendar.getInstance();
              calStart.add(Calendar.MINUTE, -1 * ikubyoplan.workTime);
              ikubyoDiary.workStartTime   = new Timestamp(calStart.getTimeInMillis());
            }
            ikubyoDiary.workEndTime         = jst;
            ikubyoDiary.numberOfSteps       = ikubyoplan.numberOfSteps;
            ikubyoDiary.distance            = ikubyoplan.distance;
            ikubyoDiary.calorie             = ikubyoplan.calorie;
            ikubyoDiary.heartRate           = ikubyoplan.heartRate;
            ikubyoDiary.naeSuryo            = ikubyoplan.naeSuryo;
            ikubyoDiary.kosu                = ikubyoplan.kosu;
            ikubyoDiary.youkiId             = ikubyoplan.youkiId;
            ikubyoDiary.baidoId             = ikubyoplan.baidoId;
            ikubyoDiary.baidoSuryo          = ikubyoplan.baidoSuryo;
            ikubyoDiary.fukudoId            = ikubyoplan.fukudoId;
            ikubyoDiary.fukudoSuryo         = ikubyoplan.fukudoSuryo;
            ikubyoDiary.senteiHeight        = ikubyoplan.senteiHeight;
            ikubyoDiary.haikiRyo            = ikubyoplan.haikiRyo;
            ikubyoDiary.save();

            List<models.IkubyoPlanSanpu> ipss = models.IkubyoPlanSanpu.find.where().eq("ikubyo_plan_id", ikubyoplan.ikubyoPlanId).orderBy("ikubyo_diary_sequence asc").findList();
            for (models.IkubyoPlanSanpu ips : ipss) {
              models.IkubyoDiarySanpu ids = new IkubyoDiarySanpu();

              //IkubyoDiarySanpuの項目設定
              ids.ikubyoDiaryId       = ikubyoDiaryId;
              ids.ikubyoDiarySequence = ips.ikubyoDiarySequence;
              ids.sanpuMethod         = ips.sanpuMethod;
              ids.kikiId              = ips.kikiId;
              ids.attachmentId        = ips.attachmentId;
              ids.nouhiId             = ips.nouhiId;
              ids.bairitu             = ips.bairitu;
              ids.sanpuryo            = ips.sanpuryo;
              ids.naeStatusUpdate     = ips.naeStatusUpdate;
              ids.save();
              //農肥履歴情報の更新
              Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  ids.nouhiId).findUnique();
              if (nouhi != null) {
                /* 農肥前回情報として保存する */
                nouhi.bairitu   = ids.bairitu;
                nouhi.sanpuryo  = ids.sanpuryo;
                nouhi.update();
              }
            }

            List<models.IkubyoPlanDetail> ipds = models.IkubyoPlanDetail.find.where().eq("ikubyo_plan_id", ikubyoplan.ikubyoPlanId).orderBy("ikubyo_diary_sequence asc").findList();
            for (models.IkubyoPlanDetail ipd : ipds) {
              models.IkubyoDiaryDetail idd = new IkubyoDiaryDetail();

              //IkubyoDiaryDetailの項目設定
              idd.ikubyoDiaryId         = ikubyoDiaryId;
              idd.ikubyoDiarySequence   = ipd.ikubyoDiarySequence;
              idd.workDetailKind      = ipd.workDetailKind;
              idd.naeNo               = ipd.naeNo;
              idd.save();
            }

            /* -- 育苗ラインを作成する -- */
            IkubyoLine ikubyoLine = new IkubyoLine();                                                                 //育苗ラインモデルの生成
            Account account     = Account.find.where().eq("account_id", ikubyoDiary.accountId).findUnique();            //アカウント情報モデルの取得

            ikubyoLine.ikubyoDiaryId    = ikubyoDiaryId;

            ikubyoLine.updateTime       = DateU.getSystemTimeStamp();
            //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
            ikubyoLine.message        = "【" + work.workName + "情報】<br>";
            switch ((int)wk.workTemplateId) {
            case AgryeelConst.WorkTemplate.NOMAL:
              ikubyoLine.message        += "";                                 //メッセージ
              break;
            case AgryeelConst.WorkTemplate.SANPU:

              List<IkubyoDiarySanpu> idss = IkubyoDiarySanpu.getIkubyoDiarySanpuList(ikubyoDiary.ikubyoDiaryId);

              for (IkubyoDiarySanpu ids : idss) {

                /* 農肥IDから農肥情報を取得する */
                Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  ids.nouhiId).findUnique();

                String sanpuName  = "";

                if (ids.sanpuMethod != 0) {
                  sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, ids.sanpuMethod) + "]";
                }

                String unit = nouhi.getUnitString();

                ikubyoLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df.format(ids.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
                ikubyoLine.message        +=  "--------------------------------------------------<br>";

                idsps.add(ids);

              }

              break;
            case AgryeelConst.WorkTemplate.KANSUI:
              ikubyoLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, ikubyoDiary.kansuiMethod) + "<br>";
              ikubyoLine.message       += "<機器> " + Kiki.getKikiName(ikubyoDiary.kikiId) + "<br>";
              ikubyoLine.message       += "<潅水量> " + ikubyoDiary.kansuiRyo + "L" + "<br>";
              ikubyoLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.NAEHASHUIK:
              /* 容器IDから容器情報を取得する */
              Youki youki = Youki.find.where().eq("youki_id",  ikubyoDiary.youkiId).findUnique();
              /* 土IDから培土情報を取得する */
              Soil baido = Soil.find.where().eq("soil_id",  ikubyoDiary.baidoId).findUnique();
              /* 土IDから覆土情報を取得する */
              Soil fukudo = Soil.find.where().eq("soil_id",  ikubyoDiary.fukudoId).findUnique();

              ikubyoLine.message       += "<品種> " + Hinsyu.getMultiHinsyuName(ikubyoDiary.hinsyuId) + "<br>";
              ikubyoLine.message       += "<容器> " + Youki.getYoukiName(ikubyoDiary.youkiId) + "<br>";
              ikubyoLine.message       += "<数量> " + ikubyoDiary.naeSuryo + youki.getUnitString() + " (" + ikubyoDiary.kosu + "個)<br>";
              if (ikubyoDiary.baidoId != 0) {
                ikubyoLine.message       += "<培土> " + Soil.getSoilName(ikubyoDiary.baidoId)  + "  " + ikubyoDiary.baidoSuryo + baido.getUnitString() + "<br>";
              } else {
                ikubyoLine.message       += "<培土> 未選択<br>";
              }
              if (ikubyoDiary.fukudoId != 0) {
                ikubyoLine.message       += "<覆土> " + Soil.getSoilName(ikubyoDiary.fukudoId)  + "  " + ikubyoDiary.fukudoSuryo + fukudo.getUnitString() + "<br>";
              } else {
                ikubyoLine.message       += "<覆土> 未選択<br>";
              }
              ikubyoLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.KARITORIIK:
              ikubyoLine.message       += "<刈取高> " + ikubyoDiary.senteiHeight + "cm" + "<br>";
              ikubyoLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.HAIKIIK:
              ikubyoLine.message       += "<廃棄量> " + ikubyoDiary.haikiRyo + "個" + "<br>";
              ikubyoLine.message       +=  "--------------------------------------------------<br>";
              break;
            }

            //作業時間をメッセージに追加する
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
            if (ikubyoDiary.workStartTime != null) {
              ikubyoLine.message += "【 開始時間 】 " + sdf.format(ikubyoDiary.workStartTime);
            }
            if (ikubyoDiary.workEndTime != null) {
              ikubyoLine.message += "【 終了時間 】 " + sdf.format(ikubyoDiary.workEndTime);
            }
            //ヘルスケアデータをメッセージに追加する
            if (
                (ikubyoDiary.numberOfSteps  != 0)
                || (ikubyoDiary.distance    != 0)
                || (ikubyoDiary.calorie     != 0)
                || (ikubyoDiary.heartRate   != 0)
                ) {
              ikubyoLine.message += "<br>【 歩数 】 " + ikubyoDiary.numberOfSteps + "歩<br>";
              ikubyoLine.message += "【 距離 】 " + ikubyoDiary.distance + "m<br>";
              ikubyoLine.message += "【 カロリー 】 " + ikubyoDiary.calorie + "kcal<br>";
              ikubyoLine.message += "【 心拍数 】 " + ikubyoDiary.heartRate + "/bpm<br>";
              ikubyoLine.message +=  "--------------------------------------------------<br>";
            }

            ikubyoLine.ikubyoDiaryId  = ikubyoDiary.ikubyoDiaryId;                  //育苗記録ID
            ikubyoLine.timeLineColor  = work.workColor;                             //タイムラインカラー
            ikubyoLine.workId         = work.workId;                                //作業ID
            ikubyoLine.workName       = work.workName;                              //作業名
            ikubyoLine.workDate       = ikubyoDiary.workDate;                       //作業日
            ikubyoLine.numberOfSteps  = ikubyoDiary.numberOfSteps;                  //歩数
            ikubyoLine.distance       = ikubyoDiary.distance;                       //距離
            ikubyoLine.calorie        = ikubyoDiary.calorie;                        //カロリー
            ikubyoLine.heartRate      = ikubyoDiary.heartRate;                      //心拍数
            ikubyoLine.naeNo          = ikubyoDiary.naeNo;                          //苗No
            ikubyoLine.workStartTime  = ikubyoDiary.workStartTime;                  //作業開始時間
            ikubyoLine.workEndTime    = ikubyoDiary.workEndTime;                    //作業終了時間
            ikubyoLine.accountId      = account.accountId;                          //アカウントID
            ikubyoLine.accountName    = account.acountName;                         //アカウント名
            ikubyoLine.farmId         = account.farmId;                             //農場ID

            ikubyoLine.save();                                                      //育苗ラインを追加

            resultJson.put("message",  ikubyoLine.message);
            resultJson.put("workTime", ikubyoDiary.workTime);

            idt.account   = account;
            idt.idspss.add(idsps);
            idt.ikubyoDiarys.add(ikubyoDiary);

            if (ikubyoDiaryId == 0) {
              ikubyoDiaryId = ikubyoDiary.ikubyoDiaryId;
            }

            Logger.info("[IkubyoPlan DELETE] ikubyoPlanId={}", ikubyoplan.ikubyoPlanId);

            Ebean.createSqlUpdate("DELETE FROM ikubyo_plan WHERE ikubyo_plan_id = :ikubyoPlanId")
            .setParameter("ikubyoPlanId", ikubyoplan.ikubyoPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_sanpu WHERE ikubyo_plan_id = :ikubyoPlanId")
            .setParameter("ikubyoPlanId", ikubyoplan.ikubyoPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_line WHERE ikubyo_plan_id = :ikubyoPlanId")
            .setParameter("ikubyoPlanId", ikubyoplan.ikubyoPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_detail WHERE ikubyo_plan_id = :ikubyoPlanId")
            .setParameter("ikubyoPlanId", ikubyoplan.ikubyoPlanId).execute();

            ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONSUBMIT, String.format("[ikubyoPlanId]=%f -> [ikubyoDiaryId]=%f", ikubyoplan.ikubyoPlanId, ikubyoDiary.ikubyoDiaryId));

          }

          ac.workId   = 0;
          ac.fieldId  = 0;
          ac.workStartTime = null;
          ac.notificationMessage = "";
          ac.messageIcon = AgryeelConst.MessageIcon.NONE;
          ac.workPlanId = 0;
          ac.update();

          Ebean.commitTransaction();
          idt.start();

        }
        catch (Exception e) {
          Logger.error(e.getMessage(),e);
          e.printStackTrace();
          Ebean.rollbackTransaction();
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アタッチメント一覧を取得します
     * @return
     */
    public static Result getAttachmentList() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input 	= request().body().asJson();
        double kikiId 	= Double.parseDouble(input.get("kikiId").asText());	//機器IDの取得

        /* 機器IDから機器情報を取得する */
        Kiki kiki 					= (Kiki)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.KIKI, "kiki_id = " + kikiId);
        if (kiki != null) {				//機器情報が取得できた場合
            String onUseAttachmentId	= kiki.onUseAttachmentId;

            if (onUseAttachmentId != null && !"".equals(onUseAttachmentId)) {
                String[] attachmentIds = onUseAttachmentId.split(",");
                List<Double> aryAttachmentId = new ArrayList<Double>();	//検索条件 アタッチメントID

                for (String attachmentId : attachmentIds) {		//検索条件を生成する
                    aryAttachmentId.add(Double.parseDouble(attachmentId));
                }

                /* アタッチメント情報一覧を取得する */
                List<Attachment> attachmentList = Attachment.find.where().in("attachment_id", aryAttachmentId).orderBy("attachment_id").findList();

                for (Attachment attachmentData : attachmentList) {							//機器情報をJSONデータに格納する

                    ObjectNode attachmentJson					= Json.newObject();
                    attachmentJson.put("attachmentId"			, attachmentData.attachmentId);		//アタッチメントID
                    attachmentJson.put("attachementName"		, attachmentData.attachementName);	//アタッチメント名

                    listJson.put(String.valueOf(attachmentData.attachmentId), attachmentJson);

                }

            }

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.ATTACHMENT, listJson);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】育苗記録を削除します
     * @return
     */
    public static Result ikubyoDiaryDelete(double ikubyoDiaryId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        models.IkubyoDiary ikubyoDiary = models.IkubyoDiary.find.where().eq("ikubyo_diary_id", ikubyoDiaryId).findUnique();

        if (ikubyoDiary != null) {

            IkubyoDiaryThread  idt  = new IkubyoDiaryThread();
            UserComprtnent uc = new UserComprtnent();
            int getAccount = uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
            Work wk = Work.getWork(ikubyoDiary.workId);

            ikubyoDiaryDeleteCommit(ikubyoDiaryId);

            Logger.info("[ IKUBYODIARY DELETED ] ID:{} NAME:{} NAENO:{} WORKID:{} WORKNAME:{} IKUBYODIARYID:{}", uc.accountData.accountId, uc.accountData.acountName, ikubyoDiary.naeNo, wk.workId, wk.workName, ikubyoDiaryId);

            idt.account   = uc.accountData;
            idt.ikubyoDiarys.add(ikubyoDiary);
            idt.start();
        }

        return ok(resultJson);
    }

    private static void ikubyoDiaryDeleteCommit(double ikubyoDiaryId) {

        /*-------------------------------------------------------------------*/
        /* 育苗記録、育苗散布、育苗ラインを削除する                          */
        /*-------------------------------------------------------------------*/
        models.IkubyoDiary id =  models.IkubyoDiary.getIkubyoDiaryById(ikubyoDiaryId);
        Ebean.createSqlUpdate("DELETE FROM ikubyo_diary WHERE ikubyo_diary_id = :ikubyoDiaryId")
        .setParameter("ikubyoDiaryId", ikubyoDiaryId).execute();

        Ebean.createSqlUpdate("DELETE FROM ikubyo_diary_sanpu WHERE ikubyo_diary_id = :ikubyoDiaryId")
        .setParameter("ikubyoDiaryId", ikubyoDiaryId).execute();

        Ebean.createSqlUpdate("DELETE FROM ikubyo_line WHERE ikubyo_diary_id = :ikubyoDiaryId")
        .setParameter("ikubyoDiaryId", ikubyoDiaryId).execute();

        Ebean.createSqlUpdate("DELETE FROM ikubyo_diary_detail WHERE ikubyo_diary_id = :ikubyoDiaryId")
        .setParameter("ikubyoDiaryId", ikubyoDiaryId).execute();

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONDELETE, String.format("[ikubyoDiaryId]=%f", ikubyoDiaryId));

    }

    /**
     * 【AICA】育苗計画の削除
     */
    private static Result ikubyoPlanDelete(double ikubyoPlanId) {

      ObjectNode resultJson   = Json.newObject();

      Ebean.createSqlUpdate("DELETE FROM ikubyo_plan WHERE ikubyo_plan_id = :ikubyoPlanId")
      .setParameter("ikubyoPlanId", ikubyoPlanId).execute();

      Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_sanpu WHERE ikubyo_plan_id = :ikubyoPlanId")
      .setParameter("ikubyoPlanId", ikubyoPlanId).execute();

      Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_line WHERE ikubyo_plan_id = :ikubyoPlanId")
      .setParameter("ikubyoPlanId", ikubyoPlanId).execute();

      Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_detail WHERE ikubyo_plan_id = :ikubyoPlanId")
      .setParameter("ikubyoPlanId", ikubyoPlanId).execute();

      Logger.info("[ IKUBYO PLAN DELETE ] PLANID:{}", ikubyoPlanId);
      resultJson.put("result" , AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    /**
     * 【AGRYEEL】農肥散布チェック
     * @return
     */
    public static Result checkNouhiIkubyo() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();
      /* JSONデータを取得 */
      JsonNode input = request().body().asJson();
      JsonNode nouhiInfoList = input.get("nouhiInfo");
      int result = 0x00;
      String message = "";
      java.util.Date workDate;
      try {
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
        workDate  = new java.util.Date(sdf.parse(input.get("workDate").asText().replace("/", "")).getTime());
      } catch (ParseException e) {
        Calendar cal = Calendar.getInstance();
        workDate  = new java.util.Date(cal.getTime().getTime());
      }
      if (nouhiInfoList != null && nouhiInfoList.size() > 0) {
        String sNaeNo = input.get("workNae").asText();                                    //複数苗同時作業記録
        String[] sNaeNos = sNaeNo.split(",");
        for (String naeNo : sNaeNos) {
          for (int nouhiIndex = 0; nouhiIndex < nouhiInfoList.size(); nouhiIndex++) {
            double  nouhiId   = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("nouhiId").asText());        //農肥IDを取得する
            param.NouhiCheckParm ncp = new param.NouhiCheckParm(nouhiId, 0, naeNo, workDate);
            result |= NouhiComprtnent.nouhiNaeCheck(ncp);
            if (!"".equals(message)) {
              message += "\n";
            }
            message += ncp.message;
          }
        }
      }
      resultJson.put(AgryeelConst.Result.RESULT, result);
      resultJson.put("message"          , message);
      return ok(resultJson);

    }
}
