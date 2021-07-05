package compornent;

import java.util.List;

import models.Sequence;
import models.Shitu;
import models.ShituOfFarm;

import com.avaje.ebean.Ebean;

/**
 * 【AGRYEEL】質コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ShituComprtnent implements AgryellInterface{

  List<Shitu> shitu;

	/**
	 * コンストラクタ
	 */
	public ShituComprtnent() {

		shitu = Shitu.find.orderBy("shitu_id").findList();

	}

  /**
   * 質情報を生成する
   * @param pName
   * @return
   */
  public static Shitu makeShitu(String pName) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.SHITUID);       //最新シーケンス値の取得

    Shitu shitu = new Shitu();
    shitu.shituId    = sequence.sequenceValue;
    shitu.shituName  = pName;
    shitu.save();

    return shitu;

  }

  /**
   * 生産者別質情報を生成する
   * @param pBeltoId
   * @param pFarmId
   * @return
   */
  public static ShituOfFarm makeShituOfFarm(double pShituId, double pFarmId) {

	ShituOfFarm shitu = new ShituOfFarm();
	shitu.shituId = pShituId;
	shitu.farmId  = pFarmId;
	shitu.save();

    return shitu;

  }

  /**
   * 質情報を更新する
   * @param pBeltoId
   * @param pName
   * @return
   */
  public static Shitu updateShitu(double pShituId, String pName) {

	  Shitu shitu = Shitu.getShituInfo(pShituId);
	  shitu.shituName  = pName;
	  shitu.update();

    return shitu;

  }

  /**
  * 質を削除します
  * @param pBeltoId
  * @param pFarmId
  * @return
  */
 public static int deleteShitu(double pShituId, double pFarmId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- 質を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM shitu WHERE shitu_id = " + pShituId).execute();
   Shitu shitu = Shitu.getShituInfo(pShituId);
   shitu.deleteFlag = 1;
   shitu.update();

   //----- 生産者別質を削除 ----
   //Ebean.createSqlUpdate("DELETE FROM shitu_of_farm WHERE farm_id = " + pFarmId + "AND shitu_id = " + pShituId).execute();
   ShituOfFarm sof = ShituOfFarm.find.where().eq("farm_id", pFarmId).eq("shitu_id", pShituId).findUnique();
   sof.deleteFlag = 1;
   sof.update();

   return result;

 }

}
