/* AGRYELL システムメッセージ登録画面 JQUERY */
(function($){
  var makeBtnClick = false;	//システムメッセージ登録ボタンクリックフラグ
  var makeCheckNo = 0;		//生成確認番号

  /* 初期処理時イベント */
  $(function(){

    var htmlString	= "";										//可変HTML文字列
    //タイトルカード
    htmlString = '<div class="card">';
    htmlString += '<div class="card-content">';
    htmlString += '<p>【AGRYELL】システムメッセージを登録します。</p>';
    htmlString += '</div>';
    htmlString += '<div class="row">';
    htmlString += '<div class="card-action center">';
    //htmlString += '<div class="card-action">';
    htmlString += '<form>';
    //更新日付
    htmlString += '<div class="row">';
    htmlString += '<div class="input-field col s10 offset-s1">';
    htmlString += '<input type="text" id="G0010UpdateDay" class="datepicker blockquote-input" value="">';
    htmlString += '<label for="G0010UpdateDay">更新日を入力してください</label>';
    htmlString += '</div>';
    htmlString += '</div>';

    //システムメッセージ
    htmlString += '<div class="row">';
    htmlString += '<div class="input-field col s10 offset-s1">';
    htmlString += '<input id="G0010InSystemMessage" type="text" class="validate" maxlength="2000" style="ime-mode: active;">';
    htmlString += '<label for="G1001InAccountId" class="left-align">システムメッセージを入力してください</label>';
    htmlString += '</div>';
    htmlString += '</div>';
    htmlString += '</form>';
    //システムメッセージ登録ボタン
    htmlString += '<p><div id="G0010BtnSystemMessageMake" class="waves-effect waves-light btn white-text">登録</div></p>';
    htmlString += '</div>';
    htmlString += '</div>';
    $("#G0010SystemMessageMake").html(htmlString);					//可変HTML部分に反映する

    /*----- 各コントロールにイベントを実装する -----*/
    $("#G0010InSystemMessage").bind('change', G0010SystemMessage );         //システムメッセージ
    $("#G0010BtnSystemMessageMake").bind('click', G0010SystemMessageMake ); //システムメッセージ登録ボタン

    $(document).ready(function(){
    });
  }); // end of document ready

  /* システムメッセージ入力時イベント */
  function G0010SystemMessage() {
    /* 入力項目情報定義 */
    var checktarget = [
        { "id" : "G0010InSystemMessage", "name" : "システムメッセージ", "length" : "2000", "json" : "message", "check" : { "required" : "1","maxlength" : "1"}}
    ];

      /* 入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

      return true;
  } // end of G0010SystemMessage

  /* システムメッセージ登録ボタン押下時イベント */
  function G0010SystemMessageMake(){

    // システムメッセージ登録ボタン押下フラグＯＮ
    makeBtnClick = true;

    /* システムメッセージチェック */
    if(G0010SystemMessage() == false){
      makeBtnClick = false;
      return;
    }

    /* 入力項目情報定義 */
    var checktarget = [
      { "id" : "G0010UpdateDay", "name" : "更新日付", "json" : "updateTime", "check" : { "required" : "1"}},
      { "id" : "G0010InSystemMessage", "name" : "システムメッセージ", "json" : "message", "check" : { "required" : "1","maxlength" : "1"}},
    ];

    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

      SystemMessageMake(jsondata);                        					  //アカウント情報作成

  } // end of G0010SystemMessageMake


  /*アカウント情報を作成する */
  function SystemMessageMake(jsondata){

    $.ajax({
        url:"/systemMessageMakeSubmit", 						//システムメッセージ登録
      type:'POST',
      data:JSON.stringify(jsondata),							//入力用JSONデータ
      complete:function(data, status, jqXHR){					//処理成功時
        var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

        if (jsonResult.result == 'SUCCESS') {
            var htmlString	= "";									  //可変HTML文字列
            htmlString = '<div class="row" align="center">';
            htmlString += '<span>システムメッセージを登録しました</span>';
            htmlString += '</div>';
            htmlString += '<div class="row" align="center">';
            htmlString += '<a href="./">ログイン画面へ</a></div>';
            $("#G0010SystemMessageMake").html(htmlString);			  //可変HTML部分に反映する

          //window.location.href = './';	                          //メニュー画面に遷移する
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  } // end of SystemMessageMake

})(jQuery); // end of jQuery name space