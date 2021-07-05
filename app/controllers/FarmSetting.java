package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import models.Farm;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import util.StringU;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.SessionCheckComponent;
import compornent.FarmComprtnent;

import consts.AgryeelConst;
public class FarmSetting extends Controller {

    @Security.Authenticated(SessionCheckComponent.class)
    public static Result farmSettingMove(double farmId) {

        session(AgryeelConst.SessionKey.FARMID
                , String.valueOf(farmId));										//生産者IDをセッションに格納

        return ok(views.html.farmSetting.render(""));
    }

    /**
     * 【AGRYEEL】生産者情報設定初期表示データ取得
     * @return
     */
    public static Result farmSettingInit() {
        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        /* 生産者IDの取得 */
        Double farmId = Double.parseDouble(session(AgryeelConst.SessionKey.FARMID));
        resultJson.put("farmId", farmId);

        /* 生産者IDから生産者情報を取得する */
        FarmComprtnent fc = new FarmComprtnent();
        fc.GetFarmData(farmId);
        if (fc.farmData != null) {														//生産者情報が存在する場合
            resultJson.put("farmName", fc.farmData.farmName);							//生産者名
            if(fc.farmData.representativeName != null){
	            resultJson.put("representativeName"	, fc.farmData.representativeName);	//代表者
            }else{
	            resultJson.put("representativeName"	, "");								//代表者
            }
            if(fc.farmData.postNo != null){
	             resultJson.put("postNo"			, fc.farmData.postNo);				//郵便番号
            }else{
	             resultJson.put("postNo"			, "");								//郵便番号
            }
            if(fc.farmData.prefectures != null){
	             resultJson.put("prefectures"		, fc.farmData.prefectures);			//都道府県
            }else{
	             resultJson.put("prefectures"		, "");								//都道府県
            }
            if(fc.farmData.address != null){
            	resultJson.put("address"			, fc.farmData.address);				//市町村など
            }else{
            	resultJson.put("address"			, "");								//市町村など
            }
            if(fc.farmData.tel != null){
            	resultJson.put("tel"				, fc.farmData.tel);					//電話番号
            }else{
            	resultJson.put("tel"				, "");								//電話番号
            }
            if(fc.farmData.responsibleMobileTel != null){
            	resultJson.put("responsibleMobileTel", fc.farmData.responsibleMobileTel);	//責任者携帯番号
            }else{
            	resultJson.put("responsibleMobileTel", "");								//責任者携帯番号
            }
            if(fc.farmData.fax != null){
            	resultJson.put("fax"				, fc.farmData.fax);					//FAX
            }else{
            	resultJson.put("fax"				, "");								//FAX
            }
            if(fc.farmData.mailAddressPC != null){
	            resultJson.put("mailAddressPC"		, fc.farmData.mailAddressPC);		//メールアドレス（パソコン）
            }else{
	            resultJson.put("mailAddressPC"		, "");								//メールアドレス（パソコン）
            }
            if(fc.farmData.mailAddressMobile != null){
	            resultJson.put("mailAddressMobile"	, fc.farmData.mailAddressMobile);	//メールアドレス（携帯）
            }else{
	            resultJson.put("mailAddressMobile"	, "");								//メールアドレス（携帯）
            }
            if(fc.farmData.url != null){
	            resultJson.put("url"				, fc.farmData.url);					//ホームページＵＲＬ
            }else{
	            resultJson.put("url"				, "");								//ホームページＵＲＬ
            }
            if(fc.farmData.registrationCode != null){
	            resultJson.put("registrationCode"	, fc.farmData.registrationCode);	//レジストレーションコード
            }else{
	            resultJson.put("registrationCode"	, "");								//レジストレーションコード
            }

            //---------- 生産者ステータス情報の取得 ----------
            resultJson.put("areaUnit"			, fc.farmStatusData.areaUnit);			//面積単位
            resultJson.put("kisyo"				, fc.farmStatusData.kisyo);				//期初
            resultJson.put("nouhiCheck"			, fc.farmStatusData.nouhiCheck);		//農肥チェック
            resultJson.put("workPlanAutoMove"	, fc.farmStatusData.workPlanAutoMove);	//作業指示自動移動
            resultJson.put("contractPlan"		, fc.farmStatusData.contractPlan);		//契約プラン
            resultJson.put("dataUsePermission"	, fc.farmStatusData.dataUsePermission);	//データ使用許可
            resultJson.put("historyReference"	, fc.farmStatusData.historyReference);	//履歴参照
            resultJson.put("syukakuInputCount"	, fc.farmStatusData.syukakuInputCount);	//収穫入力数
            resultJson.put("paymentKubun"		, fc.farmStatusData.paymentKubun);		//支払区分
            //最終回収日
            String kaisyuday = "";
            if(String.valueOf(fc.farmStatusData.lastCollectionDate) != "null"){
            	kaisyuday = dateFormat.format(fc.farmStatusData.lastCollectionDate);
            }else{
            	kaisyuday = "2000-01-01";
            }
            resultJson.put("lastCollectionDate"	, kaisyuday);
            //次回回収日
            kaisyuday = "";
            if(String.valueOf(fc.farmStatusData.nextCollectionDate) != "null"){
            	kaisyuday = dateFormat.format(fc.farmStatusData.nextCollectionDate);
            }else{
            	kaisyuday = "2000-01-01";
            }
            resultJson.put("nextCollectionDate"	, kaisyuday);

        }
        else {
            return notFound();															//生産者情報が存在しない場合、エラーとする
        }

        return ok(resultJson);
    }

    /**
     * 【AGRYEEL】生産者情報保存処理
     * @return
     */
    public static Result submitFarm() {

        /* 戻り値用JSONデータの生成 */
        ObjectNode 	resultJson = Json.newObject();

        /* JSONデータを取得 */
        JsonNode input = request().body().asJson();
        Double	farmId = Double.parseDouble(input.get("farmId").asText());

        /*-- 取得した項目を生産者情報に保存する --*/
        Farm farmData = Farm.getFarm(farmId);
        farmData.representativeName		=	input.get("representativeName").asText();					//代表者
        farmData.postNo					=	input.get("postNo").asText();								//郵便番号
        farmData.prefectures			=	input.get("prefectures").asText();							//都道府県
        farmData.address				=	input.get("address").asText();								//市町村など
        farmData.tel					=	input.get("tel").asText();									//電話番号
        farmData.responsibleMobileTel	=	input.get("responsibleMobileTel").asText();					//責任者携帯番号
        farmData.fax					=	input.get("fax").asText();									//FAX
        farmData.mailAddressPC			=	input.get("mailAddressPC").asText();						//メールアドレス（パソコン）
        farmData.mailAddressMobile		=	input.get("mailAddressMobile").asText();					//メールアドレス（携帯）
        farmData.url					=	input.get("url").asText();									//ホームページＵＲＬ
        farmData.update();																				//生産者情報更新

        //---------- 生産者ステータス情報の更新 ----------
        FarmComprtnent fc = new FarmComprtnent();
        fc.GetFarmData(farmId);
        fc.farmStatusData.areaUnit      = input.get("areaUnit").asInt();
        fc.farmStatusData.kisyo         = input.get("kisyo").asInt();
        fc.farmStatusData.nouhiCheck    = input.get("nouhiCheck").asInt();
        fc.farmStatusData.workPlanAutoMove = input.get("workPlanAutoMove").asInt();
        fc.farmStatusData.historyReference = input.get("historyReference").asInt();
        fc.farmStatusData.syukakuInputCount = input.get("syukakuInputCount").asInt();
        fc.farmStatusData.update();

        resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);

        return ok(resultJson);
    }
}
