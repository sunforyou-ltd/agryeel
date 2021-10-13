package compornent;

import java.util.List;

import models.Crop;
import models.Hinsyu;
import models.IkubyoDiary;
import models.IkubyoPlan;
import models.NaeStatus;
import models.Soil;
import models.Work;
import models.Youki;
import play.libs.Json;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】育苗記録作業別苗播種
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NaehashuIkubyoCompornent extends CommonIkubyoDiaryWork {

    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 品種ID
     */
    public String hinsyuId;
    /**
     * 容器ID
     */
    public double youkiId;
    /**
     * 容器単位種別
     */
    public int youkiUnitKind;
    /**
     * 苗数量
     */
    public double naeSuryo = 0;
    /**
     * 個数
     */
    public double kosu = 0;
    /**
     * 培土ID
     */
    public double baidoId;
    /**
     * 培土数量
     */
    public double baidoSuryo;
    /**
     * 培土単位種別
     */
    public int baidoUnitKind;
    /**
     * 覆土ID
     */
    public double fukudoId;
    /**
     * 覆土数量
     */
    public double fukudoSuryo;
    /**
     * 覆土単位種別
     */
    public int fukudoUnitKind;
    /**
     * 容器個数
     */
    public double youkiKosu = 0;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public NaehashuIkubyoCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public NaehashuIkubyoCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

        String	cropName = "未選択";
        String	hinsyuName = "未選択";
        String	youkiName  = "未選択";
        String	baidoName  = "未選択";
        String	fukudoName = "未選択";

        getHinsyuList();											//種
        getYoukiList();												//容器
        getSoilList(Soil.ConstKind.ALL);							//土

        /* 苗播種情報の取得 */
        double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        NaeStatus naeStatusData = NaeStatus.getStatusOfNae(this.naeNo);
        if (this.ikubyoDiary != null) {	/* 育苗記録編集の場合 */
          Hinsyu hinsyu = Hinsyu.getHinsyuInfo(Double.parseDouble(this.ikubyoDiary.hinsyuId));
          cropId        = hinsyu.cropId;						//生産物ID
          hinsyuId  	= this.ikubyoDiary.hinsyuId;			//品種
          youkiId 		= this.ikubyoDiary.youkiId;				//容器ID
          naeSuryo 		= this.ikubyoDiary.naeSuryo;			//苗数量
          kosu 			= this.ikubyoDiary.kosu;				//個数
          baidoId 		= this.ikubyoDiary.baidoId;				//培土ID
          baidoSuryo 	= this.ikubyoDiary.baidoSuryo;			//培土数量
          fukudoId 		= this.ikubyoDiary.fukudoId;			//覆土ID
          fukudoSuryo 	= this.ikubyoDiary.fukudoSuryo;			//覆土数量
        }
        else if (this.ikubyoPlan != null) { /* 育苗計画編集の場合 */
          Hinsyu hinsyu = Hinsyu.getHinsyuInfo(Double.parseDouble(this.ikubyoPlan.hinsyuId));
          cropId        = hinsyu.cropId;						//生産物ID
          hinsyuId  	= this.ikubyoPlan.hinsyuId;				//品種
          youkiId 		= this.ikubyoPlan.youkiId;				//容器ID
          naeSuryo 		= this.ikubyoPlan.naeSuryo;				//苗数量
          kosu 			= this.ikubyoPlan.kosu;					//個数
          baidoId 		= this.ikubyoPlan.baidoId;				//培土ID
          baidoSuryo 	= this.ikubyoPlan.baidoSuryo;			//培土数量
          fukudoId 		= this.ikubyoPlan.fukudoId;				//覆土ID
          fukudoSuryo 	= this.ikubyoPlan.fukudoSuryo;			//覆土数量
        }
        else {
          cropId 		= 0;
          hinsyuId  	= "";
          youkiId 		= 0;
          naeSuryo 		= 0;
          kosu 			= 0;
          baidoId 		= 0;
          baidoSuryo 	= 0;
          baidoUnitKind = 0;
          fukudoId 		= 0;
          fukudoSuryo 	= 0;
          fukudoUnitKind = 0;
        }

        if (cropId != 0) {
        	Crop crop = Crop.getCropInfo(cropId);
        	cropName = crop.cropName;
        }
        else {
        	cropName = "未選択";
        }

        if (!"".equals(hinsyuId)) {
          hinsyuName = Hinsyu.getMultiHinsyuName(hinsyuId);
        }
        else {
          hinsyuName = "未選択";
        }

        if (youkiId != 0) {
          youkiName = Youki.getYoukiName(youkiId);
          youkiKosu = Youki.getKosu(youkiId);
          youkiUnitKind = Youki.getUnitKind(youkiId);
        }
        else {
          youkiName = "未選択";
          youkiKosu = 0;
          youkiUnitKind = 1;
        }

        if (baidoId != 0) {
          baidoName = Soil.getSoilName(baidoId);
          baidoUnitKind = Soil.getUnitKind(baidoId);
        }
        else {
          baidoName = "未選択";
          baidoUnitKind = 1;
        }

        if (fukudoId != 0) {
          fukudoName = Soil.getSoilName(fukudoId);
          fukudoUnitKind = Soil.getUnitKind(baidoId);
        }
        else {
          fukudoName = "未選択";
          fukudoUnitKind = 1;
        }

        resultJson.put("cropId"			, cropId);					//生産物ID
        resultJson.put("cropSpan"		, cropName);				//生産物名
        resultJson.put("hinsyuId"		, hinsyuId);				//品種ID
        resultJson.put("hinsyuSpan"		, hinsyuName);				//品種名
        resultJson.put("youkiId"		, youkiId);					//容器ID
        resultJson.put("youkiSpan"		, youkiName);				//容器名
        resultJson.put("youkiKosu"		, youkiKosu);				//容器個数
        resultJson.put("youkiUnit"		, youkiUnitKind);			//容器単位種別
        resultJson.put("naeSuryo"		, naeSuryo);				//苗数量
        resultJson.put("kosu"			, kosu);					//個数
        resultJson.put("baidoId"		, baidoId);					//培土ID
        resultJson.put("baidoSpan"		, baidoName);				//培土名
        resultJson.put("baidoSuryo"		, baidoSuryo);				//培土数量
        resultJson.put("baidoUnit"		, baidoUnitKind);			//培土単位種別
        resultJson.put("fukudoId"		, fukudoId);				//覆土ID
        resultJson.put("fukudoSpan"		, fukudoName);				//覆土名
        resultJson.put("fukudoSuryo"	, fukudoSuryo);				//覆土数量
        resultJson.put("fukudoUnit"		, fukudoUnitKind);			//覆土単位種別


    }

    /**
     * 育苗記録保存
     */
    @Override
    public void commit(JsonNode input, IkubyoDiary ikd, Work wk) {

      super.commit(input, ikd, wk);

      ikd.hinsyuId = input.get("hinsyu").asText();                             //品種
      if ("".equals(input.get("youki").asText())) {
        ikd.youkiId = 0;
      }
      else {
        ikd.youkiId = Double.parseDouble(input.get("youki").asText());         //容器
      }
      if ("".equals(input.get("baido").asText())) {
        ikd.baidoId = 0;
      }
      else {
        ikd.baidoId = Double.parseDouble(input.get("baido").asText());         //培土
      }
      if ("".equals(input.get("fukudo").asText())) {
        ikd.fukudoId = 0;
      }
      else {
        ikd.fukudoId = Double.parseDouble(input.get("fukudo").asText());       //覆土
      }

      ikd.naeSuryo   = Double.parseDouble(input.get("naeSuryo").asText());     //苗数量
      ikd.kosu = Double.parseDouble(input.get("kosu").asText());               //個数
      ikd.baidoSuryo = Double.parseDouble(input.get("baidoSuryo").asText());   //培土数量
      ikd.fukudoSuryo = Double.parseDouble(input.get("fukudoSuryo").asText()); //覆土数量
    }

    /**
     * 育苗計画保存
     */
    @Override
    public void plan(JsonNode input, IkubyoPlan ikp, Work wk) {

      super.plan(input, ikp, wk);

      ikp.hinsyuId = input.get("hinsyu").asText();                             //品種
      if ("".equals(input.get("youki").asText())) {
        ikp.youkiId = 0;
      }
      else {
        ikp.youkiId = Double.parseDouble(input.get("youki").asText());         //容器
      }
      if ("".equals(input.get("baido").asText())) {
        ikp.baidoId = 0;
      }
      else {
        ikp.baidoId = Double.parseDouble(input.get("baido").asText());         //培土
      }
      if ("".equals(input.get("fukudo").asText())) {
        ikp.fukudoId = 0;
      }
      else {
        ikp.fukudoId = Double.parseDouble(input.get("fukudo").asText());       //覆土
      }

      ikp.naeSuryo   = Double.parseDouble(input.get("naeSuryo").asText());     //苗数量
      ikp.kosu = Double.parseDouble(input.get("kosu").asText());               //個数
      ikp.baidoSuryo = Double.parseDouble(input.get("baidoSuryo").asText());   //培土数量
      ikp.fukudoSuryo = Double.parseDouble(input.get("fukudoSuryo").asText()); //覆土数量
    }

    /**
     * 容器一覧取得
     */
    public static int getYoukiOfFarmJson(double pFarmId, ObjectNode pListJson) {

      //----- 生産者別容器情報を取得 -----
      List<Youki> youkiList = Youki.getYoukiOfFarm(pFarmId);
      for (Youki youki : youkiList) {

        if (youki.deleteFlag == 1) {
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , youki.youkiId);
        jd.put("name" , youki.youkiName);

        pListJson.put(String.valueOf(youki.youkiId), jd);

      }
      return GET_SUCCESS;
    }

    /**
     * 土一覧取得
     */
    public static int getSoilOfFarmJson(double pFarmId, int soilKind, ObjectNode pListJson) {

      //----- 生産者別土情報を取得 -----
      List<Soil> soilList = Soil.getSoilOfFarm(pFarmId);
      for (Soil soil : soilList) {

        if (soil.deleteFlag == 1) {
          continue;
        }

        if (soil.soilKind != soilKind) {
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , soil.soilId);
        jd.put("name" , soil.soilName);

        pListJson.put(String.valueOf(soil.soilId), jd);

      }
      return GET_SUCCESS;
    }

}
