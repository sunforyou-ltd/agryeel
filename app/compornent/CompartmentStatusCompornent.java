package compornent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Hinsyu;
import models.MotochoBase;
import models.Nouhi;
import models.Work;
import models.WorkChainItem;
import models.WorkDiary;
import models.WorkDiaryDetail;
import models.WorkDiarySanpu;

import org.apache.commons.lang3.time.DateUtils;

import play.Logger;
import play.libs.Json;
import util.DateU;
import batch.PredictionPoint;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

public class CompartmentStatusCompornent implements AgryellInterface {


    double kukakuId = 0;				//区画ID
    double workId = 0;        //区画ID
    List<CompartmentWorkChainStatus> cwcss;
    List<WorkChainItem> wcis;
    List<Work> ws;
    public List<WorkDiarySanpu> wdsps;
    public java.sql.Date wdDate;

    public CompartmentStatusCompornent() {
    }
    /**
     * コンストラクタ
     * @param kukakuId
     */
    public CompartmentStatusCompornent(double kukakuId) {

        this.kukakuId = kukakuId;

    }
    public CompartmentStatusCompornent(double kukakuId, double workId) {

      this.kukakuId = kukakuId;
      this.workId = workId;

    }
    public void getAllData(double farmId) {
      this.cwcss = CompartmentWorkChainStatus.find.where().eq("farm_id", farmId).orderBy("kukaku_id").findList();
      this.wcis  = WorkChainItem.find.where().orderBy("work_chain_id, sequence_id").findList();
      this.ws    = Work.find.where().orderBy("work_id").findList();
    }
    private CompartmentWorkChainStatus getCompartmentWorkChainStatus(double kukakuId) {
      CompartmentWorkChainStatus result = null;
      for (CompartmentWorkChainStatus data : cwcss) {
        if (data.kukakuId == kukakuId) {
          result = data;
          break;
        }
      }
      return result;
    }
    private List<WorkChainItem> getWorkChainItem(double workChainId) {
      List<WorkChainItem> result = new ArrayList<WorkChainItem>();
      for (WorkChainItem data : wcis) {
        if (data.workChainId == workChainId) {
          result.add(data);
        }
      }
      return result;
    }
    private Work getWork(double workId) {
      Work result = null;
      for (Work data : ws) {
        if (data.workId == workId) {
          result = data;
          break;
        }
      }
      return result;
    }

