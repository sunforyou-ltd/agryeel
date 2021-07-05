package compornent;

import java.util.ArrayList;
import java.util.List;

import models.Work;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYEEL】担当者別収穫量一覧
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorktimeAccount {

	List<WotkTimeItem> wokrtimes;
	List<Work>        workList;

	/**
	 * 担当者別作業時間
	 * @author kimura
	 *
	 */
	private class WotkTimeItem {
		double workId 	 = 0;
		double time 	   = 0;
	}

	/**
	 * コンストラクタ
	 */
	public WorktimeAccount(double farmId) {

	  wokrtimes = new ArrayList<WotkTimeItem>();
		workList = new ArrayList<Work>();
		clear(farmId);
	}

	/**
	 * 担当者別作業一覧を初期化します
	 */
	public void clear(double farmId) {

	  wokrtimes.clear();
	  workList.clear();
		makeList(farmId);

	}

	/**
	 * 生産物別収穫量リストを生成します
	 * @param farmId
	 */
	private void makeList(double farmId) {

		List<Work> works = models.Work.find.where().orderBy("work_id").findList();
		for (Work work : works) {
		  WotkTimeItem item = new WotkTimeItem();
			item.workId = work.workId;
			wokrtimes.add(item);
      workList.add(work);
		}
	}

	/**
	 * 指定された作業に一致する項目を取得する
	 * @param workId
	 * @return
	 */
	private WotkTimeItem getItem(double workId) {

	  WotkTimeItem item = null;

		for (int index = 0; index < wokrtimes.size(); index++) {
		  WotkTimeItem works = wokrtimes.get(index);
			if (works.workId == workId) {
				item = works;
			}
		}

		return item;

	}

	/**
	 * 作業時間を更新する
	 * @param workId
	 * @param worktime
	 */
	public void updateWorkTime(double workId, double worktime) {

	  WotkTimeItem item = getItem(workId);

		if (item != null) {
			item.time += worktime;
		}

	}

	/**
	 * 集計結果をJSON形式で返します
	 * @return
	 */
	public ObjectNode getAccountWorktime() {

        ObjectNode listJson = Json.newObject();

		for (int index = 0; index < wokrtimes.size(); index++) {
		  WotkTimeItem wokrtime = wokrtimes.get(index);
	        ObjectNode dataJson = Json.newObject();

	        Work work = Work.find.where().eq("work_id", wokrtime.workId).findUnique();

	        if (work != null && wokrtime.time != 0) {
	        	dataJson.put("id", wokrtime.workId);
	        	dataJson.put("name", work.workName);
	        	dataJson.put("time", wokrtime.time);
	        	listJson.put(String.valueOf(wokrtime.workId), dataJson);
	        }

		}

        return listJson;

	}

	/**
	 * 作業一覧をJSON形式で返します
	 * @return
	 */
	public ObjectNode getWorkList() {

        ObjectNode listJson = Json.newObject();

		for (int index = 0; index < workList.size(); index++) {
		  Work work = workList.get(index);
	        ObjectNode dataJson = Json.newObject();

	        if (work != null) {
	        	dataJson.put("id", work.workId);
	        	dataJson.put("name", work.workName);
	        	listJson.put(String.valueOf(work.workId), dataJson);
	        }

		}

        return listJson;

	}
}
