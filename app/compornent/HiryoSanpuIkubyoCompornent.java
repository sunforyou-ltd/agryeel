package compornent;

import models.IkubyoDiary;
import models.IkubyoPlan;
import models.Work;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYEEL】育苗記録作業別肥料散布
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class HiryoSanpuIkubyoCompornent extends CommonIkubyoDiaryWork {

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public HiryoSanpuIkubyoCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public HiryoSanpuIkubyoCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        if (this.ikubyoDiary != null) {		/* 育苗記録編集の場合 */
          getIkubyoDiarySanpu(this.ikubyoDiary.ikubyoDiaryId);
        }
        else if (this.ikubyoPlan != null) {	/* 育苗計画編集の場合 */

          getIkubyoPlanSanpu(this.ikubyoPlan.ikubyoPlanId);

        }
        else {
          //前回作業情報より取得する
          getIkubyoHistryBase(this.workId);
        }
    }

    /**
     * 育苗記録保存
     */
    @Override
    public void commit(JsonNode input, IkubyoDiary ikd, Work wk) {

        super.commit(input, ikd, wk);
        commitSanpuInfo(input, ikd, wk);	//育苗記録散布情報の生成

    }
    /**
     * 育苗計画保存
     */
    @Override
    public void plan(JsonNode input, IkubyoPlan ikp, Work wk) {

        super.plan(input, ikp, wk);
        planSanpuInfo(input, ikp, wk);  //育苗計画散布情報の生成

    }

}
