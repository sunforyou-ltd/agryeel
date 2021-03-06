package batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import models.Account;
import models.Compartment;
import models.Farm;
import models.FarmStatus;
import models.PlanLine;
import models.PosttoPoint;
import models.SsKikiData;
import models.SsKikiMaster;
import models.TimeLine;
import models.Weather;
import models.WorkDiary;
import models.WorkPlan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.Logger;
import play.api.Mode;
import play.api.Play;
import util.DateU;
import batch.type.AveTime;

import com.avaje.ebean.Ebean;

import consts.AgryeelConst;

/**
 * ステータスチェックコントローラ
 * @author kimura
 *
 */
public class StatusCheck {

  public static Hashtable<String, AveTime> htAveTime;
  private static final String USER__AGENT = "Mozilla/5.0";

  /**
   * メイン処理
   * @param args
   */
  public static void main(String args[]) {
    Logger.info("---------- StatusCheck START ----------");
    Play.start(new play.api.DefaultApplication(new java.io.File("."),  StatusCheck.class.getClassLoader(), null, Mode.Prod()));
    htAveTime = new Hashtable<String, AveTime>();
    int yyyy  = 0;
    int mm    = 0;
    int dd    = 0;
    int hh    = -1;
    boolean wup = false;

    while (true) {

      Calendar cal = Calendar.getInstance();

      if (!(yyyy == cal.get(Calendar.YEAR)
          && mm == cal.get(Calendar.MONTH)
          && dd == cal.get(Calendar.DAY_OF_MONTH)
          )
         ) {

        calcAveTimeOfFarm();
        moveWorkPlan();
        deleteWorkDiary();

        wup = false;

      }

      if (cal.get(Calendar.HOUR_OF_DAY) == 2 && wup == false) {
        updateWeather();
        wup = true;
      }

      statusCheck();

      if (hh != cal.get(Calendar.HOUR_OF_DAY)) {
        getSenSprout();
      }

      yyyy  = cal.get(Calendar.YEAR);
      mm    = cal.get(Calendar.MONTH);
      dd    = cal.get(Calendar.DAY_OF_MONTH);
      hh    = cal.get(Calendar.HOUR_OF_DAY);

      try {
        Thread.sleep(1000 * 60);
      } catch (InterruptedException e) {
        e.printStackTrace();
        break;
      }
    }
    Play.stop();
    Logger.info("---------- StatusCheck END ----------");
  }
  public static void getSenSprout() {

    Logger.info("---------- getSenSprout START ----------");
    List<SsKikiMaster> skms = SsKikiMaster.find.where().ne("kukaku_id", 0).findList();
    Logger.info("MASTER RECORD = {}", skms.size());

    for (SsKikiMaster skm :skms) {
      Logger.info("TARTGET FARM[{}] NODE[{}] ", skm.farmId, skm.nodeId);
      String filename = AgryeelConst.SenSprout.DLF + "data_" + skm.nodeId + ".csv";
      ProcessBuilder p = new ProcessBuilder(AgryeelConst.SenSprout.PYTHON
                                            , AgryeelConst.SenSprout.EXECMDL, skm.nodeId, skm.userId, skm.password, "-f" + filename);
      p.redirectErrorStream(true);

      // プロセスを開始する
      Process process;
      try {
        process = p.start();
        // 結果を受け取る
        try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
            String line;
            while ((line = r.readLine()) != null) {
              Logger.info(line);
            }
        }
        int result = process.exitValue();
        Logger.info("result={}", result);
        if (result == 0) {
          File inputFile  = new File(filename);
          boolean head    = true;
          boolean delete  = false;
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          List<String> lines = Files.readAllLines(inputFile.toPath(), StandardCharsets.ISO_8859_1);
          for (String linedata : lines) {
            if (head) { //ヘッダデータの場合
              //ヘッダデータ読み飛ばし
              head = false;
              continue;
            }
            //作業記録データの読み込み
            String sData[] = linedata.split(",");
            Logger.info("------------------------------------------------------------------");
            int itemIndex = 0;
            SsKikiData skd = new SsKikiData();
            skd.kukakuId = skm.kukakuId;
            for (String data: sData){
              Logger.info(data);
              switch (itemIndex) {
              case 0:
                Calendar dt = Calendar.getInstance();
                try {
                  dt.setTime(sdf.parse(data.substring(0,19)));
                  if (!delete) { //削除していない場合
                    Calendar start  = Calendar.getInstance();
                    Calendar end    = Calendar.getInstance();
                    start.setTime(dt.getTime());
                    end.setTime(dt.getTime());
                    DateU.setTime(start, DateU.TimeType.FROM);
                    DateU.setTime(end, DateU.TimeType.TO);
                    Ebean.createSqlUpdate("DELETE FROM ss_kiki_data WHERE kukaku_id = :kukakuID AND keisoku_day_time BETWEEN :start AND :end;")
                         .setParameter("kukakuID", skm.kukakuId).setParameter("start", new java.sql.Timestamp(start.getTimeInMillis())).setParameter("end", new java.sql.Timestamp(end.getTimeInMillis()))
                         .execute();
                    delete = true;
                  }
                  skd.keisokuDayTime = new java.sql.Timestamp(dt.getTimeInMillis());
                } catch (ParseException e) {
                  e.printStackTrace();
                  Logger.error(e.getMessage(),e);
                  break;
                }
                break;
              case 1:
                skd.vmc10   = Double.parseDouble(data);
                break;
              case 2:
                skd.vmc20   = Double.parseDouble(data);
                break;
              case 3:
                skd.current = Double.parseDouble(data);
                break;
              case 4:
                skd.voltage = Double.parseDouble(data);
                break;
              case 5:
                skd.gt      = Double.parseDouble(data);
                break;
              default:
                break;
              }
              itemIndex++;
            }
            if (itemIndex==6) { //全て格納できた場合
              skd.save();
            }
          }
        }
        else {
          Logger.error("File DownLoad Error. Process SKIP!!");
        }
      } catch (IOException e) {
        e.printStackTrace();
        Logger.error(e.getMessage(), e);
      }
    }


