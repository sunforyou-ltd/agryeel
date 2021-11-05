package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】育苗計画散布情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class IkubyoPlanSanpu extends Model {

    /**
     * 育苗計画ID
     */
    public double ikubyoPlanId;
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

    public static Finder<Long, IkubyoPlanSanpu> find = new Finder<Long, IkubyoPlanSanpu>(Long.class, IkubyoPlanSanpu.class);

    public static List<IkubyoPlanSanpu> getIkubyoPlanSanpuList(double pIkubyoPlanId) {
      return IkubyoPlanSanpu.find.where().eq("ikubyo_plan_id", pIkubyoPlanId).order("ikubyo_diary_sequence asc").findList();
    }

}
