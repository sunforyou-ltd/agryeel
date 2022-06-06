package controllers;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Account;
import models.Attachment;
import models.Belto;
import models.Common;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.FarmStatus;
import models.FieldGroup;
import models.Hinsyu;
import models.Kiki;
import models.MotochoBase;
import models.Nisugata;
import models.Nouhi;
import models.PlanLine;
import models.Sequence;
import models.Shitu;
import models.Size;
import models.TimeLine;
import models.Work;
import models.WorkChainItem;
import models.WorkDiary;
import models.WorkPlan;
import models.WorkPlanDetail;
import models.WorkPlanSanpu;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.DateU;
import util.StringU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.Analysis;
import compornent.FieldComprtnent;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;

import consts.AgryeelConst;

public class WorkPlanController extends Controller {

    /**
     * 【AICA】作業指示画面への遷移
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result move() {
        return ok(views.html.workplan.render());
    }
    /**
     * 【AICA】作業指示画面への遷移
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result accountmove() {
        return ok(views.html.workplanaccount.render());
    }
    /**
     * 【AICA】作業指示書の初期化
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result init() {
      ObjectNode resultJson   = Json.newObject();

      UserComprtnent ac = new UserComprtnent();
      ac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      String accountId = ac.accountStatusData.workPlanAccountId;
      String workId = ac.accountStatusData.workPlanWorkId;
      String kukakuId = ac.accountStatusData.workPlanKukakuId;
      Logger.info("INIT accountId={} workId={} kukakuId={}", accountId, workId, kukakuId);
      resultJson.put("accountId", accountId);
      resultJson.put("workId", workId);
      resultJson.put("kukakuId", kukakuId);

      return ok(resultJson);
    }
    /**
     * 【AICA】作業指示書の取得
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result getWorkPlan(String targetDate, String accountId, double crop) {
      return getWorkPlanS(targetDate, accountId, "", "", crop);
    }
    /**
     * 【AICA】作業指示書の取得
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result getWorkPlanS(String targetDate, String accountId, String workId, String kukakuId, double crop) {

      ObjectNode resultJson   = Json.newObject();
      ObjectNode dateList     = Json.newObject();
      ObjectNode workList     = Json.newObject();
      ObjectMapper mapper     = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonApi   = mapper.createArrayNode();
      boolean api             = false;

      //----------------------------------------------------------------------------
      //- 作業指示書を取得する
      //----------------------------------------------------------------------------
      SimpleDateFormat sdfp = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat sdff = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
      SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");
      SimpleDateFormat sdff2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

      Calendar system   = Calendar.getInstance();
      Calendar start    = Calendar.getInstance();
      Calendar end      = Calendar.getInstance();
      try {

        Calendar cal = Calendar.getInstance();
        long to   = cal.getTime().getTime();

        system.setTime(sdfp.parse(targetDate));
        int dIdx = 0;
        for (int i = 0; i < 7; i++) {
          dIdx++;
          ObjectNode dateJson = Json.newObject();
          dateJson.put("date"     , sdff.format(system.getTime()));
          dateJson.put("dtwk"     , DateU.getDayOfWeek(system));
          dateJson.put("keydate"  , sdfp.format(system.getTime()));
          dateList.put(String.valueOf(dIdx), dateJson);
          system.add(Calendar.DAY_OF_MONTH, 1);
        }

        start.setTime(sdfp.parse(targetDate));
        end.setTime(sdfp.parse(targetDate));

        if (session(AgryeelConst.SessionKey.API) != null) {
          api = true;
        }
        else {
          end.add(Calendar.DAY_OF_MONTH, 7);
        }

        UserComprtnent ac = new UserComprtnent();
        ac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        //検索条件を保存
        ac.accountStatusData.workPlanAccountId  = accountId;
        ac.accountStatusData.workPlanWorkId     = workId;
        ac.accountStatusData.workPlanKukakuId   = kukakuId;
        ac.accountStatusData.update();
        String sAccountId = AgryeelConst.SpecialAccount.ALLACOUNT;
        if (!accountId.equals("")) {
          sAccountId = accountId;
        }

        Logger.info("ACCOUNT=[{}]", accountId);
        List<PlanLine> pls = PlanLine.getPlanLineOfSearch(accountId, workId, kukakuId, ac.accountData.farmId, start.getTime(), end.getTime());
        int idx = 0;

        for (PlanLine planLineData : pls) {                                        //作業情報をJSONデータに格納する
          CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(planLineData.kukakuId);
          CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
          if (crop != 0 && crop != cws.cropId) {
            continue;
          }

          WorkPlan wp = WorkPlan.find.where().eq("work_plan_id", planLineData.workPlanId).findUnique();

          if (wp == null) { continue; }

          ObjectNode timeLineJson         = Json.newObject();
          timeLineJson.put("workPlanId"   , planLineData.workPlanId);                   //作業計画ID
          timeLineJson.put("workdate"     , sdf2.format(wp.workDate));                  //作業日
          timeLineJson.put("keydate"      , sdfp.format(wp.workDate));                  //キー作業日
          timeLineJson.put("message"      , StringU.setNullTrim(planLineData.message)); //メッセージ
          timeLineJson.put("timeLineColor", planLineData.planLineColor);                //タイムラインカラー
          timeLineJson.put("workName"     , planLineData.workName);                     //作業名
          timeLineJson.put("kukakuName"   , planLineData.kukakuName);                   //区画名
          timeLineJson.put("accountId"    , planLineData.accountId);                    //アカウントID
          timeLineJson.put("accountName"  , planLineData.accountName);                  //アカウント名
          timeLineJson.put("workId"       , planLineData.workId);                       //作業ＩＤ
          timeLineJson.put("worktime"     , wp.workTime);                               //作業時間
          timeLineJson.put("kukakuId"     , planLineData.kukakuId);                     //区画ＩＤ
          Compartment cpt = Compartment.getCompartmentInfo(planLineData.kukakuId);
          timeLineJson.put("fieldId"      , cpt.fieldId);                               //圃場ID
          timeLineJson.put("end"          , AgryeelConst.WORKPLANEND.WORKPLAN);         //作業完了フラグ
          timeLineJson.put("workPlanFlag" , planLineData.workPlanFlag);                 //作業計画フラグ
          timeLineJson.put("uuidofday"    , planLineData.uuidOfDay);                    //同一日内指示番号
          if (planLineData.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH
              || planLineData.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKPLANWATCH
              || planLineData.workPlanFlag == AgryeelConst.WORKPLANFLAG.AICAPLANWATCH) {//作業中の場合

            long from = wp.workStartTime.getTime();
            long diff = ( to - from  ) / (1000 * 60 );
            timeLineJson.put("worktime"     , diff);                                    //作業時間を書き換える

          }
          if(api){
            FieldGroup fg = cpt.getFieldGroupInfo();
            timeLineJson.put("kukakuColor", fg.fieldGroupColor);                        //区画カラー
          }

          idx++;
          workList.put(Double.toString(idx), timeLineJson);
          listJsonApi.add(timeLineJson);

        }

        List<TimeLine> tls = TimeLine.getTimeLineOfSearch(accountId, workId, kukakuId, ac.accountData.farmId, start.getTime(), end.getTime());

        for (TimeLine timeLineData : tls) {                                        //作業情報をJSONデータに格納する

          CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(timeLineData.kukakuId);
          CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
          if (crop != 0 && crop != cws.cropId) {
            continue;
          }

          WorkDiary wd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

          if (wd == null) { continue; }

          ObjectNode timeLineJson         = Json.newObject();
          timeLineJson.put("workPlanId"   , timeLineData.workDiaryId);                  //作業計画ID
          timeLineJson.put("workdate"     , sdf2.format(wd.workDate));                  //作業日
          timeLineJson.put("keydate"      , sdfp.format(wd.workDate));                  //キー作業日
          timeLineJson.put("message"      , StringU.setNullTrim(timeLineData.message)); //メッセージ
          timeLineJson.put("timeLineColor", timeLineData.timeLineColor);                //タイムラインカラー
          timeLineJson.put("workName"     , timeLineData.workName);                     //作業名
          timeLineJson.put("kukakuName"   , timeLineData.kukakuName);                   //区画名
          timeLineJson.put("accountId"    , timeLineData.accountId);                    //アカウントID
          timeLineJson.put("accountName"  , timeLineData.accountName);                  //アカウント名
          timeLineJson.put("workId"       , timeLineData.workId);                       //作業ＩＤ
          timeLineJson.put("worktime"     , wd.workTime);                               //作業時間
          timeLineJson.put("kukakuId"     , timeLineData.kukakuId);                     //区画ＩＤ
          Compartment cpt = Compartment.getCompartmentInfo(timeLineData.kukakuId);
          timeLineJson.put("fieldId"      , cpt.fieldId);                               //圃場ID
          timeLineJson.put("end"          , AgryeelConst.WORKPLANEND.END);              //作業完了フラグ
          timeLineJson.put("workPlanFlag" , timeLineData.workPlanFlag);                 //作業計画フラグ
          timeLineJson.put("uuidofday"    , timeLineData.uuidOfDay);                    //同一日内指示番号
          if(api){
            FieldGroup fg = cpt.getFieldGroupInfo();
            timeLineJson.put("kukakuColor", fg.fieldGroupColor);                        //区画カラー
          }

          idx++;
          workList.put(Double.toString(idx), timeLineJson);
          listJsonApi.add(timeLineJson);

        }

      } catch (ParseException e) {
        e.printStackTrace();
      }

      if(api){
        resultJson.put("planList", listJsonApi);
      }else{
        resultJson.put("dateList", dateList);
        resultJson.put("planList", workList);
      }
      return ok(resultJson);
    }
    /**
     * 【AICA】作業指示書(担当者別)の取得
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result getWorkPlanAccount(String targetDate, String accountId) {
      return getWorkPlanAccountS(targetDate, accountId, "", "");
    }
    /**
     * 【AICA】作業指示書(担当者別)の取得
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result getWorkPlanAccountS(String targetDate, String accountId, String workId, String kukakuId) {

      ObjectNode resultJson   = Json.newObject();
      ObjectNode accountList  = Json.newObject();
      ObjectNode workList     = Json.newObject();
      ObjectMapper mapper     = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonApi   = mapper.createArrayNode();
      boolean api             = false;

      //----------------------------------------------------------------------------
      //- 作業指示書を取得する
      //----------------------------------------------------------------------------
      SimpleDateFormat sdfp = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat sdff = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
      SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");

      Calendar system   = Calendar.getInstance();
      Calendar start    = Calendar.getInstance();
      Calendar end      = Calendar.getInstance();
      try {

        String[] accountsp = accountId.split(",");
        List<String> accountKeys = new ArrayList<String>();
        for (String key : accountsp) {
          accountKeys.add(key);
        }

        UserComprtnent ac = new UserComprtnent();
        ac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        List<Account> accounts = Account.find.where().eq("farm_id", ac.accountData.farmId).in("account_id", accountKeys).findList();

        Calendar cal = Calendar.getInstance();
        long to   = cal.getTime().getTime();

        int dIdx = 0;
        //------ 担当者未選択 -----
        dIdx++;
        ObjectNode accountJson = Json.newObject();
        accountJson.put("name"       , "担当者未選択");
        accountJson.put("keyaccount" , "");
        accountList.put(String.valueOf(dIdx), accountJson);
        //------ 担当者別-----
        for (Account account : accounts) {
          dIdx++;
          accountJson = Json.newObject();
          accountJson.put("name"       , account.acountName);
          accountJson.put("keyaccount" , account.accountId);
          accountList.put(String.valueOf(dIdx), accountJson);
        }

        start.setTime(sdfp.parse(targetDate));
        end.setTime(sdfp.parse(targetDate));

        if (session(AgryeelConst.SessionKey.API) != null) {
          api = true;
        }

        ac.accountStatusData.workPlanAccountId  = accountId;
        ac.accountStatusData.workPlanWorkId     = workId;
        ac.accountStatusData.workPlanKukakuId   = kukakuId;
        ac.accountStatusData.update();

        List<PlanLine> pls = PlanLine.getPlanLineOfSearch(accountId, workId, kukakuId, ac.accountData.farmId, start.getTime(), end.getTime());
        int idx = 0;

        for (PlanLine planLineData : pls) {                                        //作業情報をJSONデータに格納する

          WorkPlan wp = WorkPlan.find.where().eq("work_plan_id", planLineData.workPlanId).findUnique();

          if (wp == null) { continue; }

          ObjectNode timeLineJson         = Json.newObject();
          timeLineJson.put("workPlanId"   , planLineData.workPlanId);                   //作業計画ID
          timeLineJson.put("workdate"     , sdf2.format(wp.workDate));                  //作業日
          timeLineJson.put("keyaccount"   , wp.accountId);                              //キーアカウントID
          timeLineJson.put("message"      , StringU.setNullTrim(planLineData.message)); //メッセージ
          timeLineJson.put("timeLineColor", planLineData.planLineColor);                //タイムラインカラー
          timeLineJson.put("workName"     , planLineData.workName);                     //作業名
          timeLineJson.put("kukakuName"   , planLineData.kukakuName);                   //区画名
          timeLineJson.put("accountId"    , planLineData.accountId);                    //アカウントID
          timeLineJson.put("accountName"  , planLineData.accountName);                  //アカウント名
          timeLineJson.put("workId"       , planLineData.workId);                       //作業ＩＤ
          timeLineJson.put("worktime"     , wp.workTime);                               //作業時間
          timeLineJson.put("kukakuId"     , planLineData.kukakuId);                     //区画ＩＤ
          Compartment cpt = Compartment.getCompartmentInfo(planLineData.kukakuId);
          timeLineJson.put("fieldId"      , cpt.fieldId);                               //圃場ID
          timeLineJson.put("end"          , AgryeelConst.WORKPLANEND.WORKPLAN);         //作業完了フラグ
          timeLineJson.put("workPlanFlag" , planLineData.workPlanFlag);                 //作業計画フラグ
          timeLineJson.put("uuidofday"    , planLineData.uuidOfDay);                    //同一日内指示番号
          if (planLineData.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH
              || planLineData.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKPLANWATCH
              || planLineData.workPlanFlag == AgryeelConst.WORKPLANFLAG.AICAPLANWATCH) {//作業中の場合

            long from = wp.workStartTime.getTime();
            long diff = ( to - from  ) / (1000 * 60 );
            timeLineJson.put("worktime"     , diff);                                    //作業時間を書き換える

          }
          if(api){
            FieldGroup fg = cpt.getFieldGroupInfo();
            timeLineJson.put("kukakuColor", fg.fieldGroupColor);                        //区画カラー
          }

          idx++;
          workList.put(Double.toString(idx), timeLineJson);
          listJsonApi.add(timeLineJson);

        }

        List<TimeLine> tls = TimeLine.getTimeLineOfSearch(accountId, workId, kukakuId, ac.accountData.farmId, start.getTime(), end.getTime());

        for (TimeLine timeLineData : tls) {                                        //作業情報をJSONデータに格納する

          WorkDiary wd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

          if (wd == null) { continue; }

          ObjectNode timeLineJson         = Json.newObject();
          timeLineJson.put("workPlanId"   , timeLineData.workDiaryId);                  //作業計画ID
          timeLineJson.put("workdate"     , sdf2.format(wd.workDate));                  //作業日
          timeLineJson.put("keyaccount"   , timeLineData.accountId);                    //キーアカウントID
          timeLineJson.put("message"      , StringU.setNullTrim(timeLineData.message)); //メッセージ
          timeLineJson.put("timeLineColor", timeLineData.timeLineColor);                //タイムラインカラー
          timeLineJson.put("workName"     , timeLineData.workName);                     //作業名
          timeLineJson.put("kukakuName"   , timeLineData.kukakuName);                   //区画名
          timeLineJson.put("accountId"    , timeLineData.accountId);                    //アカウントID
          timeLineJson.put("accountName"  , timeLineData.accountName);                  //アカウント名
          timeLineJson.put("workId"       , timeLineData.workId);                       //作業ＩＤ
          timeLineJson.put("worktime"     , wd.workTime);                               //作業時間
          timeLineJson.put("kukakuId"     , timeLineData.kukakuId);                     //区画ＩＤ
          Compartment cpt = Compartment.getCompartmentInfo(timeLineData.kukakuId);
          timeLineJson.put("fieldId"      , cpt.fieldId);                               //圃場ID
          timeLineJson.put("end"          , AgryeelConst.WORKPLANEND.END);              //作業完了フラグ
          timeLineJson.put("workPlanFlag" , timeLineData.workPlanFlag);                 //作業計画フラグ
          timeLineJson.put("uuidofday"    , timeLineData.uuidOfDay);                    //同一日内指示番号
          if(api){
            FieldGroup fg = cpt.getFieldGroupInfo();
            timeLineJson.put("kukakuColor", fg.fieldGroupColor);                        //区画カラー
          }

          idx++;
          workList.put(Double.toString(idx), timeLineJson);
          listJsonApi.add(timeLineJson);

        }

      } catch (ParseException e) {
        e.printStackTrace();
      }

      if(api){
        resultJson.put("planList", listJsonApi);
      }else{
        resultJson.put("accountList", accountList);
        resultJson.put("planList", workList);
      }
      return ok(resultJson);
    }
    /**
     * 【AICA】作業指示書の作業開始
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result startWorkPlan(double workPlanId) {

      ObjectNode resultJson   = Json.newObject();
      SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
      SimpleDateFormat sdfp = new SimpleDateFormat("yyyyMMdd");

      //----------------------------------------------------------------------------
      //- 作業指示書を作業開始の状態とする
      //----------------------------------------------------------------------------
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
      Account ac = accountComprtnent.accountData;

      WorkPlan wp = WorkPlan.getWorkPlanById(workPlanId);

      if (wp != null) {

        Calendar system = Calendar.getInstance();
        Compartment compartment = Compartment.find.where().eq("kukaku_id", wp.kukakuId).findUnique();   //区画情報モデルの取得

        if ((wp.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKPLANCOMMIT) || (wp.workPlanFlag == AgryeelConst.WORKPLANFLAG.AICAPLANCOMMIT)) {
          wp.workDate = new java.sql.Date(system.getTimeInMillis());
          wp.workStartTime = new java.sql.Timestamp(system.getTimeInMillis());
          if ((wp.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKPLANCOMMIT)) {
            wp.workPlanFlag = AgryeelConst.WORKPLANFLAG.WORKPLANWATCH;
          }
          else {
            wp.workPlanFlag = AgryeelConst.WORKPLANFLAG.AICAPLANWATCH;
          }
          wp.update();
          PlanLine pl = PlanLine.find.where().eq("work_plan_id", workPlanId).findUnique();
          if (pl != null) {
            if ((pl.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKPLANCOMMIT) || (pl.workPlanFlag == AgryeelConst.WORKPLANFLAG.AICAPLANCOMMIT)) {
              if (wp.workStartTime != null) {
                pl.message += "【 開始時間 】 " + sdf.format(wp.workStartTime);
              }
              pl.workDate = wp.workDate;
              pl.workPlanFlag = wp.workPlanFlag;
              pl.update();
              resultJson.put("keydate", sdfp.format(pl.workDate));                  //キー作業日
              resultJson.put("message"  , pl.message);
            }
          }
        }


        //--インスタンスメソッド化
//        ac.workId         = wp.workId;
//        ac.fieldId        = compartment.kukakuId;
//        ac.workStartTime  = new java.sql.Timestamp(system.getTimeInMillis());
//        ac.workPlanId     = wp.workPlanId;
        ac.setWorkingInfo(wp.workId, compartment.kukakuId, DateU.getSystemTimeStamp(), wp.workPlanId);
        ac.update();

        Compartment ct = Compartment.getCompartmentInfo(ac.fieldId);
        Work        wk = Work.getWork(ac.workId);

        Logger.info("[ WORKING START ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{} STARTTIME:{}", ac.accountId, ac.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName, sdf.format(ac.workStartTime));

        session(AgryeelConst.SessionKey.WORKING_ACTION, "display");
        accountComprtnent.getAccountJson(resultJson);
      }
      else {

      }

      return ok(resultJson);
    }
    /**
     * 【AICA】作業指示書の担当者変更
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result tantouChange(double workPlanId, String accountId) {

      ObjectNode resultJson   = Json.newObject();

      WorkPlan wp = WorkPlan.getWorkPlanById(workPlanId);

      if (wp != null) {
        wp.accountId = accountId;
        wp.update();

        PlanLine pl = PlanLine.find.where().eq("work_plan_id", workPlanId).findUnique();
        if (pl != null) {

          Account ac = Account.getAccount(accountId);

          pl.accountId    = accountId;
          pl.accountName  = ac.acountName;
          pl.update();

          Logger.info("[ WORK PLAN TANTOU CHANGE ] ID:{} NAME:{}", ac.accountId, ac.acountName);
          resultJson.put("id"     , pl.accountId);
          resultJson.put("name"   , pl.accountName);
          resultJson.put("result" , AgryeelConst.Result.SUCCESS);

        }
        else {
          resultJson.put("result" , AgryeelConst.Result.ERROR);
        }
      }
      else {
        resultJson.put("result" , AgryeelConst.Result.ERROR);
      }

      return ok(resultJson);
    }
    /**
     * 【AICA】作業指示書の作業日を変更する
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result workDateChange(double workPlanId, String workDate) {

      ObjectNode resultJson   = Json.newObject();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      Calendar cal = Calendar.getInstance();

      WorkPlan wp = WorkPlan.getWorkPlanById(workPlanId);

      if (wp != null) {
        try {
          cal.setTime(sdf.parse((workDate)));
        } catch (ParseException e) {
          e.printStackTrace();
        }
        wp.workDate = new java.sql.Date(cal.getTimeInMillis());
        wp.update();

        PlanLine pl = PlanLine.find.where().eq("work_plan_id", workPlanId).findUnique();
        if (pl != null) {

          pl.workDate = wp.workDate;
          pl.update();

          Logger.info("[ WORK PLAN WORKDATE CHANGE ] DATE:{}", sdf.format(pl.workDate));
          resultJson.put("result" , AgryeelConst.Result.SUCCESS);

        }
        else {
          resultJson.put("result" , AgryeelConst.Result.ERROR);
        }
      }
      else {
        resultJson.put("result" , AgryeelConst.Result.ERROR);
      }

      return ok(resultJson);
    }
    /**
     * 【AICA】作業指示書の削除
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result workPlanDelete(double workPlanId) {

      ObjectNode resultJson   = Json.newObject();

      Ebean.createSqlUpdate("DELETE FROM work_plan WHERE work_plan_id = :workPlanId")
      .setParameter("workPlanId", workPlanId).execute();

      Ebean.createSqlUpdate("DELETE FROM work_plan_sanpu WHERE work_plan_id = :workPlanId")
      .setParameter("workPlanId", workPlanId).execute();

      Ebean.createSqlUpdate("DELETE FROM plan_line WHERE work_plan_id = :workPlanId")
      .setParameter("workPlanId", workPlanId).execute();

      Ebean.createSqlUpdate("DELETE FROM work_plan_detail WHERE work_plan_id = :workPlanId")
      .setParameter("workPlanId", workPlanId).execute();

      Logger.info("[ WORK PLAN DELETE ] PLANID:{}", workPlanId);
      resultJson.put("result" , AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result workPlanCopy(double workPlanId, double kukakuId) {
      ObjectNode resultJson = Json.newObject();

      //----------------------------------------------------------------------------
      //- 目標収穫量の更新
      //----------------------------------------------------------------------------
      JsonNode input = request().body().asJson();
      SimpleDateFormat sdfp = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat sdfu = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      DecimalFormat    df   = new DecimalFormat("000000");
      DecimalFormat    df2  = new DecimalFormat("#,##0.00");


      try {
        Ebean.beginTransaction();

        UserComprtnent ac = new UserComprtnent();
        ac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        Calendar system = Calendar.getInstance();

        WorkPlan wp = WorkPlan.getWorkPlanById(workPlanId);
        if (wp != null) {
          //----- 作業計画の自動作成 -----
          WorkPlan workplan = new WorkPlan();
          Sequence sequence     = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKPLANID); //最新シーケンス値の取得
          double nworkPlanId    = sequence.sequenceValue;
          workplan.workPlanId          = nworkPlanId;
          workplan.workId              = wp.workId;
          workplan.kukakuId            = kukakuId;
          workplan.hillId              = wp.hillId;
          workplan.lineId              = wp.lineId;
          workplan.stockId             = wp.stockId;
          workplan.accountId           = "";
          workplan.workDate            = wp.workDate;
          workplan.workTime            = wp.workTime;
          workplan.shukakuRyo          = wp.shukakuRyo;
          workplan.detailSettingKind   = wp.detailSettingKind;
          workplan.combiId             = wp.combiId;
          workplan.kikiId              = wp.kikiId;
          workplan.attachmentId        = wp.attachmentId;
          workplan.hinsyuId            = wp.hinsyuId;
          workplan.beltoId             = wp.beltoId;
          workplan.kabuma              = wp.kabuma;
          workplan.joukan              = wp.joukan;
          workplan.jousu               = wp.jousu;
          workplan.hukasa              = wp.hukasa;
          workplan.kansuiPart          = wp.kansuiPart;
          workplan.kansuiSpace         = wp.kansuiSpace;
          workplan.kansuiMethod        = wp.kansuiMethod;
          workplan.kansuiRyo           = wp.kansuiRyo;
          workplan.syukakuNisugata     = wp.syukakuNisugata;
          workplan.syukakuSitsu        = wp.syukakuSitsu;
          workplan.syukakuSize         = wp.syukakuSize;
          workplan.kukakuStatusUpdate  = wp.kukakuStatusUpdate;
          workplan.motochoUpdate       = wp.motochoUpdate;
          workplan.workRemark          = wp.workRemark;
          workplan.workStartTime       = wp.workStartTime;
          workplan.workEndTime         = wp.workEndTime;
          workplan.numberOfSteps       = 0;
          workplan.distance            = 0;
          workplan.calorie             = 0;
          workplan.heartRate           = 0;
          workplan.useMulti            = wp.useMulti;
          workplan.retusu              = wp.retusu;
          workplan.naemaisu            = wp.naemaisu;
          workplan.useHole             = wp.useHole;
          workplan.maisu               = wp.maisu;
          workplan.useBaido            = wp.useBaido;
          workplan.senteiHeight        = wp.senteiHeight;
          workplan.workPlanFlag        = AgryeelConst.WORKPLANFLAG.WORKPLANCOMMIT;
          workplan.workPlanUUID        = df.format(ac.accountData.farmId) + sdfu.format(system.getTime()) + ac.accountData.accountId;
          workplan.shitateHonsu        = wp.shitateHonsu;
          workplan.nicho               = wp.nicho;
          workplan.haikiRyo            = wp.haikiRyo;

          workplan.save();

          //----- 作業散布計画の自動作成 -----
          List<models.WorkPlanSanpu> wpss = models.WorkPlanSanpu.find.where().eq("work_plan_id", wp.workPlanId).orderBy("work_diary_sequence asc").findList();
          int workDiarySequence = 0;
          for (models.WorkPlanSanpu wps : wpss) {
            models.WorkPlanSanpu nwps = new WorkPlanSanpu();

            workDiarySequence++;
            nwps.workPlanId          = nworkPlanId;
            nwps.workDiarySequence   = workDiarySequence;
            nwps.sanpuMethod         = wps.sanpuMethod;
            nwps.kikiId              = wps.kikiId;
            nwps.attachmentId        = wps.attachmentId;
            nwps.nouhiId             = wps.nouhiId;
            nwps.bairitu             = wps.bairitu;
            nwps.sanpuryo            = wps.sanpuryo;
            nwps.kukakuStatusUpdate  = wps.kukakuStatusUpdate;
            nwps.motochoUpdate       = wps.motochoUpdate;
            nwps.save();
          }

          //----- 作業詳細計画の自動作成 -----
          List<models.WorkPlanDetail> wpds = models.WorkPlanDetail.find.where().eq("work_plan_id", wp.workPlanId).orderBy("work_diary_sequence asc").findList();
          workDiarySequence = 0;
          for (models.WorkPlanDetail wpd : wpds) {
            models.WorkPlanDetail nwpd = new WorkPlanDetail();

            workDiarySequence++;
            nwpd.workPlanId          = nworkPlanId;
            nwpd.workDiarySequence   = workDiarySequence;
            nwpd.workDetailKind      = wpd.workDetailKind;
            nwpd.suryo               = wpd.suryo;
            nwpd.sizaiId             = wpd.sizaiId;
            nwpd.comment             = wpd.comment;
            nwpd.syukakuNisugata     = wpd.syukakuNisugata;
            nwpd.syukakuSitsu        = wpd.syukakuSitsu;
            nwpd.syukakuSize         = wpd.syukakuSize;
            nwpd.syukakuKosu         = wpd.syukakuKosu;
            nwpd.shukakuRyo          = wpd.shukakuRyo;
            nwpd.syukakuHakosu       = wpd.syukakuHakosu;
            nwpd.syukakuNinzu        = wpd.syukakuNinzu;
            nwpd.save();
          }

          /* -- プランラインを作成する -- */
          Work work = Work.find.where().eq("work_id", workplan.workId).findUnique();                       //作業情報モデルの取得
          PlanLine planLine = new PlanLine();                                       //タイムラインモデルの生成
          Compartment compartment = Compartment.find.where().eq("kukaku_id", workplan.kukakuId).findUnique();   //区画情報モデルの取得

