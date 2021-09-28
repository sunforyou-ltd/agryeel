package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】サイズ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Size extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 8403132429091060237L;
    /**
     * サイズID
     */
    public double sizeId;
    /**
     * サイズ名
     */
    public String sizeName;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Size> find = new Finder<Long, Size>(Long.class, Size.class);

    public static String getSizeName(double sizeId) {

    	String result = "未選択";

    	Size size = Size.find.where().eq("size_id", sizeId).findUnique();

    	if ( size != null ) {

    		result = size.sizeName;

    	}

    	return result;

    }

    /**
     * サイズ情報を取得します
     * @param pSizeId
     * @return
     */
    public static Size getSizeInfo(double pSizeId) {
    	return Size.find.where().eq("size_id", pSizeId).findUnique();
    }
    /**
     * 名称からＩＤに変換する
     * @param pName
     * @param pFarmId
     * @return
     */
    public static double convertId(String pName, double pFarmId) {
      double result = 0;

      List<SizeOfFarm> sofs = SizeOfFarm.find.where().eq("farm_id", pFarmId).orderBy("size_id").findList();
      List<Double> keys = new ArrayList<Double>();

      for (SizeOfFarm sof: sofs) {
        keys.add(new Double(sof.sizeId));
      }

      List<Size> sizes = Size.find.where()
                             .eq("size_name", pName)
                             .in("size_id", keys)
                                     .findList();
      if (sizes.size() == 1) { //該当レコードが１件のみ
        result = sizes.get(0).sizeId;
      }

      return result;
    }
}
