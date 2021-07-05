package compornent;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SaibaiPlanHinsyuList {

  public class SaibaiPlanHinsyu {
    public double hinsyuId;
    public double shukakuRyo;
    public SaibaiPlanHinsyu() {

    }
    public SaibaiPlanHinsyu(double tg, double vl) {
      hinsyuId    = tg;
      shukakuRyo  = vl;
    }
  }

  long count=0;
  List<Double> keys = new ArrayList<Double>();
  Hashtable<Double, SaibaiPlanHinsyu> datas = new Hashtable<Double, SaibaiPlanHinsyu>();

  public SaibaiPlanHinsyuList() {

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
    SaibaiPlanHinsyu data = datas.get(tg);
    if (data == null) {
      datas.put(tg, new SaibaiPlanHinsyu(tg, vl));
    }
    else {
      if (data.shukakuRyo < vl) {
        data.shukakuRyo = vl;
      }
    }
  }
  public SaibaiPlanHinsyu data(double tg) {
    SaibaiPlanHinsyu data = datas.get(tg);
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
    datas = new Hashtable<Double, SaibaiPlanHinsyu>();
  }
  public void clearValue() {
    datas = new Hashtable<Double, SaibaiPlanHinsyu>();
    for (Double key : keys) {
      put(key, 0);
    }
  }
  public double getMaxHinshu() {
    double hinsyuId   = 0;
    double shukakuRyo = 0;

    for (Double key : keys) {
      SaibaiPlanHinsyu sph = data(key);
      if (hinsyuId == 0) {
        hinsyuId = sph.hinsyuId;
        shukakuRyo = sph.shukakuRyo;
      }
      else {
        if (shukakuRyo < sph.shukakuRyo) {
          hinsyuId = sph.hinsyuId;
          shukakuRyo = sph.shukakuRyo;
        }
      }
    }

    return hinsyuId;
  }
}