    Logger.info("---------- getSenSprout END ----------");

  }
  public static void calcAveTimeOfFarm() {

    Logger.info("[ calcAveTimeOfFarm ] Average Worktime Calculation.");
    List<Farm> farms = Farm.find.orderBy("farm_id").findList();

      for (Farm farm : farms) {
        //----- AICA基本作業の集計 -----
        List<models.Work> works = models.Work.getWorkOfFarm(0);
        for (models.Work work : works) {
          List<TimeLine> timeLines = TimeLine.find.where().eq("work_id", work.workId).eq("farm_id", farm.farmId).findList();
          AveTime at = new AveTime();
          at.farmId = farm.farmId;
          at.workId = work.workId;
          int iCount = 0;
          for (TimeLine tl : timeLines) {
            WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
            if(wd != null) {
              at.aveTime += wd.workTime;
              iCount++;
            }
          }
          if (iCount != 0) {
            at.aveTime = Math.ceil(at.aveTime / iCount);
          }
          String key = String.format("%05d", (long)at.farmId) + String.format("%05d", (long)at.workId);
          htAveTime.put(key, at);
          Logger.info("[ calcAveTimeOfFarm ] FARM:{} WORK:{} AVE:{}", at.farmId, at.workId, at.aveTime);
        }
        //----- 各生産者独自作業の集計-----
        works = models.Work.getWorkOfFarm(farm.farmId);
        for (models.Work work : works) {
          List<TimeLine> timeLines = TimeLine.find.where().eq("work_id", work.workId).eq("farm_id", farm.farmId).findList();
          AveTime at = new AveTime();
          at.farmId = farm.farmId;
          at.workId = work.workId;
          int iCount = 0;
          for (TimeLine tl : timeLines) {
            WorkDiary wd = WorkDiary.getWorkDiaryById(tl.workDiaryId);
            if(wd != null) {
              at.aveTime += wd.workTime;
              iCount++;
            }
          }
          if (iCount != 0) {
            at.aveTime = Math.ceil(at.aveTime / iCount);
          }
          String key = String.format("%05d", (long)at.farmId) + String.format("%05d", (long)at.workId);
          htAveTime.put(key, at);
          Logger.info("[ calcAveTimeOfFarm ] FARM:{} WORK:{} AVE:{}", at.farmId, at.workId, at.aveTime);
        }
      }
  }
  public static void statusCheck() {
//    Logger.info("[ statusCheck ] Account Status Check.");
    List<Farm> farms = Farm.find.orderBy("farm_id").findList();
    Date dSystem = Calendar.getInstance().getTime();

    for (Farm farm : farms) {
      List<Account> accounts = Account.getAccountOfWorking(farm.farmId);

      for (Account account : accounts) {
        String key = String.format("%05d", (long)farm.farmId) + String.format("%05d", (long)account.workId);
        AveTime at = htAveTime.get(key);
        if (at != null) {
          if (at.aveTime == 0) {
            continue;
          }
          models.Work work = models.Work.getWork(account.workId);
          if (work != null) {
            Date dWorking = new java.util.Date(account.workStartTime.getTime());
            long  lDiff = dSystem.getTime() - dWorking.getTime();
            lDiff = lDiff / (1000 * 60);
//            Logger.info("[ statusCheck ] Account={} Work={} Danger={}min Note={}min Wornning={}min Now={}min."
//                , account.accountId
//                , account.workId
//                , (at.aveTime * (1 + (work.dangerPer / 100)))
//                , (at.aveTime * (1 + (work.notePer / 100)))
//                , (at.aveTime * (1 + (work.worningPer / 100)))
//                , lDiff
//                );
            if ((at.aveTime * (1 + (work.dangerPer / 100))) < lDiff) { //危険時間を超えている場合
              account.messageIcon         = AgryeelConst.MessageIcon.DANGER;
              account.notificationMessage = "作業時間が平均を大幅に超えています。</br>至急、担当者の状況を確認してください。";
            }
            else if ((at.aveTime * (1 + (work.notePer / 100))) < lDiff) { //注意時間を超えている場合
              account.messageIcon         = AgryeelConst.MessageIcon.NOTE;
              account.notificationMessage = "作業時間が平均を超えています。</br>担当者の状況を気に掛けてください。";
            }
            else if ((at.aveTime * (1 + (work.worningPer / 100))) < lDiff) { //警告時間を超えている場合
              account.messageIcon         = AgryeelConst.MessageIcon.WORNING;
              account.notificationMessage = "作業時間が平均をやや超えています";
            }
            else {
              account.messageIcon         = AgryeelConst.MessageIcon.NONE;
              account.notificationMessage = "【現在作業中】";
            }
            account.update();
          }
        }
      }
    }
  }
  public static void updateWeather() {

    //-------------------------------------------------------------------------------------------
    // パラメータの生成
    //-------------------------------------------------------------------------------------------
    //気象庁データダウンロードのURL
    try {
      Logger.info("-------------------------------------------------------------------------------------");
      Logger.info(">>>>> Update Weather START.");

      //前日の日付に変更する
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DAY_OF_MONTH, -1);

      List<PosttoPoint> points = PosttoPoint.find.orderBy("prec_no,block_no").findList();
      long oPrec = 0;
      long oBlock = 0;
      for (PosttoPoint point : points) {
        if (point.precNo == 0) {
          continue;
        }
        if (oPrec == point.precNo && oBlock == point.blockNo) {
          continue;
        }

        oPrec = point.precNo;
        oBlock = point.blockNo;

        Logger.info("[ {} ] PrecNo: {} BlockNo: {}", point.pointId, point.precNo, point.blockNo);
        Document doc = Jsoup.connect("https://www.data.jma.go.jp/obd/stats/etrn/view/daily_s1.php?prec_no="+point.precNo+"&block_no="+point.blockNo+"&year="+cal.get(Calendar.YEAR)+"&month="+(cal.get(Calendar.MONTH)+1)+"&day=&view=s1").get();
        Elements newsHeadlines = doc.select("tr.mtx");
        int count = 0;
        for (Element headline : newsHeadlines) {
          count++;
          if (count <= 4) {
            continue;
          }
          Elements datas = headline.select("td");
          if (Integer.parseInt(datas.get(0).select("div a").get(0).ownText()) != cal.get(Calendar.DAY_OF_MONTH)) {
            continue;
          }
          //気象情報を削除する
          Ebean.createSqlUpdate("DELETE FROM weather WHERE point_id = :pointId AND day_date = :dayDate")
          .setParameter("pointId", point.pointId).setParameter("dayDate", new java.sql.Date(cal.getTimeInMillis())).execute();

          Weather w = new Weather();
          w.pointId = point.pointId;
          w.dayDate = new java.sql.Date(cal.getTimeInMillis());
          w.kionAve = Double.parseDouble(datas.get(6).ownText());
          w.kionMax = Double.parseDouble(datas.get(7).ownText());
          w.kionMin = Double.parseDouble(datas.get(8).ownText());
          try {
            w.rain    = Double.parseDouble(datas.get(3).ownText());
          } catch (NumberFormatException nfe) {
            w.rain    = 0;
          }
          w.daylightHours = Double.parseDouble(datas.get(16).ownText());
          w.jituyo = 0;
          w.save();
          break;
        }
        controllers.AICAController.SimpleRegressionTest(point.pointId);
      }
      Logger.info(">>>>> Point Data Make End.");
      controllers.SystemBatch.calcSolarRadiation();
      Logger.info(">>>>> SolarRadiation Data Update End.");
      Logger.info(">>>>> Update Weather END.");

    } catch (IOException e1) {
      // TODO 自動生成された catch ブロック
      e1.printStackTrace();
    }
 }
  /**
   * 残っている作業指示を当日へ移動する
   */
  public static void moveWorkPlan() {
    Logger.info("-------------------- [ moveWorkPlan ] --------------------");
    //作業指示フラグの作成
    List<Integer> flags = new ArrayList<Integer>();
    flags.add(AgryeelConst.WORKPLANFLAG.WORKPLANCOMMIT);
    flags.add(AgryeelConst.WORKPLANFLAG.AICAPLANCOMMIT);

    //生産者の取得
    List<Farm> farms = Farm.find.orderBy("farm_id").findList();

    //前日を算出する
    Calendar sys = Calendar.getInstance();
    java.sql.Date sd = new java.sql.Date(sys.getTimeInMillis());
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -1);
    java.sql.Date td = new java.sql.Date(cal.getTimeInMillis());

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    for (Farm farm : farms) { //生産者分処理を行う
      //対象区画IDの生成
      List<Double> keys = Compartment.getKukakuIdOfFarm(farm.farmId);
      //生産者ステータスの取得
      FarmStatus fm = FarmStatus.getFarmStatus(farm.farmId);
      if (fm == null || fm.workPlanAutoMove == AgryeelConst.WORKPLANAUTOMOVE.NONE) { //生産者ステータスが存在しない、または作業指示の自動移動なし
        continue;
      }
      //作業指示の取得
      List<WorkPlan> wps = WorkPlan.find.where().le("work_date", td).in("work_plan_flag", flags).in("kukaku_id", keys).findList();
      for (WorkPlan wp : wps) {
        if (fm.workPlanAutoMove == AgryeelConst.WORKPLANAUTOMOVE.LIMITMOVE) { //作業指示の自動移動(２日まで)の場合
          Calendar datadate = Calendar.getInstance();
          String uuid = wp.workPlanUUID;
          if (uuid == null || "".equals(uuid.trim())) { //UUIDが存在しない場合は移動させない
            continue;
          }
          datadate.set(Integer.parseInt(uuid.substring(6, 10)), (Integer.parseInt(uuid.substring(10, 12)) - 1), Integer.parseInt(uuid.substring(12, 14)));
          DateU.setTime(datadate, DateU.TimeType.FROM);
          java.sql.Date ddd = new java.sql.Date(datadate.getTimeInMillis());
          long diff = DateU.GetDiffDate(ddd, sd);
          Logger.info("[ WORKPLANAUTOMOVE ] DIFF UUIDDATE={} DIFF={}.", sdf.format(ddd), diff);
          if (2 < diff) {
            continue;
          }
        }
        PlanLine pl = PlanLine.find.where().eq("work_plan_id", wp.workPlanId).findUnique();
        if (pl == null) {
          continue;
        }
        pl.workDate = sd;
        pl.updateTime = new java.sql.Timestamp(sys.getTimeInMillis());
        pl.update();
        Logger.info("[ WORKPLANAUTOMOVE ] MOVING ID={} UUID={} WORKDATE {} > {}.", wp.workPlanId, wp.workPlanUUID, sdf.format(wp.workDate), sdf.format(sd));
        wp.workDate = sd;
        wp.update();
      }
    }
  }
  /**
   * お試し利用の作業記録を削除する
   */
  public static void deleteWorkDiary() {
    Logger.info("-------------------- [ deleteWorkDiary ] --------------------");

    //4日前を算出する
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_MONTH, -4);
    java.sql.Date td = new java.sql.Date(cal.getTimeInMillis());

    //対象区画IDの生成
    List<Double> keys = Compartment.getKukakuIdOfFarm(20);
    //作業記録の取得
    List<WorkDiary> wds = WorkDiary.find.where().le("work_date", td).in("kukaku_id", keys).findList();
    for (WorkDiary wd : wds) {
      Ebean.createSqlUpdate("DELETE FROM work_diary WHERE work_diary_id = :workDiaryId")
      .setParameter("workDiaryId", wd.workDiaryId).execute();

      Ebean.createSqlUpdate("DELETE FROM work_diary_sanpu WHERE work_diary_id = :workDiaryId")
      .setParameter("workDiaryId", wd.workDiaryId).execute();

      Ebean.createSqlUpdate("DELETE FROM time_line WHERE work_diary_id = :workDiaryId")
      .setParameter("workDiaryId", wd.workDiaryId).execute();

      Ebean.createSqlUpdate("DELETE FROM work_diary_detail WHERE work_diary_id = :workDiaryId")
      .setParameter("workDiaryId", wd.workDiaryId).execute();
    }
  }
}