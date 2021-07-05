package models;

import java.sql.Date;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】気象情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Weather extends Model {

  /**
   * 
   */
  private static final long serialVersionUID = -3419351417064192685L;
  /**
   * 地点ID
   */
  public String pointId;
  /**
   * 日付
   */
  public Date dayDate;
  /**
   * 平均気温
   */
  public double kionAve;
  /**
   * 最高気温
   */
  public double kionMax;
  /**
   * 最低気温
   */
  public double kionMin;
  /**
   * 降水量
   */
  public double rain;
  /**
   * 日照時間
   */
  public double daylightHours;
  /**
   * 実予
   */
  public int jituyo;

  public static Finder<Long, Weather> find = new Finder<Long, Weather>(Long.class, Weather.class);

  public static List<Weather> getWeather(String pPointId, Date pStart, Date pEnd) {
    return Weather.find.where().eq("point_id", pPointId).between("day_date", pStart, pEnd).orderBy("day_date").findList();
  }
  public static int checkWeather(String pPointId, Date pDate) {
    int result = -1;
    Weather weather = Weather.find.where().eq("point_id", pPointId).eq("day_date", pDate).orderBy("day_date").findUnique();

    if (weather != null) {
      result = weather.jituyo;
    }

    return result;
  }
  public static int checkWeatherList(String pPointId, Date pDate) {
    int result = 0;
    List<Weather> weathers = Weather.find.where().eq("point_id", pPointId).eq("day_date", pDate).orderBy("day_date, jituyo asc").findList();

    for (Weather weather : weathers) {
      if (weather.jituyo == 0) {
        result += 1;
      }
      else if (weather.jituyo == 1) {
        result += 10;
      }
    }

    return result;
  }
  public static Weather getWeather(String pPointId, Date pDate) {
    Weather weather = Weather.find.where().eq("point_id", pPointId).eq("day_date", pDate).eq("jituyo", 0).orderBy("day_date").findUnique();

    return weather;
  }
}
