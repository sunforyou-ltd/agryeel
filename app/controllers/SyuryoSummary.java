package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Compartment;
import models.SriKukakuD;
import models.SriKukakuM;
import models.SriKukakuS;
import models.SriKukakuY;
import models.WorkCompartment;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SessionCheckComponent;

import consts.AgryeelConst;

public class SyuryoSummary extends Controller {

    @Security.Authenticated(SessionCheckComponent.class)
    public static Result syuryoSummaryMove() {
        return ok(views.html.syuryoSummary.render(""));
    }

    /**
     * 【AGRYEEL】収量まとめデータ取得
     * @return 収量まとめデータ取得結果JSON
     */
    public static Result syuryoSummaryGet() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();
        ObjectNode	syuryoSummaryJson	= Json.newObject();

        /* JSONデータを取得 */
        JsonNode jsonData = request().body().asJson();
        int disp = Integer.parseInt(jsonData.get("disp").asText());			//表示画面
        int place = Integer.parseInt(jsonData.get("place").asText());		//検索条件場所
        int unit = Integer.parseInt(jsonData.get("unit").asText());			//検索条件単位
        int selYear = Integer.parseInt(jsonData.get("selyear").asText());	//指定年
        int selMonth = Integer.parseInt(jsonData.get("selmonth").asText());	//指定月
        int selDay = Integer.parseInt(jsonData.get("selday").asText());		//指定日