    public void update(MotochoBase motochoBase) {


      /*------------------------------------------------------------------------------------------------------------*/
      /* 既存の圃場状況照会を削除                                                                                   */
      /*------------------------------------------------------------------------------------------------------------*/
      CompartmentStatus oldCompartmentStatus = CompartmentStatus.find.where().eq("kukaku_id", this.kukakuId).findUnique();
    	deleteCompartmentStatusCompornent();

      /*------------------------------------------------------------------------------------------------------------*/
      /* 圃場状況照会を作成                                                                                         */
      /*------------------------------------------------------------------------------------------------------------*/
    	/*----- 初期化 -----*/
      CompartmentStatus compartmentStatus = new CompartmentStatus();
      java.sql.Date nullDate		 = DateU.GetNullDate();

      compartmentStatus.finalDisinfectionDate 	= nullDate;			//最終消毒日を初期化する
      compartmentStatus.totalDisinfectionCount  = 0;            //合計消毒量を初期化する
      compartmentStatus.totalDisinfectionNumber = 0;            //合計消毒回数を初期化する
      compartmentStatus.finalKansuiDate 			  = nullDate;			//最終潅水日を初期化する
      compartmentStatus.totalKansuiCount 			  = 0;				    //合計潅水量を初期化する
      compartmentStatus.totalKansuiNumber       = 0;            //合計潅水回数を初期化する
      compartmentStatus.finalTuihiDate 			    = nullDate;			//最終追肥日を初期化する
      compartmentStatus.totalTuihiCount 			  = 0;				    //合計追肥量を初期化する
      compartmentStatus.totalTuihiNumber        = 0;            //合計追肥回数を初期化する
      compartmentStatus.shukakuStartDate 			  = nullDate;			//収穫開始日を初期化する
      compartmentStatus.shukakuEndDate 			    = nullDate;			//収穫終了日を初期化する
      compartmentStatus.totalShukakuCount	   		= 0;				    //収穫量を初期化する
      compartmentStatus.totalShukakuNumber      = 0;            //収穫回数を初期化する
      compartmentStatus.totalSolarRadiation     = 0;            //合計積算日射量を初期化する

    	/*----- 基本情報 -----*/
      compartmentStatus.kukakuId 						    = this.kukakuId;												//区画ID
      compartmentStatus.workYear                = motochoBase.workYear;                 //作業年
      compartmentStatus.rotationSpeedOfYear			= motochoBase.rotationSpeedOfYear;			//年内回転数
      compartmentStatus.hinsyuId                = motochoBase.hinsyuId;                 //品種ID
      compartmentStatus.hinsyuName					    = motochoBase.hinsyuName;								//品種名
      compartmentStatus.hashuDate						    = motochoBase.hashuDate;								//播種日
      compartmentStatus.seiikuDayCount				  = motochoBase.seiikuDayCount;						//生育日数
      compartmentStatus.shukakuStartDate				= motochoBase.shukakuStartDate;					//収穫開始日
      compartmentStatus.shukakuEndDate				  = motochoBase.shukakuEndDate;						//収穫終了日
  		compartmentStatus.totalShukakuCount			  = motochoBase.totalShukakuCount;				//収穫量
      compartmentStatus.hashuCount              = motochoBase.hashuCount;               //播種回数
  		CompartmentWorkChainStatus cwcs = compartmentStatus.getWorkChainStatus();


      compartmentStatus.katadukeDate            = motochoBase.workStartDay;
      compartmentStatus.finalDisinfectionDate   = motochoBase.finalDisinfectionDate;
      compartmentStatus.totalDisinfectionCount  = motochoBase.totalDisinfectionCount;
      compartmentStatus.totalDisinfectionNumber = motochoBase.totalDisinfectionNumber;
      compartmentStatus.finalTuihiDate          = motochoBase.finalTuihiDate;
      compartmentStatus.totalTuihiCount         = motochoBase.totalTuihiCount;
      compartmentStatus.totalTuihiNumber        = motochoBase.totalTuihiNumber;
      compartmentStatus.finalKansuiDate         = motochoBase.finalKansuiDate;
      compartmentStatus.totalKansuiCount        = motochoBase.totalKansuiCount;
      compartmentStatus.totalKansuiNumber       = motochoBase.totalKansuiNumber;
      compartmentStatus.totalShukakuCount       = motochoBase.totalShukakuCount;
      compartmentStatus.totalShukakuNumber      = motochoBase.totalShukakuNumber;
      compartmentStatus.hinsyuName              = motochoBase.hinsyuName;
      compartmentStatus.hinsyuId                = motochoBase.hinsyuId;
      compartmentStatus.cropId                  = motochoBase.cropId;
      compartmentStatus.finalEndDate                = motochoBase.workEndDay;
      compartmentStatus.totalSolarRadiation         = motochoBase.totalSolarRadiation;
      compartmentStatus.predictionShukakuStartDate  = motochoBase.predictionShukakuStartDate;
      compartmentStatus.naeNo                   = motochoBase.naeNo;

    	//次回作業を更新する
    	if (cwcs != null){
	      WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(cwcs.workChainId, this.workId);
	      if (wci != null) {
	        WorkChainItem wcin = WorkChainItem.getWorkChainItemOfSeq(cwcs.workChainId, wci.nextSequenceId);
	        if (wcin != null) {
	          compartmentStatus.nextWorkId = wcin.workId;
	        }
	      }
    	}

      CompartmentWorkChainStatus cws = compartmentStatus.getWorkChainStatus();
      String endId = "";

      if (cws != null) {
        Analysis als = new Analysis();
        List<WorkDiary> wds = WorkDiary.getWorkDiaryOfWork(compartmentStatus.kukakuId, compartmentStatus.katadukeDate, compartmentStatus.finalEndDate);
        for (WorkDiary wd :wds) {
//          if (endId.indexOf(String.valueOf(wd.workId)) != -1) { //既に同一作業が終了文字列に存在する場合
//            continue;
//          }
          if (als.check(wd.workId)) {
            continue;
          }
          als.add(wd.workId);
    	    if (!"".equals(endId)) {
    		    endId += ",";
    	    }
    	    endId += wd.workId;
        }
        cws.workEndId = endId;
        cws.update();
      }

      /*------------------------------------------------------------------------------------------------------------*/
      /* 害虫情報の集約
      /*------------------------------------------------------------------------------------------------------------*/
      compartmentStatus.pestId              = oldCompartmentStatus.pestId;
      compartmentStatus.pestGeneration      = oldCompartmentStatus.pestGeneration;
      compartmentStatus.pestIntegratedKion  = oldCompartmentStatus.pestIntegratedKion;
      compartmentStatus.prevCalcDate        = oldCompartmentStatus.prevCalcDate;
      compartmentStatus.pestIntegratedKion  = oldCompartmentStatus.pestIntegratedKion;
      compartmentStatus.pestPredictDate     = oldCompartmentStatus.pestPredictDate;
      compartmentStatus.targetSanpuDate     = oldCompartmentStatus.targetSanpuDate;

      if (this.wdsps != null) {
        Calendar syscal = Calendar.getInstance();
        Calendar datacal = Calendar.getInstance();
        for (WorkDiarySanpu wdsp : this.wdsps) {
          Nouhi nouhi = Nouhi.getNouhiInfo(wdsp.nouhiId);
          if (nouhi != null && !"".equals(nouhi.targetPest)) {
            if (nouhi.checkTargetPest(compartmentStatus.pestId)) { //抑制効果あり
              syscal.setTime(this.wdDate);
              datacal.setTime(compartmentStatus.targetSanpuDate);
              if (datacal.compareTo(syscal) == -1) {
                compartmentStatus.targetSanpuDate = new java.sql.Date(syscal.getTimeInMillis());
                compartmentStatus.pestGeneration++;
                break;
              }
            }
          }
        }
      }

    	compartmentStatus.save();

    	PredictionPoint.pestStatusPredictionSingle(compartmentStatus); //害虫発生予測日を再検出する

    }

