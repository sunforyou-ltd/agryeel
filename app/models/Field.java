package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】圃場情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Field extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1248634266107685221L;
    /**
     * 圃場ID
     */
    public double fieldId;
    /**
     * 圃場名
     */
    public String fieldName;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 地主ID
     */
    public double landlordId;
    /**
     * 郵便番号
     */
    public String postNo;
    /**
     * 都道府県
     */
    public String prefectures;
    /**
     * 市町村など
     */
    public String address;
    /**
     * 地目
     */
    public long geography;
    /**
     * 面積
     */
    public double area;
    /**
     * 土質
     */
    public long soilQuality;
    /**
     * 契約日
     */
    public Date contractDate;
    /**
     * 契約終了日
     */
    public Date contractEndDate;
    /**
     * 契約形態
     */
    public long contractType;
    /**
     * 賃借料
     */
    public double rent;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Field> find = new Finder<Long, Field>(Long.class, Field.class);

    /**
     * 圃場情報を取得します
     * @param pField
     * @return
     */
    public static Field getFieldInfo(double pField) {
      return Field.find.where().eq("field_id", pField).findUnique();
    }
    /**
     * 生産者情報を取得します
     * @return
     */
    public Farm getFarmInfo() {
      return Farm.find.where().eq("farm_id", this.farmId).findUnique();
    }
    /**
     * 地主情報を取得します
     * @return
     */
    public Landlord getLandlordInfo() {
      return Landlord.find.where().eq("landlord_id", this.landlordId).findUnique();
    }

    /**
     * 対象生産者の圃場情報を取得する
     * @param farmId
     * @return
     */
    public static List<Field> getFieldOfFarm(double farmId) {

    	List<Field> aryField = Field.find.where().eq("farm_id", farmId).orderBy("field_id asc").findList();

    	return aryField;

    }

    /**
     * 対象圃場の地主IDを取得する
     * @param farmId
     * @param fieldId
     * @return
     */
    public static double getLandlordId(double fieldId, double farmId) {

    	double result = 0;

    	Field field = Field.find.where().eq("field_id", fieldId).eq("farm_id", farmId).findUnique();

    	if ( field != null ) {

    		result = field.landlordId;

    	}

    	return result;

    }

    /**
     * 対象圃場の圃場名を取得する
     * @param fieldId
     * @return
     */
    public static String getFieldName(double fieldId) {

    	String result = "";

    	Field field = Field.find.where().eq("field_id", fieldId).findUnique();

    	if ( field != null ) {

    		result = field.fieldName;

    	}

    	return result;
    }

    /**
     * 地目
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class ConstGeography {
        /**
         * 畑
         */
        public static final int  	  FIELD 			= 1;
        /**
         * 田んぼ
         */
        public static final int  	  PADDYFIELD		= 2;
        /**
         * 宅地
         */
        public static final int  	  TAKUCHI			= 3;
    }

    /**
     * 土質
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class ConstSoilQuality {
        /**
         * 粘土
         */
        public static final int  	  CLAY 		= 1;
        /**
         * シルト
         */
        public static final int  	  SILT		= 2;
        /**
         * 砂
         */
        public static final int  	  SAND		= 3;
        /**
         * 礫
         */
        public static final int  	  MOTH		= 4;
    }

    /**
     * 契約形態
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class ConstContractType {
        /**
         * 年
         */
        public static final int  	  YEAR 			= 1;
        /**
         * 半年
         */
        public static final int  	  HALFYEAR		= 2;
        /**
         * 月
         */
        public static final int  	  MONTH			= 3;
    }

}
