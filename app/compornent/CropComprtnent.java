package compornent;

import java.util.List;

import models.Crop;
import models.CropGroup;
import models.CropGroupList;
import models.Hinsyu;
import models.HinsyuOfFarm;
import models.Sequence;
import play.libs.Json;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】生産物コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CropComprtnent implements AgryellInterface{

  List<Crop> crops;

	/**
	 * コンストラクタ
	 */
	public CropComprtnent() {

	  crops = Crop.find.orderBy("crop_id").findList();

	}

	/**
	 * 生産物一覧を取得し、JSONに格納する
	 * @param pListJson
	 * @return
	 */
  public int getCropJson(ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    if (crops.size() > 0) { //生産物が存在する場合

        for (Crop crop : crops) {
          ObjectNode jd = Json.newObject();

          jd.put("id"   , crop.cropId);
          jd.put("name" , crop.cropName);

          pListJson.put(String.valueOf(crop.cropId), jd);

        }

    }
    else {
      result  = GET_ERROR;
    }

    return result;

  }
  /**
   * 生産者IDより生産物一覧を取得し、JSONに格納する
   * @param pListJson
   * @return
   */
  public static int getCropOfFarmJson(double pFarmId, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    CropGroup cg = CropGroup.find.where().eq("farm_id", pFarmId).orderBy("crop_group_id").findUnique();

    if (cg != null) { //生産物が存在する場合

        List<CropGroupList> cgl = CropGroupList.find.where().eq("crop_group_id", cg.cropGroupId).order("crop_id").findList();

        for (CropGroupList cropGroupList : cgl) {
          if (cropGroupList.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          Crop crop = Crop.find.where().eq("crop_id", cropGroupList.cropId).findUnique();

          if (crop != null) {
            if (crop.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode jd = Json.newObject();

            jd.put("id"   , crop.cropId);
            jd.put("name" , crop.cropName);

            pListJson.put(String.valueOf(crop.cropId), jd);
          }
        }

    }
    else {
      result  = GET_ERROR;
    }

    return result;

  }
  /**
   * 生産者IDより生産物一覧を取得し、JSONに格納する(選択なし追加版)
   * @param pListJson
   * @return
   */
  public static int getCropOfFarmAllJson(double pFarmId, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    getCropOfFarmJson( pFarmId, pListJson);

    ObjectNode jd = Json.newObject();

    jd.put("id"   , 0);
    jd.put("name" , "選択なし");
    jd.put("flag" , 0);

    pListJson.put(AgryeelConst.SpecialAccount.ALLACOUNT, jd);

    return result;

  }

  /**
   * 生産者ID、生産物グループIDより生産物一覧を取得し、JSONに格納する（自所属生産物）
   * @param pListJson
   * @return
   */
  public static int getCropOfFarmSelJson(double pFarmId, double pCropGroupId, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    List<Crop> aryCrop = Crop.find.where().orderBy("crop_id asc").findList();

    for (Crop cropData : aryCrop) {
        if (cropData.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , cropData.cropId);
        jd.put("name" , cropData.cropName);
        CropGroupList groupCrop = CropGroupList.getCropUnique(pCropGroupId,cropData.cropId);
        if(groupCrop != null){
           jd.put("flag" , 1);
        }else{
    	   jd.put("flag" , 0);
        }
        pListJson.put(String.valueOf(cropData.cropId), jd);
    }

    return result;
  }


  /**
   * 生産者IDより品種一覧を取得し、JSONに格納する
   * @param pListJson
   * @return
   */
  public static int getHinsyuOfFarmJson(double pFarmId, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    List<HinsyuOfFarm> hfs = HinsyuOfFarm.find.where().eq("farm_id", pFarmId).orderBy("hinsyu_id").findList();

    for (HinsyuOfFarm hf : hfs) {
      if (hf.deleteFlag == 1) { // 削除済みの場合
        continue;
      }

      Hinsyu hs = Hinsyu.find.where().eq("hinsyu_id", hf.hinsyuId).findUnique();

      if (hs != null) {
        ObjectNode jd = Json.newObject();

        jd.put("id"   , hs.hinsyuId);
        jd.put("name" , hs.hinsyuName);
        jd.put("flag" , 0);

        pListJson.put(String.valueOf(hs.hinsyuId), jd);
      }
    }

    return result;

  }
  /**
   * 生産者ID、生産物IDより品種一覧を取得し、JSONに格納する（自所属品種）
   * @param pListJson
   * @return
   */
  public static int getHinsyuOfFarmSelJson(double pFarmId, double pCropId, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    List<HinsyuOfFarm> aryHinsyu = HinsyuOfFarm.find.where().eq("farm_id", pFarmId).orderBy("hinsyu_id asc").findList();

    for (HinsyuOfFarm hinsyuData : aryHinsyu) {
        if (hinsyuData.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode jd = Json.newObject();

        jd.put("id"   , hinsyuData.hinsyuId);
        jd.put("name" , Hinsyu.getHinsyuName(hinsyuData.hinsyuId));
        Hinsyu hinsyu = Hinsyu.getHinsyuUnique(hinsyuData.hinsyuId, pCropId);
        if(hinsyu != null && pCropId != 0 ){
           jd.put("flag" , 1);
        }else{
    	   jd.put("flag" , 0);
        }
        pListJson.put(String.valueOf(hinsyuData.hinsyuId), jd);
    }

    return result;
  }

  /**
   * 生産物情報を生成する
   * @param pGroupName
   * @param pFarmId
   * @return
   */
  public static Crop makeCrop(String pName, String pColor) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.CROPID);       //最新シーケンス値の取得

    Crop crop = new Crop();
    crop.cropId    = sequence.sequenceValue;
    crop.cropName  = pName;
    crop.cropColor = pColor;
    crop.save();

    return crop;

  }

  /**
   * 生産物情報を更新する
   * @param pGroupName
   * @param pFarmId
   * @return
   */
  public static Crop updateCrop(double pCropId, String pName, String pColor) {

	Crop crop = Crop.getCropInfo(pCropId);
    crop.cropId    = pCropId;
    crop.cropName  = pName;
    crop.cropColor = pColor;
    crop.save();

    return crop;

  }

  /**
   * 生産物グループ情報を生成する
   * @param pGroupName
   * @param pFarmId
   * @return
   */
  public static CropGroup makeCropGroup(String pGroupName, double pFarmId) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.CROPGROUPID);       //最新シーケンス値の取得

    CropGroup cg = new CropGroup();
    cg.cropGroupId    = sequence.sequenceValue;
    cg.cropGroupName  = pGroupName;
    cg.farmId         = pFarmId;
    cg.save();

    return cg;

  }

  /**
   * 生産物グループ情報を更新する
   * @param pGroupId
   * @param pGroupName
   * @return
   */
  public static CropGroup updateCropGroup(double pGroupId, String pGroupName) {

	  CropGroup cg = CropGroup.getCropGroup(pGroupId);
	  cg.cropGroupName  = pGroupName;
	  cg.update();

      return cg;

  }

  /**
   * 生産物グループ明細を更新します
   * @param pCropGroupId
   * @param pListJson
   * @return
   */
  public static int updateCropGroupList(double pCropGroupId, String[] pCropIds) {

    /** 戻り値 */
    int result  = UPDATE_SUCCESS;

    //----- 生産物グループ明細を削除 ----
    Ebean.createSqlUpdate("DELETE FROM crop_group_list WHERE crop_group_id = " + pCropGroupId).execute();

    if (pCropIds.length > 0) {

        for (int index = 0; index < pCropIds.length; index++) {

            double  id        = Double.parseDouble(pCropIds[index]);
            CropGroupList cgl = new CropGroupList();
            cgl.cropGroupId   = pCropGroupId;
            cgl.cropId        = id;
            cgl.save();

        }

    }

    return result;

  }

  /**
   * 生産物IDから生産物情報を取得する
   * @param pCropId
   * @return
   */
  public static Crop getCropById(double pCropId) {
    return Crop.find.where().eq("crop_id", pCropId).findUnique();
  }

  /**
   * 生産物グループを削除します
   * @param pCropGroupId
   * @return
   */
  public static int deleteCropGroup(double pCropGroupId) {

    /** 戻り値 */
    int result  = UPDATE_SUCCESS;

    //----- 生産物グループを削除 ----
    //Ebean.createSqlUpdate("DELETE FROM crop_group WHERE crop_group_id = " + pCropGroupId).execute();
    CropGroup cg = CropGroup.getCropGroup(pCropGroupId);
    cg.deleteFlag = 1;
    cg.update();

    //----- 生産物グループ明細を削除 ----
    //Ebean.createSqlUpdate("DELETE FROM crop_group_list WHERE crop_group_id = " + pCropGroupId).execute();
    List<CropGroupList> cgl = CropGroupList.find.where().eq("crop_group_id", pCropGroupId).findList();
    for (CropGroupList cropGroup : cgl) {
      cropGroup.deleteFlag = 1;
      cropGroup.update();
    }

    return result;

  }

  /**
   * 品種情報を生成する
   * @param pName
   * @return
   */
  public static Hinsyu makeHinsyu(String pName) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.HINSYUID);       //最新シーケンス値の取得

    Hinsyu hinsyu = new Hinsyu();
    hinsyu.hinsyuId    = sequence.sequenceValue;
    hinsyu.hinsyuName  = pName;
    hinsyu.cropId  	   = 0;
    hinsyu.save();

    return hinsyu;

  }

  /**
   * 生産者別品種情報を生成する
   * @param pHinsyuId
   * @param pFarmId
   * @return
   */
  public static HinsyuOfFarm makeHinsyuOfFarm(double pHinsyuId, double pFarmId) {

    HinsyuOfFarm hinsyu = new HinsyuOfFarm();
    hinsyu.hinsyuId    = pHinsyuId;
    hinsyu.farmId  = pFarmId;
    hinsyu.save();

    return hinsyu;

  }

  /**
   * 品種情報を更新する
   * @param pGroupName
   * @param pFarmId
   * @return
   */
  public static Hinsyu updateHinsyu(double pHinsyuId, String pName) {

	  Hinsyu hinsyu = Hinsyu.getHinsyuInfo(pHinsyuId);
	  hinsyu.hinsyuId    = hinsyu.hinsyuId;
	  hinsyu.hinsyuName  = pName;
	  hinsyu.cropId 	 = hinsyu.cropId;
	  hinsyu.save();

    return hinsyu;

  }

  /**
   * 品種情報の生産物IDを更新する（生産物IDのみ）
   * @param pHinsyuId
   * @param pCropId
   * @return
   */
  public static int updateHinsyuOfCrop(double pFarmId, String[] pHinsyuIds, double pCropId) {

    /** 戻り値 */
    int result  = UPDATE_SUCCESS;

    if (pHinsyuIds.length > 0) {

        List<HinsyuOfFarm> aryHinsyu = HinsyuOfFarm.find.where().eq("farm_id", pFarmId).orderBy("hinsyu_id asc").findList();

        for (HinsyuOfFarm hinsyuData : aryHinsyu) {

            Hinsyu hinsyu = Hinsyu.getHinsyuUnique(hinsyuData.hinsyuId, pCropId);
            if(hinsyu != null){
                //----- 生産物IDを一旦削除 ----
            	hinsyu.hinsyuId   = hinsyu.hinsyuId;
            	hinsyu.hinsyuName = hinsyu.hinsyuName;
            	hinsyu.cropId = 0;
            	hinsyu.save();
            }
        }

        //----- 生産物IDを更新 ----
        for (int index = 0; index < pHinsyuIds.length; index++) {

            double  id    = Double.parseDouble(pHinsyuIds[index]);
        	Hinsyu hinsyu = Hinsyu.getHinsyuInfo(id);
        	hinsyu.hinsyuId   = id;
        	hinsyu.hinsyuName = hinsyu.hinsyuName;
        	hinsyu.cropId = pCropId;
        	hinsyu.save();
        }
    }

    return result;

  }

  /**
  * 品種を削除します
  * @param pHinsyuId
  * @return
  */
 public static int deleteHinsyu(double pHinsyuId, double pFarmId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- 品種を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM hinsyu WHERE hinsyu_id = " + pHinsyuId).execute();
   Hinsyu hinsyu = Hinsyu.getHinsyuInfo(pHinsyuId);
   hinsyu.deleteFlag = 1;
   hinsyu.update();

   //----- 生産者別品種を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM hinsyu_of_farm WHERE farm_id = " + pFarmId + "AND hinsyu_id = " + pHinsyuId).execute();
   HinsyuOfFarm hof = HinsyuOfFarm.find.where().eq("farm_id", pFarmId).eq("hinsyu_id", pHinsyuId).findUnique();
   hof.deleteFlag = 1;
   hof.update();

   return result;

 }

}
