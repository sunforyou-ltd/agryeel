package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】区画ワークチェイン状況情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CompartmentWorkChainStatus extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -5301504577969972039L;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * ワークチェインID
     */
    public double workChainId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 作業完了ID
     */
    public String workEndId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, CompartmentWorkChainStatus> find = new Finder<Long, CompartmentWorkChainStatus>(Long.class, CompartmentWorkChainStatus.class);

    public CompartmentStatus getCompartmentStatus() {
      return CompartmentStatus.find.where().eq("kukaku_id", this.kukakuId).findUnique();
    }
    /**
     * 区画IDから区画ワークチェイン状況を取得する
     * @param pKukakuId
     * @return
     */
    public static CompartmentWorkChainStatus getCompartmentWorkChainStatusFromKEY(double pKukakuId) {
      return CompartmentWorkChainStatus.find.where().eq("kukaku_id", pKukakuId).findUnique();
    }

}
