package compornent;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.plaf.ListUI;

import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Crop;
import models.Field;
import models.FieldGroup;
import models.FieldGroupList;
import models.PosttoPoint;
import models.Weather;
import models.Work;
import models.WorkChain;
import models.WorkChainItem;
import models.WorkHistryBase;
import models.WorkLastTime;
import models.WorkTemplate;

import org.apache.commons.lang3.time.DateUtils;

import play.Logger;
import play.libs.Json;
import util.DateU;
import util.ListrU;
import util.StringU;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】アカウントコンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkChainCompornent implements AgryellInterface{

	/**
	 * コンストラクタ
	 */
	public WorkChainCompornent() {


	}

	/**
	 * 生産者IDより作業一覧を取得する
	 * @param pFarmId
	 * @param pListJson
	 * @return
	 */
	public static int getWorkOfFarmJson(double pFarmId, ObjectNode pListJson) {

    int result  = GET_SUCCESS;

    List<Work> works = Work.getWorkOfFarm(0);

    if (works.size() > 0) { //該当データが存在する場合
      for (Work work : works) {
        if (work.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , work.workId);
        jd.put("name" , work.workName);
        jd.put("flag" , 0);

        pListJson.put(String.valueOf(work.workId), jd);
      }
    }

    works = Work.getWorkOfFarm(pFarmId);

    if (works.size() > 0) { //該当データが存在する場合
      for (Work work : works) {
        ObjectNode jd = Json.newObject();

        jd.put("id"   , work.workId);
        jd.put("name" , work.workName);
        jd.put("flag" , 0);

        pListJson.put(String.valueOf(work.workId), jd);
      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
	}
  /**
   * 生産者IDより作業一覧を取得する(API専用)
   * @param pFarmId
   * @param pListJson
   * @return
   */
  public static ArrayNode getUniqueWorkOfFarmJson(double pFarmId) {

    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    UniqueList ul = new UniqueList();
    ArrayNode workList = mapper.createArrayNode();

    List<Work> works = Work.getWorkOfFarm(0);

    for (Work work : works) {
      ul.add(work.workId);
      ul.put(work.workId, work);
    }

    works = Work.getWorkOfFarm(pFarmId);

    for (Work work : works) {
      ul.add(work.workId);
      ul.put(work.workId, work);
    }

    List<Double> keys = ul.getKeys();

    for (Double key : keys) {
      ObjectNode jd = Json.newObject();

      Work work = (Work)ul.data(key);

      jd.put("id"   , work.workId);
      jd.put("name" , work.workName);
      jd.put("flag" , 0);

      workList.add(jd);

    }

    return workList;
  }

	/**
	 * 生産者ID、ワークチェインIDより作業一覧を取得する
	 * @param pFarmId
	 * @param pListJson
	 * @return
	 */
	public static int getWorkOfFarmJsonArray(double pFarmId, double pWockChainId, ArrayNode pListJson, double pLat, double pLng, int pRadius) {

    int result  = GET_SUCCESS;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList(pWockChainId);

    Logger.info("[getWorkOfFarmJsonArray]------------------------------------------------------------------------------");
    Logger.info("[getWorkOfFarmJsonArray] lat={} lng={} radius={}", pLat, pLng, pRadius);

    List<Compartment> compartments = Compartment.getCompartmentOfFarm(pFarmId);
    List<Double> compartmentkeys = new ArrayList<Double>();
    for (Compartment compartment : compartments) {
      if (compartment.deleteFlag == 1) { // 削除済みの場合
        continue;
      }
      compartmentkeys.add(compartment.kukakuId);
    }

    if (wcis.size() > 0) { //該当データが存在する場合
        for (WorkChainItem wc : wcis) {
          Work work = Work.find.where().eq("work_id", wc.workId).findUnique();

          if (work != null) {
            if (work.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode jd = Json.newObject();

            jd.put("id"         , work.workId);       //作業ID
            jd.put("name"       , work.workName);     //作業名
            jd.put("color"      , work.workColor);    //作業カラー
            jd.put("english"    , work.workEnglish);  //作業英字

            /* 次回作業対象となる区画情報を取得する */

            List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().eq("next_work_id", work.workId).in("kukaku_id", compartmentkeys).orderBy("kukaku_id").findList();

            int kukakuCount = 0;                //区画件数
            ArrayNode kukakuList = mapper.createArrayNode();
            List<Double> keys = new ArrayList<Double>();
            for (CompartmentStatus status : compartmentStatus) {
              ObjectNode cd = Json.newObject();

              if (status.nextWorkId != work.workId) {
                continue;
              }

              Compartment compartment = FieldComprtnent.getCompartment(status.kukakuId);
              FieldGroup fg = compartment.getFieldGroupInfo();
              Field fd = compartment.getFieldInfo();

              cd.put("kukakuId"   , status.kukakuId);                               //区画ID

              if (fg != null) { //圃場グループ
                  cd.put("fieldGroupId"       , fg.fieldGroupId);					//圃場グループID
              }
              if (fd != null) { //圃場
            	  cd.put("fieldId" , fd.fieldId);                                   //圃場ID
              }else{
            	  continue;
              }

              cd.put("kind" , AgryeelConst.RADIUSKUKAKU.TARGET);                  //種別
              kukakuList.add(cd);
              keys.add(status.kukakuId);

              kukakuCount++;

            }
            if (!(pLat == -1 && pLng == -1)) {
              for (Compartment ct : compartments) {

                if (keys.size() > 0) {
                  if (ListrU.keyCheck(keys, ct.kukakuId)) { continue; }
                }

                if (!ct.getDistance(pLat, pLng, pRadius)) { continue; }

                ObjectNode cd = Json.newObject();
                FieldGroup fg = ct.getFieldGroupInfo();
                Field fd = ct.getFieldInfo();

                cd.put("kukakuId"   , ct.kukakuId);                               //区画ID

                if (fg != null) { //圃場グループ
                    cd.put("fieldGroupId"       , fg.fieldGroupId);         //圃場グループID
                }
                if (fd != null) { //圃場
                  cd.put("fieldId" , fd.fieldId);                                   //圃場ID
                }else{
                  continue;
                }

                cd.put("kind" , AgryeelConst.RADIUSKUKAKU.RADIUS);                  //種別
                kukakuList.add(cd);

              }
            }

            if (kukakuCount == 0) {
                ObjectNode cd = Json.newObject();
              	cd.put("kukakuId"     , 0);
              	cd.put("fieldGroupId" , 0);
              	cd.put("fieldId"      , 0);
                cd.put("kind" , AgryeelConst.RADIUSKUKAKU.TARGET);                  //種別
                kukakuList.add(cd);
            }

            jd.put("kukakuList" , kukakuList);
            jd.put("workKukakuCount"  , kukakuCount);           					//作業対象区画件数
            pListJson.add(jd);
          }
        }
      }
      else {
        result  = GET_ERROR;
      }
      return result;
	}

	/**
	 * 生産者IDよりワークチェイン一覧を取得する
	 * @param pFarmId
	 * @param pListJson
	 * @return
	 */
  public static int getWorkChainOfFarmJson(double pFarmId, ObjectNode pListJson) {

    int result  = GET_SUCCESS;

    List<WorkChain> works = WorkChain.getWorkChainOfFarm(pFarmId);

    if (works.size() > 0) { //該当データが存在する場合
      for (WorkChain work : works) {
        if (work.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , work.workChainId);
        jd.put("name" , work.workChainName);
        jd.put("flag" , 0);

        pListJson.put(String.valueOf(work.workChainId), jd);
      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
  }
  public static int getWorkOfWorkChainJson(double pFarmId, double pWockChainId, ObjectNode pListJson) {

    int result  = GET_SUCCESS;
    Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);

    List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList(pWockChainId);
    Logger.debug("pFarmId >>>> " + pFarmId);
    Logger.debug("pWockChainId >>>> " + pWockChainId);

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

          /* 次回作業対象となる区画情報を取得する */
          List<Compartment> compartments = Compartment.getCompartmentOfFarm(pFarmId);
          List<Double> compartmentkeys = new ArrayList<Double>();
          for (Compartment compartment : compartments) {
            if (compartment.deleteFlag == 1) { // 削除済みの場合
              continue;
            }
            compartmentkeys.add(compartment.kukakuId);
          }

          List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().eq("next_work_id", work.workId).in("kukaku_id", compartmentkeys).orderBy("kukaku_id").findList();

          int kukakuCount = 0;                //区画件数
          ObjectNode cList = Json.newObject();
          for (CompartmentStatus status : compartmentStatus) {

            if (status.nextWorkId != work.workId) {
              continue;
            }

            Compartment compartment = FieldComprtnent.getCompartment(status.kukakuId);
            FieldGroup fg = compartment.getFieldGroupInfo();
            Field fd = compartment.getFieldInfo();

            ObjectNode cd = Json.newObject();

            cd.put("kukakuId"   , status.kukakuId);                                 //区画ID
            cd.put("kukakuName" , compartment.kukakuName);                          //区画名称
            cd.put("hinsyuName" , StringU.setNullTrim(status.hinsyuName));          //品種名称
            cd.put("hinsyuName" , StringU.setNullTrim(status.hinsyuName));          //品種名称
            long seiikuDayCount = 0;                                            //生育日数
            if (status.hashuDate != null && (status.hashuDate.compareTo(nullDate) != 0)) {  //播種日

              Date hashuDate = DateUtils.truncate(status.hashuDate, Calendar.DAY_OF_MONTH);
              Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

              cd.put("hashuDate"       , status.hashuDate.toString());

              seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);

            }
            else {
              cd.put("hashuDate"       , "");
              seiikuDayCount = 0;
            }

            cd.put("seiikuDayCount"    , seiikuDayCount);                           //生育日数

            if (fg != null) { //圃場グループカラー
              cd.put("fieldGroupColor" , fg.fieldGroupColor);
            }
            else {
              cd.put("fieldGroupColor" , "eeeeee");
            }
            if (fd != null) { //圃場
              cd.put("fieldName" , fd.fieldName);                                   //圃場名称
            }
            else {
              continue;
            }

            Crop cp = Crop.getCropInfo(status.cropId);

            if (cp != null) {
              cd.put("cropName" , cp.cropName);                                     //生産物名称
            }
            else {
              cd.put("cropName" , "");
            }

            cd.put("target" , "1");
            cList.put(Double.toString(status.kukakuId), cd);                        //区画情報を格納

            kukakuCount++;

          }

          //----- その他圃場を作成する -----
          ObjectNode cd = Json.newObject();
          cd.put("kukakuId"         , 0);               //区画ID
          cd.put("kukakuName"       , "その他の区画");  //区画名称
          cd.put("hinsyuName"       , "");              //品種名称
          cd.put("hashuDate"        , "");
          cd.put("seiikuDayCount"   , 0);               //生育日数
          cd.put("fieldGroupColor"  , "eeeeee");
          cd.put("fieldName"        , "");              //圃場名称
          cd.put("cropName"         , "");
          cd.put("target"           , "0");
          cList.put("0"             , cd);              //区画情報を格納

          jd.put("workKukakuList"   , cList);                 //作業対象区画
          jd.put("workKukakuCount"  , kukakuCount);           //作業対象区画件数
          pListJson.put(String.valueOf(work.workId), jd);
        }
      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
  }
  public static int getKukakuOfWorkJson(double pFarmId, double pWockId, ObjectNode pResultJson, double pLat, double pLng, int pRadius) {

    int result  = GET_SUCCESS;
    Date nullDate = DateUtils.truncate(DateU.GetNullDate(), Calendar.DAY_OF_MONTH);
    DecimalFormat dfSeq = new DecimalFormat("000");
    CompartmentStatusCompornent csc = new CompartmentStatusCompornent();
    csc.getAllData(pFarmId);

    Work work = Work.find.where().eq("work_id", pWockId).findUnique();

    if (work != null) {
      ObjectNode jd = Json.newObject();

      jd.put("workId"         , work.workId);       //作業ID
      jd.put("workName"       , work.workName);     //作業名
      jd.put("workMode"       , 0);                 //作業モード
      jd.put("workEnglish"    , work.workEnglish);  //作業英字名
      jd.put("workColor"      , work.workColor);    //作業カラー
      jd.put("workTemplateId" , work.workTemplateId);//テンプレートID

      /* 次回作業対象となる区画情報を取得する */
      List<Compartment> compartments = Compartment.getCompartmentOfFarm(pFarmId);
      List<Double> fieldkeys = new ArrayList<Double>();
      double oldFieldkey = 0;
      for (Compartment compartment : compartments) {
        if (compartment.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        if (oldFieldkey != compartment.fieldId) {
          fieldkeys.add(compartment.fieldId);
        }
        oldFieldkey = compartment.fieldId;
      }
//      List<FieldGroupList> fieldgs = FieldGroupList.find.where().in("field_id", fieldkeys).orderBy("field_group_id, field_id").findList();
//      oldFieldkey = 0;
//      fieldkeys.clear();
//      for (FieldGroupList fieldg : fieldgs) {
//        if (oldFieldkey != fieldg.fieldId) {
//          fieldkeys.add(fieldg.fieldId);
//        }
//        oldFieldkey = fieldg.fieldId;
//      }
//
//      List<Field> fields = Field.find.where().in("field_id", fieldkeys).orderBy("field_id").findList();
//
//      List<Double> compartmentkeys = new ArrayList<Double>();
//      for (Field field : fields) {
//        compartments = Compartment.getCompartmentOfField(pFarmId, field.fieldId);
//        for (Compartment compartment :compartments) {
//          if (compartment.fieldId == field.fieldId) {
//            compartmentkeys.add(compartment.kukakuId);
//          }
//        }
//      }

      int kukakuCount = 0;                //区画件数
      ObjectNode cList = Json.newObject();
      List<FieldGroupList> fieldgs = FieldGroupList.find.where().in("field_id", fieldkeys).orderBy("field_group_id, field_id").findList();
      for (FieldGroupList fieldg : fieldgs) {
        FieldComprtnent fc = new FieldComprtnent();
        fc.getFileld(fieldg.fieldId);
        compartments = fc.getCompartmentList();
        for (Compartment cpt : compartments) {

          CompartmentStatus status = CompartmentStatus.find.where().eq("kukaku_id", cpt.kukakuId).findUnique();
          if (status.nextWorkId != work.workId) {
            continue;
          }

          FieldGroup fg = cpt.getFieldGroupInfo();
          Field fd = cpt.getFieldInfo();

          ObjectNode cd = Json.newObject();

          cd.put("kukakuId"   , status.kukakuId);                                 //区画ID
          cd.put("workYear"   , status.workYear);                                 //作業年
          cd.put("rotationSpeedOfYear"   , status.rotationSpeedOfYear);           //年内回転数
          cd.put("hashuCount" , status.hashuCount);                               //播種回数
          cd.put("kukakuName" , cpt.kukakuName);                                  //区画名称
          cd.put("hinsyuName" , StringU.setNullTrim(status.hinsyuName));          //品種名称
          cd.put("hinsyuName" , StringU.setNullTrim(status.hinsyuName));          //品種名称
          long seiikuDayCount = 0;                                            //生育日数
          long seiikuDayCountEnd  = 0;
          if (status.hashuDate != null && (status.hashuDate.compareTo(nullDate) != 0)) {  //播種日

            Date hashuDate = DateUtils.truncate(status.hashuDate, Calendar.DAY_OF_MONTH);
            Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

            cd.put("hashuDate"       , status.hashuDate.toString());
            //生育日数の表示方法を変更
//            if (status.seiikuDayCount != 0) {
//              seiikuDayCount = status.seiikuDayCount;
//            }
//            else {
//              seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//            }
            if (status.shukakuStartDate != null && (status.shukakuStartDate.compareTo(nullDate) != 0)) {  //収穫開始日
              seiikuDayCount = DateU.GetDiffDate(hashuDate, status.shukakuStartDate);
            }
            else {
              seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
            }
            if (status.shukakuEndDate != null && (status.shukakuEndDate.compareTo(nullDate) != 0)) {  //収穫開始日
              seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, status.shukakuEndDate);
            }
            else {
              seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, systemDate);
            }
          }
          else {
            cd.put("hashuDate"       , "");
            seiikuDayCount = 0;
            seiikuDayCountEnd = 0;
          }

          cd.put("seiikuDayCount"    , seiikuDayCount);                           //生育日数
          cd.put("seiikuDayCountEnd" , seiikuDayCountEnd);                        //生育日数（収穫終了日）

          cd.put("fieldGroupColor"    , "b71c1c");
          cd.put("fieldGroupId"       , "-2");
          cd.put("fieldGroupName"     , "対象区画");
          cd.put("sequenceId" , dfSeq.format(-2) + dfSeq.format(0) + dfSeq.format(cpt.kukakuId)); //並び順

//          if (fg != null) { //圃場グループカラー
//            cd.put("fieldGroupColor" , fg.fieldGroupColor);
//          }
//          else {
//            cd.put("fieldGroupColor" , "eeeeee");
//          }
          if (fd != null) { //圃場
            cd.put("fieldId"    , fd.fieldId);                                   //圃場ID
            cd.put("fieldName"  , fd.fieldName);                                 //圃場名称
          }
          else {
            continue;
          }

          Crop cp = Crop.getCropInfo(status.cropId);

          if (cp != null) {
            cd.put("cropName" , cp.cropName);                                     //生産物名称
          }
          else {
            cd.put("cropName" , "");
          }
          DecimalFormat df = new DecimalFormat("#,##0.0");
          SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 積算温度
          //----------------------------------------------------------------------------------------------------------------------------------
          cd.put("totalSolarRadiation"    , df.format(status.totalSolarRadiation));                   //積算温度
          double totalDatas = 0;
          Weather oWeather      = null;
          //----- 積算温度の算出 -----
          if ((status.hashuDate != null)
              && (status.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

            Compartment ct = Compartment.getCompartmentInfo(status.kukakuId);
            if (ct != null) {
              if (fd != null) {
                String pointId = PosttoPoint.getPointId(fd.postNo);
                if (pointId != null && !"".equals(pointId)) {
                  java.sql.Date endDate;

                  if ((status.shukakuStartDate != null)
                      && (status.shukakuStartDate.compareTo(nullDate) != 0)) { //収穫開始が正しく登録されている場合
                    endDate = status.shukakuStartDate;
                  }
                  else {
                    Calendar cal = Calendar.getInstance();
                    DateU.setTime(cal, DateU.TimeType.TO);
                    cal.add(Calendar.MONTH, 2);
                    endDate = new java.sql.Date(cal.getTime().getTime());
                  }

                  List<Weather> weathers = Weather.getWeather(pointId, status.hashuDate, endDate);

                  for (Weather weather : weathers) {
                    totalDatas += weather.kionAve;
                    oWeather = weather;
                  }
                }
              }
            }
          }
          if (oWeather != null) {
            cd.put("yosokuSolarDate"      , sdf.format(oWeather.dayDate));
          }
          else {
            cd.put("yosokuSolarDate"      , "");
          }
          cd.put("yosokuSolarRadiation"   , df.format(totalDatas));
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 積算降水量
          //----------------------------------------------------------------------------------------------------------------------------------
          double totalDatar = 0;
          //----- 積算温度の算出 -----
          if ((status.hashuDate != null)
              && (status.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

            Compartment ct = Compartment.getCompartmentInfo(status.kukakuId);
            if (ct != null) {
              Field fd2 = ct.getFieldInfo();
              if (fd2 != null) {
                String pointId = PosttoPoint.getPointId(fd2.postNo);
                if (pointId != null && !"".equals(pointId)) {
                  java.sql.Date endDate;

                  Calendar cal = Calendar.getInstance();
                  DateU.setTime(cal, DateU.TimeType.TO);
                  endDate = new java.sql.Date(cal.getTime().getTime());

                  List<Weather> weathers = Weather.getWeather(pointId, status.hashuDate, endDate);

                  for (Weather weather : weathers) {
                    if (weather.jituyo == AgryeelConst.JITUYO.JISEKI) {
                      totalDatar += weather.rain;
                    }
                  }
                }
              }
            }
          }
          cd.put("rain"   , df.format(totalDatar));
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 消毒
          //----------------------------------------------------------------------------------------------------------------------------------
          long disinfectionCount = 0;
          if (status.finalDisinfectionDate != null && (status.finalDisinfectionDate.compareTo(nullDate) != 0)) {

            Date finalDisinfectionDate = DateUtils.truncate(status.finalDisinfectionDate, Calendar.DAY_OF_MONTH);

            cd.put("finalDisinfectionDate"       , status.finalDisinfectionDate.toString());

            disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, systemDate);

          }
          else {
            cd.put("finalDisinfectionDate"       , "");
            disinfectionCount = 0;
          }

          cd.put("disinfectionCount"          , disinfectionCount);                                               //最終消毒日からの経過日数
          cd.put("totalDisinfectionNumber"    , df.format(status.totalDisinfectionNumber * 0.001));//合計消毒量
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 潅水量
          //----------------------------------------------------------------------------------------------------------------------------------
          long kansuiCount = 0;
          if (status.finalKansuiDate != null && (status.finalKansuiDate.compareTo(nullDate) != 0)) {

            Date finalKansuiDate = DateUtils.truncate(status.finalKansuiDate, Calendar.DAY_OF_MONTH);

            cd.put("finalKansuiDate"       , status.finalKansuiDate.toString());

            kansuiCount = DateU.GetDiffDate(finalKansuiDate, systemDate);

          }
          else {
            cd.put("finalKansuiDate"       , "");
            kansuiCount = 0;
          }

          cd.put("kansuiCount"          , kansuiCount);                                       //最終潅水日からの経過日数
          cd.put("totalKansuiNumber"    , df.format(status.totalKansuiNumber));//合計潅水量
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 追肥
          //----------------------------------------------------------------------------------------------------------------------------------
          long tuihiCount = 0;
          if (status.finalTuihiDate != null && (status.finalTuihiDate.compareTo(nullDate) != 0)) {

            Date finalTuihiDate = DateUtils.truncate(status.finalTuihiDate, Calendar.DAY_OF_MONTH);

            cd.put("finalTuihiDate"       , status.finalTuihiDate.toString());

            tuihiCount = DateU.GetDiffDate(finalTuihiDate, systemDate);

          }
          else {
            cd.put("finalTuihiDate"       , "");
            tuihiCount = 0;
          }

          cd.put("tuihiCount"          , tuihiCount);                                       //最終追肥日からの経過日数
          cd.put("totalTuihiNumber"    , df.format(status.totalTuihiNumber * 0.001));//合計追肥量
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 収穫
          //----------------------------------------------------------------------------------------------------------------------------------
          if (status.shukakuStartDate != null && (status.shukakuStartDate.compareTo(nullDate) != 0)) {

            cd.put("shukakuStartDate"       , status.shukakuStartDate.toString());

          }
          else {
            cd.put("shukakuStartDate"       , "");
          }
          if (status.shukakuEndDate != null && (status.shukakuEndDate.compareTo(nullDate) != 0)) {

            cd.put("shukakuEndDate"       , status.shukakuEndDate.toString());

          }
          else {
            cd.put("shukakuEndDate"       , "");
          }

          cd.put("totalShukakuNumber"    , df.format(status.totalShukakuCount));        //合計収穫量
          if ((status.totalShukakuCount == 0)
              || (cpt.area == 0)) {
            cd.put("tanshu"              , "*****");
          }
          else {
            cd.put("tanshu"              , df.format((status.totalShukakuCount / cpt.area) * 10));
          }

          cd.put("target" , "1");

          //----- ワークチェーン情報の生成 -----
          ObjectNode chainJson = Json.newObject();
          csc.getWorkChainStatusJson(cpt.kukakuId, chainJson);
          cd.put("chain", chainJson);

          //----- 作業中情報の生成 -----
          ObjectNode  aj   = Json.newObject();
          UserComprtnent uc = new UserComprtnent();
          uc.getNowWorkingByField(fd.fieldId, aj);
          cd.put("working", aj);

          cList.put(Double.toString(status.kukakuId), cd);                        //区画情報を格納

        }
        for (Compartment cpt : compartments) {

          if (cpt.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          CompartmentStatus status = CompartmentStatus.find.where().eq("kukaku_id", cpt.kukakuId).findUnique();
          if (status.nextWorkId == work.workId) {
            continue;
          }

          FieldGroup fg = cpt.getFieldGroupInfo();
          Field fd = cpt.getFieldInfo();

          ObjectNode cd = Json.newObject();

          cd.put("kukakuId"   , status.kukakuId);                                 //区画ID
          cd.put("workYear"   , status.workYear);                                 //作業年
          cd.put("rotationSpeedOfYear"   , status.rotationSpeedOfYear);           //年内回転数
          cd.put("hashuCount" , status.hashuCount);                               //播種回数
          cd.put("kukakuName" , cpt.kukakuName);                                  //区画名称
          cd.put("hinsyuName" , StringU.setNullTrim(status.hinsyuName));          //品種名称
          cd.put("hinsyuName" , StringU.setNullTrim(status.hinsyuName));          //品種名称
          long seiikuDayCount = 0;                                            //生育日数
          long seiikuDayCountEnd  = 0;
          if (status.hashuDate != null && (status.hashuDate.compareTo(nullDate) != 0)) {  //播種日

            Date hashuDate = DateUtils.truncate(status.hashuDate, Calendar.DAY_OF_MONTH);
            Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);

            cd.put("hashuDate"       , status.hashuDate.toString());
            //生育日数の表示方法を変更
//            if (status.seiikuDayCount != 0) {
//              seiikuDayCount = status.seiikuDayCount;
//            }
//            else {
//              seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
//            }
            if (status.shukakuStartDate != null && (status.shukakuStartDate.compareTo(nullDate) != 0)) {  //収穫開始日
              seiikuDayCount = DateU.GetDiffDate(hashuDate, status.shukakuStartDate);
            }
            else {
              seiikuDayCount = DateU.GetDiffDate(hashuDate, systemDate);
            }
            if (status.shukakuEndDate != null && (status.shukakuEndDate.compareTo(nullDate) != 0)) {  //収穫開始日
              seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, status.shukakuEndDate);
            }
            else {
              seiikuDayCountEnd = DateU.GetDiffDate(hashuDate, systemDate);
            }

          }
          else {
            cd.put("hashuDate"       , "");
            seiikuDayCount = 0;
            seiikuDayCountEnd = 0;
          }

          cd.put("seiikuDayCount"    , seiikuDayCount);                           //生育日数
          cd.put("seiikuDayCountEnd" , seiikuDayCountEnd);                        //生育日数(収穫終了日)

          if (cpt.getDistance(pLat, pLng, pRadius)) {
            cd.put("fieldGroupColor"    , "b71c1c");
            cd.put("fieldGroupId"       , "-1");
            cd.put("fieldGroupName"     , "付近区画");
            cd.put("sequenceId" , dfSeq.format(-1) + dfSeq.format(0) + dfSeq.format(cpt.sequenceId)); //並び順
          }
          else {
            if (fg != null) { //圃場グループカラー
              cd.put("fieldGroupColor" , fg.fieldGroupColor);
              cd.put("fieldGroupId"       , fg.fieldGroupId);
              cd.put("fieldGroupName"     , fg.fieldGroupName);
              cd.put("sequenceId" , dfSeq.format(fg.sequenceId) + dfSeq.format(0) + dfSeq.format(cpt.sequenceId)); //並び順
            }
          }
          if (fd != null) { //圃場
            cd.put("fieldId"    , fd.fieldId);                                   //圃場ID
            cd.put("fieldName"  , fd.fieldName);                                 //圃場名称
          }
          else {
            continue;
          }

          Crop cp = Crop.getCropInfo(status.cropId);

          if (cp != null) {
            cd.put("cropName" , cp.cropName);                                     //生産物名称
          }
          else {
            cd.put("cropName" , "");
          }
          DecimalFormat df = new DecimalFormat("#,##0.0");
          SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
          Date systemDate = DateUtils.truncate(Calendar.getInstance().getTime(), Calendar.DAY_OF_MONTH);
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 積算温度
          //----------------------------------------------------------------------------------------------------------------------------------
          cd.put("totalSolarRadiation"    , df.format(status.totalSolarRadiation));                   //積算温度
          double totalDatas = 0;
          Weather oWeather      = null;
          //----- 積算温度の算出 -----
          if ((status.hashuDate != null)
              && (status.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

            Compartment ct = Compartment.getCompartmentInfo(status.kukakuId);
            if (ct != null) {
              if (fd != null) {
                String pointId = PosttoPoint.getPointId(fd.postNo);
                if (pointId != null && !"".equals(pointId)) {
                  java.sql.Date endDate;

                  if ((status.shukakuStartDate != null)
                      && (status.shukakuStartDate.compareTo(nullDate) != 0)) { //収穫開始が正しく登録されている場合
                    endDate = status.shukakuStartDate;
                  }
                  else {
                    Calendar cal = Calendar.getInstance();
                    DateU.setTime(cal, DateU.TimeType.TO);
                    cal.add(Calendar.MONTH, 2);
                    endDate = new java.sql.Date(cal.getTime().getTime());
                  }

                  List<Weather> weathers = Weather.getWeather(pointId, status.hashuDate, endDate);

                  for (Weather weather : weathers) {
                    totalDatas += weather.kionAve;
                    oWeather = weather;
                  }
                }
              }
            }
          }
          if (oWeather != null) {
            cd.put("yosokuSolarDate"      , sdf.format(oWeather.dayDate));
          }
          else {
            cd.put("yosokuSolarDate"      , "");
          }
          cd.put("yosokuSolarRadiation"   , df.format(totalDatas));
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 積算降水量
          //----------------------------------------------------------------------------------------------------------------------------------
          double totalDatar = 0;
          //----- 積算温度の算出 -----
          if ((status.hashuDate != null)
              && (status.hashuDate.compareTo(nullDate) != 0)) { //播種日が正しく登録されている場合

            Compartment ct = Compartment.getCompartmentInfo(status.kukakuId);
            if (ct != null) {
              Field fd2 = ct.getFieldInfo();
              if (fd2 != null) {
                String pointId = PosttoPoint.getPointId(fd2.postNo);
                if (pointId != null && !"".equals(pointId)) {
                  java.sql.Date endDate;

                  Calendar cal = Calendar.getInstance();
                  DateU.setTime(cal, DateU.TimeType.TO);
                  endDate = new java.sql.Date(cal.getTime().getTime());

                  List<Weather> weathers = Weather.getWeather(pointId, status.hashuDate, endDate);

                  for (Weather weather : weathers) {
                    if (weather.jituyo == AgryeelConst.JITUYO.JISEKI) {
                      totalDatar += weather.rain;
                    }
                  }
                }
              }
            }
          }
          cd.put("rain"   , df.format(totalDatar));
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 消毒
          //----------------------------------------------------------------------------------------------------------------------------------
          long disinfectionCount = 0;
          if (status.finalDisinfectionDate != null && (status.finalDisinfectionDate.compareTo(nullDate) != 0)) {

            Date finalDisinfectionDate = DateUtils.truncate(status.finalDisinfectionDate, Calendar.DAY_OF_MONTH);

            cd.put("finalDisinfectionDate"       , status.finalDisinfectionDate.toString());

            disinfectionCount = DateU.GetDiffDate(finalDisinfectionDate, systemDate);

          }
          else {
            cd.put("finalDisinfectionDate"       , "");
            disinfectionCount = 0;
          }

          cd.put("disinfectionCount"          , disinfectionCount);                                               //最終消毒日からの経過日数
          cd.put("totalDisinfectionNumber"    , df.format(status.totalDisinfectionNumber * 0.001));//合計消毒量
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 潅水量
          //----------------------------------------------------------------------------------------------------------------------------------
          long kansuiCount = 0;
          if (status.finalKansuiDate != null && (status.finalKansuiDate.compareTo(nullDate) != 0)) {

            Date finalKansuiDate = DateUtils.truncate(status.finalKansuiDate, Calendar.DAY_OF_MONTH);

            cd.put("finalKansuiDate"       , status.finalKansuiDate.toString());

            kansuiCount = DateU.GetDiffDate(finalKansuiDate, systemDate);

          }
          else {
            cd.put("finalKansuiDate"       , "");
            kansuiCount = 0;
          }

          cd.put("kansuiCount"          , kansuiCount);                                       //最終潅水日からの経過日数
          cd.put("totalKansuiNumber"    , df.format(status.totalKansuiNumber));//合計潅水量
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 追肥
          //----------------------------------------------------------------------------------------------------------------------------------
          long tuihiCount = 0;
          if (status.finalTuihiDate != null && (status.finalTuihiDate.compareTo(nullDate) != 0)) {

            Date finalTuihiDate = DateUtils.truncate(status.finalTuihiDate, Calendar.DAY_OF_MONTH);

            cd.put("finalTuihiDate"       , status.finalTuihiDate.toString());

            tuihiCount = DateU.GetDiffDate(finalTuihiDate, systemDate);

          }
          else {
            cd.put("finalTuihiDate"       , "");
            tuihiCount = 0;
          }

          cd.put("tuihiCount"          , tuihiCount);                                       //最終追肥日からの経過日数
          cd.put("totalTuihiNumber"    , df.format(status.totalTuihiNumber * 0.001));//合計追肥量
          //----------------------------------------------------------------------------------------------------------------------------------
          //- 収穫
          //----------------------------------------------------------------------------------------------------------------------------------
          if (status.shukakuStartDate != null && (status.shukakuStartDate.compareTo(nullDate) != 0)) {

            cd.put("shukakuStartDate"       , status.shukakuStartDate.toString());

          }
          else {
            cd.put("shukakuStartDate"       , "");
          }
          if (status.shukakuEndDate != null && (status.shukakuEndDate.compareTo(nullDate) != 0)) {

            cd.put("shukakuEndDate"       , status.shukakuEndDate.toString());

          }
          else {
            cd.put("shukakuEndDate"       , "");
          }

          cd.put("totalShukakuNumber"    , df.format(status.totalShukakuCount));        //合計収穫量
          if ((status.totalShukakuCount == 0)
              || (cpt.area == 0)) {
            cd.put("tanshu"              , "*****");
          }
          else {
            cd.put("tanshu"              , df.format((status.totalShukakuCount / cpt.area) * 10));
          }

          cd.put("target" , "0");

          //----- ワークチェーン情報の生成 -----
          ObjectNode chainJson = Json.newObject();
          csc.getWorkChainStatusJson(cpt.kukakuId, chainJson);
          cd.put("chain", chainJson);

          //----- 作業中情報の生成 -----
          ObjectNode  aj   = Json.newObject();
          UserComprtnent uc = new UserComprtnent();
          uc.getNowWorkingByField(fd.fieldId, aj);
          cd.put("working", aj);

          cList.put(Double.toString(status.kukakuId), cd);                        //区画情報を格納

        }
      }
      jd.put("workKukakuList"   , cList);                 //作業対象区画
      pResultJson.put("data", jd);
    }
    else {
      result  = GET_ERROR;
    }
    return result;
  }
  public static int getWorkOfKukakuJson(double pFarmId, double pWockChainId, ObjectNode pListJson) {

    int result  = GET_SUCCESS;

    List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList(pWockChainId);

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

          jd.put("id"   , work.workId);
          jd.put("name" , work.workName);

          pListJson.put(String.valueOf(wc.sequenceId), jd);
        }
      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
  }
  public static int getWorkOfKukakuJsonArray(double pFarmId, double pWockChainId, ArrayNode pListJson) {

    int result  = GET_SUCCESS;

    List<WorkChainItem> wcis = WorkChainItem.getWorkChainItemList(pWockChainId);

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

          jd.put("id"   , work.workId);
          jd.put("name" , work.workName);
          jd.put("flag" , 0);

          pListJson.add(jd);
        }
      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
  }
  /**
   * 作業テンプレート情報を取得する
   * @param pWorkId
   * @return
   */
  public static WorkTemplate getTemplateOfWork(double pWorkId) {

    Work work = Work.getWork(pWorkId);

    if (work == null) {
      return null;
    }

    return WorkTemplate.getWorkTemplate(work.workTemplateId);

  }
  /**
   * 作業情報を取得する
   * @param pWorkId
   * @return
   */
  public static Work getWork(double pWorkId) {

    return Work.getWork(pWorkId);
  }
  /**
   * 作業前回情報を取得する
   * @param pWorkId
   * @param pFarmId
   * @param pCropId
   * @return
   */
  public static WorkLastTime getWorkLastTime(double pWorkId, double pFarmId , double pCropId) {

    return WorkLastTime.getWorkLastTime(pWorkId, pFarmId , pCropId);
  }
  /**
   * 作業前回散布情報を取得する
   * @param pWorkId
   * @param pFarmId
   * @param pCropId
   * @return
   */
  public static WorkHistryBase getWorkHistryBase(double pWorkId, double pFarmId , double pCropId) {
    return WorkHistryBase.getWorkHistryBase(pWorkId, pFarmId, pCropId);
  }
  /**
   * 区画ワークチェイン状況情報を取得する
   * @param pKukakuId
   * @return
   */
  public static CompartmentWorkChainStatus getCompartmentWorkChainStatus(double pKukakuId) {
    return CompartmentWorkChainStatus.find.where().eq("kukaku_id", pKukakuId).findUnique();
  }
}
