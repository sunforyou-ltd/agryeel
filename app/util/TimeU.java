package util;

/**
 * 時間換算ユーティリティクラス
 * @author kimura
 *
 */
public class TimeU {

  // 時間を秒(second)で保持
  private int second = 0;


  // 秒をセット
  public void set_second( int s )
  {
    second = s;
  }

  // 分を秒でセット
  public void set_minute( int m )
  {
    // 分を秒に変換(1分=60秒)
    second = m * 60;
  }

  // 時間を秒でセット（実数）
  public void set_hour( double h )
  {
    // 時間を秒に変換(1時間=3600秒)
    second = (int)( h * 3600.0 );
  }


  // 時間成分を返す
  public int get_hour()
  {
    // 秒を3600で割って時間を計算
    return second / 3600;
  }


  // 分成分を返す
  public int get_minute()
  {
    // 秒を60で割って分を計算
    // さらに60で割ったあまりを分成分とする
    return ( second / 60 ) % 60;
  }


  // 秒成分を返す
  public int get_second()
  {
    // 秒を60で割ったあまりを秒成分とする
    return second % 60;
  }

}
