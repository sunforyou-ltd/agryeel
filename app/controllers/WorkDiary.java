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
import models.Belto;
import models.Common;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Hinsyu;
import models.Kiki;
import models.NaeStatus;
import models.Nisugata;
import models.Nouhi;
import models.PlanLine;
import models.Sequence;
import models.Shitu;
import models.Size;
import models.TimeLine;
import models.Work;
import models.WorkDiaryDetail;
import models.WorkDiarySanpu;
import models.WorkLastTime;
import models.WorkPlanDetail;
import models.WorkPlanSanpu;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CommonGetWorkDiaryData;
import compornent.CommonWorkDiary;
import compornent.CommonWorkDiaryWork;
import compornent.FarmComprtnent;
import compornent.FieldComprtnent;
import compornent.NaeMotochoCompornent;
import compornent.NaeStatusCompornent;
import compornent.NouhiComprtnent;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;
import compornent.WorkChainCompornent;
import compornent.WorkHistryBaseComprtnent;
import compornent.WorkHistryDetailComprtnent;

import consts.AgryeelConst;

public class WorkDiary extends Controller {

    private static final String SCREENID     = "WORKDIARY";
    private static final String ACTIONMOVE   = "MOVE";
    private static final String ACTIONSUBMIT = "SUBMIT";
    private static final String ACTIONDELETE = "DELETE";

    /**
     * 【AGRYEEL】作業日誌画面遷移
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result workDiaryMove(String workid, String kukakuid) {

        session(AgryeelConst.SessionKey.WORKID
                , workid);		//作業IDをセッションに格納
        session(AgryeelConst.SessionKey.KUKAKUID
                , kukakuid);	//区画IDをセッションに格納
        session(AgryeelConst.SessionKey.WORKDIARYID
                , "");										//作業記録IDをセッションに格納
        session(AgryeelConst.SessionKey.WORKPLANID
                , "");                    //作業計画IDをセッションに格納

        session(AgryeelConst.SessionKey.BACK_MODE, "");
        session(AgryeelConst.SessionKey.BACK_ACCOUNT, "");
        session(AgryeelConst.SessionKey.BACK_DATE, "");

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONMOVE);

        return ok(views.html.workDiary.render(""));

    }

    /**
     * 【AGRYEEL】作業日誌画面編集遷移
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result workDiaryEditMove(double workDiaryId) {

        session(AgryeelConst.SessionKey.WORKID
                , "");										//作業IDをセッションに格納
        session(AgryeelConst.SessionKey.KUKAKUID
                , "");										//区画IDをセッションに格納
        session(AgryeelConst.SessionKey.WORKDIARYID
                , String.valueOf(workDiaryId));				//作業記録IDをセッションに格納
        session(AgryeelConst.SessionKey.WORKPLANID
            , "");                                    //作業計画IDをセッションに格納

        session(AgryeelConst.SessionKey.BACK_MODE, "");
        session(AgryeelConst.SessionKey.BACK_ACCOUNT, "");
        session(AgryeelConst.SessionKey.BACK_DATE, "");

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONMOVE);

        return ok(views.html.workDiary.render(""));

    }
    /**
     * 【AGRYEEL】作業日誌画面計画編集遷移
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result workPlanEditMove(double workPlanId) {

        session(AgryeelConst.SessionKey.WORKID
                , "");                    //作業IDをセッションに格納
        session(AgryeelConst.SessionKey.KUKAKUID
                , "");                    //区画IDをセッションに格納
        session(AgryeelConst.SessionKey.WORKDIARYID
              , "");                            //作業記録IDをセッションに格納
        session(AgryeelConst.SessionKey.WORKPLANID
                , String.valueOf(workPlanId));  //作業計画IDをセッションに格納

        session(AgryeelConst.SessionKey.BACK_MODE, "");
        session(AgryeelConst.SessionKey.BACK_ACCOUNT, "");
        session(AgryeelConst.SessionKey.BACK_DATE, "");

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONMOVE);

        return ok(views.html.workDiary.render(""));

    }

    /**
     * 【AGRYEEL】作業日誌画面編集遷移
     * @return
     */
/*
    localStorage共通化に伴い、アクションを廃止（混乱を避けるため）
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result workDiaryEditMoveBack(double workDiaryId, String back, String accountId, String workdate) {

        session(AgryeelConst.SessionKey.WORKID
                , "");                    //作業IDをセッションに格納
        session(AgryeelConst.SessionKey.KUKAKUID
                , "");                    //区画IDをセッションに格納
        session(AgryeelConst.SessionKey.WORKDIARYID
                , String.valueOf(workDiaryId));       //作業記録IDをセッションに格納

        session(AgryeelConst.SessionKey.BACK_MODE, back);
        session(AgryeelConst.SessionKey.BACK_ACCOUNT, accountId);
        session(AgryeelConst.SessionKey.BACK_DATE, workdate);

        return ok(views.html.workDiary.render(""));

    }
*/
    /**
     * 【AGRYEEL】作業日誌初期表示データ取得
     * @return
     */
    public static Result workDiaryInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

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
        CommonWorkDiary cwd	   = new CommonWorkDiary(session(), resultJson);//作業記録共通項目

        cwd.init();															//初期処理

        /*-------------------------------------------------------------------*/
        /* 各一覧表情報の取得                                                */
        /*-------------------------------------------------------------------*/
        CommonWorkDiaryWork cwdk = CommonWorkDiaryWork.getCommonWorkDiaryWork((int)cwd.work.workTemplateId, session(), resultJson);        //作業記録作業別項目

