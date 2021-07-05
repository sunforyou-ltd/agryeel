package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Account;
import models.Compartment;
import models.Nisugata;
import models.NisugataOfFarm;
import models.Sequence;
import models.Shitu;
import models.Size;
import models.TimeLine;
import models.Work;
import models.WorkDiaryDetail;
import param.RFarmConvertParm;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;
import util.StringU;

import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CompartmentStatusCompornent;
import compornent.MotochoCompornent;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】RushFarm専用コンバート
 * @author SunForYou
 *
 */
public class RFarmController extends Controller {

  //----- カラム位置 -----
  //----- 作業日 -----
  public static int WORKDATE = 0;
  //----- 担当者 -----
  public static int TANTO = 1;
  //----- 区画 -----
  public static int KUKAKU = 2;
  //----- 作業開始時間 -----
  public static int WORKSTART = 5;
  //----- 作業終了時間 -----
  public static int WORKEND = 6;
  //----- 作業時間-----
  public static int WORKTIME = 7;
  //----- 収穫 -----
  public static int SHUKAKU_START = 8;
  public static int SHUKAKU_LOOP = 8;
  public static int SHUKAKU_STEP = 5;

  /**
   * RushFarm専用収穫記録コンバート
   * @return
   */
	public static Result convertInput() {

        ObjectNode resultJson = Json.newObject();

        //----- CSVデータを開く -----
        String path = "c:\\temp\\rconvertdata.txt";
        Logger.info("");
        Logger.info("");
        Logger.info("[ DATA CONVERT STRAT ] FILE= {} ", path);
        int     read  = 0;
        int   commit  = 0;
        int    error  = 0;
        try{
          File inputFile  = new File(path);
          BufferedReader in     = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));

          String linedata = "";
          boolean head  = true;
          boolean head2 = true;
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
          try {

            while ((linedata = in.readLine()) != null) {
              if (head) { //ヘッダデータの場合
                //ヘッダデータ読み飛ばし
                head = false;
                continue;
              }
              if (head2) { //ヘッダデータ2の場合
                //ヘッダデータ読み飛ばし
                head2 = false;
                continue;
              }
              //収穫記録データの読み込み
              read++;
              Logger.info("--------------------------------------------------------------------------------------------------------------" );
              RFarmConvertParm rp = new RFarmConvertParm();
              String sData[] = linedata.split("\t");
              //----- 作業日の取得 -----
              if (sData[WORKDATE] == null || "".equals(sData[WORKDATE])) {
                Logger.error("[ WORKDATE DATA NULL ERROR! ]");
                error++;
                continue;
              }
              java.util.Date dtWorkDate;
              try {
                rp.workDate = new java.sql.Date(sdf.parse(sData[WORKDATE]).getTime());
              } catch (ParseException e) {
                Logger.error("[ WORKDATE CONVERT DATE ERROR! ]");
                e.printStackTrace();
                error++;
                continue;
              }
              //----- アカウントIDへのコンバート -----
              String accountId = convertAccountId(sData[TANTO]);
              if ("".equals(accountId)) {
                Logger.error("[ ACCOUNT CONVERT ERROR! ] NAME = {}" , sData[TANTO]);
                error++;
                continue;
              }
              rp.accountId = accountId;
              //----- 区画IDへのコンバート -----
              String kukaku1 = StringU.hankakuAlphabetToZenkakuAlphabet(sData[KUKAKU]);
              String kukaku2 = StringU.hankakuNumberToZenkakuNumber(kukaku1);
              double kukakuId = convertKukakuId(kukaku2);
              if (kukakuId == 0) {
                Logger.error("[ KUKAKU CONVERT ERROR! ] NAME = {}" , kukaku2);
                error++;
                continue;
              }
              rp.kukakuId = kukakuId;
              //----- 作業開始時間の生成 -----
              if (sData[WORKSTART] == null || "".equals(sData[WORKSTART])) {
                Logger.error("[ WORKSTART DATA NULL ERROR! ]");
                error++;
                continue;
              }
              String sStart[] = sData[WORKSTART].split(":");
              Calendar cal = Calendar.getInstance();
              cal.setTime(rp.workDate);
              cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(sStart[0]));
              cal.set(Calendar.MINUTE, Integer.valueOf(sStart[1]));
              cal.set(Calendar.SECOND, 0);
              rp.workStart = new java.sql.Timestamp(cal.getTimeInMillis());
              //----- 作業終了時間の生成 -----
              if (sData[WORKEND] == null || "".equals(sData[WORKEND])) {
                Logger.error("[ WORKEND DATA NULL ERROR! ]");
                error++;
                continue;
              }
              String sEnd[] = sData[WORKEND].split(":");
              cal = Calendar.getInstance();
              cal.setTime(rp.workDate);
              cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(sEnd[0]));
              cal.set(Calendar.MINUTE, Integer.valueOf(sEnd[1]));
              cal.set(Calendar.SECOND, 0);
              rp.workEnd = new java.sql.Timestamp(cal.getTimeInMillis());
              //----- 作業時間の生成 -----
              if (sData[WORKTIME] == null || "".equals(sData[WORKTIME])) {
                Logger.error("[ WORKTIME DATA NULL ERROR! ]");
                error++;
                continue;
              }
              rp.workTime = (int)Double.parseDouble(sData[WORKTIME]);
              //----- 荷姿数の取得-----
              rp.nisugataCount = getNisugataCount(sData);

