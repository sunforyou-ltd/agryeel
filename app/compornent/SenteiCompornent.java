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
 * 【AGRYEEL】作業記録作業別剪定
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SenteiCompornent extends CommonWorkDiaryWork {

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public SenteiCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public SenteiCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

      double senteiHeight = 0;

      /* マルチ情報の取得 */
      double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
      CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
      WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
      if (this.workDiary != null) { /* 作業記録編集の場合 */

        senteiHeight      = this.workDiary.senteiHeight;      //剪定高

      }
      else if (this.workPlan != null) { /* 作業計画編集の場合 */

        senteiHeight      = this.workPlan.senteiHeight;      //剪定高

      }
      else {
        if (workModel != null) {

          senteiHeight      = workModel.senteiHeight;         //剪定高

        }
        else {

          senteiHeight      = 0;                              //剪定高

        }
      }

      resultJson.put("senteiHeight"      , senteiHeight);     //剪定高
    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.senteiHeight  = this.wlt.senteiHeight;
      }
      else {
        try {
          wkd.senteiHeight = Double.parseDouble(input.get("senteiHeight").asText());
        }
        catch (Exception ex) {
          wkd.senteiHeight = 0;
        }
      }

      this.wlt.senteiHeight        = wkd.senteiHeight;        //剪定高
      this.wlt.update();


    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      try {
        wkp.senteiHeight = Double.parseDouble(input.get("senteiHeight").asText());
      }
      catch (Exception ex) {
        wkp.senteiHeight = 0;
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