    private void deleteCompartmentStatusCompornent() {

        Ebean.createSqlUpdate("DELETE FROM compartment_status WHERE kukaku_id = :kukaku_id").setParameter("kukaku_id", this.kukakuId).execute();

    }


    /**
     * 更新処理
     */
    synchronized public void update() {

        /* 区画情報の取得 */
        CompartmentStatus compartmentStatus = CompartmentStatus.find.where("kukaku_id = " + this.kukakuId).findUnique();

        if (compartmentStatus == null) { return; } 			//区画状況情報が存在しない場合、処理をしない

        /* 作業記録情報の取得 */
        List<WorkDiary> 		workDiaryList 			= WorkDiary.find.where("kukaku_id = " + this.kukakuId + " AND kukaku_status_update = " + AgryeelConst.UpdateFlag.NONE).orderBy("work_date").findList();
        List<WorkDiarySanpu> 	workDiarySanpuList;
        double					workSyoudoku			= 0;
        double					workTuihi				= 0;
        java.sql.Date nullDate     = DateU.GetNullDate();

        for (WorkDiary workDiaryData : workDiaryList) {

          Work work = Work.getWork(workDiaryData.workId);

          CompartmentWorkChainStatus cwcs = compartmentStatus.getWorkChainStatus();
          WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(cwcs.workChainId, work.workId);

        	//現在の作業回転数内の作業以外の場合
        	if (compartmentStatus.katadukeDate != null) {
            	Date katadukeDate = DateUtils.truncate(compartmentStatus.katadukeDate, Calendar.DAY_OF_MONTH);
            	Date workDate = DateUtils.truncate(workDiaryData.workDate, Calendar.DAY_OF_MONTH);
            	if (workDate.before(katadukeDate)) {
                    workDiaryData.kukakuStatusUpdate = AgryeelConst.UpdateFlag.UPDATE;
                    workDiaryData.update();
            		continue;
            	}
        	}

          switch((int)work.workTemplateId) {
          case AgryeelConst.WorkTemplate.END:
            compartmentStatus.rotationSpeedOfYear++;        //年内回転数を１アップさせる

            /* 品種名を更新する */
            compartmentStatus.hinsyuName = "";

            compartmentStatus.hashuDate               = nullDate;     //播種日を初期化する
            compartmentStatus.seiikuDayCount          = 0;            //生育日数を初期化する

            compartmentStatus.finalDisinfectionDate   = nullDate;     //最終消毒日を初期化する
            compartmentStatus.totalDisinfectionCount  = 0;            //合計消毒量を初期化する
            compartmentStatus.finalKansuiDate         = nullDate;     //最終潅水日を初期化する
            compartmentStatus.totalKansuiCount        = 0;            //合計潅水量を初期化する
            compartmentStatus.finalTuihiDate          = nullDate;     //最終追肥日を初期化する
            compartmentStatus.totalTuihiCount         = 0;            //合計追肥量を初期化する
            compartmentStatus.shukakuStartDate        = nullDate;     //収穫開始日を初期化する
            compartmentStatus.shukakuEndDate          = nullDate;     //収穫終了日を初期化する
            compartmentStatus.totalShukakuCount       = 0;            //収穫量を初期化する
            compartmentStatus.katadukeDate            = workDiaryData.workStartTime;

            break;
          case AgryeelConst.WorkTemplate.SANPU:
            if ((compartmentStatus.hashuDate == null)
                || (compartmentStatus.hashuDate.compareTo(nullDate) == 0)) {  //播種日が未設定の場合
              //集計情報に含まない
            }
            else {
              switch(wci.nouhiKind) {
              case AgryeelConst.NouhiKind.NOUYAKU:
                compartmentStatus.finalDisinfectionDate   = workDiaryData.workDate; //最終消毒日を更新する

                workDiarySanpuList  = WorkDiarySanpu.find.where("work_diary_id = " + workDiaryData.workDiaryId).findList();
                workSyoudoku    = 0;

                for (WorkDiarySanpu WorkDiarySanpuData : workDiarySanpuList) {
                    workSyoudoku  += WorkDiarySanpuData.sanpuryo;
                }

                compartmentStatus.totalDisinfectionCount++;
                compartmentStatus.totalDisinfectionNumber = (long)workSyoudoku;
                break;
              case AgryeelConst.NouhiKind.HIRYO:
                compartmentStatus.finalTuihiDate  = workDiaryData.workDate; //最終追肥日を更新する

                workDiarySanpuList  = WorkDiarySanpu.find.where("work_diary_id = " + workDiaryData.workDiaryId).findList();
                workTuihi     = 0;

                for (WorkDiarySanpu WorkDiarySanpuData : workDiarySanpuList) {
                    workTuihi   += WorkDiarySanpuData.sanpuryo;
                }

                compartmentStatus.totalTuihiCount++;
                compartmentStatus.totalTuihiNumber = (long)workTuihi;
                break;
              }
            }
            break;
          case AgryeelConst.WorkTemplate.HASHU:
            compartmentStatus.hinsyuName = Hinsyu.getMultiHinsyuName(workDiaryData.hinsyuId);
            compartmentStatus.hinsyuId = workDiaryData.hinsyuId;
            compartmentStatus.cropId   = Hinsyu.getMultiHinsyuCropId(workDiaryData.hinsyuId);;

            compartmentStatus.hashuDate               = workDiaryData.workDate; //播種日を更新する
            //システム日付との自動算出を可能にする為、0のままとする
            compartmentStatus.seiikuDayCount          = 0;                      //生育日数を更新する

            compartmentStatus.finalDisinfectionDate   = nullDate;               //最終消毒日を初期化する
            compartmentStatus.totalDisinfectionCount  = 0;                      //合計消毒量を初期化する
            compartmentStatus.finalKansuiDate         = nullDate;               //最終潅水日を初期化する
            compartmentStatus.totalKansuiCount        = 0;                      //合計潅水量を初期化する
            compartmentStatus.finalTuihiDate          = nullDate;               //最終追肥日を初期化する
            compartmentStatus.totalTuihiCount         = 0;                      //合計追肥量を初期化する
            break;
          case AgryeelConst.WorkTemplate.KANSUI:
            compartmentStatus.finalKansuiDate         = workDiaryData.workDate; //最終潅水日を更新する
            compartmentStatus.totalKansuiNumber       = (long)workDiaryData.kansuiRyo;//潅水量を更新する
            compartmentStatus.totalKansuiCount++;                               //潅水量を更新する
            break;
          case AgryeelConst.WorkTemplate.SHUKAKU:
            if (compartmentStatus.totalShukakuCount == 0) {                   //収穫量がまだ０の場合

              compartmentStatus.shukakuStartDate  = workDiaryData.workDate;   //収穫開始日を更新する
              //収穫開始生育日数を算出する
              Date hashuDate = DateUtils.truncate(compartmentStatus.hashuDate, Calendar.DAY_OF_MONTH);
              Date systemDate = DateUtils.truncate(compartmentStatus.shukakuStartDate, Calendar.DAY_OF_MONTH);

              compartmentStatus.seiikuDayCount = (int)DateU.GetDiffDate(hashuDate, systemDate);

            }
            compartmentStatus.shukakuEndDate      = workDiaryData.workDate;   //収穫終了日を更新する
//            compartmentStatus.totalShukakuCount++; //収穫量を加算する
//            compartmentStatus.totalShukakuNumber += workDiaryData.shukakuRyo; //収穫量を加算する
            compartmentStatus.totalShukakuCount += workDiaryData.shukakuRyo; //収穫量を加算する
            compartmentStatus.totalShukakuNumber++; //収穫量を加算する
            break;
          case AgryeelConst.WorkTemplate.TEISHOKU:
            compartmentStatus.hinsyuName = Hinsyu.getMultiHinsyuName(workDiaryData.hinsyuId);
            compartmentStatus.hinsyuId = workDiaryData.hinsyuId;
            compartmentStatus.cropId   = Hinsyu.getMultiHinsyuCropId(workDiaryData.hinsyuId);;

            compartmentStatus.hashuDate               = workDiaryData.workDate; //播種日を更新する
            //システム日付との自動算出を可能にする為、0のままとする
            compartmentStatus.seiikuDayCount          = 0;                      //生育日数を更新する

            compartmentStatus.finalDisinfectionDate   = nullDate;               //最終消毒日を初期化する
            compartmentStatus.totalDisinfectionCount  = 0;                      //合計消毒量を初期化する
            compartmentStatus.finalKansuiDate         = nullDate;               //最終潅水日を初期化する
            compartmentStatus.totalKansuiCount        = 0;                      //合計潅水量を初期化する
            compartmentStatus.finalTuihiDate          = nullDate;               //最終追肥日を初期化する
            compartmentStatus.totalTuihiCount         = 0;                      //合計追肥量を初期化する
			//苗No取得
        	List<WorkDiaryDetail> aryWorkDetail = WorkDiaryDetail.getWorkDiaryDetailList(workDiaryData.workDiaryId);
            int naeCnt = 0;
			String naeNo = "";
        	for (WorkDiaryDetail workDiaryDetail : aryWorkDetail) {
				if (!workDiaryDetail.naeNo.equals("")) {
					if (naeCnt > 0) {
						naeNo = naeNo + ",";
					}
					naeNo = naeNo + workDiaryDetail.naeNo;
					naeCnt++;
				}
			}
			compartmentStatus.naeNo = naeNo;
            break;
          case AgryeelConst.WorkTemplate.SENKA:
            if (compartmentStatus.totalShukakuCount == 0) {                   //収穫量がまだ０の場合

              compartmentStatus.shukakuStartDate  = workDiaryData.workDate;   //収穫開始日を更新する
              //収穫開始生育日数を算出する
              Date hashuDate = DateUtils.truncate(compartmentStatus.hashuDate, Calendar.DAY_OF_MONTH);
              Date systemDate = DateUtils.truncate(compartmentStatus.shukakuStartDate, Calendar.DAY_OF_MONTH);

              compartmentStatus.seiikuDayCount = (int)DateU.GetDiffDate(hashuDate, systemDate);

            }
            compartmentStatus.shukakuEndDate      = workDiaryData.workDate;   //収穫終了日を更新する
            compartmentStatus.totalShukakuCount += workDiaryData.shukakuRyo; //収穫量を加算する
            compartmentStatus.totalShukakuNumber++; //収穫量を加算する
            break;
        }

            workDiaryData.kukakuStatusUpdate = AgryeelConst.UpdateFlag.UPDATE;
            workDiaryData.update();

        }

        compartmentStatus.update();

    }

