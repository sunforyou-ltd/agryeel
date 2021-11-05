package compornent;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import models.FarmStatus;
import models.Hinsyu;
import models.IkubyoDiary;
import models.IkubyoDiarySanpu;
import models.MotochoBase;
import models.Nouhi;
import models.Work;
import models.WorkDiary;
import models.WorkDiaryDetail;
import models.Youki;

import org.apache.commons.lang3.time.DateUtils;

import util.DateU;
import consts.AgryeelConst;

public class NaeMotochoCompornent implements AgryellInterface{


    String naeNo = "";					//苗No
    public MotochoBase lastMotochoBase;	//最終元帳基本情報
    public double lastWorkId = 0;       //最終作業ID

    /**
     * コンストラクタ
     * @param kukakuId
     */
    public NaeMotochoCompornent(String naeNo) {

        this.naeNo = naeNo;

    }

    public void make() {

      List<IkubyoDiary> aryIkubyoDiary = IkubyoDiary.getIkubyoDiary(this.naeNo);
      int oldYear            = 0;
      java.sql.Date nullDate = DateU.GetNullDate();
      java.sql.Date oldDate  = null;

      MotochoBase motochoBase = new MotochoBase();
      MotochoBase oldMotochoBase = null;
      Work oldWork = null;
      Youki youki = null;
      double suryo = 0;
      double kosu = 0;
      IkubyoDiary oldIkubyoDiary = null;

      /*------------------------------------------------------------------------------------------------------------*/
      /* 初期化                                                                                                     */
      /*------------------------------------------------------------------------------------------------------------*/
      motochoBase.hashuDate = nullDate;

      /*------------------------------------------------------------------------------------------------------------*/
      /* 生産者ステータスを取得
      /*------------------------------------------------------------------------------------------------------------*/
      String[] naeNoSp = this.naeNo.split("-");
      FarmStatus fs = FarmStatus.getFarmStatus(Double.parseDouble(naeNoSp[0]));

      for (IkubyoDiary ikubyoDiary : aryIkubyoDiary) {

        Work work = Work.getWork(ikubyoDiary.workId);
        oldDate   = ikubyoDiary.workDate;
        this.lastWorkId = ikubyoDiary.workId;
        if (work.workTemplateId != AgryeelConst.WorkTemplate.NAEHASHUIK) {
          continue;
        }

        /*------------------------------------------------------------------------------------------------------------*/
        /* 元帳照会基本の作成                                                                                         */
        /*------------------------------------------------------------------------------------------------------------*/
        motochoBase = new MotochoBase();
        motochoBase.kukakuName   = this.naeNo;						//苗Noとして使用
        motochoBase.workStartDay = ikubyoDiary.workStartTime;
        motochoBase.hinsyuId     = ikubyoDiary.hinsyuId;
        motochoBase.hinsyuName   = "";
        motochoBase.hinsyuName   = Hinsyu.getMultiHinsyuName(motochoBase.hinsyuId);
        motochoBase.cropId       = Hinsyu.getMultiHinsyuCropId(motochoBase.hinsyuId);
        motochoBase.hashuDate    = ikubyoDiary.workDate;
        motochoBase.nowPredictionShukaku = ikubyoDiary.naeSuryo;	//現在庫数として使用
        motochoBase.predictionShukakuRyo = ikubyoDiary.kosu;		//在庫個数として使用

        oldWork = work;
        oldIkubyoDiary = ikubyoDiary;

      }

      /*------------------------------------------------------------------------------------------------------------*/
      /* 元帳照会基本の反映（現在）                                                                                 */
      /*------------------------------------------------------------------------------------------------------------*/
      motochoBase.workEndDay = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
      if (motochoBase.workStartDay != null) {
        setBaseInfo(motochoBase);
        makeMotochoNouyaku(motochoBase);
        makeMotochoHiryo(motochoBase);
      }else{
        motochoBase.workStartDay = motochoBase.workEndDay;
      }

      this.lastMotochoBase = motochoBase;

    }

