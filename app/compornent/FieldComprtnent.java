package compornent;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Farm;
import models.FarmStatus;
import models.Field;
import models.FieldGroup;
import models.FieldGroupList;
import models.Landlord;
import models.MotochoBase;
import models.Sequence;
import play.libs.Json;
import util.DateU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.SystemBatch;

/**
 * 【AGRYEEL】圃場コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class FieldComprtnent implements AgryellInterface{

  List<Field> fields;
  public Field field;

	/**
	 * コンストラクタ
	 */
	public FieldComprtnent() {

	  fields = Field.find.orderBy("field_id").findList();
	  field = null;

	}
	/**
	 * コンストラクタ
	 * @param farmId
	 */
  public FieldComprtnent(double farmId) {

    fields = Field.find.where().eq("farm_id", farmId).orderBy("field_id").findList();
    field = null;

  }

  /**
   * 圃場IDより圃場情報を取得します
   * @param fieldId
   * @return
   */
  public Field getFileld(double fieldId) {
    Field result  = null;
    this.field    = null;
    for (Field field : fields) {
      if (field.fieldId == fieldId) {
        result = field;
        this.field = field;
        break;
      }
    }
    return result;
  }

	/**
	 * 圃場一覧を取得し、JSONに格納する
	 * @param pListJson
	 * @return
	 */
  public int getFieldJson(ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    if (fields.size() > 0) { //生産物が存在する場合

        for (Field field : fields) {
          ObjectNode jd = Json.newObject();

          jd.put("id"   , field.fieldId);
          jd.put("name" , field.fieldName);

          pListJson.put(String.valueOf(field.fieldId), jd);

        }

    }
    else {
      result  = GET_ERROR;
    }

    return result;

  }
  /**
   * 生産者情報を取得する
   * @return
   */
  public Farm getFarmInfo() {
    Farm farm = null;

    if (this.field != null) {
      farm = this.field.getFarmInfo();
    }
    return farm;
  }
  /**
   * 地主情報を取得する
   * @return
   */
  public Landlord getLandlordInfo() {
    Landlord landlord = null;

    if (this.field != null) {
      landlord = this.field.getLandlordInfo();
    }
    return landlord;
  }
  /**
   * 圃場配下の区画情報を取得する
   * @return
   */
  public List<Compartment> getCompartmentList() {

    List<Compartment> compartments = new ArrayList<Compartment>();
    if (this.field != null) {
      compartments = Compartment.find.where().eq("field_id", this.field.fieldId).order("sequence_id, kukaku_id").findList();
    }
    return compartments;
  }
  /**
   * 圃場配下の区画状況一覧を取得する
   * @return
   */
  public List<CompartmentStatus> getCompartmentStatusList() {

    List<CompartmentStatus> compartmentStatuss = new ArrayList<CompartmentStatus>();
    if (this.field != null) {
      List<Compartment> compartments = Compartment.find.where().eq("field_id", this.field.fieldId).findList();
      for (Compartment compartment : compartments) {
        CompartmentStatus compartmentStatus = CompartmentStatus.find.where().eq("kukaku_id", compartment.kukakuId).findUnique();
        if (compartmentStatus != null) {
          compartmentStatuss.add(compartmentStatus);
        }
      }
    }
    return compartmentStatuss;
  }
  /**
   * 生産者IDから圃場グループを取得する
   * @param farmId
   * @return
   */
  public static List<FieldGroup> getFieldGroupOfFarm(double farmId) {
    return FieldGroup.getFieldGroupOfFarm(farmId);
  }
  /**
   * 圃場グループIDから所属している圃場情報を取得します
   * @param pFieldGroupId
   * @return
   */
  public static List<Field> getField(double pFieldGroupId) {
    return FieldGroupList.getField(pFieldGroupId);
  }
  /**
   * 圃場グループIDから圃場グループ情報を取得します
   * @param pFieldGroupId
   * @return
   */
  public static FieldGroup getFieldGroup(double pFieldGroupId) {
    return FieldGroup.getFieldGroup(pFieldGroupId);
  }
  /**
   * 区画IDから区画情報を取得する
   * @return
   */
  public static Compartment getCompartment(double pKukakuId) {
    return Compartment.find.where().eq("kukaku_id", pKukakuId).findUnique();
  }
  /**
   * 区画IDから区画状況情報を取得する
   * @return
   */
  public static CompartmentStatus getCompartmentStatus(double pKukakuId) {
    return CompartmentStatus.find.where().eq("kukaku_id", pKukakuId).findUnique();
  }
  public static CompartmentStatus getCompartmentStatusFromMotocho(double pKukakuId, int year, int rotation) {
    CompartmentStatus status = null;
    if (year != 0 && rotation != 0) {
      MotochoBase motocho = MotochoBase.find.where().eq("kukaku_id", pKukakuId).eq("work_year", year).eq("rotation_speed_of_year", rotation).findUnique();
      if (motocho != null) {
        status = new CompartmentStatus();
        status.kukakuId = motocho.kukakuId;
        status.workYear = motocho.workYear;
        status.rotationSpeedOfYear = motocho.rotationSpeedOfYear;
        status.hinsyuId = motocho.hinsyuId;
        status.cropId = motocho.cropId;
        status.hinsyuName = motocho.hinsyuName;
        status.hashuDate = motocho.hashuDate;
        status.seiikuDayCount = motocho.seiikuDayCount;
        status.nowEndWork = "";
        status.finalDisinfectionDate = motocho.finalDisinfectionDate;
        status.finalKansuiDate = motocho.finalKansuiDate;
        status.finalTuihiDate = motocho.finalTuihiDate;
        status.shukakuStartDate = motocho.shukakuStartDate;
        status.shukakuEndDate = motocho.shukakuEndDate;
        status.totalDisinfectionCount = motocho.totalDisinfectionCount;
        status.totalKansuiCount = motocho.totalKansuiCount;
        status.totalTuihiCount = motocho.totalTuihiCount;
        status.totalShukakuCount = motocho.totalShukakuCount;
        status.totalSolarRadiation = motocho.totalSolarRadiation;
        status.totalDisinfectionNumber = motocho.totalDisinfectionNumber;
        status.totalKansuiNumber = motocho.totalKansuiNumber;
        status.totalTuihiNumber = motocho.totalTuihiNumber;
        status.totalShukakuNumber = motocho.totalShukakuNumber;
        status.oldDisinfectionCount = 0;
        status.oldKansuiCount = 0;
        status.oldTuihiCount = 0;
        status.oldShukakuCount = 0;
        status.oldSolarRadiation = 0;
        status.nowWorkMode = 0;
        status.endWorkId = 0;
        status.finalEndDate = motocho.workEndDay;
        status.nextWorkId = 0;
        status.workColor = "";
        status.katadukeDate = motocho.workStartDay;
        status.hashuCount = motocho.hashuCount;
      }
    }
    else {
      status = CompartmentStatus.find.where().eq("kukaku_id", pKukakuId).findUnique();
    }
    return status;
  }
  /**
   * アカウントIDから圃場グループを取得し、JSONに格納します。
   * @param pAccountId
   * @return
   */
  public static int getFieldGroupWhere(String pAccountId, ObjectNode pListJson) {
    /** 戻り値 */
    int result  = GET_SUCCESS;
    UserComprtnent uc = new UserComprtnent();
    uc.GetAccountData(pAccountId);

    List<FieldGroup> fgs = getFieldGroupOfFarm(uc.accountData.farmId);
    if (fgs.size() > 0) {
      for (FieldGroup fg : fgs) {
        if (fg.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , fg.fieldGroupId);
        jd.put("name" , fg.fieldGroupName);
        if (uc.checkFieldGroupSelect(fg.fieldGroupId)) {
          jd.put("flag" , 1);
        }
        else {
          jd.put("flag" , 0);
        }
        pListJson.put(String.valueOf(fg.fieldGroupId), jd);

      }
    }
    else {
      result  = GET_ERROR;
    }
    return result;
  }

  /**
   * 生産者ID、圃場グループIDより圃場一覧を取得し、JSONに格納する（自所属圃場）
   * @param pListJson
   * @return
   */
  public static int getFieldOfFarmSelJson(double pFarmId, double pFieldGroupId, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    List<Field> field = Field.getFieldOfFarm(pFarmId);

    for (Field fieldData : field) {
//    	/* 既に圃場グループに所属していないかチェック */
//    	if(CheckFieldExist(pFieldGroupId,fieldData.fieldId)){
//    		continue;
//    	}
        if (fieldData.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , fieldData.fieldId);
        jd.put("name" , fieldData.fieldName);
        FieldGroupList groupField = FieldGroupList.getFieldUnique(pFieldGroupId,fieldData.fieldId);
        if(groupField != null){
           jd.put("flag" , 1);
        }else{
    	   jd.put("flag" , 0);
        }
        pListJson.put(String.valueOf(fieldData.fieldId), jd);
    }

    return result;
  }

  /**
   * 生産者IDから区画を取得し、JSONに格納します。
   * @param pAccountId
   * @return
   */
  public static int getCompartmentOfFarmJsonArray(Double pFarmId, ArrayNode pListJson) {
    /** 戻り値 */
    int result  = GET_SUCCESS;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    //圃場グループ取得
    List<FieldGroup> fgs = getFieldGroupOfFarm(pFarmId);
    if (fgs.size() > 0) {
      for (FieldGroup fg : fgs) {
        ObjectNode jd = Json.newObject();

        jd.put("fieldGroupId"    , fg.fieldGroupId);
        jd.put("fieldGroupName"  , fg.fieldGroupName);
        jd.put("fieldGroupColor" , fg.fieldGroupColor);
        jd.put("fieldGroupSequenceId" , fg.sequenceId);

        //圃場取得
        ArrayNode fieldList = mapper.createArrayNode();
        List<Field> fs = getField(fg.fieldGroupId);
        if (fs.size() > 0) {
            for (Field f : fs) {
              ObjectNode jf = Json.newObject();

              jf.put("fieldId"    , f.fieldId);
              jf.put("fieldName"  , f.fieldName);

              //区画取得
              ArrayNode kukakuList = mapper.createArrayNode();
              List<Compartment> ks = Compartment.getCompartmentOfField(pFarmId, f.fieldId);
              if (ks.size() > 0) {
                  for (Compartment k : ks) {
                    ObjectNode jk = Json.newObject();

                    jk.put("kukakuId"    , k.kukakuId);
                    jk.put("kukakuName"  , k.kukakuName);
                    jk.put("kukakuSequenceId"  , k.sequenceId);

                    List<CompartmentStatus> stss = CompartmentStatus.getStatusOfCompartment(k.kukakuId);
                    if (stss.size() > 0) {
                        for (CompartmentStatus sts : stss) {
                          jk.put("rotationSpeedOfYear"    , sts.rotationSpeedOfYear);
                        }
                    }else{
                          jk.put("rotationSpeedOfYear"    , 0);
                    }
                    kukakuList.add(jk);
                  }
                  jf.put("kukakuList" , kukakuList);
                  fieldList.add(jf);
              }
            }
            jd.put("fieldList" , fieldList);
        }

        pListJson.add(jd);
      }
    }
    else {
      result  = GET_ERROR;
    }

    return result;
  }

  /**
   * 生産者IDから区画を取得し、JSONに格納します。
   * @param pAccountId
   * @return
   */
  public static int getCompartmentOfFarmRadius(double pFarmId, double pWockId, double pLat, double pLng, int pRadius, ArrayNode pListJson) {
    /** 戻り値 */
    int result  = GET_SUCCESS;
	ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    //圃場グループ取得
    List<FieldGroup> fgs = getFieldGroupOfFarm(pFarmId);
    if (fgs.size() > 0) {
      for (FieldGroup fg : fgs) {
        if (fg.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("fieldGroupId"    , fg.fieldGroupId);
        jd.put("fieldGroupName"  , fg.fieldGroupName);
        jd.put("fieldGroupColor" , fg.fieldGroupColor);
        jd.put("fieldGroupSequenceId" , fg.sequenceId);

        //圃場取得
        ArrayNode fieldList = mapper.createArrayNode();
        List<Field> fs = getField(fg.fieldGroupId);
        if (fs.size() > 0) {
            for (Field f : fs) {
              if (f.deleteFlag == 1) { // 削除済みの場合
                continue;
              }

              ObjectNode jf = Json.newObject();

              jf.put("fieldId"    , f.fieldId);
              jf.put("fieldName"  , f.fieldName);

              //区画取得
              ArrayNode kukakuList = mapper.createArrayNode();
              List<Compartment> ks = Compartment.getCompartmentOfField(pFarmId, f.fieldId);
              if (ks.size() > 0) {
                  for (Compartment k : ks) {
                    if (k.deleteFlag == 1) { // 削除済みの場合
                      continue;
                    }

                    ObjectNode jk = Json.newObject();

                    jk.put("kukakuId"    , k.kukakuId);
                    jk.put("kukakuName"  , k.kukakuName);
                    jk.put("kukakuSequenceId"  , k.sequenceId);

                    CompartmentStatus status = CompartmentStatus.find.where().eq("kukaku_id", k.kukakuId).findUnique();
                    if (pLat == 0 && pLng == 0) {
                      if (status.nextWorkId == pWockId) {
                        jk.put("kind"  , -1);
                      }
                      else {
                        jk.put("kind"  , 0);
                      }
                    }
                    else {
                      if (k.getDistance(pLat, pLng, pRadius)) {
                        jk.put("kind"  , -2);
                      }
                      else {
                        if (status.nextWorkId == pWockId) {
                          jk.put("kind"  , -1);
                        }
                        else {
                          jk.put("kind"  , 0);
                        }
                      }
                    }
                    List<CompartmentStatus> stss = CompartmentStatus.getStatusOfCompartment(k.kukakuId);
                    if (stss.size() > 0) {
                        for (CompartmentStatus sts : stss) {
                          jk.put("rotationSpeedOfYear"    , sts.rotationSpeedOfYear);
                        }
                    }else{
                          jk.put("rotationSpeedOfYear"    , 0);
                    }
                    kukakuList.add(jk);
                  }
                  jf.put("kukakuList" , kukakuList);
                  fieldList.add(jf);
              }
            }
            jd.put("fieldList" , fieldList);
        }

        pListJson.add(jd);
      }
    }
    else {
      result  = GET_ERROR;
    }

    return result;
  }

  /**
   * 対象圃場が他の圃場グループに所属しているかチェックする
   * @param pFarmId
   * @param pFieldGroupId
   * @param pFieldId
   * @return
   */
  public static boolean CheckFieldExist(double pFieldGroupId, double pFieldId) {

    /** 戻り値 */
    boolean result  = false;

    FieldGroup fieldGroup = FieldGroupList.getFieldGroup(pFieldId);

    if(fieldGroup != null){
    	if(fieldGroup.fieldGroupId != pFieldGroupId){
    		/* 自圃場グループはチェック対象外 */
    		result = true;
    	}
    }

    return result;
  }

  /**
   * 圃場IDより区画一覧を取得し、JSONに格納する（自所属区画＋未所属）
   * @param pListJson
   * @return
   */
  public static int getCompartmentOfFieldSel(double pFarmId, double pFieldId, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    List<Compartment> kukakus = Compartment.getCompartmentOfField(pFarmId, pFieldId);

    //自所属区画
    for (Compartment kukakuData : kukakus) {
        if (kukakuData.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , kukakuData.kukakuId);
        jd.put("name" , kukakuData.kukakuName);
        jd.put("flag" , 1);
        pListJson.put(String.valueOf(kukakuData.kukakuId), jd);
    }

    List<Compartment> kukakuNs = Compartment.getCompartmentOfFarmNot(pFarmId);

    //未所属区画
    for (Compartment kukakuData : kukakuNs) {
        if (kukakuData.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , kukakuData.kukakuId);
        jd.put("name" , kukakuData.kukakuName);
        jd.put("flag" , 0);
        pListJson.put(String.valueOf(kukakuData.kukakuId), jd);
    }

    return result;
  }

  /**
   * 圃場情報を生成する
   * @param pInput
   * @return
   */
  public static Field makeField(JsonNode pInput) {

    SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.FIELDID);       //最新シーケンス値の取得

    Field field = new Field();
    field.fieldId    	= sequence.sequenceValue;
    field.fieldName  	= pInput.get("fieldName").asText();
    field.farmId		   	= Double.parseDouble(pInput.get("farmId").asText());
    field.landlordId	   	= Double.parseDouble(pInput.get("landlordId").asText());
    field.postNo        	= pInput.get("postNo").asText();
    field.prefectures   	= pInput.get("prefectures").asText();
    field.address   	   	= pInput.get("address").asText();
    field.geography   	    = Long.parseLong(pInput.get("geography").asText());
    double area = Double.parseDouble(pInput.get("area").asText());
    Farm farm = Farm.getFarm(field.farmId);
    FarmStatus farmStatus = farm.getFarmStatus();
    if (farmStatus.areaUnit == 1) { //平方メートルの場合
      area = area / 100;
    }
    else if (farmStatus.areaUnit == 2) { //坪の場合
      area = area / 30.25;
    }
    field.area        = area;
    field.soilQuality      	= Long.parseLong(pInput.get("soilQuality").asText());
    try {
    	field.contractDate	=	new java.sql.Date(sdf.parse(pInput.get("contractDate").asText().replace("/", "")).getTime());
    } catch (ParseException e) {
    	field.contractDate	=	null;
    }
    try {
    	field.contractEndDate	=	new java.sql.Date(sdf.parse(pInput.get("contractEndDate").asText().replace("/", "")).getTime());
    } catch (ParseException e) {
    	field.contractEndDate	=	null;
    }
    field.contractType   	= Long.parseLong(pInput.get("contractType").asText());
    field.rent				= Double.parseDouble(pInput.get("rent").asText());
    field.save();

    //区画作成
    MakeAutoCompartment(field.farmId, field.fieldId);

    return field;

  }

