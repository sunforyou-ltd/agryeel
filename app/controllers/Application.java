package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Account;
import models.Common;
import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Crop;
import models.Farm;
import models.FarmStatus;
import models.Field;
import models.FieldGroup;
import models.FieldGroupList;
import models.Hinsyu;
import models.MessageOfAccount;
import models.MotochoBase;
import models.MotochoHiryo;
import models.MotochoNouyaku;
import models.Nouhi;
import models.Pest;
import models.PosttoPoint;
import models.Sequence;
import models.SystemMessage;
import models.TimeLine;
import models.Weather;
import models.Work;
import models.WorkChain;
import models.WorkChainItem;
import models.WorkCompartment;
import models.WorkDiary;
import models.WorkDiarySanpu;

import org.apache.commons.lang3.time.DateUtils;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.DateU;
import util.ListrU;
import util.MathU;
import util.StringU;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.CompartmentStatusCompornent;
import compornent.CropComprtnent;
import compornent.FieldComprtnent;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;
import compornent.WorkChainCompornent;

import consts.AgryeelConst;

public class Application extends Controller {

    /**
     * 【AGRYEEL】トップページ
     * @return インデックスレンダー
     */
    public static Result getMotocho() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */
        JsonNode motochoInput 				= request().body().asJson();												/* 元帳照会パラメータ */
        ObjectNode 	listJson  				= Json.newObject();															/* リストJSONオブジェクト */
        ObjectNode 	histryListJson  		= Json.newObject();															/* 作付リストJSONオブジェクト */
        DecimalFormat dfWorkYear			= new DecimalFormat("0000");												/* 作業年フォーマット */
        DecimalFormat dfrotationSpeedOfYear	= new DecimalFormat("0000");												/* 年内回転数フォーマット */
        DecimalFormat dfnpk					= new DecimalFormat("0.000");												/* NPKフォーマット */
        String		sInitKey				= "";																		/* 初回表示キー */
        SimpleDateFormat sdf				= new SimpleDateFormat("MM.dd HH:mm");

    	Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
    	Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

        /* 区画IDで元帳情報を表示する */
        List<MotochoBase> motochoBaseList = MotochoBase.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
                                                       .orderBy().desc("work_year").orderBy().desc("rotation_speed_of_year").findList();

        if (motochoBaseList.size() > 0) { 																				/* 元帳基本情報が存在する場合 */
            Compartment compartmentData = Compartment.find.where()
                            .eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
                            .eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();	/* 区画情報を取得する */

            if (compartmentData == null ) { return notFound(); }														/* 区画情報が存在しない場合 */

            //【AICA】TODO:圃場/区画の見直し
//            CompartmentGroup compartmentGroupData = CompartmentGroup.find.where()
//                    .eq("kukaku_group_id", compartmentData.kukakuGroupId).findUnique();									/* 区画グループ情報を取得する */

            /*-------------------------------------------------------------------------------------------------------*/
            /*- 元帳基本情報の生成                                                                                  -*/
            /*-------------------------------------------------------------------------------------------------------*/
            for (MotochoBase motochobaseData : motochoBaseList) {														/* 元帳基本情報分処理を行う */


                ObjectNode 	motochobaseJson  = Json.newObject();														/* 元帳基本情報JSONオブジェクトの生成 */
                ObjectNode 	histryJson  	 = Json.newObject();														/* 元帳履歴JSONオブジェクトの生成 */

                motochobaseJson.put("workYear", motochobaseData.workYear);												/* 作業年 */
                motochobaseJson.put("rotationSpeedOfYear", motochobaseData.rotationSpeedOfYear);						/* 年内回転数 */
                motochobaseJson.put("kukakuName", motochobaseData.kukakuName);											/* 区画名 */
                motochobaseJson.put("cropName", StringU.setNullTrim(motochobaseData.cropName));							/* 生産物 */
                motochobaseJson.put("hinsyuName", StringU.setNullTrim(motochobaseData.hinsyuName));						/* 品種名 */
                motochobaseJson.put("hashuDate", StringU.dateFormat("yyyy/MM/dd", motochobaseData.hashuDate));			/* 播種日 */

                /*----- 元帳履歴データ作成 -----*/
                String key 	= dfWorkYear.format(motochobaseData.workYear)
                            + dfrotationSpeedOfYear.format(motochobaseData.rotationSpeedOfYear);						/* KEYを設定 */
                histryJson.put("key"	, key);
                histryJson.put("display", "作付年：" + motochobaseData.workYear + "年 回転数：" + motochobaseData.rotationSpeedOfYear + "作");
                histryListJson.put(key, histryJson);

                long seiikuDayCount = 0;

                if (motochobaseData.hashuDate != null) {
                	Date hashuDate = DateUtils.truncate(motochobaseData.hashuDate, Calendar.DAY_OF_MONTH);
                    if (hashuDate.compareTo(nullDate) != 0) {
                        if (motochobaseData.shukakuStartDate != null) {
                        	Date syukakuStartDate = DateUtils.truncate(motochobaseData.shukakuStartDate, Calendar.DAY_OF_MONTH);
                            if (syukakuStartDate.compareTo(nullDate) != 0) {
                            	seiikuDayCount = DateU.GetDiffDate(hashuDate, syukakuStartDate);
                            }
                            else {
                            	seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
                            }
                        }
                        else {
                        	seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
                        }
                    }
                }

                motochobaseJson.put("seiikuDayCount", seiikuDayCount);													/* 生育日数 */
                motochobaseJson.put("shukakuRyo", motochobaseData.totalShukakuNumber);					/* 収穫量 */
                motochobaseJson.put("shukakuStartDate"
                                    , StringU.dateFormat("yyyy/MM/dd", motochobaseData.shukakuStartDate));				/* 収穫開始日 */
                motochobaseJson.put("shukakuEndDate", StringU.dateFormat("yyyy/MM/dd", motochobaseData.shukakuEndDate));/* 収穫終了日 */

                //【AICA】TODO:圃場/区画の見直し
//                motochobaseJson.put("kukakuGroupName", compartmentGroupData.kukakuGroupName.substring(0, 1));			/* 区画グループ名 */
//                motochobaseJson.put("kukakuGroupColor", compartmentGroupData.kukakuGroupColor);							/* 区画グループカラー */

                /*----------------------------------------------------------------------------------------------------*/
                /*- 元帳農薬情報の生成                                                                               -*/
                /*----------------------------------------------------------------------------------------------------*/
                List<MotochoNouyaku> motochoNouyakuList = MotochoNouyaku.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
                                                               .eq("work_year", motochobaseData.workYear).eq("rotation_speed_of_year", motochobaseData.rotationSpeedOfYear)
                                                               .orderBy("sanpu_date asc, nouyaku_no asc").findList();

                ObjectNode 	nouyakuListJson 			= Json.newObject();												/* 農薬リストJSONオブジェクト */

                for (MotochoNouyaku motochoNouyakuData : motochoNouyakuList) {											/* 農薬リスト分処理を行う */
                    ObjectNode 	motochoNouyakuJson  = Json.newObject();													/* 元帳農薬情報JSONオブジェクトの生成 */
                    motochoNouyakuJson.put("nouyakuNo", "殺虫" + motochoNouyakuData.nouyakuNo);							/* 農薬番号 */

                    motochoNouyakuJson.put("nouhiName", motochoNouyakuData.nouhiName);									/* 農肥名 */
                    motochoNouyakuJson.put("bairitu", motochoNouyakuData.bairitu);										/* 倍率 */
                    motochoNouyakuJson.put("sanpuDate", StringU.dateFormat("yyyy/MM/dd", motochoNouyakuData.sanpuDate));/* 散布日 */
                    motochoNouyakuJson.put("sanpuryo", motochoNouyakuData.sanpuryo);									/* 散布量 */
                    motochoNouyakuJson.put("sanpuMethod"
                            , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motochoNouyakuData.sanpuMethod));	/* 散布方法 */
                    motochoNouyakuJson.put("yushiSeibun", motochoNouyakuData.yushiSeibun);								/* 有支成分 */
                    motochoNouyakuJson.put("gUnitValue", motochoNouyakuData.gUnitValue);								/* 10ｇ当り */
                    motochoNouyakuJson.put("diffDay"   , DateU.GetDiffDate(motochoNouyakuData.sanpuDate, systemDate));	/* 経過日数 */


                    motochoNouyakuJson.put("n", dfnpk.format(motochoNouyakuData.n));									/* N */
                    motochoNouyakuJson.put("p", dfnpk.format(motochoNouyakuData.p));									/* P */
                    motochoNouyakuJson.put("k", dfnpk.format(motochoNouyakuData.k));									/* K */
                    motochoNouyakuJson.put("unit", motochoNouyakuData.unit);											/* 単位 */

                    nouyakuListJson.put(Double.toString(motochoNouyakuData.nouyakuNo) + StringU.dateFormat("yyyy/MM/dd"
                    								, motochoNouyakuData.sanpuDate), motochoNouyakuJson);				/* 農薬リストに格納 */


                }

                motochobaseJson.put("nouyakuList", nouyakuListJson);													/* 農薬リストを格納 */

                /*----------------------------------------------------------------------------------------------------*/
                /*- 元帳肥料情報の生成                                                                               -*/
                /*----------------------------------------------------------------------------------------------------*/
                List<MotochoHiryo> motochoHiryoList = MotochoHiryo.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
                                                               .eq("work_year", motochobaseData.workYear).eq("rotation_speed_of_year", motochobaseData.rotationSpeedOfYear)
                                                               .orderBy("sanpu_date asc, hiryo_no asc").findList();

                ObjectNode 	hiryoListJson 			= Json.newObject();													/* 肥料リストJSONオブジェクト */

                for (MotochoHiryo motochoHiryoData : motochoHiryoList) {												/* 肥料リスト分処理を行う */
                    ObjectNode 	motochoHiryoJson  = Json.newObject();													/* 元帳肥料情報JSONオブジェクトの生成 */
                    motochoHiryoJson.put("nouyakuNo", "元肥" + motochoHiryoData.hiryoNo);								/* 農薬番号 */

                    motochoHiryoJson.put("nouhiName", motochoHiryoData.nouhiName);										/* 農肥名 */
                    motochoHiryoJson.put("bairitu", motochoHiryoData.bairitu);											/* 倍率 */
                    motochoHiryoJson.put("sanpuDate", StringU.dateFormat("yyyy/MM/dd", motochoHiryoData.sanpuDate));	/* 散布日 */
                    motochoHiryoJson.put("sanpuryo", motochoHiryoData.sanpuryo);										/* 散布量 */
                    motochoHiryoJson.put("sanpuMethod"
                            , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motochoHiryoData.sanpuMethod));		/* 散布方法 */
                    motochoHiryoJson.put("yushiSeibun", motochoHiryoData.yushiSeibun);									/* 有支成分 */
                    motochoHiryoJson.put("gUnitValue", motochoHiryoData.gUnitValue);									/* 10ｇ当り */
                    motochoHiryoJson.put("diffDay"   , DateU.GetDiffDate(motochoHiryoData.sanpuDate, systemDate));		/* 経過日数 */

                    motochoHiryoJson.put("n", dfnpk.format(motochoHiryoData.n));										/* N */
                    motochoHiryoJson.put("p", dfnpk.format(motochoHiryoData.p));										/* P */
                    motochoHiryoJson.put("k", dfnpk.format(motochoHiryoData.k));										/* K */
                    motochoHiryoJson.put("unit", motochoHiryoData.unit);												/* 単位 */

                    hiryoListJson.put(Double.toString(motochoHiryoData.hiryoNo) + StringU.dateFormat("yyyy/MM/dd", motochoHiryoData.sanpuDate)
                    														, motochoHiryoJson);						/* 肥料リストに格納 */

                }

                motochobaseJson.put("hiryoList", hiryoListJson);														/* 肥料リストを格納 */


                listJson.put(dfWorkYear.format(motochobaseData.workYear)
                        + dfrotationSpeedOfYear.format(motochobaseData.rotationSpeedOfYear)	, motochobaseJson);

                if (sInitKey.equals("")) {																				/* 初回表示KEYが未設定の場合 */
                    sInitKey = dfWorkYear.format(motochobaseData.workYear)
                            + dfrotationSpeedOfYear.format(motochobaseData.rotationSpeedOfYear);						/* 初回表示KEYを設定 */
                }


                /*----------------------------------------------------------------------------------------------------*/
                /*- タイムラインを取得する                                                                           -*/
                /*----------------------------------------------------------------------------------------------------*/
                List<TimeLine> timeLineList		= TimeLine.getTimeLineOfRange(motochobaseData.kukakuId, motochobaseData.workStartDay, motochobaseData.workEndDay);
                List<TimeLine> teisyokuList		= new ArrayList<TimeLine>();
                List<TimeLine> timeLineList2	= new ArrayList<TimeLine>();
                Date oldDate = null;
                SimpleDateFormat sdf2			= new SimpleDateFormat("MM/dd");

                ObjectNode timeLineJson			= Json.newObject();

                for (TimeLine timeLineData : timeLineList) {
                	if (timeLineData.workId == AgryeelConst.WorkInfo.TEISYOKU) {
                		teisyokuList.add(timeLineData);
                		oldDate = timeLineData.workDate;
                		continue;
                	}
                	if ((oldDate != null) && (timeLineData.workDate.compareTo(oldDate) != 0) || timeLineData.workId <= AgryeelConst.WorkInfo.HASHU) {
                        for (TimeLine teisyokuLineData : teisyokuList) {
                        	timeLineList2.add(teisyokuLineData);
                        }
                        teisyokuList.clear();
                	}
                	timeLineList2.add(timeLineData);
                }
            	if (teisyokuList.size() > 0) {
                    for (TimeLine teisyokuLineData : teisyokuList) {
                    	timeLineList2.add(teisyokuLineData);
                    }
            	}

                for (TimeLine timeLineData : timeLineList2) {

                    ObjectNode motochoTimeLineJson			= Json.newObject();
                	WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

            		if (motochobaseData.workStartDay.compareTo(workd.workDate) == 0) {
            			switch ((int)workd.workId) {
						case AgryeelConst.WorkInfo.KATADUKE:
						case AgryeelConst.WorkInfo.DOJOKONSYODOKU:
						case AgryeelConst.WorkInfo.DOJOKONSYODOKU2:
						case AgryeelConst.WorkInfo.HIRYOSANPU:
						case AgryeelConst.WorkInfo.TAGAYASU:
						case AgryeelConst.WorkInfo.HASHU:
						case AgryeelConst.WorkInfo.TEISYOKU:
						case AgryeelConst.WorkInfo.JOSOZAISANPU:

	            			break;

						default:
	            			continue;
						}
            		}
//            		if ((motochobaseData.motochoFlag != AgryeelConst.Motocho.MOTOCHOFLAGEND) && motochobaseData.workEndDay.compareTo(workd.workDate) == 0) {
//            			switch ((int)workd.workId) {
//						case AgryeelConst.WorkInfo.KATADUKE:
//						case AgryeelConst.WorkInfo.DOJOKONSYODOKU:
//						case AgryeelConst.WorkInfo.DOJOKONSYODOKU2:
//						case AgryeelConst.WorkInfo.HIRYOSANPU:
//						case AgryeelConst.WorkInfo.TAGAYASU:
//						case AgryeelConst.WorkInfo.HASHU:
//						case AgryeelConst.WorkInfo.TEISYOKU:
//						case AgryeelConst.WorkInfo.JOSOZAISANPU:
//
//	            			continue;
//
//						default:
//	            			break;
//						}
//            		}

                    motochoTimeLineJson.put("workId", timeLineData.workId);
                    motochoTimeLineJson.put("workName", timeLineData.workName);
                    motochoTimeLineJson.put("timeLineColor", timeLineData.timeLineColor);
                    motochoTimeLineJson.put("accountName", timeLineData.accountName);
                    motochoTimeLineJson.put("message", timeLineData.message);
                    motochoTimeLineJson.put("updateTime"	, sdf.format(timeLineData.updateTime));

                    motochoTimeLineJson.put("workdate"		, sdf2.format(workd.workDate));
                    motochoTimeLineJson.put("worktime"		, workd.workTime);
                    motochoTimeLineJson.put("workDiaryId"	, workd.workDiaryId);							//ＩＤ

                    motochoTimeLineJson.put("kukakuName"	, timeLineData.kukakuName);						//区画名

                    timeLineJson.put(Double.toString(timeLineData.timeLineId), motochoTimeLineJson);

                }