    /**
     * 対象区画の作業状況をJSONに格納します
     * @param pKukakuId
     * @param pListJson
     * @return
     */
    public int getWorkChainStatusJson(double pKukakuId, ObjectNode pListJson) {

      int result = GET_SUCCESS;

      CompartmentWorkChainStatus cwcs = getCompartmentWorkChainStatus(pKukakuId);
      if (cwcs != null) {
        String[] endWork = cwcs.workEndId.split(",");
        List<Double> aryWork = new ArrayList<Double>();
        for (String id : endWork) {
          if (id == null || "".equals(id)) {
            continue;
          }
          aryWork.add(Double.parseDouble(id));
        }

        if (cwcs != null) {
          List<WorkChainItem> wcis = getWorkChainItem(cwcs.workChainId);
          for (WorkChainItem wci : wcis) {
            Work wk = getWork(wci.workId);

            ObjectNode jd = Json.newObject();

            jd.put("id"   , wk.workId);
            jd.put("name" , wk.workName);
            jd.put("color" , wk.workColor);

            int chain = 0;
            for (WorkChainItem item : wcis) {
              if (item.nextSequenceId == wci.sequenceId) { //ワークチェーン対象の場合
                chain = 1;
                break;
              }
            }
            jd.put("chain" , chain);

            int end = 0;

            for (Double id : aryWork) {
              if (wk.workId == id.doubleValue()) {
                end = 1;
                break;
              }
            }
            jd.put("end" , end);
            pListJson.put(String.valueOf(wk.workId), jd);
          }
        }
      }
      else {
        Logger.error("[ getWorkChainStatusJson NO DATA ]kukaku = {}", pKukakuId);
      }
      return result;
    }

