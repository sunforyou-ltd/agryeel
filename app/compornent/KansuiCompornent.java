package compornent;

import models.Common;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Kiki;
import models.Work;
import models.WorkDiary;
import models.WorkLastTime;
import models.WorkPlan;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】作業記録作業別潅水
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class KansuiCompornent extends CommonWorkDiaryWork {

    /**
     * 潅水方法
     */
    public int kansuiMethod;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * 潅水量
     */
    public double kansuiryo = 0;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public KansuiCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public KansuiCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        String	kansuiName		= "未選択";
        String	kikiName		  = "未選択";

        /* 潅水量の取得 */
        double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
        CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
        WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);

        if (this.workDiary != null) {	/* 作業記録編集の場合 */

        	kansuiMethod 	= this.workDiary.kansuiMethod;				//潅水方法
          kansuiryo 		= this.workDiary.kansuiRyo;					//潅水量
          kikiId 			= this.workDiary.kikiId;					//機器

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          kansuiMethod  = this.workPlan.kansuiMethod;        //潅水方法
          kansuiryo     = this.workPlan.kansuiRyo;         //潅水量
          kikiId      = this.workPlan.kikiId;          //機器

        }
        else {

            if (workModel != null) {

              kansuiryo 		= workModel.kansuiRyo;
              kansuiMethod 	= workModel.kansuiMethod;
              kikiId 			  = workModel.kikiId;

            }
            else {

              kansuiryo = 0;
              kansuiMethod 	= 0;
              kikiId 			= 0;

            }

        }

        if (kikiId != 0) {

          kikiName		= Kiki.getKikiName(kikiId);

        }
        if (kansuiMethod != 0) {

        	kansuiName		= Common.GetCommonValue(Common.ConstClass.KANSUI, kansuiMethod);

        }

        resultJson.put("kansuiMethod"	, kansuiMethod);					//潅水方法
        resultJson.put("kansuiSpan"		, kansuiName);						//潅水方法
        resultJson.put("kikiId"			  , kikiId);							//機器
        resultJson.put("kikiSpan"		  , kikiName);						//機器
        resultJson.put("kansuiryo"	  , kansuiryo);							//潅水量

    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.kansuiMethod  = this.wlt.kansuiMethod;
        wkd.kikiId        = this.wlt.kikiId;
        wkd.kansuiRyo     = this.wlt.kansuiRyo;
      }
      else {
        if ("".equals(input.get("kansui").asText())) {
          wkd.kansuiMethod  = 0;
        }
        else {
          wkd.kansuiMethod  = Integer.parseInt(input.get("kansui").asText());     //潅水方法
        }
        if ("".equals(input.get("kiki").asText())) {
          wkd.kikiId      = 0;
        }
        else {
          wkd.kikiId      = Double.parseDouble(input.get("kiki").asText());   //機器
        }
        wkd.kansuiRyo   = Double.parseDouble(input.get("kansuiryo").asText());    //潅水量
      }
      this.wlt.kansuiMethod = wkd.kansuiMethod;               //潅水方法
      this.wlt.kikiId       = wkd.kikiId;                     //機器
      this.wlt.kansuiRyo    = wkd.kansuiRyo;                  //潅水量
      this.wlt.update();

    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);
      if ("".equals(input.get("kansui").asText())) {
        wkp.kansuiMethod  = 0;
      }
      else {
        wkp.kansuiMethod  = Integer.parseInt(input.get("kansui").asText());     //潅水方法
      }
      if ("".equals(input.get("kiki").asText())) {
        wkp.kikiId      = 0;
      }
      else {
        wkp.kikiId      = Double.parseDouble(input.get("kiki").asText());   //機器
      }
      wkp.kansuiRyo   = Double.parseDouble(input.get("kansuiryo").asText());    //潅水量

    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);

      this.wlt.kansuiMethod = wkd.kansuiMethod;               //潅水方法
      this.wlt.kikiId       = wkd.kikiId;                     //機器
      this.wlt.kansuiRyo    = wkd.kansuiRyo;                  //潅水量
      this.wlt.update();

    }
}
