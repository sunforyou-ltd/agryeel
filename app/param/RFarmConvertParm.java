package param;

/**
 *  RushFarmコンバート用パラメータ
 * @author kimura
 *
 */
public class RFarmConvertParm {
  /**
   * 作業日
   */
  public java.sql.Date workDate;
  /**
   * アカウントＩＤ
   */
  public String accountId;
  /**
   * 区画ＩＤ
   */
  public double kukakuId;
  /**
   * 作業開始時間
   */
  public java.sql.Timestamp workStart;
  /**
   * 作業開始終了
   */
  public java.sql.Timestamp workEnd;
  /**
   * 作業時間
   */
  public int workTime;
  /**
   * 荷姿数
   */
  public int nisugataCount;
  /**
   * データ
   */
  public String[] data;

  public RFarmConvertParm() {
    clear();
  }
  public void clear() {
    workDate      = null;
    accountId     = "";
    kukakuId      = 0;
    workStart     = null;
    workEnd       = null;
    workTime      = 0;
    nisugataCount = 0;
    data          = null;
  }
}
