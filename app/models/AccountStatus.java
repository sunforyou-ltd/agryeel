package models;

import java.sql.Timestamp;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】アカウント検索情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class AccountStatus extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -7795508570912745485L;
    /**
     * アカウントID
     */
    public String accountId;
    /**
     * 選択チェインId
     */
    public double selectChainId;
    /**
     * 選択圃場グループId
     */
    public String selectFieldGroupId;
    /**
     * 選択開始日付
     */
    public Timestamp selectStartDate;
    /**
     * 選択終了日付
     */
    public Timestamp selectEndDate;
    /**
     * 選択作業Id
     */
    public String selectWorkId;
    /**
     * 選択担当者Id
     */
    public String selectAccountId;
    /**
     * 選択作業中
     */
    public int selectWorking;
    /**
     * 区画検索区画名
     */
    public String sskKukakuName;
    /**
     * 区画検索複数区画
     */
    public String sskMultiKukaku;
    /**
     * 区画検索品種
     */
    public String sskHinsyu;
    /**
     * 区画検索生育日数自
     */
    public int sskSeiikuF;
    /**
     * 区画検索生育日数至
     */
    public int sskSeiikuT;
    /**
     * 状況照会
     */
    public int displayStatus;
    /**
     * 選択区画ID
     */
    public String selectKukakuId;
    /**
     * 作業対象
     */
    public int workTargetDisplay;
    /**
     * 作業記録後
     */
    public int workCommitAfter;
    /**
     * メッセージ種別
     */
    public int messageKind;

    /**
     * ワークチェーン表示
     */
    public int displayChain;
    /**
     * 荷姿履歴値参照
     */
    public int nisugataRireki;
    /**
     * 区画状況照会SKIP
     */
    public int compartmentStatusSkip;
    /**
     * 選択生産物ID
     */
    public String selectCropId;
    /**
     * 選択品種ID
     */
    public String selectHinsyuId;
    /**
     * 作業日付自動設定
     */
    public int workDateAutoSet;
    /**
     * 作業開始確認
     */
    public int workStartPrompt;
    /**
     * 作業切替表示
     */
    public int workChangeDisplay;
    /**
     * 付近区画距離
     */
    public int radius;
    /**
     * 作業指示初期担当者
     */
    public int workPlanInitId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;
    /**
     * 作業記録注釈
     */
    public int workDiaryDiscription;
    /**
     * 作業指示選択作業
     */
    public String workPlanWorkId;
    /**
     * 作業指示選択区画
     */
    public String workPlanKukakuId;
    /**
     * 作業指示選択担当者
     */
    public String workPlanAccountId;

    /**
     * センスプラウト比較元区画ID
     */
    public double ssBaseKukakuId;
    /**
     * センスプラウト比較元日付自
     */
    public String ssBaseFrom;
    /**
     * センスプラウト比較元日付至
     */
    public String ssBaseTo;
    /**
     * センスプラウト比較対象区画ID
     */
    public double ssDataKukakuId;
    /**
     * センスプラウト比較対象日付自
     */
    public String ssDataFrom;
    /**
     * センスプラウト比較対象日付至
     */
    public String ssDataTo;

    public static Finder<Long, AccountStatus> find = new Finder<Long, AccountStatus>(Long.class, AccountStatus.class);
}
