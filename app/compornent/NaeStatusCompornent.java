package compornent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Hinsyu;
import models.IkubyoDiary;
import models.IkubyoDiarySanpu;
import models.MotochoBase;
import models.NaeStatus;
import models.Work;
import models.WorkChainItem;
import models.Youki;

import org.apache.commons.lang3.time.DateUtils;

import play.libs.Json;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

public class NaeStatusCompornent implements AgryellInterface {

    String naeNo = "";		//苗No
    double workId = 0;      //作業ID
    List<WorkChainItem> wcis;
    List<Work> ws;
    public List<IkubyoDiarySanpu> idsps;
    public java.sql.Date wdDate;

    public NaeStatusCompornent() {
    }
    /**
     * コンストラクタ
     * @param kukakuId
     */
    public NaeStatusCompornent(String naeNo) {

        this.naeNo = naeNo;

    }
    public NaeStatusCompornent(String naeNo, double workId) {

      this.naeNo = naeNo;
      this.workId = workId;

    }
    public void getAllData(double farmId) {
      this.wcis  = WorkChainItem.find.where().orderBy("work_chain_id, sequence_id").findList();
      this.ws    = Work.find.where().orderBy("work_id").findList();
    }
    private List<WorkChainItem> getWorkChainItem(double workChainId) {
      List<WorkChainItem> result = new ArrayList<WorkChainItem>();
      for (WorkChainItem data : wcis) {
        if (data.workChainId == workChainId) {
          result.add(data);
        }
      }
      return result;
    }
    private Work getWork(double workId) {
      Work result = null;
      for (Work data : ws) {
        if (data.workId == workId) {
          result = data;
          break;
        }
      }
      return result;
    }

    public void update(MotochoBase motochoBase) {
      /*------------------------------------------------------------------------------------------------------------*/
      /* 既存の苗状況照会を削除                                                                                   */
      /*------------------------------------------------------------------------------------------------------------*/
      NaeStatus oldNaeStatus = NaeStatus.find.where().eq("nae_no", this.naeNo).findUnique();
      deleteNaeStatusCompornent();

      if (motochoBase.cropId == 0) {
        // 苗播種作業が削除されると、空の苗状況データができる為
        return;
      }

      /*------------------------------------------------------------------------------------------------------------*/
      /* 苗状況照会を作成                                                                                         */
      /*------------------------------------------------------------------------------------------------------------*/
      /*----- 初期化 -----*/
      NaeStatus naeStatus    = new NaeStatus();
      java.sql.Date nullDate = DateU.GetNullDate();
      String[] sNaeNos = this.naeNo.split("-");
      double farmId = Double.parseDouble(sNaeNos[0]);

      naeStatus.finalDisinfectionDate   = nullDate;		//最終消毒日を初期化する
      naeStatus.totalDisinfectionCount  = 0;            //合計消毒量を初期化する
      naeStatus.totalDisinfectionNumber = 0;            //合計消毒回数を初期化する
      naeStatus.finalKansuiDate         = nullDate;		//最終潅水日を初期化する
      naeStatus.totalKansuiCount        = 0;			//合計潅水量を初期化する
      naeStatus.totalKansuiNumber       = 0;            //合計潅水回数を初期化する
      naeStatus.finalTuihiDate          = nullDate;		//最終追肥日を初期化する
      naeStatus.totalTuihiCount         = 0;			//合計追肥量を初期化する
      naeStatus.totalTuihiNumber        = 0;            //合計追肥回数を初期化する
      naeStatus.totalSolarRadiation     = 0;            //合計積算日射量を初期化する

    	/*----- 基本情報 -----*/
      naeStatus.naeNo                       = this.naeNo;							//苗No
      naeStatus.farmId                      = farmId;								//生産者ID
      naeStatus.hinsyuId                    = motochoBase.hinsyuId;					//品種ID
      naeStatus.hinsyuName                  = motochoBase.hinsyuName;				//品種名
      naeStatus.cropId                      = motochoBase.cropId;
      naeStatus.hashuDate                   = motochoBase.hashuDate;				//播種日
      naeStatus.seiikuDayCount              = motochoBase.seiikuDayCount;			//生育日数
      naeStatus.zaikoSuryo                  = motochoBase.nowPredictionShukaku;
      naeStatus.kosu                        = motochoBase.predictionShukakuRyo;
      naeStatus.katadukeDate                = motochoBase.workStartDay;
      naeStatus.finalDisinfectionDate       = motochoBase.finalDisinfectionDate;
      naeStatus.totalDisinfectionCount      = motochoBase.totalDisinfectionCount;
      naeStatus.totalDisinfectionNumber     = motochoBase.totalDisinfectionNumber;
      naeStatus.finalTuihiDate              = motochoBase.finalTuihiDate;
      naeStatus.totalTuihiCount             = motochoBase.totalTuihiCount;
      naeStatus.totalTuihiNumber            = motochoBase.totalTuihiNumber;
      naeStatus.finalKansuiDate             = motochoBase.finalKansuiDate;
      naeStatus.totalKansuiCount            = motochoBase.totalKansuiCount;
      naeStatus.totalKansuiNumber           = motochoBase.totalKansuiNumber;
      naeStatus.finalEndDate                = motochoBase.workEndDay;
      naeStatus.totalSolarRadiation         = motochoBase.totalSolarRadiation;

      naeStatus.save();

    }

