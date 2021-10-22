<<<<<<< HEAD
package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】土情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Soil extends Model {

    /**
     * 土ID
     */
    public double soilId;
    /**
     * 土名
     */
    public String soilName;
    /**
     * 土種別
     */
    public int soilKind;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 単位種別
     */
    public int unitKind;
    /**
     * 金額
     */
    public double kingaku;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Soil> find = new Finder<Long, Soil>(Long.class, Soil.class);

    public static String getSoilName(double soilId) {

    	String result = "";

    	Soil soil = Soil.find.where().eq("soil_id", soilId).findUnique();

    	if ( soil != null ) {

    		result = soil.soilName;

    	}

    	return result;

    }
    public static int getUnitKind(double soilId) {

    	int result = 0;

    	Soil soil = Soil.find.where().eq("soil_id", soilId).findUnique();

    	if ( soil != null ) {

    		result = soil.unitKind;

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

      default:
        break;
      }

      return result;

    }

    /**
     * 土種別
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
         * 培土
         */
        public static final int  	  BAIDO 			= 1;
        /**
         * 覆土
         */
        public static final int  	  FUKUDO 			= 2;
    }

    /**
     * 土情報を取得します
     * @return
     */
    public static Soil getSoilInfo(double soilId) {
      return Soil.find.where().eq("soil_id", soilId).findUnique();
    }

    /**
     * 対象生産者の土情報を取得する
     * @param farmId
     * @return
     */
    public static List<Soil> getSoilOfFarm(double farmId) {

    	List<Soil> arySoil = Soil.find.where().eq("farm_id", farmId).orderBy("soil_id asc").findList();

    	return arySoil;

    }
}
=======
package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】土情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Soil extends Model {

    /**
     * 土ID
     */
    public double soilId;
    /**
     * 土名
     */
    public String soilName;
    /**
     * 土種別
     */
    public int soilKind;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 単位種別
     */
    public int unitKind;
    /**
     * 金額
     */
    public double kingaku;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Soil> find = new Finder<Long, Soil>(Long.class, Soil.class);

    public static String getSoilName(double soilId) {

    	String result = "";

    	Soil soil = Soil.find.where().eq("soil_id", soilId).findUnique();

    	if ( soil != null ) {

    		result = soil.soilName;

    	}

    	return result;

    }
    public static int getUnitKind(double soilId) {

    	int result = 0;

    	Soil soil = Soil.find.where().eq("soil_id", soilId).findUnique();

    	if ( soil != null ) {

    		result = soil.unitKind;

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

      default:
        break;
      }

      return result;

    }

    /**
     * 土種別
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
         * 培土
         */
        public static final int  	  BAIDO 			= 1;
        /**
         * 覆土
         */
        public static final int  	  FUKUDO 			= 2;
    }

    /**
     * 土情報を取得します
     * @return
     */
    public static Soil getSoilInfo(double soilId) {
      return Soil.find.where().eq("soil_id", soilId).findUnique();
    }

    /**
     * 対象生産者の土情報を取得する
     * @param farmId
     * @return
     */
    public static List<Soil> getSoilOfFarm(double farmId) {

    	List<Soil> arySoil = Soil.find.where().eq("farm_id", farmId).orderBy("soil_id asc").findList();

    	return arySoil;

    }
}
>>>>>>> e747d9c9b47c3d59e92c6b368bbb246cc6564120
