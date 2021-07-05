package batch;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import com.avaje.ebean.Ebean;

import batch.type.AveTime;

import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Farm;
import models.Field;
import models.MotochoBase;
import models.Pest;
import models.PestOfCrop;
import models.PosttoPoint;
import models.Weather;
import play.Logger;
import play.api.Mode;
import play.api.Play;

/**
 * ステータスチェックコントローラ
 * @author kimura
 *
 */
public class PredictionPoint {

  public static Hashtable<String, AveTime> htAveTime;

  /**
   * メイン処理
   * @param args
   */
  public static void main(String args[]) {
    Logger.info("---------- PredictionPoint START ----------");
    Play.start(new play.api.DefaultApplication(new java.io.File("."),  PredictionPoint.class.getClassLoader(), null, Mode.Prod()));
    htAveTime = new Hashtable<String, AveTime>();
    int yyyy  = 0;
    int mm    = 0;
    int dd    = 0;


    while (true) {

      Calendar cal = Calendar.getInstance();

      if (!(yyyy == cal.get(Calendar.YEAR)
          && mm == cal.get(Calendar.MONTH)
          && dd == cal.get(Calendar.DAY_OF_MONTH)
          )
         ) {

        calcPredictionPoint();
        pestStatusPrediction();

      }

      try {
        Thread.sleep(1000 * 60);
        break;
      } catch (InterruptedException e) {
        e.printStackTrace();
        break;
      }
    }
    Play.stop();
    Logger.info("---------- PredictionPoint END ----------");
  }