          planLine.workPlanId = workplan.workPlanId;

          planLine.updateTime       = DateU.getSystemTimeStamp();
          planLine.message        = "【" + work.workName + "情報】<br>";
          switch ((int)work.workTemplateId) {
          case AgryeelConst.WorkTemplate.NOMAL:
            planLine.message        += "";                                 //メッセージ
            break;
          case AgryeelConst.WorkTemplate.SANPU:

            List<WorkPlanSanpu> wpssp = WorkPlanSanpu.getWorkPlanSanpuList(workplan.workPlanId);

            for (WorkPlanSanpu wps : wpssp) {

              /* 農肥IDから農肥情報を取得する */
              Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  wps.nouhiId).findUnique();

              String sanpuName  = "";

              if (wps.sanpuMethod != 0) {
                  sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, wps.sanpuMethod) + "]";
              }

              String unit = nouhi.getUnitString();

              planLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df2.format(wps.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
              planLine.message       +=  "--------------------------------------------------<br>";

            }

            break;
          case AgryeelConst.WorkTemplate.HASHU:
            planLine.message       += "<品種> " + Hinsyu.getMultiHinsyuName(workplan.hinsyuId) + "<br>";
            planLine.message       += "<株間> " + workplan.kabuma + "cm<br>";
            planLine.message       += "<条間> " + workplan.joukan + "cm<br>";
            planLine.message       += "<条数> " + workplan.jousu  + "cm<br>";
            planLine.message       += "<深さ> " + workplan.hukasa + "cm<br>";
            planLine.message       += "<機器> " + Kiki.getKikiName(workplan.kikiId) + "<br>";
            planLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workplan.attachmentId) + "<br>";
            planLine.message       += "<ベルト> " + Belto.getBeltoName(workplan.beltoId) + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.SHUKAKU:
            List<WorkPlanDetail> wpdsp = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
            int idx = 0;
            for (WorkPlanDetail wpd : wpdsp) {
              if (wpd.shukakuRyo == 0) {
                continue;
              }
              idx++;
              planLine.message       += "<荷姿" + idx + "> "     + Nisugata.getNisugataName(wpd.syukakuNisugata) + "<br>";
              planLine.message       += "<質" + idx + "> "       + Shitu.getShituName(wpd.syukakuSitsu) + "<br>";
              planLine.message       += "<サイズ" + idx + "> "   + Size.getSizeName(wpd.syukakuSize) + "<br>";
              planLine.message       += "<個数" + idx + "> "   + wpd.syukakuKosu + "個" + "<br>";
              planLine.message       += "<収穫量" + idx + "> "   + wpd.shukakuRyo + "Kg" + "<br>";
              planLine.message       +=  "--------------------------------------------------<br>";

            }
            if (idx == 0) {
              planLine.message       += "<荷姿> "     + Nisugata.getNisugataName(workplan.syukakuNisugata) + "<br>";
              planLine.message       += "<質> "       + Shitu.getShituName(workplan.syukakuSitsu) + "<br>";
              planLine.message       += "<サイズ> "   + Size.getSizeName(workplan.syukakuSize) + "<br>";
              planLine.message       += "<収穫量> "   + workplan.shukakuRyo + "Kg" + "<br>";
              planLine.message       +=  "--------------------------------------------------<br>";
            }
            break;
          case AgryeelConst.WorkTemplate.NOUKO:
            planLine.message       += "<機器> " + Kiki.getKikiName(workplan.kikiId) + "<br>";
            planLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workplan.attachmentId) + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.KANSUI:
            planLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, workplan.kansuiMethod) + "<br>";
            planLine.message       += "<機器> " + Kiki.getKikiName(workplan.kikiId) + "<br>";
            planLine.message       += "<潅水量> " + workplan.kansuiRyo + "L" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.KAISHU:
            wpds = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wpd : wpds) {
              idx++;
              planLine.message        +=  "<数量" + idx + ">" + "&nbsp;&nbsp;" + wpd.suryo + "個<br>";

            }
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.DACHAKU:
            wpds = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wpd : wpds) {
              idx++;
              planLine.message        +=  "<資材" + idx + ">" + "&nbsp;&nbsp;" + Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)wpd.sizaiId, true) + "<br>";

            }
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.COMMENT:
            wpds = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wpd : wpds) {
              idx++;
              planLine.message        +=  "<コメント" + idx + ">" + "&nbsp;&nbsp;" + wpd.comment + "<br>";

            }
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.MALTI:
            planLine.message       += "<使用マルチ> " + Common.GetCommonValue(Common.ConstClass.ITOMULTI, (int)workplan.useMulti, true) + "<br>";
            planLine.message       += "<列数> " + workplan.retusu + "列" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.TEISHOKU:
            planLine.message       += "<使用苗枚数> " + workplan.naemaisu + "枚" + "<br>";
            planLine.message       += "<列数> " + workplan.retusu + "列" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.NAEHASHU:
            planLine.message       += "<使用穴数> " + workplan.useHole + "穴" + "<br>";
            planLine.message       += "<枚数> " + workplan.maisu + "枚" + "<br>";
            planLine.message       += "<使用培土> " + Common.GetCommonValue(Common.ConstClass.ITOBAIDO, (int)workplan.useBaido, true) + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.SENTEI:
            planLine.message       += "<剪定高> " + workplan.senteiHeight + "cm" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.MABIKI:
            planLine.message       += "<仕立本数> " + workplan.shitateHonsu + "本" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.NICHOCHOSEI:
            planLine.message       += "<日長> " + workplan.nicho + "時間" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.SENKA:
            wpdsp = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wpd : wpdsp) {
              if (wpd.syukakuKosu == 0 && wpd.syukakuHakosu == 0) {
                continue;
              }
              idx++;
              planLine.message       += "<等級" + idx + "> "       + Shitu.getShituName(wpd.syukakuSitsu) + "<br>";
              planLine.message       += "<階級" + idx + "> "   + Size.getSizeName(wpd.syukakuSize) + "<br>";
              planLine.message       += "<箱数" + idx + "> "   + wpd.syukakuHakosu + "ケース" + "<br>";
              planLine.message       += "<本数" + idx + "> "   + wpd.syukakuKosu + "本" + "<br>";
              planLine.message       += "<人数" + idx + "> "   + wpd.syukakuNinzu + "人" + "<br>";
              planLine.message       += "<収穫量" + idx + "> " + wpd.shukakuRyo + "Kg" + "<br>";
              planLine.message       +=  "--------------------------------------------------<br>";

            }
            break;
          case AgryeelConst.WorkTemplate.HAIKI:
            planLine.message       += "<廃棄量> " + workplan.haikiRyo + "Kg" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          }

          planLine.planLineColor  = work.workColor;                             //プランラインカラー
          planLine.workId         = work.workId;                                    //作業ID
          planLine.workName       = work.workName;                                  //作業名
          planLine.workDate       = workplan.workDate;                              //作業日
          planLine.kukakuId       = compartment.kukakuId;                           //区画ID
          planLine.kukakuName     = compartment.kukakuName;                         //区画名
          planLine.accountId      = "";                                             //アカウントＩＤ
          planLine.accountName    = "担当者未選択";                                    //アカウント名
          planLine.farmId         = ac.accountData.farmId;                          //農場ID
          planLine.workPlanFlag   = workplan.workPlanFlag;                          //作業計画フラグ
          planLine.workPlanUUID   = workplan.workPlanUUID;                          //作業計画UUID

          planLine.save();                                                          //タイムラインを追加

        }

        Ebean.commitTransaction();
        resultJson.put("result"   , "SUCCESS");
      }
      catch (Exception ex) {
        Logger.error("[commitPlanWork] Save Error.", ex);
        ex.printStackTrace();
        Ebean.rollbackTransaction();
        resultJson.put("result"   , "ERROR");
      }


      return ok(resultJson);
    }
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result workPlanUuidOnDayChange() {

      ObjectNode  resultJson = Json.newObject();

      try {
        Ebean.beginTransaction();
        JsonNode input = request().body().asJson();
        JsonNode datalist = input.get("dataList");

        for (JsonNode info : datalist) {
          double id = info.get("id").asDouble();
          int uuid  = info.get("uuid").asInt();
          int type  = info.get("type").asInt();

          if (type == 0) { //Planの場合
            PlanLine pl = PlanLine.find.where().eq("work_plan_id", id).findUnique();
            if (pl != null) {
              pl.uuidOfDay = uuid;
              pl.update();
              WorkPlan wp = WorkPlan.getWorkPlanById(id);
              if (wp != null) {
                wp.uuidOfDay = uuid;
                wp.update();
              }
            }
          }
          else { //Diaryの場合
            TimeLine tl = TimeLine.find.where().eq("work_diary_id", id).findUnique();
            if (tl != null) {
              tl.uuidOfDay = uuid;
              tl.update();
              WorkDiary wd = WorkDiary.getWorkDiaryById(id);
              if (wd != null) {
                wd.uuidOfDay = uuid;
                wd.update();
              }
            }
          }

        }

        Ebean.commitTransaction();
      }
      catch (Exception ex) {
        Logger.error("[workPlanUuidOnDayChange] Update Error.", ex);
        ex.printStackTrace();
        Ebean.rollbackTransaction();
      }

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result makeWorkPlanChain(double workChainId, String kukakuIds, String accountId, String planDate, String copyCount) {
      ObjectNode resultJson = Json.newObject();
      SimpleDateFormat sdfu = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      SimpleDateFormat sdfp = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd");
      DecimalFormat    df   = new DecimalFormat("000000");
      DecimalFormat    df2  = new DecimalFormat("#,##0.00");
      Ebean.beginTransaction();
      try {

        Calendar system = Calendar.getInstance();
        system.setTime(sdfp.parse(planDate));
        java.sql.Date systemdate = new Date(system.getTimeInMillis());

        int count = Integer.parseInt(copyCount);

        Logger.info("***** makeWorkPlanChain START WorkChain[{}] KukakuId[{}] AccountId[{}] PlanDate[{}] CopyCount[{}]*****", workChainId, kukakuIds, accountId, planDate, copyCount);

        List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList( workChainId, "next_sequence_id desc, sequence_id desc");

        Logger.info(">>>> WorkChainItem ID[{}] COUNT[{}].", workChainId, wcis.size());

        if (wcis.size() == 0) { /* ワークチェーン対象が存在しない場合 */
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
          return ok(resultJson);
        }

        String sAccountId   = "";
        String sAccountName = "担当者未選択";

        if(!"NONE".equals(accountId)) {
          Account ac = Account.getAccount(accountId);
          if(ac == null) {
            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
            return ok(resultJson);
          }
          sAccountId = ac.accountId;
          sAccountName = ac.acountName;
        }

        String kids[] = kukakuIds.split(",");

        for (String kid : kids) {
        	double kukakuId = Double.parseDouble(kid);
            UserComprtnent ac = new UserComprtnent();
            ac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
            FarmStatus fs = FarmStatus.getFarmStatus(ac.accountData.farmId);
            List<CompartmentStatus> css = CompartmentStatus.getStatusOfCompartment(kukakuId);
            CompartmentStatus cs = css.get(0);
            CompartmentWorkChainStatus cwcs = CompartmentWorkChainStatus.find.where().eq("kukaku_id", kukakuId).findUnique();

            for (int i = 0; i< count; i++) {
              int targetId = -1;
              Analysis als = new Analysis();
              for (WorkChainItem wci : wcis) {
                Logger.info(">>>> SequenceId[{}] WorkId[{}] Next[{}].", wci.sequenceId, wci.workId, wci.nextSequenceId);
                if (targetId != -1 && wci.nextSequenceId == 0) {
                  Logger.info(">>>> Skip.");
                  continue;
                }
                targetId = wci.nextSequenceId;
                WorkChainItem wcit = WorkChainItem.getWorkChainItemOfSeq(workChainId, targetId);

                Work work = Work.find.where().eq("work_id", wcit.workId).findUnique();                       //作業情報モデルの取得
                if (work == null) { //作業モデルが取得できていない場合
                  Logger.info(">>>> NOT EXISTS Work Skip.");
                  continue;
                }
                if (work.workTemplateId == AgryeelConst.WorkTemplate.END
                    || work.workTemplateId == AgryeelConst.WorkTemplate.SAIBAIKAISI) {  //作付開始と栽培開始は除外
                  Logger.info(">>>> END Work Skip.");
                  continue;
                }
                if (als.check(targetId)) { //作成済みの場合
                  Logger.info(">>>> EXISTS MAKE Skip.");
                  continue;
                }
                als.add(targetId);
                //履歴値参照方法により取得方法を変更する
                List<WorkDiary> wds = new ArrayList<WorkDiary>();
                switch (fs.historyReference) {
                case AgryeelConst.HISTRYREFERENCE.SAMECOUNT://前年同一回数
                  Logger.info(">>>>> TYPE:{} YEAR:{} ROTATION:{}", "SAMECOUNT", cs.workYear - 1, cs.rotationSpeedOfYear);
                  MotochoBase mb = MotochoBase.find.where().eq("kukaku_id", kukakuId).eq("work_year", cs.workYear - 1).eq("rotation_speed_of_year", cs.rotationSpeedOfYear).findUnique();
                  if (mb != null) {
                    wds = WorkDiary.find.where().eq("work_id", wcit.workId).eq("kukaku_id", kukakuId).between("work_date", mb.workStartDay, mb.workEndDay).orderBy("work_date desc").findList();
                  }
                  break;

                case AgryeelConst.HISTRYREFERENCE.SAMESEASON://前年同一時期
                  Calendar start  = Calendar.getInstance();
                  Calendar end    = Calendar.getInstance();
                  start.add(Calendar.MONTH, -1);
                  end.add(Calendar.MONTH, 1);
                  java.sql.Date dStart  = new Date(start.getTimeInMillis());
                  java.sql.Date dEnd    = new Date(end.getTimeInMillis());

                  Logger.info(">>>>> TYPE:{} START:{} END:{}", "SAMESEASON", sdf.format(dStart), sdf.format(dEnd));
                  List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", kukakuId).eq("work_year", cs.workYear - 1).between("hashu_date", dStart, dEnd).orderBy("rotation_speed_of_year desc").findList();
                  if (mbs.size() > 0) {
                    wds = WorkDiary.find.where().eq("work_id", wcit.workId).eq("kukaku_id", kukakuId).between("work_date", mbs.get(0).workStartDay, mbs.get(0).workEndDay).orderBy("work_date desc").findList();
                  }
                  break;

                case AgryeelConst.HISTRYREFERENCE.SAMEKUKAKU://前回同一区画
                  Logger.info(">>>>> TYPE:{}", "SAMEKUKAKU");
                  wds = WorkDiary.find.where().eq("work_id", wcit.workId).eq("kukaku_id", kukakuId).orderBy("work_date desc").findList();
                  break;
                default:
                  List<CompartmentWorkChainStatus> twcis = CompartmentWorkChainStatus.find.where().eq("farm_id", ac.accountData.farmId).eq("crop_id", cwcs.cropId).orderBy("kukaku_id").findList();
                  List<Double> kukakuKey = new ArrayList<Double>();
                  for (CompartmentWorkChainStatus twci :twcis) {
                    kukakuKey.add(twci.kukakuId);
                  }
                  Logger.info(">>>>> TYPE:{} CROP:{} KUKAKUCOUNT{}", "SAMEKUKAKU", cwcs.cropId, kukakuKey.size());
                  wds = WorkDiary.find.where().eq("work_id", wcit.workId).in("kukaku_id", kukakuKey).orderBy("work_date desc").findList();
                  break;
                }
                if (wds.size() == 0) {
                  Logger.info(">>>> NOT EXISTS HISTRY DARA NEW MAKE.");
                  WorkDiary wd = new WorkDiary();
                  wd.workId = wcit.workId;
                  wds.add(wd);
                }
                for (WorkDiary wd : wds) {

                    if (wd != null) {
                      //----- 作業計画の自動作成 -----
                      Logger.info(">>>> BASE WorkDiary ID:{} DATE:{} WORK:{} KUKAKU:{}.", wd.workDiaryId, wd.workDate, wd.workId, wd.kukakuId);
                      WorkPlan workplan = new WorkPlan();
                      Sequence sequence     = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKPLANID); //最新シーケンス値の取得
                      double nworkPlanId    = sequence.sequenceValue;
                      workplan.workPlanId          = nworkPlanId;
                      workplan.workId              = wd.workId;
                      workplan.kukakuId            = kukakuId;
                      workplan.hillId              = wd.hillId;
                      workplan.lineId              = wd.lineId;
                      workplan.stockId             = wd.stockId;
                      workplan.accountId           = sAccountId;
                      workplan.workDate            = systemdate;
                      workplan.workTime            = wd.workTime;
                      workplan.shukakuRyo          = wd.shukakuRyo;
                      workplan.detailSettingKind   = wd.detailSettingKind;
                      workplan.combiId             = wd.combiId;
                      workplan.kikiId              = wd.kikiId;
                      workplan.attachmentId        = wd.attachmentId;
                      workplan.hinsyuId            = wd.hinsyuId;
                      workplan.beltoId             = wd.beltoId;
                      workplan.kabuma              = wd.kabuma;
                      workplan.joukan              = wd.joukan;
                      workplan.jousu               = wd.jousu;
                      workplan.hukasa              = wd.hukasa;
                      workplan.kansuiPart          = wd.kansuiPart;
                      workplan.kansuiSpace         = wd.kansuiSpace;
                      workplan.kansuiMethod        = wd.kansuiMethod;
                      workplan.kansuiRyo           = wd.kansuiRyo;
                      workplan.syukakuNisugata     = wd.syukakuNisugata;
                      workplan.syukakuSitsu        = wd.syukakuSitsu;
                      workplan.syukakuSize         = wd.syukakuSize;
                      workplan.kukakuStatusUpdate  = wd.kukakuStatusUpdate;
                      workplan.motochoUpdate       = wd.motochoUpdate;
                      workplan.workRemark          = wd.workRemark;
                      workplan.workStartTime       = wd.workStartTime;
                      workplan.workEndTime         = wd.workEndTime;
                      workplan.numberOfSteps       = 0;
                      workplan.distance            = 0;
                      workplan.calorie             = 0;
                      workplan.heartRate           = 0;
                      workplan.useMulti            = wd.useMulti;
                      workplan.retusu              = wd.retusu;
                      workplan.naemaisu            = wd.naemaisu;
                      workplan.useHole             = wd.useHole;
                      workplan.maisu               = wd.maisu;
                      workplan.useBaido            = wd.useBaido;
                      workplan.senteiHeight        = wd.senteiHeight;
                      workplan.workPlanFlag        = AgryeelConst.WORKPLANFLAG.WORKPLANCOMMIT;
                      workplan.workPlanUUID        = df.format(ac.accountData.farmId) + sdfu.format(system.getTime()) + ac.accountData.accountId;
                      workplan.shitateHonsu        = wd.shitateHonsu;
                      workplan.nicho               = wd.nicho;
                      workplan.haikiRyo            = wd.haikiRyo;

                      workplan.save();

                      //----- 作業散布計画の自動作成 -----
                      List<models.WorkDiarySanpu> wdss = models.WorkDiarySanpu.find.where().eq("work_diary_id", wd.workDiaryId).orderBy("work_diary_sequence asc").findList();
                      int workDiarySequence = 0;
                      for (models.WorkDiarySanpu wdsp : wdss) {
                        models.WorkPlanSanpu nwps = new WorkPlanSanpu();

                        workDiarySequence++;
                        nwps.workPlanId          = nworkPlanId;
                        nwps.workDiarySequence   = workDiarySequence;
                        nwps.sanpuMethod         = wdsp.sanpuMethod;
                        nwps.kikiId              = wdsp.kikiId;
                        nwps.attachmentId        = wdsp.attachmentId;
                        nwps.nouhiId             = wdsp.nouhiId;
                        nwps.bairitu             = wdsp.bairitu;
                        nwps.sanpuryo            = wdsp.sanpuryo;
                        nwps.kukakuStatusUpdate  = wdsp.kukakuStatusUpdate;
                        nwps.motochoUpdate       = wdsp.motochoUpdate;
                        nwps.save();
                      }

                      //----- 作業詳細計画の自動作成 -----
                      List<models.WorkDiaryDetail> wdds = models.WorkDiaryDetail.find.where().eq("work_diary_id", wd.workDiaryId).orderBy("work_diary_sequence asc").findList();
                      workDiarySequence = 0;
                      for (models.WorkDiaryDetail wdd : wdds) {
                        models.WorkPlanDetail nwpd = new WorkPlanDetail();

                        workDiarySequence++;
                        nwpd.workPlanId          = nworkPlanId;
                        nwpd.workDiarySequence   = workDiarySequence;
                        nwpd.workDetailKind      = wdd.workDetailKind;
                        nwpd.suryo               = wdd.suryo;
                        nwpd.sizaiId             = wdd.sizaiId;
                        nwpd.comment             = wdd.comment;
                        nwpd.syukakuNisugata     = wdd.syukakuNisugata;
                        nwpd.syukakuSitsu        = wdd.syukakuSitsu;
                        nwpd.syukakuSize         = wdd.syukakuSize;
                        nwpd.syukakuKosu         = wdd.syukakuKosu;
                        nwpd.shukakuRyo          = wdd.shukakuRyo;
                        nwpd.syukakuHakosu       = wdd.syukakuHakosu;
                        nwpd.syukakuNinzu        = wdd.syukakuNinzu;
                        nwpd.save();
                      }

                    /* -- プランラインを作成する -- */
                    PlanLine planLine = new PlanLine();                                       //タイムラインモデルの生成
                    Compartment compartment = Compartment.find.where().eq("kukaku_id", workplan.kukakuId).findUnique();   //区画情報モデルの取得

                    planLine.workPlanId = workplan.workPlanId;

                    planLine.updateTime       = DateU.getSystemTimeStamp();
                    planLine.message        = "【" + work.workName + "情報】<br>";
                    List<WorkPlanDetail> wpds;
                    switch ((int)work.workTemplateId) {
                    case AgryeelConst.WorkTemplate.NOMAL:
                      planLine.message        += "";                                 //メッセージ
                      break;
                    case AgryeelConst.WorkTemplate.SANPU:

                      List<WorkPlanSanpu> wpssp = WorkPlanSanpu.getWorkPlanSanpuList(workplan.workPlanId);

                      for (WorkPlanSanpu wps : wpssp) {

                        /* 農肥IDから農肥情報を取得する */
                        Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  wps.nouhiId).findUnique();

                        String sanpuName  = "";

                        if (wps.sanpuMethod != 0) {
                            sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, wps.sanpuMethod) + "]";
                        }

                        String unit = nouhi.getUnitString();

                        planLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df2.format(wps.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
                        planLine.message       +=  "--------------------------------------------------<br>";

                      }

                      break;
                    case AgryeelConst.WorkTemplate.HASHU:
                      planLine.message       += "<品種> " + Hinsyu.getMultiHinsyuName(workplan.hinsyuId) + "<br>";
                      planLine.message       += "<株間> " + workplan.kabuma + "cm<br>";
                      planLine.message       += "<条間> " + workplan.joukan + "cm<br>";
                      planLine.message       += "<条数> " + workplan.jousu  + "cm<br>";
                      planLine.message       += "<深さ> " + workplan.hukasa + "cm<br>";
                      planLine.message       += "<機器> " + Kiki.getKikiName(workplan.kikiId) + "<br>";
                      planLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workplan.attachmentId) + "<br>";
                      planLine.message       += "<ベルト> " + Belto.getBeltoName(workplan.beltoId) + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.SHUKAKU:
                      List<WorkPlanDetail> wpdsp = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
                      int idx = 0;
                      for (WorkPlanDetail wpd : wpdsp) {
                        if (wpd.shukakuRyo == 0) {
                          continue;
                        }
                        idx++;
                        planLine.message       += "<荷姿" + idx + "> "     + Nisugata.getNisugataName(wpd.syukakuNisugata) + "<br>";
                        planLine.message       += "<質" + idx + "> "       + Shitu.getShituName(wpd.syukakuSitsu) + "<br>";
                        planLine.message       += "<サイズ" + idx + "> "   + Size.getSizeName(wpd.syukakuSize) + "<br>";
                        planLine.message       += "<個数" + idx + "> "   + wpd.syukakuKosu + "個" + "<br>";
                        planLine.message       += "<収穫量" + idx + "> "   + wpd.shukakuRyo + "Kg" + "<br>";
                        planLine.message       +=  "--------------------------------------------------<br>";

                      }
                      if (idx == 0) {
                        planLine.message       += "<荷姿> "     + Nisugata.getNisugataName(workplan.syukakuNisugata) + "<br>";
                        planLine.message       += "<質> "       + Shitu.getShituName(workplan.syukakuSitsu) + "<br>";
                        planLine.message       += "<サイズ> "   + Size.getSizeName(workplan.syukakuSize) + "<br>";
                        planLine.message       += "<収穫量> "   + workplan.shukakuRyo + "Kg" + "<br>";
                        planLine.message       +=  "--------------------------------------------------<br>";
                      }
                      break;
                    case AgryeelConst.WorkTemplate.NOUKO:
                      planLine.message       += "<機器> " + Kiki.getKikiName(workplan.kikiId) + "<br>";
                      planLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workplan.attachmentId) + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.KANSUI:
                      planLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, workplan.kansuiMethod) + "<br>";
                      planLine.message       += "<機器> " + Kiki.getKikiName(workplan.kikiId) + "<br>";
                      planLine.message       += "<潅水量> " + workplan.kansuiRyo + "L" + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.KAISHU:
                      wpds = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
                      idx = 0;
                      for (WorkPlanDetail wpd : wpds) {
                        idx++;
                        planLine.message        +=  "<数量" + idx + ">" + "&nbsp;&nbsp;" + wpd.suryo + "個<br>";

                      }
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.DACHAKU:
                      wpds = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
                      idx = 0;
                      for (WorkPlanDetail wpd : wpds) {
                        idx++;
                        planLine.message        +=  "<資材" + idx + ">" + "&nbsp;&nbsp;" + Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)wpd.sizaiId, true) + "<br>";

                      }
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.COMMENT:
                      wpds = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
                      idx = 0;
                      for (WorkPlanDetail wpd : wpds) {
                        idx++;
                        planLine.message        +=  "<コメント" + idx + ">" + "&nbsp;&nbsp;" + wpd.comment + "<br>";

                      }
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.MALTI:
                      planLine.message       += "<使用マルチ> " + Common.GetCommonValue(Common.ConstClass.ITOMULTI, (int)workplan.useMulti, true) + "<br>";
                      planLine.message       += "<列数> " + workplan.retusu + "列" + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.TEISHOKU:
                      planLine.message       += "<使用苗枚数> " + workplan.naemaisu + "枚" + "<br>";
                      planLine.message       += "<列数> " + workplan.retusu + "列" + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.NAEHASHU:
                      planLine.message       += "<使用穴数> " + workplan.useHole + "穴" + "<br>";
                      planLine.message       += "<枚数> " + workplan.maisu + "枚" + "<br>";
                      planLine.message       += "<使用培土> " + Common.GetCommonValue(Common.ConstClass.ITOBAIDO, (int)workplan.useBaido, true) + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.SENTEI:
                      planLine.message       += "<剪定高> " + workplan.senteiHeight + "cm" + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.MABIKI:
                      planLine.message       += "<仕立本数> " + workplan.shitateHonsu + "本" + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.NICHOCHOSEI:
                      planLine.message       += "<日長> " + workplan.nicho + "時間" + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    case AgryeelConst.WorkTemplate.SENKA:
                      wpdsp = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
                      idx = 0;
                      for (WorkPlanDetail wpd : wpdsp) {
                        if (wpd.syukakuKosu == 0 && wpd.syukakuHakosu == 0) {
                          continue;
                        }
                        idx++;
                        planLine.message       += "<等級" + idx + "> "       + Shitu.getShituName(wpd.syukakuSitsu) + "<br>";
                        planLine.message       += "<階級" + idx + "> "   + Size.getSizeName(wpd.syukakuSize) + "<br>";
                        planLine.message       += "<箱数" + idx + "> "   + wpd.syukakuHakosu + "ケース" + "<br>";
                        planLine.message       += "<本数" + idx + "> "   + wpd.syukakuKosu + "本" + "<br>";
                        planLine.message       += "<人数" + idx + "> "   + wpd.syukakuNinzu + "人" + "<br>";
                        planLine.message       += "<収穫量" + idx + "> " + wpd.shukakuRyo + "Kg" + "<br>";
                        planLine.message       +=  "--------------------------------------------------<br>";

                      }
                      break;
                    case AgryeelConst.WorkTemplate.HAIKI:
                      planLine.message       += "<廃棄量> " + workplan.haikiRyo + "Kg" + "<br>";
                      planLine.message       +=  "--------------------------------------------------<br>";
                      break;
                    }

                    planLine.planLineColor  = work.workColor;                             //プランラインカラー
                    planLine.workId         = work.workId;                                    //作業ID
                    planLine.workName       = work.workName;                                  //作業名
                    planLine.workDate       = workplan.workDate;                              //作業日
                    planLine.kukakuId       = compartment.kukakuId;                           //区画ID
                    planLine.kukakuName     = compartment.kukakuName;                         //区画名
                    planLine.accountId      = sAccountId;                                     //アカウントＩＤ
                    planLine.accountName    = sAccountName;                                   //アカウント名
                    planLine.farmId         = ac.accountData.farmId;                          //農場ID
                    planLine.workPlanFlag   = workplan.workPlanFlag;                          //作業計画フラグ
                    planLine.workPlanUUID   = workplan.workPlanUUID;                          //作業計画UUID

                    planLine.save();                                                          //タイムラインを追加

                  }
                  break;
              }
               targetId = wci.nextSequenceId;
             }
            }
        }

      Ebean.commitTransaction();
      }
      catch (Exception ex) {
        Logger.error("[commitPlanWork] Save Error.", ex);
        ex.printStackTrace();
        Ebean.rollbackTransaction();
        resultJson.put("result"   , "ERROR");
      }

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
}
