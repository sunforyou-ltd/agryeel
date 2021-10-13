package compornent;

import models.Common;
import models.IkubyoDiary;
import models.IkubyoPlan;
import models.Kiki;
import models.NaeStatus;
import models.Work;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】作業記録作業別潅水
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class KansuiIkubyoCompornent extends CommonIkubyoDiaryWork {

    /**
     * 潅水方法
     */
    public int kansuiMethod;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * 潅水量
     */
    public double kansuiryo = 0;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public KansuiIkubyoCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public KansuiIkubyoCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        String	kansuiName		= "未選択";
        String	kikiName		  = "未選択";

        /* 潅水量の取得 */
        double farmId = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        NaeStatus naeStatusData = NaeStatus.getStatusOfNae(this.naeNo);

        if (this.ikubyoDiary != null) {	/* 育苗記録編集の場合 */

          kansuiMethod 	= this.ikubyoDiary.kansuiMethod;			//潅水方法
          kansuiryo 	= this.ikubyoDiary.kansuiRyo;				//潅水量
          kikiId 		= this.ikubyoDiary.kikiId;					//機器

        }
        else if (this.ikubyoPlan != null) { /* 育苗計画編集の場合 */

          kansuiMethod  = this.ikubyoPlan.kansuiMethod;             //潅水方法
          kansuiryo     = this.ikubyoPlan.kansuiRyo;                //潅水量
          kikiId        = this.ikubyoPlan.kikiId;                   //機器

        }
        else {

          kansuiryo = 0;
          kansuiMethod = 0;
          kikiId = 0;

        }

        if (kikiId != 0) {

          kikiName = Kiki.getKikiName(kikiId);

        }
        if (kansuiMethod != 0) {

        	kansuiName = Common.GetCommonValue(Common.ConstClass.KANSUI, kansuiMethod);

        }

        resultJson.put("kansuiMethod"	, kansuiMethod);					//潅水方法
        resultJson.put("kansuiSpan"		, kansuiName);						//潅水方法
        resultJson.put("kikiId"			, kikiId);							//機器
        resultJson.put("kikiSpan"		, kikiName);						//機器
        resultJson.put("kansuiryo"		, kansuiryo);						//潅水量

    }

    /**
     * 育苗記録保存
     */
    @Override
    public void commit(JsonNode input, IkubyoDiary ikd, Work wk) {

      super.commit(input, ikd, wk);

      if ("".equals(input.get("kansui").asText())) {
        ikd.kansuiMethod  = 0;
      }
      else {
        ikd.kansuiMethod  = Integer.parseInt(input.get("kansui").asText());   //潅水方法
      }
      if ("".equals(input.get("kiki").asText())) {
        ikd.kikiId  = 0;
      }
      else {
        ikd.kikiId  = Double.parseDouble(input.get("kiki").asText());         //機器
      }
      ikd.kansuiRyo = Double.parseDouble(input.get("kansuiryo").asText());    //潅水量

    }

    /**
     * 育苗計画保存
     */
    @Override
    public void plan(JsonNode input, IkubyoPlan ikp, Work wk) {

      super.plan(input, ikp, wk);
      if ("".equals(input.get("kansui").asText())) {
        ikp.kansuiMethod  = 0;
      }
      else {
        ikp.kansuiMethod  = Integer.parseInt(input.get("kansui").asText());   //潅水方法
      }
      if ("".equals(input.get("kiki").asText())) {
        ikp.kikiId = 0;
      }
      else {
        ikp.kikiId = Double.parseDouble(input.get("kiki").asText());          //機器
      }
      ikp.kansuiRyo = Double.parseDouble(input.get("kansuiryo").asText());    //潅水量

    }

}
