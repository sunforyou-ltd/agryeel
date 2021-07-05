package batch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.avaje.ebean.Ebean;

import compornent.CompartmentStatusCompornent;
import compornent.FieldComprtnent;
import compornent.MotochoCompornent;
import consts.AgryeelConst;

import models.AutoKansuiDetail;
import models.Common;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Kiki;
import models.Sequence;
import models.TimeLine;
import models.Work;
import play.Logger;
import play.api.Mode;
import play.api.Play;
import util.DateU;

public class AutoKansui {

  /**
   * メイン処理
   * @param args
   */
  public static void main(String args[]) {
    Logger.info("---------- AutoKansui START ----------");
    Play.start(new play.api.DefaultApplication(new java.io.File("."),  StatusCheck.class.getClassLoader(), null, Mode.Prod()));

    while (true) {

      autoKansui();

      try {
        Thread.sleep(1000 * 60);
      } catch (InterruptedException e) {
        e.printStackTrace();
        break;
      }
    }
    Play.stop();
    Logger.info("---------- AutoKansui END ----------");
  }

  public static void autoKansui() {

    Logger.info("[ autoKansui ] Auto Kansui Status Check.");
    List<models.AutoKansui> aks = models.AutoKansui.find.orderBy("auto_kansui_id").findList();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    java.util.Date system = new Date(Calendar.getInstance().getTime().getTime());

    for (models.AutoKansui ak : aks) {
      Logger.info("[ autoKansui ] ------------------------------ ID={} ------------------------------", ak.autoKansuiId);
      List<models.AutoKansuiDetail> akds = AutoKansuiDetail.find.orderBy("auto_kansui_id, auto_kansui_seq").findList();
      boolean bAllEnd = true;

      for(models.AutoKansuiDetail akd : akds) {
        if (akd.autoKansuiStatus == AgryeelConst.AutoKansuiStatus.NONE) {
          Logger.info("[ autoKansui ] KUKAKU:{} --> AUTO KANSUI NO TARGET.", akd.kukakuId);
          continue;
        }
        if (akd.autoKansuiStatus == AgryeelConst.AutoKansuiStatus.END) {
          Logger.info("[ autoKansui ] KUKAKU:{} --> AUTO KANSUI ENDED.", akd.kukakuId);
          continue;
        }
        Compartment ct = Compartment.getCompartmentInfo(akd.kukakuId);
        if (ct == null) {
          Logger.info("[ autoKansui ] KUKAKU:{} --> COMPARTMENT NONE.", akd.kukakuId);
          continue;
        }
        bAllEnd = false;
        java.util.Date start = new Date(akd.autoKansuiStartTime.getTime());
        java.util.Date end   = new Date(akd.autoKansuiEndTime.getTime());
        Logger.info("[ autoKansui ] START:{} END:{} SYSTEM:{}. ", sdf.format(start), sdf.format(end), sdf.format(system));
        if (!(start.after(system) || end.before(system))) {
          if (akd.autoKansuiStatus == AgryeelConst.AutoKansuiStatus.WAITING) {
            Logger.info("[ autoKansui ] KUKAKU:{} --> AUTO KANSUI START.", akd.kukakuId);
            akd.autoKansuiStatus = AgryeelConst.AutoKansuiStatus.NOW;
            akd.kansuiTime       = 0;
            akd.kansuiRyo        = 0;
          }
          else if (akd.autoKansuiStatus == AgryeelConst.AutoKansuiStatus.NOW) {
            Logger.info("[ autoKansui ] KUKAKU:{} --> AUTO KANSUI CONTINUE.", akd.kukakuId);
            akd.kansuiTime++;
            akd.kansuiRyo        += ct.kansuiRyo;
          }
        }
        else {
          if (akd.autoKansuiStatus == AgryeelConst.AutoKansuiStatus.NOW) {
            Logger.info("[ autoKansui ] KUKAKU:{} --> AUTO KANSUI END.", akd.kukakuId);
            akd.autoKansuiStatus = AgryeelConst.AutoKansuiStatus.END;
            akd.kansuiTime++;
            akd.kansuiRyo       += ct.kansuiRyo;
            makeWorkDiary(ak, akd);
          }
        }
        akd.update();
      }
      if (bAllEnd) { //全自動潅水が完了している場合
        Ebean.createSqlUpdate("DELETE FROM auto_kansui_detail WHERE auto_kansui_id = :autoKansuiId")
        .setParameter("autoKansuiId", ak.autoKansuiId).execute();
        Ebean.createSqlUpdate("DELETE FROM auto_kansui WHERE auto_kansui_id = :autoKansuiId")
        .setParameter("autoKansuiId", ak.autoKansuiId).execute();
      }
    }
  }
  public static void makeWorkDiary(models.AutoKansui ak, models.AutoKansuiDetail akd) {

    SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
    models.WorkDiary wkd = new models.WorkDiary();

    Compartment ct = Compartment.getCompartmentInfo(akd.kukakuId);

    Sequence sequence   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);              //最新シーケンス値の取得
    double workDiaryId     = sequence.sequenceValue;

