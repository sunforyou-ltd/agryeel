package compornent;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Analysis {

  long count=0;
  List<Double> keys = new ArrayList<Double>();
  Hashtable<Double, Double> datas = new Hashtable<Double, Double>();

  public Analysis() {

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
  public void put(double tg, double vl) {
    Double data = datas.get(tg);
    if (data == null) {
      datas.put(tg, new Double(vl));
    }
    else {
      datas.put(tg, new Double(data.doubleValue() + vl));
    }
  }
  public double data(double tg) {
    Double data = datas.get(tg);
    if (data == null) {
      return 0;
    }
    return data.doubleValue();
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
    datas = new Hashtable<Double, Double>();
  }
  public void clearValue() {
    datas = new Hashtable<Double, Double>();
    for (Double key : keys) {
      put(key, 0);
    }
  }

}
