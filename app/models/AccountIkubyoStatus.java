package models;

import java.sql.Timestamp;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】アカウント育苗検索情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class AccountIkubyoStatus extends Model {

    /**
     * アカウントID
     */
    public String accountId;
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
     * 選択生産物Id
     */
    public String selectCropId;
    /**
     * 選択品種Id
     */
    public String selectHinsyuId;
    /**
     * 選択作業中
     */
    public int selectWorking;
    /**
     * 苗検索生産物
     */
    public String ssnCrop;
    /**
     * 苗検索品種
     */
    public String ssnHinsyu;
    /**
     * 区画検索生育日数自
     */
    public int ssnSeiikuF;
    /**
     * 区画検索生育日数至
     */
    public int ssnSeiikuT;
    /**
     * 削除フラグ
     */
    public short deleteFlag;

    public static Finder<Long, AccountIkubyoStatus> find = new Finder<Long, AccountIkubyoStatus>(Long.class, AccountIkubyoStatus.class);
}
