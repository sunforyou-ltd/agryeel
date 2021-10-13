package controllers;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Account;
import models.Common;
import models.Crop;
import models.Farm;
import models.FarmStatus;
import models.Hinsyu;
import models.IkubyoDiary;
import models.IkubyoDiarySanpu;
import models.IkubyoLine;
import models.IkubyoPlan;
import models.IkubyoPlanLine;
import models.NaeStatus;
import models.Nouhi;
import models.Weather;
import models.Work;
import models.WorkChainItem;
import models.Youki;

import org.apache.commons.lang3.time.DateUtils;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.DateU;
import util.ListrU;
import util.StringU;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import compornent.CropComprtnent;
import compornent.NaeStatusCompornent;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;

import consts.AgryeelConst;

public class IkubyoController extends Controller {

    /**
     * 【AICA】育苗管理画面への遷移
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result move() {
        return ok(views.html.ikubyoMain.render(""));
    }

    /**
     * 【AICA】育苗作業情報を取得する
     * @return 育苗作業情報JSON
     */
    public static Result getIkubyoWork() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJsonApi  = mapper.createArrayNode();
        boolean api            = false;

        if (session(AgryeelConst.SessionKey.API) != null) {
          api = true;
        }

        /*----- 作業情報 -----*/
        listJson   = Json.newObject();	//リスト形式JSONを初期化する

