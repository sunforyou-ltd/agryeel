package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import models.Nisugata;
import models.Nouhi;
import models.ObussanNouyaku;
import models.Sequence;
import models.Size;
import models.SizeOfFarm;
import models.Syukka;
import models.SyukkaSaki;
import models.TimeLine;
import models.Work;
import models.WorkDiaryDetail;
import models.WorkDiarySanpu;

import org.apache.commons.lang3.math.NumberUtils;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】出荷情報取り込み
 * @author SunForYou
 *
 */
public class SyukkaController extends Controller {

  //*******************************************************************************************************************
  // 共通定義
  //*******************************************************************************************************************
  //----- 項目数 -----
  private static int ITEMCOUNT = 19;
  //*******************************************************************************************************************
  // カラム位置
  //*******************************************************************************************************************
  private class ShipmentConst {
    //----- 出荷日 -----
    public static final int YEAR = 2;
    public static final int MONTH = 3;
    public static final int DATE = 4;
    //----- 出荷先 -----
    public static final int SHIPMENT = 0;
    //----- 品目 -----
    public static final int CROP = 5;
    //----- 商品名 -----
    public static final int ITEM = 6;
    //----- 生産者 -----
    public static final int FARM = 7;
    //----- 産地 -----
    public static final int ORIGIN = 8;
    //----- 場所 -----
    public static final int PLACE = 9;
    //----- 荷姿 -----
    public static final int NISUGATA = 10;
    //----- サイズ -----
    public static final int SIZE = 11;
    //----- 数量 -----
    public static final int JURYO = 12;
    //----- 入数 -----
    public static final int IRISU = 13;
    //----- 単価 -----
    public static final int TANKA = 16;
    //----- 出荷量 -----
    public static final int SHIPMENTRYO = 17;
    //----- 金額 -----
    public static final int MONEY = 18;
  }
  /**
   * 出荷情報取込み処理
   * @return
   */
	public static Result shipmentData(double farmId) {

        ObjectNode resultJson = Json.newObject();

        //----- CSVデータを開く -----
        String path = "c:\\temp\\shipmentData.csv";
        Logger.info("************************************************************************************************");
        Logger.info("[ Shipment Data Recv START ]");
        try{
          // トランザクション開始
          Ebean.beginTransaction();
          File inputFile  = new File(path);
          boolean head = true;
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
          long linecount = 0;
          List<String> lines = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
          for (String linedata : lines) {
            linecount++;
            if (head) { //ヘッダデータの場合
              //ヘッダデータ読み飛ばし
              head = false;
              continue;
            }
            //作業記録データの読み込み
            String sData[] = linedata.split(",");
            StringBuffer msg = new StringBuffer();
            //----- 項目数チェック -----
            if (sData.length < ITEMCOUNT) {
              Logger.info("[ 項目数エラー ] LINE:{} ITEM:{}", linecount, sData.length);
              continue;
            }
            //----- 出荷日の作成 -----
            if (!NumberUtils.isNumber(sData[ShipmentConst.YEAR])
                || !NumberUtils.isNumber(sData[ShipmentConst.MONTH])
                || !NumberUtils.isNumber(sData[ShipmentConst.DATE])) { /* いづれかの値が数値じゃない場合 */
              Logger.info("[ 出荷日変換エラー ] YEAR:{} MONTH:{} DATE:{} "
                  , sData[ShipmentConst.YEAR], sData[ShipmentConst.MONTH], sData[ShipmentConst.DATE]);
              continue;
            }
            Syukka ship = new Syukka();
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(sData[ShipmentConst.YEAR])
                  , Integer.parseInt(sData[ShipmentConst.MONTH]) - 1
                  , Integer.parseInt(sData[ShipmentConst.DATE]));
            java.sql.Date syukaDate = new java.sql.Date(cal.getTimeInMillis());
            ship.syukkaDate = syukaDate;
            //----- 出荷No.の生成 -----
            String syukaNo = Syukka.makeSyukkaNo(farmId, syukaDate);
            ship.syukkaNo = syukaNo;
            //----- 出荷先 -----
            ship.syukkaSakiId = SyukkaSaki.convertId(sData[ShipmentConst.SHIPMENT], farmId);
            //----- 品目 -----
            ship.cropId = Crop.convertId(sData[ShipmentConst.CROP]);
            //----- 商品名 -----
            ship.syohinName = sData[ShipmentConst.ITEM];
            //----- 生産者 -----
            ship.farmId = farmId;
            //----- 産地 -----
            ship.origin = sData[ShipmentConst.ORIGIN];
            //----- 場所 -----
            ship.place = sData[ShipmentConst.PLACE];
            //----- 荷姿 -----
            ship.nisugataId = Nisugata.convertId(sData[ShipmentConst.NISUGATA], farmId);
            //----- サイズ -----
            ship.sizeId = Size.convertId(sData[ShipmentConst.SIZE], farmId);
            //----- 重量 -----
            try {
              ship.jyuRyo = Double.parseDouble(sData[ShipmentConst.JURYO]);
            }
            catch(NumberFormatException nfe) {
              ship.jyuRyo = 0;
            }
            //----- 入数 -----
            try {
              ship.irisu = Double.parseDouble(sData[ShipmentConst.IRISU]);
            }
            catch(NumberFormatException nfe) {
              ship.irisu = 0;
            }
            //----- 単価 -----
            try {
              Logger.info("TANKA:{}", sData[ShipmentConst.TANKA]);
              ship.tanka = Double.parseDouble(sData[ShipmentConst.TANKA]);
            }
            catch(NumberFormatException nfe) {
              ship.tanka = 0;
            }
            //----- 出荷量 -----
            try {
              Logger.info("SHIP:{}", sData[ShipmentConst.SHIPMENTRYO]);
              ship.syukkaRyo = Double.parseDouble(sData[ShipmentConst.SHIPMENTRYO]);
            }
            catch(NumberFormatException nfe) {
              ship.syukkaRyo = 0;
            }
            //----- 金額 -----
            try {
              Logger.info("MONEY:{}", sData[ShipmentConst.MONEY]);
              ship.kingaku = Double.parseDouble(sData[ShipmentConst.MONEY]);
            }
            catch(NumberFormatException nfe) {
              ship.kingaku = 0;
            }
            //----- 出荷情報を生成 -----
            ship.save();
          } // for (String linedata : lines)
          // トランザクション確定
          Ebean.commitTransaction();
        }
        catch (Exception e) {
          Logger.error(e.getMessage(), e);
          e.printStackTrace();
          // トランザクションロールバック
          Ebean.rollbackTransaction();
        }

