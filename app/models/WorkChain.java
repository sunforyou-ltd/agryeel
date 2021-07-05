package models;

import java.util.List;

import javax.persistence.Entity;

import com.avaje.ebean.Expr;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】ワークチェイン情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkChain extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 4697991925139297971L;
    /**
     * ワークチェインID
     */
    public double workChainId;
    /**
     * ワークチェイン名
     */
    public String workChainName;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, WorkChain> find = new Finder<Long, WorkChain>(Long.class, WorkChain.class);

    /**
     * 対象生産者のワークチェイン情報を取得する
     * @param farmId
     * @return
     */
    public static List<WorkChain> getWorkChainOfFarm(double farmId) {

    	List<WorkChain> aryWorkChain = WorkChain.find.where().disjunction().add(Expr.eq("farm_id", 0)).add(Expr.eq("farm_id", farmId)).orderBy("work_chain_id").findList();

    	return aryWorkChain;

    }

    /**
     * ワークチェーン情報を取得する
     * @param pWorkChainId
     * @return
     */
    public static WorkChain getWorkChain(double pWorkChainId) {
      return WorkChain.find.where().eq("work_chain_id", pWorkChainId).findUnique();
    }

}
