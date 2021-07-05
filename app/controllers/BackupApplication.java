package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class BackupApplication extends Controller {

    /**
     * 【AGRYEEL】トップページ
     * @return インデックスレンダー
     */
    public static Result index() {
        return ok(views.html.index.render(""));
    }

//    /**
//     * 【AGRYEEL】トップページ
//     * @return インデックスレンダー
//     */
//    public static Result getMotocho() {
//
//        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */
//        JsonNode motochoInput 				= request().body().asJson();												/* 元帳照会パラメータ */
//        ObjectNode 	listJson  				= Json.newObject();															/* リストJSONオブジェクト */
//        DecimalFormat dfWorkYear			= new DecimalFormat("0000");												/* 作業年フォーマット */
//        DecimalFormat dfrotationSpeedOfYear	= new DecimalFormat("0000");												/* 年内回転数フォーマット */
//        String		sInitKey				= "";																		/* 初回表示キー */
//
//        /* 区画IDで元帳情報を表示する */
//        List<MotochoBase> motochoBaseList = MotochoBase.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
//                                                       .orderBy().desc("work_year").orderBy().desc("rotation_speed_of_year").findList();
//
//        if (motochoBaseList.size() > 0) { 																				/* 元帳基本情報が存在する場合 */
//            Compartment compartmentData = Compartment.find.where()
//                            .eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
//                            .eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();	/* 区画情報を取得する */
//
//            if (compartmentData == null ) { return notFound(); }														/* 区画情報が存在しない場合 */
//
//            CompartmentGroup compartmentGroupData = CompartmentGroup.find.where()
//                    .eq("kukaku_group_id", compartmentData.kukakuGroupId).findUnique();									/* 区画グループ情報を取得する */
//
//            /*-------------------------------------------------------------------------------------------------------*/
//            /*- 元帳基本情報の生成                                                                                  -*/
//            /*-------------------------------------------------------------------------------------------------------*/
//            for (MotochoBase motochobaseData : motochoBaseList) {														/* 元帳基本情報分処理を行う */
//                ObjectNode 	motochobaseJson  = Json.newObject();														/* 元帳基本情報JSONオブジェクトの生成 */
//
//                System.out.println("" + motochobaseData.workYear + " " + motochobaseData.rotationSpeedOfYear);
//
//                motochobaseJson.put("workYear", motochobaseData.workYear);												/* 作業年 */
//                motochobaseJson.put("rotationSpeedOfYear", motochobaseData.rotationSpeedOfYear);						/* 年内回転数 */
//                motochobaseJson.put("kukakuName", motochobaseData.kukakuName);											/* 区画名 */
//                motochobaseJson.put("cropName", motochobaseData.cropName);												/* 生産物 */
//                motochobaseJson.put("hinsyuName", motochobaseData.hinsyuName);											/* 品種名 */
//                motochobaseJson.put("hashuDate", StringU.dateFormat("yyyy/MM/dd", motochobaseData.hashuDate));			/* 播種日 */
//                motochobaseJson.put("seiikuDayCount", motochobaseData.seiikuDayCount);									/* 生育日数 */
//                motochobaseJson.put("shukakuRyo", motochobaseData.shukakuRyo);											/* 収穫量 */
//                motochobaseJson.put("shukakuStartDate"
//                                    , StringU.dateFormat("yyyy/MM/dd", motochobaseData.shukakuStartDate));				/* 収穫開始日 */
//                motochobaseJson.put("shukakuEndDate", StringU.dateFormat("yyyy/MM/dd", motochobaseData.shukakuEndDate));/* 収穫終了日 */
//
//                motochobaseJson.put("kukakuGroupName", compartmentGroupData.kukakuGroupName.substring(0, 1));			/* 区画グループ名 */
//                motochobaseJson.put("kukakuGroupColor", compartmentGroupData.kukakuGroupColor);							/* 区画グループカラー */
//
//                /*----------------------------------------------------------------------------------------------------*/
//                /*- 元帳農薬情報の生成                                                                               -*/
//                /*----------------------------------------------------------------------------------------------------*/
//                List<MotochoNouyaku> motochoNouyakuList = MotochoNouyaku.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
//                                                               .eq("work_year", motochobaseData.workYear).eq("rotation_speed_of_year", motochobaseData.rotationSpeedOfYear)
//                                                               .orderBy("sanpu_date asc, nouyaku_no asc").findList();
//
//                ObjectNode 	nouyakuListJson 			= Json.newObject();												/* 農薬リストJSONオブジェクト */
//
//                for (MotochoNouyaku motochoNouyakuData : motochoNouyakuList) {											/* 農薬リスト分処理を行う */
//                    ObjectNode 	motochoNouyakuJson  = Json.newObject();													/* 元帳農薬情報JSONオブジェクトの生成 */
//                    motochoNouyakuJson.put("nouyakuNo", "殺虫" + motochoNouyakuData.nouyakuNo);							/* 農薬番号 */
//
//                    motochoNouyakuJson.put("nouhiName", motochoNouyakuData.nouhiName);									/* 農肥名 */
//                    motochoNouyakuJson.put("bairitu", motochoNouyakuData.bairitu);										/* 倍率 */
//                    motochoNouyakuJson.put("sanpuDate", StringU.dateFormat("yyyy/MM/dd", motochoNouyakuData.sanpuDate));/* 散布日 */
//                    motochoNouyakuJson.put("sanpuryo", motochoNouyakuData.sanpuryo);									/* 散布量 */
//                    motochoNouyakuJson.put("sanpuMethod"
//                            , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motochoNouyakuData.sanpuMethod));	/* 散布方法 */
//                    motochoNouyakuJson.put("yushiSeibun", motochoNouyakuData.yushiSeibun);								/* 有支成分 */
//                    motochoNouyakuJson.put("gUnitValue", motochoNouyakuData.gUnitValue);								/* 10ｇ当り */
//
//                    nouyakuListJson.put(Double.toString(motochoNouyakuData.nouyakuNo), motochoNouyakuJson);				/* 農薬リストに格納 */
//
//                }
//
//                motochobaseJson.put("nouyakuList", nouyakuListJson);													/* 農薬リストを格納 */
//
//                /*----------------------------------------------------------------------------------------------------*/
//                /*- 元帳肥料情報の生成                                                                               -*/
//                /*----------------------------------------------------------------------------------------------------*/
//                List<MotochoHiryo> motochoHiryoList = MotochoHiryo.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
//                                                               .eq("work_year", motochobaseData.workYear).eq("rotation_speed_of_year", motochobaseData.rotationSpeedOfYear)
//                                                               .orderBy("sanpu_date asc, hiryo_no asc").findList();
//
//                ObjectNode 	hiryoListJson 			= Json.newObject();													/* 肥料リストJSONオブジェクト */
//
//                for (MotochoHiryo motochoHiryoData : motochoHiryoList) {												/* 肥料リスト分処理を行う */
//                    ObjectNode 	motochoHiryoJson  = Json.newObject();													/* 元帳肥料情報JSONオブジェクトの生成 */
//                    motochoHiryoJson.put("nouyakuNo", "元肥" + motochoHiryoData.hiryoNo);								/* 農薬番号 */
//
//                    motochoHiryoJson.put("nouhiName", motochoHiryoData.nouhiName);										/* 農肥名 */
//                    motochoHiryoJson.put("bairitu", motochoHiryoData.bairitu);											/* 倍率 */
//                    motochoHiryoJson.put("sanpuDate", StringU.dateFormat("yyyy/MM/dd", motochoHiryoData.sanpuDate));	/* 散布日 */
//                    motochoHiryoJson.put("sanpuryo", motochoHiryoData.sanpuryo);										/* 散布量 */
//                    motochoHiryoJson.put("sanpuMethod"
//                            , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motochoHiryoData.sanpuMethod));		/* 散布方法 */
//                    motochoHiryoJson.put("yushiSeibun", motochoHiryoData.yushiSeibun);									/* 有支成分 */
//                    motochoHiryoJson.put("gUnitValue", motochoHiryoData.gUnitValue);									/* 10ｇ当り */
//
//                    nouyakuListJson.put(Double.toString(motochoHiryoData.hiryoNo), motochoHiryoJson);					/* 農薬リストに格納 */
//
//                }
//
//                motochobaseJson.put("hiryoList", hiryoListJson);														/* 農薬リストを格納 */
//
//
//                listJson.put(dfWorkYear.format(motochobaseData.workYear)
//                        + dfrotationSpeedOfYear.format(motochobaseData.rotationSpeedOfYear)	, motochobaseJson);
//
//                if (sInitKey.equals("")) {																				/* 初回表示KEYが未設定の場合 */
//                    sInitKey = dfWorkYear.format(motochobaseData.workYear)
//                            + dfrotationSpeedOfYear.format(motochobaseData.rotationSpeedOfYear);						/* 初回表示KEYを設定 */
//                }
//
//            }
//
//        }
//        else {
//            return notFound();																							/* 元帳基本情報が存在しない場合 */
//        }
//
//        resultJson.put(AgryeelConst.Motocho.MOTOCHOBASE	, listJson);
//        resultJson.put(AgryeelConst.Motocho.INITKEY		, sInitKey);
//
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】プッシュ通知機能
//     * @return プッシュ通知結果
//     */
//    public static Result getPushUpdate() {
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode resultJson = Json.newObject();
//
//        Sequence sequence 		= Sequence.GetSequenceNowValue(Sequence.SequenceIdConst.TIMELINEID);
//        double oldSequenceValue	= sequence.sequenceValue;
//
//        while (oldSequenceValue	== sequence.sequenceValue) {
//            try {
//
//                Thread.sleep(1000);
//
//            } catch (InterruptedException e) {
//
//            }
//            sequence 		= Sequence.GetSequenceNowValue(Sequence.SequenceIdConst.TIMELINEID);
//        }
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】アカウント認証
//     * @return アカウント認証結果JSON
//     */
//    public static Result accountLogin() {
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode resultJson = Json.newObject();
//
//        /* JSONデータを取得 */
//        JsonNode accountInpput = request().body().asJson();
//
//        /* アカウント情報よりデータを取得する */
//        List<Account> accountInfo = Account.find.where().eq("account_id", accountInpput.get("accountId").asText()).findList();
//
//        if (accountInfo.size() == 0) { 																						//アカウント情報が存在しない場合
//            resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
//        }
//        else {
//            Account account = accountInfo.get(0);																			//アカウント情報を取り出す
//            if (!StringU.setNullTrim(account.password).equals(accountInpput.get("password").asText().toString())) {			//パスワードが不一致
//                resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.PASSWORDUNMATCH);
//            }
//            else {																											//アカウントログイン成功
//                session(AgryeelConst.SessionKey.ACCOUNTID, StringU.setNullTrim(account.accountId));							//アカウントIDをセッションに格納
//                session(AgryeelConst.SessionKey.ACCOUNTNAME, StringU.setNullTrim(account.acountName));						//アカウント名をセッションに格納
//                session(AgryeelConst.SessionKey.ACCOUNTID_SEL, StringU.setNullTrim(account.accountId));						//アカウントID（アカウント情報選択用）をセッションに格納
//                session(AgryeelConst.SessionKey.FARMID, String.valueOf(account.farmId));									//農場IDをセッションに格納
//                session(AgryeelConst.SessionKey.FARMBASEID, String.valueOf(account.farmId));								//農場基本IDをセッションに格納
//
//                /*----- 農場情報を取得する -----*/
//                Farm farm = Farm.find.where().eq("farm_id", account.farmId).findUnique();									//農場を検索
//                if (farm != null) { //農場が取得できた場合
//                    session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(farm.farmGroupId));							//農場グループIDをセッションに格納
//                }
//                else {
//                    session(AgryeelConst.SessionKey.FARMGROUPID, String.valueOf(0));										//農場グループIDをセッションに格納
//                }
//
//                resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//            }
//        }
//
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】メニュー画面遷移
//     * @return メニュー画面レンダー
//     */
//    public static Result menuMove() {
//        return ok(views.html.menu.render(""));
//    }
//
//    /**
//     * 【AGRYEEL】メニュー初期表示データ取得
//     * @return メニュー初期表示結果JSON
//     */
//    public static Result menuInit() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//        ObjectNode 	listJson   = Json.newObject();
//
//        /*----- ハウス状況照会 -----*/
//        /* アカウント情報から作業対象区画情報を取得する */
//        List<WorkCompartment> workCompartment = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).findList();
//
//        /* 区画IDから区画状況情報を取得する */
//        List<Double> aryKukakuId = new ArrayList<Double>();						//検索条件 区画ID
//        for (WorkCompartment workCompartmentData : workCompartment) {			//検索条件を生成する
//            if (workCompartmentData.workTarget == 1) {							//作業対象圃場の場合のみ
//                aryKukakuId.add(workCompartmentData.kukakuId);
//            }
//        }
//
//        List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id").findList();
//
//        for (CompartmentStatus compartmentStatusData : compartmentStatus) {													//区画状況情報をJSONデータに格納する
//
//            /* 区画IDから区画情報を取得する */
//            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();
//            if (compartmentData == null) { continue; }																		//区画情報が存在しない場合、データを作成しない
//
//            ObjectNode compartmentStatusJson	= Json.newObject();
//            compartmentStatusJson.put("kukakuId"				, compartmentStatusData.kukakuId);							//区画ID
//            compartmentStatusJson.put("kukakuName"				, compartmentData.kukakuName);								//区画名
//            compartmentStatusJson.put("rotationSpeedOfYear"		, compartmentStatusData.rotationSpeedOfYear);				//年内回転数
//            compartmentStatusJson.put("hinsyuName"				, compartmentStatusData.hinsyuName);						//品種名
//            compartmentStatusJson.put("hashuDate"				, compartmentStatusData.hashuDate.toString());				//播種日
//            compartmentStatusJson.put("seiikuDayCount"			, compartmentStatusData.seiikuDayCount);					//生育日数
//            compartmentStatusJson.put("nowEndWork"				, compartmentStatusData.nowEndWork);						//現在完了作業
//            compartmentStatusJson.put("finalDisinfectionDate"	, compartmentStatusData.finalDisinfectionDate.toString());	//最終消毒日
//            compartmentStatusJson.put("finalKansuiDate"			, compartmentStatusData.finalKansuiDate.toString());		//最終潅水日
//            compartmentStatusJson.put("finalTuihiDate"			, compartmentStatusData.finalTuihiDate.toString());			//最終追肥日
//            compartmentStatusJson.put("shukakuStartDate"		, compartmentStatusData.shukakuStartDate.toString());		//収穫開始日
//            compartmentStatusJson.put("shukakuEndDate"			, compartmentStatusData.shukakuEndDate.toString());			//収穫終了日
//            compartmentStatusJson.put("totalDisinfectionCount"	, compartmentStatusData.totalDisinfectionCount);			//合計消毒量
//            compartmentStatusJson.put("totalKansuiCount"		, compartmentStatusData.totalKansuiCount);					//合計潅水量
//            compartmentStatusJson.put("totalTuihiCount"			, compartmentStatusData.totalTuihiCount);					//合計追肥量
//            compartmentStatusJson.put("totalShukakuCount"		, compartmentStatusData.totalShukakuCount);					//合計収穫量
//            compartmentStatusJson.put("oldDisinfectionCount"	, compartmentStatusData.oldDisinfectionCount);				//去年消毒量
//            compartmentStatusJson.put("oldKansuiCount"			, compartmentStatusData.oldKansuiCount);					//去年潅水量
//            compartmentStatusJson.put("oldTuihiCount"			, compartmentStatusData.oldTuihiCount);						//去年追肥量
//            compartmentStatusJson.put("oldShukakuCount"			, compartmentStatusData.oldShukakuCount);					//去年収穫量
//            compartmentStatusJson.put("nowWorkMode"				, compartmentStatusData.nowWorkMode);						//去年収穫量
//
//            /* Clip情報を取得する */
//            ClipCompartment clipData = ClipCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", compartmentStatusData.kukakuId).findUnique(); /* 該当キーに合致するクリップ情報を取得 */
//            if (clipData == null) {										/* 該当クリップが存在しない場合、新規にモデルを作成する */
//                compartmentStatusJson.put(AgryeelConst.ClipGroup.CLIPRESULT	, AgryeelConst.ClipGroup.NONE);					//クリップOFF
//            }
//            else {														/* 該当クリップが存在する場合 */
//                compartmentStatusJson.put(AgryeelConst.ClipGroup.CLIPRESULT	, AgryeelConst.ClipGroup.EXISTS);				//クリップグループ
//            }
//
//            /* 進捗状況を生成する */
//            List<CompartmentWorkStatus> compartmentWorkStatus = CompartmentWorkStatus.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).orderBy("kukaku_id,work_id").findList();
//            ObjectNode 	listWorkJson	= Json.newObject();
//
//            for (CompartmentWorkStatus compartmentStatusWorkData : compartmentWorkStatus) {									//区画作業状況情報をJSONデータに格納する
//                ObjectNode statusWorkJson	= Json.newObject();
//
//                /* 作業IDから作業情報を取得する */
//                Work workData = Work.find.where().eq("work_id", compartmentStatusWorkData.workId).findUnique();
//                if (workData == null) { continue; }																			//作業情報が存在しない場合、データを作成しない
//
//                statusWorkJson.put("workId"		, compartmentStatusWorkData.workId);										//作業ID
//                statusWorkJson.put("workName"	, workData.workName);														//作業名
//                statusWorkJson.put("workMode"	, workData.workMode);														//作業モード
//                statusWorkJson.put("workEndFlag", compartmentStatusWorkData.workEndFlag);									//作業完了フラグ
//
//                listWorkJson.put(Double.toString(compartmentStatusWorkData.workId), statusWorkJson);						//作業進捗をリストに追加
//
//            }
//
//            compartmentStatusJson.put("compartmentWorkStatus"	, listWorkJson);											//区間作業状況情報
//
//            listJson.put(Double.toString(compartmentStatusData.kukakuId), compartmentStatusJson);
//
//        }
//
//        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJson);
//
//        /*----- 作業情報 -----*/
//
//        listJson   = Json.newObject();	//リスト形式JSONを初期化する
//
//        /* アカウント情報から農場別生産物情報を取得する */
//        List<CropOfFarm> cropOfFarm = CropOfFarm.find.where().eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findList();
//
//        /* 生産物IDから生産物作業情報を取得する */
//        List<Double> aryCropId = new ArrayList<Double>();	//検索条件 生産物ID
//
//        for (CropOfFarm cropOfFarmData : cropOfFarm) {		//検索条件を生成する
//            aryCropId.add(cropOfFarmData.cropId);
//        }
//
//        List<WorkOfCrop> workOfCrop = WorkOfCrop.find.where().in("cropId", aryCropId).findList();
//
//        /* 作業IDから作業情報を取得する */
//        List<Double> aryWorkId 	= new ArrayList<Double>();	//検索条件 作業ID
//
//        for (WorkOfCrop workOfCropData : workOfCrop) {		//検索条件を生成する
//            aryWorkId.add(workOfCropData.workId);
//        }
//
//        List<Work> work 			= Work.find.where().in("workId", aryWorkId).orderBy("workId").findList();
//
//        for (Work workData : work) {							//作業情報をJSONデータに格納する
//
//            ObjectNode workJson		= Json.newObject();
//            workJson.put("workId"	, workData.workId);			//作業ID
//            workJson.put("workName"	, workData.workName);		//作業名
//            workJson.put("workMode"	, workData.workMode);		//作業モード
//
//            /* 次回作業対象となる区画情報を取得する */
//            compartmentStatus = CompartmentStatus.find.where().eq("next_work_id", workData.workId).orderBy("kukaku_id").findList();
//
//            int kukakuCount = 0;								//区画件数
//            ObjectNode 	listKukakuJson	= Json.newObject();		//区画情報リスト
//
//            for (CompartmentStatus compartmentStatusData : compartmentStatus) {		//対象区画情報をJSONデータに格納する
//
//                /* 区画IDから区画情報を取得する */
//                Compartment compartmentData = Compartment.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).findUnique();
//                if (compartmentData == null) { continue; }	//区画情報が存在しない場合、データを作成しない
//
//                kukakuCount++;																//区画件数をカウントアップ
//
//                if (kukakuCount <= 5) {														//５件以下の場合
//                    ObjectNode workKukaku		= Json.newObject();	//作業対象区画情報
//
//                    workKukaku.put("kukakuId", compartmentData.kukakuId);						//区画ID
//                    workKukaku.put("kukakuName", compartmentData.kukakuName);					//区画名称
//                    listKukakuJson.put(Double.toString(compartmentData.kukakuId), workKukaku);	//区画情報を格納
//                }
//
//            }
//
//            workJson.put("workKukakuList"	, listKukakuJson);								//作業対象区画
//            workJson.put("workKukakuCount"	, kukakuCount);									//作業対象区画件数
//
//            listJson.put(Double.toString(workData.workId), workJson);
//
//        }
//
//        resultJson.put(AgryeelConst.WorkInfo.TARGETWORK, listJson);
//
//        /*----- タイムライン情報 -----*/
//
//        getTimeLineData(resultJson);
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】ハウス状況照会を取得する
//     * @return ハウス状況照会JSON
//     */
//    public static Result getCompartment() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//        ObjectNode 	listJson   = Json.newObject();
//
//        /*----- ハウス状況照会 -----*/
//        /* アカウント情報から作業対象区画情報を取得する */
//        List<WorkCompartment> workCompartment = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).findList();
//
//        /* 区画IDから区画状況情報を取得する */
//        List<Double> aryKukakuId = new ArrayList<Double>();						//検索条件 区画ID
//        for (WorkCompartment workCompartmentData : workCompartment) {			//検索条件を生成する
//            if (workCompartmentData.workTarget == 1) {							//作業対象圃場の場合のみ
//                aryKukakuId.add(workCompartmentData.kukakuId);
//            }
//        }
//
//        List<CompartmentGroup> compartmentGroup = CompartmentGroup.find.where().eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).orderBy("kukaku_group_id").findList();
//
//        List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id").findList();
//
//        for (CompartmentStatus compartmentStatusData : compartmentStatus) {													//区画状況情報をJSONデータに格納する
//
//            /* 区画IDから区画情報を取得する */
//            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();
//            if (compartmentData == null) { continue; }																		//区画情報が存在しない場合、データを作成しない
//
//            ObjectNode compartmentStatusJson	= Json.newObject();
//
//            compartmentStatusJson.put("kukakuGroupColor"		, "FFFFFF");												//区画グループカラーを初期化
//            for (CompartmentGroup compartmentGroupData : compartmentGroup) {
//                if (compartmentGroupData.kukakuGroupId == compartmentData.kukakuGroupId) {
//                    compartmentStatusJson.put("kukakuGroupColor"		, compartmentGroupData.kukakuGroupColor);			//区画グループカラー
//                    break;
//                }
//            }
//
//            compartmentStatusJson.put("kukakuId"				, compartmentStatusData.kukakuId);							//区画ID
//            compartmentStatusJson.put("kukakuName"				, compartmentData.kukakuName);								//区画名
//            compartmentStatusJson.put("rotationSpeedOfYear"		, compartmentStatusData.rotationSpeedOfYear);				//年内回転数
//            compartmentStatusJson.put("hinsyuName"				, compartmentStatusData.hinsyuName);						//品種名
//            compartmentStatusJson.put("hashuDate"				, compartmentStatusData.hashuDate.toString());				//播種日
//            compartmentStatusJson.put("seiikuDayCount"			, compartmentStatusData.seiikuDayCount);					//生育日数
//            compartmentStatusJson.put("nowEndWork"				, compartmentStatusData.nowEndWork);						//現在完了作業
//            compartmentStatusJson.put("workColor"				, compartmentStatusData.workColor);							//作業カラー
//            compartmentStatusJson.put("finalDisinfectionDate"	, compartmentStatusData.finalDisinfectionDate.toString());	//最終消毒日
//            compartmentStatusJson.put("finalKansuiDate"			, compartmentStatusData.finalKansuiDate.toString());		//最終潅水日
//            compartmentStatusJson.put("finalTuihiDate"			, compartmentStatusData.finalTuihiDate.toString());			//最終追肥日
//            compartmentStatusJson.put("shukakuStartDate"		, compartmentStatusData.shukakuStartDate.toString());		//収穫開始日
//            compartmentStatusJson.put("shukakuEndDate"			, compartmentStatusData.shukakuEndDate.toString());			//収穫終了日
//            compartmentStatusJson.put("totalDisinfectionCount"	, compartmentStatusData.totalDisinfectionCount);			//合計消毒量
//            compartmentStatusJson.put("totalKansuiCount"		, compartmentStatusData.totalKansuiCount);					//合計潅水量
//            compartmentStatusJson.put("totalTuihiCount"			, compartmentStatusData.totalTuihiCount);					//合計追肥量
//            compartmentStatusJson.put("totalShukakuCount"		, compartmentStatusData.totalShukakuCount);					//合計収穫量
//            compartmentStatusJson.put("oldDisinfectionCount"	, compartmentStatusData.oldDisinfectionCount);				//去年消毒量
//            compartmentStatusJson.put("oldKansuiCount"			, compartmentStatusData.oldKansuiCount);					//去年潅水量
//            compartmentStatusJson.put("oldTuihiCount"			, compartmentStatusData.oldTuihiCount);						//去年追肥量
//            compartmentStatusJson.put("oldShukakuCount"			, compartmentStatusData.oldShukakuCount);					//去年収穫量
//            compartmentStatusJson.put("nowWorkMode"				, compartmentStatusData.nowWorkMode);						//去年収穫量
//
//            /* Clip情報を取得する */
//            ClipCompartment clipData = ClipCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", compartmentStatusData.kukakuId).findUnique(); /* 該当キーに合致するクリップ情報を取得 */
//            if (clipData == null) {										/* 該当クリップが存在しない場合、新規にモデルを作成する */
//                compartmentStatusJson.put(AgryeelConst.ClipGroup.CLIPRESULT	, AgryeelConst.ClipGroup.NONE);					//クリップOFF
//            }
//            else {														/* 該当クリップが存在する場合 */
//                compartmentStatusJson.put(AgryeelConst.ClipGroup.CLIPRESULT	, AgryeelConst.ClipGroup.EXISTS);				//クリップグループ
//            }
//
//            /* 進捗状況を生成する */
//            List<CompartmentWorkStatus> compartmentWorkStatus = CompartmentWorkStatus.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).orderBy("kukaku_id,work_id").findList();
//            ObjectNode 	listWorkJson	= Json.newObject();
//
//            for (CompartmentWorkStatus compartmentStatusWorkData : compartmentWorkStatus) {									//区画作業状況情報をJSONデータに格納する
//                ObjectNode statusWorkJson	= Json.newObject();
//
//                /* 作業IDから作業情報を取得する */
//                Work workData = Work.find.where().eq("work_id", compartmentStatusWorkData.workId).findUnique();
//                if (workData == null) { continue; }																			//作業情報が存在しない場合、データを作成しない
//
//                statusWorkJson.put("workId"		, compartmentStatusWorkData.workId);										//作業ID
//                statusWorkJson.put("workName"	, workData.workName);														//作業名
//                statusWorkJson.put("workMode"	, workData.workMode);														//作業モード
//                statusWorkJson.put("workColor"	, workData.workColor);														//作業カラー
//                statusWorkJson.put("workEndFlag", compartmentStatusWorkData.workEndFlag);									//作業完了フラグ
//
//                listWorkJson.put(Double.toString(compartmentStatusWorkData.workId), statusWorkJson);						//作業進捗をリストに追加
//
//            }
//
//            compartmentStatusJson.put("compartmentWorkStatus"	, listWorkJson);											//区間作業状況情報
//
//            listJson.put(Double.toString(compartmentStatusData.kukakuId), compartmentStatusJson);
//
//        }
//
//        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJson);
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】作業情報を取得する
//     * @return 作業情報JSON
//     */
//    public static Result getWork() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//        ObjectNode 	listJson   = Json.newObject();
//
//        /*----- 作業情報 -----*/
//
//        listJson   = Json.newObject();	//リスト形式JSONを初期化する
//
//        /* アカウント情報から農場別生産物情報を取得する */
//        List<CropOfFarm> cropOfFarm = CropOfFarm.find.where().eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findList();
//
//        /* 生産物IDから生産物作業情報を取得する */
//        List<Double> aryCropId = new ArrayList<Double>();	//検索条件 生産物ID
//
//        for (CropOfFarm cropOfFarmData : cropOfFarm) {		//検索条件を生成する
//            aryCropId.add(cropOfFarmData.cropId);
//        }
//
//        List<WorkOfCrop> workOfCrop = WorkOfCrop.find.where().in("cropId", aryCropId).findList();
//
//        /* 作業IDから作業情報を取得する */
//        List<Double> aryWorkId 	= new ArrayList<Double>();	//検索条件 作業ID
//
//        for (WorkOfCrop workOfCropData : workOfCrop) {		//検索条件を生成する
//            aryWorkId.add(workOfCropData.workId);
//        }
//
//        List<Work> work 			= Work.find.where().in("workId", aryWorkId).orderBy("workId").findList();
//
//        for (Work workData : work) {							//作業情報をJSONデータに格納する
//
//            ObjectNode workJson		= Json.newObject();
//            workJson.put("workId"	, workData.workId);			//作業ID
//            workJson.put("workName"	, workData.workName);		//作業名
//            workJson.put("workMode"	, workData.workMode);		//作業モード
//
//            /* 次回作業対象となる区画情報を取得する */
//            List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().eq("next_work_id", workData.workId).orderBy("kukaku_id").findList();
//
//            int kukakuCount = 0;								//区画件数
//            ObjectNode 	listKukakuJson	= Json.newObject();		//区画情報リスト
//
//            for (CompartmentStatus compartmentStatusData : compartmentStatus) {		//対象区画情報をJSONデータに格納する
//
//                /* 区画IDから区画情報を取得する */
//                Compartment compartmentData = Compartment.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();
//                if (compartmentData == null) { continue; }	//区画情報が存在しない場合、データを作成しない
//
//                kukakuCount++;																//区画件数をカウントアップ
//
//                if (kukakuCount <= 5) {														//５件以下の場合
//                    ObjectNode workKukaku		= Json.newObject();	//作業対象区画情報
//
//                    workKukaku.put("kukakuId", compartmentData.kukakuId);						//区画ID
//                    workKukaku.put("kukakuName", compartmentData.kukakuName);					//区画名称
//                    listKukakuJson.put(Double.toString(compartmentData.kukakuId), workKukaku);	//区画情報を格納
//                }
//
//            }
//
//            workJson.put("workKukakuList"	, listKukakuJson);								//作業対象区画
//            workJson.put("workKukakuCount"	, kukakuCount);									//作業対象区画件数
//
//            listJson.put(Double.toString(workData.workId), workJson);
//
//        }
//
//        resultJson.put(AgryeelConst.WorkInfo.TARGETWORK, listJson);
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】最新のタイムライン
//     * @return タイムライン取得結果
//     */
//    public static Result getTimeLine() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//
//        getTimeLineData(resultJson);
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】最新のタイムラインをJsonオブジェクトに追加する
//     * @return タイムライン取得結果
//     */
//    public static void getTimeLineData(ObjectNode	resultJson) {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	listJson   = Json.newObject();
//
//        /*----- タイムライン情報 -----*/
//
//        listJson   = Json.newObject();	//リスト形式JSONを初期化する
//        /* アカウント情報からタイムライン情報を取得する */
//        List<TimeLine> timeLine = TimeLine.find.where().eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).orderBy("updateTime desc").orderBy("timeLineId desc").findList();
//        SimpleDateFormat sdf	= new SimpleDateFormat("MM.dd HH:mm");
//
//        for (TimeLine timeLineData : timeLine) {							//作業情報をJSONデータに格納する
//
//            ObjectNode timeLineJson		= Json.newObject();
//            timeLineJson.put("timeLineId"	, timeLineData.timeLineId);						//タイムラインID
//            timeLineJson.put("updateTime"	, sdf.format(timeLineData.updateTime));			//更新日時
//            timeLineJson.put("message"		, StringU.setNullTrim(timeLineData.message));	//メッセージ
//            timeLineJson.put("timeLineColor", timeLineData.timeLineColor);					//タイムラインカラー
//            timeLineJson.put("workDiaryId"	, timeLineData.workDiaryId);					//作業記録ID
//            timeLineJson.put("workName"		, timeLineData.workName);						//作業名
//            timeLineJson.put("kukakuName"	, timeLineData.kukakuName);						//区画名
//            timeLineJson.put("accountName"	, timeLineData.accountName);					//アカウント名
//
//            listJson.put(Double.toString(timeLineData.timeLineId), timeLineJson);
//
//        }
//
//        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
//    }
//
//    /**
//     * 【AGRYEEL】クリップ切替処理
//     * @return クリップ切替結果JSON
//     */
//    public static Result addClip() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//
//        /* JSONデータを取得 */
//        JsonNode jsonData = request().body().asJson();
//
//        /*----- クリップ圃場情報を取得する -----*/
//        String accountId 	= session(AgryeelConst.SessionKey.ACCOUNTID);	/* アカウントID */
//        String kukakuId		= jsonData.get("kukakuId").asText();			/* 区画ID */
//
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
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】作業対象圃場画面遷移
//     * @return 作業対象圃場レンダー
//     */
//    public static Result workTargetMove() {
//        return ok(views.html.worktarget.render(""));
//    }
//
//    /**
//     * 【AGRYEEL】作業対象圃場初期表示データ取得
//     * @return 作業対象圃場データJSON
//     */
//    public static Result workTargetInit() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//        ObjectNode 	listJson   = Json.newObject();
//
//        /*----- 作業対象圃場 -----*/
//        /* アカウント情報から作業対象区画情報を取得する */
//        List<WorkCompartment> workCompartment = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).findList();
//
//        /* 区画IDから区画状況情報を取得する */
//        List<Double> aryKukakuId = new ArrayList<Double>();						//検索条件 区画ID
//        for (WorkCompartment workCompartmentData : workCompartment) {			//検索条件を生成する
//            aryKukakuId.add(workCompartmentData.kukakuId);
//        }
//
//        List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id").findList();
//
//        for (CompartmentStatus compartmentStatusData : compartmentStatus) {		//区画状況情報をJSONデータに格納する
//
//            /* 区画IDから区画情報を取得する */
//            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();
//            if (compartmentData == null) { continue; }															//区画情報が存在しない場合、データを作成しない
//
//            /* 区画IDから作業対象区画情報を取得する */
//            WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", compartmentStatusData.kukakuId).findUnique();
//
//            ObjectNode compartmentStatusJson	= Json.newObject();
//            compartmentStatusJson.put("kukakuId"			, compartmentStatusData.kukakuId);					//区画ID
//            compartmentStatusJson.put("kukakuName"			, compartmentData.kukakuName);						//区画名
//            compartmentStatusJson.put("rotationSpeedOfYear"	, compartmentStatusData.rotationSpeedOfYear);		//年内回転数
//            compartmentStatusJson.put("hinsyuName"			, compartmentStatusData.hinsyuName);				//品種名
//            compartmentStatusJson.put("workTarget"			, workCompartmentData.workTarget);					//作業対象フラグ
//            listJson.put(Double.toString(compartmentStatusData.kukakuId), compartmentStatusJson);
//
//        }
//
//        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJson);
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】農場グループデータ取得
//     * @return 農場データJSON
//     */
//    public static Result getGroupFarmList() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//        ObjectNode 	listJson   = Json.newObject();
//
//        /*----- 作業対象圃場 -----*/
//        /* アカウント情報から作業対象区画情報を取得する */
//        List<Farm> farm = Farm.find.where().eq("farm_group_id", Double.valueOf(session(AgryeelConst.SessionKey.FARMGROUPID))).findList();
//
//        for (Farm farmData : farm) {		//農場情報をJSONデータに格納する
//
//            ObjectNode farmJson	= Json.newObject();
//            farmJson.put("farmId"			, farmData.farmId);					//農場ID
//            farmJson.put("farmName"			, farmData.farmName);				//農場名
//            listJson.put(Double.toString(farmData.farmId), farmJson);
//
//        }
//
//        resultJson.put(AgryeelConst.FarmInfo.FARMLIST, listJson);
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】農場切替時
//     * @return JSON
//     */
//    public static Result farmChange() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//
//        /* JSONデータを取得 */
//        JsonNode inpput = request().body().asJson();
//
//        session(AgryeelConst.SessionKey.FARMID, String.valueOf(inpput.get("farmId").asText()));									//農場IDをセッションに格納
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】作業対象圃場更新処理
//     * @return 作業対象圃場データ更新結果JSON
//     */
//    public static Result workTargetUpdate() {
//
//        /* 戻り値用JSONデータの生成 */
//        ObjectNode 	resultJson = Json.newObject();
//
//        /* JSONデータを取得 */
//        JsonNode jsonData = request().body().asJson();
//        double kukakuId = Double.parseDouble(jsonData.get("kukakuId").asText());
//
//        /* 区画IDから作業対象区画情報を取得する */
//        //WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("kukaku_id", jsonData.get("kukakuId").asText()).findUnique();
//        WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", kukakuId).findUnique();
//
//        /* 作業対象区画情報の作業対象フラグを更新する */
//        if(workCompartmentData.workTarget == 0){
//            workCompartmentData.workTarget = 1;
//        }else{
//            workCompartmentData.workTarget = 0;
//        }
//        workCompartmentData.update();
//
//        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
//
//        return ok(resultJson);
//    }
//
//    /**
//     * 【AGRYEEL】アカウント作成画面遷移
//     * @return アカウント作成画面レンダー
//     */
//    public static Result accountMakeMove() {
//        return ok(views.html.accountMake.render(""));
//    }
}
