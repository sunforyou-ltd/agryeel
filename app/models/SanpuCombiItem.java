package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】散布組合せ明細情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SanpuCombiItem extends Model {

    /**
   *
   */
  private static final long serialVersionUID = -496898984421198990L;
    /**
     * 散布組合せ明細ID
     */
    public double sanpuCombiId;
    /**
     * シーケンスID
     */
    public int sequenceId;
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
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, SanpuCombiItem> find = new Finder<Long, SanpuCombiItem>(Long.class, SanpuCombiItem.class);

}
