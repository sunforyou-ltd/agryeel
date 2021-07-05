package models;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】システム管理情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SystemManage extends Model {

    /**
     * システム種別
     */
    public int systemKind;
    /**
     * メジャーバージョン
     */
    public int majorVersion;
    /**
     * マイナーバージョン
     */
    public int minorVersion;
    /**
     * パッチバージョン
     */
    public int patchVersion;
    /**
     * バージョンUUID
     */
    public String vuuid;
    /**
     * 強制アップデート
     */
    public int compUpdate;

    public static Finder<Long, SystemManage> find = new Finder<Long, SystemManage>(Long.class, SystemManage.class);

    public static SystemManage getSystemManage(int pSystemKind) {
      return SystemManage.find.where().eq("system_kind", pSystemKind).findUnique();
    }

}
