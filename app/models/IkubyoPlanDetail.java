package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】育苗計画詳細情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class IkubyoPlanDetail extends Model {

    /**
     * 育苗計画ID
     */
    public double ikubyoPlanId;
    /**
     * 育苗記録シーケンス
     */
    public double ikubyoDiarySequence;
    /**
     * 作業詳細種別
     */
    public int workDetailKind;
    /**
     * 苗No
     */
    public String naeNo;

    public static Finder<Long, IkubyoPlanDetail> find = new Finder<Long, IkubyoPlanDetail>(Long.class, IkubyoPlanDetail.class);

    public static List<IkubyoPlanDetail> getIkubyoPlanDetailList(double pIkubyoPlanId) {
      return IkubyoPlanDetail.find.where().eq("ikubyo_plan_id", pIkubyoPlanId).order("ikubyo_diary_sequence asc").findList();
    }

}
