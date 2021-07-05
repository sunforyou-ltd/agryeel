package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
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
import models.Crop;
import models.Hinsyu;
import models.HinsyuOfFarm;
import models.Kiki;
import models.MotochoBase;
import models.Nouhi;
import models.ObussanNouyaku;
import models.Sequence;
import models.Size;
import models.SizeOfFarm;
import models.TimeLine;
import models.Work;
import models.WorkDiaryDetail;
import models.WorkDiarySanpu;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;
import util.StringU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CompartmentStatusCompornent;
import compornent.MotochoCompornent;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】カラーリングファーム専用コンバート
 * @author SunForYou
 *
 */
public class ColoringController extends Controller {

  //----- カラム位置 -----
  //----- 区画 -----
  public static int KUKAKU = 11;
  //----- アカウント -----
  public static int ACCOUNT = 9;
  //----- 生産物 -----
  public static int CROP = 5;
  //----- 作型 -----
  public static int SAKUGATA = 6;
  //----- 品種 -----
  public static int HINSYU = 7;
  //----- 作業 -----
  public static int WORK = 15;
  //----- 作業日 -----
  public static int WORK_DATE = 1;
  public static int WORK_START_TIME = 17;
  public static int WORK_END_TIME = 18;
  //----- 播種 -----
  public static int HASHU = 4;
  //----- 根水 -----
  public static int NEMIZU = 11;
  public static int NEMIZU_MIN = 12;
  //----- 潅水 -----
  public static int KANSUI_START = 13;
  public static int KANSUI_LOOP = 19;
  public static int KANSUI_STEP = 2;
  //----- 防除 -----
  public static int BAIRITSU = 40;
  public static int SANPURYO = 41;
  //----- 肥料 -----
  public static int HIRYO_NAME = 24;
  public static int HIRYO_SANPURYO = 38;
  //----- 収穫 -----
  public static int SHUKAKU_START = 92;
  public static int SHUKAKU_LOOP = 10;
  public static int SHUKAKU_STEP = 2;
  //----- 収穫 -----
  public static int COMMENT = 21;

  public static Result coloringMakeMaster() {
    ObjectNode resultJson = Json.newObject();

    String[] nouhiNames = {
        "モスピラン水溶剤"
       ,"ユニフォーム粒剤"
       ,"グレーシア乳剤"
       ,"ゼンターリ顆粒水和剤"
       ,"ベネビア"
       ,"マッチ乳剤"
       ,"アディオン乳剤"
       ,"アフェットフロアブル"
       ,"Zボルドー"
       ,"リドミルＭＺ水和剤"
       ,"アディオン"
       ,"ライメイフロアブル"
       ,"046"
       ,"パレードフロアブル"
       ,"D-D"
    };

    for (String nouhiName : nouhiNames) {
      Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.NOUHIID);       //最新シーケンス値の取得

      Nouhi nouhi = new Nouhi();
      nouhi.nouhiId    = sequence.sequenceValue;
      nouhi.nouhiName   = nouhiName;
      nouhi.nouhiKind   = AgryeelConst.NouhiKind.NOUYAKU;
      nouhi.bairitu     = 1000;
      nouhi.sanpuryo    = 60000;
      nouhi.farmId      = 2;
      nouhi.unitKind    = 2;
      nouhi.n           = 0;
      nouhi.p           = 0;
      nouhi.k           = 0;
      nouhi.mg          = 0;
      nouhi.lower       = 0;
      nouhi.upper       = 10000;
      nouhi.finalDay    = 0;
      nouhi.sanpuCount  = 0;
      nouhi.useWhen     = 0;
      nouhi.save();

    }

    String[] hinsyuNames = {
        "ジャスティス"
       ,"千姫"
       ,"はりはり"
       ,"はりはり605"
       ,"京すだれ"
       ,"乃木坂"
       ,"若月"
       ,"みやび姫"
       ,"光姫"
       ,"春のセンバツ"
       ,"神楽坂"
       ,"冬里"
       ,"いなむら"
    };

    for (String hinsyuName : hinsyuNames) {
      Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.HINSYUID);       //最新シーケンス値の取得

      Hinsyu hinsyu = new Hinsyu();
      hinsyu.hinsyuId    = sequence.sequenceValue;
      hinsyu.hinsyuName  = hinsyuName;
      hinsyu.cropId      = 15;
      hinsyu.save();

