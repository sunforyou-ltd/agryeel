package controllers;

import java.util.List;

import models.Account;
import models.Attachment;
import models.AttachmentOfFarm;
import models.Belto;
import models.BeltoOfFarm;
import models.Compartment;
import models.Crop;
import models.CropGroup;
import models.Farm;
import models.Field;
import models.FieldGroup;
import models.Hinsyu;
import models.HinsyuOfFarm;
import models.Kiki;
import models.KikiOfFarm;
import models.Landlord;
import models.Nisugata;
import models.NisugataOfFarm;
import models.Nouhi;
import models.Shitu;
import models.ShituOfFarm;
import models.Sizai;
import models.SizaiOfFarm;
import models.Size;
import models.SizeOfFarm;
import models.Soil;
import models.Youki;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;

import consts.AgryeelConst;

public class MasterMaintenance extends Controller {

    @Security.Authenticated(SessionCheckComponent.class)
    public static Result move(double farmId, int gmnId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納
        session(AgryeelConst.SessionKey.MSTGMNID
                , String.valueOf(gmnId));										//マスタメンテナンス画面IDをセッションに格納

        return ok(views.html.masterMaintenance.render(""));
    }

    /**
     * 【AGRYEEL】マスタメンテナンス初期表示データ取得
     * @return
     */
    public static Result masterMntInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));

        /* マスタメンテナンス画面IDの取得 */
        int gmnId = Integer.parseInt(session(AgryeelConst.SessionKey.MSTGMNID));

        /*-------------------------------------------------------------------*/
        /* 各一覧表情報の取得                                                */
        /*-------------------------------------------------------------------*/
        /*----- メンテナンス項目 -----*/
        switch (gmnId) {
	        case AgryeelConst.MasterMntGmn.ACCOUNT: 		//アカウントの場合
	        	resultJson = accountGet(gmnId, farmId);
	            break;
	        case AgryeelConst.MasterMntGmn.LANDLORD: 		//地主の場合
	        	resultJson = landlordGet(gmnId, farmId);
	            break;
            case AgryeelConst.MasterMntGmn.FIELDGROUP: 		//圃場グループの場合
            	resultJson = fieldGroupGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.FIELD: 			//圃場の場合
            	resultJson = fieldGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.COMPARTMENT: 	//区画の場合
            	resultJson = fieldCompartmentGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.CROPGROUP: 		//生産物グループの場合
            	resultJson = cropGroupGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.CROP: 			//生産物の場合
            	resultJson = cropGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.NOUHI: 			//農肥の場合
            	resultJson = nouhiGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.HINSYU: 			//種の場合
            	resultJson = hinsyuGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.BELTO: 			//ベルトの場合
            	resultJson = beltoGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.KIKI: 			//機器の場合
            	resultJson = kikiGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.ATTACHMENT: 		//アタッチメントの場合
            	resultJson = attachmentGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.NISUGATA: 		//荷姿の場合
            	resultJson = nisugataGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.SHITU: 			//質の場合
            	resultJson = shituGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.SIZE: 			//サイズの場合
            	resultJson = sizeGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.SIZAI: 			//資材の場合
            	resultJson = sizaiGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.FARM: 			//生産者の場合
            	resultJson = farmGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.YOUKI: 			//容器の場合
            	resultJson = youkiGet(gmnId, farmId);
                break;
            case AgryeelConst.MasterMntGmn.SOIL: 			//土の場合
            	resultJson = soilGet(gmnId, farmId);
                break;
            default:
                break;
        }

        /*-------------------------------------------------------------------*/

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】アカウント初期表示データ取得
     * @return
     */
    public static ObjectNode accountGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.ACCOUNT);						//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* アカウント情報を取得する */
        UserComprtnent accountComprtnent = new UserComprtnent();
        accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));
        List<Account> accountList = Account.getAccountOfFarm(farmId);
        for (Account accountData : accountList) {													//アカウント情報をJSONデータに格納する

            if (accountComprtnent.accountData.managerRole == 0 && !accountComprtnent.accountData.accountId.equals(accountData.accountId)) { /* 担当者の場合 */
              continue;
            }

            if (accountData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode accountJson	= Json.newObject();
            accountJson.put("masterId"			, accountData.accountId);							//アカウントID
            accountJson.put("masterName"		, accountData.acountName);							//アカウント名

            listJson.put(String.valueOf(accountData.accountId), accountJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】地主初期表示データ取得
     * @return
     */
    public static ObjectNode landlordGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.LANDLORD);						//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 地主情報を取得する */
        List<Landlord> landlordList = Landlord.getLandlordOfFarm(farmId);
        for (Landlord landlordData : landlordList) {												//地主情報をJSONデータに格納する

            if (landlordData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode landlordJson	= Json.newObject();
            landlordJson.put("masterId"			, landlordData.landlordId);							//地主ID
            landlordJson.put("masterName"			, landlordData.landlordName);					//地主名

            listJson.put(String.valueOf(landlordData.landlordId), landlordJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】圃場グループ初期表示データ取得
     * @return
     */
    public static ObjectNode fieldGroupGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.FIELDGROUP);					//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 圃場グループ情報を取得する */
        List<FieldGroup> groupList = FieldGroup.getFieldGroupOfFarm(farmId);
        for (FieldGroup fieldGroupData : groupList) {												//圃場グループ情報をJSONデータに格納する
            if (fieldGroupData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode groupJson	= Json.newObject();
            groupJson.put("masterId"			, fieldGroupData.fieldGroupId);						//圃場グループID
            groupJson.put("masterName"			, fieldGroupData.fieldGroupName);					//圃場グループ名

            listJson.put(String.valueOf(fieldGroupData.fieldGroupId), groupJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】圃場初期表示データ取得
     * @return
     */
    public static ObjectNode fieldGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();
        UserComprtnent accountComprtnent = new UserComprtnent();
        accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.FIELD);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID
        resultJson.put("searchInfo", "getFieldGroup");												//検索情報

        /* 圃場情報を取得する */
        List<Field> fields = Field.getFieldOfFarm(farmId);
        for (Field field : fields) {
          if (field.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          ObjectNode fieldpJson  = Json.newObject();
          fieldpJson.put("masterId"            , field.fieldId);          //圃場ID
          fieldpJson.put("masterName"          , field.fieldName);        //圃場名

          listJson.put(String.valueOf(field.fieldId), fieldpJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】区画初期表示データ取得
     * @return
     */
    public static ObjectNode fieldCompartmentGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();
        UserComprtnent accountComprtnent = new UserComprtnent();
        accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.COMPARTMENT);					//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID
        resultJson.put("searchInfo", "getFieldGroup");												//検索情報

        /* 区画情報を取得する */
    	List<Compartment> kukakus = Compartment.getCompartmentOfFarm(farmId);
        for (Compartment kukaku : kukakus) {
            if (kukaku.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode pJson  = Json.newObject();
            pJson.put("masterId"            , kukaku.kukakuId);          							//区画ID
        	pJson.put("masterName"          , kukaku.kukakuName);        							//区画名

            listJson.put(String.valueOf(kukaku.kukakuId), pJson);
        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】生産物グループ初期表示データ取得
     * @return
     */
    public static ObjectNode cropGroupGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.CROPGROUP);						//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 生産物グループ情報を取得する */
        List<CropGroup> groupList = CropGroup.find.where().eq("farm_id", farmId).orderBy("crop_group_id").findList();
        for (CropGroup cropGroupData : groupList) {													//生産物グループ情報をJSONデータに格納する
            if (cropGroupData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode groupJson	= Json.newObject();
            groupJson.put("masterId"			, cropGroupData.cropGroupId);						//生産物グループID
            groupJson.put("masterName"			, cropGroupData.cropGroupName);						//生産物グループ名

            listJson.put(String.valueOf(cropGroupData.cropGroupId), groupJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】生産物初期表示データ取得
     * @return
     */
    public static ObjectNode cropGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.CROP);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 生産物情報を取得する */
        List<Crop> cropList = Crop.find.where().orderBy("crop_id").findList();
        for (Crop croppData : cropList) {															//生産物情報をJSONデータに格納する
            if (croppData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode cropJson	= Json.newObject();
            cropJson.put("masterId"			, croppData.cropId);									//生産物ID
            cropJson.put("masterName"			, croppData.cropName);								//生産物名

            listJson.put(String.valueOf(croppData.cropId), cropJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】農肥初期表示データ取得
     * @return
     */
    public static ObjectNode nouhiGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.NOUHI);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 農肥情報を取得する */
        List<Nouhi> nouhiList = Nouhi.getNouhiOfFarm(farmId);
        for (Nouhi nouhiData : nouhiList) {															//農肥情報をJSONデータに格納する
            if (nouhiData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode nouhiJson	= Json.newObject();
            nouhiJson.put("masterId"		, nouhiData.nouhiId);									//農肥ID
            nouhiJson.put("masterName"		, nouhiData.nouhiName);									//農肥名

            listJson.put(String.valueOf(nouhiData.nouhiId), nouhiJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】種初期表示データ取得
     * @return
     */
    public static ObjectNode hinsyuGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.HINSYU);						//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 品種情報を取得する */
        List<HinsyuOfFarm> hinsyuList = HinsyuOfFarm.getHinsyuOfFarm(farmId);
        for (HinsyuOfFarm hinsyuData : hinsyuList) {												//品種情報をJSONデータに格納する
            if (hinsyuData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode hinsyuJson	= Json.newObject();
            hinsyuJson.put("masterId"			, hinsyuData.hinsyuId);								//品種ID
            hinsyuJson.put("masterName"		, Hinsyu.getHinsyuName(hinsyuData.hinsyuId));			//品種名

            listJson.put(String.valueOf(hinsyuData.hinsyuId), hinsyuJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }


    /**
     * 【AGRYEEL】ベルト初期表示データ取得
     * @return
     */
    public static ObjectNode beltoGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.BELTO);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* ベルト情報を取得する */
        List<BeltoOfFarm> beltoList = BeltoOfFarm.getBeltoOfFarm(farmId);
        for (BeltoOfFarm beltoData : beltoList) {													//ベルト情報をJSONデータに格納する
            if (beltoData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode beltoJson	= Json.newObject();
            beltoJson.put("masterId"			, beltoData.beltoId);								//ベルトID
            beltoJson.put("masterName"		, Belto.getBeltoName(beltoData.beltoId));				//ベルト名

            listJson.put(String.valueOf(beltoData.beltoId), beltoJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】機器初期表示データ取得
     * @return
     */
    public static ObjectNode kikiGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.KIKI);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 機器情報を取得する */
        List<KikiOfFarm> kikiList = KikiOfFarm.getKikiOfFarm(farmId);
        for (KikiOfFarm kikiData : kikiList) {														//機器情報をJSONデータに格納する
            if (kikiData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode kikiJson	= Json.newObject();
            Kiki kiki = Kiki.getKikiInfo(kikiData.kikiId);
            kikiJson.put("masterId"			, kikiData.kikiId);										//機器ID
            kikiJson.put("masterName"		, kiki.kikiName);										//機器名
            listJson.put(String.valueOf(kikiData.kikiId), kikiJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】アタッチメント初期表示データ取得
     * @return
     */
    public static ObjectNode attachmentGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.ATTACHMENT);					//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 生産物情報を取得する */
        List<AttachmentOfFarm> attachList = AttachmentOfFarm.getAttachmentOfFarm(farmId);
        for (AttachmentOfFarm attachData : attachList) {													//アタッチメント情報をJSONデータに格納する
            if (attachData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode attachJson	= Json.newObject();
            Attachment attach = Attachment.getAttachmentInfo(attachData.attachmentId);
            attachJson.put("masterId"			, attachData.attachmentId);							//アタッチメントID
            attachJson.put("masterName"		, attach.attachementName);								//アタッチメント名

            listJson.put(String.valueOf(attachData.attachmentId), attachJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】荷姿初期表示データ取得
     * @return
     */
    public static ObjectNode nisugataGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.NISUGATA);						//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 荷姿情報を取得する */
        List<NisugataOfFarm> nisugataList = NisugataOfFarm.getNisugataOfFarm(farmId);
        for (NisugataOfFarm nisugataData : nisugataList) {											//荷姿情報をJSONデータに格納する
            if (nisugataData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode nisugataJson	= Json.newObject();
            nisugataJson.put("masterId"			, nisugataData.nisugataId);							//荷姿ID
            nisugataJson.put("masterName"		, Nisugata.getNisugataName(nisugataData.nisugataId));	//荷姿名

            listJson.put(String.valueOf(nisugataData.nisugataId), nisugataJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】質初期表示データ取得
     * @return
     */
    public static ObjectNode shituGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.SHITU);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* ベルト情報を取得する */
        List<ShituOfFarm> shituList = ShituOfFarm.getShituOfFarm(farmId);
        for (ShituOfFarm shituData : shituList) {													//質情報をJSONデータに格納する
            if (shituData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode shituJson	= Json.newObject();
            shituJson.put("masterId"		, shituData.shituId);									//質ID
            shituJson.put("masterName"		, Shitu.getShituName(shituData.shituId));				//質名

            listJson.put(String.valueOf(shituData.shituId), shituJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】サイズ初期表示データ取得
     * @return
     */
    public static ObjectNode sizeGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.SIZE);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* ベルト情報を取得する */
        List<SizeOfFarm> sizeList = SizeOfFarm.getSizeOfFarm(farmId);
        for (SizeOfFarm sizeData : sizeList) {														//サイズ情報をJSONデータに格納する
            if (sizeData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode sizeJson	= Json.newObject();
            sizeJson.put("masterId"			, sizeData.sizeId);										//サイズID
            sizeJson.put("masterName"		, Size.getSizeName(sizeData.sizeId));					//サイズ名

            listJson.put(String.valueOf(sizeData.sizeId), sizeJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】資材初期表示データ取得
     * @return
     */
    public static ObjectNode sizaiGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.SIZAI);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* ベルト情報を取得する */
        List<SizaiOfFarm> sizaiList = SizaiOfFarm.getSizaiOfFarm(farmId);
        for (SizaiOfFarm sizaiData : sizaiList) {													//資材情報をJSONデータに格納する
            if (sizaiData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode sizaiJson	= Json.newObject();
            sizaiJson.put("masterId"		, sizaiData.sizaiId);									//資材ID
            sizaiJson.put("masterName"		, Sizai.getSizaiName(sizaiData.sizaiId));				//資材名

            listJson.put(String.valueOf(sizaiData.sizaiId), sizaiJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】生産者初期表示データ取得
     * @return
     */
    public static ObjectNode farmGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.FARM);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 生産者情報を取得する */
        Farm farm = Farm.getFarm(farmId);

        ObjectNode farmJson	= Json.newObject();
        farmJson.put("masterId"			, farm.farmId);												//生産者ID
        farmJson.put("masterName"		, farm.farmName);											//生産者名

        listJson.put(String.valueOf(farm.farmId), farmJson);

        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】容器初期表示データ取得
     * @return
     */
    public static ObjectNode youkiGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.YOUKI);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 容器情報を取得する */
        List<Youki> youkiList = Youki.getYoukiOfFarm(farmId);
        for (Youki youkiData : youkiList) {															//容器情報をJSONデータに格納する
            if (youkiData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode youkiJson	= Json.newObject();
            youkiJson.put("masterId"		, youkiData.youkiId);									//容器ID
            youkiJson.put("masterName"		, youkiData.youkiName);									//容器名

            listJson.put(String.valueOf(youkiData.youkiId), youkiJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }

    /**
     * 【AGRYEEL】土初期表示データ取得
     * @return
     */
    public static ObjectNode soilGet(int gmnId, Double farmId) {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        resultJson.put("gmnId", String.valueOf(gmnId));												//画面ID
        resultJson.put("gmnName", AgryeelConst.MasterMntGmn.GmnName.SOIL);							//画面名称
        resultJson.put("farmId", String.valueOf(farmId));											//生産者ID

        /* 土情報を取得する */
        List<Soil> soilList = Soil.getSoilOfFarm(farmId);
        for (Soil soilData : soilList) {															//土情報をJSONデータに格納する
            if (soilData.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode soilJson	= Json.newObject();
            soilJson.put("masterId"			, soilData.soilId);										//土ID
            soilJson.put("masterName"		, soilData.soilName);									//土名

            listJson.put(String.valueOf(soilData.soilId), soilJson);

        }
        resultJson.put(AgryeelConst.MasterMntGmn.MASTERLIST, listJson);

        return resultJson;
    }
}
