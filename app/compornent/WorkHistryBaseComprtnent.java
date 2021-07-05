package compornent;

import java.util.ArrayList;
import java.util.List;

import models.WorkDiarySanpu;
import models.WorkHistryBase;
import play.Logger;

import com.avaje.ebean.Ebean;

/**
 * 【AGRYEEL】作業履歴共通コンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class WorkHistryBaseComprtnent {

	/** 作業履歴共通情報 */
	List<WorkHistryBase> listWorkHistryBase = null;
	/** 作業履歴共通情報スタック数 */
	public int iWorkHistryBaseCount				= 0;
	/** 農場ＩＤ */
	double farmId							= 0;
	/** 作業ＩＤ */
	double workId							= 0;
  /** 生産物ＩＤ */
  double cropId             = 0;


	/**
	 * コンストラクタ
	 */
	public WorkHistryBaseComprtnent(double farmId, double workId, double cropId) {

		clear();

		this.farmId = farmId;
		this.workId = workId;
    this.cropId = cropId;

    Logger.debug(" [ WorkHistryBaseComprtnent START ] ");
    Logger.debug(" [ FARM ] " + this.farmId);
    Logger.debug(" [ WORK ] " + this.workId);
    Logger.debug(" [ CROP ] " + this.cropId);

	}

	/**
	 * 初期化処理
	 */
	public void clear() {

		this.listWorkHistryBase = new ArrayList<WorkHistryBase>();
		this.iWorkHistryBaseCount = 0;

	}

	/**
	 * 対象となる作業履歴共通情報を作成する
	 * @param sanpu
	 */
	public void stack(WorkDiarySanpu sanpu) {

		WorkHistryBase workHistryBase = new WorkHistryBase();

		this.iWorkHistryBaseCount++;

		workHistryBase.farmId				       =	this.farmId;
		workHistryBase.workId				       =	this.workId;
    workHistryBase.cropId              =  this.cropId;
		workHistryBase.workHistrySequence	 =	this.iWorkHistryBaseCount;
		workHistryBase.sanpuMethod			   =	sanpu.sanpuMethod;
		workHistryBase.kikiId				       =	sanpu.kikiId;
		workHistryBase.attachmentId			   =	sanpu.attachmentId;
		workHistryBase.nouhiId				     =	sanpu.nouhiId;
		workHistryBase.bairitu				     =	sanpu.bairitu;
		workHistryBase.sanpuryo				     =	sanpu.sanpuryo;


		listWorkHistryBase.add(workHistryBase);

	}


	/**
	 * 作成した作業履歴情報を保存する
	 */
	public void update() {

        /* 現在保存されている作業履歴情報を削除する */
        Ebean.createSqlUpdate("DELETE FROM work_histry_base WHERE farm_id = :farmId AND work_id = :workId AND crop_id = :cropId")
        .setParameter("farmId", this.farmId).setParameter("workId", this.workId).setParameter("cropId", this.cropId).execute();

        for (WorkHistryBase workHistryBase : this.listWorkHistryBase) {

        	workHistryBase.save();

        }

	}

	/**
	 * 対象の作業履歴情報を取得する
	 */
	public void get() {

		clear();

		this.listWorkHistryBase 	= WorkHistryBase.find.where().eq("farm_id", this.farmId).eq("work_id", this.workId).eq("crop_id", this.cropId).orderBy("work_histry_sequence").findList();
		this.iWorkHistryBaseCount	= this.listWorkHistryBase.size();

	}


}
