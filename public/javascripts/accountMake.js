﻿/* AGRYEEL アカウント作成画面 JQUERY */
(function($){
  var makeBtnClick = false;	//アカウント作成ボタンクリックフラグ
  var makeCheckNo = 0;		//生成確認番号

  /* 初期処理時イベント */
  $(function(){
    var htmlString	= "";										//可変HTML文字列
    //タイトルカード
    htmlString = '<div class="card mst-panel" style="">';
    htmlString += '<div class="card-panel fixed-color" style="">';
    htmlString += '<span class="white-text">【AICA】アカウントを作成します。</span>';
    htmlString += '</div>';
    htmlString += '<div class="row" id="G1001AccountInfo">';
    htmlString += '<div class="card-action center">';
    htmlString += '<form>';
  //アカウントID
    htmlString += '<div class="row">';
    htmlString += '<div class="input-field col s10 offset-s1">';
    htmlString += '<input id="G1001InAccountId" type="email" class="validate input-text-color" maxlength="12" style="ime-mode: disabled;">';
    htmlString += '<label for="G1001InAccountId">アカウントIDを入力してください</label>';
    htmlString += '</div>';
    htmlString += '</div>';
  //パスワード
    htmlString += '<div class="row">';
    htmlString += '<div class="input-field col s10 offset-s1">';
    htmlString += '<input id="G1001InPassword" type="password" class="validate input-text-color" maxlength="30" style="ime-mode: disabled;">';
    htmlString += '<label for="G1001InPassword">パスワードを入力してください</label>';
    htmlString += '</div>';
    htmlString += '</div>';
  //氏名
    htmlString += '<div class="row">';
    htmlString += '<div class="input-field col s10 offset-s1">';
    htmlString += '<input id="G1001AccountName" type="text" class="validate input-text-color" style="ime-mode: active;">';
    htmlString += '<label for="G1001AccountName">氏名を入力してください</label>';
    htmlString += '</div>';
    htmlString += '</div>';
  //レジストレーションコード
    htmlString += '<div class="row">';
    htmlString += '<div class="input-field col s10 offset-s1">';
    htmlString += '<input id="G1001InRegistrationCode" type="email" class="validate input-text-color" maxlength="16" style="ime-mode: disabled;">';
    htmlString += '<label for="G1001InRegistrationCode">レジストレーションコードを入力してください</label>';
    htmlString += '</div>';
    htmlString += '</div>';
  //確認番号
    htmlString += '<div class="row">';
    htmlString += '<div class="col s10 offset-s1">';
    htmlString += '<div id="G1001CheckNoDisp"></div>';
    htmlString += '</div>';
    htmlString += '</div>';
    htmlString += '<div class="row">';
    htmlString += '<div class="input-field col s10 offset-s1">';
    htmlString += '<input id="G1001InCheckNo" type="email" class="validate input-text-color" maxlength="4" style="ime-mode: disabled;">';
    htmlString += '<label for="G1001InCheckNo">確認番号を入力してください</label>';
    htmlString += '</div>';
    htmlString += '</div>';
    htmlString += '</form>';
    //アカウント作成ボタン
    htmlString += '<p><div id="G1001BtnAccountMake" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">account_box</i>アカウントの作成</div></p>';
    htmlString += '<p><div id="G1001Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div></p>';
    htmlString += '</div>';
    $("#G1001AccountMake").html(htmlString);					//可変HTML部分に反映する

    htmlString = '<span class="string-color">確認番号：</span><span class="white-text">XXXX</span>';
    $("#G1001CheckNoDisp").html(htmlString);					//可変HTML部分に反映する

    /*----- 各コントロールにイベントを実装する -----*/
    $("#G1001InAccountId").bind('change', G1001AccountId );          	  //アカウントID
    $("#G1001InPassword").bind('change', G1001Password );             	  //パスワード
    $("#G1001AccountName").bind('change', G1001AccountName );     		  //氏名
    $("#G1001InRegistrationCode").bind('change', G1001RegistrationCode ); //レジストレーション
    $("#G1001InCheckNo").bind('change', G1001InCheckNo );  				  //確認番号
    $("#G1001BtnAccountMake").bind('click', G1001BtnAccountMake );        //アカウント作成ボタン
    $("#G1001Back").bind('click', onBack );        						  //戻るボタン
  }); // end of document ready

  /* アカウントＩＤ入力時イベント */
  function G1001AccountId() {
    /* 入力項目情報定義 */
    var checktarget = [
        { "id" : "G1001InAccountId", "name" : "アカウントＩＤ", "length" : "12", "json" : "accountId", "check" : { "required" : "1", "maxlength" : "1"}}
    ];

      /* 入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

    //アカウントＩＤ重複チェック
      if(AccountIdCheck(jsondata) == false){
        return false;
      }

      return true;
  } // end of G1001AccountId

  /* アカウントID情報をサーバから取得する */
  function AccountIdCheck(jsondata){

    $.ajax({
        url:"/accountIdCheck", 									//アカウントIDチェック処理
      type:'POST',
      data:JSON.stringify(jsondata),							//入力用JSONデータ
      complete:function(data, status, jqXHR){					//処理成功時
        var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

        if (jsonResult.result == 'ACCOUNTIDMATCH') {
        	alert('このアカウントＩＤ' + 'は既に使用されています');      //エラーメッセージの表示
            return false;
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });

    return true;
  } // end of AccountIdCheck

  /* パスワード入力時イベント */
  function G1001Password() {
    /* 入力項目情報定義 */
    var checktarget = [
        { "id" : "G1001InPassword", "name" : "パスワード", "length" : "30", "json" : "password", "check" : { "required" : "1", "password" : "1", "maxlength" : "1"}}
    ];

      /* 入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    return true;
  } // end of G1001Password

  /* 氏名入力時イベント */
  function G1001AccountName() {
    /* 入力項目情報定義 */
    var checktarget = [
        { "id" : "G1001AccountName", "name" : "氏名", "length" : "32", "json" : "accountName", "check" : { "required" : "1", "maxlength" : "1"}}
    ];

      /* 入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

    return true;
  } // end of G1001AccountName

  /* レジストレーションコード入力時イベント */
  function G1001RegistrationCode() {
    /* 入力項目情報定義 */
    var checktarget = [
        { "id" : "G1001InRegistrationCode", "name" : "レジストレーションコード", "length" : "8", "json" : "registrationCode", "check" : { "required" : "1", "maxlength" : "1"}}
        ];

      /* 入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

    //レジストレーションコード存在チェック
      if(RegistrationCodeCheck(jsondata) == false){
        return false;
      }

    /* アカウント登録上限チェック */
    if(AccountRegistLimitCheck() == false){
      return false;
    }

    return true;
  } // end of G1001RegistrationCode

  /* レジストレーションコード存在チェック */
  function RegistrationCodeCheck(jsondata){

    $.ajax({
        url:"/registrationCodeCheck", 							//レジストレーションコードチェック処理
      type:'POST',
      data:JSON.stringify(jsondata),							//入力用JSONデータ
      complete:function(data, status, jqXHR){					//処理成功時
        var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

        if (jsonResult.result == 'REGCODEUNMATCH') {
        	alert('このレジストレーションコードは無効です');      //エラーメッセージの表示
          return false;
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });

    return true;
  } // end of RegistrationCodeCheck

  /* アカウント登録上限チェック */
  function AccountRegistLimitCheck(){

    /* 入力項目情報定義 */
    var checktarget = [
        { "id" : "G1001InRegistrationCode", "name" : "レジストレーションコード", "length" : "8", "json" : "registrationCode", "check" : { "required" : "1", "maxlength" : "1"}}
        ];

    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

    $.ajax({
        url:"/accountRegistLimitCheck", 						//アカウント登録上限チェック処理
      type:'POST',
      data:JSON.stringify(jsondata),							//入力用JSONデータ
      complete:function(data, status, jqXHR){					//処理成功時
        var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

        if (jsonResult.result == 'ACCOUNTLIMMIT') {
        	alert('お客様のアカウント作成上限は５人までです');  //エラーメッセージの表示
          return false;
        }
        else
        {
          //確認番号表示
          if(makeBtnClick == false){
            CheckNoDisp();
          }
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });

    return true;
  } // end of RegistrationCodeCheck

  /* 確認番号表示処理 */
  function CheckNoDisp() {
    //入力レジストレーションコード取得
    var regCode = $("#G1001InRegistrationCode").val();

    //確認番号生成
    //レジストレーションコード8桁目と1桁目
    var check1 = parseInt(regCode.substr(0,1),16) + parseInt(regCode.substr(7,1),16);
    check1 = String(check1).substr(-1);
    //レジストレーションコード7桁目と2桁目
    var check2 = parseInt(regCode.substr(1,1),16) + parseInt(regCode.substr(6,1),16);
    check2 = String(check2).substr(-1);
    //レジストレーションコード6桁目と3桁目
    var check3 = parseInt(regCode.substr(2,1),16) + parseInt(regCode.substr(5,1),16);
    check3 = String(check3).substr(-1);
    //レジストレーションコード5桁目と4桁目
    var check4 = parseInt(regCode.substr(3,1),16) + parseInt(regCode.substr(4,1),16);
    check4 = String(check4).substr(-1);
    //①それぞれ加算した値の下１桁を組み合わせる
    var chkReg = String(check1) + String(check2) + String(check3) + String(check4);

    //②システム日付長整数型
    var d = new Date();
    var ms = d.getTime();

    //①＋②の下４桁を確認番号とする
    var checkNo = parseInt(chkReg) + parseInt(ms);
    makeCheckNo = String(checkNo).substr(-4);

    //確認番号表示
    var htmlString	= "";										//可変HTML文字列
    htmlString += '<span class="string-color">確認番号：</span><span class="string-color">' + makeCheckNo + '</span>';
    $("#G1001CheckNoDisp").html(htmlString);					//可変HTML部分に反映する

  } // end of CheckNoDisp

  /* 確認番号入力時イベント */
  function G1001InCheckNo() {
  var checktarget = [
    { "id" : "G1001InCheckNo", "name" : "確認番号", "length" : "4", "json" : "checkNo", "check" : { "required" : "1", "maxlength" : "1"}}
  ];

    /* 入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    //入力確認番号取得
    var inputNo = $("#G1001InCheckNo").val();

    if(inputNo != makeCheckNo){
    	alert('確認番号が違います');      //エラーメッセージの表示
      return false;
    }

    return true;
  } // end of G1001InCheckNo

  /* アカウント作成ボタン押下時イベント */
  function G1001BtnAccountMake(){

    // アカウント作成ボタン押下フラグＯＮ
    makeBtnClick = true;

    /* アカウントIDチェック */
    if(G1001AccountId() == false){
      makeBtnClick = false;
      return;
    }

    /* パスワードチェック */
    if(G1001Password() == false){
      makeBtnClick = false;
      return;
    }

    /* 氏名チェック */
    if(G1001AccountName() == false){
      makeBtnClick = false;
      return;
    }

    /* レジストレーションコードチェック */
    if(G1001RegistrationCode() == false){
      makeBtnClick = false;
      return;
    }

    /* 確認番号チェック */
    if(G1001InCheckNo() == false){
      makeBtnClick = false;
      return;
    }

    if(makeBtnClick == false){
      return;
    }

    /* 入力項目情報定義 */
    var checktarget = [
      { "id" : "G1001InAccountId", "name" : "アカウントＩＤ", "json" : "accountId", "check" : { "required" : "1"}},
      { "id" : "G1001InPassword", "name" : "パスワード", "json" : "password", "check" : { "required" : "1"}},
      { "id" : "G1001AccountName", "name" : "氏名", "json" : "accountName", "check" : { "required" : "1"}},
      { "id" : "G1001InRegistrationCode", "name" : "レジストレーションコード", "json" : "registrationCode", "check" : { "required" : "1"}},
      { "id" : "G1001InCheckNo", "name" : "確認番号", "json" : "checkNo", "check" : { "required" : "1"}}
    ];

    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

      AccountMake(jsondata);                        					  //アカウント情報作成

  } // end of G1001BtnAccountMake


  /*アカウント情報を作成する */
  function AccountMake(jsondata){

    $.ajax({
        url:"/accountMake", 										//アカウント情報作成
      type:'POST',
      data:JSON.stringify(jsondata),							//入力用JSONデータ
      complete:function(data, status, jqXHR){					//処理成功時
        var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

        if (jsonResult.result == 'SUCCESS') {
            var htmlString	= "";									  //可変HTML文字列
            htmlString = '<div class="row" align="center">';
            htmlString += '<span class="string-color">アカウントを作成しました</span>';
            htmlString += '</div>';
            htmlString += '<div class="row" align="center">';
            htmlString += '<a href="./">ログイン画面へ</a></div>';
            $("#G1001AccountMake").html(htmlString);				  //可変HTML部分に反映する

          //window.location.href = './';	                          //メニュー画面に遷移する
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  } // end of AccountMake

  function onBack() {

	window.location = "/move";

  }

})(jQuery); // end of jQuery name space