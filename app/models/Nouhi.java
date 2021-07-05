package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】農肥情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Nouhi extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1326878839998278932L;
    /**
     * 農肥ID
     */
    public double nouhiId;
    /**
     * 農肥名
     */
    public String nouhiName;
    /**
     * 農肥種別
     */
    public int nouhiKind;
    /**
     * 倍率
     */
    public double bairitu;
    /**
     * 散布量
     */
    public double sanpuryo;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 単位種別
     */
    public int unitKind;
    /**
     * N
     */
    public double n;
    /**
     * P
     */
    public double p;
    /**
     * K
     */
    public double k;
    /**
     * 倍率下限
     */
    public int lower;
    /**
     * 倍率上限
     */
    public int upper;
    /**
     * 最終経過日数
     */
    public int finalDay;
    /**
     * 散布回数
     */
    public double sanpuCount;
    /**
     * 使用時期
     */
    public double useWhen;
    /**
     * 使用回数
     */
    public int useCount;
    /**
     * Mg
     */
    public double mg;
    /**
     * 効果対象害虫
     */
    public String targetPest;
    /**
     * 金額
     */
    public double kingaku;
    /**
     * 削除フラグ
     */
    public short deleteFlag;
    /**
     * 農肥正式名
     */
    public String nouhiOfficialName;
    /**
     * 登録番号
     */
    public long registNumber;

    public static Finder<Long, Nouhi> find = new Finder<Long, Nouhi>(Long.class, Nouhi.class);

    public static String getNouhiName(double nouhiId) {

    	String result = "";

    	Nouhi nouhi = Nouhi.find.where().eq("nouhi_id", nouhiId).findUnique();

    	if ( nouhi != null ) {

    		result = nouhi.nouhiName;

    	}

    	return result;

    }
    public static int getUnitKind(double nouhiId) {

    	int result = 0;

    	Nouhi nouhi = Nouhi.find.where().eq("nouhi_id", nouhiId).findUnique();

    	if ( nouhi != null ) {

    		result = nouhi.unitKind;

    	}

    	return result;

    }
    public String getUnitString() {

      String result = "";

      switch (this.unitKind) {
      case 1:
        result = "Kg";
        break;

      case 2:
        result = "L";
        break;

      case 3:
        result = "g";
        break;

      case 4:
        result = "mL";
        break;

      case 5:
        result = "個";
        break;

      default:
        break;
      }

      return result;

    }
    public double getUnitHosei() {

      double result = 1;

      switch (this.unitKind) {
      case 1:
        result = 0.001;
        break;

      case 2:
        result = 0.001;
        break;

      default:
        break;
      }

      return result;

    }

    /**
     * 農肥種別
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class ConstKind {
        /**
         * 全て
         */
        public static final int  	  ALL 				= 0;
        /**
         * 肥料
         */
        public static final int  	  HIRYO 			= 1;
        /**
         * 農薬
         */
        public static final int  	  NOUYAKU 			= 2;
        /**
         * 土壌消毒
         */
        public static final int  	  DOJOU 			= 3;
    }

    /**
     * 農肥情報を取得します
     * @return
     */
    public static Nouhi getNouhiInfo(double nouhiId) {
      return Nouhi.find.where().eq("nouhi_id", nouhiId).findUnique();
    }

    /**
     * 対象生産者の農肥情報を取得する
     * @param farmId
     * @return
     */
    public static List<Nouhi> getNouhiOfFarm(double farmId) {

    	List<Nouhi> aryNouhi = Nouhi.find.where().eq("farm_id", farmId).orderBy("nouhi_id asc").findList();

    	return aryNouhi;

    }
    public boolean checkTargetPest(double pPestId) {
      boolean result = false;

      if (this.targetPest != null && !"".equals(this.targetPest)) {
        String[] targets = targetPest.split(",");
        for (String target : targets) {
          if (Double.parseDouble(target) == pPestId) {
            result = true;
            break;
          }
        }
      }

      return result;
    }
}
