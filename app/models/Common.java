package models;

import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】共通情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Common extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1385268899710079248L;
    /**
     * 共通種別
     */
    public int commonClass;
    /**
     * 共通枝番
     */
    public int commonSeq;
    /**
     * 共通名称
     */
    public String commonName;

    public static Finder<Long, Common> find = new Finder<Long, Common>(Long.class, Common.class);

    /**
     * 指定された共通種別の一覧表を取得する
     * @return
     */
    public static List<Common> GetCommonList(int commonClass) {

        List<Common> commonDataList = find.where().eq("common_class", commonClass).orderBy("common_seq").findList();

        return commonDataList;

    }

    /**
     * 指定された共通項目の値を取得する
     * @return
     */
    public static String GetCommonValue(int commonClass, int commonSeq) {

        return GetCommonValue(commonClass, commonSeq, false);

    }
    /**
     * 指定された共通項目の値を取得する
     * @return
     */
    public static String GetCommonValue(int commonClass, int commonSeq, boolean noselect) {

        String sResult    = "";

        Common commonData   = find.where().eq("common_class", commonClass).eq("common_seq", commonSeq).findUnique();

        if (commonData != null) {

            sResult   = commonData.commonName;

        }
        else {
          if (noselect) {
            sResult   = "未選択";
          }
        }

        return sResult;

    }

    /**
     * 共通種別
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class ConstClass {
        /**
         * 役割分担
         */
        public static final int  	  PART 				= 1;
        /**
         * 所有資格
         */
        public static final int  	  CAPACITY			= 2;
        /**
         * 作業モード
         */
        public static final int  	  WORKMODE			= 3;
        /**
         * 権限
         */
        public static final int  	  ROLE 				= 4;
        /**
         * 初期表示ページ
         */
        public static final int  	  FIRSTPAGE			= 5;
        /**
         * シーケンスID
         */
        public static final int  	  SEQUANCEID		= 6;
        /**
         * 農肥種別
         */
        public static final int  	  NOUHIKIND			= 7;
        /**
         * コンビネーション種別
         */
        public static final int  	  COMBIKIND			= 8;
        /**
         * 散布方法
         */
        public static final int  	  SANPUMETHOD		= 9;
        /**
         * 潅水方法
         */
        public static final int  	  KANSUI			= 10;
        /**
         * 荷姿
         */
        public static final int  	  NISUGATA			= 11;
        /**
         * 質
         */
        public static final int  	  SHITU				= 12;
        /**
         * サイズ
         */
        public static final int  	  SIZE				= 13;
        /**
         * 単位
         */
        public static final int  	  UNIT				= 14;
        /**
         * 機器種別
         */
        public static final int  	  KIKI				= 15;
        /**
         * アタッチメント
         */
        public static final int  	  ATTACHMENT		= 16;
        /**
         * 作業テンプレート
         */
        public static final int  	  WORKTEMPLATE		= 17;
        /**
         * 口座種別
         */
        public static final int  	  ACCOUNTTYPE		= 18;
        /**
         * 地目
         */
        public static final int  	  GEOGRAPHY		= 19;
        /**
         * 土質
         */
        public static final int  	  SOILQUALITY		= 20;
        /**
         * 表示条件
         */
        public static final int     DISPLAYWORKING   = 23;
        /**
         * 資材(伊藤園芸)
         */
        public static final int     ITOSIZAI   = 25;
        /**
         * 資材(マルチ)
         */
        public static final int     ITOMULTI   = 26;
        /**
         * 資材(培土)
         */
        public static final int     ITOBAIDO   = 27;
        /**
         * 区画種別
         */
        public static final int     KUKAKUKIND   = 29;
        /**
         * 状況照会
         */
        public static final int     DISPLAYSTAUS   = 32;
        /**
         * 作業対象表示
         */
        public static final int     WORKTARGETDISPLAY   = 33;
        /**
         * 作業記録後
         */
        public static final int     WORKCOMMITAFTER   = 34;
        /**
         * メッセージ種別
         */
        public static final int     MESSAGEKIND   = 35;
        /**
         * ワークチェーン表示
         */
        public static final int     DISPLAYCHAIN   = 36;
        /**
         * 荷姿履歴値参照
         */
        public static final int     NISUGATARIREKI   = 37;
        /**
         * 区画状況照会SKIP
         */
        public static final int     COMPARTMENTSTATUSSKIP   = 38;
        /**
         * 作業日付自動設定
         */
        public static final int     WORKDATESUTOSET   = 39;
        /**
         * 作業計画フラグ
         */
        public static final int     WORKPLANFLAG   = 40;
        /**
         * 作業開始確認
         */
        public static final int     WORKSTARTPROMPT   = 41;
        /**
         * 作業切替表示
         */
        public static final int     WORKCHANGEDISPLAY = 42;
        /**
         * 付近区画距離
         */
        public static final int     RADIUS = 43;
        /**
         * 作業指示初期担当者
         */
        public static final int     WORKPLANINITID = 44;
        /**
         * 面積単位
         */
        public static final int     AREAUNIT = 45;
        /**
         * 期初
         */
        public static final int     KISYO = 46;
        /**
         * 農肥チェック
         */
        public static final int     NOUHICHECK = 47;
        /**
         * 作業指示自動移動
         */
        public static final int     WORKPLANAUTOMOVE = 48;
        /**
         * 契約プラン
         */
        public static final int     CONTRACTPLAN = 49;
        /**
         * データ使用許可
         */
        public static final int     DATAUSEPERMISSION = 50;
        /**
         * 年度判定
         */
        public static final int     NENDOJUDGE = 51;
        /**
         * システム種別
         */
        public static final int     SYSTEMKIND = 52;
        /**
         * 履歴参照
         */
        public static final int     HISTORYREFERENCE = 53;
        /**
         * 作業指示自動生成
         */
        public static final int     WORKPLANAUTOCREATE = 54;
        /**
         * 作業指示自動生成
         */
        public static final int     WORKDIARYDISCRIPTION = 55;
        /**
         * 収穫入力人数
         */
        public static final int     SYUKAKUINPUTCOUNT = 57;
        /**
         * 育苗機能
         */
        public static final int     IKUBYOFUNCTION = 63;
    }
}
