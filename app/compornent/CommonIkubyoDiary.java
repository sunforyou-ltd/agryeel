package compornent;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import models.IkubyoDiary;
import models.IkubyoPlan;
import models.NaeNoManage;
import models.NaeStatus;
import models.Sequence;
import models.Work;
import models.WorkChainItem;
import play.Logger;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;


/**
 * 【AGRYEEL】育苗記録共通項目
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CommonIkubyoDiary {


    /**
     * HTTPセッション情報
     */
    public Session session;
    /**
     * ResultJsonData
     */
    public ObjectNode resultJson;

    /**
     * 苗No
     */
    public String naeNo;

    /**
     * 作業ID
     */
    public double workId;

    /**
     * 育苗記録
     */
    public IkubyoDiary ikubyoDiary = null;

    /**
     * 育苗計画
     */
    public IkubyoPlan ikubyoPlan = null;

    public Work work = null;

    /**
     * APIフラグ
     */
    public Boolean apiFlg = false;

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public CommonIkubyoDiary(Session session, ObjectNode resultJson) {

        this.session 	= session;
        this.resultJson = resultJson;

    }


    /**
     * 初期処理
     */
    public void init() {

      if (!"".equals(session.get(AgryeelConst.SessionKey.IKUBYODIARYID))) {
        double ikubyoDiaryId 	= Double.parseDouble(session.get(AgryeelConst.SessionKey.IKUBYODIARYID));
        this.ikubyoDiary		= IkubyoDiary.find.where().eq("ikubyo_diary_id", ikubyoDiaryId).findUnique();
      }
      else if (!"".equals(session.get(AgryeelConst.SessionKey.IKUBYOPLANID))) {
        double ikubyoPlanId  = Double.parseDouble(session.get(AgryeelConst.SessionKey.IKUBYOPLANID));
        this.ikubyoPlan = IkubyoPlan.getIkubyoPlanById(ikubyoPlanId);
      }

      getWork();					//作業情報を取得する
      getNae();						//苗情報を取得する
      getAccount();					//アカウント情報を取得する
      settingDetailSettingKind();	//詳細情報設定種別を設定する

      Date workDate;			//作業日
      Timestamp start;
      Timestamp end;
      SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

      if (this.ikubyoDiary != null) {	/* 育苗記録編集の場合 */

      	workDate 	= this.ikubyoDiary.workDate;
      	start       = this.ikubyoDiary.workStartTime;
        end         = this.ikubyoDiary.workEndTime;

        if (this.ikubyoDiary.workStartTime == null) { //作業開始日時がNULLの場合
          Calendar  cStart = Calendar.getInstance();
          cStart.setTime(workDate);
          start     = new java.sql.Timestamp(cStart.getTime().getTime());
        }
        if (this.ikubyoDiary.workEndTime == null)   { //作業終了日時がNULLの場合
          Calendar  cEnd = Calendar.getInstance();
          cEnd.setTime(workDate);
          end       = new java.sql.Timestamp(cEnd.getTime().getTime());
        }

      }
      else if (this.ikubyoPlan != null) { /* 育苗指示編集の場合 */

        workDate    = this.ikubyoPlan.workDate;
        start       = this.ikubyoPlan.workStartTime;
        end         = this.ikubyoPlan.workEndTime;

        if (this.ikubyoPlan.workStartTime == null) { //作業開始日時がNULLの場合
          Calendar  cStart = Calendar.getInstance();
          cStart.setTime(workDate);
          start     = new java.sql.Timestamp(cStart.getTime().getTime());
        }
        if (this.ikubyoPlan.workEndTime == null)   { //作業終了日時がNULLの場合
          Calendar  cEnd = Calendar.getInstance();
          cEnd.setTime(workDate);
          end       = new java.sql.Timestamp(cEnd.getTime().getTime());
        }

      }
      else {

        Calendar  cal = Calendar.getInstance();
      	workDate 		= new java.sql.Date(cal.getTime().getTime());
      	start       = new java.sql.Timestamp(cal.getTime().getTime());
      	end         = new java.sql.Timestamp(cal.getTime().getTime());

      }

      resultJson.put("workName"     , this.work.workName);
      resultJson.put("workDate"	    , sdf.format(workDate));
      resultJson.put("startDate"    , sdf.format(start));
      resultJson.put("startTime"    , sdf2.format(start));
      resultJson.put("endDate"      , sdf.format(end));
      resultJson.put("endTime"      , sdf2.format(end));
      resultJson.put("ikubyoDiaryId", session.get(AgryeelConst.SessionKey.IKUBYODIARYID));
      resultJson.put("ikubyoPlanId" , session.get(AgryeelConst.SessionKey.IKUBYOPLANID));

    }

    /**
     * 育苗記録保存
     */
    synchronized public void commit(JsonNode input, IkubyoDiary ikd, Work wk, double editIkubyoDiaryId, String pNaeNo) {

        SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");
        double ikubyoDiaryId	=	editIkubyoDiaryId;
        double farmId			=	Double.parseDouble(session.get(AgryeelConst.SessionKey.FARMID));
        String naeNo			=	pNaeNo;
        String sequenceStr		=	"";
        String sequence36		=	"";
        this.work = wk;

        int mode = input.get("mode").asInt();

        if (ikubyoDiaryId == 0) {	/* 新規の場合、採番する */
            Sequence sequence 	= Sequence.GetSequenceValue(Sequence.SequenceIdConst.IKUBYODIARYID);	//最新シーケンス値の取得
            ikubyoDiaryId		= sequence.sequenceValue;
            if (wk.workTemplateId == AgryeelConst.WorkTemplate.NAEHASHUIK) {	//苗播種の場合、苗Noを採番する
              NaeNoManage naeNoManage = NaeNoManage.GetSequenceValue(farmId);							//最新シーケンス値の取得
              sequenceStr = Integer.toString((int)naeNoManage.sequenceValue, 36);
              sequence36 = String.format("%5s", sequenceStr).replace(" ", "0");
              naeNo = String.format("%04d", (int)farmId) + "-" + sequence36;
            }
        }

        int workId = Integer.parseInt(input.get("workId").asText());	//作業IDの取得

        /* 共通項目 */
        ikd.ikubyoDiaryId	=	ikubyoDiaryId;							//作業記録ID
        ikd.workId			=	workId;									//作業ID
        ikd.naeNo			=	naeNo;									//苗No

        if (mode == AgryeelConst.WorkDiaryMode.WORKING) {

          UserComprtnent uc = new UserComprtnent();
          int getAccount = uc.GetAccountData(session.get(AgryeelConst.SessionKey.ACCOUNTID));

          Calendar cal = Calendar.getInstance();
          long to   = cal.getTime().getTime();
          long from = uc.accountData.workStartTime.getTime();

          long diff = ( to - from  ) / (1000 * 60 );

          ikd.workTime    = (int)diff;
          ikd.workStartTime = new java.sql.Timestamp(uc.accountData.workStartTime.getTime());
          ikd.workEndTime = new java.sql.Timestamp(cal.getTime().getTime());

          ikd.accountId   = uc.accountData.accountId;

          ikd.workDate  = new java.sql.Date(cal.getTime().getTime());

          //------------ ヘルスケア ----------
          if (input.get("steps") != null) { //歩数
            ikd.numberOfSteps = input.get("steps").asLong();
          }
          if (input.get("distance") != null) { //距離
            ikd.distance = input.get("distance").asDouble();
          }
          if (input.get("calorie") != null) { //カロリー
            ikd.calorie = input.get("calorie").asInt();
          }
          if (input.get("heartRate") != null) { //心拍数
            ikd.heartRate = input.get("heartRate").asInt();
          }
          Logger.info("[ WORKING ENDED ] ID:{} NAME:{} NAENO:{} WORKID:{} WORKNAME:{} STEPS:{} DISTANCE:{} CALORIE:{} HEARTRATE:{}"
              , uc.accountData.accountId, uc.accountData.acountName, naeNo, wk.workId, wk.workName
              , ikd.numberOfSteps, ikd.distance, ikd.calorie, ikd.heartRate
              );

        }
        else {
          try {
              ikd.workDate  = new java.sql.Date(sdf.parse(input.get("workDate").asText().replace("/", "")).getTime());
          } catch (ParseException e) {
            Calendar cal = Calendar.getInstance();
            ikd.workDate  = new java.sql.Date(cal.getTime().getTime());
          }
          ikd.workTime    = Integer.parseInt(input.get("workTime").asText());
          ikd.accountId   = input.get("workAccount").asText();                                //担当者

          //----- 作業開始日時 -----
          String[] startDate = input.get("startDate").asText().split("/");
          String[] startTime = input.get("startTime").asText().split(":");

          Logger.debug("[ WORK START ] ? ?", input.get("startDate").asText(), input.get("startTime").asText());

          if (startDate.length == 3 && startTime.length == 2) {
            Calendar cStart = Calendar.getInstance();
            cStart.set( Integer.parseInt(startDate[0])
                      , (Integer.parseInt(startDate[1]) - 1)
                      , Integer.parseInt(startDate[2])
                      , Integer.parseInt(startTime[0])
                      , Integer.parseInt(startTime[1])
                      , 0);
            ikd.workStartTime  = new java.sql.Timestamp(cStart.getTime().getTime());
          }

          //----- 作業終了日時 -----
          String[] endDate = input.get("endDate").asText().split("/");
          String[] endTime = input.get("endTime").asText().split(":");

          Logger.debug("[ WORK END ] ? ?", input.get("endDate").asText(), input.get("endTime").asText());

          if (endDate.length == 3 && endTime.length == 2) {
            Calendar cEnd = Calendar.getInstance();
            cEnd.set( Integer.parseInt(endDate[0])
                      , (Integer.parseInt(endDate[1]) - 1)
                      , Integer.parseInt(endDate[2])
                      , Integer.parseInt(endTime[0])
                      , Integer.parseInt(endTime[1])
                      , 0);
            ikd.workEndTime  = new java.sql.Timestamp(cEnd.getTime().getTime());
          }
          UserComprtnent uc = new UserComprtnent();
          int getAccount = uc.GetAccountData(session.get(AgryeelConst.SessionKey.ACCOUNTID));
          if (editIkubyoDiaryId == 0) { /* 新規入力の場合 */
            Logger.info("[ IKUBYODIARY NEW ] ID:{} NAME:{} NAENO:{} WORKID:{} WORKNAME:{} IKUBYODIARYID:{}", uc.accountData.accountId, uc.accountData.acountName, naeNo, wk.workId, wk.workName, ikubyoDiaryId);
          }
          else {
            Logger.info("[ IKUBYODIARY EDIT ] ID:{} NAME:{} NAENO:{} WORKID:{} WORKNAME:{} IKUBYODIARYID:{}", uc.accountData.accountId, uc.accountData.acountName, naeNo, wk.workId, wk.workName, ikubyoDiaryId);
          }
        }

        //苗Noから苗状況情報を取得する
        NaeStatus naeStatus = NaeStatus.getStatusOfNae(ikd.naeNo);
        if (naeStatus == null) {
          naeStatus = new NaeStatus();
          naeStatus.naeNo = ikd.naeNo;
          naeStatus.farmId = farmId;
          naeStatus.hinsyuId = ikd.hinsyuId;
          naeStatus.hinsyuName = "";
          naeStatus.nowEndWork = "";
          naeStatus.workColor = "";
          naeStatus.save();
        }

        ikd.naeStatusUpdate 	= AgryeelConst.UpdateFlag.NONE;		//苗状況照会反映フラグ

    }
    /**
     * 育苗計画保存
     */
    public void plan(JsonNode input, IkubyoPlan ip, Work wk, String pNaeNo) {

        SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
        Sequence sequence     = Sequence.GetSequenceValue(Sequence.SequenceIdConst.IKUBYOPLANID); //最新シーケンス値の取得
        double ikubyoPlanId   = sequence.sequenceValue;
        double farmId			=	Double.parseDouble(session.get(AgryeelConst.SessionKey.FARMID));
        String naeNo			=	pNaeNo;
        String sequenceStr		=	"";
        String sequence36		=	"";
        this.work = wk;

        int workId = Integer.parseInt(input.get("workId").asText());  //作業IDの取得

        if ("new".equals(pNaeNo) &&
            wk.workTemplateId == AgryeelConst.WorkTemplate.NAEHASHUIK) {	//苗播種の場合、苗Noを採番する
          NaeNoManage naeNoManage = NaeNoManage.GetSequenceValue(farmId);							//最新シーケンス値の取得
          sequenceStr = Integer.toString((int)naeNoManage.sequenceValue, 36);
          sequence36 = String.format("%5s", sequenceStr).replace(" ", "0");
          naeNo = String.format("%04d", (int)farmId) + "-" + sequence36;
        }

        /* 共通項目 */
        ip.ikubyoPlanId  = ikubyoPlanId;                                      //育苗計画ID
        ip.workId        = workId;                                            //作業ID
        ip.naeNo         = naeNo;                                             //苗No

        Calendar cal = Calendar.getInstance();
        int ikubyoPlanType = input.get("ikubyoPlanType").asInt();
        if (ikubyoPlanType == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH) {
          ip.workTime    = 0;
          ip.workDate  = new java.sql.Date(cal.getTime().getTime());
        }
        else {
          ip.workTime    = Integer.parseInt(input.get("workTime").asText());
          try {
            ip.workDate  = new java.sql.Date(sdf.parse(input.get("workDate").asText().replace("/", "")).getTime());
          } catch (ParseException e) {
            ip.workDate  = new java.sql.Date(cal.getTime().getTime());
          }
        }

        ip.accountId   = input.get("workAccount").asText();                                //担当者

        ip.workStartTime  = new java.sql.Timestamp(cal.getTime().getTime());

        ip.naeStatusUpdate  = AgryeelConst.UpdateFlag.NONE;                           //育苗状況照会反映フラグ

    }

    /**
     * 苗情報を取得する
     */
    public void getNae() {

        String naeNo = "";
        String naeName = "";
        double farmId = Double.parseDouble(session.get(AgryeelConst.SessionKey.FARMID));

        if (this.ikubyoDiary != null) {	/* 育苗記録編集の場合 */
          naeNo = this.ikubyoDiary.naeNo;
          NaeStatus ns = NaeStatus.find.where().eq("nae_no", naeNo).findUnique();
          if (ns != null) {
            String[] sNaeNos = naeNo.split("-");
            naeName = ns.hinsyuName + "(" + sNaeNos[1] + ")";
          }
        }
        else if (this.ikubyoPlan != null) { /* 育苗計画編集の場合 */
          naeNo = this.ikubyoPlan.naeNo;
          NaeStatus ns = NaeStatus.find.where().eq("nae_no", naeNo).findUnique();
          if (ns != null) {
            String[] sNaeNos = naeNo.split("-");
            naeName = ns.hinsyuName + "(" + sNaeNos[1] + ")";
          }
        }
        else {
          naeNo = session.get(AgryeelConst.SessionKey.NAENO);
          if (!"".equals(naeNo)) {
            NaeStatus ns = NaeStatus.find.where().eq("nae_no", naeNo).findUnique();
            if (ns != null) {
              String[] sNaeNos = naeNo.split("-");
              naeName = ns.hinsyuName + "(" + sNaeNos[1] + ")";
            }
          }
        }
        this.naeNo = naeNo;
        if ("".equals(naeNo) && this.work.workTemplateId == AgryeelConst.WorkTemplate.HAIKIIK) {
        }
        else {
          resultJson.put("naeNo", naeNo);
          resultJson.put("naeName", naeName);
        }
    }

    /**
     * 作業情報を取得する
     */
    public void getWork() {

        /* 該当作業IDの取得 */
        double workId = 0;
        double kukakuId   = 0;

        double farmId   = Double.parseDouble(session.get(AgryeelConst.SessionKey.FARMID));

        if (this.ikubyoDiary != null) {		/* 育苗記録編集の場合 */
          workId = (int) this.ikubyoDiary.workId;
        }
        else if (this.ikubyoPlan != null) { /* 育苗計画編集の場合 */
          workId = (int) this.ikubyoPlan.workId;
        }
        else {
            workId    = Double.parseDouble(session.get(AgryeelConst.SessionKey.WORKID));
        }

        this.workId   = workId;

        resultJson.put("workId", workId);

        Work work = Work.getWork(workId);
        this.work = work;

        resultJson.put("workTemplateId", (int)work.workTemplateId);

        WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(AgryeelConst.IkubyoInfo.WORKCHAINID, workId);

        //----- 作業時間 -----
        if (this.ikubyoDiary != null) { /* 育苗記録編集の場合 */
          resultJson.put("workTime" , this.ikubyoDiary.workTime);
        }
        else if (this.ikubyoPlan != null) { /* 育苗計画編集の場合 */
          resultJson.put("workTime" , this.ikubyoPlan.workTime);
        }
        else {
          resultJson.put("workTime" , 0);
        }
        //----- 使用可能機器種別-----
        if (wci != null) {
          resultJson.put("kikiKind" , wci.onUseKikiKind);
        }
        else {
          resultJson.put("kikiKind" , "0");
        }
    }

    /**
     * 担当者情報を取得する
     */
    public void getAccount() {

        if (this.ikubyoDiary != null) {	/* 育苗記録編集の場合 */
            /* 作業担当者を担当者に設定する */
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTID, String.valueOf(this.ikubyoDiary.accountId));
            UserComprtnent accountComprtnent = new UserComprtnent();
            accountComprtnent.GetAccountData(this.ikubyoDiary.accountId);
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTNAME, accountComprtnent.accountData.acountName);
        }
        else if (this.ikubyoPlan != null) { /* 育苗計画編集の場合 */
          /* 作業担当者を担当者に設定する */
          resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTID, String.valueOf(this.ikubyoPlan.accountId));
          UserComprtnent accountComprtnent = new UserComprtnent();
          if ("".equals(this.ikubyoPlan.accountId)) {
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTNAME, "未選択");
          }
          else {
            accountComprtnent.GetAccountData(this.ikubyoPlan.accountId);
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTNAME, accountComprtnent.accountData.acountName);
          }
        }
        else {
            /* ログインアカウントを担当者に設定する */
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTID, session.get(AgryeelConst.SessionKey.ACCOUNTID));
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTNAME, session.get(AgryeelConst.SessionKey.ACCOUNTNAME));
        }

    }

    /**
     * 詳細情報設定種別を設定する
     */
    public void settingDetailSettingKind() {

        /* 詳細情報設定種別をコンビネーションに設定する */
        resultJson.put("detailSettingKind", AgryeelConst.WorkDiary.DetailSettingKind.COMBI);

    }
}
