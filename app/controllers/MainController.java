package controllers;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import models.Common;
import models.Compartment;
import models.CompartmentStatus;
import models.Crop;
import models.Field;
import models.FieldGroup;
import models.FieldGroupList;
import models.Pest;
import models.PosttoPoint;
import models.Weather;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;
import util.ListrU;
import util.StringU;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;

import compornent.CompartmentStatusCompornent;
import compornent.CropComprtnent;
import compornent.FieldComprtnent;
import compornent.UserComprtnent;

import consts.AgryeelConst;

public class MainController extends Controller {

    /**
     * 【AGRYEEL】ハウス状況照会を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getFieldGroupSPD() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode 	resultJson = Json.newObject();
      ObjectNode 	listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonApi = mapper.createArrayNode();
      DecimalFormat dfSeq = new DecimalFormat("000000");
      boolean     api        = false;

      if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.REDIRECT);
      }
      if (session(AgryeelConst.SessionKey.API) != null) {
      	api = true;
      }

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      resultJson.put("displaystatus", accountComprtnent.accountStatusData.displayStatus);
      CompartmentStatusCompornent csc = new CompartmentStatusCompornent();
      csc.getAllData(accountComprtnent.accountData.farmId);

      if (StringU.nullcheck(accountComprtnent.accountStatusData.selectFieldGroupId)) {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
      }
      else {
        String[] fgws;
        fgws = accountComprtnent.accountStatusData.selectFieldGroupId.split(",");
        for (String fgw : fgws) {
          double fgi = Double.parseDouble(fgw);
          FieldGroup fg = FieldComprtnent.getFieldGroup(fgi);
          if (fg != null) {
            if (fgi != 999999 && fg.deleteFlag == 1) { // 削除済みの場合
              continue;
            }
            ObjectNode compartmentStatusJson  = Json.newObject();
            compartmentStatusJson.put("fieldGroupColor"    , fg.fieldGroupColor);     //圃場グループカラー
            compartmentStatusJson.put("fieldGroupId"       , fg.fieldGroupId);
            compartmentStatusJson.put("fieldGroupName"     , fg.fieldGroupName);
            compartmentStatusJson.put("sequenceId"         , dfSeq.format(fg.sequenceId) + dfSeq.format(0)); //並び順
            listJson.put(compartmentStatusJson.get("fieldGroupId").asText(), compartmentStatusJson);
            listJsonApi.add(compartmentStatusJson);
          }
        }

        ObjectNode compartmentStatusJson  = Json.newObject();

        compartmentStatusJson.put("fieldGroupColor"    , "808080");              //圃場グループカラー
        compartmentStatusJson.put("fieldGroupId"       , 999999);
        compartmentStatusJson.put("fieldGroupName"     , "過去圃場");
        compartmentStatusJson.put("sequenceId"         , dfSeq.format(999999) + dfSeq.format(0)); //並び順

        listJson.put("999999", compartmentStatusJson);
        listJsonApi.add(compartmentStatusJson);

        if(api){
          resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTGROUP, listJsonApi);
        }else{
          resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTGROUP, listJson);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      }
      return ok(resultJson);
    }
    /**
     * 【AGRYEEL】ハウス状況照会を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getFieldGroupSP() {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();
      ObjectNode  listJson   = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode listJsonApi = mapper.createArrayNode();
      DecimalFormat dfSeq = new DecimalFormat("000000");
      boolean     api        = false;

      if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.REDIRECT);
      }
      if (session(AgryeelConst.SessionKey.API) != null) {
        api = true;
      }

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      resultJson.put("displaystatus", accountComprtnent.accountStatusData.displayStatus);
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
        fgws = accountComprtnent.accountStatusData.selectFieldGroupId.split(",");
        for (String fgw : fgws) {
          boolean addFlg = false;
          double fgi = Double.parseDouble(fgw);
          FieldGroup fg = FieldComprtnent.getFieldGroup(fgi);
          if (fg != null) {
            if (fgi != 999999 && fg.deleteFlag == 1) { // 削除済みの場合
              continue;
            }
            List<Field> fields = FieldComprtnent.getField(fgi);
            for (Field field : fields) {
              if (fgi != 999999 && field.deleteFlag == 1) { // 削除済みの場合
                continue;
              }
              if (fgi == 999999 && field.deleteFlag == 0) { // 削除済みではない場合
                continue;
              }
              FieldComprtnent fc = new FieldComprtnent();
              fc.getFileld(field.fieldId);
              List<Compartment> compartments = fc.getCompartmentList();
              for (Compartment compartmentData : compartments) {
                if (fgi != 999999 && compartmentData.deleteFlag == 1) { // 削除済みの場合
                  continue;
                }
                if (fgi == 999999 && compartmentData.deleteFlag == 0) { // 削除済みではない場合
                  continue;
                }

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
                addFlg = true;
                break;
              }
            }
          }
          if (addFlg) {
            ObjectNode compartmentStatusJson  = Json.newObject();
            compartmentStatusJson.put("fieldGroupColor"    , fg.fieldGroupColor);     //圃場グループカラー
            compartmentStatusJson.put("fieldGroupId"       , fg.fieldGroupId);
            compartmentStatusJson.put("fieldGroupName"     , fg.fieldGroupName);
            compartmentStatusJson.put("sequenceId"         , dfSeq.format(fg.sequenceId) + dfSeq.format(0)); //並び順
            listJson.put(compartmentStatusJson.get("fieldGroupId").asText(), compartmentStatusJson);
            listJsonApi.add(compartmentStatusJson);
          }
        }

        ObjectNode compartmentStatusJson  = Json.newObject();

        compartmentStatusJson.put("fieldGroupColor"    , "808080");              //圃場グループカラー
        compartmentStatusJson.put("fieldGroupId"       , 999999);
        compartmentStatusJson.put("fieldGroupName"     , "過去圃場");
        compartmentStatusJson.put("sequenceId"         , dfSeq.format(999999) + dfSeq.format(0)); //並び順

        listJson.put("999999", compartmentStatusJson);
        listJsonApi.add(compartmentStatusJson);

        if(api){
          resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTGROUP, listJsonApi);
        }else{
          resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTGROUP, listJson);
        }

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      }
      return ok(resultJson);
    }
    /**
     * 【AGRYEEL】ハウス状況照会を取得する
     * @return ハウス状況照会JSON
     */
    public static Result getKukakuSP(double groupId) {

    /* 戻り値用JSONデータの生成 */
    ObjectNode  resultJson = Json.newObject();
    ObjectNode  listJson   = Json.newObject();
    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    ArrayNode listJsonApi = mapper.createArrayNode();
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
      double fgi = groupId;
      FieldGroup fg = FieldComprtnent.getFieldGroup(fgi);
        if (fg != null) {
          List<Field> fields = FieldComprtnent.getField(fgi);
          for (Field field : fields) {
            if (fgi != 999999 && field.deleteFlag == 1) { // 削除済みの場合
              continue;
            }
            if (fgi == 999999 && field.deleteFlag == 0) { // 削除済みではない場合
              continue;
            }

            //区画情報を取得する
            FieldComprtnent fc = new FieldComprtnent();
            fc.getFileld(field.fieldId);
            if (accountComprtnent.accountStatusData.displayStatus == AgryeelConst.DISPLAYSTATUS.FIELD) { //状況照会が圃場の場合
              ObjectNode compartmentStatusJson  = Json.newObject();
              FieldGroupList fgl = FieldGroupList.getFieldUnique(fg.fieldGroupId, field.fieldId);
              compartmentStatusJson.put("fieldId"            , field.fieldId);          //圃場ID
              compartmentStatusJson.put("fieldName"          , field.fieldName);        //圃場名
              if (fgi != 999999) {
                compartmentStatusJson.put("fieldGroupColor"    , fg.fieldGroupColor);     //圃場グループカラー
                compartmentStatusJson.put("sequenceId"         , dfSeq.format(fg.sequenceId) + dfSeq.format(fgl.sequenceId) + dfSeq.format(0)); //並び順
              }
              else {
                compartmentStatusJson.put("fieldGroupColor"    , "808080");              //圃場グループカラー
                compartmentStatusJson.put("sequenceId"         , dfSeq.format(fg.sequenceId + 9000) + dfSeq.format(fgl.sequenceId) + dfSeq.format(0)); //並び順
              }

              listJson.put(Double.toString(field.fieldId), compartmentStatusJson);
              listJsonApi.add(compartmentStatusJson);
              oldCount++;
              count++;
            }
            else { //状況照会が区画の場合
              List<Compartment> compartments = fc.getCompartmentList();
              for (Compartment compartmentData : compartments) {
                if (fgi != 999999 && compartmentData.deleteFlag == 1) { // 削除済みの場合
                  continue;
                }
                if (fgi == 999999 && compartmentData.deleteFlag == 0) { // 削除済みではない場合
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
                compartmentStatusJson.put("fieldGroupColor"    , fg.fieldGroupColor);     //圃場グループカラー
                compartmentStatusJson.put("fieldId"            , field.fieldId);          //圃場ID
                compartmentStatusJson.put("fieldName"          , field.fieldName);        //圃場名
                compartmentStatusJson.put("kukakuId"            , compartmentData.kukakuId);                    //区画ID
                compartmentStatusJson.put("kukakuName"          , compartmentData.kukakuName);                  //区画名
                compartmentStatusJson.put("kukakuKind"          , Common.GetCommonValue(Common.ConstClass.KUKAKUKIND, (int)compartmentData.kukakuKind));                  //区画名
                if (fgi != 999999) {
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
        }
      }
      resultJson.put("displaystatus", accountComprtnent.accountStatusData.displayStatus);
      resultJson.put("displayChain", accountComprtnent.accountStatusData.displayChain);
      if(api){
        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJsonApi);
      }else{
        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJson);
      }
      if (count > 0) {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
      }
      else {
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.NOTFOUND);
      }

      return ok(resultJson);
    }
}