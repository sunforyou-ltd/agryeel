package models;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】出荷情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Syukka extends Model {

    /**
     * 出荷No
     */
    public String syukkaNo;
    /**
     * 出荷日付
     */
    public Date syukkaDate;
    /**
     * 出荷先ID
     */
    public double syukkaSakiId;
    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * 生産物ID
     */
    public double cropId;
    /**
     * 商品名
     */
    public String syohinName;
    /**
     * 産地
     */
    public String origin;
    /**
     * 場所
     */
    public String place;
    /**
     * 荷姿ID
     */
    public double nisugataId;
    /**
     * サイズID
     */
    public double sizeId;
    /**
     * 重量
     */
    public double jyuRyo;
    /**
     * 入数
     */
    public double irisu;
    /**
     * 単価
     */
    public double tanka;
    /**
     * 出荷量
     */
    public double syukkaRyo;
    /**
     * 金額
     */
    public double kingaku;

    public static Finder<Long, Syukka> find = new Finder<Long, Syukka>(Long.class, Syukka.class);

    public static String makeSyukkaNo(double pFarmId, java.sql.Date pSyukkaDate) {
      String result = "";
      DecimalFormat     df = new DecimalFormat("0000");
      SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

      List<Syukka> syukkas = Syukka.find.where().eq("farm_id", pFarmId)
                                                .eq("syukka_date", pSyukkaDate)
                                                .orderBy("syukka_no desc")
                                                .findList();
      if (syukkas.size() == 0) { //該当レコードが存在しない
        result = df.format(pFarmId) + "-"
               + sdf.format(pSyukkaDate) + "-"
               + df.format(1);
      }
      else {
        int renban = Integer.parseInt(syukkas.get(0).syukkaNo.substring(12)); //連番部分を抽出
        renban++;
        result = df.format(pFarmId) + "-"
            + sdf.format(pSyukkaDate) + "-"
            + df.format(renban);
      }

      return result;
    }
}