        if (cwdk != null) {								//作業項目別コンポーネントが生成できた場合

            cwdk.kukakuId   = cwd.kukakuId;
            cwdk.workId     = cwd.workId;
        	  cwdk.workDiary	=	cwd.workDiary;
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
    public static Result submitWorkDiary() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        updateWorkDiary(resultJson, input, 0);

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】作業日誌記録処理(編集時)
     * @return
     */
    public static Result submitWorkDiaryEdit(double workDiaryId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        updateWorkDiary(resultJson, input, workDiaryId);

        return ok(resultJson);
    }

    public static void updateWorkDiary(ObjectNode resultJson, JsonNode input, double editWorkDiaryId) {
      updateWorkDiary(resultJson, input, editWorkDiaryId, false);
    }
    public static void updateWorkDiary(ObjectNode resultJson, JsonNode input, double editWorkDiaryId, boolean api) {

      try {

        WorkDiaryThread  wdt  = new WorkDiaryThread();
        Ebean.beginTransaction();
        int workId = Integer.parseInt(input.get("workId").asText());	//作業IDの取得

        /*-- 取得した項目を作業日誌に格納する --*/
        Work work = Work.find.where().eq("work_id", workId).findUnique();												//作業情報モデルの取得
        String sKukakuId = input.get("workKukaku").asText();                              //複数区画同時作業記録
        resultJson.put("kukakus", sKukakuId);                                             //連続作業入力用に区画をJSONに格納
        String[] sKukakuIds = sKukakuId.split(",");
        double workDiaryId = 0;
        DecimalFormat df = new DecimalFormat("#,##0.00");
        boolean editFlag = false;                                                         //編集フラグ

        for (String kukakuId : sKukakuIds) {
          models.WorkDiary workDiary = new models.WorkDiary();                              //作業日誌モデルの生成
          CommonWorkDiary cwd       = new CommonWorkDiary(session(), resultJson);//作業記録共通項目
          ArrayList<WorkDiarySanpu> wdsps = new ArrayList<WorkDiarySanpu>();

          /*-------------------------------------------------------------------*/
          /* 編集時は対象となる作業記録を一旦削除する                          */
          /*-------------------------------------------------------------------*/
          if (editWorkDiaryId != 0) {
            editFlag = true;
            models.WorkDiary wd =models.WorkDiary.getWorkDiaryById(editWorkDiaryId);
            if (wd != null) {
              workDiary.workStartTime = wd.workStartTime;
              workDiary.workEndTime = wd.workEndTime;
              workDiary.workPlanFlag = wd.workPlanFlag; //作業計画フラグを引き継ぐ
            }
            workDiaryDeleteCommit(editWorkDiaryId);
            if (sKukakuIds.length > 1) { //複数区画の場合
              editWorkDiaryId = 0;       //新規作業記録IDを発行する為、作業記録IDをリセットする
            }
          }

          /*-------------------------------------------------------------------*/
          /* 共通項目の反映                                                    */
          /*-------------------------------------------------------------------*/
          workDiary.hinsyuId = "0"; //品種IDの初期値を設定
          cwd.commit(input, workDiary, work, editWorkDiaryId, Double.parseDouble(kukakuId));

          /*-------------------------------------------------------------------*/
          /* 作業別項目の反映                                                  */
          /*-------------------------------------------------------------------*/
          CommonWorkDiaryWork cwdk = CommonWorkDiaryWork.getCommonWorkDiaryWork((int)cwd.work.workTemplateId, session(), resultJson);        //作業記録作業別項目

          if (cwdk != null) {               //作業項目別コンポーネントが生成できた場合
            cwdk.apiFlg = api;
            cwdk.commit(input, workDiary, work);    //作業別項目の生成

          }

          if ((work.workTemplateId == AgryeelConst.WorkTemplate.SHUKAKU && cwdk.shukakuryo > 0) ||
              (work.workTemplateId == AgryeelConst.WorkTemplate.SENKA && cwdk.shukakuryo > 0)) {
            workDiary.shukakuRyo = cwdk.shukakuryo;
          }

          /* 作業記録をこのタイミングで保存する */
          workDiary.save();

          //作付開始自動連携を実施
          if (!editFlag) {  //新規作成の場合
            models.WorkDiary autoWorkDiary = WorkChainCompornent.makeAutoStart(workDiary);
            if ( autoWorkDiary != null ) {                                                                                //自動的に作付開始が生成された場合
              //スレッドの処理対象に加える
              ArrayList<WorkDiarySanpu> autoWorkDiarySanpu = new ArrayList<WorkDiarySanpu>();
              wdt.wdspss.add(autoWorkDiarySanpu);
              wdt.workDiarys.add(autoWorkDiary);
            }
          }

          /* -- タイムラインを作成する -- */
          TimeLine timeLine = new TimeLine();                                       //タイムラインモデルの生成
          Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.TIMELINEID);               //最新シーケンス値の取得
          Compartment compartment = Compartment.find.where().eq("kukaku_id", workDiary.kukakuId).findUnique();      //区画情報モデルの取得
          Account account     = Account.find.where().eq("account_id", workDiary.accountId).findUnique();        //アカウント情報モデルの取得

          timeLine.timeLineId       = sequence.sequenceValue;                           //タイムラインID

          timeLine.updateTime       = DateU.getSystemTimeStamp();
          //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
          timeLine.message        = "【" + work.workName + "情報】<br>";
          switch ((int)cwd.work.workTemplateId) {
          case AgryeelConst.WorkTemplate.NOMAL:
            timeLine.message        += "";                                 //メッセージ
            break;
          case AgryeelConst.WorkTemplate.SANPU:

            List<WorkDiarySanpu> wdss = WorkDiarySanpu.getWorkDiarySanpuList(workDiary.workDiaryId);

            for (WorkDiarySanpu wds : wdss) {

              /* 農肥IDから農肥情報を取得する */
              Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  wds.nouhiId).findUnique();

              String sanpuName  = "";

              if (wds.sanpuMethod != 0) {
                  sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, wds.sanpuMethod) + "]";
              }

              String unit = nouhi.getUnitString();

              timeLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df.format(wds.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
              timeLine.message        +=  "--------------------------------------------------<br>";

              wdsps.add(wds);

            }

            break;
          case AgryeelConst.WorkTemplate.HASHU:
            timeLine.message       += "<品種> " + Hinsyu.getMultiHinsyuName(workDiary.hinsyuId) + "<br>";
            timeLine.message       += "<株間> " + workDiary.kabuma + "cm<br>";
            timeLine.message       += "<条間> " + workDiary.joukan + "cm<br>";
            timeLine.message       += "<条数> " + workDiary.jousu  + "cm<br>";
            timeLine.message       += "<深さ> " + workDiary.hukasa + "cm<br>";
            timeLine.message       += "<機器> " + Kiki.getKikiName(workDiary.kikiId) + "<br>";
            timeLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workDiary.attachmentId) + "<br>";
            timeLine.message       += "<ベルト> " + Belto.getBeltoName(workDiary.beltoId) + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.SHUKAKU:
            List<WorkDiaryDetail> wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
            int idx = 0;
            for (WorkDiaryDetail wdd : wdds) {
              if (wdd.shukakuRyo == 0) {
                continue;
              }
              idx++;
              timeLine.message       += "<荷姿" + idx + "> "     + Nisugata.getNisugataName(wdd.syukakuNisugata) + "<br>";
              timeLine.message       += "<質" + idx + "> "       + Shitu.getShituName(wdd.syukakuSitsu) + "<br>";
              timeLine.message       += "<サイズ" + idx + "> "   + Size.getSizeName(wdd.syukakuSize) + "<br>";
              timeLine.message       += "<個数" + idx + "> "   + wdd.syukakuKosu + "個" + "<br>";
              timeLine.message       += "<収穫量" + idx + "> "   + wdd.shukakuRyo + "Kg" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";

            }
            if (idx == 0) {
              timeLine.message       += "<荷姿> "     + Nisugata.getNisugataName(workDiary.syukakuNisugata) + "<br>";
              timeLine.message       += "<質> "       + Shitu.getShituName(workDiary.syukakuSitsu) + "<br>";
              timeLine.message       += "<サイズ> "   + Size.getSizeName(workDiary.syukakuSize) + "<br>";
              timeLine.message       += "<収穫量> "   + workDiary.shukakuRyo + "Kg" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
            }
            break;
          case AgryeelConst.WorkTemplate.NOUKO:
            timeLine.message       += "<機器> " + Kiki.getKikiName(workDiary.kikiId) + "<br>";
            timeLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workDiary.attachmentId) + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.KANSUI:
            timeLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, workDiary.kansuiMethod) + "<br>";
            timeLine.message       += "<機器> " + Kiki.getKikiName(workDiary.kikiId) + "<br>";
            timeLine.message       += "<潅水量> " + workDiary.kansuiRyo + "L" + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.KAISHU:
            wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
            idx = 0;
            for (WorkDiaryDetail wdd : wdds) {
              idx++;
              timeLine.message        +=  "<数量" + idx + ">" + "&nbsp;&nbsp;" + wdd.suryo + "個<br>";

            }
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.DACHAKU:
            wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
            idx = 0;
            for (WorkDiaryDetail wdd : wdds) {
              idx++;
              timeLine.message        +=  "<資材" + idx + ">" + "&nbsp;&nbsp;" + Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)wdd.sizaiId, true) + "<br>";

            }
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.COMMENT:
            wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
            idx = 0;
            for (WorkDiaryDetail wdd : wdds) {
              idx++;
              timeLine.message        +=  "<コメント" + idx + ">" + "&nbsp;&nbsp;" + wdd.comment + "<br>";

            }
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.MALTI:
            timeLine.message       += "<使用マルチ> " + Common.GetCommonValue(Common.ConstClass.ITOMULTI, (int)workDiary.useMulti, true) + "<br>";
            timeLine.message       += "<列数> " + workDiary.retusu + "列" + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.TEISHOKU:
            wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
            idx = 0;
            for (WorkDiaryDetail wdd : wdds) {
              idx++;
              if (!wdd.naeNo.equals("")) {
                NaeStatus ns = NaeStatus.getStatusOfNae(wdd.naeNo);
                String[] naeNos = wdd.naeNo.split("-");
                timeLine.message     += "<苗" + idx + "> "     + ns.hinsyuName + "(" + naeNos[1] + ")" + "<br>";
              }
              else {
                String[] hinsyus = workDiary.hinsyuId.split(",");
                double hinsyuId = Double.parseDouble(hinsyus[idx - 1]);
                timeLine.message     += "<苗" + idx + "> "     + Hinsyu.getHinsyuName(hinsyuId) + "<br>";
              }
              timeLine.message       += "<個数" + idx + "> "     + wdd.kosu + "個" + "<br>";
              timeLine.message       += "<列数" + idx + "> "     + wdd.retusu + "列" + "<br>";
              timeLine.message       += "<条間" + idx + "> "     + wdd.joukan + "cm" + "<br>";
              timeLine.message       += "<条数" + idx + "> "     + wdd.jousu  + "列" + "<br>";
              timeLine.message       += "<作付距離" + idx + "> " + wdd.plantingDistance + "m" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";

            }
            break;
          case AgryeelConst.WorkTemplate.NAEHASHU:
            timeLine.message       += "<使用穴数> " + workDiary.useHole + "穴" + "<br>";
            timeLine.message       += "<枚数> " + workDiary.maisu + "枚" + "<br>";
            timeLine.message       += "<使用培土> " + Common.GetCommonValue(Common.ConstClass.ITOBAIDO, (int)workDiary.useBaido, true) + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.SENTEI:
            timeLine.message       += "<剪定高> " + workDiary.senteiHeight + "cm" + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.MABIKI:
            timeLine.message       += "<仕立本数> " + workDiary.shitateHonsu + "本" + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.NICHOCHOSEI:
            timeLine.message       += "<日長> " + workDiary.nicho + "時間" + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.SENKA:
            wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
            idx = 0;
            for (WorkDiaryDetail wdd : wdds) {
              if (wdd.syukakuKosu == 0 && wdd.syukakuHakosu == 0) {
                continue;
              }
              idx++;
              timeLine.message       += "<等級" + idx + "> "       + Shitu.getShituName(wdd.syukakuSitsu) + "<br>";
              timeLine.message       += "<階級" + idx + "> "   + Size.getSizeName(wdd.syukakuSize) + "<br>";
              timeLine.message       += "<箱数" + idx + "> "   + wdd.syukakuHakosu + "ケース" + "<br>";
              timeLine.message       += "<本数" + idx + "> "   + wdd.syukakuKosu + "本" + "<br>";
              timeLine.message       += "<人数" + idx + "> "   + wdd.syukakuNinzu + "人" + "<br>";
              timeLine.message       += "<収穫量" + idx + "> " + wdd.shukakuRyo + "Kg" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
            }
            break;
          case AgryeelConst.WorkTemplate.HAIKI:
            timeLine.message       += "<廃棄量> " + workDiary.haikiRyo + "Kg" + "<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
            break;
          }

          //作業時間をメッセージに追加する
          SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
          if (workDiary.workStartTime != null) {
            timeLine.message += "【 開始時間 】 " + sdf.format(workDiary.workStartTime);
          }
          if (workDiary.workEndTime != null) {
            timeLine.message += "【 終了時間 】 " + sdf.format(workDiary.workEndTime);
          }
          //ヘルスケアデータをメッセージに追加する
          if (
              (workDiary.numberOfSteps  != 0)
              || (workDiary.distance    != 0)
              || (workDiary.calorie     != 0)
              || (workDiary.heartRate   != 0)
              ) {
            timeLine.message += "<br>【 歩数 】 " + workDiary.numberOfSteps + "歩<br>";
            timeLine.message += "【 距離 】 " + workDiary.distance + "m<br>";
            timeLine.message += "【 カロリー 】 " + workDiary.calorie + "kcal<br>";
            timeLine.message += "【 心拍数 】 " + workDiary.heartRate + "/bpm<br>";
            timeLine.message       +=  "--------------------------------------------------<br>";
          }

          timeLine.workDiaryId      = workDiary.workDiaryId;                            //作業記録ID
          timeLine.timeLineColor      = work.workColor;                               //タイムラインカラー
          timeLine.workId         = work.workId;                                //作業ID
          timeLine.workName       = work.workName;                                //作業名
          timeLine.workDate       = workDiary.workDate;                             //作業日
          timeLine.numberOfSteps  = workDiary.numberOfSteps;                        //歩数
          timeLine.distance       = workDiary.distance;                             //距離
          timeLine.calorie        = workDiary.calorie;                              //カロリー
          timeLine.heartRate      = workDiary.heartRate;                            //心拍数
          timeLine.kukakuId       = compartment.kukakuId;                           //区画ID
          timeLine.kukakuName       = compartment.kukakuName;                           //区画名
          timeLine.accountId        = account.accountId;                              //アカウントID
          timeLine.accountName      = account.acountName;                             //アカウント名
          timeLine.workStartTime  = workDiary.workStartTime;                           //作業開始時間
          timeLine.workEndTime    = workDiary.workEndTime;                             //作業終了時間
          timeLine.farmId         = account.farmId;                               //農場ID
          timeLine.workPlanFlag   = workDiary.workPlanFlag;                       //作業計画フラグ

          timeLine.save();                                                //タイムラインを追加