    private void setBaseInfo(MotochoBase motochoBase) {

      java.sql.Date nullDate = DateU.GetNullDate();
      double suryo;
      double kosu;

      List<IkubyoDiary> aryIkubyo = IkubyoDiary.getIkubyoDiaryOfWork(this.naeNo, motochoBase.workStartDay, motochoBase.workEndDay);
      /*------------------------------------------------------------------------------------------------------------*/
      /* 潅水情報の反映                                                                                                                                                                                                                                                */
      /*------------------------------------------------------------------------------------------------------------*/
      if (aryIkubyo.size() > 0) {
          for (IkubyoDiary ikubyoDiary : aryIkubyo) {
            /* 作業開始以降のみ有効とする */
            if (motochoBase.workStartDay.compareTo(ikubyoDiary.workDate) > 0) {
              continue;
            }
            Work work = Work.getWork(ikubyoDiary.workId);
            if (work.workTemplateId != AgryeelConst.WorkTemplate.KANSUI) {
              continue;
            }

            motochoBase.finalKansuiDate          = ikubyoDiary.workDate;
            motochoBase.totalKansuiCount++;
            motochoBase.totalKansuiNumber       += ikubyoDiary.kansuiRyo;
          }
      }

      /*------------------------------------------------------------------------------------------------------------*/
      /* 生育日数の算出                                                                                             */
      /*------------------------------------------------------------------------------------------------------------*/
      if ((motochoBase.hashuDate != null)
          && (motochoBase.hashuDate.compareTo(nullDate) != 0)) {  //播種日が未設定の場合
          Date hashuDate = DateUtils.truncate(motochoBase.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(new java.sql.Date(Calendar.getInstance().getTime().getTime()), Calendar.DAY_OF_MONTH);

          motochoBase.seiikuDayCount = (int)DateU.GetDiffDate(hashuDate, systemDate);
      }

      /*------------------------------------------------------------------------------------------------------------*/
      /* 廃棄情報の反映                                                                                             */
      /*------------------------------------------------------------------------------------------------------------*/
      if (aryIkubyo.size() > 0) {
          for (IkubyoDiary ikubyoDiary : aryIkubyo) {
            /* 作業開始以降のみ有効とする */
            if (motochoBase.workStartDay.compareTo(ikubyoDiary.workDate) > 0) {
              continue;
            }
            Work work = Work.getWork(ikubyoDiary.workId);
            if (work.workTemplateId != AgryeelConst.WorkTemplate.HAIKIIK) {
              continue;
            }

            motochoBase.nowPredictionShukaku -= ikubyoDiary.naeSuryo;	//現在庫数として使用
            motochoBase.predictionShukakuRyo -= ikubyoDiary.haikiRyo;	//在庫個数として使用
          }
      }

      /*------------------------------------------------------------------------------------------------------------*/
      /* 苗播種情報の反映                                                                                             */
      /*------------------------------------------------------------------------------------------------------------*/
      List<WorkDiaryDetail> aryDetail = WorkDiaryDetail.find.where().eq("nae_no", this.naeNo).findList();
      if (aryDetail.size() > 0) {
        for (WorkDiaryDetail detailDiary : aryDetail) {
          WorkDiary workDiary = WorkDiary.getWorkDiaryById(detailDiary.workDiaryId);
          /* 作業開始以降のみ有効とする */
          if (motochoBase.workStartDay.compareTo(workDiary.workDate) > 0) {
            continue;
          }
          Work work = Work.getWork(workDiary.workId);
          if (work.workTemplateId != AgryeelConst.WorkTemplate.TEISHOKU) {
            continue;
          }

          /* 対象苗の容器取得 */
          Youki youki = null;
          for (IkubyoDiary ikubyoDiary : aryIkubyo) {
            work = Work.getWork(ikubyoDiary.workId);
            if (work.workTemplateId != AgryeelConst.WorkTemplate.NAEHASHUIK) {
              continue;
            }
            youki = Youki.getYoukiInfo(ikubyoDiary.youkiId);
          }

          if (youki != null) {
            suryo = 1 / youki.kosu;
            suryo = suryo * detailDiary.kosu;
            motochoBase.nowPredictionShukaku -= suryo;				//現在庫数として使用
            motochoBase.predictionShukakuRyo -= detailDiary.kosu;	//在庫個数として使用
          }
        }
      }

    }

    synchronized private void makeMotochoNouyaku(MotochoBase motochoBase) {

      /*------------------------------------------------------------------------------------------------------------*/
      /* 元帳農薬情報の生成		                                                                                          */
      /*------------------------------------------------------------------------------------------------------------*/
      Hashtable<Double, Double> nouyakuNoTable 	= new Hashtable<Double, Double>();	//農薬番号管理テーブル
      double nouyakuNo						 	= 0;								//農薬番号
      java.sql.Date oldDate = null;

      List<IkubyoDiary> aryIkubyo = IkubyoDiary.getIkubyoDiaryOfWork(this.naeNo, motochoBase.workStartDay, motochoBase.workEndDay);

      for (IkubyoDiary ikubyoDiary : aryIkubyo) {

        /* 作業開始以前の作業は無効とする */
        if (motochoBase.workStartDay.compareTo(ikubyoDiary.workDate) > 0) {
          continue;
        }

        Work work = Work.getWork(ikubyoDiary.workId);
        if (work.workTemplateId != AgryeelConst.WorkTemplate.SANPU) {
          continue;
        }

        //該育苗記録の散布情報を取得する
        List<IkubyoDiarySanpu> ikubyoDiarySanpuList = IkubyoDiarySanpu.find.where().eq("ikubyo_diary_id", ikubyoDiary.ikubyoDiaryId).orderBy("nouhi_id asc").findList();

        for (IkubyoDiarySanpu ikubyoDiarySanpuData : ikubyoDiarySanpuList) {
            Nouhi nouhi = Nouhi.find.where().eq("nouhi_id", ikubyoDiarySanpuData.nouhiId).findUnique();   /* 農肥情報を取得 */
            if ((nouhi == null) || (nouhi.nouhiKind != AgryeelConst.NouhiKind.NOUYAKU)) {
              continue;
            }
            if (oldDate == null || oldDate.compareTo(ikubyoDiary.workDate) != 0) {
              motochoBase.totalDisinfectionCount++;                               //消毒回数
            }
            motochoBase.finalDisinfectionDate = ikubyoDiary.workDate;             //最終消毒日
            motochoBase.totalDisinfectionNumber += ikubyoDiarySanpuData.sanpuryo; //消毒量
            oldDate = ikubyoDiary.workDate;
        }
      }
    }

    synchronized private void makeMotochoHiryo(MotochoBase motochoBase) {

      /*------------------------------------------------------------------------------------------------------------*/
      /* 元帳肥料情報の生成		                                                                                  */
      /*------------------------------------------------------------------------------------------------------------*/
      Hashtable<Double, Double> hiryoNoTable 	= new Hashtable<Double, Double>();		//肥料番号管理テーブル
      double hiryoNo						 	= 0;									//肥料番号
      java.sql.Date oldDate = null;

      List<IkubyoDiary> aryIkubyo = IkubyoDiary.getIkubyoDiaryOfWork(this.naeNo, motochoBase.workStartDay, motochoBase.workEndDay);

      for (IkubyoDiary ikubyoDiary : aryIkubyo) {

        Work work = Work.getWork(ikubyoDiary.workId);
        if (work.workTemplateId != AgryeelConst.WorkTemplate.SANPU) {
          continue;
        }

        //該育苗記録の散布情報を取得する
        List<IkubyoDiarySanpu> ikubyoDiarySanpuList = IkubyoDiarySanpu.find.where().eq("ikubyo_diary_id", ikubyoDiary.ikubyoDiaryId).orderBy("nouhi_id asc").findList();

        for (IkubyoDiarySanpu ikubyoDiarySanpuData : ikubyoDiarySanpuList) {
          Nouhi nouhi = Nouhi.find.where().eq("nouhi_id", ikubyoDiarySanpuData.nouhiId).findUnique();         /* 農肥情報を取得 */
          if ((nouhi == null) || (nouhi.nouhiKind != AgryeelConst.NouhiKind.HIRYO)) {
            continue;
          }

          if (motochoBase.hashuDate != null && motochoBase.hashuDate.compareTo(ikubyoDiary.workDate) <= 0) {
            motochoBase.finalTuihiDate = ikubyoDiary.workDate;              //最終追肥日
            if (oldDate == null || oldDate.compareTo(ikubyoDiary.workDate) != 0) {
              motochoBase.totalTuihiCount++;                              //追肥回数
            }
            motochoBase.totalTuihiNumber += ikubyoDiarySanpuData.sanpuryo;  //追肥量
          }
          oldDate = ikubyoDiary.workDate;
        }
      }
    }
}
