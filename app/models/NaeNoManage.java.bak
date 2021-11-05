package models;

import javax.persistence.Entity;

import play.Logger;
import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】苗No管理情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class NaeNoManage extends Model {

    /**
     * 生産者ID
     */
    public double farmId;
    /**
     * シーケンス値
     */
    public double sequenceValue;

    public static Finder<Long, NaeNoManage> find = new Finder<Long, NaeNoManage>(Long.class, NaeNoManage.class);

    /**
     * 渡された生産者IDのシーケンス値を更新取得します
     * @param farmId 生産者ID
     * @return 苗No管理情報モデル
     */
    public static NaeNoManage GetSequenceValue(double farmId) {

        boolean update = false;
        int     count = 5;
        NaeNoManage naeNoManage = null;

        while(!update) {

          if ( count <= 0 ) {
            break;
          }

          try {
            naeNoManage = NaeNoManage.find.where().eq("farm_id", farmId).findUnique();

            if (naeNoManage == null) {
                naeNoManage = new NaeNoManage();
                naeNoManage.farmId = farmId;
                naeNoManage.sequenceValue = 1;
                naeNoManage.save();
                update = true;
            }
            else {
                naeNoManage.sequenceValue++;
                naeNoManage.update();
                update = true;
            }
          }
          catch(Exception ex) {
            Logger.error("[Sequence ERROR] farmId={} COUNT={}", farmId, count, ex);
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              // TODO 自動生成された catch ブロック
              e.printStackTrace();
            }
          }
          count--;
        }
        return naeNoManage;

    }

    /**
     * 渡された生産者IDの現在のシーケンス値を取得します
     * @param farmId 生産者ID
     * @return 苗No管理情報モデル
     */
    public static NaeNoManage GetSequenceNowValue(double farmId) {

        NaeNoManage naeNoManage = NaeNoManage.find.where().eq("farm_id", farmId).findUnique();

        if (naeNoManage == null) {
            naeNoManage = new NaeNoManage();
            naeNoManage.farmId = farmId;
            naeNoManage.sequenceValue = 0;
        }

        return naeNoManage;

    }

}