        return ok(resultJson);
	}

	/**
	 * 区画IDを取得する
	 * @param pName
	 * @return
	 */
	private static double convertKukakuId(String pName) {
	  double result = 0;
	  Compartment kukaku = Compartment.find.where().eq("kukaku_name", pName).eq("farm_id", 2).findUnique();

	  if (kukaku != null) {
	    result = kukaku.kukakuId;
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
    List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(2);
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
	private static double convertNouhiId(String pName, double pCrop) {
    double result = 0;

    Nouhi nouhi = Nouhi.find.where().eq("nouhi_name", pName).eq("farm_id", 2).findUnique();
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
	 * @return
	 */
	private static int makingSakustart(double pKukakuId, java.util.Date pHashu) {

	  int result = 0;
	  Calendar cSakuStart = Calendar.getInstance();
	  cSakuStart.setTime(pHashu);
	  DateU.setTime(cSakuStart, DateU.TimeType.FROM);
    cSakuStart.add(Calendar.DAY_OF_MONTH, -5);

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
    wkd.accountId     = "o.bussann";

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
	 * 根水データを作成する
	 * @param pKukakuId
	 * @param pNemizu
	 * @param pTime
	 * @return
	 */
  private static int makingNemizu(double pKukakuId, java.util.Date pNemizu, int pTime) {

    int result = 0;

    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    int workId = 56;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = workId;
    wkd.kukakuId      = pKukakuId;

    wkd.kansuiMethod  = 3;
    wkd.kikiId        = 77;
    wkd.kansuiRyo     = pTime * 2.5;

    wkd.workDate      = new java.sql.Date(pNemizu.getTime());
    wkd.workTime      = pTime;
    wkd.accountId     = "o.bussann";

    wkd.workStartTime  = new java.sql.Timestamp(pNemizu.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pNemizu.getTime());

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
  private static int makingHashu(double pKukakuId, java.util.Date pHashu, String pHinsyu, String[] pHinsyuName, double pCropId) {

    int result = 0;
    Calendar cSakuStart = Calendar.getInstance();
    cSakuStart.setTime(pHashu);
    DateU.setTime(cSakuStart, DateU.TimeType.FROM);

    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    int workId = 6;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = workId;
    wkd.kukakuId      = pKukakuId;
    wkd.hinsyuId      = pHinsyu;
    wkd.kikiId        = 65;
    wkd.attachmentId  = 29;
    wkd.beltoId       = 9;

    switch ((int)pCropId) {
    case 1:   //水菜
      if (pHinsyuName.length > 1) {
        wkd.kabuma        = Double.parseDouble(pHinsyuName[2].replaceAll("ｃｍ", ""));
        wkd.joukan        = 0;
        wkd.jousu         = Double.parseDouble(pHinsyuName[1].replaceAll("条", ""));
        wkd.hukasa        = 0;
      }
      else {
        wkd.kabuma        = 0;
        wkd.joukan        = 0;
        wkd.jousu         = 0;
        wkd.hukasa        = 0;
      }
      break;
    case 2:   //小松菜
      if (pHinsyuName.length > 1) {
        wkd.kabuma        = Double.parseDouble(pHinsyuName[1].replaceAll("ｃｍ", ""));
        wkd.joukan        = 0;
        wkd.jousu         = 6;
        wkd.hukasa        = 0;
      }
      else {
        wkd.kabuma        = 0;
        wkd.joukan        = 0;
        wkd.jousu         = 0;
        wkd.hukasa        = 0;
      }
      break;
    case 3:   //ほうれん草
      if (pHinsyuName.length > 1) {
        wkd.kabuma        = Double.parseDouble(pHinsyuName[2].replaceAll("ｃｍ", ""));
        if (pHinsyuName[1].indexOf("条") > 0) {
          wkd.jousu         = Double.parseDouble(pHinsyuName[1].replaceAll("条", ""));
          wkd.joukan        = 0;
        }
        else {
          wkd.joukan        = Double.parseDouble(pHinsyuName[1].replaceAll("ｃｍ", ""));;
          wkd.jousu         = 0;
        }
        wkd.hukasa        = 0;
      }
      else {
        wkd.kabuma        = 0;
        wkd.joukan        = 0;
        wkd.jousu         = 0;
        wkd.hukasa        = 0;
      }
      break;
    case 12:  //ネギ
      if (pHinsyuName.length > 1) {
        wkd.kabuma        = Double.parseDouble(pHinsyuName[1].replaceAll("ｃｍ", ""));
        wkd.joukan        = 20;
        wkd.jousu         = 0;
        wkd.hukasa        = 20;
      }
      else {
        wkd.kabuma        = 0;
        wkd.joukan        = 0;
        wkd.jousu         = 0;
        wkd.hukasa        = 0;
      }
      break;
    case 18:   //露地ほうれん草
      if (pHinsyuName.length > 1) {
        wkd.kabuma        = Double.parseDouble(pHinsyuName[2].replaceAll("ｃｍ", ""));
        wkd.joukan        = Double.parseDouble(pHinsyuName[1].replaceAll("ｃｍ", ""));
        wkd.jousu         = 0;
        wkd.hukasa        = 0;
      }
      else {
        wkd.kabuma        = 0;
        wkd.joukan        = 0;
        wkd.jousu         = 0;
        wkd.hukasa        = 0;
      }
      break;

    default:
      break;
    }


    wkd.workDate      = new java.sql.Date(cSakuStart.getTimeInMillis());
    wkd.workTime      = 0;
    wkd.accountId     = "o.bussann";

    wkd.workStartTime  = new java.sql.Timestamp(cSakuStart.getTimeInMillis());
    wkd.workEndTime    = new java.sql.Timestamp(cSakuStart.getTimeInMillis());

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
  private static int makingKansui(double pKukakuId, java.util.Date pKansui, int pTime) {

    int result = 0;

    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    int workId = 9;

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = workId;
    wkd.kukakuId      = pKukakuId;

    wkd.kansuiMethod  = 3;
    wkd.kikiId        = 77;
    wkd.kansuiRyo     = pTime * 2.5;

    wkd.workDate      = new java.sql.Date(pKansui.getTime());
    wkd.workTime      = pTime;
    wkd.accountId     = "o.bussann";

    wkd.workStartTime  = new java.sql.Timestamp(pKansui.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pKansui.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    wkd.save();
    makingTimeLine(wkd);

//    Logger.info("[ NEMIZU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }


  private static int makingShodoku(double pKukakuId, double pCrop, java.util.Date pHashu, java.util.Date pSyodoku, String sanpu) {

    int result = 0;

    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    int     iWorkId = 0;
    double dKikiId  = 0;

    if ((pHashu.compareTo(pSyodoku) == -1) || (pHashu.compareTo(pSyodoku) == 0)) { //作業日が播種日以前
      iWorkId = 2;
      dKikiId = 65;
    }
    else {
      iWorkId = 8;
      dKikiId = 69;
    }

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = iWorkId;
    wkd.kukakuId      = pKukakuId;

    wkd.workDate      = new java.sql.Date(pSyodoku.getTime());
    wkd.workTime      = 0;
    wkd.accountId     = "o.bussann";

    wkd.workStartTime  = new java.sql.Timestamp(pSyodoku.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pSyodoku.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    if (sanpu == null || "".equals(sanpu)) {
      result = -1;
    }
    else {

      //----- 散布情報の生成 -----
      String[] sanpus = sanpu.split("、");
      int iSeq = 0;

      for (String sanpuName : sanpus) {
        double nouhiId = convertNouhiId(sanpuName, pCrop);
        if (nouhiId == 0) {
          //[暫定対策]
          Logger.error("[ NOUHI CONVERT ERROR! ] NAME = {}" , sanpuName);
          continue;
        }
        ObussanNouyaku on = ObussanNouyaku.find.where().eq("work_date", wkd.workDate).eq("nouhi_id", nouhiId).eq("kukaku_id", wkd.kukakuId).findUnique();
        iSeq++;

        WorkDiarySanpu wds = new WorkDiarySanpu();

        wds.workDiaryId = wkd.workDiaryId;
        wds.workDiarySequence   = iSeq;
        wds.sanpuMethod         = 3;
        wds.kikiId              = dKikiId;
        wds.attachmentId        = 29;
        wds.nouhiId             = nouhiId;
        if (on != null) {
          wds.bairitu             = on.bairitu;
          wds.sanpuryo            = on.sanpuryo;
        }
        else {
          wds.bairitu             = 0;
          wds.sanpuryo            = 0;
        }
        wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
        wds.motochoUpdate       = AgryeelConst.UpdateFlag.NONE;

        wds.save();
      }
      wkd.save();
      makingTimeLine(wkd);
    }
//    Logger.info("[ SHODOKU MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
  private static int makingHiryou(double pKukakuId, double pCrop, java.util.Date pHashu, java.util.Date pHiryo, String sanpu) {

    int result = 0;

    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    int     iWorkId = 0;
    double dKikiId  = 0;

    if ((pHashu.compareTo(pHiryo) == -1) || (pHashu.compareTo(pHiryo) == 0)) { //作業日が播種日以前
      iWorkId = 3;
      dKikiId = 64;
    }
    else {
      iWorkId = 10;
      dKikiId = 66;
    }

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;
    wkd.workId        = iWorkId;
    wkd.kukakuId      = pKukakuId;

    wkd.workDate      = new java.sql.Date(pHiryo.getTime());
    wkd.workTime      = 0;
    wkd.accountId     = "o.bussann";

    wkd.workStartTime  = new java.sql.Timestamp(pHiryo.getTime());
    wkd.workEndTime    = new java.sql.Timestamp(pHiryo.getTime());

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;
    wkd.workPlanFlag      = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
    wkd.workPlanUUID      = "";

    if (sanpu == null || "".equals(sanpu)) {
      result = -1;
    }
    else {

      //----- 散布情報の生成 -----
      String[] sanpus = sanpu.split(" ");
      int iSeq = 0;

      for (String sanpuName : sanpus) {
        String[] sanpuItem = sanpuName.split("：");
        double nouhiId = convertNouhiId(sanpuItem[0], pCrop);
        if (nouhiId == 0) {
          Logger.error("[ NOUHI CONVERT ERROR! ] NAME = {}" , sanpuItem[0]);
          continue;
        }
        iSeq++;

        WorkDiarySanpu wds = new WorkDiarySanpu();

        wds.workDiaryId = wkd.workDiaryId;
        wds.workDiarySequence   = iSeq;
        wds.sanpuMethod         = 3;
        wds.kikiId              = dKikiId;
        wds.attachmentId        = 29;
        wds.nouhiId             = nouhiId;
        wds.bairitu             = 1;
        if (sanpuItem.length > 1) {
          try {
            wds.sanpuryo            = Double.parseDouble( sanpuItem[1]) * 1000;
          }
          catch (Exception ex) {
            wds.sanpuryo            = 0;
          }
        }
        else {
          wds.sanpuryo            = 0;
        }
        wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
        wds.motochoUpdate       = AgryeelConst.UpdateFlag.NONE;

        wds.save();
      }
      wkd.save();
      makingTimeLine(wkd);
    }
//    Logger.info("[ HIRYO MAKE ] WORK DIARY ID = {}", workDiaryId);

    return result;

  }
  private static int makingShukaku(double pKukakuId, double pCrop, java.util.Date pHashu, java.util.Date pShukaku, String shukaku) {

    int result = 0;

    double workDiaryId    = 0;

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

    int iWorkId = 0;
    iWorkId = 11;

    String[] shukakus = shukaku.split(" ");

    for (String sShukaku: shukakus) {
      if (sShukaku == null || "".equals(sShukaku)) {
        Logger.info("[ SHUKAKU NONE ] DATA NONE.");
        continue;
      }
      Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
      workDiaryId     = sequence.sequenceValue;

      /* 共通項目 */
      wkd.workDiaryId   = workDiaryId;
      wkd.workId        = iWorkId;
      wkd.kukakuId      = pKukakuId;

      wkd.workDate      = new java.sql.Date(pShukaku.getTime());
      wkd.workTime      = 0;
      wkd.accountId     = "o.bussann";

      wkd.workStartTime  = new java.sql.Timestamp(pShukaku.getTime());
      wkd.workEndTime    = new java.sql.Timestamp(pShukaku.getTime());

      double sizeId = convertSizeId(sShukaku.substring(0, 1));
      if (sizeId == 0) {
        Logger.info("[ SHUKAKU SIZE ERROR ] SIZE={}.", sShukaku.substring(0, 1));
        continue;
      }

      wkd.shukakuRyo      = Double.parseDouble(sShukaku.substring(1));
      wkd.syukakuNisugata = 19;
      wkd.syukakuSitsu    = 3;
      wkd.syukakuSize     = (int)sizeId;

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
      wdd.syukakuKosu     = 1;                                                      //個数
      wdd.shukakuRyo      = wkd.shukakuRyo;                                         //収穫量
      wdd.save();

      makingTimeLine(wkd);

//      Logger.info("[ SHUKAKU MAKE ] WORK DIARY ID = {}", workDiaryId);

    }



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

  public static Result NouhiInput() {

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
            double nouhiId = convertNouhiId(nm, cropId);
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
  public static Result ReData() {
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
