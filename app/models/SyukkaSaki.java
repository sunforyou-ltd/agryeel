package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】出荷先情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SyukkaSaki extends Model {

    /**
     * 出荷先ID
     */
    public double syukkaSakiId;
    /**
     * 出荷先名
     */
    public String syukkaSakiName;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, SyukkaSaki> find = new Finder<Long, SyukkaSaki>(Long.class, SyukkaSaki.class);

    public static double convertId(String pName, double pFarmId) {
      double result = 0;

      List<SyukkaSaki> syukkaSakis = SyukkaSaki.find.where()
                                               .eq("syukka_saki_name", pName)
                                               .eq("farm_id", pFarmId)
                                               .findList();
      if (syukkaSakis.size() == 1) { //該当レコードが１件のみ
        result = syukkaSakis.get(0).syukkaSakiId;
      }

      return result;
    }
}