    /**
     * 対象区画の作業状況をJSONに格納します(Array)
     * @param pKukakuId
     * @param pListJson
     * @return
     */
    public int getWorkChainStatusJsonArray(double pKukakuId, ArrayNode pListJson) {

      int result = GET_SUCCESS;

      CompartmentWorkChainStatus cwcs = getCompartmentWorkChainStatus(pKukakuId);
      if (cwcs != null) {
        String[] endWork = cwcs.workEndId.split(",");
        List<Double> aryWork = new ArrayList<Double>();
        for (String id : endWork) {
          if (id == null || "".equals(id)) {
            continue;
          }
          aryWork.add(Double.parseDouble(id));
        }

        if (cwcs != null) {
          List<WorkChainItem> wcis = getWorkChainItem(cwcs.workChainId);
          for (WorkChainItem wci : wcis) {
            Work wk = getWork(wci.workId);

            ObjectNode jd = Json.newObject();

            jd.put("id"   , wk.workId);
            jd.put("name" , wk.workName);
            jd.put("color" , wk.workColor);

            int chain = 0;
            for (WorkChainItem item : wcis) {
              if (item.nextSequenceId == wci.sequenceId) { //ワークチェーン対象の場合
                chain = 1;
                break;
              }
            }
            jd.put("chain" , chain);

            int end = 0;

            for (Double id : aryWork) {
              if (wk.workId == id.doubleValue()) {
                end = 1;
                break;
              }
            }
            jd.put("end" , end);
            pListJson.add(jd);
          }
        }
      }
      else {
        Logger.error("[ getWorkChainStatusJson NO DATA ]kukaku = {}", pKukakuId);
      }
      return result;
    }

