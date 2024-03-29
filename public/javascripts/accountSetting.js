﻿/* AGRYEEL アカウント情報設定画面 JQUERY */
(function($){

  var managerRoleList = {
    "0" : { "typeId" : "0", "typeName" : "担当者" }
   ,"1" : { "typeId" : "1", "typeName" : "作業管理者" }
   ,"2" : { "typeId" : "2", "typeName" : "経営者" }
  };

  var firstPageList = {
    "0" : { "typeId" : "0", "typeName" : "圃場状況照会" }
   ,"1" : { "typeId" : "1", "typeName" : "作業状況照会" }
   ,"2" : { "typeId" : "2", "typeName" : "タイムライン" }
   ,"3" : { "typeId" : "3", "typeName" : "作業指示書" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

	  //$('.modal').modal();
	  $('.mselectmodal-trigger').unbind('click');
	  $('.mselectmodal-trigger').bind('click', mSelectOpen);

    //------------------------------------------------------------------------------------------------------------------
    //- セレクトモーダルの初期化
    //------------------------------------------------------------------------------------------------------------------
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);


    });

    $.ajax({
        url:"/accountSettingInit", 										//アカウント情報設定初期処理
        type:'GET',
        complete:function(data, status, jqXHR){							//処理成功時
        var jsonResult = JSON.parse( data.responseText );				//戻り値用JSONデータの生成
        var htmlString	= "";											//可変HTML文字列
        var accountId = jsonResult["accountId"];						//圃場ID取得
        var managerRoleID = 0;											//管理者権限ID
        var managerRole = "";											//管理者権限
        var firstPageID = 0;											//初期表示ページID
        var firstPage = "";												//初期表示ページ

        //管理者権限
        managerRoleID = jsonResult["managerRole"];
        if(jsonResult["managerRole"] == 1){
      	  managerRole = "作業管理者";
        }else if(jsonResult["managerRole"] == 2){
      	  managerRole = "経営者";
        }else if(jsonResult["managerRole"] == 99){
      	  managerRole = "メーカー";
        }else{
        	managerRoleID = 0;
      	  managerRole = "担当者";
        }


        //初期表示ページ
        firstPageID = jsonResult["firstPage"];
        if(jsonResult["firstPage"] == 0){
      	  firstPage = "圃場状況照会";
        }else if(jsonResult["firstPage"] == 1){
          firstPage = "作業状況照会";
        }else if(jsonResult["firstPage"] == 2){
          firstPage = "タイムライン";
        }else if(jsonResult["firstPage"] == 3){
          firstPage = "作業指示書";
        }else{
          firstPageID = 1;
          firstPage = "圃場状況照会";
        }

        /* メニュー権限文字列生成 */
        var spanMsg = "";
        var msgCnt  = 0;
        var menuList = jsonResult["menuRoleDataList"]; 						//jSONデータよりメニュー権限リストを取得
        for ( var menuKey in menuList ) {									//メニュー権限件数分処理を行う

            var menuData = menuList[menuKey];

            if (msgCnt > 1) {
                spanMsg += "，．．．";
                break;
            }
            else if (msgCnt == 1) {
                spanMsg += "，";
            }
            spanMsg += menuData["menuName"];
            msgCnt++;
        }

        //選択用モーダルリストの生成
        //管理者権限モーダルリストの作成
        htmlString+= MakeSelectModal('G1002ModalManagerRole', '管理者権限', managerRoleList, 'typeId', 'typeName', 'G1002ManagerRole', 'managerRole');
        //初期表示ページモーダルリストの作成
  	  	htmlString+= MakeSelectModal('G1002ModalFirstPage', '初期表示ページ', firstPageList, 'typeId', 'typeName', 'G1002FirstPage', 'firstPage');
        $("#G1002SelectModal").html(htmlString);							//可変HTML部分に反映する

        htmlString = '<div class="card mst-panel" style="">';
        htmlString+= '<div class="card-panel fixed-color" style="">';
        /* タイトル */
        htmlString+= '<span class="white-text" id="G1002Title" farmId="' + jsonResult["farmId"] + '" accountId="' + jsonResult["accountId"] + '">アカウント情報設定</span>';
  	    htmlString+= '</div>';

        htmlString+= '<div class="row" id="G1002AccountInfo">';
        htmlString+= '<div class="card-action-center">';
        htmlString+= '<form>';

        /* アカウントID */
	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1 mst-item-title">';
        htmlString+= 'アカウントID：<span class="mst-item" style="">' + jsonResult["accountId"] + '</span>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* パスワード */
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G1002InPassword" type="password" class="validate input-text-color" maxlength="30" style="ime-mode: disabled;" value="' + jsonResult["password"] + '">';
        htmlString+= '<label for="G1002InPassword">パスワードを入力してください<span style="color:red">（*）</span></label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 氏名 */
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G1002InAcountName" type="text" class="validate input-text-color" maxlength="32" style="ime-mode: active;" value="' + jsonResult["acountName"] + '">';
        htmlString+= '<label for="G1002InAcountName">氏名を入力してください<span style="color:red">（*）</span></label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* かな */
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G1002InAcountKana" type="text" class="validate input-text-color" maxlength="32" style="ime-mode: active;" value="' + jsonResult["acountKana"] + '">';
        htmlString+= '<label for="G1002InAcountKana">かなを入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 備考 */
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G1002Remark" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["remark"] + '">';
        htmlString+= '<label for="G1002Remark">備考を入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        //メールアドレス
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
   	    htmlString+= '<input id="G1002MailAddress" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["mailAddress"] + '">';
        htmlString+= '<label for="G1002MailAddress">メールアドレスを入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        //生年月日の生成
        var birthday = jsonResult["birthday"];
  	    htmlString+= '<div class="row">';
        htmlString += '<div class="input-field col s10 offset-s1 grey-text">';
        htmlString += '<input type="text" placeholder="生年月日" id="G1002BirthDay" class="datepicker input-text-color" style="" value="' + birthday.replace("-", "/").replace("-", "/") + '">';
        htmlString+= '<label for="G1002BirthDay">生年月日を入力してください</label>';
        htmlString += '</div>';
        htmlString+= '</div>';

        /* 管理者権限 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        if (jsonResult.selfrole == 0) {
          htmlString+= '管理者権限<span>&nbsp;&nbsp;&nbsp;&nbsp;' + managerRole + '</span>';
        }
        else {
          htmlString+= '管理者権限<a href="#G1002ModalManagerRole"  class="collection-item modal-trigger select-modal"><span id="G1002ManagerRoleSpan" class="blockquote-input">' + managerRole + '</span></a>';
        }
        htmlString+= '<input type="hidden" id="G1002ManagerRoleValue" value="' + managerRoleID + '">';
        htmlString+= '</div>';
        htmlString+= '</div>';

	    /* メニュー権限 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1  mst-item-title">';
        htmlString+= 'メニュー権限<span style="color:red">（*）</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="メニュー権限一覧" data="' + jsonResult["farmId"] + '/' + jsonResult["accountId"] + '/getMenuRoleSel" displayspan="#G1002MenuRolespan"><span id="G1002MenuRolespan" class="blockquote-input">' + spanMsg + '</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 初期表示ページ */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '初期表示ページ<a href="#G1002ModalFirstPage"  class="collection-item modal-trigger select-modal"><span id="G1002FirstPageSpan" class="blockquote-input">' + firstPage + '</span></a>';
        htmlString+= '<input type="hidden" id="G1002FirstPageValue" value="' + firstPageID + '">';
        htmlString+= '</div>';
        htmlString+= '</div>';

        //心拍数上限
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
   	    htmlString+= '心拍数上限<span id="G1002HeartRateUpLimitSpan" class="blockquote-input">' + jsonResult["heartRateUpLimit"] + '</span><span class="blockquote-inputOption"> bpm</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1002HeartRateUpLimit">keyboard</i><br />';
        htmlString+= '<input type="hidden" id="G1002HeartRateUpLimit" value="' + jsonResult["heartRateUpLimit"] + '">';
        htmlString+= '</div>';
        htmlString+= '</div>';

        //心拍数下限
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
   	    htmlString+= '心拍数下限<span id="G1002HeartRateDownLimitSpan" class="blockquote-input">' + jsonResult["heartRateDownLimit"] + '</span><span class="blockquote-inputOption"> bpm</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1002HeartRateDownLimit">keyboard</i><br />';
        htmlString+= '<input type="hidden" id="G1002HeartRateDownLimit" value="' + jsonResult["heartRateDownLimit"] + '">';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 作業対象表示 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '作業対象表示<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業対象表示選択"" data="getWorkTargetDisplay" displayspan="#G1002WorkTargetDisplaySpan"><span id="G1002WorkTargetDisplaySpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 作業記録後 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '作業記録後<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業記録後動作選択"" data="getWorkCommitAfter" displayspan="#G1002WorkCommitAfterSpan"><span id="G1002WorkCommitAfterSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* ワークチェーン表示 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= 'ワークチェーン表示方法<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="ワークチェーン表示方法選択"" data="getDisplayChain" displayspan="#G1002DisplayChainSpan"><span id="G1002DisplayChainSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 荷姿履歴値参照*/
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '荷姿履歴値参照<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="荷姿履歴値参照選択"" data="getNisugataRireki" displayspan="#G1002NisugataRirekiSpan"><span id="G1002NisugataRirekiSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 区画状況照会SKIP */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '区画状況照会SKIP<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="区画状況照会SKIP選択"" data="getCompartmentStatusSkip" displayspan="#G1002CompartmentStatusSkipSpan"><span id="G1002CompartmentStatusSkipSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 作業日付自動設定 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '作業日付自動設定<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業日付自動設定選択"" data="getWorkDateAutoSet" displayspan="#G1002WorkDateAutoSetSpan"><span id="G1002WorkDateAutoSetSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 作業開始確認 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '作業開始確認<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業開始確認選択"" data="getWorkStartPrompt" displayspan="#G1002WorkStartPromptSpan"><span id="G1002WorkStartPromptSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 作業切替表示 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '作業切替表示<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業切替表示選択"" data="getWorkChangeDisplay" displayspan="#G1002WorkChangeDisplaySpan"><span id="G1002WorkChangeDisplaySpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 付近区画半径 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '付近区画距離<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="付近区画距離選択"" data="getRadius" displayspan="#G1002RadiusSpan"><span id="G1002RadiusSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 作業指示初期担当者 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '作業指示初期担当者<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業指示初期担当者選択"" data="getWorkPlanInitId" displayspan="#G1002WorkPlanInitIdSpan"><span id="G1002WorkPlanInitIdSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 作業記録注釈 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '作業記録注釈<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業記録注釈選択"" data="getWorkDiaryDiscription" displayspan="#G1002WorkDiaryDiscriptionSpan"><span id="G1002WorkDiaryDiscriptionSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        htmlString+= '</form>';
        htmlString+= '</div>';
        htmlString+= '</div>';
        /* コマンドボタン */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s12 m4" style="text-align: center;">';
        htmlString+= '<div id="G1002Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
        htmlString+= '</div>';
        htmlString+= '<div class="col s12 m4" style="text-align: center;">';
        htmlString+= '<div id="G1002Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
        htmlString+= '</div>';
        htmlString+= '<div class="col s12 m4" style="text-align: center;">';
        htmlString+= '<div id="G1002Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
        htmlString+= '</div>';
        htmlString+= '</div>';
        htmlString+= '<div class="row"></div>';

        htmlString+= '</div>';
        $("#G1002AccountSetting").html(htmlString);									//可変HTML部分に反映する

        $("input").prop("autocomplete", "off");										//オートコンプリート無効化
        $('.modal').modal();                        								//選択用モーダル画面初期化
  	    CalcInit();														        	// 数値入力電卓初期化
  	    SelectModalInit();												  			//選択用モーダルイベント初期化
        mSelectDataSet("#G1002MenuRolespan", jsonResult["farmId"] + '/' + jsonResult["accountId"] + '/getMenuRoleSel');		//マルチセレクトモーダル初期処理

        $("#G1002Submit").bind('click', onSubmit );     							//確定ボタン
        $("#G1002Delete").bind('click', onDelete );     							//削除ボタン
        $("#G1002Back").bind('click', onBack );     								//戻るボタン

        //------------------------------------------------------------------------------------------------------------------
        //- セレクトモーダルの初期選択反映
        //------------------------------------------------------------------------------------------------------------------
        //----- 作業対象表示 -----
        selectDataGet("#G1002WorkTargetDisplaySpan", "getWorkTargetDisplay");
        var oJson = selectData(jsonResult.workTargetDisplay);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 作業記録後 -----
        selectDataGet("#G1002WorkCommitAfterSpan", "getWorkCommitAfter");
        var oJson = selectData(jsonResult.workCommitAfter);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- ワークチェーン表示方法 -----
        selectDataGet("#G1002DisplayChainSpan", "getDisplayChain");
        var oJson = selectData(jsonResult.displayChain);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 荷姿履歴値参照 -----
        selectDataGet("#G1002NisugataRirekiSpan", "getNisugataRireki");
        var oJson = selectData(jsonResult.nisugataRireki);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 区画状況照会SKIP -----
        selectDataGet("#G1002CompartmentStatusSkipSpan", "getCompartmentStatusSkip");
        var oJson = selectData(jsonResult.compartmentStatusSkip);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 作業日付自動設定 -----
        selectDataGet("#G1002WorkDateAutoSetSpan", "getWorkDateAutoSet");
        var oJson = selectData(jsonResult.workDateAutoSet);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 作業開始確認 -----
        selectDataGet("#G1002WorkStartPromptSpan", "getWorkStartPrompt");
        var oJson = selectData(jsonResult.workStartPrompt);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 作業切替表示 -----
        selectDataGet("#G1002WorkChangeDisplaySpan", "getWorkChangeDisplay");
        var oJson = selectData(jsonResult.workChangeDisplay);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 付近区画距離 -----
        selectDataGet("#G1002RadiusSpan", "getRadius");
        var oJson = selectData(jsonResult.radius);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 作業指示初期担当者 -----
        selectDataGet("#G1002WorkPlanInitIdSpan", "getWorkPlanInitId");
        var oJson = selectData(jsonResult.workPlanInitId);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 作業記録注釈 -----
        selectDataGet("#G1002WorkDiaryDiscriptionSpan", "getWorkDiaryDiscription");
        var oJson = selectData(jsonResult.workDiaryDiscription);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });

  }); // end of document ready

  /* 確定ボタン押下処理 */
  function onSubmit(){

		var farmId = $("#G1002Title").attr("farmId");
		var accountId = $("#G1002Title").attr("accountId");
		var birthDay =  $("#G1002BirthDay").val();

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G1002InPassword"			, "name" : "パスワード"			, "length" : "30"	, "json" : "password"				, "check" : { "required" : "1", "password" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1002InAcountName"		, "name" : "氏名"				, "length" : "32"	, "json" : "accountName"			, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1002InAcountKana"		, "name" : "かな"				, "length" : "32"	, "json" : "accountKana"			, "check" : { "furigana" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1002Remark"				, "name" : "備考"				, "length" : "128"	, "json" : "remark"					, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1002MailAddress"			, "name" : "メールアドレス"		, "length" : "128"	, "json" : "mailAddress"			, "check" : { "malladdress" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1002ManagerRoleValue"	, "name" : "管理者権限"			, "length" : "16"	, "json" : "managerRole"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1002FirstPageValue"		, "name" : "初期表示ページ"		, "length" : "16"	, "json" : "firstPage"				, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1002HeartRateUpLimit"	, "name" : "心拍数上限"			, "length" : "8"	, "json" : "heartRateUpLimit"		, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1002HeartRateDownLimit"	, "name" : "心拍数下限"			, "length" : "8"	, "json" : "heartRateDownLimit"		, "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* メニュー権限のチェック */
        var menuRole = "";
    	menuRole = mSelectConvertJson("#G1002MenuRolespan");
        if (menuRole == "") {
          displayToast('メニュー権限を選択してください。', 4000, 'rounded');
          return false;
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["accountId"] = accountId;
	    jsondata["birthday"] = birthDay;
	    jsondata["menuRole"] = menuRole;

	    var workTargetDisplay = selectConvertJson("#G1002WorkTargetDisplaySpan");
      var workCommitAfter   = selectConvertJson("#G1002WorkCommitAfterSpan");
      var displayChain      = selectConvertJson("#G1002DisplayChainSpan");
      var nisugataRireki    = selectConvertJson("#G1002NisugataRirekiSpan");
      var compartmentStatusSkip    = selectConvertJson("#G1002CompartmentStatusSkipSpan");
      var workDateAutoSet    = selectConvertJson("#G1002WorkDateAutoSetSpan");
      var workStartPrompt    = selectConvertJson("#G1002WorkStartPromptSpan");
      var workChangeDisplay    = selectConvertJson("#G1002WorkChangeDisplaySpan");
      var radius               = selectConvertJson("#G1002RadiusSpan");
      var workPlanInitId       = selectConvertJson("#G1002WorkPlanInitIdSpan");
      var workDiaryDiscription = selectConvertJson("#G1002WorkDiaryDiscriptionSpan");

      jsondata["workTargetDisplay"]       = workTargetDisplay;
      jsondata["workCommitAfter"]         = workCommitAfter;
      jsondata["displayChain"]            = displayChain;
      jsondata["nisugataRireki"]          = nisugataRireki;
      jsondata["compartmentStatusSkip"]   = compartmentStatusSkip;
      jsondata["workDateAutoSet"]         = workDateAutoSet;
      jsondata["workStartPrompt"]         = workStartPrompt;
      jsondata["workChangeDisplay"]       = workChangeDisplay;
      jsondata["radius"]                  = radius;
      jsondata["workPlanInitId"]          = workPlanInitId;
      jsondata["workDiaryDiscription"]    = workDiaryDiscription;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitAccount",
			type:'POST',
	        data:JSON.stringify(jsondata),								//入力用JSONデータ
			complete:function(data, status, jqXHR){						//処理成功時

			onProcing(false);
		    onBack()

			},
		    dataType:'json',
		    contentType:'text/json',
		    async: true
		});

  } // end of SubmitTap

  /* 削除ボタン押下処理 */
  function onDelete() {

	var accountId = $("#G1002Title").attr("accountId");
	//JSONDATA変換用文字列の作成
	var result = '{"accountId":"' + accountId + '"}';
	var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	console.log("jsondata", jsondata);
    if (confirm("アカウント情報を削除します。よろしいですか？")) {
		$.ajax({
			url:"/deleteAccount",
			type:'POST',
		    data:JSON.stringify(jsondata),									//入力用JSONデータ
			complete:function(data, status, jqXHR){							//処理成功時

			onProcing(false);
			onBack()

			},
			dataType:'json',
			contentType:'text/json',
			async: true
		  });
	}
  }

  /* 戻るボタン押下時イベント */
  function onBack(){

		var farmID	= $("#G1002Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/0/masterMntMove";

  } // end of DeleteTap

})(jQuery); // end of jQuery name space