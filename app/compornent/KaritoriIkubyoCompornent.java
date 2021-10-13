package compornent;

import models.IkubyoDiary;
import models.IkubyoPlan;
import models.NaeStatus;
import models.Work;
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
public class KaritoriIkubyoCompornent extends CommonIkubyoDiaryWork {

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public KaritoriIkubyoCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public KaritoriIkubyoCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

      double senteiHeight = 0;

      /* 剪定情報の取得 */
      double farmId = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      NaeStatus naeStatusData = NaeStatus.getStatusOfNae(this.naeNo);
      if (this.ikubyoDiary != null) { /* 育苗記録編集の場合 */
        senteiHeight = this.ikubyoDiary.senteiHeight;      //剪定高
      }
      else if (this.ikubyoPlan != null) { /* 育苗計画編集の場合 */

        senteiHeight = this.ikubyoPlan.senteiHeight;       //剪定高

      }
      else {
        senteiHeight = 0;                                  //剪定高
      }

      resultJson.put("senteiHeight", senteiHeight);        //剪定高
    }

    /**
     * 育苗記録保存
     */
    @Override
    public void commit(JsonNode input, IkubyoDiary ikd, Work wk) {

      super.commit(input, ikd, wk);

      try {
        ikd.senteiHeight = Double.parseDouble(input.get("senteiHeight").asText());
      }
      catch (Exception ex) {
        ikd.senteiHeight = 0;
      }
    }

    /**
     * 育苗計画保存
     */
    @Override
    public void plan(JsonNode input, IkubyoPlan ikp, Work wk) {

      super.plan(input, ikp, wk);

      try {
        ikp.senteiHeight = Double.parseDouble(input.get("senteiHeight").asText());
      }
      catch (Exception ex) {
        ikp.senteiHeight = 0;
      }

    }

}