    public int getWorkChainStatusFromMotochoJson(double pKukakuId,CompartmentStatus status, ObjectNode pListJson) {

      int result = GET_SUCCESS;

      List<WorkDiary> wds = WorkDiary.find.where().eq("kukaku_id", pKukakuId).between("work_date", status.katadukeDate, status.finalEndDate).orderBy("work_date").findList();
      if (wds.size() > 0) {
        List<Double> aryWork = new ArrayList<Double>();
        for (WorkDiary wd : wds) {
          aryWork.add(wd.workId);
        }
        CompartmentWorkChainStatus cwcs = getCompartmentWorkChainStatus(pKukakuId);
        if (cwcs != null) {
          List<WorkChainItem> wcis = getWorkChainItem(cwcs.workChainId);
          for (WorkChainItem wci : wcis) {
            Work wk = getWork(wci.workId);

            ObjectNode jd = Json.newObject();

            jd.put("id"   , wk.workId);
            jd.put("name" , wk.workName);
            jd.put("color" , wk.workColor);

            int end = 0;

            for (Double id : aryWork) {
              if (wk.workId == id.doubleValue()) {
                end = 1;
                break;
              }
            }
            jd.put("end" , end);
            pListJson.put(String.valueOf(wk.workId), jd);
          }
        }
      }
      else {
        Logger.error("[ getWorkChainStatusJson NO DATA ]kukaku = {}", pKukakuId);
      }
      return result;
    }

