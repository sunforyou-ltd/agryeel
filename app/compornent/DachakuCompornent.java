package compornent;

import models.Work;
import models.WorkDiary;
import models.WorkPlan;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYEEL】作業記録作業別脱着
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class DachakuCompornent extends CommonWorkDiaryWork {

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public DachakuCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public DachakuCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        if (this.workDiary != null) {	/* 作業記録編集の場合 */

        	getWorkDiaryDetail(this.workDiary.workDiaryId);

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          getWorkPlanDetail(this.workPlan.workPlanId);

        }
        else {

          //前回作業情報より取得する
          getWorkHistryDetail(this.workId);

        }

    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

        super.commit(input, wkd, wk);
        commitDetailInfo(input, wkd, wk);

    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

        super.plan(input, wkp, wk);
        planDetailInfo(input, wkp, wk);

    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);
      //メインコントローラ側で履歴値を保存する

    }
}