                motochobaseJson.put("timeLineList", timeLineJson);														/* タイムラインリストを格納 */

            }

        }
        else {

            return notFound();																							/* 元帳基本情報が存在しない場合 */

        }

        resultJson.put(AgryeelConst.Motocho.MOTOCHOBASE	, listJson);
        resultJson.put(AgryeelConst.Motocho.MOTOCHOYEAR	, histryListJson);
        resultJson.put(AgryeelConst.Motocho.INITKEY		, sInitKey);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】ユーザログアウト
     * @return ログアウト結果
     */
    public static Result userLogout() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        Account account = accountComprtnent.accountData;
        if (account != null) {
          account.hashValue = "";
          account.update();
        }

        Logger.info("[ LOGOUT ] ID:{} NAME:{}", account.accountId, account.acountName);
        SessionCheckComponent.unregisterLoginSession(ctx());

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】プッシュ通知機能
     * @return プッシュ通知結果
     */
    public static Result getPushUpdate() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        Sequence sequence 		= Sequence.GetSequenceNowValue(Sequence.SequenceIdConst.TIMELINEID);
        double oldSequenceValue	= sequence.sequenceValue;

        while (oldSequenceValue	== sequence.sequenceValue) {
            try {

                Thread.sleep(1000);

            } catch (InterruptedException e) {

            }
            sequence 		= Sequence.GetSequenceNowValue(Sequence.SequenceIdConst.TIMELINEID);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】メニュー画面遷移
     * @return メニュー画面レンダー
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result menuMove() {
        return ok(views.html.menu.render(""));
    }

    /**
     * 【AGRYEEL】ハウス状況照会を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getCompartment() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();
        ObjectNode 	listGJson  = Json.newObject();
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode listJsonApi = mapper.createArrayNode();
        ArrayNode listGJsonApi = mapper.createArrayNode();
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

        CompartmentStatusCompornent csc = new CompartmentStatusCompornent();
        csc.getAllData(accountComprtnent.accountData.farmId);

        if (StringU.nullcheck(accountComprtnent.accountStatusData.selectFieldGroupId)) {
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
        }
        else {
          //----- 検索条件の整備 -----
          //----- 区画選択 -----
          List<Double> aryMultiKukaku = new ArrayList<Double>();
          if (!StringU.nullcheck(accountComprtnent.accountStatusData.sskMultiKukaku)) { //指定あり
            String[] sMultiKukakus = accountComprtnent.accountStatusData.sskMultiKukaku.split(",");
            for (String sMultiKukaku : sMultiKukakus) {
              aryMultiKukaku.add(Double.parseDouble(sMultiKukaku));
            }
          }
          //----- 品種選択 -----
          List<Double> aryHinsyu = new ArrayList<Double>();
          if (!StringU.nullcheck(accountComprtnent.accountStatusData.sskHinsyu)) { //指定あり
            String[] sHinsyus = accountComprtnent.accountStatusData.sskHinsyu.split(",");
            for (String sHinsyu : sHinsyus) {
              aryHinsyu.add(Double.parseDouble(sHinsyu));
            }
          }
          String[] fgws;
          for (int idx=0; idx<2; idx++) {
            if (idx == 0) {
              fgws = accountComprtnent.accountStatusData.selectFieldGroupId.split(",");
            }
            else {
              fgws = FieldGroup.getFieldGroupIdOfFarm(accountComprtnent.accountData.farmId);
            }
            for (String fgw : fgws) {
                double fgi = Double.parseDouble(fgw);
                FieldGroup fg = FieldComprtnent.getFieldGroup(fgi);
                if (fg != null) {
                  if (idx == 0 && fg.deleteFlag == 1) { // 削除済みの場合
                    continue;
                  }
                  List<Field> fields = FieldComprtnent.getField(fgi);
                  for (Field field : fields) {
                    if (idx == 0 && field.deleteFlag == 1) { // 削除済みの場合
                      continue;
                    }
                    if (idx == 1 && field.deleteFlag == 0) { // 削除済みではない場合
                      continue;
                    }

                    //区画情報を取得する
                    FieldComprtnent fc = new FieldComprtnent();
                    fc.getFileld(field.fieldId);
                    if (accountComprtnent.accountStatusData.displayStatus == AgryeelConst.DISPLAYSTATUS.FIELD) { //状況照会が圃場の場合
                      ObjectNode compartmentStatusJson  = Json.newObject();
                      FieldGroupList fgl = FieldGroupList.getFieldUnique(fg.fieldGroupId, field.fieldId);
                      if (idx == 0) {
                        compartmentStatusJson.put("fieldGroupColor"    , fg.fieldGroupColor);     //圃場グループカラー
                        compartmentStatusJson.put("fieldGroupId"       , fg.fieldGroupId);
                        compartmentStatusJson.put("fieldGroupName"     , fg.fieldGroupName);
                      }
                      else {
                        compartmentStatusJson.put("fieldGroupColor"    , "808080");              //圃場グループカラー
                        compartmentStatusJson.put("fieldGroupId"       , 9999);
                        compartmentStatusJson.put("fieldGroupName"     , "過去圃場");
                      }
                      compartmentStatusJson.put("fieldId"            , field.fieldId);          //圃場ID
                      compartmentStatusJson.put("fieldName"          , field.fieldName);        //圃場名
                      if (idx == 0) {
                        compartmentStatusJson.put("sequenceId"         , dfSeq.format(fg.sequenceId) + dfSeq.format(fgl.sequenceId) + dfSeq.format(0)); //並び順
                      }
                      else {
                        compartmentStatusJson.put("sequenceId"         , dfSeq.format(fg.sequenceId + 9000) + dfSeq.format(fgl.sequenceId) + dfSeq.format(0)); //並び順
                      }

                      listJson.put(Double.toString(field.fieldId), compartmentStatusJson);
                      listJsonApi.add(compartmentStatusJson);
                      oldCount++;
                    }
                    else { //状況照会が区画の場合
                      List<Compartment> compartments = fc.getCompartmentList();
                      for (Compartment compartmentData : compartments) {
                        if (idx == 0 && compartmentData.deleteFlag == 1) { // 削除済みの場合
                          continue;
                        }
                        if (idx == 1 && compartmentData.deleteFlag == 0) { // 削除済みではない場合
                          continue;
                        }
                        oldCount++;

                        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(compartmentData.kukakuId);
                        //----- ここで検索条件との一致チェックを行う -----
                        //----- 区画名 -----
                        if (!StringU.nullcheck(accountComprtnent.accountStatusData.sskKukakuName)) { //指定あり
                          if (compartmentData.kukakuName.indexOf(accountComprtnent.accountStatusData.sskKukakuName) == -1) { //該当文字列が存在しない場合
                            continue;
                          }
                        }
                        //----- 区画選択 -----
                        if (!ListrU.keyCheck(aryMultiKukaku, compartmentData.kukakuId)) { //該当条件に含まれない場合
                          continue;
                        }
                        //----- 品種選択 -----
                        if (!ListrU.keyChecks(aryHinsyu, compartmentStatusData.hinsyuId)) { //該当条件に含まれない場合
                          continue;
                        }
                        ObjectNode compartmentStatusJson  = Json.newObject();
                        FieldGroupList fgl = FieldGroupList.getFieldUnique(fg.fieldGroupId, field.fieldId);
                        if (idx == 0) {
                          compartmentStatusJson.put("fieldGroupColor"    , fg.fieldGroupColor);     //圃場グループカラー
                          compartmentStatusJson.put("fieldGroupId"       , fg.fieldGroupId);
                          compartmentStatusJson.put("fieldGroupName"     , fg.fieldGroupName);
                        }
                        else {
                          compartmentStatusJson.put("fieldGroupColor"    , "808080");     //圃場グループカラー
                          compartmentStatusJson.put("fieldGroupId"       , 9999);
                          compartmentStatusJson.put("fieldGroupName"     , "過去圃場");
                        }
                        compartmentStatusJson.put("fieldId"            , field.fieldId);          //圃場ID
                        compartmentStatusJson.put("fieldName"          , field.fieldName);        //圃場名
                        compartmentStatusJson.put("kukakuId"            , compartmentData.kukakuId);                    //区画ID
                        compartmentStatusJson.put("kukakuName"          , compartmentData.kukakuName);                  //区画名
                        compartmentStatusJson.put("kukakuKind"          , Common.GetCommonValue(Common.ConstClass.KUKAKUKIND, (int)compartmentData.kukakuKind));                  //区画名
                        if (idx == 0) {
                          compartmentStatusJson.put("sequenceId"          , dfSeq.format(fg.sequenceId) + dfSeq.format(0) + dfSeq.format(compartmentData.sequenceId)); //並び順
                        }
                        else {
                          compartmentStatusJson.put("sequenceId"          , dfSeq.format(fg.sequenceId + 9000) + dfSeq.format(0) + dfSeq.format(compartmentData.sequenceId)); //並び順
                        }
                        compartmentStatusJson.put("workYear"            , compartmentStatusData.workYear);              //作業年
                        compartmentStatusJson.put("rotationSpeedOfYear" , compartmentStatusData.rotationSpeedOfYear);   //年内回転数
                        compartmentStatusJson.put("hashuCount"          , compartmentStatusData.hashuCount);            //播種回数
                        long seiikuDayCount     = 0;
                        long seiikuDayCountEnd  = 0;
                        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //播種日

                          Date hashuDate = DateUtils.truncate(compartmentStatusData.hashuDate, Calendar.DAY_OF_MONTH);
                          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

                          compartmentStatusJson.put("hashuDate"       , compartmentStatusData.hashuDate.toString());

                          //生育日数の表示方法を変更
//                          if (compartmentStatusData.seiikuDayCount != 0) {
//                            seiikuDayCount = compartmentStatusData.seiikuDayCount;
//                          }
//                          else {
//                            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//                          }
                          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {  //収穫開始日
                            seiikuDayCount = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuStartDate);
                          }
                          else {
                            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
                          }
                          if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {  //収穫開始日
                            seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuEndDate);
                          }
                          else {
                            seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, systemDate);
                          }
                        }
                        else {
                          compartmentStatusJson.put("hashuDate"       , "");
                          seiikuDayCount = 0;
                          seiikuDayCountEnd = 0;
                        }
                        //----- ここで生育日数との一致チェックを行う -----
                        long nisuf = 0;
                        long nisut = 9999;
                        if (accountComprtnent.accountStatusData.sskSeiikuF != 0) { //指定あり
                          nisuf = accountComprtnent.accountStatusData.sskSeiikuF;
                        }
                        if (accountComprtnent.accountStatusData.sskSeiikuT != 0) { //指定あり
                          nisut = accountComprtnent.accountStatusData.sskSeiikuT;
                        }

                        if (!(nisuf <= seiikuDayCount && seiikuDayCount <= nisut)) { //指定範囲以外の場合
                          continue;
                        }

                        compartmentStatusJson.put("seiikuDayCount"    , seiikuDayCount);                                //生育日数
                        compartmentStatusJson.put("seiikuDayCountEnd" , seiikuDayCountEnd);                             //生育日数(収穫終了日)

                        Crop cp = CropComprtnent.getCropById(compartmentStatusData.cropId);
                        if (cp != null) {                                                                               //生産物名称
                          compartmentStatusJson.put("cropName"    , cp.cropName);
                        }
                        else {
                          compartmentStatusJson.put("cropName"    , "");
                        }

                        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

                            compartmentStatusJson.put("hinsyuName"        , compartmentStatusData.hinsyuName);            //品種名

                        }
                        else {

                            compartmentStatusJson.put("hinsyuName"        , "");                                          //品種名

                        }

                        DecimalFormat df = new DecimalFormat("#,##0.0");
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
                        SimpleDateFormat sdfL = new SimpleDateFormat("yyyy/MM/dd");
                        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- 積算温度
                        //----------------------------------------------------------------------------------------------------------------------------------
                        compartmentStatusJson.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));
                        double totalDatas = 0;
                        Weather oWeather      = null;
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
                        if (oWeather != null) {
                          compartmentStatusJson.put("yosokuSolarDate"      , sdf.format(oWeather.dayDate));
                        }
                        else {
                          compartmentStatusJson.put("yosokuSolarDate"      , "");
                        }
                        compartmentStatusJson.put("yosokuSolarRadiation"   , df.format(totalDatas));
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- 積算降水量
                        //----------------------------------------------------------------------------------------------------------------------------------
                        double totalDatar = 0;
                        oWeather      = null;
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

                                Calendar cal = Calendar.getInstance();
                                DateU.setTime(cal, DateU.TimeType.TO);
                                endDate = new java.sql.Date(cal.getTime().getTime());

                                List<Weather> weathers = Weather.getWeather(pointId, compartmentStatusData.hashuDate, endDate);

                                for (Weather weather : weathers) {
                                  if (weather.jituyo == AgryeelConst.JITUYO.JISEKI) {
                                    totalDatar += weather.rain;
                                    oWeather = weather;
                                  }
                                }
                              }
                            }
                          }
                        }
                        compartmentStatusJson.put("rain"   , df.format(totalDatar));
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- 消毒
                        //----------------------------------------------------------------------------------------------------------------------------------
                        long disinfectionCount = 0;
                        if (compartmentStatusData.finalDisinfectionDate != null && (compartmentStatusData.finalDisinfectionDate.compareTo(nullDate) != 0)) {

                          Date finalDisinfectionDate = DateUtils.truncate(compartmentStatusData.finalDisinfectionDate, Calendar.DAY_OF_MONTH);

                          compartmentStatusJson.put("finalDisinfectionDate"       , compartmentStatusData.finalDisinfectionDate.toString());

                          disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, systemDate);

                        }
                        else {
                          compartmentStatusJson.put("finalDisinfectionDate"       , "");
                          disinfectionCount = 0;
                        }

                        compartmentStatusJson.put("disinfectionCount"          , disinfectionCount);                                               //最終消毒日からの経過日数
                        compartmentStatusJson.put("totalDisinfectionNumber"    , df.format(compartmentStatusData.totalDisinfectionNumber * 0.001));//合計消毒量
                        compartmentStatusJson.put("totalDisinfectionCount"     , compartmentStatusData.totalDisinfectionCount);                   //合計消毒回数
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- 潅水量
                        //----------------------------------------------------------------------------------------------------------------------------------
                        long kansuiCount = 0;
                        if (compartmentStatusData.finalKansuiDate != null && (compartmentStatusData.finalKansuiDate.compareTo(nullDate) != 0)) {

                          Date finalKansuiDate = DateUtils.truncate(compartmentStatusData.finalKansuiDate, Calendar.DAY_OF_MONTH);

                          compartmentStatusJson.put("finalKansuiDate"       , compartmentStatusData.finalKansuiDate.toString());

                          kansuiCount = DateU.GetDiffDate(finalKansuiDate, systemDate);

                        }
                        else {
                          compartmentStatusJson.put("finalKansuiDate"       , "");
                          kansuiCount = 0;
                        }

                        compartmentStatusJson.put("kansuiCount"          , kansuiCount);                                       //最終潅水日からの経過日数
                        compartmentStatusJson.put("totalKansuiNumber"    , df.format(compartmentStatusData.totalKansuiNumber));//合計潅水量
                        compartmentStatusJson.put("totalKansuiCount"     , compartmentStatusData.totalKansuiCount);            //合計潅水回数
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- 追肥
                        //----------------------------------------------------------------------------------------------------------------------------------
                        long tuihiCount = 0;
                        if (compartmentStatusData.finalTuihiDate != null && (compartmentStatusData.finalTuihiDate.compareTo(nullDate) != 0)) {

                          Date finalTuihiDate = DateUtils.truncate(compartmentStatusData.finalTuihiDate, Calendar.DAY_OF_MONTH);

                          compartmentStatusJson.put("finalTuihiDate"       , compartmentStatusData.finalTuihiDate.toString());

                          tuihiCount = DateU.GetDiffDate(finalTuihiDate, systemDate);

                        }
                        else {
                          compartmentStatusJson.put("finalTuihiDate"       , "");
                          tuihiCount = 0;
                        }

                        compartmentStatusJson.put("tuihiCount"          , tuihiCount);                                       //最終追肥日からの経過日数
                        compartmentStatusJson.put("totalTuihiNumber"    , df.format(compartmentStatusData.totalTuihiNumber * 0.001));//合計追肥量
                        compartmentStatusJson.put("totalTuihiCount"     , compartmentStatusData.totalTuihiCount);            //合計追肥回数
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- 収穫
                        //----------------------------------------------------------------------------------------------------------------------------------
                        if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {

                          compartmentStatusJson.put("shukakuStartDate"       , compartmentStatusData.shukakuStartDate.toString());

                        }
                        else {
                          compartmentStatusJson.put("shukakuStartDate"       , "");
                        }
                        if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {

                          compartmentStatusJson.put("shukakuEndDate"       , compartmentStatusData.shukakuEndDate.toString());

                        }
                        else {
                          compartmentStatusJson.put("shukakuEndDate"       , "");
                        }

                        compartmentStatusJson.put("totalShukakuNumber"    , df.format(compartmentStatusData.totalShukakuCount));        //合計収穫量
                        if ((compartmentStatusData.totalShukakuCount == 0)
                            || (compartmentData.area == 0)) {
                          compartmentStatusJson.put("tanshu"              , "*****");
                        }
                        else {
                          compartmentStatusJson.put("tanshu"              , df.format((compartmentStatusData.totalShukakuCount / compartmentData.area) * 10));
                        }

                        if(api){
                          ArrayNode chainJson = mapper.createArrayNode();
                          csc.getWorkChainStatusJsonArray(compartmentData.kukakuId, chainJson);
                          compartmentStatusJson.put("chain", chainJson);

                          ArrayNode  aj   = mapper.createArrayNode();
                          UserComprtnent uc = new UserComprtnent();
                          uc.getNowWorkingByFieldArray(field.fieldId, aj);
                          compartmentStatusJson.put("working", aj);
                        }else{
                          ObjectNode chainJson = Json.newObject();
                          csc.getWorkChainStatusJson(compartmentData.kukakuId, chainJson);
                          compartmentStatusJson.put("chain", chainJson);

                          ObjectNode  aj   = Json.newObject();                                                              //作業中アカウント情報
                          UserComprtnent uc = new UserComprtnent();
                          uc.getNowWorkingByField(field.fieldId, aj);
                          compartmentStatusJson.put("working", aj);
                        }

                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- 害虫情報
                        //----------------------------------------------------------------------------------------------------------------------------------
                        String on = "";
                        double pestId   = 0;
                        String pestName = "";
                        String pestPredictDate = "";
                        long diffPredictDate = 0;
                        Calendar datacal = Calendar.getInstance();
                        datacal.setTime(compartmentStatusData.pestPredictDate);
                        if (syscal.compareTo(datacal) <= 0) {
                          pestId = compartmentStatusData.pestId;
                          Pest pest = Pest.getPestGeneration(pestId, compartmentStatusData.pestGeneration);
                          if (pest != null) {
                            pestName = pest.pestName;
                          }
                          pestPredictDate = sdfL.format(compartmentStatusData.pestPredictDate);
                          diffPredictDate = DateU.GetDiffDate(syscal.getTime(), datacal.getTime());
                          if (diffPredictDate <= 7) {
                            on = "on";
                          }
                        }
                        compartmentStatusJson.put("pestId"              , pestId);
                        compartmentStatusJson.put("pestName"            , pestName);
                        compartmentStatusJson.put("pestPredictDate"     , pestPredictDate);
                        compartmentStatusJson.put("pestOn"              , on);

                        listJson.put(Double.toString(compartmentData.kukakuId), compartmentStatusJson);
                        listJsonApi.add(compartmentStatusJson);
                        count++;
                      }
                    }
                  }

                  if (idx == 0) {
                    ObjectNode compartmentGroupJson = Json.newObject();
                    compartmentGroupJson.put("kukakuId"       , fg.fieldGroupId);
                    compartmentGroupJson.put("kukakuName"     , fg.fieldGroupName);
                    compartmentGroupJson.put("kukakuColor"    , fg.fieldGroupColor);
                    listGJson.put(Double.toString(fg.fieldGroupId), compartmentGroupJson);
                    listGJsonApi.add(compartmentGroupJson);
                  }

                }
            }
          }
          if (oldCount >= 0) {
              ObjectNode compartmentGroupJson = Json.newObject();
              compartmentGroupJson.put("kukakuId"       , 9999);
              compartmentGroupJson.put("kukakuName"     , "過去圃場");
              compartmentGroupJson.put("kukakuColor"    , "808080");
              listGJson.put(Double.toString(9999), compartmentGroupJson);
              listGJsonApi.add(compartmentGroupJson);
          }
          resultJson.put("displaystatus", accountComprtnent.accountStatusData.displayStatus);
          resultJson.put("displayChain", accountComprtnent.accountStatusData.displayChain);
          if(api){
            resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTGROUP, listGJsonApi);
            resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJsonApi);
          }else{
            resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTGROUP, listGJson);
            resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJson);
          }
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        }

        return ok(resultJson);
    }
    public static Result getKukakuStatus(double kukakuId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();
        ObjectNode  listJson   = Json.newObject();
        ObjectNode  listGJson  = Json.newObject();
        boolean     target     = false;
        Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);

        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        CompartmentStatusCompornent csc = new CompartmentStatusCompornent();
        csc.getAllData(accountComprtnent.accountData.farmId);

        Compartment compartmentData = Compartment.getCompartmentInfo(kukakuId);
        Field field = compartmentData.getFieldInfo();

        ObjectNode compartmentStatusJson  = Json.newObject();
        compartmentStatusJson.put("fieldId"             , field.fieldId);          //圃場ID
        compartmentStatusJson.put("fieldName"           , field.fieldName);        //圃場名
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(compartmentData.kukakuId);
        compartmentStatusJson.put("kukakuId"            , compartmentData.kukakuId);                    //区画ID
        compartmentStatusJson.put("kukakuName"          , compartmentData.kukakuName);                  //区画名
        compartmentStatusJson.put("kukakuKind"          , Common.GetCommonValue(Common.ConstClass.KUKAKUKIND, (int)compartmentData.kukakuKind));  //区画種別
        compartmentStatusJson.put("workYear"            , compartmentStatusData.workYear);              //作業年
        compartmentStatusJson.put("rotationSpeedOfYear" , compartmentStatusData.rotationSpeedOfYear);   //年内回転数
        compartmentStatusJson.put("hashuCount"          , compartmentStatusData.hashuCount);            //播種回数
        long seiikuDayCount = 0;
        long seiikuDayCountEnd  = 0;
        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //播種日

          Date hashuDate = DateUtils.truncate(compartmentStatusData.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

          compartmentStatusJson.put("hashuDate"       , compartmentStatusData.hashuDate.toString());
          //生育日数の表示方法を変更
//          if (compartmentStatusData.seiikuDayCount != 0) {
//            seiikuDayCount = compartmentStatusData.seiikuDayCount;
//          }
//          else {
//            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//          }
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {  //収穫開始日
            seiikuDayCount = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuStartDate);
          }
          else {
            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
          }
          if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {      //収穫終了日
            seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuEndDate);
          }
          else {
            seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, systemDate);
          }

        }
        else {
          compartmentStatusJson.put("hashuDate"       , "");
          seiikuDayCount    = 0;
          seiikuDayCountEnd = 0;
        }

        compartmentStatusJson.put("seiikuDayCount"    , seiikuDayCount);                                //生育日数
        compartmentStatusJson.put("seiikuDayCountEnd" , seiikuDayCountEnd);                             //生育日数(収穫終了日)

        Crop cp = CropComprtnent.getCropById(compartmentStatusData.cropId);
        if (cp != null) {                                                                               //生産物名称
          compartmentStatusJson.put("cropName"    , cp.cropName);
        }
        else {
          compartmentStatusJson.put("cropName"    , "");
        }

        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

            compartmentStatusJson.put("hinsyuName"        , compartmentStatusData.hinsyuName);            //品種名

        }
        else {

            compartmentStatusJson.put("hinsyuName"        , "");                                          //品種名

        }

        DecimalFormat df = new DecimalFormat("#,##0.0");
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 積算温度
        //----------------------------------------------------------------------------------------------------------------------------------
        compartmentStatusJson.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));
        double totalDatas = 0;
        Weather oWeather      = null;
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
        if (oWeather != null) {
          compartmentStatusJson.put("yosokuSolarDate"      , sdf.format(oWeather.dayDate));
        }
        else {
          compartmentStatusJson.put("yosokuSolarDate"      , "");
        }
        compartmentStatusJson.put("yosokuSolarRadiation"   , df.format(totalDatas));
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 積算降水量
        //----------------------------------------------------------------------------------------------------------------------------------
        double totalDatar = 0;
        oWeather      = null;
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

                Calendar cal = Calendar.getInstance();
                DateU.setTime(cal, DateU.TimeType.TO);
                endDate = new java.sql.Date(cal.getTime().getTime());

                List<Weather> weathers = Weather.getWeather(pointId, compartmentStatusData.hashuDate, endDate);

                for (Weather weather : weathers) {
                  if (weather.jituyo == AgryeelConst.JITUYO.JISEKI) {
                    totalDatar += weather.rain;
                    oWeather = weather;
                  }
                }
              }
            }
          }
        }
        compartmentStatusJson.put("rain"   , df.format(totalDatar));
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 消毒
        //----------------------------------------------------------------------------------------------------------------------------------
        long disinfectionCount = 0;
        if (compartmentStatusData.finalDisinfectionDate != null && (compartmentStatusData.finalDisinfectionDate.compareTo(nullDate) != 0)) {

          Date finalDisinfectionDate = DateUtils.truncate(compartmentStatusData.finalDisinfectionDate, Calendar.DAY_OF_MONTH);

          compartmentStatusJson.put("finalDisinfectionDate"       , compartmentStatusData.finalDisinfectionDate.toString());

          disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, systemDate);

        }
        else {
          compartmentStatusJson.put("finalDisinfectionDate"       , "");
          disinfectionCount = 0;
        }

        compartmentStatusJson.put("disinfectionCount"          , disinfectionCount);                                               //最終消毒日からの経過日数
        compartmentStatusJson.put("totalDisinfectionNumber"    , df.format(compartmentStatusData.totalDisinfectionNumber * 0.001));//合計消毒量
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 潅水量
        //----------------------------------------------------------------------------------------------------------------------------------
        long kansuiCount = 0;
        if (compartmentStatusData.finalKansuiDate != null && (compartmentStatusData.finalKansuiDate.compareTo(nullDate) != 0)) {

          Date finalKansuiDate = DateUtils.truncate(compartmentStatusData.finalKansuiDate, Calendar.DAY_OF_MONTH);

          compartmentStatusJson.put("finalKansuiDate"       , compartmentStatusData.finalKansuiDate.toString());

          kansuiCount = DateU.GetDiffDate(finalKansuiDate, systemDate);

        }
        else {
          compartmentStatusJson.put("finalKansuiDate"       , "");
          kansuiCount = 0;
        }

        compartmentStatusJson.put("kansuiCount"          , kansuiCount);                                       //最終潅水日からの経過日数
        compartmentStatusJson.put("totalKansuiNumber"    , df.format(compartmentStatusData.totalKansuiNumber));//合計潅水量
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 追肥
        //----------------------------------------------------------------------------------------------------------------------------------
        long tuihiCount = 0;
        if (compartmentStatusData.finalTuihiDate != null && (compartmentStatusData.finalTuihiDate.compareTo(nullDate) != 0)) {

          Date finalTuihiDate = DateUtils.truncate(compartmentStatusData.finalTuihiDate, Calendar.DAY_OF_MONTH);

          compartmentStatusJson.put("finalTuihiDate"       , compartmentStatusData.finalTuihiDate.toString());

          tuihiCount = DateU.GetDiffDate(finalTuihiDate, systemDate);

        }
        else {
          compartmentStatusJson.put("finalTuihiDate"       , "");
          tuihiCount = 0;
        }

        compartmentStatusJson.put("tuihiCount"          , tuihiCount);                                       //最終追肥日からの経過日数
        compartmentStatusJson.put("totalTuihiNumber"    , df.format(compartmentStatusData.totalTuihiNumber * 0.001));//合計追肥量
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 収穫
        //----------------------------------------------------------------------------------------------------------------------------------
        if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {

          compartmentStatusJson.put("shukakuStartDate"       , compartmentStatusData.shukakuStartDate.toString());

        }
        else {
          compartmentStatusJson.put("shukakuStartDate"       , "");
        }
        if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {

          compartmentStatusJson.put("shukakuEndDate"       , compartmentStatusData.shukakuEndDate.toString());

        }
        else {
          compartmentStatusJson.put("shukakuEndDate"       , "");
        }

        compartmentStatusJson.put("totalShukakuNumber"    , df.format(compartmentStatusData.totalShukakuCount));        //合計収穫量
        if ((compartmentStatusData.totalShukakuCount == 0)
            || (compartmentData.area == 0)) {
          compartmentStatusJson.put("tanshu"              , "*****");
        }
        else {
          compartmentStatusJson.put("tanshu"              , df.format((compartmentStatusData.totalShukakuCount / compartmentData.area) * 10));
        }

        ObjectNode chainJson = Json.newObject();
        csc.getWorkChainStatusJson(compartmentData.kukakuId, chainJson);
        compartmentStatusJson.put("chain", chainJson);

        ObjectNode  aj   = Json.newObject();                                                              //作業中アカウント情報
        UserComprtnent uc = new UserComprtnent();
        uc.getNowWorkingByField(compartmentData.kukakuId, aj);
        compartmentStatusJson.put("working", aj);
        resultJson.put("status", compartmentStatusJson);
        resultJson.put("displayChain", accountComprtnent.accountStatusData.displayChain);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】ハウス状況照会を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getCompartmentDisplay() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();
        long seiikuDayCount = 0;																						//生育日数

        /* JSONデータを取得 */
        JsonNode inputJson = request().body().asJson();
        String	sKukakuId  = inputJson.get("kukakuId").asText();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        //圃場情報の取得
        FieldComprtnent fc = new FieldComprtnent(accountComprtnent.accountData.farmId);
        fc.getFileld(Double.parseDouble(sKukakuId));

        //区画情報の取得
        CompartmentStatus compartmentStatusData = fc.getCompartmentStatusList().get(0);
        Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);

        ObjectNode compartmentStatusJson	= Json.newObject();

        /* 区画IDから区画情報を取得する */
        Compartment compartmentData = FieldComprtnent.getCompartment(compartmentStatusData.kukakuId);
        FieldGroup fg = FieldGroupList.getFieldGroup(fc.field.fieldId);
        String cropColor = "000000";
        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

        	Hinsyu hinsyuData = Hinsyu.find.where().eq("hinsyu_name", compartmentStatusData.hinsyuName.trim()).findUnique();
        	Crop cropData = Crop.find.where().eq("crop_id", hinsyuData.cropId).findUnique();
        	if (cropData != null) {
                cropColor = cropData.cropColor;
        	}

            compartmentStatusJson.put("hinsyuName"				, compartmentStatusData.hinsyuName);						//品種名
            compartmentStatusJson.put("hinsyuColor"				, cropColor);												//品種カラー

        }
        else {

            compartmentStatusJson.put("hinsyuName"				, "");														//品種名
            compartmentStatusJson.put("hinsyuColor"				, cropColor);												//品種カラー

        }

        compartmentStatusJson.put("kukakuGroupColor"		, "FFFFFF");												//圃場グループカラーを初期化
        if (fg != null) {
          compartmentStatusJson.put("kukakuGroupColor"	, fg.fieldGroupColor);			      //圃場グループカラー
        }

        compartmentStatusJson.put("kukakuId"				    , compartmentStatusData.kukakuId);							//区画ID
        compartmentStatusJson.put("kukakuName"				  , compartmentData.kukakuName);								  //区画名
        compartmentStatusJson.put("rotationSpeedOfYear"	, compartmentStatusData.rotationSpeedOfYear);		//年内回転数
        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //播種日

          Date hashuDate = DateUtils.truncate(compartmentStatusData.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

          compartmentStatusJson.put("hashuDate"       , compartmentStatusData.hashuDate.toString());

          if (compartmentStatusData.seiikuDayCount != 0) {
            seiikuDayCount = compartmentStatusData.seiikuDayCount;
          }
          else {
            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
          }
        }
        else {
          compartmentStatusJson.put("hashuDate"       , "");
          seiikuDayCount = 0;
        }

        compartmentStatusJson.put("seiikuDayCount"		, seiikuDayCount);											//生育日数
        compartmentStatusJson.put("nowEndWork"				, compartmentStatusData.nowEndWork);		//現在完了作業
        compartmentStatusJson.put("workColor"				  , compartmentStatusData.workColor);			//作業カラー
        if (compartmentStatusData.finalDisinfectionDate != null) {													//最終消毒日
            compartmentStatusJson.put("finalDisinfectionDate"	, compartmentStatusData.finalDisinfectionDate.toString());
        }
        else {
            compartmentStatusJson.put("finalDisinfectionDate"	, "");
        }
        if (compartmentStatusData.finalKansuiDate != null) {															//最終潅水日
            compartmentStatusJson.put("finalKansuiDate"			, compartmentStatusData.finalKansuiDate.toString());
        }
        else {
            compartmentStatusJson.put("finalKansuiDate"			, "");
        }
        if (compartmentStatusData.finalTuihiDate != null) {																//最終追肥日
            compartmentStatusJson.put("finalTuihiDate"			, compartmentStatusData.finalTuihiDate.toString());
        }
        else {
            compartmentStatusJson.put("finalTuihiDate"			, "");
        }
        if (compartmentStatusData.shukakuStartDate != null) {															//収穫開始日
            compartmentStatusJson.put("shukakuStartDate"		, compartmentStatusData.shukakuStartDate.toString());
        }
        else {
            compartmentStatusJson.put("shukakuStartDate"		, "");
        }
        if (compartmentStatusData.shukakuEndDate != null) {																//収穫終了日
            compartmentStatusJson.put("shukakuEndDate"			, compartmentStatusData.shukakuEndDate.toString());
        }
        else {
            compartmentStatusJson.put("shukakuEndDate"			, "");
        }
        compartmentStatusJson.put("totalSolarRadiation"     , compartmentStatusData.totalSolarRadiation);
        compartmentStatusJson.put("totalDisinfectionCount"  , compartmentStatusData.totalDisinfectionCount);	//合計消毒量
        compartmentStatusJson.put("totalKansuiCount"		    , compartmentStatusData.totalKansuiCount);				//合計潅水量
        compartmentStatusJson.put("totalTuihiCount"			    , compartmentStatusData.totalTuihiCount);					//合計追肥量
        compartmentStatusJson.put("totalShukakuCount"		    , compartmentStatusData.totalShukakuCount);				//合計収穫量
        compartmentStatusJson.put("oldDisinfectionCount"	  , compartmentStatusData.oldDisinfectionCount);		//去年消毒量
        compartmentStatusJson.put("oldKansuiCount"			    , compartmentStatusData.oldKansuiCount);					//去年潅水量
        compartmentStatusJson.put("oldTuihiCount"			      , compartmentStatusData.oldTuihiCount);						//去年追肥量
        compartmentStatusJson.put("oldShukakuCount"			    , compartmentStatusData.oldShukakuCount);					//去年収穫量
        compartmentStatusJson.put("nowWorkMode"				      , compartmentStatusData.nowWorkMode);						  //現在作業モード
        compartmentStatusJson.put("endWorkId"				        , compartmentStatusData.endWorkId);							  //現在完了作業ＩＤ

        resultJson.put("kukakuId", sKukakuId);
        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTDISPKAY, compartmentStatusJson);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】作業情報を取得する
     * @return 作業情報JSON
     */
    public static Result getWork() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- 作業情報 -----*/

        listJson   = Json.newObject();	//リスト形式JSONを初期化する

        //アカウント情報の取得
        if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.REDIRECT);
        }
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
Logger.debug("[ GET WORK ] START");
        WorkChainCompornent.getWorkOfWorkChainJson(accountComprtnent.accountData.farmId, accountComprtnent.accountStatusData.selectChainId, listJson);
