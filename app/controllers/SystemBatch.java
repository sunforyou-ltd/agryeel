package controllers;

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
import java.util.Hashtable;
import java.util.List;

import models.Account;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Crop;
import models.CropGroup;
import models.CropGroupList;
import models.CropInfoOfFarm;
import models.Farm;
import models.Field;
import models.FieldGroup;
import models.MotochoBase;
import models.Nouhi;
import models.PlanLine;
import models.PosttoPoint;
import models.SsKikiData;
import models.SsKikiMaster;
import models.TimeLine;
import models.Weather;
import models.Work;
import models.WorkDiary;
import models.WorkDiaryDetail;
import models.WorkDiarySanpu;
import models.WorkHistryDetail;
import models.YearInfoOfFarm;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.Analysis;
import compornent.CompartmentStatusCompornent;
import compornent.FarmComprtnent;
import compornent.FieldComprtnent;
import compornent.HashuCompornent;
import compornent.MotochoCompornent;
import compornent.NouhiComprtnent;
import compornent.UserComprtnent;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】システムバッチ
 * @author SunForYou
 *
 */
public class SystemBatch extends Controller {



	public static Result makeCompartmentStatus() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */
        java.sql.Date defaultDate 			= DateU.GetNullDate();

        //----- 作業進捗データを削除する -----
        Ebean.createSqlUpdate("TRUNCATE TABLE compartment_status").execute();
        //----- 圃場リストを取得する -----
        List<Compartment> compartmentList	= Compartment.find.order("kukaku_id").findList();
        //----- 圃場状況照会作業進捗を作成する -----
        for (Compartment compartment : compartmentList) {

        	CompartmentStatus compartmentStatus 		= new CompartmentStatus();
        	compartmentStatus.kukakuId					= compartment.kukakuId;
        	compartmentStatus.rotationSpeedOfYear		= 0;
        	compartmentStatus.hinsyuName				= "";
        	compartmentStatus.hashuDate					= defaultDate;
        	compartmentStatus.seiikuDayCount			= 0;
        	compartmentStatus.nowEndWork				= "";
        	compartmentStatus.finalDisinfectionDate		= defaultDate;
        	compartmentStatus.finalKansuiDate			= defaultDate;
        	compartmentStatus.finalTuihiDate			= defaultDate;
        	compartmentStatus.shukakuStartDate			= defaultDate;
        	compartmentStatus.shukakuEndDate			= defaultDate;
        	compartmentStatus.totalDisinfectionCount	= 0;
        	compartmentStatus.totalKansuiCount			= 0;
        	compartmentStatus.totalTuihiCount			= 0;
        	compartmentStatus.totalShukakuCount			= 0;
        	compartmentStatus.oldDisinfectionCount		= 0;
        	compartmentStatus.oldKansuiCount			= 0;
        	compartmentStatus.oldTuihiCount				= 0;
        	compartmentStatus.oldShukakuCount			= 0;
        	compartmentStatus.nowWorkMode				= 0;
        	compartmentStatus.nextWorkId				= AgryeelConst.WorkInfo.KATADUKE;
        	compartmentStatus.workColor					= "FFFFFF";
        	compartmentStatus.endWorkId					= 0;
        	compartmentStatus.katadukeDate				= defaultDate;
        	compartmentStatus.save();

        }

