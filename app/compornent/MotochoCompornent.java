package compornent;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import play.Logger;
import play.libs.Json;

import models.Compartment;
import models.Crop;
import models.FarmStatus;
import models.Field;
import models.FieldGroup;
import models.Hinsyu;
import models.MotochoBase;
import models.MotochoHiryo;
import models.MotochoNouyaku;
import models.Nouhi;
import models.PosttoPoint;
import models.Weather;
import models.Work;
import models.WorkDiary;
import models.WorkDiarySanpu;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import consts.AgryeelConst;

public class MotochoCompornent implements AgryellInterface{


    double kukakuId = 0;				//区画ID
    public MotochoBase lastMotochoBase;	//最終元帳基本情報
    public double lastWorkId = 0;       //最終作業ID

    private int hashuCount  = 0;

    /**
     * コンストラクタ
     * @param kukakuId
     */
    public MotochoCompornent(double kukakuId) {

        this.kukakuId = kukakuId;

    }

    public void make() {

    	List<WorkDiary> aryWorkDiary 	= WorkDiary.getWorkDiary(this.kukakuId);
    	int rotationSpeedOfYear  = 0;
    	int oldYear				       = 0;
    	boolean initFlag		     = true;
    	boolean initDataFlag	   = true;
    	boolean dataMake		     = false;
      java.sql.Date nullDate	 = DateU.GetNullDate();
      java.sql.Date oldDate    = null;

      MotochoBase motochoBase  = new MotochoBase();
      MotochoBase oldMotochoBase  = null;
      Work oldWork = null;
      WorkDiary oldWorkDiary = null;

      /*------------------------------------------------------------------------------------------------------------*/
      /* 初期化                                                                                                     */
      /*------------------------------------------------------------------------------------------------------------*/
  		motochoBase.hashuDate			    = nullDate;
  		motochoBase.shukakuStartDate	= nullDate;
  		motochoBase.shukakuEndDate		= nullDate;

      /*------------------------------------------------------------------------------------------------------------*/
      /* 対象区画の元帳データを削除                                                                                 */
      /*------------------------------------------------------------------------------------------------------------*/
  		deleteMotocho();

      /*------------------------------------------------------------------------------------------------------------*/
      /* 区画情報の取得                                                                                             */
      /*------------------------------------------------------------------------------------------------------------*/
      /* 区画状況情報の取得 */
      Compartment compartment = Compartment.find.where().eq("kukaku_id", this.kukakuId).findUnique();

      /*------------------------------------------------------------------------------------------------------------*/
      /* 生産者ステータスを取得
      /*------------------------------------------------------------------------------------------------------------*/
      FarmStatus fs = FarmStatus.getFarmStatus(compartment.farmId);

      /*------------------------------------------------------------------------------------------------------------*/
      /* 区画グループ情報の取得                                                                                     */
      /*------------------------------------------------------------------------------------------------------------*/
      /* 区画グループ情報の取得 */
      FieldGroup fieldGroup = compartment.getFieldGroupInfo();

    	for (WorkDiary workDiary : aryWorkDiary) {

    	  Work work = Work.getWork(workDiary.workId);
    	  oldDate   = workDiary.workDate;
    	  this.lastWorkId = workDiary.workId;
    		if (work.workTemplateId != AgryeelConst.WorkTemplate.END && work.workTemplateId != AgryeelConst.WorkTemplate.SAIBAIKAISI) {
    			continue;
    		}

    		initDataFlag	= false;

        /*------------------------------------------------------------------------------------------------------------*/
        /* 元帳照会基本の反映                                                                                         */
        /*------------------------------------------------------------------------------------------------------------*/
    		if (dataMake) {

      		motochoBase.workEndDay = oldDate;

      		if (motochoBase.workStartDay != null) {
        		setBaseInfo(motochoBase);
        		if (oldWork.workTemplateId == AgryeelConst.WorkTemplate.SAIBAIKAISI && oldMotochoBase != null) {
            		motochoBase.hashuDate 	= oldWorkDiary.workDate;
            		motochoBase.hinsyuId 	= oldMotochoBase.hinsyuId;
            		motochoBase.hinsyuName	= "";
	                motochoBase.hinsyuName = Hinsyu.getMultiHinsyuName(motochoBase.hinsyuId); //品種名
	                motochoBase.cropId     = Hinsyu.getMultiHinsyuCropId(motochoBase.hinsyuId);
	                motochoBase.cropName  = "";
	                Crop crop = Crop.find.where().eq("crop_id", motochoBase.cropId).findUnique();        //生産物情報を取得する
	                if (crop != null) { motochoBase.cropId        = crop.cropId; motochoBase.cropName = crop.cropName; }             //生産物名
        		}
        		makeMotochoNouyaku(motochoBase);
        		makeMotochoHiryo(motochoBase);
            motochoBase.save();
          	initFlag		= false;
          	oldMotochoBase = motochoBase;
      		}

    		}

    		dataMake = false;

        /*------------------------------------------------------------------------------------------------------------*/
        /* 作付け年と回転数の計算                                                                                     */
        /*------------------------------------------------------------------------------------------------------------*/
    		//作付年と回転数を生産者単位で可変する
    		Calendar workdate = Calendar.getInstance();
    		workdate.setTime(workDiary.workDate);
    		int year = workdate.get(Calendar.YEAR);
        int month = workdate.get(Calendar.MONTH) + 1;
        if (month < fs.kisyo) {
          year--;
        }
        if (fs.nendoJudge == 1) { //年度判定が翌年の場合
          year++;
        }
        Logger.info("[{}/{}/{}]->{} OLD:{}", workdate.get(Calendar.YEAR), workdate.get(Calendar.MONTH), workdate.get(Calendar.DAY_OF_MONTH), year, oldYear);
    		if (oldYear != year) {

    	    	rotationSpeedOfYear = 0;
    	    	hashuCount          = 0;

    		}

	    	rotationSpeedOfYear++;

        /*------------------------------------------------------------------------------------------------------------*/
        /* 元帳照会基本の作成                                                                                         */
        /*------------------------------------------------------------------------------------------------------------*/
    		motochoBase = new MotochoBase();

    		motochoBase.kukakuId 				    = this.kukakuId;
    		motochoBase.kukakuName			    = compartment.kukakuName;
    		motochoBase.kukakuGroupColor	  = fieldGroup.fieldGroupColor;
    		motochoBase.workYear 				    = year;
    		motochoBase.rotationSpeedOfYear = rotationSpeedOfYear;
    		motochoBase.workStartDay 			  = workDiary.workDate;
    		motochoBase.area                = compartment.area;

    		oldYear = year;
    		oldWork = work;
    		oldWorkDiary = workDiary;

    		dataMake = true;

    	}

      /*------------------------------------------------------------------------------------------------------------*/
      /* 元帳照会基本の反映（現在）                                                                                 */
      /*------------------------------------------------------------------------------------------------------------*/
  		motochoBase.workEndDay = new java.sql.Date(Calendar.getInstance().getTime().getTime());
  		if (motochoBase.workStartDay != null) {
        setBaseInfo(motochoBase);
		if (oldWork.workTemplateId == AgryeelConst.WorkTemplate.SAIBAIKAISI && oldMotochoBase != null) {
    		motochoBase.hashuDate 	= oldWorkDiary.workDate;
    		motochoBase.hinsyuId 	= oldMotochoBase.hinsyuId;
    		motochoBase.hinsyuName	= "";
            motochoBase.hinsyuName = Hinsyu.getMultiHinsyuName(motochoBase.hinsyuId); //品種名
            motochoBase.cropId     = Hinsyu.getMultiHinsyuCropId(motochoBase.hinsyuId);
            motochoBase.cropName  = "";
            Crop crop = Crop.find.where().eq("crop_id", motochoBase.cropId).findUnique();        //生産物情報を取得する
            if (crop != null) { motochoBase.cropId        = crop.cropId; motochoBase.cropName = crop.cropName; }             //生産物名
		}
        makeMotochoNouyaku(motochoBase);
        makeMotochoHiryo(motochoBase);
        motochoBase.save();
  		}else{
  			motochoBase.workStartDay = motochoBase.workEndDay;
  		}

  		this.lastMotochoBase = motochoBase;

    }

