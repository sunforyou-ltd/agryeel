package util;

import java.util.ArrayList;
import java.util.List;

import play.Logger;

/**
 * 【AGRYEEL】リスト操作ユーティリティ
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ListrU {

  public static boolean keyCheck(List<Double> pList, double pKey) {
    boolean result = false;
    if (pList.size() == 0) { //条件がない場合
      result = true;
    }
    else {
      for (Double list : pList) {
        if (list.doubleValue() == pKey) { //KEYが一致
          result = true;
          break;
        }
      }
    }
    return result;
  }
  public static boolean keyCheckStr(List<String> pList, String pKey) {
    boolean result = false;
    if (pList.size() == 0) { //条件がない場合
      result = true;
    }
    else {
      for (String list : pList) {
        if (list.equals(pKey)) { //KEYが一致
          result = true;
          break;
        }
      }
    }
    return result;
  }
  public static boolean keyChecks(List<Double> pList, String pKey) {
    boolean result = false;
    if (pList.size() == 0) { //条件がない場合
      result = true;
    }
    else {
      if (pKey != null) {
        String[] keys = pKey.split(",");
        for (String key : keys) {
          for (Double list : pList) {
            try {
              if (list.doubleValue() == Double.parseDouble(key)) { //KEYが一致
                result = true;
                break;
              }
            }
            catch (NumberFormatException e) {
              //次へ
            }
          }
        }
      }
    }
    return result;
  }
  /**
   * カンマ区切り文字をSQLIN句用Listに変換する(String)
   * @param pData
   * @return
   */
  public static ArrayList<String> makeInKeyString(String pData) {
    ArrayList<String> keys = new ArrayList<String>();
    if (pData != null && !"".equals(pData)) {
      String[] sData = pData.split(",");
      for (String data : sData) {
        keys.add(data);
      }
    }
    return keys;
  }
  /**
   * カンマ区切り文字をSQLIN句用Listに変換する(Double)
   * @param pData
   * @return
   */
  public static ArrayList<Double> makeInKeyDouble(String pData) {
    ArrayList<Double> keys = new ArrayList<Double>();
    if (pData != null && !"".equals(pData)) {
      String[] sData = pData.split(",");
      for (String data : sData) {
        try
        {
          keys.add(Double.parseDouble(data));
        }
        catch (Exception e) {
        }
      }
    }
    return keys;
  }
}
