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
 * 【AGRYEEL】作業記録作業別間引き
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class MabikiCompornent extends CommonWorkDiaryWork {

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public MabikiCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public MabikiCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

      double shitateHonsu = 0;

      /* 間引き情報の取得 */
      double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
      CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
      WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
      if (this.workDiary != null) { /* 作業記録編集の場合 */

        shitateHonsu      = this.workDiary.shitateHonsu;      //仕立本数

      }
      else if (this.workPlan != null) { /* 作業計画編集の場合 */

        shitateHonsu      = this.workPlan.shitateHonsu;       //仕立本数

      }
      else {
        if (workModel != null) {

          shitateHonsu      = workModel.shitateHonsu;         //仕立本数

        }
        else {

          shitateHonsu      = 0;                              //仕立本数

        }
      }

      resultJson.put("shitateHonsu"      , shitateHonsu);     //仕立本数
    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.shitateHonsu  = this.wlt.shitateHonsu;
      }
      else {
        try {
          wkd.shitateHonsu = Double.parseDouble(input.get("shitateHonsu").asText());
        }
        catch (Exception ex) {
          wkd.shitateHonsu = 0;
        }
      }

      this.wlt.shitateHonsu        = wkd.shitateHonsu;        //仕立本数
      this.wlt.update();


    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      try {
        wkp.shitateHonsu = Double.parseDouble(input.get("shitateHonsu").asText());
      }
      catch (Exception ex) {
        wkp.shitateHonsu = 0;
      }

    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);

    }
}
