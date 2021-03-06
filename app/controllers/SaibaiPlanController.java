package controllers;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.Account;
import models.Attachment;
import models.Belto;
import models.Common;
import models.Compartment;
import models.Crop;
import models.Hinsyu;
import models.Kiki;
import models.MotochoBase;
import models.Nisugata;
import models.Nouhi;
import models.PlanLine;
import models.SaibaiPlanGoal;
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
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.MonthWeekList;
import compornent.SaibaiPlanHinsyuList;
import compornent.SaibaiPlanHinsyuList.SaibaiPlanHinsyu;
import compornent.UserComprtnent;

import consts.AgryeelConst;

public class SaibaiPlanController extends Controller {

  private static class CompareRinsaku implements Comparator<JsonNode> {
    public int compare(JsonNode a, JsonNode b) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      try {
        java.util.Date ad = sdf.parse(a.get("hashu").asText());
        java.util.Date bd = sdf.parse(b.get("hashu").asText());
        return ad.compareTo(bd);
      } catch (ParseException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      }
      return 0;
    }
  }
    /**
     * 【AICA】栽培計画画面への遷移
     */
    public static Result move() {
        return ok(views.html.saibaiplan.render());
    }
    /**
     * 【AICA】栽培計画実行
     */
    public static Result makePlan() {

      ObjectNode resultJson   = Json.newObject();
      ObjectNode cropList     = Json.newObject();
      ObjectNode cropGoalList = Json.newObject();
      ObjectNode goalList     = Json.newObject();

      //----------------------------------------------------------------------------
      //- 栽培計画ヘッダ情報の生成
      //----------------------------------------------------------------------------
      SimpleDateFormat sdff = new SimpleDateFormat("yyyy/MM/dd");

      int year = Calendar.getInstance().get(Calendar.YEAR);
      Calendar start = Calendar.getInstance();
      start.set(Calendar.YEAR, year);
      start.set(Calendar.DAY_OF_MONTH, 1);
      Calendar end   = Calendar.getInstance();
      end.set(Calendar.YEAR, year);
      end.set(Calendar.DAY_OF_MONTH, 1);
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      Calendar target = Calendar.getInstance();

      ObjectNode monthList = Json.newObject();
      for (int month = 0; month < 12; month++) {
        ObjectNode monthJson = Json.newObject();
        cal.set(Calendar.MONTH, month);
        monthJson.put("month", (month + 1));
        monthJson.put("weekcount", cal.getActualMaximum(Calendar.WEEK_OF_MONTH));
        monthList.put(String.valueOf((month + 1)), monthJson);
      }
      resultJson.put("month", monthList);
      //----------------------------------------------------------------------------
      //- 栽培計画の生成
      //----------------------------------------------------------------------------
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
      Account account = accountComprtnent.accountData;
      double farmId = account.farmId;
      boolean existData = false;
      long listIdx = 0;

      JsonNode input = request().body().asJson();
      JsonNode datalist = input.get("datalist");
      try {
        List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
        List<Double> keys = new ArrayList<Double>();
        for (Compartment ct : cts) {
          keys.add(new Double(ct.kukakuId));
        }
        //----------------------------------------------------------------------------
        //- 条件リストを通常と輪作に分ける
        //----------------------------------------------------------------------------
        List<JsonNode> nomalJoken   = new ArrayList<JsonNode>();
        List<JsonNode> rinsakuJoken = new ArrayList<JsonNode>();
        for (JsonNode info : datalist) {
          if (info.get("rinsaku").asInt() == 0) {
            nomalJoken.add(info);
          }
          else {
            rinsakuJoken.add(info);
          }
        }
        //----------------------------------------------------------------------------
        //- 通常栽培計画の場合
        //----------------------------------------------------------------------------
        for (JsonNode info : nomalJoken) {
          double cropId = Double.parseDouble(info.get("cropId").asText());
          String hinsyuId = info.get("hinsyuId").asText();
          String hashuString = info.get("hashu").asText();
          ObjectNode rotationList = Json.newObject();
          boolean init = true;
          listIdx      = 0;
          cal.set(Calendar.YEAR, year);
          Logger.info("[makePlan]--------------------------------------------------------------------------");
          while (year == cal.get(Calendar.YEAR)) {
            if ("".equals(info.get("hinsyuId").asText())) { //品種が未選択の場合
              //----- 最大収穫量の品種を取得する -----
              if (init) {
                start.setTime(sdff.parse(hashuString));
                start.add(Calendar.WEEK_OF_MONTH, -2);
                end.setTime(sdff.parse(hashuString));
                end.add(Calendar.WEEK_OF_MONTH, 2);
                cal.setTime(sdff.parse(hashuString));
              }
              else {
                cal.add(Calendar.DAY_OF_MONTH, info.get("sakuma").asInt());
                start.setTime(cal.getTime());
                start.add(Calendar.WEEK_OF_MONTH, -2);
                end.setTime(cal.getTime());
                end.add(Calendar.WEEK_OF_MONTH, 2);
              }
              SaibaiPlanHinsyuList spl = new SaibaiPlanHinsyuList();
              for (int prev = 0; prev < 10; prev++) {
                start.add(Calendar.YEAR , -1 * prev);
                end.add(Calendar.YEAR   , -1 * prev);
                cal.add(Calendar.YEAR   , -1 * prev);
                List<MotochoBase> datas = MotochoBase.find.where().eq("crop_id", cropId).in("kukaku_id", keys).between("hashu_date", start.getTime(), end.getTime()).findList();
                for(MotochoBase data: datas) {
                  if (data.hinsyuId == null) {
                    continue;
                  }
                  if (data.hashuDate == null || data.shukakuStartDate == null) {
                    continue;
                  }
                  String[] hinsyuIds = data.hinsyuId.split(",");
                  spl.add(Double.parseDouble(hinsyuIds[0]));
                  spl.put(Double.parseDouble(hinsyuIds[0]), data.totalShukakuCount);
                }
              }
              DecimalFormat fHinsyu = new DecimalFormat("##0");
              hinsyuId = fHinsyu.format(spl.getMaxHinshu());
            }
            Logger.info("[makePlan] JOKEN DATA >>> crop={} hashu={} hinsyu={}", cropId, hashuString, hinsyuId);
            //----- 過去10年分のデータを取得 -----
            if (init) {
              start.setTime(sdff.parse(hashuString));
              start.add(Calendar.WEEK_OF_MONTH, -2);
              end.setTime(sdff.parse(hashuString));
              end.add(Calendar.WEEK_OF_MONTH, 2);
              cal.setTime(sdff.parse(hashuString));
            }
            else {
              cal.set(Calendar.YEAR, year);
              start.setTime(cal.getTime());
              start.add(Calendar.WEEK_OF_MONTH, -2);
              end.setTime(cal.getTime());
              end.add(Calendar.WEEK_OF_MONTH, 2);
            }
            int tYear = 0;
            long tDiff = 0;
            MotochoBase tbase = null;
            MonthWeekList mws = new MonthWeekList(year);
            for (int prev = 0; prev < 10; prev++) {
              start.add(Calendar.YEAR , -1 * prev);
              end.add(Calendar.YEAR   , -1 * prev);
              cal.add(Calendar.YEAR   , -1 * prev);
              List<MotochoBase> datas = MotochoBase.find.where().eq("crop_id", cropId).in("kukaku_id", keys).between("hashu_date", start.getTime(), end.getTime()).findList();
              for(MotochoBase data: datas) {
                if ((data.hinsyuId == null) || (data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) == -1)) {
                  continue;
                }
                if (data.hashuDate == null || data.shukakuStartDate == null) {
                  continue;
                }
                target.setTime(data.hashuDate);
                if (tYear == 0) { // 初回データ
                  tbase = data;
                  tDiff = DateU.GetDiffDate(tbase.hashuDate, cal.getTime());
                  tYear = cal.get(Calendar.YEAR);
                }
                else {  // 播種日が直近のデータを探索する
                  long diff = DateU.GetDiffDate(tbase.hashuDate, cal.getTime());
                  if ( (diff < tDiff) || ((diff == tDiff) && cal.get(Calendar.YEAR) < tYear)) {
                    tbase = data;
                    tDiff = diff;
                    tYear = cal.get(Calendar.YEAR);
                  }
                }
              }
            }
            //----- 各作業データを集計する -----
            long sakuduke = 0;
            long kanri    = 0;
            long shukaku  = 0;
            int  idx      = 0;
            ObjectNode workList = Json.newObject();
            java.sql.Date oWorkDate = null;
            java.sql.Date nWorkDate = null;
            if (tbase != null) {
              Logger.info("[makePlan] BASE DATA >>> kukaku={} year={} rotation={} seiiku={} kion={}", tbase.kukakuId, tbase.workYear, tbase.rotationSpeedOfYear, tbase.seiikuDayCount, tbase.totalSolarRadiation);
              if (init) {
                cal.setTime(sdff.parse(hashuString));
              }
              else {
                cal.set(Calendar.YEAR, year);
                int diffhashu = (int)DateU.GetDiffDate(tbase.workStartDay, tbase.hashuDate);
                cal.add(Calendar.DAY_OF_MONTH, diffhashu);
              }
              List<models.WorkDiary> wds = models.WorkDiary.getWorkDiaryOfWork(tbase.kukakuId, new java.sql.Timestamp(tbase.workStartDay.getTime()), new java.sql.Timestamp(tbase.workEndDay.getTime()));
              double oWorkId = -1;
              for (models.WorkDiary wd :wds) {
                target.setTime(cal.getTime());
                long diff = DateU.GetDiffDate(tbase.hashuDate, wd.workDate);
                target.add(Calendar.DAY_OF_MONTH, (int)diff);
                int workMode = 0;
                List<WorkChainItem> wcis = WorkChainItem.find.where().eq("work_id", wd.workId).findList();
                for (WorkChainItem wci : wcis) {
                  workMode = wci.workMode;
                  break;
                }
                if ((tbase.hashuDate.compareTo(wd.workDate) == -1)
                      && (workMode == 1)) { // 播種日以降の作付管理作業は無視
                  continue;
                }
                switch (workMode) {
                case 1: //作付
                  sakuduke += wd.workTime;
                  break;

                case 2: //管理
                  kanri   += wd.workTime;
                  break;

                case 3: //収穫
                  shukaku += wd.workTime;
                  break;

                default:
                  continue;
                }
                Work wk = Work.getWork(wd.workId);
                if ((wk != null) && (oWorkId != wd.workDiaryId) && ((oWorkDate == null ) || (oWorkDate != null && oWorkDate.compareTo(wd.workDate) != 0))) {
                  idx++;
                  ObjectNode workJson = Json.newObject();
                  workJson.put("idx", idx);
                  workJson.put("workDiaryId", wd.workDiaryId);
                  workJson.put("workId", wd.workId);
                  workJson.put("name", wk.workName);
                  workJson.put("date", sdff.format(target.getTime()));
                  workJson.put("time", wd.workTime);
                  workJson.put("mode", workMode);
                  workList.put(String.valueOf(idx), workJson);
                  Logger.info("IDX={} DATE={} WORKID={}", idx, sdff.format(target.getTime()), wd.workId);
                  if (target.get(Calendar.YEAR) == year) {
                    mws.update((target.get(Calendar.MONTH) + 1), target.get(Calendar.WEEK_OF_MONTH), workMode);
                  }
                }
                oWorkId   = wd.workDiaryId;
                oWorkDate = wd.workDate;
                nWorkDate = new java.sql.Date(target.getTimeInMillis());
              }
              ObjectNode rotationJson = Json.newObject();
              Crop crop = Crop.getCropInfo(cropId);
              rotationJson.put("id"         , crop.cropId);
              rotationJson.put("name"       , crop.cropName);
              rotationJson.put("color"      , crop.cropColor);
              rotationJson.put("hinsyu"     , tbase.hinsyuName);
              rotationJson.put("seiiku"     , DateU.GetDiffDate(tbase.hashuDate, tbase.shukakuStartDate));
              rotationJson.put("sakuduke"   , sakuduke);
              rotationJson.put("kanri"      , kanri);
              rotationJson.put("shukaku"    , shukaku);
              rotationJson.put("work"       , workList);
              rotationJson.put("monthweek"  , mws.outToJson());
              listIdx++;
              rotationList.put(String.valueOf(listIdx), rotationJson);
              Logger.info("SAKUDUKE={} KANRI={} SHUKAKU={}", sakuduke, kanri, shukaku);
              existData = true;
            }
            else {
              Logger.info("NOT FOUND MOTOCHO DATA.");
            }
            init = false;
            if (nWorkDate != null) {
              cal.setTime(nWorkDate);
            }
            else {
              break;
            }
          }
          ObjectNode cropJson = Json.newObject();
          Crop crop = Crop.getCropInfo(cropId);
          cropJson.put("id"           , crop.cropId);
          cropJson.put("name"         , crop.cropName);
          cropJson.put("color"        , crop.cropColor);
          cropJson.put("rotation"     , listIdx);
          cropJson.put("rotationList" , rotationList);
          cropList.put(String.valueOf((long)crop.cropId), cropJson);
          cropGoalList.put(String.valueOf((long)crop.cropId), cropJson);
        }
        if (existData) {
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        }
        else {
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
        }
        //----------------------------------------------------------------------------
        //- 輪作栽培計画の場合
        //----------------------------------------------------------------------------
        cal.set(Calendar.YEAR, year);
        ObjectNode rotationList = Json.newObject();
        Collections.sort(rinsakuJoken, new CompareRinsaku());
        boolean init = true;
        int sakuma = 0;
        listIdx      = 0;
        for (JsonNode info : rinsakuJoken) {
          double cropId = Double.parseDouble(info.get("cropId").asText());
          String hinsyuId = info.get("hinsyuId").asText();
          String hashuString = info.get("hashu").asText();
          if ("".equals(info.get("hinsyuId").asText())) { //品種が未選択の場合
            //----- 最大収穫量の品種を取得する -----
            if (init) {
              start.setTime(sdff.parse(hashuString));
              start.add(Calendar.WEEK_OF_MONTH, -2);
              end.setTime(sdff.parse(hashuString));
              end.add(Calendar.WEEK_OF_MONTH, 2);
              cal.setTime(sdff.parse(hashuString));
            }
            else {
              cal.add(Calendar.DAY_OF_MONTH, sakuma);
              start.setTime(cal.getTime());
              start.add(Calendar.WEEK_OF_MONTH, -2);
              end.setTime(cal.getTime());
              end.add(Calendar.WEEK_OF_MONTH, 2);
            }
            sakuma = info.get("sakuma").asInt();
            SaibaiPlanHinsyuList spl = new SaibaiPlanHinsyuList();
            for (int prev = 0; prev < 10; prev++) {
              start.add(Calendar.YEAR , -1 * prev);
              end.add(Calendar.YEAR   , -1 * prev);
              cal.add(Calendar.YEAR   , -1 * prev);
              List<MotochoBase> datas = MotochoBase.find.where().eq("crop_id", cropId).in("kukaku_id", keys).between("hashu_date", start.getTime(), end.getTime()).findList();
              for(MotochoBase data: datas) {
                if (data.hinsyuId == null) {
                  continue;
                }
                if (data.hashuDate == null || data.shukakuStartDate == null) {
                  continue;
                }
                String[] hinsyuIds = data.hinsyuId.split(",");
                spl.add(Double.parseDouble(hinsyuIds[0]));
                spl.put(Double.parseDouble(hinsyuIds[0]), data.totalShukakuCount);
              }
            }
            DecimalFormat fHinsyu = new DecimalFormat("##0");
            hinsyuId = fHinsyu.format(spl.getMaxHinshu());
          }
          Logger.info("[makePlan] JOKEN DATA >>> crop={} hashu={} hinsyu={}", cropId, hashuString, hinsyuId);
          //----- 過去10年分のデータを取得 -----
          if (init) {
            start.setTime(sdff.parse(hashuString));
            start.add(Calendar.WEEK_OF_MONTH, -2);
            end.setTime(sdff.parse(hashuString));
            end.add(Calendar.WEEK_OF_MONTH, 2);
            cal.setTime(sdff.parse(hashuString));
          }
          else {
            cal.set(Calendar.YEAR, year);
            start.setTime(cal.getTime());
            start.add(Calendar.WEEK_OF_MONTH, -2);
            end.setTime(cal.getTime());
            end.add(Calendar.WEEK_OF_MONTH, 2);
          }
          int tYear = 0;
          long tDiff = 0;
          MotochoBase tbase = null;
          MonthWeekList mws = new MonthWeekList(year);
          for (int prev = 0; prev < 10; prev++) {
            start.add(Calendar.YEAR , -1 * prev);
            end.add(Calendar.YEAR   , -1 * prev);
            cal.add(Calendar.YEAR   , -1 * prev);
            List<MotochoBase> datas = MotochoBase.find.where().eq("crop_id", cropId).in("kukaku_id", keys).between("hashu_date", start.getTime(), end.getTime()).findList();
            for(MotochoBase data: datas) {
              if ((data.hinsyuId == null) || (data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) == -1)) {
                continue;
              }
              if (data.hashuDate == null || data.shukakuStartDate == null) {
                continue;
              }
              target.setTime(data.hashuDate);
              if (tYear == 0) { // 初回データ
                tbase = data;
                tDiff = DateU.GetDiffDate(tbase.hashuDate, cal.getTime());
                tYear = cal.get(Calendar.YEAR);
              }
              else {  // 播種日が直近のデータを探索する
                long diff = DateU.GetDiffDate(tbase.hashuDate, cal.getTime());
                if ( (diff < tDiff) || ((diff == tDiff) && cal.get(Calendar.YEAR) < tYear)) {
                  tbase = data;
                  tDiff = diff;
                  tYear = cal.get(Calendar.YEAR);
                }
              }
            }
          }
          //----- 各作業データを集計する -----
          long sakuduke = 0;
          long kanri    = 0;
          long shukaku  = 0;
          int  idx      = 0;
          ObjectNode workList = Json.newObject();
          java.sql.Date oWorkDate = null;
          java.sql.Date nWorkDate = null;
          if (tbase != null) {
            Logger.info("[makePlan] BASE DATA >>> kukaku={} year={} rotation={} seiiku={} kion={}", tbase.kukakuId, tbase.workYear, tbase.rotationSpeedOfYear, tbase.seiikuDayCount, tbase.totalSolarRadiation);
            if (init) {
              cal.setTime(sdff.parse(hashuString));
            }
            else {
              cal.set(Calendar.YEAR, year);
              int diffhashu = (int)DateU.GetDiffDate(tbase.workStartDay, tbase.hashuDate);
              cal.add(Calendar.DAY_OF_MONTH, diffhashu);
            }
            List<models.WorkDiary> wds = models.WorkDiary.getWorkDiaryOfWork(tbase.kukakuId, new java.sql.Timestamp(tbase.workStartDay.getTime()), new java.sql.Timestamp(tbase.workEndDay.getTime()));
            double oWorkId = -1;
            for (models.WorkDiary wd :wds) {
              target.setTime(cal.getTime());
              long diff = DateU.GetDiffDate(tbase.hashuDate, wd.workDate);
              target.add(Calendar.DAY_OF_MONTH, (int)diff);
              int workMode = 0;
              List<WorkChainItem> wcis = WorkChainItem.find.where().eq("work_id", wd.workId).findList();
              for (WorkChainItem wci : wcis) {
                workMode = wci.workMode;
                break;
              }
              if ((tbase.hashuDate.compareTo(wd.workDate) == -1)
                    && (workMode == 1)) { // 播種日以降の作付管理作業は無視
                continue;
              }
              switch (workMode) {
              case 1: //作付
                sakuduke += wd.workTime;
                break;

              case 2: //管理
                kanri   += wd.workTime;
                break;

              case 3: //収穫
                shukaku += wd.workTime;
                break;

              default:
                continue;
              }
              Work wk = Work.getWork(wd.workId);
              if ((wk != null) && (oWorkId != wd.workDiaryId) && ((oWorkDate == null ) || (oWorkDate != null && oWorkDate.compareTo(wd.workDate) != 0))) {
                idx++;
                ObjectNode workJson = Json.newObject();
                workJson.put("idx", idx);
                workJson.put("workDiaryId", wd.workDiaryId);
                workJson.put("workId", wd.workId);
                workJson.put("name", wk.workName);
                workJson.put("date", sdff.format(target.getTime()));
                workJson.put("time", wd.workTime);
                workJson.put("mode", workMode);
                workList.put(String.valueOf(idx), workJson);
                Logger.info("IDX={} DATE={} WORKID={}", idx, sdff.format(target.getTime()), wd.workId);
                if (target.get(Calendar.YEAR) == year) {
                  mws.update((target.get(Calendar.MONTH) + 1), target.get(Calendar.WEEK_OF_MONTH), workMode);
                }
              }
              oWorkId   = wd.workDiaryId;
              oWorkDate = wd.workDate;
              nWorkDate = new java.sql.Date(target.getTimeInMillis());
            }
            ObjectNode rotationJson = Json.newObject();
            Crop crop = Crop.getCropInfo(cropId);
            rotationJson.put("id"         , crop.cropId);
            rotationJson.put("name"       , crop.cropName);
            rotationJson.put("color"      , crop.cropColor);
            rotationJson.put("hinsyu"     , tbase.hinsyuName);
            rotationJson.put("seiiku"     , DateU.GetDiffDate(tbase.hashuDate, tbase.shukakuStartDate));
            rotationJson.put("sakuduke"   , sakuduke);
            rotationJson.put("kanri"      , kanri);
            rotationJson.put("shukaku"    , shukaku);
            rotationJson.put("work"       , workList);
            rotationJson.put("monthweek"  , mws.outToJson());
            listIdx++;
            rotationList.put(String.valueOf(listIdx), rotationJson);
            cropGoalList.put(String.valueOf((long)crop.cropId), rotationJson);
            Logger.info("SAKUDUKE={} KANRI={} SHUKAKU={}", sakuduke, kanri, shukaku);
            existData = true;
          }
          else {
            Logger.info("NOT FOUND MOTOCHO DATA.");
          }
          init = false;
          if (nWorkDate != null) {
            cal.setTime(nWorkDate);
          }
          else {
            break;
          }
        }
        ObjectNode cropJson = Json.newObject();
        cropJson.put("id"           , "rinsaku");
        cropJson.put("name"         , "輪作");
        cropJson.put("color"        , "aaaaaa");
        cropJson.put("rotation"     , listIdx);
        cropJson.put("rotationList" , rotationList);
        cropList.put("rinsaku", cropJson);
        if (existData) {
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        }
        else {
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
        }
        //----------------------------------------------------------------------------
        //- 目標収穫量の取得
        //----------------------------------------------------------------------------
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        DecimalFormat df = new DecimalFormat("00");
        DecimalFormat rdf = new DecimalFormat("##0");
        for (JsonNode crop : cropGoalList) {
          SaibaiPlanGoal spg = SaibaiPlanGoal.find.where().eq("farm_id", farmId).eq("work_year", year).eq("crop_id", crop.get("id").asDouble()).findUnique();
          ObjectNode goalJson = Json.newObject();
          goalJson.put("id"   , crop.get("id").asLong());
          goalJson.put("name" , "（" + crop.get("name").asText() + "）");
          for (int month = 0; month < 12; month++) {
            cal.set(Calendar.MONTH, month);
            goalJson.put("s" + df.format(month + 1), cal.getActualMaximum(Calendar.WEEK_OF_MONTH));
          }
          if (spg != null) {
            goalJson.put("m01"   , spg.month1);
            goalJson.put("m02"   , spg.month2);
            goalJson.put("m03"   , spg.month3);
            goalJson.put("m04"   , spg.month4);
            goalJson.put("m05"   , spg.month5);
            goalJson.put("m06"   , spg.month6);
            goalJson.put("m07"   , spg.month7);
            goalJson.put("m08"   , spg.month8);
            goalJson.put("m09"   , spg.month9);
            goalJson.put("m10"   , spg.month10);
            goalJson.put("m11"   , spg.month11);
            goalJson.put("m12"   , spg.month12);
          }
          else {
            for (int month = 0; month < 12; month++) {
              goalJson.put("m" + df.format(month + 1)   , 0);
            }
          }
          for (int month = 0; month < 12; month++) {
            goalJson.put("r" + df.format(month + 1)   , rdf.format(getShukakuryo(year, month, farmId, crop.get("id").asDouble())));
          }

          goalList.put(String.valueOf(crop.get("id").asLong()), goalJson);
        }

      } catch (ParseException e) {
        e.printStackTrace();
      }
      resultJson.put("year", year);
      resultJson.put("crop", cropList);
      resultJson.put("goal", goalList);
      return ok(resultJson);
    }
    private static double getShukakuryo(int year, int month, double farmId, double cropId) {

      double syukakuryo = 0;

      Calendar start = Calendar.getInstance();
      start.set(Calendar.YEAR, year);
      start.set(Calendar.MONTH, month);
      start.set(Calendar.DAY_OF_MONTH, 1);
      Calendar end   = Calendar.getInstance();
      end.set(Calendar.YEAR, year);
      end.set(Calendar.MONTH, month);
      end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));

      List<TimeLine> tls = TimeLine.getTimeLineOfFarm(farmId, new java.sql.Timestamp(start.getTimeInMillis()), new java.sql.Timestamp(end.getTimeInMillis()));

      for (TimeLine tl : tls) {
        WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
        if (wd != null) {
          Work wk = Work.getWork(wd.workId);
          if (wk != null) {
            if (wk.workTemplateId != AgryeelConst.WorkTemplate.SHUKAKU &&
                wk.workTemplateId != AgryeelConst.WorkTemplate.SENKA) {
              continue;
            }
            List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", wd.kukakuId).le("work_start_day", wd.workDate).ge("work_end_day", wd.workDate).findList();
            if (mbs.size() == 0 || mbs.get(0).cropId != cropId) {
              continue;
            }
            syukakuryo += wd.shukakuRyo;
          }
        }
      }
      return syukakuryo;
    }
    public static Result commitGoalPlan() {
      ObjectNode resultJson = Json.newObject();
      boolean save = false;

      //----------------------------------------------------------------------------
      //- 目標収穫量の更新
      //----------------------------------------------------------------------------
      JsonNode input = request().body().asJson();

      UserComprtnent ac = new UserComprtnent();
      int getAccount = ac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      SaibaiPlanGoal spg = SaibaiPlanGoal.find.where().eq("farm_id", ac.accountData.farmId).eq("work_year", input.get("year").asInt()).eq("crop_id", input.get("id").asDouble()).findUnique();
      if (spg == null) {
        spg = new SaibaiPlanGoal();
        spg.farmId    = ac.accountData.farmId;
        spg.workYear  = input.get("year").asInt();
        spg.cropId    = input.get("id").asDouble();
        save          = true;
      }

      switch (input.get("month").asInt()) {
        case 1:
          spg.month1 = input.get("goalshukaku").asDouble();
          break;
        case 2:
          spg.month2 = input.get("goalshukaku").asDouble();
          break;
        case 3:
          spg.month3 = input.get("goalshukaku").asDouble();
          break;
        case 4:
          spg.month4 = input.get("goalshukaku").asDouble();
          break;
        case 5:
          spg.month5 = input.get("goalshukaku").asDouble();
          break;
        case 6:
          spg.month6 = input.get("goalshukaku").asDouble();
          break;
        case 7:
          spg.month7 = input.get("goalshukaku").asDouble();
          break;
        case 8:
          spg.month8 = input.get("goalshukaku").asDouble();
          break;
        case 9:
          spg.month9 = input.get("goalshukaku").asDouble();
          break;
        case 10:
          spg.month10 = input.get("goalshukaku").asDouble();
          break;
        case 11:
          spg.month11 = input.get("goalshukaku").asDouble();
          break;
        case 12:
          spg.month12 = input.get("goalshukaku").asDouble();
          break;
      }

      if (save) {
        spg.save();
      }
      else {
        spg.update();
      }

      return ok(resultJson);
    }
    public static Result commitPlanWork() {
      ObjectNode resultJson = Json.newObject();

      //----------------------------------------------------------------------------
      //- 目標収穫量の更新
      //----------------------------------------------------------------------------
      JsonNode input = request().body().asJson();
      SimpleDateFormat sdfp = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat sdfu = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      DecimalFormat    df   = new DecimalFormat("000000");


      try {
        Ebean.beginTransaction();

        UserComprtnent ac = new UserComprtnent();
        ac.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        String[] kukakuIds = input.get("kukaku").asText().split(",");

        JsonNode datalist = input.get("work");
        Calendar system = Calendar.getInstance();

        for (String sKukakuId : kukakuIds) {

          double kukakuId = Double.parseDouble(sKukakuId);

          for (JsonNode info : datalist) {

            WorkDiary wd = WorkDiary.getWorkDiaryById(info.get("workDiaryId").asDouble());
            if (wd == null) {
              continue;
            }
            //----- 作業計画の自動作成 -----
            WorkPlan workplan = new WorkPlan();
            Sequence sequence     = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKPLANID); //最新シーケンス値の取得
            double workPlanId     = sequence.sequenceValue;
            workplan.workPlanId          = workPlanId;
            workplan.workId              = wd.workId;
            workplan.kukakuId            = kukakuId;
            workplan.hillId              = wd.hillId;
            workplan.lineId              = wd.lineId;
            workplan.stockId             = wd.stockId;
            workplan.accountId           = "";
            workplan.workDate            = new java.sql.Date(sdfp.parse(info.get("date").asText()).getTime());
            workplan.workTime            = 0;
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
            workplan.workStartTime       = null;
            workplan.workEndTime         = null;
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
            workplan.workPlanFlag        = AgryeelConst.WORKPLANFLAG.AICAPLANCOMMIT;
            workplan.workPlanUUID        = df.format(ac.accountData.farmId) + sdfu.format(system.getTime()) + ac.accountData.accountId;
            workplan.shitateHonsu        = wd.shitateHonsu;
            workplan.nicho               = wd.nicho;
            workplan.haikiRyo            = wd.haikiRyo;

            workplan.save();

            //----- 作業散布計画の自動作成 -----
            List<models.WorkDiarySanpu> wdss = models.WorkDiarySanpu.find.where().eq("work_diary_id", wd.workDiaryId).orderBy("work_diary_sequence asc").findList();
            int workDiarySequence = 0;
            for (models.WorkDiarySanpu wds : wdss) {
              models.WorkPlanSanpu wps = new WorkPlanSanpu();

              workDiarySequence++;
              wps.workPlanId          = workPlanId;
              wps.workDiarySequence   = workDiarySequence;
              wps.sanpuMethod         = wds.sanpuMethod;
              wps.kikiId              = wds.kikiId;
              wps.attachmentId        = wds.attachmentId;
              wps.nouhiId             = wds.nouhiId;
              wps.bairitu             = wds.bairitu;
              wps.sanpuryo            = wds.sanpuryo;
              wps.kukakuStatusUpdate  = wds.kukakuStatusUpdate;
              wps.motochoUpdate       = wds.motochoUpdate;
              wps.save();
            }

            //----- 作業詳細計画の自動作成 -----
            List<models.WorkDiaryDetail> wdds = models.WorkDiaryDetail.find.where().eq("work_diary_id", wd.workDiaryId).orderBy("work_diary_sequence asc").findList();
            workDiarySequence = 0;
            for (models.WorkDiaryDetail wdd : wdds) {
              models.WorkPlanDetail wpd = new WorkPlanDetail();

              workDiarySequence++;
              wpd.workPlanId          = workPlanId;
              wpd.workDiarySequence   = workDiarySequence;
              wpd.workDetailKind      = wpd.workDetailKind;
              wpd.suryo               = wpd.suryo;
              wpd.sizaiId             = wpd.sizaiId;
              wpd.comment             = wpd.comment;
              wpd.syukakuNisugata     = wpd.syukakuNisugata;
              wpd.syukakuSitsu        = wpd.syukakuSitsu;
              wpd.syukakuSize         = wpd.syukakuSize;
              wpd.syukakuKosu         = wpd.syukakuKosu;
              wpd.shukakuRyo          = wpd.shukakuRyo;
              wpd.syukakuHakosu       = wpd.syukakuHakosu;
              wpd.syukakuNinzu        = wpd.syukakuNinzu;
              wpd.save();
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

              List<WorkPlanSanpu> wpss = WorkPlanSanpu.getWorkPlanSanpuList(workplan.workPlanId);

              for (WorkPlanSanpu wps : wpss) {

                /* 農肥IDから農肥情報を取得する */
                Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  wps.nouhiId).findUnique();

                String sanpuName  = "";

                if (wps.sanpuMethod != 0) {
                    sanpuName = "&nbsp;&nbsp;&nbsp;&nbsp;[" + Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, wps.sanpuMethod) + "]";
                }

                String unit = nouhi.getUnitString();

                planLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df.format(wps.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
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
              List<WorkPlanDetail> wpds = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
              int idx = 0;
              for (WorkPlanDetail wpd : wpds) {
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
              wpds = WorkPlanDetail.getWorkPlanDetailList(workplan.workPlanId);
              idx = 0;
              for (WorkPlanDetail wpd : wpds) {
                if (wpd.shukakuRyo == 0) {
                  continue;
                }
                idx++;
                planLine.message       += "<等級" + idx + "> "   + Shitu.getShituName(wpd.syukakuSitsu) + "<br>";
                planLine.message       += "<階級" + idx + "> "   + Size.getSizeName(wpd.syukakuSize) + "<br>";
                planLine.message       += "<箱数" + idx + "> "   + wpd.syukakuHakosu + "ケース" + "<br>";
                planLine.message       += "<個数" + idx + "> "   + wpd.syukakuKosu + "本" + "<br>";
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
        }

        Ebean.commitTransaction();
      }
      catch (Exception ex) {
        Logger.error("[commitPlanWork] Save Error.", ex);
        ex.printStackTrace();
        Ebean.rollbackTransaction();
      }


      return ok(resultJson);
    }
    public static Result getHinsyuOfCropHashuJson(double cropId, String hashuDate) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int year = 2020;
      SimpleDateFormat sdff = new SimpleDateFormat("yyyyMMdd");
      DecimalFormat df = new DecimalFormat("#,##0.0");

      Calendar start = Calendar.getInstance();
      start.set(Calendar.YEAR, year);
      start.set(Calendar.DAY_OF_MONTH, 1);
      Calendar end   = Calendar.getInstance();
      end.set(Calendar.YEAR, year);
      end.set(Calendar.DAY_OF_MONTH, 1);
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.DAY_OF_MONTH, 1);
      Calendar target = Calendar.getInstance();

      //----------------------------------------------------------------------------
      //- 栽培計画の生成
      //----------------------------------------------------------------------------
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
      Account account = accountComprtnent.accountData;
      double farmId = account.farmId;

      try {
        List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
        List<Double> keys = new ArrayList<Double>();
        for (Compartment ct : cts) {
          keys.add(new Double(ct.kukakuId));
        }
        Logger.info("--------------------------------------------------------------------------");
        Logger.info("crop={} hashu={}", cropId, hashuDate);
        //----- 過去10年分のデータを取得 -----
        start.setTime(sdff.parse(hashuDate));
        start.add(Calendar.WEEK_OF_MONTH, -2);
        end.setTime(sdff.parse(hashuDate));
        end.add(Calendar.WEEK_OF_MONTH, 2);
        cal.setTime(sdff.parse(hashuDate));
        SaibaiPlanHinsyuList spl = new SaibaiPlanHinsyuList();
        for (int prev = 0; prev < 10; prev++) {
          start.add(Calendar.YEAR , -1 * prev);
          end.add(Calendar.YEAR   , -1 * prev);
          cal.add(Calendar.YEAR   , -1 * prev);
          List<MotochoBase> datas = MotochoBase.find.where().eq("crop_id", cropId).in("kukaku_id", keys).between("hashu_date", start.getTime(), end.getTime()).findList();
          for(MotochoBase data: datas) {
            if (data.hinsyuId == null) {
              continue;
            }
            if (data.hashuDate == null || data.shukakuStartDate == null) {
              continue;
            }
            String[] hinsyuIds = data.hinsyuId.split(",");
            spl.add(Double.parseDouble(hinsyuIds[0]));
            spl.put(Double.parseDouble(hinsyuIds[0]), data.totalShukakuCount);
          }
        }

        keys = spl.getKeys();
        for (double key : keys) {
          SaibaiPlanHinsyu sph = spl.data(key);
          if (sph != null) {
            Hinsyu hinsyu = Hinsyu.getHinsyuInfo(sph.hinsyuId);
            if (hinsyu != null) {
              ObjectNode jd = Json.newObject();

              jd.put("id"   , hinsyu.hinsyuId);
              jd.put("name" , hinsyu.hinsyuName + "(" + df.format(sph.shukakuRyo) + "Kg)");

              listJson.put(String.valueOf(hinsyu.hinsyuId), jd);
            }
          }
        }

      } catch (ParseException e) {
        e.printStackTrace();
      }

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
    }
}
