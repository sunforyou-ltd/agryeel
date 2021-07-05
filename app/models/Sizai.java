package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】資材情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Sizai extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -3424191013494174240L;
    /**
     * 資材ID
     */
    public double sizaiId;
    /**
     * 資材名
     */
    public String sizaiName;
    /**
     * 資材種別
     */
    public long sizaiKind;
    /**
     * 単位種別
     */
    public int unitKind;
    /**
     * 量
     */
    public double ryo;
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
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Sizai> find = new Finder<Long, Sizai>(Long.class, Sizai.class);

    public static String getSizaiName(double sizaiId) {

    	String result = "未選択";

    	Sizai sizai = Sizai.find.where().eq("sizai_id", sizaiId).findUnique();

    	if ( sizai != null ) {

    		result = sizai.sizaiName;

    	}

    	return result;

    }

    /**
     * 資材情報を取得します
     * @param pSizaiId
     * @return
     */
    public static Sizai getSizaiInfo(double pSizaiId) {
    	return Sizai.find.where().eq("sizai_id", pSizaiId).findUnique();
    }

    /**
     * 資材種別
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class SizaiKindClass {
        /**
         * マルチ
         */
        public static final int  	  MULTI				= 1;
        /**
         * 資材
         */
        public static final int  	  SIZAI				= 2;
        /**
         * 培土
         */
        public static final int  	  BAIDO				= 3;
        /**
         * ビニール
         */
        public static final int  	  VINYL				= 4;
        /**
         * 土壌消毒用ビニール
         */
        public static final int  	  DOJYOVINYL		= 5;
        /**
         * セルトレイ
         */
        public static final int  	  CELLTRAY			= 6;
        /**
         * 苗箱
         */
        public static final int  	  SEEDLINGBOX		= 7;
        /**
         * ペーパーポット
         */
        public static final int  	  PAPERPOT			= 8;
        /**
         * ポット
         */
        public static final int  	  POT				= 9;
        /**
         * 潅水チューブ
         */
        public static final int  	  KANSUITUBE		= 10;
        /**
         * トンネル上ヘゴ
         */
        public static final int  	  TUNNELTOPHEGO		= 11;
        /**
         * トンネル下ヘゴ
         */
        public static final int  	  TUNNELUNDERHEGO	= 12;
        /**
         * トンネルビニール
         */
        public static final int  	  TUNNELVINYL		= 13;
        /**
         * ロータリー爪
         */
        public static final int  	  ROTARYCLAW		= 14;
        /**
         * エンジンオイル
         */
        public static final int  	  ENGINEOIL			= 15;
        /**
         * 農具
         */
        public static final int  	  FARMTOOLS			= 16;
        /**
         * 農具消耗品
         */
        public static final int  	  FARMTOOLSCONSUMABLES	= 17;
    }
}
