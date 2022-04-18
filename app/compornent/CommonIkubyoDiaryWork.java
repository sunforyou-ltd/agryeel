package compornent;

import java.text.DecimalFormat;
import java.util.List;

import models.Account;
import models.Attachment;
import models.Belto;
import models.Common;
import models.Crop;
import models.Hinsyu;
import models.IkubyoDiary;
import models.IkubyoDiaryDetail;
import models.IkubyoDiarySanpu;
import models.IkubyoLine;
import models.IkubyoPlan;
import models.IkubyoPlanDetail;
import models.IkubyoPlanSanpu;
import models.Kiki;
import models.NaeStatus;
import models.Nouhi;
import models.NouhiOfCrop;
import models.Soil;
import models.Work;
import models.Youki;
import play.libs.Json;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;


/**
 * 【AGRYEEL】作業記録作業別スーパークラス
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public abstract class CommonIkubyoDiaryWork implements AgryellInterface {


    /**
     * HTTPセッション情報
     */
    public Session session;
    /**
     * ResultJsonData
     */
    public ObjectNode resultJson;

    /**
     * 育苗記録
     */
    public IkubyoDiary ikubyoDiary = null;

    /**
     * 苗No
     */
    public String naeNo;

    /**
     * 作業ID
     */
    public double workId;

    /**
     * APIフラグ
     */
    public Boolean apiFlg = false;
    /**
     * 育苗計画
     */
    public IkubyoPlan ikubyoPlan = null;
    /**
     * 合計収穫量
     */
    public double shukakuryo = 0;

    public int mode;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public CommonIkubyoDiaryWork(Session session, ObjectNode resultJson) {

        this.session 		= session;
        this.resultJson 	= resultJson;
        this.apiFlg = false;

        getAccountList();						//アカウント情報一覧

    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public CommonIkubyoDiaryWork(Session session, ObjectNode resultJson, Boolean api) {

        this.session 		= session;
        this.resultJson 	= resultJson;

        this.apiFlg = api;
        getAccountList();						//アカウント情報一覧

    }

    /**
     * 初期処理
     */
    public abstract void init();

    /**
     * 育苗記録保存
     */
    synchronized public void commit(JsonNode input, IkubyoDiary ikd, Work wk) {

      double   farmId         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      NaeStatus naeStatusData = NaeStatus.getStatusOfNae(ikd.naeNo);

      this.mode = input.get("mode").asInt();

    }
    /**
     * 育苗計画保存
     */
    public void plan(JsonNode input, IkubyoPlan wkp, Work wk) {

      /* ここでは特に何もしない */

    }

    /**
     * 育苗記録散布情報保存
     * @param input
     * @param wkd
     * @param wk
     */
    synchronized protected void commitSanpuInfo(JsonNode input, IkubyoDiary ikd, Work wk) {

      JsonNode nouhiInfoList = input.get("nouhiInfo");
      double 	 farmId 	     = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

      if (nouhiInfoList != null && nouhiInfoList.size() > 0) {

        for (int nouhiIndex = 0; nouhiIndex < nouhiInfoList.size(); nouhiIndex++) {

            double  nouhiId   = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("nouhiId").asText());        //農肥IDを取得する

            /* 農肥IDから農肥情報を取得する */
            Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  nouhiId).findUnique();
            double hosei = 1;

            if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
              hosei = 1000;
            }

            /* 農肥前回情報として保存する */
            nouhi.bairitu   = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("bairitu").asText());          //倍率を取得する
            nouhi.sanpuryo  = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("sanpuryo").asText());         //散布量を取得する
            nouhi.sanpuryo *= hosei;
            double sanpuryo = nouhi.sanpuryo;
            nouhi.update();

            /* 作業記録農肥散布情報を生成する */
            IkubyoDiarySanpu ids = new IkubyoDiarySanpu();                                    //育苗記録散布情報

            ids.ikubyoDiaryId = ikd.ikubyoDiaryId;                                            //育苗記録ＩＤ
            ids.ikubyoDiarySequence   = (nouhiIndex + 1);                                     //育苗記録シーケンス
            String sanpuMethod  = nouhiInfoList.get(nouhiIndex).get("sanpuId").asText();      //散布方法
            if (sanpuMethod != null && !"".equals(sanpuMethod)) {
              ids.sanpuMethod     = Integer.parseInt(sanpuMethod);
            }
            else {
              ids.sanpuMethod     = 0;
            }
            String kikiId  = nouhiInfoList.get(nouhiIndex).get("kiki").asText();          //機器
            if (kikiId != null && !"".equals(kikiId)) {
              ids.kikiId        = Double.parseDouble(kikiId);
            }
            else {
              ids.kikiId        = 0;
            }
            String sAttachmentId  = nouhiInfoList.get(nouhiIndex).get("attachment").asText();
            if (sAttachmentId != null && !"".equals(sAttachmentId)) {
                ids.attachmentId    = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("attachment").asText());   //アタッチメントＩＤ
            }
            else {
                ids.attachmentId    = 0;                                        //アタッチメントＩＤ
            }
            ids.nouhiId         = nouhiId;                                      //農肥ＩＤ
            ids.bairitu         = nouhi.bairitu;                                //倍率
            ids.sanpuryo        = sanpuryo;                                     //散布量
            ids.naeStatusUpdate = AgryeelConst.UpdateFlag.NONE;                 //苗状況照会反映フラグ

            ids.save();
        }
      }
    }
    /**
     * 育苗計画散布情報保存
     * @param input
     * @param ikp
     * @param wk
     */
    protected void planSanpuInfo(JsonNode input, IkubyoPlan ikp, Work wk) {

        JsonNode nouhiInfoList = input.get("nouhiInfo");
        double   farmId        = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

        if (nouhiInfoList != null && nouhiInfoList.size() > 0) {
          for (int nouhiIndex = 0; nouhiIndex < nouhiInfoList.size(); nouhiIndex++) {

              double  nouhiId   = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("nouhiId").asText());        //農肥IDを取得する

              /* 農肥IDから農肥情報を取得する */
              Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  nouhiId).findUnique();
              double hosei = 1;

              if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
                hosei = 1000;
              }

              /* 育苗計画農肥散布情報を生成する */
              IkubyoPlanSanpu ips = new IkubyoPlanSanpu();                              //育苗記録散布情報

              ips.ikubyoPlanId = ikp.ikubyoPlanId;                                        //育苗作業計画ＩＤ
              ips.ikubyoDiarySequence   = (nouhiIndex + 1);                               //作業記録シーケンス
              ips.sanpuMethod         = Integer.parseInt(nouhiInfoList.get(nouhiIndex).get("sanpuId").asText());      //散布方法
              ips.kikiId              = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("kiki").asText());       //機器ＩＤ
              String sAttachmentId  = nouhiInfoList.get(nouhiIndex).get("attachment").asText();
              if (sAttachmentId != null && !"".equals(sAttachmentId)) {
                  ips.attachmentId    = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("attachment").asText()); //アタッチメントＩＤ
              }
              else {
                  ips.attachmentId    = 0;                                                                            //アタッチメントＩＤ
              }
              ips.nouhiId       = nouhiId;                                                                            //農肥ＩＤ
              ips.bairitu       = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("bairitu").asText());          //倍率
              ips.sanpuryo      = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("sanpuryo").asText()) * hosei; //散布量
              ips.naeStatusUpdate  = AgryeelConst.UpdateFlag.NONE;                                                    //苗状況照会反映フラグ

              ips.save();

          }
      }
    }
    /**
     * 育苗記録詳細情報保存
     * @param input
     * @param ikd
     * @param wk
     */
    synchronized protected void commitDetailInfo(JsonNode input, IkubyoDiary ikd, Work wk) {

        JsonNode detailInfoList = input.get("detailInfo");
        double   farmId        = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

        if (detailInfoList != null && detailInfoList.size() > 0) {

          for (int seqIndex = 0; seqIndex < detailInfoList.size(); seqIndex++) {

            /* 育苗記録詳細情報を生成する */
            IkubyoDiaryDetail idd = new IkubyoDiaryDetail();                              //育苗記録散布情報

            idd.ikubyoDiaryId         = ikd.ikubyoDiaryId;                                //育苗記録ＩＤ
            idd.ikubyoDiarySequence   = (seqIndex + 1);                                   //育苗記録シーケンス
            idd.naeNo                 = ikd.naeNo;                                        //苗No

            /* 今後の項目追加メンテ用で残しておく
            switch ((int)wk.workTemplateId) {
            case AgryeelConst.WorkTemplate.KAISHU:
              break;

            default:
              break;
            }
            */
            idd.save();

          }

        }
    }
    /**
     * 育苗計画詳細情報保存
     * @param input
     * @param ikp
     * @param wk
     */
    protected void planDetailInfo(JsonNode input, IkubyoPlan ikp, Work wk) {

        JsonNode detailInfoList = input.get("detailInfo");
        double   farmId         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

        if (detailInfoList != null && detailInfoList.size() > 0) {

          for (int seqIndex = 0; seqIndex < detailInfoList.size(); seqIndex++) {

            /* 育苗計画詳細情報を生成する */
            IkubyoPlanDetail ipd = new IkubyoPlanDetail();                                    //育苗計画散布情報

            ipd.ikubyoPlanId          = ikp.ikubyoPlanId;                                     //育苗計画ＩＤ
            ipd.ikubyoDiarySequence   = (seqIndex + 1);                                       //作業記録シーケンス
            ipd.naeNo                 = ikp.naeNo;                                            //苗No

            ipd.save();
          }
        }
    }

    /**
     * 【AGRYEEL】アカウント情報一覧を取得する
     * @return
     */
    public void getAccountList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJsonApi = mapper.createArrayNode();

        /* アカウント情報から同一農場のアカウントリストを取得する */
        List<Account> accountList = Account.find.where().eq("farm_id", Double.parseDouble(session.get(AgryeelConst.SessionKey.FARMID))).orderBy("account_id").findList();

        for (Account accountData : accountList) {													//アカウント情報をJSONデータに格納する

            ObjectNode accountJson	= Json.newObject();
            accountJson.put("accountId"				, accountData.accountId);						//アカウントID
            accountJson.put("accountName"			, accountData.acountName);						//アカウント名

            if(this.apiFlg){
            	listJsonApi.add(accountJson);
            }else{
            	listJson.put(accountData.accountId, accountJson);
            }
        }

        if(this.apiFlg){
        	resultJson.put(AgryeelConst.WorkDiary.DataList.ACCOUNT, listJsonApi);
        }else{
        	resultJson.put(AgryeelConst.WorkDiary.DataList.ACCOUNT, listJson);
        }

    }

    /**
     * 【AGRYEEL】機器情報一覧を取得する
     * @return
     */
    public void getKikiList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 現在の作業情報を取得する */
        String searchString = "";					//検索条件

        /* 該当作業IDの取得 */
        double workId = 0;

        if (this.ikubyoDiary != null) {	/* 育苗記録編集の場合 */
          workId = this.ikubyoDiary.workId;
        }
        else if (this.ikubyoPlan != null) { /* 育苗計画編集の場合 */
          workId = this.ikubyoPlan.workId;
        }
        else {
          workId = Double.parseDouble(session.get(AgryeelConst.SessionKey.WORKID));
        }

        /* 作業IDから作業情報を取得する */
        Work workData = (Work)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.WORK, "work_id = " + workId  );
        if (workData != null) {																		//作業情報が存在する場合
            String[] kikiKind   = "0".split(",");
            searchString 		= "kiki_kind in(";
            boolean firstFlag	= true;
            for (String kind : kikiKind) {

                if (!firstFlag) {
                    searchString += ",";
                }
                searchString 	+= kind;
                firstFlag		 = false;
            }
            searchString 	+= ")";
        }

        /* 機器情報一覧を取得する */
        List<Kiki> kikiList = Kiki.find.where(searchString).orderBy("kiki_id").findList();

        for (Kiki kikiData : kikiList) {											//機器情報をJSONデータに格納する

            ObjectNode kikiJson				= Json.newObject();
            kikiJson.put("kikiId"			, kikiData.kikiId);						//機器ID
            kikiJson.put("kikiName"			, kikiData.kikiName);					//機器名
            kikiJson.put("kikiKind"			, kikiData.kikiKind);					//機器種別

            listJson.put(String.valueOf(kikiData.kikiId), kikiJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.KIKI, listJson);

    }
    /**
     * 【AGRYEEL】アタッチメント情報一覧を取得する
     * @return
     */
    public void getAttachmentList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* アタッチメント情報一覧を取得する */
        List<Attachment> attachmentList = Attachment.find.orderBy("attachment_id").findList();

        for (Attachment attachmentData : attachmentList) {							//機器情報をJSONデータに格納する

            ObjectNode attachmentJson					= Json.newObject();
            attachmentJson.put("attachmentId"			, attachmentData.attachmentId);		//アタッチメントID
            attachmentJson.put("attachementName"		, attachmentData.attachementName);	//アタッチメント名
            //【AICA】TODO:ワークチェイン実装後に修正すること
            //attachmentJson.put("workId"					, attachmentData.workId);			//作業ID
            attachmentJson.put("workId"         , "");     //作業ID

            listJson.put(String.valueOf(attachmentData.attachmentId), attachmentJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.ATTACHMENT, listJson);

    }

    /**
     * 【AGRYEEL】散布情報一覧を取得する
     * @return
     */
    public void getSanpuList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 散布方法情報一覧を取得する */
        List<Common> commonList = Common.GetCommonList(Common.ConstClass.SANPUMETHOD);

        for (Common commonData : commonList) {										//散布方法情報をJSONデータに格納する

            ObjectNode commonJson			= Json.newObject();
            commonJson.put("sanpuId"		, commonData.commonSeq);				//散布方法ID
            commonJson.put("sanpuName"		, commonData.commonName);				//散布方法名

            listJson.put(String.valueOf(commonData.commonSeq), commonJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.SANPUMETHOD, listJson);

    }

    /**
     * 【AGRYEEL】種情報一覧を取得する
     * @return
     */
    public void getHinsyuList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 種情報一覧を取得する */
        List<Hinsyu> hinsyuList = Hinsyu.find.orderBy("hinsyu_id").findList();

        for (Hinsyu hinsyuData : hinsyuList) {										//種情報をJSONデータに格納する

            ObjectNode hinsyuJson			= Json.newObject();
            hinsyuJson.put("hinsyuId"		, hinsyuData.hinsyuId);					//品種ID
            hinsyuJson.put("hinsyuName"		, hinsyuData.hinsyuName);				//品種名

            listJson.put(String.valueOf(hinsyuData.hinsyuId), hinsyuJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.HINSYU, listJson);

    }

    /**
     * 【AGRYEEL】ベルト情報一覧を取得する
     * @return
     */
    public void getBeltoList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* ベルト情報一覧を取得する */
        List<Belto> beltoList = Belto.find.orderBy("belto_id").findList();

        for (Belto beltoData : beltoList) {											//ベルト情報をJSONデータに格納する

            ObjectNode beltoJson			= Json.newObject();
            beltoJson.put("beltoId"			, beltoData.beltoId);					//ベルトID
            beltoJson.put("beltoName"		, beltoData.beltoName);					//ベルト名

            listJson.put(String.valueOf(beltoData.beltoId), beltoJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.BELTO, listJson);

    }

    /**
     * 【AGRYEEL】潅水方法情報一覧を取得する
     * @return
     */
    public void getKansuiList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 潅水方法情報一覧を取得する */
        List<Common> commonList = Common.GetCommonList(Common.ConstClass.KANSUI);

        for (Common commonData : commonList) {										//潅水方法情報をJSONデータに格納する

            ObjectNode commonJson			= Json.newObject();
            commonJson.put("kansuiId"		, commonData.commonSeq);				//潅水方法ID
            commonJson.put("kansuiName"		, commonData.commonName);				//潅水方法名

            listJson.put(String.valueOf(commonData.commonSeq), commonJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.KANSUI, listJson);

    }

    /**
     * 【AGRYEEL】生産物情報一覧を取得する
     * @param resultJson
     * @return
     */
    public void getCropList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 生産物情報一覧を取得する */
        List<Crop> cropList = Crop.find.orderBy("crop_id").findList();

        for (Crop cropData : cropList) {															//生産物情報をJSONデータに格納する

            ObjectNode cropJson		= Json.newObject();
            cropJson.put("cropId"			, cropData.cropId);						//生産物ID
            cropJson.put("cropName"			, cropData.cropName);					//生産物名

            listJson.put(String.valueOf(cropData.cropId), cropJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.CROP, listJson);

    }

    /**
     * 【AGRYEEL】農肥情報一覧を取得する
     * @return
     */
    public void getNouhiList(int nouhiKind) {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 農肥情報一覧を取得する */
        /*----- 農肥種別より検索条件を作成する -----*/
        String sWhereString		=	"";				// 検索条件

        if ( nouhiKind != Nouhi.ConstKind.ALL ) {	// 農肥種別が指定されている場合
            sWhereString		=	"nouhi_kind = " + nouhiKind;
        }


        List<Nouhi> nouhiList = Nouhi.find.where(sWhereString).orderBy("nouhi_id").findList();

        for (Nouhi nouhiData : nouhiList) {											//農肥情報をJSONデータに格納する

            ObjectNode nouhiJson			= Json.newObject();
            Nouhi nouhi = Nouhi.find.where().eq("nouhi_id",  nouhiData.nouhiId).findUnique();
            double hosei = 1;
            if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
              hosei = 0.001;
            }

            nouhiJson.put("nouhiId"			, nouhiData.nouhiId);					//農肥ID
            nouhiJson.put("nouhiName"		, nouhiData.nouhiName);					//農肥名
            nouhiJson.put("nouhiKind"		, nouhiData.nouhiKind);					//農肥種別
            nouhiJson.put("bairitu"			, nouhiData.bairitu);					//倍率
            nouhiJson.put("sanpuryo"		, nouhiData.sanpuryo * hosei);					//散布情報

            listJson.put(String.valueOf(nouhiData.nouhiId), nouhiJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.NOUHI, listJson);

    }

    /**
     * 【AGRYEEL】生産物農肥情報一覧を取得する
     * @return
     */
    public void getNouhiOfCropList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 生産物農肥情報一覧を取得する */
        List<NouhiOfCrop> nouhiOfCropList = NouhiOfCrop.find.orderBy("kiki_id").findList();

        for (NouhiOfCrop nouhiOfCropData : nouhiOfCropList) {						//機器情報をJSONデータに格納する

            ObjectNode nouhiOfCropJson		= Json.newObject();
            nouhiOfCropJson.put("cropId"			, nouhiOfCropData.cropId);		//生産物ID
            nouhiOfCropJson.put("nouhiId"			, nouhiOfCropData.nouhiId);		//農肥ID

            listJson.put(String.valueOf(nouhiOfCropData.nouhiId), nouhiOfCropJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.NOUHICROP, listJson);

    }

    /**
     * 【AGRYEEL】容器情報一覧を取得する
     * @return
     */
    public void getYoukiList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 種情報一覧を取得する */
        List<Youki> youkiList = Youki.find.orderBy("youki_id").findList();

        for (Youki youkiData : youkiList) {											//容器情報をJSONデータに格納する

            ObjectNode youkiJson = Json.newObject();
            youkiJson.put("youkiId"		, youkiData.youkiId);			//容器ID
            youkiJson.put("youkiName"	, youkiData.youkiName);			//容器名
            youkiJson.put("unitKind"	, youkiData.unitKind);			//単位種別
            youkiJson.put("kosu"		, youkiData.kosu);				//個数

            listJson.put(String.valueOf(youkiData.youkiId), youkiJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.YOUKI, listJson);

    }

    /**
     * 【AGRYEEL】土情報一覧を取得する
     * @return
     */
    public void getSoilList(int soilKind) {

        /* 一覧表JSONオブジェクト */
        ObjectNode listJson = Json.newObject();

        /* 土情報一覧を取得する */
        /*----- 土種別より検索条件を作成する -----*/
        String sWhereString = "";				// 検索条件

        if ( soilKind != Soil.ConstKind.ALL ) {		// 土種別が指定されている場合
            sWhereString = "soil_kind = " + soilKind;
        }

        List<Soil> soilList = Soil.find.where(sWhereString).orderBy("soil_id").findList();

        for (Soil soilData : soilList) {			//土情報をJSONデータに格納する

            ObjectNode soilJson = Json.newObject();
            soilJson.put("soilId"  , soilData.soilId);				//土肥ID
            soilJson.put("soilName", soilData.soilName);			//土肥名
            soilJson.put("soilKind", soilData.soilKind);			//土肥種別
            soilJson.put("unitKind", soilData.unitKind);			//単位種別

            listJson.put(String.valueOf(soilData.soilId), soilJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.SOIL, listJson);

    }

    public void getIkubyoHistryBase(double workId) {

      /* 一覧表JSONオブジェクト */
      ObjectNode 	listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonApi = mapper.createArrayNode();

      int 	sanpuMethod		    = -1;
      double 	kikiId			= -1;
      double 	attachmentId	= -1;
      String	sanpuMethodName	= "";
      String	kikiName		= "";
      String	attachmentName	= "";
      String	nouhiName		= "";
      int 	unitKind		    = 0;
      double  farmId 			= Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      double  ikubyoDiaryId     = 0;

      NaeStatus naeStatusData = NaeStatus.getStatusOfNae(this.naeNo);

      /* 同一作業、同一苗の取得 */
      List<IkubyoDiary> idsl = IkubyoDiary.find.where().eq("work_id", workId).eq("nae_no", this.naeNo).orderBy("work_date desc").findList();
      if (idsl.size() > 0) {
        ikubyoDiaryId = idsl.get(0).ikubyoDiaryId;
      }
      else {
        /* 同一作業、別苗の取得 */
        List<IkubyoLine> ils = IkubyoLine.find.where().eq("work_id", workId).eq("farm_id", farmId).orderBy("work_date desc").findList();
        if (ils.size() > 0) {
          ikubyoDiaryId = ils.get(0).ikubyoDiaryId;
        }
      }

      List<IkubyoDiarySanpu> idss = IkubyoDiarySanpu.getIkubyoDiarySanpuList(ikubyoDiaryId);

      ObjectNode sanpu    = Json.newObject();
      ObjectNode nouhiList  = Json.newObject();
      ArrayNode nouhiListApi = mapper.createArrayNode();
      DecimalFormat df = new DecimalFormat("###0.00");

      for (IkubyoDiarySanpu ids : idss) {

        if (sanpuMethod != ids.sanpuMethod
            || kikiId != ids.kikiId
            || attachmentId != ids.attachmentId) {

          sanpu     = Json.newObject();
          nouhiList   = Json.newObject();
          nouhiListApi = mapper.createArrayNode();

          sanpuMethodName = Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, ids.sanpuMethod, true);
          kikiName    = Kiki.getKikiName(ids.kikiId);
          attachmentName  = Attachment.getAttachmentName(ids.attachmentId);

          sanpu.put("sanpuMethod"     , ids.sanpuMethod);
          sanpu.put("sanpuMethodName" , sanpuMethodName);
          sanpu.put("kikiId"          , ids.kikiId);
          sanpu.put("kikiName"        , kikiName);
          sanpu.put("attachmentId"    , ids.attachmentId);
          sanpu.put("attachmentName"  , attachmentName);
          if(this.apiFlg){
            sanpu.put("nouhiList"     , nouhiListApi);
            listJsonApi.add(sanpu);
          }else{
            sanpu.put("nouhiList"     , nouhiList);
            listJson.put(String.valueOf(ids.sanpuMethod), sanpu);
          }

        }

        ObjectNode nouhi  = Json.newObject();

        Nouhi nouhis = Nouhi.find.where().eq("nouhi_id",  ids.nouhiId).findUnique();
        double hosei = 1;
        if (nouhis.unitKind == 1 || nouhis.unitKind == 2) { //単位種別がKgかLの場合
          hosei = 0.001;
        }

        nouhiName   = Nouhi.getNouhiName(ids.nouhiId);
        unitKind    = Nouhi.getUnitKind(ids.nouhiId);
        double sanpryo = ids.sanpuryo;

        if (sanpryo <= 0) { /* 散布量が0以下の場合、参照しない */
          continue;
        }

        nouhi.put("nouhiId"   , ids.nouhiId);
        nouhi.put("nouhiName" , nouhiName);
        nouhi.put("bairitu"   , ids.bairitu);
        nouhi.put("sanpuryo"  , df.format(sanpryo * hosei));
        nouhi.put("unitKind"  , unitKind);
        nouhiList.put(String.valueOf(ids.nouhiId), nouhi);
        nouhiListApi.add(nouhi);

        sanpuMethod   = ids.sanpuMethod;
        kikiId        = ids.kikiId;
        attachmentId  = ids.attachmentId;

      }

      if(this.apiFlg){
      	resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonApi);
  		}else{
  			resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
  		}

    }

    public void getIkubyoDiarySanpu(double ikubyoDiaryId) {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJsonApi = mapper.createArrayNode();

        int 	sanpuMethod		= -1;
        double 	kikiId			= -1;
        double 	attachmentId	= -1;
        String	sanpuMethodName	= "";
        String	kikiName		= "";
        String	attachmentName	= "";
        String	nouhiName		= "";
        int 	unitKind		= 0;
        double 	farmId 			= Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

        if (this.ikubyoDiary == null) {
        	return;
        }

        List<IkubyoDiarySanpu> ids = IkubyoDiarySanpu.find.where().eq("ikubyo_diary_id", ikubyoDiaryId).orderBy("ikubyo_diary_sequence").findList();

        ObjectNode sanpu 		= Json.newObject();
      	ObjectNode nouhiList 	= Json.newObject();
      	ArrayNode nouhiListApi = mapper.createArrayNode();
        DecimalFormat df = new DecimalFormat("###0.00");


        for (IkubyoDiarySanpu ikubyoDiarySanpu : ids) {

        	if (sanpuMethod != ikubyoDiarySanpu.sanpuMethod
        			|| kikiId != ikubyoDiarySanpu.kikiId
        			|| attachmentId != ikubyoDiarySanpu.attachmentId) {

        		sanpu 		= Json.newObject();
        		nouhiList 	= Json.newObject();
        		nouhiListApi = mapper.createArrayNode();

        		sanpuMethodName = Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, ikubyoDiarySanpu.sanpuMethod, true);
        		kikiName		= Kiki.getKikiName(ikubyoDiarySanpu.kikiId);
        		attachmentName	= Attachment.getAttachmentName(ikubyoDiarySanpu.attachmentId);

        		sanpu.put("sanpuMethod"		, ikubyoDiarySanpu.sanpuMethod);
        		sanpu.put("sanpuMethodName"	, sanpuMethodName);
        		sanpu.put("kikiId"			, ikubyoDiarySanpu.kikiId);
        		sanpu.put("kikiName"		, kikiName);
        		sanpu.put("attachmentId"	, ikubyoDiarySanpu.attachmentId);
        		sanpu.put("attachmentName"	, attachmentName);
        		if(this.apiFlg){
        			sanpu.put("nouhiList"		, nouhiListApi);
        			listJsonApi.add(sanpu);
        		}else{
        			sanpu.put("nouhiList"		, nouhiList);
        			listJson.put(String.valueOf(ikubyoDiarySanpu.sanpuMethod), sanpu);
        		}

        	}

            ObjectNode nouhi	= Json.newObject();

            Nouhi nouhis = Nouhi.find.where().eq("nouhi_id",  ikubyoDiarySanpu.nouhiId).findUnique();
            double hosei = 1;
            if (nouhis.unitKind == 1 || nouhis.unitKind == 2) { //単位種別がKgかLの場合
              hosei = 0.001;
            }

            nouhiName		= Nouhi.getNouhiName(ikubyoDiarySanpu.nouhiId);
            unitKind		= Nouhi.getUnitKind(ikubyoDiarySanpu.nouhiId);

            nouhi.put("nouhiId"		, ikubyoDiarySanpu.nouhiId);
            nouhi.put("nouhiName"	, nouhiName);
            nouhi.put("bairitu"		, ikubyoDiarySanpu.bairitu);
            nouhi.put("sanpuryo"	, df.format(ikubyoDiarySanpu.sanpuryo * hosei));
            nouhi.put("unitKind"	, unitKind);
            nouhi.put("yukoSeibun", ikubyoDiarySanpu.yukoSeibun);
            nouhiList.put(String.valueOf(ikubyoDiarySanpu.nouhiId), nouhi);
            nouhiListApi.add(nouhi);

            sanpuMethod 	= ikubyoDiarySanpu.sanpuMethod;
            kikiId 			= ikubyoDiarySanpu.kikiId;
            attachmentId 	= ikubyoDiarySanpu.attachmentId;

        }
        if(this.apiFlg){
        	resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonApi);
    	}else{
    		resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
    	}

      }
      public void getIkubyoPlanSanpu(double ikubyoPlanId) {

        /* 一覧表JSONオブジェクト */
        ObjectNode  listJson   = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJsonApi = mapper.createArrayNode();

        int   sanpuMethod   = -1;
        double  kikiId      = -1;
        double  attachmentId  = -1;
        String  sanpuMethodName = "";
        String  kikiName    = "";
        String  attachmentName  = "";
        String  nouhiName   = "";
        int   unitKind    = 0;
        double  farmId      = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

        if (this.ikubyoPlan == null) {
          return;
        }

        List<IkubyoPlanSanpu> ips = IkubyoPlanSanpu.find.where().eq("ikubyo_plan_id", ikubyoPlanId).orderBy("ikubyo_diary_sequence").findList();

        ObjectNode sanpu    = Json.newObject();
        ObjectNode nouhiList  = Json.newObject();
        ArrayNode nouhiListApi = mapper.createArrayNode();
        DecimalFormat df = new DecimalFormat("###0.00");


        for (IkubyoPlanSanpu ikubyoPlanSanpu : ips) {

          if (sanpuMethod != ikubyoPlanSanpu.sanpuMethod
              || kikiId != ikubyoPlanSanpu.kikiId
              || attachmentId != ikubyoPlanSanpu.attachmentId) {

            sanpu     = Json.newObject();
            nouhiList   = Json.newObject();
            nouhiListApi = mapper.createArrayNode();

            sanpuMethodName = Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, ikubyoPlanSanpu.sanpuMethod, true);
            kikiName    = Kiki.getKikiName(ikubyoPlanSanpu.kikiId);
            attachmentName  = Attachment.getAttachmentName(ikubyoPlanSanpu.attachmentId);

            sanpu.put("sanpuMethod"     , ikubyoPlanSanpu.sanpuMethod);
            sanpu.put("sanpuMethodName" , sanpuMethodName);
            sanpu.put("kikiId"          , ikubyoPlanSanpu.kikiId);
            sanpu.put("kikiName"        , kikiName);
            sanpu.put("attachmentId"    , ikubyoPlanSanpu.attachmentId);
            sanpu.put("attachmentName"  , attachmentName);
            if(this.apiFlg){
              sanpu.put("nouhiList"   , nouhiListApi);
              listJsonApi.add(sanpu);
            }else{
              sanpu.put("nouhiList"   , nouhiList);
              listJson.put(String.valueOf(ikubyoPlanSanpu.sanpuMethod), sanpu);
            }

          }

          ObjectNode nouhi  = Json.newObject();

          Nouhi nouhis = Nouhi.find.where().eq("nouhi_id",  ikubyoPlanSanpu.nouhiId).findUnique();
          double hosei = 1;
          if (nouhis.unitKind == 1 || nouhis.unitKind == 2) { //単位種別がKgかLの場合
            hosei = 0.001;
          }

          nouhiName   = Nouhi.getNouhiName(ikubyoPlanSanpu.nouhiId);
          unitKind    = Nouhi.getUnitKind(ikubyoPlanSanpu.nouhiId);

          nouhi.put("nouhiId"   , ikubyoPlanSanpu.nouhiId);
          nouhi.put("nouhiName" , nouhiName);
          nouhi.put("bairitu"   , ikubyoPlanSanpu.bairitu);
          nouhi.put("sanpuryo"  , df.format(ikubyoPlanSanpu.sanpuryo * hosei));
          nouhi.put("unitKind"  , unitKind);
          nouhi.put("yukoSeibun", ikubyoPlanSanpu.yukoSeibun);
          nouhiList.put(String.valueOf(ikubyoPlanSanpu.nouhiId), nouhi);
          nouhiListApi.add(nouhi);

          sanpuMethod   = ikubyoPlanSanpu.sanpuMethod;
          kikiId        = ikubyoPlanSanpu.kikiId;
          attachmentId  = ikubyoPlanSanpu.attachmentId;

        }
        if(this.apiFlg){
          resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonApi);
        }else{
          resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
        }
    }

    public void getIkubyoDiaryDetail(double ikubyoDiaryId) {

      /* 一覧表JSONオブジェクト */
      ObjectNode  listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonArray = mapper.createArrayNode();

      double  farmId      = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

      if (this.ikubyoDiary == null) {
        return;
      }
      IkubyoDiary id = IkubyoDiary.find.where().eq("ikubyo_diary_id", ikubyoDiaryId).findUnique();
      Work wk = Work.getWork(id.workId);

      List<IkubyoDiaryDetail> idd = IkubyoDiaryDetail.find.where().eq("ikubyo_diary_id", ikubyoDiaryId).orderBy("ikubyo_diary_sequence").findList();

      for (IkubyoDiaryDetail ikubyoDiaryDetail : idd) {

        ObjectNode dt    = Json.newObject();
        dt.put("workDetailKind", ikubyoDiaryDetail.workDetailKind);
        listJson.put(String.valueOf((int)ikubyoDiaryDetail.ikubyoDiarySequence), dt);
        listJsonArray.add(dt);

      }

      int sequence = idd.size() + 1;

      for (int seq = sequence ; seq <= 5; seq++) {

        ObjectNode dt    = Json.newObject();
        dt.put("workDetailKind", 0);
        listJson.put(String.valueOf(seq), dt);
        listJsonArray.add(dt);

      }

      if(this.apiFlg){
    	  resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonArray);
      }else{
    	  resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
      }

  }
    public void getIkubyoPlanDetail(double ikubyoPlanId) {

      /* 一覧表JSONオブジェクト */
      ObjectNode  listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonArray = mapper.createArrayNode();

      double  farmId      = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

      if (this.ikubyoPlan == null) {
        return;
      }
      IkubyoPlan wp = IkubyoPlan.find.where().eq("ikubyo_plan_id", ikubyoPlanId).findUnique();
      Work wk = Work.getWork(wp.workId);

      List<IkubyoPlanDetail> ipd = IkubyoPlanDetail.find.where().eq("ikubyo_plan_id", ikubyoPlanId).orderBy("ikubyo_diary_sequence").findList();

      for (IkubyoPlanDetail ikubyoPlanDetail : ipd) {

        ObjectNode dt    = Json.newObject();
        dt.put("workDetailKind", ikubyoPlanDetail.workDetailKind);
        listJson.put(String.valueOf((int)ikubyoPlanDetail.ikubyoDiarySequence), dt);
        listJsonArray.add(dt);

      }

      int sequence = ipd.size() + 1;

      for (int seq = sequence ; seq <= 5; seq++) {

        ObjectNode dt    = Json.newObject();
        dt.put("workDetailKind", 0);
        listJson.put(String.valueOf(seq), dt);
        listJsonArray.add(dt);

      }

      if(this.apiFlg){
        resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonArray);
      }else{
        resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
      }

  }

  public static CommonIkubyoDiaryWork getCommonIkubyoDiaryWork(int pWorkTemplateId, Session session, ObjectNode resultJson) {
    CommonIkubyoDiaryWork cidk = null;        //育苗記録作業別項目

    /*----- 作業別項目 -----*/
    //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
    switch (pWorkTemplateId) {
    case AgryeelConst.WorkTemplate.SANPU:
      cidk = new HiryoSanpuIkubyoCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.KANSUI:
      cidk = new KansuiIkubyoCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.NAEHASHUIK:
      cidk = new NaehashuIkubyoCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.KARITORIIK:
      cidk = new KaritoriIkubyoCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.HAIKIIK:
      cidk = new HaikiIkubyoCompornent(session, resultJson);
      break;
    }
    return cidk;
  }

  public static CommonIkubyoDiaryWork getCommonIkubyoDiaryWorkApi(int pWorkTemplateId, Session session, ObjectNode resultJson) {
    CommonIkubyoDiaryWork cidk = null;        //育苗記録作業別項目

    /*----- 作業別項目 -----*/
    //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
    switch (pWorkTemplateId) {
    case AgryeelConst.WorkTemplate.SANPU:
      cidk = new HiryoSanpuIkubyoCompornent(session, resultJson, true);
      break;
    case AgryeelConst.WorkTemplate.KANSUI:
      cidk = new KansuiIkubyoCompornent(session, resultJson, true);
      break;
    case AgryeelConst.WorkTemplate.NAEHASHUIK:
      cidk = new NaehashuIkubyoCompornent(session, resultJson, true);
      break;
    case AgryeelConst.WorkTemplate.KARITORIIK:
      cidk = new KaritoriIkubyoCompornent(session, resultJson, true);
      break;
    case AgryeelConst.WorkTemplate.HAIKIIK:
      cidk = new HaikiIkubyoCompornent(session, resultJson, true);
      break;
    }
    return cidk;
  }
}