    private void deleteNaeStatusCompornent() {

        Ebean.createSqlUpdate("DELETE FROM nae_status WHERE nae_no = :nae_no").setParameter("nae_no", this.naeNo).execute();

    }

    /**
     * 更新処理
     */
    synchronized public void update() {

        /* 苗情報の取得 */
        NaeStatus naeStatus = NaeStatus.find.where("nae_no = " + this.naeNo).findUnique();

        if (naeStatus == null) { return; } 			//苗状況情報が存在しない場合、処理をしない

        /* 育苗記録情報の取得 */
        List<IkubyoDiary> 		ikubyoDiaryList	= IkubyoDiary.find.where("nae_no = " + this.naeNo + " AND nae_status_update = " + AgryeelConst.UpdateFlag.NONE).orderBy("work_date").findList();
        List<IkubyoDiarySanpu> 	ikubyoDiarySanpuList;
        double					ikubyoSyoudoku			= 0;
        double					ikubyoTuihi				= 0;
        java.sql.Date nullDate     = DateU.GetNullDate();

        for (IkubyoDiary ikubyoDiaryData : ikubyoDiaryList) {

          Work work = Work.getWork(ikubyoDiaryData.workId);

          WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(AgryeelConst.IkubyoInfo.WORKCHAINID, work.workId);

          switch((int)work.workTemplateId) {
          case AgryeelConst.WorkTemplate.NAEHASHUIK:
            naeStatus.hinsyuName = Hinsyu.getMultiHinsyuName(ikubyoDiaryData.hinsyuId);
            naeStatus.hinsyuId   = ikubyoDiaryData.hinsyuId;
            naeStatus.cropId     = Hinsyu.getMultiHinsyuCropId(ikubyoDiaryData.hinsyuId);;
            naeStatus.zaikoSuryo = ikubyoDiaryData.naeSuryo;
            naeStatus.kosu       = ikubyoDiaryData.kosu;

            naeStatus.hashuDate  = ikubyoDiaryData.workDate;                    //播種日を更新する
            //システム日付との自動算出を可能にする為、0のままとする
            naeStatus.seiikuDayCount          = 0;                      //生育日数を更新する

            naeStatus.finalDisinfectionDate   = nullDate;               //最終消毒日を初期化する
            naeStatus.totalDisinfectionCount  = 0;                      //合計消毒量を初期化する
            naeStatus.finalKansuiDate         = nullDate;               //最終潅水日を初期化する
            naeStatus.totalKansuiCount        = 0;                      //合計潅水量を初期化する
            naeStatus.finalTuihiDate          = nullDate;               //最終追肥日を初期化する
            naeStatus.totalTuihiCount         = 0;                      //合計追肥量を初期化する
            break;
          case AgryeelConst.WorkTemplate.SANPU:
            if ((naeStatus.hashuDate == null)
                || (naeStatus.hashuDate.compareTo(nullDate) == 0)) {    //播種日が未設定の場合
              //集計情報に含まない
            }
            else {
              switch(wci.nouhiKind) {
              case AgryeelConst.NouhiKind.NOUYAKU:
                naeStatus.finalDisinfectionDate = ikubyoDiaryData.workDate; //最終消毒日を更新する

                ikubyoDiarySanpuList = IkubyoDiarySanpu.find.where("ikubyo_diary_id = " + ikubyoDiaryData.ikubyoDiaryId).findList();
                ikubyoSyoudoku    = 0;

                for (IkubyoDiarySanpu IkubyoDiarySanpuData : ikubyoDiarySanpuList) {
                	ikubyoSyoudoku  += IkubyoDiarySanpuData.sanpuryo;
                }

                naeStatus.totalDisinfectionCount++;
                naeStatus.totalDisinfectionNumber = (long)ikubyoSyoudoku;
                break;
              case AgryeelConst.NouhiKind.HIRYO:
                naeStatus.finalTuihiDate  = ikubyoDiaryData.workDate; //最終追肥日を更新する

                ikubyoDiarySanpuList  = IkubyoDiarySanpu.find.where("ikubyo_diary_id = " + ikubyoDiaryData.ikubyoDiaryId).findList();
                ikubyoTuihi     = 0;

                for (IkubyoDiarySanpu IkubyoDiarySanpuData : ikubyoDiarySanpuList) {
                	ikubyoTuihi   += IkubyoDiarySanpuData.sanpuryo;
                }

                naeStatus.totalTuihiCount++;
                naeStatus.totalTuihiNumber = (long)ikubyoTuihi;
                break;
              }
            }
            break;
          case AgryeelConst.WorkTemplate.KANSUI:
            naeStatus.finalKansuiDate   = ikubyoDiaryData.workDate;        //最終潅水日を更新する
            naeStatus.totalKansuiNumber = (long)ikubyoDiaryData.kansuiRyo; //潅水量を更新する
            naeStatus.totalKansuiCount++;                                  //潅水量を更新する
            break;
          case AgryeelConst.WorkTemplate.HAIKIIK:
            naeStatus.zaikoSuryo -= ikubyoDiaryData.naeSuryo;
            naeStatus.kosu       -= ikubyoDiaryData.haikiRyo;
            break;

          }

          ikubyoDiaryData.naeStatusUpdate = AgryeelConst.UpdateFlag.UPDATE;
          ikubyoDiaryData.update();

        }

        naeStatus.update();

    }

