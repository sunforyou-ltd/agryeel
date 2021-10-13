package controllers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import models.Compartment;
import models.CompartmentWorkChainStatus;
import models.CostOfCompartment;
import models.CostOfCrop;
import models.CostOfFarm;
import models.CropGroup;
import models.CropGroupList;
import models.WorkDiary;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * AI共通コントローラ
 * @author kimura
 *
 */
public class ManagementController extends Controller {

    public static Result updateCost(double farmId, int year) {
      ObjectNode resultJson = Json.newObject();
      try
      {
        Ebean.beginTransaction();
        //---------------------------------------------------------------------
        //- 区画別原価集計
        //---------------------------------------------------------------------
        Calendar cStart = Calendar.getInstance();
        Calendar cEnd   = Calendar.getInstance();
        Calendar cWork   = Calendar.getInstance();
        cStart.set(year, 1, 1);
        cEnd.set(year, 12, 31);
        List<Compartment> cps = Compartment.getCompartmentOfFarm(farmId);
        for (Compartment cp :cps) {
          Hashtable<String, CostOfCompartment> table = new Hashtable<String, CostOfCompartment>();
          for (int i = 1; i <= 12; i++) {
            CostOfCompartment coc = new CostOfCompartment();
            coc.kukakuId = cp.kukakuId;
            coc.year     = (short)year;
            coc.month    = (short)i;
            coc.costPeople  = 0;
            coc.costNouyaku = 0;
            coc.costHiryo   = 0;
            table.put(String.valueOf(i), coc);
          }
          List<WorkDiary> wds = WorkDiary.getWorkDiaryOfWork(cp.kukakuId, new java.sql.Timestamp(cStart.getTimeInMillis()), new java.sql.Timestamp(cEnd.getTimeInMillis()));
          for (WorkDiary wd :wds) {
            cWork.setTimeInMillis(wd.workDate.getTime());
            int month = cWork.get(Calendar.MONTH) + 1;
            CostOfCompartment coc = table.get(String.valueOf(month));
            coc.costPeople += wd.workTime * (720 / 60);
          }
          Ebean.createSqlUpdate("DELETE FROM cost_of_compartment WHERE kukaku_id = :kukakuId AND year = :year")
          .setParameter("kukakuId", cp.kukakuId).setParameter("year", year).execute();
          for (int i = 1; i <= 12; i++) {
            CostOfCompartment coc = table.get(String.valueOf(i));
            coc.save();
          }
        }
        //---------------------------------------------------------------------
        //- 品目別原価集計
        //---------------------------------------------------------------------
        List<CropGroup> cgs = CropGroup.getCropGroupOfFarm(farmId);
        for (CropGroup cg : cgs) {
          List<models.Crop> cs = CropGroupList.getCrop(cg.cropGroupId);
          for (models.Crop c : cs) {
            List<Double> keys = new ArrayList<Double>();
            for (Compartment cp :cps) {
              CompartmentWorkChainStatus cwcs = cp.getCompartmentWorkChainStatus();
              if (cwcs != null && cwcs.cropId == c.cropId) { //品目が一致した場合
                keys.add(cp.kukakuId);
              }
            }

            Hashtable<String, CostOfCrop> table = new Hashtable<String, CostOfCrop>();
            for (int i = 1; i <= 12; i++) {
              CostOfCrop coc = new CostOfCrop();
              coc.farmId   = farmId;
              coc.cropId   = c.cropId;
              coc.year     = (short)year;
              coc.month    = (short)i;
              coc.cost     = 0;
              table.put(String.valueOf(i), coc);
            }
            List<CostOfCompartment> cocs = CostOfCompartment.find.where().in("kukaku_id", keys).eq("year", year).orderBy("month").findList();
            for (CostOfCompartment coc:cocs) {
              CostOfCrop cc = table.get(String.valueOf(coc.month));
              cc.cost += (coc.costPeople + coc.costNouyaku + coc.costHiryo);
            }
            Ebean.createSqlUpdate("DELETE FROM cost_of_crop WHERE farm_id = :farmId AND crop_id = :cropId AND year = :year")
            .setParameter("farmId", farmId).setParameter("cropId", c.cropId).setParameter("year", year).execute();
            for (int i = 1; i <= 12; i++) {
              CostOfCrop cc = table.get(String.valueOf(i));
              cc.save();
            }
          }

          break;
        }
        //---------------------------------------------------------------------
        //- 生産者別原価集計
        //---------------------------------------------------------------------
        Hashtable<String, CostOfFarm> table = new Hashtable<String, CostOfFarm>();
        for (int i = 1; i <= 12; i++) {
          CostOfFarm cof = new CostOfFarm();
          cof.farmId   = farmId;
          cof.year     = (short)year;
          cof.month    = (short)i;
          cof.cost     = 0;
          table.put(String.valueOf(i), cof);
        }
        List<CostOfCrop> cocs = CostOfCrop.find.where().in("farm_id", farmId).eq("year", year).orderBy("crop_id,month").findList();
        for (CostOfCrop coc:cocs) {
          CostOfFarm cc = table.get(String.valueOf(coc.month));
          cc.cost += coc.cost;
        }
        Ebean.createSqlUpdate("DELETE FROM cost_of_farm WHERE farm_id = :farmId AND year = :year")
        .setParameter("farmId", farmId).setParameter("year", year).execute();
        for (int i = 1; i <= 12; i++) {
          CostOfFarm cc = table.get(String.valueOf(i));
          cc.save();
        }
        Ebean.commitTransaction();
      }
      catch (Exception e) {
        Logger.error(e.getMessage(), e);
        e.printStackTrace();
        Ebean.rollbackTransaction();;
      }
      return ok(resultJson);
    }

