package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Account;
import models.Compartment;
import models.Hinsyu;
import models.IkubyoPlan;
import models.IkubyoPlanLine;
import models.NaeStatus;
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

public class WorkingIkubyo extends Controller {

    /**
     * 【AICA】育苗管理作業中画面遷移
     * @return UI画面レンダー
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result move() {
        return ok(views.html.workingikubyo.render());
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
      String    planid    = input.get("planid").asText();
      String    action    = input.get("action").asText();

      Logger.debug("[workid] >>>>> " + workid);
      Logger.debug("[planid] >>>>> " + planid);
      Logger.debug("[action] >>>>> " + action);

      session(AgryeelConst.SessionKey.WORKING_WORKID, workid);
      session(AgryeelConst.SessionKey.WORKING_PLANID, planid);
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
      String action   = session(AgryeelConst.SessionKey.WORKING_ACTION);
      SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

      resultJson.put("action"   , action);
      resultJson.put("workid"   , workid);

      if ("display".equals(action)) {
        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        resultJson.put("planid"   , accountComprtnent.accountData.workPlanId);

        Work wk = Work.getWork(accountComprtnent.accountData.workId);
        if (wk != null) {
          resultJson.put("workname", wk.workName);
        }
        else {
          resultJson.put("workname", "");
        }

        IkubyoPlanLine ipl = IkubyoPlanLine.find.where().eq("ikubyo_plan_id", accountComprtnent.accountData.workPlanId).findUnique();
        String[] sNaeNos = ipl.naeNo.split("-");
        NaeStatus ns = NaeStatus.find.where().eq("nae_no", ipl.naeNo).findUnique();
        if (ns != null) {
          resultJson.put("naeName"   , ns.hinsyuName + "(" + sNaeNos[1] + ")");                             //苗名
        }
        else {
          IkubyoPlan ip = IkubyoPlan.find.where().eq("ikubyo_plan_id", ipl.ikubyoPlanId).findUnique();
          resultJson.put("naeName"   ,  Hinsyu.getMultiHinsyuName(ip.hinsyuId) + "(" + sNaeNos[1] + ")");   //苗名
        }

        Calendar cal = Calendar.getInstance();
        long to   = cal.getTime().getTime();
        long from = accountComprtnent.accountData.workStartTime.getTime();

        long diff = ( to - from  ) / (1000 * 60 );

        resultJson.put("starttime", sdf.format(accountComprtnent.accountData.workStartTime) + " ～");
        resultJson.put("difftime", String.valueOf(diff) + "分経過" );
        if (accountComprtnent.accountData.workPlanId != 0) {
          if (ipl != null) {
            resultJson.put("aica", ipl.message + "<br><br>" + "上記の内容で作業中です。" );
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
        resultJson.put("naeName"   , "");
        resultJson.put("starttime", "");
        resultJson.put("difftime", "");
        resultJson.put("aica", "お疲れ様です。気を付けて作業してくださいね。" );
      }

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

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

      double planId = ac.workPlanId;
      Work wk = Work.getWork(ac.workId);
      String naeNo = "";
      if (planId != 0) {
        IkubyoPlanLine ipl = IkubyoPlanLine.find.where().eq("ikubyo_plan_id", planId).findUnique();
        naeNo = ipl.naeNo;
      }
      Logger.info("[ WORKING CANCELED ] ID:{} NAME:{} NAENO:{} WORKID:{} WORKNAME:{}", ac.accountId, ac.acountName, naeNo, wk.workId, wk.workName);

      Ebean.beginTransaction();

      try{
        /* 育苗計画が存在する場合 */
        if (ac.workPlanId != 0) {
          models.IkubyoPlan ip = models.IkubyoPlan.getIkubyoPlanById(ac.workPlanId);
          List<models.IkubyoPlan> ikubyoplans = models.IkubyoPlan.find.where().eq("work_id", ac.workId).eq("account_id", ac.accountId).orderBy("ikubyo_plan_id").findList();

          for (models.IkubyoPlan ikubyoplan: ikubyoplans) {
            Ebean.createSqlUpdate("DELETE FROM ikubyo_plan WHERE ikubyo_plan_id = :ikubyoPlanId")
            .setParameter("ikubyoPlanId", ikubyoplan.ikubyoPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_sanpu WHERE ikubyo_plan_id = :ikubyoPlanId")
            .setParameter("ikubyoPlanId", ikubyoplan.ikubyoPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_line WHERE ikubyo_plan_id = :ikubyoPlanId")
            .setParameter("ikubyoPlanId", ikubyoplan.ikubyoPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM ikubyo_plan_detail WHERE ikubyo_plan_id = :ikubyoPlanId")
            .setParameter("ikubyoPlanId", ikubyoplan.ikubyoPlanId).execute();

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
