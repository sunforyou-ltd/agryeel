package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】散布組合せ情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SanpuCombi extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 2521328221427270679L;
    /**
     * 散布組合せID
     */
    public double sanpuCombiId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, SanpuCombi> find = new Finder<Long, SanpuCombi>(Long.class, SanpuCombi.class);

    /**
     * 対象生産者の散布組合せ情報を取得する
     * @param farmId
     * @return
     */
    public static List<SanpuCombi> getAccountOfFarm(double farmId) {

    	List<SanpuCombi> arySanpuCombi = SanpuCombi.find.where().eq("farm_id", farmId).orderBy("sanpu_combi_id desc").findList();

    	return arySanpuCombi;

    }

}
