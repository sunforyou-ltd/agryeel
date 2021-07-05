/* AGRYEEL システム共通 JQUERY */

/* 共通変数 */
var accountInfo = [{"accountId":"", "farmId":""}];
/* 単位種別 */
var unitKind	= {"0":"", "1":"Kg", "2":"L", "3":"g", "4":"mL", "5":"個"};
/* 処理中フラグ */
var procFlag	= false;
/* テンプレートID */
var templateIdEnd = 7;

/* ユーザ情報 */
var userinfo  = {id:"", name:"", farm:0, field: 0, work: 0, start: ""};

var TRANS_HOJOG = 0.4;
var TRANS_HOJO  = 0.1;
var TRANS_TIMELINE = 0.1;
var TRANS_WORK = 0.1;
var TRANS_MENU = 0;
var TRANS_AWAREA = 0.4;
var COLOR_WORKNAME = "#eeeeee";
var COLOR_AWTIMELINE = "rgba( 170, 170 ,170, 0.2)";
var COLOR_AWTIME = "rgba( 170, 170 ,170, 0.4)";
var COLOR_AWITEM = "rgba( 200, 200 , 200, 1.0)";
var COLOR_AWSUB = "rgba( 200, 200 , 200, 0.6)";

//----- オープン関数に変更 ------
//------------------------------------------------------------------------------------------------------------------
//- タイムライン初期検索
//------------------------------------------------------------------------------------------------------------------
function timelineInit() {
  var tl = $("#timelinelist");
  if (tl != undefined) {
    $.ajax({
      url:"/getTimeLineInitData",
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時

        var jsonResult = JSON.parse( data.responseText );

        $("#timelinelist").empty();

        var htmlString  = timelineDisplay(jsonResult);                   //可変HTML文字列

        $("#timelinelist").html(htmlString);         //可変HTML部分に反映する

        $(".timelinelist").unbind("click");
        $(".timelinelist").bind("click", workDiaryEdit);

      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  }
}
//タイムライン検索のインスタンス変数
var oTimeLine = {start: "", end: "", work:"", account: ""};
//------------------------------------------------------------------------------------------------------------------
//- タイムライン検索条件取得時
//------------------------------------------------------------------------------------------------------------------
function getTimeLineWhere() {
  var url = "/getAccountInfo";
  $.ajax({
    url:url,
    type:'GET',
    complete:function(data, status, jqXHR){
      var jsonResult = JSON.parse( data.responseText );

      if (jsonResult.result == 'SUCCESS') {
        //----- 検索開始日付 -----
        var start = jsonResult.selectStartDate.substr(0, 4) + "/" + jsonResult.selectStartDate.substr(4, 2) + "/" + jsonResult.selectStartDate.substr(6, 2);
        $('#G0002TimeLineF').val(start);
        $('#G0002TimeLineF').datepicker('setDate', new Date(start));
        //----- 検索終了日付 -----
        var end = jsonResult.selectEndDate.substr(0, 4) + "/" + jsonResult.selectEndDate.substr(4, 2) + "/" + jsonResult.selectEndDate.substr(6, 2);
        $('#G0002TimeLineT').val(end);
        $('#G0002TimeLineT').datepicker('setDate', new Date(end));

        //----- 作業一覧 -----
        mSelectDataGet("#G0002TimeLineWork", "getWorkOfFarm");
        var works = new String(jsonResult.selectWorkId).split(",");

        for (var key in works) {
          var data = works[key];
          var oJson = mSelectData(data);
          if (oJson != undefined) {
            oJson.select = true;
          }
        }
        mSelectClose();
        //----- 担当者一覧 -----
        mSelectDataGet("#G0002TimeLineAccount", "getAccountOfFarm");
        var accounts = new String(jsonResult.selectAccountId).split(",");

        for (var key in accounts) {
          var data = accounts[key];
          var oJson = mSelectData(data);
          if (oJson != undefined) {
            oJson.select = true;
          }
        }
        mSelectClose();
        //----- 表示条件一覧 -----
        selectDataGet("#G0002TimeLineWorking", "getTimeLineWorking");
        var accounts = new String(jsonResult.selectAccountId).split(",");
        var oJson = selectData(jsonResult.selectWorking);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();
      }
    },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
}
//------------------------------------------------------------------------------------------------------------------
//- タイムライン検索条件生成時
//------------------------------------------------------------------------------------------------------------------
function timelinemenu() {
  var tm = $('#timelinemenu');
  if (tm != undefined) {
    tm.empty();

    tm.append('<h6 class="">タイムライン検索条件</h6>');     //コンテンツヘッダーを生成する
    tm.append('<div class="row">');
    tm.append('<div class="col s12 input-field">');
    tm.append('<input type="text" placeholder="検索期間開始" id="G0002TimeLineF" class="datepicker input-text-color" style="">');
    tm.append('</div>');
    tm.append('<div class="col s12 input-field">');
    tm.append('<input type="text" placeholder="検索期間終了" id="G0002TimeLineT" class="datepicker input-text-color" style="">');
    tm.append('</div>');
    tm.append('<div class="row">');
    tm.append('<div class="col s12">');
    tm.append('<span class="mselectmodal-trigger-title">作業</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="作業一覧" data="getWorkOfFarm" displayspan="#G0002TimeLineWork"><span id="G0002TimeLineWork" class="blockquote-input">未選択</span></a>');
    tm.append('</div>');
    tm.append('</div>');
    tm.append('<div class="row">');
    tm.append('<div class="col s12">');
    tm.append('<span class="mselectmodal-trigger-title">担当者</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="担当者一覧" data="getAccountOfFarm" displayspan="#G0002TimeLineAccount"><span id="G0002TimeLineAccount" class="blockquote-input">未選択</span></a>');
    tm.append('</div>');
    tm.append('</div>');
    tm.append('<div class="row">');
    tm.append('<div class="col s12">');
    tm.append('<span class="mselectmodal-trigger-title">表示条件</span><a href="#selectmodal"  class="selectmodal-trigger" title="表示条件" data="getTimeLineWorking" displayspan="#G0002TimeLineWorking"><span id="G0002TimeLineWorking" class="blockquote-input">未選択</span></a>');
    tm.append('</div>');
    tm.append('</div>');
    tm.append('<div class="row">');
    tm.append('<div class="col s12">');
    tm.append('<a href="#!" id="timelineback" class="waves-effect waves-green btn-flat right string-color" style="">閉じる</a>');
    tm.append('<a href="#!" id="timelinecommit" class="waves-effect waves-green btn-flat right string-color" style="">確　定</a>');
    tm.append('</div>');
    tm.append('</div>');

    $('#timelinecommit').unbind("click");
    $('#timelinecommit').bind("click", timelineCommit);
    $('#timelineback').unbind("click");
    $('#timelineback').bind("click", timelineClose);

    //------------------------------------------------------------------------------------------------------------------
    //- タイムライン検索条件の初期化
    //------------------------------------------------------------------------------------------------------------------
    $('#timelinetrigger').unbind('click');
    $('#timelinetrigger').bind('click', timelineModalOpen);

    tm.hide();

  }
}
//------------------------------------------------------------------------------------------------------------------
//- タイムライン検索条件オープン時
//------------------------------------------------------------------------------------------------------------------
function timelineModalOpen() {
  var tm = $('#timelinemenu');
  var ms = $("#mainsection");
  getTimeLineWhere();     //タイムライン検索条件取得
  if (ms != undefined) {
    ms.fadeOut(0);
  }
  if (tm != undefined) {
    tm.fadeIn(500);
  }
}
//------------------------------------------------------------------------------------------------------------------
//- タイムライン検索条件確定時
//------------------------------------------------------------------------------------------------------------------
function timelineCommit() {

  //入力チェック
  var from = $("#G0002TimeLineF").val();
  var to = $("#G0002TimeLineT").val();

  if (from == "") {
    displayToast('検索期間開始が未入力です', 4000, 'rounded');
    return;
  }
  if (to == "") {
    displayToast('検索期間終了が未入力です', 4000, 'rounded');
    return;
  }

  var work = mSelectConvertJson("#G0002TimeLineWork");
  var account = mSelectConvertJson("#G0002TimeLineAccount");
  var working = mSelectConvertJson("#G0002TimeLineWorking");

  var jsondata = {};

  jsondata["from"]    = from;
  jsondata["to"]      = to;
  jsondata["work"]    = work;
  jsondata["account"] = account;
  jsondata["working"] = working;

  console.log("jsondata", jsondata);

  $.ajax({
    url:"/timelineStatusCommit",
    type:'POST',
    data:JSON.stringify(jsondata),                //入力用JSONデータ
    complete:function(data, status, jqXHR){           //処理成功時

      $.ajax({
        url:"/getTimeLineData",
        type:'GET',
        complete:function(data, status, jqXHR){           //処理成功時

          var jsonResult = JSON.parse( data.responseText );

          $("#timelinelist").empty();

          var htmlString  = timelineDisplay(jsonResult);                   //可変HTML文字列

          $("#timelinelist").html(htmlString);         //可変HTML部分に反映する

          $(".timelinelist").unbind("click");
          $(".timelinelist").bind("click", workDiaryEdit);

          var tm = $('#timelinemenu');
          var ms = $("#mainsection");
          if (tm != undefined) {
            tm.fadeOut(0);
          }
          if (ms != undefined) {
            ms.fadeIn(500);
          }
        },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
    },
    dataType:'json',
    contentType:'text/json',
    async: false
  });

}
//------------------------------------------------------------------------------------------------------------------
//- タイムライン表示
//------------------------------------------------------------------------------------------------------------------
function timelineDisplay(jsonResult) {

  var htmlString  = "";                   //可変HTML文字列
  timeLineList  = jsonResult.targetTimeLine;

  //----- ここからタイムラインの編集 ----
  htmlString += '<div class="">';
  htmlString += '<ul class="timeline">';
  for ( var timeLineKey in timeLineList ) {             //タイムライン件数分処理を行う

    var timeLine    = timeLineList[timeLineKey];    //タイムライン情報の取得
    var timeLineColor = "";

    var code  = timeLine["timeLineColor"];
    var red   = parseInt(code.substring(0,2), 16);
    var green = parseInt(code.substring(2,4), 16);
    var blue  = parseInt(code.substring(4,6), 16);

    htmlString += '<li class="timelinelist" key="' + timeLine["workDiaryId"] + '" style="border-left: 8px solid #' + timeLine["timeLineColor"] + '; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_TIMELINE + '); border-bottom: 0px;">';
    htmlString += '<div class="work-title">';
    htmlString += icontag(timeLine["workId"], timeLine["timeLineColor"], timeLine["workName"], "timeline");
    htmlString += '<span class="account-text">' + timeLine["accountName"] + '</span>';
    htmlString += '<span class="work-workdate-text">'  + timeLine["workdate"] + '</span>';
    htmlString += '<span class="work-text">' + timeLine["kukakuName"] + '&nbsp;&nbsp;&nbsp;&nbsp;' + timeLine["workName"] + '&nbsp;&nbsp;(' + timeLine["worktime"] + '分)&nbsp;&nbsp;&nbsp;&nbsp;</span>';
    htmlString += '<span class="work-update-text">'  + timeLine["updateTime"] + '</span>';
    htmlString += '</div>';
    htmlString += '<div class="message">';
    htmlString += '<span>' + timeLine["message"] + '</span>';
    htmlString += '</div>';
    htmlString += '</li>';

  } // timeLineList
  htmlString += '</ul>';
  htmlString += '</div>';

  return htmlString;

}
function workDiaryEdit() {
  var timeline = $(this);
  if (timeline.attr("key") < 0) { //作業中の場合
    displayToast('現在作業中です。', 4000, '');
    return;
  }
  window.location.href = "/" + timeline.attr("key") + "/workDiaryEdit";
}
//------------------------------------------------------------------------------------------------------------------
//- タイムライン検索条件閉じる時
//------------------------------------------------------------------------------------------------------------------
function timelineClose() {
  var tm = $('#timelinemenu');
  var ms = $("#mainsection");
  if (tm != undefined) {
    tm.fadeOut(0);
  }
  if (ms != undefined) {
    ms.fadeIn(500);
  }
}

(function($){

  //メインメニューの制御情報
  var oMenu = { area: false, open: false };

  $(document).ready(function(){

    getAccountInfo();       //アカウント情報の取得
    meinmenu();             //メインメニューの生成
    timelinemenu();         //タイムラインメニュー
    timelineInit();         //タイムライン初期検索
    noneAutoComplete();     //オートコンプリート無効化

  }); //end of document.ready

  //------------------------------------------------------------------------------------------------------------------
  //- オートコンプリート無効化
  //------------------------------------------------------------------------------------------------------------------
  function noneAutoComplete() {
    $("input").prop("autocomplete", "off");
  }

  //------------------------------------------------------------------------------------------------------------------
  //- アカウント情報取得
  //------------------------------------------------------------------------------------------------------------------
  function getAccountInfo() {
    var url = "/getAccountInfo";
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        if (jsonResult.result == 'SUCCESS') {
          userinfo.id     = jsonResult.id;
          userinfo.name   = jsonResult.name;
          userinfo.farm   = jsonResult.farmid;
          userinfo.field  = jsonResult.field;
          userinfo.work   = jsonResult.work;
          userinfo.start  = jsonResult.start;
        }
      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }
  //------------------------------------------------------------------------------------------------------------------
  //- メインメニューの自動生成
  //------------------------------------------------------------------------------------------------------------------
  function meinmenu() {
    //- メインメニューエリアを取得する -
    var mm = $("#mainmenu");
    if (mm != undefined ) { // 該当ページにメインメニューエリアが存在する
      mm.empty();           //メニューエリアの内容を削除
      mm.append('<ul id="mainmenulist" class="collection" style="border: 0px;">'); //メニューリストを生成する
      var ml = $("#mainmenulist");                            //メニューリストを取得する
      if (ml != undefined ) {                                 //メニューリストが存在する場合
        //- 各メニューを自動的に生成する -
        ml.append('<li class="collection-item menuitem main-trriger" style="border-left-color:#009688; background-color: rgba(0, 150, 136, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#009688;">menu</i><span style="color:#009688;">メインメニュー</span></li>');
        if (userinfo.work != 0) {
          ml.append('<li class="collection-item menuitem working-trriger" style="border-left-color:#d32f2f; background-color: rgba(211, 47, 47, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#d32f2f;">gavel</i><span style="color:#d32f2f;">作業中</span></li>');
        }
        ml.append('<li class="collection-item menuitem wkaccount-trriger" style="border-left-color:#1976d2; background-color: rgba(25, 118, 210, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#1976d2;">gavel</i><span style="color:#1976d2;">担当者別作業一覧</span></li>');
        ml.append('<li class="collection-item menuitem mast-trriger" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">マスタ設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="0" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">アカウント設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="1" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">地主設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="2" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">圃場グループ設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="3" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">圃場設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="4" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">区画設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="5" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">生産物グループ設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="6" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">生産物設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="7" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">農肥設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="8" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">品種設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="9" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">ベルト設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="10" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">機器設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="11" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">アタッチメント設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="12" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">荷姿設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="13" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">質設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="14" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">サイズ設定</span></li>');
        ml.append('<li class="collection-item menuitem menu-trriger" gmnid="15" style="border-left-color:#7b1fa2; background-color: rgba(123, 31, 162, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#7b1fa2;">settings</i><span style="color:#7b1fa2;">資材設定</span></li>');
        ml.append('<li id="menu-logout" class="collection-item menuitem" style="border-left-color:#FFA000; background-color: rgba(255, 160, 0, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#FFA000;">transfer_within_a_station</i><span style="color:#FFA000;">ログアウト</span></li>');
        ml.append('<li id="menuclose" class="collection-item menuitem" style="border-left-color:#512DA8; background-color: rgba(81, 45, 168, ' + TRANS_MENU + ') !important;"><i class="material-icons" style="color:#512DA8;">close</i><span style="color:#512DA8;">閉じる</span></li>');
      }
      //- クローズメニューにイベントを設定する
      var cml = $('#menuclose');
      if (cml != undefined ) { //クローズメニューが存在する場合
        cml.unbind('click');
        cml.bind('click', ctlMainmenu);
      }
      //- メニューエリアオープン用FABを設定する -
      $('.nav-item div').append('<a id="menubtn" class="waves-effect waves-light nav-menu right scale-transition"><i class="material-icons">menu</i></a>');
      //- FABにイベントを設定する
      var fab = $('#menubtn');
      if (fab != undefined ) { //FABが存在する場合
        fab.unbind('click');
        fab.bind('click', ctlMainmenu);
      }

      var logout = $('#menu-logout');
      if (logout != null) {
        logout.bind("click", UserLogout);
      }

      $(".main-trriger").unbind("click");
      $(".main-trriger").bind("click", mainmove);
      $(".mast-trriger").unbind("click");
      $(".mast-trriger").bind("click", dispmast);
      $('.menu-trriger').hide();
      $(".working-trriger").unbind("click");
      $(".working-trriger").bind("click", workingmove);
      $(".wkaccount-trriger").unbind("click");
      $(".wkaccount-trriger").bind("click", workingaccountmove);

      var mst = $(".menu-trriger");
      mst.unbind('click');
      mst.bind('click', menumove);

      //- 初期画面はメニューエリアを非表示にする -
      mm.hide();
      oMenu.area = true;

    }
    else {
      oMenu.area = false;
    }
  }

  //------------------------------------------------------------------------------------------------------------------
  //- メインメニュー画面遷移
  //------------------------------------------------------------------------------------------------------------------
  function mainmove() {
    var url = "/menuMove";

    window.location.href = url;

  }
  //------------------------------------------------------------------------------------------------------------------
  //- 作業中画面遷移
  //------------------------------------------------------------------------------------------------------------------
  function workingmove() {
    var url = "/workingmove";

    window.location.href = url;

  }
  //------------------------------------------------------------------------------------------------------------------
  //- 担当者別作業一覧画面遷移
  //------------------------------------------------------------------------------------------------------------------
  function workingaccountmove() {
    var url = "/workingaccountmove";

    window.location.href = url;

  }
  //------------------------------------------------------------------------------------------------------------------
  //- マスタメンテナンス表示切替
  //------------------------------------------------------------------------------------------------------------------
  var bMast = false;
  function dispmast() {

    if (bMast) {
      $('.menu-trriger').hide();
      bMast = false;
    }
    else {
      $('.menu-trriger').show();
      bMast = true;    }

  }
  //------------------------------------------------------------------------------------------------------------------
  //- 画面遷移
  //------------------------------------------------------------------------------------------------------------------
  function menumove() {
    var item = $(this);
    var url = "/" + userinfo.farm + "/" + item.attr("gmnid") + "/masterMntMove";

    window.location.href = url;

  }

  //------------------------------------------------------------------------------------------------------------------
  //- ユーザログアウトを実行する
  //------------------------------------------------------------------------------------------------------------------
  function UserLogout() {

      $.ajax({
          url:"/userLogout",
          type:'GET',
          complete:function(data, status, jqXHR){           //処理成功時


            window.location.href = "/move";


          },
          dataType:'json',
          contentType:'text/json'
        });

  }

  //------------------------------------------------------------------------------------------------------------------
  //- メインメニューの制御
  //------------------------------------------------------------------------------------------------------------------
  function ctlMainmenu() {
    if (oMenu.area) {   //メニューエリアが存在する場合
      //- メインメニューエリアを取得する -
      var mm = $("#mainmenu");
      var ms = $("#mainsection");
      if (mm != undefined ) { // 該当ページにメインメニューエリアが存在する
        if (oMenu.open) {     //メニューエリアが表示されている
          mm.fadeOut(0);   //メニューエリアを非表示
          ms.fadeIn(500);
          oMenu.open = false;
          //- FABを表示する -
          var fab = $('#menubtn');
          if (fab != undefined ) { //FABが存在する場合
            fab.removeClass('scale-out');
          }
        }
        else {
          ms.fadeOut(0);
          mm.fadeIn(500);         //メニューエリアを表示
          oMenu.open = true;
          //- FABを非表示にする -
          var fab = $('#menubtn');
          if (fab != undefined ) { //FABが存在する場合
            fab.addClass('scale-out');
          }
        }
      }
    }
  }
})(jQuery); // end of jQuery name space

/*----- 入力チェック関数群 -----*/
/* 入力項目の値の収集及びチェックを行う */
function InputDataManager(targetList) {

  var result        = true;                                                     //チェック結果をONに設定
  var aryInput      = {};                                                       //収集JSONデータ配列の初期化

  for ( var targetKey in targetList ) {                                         //収集対象項目数分処理を行う
    var target = targetList[targetKey];                                         //収集対象項目情報の取得

    for ( var checkKey in target["check"] ) {                                   //チェック項目数分処理を行う
      var check = target["check"][checkKey]                                     //チェック対象値を取得

      if (check == 1) {                                                         //チェック対象の場合
        if (checkKey == "required") {                                           //必須チェックの場合
          result = result & RequiredCheck(target["id"], target["name"])         //必須チェックを行う
        }
        else if (checkKey == "select") {                                        //選択チェックの場合
          result = result & SelectCheck(target["id"], target["name"])           //選択チェックを行う
        }
        else if (checkKey == "number") {                                        //数値チェックの場合
          result = result & NumberCheck(target["id"], target["name"])           //数値チェックを行う
        }
        else if (checkKey == "kanji") {                                         //漢字チェックの場合
          result = result & KanjiCheck(target["id"], target["name"])            //漢字チェックを行う
        }
        else if (checkKey == "furigana") {                                      //フリガナチェックの場合
          result = result & FuriganaCheck(target["id"], target["name"])         //フリガナチェックを行う
        }
        else if (checkKey == "telfax") {                                        //電話番号＆ＦＡＸチェックの場合
          result = result & TelFaxCheck(target["id"], target["name"])           //電話番号＆ＦＡＸチェックを行う
        }
        else if (checkKey == "malladdress") {                                   //メールアドレスチェックの場合
          result = result & MalladdressCheck(target["id"], target["name"])      //メールアドレスチェックを行う
        }
        else if (checkKey == "password") {                                		//パスワードチェックの場合
            result = result & PasswordCheck(target["id"], target["name"])   	//パスワードチェックを行う
        }
        else if (checkKey == "maxlength") {                                				 //最大桁数チェックの場合
            result = result & MaxLength(target["id"], target["name"], target["length"])  //最大桁数チェックを行う
        }

        if (!result) {                                                          //入力エラーの場合
          break;                                                                //処理を中断する
        }
      }
    }
    if (!result) {                                                              //入力エラーの場合
      break;                                                                    //処理を中断する
    }
  }
  return result;
}

/* 必須チェック */
function RequiredCheck( targetId, targetName) {

  var result        = true;                               //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();            //指定されたIDの値を取得

  if (requiredValue == "") {                              //値が未入力の場合
    displayToast(targetName + 'が未入力です', 4000, 'rounded');  //エラーメッセージの表示
    result        = false;                                //チェック結果をOFFに設定
  }

  return result;                                          //戻り値を返す

}

/* 選択チェック */
function SelectCheck( targetId, targetName) {

  var result        = true;                               //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();            //指定されたIDの値を取得

  if (requiredValue == 0) {                               //値が未選択値の場合
    displayToast(targetName + 'が未選択です', 4000, 'rounded');  //エラーメッセージの表示
    result        = false;                                //チェック結果をOFFに設定
  }

  return result;                                          //戻り値を返す

}

/* 数値チェック */
function NumberCheck( targetId, targetName) {

  var result        = true;                                                   //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();                                //指定されたIDの値を取得

  if(requiredValue == ""){													  //空白の場合
	  return result;
  }

  if (!requiredValue.match(/^([1-9]\d*|0)(\.\d+)?$/)) {                       //値が数値以外の場合
    displayToast(targetName + 'は半角数字のみ入力可能です', 4000, 'rounded'); //エラーメッセージの表示
    result        = false;                                                    //チェック結果をOFFに設定
  }

  return result;                                                              //戻り値を返す

}

/* 漢字チェック */
function KanjiCheck( targetId, targetName) {

  var result        = true;                                                   //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();                                //指定されたIDの値を取得

  if (!requiredValue.match(/^[ぁ-んァ-ヶー一-龠 　\r\n\t]+$/)) {              //値がふりがな、カタカナ、ー、漢字、全角半角スペース以外の場合
    displayToast(targetName + 'が不正です', 4000, 'rounded');//エラーメッセージの表示
    result        = false;                                                    //チェック結果をOFFに設定
  }

  return result;                                                              //戻り値を返す

}

/* フリガナチェック */
function FuriganaCheck( targetId, targetName) {

  var result        = true;                                                   //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();                                //指定されたIDの値を取得

  if(requiredValue == ""){													  //空白の場合
	  return result;
  }

  //if (!requiredValue.match(/^[ぁ-んー 　\r\n\t]*$/)) {                  		//値がひらがな、ー、全角半角スペース以外の場合
  if (!requiredValue.match(/^[\u3040-\u309f ー 　]+$/)) {          	            //値がひらがな、ー、全角半角スペース以外の場合
    displayToast(targetName + 'が不正です', 4000, 'rounded');//エラーメッセージの表示
    result        = false;                                                    //チェック結果をOFFに設定
  }

  return result;                                                              //戻り値を返す

}

/* 電話番号＆ＦＡＸチェック */
function TelFaxCheck( targetId, targetName) {

  var result        = true;                                                         //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();                                      //指定されたIDの値を取得

  if(requiredValue == ""){															//空白の場合
	  return result;
  }
  if (!requiredValue.match(/^[0-9\-]+$/)) {                                         //値が数字とハイフン以外の場合
    displayToast(targetName + 'は半角数字又はハイフンのみ入力可能です', 4000, 'rounded');  //エラーメッセージの表示
    result        = false;                                                          //チェック結果をOFFに設定
  }

  return result;                                                                    //戻り値を返す

}

/* メールアドレスチェック */
function MalladdressCheck( targetId, targetName) {

  var result        = true;                                                                               //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();                                                            //指定されたIDの値を取得

  if(requiredValue == ""){																				  //空白の場合
	  return result;
  }
  if (!requiredValue.match(/^([a-zA-Z0-9])+([a-zA-Z0-9\._-])*@([a-zA-Z0-9_-])+([a-zA-Z0-9\._-]+)+$/)) {   //値が数字とハイフン以外の場合
    displayToast(targetName + 'が不正です', 4000, 'rounded');                            //エラーメッセージの表示
    result        = false;                                                                                //チェック結果をOFFに設定
  }

  return result;                                                                                          //戻り値を返す

}

/* パスワードチェック */
function PasswordCheck( targetId, targetName) {

  var result        = true;                                                                               //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();                                                            //指定されたIDの値を取得

  if (!requiredValue.match(/^[a-zA-Z0-9]+$/)) {   			  											  //値が半角英数字以外の場合
      displayToast(targetName + 'が不正です', 4000, 'rounded');                        //エラーメッセージの表示
      result        = false;                                                                            //チェック結果をOFFに設定
  }
  else if (requiredValue.length < 6) {   																  //値が6桁数未満の場合
      displayToast('6文字以上入力してください', 4000, 'rounded');                            				  //エラーメッセージの表示
      result        = false;                                                                        	  //チェック結果をOFFに設定
  }
  else if (!requiredValue.match(/^(?=.*[0-9])(?=.*[a-z])[0-9a-z\-]+$/)) {								  //値が半角英数字１文字づつ使われていない場合
    displayToast('英字と数字を必ず\r\n1文字ずつ使用してください', 4000, 'rounded');                            	  //エラーメッセージの表示
    result        = false;                                                                                //チェック結果をOFFに設定
  }

  return result;                                                                                          //戻り値を返す

}

/* 最大桁数チェック */
function MaxLength( targetId, targetName, targetLength) {

  var result        = true;                                                                               //チェック結果をONに設定
  var requiredValue = $("#" + targetId).val();                                                            //指定されたIDの値を取得

  if (requiredValue.length > targetLength) {   			  											  	  //値が最大桁数より大きい場合
      displayToast(targetName + 'は' + targetLength + '文字以内で入力してください', 4000, 'rounded');          //エラーメッセージの表示
      result        = false;                                                                            //チェック結果をOFFに設定
  }

  return result;                                                                                          //戻り値を返す

}

/* 入力項目をリクエスト用JSONデータに変換する */
function InputDataToJson(targetList){

  var result 		= "";																//収集結果JSONDATA
  var itemCount 	= 0;

  result += "{"
  for ( var targetKey in targetList ) {                                   			//収集対象項目数分処理を行う

    var target 		= targetList[targetKey];                            			//収集対象項目情報の取得
    var targetValue = $("#" + target["id"]).val();                      			//指定されたIDの値を取得

    if (itemCount > 0) {
      result += ","
    }
    result += '"' + target["json"] + '":"' + targetValue + '"';						//入力項目をJSONDATAに出力
    itemCount++;

  }
  result += "}"

  return (new Function("return " + result))();
}

/* JSON文字列をJSONオブジェクトに変換する */
function StringToJson(jsonString){
  return (new Function("return " + jsonString))();
}
/* 対象日付とシステム日付の差分日数を算出する */
function GetSystemDateDiff(dateString) {
  var systemDate 	= new Date();				//本日のシステム日付
  var targetDate	= new Date(dateString);		//比較対象日付

  var msDiff = systemDate.getTime() - targetDate.getTime(); //経過時間を算出する

  var datsDiff = Math.floor(msDiff / (1000 * 60 * 60 *24));
  ++datsDiff;

  if (dateString == "1900-01-01") {
	  datsDiff = "－";
  }

  return datsDiff;
}
/* システム日付を取得する */
function GetSystemDate() {
    var systemDate 	= new Date();							//本日のシステム日付
    var year 		= systemDate.getFullYear();				//システム年
    var month 		= systemDate.getMonth() + 1;			//システム月
    var day 		= systemDate.getDate();					//システム日

    if ( month < 10 ) {
      month = '0' + month;
    }
    if ( day < 10 ) {
      day = '0' + day;
    }

    var str = year + '/' + month + '/' + day;

    return str;
}
function GetSystemDatePrev(prev) {
    var systemDate 	= new Date();							//本日のシステム日付

    systemDate.setDate(systemDate.getDate() - prev);

    var year 		= systemDate.getFullYear();				//システム年
    var month 		= systemDate.getMonth() + 1;			//システム月
    var day 		= systemDate.getDate();					//システム日

    if ( month < 10 ) {
      month = '0' + month;
    }
    if ( day < 10 ) {
      day = '0' + day;
    }

    var str = year + '/' + month + '/' + day;

    return str;
}
/* 範囲入力初期化処理 */
function RangeInit() {

    $('.numberInputRange').change(function() {

      $("#" + $(this).attr("id") + "Span").html($(this).val());

    });
}

function MakeSelectModal( modalId, modalMessage, targetList, itemKey, itemName, targetId, targetField) {
    var htmlString	= "";											//可変HTML文字列

    htmlString+= '<div id="' + modalId + '" class="modal"' + ' targetId="' + targetId + '" targetField="' + targetField + '">';
    htmlString+= '<div class="row">';
    htmlString+= '<div class="col s12">';
    htmlString+= '<div class="card-panel teal">';
    htmlString+= '<span class="modal-title white-text">' + modalMessage + 'を選択して下さい</span>';
    htmlString+= '</div>';
    htmlString+= '</div>';
    htmlString+= '</div>';
    htmlString+= '<div class="row">';
    htmlString+= '<div class="col s12">';
    htmlString+= '<div class="list-group modal-list">';

    for ( var targetKey in targetList ) {							//対象アカウント件数分処理を行う

      var targetData = targetList[targetKey];

      htmlString+= '<a href="#" class="select-modal-tap" itemKey=' + targetData[itemKey] + ' itemName=' + targetData[itemName] + ' targetId=' + targetId + ' targetField=' + targetField + ' targetModal=' + modalId + '>';
      htmlString+= '<div class="list-group-item">';
      htmlString+= '<div class="row-picture"><i class="small mdi-notification-do-not-disturb"></i></div>';
      htmlString+= '<div class="row-content"><p class="list-group-item-text">' + targetData[itemName] + '</p></div>';
      htmlString+= '</div>';
      htmlString+= '<div class="list-group-separator"></div>';
      htmlString+= '</a>';

    }

    htmlString+= '</div>';
    htmlString+= '</div>';
    htmlString+= '</div>';
    htmlString+= '</div>';

    return htmlString;
}

function MakeMultiSelectModal( modalId, modalMessage, targetList, itemKey, itemName, targetId, targetField) {
    var htmlString	= "";											//可変HTML文字列

    htmlString+= '<div id="' + modalId + '" class="modal"' + ' targetId="' + targetId + '" targetField="' + targetField + '">';
    htmlString+= '<div class="modal-title">';
    htmlString+= '<div class="card-panel teal">';
    htmlString+= '<span class="white-text">' + modalMessage + 'を選択して下さい</span><br><a class="btn-floating red right"><i class="mdi-action-done"></i></a>';
    htmlString+= '</div>';
    htmlString+= '</div>';
    htmlString+= '<div class="modal-list">';
    htmlString+= '<div class="list-group">';

    for ( var targetKey in targetList ) {							//対象アカウント件数分処理を行う

      var targetData = targetList[targetKey];

      htmlString+= '<a href="#" class="select-modal-tap" itemKey=' + targetData[itemKey] + ' itemName=' + targetData[itemName] + ' targetModal=' + modalId + '>';
      htmlString+= '<div class="list-group-item">';
      htmlString+= '<div class="row-picture"><i class="small mdi-notification-do-not-disturb"></i></div>';
      htmlString+= '<div class="row-content"><p class="list-group-item-text">' + targetData[itemName] + '<a class="btn-floating waves-effect waves-light blue darken-1 right multicheck" itemKey=' + targetData[itemKey] + ' itemSelect="0" ><i class="mdi-content-add"></i></a></p></div>';
      htmlString+= '</div>';
      htmlString+= '<div class="list-group-separator"></div>';
      htmlString+= '</a>';

    }

    htmlString+= '</div>';
    htmlString+= '</div>';
    htmlString+= '</div>';


    return htmlString;
}

function SelectHistryModalInit() {

    $('.select-modal-tap').click(function() {

      var targetId 		= $('#' + $(this).attr("targetModal")).attr("targetId");
      var targetField 	= $('#' + $(this).attr("targetModal")).attr("targetField");

      $('#' + targetId).attr(targetField, $(this).attr("itemKey"));

      var targetSpanId = '#' + targetId + 'Span';
      $(targetSpanId).html($(this).attr("itemName"));

      var targetValueId = '#' + targetId + 'Value';
      $(targetValueId).val($(this).attr("itemKey"));

      $('#' + $(this).attr("targetModal")).modal('close');
      $(targetValueId).change();

    });

    $('.select-modal').click(function(){

      $($(this).attr("href")).attr("targetId", $(this).attr("targetId"));
      $($(this).attr("href")).attr("targetField", $(this).attr("targetField"));
        $($(this).attr("href")).modal('open');																/* 電卓機能を表示 */
    });

}

function SelectModalInit() {

  var scrollValue = 0;	//スクロール量

    $('.select-modal-tap').click(function() {

      var targetId 		= $('#' + $(this).attr("targetModal")).attr("targetId");
      var targetField 	= $('#' + $(this).attr("targetModal")).attr("targetField");

      $('#' + targetId).attr(targetField, $(this).attr("itemKey"));

      var targetSpanId = '#' + targetId + 'Span';
      $(targetSpanId).html($(this).attr("itemName"));

      var targetValueId = '#' + targetId + 'Value';
      $(targetValueId).val($(this).attr("itemKey"));

      $('#' + $(this).attr("targetModal")).modal('close');
      $(targetValueId).change();

      $('html,body').animate({ scrollTop: scrollValue }, 'fast');

    });

    /* 電卓アイコンタップ時イベント */
    $('.select-modal').click(function(){

      scrollValue = $(window).scrollTop();

      $($(this).attr("href")).attr("targetId", $(this).attr("targetId"));
      $($(this).attr("href")).attr("targetField", $(this).attr("targetField"));
        $($(this).attr("href")).modal('open');																/* 電卓機能を表示 */
      });
}

function MultiSelectModalInit() {

    $('.multicheck').click(function() {

      $(this).toggleClass('blue');                                                //クラスからblueを除去して背景色を変更する(redと差し替える)
      $(this).toggleClass('red');                                                 //クラスにredを追加して背景色を変更する(blueと差し替える)
      $(this).children('i').toggleClass('mdi-content-add');                       //クラスから＋を除去してアイコンを変更する(レと差し替える)
      $(this).children('i').toggleClass('mdi-action-done');                       //クラスにレを追加してアイコンを変更する(＋と差し替える)

    });
}

/* プッシュ通知を監視する */
function PushCommitService(){

  $.ajax({
      url:"/pushService",										//作業日誌保存処理
      type:'Get',
      complete:function(data, status, jqXHR){					//処理成功時

        var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

        if (jsonResult.result == 'SUCCESS') {
          displayToast('タイムラインが更新されました。', 4000, 'rounded');    			  //保存メッセージの表示
        }

        PushCommitService();

      },
      dataType:'json',
      contentType:'text/json',
      async: true
  });
} // end of SubmitWorkDiary


/* アイコンフォントタグを作成する */
function icontag(workId, workColor, workName, spclass) {

	/* タグ文字列 */
	var htmlString	=	'<div class="work-icon">';

	switch (workId) {
	case 1 : //片付け
		htmlString += '<span class="icon-agryeel_icon_S_01">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span>';
		htmlString += '</span>';
		break;
	case 2 : //土壌消毒
		htmlString += '<span class="icon-agryeel_icon_S_09">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span>';
		htmlString += '</span>';
		break;
	case 3 : //肥料散布
		htmlString += '<span class="icon-agryeel_icon_S_02">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span><span class="path14"></span><span class="path15"></span><span class="path16"></span><span class="path17"></span><span class="path18"></span><span class="path19"></span><span class="path20"></span>';
		htmlString += '</span>';
		break;
	case 4 : //耕す
		htmlString += '<span class="icon-agryeel_icon_S_03">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span><span class="path14"></span><span class="path15"></span><span class="path16"></span><span class="path17"></span><span class="path18"></span><span class="path19"></span><span class="path20"></span>';
		htmlString += '</span>';
		break;
	case 5 : //土壌消毒
		htmlString += '<span class="icon-agryeel_icon_S_09">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span>';
		htmlString += '</span>';
		break;
	case 6 : //播種
		htmlString += '<span class="icon-agryeel_icon_S_04">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span>';
		htmlString += '</span>';
		break;
	case 7 : //除草剤散布
		htmlString += '<span class="icon-agryeel_icon_S_05">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span><span class="path14"></span><span class="path15"></span><span class="path16"></span><span class="path17"></span><span class="path18"></span><span class="path19"></span><span class="path20"></span><span class="path21"></span><span class="path22"></span>';
		htmlString += '</span>';
		break;
	case 8 : //消毒
		htmlString += '<span class="icon-agryeel_icon_S_07">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span>';
		htmlString += '</span>';
		break;
	case 9 : //潅水
		htmlString += '<span class="icon-agryeel_icon_S_06">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span>';
		htmlString += '</span>';
		break;
	case 10 : //追肥
		htmlString += '<span class="icon-agryeel_icon_S_02">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span><span class="path14"></span><span class="path15"></span><span class="path16"></span><span class="path17"></span><span class="path18"></span><span class="path19"></span><span class="path20"></span>';
		htmlString += '</span>';
		break;
	case 11 : //収穫
		htmlString += '<span class="icon-agryeel_icon_S_08">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span><span class="path13"></span><span class="path14"></span><span class="path15"></span><span class="path16"></span><span class="path17"></span><span class="path18"></span><span class="path19"></span><span class="path20"></span><span class="path21"></span><span class="path22"></span><span class="path23"></span>';
		htmlString += '</span>';
		break;
	case 12 : //定植
		htmlString += '<span class="icon-agryeel_icon_S_010">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span>';
		htmlString += '</span>';
		break;
	case 995 : //【作業記録】電卓
		htmlString += '<span class="icon-295px5" style="font-size: 0.9em; padding-top:4px;">';
		htmlString += '<span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span>';
		htmlString += '</span>';
		break;
	case 996 : //【圃場照会】元帳照会
		htmlString += '<div class="icon-295px3" style="font-size: 0.9em; padding-top:4px;">';
		htmlString += '<span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span>';
		htmlString += '</div>';
		break;
	case 997 : //【圃場照会】ブックマーク
		htmlString += '<div class="icon-295px" style="font-size: 0.9em; padding-top:4px;">';
		htmlString += '<span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span><span class="path12"></span>';
		htmlString += '</div>';
		break;
	case 998 : //【圃場照会】詳細切替
		htmlString += '<div class="icon-295px4" style="font-size: 0.9em; padding-top:4px;">';
		htmlString += '<span class="path1"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span>';
		htmlString += '</div>';
		break;
	case 999 : //ページ切替
		htmlString += '<span class="icon-295px2">';
		htmlString += '<span class="path1" style="color:#' + workColor + ';"></span><span class="path2"></span><span class="path3"></span><span class="path4"></span><span class="path5"></span><span class="path6"></span><span class="path7"></span><span class="path8"></span><span class="path9"></span><span class="path10"></span><span class="path11"></span>';
		htmlString += '</span>';
		break;
	default:
    htmlString += '<span class="none-icon ' + spclass + '" style="background-color: #' + workColor + '">' + workName.substring(0, 1) + '</span>';
		break;

	}

	htmlString	   += '</div>';

	return htmlString;

}

function ConvertNullDate(date) {

	var convert = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

	if (date != "1900-01-01") {
		convert = date;
	}

	return convert;

}

function MakePreLoader() {

	var htmlString = "";

	htmlString += '<div class="progress">';
	htmlString += '<div class="indeterminate"></div>';
	htmlString += '</div>';

	return htmlString;

}
function onProcing(flag) {

	var loading 	= $("#LoadingArea");
	var contents 	= $("#MainContents");

	if (flag == true) {
		contents.hide();
		loading.show();
	    $('.agryeel-loading').delay(600).fadeOut(500);
	    $('.agryeel-loading-action').delay(900).fadeOut(500);
	}
	else {
		loading.hide();
		contents.show();
		contents.delay(1500).fadeIn(1000);
	}

	procFlag = flag;

}
//---------------------------------------------------------------------------------------------------------------------
//- DisplayToast
//---------------------------------------------------------------------------------------------------------------------
function displayToast(pMsg, pLength, pClasses) {
	M.toast({html: pMsg, displayLength: pLength, classes: pClasses});
}