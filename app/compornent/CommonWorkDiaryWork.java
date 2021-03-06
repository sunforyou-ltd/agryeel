package compornent;

import java.text.DecimalFormat;
import java.util.List;

import models.Account;
import models.Attachment;
import models.Belto;
import models.Common;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Crop;
import models.Hinsyu;
import models.Kiki;
import models.NaeStatus;
import models.Nisugata;
import models.Nouhi;
import models.NouhiOfCrop;
import models.Shitu;
import models.Size;
import models.TimeLine;
import models.Work;
import models.WorkDiary;
import models.WorkDiaryDetail;
import models.WorkDiarySanpu;
import models.WorkHistryBase;
import models.WorkHistryDetail;
import models.WorkLastTime;
import models.WorkPlan;
import models.WorkPlanDetail;
import models.WorkPlanSanpu;
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
public abstract class CommonWorkDiaryWork implements AgryellInterface {


    /**
     * HTTPセッション情報
     */
    public Session session;
    /**
     * ResultJsonData
     */
    public ObjectNode resultJson;

    /**
     * 作業記録
     */
    public WorkDiary workDiary = null;

    /**
     * 作業記録
     */
    public WorkLastTime wlt = null;

    /**
     * 区画ID
     */
    public double kukakuId;

    /**
     * 作業ID
     */
    public double workId;