//----- スレッド化による非同期処理に変更 -----
//              /* 元帳照会を更新する */
//              MotochoCompornent motochoCompornent = new MotochoCompornent(workDiary.kukakuId);
//              motochoCompornent.make();

//            /* 区画状況照会を更新する */
//            CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(workDiary.kukakuId, workDiary.workId);
//            compartmentStatusCompornent.wdsps   = wdsps;
//            compartmentStatusCompornent.wdDate  = workDiary.workDate;
//            compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);
//            /* 農肥使用回数を再集計する */
//            Logger.info("NOUHI START.");
//            NouhiComprtnent.updateUseCount(account.farmId);
//            Logger.info("NOUHI END.");
//            /* 播種回数を再集計する */
//            Logger.info("HASHU START.");
//            HashuCompornent.updateUseCount(account.farmId);
//            Logger.info("HASHU END.");

          wdt.account   = account;
          wdt.wdspss.add(wdsps);
          wdt.workDiarys.add(workDiary);

          if (workDiaryId == 0) {
            workDiaryId = workDiary.workDiaryId;
          }

        }
        //wdt.start();

        //全て正常終了の場合、作業中情報を初期化
        UserComprtnent accountComprtnent = new UserComprtnent();                          //入力回数を更新する
        accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        accountComprtnent.InputCountUP();

        //作業記録後の判定
        resultJson.put("workDiaryId", workDiaryId);
        resultJson.put("workCommitAfter", accountComprtnent.accountStatusData.workCommitAfter);

        int mode = input.get("mode").asInt();
        if (mode == AgryeelConst.WorkDiaryMode.WORKING) { // 作業中の場合のみ
          int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
          Account ac = accountComprtnent.accountData;

          //-- インスタンスメソッド化
//          ac.workId   = 0;
//          ac.fieldId  = 0;
//          ac.workStartTime = null;
//          ac.notificationMessage = "";
//          ac.messageIcon = AgryeelConst.MessageIcon.NONE;
//          ac.workPlanId = 0;
          ac.clearWorkingInfo();
          ac.update();
        }

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONSUBMIT, String.format("[workDiaryId]=%f", workDiaryId));

        Ebean.commitTransaction();
        wdt.start();
      }
      catch (Exception e) {
        Logger.error(e.getMessage(),e);
        e.printStackTrace();
        Ebean.rollbackTransaction();
      }
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
    }
    /**
     * 【AGRYEEL】作業計画記録開始
     * @return
     */
    public static Result submitWorkPlan() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        updateWorkPlan(resultJson, input);

        return ok(resultJson);
    }
    public static void updateWorkPlan(ObjectNode resultJson, JsonNode input) {
      updateWorkPlan(resultJson, input, false);
    }
    public static void updateWorkPlan(ObjectNode resultJson, JsonNode input, boolean api) {

      try {

        WorkDiaryThread  wdt  = new WorkDiaryThread();
        Ebean.beginTransaction();
        int workId = Integer.parseInt(input.get("workId").asText());  //作業IDの取得
        int workPlanId = Integer.parseInt(input.get("workPlanId").asText());  //作業計画ＩＤの取得

        /*-- 取得した項目を作業日誌に格納する --*/
        Work work = Work.find.where().eq("work_id", workId).findUnique();                       //作業情報モデルの取得
        String sKukakuId = input.get("workKukaku").asText();                                    //複数区画同時作業記録
        String[] sKukakuIds = sKukakuId.split(",");
        double workDiaryId = 0;
        DecimalFormat df = new DecimalFormat("#,##0.00");

        //----- 作業指示関係 -----
        SimpleDateFormat sdfu = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        DecimalFormat    dff  = new DecimalFormat("000000");
        int workPlanType = input.get("workPlanType").asInt();
        UserComprtnent myac = new UserComprtnent();
        myac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        Calendar system = Calendar.getInstance();

        for (String kukakuId : sKukakuIds) {
          models.WorkPlan workplan = new models.WorkPlan();                                    //作業日誌モデルの生成
          CommonWorkDiary cwd       = new CommonWorkDiary(session(), resultJson);               //作業記録共通項目

          /*-------------------------------------------------------------------*/
          /* 編集時は対象となる作業記録を一旦削除する                          */
          /*-------------------------------------------------------------------*/
          if (workPlanId != 0) {
            WorkPlanController.workPlanDelete(workPlanId);
          }

          /*-------------------------------------------------------------------*/
          /* 共通項目の反映                                                    */
          /*-------------------------------------------------------------------*/
          workplan.hinsyuId = "0";
          cwd.plan(input, workplan, work, Double.parseDouble(kukakuId));

          /*-------------------------------------------------------------------*/
          /* 作業別項目の反映                                                  */
          /*-------------------------------------------------------------------*/
          CommonWorkDiaryWork cwdk = CommonWorkDiaryWork.getCommonWorkDiaryWork((int)cwd.work.workTemplateId, session(), resultJson);        //作業記録作業別項目

          if (cwdk != null) {               //作業項目別コンポーネントが生成できた場合
            cwdk.apiFlg = api;
            cwdk.plan(input, workplan, work);    //作業別項目の生成

          }

          workplan.workPlanFlag = workPlanType;
          workplan.workPlanUUID = dff.format(myac.accountData.farmId) + sdfu.format(system.getTime()) + myac.accountData.accountId;
//          if (workplan.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKPLANCOMMIT || workplan.workPlanFlag == AgryeelConst.WORKPLANFLAG.AICAPLANCOMMIT) {
//            workplan.workStartTime = null;
//          }
          /* 作業記録をこのタイミングで保存する */
          workplan.save();

          /* -- プランラインを作成する -- */
          PlanLine planLine = new PlanLine();                                       //タイムラインモデルの生成
          Compartment compartment = Compartment.find.where().eq("kukaku_id", workplan.kukakuId).findUnique();   //区画情報モデルの取得
          Account account     = Account.find.where().eq("account_id", workplan.accountId).findUnique();         //アカウント情報モデルの取得

          planLine.workPlanId = workplan.workPlanId;

          planLine.updateTime       = DateU.getSystemTimeStamp();
          //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
          planLine.message        = "【" + work.workName + "情報】<br>";
          switch ((int)cwd.work.workTemplateId) {
          case AgryeelConst.WorkTemplate.NOMAL:
            planLine.message        += "";                                 //メッセージ
            break;
          case AgryeelConst.WorkTemplate.SANPU:

            List<WorkPlanSanpu> wdss = WorkDiarySanpu.getWorkPlanSanpuList(workplan.workPlanId);

            for (WorkPlanSanpu wds : wdss) {

              /* 農肥IDから農肥情報を取得する */
              Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  wds.nouhiId).findUnique();

              String sanpuName  = "";

              if (wds.sanpuMethod != 0) {
                  sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, wds.sanpuMethod) + "]";
              }

              String unit = nouhi.getUnitString();

              planLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df.format(wds.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
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
            List<WorkPlanDetail> wdds = WorkDiaryDetail.getWorkPlanDetailList(workplan.workPlanId);
            int idx = 0;
            for (WorkPlanDetail wdd : wdds) {
              if (wdd.shukakuRyo == 0) {
                continue;
              }
              idx++;
              planLine.message       += "<荷姿" + idx + "> "     + Nisugata.getNisugataName(wdd.syukakuNisugata) + "<br>";
              planLine.message       += "<質" + idx + "> "       + Shitu.getShituName(wdd.syukakuSitsu) + "<br>";
              planLine.message       += "<サイズ" + idx + "> "   + Size.getSizeName(wdd.syukakuSize) + "<br>";
              planLine.message       += "<個数" + idx + "> "   + wdd.syukakuKosu + "個" + "<br>";
              planLine.message       += "<収穫量" + idx + "> "   + wdd.shukakuRyo + "Kg" + "<br>";
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
            wdds = WorkDiaryDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wdd : wdds) {
              idx++;
              planLine.message        +=  "<数量" + idx + ">" + "&nbsp;&nbsp;" + wdd.suryo + "個<br>";

            }
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.DACHAKU:
            wdds = WorkDiaryDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wdd : wdds) {
              idx++;
              planLine.message        +=  "<資材" + idx + ">" + "&nbsp;&nbsp;" + Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)wdd.sizaiId, true) + "<br>";

            }
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.COMMENT:
            wdds = WorkDiaryDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wdd : wdds) {
              idx++;
              planLine.message        +=  "<コメント" + idx + ">" + "&nbsp;&nbsp;" + wdd.comment + "<br>";

            }
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.MALTI:
            planLine.message       += "<使用マルチ> " + Common.GetCommonValue(Common.ConstClass.ITOMULTI, (int)workplan.useMulti, true) + "<br>";
            planLine.message       += "<列数> " + workplan.retusu + "列" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          case AgryeelConst.WorkTemplate.TEISHOKU:
            wdds = WorkDiaryDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wdd : wdds) {
              idx++;
              if (!wdd.naeNo.equals("")) {
                NaeStatus ns = NaeStatus.getStatusOfNae(wdd.naeNo);
                String[] naeNos = wdd.naeNo.split("-");
                planLine.message     += "<苗" + idx + "> "     + ns.hinsyuName + "(" + naeNos[1] + ")" + "<br>";
              }
              else {
                String[] hinsyus = workplan.hinsyuId.split(",");
                double hinsyuId = Double.parseDouble(hinsyus[idx - 1]);
                planLine.message     += "<苗" + idx + "> "     + Hinsyu.getHinsyuName(hinsyuId) + "<br>";
              }
              planLine.message       += "<個数" + idx + "> "     + wdd.kosu + "個" + "<br>";
              planLine.message       += "<列数" + idx + "> "     + wdd.retusu + "列" + "<br>";
              planLine.message       += "<条間" + idx + "> "     + wdd.joukan + "cm" + "<br>";
              planLine.message       += "<条数" + idx + "> "     + wdd.jousu  + "列" + "<br>";
              planLine.message       += "<作付距離" + idx + "> " + wdd.plantingDistance + "m" + "<br>";
              planLine.message       +=  "--------------------------------------------------<br>";

            }
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
            wdds = WorkDiaryDetail.getWorkPlanDetailList(workplan.workPlanId);
            idx = 0;
            for (WorkPlanDetail wdd : wdds) {
              if (wdd.syukakuKosu == 0 && wdd.syukakuHakosu == 0) {
                continue;
              }
              idx++;
              planLine.message       += "<等級" + idx + "> "   + Shitu.getShituName(wdd.syukakuSitsu) + "<br>";
              planLine.message       += "<階級" + idx + "> "   + Size.getSizeName(wdd.syukakuSize) + "<br>";
              planLine.message       += "<箱数" + idx + "> "   + wdd.syukakuHakosu + "ケース" + "<br>";
              planLine.message       += "<個数" + idx + "> "   + wdd.syukakuKosu + "本" + "<br>";
              planLine.message       += "<人数" + idx + "> "   + wdd.syukakuNinzu + "人" + "<br>";
              planLine.message       += "<収穫量" + idx + "> " + wdd.shukakuRyo + "Kg" + "<br>";
              planLine.message       +=  "--------------------------------------------------<br>";

            }
            break;
          case AgryeelConst.WorkTemplate.HAIKI:
            planLine.message       += "<廃棄量> " + workplan.haikiRyo + "Kg" + "<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
            break;
          }

          //作業時間をメッセージに追加する
          SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
          if (workplan.workStartTime != null && (workplan.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH || workplan.workPlanFlag == AgryeelConst.WORKPLANFLAG.AICAPLANWATCH)) {
            planLine.message += "【 開始時間 】 " + sdf.format(workplan.workStartTime);
          }
          //ヘルスケアデータをメッセージに追加する
          if (
              (workplan.numberOfSteps  != 0)
              || (workplan.distance    != 0)
              || (workplan.calorie     != 0)
              || (workplan.heartRate   != 0)
              ) {
            planLine.message += "<br>【 歩数 】 " + workplan.numberOfSteps + "歩<br>";
            planLine.message += "【 距離 】 " + workplan.distance + "m<br>";
            planLine.message += "【 カロリー 】 " + workplan.calorie + "kcal<br>";
            planLine.message += "【 心拍数 】 " + workplan.heartRate + "/bpm<br>";
            planLine.message       +=  "--------------------------------------------------<br>";
          }

          planLine.planLineColor      = work.workColor;                             //プランラインカラー
          planLine.workId         = work.workId;                                    //作業ID
          planLine.workName       = work.workName;                                  //作業名
          planLine.workDate       = workplan.workDate;                              //作業日
          planLine.numberOfSteps  = workplan.numberOfSteps;                         //歩数
          planLine.distance       = workplan.distance;                              //距離
          planLine.calorie        = workplan.calorie;                               //カロリー
          planLine.heartRate      = workplan.heartRate;                             //心拍数
          planLine.kukakuId       = compartment.kukakuId;                           //区画ID
          planLine.kukakuName       = compartment.kukakuName;                       //区画名
          planLine.accountId        = account.accountId;                            //アカウントID
          planLine.accountName      = account.acountName;                           //アカウント名
          planLine.farmId         = account.farmId;                                 //農場ID
          planLine.workStartTime  = workplan.workStartTime;                         //作業開始時間
          planLine.workEndTime    = workplan.workEndTime;                           //作業収量時間
          planLine.workPlanFlag   = workplan.workPlanFlag;                          //作業計画フラグ
          planLine.workPlanUUID   = workplan.workPlanUUID;                          //作業計画UUID

          planLine.save();                                                          //タイムラインを追加

          if (workplan.workPlanFlag == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH || workplan.workPlanFlag == AgryeelConst.WORKPLANFLAG.AICAPLANWATCH) {
            UserComprtnent accountComprtnent = new UserComprtnent();
            int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
            Account ac = accountComprtnent.accountData;

            //-- インスタンスメソッド化
//            ac.workId   = work.workId;
//            ac.fieldId  = compartment.kukakuId;
//            ac.workStartTime = workplan.workStartTime;
//            ac.workPlanId = workplan.workPlanId;
            ac.setWorkingInfo( work.workId, compartment.kukakuId, workplan.workStartTime, workplan.workPlanId);
            ac.update();

            Compartment ct = Compartment.getCompartmentInfo(ac.fieldId);
            Work        wk = Work.getWork(ac.workId);

            //SystemMessage.makeOneMessage(ac.farmId, "[" + ac.acountName + "] " + ct.kukakuName + "で" + wk.workName + "を開始しました。");

            Logger.info("[ WORKING START ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{} STARTTIME:{}", ac.accountId, ac.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName, sdf.format(ac.workStartTime));

            session(AgryeelConst.SessionKey.WORKING_ACTION, "display");
            accountComprtnent.getAccountJson(resultJson);
          }
          ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONSUBMIT, String.format("[workPlanId]=%f", planLine.workPlanId));
        }
        Ebean.commitTransaction();
        wdt.start();
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
    public static Result getNouhiValue() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        double nouhiId = Double.parseDouble(input.get("nouhiId").asText());	//農肥IDの取得
        String kukakus = input.get("kukakuId").asText();
        String[] kukaku = kukakus.split(",");
        DecimalFormat df = new DecimalFormat("##0.00");

        /* 農肥IDから農肥情報を取得する */
        Nouhi nouhi = (Nouhi)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.NOUHI, "nouhi_id = " + nouhiId);
        double hosei = 1;
        if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
          hosei = 0.001;
        }
        double sanpuryo = nouhi.sanpuryo * hosei;

        if (kukaku.length > 0) {
          Compartment ct = Compartment.getCompartmentInfo(Double.parseDouble(kukaku[0]));
          if (ct != null && ct.area != 0) {
            sanpuryo = sanpuryo * 0.1 * ct.area;
          }
        }

        resultJson.put("bairitu"	, nouhi.bairitu);										//倍率
        resultJson.put("sanpuryo"	, df.format(sanpuryo));             //散布量
        resultJson.put("unit"		  , nouhi.unitKind);									//単位種別
        resultJson.put("lower"		, nouhi.lower);											//倍率下限
        resultJson.put("upper"		, nouhi.upper);											//倍率上限

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】作業計画記録完了
     * @return
     */
    public static Result planToDiary() {
      return planToDiaryCommit(true);
    }
    /**
     * 【AGRYEEL】作業計画記録完了
     * @return
     */
    public static Result planToDiaryTimeCommit() {
      return planToDiaryCommit(false);
    }
    /**
     * 【AGRYEEL】作業計画記録完了
     * @return
     */
    public static Result planToDiaryCommit(boolean timeFlag) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();

        try {

          Ebean.beginTransaction();
          WorkDiaryThread  wdt  = new WorkDiaryThread();
          UserComprtnent accountComprtnent = new UserComprtnent();
          int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
          Account ac = accountComprtnent.accountData;
          double kukakuId = 0;
          int workId      = 0;
          if (timeFlag) { //時間計測ありの場合
            workId = Integer.parseInt(input.get("workId").asText());  //作業IDの取得
            kukakuId = ac.fieldId;
          }
          else {
            models.WorkPlan workplan = models.WorkPlan.find.where().eq("work_plan_id", Double.parseDouble(input.get("planId").asText())).findUnique();
            if (workplan != null) {
              workId = (int)workplan.workId;
              kukakuId = workplan.kukakuId;
            }
          }
          Compartment ct = Compartment.getCompartmentInfo(kukakuId);
          Work        wk = Work.getWork(workId);
          ArrayList<WorkDiarySanpu> wdsps = new ArrayList<WorkDiarySanpu>();
          DecimalFormat df = new DecimalFormat("#,##0.00");

          Logger.info("[ PLANTODIARY ] ACCOUNTID={} WORKID={} PLANID={}", session(AgryeelConst.SessionKey.ACCOUNTID), workId);
          if (accountComprtnent.accountData != null) {
            Logger.info("[ PLANTODIARY ] FIELDID={} ", accountComprtnent.accountData.fieldId);
          }
          else {
            Logger.info("[ PLANTODIARY ] Account Get Error (NULL Jadge) ");
          }

          /*-- 取得した項目を作業日誌に格納する --*/
          Work work = Work.find.where().eq("work_id", workId).findUnique();                       //作業情報モデルの取得
          double workDiaryId = 0;
          Calendar cal = Calendar.getInstance();
          java.sql.Timestamp jst = new Timestamp(cal.getTimeInMillis());

          List<models.WorkPlan> workplans;
          if (timeFlag) { //時間計測ありの場合
            workplans = models.WorkPlan.find.where().eq("work_id", workId).eq("account_id", ac.accountId).disjunction().add(Expr.eq("work_plan_flag", AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH)).add(Expr.eq("work_plan_flag", AgryeelConst.WORKPLANFLAG.WORKPLANWATCH)).add(Expr.eq("work_plan_flag", AgryeelConst.WORKPLANFLAG.AICAPLANWATCH)).endJunction().orderBy("work_plan_id").findList();
          }
          else {
            workplans = models.WorkPlan.find.where().eq("work_plan_id", Double.parseDouble(input.get("planId").asText())).findList();
          }

          long to   = cal.getTime().getTime();

          for (models.WorkPlan workplan: workplans) {
            models.WorkDiary workDiary = new models.WorkDiary();
            if (workplan == null) {
              Logger.error("[ WORKING PLAN NODATA ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{}", ac.accountId, ac.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName);
            }
            if (timeFlag && workplan.workStartTime == null) {
              continue;
            }
            Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
            workDiaryId         = sequence.sequenceValue;

            //workDiary項目の設定
            workDiary.workDiaryId         = workDiaryId;
            workDiary.workId              = workplan.workId;
            workDiary.kukakuId            = workplan.kukakuId;
            workDiary.hillId              = workplan.hillId;
            workDiary.lineId              = workplan.lineId;
            workDiary.stockId             = workplan.stockId;
            workDiary.accountId           = workplan.accountId;
            workDiary.workDate            = workplan.workDate;
            if (timeFlag) { //時間計測ありの場合
              //作業時間の算出
              long from = workplan.workStartTime.getTime();
              long diff = ( to - from  ) / (1000 * 60 );
              long time = diff / workplans.size();
              workDiary.workTime            = (int)time;
            }
            else {
              workDiary.workTime            = workplan.workTime;
            }
            workDiary.shukakuRyo          = workplan.shukakuRyo;
            workDiary.detailSettingKind   = workplan.detailSettingKind;
            workDiary.combiId             = workplan.combiId;
            workDiary.kikiId              = workplan.kikiId;
            workDiary.attachmentId        = workplan.attachmentId;
            workDiary.hinsyuId            = workplan.hinsyuId;
            workDiary.beltoId             = workplan.beltoId;
            workDiary.kabuma              = workplan.kabuma;
            workDiary.joukan              = workplan.joukan;
            workDiary.jousu               = workplan.jousu;
            workDiary.hukasa              = workplan.hukasa;
            workDiary.kansuiPart          = workplan.kansuiPart;
            workDiary.kansuiSpace         = workplan.kansuiSpace;
            workDiary.kansuiMethod        = workplan.kansuiMethod;
            workDiary.kansuiRyo           = workplan.kansuiRyo;
            workDiary.syukakuNisugata     = workplan.syukakuNisugata;
            workDiary.syukakuSitsu        = workplan.syukakuSitsu;
            workDiary.syukakuSize         = workplan.syukakuSize;
            workDiary.kukakuStatusUpdate  = workplan.kukakuStatusUpdate;
            workDiary.motochoUpdate       = workplan.motochoUpdate;
            workDiary.workRemark          = workplan.workRemark;
            if (timeFlag) { //時間計測ありの場合
            workDiary.workStartTime       = workplan.workStartTime;
            }
            else {
              Calendar calStart = Calendar.getInstance();
              calStart.add(Calendar.MINUTE, -1 * workplan.workTime);
              workDiary.workStartTime     = new Timestamp(calStart.getTimeInMillis());
            }
            workDiary.workEndTime         = jst;
            workDiary.numberOfSteps       = workplan.numberOfSteps;
            workDiary.distance            = workplan.distance;
            workDiary.calorie             = workplan.calorie;
            workDiary.heartRate           = workplan.heartRate;
            workDiary.useMulti            = workplan.useMulti;
            workDiary.retusu              = workplan.retusu;
            workDiary.naemaisu            = workplan.naemaisu;
            workDiary.useHole             = workplan.useHole;
            workDiary.maisu               = workplan.maisu;
            workDiary.useBaido            = workplan.useBaido;
            workDiary.senteiHeight        = workplan.senteiHeight;
            workDiary.workPlanFlag        = workplan.workPlanFlag;
            workDiary.workPlanUUID        = workplan.workPlanUUID;
            workDiary.shitateHonsu        = workplan.shitateHonsu;
            workDiary.nicho               = workplan.nicho;
            workDiary.haikiRyo            = workplan.haikiRyo;

            CommonWorkDiaryWork cwdk = CommonWorkDiaryWork.getCommonWorkDiaryWork((int)wk.workTemplateId, session(), resultJson);        //作業記録作業別項目

            if (cwdk != null) {               //作業項目別コンポーネントが生成できた場合

              cwdk.saveHistry(workDiary);

            }

            //---------------------------------------------------------------------------------------------------------------------------------------------------------
            //----- 区画IDから区画状況情報を取得する                                                                                                                                                                                                                                                                                                      -----
            //---------------------------------------------------------------------------------------------------------------------------------------------------------
            CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(workDiary.kukakuId);
            CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();
//            if (compartmentStatusData != null) {
//              if (compartmentStatusData.katadukeDate != null) {
//                Date katadukeDate = DateUtils.truncate(compartmentStatusData.katadukeDate, Calendar.DAY_OF_MONTH);
//                Date workDate = DateUtils.truncate(workDiary.workDate, Calendar.DAY_OF_MONTH);
//                if (!workDate.before(katadukeDate)) {
//                  if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
//                    cws.workEndId = String.valueOf(wk.workId);
//                  }
//                  else {
//                    String workEndId = cws.workEndId;
//                    boolean endidExists = false;
//                    if (workEndId != null && !"".equals(workEndId)) {
//                      String[] endIds = workEndId.split(",");
//                      for (String endId : endIds) {
//                        if (Double.parseDouble(endId) == wk.workId) {
//                          endidExists = true;
//                          break;
//                        }
//                      }
//                      if (endidExists == false) {
//                        cws.workEndId += "," + String.valueOf(wk.workId);
//                      }
//                    }
//                    else {
//                      cws.workEndId = String.valueOf(wk.workId);
//                    }
//                  }
//                }
//                else if (workDate.compareTo(katadukeDate) == 0) {
//                  if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
//                    cws.workEndId = String.valueOf(wk.workId);
//                  }
//                }
//              }
//              else {
//                if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
//                  cws.workEndId = String.valueOf(wk.workId);
//                }
//              }
//              cws.update();
//            }
            //---------------------------------------------------------------------------------------------------------------------------------------------------------
            List<models.WorkPlanSanpu> wpss = models.WorkPlanSanpu.find.where().eq("work_plan_id", workplan.workPlanId).orderBy("work_diary_sequence asc").findList();
            WorkHistryBaseComprtnent whb = new WorkHistryBaseComprtnent(ac.farmId, wk.workId, cws.cropId);                       //作業履歴共通コンポーネントの作成
            for (models.WorkPlanSanpu wps : wpss) {
              models.WorkDiarySanpu wds = new WorkDiarySanpu();

              //WorkDiarySanpuの項目設定
              wds.workDiaryId         = workDiaryId;
              wds.workDiarySequence   = wps.workDiarySequence;
              wds.sanpuMethod         = wps.sanpuMethod;
              wds.kikiId              = wps.kikiId;
              wds.attachmentId        = wps.attachmentId;
              wds.nouhiId             = wps.nouhiId;
              wds.bairitu             = wps.bairitu;
              wds.sanpuryo            = wps.sanpuryo;
              wds.kukakuStatusUpdate  = wps.kukakuStatusUpdate;
              wds.motochoUpdate       = wps.motochoUpdate;
              wds.save();
              whb.stack(wds);         //散布情報を履歴情報として格納する
              //農肥履歴情報の更新
              Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  wds.nouhiId).findUnique();
              if (nouhi != null) {
                /* 農肥前回情報として保存する */
                nouhi.bairitu   = wds.bairitu;
                nouhi.sanpuryo  = wds.sanpuryo;
                nouhi.update();
              }
            }
            if (whb.iWorkHistryBaseCount > 0) {                                         //作業履歴共通情報が存在する場合

              whb.update();

            }

            List<models.WorkPlanDetail> wpds = models.WorkPlanDetail.find.where().eq("work_plan_id", workplan.workPlanId).orderBy("work_diary_sequence asc").findList();
            WorkHistryDetailComprtnent whd = new WorkHistryDetailComprtnent(ac.farmId, wk.workId, cws.cropId);                    //作業履歴詳細コンポーネントの作成
            double shukakuryo = 0;
            for (models.WorkPlanDetail wpd : wpds) {
              models.WorkDiaryDetail wdd = new WorkDiaryDetail();

              //WorkDiaryDetailの項目設定
              wdd.workDiaryId         = workDiaryId;
              wdd.workDiarySequence   = wpd.workDiarySequence;
              wdd.workDetailKind      = wpd.workDetailKind;
              wdd.suryo               = wpd.suryo;
              wdd.sizaiId             = wpd.sizaiId;
              wdd.comment             = wpd.comment;
              wdd.syukakuNisugata     = wpd.syukakuNisugata;
              wdd.syukakuSitsu        = wpd.syukakuSitsu;
              wdd.syukakuSize         = wpd.syukakuSize;
              wdd.syukakuKosu         = wpd.syukakuKosu;
              wdd.shukakuRyo          = wpd.shukakuRyo;
              wdd.syukakuHakosu       = wpd.syukakuHakosu;
              wdd.syukakuNinzu        = wpd.syukakuNinzu;
              wdd.naeNo               = wpd.naeNo;
              wdd.kosu                = wpd.kosu;
              wdd.retusu              = wpd.retusu;
              wdd.joukan              = wpd.joukan;
              wdd.jousu               = wpd.jousu;
              wdd.plantingDistance    = wpd.plantingDistance;
              shukakuryo             += wpd.shukakuRyo;
              wdd.save();
              whd.stack(wdd);         //詳細情報を履歴情報として格納する
            }
            if (whd.iWorkHistryDetailCount > 0) {                                         //作業履歴共通情報が存在する場合

              whd.update();

            }
            if (work.workTemplateId == AgryeelConst.WorkTemplate.SHUKAKU && shukakuryo > 0) {
              workDiary.shukakuRyo = shukakuryo;
            }
            workDiary.save();

            //作付開始自動連携を実施
            models.WorkDiary autoWorkDiary = WorkChainCompornent.makeAutoStart(workDiary);
            if ( autoWorkDiary != null ) {                                                                              //自動的に作付開始が生成された場合
              //スレッドの処理対象に加える
              ArrayList<WorkDiarySanpu> autoWorkDiarySanpu = new ArrayList<WorkDiarySanpu>();
              wdt.wdspss.add(autoWorkDiarySanpu);
              wdt.workDiarys.add(autoWorkDiary);
            }

            /* -- タイムラインを作成する -- */
            TimeLine timeLine = new TimeLine();                                                                       //タイムラインモデルの生成
            sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.TIMELINEID);                                //最新シーケンス値の取得
            Compartment compartment = Compartment.find.where().eq("kukaku_id", workDiary.kukakuId).findUnique();      //区画情報モデルの取得
            Account account     = Account.find.where().eq("account_id", workDiary.accountId).findUnique();            //アカウント情報モデルの取得

            timeLine.timeLineId       = sequence.sequenceValue;                           //タイムラインID

            timeLine.updateTime       = DateU.getSystemTimeStamp();
            //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
            timeLine.message        = "【" + work.workName + "情報】<br>";
            switch ((int)wk.workTemplateId) {
            case AgryeelConst.WorkTemplate.NOMAL:
              timeLine.message        += "";                                 //メッセージ
              break;
            case AgryeelConst.WorkTemplate.SANPU:

              List<WorkDiarySanpu> wdss = WorkDiarySanpu.getWorkDiarySanpuList(workDiary.workDiaryId);

              for (WorkDiarySanpu wds : wdss) {

                /* 農肥IDから農肥情報を取得する */
                Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  wds.nouhiId).findUnique();

                String sanpuName  = "";

                if (wds.sanpuMethod != 0) {
                    sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, wds.sanpuMethod) + "]";
                }

                String unit = nouhi.getUnitString();

                timeLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df.format(nouhi.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
                timeLine.message       +=  "--------------------------------------------------<br>";

                wdsps.add(wds);

              }

              break;
            case AgryeelConst.WorkTemplate.HASHU:
              timeLine.message       += "<品種> " + Hinsyu.getMultiHinsyuName(workDiary.hinsyuId) + "<br>";
              timeLine.message       += "<株間> " + workDiary.kabuma + "cm<br>";
              timeLine.message       += "<条間> " + workDiary.joukan + "cm<br>";
              timeLine.message       += "<条数> " + workDiary.jousu  + "cm<br>";
              timeLine.message       += "<深さ> " + workDiary.hukasa + "cm<br>";
              timeLine.message       += "<機器> " + Kiki.getKikiName(workDiary.kikiId) + "<br>";
              timeLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workDiary.attachmentId) + "<br>";
              timeLine.message       += "<ベルト> " + Belto.getBeltoName(workDiary.beltoId) + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.SHUKAKU:
              List<WorkDiaryDetail> wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
              int idx = 0;
              for (WorkDiaryDetail wdd : wdds) {
                if (wdd.shukakuRyo == 0) {
                  continue;
                }
                idx++;
                timeLine.message       += "<荷姿" + idx + "> "     + Nisugata.getNisugataName(wdd.syukakuNisugata) + "<br>";
                timeLine.message       += "<質" + idx + "> "       + Shitu.getShituName(wdd.syukakuSitsu) + "<br>";
                timeLine.message       += "<サイズ" + idx + "> "   + Size.getSizeName(wdd.syukakuSize) + "<br>";
                timeLine.message       += "<個数" + idx + "> "   + wdd.syukakuKosu + "個" + "<br>";
                timeLine.message       += "<収穫量" + idx + "> "   + wdd.shukakuRyo + "Kg" + "<br>";
                timeLine.message       +=  "--------------------------------------------------<br>";

              }
              if (idx == 0) {
                timeLine.message       += "<荷姿> "     + Nisugata.getNisugataName(workDiary.syukakuNisugata) + "<br>";
                timeLine.message       += "<質> "       + Shitu.getShituName(workDiary.syukakuSitsu) + "<br>";
                timeLine.message       += "<サイズ> "   + Size.getSizeName(workDiary.syukakuSize) + "<br>";
                timeLine.message       += "<収穫量> "   + workDiary.shukakuRyo + "Kg" + "<br>";
                timeLine.message       +=  "--------------------------------------------------<br>";
              }
              break;
            case AgryeelConst.WorkTemplate.NOUKO:
              timeLine.message       += "<機器> " + Kiki.getKikiName(workDiary.kikiId) + "<br>";
              timeLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workDiary.attachmentId) + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.KANSUI:
              timeLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, workDiary.kansuiMethod) + "<br>";
              timeLine.message       += "<機器> " + Kiki.getKikiName(workDiary.kikiId) + "<br>";
              timeLine.message       += "<潅水量> " + workDiary.kansuiRyo + "L" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.KAISHU:
              wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
              idx = 0;
              for (WorkDiaryDetail wdd : wdds) {
                idx++;
                timeLine.message        +=  "<数量" + idx + ">" + "&nbsp;&nbsp;" + wdd.suryo + "個<br>";

              }
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.DACHAKU:
              wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
              idx = 0;
              for (WorkDiaryDetail wdd : wdds) {
                idx++;
                timeLine.message        +=  "<資材" + idx + ">" + "&nbsp;&nbsp;" + Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)wdd.sizaiId, true) + "<br>";

              }
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.COMMENT:
              wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
              idx = 0;
              for (WorkDiaryDetail wdd : wdds) {
                idx++;
                timeLine.message        +=  "<コメント" + idx + ">" + "&nbsp;&nbsp;" + wdd.comment + "<br>";

              }
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.MALTI:
              timeLine.message       += "<使用マルチ> " + Common.GetCommonValue(Common.ConstClass.ITOMULTI, (int)workDiary.useMulti, true) + "<br>";
              timeLine.message       += "<列数> " + workDiary.retusu + "列" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.TEISHOKU:
              wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
              idx = 0;
              for (WorkDiaryDetail wdd : wdds) {
                idx++;
                if (!wdd.naeNo.equals("")) {
                  NaeStatus ns = NaeStatus.getStatusOfNae(wdd.naeNo);
                  String[] naeNos = wdd.naeNo.split("-");
                  timeLine.message     += "<苗" + idx + "> "     + ns.hinsyuName + "(" + naeNos[1] + ")" + "<br>";
                }
                else {
                  String[] hinsyus = workDiary.hinsyuId.split(",");
                  double hinsyuId = Double.parseDouble(hinsyus[idx - 1]);
                  timeLine.message     += "<苗" + idx + "> "     + Hinsyu.getHinsyuName(hinsyuId) + "<br>";
                }
                timeLine.message       += "<個数" + idx + "> "     + wdd.kosu + "個" + "<br>";
                timeLine.message       += "<列数" + idx + "> "     + wdd.retusu + "列" + "<br>";
                timeLine.message       += "<条間" + idx + "> "     + wdd.joukan + "cm" + "<br>";
                timeLine.message       += "<条数" + idx + "> "     + wdd.jousu  + "列" + "<br>";
                timeLine.message       += "<作付距離" + idx + "> " + wdd.plantingDistance + "m" + "<br>";
                timeLine.message       +=  "--------------------------------------------------<br>";

              }
              break;
            case AgryeelConst.WorkTemplate.NAEHASHU:
              timeLine.message       += "<使用穴数> " + workDiary.useHole + "穴" + "<br>";
              timeLine.message       += "<枚数> " + workDiary.maisu + "枚" + "<br>";
              timeLine.message       += "<使用培土> " + Common.GetCommonValue(Common.ConstClass.ITOBAIDO, (int)workDiary.useBaido, true) + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.SENTEI:
              timeLine.message       += "<剪定高> " + workDiary.senteiHeight + "cm" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.MABIKI:
              timeLine.message       += "<仕立本数> " + workDiary.shitateHonsu + "本" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.NICHOCHOSEI:
              timeLine.message       += "<日長> " + workDiary.nicho + "時間" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            case AgryeelConst.WorkTemplate.SENKA:
              wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
              idx = 0;
              for (WorkDiaryDetail wdd : wdds) {
                if (wdd.syukakuKosu == 0 && wdd.syukakuHakosu == 0) {
                  continue;
                }
                idx++;
                timeLine.message       += "<等級" + idx + "> "   + Shitu.getShituName(wdd.syukakuSitsu) + "<br>";
                timeLine.message       += "<階級" + idx + "> "   + Size.getSizeName(wdd.syukakuSize) + "<br>";
                timeLine.message       += "<箱数" + idx + "> "   + wdd.syukakuHakosu + "ケース" + "<br>";
                timeLine.message       += "<本数" + idx + "> "   + wdd.syukakuKosu + "本" + "<br>";
                timeLine.message       += "<人数" + idx + "> "   + wdd.syukakuNinzu + "人数" + "<br>";
                timeLine.message       += "<収穫量" + idx + "> " + wdd.shukakuRyo + "Kg" + "<br>";
                timeLine.message       +=  "--------------------------------------------------<br>";

              }
              break;
            case AgryeelConst.WorkTemplate.HAIKI:
              timeLine.message       += "<廃棄量> " + workDiary.haikiRyo + "Kg" + "<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
              break;
            }

            //作業時間をメッセージに追加する
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd HH:mm");
            if (workDiary.workStartTime != null) {
              timeLine.message += "【 開始時間 】 " + sdf.format(workDiary.workStartTime);
            }
            if (workDiary.workEndTime != null) {
              timeLine.message += "【 終了時間 】 " + sdf.format(workDiary.workEndTime);
            }
            //ヘルスケアデータをメッセージに追加する
            if (
                (workDiary.numberOfSteps  != 0)
                || (workDiary.distance    != 0)
                || (workDiary.calorie     != 0)
                || (workDiary.heartRate   != 0)
                ) {
              timeLine.message += "<br>【 歩数 】 " + workDiary.numberOfSteps + "歩<br>";
              timeLine.message += "【 距離 】 " + workDiary.distance + "m<br>";
              timeLine.message += "【 カロリー 】 " + workDiary.calorie + "kcal<br>";
              timeLine.message += "【 心拍数 】 " + workDiary.heartRate + "/bpm<br>";
              timeLine.message       +=  "--------------------------------------------------<br>";
            }

            timeLine.workDiaryId      = workDiary.workDiaryId;                            //作業記録ID
            timeLine.timeLineColor      = work.workColor;                               //タイムラインカラー
            timeLine.workId         = work.workId;                                //作業ID
            timeLine.workName       = work.workName;                                //作業名
            timeLine.workDate       = workDiary.workDate;                             //作業日
            timeLine.numberOfSteps  = workDiary.numberOfSteps;                        //歩数
            timeLine.distance       = workDiary.distance;                             //距離
            timeLine.calorie        = workDiary.calorie;                              //カロリー
            timeLine.heartRate      = workDiary.heartRate;                            //心拍数
            timeLine.kukakuId       = compartment.kukakuId;                           //区画ID
            timeLine.kukakuName       = compartment.kukakuName;                           //区画名
            timeLine.accountId        = account.accountId;                              //アカウントID
            timeLine.accountName      = account.acountName;                             //アカウント名
            timeLine.farmId         = account.farmId;                               //農場ID
            timeLine.workPlanFlag   = workDiary.workPlanFlag;                       //作業計画フラグ
            timeLine.workPlanUUID   = workDiary.workPlanUUID;                       //作業計画UUID

            timeLine.save();                                                //タイムラインを追加

            resultJson.put("message", timeLine.message);
            resultJson.put("workTime", workDiary.workTime);

