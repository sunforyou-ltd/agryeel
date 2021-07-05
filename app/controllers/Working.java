package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Account;
import models.Compartment;
import models.PlanLine;
import models.TimeLine;
import models.Work;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.FieldComprtnent;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;

import consts.AgryeelConst;

public class Working extends Controller {

    /**
     * 【AGRYEEL】作業中画面遷移
     * @return UI画面レンダー
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result move() {
        return ok(views.html.working.render());
    }

    /**
     * 遷移前パラメータ格納処理
     * @return
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result initparam() {

      ObjectNode  resultJson = Json.newObject();

      JsonNode  input = request().body().asJson();
      String    workid    = input.get("workid").asText();
      String    kukakuid  = input.get("kukakuid").asText();
      String    action    = input.get("action").asText();

      Logger.debug("[workid] >>>>> " + workid);
      Logger.debug("[kukakuid] >>>>> " + kukakuid);
      Logger.debug("[action] >>>>> " + action);

      session(AgryeelConst.SessionKey.WORKING_WORKID, workid);
      session(AgryeelConst.SessionKey.WORKING_KUKAKUID, kukakuid);
      session(AgryeelConst.SessionKey.WORKING_ACTION, action);

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }

    /**
     * 画面遷移後、初期処理
     * @return
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result init() {

      ObjectNode  resultJson = Json.newObject();

      String workid   = session(AgryeelConst.SessionKey.WORKING_WORKID);
      String kukakuid = session(AgryeelConst.SessionKey.WORKING_KUKAKUID);
      String action   = session(AgryeelConst.SessionKey.WORKING_ACTION);
      SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

      resultJson.put("action"   , action);
      resultJson.put("workid"   , workid);
      resultJson.put("kukakuid" , kukakuid);

      if ("display".equals(action)) {
        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        Work wk = Work.getWork(accountComprtnent.accountData.workId);
        if (wk != null) {
          resultJson.put("workname", wk.workName);
        }
        else {
          resultJson.put("workname", "");
        }

//        if (accountComprtnent.accountData.workPlanId != 0) {
//          List<models.WorkPlan> workplans = models.WorkPlan.find.where().eq("work_id", accountComprtnent.accountData.workId).eq("account_id", accountComprtnent.accountData.accountId).orderBy("work_plan_id").findList();
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
//          resultJson.put("kukakuname", sKukaku);
//        }
//        else {
//          Compartment cd = FieldComprtnent.getCompartment(accountComprtnent.accountData.fieldId);
//          if (cd != null) {
//            resultJson.put("kukakuname", cd.kukakuName);
//          }
//          else {
//            resultJson.put("kukakuname", "");
//          }
//        }
        Compartment cd = FieldComprtnent.getCompartment(accountComprtnent.accountData.fieldId);
        if (cd != null) {
          resultJson.put("kukakuname", cd.kukakuName);
        }
        else {
          resultJson.put("kukakuname", "");
        }

        Calendar cal = Calendar.getInstance();
        long to   = cal.getTime().getTime();
        long from = accountComprtnent.accountData.workStartTime.getTime();

        long diff = ( to - from  ) / (1000 * 60 );

        resultJson.put("starttime", sdf.format(accountComprtnent.accountData.workStartTime) + " ～");
        resultJson.put("difftime", String.valueOf(diff) + "分経過" );
        if (accountComprtnent.accountData.workPlanId != 0) {
          PlanLine pl = PlanLine.find.where().eq("work_plan_id", accountComprtnent.accountData.workPlanId).findUnique();
          if (pl != null) {
            resultJson.put("aica", pl.message + "<br><br>" + "上記の内容で作業中です。" );
          }
          else {
            resultJson.put("aica", "お疲れ様です。気を付けて作業してくださいね。" );
          }
        }
        else {
          resultJson.put("aica", "お疲れ様です。気を付けて作業してくださいね。" );
        }

      }
      else {
        Work wk = Work.getWork(Double.parseDouble(workid));
        if (wk != null) {
          resultJson.put("workname", wk.workName);
        }
        else {
          resultJson.put("workname", "");
        }

        Compartment cd = FieldComprtnent.getCompartment(Double.parseDouble(kukakuid));
        if (cd != null) {
          resultJson.put("kukakuname", cd.kukakuName);
        }
        else {
          resultJson.put("kukakuworkname", "");
        }

        resultJson.put("starttime", "");
        resultJson.put("difftime", "");

        //【暫定対策】AIっぽく情報を取得する
//        Calendar cSysFrom = Calendar.getInstance();
//        Calendar cSysTo   = Calendar.getInstance();
//        List<TimeLine> tls = new ArrayList<TimeLine>();
//
//        DateU.setTime(cSysFrom, DateU.TimeType.FROM);
//        DateU.setTime(cSysTo, DateU.TimeType.TO);
//        for (int year = -1; -10 <= year; year--) {
//          cSysFrom.add(Calendar.YEAR, year);
//          cSysFrom.add(Calendar.DAY_OF_MONTH, -14);
//          cSysTo.add(Calendar.YEAR, year);
//          cSysTo.add(Calendar.DAY_OF_MONTH, 14);
//          tls = TimeLine.find.where().eq("work_id", Double.parseDouble(workid)).between("work_date", new java.sql.Date(cSysFrom.getTimeInMillis()), new java.sql.Date(cSysTo.getTimeInMillis())).orderBy("work_date desc").findList();
//          if (tls.size() > 0) {
//            break;
//          }
//        }
        List<TimeLine> tls = new ArrayList<TimeLine>();
        tls = TimeLine.find.where().eq("work_id", Double.parseDouble(workid)).eq("kukaku_id", Double.parseDouble(kukakuid)).orderBy("work_date desc").findList();
        if (tls.size() > 0) {
          TimeLine tl = tls.get(0);
          String message = tl.message;
          resultJson.put("aica", message + "<br><br>この様な内容で作業してはいかがでしょうか？" );
        }
        else {
          resultJson.put("aica", "お疲れ様です。気を付けて作業してくださいね。" );
        }
      }

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    /**
     * 作業を開始または終了する。
     * @return
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result workingcommit() {

      ObjectNode  resultJson = Json.newObject();

      String workid   = session(AgryeelConst.SessionKey.WORKING_WORKID);
      String kukakuid = session(AgryeelConst.SessionKey.WORKING_KUKAKUID);
      String action   = session(AgryeelConst.SessionKey.WORKING_ACTION);

      if ("display".equals(action)) {

      }
      else {

        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        Account ac = accountComprtnent.accountData;

        ac.workId   = Double.parseDouble(workid);
        ac.fieldId  = Double.parseDouble(kukakuid);
        ac.workStartTime = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
        ac.update();

        Compartment ct = Compartment.getCompartmentInfo(ac.fieldId);
        Work        wk = Work.getWork(ac.workId);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Logger.info("[ WORKING START ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{} STARTTIME:{}", ac.accountId, ac.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName, sdf.format(ac.workStartTime));

        session(AgryeelConst.SessionKey.WORKING_ACTION, "display");
        accountComprtnent.getAccountJson(resultJson);

      }

      return ok(resultJson);
    }
    /**
     * 作業を中断する。
     * @return
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result workingstop() {

      ObjectNode  resultJson = Json.newObject();

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
      Account ac = accountComprtnent.accountData;

      Compartment ct = Compartment.getCompartmentInfo(ac.fieldId);
      Work        wk = Work.getWork(ac.workId);
      Logger.info("[ WORKING CANCELED ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{}", ac.accountId, ac.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName);

      Ebean.beginTransaction();

      try{
        /* 作業計画が存在する場合 */
        if (ac.workPlanId != 0) {
          models.WorkPlan wp = models.WorkPlan.getWorkPlanById(ac.workPlanId);
          if (wp.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH) {
            List<models.WorkPlan> workplans = models.WorkPlan.find.where().eq("work_id", ac.workId).eq("account_id", ac.accountId).orderBy("work_plan_id").findList();

            for (models.WorkPlan workplan: workplans) {
              Ebean.createSqlUpdate("DELETE FROM work_plan WHERE work_plan_id = :workPlanId")
              .setParameter("workPlanId", workplan.workPlanId).execute();

              Ebean.createSqlUpdate("DELETE FROM work_plan_sanpu WHERE work_plan_id = :workPlanId")
              .setParameter("workPlanId", workplan.workPlanId).execute();

              Ebean.createSqlUpdate("DELETE FROM plan_line WHERE work_plan_id = :workPlanId")
              .setParameter("workPlanId", workplan.workPlanId).execute();

              Ebean.createSqlUpdate("DELETE FROM work_plan_detail WHERE work_plan_id = :workPlanId")
              .setParameter("workPlanId", workplan.workPlanId).execute();

            }
          }
          else {
            wp.workStartTime = null;
            if (wp.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKPLANWATCH) {
              wp.workPlanFlag = AgryeelConst.WORKPLANFLAG.WORKPLANCOMMIT;
            }
            else {
              wp.workPlanFlag = AgryeelConst.WORKPLANFLAG.AICAPLANCOMMIT;
            }
            wp.update();
            PlanLine pl = PlanLine.find.where().eq("work_plan_id", ac.workPlanId).findUnique();
            if (pl != null) {
              if (pl.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKPLANWATCH || pl.workPlanFlag == AgryeelConst.WORKPLANFLAG.AICAPLANWATCH) {
                pl.workStartTime = null;
                pl.message = pl.message.substring(0, pl.message.indexOf("【 開始時間 】"));
                pl.workPlanFlag = wp.workPlanFlag;
                pl.update();
              }
            }
          }
        }

        ac.workId   = 0;
        ac.fieldId  = 0;
        ac.workStartTime = null;
        ac.workPlanId = 0;
        ac.update();

        Ebean.commitTransaction();
      }
      catch(Exception ex) {
        ex.printStackTrace();
        Logger.error(ex.getMessage(), ex);
        Ebean.rollbackTransaction();
      }

      return ok(resultJson);
    }

}