        List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList(AgryeelConst.IkubyoInfo.WORKCHAINID);
        if (wcis.size() > 0) { //該当データが存在する場合
          for (WorkChainItem wc : wcis) {
            if (wc.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            Work work = Work.find.where().eq("work_id", wc.workId).findUnique();

            if (work != null) {
              if (work.deleteFlag == 1) { // 削除済みの場合
                continue;
              }

              ObjectNode jd = Json.newObject();

              jd.put("workId"         , work.workId);       //作業ID
              jd.put("workName"       , work.workName);     //作業名
              jd.put("workMode"       , wc.workMode);       //作業モード
              jd.put("workEnglish"    , work.workEnglish);  //作業英字名
              jd.put("workColor"      , work.workColor);    //作業カラー
              jd.put("workTemplateId" , work.workTemplateId);//テンプレートID
              listJson.put(String.valueOf(work.workId), jd);
              listJsonApi.add(jd);
            }
          }
        }

        if(api) {
          resultJson.put(AgryeelConst.IkubyoInfo.TARGETIKUBYOWORK, listJsonApi);
        } else {
          resultJson.put(AgryeelConst.IkubyoInfo.TARGETIKUBYOWORK, listJson);
        }
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AICA】苗状況照会を取得する
     * @return 苗状況照会JSON
     */
    public static Result getNaeSP() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();
      ObjectNode  listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode   listJsonApi = mapper.createArrayNode();
      boolean     target     = false;
      boolean     api        = false;
      double      count = 0;
      long        oldCount = 0;
      DecimalFormat dfSeq = new DecimalFormat("000");
      Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
      Calendar syscal = Calendar.getInstance();

      if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.REDIRECT);
      }
      if (session(AgryeelConst.SessionKey.API) != null) {
        api = true;
      }

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      NaeStatusCompornent nsc = new NaeStatusCompornent();
      nsc.getAllData(accountComprtnent.accountData.farmId);

      //----- 検索条件の整備 -----
      //----- 生産物選択 -----
      List<Double> aryCrop = new ArrayList<Double>();
      if (!StringU.nullcheck(accountComprtnent.accountIkubyoStatusData.ssnCrop)) { //指定あり
        String[] sCrops = accountComprtnent.accountIkubyoStatusData.ssnCrop.split(",");
        for (String sCrop : sCrops) {
          aryCrop.add(Double.parseDouble(sCrop));
        }
      }
      //----- 品種選択 -----
      List<Double> aryHinsyu = new ArrayList<Double>();
      if (!StringU.nullcheck(accountComprtnent.accountIkubyoStatusData.ssnHinsyu)) { //指定あり
        String[] sHinsyus = accountComprtnent.accountIkubyoStatusData.ssnHinsyu.split(",");
        for (String sHinsyu : sHinsyus) {
          aryHinsyu.add(Double.parseDouble(sHinsyu));
        }
      }

      //苗状況照会情報を取得する
      List<NaeStatus> naeStatuss = NaeStatus.find.where().eq("farm_id", accountComprtnent.accountData.farmId).order("crop_id,hinsyu_id,nae_no").findList();
      for (NaeStatus naeData : naeStatuss) {
        if (naeData.deleteFlag == 1) { // 削除済みの場合
          continue;
        }
        oldCount++;

        //----- ここで検索条件との一致チェックを行う -----
        //----- 生産物選択 -----
        if (!ListrU.keyCheck(aryCrop, naeData.cropId)) {      //該当条件に含まれない場合
          continue;
        }
        //----- 品種選択 -----
        if (!ListrU.keyChecks(aryHinsyu, naeData.hinsyuId)) { //該当条件に含まれない場合
          continue;
        }
        ObjectNode naeStatusJson  = Json.newObject();
        String[] sNaeNos = naeData.naeNo.split("-");
        naeStatusJson.put("naeNo"            , naeData.naeNo);                    //苗No
        naeStatusJson.put("naeName"          , sNaeNos[1]);                       //苗名
        naeStatusJson.put("zaikoSuryo"       , naeData.zaikoSuryo);               //在庫数量
        naeStatusJson.put("kosu"             , naeData.kosu);                     //個数
        naeStatusJson.put("unit"             , nsc.getYoukiUnit(naeData.naeNo));  //単位

        long seiikuDayCount     = 0;
        long seiikuDayCountEnd  = 0;
        if (naeData.hashuDate != null && (naeData.hashuDate.compareTo(nullDate) != 0)) {  //播種日

          Date hashuDate = DateUtils.truncate(naeData.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

          naeStatusJson.put("hashuDate"       , naeData.hashuDate.toString());

          seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
        }
        else {
        	naeStatusJson.put("hashuDate"       , "");
          seiikuDayCount = 0;
        }
        //----- ここで生育日数との一致チェックを行う -----
        long nisuf = 0;
        long nisut = 9999;
        if (accountComprtnent.accountIkubyoStatusData.ssnSeiikuF != 0) { //指定あり
          nisuf = accountComprtnent.accountIkubyoStatusData.ssnSeiikuF;
        }
        if (accountComprtnent.accountIkubyoStatusData.ssnSeiikuT != 0) { //指定あり
          nisut = accountComprtnent.accountIkubyoStatusData.ssnSeiikuT;
        }

        if (!(nisuf <= seiikuDayCount && seiikuDayCount <= nisut)) { //指定範囲以外の場合
          continue;
        }

        naeStatusJson.put("seiikuDayCount"    , seiikuDayCount);                  //生育日数

        Crop cp = CropComprtnent.getCropById(naeData.cropId);
        if (cp != null) {                                                         //生産物名称
          naeStatusJson.put("cropName"    , cp.cropName);
        }
        else {
          naeStatusJson.put("cropName"    , "");
        }

        if (naeData.hinsyuName != null && !"".equals(naeData.hinsyuName)) {
            naeStatusJson.put("hinsyuName"        , naeData.hinsyuName);          //品種名
        }
        else {
            naeStatusJson.put("hinsyuName"        , "");                          //品種名
        }

        DecimalFormat df = new DecimalFormat("#,##0.0");
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        SimpleDateFormat sdfL = new SimpleDateFormat("yyyy/MM/dd");
        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
        //苗の郵便番号を設定する箇所がない為、検討要
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 積算温度
        //----------------------------------------------------------------------------------------------------------------------------------
        //naeStatusJson.put("totalSolarRadiation"    , df.format(naeData.totalSolarRadiation));
        //double totalDatas = 0;
        //Weather oWeather      = null;
        //----- 積算温度の算出 -----
        /*
        if ((naeData.hashuDate != null)
            && (naeData.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

          Compartment ct = Compartment.getCompartmentInfo(compartmentStatusData.kukakuId);
          if (ct != null) {
            Field fd = ct.getFieldInfo();
            if (fd != null) {
              String pointId = PosttoPoint.getPointId(fd.postNo);
              if (pointId != null && !"".equals(pointId)) {
                java.sql.Date endDate;

                if ((compartmentStatusData.shukakuStartDate != null)
                    && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) { //収穫開始が正しく登録されている場合
                  endDate = compartmentStatusData.shukakuStartDate;
                }
                else {
                  Calendar cal = Calendar.getInstance();
                  DateU.setTime(cal, DateU.TimeType.TO);
                  cal.add(Calendar.MONTH, 2);
                  endDate = new java.sql.Date(cal.getTime().getTime());
                }

                List<Weather> weathers = Weather.getWeather(pointId, compartmentStatusData.hashuDate, endDate);

                for (Weather weather : weathers) {
                  totalDatas += weather.kionAve;
                  oWeather = weather;
                }
              }
            }
          }
        }
        */
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 消毒
        //----------------------------------------------------------------------------------------------------------------------------------
        long disinfectionCount = 0;
        if (naeData.finalDisinfectionDate != null && (naeData.finalDisinfectionDate.compareTo(nullDate) != 0)) {

          Date finalDisinfectionDate = DateUtils.truncate(naeData.finalDisinfectionDate, Calendar.DAY_OF_MONTH);

          naeStatusJson.put("finalDisinfectionDate"       , naeData.finalDisinfectionDate.toString());

          disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, systemDate);

        }
        else {
          naeStatusJson.put("finalDisinfectionDate"       , "");
          disinfectionCount = 0;
        }

        naeStatusJson.put("disinfectionCount"          , disinfectionCount);                                 //最終消毒日からの経過日数
        naeStatusJson.put("totalDisinfectionNumber"    , df.format(naeData.totalDisinfectionNumber * 0.001));//合計消毒量
        naeStatusJson.put("totalDisinfectionCount"     , naeData.totalDisinfectionCount);                    //合計消毒回数
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 潅水量
        //----------------------------------------------------------------------------------------------------------------------------------
        long kansuiCount = 0;
        if (naeData.finalKansuiDate != null && (naeData.finalKansuiDate.compareTo(nullDate) != 0)) {

          Date finalKansuiDate = DateUtils.truncate(naeData.finalKansuiDate, Calendar.DAY_OF_MONTH);

          naeStatusJson.put("finalKansuiDate"       , naeData.finalKansuiDate.toString());

          kansuiCount = DateU.GetDiffDate(finalKansuiDate, systemDate);

        }
        else {
          naeStatusJson.put("finalKansuiDate"       , "");
          kansuiCount = 0;
        }

        naeStatusJson.put("kansuiCount"          , kansuiCount);                         //最終潅水日からの経過日数
        naeStatusJson.put("totalKansuiNumber"    , df.format(naeData.totalKansuiNumber));//合計潅水量
        naeStatusJson.put("totalKansuiCount"     , naeData.totalKansuiCount);            //合計潅水回数
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 追肥
        //----------------------------------------------------------------------------------------------------------------------------------
        long tuihiCount = 0;
        if (naeData.finalTuihiDate != null && (naeData.finalTuihiDate.compareTo(nullDate) != 0)) {

          Date finalTuihiDate = DateUtils.truncate(naeData.finalTuihiDate, Calendar.DAY_OF_MONTH);

          naeStatusJson.put("finalTuihiDate"       , naeData.finalTuihiDate.toString());

          tuihiCount = DateU.GetDiffDate(finalTuihiDate, systemDate);

        }
        else {
        	naeStatusJson.put("finalTuihiDate"       , "");
          tuihiCount = 0;
        }

        naeStatusJson.put("tuihiCount"          , tuihiCount);                                 //最終追肥日からの経過日数
        naeStatusJson.put("totalTuihiNumber"    , df.format(naeData.totalTuihiNumber * 0.001));//合計追肥量
        naeStatusJson.put("totalTuihiCount"     , naeData.totalTuihiCount);                    //合計追肥回数

        if(api){
          ArrayNode chainJson = mapper.createArrayNode();
          nsc.getWorkStatusJsonArray(naeData.naeNo, chainJson);
          naeStatusJson.put("chain", chainJson);
        }else{
          ObjectNode chainJson = Json.newObject();
          nsc.getWorkStatusJson(naeData.naeNo, chainJson);
          naeStatusJson.put("chain", chainJson);
        }

        //----------------------------------------------------------------------------------------------------------------------------------
        //- 害虫情報
        //----------------------------------------------------------------------------------------------------------------------------------
        /*
        String on = "";
        double pestId   = 0;
        String pestName = "";
        String pestPredictDate = "";
        long diffPredictDate = 0;
        Calendar datacal = Calendar.getInstance();
        datacal.setTime(naeData.pestPredictDate);
        if (syscal.compareTo(datacal) <= 0) {
          pestId = naeData.pestId;
          Pest pest = Pest.getPestGeneration(pestId, naeData.pestGeneration);
          if (pest != null) {
            pestName = pest.pestName;
          }
          pestPredictDate = sdfL.format(naeData.pestPredictDate);
          diffPredictDate = DateU.GetDiffDate(syscal.getTime(), datacal.getTime());
          if (diffPredictDate <= 7) {
            on = "on";
          }
        }
        naeStatusJson.put("pestId"              , pestId);
        naeStatusJson.put("pestName"            , pestName);
        naeStatusJson.put("pestPredictDate"     , pestPredictDate);
        naeStatusJson.put("pestOn"              , on);
        */

        listJson.put(naeData.naeNo, naeStatusJson);
        listJsonApi.add(naeStatusJson);
        count++;
      }
      if(api){
        resultJson.put(AgryeelConst.IkubyoInfo.TARGETNAESTATUS, listJsonApi);
      }else{
        resultJson.put(AgryeelConst.IkubyoInfo.TARGETNAESTATUS, listJson);
      }
      if (count > 0) {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
      }
      else {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
      }

      return ok(resultJson);
    }

  /**
   * 初期検索育苗ラインデータを取得する
   * @return
   */
  public static Result getIkubyoLineInitData() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode   listJsonApi = mapper.createArrayNode();
      boolean     api        = false;

      if (session(AgryeelConst.SessionKey.API) != null) {
        api = true;
      }

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      if (getAccount == UserComprtnent.GET_ERROR) {
        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
        resultJson.put("result"   , "ERROR");
        return ok(resultJson);
      }

      listJson   = Json.newObject();  //リスト形式JSONを初期化する
      /* アカウント情報からタイムライン検索条件を取得する */
      UserComprtnent uc = new UserComprtnent();
      uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      Calendar cStart = Calendar.getInstance();
      Calendar cEnd = Calendar.getInstance();

      cStart.add(Calendar.DAY_OF_MONTH, -7);
      DateU.setTime(cStart, DateU.TimeType.FROM);
      DateU.setTime(cEnd, DateU.TimeType.TO);

      SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
      SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");

      double ikubyoDiaryId = 0;
      List<Account> workings = Account.getAccountOfWorking(accountComprtnent.accountData.farmId);
      for (Account working : workings) {                                        //作業情報をJSONデータに格納する

        Work wk = working.getWork();

        if (wk == null) { continue; }

        Calendar cal = Calendar.getInstance();
        long to   = cal.getTime().getTime();
        long from = working.workStartTime.getTime();

        long diff = ( to - from  ) / (1000 * 60 );

        ObjectNode timeLineJson         = Json.newObject();
        ikubyoDiaryId--;
        timeLineJson.put("ikubyoDiaryId", ikubyoDiaryId);                             //育苗記録ID
        timeLineJson.put("workdate"     , sdf2.format(working.workStartTime));        //作業日
        timeLineJson.put("updateTime"   , sdf.format(working.workStartTime));         //更新日時
        if (working.workPlanId != 0) {
          IkubyoPlanLine ipl = IkubyoPlanLine.find.where().eq("ikubyo_plan_id", working.workPlanId).findUnique();
          if (ipl != null) {
            timeLineJson.put("message"      , ipl.message);                               //メッセージ
          }
          else {
            continue;
          }
          String[] sNaeNos = ipl.naeNo.split("-");
          timeLineJson.put("naeNo"        , sNaeNos[1]);                                  //苗No
          NaeStatus ns = NaeStatus.find.where().eq("nae_no", ipl.naeNo).findUnique();
          if (ns != null) {
            timeLineJson.put("hinsyuName"   , ns.hinsyuName);                             //品種名
          }
          else {
            IkubyoPlan ip = IkubyoPlan.find.where().eq("ikubyo_plan_id", ipl.ikubyoPlanId).findUnique();
            timeLineJson.put("hinsyuName"   ,  Hinsyu.getMultiHinsyuName(ip.hinsyuId));   //品種名
          }
        }
        else {
          continue;
        }
        timeLineJson.put("timeLineColor", "aaaaaa");                                  //タイムラインカラー
        timeLineJson.put("workName"     , wk.workName);                               //作業名
        timeLineJson.put("accountName"  , working.acountName);                        //アカウント名
        timeLineJson.put("workId"       , wk.workId);                                 //作業ＩＤ
        timeLineJson.put("worktime"     , diff);                                      //作業時間

        listJson.put(Double.toString(ikubyoDiaryId), timeLineJson);
        listJsonApi.add(timeLineJson);

      }

      /* アカウント情報から育苗ライン情報を取得する */
      List<IkubyoLine> ikubyoLine = IkubyoLine.find.where().eq("farm_id", uc.accountData.farmId).between("work_date", new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis())).orderBy("work_date desc, update_time desc").findList();

      for (IkubyoLine ikubyoLineData : ikubyoLine) {                                  //育苗ライン情報をJSONデータに格納する

        IkubyoDiary id = IkubyoDiary.find.where().eq("ikubyo_diary_id", ikubyoLineData.ikubyoDiaryId).findUnique();

        if (id == null) { continue; }

        ObjectNode timeLineJson = Json.newObject();
        timeLineJson.put("ikubyoDiaryId", ikubyoLineData.ikubyoDiaryId);                 //育苗記録ID
        timeLineJson.put("workdate"     , sdf2.format(id.workDate));                    //作業日
        timeLineJson.put("updateTime"   , sdf.format(ikubyoLineData.updateTime));       //更新日時
        timeLineJson.put("message"      , StringU.setNullTrim(ikubyoLineData.message)); //メッセージ
        timeLineJson.put("timeLineColor", ikubyoLineData.timeLineColor);                //タイムラインカラー
        timeLineJson.put("workName"     , ikubyoLineData.workName);                     //作業名
        String[] sNaeNos = ikubyoLineData.naeNo.split("-");
        timeLineJson.put("naeNo"        , sNaeNos[1]);                                  //苗No
        NaeStatus ns = NaeStatus.find.where().eq("nae_no", ikubyoLineData.naeNo).findUnique();
        if (ns != null) {
          timeLineJson.put("hinsyuName" , ns.hinsyuName);                               //品種名
        }
        else {
          timeLineJson.put("hinsyuName" , Hinsyu.getMultiHinsyuName(id.hinsyuId));      //品種名
        }
        timeLineJson.put("accountName"  , ikubyoLineData.accountName);                  //アカウント名
        timeLineJson.put("workId"       , ikubyoLineData.workId);                       //作業ＩＤ
        timeLineJson.put("worktime"     , id.workTime);                                 //作業時間

        listJson.put(Double.toString(ikubyoLineData.ikubyoDiaryId), timeLineJson);
        listJsonApi.add(timeLineJson);

      }

      if(api){
        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJsonApi);
      }else{
        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
      }
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }

  /**
   * 育苗ライン検索条件をもとに育苗ライン情報を取得する
   * @return
   */
  public static Result getIkubyoLineData() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode   listJsonApi = mapper.createArrayNode();
      boolean     api        = false;

      if (session(AgryeelConst.SessionKey.API) != null) {
        api = true;
      }

      listJson   = Json.newObject();  //リスト形式JSONを初期化する
      /* アカウント情報からタイムライン検索条件を取得する */
      UserComprtnent uc = new UserComprtnent();
      uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      String[] works = uc.accountIkubyoStatusData.selectWorkId.split(",");
      List<Double>      aryWork     = new ArrayList<Double>();
      for (String data : works) {
        try {
          if ("".equals(data) || "\'\'".equals(data)) {
            continue;
          }
          aryWork.add(Double.parseDouble(data));
        }
        catch (NumberFormatException nfe) {

        }
        finally {

        }
      }
      if (aryWork.size() == 0) {
        List<Work> workds = Work.getWorkOfFarm(uc.accountData.farmId);
        for (Work data: workds) {
          if ("".equals(data) || "\'\'".equals(data)) {
            continue;
          }
          aryWork.add(data.workId);
        }
      }
      String[] accounts = uc.accountIkubyoStatusData.selectAccountId.split(",");
      List<String>      aryAccount     = new ArrayList<String>();
      for (String data : accounts) {
        try {
          if ("".equals(data) || "\'\'".equals(data)) {
            continue;
          }
          aryAccount.add(data);
        }
        finally {

        }
      }
      if (aryAccount.size() == 0) {
        List<Account> accountds = Account.getAccountOfFarm(uc.accountData.farmId);
        for (Account data: accountds) {
          aryAccount.add(data.accountId);
        }
      }
      //----- 生産物 -----
      String[] crops = uc.accountIkubyoStatusData.selectCropId.split(",");
      List<Double>      aryCrop     = new ArrayList<Double>();
      for (String data : crops) {
        try {
          if ("".equals(data) || "\'\'".equals(data)) {
            continue;
          }
          aryCrop.add(Double.parseDouble(data));
        }
        catch (NumberFormatException nfe) {

        }
        finally {

        }
      }
      //----- 品種 -----
      String[] hinsyus = uc.accountIkubyoStatusData.selectHinsyuId.split(",");
      List<Double>      aryHinsyu     = new ArrayList<Double>();
      for (String data : hinsyus) {
        try {
          if ("".equals(data) || "\'\'".equals(data)) {
            continue;
          }
          aryHinsyu.add(Double.parseDouble(data));
        }
        catch (NumberFormatException nfe) {

        }
        finally {

        }
      }

      SimpleDateFormat sdf  = new SimpleDateFormat("MM.dd HH:mm");
      SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");

      if ((uc.accountIkubyoStatusData.selectWorking == 1)
          || (uc.accountIkubyoStatusData.selectWorking == 2)) {

        double ikubyoDiaryId = 0;
        List<Account> workings = Account.getAccountOfWorking(accountComprtnent.accountData.farmId);
        for (Account working : workings) {                                        //作業情報をJSONデータに格納する

          Work wk = working.getWork();

          if (wk == null) { continue; }

          Calendar cal = Calendar.getInstance();
          long to   = cal.getTime().getTime();
          long from = working.workStartTime.getTime();

          long diff = ( to - from  ) / (1000 * 60 );

          ObjectNode timeLineJson         = Json.newObject();
          ikubyoDiaryId--;
          timeLineJson.put("ikubyoDiaryId"   , ikubyoDiaryId);                          //育苗記録ID
          timeLineJson.put("workdate"     , sdf2.format(working.workStartTime));        //作業日
          timeLineJson.put("updateTime"   , sdf.format(working.workStartTime));         //更新日時
          if (working.workPlanId != 0) {
            IkubyoPlanLine ipl = IkubyoPlanLine.find.where().eq("ikubyo_plan_id", working.workPlanId).findUnique();
            if (ipl != null) {
              timeLineJson.put("message"      , ipl.message);                            //メッセージ
            }
            else {
              continue;
            }
            String[] sNaeNos = ipl.naeNo.split("-");
            timeLineJson.put("naeNo"        , sNaeNos[1]);                                  //苗No
            NaeStatus ns = NaeStatus.find.where().eq("nae_no", ipl.naeNo).findUnique();
            if (ns != null) {
              timeLineJson.put("hinsyuName"   , ns.hinsyuName);                             //品種名
            }
            else {
              IkubyoPlan ip = IkubyoPlan.find.where().eq("ikubyo_plan_id", ipl.ikubyoPlanId).findUnique();
              timeLineJson.put("hinsyuName"   ,  Hinsyu.getMultiHinsyuName(ip.hinsyuId));   //品種名
            }
          }
          else {
            continue;
          }
          timeLineJson.put("timeLineColor", "aaaaaa");                                  //タイムラインカラー
          timeLineJson.put("workName"     , wk.workName);                               //作業名
          timeLineJson.put("accountName"  , working.acountName);                        //アカウント名
          timeLineJson.put("workId"       , wk.workId);                                 //作業ＩＤ
          timeLineJson.put("worktime"     , diff);                                      //作業時間

          listJson.put(Double.toString(ikubyoDiaryId), timeLineJson);
          listJsonApi.add(timeLineJson);

        }

      }

      if ((uc.accountIkubyoStatusData.selectWorking == 1)
          || (uc.accountIkubyoStatusData.selectWorking == 3)) {

        /* アカウント情報から育苗ライン情報を取得する */
        List<IkubyoLine> ikubyoLine = IkubyoLine.find.where().eq("farm_id", uc.accountData.farmId).in("account_id", aryAccount).in("work_id", aryWork).between("work_date", uc.accountIkubyoStatusData.selectStartDate, uc.accountIkubyoStatusData.selectEndDate).orderBy("work_date desc, update_time desc").findList();

        for (IkubyoLine ikubyoLineData : ikubyoLine) {                                        //育苗ライン情報をJSONデータに格納する

          IkubyoDiary id = IkubyoDiary.find.where().eq("ikubyo_diary_id", ikubyoLineData.ikubyoDiaryId).findUnique();

          if (id == null) { continue; }

          //----- 生産物などのチェック -----
          if ((aryCrop.size() > 0) || (aryHinsyu.size() > 0) ) {
            NaeStatus naeStatus = NaeStatus.find.where().eq("nae_no", id.naeNo).findUnique();
            if (naeStatus == null) { //生産物指定または品種指定ありで、苗状況データなしは対象外とする
              continue;
            }
            if (aryCrop.size() > 0) {
              if (!ListrU.keyCheck(aryCrop, naeStatus.cropId)) {
                continue;
              }
            }
            if (aryHinsyu.size() > 0) {
              if (!ListrU.keyChecks(aryHinsyu, naeStatus.hinsyuId)) {
                continue;
              }
            }
          }

          ObjectNode timeLineJson         = Json.newObject();
          timeLineJson.put("ikubyoDiaryId", ikubyoLineData.ikubyoDiaryId);                //育苗記録ID
          timeLineJson.put("workdate"     , sdf2.format(id.workDate));                    //作業日
          timeLineJson.put("updateTime"   , sdf.format(ikubyoLineData.updateTime));       //更新日時
          timeLineJson.put("message"      , StringU.setNullTrim(ikubyoLineData.message)); //メッセージ
          timeLineJson.put("timeLineColor", ikubyoLineData.timeLineColor);                //タイムラインカラー
          timeLineJson.put("workName"     , ikubyoLineData.workName);                     //作業名
          String[] sNaeNos = ikubyoLineData.naeNo.split("-");
          timeLineJson.put("naeNo"        , sNaeNos[1]);                                  //苗No
          NaeStatus ns = NaeStatus.find.where().eq("nae_no", ikubyoLineData.naeNo).findUnique();
          if (ns != null) {
            timeLineJson.put("hinsyuName" , ns.hinsyuName);                               //品種名
          }
          else {
            timeLineJson.put("hinsyuName" , Hinsyu.getMultiHinsyuName(id.hinsyuId));      //品種名
          }
          timeLineJson.put("accountName"  , ikubyoLineData.accountName);                  //アカウント名
          timeLineJson.put("workId"       , ikubyoLineData.workId);                       //作業ＩＤ
          timeLineJson.put("worktime"     , id.workTime);                                 //作業時間

          listJson.put(Double.toString(ikubyoLineData.ikubyoDiaryId), timeLineJson);
          listJsonApi.add(timeLineJson);

        }

      }

      if(api){
        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJsonApi);
      }else{
        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
      }

      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }

    /**
     * 苗詳細情報を取得する
     * @return
     */
    public static Result getNaeDetail() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson    = Json.newObject();
      ObjectNode  timelineListJson  = Json.newObject();
      ObjectNode  nouyakuListJson   = Json.newObject();
      ObjectNode  hiryoListJson     = Json.newObject();
      ObjectNode  nJ = Json.newObject();
      Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode   timelineListJsonApi = mapper.createArrayNode();
      ArrayNode   nouyakuListJsonApi  = mapper.createArrayNode();
      ArrayNode   hiryoListJsonApi    = mapper.createArrayNode();
      ArrayNode   nJApi = mapper.createArrayNode();
      DecimalFormat df = new DecimalFormat("#,##0.00");
      boolean     api        = false;
      if (session(AgryeelConst.SessionKey.API) != null) {
      	api = true;
      }

      /* JSONデータを取得 */
      JsonNode inputJson = request().body().asJson();
      String naeNo = inputJson.get("nae").asText();
      String[] sNaeNos = naeNo.split("-");

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
      NaeStatusCompornent nsc = new NaeStatusCompornent();
      nsc.getAllData(accountComprtnent.accountData.farmId);
      Farm farm = Farm.getFarm(accountComprtnent.accountData.farmId);
      FarmStatus farmStatus = farm.getFarmStatus();
      List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList(AgryeelConst.IkubyoInfo.WORKCHAINID);

      NaeStatus naeStatus = NaeStatus.find.where().eq("nae_no", naeNo).findUnique();

      nJ.put("naeNo"            , naeStatus.naeNo);                     //苗No
      nJ.put("naeName"          , sNaeNos[1]);                          //苗名
      nJ.put("zaikoSuryo"       , naeStatus.zaikoSuryo);                //在庫数量
      nJ.put("kosu"             , naeStatus.kosu);                      //個数
      nJ.put("unit"             , nsc.getYoukiUnit(naeStatus.naeNo));   //単位

      //----------------------------------------------------------------------------------------------------------------------------------
      //- 生産物
      //----------------------------------------------------------------------------------------------------------------------------------
      Crop crop = CropComprtnent.getCropById(naeStatus.cropId);
      if (crop != null ){
        nJ.put("cropId"          , crop.cropId);
        nJ.put("cropName"        , crop.cropName);
      }
      else {
        if(api){
          nJ.put("cropId"          , 0);
        }
        else {
          nJ.put("cropId"          , "");
        }
        nJ.put("cropName"        , "");
      }
      if (naeStatus.hinsyuName != null && !"".equals(naeStatus.hinsyuName)) {
        nJ.put("hinsyuName"        , naeStatus.hinsyuName);            //品種名
      }
      else {
        nJ.put("hinsyuName"        , "");                              //品種名
      }
      long seiikuDayCount = 0;
      if (naeStatus.hashuDate != null && (naeStatus.hashuDate.compareTo(nullDate) != 0)) {  //播種日

        Date hashuDate = DateUtils.truncate(naeStatus.hashuDate, Calendar.DAY_OF_MONTH);
        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

        nJ.put("hashuDate"       , naeStatus.hashuDate.toString());
        //生育日数の表示方法を変更
        if (naeStatus.seiikuDayCount != 0) {
          seiikuDayCount = naeStatus.seiikuDayCount;
        }
        else {
          seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
        }
      }
      else {
        nJ.put("hashuDate"       , "");
        seiikuDayCount = 0;
      }
      nJ.put("seiikuDayCount"    , seiikuDayCount);                                                   //生育日数

      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
      Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

      ObjectNode  labels    = Json.newObject();
      ObjectNode  datas     = Json.newObject();
      ObjectNode  mixdatas  = Json.newObject();
      double totalDatas     = 0;
      double todayDatas     = 0;
      java.sql.Date oldDate = null;
      Weather oWeather      = null;
      int idx = 0;
      //----------------------------------------------------------------------------------------------------------------------------------
      //- 積算温度
      //----------------------------------------------------------------------------------------------------------------------------------
      /*
      kJ.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));                   //積算温度

      //----- 積算温度の算出 -----
      if ((compartmentStatusData.hashuDate != null)
          && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

        Compartment ct = Compartment.getCompartmentInfo(compartmentStatusData.kukakuId);
        if (ct != null) {
          Field fd = ct.getFieldInfo();
          if (fd != null) {
            String pointId = PosttoPoint.getPointId(fd.postNo);
            if (pointId != null && !"".equals(pointId)) {
              java.sql.Date endDate;

              if ((compartmentStatusData.shukakuStartDate != null)
                  && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) { //収穫開始が正しく登録されている場合
                endDate = compartmentStatusData.shukakuStartDate;
              }
              else {
                Calendar cal = Calendar.getInstance();
                if (year != 0 && rotation != 0) {
                  cal.setTime(compartmentStatusData.finalEndDate);
                  DateU.setTime(cal, DateU.TimeType.TO);
                  endDate = new java.sql.Date(cal.getTime().getTime());
                }
                else {
                  DateU.setTime(cal, DateU.TimeType.TO);
                  cal.add(Calendar.MONTH, 2);
                  endDate = new java.sql.Date(cal.getTime().getTime());
                }
              }

              List<Weather> weathers = Weather.getWeather(pointId, compartmentStatusData.hashuDate, endDate);

              for (Weather weather : weathers) {
                idx++;
                labels.put(String.valueOf(idx), sdf.format(weather.dayDate));
                datas.put(String.valueOf(idx), weather.kionAve);
                totalDatas += weather.kionAve;
                mixdatas.put(String.valueOf(idx), totalDatas);
                oWeather = weather;
              }
            }
          }
        }
      }
      if (oWeather != null) {
        kJ.put("yosokuSolarDate"      , sdf.format(oWeather.dayDate));
      }
      else {
        kJ.put("yosokuSolarDate"      , "");
      }
      kJ.put("yosokuSolarRadiation"   , df.format(totalDatas));
      kJ.put("solarRadiationLabel"    , labels);
      kJ.put("solarRadiationData"     , datas);
      kJ.put("solarRadiationMixData"  , mixdatas);
      */
      //----------------------------------------------------------------------------------------------------------------------------------
      //- 消毒
      //----------------------------------------------------------------------------------------------------------------------------------
      long disinfectionCount = 0;
      if (naeStatus.finalDisinfectionDate != null && (naeStatus.finalDisinfectionDate.compareTo(nullDate) != 0)) {
        Date finalDisinfectionDate = DateUtils.truncate(naeStatus.finalDisinfectionDate, Calendar.DAY_OF_MONTH);
        nJ.put("finalDisinfectionDate"       , naeStatus.finalDisinfectionDate.toString());
        disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, systemDate);
      }
      else {
        nJ.put("finalDisinfectionDate"       , "");
        disinfectionCount = 0;
      }

      nJ.put("disinfectionCount"          , disinfectionCount);                                       //最終消毒日からの経過日数
      nJ.put("totalDisinfectionNumber"    , df.format(naeStatus.totalDisinfectionNumber * 0.001));    //合計消毒量
      nJ.put("totalDisinfectionCount"     , naeStatus.totalDisinfectionCount);                        //合計消毒回数
      //グラフデータの作成
      List<Double> disinfectionKey = new ArrayList<Double>();
      for (WorkChainItem wci : wcis) {
        Work wk = Work.getWork(wci.workId);
        if (wk.workTemplateId == AgryeelConst.WorkTemplate.SANPU
            && wci.nouhiKind == AgryeelConst.NouhiKind.NOUYAKU) {
          disinfectionKey.add(wk.workId);
        }
      }
      List<IkubyoDiary> ids = IkubyoDiary.find.where().eq("naeNo", naeStatus.naeNo).in("work_id", disinfectionKey).between("work_date", naeStatus.katadukeDate, new java.sql.Date(systemDate.getTime())).orderBy("work_date").findList();
      labels    = Json.newObject();
      datas     = Json.newObject();
      mixdatas  = Json.newObject();
      totalDatas = 0;
      todayDatas = 0;
      oldDate = null;
      idx = 0;
      for (IkubyoDiary id : ids) {
        if (oldDate == null) {
          idx++;
          labels.put(String.valueOf(idx), sdf.format(id.workDate));
          oldDate = id.workDate;
        }
        else {
          if (oldDate.compareTo(id.workDate) != 0) {
            datas.put(String.valueOf(idx), todayDatas);
            mixdatas.put(String.valueOf(idx), totalDatas);
            idx++;
            labels.put(String.valueOf(idx), sdf.format(id.workDate));
            oldDate = id.workDate;
            todayDatas = 0;
          }
        }
        List<IkubyoDiarySanpu> idss = IkubyoDiarySanpu.getIkubyoDiarySanpuList(id.ikubyoDiaryId);
        for (IkubyoDiarySanpu idsd : idss) {
          double hosei = 1;
          Nouhi nouhi = Nouhi.getNouhiInfo(idsd.nouhiId);
          if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
            hosei = 0.001;
          }
          totalDatas += (idsd.sanpuryo * hosei);
          todayDatas += (idsd.sanpuryo * hosei);
        }
      }
      datas.put(String.valueOf(idx), todayDatas);
      mixdatas.put(String.valueOf(idx), totalDatas);
      nJ.put("disinfectionLabel"    , labels);
      nJ.put("disinfectionData"     , datas);
      nJ.put("disinfectionMixData"  , mixdatas);
      //----------------------------------------------------------------------------------------------------------------------------------
      //- 潅水量
      //----------------------------------------------------------------------------------------------------------------------------------
      long kansuiCount = 0;
      if (naeStatus.finalKansuiDate != null && (naeStatus.finalKansuiDate.compareTo(nullDate) != 0)) {
        Date finalKansuiDate = DateUtils.truncate(naeStatus.finalKansuiDate, Calendar.DAY_OF_MONTH);
        nJ.put("finalKansuiDate"       , naeStatus.finalKansuiDate.toString());
        kansuiCount = DateU.GetDiffDate(finalKansuiDate, systemDate);
      }
      else {
        nJ.put("finalKansuiDate"       , "");
        kansuiCount = 0;
      }

      nJ.put("kansuiCount"          , kansuiCount);                            //最終潅水日からの経過日数
      nJ.put("totalKansuiNumber"    , df.format(naeStatus.totalKansuiNumber)); //合計潅水量
      nJ.put("totalKansuiCount"     , naeStatus.totalKansuiCount);             //合計潅水回数
      //グラフデータの作成
      List<Double> kansuiKey = new ArrayList<Double>();
      for (WorkChainItem wci : wcis) {
        Work wk = Work.getWork(wci.workId);
        if (wk.workTemplateId == AgryeelConst.WorkTemplate.KANSUI) {
          kansuiKey.add(wk.workId);
        }
      }
      ids   = IkubyoDiary.find.where().eq("naeNo", naeStatus.naeNo).in("work_id", kansuiKey).between("work_date", naeStatus.katadukeDate, new java.sql.Date(systemDate.getTime())).orderBy("work_date").findList();
      labels    = Json.newObject();
      datas     = Json.newObject();
      mixdatas  = Json.newObject();
      totalDatas = 0;
      todayDatas = 0;
      oldDate = null;
      idx = 0;
      for (IkubyoDiary id : ids) {
        if (oldDate == null) {
          idx++;
          labels.put(String.valueOf(idx), sdf.format(id.workDate));
          oldDate = id.workDate;
        }
        else {
          if (oldDate.compareTo(id.workDate) != 0) {
            datas.put(String.valueOf(idx), todayDatas);
            mixdatas.put(String.valueOf(idx), totalDatas);
            idx++;
            labels.put(String.valueOf(idx), sdf.format(id.workDate));
            oldDate = id.workDate;
            todayDatas = 0;
          }
        }
        totalDatas += id.kansuiRyo;
        todayDatas += id.kansuiRyo;
      }
      datas.put(String.valueOf(idx), todayDatas);
      mixdatas.put(String.valueOf(idx), totalDatas);
      nJ.put("kansuiLabel"    , labels);
      nJ.put("kansuiData"     , datas);
      nJ.put("kansuiMixData"  , mixdatas);

      //----------------------------------------------------------------------------------------------------------------------------------
      //- 追肥
      //----------------------------------------------------------------------------------------------------------------------------------
      long tuihiCount = 0;
      if (naeStatus.finalTuihiDate != null && (naeStatus.finalTuihiDate.compareTo(nullDate) != 0)) {
        Date finalTuihiDate = DateUtils.truncate(naeStatus.finalTuihiDate, Calendar.DAY_OF_MONTH);
        nJ.put("finalTuihiDate"       , naeStatus.finalTuihiDate.toString());
        tuihiCount = DateU.GetDiffDate(finalTuihiDate, systemDate);
     }
      else {
        nJ.put("finalTuihiDate"       , "");
        tuihiCount = 0;
      }

      nJ.put("tuihiCount"          , tuihiCount);                                    //最終追肥日からの経過日数
      nJ.put("totalTuihiNumber"    , df.format(naeStatus.totalTuihiNumber * 0.001)); //合計追肥量
      nJ.put("totalTuihiCount"     , naeStatus.totalTuihiCount);                     //合計追肥回数

      //グラフデータの作成
      List<Double> tuihiKey = new ArrayList<Double>();
      for (WorkChainItem wci : wcis) {
        Work wk = Work.getWork(wci.workId);
        if (wk.workTemplateId == AgryeelConst.WorkTemplate.SANPU
            && wci.nouhiKind == AgryeelConst.NouhiKind.HIRYO) {
          tuihiKey.add(wk.workId);
        }
      }
      ids   = IkubyoDiary.find.where().eq("naeNo", naeStatus.naeNo).in("work_id", tuihiKey).between("work_date", naeStatus.katadukeDate, new java.sql.Date(systemDate.getTime())).orderBy("work_date").findList();
      labels    = Json.newObject();
      datas     = Json.newObject();
      mixdatas  = Json.newObject();
      totalDatas = 0;
      todayDatas = 0;
      oldDate = null;
      idx = 0;
      for (IkubyoDiary id : ids) {
        if (!(naeStatus.hashuDate != null && naeStatus.hashuDate.compareTo(id.workDate) <= 0)) {
          continue;
        }
        if (oldDate == null) {
          idx++;
          labels.put(String.valueOf(idx), sdf.format(id.workDate));
          oldDate = id.workDate;
        }
        else {
          if (oldDate.compareTo(id.workDate) != 0) {
            datas.put(String.valueOf(idx), todayDatas);
            mixdatas.put(String.valueOf(idx), totalDatas);
            idx++;
            labels.put(String.valueOf(idx), sdf.format(id.workDate));
            oldDate = id.workDate;
            todayDatas = 0;
          }
        }
        List<IkubyoDiarySanpu> idss = IkubyoDiarySanpu.getIkubyoDiarySanpuList(id.ikubyoDiaryId);
        for (IkubyoDiarySanpu idsd : idss) {
          double hosei = 1;
          Nouhi nouhi = Nouhi.getNouhiInfo(idsd.nouhiId);
          if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
            hosei = 0.001;
          }
          totalDatas += (idsd.sanpuryo * hosei);
          todayDatas += (idsd.sanpuryo * hosei);
        }
      }
      datas.put(String.valueOf(idx), todayDatas);
      mixdatas.put(String.valueOf(idx), totalDatas);
      nJ.put("tuihiLabel"    , labels);
      nJ.put("tuihiData"     , datas);
      nJ.put("tuihiMixData"  , mixdatas);

      ObjectNode chainJson = Json.newObject();
      ArrayNode chainJsonApi = mapper.createArrayNode();
      if(api){
        nsc.getWorkStatusJsonArray(naeStatus.naeNo, chainJsonApi);
      }else{
        nsc.getWorkStatusJson(naeStatus.naeNo, chainJson);
      }
      if(api){
        nJ.put("chain", chainJsonApi);
      }else{
        nJ.put("chain", chainJson);
      }

      //----------------------------------------------------------------------------------------------------------------------------------
      //- 農薬情報の取得
      //----------------------------------------------------------------------------------------------------------------------------------
      int nouyakuNo = 0;
      int hiryoNo = 0;
      SimpleDateFormat sdfN = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat sdfK = new SimpleDateFormat("yyyyMMdd");
      DecimalFormat    dfN  = new DecimalFormat("##0.000");
      DecimalFormat    dfK  = new DecimalFormat("00000000");
      disinfectionKey = new ArrayList<Double>();
      for (WorkChainItem wci : wcis) {
        Work wk = Work.getWork(wci.workId);
        if (wk.workTemplateId == AgryeelConst.WorkTemplate.SANPU
            && wci.nouhiKind == AgryeelConst.NouhiKind.NOUYAKU) {
          disinfectionKey.add(wk.workId);
        }
      }
      ids = IkubyoDiary.find.where().eq("naeNo", naeStatus.naeNo).in("work_id", disinfectionKey).between("work_date", naeStatus.katadukeDate, new java.sql.Date(systemDate.getTime())).orderBy("work_date").findList();
      nouyakuNo = 1;
      for (IkubyoDiary id : ids) {
        List<IkubyoDiarySanpu> idss = IkubyoDiarySanpu.getIkubyoDiarySanpuList(id.ikubyoDiaryId);
        for (IkubyoDiarySanpu idsd : idss) {
          Nouhi nouhi = Nouhi.getNouhiInfo(idsd.nouhiId);
          ObjectNode nj         = Json.newObject();
          nj.put("key"        , "n" + dfK.format(nouyakuNo));
          nj.put("color"      , AgryeelConst.ColorData[(int)(nouyakuNo % 10)]);
          nj.put("no"         , "殺虫" + nouyakuNo);
          nj.put("date"       , sdfN.format(id.workDate));
          nj.put("diffdate"   , DateU.GetDiffDate(id.workDate, systemDate));
          nj.put("nouhiName"  , nouhi.nouhiName);
          nj.put("bairitu"    , idsd.bairitu + "倍");
          nj.put("method"     , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, idsd.sanpuMethod));
          nj.put("ryo"        , dfN.format(idsd.sanpuryo / 1000)  + " " + nouhi.getUnitString());
          nouyakuListJson.put("n" + dfK.format(nouyakuNo) + sdfK.format(id.workDate), nj);
          nouyakuListJsonApi.add(nj);
          nouyakuNo++;
        }
      }
      //----------------------------------------------------------------------------------------------------------------------------------
      //- 肥料情報の取得
      //----------------------------------------------------------------------------------------------------------------------------------
      tuihiKey = new ArrayList<Double>();
      for (WorkChainItem wci : wcis) {
        Work wk = Work.getWork(wci.workId);
        if (wk.workTemplateId == AgryeelConst.WorkTemplate.SANPU
            && wci.nouhiKind == AgryeelConst.NouhiKind.HIRYO) {
          tuihiKey.add(wk.workId);
        }
      }
      ids   = IkubyoDiary.find.where().eq("naeNo", naeStatus.naeNo).in("work_id", tuihiKey).between("work_date", naeStatus.katadukeDate, new java.sql.Date(systemDate.getTime())).orderBy("work_date").findList();
      int motogoe = 0;
      int tuihi   = 0;
      double n  = 0;
      double p  = 0;
      double k  = 0;
      double mg = 0;
      hiryoNo = 1;
      for (IkubyoDiary id : ids) {
        List<IkubyoDiarySanpu> idss = IkubyoDiarySanpu.getIkubyoDiarySanpuList(id.ikubyoDiaryId);
        for (IkubyoDiarySanpu idsd : idss) {
          Nouhi nouhi = Nouhi.getNouhiInfo(idsd.nouhiId);
          ObjectNode nj = Json.newObject();
          if (!(naeStatus.hashuDate != null && naeStatus.hashuDate.compareTo(id.workDate) <= 0)) {
            motogoe++;
            nj.put("no"     , "元肥" + motogoe);
          }
          else {
            tuihi++;
            nj.put("no"     , "追肥" + tuihi);
          }
          nj.put("key"        , "h" + dfK.format(hiryoNo));
          nj.put("color"      , AgryeelConst.ColorData[(int)(hiryoNo % 10)]);
          nj.put("date"       , sdfN.format(id.workDate));
          nj.put("diffdate"   , DateU.GetDiffDate(id.workDate, systemDate));
          nj.put("nouhiName"  , nouhi.nouhiName);
          nj.put("bairitu"    , idsd.bairitu + "倍");
          nj.put("method"     , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, idsd.sanpuMethod));
          nj.put("ryo"        , dfN.format(idsd.sanpuryo / 1000)  + " " + nouhi.getUnitString());
          nj.put("n"          , "N&nbsp;:" + dfN.format(nouhi.n * 0.001)  + " " + nouhi.getUnitString());
          nj.put("p"          , "P&nbsp;:" + dfN.format(nouhi.p * 0.001)  + " " + nouhi.getUnitString());
          nj.put("k"          , "K&nbsp;:" + dfN.format(nouhi.k * 0.001)  + " " + nouhi.getUnitString());
          nj.put("mg"         , "Mg:" + dfN.format(nouhi.mg * 0.001) + " " + nouhi.getUnitString());
          n   += (nouhi.n   * 0.001);
          p   += (nouhi.p   * 0.001);
          k   += (nouhi.k   * 0.001);
          mg  += (nouhi.mg  * 0.001);

          hiryoListJson.put("h" + dfK.format(hiryoNo) + sdfK.format(id.workDate), nj);
          hiryoListJsonApi.add(nj);
          hiryoNo++;
        }
      }
      //総合計の生成
      ObjectNode nj         = Json.newObject();
      nj.put("no"         , "合 計");
      nj.put("key"        , "h9999");
      nj.put("color"      , AgryeelConst.ColorData[(int)(9999 % 10)]);
      nj.put("date"       , "");
      nj.put("diffdate"   , "");
      nj.put("nouhiName"  , "年間肥料成分量");
      nj.put("bairitu"    , "");
      nj.put("method"     , "");
      nj.put("ryo"        , "");
      nj.put("n"          , "N&nbsp;:" + dfN.format(n)  + " Kg");
      nj.put("p"          , "P&nbsp;:" + dfN.format(p)  + " Kg");
      nj.put("k"          , "K&nbsp;:" + dfN.format(k)  + " Kg");
      nj.put("mg"         , "Mg:" + dfN.format(mg) + " Kg");
      hiryoListJson.put("h999999999999", nj);
      hiryoListJsonApi.add(nj);
      //----------------------------------------------------------------------------------------------------------------------------------
      //- 育苗ラインの取得
      //----------------------------------------------------------------------------------------------------------------------------------
      /* アカウント情報から育苗ライン情報を取得する */
      List<IkubyoLine> ikubyoLine;
      ikubyoLine = IkubyoLine.find.where().eq("nae_no", naeStatus.naeNo).between("work_date", naeStatus.katadukeDate, new java.sql.Date(systemDate.getTime())).orderBy("work_date desc, update_time desc").findList();

      SimpleDateFormat sdf2  = new SimpleDateFormat("MM.dd HH:mm");
      SimpleDateFormat sdf3 = new SimpleDateFormat("MM/dd");

      for (IkubyoLine ikubyoLineData : ikubyoLine) {                                        //作業情報をJSONデータに格納する

        IkubyoDiary id = IkubyoDiary.find.where().eq("ikubyo_diary_id", ikubyoLineData.ikubyoDiaryId).findUnique();

        if (id == null) { continue; }

        ObjectNode ikubyoLineJson         = Json.newObject();
        ikubyoLineJson.put("ikubyoDiaryId", id.ikubyoDiaryId);                            //ＩＤ
        ikubyoLineJson.put("workdate"     , sdf3.format(id.workDate));                    //作業日
        ikubyoLineJson.put("updateTime"   , sdf2.format(ikubyoLineData.updateTime));      //更新日時
        ikubyoLineJson.put("message"      , StringU.setNullTrim(ikubyoLineData.message)); //メッセージ
        ikubyoLineJson.put("timeLineColor", ikubyoLineData.timeLineColor);                //タイムラインカラー
        ikubyoLineJson.put("workName"     , ikubyoLineData.workName);                     //作業名
        ikubyoLineJson.put("naeNo"        , sNaeNos[1]);                                  //苗No
        ikubyoLineJson.put("hinsyuName" , naeStatus.hinsyuName);                          //品種名
        ikubyoLineJson.put("accountName"  , ikubyoLineData.accountName);                  //アカウント名
        ikubyoLineJson.put("workId"       , ikubyoLineData.workId);                       //作業ＩＤ
        ikubyoLineJson.put("worktime"     , id.workTime);                                 //作業時間

        timelineListJson.put(Double.toString(ikubyoLineData.ikubyoDiaryId), ikubyoLineJson);
        timelineListJsonApi.add(ikubyoLineJson);

      }

      if(api){
        resultJson.put("naeJson", nJApi);
        resultJson.put("targetTimeLine", timelineListJsonApi);
        resultJson.put("nouyakuList", nouyakuListJsonApi);
        resultJson.put("hiryoList"  , hiryoListJsonApi);
      }else{
        resultJson.put("naeJson", nJ);
        resultJson.put("targetTimeLine", timelineListJson);
        resultJson.put("nouyakuList", nouyakuListJson);
        resultJson.put("hiryoList"  , hiryoListJson);
      }
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }

  /**
   * 苗検索条件をアカウント状況に反映する
   * @return
   */
  public static Result naesearchCommit() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      /* JSONデータを取得 */
      JsonNode input = request().body().asJson();

      accountComprtnent.accountIkubyoStatusData.ssnCrop      = input.get("sc").asText();
      accountComprtnent.accountIkubyoStatusData.ssnHinsyu    = input.get("sh").asText();
      accountComprtnent.accountIkubyoStatusData.ssnSeiikuF   = input.get("from").asInt();
      accountComprtnent.accountIkubyoStatusData.ssnSeiikuT   = input.get("to").asInt();
      accountComprtnent.accountIkubyoStatusData.update();

      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }

  /**
   * 育苗ライン検索条件をアカウント状況に反映する
   * @return
   */
  public static Result ikubyolineStatusCommit() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      /* JSONデータを取得 */
      JsonNode input = request().body().asJson();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

      try {

        Calendar cFrom = Calendar.getInstance();
        cFrom.setTimeInMillis(sdf.parse(input.get("from").asText()).getTime());
        DateU.setTime(cFrom, DateU.TimeType.FROM);

        Calendar cTo = Calendar.getInstance();
        cTo.setTimeInMillis(sdf.parse(input.get("to").asText()).getTime());
        DateU.setTime(cTo, DateU.TimeType.TO);

        java.sql.Timestamp from = new java.sql.Timestamp(cFrom.getTimeInMillis());
        java.sql.Timestamp to   = new java.sql.Timestamp(cTo.getTimeInMillis());

        accountComprtnent.accountIkubyoStatusData.selectStartDate = from;
        accountComprtnent.accountIkubyoStatusData.selectEndDate   = to;
        accountComprtnent.accountIkubyoStatusData.selectWorkId    = input.get("work").asText();
        accountComprtnent.accountIkubyoStatusData.selectAccountId = input.get("account").asText();
        accountComprtnent.accountIkubyoStatusData.selectWorking   = input.get("working").asInt();
        accountComprtnent.accountIkubyoStatusData.selectCropId    = input.get("crop").asText();
        accountComprtnent.accountIkubyoStatusData.selectHinsyuId  = input.get("hinsyu").asText();
        accountComprtnent.accountIkubyoStatusData.update();

        resultJson.put("result"   , "SUCCESS");
      } catch (ParseException e) {
        e.printStackTrace();
        resultJson.put("result"   , "ERROR");
      }

      return ok(resultJson);
  }

  /**
   * 生産者IDから苗情報一覧を取得する
   * @return
   */
  public static Result getNaeOfFarm() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      //アカウント情報の取得
      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      int result = NaeStatusCompornent.getNaeOfFarmJson(accountComprtnent.accountData.farmId, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("flag"     , 0);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }

  public static Result getNaeZaikoInfo(String naeNo) {

    /* 戻り値用JSONデータの生成 */
    ObjectNode resultJson = Json.newObject();

    //----- 苗状況情報の取得 -----
    NaeStatus ns = NaeStatus.getStatusOfNae(naeNo);

    if (ns != null) {
      IkubyoDiary id = IkubyoDiary.find.where().eq("nae_no", naeNo).ne("youki_id", 0).findUnique();

      if (id != null) {
        Youki y = Youki.getYoukiInfo(id.youkiId);
        resultJson.put("zaikoSuryo", ns.zaikoSuryo);
        resultJson.put("zaikoKosu" , ns.kosu);
        resultJson.put("unitKind"  , y.unitKind);
        resultJson.put("kosu"      , y.kosu);
        resultJson.put("result"    , AgryeelConst.Result.SUCCESS);
      }
      else {
        resultJson.put("result"    , AgryeelConst.Result.NOTFOUND);
      }
    }
    else {
      resultJson.put("result"      , AgryeelConst.Result.NOTFOUND);
    }

    return ok(resultJson);
  }

  /**
   * 苗Noから苗情報一覧を取得する
   * @return
   */
  public static Result getNaeInfoList(String naeNo) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson = Json.newObject();
      ObjectNode listJson = Json.newObject();

      int result = NaeStatusCompornent.getNaeInfoJson(naeNo, listJson);

      resultJson.put("datalist" , listJson);
      resultJson.put("result"   , "SUCCESS");

      return ok(resultJson);
  }

}
