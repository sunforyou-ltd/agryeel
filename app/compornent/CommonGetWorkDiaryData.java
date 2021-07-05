package compornent;

import java.util.ArrayList;
import java.util.List;

import models.Compartment;
import models.Crop;
import models.Kiki;
import models.Nouhi;
import models.NouhiOfCrop;
import models.Work;
import play.db.ebean.Model;


/**
 * 【AGRYEEL】作業記録共通項目情報取得
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CommonGetWorkDiaryData {

    /**
     * ユニーク情報を取得する
     * @param 情報種別
     * @param 検索条件文字列
     * @return
     */
    public static Model GetData(int infoKind, String searchString) {

        Model mdlData	=	null;					//検索結果モデル

        switch(infoKind) {							//情報種別毎に情報を取得する
            case InfoKindConst.CROP:				//生産物情報
                mdlData = Crop.find.where(searchString).findUnique();
                break;
            case InfoKindConst.COMPARTMENT:			//区画情報
                mdlData = Compartment.find.where(searchString).findUnique();
                break;
            case InfoKindConst.NOUHIOFCROP:			//生産物農肥情報
                mdlData = NouhiOfCrop.find.where(searchString).findUnique();
                break;
            case InfoKindConst.WORK:				//作業情報
                mdlData = Work.find.where(searchString).findUnique();
                break;
            case InfoKindConst.NOUHI:				//農肥情報
                mdlData = Nouhi.find.where(searchString).findUnique();
                break;
            case InfoKindConst.KIKI:				//機器情報
                mdlData = Kiki.find.where(searchString).findUnique();
                break;
        }


        return mdlData;

    }

    /**
     * 情報を取得する
     * @param 情報種別
     * @param 検索条件文字列
     * @return
     */
    public static List<Model> GetDataList(int infoKind, String searchString, String orderString) {

        List<Model> mdlDataList	=	new ArrayList<Model>();	//検索結果モデル

        switch(infoKind) {							//情報種別毎に情報を取得する
            case InfoKindConst.CROP:				//生産物情報

                List<Crop>mdlCrop = Crop.find.where(searchString).order(orderString).findList();

                for (Crop mdldata : mdlCrop) {
                    mdlDataList.add(mdldata);
                }

                break;
            case InfoKindConst.COMPARTMENT:			//区画情報

                List<Compartment>mdlCompartment = Compartment.find.where(searchString).order(orderString).findList();

                for (Compartment mdldata : mdlCompartment) {
                    mdlDataList.add(mdldata);
                }

                break;
            case InfoKindConst.NOUHIOFCROP:			//生産物農肥情報

                List<NouhiOfCrop>mdlNouhiOfCrop = NouhiOfCrop.find.where(searchString).order(orderString).findList();

                for (NouhiOfCrop mdldata : mdlNouhiOfCrop) {
                    mdlDataList.add(mdldata);
                }

                break;
            case InfoKindConst.WORK:				//作業情報

                List<Work>mdlWork = Work.find.where(searchString).order(orderString).findList();

                for (Work mdldata : mdlWork) {
                    mdlDataList.add(mdldata);
                }

                break;
            case InfoKindConst.NOUHI:				//農肥情報

                List<Nouhi>mdlNouhi = Nouhi.find.where(searchString).order(orderString).findList();

                for (Nouhi mdldata : mdlNouhi) {
                    mdlDataList.add(mdldata);
                }

                break;
            case InfoKindConst.KIKI:				//機器情報

                List<Kiki>mdlKiki = Kiki.find.where(searchString).order(orderString).findList();

                for (Kiki mdldata : mdlKiki) {
                    mdlDataList.add(mdldata);
                }

                break;
        }


        return mdlDataList;

    }

    /**
     * 情報種別
     *
     * @author SUN FOR YOU.Ltd
     *
     */
    public static class InfoKindConst {

        /**
         * 生産物情報
         */
        public static final int  CROP 			= 1;

        /**
         * 区画情報
         */
        public static final int  COMPARTMENT	= 2;

        /**
         * 生産物農肥情報
         */
        public static final int  NOUHIOFCROP	= 3;

        /**
         * 作業情報
         */
        public static final int  WORK			= 4;

        /**
         * 農肥情報
         */
        public static final int  NOUHI			= 5;

        /**
         * 機器情報
         */
        public static final int  KIKI			= 6;
    }
}