    private void deleteMotocho() {

        Ebean.createSqlUpdate("DELETE FROM motocho_base 	  WHERE kukaku_id = :kukaku_id").setParameter("kukaku_id", this.kukakuId).execute();
        Ebean.createSqlUpdate("DELETE FROM motocho_hiryo 	  WHERE kukaku_id = :kukaku_id").setParameter("kukaku_id", this.kukakuId).execute();
        Ebean.createSqlUpdate("DELETE FROM motocho_nouyaku  WHERE kukaku_id = :kukaku_id").setParameter("kukaku_id", this.kukakuId).execute();

    }

    private void setBaseInfo(MotochoBase motochoBase) {

      List<WorkDiary> aryWork = WorkDiary.getWorkDiaryOfWork(this.kukakuId, motochoBase.workStartDay, motochoBase.workEndDay);
      /*------------------------------------------------------------------------------------------------------------*/
      /* 播種情報の反映                                                                                                                                                                                                                                                */
      /*------------------------------------------------------------------------------------------------------------*/

    	if (aryWork.size() > 0) {

        	for (WorkDiary workDiary : aryWork) {

        		/* 作業開始以前の作業は無効とする */
        		if (motochoBase.workStartDay.compareTo(workDiary.workDate) > 0) {
        			continue;
        		}

        		Work work = Work.getWork(workDiary.workId);
        		if (work.workTemplateId != AgryeelConst.WorkTemplate.HASHU) {
        		  continue;
        		}

        		this.hashuCount++;

        		motochoBase.hashuDate 	= workDiary.workDate;
        		motochoBase.hinsyuId 	= workDiary.hinsyuId;
        		motochoBase.hinsyuName	= "";
            motochoBase.hinsyuName = Hinsyu.getMultiHinsyuName(workDiary.hinsyuId); //品種名
            motochoBase.cropId     = Hinsyu.getMultiHinsyuCropId(workDiary.hinsyuId);
            motochoBase.cropName  = "";
            Crop crop = Crop.find.where().eq("crop_id", motochoBase.cropId).findUnique();        //生産物情報を取得する
            if (crop != null) { motochoBase.cropId        = crop.cropId; motochoBase.cropName = crop.cropName; }             //生産物名
        	}
          motochoBase.hashuCount  = this.hashuCount;
    	}

      /*------------------------------------------------------------------------------------------------------------*/
      /* 収穫情報の反映                                                                                                                                                                                                                                                */
      /*------------------------------------------------------------------------------------------------------------*/
    	boolean shukakuFlag = false;
    	Calendar shukakuStartDate = Calendar.getInstance();

    	if (aryWork.size() > 0) {

        	for (WorkDiary workDiary : aryWork) {

        		/* 作業開始以降のみ有効とする */
        		if (motochoBase.workStartDay.compareTo(workDiary.workDate) > 0) {
        			continue;
        		}
            /* 播種日以降のみ有効とする */
            if (motochoBase.hashuDate == null) {
              continue;
            }
            if (motochoBase.hashuDate.compareTo(workDiary.workDate) > 0) {
              continue;
            }
            Work work = Work.getWork(workDiary.workId);
            if (work.workTemplateId != AgryeelConst.WorkTemplate.SHUKAKU &&
                work.workTemplateId != AgryeelConst.WorkTemplate.SENKA) {
              continue;
            }

        		if (!shukakuFlag) {
        			motochoBase.shukakuStartDate 	= workDiary.workDate;
        			shukakuStartDate.setTime(motochoBase.shukakuStartDate);
        			shukakuFlag = true;
        		}

        		motochoBase.shukakuEndDate 	        = workDiary.workDate;
//        		motochoBase.totalShukakuCount++;
//        		motochoBase.totalShukakuNumber	   += workDiary.shukakuRyo;
            motochoBase.totalShukakuCount += workDiary.shukakuRyo;
            motochoBase.totalShukakuNumber++;

        	}

    	}
      /*------------------------------------------------------------------------------------------------------------*/
      /* 潅水情報の反映                                                                                                                                                                                                                                                */
      /*------------------------------------------------------------------------------------------------------------*/

      if (aryWork.size() > 0) {

          for (WorkDiary workDiary : aryWork) {

            /* 作業開始以降のみ有効とする */
            if (motochoBase.workStartDay.compareTo(workDiary.workDate) > 0) {
              continue;
            }
            Work work = Work.getWork(workDiary.workId);
            if (work.workTemplateId != AgryeelConst.WorkTemplate.KANSUI) {
              continue;
            }

            motochoBase.finalKansuiDate          = workDiary.workDate;
            motochoBase.totalKansuiCount++;
            motochoBase.totalKansuiNumber       += workDiary.kansuiRyo;

          }

      }
      /*------------------------------------------------------------------------------------------------------------*/
      /* 積算温度の反映                                                                                                                                                                                                                                                */
      /*------------------------------------------------------------------------------------------------------------*/
      java.sql.Date nullDate     = DateU.GetNullDate();
      if ((motochoBase.hashuDate != null)
          && (motochoBase.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

        Compartment ct = Compartment.getCompartmentInfo(motochoBase.kukakuId);
        if (ct != null) {
          Field fd = ct.getFieldInfo();
          if (fd != null) {
            String pointId = PosttoPoint.getPointId(fd.postNo);
            if (pointId != null && !"".equals(pointId)) {
              java.sql.Date endDate;

              if ((motochoBase.shukakuStartDate != null)
                  && (motochoBase.shukakuStartDate.compareTo(nullDate) != 0)) { //収穫開始が正しく登録されている場合
                endDate = motochoBase.shukakuStartDate;
              }
              else {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -2);
                DateU.setTime(cal, DateU.TimeType.TO);
                endDate = new java.sql.Date(cal.getTime().getTime());
              }

              List<Weather> weathers = Weather.getWeather(pointId, motochoBase.hashuDate, endDate);

              double kion = 0;
              for (Weather weather : weathers) {
                kion += weather.kionAve;
              }
              motochoBase.totalSolarRadiation = kion;
            }
          }
        }
      }

      /*------------------------------------------------------------------------------------------------------------*/
      /* 生育日数の算出                                                                                                                                                                                                                                                */
      /*------------------------------------------------------------------------------------------------------------*/
      if ((motochoBase.hashuDate != null)
          && (motochoBase.hashuDate.compareTo(nullDate) != 0)) {  //播種日が未設定の場合
        if ((motochoBase.shukakuStartDate != null)
            && (motochoBase.shukakuStartDate.compareTo(nullDate) != 0)) {  //播種日が未設定の場合
          Date hashuDate = DateUtils.truncate(motochoBase.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(motochoBase.shukakuStartDate, Calendar.DAY_OF_MONTH);

          motochoBase.seiikuDayCount = (int)DateU.GetDiffDate(hashuDate, systemDate);

        }
      }
      /*------------------------------------------------------------------------------------------------------------*/
      /* 収穫開始 予測                                                                                                                                                                                                                                                  */
      /*------------------------------------------------------------------------------------------------------------*/
      if (!"".equals(motochoBase.hinsyuId) && (motochoBase.hashuDate != null)
        && (motochoBase.hashuDate.compareTo(nullDate) != 0)) {
          Compartment ct = Compartment.find.where().eq("kukaku_id", this.kukakuId).findUnique();
          models.PredictionPoint pp = models.PredictionPoint.find.where().eq("farm_id", ct.farmId).eq("hinsyu_id", motochoBase.hinsyuId).findUnique();
          if (pp != null && pp.integratedTemp != 0) {
            Field fd = ct.getFieldInfo();
            if (fd != null) {
              String pointId = PosttoPoint.getPointId(fd.postNo);
              if (pointId != null && !"".equals(pointId)) {
                java.sql.Date endDate;

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 2);
                DateU.setTime(cal, DateU.TimeType.TO);
                endDate = new java.sql.Date(cal.getTime().getTime());

                List<Weather> weathers = Weather.getWeather(pointId, motochoBase.hashuDate, endDate);

                double kion = 0;
                for (Weather weather : weathers) {
                  if (weather.kionAve < 10 ) {
                    kion += 0;
                  }
                  else  if (25 < weather.kionAve) {
                    kion += 25;
                  }
                  else {
                    kion += weather.kionAve;
                  }
                  if (pp.integratedTemp <= kion) {
                    motochoBase.predictionShukakuStartDate = weather.dayDate;
                    break;
                  }
                }
              }
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

      List<WorkDiary> aryWork = WorkDiary.getWorkDiaryOfWork(this.kukakuId, motochoBase.workStartDay, motochoBase.workEndDay);

      for (WorkDiary workDiary : aryWork) {

        /* 作業開始以前の作業は無効とする */
        if (motochoBase.workStartDay.compareTo(workDiary.workDate) > 0) {
          continue;
        }

        Work work = Work.getWork(workDiary.workId);
        if (work.workTemplateId != AgryeelConst.WorkTemplate.SANPU) {
          continue;
        }

        //該当作業記録の散布情報を取得する
        List<WorkDiarySanpu> workDiarySanpuList = WorkDiarySanpu.find.where().eq("work_diary_id", workDiary.workDiaryId).orderBy("nouhi_id asc").findList();

        for (WorkDiarySanpu workDiarySanpuData : workDiarySanpuList) {
            Nouhi     nouhi   = Nouhi.find.where().eq("nouhi_id", workDiarySanpuData.nouhiId).findUnique();   /* 農肥情報を取得 */
            if ((nouhi == null) || (nouhi.nouhiKind != AgryeelConst.NouhiKind.NOUYAKU)) {
              continue;
            }
            MotochoNouyaku motochoNouyaku       = new MotochoNouyaku();	           //元帳農薬情報の生成
            motochoNouyaku.kukakuId				       = this.kukakuId;										 //区画ID
            motochoNouyaku.workYear				       = motochoBase.workYear;						 //作付年
            motochoNouyaku.rotationSpeedOfYear	 = motochoBase.rotationSpeedOfYear;	 //年内回転数
            motochoNouyaku.nouhiId				       = workDiarySanpuData.nouhiId;			 //農肥ID

            Double dNouyakuNo = nouyakuNoTable.get(Double.valueOf(motochoNouyaku.nouhiId));						//農薬番号テーブルより農薬番号を取得
            if (dNouyakuNo == null) {																			 //農薬番号未採番の場合
                nouyakuNo++;																					     //農薬番号をカウントアップ
                motochoNouyaku.nouyakuNo		= nouyakuNo;									 //農薬番号
                nouyakuNoTable.put(Double.valueOf(motochoNouyaku.nouhiId), Double.valueOf(nouyakuNo));//農薬番号をテーブルに格納
            }
            else {
                motochoNouyaku.nouyakuNo		= dNouyakuNo.doubleValue();										//農薬番号
            }

            MotochoNouyaku motochoNouyakuE	 = MotochoNouyaku.find.where()
                                          										.eq("kukaku_id", this.kukakuId)
                                          										.eq("work_year", motochoBase.workYear)
                                          										.eq("rotation_speed_of_year", motochoBase.rotationSpeedOfYear)
                                          										.eq("nouyaku_no", motochoNouyaku.nouyakuNo)
                                          										.eq("sanpu_date", workDiary.workDate)
                                          										.findUnique();


            if (motochoNouyakuE == null || (motochoNouyakuE.sanpuDate.compareTo(workDiary.workDate) != 0)) {

                motochoNouyaku.nouhiName			= nouhi.nouhiName;												//農肥名

                motochoNouyaku.sanpuDate			= workDiary.workDate;											//散布日
                motochoNouyaku.bairitu				= workDiarySanpuData.bairitu;							//倍率
                motochoNouyaku.sanpuMethod		= workDiarySanpuData.sanpuMethod;				  //散布方法
                motochoNouyaku.sanpuryo				= workDiarySanpuData.sanpuryo;						//散布量

                motochoNouyaku.n					    = motochoNouyaku.sanpuryo * (nouhi.n / 100) * (1 / motochoNouyaku.bairitu);
                motochoNouyaku.p					    = motochoNouyaku.sanpuryo * (nouhi.p / 100) * (1 / motochoNouyaku.bairitu);
                motochoNouyaku.k					    = motochoNouyaku.sanpuryo * (nouhi.k / 100) * (1 / motochoNouyaku.bairitu);

            }
            else {

            	motochoNouyakuE.sanpuryo			  = motochoNouyakuE.sanpuryo + workDiarySanpuData.sanpuryo;	//散布量

            	motochoNouyakuE.n					      = motochoNouyakuE.sanpuryo * (nouhi.n / 100) * (1 / motochoNouyakuE.bairitu);
            	motochoNouyakuE.p					      = motochoNouyakuE.sanpuryo * (nouhi.p / 100) * (1 / motochoNouyakuE.bairitu);
            	motochoNouyakuE.k					      = motochoNouyakuE.sanpuryo * (nouhi.k / 100) * (1 / motochoNouyakuE.bairitu);

            }


            if (nouhi.unitKind == 1) {
                motochoNouyaku.unit = "Kg";
            }
            else {
                motochoNouyaku.unit = "L";
            }

            if (motochoNouyakuE == null || (motochoNouyakuE.sanpuDate.compareTo(workDiary.workDate) != 0)) {

                motochoNouyaku.save();

            }
            else {

            	motochoNouyakuE.update();

            }
            if (oldDate == null || oldDate.compareTo(workDiary.workDate) != 0) {
              motochoBase.totalDisinfectionCount++;                               //消毒回数
            }
            motochoBase.finalDisinfectionDate = workDiary.workDate;             //最終消毒日
            motochoBase.totalDisinfectionNumber += workDiarySanpuData.sanpuryo; //消毒量
            oldDate = workDiary.workDate;
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

      List<WorkDiary> aryWork = WorkDiary.getWorkDiaryOfWork(this.kukakuId, motochoBase.workStartDay, motochoBase.workEndDay);

      for (WorkDiary workDiary : aryWork) {

        Work work = Work.getWork(workDiary.workId);
        if (work.workTemplateId != AgryeelConst.WorkTemplate.SANPU) {
          continue;
        }

        //該当作業記録の散布情報を取得する
        List<WorkDiarySanpu> workDiarySanpuList = WorkDiarySanpu.find.where().eq("work_diary_id", workDiary.workDiaryId).orderBy("nouhi_id asc").findList();

        for (WorkDiarySanpu workDiarySanpuData : workDiarySanpuList) {

          Nouhi nouhi = Nouhi.find.where().eq("nouhi_id", workDiarySanpuData.nouhiId).findUnique();         /* 農肥情報を取得 */
          if ((nouhi == null) || (nouhi.nouhiKind != AgryeelConst.NouhiKind.HIRYO)) {
            continue;
          }

          MotochoHiryo motochoHiryo 			  = new MotochoHiryo();									//元帳肥料情報の生成
          motochoHiryo.kukakuId				      = this.kukakuId;												//区画ID
          motochoHiryo.workYear				      = motochoBase.workYear;									//作付年
          motochoHiryo.rotationSpeedOfYear	= motochoBase.rotationSpeedOfYear;			//年内回転数
          motochoHiryo.nouhiId				      = workDiarySanpuData.nouhiId;					  //農肥ID

          Double dHiryoNo = hiryoNoTable.get(Double.valueOf(motochoHiryo.nouhiId));							//肥料番号テーブルより肥料番号を取得
          if (dHiryoNo == null) {																				       //肥料番号未採番の場合
              hiryoNo++;																						           //肥料番号をカウントアップ
              motochoHiryo.hiryoNo			= hiryoNo;														 //肥料番号
              hiryoNoTable.put(Double.valueOf(motochoHiryo.nouhiId), Double.valueOf(hiryoNo));  //肥料番号をテーブルに格納
          }
          else {
              motochoHiryo.hiryoNo			= dHiryoNo.doubleValue();										//肥料番号
          }

          MotochoHiryo motochoHiryoE		 = MotochoHiryo.find.where()
                                                				.eq("kukaku_id", this.kukakuId)
                                                				.eq("work_year", motochoBase.workYear)
                                                				.eq("rotation_speed_of_year", motochoBase.rotationSpeedOfYear)
                                                				.eq("hiryo_no", motochoHiryo.hiryoNo)
                                                				.eq("sanpu_date", workDiary.workDate)
                                                				.findUnique();


          if (motochoHiryoE == null || (motochoHiryoE.sanpuDate.compareTo(workDiary.workDate) != 0)) {

              motochoHiryo.nouhiName			= nouhi.nouhiName;													//農肥名
              motochoHiryo.sanpuDate			= workDiary.workDate;												//散布日
              motochoHiryo.bairitu			  = workDiarySanpuData.bairitu;								//倍率
              motochoHiryo.sanpuMethod		= workDiarySanpuData.sanpuMethod;						//散布方法
              motochoHiryo.sanpuryo			  = workDiarySanpuData.sanpuryo;							//散布量

              motochoHiryo.n					    = motochoHiryo.sanpuryo * (nouhi.n / 100) * (1 / motochoHiryo.bairitu);
              motochoHiryo.p					    = motochoHiryo.sanpuryo * (nouhi.p / 100) * (1 / motochoHiryo.bairitu);
              motochoHiryo.k					    = motochoHiryo.sanpuryo * (nouhi.k / 100) * (1 / motochoHiryo.bairitu);
              motochoHiryo.mg             = motochoHiryo.sanpuryo * (nouhi.mg / 100) * (1 / motochoHiryo.bairitu);
          }
          else {

            	motochoHiryoE.sanpuryo			= motochoHiryoE.sanpuryo + workDiarySanpuData.sanpuryo;			//散布量

            	motochoHiryoE.n					    = motochoHiryoE.sanpuryo * (nouhi.n / 100) * (1 / motochoHiryoE.bairitu);
            	motochoHiryoE.p					    = motochoHiryoE.sanpuryo * (nouhi.p / 100) * (1 / motochoHiryoE.bairitu);
              motochoHiryoE.k             = motochoHiryoE.sanpuryo * (nouhi.k / 100) * (1 / motochoHiryoE.bairitu);
              motochoHiryoE.mg            = motochoHiryoE.sanpuryo * (nouhi.mg / 100) * (1 / motochoHiryoE.bairitu);

          }

          if (nouhi.unitKind == 1) {
          	motochoHiryo.unit = "Kg";
          }
          else {
          	motochoHiryo.unit = "L";
          }

          if (motochoHiryoE == null || (motochoHiryoE.sanpuDate.compareTo(workDiary.workDate) != 0)) {

          	motochoHiryo.save();

          }
          else {

          	motochoHiryoE.update();

          }
          if (motochoBase.hashuDate != null && motochoBase.hashuDate.compareTo(workDiary.workDate) <= 0) {
            motochoBase.finalTuihiDate = workDiary.workDate;              //最終追肥日
            if (oldDate == null || oldDate.compareTo(workDiary.workDate) != 0) {
              motochoBase.totalTuihiCount++;                              //追肥回数
            }
            motochoBase.totalTuihiNumber += workDiarySanpuData.sanpuryo;  //追肥量
          }
          oldDate = workDiary.workDate;
        }
      }
    }

    public static List<MotochoBase> getMotochoYear(double kukakuId) {

    	List<MotochoBase> list = MotochoBase.find.where("kukaku_id = " + kukakuId).orderBy("work_year asc, rotation_speed_of_year asc").findList();
    	return list;

    }
    public static int getMotochoHistry(double kukakuId, ObjectNode pListJson) {

      int result  = GET_SUCCESS;

      List<MotochoBase> list = MotochoBase.find.where("kukaku_id = " + kukakuId).orderBy("work_year desc, rotation_speed_of_year desc").findList();
      DecimalFormat df  = new DecimalFormat("00000000");
      DecimalFormat df2 = new DecimalFormat("0000");
      DecimalFormat df3 = new DecimalFormat("00");
      for (MotochoBase base : list) {

          ObjectNode jd = Json.newObject();

          jd.put("id"   , df.format(base.kukakuId) + df2.format(base.workYear) + df3.format(base.rotationSpeedOfYear));
          if (base.hinsyuName != null) {
              jd.put("name" , "" + df2.format(base.workYear) + " 年 " + df3.format(base.rotationSpeedOfYear) + " 作（" + base.hashuDate + "  " + base.hinsyuName + "）");
          }
          else {
              jd.put("name" , "" + df2.format(base.workYear) + " 年 " + df3.format(base.rotationSpeedOfYear) + " 作");
          }

          pListJson.put(df.format(base.kukakuId) + df2.format(base.workYear) + df3.format(base.rotationSpeedOfYear), jd);
      }
      return result;

    }

    public static int getMotochoHistryArray(double kukakuId, ArrayNode pListJson) {

      int result  = GET_SUCCESS;

      List<MotochoBase> list = MotochoBase.find.where("kukaku_id = " + kukakuId).orderBy("work_year desc, rotation_speed_of_year desc").findList();
      DecimalFormat df  = new DecimalFormat("00000000");
      DecimalFormat df2 = new DecimalFormat("0000");
      DecimalFormat df3 = new DecimalFormat("00");
      for (MotochoBase base : list) {

          ObjectNode jd = Json.newObject();

          jd.put("id"   , df.format(base.kukakuId) + df2.format(base.workYear) + df3.format(base.rotationSpeedOfYear));
          if (base.hinsyuName != null) {
              jd.put("name" , "" + df2.format(base.workYear) + " 年 " + df3.format(base.rotationSpeedOfYear) + " 作（" + base.hashuDate + "  " + base.hinsyuName + "）");
          }
          else {
              jd.put("name" , "" + df2.format(base.workYear) + " 年 " + df3.format(base.rotationSpeedOfYear) + " 作");
          }
          jd.put("flag" , 0);

          pListJson.add(jd);
      }
      return result;

    }

}
