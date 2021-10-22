package param;

/**
 *  農肥チェック用パラメータ
 * @author kimura
 *
 */
public class NouhiCheckParm {
  /**
   * 農肥ID
   */
  public double nouhiId;
  /**
   * 区画ID
   */
  public double kukakuId;
  /**
   * 苗No
   */
  public String naeNo;
  /**
   * 作業日
   */
  public java.util.Date workDate;
  /**
   * 散布回数
   */
  public double sanpuCount;
  /**
   * 使用時期
   */
  public double useWhen;
  /**
   * チェックコード
   */
  public int checkcode;
  /**
   * メッセージ
   */
  public String message;

  public NouhiCheckParm() {
    clear();
  }
  public NouhiCheckParm(double pNouhiId, double pKukakuId, String pNaeNo, java.util.Date pworkDate) {
    clear();
    nouhiId   = pNouhiId;
    kukakuId  = pKukakuId;
    naeNo     = pNaeNo;
    workDate  = pworkDate;
  }
  public void clear() {
    nouhiId     = 0;
    kukakuId    = 0;
    naeNo       = "";
    workDate    = null;
    sanpuCount  = 0;
    useWhen     = 0;
    checkcode   = 0;
    message     = "";
  }
}
