package compornent;

import java.util.List;

import models.Sequence;
import models.Size;
import models.SizeOfFarm;

import com.avaje.ebean.Ebean;

/**
 * 【AGRYEEL】サイズコンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SizeComprtnent implements AgryellInterface{

  List<Size> size;

	/**
	 * コンストラクタ
	 */
	public SizeComprtnent() {

		size = Size.find.orderBy("size_id").findList();

	}

  /**
   * サイズ情報を生成する
   * @param pName
   * @return
   */
  public static Size makeSize(String pName) {

    Sequence sequence = Sequence.GetSequenceValue(Sequence.SequenceIdConst.SIZEID);       //最新シーケンス値の取得

    Size size = new Size();
    size.sizeId    = sequence.sequenceValue;
    size.sizeName  = pName;
    size.save();

    return size;

  }

  /**
   * 生産者別サイズ情報を生成する
   * @param pBeltoId
   * @param pFarmId
   * @return
   */
  public static SizeOfFarm makeSizeOfFarm(double pSizeId, double pFarmId) {

	SizeOfFarm size = new SizeOfFarm();
	size.sizeId = pSizeId;
	size.farmId  = pFarmId;
	size.save();

    return size;

  }

  /**
   * サイズ情報を更新する
   * @param pSizeId
   * @param pName
   * @return
   */
  public static Size updateSize(double pSizeId, String pName) {

	  Size size = Size.getSizeInfo(pSizeId);
	  size.sizeName  = pName;
	  size.update();

    return size;

  }

  /**
  * サイズを削除します
  * @param pBeltoId
  * @param pFarmId
  * @return
  */
 public static int deleteSize(double pSizeId, double pFarmId) {

   /** 戻り値 */
   int result  = UPDATE_SUCCESS;

   //----- サイズを削除 ----
   //Ebean.createSqlUpdate("DELETE FROM size WHERE size_id = " + pSizeId).execute();
   Size size = Size.getSizeInfo(pSizeId);
   size.deleteFlag = 1;
   size.update();

   //----- 生産者別サイズを削除 ----
   //Ebean.createSqlUpdate("DELETE FROM size_of_farm WHERE farm_id = " + pFarmId + "AND size_id = " + pSizeId).execute();
   SizeOfFarm sof = SizeOfFarm.find.where().eq("farm_id", pFarmId).eq("size_id", pSizeId).findUnique();
   sof.deleteFlag = 1;
   sof.update();

   return result;

 }

}