    int workId = (int)ak.workId;
    Work wk = Work.getWork(ak.workId);

    /* 共通項目 */
    wkd.workDiaryId   = workDiaryId;                                        //作業記録ID
    wkd.workId      = workId;                                               //作業ID
    wkd.kukakuId    = akd.kukakuId;                                              //区画ID

    wkd.workDate  = ak.workDate;

    wkd.workTime    = akd.kansuiTime;
    wkd.accountId   = "AUTOKANSUI" + akd.kukakuId;

    wkd.workStartTime  = akd.autoKansuiStartTime;
    wkd.workEndTime    = akd.autoKansuiEndTime;

    wkd.kansuiMethod  = akd.kansuiMethod;
    wkd.kikiId        = akd.kikiId;
    wkd.kansuiRyo     = akd.kansuiRyo;

    Logger.info("[ AUTO KANSUI WORKDIARY NEW ] KUKAKUID:{} KUKAKUNAME:{} WORKDIARYID:{}", ct.kukakuId, ct.kukakuName, workDiaryId);

    //区画IDから区画状況情報を取得する
    CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(wkd.kukakuId);
    if (compartmentStatusData != null) {
      CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();
      if (compartmentStatusData.katadukeDate != null) {
        Date katadukeDate = DateUtils.truncate(compartmentStatusData.katadukeDate, Calendar.DAY_OF_MONTH);
        Date workDate = DateUtils.truncate(wkd.workDate, Calendar.DAY_OF_MONTH);
        if (!workDate.before(katadukeDate)) {
          if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
            cws.workEndId = String.valueOf(wk.workId);
          }
          else {
            String workEndId = cws.workEndId;
            boolean endidExists = false;
            if (workEndId != null && !"".equals(workEndId)) {
              String[] endIds = workEndId.split(",");
              for (String endId : endIds) {
                if (Double.parseDouble(endId) == wk.workId) {
                  endidExists = true;
                  break;
                }
              }
              if (endidExists == false) {
                cws.workEndId += "," + String.valueOf(wk.workId);
              }
            }
            else {
              cws.workEndId = String.valueOf(wk.workId);
            }
          }
        }
        else if (workDate.compareTo(katadukeDate) == 0) {
          if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
            cws.workEndId = String.valueOf(wk.workId);
          }
        }
      }
      else {
        if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
          cws.workEndId = String.valueOf(wk.workId);
        }
      }
      cws.update();
    }

    wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;                             //区画状況照会反映フラグ
    wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;                             //元帳照会反映フラグ

    wkd.save();

    /* -- タイムラインを作成する -- */
    TimeLine timeLine = new TimeLine();                                       //タイムラインモデルの生成
    sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.TIMELINEID);               //最新シーケンス値の取得

    timeLine.timeLineId       = sequence.sequenceValue;                           //タイムラインID
    timeLine.updateTime       = DateU.getSystemTimeStamp();

    timeLine.message        = "【" + wk.workName + "情報】<br>";
    timeLine.message       += "<潅水方法> " + Common.GetCommonValue(Common.ConstClass.KANSUI, wkd.kansuiMethod) + "<br>";
    timeLine.message       += "<機器> " + Kiki.getKikiName(wkd.kikiId) + "<br>";
    timeLine.message       += "<潅水量> " + wkd.kansuiRyo + "L" + "<br>";

    //作業時間をメッセージに追加する
    SimpleDateFormat sdf2 = new SimpleDateFormat("yy/MM/dd HH:mm");
    if (wkd.workStartTime != null) {
      timeLine.message += "【 開始時間 】 " + sdf2.format(wkd.workStartTime);
    }
    if (wkd.workEndTime != null) {
      timeLine.message += "【 終了時間 】 " + sdf2.format(wkd.workEndTime);
    }
    timeLine.workDiaryId      = wkd.workDiaryId;                      //作業記録ID
    timeLine.timeLineColor    = wk.workColor;                         //タイムラインカラー
    timeLine.workId           = wk.workId;                            //作業ID
    timeLine.workName         = wk.workName;                          //作業名
    timeLine.workDate         = wkd.workDate;                         //作業日
    timeLine.kukakuId         = ct.kukakuId;                          //区画ID
    timeLine.kukakuName       = ct.kukakuName;                        //区画名
    timeLine.accountId        = wkd.accountId;                        //アカウントID
    timeLine.accountName      = "自動潅水";                             //アカウント名
    timeLine.farmId           = ct.farmId;                            //農場ID

    timeLine.save();                                                  //タイムラインを追加

    /* 元帳照会を更新する */
    MotochoCompornent motochoCompornent = new MotochoCompornent(wkd.kukakuId);
    motochoCompornent.make();

    /* 区画状況照会を更新する */
    CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(wkd.kukakuId, wkd.workId);
    compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);

  }
}
