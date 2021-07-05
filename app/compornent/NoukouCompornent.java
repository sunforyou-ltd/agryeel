package compornent;

import models.Attachment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Kiki;
import models.Work;
import models.WorkDiary;
import models.WorkLastTime;
import models.WorkPlan;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】作業記録作業別耕す
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NoukouCompornent extends CommonWorkDiaryWork {

    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * アタッチメントID
     */
    public double attachmentId;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public NoukouCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public NoukouCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

      //【AICA】TODO:コンビネーションはワークチェーンの散布情報組み合わせに移行する
//        getCombiList(Combi.ConstKind.TAGAYASU);						//コンビネーション
        getKikiList();												//機器
        String	kikiName		= "未選択";
        String	attachmentName	= "選択不可";
        //getAttachmentList();										//アタッチメント

        if (this.workDiary != null) {	/* 作業記録編集の場合 */

            kikiId 			= this.workDiary.kikiId;					//機器
            attachmentId 	= this.workDiary.attachmentId;				//アタッチメント

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          kikiId      = this.workPlan.kikiId;          //機器
          attachmentId  = this.workPlan.attachmentId;        //アタッチメント

        }
        else {

          double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
          CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
          CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
          WorkLastTime workModel = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);

          if (workModel != null) {

            kikiId        = workModel.kikiId;
            attachmentId  = workModel.attachmentId;

          }
          else {

            kikiId      = 0;
            attachmentId  = 0;                  //アタッチメント

          }

        }
        if (kikiId != 0) {

    		kikiName		= Kiki.getKikiName(kikiId);
            if (attachmentId != 0) {

        		attachmentName	= Attachment.getAttachmentName(attachmentId);

            }
            else {

        		attachmentName	= "未選択";

            }

        }

        resultJson.put("kikiId"			, kikiId);							//機器
        resultJson.put("kikiSpan"		, kikiName);						//機器
        resultJson.put("attachmentId"	, attachmentId);					//アタッチメント
        resultJson.put("attachmentSpan"	, attachmentName);					//アタッチメント

    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

        super.commit(input, wkd, wk);

        if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {
          wkd.kikiId        = this.wlt.kikiId;
          wkd.attachmentId  = this.wlt.attachmentId;
        }
        else {
          if ("".equals(input.get("kiki").asText())) {
            wkd.kikiId        = 0;
          }
          else {
            wkd.kikiId        = Double.parseDouble(input.get("kiki").asText());   //機器
          }
          if("".equals(input.get("attachment").asText())) {
            wkd.attachmentId  = 0; //アタッチメント
          }
          else {
            wkd.attachmentId  = Double.parseDouble(input.get("attachment").asText()); //アタッチメント
          }
        }
        this.wlt.kikiId       = wkd.kikiId;
        this.wlt.attachmentId = wkd.attachmentId;
        this.wlt.update();
    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

        super.plan(input, wkp, wk);

        if ("".equals(input.get("kiki").asText())) {
          wkp.kikiId        = 0;
        }
        else {
          wkp.kikiId        = Double.parseDouble(input.get("kiki").asText());   //機器
        }
        if ("".equals(input.get("attachment").asText())) {
          wkp.attachmentId  = 0;
        }
        else {
          wkp.attachmentId  = Double.parseDouble(input.get("attachment").asText()); //アタッチメント
        }
    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);

      this.wlt.kikiId       = wkd.kikiId;
      this.wlt.attachmentId = wkd.attachmentId;
      this.wlt.update();

    }
}
