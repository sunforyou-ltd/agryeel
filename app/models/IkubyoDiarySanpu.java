package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】育苗記録散布情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class IkubyoDiarySanpu extends Model {

    /**
     * 育苗記録ID
     */
    public double ikubyoDiaryId;
    /**
     * 育苗記録シーケンス
     */
    public double ikubyoDiarySequence;
    /**
     * 散布方法
     */
    public int sanpuMethod;
    /**
     * 機器ID
     */
    public double kikiId;
    /**
     * アタッチメントID
     */
    public double attachmentId;
    /**
     * 農肥ID
     */
    public double nouhiId;
    /**
     * 倍率
     */
    public double bairitu;
    /**
     * 散布量
     */
    public double sanpuryo;
    /**
     * 苗状況照会反映フラグ
     */
    public int naeStatusUpdate;
    /**
     * 有効成分
     */
    public String yukoSeibun;

    public static Finder<Long, IkubyoDiarySanpu> find = new Finder<Long, IkubyoDiarySanpu>(Long.class, IkubyoDiarySanpu.class);

    public static List<IkubyoDiarySanpu> getIkubyoDiarySanpuList(double pIkubyoDiaryId) {
      return IkubyoDiarySanpu.find.where().eq("ikubyo_diary_id", pIkubyoDiaryId).order("ikubyo_diary_sequence asc").findList();
    }

}