    /**
     * APIフラグ
     */
    public Boolean apiFlg = false;
    /**
     * 作業計画
     */
    public WorkPlan workPlan = null;
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
    public CommonWorkDiaryWork(Session session, ObjectNode resultJson) {

        this.session 		= session;
        this.resultJson 	= resultJson;
        this.apiFlg = false;

        getAccountList();						//アカウント情報一覧
        getKukakuList();						//区画情報一覧


    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public CommonWorkDiaryWork(Session session, ObjectNode resultJson, Boolean api) {

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
     * 作業記録保存
     */
    synchronized public void commit(JsonNode input, WorkDiary wkd, Work wk) {

      double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(wkd.kukakuId);
      CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();

      this.mode = input.get("mode").asInt();

      //前回作業情報を取得する
      this.wlt = WorkChainCompornent.getWorkLastTime(wkd.workId, farmId, cws.cropId);
      if (this.wlt == null) { //前回作業情報が存在しない場合、新規データを作成する
        this.wlt = new WorkLastTime();
        this.wlt.farmId = farmId;
        this.wlt.workId = wkd.workId;
        this.wlt.cropId = cws.cropId;
        this.wlt.save();
      }

      this.wlt.workTime = wkd.workTime; //作業時間を格納する
      this.wlt.update();

    }
    /**
     * 作業計画保存
     */
    public void plan(JsonNode input, WorkPlan wkp, Work wk) {

      /* ここでは特に何もしない */

    }
    /**
     * 作業記録保存
     */
    synchronized public void saveHistry(WorkDiary wkd) {

      double   farmId                         = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(wkd.kukakuId);
      CompartmentWorkChainStatus cws          = compartmentStatusData.getWorkChainStatus();

      //前回作業情報を取得する
      this.wlt = WorkChainCompornent.getWorkLastTime(wkd.workId, farmId, cws.cropId);
      if (this.wlt == null) { //前回作業情報が存在しない場合、新規データを作成する
        this.wlt = new WorkLastTime();
        this.wlt.farmId = farmId;
        this.wlt.workId = wkd.workId;
        this.wlt.cropId = cws.cropId;
        this.wlt.save();
      }

      this.wlt.workTime = wkd.workTime; //作業時間を格納する
      this.wlt.update();

    }


    /**
     * 作業記録散布情報保存
     * @param input
     * @param wkd
     * @param wk
     */
    synchronized protected void commitSanpuInfo(JsonNode input, WorkDiary wkd, Work wk) {

        JsonNode nouhiInfoList = input.get("nouhiInfo");
        double 	 farmId 	     = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

        if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {

          WorkHistryBaseComprtnent whb = new WorkHistryBaseComprtnent(this.wlt.farmId, this.wlt.workId, this.wlt.cropId);                       //作業履歴共通コンポーネントの作成
          whb.get();

          int nouhiIndex = 0;

          for (WorkHistryBase workHistryBase : whb.listWorkHistryBase) {

            /* 作業記録農肥散布情報を生成する */
            WorkDiarySanpu wds = new WorkDiarySanpu();                                    //作業記録散布情報

            wds.workDiaryId         = wkd.workDiaryId;                                    //作業記録ＩＤ
            wds.workDiarySequence   = (nouhiIndex + 1);                                   //作業記録シーケンス
            wds.sanpuMethod         = workHistryBase.sanpuMethod;                         //散布方法
            wds.kikiId              = workHistryBase.kikiId;                              //機器ＩＤ
            wds.attachmentId        = workHistryBase.attachmentId;                        //アタッチメントＩＤ
            wds.nouhiId             = workHistryBase.nouhiId;                             //農肥ＩＤ
            wds.bairitu             = workHistryBase.bairitu;                             //倍率
            wds.sanpuryo            = workHistryBase.sanpuryo;                            //散布量
            wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;                       //区画状況照会反映フラグ
            wds.motochoUpdate       = AgryeelConst.UpdateFlag.NONE;                       //元帳照会反映フラグ

            wds.save();

            nouhiIndex++;

          }

        }
        else { //通常入力の場合
          if (nouhiInfoList != null && nouhiInfoList.size() > 0) {

            WorkHistryBaseComprtnent whb = new WorkHistryBaseComprtnent(this.wlt.farmId, this.wlt.workId, this.wlt.cropId);                       //作業履歴共通コンポーネントの作成

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
                Compartment cp = Compartment.getCompartmentInfo(wkd.kukakuId);
                if (cp.area != 0) {
                  nouhi.sanpuryo = (nouhi.sanpuryo / cp.area) * 10;
                }
                nouhi.update();

                /* 作業記録農肥散布情報を生成する */
                WorkDiarySanpu wds = new WorkDiarySanpu();                                    //作業記録散布情報

                wds.workDiaryId = wkd.workDiaryId;                                        //作業記録ＩＤ
                wds.workDiarySequence   = (nouhiIndex + 1);                                   //作業記録シーケンス
                String sanpuMethod  = nouhiInfoList.get(nouhiIndex).get("sanpuId").asText();        //散布方法
                if (sanpuMethod != null && !"".equals(sanpuMethod)) {
                  wds.sanpuMethod     = Integer.parseInt(sanpuMethod);
                }
                else {
                  wds.sanpuMethod     = 0;
                }
                String kikiId  = nouhiInfoList.get(nouhiIndex).get("kiki").asText();          //機器
                if (kikiId != null && !"".equals(kikiId)) {
                  wds.kikiId        = Double.parseDouble(kikiId);
                }
                else {
                  wds.kikiId        = 0;
                }
                String sAttachmentId  = nouhiInfoList.get(nouhiIndex).get("attachment").asText();
                if (sAttachmentId != null && !"".equals(sAttachmentId)) {
                    wds.attachmentId    = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("attachment").asText());   //アタッチメントＩＤ
                }
                else {
                    wds.attachmentId    = 0;                                        //アタッチメントＩＤ
                }
                wds.nouhiId       = nouhiId;                                        //農肥ＩＤ
                wds.bairitu       = nouhi.bairitu;                                  //倍率
                wds.sanpuryo      = sanpuryo;                                       //散布量
                wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;                             //区画状況照会反映フラグ
                wds.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;                             //元帳照会反映フラグ

                wds.save();

                whb.stack(wds);                                                 //散布情報を履歴情報として格納する

            }

            if (whb.iWorkHistryBaseCount > 0) {                                         //作業履歴共通情報が存在する場合

              whb.update();

            }
        }

        }


    }
    /**
     * 作業計画散布情報保存
     * @param input
     * @param wkd
     * @param wk
     */
    protected void planSanpuInfo(JsonNode input, WorkPlan wkp, Work wk) {

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

              /* 作業計画農肥散布情報を生成する */
              WorkPlanSanpu wds = new WorkPlanSanpu();                                  //作業記録散布情報

              wds.workPlanId = wkp.workPlanId;                                          //作業記録ＩＤ
              wds.workDiarySequence   = (nouhiIndex + 1);                               //作業記録シーケンス
              wds.sanpuMethod         = Integer.parseInt(nouhiInfoList.get(nouhiIndex).get("sanpuId").asText());      //散布方法
              wds.kikiId              = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("kiki").asText());       //機器ＩＤ
              String sAttachmentId  = nouhiInfoList.get(nouhiIndex).get("attachment").asText();
              if (sAttachmentId != null && !"".equals(sAttachmentId)) {
                  wds.attachmentId    = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("attachment").asText()); //アタッチメントＩＤ
              }
              else {
                  wds.attachmentId    = 0;                                                                            //アタッチメントＩＤ
              }
              wds.nouhiId       = nouhiId;                                                                            //農肥ＩＤ
              wds.bairitu       = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("bairitu").asText());          //倍率
              wds.sanpuryo      = Double.parseDouble(nouhiInfoList.get(nouhiIndex).get("sanpuryo").asText()) * hosei; //散布量
              wds.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;                                                 //区画状況照会反映フラグ
              wds.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;                                                   //元帳照会反映フラグ

              wds.save();

          }
      }
    }
    /**
     * 作業記録詳細情報保存
     * @param input
     * @param wkd
     * @param wk
     */
    synchronized protected void commitDetailInfo(JsonNode input, WorkDiary wkd, Work wk) {

        JsonNode detailInfoList = input.get("detailInfo");
        double   farmId        = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

        if (this.mode == AgryeelConst.WorkDiaryMode.WORKING) {

          WorkHistryDetailComprtnent whd = new WorkHistryDetailComprtnent(this.wlt.farmId, this.wlt.workId, this.wlt.cropId);                       //作業履歴詳細コンポーネントの作成
          whd.get();

          int seqIndex = 0;

          for (WorkHistryDetail workHistryDetail : whd.listWorkHistryDetail) {

            /* 作業記録詳細情報を生成する */
            WorkDiaryDetail wdd = new WorkDiaryDetail();                                    //作業記録散布情報

            wdd.workDiaryId         = wkd.workDiaryId;                                    //作業記録ＩＤ
            wdd.workDiarySequence   = (seqIndex + 1);                                     //作業記録シーケンス

            switch ((int)wk.workTemplateId) {
            case AgryeelConst.WorkTemplate.KAISHU:
              wdd.suryo   = workHistryDetail.suryo;                                       //数量
              break;

            case AgryeelConst.WorkTemplate.DACHAKU:
              wdd.sizaiId   = workHistryDetail.sizaiId;                                   //資材ID
              break;

            case AgryeelConst.WorkTemplate.COMMENT:
              //wdd.comment   = workHistryDetail.comment;                                   //コメント
              wdd.comment   = "";                                   //コメント
              break;

            case AgryeelConst.WorkTemplate.SHUKAKU:
              wdd.syukakuNisugata = workHistryDetail.syukakuNisugata;                             //荷姿
              wdd.syukakuSitsu    = workHistryDetail.syukakuSitsu;                                //質
              wdd.syukakuSize     = workHistryDetail.syukakuSize;                                 //サイズ
              wdd.syukakuKosu     = workHistryDetail.syukakuKosu;                                 //個数
              wdd.shukakuRyo      = workHistryDetail.shukakuRyo;                                  //収穫量
              this.shukakuryo    += wdd.shukakuRyo;
              break;

            case AgryeelConst.WorkTemplate.TEISHOKU:
              wdd.naeNo    = workHistryDetail.naeNo;                                //苗No
              wdd.kosu     = workHistryDetail.kosu;                                 //個数
              wdd.retusu   = workHistryDetail.retusu;                               //列数
              wdd.joukan   = workHistryDetail.joukan;                               //条間
              wdd.jousu    = workHistryDetail.jousu;                                //条数
              wdd.plantingDistance = workHistryDetail.plantingDistance;             //作付距離
              break;

            case AgryeelConst.WorkTemplate.SENKA:
              wdd.syukakuSitsu    = workHistryDetail.syukakuSitsu;                                //質
              wdd.syukakuSize     = workHistryDetail.syukakuSize;                                 //サイズ
              wdd.syukakuHakosu   = workHistryDetail.syukakuHakosu;                               //箱数
              wdd.syukakuKosu     = workHistryDetail.syukakuKosu;                                 //個数
              wdd.syukakuNinzu    = workHistryDetail.syukakuNinzu;                                //人数
              wdd.shukakuRyo      = workHistryDetail.shukakuRyo;                                  //収穫量
              this.shukakuryo    += wdd.shukakuRyo;
              break;

            default:
              break;
            }

            wdd.save();

            seqIndex++;

          }

        }
        else { //通常入力の場合
          if (detailInfoList != null && detailInfoList.size() > 0) {

            WorkHistryDetailComprtnent whd = new WorkHistryDetailComprtnent(this.wlt.farmId, this.wlt.workId, this.wlt.cropId);                       //作業履歴詳細コンポーネントの作成

            for (int seqIndex = 0; seqIndex < detailInfoList.size(); seqIndex++) {

              /* 作業記録詳細情報を生成する */
              WorkDiaryDetail wdd = new WorkDiaryDetail();                                    //作業記録散布情報

              wdd.workDiaryId         = wkd.workDiaryId;                                    //作業記録ＩＤ
              wdd.workDiarySequence   = (seqIndex + 1);                                     //作業記録シーケンス

              switch ((int)wk.workTemplateId) {
              case AgryeelConst.WorkTemplate.KAISHU:
                wdd.suryo   = Integer.parseInt(detailInfoList.get(seqIndex).get("suryo").asText());                                       //数量
                break;

              case AgryeelConst.WorkTemplate.DACHAKU:
                if (detailInfoList.get(seqIndex).get("sizaiId").asText() != null && !"".equals(detailInfoList.get(seqIndex).get("sizaiId").asText())) {
                  wdd.sizaiId   = Double.parseDouble(detailInfoList.get(seqIndex).get("sizaiId").asText());                                   //資材ID
                }
                else {
                  wdd.sizaiId   = 0;
                }
                break;

              case AgryeelConst.WorkTemplate.COMMENT:
                wdd.comment   = detailInfoList.get(seqIndex).get("comment").asText();                                   //コメント
                break;

              case AgryeelConst.WorkTemplate.SHUKAKU:
                try {
                  wdd.syukakuNisugata = Integer.parseInt(detailInfoList.get(seqIndex).get("nisugata").asText());                                   //荷姿
                }
                catch (Exception e) {
                  wdd.syukakuNisugata = 0;
                }
                try {
                  wdd.syukakuSitsu    = Integer.parseInt(detailInfoList.get(seqIndex).get("sitsu").asText());                                      //質
                }
                catch (Exception e) {
                  wdd.syukakuSitsu    = 0;
                }
                try {
                  wdd.syukakuSize     = Integer.parseInt(detailInfoList.get(seqIndex).get("size").asText());                                       //サイズ
                }
                catch (Exception e) {
                  wdd.syukakuSize     = 0;
                }
                try {
                  wdd.syukakuKosu     = Double.parseDouble(detailInfoList.get(seqIndex).get("kosu").asText());                                     //個数
                }
                catch (Exception e) {
                  wdd.syukakuKosu     = 0;
                }
                try {
                  wdd.shukakuRyo      = Double.parseDouble(detailInfoList.get(seqIndex).get("shukakuRyo").asText());                               //収穫量
                }
                catch (Exception e) {
                  wdd.shukakuRyo      = 0;
                }
                this.shukakuryo    += wdd.shukakuRyo;
                break;

              case AgryeelConst.WorkTemplate.TEISHOKU:
                String naeNo = detailInfoList.get(seqIndex).get("naeNo").asText();
                if (naeNo.indexOf("-") != -1) {
                  wdd.naeNo = naeNo;                                                                                   //苗No
                }
                else {
                  wdd.naeNo = "";                                                                                      //苗No
                }
                try {
                  wdd.kosu = Double.parseDouble(detailInfoList.get(seqIndex).get("kosu").asText());                    //個数
                }
                catch (Exception e) {
                  wdd.kosu = 0;
                }
                try {
                  wdd.retusu    = Double.parseDouble(detailInfoList.get(seqIndex).get("retusu").asText());             //列数
                }
                catch (Exception e) {
                  wdd.retusu    = 0;
                }
                try {
                  wdd.joukan     = Double.parseDouble(detailInfoList.get(seqIndex).get("joukan").asText());            //条間
                }
                catch (Exception e) {
                  wdd.joukan     = 0;
                }
                try {
                  wdd.jousu     = Double.parseDouble(detailInfoList.get(seqIndex).get("jousu").asText());              //条数
                }
                catch (Exception e) {
                  wdd.jousu     = 0;
                }
                try {
                  wdd.plantingDistance = Double.parseDouble(detailInfoList.get(seqIndex).get("pDistance").asText());   //作付距離
                }
                catch (Exception e) {
                  wdd.plantingDistance      = 0;
                }
                break;

              case AgryeelConst.WorkTemplate.SENKA:
                try {
                  wdd.syukakuSitsu    = Integer.parseInt(detailInfoList.get(seqIndex).get("sitsu").asText());                                      //質
                }
                catch (Exception e) {
                  wdd.syukakuSitsu    = 0;
                }
                try {
                  wdd.syukakuSize     = Integer.parseInt(detailInfoList.get(seqIndex).get("size").asText());                                       //サイズ
                }
                catch (Exception e) {
                  wdd.syukakuSize     = 0;
                }
                try {
                  wdd.syukakuHakosu     = Double.parseDouble(detailInfoList.get(seqIndex).get("hakosu").asText());                                 //箱数
                }
                catch (Exception e) {
                  wdd.syukakuHakosu     = 0;
                }
                try {
                  wdd.syukakuKosu     = Double.parseDouble(detailInfoList.get(seqIndex).get("kosu").asText());                                     //個数
                }
                catch (Exception e) {
                  wdd.syukakuKosu     = 0;
                }
                try {
                  wdd.syukakuNinzu    = Integer.parseInt(detailInfoList.get(seqIndex).get("ninzu").asText());                                      //人数
                }
                catch (Exception e) {
                  wdd.syukakuNinzu     = 0;
                }
                try {
                  wdd.shukakuRyo      = Double.parseDouble(detailInfoList.get(seqIndex).get("shukakuRyo").asText());                               //収穫量
                }
                catch (Exception e) {
                  wdd.shukakuRyo      = 0;
                }
                this.shukakuryo    += wdd.shukakuRyo;
                break;

              default:
                break;
              }
              wdd.save();

              whd.stack(wdd);

            }

            if (whd.iWorkHistryDetailCount > 0) {

              whd.update();

            }
        }

        }
    }
    /**
     * 作業計画詳細情報保存
     * @param input
     * @param wkd
     * @param wk
     */
    protected void planDetailInfo(JsonNode input, WorkPlan wkp, Work wk) {

        JsonNode detailInfoList = input.get("detailInfo");
        double   farmId        = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        String   naeNo = "";

        if (detailInfoList != null && detailInfoList.size() > 0) {

          for (int seqIndex = 0; seqIndex < detailInfoList.size(); seqIndex++) {

            /* 作業記録詳細情報を生成する */
            WorkPlanDetail wdd = new WorkPlanDetail();                                    //作業計画散布情報

            wdd.workPlanId         = wkp.workPlanId;                                      //作業計画ＩＤ
            wdd.workDiarySequence   = (seqIndex + 1);                                     //作業記録シーケンス

            switch ((int)wk.workTemplateId) {
            case AgryeelConst.WorkTemplate.KAISHU:
              wdd.suryo   = Integer.parseInt(detailInfoList.get(seqIndex).get("suryo").asText());                                       //数量
              break;

            case AgryeelConst.WorkTemplate.DACHAKU:
              if (detailInfoList.get(seqIndex).get("sizaiId").asText() != null && !"".equals(detailInfoList.get(seqIndex).get("sizaiId").asText())) {
                wdd.sizaiId   = Double.parseDouble(detailInfoList.get(seqIndex).get("sizaiId").asText());                                   //資材ID
              }
              else {
                wdd.sizaiId   = 0;
              }
              break;

            case AgryeelConst.WorkTemplate.COMMENT:
              wdd.comment   = detailInfoList.get(seqIndex).get("comment").asText();                                   //コメント
              break;

            case AgryeelConst.WorkTemplate.SHUKAKU:
              wdd.syukakuNisugata = Integer.parseInt(detailInfoList.get(seqIndex).get("nisugata").asText());                                   //荷姿
              wdd.syukakuSitsu    = Integer.parseInt(detailInfoList.get(seqIndex).get("sitsu").asText());                                      //質
              wdd.syukakuSize     = Integer.parseInt(detailInfoList.get(seqIndex).get("size").asText());                                       //サイズ
              wdd.syukakuKosu     = Double.parseDouble(detailInfoList.get(seqIndex).get("kosu").asText());                                       //個数
              wdd.shukakuRyo      = Double.parseDouble(detailInfoList.get(seqIndex).get("shukakuRyo").asText());                               //収穫量
              this.shukakuryo    += wdd.shukakuRyo;
              break;

            case AgryeelConst.WorkTemplate.TEISHOKU:
              naeNo = detailInfoList.get(seqIndex).get("naeNo").asText();
              if (naeNo.indexOf("-") != -1) {
                wdd.naeNo = detailInfoList.get(seqIndex).get("naeNo").asText();                                      //苗No
              }
              else {
                wdd.naeNo = "";                                                                                      //苗No
              }
              wdd.kosu   = Double.parseDouble(detailInfoList.get(seqIndex).get("kosu").asText());                    //個数
              wdd.retusu = Double.parseDouble(detailInfoList.get(seqIndex).get("retusu").asText());                  //列数
              wdd.joukan = Double.parseDouble(detailInfoList.get(seqIndex).get("joukan").asText());                  //条間
              wdd.jousu  = Double.parseDouble(detailInfoList.get(seqIndex).get("jousu").asText());                   //条数
              wdd.plantingDistance = Double.parseDouble(detailInfoList.get(seqIndex).get("pDistance").asText());     //作付距離
              break;

            case AgryeelConst.WorkTemplate.SENKA:
              wdd.syukakuSitsu    = Integer.parseInt(detailInfoList.get(seqIndex).get("sitsu").asText());                                      //質
              wdd.syukakuSize     = Integer.parseInt(detailInfoList.get(seqIndex).get("size").asText());                                       //サイズ
              wdd.syukakuHakosu   = Double.parseDouble(detailInfoList.get(seqIndex).get("hakosu").asText());                                   //箱数
              wdd.syukakuKosu     = Double.parseDouble(detailInfoList.get(seqIndex).get("kosu").asText());                                     //個数
              wdd.syukakuNinzu    = Integer.parseInt(detailInfoList.get(seqIndex).get("ninzu").asText());                                      //サイズ
              wdd.shukakuRyo      = Double.parseDouble(detailInfoList.get(seqIndex).get("shukakuRyo").asText());                               //収穫量
              this.shukakuryo    += wdd.shukakuRyo;
              break;

            default:
              break;
            }
            wdd.save();
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
     * 【AGRYEEL】区画情報一覧を取得する
     * @return
     */
    public void getKukakuList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

//        /* アカウント情報から作業対象区画情報を取得する */
//        List<WorkCompartment> workCompartment = WorkCompartment.find.where().eq("account_id", session.get(AgryeelConst.SessionKey.ACCOUNTID)).orderBy("kukaku_id").findList();
//
//        /* 区画IDから区画状況情報を取得する */
//        for (WorkCompartment workCompartmentData : workCompartment) {									//作業対象圃場分処理を行う
//
//            /* 区画IDから区画情報を取得する */
//            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", workCompartmentData.kukakuId).eq("farm_id", Double.parseDouble(session.get(AgryeelConst.SessionKey.FARMID))).findUnique();
//            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない
//
//            ObjectNode kukakuJson	= Json.newObject();
//            kukakuJson.put("kukakuId"				, compartmentData.kukakuId);						//区画ID
//            kukakuJson.put("kukakuName"				, compartmentData.kukakuName);						//区画名
//
//            listJson.put(String.valueOf(compartmentData.kukakuId), kukakuJson);
//
//        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.KUKAKU, listJson);

    }

//    【AICA】コンビネーションはワークチェーンの散布情報組み合わせに移行する
//    /**
//     * 【AGRYEEL】コンビネーション情報一覧を取得する
//     * @return
//     */
//    public void getCombiList(int combiKind) {
//
//        /* 一覧表JSONオブジェクト */
//        ObjectNode 	listJson   = Json.newObject();
//
//        String sWhereString		=	"";				// 検索条件
//
//        if ( combiKind != Combi.ConstKind.NONE ) {	// コンビネーション種別が指定されている場合
//            sWhereString		=	"combi_kind = " + combiKind;
//        }
//
//        /* 機器情報一覧を取得する */
//        List<Combi> combiList = Combi.find.where(sWhereString).orderBy("combi_id").findList();
//
//        for (Combi combiData : combiList) {											//コンビネーション情報をJSONデータに格納する
//
//            ObjectNode combiJson			= Json.newObject();
//            combiJson.put("combiId"			, combiData.combiId);					//コンビネーションID
//            combiJson.put("combiName"		, combiData.combiName);					//コンビネーション名
//
//            listJson.put(String.valueOf(combiData.combiId), combiJson);
//
//        }
//
//        resultJson.put(AgryeelConst.WorkDiary.DataList.COMBI, listJson);
//
//    }

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

        if (this.workDiary != null) {	/* 作業記録編集の場合 */
        	workId = this.workDiary.workId;
        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */
          workId = this.workPlan.workId;
        }
        else {
          workId = Double.parseDouble(session.get(AgryeelConst.SessionKey.WORKID));
        }


        /* 作業IDから作業情報を取得する */
        Work workData = (Work)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.WORK, "work_id = " + workId  );
        if (workData != null) {																		//作業情報が存在する場合
            //【AICA】TODO:ワークチェイン実装後に修正すること
            //String[] kikiKind 	= workData.onUseKikiKind.split(",");
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
     * 【AGRYEEL】上下限情報一覧を取得する
     * @return
     */
    public void getUpdownLimitList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        //【AICA】TODO:ワークチェイン実装後に修正すること
//        /* 上下限情報一覧を取得する */
//        List<UpdownLimit> updownLimitList = UpdownLimit.find.orderBy("item_id").findList();
//
//        for (UpdownLimit UpdownLimitData : updownLimitList) {						//機器情報をJSONデータに格納する
//
//            ObjectNode updownLimitJson		= Json.newObject();
//            updownLimitJson.put("itemId"			, UpdownLimitData.itemId);		//項目ID
//            updownLimitJson.put("itemName"			, UpdownLimitData.itemName);	//項目名
//            updownLimitJson.put("upLimit"			, UpdownLimitData.upLimit);		//上限値
//            updownLimitJson.put("downLimit"			, UpdownLimitData.downLimit);	//下限値
//
//            listJson.put(String.valueOf(UpdownLimitData.itemId), updownLimitJson);
//
//        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.UPDAOWN, listJson);

    }

    /**
     * 【AGRYEEL】荷姿情報一覧を取得する
     * @return
     */
    public void getNisugataList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 散布方法情報一覧を取得する */
        List<Common> commonList = Common.GetCommonList(Common.ConstClass.NISUGATA);

        for (Common commonData : commonList) {										//散布方法情報をJSONデータに格納する

            ObjectNode commonJson			= Json.newObject();
            commonJson.put("nisugataId"		, commonData.commonSeq);				//散布方法ID
            commonJson.put("nisugataName"	, commonData.commonName);				//散布方法名

            listJson.put(String.valueOf(commonData.commonSeq), commonJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.NISUGATA, listJson);

    }

    /**
     * 【AGRYEEL】質情報一覧を取得する
     * @return
     */
    public void getShituList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 散布方法情報一覧を取得する */
        List<Common> commonList = Common.GetCommonList(Common.ConstClass.SHITU);

        for (Common commonData : commonList) {										//散布方法情報をJSONデータに格納する

            ObjectNode commonJson			= Json.newObject();
            commonJson.put("shituId"		, commonData.commonSeq);				//散布方法ID
            commonJson.put("shituName"		, commonData.commonName);				//散布方法名

            listJson.put(String.valueOf(commonData.commonSeq), commonJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.SHITU, listJson);

    }

    /**
     * 【AGRYEEL】サイズ情報一覧を取得する
     * @return
     */
    public void getSizeList() {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 散布方法情報一覧を取得する */
        List<Common> commonList = Common.GetCommonList(Common.ConstClass.SIZE);

        for (Common commonData : commonList) {										//散布方法情報をJSONデータに格納する

            ObjectNode commonJson			= Json.newObject();
            commonJson.put("sizeId"		, commonData.commonSeq);				//散布方法ID
            commonJson.put("sizeName"	, commonData.commonName);				//散布方法名

            listJson.put(String.valueOf(commonData.commonSeq), commonJson);

        }

        resultJson.put(AgryeelConst.WorkDiary.DataList.SIZE, listJson);

    }

    /**
     * 【AGRYEEL】対象区画の生産物IDに紐づ前回農肥情報を取得する
     * @return
     */
    public void getPrevNouhiInfo(double kukakuId) {

        /* 一覧表JSONオブジェクト */
        ObjectNode 	listJson   = Json.newObject();

        /* 区画IDに紐づく生産物IDを取得する */
        Compartment compartment = (Compartment)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.COMPARTMENT, "kukaku_id = " + kukakuId  );

        if (compartment == null) { return; } 										//該当データなしの場合、処理を行わない

        /* 生産物IDに紐づく農肥情報を取得する */
        //【AICA】TODO:ワークチェイン実装後に修正すること
//        List<Model> nouhiOfCropList = CommonGetWorkDiaryData.GetDataList(CommonGetWorkDiaryData.InfoKindConst.NOUHIOFCROP, "crop_id = " + compartment.cropId, "nouhi_id");
//
//        for (Model nouhiOfCropData : nouhiOfCropList) {								//前回農肥情報をJSONデータに格納する
//
//            /* 農肥IDより農肥情報を取得する */
//            Nouhi nouhi = (Nouhi)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.NOUHI, "nouhi_id" + ((NouhiOfCrop)nouhiOfCropData).nouhiId);
//
//            if (nouhi == null ) { return; }											//該当データなしの場合、処理を行わない
//
//            ObjectNode nouhiOfCropJson					= Json.newObject();
//            nouhiOfCropJson.put("nouhiId"				, nouhi.nouhiId);			//農肥ID
//            nouhiOfCropJson.put("nouhiName"				, nouhi.nouhiName);			//農肥名
//            nouhiOfCropJson.put("bairitu"				, nouhi.bairitu);			//倍率
//            nouhiOfCropJson.put("sanpuryo"				, nouhi.sanpuryo);			//散布量
//
//            listJson.put(String.valueOf(nouhi.nouhiId), nouhiOfCropJson);
//
//        }

        resultJson.put(AgryeelConst.WorkDiary.PrevData.NOUHIINFO, listJson);

    }

    public void getWorkHistryBase(double workId) {

      /* 一覧表JSONオブジェクト */
      ObjectNode 	listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonApi = mapper.createArrayNode();

      int 	sanpuMethod		    = -1;
      double 	kikiId			    = -1;
      double 	attachmentId	  = -1;
      String	sanpuMethodName	= "";
      String	kikiName		    = "";
      String	attachmentName	= "";
      String	nouhiName		    = "";
      int 	unitKind		      = 0;
      double 	farmId 			    = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      double  workDiaryId     = 0;
      double  oldArea         = 0;
      double  newArea         = 0;

      CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
      CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();

      /* 同一作業、同一区画の取得 */
      List<WorkDiary> wdsl = WorkDiary.find.where().eq("work_id", workId).eq("kukaku_id", this.kukakuId).orderBy("work_date desc").findList();
      if (wdsl.size() > 0) {
        workDiaryId = wdsl.get(0).workDiaryId;
      }
      else {
        /* 同一作業、別区画の取得 */
        List<TimeLine> tls = TimeLine.find.where().eq("work_id", workId).eq("farm_id", farmId).orderBy("work_date desc").findList();
        if (tls.size() > 0) {
          workDiaryId = tls.get(0).workDiaryId;
          Compartment cp = Compartment.getCompartmentInfo(tls.get(0).kukakuId);
          if (cp != null) {
            oldArea = cp.area;
          }
        }
      }
      Compartment cp = Compartment.getCompartmentInfo(this.kukakuId);
      if (cp != null) {
        newArea = cp.area;
      }

      List<WorkDiarySanpu> wdss = WorkDiarySanpu.getWorkDiarySanpuList(workDiaryId);

      ObjectNode sanpu    = Json.newObject();
      ObjectNode nouhiList  = Json.newObject();
      ArrayNode nouhiListApi = mapper.createArrayNode();
      DecimalFormat df = new DecimalFormat("###0.00");

      for (WorkDiarySanpu wds : wdss) {

        if (sanpuMethod != wds.sanpuMethod
            || kikiId != wds.kikiId
            || attachmentId != wds.attachmentId) {

          sanpu     = Json.newObject();
          nouhiList   = Json.newObject();
          nouhiListApi = mapper.createArrayNode();

          sanpuMethodName = Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, wds.sanpuMethod, true);
          kikiName    = Kiki.getKikiName(wds.kikiId);
          attachmentName  = Attachment.getAttachmentName(wds.attachmentId);

          sanpu.put("sanpuMethod"     , wds.sanpuMethod);
          sanpu.put("sanpuMethodName" , sanpuMethodName);
          sanpu.put("kikiId"          , wds.kikiId);
          sanpu.put("kikiName"        , kikiName);
          sanpu.put("attachmentId"    , wds.attachmentId);
          sanpu.put("attachmentName"  , attachmentName);
          if(this.apiFlg){
            sanpu.put("nouhiList"     , nouhiListApi);
            listJsonApi.add(sanpu);
          }else{
            sanpu.put("nouhiList"     , nouhiList);
            listJson.put(String.valueOf(wds.sanpuMethod), sanpu);
          }

        }

        ObjectNode nouhi  = Json.newObject();

        Nouhi nouhis = Nouhi.find.where().eq("nouhi_id",  wds.nouhiId).findUnique();
        double hosei = 1;
        if (nouhis.unitKind == 1 || nouhis.unitKind == 2) { //単位種別がKgかLの場合
          hosei = 0.001;
        }

        nouhiName   = Nouhi.getNouhiName(wds.nouhiId);
        unitKind    = Nouhi.getUnitKind(wds.nouhiId);
        double sanpryo = wds.sanpuryo;
        if (oldArea != 0) {
          sanpryo = (int)(sanpryo / oldArea * newArea);
        }

        if (sanpryo <= 0) { /* 散布量が0以下の場合、参照しない */
          continue;
        }

        nouhi.put("nouhiId"   , wds.nouhiId);
        nouhi.put("nouhiName" , nouhiName);
        nouhi.put("bairitu"   , wds.bairitu);
        nouhi.put("sanpuryo"  , df.format(sanpryo * hosei));
        nouhi.put("unitKind"  , unitKind);
        nouhiList.put(String.valueOf(wds.nouhiId), nouhi);
        nouhiListApi.add(nouhi);

        sanpuMethod   = wds.sanpuMethod;
        kikiId        = wds.kikiId;
        attachmentId  = wds.attachmentId;

      }


