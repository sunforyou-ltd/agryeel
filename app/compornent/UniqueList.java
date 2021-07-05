package compornent;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class UniqueList {

  long count=0;
  List<Double> keys = new ArrayList<Double>();
  Hashtable<Double, Object> datas = new Hashtable<Double, Object>();

  public UniqueList() {

  }

  public boolean check(double tg) {
    boolean result = false;
    for (Double key : keys) {
      if (key.doubleValue() == tg) {
        result = true;
        break;
      }
    }
    return result;
  }

  public void add(double tg) {
    if (!check(tg)) { /* KEYに存在しない場合のみリストにキーを追加 */
      keys.add(new Double(tg));
      count++;
    }
  }
  public void put(double tg, Object vl) {
    Object data = datas.get(tg);
    if (data == null) {
      datas.put(tg, vl);
    }
  }
  public Object data(double tg) {
    Object data = datas.get(tg);
    if (data == null) {
      return 0;
    }
    return data;
  }

  public long getCount() {
    return count;
  }

  public List<Double> getKeys() {
    return keys;
  }

  public void clear() {
    count=0;
    keys.clear();
    datas = new Hashtable<Double, Object>();
  }
  public void clearValue() {
    datas = new Hashtable<Double, Object>();
    for (Double key : keys) {
      put(key, 0);
    }
  }

}
