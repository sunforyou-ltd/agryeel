package controllers;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import models.Account;
import models.Compartment;
import models.FarmStatus;
import models.PlanLine;
import models.TimeLine;
import models.Work;
import models.WorkDiary;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.StringU;
import util.TimeU;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;

import consts.AgryeelConst;

public class WorkingAccount extends Controller {

    /**
     * 【AGRYEEL】担当者別作業一覧
     * @return UI画面レンダー
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result move() {
        if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
          return redirect("/move");
        }
        session(AgryeelConst.SessionKey.BACK_MODE, "");
        session(AgryeelConst.SessionKey.BACK_ACCOUNT, "");
        session(AgryeelConst.SessionKey.BACK_DATE, "");
        return ok(views.html.workingaccount.render());
    }

  /*
  localStorage共通化に伴い、アクションを廃止（混乱を避けるため）
  public static Result backmove(String back, String accountId, String workdate) {

    if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
      return redirect("/move");
    }
    session(AgryeelConst.SessionKey.BACK_MODE, back);
    session(AgryeelConst.SessionKey.BACK_ACCOUNT, accountId);
    session(AgryeelConst.SessionKey.BACK_DATE, workdate);

    return ok(views.html.workingaccount.render());
  }
  */

    /**
     * 画面遷移後、初期処理
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result init(String targetDate) {

      ObjectNode        resultJson  = Json.newObject();
      ObjectNode        listJson    = Json.newObject();
      SimpleDateFormat  sdf3        = new SimpleDateFormat("yyyyMMdd");
      java.util.Date    dt;
      boolean           api         = false; //API フラグ
      ObjectMapper      mapper      = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode         apiJson     = mapper.createArrayNode();

      if (session(AgryeelConst.SessionKey.API) != null) { // APIの場合、フラグをON
        api = true;
      }
      Logger.info("[WorkingAccount] ACCOUNT[{}] DATE[{}] API[{}]", session(AgryeelConst.SessionKey.ACCOUNTID), targetDate, api);

      try {
        dt = sdf3.parse(targetDate);
      } catch (ParseException e) {
        dt = Calendar.getInstance().getTime();
      }

      UserComprtnent accountComprtnent = new UserComprtnent();
      if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
        return redirect("/move");
      }
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      List<Account> accounts = Account.getAccountOfFarm(accountComprtnent.accountData.farmId);
      FarmStatus fs = FarmStatus.getFarmStatus(accountComprtnent.accountData.farmId);

      SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
      SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");


      double timelineId = 0;
      //----- 全担当者を固定で表示する -----
      ObjectNode timeLineJson         = Json.newObject();
      if (!api) {                                                                     //API以外の場合
        timelineId--;
        timeLineJson.put("timeLineId"   , timelineId);                                //タイムラインID
        timeLineJson.put("workdate"     , "");
        timeLineJson.put("updateTime"   , "");
        timeLineJson.put("message"      , "");                                        //メッセージ
        timeLineJson.put("timeLineColor", "cccccc");                                  //タイムラインカラー
        timeLineJson.put("workDiaryId"  , timelineId);                                //作業記録ID
        timeLineJson.put("workName"     , "");                                        //作業名
        timeLineJson.put("kukakuName"   , "");                                        //区画名
        timeLineJson.put("accountId"    , AgryeelConst.SpecialAccount.ALLACOUNT);     //アカウントID
        timeLineJson.put("accountName"  , "全担当者");                                  //アカウント名
        timeLineJson.put("workId"       , 0);                                         //作業ＩＤ
        timeLineJson.put("worktime"     , 0);                                         //作業時間
        timeLineJson.put("workDiaryId"  , timelineId);                                //ＩＤ
        timeLineJson.put("msgIcon"      , 0);                                         //メッセージアイコン
        timeLineJson.put("today"        , "");                                        //該当日作業時間
        timeLineJson.put("check"        , 0);                                         //入力時間チェック


        listJson.put(AgryeelConst.SpecialAccount.ALLACOUNT, timeLineJson);
      }

      //----- 正規の担当者別作業一覧を表示する-----
      for (Account working : accounts) {

        if (working.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        Work wk = working.getWork();

        List<TimeLine> tls = TimeLine.getTimeLineOfAccount(working.accountId, working.farmId, new java.sql.Timestamp(dt.getTime()), new java.sql.Timestamp(dt.getTime()));
        long wt = 0;
        for (TimeLine tl:tls) {
          WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
          if (wd != null) {
            wt += wd.workTime;
            Logger.debug("[ workdiaryid ]{} [ time ] {}", tl.workDiaryId, wd.workTime);
          }
        }
        int check = 0;
        if ((wt != 0) &&( wt < fs.workTimeOfDay)) {
          check = 1;
        }
        TimeU tu = new TimeU();
        tu.set_minute((int)wt);
        String todayString = tu.get_hour() + " 時間" + tu.get_minute() + "分";

        if (wk == null) {

          timeLineJson         = Json.newObject();
          timelineId--;
          timeLineJson.put("timeLineId"   , timelineId);                                //タイムラインID
          timeLineJson.put("workdate"     , "");
          timeLineJson.put("updateTime"   , "");
          timeLineJson.put("message"      , "");                                        //メッセージ
          timeLineJson.put("timeLineColor", "cccccc");                                  //タイムラインカラー
          timeLineJson.put("workDiaryId"  , timelineId);                                //作業記録ID
          timeLineJson.put("workName"     , "");                                        //作業名
          timeLineJson.put("kukakuName"   , "");                                        //区画名
          timeLineJson.put("accountId"    , working.accountId);                         //アカウントID
          timeLineJson.put("accountName"  , working.acountName);                        //アカウント名
          timeLineJson.put("workId"       , 0);                                         //作業ＩＤ
          timeLineJson.put("worktime"     , 0);                                         //作業時間
          timeLineJson.put("workDiaryId"  , timelineId);                                //ＩＤ
          timeLineJson.put("msgIcon"      , 0);                                         //メッセージアイコン
          timeLineJson.put("today"        , todayString);                               //該当日作業時間
          timeLineJson.put("check"        , check);                                     //入力時間チェック

          listJson.put(working.accountId, timeLineJson);
          apiJson.add(timeLineJson);

        }
        else { //作業中の場合

          Compartment cpt = Compartment.getCompartmentInfo(working.fieldId);

          Calendar cal = Calendar.getInstance();
          long to   = cal.getTime().getTime();
          long from = working.workStartTime.getTime();

          long diff = ( to - from  ) / (1000 * 60 );

          timeLineJson         = Json.newObject();
          timelineId--;
          timeLineJson.put("timeLineId"   , timelineId);                                //タイムラインID
          timeLineJson.put("workdate"     , sdf2.format(working.workStartTime));        //作業日
          timeLineJson.put("updateTime"   , sdf.format(working.workStartTime));         //更新日時
          if (working.workPlanId != 0) {
            PlanLine pl = PlanLine.find.where().eq("work_plan_id", working.workPlanId).findUnique();
            if (pl != null) {
              timeLineJson.put("message"      , pl.message + "<br><br>" + working.notificationMessage);             //メッセージ
            }
            else {
              timeLineJson.put("message"      , working.notificationMessage);                          //メッセージ
            }
          }
          else {
            timeLineJson.put("message"      , working.notificationMessage);                            //メッセージ
          }
          timeLineJson.put("timeLineColor", wk.workColor);                              //タイムラインカラー
          timeLineJson.put("workDiaryId"  , timelineId);                                //作業記録ID
          timeLineJson.put("workName"     , wk.workName);                               //作業名
//          if (working.workPlanId != 0) {
//            List<models.WorkPlan> workplans = models.WorkPlan.find.where().eq("work_id", working.workId).eq("account_id", working.accountId).orderBy("work_plan_id").findList();
//            String sKukaku = "";
//            for (models.WorkPlan workplan: workplans) {
//              Compartment cd = FieldComprtnent.getCompartment(workplan.kukakuId);
//              if (cd != null) {
//                if (!"".equals(sKukaku)) {
//                  sKukaku += ",";
//                }
//                sKukaku += cd.kukakuName;
//              }
//            }
//            timeLineJson.put("kukakuName", sKukaku);
//          }
//          else {
//            timeLineJson.put("kukakuName"   , cpt.kukakuName);                            //区画名
//          }
          timeLineJson.put("kukakuName"   , cpt.kukakuName);                            //区画名
          timeLineJson.put("accountId"    , working.accountId);                         //アカウントID
          timeLineJson.put("accountName"  , working.acountName);                        //アカウント名
          timeLineJson.put("workId"       , wk.workId);                                 //作業ＩＤ
          timeLineJson.put("worktime"     , diff);                                      //作業時間
          timeLineJson.put("workDiaryId"  , timelineId);                                //ＩＤ
          timeLineJson.put("msgIcon"      , working.messageIcon);                       //メッセージアイコン
          timeLineJson.put("today"        , todayString);                               //該当日作業時間
          timeLineJson.put("check"        , check);                                     //入力時間チェック

          listJson.put(working.accountId, timeLineJson);
          apiJson.add(timeLineJson);

        }



      }

      if (!api) {                                                                     //API以外の場合
        resultJson.put("backmode", session(AgryeelConst.SessionKey.BACK_MODE));
        resultJson.put("backaccount", session(AgryeelConst.SessionKey.BACK_ACCOUNT));
        resultJson.put("backdate", session(AgryeelConst.SessionKey.BACK_DATE));

        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
      }
      else {
        resultJson.put("data", apiJson);
      }
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    public static Result accountinfo(String accountid, String targetDate) {

      ObjectNode  resultJson = Json.newObject();
      ObjectNode  listJson   = Json.newObject();
      SimpleDateFormat sdf3  = new SimpleDateFormat("yyyyMMdd");
      java.util.Date dt;
      try {
        dt = sdf3.parse(targetDate);
      } catch (ParseException e) {
        dt = Calendar.getInstance().getTime();
      }

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      Calendar cals = Calendar.getInstance();  //開始日の生成
      cals.setTime(dt);
      cals.set(cals.get(Calendar.YEAR), 0, 1, 0, 0, 0);
      java.sql.Timestamp dates = new Timestamp(cals.getTime().getTime());
      Calendar cale = Calendar.getInstance();  //終了日の生成
      cale.setTime(dt);
      cale.set(cale.get(Calendar.YEAR), 11, 31, 23, 59, 59);
      java.sql.Timestamp datee = new Timestamp(cale.getTime().getTime());

      List<TimeLine> tls = TimeLine.getTimeLineOfAccount(accountid, accountComprtnent.accountData.farmId, dates, datee);

      double yeartime = 0;
      double[] monthtime = {0,0,0,0,0,0,0,0,0,0,0,0};

      for (TimeLine tl : tls) {

        WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", tl.workDiaryId).findUnique();

        if (workd == null) { continue; }

        Calendar clst = Calendar.getInstance();
        clst.setTime((java.util.Date)tl.workDate);


        yeartime += workd.workTime;

        monthtime[clst.get(Calendar.MONTH)] += workd.workTime;

      }

      DecimalFormat df = new DecimalFormat("0000.0");

      //----- 分から時間に換算しながらJSONに格納する -----
      yeartime = yeartime / 60;
      resultJson.put("yeartime", df.format(yeartime));

      for (int i=0; i < 12; i++) {
        monthtime[i] = monthtime[i] / 60;
        resultJson.put("monthtime" + (i + 1), df.format(monthtime[i]));
      }

      //----- 作業日 -----
      Calendar calsys = Calendar.getInstance();  //システム日付の生成
      calsys.setTime(dt);
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

      resultJson.put("workdate", sdf.format(calsys.getTime()));

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      //アカウント情報をJSONに格納する
      if (accountid.equals(AgryeelConst.SpecialAccount.ALLACOUNT)) {
        resultJson.put("name", "全担当者");
        resultJson.put("id", AgryeelConst.SpecialAccount.ALLACOUNT);
      }
      else {
        Account ac = Account.getAccount(accountid);
        if (ac != null) {
          resultJson.put("name", ac.acountName);
          resultJson.put("id", ac.accountId);
        }
        else {
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
        }
      }


      return ok(resultJson);
    }
    public static Result getworkdata(String accountid, String workdate) {

      ObjectNode  resultJson = Json.newObject();
      ObjectNode  listJson   = Json.newObject();

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      String[] date = {workdate.substring(0,4), workdate.substring(4,6), workdate.substring(6,8)};

      Logger.debug("TARGET DATE -> " + date[0] + "/" + date[1] + "/" + date[2]);

      Calendar cals = Calendar.getInstance();  //開始日の生成
      cals.set(Integer.parseInt(date[0]), (Integer.parseInt(date[1]) - 1), Integer.parseInt(date[2]), 0, 0, 0);
      java.sql.Timestamp dates = new Timestamp(cals.getTime().getTime());
      Calendar cale = Calendar.getInstance();  //終了日の生成
      cale.set(Integer.parseInt(date[0]), (Integer.parseInt(date[1]) - 1), Integer.parseInt(date[2]), 23, 59, 59);
      java.sql.Timestamp datee = new Timestamp(cale.getTime().getTime());

      List<TimeLine> tls = TimeLine.getTimeLineOfAccount(accountid, accountComprtnent.accountData.farmId, dates, datee);

      SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
      SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");
      SimpleDateFormat sdf3 = new SimpleDateFormat("HHmm");
      for (TimeLine tl : tls) {

        WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", tl.workDiaryId).findUnique();
        Work      work  = Work.getWork(tl.workId);

        if (workd == null || work == null || workd.workStartTime == null || workd.workEndTime == null) { continue; }

        ObjectNode  jd   = Json.newObject();
        jd.put("timeLineId"   , tl.timeLineId);                   //タイムラインID
        jd.put("workdate"     , sdf2.format(workd.workDate));     //作業日
        jd.put("updateTime"   , sdf.format(tl.updateTime));       //更新日時
        jd.put("message"      , StringU.setNullTrim(tl.message)); //メッセージ
        jd.put("timeLineColor", tl.timeLineColor);                //タイムラインカラー
        jd.put("workDiaryId"  , tl.workDiaryId);                  //作業記録ID
        jd.put("workName"     , tl.workName);                     //作業名
        jd.put("kukakuName"   , tl.kukakuName);                   //区画名
        jd.put("accountName"  , tl.accountName);                  //アカウント名
        jd.put("workId"       , tl.workId);                       //作業ＩＤ
        jd.put("worktime"     , workd.workTime);                  //作業時間
        jd.put("workDiaryId"  , workd.workDiaryId);               //ＩＤ
        jd.put("start"        , sdf3.format(workd.workStartTime));//作業開始時間
        jd.put("end"          , sdf3.format(workd.workEndTime));  //作業終了時間

        listJson.put(String.valueOf(tl.timeLineId), jd);

      }

      Account ac = Account.getAccount(accountid);

      if (ac != null && ac.workId != 0) {
        Compartment cpt = Compartment.getCompartmentInfo(ac.fieldId);

        Calendar calt = Calendar.getInstance();
        calt.setTime(dates);
        Calendar calw = Calendar.getInstance();
        calw.setTime(ac.workStartTime);
        int wy = calw.get(Calendar.YEAR);
        int wm = calw.get(Calendar.MONTH);
        int wd = calw.get(Calendar.DAY_OF_MONTH);
        int sy = calt.get(Calendar.YEAR);
        int sm = calt.get(Calendar.MONTH);
        int sd = calt.get(Calendar.DAY_OF_MONTH);

        if ((wy == sy) && (wm == sm) && (wd == sd)) { //日付が当時の場合のみ作業中データを作成
          Calendar cal  = Calendar.getInstance();
          long to   = cal.getTime().getTime();
          long from = ac.workStartTime.getTime();

          long diff = ( to - from  ) / (1000 * 60 );

          Work      work  = Work.getWork(ac.workId);

          ObjectNode jd         = Json.newObject();
          jd.put("timeLineId"   , -1);                                        //タイムラインID
          jd.put("workdate"     , sdf2.format(ac.workStartTime));             //作業日
          jd.put("updateTime"   , sdf.format(ac.workStartTime));              //更新日時
          jd.put("message"      , "【現在作業中】");                              //メッセージ
          jd.put("timeLineColor", "aaaaaa");                                  //タイムラインカラー
          jd.put("workDiaryId"  , -1);                                        //作業記録ID
          jd.put("workName"     , work.workName);                             //作業名
          jd.put("kukakuName"   , cpt.kukakuName);                            //区画名
          jd.put("accountId"    , ac.accountId);                              //アカウントID
          jd.put("accountName"  , ac.acountName);                             //アカウント名
          jd.put("workId"       , work.workId);                               //作業ＩＤ
          jd.put("worktime"     , diff);                                      //作業時間
          jd.put("workDiaryId"  , -1);                                        //ＩＤ
          jd.put("start"        , sdf3.format(ac.workStartTime));             //作業開始時間
          jd.put("end"          , sdf3.format(cal.getTime()));                //作業終了時間

          listJson.put("-1", jd);
        }

      }


      resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
}