        return ok(resultJson);
	}


    /**
     * 【AGRYEEL】圃場状況照会 作業進捗データ作成
     * @return
     */
    public static Result makeCompartmentWorkStatus() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 作業進捗データを削除する -----
        Ebean.createSqlUpdate("TRUNCATE TABLE compartment_work_status").execute();

        //----- 作業リストを取得する -----
        List<Work> workList					= Work.find.orderBy("work_id").findList();
        //----- 圃場リストを取得する -----
        List<Compartment> compartmentList	= Compartment.find.order("kukaku_id").findList();
        //----- 圃場状況照会作業進捗を作成する -----
        for (Compartment compartment : compartmentList) {

            for (Work work : workList) {
              //【AICA】TODO:圃場状況照会の見直し
//            	CompartmentWorkStatus compartmentWorkStatus = new CompartmentWorkStatus();
//            	compartmentWorkStatus.kukakuId				= compartment.kukakuId;
//            	compartmentWorkStatus.workId				= work.workId;
//            	compartmentWorkStatus.workEndFlag			= 0;
//            	compartmentWorkStatus.save();

            }

        }

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】圃場状況照会 最終作業終了日を更新する
     * @return
     */
    public static Result updateFinalEndDate() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 圃場状況照会を取得する
        List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.orderBy("kukaku_id").findList();
        for (CompartmentStatus compartmentStatusData : compartmentStatus) {

        	List<models.WorkDiary> workdiary			= models.WorkDiary.find.where("kukaku_id = " + compartmentStatusData.kukakuId + " AND work_id = " + compartmentStatusData.endWorkId).orderBy("work_date desc").findList();
        	for (models.WorkDiary workdiaryData : workdiary) {
        		compartmentStatusData.finalEndDate		= workdiaryData.workDate;
        		compartmentStatusData.save();
        		break;
        	}

        }

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】圃場状況照会 現在の作業記録から圃場状況照会を作成する
     * @return
     */
    public static Result updateCompartmentStatus(double kukakuId) {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        /* 区画状況照会を更新する */
        CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(kukakuId);
        compartmentStatusCompornent.update();

        /* 元帳照会を更新する */
        MotochoCompornent motochoCompornent = new MotochoCompornent(kukakuId);
        motochoCompornent.make();

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】圃場状況照会 アカウントの対象圃場を作成する
     * @return
     */
    public static Result makeTargetCompartment(String accountId) {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        /* アカウントコンポーネント */
        UserComprtnent accountComprtnent = new UserComprtnent();
		accountComprtnent.MakeFieldGroup(accountId);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】タイムライン検索条件
     * @return
     */
    public static Result makeSearchWhere() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

//        //----- 作業進捗データを削除する -----
//        Ebean.createSqlUpdate("TRUNCATE TABLE search_where").execute();
//
//        //----- 作業リストを取得する -----
//        List<Work> workList					= Work.find.orderBy("work_id").findList();
//        //----- 担当者リストを取得する -----
//        List<Account> accountList	= Account.find.order("account_id").findList();
//        //----- タイムライン検索条件を作成する -----
//        for (Account account : accountList) {
//
//            for (Work work : workList) {
//
//            	SearchWhere searchWhere = new SearchWhere();
//            	searchWhere.accountId 	= account.accountId;
//            	searchWhere.workId 		= work.workId;
//            	searchWhere.flag 		= 1;
//
//            	searchWhere.save();
//
//            }
//
//        }

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】区画グループ検索条件
     * @return
     */
    public static Result makeHouseGroupWhere() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 作業進捗データを削除する -----
        Ebean.createSqlUpdate("DELETE FROM house_group_where").execute();

        //----- 担当者リストを取得する -----
        List<Account> accountList	= Account.find.order("account_id").findList();
        //----- タイムライン検索条件を作成する -----
        for (Account account : accountList) {

          //【AICA】TODO:圃場/区画の見直し
          //----- 区画グループリストを取得する -----
//          List<CompartmentGroup> compartmentGroupList					= CompartmentGroup.find.where().eq("farm_id", account.farmId).orderBy("kukaku_group_id").findList();
//          for (CompartmentGroup compartmentGroup : compartmentGroupList) {
//
//          	HouseGroupWhere houseGroupWhere = new HouseGroupWhere();
//          	houseGroupWhere.accountId 		= account.accountId;
//          	houseGroupWhere.kukakuGroupId 	= compartmentGroup.kukakuGroupId;
//          	houseGroupWhere.flag 			= 1;
//
//          	houseGroupWhere.save();
//
//          }
        }

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】タイムライン作業日付更新
     * @return
     */
    public static Result updateTimeLineWorkData() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        List<TimeLine> timeLineList			= TimeLine.find.orderBy("time_line_id").findList();

        for (TimeLine timelineData : timeLineList) {

        	WorkDiary wd = WorkDiary.find.where().eq("work_diary_id", timelineData.workDiaryId).findUnique();

        	if (wd == null) { continue; }

        	timelineData.workDate = wd.workDate;
        	timelineData.update();

        }


        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】元帳照会再生成
     * @return
     */
    public static Result motochoRemake() {
        Logger.info("----------- [SystemBatch] motochoRemake START -----------");
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        List<Compartment> aryCompartment = Compartment.find.orderBy("kukaku_id").findList();

        /*----- 元帳照会を再生成する ----*/

        for (Compartment compartment : aryCompartment) {
          Logger.info("-----> KUKAKU={} Processing", compartment.kukakuId);
          MotochoCompornent motochoCompornent = new MotochoCompornent(compartment.kukakuId);
          motochoCompornent.make();
          CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(compartment.kukakuId, motochoCompornent.lastWorkId);
          compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);

        }

        Logger.info("----------- [SystemBatch] motochoRemake END -----------");
        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】区画状況照会テスト
     * @return
     */
    public static Result kukakuTest(double kukakuId) {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        MotochoCompornent motochoCompornent = new MotochoCompornent(kukakuId);
        motochoCompornent.make();
        CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(kukakuId);
        compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】テストメソッド
     * @return
     */
    public static Result test() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        Farm farm = FarmComprtnent.MakeFarm("kimura@sunforyou.jp", "サンフォーユー牧場");
        FarmComprtnent.MakeAutoHojo(farm.farmId, 26, AgryeelConst.HojoGroupType.ALPHABET, 5);
        resultJson.put("RegistrationCode", farm.registrationCode);


        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】圃場状況照会 作業進捗データに定植を追加
     * @return
     */
    public static Result addTeisyokuWorkStatus() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 圃場リストを取得する -----
        List<Compartment> compartmentList	= Compartment.find.order("kukaku_id").findList();
        //----- 圃場状況照会作業進捗を作成する -----
        for (Compartment compartment : compartmentList) {

          //【AICA】TODO:圃場/区画の見直し
//        	CompartmentWorkStatus compartmentWorkStatus = new CompartmentWorkStatus();
//        	compartmentWorkStatus.kukakuId				= compartment.kukakuId;
//        	compartmentWorkStatus.workId				= AgryeelConst.WorkInfo.TEISYOKU;
//        	compartmentWorkStatus.workEndFlag			= 0;
//        	compartmentWorkStatus.save();

        }

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】タイムライン検索条件に定植を追加
     * @return
     */
    public static Result addTeisyokuSearchWhere() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

//        //----- 担当者リストを取得する -----
//        List<Account> accountList	= Account.find.order("account_id").findList();
//        //----- タイムライン検索条件を作成する -----
//        for (Account account : accountList) {
//
//        	SearchWhere searchWhere = new SearchWhere();
//        	searchWhere.accountId 	= account.accountId;
//        	searchWhere.workId 		= AgryeelConst.WorkInfo.TEISYOKU;
//        	searchWhere.flag 		= 1;
//        	searchWhere.save();
//
//        }

        return ok(resultJson);
    }
	public static Result makeCompartmentALL(double kukakuIdS, double kukakuIdE) {

      ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */
      java.sql.Date defaultDate 			= DateU.GetNullDate();
      double farmId = 0;

      //----- 作業リストを取得する -----
      List<Work> workList					= Work.find.orderBy("work_id").findList();

      //----- 圃場リストを取得する -----
      List<Compartment> compartments	= Compartment.find.where().between("kukaku_id", kukakuIdS, kukakuIdE).order("kukaku_id").findList();
      for (Compartment compartment : compartments) {
        //----- 圃場状況照会作業進捗を作成する -----
      	CompartmentStatus compartmentStatus 		= new CompartmentStatus();
      	compartmentStatus.kukakuId					    = compartment.kukakuId;
      	compartmentStatus.workYear              = 0;
      	compartmentStatus.rotationSpeedOfYear		= 0;
        compartmentStatus.hinsyuId              = "";
        compartmentStatus.cropId                = 0;
      	compartmentStatus.hinsyuName				    = "";
      	compartmentStatus.hashuDate					    = defaultDate;
      	compartmentStatus.seiikuDayCount			  = 0;
      	compartmentStatus.nowEndWork				    = "";
      	compartmentStatus.finalDisinfectionDate	= defaultDate;
      	compartmentStatus.finalKansuiDate			  = defaultDate;
      	compartmentStatus.finalTuihiDate			  = defaultDate;
      	compartmentStatus.shukakuStartDate			= defaultDate;
      	compartmentStatus.shukakuEndDate			  = defaultDate;
        compartmentStatus.totalSolarRadiation   = 0;
      	compartmentStatus.totalDisinfectionCount= 0;
      	compartmentStatus.totalKansuiCount			= 0;
      	compartmentStatus.totalTuihiCount			  = 0;
      	compartmentStatus.totalShukakuCount			= 0;
        compartmentStatus.totalDisinfectionNumber  = 0;
        compartmentStatus.totalKansuiNumber        = 0;
        compartmentStatus.totalTuihiNumber         = 0;
        compartmentStatus.totalShukakuNumber       = 0;
      	compartmentStatus.oldDisinfectionCount		 = 0;
      	compartmentStatus.oldKansuiCount			= 0;
      	compartmentStatus.oldTuihiCount				= 0;
      	compartmentStatus.oldShukakuCount			= 0;
        compartmentStatus.oldSolarRadiation   = 0;
      	compartmentStatus.nowWorkMode				= 0;
      	compartmentStatus.nextWorkId				= AgryeelConst.WorkInfo.KATADUKE;
      	compartmentStatus.workColor					= "FFFFFF";
      	compartmentStatus.endWorkId					= 0;
      	compartmentStatus.katadukeDate				= defaultDate;
      	//----- 追加項目の初期値を設定 -----
        compartmentStatus.hashuCount            = 0;
        compartmentStatus.nowPredictionPoint    = 0;
        compartmentStatus.nowPredictionShukaku  = 0;
        compartmentStatus.predictionShukakuStartDate        = defaultDate;
        compartmentStatus.predictionShukakuRyo  = 0;
        compartmentStatus.pestId                = 0;
        compartmentStatus.pestGeneration        = 0;
        compartmentStatus.pestIntegratedKion    = 0;
        compartmentStatus.prevCalcDate          = defaultDate;
        compartmentStatus.pestPredictDate       = defaultDate;
        compartmentStatus.targetSanpuDate       = defaultDate;
      	compartmentStatus.save();

        farmId = compartment.farmId;

        //----- 元帳照会を作成する -----
        MotochoCompornent motochoCompornent = new MotochoCompornent(compartment.kukakuId);
        motochoCompornent.make();
        CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(compartment.kukakuId);
        compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);

        //-----区画ワークチェイン状況情報を生成する
        CompartmentWorkChainStatus cwcs = new CompartmentWorkChainStatus();
        cwcs.kukakuId     = compartmentStatus.kukakuId;
        cwcs.workChainId  = 0;                          //基礎ワークチェインを設定する
        cwcs.farmId       = farmId;
        cwcs.cropId       = 0;
        cwcs.workEndId    = "";
        cwcs.save();

      }
      return ok(resultJson);
	}
  public static Result calcSolarRadiation() {

    ObjectNode resultJson         = Json.newObject();                             /* 戻り値用JSONオブジェクト */

    //----- 積算温度の算出 -----
    List<CompartmentStatus> compartmentStatuss = CompartmentStatus.find.findList();
    java.sql.Date nullDate     = DateU.GetNullDate();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    for (CompartmentStatus compartmentStatus : compartmentStatuss) {
      if ((compartmentStatus.hashuDate != null)
          && (compartmentStatus.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

        Compartment ct = Compartment.getCompartmentInfo(compartmentStatus.kukakuId);
        if (ct != null) {
          Field fd = ct.getFieldInfo();
          if (fd != null) {
            String pointId = PosttoPoint.getPointId(fd.postNo);
            if (pointId != null && !"".equals(pointId)) {
              java.sql.Date endDate;

              if ((compartmentStatus.shukakuStartDate != null)
                  && (compartmentStatus.shukakuStartDate.compareTo(nullDate) != 0)) { //収穫開始が正しく登録されている場合
                endDate = compartmentStatus.shukakuStartDate;
              }
              else {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -2);
                DateU.setTime(cal, DateU.TimeType.TO);
                endDate = new java.sql.Date(cal.getTime().getTime());
              }

              List<Weather> weathers = Weather.getWeather(pointId, compartmentStatus.hashuDate, endDate);

              double kion = 0;
              for (Weather weather : weathers) {
                kion += weather.kionAve;
              }
              compartmentStatus.totalSolarRadiation = kion;
              compartmentStatus.update();
            }
          }
        }
      }
    }
    return ok(resultJson);
  }
  public static Result useCountUpdate() {

    ObjectNode resultJson         = Json.newObject();                             /* 戻り値用JSONオブジェクト */

    //----- 各生産者単位で農肥の使用回数を算出する -----
    Logger.info("[ USE COUNT UPDATE ] START");
    List<Farm> farms = Farm.find.orderBy("farm_id").findList();
    for (Farm farm : farms) {
      int datacount = NouhiComprtnent.updateUseCount(farm.farmId);
      Logger.info("[ USE COUNT NOUHI  ] FARMID:{} UPDATE:{}", farm.farmId, datacount);
      int datacount2 = HashuCompornent.updateUseCount(farm.farmId);
      Logger.info("[ USE COUNT HINSYU ] FARMID:{} UPDATE:{}", farm.farmId, datacount2);
    }
    Logger.info("[ USE COUNT UPDATE ] END");

    return ok(resultJson);
  }
  public static Result shukakuDataIko() {
    ObjectNode resultJson         = Json.newObject();                             /* 戻り値用JSONオブジェクト */

    //作業から収穫テンプレート分の作業IDを取得する
    List<Work> wks = Work.find.where().eq("work_template_id", AgryeelConst.WorkTemplate.SHUKAKU).orderBy("farm_id, work_id").findList();

    for (Work wk :wks) {
      List<WorkDiary> wds = WorkDiary.find.where().eq("work_id", wk.workId).orderBy("work_diary_id").findList();
      //WorkDiaryの収穫情報をWorkDiaryDetailに移行
      for (WorkDiary wd :wds) {

        Ebean.createSqlUpdate("DELETE FROM work_diary_detail WHERE work_diary_id = :workDiaryId").setParameter("workDiaryId", wd.workDiaryId).execute();

        WorkDiaryDetail wdd   = new WorkDiaryDetail();
        wdd.workDiaryId       = wd.workDiaryId;
        wdd.workDiarySequence = 1;
        wdd.syukakuNisugata   = wd.syukakuNisugata;
        wdd.syukakuSitsu      = wd.syukakuSitsu;
        wdd.syukakuSize       = wd.syukakuSize;
        wdd.syukakuKosu       = 1;
        wdd.shukakuRyo        = wd.shukakuRyo;
        wdd.save();

        List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", wd.kukakuId).findList();
        if (mbs.size() == 0) {
          continue;
        }
        WorkHistryDetail whd   = new WorkHistryDetail();
        whd.farmId            = wk.farmId;
        whd.workId            = wd.workId;
        MotochoBase mb        = mbs.get(0);
        whd.cropId            = mb.cropId;
        whd.workHistrySequence = 1;
        whd.syukakuNisugata   = wd.syukakuNisugata;
        whd.syukakuSitsu      = wd.syukakuSitsu;
        whd.syukakuSize       = wd.syukakuSize;
        whd.syukakuKosu       = 1;
        whd.shukakuRyo        = wd.shukakuRyo;
        Ebean.createSqlUpdate("DELETE FROM work_histry_detail WHERE farm_id = :farmId AND work_id = :workId AND crop_id = :cropId").setParameter("farmId", wk.farmId).setParameter("workId", wk.workId).setParameter("cropId", mb.cropId).execute();
        whd.save();
      }
    }
    return ok(resultJson);
  }
  /**
   * 農肥マスタの散布量を１０ａ換算に設定する
   * @return
   */
  public static Result SanpuryoRecalc() {
    ObjectNode resultJson  = Json.newObject();                             /* 戻り値用JSONオブジェクト */

    List<Nouhi> nouhis = Nouhi.find.orderBy("nouhi_id").findList();
    Hashtable<Double, List<Double>> ht = new Hashtable<Double, List<Double>>();
    List<Farm> farms = Farm.find.orderBy("farm_id").findList();

    for (Farm farm : farms) {
      List<TimeLine> tls = TimeLine.find.where().eq("farm_id", farm.farmId).orderBy("work_date desc").findList();
      List<Double> keys = new ArrayList<Double>();
      for (TimeLine tl : tls) {
        keys.add(tl.workDiaryId);
      }
      ht.put(farm.farmId, keys);
    }

    for (Nouhi nouhi : nouhis) {
      List<WorkDiarySanpu> wdsps = WorkDiarySanpu.find.where().in("work_diary_id", ht.get(nouhi.farmId)).eq("nouhi_id", nouhi.nouhiId).findList();
      for (WorkDiarySanpu wdsp : wdsps) {
        WorkDiary wd = WorkDiary.getWorkDiaryById(wdsp.workDiaryId);
        Compartment cp = Compartment.getCompartmentInfo(wd.kukakuId);
        if (wd != null && cp != null) {
          if (cp.area != 0) {
            double sanpuryo = wdsp.sanpuryo / cp.area * 10;
            int iSanpuryo = (int)Math.ceil(sanpuryo);
            Logger.info("[Nouhi]{} [AREA]{} [SANPURYO]{} [MASTER]{}", nouhi.nouhiId, cp.area, wdsp.sanpuryo, iSanpuryo);
            nouhi.sanpuryo  = iSanpuryo;
            nouhi.update();
            break;
          }
        }
      }
    }

    return ok(resultJson);
  }
  public static Result sortSeq() {
    ObjectNode resultJson  = Json.newObject();

    List<Farm> farms = Farm.find.orderBy("farm_id").findList();
    for (Farm farm : farms) {
      List<FieldGroup> fgs =  FieldGroup.getFieldGroupOfFarm(farm.farmId);
      int seqId = 0;
      for (FieldGroup fg : fgs) {
        seqId++;
        fg.sequenceId = seqId;
        fg.update();

        List<Field> fields = FieldComprtnent.getField(fg.fieldGroupId);
        int seqIdc = 0;
        for (Field field : fields) {
          FieldComprtnent fc = new FieldComprtnent();
          fc.getFileld(field.fieldId);

          List<Compartment> compartments = fc.getCompartmentList();
          for (Compartment compartmentData : compartments) {
            seqIdc++;
            compartmentData.sequenceId = seqIdc;
            compartmentData.update();
          }
        }
      }
    }

    return ok(resultJson);
  }
  public static Result setUuidOfDay(String from, String to) {
    ObjectNode resultJson  = Json.newObject();

    Logger.info("---------------------------------------------------");
    Logger.info("START Method setUuidOfDay.");

    //----- 日付への変換 -----
    SimpleDateFormat dateParse = new SimpleDateFormat("yyyyMMdd");
    java.sql.Date dFrom  = null;
    java.sql.Date dTo    = null;
    try {
      dFrom = new java.sql.Date(dateParse.parse(from).getTime());
    } catch (ParseException e) {
      resultJson.put("result", AgryeelConst.Result.ERROR);
      resultJson.put("message", "期間指定（自）に誤りがあります。 FROM = " + from);
      return ok(resultJson);
    }
    try {
      dTo = new java.sql.Date(dateParse.parse(to).getTime());
    } catch (ParseException e) {
      resultJson.put("result", AgryeelConst.Result.ERROR);
      resultJson.put("message", "期間指定（至）に誤りがあります。 TO   = " + to);
      return ok(resultJson);
    }
    //----- 日数差分の算出-----
    long dayCount = DateU.GetDiffDate(dFrom, dTo);
    //----- 生産者の取得 -----
    List<Farm> farms = Farm.find.orderBy("farm_id").findList();
    for (Farm farm : farms) {
      Logger.info("[Farm] farmId={}", farm.farmId);
      //----- アカウントの取得 -----
      List<Account> acs = Account.getAccountOfFarm(farm.farmId);
      for (Account ac : acs) {
        //----- FROMから日数差分分繰り返す-----
        Logger.info("[Account] AccountId={}", ac.accountId);
        Calendar syscal = Calendar.getInstance();
        syscal.setTime(dFrom);

        for (int i = 0; i < dayCount; i++) {
          int uuid = 0;
          //----- 該当日のPlanLineを取得 -----
          List<PlanLine> pls = PlanLine.getPlanLineOfAccount(ac.accountId, ac.farmId, new java.sql.Date(syscal.getTimeInMillis()), new java.sql.Date(syscal.getTimeInMillis()));
          //----- uuidを設定する -----
          for (PlanLine pl : pls) {
            uuid++;
            pl.uuidOfDay = uuid;
            pl.update();
          }
          //----- 該当日のTimeLineを取得 -----
          List<TimeLine> tls = TimeLine.getTimeLineOfAccount(ac.accountId, ac.farmId, new java.sql.Date(syscal.getTimeInMillis()), new java.sql.Date(syscal.getTimeInMillis()));
          //----- uuidを設定する -----
          for (TimeLine tl : tls) {
            uuid++;
            tl.uuidOfDay = uuid;
            tl.update();
          }
          syscal.add(Calendar.DAY_OF_MONTH, 1);
        }
      }
    }

    return ok(resultJson);
  }
  public static Result execInfoOfFarm(int year) {
    ObjectNode resultJson  = Json.newObject();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    Logger.info("---------------------------------------------------");
    Logger.info("START Method execInfoOfFarm.");

    List<Farm> farms = Farm.find.orderBy("farm_id").findList();
    for (Farm farm : farms) {
      Logger.info("[Farm] farmId={}", farm.farmId);
      Calendar from = Calendar.getInstance();
      Calendar to = Calendar.getInstance();
      from.set(year, 0, 1);
      to.set(year, 11, 31);
      Analysis shukaku = new Analysis();
      Analysis time = new Analysis();
      Analysis pshukaku = new Analysis();
      Analysis ptime = new Analysis();

      //月別
      for (int month = 0; month < 12; month++) {
        shukaku.add(month);
        time.add(month);
        pshukaku.add(month);
        ptime.add(month);
      }

      //品目別
      Analysis shukakuc = new Analysis();
      Analysis timec = new Analysis();
      Analysis pshukakuc = new Analysis();
      Analysis ptimec = new Analysis();
      List<CropGroup> cgs = CropGroup.getCropGroupOfFarm(farm.farmId);
      if (cgs.size() > 0) {
        List<Crop> cgls = CropGroupList.getCrop(cgs.get(0).cropGroupId);
        for (Crop crop : cgls) {
          shukakuc.add(crop.cropId);
          timec.add(crop.cropId);
          pshukakuc.add(crop.cropId);
          ptimec.add(crop.cropId);
        }
      }

      //----- 今年 -----
      List<TimeLine> timelines = TimeLine.getTimeLineOfFarm(farm.farmId, new java.sql.Date(from.getTimeInMillis()), new java.sql.Date(to.getTimeInMillis()));

      Logger.info("START={} END={}.", sdf.format(from.getTime()), sdf.format(to.getTime()));
      Logger.info("NOW CONUT={}.", timelines.size());
      for (TimeLine data : timelines) {
        WorkDiary wd = WorkDiary.getWorkDiaryById(data.workDiaryId);
        if (wd != null) {
          Calendar date = Calendar.getInstance();
          date.setTimeInMillis(data.workDate.getTime());
          shukaku.put(date.get(Calendar.MONTH), wd.shukakuRyo);
          time.put(date.get(Calendar.MONTH), wd.workTime);
          List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", wd.kukakuId).le("work_start_day", wd.workDate).ge("work_end_day", wd.workDate).findList();
          for (MotochoBase mb : mbs) {
            shukakuc.put(mb.cropId, wd.shukakuRyo);
            timec.put(mb.cropId, wd.workTime);
            break;
          }
        }
      }

      //----- 昨年 -----
      from.add(Calendar.YEAR, -1);
      to.add(Calendar.YEAR, -1);

      timelines = TimeLine.getTimeLineOfFarm(farm.farmId, new java.sql.Date(from.getTimeInMillis()), new java.sql.Date(to.getTimeInMillis()));

      Logger.info("START={} END={}.", sdf.format(from.getTime()), sdf.format(to.getTime()));
      Logger.info("PREV CONUT={}.", timelines.size());
      for (TimeLine data : timelines) {
        WorkDiary wd = WorkDiary.getWorkDiaryById(data.workDiaryId);
        if (wd != null) {
          Calendar date = Calendar.getInstance();
          date.setTimeInMillis(data.workDate.getTime());
          pshukaku.put(date.get(Calendar.MONTH), wd.shukakuRyo);
          ptime.put(date.get(Calendar.MONTH), wd.workTime);
          List<MotochoBase> mbs = MotochoBase.find.where().eq("kukaku_id", wd.kukakuId).le("work_start_day", wd.workDate).ge("work_end_day", wd.workDate).findList();
          for (MotochoBase mb : mbs) {
            pshukakuc.put(mb.cropId, wd.shukakuRyo);
            ptimec.put(mb.cropId, wd.workTime);
            break;
          }
        }
      }

      Ebean.createSqlUpdate("DELETE FROM year_info_of_farm WHERE farm_id = :farmID AND year = :YEAR").setParameter("farmID", farm.farmId).setParameter("YEAR", year).execute();
      for (int month = 0; month < 12; month++) {
        YearInfoOfFarm yof = new YearInfoOfFarm();
        yof.farmId = farm.farmId;
        yof.year = (short) year;
        yof.month = (short) (month + 1);
        yof.shukakuRyo = (double)Math.round(shukaku.data(month) * 10) /10 ;
        yof.shukakuRyoPrev = (double)Math.round(pshukaku.data(month) * 10) /10;
        yof.workTimeMonth = ((double)Math.round((time.data(month) / 60) * 10)) /10;
        yof.workTimeMonthPrev = ((double)Math.round((ptime.data(month) / 60) * 10)) /10;
        yof.save();
      }
      Ebean.createSqlUpdate("DELETE FROM crop_info_of_farm WHERE farm_id = :farmID AND year = :YEAR").setParameter("farmID", farm.farmId).setParameter("YEAR", year).execute();
      if (cgs.size() > 0) {
        List<Crop> cgls = CropGroupList.getCrop(cgs.get(0).cropGroupId);
        for (Crop crop : cgls) {
          CropInfoOfFarm cof = new CropInfoOfFarm();
          cof.farmId = farm.farmId;
          cof.year = (short) year;
          cof.cropId = crop.cropId;
          cof.shukakuRyo = (double)Math.round(shukakuc.data(crop.cropId) * 10) /10 ;
          cof.shukakuRyoPrev = (double)Math.round(pshukakuc.data(crop.cropId) * 10) /10;
          cof.workTimeMonth = ((double)Math.round((timec.data(crop.cropId) / 60) * 10)) /10;
          cof.workTimeMonthPrev = ((double)Math.round((ptimec.data(crop.cropId) / 60) * 10)) /10;
          cof.save();
        }
      }
    }

    return ok(resultJson);
  }
  public static Result getSensProutData(String pDate) {
    ObjectNode resultJson  = Json.newObject();
    Logger.info("---------- getSenSprout START ----------");
    List<SsKikiMaster> skms = SsKikiMaster.find.where().ne("kukaku_id", 0).findList();
    Logger.info("MASTER RECORD = {}", skms.size());

    for (SsKikiMaster skm :skms) {
      Logger.info("TARTGET FARM[{}] NODE[{}] ", skm.farmId, skm.nodeId);
      String filename = AgryeelConst.SenSprout.DLF + "data_" + skm.nodeId + ".csv";
      ProcessBuilder p = new ProcessBuilder(AgryeelConst.SenSprout.PYTHON
                                            , AgryeelConst.SenSprout.EXECMDL, skm.nodeId, skm.userId, skm.password, "-f" + filename, "-s" + pDate, "-e" + pDate);
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
    return ok(resultJson);
  }
}
