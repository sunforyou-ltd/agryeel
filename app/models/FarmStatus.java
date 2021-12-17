package models;

import java.sql.Date;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】生産者ステータス情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class FarmStatus extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 7727863951051846480L;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 面積単位
     */
    public int areaUnit;
    /**
     * 期初
     */
    public int kisyo;
    /**
     * 農肥チェック
     */
    public int nouhiCheck;
    /**
     * 作業指示自動移動
     */
    public int workPlanAutoMove;
    /**
     * 契約プラン
     */
    public int contractPlan;
    /**
     * データ使用許可
     */
    public int dataUsePermission;
    /**
     * 年度判定
     */
    public int nendoJudge;
    /**
     * 一日作業時間
     */
    public int workTimeOfDay;
    /**
     * 区画収穫担当人数
     */
    public int syukakuTantoNinzu;
    /**
     * 履歴参照
     */
    public int historyReference;
    /**
     * 収穫入力数
     */
    public int syukakuInputCount;
    /**
     * 支払区分
     */
    public int paymentKubun;
    /**
     * 最終回収日
     */
    public Date lastCollectionDate;
    /**
     * 次回回収日
     */
    public Date nextCollectionDate;
    /**
     * 育苗機能
     */
    public int ikubyoFunction;
    /**
     * 区画選択方法
     */
    public int kukakuSelectMethod;

    public static Finder<Long, FarmStatus> find = new Finder<Long, FarmStatus>(Long.class, FarmStatus.class);

    public static FarmStatus getFarmStatus(double farmId) {
      return FarmStatus.find.where().eq("farm_id", farmId).findUnique();
    }
}