Logger.debug("[ GET WORK ] END");

        //----- ワークチェイン情報を取得 -----
        WorkChain wc = WorkChain.getWorkChain(accountComprtnent.accountStatusData.selectChainId);
        resultJson.put("workchainid"  , accountComprtnent.accountStatusData.selectChainId);
        resultJson.put("workchainname", wc.workChainName);


        resultJson.put(AgryeelConst.WorkInfo.TARGETWORK, listJson);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】作業対象区画を取得する
     * @return 作業情報JSON
     */
    public static Result getKukakuOfWorkJson() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();
        JsonNode jn = request().body().asJson();

        double workId = jn.get("workId").asDouble();
        double pLat   = jn.get("pLat").asDouble();
        double pLng   = jn.get("pLng").asDouble();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        WorkChainCompornent.getKukakuOfWorkJson(accountComprtnent.accountData.farmId, workId, resultJson, pLat, pLng, accountComprtnent.accountStatusData.radius);
        resultJson.put("workTargetDisplay", accountComprtnent.accountStatusData.workTargetDisplay);
        resultJson.put("displayChain", accountComprtnent.accountStatusData.displayChain);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】最新のタイムライン
     * @return タイムライン取得結果
     */
    public static Result getTimeLine() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        getTimeLineData(resultJson);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】最新のタイムラインをJsonオブジェクトに追加する
     * @return タイムライン取得結果
     */
    public static void getTimeLineData(ObjectNode	resultJson) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	listJson   = Json.newObject();

        /*----- タイムライン情報 -----*/
        JsonNode input 			= request().body().asJson();
        SimpleDateFormat sdfT	=	new SimpleDateFormat("yyyyMMdd");
        Date dateFrom;
        Date dateTo;
        boolean target			= false;

        try {
          dateFrom 		= new java.sql.Date(sdfT.parse(input.get("timeLineF").asText().replace("/", "")).getTime());
    			dateTo 			= new java.sql.Date(sdfT.parse(input.get("timeLineT").asText().replace("/", "")).getTime());
    		} catch (ParseException e) {
    			dateFrom		=	new java.sql.Date(new Date().getTime());
    			dateTo			=	new java.sql.Date(new Date().getTime());

    		}

        listJson   = Json.newObject();	//リスト形式JSONを初期化する
        /* アカウント情報からタイムライン検索条件を取得する */
        UserComprtnent uc = new UserComprtnent();
        uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        String[] searchWhere = uc.accountStatusData.selectWorkId.split(",");
        List<Double>			aryWork			= new ArrayList<Double>();
        for (String searchWhereData : searchWhere) {
          aryWork.add(Double.parseDouble(searchWhereData));
        }

        /* アカウント情報からタイムライン情報を取得する */
        List<TimeLine> timeLine = TimeLine.find.where().eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).in("work_id", aryWork).between("work_date", dateFrom, dateTo).orderBy("work_date desc, updateTime desc, timeLineId desc").findList();
        SimpleDateFormat sdf	= new SimpleDateFormat("MM.dd HH:mm");
        SimpleDateFormat sdf2	= new SimpleDateFormat("MM/dd");

        for (TimeLine timeLineData : timeLine) {							//作業情報をJSONデータに格納する

        	WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

        	if (workd == null) { continue; }

        	target = false;

          ObjectNode timeLineJson		= Json.newObject();
          timeLineJson.put("timeLineId"	, timeLineData.timeLineId);						//タイムラインID
          timeLineJson.put("workdate"		, sdf2.format(workd.workDate));					//作業日
          timeLineJson.put("updateTime"	, sdf.format(timeLineData.updateTime));			//更新日時
          timeLineJson.put("message"		, StringU.setNullTrim(timeLineData.message));	//メッセージ
          timeLineJson.put("timeLineColor", timeLineData.timeLineColor);					//タイムラインカラー
          timeLineJson.put("workDiaryId"	, timeLineData.workDiaryId);					//作業記録ID
          timeLineJson.put("workName"		, timeLineData.workName);						//作業名
          timeLineJson.put("kukakuName"	, timeLineData.kukakuName);						//区画名
          timeLineJson.put("accountName"	, timeLineData.accountName);					//アカウント名
          timeLineJson.put("workId"		, timeLineData.workId);							//作業ＩＤ
          timeLineJson.put("worktime"		, workd.workTime);								//作業時間
          timeLineJson.put("workDiaryId"	, workd.workDiaryId);							//ＩＤ

          listJson.put(Double.toString(timeLineData.timeLineId), timeLineJson);

        }

        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
    }

    /**
     * 【AGRYEEL】クリップ切替処理
     * @return クリップ切替結果JSON
     */
    public static Result addClip() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode jsonData = request().body().asJson();

        /*----- クリップ圃場情報を取得する -----*/
        String accountId 	= session(AgryeelConst.SessionKey.ACCOUNTID);	/* アカウントID */
        String kukakuId		= jsonData.get("kukakuId").asText();			/* 区画ID */

        //【AICA】TODO:クリップモード修正
