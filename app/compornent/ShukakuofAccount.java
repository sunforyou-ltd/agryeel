package compornent;

import java.util.ArrayList;
import java.util.List;

import models.Crop;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYEEL】担当者別収穫量一覧
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ShukakuofAccount {

	List<HarvestItem> harvests;
	List<Crop>        cropList;

	/**
	 * 生産物別収穫量
	 * @author kimura
	 *
	 */
	private class HarvestItem {
		double cropId 	= 0;
		double harvest 	= 0;
	}

	/**
	 * コンストラクタ
	 */
	public ShukakuofAccount(double farmId) {

		harvests = new ArrayList<HarvestItem>();
		cropList = new ArrayList<Crop>();
		clear(farmId);
	}

	/**
	 * 生産物別収穫量一覧を初期化します
	 */
	public void clear(double farmId) {

		harvests.clear();
		cropList.clear();
		makeList(farmId);

	}

	/**
	 * 生産物別収穫量リストを生成します
	 * @param farmId
	 */
	private void makeList(double farmId) {

	  //【AICA】TODO:生産者別生産物情報は見直す
//		List<models.CropOfFarm> crops = models.CropOfFarm.find.where().eq("farm_id", farmId).orderBy("crop_id").findList();
//		for (models.CropOfFarm crop : crops) {
//			HarvestItem item = new HarvestItem();
//			item.cropId = crop.cropId;
//			harvests.add(item);
//
//	        Crop cropdata = Crop.find.where().eq("crop_id", crop.cropId).findUnique();
//
//	        if (cropdata != null) {
//	        	cropList.add(cropdata);
//	        }
//		}
	}

	/**
	 * 指定された生産物に一致する項目を取得する
	 * @param cropId
	 * @return
	 */
	private HarvestItem getItem(double cropId) {

		HarvestItem item = null;

		for (int index = 0; index < harvests.size(); index++) {
			HarvestItem harves = harvests.get(index);
			if (harves.cropId == cropId) {
				item = harves;
			}
		}

		return item;

	}

	/**
	 * 収穫量を更新する
	 * @param cropId
	 * @param harvest
	 */
	public void updateHarvest(double cropId, double harvest) {

		HarvestItem item = getItem(cropId);

		if (item != null) {
			item.harvest += harvest;
		}

	}

	/**
	 * 集計結果をJSON形式で返します
	 * @return
	 */
	public ObjectNode getAccountHarvest() {

        ObjectNode listJson = Json.newObject();

		for (int index = 0; index < harvests.size(); index++) {
			HarvestItem harvest = harvests.get(index);
	        ObjectNode dataJson = Json.newObject();

	        Crop crop = Crop.find.where().eq("crop_id", harvest.cropId).findUnique();

	        if (crop != null && harvest.harvest != 0) {
	        	dataJson.put("id", harvest.cropId);
	        	dataJson.put("name", crop.cropName);
	        	dataJson.put("harvest", harvest.harvest);
	        	listJson.put(String.valueOf(harvest.cropId), dataJson);
	        }

		}

        return listJson;

	}

	/**
	 * 生産物一覧をJSON形式で返します
	 * @return
	 */
	public ObjectNode getCropList() {

        ObjectNode listJson = Json.newObject();

		for (int index = 0; index < cropList.size(); index++) {
			Crop crop = cropList.get(index);
	        ObjectNode dataJson = Json.newObject();

	        if (crop != null) {
	        	dataJson.put("id", crop.cropId);
	        	dataJson.put("name", crop.cropName);
	        	listJson.put(String.valueOf(crop.cropId), dataJson);
	        }

		}

        return listJson;

	}
}
