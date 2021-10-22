/* AGRYEEL ログイン画面 JQUERY */
(function($){

  /* 初期処理時イベント */
  $(function(){

    var key= localStorage.getItem("accountId");

    if (key != null) {
      var url = "/" + key + "/hashLogin";
      $.ajax({
        url:url,
      type:'GET',
      complete:function(data, status, jqXHR){         //処理成功時
        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        //ここに戻るという事は認証エラー
        if (jsonResult.result == 'SUCCESS') {
          if (jsonResult.work != 0) {  //該当ユーザが作業中の場合
            if (jsonResult.field != 0) {
              //通常作業
              var inputJson = {"workid":"", "kukakuid":"", "action":""};
              inputJson.action = "display";
              inputJson.workid = jsonResult.work;
              inputJson.kukakuid = jsonResult.field;
              $.ajax({
                url:"/initparam",
                type:'POST',
                data:JSON.stringify(inputJson),               //入力用JSONデータ
                complete:function(data, status, jqXHR){           //処理成功時
                  var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
                  window.location.href = "/workingmove";
              },
                dataType:'json',
                contentType:'text/json'
              });
            }
            else {
              //育苗作業
              var inputJson = {"workid":"", "planid":"", "action":""};
              inputJson.action = "display";
              inputJson.workid = jsonResult.work;
              inputJson.planid = jsonResult.planId;
              $.ajax({
                url:"/initikubyoparam",
                type:'POST',
                data:JSON.stringify(inputJson),               //入力用JSONデータ
                complete:function(data, status, jqXHR){           //処理成功時
                  var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
                  window.location.href = "/workingikubyomove";
              },
                dataType:'json',
                contentType:'text/json'
              });
            }
          }
          else {
            if (jsonResult.firstPage == 3) {
              window.location.href = '/workPlanMove';                              //作業指示画面に遷移する
            }
            else {
              window.location.href = '/menuMove';                                  //メニュー画面に遷移する
            }
          }
        }
        else {
          /*----- 初期値設定 -----*/
          $("#G0001InAccountId").val("");               //空白を設定する
          $("#G0001InPassword").val("");                //空白を設定する

          /*----- 各ボタンにクリックイベントを押下する -----*/
          $("#G0001BtnLogin").bind('click', G0001Login );             //通常ログインボタン
          $("#G0001BtnTrialLogin").bind('click', trialLogin );        //お試し利用ボタン
          $("#G0001BtnGoogleLogin").bind('click', googleLogin );      //通常ログインボタン
        }
      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
    }
    else {
      /*----- 初期値設定 -----*/
      $("#G0001InAccountId").val("");               //空白を設定する
      $("#G0001InPassword").val("");                //空白を設定する

      /*----- 各ボタンにクリックイベントを押下する -----*/
      $("#G0001BtnLogin").bind('click', G0001Login );             //通常ログインボタン
      $("#G0001BtnTrialLogin").bind('click', trialLogin );        //お試し利用ボタン
      $("#G0001BtnGoogleLogin").bind('click', googleLogin );      //通常ログインボタン
    }

  }); // end of document ready

  /* 通常ログインボタン押下時イベント */
  function G0001Login(){

	/* 当画面の入力項目情報定義 */
	var checktarget = [
			{ "id" : "G0001InAccountId", "name" : "アカウントＩＤ", "json" : "accountId", "check" : { "required" : "1"}}
		   ,{ "id" : "G0001InPassword", "name" : "パスワード", "json" : "password", "check" : { "required" : "1 "}}
	];

    /* 各入力項目のチェック */
	if (InputDataManager(checktarget) == false) {
		return;
	}

	var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

    AccountCheck(jsondata);                        					  //アカウント認証


  } // end of G0001Login

  /* グーグルログインボタン押下時イベントはHTML側で実装 */

  /* アカウント情報をサーバから取得する */
  function AccountCheck(jsondata){

      $.ajax({
        url:"/accountLogin",                    //アカウントログイン処理
      type:'POST',
      data:JSON.stringify(jsondata),              //入力用JSONデータ
      complete:function(data, status, jqXHR){         //処理成功時
        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

        if (jsonResult.result == 'SUCCESS') {
          localStorage.setItem("accountId", jsonResult.accountId);
          //バックモード関係の項目をクリアする
          localStorage.setItem("backMode"         , null);
          localStorage.setItem("backFieldGroupId" , null);
          localStorage.setItem("backFieldId"      , null);
          localStorage.setItem("backKukakuId"     , null);
          localStorage.setItem("backWorkId"       , null);
          localStorage.setItem("backWorkDiaryId"  , null);
          localStorage.setItem("backAccountId"    , null);
          localStorage.setItem("backWorkDate"     , null);

          if (jsonResult.work != 0) {  //該当ユーザが作業中の場合
            if (jsonResult.field != 0) {
              //通常作業
              var inputJson = {"workid":"", "kukakuid":"", "action":""};
              inputJson.action = "display";
              inputJson.workid = jsonResult.work;
              inputJson.kukakuid = jsonResult.field;
              $.ajax({
                url:"/initparam",
                type:'POST',
                data:JSON.stringify(inputJson),               //入力用JSONデータ
                complete:function(data, status, jqXHR){           //処理成功時
                  var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
                  window.location.href = "/workingmove";
              },
                dataType:'json',
                contentType:'text/json'
              });
            }
            else {
              //育苗作業
              var inputJson = {"workid":"", "planid":"", "action":""};
              inputJson.action = "display";
              inputJson.workid = jsonResult.work;
              inputJson.planid = jsonResult.planId;
              $.ajax({
                url:"/initikubyoparam",
                type:'POST',
                data:JSON.stringify(inputJson),               //入力用JSONデータ
                complete:function(data, status, jqXHR){           //処理成功時
                  var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
                  window.location.href = "/workingikubyomove";
              },
                dataType:'json',
                contentType:'text/json'
              });
            }
          }
          else {
            if (jsonResult.firstPage == 3) {
              window.location.href = '/workPlanMove';                              //作業指示画面に遷移する
            }
            else {
              window.location.href = '/menuMove';                                  //メニュー画面に遷移する
            }
          }
        }
        else if (jsonResult.result == 'PASSWORDUNMATCH') {
          displayToast('パスワード' + 'が一致しません。', 4000, 'rounded');      //エラーメッセージの表示
        }
        else {
          displayToast('アカウントＩＤ' + 'が存在しません。', 4000, 'rounded');  //エラーメッセージの表示
        }
      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  } // end of G0001Login

  /* お試し利用ボタン押下時イベント */
  function trialLogin(){

    // お試しユーザ設定
    $("#G0001InAccountId").val("aica");
    $("#G0001InPassword").val("aica0123");

    // ログイン
    G0001Login()


  } // end of G0001Login

  function googleLogin() {


		/*----- SNS認証初期設定 -----*/
		hello.init({
			google: '1010999826638-5ebh2mnio4duj1js0tf8mutkmbi8mlbd.apps.googleusercontent.com'
		});

		  hello('google').login().then(function() {

			  hello('google').api('me').then(function(json) {

		        	$("#googleId").val(json.id);															/* googleIDをパラメータに設定する */

		        	/* 当画面の入力項目情報定義 */
		        	var checktarget = [
		        			{ "id" : "googleId", "name" : "GoogleID", "json" : "googleId", "check" : {}}
		        	];

		        	var jsondata   = InputDataToJson(checktarget);					  						//JSONDATAに変換する

		        	$.ajax({
		        		url:"/googleLogin",																	//アカウントログイン処理
		        		type:'POST',
		        		data:JSON.stringify(jsondata),														//入力用JSONデータ
		        		complete:function(data, status, jqXHR){												//処理成功時
		        			var jsonResult = JSON.parse( data.responseText );								//戻り値用JSONデータの生成

		        			if (jsonResult.result == "SUCCESS") {

		        			    if (jsonResult.work != 0) {  //該当ユーザが作業中の場合
                        window.location.href = '/workingmove';                               //作業中画面に遷移する
		        			    }
		        			    else {
	                      window.location.href = '/menuMove';                                  //メニュー画面に遷移する
		        			    }
		        			}
		        			else {

		    					displayToast('アカウントが存在しません', 4000, 'rounded');   			//エラーメッセージの表示

		        			}
		        		},
		    	        cache: false,
		        		dataType:'json',
		        		contentType:'text/json',
		        		async: false
		        	});

				}, function(e) {

					displayToast('Google認証時に想定外エラーが発生しました', 4000, 'rounded');   //エラーメッセージの表示

				});

			}, function(e) {

				displayToast('Google認証に失敗しました', 4000, 'rounded');      				//エラーメッセージの表示

			});

  }

})(jQuery); // end of jQuery name space