//        ClipCompartment clipData = ClipCompartment.find.where().eq("account_id", accountId).eq("kukaku_id", Double.parseDouble(kukakuId)).findUnique(); /* 該当キーに合致するクリップ情報を取得 */
//
//        if (clipData == null) {										/* 該当クリップが存在しない場合、新規にモデルを作成する */
//            clipData = new ClipCompartment();								/* オブジェクト化 */
//            clipData.accountId 		= accountId;						/* アカウントIDをセット */
//            clipData.kukakuId 		= Double.parseDouble(kukakuId);		/* 区画IDをセット */
//            clipData.clipGroupId	= 0;								/* 初期グループをセット */
//            clipData.save();											/* 新規クリップを追加 */
//            resultJson.put(AgryeelConst.ClipGroup.CLIPRESULT, AgryeelConst.ClipGroup.EXISTS);
//        }
//        else {														/* 該当クリップが存在する場合 */
//            /* クリップ情報を削除する */
//            Ebean.createSqlUpdate("DELETE FROM clip_compartment WHERE account_id = :accountId AND kukaku_id = :kukakuId")
//            .setParameter("accountId", clipData.accountId).setParameter("kukakuId", clipData.kukakuId).execute();
//
//            resultJson.put(AgryeelConst.ClipGroup.CLIPRESULT, AgryeelConst.ClipGroup.NONE);
//        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】作業対象圃場画面遷移
     * @return 作業対象圃場レンダー
     */
    public static Result workTargetMove() {
        return ok(views.html.worktarget.render(""));
    }

    /**
     * 【AGRYEEL】作業対象圃場初期表示データ取得
     * @return 作業対象圃場データJSON
     */
    public static Result workTargetInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- 作業対象圃場 -----*/
        /* アカウント情報から作業対象区画情報を取得する */
        List<WorkCompartment> workCompartment = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).findList();

        /* 区画IDから区画状況情報を取得する */
        List<Double> aryKukakuId = new ArrayList<Double>();						//検索条件 区画ID
        for (WorkCompartment workCompartmentData : workCompartment) {			//検索条件を生成する
            aryKukakuId.add(workCompartmentData.kukakuId);
        }

        List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id").findList();

        for (CompartmentStatus compartmentStatusData : compartmentStatus) {		//区画状況情報をJSONデータに格納する

            /* 区画IDから区画情報を取得する */
            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();
            if (compartmentData == null) { continue; }															//区画情報が存在しない場合、データを作成しない

            /* 区画IDから作業対象区画情報を取得する */
            WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", compartmentStatusData.kukakuId).findUnique();

            ObjectNode compartmentStatusJson	= Json.newObject();
            compartmentStatusJson.put("kukakuId"			, compartmentStatusData.kukakuId);							//区画ID
            compartmentStatusJson.put("kukakuName"			, compartmentData.kukakuName);								//区画名
            compartmentStatusJson.put("rotationSpeedOfYear"	, compartmentStatusData.rotationSpeedOfYear);				//年内回転数
            compartmentStatusJson.put("hinsyuName"			, StringU.setNullTrim(compartmentStatusData.hinsyuName));	//品種名
            compartmentStatusJson.put("workTarget"			, workCompartmentData.workTarget);							//作業対象フラグ
            listJson.put(Double.toString(compartmentStatusData.kukakuId), compartmentStatusJson);

        }

        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJson);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】農場グループデータ取得
     * @return 農場データJSON
     */
    public static Result getGroupFarmList() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- 作業対象圃場 -----*/
        /* アカウント情報から作業対象区画情報を取得する */
        List<Farm> farm = Farm.find.where().eq("farm_group_id", Double.valueOf(session(AgryeelConst.SessionKey.FARMGROUPID))).findList();

        for (Farm farmData : farm) {		//農場情報をJSONデータに格納する

            ObjectNode farmJson	= Json.newObject();
            farmJson.put("farmId"			, farmData.farmId);					//農場ID
            farmJson.put("farmName"			, farmData.farmName);				//農場名
            listJson.put(Double.toString(farmData.farmId), farmJson);

        }

        resultJson.put(AgryeelConst.FarmInfo.FARMLIST, listJson);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】農場切替時
     * @return JSON
     */
    public static Result farmChange() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode inpput = request().body().asJson();

        session(AgryeelConst.SessionKey.FARMID, String.valueOf(inpput.get("farmId").asText()));									//農場IDをセッションに格納

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】作業対象圃場更新処理
     * @return 作業対象圃場データ更新結果JSON
     */
    public static Result workTargetUpdate() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode jsonData = request().body().asJson();
        double kukakuId = Double.parseDouble(jsonData.get("kukakuId").asText());

        /* 区画IDから作業対象区画情報を取得する */
        //WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("kukaku_id", jsonData.get("kukakuId").asText()).findUnique();
        WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", kukakuId).findUnique();

        /* 作業対象区画情報の作業対象フラグを更新する */
        if(workCompartmentData.workTarget == 0){
            workCompartmentData.workTarget = 1;
        }else{
            workCompartmentData.workTarget = 0;
        }
        workCompartmentData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アカウント作成画面遷移
     * @return アカウント作成画面レンダー
     */
    public static Result accountMakeMove() {
        return ok(views.html.accountMake.render(""));
    }

    /**
     * 【AGRYEEL】区画グループ検索条件を取得する
     * @return 検索条件JSON
     */
    public static Result getHouseGroupWhere() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- 作業情報 -----*/

        listJson   = Json.newObject();	//リスト形式JSONを初期化する

    	int clipMode = 0;

    	UserComprtnent accountComprtnent = new UserComprtnent();
    	accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
    	clipMode = accountComprtnent.accountData.clipMode;

    	  //【AICA】TODO:生産者別生産物の見直し
