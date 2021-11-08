package controllers;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import models.Account;
import models.AicaData;
import models.Attachment;
import models.Belto;
import models.Common;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Crop;
import models.CropGroup;
import models.CropGroupList;
import models.Farm;
import models.FarmStatus;
import models.Field;
import models.FieldGroup;
import models.Hinsyu;
import models.Kiki;
import models.MotochoBase;
import models.Nisugata;
import models.Nouhi;
import models.PosttoPoint;
import models.Shitu;
import models.Size;
import models.TimeLine;
import models.Weather;
import models.Work;
import models.WorkChainItem;
import models.WorkDiary;
import models.WorkDiaryDetail;
import models.WorkDiarySanpu;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;
import util.ListrU;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.AicaCompornent;
import compornent.Analysis;
import compornent.FieldComprtnent;
import compornent.SaibaiPlanHinsyuList;
import compornent.UserComprtnent;

import consts.AgryeelConst;

/**
 * AI共通コントローラ
 * @author kimura
 *
 */
public class AICAController extends Controller {

    public static Result test() {
      ObjectNode resultJson = Json.newObject();
      return ok(resultJson);
    }
    public static Result SimpleRegressionTest(String point) {
      ObjectNode resultJson = Json.newObject();

      //----- 各種日付の生成 -----
      Calendar cStart = Calendar.getInstance();
      Calendar cEnd   = Calendar.getInstance();
      //----- 各種日付の設定-----
      cEnd.add(Calendar.MONTH, 4);

      long dfDate = DateU.GetDiffDate(cStart.getTime(), cEnd.getTime());
      DecimalFormat     df = new DecimalFormat("#,##0.0");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      Logger.info("----- SimpleRegressionTest -----");
      Logger.info("[ START ] {} ", sdf.format(cStart.getTime()));
      Logger.info("[ END   ] {} ", sdf.format(cEnd.getTime()));

      Calendar cprev  = Calendar.getInstance();
      Calendar cSys   = Calendar.getInstance();
      cprev.add(Calendar.DATE, -20);
      /* x,yは気温予測、x2,y2は日照時間予測 */
      List<double[]> aryY = new ArrayList<double[]>();
      List<double[][]> aryX = new ArrayList<double[][]>();
      List<double[]> aryY2 = new ArrayList<double[]>();
      List<double[][]> aryX2 = new ArrayList<double[][]>();
      int countY = 0;
      for (int i = 0; i < (int)dfDate; i++) {
        aryY.clear();
        aryX.clear();
        aryY2.clear();
        aryX2.clear();
        countY = 0;
        if (i != 0) {
          cprev.add(Calendar.DATE, 1);
          cSys.add(Calendar.DATE, 1);
          cStart.add(Calendar.DATE, 1);
        }
        int jituyo = Weather.checkWeather(point, new java.sql.Date(cStart.getTimeInMillis()));

        if (jituyo == 0) { //既に実績データが存在する場合何もしない
          continue;
        }
        else if (jituyo == 1) { //予測データが存在する場合
          Ebean.createSqlUpdate("DELETE FROM weather WHERE point_id = :pointId AND day_date = :dayDate").setParameter("pointId", point).setParameter("dayDate", new java.sql.Date(cStart.getTimeInMillis())).execute();
        }

        OLSMultipleLinearRegression mr  = new OLSMultipleLinearRegression();
        OLSMultipleLinearRegression mr2 = new OLSMultipleLinearRegression();

        cprev.set(Calendar.YEAR, cStart.get(Calendar.YEAR));
        cSys.set(Calendar.YEAR, cStart.get(Calendar.YEAR));
        if (cprev.compareTo(cSys) == 1) {
          cSys.add(Calendar.YEAR, 1);
        }
        for (int iYear = 0; iYear < 10; iYear++) {
          cprev.add(Calendar.YEAR, -1);
          cSys.add(Calendar.YEAR, -1);
          List<Weather> weathers = Weather.getWeather(point, new java.sql.Date(cprev.getTimeInMillis()), new java.sql.Date(cSys.getTimeInMillis()));

          if (weathers == null || weathers.size() == 0) {
            continue;
          }

          double[]  y    = new double[weathers.size() - 7];
          double[][] x   = new double[weathers.size() - 7][7];
          double[]  y2    = new double[weathers.size() - 7];
          double[][] x2   = new double[weathers.size() - 7][7];

          int idxx = 0;
          int idxy = 0;

          for (Weather w : weathers) {
            if (idxx <= 6) {
              x[idxy][idxx] = w.kionAve;
              x2[idxy][idxx] = w.daylightHours;
            }
            else {
              y[idxy]       = w.kionAve;
              y2[idxy]      = w.daylightHours;
              idxy++;
              countY++;
              if ((weathers.size() - 7) <= idxy) {
                break;
              }
              for (int idx=1; idx<=6; idx++) {
                x[idxy][idx-1]  = x[idxy-1][idx];
                x[idxy][6]      = w.kionAve;
                x2[idxy][idx-1] = x2[idxy-1][idx];
                x2[idxy][6]     = w.daylightHours;
              }
            }
            idxx++;
          }
          aryY.add(y);
          aryX.add(x);
          aryY2.add(y2);
          aryX2.add(x2);
        }

        double[]  y    = new double[countY];
        double[][] x   = new double[countY][7];
        double[]  y2   = new double[countY];
        double[][] x2  = new double[countY][7];

        int iYidx = 0;
        int iXidx = 0;
        int y2idx = -1;
        for (double[] ydata: aryY) {
          y2idx++;
          double[] y2data = aryY2.get(y2idx);
          double[][] xx  = aryX.get(iXidx);
          double[][] xx2 = aryX2.get(iXidx);
          for (int ii = 0; ii < ydata.length; ii++) {
            y[iYidx] = ydata[ii];
            x[iYidx] = xx[ii];
            y2[iYidx] = y2data[ii];
            x2[iYidx] = xx2[ii];
            iYidx++;
          }
          iXidx++;
        }

        mr.newSampleData(y, x);

        double[] coes = mr.estimateRegressionParameters();
        boolean init = true;
        double   b0 = 0;
        double[] bx = new double[7];
        int ibx = 0;
        for (double coe : coes) {
          if (init) {
//            Logger.info("[ INTERCEPT ] {} ", coe);
            b0 = coe;
            init = false;
          }
          else {
//            Logger.info("[ SLOPE     ] {} ", coe);
            bx[ibx] = coe;
            ibx++;
          }
        }

        double kion = b0;
        for (int idx=0; idx<7; idx++) {
//          Logger.info("[ {} ] {} ", idx, x[0][idx]);
          kion += (x[0][idx] * bx[idx]);
        }

        mr2.newSampleData(y2, x2);

        coes = mr2.estimateRegressionParameters();
        init = true;
        b0 = 0;
        bx = new double[7];
        ibx = 0;
        for (double coe : coes) {
          if (init) {
            b0 = coe;
            init = false;
          }
          else {
            bx[ibx] = coe;
            ibx++;
          }
        }

        double daylightHours = b0;
        for (int idx=0; idx<7; idx++) {
          daylightHours += (x2[0][idx] * bx[idx]);
        }
        Logger.info("[ {} ] {} ", sdf.format(cStart.getTime()), df.format(kion));
        Weather yosoku = new Weather();
        yosoku.pointId = point;
        yosoku.dayDate = new java.sql.Date(cStart.getTimeInMillis());
        BigDecimal bd  = new BigDecimal(kion);
        BigDecimal bds = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        yosoku.kionAve = bds.doubleValue();
        bd  = new BigDecimal(daylightHours);
        bds = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        yosoku.daylightHours = bds.doubleValue();
        yosoku.jituyo  = AgryeelConst.JITUYO.YOSOKU;
        yosoku.save();
      }
      return ok(resultJson);
    }
    public static Result dataTest(String point, String start, String end) {
      ObjectNode resultJson = Json.newObject();

      //----- 各種日付の生成 -----
      Calendar cStart = Calendar.getInstance();
      cStart.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      Calendar cEnd   = Calendar.getInstance();
      cEnd.set(Integer.parseInt(end.substring(0,4)), Integer.parseInt(end.substring(4,6)) - 1, Integer.parseInt(end.substring(6)));
      //----- 各種日付の設定-----
      //cEnd.add(Calendar.MONTH, 2);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      String filename      = "AicaData" + sdf.format(Calendar.getInstance().getTime());
      response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
      StringBuffer sb    = new StringBuffer();

      long dfDate = DateU.GetDiffDate(cStart.getTime(), cEnd.getTime());
      DecimalFormat     df = new DecimalFormat("#,##0.0");
      Logger.info("----- SimpleRegressionTest -----");
      Logger.info("[ START ] {} ", sdf.format(cStart.getTime()));
      Logger.info("[ END   ] {} ", sdf.format(cEnd.getTime()));

      Calendar cprev  = Calendar.getInstance();
      cprev.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      Calendar cSys   = Calendar.getInstance();
      cSys.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      cprev.add(Calendar.DATE, -20);
      List<double[]> aryY = new ArrayList<double[]>();
      List<double[][]> aryX = new ArrayList<double[][]>();
      int countY = 0;
      for (int i = 0; i < (int)dfDate; i++) {
        aryY.clear();
        aryX.clear();
        countY = 0;
        if (i != 0) {
          cprev.add(Calendar.DATE, 1);
          cSys.add(Calendar.DATE, 1);
          cStart.add(Calendar.DATE, 1);
        }
        int jituyo = Weather.checkWeatherList(point, new java.sql.Date(cStart.getTimeInMillis()));
        Logger.info("jituyo = {}", jituyo);
        Weather get = null;
        if ((jituyo % 10) == 1) { //既に実績データが存在する場合何もしない
          get = Weather.getWeather(point, new java.sql.Date(cStart.getTimeInMillis()));
        }
        else {
          get = new Weather();
        }
        if ((jituyo / 10) == 1) { //予測データが存在する場合
          Ebean.createSqlUpdate("DELETE FROM weather WHERE point_id = :pointId AND day_date = :dayDate AND jituyo = 1").setParameter("pointId", point).setParameter("dayDate", new java.sql.Date(cStart.getTimeInMillis())).execute();
        }

        OLSMultipleLinearRegression mr = new OLSMultipleLinearRegression();

        cprev.set(Calendar.YEAR, cStart.get(Calendar.YEAR));
        cSys.set(Calendar.YEAR, cStart.get(Calendar.YEAR));
        for (int iYear = 0; iYear < 10; iYear++) {
          cprev.add(Calendar.YEAR, -1);
          cSys.add(Calendar.YEAR, -1);
          List<Weather> weathers = Weather.getWeather(point, new java.sql.Date(cprev.getTimeInMillis()), new java.sql.Date(cSys.getTimeInMillis()));

          if (weathers == null || weathers.size() == 0) {
            continue;
          }

          double[]  y    = new double[weathers.size() - 7];
          double[][] x   = new double[weathers.size() - 7][7];

          int idxx = 0;
          int idxy = 0;

          for (Weather w : weathers) {
            if (idxx <= 6) {
              x[idxy][idxx] = w.kionAve;
            }
            else {
              y[idxy]       = w.kionAve;
              idxy++;
              countY++;
              if ((weathers.size() - 7) <= idxy) {
                break;
              }
              for (int idx=1; idx<=6; idx++) {
                x[idxy][idx-1]  = x[idxy-1][idx];
                x[idxy][6]      = w.kionAve;
              }
            }
            idxx++;
          }
          aryY.add(y);
          aryX.add(x);
        }

        double[]  y    = new double[countY];
        double[][] x   = new double[countY][7];

        int iYidx = 0;
        int iXidx = 0;
        for (double[] ydata: aryY) {
          double[][] xx = aryX.get(iXidx);
          for (int ii = 0; ii < ydata.length; ii++) {
            y[iYidx] = ydata[ii];
            x[iYidx] = xx[ii];
            iYidx++;
          }
          iXidx++;
        }

        mr.newSampleData(y, x);

        double[] coes = mr.estimateRegressionParameters();
        boolean init = true;
        double   b0 = 0;
        double[] bx = new double[7];
        int ibx = 0;
        for (double coe : coes) {
          if (init) {
            b0 = coe;
            init = false;
          }
          else {
            bx[ibx] = coe;
            ibx++;
          }
        }

        double kion = b0;
        for (int idx=0; idx<7; idx++) {
          kion += (x[0][idx] * bx[idx]);
        }
        Logger.info("[ {} ] YOSOKU={} JISEKI={}", sdf.format(cStart.getTime()), df.format(kion), df.format(get.kionAve));
        sb.append(sdf.format(cStart.getTime()) + "," + df.format(kion) + "," + df.format(get.kionAve) + "\r\n");
        Weather yosoku = new Weather();
        yosoku.pointId = point;
        yosoku.dayDate = new java.sql.Date(cStart.getTimeInMillis());
        BigDecimal bd  = new BigDecimal(kion);
        BigDecimal bds = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        yosoku.kionAve = bds.doubleValue();
        yosoku.jituyo  = AgryeelConst.JITUYO.YOSOKU;
        yosoku.save();
      }

      try {
        return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return ok();
    }
    public static Result data10(String point, String start, String end) {
      ObjectNode resultJson = Json.newObject();

      //----- 各種日付の生成 -----
      Calendar cStart = Calendar.getInstance();
      cStart.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      Calendar cEnd   = Calendar.getInstance();
      cEnd.set(Integer.parseInt(end.substring(0,4)), Integer.parseInt(end.substring(4,6)) - 1, Integer.parseInt(end.substring(6)));
      //----- 各種日付の設定-----
      //cEnd.add(Calendar.MONTH, 2);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      String filename      = "AicaData" + sdf.format(Calendar.getInstance().getTime());
      response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
      StringBuffer sb    = new StringBuffer();

      long dfDate = DateU.GetDiffDate(cStart.getTime(), cEnd.getTime());
      DecimalFormat     df = new DecimalFormat("#,##0.0");
      Logger.info("----- SimpleRegressionTest -----");
      Logger.info("[ START ] {} ", sdf.format(cStart.getTime()));
      Logger.info("[ END   ] {} ", sdf.format(cEnd.getTime()));

      Calendar cprev  = Calendar.getInstance();
      cprev.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      Calendar cSys   = Calendar.getInstance();
      cSys.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      cprev.add(Calendar.DATE, -20);
      List<double[]> aryY = new ArrayList<double[]>();
      List<double[][]> aryX = new ArrayList<double[][]>();
      int countY = 0;
      for (int i = 0; i < (int)dfDate; i++) {
        aryY.clear();
        aryX.clear();
        countY = 0;
        if (i != 0) {
          cprev.add(Calendar.DATE, 1);
          cSys.add(Calendar.DATE, 1);
          cStart.add(Calendar.DATE, 1);
        }
        int jituyo = Weather.checkWeatherList(point, new java.sql.Date(cStart.getTimeInMillis()));

        Weather get = null;
        if ((jituyo % 10) == 1) { //既に実績データが存在する場合何もしない
          get = Weather.getWeather(point, new java.sql.Date(cStart.getTimeInMillis()));
        }
        else {
          get = new Weather();
        }
        if ((jituyo / 10) == 1) { //予測データが存在する場合
          Ebean.createSqlUpdate("DELETE FROM weather WHERE point_id = :pointId AND day_date = :dayDate AND jituyo = 1").setParameter("pointId", point).setParameter("dayDate", new java.sql.Date(cStart.getTimeInMillis())).execute();
        }
        sb.append(sdf.format(cStart.getTime()) + "," + df.format(get.kionAve) + "\r\n");
      }

      try {
        return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return ok();
    }
    public static Result AicaDataCalc() {

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      String filename      = "AicaData" + sdf.format(Calendar.getInstance().getTime());
      response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

      Calendar sysdate = Calendar.getInstance();
      sysdate.add(Calendar.YEAR, -1);

      List<AicaData> aicas = AicaData.find.where().le("work_year", sysdate.get(Calendar.YEAR)).orderBy("kukaku_id, work_year, rotation_speed_of_year").findList();


      int key = 1;
      StringBuffer sb    = new StringBuffer();
      sb.append("\"区画名\"");
      sb.append(",\"作付年\"");
      sb.append(",\"年内回転数\"");
      sb.append(",\"播種日\"");
      sb.append(",\"品目\"");
      sb.append(",\"品種\"");
      sb.append(",\"生育日数\"");
      sb.append(",\"気温\"");
      sb.append(",\"収穫開始日\"");
      sb.append(",\"収穫終了日\"");
      sb.append(",\"収穫量\"");
      sb.append("\r\n");
      for (AicaData aica : aicas) {
        if (aica.hashuDate == null || aica.shukakuStartDate == null) {
          continue;
        }
        if (aica.seiikuDayCount <= 0) {
          continue;
        }
        ObjectNode jd = Json.newObject();
        sb.append("\"" + aica.kukakuName + "\"");
        sb.append("," + aica.workYear + "");
        sb.append("," + aica.rotationSpeedOfYear + "");
        sb.append(",\"" + sdf.format(aica.hashuDate) + "\"");
        sb.append(",\"" + aica.cropName + "\"");
        sb.append(",\"" + aica.hinsyuName + "\"");
        sb.append("," + aica.seiikuDayCount + "");
        List<Weather> weathers = Weather.getWeather("KURUME", new java.sql.Date(aica.hashuDate.getTime()), new java.sql.Date(aica.shukakuStartDate.getTime()));
        double sekisan = 0;
        for (Weather weather : weathers) {
          sekisan += weather.kionAve;
        }
        sb.append("," + sekisan + "");
        sb.append(",\"" + sdf.format(aica.shukakuStartDate) + "\"");
        sb.append(",\"" + sdf.format(aica.shukakuEndDate) + "\"");
        sb.append("," + aica.shukakuRyo + "");
        sb.append("\r\n");
      }

      try {
        return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return ok();
    }
    public static Result MakeRushData() {

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      DecimalFormat df     = new DecimalFormat("#,##0.0");
      String filename      = "AicaData" + sdf.format(Calendar.getInstance().getTime());
      response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

      Calendar from = Calendar.getInstance();
      Calendar to   = Calendar.getInstance();

      from.set(2019, 5, 1, 0, 0, 0);
      to.set(2019, 8, 17, 23, 59, 59);

      List<TimeLine> tls = TimeLine.find.where().eq("farm_id", 1).between("work_date", new java.sql.Date(from.getTimeInMillis()), new java.sql.Date(to.getTimeInMillis())).orderBy("kukaku_id, work_date").findList();

      StringBuffer sb    = new StringBuffer();
      sb.append("\"区画名\"");
      sb.append(",\"作業日\"");
      sb.append(",\"作業名\"");
      sb.append(",\"品目\"");
      sb.append(",\"品種名\"");
      sb.append(",\"農薬/肥料名\"");
      sb.append(",\"倍率\"");
      sb.append(",\"散布量\"");
      sb.append(",\"単位\"");
      sb.append("\r\n");
      for (TimeLine tl : tls) {
        Work work = Work.getWork(tl.workId);
        if ((work == null) || ((work.workTemplateId != 2) && (work.workTemplateId != 3))) {
          continue;
        }
        Compartment kukaku = Compartment.getCompartmentInfo(tl.kukakuId);
        if (kukaku == null) {
          continue;
        }
        WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
        if (wd == null) {
          continue;
        }
        if (work.workTemplateId == 3) { /* 播種情報 */
          Hinsyu hinsyu = Hinsyu.getHinsyuInfo(Double.valueOf(wd.hinsyuId));
          Crop crop = Crop.getCropInfo(hinsyu.cropId);
          if (hinsyu == null || crop == null) {
            continue;
          }
          sb.append("\"" + kukaku.kukakuName + "\"");
          sb.append(",\"" + sdf.format(wd.workDate) + "\"");
          sb.append(",\"" + work.workName + "\"");
          sb.append(",\"" + crop.cropName + "\"");
          sb.append(",\"" + hinsyu.hinsyuName + "\"");
          sb.append("\r\n");
        }
        else {

          List<WorkDiarySanpu> wdss = WorkDiarySanpu.getWorkDiarySanpuList(wd.workDiaryId);

          for (WorkDiarySanpu wds : wdss) {
            Nouhi nouhi = Nouhi.getNouhiInfo(wds.nouhiId);
            String unit = Common.GetCommonValue(Common.ConstClass.UNIT, nouhi.unitKind);

            sb.append("\"" + kukaku.kukakuName + "\"");
            sb.append(",\"" + sdf.format(wd.workDate) + "\"");
            sb.append(",\"" + work.workName + "\"");
            sb.append(",\"\",\"\",\"" + nouhi.nouhiName + "\"");
            sb.append("," + wds.bairitu + "");
            double hosei = 1;
            if (nouhi.unitKind == 1 || nouhi.unitKind == 2) {
              hosei = 0.001;
            }
            sb.append(",\"" + df.format(wds.sanpuryo * hosei) +"\"");
            sb.append(",\"" + unit +"\"");
            sb.append("\r\n");
          }
        }
      }

      try {
        return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return ok();
    }
    public static Result OutKionJiseki(String point, String start, String end, int prev) {
      ObjectNode resultJson = Json.newObject();

      //----- 各種日付の生成 -----
      Calendar cStart = Calendar.getInstance();
      cStart.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      Calendar cEnd   = Calendar.getInstance();
      cEnd.set(Integer.parseInt(end.substring(0,4)), Integer.parseInt(end.substring(4,6)) - 1, Integer.parseInt(end.substring(6)));
      //----- 各種日付の設定-----

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      String filename      = "AicaData_KionJiseki(" + point + ")" + sdf.format(Calendar.getInstance().getTime());
      response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
      StringBuffer sb    = new StringBuffer();

      long dfDate = DateU.GetDiffDate(cStart.getTime(), cEnd.getTime());
      DecimalFormat     df = new DecimalFormat("#,##0.0");
      Logger.info("----- OutKionJiseki -----");
      Logger.info("[ START ] {} ", sdf.format(cStart.getTime()));
      Logger.info("[ END   ] {} ", sdf.format(cEnd.getTime()));

      Calendar cSys   = Calendar.getInstance();
      /* ヘッダ作成 */
      cSys.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      sb.append("\"" + point + "\"");
      cSys.add(Calendar.DAY_OF_MONTH, -1);
      for (int i = 0; i < (int)dfDate; i++) {
        cSys.add(Calendar.DAY_OF_MONTH, 1);
        sb.append(",\"" + sdf.format(cSys.getTime()) + "\"");
      }
      sb.append("\r\n");
      /* データ作成 */
      for (int y = 0; y < prev; y++) {
        cSys.set((Integer.parseInt(start.substring(0,4)) - y), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
        sb.append(cSys.get(Calendar.YEAR));
        cSys.add(Calendar.DAY_OF_MONTH, -1);
        for (int i = 0; i < (int)dfDate; i++) {
          cSys.add(Calendar.DAY_OF_MONTH, 1);
          Weather w = Weather.getWeather(point, new java.sql.Date(cSys.getTimeInMillis()));
          if (w != null) {
            sb.append("," + df.format(w.kionAve));
          }
          else {
            sb.append(",");
          }
        }
        sb.append("\r\n");
      }

      try {
        return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return ok();
    }
    public static Result OutRainJiseki(String point, String start, String end, int prev) {
      ObjectNode resultJson = Json.newObject();

      //----- 各種日付の生成 -----
      Calendar cStart = Calendar.getInstance();
      cStart.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      Calendar cEnd   = Calendar.getInstance();
      cEnd.set(Integer.parseInt(end.substring(0,4)), Integer.parseInt(end.substring(4,6)) - 1, Integer.parseInt(end.substring(6)));
      //----- 各種日付の設定-----

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      String filename      = "AicaData_RainJiseki(" + point + ")" + sdf.format(Calendar.getInstance().getTime());
      response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
      StringBuffer sb    = new StringBuffer();

      long dfDate = DateU.GetDiffDate(cStart.getTime(), cEnd.getTime());
      DecimalFormat     df = new DecimalFormat("#,##0.0");
      Logger.info("----- OutRainJiseki -----");
      Logger.info("[ START ] {} ", sdf.format(cStart.getTime()));
      Logger.info("[ END   ] {} ", sdf.format(cEnd.getTime()));

      Calendar cSys   = Calendar.getInstance();
      /* ヘッダ作成 */
      cSys.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      sb.append("\"" + point + "\"");
      cSys.add(Calendar.DAY_OF_MONTH, -1);
      int oldMonth = 0;
      double rain  = 0;
      for (int i = 0; i < (int)dfDate; i++) {
        cSys.add(Calendar.DAY_OF_MONTH, 1);
        if (oldMonth != 0 && oldMonth != cSys.get(Calendar.MONTH)) {
          sb.append("," + (oldMonth + 1) + "月");
        }
        oldMonth = cSys.get(Calendar.MONTH);
      }
      sb.append("," + (oldMonth + 1) + "月");
      sb.append("\r\n");
      /* データ作成 */
      for (int y = 0; y < prev; y++) {
        cSys.set((Integer.parseInt(start.substring(0,4)) - y), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
        sb.append(cSys.get(Calendar.YEAR));
        cSys.add(Calendar.DAY_OF_MONTH, -1);
        oldMonth = 0;
        rain  = 0;
        for (int i = 0; i < (int)dfDate; i++) {
          cSys.add(Calendar.DAY_OF_MONTH, 1);
          Weather w = Weather.getWeather(point, new java.sql.Date(cSys.getTimeInMillis()));
          if (oldMonth != 0 && oldMonth != cSys.get(Calendar.MONTH)) {
            sb.append("," + rain);
            rain = 0;
          }
          else {
            if (w != null) {
              rain += w.rain;
            }
          }
          oldMonth = cSys.get(Calendar.MONTH);
        }
        sb.append("," + rain);
        sb.append("\r\n");
      }

      try {
        return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return ok();
    }
    public static Result OutKionJituyo(String point, String start, String end) {
      ObjectNode resultJson = Json.newObject();

      //----- 各種日付の生成 -----
      Calendar cStart = Calendar.getInstance();
      cStart.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      Calendar cEnd   = Calendar.getInstance();
      cEnd.set(Integer.parseInt(end.substring(0,4)), Integer.parseInt(end.substring(4,6)) - 1, Integer.parseInt(end.substring(6)));
      //----- 各種日付の設定-----

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      String filename      = "AicaData_KionJituyo(" + point + ")" + sdf.format(Calendar.getInstance().getTime());
      response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
      StringBuffer sb    = new StringBuffer();

      long dfDate = DateU.GetDiffDate(cStart.getTime(), cEnd.getTime());
      DecimalFormat     df = new DecimalFormat("#,##0.0");
      Logger.info("----- OutKionJiseki -----");
      Logger.info("[ START ] {} ", sdf.format(cStart.getTime()));
      Logger.info("[ END   ] {} ", sdf.format(cEnd.getTime()));

      Calendar cSys   = Calendar.getInstance();
      /* ヘッダ作成 */
      cSys.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      sb.append("\"" + point + "\"");
      cSys.add(Calendar.DAY_OF_MONTH, -1);
      for (int i = 0; i < (int)dfDate; i++) {
        cSys.add(Calendar.DAY_OF_MONTH, 1);
        sb.append(",\"" + sdf.format(cSys.getTime()) + "\"");
      }
      sb.append("\r\n");
      /* データ作成(実績) */
      sb.append("\"実績\"");
      cSys.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      cSys.add(Calendar.DAY_OF_MONTH, -1);
      for (int i = 0; i < (int)dfDate; i++) {
        cSys.add(Calendar.DAY_OF_MONTH, 1);
        Weather w = Weather.getWeather(point, new java.sql.Date(cSys.getTimeInMillis()));
        if (w != null) {
          sb.append("," + df.format(w.kionAve));
        }
        else {
          sb.append(",");
        }
      }
      sb.append("\r\n");

      /* データ作成(予測) */
      Calendar cprev  = Calendar.getInstance();
      cprev.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      cprev.add(Calendar.DAY_OF_MONTH, -1);
      cprev.add(Calendar.DATE, -20);
      cSys.set(Integer.parseInt(start.substring(0,4)), Integer.parseInt(start.substring(4,6)) - 1, Integer.parseInt(start.substring(6)));
      cSys.add(Calendar.DAY_OF_MONTH, -1);
      List<double[]> aryY = new ArrayList<double[]>();
      List<double[][]> aryX = new ArrayList<double[][]>();
      int countY = 0;
      sb.append("\"予測\"");
      for (int i = 0; i < (int)dfDate; i++) {
        cSys.add(Calendar.DAY_OF_MONTH, 1);
        cprev.add(Calendar.DAY_OF_MONTH, 1);
        aryY.clear();
        aryX.clear();
        countY = 0;

        OLSMultipleLinearRegression mr = new OLSMultipleLinearRegression();

        cprev.set(Calendar.YEAR, cStart.get(Calendar.YEAR));
        cSys.set(Calendar.YEAR, cStart.get(Calendar.YEAR));
        for (int iYear = 0; iYear < 10; iYear++) {
          cprev.add(Calendar.YEAR, -1);
          cSys.add(Calendar.YEAR, -1);
          List<Weather> weathers = Weather.getWeather(point, new java.sql.Date(cprev.getTimeInMillis()), new java.sql.Date(cSys.getTimeInMillis()));

          if (weathers == null || weathers.size() == 0) {
            continue;
          }

          double[]  y    = new double[weathers.size() - 7];
          double[][] x   = new double[weathers.size() - 7][7];

          int idxx = 0;
          int idxy = 0;

          for (Weather w : weathers) {
            if (idxx <= 6) {
              x[idxy][idxx] = w.kionAve;
            }
            else {
              y[idxy]       = w.kionAve;
              idxy++;
              countY++;
              if ((weathers.size() - 7) <= idxy) {
                break;
              }
              for (int idx=1; idx<=6; idx++) {
                x[idxy][idx-1]  = x[idxy-1][idx];
                x[idxy][6]      = w.kionAve;
              }
            }
            idxx++;
          }
          aryY.add(y);
          aryX.add(x);
        }

        double[]  y    = new double[countY];
        double[][] x   = new double[countY][7];

        int iYidx = 0;
        int iXidx = 0;
        for (double[] ydata: aryY) {
          double[][] xx = aryX.get(iXidx);
          for (int ii = 0; ii < ydata.length; ii++) {
            y[iYidx] = ydata[ii];
            x[iYidx] = xx[ii];
            iYidx++;
          }
          iXidx++;
        }

        mr.newSampleData(y, x);

        double[] coes = mr.estimateRegressionParameters();
        boolean init = true;
        double   b0 = 0;
        double[] bx = new double[7];
        int ibx = 0;
        for (double coe : coes) {
          if (init) {
            b0 = coe;
            init = false;
          }
          else {
            bx[ibx] = coe;
            ibx++;
          }
        }

        double kion = b0;
        for (int idx=0; idx<7; idx++) {
          kion += (x[0][idx] * bx[idx]);
        }
        sb.append("," + df.format(kion));
      }

      try {
        return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return ok();
    }
    public static Result shukakuYosoku() {
      ObjectNode resultJson = Json.newObject();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      String filename      = "AicaData_ShukakuYosoku" + sdf.format(Calendar.getInstance().getTime());
      response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
      StringBuffer sb    = new StringBuffer();

      DecimalFormat     df = new DecimalFormat("##0.0");
      Logger.info("----- ShukakuYosoku -----");

      List<MotochoBase> bases = MotochoBase.find.where().ne("hashu_date", null).ne("shukaku_start_date", null).orderBy("kukaku_id").findList();

      sb.append("\"区画名\"");
      sb.append(",\"播種日\"");
      sb.append(",\"品種\"");
      sb.append(",\"収穫開始日\"");
      sb.append(",\"生育日数\"");
      sb.append(",\"積算温度\"");
      sb.append(",\"積算日照時間\"");
      sb.append(",\"予測ポイント\"");
      sb.append(",\"収穫量\"");
      sb.append(",\"ポイント当たりの収穫量\"");
      sb.append("\r\n");
      for (MotochoBase base : bases) {
        Compartment ct = Compartment.getCompartmentInfo(base.kukakuId);
        Field fd = ct.getFieldInfo();
        if (fd != null) {
          if (base.seiikuDayCount < 0) {
            continue;
          }
          String pointId = PosttoPoint.getPointId(fd.postNo);
          String[] hinsyus = base.hinsyuName.split(",");
          for (String hinsyu : hinsyus) {
            sb.append("\"" + base.kukakuName + "\"");
            sb.append("," + "\"" + sdf.format(base.hashuDate) + "\"");
            sb.append("," + "\"" + hinsyu + "\"");
            sb.append("," + "\"" + sdf.format(base.shukakuStartDate) + "\"");
            sb.append("," + base.seiikuDayCount);
            List<Weather> weathers = Weather.getWeather(pointId, base.hashuDate, base.shukakuStartDate);
            double kion  = 0;
            double nisyo = 0;
            double point = 0;
            for (Weather weather : weathers) {
              kion  += weather.kionAve;
              nisyo  += weather.daylightHours;
            }
            sb.append("," + df.format(kion));
            sb.append("," + df.format(nisyo));
            if (nisyo != 0) {
              point += (kion / nisyo);
            }
            sb.append("," + df.format(point));
            sb.append("," + df.format(base.totalShukakuNumber));
            double pointShukaku = 0;
            if (point != 0) {
              pointShukaku = base.totalShukakuNumber / point;
            }
            sb.append("," + df.format(pointShukaku));
            sb.append("\r\n");
          }
        }
      }

      try {
        return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return ok();
    }
    public static Result outWorkdiary(double farmId, String targetMonth) {
      ObjectNode resultJson = Json.newObject();

      String pMonth = targetMonth + "01";
      SimpleDateFormat sdfp = new SimpleDateFormat("yyyyMMdd");

      try {
        java.util.Date dMonth = sdfp.parse(pMonth);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dMonth.getTime());

        int start = 1;
        int end   = cal.getActualMaximum(Calendar.DATE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月分");
        SimpleDateFormat tmf = new SimpleDateFormat("HH:mm");
        DecimalFormat     df = new DecimalFormat("#,##0.0");
        String filename      = "AicaData_WorkDiary" + sdf.format(Calendar.getInstance().getTime());
        response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
        StringBuffer sb    = new StringBuffer();

        List<Account> accounts = Account.getAccountOfFarm(farmId);
        for(Account at : accounts) {
          //----- アカウント名 -----
          sb.append("\"" + at.acountName + "\"");
          for (int i = 0; i < 41; i++) {
            sb.append(",\"\"");
          }
          sb.append("\r\n");

          //----- 年月ヘッダ -----
          sb.append("\"" + sdf.format(dMonth) + "\",\"曜日\"");
          sb.append(",\"計時間\"");
          for (int i = 0; i < 30; i++) {
            sb.append(",\"開始\",\"終了\",\"作業\",\"時間\"");
          }
          sb.append("\r\n");
          for (int i = start; i <= end; i++){
            cal.set(Calendar.DAY_OF_MONTH, i);
            sb.append("" + i + "");                    //日
            sb.append(",\"" + getWeek(cal) + "\"");     //曜日

            List<WorkDiary> wds = WorkDiary.find.where().eq("account_id", at.accountId).eq("work_date", new java.sql.Date(cal.getTimeInMillis())).orderBy("work_start_time").findList();
            int tWorkTime = 0;
            for (WorkDiary wd : wds) {
              tWorkTime += wd.workTime;
            }
            sb.append("," + df.format((double)tWorkTime / (double)60) + "");          //作業時間
            for (WorkDiary wd : wds) {
              if (wd.workStartTime == null) {
                sb.append(",\"未測定\"");     //開始時間
              }
              else {
                sb.append(",\"" + tmf.format(wd.workStartTime) + "\"");     //開始時間
              }
              if (wd.workEndTime == null) {
                sb.append(",\"未測定\"");                                    //終了時間
              }
              else {
                sb.append(",\"" + tmf.format(wd.workEndTime) + "\"");       //終了時間
              }
              Work wk = Work.getWork(wd.workId);
              sb.append(",\"" + wk.workName + "\"");                        //作業名
              sb.append("," + df.format(((double)wd.workTime / (double)60)) + "");  //作業時間
              tWorkTime += wd.workTime;
            }
            for (int x=wds.size(); x<30;x++) {
              sb.append(",\"\"");       //開始時間
              sb.append(",\"\"");       //終了時間
              sb.append(",\"\"");       //作業名
              sb.append(",");           //作業時間
            }
            sb.append("\r\n");
          }
        }
        try {
          return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
          Logger.error(e.getMessage(),e);
          resultJson.put("result", "MonthError");
          return ok(resultJson);
        }

      } catch (ParseException e1) {
        e1.printStackTrace();
        Logger.error(e1.getMessage(),e1);
        resultJson.put("result", "MonthError");
        return ok(resultJson);
      }
    }
    public static Result AICADataoutput(double farmId, String dateFrom, String dateTo, double cropId) {
      ObjectNode resultJson = Json.newObject();

      //----- パラメータの調整 -----
      //生産者情報の取得
      Farm fm = Farm.find.where().eq("farm_id", farmId).findUnique();
      if (fm == null) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "指定された生産者ＩＤが存在しません。");
        return ok(resultJson);
      }

      //指定された範囲日付の変換
      SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
      java.sql.Date dFrom  = null;
      java.sql.Date dTo    = null;
      try {
        dFrom = new java.sql.Date(dateParse.parse(dateFrom).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（自）に誤りがあります。 FROM = " + dateFrom);
        return ok(resultJson);
      }
      try {
        dTo = new java.sql.Date(dateParse.parse(dateTo).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + dateTo);
        return ok(resultJson);
      }

      //生産物情報の取得
      if (cropId != 0) {  //指定がある場合のみ
        Crop cp = Crop.getCropInfo(cropId);
        if (cp == null) {
          resultJson.put("result", AgryeelConst.Result.ERROR);
          resultJson.put("message", "指定された品目ＩＤが存在しません。");
          return ok(resultJson);
        }
      }

      //----- 区画情報の取得 -----
      List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
      if (cts.size() == 0) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "該当する区画が存在しません。");
        return ok(resultJson);
      }

      DecimalFormat decf  = new DecimalFormat("00000");
      SimpleDateFormat sdfs = new SimpleDateFormat("MM/dd");
      long totalCount = 0;
      List<MotochoBase> bases = null;
      ObjectNode datalist = Json.newObject();
      for (Compartment cp : cts) {  //区画分ループする

        if (cropId == 0) {
          bases = MotochoBase.find.where()
              .eq("kukaku_id", cp.kukakuId)
              .between("hashu_date", dFrom, dTo)
              .orderBy("work_year DESC, rotation_speed_of_year DESC")
              .findList();
        }
        else {
          bases = MotochoBase.find.where()
              .eq("kukaku_id", cp.kukakuId)
              .eq("crop_id", cropId)
              .between("hashu_date", dFrom, dTo)
              .orderBy("work_year DESC, rotation_speed_of_year DESC")
              .findList();
        }
        //----- 元帳基本情報の取得 -----
        for (MotochoBase base : bases) {

          ObjectNode outjson = Json.newObject();
          ObjectNode motocho = Json.newObject();

          Calendar cHashu = Calendar.getInstance();
          cHashu.setTime(base.hashuDate);

          motocho.put("year"    , cHashu.get(Calendar.YEAR));
          motocho.put("month"   , cHashu.get(Calendar.MONTH) + 1);
          motocho.put("day"     , cHashu.get(Calendar.DAY_OF_MONTH));
          motocho.put("week"    , cHashu.get(Calendar.WEEK_OF_YEAR));

          Field fd = cp.getFieldInfo();
          motocho.put("field"   , fd.fieldName);
          motocho.put("kukaku"  , cp.kukakuName);

          motocho.put("rotation", base.rotationSpeedOfYear);
          motocho.put("crop"    , base.cropName);
          motocho.put("cropId"  , base.cropId);
          motocho.put("hinsyu"  , base.hinsyuName);

          //生育状況
          /* 最新の作付回転数と同じ、且つ収穫開始日が未定の場合、生育範囲をシステム日付にする */
          java.sql.Date endDate = new java.sql.Date(DateU.getSystemTimeStamp().getTime());
          if (base.shukakuStartDate != null) {
            endDate = base.shukakuStartDate;
          }
          else {
            CompartmentStatus cs = FieldComprtnent.getCompartmentStatus(cp.kukakuId);
            if ((cs.workYear == base.workYear) && (cs.rotationSpeedOfYear == base.rotationSpeedOfYear)) {
              /* システム日付のまま */
            }
            else {
              endDate = new java.sql.Date(base.workEndDay.getTime());
            }
          }
          if (base.seiikuDayCount > 0) {
            motocho.put("seiiku"  , base.seiikuDayCount);
          }
          else {
            motocho.put("seiiku"  , DateU.GetDiffDate(base.hashuDate, endDate));
          }

          //気象情報の取得
          String point = PosttoPoint.getPointId(fd.postNo);

          if (!"".equals(point)) {
            List<Weather> wths = Weather.getWeather(point, base.hashuDate, endDate);
            double kion = 0;
            double rain = 0;

            for (Weather wth : wths) {
              kion += wth.kionAve;
              rain += wth.rain;
            }

            motocho.put("kion"      , new BigDecimal(kion).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            motocho.put("rain"      , new BigDecimal(rain).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            if (base.seiikuDayCount != 0) {
              motocho.put("kionave"   , new BigDecimal((kion / base.seiikuDayCount)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
              motocho.put("rainave"   , new BigDecimal((rain / base.seiikuDayCount)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            else {
              motocho.put("kionave"   , 0);
              motocho.put("rainave"   , 0);

            }

          }
          else {
            motocho.put("kion"      , 0);
            motocho.put("rain"      , 0);
            motocho.put("kionave"   , 0);
            motocho.put("rainave"   , 0);
          }

          //収穫量情報の取得
          motocho.put("shukaku"   , base.totalShukakuCount);
          double tanshu = 0;
          if (base.area != 0) {
            tanshu = (base.totalShukakuNumber / base.area) * 10;
          }
          motocho.put("tanshu"    , tanshu);
          if (base.shukakuStartDate != null) {
            long dfShukakuStart = DateU.GetDiffDate(base.hashuDate, base.shukakuStartDate);
            motocho.put("shukakuStart"  , sdfs.format(base.shukakuStartDate));
            motocho.put("shukakuStartDF", dfShukakuStart);
          }
          else {
            motocho.put("shukakuStart"  , "");
            motocho.put("shukakuStartDF", 0);
          }
          if (base.shukakuEndDate != null) {
            long dfShukakuEnd = DateU.GetDiffDate(base.hashuDate, base.shukakuEndDate);
            motocho.put("shukakuEnd"    , sdfs.format(base.shukakuEndDate));
            motocho.put("shukakuEndDF"  , dfShukakuEnd);
          }
          else {
            motocho.put("shukakuEnd"    , "");
            motocho.put("shukakuEndDF"  , 0);
          }
          if (base.shukakuStartDate != null && base.shukakuEndDate != null) {
            long dfShukaku = DateU.GetDiffDate(base.shukakuStartDate, base.shukakuEndDate);
            motocho.put("shukakuDF"     , dfShukaku);
          }
          else {
            motocho.put("shukakuDF"     , 0);
          }

          int rotationZen = base.rotationSpeedOfYear - 1;
          if (rotationZen <= 0) {
            motocho.put("shukakuStanby" , 0);
          }
          else {

            MotochoBase baseZen = MotochoBase.find.where().eq("kukaku_id", base.kukakuId).eq("work_year", base.workYear).eq("rotation_speed_of_year", rotationZen).findUnique();
            if (baseZen == null) {
              motocho.put("shukakuStanby" , 0);
            }
            else {
              if (baseZen.shukakuEndDate != null) {
                long dfStanby = DateU.GetDiffDate(baseZen.shukakuEndDate, base.hashuDate);
                motocho.put("shukakuStanby" , dfStanby);
              }
              else {
                motocho.put("shukakuStanby" , 0);
              }
            }
          }
          motocho.put("area" , base.area);
          //----- 作業記録情報の取得 -----
          ObjectNode hiryoinfo    = Json.newObject();
          ObjectNode nouyakinfo   = Json.newObject();
          ObjectNode shukakuinfo  = Json.newObject();
          ObjectNode hiryoList    = Json.newObject();
          ObjectNode nouyakuList  = Json.newObject();
          ObjectNode shukakuList  = Json.newObject();
          double tn                 = 0;
          double tp                 = 0;
          double tk                 = 0;
          double tmh                = 0;
          double tmn                = 0;
          long idxh                 = 0;
          long idxn                 = 0;
          long idxs                 = 0;
          double shukaku            = 0;
          java.sql.Date shukakuDate = null;
          List<WorkDiary> wds = WorkDiary.find.where().eq("kukaku_id", base.kukakuId).between("work_date", base.workStartDay, base.workEndDay).orderBy("work_date ASC").findList();
          for (WorkDiary wd : wds) {
            List<WorkDiarySanpu> wdsps = WorkDiarySanpu.getWorkDiarySanpuList(wd.workDiaryId);
            //----- 肥料情報の取得 -----
            long no   = 0;
            for (WorkDiarySanpu wdsp : wdsps) {
              Nouhi nh = Nouhi.getNouhiInfo(wdsp.nouhiId);
              if (nh == null) {
                continue;
              }
              if (nh.nouhiKind != AgryeelConst.NouhiKind.HIRYO) {
                continue;
              }
              double hosei = 1;
              if (nh.unitKind == 1 || nh.unitKind == 2) { //単位種別がKgかLの場合
                hosei = 0.001;
              }
              ObjectNode hiryo = Json.newObject();
              double ryo = 0;
              if (wdsp.bairitu != 0) {
                ryo = new BigDecimal((wdsp.sanpuryo * hosei) / wdsp.bairitu).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
              }
              no++;
              hiryo.put("date"    , sdfs.format(wd.workDate));
              hiryo.put("no"      , no);
              hiryo.put("name"    , nh.nouhiName);
              hiryo.put("kind"    , "");      //肥料種類は項目が存在しない
              hiryo.put("ryo"     , ryo);
              hiryo.put("ryounit" , Common.GetCommonValue(Common.ConstClass.UNIT, nh.unitKind));
              hiryo.put("bairitu" , wdsp.bairitu);
              hiryo.put("sanpuryo", (wdsp.sanpuryo * hosei));
              hiryo.put("unit"    , Common.GetCommonValue(Common.ConstClass.UNIT, nh.unitKind));
              hiryo.put("n"       , new BigDecimal(nh.n * ryo).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
              hiryo.put("p"       , new BigDecimal(nh.p * ryo).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
              hiryo.put("k"       , new BigDecimal(nh.k * ryo).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
              hiryo.put("money"   , 0);      //金額は項目が存在しない
              tn += new BigDecimal(nh.n * ryo).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
              tp += new BigDecimal(nh.p * ryo).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
              tk += new BigDecimal(nh.k * ryo).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
              idxh++;
              hiryoList.put(String.valueOf(idxh), hiryo);
            }
            //----- 農薬情報の取得 -----
            no   = 0;
            for (WorkDiarySanpu wdsp : wdsps) {
              Nouhi nh = Nouhi.getNouhiInfo(wdsp.nouhiId);
              if (nh == null) {
                continue;
              }
              if (nh.nouhiKind != AgryeelConst.NouhiKind.NOUYAKU) {
                continue;
              }
              double hosei = 1;
              if (nh.unitKind == 1 || nh.unitKind == 2) { //単位種別がKgかLの場合
                hosei = 0.001;
              }
              ObjectNode nouyaku = Json.newObject();
              double ryo = new BigDecimal((wdsp.sanpuryo * hosei) / wdsp.bairitu).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
              no++;
              nouyaku.put("date"    , sdfs.format(wd.workDate));
              nouyaku.put("no"      , no);
              nouyaku.put("name"    , nh.nouhiName);
              nouyaku.put("kind"    , "");      //肥料種類は項目が存在しない
              nouyaku.put("ryo"     , ryo);
              nouyaku.put("ryounit" , Common.GetCommonValue(Common.ConstClass.UNIT, nh.unitKind));
              nouyaku.put("bairitu" , wdsp.bairitu);
              nouyaku.put("seibun1" , "");      //主要成分は項目が存在しない
              nouyaku.put("seibun2" , "");      //主要成分は項目が存在しない
              nouyaku.put("sanpuryo", (wdsp.sanpuryo * hosei));
              nouyaku.put("unit"    , Common.GetCommonValue(Common.ConstClass.UNIT, nh.unitKind));
              nouyaku.put("money"   , 0);      //金額は項目が存在しない
              idxn++;
              nouyakuList.put(String.valueOf(idxn), nouyaku);
            }
            //----- 収穫情報の取得 -----
            Work wk = Work.getWork(wd.workId);
            if (wk == null || (wk.workTemplateId != AgryeelConst.WorkTemplate.SHUKAKU &&
                wk.workTemplateId != AgryeelConst.WorkTemplate.SENKA)) {
              continue;
            }

            if (shukakuDate != null && shukakuDate.compareTo(wd.workDate) != 0) {
              ObjectNode shukakuJson = Json.newObject();
              shukakuJson.put("date"    , sdfs.format(shukakuDate));
              shukakuJson.put("shukaku" , shukaku);
              idxs++;
              shukakuList.put(String.valueOf(idxs), shukakuJson);
              shukaku = 0;
            }

            shukaku      += wd.shukakuRyo;
            shukakuDate   = wd.workDate;

          }
          if (shukakuDate != null) {
            ObjectNode shukakuJson = Json.newObject();
            shukakuJson.put("date"    , sdfs.format(shukakuDate));
            shukakuJson.put("shukaku" , shukaku);
            idxs++;
            shukakuList.put(String.valueOf(idxs), shukakuJson);
          }
          hiryoinfo.put("n"     , new BigDecimal(tn).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
          hiryoinfo.put("p"     , new BigDecimal(tp).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
          hiryoinfo.put("k"     , new BigDecimal(tk).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
          hiryoinfo.put("money" , tmh);
          hiryoinfo.put("data"  , hiryoList);
          nouyakinfo.put("money", tmn);
          nouyakinfo.put("data" , nouyakuList);
          shukakuinfo.put("data", shukakuList);
          outjson.put("motocho" , motocho);
          outjson.put("hiryo"   , hiryoinfo);
          outjson.put("nouyaku" , nouyakinfo);
          outjson.put("shukaku" , shukakuinfo);
          datalist.put(decf.format(base.kukakuId) + decf.format(base.workYear) + decf.format(base.rotationSpeedOfYear), outjson);
        }
      }
      resultJson.put("out", datalist);
      return ok(resultJson);
    }
    public static Result WorkDiaryDataoutput(double farmId, String dateFrom, String dateTo) {
      ObjectNode resultJson = Json.newObject();

      //----- パラメータの調整 -----
      //生産者情報の取得
      Farm fm = Farm.find.where().eq("farm_id", farmId).findUnique();
      if (fm == null) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "指定された生産者ＩＤが存在しません。");
        return ok(resultJson);
      }

      //指定された範囲日付の変換
      SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
      java.sql.Date dFrom  = null;
      java.sql.Date dTo    = null;
      try {
        dFrom = new java.sql.Date(dateParse.parse(dateFrom).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（自）に誤りがあります。 FROM = " + dateFrom);
        return ok(resultJson);
      }
      try {
        dTo = new java.sql.Date(dateParse.parse(dateTo).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + dateTo);
        return ok(resultJson);
      }

      DecimalFormat decf  = new DecimalFormat("00000000");
      SimpleDateFormat sdfs = new SimpleDateFormat("MM/dd");
      SimpleDateFormat sdfh = new SimpleDateFormat("HH:mm");
      long totalCount = 0;
      List<MotochoBase> bases = null;
      ObjectNode datalist = Json.newObject();

      List <TimeLine>tls = TimeLine.find.where()
          .eq("farm_id", farmId)
          .between("work_date", dFrom, dTo)
          .orderBy("work_date ASC, update_time ASC")
          .findList();

      long totalC = 0;
      for (TimeLine tl :tls) {
        WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
        if (wd != null) {
          Work wk = Work.getWork(wd.workId);
          if (wk != null) {
            ObjectNode outjson = Json.newObject();
            //----- 基本情報 -----
            totalC++;
            ObjectNode base = Json.newObject();
            Calendar cWork = Calendar.getInstance();
            cWork.setTime(wd.workDate);
            base.put("id"       , tl.workDiaryId);
            base.put("template" , wk.workTemplateId);
            base.put("year"     , cWork.get(Calendar.YEAR));
            base.put("month"    , cWork.get(Calendar.MONTH) + 1);
            base.put("day"      , cWork.get(Calendar.DAY_OF_MONTH));
            //----- 作業情報 -----
            base.put("kukaku"   , tl.kukakuName);
            base.put("tanto"    , tl.accountName);
            if (wd.workStartTime != null) {
              base.put("start"    , sdfh.format(wd.workStartTime));
            }
            else {
              base.put("start"    , "");
            }
            if (wd.workEndTime != null) {
              base.put("end"    , sdfh.format(wd.workEndTime));
            }
            else {
              base.put("end"    , "");
            }
            base.put("time"     , wd.workTime);
            base.put("work"     , wk.workName);
            base.put("color"    , wk.workColor);
            outjson.put("base"  , base);

            switch ((int)wk.workTemplateId) {
            case AgryeelConst.WorkTemplate.SANPU:
              //----- 散布情報 -----
              List<WorkDiarySanpu> wdss = WorkDiarySanpu.getWorkDiarySanpuList(wd.workDiaryId);
              int idx = 0;
              ObjectNode sanpu  = Json.newObject();
              ObjectNode list   = Json.newObject();
              for(WorkDiarySanpu wds :wdss) {
                idx++;
                ObjectNode data   = Json.newObject();
                data.put("method",Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, wds.sanpuMethod, true));
                Kiki kiki = Kiki.getKikiInfo(wds.kikiId);
                if (kiki != null) {
                  data.put("kiki", kiki.kikiName);
                }
                else {
                  data.put("kiki", "未選択");
                }
                Attachment attachment = Attachment.getAttachmentInfo(wds.attachmentId);
                if (attachment != null) {
                  data.put("attachment", attachment.attachementName);
                }
                else {
                  data.put("attachment", "未選択");
                }
                Nouhi nh = Nouhi.getNouhiInfo(wds.nouhiId);
                if (nh != null) {
                  data.put("nouhi", nh.nouhiName);
                }
                else {
                  data.put("nouhi", "未選択");
                }
                data.put("bairitu", wds.bairitu);
                double hosei = 1;
                if (nh.unitKind == 1 || nh.unitKind == 2) { //単位種別がKgかLの場合
                  hosei = 0.001;
                }
                data.put("ryo"      , (wds.sanpuryo * hosei));
                data.put("unit"     , Common.GetCommonValue(Common.ConstClass.UNIT, nh.unitKind));
                list.put(String.valueOf(idx), data);
              }
              sanpu.put("count" , idx);
              sanpu.put("data"  , list);
              outjson.put("sanpu"  , sanpu);

              break;
            case AgryeelConst.WorkTemplate.NOUKO:
              //----- 農耕情報 -----
              ObjectNode noukou  = Json.newObject();
              Kiki kiki = Kiki.getKikiInfo(wd.kikiId);
              if (kiki != null) {
                noukou.put("kiki", kiki.kikiName);
              }
              else {
                noukou.put("kiki", "未選択");
              }
              Attachment attachment = Attachment.getAttachmentInfo(wd.attachmentId);
              if (attachment != null) {
                noukou.put("attachment", attachment.attachementName);
              }
              else {
                noukou.put("attachment", "未選択");
              }
              outjson.put("noukou"  , noukou);
              break;
            case AgryeelConst.WorkTemplate.HASHU:
              //----- 播種情報 -----
              ObjectNode hashu  = Json.newObject();
              double cropId = Hinsyu.getMultiHinsyuCropId(wd.hinsyuId);
              Crop crop = Crop.getCropInfo(cropId);
              if (crop != null) {
                hashu.put("crop", crop.cropName);
              }
              else {
                hashu.put("crop", "未選択");
              }
              hashu.put("hinsyu", Hinsyu.getMultiHinsyuName(wd.hinsyuId));

              kiki = Kiki.getKikiInfo(wd.kikiId);
              if (kiki != null) {
                hashu.put("kiki", kiki.kikiName);
              }
              else {
                hashu.put("kiki", "未選択");
              }
              attachment = Attachment.getAttachmentInfo(wd.attachmentId);
              if (attachment != null) {
                hashu.put("attachment", attachment.attachementName);
              }
              else {
                hashu.put("attachment", "未選択");
              }
              Belto belto = Belto.getBeltoInfo(wd.beltoId);
              if (belto != null) {
                hashu.put("belto", belto.beltoName);
              }
              else {
                hashu.put("belto", "未選択");
              }
              hashu.put("kabuma", wd.kabuma);
              hashu.put("joukan", wd.joukan);
              hashu.put("jousu" , wd.jousu);
              hashu.put("hukasa", wd.hukasa);
              outjson.put("hashu"  , hashu);
              break;
            case AgryeelConst.WorkTemplate.KANSUI:
              //----- 潅水情報 -----
              ObjectNode kansui  = Json.newObject();
              kansui.put("method"     , Common.GetCommonValue(Common.ConstClass.KANSUI, wd.kansuiMethod));
              kiki = Kiki.getKikiInfo(wd.kikiId);
              if (kiki != null) {
                kansui.put("kiki", kiki.kikiName);
              }
              else {
                kansui.put("kiki", "未選択");
              }
              kansui.put("ryo", wd.kansuiRyo);
              outjson.put("kansui"  , kansui);

              break;
            case AgryeelConst.WorkTemplate.SHUKAKU:
              //----- 収穫情報 -----
              List<WorkDiaryDetail> wdds = WorkDiaryDetail.getWorkDiaryDetailList(wd.workDiaryId);
              idx = 0;
              ObjectNode shukaku  = Json.newObject();
              list                = Json.newObject();
              for(WorkDiaryDetail wdd :wdds) {
                idx++;
                ObjectNode data   = Json.newObject();
                Nisugata nisugata = Nisugata.getNisugataInfo(wdd.syukakuNisugata);
                if (nisugata != null) {
                  data.put("nisugata", nisugata.nisugataName);
                }
                else {
                  data.put("nisugata", "未選択");
                }
                Size size = Size.getSizeInfo(wdd.syukakuSize);
                if (size != null) {
                  data.put("size", size.sizeName);
                }
                else {
                  data.put("size", "未選択");
                }
                Shitu shitu = Shitu.getShituInfo(wdd.syukakuSitsu);
                if (shitu != null) {
                  data.put("shitu", shitu.shituName);
                }
                else {
                  data.put("shitu", "未選択");
                }
                data.put("kosu"     , wdd.suryo);
                data.put("ryo"      , wdd.shukakuRyo);
                list.put(String.valueOf(idx), data);
              }
              shukaku.put("count"     , idx);
              shukaku.put("data"      , list);
              outjson.put("shukaku"   , shukaku);

              break;
            case AgryeelConst.WorkTemplate.COMMENT:
              //----- コメント情報 -----
              wdds = WorkDiaryDetail.getWorkDiaryDetailList(wd.workDiaryId);
              idx = 0;
              ObjectNode comment  = Json.newObject();
              list                = Json.newObject();
              for(WorkDiaryDetail wdd :wdds) {
                idx++;
                ObjectNode data   = Json.newObject();
                data.put("comment"    , wdd.comment);
                list.put(String.valueOf(idx), data);
              }
              comment.put("count"     , idx);
              comment.put("data"      , list);
              outjson.put("comment"   , comment);

              break;

            default:
              break;
            }

            datalist.put( decf.format(wd.workDiaryId), outjson);

          }
          else {
            Logger.error("WORK NOT FOUND WORKID={}", wd.workId);
          }
        }
        else {
          Logger.error("TIMELINE -> WORKDIARY CHANGE ERROR WORKDIARYID={}", tl.workDiaryId);
        }
      }

      resultJson.put("count", totalC);
      resultJson.put("out"  , datalist);
      return ok(resultJson);
    }
  //public static Result ShukakuDataOutput(double kukakuId, String dateFrom, String dateTo) {
    public static Result ShukakuDataOutput(double farmId, String dateFrom, String dateTo, String cropId) {
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      boolean     api        = false;

      if (session(AgryeelConst.SessionKey.API) != null) {
      	api = true;
      }

      //----- パラメータの調整 -----
      //区画情報の取得
//      Compartment cp = Compartment.getCompartmentInfo(kukakuId);
//      if (cp == null) {
//        resultJson.put("result", AgryeelConst.Result.ERROR);
//        resultJson.put("message", "指定された区画が存在しません。");
//        return ok(resultJson);
//      }

      //指定された範囲日付の変換
      SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
      java.sql.Date dFrom  = null;
      java.sql.Date dTo    = null;
      try {
        dFrom = new java.sql.Date(dateParse.parse(dateFrom).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（自）に誤りがあります。 FROM = " + dateFrom);
        return ok(resultJson);
      }
      try {
        dTo = new java.sql.Date(dateParse.parse(dateTo).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + dateTo);
        return ok(resultJson);
      }
      List<Double> cropList = new ArrayList<Double>();
      if(!"NONE".equals(cropId)) {
        String[] crops = cropId.split(",");
        for (String crop : crops) {
          cropList.add(Double.parseDouble(crop));
        }
      }

      SimpleDateFormat sdfs = new SimpleDateFormat("MM/dd");
      ObjectNode datalist = Json.newObject();
      ArrayNode datalistApi = mapper.createArrayNode();

      long dateCount = DateU.GetDiffDate(dFrom, dTo);
      Calendar sys = Calendar.getInstance();
      sys.setTime(dFrom);
      long totalC       = 0;
      for (int i=0; i <= dateCount; i++) {
        java.sql.Date sysdate = new java.sql.Date(sys.getTimeInMillis());
        java.sql.Timestamp sysTimestamp = new java.sql.Timestamp(sys.getTimeInMillis());
        List<TimeLine> tls = TimeLine.getTimeLineOfFarm(farmId, sysTimestamp, sysTimestamp);
        double shukakuryo = 0;
        Analysis als = new Analysis();
        for (TimeLine tl : tls ) {

          WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
          if (wd != null) {
            Work wk = Work.getWork(wd.workId);
            if (wk != null) {
              if (wk.workTemplateId != AgryeelConst.WorkTemplate.SHUKAKU &&
                  wk.workTemplateId != AgryeelConst.WorkTemplate.SENKA) {
                continue;
              }
              //----- 収穫量 -----
              Compartment cp = Compartment.getCompartmentInfo(wd.kukakuId);
              List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", cp.kukakuId).le("shukaku_start_date", sysdate).ge("shukaku_end_date", sysdate).orderBy("work_year ASC, rotation_speed_of_year ASC").findList();
              for (MotochoBase mb : mbs) {
                if (cropList.size() > 0) {
                  if (!ListrU.keyCheck(cropList, mb.cropId)) {
                    continue;
                  }
                }
                shukakuryo += wd.shukakuRyo;
                als.add(wd.kukakuId);
                als.put(wd.kukakuId, wd.shukakuRyo);
                break;
              }
            }
            else {
              Logger.error("WORK NOT FOUND WORKID={}", wd.workId);
            }
          }
        }
        //----- 積算情報 -----
        totalC++;
        ObjectNode base = Json.newObject();
        Calendar cWork = Calendar.getInstance();
        cWork.setTime(sysdate);
        base.put("year"     , cWork.get(Calendar.YEAR));
        base.put("month"    , cWork.get(Calendar.MONTH) + 1);
        base.put("day"      , cWork.get(Calendar.DAY_OF_MONTH));
        base.put("date"     , sdfs.format(cWork.getTime()));

        /* 収穫対象となった区画の平均積算情報を算出する */
        List<Double>keys = als.getKeys();

        double kion = 0;
        double rain = 0;
        long seiiku = 0;
        long count  = 0;
        ObjectNode kukaku = Json.newObject();
        ArrayNode kukakuApi = mapper.createArrayNode();
        for (Double key : keys) {
          Compartment cp = Compartment.getCompartmentInfo(key.doubleValue());
          Field field = cp.getFieldInfo();
          FieldGroup fg = cp.getFieldGroupInfo();

          String points = PosttoPoint.getPointId(field.postNo);
          List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", cp.kukakuId).le("shukaku_start_date", sysdate).ge("shukaku_end_date", sysdate).orderBy("work_year ASC, rotation_speed_of_year ASC").findList();
          for (MotochoBase mb : mbs) {
            if (mb.hashuDate == null) {
              continue;
            }
            if (cropList.size() > 0) {
              if (!ListrU.keyCheck(cropList, mb.cropId)) {
                continue;
              }
            }
            seiiku += DateU.GetDiffDate(mb.hashuDate, sysdate);
            if (!"".equals(points)) {
              List<Weather> wths = Weather.getWeather(points, mb.hashuDate, sysdate);

              for (Weather wth : wths) {
                kion += wth.kionAve;
                rain += wth.rain;
              }
            }
            ObjectNode cpdata = Json.newObject();
            cpdata.put("id"     , cp.kukakuId);
            cpdata.put("name"   , cp.kukakuName);
            cpdata.put("shukakuryo", als.data(cp.kukakuId));
            cpdata.put("seiiku" , DateU.GetDiffDate(mb.hashuDate, sysdate));
            cpdata.put("kion"   , new BigDecimal(kion).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            cpdata.put("rain"   , new BigDecimal(rain).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            if (DateU.GetDiffDate(mb.hashuDate, sysdate) != 0) {
              cpdata.put("kionave"   , new BigDecimal(kion / DateU.GetDiffDate(mb.hashuDate, sysdate)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            else {
              cpdata.put("kionave"   , 0);
            }
            kukaku.put(String.valueOf(cp.kukakuId), cpdata);
            kukakuApi.add(cpdata);
            count++;
            break;
          }
        }
        if(api){
          base.put("kukaku", kukakuApi);
        }else{
          base.put("kukaku", kukaku);
        }
        base.put("shukakuryo"    , new BigDecimal(shukakuryo).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        if (count == 0) {
          base.put("seiiku"    , 0);
          base.put("kion"      , 0);
          base.put("rain"      , 0);
          base.put("kionave"   , 0);
        }
        else {
          base.put("seiiku"    , new BigDecimal(seiiku  / count).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
          base.put("kion"      , new BigDecimal(kion    / count).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
          base.put("rain"      , new BigDecimal(rain    / count).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
          if (seiiku > 0) {
            base.put("kionave"   , new BigDecimal(kion    / seiiku).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
          }
          else {
            base.put("kionave"   , 0);
          }
        }
        if(api){
          datalistApi.add(base);
        }else{
          datalist.put( dateParse.format(sysdate), base);
        }
        shukakuryo = 0;
        als.clear();
        sys.add(Calendar.DAY_OF_MONTH, 1);
      }
      resultJson.put("count", totalC);
      if(api){
        resultJson.put("out"  , datalistApi);
      }else{
        resultJson.put("out"  , datalist);
      }
      return ok(resultJson);
    }
    public static Result TotalShukakuDataOutput(double farmId, String dateFrom, String dateTo) {
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      boolean     api        = false;

      if (session(AgryeelConst.SessionKey.API) != null) {
        api = true;
      }

      //----- パラメータの調整 -----
      //指定された範囲日付の変換
      SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
      java.sql.Timestamp dFrom  = null;
      java.sql.Timestamp dTo    = null;
      try {
        dFrom = new java.sql.Timestamp(dateParse.parse(dateFrom).getTime());
        Calendar cFrom = Calendar.getInstance();
        cFrom.setTime(dFrom);
        DateU.setTime(cFrom, DateU.TimeType.FROM);
        dFrom = new java.sql.Timestamp(cFrom.getTimeInMillis());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（自）に誤りがあります。 FROM = " + dateFrom);
        return ok(resultJson);
      }
      try {
        dTo = new java.sql.Timestamp(dateParse.parse(dateTo).getTime());
        Calendar cTo = Calendar.getInstance();
        cTo.setTime(dTo);
        DateU.setTime(cTo, DateU.TimeType.TO);
        dTo = new java.sql.Timestamp(cTo.getTimeInMillis());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + dateTo);
        return ok(resultJson);
      }

      SimpleDateFormat sdfs = new SimpleDateFormat("MM/dd");
      ObjectNode datalist = Json.newObject();
      ArrayNode datalistApi = mapper.createArrayNode();

      List<Crop> crops = new ArrayList<Crop>();
      CropGroup cg = CropGroup.find.where().eq("farm_id", farmId).orderBy("crop_group_id").findUnique();
      if (cg != null) { //生産物が存在する場合

        List<CropGroupList> cgl = CropGroupList.find.where().eq("crop_group_id", cg.cropGroupId).order("crop_id").findList();
        for (CropGroupList cropGroupList : cgl) {

          Crop crop = Crop.find.where().eq("crop_id", cropGroupList.cropId).findUnique();

          if (crop != null) {
            crops.add(crop);
          }
        }
      }
      else {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "品目が存在しません");
        return ok(resultJson);
      }

      List<TimeLine> tls = TimeLine.getTimeLineOfFarm(farmId, dFrom, dTo);
      double total = 0;
      for (Crop crop : crops) {
        double totalc = 0;
        for (TimeLine tl : tls ) {
          WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
          if (wd != null) {
            Work wk = Work.getWork(wd.workId);
            if (wk != null) {
              if (wk.workTemplateId != AgryeelConst.WorkTemplate.SHUKAKU &&
                  wk.workTemplateId != AgryeelConst.WorkTemplate.SENKA) {
                continue;
              }
              Compartment cp = Compartment.getCompartmentInfo(wd.kukakuId);
              List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", cp.kukakuId).le("shukaku_start_date", wd.workDate).ge("shukaku_end_date", wd.workDate).orderBy("work_year ASC, rotation_speed_of_year ASC").findList();
              for (MotochoBase mb : mbs) {
                if (crop.cropId != mb.cropId) {
                  continue;
                }
                totalc += wd.shukakuRyo;
                total  += wd.shukakuRyo;
                break;
              }
            }
            else {
              Logger.error("WORK NOT FOUND WORKID={}", wd.workId);
            }
          }
        }
        ObjectNode base = Json.newObject();
        base.put("id"         , crop.cropId);
        base.put("name"       , crop.cropName);
        base.put("color"      , crop.cropColor);
        base.put("shukakuRyo" , new BigDecimal(totalc).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        datalist.put(String.valueOf(crop.cropId), base);
        datalistApi.add(base);
      }
      resultJson.put("shukakuRyo", new BigDecimal(total).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
      if(api){
        resultJson.put("out"  , datalistApi);
      }else{
        resultJson.put("out"  , datalist);
      }
      return ok(resultJson);
    }
    public static Result ShukakuKukakuDataOutput(double kukakuId, String dateTo) {
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      boolean     api        = false;

      if (session(AgryeelConst.SessionKey.API) != null) {
      	api = true;
      }

      //----- パラメータの調整 -----
      //区画情報の取得
      Compartment cp = Compartment.getCompartmentInfo(kukakuId);
      if (cp == null) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "指定された区画が存在しません。");
        return ok(resultJson);
      }

      //指定された範囲日付の変換
      SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
      java.sql.Date dFrom  = null;
      java.sql.Date dTo    = null;
      try {
        dTo = new java.sql.Date(dateParse.parse(dateTo).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + dateTo);
        return ok(resultJson);
      }

      SimpleDateFormat sdfs = new SimpleDateFormat("MM/dd");
      ObjectNode datalist = Json.newObject();
      ArrayNode datalistApi = mapper.createArrayNode();

      Calendar sys = Calendar.getInstance();
      sys.setTime(dTo);
      sys.add(Calendar.MONTH, -2);
      dFrom = new java.sql.Date(sys.getTimeInMillis());
      long dateCount = DateU.GetDiffDate(dFrom, dTo);
      long totalC       = 0;
      for (int i=0; i <= dateCount; i++) {
        java.sql.Date sysdate = new java.sql.Date(sys.getTimeInMillis());
        java.sql.Timestamp sysTimestamp = new java.sql.Timestamp(sys.getTimeInMillis());
        List<TimeLine> tls = TimeLine.getTimeLineOfRange(kukakuId, sysTimestamp, sysTimestamp);
        double shukakuryo = 0;
        Analysis als = new Analysis();
        for (TimeLine tl : tls ) {

          WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
          if (wd != null) {
            Work wk = Work.getWork(wd.workId);
            if (wk != null) {
              if (wk.workTemplateId != AgryeelConst.WorkTemplate.SHUKAKU &&
                  wk.workTemplateId != AgryeelConst.WorkTemplate.SENKA) {
                continue;
              }
              //----- 収穫量 -----
              shukakuryo += wd.shukakuRyo;
              als.add(wd.kukakuId);
              als.put(wd.kukakuId, wd.shukakuRyo);
            }
            else {
              Logger.error("WORK NOT FOUND WORKID={}", wd.workId);
            }
          }
        }
        //----- 積算情報 -----
        totalC++;
        ObjectNode base = Json.newObject();
        Calendar cWork = Calendar.getInstance();
        cWork.setTime(sysdate);
        base.put("year"     , cWork.get(Calendar.YEAR));
        base.put("month"    , cWork.get(Calendar.MONTH) + 1);
        base.put("day"      , cWork.get(Calendar.DAY_OF_MONTH));
        base.put("date"     , sdfs.format(cWork.getTime()));

        double kion = 0;
        double rain = 0;
        long seiiku = 0;

        Field field = cp.getFieldInfo();
        String points = PosttoPoint.getPointId(field.postNo);
        List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", cp.kukakuId).le("shukaku_start_date", dTo).ge("shukaku_end_date", dTo).orderBy("work_year ASC, rotation_speed_of_year ASC").findList();
        for (MotochoBase mb : mbs) {
          if (mb.hashuDate == null) {
            continue;
          }
          seiiku = DateU.GetDiffDate(mb.hashuDate, sysdate);
          if (seiiku < 0) {
            seiiku = 0;
          }
          if (!"".equals(points)) {
            List<Weather> wths = Weather.getWeather(points, mb.hashuDate, sysdate);

            for (Weather wth : wths) {
              kion += wth.kionAve;
              rain += wth.rain;
            }
          }
          break;
        }
        base.put("shukakuryo"   , new BigDecimal(shukakuryo).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        base.put("seiiku"       , new BigDecimal(seiiku).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        base.put("kion"         , new BigDecimal(kion).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        base.put("rain"         , new BigDecimal(rain).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        if (seiiku != 0) {
          base.put("kionave"      , new BigDecimal(kion / seiiku).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        else {
          base.put("kionave"      , 0);
        }
        datalist.put( dateParse.format(sysdate), base);
        datalistApi.add(base);
        resultJson.put("kion"     , new BigDecimal(kion).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        resultJson.put("rain"     , new BigDecimal(rain).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        if (seiiku != 0) {
          resultJson.put("kionave"  , new BigDecimal(kion / seiiku).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        else {
          resultJson.put("kionave"  , 0);
        }
        shukakuryo = 0;
        als.clear();
        sys.add(Calendar.DAY_OF_MONTH, 1);
      }
      resultJson.put("count", totalC);
      if(api){
        resultJson.put("out"  , datalistApi);
      }else{
        resultJson.put("out"  , datalist);
      }
      return ok(resultJson);
    }
    public static Result TotalWorkDataOutput(double farmId, String dateFrom, String dateTo, String accounts, String works, String crops) {
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      boolean     api        = false;

      if (session(AgryeelConst.SessionKey.API) != null) {
        api = true;
      }

      //----- パラメータの調整 -----
      //指定された範囲日付の変換
      SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
      java.sql.Date dFrom  = null;
      java.sql.Date dTo    = null;
      try {
        dFrom = new java.sql.Date(dateParse.parse(dateFrom).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（自）に誤りがあります。 FROM = " + dateFrom);
        return ok(resultJson);
      }
      try {
        dTo = new java.sql.Date(dateParse.parse(dateTo).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + dateTo);
        return ok(resultJson);
      }

      SimpleDateFormat sdfs = new SimpleDateFormat("yyyy/MM");
      ObjectNode datalist = Json.newObject();
      ArrayNode datalistApi = mapper.createArrayNode();
      List<String> acs = new ArrayList<String>();
      if (!"NONE".equals(accounts)) {
        String[] account_s = accounts.split(",");
        for (String ac : account_s) {
          acs.add(ac);
        }
      }
      List<Double> wks = new ArrayList<Double>();
      if (!"NONE".equals(works)) {
        String[] work_s = works.split(",");
        for (String wk : work_s) {
          wks.add(Double.parseDouble(wk));
        }
      }
      List<Double> cps = new ArrayList<Double>();
      if (!"NONE".equals(crops)) {
        String[] crop_s = crops.split(",");
        for (String crop : crop_s) {
          cps.add(Double.parseDouble(crop));
        }
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(dFrom);
      cal.set(Calendar.MONTH, 0);
      cal.set(Calendar.DAY_OF_MONTH, 1);

      Calendar calE = Calendar.getInstance();
      calE.setTime(dFrom);
      calE.set(Calendar.MONTH, 11);
      calE.set(Calendar.DAY_OF_MONTH, 31);

      Analysis als = new Analysis();
      List<Work> workl = Work.getWorkOfFarm(0);

      if (workl.size() > 0) { //該当データが存在する場合
        for (Work work : workl) {
          if ((wks.size() > 0) && (!ListrU.keyCheck(wks, work.workId))) {
            continue;
          }
          als.add(work.workId);
        }
      }

      workl = Work.getWorkOfFarm(farmId);

      if (workl.size() > 0) { //該当データが存在する場合
        for (Work work : workl) {
          if ((wks.size() > 0) && (!ListrU.keyCheck(wks, work.workId))) {
            continue;
          }
          als.add(work.workId);
        }
      }

      long total = 0;
      Calendar end = Calendar.getInstance();
      while(true) {
        end.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        end.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        end.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH));
        List<TimeLine> tls = TimeLine.find.where().eq("farm_id", farmId).between("work_date", new java.sql.Date(cal.getTimeInMillis()), new java.sql.Date(end.getTimeInMillis())).orderBy("work_date ASC, work_id ASC").findList();
        for (TimeLine tl : tls) {
          //----- アカウントチェック -----
          if ((acs.size() > 0) && (!ListrU.keyCheckStr(acs, tl.accountId))) {
            continue;
          }
          if ((wks.size() > 0) && (!ListrU.keyCheck(wks, tl.workId))) {
            continue;
          }
          Compartment cp = Compartment.getCompartmentInfo(tl.kukakuId);
          if (cp == null) {
            continue;
          }
          CompartmentWorkChainStatus cwcs = cp.getCompartmentWorkChainStatus();
          if (cwcs == null) {
            continue;
          }
          if ((cps.size() > 0) && (!ListrU.keyCheck(cps, cwcs.cropId))) {
            continue;
          }

          WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
          if (wd != null) {
            als.put(tl.workId, wd.workTime);
            total += wd.workTime;
          }
        }
        ObjectNode base = Json.newObject();
        base.put("month"       , sdfs.format(cal.getTime()));
        ObjectNode workList = Json.newObject();
        ArrayNode workListApi = mapper.createArrayNode();
        List<Double> keys = als.getKeys();
        for (Double key : keys) {
          Work wk = Work.getWork(key);
          if (wk != null) {
            ObjectNode work = Json.newObject();
            work.put("id"         , wk.workId);
            work.put("name"       , wk.workName);
            work.put("color"      , wk.workColor);
            work.put("time"       , new BigDecimal(als.data(key) / 60).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            workList.put(String.valueOf(key), work);
            workListApi.add(work);
          }
        }
        base.put("time"           , new BigDecimal(total / 60).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        if(api){
          base.put("work"           , workListApi);
        }else{
          base.put("work"           , workList);
        }
        datalist.put(dateParse.format(cal.getTime()), base);
        datalistApi.add(base);
        cal.add(Calendar.MONTH, 1);
        if (calE.getTime().compareTo(cal.getTime()) < 0) {
          break;
        }
        total = 0;
        als.clearValue();
      }
      if(api){
        resultJson.put("out"  , datalistApi);
      }else{
        resultJson.put("out"  , datalist);
      }
      return ok(resultJson);
    }
    public static Result getCompartmentSpreadOfFarm(double farmId) {
      ObjectNode resultJson = Json.newObject();

      //----- パラメータの調整 -----
      //区画情報の取得
      List<Compartment> cps = Compartment.getCompartmentOfFarm(farmId);
      ObjectNode datalist = Json.newObject();
      long totalC = 0;
      DecimalFormat decf  = new DecimalFormat("00000000");
      for (Compartment cp :cps) {
        totalC++;
        ObjectNode base = Json.newObject();
        base.put("id"   , cp.kukakuId);
        base.put("name" , cp.kukakuName);
        datalist.put( decf.format(cp.kukakuId), base);
      }
      resultJson.put("count", totalC);
      resultJson.put("out"  , datalist);
      return ok(resultJson);
    }
    /**
     * 茶園一覧表を取得する
     * @param farmId
     * @param year
     * @return
     */
    public static Result getChaenList(double farmId, int year) {
      ObjectNode resultJson = Json.newObject();
      ObjectNode datalist = Json.newObject();

      List<Compartment> cts =  Compartment.getCompartmentOfFarm(farmId);

      int no = 0;
      for (Compartment ct : cts) {
        no++;
        ObjectNode json = Json.newObject();
        json.put("no", no);
        json.put("name", ct.kukakuName);
        MotochoBase base = MotochoBase.find.where().eq("kukaku_id",ct.kukakuId).eq("work_year", year).findUnique();
        if (base != null) {
          json.put("hinsyu", base.hinsyuName);
          json.put("area", base.area);
        }
        else {
          json.put("hinsyu", "");
          json.put("area", ct.area);
        }
        datalist.put(String.valueOf(no), json);
      }

      resultJson.put("out"  , datalist);
      return ok(resultJson);
    }
    /**
     * 病害虫防除記録を取得する
     * @param farmId
     * @param year
     * @return
     */
    public static Result getByouGaiBoujoList(double farmId, int year) {
      ObjectNode resultJson = Json.newObject();
      ObjectNode datalist = Json.newObject();

      List<Nouhi> nouhis = Nouhi.find.where().eq("farm_id", farmId).eq("nouhi_kind", 1).orderBy("nouhi_id").findList();
      List<Compartment> cts =  Compartment.getCompartmentOfFarm(farmId);
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
      DecimalFormat df = new DecimalFormat("#,##0");

      int nouhino = 0;
      for (Nouhi nouhi : nouhis) {
        nouhino++;
        ObjectNode json = Json.newObject();
        json.put("name"   , nouhi.nouhiName);
        json.put("bairitu", df.format(nouhi.bairitu));
        json.put("ryo"    , df.format(nouhi.sanpuryo * 0.001));

        int no = 0;
        Calendar from = Calendar.getInstance();
        Calendar to   = Calendar.getInstance();
        from.set(year, 0, 1);
        to.set(year, 11, 31);
        ObjectNode sanpus = Json.newObject();
        for (Compartment ct : cts) {
          no++;
          ObjectNode sanpu = Json.newObject();
          sanpu.put("no", no);
          sanpu.put("name", ct.kukakuName);
          sanpu.put("date", "");
          List<WorkDiary> wds = WorkDiary.getWorkDiaryOfWork(ct.kukakuId, new java.sql.Timestamp(from.getTimeInMillis()), new java.sql.Timestamp(to.getTimeInMillis()));
          for (WorkDiary wd :wds) {
            if (wd.workId != 8) { //消毒以外
              continue;
            }
            WorkDiarySanpu wdsp = WorkDiarySanpu.find.where().eq("work_diary_id", wd.workDiaryId).eq("nouhi_id", nouhi.nouhiId).findUnique();
            if (wdsp != null) {
              sanpu.put("date", sdf.format(wd.workDate));
              break;
            }
          }
          sanpus.put(String.valueOf(no), sanpu);
        }
        json.put("sanpu", sanpus);
        datalist.put(String.valueOf(nouhino), json);
      }

      resultJson.put("out"  , datalist);
      return ok(resultJson);
    }
    /**
     * 施肥記録を取得する
     * @param farmId
     * @param year
     * @return
     */
    public static Result getShihiList(double farmId, int year) {
      ObjectNode resultJson = Json.newObject();
      ObjectNode datalist = Json.newObject();

      List<Nouhi> nouhis = Nouhi.find.where().eq("farm_id", farmId).eq("nouhi_kind", 2).orderBy("nouhi_id").findList();
      List<Compartment> cts =  Compartment.getCompartmentOfFarm(farmId);
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
      DecimalFormat df = new DecimalFormat("#,##0");

      int nouhino = 0;
      for (Nouhi nouhi : nouhis) {
        nouhino++;
        ObjectNode json = Json.newObject();
        json.put("name"   , nouhi.nouhiName);
        json.put("ryo"    , df.format(nouhi.sanpuryo * 0.001));

        int no = 0;
        Calendar from = Calendar.getInstance();
        Calendar to   = Calendar.getInstance();
        from.set(year, 0, 1);
        to.set(year, 11, 31);
        ObjectNode sanpus = Json.newObject();
        for (Compartment ct : cts) {
          no++;
          ObjectNode sanpu = Json.newObject();
          sanpu.put("no", no);
          sanpu.put("name", ct.kukakuName);
          sanpu.put("date", "");
          List<WorkDiary> wds = WorkDiary.getWorkDiaryOfWork(ct.kukakuId, new java.sql.Timestamp(from.getTimeInMillis()), new java.sql.Timestamp(to.getTimeInMillis()));
          for (WorkDiary wd :wds) {
            if (wd.workId != 3 && wd.workId != 10) { //肥料散布/追肥以外
              continue;
            }
            WorkDiarySanpu wdsp = WorkDiarySanpu.find.where().eq("work_diary_id", wd.workDiaryId).eq("nouhi_id", nouhi.nouhiId).findUnique();
            if (wdsp != null) {
              sanpu.put("date", sdf.format(wd.workDate));
              break;
            }
          }
          sanpus.put(String.valueOf(no), sanpu);
        }
        json.put("sanpu", sanpus);
        datalist.put(String.valueOf(nouhino), json);
      }

      resultJson.put("out"  , datalist);
      return ok(resultJson);
    }
    /**
     * 摘採記録を取得する
     * @param farmId
     * @param year
     * @return
     */
    public static Result getTekisaiList(double farmId, int year) {
      ObjectNode resultJson = Json.newObject();
      ObjectNode datalist = Json.newObject();

      List<Compartment> cts =  Compartment.getCompartmentOfFarm(farmId);
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
      DecimalFormat df = new DecimalFormat("#,##0.0");

      int no = 0;
      Calendar from = Calendar.getInstance();
      Calendar to   = Calendar.getInstance();
      from.set(year, 0, 1);
      to.set(year, 11, 31);
      for (Compartment ct : cts) {
        no++;
        ObjectNode json = Json.newObject();
        json.put("no", no);
        json.put("name", ct.kukakuName);
        json.put("dateh1", "");
        json.put("datet1", "");
        json.put("dates1", "");
        json.put("dateh2", "");
        json.put("datet2", "");
        json.put("dates2", "");
        json.put("datet3", "");
        json.put("dates3", "");
        json.put("datet4", "");
        json.put("dates4", "");
        List<WorkDiary> wds = WorkDiary.getWorkDiaryOfWork(ct.kukakuId, new java.sql.Timestamp(from.getTimeInMillis()), new java.sql.Timestamp(to.getTimeInMillis()));
        int tekisai = 0;
        for (WorkDiary wd :wds) {
          if (wd.workId != 82) { //摘採以外
            continue;
          }
          tekisai++;
          if (4 < tekisai) {
            break;
          }
          switch (tekisai) {
          case 2:
            json.put("datet2", sdf.format(wd.workDate));
            json.put("dates2", df.format(wd.shukakuRyo));
            break;

          case 3:
            json.put("datet3", sdf.format(wd.workDate));
            json.put("dates3", df.format(wd.shukakuRyo));
            break;

          case 4:
            json.put("datet4", sdf.format(wd.workDate));
            json.put("dates4", df.format(wd.shukakuRyo));
            break;

          default:
            json.put("datet1", sdf.format(wd.workDate));
            json.put("dates1", df.format(wd.shukakuRyo));
            break;
          }
        }
        datalist.put(String.valueOf(no), json);
      }
      resultJson.put("out"  , datalist);
      return ok(resultJson);
    }
    public static Result AccountUnitDataOutput(double farmId, String dateFrom, String dateTo) {
      ObjectNode resultJson = Json.newObject();

      //----- パラメータの調整 -----
      //指定された範囲日付の変換
      SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
      DecimalFormat    df        = new DecimalFormat("#,##0.0");
      java.sql.Timestamp dFrom  = null;
      java.sql.Timestamp dTo    = null;
      try {
        dFrom = new java.sql.Timestamp(dateParse.parse(dateFrom).getTime());
        Calendar cFrom = Calendar.getInstance();
        cFrom.setTime(dFrom);
        DateU.setTime(cFrom, DateU.TimeType.FROM);
        dFrom = new java.sql.Timestamp(cFrom.getTimeInMillis());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（自）に誤りがあります。 FROM = " + dateFrom);
        return ok(resultJson);
      }
      try {
        dTo = new java.sql.Timestamp(dateParse.parse(dateTo).getTime());
        Calendar cTo = Calendar.getInstance();
        cTo.setTime(dTo);
        DateU.setTime(cTo, DateU.TimeType.TO);
        dTo = new java.sql.Timestamp(cTo.getTimeInMillis());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + dateTo);
        return ok(resultJson);
      }

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      ObjectNode datalist = Json.newObject();

      List<Crop> crops = new ArrayList<Crop>();
      CropGroup cg = CropGroup.find.where().eq("farm_id", farmId).orderBy("crop_group_id").findUnique();
      if (cg != null) { //生産物が存在する場合

        List<CropGroupList> cgl = CropGroupList.find.where().eq("crop_group_id", cg.cropGroupId).order("crop_id").findList();
        for (CropGroupList cropGroupList : cgl) {

          Crop crop = Crop.find.where().eq("crop_id", cropGroupList.cropId).findUnique();

          if (crop != null) {
            crops.add(crop);
          }
        }
      }
      else {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "品目が存在しません");
        return ok(resultJson);
      }

      List<Account> accounts = Account.getAccountOfFarm(farmId);
      Analysis alsTC = new Analysis();  //全体収穫量
      Analysis alsTT = new Analysis();  //全体作業時間
      for (Account account : accounts) {
        ObjectNode accountJson = Json.newObject();
        accountJson.put("id", account.accountId);
        accountJson.put("name", account.acountName);

        ObjectNode dayJson = Json.newObject();
        List<TimeLine> tls = TimeLine.getTimeLineOfAccount(account.accountId, farmId, dFrom, dTo);
        ObjectNode cropJson = Json.newObject();
        Analysis alsC = new Analysis(); //担当者別収穫量
        Analysis alsT = new Analysis(); //担当者別作業時間
        double wkt = 0;
        double pt  = 0;
        double shukakuRyo  = 0;
        for (TimeLine tl : tls ) {
          WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
          if (wd != null) {
            Work wk = Work.getWork(wd.workId);
            if (wk != null) {
              if (wk.workTemplateId != AgryeelConst.WorkTemplate.SHUKAKU &&
                  wk.workTemplateId != AgryeelConst.WorkTemplate.SENKA) {
                continue;
              }
              Compartment cp = Compartment.getCompartmentInfo(wd.kukakuId);
              List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", cp.kukakuId).le("shukaku_start_date", wd.workDate).ge("shukaku_end_date", wd.workDate).orderBy("work_year ASC, rotation_speed_of_year ASC").findList();
              for (MotochoBase mb : mbs) {
                //----- 担当者別の集計結果 -----
                alsC.add(mb.cropId);
                alsC.put(mb.cropId, wd.shukakuRyo);
                alsT.add(mb.cropId);
                alsT.put(mb.cropId, wd.workTime);
                wkt += wd.workTime;
                shukakuRyo += wd.shukakuRyo;
                //----- 全体の集計結果 -----
                alsTC.add(mb.cropId);
                alsTC.put(mb.cropId, wd.shukakuRyo);
                alsTT.add(mb.cropId);
                alsTT.put(mb.cropId, wd.workTime);
                break;
              }
            }
            else {
              Logger.error("WORK NOT FOUND WORKID={}", wd.workId);
            }
          }
        }
        Collections.sort(alsC.getKeys());
        for (Double key : alsC.getKeys()) {
          ObjectNode base = Json.newObject();
          Crop crop = Crop.getCropInfo(key);
          if (crop == null) {continue;}
          base.put("id"         , crop.cropId);
          base.put("name"       , crop.cropName);
          base.put("color"      , crop.cropColor);
          if (alsC.data(key) == 0) {
            base.put("shukakuRyo" , 0);
          }
          else {
            base.put("shukakuRyo" , df.format(new BigDecimal(alsC.data(key)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
          }
          if (alsT.data(key) == 0) {
            base.put("time"       , 0);
          }
          else {
            base.put("time"       , df.format(new BigDecimal(alsT.data(key) / 60).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
          }
          if (alsC.data(key) == 0 || alsT.data(key) == 0) {
            base.put("unitSyukaku", 0);
            base.put("point"      , 0);
          }
          else {
            base.put("unitSyukaku", df.format(new BigDecimal(alsC.data(key) / (alsT.data(key) / 60)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
            base.put("point"      , new BigDecimal(alsC.data(key) / (alsT.data(key) / 60)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            pt += new BigDecimal(alsC.data(key) / (alsT.data(key) / 60)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
          }
          cropJson.put(String.valueOf(crop.cropId), base);
        }
        accountJson.put("crops"   , cropJson);
        accountJson.put("worktime", df.format(new BigDecimal(wkt / 60).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
        accountJson.put("point"   , pt);
        accountJson.put("shukakuRyo"   , shukakuRyo);
        accountJson.put("start" , sdf.format(dFrom));
        accountJson.put("end"   , sdf.format(dTo));
        datalist.put(account.accountId, accountJson);
      }
      Collections.sort(alsTC.getKeys());
      ObjectNode cropJson = Json.newObject();
      for (Double key : alsTC.getKeys()) {
        ObjectNode base = Json.newObject();
        Crop crop = Crop.getCropInfo(key);
        if (crop == null) {continue;}
        base.put("id"         , crop.cropId);
        base.put("name"       , crop.cropName);
        base.put("color"      , crop.cropColor);
        base.put("shukakuRyo" , df.format(new BigDecimal(alsTC.data(key)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
        base.put("time"       , df.format(new BigDecimal(alsTT.data(key) / 60).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
        base.put("unitSyukaku", df.format(new BigDecimal(alsTC.data(key) / (alsTT.data(key) / 60)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
        base.put("point"      , new BigDecimal(alsTC.data(key) / (alsTT.data(key) / 60)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        cropJson.put(String.valueOf(crop.cropId), base);
      }
      resultJson.put("crops"  , cropJson);
      resultJson.put("out"  , datalist);
      return ok(resultJson);
    }
    public static Result AccountDateDataOutput(String accountId, String cropId, String dateFrom, String dateTo) {
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

      //----- パラメータの調整 -----
      //指定された範囲日付の変換
      SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
      java.sql.Date dFrom  = null;
      java.sql.Date dTo    = null;
      try {
        dFrom = new java.sql.Date(dateParse.parse(dateFrom).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（自）に誤りがあります。 FROM = " + dateFrom);
        return ok(resultJson);
      }
      try {
        dTo = new java.sql.Date(dateParse.parse(dateTo).getTime());
      } catch (ParseException e) {
        resultJson.put("result", AgryeelConst.Result.ERROR);
        resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + dateTo);
        return ok(resultJson);
      }

      SimpleDateFormat sdfs = new SimpleDateFormat("MM/dd");
      ObjectNode datalist = Json.newObject();

      List<Crop> crops = new ArrayList<Crop>();
      String[] cropList = cropId.split(",");

      for (String crop : cropList) {
        Crop data = Crop.getCropInfo(Double.parseDouble(crop));
        crops.add(data);
      }

      Account account = Account.getAccount(accountId);

      long dateCount = DateU.GetDiffDate(dFrom, dTo);
      Calendar sys = Calendar.getInstance();
      sys.setTime(dFrom);
      for (int i=0; i <= dateCount; i++) {
        ObjectNode dayJson = Json.newObject();
        java.sql.Date sysdate = new java.sql.Date(sys.getTimeInMillis());
        java.sql.Timestamp sysTimestamp = new java.sql.Timestamp(sys.getTimeInMillis());
        List<TimeLine> tls = TimeLine.getTimeLineOfAccount(accountId, account.farmId, sysTimestamp, sysTimestamp);
        ObjectNode cropJson = Json.newObject();
        double totalC = 0;
        double totalT = 0;
        for (Crop crop : crops) {
          double totalc = 0;
          double totalt = 0;
          for (TimeLine tl : tls ) {
            WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
            if (wd != null) {
              Work wk = Work.getWork(wd.workId);
              if (wk != null) {
                if (wk.workTemplateId != AgryeelConst.WorkTemplate.SHUKAKU &&
                    wk.workTemplateId != AgryeelConst.WorkTemplate.SENKA) {
                  continue;
                }
                Compartment cp = Compartment.getCompartmentInfo(wd.kukakuId);
                List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", cp.kukakuId).le("shukaku_start_date", wd.workDate).ge("shukaku_end_date", wd.workDate).orderBy("work_year ASC, rotation_speed_of_year ASC").findList();
                for (MotochoBase mb : mbs) {
                  if (crop.cropId != mb.cropId) {
                    continue;
                  }
                  totalc += wd.shukakuRyo;
                  totalC += wd.shukakuRyo;
                  totalt += wd.workTime;
                  totalT += wd.workTime;
                  break;
                }
              }
              else {
                Logger.error("WORK NOT FOUND WORKID={}", wd.workId);
              }
            }
          }
          ObjectNode base = Json.newObject();
          base.put("id"         , crop.cropId);
          base.put("name"       , crop.cropName);
          base.put("color"      , crop.cropColor);
          base.put("shukakuRyo" , new BigDecimal(totalc).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
          base.put("time"       , new BigDecimal(totalt / 60).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
          cropJson.put(String.valueOf(crop.cropId), base);
        }
        dayJson.put("shukakuRyo", new BigDecimal(totalC).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        dayJson.put("time"      , new BigDecimal(totalT / 60).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        dayJson.put("date", sdfs.format(sysdate));
        dayJson.put("crop", cropJson);
        datalist.put(dateParse.format(sysdate), dayJson);
        sys.add(Calendar.DAY_OF_MONTH, 1);
      }
      resultJson.put("out"  , datalist);
      return ok(resultJson);
    }
    private static String getWeek(Calendar cal) {
      String result = "";
      switch (cal.get(Calendar.DAY_OF_WEEK)) {
      case Calendar.SUNDAY:
        result = "日";
        break;
      case Calendar.MONDAY:
        result = "月";
        break;
      case Calendar.TUESDAY:
        result = "火";
        break;
      case Calendar.WEDNESDAY:
        result = "水";
        break;
      case Calendar.THURSDAY:
        result = "木";
        break;
      case Calendar.FRIDAY:
        result = "金";
        break;
      case Calendar.SATURDAY:
        result = "土";
        break;
      }
      return result;
  }
  public static Result aicaDataBase() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    DecimalFormat df = new DecimalFormat("##0.0");
    String filename      = "AicaData" + sdf.format(Calendar.getInstance().getTime());
    response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

    StringBuffer sb    = new StringBuffer();
    sb.append("\"区画名\"");
    sb.append(",\"作付年\"");
    sb.append(",\"年内回転数\"");
    sb.append(",\"播種日\"");
    sb.append(",\"品目\"");
    sb.append(",\"品種\"");
    sb.append(",\"想定積算温度\"");
    sb.append("\r\n");

    List<Compartment> cts = Compartment.find.where().eq("kukaku_id", 1).findList();
    Calendar st = Calendar.getInstance();
    Calendar ed = Calendar.getInstance();
    for (Compartment ct: cts) {
      CompartmentStatus cs = CompartmentStatus.getStatusOfCompartment(ct.kukakuId).get(0);
      Crop crop = Crop.getCropInfo(cs.cropId);
      if (cs == null || crop == null) {
        continue;
      }
      if (cs.hashuDate == null) {
        continue;
      }
      AicaCompornent aica = modelingSekisan("KURUME", cs.hashuDate);

      st.setTime(cs.hashuDate);
      st.add(Calendar.DAY_OF_MONTH, -1 * aica.getSekisanCount());
      ed.setTime(cs.hashuDate);
      ed.add(Calendar.DAY_OF_MONTH, -1);

      List<Weather> weathers = Weather.getWeather("KURUME", new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
      double[] x = new double[aica.getSekisanCount()];
      int idx = 0;
      for (Weather weather : weathers) {
        double kion = weather.kionAve;
        x[idx] = kion;
        idx++;
      }

      double[] sekisans = aica.predictionSekisan(x, 90);
      double sekisan = 0;

      for (int i=0; i<sekisans.length; i++) {
        sekisan += sekisans[i];
      }

      sb.append("\"" + ct.kukakuName + "\"");
      sb.append("," + cs.workYear + "");
      sb.append("," + cs.rotationSpeedOfYear + "");
      sb.append(",\"" + sdf.format(cs.hashuDate) + "\"");
      sb.append(",\"" + crop.cropName + "\"");
      sb.append(",\"" + cs.hinsyuName + "\"");
      sb.append(",\"" + df.format(sekisan) + "\"");
      sb.append("\r\n");

    }
    try {
      return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return ok();
  }
  private static AicaCompornent modelingSekisan(String pPointCd, java.sql.Date pBaseDate) {

    AicaCompornent aica = new AicaCompornent();
    aica.modelingSekisan(pPointCd, pBaseDate);

    return aica;
  }
  public static Result aicaSaibaiManage(double kukakuId) {

    ObjectNode  resultJson = Json.newObject();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    DecimalFormat     df = new DecimalFormat("#,##0.0");
    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    boolean     api        = false;

    if (session(AgryeelConst.SessionKey.API) != null) {
    	api = true;
    }

    //---------------------------------------------------------
    // 必要な情報の取得
    //---------------------------------------------------------
    //区画情報
    Compartment cp = Compartment.getCompartmentInfo(kukakuId);
    if (cp == null) {
      return ok(resultJson);
    }
    CompartmentStatus cs = CompartmentStatus.find.where().eq("kukaku_id", kukakuId).findUnique();
    if (cs == null) {
      return ok(resultJson);
    }
    CompartmentWorkChainStatus cwcs = CompartmentWorkChainStatus.find.where().eq("kukaku_id", kukakuId).findUnique();
    if (cwcs == null) {
      return ok(resultJson);
    }
    //アカウント情報
    UserComprtnent accountComprtnent = new UserComprtnent();
    int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

    Account account = accountComprtnent.accountData;
    double farmId = account.farmId;
    FarmStatus fs = FarmStatus.getFarmStatus(farmId);

    List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
    List<Double> keys = new ArrayList<Double>();
    for (Compartment ct : cts) {
      keys.add(new Double(ct.kukakuId));
    }
    SaibaiPlanHinsyuList spl = new SaibaiPlanHinsyuList();
    //区画縛り
    List<MotochoBase> datas = get10YearsMotochoBase(cwcs.cropId, cs.katadukeDate, cs.kukakuId, keys);
    if (datas.size() == 0) {
      //生産者縛り
      datas = get10YearsMotochoBase(cwcs.cropId, cs.katadukeDate, 0, keys);
    }
    if (datas.size() == 0) {
      //AICA全体
      keys.clear();
      datas = get10YearsMotochoBase(cwcs.cropId, cs.katadukeDate, 0, keys);
    }
    if (datas.size() == 0) {
      return ok(resultJson);
    }

    //---------------------------------------------------------
    // 播種済か？否か？による予測モード判定
    //---------------------------------------------------------
    Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
    short mode = 0;
    if ((cs.hashuDate != null)
        && (cs.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合
      mode = 1;
    }
    resultJson.put("mode", mode);
    //---------------------------------------------------------
    // AICA栽培管理データの生成
    //---------------------------------------------------------
    Calendar calHashu = Calendar.getInstance();
    double cropId   = 0;
    String cropName = "";
    String hinsyuId   = "";
    String hinsyuName = "";
    Logger.info("----------------------------------------------------------------------");
    if (mode == 0) {
      //---------------------------------------------------------
      // 想定播種日の算出
      //---------------------------------------------------------
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
        Logger.info("[HINSYUID]{} [SHUKAKU]{}", hinsyuIds[0], data.totalShukakuCount);
        if (data.cropId == 0) {
          continue;
        }
        if (api) {
          cropId = data.cropId;
        }
      }
      Logger.info("----------------------------------------------------------------------");
      DecimalFormat fHinsyu = new DecimalFormat("##0");
      hinsyuId   = fHinsyu.format(spl.getMaxHinshu());
      hinsyuName = Hinsyu.getHinsyuName(Double.parseDouble(hinsyuId));
      resultJson.put("hinsyuId", hinsyuId);
      resultJson.put("hinsyuName", hinsyuName);
      if (api) {
        Crop crop = Crop.getCropInfo(cropId);
        if (crop != null) {
          cropName = crop.cropName;
        }
        resultJson.put("cropId", cropId);
        resultJson.put("cropName", cropName);
      }
      boolean init = true;
      long tDiff = 0;
      MotochoBase tbase = null;
      Calendar cal = Calendar.getInstance();
      Calendar target = Calendar.getInstance();
      cal.setTime(cs.katadukeDate);
      for(MotochoBase data: datas) {
        if ((data.hinsyuId == null) || (data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) == -1)) {
          Logger.info("[KUKAKU]{} [YEAR]{} [ROTATION]{} [HINSYUID]{}", data.kukakuId, data.workYear, data.rotationSpeedOfYear, data.hinsyuId);
          continue;
        }
        if (data.hashuDate == null || data.shukakuStartDate == null) {
          Logger.info("[KUKAKU]{} [YEAR]{} [ROTATION]{} HASHU SHUKAKU DATE ERROR", data.kukakuId, data.workYear, data.rotationSpeedOfYear);
          continue;
        }
        target.setTime(data.hashuDate);
        cal.set(Calendar.YEAR, target.get(Calendar.YEAR));
        if (init) { // 初回データ
          tbase = data;
          tDiff = Math.abs(DateU.GetDiffDate(cal.getTime(), target.getTime()));
          Logger.info("[KUKAKU]{} [YEAR]{} [ROTATION]{} [DIFF]{}", tbase.kukakuId, tbase.workYear, tbase.rotationSpeedOfYear, tDiff);
        }
        else {  // 播種日が直近のデータを探索する
          long diff = Math.abs(DateU.GetDiffDate(cal.getTime(), target.getTime()));
          if ( (diff < tDiff) || (diff == tDiff)) {
            tbase = data;
            tDiff = diff;
            Logger.info("[KUKAKU]{} [YEAR]{} [ROTATION]{} [DIFF]{}", tbase.kukakuId, tbase.workYear, tbase.rotationSpeedOfYear, tDiff);
          }
        }
        init = false;
      }
      if (tbase != null) {
        Logger.info("[KUKAKU]{} [YEAR]{} [ROTATION]{} [START]{} [END]{}", tbase.kukakuId, tbase.workYear, tbase.rotationSpeedOfYear, sdf.format(tbase.workStartDay), sdf.format(tbase.workEndDay));
        long diff = Math.abs(DateU.GetDiffDate(tbase.workStartDay, tbase.hashuDate));
        cal.setTime(cs.katadukeDate);
        cal.add(Calendar.DAY_OF_MONTH, (int)diff);
        resultJson.put("hasyuDate", sdf.format(cal.getTime()));
        calHashu.setTime(cal.getTime());
        //---------------------------------------------------------
        // 作付管理の作業を作成する
        //---------------------------------------------------------
        int  idx      = 0;
        List<ObjectNode> workLists = new ArrayList<ObjectNode>();
        ObjectNode workList = Json.newObject();
        ArrayNode workListApi = mapper.createArrayNode();
        java.sql.Date oWorkDate = null;
        List<models.WorkDiary> wds = models.WorkDiary.getWorkDiaryOfWork(tbase.kukakuId, tbase.workStartDay, tbase.workEndDay);
        double oWorkId = -1;
        Logger.info("[KUKAKU]{} [YEAR]{} [ROTATION]{} [START]{} [END]{} [COUNT]{}", tbase.kukakuId, tbase.workYear, tbase.rotationSpeedOfYear, sdf.format(tbase.workStartDay), sdf.format(tbase.workEndDay), wds.size());
        for (models.WorkDiary wd :wds) {
          Logger.info("[WORKID]{}", wd.workId);
          target.setTime(cal.getTime());
          diff = DateU.GetDiffDate(tbase.hashuDate, wd.workDate);
          target.add(Calendar.DAY_OF_MONTH, (int)diff);
          int workMode = 0;
          Compartment baseCt = Compartment.getCompartmentInfo(tbase.kukakuId);
          CompartmentWorkChainStatus baseCwcs = baseCt.getCompartmentWorkChainStatus();
          List<WorkChainItem> wcis = WorkChainItem.find.where().eq("work_chain_id", baseCwcs.workChainId).eq("work_id", wd.workId).findList();
          for (WorkChainItem wci : wcis) {
            workMode = wci.workMode;
            break;
          }
          if(workMode != 1) { /* 作付管理以外は不要 */
            continue;
          }
          Work wk = Work.getWork(wd.workId);
          if ((tbase.hashuDate.compareTo(wd.workDate) == -1)
                && (wk.workTemplateId == AgryeelConst.WorkTemplate.END)) { // 播種日以降の作付け開始は無視する
            continue;
          }
          if ((wk != null) && (oWorkId != wd.workId) && ((oWorkDate == null ) || (oWorkDate != null && oWorkDate.compareTo(wd.workDate) != 0))) {
            idx++;
            ObjectNode workJson = Json.newObject();
            workJson.put("idx", idx);
            workJson.put("workDiaryId", wd.workDiaryId);
            workJson.put("workId", wd.workId);
            workJson.put("name", wk.workName);
            workJson.put("color", wk.workColor);
            workJson.put("date", sdf.format(target.getTime()));
            workJson.put("time", wd.workTime);
            workJson.put("mode", workMode);
            workLists.add(workJson);
//          workList.put(String.valueOf(idx), workJson);
//          workListApi.add(workJson);
          }
          oWorkId   = wd.workId;
          oWorkDate = wd.workDate;
        }
        Collections.sort(workLists,
            new Comparator<ObjectNode>() {
              @Override
              public int compare(ObjectNode o1, ObjectNode o2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                try {
                  java.util.Date d1 = sdf.parse(o1.get("date").asText());
                  java.util.Date d2 = sdf.parse(o2.get("date").asText());
                  return d1.compareTo(d2);
                } catch (ParseException e) {
                  return 0;
                }
              }
            }
        );
        idx=0;
        for (ObjectNode node:workLists) {
          idx++;
          workList.put(String.valueOf(idx), node);
          workListApi.add(node);
        }
        if(api){
          resultJson.put("sakudukeList", workListApi);
        }else{
          resultJson.put("sakudukeList", workList);
        }
      }
    }
    else {
      //---------------------------------------------------------
      // 想定播種日の算出
      //---------------------------------------------------------
      hinsyuId   = cs.hinsyuId;
      hinsyuName = cs.hinsyuName;
      resultJson.put("hinsyuId", hinsyuId);
      resultJson.put("hinsyuName", hinsyuName);
      resultJson.put("hasyuDate", sdf.format(cs.hashuDate));
      calHashu.setTime(cs.hashuDate);
      if (api) {
        cropId = cs.cropId;
        Crop crop = Crop.getCropInfo(cropId);
        if (crop != null) {
          cropName = crop.cropName;
        }
        resultJson.put("cropId", cropId);
        resultJson.put("cropName", cropName);
      }
      //---------------------------------------------------------
      // 作付管理の作業を作成する
      //---------------------------------------------------------
      int  idx      = 0;
      ObjectNode workList = Json.newObject();
      List<ObjectNode> workLists = new ArrayList<ObjectNode>();
      ArrayNode workListApi = mapper.createArrayNode();
      java.sql.Date oWorkDate = null;
      List<models.WorkDiary> wds = models.WorkDiary.getWorkDiaryOfWork(cs.kukakuId, cs.katadukeDate, new java.sql.Timestamp(cs.hashuDate.getTime()));
      double oWorkId = -1;
      for (models.WorkDiary wd :wds) {
        int workMode = 0;
        List<WorkChainItem> wcis = WorkChainItem.find.where().eq("work_id", wd.workId).findList();
        for (WorkChainItem wci : wcis) {
          workMode = wci.workMode;
          break;
        }
        if(workMode != 1) { /* 作付管理以外は不要 */
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
          workJson.put("color", wk.workColor);
          workJson.put("date", sdf.format(wd.workDate));
          workJson.put("time", wd.workTime);
          workJson.put("mode", workMode);
          workLists.add(workJson);
//        workList.put(String.valueOf(idx), workJson);
//        workListApi.add(workJson);
        }
        oWorkId   = wd.workDiaryId;
        oWorkDate = wd.workDate;
      }
      Collections.sort(workLists,
          new Comparator<ObjectNode>() {
            @Override
            public int compare(ObjectNode o1, ObjectNode o2) {
              SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
              try {
                java.util.Date d1 = sdf.parse(o1.get("date").asText());
                java.util.Date d2 = sdf.parse(o2.get("date").asText());
                return d1.compareTo(d2);
              } catch (ParseException e) {
                return 0;
              }
            }
          }
      );
      idx=0;
      for (ObjectNode node:workLists) {
        idx++;
        workList.put(String.valueOf(idx), node);
        workListApi.add(node);
      }
      if(api){
        resultJson.put("sakudukeList", workListApi);
      }else{
        resultJson.put("sakudukeList", workList);
      }

    }
    //---------------------------------------------------------
    // 想定収穫日を算出する
    //---------------------------------------------------------
    double dShukakuRyo = 0;
    Calendar st = Calendar.getInstance();
    st.setTime(calHashu.getTime());
    st.add(Calendar.DAY_OF_MONTH, -6);
    Calendar ed = Calendar.getInstance();
    ed.setTime(calHashu.getTime());
    Calendar cal    = Calendar.getInstance();
    Calendar target = Calendar.getInstance();

    Field fd = cp.getFieldInfo();
    if (fd != null) {
      String pointId = PosttoPoint.getPointId(fd.postNo);
      if (pointId != null && !"".equals(pointId)) {
        //---------------------------------------------------------
        // 想定積算温度を算出する
        //---------------------------------------------------------
        boolean init        = true;
        long tDiff          = 0;
        MotochoBase tbase   = null;
        double sekisanKion  = 0;
        cal.setTime(cs.katadukeDate);
        for(MotochoBase data: datas) {
//        if ((data.hinsyuId == null) || (data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) == -1)) {
//        continue;
//      }
          if (data.hashuDate == null || data.shukakuStartDate == null) {
            continue;
          }
          target.setTime(data.hashuDate);
          cal.set(Calendar.YEAR, target.get(Calendar.YEAR));
          if (init) { // 初回データ
            tbase = data;
            tDiff = Math.abs(DateU.GetDiffDate(cal.getTime(), target.getTime()));
          }
          else {  // 播種日が直近のデータを探索する
            if ((tbase.hinsyuId == null) || (tbase.hinsyuId != null && tbase.hinsyuId.indexOf(hinsyuId) == -1)
                && ((data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) != -1))) {
              long diff = Math.abs(DateU.GetDiffDate(cal.getTime(), target.getTime()));
              tbase = data;
              tDiff = diff;
            }
            else {
              if ((tbase.hinsyuId != null && tbase.hinsyuId.indexOf(hinsyuId) != -1)
                  && ((data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) != -1))) {
                long diff = Math.abs(DateU.GetDiffDate(cal.getTime(), target.getTime()));
                if ( (diff < tDiff) || (diff == tDiff)) {
                  tbase = data;
                  tDiff = diff;
                }
              }
            }
          }
          init = false;
        }
        if (tbase != null) {
          st.setTime(tbase.hashuDate);
          ed.setTime(tbase.shukakuStartDate);
          long diff = Math.abs(DateU.GetDiffDate(st.getTime(), ed.getTime()));
          if (diff < 10) {
            ed.setTime(st.getTime());
            ed.add(Calendar.DAY_OF_WEEK, 30);
          }
          List<Weather> weathers = Weather.getWeather( pointId, new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
          for (Weather weather : weathers) {
            double kion = weather.kionAve;
            sekisanKion += kion;
          }
          dShukakuRyo = tbase.totalShukakuCount / sekisanKion;
        }
        else {
          st.setTime(calHashu.getTime());
          st.add(Calendar.YEAR, -1);
          ed.setTime(calHashu.getTime());
          ed.add(Calendar.YEAR, -1);
          ed.add(Calendar.DAY_OF_WEEK, 40);
          List<Weather> weathers = Weather.getWeather( pointId, new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
          for (Weather weather : weathers) {
            double kion = weather.kionAve;
            sekisanKion += kion;
          }
          dShukakuRyo = 0;
        }

        Calendar calSystem = Calendar.getInstance();

        AicaCompornent aica = modelingSekisan(pointId, new java.sql.Date(calSystem.getTime().getTime()));

//既に予測している気温で積算を行う
//        st.setTime(calHashu.getTime());
//        st.add(Calendar.DAY_OF_MONTH, -6);
//        ed.setTime(calHashu.getTime());
//        List<Weather> weathers = Weather.getWeather( pointId, new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
//        double[] x = new double[aica.getSekisanCount()];
//        int idx = 0;
//        for (Weather weather : weathers) {
//          double kion = weather.kionAve;
//          x[idx] = kion;
//          idx++;
//        }
//
//        double[] sekisans = aica.predictionSekisan(x, 180);
        st.setTime(calHashu.getTime());
        ed.setTime(calHashu.getTime());
        ed.add(Calendar.MONTH, 4);
        List<Weather> weathers = Weather.getWeather( pointId, new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
        int idx = 0;
        double[] sekisans = new double[weathers.size()];
        for (Weather weather : weathers) {
          double kion = weather.kionAve;
          sekisans[idx] = kion;
          idx++;
        }

        double sekisan    = 0;
        int dayCount      = 0;

        for (int i=0; i<sekisans.length; i++) {
          Logger.info("[AICA MANAGE] KION={}", sekisans[i]);
          sekisan += sekisans[i];
          dayCount++;
          if(sekisanKion <= sekisan) { //想定積算温度が超えた場合
            Logger.info("[BASE SEKISAN]{} [SOUTEI SEKISAN]{} [SEIIKU]{}", sekisanKion, sekisan, dayCount);
            cal.setTime(calHashu.getTime());
            cal.add(Calendar.DAY_OF_MONTH, dayCount);
            dShukakuRyo = dShukakuRyo * sekisan;
            resultJson.put("souteiShukakuDay", sdf.format(cal.getTime()));
            resultJson.put("souteiShukakuRyo", df.format(dShukakuRyo));
            resultJson.put("seiiku", dayCount);

            //---------------------------------------------------------
            //自生産者の該当品種収穫力を算出する
            //---------------------------------------------------------
            List<Double> kkey = new ArrayList<Double>();
            for (Compartment ct : cts) {
              CompartmentWorkChainStatus cws = ct.getCompartmentWorkChainStatus();
              if (cws == null || (cws != null && cwcs.cropId != cws.cropId)) {
                continue;
              }
              kkey.add(new Double(ct.kukakuId));
            }
            List<Work> works = Work.find.where().disjunction().add(Expr.eq("farm_id", 0)).add(Expr.eq("farm_id", farmId)).endJunction().eq("work_template_id", AgryeelConst.WorkTemplate.SHUKAKU).findList();
            List<Double> wkey = new ArrayList<Double>();
            for (Work work : works) {
              wkey.add(new Double(work.workId));
            }
            Calendar sst = Calendar.getInstance();
            sst.add(Calendar.MONTH, -2);
            Calendar sed = Calendar.getInstance();
            List<WorkDiary> workDiarys = WorkDiary.find.where().in("kukaku_id", kkey).in("work_id", wkey).between("work_date", new java.sql.Date(sst.getTimeInMillis()), new java.sql.Date(sed.getTimeInMillis())).findList();
            double aveShukakuryo   = 0;
            long   needWorkTime    = 0;
            double totalShukakuryo = 0;
            long   totalWorkTime   = 0;
            Work   wShukaku        = null;
            for(WorkDiary workDiary: workDiarys) {
              if (workDiary.shukakuRyo  == 0
               || workDiary.workTime    == 0) {
                continue;
              }
              if (wShukaku == null) {
                wShukaku = Work.getWork(workDiary.workId);
              }
              totalShukakuryo += workDiary.shukakuRyo;
              totalWorkTime   += workDiary.workTime;
            }
            if (totalWorkTime != 0) {
              aveShukakuryo = totalShukakuryo / totalWorkTime;
            }
            if (aveShukakuryo != 0) {
              needWorkTime = (long)(dShukakuRyo / aveShukakuryo);
            }
            if (wShukaku == null) {
              List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList(cwcs.workChainId);
              for (WorkChainItem wci :wcis) {
                Work w = Work.getWork(wci.workId);
                if (w.workTemplateId == AgryeelConst.WorkTemplate.SHUKAKU ||
                    w.workTemplateId == AgryeelConst.WorkTemplate.SENKA) {
                  wShukaku = w;
                  break;
                }
              }
            }
            //---------------------------------------------------------
            //想定収穫作業日数を算出する
            //---------------------------------------------------------
            Logger.info("[TOTAL SHUKAKURYO]{} [TOTAL WORKTIME]{} [AVERAGE SHUKAKURYO]{} [NEED TIME]{} [ONEDAY TIME]{}", totalShukakuryo, totalWorkTime, aveShukakuryo, needWorkTime, fs.workTimeOfDay);
            long tantoNinzu   = (int)Math.ceil((double)needWorkTime / fs.workTimeOfDay);
            int  shukakuNisu  = (int)Math.ceil(tantoNinzu / fs.syukakuTantoNinzu);
            Logger.info("[Ninzu]{} [Nissu]{}", tantoNinzu, shukakuNisu);
            resultJson.put("souteiTantoNinzu" , tantoNinzu);
            resultJson.put("souteiShukakuNisu", shukakuNisu);
            ObjectNode workList = Json.newObject();
            ArrayNode workListApi = mapper.createArrayNode();
            for (int ii=0; ii < shukakuNisu; ii++) {
              ObjectNode workJson = Json.newObject();
              workJson.put("idx", (ii+1));
              workJson.put("workDiaryId", 0);
              workJson.put("workId", wShukaku.workId);
              workJson.put("name", wShukaku.workName);
              workJson.put("color", wShukaku.workColor);
              workJson.put("date", sdf.format(cal.getTime()));
              workJson.put("workTime", fs.workTimeOfDay);
              if (tantoNinzu < fs.syukakuTantoNinzu) {
                workJson.put("ninzu", tantoNinzu);
              }
              else {
                workJson.put("ninzu", fs.syukakuTantoNinzu);
                tantoNinzu -= fs.syukakuTantoNinzu;
              }
              workList.put(String.valueOf(ii+1), workJson);
              workListApi.add(workJson);
              cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            if (shukakuNisu == 0) {
              ObjectNode workJson = Json.newObject();
              workJson.put("idx", 1);
              workJson.put("workDiaryId", 0);
              workJson.put("workId", wShukaku.workId);
              workJson.put("name", wShukaku.workName);
              workJson.put("color", wShukaku.workColor);
              workJson.put("date", sdf.format(cal.getTime()));
              workJson.put("workTime", fs.workTimeOfDay);
              workJson.put("ninzu", tantoNinzu);
              workList.put(String.valueOf(1), workJson);
              workListApi.add(workJson);
            }
            if(api){
              resultJson.put("shukakuList", workListApi);
            }else{
              resultJson.put("shukakuList", workList);
            }
            //---------------------------------------------------------
            //想定栽培管理を行う
            //---------------------------------------------------------
            int iSaibaiIdx = 0;
            List<ObjectNode> workLists = new ArrayList<ObjectNode>();
            workList = Json.newObject();
            workListApi = mapper.createArrayNode();
            //同一時期範囲を生成する
            Calendar dst = Calendar.getInstance();
            dst.setTime(cal.getTime());
            dst.add(Calendar.WEEK_OF_YEAR, -2);
            Calendar ded = Calendar.getInstance();
            ded.setTime(cal.getTime());
            ded.add(Calendar.WEEK_OF_YEAR, 2);
            //同一時期内に消毒を実施しているかチェックする
            works = Work.find.where().disjunction().add(Expr.eq("farm_id", 0)).add(Expr.eq("farm_id", farmId)).endJunction().eq("work_template_id", AgryeelConst.WorkTemplate.SANPU).findList();
            wkey = new ArrayList<Double>();
            for (Work work : works) {
              wkey.add(new Double(work.workId));
            }
            WorkDiary             shoudokuDiary   = null;
            List<WorkDiarySanpu>  shoudokuSanpus  = null;
            for (int prev = 0; prev < 10; prev++) {
              dst.add(Calendar.YEAR , -1 * prev);
              ded.add(Calendar.YEAR , -1 * prev);
              workDiarys = WorkDiary.find.where().in("kukaku_id", kkey).in("work_id", wkey).between("work_date", new java.sql.Date(dst.getTimeInMillis()), new java.sql.Date(ded.getTimeInMillis())).orderBy("work_date").findList();
              for(WorkDiary workDiary: workDiarys) {
                List<WorkDiarySanpu> wdss = WorkDiarySanpu.getWorkDiarySanpuList(workDiary.workDiaryId);
                for (WorkDiarySanpu wds : wdss) {
                  Nouhi nouhi = Nouhi.getNouhiInfo(wds.nouhiId);
                  if (nouhi != null && nouhi.nouhiKind == AgryeelConst.NouhiKind.NOUYAKU) {
                    List<MotochoBase> shoudokuBases = MotochoBase.find.where().eq("kukaku_id", workDiary.kukakuId).le("hashu_date", workDiary.workDate).ge("work_end_day", workDiary.workDate).findList();
                    MotochoBase shoudokuBase = null;
                    if (shoudokuBases.size() > 0) {
                      shoudokuBase = shoudokuBases.get(0);
                    }
                    if (shoudokuBase != null && shoudokuBase.shukakuStartDate != null) {
                      shoudokuSanpus = wdss;
                      break;
                    }
                    else {
                      continue;
                    }
                  }
                }
                if (shoudokuSanpus != null) {
                  shoudokuDiary = workDiary;
                  break;
                }
              }
              if (shoudokuSanpus != null) {
                break;
              }
            }
            //消毒を収穫の何日前に実施しているか算出する
            if (shoudokuDiary != null) {
              List<MotochoBase> shoudokuBases = MotochoBase.find.where().eq("kukaku_id", shoudokuDiary.kukakuId).le("work_start_day", shoudokuDiary.workDate).ge("work_end_day", shoudokuDiary.workDate).findList();
              MotochoBase shoudokuBase = null;
              if (shoudokuBases.size() > 0) {
                shoudokuBase = shoudokuBases.get(0);
              }
              if (shoudokuBase != null && shoudokuBase.shukakuStartDate != null) {
                long diff = Math.abs(DateU.GetDiffDate(shoudokuDiary.workDate, shoudokuBase.shukakuStartDate));
                cal.setTime(calHashu.getTime());
                cal.add(Calendar.DAY_OF_MONTH, dayCount);
                Calendar shodokuDate = Calendar.getInstance();
                shodokuDate.setTime(cal.getTime());
                shodokuDate.add(Calendar.DAY_OF_MONTH, (int)(-1 * diff));
                ObjectNode workJson = Json.newObject();
                Work   wShodoku        = Work.getWork(shoudokuDiary.workId);
                iSaibaiIdx++;
                workJson.put("idx", iSaibaiIdx);
                workJson.put("workDiaryId", 0);
                workJson.put("workId", wShodoku.workId);
                workJson.put("color", wShodoku.workColor);
                workJson.put("name", wShodoku.workName);
                workJson.put("date", sdf.format(shodokuDate.getTime()));
                if(api){
                  workJson.put("workTime", (long)0);
                }
                ObjectNode sanpuList = Json.newObject();
                ArrayNode sanpuListApi = mapper.createArrayNode();
                int sanpuIdx = 0;
                for (WorkDiarySanpu shoudokuSanpu : shoudokuSanpus){
                  ObjectNode sanpuJson = Json.newObject();
                  sanpuIdx++;
                  Nouhi nouhi = Nouhi.getNouhiInfo(shoudokuSanpu.nouhiId);
                  double hosei = 1;
                  if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
                    hosei = 0.001;
                  }
                  sanpuJson.put("idx"        , sanpuIdx);
                  sanpuJson.put("nouhiId"    , nouhi.nouhiId);
                  sanpuJson.put("nouhiName"  , nouhi.nouhiName);
                  sanpuJson.put("bairitu"    , shoudokuSanpu.bairitu);
                  sanpuJson.put("sanpuryo"   , shoudokuSanpu.sanpuryo * hosei);
                  sanpuJson.put("unit"       , Common.GetCommonValue(Common.ConstClass.UNIT, nouhi.unitKind));
                  sanpuList.put(String.valueOf(sanpuIdx), sanpuJson);
                  sanpuListApi.add(sanpuJson);
                }

                if(api){
                  workJson.put("sanpu", sanpuListApi);
                  //workListApi.add(workJson);
                }else{
                  workJson.put("sanpu", sanpuList);
                  //workList.put(String.valueOf(iSaibaiIdx), workJson);
                }
                workLists.add(workJson);
              }
            }
            //同一時期内に潅水を実施しているか？
            Work workKansui = null;
            List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList(cwcs.workChainId);
            for (WorkChainItem wci :wcis) {
              Work w = Work.getWork(wci.workId);
              if (w.workTemplateId == AgryeelConst.WorkTemplate.KANSUI) {
                workKansui = w;
                break;
              }
            }
            if (workKansui != null) {
              keys.clear();
              for (Compartment ct : cts) {
                keys.add(new Double(ct.kukakuId));
              }
              //----- 品種縛り -----
              //区画縛り
              List<MotochoBase> baseKansuis = get10YearsMotochoBaseKansui(hinsyuId,cwcs.cropId, new java.sql.Date(calHashu.getTime().getTime()), cs.kukakuId, keys);
              if (baseKansuis.size() == 0) {
                //生産者縛り
                baseKansuis = get10YearsMotochoBaseKansui(hinsyuId, cwcs.cropId, new java.sql.Date(calHashu.getTime().getTime()), 0, keys);
              }
              if (baseKansuis.size() == 0) {
                //AICA全体
                keys.clear();
                baseKansuis = get10YearsMotochoBaseKansui(hinsyuId, cwcs.cropId, new java.sql.Date(calHashu.getTime().getTime()), 0, keys);
              }
              //----- 品種縛りなし -----
              keys.clear();
              for (Compartment ct : cts) {
                keys.add(new Double(ct.kukakuId));
              }
              if (baseKansuis.size() == 0) {
                //区画縛り
                baseKansuis = get10YearsMotochoBaseKansui("0", cwcs.cropId, new java.sql.Date(calHashu.getTime().getTime()), 0, keys);
              }
              if (baseKansuis.size() == 0) {
                //生産者縛り
                baseKansuis = get10YearsMotochoBaseKansui("0", cwcs.cropId, new java.sql.Date(calHashu.getTime().getTime()), 0, keys);
              }
              if (baseKansuis.size() == 0) {
                //AICA全体
                keys.clear();
                baseKansuis = get10YearsMotochoBaseKansui("0", cwcs.cropId, new java.sql.Date(calHashu.getTime().getTime()), 0, keys);
              }
              double totalSekisan = 0;
              double diffSekisan = 0;
              MotochoBase baseKansui = null;
              List<Weather> baseWeathers = new ArrayList<Weather>();
              for (MotochoBase bK: baseKansuis) {
                Compartment kcp = Compartment.getCompartmentInfo(bK.kukakuId);
                Field kfd = kcp.getFieldInfo();
                if (kfd != null) {
                  String kPointId = PosttoPoint.getPointId(kfd.postNo);
                  weathers = Weather.getWeather( kPointId, bK.hashuDate, bK.shukakuStartDate);
                  double kSekisan = 0;
                  for (Weather weather : weathers) {
                    kSekisan += weather.kionAve;
                  }
                  if (diffSekisan == 0) {
                    diffSekisan = Math.abs(kSekisan - sekisan);
                    baseKansui  = bK;
                    totalSekisan=kSekisan;
                    baseWeathers = weathers;
                  }
                  else {
                    if (Math.abs(kSekisan - sekisan) < diffSekisan) {
                      diffSekisan = Math.abs(kSekisan - sekisan);
                      baseKansui  = bK;
                      totalSekisan=kSekisan;
                      baseWeathers = weathers;
                    }
                  }
                }
              }
              if (baseKansui != null) {
                //対象期間の総潅水分数を求める
                long kansuiTime = 0;
                workDiarys = WorkDiary.find.where().in("kukaku_id", baseKansui.kukakuId).between("work_date", baseKansui.hashuDate, baseKansui.shukakuStartDate).orderBy("work_date").findList();
                Analysis als = new Analysis();
                Logger.info("---------- KANSUI INFO ----------");
                for(WorkDiary workDiary: workDiarys) {
                  Work work = Work.getWork(workDiary.workId);
                  if (work.workTemplateId != AgryeelConst.WorkTemplate.KANSUI) {
                    continue;
                  }
                  int diff = (int)Math.abs(DateU.GetDiffDate(baseKansui.hashuDate, workDiary.workDate));
                  double bsekisan = 0;
                  for (int iw=0; iw <= diff; iw++) {
                    Weather w = baseWeathers.get(iw);
                    bsekisan += w.kionAve;
                  }
                  als.add(bsekisan);
                  als.put(bsekisan, workDiary.workTime);
                  kansuiTime += workDiary.workTime;
                  Logger.info("[DATE]{} [NISU]{} [SEKISAN]{} [TIME]{}", sdf.format(workDiary.workDate), diff, bsekisan, workDiary.workTime);
                }
                double timePerSekisan = kansuiTime / totalSekisan;
                double dSekisan = 0;
                double dPrevSekisan = 0;
                int kidx = 0;
                List<Double> kansuiKeys = als.getKeys();
                for (double kansuiKey : kansuiKeys) {
                  double time = als.data(kansuiKey);
                  for (int ik=0; ik<sekisans.length; ik++) {
                    dSekisan += sekisans[ik];
                    if (kansuiKey <= dSekisan) {
                      ObjectNode workJson = Json.newObject();
                      iSaibaiIdx++;
                      workJson.put("idx", iSaibaiIdx);
                      workJson.put("workDiaryId", 0);
                      workJson.put("workId", workKansui.workId);
                      workJson.put("name", workKansui.workName);
                      workJson.put("color", workKansui.workColor);
                      cal.setTime(calHashu.getTime());
                      cal.add(Calendar.DAY_OF_MONTH, ik);
                      workJson.put("date", sdf.format(cal.getTime()));
                      if (dPrevSekisan == 0) {
                        workJson.put("workTime", (long)time);
                      }
                      else {
                        long hoseiTime = (long)(timePerSekisan * (dSekisan - dPrevSekisan));
                        workJson.put("workTime", (long)time + hoseiTime);
                      }
                      if(api){
                        ArrayNode sanpuListApi = mapper.createArrayNode();
                        workJson.put("sanpu", sanpuListApi);
                      }
//                    workList.put(String.valueOf(iSaibaiIdx), workJson);
//                    workListApi.add(workJson);
                      workLists.add(workJson);
                      dPrevSekisan = dSekisan;
                      dSekisan = 0;
                      break;
                    }
                  }
                }
              }
            }
            Collections.sort(workLists,
                new Comparator<ObjectNode>() {
                  @Override
                  public int compare(ObjectNode o1, ObjectNode o2) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                    try {
                      java.util.Date d1 = sdf.parse(o1.get("date").asText());
                      java.util.Date d2 = sdf.parse(o2.get("date").asText());
                      return d1.compareTo(d2);
                    } catch (ParseException e) {
                      return 0;
                    }
                  }
                }
            );
            idx=0;
            for (ObjectNode node:workLists) {
              idx++;
              workList.put(String.valueOf(idx), node);
              workListApi.add(node);
            }
            if(api){
              resultJson.put("saibaiList", workListApi);
            }else{
              resultJson.put("saibaiList", workList);
            }
            break;
          }
        }
      }
    }

    return ok(resultJson);
  }
  public static Result aicaSaibaiVerification() {

    ObjectNode  resultJson = Json.newObject();
    ObjectNode  listJson = Json.newObject();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    DecimalFormat     df = new DecimalFormat("#,##0.0");
    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    boolean     api        = false;
    StringBuffer sb = new StringBuffer();

    if (session(AgryeelConst.SessionKey.API) != null) {
      api = true;
    }

    sb.append("区画ID");
    sb.append(",").append("区画名");
    sb.append(",").append("作業年");
    sb.append(",").append("回転数");
    sb.append(",").append("品種ID");
    sb.append(",").append("品種");
    sb.append(",").append("播種日");
    sb.append(",").append("想定収穫日");
    sb.append(",").append("実績収穫日");
    sb.append(",").append("想定生育日数");
    sb.append(",").append("実績生育日数");
    sb.append(",").append("差");
    sb.append("\r\n");
    Logger.info(">>>>> START");
    for (int farmId = 3; farmId <= 3; farmId++) {
      List<Compartment> cps = Compartment.getCompartmentOfFarm(farmId);
      for (Compartment cp : cps) {
        Logger.info("[ID]{} START. ", cp.kukakuId);
        //---------------------------------------------------------
        // 必要な情報の取得
        //---------------------------------------------------------
        //区画情報
        CompartmentStatus cs = CompartmentStatus.find.where().eq("kukaku_id", cp.kukakuId).findUnique();
        if (cs == null) {
          continue;
        }
        CompartmentWorkChainStatus cwcs = CompartmentWorkChainStatus.find.where().eq("kukaku_id", cp.kukakuId).findUnique();
        if (cwcs == null) {
          continue;
        }
        //アカウント情報
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        Account account = accountComprtnent.accountData;
        FarmStatus fs = FarmStatus.getFarmStatus(farmId);

        List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
        List<Double> keys = new ArrayList<Double>();
        for (Compartment ct : cts) {
          keys.add(new Double(ct.kukakuId));
        }
        SaibaiPlanHinsyuList spl = new SaibaiPlanHinsyuList();
        //区画縛り
        List<MotochoBase> datas = get10YearsMotochoBase(cwcs.cropId, cs.katadukeDate, cs.kukakuId, keys);
        if (datas.size() == 0) {
          //生産者縛り
          datas = get10YearsMotochoBase(cwcs.cropId, cs.katadukeDate, 0, keys);
        }
        if (datas.size() == 0) {
          //AICA全体
          keys.clear();
          datas = get10YearsMotochoBase(cwcs.cropId, cs.katadukeDate, 0, keys);
        }
        if (datas.size() == 0) {
          continue;
        }
        Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
        List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", cp.kukakuId).findList();
        for (MotochoBase mb : mbs) {
          //---------------------------------------------------------
          // 播種済か？否か？による予測モード判定
          //---------------------------------------------------------
          if ((mb.hashuDate != null)
              && (mb.hashuDate.compareTo(nullDate) != 0)
              && (mb.shukakuStartDate != null)
              && (mb.shukakuStartDate.compareTo(nullDate) != 0)) { //播種日と収穫開始日が正しく登録されている場合
          }
          else {
            continue;
          }
          sb.append(cp.kukakuId);
          sb.append(",").append(cp.kukakuName);
          //---------------------------------------------------------
          // AICA栽培管理データの生成
          //---------------------------------------------------------
          ObjectNode  dataJson = Json.newObject();
          Calendar calHashu = Calendar.getInstance();
          String hinsyuId   = "";
          String hinsyuName = "";
          Logger.info("----------------------------------------------------------------------");
          //---------------------------------------------------------
          // 播種日の確定
          //---------------------------------------------------------
          hinsyuId   = mb.hinsyuId;
          hinsyuName = mb.hinsyuName;
          sb.append(",").append(mb.workYear);
          sb.append(",").append(mb.rotationSpeedOfYear);
          sb.append(",").append(hinsyuId);
          sb.append(",").append(hinsyuName);
          sb.append(",").append(sdf.format(mb.hashuDate));
          calHashu.setTime(mb.hashuDate);
          //---------------------------------------------------------
          // 想定収穫日を算出する
          //---------------------------------------------------------
          double dShukakuRyo = 0;
          Calendar st = Calendar.getInstance();
          st.setTime(calHashu.getTime());
          st.add(Calendar.DAY_OF_MONTH, -6);
          Calendar ed = Calendar.getInstance();
          ed.setTime(calHashu.getTime());
          Calendar cal    = Calendar.getInstance();
          Calendar target = Calendar.getInstance();
          Field fd = cp.getFieldInfo();
          if (fd != null) {
            String pointId = PosttoPoint.getPointId(fd.postNo);
            if (pointId != null && !"".equals(pointId)) {
              //---------------------------------------------------------
              // 想定積算温度を算出する
              //---------------------------------------------------------
              boolean init        = true;
              long tDiff          = 0;
              MotochoBase tbase   = null;
              double sekisanKion  = 0;
              cal.setTime(cs.katadukeDate);
              for(MotochoBase data: datas) {
//                if ((data.hinsyuId == null) || (data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) == -1)) {
//                  continue;
//                }
                if (data.hashuDate == null || data.shukakuStartDate == null) {
                  continue;
                }
                target.setTime(data.hashuDate);
                cal.set(Calendar.YEAR, target.get(Calendar.YEAR));
                if (init) { // 初回データ
                  tbase = data;
                  tDiff = Math.abs(DateU.GetDiffDate(cal.getTime(), target.getTime()));
                }
                else {  // 播種日が直近のデータを探索する
                  if ((tbase.hinsyuId == null) || (tbase.hinsyuId != null && tbase.hinsyuId.indexOf(hinsyuId) == -1)
                      && ((data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) != -1))) {
                    long diff = Math.abs(DateU.GetDiffDate(cal.getTime(), target.getTime()));
                    tbase = data;
                    tDiff = diff;
                  }
                  else {
                    if ((tbase.hinsyuId != null && tbase.hinsyuId.indexOf(hinsyuId) != -1)
                        && ((data.hinsyuId != null && data.hinsyuId.indexOf(hinsyuId) != -1))) {
                      long diff = Math.abs(DateU.GetDiffDate(cal.getTime(), target.getTime()));
                      if ( (diff < tDiff) || (diff == tDiff)) {
                        tbase = data;
                        tDiff = diff;
                      }
                    }
                  }
                }
                init = false;
              }
              if (tbase != null) {
                st.setTime(tbase.hashuDate);
                ed.setTime(tbase.shukakuStartDate);
                long diff = Math.abs(DateU.GetDiffDate(st.getTime(), ed.getTime()));
                if (diff < 10) {
                  ed.setTime(st.getTime());
                  ed.add(Calendar.DAY_OF_WEEK, 30);
                }
                List<Weather> weathers = Weather.getWeather( pointId, new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
                for (Weather weather : weathers) {
                  double kion = weather.kionAve;
                  sekisanKion += kion;
                }
                dShukakuRyo = tbase.totalShukakuCount / sekisanKion;
              }
              else {
                st.setTime(calHashu.getTime());
                st.add(Calendar.YEAR, -1);
                ed.setTime(calHashu.getTime());
                ed.add(Calendar.YEAR, -1);
                ed.add(Calendar.DAY_OF_WEEK, 40);
                List<Weather> weathers = Weather.getWeather( pointId, new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
                for (Weather weather : weathers) {
                  double kion = weather.kionAve;
                  sekisanKion += kion;
                }
                dShukakuRyo = 0;
              }


//既に予測している気温で積算を算出する
//              AicaCompornent aica = modelingSekisan(pointId, new java.sql.Date(calHashu.getTime().getTime()));
//              st.setTime(calHashu.getTime());
//              st.add(Calendar.DAY_OF_MONTH, -6);
//              ed.setTime(calHashu.getTime());
//              List<Weather> weathers = Weather.getWeather( pointId, new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
//              double[] x = new double[aica.getSekisanCount()];
//              int idx = 0;
//              for (Weather weather : weathers) {
//                double kion = weather.kionAve;
//                x[idx] = kion;
//                idx++;
//              }
//              double[] sekisans = aica.predictionSekisan(x, 90);

              st.setTime(calHashu.getTime());
              ed.setTime(calHashu.getTime());
              ed.add(Calendar.MONTH, 4);
              List<Weather> weathers = Weather.getWeather( pointId, new java.sql.Date(st.getTime().getTime()), new java.sql.Date(ed.getTime().getTime()));
              int idx = 0;
              double[] sekisans = new double[weathers.size()];
              for (Weather weather : weathers) {
                double kion = weather.kionAve;
                sekisans[idx] = kion;
                idx++;
              }

              double sekisan    = 0;
              int dayCount      = 0;

              for (int i=0; i<sekisans.length; i++) {
                sekisan += sekisans[i];
                dayCount++;
                if(sekisanKion <= sekisan) { //想定積算温度が超えた場合
                  Logger.info("[ID]{} [YEAR]{} [ROTATION]{} [BASE SEKISAN]{} [SOUTEI SEKISAN]{} [SEIIKU]{}", cp.kukakuId, mb.workYear, mb.rotationSpeedOfYear, sekisanKion, sekisan, dayCount);
                  cal.setTime(calHashu.getTime());
                  cal.add(Calendar.DAY_OF_MONTH, dayCount);
                  dShukakuRyo = dShukakuRyo * sekisan;
                  sb.append(",").append(sdf.format(cal.getTime()));
                  sb.append(",").append(sdf.format(mb.shukakuStartDate));
                  sb.append(",").append(dayCount);
                  sb.append(",").append(mb.seiikuDayCount);
                  sb.append(",").append(dayCount - mb.seiikuDayCount);
                  break;
                }
              }
            }
          }
          sb.append("\r\n");
        }
      }
    }

    Logger.info(">>>>> END");
    try {
      return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
    } catch (UnsupportedEncodingException e) {
      Logger.error("DOWNLOAD ERROR", e);
      e.printStackTrace();
    }
    return ok();
  }
  private static List<MotochoBase> get10YearsMotochoBase(double pCropId, java.sql.Timestamp pWorkStartDate, double pKukakuId,List<Double> pKeys) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    Calendar cal = Calendar.getInstance();
    cal.setTime(pWorkStartDate);
    Calendar start = Calendar.getInstance();
    start.setTime(cal.getTime());
    start.add(Calendar.MONTH, -1);
    Calendar end   = Calendar.getInstance();
    end.setTime(cal.getTime());
    end.add(Calendar.MONTH, 1);
    Logger.info("[AICA MANAGE] BASE SEATCH START={} END ={}", sdf.format(start.getTime()), sdf.format(end.getTime()));
    List<MotochoBase> results = new ArrayList<MotochoBase>();
    for (int prev = 0; prev < 10; prev++) {
      start.add(Calendar.YEAR , -1);
      end.add(Calendar.YEAR   , -1);
      cal.add(Calendar.YEAR   , -1);
      List<MotochoBase> datas;
      if (pKukakuId != 0) {
        datas = MotochoBase.find.where().eq("crop_id", pCropId).eq("kukaku_id", pKukakuId).between("hashu_date", start.getTime(), end.getTime()).findList();
      }
      else if (pKeys.size() > 0) {
        datas = MotochoBase.find.where().eq("crop_id", pCropId).in("kukaku_id", pKeys).between("hashu_date", start.getTime(), end.getTime()).findList();
      }
      else {
        datas = MotochoBase.find.where().eq("crop_id", pCropId).between("hashu_date", start.getTime(), end.getTime()).findList();
      }
      Logger.info("[AICA MANAGE] PREV SEATCH START={} END ={} COUNT={}", sdf.format(start.getTime()), sdf.format(end.getTime()), datas.size());
      for (MotochoBase data : datas) {
        Logger.info("[AICA MANAGE] MOTOCHO HASHU={}", sdf.format(data.hashuDate));
        results.add(data);
      }
    }
    return results;
  }
  private static List<MotochoBase> get10YearsMotochoBaseKansui(String pHinsyuId, double pCropId, java.sql.Date pWorkStartDate, double pKukakuId,List<Double> pKeys) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(pWorkStartDate);
    Calendar start = Calendar.getInstance();
    start.setTime(cal.getTime());
    start.add(Calendar.MONTH, -1);
    Calendar end   = Calendar.getInstance();
    end.setTime(cal.getTime());
    end.add(Calendar.MONTH, 1);
    List<MotochoBase> results = new ArrayList<MotochoBase>();
    for (int prev = 1; prev <= 10; prev++) {
      start.add(Calendar.YEAR , -1);
      end.add(Calendar.YEAR   , -1);
      cal.add(Calendar.YEAR   , -1);
      List<MotochoBase> datas;
      if (Double.parseDouble(pHinsyuId) != 0) {
        if (pKukakuId != 0) {
          datas = MotochoBase.find.where().gt("total_kansui_count", 0).like("hinsyu_id", pHinsyuId).eq("kukaku_id", pKukakuId).between("hashu_date", start.getTime(), end.getTime()).findList();
        }
        else if (pKeys.size() > 0) {
          datas = MotochoBase.find.where().gt("total_kansui_count", 0).like("hinsyu_id", pHinsyuId).in("kukaku_id", pKeys).between("hashu_date", start.getTime(), end.getTime()).findList();
        }
        else {
          datas = MotochoBase.find.where().gt("total_kansui_count", 0).like("hinsyu_id", pHinsyuId).between("hashu_date", start.getTime(), end.getTime()).findList();
        }
      }
      else {
        if (pKukakuId != 0) {
          datas = MotochoBase.find.where().gt("total_kansui_count", 0).eq("crop_id", pCropId).eq("kukaku_id", pKukakuId).between("hashu_date", start.getTime(), end.getTime()).findList();
        }
        else if (pKeys.size() > 0) {
          datas = MotochoBase.find.where().gt("total_kansui_count", 0).eq("crop_id", pCropId).in("kukaku_id", pKeys).between("hashu_date", start.getTime(), end.getTime()).findList();
        }
        else {
          datas = MotochoBase.find.where().gt("total_kansui_count", 0).eq("crop_id", pCropId).between("hashu_date", start.getTime(), end.getTime()).findList();
        }
      }
      for (MotochoBase data : datas) {
        results.add(data);
      }
    }
    return results;
  }
  public static Result getMLData() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    DecimalFormat df = new DecimalFormat("##0.0");
    String filename      = "MLData" + sdf.format(Calendar.getInstance().getTime());
    response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

    StringBuffer sb    = new StringBuffer();
    sb.append("\"kukaku\"");
    sb.append(",\"hinsyu\"");
    sb.append(",\"seiiku\"");
    sb.append(",\"sekisan\"");
    sb.append("\r\n");

    for (int farmId = 1; farmId <= 3; farmId++) {
      List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
      for (Compartment ct: cts) {
        List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", ct.kukakuId).orderBy("work_year, rotation_speed_of_year").findList();
        for (MotochoBase mb: mbs) {
          if (mb.hashuDate == null) { //播種が行われていない場合、除外する
            continue;
          }
          if (mb.shukakuStartDate == null) { //収穫が行われていない場合、除外する
            continue;
          }
          if (mb.totalSolarRadiation == 0) { //積算気温が0度の圃場は除外する
            continue;
          }
          String[] hinsyuIds = mb.hinsyuId.split(",");
          for (String hinsyuId:hinsyuIds) {
            sb.append(ct.kukakuId);
            sb.append(","+ hinsyuId);
            sb.append(","+ mb.seiikuDayCount);
            sb.append(","+ mb.totalSolarRadiation);
            sb.append("\r\n");
          }
        }
      }
    }
    try {
      return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return ok();
  }
  public static Result getMLData2() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    DecimalFormat df = new DecimalFormat("##0.0");
    String filename      = "MLData2" + sdf.format(Calendar.getInstance().getTime());
    response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

    StringBuffer sb    = new StringBuffer();
    sb.append("\"hinsyu\"");
    for(int i=1; i<= 20; i++) {
      sb.append(",\"kion" + i + "\"");
    }
    sb.append(",\"kousui\"");
    sb.append(",\"sekisan\"");
    sb.append("\r\n");
    for (int farmId = 1; farmId <= 3; farmId++) {
      List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
      for (Compartment ct: cts) {
        List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", ct.kukakuId).orderBy("work_year, rotation_speed_of_year").findList();
        for (MotochoBase mb: mbs) {
          if (mb.hashuDate == null) { //播種が行われていない場合、除外する
            continue;
          }
          if (mb.shukakuStartDate == null) { //収穫が行われていない場合、除外する
            continue;
          }
          if (mb.totalSolarRadiation == 0) { //積算気温が0度の圃場は除外する
            continue;
          }
          Field field = FieldComprtnent.getCompartment(mb.kukakuId).getFieldInfo();
          if (field == null) { //圃場が取得できない場合、除外する
            continue;
          }
          String point = PosttoPoint.getPointId(field.postNo);
          if ("".equals(point)) { //ポイントが取得できない場合、除外する
            continue;
          }
          Calendar start  = Calendar.getInstance();
          Calendar end    = Calendar.getInstance();
          end.setTimeInMillis(mb.hashuDate.getTime());
          end.add(Calendar.DAY_OF_MONTH, -1);
          start.setTimeInMillis(mb.hashuDate.getTime());
          start.add(Calendar.DAY_OF_MONTH, -20);
          List<Weather> weathers = Weather.getWeather(point, new java.sql.Date(start.getTimeInMillis()), new java.sql.Date(end.getTimeInMillis()));
          if (weathers.size() < 20) { //20日未満の場合、除外する
            Logger.error("[POINT]={} START={} END={} SIZE={}", point, sdf.format(start.getTime()), sdf.format(end.getTime()), weathers.size());
            continue;
          }
          String[] hinsyuIds = mb.hinsyuId.split(",");
          for (String hinsyuId:hinsyuIds) {
            sb.append(hinsyuId);
            double kousui = 0;
            for (Weather weather:weathers) {
              sb.append(","+ weather.kionAve);
              kousui += weather.rain;
            }
            sb.append(","+ kousui);
            sb.append(","+ mb.totalSolarRadiation);
            sb.append("\r\n");
          }
        }
      }
    }
    try {
      return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return ok();
  }
  public static Result getFeedBackData() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    DecimalFormat df = new DecimalFormat("##0.0");
    String filename      = "FeedBackData" + sdf.format(Calendar.getInstance().getTime());
    response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");

    StringBuffer sb    = new StringBuffer();
    sb.append("\"kukaku\"");
    sb.append(",\"kname\"");
    sb.append(",\"hinsyu\"");
    sb.append(",\"hname\"");
    sb.append(",\"crop\"");
    sb.append(",\"hashu\"");
    sb.append(",\"month\"");
    sb.append(",\"shukaku\"");
    for(int i=1; i<= 60; i++) {
      sb.append(",\"kion" + i + "\"");
    }
    sb.append(",\"kousui\"");
    sb.append(",\"nisyo\"");
    sb.append(",\"sekisan\"");
    sb.append(",\"seiiku\"");
    sb.append("\r\n");
    for (int farmId = 1; farmId <= 3; farmId++) {
      List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
      for (Compartment ct: cts) {
        List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", ct.kukakuId).orderBy("work_year, rotation_speed_of_year").findList();
        for (MotochoBase mb: mbs) {
          if (mb.hashuDate == null) { //播種が行われていない場合、除外する
            continue;
          }
          if (mb.shukakuStartDate == null) { //収穫が行われていない場合、除外する
            continue;
          }
          if (mb.totalSolarRadiation == 0) { //積算気温が0度の圃場は除外する
            continue;
          }
          Field field = FieldComprtnent.getCompartment(mb.kukakuId).getFieldInfo();
          if (field == null) { //圃場が取得できない場合、除外する
            continue;
          }
          String point = PosttoPoint.getPointId(field.postNo);
          if ("".equals(point)) { //ポイントが取得できない場合、除外する
            continue;
          }
          Calendar start  = Calendar.getInstance();
          Calendar end    = Calendar.getInstance();
          end.setTimeInMillis(mb.hashuDate.getTime());
          end.add(Calendar.DAY_OF_MONTH, -1);
          start.setTimeInMillis(mb.hashuDate.getTime());
          start.add(Calendar.DAY_OF_MONTH, -60);
          List<Weather> weathers = Weather.getWeather(point, new java.sql.Date(start.getTimeInMillis()), new java.sql.Date(end.getTimeInMillis()));
          if (weathers.size() < 60) { //60日未満の場合、除外する
            Logger.error("[POINT]={} START={} END={} SIZE={}", point, sdf.format(start.getTime()), sdf.format(end.getTime()), weathers.size());
            continue;
          }
          String[] hinsyuIds = mb.hinsyuId.split(",");
          String[] hinsyuNames = mb.hinsyuName.split("、");
          int i=0;
          for (String hinsyuId:hinsyuIds) {
            sb.append(mb.kukakuId);
            sb.append(","+ mb.kukakuName);
            sb.append(","+ hinsyuId);
            if ((hinsyuNames.length - 1) < i) {
              sb.append(","+ hinsyuNames[(hinsyuNames.length - 1)]);
            }
            else {
              sb.append(","+ hinsyuNames[i]);
            }
            i++;
            sb.append(","+ mb.cropName);
            sb.append(","+ sdf.format(mb.hashuDate));
            sb.append(","+ (mb.hashuDate.getMonth() + 1));
            sb.append(","+ sdf.format(mb.shukakuStartDate));
            double kousui = 0;
            double nisyo = 0;
            for (Weather weather:weathers) {
              sb.append(","+ weather.kionAve);
              kousui += weather.rain;
              nisyo  += weather.daylightHours;
            }
            sb.append(","+ kousui);
            sb.append(","+ nisyo);
            sb.append(","+ mb.totalSolarRadiation);
            sb.append(","+ mb.seiikuDayCount);
            sb.append("\r\n");
          }
        }
      }
    }
    try {
      return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return ok();
  }
//  public static Result getFeedBackData() {
//    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//    DecimalFormat df = new DecimalFormat("##0.0");
//    String filename      = "MLData" + sdf.format(Calendar.getInstance().getTime());
//    response().setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
//
//    StringBuffer sb    = new StringBuffer();
//    sb.append("\"区画ID\"");
//    sb.append(",\"品種ID\"");
//    sb.append(",\"播種日\"");
//    sb.append(",\"予測収穫開始\"");
//    sb.append(",\"予測生育日数\"");
//    sb.append(",\"実績収穫開始\"");
//    sb.append(",\"実績生育日数\"");
//    sb.append(",\"実績積算温度\"");
//    sb.append("\r\n");
//
//    List<Compartment> cts = Compartment.find.where().findList();
//    for (Compartment ct: cts) {
//      List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", ct.kukakuId).orderBy("work_year, rotation_speed_of_year").findList();
//      for (MotochoBase mb: mbs) {
//        if (mb.hashuDate == null) { //播種が行われていない場合、除外する
//          continue;
//        }
//        if (mb.shukakuStartDate == null) { //収穫が行われていない場合、除外する
//          continue;
//        }
//        String[] hinsyuIds = mb.hinsyuId.split(",");
//        for (String hinsyuId:hinsyuIds) {
//          sb.append(ct.kukakuId);
//          sb.append(","+ hinsyuId);
//          sb.append(",\""+ sdf.format(mb.hashuDate) +"\"");
//          sb.append(",\""+ sdf.format(mb.shukakuStartDate) +"\"");
//          sb.append(","+ mb.seiikuDayCount);
//          sb.append(","+ mb.totalSolarRadiation);
//          sb.append("\r\n");
//        }
//      }
//    }
//    try {
//      return ok(sb.toString().getBytes("Shift_JIS")).as("text/csv charset=Shift_JIS");
//    } catch (UnsupportedEncodingException e) {
//      e.printStackTrace();
//    }
//    return ok();
//  }
}
