package compornent;

import java.util.List;

import models.Belto;
import models.BeltoOfFarm;
import models.Sequence;

import com.avaje.ebean.Ebean;

/**
 * 【AGRYEEL】ベルトコンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class BeltoComprtnent implements AgryellInterface{

  List<Belto> beltos;

	/**
	 * コンストラクタ
	 */
	public BeltoComprtnent() {

		beltos = Belto.find.orderBy("belto_id").findList();

	}

  /**
   * ベルト情報を生成する
   * @param pName
   * @return
   */
  public static Belto makeBelto(String pName) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.BELTOID);       //最新シーケンス値の取得

    Belto belto = new Belto();
    belto.beltoId    = sequence.sequenceValue;
    belto.beltoName  = pName;
    belto.save();

    return belto;

  }

  /**
   * 生産者別ベルト情報を生成する
   * @param pBeltoId
   * @param pFarmId
   * @return
   */
  public static BeltoOfFarm makeBeltoOfFarm(double pBeltoId, double pFarmId) {

    BeltoOfFarm belto = new BeltoOfFarm();
    belto.beltoId = pBeltoId;
    belto.farmId  = pFarmId;
    belto.save();

    return belto;

  }

  /**
   * ベルト情報を更新する
   * @param pBeltoId
   * @param pName
   * @return
   */
  public static Belto updateBelto(double pBeltoId, String pName) {

	  Belto belto = Belto.getBeltoInfo(pBeltoId);
	  belto.beltoName  = pName;
	  belto.update();

    return belto;

  }

  /**
  * ベルトを削除します
  * @param pBeltoId
  * @param pFarmId
  * @return
  */
 public static int deleteBelto(double pBeltoId, double pFarmId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- ベルトを削除 ----
   //Ebean.createSqlUpdate("DELETE FROM belto WHERE belto_id = " + pBeltoId).execute();
   Belto belto = Belto.getBeltoInfo(pBeltoId);
   belto.deleteFlag = 1;
   belto.update();

   //----- 生産者別ベルトを削除 ----
   //Ebean.createSqlUpdate("DELETE FROM belto_of_farm WHERE farm_id = " + pFarmId + "AND belto_id = " + pBeltoId).execute();
   BeltoOfFarm bof = BeltoOfFarm.find.where().eq("farm_id", pFarmId).eq("belto_id", pBeltoId).findUnique();
   bof.deleteFlag = 1;
   bof.update();

   return result;

 }

}