//            /* 元帳照会を更新する */
//            MotochoCompornent motochoCompornent = new MotochoCompornent(workDiary.kukakuId);
//            motochoCompornent.make();
//
//            /* 区画状況照会を更新する */
//            CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(workDiary.kukakuId, workDiary.workId);
//            compartmentStatusCompornent.wdsps   = wdsps;
//            compartmentStatusCompornent.wdDate  = workDiary.workDate;
//            compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);
//
//            /* 農肥使用回数を再集計する */
//            NouhiComprtnent.updateUseCount(account.farmId);
//            /* 播種回数を再集計する */
//            HashuCompornent.updateUseCount(account.farmId);

            wdt.account   = account;
            wdt.wdspss.add(wdsps);
            wdt.workDiarys.add(workDiary);

            if (workDiaryId == 0) {
              workDiaryId = workDiary.workDiaryId;
            }

            Logger.info("[WorkPlan DELETE] workPlanId={}", workplan.workPlanId);

            Ebean.createSqlUpdate("DELETE FROM work_plan WHERE work_plan_id = :workPlanId")
            .setParameter("workPlanId", workplan.workPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM work_plan_sanpu WHERE work_plan_id = :workPlanId")
            .setParameter("workPlanId", workplan.workPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM plan_line WHERE work_plan_id = :workPlanId")
            .setParameter("workPlanId", workplan.workPlanId).execute();

            Ebean.createSqlUpdate("DELETE FROM work_plan_detail WHERE work_plan_id = :workPlanId")
            .setParameter("workPlanId", workplan.workPlanId).execute();

            ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONSUBMIT, String.format("[workPlanId]=%f -> [workDiaryId]=%f", workplan.workPlanId, workDiary.workDiaryId));

          }

          //wdt.start();

          //SystemMessage.makeOneMessage(ac.farmId, "[" + ac.acountName + "] " + ct.kukakuName + "の" + wk.workName + "を終了しました。");

          //-- インスタンスメソッド化
