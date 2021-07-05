package models;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Entity;

import play.db.ebean.Model;
import util.DateU;

@Entity
/**
 * 【AGRYEEL】[大坪物産]農薬コンバートデータ
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class ObussanNouyaku extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 1443372075535666265L;
    /**
     * 作業日付
     */
    public Date workDate;
    /**
     * 農肥ID
     */
    public double nouhiId;
    /**
     * 区画ID
     */
    public double kukakuId;
    /**
     * 倍率
     */
    public double bairitu;
    /**
     * 散布量
     */
    public double sanpuryo;

    public static Finder<Long, ObussanNouyaku> find = new Finder<Long, ObussanNouyaku>(Long.class, ObussanNouyaku.class);

}
