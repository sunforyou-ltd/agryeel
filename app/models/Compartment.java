package models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import compornent.FieldComprtnent;

import play.Logger;
import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】区画情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Compartment extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 960402511105747780L;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 区画名
     */
    public String kukakuName;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 圃場ID
     */
    public double fieldId;
    /**
     * 面積
     */
    public double area;
    /**
     * 土質
     */
    public long soilQuality;
    /**
     * 間口
     */
    public double frontage;
    /**
     * 奥行
     */
    public double depth;
    /**
     * 潅水方法
     */
    public long kansuiMethod;
    /**
     * 潅水量
     */
    public double kansuiRyo;
    /**
     * 潅水時間
     */
    public long kansuiTime;
    /**
     * 潅水順番
     */
    public long kansuiOrder;
    /**
     * 区画種別
     */
    public long kukakuKind;
    /**
     * ハウス名
     */
    public String houseName;
    /**
     * 金額
     */
    public double kingaku;
    /**
     * 購入日
     */
    public Date purchaseDate;
    /**
     * 耐用年数
     */
    public long serviceLife;
    /**
     * シーケンスID
     */
    public long sequenceId;
    /**
     * 緯度
     */
    public double lat;
    /**
     * 経度
     */
    public double lng;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Compartment> find = new Finder<Long, Compartment>(Long.class, Compartment.class);

    /**
     * 区画情報を取得します
     * @param pField
     * @return
     */
    public static Compartment getCompartmentInfo(double pKukaku) {
      return Compartment.find.where().eq("kukaku_id", pKukaku).findUnique();
    }

    /**
     * 対象生産者の区画情報を取得する
     * @param farmId
     * @return
     */
    public static List<Compartment> getCompartmentOfFarm(double farmId) {

    	List<Compartment> aryCompartment = Compartment.find.where().eq("farm_id", farmId).orderBy("field_id asc, kukaku_id asc").findList();

    	return aryCompartment;

    }

    /**
     * 対象生産者の区画IDを取得する
     * @param farmId
     * @return
     */
    public static List<Double> getKukakuIdOfFarm(double farmId) {
      List<Double> aryCompartment = new ArrayList<Double>();
      List<FieldGroup> fgs = FieldGroup.getFieldGroupOfFarm(farmId);

      for (FieldGroup fg : fgs) {
        List<Field> fields = FieldComprtnent.getField(fg.fieldGroupId);
        for (Field field : fields) {

          FieldComprtnent fc = new FieldComprtnent();
          fc.getFileld(field.fieldId);
          List<Compartment> compartments = fc.getCompartmentList();
          for (Compartment compartmentData : compartments) {
            aryCompartment.add(compartmentData.kukakuId);
          }
        }
      }

      return aryCompartment;

    }

    /**
     * 対象生産者の区画情報を取得する（シーケンス順）
     * @param farmId
     * @return
     */
    public static List<Compartment> getCompartmentOfFarmSort(double farmId) {
      List<Compartment> aryCompartment = new ArrayList<Compartment>();
      List<FieldGroup> fgs = FieldGroup.getFieldGroupOfFarm(farmId);

      for (FieldGroup fg : fgs) {
        List<Field> fields = FieldComprtnent.getField(fg.fieldGroupId);
        for (Field field : fields) {

          FieldComprtnent fc = new FieldComprtnent();
          fc.getFileld(field.fieldId);
          List<Compartment> compartments = fc.getCompartmentList();
          for (Compartment compartmentData : compartments) {
            aryCompartment.add(compartmentData);
          }
        }
      }

    	return aryCompartment;

    }

    /**
     * 対象圃場の区画情報を取得する
     * @param farmId
     * @param fieldId
     * @return
     */
    public static List<Compartment> getCompartmentOfField(double farmId,double fieldId) {

    	List<Compartment> aryCompartment = Compartment.find.where().eq("farm_id", farmId).eq("field_id", fieldId).orderBy("kukaku_id asc").findList();

    	return aryCompartment;

    }

    /**
     * 対象区画の圃場を取得します
     * @return
     */
    public Field getFieldInfo() {
      return Field.find.where().eq("field_id", this.fieldId).findUnique();
    }
    /**
     * 対象区画の圃場グループを取得します
     * @return
     */
    public FieldGroup getFieldGroupInfo() {
      FieldGroup fg = null;
      List<FieldGroupList> fgls = FieldGroupList.find.where().eq("field_id", this.fieldId).order("field_group_id").findList();
      if (fgls.size() > 0) {
        fg = FieldGroup.find.where().eq("field_group_id", fgls.get(0).fieldGroupId).findUnique();
      }

      return fg;
    }

    /**
     * 対象生産者の未所属区画情報を取得する
     * @param farmId
     * @return
     */
    public static List<Compartment> getCompartmentOfFarmNot(double farmId) {

    	List<Compartment> aryCompartment = Compartment.find.where().eq("farm_id", farmId).eq("field_id", 0).orderBy("kukaku_id asc").findList();

    	return aryCompartment;

    }
    public CompartmentWorkChainStatus getCompartmentWorkChainStatus() {
      return CompartmentWorkChainStatus.find.where().eq("kukaku_id", this.kukakuId).findUnique();
    }
    //世界観測値系
    public static final double GRS80_A = 6378137.000;//長半径 a(m)
    public static final double GRS80_E2 = 0.00669438002301188;//第一遠心率  eの2乗

    public double deg2rad(double deg){
        return deg * Math.PI / 180.0;
    }
    public boolean getDistance(double pLat, double pLng, int pRadius){

      if (pLat == 0 && pLng == 0) {
        return false;
      }

      double my = deg2rad((pLat + this.lat) / 2.0); //緯度の平均値
      double dy = deg2rad(pLat - this.lat); //緯度の差
      double dx = deg2rad(pLng - this.lng); //経度の差

      //卯酉線曲率半径を求める(東と西を結ぶ線の半径)
      double sinMy = Math.sin(my);
      double w = Math.sqrt(1.0 - GRS80_E2 * sinMy * sinMy);
      double n = GRS80_A / w;

      //子午線曲線半径を求める(北と南を結ぶ線の半径)
      double mnum = GRS80_A * (1 - GRS80_E2);
      double m = mnum / (w * w * w);

      //ヒュベニの公式
      double dym = dy * m;
      double dxncos = dx * n * Math.cos(my);
      double dis = Math.sqrt(dym * dym + dxncos * dxncos) / 1000;
      Logger.info("[getDistance] distance={} rradius={}", dis, pRadius);
      return dis < pRadius ? true : false;
    }
 }
