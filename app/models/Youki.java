package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】容器情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Youki extends Model {

    /**
     * 容器ID
     */
    public double youkiId;
    /**
     * 容器名
     */
    public String youkiName;
    /**
     * 容器種別
     */
    public int youkiKind;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 単位種別
     */
    public int unitKind;
    /**
     * 個数
     */
    public double kosu;
    /**
     * 金額
     */
    public double kingaku;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Youki> find = new Finder<Long, Youki>(Long.class, Youki.class);

    public static String getYoukiName(double youkiId) {

    	String result = "";

    	Youki youki = Youki.find.where().eq("youki_id", youkiId).findUnique();

    	if ( youki != null ) {

    		result = youki.youkiName;

    	}

    	return result;

    }
    public static int getUnitKind(double youkiId) {

    	int result = 0;

    	Youki youki = Youki.find.where().eq("youki_id", youkiId).findUnique();

    	if ( youki != null ) {

    		result = youki.unitKind;

    	}

    	return result;

    }
<<<<<<< HEAD
=======
    public static double getKosu(double youkiId) {

    	double result = 0;

    	Youki youki = Youki.find.where().eq("youki_id", youkiId).findUnique();

    	if ( youki != null ) {

    		result = youki.kosu;

    	}

    	return result;

    }
>>>>>>> e747d9c9b47c3d59e92c6b368bbb246cc6564120
    public String getUnitString() {

      String result = "";

      switch (this.unitKind) {
      case 1:
        result = "枚";
        break;

      case 2:
        result = "穴";
        break;

      case 3:
        result = "個";
        break;

      default:
        break;
      }

      return result;

    }

    /**
     * 容器種別
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
         * トレイ
         */
        public static final int  	  TRAY 				= 1;
        /**
         * セルトレイ
         */
        public static final int  	  CELLTRAY 			= 2;
        /**
         * ポット
         */
        public static final int  	  POT 				= 3;
        /**
         * ペーパーポット
         */
        public static final int  	  PAPERPOT 			= 4;
        /**
         * 苗箱
         */
        public static final int  	  NAEBAKO 			= 5;
    }

    /**
     * 容器情報を取得します
     * @return
     */
    public static Youki getYoukiInfo(double youkiId) {
      return Youki.find.where().eq("youki_id", youkiId).findUnique();
    }

    /**
     * 対象生産者の容器情報を取得する
     * @param farmId
     * @return
     */
    public static List<Youki> getYoukiOfFarm(double farmId) {

    	List<Youki> aryYouki = Youki.find.where().eq("farm_id", farmId).orderBy("youki_id asc").findList();

    	return aryYouki;

    }
}
