package controllers;

import models.CropGroup;
import models.Farm;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import compornent.CropComprtnent;
import compornent.FarmComprtnent;

import consts.AgryeelConst;

public class FarmMake extends Controller {

    public static Result move() {
        return ok(views.html.farmMake.render());
    }

    /**
     * 【AGRYEEL】メールアドレス重複チェック
     * @return メールアドレス重複チェック結果JSON
     */
    public static Result farmMakeCheckMailAddress(String mailAddress) {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();

        int iReturn				= FarmComprtnent.CheckRegistAddress(mailAddress);

        resultJson.put(AgryeelConst.Result.RESULT, String.valueOf(iReturn));

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】生産者登録実行
     * @return 生産者登録結果JSON
     */
    public static Result farmMakeSubmit() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson 	= Json.newObject();

        JsonNode 	farmInput 	= request().body().asJson();
        String 		mailAddress = farmInput.get("mailAddress").asText();
        String 		farmName 	= farmInput.get("farmName").asText();
        int 		hojoGroups 	= Integer.parseInt(farmInput.get("hojoGroups").asText());
        int 		groupsType 	= Integer.parseInt(farmInput.get("groupsType").asText());
        int 		hojo 		= Integer.parseInt(farmInput.get("hojo").asText());

        Farm farm = FarmComprtnent.MakeFarm(mailAddress, farmName);
        FarmComprtnent.MakeAutoHojo(farm.farmId, hojoGroups, groupsType, hojo);

        //----- 生産物一覧を作成します -----
        CropGroup cg = CropComprtnent.makeCropGroup(farmName, farm.farmId);
        String cropString = farmInput.get("crop").asText();
        Logger.info("crops [" + cropString + "]");
        String[] crops = cropString.split(",");
        CropComprtnent.updateCropGroupList(cg.cropGroupId, crops);

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

}