        /*----- 収量まとめ -----*/
        /* アカウント情報から作業対象区画情報を取得する */
        List<WorkCompartment> workCompartment = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).findList();

        /* 区画IDから区画状況情報を取得する */
        List<Double> aryKukakuId = new ArrayList<Double>();						//検索条件 区画ID
        for (WorkCompartment workCompartmentData : workCompartment) {			//検索条件を生成する
            if (workCompartmentData.workTarget == 1) {							//作業対象圃場の場合のみ
                aryKukakuId.add(workCompartmentData.kukakuId);
            }
        }

        /* 画面検索条件より対象収量まとめ情報取得 */
        /*----- 一覧表 -----*/
    	int unitCnt = 0;
        if(disp == AgryeelConst.SyuryoSummary.DisplayInfo.LIST){
            /*----- ①①場所別作付単位 -----*/
	        if(place == AgryeelConst.SyuryoSummary.SearchPlaceInfo.KUKAKU &&
	           unit == AgryeelConst.SyuryoSummary.SearchUnitInfo.SAKUDUKE){
	        	/* 現在年の取得 */
	        	Calendar calendar = Calendar.getInstance();
	        	int nowYear = calendar.get(Calendar.YEAR);

		        List<SriKukakuS> sriKukakuS = SriKukakuS.find.where().in("kukaku_id", aryKukakuId).eq("work_year", nowYear).orderBy("kukaku_id,rotation_speed_of_year").findList();

		        for (SriKukakuS sriKukakuSData : sriKukakuS) {													//区画状況情報をJSONデータに格納する
		            /* 区画IDから区画情報を取得する */
		            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", sriKukakuSData.kukakuId).findUnique();
		            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない

		            syuryoSummaryJson	= Json.newObject();
		            syuryoSummaryJson.put("placeId"					, sriKukakuSData.kukakuId);							//区画ID
		            syuryoSummaryJson.put("placeName"				, compartmentData.kukakuName);						//区画名
		            syuryoSummaryJson.put("workYear"				, sriKukakuSData.workYear);							//作業年
		            syuryoSummaryJson.put("rotationSpeedOfYear"		, sriKukakuSData.rotationSpeedOfYear);				//年内回転数
		            syuryoSummaryJson.put("totalShukakuCount"		, sriKukakuSData.totalShukakuCount);				//合計収穫量

		            //【AICA】TODO:クリップの見直し
//		            /* Clip情報を取得する */
//		            if(disp == AgryeelConst.SyuryoSummary.DisplayInfo.LIST){
//			            ClipCompartment clipData = ClipCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", sriKukakuSData.kukakuId).findUnique(); /* 該当キーに合致するクリップ情報を取得 */
//			            if (clipData == null) {										/* 該当クリップが存在しない場合、新規にモデルを作成する */
//			            	syuryoSummaryJson.put(AgryeelConst.ClipGroup.CLIPRESULT	, AgryeelConst.ClipGroup.NONE);					//クリップOFF
//			            }
//			            else {														/* 該当クリップが存在する場合 */
//			            	syuryoSummaryJson.put(AgryeelConst.ClipGroup.CLIPRESULT	, AgryeelConst.ClipGroup.EXISTS);				//クリップグループ
//			            }
//		            }
//		            listJson.put(Double.toString(sriKukakuSData.kukakuId) + Integer.toString(sriKukakuSData.workYear) + Integer.toString(sriKukakuSData.rotationSpeedOfYear), syuryoSummaryJson);

		            // 最大作付数の取得
		            if(sriKukakuSData.rotationSpeedOfYear != 99 &&
		               unitCnt < sriKukakuSData.rotationSpeedOfYear){
		            	unitCnt = sriKukakuSData.rotationSpeedOfYear;
		            }
		        }
	        }
            /*----- ①②場所別年単位 -----*/
	        else if(place == AgryeelConst.SyuryoSummary.SearchPlaceInfo.KUKAKU &&
	        		unit == AgryeelConst.SyuryoSummary.SearchUnitInfo.YEAR){
	        	/* 現在年の取得 */
	        	int maxYear = 0;
	        	int minYear = 9999;
	        	int kukakuMin = 0;

		        List<SriKukakuY> sriKukakuY = SriKukakuY.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id,work_year").findList();

		        double preKukakuId = 0;
		        for (SriKukakuY sriKukakuYData : sriKukakuY) {													//区画状況情報をJSONデータに格納する
		            /* 区画IDから区画情報を取得する */
		            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", sriKukakuYData.kukakuId).findUnique();
		            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない

		            syuryoSummaryJson	= Json.newObject();
		            // 区画毎の最小年を保存
		            if(preKukakuId == 0 || preKukakuId != sriKukakuYData.kukakuId)
		            {
		            	kukakuMin = sriKukakuYData.workYear;
		            	preKukakuId = sriKukakuYData.kukakuId;
		            }
		            syuryoSummaryJson.put("placeId"					, sriKukakuYData.kukakuId);							//区画ID
		            syuryoSummaryJson.put("placeName"				, compartmentData.kukakuName);						//区画名
		            syuryoSummaryJson.put("workYear"				, sriKukakuYData.workYear);							//作業年
		            syuryoSummaryJson.put("totalShukakuCount"		, sriKukakuYData.totalShukakuCount);				//合計収穫量
		            syuryoSummaryJson.put("minYear"					, kukakuMin);										//区画毎最小年

		            listJson.put(Double.toString(sriKukakuYData.kukakuId) + Integer.toString(sriKukakuYData.workYear), syuryoSummaryJson);

		            // 全体最大年数の取得
		            if(sriKukakuYData.workYear != 2999 &&
		            	maxYear < sriKukakuYData.workYear){
		            	maxYear = sriKukakuYData.workYear;
		            }

		            // 全体最小年数の取得
		            if(sriKukakuYData.workYear != 2099 &&
		            	minYear > sriKukakuYData.workYear){
		            	minYear = sriKukakuYData.workYear;
		            }
		        }

	            // 保存年数の取得
	            unitCnt = maxYear - minYear;
	            if(unitCnt == 0){
	            	unitCnt = 1;
	            }

	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMIN, minYear);
	        }
            /*----- ①③場所別月単位 -----*/
	        else if(place == AgryeelConst.SyuryoSummary.SearchPlaceInfo.KUKAKU &&
	        		unit == AgryeelConst.SyuryoSummary.SearchUnitInfo.MONTH){
	        	int maxYear = 0;
	        	int minYear = 9999;

	        	/* 現在日付の取得 */
	        	Calendar calendar = Calendar.getInstance();
	        	int nowYear = calendar.get(Calendar.YEAR);

	        	// 選択年が0の場合、現在年データを取得
	        	if(selYear == 0){
	        		selYear = nowYear;
	        	}

		        List<SriKukakuM> sriKukakuM = SriKukakuM.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id,work_year_month").findList();

		        for (SriKukakuM sriKukakuMData : sriKukakuM) {													//区画状況情報をJSONデータに格納する
		            /* 区画IDから区画情報を取得する */
		            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", sriKukakuMData.kukakuId).findUnique();
		            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない

		            // 作業年月の取得
		            String workYear = String.valueOf(sriKukakuMData.workYearMonth).substring(0,4);
		            String workDate = String.valueOf(sriKukakuMData.workYearMonth).replace("-", "");

		            // 保存最大年の取得
		            String workDay = workDate.substring(6);
		            int iYear = Integer.parseInt(workYear);
		            int iDay = Integer.parseInt(workDay);
		            if(iDay == 1 &&
		               maxYear < iYear){
		            	maxYear = iYear;
		            }

		            // 保存最小年月の取得
		            if(iDay == 1 &&
		               minYear > iYear){
		            	minYear = iYear;

		            }

		            if(Integer.parseInt(workYear) != selYear) { continue; }										//対象年ではない場合、データを作成しない

		            syuryoSummaryJson	= Json.newObject();
		            syuryoSummaryJson.put("placeId"					, sriKukakuMData.kukakuId);							//区画ID
		            syuryoSummaryJson.put("placeName"				, compartmentData.kukakuName);						//区画名
		            syuryoSummaryJson.put("workYearMonth"			, workDate);										//作業年月
		            syuryoSummaryJson.put("totalShukakuCount"		, sriKukakuMData.totalShukakuCount);				//合計収穫量

		            listJson.put(Double.toString(sriKukakuMData.kukakuId) + workDate, syuryoSummaryJson);

		        }

	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMAX, maxYear);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMIN, minYear);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKYEAR, nowYear);
	        }
            /*----- ①④場所別日単位 -----*/
	        else if(place == AgryeelConst.SyuryoSummary.SearchPlaceInfo.KUKAKU &&
	        		unit == AgryeelConst.SyuryoSummary.SearchUnitInfo.DAY){
	        	int maxYearMonth = 0;
	        	int minYearMonth = 999999;

	        	/* 現在日付の取得 */
	        	Calendar calendar = Calendar.getInstance();
	        	int nowYear = calendar.get(Calendar.YEAR);
	        	int nowMonth = calendar.get(Calendar.MONTH) + 1;
		        //String nowYearMonth = new SimpleDateFormat("yyyyMM").format(calendar.getTime());

	        	// 選択年月が0の場合、現在年月データを取得
	        	if(selYear == 0 || selMonth == 0){
	        		selYear = nowYear;
	        		selMonth = nowMonth;
	        	}

	        	// 末日の取得
	        	calendar.set(Calendar.YEAR, selYear);
	        	calendar.set(Calendar.MONTH, selMonth - 1);
	        	int lastDayOfMonth = calendar.getActualMaximum(Calendar.DATE);

		        List<SriKukakuD> sriKukakuD = SriKukakuD.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id,work_date").findList();

		        for (SriKukakuD sriKukakuDData : sriKukakuD) {													//区画状況情報をJSONデータに格納する
		            /* 区画IDから区画情報を取得する */
		            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", sriKukakuDData.kukakuId).findUnique();
		            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない

		            // 作業年月の取得
		            String workDate = String.valueOf(sriKukakuDData.workDate).replace("-", "");
		            String workYear = workDate.substring(0,4);
		            String workMonth = workDate.substring(4,6);

		            // 保存最大年月の取得
		            int iYearMonth = Integer.parseInt(workYear + workMonth);
		            int iChk = Integer.parseInt(workYear.substring(0,2));
		            if(iChk != 25 &&
		            	maxYearMonth < iYearMonth){
		            	maxYearMonth = iYearMonth;
		            }

		            // 保存最小年月の取得
		            if(minYearMonth > iYearMonth){
		            	minYearMonth = iYearMonth;

		            }

		            if((Integer.parseInt(workYear) != selYear &&
		                Integer.parseInt(workMonth) != selMonth) ||
		                (Integer.parseInt(workYear) != selYear + 500 &&
		                Integer.parseInt(workMonth) != selMonth)
		               ) { continue; }										//対象年月ではない場合、データを作成しない

		            syuryoSummaryJson	= Json.newObject();
		            syuryoSummaryJson.put("placeId"					, sriKukakuDData.kukakuId);							//区画ID
		            syuryoSummaryJson.put("placeName"				, compartmentData.kukakuName);						//区画名
		            syuryoSummaryJson.put("workDate"				, workDate);										//作業日付
		            syuryoSummaryJson.put("totalShukakuCount"		, sriKukakuDData.totalShukakuCount);				//合計収穫量

		            listJson.put(Double.toString(sriKukakuDData.kukakuId) + workDate, syuryoSummaryJson);

		        }

	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMAX, maxYearMonth);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMIN, minYearMonth);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKYEAR, nowYear);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKMONTH, nowMonth);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.LASTDAY, lastDayOfMonth);
	        }
    	}
        /*----- グラフ -----*/
        else if(disp == AgryeelConst.SyuryoSummary.DisplayInfo.GRAPH){
            /*----- ①①場所別作付単位 -----*/
	        if(place == AgryeelConst.SyuryoSummary.SearchPlaceInfo.KUKAKU &&
	           unit == AgryeelConst.SyuryoSummary.SearchUnitInfo.SAKUDUKE){
	        	/* 現在年の取得 */
	        	Calendar calendar = Calendar.getInstance();
	        	int nowYear = calendar.get(Calendar.YEAR);

		        List<SriKukakuS> sriKukakuS = SriKukakuS.find.where().in("kukaku_id", aryKukakuId).eq("work_year", nowYear).orderBy("rotation_speed_of_year,kukaku_id").findList();

		        for (SriKukakuS sriKukakuSData : sriKukakuS) {													//区画状況情報をJSONデータに格納する
		            /* 区画IDから区画情報を取得する */
		            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", sriKukakuSData.kukakuId).findUnique();
		            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない

		            syuryoSummaryJson	= Json.newObject();
		            syuryoSummaryJson.put("placeId"					, sriKukakuSData.kukakuId);							//区画ID
		            syuryoSummaryJson.put("placeName"				, compartmentData.kukakuName);						//区画名
		            syuryoSummaryJson.put("workYear"				, sriKukakuSData.workYear);							//作業年
		            syuryoSummaryJson.put("rotationSpeedOfYear"		, sriKukakuSData.rotationSpeedOfYear);				//年内回転数
		            syuryoSummaryJson.put("totalShukakuCount"		, sriKukakuSData.totalShukakuCount);				//合計収穫量

		            listJson.put(Double.toString(sriKukakuSData.kukakuId) + Integer.toString(sriKukakuSData.workYear) + Integer.toString(sriKukakuSData.rotationSpeedOfYear), syuryoSummaryJson);

		            // 最大作付数の取得
		            if(sriKukakuSData.rotationSpeedOfYear != 99 &&
		               unitCnt < sriKukakuSData.rotationSpeedOfYear){
		            	unitCnt = sriKukakuSData.rotationSpeedOfYear;
		            }
		        }
	        }
            /*----- ①②場所別年単位 -----*/
	        else if(place == AgryeelConst.SyuryoSummary.SearchPlaceInfo.KUKAKU &&
	        		unit == AgryeelConst.SyuryoSummary.SearchUnitInfo.YEAR){
	        	/* 現在年の取得 */
	        	Calendar calendar = Calendar.getInstance();
	        	int nowYear = calendar.get(Calendar.YEAR);

	        	// 選択年が0の場合、現在年データを取得
	        	if(selYear == 0){
	        		selYear = nowYear;
	        	}

	        	int maxYear = 0;
	        	int minYear = 9999;
	        	int kukakuMin = 0;

		        List<SriKukakuY> sriKukakuY = SriKukakuY.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id,work_year").findList();

		        double preKukakuId = 0;
		        for (SriKukakuY sriKukakuYData : sriKukakuY) {													//区画状況情報をJSONデータに格納する
		            /* 区画IDから区画情報を取得する */
		            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", sriKukakuYData.kukakuId).findUnique();
		            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない

		            syuryoSummaryJson	= Json.newObject();
		            // 区画毎の最小年を保存
		            if(preKukakuId == 0 || preKukakuId != sriKukakuYData.kukakuId)
		            {
		            	kukakuMin = sriKukakuYData.workYear;
		            	preKukakuId = sriKukakuYData.kukakuId;
		            }
		            syuryoSummaryJson.put("placeId"					, sriKukakuYData.kukakuId);							//区画ID
		            syuryoSummaryJson.put("placeName"				, compartmentData.kukakuName);						//区画名
		            syuryoSummaryJson.put("workYear"				, sriKukakuYData.workYear);							//作業年
		            syuryoSummaryJson.put("totalShukakuCount"		, sriKukakuYData.totalShukakuCount);				//合計収穫量
		            syuryoSummaryJson.put("minYear"					, kukakuMin);										//区画毎最小年

		            listJson.put(Double.toString(sriKukakuYData.kukakuId) + Integer.toString(sriKukakuYData.workYear), syuryoSummaryJson);

		            // 全体最大年数の取得
		            if(sriKukakuYData.workYear != 2999 &&
		            	maxYear < sriKukakuYData.workYear){
		            	maxYear = sriKukakuYData.workYear;
		            }

		            // 全体最小年数の取得
		            if(sriKukakuYData.workYear != 2099 &&
		            	minYear > sriKukakuYData.workYear){
		            	minYear = sriKukakuYData.workYear;
		            }
		        }

	            // 保存年数の取得
	            unitCnt = maxYear - minYear;
	            if(unitCnt == 0){
	            	unitCnt = 1;
	            }

	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMIN, minYear);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMAX, maxYear);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKYEAR, nowYear);
	        }
            /*----- ①③場所別月単位 -----*/
	        else if(place == AgryeelConst.SyuryoSummary.SearchPlaceInfo.KUKAKU &&
	        		unit == AgryeelConst.SyuryoSummary.SearchUnitInfo.MONTH){
	        	int maxYearMonth = 0;
	        	int minYearMonth = 999999;

	        	/* 現在日付の取得 */
	        	Calendar calendar = Calendar.getInstance();
	        	int nowYear = calendar.get(Calendar.YEAR);
	        	int nowMonth = calendar.get(Calendar.MONTH) + 1;

	        	// 選択年が0の場合、現在年データを取得
	        	if(selYear == 0){
	        		selYear = nowYear;
	        	}

		        List<SriKukakuM> sriKukakuM = SriKukakuM.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id,work_year_month").findList();

		        for (SriKukakuM sriKukakuMData : sriKukakuM) {													//区画状況情報をJSONデータに格納する
		            /* 区画IDから区画情報を取得する */
		            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", sriKukakuMData.kukakuId).findUnique();
		            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない

		            // 作業年月の取得
		            String workYear = String.valueOf(sriKukakuMData.workYearMonth).substring(0,4);
		            String workDate = String.valueOf(sriKukakuMData.workYearMonth).replace("-", "");
		            String workMonth = workDate.substring(4,6);

		            // 保存最大年月の取得
		            String workDay = workDate.substring(6);
		            int iDay = Integer.parseInt(workDay);
		            int iYearMonth = Integer.parseInt(workYear + workMonth);
		            if(iDay == 1 &&
		            	maxYearMonth < iYearMonth){
		            	maxYearMonth = iYearMonth;
		            }

		            // 保存最小年月の取得
		            if(iDay == 1 &&
		               minYearMonth > iYearMonth){
		               minYearMonth = iYearMonth;

		            }

		            if(Integer.parseInt(workYear) != selYear ||
		                Integer.parseInt(workMonth) != selMonth ||
		                iDay != 1)
		            	 { continue; }										//対象年月ではない場合、データを作成しない

		            syuryoSummaryJson	= Json.newObject();
		            syuryoSummaryJson.put("placeId"					, sriKukakuMData.kukakuId);							//区画ID
		            syuryoSummaryJson.put("placeName"				, compartmentData.kukakuName);						//区画名
		            syuryoSummaryJson.put("workYearMonth"			, workYear + workMonth);							//作業年月
		            syuryoSummaryJson.put("totalShukakuCount"		, sriKukakuMData.totalShukakuCount);				//合計収穫量

		            listJson.put(Double.toString(sriKukakuMData.kukakuId) + workDate, syuryoSummaryJson);

		        }

	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMAX, maxYearMonth);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMIN, minYearMonth);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKYEAR, nowYear);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKMONTH, nowMonth);
	        }
            /*----- ①④場所別日単位 -----*/
	        else if(place == AgryeelConst.SyuryoSummary.SearchPlaceInfo.KUKAKU &&
	        		unit == AgryeelConst.SyuryoSummary.SearchUnitInfo.DAY){
	        	int maxYearMonth = 0;
	        	int minYearMonth = 999999;

	        	/* 現在日付の取得 */
	        	Calendar calendar = Calendar.getInstance();
	        	int nowYear = calendar.get(Calendar.YEAR);
	        	int nowMonth = calendar.get(Calendar.MONTH) + 1;
	        	int nowDay = calendar.get(Calendar.DATE);
		        //String nowYearMonth = new SimpleDateFormat("yyyyMM").format(calendar.getTime());

	        	// 選択年月が0の場合、現在年月データを取得
	        	if(selYear == 0 || selMonth == 0 || selDay == 0){
	        		selYear = nowYear;
	        		selMonth = nowMonth;
	        		selDay = nowDay;
	        	}

	        	// 末日の取得
	        	int lastDayOfMonth = 0;
	        	if(selYear != nowYear || selMonth != nowMonth){
	        		calendar.set(Calendar.YEAR, selYear);
	        		calendar.set(Calendar.MONTH, selMonth - 1);
	        		lastDayOfMonth = calendar.getActualMaximum(Calendar.DATE);
	        	}else{
	        		lastDayOfMonth = nowDay;			// 現在年月の場合当日を設定
	        	}

		        List<SriKukakuD> sriKukakuD = SriKukakuD.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id,work_date").findList();

		        for (SriKukakuD sriKukakuDData : sriKukakuD) {													//区画状況情報をJSONデータに格納する
		            /* 区画IDから区画情報を取得する */
		            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", sriKukakuDData.kukakuId).findUnique();
		            if (compartmentData == null) { continue; }													//区画情報が存在しない場合、データを作成しない

		            // 作業年月の取得
		            String workDate = String.valueOf(sriKukakuDData.workDate).replace("-", "");
		            String workYear = workDate.substring(0,4);
		            String workMonth = workDate.substring(4,6);
		            String workDay = workDate.substring(6);

		            // 保存最大年月の取得
		            int iYearMonth = Integer.parseInt(workYear + workMonth);
		            int iChk = Integer.parseInt(workYear.substring(0,2));
		            if(iChk != 25 &&
		            	maxYearMonth < iYearMonth){
		            	maxYearMonth = iYearMonth;
		            }

		            // 保存最小年月の取得
		            if(minYearMonth > iYearMonth){
		            	minYearMonth = iYearMonth;

		            }

		            if((Integer.parseInt(workYear) != selYear &&
		                Integer.parseInt(workMonth) != selMonth &&
		                Integer.parseInt(workDay) != selDay) ||
		                (Integer.parseInt(workYear) != selYear + 500 &&
		                Integer.parseInt(workMonth) != selMonth)
		               ) { continue; }										//対象年月ではない場合、データを作成しない

		            syuryoSummaryJson	= Json.newObject();
		            syuryoSummaryJson.put("placeId"					, sriKukakuDData.kukakuId);							//区画ID
		            syuryoSummaryJson.put("placeName"				, compartmentData.kukakuName);						//区画名
		            syuryoSummaryJson.put("workDate"				, workDate);										//作業日付
		            syuryoSummaryJson.put("totalShukakuCount"		, sriKukakuDData.totalShukakuCount);				//合計収穫量

		            listJson.put(Double.toString(sriKukakuDData.kukakuId) + workDate, syuryoSummaryJson);

		        }

	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMAX, maxYearMonth);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITMIN, minYearMonth);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKYEAR, nowYear);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKMONTH, nowMonth);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.WORKDAY, nowDay);
	            resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.LASTDAY, lastDayOfMonth);
	        }
        }

        resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.SYURYOSUMMARY, listJson);
        resultJson.put(AgryeelConst.SyuryoSummary.SyuryoSummaryInfo.UNITCNT, unitCnt);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }

}
