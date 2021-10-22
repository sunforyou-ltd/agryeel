package compornent;

import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Work;
import models.WorkDiary;
import models.WorkLastTime;
import models.WorkPlan;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】作業記録作業別マルチ
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class MultiCompornent extends CommonWorkDiaryWork {

    /**
     * 使用マルチ
     */
    public double useMulti;
    /**
     * 列数
     */
    public double retusu = 0;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public MultiCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public MultiCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        String	useMultiName		= "未選択";

        /* マルチ情報の取得 */
        double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
        CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
        WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
        if (this.workDiary != null) {	/* 作業記録編集の場合 */

          useMulti 			= this.workDiary.useMulti;				//使用マルチ
          retusu 			  = this.workDiary.retusu;					//列数

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          useMulti      = this.workPlan.useMulti;        //使用マルチ
          retusu        = this.workPlan.retusu;          //列数

        }
        else {
          if (workModel != null) {

            useMulti 			= workModel.useMulti;				//使用マルチ
            retusu 			  = workModel.retusu;					//列数

          }
          else {

            useMulti 			= 0;									//使用マルチ
            retusu 			  = 0;									//列数

          }
        }

        useMultiName = "テストマルチ";

//        if (useMulti != 0) {
//
//        	hinsyuName		= Hinsyu.getHinsyuName(hinsyuId);
//
//        }
//        else {
//          hinsyuName  = "未選択";
//        }
        resultJson.put("retusu"			  , retusu);							//列数
        resultJson.put("useMulti"			, useMulti);						//使用マルチ
        resultJson.put("useMultiSpan"	, useMultiName);				//マルチ名

    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.retusu        = this.wlt.retusu;
        wkd.useMulti      = this.wlt.useMulti;
      }
      else {
        wkd.retusu        = Double.parseDouble(input.get("retusu").asText());       //列数
        if("".equals(input.get("useMulti").asText())) {
          wkd.useMulti      = 0;
        }
        else {
          wkd.useMulti      = Double.parseDouble(input.get("useMulti").asText());   //使用マルチ
        }
      }

      this.wlt.retusu 			  = wkd.retusu;											//列数
      this.wlt.useMulti       = wkd.useMulti;							      //使用マルチ
      this.wlt.update();

    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      wkp.retusu        = Double.parseDouble(input.get("retusu").asText());     //列数
      wkp.useMulti      = Double.parseDouble(input.get("useMulti").asText());   //使用マルチ

    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);
      this.wlt.retusu         = wkd.retusu;                     //列数
      this.wlt.useMulti       = wkd.useMulti;                   //使用マルチ
      this.wlt.update();

    }
}
