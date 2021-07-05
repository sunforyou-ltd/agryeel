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
import models.KinmuDiary;
import models.KinmuStatus;
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

/**
 * 出退勤管理用コントローラー
 * @author kimura
 *
 */
public class KinmuController extends Controller {

    public static Result addKinmuDiary(String accountId, int kinmuFlag) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode 	resultJson = Json.newObject();

      /* 本来はここに整合性チェックを実装する */
      UserComprtnent uc = new UserComprtnent();
      int returnCode = uc.GetAccountData(accountId);


      /* 勤務記録を作成する */
      Calendar system = Calendar.getInstance();
      KinmuDiary kd = new KinmuDiary();
      kd.accountId = accountId;
      kd.kinmuId   = system.getTimeInMillis();
      kd.kinmuFlag = (short)kinmuFlag;
      kd.message   = editMessage(uc.accountData.acountName, (short)kinmuFlag);
      kd.dayTime   = new java.sql.Timestamp(system.getTimeInMillis());
      kd.year      = (short)system.get(Calendar.YEAR);
      kd.month     = (short)(system.get(Calendar.MONTH) + 1);
      kd.day       = (short)system.get(Calendar.DAY_OF_MONTH);
      kd.save();

      KinmuStatus.updateStatus(uc.accountData.farmId, kd.accountId, kd.year, kd.month, kd.day);

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    public static Result getKinmuStatus(String accountId, int year, int month, int day) {

      /* 戻り値用JSONデータの生成 */
      ObjectNode  resultJson = Json.newObject();

      /* 本来はここに整合性チェックを実装する */
      UserComprtnent uc = new UserComprtnent();
      int returnCode = uc.GetAccountData(accountId);

      /*
       * 【ここで返すJSONのイメージ】
       * {id:"", name:"", status:0, data[{key:0, flag:1, ds:"yyyy/MM/dd HH:mm:ss", message:""}, {key:1, flag:2, ds:"yyyy/MM/dd HH:mm:ss", message:""}]}
       * */
      KinmuStatus ks = KinmuStatus.find.where().eq("account_id", accountId).eq("year", year).findUnique();
      resultJson.put("id"   , uc.accountData.accountId);
      resultJson.put("name" , uc.accountData.acountName);
      short flag = 2; /* 初期値は退勤済 */
      if (ks != null) {
        flag = ks.kinmuStatus;
      }
      resultJson.put("status" , flag);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode datas = mapper.createArrayNode();
      List<KinmuDiary> kds = KinmuDiary.find.where().eq("account_id", accountId).eq("year", year).eq("month", month).eq("day", day).orderBy("kinmu_id asc").findList();
      for (KinmuDiary kd : kds) {
        ObjectNode  dataJson = Json.newObject();
        dataJson.put("key"      , kd.kinmuId);
        dataJson.put("flag"     , kd.kinmuFlag);
        dataJson.put("ds"       , sdf.format(kd.dayTime));
        dataJson.put("message"  , kd.message);
        datas.add(dataJson);
      }
      resultJson.put("data" , datas);

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

      return ok(resultJson);
    }
    private static String editMessage(String name, short kinmuFlag) {
      String result = "";
      switch (kinmuFlag) {
      case 1: /* 出勤 */
        result = "[" + name + "]さんは出勤しました。";
        break;

      case 2: /* 退勤 */
        result = "[" + name + "]さんは帰宅しました。";
        break;

      case 3: /* 休憩開始 */
        result = "[" + name + "]さんは休憩を開始しました。";
        break;

      case 4: /* 休憩終了 */
        result = "[" + name + "]さんは休憩を終了しました。";
        break;

      default:
        break;
      }
      return result;
    }
}