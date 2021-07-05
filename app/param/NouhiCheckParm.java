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
  public NouhiCheckParm(double pNouhiId, double pKukakuId, java.util.Date pworkDate) {
    clear();
    nouhiId   = pNouhiId;
    kukakuId  = pKukakuId;
    workDate  = pworkDate;
  }
  public void clear() {
    nouhiId     = 0;
    kukakuId    = 0;
    workDate    = null;
    sanpuCount  = 0;
    useWhen     = 0;
    checkcode   = 0;
    message     = "";
  }
}
