package compornent;

import java.util.ArrayList;
import java.util.List;

import models.Attachment;
import models.Belto;
import models.BeltoOfFarm;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Crop;
import models.Hinsyu;
import models.HinsyuOfFarm;
import models.Kiki;
import models.Nouhi;
import models.TimeLine;
import models.Work;
import models.WorkDiary;
import models.WorkLastTime;
import models.WorkPlan;
import play.Logger;
import play.libs.Json;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】作業記録作業別播種
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class HashuCompornent extends CommonWorkDiaryWork {

    /**
     * 株間
     */
    public double kabuma = 0;
    /**
     * 条間
     */
    public double joukan = 0;
    /**
     * 条数
     */
    public double jousu  = 0;
    /**
     * 深さ
     */
    public double hukasa = 0;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * アタッチメントID
     */
    public double attachmentId;
    /**
     * 品種ID
     */
    public String hinsyuId;
    /**
     * ベルトID
     */
    public double beltoId;
    /**
     * 生産物ID
     */
    public double cropId;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public HashuCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public HashuCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        String	hinsyuName		= "未選択";
        String	kikiName		= "未選択";
        String	attachmentName	= "選択不可";
        String	beltName		= "未選択";
        String  cropName    = "未選択";

        //【AICA】TODO:コンビネーションはワークチェーンの散布情報組み合わせに移行する
        //getCombiList(Combi.ConstKind.HASHU);						//コンビネーション
        getKikiList();												//機器
        //getAttachmentList();										//アタッチメント
        getHinsyuList();											//種
        getBeltoList();												//ベルト
        getSanpuList();												//散布方法
        getNouhiList(Nouhi.ConstKind.HIRYO);						//肥料

        /* 播種情報の取得 */
        double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
        CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();

        this.cropId = cws.cropId; //生産物ID
        Crop crop = CropComprtnent.getCropById(this.cropId);
        if (crop != null) {
          cropName = crop.cropName;
        }

        WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
        if (this.workDiary != null) {	/* 作業記録編集の場合 */

            kabuma 			= this.workDiary.kabuma;					//株間
            joukan 			= this.workDiary.joukan;					//条間
            jousu  			= this.workDiary.jousu;						//条数
            hukasa 			= this.workDiary.hukasa;					//深さ
            kikiId 			= this.workDiary.kikiId;					//機器
            attachmentId 	= this.workDiary.attachmentId;	//アタッチメント
            hinsyuId  	= this.workDiary.hinsyuId;				//品種
            beltoId 		= this.workDiary.beltoId;					//ベルト

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          kabuma      = this.workPlan.kabuma;          //株間
          joukan      = this.workPlan.joukan;          //条間
          jousu       = this.workPlan.jousu;           //条数
          hukasa      = this.workPlan.hukasa;          //深さ
          kikiId      = this.workPlan.kikiId;          //機器
          attachmentId  = this.workPlan.attachmentId;  //アタッチメント
          hinsyuId    = this.workPlan.hinsyuId;        //品種
          beltoId     = this.workPlan.beltoId;         //ベルト

        }
        else {
        if (workModel != null) {

            kabuma 			= workModel.kabuma;						//株間
            joukan 			= workModel.joukan;						//条間
            jousu  			= workModel.jousu;						//条数
            hukasa 			= workModel.hukasa;						//深さ
            kikiId 			= workModel.kikiId;						//機器
            attachmentId 	= workModel.attachmentId;				//アタッチメント
            hinsyuId  	= workModel.hinsyuId;					//品種
            beltoId 		= workModel.beltoId;					//ベルト

        }
        else {

            kabuma 			= 0;									//株間
            joukan 			= 0;									//条間
            jousu  			= 0;									//条数
            hukasa 			= 0;									//深さ
            kikiId 			= 0;									//機器
            attachmentId 	= 0;									//アタッチメント
            hinsyuId  		= "";								//品種
            beltoId 		= 0;									//ベルト

        }
        }

        if (!"".equals(hinsyuId)) {

        	hinsyuName		= Hinsyu.getMultiHinsyuName(hinsyuId);

        }
        else {
          hinsyuName  = "未選択";
        }
        if (kikiId != 0) {

    		kikiName		= Kiki.getKikiName(kikiId);
            if (attachmentId != 0) {

        		attachmentName	= Attachment.getAttachmentName(attachmentId);

            }
            else {

        		attachmentName	= "未選択";

            }

        }
        if (beltoId != 0) {

        	beltName		= Belto.getBeltoName(beltoId);

        }
        else {
          beltName  = "未選択";
        }

        resultJson.put("kabuma"			, kabuma);							//株間
        resultJson.put("joukan"			, joukan);							//条間
        resultJson.put("jousu"			, jousu);							  //条数
        resultJson.put("hukasa"			, hukasa);							//深さ
        resultJson.put("kikiId"			, kikiId);							//機器
        resultJson.put("kikiSpan"		, kikiName);						//機器
        resultJson.put("attachmentId"	, attachmentId);			//アタッチメント
        resultJson.put("attachmentSpan"	, attachmentName);	//アタッチメント
        resultJson.put("hinsyuId"		, hinsyuId);						//品種
        resultJson.put("hinsyuSpan"		, hinsyuName);				//品種
        resultJson.put("beltoId"		, beltoId);							//ベルト
        resultJson.put("beltoSpan"		, beltName);					//ベルト
        resultJson.put("crop"       , this.cropId);         //生産物ID
        resultJson.put("cropSpan"   , cropName);            //生産物

    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.kikiId        = this.wlt.kikiId;
        wkd.attachmentId  = this.wlt.attachmentId;
        wkd.hinsyuId      = this.wlt.hinsyuId;
        wkd.beltoId       = this.wlt.beltoId;
        wkd.kabuma        = this.wlt.kabuma;
        wkd.joukan        = this.wlt.joukan;
        wkd.jousu         = this.wlt.jousu;
        wkd.hukasa        = this.wlt.hukasa;
      }
      else {
        if ("".equals(input.get("kiki").asText())) {
          wkd.kikiId        = 0;
        }
        else {
          wkd.kikiId        = Double.parseDouble(input.get("kiki").asText());       //機器
        }
        if ("".equals(input.get("attachment").asText())) {
          wkd.attachmentId  = 0;
        }
        else {
          wkd.attachmentId  = Double.parseDouble(input.get("attachment").asText()); //アタッチメント
        }

        wkd.hinsyuId      = input.get("hinsyu").asText();                         //品種（複数品種化の為、テキスト値をそのまま格納する）
        if ("".equals(input.get("belto").asText())) {
          wkd.beltoId       = 0;
        }
        else {
          wkd.beltoId       = Double.parseDouble(input.get("belto").asText());      //ベルト
        }
        wkd.kabuma        = Double.parseDouble(input.get("kabuma").asText());     //株間
        wkd.joukan        = Double.parseDouble(input.get("joukan").asText());     //条間
        wkd.jousu         = Double.parseDouble(input.get("jousu").asText());      //条数
        wkd.hukasa        = Double.parseDouble(input.get("hukasa").asText());     //深さ
      }

      this.wlt.kikiId 			  = wkd.kikiId;											//機器
      this.wlt.attachmentId   = wkd.attachmentId;							  //アタッチメント
      this.wlt.hinsyuId 		  = wkd.hinsyuId;										//品種
      this.wlt.beltoId 			  = wkd.beltoId;										//ベルト
      this.wlt.kabuma 			  = wkd.kabuma;											//株間
      this.wlt.joukan 			  = wkd.joukan;											//条間
      this.wlt.jousu 			    = wkd.jousu;											//条数
      this.wlt.hukasa 			  = wkd.hukasa;											//深さ
      this.wlt.update();

    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      if ("".equals(input.get("kiki").asText())) {
        wkp.kikiId        = 0;
      }
      else {
        wkp.kikiId        = Double.parseDouble(input.get("kiki").asText());       //機器
      }
      if ("".equals(input.get("attachment").asText())) {
        wkp.attachmentId  = 0;
      }
      else {
        wkp.attachmentId  = Double.parseDouble(input.get("attachment").asText()); //アタッチメント
      }
      wkp.hinsyuId      = input.get("hinsyu").asText();                         //品種（複数品種化の為、テキスト値をそのまま格納する）
      if ("".equals(input.get("belto").asText())) {
        wkp.beltoId       = 0;
      }
      else {
        wkp.beltoId       = Double.parseDouble(input.get("belto").asText());      //ベルト
      }
      wkp.kabuma        = Double.parseDouble(input.get("kabuma").asText());     //株間
      wkp.joukan        = Double.parseDouble(input.get("joukan").asText());     //条間
      wkp.jousu         = Double.parseDouble(input.get("jousu").asText());      //条数
      wkp.hukasa        = Double.parseDouble(input.get("hukasa").asText());     //深さ

    }
    public static int getHinsyuOfCropToCropJson(double pFarmId, double pCropId, ObjectNode pListJson) {

      List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(pFarmId);
      List<Double>hinsyus = new ArrayList<Double>();
      for (HinsyuOfFarm hof : hofs) {
        hinsyus.add(hof.hinsyuId);
      }
      //----- 対象機器の取得 -----
      List<Hinsyu> hinsyuList = Hinsyu.find.where().in("hinsyu_id", hinsyus).eq("crop_id", pCropId).order("hinsyu_id ASC").findList();
      for (Double key : hinsyus) {
        for (Hinsyu hinsyu : hinsyuList) {
          if (key.doubleValue() == hinsyu.hinsyuId) {
            ObjectNode jd = Json.newObject();

            jd.put("id"   , hinsyu.hinsyuId);
            jd.put("name" , hinsyu.hinsyuName);

            pListJson.put(String.valueOf(hinsyu.hinsyuId), jd);
            break;
          }
        }
      }
      return GET_SUCCESS;
    }
    public static int getHinsyuOfCropJson(double pFarmId, double pKukakuId, ObjectNode pListJson) {

      //----- 区画ワークチェーン状況を取得 -----
      CompartmentWorkChainStatus cwcs = WorkChainCompornent.getCompartmentWorkChainStatus(pKukakuId);
      if (cwcs != null) {
        //----- 生産者別機器情報を取得 -----
        List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(pFarmId);
        List<Double>hinsyus = new ArrayList<Double>();
        for (HinsyuOfFarm hof : hofs) {
          hinsyus.add(hof.hinsyuId);
        }
        //----- 対象機器の取得 -----
        Logger.info("[ GET HINSYU ] CROPID={}", cwcs.cropId);
        List<Hinsyu> hinsyuList = Hinsyu.find.where().in("hinsyu_id", hinsyus).eq("crop_id", cwcs.cropId).order("hinsyu_id ASC").findList();
        for (Double key : hinsyus) {
          for (Hinsyu hinsyu : hinsyuList) {
            if (key.doubleValue() == hinsyu.hinsyuId) {
              ObjectNode jd = Json.newObject();

              jd.put("id"   , hinsyu.hinsyuId);
              jd.put("name" , hinsyu.hinsyuName);

              pListJson.put(String.valueOf(hinsyu.hinsyuId), jd);
              break;
            }
          }
        }
      }
      return GET_SUCCESS;
    }
    public static int getHinsyuOfFarmJson(double pFarmId, ObjectNode pListJson) {

      //----- 区画ワークチェーン状況を取得 -----
      //----- 生産者別機器情報を取得 -----
      List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(pFarmId);
      List<Double>hinsyus = new ArrayList<Double>();
      for (HinsyuOfFarm hof : hofs) {
        if (hof.deleteFlag == 1) { // 削除済みの場合
          continue;
        }
        hinsyus.add(hof.hinsyuId);
      }
      //----- 対象機器の取得 -----
      List<Hinsyu> hinsyuList = Hinsyu.find.where().in("hinsyu_id", hinsyus).order("hinsyu_id ASC").findList();
      for (Double key : hinsyus) {
        for (Hinsyu hinsyu : hinsyuList) {
          if (key.doubleValue() == hinsyu.hinsyuId) {
            ObjectNode jd = Json.newObject();

            jd.put("id"   , hinsyu.hinsyuId);
            jd.put("name" , hinsyu.hinsyuName);

            pListJson.put(String.valueOf(hinsyu.hinsyuId), jd);
            break;
          }
        }
      }
      return GET_SUCCESS;
    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);

      this.wlt.kikiId         = wkd.kikiId;                     //機器
      this.wlt.attachmentId   = wkd.attachmentId;               //アタッチメント
      this.wlt.hinsyuId       = wkd.hinsyuId;                   //品種
      this.wlt.beltoId        = wkd.beltoId;                    //ベルト
      this.wlt.kabuma         = wkd.kabuma;                     //株間
      this.wlt.joukan         = wkd.joukan;                     //条間
      this.wlt.jousu          = wkd.jousu;                      //条数
      this.wlt.hukasa         = wkd.hukasa;                     //深さ
      this.wlt.update();

    }
    public static int getBeltoOfFarmJson(double pFarmId, ObjectNode pListJson) {

      //----- 生産者別機器情報を取得 -----
      List<BeltoOfFarm> bofs = BeltoOfFarm.getBeltoOfFarm(pFarmId);
      List<Double>beltos = new ArrayList<Double>();
      for (BeltoOfFarm bof : bofs) {
        if (bof.deleteFlag == 1) { // 削除済みの場合
          continue;
        }
        beltos.add(bof.beltoId);
      }
      //----- 対象機器の取得 -----
      List<Belto> beltoList = Belto.find.where().in("belto_id", beltos).order("belto_id").findList();
      for (Belto belto : beltoList) {

        ObjectNode jd = Json.newObject();

        jd.put("id"   , belto.beltoId);
        jd.put("name" , belto.beltoName);

        pListJson.put(String.valueOf(belto.beltoId), jd);

      }
      return GET_SUCCESS;
    }
    public static int updateUseCount(double pFarmId) {
      int result = 0;

      //区画から作業記録を取得するように変更
/*
      //該当生産者の作業記録を絞り込む
      List<TimeLine> tls = TimeLine.find.where().eq("farm_id", pFarmId).findList();
      List<Double>wdkey = new ArrayList<Double>();
      for (TimeLine tl : tls) {
        wdkey.add(tl.workDiaryId);
      }
*/
      //該当生産者の区画を取得
      List<Double>cpkey = Compartment.getKukakuIdOfFarm(pFarmId);

      //該当生産者の品種を絞り込む
      List<HinsyuOfFarm> hofs = HinsyuOfFarm.getHinsyuOfFarm(pFarmId);
      //List<WorkDiary> wdss = WorkDiary.find.where().in("work_diary_id", wdkey).findList();
      List<WorkDiary> wdss = WorkDiary.find.where().in("kukaku_id", cpkey).findList();
      for (WorkDiary wds : wdss) {

        if (wds.hinsyuId == null || "".equals(wds.hinsyuId) || "0".equals(wds.hinsyuId)) {
          continue;
        }
        String[] hinsyus = wds.hinsyuId.split(",");
        for (String hinsyu :hinsyus) {
          for (HinsyuOfFarm hof : hofs) {
            if (Double.parseDouble(hinsyu) == hof.hinsyuId) {
              hof.useCount++;
              hof.update();
              result++;
            }
          }
        }
      }
      return result;

    }
}