    public int getWorkChainStatusFromMotochoJsonArray(double pKukakuId,CompartmentStatus status, ArrayNode pListJson) {

      int result = GET_SUCCESS;

      List<WorkDiary> wds = WorkDiary.find.where().eq("kukaku_id", pKukakuId).between("work_date", status.katadukeDate, status.finalEndDate).orderBy("work_date").findList();
      if (wds.size() > 0) {
        List<Double> aryWork = new ArrayList<Double>();
        for (WorkDiary wd : wds) {
          aryWork.add(wd.workId);
        }
        CompartmentWorkChainStatus cwcs = getCompartmentWorkChainStatus(pKukakuId);
        if (cwcs != null) {
          List<WorkChainItem> wcis = getWorkChainItem(cwcs.workChainId);
          for (WorkChainItem wci : wcis) {
            Work wk = getWork(wci.workId);

            ObjectNode jd = Json.newObject();

            jd.put("id"   , wk.workId);
            jd.put("name" , wk.workName);
            jd.put("color" , wk.workColor);

            int chain = 0;
            for (WorkChainItem item : wcis) {
              if (item.nextSequenceId == wci.sequenceId) { //ワークチェーン対象の場合
                chain = 1;
                break;
              }
            }
            jd.put("chain" , chain);

            int end = 0;

            for (Double id : aryWork) {
              if (wk.workId == id.doubleValue()) {
                end = 1;
                break;
              }
            }
            jd.put("end" , end);
            pListJson.add(jd);
          }
        }
      }
      else {
        Logger.error("[ getWorkChainStatusJson NO DATA ]kukaku = {}", pKukakuId);
      }
      return result;
    }

    /**
     * 生産者IDから区画情報を取得する
     * @param pFarmId
     * @param pListJson
     * @return
     */
    public static int getCompartmentOfFarmJson(double pFarmId, ObjectNode pListJson) {

      int result  = GET_SUCCESS;

      List<Compartment> compartments = Compartment.getCompartmentOfFarmSort(pFarmId);

      if (compartments.size() > 0) { //該当データが存在する場合
        long idx = 0;
        for (Compartment compartment : compartments) {
          if (compartment.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          ObjectNode jd = Json.newObject();

          jd.put("id"   , compartment.kukakuId);
          jd.put("name" , compartment.kukakuName);
          jd.put("flag" , 0);

          idx++;
          pListJson.put(String.valueOf(idx), jd);
        }
      }
      else {
        result  = GET_ERROR;
      }
      return result;
    }
}
