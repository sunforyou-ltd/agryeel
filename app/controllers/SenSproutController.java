package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.AccountStatus;
import models.Compartment;
import models.SsKikiData;
import models.Work;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.UserComprtnent;

import consts.AgryeelConst;

/**
 * 各種サービス用コントローラー
 * @author kimura
 *
 */
public class SenSproutController extends Controller {

    public static Result getJoken(String accountId) {
      ObjectNode resultJson   = Json.newObject();
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(accountId);
      AccountStatus as = accountComprtnent.accountStatusData;
      ObjectNode base   = Json.newObject();
      ObjectNode data   = Json.newObject();

      base.put("from", as.ssBaseFrom);
      base.put("to", as.ssBaseTo);
      base.put("kukakuid", as.ssBaseKukakuId);
      Compartment ct = Compartment.getCompartmentInfo(as.ssBaseKukakuId);
      if (ct == null) {
        base.put("name", "");
      }
      else {
        base.put("name", ct.kukakuName);
      }
      resultJson.put("base", base);
      data.put("from", as.ssDataFrom);
      data.put("to", as.ssDataTo);
      data.put("kukakuid", as.ssDataKukakuId);
      Compartment ctd = Compartment.getCompartmentInfo(as.ssDataKukakuId);
      if (ctd == null) {
        data.put("name", "");
      }
      else {
        data.put("name", ctd.kukakuName);
      }
      resultJson.put("data", data);

      return ok(resultJson);
    }
    public static Result getData(String accountId, double farmId, double bKukakuId, String bFrom, String bTo, double dKukakuId, String dFrom, String dTo) {
      ObjectNode resultJson   = Json.newObject();
      ObjectNode base   = Json.newObject();
      ObjectNode data   = Json.newObject();
      SimpleDateFormat psdf = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat sdft = new SimpleDateFormat("yyyy/MM/dd HH時");
      java.sql.Timestamp dBaseFrom;
      java.sql.Timestamp dBaseTo;
      java.sql.Timestamp dDataFrom;
      java.sql.Timestamp dDataTo;
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      String sOldDate = "";

      //----------------------------------------------------------------------------------------------------------------
      // アカウントステータスの取得
      //----------------------------------------------------------------------------------------------------------------
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(accountId);
      AccountStatus as = accountComprtnent.accountStatusData;
      //----------------------------------------------------------------------------------------------------------------
      // 潅水作業の抽出
      //----------------------------------------------------------------------------------------------------------------
      List<Work> works = Work.find.where().eq("farm_id", farmId).eq("work_template_id", AgryeelConst.WorkTemplate.KANSUI).findList();
      List<Double> workkeys = new ArrayList<Double>();
      for (Work work: works) {
        workkeys.add(work.workId);
      }
      workkeys.add((double)9); // AICA基本作業の潅水

      //----------------------------------------------------------------------------------------------------------------
      // 比較元データの生成
      //----------------------------------------------------------------------------------------------------------------
      try {
        Calendar bCfrom = Calendar.getInstance();
        Calendar bCTo = Calendar.getInstance();
        bCfrom.setTimeInMillis(psdf.parse(bFrom).getTime());
        bCTo.setTimeInMillis(psdf.parse(bTo).getTime());
        DateU.setTime(bCfrom, DateU.TimeType.FROM);
        DateU.setTime(bCTo, DateU.TimeType.TO);
        dBaseFrom = new java.sql.Timestamp(bCfrom.getTimeInMillis());
        dBaseTo   = new java.sql.Timestamp(bCTo.getTimeInMillis());
        base.put("from", sdf.format(dBaseFrom));
        base.put("to", sdf.format(dBaseTo));
        base.put("kukakuid", bKukakuId);
        Compartment bcp = Compartment.getCompartmentInfo(bKukakuId);
        if (bcp == null) {
          base.put("name", "該当区画なし");
        }
        else {
          base.put("name", bcp.kukakuName);
        }
        // 条件の保存
        as.ssBaseKukakuId = bKukakuId;
        as.ssBaseFrom     = sdf.format(dBaseFrom);
        as.ssBaseTo       = sdf.format(dBaseTo);
        as.update();

        List<SsKikiData> skds = SsKikiData.find.where().eq("kukaku_id", bKukakuId).between("keisoku_day_time", dBaseFrom, dBaseTo).orderBy("keisoku_day_time").findList();
        ArrayNode bVmc10s = mapper.createArrayNode();
        ArrayNode bVmc20s = mapper.createArrayNode();
        ArrayNode bgts = mapper.createArrayNode();
        ArrayNode bkansui = mapper.createArrayNode();
        ArrayNode bseiiku = mapper.createArrayNode();
        int iSeiiku = 0;
        sOldDate = "";
        for (SsKikiData skd: skds) {
          base.put("vmc10", skd.vmc10);
          base.put("vmc20", skd.vmc20);
          base.put("gt", skd.gt);
          bVmc10s.add(skd.vmc10);
          bVmc20s.add(skd.vmc20);
          bgts.add(skd.gt);
          String sNewDate = psdf.format(skd.keisokuDayTime.getTime());
          if (!"".equals(sOldDate) && !sOldDate.equals(sNewDate)) {
            List<models.WorkDiary> wds = models.WorkDiary.find.where()
                .eq("kukaku_id", skd.kukakuId)
                .in("work_id", workkeys)
                .eq("work_date", new java.sql.Date(psdf.parse(sOldDate).getTime())).findList();
            double kansuiryo = 0;
            for (models.WorkDiary wd: wds) {
            kansuiryo += wd.kansuiRyo;
            }
            bkansui.add(kansuiryo);
            iSeiiku++;
            Logger.info("***** DATE BREAK OLD={} NEW={} COUNT={} RTI={}", sOldDate, sNewDate, wds.size(), kansuiryo );
          }
          else {
            bkansui.add(0);
          }
          bseiiku.add(iSeiiku);

          sOldDate = sNewDate;
        }
        if (!"".equals(sOldDate)) {
          List<models.WorkDiary> wds = models.WorkDiary.find.where()
              .eq("kukaku_id", bKukakuId)
              .in("work_id", workkeys)
              .eq("work_date", new java.sql.Date(psdf.parse(sOldDate).getTime())).findList();
          double kansuiryo = 0;
          for (models.WorkDiary wd: wds) {
            kansuiryo += wd.kansuiRyo;
          }
          bkansui.add(kansuiryo);
          Logger.info("***** DATE BREAK OLD={} COUNT={} RTI={}", sOldDate, wds.size(), kansuiryo );
        }
        else {
          bkansui.add(0);
        }
        if (skds.size() == 0) {
          base.put("vmc10", 0);
          base.put("vmc20", 0);
          base.put("gt", 0);
        }
        base.put("basePer", 30);
        base.put("gvmc10", bVmc10s);
        base.put("gvmc20", bVmc20s);
        base.put("ggt", bgts);
        base.put("kansui", bkansui);
        base.put("seiiku", bseiiku);
      } catch (Exception e) {
        // TODO: handle exception
      }
      resultJson.put("base", base);
      //----------------------------------------------------------------------------------------------------------------
      // 比較対象データの生成
      //----------------------------------------------------------------------------------------------------------------
      try {
        Calendar dCfrom = Calendar.getInstance();
        Calendar dCTo = Calendar.getInstance();
        dCfrom.setTimeInMillis(psdf.parse(dFrom).getTime());
        dCTo.setTimeInMillis(psdf.parse(dTo).getTime());
        DateU.setTime(dCfrom, DateU.TimeType.FROM);
        DateU.setTime(dCTo, DateU.TimeType.TO);
        dDataFrom = new java.sql.Timestamp(dCfrom.getTimeInMillis());
        dDataTo   = new java.sql.Timestamp(dCTo.getTimeInMillis());
        data.put("from", sdf.format(dDataFrom));
        data.put("to", sdf.format(dDataTo));
        data.put("kukakuid", dKukakuId);
        Compartment dcp = Compartment.getCompartmentInfo(dKukakuId);
        if (dcp == null) {
          data.put("name", "該当区画なし");
        }
        else {
          data.put("name", dcp.kukakuName);
        }
        // 条件の保存
        as.ssDataKukakuId = dKukakuId;
        as.ssDataFrom     = sdf.format(dDataFrom);
        as.ssDataTo       = sdf.format(dDataTo);
        as.update();

        List<SsKikiData> skds = SsKikiData.find.where().eq("kukaku_id", dKukakuId).between("keisoku_day_time", dDataFrom, dDataTo).orderBy("keisoku_day_time").findList();
        ArrayNode dVmc10s = mapper.createArrayNode();
        ArrayNode dVmc20s = mapper.createArrayNode();
        ArrayNode dgts = mapper.createArrayNode();
        ArrayNode dkansui = mapper.createArrayNode();
        ArrayNode dseiiku = mapper.createArrayNode();
        int iSeiiku = 0;
        sOldDate = "";
        for (SsKikiData skd: skds) {
          data.put("vmc10", skd.vmc10);
          data.put("vmc20", skd.vmc20);
          data.put("gt", skd.gt);
          dVmc10s.add(skd.vmc10);
          dVmc20s.add(skd.vmc20);
          dgts.add(skd.gt);
          resultJson.put("td", sdft.format(skd.keisokuDayTime));
          String sNewDate = psdf.format(skd.keisokuDayTime.getTime());
          if (!"".equals(sOldDate) && !sOldDate.equals(sNewDate)) {
            List<models.WorkDiary> wds = models.WorkDiary.find.where()
                .eq("kukaku_id", skd.kukakuId)
                .in("work_id", workkeys)
                .eq("work_date", new java.sql.Date(psdf.parse(sOldDate).getTime())).findList();
            double kansuiryo = 0;
            for (models.WorkDiary wd: wds) {
              kansuiryo += wd.kansuiRyo;
            }
            dkansui.add(kansuiryo);
            iSeiiku++;
            Logger.info("***** DATE BREAK OLD={} NEW={} COUNT={} RTI={}", sOldDate, sNewDate, wds.size(), kansuiryo );
          }
          else {
            dkansui.add(0);
          }
          dseiiku.add(iSeiiku);
          sOldDate = sNewDate;
        }
        if (!"".equals(sOldDate)) {
          List<models.WorkDiary> wds = models.WorkDiary.find.where()
              .eq("kukaku_id", dKukakuId)
              .in("work_id", workkeys)
              .eq("work_date", new java.sql.Date(psdf.parse(sOldDate).getTime())).findList();
          double kansuiryo = 0;
          for (models.WorkDiary wd: wds) {
          kansuiryo += wd.kansuiRyo;
          }
          dkansui.add(kansuiryo);
          Logger.info("***** DATE BREAK OLD={} COUNT={} RTI={}", sOldDate, wds.size(), kansuiryo );
        }
        else {
          dkansui.add(0);
        }
        if (skds.size() == 0) {
          data.put("vmc10", 0);
          data.put("vmc20", 0);
          data.put("gt", 0);
          resultJson.put("td", "");
        }
        data.put("basePer", 30);
        data.put("gvmc10", dVmc10s);
        data.put("gvmc20", dVmc20s);
        data.put("ggt", dgts);
        data.put("kansui", dkansui);
        data.put("seiiku", dseiiku);
      } catch (Exception e) {
        // TODO: handle exception
      }
      resultJson.put("data", data);



      return ok(resultJson);
    }
    public static Result getSSKukaku(double farmId) {
      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();

      ObjectMapper mapper     = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode    apiJson    = mapper.createArrayNode();

      List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
      List<Double> keys = new ArrayList<Double>();
      for (Compartment ct :cts) {
        keys.add(ct.kukakuId);
      }

      List<SsKikiData> ssds = SsKikiData.find.where().in("kukaku_id", keys).orderBy("kukaku_id, keisoku_day_time").findList();
      double oldId = 0;

      for (SsKikiData ssd: ssds) {
        if (oldId != ssd.kukakuId) {
          Compartment ct = Compartment.getCompartmentInfo(ssd.kukakuId);
          if (ct != null) {
            ObjectNode jd = Json.newObject();

            jd.put("id"   , ct.kukakuId);
            jd.put("name" , ct.kukakuName);

            apiJson.add(jd);

          }
        }
        oldId = ssd.kukakuId;
      }

      resultJson.put("data", apiJson);
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);

    }
}
