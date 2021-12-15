package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】ワークチェイン明細情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkChainItem extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 3115095484556826465L;
    /**
     * ワークチェインID
     */
    public double workChainId;
    /**
     * シーケンスID
     */
    public int sequenceId;
    /**
     * 作業ID
     */
    public double workId;
    /**
     * 散布組合せID
     */
    public double sanpuCombiId;
    /**
     * 使用可能機器種別
     */
    public String onUseKikiKind;
    /**
     * 作業モード
     */
    public int workMode;
    /**
     * 次のシーケンスID
     */
    public int nextSequenceId;
    /**
     * 農肥種別
     */
    public int nouhiKind;
    /**
     * 削除フラグ
     */
    public short deleteFlag;
    /**
     * 作付開始連携フラグ
     */
    public int autoStartFlag;

    public static Finder<Long, WorkChainItem> find = new Finder<Long, WorkChainItem>(Long.class, WorkChainItem.class);

    public static List<WorkChainItem> getWorkChainItemList(double pWorkChainId) {
      return WorkChainItem.find.where().eq("work_chain_id", pWorkChainId).order("sequence_id").findList();
    }
    public static List<WorkChainItem> getWorkChainItemList(double pWorkChainId, String pOrderBy) {
      return WorkChainItem.find.where().eq("work_chain_id", pWorkChainId).orderBy(pOrderBy).findList();
    }

    public static WorkChainItem getWorkChainItemOfSeq(double pWorkChainId, int pSequenceId) {
      return WorkChainItem.find.where().eq("work_chain_id", pWorkChainId).eq("sequence_id", pSequenceId).findUnique();
    }
    public static WorkChainItem getWorkChainItemOfWorkId(double pWorkChainId, double pWorkId) {
      return WorkChainItem.find.where().eq("work_chain_id", pWorkChainId).eq("work_id", pWorkId).findUnique();
    }

}