      HinsyuOfFarm hof = new HinsyuOfFarm();
      hof.hinsyuId     = hinsyu.hinsyuId;
      hof.farmId       = 2;
      hof.save();

    }

    return ok(resultJson);
  }
  public static Result coloringDeleteData() {

    ObjectNode resultJson = Json.newObject();
    try {

      Ebean.beginTransaction();

      List<TimeLine> tls = TimeLine.find.where().eq("farm_id", 15).findList();
      for (TimeLine tl : tls) {
        double workDiaryId = tl.workDiaryId;
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

      }

      Ebean.commitTransaction();

    }
    catch (Exception e) {
      Ebean.rollbackTransaction();
    }
    return ok(resultJson);

  }
  /**
   * カラーリングファーム専用作業記録コンバート
   * @return
   */
	public static Result colorConvertInput(boolean motocho, boolean kisyo) {

        ObjectNode resultJson = Json.newObject();
        String preKukaku = "";
        String preWork = "ini";
        int cnt = 0;

        //----- CSVデータを開く -----
        String path = "c:\\temp\\coloringdata.csv";
        Logger.info("");
        Logger.info("");
        Logger.info("[ DATA CONVERT STRAT ] FILE= {} ", path);
        try{
          File inputFile  = new File(path);
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
          SimpleDateFormat sdft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
          long linecount = 0;
          List<String> lines = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
          for (String linedata : lines) {
            linecount++;
            Logger.info("PROCESS LINE={}.", linecount);
            //作業記録データの読み込み
            String sData[] = linedata.split(",");
            //----- 区画IDへのコンバート -----
            String kukaku = sData[KUKAKU];

            double kukakuId = convertKukakuId(kukaku);
            if (kukakuId == 0) {
              Logger.error("[ KUKAKU CONVERT ERROR! ] NAME = {}" , sData[KUKAKU]);
              continue;
            }
            if (!preKukaku.equals("") && !preKukaku.equals(kukaku)) {
              cnt = 0;
              preWork = "ini";
            }
            preKukaku = kukaku;

            //----- アカウントIDへのコンバート -----
            String accountName = sData[ACCOUNT];
            String workName = sData[WORK];
            String accountId = convertAccountId(accountName);
            if (accountId == "") {
              if (workName.equals("播種") || workName.equals("定植") || workName.equals("肥料散布")) {
                accountId = "coloring1";
              }
              else {
                continue;
              }
            }

            //----- 生産物IDへのコンバート -----
            String cropName = sData[CROP].replaceAll("露地ホウレンソウ", "ほうれん草");
            cropName = cropName.replaceAll("ハウスホウレンソウ", "ほうれん草");
            cropName = cropName.replaceAll("スピナッチ", "ほうれん草");
            cropName = cropName.replaceAll("みずな", "水菜");
            cropName = cropName.replaceAll("チンゲンサイ", "チンゲン菜");
            cropName = cropName.replaceAll("山ほうれんそう", "山ほうれん草");

            double cropId = 0;
            double hinsyuId = 0;
            if (cropName.indexOf("その他") < 0) {
              cropId = convertCropId(cropName);
              if (cropId == 0) {
                Logger.error("[ CROP CONVERT ERROR! ] NAME = {}" , cropName);
                continue;
              }

              //if (cropId == 3 && kukaku.indexOf("露地") >= 0) { //露地ほうれん草の場合
              //  cropId = 18;
              //}

              //----- 品種IDへのコンバート -----
              //Logger.info("[ HINSYU NAME ] NAME = {}" , sData[HINSYU]);
              String hinsyuItem = sData[HINSYU];
              hinsyuId = convertHinsyuId(hinsyuItem);
              if (hinsyuId == 0) {
                Logger.error("[ HINSYU CONVERT ERROR! ] NAME = {}" , sData[HINSYU]);
                continue;
              }
            }

            //----- 作業日取得 -----
            java.util.Date dtWork;
            java.util.Date dtWorkStart;
            java.util.Date dtWorkEnd;
            String workStartTime = sData[WORK_DATE] + " " + sData[WORK_START_TIME] + ":00";
            String workEndTime = sData[WORK_DATE] + " " + sData[WORK_END_TIME] + ":00";
            if (sData[WORK_START_TIME].equals("")) {
              workStartTime = sData[WORK_DATE] + " " + "00:00:00";
            }
            if (sData[WORK_END_TIME].equals("")) {
              workEndTime = sData[WORK_DATE] + " " + "00:00:00";
            }
            try {
              dtWork = sdf.parse(sData[WORK_DATE]);
              dtWorkStart = sdft.parse(workStartTime);
              dtWorkEnd = sdft.parse(workEndTime);
            } catch (ParseException e) {
              Logger.error("[ WORKDATE CONVERT DATE ERROR! ]");
              e.printStackTrace();
              continue;
            }

            //----- 作付開始データの作成 -----
            String sakuGata = sData[SAKUGATA];
            if ((kisyo && cnt == 0) ||
                (workName.equals("播種") && !preWork.equals("播種")) ||
                (workName.equals("肥料散布") && cropName.equals("さつまいも") && !preWork.equals("肥料散布"))) {
              Logger.info("[ PROC DATA ] SAKUSTART = {}", sdf.format(dtWork));
              if (kisyo && cnt == 0) {
                makingSakustart(kukakuId, dtWork, "coloring1", true);
              } else {
                makingSakustart(kukakuId, dtWork, accountId, false);
              }
            }
            preWork = workName;
            cnt++;

            double nouhiId = 0;
            double bairitsu = 0;
            double sanpuryo = 0;
            String hiryoName = sData[HIRYO_NAME];
            if (hiryoName.equals("日産スピノエース顆粒水和剤")) {
              hiryoName = "スピノエース顆粒水和剤";
            }
            else if (hiryoName.equals("ホクコーゼンターリ顆粒水和剤")) {
              hiryoName = "ゼンターリ顆粒水和剤";
            }
            else if (hiryoName.equals("デュポンレンザー")) {
              hiryoName = "レンザー";
            }

            if (workName.equals("耕起") ||
                workName.equals("耕運")) {
              //----- 耕すデータの作成 -----
              makingTagayasu(workName, kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId);
            }
            else if (workName.equals("播種") ||
                     workName.equals("定植")) {
              //----- 播種データの作成 -----
              makingHashu(workName, kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId, String.valueOf(hinsyuId));
            }
            else if (workName.indexOf("防除") >= 0) {
              //----- 防除データの作成 -----
              bairitsu = Double.parseDouble(sData[BAIRITSU]);
              sanpuryo = Double.parseDouble(sData[SANPURYO]);
              if (workName.indexOf("ゼンターリ") >= 0) {
                nouhiId = 541;
              }
              else if (workName.indexOf("スピノエース") >= 0) {
                nouhiId = 542;
              }
              if (sanpuryo != 0) {
                sanpuryo = sanpuryo * 1000;
              }
              makingShodoku(kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId, nouhiId, bairitsu, sanpuryo);
            }
            else if (workName.equals("農薬散布")) {
              //----- 農薬散布データの作成 -----
              bairitsu = Double.parseDouble(sData[BAIRITSU]);
              sanpuryo = Double.parseDouble(sData[SANPURYO]);
              if (sanpuryo != 0) {
                sanpuryo = sanpuryo * 1000;
              }
              makingNouyakuSanpu(kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId, hiryoName, bairitsu, sanpuryo);
            }
            else if (workName.equals("肥料散布")) {
              //----- 肥料散布データの作成 -----
              sanpuryo = Double.parseDouble(sData[HIRYO_SANPURYO]);
              if (sanpuryo != 0) {
                sanpuryo = sanpuryo * 1000;
              }
              makingHiryou(kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId, hiryoName, sanpuryo);
            }
            else if (workName.equals("追肥")) {
              //----- 追肥データの作成 -----
              sanpuryo = Double.parseDouble(sData[HIRYO_SANPURYO]);
              if (sanpuryo != 0) {
                sanpuryo = sanpuryo * 1000;
              }
              makingTuihi(kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId, hiryoName, sanpuryo);
            }
            else if (workName.equals("灌水")) {
              //----- 潅水データの作成 -----
              makingKansui(kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId);
            }
            else if (workName.equals("収穫") ||
                     workName.equals("収穫・調整")) {
              //----- 収穫データの作成 -----
              makingShukaku(kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId);
            }
            else {
              //----- その他データの作成 -----
              makingOther(workName, kukakuId, dtWork, dtWorkStart, dtWorkEnd, accountId, sData[COMMENT]);
            }
          }

          if (motocho) {
            List<Compartment> cts = Compartment.getCompartmentOfFarm(15);
            for (Compartment ct : cts) {
              Logger.info("PROCESS KUKAKU={}.", ct.kukakuId);
              /* 元帳照会を更新する */
              MotochoCompornent motochoCompornent = new MotochoCompornent(ct.kukakuId);
              motochoCompornent.make();

              List<TimeLine> tls = TimeLine.getTimeLineOfFarm(15, motochoCompornent.lastMotochoBase.workStartDay, motochoCompornent.lastMotochoBase.workEndDay);

              if (tls.size() > 0) {
                /* 区画状況照会を更新する */
                CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(ct.kukakuId, tls.get(tls.size()-1).workId);
                compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);
              }
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
          Logger.error(e.getMessage(),e );
        }
        Logger.info("[ DATA CONVERT END ] ");
        return ok(resultJson);
	}

	/**
	 * 区画IDを取得する
	 * @param pName
	 * @return
	 */
	private static double convertKukakuId(String pName) {
	  double result = 0;
	  Compartment kukaku = Compartment.find.where().eq("kukaku_name", pName).eq("farm_id", 15).findUnique();

	  if (kukaku != null) {
	    result = kukaku.kukakuId;
	  }
	  return result;
	}

	/**
	 * アカウントIDを取得する
	 * @param pName
	 * @return
	 */
	private static String convertAccountId(String pName) {
	  String result = "";
	  Account account = Account.find.where().eq("acount_name", pName).eq("farm_id", 15).findUnique();

	  if (account != null) {
	    result = account.accountId;
	  }
	  return result;
	}

	/**
	 * 生産物IDを取得する
	 * @param pName
	 * @return
	 */
	private static double convertCropId(String pName) {
    double result = 0;
	  Crop crop = Crop.find.where().eq("crop_name", pName).findUnique();
    if (crop != null) {
      result = crop.cropId;
    }
    return result;
	}
	/**
	 * 品種IDを取得する
	 * @param pName
	 * @return
	 */
	private static double convertHinsyuId(String pName) {
    double result = 0;
    List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(15);
    List<Double> keys = new ArrayList<Double>();

    for (HinsyuOfFarm hof : hofs) {
      keys.add(hof.hinsyuId);
    }
    Hinsyu hinsyu = Hinsyu.find.where().eq("hinsyu_name", pName).in("hinsyu_id", keys).findUnique();
    if (hinsyu != null) {
      result = hinsyu.hinsyuId;
    }
    return result;
	}
	/**
	 * 農肥IDを取得する
	 * @param pName
	 * @param pCrop
	 * @return
	 */
  private static double convertNouhiId(String pName) {
    double result = 0;

    Nouhi nouhi = Nouhi.find.where().eq("nouhi_name", pName).eq("farm_id", 15).findUnique();
    if (nouhi != null) {
      result = nouhi.nouhiId;
    }
    return result;
  }
  private static double convertSizeId(String pName) {
    double result = 0;
    List<SizeOfFarm> sofs = SizeOfFarm.getSizeOfFarm(2);
    List<Double> keys = new ArrayList<Double>();

    for (SizeOfFarm sof : sofs) {
      keys.add(sof.sizeId);
    }
    Size size = Size.find.where().eq("size_name", pName).in("size_id", keys).findUnique();
    if (size != null) {
      result = size.sizeId;
    }
    return result;
  }
	/**
	 * 作付開始データを作成する
	 * @param pKukakuId
	 * @param pHashu
	 * @param pKisyo
	 * @return
	 */
	private static int makingSakustart(double pKukakuId, java.util.Date pWorkDt, String account, boolean pKisyo) {

	  int result = 0;
	  Calendar cSakuStart = Calendar.getInstance();
	  cSakuStart.setTime(pWorkDt);
	  DateU.setTime(cSakuStart, DateU.TimeType.FROM);
      if (pKisyo) {
        cSakuStart.add(Calendar.DAY_OF_MONTH, -5);
      } else {
        cSakuStart.add(Calendar.DAY_OF_MONTH, -1);
      }
      double workDiaryId    = 0;

      models.WorkDiary wkd = new models.WorkDiary();
      Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

      Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
      workDiaryId     = sequence.sequenceValue;

      int workId = 13;

      /* 共通項目 */
      wkd.workDiaryId   = workDiaryId;
      wkd.workId        = workId;
      wkd.kukakuId      = pKukakuId;

      wkd.workDate      = new java.sql.Date(cSakuStart.getTimeInMillis());
      wkd.workTime      = 0;
      wkd.accountId     = account;

      wkd.workStartTime  = new java.sql.Timestamp(cSakuStart.getTimeInMillis());
      wkd.workEndTime    = new java.sql.Timestamp(cSakuStart.getTimeInMillis());

      wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
      wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
      wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
      wkd.workPlanUUID      = "";

      wkd.save();
      makingTimeLine(wkd);

//    Logger.info("[ SAKU START MAKE ] WORK DIARY ID = {}", workDiaryId);

      return result;

	}
  /**
   * その他データを作成する
   * @param pWork
   * @param pKukakuId
   * @param pWorkDt
   * @param pWorkStDt
   * @param pWorkEdDt
   * @param account
   * @return
   */
  private static int makingOther(String pWork, double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account, String pComment) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);     
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    /* 作業ID */
    int workId = 0;
    if(pWork.equals("片付け")) {
      workId = 1;
    }
    else if(pWork.equals("播種準備")) {
      workId = 138;
    }
    else if(pWork.equals("洗浄")) {
      workId = 139;
    }
    else if(pWork.equals("調製") ||
            pWork.equals("調整")) {
      workId = 140;
    }
    else if(pWork.equals("草取り")) {
      workId = 141;
    }
    else if(pWork.equals("廃棄")) {
      workId = 142;
    }
    else if(pWork.equals("ビニール・パイプ作業")) {
      workId = 143;
    }
    else if(pWork.equals("畝作り") ||
            pWork.equals("畝立て")) {
      workId = 144;
    }
    else if(pWork.equals("出荷・運搬・配達")) {
      workId = 146;
    }
    else if(pWork.equals("ハウス横あげ・草取り")) {
      workId = 147;
    }
    else if(pWork.equals("草刈り")) {
      workId = 148;
    }
    else if(pWork.equals("堆肥散布")) {
      workId = 149;
    }
    else if(pWork.equals("太陽熱消毒")) {
      workId = 150;
    }
    else if(pWork.equals("片づけ・移動")) {
      workId = 151;
    }
    else if(pWork.equals("加工作業")) {
      workId = 153;
    }
    else if(pWork.equals("その他")) {
      workId = 154;
    }
    else if(pWork.equals("事務作業")) {
      workId = 155;
    }
    else {
      workId = 154;
      pComment = pWork;
    }

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = workId;
    wkd.kukakuId      = pKukakuId;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    wkd.save();

    if (workId == 153 || workId == 154) {
      for (int i=0; i<5; i++ ) {
        /* 作業記録詳細情報を生成する */
        WorkDiaryDetail wdd = new WorkDiaryDetail();                                  //作業記録散布情報
    
        wdd.workDiaryId         = wkd.workDiaryId;                                    //作業記録ＩＤ
        wdd.workDiarySequence   = i+1;                                                //作業記録シーケンス
        if (i == 0) {
          wdd.comment = pComment;                                                     //コメント
        }
        else {
          wdd.comment = "";                                                           //コメント
        }
        wdd.save();
      }
    }

    makingTimeLine(wkd);