    /**
     * 生産者IDから苗情報を取得する
     * @param pFarmId
     * @param pListJson
     * @return
     */
    public static int getNaeOfFarmJson(double pFarmId, ObjectNode pListJson) {

      int result  = GET_SUCCESS;
      long seiikuDayCount = 0;

      List<NaeStatus> naes = NaeStatus.getStatusOfFarm(pFarmId);

      if (naes.size() > 0) { //該当データが存在する場合
        long idx = 0;
        for (NaeStatus nae : naes) {
          if (nae.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          if (nae.kosu == 0) { // 数量０の場合
            continue;
          }

          Date hashuDate = DateUtils.truncate(nae.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
          seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);

          ObjectNode jd = Json.newObject();
          String[] sNaeNos = nae.naeNo.split("-");
          DecimalFormat df = new DecimalFormat("#,###");

          jd.put("id"   , nae.naeNo);
          jd.put("name" , nae.hinsyuName + "(" + sNaeNos[1] + ")");
          jd.put("sub"  , df.format(nae.kosu) + "個&nbsp;&nbsp;&nbsp;&nbsp;" + seiikuDayCount + "日");
          jd.put("flag" , 0);

          idx++;
          pListJson.put(String.valueOf(idx), jd);
        }
      }
      else {
        result  = GET_ERROR;
      }
      return result;
    }

    /**
     * 生産者IDから苗情報を取得する
     * @param pFarmId
     * @param pListJson
     * @return
     */
    public static int getNaeOfFarmJsonArray(double pFarmId, ArrayNode pListJson) {

      int result  = GET_SUCCESS;
      long seiikuDayCount = 0;

      List<NaeStatus> naes = NaeStatus.getStatusOfFarm(pFarmId);

      if (naes.size() > 0) { //該当データが存在する場合
        long idx = 0;
        for (NaeStatus nae : naes) {
          if (nae.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          if (nae.kosu == 0) { // 数量０の場合
            continue;
          }

          Date hashuDate = DateUtils.truncate(nae.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
          seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);

          ObjectNode jd = Json.newObject();
          String[] sNaeNos = nae.naeNo.split("-");
          DecimalFormat df = new DecimalFormat("#,###");

          jd.put("id"   , nae.naeNo);
          jd.put("name" , nae.hinsyuName + "(" + sNaeNos[1] + ")");
          jd.put("sub"  , df.format(nae.kosu) + "個&nbsp;&nbsp;&nbsp;&nbsp;" + seiikuDayCount + "日");
          jd.put("flag" , 0);

          idx++;
          pListJson.add(jd);
        }
      }
      else {
        result  = GET_ERROR;
      }
      return result;
    }

    /**
     * 対象苗の作業状況をJSONに格納します
     * @param pNaeNo
     * @param pListJson
     * @return
     */
    public int getWorkStatusJson(String pNaeNo, ObjectNode pListJson) {

      int result = GET_SUCCESS;

      List<IkubyoDiary> ids = IkubyoDiary.find.where().eq("nae_no", pNaeNo).orderBy("work_date").findList();
      if (ids.size() > 0) {
        List<Double> aryWork = new ArrayList<Double>();
        for (IkubyoDiary id : ids) {
          aryWork.add(id.workId);
        }
        List<WorkChainItem> wcis = getWorkChainItem(AgryeelConst.IkubyoInfo.WORKCHAINID);
        for (WorkChainItem wci : wcis) {
          Work wk = getWork(wci.workId);

          ObjectNode jd = Json.newObject();

          jd.put("id"   , wk.workId);
          jd.put("name" , wk.workName);
          jd.put("color" , wk.workColor);

          int end = 0;

          for (Double id : aryWork) {
            if (wk.workId == id.doubleValue()) {
              end = 1;
              break;
            }
          }
          jd.put("end" , end);
          pListJson.put(String.valueOf(wk.workId), jd);
        }
      }

      return result;
    }

    /**
     * 対象苗の作業状況をJSONに格納します
     * @param pNaeNo
     * @param pListJson
     * @return
     */
    public int getWorkStatusJsonArray(String pNaeNo, ArrayNode pListJson) {

      int result = GET_SUCCESS;

      List<IkubyoDiary> ids = IkubyoDiary.find.where().eq("nae_no", pNaeNo).orderBy("work_date").findList();
      if (ids.size() > 0) {
        List<Double> aryWork = new ArrayList<Double>();
        for (IkubyoDiary id : ids) {
          aryWork.add(id.workId);
        }
        List<WorkChainItem> wcis = getWorkChainItem(AgryeelConst.IkubyoInfo.WORKCHAINID);
        for (WorkChainItem wci : wcis) {
          Work wk = getWork(wci.workId);

          ObjectNode jd = Json.newObject();

          jd.put("id"   , wk.workId);
          jd.put("name" , wk.workName);
          jd.put("color" , wk.workColor);

          int end = 0;

          for (Double id : aryWork) {
            if (wk.workId == id.doubleValue()) {
              end = 1;
              break;
            }
          }
          jd.put("end" , end);
          pListJson.add(jd);
        }
      }

      return result;
    }

    /**
     * 対象苗の容器単位を返却します
     * @param pNaeNo
     * @return unitKind
     */
    public int getYoukiUnit(String pNaeNo) {
      int unitKind = 1;

      List<IkubyoDiary> ids = IkubyoDiary.find.where().eq("nae_no", pNaeNo).orderBy("work_date").findList();
      if (ids.size() > 0) {
        for (IkubyoDiary id : ids) {
          if (id.youkiId == 0) {
            continue;
          }
          unitKind = Youki.getUnitKind(id.youkiId);
          break;
        }
      }
      return unitKind;
    }

    /**
     * 対象苗の情報をJSONに格納します
     * @param pNaeNo
     * @param pListJson
     * @return
     */
    public static int getNaeInfoJson(String pNaeNo, ObjectNode pListJson) {

      int result = GET_SUCCESS;
      String[] naeNos = pNaeNo.split(",");

      if (pNaeNo.indexOf("-") != -1) {
        for (String naeNo : naeNos) {
            NaeStatus naeStatus = NaeStatus.find.where().eq("nae_no", naeNo).findUnique();
            ObjectNode jd = Json.newObject();
       
            String[] nae = naeStatus.naeNo.split("-");
            jd.put("id"   , naeStatus.naeNo);
            jd.put("name" , naeStatus.hinsyuName + "(" + nae[1] + ")");
            jd.put("hinsyuId" , naeStatus.hinsyuId);
            jd.put("kosu" , naeStatus.kosu);
            pListJson.put(String.valueOf(naeStatus.naeNo), jd);
        }
      } else {
        for (String naeNo : naeNos) {
            ObjectNode jd = Json.newObject();
            double hinsyuId = Double.parseDouble(naeNo);

            jd.put("id"       , "");
            jd.put("name"     , Hinsyu.getHinsyuName(hinsyuId));
            jd.put("hinsyuId" , hinsyuId);
            jd.put("kosu"     , 0);
            pListJson.put(String.valueOf(hinsyuId), jd);
        }
      }

      return result;
    }

    /**
     * 対象苗の情報をJSONに格納します
     * @param pNaeNo
     * @param pListJson
     * @return
     */
    public static int getNaeInfoJsonArray(String pNaeNo, ArrayNode pListJson) {

      int result = GET_SUCCESS;
      String[] naeNos = pNaeNo.split(",");

      if (pNaeNo.indexOf("-") != -1) {
        for (String naeNo : naeNos) {
            NaeStatus naeStatus = NaeStatus.find.where().eq("nae_no", naeNo).findUnique();
            ObjectNode jd = Json.newObject();
      
            String[] nae = naeStatus.naeNo.split("-");
            jd.put("id"   , naeStatus.naeNo);
            jd.put("hinsyuId" , naeStatus.hinsyuId);
            jd.put("name" , naeStatus.hinsyuName + "(" + nae[1] + ")");
            jd.put("kosu" , naeStatus.kosu);
            pListJson.add(jd);
        }
      } else {
        for (String naeNo : naeNos) {
            ObjectNode jd = Json.newObject();
            double hinsyuId = Double.parseDouble(naeNo);

            jd.put("id"       , "");
            jd.put("hinsyuId" , hinsyuId);
            jd.put("name"     , Hinsyu.getHinsyuName(hinsyuId));
            jd.put("kosu"     , 0);
            pListJson.add(jd);
        }
      }

      return result;
    }

}