    public static Result getCostOfCompartment(double farmId, double cropId, int year) {
      DecimalFormat df = new DecimalFormat("#,##0");
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ArrayNode hojoKeihi = mapper.createArrayNode();

      List<Compartment> cts = Compartment.getCompartmentOfFarm(farmId);
      for (Compartment ct : cts) {
        CompartmentWorkChainStatus cwcs = ct.getCompartmentWorkChainStatus();
        if (cwcs.cropId != cropId) { //品目が一致しない場合、除外
          continue;
        }
        ArrayNode hojoCost = mapper.createArrayNode();
        //当年
        List<CostOfCompartment> cocs = CostOfCompartment.find.where().eq("year", year).eq("kukaku_id", ct.kukakuId).orderBy("month").findList();
        ObjectNode hojoJson = Json.newObject();
        hojoJson.put("name", ct.kukakuName);
        double people   = 0;
        double hiryo    = 0;
        double nouyaku  = 0;
        ArrayNode cost = mapper.createArrayNode();
        for (CostOfCompartment coc:cocs) {
          people += coc.costPeople;
          hiryo += coc.costHiryo;
          nouyaku += coc.costNouyaku;
          cost.add((coc.costPeople + coc.costHiryo + coc.costNouyaku));
        }
        hojoCost.add(cost);
        //前年
        cost = mapper.createArrayNode();
        cocs = CostOfCompartment.find.where().eq("year", (year - 1)).eq("kukaku_id", ct.kukakuId).orderBy("month").findList();
        for (CostOfCompartment coc:cocs) {
          cost.add((coc.costPeople + coc.costHiryo + coc.costNouyaku));
        }
        hojoCost.add(cost);
        hojoJson.put("people", df.format(people));
        hojoJson.put("hiryo", df.format(hiryo));
        hojoJson.put("nouyaku", df.format(nouyaku));
        hojoJson.put("cost", hojoCost);
        hojoKeihi.add(hojoJson);
      }

      return ok(hojoKeihi);
    }
    public static Result getCost(double farmId, int year) {
      ObjectNode resultJson = Json.newObject();
      DecimalFormat df = new DecimalFormat("#,##0");
      ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
      ObjectNode allJson = Json.newObject();
      ArrayNode hinmoku = mapper.createArrayNode();
      ArrayNode keihi = mapper.createArrayNode();
      //当年
      List<CostOfFarm> cofs = CostOfFarm.find.where().eq("farm_id", farmId).eq("year", year).orderBy("month").findList();
      ArrayNode allCost = mapper.createArrayNode();
      double pyear   = 0;
      double pprev   = 0;
      ArrayNode cost = mapper.createArrayNode();
      for (CostOfFarm cof:cofs) {
        pyear += cof.cost;
        cost.add(cof.cost);
      }
      allCost.add(cost);
      //前年
      cost = mapper.createArrayNode();
      cofs = CostOfFarm.find.where().eq("farm_id", farmId).eq("year", (year - 1)).orderBy("month").findList();
      for (CostOfFarm cof:cofs) {
        pprev += cof.cost;
        cost.add(cof.cost);
      }
      allCost.add(cost);
      if (pyear < pprev) {
        allJson.put("year", "↓" + df.format(pyear));
        allJson.put("color", "004d40");
      }
      else {
        allJson.put("year", "↑" + df.format(pyear));
        allJson.put("color", "b71c1c");
      }
      allJson.put("prev", df.format(pprev));
      allJson.put("cost", allCost);

      //生産者に紐づく品目グループを取得する
      List<CropGroup> cgs = CropGroup.getCropGroupOfFarm(farmId);
      for (CropGroup cg : cgs) {
        List<models.Crop> cs = CropGroupList.getCrop(cg.cropGroupId);
        for (models.Crop c : cs) {
          //当年
          List<CostOfCrop> cocs = CostOfCrop.find.where().eq("farm_id", farmId).eq("crop_id", c.cropId).eq("year", year).orderBy("month").findList();
          ArrayNode cropCost = mapper.createArrayNode();
          double cyear   = 0;
          double cprev   = 0;
          ArrayNode ccost = mapper.createArrayNode();
          for (CostOfCrop coc:cocs) {
            cyear += coc.cost;
            ccost.add(coc.cost);
          }
          cropCost.add(ccost);
          //前年
          cocs = CostOfCrop.find.where().eq("farm_id", farmId).eq("crop_id", c.cropId).eq("year", (year-1)).orderBy("month").findList();
          ccost = mapper.createArrayNode();
          for (CostOfCrop coc:cocs) {
            cprev += coc.cost;
            ccost.add(coc.cost);
          }
          cropCost.add(ccost);
          ObjectNode cropJson = Json.newObject();
          cropJson.put("name", c.cropName);
          switch ((int)c.cropId) {
          case 1:
            cropJson.put("image", "mizuna");
            break;
          case 2:
            cropJson.put("image", "komatuna");
            break;
          case 3:
            cropJson.put("image", "hourensou");
            break;
          case 4:
            cropJson.put("image", "tingensai");
            break;

          default:
            cropJson.put("image", "rush");
            break;
          }
          if (cyear < cprev) {
            cropJson.put("year", "↓" + df.format(cyear));
            cropJson.put("color", "004d40");
          }
          else {
            cropJson.put("year", "↑" + df.format(cyear));
            cropJson.put("color", "b71c1c");
          }
          cropJson.put("prev", df.format(cprev));
          cropJson.put("cost", cropCost);
          hinmoku.add(cropJson);
        }

        break;
      }
      allJson.put("hinmoku", hinmoku);

      return ok(allJson);
    }
}
