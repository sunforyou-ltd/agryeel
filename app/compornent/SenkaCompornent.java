package compornent;

import java.util.List;

import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Nisugata;
import models.NisugataOfFarm;
import models.Shitu;
import models.ShituOfFarm;
import models.Size;
import models.SizeOfFarm;
import models.Work;
import models.WorkDiary;
import models.WorkLastTime;
import models.WorkPlan;
import play.libs.Json;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】作業記録作業別選花
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SenkaCompornent extends CommonWorkDiaryWork {

    /**
     * 収穫量
     */
    public double shukakuRyo 	= 0;
    /**
     * 質
     */
    public double sitsu 		= 0;
    /**
     * サイズ
     */
    public double size 			= 0;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public SenkaCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public SenkaCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        getShituList();												//質
        getSizeList();												//サイズ

        /* 選花情報の取得 */
        double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
        CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();
        WorkLastTime workModel                  = WorkChainCompornent.getWorkLastTime(this.workId, farmId, cws.cropId);
        String cmnSitsu 	                      = "";
        String cmnSize 		                      = "";

        if (this.workDiary != null) {	/* 作業記録編集の場合 */

            shukakuRyo 	= this.workDiary.shukakuRyo;						//収穫量
            sitsu   	= this.workDiary.syukakuSitsu;
            size   		= this.workDiary.syukakuSize;
            cmnSitsu  = Shitu.getShituName(sitsu);
            cmnSize   = Size.getSizeName(size);

            getWorkDiaryDetail(this.workDiary.workDiaryId);

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */

          shukakuRyo  = this.workPlan.shukakuRyo;            //収穫量
          sitsu     = this.workPlan.syukakuSitsu;
          size      = this.workPlan.syukakuSize;
          cmnSitsu  = Shitu.getShituName(sitsu);
          cmnSize   = Size.getSizeName(size);

          getWorkPlanDetail(this.workPlan.workPlanId);

        }
        else {

          if (workModel != null) {

              shukakuRyo 	= workModel.shukakuRyo;						//収穫量
              sitsu   	  = workModel.syukakuSitsu;
              size   		  = workModel.syukakuSize;
              cmnSitsu  = Shitu.getShituName(sitsu);
              cmnSize   = Size.getSizeName(size);

          }
          else {

              shukakuRyo 	= 0;										//収穫量
              sitsu 		= 0;
              size 		= 0;

          }

          //前回作業情報より取得する
          getWorkHistryDetail(this.workId);

        }

        if ("".equals(cmnSitsu)) {
        	cmnSitsu = "未選択";
        }
        if ("".equals(cmnSize)) {
        	cmnSize = "未選択";
        }

        resultJson.put("shukakuRyo"		, shukakuRyo);					//収穫量
        resultJson.put("sitsu"			, sitsu);
        resultJson.put("size"			, size);
        resultJson.put("sitsuSpan"		, cmnSitsu);
        resultJson.put("sizeSpan"		, cmnSize);
    }

    /**
     * 作業記録保存
     */
    @Override
    public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      super.commit(input, wkd, wk);

      int mode = input.get("mode").asInt();

      if (mode == AgryeelConst.WorkDiaryMode.WORKING) {
        wkd.shukakuRyo      = this.wlt.shukakuRyo;
        wkd.syukakuSitsu    = this.wlt.syukakuSitsu;
        wkd.syukakuSize     = this.wlt.syukakuSize;
      }
      else {
        wkd.shukakuRyo      = Double.parseDouble(input.get("shukakuRyo").asText());   //収穫量
        wkd.syukakuSitsu    = Integer.parseInt(input.get("shitu").asText());
        wkd.syukakuSize     = Integer.parseInt(input.get("size").asText());
      }
      this.wlt.shukakuRyo       = wkd.shukakuRyo;                     //収穫量
      this.wlt.syukakuSitsu     = wkd.syukakuSitsu;
      this.wlt.syukakuSize      = wkd.syukakuSize;
      this.wlt.update();
      commitDetailInfo(input, wkd, wk);

    }
    /**
     * 作業計画保存
     */
    @Override
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      super.plan(input, wkp, wk);

      int mode = input.get("mode").asInt();

      wkp.shukakuRyo      = Double.parseDouble(input.get("shukakuRyo").asText());   //収穫量
      wkp.syukakuSitsu    = Integer.parseInt(input.get("shitu").asText());
      wkp.syukakuSize     = Integer.parseInt(input.get("size").asText());
      planDetailInfo(input, wkp, wk);

    }
    /**
     * 作業履歴値保存
     */
    @Override
    public void saveHistry(WorkDiary wkd) {

      super.saveHistry(wkd);

      this.wlt.shukakuRyo       = wkd.shukakuRyo;                     //収穫量
      this.wlt.syukakuSitsu     = wkd.syukakuSitsu;
      this.wlt.syukakuSize      = wkd.syukakuSize;
      this.wlt.update();

    }
    /**
     * 質を取得する
     * @param pFarmId
     * @param pListJson
     * @return
     */
    public static int getShituJson(double pFarmId, ObjectNode pListJson) {

      /** 戻り値 */
      int result  = GET_SUCCESS;

      List<ShituOfFarm> datas = ShituOfFarm.getShituOfFarm(pFarmId);

      for (ShituOfFarm data : datas) {
        if (data.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        Shitu model = Shitu.getShituInfo(data.shituId);
        if (model != null) {
          ObjectNode jd = Json.newObject();

          jd.put("id"   , model.shituId);
          jd.put("name" , model.shituName);

          pListJson.put(String.valueOf(model.shituId), jd);
        }
      }

      return result;

    }
    /**
     * サイズを取得する
     * @param pFarmId
     * @param pListJson
     * @return
     */
    public static int getSizeJson(double pFarmId, ObjectNode pListJson) {

      /** 戻り値 */
      int result  = GET_SUCCESS;

      List<SizeOfFarm> datas = SizeOfFarm.getSizeOfFarm(pFarmId);

      for (SizeOfFarm data : datas) {
        if (data.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        Size model = Size.getSizeInfo(data.sizeId);
        if (model != null) {
          ObjectNode jd = Json.newObject();

          jd.put("id"   , model.sizeId);
          jd.put("name" , model.sizeName);

          pListJson.put(String.valueOf(model.sizeId), jd);
        }
      }

      return result;

    }
}