//        /* アカウント情報から農場別生産物情報を取得する */
//        List<HouseGroupWhere> houseGroupWhere = HouseGroupWhere.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).orderBy("kukaku_group_id").findList();
//
//        for (HouseGroupWhere houseGroupWhereData : houseGroupWhere) {
//
//        	CompartmentGroup compartmentGroup = CompartmentGroup.find.where().eq("kukaku_group_id", houseGroupWhereData.kukakuGroupId).findUnique();
//
//            ObjectNode compartmentGroupJson		= Json.newObject();
//            compartmentGroupJson.put("kukakuGroupId"	, compartmentGroup.kukakuGroupId);			//作業ID
//            compartmentGroupJson.put("kukakuGroupName"	, compartmentGroup.kukakuGroupName);		//作業ID
//            compartmentGroupJson.put("kukakuGroupColor"	, compartmentGroup.kukakuGroupColor);		//作業カラー
//
//            if (clipMode == 1) {
//
//                compartmentGroupJson.put("flag"				, "0");							//フラグ
//
//            }
//            else {
//                compartmentGroupJson.put("flag"				, houseGroupWhereData.flag);	//フラグ
//            }
//
//            listJson.put(Double.toString(compartmentGroup.kukakuGroupId), compartmentGroupJson);
//
//        }

       resultJson.put("clipMode", String.valueOf(clipMode));
       resultJson.put("compartmentGroup", listJson);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】区画グループ検索条件を変更する
     * @return 検索条件JSON
     */
    public static Result changeHouseGroupWhere(double kukakuGroupId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- 作業情報 -----*/

        listJson   = Json.newObject();	//リスト形式JSONを初期化する

    	int clipMode = 0;

    	UserComprtnent accountComprtnent = new UserComprtnent();
    	accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        if ( kukakuGroupId == AgryeelConst.ClipGroup.CLIPGROUPNO) {

        	clipMode = accountComprtnent.ChangeClipMode();

        }
        else {

        	clipMode = accountComprtnent.UpdateClipMode(AgryeelConst.ClipGroup.NONE);

        }

        //【AICA】TODO:生産者別生産物の見直し
//        /* アカウント情報から農場別生産物情報を取得する */
//        List<HouseGroupWhere> houseGroupWhere = HouseGroupWhere.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).orderBy("kukaku_group_id").findList();
//
//        for (HouseGroupWhere houseGroupWhereData : houseGroupWhere) {
//
//        	CompartmentGroup compartmentGroup = CompartmentGroup.find.where().eq("kukaku_group_id", houseGroupWhereData.kukakuGroupId).findUnique();
//
//            ObjectNode compartmentGroupJson		= Json.newObject();
//
//            if (clipMode == 1) {
//
//    			houseGroupWhereData.flag = 0;
//
//            }
//            else {
//            	if (houseGroupWhereData.kukakuGroupId == kukakuGroupId) {
//
//            		if (houseGroupWhereData.flag == 0) {
//
//            			houseGroupWhereData.flag = 1;
//
//            		}
//            		else {
//
//            			houseGroupWhereData.flag = 0;
//
//            		}
//
//            		houseGroupWhereData.update();
//
//            	}
//            }
//
//            compartmentGroupJson.put("kukakuGroupId"	, compartmentGroup.kukakuGroupId);			//作業ID
//            compartmentGroupJson.put("kukakuGroupName"	, compartmentGroup.kukakuGroupName);		//作業ID
//            compartmentGroupJson.put("kukakuGroupColor"	, compartmentGroup.kukakuGroupColor);		//作業カラー
//            compartmentGroupJson.put("flag"				, houseGroupWhereData.flag);	//フラグ
//
//            listJson.put(Double.toString(compartmentGroup.kukakuGroupId), compartmentGroupJson);
//
//        }

        resultJson.put("compartmentGroup", listJson);
        resultJson.put("clipMode", String.valueOf(clipMode));

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】タイムライン検索条件を取得する
     * @return 検索条件JSON
     */
    public static Result getSearchWhere() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- 作業情報 -----*/

        listJson   = Json.newObject();	//リスト形式JSONを初期化する

        //TODO:TimeLine検索条件みなおし
        /* アカウント情報から農場別生産物情報を取得する */
//        List<SearchWhere> searchWhere = SearchWhere.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).orderBy("work_id").findList();
//
//        for (SearchWhere searchWhereData : searchWhere) {
//
//        	Work workData = Work.find.where().eq("work_id", searchWhereData.workId).findUnique();
//
//            ObjectNode workJson		= Json.newObject();
//            workJson.put("workId"		, workData.workId);			//作業ID
//            workJson.put("workColor"	, workData.workColor);		//作業カラー
//            workJson.put("flag"			, searchWhereData.flag);	//フラグ
//
//            listJson.put(Double.toString(workData.workId), workJson);
//
//        }
//
//        resultJson.put("workList", listJson);
//
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】タイムライン検索条件を変更する
     * @return 検索条件JSON
     */
    public static Result changeSearchWhere(double workId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- 作業情報 -----*/

        //TODO:TimeLine検索条件みなおし
//        listJson   = Json.newObject();	//リスト形式JSONを初期化する
//
//        /* アカウント情報から農場別生産物情報を取得する */
//        List<SearchWhere> searchWhere = SearchWhere.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).orderBy("work_id").findList();
//
//        for (SearchWhere searchWhereData : searchWhere) {
//
//        	Work workData = Work.find.where().eq("work_id", searchWhereData.workId).findUnique();
//
//        	if (searchWhereData.workId == workId) {
//
//        		if (searchWhereData.flag == 0) {
//
//        			searchWhereData.flag = 1;
//
//        		}
//        		else {
//
//        			searchWhereData.flag = 0;
//
//        		}
//
//        		searchWhereData.update();
//
//        	}
//
//            ObjectNode workJson		= Json.newObject();
//            workJson.put("workId"		, workData.workId);			//作業ID
//            workJson.put("workColor"	, workData.workColor);		//作業カラー
//            workJson.put("flag"			, searchWhereData.flag);	//フラグ
//
//            listJson.put(Double.toString(workData.workId), workJson);
//
//        }
//
//        resultJson.put("workList", listJson);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }
    /**
     * 【AGRYEEL】システムメッセージを取得する
     * @return 検索条件JSON
     */
    public static Result getSystemMessage() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();
        ObjectNode  listJson = Json.newObject();

        /* アカウント情報を取得する */
        String accountId = session(AgryeelConst.SessionKey.ACCOUNTID);
        UserComprtnent accountComprtnent = new UserComprtnent();
        accountComprtnent.GetAccountData(accountId);

        List<SystemMessage> sms = MessageOfAccount.getMessageByAccountId(accountId);
        int messageKind = accountComprtnent.accountStatusData.messageKind;
        if(sms.size() > 0) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
          SimpleDateFormat tmf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
          Calendar cal = Calendar.getInstance();
          java.sql.Date system = new java.sql.Date(cal.getTimeInMillis());

          for(SystemMessage sm : sms) {
            /* フィルターチェック */
            if ((messageKind != AgryeelConst.MessageKind.ALL) && (messageKind != sm.messageKind)) {
              continue;
            }
            ObjectNode  mssageJson = Json.newObject();
            mssageJson.put("key", tmf.format(sm.updateTime));
            mssageJson.put("message", sm.message);
            mssageJson.put("date", sdf.format(sm.releaseDate));

            long diff = DateU.GetDiffDate(sm.releaseDate, system);

            if ( 0 <= diff && diff < 5) {
              mssageJson.put("diff", diff);
            }
            else {
              mssageJson.put("diff", 5);
            }
            if (diff == 0) {
              mssageJson.put("new", 1);
            }
            else {
              mssageJson.put("new", 0);
            }

            listJson.put(tmf.format(sm.updateTime), mssageJson);
          }
          resultJson.put("datalist", listJson);
          resultJson.put("browse", 1);
        }
        else {
          resultJson.put("browse", 0);
        }
        resultJson.put("mkind", messageKind);
        resultJson.put("mkindn", Common.GetCommonValue(Common.ConstClass.MESSAGEKIND, messageKind));

        return ok(resultJson);
    }
    public static Result commitSystemMessage(String key) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);

      /* アカウント情報を取得する */
      String accountId = session(AgryeelConst.SessionKey.ACCOUNTID);
      UserComprtnent accountComprtnent = new UserComprtnent();
      accountComprtnent.GetAccountData(accountId);
      SimpleDateFormat tmf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
      java.sql.Timestamp updateTime;
      try {
        updateTime = new java.sql.Timestamp(tmf.parse(key).getTime());
        MessageOfAccount.checkMessage(accountId, updateTime);
        List<SystemMessage> sms = MessageOfAccount.getMessageByAccountId(accountId);
        if(sms.size() > 0) {
          resultJson.put("browse", 1);
        }
        else {
          resultJson.put("browse", 0);
        }
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
      } catch (ParseException e) {
        e.printStackTrace();
      }

      return ok(resultJson);
    }
    public static Result commitAllSystemMessage() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);

      /* アカウント情報を取得する */
      String accountId = session(AgryeelConst.SessionKey.ACCOUNTID);
      UserComprtnent accountComprtnent = new UserComprtnent();
      accountComprtnent.GetAccountData(accountId);

      List<SystemMessage> sms = MessageOfAccount.getMessageByAccountId(accountId);
      int messageKind = accountComprtnent.accountStatusData.messageKind;
      if(sms.size() > 0) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat tmf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Calendar cal = Calendar.getInstance();
        java.sql.Date system = new java.sql.Date(cal.getTimeInMillis());

        for(SystemMessage sm : sms) {
          /* フィルターチェック */
          if ((messageKind != AgryeelConst.MessageKind.ALL) && (messageKind != sm.messageKind)) {
            continue;
          }
          MessageOfAccount.checkMessage(accountId, sm.updateTime);
        }
        sms = MessageOfAccount.getMessageByAccountId(accountId);
        if(sms.size() > 0) {
          resultJson.put("browse", 1);
        }
        else {
          resultJson.put("browse", 0);
        }
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
      }
      else {
        resultJson.put("browse", 0);
      }
      return ok(resultJson);
    }
    public static Result changeMessageKind(int messageKind) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);

      /* アカウント情報を取得する */
      String accountId = session(AgryeelConst.SessionKey.ACCOUNTID);
      UserComprtnent accountComprtnent = new UserComprtnent();
      accountComprtnent.GetAccountData(accountId);
      accountComprtnent.accountStatusData.messageKind = messageKind;
      accountComprtnent.accountStatusData.update();

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    /**
     * 圃場グループ選択変更時
     * @return
     */
    public static Result changeFieldGroup() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode inputJson = request().body().asJson();
        String  selectFieldGroup  = inputJson.get("selectFieldGroup").asText();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        accountComprtnent.accountStatusData.selectFieldGroupId = selectFieldGroup;
        accountComprtnent.accountStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
    /**
     * ワークチェイン選択変更時
     * @return
     */
    public static Result changeWorkChain() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode inputJson = request().body().asJson();
        String  selectWorkChain  = inputJson.get("selectWorkChain").asText();

        //アカウント情報の取得
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        double chainId = 0;
        if ((selectWorkChain != null) && (!"".equals(selectWorkChain))) {
          chainId = Double.parseDouble(selectWorkChain);
        }
        accountComprtnent.accountStatusData.selectChainId = chainId;
        accountComprtnent.accountStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * 圃場詳細情報を取得する
     * @return
     */
    public static Result getFieldDetail() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();
      ObjectNode  listJson = Json.newObject();
      Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
      ObjectMapper map = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode   listJsonApi = map.createArrayNode();
      boolean     api        = false;
      if (session(AgryeelConst.SessionKey.API) != null) {
      	api = true;
      }

      /* JSONデータを取得 */
      JsonNode inputJson = request().body().asJson();
      double  fieldId  = inputJson.get("field").asDouble();

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      resultJson.put("compartmentStatusSkip", accountComprtnent.accountStatusData.compartmentStatusSkip); //区画状況照会SKIPを取得
      resultJson.put("displayChain", accountComprtnent.accountStatusData.displayChain);

      CompartmentStatusCompornent csc = new CompartmentStatusCompornent();
      csc.getAllData(accountComprtnent.accountData.farmId);
      Farm farm = Farm.getFarm(accountComprtnent.accountData.farmId);
      FarmStatus farmStatus = farm.getFarmStatus();
      DecimalFormat df = new DecimalFormat("#,##0.00");

      Field field = Field.getFieldInfo(fieldId);
      FieldGroup fg = FieldGroupList.getFieldGroup(field.fieldId);
      //圃場情報を取得

      ObjectNode fieldJson  = Json.newObject();

      fieldJson.put("fieldColor"         , fg.fieldGroupColor);     //圃場グループカラー
      fieldJson.put("fieldId"            , field.fieldId);          //圃場ID
      fieldJson.put("fieldName"          , field.fieldName);        //圃場名
      fieldJson.put("post"               , StringU.setNullTrim(field.postNo));           //郵便番号
      double area = field.area;
      if (farmStatus.areaUnit == 1) { //平方メートルの場合
        area = area * 100;
      }
      else if (farmStatus.areaUnit == 2) { //坪の場合
        area = area * 30.25;
      }
      fieldJson.put("area"                , df.format(area));                        //区画面積
      fieldJson.put("areaUnit"            , Common.GetCommonValue(Common.ConstClass.AREAUNIT, farmStatus.areaUnit, true)); //区画面積単位

      //----------------------------------------------------------------------------------------------------------------------------------
      //- 天気予報
      //----------------------------------------------------------------------------------------------------------------------------------
      URL url;
      ObjectNode  weatherList  = Json.newObject();
      ObjectNode  weatherListd = Json.newObject();
      ArrayNode   weatherListApi = map.createArrayNode();
      ArrayNode   weatherListdApi = map.createArrayNode();
      try {
        if ((field.postNo != null) && (!"".equals(field.postNo))) {
          url = new URL(AgryeelConst.Owm.URL + "?zip=" + field.postNo.substring(0,3) + "-" + field.postNo.substring(3) + ",JP&appid=" + AgryeelConst.Owm.API);
          HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
          InputStream in2 = httpConn.getInputStream();
          BufferedReader reader = new BufferedReader(new InputStreamReader(in2, "UTF-8"));

          ObjectMapper mapper = new ObjectMapper();
          JsonNode node = mapper.readTree(reader);
          SimpleDateFormat sdfp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          SimpleDateFormat sdfd = new SimpleDateFormat("MM/dd");
          SimpleDateFormat sdft = new SimpleDateFormat("HH:mm");
          DecimalFormat    dfr  = new DecimalFormat("0.0");

          //----- 直近18時間分の天気予報
          for (int idx=0; idx<5; idx++) {
            JsonNode node2 = node.get("list").get(idx);
            ObjectNode  wt = Json.newObject();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(sdfp.parse(node2.get("dt_txt").asText()).getTime());
            cal.add(Calendar.HOUR_OF_DAY, AgryeelConst.Owm.TIMEHOSEI);
            wt.put("date", sdfd.format(cal.getTime()));
            wt.put("time", sdft.format(cal.getTime()));
            wt.put("icon", node2.get("weather").get(0).get("icon").asText());
            if (node2.get("rain") != null && node2.get("rain").get("3h") != null) {
              wt.put("rain", dfr.format(node2.get("rain").get("3h").asDouble()));
            }
            else {
              wt.put("rain", "-");
            }
            weatherList.put(String.valueOf(idx), wt);
            weatherListApi.add(wt);
          }

          int cnt = node.get("cnt").asInt();
          for (int idx=0; idx<cnt; idx++) {
            JsonNode node2 = node.get("list").get(idx);
            ObjectNode  wt = Json.newObject();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(sdfp.parse(node2.get("dt_txt").asText()).getTime());
            cal.add(Calendar.HOUR_OF_DAY, AgryeelConst.Owm.TIMEHOSEI);
            if (cal.get(Calendar.HOUR_OF_DAY) == 12) {
              wt.put("date", sdfd.format(cal.getTime()));
              wt.put("time", sdft.format(cal.getTime()));
              wt.put("icon", node2.get("weather").get(0).get("icon").asText());
              if (node2.get("rain") != null && node2.get("rain").get("3h") != null) {
                wt.put("rain", dfr.format(node2.get("rain").get("3h").asDouble()));
              }
              else {
                wt.put("rain", "-");
              }
              weatherListd.put(String.valueOf(idx), wt);
              weatherListdApi.add(wt);
            }
          }
        }
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      }
      if(api){
        fieldJson.put("weatherlist" , weatherListApi);
        fieldJson.put("weatherlistd", weatherListdApi);
      }else{
        fieldJson.put("weatherlist" , weatherList);
        fieldJson.put("weatherlistd", weatherListd);
      }

      //圃場に所属している区画一覧を取得する
      FieldComprtnent fc = new FieldComprtnent();
      fc.getFileld(field.fieldId);
      List<Compartment> compartments = fc.getCompartmentList();
      resultJson.put("compartmentCount", compartments.size());
      for (Compartment compartmentData : compartments) {
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(compartmentData.kukakuId);

        ObjectNode  kJ = Json.newObject();

        if(api){
          kJ.put("kukakuId"          , compartmentData.kukakuId);                    //区画ID
        }
        kJ.put("kukakuName"          , compartmentData.kukakuName);                  //区画名
        kJ.put("workYear"            , compartmentStatusData.workYear);              //作業年
        kJ.put("rotationSpeedOfYear" , compartmentStatusData.rotationSpeedOfYear);   //年内回転数
        kJ.put("hashuCount"          , compartmentStatusData.hashuCount);            //播種回数
        long seiikuDayCount = 0;
        long seiikuDayCountEnd  = 0;
        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //播種日

          Date hashuDate = DateUtils.truncate(compartmentStatusData.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

          kJ.put("hashuDate"       , compartmentStatusData.hashuDate.toString());

          //
//          if (compartmentStatusData.seiikuDayCount != 0) {
//            seiikuDayCount = compartmentStatusData.seiikuDayCount;
//          }
//          else {
//            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//          }
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {  //収穫開始日
            seiikuDayCount = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuStartDate);
          }
          else {
            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
          }
          if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {  //収穫開始日
            seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuEndDate);
          }
          else {
            seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, systemDate);
          }

        }
        else {
          kJ.put("hashuDate"       , "");
          seiikuDayCount = 0;
          seiikuDayCountEnd = 0;
        }

        kJ.put("seiikuDayCount"    , seiikuDayCount);                                                   //生育日数
        kJ.put("seiikuDayCountEnd" , seiikuDayCountEnd);                                                //生育日数(収穫終了日)

        Crop cp = CropComprtnent.getCropById(compartmentStatusData.cropId);
        if (cp != null) {                                                                               //生産物名称
          kJ.put("cropName"    , cp.cropName);
        }
        else {
          kJ.put("cropName"    , "");
        }
        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

          kJ.put("hinsyuName"        , compartmentStatusData.hinsyuName);            //品種名

        }
        else {

          kJ.put("hinsyuName"        , "");                                          //品種名

        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 積算温度
        //----------------------------------------------------------------------------------------------------------------------------------
        kJ.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));
        double totalDatas = 0;
        double todayDatas = 0;
        java.sql.Date oldDate = null;
        Weather oWeather      = null;
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
        if (oWeather != null) {
          kJ.put("yosokuSolarDate"      , sdf.format(oWeather.dayDate));
        }
        else {
          kJ.put("yosokuSolarDate"      , "");
        }
        kJ.put("yosokuSolarRadiation"   , df.format(totalDatas));
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 積算降水量
        //----------------------------------------------------------------------------------------------------------------------------------
        double totalDatar = 0;
        oWeather      = null;
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

                Calendar cal = Calendar.getInstance();
                DateU.setTime(cal, DateU.TimeType.TO);
                endDate = new java.sql.Date(cal.getTime().getTime());

                List<Weather> weathers = Weather.getWeather(pointId, compartmentStatusData.hashuDate, endDate);

                for (Weather weather : weathers) {
                  if (weather.jituyo == AgryeelConst.JITUYO.JISEKI) {
                    totalDatar += weather.rain;
                    oWeather = weather;
                  }
                }
              }
            }
          }
        }
        kJ.put("rain"   , df.format(totalDatar));
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 消毒
        //----------------------------------------------------------------------------------------------------------------------------------
        long disinfectionCount = 0;
        if (compartmentStatusData.finalDisinfectionDate != null && (compartmentStatusData.finalDisinfectionDate.compareTo(nullDate) != 0)) {

          Date finalDisinfectionDate = DateUtils.truncate(compartmentStatusData.finalDisinfectionDate, Calendar.DAY_OF_MONTH);

          kJ.put("finalDisinfectionDate"       , compartmentStatusData.finalDisinfectionDate.toString());

          disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, systemDate);

        }
        else {
          kJ.put("finalDisinfectionDate"       , "");
          disinfectionCount = 0;
        }

        kJ.put("disinfectionCount"          , disinfectionCount);                                               //最終消毒日からの経過日数
        kJ.put("totalDisinfectionNumber"    , df.format(compartmentStatusData.totalDisinfectionNumber * 0.001));//合計消毒量
        kJ.put("totalDisinfectionCount"     , compartmentStatusData.totalDisinfectionCount);                    //合計消毒回数
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 潅水量
        //----------------------------------------------------------------------------------------------------------------------------------
        long kansuiCount = 0;
        if (compartmentStatusData.finalKansuiDate != null && (compartmentStatusData.finalKansuiDate.compareTo(nullDate) != 0)) {

          Date finalKansuiDate = DateUtils.truncate(compartmentStatusData.finalKansuiDate, Calendar.DAY_OF_MONTH);

          kJ.put("finalKansuiDate"       , compartmentStatusData.finalKansuiDate.toString());

          kansuiCount = DateU.GetDiffDate(finalKansuiDate, systemDate);

        }
        else {
          kJ.put("finalKansuiDate"       , "");
          kansuiCount = 0;
        }

        kJ.put("kansuiCount"          , kansuiCount);                                       //最終潅水日からの経過日数
        kJ.put("totalKansuiNumber"    , df.format(compartmentStatusData.totalKansuiNumber));//合計潅水量
        kJ.put("totalKansuiCount"     , compartmentStatusData.totalKansuiCount);            //合計潅水回数
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 追肥
        //----------------------------------------------------------------------------------------------------------------------------------
        long tuihiCount = 0;
        if (compartmentStatusData.finalTuihiDate != null && (compartmentStatusData.finalTuihiDate.compareTo(nullDate) != 0)) {

          Date finalTuihiDate = DateUtils.truncate(compartmentStatusData.finalTuihiDate, Calendar.DAY_OF_MONTH);

          kJ.put("finalTuihiDate"       , compartmentStatusData.finalTuihiDate.toString());

          tuihiCount = DateU.GetDiffDate(finalTuihiDate, systemDate);

        }
        else {
          kJ.put("finalTuihiDate"       , "");
          tuihiCount = 0;
        }

        kJ.put("tuihiCount"          , tuihiCount);                                       //最終追肥日からの経過日数
        kJ.put("totalTuihiNumber"    , df.format(compartmentStatusData.totalTuihiNumber * 0.001));//合計追肥量
        kJ.put("totalTuihiCount"     , compartmentStatusData.totalTuihiCount);            //合計追肥回数
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 収穫
        //----------------------------------------------------------------------------------------------------------------------------------
        if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {

          kJ.put("shukakuStartDate"       , compartmentStatusData.shukakuStartDate.toString());

        }
        else {
          kJ.put("shukakuStartDate"       , "");
        }
        if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {

          kJ.put("shukakuEndDate"       , compartmentStatusData.shukakuEndDate.toString());

        }
        else {
          kJ.put("shukakuEndDate"       , "");
        }

        kJ.put("totalShukakuNumber"    , df.format(compartmentStatusData.totalShukakuCount));        //合計収穫量
        if ((compartmentStatusData.totalShukakuCount == 0)
            || (compartmentData.area == 0)) {
          kJ.put("tanshu"              , "*****");
        }
        else {
          kJ.put("tanshu"              , df.format((compartmentStatusData.totalShukakuCount / compartmentData.area) * 10));
        }

        ObjectNode chainJson = Json.newObject();
        ArrayNode chainJsonApi = map.createArrayNode();
        if(api){
          csc.getWorkChainStatusJsonArray(compartmentData.kukakuId, chainJsonApi);
          kJ.put("chain", chainJsonApi);
        }else{
          csc.getWorkChainStatusJson(compartmentData.kukakuId, chainJson);
          kJ.put("chain", chainJson);
        }

        ObjectNode  aj   = Json.newObject();                                                              //作業中アカウント情報
        UserComprtnent uc = new UserComprtnent();
        uc.getNowWorkingByField(compartmentData.kukakuId, aj);
        kJ.put("working", aj);

        listJson.put(String.valueOf(compartmentData.kukakuId), kJ);
        listJsonApi.add(kJ);

      }

      resultJson.put("fieldJson", fieldJson);
      if(api){
        resultJson.put("datalist", listJsonApi);
      }else{
        resultJson.put("datalist", listJson);
      }
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    /**
     * 区画詳細情報を取得する
     * @return
     */
    public static Result getKukakuDetail() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson    = Json.newObject();
      ObjectNode  timelineListJson  = Json.newObject();
      ObjectNode  nouyakuListJson   = Json.newObject();
      ObjectNode  hiryoListJson     = Json.newObject();
      ObjectNode  kJ = Json.newObject();
      Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode   timelineListJsonApi = mapper.createArrayNode();
      ArrayNode   nouyakuListJsonApi  = mapper.createArrayNode();
      ArrayNode   hiryoListJsonApi    = mapper.createArrayNode();
      ArrayNode   kJApi = mapper.createArrayNode();
      DecimalFormat df = new DecimalFormat("#,##0.00");
      boolean     api        = false;
      if (session(AgryeelConst.SessionKey.API) != null) {
      	api = true;
      }

      /* JSONデータを取得 */
      JsonNode inputJson = request().body().asJson();
      double  kukakuId  = inputJson.get("kukaku").asDouble();
      int     year      = inputJson.get("year").asInt();
      int     rotation  = inputJson.get("rotation").asInt();

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
      CompartmentStatusCompornent csc = new CompartmentStatusCompornent();
      csc.getAllData(accountComprtnent.accountData.farmId);
      Farm farm = Farm.getFarm(accountComprtnent.accountData.farmId);
      FarmStatus farmStatus = farm.getFarmStatus();

      Compartment compartmentData = FieldComprtnent.getCompartment(kukakuId);
      Timestamp systemTime = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());

      if (compartmentData != null) {
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatusFromMotocho(compartmentData.kukakuId, year, rotation);

        //-------------- 日付範囲キーを変数化する -------------------------------
        java.sql.Timestamp tStart   = compartmentStatusData.katadukeDate;
        java.sql.Timestamp tEnd     = compartmentStatusData.finalEndDate;
        java.sql.Timestamp tsystem  = DateU.getSystemTimeStamp();

        kJ.put("kukakuId"            , compartmentData.kukakuId);                    //区画ID
        kJ.put("kukakuName"          , compartmentData.kukakuName);                  //区画名
        double area = compartmentData.area;
        if (farmStatus.areaUnit == 1) { //平方メートルの場合
          area = area * 100;
        }
        else if (farmStatus.areaUnit == 2) { //坪の場合
          area = area * 30.25;
        }
        kJ.put("area"                , df.format(area));                        //区画面積
        kJ.put("areaUnit"            , Common.GetCommonValue(Common.ConstClass.AREAUNIT, farmStatus.areaUnit, true)); //区画面積単位
        kJ.put("rotationSpeedOfYear" , compartmentStatusData.rotationSpeedOfYear);   //年内回転数
        kJ.put("hashuCount"          , compartmentStatusData.hashuCount);            //播種回数
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 作付管理のキーを作成する
        //----------------------------------------------------------------------------------------------------------------------------------
        DecimalFormat df1 = new DecimalFormat("00000000");
        DecimalFormat df2 = new DecimalFormat("0000");
        DecimalFormat df3 = new DecimalFormat("00");
        kJ.put("motochoid"   , df1.format(compartmentStatusData.kukakuId) + df2.format(compartmentStatusData.workYear) + df3.format(compartmentStatusData.rotationSpeedOfYear));
        kJ.put("motochoname" , "" + df2.format(compartmentStatusData.workYear) + " 年 " + df3.format(compartmentStatusData.rotationSpeedOfYear) + " 作");
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 区画ワークチェイン状況を取得する
        //----------------------------------------------------------------------------------------------------------------------------------
        CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();
        WorkChain wc = WorkChain.getWorkChain(cws.workChainId);
        List<WorkChainItem> wcis       = WorkChainItem.getWorkChainItemList(cws.workChainId);
        if (wc != null) {
          kJ.put("workChainId"          , wc.workChainId);
          kJ.put("workChainName"        , wc.workChainName);
        }
        else {
          kJ.put("workChainId"          , "");
          kJ.put("workChainName"        , "");
        }
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 生産物
        //----------------------------------------------------------------------------------------------------------------------------------
        Crop crop = CropComprtnent.getCropById(cws.cropId);
        if (crop != null ){
          kJ.put("cropId"          , crop.cropId);
          kJ.put("cropName"        , crop.cropName);
        }
        else {
          if(api){
            kJ.put("cropId"          , 0);
          }
          else {
            kJ.put("cropId"          , "");
          }
          kJ.put("cropName"        , "");
        }
//----- V02R291 ADD START -----
        crop = CropComprtnent.getCropById(compartmentStatusData.cropId);
        if (crop != null ){
          kJ.put("cropIdD"          , crop.cropId);
          kJ.put("cropNameD"        , crop.cropName);
        }
        else {
          if(api){
            kJ.put("cropIdD"          , 0);
          }
          else {
            kJ.put("cropIdD"          , "");
          }
          kJ.put("cropNameD"        , "");
        }
//----- V02R291 ADD END   -----
        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

          kJ.put("hinsyuName"        , compartmentStatusData.hinsyuName);            //品種名

        }
        else {

          kJ.put("hinsyuName"        , "");                                          //品種名

        }
        long seiikuDayCount = 0;
        long seiikuDayCountEnd  = 0;
        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //播種日

          Date hashuDate = DateUtils.truncate(compartmentStatusData.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

          kJ.put("hashuDate"       , compartmentStatusData.hashuDate.toString());
          //生育日数の表示方法を変更
//          if (compartmentStatusData.seiikuDayCount != 0) {
//            seiikuDayCount = compartmentStatusData.seiikuDayCount;
//          }
//          else {
//            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//          }
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {  //収穫開始日
            seiikuDayCount = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuStartDate);
          }
          else {
            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
          }
          if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {  //収穫開始日
            seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuEndDate);
          }
          else {
            seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, systemDate);
          }

        }
        else {
          kJ.put("hashuDate"       , "");
          seiikuDayCount = 0;
          seiikuDayCountEnd = 0;
        }
        kJ.put("seiikuDayCount"    , seiikuDayCount);                                                   //生育日数
        kJ.put("seiikuDayCountEnd" , seiikuDayCountEnd);                                                //生育日数（収穫終了日）

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 積算温度
        //----------------------------------------------------------------------------------------------------------------------------------
        kJ.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));                   //積算温度
        ObjectNode  labels    = Json.newObject();
        ObjectNode  datas     = Json.newObject();
        ObjectNode  mixdatas  = Json.newObject();