              //----- 収穫データの作成 -----
              rp.data = sData;
              makingShukaku(rp);
              commit++;
            }
            in.close();
          } catch (IOException e) {
            e.printStackTrace();
            Logger.error(e.getMessage(),e );
          }
        } catch(FileNotFoundException e){
          e.printStackTrace();
          Logger.error(e.getMessage(),e);
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
          Logger.error(e.getMessage(),e);
        }
        Logger.info("[ DATA CONVERT END ] READ= {} COMMIT= {} ERROR= {}", read, commit, error);

        return ok(resultJson);
	}

  /**
   * アカウントIDを取得する
   * @param pName
   * @return
   */
  private static String convertAccountId(String pName) {
    String result = "";
    Account     account = Account.find.where().eq("acount_name", pName).eq("farm_id", 1).findUnique();
    if (account != null) {
      result = account.accountId;
    }
    return result;
  }
	/**
	 * 区画IDを取得する
	 * @param pName
	 * @return
	 */
	private static double convertKukakuId(String pName) {
	  double result = 0;
	  Compartment kukaku = Compartment.find.where().eq("kukaku_name", pName).eq("farm_id", 1).findUnique();

	  if (kukaku != null) {
	    result = kukaku.kukakuId;
	  }
	  return result;
	}
  /**
   * 荷姿IDを取得する
   * @param pName
   * @return
   */
  private static double convertNisugataId(String pName) {
    double result = 0;
    List<NisugataOfFarm> nfs = NisugataOfFarm.getNisugataOfFarm(1);
    List<Double> key = new ArrayList<Double>();
    for (NisugataOfFarm nf : nfs) {
      key.add(nf.nisugataId);
    }
    Nisugata nisugata = Nisugata.find.where().in("nisugata_id", key).eq("nisugata_name", pName).findUnique();
    if (nisugata != null) {
      result = nisugata.nisugataId;
    }
    return result;
  }
  /**
   * 荷姿数を数える
   * @param pName
   * @return
   */
  private static int getNisugataCount(String sData[]) {
    int result = 0;
    for (int i=0; i<SHUKAKU_LOOP; i++) {
      if ((sData.length - 1) < SHUKAKU_START + (SHUKAKU_STEP*i)) {
        break;
      }
      if (sData[SHUKAKU_START + (SHUKAKU_STEP*i)] == null || "".equals(sData[SHUKAKU_START + (SHUKAKU_STEP*i)])) {
        break;
      }
      result++;
    }
    return result;
  }

  private static int makingShukaku(RFarmConvertParm rp) {
    int result = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    models.WorkDiary wkd = new models.WorkDiary();
    Compartment ct = Compartment.getCompartmentInfo(rp.kukakuId);

    double workDiaryId    = 0;
    double shukakuryo     = 0;
    int iWorkId           = 0;
    iWorkId               = 11;

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    workDiaryId     = sequence.sequenceValue;

    for (int i=0; i< rp.nisugataCount; i++) {

      int iNisugata   = SHUKAKU_START + (SHUKAKU_STEP*i);
      int iKosu       = SHUKAKU_START + (SHUKAKU_STEP*i) + 1;
      int iShukakuryo = SHUKAKU_START + (SHUKAKU_STEP*i) + 3;

      String nisugata = rp.data[iNisugata].replaceAll("\"", "");
      double nisugataId = convertNisugataId(nisugata);
      if (nisugataId == 0) {
        Logger.error("[ NISUGATA CONVERT ERROR! NAME = {}]", rp.data[iNisugata]);
        result = -1;
        break;
      }
      if (rp.data[iShukakuryo] == null || "".equals(rp.data[iShukakuryo])) {
        Logger.error("[ SHUKAKURYO DATA NONE ERROR!]");
        result = -1;
        break;
      }

      WorkDiaryDetail wdd = new WorkDiaryDetail();

      wdd.workDiaryId         = workDiaryId;                                          //作業記録ＩＤ
      wdd.workDiarySequence   = (i + 1);                                              //作業記録シーケンス

      wdd.syukakuNisugata = (int)nisugataId;                                          //荷姿
      wdd.syukakuSitsu    = 1;                                                        //質
      wdd.syukakuSize     = 1;                                                        //サイズ
      wdd.syukakuKosu     = Double.valueOf(rp.data[iKosu]);                           //個数
      wdd.shukakuRyo      = Double.valueOf(rp.data[iShukakuryo]);                     //収穫量
      shukakuryo         += wdd.shukakuRyo;

      wdd.save();

    }
    /* 共通項目 */
    wkd.workDiaryId     = workDiaryId;
    wkd.workId          = iWorkId;
    wkd.kukakuId        = rp.kukakuId;

    wkd.workDate        = rp.workDate;
    wkd.workTime        = rp.workTime;
    wkd.accountId       = rp.accountId;

    wkd.workStartTime   = rp.workStart;
    wkd.workEndTime     = rp.workEnd;

    wkd.shukakuRyo      = shukakuryo;
    wkd.syukakuNisugata = 0;
    wkd.syukakuSitsu    = 0;
    wkd.syukakuSize     = 0;

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;

    wkd.save();

    Logger.info("[ SHUKAKU MAKE ] WORKDIARYID = {} DATE={} KUKAKU={} NISUGATA={} SHUKAKURYO={}", workDiaryId, sdf.format(wkd.workDate), wkd.kukakuId, wkd.syukakuNisugata, wkd.shukakuRyo);

    makingTimeLine(wkd);

    return result;
  }
  private static void makingTimeLine(models.WorkDiary workDiary) {

    Work work = Work.find.where().eq("work_id", workDiary.workId).findUnique();                       //作業情報モデルの取得


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
    case AgryeelConst.WorkTemplate.SHUKAKU:
      List<WorkDiaryDetail> wdds = WorkDiaryDetail.getWorkDiaryDetailList(workDiary.workDiaryId);
      int idx = 0;
      for (WorkDiaryDetail wdd : wdds) {
        if (wdd.syukakuNisugata == 0
            || (wdd.syukakuSitsu == 0)
            || (wdd.syukakuSize == 0)
            || (wdd.syukakuKosu == 0)
            || (wdd.shukakuRyo == 0)
            ) {
          continue;
        }
        idx++;
        timeLine.message       += "<荷姿" + idx + "> "        + Nisugata.getNisugataName(wdd.syukakuNisugata) + "<br>";
        timeLine.message       += "<質" + idx + "> "         + Shitu.getShituName(wdd.syukakuSitsu) + "<br>";
        timeLine.message       += "<サイズ" + idx + "> "       + Size.getSizeName(wdd.syukakuSize) + "<br>";
        timeLine.message       += "<個数" + idx + "> "        + wdd.syukakuKosu + "個" + "<br>";
        timeLine.message       += "<収穫量" + idx + "> "      + wdd.shukakuRyo + "Kg" + "<br>";

      }
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

    timeLine.save();                                                //タイムラインを追加

    /* 元帳照会を更新する */
    MotochoCompornent motochoCompornent = new MotochoCompornent(workDiary.kukakuId);
    motochoCompornent.make();

    /* 区画状況照会を更新する */
    CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(workDiary.kukakuId, workDiary.workId);
    compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);
  }

}
