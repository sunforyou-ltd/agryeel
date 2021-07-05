package models;

import java.util.List;

import javax.persistence.Entity;

import com.avaje.ebean.Expr;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】作業情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Work extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -8025683306889050063L;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * 作業名
     */
    public String workName;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * テンプレートID
     */
    public double workTemplateId;
    /**
     * 項目1
     */
    public String item1;
    /**
     * 項目2
     */
    public String item2;
    /**
     * 項目3
     */
    public String item3;
    /**
     * 項目4
     */
    public String item4;
    /**
     * 項目5
     */
    public String item5;
    /**
     * 項目6
     */
    public String item6;
    /**
     * 項目7
     */
    public String item7;
    /**
     * 項目8
     */
    public String item8;
    /**
     * 項目9
     */
    public String item9;
    /**
     * 項目10
     */
    public String item10;
    /**
     * 数値項目1
     */
    public String numericItem1;
    /**
     * 数値項目2
     */
    public String numericItem2;
    /**
     * 数値項目3
     */
    public String numericItem3;
    /**
     * 数値項目4
     */
    public String numericItem4;
    /**
     * 数値項目5
     */
    public String numericItem5;
    /**
     * 数値項目6
     */
    public String numericItem6;
    /**
     * 数値項目7
     */
    public String numericItem7;
    /**
     * 数値項目8
     */
    public String numericItem8;
    /**
     * 数値項目9
     */
    public String numericItem9;
    /**
     * 数値項目10
     */
    public String numericItem10;
    /**
     * 作業画像
     */
    public byte[] workPicture;
    /**
     * 作業カラー
     */
    public String workColor;
    /**
     * 作業英字名
     */
    public String workEnglish;
    /**
     * 警告％
     */
    public double worningPer;
    /**
     * 注意％
     */
    public double notePer;
    /**
     * 危険％
     */
    public double dangerPer;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, Work> find = new Finder<Long, Work>(Long.class, Work.class);

    /**
     * 対象生産者の作業情報を取得する
     * @param farmId
     * @return
     */
    public static List<Work> getWorkOfFarm(double farmId) {

    	List<Work> aryWork = Work.find.where().eq("farm_id", farmId).orderBy("work_id").findList();

    	return aryWork;

    }
    /**
     * 対象生産者の作業情報を取得する
     * @param farmId
     * @return
     */
    public static List<Work> getWorkOfBaseFarm(double farmId) {

      List<Work> aryWork = Work.find.where().disjunction().add(Expr.eq("farm_id", farmId)).add(Expr.eq("farm_id", 0)).endJunction().orderBy("work_id").findList();

      return aryWork;

    }

    /**
     * 作業情報を取得します
     * @param pWorkId
     * @return
     */
    public static Work getWork(double pWorkId) {
      return Work.find.where().eq("work_id", pWorkId).findUnique();
    }

}