//      double totalDatas     = compartmentStatusData.totalSolarRadiation;                                        //予測積算温度は現在積算温度が初期値
        double totalDatas     = 0;
        double todayDatas     = 0;
        java.sql.Date oldDate = null;
        Weather oWeather      = null;
        int idx = 0;
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
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 積算降水量
        //----------------------------------------------------------------------------------------------------------------------------------
        labels    = Json.newObject();
        datas     = Json.newObject();
        mixdatas  = Json.newObject();
        totalDatas = 0;
        todayDatas = 0;
        oldDate = null;
        idx = 0;
        //----- 積算降水量の算出 -----
        if ((compartmentStatusData.hashuDate != null)
            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

          Compartment ct = Compartment.getCompartmentInfo(compartmentStatusData.kukakuId);
          if (ct != null) {
            Field fd = ct.getFieldInfo();
            if (fd != null) {
              String pointId = PosttoPoint.getPointId(fd.postNo);
              if (pointId != null && !"".equals(pointId)) {
                java.sql.Date endDate;

                Calendar cal = Calendar.getInstance();
                if (year != 0 && rotation != 0) {
                  cal.setTime(compartmentStatusData.finalEndDate);
                  DateU.setTime(cal, DateU.TimeType.TO);
                  endDate = new java.sql.Date(cal.getTime().getTime());
                }
                else {
                  DateU.setTime(cal, DateU.TimeType.TO);
                  endDate = new java.sql.Date(cal.getTime().getTime());
                }

                List<Weather> weathers = Weather.getWeather(pointId, compartmentStatusData.hashuDate, endDate);

                for (Weather weather : weathers) {
                  if (weather.jituyo == AgryeelConst.JITUYO.JISEKI) {
                    idx++;
                    labels.put(String.valueOf(idx), sdf.format(weather.dayDate));
                    datas.put(String.valueOf(idx), weather.rain);
                    totalDatas += weather.rain;
                    mixdatas.put(String.valueOf(idx), totalDatas);
                    oWeather = weather;
                  }
                }
              }
            }
          }
        }
        kJ.put("rain"         , df.format(totalDatas));
        kJ.put("rainLabel"    , labels);
        kJ.put("rainData"     , datas);
        kJ.put("rainMixData"  , mixdatas);
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 消毒
        //----------------------------------------------------------------------------------------------------------------------------------
        long disinfectionCount = 0;
        if (compartmentStatusData.finalDisinfectionDate != null && (compartmentStatusData.finalDisinfectionDate.compareTo(nullDate) != 0)) {

          Date finalDisinfectionDate = DateUtils.truncate(compartmentStatusData.finalDisinfectionDate, Calendar.DAY_OF_MONTH);

          kJ.put("finalDisinfectionDate"       , compartmentStatusData.finalDisinfectionDate.toString());

          if (year != 0 && rotation != 0) {
            disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, compartmentStatusData.finalEndDate);
          }
          else {
            disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, systemDate);
          }

        }
        else {
          kJ.put("finalDisinfectionDate"       , "");
          disinfectionCount = 0;
        }

        kJ.put("disinfectionCount"          , disinfectionCount);                                       //最終消毒日からの経過日数
        kJ.put("totalDisinfectionNumber"    , df.format(compartmentStatusData.totalDisinfectionNumber * 0.001));//合計消毒量
        kJ.put("totalDisinfectionCount"     , compartmentStatusData.totalDisinfectionCount);            //合計消毒回数
        //グラフデータの作成
        List<Double> disinfectionKey = new ArrayList<Double>();
        for (WorkChainItem wci : wcis) {
          Work wk = Work.getWork(wci.workId);
          if (wk.workTemplateId == AgryeelConst.WorkTemplate.SANPU
              && wci.nouhiKind == AgryeelConst.NouhiKind.NOUYAKU) {
            disinfectionKey.add(wk.workId);
          }
        }
        List<WorkDiary> wds;
        if (year != 0 && rotation != 0) {
          wds   = WorkDiary.find.where().eq("kukaku_id", compartmentData.kukakuId).in("work_id", disinfectionKey).between("work_start_time", tStart, tEnd).orderBy("work_date").findList();
        }
        else {
          wds   = WorkDiary.find.where().eq("kukaku_id", compartmentData.kukakuId).in("work_id", disinfectionKey).between("work_start_time", tStart, tsystem).orderBy("work_date").findList();
        }
        labels    = Json.newObject();
        datas     = Json.newObject();
        mixdatas  = Json.newObject();
        totalDatas = 0;
        todayDatas = 0;
        oldDate = null;
        idx = 0;
        for (WorkDiary wd : wds) {
          if (oldDate == null) {
            idx++;
            labels.put(String.valueOf(idx), sdf.format(wd.workDate));
            oldDate = wd.workDate;
            Logger.debug("[ DATE ] " + sdf.format(wd.workDate));
          }
          else {
            if (oldDate.compareTo(wd.workDate) != 0) {
              datas.put(String.valueOf(idx), MathU.round(todayDatas, 2));
              mixdatas.put(String.valueOf(idx), MathU.round(totalDatas, 2));
              idx++;
              labels.put(String.valueOf(idx), sdf.format(wd.workDate));
              Logger.debug("[ DATE  SANPU ] " + todayDatas);
              Logger.debug("[ TOTAL SANPU ] " + totalDatas);
              oldDate = wd.workDate;
              todayDatas = 0;
            }
          }
          List<WorkDiarySanpu> wdss = WorkDiarySanpu.getWorkDiarySanpuList(wd.workDiaryId);
          for (WorkDiarySanpu wdsd : wdss) {
            double hosei = 1;
            Nouhi nouhi = Nouhi.getNouhiInfo(wdsd.nouhiId);
            //グラフはKgとLに無条件補正する
//            if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
//              hosei = 0.001;
//            }
            hosei = 0.001;
            totalDatas += (wdsd.sanpuryo * hosei);
            todayDatas += (wdsd.sanpuryo * hosei);
          }
        }
        datas.put(String.valueOf(idx), MathU.round(todayDatas, 2));
        mixdatas.put(String.valueOf(idx), MathU.round(totalDatas, 2));
        Logger.debug("[ DATE  SANPU ] " + todayDatas);
        Logger.debug("[ TOTAL SANPU ] " + totalDatas);
        kJ.put("disinfectionLabel"    , labels);
        kJ.put("disinfectionData"     , datas);
        kJ.put("disinfectionMixData"  , mixdatas);
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 潅水量
        //----------------------------------------------------------------------------------------------------------------------------------
        long kansuiCount = 0;
        if (compartmentStatusData.finalKansuiDate != null && (compartmentStatusData.finalKansuiDate.compareTo(nullDate) != 0)) {

          Date finalKansuiDate = DateUtils.truncate(compartmentStatusData.finalKansuiDate, Calendar.DAY_OF_MONTH);

          kJ.put("finalKansuiDate"       , compartmentStatusData.finalKansuiDate.toString());

          kansuiCount = DateU.GetDiffDate(finalKansuiDate, systemDate);

        }
        else {
          kJ.put("finalKansuiDate"       , "");
          kansuiCount = 0;
        }

        kJ.put("kansuiCount"          , kansuiCount);                                       //最終潅水日からの経過日数
        kJ.put("totalKansuiNumber"    , df.format(compartmentStatusData.totalKansuiNumber));//合計潅水量
        kJ.put("totalKansuiCount"     , compartmentStatusData.totalKansuiCount);            //合計潅水回数
        //グラフデータの作成
        List<Double> kansuiKey = new ArrayList<Double>();
        for (WorkChainItem wci : wcis) {
          Work wk = Work.getWork(wci.workId);
          if (wk.workTemplateId == AgryeelConst.WorkTemplate.KANSUI) {
            kansuiKey.add(wk.workId);
          }
        }
        if (year != 0 && rotation != 0) {
          wds   = WorkDiary.find.where().eq("kukaku_id", compartmentData.kukakuId).in("work_id", kansuiKey).between("work_start_time", tStart, tEnd).orderBy("work_date").findList();
        }
        else {
          wds   = WorkDiary.find.where().eq("kukaku_id", compartmentData.kukakuId).in("work_id", kansuiKey).between("work_start_time", tStart, tsystem).orderBy("work_date").findList();
        }
        labels    = Json.newObject();
        datas     = Json.newObject();
        mixdatas  = Json.newObject();
        totalDatas = 0;
        todayDatas = 0;
        oldDate = null;
        idx = 0;
        for (WorkDiary wd : wds) {
          if (oldDate == null) {
            idx++;
            labels.put(String.valueOf(idx), sdf.format(wd.workDate));
            oldDate = wd.workDate;
            Logger.debug("[ DATE ] " + sdf.format(wd.workDate));
          }
          else {
            if (oldDate.compareTo(wd.workDate) != 0) {
              datas.put(String.valueOf(idx), todayDatas);
              mixdatas.put(String.valueOf(idx), totalDatas);
              idx++;
              labels.put(String.valueOf(idx), sdf.format(wd.workDate));
              Logger.debug("[ DATE  SANPU ] " + todayDatas);
              Logger.debug("[ TOTAL SANPU ] " + totalDatas);
              oldDate = wd.workDate;
              todayDatas = 0;
            }
          }
          totalDatas += wd.kansuiRyo;
          todayDatas += wd.kansuiRyo;
        }
        datas.put(String.valueOf(idx), todayDatas);
        mixdatas.put(String.valueOf(idx), totalDatas);
        Logger.debug("[ DATE  SANPU ] " + todayDatas);
        Logger.debug("[ TOTAL SANPU ] " + totalDatas);
        kJ.put("kansuiLabel"    , labels);
        kJ.put("kansuiData"     , datas);
        kJ.put("kansuiMixData"  , mixdatas);

        //----------------------------------------------------------------------------------------------------------------------------------
        //- 追肥
        //----------------------------------------------------------------------------------------------------------------------------------
        long tuihiCount = 0;
        if (compartmentStatusData.finalTuihiDate != null && (compartmentStatusData.finalTuihiDate.compareTo(nullDate) != 0)) {

          Date finalTuihiDate = DateUtils.truncate(compartmentStatusData.finalTuihiDate, Calendar.DAY_OF_MONTH);

          kJ.put("finalTuihiDate"       , compartmentStatusData.finalTuihiDate.toString());

          tuihiCount = DateU.GetDiffDate(finalTuihiDate, systemDate);

        }
        else {
          kJ.put("finalTuihiDate"       , "");
          tuihiCount = 0;
        }

        kJ.put("tuihiCount"          , tuihiCount);                                       //最終追肥日からの経過日数
        kJ.put("totalTuihiNumber"    , df.format(compartmentStatusData.totalTuihiNumber * 0.001));//合計追肥量
        kJ.put("totalTuihiCount"     , compartmentStatusData.totalTuihiCount);            //合計追肥回数

        //グラフデータの作成
        List<Double> tuihiKey = new ArrayList<Double>();
        for (WorkChainItem wci : wcis) {
          Work wk = Work.getWork(wci.workId);
          if (wk.workTemplateId == AgryeelConst.WorkTemplate.SANPU
              && wci.nouhiKind == AgryeelConst.NouhiKind.HIRYO) {
            tuihiKey.add(wk.workId);
          }
        }
        if (year != 0 && rotation != 0) {
          wds   = WorkDiary.find.where().eq("kukaku_id", compartmentData.kukakuId).in("work_id", tuihiKey).between("work_start_time", tStart, tEnd).orderBy("work_date").findList();
        }
        else {
          wds   = WorkDiary.find.where().eq("kukaku_id", compartmentData.kukakuId).in("work_id", tuihiKey).between("work_start_time", tStart, tsystem).orderBy("work_date").findList();
        }
        labels    = Json.newObject();
        datas     = Json.newObject();
        mixdatas  = Json.newObject();
        totalDatas = 0;
        todayDatas = 0;
        oldDate = null;
        idx = 0;
        for (WorkDiary wd : wds) {
          if (!(compartmentStatusData.hashuDate != null && compartmentStatusData.hashuDate.compareTo(wd.workDate) <= 0)) {
            continue;
          }
          if (oldDate == null) {
            idx++;
            labels.put(String.valueOf(idx), sdf.format(wd.workDate));
            oldDate = wd.workDate;
            Logger.debug("[ DATE ] " + sdf.format(wd.workDate));
          }
          else {
            if (oldDate.compareTo(wd.workDate) != 0) {
              datas.put(String.valueOf(idx), todayDatas);
              mixdatas.put(String.valueOf(idx), totalDatas);
              idx++;
              labels.put(String.valueOf(idx), sdf.format(wd.workDate));
              Logger.debug("[ DATE  SANPU ] " + todayDatas);
              Logger.debug("[ TOTAL SANPU ] " + totalDatas);
              oldDate = wd.workDate;
              todayDatas = 0;
            }
          }
          List<WorkDiarySanpu> wdss = WorkDiarySanpu.getWorkDiarySanpuList(wd.workDiaryId);
          for (WorkDiarySanpu wdsd : wdss) {
            double hosei = 1;
            Nouhi nouhi = Nouhi.getNouhiInfo(wdsd.nouhiId);
            if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //単位種別がKgかLの場合
              hosei = 0.001;
            }
            totalDatas += (wdsd.sanpuryo * hosei);
            todayDatas += (wdsd.sanpuryo * hosei);
          }
        }
        datas.put(String.valueOf(idx), todayDatas);
        mixdatas.put(String.valueOf(idx), totalDatas);
        Logger.debug("[ DATE  SANPU ] " + todayDatas);
        Logger.debug("[ TOTAL SANPU ] " + totalDatas);
        kJ.put("tuihiLabel"    , labels);
        kJ.put("tuihiData"     , datas);
        kJ.put("tuihiMixData"  , mixdatas);

        //----------------------------------------------------------------------------------------------------------------------------------
        //- 収穫
        //----------------------------------------------------------------------------------------------------------------------------------
        if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {

          kJ.put("shukakuStartDate"       , compartmentStatusData.shukakuStartDate.toString());

        }
        else {
          kJ.put("shukakuStartDate"       , "");
        }
        if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {

          kJ.put("shukakuEndDate"       , compartmentStatusData.shukakuEndDate.toString());

        }
        else {
          kJ.put("shukakuEndDate"       , "");
        }

        kJ.put("totalShukakuNumber"    , df.format(compartmentStatusData.totalShukakuCount));        //合計収穫量
        if ((compartmentStatusData.totalShukakuCount == 0)
            || (compartmentData.area == 0)) {
          kJ.put("tanshu"              , "*****");
        }
        else {
          kJ.put("tanshu"              , df.format((compartmentStatusData.totalShukakuCount / compartmentData.area) * 10));
        }
        //グラフデータの作成
        List<Double> shukakuKey = new ArrayList<Double>();
        for (WorkChainItem wci : wcis) {
          Work wk = Work.getWork(wci.workId);
          if (wk.workTemplateId == AgryeelConst.WorkTemplate.SHUKAKU ||
              wk.workTemplateId == AgryeelConst.WorkTemplate.SENKA) {
            shukakuKey.add(wk.workId);
          }
        }
        if (year != 0 && rotation != 0) {
          wds   = WorkDiary.find.where().eq("kukaku_id", compartmentData.kukakuId).in("work_id", shukakuKey).between("work_start_time", tStart, tEnd).orderBy("work_date").findList();
        }
        else {
          wds   = WorkDiary.find.where().eq("kukaku_id", compartmentData.kukakuId).in("work_id", shukakuKey).between("work_start_time", tStart, tsystem).orderBy("work_date").findList();
        }
        labels    = Json.newObject();
        datas     = Json.newObject();
        mixdatas  = Json.newObject();
        totalDatas = 0;
        todayDatas = 0;
        oldDate = null;
        idx = 0;
        for (WorkDiary wd : wds) {
          if (oldDate == null) {
            idx++;
            labels.put(String.valueOf(idx), sdf.format(wd.workDate));
            oldDate = wd.workDate;
            Logger.debug("[ DATE ] " + sdf.format(wd.workDate));
          }
          else {
            if (oldDate.compareTo(wd.workDate) != 0) {
              datas.put(String.valueOf(idx), todayDatas);
              mixdatas.put(String.valueOf(idx), totalDatas);
              idx++;
              labels.put(String.valueOf(idx), sdf.format(wd.workDate));
              Logger.debug("[ DATE  SANPU ] " + todayDatas);
              Logger.debug("[ TOTAL SANPU ] " + totalDatas);
              oldDate = wd.workDate;
              todayDatas = 0;
            }
          }
          totalDatas += wd.shukakuRyo;
          todayDatas += wd.shukakuRyo;
        }
        datas.put(String.valueOf(idx), todayDatas);
        mixdatas.put(String.valueOf(idx), totalDatas);
        Logger.debug("[ DATE  SANPU ] " + todayDatas);
        Logger.debug("[ TOTAL SANPU ] " + totalDatas);
        kJ.put("shukakuLabel"    , labels);
        kJ.put("shukakuData"     , datas);
        kJ.put("shukakuMixData"  , mixdatas);

        ObjectNode chainJson = Json.newObject();
        ArrayNode chainJsonApi = mapper.createArrayNode();
        if (year != 0 && rotation != 0) {
          if(api){
            csc.getWorkChainStatusFromMotochoJsonArray(compartmentData.kukakuId, compartmentStatusData, chainJsonApi);
          }else{
            csc.getWorkChainStatusFromMotochoJson(compartmentData.kukakuId, compartmentStatusData, chainJson);
          }
        }
        else {
          if(api){
            csc.getWorkChainStatusJsonArray(compartmentData.kukakuId, chainJsonApi);
          }else{
            csc.getWorkChainStatusJson(compartmentData.kukakuId, chainJson);
          }
        }
        if(api){
          kJ.put("chain", chainJsonApi);
        }else{
          kJ.put("chain", chainJson);
        }

        ObjectNode  aj   = Json.newObject();                                                              //作業中アカウント情報
        ArrayNode ajApi  = mapper.createArrayNode();
        UserComprtnent uc = new UserComprtnent();
        if(api){
          uc.getNowWorkingByFieldArray(compartmentData.kukakuId, ajApi);
          kJ.put("working", ajApi);
        }else{
          uc.getNowWorkingByField(compartmentData.kukakuId, aj);
          kJ.put("working", aj);
        }

        //----------------------------------------------------------------------------------------------------------------------------------
        //- 農薬情報の取得
        //----------------------------------------------------------------------------------------------------------------------------------
        SimpleDateFormat sdfN = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdfK = new SimpleDateFormat("yyyyMMdd");
        DecimalFormat    dfN  = new DecimalFormat("##0.000");
        DecimalFormat    dfK  = new DecimalFormat("00000000");
        List<MotochoNouyaku> nouyakus = MotochoNouyaku.find.where().eq("kukaku_id", compartmentStatusData.kukakuId)
                                                                   .eq("work_year", compartmentStatusData.workYear)
                                                                   .eq("rotation_speed_of_year", compartmentStatusData.rotationSpeedOfYear)
                                                                   .orderBy("nouyaku_no asc,sanpu_date asc")
                                                                   .findList();
        for (MotochoNouyaku motocho : nouyakus) {
          ObjectNode nj         = Json.newObject();
          nj.put("key"        , "n" + dfK.format(motocho.nouyakuNo));
          nj.put("color"      , AgryeelConst.ColorData[(int)(motocho.nouyakuNo % 10)]);
          nj.put("no"         , "殺虫" + motocho.nouyakuNo);
          nj.put("date"       , sdfN.format(motocho.sanpuDate));
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {

            nj.put("diffdate"       , DateU.GetDiffDate(motocho.sanpuDate, compartmentStatusData.shukakuStartDate));

          }
          else {
            nj.put("diffdate"       , DateU.GetDiffDate(motocho.sanpuDate, systemDate));
          }
          nj.put("nouhiName"  , motocho.nouhiName);
          nj.put("bairitu"    , motocho.bairitu + "倍");
          nj.put("method"     , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motocho.sanpuMethod));
          nj.put("ryo"        , dfN.format(motocho.sanpuryo / 1000)  + " " + motocho.unit);
          nouyakuListJson.put("n" + dfK.format(motocho.nouyakuNo) + sdfK.format(motocho.sanpuDate), nj);
          nouyakuListJsonApi.add(nj);
        }
        //----------------------------------------------------------------------------------------------------------------------------------
        //- 肥料情報の取得
        //----------------------------------------------------------------------------------------------------------------------------------
        List<MotochoHiryo> hiryous = MotochoHiryo.find.where().eq("kukaku_id", compartmentStatusData.kukakuId)
                                                              .eq("work_year", compartmentStatusData.workYear)
                                                              .eq("rotation_speed_of_year", compartmentStatusData.rotationSpeedOfYear)
                                                              .orderBy("hiryo_no asc,sanpu_date asc")
                                                              .findList();
        int motogoe = 0;
        int tuihi   = 0;
        double n  = 0;
        double p  = 0;
        double k  = 0;
        double mg = 0;
        for (MotochoHiryo motocho : hiryous) {
          ObjectNode nj         = Json.newObject();
          if (!(compartmentStatusData.hashuDate != null && compartmentStatusData.hashuDate.compareTo(motocho.sanpuDate) <= 0)) {
            motogoe++;
            nj.put("no"     , "元肥" + motogoe);
          }
          else {
            tuihi++;
            nj.put("no"     , "追肥" + tuihi);
          }
          nj.put("key"        , "h" + dfK.format(motocho.hiryoNo));
          nj.put("color"      , AgryeelConst.ColorData[(int)(motocho.hiryoNo % 10)]);
          nj.put("date"       , sdfN.format(motocho.sanpuDate));
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {

            nj.put("diffdate"       , DateU.GetDiffDate(motocho.sanpuDate, compartmentStatusData.shukakuStartDate));

          }
          else {
            nj.put("diffdate"       , DateU.GetDiffDate(motocho.sanpuDate, systemDate));
          }
          nj.put("nouhiName"  , motocho.nouhiName);
          nj.put("bairitu"    , motocho.bairitu + "倍");
          nj.put("method"     , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motocho.sanpuMethod));
          nj.put("ryo"        , dfN.format(motocho.sanpuryo / 1000)  + " " + motocho.unit);
          nj.put("n"          , "N&nbsp;:" + dfN.format(motocho.n * 0.001)  + " " + motocho.unit);
          nj.put("p"          , "P&nbsp;:" + dfN.format(motocho.p * 0.001)  + " " + motocho.unit);
          nj.put("k"          , "K&nbsp;:" + dfN.format(motocho.k * 0.001)  + " " + motocho.unit);
          nj.put("mg"         , "Mg:" + dfN.format(motocho.mg * 0.001) + " " + motocho.unit);
          n   += (motocho.n   * 0.001);
          p   += (motocho.p   * 0.001);
          k   += (motocho.k   * 0.001);
          mg  += (motocho.mg  * 0.001);

          hiryoListJson.put("h" + dfK.format(motocho.hiryoNo) + sdfK.format(motocho.sanpuDate), nj);
          hiryoListJsonApi.add(nj);
        }
        //総合計の生成
        ObjectNode nj         = Json.newObject();
        nj.put("no"     , "合 計");
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
        //- タイムラインの取得
        //----------------------------------------------------------------------------------------------------------------------------------
        /* アカウント情報からタイムライン情報を取得する */
        List<TimeLine> timeLine;
        if (year != 0 && rotation != 0) {
          timeLine = TimeLine.find.where().eq("kukaku_id", compartmentData.kukakuId).between("work_start_time", tStart, tEnd).orderBy("work_date desc, work_start_time desc").findList();
        }
        else {
          timeLine = TimeLine.find.where().eq("kukaku_id", compartmentData.kukakuId).between("work_start_time", tStart, tsystem).orderBy("work_date desc, work_start_time desc").findList();
        }

        SimpleDateFormat sdf2  = new SimpleDateFormat("MM.dd HH:mm");
        SimpleDateFormat sdf3 = new SimpleDateFormat("MM/dd");
        SimpleDateFormat sdft  = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        for (TimeLine timeLineData : timeLine) {                                        //作業情報をJSONデータに格納する

          WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

          if (workd == null) { continue; }

          ObjectNode timeLineJson         = Json.newObject();
          timeLineJson.put("timeLineId"   , timeLineData.timeLineId);                   //タイムラインID
          timeLineJson.put("workdate"     , sdf3.format(workd.workDate));               //作業日
          timeLineJson.put("updateTime"   , sdf2.format(timeLineData.updateTime));       //更新日時
          timeLineJson.put("message"      , StringU.setNullTrim(timeLineData.message)); //メッセージ
          timeLineJson.put("timeLineColor", timeLineData.timeLineColor);                //タイムラインカラー
          timeLineJson.put("workDiaryId"  , timeLineData.workDiaryId);                  //作業記録ID
          timeLineJson.put("workName"     , timeLineData.workName);                     //作業名
          timeLineJson.put("kukakuName"   , timeLineData.kukakuName);                   //区画名
          timeLineJson.put("accountName"  , timeLineData.accountName);                  //アカウント名
          timeLineJson.put("workId"       , timeLineData.workId);                       //作業ＩＤ
          timeLineJson.put("worktime"     , workd.workTime);                            //作業時間
          timeLineJson.put("workDiaryId"  , workd.workDiaryId);                         //ＩＤ
          timeLineJson.put("kukakuId"     , timeLineData.kukakuId);                     //区画ＩＤ
          Compartment cpt = Compartment.getCompartmentInfo(timeLineData.kukakuId);
          Logger.info("[TIME LINE]kukakuId={} fieldId={}", timeLineData.kukakuId, cpt.fieldId);
          timeLineJson.put("fieldId"      , cpt.fieldId);                               //圃場ID
          timeLineJson.put("fieldGroupId" , cpt.getFieldGroupInfo().fieldGroupId);      //圃場グループID

          timelineListJson.put(sdft.format(timeLineData.workStartTime), timeLineJson);
          timelineListJsonApi.add(timeLineJson);

        }

      }

      if(api){
        kJApi.add(kJ);
        resultJson.put("kukakuJson", kJApi);
        resultJson.put("targetTimeLine", timelineListJsonApi);
        resultJson.put("nouyakuList", nouyakuListJsonApi);
        resultJson.put("hiryoList"  , hiryoListJsonApi);
      }else{
        resultJson.put("kukakuJson", kJ);
        resultJson.put("targetTimeLine", timelineListJson);
        resultJson.put("nouyakuList", nouyakuListJson);
        resultJson.put("hiryoList"  , hiryoListJson);
      }
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    /**
     * 区画生産物変更時
     * @return
     */
    public static Result selectKukakuCrop() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode inputJson = request().body().asJson();
        double kukakuId  = inputJson.get("kukakuId").asDouble();

        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(kukakuId);

        CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();

        double cropId  = inputJson.get("cropId").asDouble();

        cws.cropId      = cropId;
        //初期化を行わない
