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
 * 【AGRYEEL】作業記録作業別定植
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class TeishokuCompornent extends CommonWorkDiaryWork {

    /**
     * 使用苗枚数
     */
    public int naemaisu;
    /**
     * 列数
     */
    public double retusu = 0;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public TeishokuCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public TeishokuCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        /* 定植情報の取得 */
        double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
        CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
        WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
        if (this.workDiary != null) {	/* 作業記録編集の場合 */

          naemaisu 			= this.workDiary.naemaisu;				//使用苗枚数
          retusu 			  = this.workDiary.retusu;					//列数

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          naemaisu      = this.workPlan.naemaisu;        //使用苗枚数
          retusu        = this.workPlan.retusu;          //列数

        }
        else {
          if (workModel != null) {

            naemaisu 			= workModel.naemaisu;				//使用苗枚数
            retusu 			  = workModel.retusu;					//列数

          }
          else {

            naemaisu 			= 0;									//使用苗枚数
            retusu 			  = 0;									//列数

          }
        }

        resultJson.put("retusu"			  , retusu);							//列数
        resultJson.put("naemaisu"			, naemaisu);						//使用苗枚数

    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.retusu        = this.wlt.retusu;
        wkd.naemaisu      = this.wlt.naemaisu;
      }
      else {
        wkd.retusu        = Double.parseDouble(input.get("retusu").asText());     //列数
        wkd.naemaisu      = Integer.parseInt(input.get("naemaisu").asText());     //使用苗枚数
      }

      this.wlt.retusu 			  = wkd.retusu;											//列数
      this.wlt.naemaisu       = wkd.naemaisu;							      //使用苗枚数
      this.wlt.update();

    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      wkp.retusu        = Double.parseDouble(input.get("retusu").asText());     //列数
      wkp.naemaisu      = Integer.parseInt(input.get("naemaisu").asText());     //使用苗枚数

    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);
      this.wlt.retusu         = wkd.retusu;                     //列数
      this.wlt.naemaisu       = wkd.naemaisu;                   //使用苗枚数
      this.wlt.update();

    }
}
=======
package compornent;

import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Hinsyu;
import models.Work;
import models.WorkDiary;
import models.WorkLastTime;
import models.WorkPlan;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】作業記録作業別定植
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class TeishokuCompornent extends CommonWorkDiaryWork {

    /**
     * 品種ID
     */
    public String hinsyuId;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public TeishokuCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public TeishokuCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        String	hinsyuName		= "未選択";

        /* 定植情報の取得 */
        double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
        CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
        WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
        if (this.workDiary != null) {	/* 作業記録編集の場合 */
          hinsyuId  	= this.workDiary.hinsyuId;				//品種
          getWorkDiaryDetail(this.workDiary.workDiaryId);

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          hinsyuId    = this.workPlan.hinsyuId;					//品種
          getWorkPlanDetail(this.workPlan.workPlanId);

        }
        else {
            hinsyuId  		= "";								//品種
          //前回作業情報より取得する
          getWorkHistryDetail(this.workId);

        }

        if (!"".equals(hinsyuId)) {
        	hinsyuName		= Hinsyu.getMultiHinsyuName(hinsyuId);
        }
        else {
          hinsyuName  = "未選択";
        }

        resultJson.put("hinsyuId"		, hinsyuId);			//品種
        resultJson.put("hinsyuSpan"		, hinsyuName);			//品種

    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.hinsyuId      = this.wlt.hinsyuId;
      }
      else {
        wkd.hinsyuId      = input.get("hinsyu").asText();
      }

      this.wlt.hinsyuId 		  = wkd.hinsyuId;				//品種
      this.wlt.update();
      commitDetailInfo(input, wkd, wk);

    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      wkp.hinsyuId      = input.get("hinsyu").asText();			//品種
      planDetailInfo(input, wkp, wk);

    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);
      this.wlt.hinsyuId       = wkd.hinsyuId;                   //品種
      this.wlt.update();

    }
}
