package compornent;

import java.util.ArrayList;
import java.util.List;

import models.Common;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 【AGRYELL】コモンコンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CommonComprtnent implements AgryellInterface {


  List<Common> commons = new ArrayList<Common>();

	/**
	 * コンストラクタ
	 */
	public CommonComprtnent() {

	  commons = new ArrayList<Common>();

	}

	/**
	 * 初期化
	 */
	public void clear() {
	  commons.clear();
	}

	/**
	 * 共通種別から共通リストを取得する
	 * @param pCommonClass
	 * @return
	 */
	public int getCommonList(int pCommonClass) {

		/** 戻り値 */
		int	result	=	GET_SUCCESS;

		commons = Common.GetCommonList(pCommonClass);

    if (commons.size() == 0) {
      result	=	GET_ERROR;
    }

    return result;

	}

	/**
	 * 共通種別から共通リストJSONを作成する
	 * @param pCommonClass
	 * @param pListJson
	 * @return
	 */
  public int getCommonJson(int pCommonClass, ObjectNode pListJson) {

    /** 戻り値 */
    int result  = GET_SUCCESS;

    result = getCommonList(pCommonClass);

    if (result == GET_SUCCESS) { //共通情報が取得できた場合
      for (Common data : commons) {

        ObjectNode jd = Json.newObject();

        jd.put("id"   , data.commonSeq);
        jd.put("name" , data.commonName);

        pListJson.put(String.valueOf(data.commonSeq), jd);

      }
    }

    return result;

  }

	/**
	 * 共通種別から共通リストJSONを作成する
	 * @param pCommonClass
	 * @param pListJson
	 * @return
	 */
	public int getCommonJsonArray(int pCommonClass, ArrayNode pListJson) {

	  /** 戻り値 */
	  int result  = GET_SUCCESS;

	  result = getCommonList(pCommonClass);

	  if (result == GET_SUCCESS) { //共通情報が取得できた場合
	    for (Common data : commons) {

	      ObjectNode jd = Json.newObject();

	      jd.put("id"   , data.commonSeq);
	      jd.put("name" , data.commonName);
	      jd.put("flag" , 0);

	      pListJson.add(jd);

	    }
	  }

	  return result;

	}
}