//          ac.workId   = 0;
//          ac.fieldId  = 0;
//          ac.workStartTime = null;
//          ac.notificationMessage = "";
//          ac.messageIcon = AgryeelConst.MessageIcon.NONE;
//          ac.workPlanId = 0;
          ac.clearWorkingInfo();
          ac.update();

          Ebean.commitTransaction();
          wdt.start();

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
                    //【AICA】：アタッチメントから作業ＩＤ削除
                    //attachmentJson.put("workId"					, attachmentData.workId);			//作業ID

                    listJson.put(String.valueOf(attachmentData.attachmentId), attachmentJson);

                }

            }

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.ATTACHMENT, listJson);

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】作業前回情報を反映する
     * @param wk
     */
    private static void updateWorkLastTime(Work wk) {

    	WorkLastTime wlt = new WorkLastTime();

    	//【AICA】TODO:ワークチェインによる全体的な見直し
//    	/*----- 作業共通情報を設定 -----*/
//    	wlt.farmId		= Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));
//    	wlt.workId		= wk.workId;
//    	wlt.workTime	= wk.workTime;
//
//    	/*----- 作業別情報を設定 -----*/
//    	switch ((int)wk.workId) {
//			case AgryeelConst.WorkInfo.HASHU:
//      case AgryeelConst.WorkInfo.TEISYOKU:
//		    	wlt.kabuma			= wk.kabuma;
//		    	wlt.joukan			= wk.joukan;
//		    	wlt.jousu			= wk.jousu;
//		    	wlt.hukasa			= wk.hukasa;
//		    	wlt.kikiId			= wk.kikiId;
//		    	wlt.attachmentId	= wk.attachmentId;
//		    	wlt.hinsyuId		= wk.hinsyuId;
//		    	wlt.beltoId			= wk.beltoId;
//				break;
//			case AgryeelConst.WorkInfo.KANSUI:
//		    	wlt.kansuiRyo		= wk.kansuiRyo;
//		    	wlt.kansuiPart		= wk.kansuiPart;
//		    	wlt.kansuiSpace		= wk.kansuiSpace;
//		    	wlt.kansuiMethod	= wk.kansuiMethod;
//				break;
//			case AgryeelConst.WorkInfo.SHUKAKU:
//		    	wlt.shukakuRyo		= wk.shukakuRyo;
//		    	wlt.syukakuNisugata	= wk.syukakuNisugata;
//		    	wlt.syukakuSitsu	= wk.syukakuSitsu;
//		    	wlt.syukakuSize		= wk.syukakuSize;
//				break;
//
//		default:
//			break;
//		}
//
//        /* 現在保存されている作業前回情報を削除する */
//        Ebean.createSqlUpdate("DELETE FROM work_last_time WHERE farm_id = :farmId AND work_id = :workId")
//        .setParameter("farmId", wlt.farmId).setParameter("workId", wlt.workId).execute();
//
//        wlt.save();

    }

    /**
     * 【AGRYEEL】作業記録を削除します
     * @return
     */
    public static Result workDiaryDelete(double workDiaryId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        int naeCnt = 0;
        String naeNo = "";

        models.WorkDiary workDiary = models.WorkDiary.find.where().eq("work_diary_id", workDiaryId).findUnique();

        if (workDiary != null) {

          WorkDiaryThread  wdt  = new WorkDiaryThread();
          UserComprtnent uc = new UserComprtnent();
          int getAccount = uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

          Compartment ct = Compartment.getCompartmentInfo(workDiary.kukakuId);
          Work wk = Work.getWork(workDiary.workId);

          /* 苗状況照会更新 */
        	List<WorkDiaryDetail> aryWorkDetail = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
        	for (WorkDiaryDetail workDiaryDetail : aryWorkDetail) {
          	if (workDiaryDetail.naeNo != null && !workDiaryDetail.naeNo.equals("")) {
    					if (naeNo.indexOf(workDiaryDetail.naeNo) >= 0) {
    						continue;
    					}
    					if (naeCnt > 0) {
    						naeNo = naeNo + ",";
    					}
    					naeNo = naeNo + workDiaryDetail.naeNo;
    					naeCnt++;
    				}
    			}

          workDiaryDeleteCommit(workDiaryId);

          Logger.info("[ WORKDIARY DELETED ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{} WORKDIARYID:{}", uc.accountData.accountId, uc.accountData.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName, workDiaryId);

    			if (!naeNo.equals("")) {
    				String[] sNaeNos = naeNo.split(",");
    				for (String nae : sNaeNos) {
    					/* 元帳照会を更新する */
    					NaeMotochoCompornent motochoCompornent = new NaeMotochoCompornent(nae);
    					motochoCompornent.make();

    					/* 苗状況照会を更新する */
    					NaeStatusCompornent naeStatusCompornent = new NaeStatusCompornent(nae);
    					naeStatusCompornent.idsps   = null;
    					naeStatusCompornent.wdDate  = workDiary.workDate;
    					naeStatusCompornent.update(motochoCompornent.lastMotochoBase);
    				}
    			}

          wdt.account   = uc.accountData;
          wdt.workDiarys.add(workDiary);
          wdt.start();
        }

        return ok(resultJson);
    }

    private static void workDiaryDeleteCommit(double workDiaryId) {

        /*-------------------------------------------------------------------*/
        /* 作業記録、作業散布、タイムラインを削除する                        */
        /*-------------------------------------------------------------------*/
        models.WorkDiary wd =  models.WorkDiary.getWorkDiaryById(workDiaryId);
        Ebean.createSqlUpdate("DELETE FROM work_diary WHERE work_diary_id = :workDiaryId")
        .setParameter("workDiaryId", workDiaryId).execute();

        Ebean.createSqlUpdate("DELETE FROM work_diary_sanpu WHERE work_diary_id = :workDiaryId")
        .setParameter("workDiaryId", workDiaryId).execute();

        Ebean.createSqlUpdate("DELETE FROM time_line WHERE work_diary_id = :workDiaryId")
        .setParameter("workDiaryId", workDiaryId).execute();

        Ebean.createSqlUpdate("DELETE FROM work_diary_detail WHERE work_diary_id = :workDiaryId")
        .setParameter("workDiaryId", workDiaryId).execute();
//ここでは元帳照会と区画の再集計は行わない
//        if (wd != null) {
//          /* 元帳照会を更新する */
//          MotochoCompornent motochoCompornent = new MotochoCompornent(wd.kukakuId);
//          motochoCompornent.make();
//
//          /* 区画状況照会を更新する */
//          CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(wd.kukakuId, wd.workId);
//          compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);
//
//        }

        ActiveLog.commit(session(AgryeelConst.SessionKey.ACCOUNTID), SCREENID, ACTIONDELETE, String.format("[workDiaryId]=%f", workDiaryId));

    }
    /**
     * 【AGRYEEL】農肥散布チェック
     * @return
     */
    public static Result checkNouhi() {

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
        String sKukakuId = input.get("workKukaku").asText();                                    //複数区画同時作業記録
        String[] sKukakuIds = sKukakuId.split(",");
        for (String skukakuId : sKukakuIds) {
          for (int nouhiIndex = 0; nouhiIndex < nouhiInfoList.size(); nouhiIndex++) {
            double  nouhiId   = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("nouhiId").asText());        //農肥IDを取得する
            double  kukakuId  = Double.valueOf(skukakuId);
            param.NouhiCheckParm ncp = new param.NouhiCheckParm(nouhiId, kukakuId, "", workDate);
            result |= NouhiComprtnent.nouhiCheck(ncp);
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