//----- 算出方法の変更に伴い、コメント化 -----
//    	WorkHistryBaseComprtnent whb = new WorkHistryBaseComprtnent(farmId, workId, cws.cropId);								//作業履歴共通コンポーネントの作成
//    	whb.get();
//
//      ObjectNode sanpu 		= Json.newObject();
//      ObjectNode nouhiList 	= Json.newObject();
//    	ArrayNode nouhiListApi = mapper.createArrayNode();
//
//      for (WorkHistryBase workHistryBase : whb.listWorkHistryBase) {
//
//      	if (sanpuMethod != workHistryBase.sanpuMethod
//      			|| kikiId != workHistryBase.kikiId
//      			|| attachmentId != workHistryBase.attachmentId) {
//
//      		sanpu 		= Json.newObject();
//      		nouhiList 	= Json.newObject();
//      		nouhiListApi = mapper.createArrayNode();
//
//      		sanpuMethodName = Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, workHistryBase.sanpuMethod);
//      		kikiName		= Kiki.getKikiName(workHistryBase.kikiId);
//      		attachmentName	= Attachment.getAttachmentName(workHistryBase.attachmentId);
//
//      		sanpu.put("sanpuMethod"		, workHistryBase.sanpuMethod);
//      		sanpu.put("sanpuMethodName"	, sanpuMethodName);
//      		sanpu.put("kikiId"			, workHistryBase.kikiId);
//      		sanpu.put("kikiName"		, kikiName);
//      		sanpu.put("attachmentId"	, workHistryBase.attachmentId);
//      		sanpu.put("attachmentName"	, attachmentName);
//      		if(this.apiFlg){
//      			sanpu.put("nouhiList"		, nouhiListApi);
//      			listJsonApi.add(sanpu);
//      		}else{
//          		sanpu.put("nouhiList"		, nouhiList);
//      			listJson.put(String.valueOf(workHistryBase.sanpuMethod), sanpu);
//      		}
//
//      	}
//
//          ObjectNode nouhi	= Json.newObject();
//
//          Nouhi nouhis = Nouhi.find.where().eq("nouhi_id",  workHistryBase.nouhiId).findUnique();
//          double hosei = 1;
//          if (nouhis.unitKind == 1 || nouhis.unitKind == 2) { //単位種別がKgかLの場合
//            hosei = 0.001;
//          }
//
//          nouhiName		= Nouhi.getNouhiName(workHistryBase.nouhiId);
//          unitKind		= Nouhi.getUnitKind(workHistryBase.nouhiId);
//
//          nouhi.put("nouhiId"		, workHistryBase.nouhiId);
//          nouhi.put("nouhiName"	, nouhiName);
//          nouhi.put("bairitu"		, workHistryBase.bairitu);
//          nouhi.put("sanpuryo"	, workHistryBase.sanpuryo * hosei);
//          nouhi.put("unitKind"	, unitKind);
//          nouhiList.put(String.valueOf(workHistryBase.nouhiId), nouhi);
//          nouhiListApi.add(nouhi);
//
//          sanpuMethod 	= workHistryBase.sanpuMethod;
//          kikiId 			= workHistryBase.kikiId;
//          attachmentId 	= workHistryBase.attachmentId;
//
//      }

      if(this.apiFlg){
      	resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonApi);
  		}else{
  			resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
  		}
    }

    public void getWorkDiarySanpu(double workDiaryId) {

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

        if (this.workDiary == null) {
        	return;
        }

        List<WorkDiarySanpu> wds = WorkDiarySanpu.find.where().eq("work_diary_id", workDiaryId).orderBy("work_diary_sequence").findList();

        ObjectNode sanpu 		= Json.newObject();
      	ObjectNode nouhiList 	= Json.newObject();
      	ArrayNode nouhiListApi = mapper.createArrayNode();
        DecimalFormat df = new DecimalFormat("###0.00");


        for (WorkDiarySanpu workDiarySanpu : wds) {

        	if (sanpuMethod != workDiarySanpu.sanpuMethod
        			|| kikiId != workDiarySanpu.kikiId
        			|| attachmentId != workDiarySanpu.attachmentId) {

        		sanpu 		= Json.newObject();
        		nouhiList 	= Json.newObject();
        		nouhiListApi = mapper.createArrayNode();

        		sanpuMethodName = Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, workDiarySanpu.sanpuMethod, true);
        		kikiName		= Kiki.getKikiName(workDiarySanpu.kikiId);
        		attachmentName	= Attachment.getAttachmentName(workDiarySanpu.attachmentId);

        		sanpu.put("sanpuMethod"		, workDiarySanpu.sanpuMethod);
        		sanpu.put("sanpuMethodName"	, sanpuMethodName);
        		sanpu.put("kikiId"			, workDiarySanpu.kikiId);
        		sanpu.put("kikiName"		, kikiName);
        		sanpu.put("attachmentId"	, workDiarySanpu.attachmentId);
        		sanpu.put("attachmentName"	, attachmentName);
        		if(this.apiFlg){
        			sanpu.put("nouhiList"		, nouhiListApi);
        			listJsonApi.add(sanpu);
        		}else{
        			sanpu.put("nouhiList"		, nouhiList);
        			listJson.put(String.valueOf(workDiarySanpu.sanpuMethod), sanpu);
        		}

        	}

            ObjectNode nouhi	= Json.newObject();

            Nouhi nouhis = Nouhi.find.where().eq("nouhi_id",  workDiarySanpu.nouhiId).findUnique();
            double hosei = 1;
            if (nouhis.unitKind == 1 || nouhis.unitKind == 2) { //単位種別がKgかLの場合
              hosei = 0.001;
            }

            nouhiName		= Nouhi.getNouhiName(workDiarySanpu.nouhiId);
            unitKind		= Nouhi.getUnitKind(workDiarySanpu.nouhiId);

            nouhi.put("nouhiId"		, workDiarySanpu.nouhiId);
            nouhi.put("nouhiName"	, nouhiName);
            nouhi.put("bairitu"		, workDiarySanpu.bairitu);
            nouhi.put("sanpuryo"	, df.format(workDiarySanpu.sanpuryo * hosei));
            nouhi.put("unitKind"	, unitKind);
            nouhiList.put(String.valueOf(workDiarySanpu.nouhiId), nouhi);
            nouhiListApi.add(nouhi);

            sanpuMethod 	= workDiarySanpu.sanpuMethod;
            kikiId 			= workDiarySanpu.kikiId;
            attachmentId 	= workDiarySanpu.attachmentId;

        }
        if(this.apiFlg){
        	resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonApi);
    		}else{
    			resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
    		}

      }
      public void getWorkPlanSanpu(double workPlanId) {

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

        if (this.workPlan == null) {
          return;
        }

        List<WorkPlanSanpu> wps = WorkPlanSanpu.find.where().eq("work_plan_id", workPlanId).orderBy("work_diary_sequence").findList();

        ObjectNode sanpu    = Json.newObject();
        ObjectNode nouhiList  = Json.newObject();
        ArrayNode nouhiListApi = mapper.createArrayNode();
        DecimalFormat df = new DecimalFormat("###0.00");


        for (WorkPlanSanpu workPlanSanpu : wps) {

          if (sanpuMethod != workPlanSanpu.sanpuMethod
              || kikiId != workPlanSanpu.kikiId
              || attachmentId != workPlanSanpu.attachmentId) {

            sanpu     = Json.newObject();
            nouhiList   = Json.newObject();
            nouhiListApi = mapper.createArrayNode();

            sanpuMethodName = Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, workPlanSanpu.sanpuMethod, true);
            kikiName    = Kiki.getKikiName(workPlanSanpu.kikiId);
            attachmentName  = Attachment.getAttachmentName(workPlanSanpu.attachmentId);

            sanpu.put("sanpuMethod"   , workPlanSanpu.sanpuMethod);
            sanpu.put("sanpuMethodName" , sanpuMethodName);
            sanpu.put("kikiId"      , workPlanSanpu.kikiId);
            sanpu.put("kikiName"    , kikiName);
            sanpu.put("attachmentId"  , workPlanSanpu.attachmentId);
            sanpu.put("attachmentName"  , attachmentName);
            if(this.apiFlg){
              sanpu.put("nouhiList"   , nouhiListApi);
              listJsonApi.add(sanpu);
            }else{
              sanpu.put("nouhiList"   , nouhiList);
              listJson.put(String.valueOf(workPlanSanpu.sanpuMethod), sanpu);
            }

          }

          ObjectNode nouhi  = Json.newObject();

          Nouhi nouhis = Nouhi.find.where().eq("nouhi_id",  workPlanSanpu.nouhiId).findUnique();
          double hosei = 1;
          if (nouhis.unitKind == 1 || nouhis.unitKind == 2) { //単位種別がKgかLの場合
            hosei = 0.001;
          }

          nouhiName   = Nouhi.getNouhiName(workPlanSanpu.nouhiId);
          unitKind    = Nouhi.getUnitKind(workPlanSanpu.nouhiId);

          nouhi.put("nouhiId"   , workPlanSanpu.nouhiId);
          nouhi.put("nouhiName" , nouhiName);
          nouhi.put("bairitu"   , workPlanSanpu.bairitu);
          nouhi.put("sanpuryo"  , df.format(workPlanSanpu.sanpuryo * hosei));
          nouhi.put("unitKind"  , unitKind);
          nouhiList.put(String.valueOf(workPlanSanpu.nouhiId), nouhi);
          nouhiListApi.add(nouhi);

          sanpuMethod   = workPlanSanpu.sanpuMethod;
          kikiId      = workPlanSanpu.kikiId;
          attachmentId  = workPlanSanpu.attachmentId;

        }
        if(this.apiFlg){
          resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonApi);
        }else{
          resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
        }
    }

    public void getWorkLastTime(double workId) {

        double 	farmId	 		= Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
        double  cropId      = 0;

        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
        CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();

        if (cws != null) {
          cropId = cws.cropId;
        }

        WorkLastTime wlt		= WorkLastTime.find.where().eq("farm_id", farmId).eq("work_id", workId).eq("crop_id", cropId).findUnique();

        if (wlt == null && (workId != AgryeelConst.WorkInfo.SHUKAKU)) { return; }

    	/*----- 作業共通情報を設定 -----*/
        if (wlt != null) {
            resultJson.put("workTime"	, wlt.workTime);									//作業時間
        }
        else {
            resultJson.put("workTime"	, 0);												//作業時間
        }

        Work work = Work.getWork(workId);

        switch ((int)work.workTemplateId) {
        case AgryeelConst.WorkTemplate.NOMAL:
          //作業時間のみ
          break;
        case AgryeelConst.WorkTemplate.SANPU:
          break;
        case AgryeelConst.WorkTemplate.NOUKO:
          if (wlt != null) {
          }
          break;
        }
    }
    public void getWorkHistryDetail(double workId) {

      /* 一覧表JSONオブジェクト */
      ObjectNode  listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonArray = mapper.createArrayNode();

      double  farmId          = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

      CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(this.kukakuId);
      CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();
      Work wk = Work.getWork(workId);

      WorkHistryDetailComprtnent whd = new WorkHistryDetailComprtnent(farmId, workId, cws.cropId);                //作業履歴共通コンポーネントの作成
      whd.get();

      if (wk.workTemplateId == AgryeelConst.WorkTemplate.SHUKAKU) { //収穫の場合

        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(this.session.get(AgryeelConst.SessionKey.ACCOUNTID));
        int nisugatarireki = accountComprtnent.accountStatusData.nisugataRireki;

        if (nisugatarireki == 0) { /* 収穫全体で履歴値参照 */
          for (WorkHistryDetail workHistryDetail : whd.listWorkHistryDetail) {

            ObjectNode dt    = Json.newObject();
            dt.put("nisugata"   , workHistryDetail.syukakuNisugata);
            dt.put("sitsu"      , workHistryDetail.syukakuSitsu);
            dt.put("size"       , workHistryDetail.syukakuSize);
            dt.put("kosu"       , workHistryDetail.syukakuKosu);
            dt.put("shukakuRyo" , workHistryDetail.shukakuRyo);
            String cmnNisugata                      = "";
            String cmnSitsu                         = "";
            String cmnSize                          = "";
            cmnNisugata = Nisugata.getNisugataName(workHistryDetail.syukakuNisugata);
            Nisugata n = Nisugata.getNisugataInfo(workHistryDetail.syukakuNisugata);
            if (n != null) {
              dt.put("capa"       , n.capacity);
            }
            else {
              dt.put("capa"         , 0);
            }
            cmnSitsu  = Shitu.getShituName(workHistryDetail.syukakuSitsu);
            cmnSize   = Size.getSizeName(workHistryDetail.syukakuSize);
            if ("".equals(cmnNisugata)) {
              cmnNisugata = "未選択";
            }
            if ("".equals(cmnSitsu)) {
              cmnSitsu = "未選択";
            }
            if ("".equals(cmnSize)) {
              cmnSize = "未選択";
            }
            dt.put("nisugataSpan" , cmnNisugata);
            dt.put("sitsuSpan"    , cmnSitsu);
            dt.put("sizeSpan"   , cmnSize);
            listJson.put(String.valueOf(workHistryDetail.workHistrySequence), dt);
            listJsonArray.add(dt);

          }
        }
        else {
          whd.iWorkHistryDetailCount = 0;
        }

        int sequence = whd.iWorkHistryDetailCount + 1;

        for (int seq = sequence ; seq <= 10; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("nisugata"     , 0);
          dt.put("sitsu"        , 0);
          dt.put("size"         , 0);
          dt.put("kosu"         , 0);
          dt.put("capa"         , 0);
          dt.put("shukakuRyo"   , 0);
          dt.put("nisugataSpan" , "未選択");
          dt.put("sitsuSpan"    , "未選択");
          dt.put("sizeSpan"     , "未選択");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }
      }
      else if (wk.workTemplateId == AgryeelConst.WorkTemplate.TEISHOKU) { //定植の場合
        for (WorkHistryDetail workHistryDetail : whd.listWorkHistryDetail) {
          ObjectNode dt    = Json.newObject();
          dt.put("naeNo" , workHistryDetail.naeNo);
          if (!workHistryDetail.naeNo.equals("")) {
            NaeStatus ns = NaeStatus.getStatusOfNae(workHistryDetail.naeNo);
            String[] naeNos = workHistryDetail.naeNo.split("-");
            dt.put("naeName"  , ns.hinsyuName + "(" + naeNos[1] + ")");
            dt.put("hinsyuId" , ns.hinsyuId);
            dt.put("zaikoKosu", ns.kosu);
          }
          else {
            dt.put("naeName"  , "");
            dt.put("hinsyuId" , 0);
            dt.put("zaikoKosu", 0);
          }
          dt.put("kosu"  , workHistryDetail.kosu);
          dt.put("retusu", workHistryDetail.retusu);
          dt.put("joukan", workHistryDetail.joukan);
          dt.put("jousu" , workHistryDetail.jousu);
          dt.put("pDistance" , workHistryDetail.plantingDistance);
          listJson.put(String.valueOf(workHistryDetail.workHistrySequence), dt);
          listJsonArray.add(dt);
        }
      }
      else if (wk.workTemplateId == AgryeelConst.WorkTemplate.SENKA) { //選花の場合

        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(this.session.get(AgryeelConst.SessionKey.ACCOUNTID));
        int nisugatarireki = accountComprtnent.accountStatusData.nisugataRireki;

        if (nisugatarireki == 0) { /* 収穫全体で履歴値参照 */
          for (WorkHistryDetail workHistryDetail : whd.listWorkHistryDetail) {

            ObjectNode dt    = Json.newObject();
            dt.put("sitsu"      , workHistryDetail.syukakuSitsu);
            dt.put("size"       , workHistryDetail.syukakuSize);
            dt.put("hakosu"     , workHistryDetail.syukakuHakosu);
            dt.put("kosu"       , workHistryDetail.syukakuKosu);
            dt.put("ninzu"      , workHistryDetail.syukakuNinzu);
            dt.put("shukakuRyo" , workHistryDetail.shukakuRyo);
            String cmnSitsu                         = "";
            String cmnSize                          = "";
            cmnSitsu  = Shitu.getShituName(workHistryDetail.syukakuSitsu);
            cmnSize   = Size.getSizeName(workHistryDetail.syukakuSize);
            if ("".equals(cmnSitsu)) {
              cmnSitsu = "未選択";
            }
            if ("".equals(cmnSize)) {
              cmnSize = "未選択";
            }
            dt.put("sitsuSpan"    , cmnSitsu);
            dt.put("sizeSpan"   , cmnSize);
            listJson.put(String.valueOf(workHistryDetail.workHistrySequence), dt);
            listJsonArray.add(dt);

          }
        }
        else {
          whd.iWorkHistryDetailCount = 0;
        }

        int sequence = whd.iWorkHistryDetailCount + 1;

        for (int seq = sequence ; seq <= 10; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("sitsu"        , 0);
          dt.put("size"         , 0);
          dt.put("hakosu"       , 0);
          dt.put("kosu"         , 0);
          dt.put("ninzu"        , 0);
          dt.put("shukakuRyo"   , 0);
          dt.put("sitsuSpan"    , "未選択");
          dt.put("sizeSpan"     , "未選択");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }
      }
      else {
        for (WorkHistryDetail workHistryDetail : whd.listWorkHistryDetail) {

          ObjectNode dt    = Json.newObject();
          dt.put("workDetailKind", workHistryDetail.workDetailKind);
          dt.put("suryo", workHistryDetail.suryo);
          dt.put("sizaiId", workHistryDetail.sizaiId);
          dt.put("sizaiSpan", Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)workHistryDetail.sizaiId, true));
          //過去の履歴を参照しないようにする
          //dt.put("comment", workHistryDetail.comment);
          dt.put("comment", "");
          listJson.put(String.valueOf(workHistryDetail.workHistrySequence), dt);
          listJsonArray.add(dt);

        }

        int sequence = whd.iWorkHistryDetailCount + 1;

        for (int seq = sequence ; seq <= 5; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("workDetailKind", 0);
          dt.put("suryo", 0);
          dt.put("sizaiId", 0);
          dt.put("sizaiSpan", "未選択");
          dt.put("comment", "");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }
      }


      if(this.apiFlg){
    	  resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonArray);
      }else{
    	  resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
      }

    }
    public void getWorkDiaryDetail(double workDiaryId) {

      /* 一覧表JSONオブジェクト */
      ObjectNode  listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonArray = mapper.createArrayNode();

      double  farmId      = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

      if (this.workDiary == null) {
        return;
      }
      WorkDiary wd = WorkDiary.find.where().eq("work_diary_id", workDiaryId).findUnique();
      Work wk = Work.getWork(wd.workId);

      List<WorkDiaryDetail> wdd = WorkDiaryDetail.find.where().eq("work_diary_id", workDiaryId).orderBy("work_diary_sequence").findList();

      if (wk.workTemplateId == AgryeelConst.WorkTemplate.SHUKAKU) { //収穫の場合
        for (WorkDiaryDetail workDiaryDetail : wdd) {

          ObjectNode dt    = Json.newObject();
          dt.put("nisugata"   , workDiaryDetail.syukakuNisugata);
          dt.put("sitsu"      , workDiaryDetail.syukakuSitsu);
          dt.put("size"       , workDiaryDetail.syukakuSize);
          dt.put("kosu"       , workDiaryDetail.syukakuKosu);
          dt.put("shukakuRyo" , workDiaryDetail.shukakuRyo);
          String cmnNisugata                      = "";
          String cmnSitsu                         = "";
          String cmnSize                          = "";
          cmnNisugata = Nisugata.getNisugataName(workDiaryDetail.syukakuNisugata);
          Nisugata n = Nisugata.getNisugataInfo(workDiaryDetail.syukakuNisugata);
          if (n != null) {
            dt.put("capa"       , n.capacity);
          }
          else {
            dt.put("capa"         , 0);
          }
          cmnSitsu  = Shitu.getShituName(workDiaryDetail.syukakuSitsu);
          cmnSize   = Size.getSizeName(workDiaryDetail.syukakuSize);
          if ("".equals(cmnNisugata)) {
            cmnNisugata = "未選択";
          }
          if ("".equals(cmnSitsu)) {
            cmnSitsu = "未選択";
          }
          if ("".equals(cmnSize)) {
            cmnSize = "未選択";
          }
          dt.put("nisugataSpan" , cmnNisugata);
          dt.put("sitsuSpan"    , cmnSitsu);
          dt.put("sizeSpan"     , cmnSize);
          listJson.put(String.valueOf((int)workDiaryDetail.workDiarySequence), dt);
          listJsonArray.add(dt);

        }

        int sequence = wdd.size() + 1;

        for (int seq = sequence ; seq <= 10; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("nisugata"     , 0);
          dt.put("sitsu"        , 0);
          dt.put("size"         , 0);
          dt.put("kosu"         , 0);
          dt.put("capa"         , 0);
          dt.put("shukakuRyo"   , 0);
          dt.put("nisugataSpan" , "未選択");
          dt.put("sitsuSpan"    , "未選択");
          dt.put("sizeSpan"     , "未選択");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }
      }
      else if (wk.workTemplateId == AgryeelConst.WorkTemplate.TEISHOKU) { //定植の場合
        int idx = 0;
        for (WorkDiaryDetail workDiaryDetail : wdd) {
          ObjectNode dt    = Json.newObject();
          if (!workDiaryDetail.naeNo.equals("")) {
            NaeStatus ns = NaeStatus.getStatusOfNae(workDiaryDetail.naeNo);
            String[] naeNos = workDiaryDetail.naeNo.split("-");
            dt.put("naeNo" , workDiaryDetail.naeNo);
            dt.put("naeName"  , ns.hinsyuName + "(" + naeNos[1] + ")");
            dt.put("hinsyuId" , ns.hinsyuId);
            dt.put("zaikoKosu", ns.kosu);
          }
          else {
            String[] hinsyus = wd.hinsyuId.split(",");
            double hinsyuId = Double.parseDouble(hinsyus[(int)workDiaryDetail.workDiarySequence - 1]);
            dt.put("naeNo" , 0);
            dt.put("naeName"  , Hinsyu.getHinsyuName(hinsyuId));
            dt.put("hinsyuId" , hinsyuId);
            dt.put("zaikoKosu", 0);
          }
          dt.put("kosu"  , workDiaryDetail.kosu);
          dt.put("retusu", workDiaryDetail.retusu);
          dt.put("joukan", workDiaryDetail.joukan);
          dt.put("jousu" , workDiaryDetail.jousu);
          dt.put("pDistance" , workDiaryDetail.plantingDistance);
          listJson.put(String.valueOf((int)workDiaryDetail.workDiarySequence), dt);
          listJsonArray.add(dt);
        }
      }
      else if (wk.workTemplateId == AgryeelConst.WorkTemplate.SENKA) { //選花の場合
        for (WorkDiaryDetail workDiaryDetail : wdd) {

          ObjectNode dt    = Json.newObject();
          dt.put("sitsu"      , workDiaryDetail.syukakuSitsu);
          dt.put("size"       , workDiaryDetail.syukakuSize);
          dt.put("hakosu"     , workDiaryDetail.syukakuHakosu);
          dt.put("kosu"       , workDiaryDetail.syukakuKosu);
          dt.put("ninzu"      , workDiaryDetail.syukakuNinzu);
          dt.put("shukakuRyo" , workDiaryDetail.shukakuRyo);
          String cmnSitsu                         = "";
          String cmnSize                          = "";
          cmnSitsu  = Shitu.getShituName(workDiaryDetail.syukakuSitsu);
          cmnSize   = Size.getSizeName(workDiaryDetail.syukakuSize);
          if ("".equals(cmnSitsu)) {
            cmnSitsu = "未選択";
          }
          if ("".equals(cmnSize)) {
            cmnSize = "未選択";
          }
          dt.put("sitsuSpan"    , cmnSitsu);
          dt.put("sizeSpan"     , cmnSize);
          listJson.put(String.valueOf((int)workDiaryDetail.workDiarySequence), dt);
          listJsonArray.add(dt);

        }

        int sequence = wdd.size() + 1;

        for (int seq = sequence ; seq <= 10; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("sitsu"        , 0);
          dt.put("size"         , 0);
          dt.put("hakosu"       , 0);
          dt.put("kosu"         , 0);
          dt.put("ninzu"        , 0);
          dt.put("shukakuRyo"   , 0);
          dt.put("sitsuSpan"    , "未選択");
          dt.put("sizeSpan"     , "未選択");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }
      }
      else {
        for (WorkDiaryDetail workDiaryDetail : wdd) {

          ObjectNode dt    = Json.newObject();
          dt.put("workDetailKind", workDiaryDetail.workDetailKind);
          dt.put("suryo", workDiaryDetail.suryo);
          dt.put("sizaiId", workDiaryDetail.sizaiId);
          dt.put("sizaiSpan", Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)workDiaryDetail.sizaiId, true));
          dt.put("comment", workDiaryDetail.comment);
          listJson.put(String.valueOf((int)workDiaryDetail.workDiarySequence), dt);
          listJsonArray.add(dt);

        }

        int sequence = wdd.size() + 1;

        for (int seq = sequence ; seq <= 5; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("workDetailKind", 0);
          dt.put("suryo", 0);
          dt.put("sizaiId", 0);
          dt.put("sizaiSpan", "未選択");
          dt.put("comment", "");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }

      }

      if(this.apiFlg){
    	  resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonArray);
      }else{
    	  resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
      }

  }
    public void getWorkPlanDetail(double workPlanId) {

      /* 一覧表JSONオブジェクト */
      ObjectNode  listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonArray = mapper.createArrayNode();

      double  farmId      = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));

      if (this.workPlan == null) {
        return;
      }
      WorkPlan wp = WorkPlan.find.where().eq("work_plan_id", workPlanId).findUnique();
      Work wk = Work.getWork(wp.workId);

      List<WorkPlanDetail> wpd = WorkPlanDetail.find.where().eq("work_plan_id", workPlanId).orderBy("work_diary_sequence").findList();

      if (wk.workTemplateId == AgryeelConst.WorkTemplate.SHUKAKU) { //収穫の場合
        for (WorkPlanDetail workPlanDetail : wpd) {

          ObjectNode dt    = Json.newObject();
          dt.put("nisugata"   , workPlanDetail.syukakuNisugata);
          dt.put("sitsu"      , workPlanDetail.syukakuSitsu);
          dt.put("size"       , workPlanDetail.syukakuSize);
          dt.put("kosu"       , workPlanDetail.syukakuKosu);
          dt.put("shukakuRyo" , workPlanDetail.shukakuRyo);
          String cmnNisugata                      = "";
          String cmnSitsu                         = "";
          String cmnSize                          = "";
          cmnNisugata = Nisugata.getNisugataName(workPlanDetail.syukakuNisugata);
          Nisugata n = Nisugata.getNisugataInfo(workPlanDetail.syukakuNisugata);
          if (n != null) {
            dt.put("capa"       , n.capacity);
          }
          else {
            dt.put("capa"         , 0);
          }
          cmnSitsu  = Shitu.getShituName(workPlanDetail.syukakuSitsu);
          cmnSize   = Size.getSizeName(workPlanDetail.syukakuSize);
          if ("".equals(cmnNisugata)) {
            cmnNisugata = "未選択";
          }
          if ("".equals(cmnSitsu)) {
            cmnSitsu = "未選択";
          }
          if ("".equals(cmnSize)) {
            cmnSize = "未選択";
          }
          dt.put("nisugataSpan" , cmnNisugata);
          dt.put("sitsuSpan"    , cmnSitsu);
          dt.put("sizeSpan"     , cmnSize);
          listJson.put(String.valueOf((int)workPlanDetail.workDiarySequence), dt);
          listJsonArray.add(dt);

        }

        int sequence = wpd.size() + 1;

        for (int seq = sequence ; seq <= 10; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("nisugata"     , 0);
          dt.put("sitsu"        , 0);
          dt.put("size"         , 0);
          dt.put("kosu"         , 0);
          dt.put("capa"         , 0);
          dt.put("shukakuRyo"   , 0);
          dt.put("nisugataSpan" , "未選択");
          dt.put("sitsuSpan"    , "未選択");
          dt.put("sizeSpan"     , "未選択");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }
      }
      else if (wk.workTemplateId == AgryeelConst.WorkTemplate.TEISHOKU) { //定植の場合
        int idx = 0;
        for (WorkPlanDetail workPlanDetail : wpd) {
          ObjectNode dt    = Json.newObject();
          if (!workPlanDetail.naeNo.equals("")) {
            NaeStatus ns = NaeStatus.getStatusOfNae(workPlanDetail.naeNo);
            String[] naeNos = workPlanDetail.naeNo.split("-");
            dt.put("naeNo" , workPlanDetail.naeNo);
            dt.put("naeName"  , ns.hinsyuName + "(" + naeNos[1] + ")");
            dt.put("hinsyuId" , ns.hinsyuId);
            dt.put("zaikoKosu", ns.kosu);
          }
          else {
            String[] hinsyus = wp.hinsyuId.split(",");
            double hinsyuId = Double.parseDouble(hinsyus[(int)workPlanDetail.workDiarySequence - 1]);
            dt.put("naeNo" , 0);
            dt.put("naeName"  , Hinsyu.getHinsyuName(hinsyuId));
            dt.put("hinsyuId" , hinsyuId);
            dt.put("zaikoKosu", 0);
          }
          dt.put("kosu"  , workPlanDetail.kosu);
          dt.put("retusu", workPlanDetail.retusu);
          dt.put("joukan", workPlanDetail.joukan);
          dt.put("jousu" , workPlanDetail.jousu);
          dt.put("pDistance" , workPlanDetail.plantingDistance);
          listJson.put(String.valueOf((int)workPlanDetail.workDiarySequence), dt);
          listJsonArray.add(dt);
        }
      }
      else if (wk.workTemplateId == AgryeelConst.WorkTemplate.SENKA) { //選花の場合
        for (WorkPlanDetail workPlanDetail : wpd) {

          ObjectNode dt    = Json.newObject();
          dt.put("sitsu"      , workPlanDetail.syukakuSitsu);
          dt.put("size"       , workPlanDetail.syukakuSize);
          dt.put("hakosu"     , workPlanDetail.syukakuHakosu);
          dt.put("kosu"       , workPlanDetail.syukakuKosu);
          dt.put("ninzu"      , workPlanDetail.syukakuNinzu);
          dt.put("shukakuRyo" , workPlanDetail.shukakuRyo);
          String cmnSitsu                         = "";
          String cmnSize                          = "";
          cmnSitsu  = Shitu.getShituName(workPlanDetail.syukakuSitsu);
          cmnSize   = Size.getSizeName(workPlanDetail.syukakuSize);
          if ("".equals(cmnSitsu)) {
            cmnSitsu = "未選択";
          }
          if ("".equals(cmnSize)) {
            cmnSize = "未選択";
          }
          dt.put("sitsuSpan"    , cmnSitsu);
          dt.put("sizeSpan"     , cmnSize);
          listJson.put(String.valueOf((int)workPlanDetail.workDiarySequence), dt);
          listJsonArray.add(dt);

        }

        int sequence = wpd.size() + 1;

        for (int seq = sequence ; seq <= 10; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("sitsu"        , 0);
          dt.put("size"         , 0);
          dt.put("hakosu"       , 0);
          dt.put("kosu"         , 0);
          dt.put("ninzu"        , 0);
          dt.put("shukakuRyo"   , 0);
          dt.put("sitsuSpan"    , "未選択");
          dt.put("sizeSpan"     , "未選択");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }
      }
      else {
        for (WorkPlanDetail workPlanDetail : wpd) {

          ObjectNode dt    = Json.newObject();
          dt.put("workDetailKind", workPlanDetail.workDetailKind);
          dt.put("suryo", workPlanDetail.suryo);
          dt.put("sizaiId", workPlanDetail.sizaiId);
          dt.put("sizaiSpan", Common.GetCommonValue(Common.ConstClass.ITOSIZAI, (int)workPlanDetail.sizaiId, true));
          dt.put("comment", workPlanDetail.comment);
          listJson.put(String.valueOf((int)workPlanDetail.workDiarySequence), dt);
          listJsonArray.add(dt);

        }

        int sequence = wpd.size() + 1;

        for (int seq = sequence ; seq <= 5; seq++) {

          ObjectNode dt    = Json.newObject();
          dt.put("workDetailKind", 0);
          dt.put("suryo", 0);
          dt.put("sizaiId", 0);
          dt.put("sizaiSpan", "未選択");
          dt.put("comment", "");
          listJson.put(String.valueOf(seq), dt);
          listJsonArray.add(dt);

        }

      }

      if(this.apiFlg){
        resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJsonArray);
      }else{
        resultJson.put(AgryeelConst.WorkDiary.PrevData.WORKHISTRYBASE, listJson);
      }

  }
  public static CommonWorkDiaryWork getCommonWorkDiaryWork(int pWorkTemplateId, Session session, ObjectNode resultJson) {
    CommonWorkDiaryWork cwdk = null;        //作業記録作業別項目

    /*----- 作業別項目 -----*/
    //AICA 作業テンプレート毎にコンポーネント切り分ける様に変更
    switch (pWorkTemplateId) {
    case AgryeelConst.WorkTemplate.NOMAL:
      cwdk = new NomalCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.SANPU:
      cwdk = new HiryoSanpuCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.HASHU:
      cwdk = new HashuCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.SHUKAKU:
      cwdk = new ShukakuCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.NOUKO:
      cwdk = new NoukouCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.KANSUI:
      cwdk = new KansuiCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.KAISHU:
      cwdk = new KaishuCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.DACHAKU:
      cwdk = new DachakuCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.COMMENT:
      cwdk = new CommentCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.MALTI:
      cwdk = new MultiCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.TEISHOKU:
      cwdk = new TeishokuCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.NAEHASHU:
      cwdk = new NaehashuCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.SENTEI:
      cwdk = new SenteiCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.MABIKI:
      cwdk = new MabikiCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.NICHOCHOSEI:
      cwdk = new NichoChoseiCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.SENKA:
      cwdk = new SenkaCompornent(session, resultJson);
      break;
    case AgryeelConst.WorkTemplate.HAIKI:
      cwdk = new HaikiCompornent(session, resultJson);
      break;
    }
    return cwdk;
  }
}