  public static void calcPredictionPoint() {

    Logger.info("[ calcPredictionPoint ] PredictionPoint Calculation.");

    Ebean.beginTransaction();

    Ebean.createSqlUpdate("TRUNCATE TABLE prediction_point;").execute();

    List<Farm> farms = Farm.find.where().between("farm_id", 1, 3).orderBy("farm_id").findList();

    for (Farm farm : farms) {
      List<Double> keys = new ArrayList<Double>();
      List<Compartment> Compartments = Compartment.getCompartmentOfFarm(farm.farmId);
      for (Compartment compartment : Compartments) {
        keys.add(compartment.kukakuId);
      }
      List<MotochoBase> bases = MotochoBase.find.where().ne("hashu_date", null).ne("shukaku_start_date", null).in("kukaku_id", keys).orderBy("hinsyu_id").findList();
      String oldHinsyuId = "";
      int     kensu       = 0;
      double  point       = 0;
      double  shukakuryo  = 0;
      double  integratedTemp = 0;

      for (MotochoBase base : bases) {
        if (!"".equals(oldHinsyuId) && !oldHinsyuId.equals(base.hinsyuId)) {
          models.PredictionPoint pp = new models.PredictionPoint();
          pp.farmId   = farm.farmId;
          pp.hinsyuId = oldHinsyuId;
          if (kensu != 0) {
            point                   = (point / kensu);
            double pointShukakuryo  = (shukakuryo / kensu);
            double integratedTemp2  = (integratedTemp / kensu);

            BigDecimal bd               = new BigDecimal(point);
            BigDecimal bds              = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
            pp.predictionPoint          = bds.doubleValue();
            Logger.info("[ calcPredictionPoint ] farm={} oldHinsyuId={} shukakuryo={}.",farm.farmId, oldHinsyuId, pointShukakuryo);
            pp.predictionPointShukaku   = pointShukakuryo;
            pp.integratedTemp           = integratedTemp2;
            pp.save();
            kensu       = 0;
            point       = 0;
            shukakuryo  = 0;
            integratedTemp = 0;
          }
        }
        Compartment ct = Compartment.getCompartmentInfo(base.kukakuId);
        Field fd = ct.getFieldInfo();
        if (fd != null) {
          if (base.seiikuDayCount < 0) {
            continue;
          }
          String pointId = PosttoPoint.getPointId(fd.postNo);
          List<Weather> weathers = Weather.getWeather(pointId, base.hashuDate, base.shukakuStartDate);
          double  kion        = 0;
          double  nisyo       = 0;
          for (Weather weather : weathers) {
            kion    += weather.kionAve;
            nisyo   += weather.daylightHours;
            if (weather.kionAve < 10 ) {
              integratedTemp += 0;
            }
            else  if (25 < weather.kionAve) {
              integratedTemp += 25;
            }
            else {
              integratedTemp += weather.kionAve;
            }
          }
          if (nisyo != 0) {
            point   += (kion / nisyo);
            if ((kion / nisyo) != 0) {
              if (ct.area != 0) {
                shukakuryo   += ((base.totalShukakuNumber / ct.area) / (kion / nisyo));
              }
            }
          }

          kensu++;
          oldHinsyuId = base.hinsyuId;
        }
      }
      if (0 < kensu) {
        models.PredictionPoint pp = new models.PredictionPoint();
        pp.farmId   = farm.farmId;
        pp.hinsyuId = oldHinsyuId;
        if (kensu != 0) {
          point                   = (point / kensu);
          double pointShukakuryo  = (shukakuryo / kensu);
          double integratedTemp2  = (integratedTemp / kensu);

          BigDecimal bd               = new BigDecimal(point);
          BigDecimal bds              = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
          pp.predictionPoint          = bds.doubleValue();
          Logger.info("[ calcPredictionPoint ] farm={} oldHinsyuId={} shukakuryo={}.",farm.farmId, oldHinsyuId, pointShukakuryo);
          pp.predictionPointShukaku   = pointShukakuryo;
          pp.integratedTemp           = integratedTemp2;
          pp.save();
        }
      }
    }
    Ebean.commitTransaction();
  }
  public static void pestStatusPrediction() {

    Logger.info("[ pestStatusPrediction ] pestStatusPrediction START.");

    //当面は生産物がお茶の区画のみ算出する
    List<CompartmentWorkChainStatus> cwss = CompartmentWorkChainStatus.find.where().eq("crop_id", 25).orderBy("kukaku_id").findList();
    //生産物対象の害虫を取得する
    List<Pest> pests = PestOfCrop.getPestGeneration(25);
    Calendar syscal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    Logger.info("[ pestStatusPrediction ] PEST COUNT = {}.", pests.size());

    if (pests.size() == 0 ) {
      return;
    }

    for (CompartmentWorkChainStatus cws : cwss) { //対象区画分処理を行う
      CompartmentStatus cs = cws.getCompartmentStatus();
      if (cs != null) { //区画状況が取得できた場合
        Calendar datacal = Calendar.getInstance();
        datacal.setTime(cs.prevCalcDate);

        if (syscal.get(Calendar.YEAR) != datacal.get(Calendar.YEAR)) {
          cs.pestGeneration = 1;                                                //世代を1に戻す
          datacal.set(syscal.get(Calendar.YEAR), 0, 1);
          cs.targetSanpuDate = new java.sql.Date(datacal.getTimeInMillis());    //該当防除日を1月1日に変更する
        }

        Logger.info("[ pestStatusPrediction ] ---------------------------------------------------------------------------------------------------.");
        Logger.info("[ pestStatusPrediction ] KUKAKU = {} GENERATION = {} START DATE = {}.", cs.kukakuId, cs.pestGeneration, sdf.format(cs.targetSanpuDate));

        Compartment ct = Compartment.getCompartmentInfo(cs.kukakuId);
        Field fd = ct.getFieldInfo();
        String pointId = PosttoPoint.getPointId(fd.postNo);
        if (pointId != null && !"".equals(pointId)) {
          Logger.info("[ pestStatusPrediction ] POINT = {}.", pointId);
          datacal.set(syscal.get(Calendar.YEAR), 11, 31);
          List<Weather> weathers = Weather.getWeather(pointId, cs.targetSanpuDate, new java.sql.Date(datacal.getTimeInMillis()));
          Pest pest = Pest.getPestGeneration(pests.get(0).pestId, cs.pestGeneration);
          if (pest != null) {
            Logger.info("[ pestStatusPrediction ] PEST = {} PestIntegratedKion = {}.", pest.pestId, pest.pestIntegratedKion);
            double kion = 0;
            boolean update = false;
            for (Weather weather : weathers) {
              kion += weather.kionAve;
              if (pest.pestIntegratedKion < kion) {
                cs.pestId = pest.pestId;
                cs.pestIntegratedKion = kion;
                cs.pestPredictDate = weather.dayDate;
                update = true;
                break;
              }
            }
            if (!update) {
              cs.pestId = 0;
              cs.pestIntegratedKion = 0;
              datacal.set(1900, 0, 1);
              cs.pestPredictDate = new java.sql.Date(datacal.getTimeInMillis());
            }
          }
        }
        cs.prevCalcDate = new java.sql.Date(syscal.getTimeInMillis());
        cs.update();
      }
    }

  }
  public static void pestStatusPredictionSingle(CompartmentStatus cs) {

//    Logger.info("[ pestStatusPredictionSingle ] pestStatusPrediction START.");

    //生産物対象の害虫を取得する
    List<Pest> pests = PestOfCrop.getPestGeneration(25);
    Calendar syscal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

//    Logger.info("[ pestStatusPredictionSingle ] PEST COUNT = {}.", pests.size());

    if (pests.size() == 0 ) {
      return;
    }

    if (cs != null) { //区画状況が取得できた場合
      Calendar datacal = Calendar.getInstance();
      datacal.setTime(cs.prevCalcDate);

      if (syscal.get(Calendar.YEAR) != datacal.get(Calendar.YEAR)) {
        cs.pestGeneration = 1;                                                //世代を1に戻す
        datacal.set(syscal.get(Calendar.YEAR), 0, 1);
        cs.targetSanpuDate = new java.sql.Date(datacal.getTimeInMillis());    //該当防除日を1月1日に変更する
      }

//      Logger.info("[ pestStatusPredictionSingle ] ---------------------------------------------------------------------------------------------------.");
//      Logger.info("[ pestStatusPredictionSingle ] KUKAKU = {} GENERATION = {} START DATE = {}.", cs.kukakuId, cs.pestGeneration, sdf.format(cs.targetSanpuDate));

      Compartment ct = Compartment.getCompartmentInfo(cs.kukakuId);
      Field fd = ct.getFieldInfo();
      String pointId = PosttoPoint.getPointId(fd.postNo);
      if (pointId != null && !"".equals(pointId)) {
//        Logger.info("[ pestStatusPredictionSingle ] POINT = {}.", pointId);
        datacal.set(syscal.get(Calendar.YEAR), 11, 31);
        List<Weather> weathers = Weather.getWeather(pointId, cs.targetSanpuDate, new java.sql.Date(datacal.getTimeInMillis()));
        Pest pest = Pest.getPestGeneration(pests.get(0).pestId, cs.pestGeneration);
        if (pest != null) {
//          Logger.info("[ pestStatusPredictionSingle ] PEST = {} PestIntegratedKion = {}.", pest.pestId, pest.pestIntegratedKion);
          double kion = 0;
          boolean update = false;
          for (Weather weather : weathers) {
            kion += weather.kionAve;
            if (pest.pestIntegratedKion < kion) {
              cs.pestId = pest.pestId;
              cs.pestIntegratedKion = kion;
              cs.pestPredictDate = weather.dayDate;
              update = true;
              break;
            }
          }
          if (!update) {
            cs.pestId = 0;
            cs.pestIntegratedKion = 0;
            datacal.set(1900, 0, 1);
            cs.pestPredictDate = new java.sql.Date(datacal.getTimeInMillis());
          }
        }
      }
      cs.prevCalcDate = new java.sql.Date(syscal.getTimeInMillis());
      cs.update();
    }

  }
}