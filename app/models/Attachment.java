package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】アタッチメント情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Attachment extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 3116735804541197337L;
    /**
     * アタッチメントID
     */
    public double attachmentId;
    /**
     * アタッチメント名
     */
    public String attachementName;
    /**
     * 型式
     */
    public String katasiki;
    /**
     * アタッチメント種別
     */
    public int attachmentKind;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Attachment> find = new Finder<Long, Attachment>(Long.class, Attachment.class);

    /**
     * アタッチメント種別
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class ConstAttachmentKind {
        /**
         * ロータリー
         */
        public static final int  	  ROTARY 			= 1;
        /**
         * 肥料散布アタッチメント
         */
        public static final int  	  HIRYOUSANPU		= 2;
        /**
         * トラクターマルチャー
         */
        public static final int		  TRACTORMULCHER	= 3;
        /**
         * 管理機マルチャー
         */
        public static final int		  KANRIKIMULCHER	= 4;
        /**
         * ロータリーマルチャー
         */
        public static final int		  ROTARYMULCHER		= 5;
        /**
         * 土壌消毒機械
         */
        public static final int		  DOJYOSYOUDOKU		= 6;
        /**
         * 播種アタッチメント
         */
        public static final int		  HASYU				= 7;
        /**
         * 粒剤散布アタッチメント
         */
        public static final int		  RYUZAISANPU		= 8;
        /**
         * 溝切アタッチメント
         */
        public static final int		  GROOVECUTT		= 9;
    }

    /**
     * アタッチメント名を取得する
     * @param attachmentId
     * @return
     */
    public static String getAttachmentName(double attachmentId) {

    	String result = "未選択";

    	Attachment attachment = Attachment.find.where().eq("attachment_id", attachmentId).findUnique();

    	if ( attachment != null ) {

    		result = attachment.attachementName;

    	}

    	return result;

    }

    /**
     * アタッチメント情報を取得します
     * @param pBeltoId
     * @return
     */
    public static Attachment getAttachmentInfo(double pAttachmentId) {
    	return Attachment.find.where().eq("attachment_id", pAttachmentId).findUnique();
    }
}
