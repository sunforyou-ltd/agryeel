package compornent;

import java.util.List;
import java.util.Random;

import util.StringU;
import consts.AgryeelConst;
import controllers.SystemBatch;

import models.Account;
import models.Compartment;
import models.Farm;
import models.FarmGroup;
import models.FarmStatus;
import models.Field;
import models.FieldGroup;
import models.FieldGroupList;
import models.Sequence;

/**
 * 【AGRYEEL】農場コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class FarmComprtnent {

	/** 取得成功 */
	public static final int 	GET_SUCCESS	= 0;
	/** 取得失敗 */
	public static final int 	GET_ERROR	= -1;

	/** レジストレーション認証（認証成功） */
	public static final int 	REGIST_CHECK_SUCCESS	=  0;
	/** レジストレーション認証（認証失敗） */
	public static final int 	REGIST_CHECK_NG			= -1;

	/** メールアドレス重複チェック(未重複) */
	public static final int 	REGIST_ADDRESS_SUCCESS	=  0;
	/** メールアドレス重複チェック(重複) */
	public static final int 	REGIST_ADDRESS_NG		= -1;


	/** 農場情報 */
	public Farm farmData;
	public FarmStatus farmStatusData;

	/**
	 * コンストラクタ
	 */
	public FarmComprtnent() {

		farmData = null;
		farmStatusData = null;

	}

	/**
	 * 農場情報を取得する
	 * @param farmId 農場ＩＤ
	 * @return 取得結果
	 */
	public int GetFarmData(double farmId) {

		/** 戻り値 */
		int	result	=	GET_SUCCESS;

        Farm farm = Farm.find.where().eq("farm_id", farmId).findUnique();									//農場を検索

        if (farm != null) { //農場が取得できた場合
        	farmData = farm;
        	FarmStatus farmStatus = FarmStatus.find.where().eq("farm_id", farmId).findUnique();
        	if (farmStatus != null) {
        	  farmStatusData = farmStatus;
        	}
        }
        else {
    		result	=	GET_ERROR;
        }

        return result;

	}

	/**
	 * レジストレーション認証を行う
	 * @param registrationCode レジストレーションコード
	 * @return 取得結果
	 */
	public int RegistrationCheck(String registrationCode) {

		/** 戻り値 */
		int	result	=	REGIST_CHECK_SUCCESS;

        Farm farm = Farm.find.where().eq("registration_code", registrationCode).findUnique();				//農場を検索

        if (farm != null) { //農場が取得できた場合
        	farmData = farm;
        }
        else {
    		result	=	REGIST_CHECK_NG;
        }

        return result;

	}

	/**
	 * 対象農場のアカウント登録数上限チェックを行う
	 * @param registrationCode レジストレーションコード
	 * @return 取得結果
	 */
	public int AccountRegistLimitCheck(String registrationCode) {

		/** 戻り値 */
		int	result	=	REGIST_CHECK_SUCCESS;

        Farm farm = Farm.find.where().eq("registration_code", registrationCode).findUnique();				//農場を検索

        if (farm != null) { //農場が取得できた場合
        	farmData = farm;
            FarmStatus farmStatus = FarmStatus.find.where().eq("farm_id", farmData.farmId).findUnique();
            if (farmStatus.contractPlan == AgryeelConst.ContractPlanInfo.LIGHTSTANDARD ||
                farmStatus.contractPlan == AgryeelConst.ContractPlanInfo.LIGHTPRO) {
                List<Account> accounts = Account.getAccountOfFarm(farmData.farmId);	//対象農業の全アカウント取得
                if (accounts.size() == 5) {
                    result	=	REGIST_CHECK_NG;
                }
            }
        }
        else {
    		result	=	REGIST_CHECK_NG;
        }

        return result;
	}

  /**
   * 対象圃場一覧を取得する
   * @param farmId 農場ＩＤ
   * @return 取得結果
   */
  public List<Field> GetField(double farmId) {

        return Field.find.where().eq("farm_id", farmId).findList();

  }
	/**
	 * 対象区画一覧を取得する
	 * @param farmId 生産者ＩＤ
	 * @return 取得結果
	 */
	public List<Compartment> GetCompartment(double farmId) {

        return Compartment.find.where().eq("farm_id", farmId).findList();

	}

	/**
	 * 渡されたメールアドレスで農場が既に登録されているかチェックする
	 * @param mailAddress
	 * @return
	 */
	public static int CheckRegistAddress(String mailAddress) {

		/** 戻り値 */
		int	result	=	REGIST_ADDRESS_SUCCESS;

        List<Farm> farm = Farm.find.where().eq("mail_address_pc", mailAddress).findList();				//農場を検索

        if (farm.size() > 0) { //農場が取得できた場合
    		result	=	REGIST_ADDRESS_NG;
        }

        return result;

	}

	/**
	 * 生産者データを作成します
	 * @param mailAddress
	 * @param farmName
	 * @return
	 */
	public static Farm MakeFarm(String mailAddress, String farmName) {

        /*------------------------------------------------------------------------------------------------------------*/
        /* 生産者グループデータを作成する                                                                                       */
        /*------------------------------------------------------------------------------------------------------------*/
        FarmGroup farmGroup = new FarmGroup();

        Sequence sequenceGroup  = Sequence.GetSequenceValue(Sequence.SequenceIdConst.FARMGROUPID);       //最新シーケンス値の取得

        farmGroup.farmGroupId   = sequenceGroup.sequenceValue;
        farmGroup.farmGroupName = farmName + "グループ";
        farmGroup.save();

        /*------------------------------------------------------------------------------------------------------------*/
        /* 生産者データを作成する                                                                                       */
        /*------------------------------------------------------------------------------------------------------------*/
        Farm farm = new Farm();

        Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.FARMID);				//最新シーケンス値の取得

        farm.farmId				   = sequence.sequenceValue;
        farm.farmGroupId		 = farmGroup.farmGroupId;
        farm.mailAddressPC	 = mailAddress;
        farm.farmName			   = farmName;
        farm.registrationCode 	= MakeRegistration();
        farm.save();

        /*------------------------------------------------------------------------------------------------------------*/
        /* 生産者ステータスデータを作成する                                                                           */
        /*------------------------------------------------------------------------------------------------------------*/
        FarmStatus farmStatus = new FarmStatus();
        farmStatus.farmId	= farm.farmId;
        farmStatus.kisyo	= 1;
        farmStatus.save();

        /*------------------------------------------------------------------------------------------------------------*/
        /* 農場別生産物データを作成する                                                                               */
        /*------------------------------------------------------------------------------------------------------------*/
        //CropCompornentへ処理を移行

        /*------------------------------------------------------------------------------------------------------------*/
        /* レジストレーションコードを送信する                                                                         */
        /*------------------------------------------------------------------------------------------------------------*/
        StringBuffer sb = new StringBuffer();

        sb.append(farm.farmName + " 様\n");
        sb.append("\n");
        sb.append("この度は｢AGRYELL｣に使用登録をして頂きありがとうございます。\n");
        sb.append("アカウントを作成する際に必要なレジストレーションコードは\n");
        sb.append("\n");
        sb.append(farm.registrationCode + "\n");
        sb.append("\n");
        sb.append("となります。レジストレーションコードの再発行は出来ませんので、必ず保管して頂く様お願い致します。\n");
        sb.append("\n");
        sb.append("｢AGRYELL｣の機能説明やバージョンアップ情報、お問い合わせなどは下記の専用サイトをご活用ください。\n");
        sb.append("\n");
        sb.append("｢AGRYEEL 専用サイト｣\n");
        sb.append("<https://www.facebook.com/agryell/>\n");
        sb.append("\n");
        sb.append("｢AGRYEEL｣が皆様の日々のお手伝いをできる様に努力して参りますので今後ともよろしくお願い致します。\n");
        sb.append("\n");
        sb.append("◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆\n");
        sb.append("\n");
        sb.append("農業トータルサポートシステム\n");
        sb.append("｢AGRYELL｣\n");
        sb.append("<http://www.agryell.com/>\n");
        sb.append("\n");
        sb.append("｢AGRYEEL 専用サイト｣\n");
        sb.append("<https://www.facebook.com/agryell/>\n");
        sb.append("\n");
        sb.append("◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆\n");

        MailComprtnent.send(farm.mailAddressPC, "｢AGRYELL｣への生産者登録を行って頂きありがとうございます", sb.toString());

        return farm;

	}

	/**
	 * レジストレーションコードを作成します
	 * @return
	 */
	public static String MakeRegistration() {

		String result = "";				//レジストレーションコード

		String[] sCharcter = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};

		for (int iIndex = 0; iIndex < 8; iIndex++) {

			Random rndData 	= new Random();				//SEED値を変換する為
			double dValue 	= rndData.nextDouble();		//乱数の発生
			int iValue 		= (int)(dValue * 100000000);//８桁の整数に変更
			int iCode		= iValue % 16;				//16進数に変換
			result		   += sCharcter[iCode];
		}

		return result;

	}

	public static void MakeAutoHojo(double farmId, int hojoGroups, int groupType, int hojos) {

		String[] data;
		double   kukakuIdS = 0;
    double   kukakuIdE = 0;

		switch (groupType) {
		case AgryeelConst.HojoGroupType.HIRAGANA:

			data = AgryeelConst.HiraganaData;
			break;

		case AgryeelConst.HojoGroupType.KANA:

			data = AgryeelConst.KatakanaData;
			break;

		default:
			data = AgryeelConst.AlphabetData;
			break;

		}

    //【AICA】TODO：圃場/区画の制御に伴い、処理を変更する
    /*------------------------------------------------------------------------------------------------------------*/
    /* 圃場グループを作成する                                                                                     */
    /*------------------------------------------------------------------------------------------------------------*/
		for (int iGroupIndex = 0; iGroupIndex < hojoGroups; iGroupIndex++) {

		  FieldGroup fieldGroup 	= new FieldGroup();
      Sequence sequenceGroup = Sequence.GetSequenceValue(Sequence.SequenceIdConst.FIELDGROUPID);    //最新シーケンス値の取得

      fieldGroup.fieldGroupId		    = sequenceGroup.sequenceValue;
      fieldGroup.fieldGroupName	    = data[iGroupIndex];
      fieldGroup.farmId				      = farmId;
      fieldGroup.fieldGroupColor	  = AgryeelConst.ColorData[iGroupIndex];
      fieldGroup.save();

      /*------------------------------------------------------------------------------------------------------------*/
      /* 圃場を作成する                                                                                             */
      /*------------------------------------------------------------------------------------------------------------*/
			for (int iHojoIndex = 0; iHojoIndex < hojos; iHojoIndex++) {

			  //----- 圃場を生成する -----
			  Field field 	= new Field();
        Sequence sequenceHojo 		= Sequence.GetSequenceValue(Sequence.SequenceIdConst.FIELDID);		//最新シーケンス値の取得
        field.fieldId		= sequenceHojo.sequenceValue;
        String sRenban				= String.valueOf((iHojoIndex + 1));
        field.fieldName		= fieldGroup.fieldGroupName + "－" + StringU.toWideNumber(sRenban);
        field.farmId			= farmId;
        field.save();

        //----- 圃場グループに追加する-----
        FieldGroupList fgl = new FieldGroupList();
        fgl.fieldGroupId = fieldGroup.fieldGroupId;
        fgl.fieldId      = field.fieldId;
        fgl.sequenceId   = (iHojoIndex + 1);
        fgl.save();

        //----- 区画を生成する -----
        Compartment compartment   = new Compartment();
        Sequence sequenceKukaku   = Sequence.GetSequenceValue(Sequence.SequenceIdConst.KUKAKUID);    //最新シーケンス値の取得
        compartment.kukakuId      = sequenceKukaku.sequenceValue;
        compartment.kukakuName    = field.fieldName;
        compartment.farmId        = farmId;
        compartment.fieldId       = field.fieldId;
        compartment.save();

        //最初の区画IDを格納
        if (iGroupIndex == 0 && iHojoIndex == 0) {
          kukakuIdS = compartment.kukakuId;
          kukakuIdE = compartment.kukakuId;
        }
        //最終の区画IDを格納
        if (iGroupIndex == (hojoGroups - 1) && iHojoIndex == (hojos - 1)) {
          kukakuIdE = compartment.kukakuId;
        }
			}
		}

    if (kukakuIdS != 0 && kukakuIdE != 0) {
      SystemBatch.makeCompartmentALL(kukakuIdS, kukakuIdE);
    }
	}
}
