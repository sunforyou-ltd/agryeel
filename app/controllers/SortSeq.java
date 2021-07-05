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

public class SortSeq extends Controller {

    /**
     * 【AGRYEEL】担当者別作業一覧
     * @return UI画面レンダー
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result move() {
        if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
          return redirect("/move");
        }
        return ok(views.html.sortSeq.render());
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
    /**
     * 圃場グループの並び順設定変更時
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result fieldGroupUpdate() {

      ObjectNode  resultJson = Json.newObject();

      try {
        Ebean.beginTransaction();
        JsonNode input = request().body().asJson();
        JsonNode datalist = input.get("datalist");

        for (JsonNode info : datalist) {
          FieldGroup fg = FieldGroup.getFieldGroup(info.get("fieldGroupId").asDouble());
          if (fg != null) {
            fg.sequenceId = info.get("sequenceId").asInt();
            fg.update();

          }
          else {
            Logger.error("[SortSeq] Compartment Not Exists kukakuId={}.", info.get("kukakuId").asDouble());
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
    /**
     * 区画の並び順設定変更時
     * @return
     */
    @Security.Authenticated(SessionCheckComponent.class)
    public static Result update() {

      ObjectNode  resultJson = Json.newObject();

      try {
        Ebean.beginTransaction();
        JsonNode input = request().body().asJson();
        JsonNode datalist = input.get("datalist");
        double fieldGroup = input.get("fieldGroupId").asDouble();

        Ebean.createSqlUpdate("DELETE FROM field_group_list WHERE field_group_id = :fieldGroupId")
        .setParameter("fieldGroupId", fieldGroup).execute();

        for (JsonNode info : datalist) {
          Compartment ct = Compartment.getCompartmentInfo(info.get("kukakuId").asDouble());
          if (ct != null) {
            ct.sequenceId = info.get("sequenceId").asInt();
            ct.update();

            Field fd = ct.getFieldInfo();

            FieldGroupList fgld = FieldGroupList.getFieldUnique(fieldGroup, fd.fieldId);
            if (fgld == null) {
              FieldGroupList fgl = new FieldGroupList();
              fgl.fieldGroupId = fieldGroup;
              fgl.fieldId      = fd.fieldId;
              fgl.sequenceId   = ct.sequenceId;
              fgl.save();
            }
          }
          else {
            Logger.error("[SortSeq] Compartment Not Exists kukakuId={}.", info.get("kukakuId").asDouble());
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
