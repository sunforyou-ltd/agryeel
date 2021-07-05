package compornent;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import param.NouhiCheckParm;
import play.libs.Json;
import util.DateU;

import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Nouhi;
import models.NouhiOfCrop;
import models.Sequence;
import models.WorkChainItem;
import models.WorkDiarySanpu;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYEEL】農肥コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NouhiComprtnent implements AgryellInterface{

  /**
   * 農肥チェック結果
   * @author kimura
   *
   */
  public class CheckCode {
    public static final int ERROR         = 0xFF;
    public static final int NOMAL         = 0x00;
    public static final int COUNT         = 0x01;
    public static final int WHEN          = 0x02;
    public static final int DOUBLE        = 0x03;
  }

  List<Nouhi> nouhis;

	/**
	 * コンストラクタ
	 */
	public NouhiComprtnent() {

		nouhis = Nouhi.find.orderBy("nouhi_id").findList();

	}

  /**
   * 農肥情報を生成する
   * @param pName
   * @return
   */
  public static Nouhi makeNouhi(JsonNode pInput) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.NOUHIID);       //最新シーケンス値の取得

    Nouhi nouhi = new Nouhi();
    nouhi.nouhiId    = sequence.sequenceValue;
    nouhi.nouhiName  	= pInput.get("nouhiName").asText();
    nouhi.nouhiKind    	= Integer.parseInt(pInput.get("nouhiKind").asText());
    nouhi.bairitu	   	= Double.parseDouble(pInput.get("bairitu").asText());
    nouhi.sanpuryo		= Double.parseDouble(pInput.get("sanpuryo").asText());
    nouhi.farmId		= Double.parseDouble(pInput.get("farmId").asText());
    nouhi.unitKind    	= Integer.parseInt(pInput.get("unitKind").asText());
    nouhi.n				= Double.parseDouble(pInput.get("n").asText());
    nouhi.p				= Double.parseDouble(pInput.get("p").asText());
    nouhi.k				= Double.parseDouble(pInput.get("k").asText());
    nouhi.mg      = Double.parseDouble(pInput.get("mg").asText());
    nouhi.lower	    	= Integer.parseInt(pInput.get("lower").asText());
    nouhi.upper	    	= Integer.parseInt(pInput.get("upper").asText());
    nouhi.finalDay	    = Integer.parseInt(pInput.get("finalDay").asText());
    nouhi.sanpuCount    = Double.parseDouble(pInput.get("sanpuCount").asText());
    nouhi.useWhen	    = Double.parseDouble(pInput.get("useWhen").asText());
    nouhi.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
    nouhi.nouhiOfficialName = pInput.get("nouhiOfficialName").asText();
    nouhi.registNumber  = Long.parseLong(pInput.get("registNumber").asText());
    nouhi.save();

    return nouhi;

  }

  /**
   * 農肥情報を更新する
   * @param pBeltoId
   * @param pName
   * @return
   */
  public static Nouhi updateNouhi(JsonNode pInput) {

	  Nouhi nouhi = Nouhi.getNouhiInfo(Double.parseDouble(pInput.get("nouhiId").asText()));
      nouhi.nouhiName  	= pInput.get("nouhiName").asText();
      nouhi.nouhiKind   = Integer.parseInt(pInput.get("nouhiKind").asText());
      nouhi.bairitu	   	= Double.parseDouble(pInput.get("bairitu").asText());
      nouhi.sanpuryo	= Double.parseDouble(pInput.get("sanpuryo").asText());
      nouhi.farmId		= Double.parseDouble(pInput.get("farmId").asText());
      nouhi.unitKind    = Integer.parseInt(pInput.get("unitKind").asText());
      nouhi.n			= Double.parseDouble(pInput.get("n").asText());
      nouhi.p			= Double.parseDouble(pInput.get("p").asText());
      nouhi.k			= Double.parseDouble(pInput.get("k").asText());
      nouhi.mg    = Double.parseDouble(pInput.get("mg").asText());
      nouhi.lower	    = Integer.parseInt(pInput.get("lower").asText());
      nouhi.upper	    = Integer.parseInt(pInput.get("upper").asText());
      nouhi.finalDay	= Integer.parseInt(pInput.get("finalDay").asText());
      nouhi.sanpuCount  = Double.parseDouble(pInput.get("sanpuCount").asText());
      nouhi.useWhen	    = Double.parseDouble(pInput.get("useWhen").asText());
      nouhi.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
      nouhi.nouhiOfficialName = pInput.get("nouhiOfficialName").asText();
      nouhi.registNumber = Long.parseLong(pInput.get("registNumber").asText());
      nouhi.update();

    return nouhi;

  }

  /**
  * 農肥を削除します
  * @param pNouhiId
  * @return
  */
 public static int deleteNouhi(double pNouhiId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- 農肥を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM nouhi WHERE nouhi_id = " + pNouhiId).execute();
   Nouhi nouhi = Nouhi.getNouhiInfo(pNouhiId);
   nouhi.deleteFlag = 1;
   nouhi.update();

   return result;

 }
 public static int getNouhiOfWorkJson(double pWorkId, double pKukakuId, ObjectNode pListJson) {

   //----- 区画ワークチェーン状況を取得 -----
   CompartmentWorkChainStatus cwcs = WorkChainCompornent.getCompartmentWorkChainStatus(pKukakuId);
   if (cwcs != null) {
     WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(cwcs.workChainId, pWorkId);
     if (wci != null) {
       //----- 生産者別機器情報を取得 -----
       List<NouhiOfCrop> nocs = NouhiOfCrop.find.where().eq("work_chain_id", wci.workChainId).eq("crop_id", cwcs.cropId).findList();
       List<Double>nouhis = new ArrayList<Double>();
       for (NouhiOfCrop noc : nocs) {
         nouhis.add(noc.nouhiId);
       }
       //----- 対象機器の取得 -----
       List<Nouhi> nouhiList = Nouhi.find.where().in("nouhi_id", nouhis).eq("nouhi_kind", wci.nouhiKind).order("use_count desc, nouhi_id asc").findList();
       for (Nouhi nouhi : nouhiList) {

         ObjectNode jd = Json.newObject();

         jd.put("id"   , nouhi.nouhiId);
         jd.put("name" , nouhi.nouhiName);

         pListJson.put(String.valueOf(nouhi.nouhiId), jd);

       }
     }
   }

   return GET_SUCCESS;

 }

 public static int getNouhiOfFarmJson(double pWorkId, double farmId, double pKukakuId, ObjectNode pListJson) {

   //----- 区画ワークチェーン状況を取得 -----
   CompartmentWorkChainStatus cwcs = WorkChainCompornent.getCompartmentWorkChainStatus(pKukakuId);
   if (cwcs != null) {
     WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(cwcs.workChainId, pWorkId);
     if (wci != null) {
       List<Nouhi> nouhiList = Nouhi.find.where().in("farm_id", farmId).eq("nouhi_kind", wci.nouhiKind).order("use_count desc, nouhi_id asc").findList();
       for (Nouhi nouhi : nouhiList) {
         if (nouhi.deleteFlag == 1) { // 削除済みの場合
           continue;
         }

         ObjectNode jd = Json.newObject();

         jd.put("id"   , nouhi.nouhiId);
         jd.put("name" , nouhi.nouhiName);

         pListJson.put(String.valueOf(nouhi.nouhiId), jd);

       }
     }
   }

   return GET_SUCCESS;

 }

   public static int updateUseCount(double pFarmId) {
     int result = 0;

     List<Nouhi> nouhis = Nouhi.find.where().eq("farm_id", pFarmId).orderBy("nouhi_id").findList();
     for (Nouhi nouhi : nouhis) {
       List<WorkDiarySanpu> wdss = WorkDiarySanpu.find.where().eq("nouhi_id", nouhi.nouhiId).findList();
       nouhi.useCount = wdss.size();
       nouhi.update();
       result++;
     }

     return result;

   }
 public static int nouhiCheck(NouhiCheckParm p) {
   int result = CheckCode.NOMAL;
   Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
   DecimalFormat df = new DecimalFormat("##0");

   //----- 各種パラメータチェック -----
   if ((p.nouhiId == 0) || (p.kukakuId == 0)) {
     result       = CheckCode.ERROR;
     p.checkcode  = result;
   }
   else {
     //----- 農肥情報の取得 -----
     Nouhi n = Nouhi.getNouhiInfo(p.nouhiId);
     if (n == null) {
       result       = CheckCode.ERROR;
       p.checkcode  = result;
     }
     else {
       //----- 区画状況情報の取得 -----
       CompartmentStatus cs = FieldComprtnent.getCompartmentStatus(p.kukakuId);
       if (cs == null) {
         result       = CheckCode.ERROR;
         p.checkcode  = result;
       }
       else {
         //----- 散布回数のチェック -----
         int sanpuCount = 0;
         if (n.sanpuCount > 0) {
           List<models.WorkDiary> wdl = models.WorkDiary.find.where().eq("kukaku_id", p.kukakuId).between("work_date", cs.katadukeDate, cs.finalEndDate).findList();
           for (models.WorkDiary wd : wdl) {
             models.WorkDiarySanpu wds = models.WorkDiarySanpu.find.where().eq("work_diary_id", wd.workDiaryId).eq("nouhi_id", p.nouhiId).findUnique();
             if (wds != null) {
               sanpuCount++;
             }
           }
           sanpuCount++;//今回散布分を加算
           if (n.sanpuCount < sanpuCount) {// 散布回数上限値オーバー
             result       |= CheckCode.COUNT;
             p.checkcode  = result;
             p.message = "【" + n.nouhiName + "】散布回数を超えています。(" + df.format(sanpuCount) + " / " + df.format(n.sanpuCount) + ")";
           }
         }
         //散布期間のチェック
         if (n.useWhen > 0) {
           //----- 収穫開始日予測が実施されているか？ -----
           if ((cs.predictionShukakuStartDate != null) && (cs.predictionShukakuStartDate.compareTo(nullDate) != 0))  {
             long diff = DateU.GetDiffDate(p.workDate, new java.util.Date(cs.predictionShukakuStartDate.getTime()));
             if (diff < n.useWhen) { //散布可能期間を過ぎている場合
               result       |= CheckCode.WHEN;
               p.checkcode  = result;
               Calendar cal = Calendar.getInstance();
               cal.setTimeInMillis(cs.predictionShukakuStartDate.getTime());
               cal.add(Calendar.DAY_OF_MONTH, (int)(-1 * n.useWhen));
               SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
               p.message = "【" + n.nouhiName + "】散布可能期間を超えています。(" + sdf.format(cal.getTime()) + ")";
             }
           }
         }
       }
     }
   }



   return result;
 }


}
