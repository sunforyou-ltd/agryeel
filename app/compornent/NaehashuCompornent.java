package compornent;

import models.Common;
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
 * 【AGRYEEL】作業記録作業別苗播種
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NaehashuCompornent extends CommonWorkDiaryWork {

    /**
     * 使用培土
     */
    public double useBaido;
    /**
     * 枚数
     */
    public int maisu = 0;
    /**
     * 使用穴数
     */
    public int useHole = 0;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public NaehashuCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public NaehashuCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        String	useBaidoName		= "未選択";

        /* マルチ情報の取得 */
        double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
        CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
        WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
        if (this.workDiary != null) {	/* 作業記録編集の場合 */

          useBaido 			= this.workDiary.useBaido;			//使用培土
          maisu 			  = this.workDiary.maisu;					//枚数
          useHole       = this.workDiary.useHole;       //使用穴数

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          useBaido      = this.workPlan.useBaido;      //使用培土
          maisu         = this.workPlan.maisu;         //枚数
          useHole       = this.workPlan.useHole;       //使用穴数

        }
        else {
          if (workModel != null) {

            useBaido 			= workModel.useBaido;				//使用培土
            maisu 			  = workModel.maisu;					//枚数
            useHole       = workModel.useHole;        //使用穴数

          }
          else {

            useBaido 			= 0;									//使用培土
            maisu 			  = 0;									//枚数
            useHole       = 0;                  //使用穴数

          }
        }

        useBaidoName = "";

        if (useBaido != 0) {

          useBaidoName	= Common.GetCommonValue(Common.ConstClass.ITOBAIDO, (int)useBaido, true);

        }
        else {
          useBaidoName  = "未選択";
        }
        resultJson.put("maisu"			  , maisu);							//枚数
        resultJson.put("useHole"      , useHole);           //使用穴数
        resultJson.put("useBaido"			, useBaido);					//使用培土
        resultJson.put("useBaidoSpan"	, useBaidoName);	    //使用培土名

    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.maisu        = this.wlt.maisu;
        wkd.useHole      = this.wlt.useHole;
        wkd.useBaido     = this.wlt.useBaido;
      }
      else {
        wkd.maisu        = Integer.parseInt(input.get("maisu").asText());       //枚数
        wkd.useHole      = Integer.parseInt(input.get("useHole").asText());     //使用穴数
        if ("".equals(input.get("useBaido").asText())) {
          wkd.useBaido     = 0;
        }
        else {
          wkd.useBaido     = Double.parseDouble(input.get("useBaido").asText());  //使用培土
        }
      }

      this.wlt.maisu 			  = wkd.maisu;											//枚数
      this.wlt.useHole      = wkd.useHole;                    //使用穴数
      this.wlt.useBaido     = wkd.useBaido;							      //使用培土
      this.wlt.update();

    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      wkp.maisu        = Integer.parseInt(input.get("maisu").asText());       //枚数
      wkp.useHole      = Integer.parseInt(input.get("useHole").asText());     //使用穴数
      if ("".equals(input.get("useBaido").asText())) {
        wkp.useBaido     = 0;
      }
      else {
        wkp.useBaido     = Double.parseDouble(input.get("useBaido").asText());  //使用培土
      }

    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);
      this.wlt.maisu        = wkd.maisu;                      //枚数
      this.wlt.useHole      = wkd.useHole;                    //使用穴数
      this.wlt.useBaido     = wkd.useBaido;                   //使用培土
      this.wlt.update();

    }
}
