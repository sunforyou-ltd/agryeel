package compornent;

import java.util.ArrayList;
import java.util.List;

import models.WorkDiaryDetail;
import models.WorkHistryDetail;
import play.Logger;

import com.avaje.ebean.Ebean;

/**
 * 【AGRYEEL】作業履歴詳細コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkHistryDetailComprtnent {

	/** 作業履歴詳細情報 */
	List<WorkHistryDetail> listWorkHistryDetail = null;
	/** 作業履歴詳細情報スタック数 */
	public int iWorkHistryDetailCount				= 0;
	/** 農場ＩＤ */
	double farmId							= 0;
	/** 作業ＩＤ */
	double workId							= 0;
  /** 生産物ＩＤ */
  double cropId             = 0;


	/**
	 * コンストラクタ
	 */
	public WorkHistryDetailComprtnent(double farmId, double workId, double cropId) {

		clear();

		this.farmId = farmId;
		this.workId = workId;
    this.cropId = cropId;

    Logger.debug(" [ WorkHistryDetailComprtnent START ] ");
    Logger.debug(" [ FARM ] " + this.farmId);
    Logger.debug(" [ WORK ] " + this.workId);
    Logger.debug(" [ CROP ] " + this.cropId);

	}

	/**
	 * 初期化処理
	 */
	public void clear() {

		this.listWorkHistryDetail = new ArrayList<WorkHistryDetail>();
		this.iWorkHistryDetailCount = 0;

	}

	/**
	 * 対象となる作業履歴詳細情報を作成する
	 * @param sanpu
	 */
	public void stack(WorkDiaryDetail detail) {

	  WorkHistryDetail workHistryDetail = new WorkHistryDetail();

		this.iWorkHistryDetailCount++;

		workHistryDetail.farmId				       =	this.farmId;
		workHistryDetail.workId				       =	this.workId;
		workHistryDetail.cropId              =  this.cropId;
		workHistryDetail.workHistrySequence	 =	this.iWorkHistryDetailCount;
		workHistryDetail.workDetailKind	     =	detail.workDetailKind;
    workHistryDetail.suryo               =  detail.suryo;
    workHistryDetail.sizaiId             =  detail.sizaiId;
    workHistryDetail.comment             =  detail.comment;
    workHistryDetail.syukakuNisugata     =  detail.syukakuNisugata;
    workHistryDetail.syukakuSitsu        =  detail.syukakuSitsu;
    workHistryDetail.syukakuSize         =  detail.syukakuSize;
    workHistryDetail.syukakuKosu         =  detail.syukakuKosu;
    workHistryDetail.shukakuRyo          =  detail.shukakuRyo;
    workHistryDetail.syukakuHakosu       =  detail.syukakuHakosu;
    workHistryDetail.syukakuNinzu        =  detail.syukakuNinzu;


    listWorkHistryDetail.add(workHistryDetail);

	}


	/**
	 * 作成した作業履歴情報を保存する
	 */
	public void update() {

        /* 現在保存されている作業履歴情報を削除する */
        Ebean.createSqlUpdate("DELETE FROM work_histry_detail WHERE farm_id = :farmId AND work_id = :workId AND crop_id = :cropId")
        .setParameter("farmId", this.farmId).setParameter("workId", this.workId).setParameter("cropId", this.cropId).execute();

        for (WorkHistryDetail workHistryDetail : this.listWorkHistryDetail) {

          workHistryDetail.save();

        }

	}

	/**
	 * 対象の作業履歴情報を取得する
	 */
	public void get() {

		clear();

		this.listWorkHistryDetail 	= WorkHistryDetail.find.where().eq("farm_id", this.farmId).eq("work_id", this.workId).eq("crop_id", this.cropId).orderBy("work_histry_sequence").findList();
		this.iWorkHistryDetailCount	= this.listWorkHistryDetail.size();

	}


}
