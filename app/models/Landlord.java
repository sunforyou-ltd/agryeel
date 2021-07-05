package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】地主情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Landlord extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -3146326155415925221L;
    /**
     * 地主ID
     */
    public double landlordId;
    /**
     * 地主名
     */
    public String landlordName;
    /**
     * 生産者ID
     */
    public double farmId;
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
     * 電話番号
     */
    public String tel;
    /**
     * 責任者携帯番号
     */
    public String responsibleMobileTel;
    /**
     * FAX
     */
    public String fax;
    /**
     * メールアドレス（パソコン）
     */
    public String mailAddressPC;
    /**
     * メールアドレス（携帯）
     */
    public String mailAddressMobile;
    /**
     * 振込先銀行名
     */
    public String bankName;
    /**
     * 振込先口座種別
     */
    public long accountType;
    /**
     * 振込先口座番号
     */
    public String accountNumber;
    /**
     * 支払日
     */
    public String paymentDate;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Landlord> find = new Finder<Long, Landlord>(Long.class, Landlord.class);

    /**
     * 口座種別
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class ConstAccountType {
        /**
         * 普通
         */
        public static final int  	  USUALLY 		= 1;
        /**
         * 定期
         */
        public static final int  	  REGULAR		= 2;
        /**
         * 当座
         */
        public static final int  	  CURRENT		= 3;
    }

    /**
     * 地主名を取得する
     * @param landlordId
     * @return
     */
    public static String getLandlordName(double landlordId) {

    	String result = "";

    	Landlord landlord = Landlord.find.where().eq("landlord_id", landlordId).findUnique();

    	if ( landlord != null ) {

    		result = landlord.landlordName;

    	}

    	return result;
    }

    /**
     * 対象生産者の地主情報を取得する
     * @param farmId
     * @return
     */
    public static List<Landlord> getLandlordOfFarm(double farmId) {

    	List<Landlord> aryLandlord = Landlord.find.where().eq("farm_id", farmId).orderBy("landlord_id asc").findList();

    	return aryLandlord;

    }

    /**
     * 地主情報を取得します
     * @param pField
     * @return
     */
    public static Landlord getLandlordInfo(double pLandlordld) {
      return Landlord.find.where().eq("landlord_id", pLandlordld).findUnique();
    }
}
