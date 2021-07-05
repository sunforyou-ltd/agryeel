package controllers;

import java.util.List;

import models.Compartment;
import models.Field;
import models.FieldGroup;
import models.FieldGroupList;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import compornent.FieldComprtnent;
import compornent.SessionCheckComponent;
import compornent.UserComprtnent;
import consts.AgryeelConst;

public class CompartmentMapping extends Controller {

    /**
     * 【AGRYEEL】区画位置情報設定画面
     * @return 区画位置情報設定画面レンダー
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result move() {
        if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
          return redirect("/move");
        }
        return ok(views.html.compartmentMapping.render());
    }

    /**
     * 画面遷移後、初期処理
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result init() {

      ObjectNode  resultJson = Json.newObject();

      UserComprtnent accountComprtnent = new UserComprtnent();
      int getAccount = accountComprtnent.GetAccountData(session(AgryeelConst.SessionKey.ACCOUNTID));

      List<FieldGroup> fgs = FieldGroup.getFieldGroupOfFarm(accountComprtnent.accountData.farmId);
      ObjectNode  listJson = Json.newObject();
      for (FieldGroup fg : fgs) {
        if (fg.deleteFlag == 1) { // 削除済みの場合
          continue;
        }

        ObjectNode  groupJson = Json.newObject();
        groupJson.put("id"          , fg.fieldGroupId);
        groupJson.put("name"        , fg.fieldGroupName + "グループ");
        groupJson.put("color"       , fg.fieldGroupColor);
        groupJson.put("sequenceId"  , fg.sequenceId);
        //----- ここで区画を取得する -----
        ObjectNode  kukakulistJson = Json.newObject();
        List<Field> fields = FieldComprtnent.getField(fg.fieldGroupId);
        for (Field field : fields) {
          if (field.deleteFlag == 1) { // 削除済みの場合
            continue;
          }

          FieldComprtnent fc = new FieldComprtnent();
          fc.getFileld(field.fieldId);
          List<Compartment> compartments = fc.getCompartmentList();

          for (Compartment cd : compartments) {
            if (cd.deleteFlag == 1) { // 削除済みの場合
              continue;
            }

            ObjectNode  kukakuJson = Json.newObject();
            kukakuJson.put("id"          , cd.kukakuId);
            kukakuJson.put("name"        , cd.kukakuName);
            kukakuJson.put("color"       , fg.fieldGroupColor);
            kukakuJson.put("sequenceId"  , cd.sequenceId);
            kukakuJson.put("lat"         , cd.lat);
            kukakuJson.put("lng"         , cd.lng);
            kukakulistJson.put(String.valueOf(cd.kukakuId) , kukakuJson);
          }
        }
        groupJson.put("kukakus"     , kukakulistJson);
        listJson.put(String.valueOf(fg.fieldGroupId), groupJson);
      }

      resultJson.put("datalist", listJson);
      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result latLngUpdate() {

      ObjectNode  resultJson = Json.newObject();

      try {
        Ebean.beginTransaction();
        JsonNode input = request().body().asJson();

        double kukakuId = input.get("kukakuId").asDouble();
        double lat      = input.get("lat").asDouble();
        double lng      = input.get("lng").asDouble();

        Compartment ct = Compartment.getCompartmentInfo(kukakuId);
        if (ct != null) {
          ct.lat = lat;
          ct.lng = lng;
          ct.update();
          resultJson.put("kukakuId", ct.kukakuId);
        }

        Ebean.commitTransaction();
      }
      catch (Exception ex) {
        Logger.error("[SortSeq] Update Error.", ex);
        ex.printStackTrace();
        Ebean.rollbackTransaction();
      }

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result latLngDelete() {

      ObjectNode  resultJson = Json.newObject();

      try {
        Ebean.beginTransaction();
        JsonNode input = request().body().asJson();
        JsonNode datalist = input.get("datalist");

        for (JsonNode info : datalist) {
          double kukakuId = info.get("kukakuId").asDouble();

          Compartment ct = Compartment.getCompartmentInfo(kukakuId);
          if (ct != null) {
            ct.lat = 0;
            ct.lng = 0;
            ct.update();
          }
        }

        Ebean.commitTransaction();
      }
      catch (Exception ex) {
        Logger.error("[SortSeq] Update Error.", ex);
        ex.printStackTrace();
        Ebean.rollbackTransaction();
      }

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
}
