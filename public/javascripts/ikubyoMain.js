(function($){
  'use strict';

  var floatingButton;				/* 該当フローティングボタン */
  var motochoButton;				/* 該当元帳照会ボタン */
  var kukakuJoukyouList;			/* 区画状況照会一覧 */
  var grafhObject = new Object();	/* グラフオブジェクト */

  var syncerModal_top 	= 0;		/* モーダル表示ＴＯＰ位置 */
  var syncerModal_left 	= 0;		/* モーダル表示ＬＥＦＴ位置 */
  /* V1.32 元帳照会遷移後のグラフ表示不正対応 */
  var motochoFlag		= false;	/* 元帳照会表示中 */
  var timelineFlag		= false;	/* 元帳照会タイムライン表示中 */

  var motochoBaseDataList;			/* 元帳照会リスト */
  var motochoYearDataList; 			/* 元帳履歴照会リスト */
  var timeLineList;					/* タイムラインリスト */
  var motochoTimeLine;				/* タイムラインリスト（元帳照会） */
  var timelineData;					/* 選択中タイムラインデータ */
  /* 処理中フラグ */
  var procFlag	= false;
  var touched 	= false;
  var touch_time 	= 0;

  var youkiUnit	= {"0":"", "1":"枚", "2":"穴", "3":"個"};								//容器単位種別

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

      console.log("INIT STRAT");
      /* V1.32 元帳照会遷移後のグラフ表示不正対応 */
      motochoFlag = false;
      timelineFlag = false;

      /* アコーディオンオブジェクトの初期設定 */
      $('.collapsible').collapsible({
        accordion : false
      });
      /* TABの初期設定 */
      //$('.tabs').tabs();
      TabControl();

      sleep(0.1, GetWork());
      if (userinfo.contractPlan == 0 ||
          userinfo.contractPlan >= 2) {
        sleep(0.1, GetNaeSPD());
        sleep(0.1, naesearch());
      }

      $("#G1002KukakuList").hide();
      $("#systemMessagearea").hide();

      //元帳照会用タイムライン簡易切替ボタンを非表示にする
      $("#mtimeline-close").hide();
      $("#mtimeline-open").hide();

      $('#menu-top').unbind("click");
      $('#menu-top').bind("click", pageTopClick);
      $('#menu-close').unbind("click");
      $('#menu-close').bind("click", groupCloseClick);
      $('#menu-open').unbind("click");
      $('#menu-open').bind("click", groupOpenClick);

      $('#timeline-top').unbind("click");
      $('#timeline-top').bind("click", pageTopClick);
      $('#timeline-close').unbind("click");
      $('#timeline-close').bind("click", timelineCloseClick);
      $('#timeline-open').unbind("click");
      $('#timeline-open').bind("click", timelineOpenClick);
      $("#menu-sm").unbind();
      $("#menu-sm").bind("click", openSystemMessage);
      $("#timeline-sm").unbind();
      $("#timeline-sm").bind("click", openSystemMessage);

      $('#workdiv').on('inview', function(event, isInView, visiblePartX, visiblePartY) {
        if (isInView) {
          houseView();
        }
      });
      $('#housediv').on('inview', function(event, isInView, visiblePartX, visiblePartY) {
        if (isInView) {
          houseView();
        }
      });
      $('#timelinediv').on('inview', function(event, isInView, visiblePartX, visiblePartY) {
        if (isInView) {
          timelineView();
        }
      });
      displaySystemMessage();
      ikubyolinemenu();         //育苗ラインメニュー
      ikubyolineInit();         //育苗ライン初期検索

    });
  }); // end of document ready

  function TabControl() {
    $.ajax({
      url:"/getAccountInfo",
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時

        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        var menu = jsonResult.firstPage;
        setTabControl();
        if (localStorage.getItem("backMode") == "2") {
          menu = 2;
        }
        else if  (localStorage.getItem("backMode") == "3") {
          menu = 0;
        }
        else if  (localStorage.getItem("backMode") == "4") {
          menu = 1;
        }
        switch(menu) {
        case 0: //育苗状況照会
          $('.tabs').tabs('select', 'housediv');
          $("#menutimeline-icon").addClass("none");
          $("#menupage-icon").removeClass("none");
          $("#reorderButtonList").hide();
          break;
        case 1: //作業一覧
          $('.tabs').tabs('select', 'workdiv');
          $("#menutimeline-icon").addClass("none");
          $("#menupage-icon").removeClass("none");
          $("#reorderButtonList").hide();
          break;
        case 2: //タイムライン
          $('.tabs').tabs('select', 'timelinediv');
          $("#menupage-icon").addClass("none");
          $("#menutimeline-icon").removeClass("none");
          $("#reorderButtonList").hide();
          if (localStorage.getItem("backMode") == "2") {
            //lockScreen(LOCK_ID);
            $("#timelinekey-" + localStorage.getItem("backIkubyoDiaryId"))[0].scrollIntoView(true);
            localStorage.setItem("backMode"         , null);
            localStorage.setItem("backIkubyoDiaryId"  , null);
            //unlockScreen(LOCK_ID);
          }
          break;
        }
      },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function setTabControl() {
    var htmlString	= "";											//可変HTML文字列

    htmlString+= '<ul class="tabs">';
    htmlString+= '<li class="tab col s4"><a id="G1002tabwork" href="#workdiv">Work</a></li>';
    htmlString+= '<li class="tab col s4"><a id="G1002tabfield" href="#housediv">Status</a></li>';
    htmlString+= '<li class="tab col s4"><a id="G1002tabtime" href="#timelinediv">TimeLine</a></li>';
    htmlString+= '</ul>';

    $("#G1002MainTab").html(htmlString);									//可変HTML部分に反映する
    $('.tabs').tabs();
  }
  function delete_element( id_name ){
    var dom_obj = document.getElementById(id_name);
    var dom_obj_parent = dom_obj.parentNode;
    dom_obj_parent.removeChild(dom_obj);
  }
  function kukakuJokyoCompare(a, b) {
    var comp = 0;
    var afieldGroupSeq = parseInt(a.sequenceId.substring(0, 4));
    var bfieldGroupSeq = parseInt(b.sequenceId.substring(0, 4));
    var akukakuSeq = parseInt(a.sequenceId.substring(7));
    var bkukakuSeq = parseInt(b.sequenceId.substring(7));

    if (afieldGroupSeq > bfieldGroupSeq) {
      comp = 1;
    }
    else if (afieldGroupSeq < bfieldGroupSeq) {
      comp = -1;
    }
    else {
      if (akukakuSeq > bkukakuSeq) {
        comp = 1;
      }
      else if (akukakuSeq < bkukakuSeq) {
        comp = -1;
      }
    }
    return comp;
  }
  var groupinfo = {};
  function fieldGroupCompare(a, b) {
    var comp = 0;

    if (a.sequenceId > b.sequenceId) {
      comp = 1;
    }
    else if (a.sequenceId < b.sequenceId) {
      comp = -1;
    }
    return comp;
  }
  function kukakuJokyoCompareSP(a, b) {
    var comp = 0;
    var afieldGroupSeq = parseInt(a.sequenceId.substring(0, 4));
    var bfieldGroupSeq = parseInt(b.sequenceId.substring(0, 4));
    var afieldSeq = parseInt(a.sequenceId.substring(4, 7));
    var bfieldSeq = parseInt(b.sequenceId.substring(4, 7));
    var akukakuSeq = parseInt(a.sequenceId.substring(7));
    var bkukakuSeq = parseInt(b.sequenceId.substring(7));

    if (afieldGroupSeq > bfieldGroupSeq) {
      comp = -1;
    }
    else if (afieldGroupSeq < bfieldGroupSeq) {
      comp = 1;
    }
    else {
      if (afieldSeq > bfieldSeq) {
        comp = -1;
      }
      else if (afieldSeq < bfieldSeq) {
        comp = 1;
      }
      else {
        if (akukakuSeq > bkukakuSeq) {
          comp = -1;
        }
        else if (akukakuSeq < bkukakuSeq) {
          comp = 1;
        }
      }
    }
    return comp;
  }
  function GetNaeSPD() {

	grafhObject = new Object();	/* グラフオブジェクトを初期化する */

	$("#G1002Displayname").text("Nae Status");

	var url = "/getNaeSP"

	  $.ajax({
	    url: url,
	    type:'GET',
	    complete:function(data, status, jqXHR){						//処理成功時
	        var jsonResult 	= JSON.parse( data.responseText );		//戻り値用JSONデータの生成
	        var naeJoukyouList = jsonResult.targetNaeStatus;		//苗状況対象リスト
	        var htmlString	= "";									//可変HTML文字列
	        var htmlLiString= "";									//苗ナビゲーションHTML文字列
	        var iCount		= 0;									//偶奇判定件数
	        var sPattern		= "";								//パターンスタイル

	        htmlString	= "";										//可変HTML文字列初期化

	        $("#G1002HouseList").empty();
	        $("#G0002HouseList").append('<div class="progress"><div class="indeterminate"></div></div>');

	        //----- ここから苗状況照会の編集 ----
	        if (jsonResult.result == "SUCCESS") {
	          var hl = $("#G1002HouseList");
	          if (hl != undefined) {
	            hl.append('<ul id="houselist" class="collection" style="border: 0px;">');
	            var ul = $('#houselist');
	            var lists = [];
	            for ( var naeKey in naeJoukyouList ) {                     //対象苗情報分処理を行う
	              lists.push(naeJoukyouList[naeKey]);
	            }
	            //lists.sort(kukakuJokyoCompareSP);
	            for ( var naeKey in lists ) {                     //対象苗情報分処理を行う
	              var naeStatus = lists[naeKey];                  //苗状況情報の取得
	              var key = naeStatus.naeNo;

	              ul.append('<li id="item' + key + '" class="collection-item nae-item" key="' + key +'"><span class="hinsyu">' + naeStatus["hinsyuName"] + '</span><span class="kukaku">(' + naeStatus["naeName"] + ')</span><span class="kukaku">&nbsp;&nbsp;&nbsp;&nbsp;' + naeStatus["zaikoSuryo"] + youkiUnit[naeStatus["unit"]] + '(' + naeStatus["kosu"] +'個)</span></li>');
	              var li = $('#item' + key);

		          var hinsyu = "";
		          li.append('<ul id="item' + key + '-wrapper" class="item-div-wrapper"></ul>');
		          var wrapper = $("#item" + key + "-wrapper");
		          wrapper.append('<li id="item' + key + '-itemarea1" class="item-div-area"></li>');
		          var itemarea = $("#item" + key + "-itemarea1");
		          itemarea.append('<div id="item' + key + '-item1" class="item-div-item valign-wrapper"></div>');
		          var div = $("#item" + key + "-item1");
		          div.append('<img class="icon"  src="/assets/svg/field-icon-hasyu.svg">');
		          div.append('<span class="item top">' + ConvertNullDate(naeStatus["hashuDate"]) + '</span>');
		          div.append('<span class="item bottom">' + naeStatus["seiikuDayCount"] + '日</span>');
		          wrapper.append('<li id="item' + key + '-itemarea2" class="item-div-area"></li>');
		          itemarea = $("#item" + key + "-itemarea2");
		          itemarea.append('<div id="item' + key + '-item2" class="item-div-item valign-wrapper"></div>');
		          div = $("#item" + key + "-item2");
		          div.append('<img class="icon"  src="/assets/svg/field-icon-kansui.svg">');
	              div.append('<span class="item top">' + ConvertNullDate(naeStatus["totalKansuiNumber"]) + '<span class="sub">L</span></span>');
	              div.append('<span class="item bottom">' + ConvertNullDate(naeStatus["finalKansuiDate"]) + '<span class="sub">' + naeStatus["kansuiCount"] + '&nbsp;日経過</span></span>');
		          wrapper.append('<li id="item' + key + '-itemarea3" class="item-div-area"></li>');
		          itemarea = $("#item" + key + "-itemarea3");
		          itemarea.append('<div id="item' + key + '-item3" class="item-div-item valign-wrapper"></div>');
		          div = $("#item" + key + "-item3");
	              div.append('<img class="icon"  src="/assets/svg/field-icon-hiryo.svg">');
	              div.append('<span class="item top">' + ConvertNullDate(naeStatus["totalTuihiNumber"]) + '<span class="sub">Kg</span></span>');
	              div.append('<span class="item bottom">' + ConvertNullDate(naeStatus["finalTuihiDate"]) + '<span class="sub">' + naeStatus["tuihiCount"] + '&nbsp;日経過</span></span>');
		          wrapper.append('<li id="item' + key + '-itemarea4" class="item-div-area"></li>');
		          itemarea = $("#item" + key + "-itemarea4");
		          itemarea.append('<div id="item' + key + '-item4" class="item-div-item valign-wrapper"></div>');
		          div = $("#item" + key + "-item4");
	              div.append('<img class="icon"  src="/assets/svg/field-icon-syodoku.svg">');
	              div.append('<span class="item top">' + ConvertNullDate(naeStatus["totalDisinfectionNumber"]) + '<span class="sub">L</span></span>');
	              div.append('<span class="item bottom">' + ConvertNullDate(naeStatus["finalDisinfectionDate"]) + '<span class="sub">' + naeStatus["disinfectionCount"] + '&nbsp;日経過</span></span>');
	              //if (houseStatus["pestId"] != 0 &&
	              //    (userinfo.contractPlan == 0 ||
	              //     userinfo.contractPlan >= 4)) {
	              //  li.append('<i class="material-icons adb ' + houseStatus["pestOn"] + '" pestname="' + houseStatus["pestName"] + '" pestdate="' + houseStatus["pestPredictDate"] + '">adb</i>');
	              //}
	              li.append('<a class="waves-effect waves-light btn-flat haiki-make" naeName="' + naeStatus["naeName"] + '" naeNo="' + naeStatus["naeNo"] + '" item="item' + key + '">廃</a>');
	            }
	            $("#G1002HouseList .adb").unbind("click");
	            $("#G1002HouseList .adb").bind("click", pestAlert);
	            $("#G1002HouseList .haiki-make").unbind("click");
	            $("#G1002HouseList .haiki-make").bind("click", haikiMove);
	            $("#G1002HouseList .nae-item").unbind("click");
	            $("#G1002HouseList .nae-item").bind("click", naeDisplay);
	            if (localStorage.getItem("backMode") == "3") {
	              //lockScreen(LOCK_ID);
	              $("#item" + localStorage.getItem("backFieldId")).click();
	            }
	          }
	        }
	        else if (jsonResult.result == "REDIRECT") {
	          window.location.href = '/move';
	          return;
	        }
	        else {
	          displayToast("該当データが存在しませんでした。", 4000, "");
	        }
	        //group.attr("data", "on");
	    },
	    dataType:'json',
	    contentType:'text/json'
	  });
  }
  function pestAlert() {
    var pest = $(this);
    window.alert('[' + pest.attr("pestname") + ']\n' + pest.attr("pestdate") + 'に発生するかもしれません。');
    return false;
  }
  function haikiMove() {
    var info = $(this);
    localStorage.setItem("backMode"         , "4");
    localStorage.setItem("backWorkId"       , info.attr("workid"));
    localStorage.setItem("backKukakuId"     , 0);
    localStorage.setItem("backFieldGroupId" , 0);
    localStorage.setItem("backFieldId"      , 0);
    window.location.href = '/208/' + info.attr("naeNo") + '/ikubyoDiaryMove';
  }

  var oKukakuInfo = {kukakuId: 0, cropId: 0, cropName:"", workchainId: 0, workchainName:""};

  var areacontrol = {nouyaku: false, hiryou: false, total: false, timeline: false};
  function naeDisplayEdit(jsonResult) {
    var ka = $("#kukakuarea");
    ka.empty();

    ka.append('<div id="naedetail" class="nae-detail"></div>');

    var kd = $("#naedetail");
    var kdj = jsonResult.naeJson;

    kd.append('<div class="sub-menu"><i class="material-icons small left" id="naedetailback">arrow_back</i><span class="title">苗情報</span></div>');
    //------------------------------------------------------------------------------------------------------------------
    //- 苗状況の表示
    //------------------------------------------------------------------------------------------------------------------
    kd.append('<div class="card-panel detail-panel-color" id="naedetail-1"></div>');
    var k1 = $("#naedetail-1");

    //苗情報
    k1.append('<span class="item-title">苗名</span><span class="item">' + kdj["hinsyuName"] + ' (' + kdj["naeName"] + ')</span>');
    //
    var chain = '<div class="chain">';
    var chaindata = kdj["chain"];
    for (var ckey in chaindata) {
      var data = chaindata[ckey];
      if (data.end == 1) {
        chain += '<span class="chainitem" style="background-color: #' + data.color +';">' + data.name + '</span>';
      }
      else {
        chain += '<span class="chainitem">' + data.name + '</span>';
      }
    }
    chain += '</div>';
    k1.append(chain);

    var hinsyu = "";
    if (kdj["hinsyuName"] != "") {
      hinsyu = " ( " + kdj["hinsyuName"] + " )"
    }
    k1.append('</br><span class="item-title">品目</span><span class="item">' + kdj["cropName"] + hinsyu + '</span>');
    k1.append('</br><span class="item-title">播種日</span><span class="item">' + kdj["hashuDate"] + '</span>');
    k1.append('</br><span class="item-title">生育日数</span><span class="item">' + kdj["seiikuDayCount"] + '日</span>');
    k1.append('</br><span class="item-title">現在庫数</span><span class="item">' + kdj["zaikoSuryo"] + youkiUnit[kdj["unit"]] + '(' + kdj["kosu"] + '個)</span>');
    //---------------------------------------------------------------------------------------------------------------------------------
    //農薬情報アコーディンの挿入
    //---------------------------------------------------------------------------------------------------------------------------------
    kd.append('<div id="totalnouyakubar" class="valign-wrapper">農薬情報<i class="material-icons small right-align" id="">expand_more</i></div>');
    kd.append('<div id="totalnouyakarea"></div>');
    var tna = $("#totalnouyakarea");
    //---------------------------------------------------------------------------------------------------------------------------------
    //農薬情報
    //---------------------------------------------------------------------------------------------------------------------------------
    tna.append('<div class="card-panel detail-panel-color" id="totalnouyakarea-1"></div>');
    var tn1 = $("#totalnouyakarea-1");
    tn1.append('<ul id="nouyakulist" class="nouhilist"></ul>');
    var nl = $("#nouyakulist");
    var nouyakulist = jsonResult.nouyakuList;
    var oldno = "";
    for (var key in nouyakulist) {
      var data = nouyakulist[key];
      if (oldno != data.no) {
        nl.append('<li id="' + key + '" style="border-left: solid 4px #' + data.color + ';"><span class="no nouyaku">' + data.no + '</span><span class="name">' + data.nouhiName + '</li>');
        var nli  = $("#" + key);
        nli.append('<ul id="u-' + key + '"></ul>')
        var nliu  = $("#u-" + key);
      }
      nliu.append('<li class="nouhiitem"><span class="date">' + data.date + '</span><span class="diffdate">' + data.diffdate + '日経過</span><span class="method">' + data.method + '<span><br><span class="bairitu">' + data.bairitu + '</span><span class="ryo">' + data.ryo + '</span></li>');
      oldno = data.no;
    }
    //---------------------------------------------------------------------------------------------------------------------------------
    //農薬情報アコーディンの制御
    //---------------------------------------------------------------------------------------------------------------------------------
    tna.hide();
    areacontrol.nouyaku = false;
    $("#totalnouyakubar").unbind("click");
    $("#totalnouyakubar").bind("click", totalnouyakubarClick);
    //---------------------------------------------------------------------------------------------------------------------------------
    //肥料情報アコーディンの挿入
    //---------------------------------------------------------------------------------------------------------------------------------
    kd.append('<div id="totalhiryoubar" class="valign-wrapper">肥料情報<i class="material-icons small right-align" id="">expand_more</i></div>');
    kd.append('<div id="totalhiryouarea"></div>');
    var tha = $("#totalhiryouarea");
    //---------------------------------------------------------------------------------------------------------------------------------
    //肥料情報
    //---------------------------------------------------------------------------------------------------------------------------------
    tha.append('<div class="card-panel detail-panel-color" id="totalhiryouarea-1"></div>');
    var tn1 = $("#totalhiryouarea-1");
    tn1.append('<ul id="hiryoulist" class="nouhilist"></ul>');
    var nl = $("#hiryoulist");
    var hiryoulist = jsonResult.hiryoList;
    var oldno = "";
    for (var key in hiryoulist) {
      var data = hiryoulist[key];
      if (oldno != data.no) {
        nl.append('<li id="' + key + '" style="border-left: solid 4px #' + data.color + ';"><span class="no hiryou">' + data.no + '</span><span class="name">' + data.nouhiName + '</li>');
        var nli  = $("#" + key);
        nli.append('<ul id="u-' + key + '"></ul>')
        var nliu  = $("#u-" + key);
      }
      var diff = data.diffdate;
      if (diff != "") {
        diff += "日経過";
      }
      nliu.append('<li class="nouhiitem"><span class="date">' + data.date + '</span><span class="diffdate">' + diff + '</span><span class="method">' + data.method + '<span><br><span class="bairitu">' + data.bairitu + '</span><span class="ryo">' + data.ryo + '</span><br><span class="n">' + data.n + '</span><span class="p">' + data.p + '</span><br><span class="k">' + data.k + '</span><span class="mg">' + data.mg + '</span></li>');
      oldno = data.no;
    }
    //---------------------------------------------------------------------------------------------------------------------------------
    //肥料情報アコーディンの制御
    //---------------------------------------------------------------------------------------------------------------------------------
    tha.hide();
    areacontrol.hiryou = false;
    $("#totalhiryoubar").unbind("click");
    $("#totalhiryoubar").bind("click", totalhiryoubarClick);
    //---------------------------------------------------------------------------------------------------------------------------------
    //集計結果アコーディンの挿入
    //---------------------------------------------------------------------------------------------------------------------------------
    kd.append('<div id="totalgrafhbar" class="valign-wrapper">各種集計結果<i class="material-icons small right-align" id="">expand_more</i></div>');
    kd.append('<div id="totalgrafharea"></div>');
    var tga = $("#totalgrafharea");
    //---------------------------------------------------------------------------------------------------------------------------------
    //消毒量
    //---------------------------------------------------------------------------------------------------------------------------------
    tga.append('</br><span class="item-title">消毒</span><span class="item">' + kdj["totalDisinfectionNumber"] + '&nbsp;L</span>');
    if (kdj["disinfectionCount"] != 0) {
      tga.append('<span class="item">' + kdj["finalDisinfectionDate"] + '</span><span class="sub">' + kdj["disinfectionCount"] + '日経過</span>');
    }
    //
    makeGraph(tga, 'tDisinfection', 'mix', kdj["disinfectionLabel"], '消毒量', kdj["disinfectionData"], 'rgba(56, 142, 60 , 0.2)', 'rgba(56, 142, 60 , 0.4)', '合計消毒量', kdj["disinfectionMixData"], 'rgba(56, 142, 60 , 0)', 'rgba(56, 142, 60 , 1)');
    //---------------------------------------------------------------------------------------------------------------------------------
    //潅水量
    //---------------------------------------------------------------------------------------------------------------------------------
    tga.append('</br><span class="item-title">潅水</span><span class="item">' + kdj["totalKansuiNumber"] + '&nbsp;L</span>');
    if (kdj["kansuiCount"] != 0) {
      tga.append('<span class="item">' + kdj["finalKansuiDate"] + '</span><span class="sub">' + kdj["kansuiCount"] + '日経過</span>');
    }
    //
    makeGraph(tga, 'tKansui', 'mix', kdj["kansuiLabel"], '潅水量', kdj["kansuiData"], 'rgba(2, 136, 209 , 0.2)', 'rgba(2, 136, 209 , 0.4)', '合計潅水量', kdj["kansuiMixData"], 'rgba(2, 136, 209 , 0)', 'rgba(2, 136, 209 , 1)');
    //---------------------------------------------------------------------------------------------------------------------------------
    //追肥
    //---------------------------------------------------------------------------------------------------------------------------------
    tga.append('</br><span class="item-title">追肥</span><span class="item">' + kdj["totalTuihiNumber"] + '&nbsp;Kg</span>');
    if (kdj["kansuiCount"] != 0) {
      tga.append('<span class="item">' + kdj["finalTuihiDate"] + '</span><span class="sub">' + kdj["tuihiCount"] + '日経過</span>');
    }
    makeGraph(tga, 'tTuihi', 'mix', kdj["tuihiLabel"], '追肥量', kdj["tuihiData"], 'rgba(251, 192, 45 , 0.2)', 'rgba(251, 192, 45 , 0.4)', '合計', kdj["tuihiMixData"], 'rgba(251, 192, 45 , 0)', 'rgba(251, 192, 45 , 1)');
    //---------------------------------------------------------------------------------------------------------------------------------
    //集計結果アコーディンの制御
    //---------------------------------------------------------------------------------------------------------------------------------
    tga.hide();
    areacontrol.total = false;
    $("#totalgrafhbar").unbind("click");
    $("#totalgrafhbar").bind("click", totalgrafhbarClick);
    //---------------------------------------------------------------------------------------------------------------------------------
    //タイムラインアコーディンの挿入
    //---------------------------------------------------------------------------------------------------------------------------------
    kd.append('<div id="timelinebar" class="valign-wrapper">タイムライン<i class="material-icons small right-align" id="">expand_more</i></div>');
    kd.append('<div id="timelinearea"></div>');
    var tla = $("#timelinearea");
    var timelineString = ikubyolineDisplay(jsonResult)
    tla.append(timelineString);
    $("#timelinearea .timelinelist").removeClass("kani");
    $(".timelinelist").addClass("motocho");
    $(".timelinelist").unbind("click");
    $(".timelinelist").bind("click", ikubyoDiaryEdit);
    //---------------------------------------------------------------------------------------------------------------------------------
    //タイムラインアコーディンの制御
    //---------------------------------------------------------------------------------------------------------------------------------
    tla.hide();
    areacontrol.timeline = false;
    $("#timelinebar").unbind("click");
    $("#timelinebar").bind("click", timelinebarClick);

    $("#naedetailback").unbind("click");
    $("#naedetailback").bind("click", naedetailBack);
    var ms = $("#mainsection");
    var ka = $("#kukakuarea");
    if (ms != undefined) {
      ms.fadeOut(0);
    }
    if (ka != undefined) {
      ka.fadeIn(500);
    }
    //元帳照会用タイムライン簡易切替ボタンを表示する
    timelineCloseClick();
    $("#mtimeline-close").unbind("click");
    $("#mtimeline-close").bind("click", timelineCloseClick);
    $("#mtimeline-open").unbind("click");
    $("#mtimeline-open").bind("click", timelineOpenClick);
    $("#menu-close").hide();
    $("#menu-open").hide();
    $("#mtimeline-close").show();
    $("#mtimeline-open").show();

    //---------------------------------------------------------------------------------------------------------------------------------
    //作業記録一覧の生成
    //---------------------------------------------------------------------------------------------------------------------------------
    selectDataGet("#workReorder", "/getWorkOfIkubyo");
    $('#workReorderValue').unbind('change');
    $('#workReorderValue').bind('change', workChange);
    $('.modal').modal();
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);
    $("#reorderButtonList").show();
    //---------------------------------------------------------------------------------------------------------------------------------
    //バックモードの初期化
    //---------------------------------------------------------------------------------------------------------------------------------
    if (localStorage.getItem("backMode") == "3") {
      //unlockScreen(LOCK_ID);
      if (localStorage.getItem("backIkubyoDiaryId") != "null") {
        $("#timelinebar").click();
      }

      localStorage.setItem("backMode"         , null);
      localStorage.setItem("backFieldId"      , null);
      localStorage.setItem("backKukakuId"     , null);
      localStorage.setItem("backWorkId"       , null);
      localStorage.setItem("backIkubyoDiaryId"  , null);
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 作業切替を実行する
  //------------------------------------------------------------------------------------------------------------------
  function workChange() {
    var work = $('#workReorderValue').val();
    if (work != 0) {
      var kukaku = oKukakuInfo.kukakuId;
      if (kukaku != "") {
        localStorage.setItem("backMode"         , "3");
        localStorage.setItem("backWorkId"       , work);
        localStorage.setItem("backIkubyoDiaryId"  , null);
        window.location.href = '/' + work + '/NONE/ikubyoDiaryMove';
        return false;
      }
    }
  }
  function naeDisplay() {
    var nae = $(this);
    var inputJson = {"nae": 0};

    inputJson.nae = nae.attr("key");

    $.ajax({
      url:"/getNaeDetail",
      type:'POST',
      data:JSON.stringify(inputJson),               //入力用JSONデータ
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        naeDisplayEdit(jsonResult);
    },
      dataType:'json',
      contentType:'text/json'
    });

  }
  function totalnouyakubarClick() {
    var tna = $("#totalnouyakarea");
    var i = $("#totalnouyakubar i");
    if (tna != undefined) {
      if (areacontrol.nouyaku) {
        tna.hide();
        i.text("expand_more")
        areacontrol.nouyaku = false;
      }
      else {
        tna.show();
        i.text("expand_less")
        areacontrol.nouyaku = true;
      }
    }
  }
  function totalhiryoubarClick() {
    var tha = $("#totalhiryouarea");
    var i = $("#totalhiryoubar i");
    if (tha != undefined) {
      if (areacontrol.hiryou) {
        tha.hide();
        i.text("expand_more")
        areacontrol.hiryou = false;
      }
      else {
        tha.show();
        i.text("expand_less")
        areacontrol.hiryou = true;
      }
    }
  }
  function totalgrafhbarClick() {
    var tga = $("#totalgrafharea");
    var i = $("#totalgrafhbar i");
    if (tga != undefined) {
      if (areacontrol.total) {
        tga.hide();
        i.text("expand_more")
        areacontrol.total = false;
      }
      else {
        tga.show();
        i.text("expand_less")
        areacontrol.total = true;
      }
    }
  }
  function timelinebarClick() {
    var tla = $("#timelinearea");
    var i = $("#timelinebar i");
    if (tla != undefined) {
      if (areacontrol.timeline) {
        tla.hide();
        i.text("expand_more")
        areacontrol.timeline = false;
      }
      else {
        tla.show();
        i.text("expand_less")
        areacontrol.timeline = true;
      }
    }
  }
  function makeGraph(pDiv, pId, pType, pLabelJson, pLabel, pDataJson, pBackStyle, pBorderStyle, pMixLabel, pMixDataJson, pMixBackStyle, pMixBorderStyle) {

    pDiv.append('<canvas id="' + pId + '" width="80%" height="60%"></canvas>');

    var labelJson   = [];
    var dataJson    = [];
    var mixdataJson = [];
    var max         = -1;
    var min         = 0x7FFFFFFF;
    var mmax        = -1;
    var mmin        = 0x7FFFFFFF;

    //ラベル
    labelJson.push('');
    for (var key in pLabelJson) {
      var data = pLabelJson[key];
      labelJson.push(data);
    }
    //データ
    dataJson.push(0);
    for (var key in pDataJson) {
      var data = pDataJson[key];
      dataJson.push(data);
      if (max < data) {
        max = data;
      }
      if (data < min) {
        min = data;
      }
    }
    //積算データ
    mixdataJson.push(0);
    for (var key in pMixDataJson) {
      var data = pMixDataJson[key];
      mixdataJson.push(data);
      if (mmax < data) {
        mmax = data;
      }
      if (data < mmin) {
        mmin = data;
      }
    }

    if (pType == 'mix') {


      var backStyle       = [];
      var borderStyle     = [];

      backStyle.push(pBackStyle);
      borderStyle.push(pBorderStyle);
      for (var key in pLabelJson) {
        backStyle.push(pBackStyle);
        borderStyle.push(pBorderStyle);
      }

      var ctx = document.getElementById(pId).getContext('2d');
      var myChart = new Chart(ctx, {
        type: 'bar',
        options: {
        scales: {
          yAxes: [{
              id: "ya1",
              type: "linear",
              position: "left",
              ticks: {
                suggestedMax: max,
                suggestedMin: min,
                stepSize: ((max - min) / 5)
              },
            },
            {
              id: "ya2",
              type: "linear",
              position: "right",
              ticks: {
                suggestedMax: mmax,
                suggestedMin: mmin,
                stepSize: ((mmax - mmin) / 5)
              },
            }],
          }
          },
          data: {
            labels: labelJson,
            datasets: [{
              label: pLabel,
              data: dataJson,
              backgroundColor: backStyle,
              borderColor: borderStyle,
              borderWidth: 1,
              pointBorderColor: [
                  'rgba(128, 128, 128, 0.8)'
              ],
              yAxisID: "ya1",
            }
            ,{
              label: pMixLabel,
              data: mixdataJson,
              backgroundColor: [pMixBackStyle],
              borderColor: [pMixBorderStyle],
              borderWidth: 1,
              pointBorderColor: [
                  'rgba(128, 128, 128, 0.8)'
              ],
              yAxisID: "ya2"
              ,type :'line'
            }]
        }
      });
    }
    else {
      var backStyle       = [];
      var borderStyle     = [];

      for (var key in pLabelJson) {
        backStyle.push(pBackStyle);
        borderStyle.push(pBorderStyle);
      }

      var ctx = document.getElementById(pId).getContext('2d');
      var myChart = new Chart(ctx, {
        type: pType,
        data: {
            labels: labelJson,
            datasets: [{
              label: pLabel,
              data: dataJson,
              backgroundColor: backStyle,
              borderColor: borderStyle,
              borderWidth: 1,
              pointBorderColor: [
                  'rgba(128, 128, 128, 0.8)'
              ]
            }]
        }
      });
    }
  }

  function naedetailBack() {
    var ms = $("#mainsection");
    var ka = $("#kukakuarea");
    //元帳照会用タイムライン簡易切替ボタンを表示する
    $("#mtimeline-close").hide();
    $("#mtimeline-open").hide();
    $("#menu-close").show();
    $("#menu-open").show();
    if (ka != undefined) {
      ka.fadeOut(0);
    }
    if (ms != undefined) {
      ms.fadeIn(500);
    }
  }

  //------------------
  // 作業情報
  //------------------
  var workinfo = {open: false, data:{}};
  function GetWork() {

      $.ajax({
        url:"/getIkubyoWork",										//作業情報取得処理
        type:'GET',
        complete:function(data, status, jqXHR){						//処理成功時
            var jsonResult 	= JSON.parse( data.responseText );		//戻り値用JSONデータの生成
            var workList 		= jsonResult.targetIkubyoWork;		//作業対象リスト
            if (jsonResult.result == "REDIRECT") {
              window.location.href = '/move';
              return;
            }

            workinfo.data = workList;
            var htmlString = "";

            //----- ここから作業タブの編集 ----
            htmlString  = "";                   //可変HTML文字列
            for ( var workKey in workList ) {                       //作業対象件数分処理を行う

              var work 			= workList[workKey];                //作業情報の取得
              var workModeColor	= "";

              //作業項目一覧を作成する
              htmlString += '<div>';

              if (work["workMode"] == 0) { 				//現在作業モードが｢作付｣の場合
                  workModeColor	= "sakuduke";
              }
              else if (work["workMode"] == 1) { 		//現在作業モードが｢管理｣の場合
                  workModeColor	= "kanri";
              }
              else { 									//現在作業モードが｢収穫｣の場合
                  workModeColor	= "syukaku";
              }

              var code = work["workColor"];
              var red   = parseInt(code.substring(0,2), 16);
              var green = parseInt(code.substring(2,4), 16);
              var blue  = parseInt(code.substring(4,6), 16);
              var sKey  = ("" + work["workId"]).split(".");

              htmlString += '<div class="work-card" id="work-' + sKey[0] + '" style="border-left: 8px solid #' + work["workColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_WORK + ');" workid=' + work["workId"] + ' workTemplateId="' + work["workTemplateId"] + '">';
              htmlString += '<div class="work-title" worklist="worklist' + work["workId"] + '">';
              htmlString += icontag(work["workId"], work["workColor"], work["workName"], "");
              htmlString += '<span class="work-text">' + work["workName"] + '</span>';
              htmlString += '<span class="work-sub-text">' + work["workEnglish"] + '</span>';
              htmlString += '</div>';
              htmlString += '</div>';

              htmlString += '</div>';

            } // workList

            //作業記録日誌画面に遷移する為のフォームを埋め込む
            //htmlString += '<form id="G1002Form" action="./workDiaryMove" method="GET" ><input type="hidden" id="G1002WorkId" name="workId"><input type="hidden" id="G1002kukakuId" name="kukakuId"></form>';

            $("#G1002WorkList").html(htmlString);					//可変HTML部分に反映する

            $('.work-card').unbind("click");
            $('.work-card').bind("click", WorkingMove);

            //if (localStorage.getItem("backMode") == "4") {
            //  if (!(localStorage.getItem("backWorkId") == undefined || localStorage.getItem("backWorkId") == "null")) {
            //    var sKey = ("" + localStorage.getItem("backWorkId")).split(".");
            //    $("#work-" + sKey[0]).click();
            //  }
            //}

        },
        dataType:'json',
        contentType:'text/json'
      });
  }

  //--------------------------------------------------
  // 作業対象区画表示
  //--------------------------------------------------
  var wgroupinfo = {};
  var wfieldinfo = {};
  function workKukakuCompare(a, b) {
    var result = 0;
    var afieldGroupSeq = parseInt(a.sequenceId.substring(0, 4));
    var bfieldGroupSeq = parseInt(b.sequenceId.substring(0, 4));
    var akukakuSeq = parseInt(a.sequenceId.substring(7));
    var bkukakuSeq = parseInt(b.sequenceId.substring(7));
//    if(a.fieldGroupId < b.fieldGroupId) {result = -1;}
//    else if(a.fieldGroupId > b.fieldGroupId) {result = 1;}
//    else {
//      if(a.sequenceId < b.sequenceId) {result = -1;}
//      else if(a.sequenceId > b.sequenceId) {result = 1;}
//    }
    if(afieldGroupSeq < bfieldGroupSeq) {result = -1;}
    else if(afieldGroupSeq > bfieldGroupSeq) {result = 1;}
    else {
      if(akukakuSeq < bkukakuSeq) {result = -1;}
      else if(akukakuSeq > bkukakuSeq) {result = 1;}
    }
    return result;
  }

  function WorkingMove() {
    //----- 作業中チェック -----
    var inputJson = {"workid":"", "kukakuid":"", "action":""};
    var info = $(this);
    if (userinfo.work != 0) {
      var result = window.confirm('あなたは現在作業中です。\n作業中画面を表示しますか？');
      if (result) {
        if (userinfo.field != 0) {
          window.location.href = "/workingmove";
        }
        else {
          window.location.href = "/workingikubyomove";
        }
        return;
      }
      else {
        var result2 = window.confirm('作業記録画面に移動しますか？');
        if (result2) {
          localStorage.setItem("backMode"         , "4");
          localStorage.setItem("backWorkId"       , info.attr("workid"));
          localStorage.setItem("backKukakuId"     , 0);
          localStorage.setItem("backFieldGroupId" , 0);
          localStorage.setItem("backFieldId"      , 0);
          window.location.href = '/' + info.attr("workid") + '/NONE/ikubyoDiaryMove';
          return;
        }
        else {
          return;
        }
      }
    }
    else {
      localStorage.setItem("backMode"         , "4");
      localStorage.setItem("backWorkId"       , info.attr("workid"));
      localStorage.setItem("backKukakuId"     , 0);
      localStorage.setItem("backFieldGroupId" , 0);
      localStorage.setItem("backFieldId"      , 0);
      window.location.href = '/' + info.attr("workid") + '/NONE/ikubyoDiaryMove';
      return;
    }
  }

  //---------------------------------------------------------------------------
  //- システムメッセージ表示
  //---------------------------------------------------------------------------
  var systemColor = [
       "f44336"
      ,"9c27b0"
      ,"3f51b5"
      ,"03a9f4"
      ,"009688"
      ,"9e9e9e"
  ];
  function openSystemMessage() {
    $("#mainsection").hide();
    $("#systemMessagearea").show();
  }
  function closeSystemMessage() {
    $("#systemMessagearea").hide();
    $("#mainsection").show();
  }
  function displaySystemMessage() {

  $.ajax({
      url:"/getSystemMessage",
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );
        if (jsonResult.browse == 1) { //システムメッセージ表示有りの場合のみ
          //----- メッセージ一覧表を作成する -----
          $("#systemMessagearea").empty();

          var htmlString  = "";                   //可変HTML文字列
          var msgs = jsonResult.datalist;

          htmlString += '<div class="">';
          htmlString += '<h6 class=""><i class="material-icons right" id="systemmessageback">close</i>NotificationMessage</h6>';
          htmlString += '<div class="row">';
          htmlString += '<div class="col s12 input-field">';
          htmlString += '<span class="mselectmodal-trigger-title">表示条件</span><a id="#G1002modalMessageKind" href="#selectmodal"  class="selectmodal-trigger" title="表示条件一覧" data="getMessageKind" displayspan="#G1002MessageKind" htext="#G1002MessageKindHidden"><span id="G1002MessageKind" class="blockquote-input">' + jsonResult.mkindn + '</span></a>';
          htmlString += '<input type="hidden" id="G1002MessageKindHidden" value="' + jsonResult.mkind + '">';
          htmlString += '</div>';
          htmlString += '<div class="row">';
          htmlString += '<div class="col s12 center">';
          htmlString += '<a id="systemmessage-alldeletebtn" class="waves-effect waves-light btn-small red darken-2"><i class="material-icons left">delete</i>一括削除(ALL DELETE)</a>';
          htmlString += '</div>';
          htmlString += '</div>';
          htmlString += '<ul class="system-message">';
          for ( var key in msgs ) {             //タイムライン件数分処理を行う

            var msg    = msgs[key];    //タイムライン情報の取得
            var code  = systemColor[msg["diff"]];
            var red   = parseInt(code.substring(0,2), 16);
            var green = parseInt(code.substring(2,4), 16);
            var blue  = parseInt(code.substring(4,6), 16);

            htmlString += '<li id="msg-' + msg["key"] + '" class="messagelist waves-effect waves-light" key="' + msg["key"] + '" style="border-left: 8px solid #' + systemColor[msg["diff"]] + '; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_MESSAGE + '); border-bottom: 0px;">';
            htmlString += '<div class="date-title">';
            htmlString += '<span>' + msg["date"] + '</span>';
            if (msg["new"] == 1) {
              htmlString += '<span class="new">new</span>';
            }
            htmlString += '</div>';
            htmlString += '<div class="message">';
            htmlString += '<span>' + msg["message"] + '</span>';
            htmlString += '</div>';
            htmlString += '</li>';

          } // timeLineList
          htmlString += '</ul>';
          htmlString += '</div>';
          $("#systemMessagearea").html(htmlString);

          $(".messagelist").unbind("click");
          $(".messagelist").bind("click", messageComplate);
          $("#G1002MessageKindHidden").unbind("change");
          $("#G1002MessageKindHidden").bind("change", changeMessageKind);
          $("#systemmessageback").unbind("click");
          $("#systemmessageback").bind("click", closeSystemMessage);
          $("#systemmessage-alldeletebtn").unbind("click");
          $("#systemmessage-alldeletebtn").bind("click", messageAllComplate);


          $("#menuBtn").addClass("pulse");
          $("#menu-sm").addClass("pulse");
          $("#timelineBtn").addClass("pulse");
          $("#timeline-sm").addClass("pulse");
        }
        else {
          $("#systemMessagearea").empty();

          var htmlString  = "";                   //可変HTML文字列
          var msgs = jsonResult.datalist;

          htmlString += '<div class="">';
          htmlString += '<h6 class=""><i class="material-icons right" id="systemmessageback">close</i>NotificationMessage</h6>';
          htmlString += '<div class="row">';
          htmlString += '<div class="col s12 input-field">';
          htmlString += '<span class="mselectmodal-trigger-title">表示条件</span><a id="#G1002modalMessageKind" href="#selectmodal"  class="selectmodal-trigger" title="表示条件一覧" data="getMessageKind" displayspan="#G1002MessageKind" htext="#G1002MessageKindHidden"><span id="G1002MessageKind" class="blockquote-input">' + jsonResult.mkindn + '</span></a>';
          htmlString += '<input type="hidden" id="G1002MessageKindHidden" value="' + jsonResult.mkind + '">';
          htmlString += '</div>';
          htmlString += '</div>';
          htmlString += '<div class="row">';
          htmlString += '<div class="col s12 center">';
          htmlString += '<a id="systemmessage-alldeletebtn" class="waves-effect waves-light btn-small red darken-2 disabled"><i class="material-icons left">delete</i>一括削除(ALL DELETE)</a>';
          htmlString += '</div>';
          htmlString += '</div>';
          $("#systemMessagearea").html(htmlString);

          $("#G1002MessageKindHidden").unbind("change");
          $("#G1002MessageKindHidden").bind("change", changeMessageKind);
          $("#systemmessageback").unbind("click");
          $("#systemmessageback").bind("click", closeSystemMessage);
          $("#systemmessage-alldeletebtn").unbind("click");

          $("#menuBtn").removeClass("pulse");
          $("#menu-sm").removeClass("pulse");
          $("#timelineBtn").removeClass("pulse");
          $("#timeline-sm").removeClass("pulse");
        }
        //------------------------------------------------------------------------------------------------------------------
        //- セレクトモーダルの初期化
        //------------------------------------------------------------------------------------------------------------------
        $('.selectmodal-trigger').unbind('click');
        $('.selectmodal-trigger').bind('click', selectOpen);
      },
      dataType:'json',
      contentType:'text/json'
    });
  }
  //---------------------------------------------------------------------------
  //- システムメッセージ既読処理
  //---------------------------------------------------------------------------
  function messageComplate() {
    var result = window.confirm('該当メッセージを削除しますか？');
    if (result) {
      var key = $(this).attr("key");
      var url = "/" + $(this).attr("key") + "/commitSystemMessage";
      $.ajax({
        url:url,
        type:'GET',
        complete:function(data, status, jqXHR){           //処理成功時
          var jsonResult    = JSON.parse( data.responseText );
          $("#msg-" + key).empty();
          $("#msg-" + key).addClass("remove");
          sleep(5, function () {

            $("#msg-" + key).remove();

          });
          if (jsonResult.browse == 1) { //システムメッセージ表示有りの場合のみ
            $("#menuBtn").addClass("pulse");
            $("#menu-sm").addClass("pulse");
            $("#timelineBtn").addClass("pulse");
            $("#timeline-sm").addClass("pulse");
          }
          else {
            $("#menuBtn").removeClass("pulse");
            $("#menu-sm").removeClass("pulse");
            $("#timelineBtn").removeClass("pulse");
            $("#timeline-sm").removeClass("pulse");
          }
        },
        dataType:'json',
        contentType:'text/json'
      });
    }

  }
  //---------------------------------------------------------------------------
  //- システムメッセージ一括削除
  //---------------------------------------------------------------------------
  function messageAllComplate() {
    var result = window.confirm('現在表示している全てのメッセージを削除しますか？');
    if (result) {
      var key = $(this).attr("key");
      var url = "/commitAllSystemMessage";
      $.ajax({
        url:url,
        type:'GET',
        complete:function(data, status, jqXHR){           //処理成功時
          var jsonResult    = JSON.parse( data.responseText );
          $(".messagelist").empty();
          $(".messagelist").addClass("remove");
          sleep(5, function () {

            $(".messagelist").remove();

          });
          if (jsonResult.browse == 1) { //システムメッセージ表示有りの場合のみ
            $("#menuBtn").addClass("pulse");
            $("#menu-sm").addClass("pulse");
            $("#timelineBtn").addClass("pulse");
            $("#timeline-sm").addClass("pulse");
          }
          else {
            $("#menuBtn").removeClass("pulse");
            $("#menu-sm").removeClass("pulse");
            $("#timelineBtn").removeClass("pulse");
            $("#timeline-sm").removeClass("pulse");
          }
        },
        dataType:'json',
        contentType:'text/json'
      });
    }

  }
  //---------------------------------------------------------------------------
  //- メッセージ種別変更時イベント
  //---------------------------------------------------------------------------
  function changeMessageKind() {
    var url = "/" + $("#G1002MessageKindHidden").val() + "/changeMessageKind";
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );
        displaySystemMessage();
      },
      dataType:'json',
      contentType:'text/json'
    });
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 苗検索条件生成時
  //------------------------------------------------------------------------------------------------------------------
  function naesearch() {
    var ks = $('#naesearch');
    if (ks != undefined) {
      var htmlS = "";
      ks.empty();

      ks.append('<h6 class="">苗検索条件</h6>');     //コンテンツヘッダーを生成する
      ks.append('<div class="row">');
      ks.append('<div class="col s12 input-field">');
      ks.append('<span class="mselectmodal-trigger-title">対象生産物</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="対象生産物一覧" data="'+ userinfo.farm + '/getCrop" displayspan="#G1002SsnCrop"><span id="G1002SsnCrop" class="blockquote-input">未選択</span></a>');
      ks.append('</div>');
      ks.append('</div>');
      ks.append('<div class="row">');
      ks.append('<div class="col s12 input-field">');
      ks.append('<span class="mselectmodal-trigger-title">対象品種</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="対象品種一覧" data="getHinsyu" displayspan="#G1002SsnHinsyu"><span id="G1002SsnHinsyu" class="blockquote-input">未選択</span></a>');
      ks.append('</div>');
      ks.append('</div>');
      ks.append('<div class="row">');
      htmlS  = '<div class="col s6 input-field">';
      htmlS += '<input type="text" placeholder="" id="G1002SsnSeiikuF" class="right-align input-text-color" style="">';
      htmlS += '<label for="G1002SsnSeiikuF">生育日数（自）</label>';
      htmlS += '</div>';
      ks.append(htmlS);
      htmlS  = '<div class="col s6 input-field">';
      htmlS += '<input type="text" placeholder="" id="G1002SsnSeiikuT" class="right-align input-text-color" style="">';
      htmlS += '<label for="G1002SsnSeiikuT">生育日数（至）</label>';
      htmlS += '</div>';
      ks.append(htmlS);
      ks.append('</div>');
      ks.append('<div class="row">');
      ks.append('<div class="col s12">');
      ks.append('<a href="#!" id="naesearchclear" class="waves-effect waves-green btn-flat left string-color" style="">クリア</a>');
      ks.append('<a href="#!" id="naesearchback" class="waves-effect waves-green btn-flat right string-color" style="">閉じる</a>');
      ks.append('<a href="#!" id="naesearchcommit" class="waves-effect waves-green btn-flat right string-color" style="">確　定</a>');
      ks.append('</div>');
      ks.append('</div>');

      $('#naesearchclear').unbind("click");
      $('#naesearchclear').bind("click", naesearchclear);
      $('#naesearchcommit').unbind("click");
      $('#naesearchcommit').bind("click", naesearchCommit);
      $('#naesearchback').unbind("click");
      $('#naesearchback').bind("click", naesearchClose);

      //------------------------------------------------------------------------------------------------------------------
      //- モーダルの初期化
      //------------------------------------------------------------------------------------------------------------------
      $('.modal').modal();
      //------------------------------------------------------------------------------------------------------------------
      //- セレクトモーダルの初期化
      //------------------------------------------------------------------------------------------------------------------
      $('.selectmodal-trigger').unbind('click');
      $('.selectmodal-trigger').bind('click', selectOpen);
      //------------------------------------------------------------------------------------------------------------------
      //- マルチセレクトモーダルの初期化
      //------------------------------------------------------------------------------------------------------------------
      $('.mselectmodal-trigger').unbind('click');
      $('.mselectmodal-trigger').bind('click', mSelectOpen);
      //------------------------------------------------------------------------------------------------------------------
      //- タイムライン検索条件の初期化
      //------------------------------------------------------------------------------------------------------------------
      $('#naesearchtrigger').unbind('click');
      $('#naesearchtrigger').bind('click', naesearchModalOpen);

      ks.hide();

    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 苗検索条件取得時
  //------------------------------------------------------------------------------------------------------------------
  function getNaesearchWhere() {
    var url = "/getAccountInfo";
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        if (jsonResult.result == 'SUCCESS') {
          //----- 対象生産物 -----
          mSelectDataGet("#G1002SsnCrop", userinfo.farm + "/getCrop");
          var crs = new String(jsonResult.ssnCrop).split(",");

          for (var key in crs) {
            var data = crs[key];
            var oJson = mSelectData(data);
            if (oJson != undefined) {
              oJson.select = true;
            }
          }
          mSelectClose();
          //----- 対象品種 -----
          mSelectDataGet("#G1002SsnHinsyu", "getHinsyu");
          var hss = new String(jsonResult.ssnHinsyu).split(",");

          for (var key in hss) {
            var data = hss[key];
            var oJson = mSelectData(data);
            if (oJson != undefined) {
              oJson.select = true;
            }
          }
          mSelectClose();
          //----- 生育日数 -----
          $('#G1002SsnSeiikuF').val(jsonResult.ssnSeiikuF);
          $('#G1002SsnSeiikuT').val(jsonResult.ssnSeiikuT);
        }
      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 苗検索条件オープン時
  //------------------------------------------------------------------------------------------------------------------
  function naesearchModalOpen() {
    var ks = $('#naesearch');
    var ms = $("#mainsection");
    getNaesearchWhere();     //苗検索条件取得
    if (ms != undefined) {
      ms.fadeOut(0);
    }
    if (ks != undefined) {
      ks.fadeIn(500);
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 苗検索条件閉じる時
  //------------------------------------------------------------------------------------------------------------------
  function naesearchClose() {
    var ks = $('#naesearch');
    var ms = $("#mainsection");
    if (ks != undefined) {
      ks.fadeOut(0);
    }
    if (ms != undefined) {
      ms.fadeIn(500);
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 苗検索条件クリア時
  //------------------------------------------------------------------------------------------------------------------
  function naesearchclear() {

    //----- 対象生産物 -----
    mSelectDataGet("#G1002SsnCrop", userinfo.farm + "/getCrop");
    for (var idx = 0; idx < oMselect.data.length; idx++) {
      oMselect.data[idx].select = false;
    }
    mSelectClose();
    //----- 対象品種 -----
    mSelectDataGet("#G1002SsnHinsyu", "getHinsyu");
    for (var idx = 0; idx < oMselect.data.length; idx++) {
      oMselect.data[idx].select = false;
    }
    mSelectClose();
    //----- 生育日数 -----
    $('#G1002SsnSeiikuF').val(0);
    $('#G1002SsnSeiikuT').val(0);

    displayToast('検索条件を初期化しました。確定ボタンを押下して条件を反映させてください。', 4000, '');
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 苗検索条件確定時
  //------------------------------------------------------------------------------------------------------------------
  function naesearchCommit() {

    //入力チェック
    var from = $("#G1002SsnSeiikuF").val();
    var to = $("#G1002SsnSeiikuT").val();

    //----- 生育日数が未入力の場合、0に補正する
    if (from == "") {
      from = 0;
    }
    else {
      if (!NumberCheck("G1002SsnSeiikuF", "生育日数（自）")) {
        return false;
      }
    }
    if (to == "") {
      to = 0;
    }
    else {
      if (!NumberCheck("G1002SsnSeiikuT", "生育日数（至）")) {
        return false;
      }
    }

    from *= 1;
    to *= 1;

    if (to < from) {
      alert("生育日数（自）に生育日数（至）より大きい数値が設定されています。");
      return false;
    }

    var sc = mSelectConvertJson("#G1002SsnCrop");
    var sh = mSelectConvertJson("#G1002SsnHinsyu");

    var jsondata = {};

    jsondata["sc"]      = sc;
    jsondata["sh"]      = sh;
    jsondata["from"]    = from;
    jsondata["to"]      = to;

    console.log("jsondata", jsondata);

    $.ajax({
      url:"/naesearchCommit",
      type:'POST',
      data:JSON.stringify(jsondata),                //入力用JSONデータ
      complete:function(data, status, jqXHR){           //処理成功時
    	GetNaeSPD();
        var ks = $('#naesearch');
        var ms = $("#mainsection");
        if (ks != undefined) {
          ks.fadeOut(0);
        }
        if (ms != undefined) {
          ms.fadeIn(500);
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });

  }
  //------------------------------------------------------------------------------------------------------------------
  //- ページトップボタン押下時
  //------------------------------------------------------------------------------------------------------------------
  function pageTopClick() {
    $(window).scrollTop(0);
    $("#G1002WorkList").scrollTop(0);
    $("#G1002HouseList").scrollTop(0);
    $(".timeline").scrollTop(0);
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 一括アコーディオン閉じるボタン押下時
  //------------------------------------------------------------------------------------------------------------------
  function groupCloseClick() {
    $(".field-item").hide();
    for(var group in groupinfo) {
      groupinfo[group] = false;
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 一括アコーディオン開くボタン押下時
  //------------------------------------------------------------------------------------------------------------------
  function groupOpenClick() {
    $(".field-item").show();
    for(var group in groupinfo) {
      groupinfo[group] = true;
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- タイムライン簡易表示ボタンクリック
  //------------------------------------------------------------------------------------------------------------------
  function timelineCloseClick() {
    $(".timelinelist").addClass("kani");
  }
  //------------------------------------------------------------------------------------------------------------------
  //- タイムライン詳細表示ボタンクリック
  //------------------------------------------------------------------------------------------------------------------
  function timelineOpenClick() {
    $(".timelinelist").removeClass("kani");
  }
  function houseView() {
    $("#menutimeline-icon").addClass("none");
    $("#menupage-icon").removeClass("none");
  }
  function timelineView() {
    $("#menupage-icon").addClass("none");
    $("#menutimeline-icon").removeClass("none");
  }
})(jQuery); // end of jQuery name space