/**
 * 圃場情報を更新する
 * @param pInput
 * @return
 */
  public static Field updateField(JsonNode pInput) {

    SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");

	Field field = Field.getFieldInfo(Double.parseDouble(pInput.get("fieldId").asText()));
    field.fieldName  	= pInput.get("fieldName").asText();
    field.farmId		   	= Double.parseDouble(pInput.get("farmId").asText());
    field.landlordId	   	= Double.parseDouble(pInput.get("landlordId").asText());
    field.postNo        	= pInput.get("postNo").asText();
    field.prefectures   	= pInput.get("prefectures").asText();
    field.address   	   	= pInput.get("address").asText();
    field.geography   	    = Long.parseLong(pInput.get("geography").asText());
    double area = Double.parseDouble(pInput.get("area").asText());
    Farm farm = Farm.getFarm(field.farmId);
    FarmStatus farmStatus = farm.getFarmStatus();
    if (farmStatus.areaUnit == 1) { //平方メートルの場合
      area = area / 100;
    }
    else if (farmStatus.areaUnit == 2) { //坪の場合
      area = area / 30.25;
    }
    field.area        = area;
    field.soilQuality      	= Long.parseLong(pInput.get("soilQuality").asText());
    try {
    	field.contractDate	=	new java.sql.Date(sdf.parse(pInput.get("contractDate").asText().replace("/", "")).getTime());
    } catch (ParseException e) {
    	field.contractDate	=	null;
    }
    try {
    	field.contractEndDate	=	new java.sql.Date(sdf.parse(pInput.get("contractEndDate").asText().replace("/", "")).getTime());
    } catch (ParseException e) {
    	field.contractEndDate	=	null;
    }
    field.contractType   	= Long.parseLong(pInput.get("contractType").asText());
    field.rent				= Double.parseDouble(pInput.get("rent").asText());

    field.update();

    return field;

  }

  /**
   * 圃場情報を削除します
   * @param pCropGroupId
   * @return
   */
  public static int deleteField(double pFarmId, double pFieldId) {

    /** 戻り値 */
    int result  = UPDATE_SUCCESS;

    //----- 圃場を削除 ----
    //Ebean.createSqlUpdate("DELETE FROM field WHERE field_id = " + pFieldId).execute();
    Field field = Field.getFieldInfo(pFieldId);
    field.deleteFlag = 1;
    field.update();

    //----- 圃場グループ明細を削除 ----
    //Ebean.createSqlUpdate("DELETE FROM field_group_list WHERE field_id = " + pFieldId).execute();
    List<FieldGroupList> fgls = FieldGroupList.getFieldGroupListForFieldId(pFieldId);
    for(FieldGroupList fgl:fgls){
  	  fgl.deleteFlag = 1;
  	  fgl.update();
    }

    //----- 区画情報を削除 ----
    //List<Compartment> kukakus = Compartment.getCompartmentOfField(pFarmId, pFieldId);
    //for(Compartment kukakuData:kukakus){
  	//  kukakuData.fieldId = 0;
  	//  kukakuData.update();
    //}
    List<Compartment> kukakus = Compartment.getCompartmentOfField(pFarmId, pFieldId);
    for(Compartment kukakuData:kukakus){
  	  deleteCompartment(kukakuData.kukakuId);
    }

    return result;

  }

  /**
   * 圃場グループ情報を生成する
   * @param pGroupName
   * @param pFarmId
   * @return
   */
  public static FieldGroup makeFieldGroup(String pGroupName, String pGroupColor, double pFarmId, int fieldGroupSequenceId, short workPlanAutoCreate) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.FIELDGROUPID);       //最新シーケンス値の取得

    FieldGroup fg = new FieldGroup();
    fg.fieldGroupId    = sequence.sequenceValue;
    fg.fieldGroupName  = pGroupName;
    fg.fieldGroupColor = pGroupColor;
    fg.farmId          = pFarmId;
    fg.sequenceId      = fieldGroupSequenceId;
    fg.workPlanAutoCreate = workPlanAutoCreate;
    fg.save();

    return fg;

  }

  /**
   * 圃場グループ情報を更新する
   * @param pGroupId
   * @param pGroupName
   * @param pFarmId
   * @return
   */
  public static FieldGroup updateFieldGroup(double pGroupId, String pGroupName, String pGroupColor, double pFarmId, int fieldGroupSequenceId, short workPlanAutoCreate) {

    FieldGroup fg = FieldGroup.getFieldGroup(pGroupId);
    fg.fieldGroupName  = pGroupName;
    fg.fieldGroupColor = pGroupColor;
    fg.sequenceId      = fieldGroupSequenceId;
    fg.workPlanAutoCreate = workPlanAutoCreate;
    fg.update();

    return fg;

  }

  /**
   * 圃場グループ明細を更新します
   * @param pCropGroupId
   * @param pListJson
   * @return
   */
  public static int updateFieldGroupList(double pFieldGroupId, String[] pFieldIds) {

    /** 戻り値 */
    int result  = UPDATE_SUCCESS;
    long idx = 0;

    //----- 圃場グループ明細を削除 ----
    Ebean.createSqlUpdate("DELETE FROM field_group_list WHERE field_group_id = " + pFieldGroupId).execute();

    if (pFieldIds.length > 0) {

        for (int index = 0; index < pFieldIds.length; index++) {

        	idx = idx + 1;
            double  id        = Double.parseDouble(pFieldIds[index]);
            FieldGroupList fgl = new FieldGroupList();
            fgl.fieldGroupId   = pFieldGroupId;
            fgl.fieldId        = id;
            fgl.sequenceId     = idx;
            fgl.save();

        }
    }
    return result;
  }

    /**
     * 圃場グループを削除します
     * @param pCropGroupId
     * @return
     */
    public static int deleteFieldGroup(double pFieldGroupId) {

      /** 戻り値 */
      int result  = UPDATE_SUCCESS;

      //----- 圃場グループを削除 ----
      //Ebean.createSqlUpdate("DELETE FROM field_group WHERE field_group_id = " + pFieldGroupId).execute();
      FieldGroup fg = FieldGroup.getFieldGroup(pFieldGroupId);
      fg.deleteFlag = 1;
      fg.update();

      //----- 圃場グループ明細を削除 ----
      //Ebean.createSqlUpdate("DELETE FROM field_group_list WHERE field_group_id = " + pFieldGroupId).execute();
      List<FieldGroupList> fgls = FieldGroupList.getFieldGroupListForGroupId(pFieldGroupId);
      for(FieldGroupList fgl:fgls){
        fgl.deleteFlag = 1;
  	    fgl.update();
      }

      return result;

    }

    /**
     * 区画を自動作成します
     * @param pCropGroupId
     * @return
     */
	public static void MakeAutoCompartment(double farmId, double fieldId) {

		String[] data;
		java.sql.Date defaultDate 			= DateU.GetNullDate();

        //----- 圃場を取得する -----
		Field field = Field.getFieldInfo(fieldId);

        //----- 区画を生成する -----
        Compartment compartment   = new Compartment();
        Sequence sequenceKukaku   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.KUKAKUID);    //最新シーケンス値の取得
        compartment.kukakuId      = sequenceKukaku.sequenceValue;
        compartment.kukakuName    = field.fieldName;
        compartment.farmId        = farmId;
        compartment.fieldId       = field.fieldId;
        compartment.area       	  = field.area;
        compartment.soilQuality   = field.soilQuality;
        compartment.purchaseDate  = defaultDate;
        compartment.save();

        //区画関連情報作成
        SystemBatch.makeCompartmentALL(compartment.kukakuId, compartment.kukakuId);
	}

    /**
     * 区画情報を生成する
     * @param pInput
     * @return
     */
    public static Compartment makeCompartment(JsonNode pInput) {

	  java.sql.Date defaultDate	= DateU.GetNullDate();
      SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");
      Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.KUKAKUID);       //最新シーケンス値の取得

      Compartment kukaku = new Compartment();
      kukaku.kukakuId    	= sequence.sequenceValue;
      kukaku.kukakuName  	= pInput.get("kukakuName").asText();
      kukaku.farmId		   	= Double.parseDouble(pInput.get("farmId").asText());
      kukaku.fieldId	   	= Double.parseDouble(pInput.get("fieldId").asText());
      double area = Double.parseDouble(pInput.get("area").asText());
      Farm farm = Farm.getFarm(kukaku.farmId);
      FarmStatus farmStatus = farm.getFarmStatus();
      if (farmStatus.areaUnit == 1) { //平方メートルの場合
        area = area / 100;
      }
      else if (farmStatus.areaUnit == 2) { //坪の場合
        area = area / 30.25;
      }
      kukaku.area        = area;
      kukaku.soilQuality    = Long.parseLong(pInput.get("soilQuality").asText());
      kukaku.frontage		= Double.parseDouble(pInput.get("frontage").asText());
      kukaku.depth			= Double.parseDouble(pInput.get("depth").asText());
      kukaku.kansuiMethod	= Long.parseLong(pInput.get("kansuiMethod").asText());
      kukaku.kansuiRyo		= Double.parseDouble(pInput.get("kansuiRyo").asText());
      kukaku.kansuiTime		= Long.parseLong(pInput.get("kansuiTime").asText());
      kukaku.kansuiOrder	= Long.parseLong(pInput.get("kansuiOrder").asText());
      kukaku.kukakuKind		= Long.parseLong(pInput.get("kukakuKind").asText());
      kukaku.houseName  	= pInput.get("houseName").asText();
      kukaku.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
      try {
    	  kukaku.purchaseDate	=	new java.sql.Date(sdf.parse(pInput.get("purchaseDate").asText().replace("/", "")).getTime());	//購入日
      } catch (ParseException e) {
    	  kukaku.purchaseDate	=	defaultDate;
      }
      kukaku.serviceLife	= Long.parseLong(pInput.get("serviceLife").asText());
      kukaku.sequenceId   = pInput.get("sequenceId").asInt();
      kukaku.lat          = pInput.get("lat").asDouble();
      kukaku.lng          = pInput.get("lng").asDouble();

      kukaku.save();

      //区画関連情報作成
      SystemBatch.makeCompartmentALL(kukaku.kukakuId, kukaku.kukakuId);

      return kukaku;

    }

    /**
     * 区画情報を更新する
     * @param pInput
     * @return
     */
    public static Compartment updateCompartment(JsonNode pInput) {
  	  java.sql.Date defaultDate	= DateU.GetNullDate();
      SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");

      Compartment kukaku 	= Compartment.getCompartmentInfo(Double.parseDouble(pInput.get("kukakuId").asText()));
      kukaku.kukakuName  	= pInput.get("kukakuName").asText();
      kukaku.farmId		   	= Double.parseDouble(pInput.get("farmId").asText());
      kukaku.fieldId	   	= Double.parseDouble(pInput.get("fieldId").asText());
      double area = Double.parseDouble(pInput.get("area").asText());
      Farm farm = Farm.getFarm(kukaku.farmId);
      FarmStatus farmStatus = farm.getFarmStatus();
      if (farmStatus.areaUnit == 1) { //平方メートルの場合
        area = area / 100;
      }
      else if (farmStatus.areaUnit == 2) { //坪の場合
        area = area / 30.25;
      }
      kukaku.area        = area;
      kukaku.soilQuality    = Long.parseLong(pInput.get("soilQuality").asText());
      kukaku.frontage		= Double.parseDouble(pInput.get("frontage").asText());
      kukaku.depth			= Double.parseDouble(pInput.get("depth").asText());
      kukaku.kansuiMethod	= Long.parseLong(pInput.get("kansuiMethod").asText());
      kukaku.kansuiRyo		= Double.parseDouble(pInput.get("kansuiRyo").asText());
      kukaku.kansuiTime		= Long.parseLong(pInput.get("kansuiTime").asText());
      kukaku.kansuiOrder	= Long.parseLong(pInput.get("kansuiOrder").asText());
      kukaku.kukakuKind		= Long.parseLong(pInput.get("kukakuKind").asText());
      kukaku.houseName  	= pInput.get("houseName").asText();
      kukaku.kingaku		= Double.parseDouble(pInput.get("kingaku").asText());
      try {
    	  kukaku.purchaseDate	=	new java.sql.Date(sdf.parse(pInput.get("purchaseDate").asText().replace("/", "")).getTime());	//購入日
      } catch (ParseException e) {
    	  kukaku.purchaseDate	=	defaultDate;
      }
      kukaku.serviceLife	= Long.parseLong(pInput.get("serviceLife").asText());
      kukaku.sequenceId   = pInput.get("sequenceId").asInt();
      kukaku.lat          = pInput.get("lat").asDouble();
      kukaku.lng          = pInput.get("lng").asDouble();
      kukaku.update();

      return kukaku;

    }

    /**
     * 区画情報を更新します（圃場のみ）
     * @param pCropGroupId
     * @param pListJson
     * @return
     */
    public static int updateCropCompartmentField(double pFarmId, double pFieldId, String[] pKukakuIds) {

      /** 戻り値 */
      int result  = UPDATE_SUCCESS;
      boolean bexist = false;

      //現状の圃場所属区画情報を取得
      List<Compartment> kukakusNow = Compartment.getCompartmentOfField(pFarmId, pFieldId);
      List<Compartment> kukakus = Compartment.getCompartmentOfField(pFarmId, pFieldId);

      //一旦区画情報の圃場IDを削除
      for(Compartment kukakuData:kukakus){
    	  kukakuData.fieldId = 0;
    	  kukakuData.update();
      }

      //区画情報の圃場IDを更新
      if (pKukakuIds.length > 0) {

          for (int index = 0; index < pKukakuIds.length; index++) {

              double  id        = Double.parseDouble(pKukakuIds[index]);
              Compartment cp = Compartment.getCompartmentInfo(id);
              cp.fieldId   = pFieldId;
              cp.update();

              //新規区画の場合、区画関連情報を作成
//              bexist = false;
//              for(Compartment kukakuData:kukakusNow){
//            	  if(kukakuData.kukakuId == id){
//            		  bexist = true;
//            		  break;
//            	  }
//              }
//              if(!bexist){
//                  //区画関連情報作成
//                  SystemBatch.makeCompartmentALL(id, id);
//              }
          }
      }

      return result;

    }

    /**
     * 区画情報を削除します
     * @param pCropGroupId
     * @return
     */
    public static int deleteCompartment(double pKukakuId) {

      /** 戻り値 */
      int result  = UPDATE_SUCCESS;

      //----- 区画を削除 ----
      //Ebean.createSqlUpdate("DELETE FROM compartment WHERE kukaku_id = " + pKukakuId).execute();
      Compartment kukaku = Compartment.getCompartmentInfo(pKukakuId);
      kukaku.deleteFlag = 1;
      kukaku.update();

      //----- 区画状況を削除 ----
      //Ebean.createSqlUpdate("DELETE FROM compartment_status WHERE kukaku_id = " + pKukakuId).execute();
      List<CompartmentStatus> stss = CompartmentStatus.getStatusOfCompartment(pKukakuId);
      if (stss.size() > 0) {
          for (CompartmentStatus sts : stss) {
            sts.deleteFlag = 1;
            sts.update();

            //----- 区画ワークチェイン状況を削除 ----
            CompartmentWorkChainStatus cws = sts.getWorkChainStatus();
            cws.deleteFlag = 1;
            cws.update();
          }
      }

      //----- 区画ワークチェイン状況を削除 ----
      //Ebean.createSqlUpdate("DELETE FROM compartment_work_chain_status WHERE kukaku_id = " + pKukakuId).execute();

      return result;

    }
}
