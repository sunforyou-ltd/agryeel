package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Compartment;
import models.CompartmentStatus;
import models.WorkCompartment;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SessionCheckComponent;

import consts.AgryeelConst;

public class FarmField extends Controller {

    @Security.Authenticated(SessionCheckComponent.class)
    public static Result inputTargetMove() {
        return ok(views.html.inputtarget.render(""));
    }

    public static Result inputTargetInit() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
        ObjectNode 	listJson   = Json.newObject();

        /*----- 入力圃場選択 -----*/
        /* アカウント情報から作業対象区画情報を取得する */
        List<WorkCompartment> workCompartment = WorkCompartment.find.where().eq("account_id", session(AgryeelConst.SessionKey.ACCOUNTID)).findList();

        /* 区画IDから区画状況情報を取得する */
        List<Double> aryKukakuId = new ArrayList<Double>();						//検索条件 区画ID
        for (WorkCompartment workCompartmentData : workCompartment) {			//検索条件を生成する
            aryKukakuId.add(workCompartmentData.kukakuId);
        }

        List<CompartmentStatus> compartmentStatus = CompartmentStatus.find.where().in("kukaku_id", aryKukakuId).findList();

        for (CompartmentStatus compartmentStatusData : compartmentStatus) {		//区画状況情報をJSONデータに格納する

            /* 区画IDから区画情報を取得する */
            Compartment compartmentData = Compartment.find.where().eq("kukaku_id", compartmentStatusData.kukakuId).findUnique();
            if (compartmentData == null) { continue; }															//区画情報が存在しない場合、データを作成しない

            ObjectNode compartmentStatusJson	= Json.newObject();
            compartmentStatusJson.put("kukakuId"			, compartmentStatusData.kukakuId);					//区画ID
            compartmentStatusJson.put("kukakuName"			, compartmentData.kukakuName);						//区画名
            compartmentStatusJson.put("rotationSpeedOfYear"	, compartmentStatusData.rotationSpeedOfYear);		//年内回転数
            compartmentStatusJson.put("hinsyuName"			, compartmentStatusData.hinsyuName);				//品種名
            listJson.put(Double.toString(compartmentStatusData.kukakuId), compartmentStatusJson);

        }

        resultJson.put(AgryeelConst.KukakuInfo.TARGETCOMPARTMENTSTATUS, listJson);
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }

    public static Result inputTargetSet() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode inputTarget = request().body().asJson();

        String inputResult = inputTarget.toString();

        session(AgryeelConst.SessionKey.KUKAKUID, inputResult);			//区間IDをセッションに格納
        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
