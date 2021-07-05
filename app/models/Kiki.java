package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】機器情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Kiki extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -6489431654729388392L;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * 機器名
     */
    public String kikiName;
    /**
     * 型式
     */
    public String katasiki;
    /**
     * メーカー
     */
    public String maker;
    /**
     * 機器種別
     */
    public int kikiKind;
    /**
     * 使用可能アタッチメントID
     */
    public String onUseAttachmentId;
    /**
     * 金額
     */
    public double kingaku;
    /**
     * 購入日
     */
    public Date purchaseDate;
    /**
     * 日数
     */
    public long days;
    /**
     * 耐用年数
     */
    public long serviceLife;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Kiki> find = new Finder<Long, Kiki>(Long.class, Kiki.class);

    public static String getKikiName(double kikiId) {

    	String result = "未選択";

    	Kiki kiki = Kiki.find.where().eq("kiki_id", kikiId).findUnique();

    	if ( kiki != null ) {

    		result = kiki.kikiName;

    	}

    	return result;
    }

    /**
     * 機器情報を取得します
     * @param pKikiId
     * @return
     */
    public static Kiki getKikiInfo(double pKikiId) {
    	return Kiki.find.where().eq("kiki_id", pKikiId).findUnique();
    }
}
