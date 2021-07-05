package controllers;

import java.util.List;

import models.Crop;
import models.CropInfoOfFarm;
import models.YearInfoOfFarm;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * AI共通コントローラ
 * @author kimura
 *
 */
public class AICASCOREController extends Controller {

    public static Result getInfo(double farmId, int year) {
      ObjectNode resultJson = Json.newObject();
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode ydata = mapper.createArrayNode();
      ArrayNode cdata = mapper.createArrayNode();

      List<YearInfoOfFarm> yofs = YearInfoOfFarm.find.where().eq("farm_id", farmId).eq("year", year).orderBy("month").findList();
      for (YearInfoOfFarm yof : yofs) {
        ObjectNode dataJson  = Json.newObject();
        dataJson.put("month", yof.month);
        dataJson.put("shukaku", yof.shukakuRyo);
        dataJson.put("pshukaku", yof.shukakuRyoPrev);
        dataJson.put("time", yof.workTimeMonth);
        dataJson.put("ptime", yof.workTimeMonthPrev);
        ydata.add(dataJson);
      }
      List<CropInfoOfFarm> cofs = CropInfoOfFarm.find.where().eq("farm_id", farmId).eq("year", year).orderBy("crop_id").findList();
      for (CropInfoOfFarm cof : cofs) {
        ObjectNode dataJson  = Json.newObject();
        dataJson.put("id", cof.cropId);
        Crop cp = Crop.getCropInfo(cof.cropId);
        if (cp == null) {
          continue;
        }
        dataJson.put("name", cp.cropName);
        dataJson.put("color", cp.cropColor);
        dataJson.put("shukaku", cof.shukakuRyo);
        dataJson.put("pshukaku", cof.shukakuRyoPrev);
        dataJson.put("time", cof.workTimeMonth);
        dataJson.put("ptime", cof.workTimeMonthPrev);
        cdata.add(dataJson);
      }
      resultJson.put("yearinfo", ydata);
      resultJson.put("cropinfo", cdata);

      return ok(resultJson);
    }
}
