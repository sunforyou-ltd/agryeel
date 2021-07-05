package compornent;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import models.Compartment;
import models.CompartmentStatus;
import models.CompartmentWorkChainStatus;
import models.Sequence;
import models.Work;
import models.WorkChainItem;
import models.WorkDiary;
import models.WorkLastTime;
import models.WorkPlan;
import play.Logger;
import play.mvc.Http.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;


/**
 * 【AGRYEEL】作業記録共通項目
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class CommonWorkDiary {


    /**
     * HTTPセッション情報
     */
    public Session session;
    /**
     * ResultJsonData
     */
    public ObjectNode resultJson;

    /**
     * 区画ID
     */
    public double kukakuId;

    /**
     * 作業ID
     */
    public double workId;

    /**
     * 作業記録
     */
    public WorkDiary workDiary = null;
    /**
     * 作業指示
     */
    public WorkPlan workPlan = null;

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
    public CommonWorkDiary(Session session, ObjectNode resultJson) {

        this.session 	= session;
        this.resultJson = resultJson;

    }


    /**
     * 初期処理
     */
    public void init() {

    	if (!"".equals(session.get(AgryeelConst.SessionKey.WORKDIARYID))) {

            double workDiaryId 	= Double.parseDouble(session.get(AgryeelConst.SessionKey.WORKDIARYID));

            this.workDiary		= WorkDiary.find.where().eq("work_diary_id", workDiaryId).findUnique();

    	}
    	else if (!"".equals(session.get(AgryeelConst.SessionKey.WORKPLANID))) {

        double workPlanId  = Double.parseDouble(session.get(AgryeelConst.SessionKey.WORKPLANID));

        this.workPlan = WorkPlan.getWorkPlanById(workPlanId);

      }

      getCompartment();					//区画情報を取得する
      getWork();							//作業情報を取得する
      getAccount();						//アカウント情報を取得する
      settingDetailSettingKind();			//詳細情報設定種別を設定する

      Date workDate;			//作業日
      Timestamp start;
      Timestamp end;
      SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd");
      SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

      if (this.workDiary != null) {	/* 作業記録編集の場合 */

      	workDate 		= this.workDiary.workDate;
      	start       = this.workDiary.workStartTime;
        end         = this.workDiary.workEndTime;

        if (this.workDiary.workStartTime == null) { //作業開始日時がNULLの場合
          Calendar  cStart = Calendar.getInstance();
          cStart.setTime(workDate);
          start     = new java.sql.Timestamp(cStart.getTime().getTime());
        }
        if (this.workDiary.workEndTime == null)   { //作業終了日時がNULLの場合
          Calendar  cEnd = Calendar.getInstance();
          cEnd.setTime(workDate);
          end       = new java.sql.Timestamp(cEnd.getTime().getTime());
        }

      }
      else if (this.workPlan != null) { /* 作業指示編集の場合 */

        workDate    = this.workPlan.workDate;
        start       = this.workPlan.workStartTime;
        end         = this.workPlan.workEndTime;

        if (this.workPlan.workStartTime == null) { //作業開始日時がNULLの場合
          Calendar  cStart = Calendar.getInstance();
          cStart.setTime(workDate);
          start     = new java.sql.Timestamp(cStart.getTime().getTime());
        }
        if (this.workPlan.workEndTime == null)   { //作業終了日時がNULLの場合
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

      resultJson.put("workName"   , this.work.workName);
      resultJson.put("workDate"		, sdf.format(workDate));
      resultJson.put("startDate"  , sdf.format(start));
      resultJson.put("startTime"  , sdf2.format(start));
      resultJson.put("endDate"    , sdf.format(end));
      resultJson.put("endTime"    , sdf2.format(end));
      resultJson.put("workDiaryId"	, session.get(AgryeelConst.SessionKey.WORKDIARYID));
      resultJson.put("workPlanId"  , session.get(AgryeelConst.SessionKey.WORKPLANID));

    }

    /**
     * 作業記録保存
     */
    synchronized public void commit(JsonNode input, WorkDiary wkd, Work wk, double editWorkDiaryId, double pKukakuId) {

        SimpleDateFormat sdf	=	new SimpleDateFormat("yyyyMMdd");
        double workDiaryId		=	editWorkDiaryId;
        this.work = wk;

        int mode = input.get("mode").asInt();

        Compartment ct = Compartment.getCompartmentInfo(pKukakuId);

        if (workDiaryId == 0) {	/* 新規の場合、採番する */
            Sequence sequence 	= Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKDIARYID);							//最新シーケンス値の取得
            workDiaryId			= sequence.sequenceValue;
        }

        int workId = Integer.parseInt(input.get("workId").asText());	//作業IDの取得

        /* 共通項目 */
        wkd.workDiaryId		=	workDiaryId;																			  //作業記録ID
        wkd.workId			=	workId;																					      //作業ID
        //複数区画対応
        //wkd.kukakuId		=	Double.parseDouble(input.get("workKukaku").asText());	//区画ID
        wkd.kukakuId    = pKukakuId;                                              //区画ID
        if (editWorkDiaryId == 0) {
          wkd.workPlanFlag = AgryeelConst.WORKPLANFLAG.WORKDIARYCOMMIT;
        }

        if (mode == AgryeelConst.WorkDiaryMode.WORKING) {

          UserComprtnent uc = new UserComprtnent();
          int getAccount = uc.GetAccountData(session.get(AgryeelConst.SessionKey.ACCOUNTID));

          Calendar cal = Calendar.getInstance();
          long to   = cal.getTime().getTime();
          long from = uc.accountData.workStartTime.getTime();

          long diff = ( to - from  ) / (1000 * 60 );

          wkd.workTime    = (int)diff;
          wkd.workStartTime = new java.sql.Timestamp(uc.accountData.workStartTime.getTime());
          wkd.workEndTime = new java.sql.Timestamp(cal.getTime().getTime());

          wkd.accountId   = uc.accountData.accountId;

          wkd.workDate  = new java.sql.Date(cal.getTime().getTime());

          //------------ ヘルスケア ----------
          if (input.get("steps") != null) { //歩数
            wkd.numberOfSteps = input.get("steps").asLong();
          }
          if (input.get("distance") != null) { //距離
            wkd.distance = input.get("distance").asDouble();
          }
          if (input.get("calorie") != null) { //カロリー
            wkd.calorie = input.get("calorie").asInt();
          }
          if (input.get("heartRate") != null) { //心拍数
            wkd.heartRate = input.get("heartRate").asInt();
          }
          Logger.info("[ WORKING ENDED ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{} STEPS:{} DISTANCE:{} CALORIE:{} HEARTRATE:{}"
              , uc.accountData.accountId, uc.accountData.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName
              , wkd.numberOfSteps, wkd.distance, wkd.calorie, wkd.heartRate
              );

        }
        else {
          try {
              wkd.workDate  = new java.sql.Date(sdf.parse(input.get("workDate").asText().replace("/", "")).getTime());
          } catch (ParseException e) {
            Calendar cal = Calendar.getInstance();
            wkd.workDate  = new java.sql.Date(cal.getTime().getTime());
          }
          wkd.workTime    = Integer.parseInt(input.get("workTime").asText());
          wkd.accountId   = input.get("workAccount").asText();                                //担当者

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
            wkd.workStartTime  = new java.sql.Timestamp(cStart.getTime().getTime());
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
            wkd.workEndTime  = new java.sql.Timestamp(cEnd.getTime().getTime());
          }
          UserComprtnent uc = new UserComprtnent();
          int getAccount = uc.GetAccountData(session.get(AgryeelConst.SessionKey.ACCOUNTID));
          if (editWorkDiaryId == 0) { /* 新規入力の場合 */
            Logger.info("[ WORKDIARY NEW ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{} WORKDIARYID:{}", uc.accountData.accountId, uc.accountData.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName, workDiaryId);
          }
          else {
            Logger.info("[ WORKDIARY EDIT ] ID:{} NAME:{} KUKAKUID:{} KUKAKUNAME:{} WORKID:{} WORKNAME:{} WORKDIARYID:{}", uc.accountData.accountId, uc.accountData.acountName, ct.kukakuId, ct.kukakuName, wk.workId, wk.workName, workDiaryId);
          }
        }

        //区画IDから区画状況情報を取得する
        CompartmentStatus compartmentStatusData = FieldComprtnent.getCompartmentStatus(wkd.kukakuId);
        if (compartmentStatusData != null) {
          CompartmentWorkChainStatus cws = compartmentStatusData.getWorkChainStatus();
          if (compartmentStatusData.katadukeDate != null) {
            Date katadukeDate = DateUtils.truncate(compartmentStatusData.katadukeDate, Calendar.DAY_OF_MONTH);
            Date workDate = DateUtils.truncate(wkd.workDate, Calendar.DAY_OF_MONTH);
            if (!workDate.before(katadukeDate)) {
              if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
                cws.workEndId = String.valueOf(wk.workId);
              }
              else {
                String workEndId = cws.workEndId;
                boolean endidExists = false;
                if (workEndId != null && !"".equals(workEndId)) {
                  String[] endIds = workEndId.split(",");
                  for (String endId : endIds) {
                    if (Double.parseDouble(endId) == wk.workId) {
                      endidExists = true;
                      break;
                    }
                  }
                  if (endidExists == false) {
                    cws.workEndId += "," + String.valueOf(wk.workId);
                  }
                }
                else {
                  cws.workEndId = String.valueOf(wk.workId);
                }
              }
            }
            else if (workDate.compareTo(katadukeDate) == 0) {
              if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
                cws.workEndId = String.valueOf(wk.workId);
              }
            }
          }
          else {
            if (wk.workTemplateId == AgryeelConst.WorkTemplate.END) {
              cws.workEndId = String.valueOf(wk.workId);
            }
          }
          cws.update();
        }

        wkd.kukakuStatusUpdate 	= AgryeelConst.UpdateFlag.NONE;															//区画状況照会反映フラグ
        wkd.motochoUpdate 		= AgryeelConst.UpdateFlag.NONE;															//元帳照会反映フラグ

    }
    /**
     * 作業計画保存
     */
    public void plan(JsonNode input, WorkPlan wkd, Work wk, double pKukakuId) {

        SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
        Sequence sequence     = Sequence.GetSequenceValue(Sequence.SequenceIdConst.WORKPLANID); //最新シーケンス値の取得
        double workPlanId     = sequence.sequenceValue;
        this.work = wk;

        int workId = Integer.parseInt(input.get("workId").asText());  //作業IDの取得

        /* 共通項目 */
        wkd.workPlanId  = workPlanId;                                           //作業計画ID
        wkd.workId      = workId;                                               //作業ID
        wkd.kukakuId    = pKukakuId;                                            //区画ID

        Calendar cal = Calendar.getInstance();
        int workPlanType = input.get("workPlanType").asInt();
        if (workPlanType == AgryeelConst.WORKPLANFLAG.WORKDIARYWATCH) {
          wkd.workTime    = 0;
          wkd.workDate  = new java.sql.Date(cal.getTime().getTime());
        }
        else {
          wkd.workTime    = Integer.parseInt(input.get("workTime").asText());
          try {
            wkd.workDate  = new java.sql.Date(sdf.parse(input.get("workDate").asText().replace("/", "")).getTime());
          } catch (ParseException e) {
            wkd.workDate  = new java.sql.Date(cal.getTime().getTime());
          }
        }

        wkd.accountId   = input.get("workAccount").asText();                                //担当者

        wkd.workStartTime  = new java.sql.Timestamp(cal.getTime().getTime());

        wkd.kukakuStatusUpdate  = AgryeelConst.UpdateFlag.NONE;                           //区画状況照会反映フラグ
        wkd.motochoUpdate     = AgryeelConst.UpdateFlag.NONE;                             //元帳照会反映フラグ
        wkd.workPlanFlag      = workPlanType;                                             //作業計画フラグ

    }

    /**
     * 区画情報を取得する
     */
    public void getCompartment() {

        double kukakuId 	= 0;
        double farmId	 	= Double.parseDouble(session.get(AgryeelConst.SessionKey.FARMID));

        if (this.workDiary != null) {	/* 作業記録編集の場合 */
        	kukakuId		= this.workDiary.kukakuId;

          this.kukakuId   = kukakuId;

          /* 区画IDから区画情報を取得する */
          Compartment kukakuData = (Compartment)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.COMPARTMENT, "kukaku_id = " + kukakuId + "AND farm_id = " + farmId );
          resultJson.put("kukakuId", kukakuId);
          if (kukakuData != null) { resultJson.put("kukakuName", kukakuData.kukakuName); }  //区画情報が存在する場合、区画名を格納する

        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */
          kukakuId    = this.workPlan.kukakuId;

          this.kukakuId   = kukakuId;

          /* 区画IDから区画情報を取得する */
          Compartment kukakuData = (Compartment)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.COMPARTMENT, "kukaku_id = " + kukakuId + "AND farm_id = " + farmId );
          resultJson.put("kukakuId", kukakuId);
          if (kukakuData != null) { resultJson.put("kukakuName", kukakuData.kukakuName); }  //区画情報が存在する場合、区画名を格納する

        }
        else {

            /* 該当区画IDの取得 */
            String kukaku = session.get(AgryeelConst.SessionKey.KUKAKUID);
            if (kukaku != null && !"".equals(kukaku)) {
              String[] kukakus = kukaku.split(",");
              String   kukakuname = "";

              for (String id : kukakus) {

                kukakuId    = Double.parseDouble(id);
                this.kukakuId   = kukakuId;
                /* 区画IDから区画情報を取得する */
                Compartment kukakuData = (Compartment)CommonGetWorkDiaryData.GetData(CommonGetWorkDiaryData.InfoKindConst.COMPARTMENT, "kukaku_id = " + kukakuId + "AND farm_id = " + farmId );
                if (kukakuData != null) {
                  if (!"".equals(kukakuname)) {
                    kukakuname += ",";
                  }
                  kukakuname += kukakuData.kukakuName;
                }
              }

              if (this.apiFlg) { //APIの場合は、単一区画の為、Int化する
                resultJson.put("kukakuId"   , Double.parseDouble(kukaku));
              }
              else {
                resultJson.put("kukakuId"   , kukaku);
              }
              resultJson.put("kukakuName" , kukakuname);
            }

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

        if (this.workDiary != null) {	/* 作業記録編集の場合 */
        	workId			= (int) this.workDiary.workId;
          kukakuId    = this.workDiary.kukakuId;
        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */
          workId      = (int) this.workPlan.workId;
          kukakuId    = this.workPlan.kukakuId;
        }
        else {
            workId    = Double.parseDouble(session.get(AgryeelConst.SessionKey.WORKID));
            String kukaku = session.get(AgryeelConst.SessionKey.KUKAKUID);
            if (kukaku != null && !"".equals(kukaku)) {
              String[] kukakus = kukaku.split(",");
              String   kukakuname = "";

              for (String id : kukakus) {

                kukakuId    = Double.parseDouble(id);
                this.kukakuId   = kukakuId;
                break;

              }
            }
//          kukakuId  = Double.parseDouble(session.get(AgryeelConst.SessionKey.KUKAKUID));
        }

        this.workId   = workId;

        resultJson.put("workId", workId);

        Work work = Work.getWork(workId);
        this.work = work;

        resultJson.put("workTemplateId", (int)work.workTemplateId);

        Compartment compartmentData = FieldComprtnent.getCompartment(kukakuId);

        if (compartmentData != null) {
          CompartmentWorkChainStatus compartmentWorkChainStatus = WorkChainCompornent.getCompartmentWorkChainStatus(compartmentData.kukakuId);
          WorkChainItem wci = WorkChainItem.getWorkChainItemOfWorkId(compartmentWorkChainStatus.workChainId, workId);
          WorkLastTime wlt = WorkChainCompornent.getWorkLastTime(workId, farmId, compartmentWorkChainStatus.cropId);

          //----- 作業時間 -----
          if (this.workDiary != null) { /* 作業記録編集の場合 */
            resultJson.put("workTime" , this.workDiary.workTime);
          }
          else if (this.workPlan != null) { /* 作業計画編集の場合 */
            resultJson.put("workTime" , this.workPlan.workTime);
          }
          else {
            if (wlt != null) {
              resultJson.put("workTime" , wlt.workTime);
            }
            else {
              resultJson.put("workTime" , 0);
            }
          }
          //----- 使用可能機器種別-----
          if (wci != null) {
            resultJson.put("kikiKind" , wci.onUseKikiKind);
          }
          else {
            resultJson.put("kikiKind" , "0");
          }
        }
    }

    /**
     * 担当者情報を取得する
     */
    public void getAccount() {

        if (this.workDiary != null) {	/* 作業記録編集の場合 */
            /* 作業担当者を担当者に設定する */
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTID, String.valueOf(this.workDiary.accountId));
            UserComprtnent accountComprtnent = new UserComprtnent();
            accountComprtnent.GetAccountData(this.workDiary.accountId);
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTNAME, accountComprtnent.accountData.acountName);
        }
        else if (this.workPlan != null) { /* 作業計画編集の場合 */
          /* 作業担当者を担当者に設定する */
          resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTID, String.valueOf(this.workPlan.accountId));
          UserComprtnent accountComprtnent = new UserComprtnent();
          if ("".equals(this.workPlan.accountId)) {
            resultJson.put(AgryeelConst.WorkDiary.Field.ACCOUNTNAME, "未選択");
          }
          else {
            accountComprtnent.GetAccountData(this.workPlan.accountId);
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
