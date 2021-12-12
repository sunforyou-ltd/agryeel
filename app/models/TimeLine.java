package models;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;
import util.DateU;
import util.ListrU;

import com.avaje.ebean.Expr;
import com.avaje.ebean.annotation.CreatedTimestamp;

import consts.AgryeelConst;

import com.avaje.ebean.Expr;

import consts.AgryeelConst;

@Entity
/**
 * 【AGRYEEL】タイムライン情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class TimeLine extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -8184948097382924533L;
    /**
     * タイムラインID
     */
    public double timeLineId;
    /**
     * 更新日時
     */
    public Timestamp updateTime;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * メッセージ
     */
    public String message;
    /**
     * 作業記録ID
     */
    public double workDiaryId;
    /**
     * タイムラインカラー
     */
    public String timeLineColor;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * 作業名
     */
    public String workName;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 区画名
     */
    public String kukakuName;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 氏名
     */
    public String accountName;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 作業開始時間
     */
    //Ebeanで時間の更新が不可能な為、@CreateTimeStampを削除
    public Timestamp workStartTime;
    /**
     * 作業終了時間
     */
    //Ebeanで時間の更新が不可能な為、@CreateTimeStampを削除
    public Timestamp workEndTime;
    /**
     * 歩数
     */
    public long numberOfSteps;
    /**
     * 距離
     */
    public double distance;
    /**
     * カロリー
     */
    public int calorie;
    /**
     * 心拍数
     */
    public int heartRate;
    /**
     * 作業計画フラグ
     */
    public int workPlanFlag;
    /**
     * 作業計画作成ＵＵＩＤ
     */
    public String workPlanUUID;
    /**
     * 同一日内指示番号
     */
    public long uuidOfDay;

    public static Finder<Long, TimeLine> find = new Finder<Long, TimeLine>(Long.class, TimeLine.class);

    /**
     * 開始時刻を設定する（Ebean更新対策）
     * @param pWorkStartTime
     */
    public void setWorkStartTime(Timestamp pWorkStartTime) {
      this.workStartTime = pWorkStartTime;
    }
    /**
     * 終了時刻を設定する（Ebean更新対策）
     * @param pWorkEndTime
     */
    public void setWorkEndTime(Timestamp pWorkEndTime) {
      this.workEndTime = pWorkEndTime;
    }

    /**
     * 対象期間のタイムラインを取得する
     * @param startDate
     * @param endDate
     * @returnaccountidaccountidaccountid
     */
    public static List<TimeLine> getTimeLineOfFarm(double farmId, Timestamp startDate,Timestamp endDate) {

      List<TimeLine> aryTimeLine = TimeLine.find.where().eq("farm_id", farmId).between("work_start_time", startDate, endDate).orderBy("work_date desc, work_id desc, update_time desc").findList();

      return aryTimeLine;

    }
    /**
     * 対象期間のタイムラインを取得する
     * @param startDate
     * @param endDate
     * @returnaccountidaccountidaccountid
     */
    public static List<TimeLine> getTimeLineOfRange(double kukakuId, java.sql.Timestamp startDate,java.sql.Timestamp endDate) {

      DateU.putTimeLog(startDate);
      DateU.putTimeLog(endDate);

    	List<TimeLine> aryTimeLine = TimeLine.find.where().eq("kukaku_id", kukakuId).between("work_start_time", startDate, endDate).orderBy("work_date desc, work_start_time desc").findList();

    	return aryTimeLine;

    }
    public static List<TimeLine> getTimeLineOfAccount(String accountid, double farmId, Timestamp startDate,Timestamp endDate) {

      List<TimeLine> aryTimeLine;
      if (accountid.equals(AgryeelConst.SpecialAccount.ALLACOUNT)) {
        aryTimeLine = TimeLine.find.where().between("work_start_time", startDate, endDate).eq("farm_id", farmId).orderBy("work_date desc, update_time desc").findList();
      }
      else {
        String[] accounts = accountid.split(",");
        if (accounts.length > 1) {
          List<String> accountKeys = new ArrayList<String>();
          for (String key : accounts) {
            accountKeys.add(key);
          }
          aryTimeLine = TimeLine.find.where().disjunction().add(Expr.in("account_id", accountKeys)).add(Expr.eq("account_id", "")).endJunction().eq("farm_id", farmId).between("work_start_time", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
        else {
          aryTimeLine = TimeLine.find.where().eq("account_id", accountid).between("work_start_time", startDate, endDate).orderBy("work_date desc, update_time desc").findList();
        }
      }

      return aryTimeLine;

    }
    public static List<TimeLine> getTimeLineOfSearch(String accountid, String workid, String kukakuid, double farmId, Date startDate,Date endDate) {

      List<TimeLine> aryTimeLine;
      List<String> accountKeys  = ListrU.makeInKeyString(accountid);
      if (accountKeys.size() == 0) {
        List<Account> accounts = models.Account.getAccountOfFarm(farmId);
        for (Account account : accounts) {
          accountKeys.add(account.accountId);
        }
      }
      List<Double> workKeys     = ListrU.makeInKeyDouble(workid);
      if (workKeys.size() == 0) {
        List<Work> works = models.Work.getWorkOfBaseFarm(farmId);
        for (Work work : works) {
          workKeys.add(work.workId);
        }
      }
      List<Double> kukakuKeys   = ListrU.makeInKeyDouble(kukakuid);
      if (kukakuKeys.size() == 0) {
        List<Compartment> kukakus = models.Compartment.getCompartmentOfFarm(farmId);
        for (Compartment kukaku : kukakus) {
          kukakuKeys.add(kukaku.kukakuId);
        }
      }
      aryTimeLine = TimeLine.find.where().eq("farm_id", farmId).between("work_date", startDate, endDate).disjunction().add(Expr.in("account_id", accountKeys)).add(Expr.eq("account_id", "")).endJunction()
                                          .in("work_id", workKeys).in("kukaku_id", kukakuKeys).orderBy("work_date desc, update_time desc").findList();

      return aryTimeLine;

    }
    public static void setTimeLineData( models.TimeLine timeLine, models.WorkDiary workDiary, models.Work work) {

      final DecimalFormat df = new DecimalFormat("#,##0.00");

      Sequence sequence       = Sequence.GetSequenceValue(Sequence.SequenceIdConst.TIMELINEID);                         //最新シーケンス値の取得
      Compartment compartment = Compartment.find.where().eq("kukaku_id", workDiary.kukakuId).findUnique();              //区画情報モデルの取得
      Account account         = Account.find.where().eq("account_id", workDiary.accountId).findUnique();                //アカウント情報モデルの取得

      timeLine.timeLineId       = sequence.sequenceValue;                                                                   //タイムラインID

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

          timeLine.message        +=  nouhi.nouhiName + "&nbsp;&nbsp;" + nouhi.bairitu + "倍&nbsp;&nbsp;" + df.format(wds.sanpuryo * nouhi.getUnitHosei()) + unit + sanpuName + "<br>";
          timeLine.message        +=  "--------------------------------------------------<br>";

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

      timeLine.workDiaryId    = workDiary.workDiaryId;                          //作業記録ID
      timeLine.timeLineColor  = work.workColor;                                 //タイムラインカラー
      timeLine.workId         = work.workId;                                    //作業ID
      timeLine.workName       = work.workName;                                  //作業名
      timeLine.workDate       = workDiary.workDate;                             //作業日
      timeLine.numberOfSteps  = workDiary.numberOfSteps;                        //歩数
      timeLine.distance       = workDiary.distance;                             //距離
      timeLine.calorie        = workDiary.calorie;                              //カロリー
      timeLine.heartRate      = workDiary.heartRate;                            //心拍数
      timeLine.kukakuId       = compartment.kukakuId;                           //区画ID
      timeLine.kukakuName     = compartment.kukakuName;                         //区画名
      timeLine.accountId      = account.accountId;                              //アカウントID
      timeLine.accountName    = account.acountName;                             //アカウント名
      timeLine.farmId         = account.farmId;                                 //農場ID
      timeLine.workStartTime  = workDiary.workStartTime;                        //作業開始時間
      timeLine.workEndTime    = workDiary.workEndTime;                          //作業終了時間
      timeLine.workPlanFlag   = workDiary.workPlanFlag;                         //作業計画フラグ

    }
}
