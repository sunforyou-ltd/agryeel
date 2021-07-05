package models;

import javax.persistence.Entity;

import play.Logger;
import play.db.ebean.Model;

@Entity
/**
 * 【AGRYEEL】シーケンス情報モデル
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class Sequence extends Model {

    /**
   *
   */
  private static final long serialVersionUID = 977887280747670650L;

    /**
     * 【AGRYEEL】シーケンスID定義
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public class SequenceIdConst {

        /**
         * なし
         */
        public static final int NONE			=	0;
        /**
         * 農場ID
         */
        public static final int FARMID			=	1;
        /**
         * 区画グループ(AICA移行に伴い、廃止)
         */
        public static final int KUKAKUGROUPID	=	2;
        /**
         * 区画ID
         */
        public static final int KUKAKUID		=	3;
        /**
         * チームID
         */
        public static final int TEAMID			=	4;
        /**
         * 作業ID
         */
        public static final int WORKID			=	5;
        /**
         * 生産物ID
         */
        public static final int CROPID			=	6;
        /**
         * クリップグループID
         */
        public static final int CRIPGROUPID		=	7;
        /**
         * 作業記録ID
         */
        public static final int WORKDIARYID		=	8;
        /**
         * タイムラインID
         */
        public static final int TIMELINEID		=	9;
        /**
         * 農肥ID
         */
        public static final int NOUHIID			=  10;
        /**
         * コンビID
         */
        public static final int COMBIID			=  11;
        /**
         * 農肥グループID
         */
        public static final int NOUHIGROUPID	=  12;
        /**
         * 品種ID
         */
        public static final int HINSYUID		=  13;
        /**
         * ベルトID
         */
        public static final int BELTOID			=  14;
        /**
         * 機器ID
         */
        public static final int KIKIID			=  15;
        /**
         * アタッチメントID
         */
        public static final int ATTACHMENTID		=  16;
        /**
         * ワークチェインID
         */
        public static final int WORKCHAINID		=  17;
        /**
         * 散布組合せID
         */
        public static final int SANPUCOMBIID		=  18;
        /**
         * 生産物グループID
         */
        public static final int CROPGROUPID    =  19;
        /**
         * 圃場グループID
         */
        public static final int FIELDGROUPID    =  20;
        /**
         * 圃場ID
         */
        public static final int FIELDID    =  21;
        /**
         * 生産者グループID
         */
        public static final int FARMGROUPID    =  22;
        /**
         * 地主ID
         */
        public static final int LANDLORDID    =  23;
        /**
         * 荷姿ID
         */
        public static final int NISUGATAID    =  24;
        /**
         * 質ID
         */
        public static final int SHITUID    =  25;
        /**
         * サイズID
         */
        public static final int SIZEID    =  26;
        /**
         * 資材ID
         */
        public static final int SIZAIID    =  27;
        /**
         * 自動潅水ID
         */
        public static final int AUTOKANSUIID    =  28;
        /**
         * 作業計画ID
         */
        public static final int WORKPLANID      =  29;
    }

    /**
     * シーケンスID
     */
    public int sequenceId;
    /**
     * シーケンス値
     */
    public double sequenceValue;

    public static Finder<Long, Sequence> find = new Finder<Long, Sequence>(Long.class, Sequence.class);

    /**
     * 渡された種別のシーケンス値を更新取得します
     * @param sequenceId シーケンスID
     * @return シーケンス情報モデル
     */
    public static Sequence GetSequenceValue(int sequenceId) {

        boolean update = false;
        int     count = 5;
        Sequence sequence = null;

        while(!update) {

          if ( count <= 0 ) {
            break;
          }

          try {
            sequence = Sequence.find.where().eq("sequence_id", sequenceId).findUnique();

            if (sequence == null) {
                sequence = new Sequence();
                sequence.sequenceId   = sequenceId;
                sequence.sequenceValue  = 1;
                sequence.save();
                update = true;
            }
            else {
                sequence.sequenceValue++;
                sequence.update();
                update = true;
            }
          }
          catch(Exception ex) {
            Logger.error("[Sequence ERROR] sequenceId={} COUNT={}", sequenceId, count, ex);
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              // TODO 自動生成された catch ブロック
              e.printStackTrace();
            }
          }
          count--;
        }


        return sequence;

    }

    /**
     * 渡された種別の現在のシーケンス値を取得します
     * @param sequenceId シーケンスID
     * @return シーケンス情報モデル
     */
    public static Sequence GetSequenceNowValue(int sequenceId) {

        Sequence sequence = Sequence.find.where().eq("sequence_id", sequenceId).findUnique();

        if (sequence == null) {
            sequence = new Sequence();
            sequence.sequenceId 	= sequenceId;
            sequence.sequenceValue 	= 0;
        }

        return sequence;

    }

}
