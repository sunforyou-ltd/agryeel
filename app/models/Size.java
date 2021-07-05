package models;

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
}
