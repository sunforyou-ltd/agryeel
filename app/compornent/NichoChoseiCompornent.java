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
 * 【AGRYEEL】作業記録作業別日長調整
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NichoChoseiCompornent extends CommonWorkDiaryWork {

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public NichoChoseiCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public NichoChoseiCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

      double nicho = 0;

      /* 日長調整情報の取得 */
      double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
      CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
      WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
      if (this.workDiary != null) { /* 作業記録編集の場合 */

        nicho      = this.workDiary.nicho;      //日長

      }
      else if (this.workPlan != null) { /* 作業計画編集の場合 */

        nicho      = this.workPlan.nicho;       //日長

      }
      else {
        if (workModel != null) {

          nicho      = workModel.nicho;         //日長

        }
        else {

          nicho      = 0;                       //日長

        }
      }

      resultJson.put("nicho"      , nicho);     //日長
    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.nicho  = this.wlt.nicho;
      }
      else {
        try {
          wkd.nicho = Double.parseDouble(input.get("nicho").asText());
        }
        catch (Exception ex) {
          wkd.nicho = 0;
        }
      }

      this.wlt.nicho        = wkd.nicho;        //日長
      this.wlt.update();


    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      try {
        wkp.nicho = Double.parseDouble(input.get("nicho").asText());
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
