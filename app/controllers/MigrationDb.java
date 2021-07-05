package controllers;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import models.Account;
import models.Attachment;
import models.Belto;
import models.Compartment;
import models.CompartmentStatus;
import models.Crop;
import models.CropGroup;
import models.Farm;
import models.FarmGroup;
import models.Hinsyu;
import models.Kiki;
import models.Nouhi;
import models.Sequence;
import models.TimeLine;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.DateU;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

/* agryeeldb → agryelldb移行バッチ */
public class MigrationDb extends Controller {

	  private static final char PKG_SEPARATOR = '.';

	  private static final char DIR_SEPARATOR = '/';

	  private static final String CLASS_FILE_SUFFIX = ".class";

	  private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

	  /**
	   * 【AGRYELL】agryeeldb → agryelldb移行
	   */
	  public static Result migrationDb() {

        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */
        java.sql.Date defaultDate 			= DateU.GetNullDate();

        final DataSourceConfig dsConfig = new DataSourceConfig() {
            {
              setDriver("org.postgresql.Driver");
              setUrl("jdbc:postgresql://localhost:5432/agryeeldb");						/* 旧DB           */
              setUsername("agryeel");													/* 旧DBユーザ     */
              setPassword("agryeel");													/* 旧DBパスワード */
            }
          };

          EbeanServer server = EbeanServerFactory.create(new ServerConfig() {
            {
              setName("agryeeldb");
              setDataSourceConfig(dsConfig);
              setClasses(find("models"));
              setRegister(false);
              setDdlRun(false);
              setDdlGenerate(false);
              setDefaultServer(false);
            }
          });

          /* 生産者グループ */
          migrationFarmGroup(server);

          /* 生産者 */
          migrationFarm(server);

          /* アカウント */
          migrationAccount(server);

          /* 区画 */
          migrationCompartment(server);

          /* 生産物グループ */
          migrationCropGroup(server);

          /* 生産物 */
          migrationCrop(server);

          /* 農肥 */
          migrationNouhi(server);

          /* 品種 */
          migrationHinsyu(server);

          /* ベルト */
          migrationBelto(server);

          /* 機器 */
          migrationKiki(server);

          /* アタッチメント */
          migrationAttachment(server);

          /* シーケンス */
          migrationSequence(server);

          /* 区画状況 */
          //migrationCompartmentStatus(server);

          /* タイムライン */
          //migrationTimeLine(server);

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】生産者グループデータ移行
	 */
	private static Result migrationFarmGroup(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 生産者グループリストを取得する -----
        List<FarmGroup> groupList	= server.find(FarmGroup.class).order("farm_group_id").findList();

        //----- 生産者グループを作成する -----
        for (FarmGroup farmGroupOld : groupList) {

        	FarmGroup farmGroup 	= new FarmGroup();
        	farmGroup.farmGroupId	= farmGroupOld.farmGroupId;
        	farmGroup.farmGroupName	= farmGroupOld.farmGroupName;
        	farmGroup.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】生産者データ移行
	 */
	private static Result migrationFarm(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 生産者リストを取得する -----
        List<Farm> farmList	= server.find(Farm.class).order("farm_id").findList();

        //----- 生産者を作成する -----
        for (Farm farmOld : farmList) {

        	Farm farm 	= new Farm();
        	farm.farmId					= farmOld.farmId;
        	farm.farmName				= farmOld.farmName;
        	farm.farmGroupId			= farmOld.farmGroupId;
        	farm.representativeName		= farmOld.representativeName;
        	farm.postNo					= farmOld.postNo;
        	farm.prefectures			= farmOld.prefectures;
        	farm.address				= farmOld.address;
        	farm.tel					= farmOld.tel;
        	farm.responsibleMobileTel	= farmOld.responsibleMobileTel;
        	farm.fax					= farmOld.fax;
        	farm.mailAddressPC			= farmOld.mailAddressPC;
        	farm.mailAddressMobile		= farmOld.mailAddressMobile;
        	farm.url					= farmOld.url;
        	farm.registrationCode		= farmOld.registrationCode;
        	farm.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】アカウントデータ移行
	 */
	private static Result migrationAccount(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- アカウントリストを取得する -----
        List<Account> accountList	= server.find(Account.class).select("accountId,password,acountName,acountKana,part,"
        		+ "remark,mailAddress,birthday,managerRole,googleID,farmId,firstPage,loginCount,inputCount,clipMode").order("account_id").findList();

        //----- アカウントを作成する -----
        for (Account accountOld : accountList) {

        	Account account 		= new Account();
        	account.accountId		= accountOld.accountId;
        	account.password		= accountOld.password;
        	account.acountName		= accountOld.acountName;
        	account.acountKana		= accountOld.acountKana;
        	account.part			= accountOld.part;
        	account.remark			= accountOld.remark;
        	account.mailAddress		= accountOld.mailAddress;
        	account.birthday		= accountOld.birthday;
        	account.managerRole		= accountOld.managerRole;
        	account.menuRole		= 1;						/* TODO:初期値検討 */
        	account.googleID		= accountOld.googleID;
        	account.farmId			= accountOld.farmId;
        	account.firstPage		= accountOld.firstPage;
        	account.loginCount		= accountOld.loginCount;
        	account.inputCount		= accountOld.inputCount;
        	account.clipMode		= accountOld.clipMode;
        	account.workStartTime	= null;

        	account.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】区画データ移行
	 */
	private static Result migrationCompartment(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 区画リストを取得する -----
        List<Compartment> kukakuList	= server.find(Compartment.class).select("kukakuId,kukakuName,farmId").order("kukaku_id").findList();

        //----- 区画を作成する -----
        for (Compartment kukakuOld : kukakuList) {

        	Compartment kukaku 		= new Compartment();
        	kukaku.kukakuId			= kukakuOld.kukakuId;
        	kukaku.kukakuName		= kukakuOld.kukakuName;
        	kukaku.farmId			= kukakuOld.farmId;
        	kukaku.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】生産物グループデータ移行
	 */
	private static Result migrationCropGroup(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 生産物グループリストを取得する -----
        List<CropGroup> groupList	= server.find(CropGroup.class).order("crop_group_id").findList();

        //----- 生産物グループを作成する -----
        for (CropGroup cropGroupOld : groupList) {

        	CropGroup cropGroup 	= new CropGroup();
        	cropGroup.cropGroupId	= cropGroupOld.cropGroupId;
        	cropGroup.cropGroupName	= cropGroupOld.cropGroupName;
        	cropGroup.farmId		= cropGroupOld.farmId;
        	cropGroup.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】生産物データ移行
	 */
	private static Result migrationCrop(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 生産物リストを取得する -----
        List<Crop> cropList	= server.find(Crop.class).select("cropId,cropName,cropColor").order("crop_id").findList();

        //----- 生産物を作成する -----
        for (Crop cropOld : cropList) {

        	Crop crop 		= new Crop();
        	crop.cropId		= cropOld.cropId;
        	crop.cropName	= cropOld.cropName;
        	crop.cropColor	= cropOld.cropColor;
        	crop.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】農肥データ移行
	 */
	private static Result migrationNouhi(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 農肥リストを取得する -----
        List<Nouhi> nouhiList	= server.find(Nouhi.class).select("nouhiId,nouhiName,nouhiKind,bairitu,sanpuryo,"
        		+ "unitKind,n,p,k,lower,upper,finalDay").order("nouhi_id").findList();

        //----- 農肥を作成する -----
        for (Nouhi nouhiOld : nouhiList) {

        	Nouhi nouhi 		= new Nouhi();
        	nouhi.nouhiId		= nouhiOld.nouhiId;
        	nouhi.nouhiName		= nouhiOld.nouhiName;
        	nouhi.nouhiKind		= nouhiOld.nouhiKind;
        	nouhi.bairitu		= nouhiOld.bairitu;
        	nouhi.sanpuryo		= nouhiOld.sanpuryo;
        	nouhi.unitKind		= nouhiOld.unitKind;
        	nouhi.n				= nouhiOld.n;
        	nouhi.p				= nouhiOld.p;
        	nouhi.k				= nouhiOld.k;
        	nouhi.lower			= nouhiOld.lower;
        	nouhi.upper			= nouhiOld.upper;
        	nouhi.finalDay		= nouhiOld.finalDay;
        	nouhi.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】品種データ移行
	 */
	private static Result migrationHinsyu(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 品種リストを取得する -----
        List<Hinsyu> hinsyuList	= server.find(Hinsyu.class).order("hinsyu_id").findList();

        //----- 品種を作成する -----
        for (Hinsyu hinsyuOld : hinsyuList) {

        	Hinsyu hinsyu 		= new Hinsyu();
        	hinsyu.hinsyuId		= hinsyuOld.hinsyuId;
        	hinsyu.hinsyuName	= hinsyuOld.hinsyuName;
        	hinsyu.cropId		= hinsyuOld.cropId;
        	hinsyu.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】ベルトデータ移行
	 */
	private static Result migrationBelto(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- ベルトリストを取得する -----
        List<Belto> beltoList	= server.find(Belto.class).order("belto_id").findList();

        //----- ベルトを作成する -----
        for (Belto beltoOld : beltoList) {

        	Belto belto 		= new Belto();
        	belto.beltoId		= beltoOld.beltoId;
        	belto.beltoName		= beltoOld.beltoName;
        	belto.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】機器データ移行
	 */
	private static Result migrationKiki(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- 機器リストを取得する -----
        List<Kiki> kikiList	= server.find(Kiki.class).order("kiki_id").findList();

        //----- 機器を作成する -----
        for (Kiki kikiOld : kikiList) {

        	Kiki kiki 		= new Kiki();
        	kiki.kikiId		= kikiOld.kikiId;
        	kiki.kikiName	= kikiOld.kikiName;
        	kiki.katasiki	= kikiOld.katasiki;
        	kiki.maker		= kikiOld.maker;
        	kiki.kikiKind	= kikiOld.kikiKind;
        	kiki.onUseAttachmentId	= kikiOld.onUseAttachmentId;
        	kiki.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】アタッチメントデータ移行
	 */
	private static Result migrationAttachment(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- アタッチメントリストを取得する -----
        List<Attachment> attachmentList	= server.find(Attachment.class).select("attachmentId,attachementName,katasiki,attachmentKind").order("attachment_id").findList();

        //----- アタッチメントを作成する -----
        for (Attachment attachmentOld : attachmentList) {

        	Attachment attachment 	= new Attachment();
        	attachment.attachmentId		= attachmentOld.attachmentId;
        	attachment.attachementName	= attachmentOld.attachementName;
        	attachment.katasiki			= attachmentOld.katasiki;
        	attachment.attachmentKind	= attachmentOld.attachmentKind;
        	attachment.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】シーケンスデータ移行
	 */
	private static Result migrationSequence(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- シーケンスリストを取得する -----
        List<Sequence> sequenceList	= server.find(Sequence.class).order("sequence_id").findList();

        //----- シーケンスを作成する -----
        for (Sequence sequenceOld : sequenceList) {

        	Sequence sequence 		= new Sequence();
        	sequence.sequenceId		= sequenceOld.sequenceId;
        	sequence.sequenceValue	= sequenceOld.sequenceValue;
        	sequence.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】区画状況データ移行
	 */
	private static Result migrationCompartmentStatus(EbeanServer server){
        ObjectNode resultJson 		= Json.newObject();															/* 戻り値用JSONオブジェクト */
        double hinsyuId				= 0;
        double cropId				= 0;

        //----- 区画状況リストを取得する -----
        List<CompartmentStatus> kukakuList	= server.find(CompartmentStatus.class).select("kukakuId,rotationSpeedOfYear,hinsyuName,hashuDate,seiikuDayCount,nowEndWork,finalDisinfectionDate,finalKansuiDate,"
        		+ "finalTuihiDate,shukakuStartDate,shukakuEndDate,totalDisinfectionCount,totalKansuiCount,totalTuihiCount,totalShukakuCount,oldDisinfectionCount,oldKansuiCount,oldTuihiCount,oldShukakuCount,"
        		+ "nowWorkMode,endWorkId,finalEndDate,nextWorkId,workColor,katadukeDate").order("kukaku_id").findList();

        //----- 区画状況を作成する -----
        for (CompartmentStatus kukakuOld : kukakuList) {
        	//品種名、播種日が空白のデータは対象外
        	//if(kukakuOld.hashuDate == null ||
        	//   kukakuOld.hinsyuName == null){
        	//	continue;
        	//}

        	//作業年取得
        	int year = 0;
        	if(kukakuOld.hashuDate == null){
        		year = 0;
        	}else{
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
	    	    String yyyyFormat = sdf.format( kukakuOld.hashuDate );
	    	    year = Integer.parseInt(yyyyFormat);
        	}

        	//品種取得
        	if(kukakuOld.hinsyuName == null){
        		hinsyuId = 0;
        		cropId = 0;
        	}else{
        		//----- 品種リストを取得する -----
                Hinsyu hinsyu	= server.find(Hinsyu.class).where().eq("hinsyu_name",kukakuOld.hinsyuName).findUnique();
                if(hinsyu == null){
            		hinsyuId = 0;
            		cropId = 0;
                }else{
                    hinsyuId = hinsyu.hinsyuId;
                    cropId = hinsyu.cropId;
                }
        	}

        	CompartmentStatus kukakuStatus 	= new CompartmentStatus();
        	kukakuStatus.kukakuId				= kukakuOld.kukakuId;
        	kukakuStatus.workYear				= year;
        	kukakuStatus.rotationSpeedOfYear	= kukakuOld.rotationSpeedOfYear;
//      	kukakuStatus.hinsyuId				= hinsyuId;
        	kukakuStatus.cropId					= cropId;
        	kukakuStatus.hinsyuName				= kukakuOld.hinsyuName;
        	kukakuStatus.hashuDate				= kukakuOld.hashuDate;
        	kukakuStatus.seiikuDayCount			= kukakuOld.seiikuDayCount;
        	kukakuStatus.nowEndWork				= kukakuOld.nowEndWork;
        	kukakuStatus.finalDisinfectionDate	= kukakuOld.finalDisinfectionDate;
        	kukakuStatus.finalKansuiDate		= kukakuOld.finalKansuiDate;
        	kukakuStatus.finalTuihiDate			= kukakuOld.finalTuihiDate;
        	kukakuStatus.shukakuStartDate		= kukakuOld.shukakuStartDate;
        	kukakuStatus.shukakuEndDate			= kukakuOld.shukakuEndDate;
        	kukakuStatus.totalDisinfectionCount	= kukakuOld.totalDisinfectionCount;
        	kukakuStatus.totalKansuiCount		= kukakuOld.totalKansuiCount;
        	kukakuStatus.totalTuihiCount		= kukakuOld.totalTuihiCount;
        	kukakuStatus.totalShukakuCount		= kukakuOld.totalShukakuCount;
        	kukakuStatus.oldDisinfectionCount	= kukakuOld.oldDisinfectionCount;
        	kukakuStatus.oldKansuiCount			= kukakuOld.oldKansuiCount;
        	kukakuStatus.oldTuihiCount			= kukakuOld.oldTuihiCount;
        	kukakuStatus.oldShukakuCount		= kukakuOld.oldShukakuCount;
        	kukakuStatus.nowWorkMode			= kukakuOld.nowWorkMode;
        	kukakuStatus.endWorkId				= kukakuOld.endWorkId;
        	kukakuStatus.finalEndDate			= kukakuOld.finalEndDate;
        	kukakuStatus.nextWorkId				= kukakuOld.nextWorkId;
        	kukakuStatus.workColor				= kukakuOld.workColor;
        	kukakuStatus.katadukeDate			= kukakuOld.katadukeDate;
        	kukakuStatus.save();
        }

        return ok(resultJson);
	}

	/**
	 * 【AGRYELL】タイムラインデータ移行
	 */
	private static Result migrationTimeLine(EbeanServer server){
        ObjectNode resultJson 				= Json.newObject();															/* 戻り値用JSONオブジェクト */

        //----- タイムラインリストを取得する -----
        List<TimeLine> timeLineList	= server.find(TimeLine.class).select("timeLineId,updateTime,workDate,message,workDiaryId,timeLineColor,workId,workName,kukakuId,kukakuName,"
        		+ "accountId,accountName").order("time_line_id").findList();

        //----- タイムラインを作成する -----
        for (TimeLine timeLineOld : timeLineList) {

        	TimeLine timeLine 		= new TimeLine();
        	timeLine.timeLineId		= timeLineOld.timeLineId;
        	timeLine.updateTime		= timeLineOld.updateTime;
        	timeLine.workDate		= timeLineOld.workDate;
        	timeLine.message		= timeLineOld.message;
        	timeLine.workDiaryId	= timeLineOld.workDiaryId;
        	timeLine.timeLineColor	= timeLineOld.timeLineColor;
        	timeLine.workId			= timeLineOld.workId;
        	timeLine.workName		= timeLineOld.workName;
        	timeLine.kukakuId		= timeLineOld.kukakuId;
        	timeLine.kukakuName		= timeLineOld.kukakuName;
        	timeLine.accountId		= timeLineOld.accountId;
        	timeLine.accountName	= timeLineOld.accountName;
        	timeLine.save();
        }

        return ok(resultJson);
	}

	 private static List<Class<?>> find(String scannedPackage) {
		    String scannedPath = scannedPackage.replace(PKG_SEPARATOR, DIR_SEPARATOR);
		    URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
		    if (scannedUrl == null) {
		      throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
		    }
		    File scannedDir = new File(scannedUrl.getFile());
		    List<Class<?>> classes = new ArrayList<Class<?>>();
		    for (File file : scannedDir.listFiles()) {
		      classes.addAll(find(file, scannedPackage));
		    }
		    return classes;
		  }

		  private static List<Class<?>> find(File file, String scannedPackage) {
		    List<Class<?>> classes = new ArrayList<Class<?>>();
		    String resource = scannedPackage + PKG_SEPARATOR + file.getName();
		    if (file.isDirectory()) {
		      for (File child : file.listFiles()) {
		        classes.addAll(find(child, resource));
		      }
		    } else if (resource.endsWith(CLASS_FILE_SUFFIX)) {
		      int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
		      String className = resource.substring(0, endIndex);
		      try {
		        classes.add(Class.forName(className));
		      } catch (ClassNotFoundException ignore) {
		      }
		    }
		    return classes;
		  }
}
