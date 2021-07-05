package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Farm extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 5774429765551891368L;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 生産者名
     */
    public String farmName;
    /**
     * 生産者グループID
     */
    public double farmGroupId;
    /**
     * 代表者
     */
    public String representativeName;
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
     * ホームページＵＲＬ
     */
    public String url;
    /**
     * レジストレーションコード
     */
    public String registrationCode;

    public static Finder<Long, Farm> find = new Finder<Long, Farm>(Long.class, Farm.class);

    /**
     * 対象生産者情報を取得する
     * @param farmId
     * @return
     */
    public static Farm getFarm(double farmId) {
    	return Farm.find.where().eq("farm_id", farmId).findUnique();
    }

    /**
     * 対象生産者グループの生産者情報を取得する
     * @param farmGroupId
     * @return
     */
    public static List<Farm> getFarmOfGroup(double farmGroupId) {

    	List<Farm> aryFarm = Farm.find.where().eq("farm_group_id", farmGroupId).orderBy("farm_id desc").findList();

    	return aryFarm;

    }

    /**
     * 生産者ステータスを取得する
     * @return
     */
    public FarmStatus getFarmStatus() {
      return FarmStatus.getFarmStatus(farmId);
    }

}
