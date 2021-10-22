package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】育苗記録詳細情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class IkubyoDiaryDetail extends Model {

    /**
     * 育苗記録ID
     */
    public double ikubyoDiaryId;
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

    public static Finder<Long, IkubyoDiaryDetail> find = new Finder<Long, IkubyoDiaryDetail>(Long.class, IkubyoDiaryDetail.class);

    public static List<IkubyoDiaryDetail> getIkubyoDiaryDetailList(double pIkubyoDiaryId) {
<<<<<<< HEAD
      return IkubyoDiaryDetail.find.where().eq("ikubyo_diary_id", pIkubyoDiaryId).order("ikubyo_diary_sequence asc").findList();
=======
      return IkubyoDiaryDetail.find.where().eq("ikubyo_plan_id", pIkubyoDiaryId).order("ikubyo_diary_sequence asc").findList();
>>>>>>> e747d9c9b47c3d59e92c6b368bbb246cc6564120
    }

}
