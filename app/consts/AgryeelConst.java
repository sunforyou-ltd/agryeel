package consts;

/**
 * 【AGRYEEL】共通固定変数定義
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class AgryeelConst {
    /**
     * セッションキー
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class SessionKey {
        /**
         * アカウントID
         */
        public static String  ACCOUNTID 		= "accountId";
        /**
         * アカウント名
         */
        public static String  ACCOUNTNAME 		= "accountName";
        /**
         * アカウントID（アカウント情報選択用）
         */
        public static String  ACCOUNTID_SEL		= "accountId_sel";
        /**
         * 農場ID
         */
        public static String  FARMID 			= "farmId";
        /**
         * 農場グループID
         */
        public static String  FARMGROUPID		= "farmGroupId";
        /**
         * 農場基本ID
         */
        public static String  FARMBASEID		= "farmBaseId";
        /**
         * 圃場グループID
         */
        public static String  FIELDGROUPID		= "fieldGroupId";
        /**
         * 圃場ID
         */
        public static String  FIELDID			= "fieldId";
        /**
         * 区間ID（入力圃場）
         */
        public static String  KUKAKUID			= "kukakuId";
        /**
         * 地主ID
         */
        public static String  LANDLORDID		= "landlordId";
        /**
         * 生産物グループID
         */
        public static String  CROPGROUP			= "cropGroupId";
        /**
         * 生産物ID
         */
        public static String  CROPID			= "cropId";
        /**
         * 農肥ID
         */
        public static String  NOUHIID			= "nouhiId";
        /**
         * 品種ID
         */
        public static String  HINSYUID			= "hinsyuId";
        /**
         * ベルトID
         */
        public static String  BELTOID			= "beltoId";
        /**
         * 機器ID
         */
        public static String  KIKIID			= "kikiId";
        /**
         * アタッチメントID
         */
        public static String  ATTACHMENTID		= "attachmentId";
        /**
         * 荷姿ID
         */
        public static String  NISUGATAID		= "nisugataId";
        /**
         * 質ID
         */
        public static String  SHITUID			= "shituId";
        /**
         * サイズID
         */
        public static String  SIZEID			= "sizeId";
        /**
         * 資材ID
         */
        public static String  SIZAIID			= "sizaiId";
        /**
         * 容器ID
         */
        public static String  YOUKIID			= "youkiId";
        /**
         * 土ID
         */
        public static String  SOILID			= "soilId";
        /**
         * 作業ID（作業記録日誌）
         */
        public static String  WORKID			= "workId";
        /**
         * 作業記録ＩＤ（作業記録日誌）
         */
        public static String  WORKDIARYID		= "workDiaryId";
        /**
         * 作業計画ＩＤ（作業記録日誌）
         */
        public static String  WORKPLANID   = "workPlanId";
        /**
         * 育苗記録ＩＤ（育苗記録日誌）
         */
        public static String  IKUBYODIARYID		= "ikubyoDiaryId";
        /**
         * 育苗計画ＩＤ（育苗記録日誌）
         */
        public static String  IKUBYOPLANID		= "ikubyoPlanId";
        /**
         * 苗No
         */
        public static String  NAENO				= "naeNo";
        /**
         * マスタメンテナンス画面ID
         */
        public static String  MSTGMNID			= "mstGmnId";
        /**
         * 作業中(作業ID)
         */
        public static String  WORKING_WORKID = "workingworkid";
        /**
         * 作業中(区画ID)
         */
        public static String  WORKING_KUKAKUID = "workingkukakuid";
        /**
         * 作業中(アクション)
         */
        public static String  WORKING_ACTION = "workingaction";
        /**
         * 作業中(計画ID)
         */
        public static String  WORKING_PLANID = "workingaplanid";
        /**
         * 作業記録BACK(遷移先)
         */
        public static String  BACK_MODE = "backmode";
        /**
         * 作業記録BACK(アカウントID)
         */
        public static String  BACK_ACCOUNT = "backaccountid";
        /**
         * 作業記録BACK(選択日付)
         */
        public static String  BACK_DATE = "backdate";
        /**
         * API
         */
        public static String  API = "api";
    }
    /**
     * 契約プラン情報
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class ContractPlanInfo {
        /**
         * Free
         */
        public static final int  	  FREE	 			= 0;
        /**
         * Light
         */
        public static final int  	  LIGHT				= 1;
        /**
         * LightStandard
         */
        public static final int  	  LIGHTSTANDARD		= 2;
        /**
         * Standard
         */
        public static final int  	  STANDARD			= 3;
        /**
         * LightPro
         */
        public static final int  	  LIGHTPRO			= 4;
        /**
         * Pro
         */
        public static final int  	  PRO				= 5;
    }
    /**
     * 農場情報
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class FarmInfo {
        /**
         * 農場
         */
        public static String  FARMLIST	= "farmList";
    }
    /**
     * 区画状況情報
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class KukakuInfo {
        /**
         * 該当区画グループ状況
         */
        public static String  TARGETCOMPARTMENTGROUP	= "targetCompartmentGroup";
        /**
         * 該当区画状況
         */
        public static String  TARGETCOMPARTMENTSTATUS	= "targetCompartmentStatus";
        /**
         * 該当区画詳細
         */
        public static String  TARGETCOMPARTMENTDISPKAY	= "targetCompartmentDisplay";
    }
    /**
     * 圃場グループタイプ
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class HojoGroupType {
        /**
         * アルファベット
         */
        public static final int  ALPHABET	= 0;
        /**
         * ひらがな
         */
        public static final int  HIRAGANA	= 1;
        /**
         * カタカナ
         */
        public static final int  KANA		= 2;
    }
    /**
     * カラーデータ
     */
    public static String[] ColorData = {
    	"D32F2F","C2185B","7B1FA2","512DA8","303F9F"
       ,"1976D2","0288D1","0097A7","00796B","388E3C"
       ,"689F38","AFB42B","FBC02D","FFA000","F57C00"
       ,"EF5350","EC407A","AB47BC","7E57C2","5C6BC0"
       ,"42A5F5","29B6F6","26C6DA","26A69A","66BB6A"
       ,"9CCC65"
    };
    /**
     * アルファベットデータ
     */
    public static String[] AlphabetData = {
    	"Ａ","Ｂ","Ｃ","Ｄ","Ｅ"
       ,"Ｆ","Ｇ","Ｈ","Ｉ","Ｊ"
       ,"Ｋ","Ｌ","Ｍ","Ｎ","Ｏ"
       ,"Ｐ","Ｑ","Ｒ","Ｓ","Ｔ"
       ,"Ｕ","Ｖ","Ｗ","Ｘ","Ｙ"
       ,"Ｚ"
    };
    /**
     * ひらがなデータ
     */
    public static String[] HiraganaData = {
    	"あ","い","う","え","お"
       ,"か","き","く","け","こ"
       ,"さ","し","す","せ","そ"
       ,"た","ち","つ","て","と"
       ,"な","に","ぬ","ね","の"
       ,"は"
    };
    /**
     * カタカナデータ
     */
    public static String[] KatakanaData = {
    	"ア","イ","ウ","エ","オ"
       ,"カ","キ","ク","ケ","コ"
       ,"サ","シ","ス","セ","ソ"
       ,"タ","チ","ツ","テ","ト"
       ,"ナ","ニ","ヌ","ネ","ノ"
       ,"ハ"
    };
    /**
     * 作業情報
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class WorkInfo {
        /**
         * 該当作業
         */
        public static String  TARGETWORK 		= "targetWork";
        /**
         * 片付け
         */
        public static final int  	  KATADUKE 			= 1;
        /**
         * 土壌根消毒
         */
        public static final int  	  DOJOKONSYODOKU	= 2;
        /**
         * 肥料散布
         */
        public static final int  	  HIRYOSANPU		= 3;
        /**
         * 耕す
         */
        public static final int  	  TAGAYASU			= 4;
        /**
         * 土壌根消毒
         */
        public static final int  	  DOJOKONSYODOKU2	= 5;
        /**
         * 播種
         */
        public static final int  	  HASHU				= 6;
        /**
         * 除草剤散布
         */
        public static final int  	  JOSOZAISANPU		= 7;
        /**
         * 消毒
         */
        public static final int  	  SHODOKU			= 8;
        /**
         * 潅水
         */
        public static final int  	  KANSUI			= 9;
        /**
         * 追肥
         */
        public static final int  	  TUIHI				= 10;
        /**
         * 収穫
         */
        public static final int  	  SHUKAKU			= 11;
        /**
         * 定植
         */
        public static final int  	  TEISYOKU			= 12;

    }
    /**
     * 作業モード情報
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class WorkMode {
        /**
         * 作付け
         */
        public static final int  	  SAKUDUKE 			= 0;
        /**
         * 管理
         */
        public static final int  	  KANRI				= 1;
        /**
         * 収穫
         */
        public static final int  	  SYUKAKU			= 2;
    }
    /**
     * 反映フラグ
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class UpdateFlag {
        /**
         * 未更新
         */
        public static final int  	 NONE				= 0;
        /**
         * 反映済
         */
        public static final int  	 UPDATE				= 1;
    }
    /**
     * タイムライン情報
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class TimeLineInfo {
        /**
         * 該当タイムライン
         */
        public static String  TARGETTIMELINE 		= "targetTimeLine";
    }
    /**
     * アプリケーション戻り値
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class Result {
        /**
         * JSON戻り値フィールド名
         */
        public static String  RESULT 			= "result";
        /**
         * アプリケーション処理成功
         */
        public static String  SUCCESS 			= "SUCCESS";
        /**
         * 要求情報未存在エラー
         */
        public static String  NOTFOUND 			= "NOTFOUND";
        /**
         * アカウントID一致エラー
         */
        public static String  ACCOUNTIDMATCH 	= "ACCOUNTIDMATCH";
        /**
         * パスワード不一致エラー
         */
        public static String  PASSWORDUNMATCH 	= "PASSWORDUNMATCH";
        /**
         * レジストレーションコード不一致エラー
         */
        public static String  REGCODEUNMATCH 	= "REGCODEUNMATCH";
        /**
         * アカウント上限エラー
         */
        public static String  ACCOUNTLIMMIT 	= "ACCOUNTLIMMIT";
        /**
         * 例外エラー
         */
        public static String  ERROR 			= "ERROR";
        /**
         * リダイレクト
         */
        public static String  REDIRECT    = "REDIRECT";
    }

    /**
     * クリップグループ
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class ClipGroup {
        /**
         * JSON戻り値フィールド名
         */
        public static String  CLIPRESULT 		= "clipResult";
        /**
         * クリップグループなし
         */
        public static final short  NONE 		= 0;
        /**
         * クリップグループあり
         */
        public static final short  EXISTS		= 1;
        /**
         * クリップグループ番号
         */
        public static final double CLIPGROUPNO	= -99999;
    }

    /**
     * アカウント情報設定
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class AccountSetting {
        /**
         * メニュー権限ビットパターン
         *
         * @author SUN FOR YOU.Ltd
         *
         */
    	public static long menuRolePtn[] =
		{
			0x80000000, 0x40000000, 0x20000000, 0x10000000,
			0x08000000, 0x04000000, 0x02000000, 0x01000000,
			0x00800000, 0x00400000, 0x00200000, 0x00100000,
			0x00080000, 0x00040000, 0x00020000, 0x00010000,
			0x00008000, 0x00004000, 0x00002000, 0x00001000,
			0x00000800, 0x00000400, 0x00000200, 0x00000100,
			0x00000080, 0x00000040, 0x00000020, 0x00000010,
			0x00000008, 0x00000004, 0x00000002, 0x00000001
		};
        /**
         * メニュー権限ビット名称
         *
         * @author SUN FOR YOU.Ltd
         *
         */
    	public static String menuRoleNme[] =
		{
			"圃場状況照会(メイン画面)", "メンテナンス(管理者)", "メンテナンス(一般)", "作業内容修正承認",
			"ワークチェーン設定", "", "", "",
			"", "", "", "",
			"", "", "", "",
			"", "", "", "",
			"", "", "", "",
			"", "", "", "",
			"", "", "", ""
		};
        /**
         * メニュー権限定義
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class menuRole {
	        /**
	         * 圃場状況照会
	         */
	        public static int  MAIN 		= 0;
	        /**
	         * メンテナンス(管理者)
	         */
	        public static int  MNTMANAGER 	= 1;
	        /**
	         * メンテナンス(一般)
	         */
	        public static int  MNTGENERAL 	= 2;
	        /**
	         * 作業内容修正承認
	         */
	        public static int  APPROVAL 	= 3;
	        /**
	         * 作業内容修正承認
	         */
	        public static int  WORKCHAINSET = 4;
        }
        /**
         * 選択項目一覧データ
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class DataList {
            /**
             * アカウント
             */
            public static String  ACCOUNT 		= "accountDataList";
            /**
             * メニュー権限
             */
            public static String  MENUROLE	 	= "menuRoleDataList";
        }
        /**
         * フィールド
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class Field {
            /**
             * アカウントID
             */
            public static String  ACCOUNTID 	= "accountId";
        }
    }

    /**
     * 圃場グループ情報設定
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class FieldGroup {
        /**
         * 項目一覧データ
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class DataList {
            /**
             * 圃場グループ明細
             */
            public static String  GROUPLIST 		= "groupDataList";
        }
    }

    /**
     * 圃場情報設定
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class Field {
        /**
         * 項目一覧データ
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class DataList {
            /**
             * 地主一覧
             */
            public static String  LANDLORDLIST 		= "landlordDataList";
            /**
             * 区画一覧
             */
            public static String  KUKAKULIST 		= "kukakuDataList";
        }
    }

    /**
     * 生産物グループ情報設定
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class CropGroup {
        /**
         * 項目一覧データ
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class DataList {
            /**
             * 圃場グループ明細
             */
            public static String  GROUPLIST 		= "groupDataList";
        }
    }

    /**
     * 生産物情報設定
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class Crop {
        /**
         * 項目一覧データ
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class DataList {
            /**
             * 品種
             */
            public static String  HINSYU 		= "hinsyuDataList";
        }
    }

    /**
     * 作業日誌
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class WorkDiary {
        /**
         * 選択項目一覧データ
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class DataList {
            /**
             * アカウント
             */
            public static String  ACCOUNT 		= "accountDataList";
            /**
             * 作業場所（区画）
             */
            public static String  KUKAKU 		= "kukakuDataList";
            /**
             * 農肥
             */
            public static String  NOUHI 		= "nouhiDataList";
            /**
             * 生産物
             */
            public static String  CROP 			= "cropDataList";
            /**
             * 品種
             */
            public static String  HINSYU 		= "hinsyuDataList";
            /**
             * ベルト
             */
            public static String  BELTO 		= "beltoDataList";
            /**
             * 機器
             */
            public static String  KIKI 			= "kikiDataList";
            /**
             * 生産物農肥
             */
            public static String  NOUHICROP		= "nouhiCropDataList";
            /**
             * 上下限
             */
            public static String  UPDAOWN		= "updownLimitDataList";
            /**
             * アタッチメント
             */
            public static String  ATTACHMENT	= "attachmentDataList";
            /**
             * コンビネーション
             */
            public static String  COMBI			= "combiDataList";
            /**
             * 散布方法
             */
            public static String  SANPUMETHOD	= "sanpuMethodDataList";
            /**
             * 潅水方法
             */
            public static String  KANSUI		= "kansuiDataList";
            /**
             * 荷姿
             */
            public static String  NISUGATA		= "nisugataDataList";
            /**
             * 質
             */
            public static String  SHITU			= "shituDataList";
            /**
             * サイズ
             */
            public static String  SIZE			= "sizeDataList";
            /**
             * 容器
             */
            public static String  YOUKI			= "youkiDataList";
            /**
             * 土
             */
            public static String  SOIL			= "soilDataList";
        }
        /**
         * 前回履歴値
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class PrevData {
            /**
             * 農肥情報
             */
            public static String  NOUHIINFO 		= "nouhiInfo";
            /**
             * 作業履歴共通情報
             */
            public static String  WORKHISTRYBASE	= "workHistryBase";
        }
        /**
         * 詳細情報設定種別
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class DetailSettingKind {
            /**
             * 手動
             */
            public static short  MANUAL	= 0;
            /**
             * コンビネーション
             */
            public static short  COMBI	= 1;
        }
        /**
         * フィールド
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class Field {
            /**
             * アカウントID
             */
            public static String  ACCOUNTID 	= "accountId";
            /**
             * アカウント名
             */
            public static String  ACCOUNTNAME 	= "accountName";
        }
    }

    /**
     * 収量まとめ
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class SyuryoSummary {
        /**
         * 表示画面情報
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class DisplayInfo {
            /**
             * 一覧
             */
            public static final int  	  LIST	 			= 0;
            /**
             * グラフ
             */
            public static final int  	  GRAPH				= 1;
            /**
             * クリップ
             */
            public static final int  	  CLIP				= 2;
        }

        /**
         * 検索条件場所情報
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class SearchPlaceInfo {
            /**
             * 区画
             */
            public static final int  	  KUKAKU 			= 1;
            /**
             * 畝
             */
            public static final int  	  HILL				= 2;
            /**
             * 条
             */
            public static final int  	  LINE				= 3;
            /**
             * 株
             */
            public static final int  	  STOCK				= 4;
            /**
             * 担当者
             */
            public static final int  	  ACCOUNT			= 5;
        }

        /**
         * 検索条件単位情報
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class SearchUnitInfo {
            /**
             * 作付け
             */
            public static final int  	  SAKUDUKE 			= 1;
            /**
             * 年
             */
            public static final int  	  YEAR				= 2;
            /**
             * 月
             */
            public static final int  	  MONTH				= 3;
            /**
             * 日
             */
            public static final int  	  DAY				= 4;
        }

        /**
         * 収量まとめ情報
         *
         * @author SUN FOR YOU.Ltd
         *
         */
        public static class SyuryoSummaryInfo {
            /**
             * 該当収量まとめ
             */
            public static String  SYURYOSUMMARY	= "syuryoSummary";
            /**
             * 単位数
             */
            public static String  UNITCNT	= "unitCnt";
            /**
             * 作業年
             */
            public static String  WORKYEAR	= "workYear";
            /**
             * 作業月
             */
            public static String  WORKMONTH	= "workMonth";
            /**
             * 作業日
             */
            public static String  WORKDAY	= "workDay";
            /**
             * 単位最小
             */
            public static String  UNITMIN	= "unitMin";
            /**
             * 単位最大
             */
            public static String  UNITMAX	= "unitMax";
            /**
             * 末日
             */
            public static String  LASTDAY	= "lastDay";
        }
    }
    /**
     * 元帳照会
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class Motocho {
        /**
         * 元帳基本情報リスト
         */
        public static String  MOTOCHOBASE 	= "motochoBaseDataList";
        /**
         * 元帳作業年回転数リスト
         */
        public static String  MOTOCHOYEAR 	= "motochoYearDataList";
        /**
         * 初回表示KEY
         */
        public static String  INITKEY 		= "initKey";

        /**
         * 中間元帳履歴
         */
		public static final int	MOTOCHOFLAGNONE 	=	0;
		/**
		 * 初回元帳履歴
		 */
		public static final int	MOTOCHOFLAGSTART 	=	1;
		/**
		 * 最終元帳履歴
		 */
		public static final int	MOTOCHOFLAGEND 		=	2;
    }

    /**
     * マスタメンテナンス画面情報
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class MasterMntGmn {
        /**
         * マスタ情報リスト
         */
        public static String  MASTERLIST 		= "masterDataList";
        public static class GmnName {
	        /**
	         * 画面名（生産者）
	         */
	        public static String  FARM	 		= "生産者";
	        /**
	         * 画面名（アカウント）
	         */
	        public static String  ACCOUNT 		= "アカウント";
	        /**
	         * 画面名（地主）
	         */
	        public static String  LANDLORD 		= "地主";
	        /**
	         * 画面名（圃場グループ）
	         */
	        public static String  FIELDGROUP 	= "圃場グループ";
	        /**
	         * 画面名（圃場）
	         */
	        public static String  FIELD 		= "圃場";
	        /**
	         * 画面名（区画）
	         */
	        public static String  COMPARTMENT 	= "区画";
	        /**
	         * 画面名（品目グループ）
	         */
	        public static String  CROPGROUP 	= "品目グループ";
	        /**
	         * 画面名（品目）
	         */
	        public static String  CROP 			= "品目";
	        /**
	         * 画面名（農肥）
	         */
	        public static String  NOUHI			= "農肥";
	        /**
	         * 画面名（種）
	         */
	        public static String  HINSYU		= "品種";
	        /**
	         * 画面名（ベルト）
	         */
	        public static String  BELTO			= "ベルト";
	        /**
	         * 画面名（機器）
	         */
	        public static String  KIKI			= "機器";
	        /**
	         * 画面名（アタッチメント）
	         */
	        public static String  ATTACHMENT	= "アタッチメント";
	        /**
	         * 画面名（荷姿）
	         */
	        public static String  NISUGATA		= "荷姿";
	        /**
	         * 画面名（等級・質）
	         */
	        public static String  SHITU			= "等級・質";
	        /**
	         * 画面名（階級・サイズ）
	         */
	        public static String  SIZE			= "階級・サイズ";
	        /**
	         * 画面名（容器）
	         */
	        public static String  YOUKI			= "容器";
	        /**
	         * 画面名（土）
	         */
	        public static String  SOIL			= "土";
	        /**
	         * 画面名（資材）
	         */
	        public static String  SIZAI			= "資材";
        }
        /**
         * アカウント
         */
        public static final int  	  ACCOUNT 			= 0;
        /**
         * 地主
         */
        public static final int  	  LANDLORD			= 1;
        /**
         * 圃場グループ
         */
        public static final int  	  FIELDGROUP		= 2;
        /**
         * 圃場
         */
        public static final int  	  FIELD				= 3;
        /**
         * 区画
         */
        public static final int  	  COMPARTMENT		= 4;
        /**
         * 生産物グループ
         */
        public static final int  	  CROPGROUP			= 5;
        /**
         * 生産物
         */
        public static final int  	  CROP				= 6;
        /**
         * 農肥
         */
        public static final int  	  NOUHI				= 7;
        /**
         * 種
         */
        public static final int  	  HINSYU			= 8;
        /**
         * ベルト
         */
        public static final int  	  BELTO				= 9;
        /**
         * 機器
         */
        public static final int  	  KIKI				= 10;
        /**
         * アタッチメント
         */
        public static final int  	  ATTACHMENT		= 11;
        /**
         * 荷姿
         */
        public static final int  	  NISUGATA			= 12;
        /**
         * 等級・質
         */
        public static final int  	  SHITU				= 13;
        /**
         * 階級・サイズ
         */
        public static final int  	  SIZE				= 14;
        /**
         * 資材
         */
        public static final int  	  SIZAI				= 15;
        /**
         * 生産者
         */
        public static final int  	  FARM				= 16;
        /**
         * 容器
         */
        public static final int  	  YOUKI				= 17;
        /**
         * 土
         */
        public static final int  	  SOIL				= 18;

    }

	/**
	 * 育苗情報
	 *
	 * @author SUN FOR YOU.Ltd
	 *
	 */
    public class IkubyoInfo {
        /**
         * 該当作業
         */
        public static final String  TARGETIKUBYOWORK = "targetIkubyoWork";
        /**
         * 該当苗状況
         */
        public static final String  TARGETNAESTATUS	= "targetNaeStatus";

        public static final double WORKCHAINID = 36;
    }

	/**
	 * セッションチェック
	 *
	 * @author SUN FOR YOU.Ltd
	 *
	 */
	public class SessionCheck {

		/** セッションチェック用 アプリケーションキーコード */
		public static final String 		AppKeyCode			= 	"AGRYEELSessionCheck";

		/** セッションチェックエラー */
		public static final String		AuthError				=	"-1";

		/** キャッシュ有効期限（３０分） */
		public static final int			CacheLimitTime			=	60 * 30;

		/** キャッシュ有効期限（１週間） */
		public static final int			LoginCookieLimitTime	=	60 * 60 * 24 * 7;
	}

	/**
	 * メール情報
	 *
	 * @author SUN FOR YOU.Ltd
	 *
	 */
	public class MailInfo {
		public static final String		CHARSET					= "UTF-8";
		public static final String		HOSTNAME				= "smtp.lolipop.jp";
		public static final int			PORT					= 465;
		public static final boolean		SSL						= true;
		public static final String		AUTHUSER				= "service@sunforyou.jp";
		public static final String		AUTHPASSWORD			= "Service_0363";
		public static final String		FROMADDRESS				= "service@sunforyou.jp";
	}

	/**
	 * 作業テンプレート
	 * @author kimura
	 *
	 */
	public class WorkTemplate {
    public static final int NOMAL          = 1;
    public static final int SANPU          = 2;
    public static final int HASHU          = 3;
    public static final int SHUKAKU        = 4;
    public static final int NOUKO          = 5;
    public static final int KANSUI         = 6;
    public static final int END            = 7;
    public static final int KAISHU         = 8;
    public static final int DACHAKU        = 9;
    public static final int COMMENT        = 10;
    public static final int MALTI          = 11;
    public static final int TEISHOKU       = 12;
    public static final int NAEHASHU       = 13;
    public static final int AUTOKANSUI     = 14;
    public static final int SENTEI         = 15;
    public static final int SAIBAIKAISI    = 16;
    public static final int MABIKI         = 17;
    public static final int NICHOCHOSEI    = 18;
    public static final int SENKA          = 19;
    public static final int HAIKI          = 20;
    public static final int NAEHASHUIK     = 21;
    public static final int KARITORIIK     = 22;
    public static final int HAIKIIK        = 23;
	}
  public class WorkDetailKind {
    public static final int DACHAKU         = 1;
    public static final int COMMENT         = 2;
    public static final int MALTI           = 3;
  }
  public class NouhiKind {
    public static final int NONE           = 0;
    public static final int NOUYAKU        = 1;
    public static final int HIRYO          = 2;
  }
  public class WorkDiaryMode {
    public static final int INIT          = 1;
    public static final int NEW           = 2;
    public static final int EDIT          = 3;
    public static final int DELETE        = 4;
    public static final int WORKING       = 10;
  }
  public class JITUYO {
    public static final int JISEKI        = 0;
    public static final int YOSOKU        = 1;
  }
  public class DISPLAYSTATUS {
    public static final int KUKAKU        = 0;
    public static final int FIELD         = 1;
  }
  public class MessageIcon {
    public static final int NONE          = 0;
    public static final int WORNING       = 1;
    public static final int NOTE          = 2;
    public static final int DANGER        = 3;
  }
  public class AutoKansuiStatus {
    public static final int NONE          = -1;
    public static final int WAITING       = 0;
    public static final int NOW           = 1;
    public static final int END           = 9;
  }
  public class SpecialAccount {
    public static final String ALLACOUNT  = "aaaaaa";
  }
  public class MessageKind {
    public static final int PUSH          =  0;
    public static final int SYSTEM        =  1;
    public static final int ONE           =  2;
    public static final int ALL           = 99;
  }
  public class WORKPLANFLAG {
    public static final int WORKDIARYCOMMIT = 0;
    public static final int WORKDIARYWATCH  = 1;
    public static final int WORKPLANCOMMIT  = 2;
    public static final int AICAPLANCOMMIT  = 3;
    public static final int WORKPLANWATCH   = 4;
    public static final int AICAPLANWATCH   = 5;
  }
  public class WORKPLANEND {
    public static final int WORKPLAN = 0;
    public static final int END = 1;
  }
  public class RADIUSKUKAKU {
    public static final int TARGET = 0;
    public static final int RADIUS = 1;
  }
  public class WORKPLANAUTOMOVE {
    public static final int NONE = 0;
    public static final int MOVE = 1;
    public static final int LIMITMOVE = 2;
  }
  /**
   * OpenWeatherMap API
   * @author kimura
   *
   */
  public class Owm {
    public static final String URL      = "http://api.openweathermap.org/data/2.5/forecast";
    public static final String API      = "99a9f91242e9c4e50aed95e7662f075d";
    public static final int TIMEHOSEI   = 9;
  }
  public class SenSprout {
    public static final String PYTHON     = "python";
    public static final String EXECMDL    = "C:\\SenSprout\\sensor_download_csv_201109.py";
    public static final String DLF        = "C:\\SenSprout\\download\\";
  }
  public class HISTRYREFERENCE {
    public static final int LASTTIME    = 1;  //前回履歴値
    public static final int SAMECOUNT   = 2;  //前年同一回数
    public static final int SAMESEASON  = 3;  //前年同一時期
    public static final int SAMEKUKAKU  = 4;  //前回同一区画
  }
  public class ManegerRoll {
    public static final int TANTO    =  0;  //担当者
    public static final int KANRI    =  1;  //作業管理者
    public static final int KEIEI    =  2;  //経営者
    public static final int MAKER    = 99; //メーカー
  }
}