//        cws.workChainId = 0;
//        cws.workEndId   = "";
        cws.update();

        Crop crop = CropComprtnent.getCropById(cws.cropId);
        if (crop != null) {
          resultJson.put("cropId"          , crop.cropId);
          resultJson.put("cropName"        , crop.cropName);
        }
        else {
          resultJson.put("cropId"          , "");
          resultJson.put("cropName"        , "");
        }

        WorkChain wc = WorkChain.getWorkChain(cws.workChainId);
        if (wc != null) {
          resultJson.put("workChainId"          , wc.workChainId);
          resultJson.put("workChainName"        , wc.workChainName);
        }
        else {
          resultJson.put("workChainId"          , "");
          resultJson.put("workChainName"        , "");
        }

        UserComprtnent uc = new UserComprtnent();
        int getAccount = uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        Compartment ct = Compartment.getCompartmentInfo(kukakuId);

        Logger.info("[ KUKAKUCROP CHANGED ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} CROPID:{} CROPNAME:{}", uc.accountData.accountId, uc.accountData.acountName, ct.kukakuId, ct.kukakuName, crop.cropId, crop.cropName);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
    /**
     * 区画ワークチェーン変更時
     * @return
     */
    public static Result selectKukakuWorkChain() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode inputJson = request().body().asJson();
        double kukakuId  = inputJson.get("kukakuId").asDouble();

        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(kukakuId);

        CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();

        double workchainId  = inputJson.get("workchainId").asDouble();

        cws.workChainId = workchainId;
        cws.workEndId   = "";
        cws.update();

        WorkChain wc = WorkChain.getWorkChain(cws.workChainId);
        if (wc != null) {
          resultJson.put("workChainId"          , wc.workChainId);
          resultJson.put("workChainName"        , wc.workChainName);
        }
        else {
          resultJson.put("workChainId"          , "");
          resultJson.put("workChainName"        , "");
        }

        UserComprtnent uc = new UserComprtnent();
        int getAccount = uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        Compartment ct = Compartment.getCompartmentInfo(kukakuId);

        Logger.info("[ WORKCHAIN CHANGED ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKCHAINID:{} WORKCHAINNAME:{}", uc.accountData.accountId, uc.accountData.acountName, ct.kukakuId, ct.kukakuName, wc.workChainId, wc.workChainName);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
    /**
     * 複数区画生産物変更時
     * @return
     */
    public static Result selectMultiKukakuCrop() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode  resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode inputJson  = request().body().asJson();
        String sKukakuId    = inputJson.get("kukakuId").asText();
        String[] kukakuIds  = sKukakuId.split(",");

        UserComprtnent uc = new UserComprtnent();
        int getAccount = uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        double dKukakuId = 0;

        for (String id : kukakuIds) {

          if (id != null && !"".equals(id)) {
            double kukakuId = Double.parseDouble(id);
            CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(kukakuId);

            CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();

            double cropId  = inputJson.get("cropId").asDouble();

            cws.cropId      = cropId;
            cws.update();

            Crop crop = CropComprtnent.getCropById(cws.cropId);
            if (crop != null) {
              resultJson.put("cropId"          , crop.cropId);
              resultJson.put("cropName"        , crop.cropName);
            }
            else {
              resultJson.put("cropId"          , "");
              resultJson.put("cropName"        , "");
            }

            Compartment ct = Compartment.getCompartmentInfo(kukakuId);

            Logger.info("[ HASHU KUKAKUCROP CHANGED ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} CROPID:{} CROPNAME:{}", uc.accountData.accountId, uc.accountData.acountName, ct.kukakuId, ct.kukakuName, crop.cropId, crop.cropName);

            if (dKukakuId == 0) {
              dKukakuId = ct.kukakuId;
            }
          }

          resultJson.put("kukakuId"        , dKukakuId);

        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
