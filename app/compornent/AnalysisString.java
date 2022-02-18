package compornent;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class AnalysisString {

  long count=0;
  List<String> keys = new ArrayList<String>();
  Hashtable<String, Double> datas = new Hashtable<String, Double>();

  /**
   * コンストラクタ
   */
  public AnalysisString() {

  }

  /**
   * 対象キーが既に存在するかチェックする
   * @param tg 対象キー
   * @return 存在する場合、true
   */
  public boolean check(String tg) {
    boolean result = false;         /* 判定結果 */
    for (String key : keys) {       /* 格納キー分ループ */
      if (key.equals(tg)) {         /* キーを比較する */
        result = true;              /* 存在する場合、戻り値をtrueにする */
        break;                      /* 中断 */
      }
    }
    return result;                  /* 戻り値 */
  }

  /**
   * キーを追加する
   * @param tg  キー
   */
  public void add(String tg) {
    if (!check(tg)) {               /* KEYに存在しない場合のみリストにキーを追加 */
      keys.add(tg);                 /* キーを追加する */
      count++;                      /* 件数をカウントアップ */
    }
  }
  /**
   * 集計値を加算する
   * @param tg  キー
   * @param vl  格納値
   */
  public void put(String tg, double vl) {
    Double data = datas.get(tg);    /* 集計値を取得する */
    if (data == null) {             /* 集計値が存在しない場合 */
      datas.put(tg, new Double(vl));/* 格納値を集計値とする */
    }
    else {                          /* 取得できた場合 */
      datas.put(tg, new Double(data.doubleValue() + vl)); /* 格納値＋集計値を保存する */
    }
  }
  /**
   * キーの集計値を取得する
   * @param tg  キー
   * @return  集計値
   */
  public double data(String tg) {
    Double data = datas.get(tg);    /* 集計値を取得する */
    if (data == null) {             /* 集計値が存在しない場合 */
      return 0;                     /* 集計値を0とする */
    }
    return data.doubleValue();      /* 集計値を戻り値とする */
  }

  /**
   * 集計キーの件数を取得する
   * @return 件数
   */
  public long getCount() {
    return count;                   /* 件数 */
  }

  /**
   * 格納キーを取得する
   * @return 格納キーリスト
   */
  public List<String> getKeys() {
    return keys;                    /* 格納キーリストを戻り値とする */
  }

  /**
   * 初期化処理
   */
  public void clear() {
    count=0;                                  /* 件数を0にする */
    keys.clear();                             /* キーリストを初期化する */
    datas = new Hashtable<String, Double>();  /* 格納マップを初期化する */
  }
  /**
   * 格納値を初期化する
   */
  public void clearValue() {
    datas = new Hashtable<String, Double>();  /* 格納マップを初期化する */
    for (String key : keys) {                 /* 格納キー分ループする */
      put(key, 0);                            /* 集計値を0にする */
    }
  }

}
