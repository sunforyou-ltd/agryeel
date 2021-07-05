package compornent;

/***
 * 【AGRYELL】共通インターフェース
 * @author kimura
 *
 */
public interface AgryellInterface {

  //----- 戻り値 -----
  /** 取得成功 */
  int   GET_SUCCESS = 0;
  /** 取得失敗 */
  int   GET_ERROR = -1;
  /** 更新成功 */
  int   UPDATE_SUCCESS = 0;
  /** 更新失敗 */
  int   UPDATE_ERROR = -1;

  //----- JSON 特殊値 -----
  String JSON_NONE = "none";

}
