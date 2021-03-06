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
     * ???AGRYEEL?????????????????????
     * @return ??????????????????????????????
     */
    public static Result getMotocho() {

        ObjectNode resultJson 				= Json.newObject();															/* ????????????JSON?????????????????? */
        JsonNode motochoInput 				= request().body().asJson();												/* ??????????????????????????? */
        ObjectNode 	listJson  				= Json.newObject();															/* ?????????JSON?????????????????? */
        ObjectNode 	histryListJson  		= Json.newObject();															/* ???????????????JSON?????????????????? */
        DecimalFormat dfWorkYear			= new DecimalFormat("0000");												/* ??????????????????????????? */
        DecimalFormat dfrotationSpeedOfYear	= new DecimalFormat("0000");												/* ????????????????????????????????? */
        DecimalFormat dfnpk					= new DecimalFormat("0.000");												/* NPK?????????????????? */
        String		sInitKey				= "";																		/* ?????????????????? */
        SimpleDateFormat sdf				= new SimpleDateFormat("MM.dd HH:mm");

    	Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
    	Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

        /* ??????ID?????????????????????????????? */
        List<MotochoBase> motochoBaseList = MotochoBase.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
                                                       .orderBy().desc("work_year").orderBy().desc("rotation_speed_of_year").findList();

        if (motochoBaseList.size() > 0) { 																				/* ??????????????????????????????????????? */
            Compartment compartmentData = Compartment.find.where()
                            .eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
                            .eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();	/* ??????????????????????????? */

            if (compartmentData == null ) { return notFound(); }														/* ???????????????????????????????????? */

            //???AICA???TODO:??????/??????????????????
//            CompartmentGroup compartmentGroupData = CompartmentGroup.find.where()
//                    .eq("kukaku_group_id", compartmentData.kukakuGroupId).findUnique();									/* ??????????????????????????????????????? */

            /*-------------------------------------------------------------------------------------------------------*/
            /*- ???????????????????????????                                                                                  -*/
            /*-------------------------------------------------------------------------------------------------------*/
            for (MotochoBase motochobaseData : motochoBaseList) {														/* ???????????????????????????????????? */


                ObjectNode 	motochobaseJson  = Json.newObject();														/* ??????????????????JSON??????????????????????????? */
                ObjectNode 	histryJson  	 = Json.newObject();														/* ????????????JSON??????????????????????????? */

                motochobaseJson.put("workYear", motochobaseData.workYear);												/* ????????? */
                motochobaseJson.put("rotationSpeedOfYear", motochobaseData.rotationSpeedOfYear);						/* ??????????????? */
                motochobaseJson.put("kukakuName", motochobaseData.kukakuName);											/* ????????? */
                motochobaseJson.put("cropName", StringU.setNullTrim(motochobaseData.cropName));							/* ????????? */
                motochobaseJson.put("hinsyuName", StringU.setNullTrim(motochobaseData.hinsyuName));						/* ????????? */
                motochobaseJson.put("hashuDate", StringU.dateFormat("yyyy/MM/dd", motochobaseData.hashuDate));			/* ????????? */

                /*----- ??????????????????????????? -----*/
                String key 	= dfWorkYear.format(motochobaseData.workYear)
                            + dfrotationSpeedOfYear.format(motochobaseData.rotationSpeedOfYear);						/* KEY????????? */
                histryJson.put("key"	, key);
                histryJson.put("display", "????????????" + motochobaseData.workYear + "??? ????????????" + motochobaseData.rotationSpeedOfYear + "???");
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

                motochobaseJson.put("seiikuDayCount", seiikuDayCount);													/* ???????????? */
                motochobaseJson.put("shukakuRyo", motochobaseData.totalShukakuNumber);					/* ????????? */
                motochobaseJson.put("shukakuStartDate"
                                    , StringU.dateFormat("yyyy/MM/dd", motochobaseData.shukakuStartDate));				/* ??????????????? */
                motochobaseJson.put("shukakuEndDate", StringU.dateFormat("yyyy/MM/dd", motochobaseData.shukakuEndDate));/* ??????????????? */

                //???AICA???TODO:??????/??????????????????
//                motochobaseJson.put("kukakuGroupName", compartmentGroupData.kukakuGroupName.substring(0, 1));			/* ????????????????????? */
//                motochobaseJson.put("kukakuGroupColor", compartmentGroupData.kukakuGroupColor);							/* ??????????????????????????? */

                /*----------------------------------------------------------------------------------------------------*/
                /*- ???????????????????????????                                                                               -*/
                /*----------------------------------------------------------------------------------------------------*/
                List<MotochoNouyaku> motochoNouyakuList = MotochoNouyaku.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
                                                               .eq("work_year", motochobaseData.workYear).eq("rotation_speed_of_year", motochobaseData.rotationSpeedOfYear)
                                                               .orderBy("sanpu_date asc, nouyaku_no asc").findList();

                ObjectNode 	nouyakuListJson 			= Json.newObject();												/* ???????????????JSON?????????????????? */

                for (MotochoNouyaku motochoNouyakuData : motochoNouyakuList) {											/* ????????????????????????????????? */
                    ObjectNode 	motochoNouyakuJson  = Json.newObject();													/* ??????????????????JSON??????????????????????????? */
                    motochoNouyakuJson.put("nouyakuNo", "??????" + motochoNouyakuData.nouyakuNo);							/* ???????????? */

                    motochoNouyakuJson.put("nouhiName", motochoNouyakuData.nouhiName);									/* ????????? */
                    motochoNouyakuJson.put("bairitu", motochoNouyakuData.bairitu);										/* ?????? */
                    motochoNouyakuJson.put("sanpuDate", StringU.dateFormat("yyyy/MM/dd", motochoNouyakuData.sanpuDate));/* ????????? */
                    motochoNouyakuJson.put("sanpuryo", motochoNouyakuData.sanpuryo);									/* ????????? */
                    motochoNouyakuJson.put("sanpuMethod"
                            , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motochoNouyakuData.sanpuMethod));	/* ???????????? */
                    motochoNouyakuJson.put("yushiSeibun", motochoNouyakuData.yushiSeibun);								/* ???????????? */
                    motochoNouyakuJson.put("gUnitValue", motochoNouyakuData.gUnitValue);								/* 10????????? */
                    motochoNouyakuJson.put("diffDay"   , DateU.GetDiffDate(motochoNouyakuData.sanpuDate, systemDate));	/* ???????????? */


                    motochoNouyakuJson.put("n", dfnpk.format(motochoNouyakuData.n));									/* N */
                    motochoNouyakuJson.put("p", dfnpk.format(motochoNouyakuData.p));									/* P */
                    motochoNouyakuJson.put("k", dfnpk.format(motochoNouyakuData.k));									/* K */
                    motochoNouyakuJson.put("unit", motochoNouyakuData.unit);											/* ?????? */

                    nouyakuListJson.put(Double.toString(motochoNouyakuData.nouyakuNo) + StringU.dateFormat("yyyy/MM/dd"
                    								, motochoNouyakuData.sanpuDate), motochoNouyakuJson);				/* ???????????????????????? */


                }

                motochobaseJson.put("nouyakuList", nouyakuListJson);													/* ???????????????????????? */

                /*----------------------------------------------------------------------------------------------------*/
                /*- ???????????????????????????                                                                               -*/
                /*----------------------------------------------------------------------------------------------------*/
                List<MotochoHiryo> motochoHiryoList = MotochoHiryo.find.where().eq("kukaku_id", Double.parseDouble(motochoInput.get("kukakuId").asText()))
                                                               .eq("work_year", motochobaseData.workYear).eq("rotation_speed_of_year", motochobaseData.rotationSpeedOfYear)
                                                               .orderBy("sanpu_date asc, hiryo_no asc").findList();

                ObjectNode 	hiryoListJson 			= Json.newObject();													/* ???????????????JSON?????????????????? */

                for (MotochoHiryo motochoHiryoData : motochoHiryoList) {												/* ????????????????????????????????? */
                    ObjectNode 	motochoHiryoJson  = Json.newObject();													/* ??????????????????JSON??????????????????????????? */
                    motochoHiryoJson.put("nouyakuNo", "??????" + motochoHiryoData.hiryoNo);								/* ???????????? */

                    motochoHiryoJson.put("nouhiName", motochoHiryoData.nouhiName);										/* ????????? */
                    motochoHiryoJson.put("bairitu", motochoHiryoData.bairitu);											/* ?????? */
                    motochoHiryoJson.put("sanpuDate", StringU.dateFormat("yyyy/MM/dd", motochoHiryoData.sanpuDate));	/* ????????? */
                    motochoHiryoJson.put("sanpuryo", motochoHiryoData.sanpuryo);										/* ????????? */
                    motochoHiryoJson.put("sanpuMethod"
                            , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motochoHiryoData.sanpuMethod));		/* ???????????? */
                    motochoHiryoJson.put("yushiSeibun", motochoHiryoData.yushiSeibun);									/* ???????????? */
                    motochoHiryoJson.put("gUnitValue", motochoHiryoData.gUnitValue);									/* 10????????? */
                    motochoHiryoJson.put("diffDay"   , DateU.GetDiffDate(motochoHiryoData.sanpuDate, systemDate));		/* ???????????? */

                    motochoHiryoJson.put("n", dfnpk.format(motochoHiryoData.n));										/* N */
                    motochoHiryoJson.put("p", dfnpk.format(motochoHiryoData.p));										/* P */
                    motochoHiryoJson.put("k", dfnpk.format(motochoHiryoData.k));										/* K */
                    motochoHiryoJson.put("unit", motochoHiryoData.unit);												/* ?????? */

                    hiryoListJson.put(Double.toString(motochoHiryoData.hiryoNo) + StringU.dateFormat("yyyy/MM/dd", motochoHiryoData.sanpuDate)
                    														, motochoHiryoJson);						/* ???????????????????????? */

                }

                motochobaseJson.put("hiryoList", hiryoListJson);														/* ???????????????????????? */


                listJson.put(dfWorkYear.format(motochobaseData.workYear)
                        + dfrotationSpeedOfYear.format(motochobaseData.rotationSpeedOfYear)	, motochobaseJson);

                if (sInitKey.equals("")) {																				/* ????????????KEY????????????????????? */
                    sInitKey = dfWorkYear.format(motochobaseData.workYear)
                            + dfrotationSpeedOfYear.format(motochobaseData.rotationSpeedOfYear);						/* ????????????KEY????????? */
                }


                /*----------------------------------------------------------------------------------------------------*/
                /*- ?????????????????????????????????                                                                           -*/
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
                    motochoTimeLineJson.put("workDiaryId"	, workd.workDiaryId);							//??????

                    motochoTimeLineJson.put("kukakuName"	, timeLineData.kukakuName);						//?????????

                    timeLineJson.put(Double.toString(timeLineData.timeLineId), motochoTimeLineJson);

                }

                motochobaseJson.put("timeLineList", timeLineJson);														/* ???????????????????????????????????? */

            }

        }
        else {

            return notFound();																							/* ?????????????????????????????????????????? */

        }

        resultJson.put(AgryeelConst.Motocho.MOTOCHOBASE	, listJson);
        resultJson.put(AgryeelConst.Motocho.MOTOCHOYEAR	, histryListJson);
        resultJson.put(AgryeelConst.Motocho.INITKEY		, sInitKey);

        return ok(resultJson);
    }

    /**
     * ???AGRYEEL???????????????????????????
     * @return ?????????????????????
     */
    public static Result userLogout() {
        /* ????????????JSON?????????????????? */
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
     * ???AGRYEEL???????????????????????????
     * @return ????????????????????????
     */
    public static Result getPushUpdate() {
        /* ????????????JSON?????????????????? */
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
     * ???AGRYEEL???????????????????????????
     * @return ??????????????????????????????
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result menuMove() {
        return ok(views.html.menu.render(""));
    }

    /**
     * ???AGRYEEL???????????????????????????????????????
     * @return ?????????????????????JSON
     */
    public static Result getCompartment() {

        /* ????????????JSON?????????????????? */
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
          //----- ????????????????????? -----
          //----- ???????????? -----
          List<Double> aryMultiKukaku = new ArrayList<Double>();
          if (!StringU.nullcheck(accountComprtnent.accountStatusData.sskMultiKukaku)) { //????????????
            String[] sMultiKukakus = accountComprtnent.accountStatusData.sskMultiKukaku.split(",");
            for (String sMultiKukaku : sMultiKukakus) {
              aryMultiKukaku.add(Double.parseDouble(sMultiKukaku));
            }
          }
          //----- ???????????? -----
          List<Double> aryHinsyu = new ArrayList<Double>();
          if (!StringU.nullcheck(accountComprtnent.accountStatusData.sskHinsyu)) { //????????????
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
                  if (idx == 0 && fg.deleteFlag == 1) { // ?????????????????????
                    continue;
                  }
                  List<Field> fields = FieldComprtnent.getField(fgi);
                  for (Field field : fields) {
                    if (idx == 0 && field.deleteFlag == 1) { // ?????????????????????
                      continue;
                    }
                    if (idx == 1 && field.deleteFlag == 0) { // ??????????????????????????????
                      continue;
                    }

                    //???????????????????????????
                    FieldComprtnent fc = new FieldComprtnent();
                    fc.getFileld(field.fieldId);
                    if (accountComprtnent.accountStatusData.displayStatus == AgryeelConst.DISPLAYSTATUS.FIELD) { //??????????????????????????????
                      ObjectNode compartmentStatusJson  = Json.newObject();
                      FieldGroupList fgl = FieldGroupList.getFieldUnique(fg.fieldGroupId, field.fieldId);
                      if (idx == 0) {
                        compartmentStatusJson.put("fieldGroupColor"    , fg.fieldGroupColor);     //???????????????????????????
                        compartmentStatusJson.put("fieldGroupId"       , fg.fieldGroupId);
                        compartmentStatusJson.put("fieldGroupName"     , fg.fieldGroupName);
                      }
                      else {
                        compartmentStatusJson.put("fieldGroupColor"    , "808080");              //???????????????????????????
                        compartmentStatusJson.put("fieldGroupId"       , 9999);
                        compartmentStatusJson.put("fieldGroupName"     , "????????????");
                      }
                      compartmentStatusJson.put("fieldId"            , field.fieldId);          //??????ID
                      compartmentStatusJson.put("fieldName"          , field.fieldName);        //?????????
                      if (idx == 0) {
                        compartmentStatusJson.put("sequenceId"         , dfSeq.format(fg.sequenceId) + dfSeq.format(fgl.sequenceId) + dfSeq.format(0)); //?????????
                      }
                      else {
                        compartmentStatusJson.put("sequenceId"         , dfSeq.format(fg.sequenceId + 9000) + dfSeq.format(fgl.sequenceId) + dfSeq.format(0)); //?????????
                      }

                      listJson.put(Double.toString(field.fieldId), compartmentStatusJson);
                      listJsonApi.add(compartmentStatusJson);
                      oldCount++;
                    }
                    else { //??????????????????????????????
                      List<Compartment> compartments = fc.getCompartmentList();
                      for (Compartment compartmentData : compartments) {
                        if (idx == 0 && compartmentData.deleteFlag == 1) { // ?????????????????????
                          continue;
                        }
                        if (idx == 1 && compartmentData.deleteFlag == 0) { // ??????????????????????????????
                          continue;
                        }
                        oldCount++;

                        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(compartmentData.kukakuId);
                        //----- ?????????????????????????????????????????????????????? -----
                        //----- ????????? -----
                        if (!StringU.nullcheck(accountComprtnent.accountStatusData.sskKukakuName)) { //????????????
                          if (compartmentData.kukakuName.indexOf(accountComprtnent.accountStatusData.sskKukakuName) == -1) { //???????????????????????????????????????
                            continue;
                          }
                        }
                        //----- ???????????? -----
                        if (!ListrU.keyCheck(aryMultiKukaku, compartmentData.kukakuId)) { //????????????????????????????????????
                          continue;
                        }
                        //----- ???????????? -----
                        if (!ListrU.keyChecks(aryHinsyu, compartmentStatusData.hinsyuId)) { //????????????????????????????????????
                          continue;
                        }
                        ObjectNode compartmentStatusJson  = Json.newObject();
                        FieldGroupList fgl = FieldGroupList.getFieldUnique(fg.fieldGroupId, field.fieldId);
                        if (idx == 0) {
                          compartmentStatusJson.put("fieldGroupColor"    , fg.fieldGroupColor);     //???????????????????????????
                          compartmentStatusJson.put("fieldGroupId"       , fg.fieldGroupId);
                          compartmentStatusJson.put("fieldGroupName"     , fg.fieldGroupName);
                        }
                        else {
                          compartmentStatusJson.put("fieldGroupColor"    , "808080");     //???????????????????????????
                          compartmentStatusJson.put("fieldGroupId"       , 9999);
                          compartmentStatusJson.put("fieldGroupName"     , "????????????");
                        }
                        compartmentStatusJson.put("fieldId"            , field.fieldId);          //??????ID
                        compartmentStatusJson.put("fieldName"          , field.fieldName);        //?????????
                        compartmentStatusJson.put("kukakuId"            , compartmentData.kukakuId);                    //??????ID
                        compartmentStatusJson.put("kukakuName"          , compartmentData.kukakuName);                  //?????????
                        compartmentStatusJson.put("kukakuKind"          , Common.GetCommonValue(Common.ConstClass.KUKAKUKIND, (int)compartmentData.kukakuKind));                  //?????????
                        if (idx == 0) {
                          compartmentStatusJson.put("sequenceId"          , dfSeq.format(fg.sequenceId) + dfSeq.format(0) + dfSeq.format(compartmentData.sequenceId)); //?????????
                        }
                        else {
                          compartmentStatusJson.put("sequenceId"          , dfSeq.format(fg.sequenceId + 9000) + dfSeq.format(0) + dfSeq.format(compartmentData.sequenceId)); //?????????
                        }
                        compartmentStatusJson.put("workYear"            , compartmentStatusData.workYear);              //?????????
                        compartmentStatusJson.put("rotationSpeedOfYear" , compartmentStatusData.rotationSpeedOfYear);   //???????????????
                        compartmentStatusJson.put("hashuCount"          , compartmentStatusData.hashuCount);            //????????????
                        long seiikuDayCount     = 0;
                        long seiikuDayCountEnd  = 0;
                        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //?????????

                          Date hashuDate = DateUtils.truncate(compartmentStatusData.hashuDate, Calendar.DAY_OF_MONTH);
                          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

                          compartmentStatusJson.put("hashuDate"       , compartmentStatusData.hashuDate.toString());

                          //????????????????????????????????????
//                          if (compartmentStatusData.seiikuDayCount != 0) {
//                            seiikuDayCount = compartmentStatusData.seiikuDayCount;
//                          }
//                          else {
//                            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//                          }
                          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {  //???????????????
                            seiikuDayCount = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuStartDate);
                          }
                          else {
                            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
                          }
                          if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {  //???????????????
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
                        //----- ?????????????????????????????????????????????????????? -----
                        long nisuf = 0;
                        long nisut = 9999;
                        if (accountComprtnent.accountStatusData.sskSeiikuF != 0) { //????????????
                          nisuf = accountComprtnent.accountStatusData.sskSeiikuF;
                        }
                        if (accountComprtnent.accountStatusData.sskSeiikuT != 0) { //????????????
                          nisut = accountComprtnent.accountStatusData.sskSeiikuT;
                        }

                        if (!(nisuf <= seiikuDayCount && seiikuDayCount <= nisut)) { //???????????????????????????
                          continue;
                        }

                        compartmentStatusJson.put("seiikuDayCount"    , seiikuDayCount);                                //????????????
                        compartmentStatusJson.put("seiikuDayCountEnd" , seiikuDayCountEnd);                             //????????????(???????????????)

                        Crop cp = CropComprtnent.getCropById(compartmentStatusData.cropId);
                        if (cp != null) {                                                                               //???????????????
                          compartmentStatusJson.put("cropName"    , cp.cropName);
                        }
                        else {
                          compartmentStatusJson.put("cropName"    , "");
                        }

                        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

                            compartmentStatusJson.put("hinsyuName"        , compartmentStatusData.hinsyuName);            //?????????

                        }
                        else {

                            compartmentStatusJson.put("hinsyuName"        , "");                                          //?????????

                        }

                        DecimalFormat df = new DecimalFormat("#,##0.0");
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
                        SimpleDateFormat sdfL = new SimpleDateFormat("yyyy/MM/dd");
                        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- ????????????
                        //----------------------------------------------------------------------------------------------------------------------------------
                        compartmentStatusJson.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));
                        double totalDatas = 0;
                        Weather oWeather      = null;
                        //----- ????????????????????? -----
                        if ((compartmentStatusData.hashuDate != null)
                            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //????????????????????????????????????????????????

                          Compartment ct = Compartment.getCompartmentInfo(compartmentStatusData.kukakuId);
                          if (ct != null) {
                            Field fd = ct.getFieldInfo();
                            if (fd != null) {
                              String pointId = PosttoPoint.getPointId(fd.postNo);
                              if (pointId != null && !"".equals(pointId)) {
                                java.sql.Date endDate;

                                if ((compartmentStatusData.shukakuStartDate != null)
                                    && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) { //???????????????????????????????????????????????????
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
                        //- ???????????????
                        //----------------------------------------------------------------------------------------------------------------------------------
                        double totalDatar = 0;
                        oWeather      = null;
                        //----- ????????????????????? -----
                        if ((compartmentStatusData.hashuDate != null)
                            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //????????????????????????????????????????????????

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
                        //- ??????
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

                        compartmentStatusJson.put("disinfectionCount"          , disinfectionCount);                                               //????????????????????????????????????
                        compartmentStatusJson.put("totalDisinfectionNumber"    , df.format(compartmentStatusData.totalDisinfectionNumber * 0.001));//???????????????
                        compartmentStatusJson.put("totalDisinfectionCount"     , compartmentStatusData.totalDisinfectionCount);                   //??????????????????
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- ?????????
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

                        compartmentStatusJson.put("kansuiCount"          , kansuiCount);                                       //????????????????????????????????????
                        compartmentStatusJson.put("totalKansuiNumber"    , df.format(compartmentStatusData.totalKansuiNumber));//???????????????
                        compartmentStatusJson.put("totalKansuiCount"     , compartmentStatusData.totalKansuiCount);            //??????????????????
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- ??????
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

                        compartmentStatusJson.put("tuihiCount"          , tuihiCount);                                       //????????????????????????????????????
                        compartmentStatusJson.put("totalTuihiNumber"    , df.format(compartmentStatusData.totalTuihiNumber * 0.001));//???????????????
                        compartmentStatusJson.put("totalTuihiCount"     , compartmentStatusData.totalTuihiCount);            //??????????????????
                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- ??????
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

                        compartmentStatusJson.put("totalShukakuNumber"    , df.format(compartmentStatusData.totalShukakuCount));        //???????????????
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

                          ObjectNode  aj   = Json.newObject();                                                              //??????????????????????????????
                          UserComprtnent uc = new UserComprtnent();
                          uc.getNowWorkingByField(field.fieldId, aj);
                          compartmentStatusJson.put("working", aj);
                        }

                        //----------------------------------------------------------------------------------------------------------------------------------
                        //- ????????????
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
              compartmentGroupJson.put("kukakuName"     , "????????????");
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

        /* ????????????JSON?????????????????? */
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
        compartmentStatusJson.put("fieldId"             , field.fieldId);          //??????ID
        compartmentStatusJson.put("fieldName"           , field.fieldName);        //?????????
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(compartmentData.kukakuId);
        compartmentStatusJson.put("kukakuId"            , compartmentData.kukakuId);                    //??????ID
        compartmentStatusJson.put("kukakuName"          , compartmentData.kukakuName);                  //?????????
        compartmentStatusJson.put("kukakuKind"          , Common.GetCommonValue(Common.ConstClass.KUKAKUKIND, (int)compartmentData.kukakuKind));  //????????????
        compartmentStatusJson.put("workYear"            , compartmentStatusData.workYear);              //?????????
        compartmentStatusJson.put("rotationSpeedOfYear" , compartmentStatusData.rotationSpeedOfYear);   //???????????????
        compartmentStatusJson.put("hashuCount"          , compartmentStatusData.hashuCount);            //????????????
        long seiikuDayCount = 0;
        long seiikuDayCountEnd  = 0;
        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //?????????

          Date hashuDate = DateUtils.truncate(compartmentStatusData.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

          compartmentStatusJson.put("hashuDate"       , compartmentStatusData.hashuDate.toString());
          //????????????????????????????????????
//          if (compartmentStatusData.seiikuDayCount != 0) {
//            seiikuDayCount = compartmentStatusData.seiikuDayCount;
//          }
//          else {
//            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//          }
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {  //???????????????
            seiikuDayCount = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuStartDate);
          }
          else {
            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
          }
          if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {      //???????????????
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

        compartmentStatusJson.put("seiikuDayCount"    , seiikuDayCount);                                //????????????
        compartmentStatusJson.put("seiikuDayCountEnd" , seiikuDayCountEnd);                             //????????????(???????????????)

        Crop cp = CropComprtnent.getCropById(compartmentStatusData.cropId);
        if (cp != null) {                                                                               //???????????????
          compartmentStatusJson.put("cropName"    , cp.cropName);
        }
        else {
          compartmentStatusJson.put("cropName"    , "");
        }

        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

            compartmentStatusJson.put("hinsyuName"        , compartmentStatusData.hinsyuName);            //?????????

        }
        else {

            compartmentStatusJson.put("hinsyuName"        , "");                                          //?????????

        }

        DecimalFormat df = new DecimalFormat("#,##0.0");
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        compartmentStatusJson.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));
        double totalDatas = 0;
        Weather oWeather      = null;
        //----- ????????????????????? -----
        if ((compartmentStatusData.hashuDate != null)
            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //????????????????????????????????????????????????

          Compartment ct = Compartment.getCompartmentInfo(compartmentStatusData.kukakuId);
          if (ct != null) {
            Field fd = ct.getFieldInfo();
            if (fd != null) {
              String pointId = PosttoPoint.getPointId(fd.postNo);
              if (pointId != null && !"".equals(pointId)) {
                java.sql.Date endDate;

                if ((compartmentStatusData.shukakuStartDate != null)
                    && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) { //???????????????????????????????????????????????????
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
        //- ???????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        double totalDatar = 0;
        oWeather      = null;
        //----- ????????????????????? -----
        if ((compartmentStatusData.hashuDate != null)
            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //????????????????????????????????????????????????

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
        //- ??????
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

        compartmentStatusJson.put("disinfectionCount"          , disinfectionCount);                                               //????????????????????????????????????
        compartmentStatusJson.put("totalDisinfectionNumber"    , df.format(compartmentStatusData.totalDisinfectionNumber * 0.001));//???????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ?????????
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

        compartmentStatusJson.put("kansuiCount"          , kansuiCount);                                       //????????????????????????????????????
        compartmentStatusJson.put("totalKansuiNumber"    , df.format(compartmentStatusData.totalKansuiNumber));//???????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ??????
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

        compartmentStatusJson.put("tuihiCount"          , tuihiCount);                                       //????????????????????????????????????
        compartmentStatusJson.put("totalTuihiNumber"    , df.format(compartmentStatusData.totalTuihiNumber * 0.001));//???????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ??????
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

        compartmentStatusJson.put("totalShukakuNumber"    , df.format(compartmentStatusData.totalShukakuCount));        //???????????????
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

        ObjectNode  aj   = Json.newObject();                                                              //??????????????????????????????
        UserComprtnent uc = new UserComprtnent();
        uc.getNowWorkingByField(compartmentData.kukakuId, aj);
        compartmentStatusJson.put("working", aj);
        resultJson.put("status", compartmentStatusJson);
        resultJson.put("displayChain", accountComprtnent.accountStatusData.displayChain);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }

    /**
     * ???AGRYEEL???????????????????????????????????????
     * @return ?????????????????????JSON
     */
    public static Result getCompartmentDisplay() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();
        long seiikuDayCount = 0;																						//????????????

        /* JSON?????????????????? */
        JsonNode inputJson = request().body().asJson();
        String	sKukakuId  = inputJson.get("kukakuId").asText();

        //??????????????????????????????
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        //?????????????????????
        FieldComprtnent fc = new FieldComprtnent(accountComprtnent.accountData.farmId);
        fc.getFileld(Double.parseDouble(sKukakuId));

        //?????????????????????
        CompartmentStatus compartmentStatusData = fc.getCompartmentStatusList().get(0);
        Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);

        ObjectNode compartmentStatusJson	= Json.newObject();

        /* ??????ID????????????????????????????????? */
        Compartment compartmentData = FieldComprtnent.getCompartment(compartmentStatusData.kukakuId);
        FieldGroup fg = FieldGroupList.getFieldGroup(fc.field.fieldId);
        String cropColor = "000000";
        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

        	Hinsyu hinsyuData = Hinsyu.find.where().eq("hinsyu_name", compartmentStatusData.hinsyuName.trim()).findUnique();
        	Crop cropData = Crop.find.where().eq("crop_id", hinsyuData.cropId).findUnique();
        	if (cropData != null) {
                cropColor = cropData.cropColor;
        	}

            compartmentStatusJson.put("hinsyuName"				, compartmentStatusData.hinsyuName);						//?????????
            compartmentStatusJson.put("hinsyuColor"				, cropColor);												//???????????????

        }
        else {

            compartmentStatusJson.put("hinsyuName"				, "");														//?????????
            compartmentStatusJson.put("hinsyuColor"				, cropColor);												//???????????????

        }

        compartmentStatusJson.put("kukakuGroupColor"		, "FFFFFF");												//???????????????????????????????????????
        if (fg != null) {
          compartmentStatusJson.put("kukakuGroupColor"	, fg.fieldGroupColor);			      //???????????????????????????
        }

        compartmentStatusJson.put("kukakuId"				    , compartmentStatusData.kukakuId);							//??????ID
        compartmentStatusJson.put("kukakuName"				  , compartmentData.kukakuName);								  //?????????
        compartmentStatusJson.put("rotationSpeedOfYear"	, compartmentStatusData.rotationSpeedOfYear);		//???????????????
        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //?????????

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

        compartmentStatusJson.put("seiikuDayCount"		, seiikuDayCount);											//????????????
        compartmentStatusJson.put("nowEndWork"				, compartmentStatusData.nowEndWork);		//??????????????????
        compartmentStatusJson.put("workColor"				  , compartmentStatusData.workColor);			//???????????????
        if (compartmentStatusData.finalDisinfectionDate != null) {													//???????????????
            compartmentStatusJson.put("finalDisinfectionDate"	, compartmentStatusData.finalDisinfectionDate.toString());
        }
        else {
            compartmentStatusJson.put("finalDisinfectionDate"	, "");
        }
        if (compartmentStatusData.finalKansuiDate != null) {															//???????????????
            compartmentStatusJson.put("finalKansuiDate"			, compartmentStatusData.finalKansuiDate.toString());
        }
        else {
            compartmentStatusJson.put("finalKansuiDate"			, "");
        }
        if (compartmentStatusData.finalTuihiDate != null) {																//???????????????
            compartmentStatusJson.put("finalTuihiDate"			, compartmentStatusData.finalTuihiDate.toString());
        }
        else {
            compartmentStatusJson.put("finalTuihiDate"			, "");
        }
        if (compartmentStatusData.shukakuStartDate != null) {															//???????????????
            compartmentStatusJson.put("shukakuStartDate"		, compartmentStatusData.shukakuStartDate.toString());
        }
        else {
            compartmentStatusJson.put("shukakuStartDate"		, "");
        }
        if (compartmentStatusData.shukakuEndDate != null) {																//???????????????
            compartmentStatusJson.put("shukakuEndDate"			, compartmentStatusData.shukakuEndDate.toString());
        }
        else {
            compartmentStatusJson.put("shukakuEndDate"			, "");
        }
        compartmentStatusJson.put("totalSolarRadiation"     , compartmentStatusData.totalSolarRadiation);
        compartmentStatusJson.put("totalDisinfectionCount"  , compartmentStatusData.totalDisinfectionCount);	//???????????????
        compartmentStatusJson.put("totalKansuiCount"		    , compartmentStatusData.totalKansuiCount);				//???????????????
        compartmentStatusJson.put("totalTuihiCount"			    , compartmentStatusData.totalTuihiCount);					//???????????????
        compartmentStatusJson.put("totalShukakuCount"		    , compartmentStatusData.totalShukakuCount);				//???????????????
        compartmentStatusJson.put("oldDisinfectionCount"	  , compartmentStatusData.oldDisinfectionCount);		//???????????????
        compartmentStatusJson.put("oldKansuiCount"			    , compartmentStatusData.oldKansuiCount);					//???????????????
        compartmentStatusJson.put("oldTuihiCount"			      , compartmentStatusData.oldTuihiCount);						//???????????????
        compartmentStatusJson.put("oldShukakuCount"			    , compartmentStatusData.oldShukakuCount);					//???????????????
        compartmentStatusJson.put("nowWorkMode"				      , compartmentStatusData.nowWorkMode);						  //?????????????????????
        compartmentStatusJson.put("endWorkId"				        , compartmentStatusData.endWorkId);							  //????????????????????????

        resultJson.put("kukakuId", sKukakuId);
        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTDISPKAY, compartmentStatusJson);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * ???AGRYEEL??????????????????????????????
     * @return ????????????JSON
     */
    public static Result getWork() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- ???????????? -----*/

        listJson   = Json.newObject();	//???????????????JSON??????????????????

        //??????????????????????????????
        if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
          resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.REDIRECT);
        }
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
Logger.debug("[ GET WORK ] START");
        WorkChainCompornent.getWorkOfWorkChainJson(accountComprtnent.accountData.farmId, accountComprtnent.accountStatusData.selectChainId, listJson);
Logger.debug("[ GET WORK ] END");

        //----- ???????????????????????????????????? -----
        WorkChain wc = WorkChain.getWorkChain(accountComprtnent.accountStatusData.selectChainId);
        resultJson.put("workchainid"  , accountComprtnent.accountStatusData.selectChainId);
        resultJson.put("workchainname", wc.workChainName);


        resultJson.put(AgryeelConst.WorkInfo.TARGETWORK, listJson);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
    /**
     * ???AGRYEEL????????????????????????????????????
     * @return ????????????JSON
     */
    public static Result getKukakuOfWorkJson() {

        /* ????????????JSON?????????????????? */
        ObjectNode  resultJson = Json.newObject();
        JsonNode jn = request().body().asJson();

        double workId = jn.get("workId").asDouble();
        double pLat   = jn.get("pLat").asDouble();
        double pLng   = jn.get("pLng").asDouble();

        //??????????????????????????????
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        WorkChainCompornent.getKukakuOfWorkJson(accountComprtnent.accountData.farmId, workId, resultJson, pLat, pLng, accountComprtnent.accountStatusData.radius);
        resultJson.put("workTargetDisplay", accountComprtnent.accountStatusData.workTargetDisplay);
        resultJson.put("displayChain", accountComprtnent.accountStatusData.displayChain);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * ???AGRYEEL??????????????????????????????
     * @return ??????????????????????????????
     */
    public static Result getTimeLine() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();

        getTimeLineData(resultJson);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
        return ok(resultJson);
    }

    /**
     * ???AGRYEEL?????????????????????????????????Json?????????????????????????????????
     * @return ??????????????????????????????
     */
    public static void getTimeLineData(ObjectNode	resultJson) {

        /* ????????????JSON?????????????????? */
        ObjectNode 	listJson   = Json.newObject();

        /*----- ???????????????????????? -----*/
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

        listJson   = Json.newObject();	//???????????????JSON??????????????????
        /* ???????????????????????????????????????????????????????????????????????? */
        UserComprtnent uc = new UserComprtnent();
        uc.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        String[] searchWhere = uc.accountStatusData.selectWorkId.split(",");
        List<Double>			aryWork			= new ArrayList<Double>();
        for (String searchWhereData : searchWhere) {
          aryWork.add(Double.parseDouble(searchWhereData));
        }

        /* ?????????????????????????????????????????????????????????????????? */
        List<TimeLine> timeLine = TimeLine.find.where().eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).in("work_id", aryWork).between("work_date", dateFrom, dateTo).orderBy("work_date desc, updateTime desc, timeLineId desc").findList();
        SimpleDateFormat sdf	= new SimpleDateFormat("MM.dd HH:mm");
        SimpleDateFormat sdf2	= new SimpleDateFormat("MM/dd");

        for (TimeLine timeLineData : timeLine) {							//???????????????JSON????????????????????????

        	WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

        	if (workd == null) { continue; }

        	target = false;

          ObjectNode timeLineJson		= Json.newObject();
          timeLineJson.put("timeLineId"	, timeLineData.timeLineId);						//??????????????????ID
          timeLineJson.put("workdate"		, sdf2.format(workd.workDate));					//?????????
          timeLineJson.put("updateTime"	, sdf.format(timeLineData.updateTime));			//????????????
          timeLineJson.put("message"		, StringU.setNullTrim(timeLineData.message));	//???????????????
          timeLineJson.put("timeLineColor", timeLineData.timeLineColor);					//???????????????????????????
          timeLineJson.put("workDiaryId"	, timeLineData.workDiaryId);					//????????????ID
          timeLineJson.put("workName"		, timeLineData.workName);						//?????????
          timeLineJson.put("kukakuName"	, timeLineData.kukakuName);						//?????????
          timeLineJson.put("accountName"	, timeLineData.accountName);					//??????????????????
          timeLineJson.put("workId"		, timeLineData.workId);							//????????????
          timeLineJson.put("worktime"		, workd.workTime);								//????????????
          timeLineJson.put("workDiaryId"	, workd.workDiaryId);							//??????

          listJson.put(Double.toString(timeLineData.timeLineId), timeLineJson);

        }

        resultJson.put(AgryeelConst.TimeLineInfo.TARGETTIMELINE, listJson);
    }

    /**
     * ???AGRYEEL???????????????????????????
     * @return ????????????????????????JSON
     */
    public static Result addClip() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();

        /* JSON?????????????????? */
        JsonNode jsonData = request().body().asJson();

        /*----- ??????????????????????????????????????? -----*/
        String accountId 	= session(AgryeelConst.SessionKey.ACCOUNTID);	/* ???????????????ID */
        String kukakuId		= jsonData.get("kukakuId").asText();			/* ??????ID */

        //???AICA???TODO:???????????????????????????
//        ClipCompartment clipData = ClipCompartment.find.where().eq("account_id", accountId).eq("kukaku_id", Double.parseDouble(kukakuId)).findUnique(); /* ?????????????????????????????????????????????????????? */
//
//        if (clipData == null) {										/* ?????????????????????????????????????????????????????????????????????????????? */
//            clipData = new ClipCompartment();								/* ????????????????????? */
//            clipData.accountId 		= accountId;						/* ???????????????ID???????????? */
//            clipData.kukakuId 		= Double.parseDouble(kukakuId);		/* ??????ID???????????? */
//            clipData.clipGroupId	= 0;								/* ?????????????????????????????? */
//            clipData.save();											/* ??????????????????????????? */
//            resultJson.put(AgryeelConst.ClipGroup.CLIPRESULT, AgryeelConst.ClipGroup.EXISTS);
//        }
//        else {														/* ??????????????????????????????????????? */
//            /* ????????????????????????????????? */
//            Ebean.createSqlUpdate("DELETE FROM clip_compartment WHERE account_id = :accountId AND kukaku_id = :kukakuId")
//            .setParameter("accountId", clipData.accountId).setParameter("kukakuId", clipData.kukakuId).execute();
//
//            resultJson.put(AgryeelConst.ClipGroup.CLIPRESULT, AgryeelConst.ClipGroup.NONE);
//        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * ???AGRYEEL?????????????????????????????????
     * @return ??????????????????????????????
     */
    public static Result workTargetMove() {
        return ok(views.html.worktarget.render(""));
    }

    /**
     * ???AGRYEEL????????????????????????????????????????????????
     * @return ???????????????????????????JSON
     */
    public static Result workTargetInit() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- ?????????????????? -----*/
        /* ?????????????????????????????????????????????????????????????????? */
        List<WorkCompartment> workCompartment = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).findList();

        /* ??????ID??????????????????????????????????????? */
        List<Double> aryKukakuId = new ArrayList<Double>();						//???????????? ??????ID
        for (WorkCompartment workCompartmentData : workCompartment) {			//???????????????????????????
            aryKukakuId.add(workCompartmentData.kukakuId);
        }

        List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().in("kukaku_id", aryKukakuId).orderBy("kukaku_id").findList();

        for (CompartmentStatus compartmentStatusData : compartmentStatus) {		//?????????????????????JSON????????????????????????

            /* ??????ID????????????????????????????????? */
            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).eq("farm_id", Double.parseDouble(session(AgryeelConst.SessionKey.FARMID))).findUnique();
            if (compartmentData == null) { continue; }															//??????????????????????????????????????????????????????????????????

            /* ??????ID????????????????????????????????????????????? */
            WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", compartmentStatusData.kukakuId).findUnique();

            ObjectNode compartmentStatusJson	= Json.newObject();
            compartmentStatusJson.put("kukakuId"			, compartmentStatusData.kukakuId);							//??????ID
            compartmentStatusJson.put("kukakuName"			, compartmentData.kukakuName);								//?????????
            compartmentStatusJson.put("rotationSpeedOfYear"	, compartmentStatusData.rotationSpeedOfYear);				//???????????????
            compartmentStatusJson.put("hinsyuName"			, StringU.setNullTrim(compartmentStatusData.hinsyuName));	//?????????
            compartmentStatusJson.put("workTarget"			, workCompartmentData.workTarget);							//?????????????????????
            listJson.put(Double.toString(compartmentStatusData.kukakuId), compartmentStatusJson);

        }

        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJson);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * ???AGRYEEL????????????????????????????????????
     * @return ???????????????JSON
     */
    public static Result getGroupFarmList() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- ?????????????????? -----*/
        /* ?????????????????????????????????????????????????????????????????? */
        List<Farm> farm = Farm.find.where().eq("farm_group_id", Double.valueOf(session(AgryeelConst.SessionKey.FARMGROUPID))).findList();

        for (Farm farmData : farm) {		//???????????????JSON????????????????????????

            ObjectNode farmJson	= Json.newObject();
            farmJson.put("farmId"			, farmData.farmId);					//??????ID
            farmJson.put("farmName"			, farmData.farmName);				//?????????
            listJson.put(Double.toString(farmData.farmId), farmJson);

        }

        resultJson.put(AgryeelConst.FarmInfo.FARMLIST, listJson);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * ???AGRYEEL??????????????????
     * @return JSON
     */
    public static Result farmChange() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();

        /* JSON?????????????????? */
        JsonNode inpput = request().body().asJson();

        session(AgryeelConst.SessionKey.FARMID, String.valueOf(inpput.get("farmId").asText()));									//??????ID???????????????????????????

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    /**
     * ???AGRYEEL?????????????????????????????????
     * @return ???????????????????????????????????????JSON
     */
    public static Result workTargetUpdate() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();

        /* JSON?????????????????? */
        JsonNode jsonData = request().body().asJson();
        double kukakuId = Double.parseDouble(jsonData.get("kukakuId").asText());

        /* ??????ID????????????????????????????????????????????? */
        //WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("kukaku_id", jsonData.get("kukakuId").asText()).findUnique();
        WorkCompartment workCompartmentData = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).eq("kukaku_id", kukakuId).findUnique();

        /* ??????????????????????????????????????????????????????????????? */
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
     * ???AGRYEEL????????????????????????????????????
     * @return ???????????????????????????????????????
     */
    public static Result accountMakeMove() {
        return ok(views.html.accountMake.render(""));
    }

    /**
     * ???AGRYEEL????????????????????????????????????????????????
     * @return ????????????JSON
     */
    public static Result getHouseGroupWhere() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- ???????????? -----*/

        listJson   = Json.newObject();	//???????????????JSON??????????????????

    	int clipMode = 0;

    	UserComprtnent accountComprtnent = new UserComprtnent();
    	accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
    	clipMode = accountComprtnent.accountData.clipMode;

    	  //???AICA???TODO:?????????????????????????????????
//        /* ?????????????????????????????????????????????????????????????????? */
//        List<HouseGroupWhere> houseGroupWhere = HouseGroupWhere.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).orderBy("kukaku_group_id").findList();
//
//        for (HouseGroupWhere houseGroupWhereData : houseGroupWhere) {
//
//        	CompartmentGroup compartmentGroup = CompartmentGroup.find.where().eq("kukaku_group_id", houseGroupWhereData.kukakuGroupId).findUnique();
//
//            ObjectNode compartmentGroupJson		= Json.newObject();
//            compartmentGroupJson.put("kukakuGroupId"	, compartmentGroup.kukakuGroupId);			//??????ID
//            compartmentGroupJson.put("kukakuGroupName"	, compartmentGroup.kukakuGroupName);		//??????ID
//            compartmentGroupJson.put("kukakuGroupColor"	, compartmentGroup.kukakuGroupColor);		//???????????????
//
//            if (clipMode == 1) {
//
//                compartmentGroupJson.put("flag"				, "0");							//?????????
//
//            }
//            else {
//                compartmentGroupJson.put("flag"				, houseGroupWhereData.flag);	//?????????
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
     * ???AGRYEEL????????????????????????????????????????????????
     * @return ????????????JSON
     */
    public static Result changeHouseGroupWhere(double kukakuGroupId) {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- ???????????? -----*/

        listJson   = Json.newObject();	//???????????????JSON??????????????????

    	int clipMode = 0;

    	UserComprtnent accountComprtnent = new UserComprtnent();
    	accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        if ( kukakuGroupId == AgryeelConst.ClipGroup.CLIPGROUPNO) {

        	clipMode = accountComprtnent.ChangeClipMode();

        }
        else {

        	clipMode = accountComprtnent.UpdateClipMode(AgryeelConst.ClipGroup.NONE);

        }

        //???AICA???TODO:?????????????????????????????????
//        /* ?????????????????????????????????????????????????????????????????? */
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
//            compartmentGroupJson.put("kukakuGroupId"	, compartmentGroup.kukakuGroupId);			//??????ID
//            compartmentGroupJson.put("kukakuGroupName"	, compartmentGroup.kukakuGroupName);		//??????ID
//            compartmentGroupJson.put("kukakuGroupColor"	, compartmentGroup.kukakuGroupColor);		//???????????????
//            compartmentGroupJson.put("flag"				, houseGroupWhereData.flag);	//?????????
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
     * ???AGRYEEL????????????????????????????????????????????????
     * @return ????????????JSON
     */
    public static Result getSearchWhere() {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- ???????????? -----*/

        listJson   = Json.newObject();	//???????????????JSON??????????????????

        //TODO:TimeLine????????????????????????
        /* ?????????????????????????????????????????????????????????????????? */
//        List<SearchWhere> searchWhere = SearchWhere.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).orderBy("work_id").findList();
//
//        for (SearchWhere searchWhereData : searchWhere) {
//
//        	Work workData = Work.find.where().eq("work_id", searchWhereData.workId).findUnique();
//
//            ObjectNode workJson		= Json.newObject();
//            workJson.put("workId"		, workData.workId);			//??????ID
//            workJson.put("workColor"	, workData.workColor);		//???????????????
//            workJson.put("flag"			, searchWhereData.flag);	//?????????
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
     * ???AGRYEEL????????????????????????????????????????????????
     * @return ????????????JSON
     */
    public static Result changeSearchWhere(double workId) {

        /* ????????????JSON?????????????????? */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- ???????????? -----*/

        //TODO:TimeLine????????????????????????
//        listJson   = Json.newObject();	//???????????????JSON??????????????????
//
//        /* ?????????????????????????????????????????????????????????????????? */
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
//            workJson.put("workId"		, workData.workId);			//??????ID
//            workJson.put("workColor"	, workData.workColor);		//???????????????
//            workJson.put("flag"			, searchWhereData.flag);	//?????????
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
     * ???AGRYEEL?????????????????????????????????????????????
     * @return ????????????JSON
     */
    public static Result getSystemMessage() {

        /* ????????????JSON?????????????????? */
        ObjectNode  resultJson = Json.newObject();
        ObjectNode  listJson = Json.newObject();

        /* ???????????????????????????????????? */
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
            /* ??????????????????????????? */
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

      /* ????????????JSON?????????????????? */
      ObjectNode  resultJson = Json.newObject();
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);

      /* ???????????????????????????????????? */
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

      /* ????????????JSON?????????????????? */
      ObjectNode  resultJson = Json.newObject();
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);

      /* ???????????????????????????????????? */
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
          /* ??????????????????????????? */
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

      /* ????????????JSON?????????????????? */
      ObjectNode  resultJson = Json.newObject();
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.ERROR);

      /* ???????????????????????????????????? */
      String accountId = session(AgryeelConst.SessionKey.ACCOUNTID);
      UserComprtnent accountComprtnent = new UserComprtnent();
      accountComprtnent.GetAccountData(accountId);
      accountComprtnent.accountStatusData.messageKind = messageKind;
      accountComprtnent.accountStatusData.update();

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    /**
     * ?????????????????????????????????
     * @return
     */
    public static Result changeFieldGroup() {

        /* ????????????JSON?????????????????? */
        ObjectNode  resultJson = Json.newObject();

        /* JSON?????????????????? */
        JsonNode inputJson = request().body().asJson();
        String  selectFieldGroup  = inputJson.get("selectFieldGroup").asText();

        //??????????????????????????????
        UserComprtnent accountComprtnent = new UserComprtnent();
        int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        accountComprtnent.accountStatusData.selectFieldGroupId = selectFieldGroup;
        accountComprtnent.accountStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
    /**
     * ????????????????????????????????????
     * @return
     */
    public static Result changeWorkChain() {

        /* ????????????JSON?????????????????? */
        ObjectNode  resultJson = Json.newObject();

        /* JSON?????????????????? */
        JsonNode inputJson = request().body().asJson();
        String  selectWorkChain  = inputJson.get("selectWorkChain").asText();

        //??????????????????????????????
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
     * ?????????????????????????????????
     * @return
     */
    public static Result getFieldDetail() {

      /* ????????????JSON?????????????????? */
      ObjectNode  resultJson = Json.newObject();
      ObjectNode  listJson = Json.newObject();
      Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
      ObjectMapper map = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode   listJsonApi = map.createArrayNode();
      boolean     api        = false;
      if (session(AgryeelConst.SessionKey.API) != null) {
      	api = true;
      }

      /* JSON?????????????????? */
      JsonNode inputJson = request().body().asJson();
      double  fieldId  = inputJson.get("field").asDouble();

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      resultJson.put("compartmentStatusSkip", accountComprtnent.accountStatusData.compartmentStatusSkip); //??????????????????SKIP?????????
      resultJson.put("displayChain", accountComprtnent.accountStatusData.displayChain);

      CompartmentStatusCompornent csc = new CompartmentStatusCompornent();
      csc.getAllData(accountComprtnent.accountData.farmId);
      Farm farm = Farm.getFarm(accountComprtnent.accountData.farmId);
      FarmStatus farmStatus = farm.getFarmStatus();
      DecimalFormat df = new DecimalFormat("#,##0.00");

      Field field = Field.getFieldInfo(fieldId);
      FieldGroup fg = FieldGroupList.getFieldGroup(field.fieldId);
      //?????????????????????

      ObjectNode fieldJson  = Json.newObject();

      fieldJson.put("fieldColor"         , fg.fieldGroupColor);     //???????????????????????????
      fieldJson.put("fieldId"            , field.fieldId);          //??????ID
      fieldJson.put("fieldName"          , field.fieldName);        //?????????
      fieldJson.put("post"               , StringU.setNullTrim(field.postNo));           //????????????
      double area = field.area;
      if (farmStatus.areaUnit == 1) { //???????????????????????????
        area = area * 100;
      }
      else if (farmStatus.areaUnit == 2) { //????????????
        area = area * 30.25;
      }
      fieldJson.put("area"                , df.format(area));                        //????????????
      fieldJson.put("areaUnit"            , Common.GetCommonValue(Common.ConstClass.AREAUNIT, farmStatus.areaUnit, true)); //??????????????????

      //----------------------------------------------------------------------------------------------------------------------------------
      //- ????????????
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

          //----- ??????18????????????????????????
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
        // TODO ????????????????????? catch ????????????
        e.printStackTrace();
      }
      if(api){
        fieldJson.put("weatherlist" , weatherListApi);
        fieldJson.put("weatherlistd", weatherListdApi);
      }else{
        fieldJson.put("weatherlist" , weatherList);
        fieldJson.put("weatherlistd", weatherListd);
      }

      //??????????????????????????????????????????????????????
      FieldComprtnent fc = new FieldComprtnent();
      fc.getFileld(field.fieldId);
      List<Compartment> compartments = fc.getCompartmentList();
      resultJson.put("compartmentCount", compartments.size());
      for (Compartment compartmentData : compartments) {
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(compartmentData.kukakuId);

        ObjectNode  kJ = Json.newObject();

        if(api){
          kJ.put("kukakuId"          , compartmentData.kukakuId);                    //??????ID
        }
        kJ.put("kukakuName"          , compartmentData.kukakuName);                  //?????????
        kJ.put("workYear"            , compartmentStatusData.workYear);              //?????????
        kJ.put("rotationSpeedOfYear" , compartmentStatusData.rotationSpeedOfYear);   //???????????????
        kJ.put("hashuCount"          , compartmentStatusData.hashuCount);            //????????????
        long seiikuDayCount = 0;
        long seiikuDayCountEnd  = 0;
        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //?????????

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
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {  //???????????????
            seiikuDayCount = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuStartDate);
          }
          else {
            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
          }
          if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {  //???????????????
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

        kJ.put("seiikuDayCount"    , seiikuDayCount);                                                   //????????????
        kJ.put("seiikuDayCountEnd" , seiikuDayCountEnd);                                                //????????????(???????????????)

        Crop cp = CropComprtnent.getCropById(compartmentStatusData.cropId);
        if (cp != null) {                                                                               //???????????????
          kJ.put("cropName"    , cp.cropName);
        }
        else {
          kJ.put("cropName"    , "");
        }
        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

          kJ.put("hinsyuName"        , compartmentStatusData.hinsyuName);            //?????????

        }
        else {

          kJ.put("hinsyuName"        , "");                                          //?????????

        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        kJ.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));
        double totalDatas = 0;
        double todayDatas = 0;
        java.sql.Date oldDate = null;
        Weather oWeather      = null;
        //----- ????????????????????? -----
        if ((compartmentStatusData.hashuDate != null)
            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //????????????????????????????????????????????????

          Compartment ct = Compartment.getCompartmentInfo(compartmentStatusData.kukakuId);
          if (ct != null) {
            Field fd = ct.getFieldInfo();
            if (fd != null) {
              String pointId = PosttoPoint.getPointId(fd.postNo);
              if (pointId != null && !"".equals(pointId)) {
                java.sql.Date endDate;

                if ((compartmentStatusData.shukakuStartDate != null)
                    && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) { //???????????????????????????????????????????????????
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
        //- ???????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        double totalDatar = 0;
        oWeather      = null;
        //----- ????????????????????? -----
        if ((compartmentStatusData.hashuDate != null)
            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //????????????????????????????????????????????????

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
        //- ??????
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

        kJ.put("disinfectionCount"          , disinfectionCount);                                               //????????????????????????????????????
        kJ.put("totalDisinfectionNumber"    , df.format(compartmentStatusData.totalDisinfectionNumber * 0.001));//???????????????
        kJ.put("totalDisinfectionCount"     , compartmentStatusData.totalDisinfectionCount);                    //??????????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ?????????
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

        kJ.put("kansuiCount"          , kansuiCount);                                       //????????????????????????????????????
        kJ.put("totalKansuiNumber"    , df.format(compartmentStatusData.totalKansuiNumber));//???????????????
        kJ.put("totalKansuiCount"     , compartmentStatusData.totalKansuiCount);            //??????????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ??????
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

        kJ.put("tuihiCount"          , tuihiCount);                                       //????????????????????????????????????
        kJ.put("totalTuihiNumber"    , df.format(compartmentStatusData.totalTuihiNumber * 0.001));//???????????????
        kJ.put("totalTuihiCount"     , compartmentStatusData.totalTuihiCount);            //??????????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ??????
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

        kJ.put("totalShukakuNumber"    , df.format(compartmentStatusData.totalShukakuCount));        //???????????????
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

        ObjectNode  aj   = Json.newObject();                                                              //??????????????????????????????
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
     * ?????????????????????????????????
     * @return
     */
    public static Result getKukakuDetail() {

      /* ????????????JSON?????????????????? */
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

      /* JSON?????????????????? */
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

        //-------------- ???????????????????????????????????? -------------------------------
        java.sql.Timestamp tStart   = compartmentStatusData.katadukeDate;
        java.sql.Timestamp tEnd     = compartmentStatusData.finalEndDate;
        java.sql.Timestamp tsystem  = DateU.getSystemTimeStamp();

        kJ.put("kukakuId"            , compartmentData.kukakuId);                    //??????ID
        kJ.put("kukakuName"          , compartmentData.kukakuName);                  //?????????
        double area = compartmentData.area;
        if (farmStatus.areaUnit == 1) { //???????????????????????????
          area = area * 100;
        }
        else if (farmStatus.areaUnit == 2) { //????????????
          area = area * 30.25;
        }
        kJ.put("area"                , df.format(area));                        //????????????
        kJ.put("areaUnit"            , Common.GetCommonValue(Common.ConstClass.AREAUNIT, farmStatus.areaUnit, true)); //??????????????????
        kJ.put("rotationSpeedOfYear" , compartmentStatusData.rotationSpeedOfYear);   //???????????????
        kJ.put("hashuCount"          , compartmentStatusData.hashuCount);            //????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ????????????????????????????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        DecimalFormat df1 = new DecimalFormat("00000000");
        DecimalFormat df2 = new DecimalFormat("0000");
        DecimalFormat df3 = new DecimalFormat("00");
        kJ.put("motochoid"   , df1.format(compartmentStatusData.kukakuId) + df2.format(compartmentStatusData.workYear) + df3.format(compartmentStatusData.rotationSpeedOfYear));
        kJ.put("motochoname" , "" + df2.format(compartmentStatusData.workYear) + " ??? " + df3.format(compartmentStatusData.rotationSpeedOfYear) + " ???");
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ????????????????????????????????????????????????
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
        //- ?????????
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
        if (compartmentStatusData.hinsyuName != null && !"".equals(compartmentStatusData.hinsyuName)) {

          kJ.put("hinsyuName"        , compartmentStatusData.hinsyuName);            //?????????

        }
        else {

          kJ.put("hinsyuName"        , "");                                          //?????????

        }
        long seiikuDayCount = 0;
        long seiikuDayCountEnd  = 0;
        if (compartmentStatusData.hashuDate != null && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) {  //?????????

          Date hashuDate = DateUtils.truncate(compartmentStatusData.hashuDate, Calendar.DAY_OF_MONTH);
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

          kJ.put("hashuDate"       , compartmentStatusData.hashuDate.toString());
          //????????????????????????????????????
//          if (compartmentStatusData.seiikuDayCount != 0) {
//            seiikuDayCount = compartmentStatusData.seiikuDayCount;
//          }
//          else {
//            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//          }
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {  //???????????????
            seiikuDayCount = DateU.GetDiffDate(hashuDate, compartmentStatusData.shukakuStartDate);
          }
          else {
            seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
          }
          if (compartmentStatusData.shukakuEndDate != null && (compartmentStatusData.shukakuEndDate.compareTo(nullDate) != 0)) {  //???????????????
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
        kJ.put("seiikuDayCount"    , seiikuDayCount);                                                   //????????????
        kJ.put("seiikuDayCountEnd" , seiikuDayCountEnd);                                                //?????????????????????????????????

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        kJ.put("totalSolarRadiation"    , df.format(compartmentStatusData.totalSolarRadiation));                   //????????????
        ObjectNode  labels    = Json.newObject();
        ObjectNode  datas     = Json.newObject();
        ObjectNode  mixdatas  = Json.newObject();
//      double totalDatas     = compartmentStatusData.totalSolarRadiation;                                        //???????????????????????????????????????????????????
        double totalDatas     = 0;
        double todayDatas     = 0;
        java.sql.Date oldDate = null;
        Weather oWeather      = null;
        int idx = 0;
        //----- ????????????????????? -----
        if ((compartmentStatusData.hashuDate != null)
            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //????????????????????????????????????????????????

          Compartment ct = Compartment.getCompartmentInfo(compartmentStatusData.kukakuId);
          if (ct != null) {
            Field fd = ct.getFieldInfo();
            if (fd != null) {
              String pointId = PosttoPoint.getPointId(fd.postNo);
              if (pointId != null && !"".equals(pointId)) {
                java.sql.Date endDate;

                if ((compartmentStatusData.shukakuStartDate != null)
                    && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) { //???????????????????????????????????????????????????
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
        //- ???????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        labels    = Json.newObject();
        datas     = Json.newObject();
        mixdatas  = Json.newObject();
        totalDatas = 0;
        todayDatas = 0;
        oldDate = null;
        idx = 0;
        //----- ???????????????????????? -----
        if ((compartmentStatusData.hashuDate != null)
            && (compartmentStatusData.hashuDate.compareTo(nullDate) != 0)) { //????????????????????????????????????????????????

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
        //- ??????
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

        kJ.put("disinfectionCount"          , disinfectionCount);                                       //????????????????????????????????????
        kJ.put("totalDisinfectionNumber"    , df.format(compartmentStatusData.totalDisinfectionNumber * 0.001));//???????????????
        kJ.put("totalDisinfectionCount"     , compartmentStatusData.totalDisinfectionCount);            //??????????????????
        //???????????????????????????
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
            //????????????Kg???L????????????????????????
//            if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //???????????????Kg???L?????????
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
        //- ?????????
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

        kJ.put("kansuiCount"          , kansuiCount);                                       //????????????????????????????????????
        kJ.put("totalKansuiNumber"    , df.format(compartmentStatusData.totalKansuiNumber));//???????????????
        kJ.put("totalKansuiCount"     , compartmentStatusData.totalKansuiCount);            //??????????????????
        //???????????????????????????
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
        //- ??????
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

        kJ.put("tuihiCount"          , tuihiCount);                                       //????????????????????????????????????
        kJ.put("totalTuihiNumber"    , df.format(compartmentStatusData.totalTuihiNumber * 0.001));//???????????????
        kJ.put("totalTuihiCount"     , compartmentStatusData.totalTuihiCount);            //??????????????????

        //???????????????????????????
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
            if (nouhi.unitKind == 1 || nouhi.unitKind == 2) { //???????????????Kg???L?????????
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
        //- ??????
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

        kJ.put("totalShukakuNumber"    , df.format(compartmentStatusData.totalShukakuCount));        //???????????????
        if ((compartmentStatusData.totalShukakuCount == 0)
            || (compartmentData.area == 0)) {
          kJ.put("tanshu"              , "*****");
        }
        else {
          kJ.put("tanshu"              , df.format((compartmentStatusData.totalShukakuCount / compartmentData.area) * 10));
        }
        //???????????????????????????
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

        ObjectNode  aj   = Json.newObject();                                                              //??????????????????????????????
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
        //- ?????????????????????
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
          nj.put("no"         , "??????" + motocho.nouyakuNo);
          nj.put("date"       , sdfN.format(motocho.sanpuDate));
          if (compartmentStatusData.shukakuStartDate != null && (compartmentStatusData.shukakuStartDate.compareTo(nullDate) != 0)) {

            nj.put("diffdate"       , DateU.GetDiffDate(motocho.sanpuDate, compartmentStatusData.shukakuStartDate));

          }
          else {
            nj.put("diffdate"       , DateU.GetDiffDate(motocho.sanpuDate, systemDate));
          }
          nj.put("nouhiName"  , motocho.nouhiName);
          nj.put("bairitu"    , motocho.bairitu + "???");
          nj.put("method"     , Common.GetCommonValue(Common.ConstClass.SANPUMETHOD, motocho.sanpuMethod));
          nj.put("ryo"        , dfN.format(motocho.sanpuryo / 1000)  + " " + motocho.unit);
          nouyakuListJson.put("n" + dfK.format(motocho.nouyakuNo) + sdfK.format(motocho.sanpuDate), nj);
          nouyakuListJsonApi.add(nj);
        }
        //----------------------------------------------------------------------------------------------------------------------------------
        //- ?????????????????????
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
            nj.put("no"     , "??????" + motogoe);
          }
          else {
            tuihi++;
            nj.put("no"     , "??????" + tuihi);
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
          nj.put("bairitu"    , motocho.bairitu + "???");
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
        //??????????????????
        ObjectNode nj         = Json.newObject();
        nj.put("no"     , "??? ???");
        nj.put("key"        , "h9999");
        nj.put("color"      , AgryeelConst.ColorData[(int)(9999 % 10)]);
        nj.put("date"       , "");
        nj.put("diffdate"   , "");
        nj.put("nouhiName"  , "?????????????????????");
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
        //- ???????????????????????????
        //----------------------------------------------------------------------------------------------------------------------------------
        /* ?????????????????????????????????????????????????????????????????? */
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

        for (TimeLine timeLineData : timeLine) {                                        //???????????????JSON????????????????????????

          WorkDiary workd = WorkDiary.find.where().eq("work_diary_id", timeLineData.workDiaryId).findUnique();

          if (workd == null) { continue; }

          ObjectNode timeLineJson         = Json.newObject();
          timeLineJson.put("timeLineId"   , timeLineData.timeLineId);                   //??????????????????ID
          timeLineJson.put("workdate"     , sdf3.format(workd.workDate));               //?????????
          timeLineJson.put("updateTime"   , sdf2.format(timeLineData.updateTime));       //????????????
          timeLineJson.put("message"      , StringU.setNullTrim(timeLineData.message)); //???????????????
          timeLineJson.put("timeLineColor", timeLineData.timeLineColor);                //???????????????????????????
          timeLineJson.put("workDiaryId"  , timeLineData.workDiaryId);                  //????????????ID
          timeLineJson.put("workName"     , timeLineData.workName);                     //?????????
          timeLineJson.put("kukakuName"   , timeLineData.kukakuName);                   //?????????
          timeLineJson.put("accountName"  , timeLineData.accountName);                  //??????????????????
          timeLineJson.put("workId"       , timeLineData.workId);                       //????????????
          timeLineJson.put("worktime"     , workd.workTime);                            //????????????
          timeLineJson.put("workDiaryId"  , workd.workDiaryId);                         //??????
          timeLineJson.put("kukakuId"     , timeLineData.kukakuId);                     //????????????
          Compartment cpt = Compartment.getCompartmentInfo(timeLineData.kukakuId);
          Logger.info("[TIME LINE]kukakuId={} fieldId={}", timeLineData.kukakuId, cpt.fieldId);
          timeLineJson.put("fieldId"      , cpt.fieldId);                               //??????ID
          timeLineJson.put("fieldGroupId" , cpt.getFieldGroupInfo().fieldGroupId);      //??????????????????ID

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
     * ????????????????????????
     * @return
     */
    public static Result selectKukakuCrop() {

        /* ????????????JSON?????????????????? */
        ObjectNode  resultJson = Json.newObject();

        /* JSON?????????????????? */
        JsonNode inputJson = request().body().asJson();
        double kukakuId  = inputJson.get("kukakuId").asDouble();

        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(kukakuId);

        CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();

        double cropId  = inputJson.get("cropId").asDouble();

        cws.cropId      = cropId;
        //????????????????????????
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
     * ????????????????????????????????????
     * @return
     */
    public static Result selectKukakuWorkChain() {

        /* ????????????JSON?????????????????? */
        ObjectNode  resultJson = Json.newObject();

        /* JSON?????????????????? */
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
     * ??????????????????????????????
     * @return
     */
    public static Result selectMultiKukakuCrop() {

        /* ????????????JSON?????????????????? */
        ObjectNode  resultJson = Json.newObject();

        /* JSON?????????????????? */
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
