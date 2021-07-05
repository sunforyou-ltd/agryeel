package compornent;

import java.util.List;

import models.Nisugata;
import models.NisugataOfFarm;
import models.Sequence;

import com.avaje.ebean.Ebean;

/**
 * 【AGRYEEL】荷姿コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NisugataComprtnent implements AgryellInterface{

  List<Nisugata> nisugata;

	/**
	 * コンストラクタ
	 */
	public NisugataComprtnent() {

		nisugata = Nisugata.find.orderBy("nisugata_id").findList();

	}

  /**
   * 荷姿情報を生成する
   * @param pName
   * @return
   */
  public static Nisugata makeNisugata(String pName, long pKind, double pCapacity, double pKingaku) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.NISUGATAID);       //最新シーケンス値の取得

    Nisugata nisugata = new Nisugata();
    nisugata.nisugataId    = sequence.sequenceValue;
    nisugata.nisugataName  = pName;
    nisugata.nisugataKind  = pKind;
    nisugata.capacity  	   = pCapacity;
    nisugata.kingaku       = pKingaku;
    nisugata.save();

    return nisugata;

  }

  /**
   * 生産者別荷姿情報を生成する
   * @param pNisugataId
   * @param pFarmId
   * @return
   */
  public static NisugataOfFarm makeNisugataOfFarm(double pNisugataId, double pFarmId) {

	NisugataOfFarm nisugata = new NisugataOfFarm();
	nisugata.nisugataId = pNisugataId;
	nisugata.farmId  = pFarmId;
	nisugata.save();

    return nisugata;

  }

  /**
   * 荷姿情報を更新する
   * @param pNisugataId
   * @param pName
   * @return
   */
  public static Nisugata updateNisugata(double pNisugataId, String pName, long pKind, double pCapacity, double pKingaku) {

	  Nisugata nisugata = Nisugata.getNisugataInfo(pNisugataId);
	  nisugata.nisugataName  = pName;
	  nisugata.nisugataKind  = pKind;
	  nisugata.capacity  	   = pCapacity;
	  nisugata.kingaku       = pKingaku;
	  nisugata.update();

    return nisugata;

  }

  /**
  * 荷姿を削除します
  * @param pNisugataId
  * @param pFarmId
  * @return
  */
 public static int deleteNisugata(double pNisugataId, double pFarmId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- 荷姿を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM nisugata WHERE nisugata_id = " + pNisugataId).execute();
   Nisugata nisugata = Nisugata.getNisugataInfo(pNisugataId);
   nisugata.deleteFlag = 1;
   nisugata.update();

   //----- 生産者別荷姿を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM nisugata_of_farm WHERE farm_id = " + pFarmId + "AND nisugata_id = " + pNisugataId).execute();
   NisugataOfFarm nof = NisugataOfFarm.find.where().eq("farm_id", pFarmId).eq("nisugata_id", pNisugataId).findUnique();
   nof.deleteFlag = 1;
   nof.update();

   return result;

 }

}
