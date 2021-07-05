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
 * 【AGRYEEL】作業記録作業別廃棄
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class HaikiCompornent extends CommonWorkDiaryWork {

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public HaikiCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public HaikiCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

      double haikiRyo = 0;

      /* 日長調整情報の取得 */
      double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
      CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
      WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
      if (this.workDiary != null) { /* 作業記録編集の場合 */

        haikiRyo      = this.workDiary.haikiRyo;      //廃棄量

      }
      else if (this.workPlan != null) { /* 作業計画編集の場合 */

        haikiRyo      = this.workPlan.haikiRyo;       //廃棄量

      }
      else {
        if (workModel != null) {

          haikiRyo      = workModel.haikiRyo;         //廃棄量

        }
        else {

          haikiRyo      = 0;                       //廃棄量

        }
      }

      resultJson.put("haikiRyo"      , haikiRyo);     //廃棄量
    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.haikiRyo  = this.wlt.haikiRyo;
      }
      else {
        try {
          wkd.haikiRyo = Double.parseDouble(input.get("haikiRyo").asText());
        }
        catch (Exception ex) {
          wkd.haikiRyo = 0;
        }
      }

      this.wlt.haikiRyo        = wkd.haikiRyo;        //廃棄量
      this.wlt.update();


    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      try {
        wkp.haikiRyo = Double.parseDouble(input.get("haikiRyo").asText());
      }
      catch (Exception ex) {
        wkp.nicho = 0;
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