//    Logger.info("[ NEMIZU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
  /**
   * 耕すデータを作成する
   * @param pKukakuId
   * @param pWorkDt
   * @param pWorkStDt
   * @param pWorkEdDt
   * @param account
   * @return
   */
  private static int makingTagayasu(String pWork, double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);     
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    /* 作業ID */
    int workId = 0;
    if(pWork.equals("耕起")) {
      workId = 4;
    }
    else if(pWork.equals("耕運")) {
      workId = 152;
    }

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = workId;
    wkd.kukakuId      = pKukakuId;

    wkd.kikiId        = 93;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    wkd.save();
    makingTimeLine(wkd);

//    Logger.info("[ NEMIZU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
	/**
	 * 播種データを作成する
	 * @param pKukakuId
	 * @param pHashu
	 * @param pHinsyu
	 * @return
	 */
  private static int makingHashu(String pWork, double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account, String pHinsyu) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);     
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    /* 作業ID */
    int workId = 0;
    if(pWork.equals("播種")) {
      workId = 6;
    }
    else if(pWork.equals("定植")) {
      workId = 145;
    }

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = workId;
    wkd.kukakuId      = pKukakuId;
    wkd.hinsyuId      = pHinsyu;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    wkd.save();
    makingTimeLine(wkd);

//    Logger.info("[ HASHU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
  /**
   * 潅水データを作成する
   * @param pKukakuId
   * @param pKansui
   * @param pTime
   * @return
   */
  private static int makingKansui(double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    int workId = 9;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = workId;
    wkd.kukakuId      = pKukakuId;

    wkd.kansuiMethod  = 0;
    wkd.kikiId        = 0;
    wkd.kansuiRyo     = 0;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    wkd.save();
    makingTimeLine(wkd);

//    Logger.info("[ NEMIZU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }


  private static int makingShodoku(double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account, double nouhiId, double bairitsu, double sanpuryo) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = 8;
    wkd.kukakuId      = pKukakuId;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    if (nouhiId == 0 && sanpuryo == 0) {
      result = -1;
    }
    else {
      //----- 散布情報の生成 -----
      WorkDiarySanpu wds = new WorkDiarySanpu();

      wds.workDiaryId = wkd.workDiaryId;
      wds.workDiarySequence   = 1;
      wds.sanpuMethod         = 3;
      wds.nouhiId             = nouhiId;
      wds.bairitu             = bairitsu;
      wds.sanpuryo            = sanpuryo;
      wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
      wds.motochoUpdate       = AgryeelConst.UpdateFlag.NONE;

      wds.save();
    }
    wkd.save();
    makingTimeLine(wkd);
//    Logger.info("[ SHODOKU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
  private static int makingNouyakuSanpu(double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account, String hiryoName, double bairitsu, double sanpuryo) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = 8;
    wkd.kukakuId      = pKukakuId;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    if ("".equals(hiryoName)) {
      result = -1;
    }
    else {
      //----- 散布情報の生成 -----
      double nouhiId = convertNouhiId(hiryoName);
      WorkDiarySanpu wds = new WorkDiarySanpu();

      wds.workDiaryId = wkd.workDiaryId;
      wds.workDiarySequence   = 1;
      wds.sanpuMethod         = 3;
      wds.nouhiId             = nouhiId;
      wds.bairitu             = bairitsu;
      wds.sanpuryo            = sanpuryo;
      wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
      wds.motochoUpdate       = AgryeelConst.UpdateFlag.NONE;

      wds.save();
      wkd.save();
      makingTimeLine(wkd);
    }
//    Logger.info("[ SHODOKU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
  private static int makingHiryou(double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account, String hiryoName, double sanpuryo) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    int     iWorkId = 0;
    double dKikiId  = 0;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = 3;
    wkd.kukakuId      = pKukakuId;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    if ("".equals(hiryoName)) {
      result = -1;
    }
    else {
      //----- 散布情報の生成 -----
      double nouhiId = convertNouhiId(hiryoName);
      WorkDiarySanpu wds = new WorkDiarySanpu();

      wds.workDiaryId         = wkd.workDiaryId;
      wds.workDiarySequence   = 1;
      wds.sanpuMethod         = 3;
      wds.nouhiId             = nouhiId;
      wds.bairitu             = 1;
      wds.sanpuryo            = sanpuryo;
      wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
      wds.motochoUpdate       = AgryeelConst.UpdateFlag.NONE;

      wds.save();
      wkd.save();
      makingTimeLine(wkd);
    }
//    Logger.info("[ HIRYO MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
  private static int makingTuihi(double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account, String hiryoName, double sanpuryo) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    int     iWorkId = 0;
    double dKikiId  = 0;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = 10;
    wkd.kukakuId      = pKukakuId;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    if ("".equals(hiryoName)) {
      result = -1;
    }
    else {
      //----- 散布情報の生成 -----
      double nouhiId = convertNouhiId(hiryoName);
      WorkDiarySanpu wds = new WorkDiarySanpu();

      wds.workDiaryId         = wkd.workDiaryId;
      wds.workDiarySequence   = 1;
      wds.sanpuMethod         = 3;
      wds.nouhiId             = nouhiId;
      wds.bairitu             = 1;
      wds.sanpuryo            = sanpuryo;
      wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
      wds.motochoUpdate       = AgryeelConst.UpdateFlag.NONE;

      wds.save();
    }
    wkd.save();
    makingTimeLine(wkd);
//    Logger.info("[ HIRYO MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
  private static int makingShukaku(double pKukakuId, java.util.Date pWorkDt, java.util.Date pWorkStDt, java.util.Date pWorkEdDt, String account) {

    int result = 0;
    long diff = pWorkEdDt.getTime() - pWorkStDt.getTime();
    long diffMinutes = diff / (60 * 1000);
    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    int iWorkId = 0;
    iWorkId = 11;

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = iWorkId;
    wkd.kukakuId      = pKukakuId;

    wkd.workDate      = new java.sql.Date(pWorkDt.getTime());
    wkd.workTime      = (int)diffMinutes;
    wkd.accountId     = account;

    wkd.workStartTime  = new java.sql.Timestamp(pWorkStDt.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pWorkEdDt.getTime());

    wkd.shukakuRyo      = 0;
    wkd.syukakuNisugata = 0;
    wkd.syukakuSitsu    = 0;
    wkd.syukakuSize     = 0;

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    wkd.save();

    /* 作業記録詳細情報を生成する */
    WorkDiaryDetail wdd = new WorkDiaryDetail();                                  //作業記録散布情報

    wdd.workDiaryId         = wkd.workDiaryId;                                    //作業記録ＩＤ
    wdd.workDiarySequence   = 1;                                                  //作業記録シーケンス

    wdd.syukakuNisugata = wkd.syukakuNisugata;                                    //荷姿
    wdd.syukakuSitsu    = wkd.syukakuSitsu;                                       //質
    wdd.syukakuSize     = wkd.syukakuSize;                                        //サイズ
    wdd.syukakuKosu     = 0;                                                      //個数
    wdd.shukakuRyo      = wkd.shukakuRyo;                                         //収穫量
    wdd.save();

    makingTimeLine(wkd);

//      Logger.info("[ SHUKAKU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }

  private static void makingTimeLine(models.WorkDiary workDiary) {

    Work work = Work.find.where().eq("work_id", workDiary.workId).findUnique();                       //作業情報モデルの取得
    DecimalFormat df = new DecimalFormat("#,##0.00");


    /* -- タイムラインを作成する -- */
    TimeLine timeLine = new TimeLine();                                       //タイムラインモデルの生成
    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.TIMELINEID);               //最新シーケンス値の取得
    Compartment compartment = Compartment.find.where().eq("kukaku_id", workDiary.kukakuId).findUnique();      //区画情報モデルの取得
    Account account     = Account.find.where().eq("account_id", workDiary.accountId).findUnique();        //アカウント情報モデルの取得

    timeLine.timeLineId       = sequence.sequenceValue;                           //タイムラインID

    timeLine.updateTime       = DateU.getSystemTimeStamp();
    //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
    timeLine.message        = "【" + work.workName + "情報】<br>";
    switch ((int)work.workTemplateId) {
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

        timeLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + wds.bairitu + "倍&nbsp;&nbsp;" + df.format(wds.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";

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
      break;
    case AgryeelConst.WorkTemplate.SHUKAKU:
      timeLine.message       += "<荷姿> "     + Common.GetCommonValue(Common.ConstClass.NISUGATA , workDiary.syukakuNisugata) + "<br>";
      timeLine.message       += "<質> "       + Common.GetCommonValue(Common.ConstClass.SHITU    , workDiary.syukakuSitsu) + "<br>";
      timeLine.message       += "<サイズ> "   + Common.GetCommonValue(Common.ConstClass.SIZE     , workDiary.syukakuSize) + "<br>";
      timeLine.message       += "<収穫量> "   + workDiary.shukakuRyo + "Kg" + "<br>";
      break;
    case AgryeelConst.WorkTemplate.NOUKO:
      timeLine.message       += "<機器> " + Kiki.getKikiName(workDiary.kikiId) + "<br>";
      timeLine.message       += "<アタッチメント> " + Attachment.getAttachmentName(workDiary.attachmentId) + "<br>";
      break;
    case AgryeelConst.WorkTemplate.KANSUI:
      timeLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, workDiary.kansuiMethod) + "<br>";
      timeLine.message       += "<機器> " + Kiki.getKikiName(workDiary.kikiId) + "<br>";
      timeLine.message       += "<潅水量> " + workDiary.kansuiRyo + "L" + "<br>";
      break;
    case AgryeelConst.WorkTemplate.KAISHU:
      List<WorkDiaryDetail> wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
      int idx = 0;
      for (WorkDiaryDetail wdd : wdds) {
        idx++;
        timeLine.message        +=  "<数量" + idx + ">" + "&nbsp;&nbsp;" + wdd.suryo + "個<br>";

      }
      break;
    case AgryeelConst.WorkTemplate.DACHAKU:
      wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
      idx = 0;
      for (WorkDiaryDetail wdd : wdds) {
        idx++;
        timeLine.message        +=  "<資材" + idx + ">" + "&nbsp;&nbsp;" + Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)wdd.sizaiId, true) + "<br>";

      }
      break;
    case AgryeelConst.WorkTemplate.COMMENT:
      wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
      idx = 0;
      for (WorkDiaryDetail wdd : wdds) {
        idx++;
        timeLine.message        +=  "<コメント" + idx + ">" + "&nbsp;&nbsp;" + wdd.comment + "<br>";

      }
      break;
    case AgryeelConst.WorkTemplate.MALTI:
      timeLine.message       += "<使用マルチ> " + Common.GetCommonValue(Common.ConstClass.ITOMULTI, (int)workDiary.useMulti, true) + "<br>";
      timeLine.message       += "<列数> " + workDiary.retusu + "列" + "<br>";
      break;
    case AgryeelConst.WorkTemplate.TEISHOKU:
      timeLine.message       += "<使用苗枚数> " + workDiary.naemaisu + "枚" + "<br>";
      timeLine.message       += "<列数> " + workDiary.retusu + "列" + "<br>";
      break;
    case AgryeelConst.WorkTemplate.NAEHASHU:
      timeLine.message       += "<使用穴数> " + workDiary.useHole + "穴" + "<br>";
      timeLine.message       += "<枚数> " + workDiary.maisu + "枚" + "<br>";
      timeLine.message       += "<使用培土> " + Common.GetCommonValue(Common.ConstClass.ITOBAIDO, (int)workDiary.useBaido, true) + "<br>";
      break;
    case AgryeelConst.WorkTemplate.HAIKI:
      timeLine.message       += "<廃棄量> " + workDiary.haikiRyo + "Kg" + "<br>";
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
    timeLine.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    timeLine.workPlanUUID      = "";

    timeLine.save();                                                //タイムラインを追加

  }
  private static String changeKukakuName(String pKukakuName) {
    String[][] kukakuNames = {
        {"ハワイ1", "ハワイ 1"}
       ,{"ハワイ2", "ハワイ 2"}
       ,{"ハワイ3", "ハワイ 3"}
       ,{"ハワイ4", "ハワイ 4"}
       ,{"ハワイ5", "ハワイ 5"}
       ,{"ハワイ6", "ハワイ 6"}
       ,{"ハワイ7", "ハワイ 7"}
       ,{"ハワイ8", "ハワイ 8"}
       ,{"ハワイ9", "ハワイ 9"}
       ,{"西春日1", "西春日 1"}
       ,{"西春日2", "西春日 2"}
       ,{"西春日3", "西春日 3"}
       ,{"西春日4", "西春日 4"}
       ,{"西春日5", "西春日 5"}
       ,{"西春日6", "西春日 6"}
       ,{"西春日7", "西春日 7"}
       ,{"西春日8", "西春日 8"}
       ,{"西春日9", "西春日 9"}
       ,{"中春日1", "中春日 1"}
       ,{"中春日2", "中春日 2"}
       ,{"中春日3", "中春日 3"}
       ,{"中春日4", "中春日 4"}
       ,{"中春日5", "中春日 5"}
       ,{"中春日6", "中春日 6"}
       ,{"中春日7", "中春日 7"}
       ,{"中春日8", "中春日 8"}
       ,{"中春日9", "中春日 9"}
       ,{"ブラジル1", "ブラジル 1"}
       ,{"ブラジル2", "ブラジル 2"}
       ,{"ブラジル3", "ブラジル 3"}
       ,{"ブラジル4", "ブラジル 4"}
       ,{"ブラジル5", "ブラジル 5"}
       ,{"ブラジル6", "ブラジル 6"}
       ,{"ブラジル7", "ブラジル 7"}
       ,{"ブラジル8", "ブラジル 8"}
       ,{"ブラジル9", "ブラジル 9"}
       ,{"団地1", "団地 1"}
       ,{"団地2", "団地 2"}
       ,{"団地3", "団地 3"}
       ,{"団地4", "団地 4"}
       ,{"団地5", "団地 5"}
       ,{"団地6", "団地 6"}
       ,{"団地7", "団地 7"}
       ,{"団地8", "団地 8"}
       ,{"団地9", "団地 9"}
       ,{"ゴンダ1", "ゴンダ 1"}
       ,{"ゴンダ2", "ゴンダ 2"}
       ,{"ゴンダ3", "ゴンダ 3"}
       ,{"ゴンダ4", "ゴンダ 4"}
       ,{"ゴンダ5", "ゴンダ 5"}
       ,{"ゴンダ6", "ゴンダ 6"}
       ,{"ゴンダ7", "ゴンダ 7"}
       ,{"ゴンダ8", "ゴンダ 8"}
       ,{"ゴンダ9", "ゴンダ 9"}
       ,{"東畑1", "東畑 1"}
       ,{"東畑2", "東畑 2"}
       ,{"東畑3", "東畑 3"}
       ,{"東畑4", "東畑 4"}
       ,{"東畑5", "東畑 5"}
       ,{"東畑6", "東畑 6"}
       ,{"東畑7", "東畑 7"}
       ,{"東畑8", "東畑 8"}
       ,{"東畑9", "東畑 9"}
       ,{"前田1", "前田 1"}
       ,{"前田2", "前田 2"}
       ,{"前田3", "前田 3"}
       ,{"前田4", "前田 4"}
       ,{"前田5", "前田 5"}
       ,{"前田6", "前田 6"}
       ,{"前田7", "前田 7"}
       ,{"前田8", "前田 8"}
       ,{"前田9", "前田 9"}
       ,{"西畑1", "西畑 1"}
       ,{"西畑2", "西畑 2"}
       ,{"西畑3", "西畑 3"}
       ,{"西畑4", "西畑 4"}
       ,{"西畑5", "西畑 5"}
       ,{"西畑6", "西畑 6"}
       ,{"西畑7", "西畑 7"}
       ,{"西畑8", "西畑 8"}
       ,{"西畑9", "西畑 9"}
       ,{"東春日1", "東春日 1"}
       ,{"東春日2", "東春日 2"}
       ,{"東春日3", "東春日 3"}
       ,{"東春日4", "東春日 4"}
       ,{"東春日5", "東春日 5"}
       ,{"東春日6", "東春日 6"}
       ,{"東春日7", "東春日 7"}
       ,{"東春日8", "東春日 8"}
       ,{"東春日9", "東春日 9"}
       ,{"中畑1", "中畑 1"}
       ,{"中畑2", "中畑 2"}
       ,{"中畑3", "中畑 3"}
       ,{"中畑4", "中畑 4"}
       ,{"中畑5", "中畑 5"}
       ,{"中畑6", "中畑 6"}
       ,{"中畑7", "中畑 7"}
       ,{"中畑8", "中畑 8"}
       ,{"中畑9", "中畑 9"}
       ,{"盛華園1", "盛華園 1"}
       ,{"盛華園2", "盛華園 2"}
       ,{"盛華園3", "盛華園 3"}
       ,{"盛華園4", "盛華園 4"}
       ,{"盛華園5", "盛華園 5"}
       ,{"盛華園6", "盛華園 6"}
       ,{"盛華園7", "盛華園 7"}
       ,{"盛華園8", "盛華園 8"}
       ,{"盛華園9", "盛華園 9"}
    };
    String kukakuName = pKukakuName.replaceAll("-", "－");
    for (int i=0; i<kukakuNames.length; i++ ) {
      if (kukakuNames[i][0].equals(pKukakuName)) {
        kukakuName = kukakuNames[i][1];
        break;
      }
    }
    return kukakuName;
  }
  //----- カラム位置 -----
  //----- 品目 -----
  public static int N_CROP = 4;
  //----- 作業日 -----
  public static int N_WORKDATE = 3;
  //----- 農薬 -----
  public static int N_NOUYAKU_START = 11;
  public static int N_NOUYAKU_LOOP  = 5;
  public static int N_NOUYAKU_STEP  = 4;
  //----- 区画 -----
  public static int N_KUKAKU_START = 31;
  public static int N_KUKAKU_LOOP  = 20;
  public static int N_KUKAKU_STEP  = 2;

  public static Result coloringNouhiInput() {

    ObjectNode resultJson = Json.newObject();

    //----- CSVデータを開く -----
    String path = "c:\\temp\\nouhidata.csv";
    Logger.info("");
    Logger.info("");
    Logger.info("[ NOUHI DATAINPUT STRAT ] FILE= {} ", path);
    try{

      Ebean.beginTransaction();

      Ebean.createSqlUpdate("TRUNCATE TABLE obussan_nouyaku").execute();

      File inputFile  = new File(path);
      boolean head = true;
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
      long linecount = 0;
      List<String> lines = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
      for (String linedata : lines) {
        linecount++;
        Logger.info("PROCESS LINE={}.", linecount);
        if (head) { //ヘッダデータの場合
          //ヘッダデータ読み飛ばし
          head = false;
          continue;
        }
        //農薬散布記録データの読み込み
        String sData[] = linedata.split(",");
        //----- 作業日の取得 -----
        if (sData[N_WORKDATE] == null || "".equals(sData[N_WORKDATE])) {
          Logger.error("[ WORK DATA NULL ERROR! ]");
          continue;
        }
        java.util.Date dtWorkdate;
        try {
          dtWorkdate = sdf.parse(sData[N_WORKDATE]);
        } catch (ParseException e) {
          Logger.error("[ WORK CONVERT DATE ERROR! ]");
          e.printStackTrace();
          continue;
        }
        //----- 生産物IDへのコンバート -----
        double cropId = convertCropId(sData[N_CROP]);
        if (cropId == 0) {
          Logger.error("[ CROP CONVERT ERROR! ] NAME = {}" , sData[N_CROP]);
          continue;
        }
        //----- 農薬情報の取得 -----
        double sr = 1;
        for (int i=0; i<N_NOUYAKU_LOOP; i++) {
            if ((sData.length - 1) < N_NOUYAKU_START + (N_NOUYAKU_STEP*i)) {
              break;
            }
            if (sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i)] == null || "".equals(sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i)])) {
              break;
            }
            String nm = sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i)];
            double nouhiId = convertNouhiId(nm);
            if (nouhiId == 0) {
              Logger.error("[ NOUHI CONVERT ERROR! ] NAME = {}" , nm);
              continue;
            }
            double br = 1;

            if (i==0) {
              if (sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i) - 1] != null && !"".equals(sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i) - 1])) {
                sr = Double.parseDouble(sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i) - 1]);
              }
            }
            if (sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i) + 1] != null && !"".equals(sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i) + 1])) {
              br = Double.parseDouble(sData[N_NOUYAKU_START + (N_NOUYAKU_STEP*i) + 1]);
            }
            //----- 区画 情報分散布データを作成する-----
            for (int y=0; y<N_KUKAKU_LOOP; y++) {
              if ((sData.length - 1) < N_KUKAKU_START + (N_KUKAKU_STEP*y)) {
                break;
              }
              if (sData[N_KUKAKU_START + (N_KUKAKU_STEP*y)] == null || "".equals(sData[N_KUKAKU_START + (N_KUKAKU_STEP*y)])) {
                break;
              }
              String kukakuName = changeKukakuName(sData[N_KUKAKU_START + (N_KUKAKU_STEP*y)]);
              double kukakuId = convertKukakuId(kukakuName);
              if (kukakuId == 0) {
                Logger.error("[ KUKAKU CONVERT ERROR! ] NAME = {}" , sData[N_KUKAKU_START + (N_KUKAKU_STEP*y)]);
                continue;
              }

              ObussanNouyaku on = new ObussanNouyaku();
              on.workDate = new java.sql.Date(dtWorkdate.getTime());
              on.nouhiId  = nouhiId;
              on.kukakuId = kukakuId;
              on.sanpuryo = sr;
              on.bairitu  = br;
              on.save();

            }
        }
      }
      Ebean.commitTransaction();
    } catch (IOException e) {
      e.printStackTrace();
      Logger.error(e.getMessage(),e );
    } catch (Exception se) {
      Logger.error(se.getMessage(), se);
      Ebean.rollbackTransaction();
    }

    Logger.info("[ NOUHI DATAINPUT END ] ");
    return ok(resultJson);
  }
  public static Result coloringReData() {
    ObjectNode resultJson = Json.newObject();
    Logger.info("-------------------------------------------------------------");
    Logger.info("[ReData]Oobussann Data Remake");

    List<Compartment> cts = Compartment.getCompartmentOfFarm(2);
    for (Compartment ct: cts) {
      List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", ct.kukakuId).findList();
      for (MotochoBase mb : mbs) {
        if (mb.hinsyuId != null && !"".equals(mb.hinsyuId)) {
          Hinsyu hs = Hinsyu.getHinsyuInfo(Double.parseDouble(mb.hinsyuId));
          if (hs != null) {
            Crop cp = Crop.getCropInfo(hs.cropId);
            if (cp!= null) {
              mb.cropId   = cp.cropId;
              mb.cropName = cp.cropName;
              mb.update();
            }
          }
        }
      }
      CompartmentStatus cs = CompartmentStatus.find.where().eq("kukaku_id", ct.kukakuId).findUnique();
      if (cs != null) {
        if (cs.hinsyuId != null && !"".equals(cs.hinsyuId)) {
          Hinsyu hs = Hinsyu.getHinsyuInfo(Double.parseDouble(cs.hinsyuId));
          if (hs != null) {
            Crop cp = Crop.getCropInfo(hs.cropId);
            if (cp!= null) {
              cs.cropId   = cp.cropId;
              cs.update();
            }
          }
        }
      }
    }

    return ok(resultJson);
  }
}
