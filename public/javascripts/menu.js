﻿(function($){
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
        sleep(0.1, GetFieldGroupSPD());
        sleep(0.1, kukakusearch());
      }

      $('#G0002WorkChainHidden').unbind("change");
      $('#G0002WorkChainHidden').bind("change", changeWorkChain);

      $('#G0002FieldGroupHidden').unbind("change");
      $('#G0002FieldGroupHidden').bind("change", changeFieldGroup);

      $("#G0002KukakuList").hide();
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

      if (userinfo.status == 1) { //ステータス異常有りの場合
        displayToast("作業状況に不安な担当者がいます。</br>「担当者別作業一覧」を確認してください。", 4000, "");
      }

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
        case 0: //圃場状況照会
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
            $("#timelinekey-" + localStorage.getItem("backWorkDiaryId"))[0].scrollIntoView(true);
            localStorage.setItem("backMode"         , null);
            localStorage.setItem("backWorkDiaryId"  , null);
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
    if (userinfo.contractPlan == 1) {	// Light
      htmlString+= '<li class="tab col s6"><a id="G0002tabwork" href="#workdiv">Work</a></li>';
      htmlString+= '<li class="tab col s6"><a id="G0002tabtime" href="#timelinediv">TimeLine</a></li>';
      delete_element("housediv");
    } else if (userinfo.contractPlan == 2 ||
               userinfo.contractPlan == 3) {	// Standard
      htmlString+= '<li class="tab col s4"><a id="G0002tabwork" href="#workdiv">Work</a></li>';
      htmlString+= '<li class="tab col s4"><a id="G0002tabfield" href="#housediv">Field</a></li>';
      htmlString+= '<li class="tab col s4"><a id="G0002tabtime" href="#timelinediv">TimeLine</a></li>';
    } else {	// Free,Pro
      htmlString+= '<li class="tab col s4"><a id="G0002tabwork" href="#workdiv">Work</a></li>';
      htmlString+= '<li class="tab col s4"><a id="G0002tabfield" href="#housediv">Field</a></li>';
      htmlString+= '<li class="tab col s4"><a id="G0002tabtime" href="#timelinediv">TimeLine</a></li>';
    }
    htmlString+= '</ul>';

    $("#G0002MainTab").html(htmlString);									//可変HTML部分に反映する
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
  function GetCompartment() {
    var inputJson = StringToJson('{"accountId":"' + accountInfo.accountId + '", "farmId":"' + accountInfo.farmId + '"}');

    grafhObject = new Object();	/* グラフオブジェクトを初期化する */

    $("#G0002HouseList").empty();
    $("#G0002HouseList").append('<div class="progress"><div class="indeterminate"></div></div>');

      $.ajax({
        url:"/getCompartment",										//メニュー初期処理
        type:'POST',
        data:JSON.stringify(inputJson),								//入力用JSONデータ
        complete:function(data, status, jqXHR){						//処理成功時
            var jsonResult 	= JSON.parse( data.responseText );			//戻り値用JSONデータの生成
            var kukakuJoukyouList = jsonResult.targetCompartmentStatus;	//ハウス状況対象リスト
            var kukakuGroupList   = jsonResult.targetCompartmentGroup;	//ハウスグループ対象リスト
            var htmlString	= "";										//可変HTML文字列
            var htmlLiString= "";										//区画ナビゲーションHTML文字列
            var iCount		= 0;										//偶奇判定件数
            var sPattern		= "";										//パターンスタイル

            $("#G0002HouseList").empty();
            $(".house-working-statusgraph").remove();

            htmlString	= "";											//可変HTML文字列初期化

            //----- ここからハウス状況照会の編集 ----
            if (jsonResult.result == "SUCCESS") {
              if (jsonResult.displaystatus == 1) { //圃場の場合
                $("#G0002Displayname").text("Field Status");
              }
              else { //区画の場合
                $("#G0002Displayname").text("Compartment Status");
              }
              var hl = $("#G0002HouseList");
              if (hl != undefined) {
                var ul = $('#houselist');
                var oldGroupId = 0;
                var lists = [];
                for ( var houseKey in kukakuJoukyouList ) {                     //対象圃場情報分処理を行う
                  lists.push(kukakuJoukyouList[houseKey]);
                }
                lists.sort(kukakuJokyoCompare);
                for ( var houseKey in lists ) {                     //対象圃場情報分処理を行う
                  var houseStatus = lists[houseKey];                //ハウス状況情報の取得
                  var key = new String(houseStatus.kukakuId).split(".");

                  var code = houseStatus["fieldGroupColor"];
                  var red   = parseInt(code.substring(0,2), 16);
                  var green = parseInt(code.substring(2,4), 16);
                  var blue  = parseInt(code.substring(4,6), 16);

                  if (oldGroupId != houseStatus["fieldGroupId"]) {
                    if (oldGroupId != 0) {
                      groupinfo[oldGroupId] = false;
                      $(".g-" + oldGroupId).hide();
                    }

//                    ul.append('<li class="collection-item group-item" style="border-bottom: 0px;border-left: 8px solid #' + houseStatus["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJOG + ');" fieldGroupId="' + houseStatus["fieldGroupId"] +'"><span class="field">' + houseStatus["fieldGroupName"] + 'グループ</span></li>');
                    ul.append('<li class="collection-item group-item" style="border-bottom: 0px;border-left: 8px solid rgba(' + red + ',' + green + ',' + blue + ', 1.0); background: linear-gradient(-75deg, rgba(' + red + ',' + green + ',' + blue + ', .6), rgba(' + red + ',' + green + ',' + blue + ', 1.0));" fieldGroupId="' + houseStatus["fieldGroupId"] +'"><span class="field">' + houseStatus["fieldGroupName"] + 'グループ</span></li>');
                  }
                  oldGroupId = houseStatus["fieldGroupId"];
                  if (jsonResult.displaystatus == 1) { //圃場の場合
                    ul.append('<li id="item' + key[0] + '" class="collection-item field-item wfield-item g-' + houseStatus["fieldGroupId"] +'" style="border-left: 8px solid #' + houseStatus["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJO + ');" key="' + houseStatus["fieldId"] +'"><span class="field">' + houseStatus["fieldName"] + '</span><i class="material-icons">chevron_right</i></li>');
                  }
                  else { //区画の場合
                    ul.append('<li id="item' + key[0] + '" class="collection-item field-item g-' + houseStatus["fieldGroupId"] +'" style="border-left: 8px solid #' + houseStatus["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJO + ');" key="' + houseStatus["fieldId"] +'"><span class="field">' + houseStatus["kukakuName"] + '</span><span class="kukaku">(' + houseStatus["fieldName"] + ')</span><span class="kukaku">' + houseStatus["kukakuKind"] + '</span><i class="material-icons">chevron_right</i></li>');
                    var li = $('#item' + key[0]);
                    var chain = '<div class="chain">';
                    if (jsonResult.displayChain != 99) {
                      var chaindata = houseStatus["chain"];
                      for (var ckey in chaindata) {
                        var data = chaindata[ckey];
                        if ((jsonResult.displayChain == 1) && (data.chain == 0)) {
                          continue;
                        }
                        if (data.end == 1) {
                          chain += '<span class="chainitem" style="background-color: #' + data.color +';">' + data.name + '</span>';
                        }
                        else {
                          chain += '<span class="chainitem">' + data.name + '</span>';
                        }
                      }
                    }
                    chain += '</div>';
                    li.append(chain);
                    var hinsyu = "";
                    li.append('<ul id="item' + key[0] + '-wrapper" class="item-div-wrapper"></ul>');
                    var wrapper = $("#item" + key[0] + "-wrapper");
                    wrapper.append('<li id="item' + key[0] + '-itemarea1" class="item-div-area"></li>');
                    var itemarea = $("#item" + key[0] + "-itemarea1");
                    itemarea.append('<div id="item' + key[0] + '-item1" class="item-div-item valign-wrapper"></div>');
                    var div = $("#item" + key[0] + "-item1");
                    div.append('<span class="sub">' + houseStatus["workYear"] + '年</span>');
                    div.append('<span class="item">' + houseStatus["rotationSpeedOfYear"] + '作目</span>');
                    div.append('<span class="sub">' + houseStatus["cropName"] + '</span>');
                    div.append('<span class="item">' + houseStatus["hinsyuName"] + '</span>');
                    itemarea.append('<div id="item' + key[0] + '-item2" class="item-div-item valign-wrapper"></div>');
                    div = $("#item" + key[0] + "-item2");
                    div.append('<img class="icon"  src="/assets/svg/field-icon-hasyu.svg">');
                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["hashuDate"]) + '<span class="sub">' + houseStatus["hashuCount"] + '&nbsp;回</span></span>');
                    div.append('<span class="item bottom">' + houseStatus["seiikuDayCount"] + '日&nbsp;～&nbsp;' + houseStatus["seiikuDayCountEnd"] + '日</span>');
                    itemarea.append('<div id="item' + key[0] + '-item3" class="item-div-item valign-wrapper"></div>');
                    div = $("#item" + key[0] + "-item3");
                    div.append('<img class="icon"  src="/assets/svg/field-icon-shukaku.svg">');
                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalShukakuNumber"]) + '&nbsp;Kg</span>');
                    div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["shukakuStartDate"]) + '<span class="sub">&nbsp;～&nbsp</span>' + ConvertNullDate(houseStatus["shukakuEndDate"]) + '</span>');
                    wrapper.append('<li id="item' + key[0] + '-itemarea2" class="item-div-area"></li>');
                    itemarea = $("#item" + key[0] + "-itemarea2");
                    itemarea.append('<div id="item' + key[0] + '-item4" class="item-div-item valign-wrapper"></div>');
                    div = $("#item" + key[0] + "-item4");
                    div.append('<img class="icon"  src="/assets/svg/field-icon-hiryo.svg">');
                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalTuihiNumber"]) + '<span class="sub">Kg</span></span>');
                    div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalTuihiDate"]) + '<span class="sub">' + houseStatus["tuihiCount"] + '&nbsp;日経過</span></span>');
                    itemarea.append('<div id="item' + key[0] + '-item5" class="item-div-item valign-wrapper"></div>');
                    div = $("#item" + key[0] + "-item5");
                    div.append('<img class="icon"  src="/assets/svg/field-icon-kansui.svg">');
                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalKansuiNumber"]) + '<span class="sub">L</span></span>');
                    div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalKansuiDate"]) + '<span class="sub">' + houseStatus["kansuiCount"] + '&nbsp;日経過</span></span>');
                    itemarea.append('<div id="item' + key[0] + '-item6" class="item-div-item valign-wrapper"></div>');
                    div = $("#item" + key[0] + "-item6");
                    div.append('<img class="icon"  src="/assets/svg/field-icon-syodoku.svg">');
                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalDisinfectionNumber"]) + '<span class="sub">L</span></span>');
                    div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalDisinfectionDate"]) + '<span class="sub">' + houseStatus["disinfectionCount"] + '&nbsp;日経過</span></span>');
                    wrapper.append('<li id="item' + key[0] + '-itemarea3" class="item-div-area"></li>');
                    itemarea = $("#item" + key[0] + "-itemarea3");
                    itemarea.append('<div id="item' + key[0] + '-item7" class="item-div-item valign-wrapper"></div>');
                    div = $("#item" + key[0] + "-item7");
                    div.append('<img class="icon"  src="/assets/svg/field-icon-sun.svg">');
                    div.append('<span class="item">' + ConvertNullDate(houseStatus["totalSolarRadiation"]) + '</span>');
                    div.append('<span class="sub">℃</span>');
                    div.append('<span class="sub">' + houseStatus["yosokuSolarRadiation"] + '&nbsp;℃</span>');
                    itemarea.append('<div id="item' + key[0] + '-item8" class="item-div-item valign-wrapper"></div>');
                    div = $("#item" + key[0] + "-item8");
                    div.append('<img class="icon"  src="/assets/svg/field-icon-rain.svg">');
                    div.append('<span class="item">' + ConvertNullDate(houseStatus["rain"]) + '</span>');
                    div.append('<span class="sub">mm</span>');

                    if (houseStatus["pestId"] != 0 &&
                        (userinfo.contractPlan == 0 ||
                         userinfo.contractPlan >= 4)) {
                      li.append('<i class="material-icons adb ' + houseStatus["pestOn"] + '" pestname="' + houseStatus["pestName"] + '" pestdate="' + houseStatus["pestPredictDate"] + '">adb</i>');
                    }
                    li.append('<a class="waves-effect waves-light btn-flat saku-start" kukakuname="' + houseStatus["kukakuName"] + '" kukakuid="' + houseStatus["kukakuId"] + '" item="item' + key[0] + '">作</a>');
                    if (userinfo.contractPlan == 0 ||
                        userinfo.contractPlan >= 4) {
                      li.append('<i class="material-icons aica-analysis" style="color: #26a69a;"  kukakuid="' + houseStatus["kukakuId"] + '">analytics</i>');
                    }
                  }
                }
                if (oldGroupId != 0) {
                  groupinfo[oldGroupId] = false;
                  $(".g-" + oldGroupId).hide();
                }
                $(".adb").unbind("click");
                $(".adb").bind("click", pestAlert);
                if (userinfo.contractPlan == 0 ||
                    userinfo.contractPlan >= 4) {
                  $(".aica-analysis").unbind("click");
                  $(".aica-analysis").bind("click", analysis);
                }
                $(".saku-start").unbind("click");
                $(".saku-start").bind("click", sakuStart);
                $(".group-item").unbind("click");
                $(".group-item").bind("click", groupDisplay);
                $(".field-item").unbind("click");
                $(".field-item").bind("click", fieldDisplay);
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

        },
        dataType:'json',
        contentType:'text/json'
      });
  }
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
  function GetFieldGroupSPD() {
	var inputJson = StringToJson('{"accountId":"' + accountInfo.accountId + '", "farmId":"' + accountInfo.farmId + '"}');

	$("#G0002HouseList").empty();
	$("#G0002HouseList").append('<div class="progress"><div class="indeterminate"></div></div>');

    $.ajax({
      url:"/getFieldGroupSPD",										//メニュー初期処理
      type:'GET',
      complete:function(data, status, jqXHR){						//処理成功時
        var jsonResult 	= JSON.parse( data.responseText );			//戻り値用JSONデータの生成
        var kukakuGroupList   = jsonResult.targetCompartmentGroup;	//ハウスグループ対象リスト
        var htmlString	= "";										//可変HTML文字列
        var iCount		= 0;										//偶奇判定件数

        $("#G0002HouseList").empty();

        htmlString	= "";											//可変HTML文字列初期化

        //----- ここからハウス状況照会の編集 ----
        if (jsonResult.result == "SUCCESS") {
          if (jsonResult.displaystatus == 1) { //圃場の場合
            $("#G0002Displayname").text("Field Status");
          }
          else { //区画の場合
            $("#G0002Displayname").text("Compartment Status");
          }
          var hl = $("#G0002HouseList");
          if (hl != undefined) {
            hl.append('<ul id="houselist" class="collection" style="border: 0px;">');
            var ul = $('#houselist');
            var oldGroupId = 0;
            var lists = [];
            for ( var houseKey in kukakuGroupList ) {
              lists.push(kukakuGroupList[houseKey]);
            }
            lists.sort(fieldGroupCompare);
            for ( var houseKey in lists ) {                         	//対象圃場情報分処理を行う
              var houseStatus = lists[houseKey];                    	//ハウス状況情報の取得
              var key = new String(houseStatus.fieldGroupId).split(".");

              var code = houseStatus["fieldGroupColor"];
              var red   = parseInt(code.substring(0,2), 16);
              var green = parseInt(code.substring(2,4), 16);
              var blue  = parseInt(code.substring(4,6), 16);

              ul.append('<li class="collection-item group-item" id="groupId-' + key[0] + '" data="off" style="border-bottom: 0px;border-left: 8px solid rgba(' + red + ',' + green + ',' + blue + ', 1.0); background: linear-gradient(-75deg, rgba(' + red + ',' + green + ',' + blue + ', .6), rgba(' + red + ',' + green + ',' + blue + ', 1.0));" fieldGroupId="' + houseStatus["fieldGroupId"] +'"><span class="field">' + houseStatus["fieldGroupName"] + 'グループ</span></li>');
            }
            groupinfo[oldGroupId] = false;
            $(".g-" + oldGroupId).hide();

            $(".group-item").unbind("click");
            $(".group-item").bind("click", groupDisplaySP);
            if (localStorage.getItem("backMode") == "3") {
              $("#groupId-" + localStorage.getItem("backFieldGroupId")).click();
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
      },
        dataType:'json',
        contentType:'text/json'
      });
  }
  function GetFieldGroupSP() {
	var inputJson = StringToJson('{"accountId":"' + accountInfo.accountId + '", "farmId":"' + accountInfo.farmId + '"}');

	$("#G0002HouseList").empty();
	$("#G0002HouseList").append('<div class="progress"><div class="indeterminate"></div></div>');

    $.ajax({
      url:"/getFieldGroupSP",										//メニュー初期処理
      type:'GET',
      complete:function(data, status, jqXHR){						//処理成功時
        var jsonResult 	= JSON.parse( data.responseText );			//戻り値用JSONデータの生成
        var kukakuGroupList   = jsonResult.targetCompartmentGroup;	//ハウスグループ対象リスト
        var htmlString	= "";										//可変HTML文字列
        var iCount		= 0;										//偶奇判定件数

        $("#G0002HouseList").empty();

        htmlString	= "";											//可変HTML文字列初期化

        //----- ここからハウス状況照会の編集 ----
        if (jsonResult.result == "SUCCESS") {
          if (jsonResult.displaystatus == 1) { //圃場の場合
            $("#G0002Displayname").text("Field Status");
          }
          else { //区画の場合
            $("#G0002Displayname").text("Compartment Status");
          }
          var hl = $("#G0002HouseList");
          if (hl != undefined) {
            hl.append('<ul id="houselist" class="collection" style="border: 0px;">');
            var ul = $('#houselist');
            var oldGroupId = 0;
            var lists = [];
            for ( var houseKey in kukakuGroupList ) {
              lists.push(kukakuGroupList[houseKey]);
            }
            lists.sort(fieldGroupCompare);
            for ( var houseKey in lists ) {                         	//対象圃場情報分処理を行う
              var houseStatus = lists[houseKey];                    	//ハウス状況情報の取得
              var key = new String(houseStatus.fieldGroupId).split(".");

              var code = houseStatus["fieldGroupColor"];
              var red   = parseInt(code.substring(0,2), 16);
              var green = parseInt(code.substring(2,4), 16);
              var blue  = parseInt(code.substring(4,6), 16);

              ul.append('<li class="collection-item group-item" id="groupId-' + key[0] + '" data="off" style="border-bottom: 0px;border-left: 8px solid rgba(' + red + ',' + green + ',' + blue + ', 1.0); background: linear-gradient(-75deg, rgba(' + red + ',' + green + ',' + blue + ', .6), rgba(' + red + ',' + green + ',' + blue + ', 1.0));" fieldGroupId="' + houseStatus["fieldGroupId"] +'"><span class="field">' + houseStatus["fieldGroupName"] + 'グループ</span></li>');
            }
            groupinfo[oldGroupId] = false;
            $(".g-" + oldGroupId).hide();

            $(".group-item").unbind("click");
            $(".group-item").bind("click", groupDisplaySP);
          }
        }
        else if (jsonResult.result == "REDIRECT") {
          window.location.href = '/move';
          return;
        }
        else {
          displayToast("該当データが存在しませんでした。", 4000, "");
        }
      },
        dataType:'json',
        contentType:'text/json'
      });
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
  function groupDisplaySP() {
	var group = $(this);
	if (groupinfo[group.attr("fieldGroupId")]) {
	  $(".g-" + group.attr("fieldGroupId")).hide();
	  groupinfo[group.attr("fieldGroupId")] = false;
	}
	else {
	  $(".g-" + group.attr("fieldGroupId")).show();
	  groupinfo[group.attr("fieldGroupId")] = true;
	  if (group.attr("data") == "off") {

		    grafhObject = new Object();	/* グラフオブジェクトを初期化する */

		    var url = "/" + group.attr("fieldGroupId") + "/getKukakuSP"

		      $.ajax({
		        url: url,
		        type:'GET',
		        complete:function(data, status, jqXHR){						//処理成功時
		            var jsonResult 	= JSON.parse( data.responseText );			//戻り値用JSONデータの生成
		            var kukakuJoukyouList = jsonResult.targetCompartmentStatus;	//ハウス状況対象リスト
		            var kukakuGroupList   = jsonResult.targetCompartmentGroup;	//ハウスグループ対象リスト
		            var htmlString	= "";										//可変HTML文字列
		            var htmlLiString= "";										//区画ナビゲーションHTML文字列
		            var iCount		= 0;										//偶奇判定件数
		            var sPattern		= "";										//パターンスタイル

		            htmlString	= "";											//可変HTML文字列初期化

		            //----- ここからハウス状況照会の編集 ----
		            if (jsonResult.result == "SUCCESS") {
		              var hl = $("#G0002HouseList");
		              if (hl != undefined) {
		                var ul = $('#groupId-' + group.attr("fieldGroupId"));
		                var lists = [];
		                for ( var houseKey in kukakuJoukyouList ) {                     //対象圃場情報分処理を行う
		                  lists.push(kukakuJoukyouList[houseKey]);
		                }
		                lists.sort(kukakuJokyoCompareSP);
		                for ( var houseKey in lists ) {                     //対象圃場情報分処理を行う
		                  var houseStatus = lists[houseKey];                //ハウス状況情報の取得
		                  var key = new String(houseStatus.kukakuId).split(".");

		                  var code = houseStatus["fieldGroupColor"];
		                  var red   = parseInt(code.substring(0,2), 16);
		                  var green = parseInt(code.substring(2,4), 16);
		                  var blue  = parseInt(code.substring(4,6), 16);

		                  if (jsonResult.displaystatus == 1) { //圃場の場合
		                    ul.after('<li id="item' + houseStatus["fieldId"] + '" class="collection-item field-item wfield-item g-' + group.attr("fieldGroupId") +'" style="border-left: 8px solid #' + houseStatus["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJO + ');" key="' + houseStatus["fieldId"] +'"><span class="field">' + houseStatus["fieldName"] + '</span><i class="material-icons">chevron_right</i></li>');
		                  }
		                  else { //区画の場合
		                    ul.after('<li id="item' + key[0] + '" class="collection-item field-item g-' + group.attr("fieldGroupId") +'" style="border-left: 8px solid #' + houseStatus["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJO + ');" key="' + houseStatus["fieldId"] +'"><span class="field">' + houseStatus["kukakuName"] + '</span><span class="kukaku">(' + houseStatus["fieldName"] + ')</span><span class="kukaku">' + houseStatus["kukakuKind"] + '</span><i class="material-icons">chevron_right</i></li>');
		                    var li = $('#item' + key[0]);
		                    var chain = '<div class="chain">';
		                    if (jsonResult.displayChain != 99) {
		                      var chaindata = houseStatus["chain"];
		                      for (var ckey in chaindata) {
		                        var data = chaindata[ckey];
		                        if ((jsonResult.displayChain == 1) && (data.chain == 0)) {
		                          continue;
		                        }
		                        if (data.end == 1) {
		                          chain += '<span class="chainitem" style="background-color: #' + data.color +';">' + data.name + '</span>';
		                        }
		                        else {
		                          chain += '<span class="chainitem">' + data.name + '</span>';
		                        }
		                      }
		                    }
		                    chain += '</div>';
		                    li.append(chain);
		                    var hinsyu = "";
		                    li.append('<ul id="item' + key[0] + '-wrapper" class="item-div-wrapper"></ul>');
		                    var wrapper = $("#item" + key[0] + "-wrapper");
		                    wrapper.append('<li id="item' + key[0] + '-itemarea1" class="item-div-area"></li>');
		                    var itemarea = $("#item" + key[0] + "-itemarea1");
		                    itemarea.append('<div id="item' + key[0] + '-item1" class="item-div-item valign-wrapper"></div>');
		                    var div = $("#item" + key[0] + "-item1");
		                    div.append('<span class="sub">' + houseStatus["workYear"] + '年</span>');
		                    div.append('<span class="item">' + houseStatus["rotationSpeedOfYear"] + '作目</span>');
		                    div.append('<span class="sub">' + houseStatus["cropName"] + '</span>');
		                    div.append('<span class="item">' + houseStatus["hinsyuName"] + '</span>');
		                    itemarea.append('<div id="item' + key[0] + '-item2" class="item-div-item valign-wrapper"></div>');
		                    div = $("#item" + key[0] + "-item2");
		                    div.append('<img class="icon"  src="/assets/svg/field-icon-hasyu.svg">');
		                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["hashuDate"]) + '<span class="sub">' + houseStatus["hashuCount"] + '&nbsp;回</span></span>');
		                    div.append('<span class="item bottom">' + houseStatus["seiikuDayCount"] + '日&nbsp;～&nbsp;' + houseStatus["seiikuDayCountEnd"] + '日</span>');
		                    itemarea.append('<div id="item' + key[0] + '-item3" class="item-div-item valign-wrapper"></div>');
		                    div = $("#item" + key[0] + "-item3");
		                    div.append('<img class="icon"  src="/assets/svg/field-icon-shukaku.svg">');
		                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalShukakuNumber"]) + '&nbsp;Kg</span>');
		                    div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["shukakuStartDate"]) + '<span class="sub">&nbsp;～&nbsp</span>' + ConvertNullDate(houseStatus["shukakuEndDate"]) + '</span>');
		                    wrapper.append('<li id="item' + key[0] + '-itemarea2" class="item-div-area"></li>');
		                    itemarea = $("#item" + key[0] + "-itemarea2");
		                    itemarea.append('<div id="item' + key[0] + '-item4" class="item-div-item valign-wrapper"></div>');
		                    div = $("#item" + key[0] + "-item4");
		                    div.append('<img class="icon"  src="/assets/svg/field-icon-hiryo.svg">');
		                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalTuihiNumber"]) + '<span class="sub">Kg</span></span>');
		                    div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalTuihiDate"]) + '<span class="sub">' + houseStatus["tuihiCount"] + '&nbsp;日経過</span></span>');
		                    itemarea.append('<div id="item' + key[0] + '-item5" class="item-div-item valign-wrapper"></div>');
		                    div = $("#item" + key[0] + "-item5");
		                    div.append('<img class="icon"  src="/assets/svg/field-icon-kansui.svg">');
		                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalKansuiNumber"]) + '<span class="sub">L</span></span>');
		                    div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalKansuiDate"]) + '<span class="sub">' + houseStatus["kansuiCount"] + '&nbsp;日経過</span></span>');
		                    itemarea.append('<div id="item' + key[0] + '-item6" class="item-div-item valign-wrapper"></div>');
		                    div = $("#item" + key[0] + "-item6");
		                    div.append('<img class="icon"  src="/assets/svg/field-icon-syodoku.svg">');
		                    div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalDisinfectionNumber"]) + '<span class="sub">L</span></span>');
		                    div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalDisinfectionDate"]) + '<span class="sub">' + houseStatus["disinfectionCount"] + '&nbsp;日経過</span></span>');
		                    wrapper.append('<li id="item' + key[0] + '-itemarea3" class="item-div-area"></li>');
		                    itemarea = $("#item" + key[0] + "-itemarea3");
		                    itemarea.append('<div id="item' + key[0] + '-item7" class="item-div-item valign-wrapper"></div>');
		                    div = $("#item" + key[0] + "-item7");
		                    div.append('<img class="icon"  src="/assets/svg/field-icon-sun.svg">');
		                    div.append('<span class="item">' + ConvertNullDate(houseStatus["totalSolarRadiation"]) + '</span>');
		                    div.append('<span class="sub">℃</span>');
		                    div.append('<span class="sub">' + houseStatus["yosokuSolarRadiation"] + '&nbsp;℃</span>');
		                    itemarea.append('<div id="item' + key[0] + '-item8" class="item-div-item valign-wrapper"></div>');
		                    div = $("#item" + key[0] + "-item8");
		                    div.append('<img class="icon"  src="/assets/svg/field-icon-rain.svg">');
		                    div.append('<span class="item">' + ConvertNullDate(houseStatus["rain"]) + '</span>');
		                    div.append('<span class="sub">mm</span>');

		                    if (houseStatus["pestId"] != 0 &&
		                        (userinfo.contractPlan == 0 ||
		                         userinfo.contractPlan >= 4)) {
		                      li.append('<i class="material-icons adb ' + houseStatus["pestOn"] + '" pestname="' + houseStatus["pestName"] + '" pestdate="' + houseStatus["pestPredictDate"] + '">adb</i>');
		                    }
		                    li.append('<a class="waves-effect waves-light btn-flat saku-start" kukakuname="' + houseStatus["kukakuName"] + '" kukakuid="' + houseStatus["kukakuId"] + '" item="item' + key[0] + '">作</a>');
		                    if (userinfo.contractPlan == 0 ||
		                        userinfo.contractPlan >= 4) {
		                      li.append('<i class="material-icons aica-analysis" style="color: #26a69a;"  kukakuid="' + houseStatus["kukakuId"] + '">analytics</i>');
		                    }
		                  }
		                }
		                $("#G0002HouseList .adb").unbind("click");
		                $("#G0002HouseList .adb").bind("click", pestAlert);
		                if (userinfo.contractPlan == 0 ||
		                    userinfo.contractPlan >= 4) {
		                  $("#G0002HouseList .aica-analysis").unbind("click");
		                  $("#G0002HouseList .aica-analysis").bind("click", analysis);
		                }
		                $("#G0002HouseList .saku-start").unbind("click");
		                $("#G0002HouseList .saku-start").bind("click", sakuStart);
		                $("#G0002HouseList .field-item").unbind("click");
		                $("#G0002HouseList .field-item").bind("click", fieldDisplay);
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
		            group.attr("data", "on");
		        },
		        dataType:'json',
		        contentType:'text/json'
		      });
	  }
	}
  }
  function pestAlert() {
    var pest = $(this);
    window.alert('[' + pest.attr("pestname") + ']\n' + pest.attr("pestdate") + 'に発生するかもしれません。');
    return false;
  }
  function analysis() {
    var my = $(this);
    var url="/" + my.attr("kukakuid") + "/aicaSaibaiManage";
    displayToast('栽培管理予測を開始します', 4000, 'rounded');
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );
        displayToast('栽培管理予測結果を表示します', 4000, 'rounded');

        lockScreen(LOCK_ID);

        //----- 一時エリアの作成 -----
        var divTag = $('<div />').attr("id", "aicaSaibaiManageArea");
        divTag.addClass("aicaSaibaiManageArea");

        $('body').append(divTag);
        var area = $("#aicaSaibaiManageArea");

        //----- 想定条件 -----
        area.append('<div class="itemTitle">AICAによる栽培予測<i class="material-icons aica-analysis-close">close</i></div>');
        area.append('<div class="item">播種日：' + jsonResult.hasyuDate + '</div>');
        area.append('<div class="item">品種：' + jsonResult.hinsyuName + '</div>');
        //----- 作付管理 -----
        area.append('<div class="itemTitle">作付管理</div>');
        var sakudukeList = jsonResult.sakudukeList;
        for(var key in sakudukeList) {
          var data = sakudukeList[key];
          area.append('<div class="item">' + data.date + '&nbsp;' + data.name + '</div>');
        }
        //----- 生育管理 -----
        area.append('<div class="itemTitle">生育管理（' + jsonResult.seiiku + '日）</div>');
        var saibaiList = jsonResult.saibaiList;
        for(var key in saibaiList) {
          var data = saibaiList[key];
          if (data.workTime != undefined) {
            area.append('<div class="item">' + data.date + '&nbsp;' + data.name + '&nbsp;' + data.workTime + '分</div>');
          }
          else {
            area.append('<div class="item">' + data.date + '&nbsp;' + data.name + '</div>');
          }
          var sanpu = data.sanpu;
          if (sanpu != undefined) {
            for(var skey in sanpu) {
              var sdata = sanpu[skey];
              area.append('<div class="item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + sdata.nouhiName + '&nbsp;' + sdata.bairitu + '倍&nbsp;' + sdata.sanpuryo + sdata.unit + '</div>');
            }
          }
        }
        //----- 収穫 -----
        area.append('<div class="itemTitle">収穫（' + jsonResult.souteiShukakuRyo + 'Kg）</div>');
        var shukakuList = jsonResult.shukakuList;
        for(var key in shukakuList) {
          var data = shukakuList[key];
          area.append('<div class="item">' + data.date + '&nbsp;' + data.name + '&nbsp;' + data.ninzu + '人</div>');
        }
        $(".aica-analysis-close").unbind("click");
        $(".aica-analysis-close").bind("click", analysisClose);
      },
      error:function(data, status, jqXHR){
        displayToast('栽培管理予測に失敗しました', 4000, 'rounded');
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
    return false;
  }
  function analysisClose() {
    var area = $("#aicaSaibaiManageArea");
    area.remove();
    unlockScreen(LOCK_ID);
  }
  function sakuStart() {
    var saku = $(this);
    var result = window.confirm('【区画】' + saku.attr("kukakuname") + 'を作付開始します。\nよろしいですか？');
    if (result) {
      var inputjson = {};
      inputjson["workDate"]     = "";
      inputjson["workKukaku"]   = saku.attr("kukakuId");
      inputjson["workTime"]     = 0;
      inputjson["workAccount"]  = userinfo.id;
      inputjson["workId"]       = 13;
      inputjson["workDiaryId"]  = 0;
      inputjson["mode"]         = 2;

      var dt = new Date();
      var sedate = dt.getFullYear() + "/" + (parseInt(dt.getMonth())+1) + "/" + dt.getDate();
      var setime = dt.getHours() + ":" + dt.getMinutes();

      inputjson["startDate"]    = sedate;
      inputjson["startTime"]    = setime;
      inputjson["endDate"]      = sedate;
      inputjson["endTime"]      = setime;

      $.ajax({
        url:"/submitWorkDiary",
        type:'POST',
        data:JSON.stringify(inputjson),
        complete:function(data, status, jqXHR){
          var jsonResult = JSON.parse( data.responseText );

          if (jsonResult.result == 'SUCCESS') {
            displayToast('作業日誌を記録しました。', 4000, 'rounded');
            var url = "/" + saku.attr("kukakuId") + "/getKukakuStatus";
            $.ajax({
              url:url,
              type:'GET',
              complete:function(data, status, jqXHR){
                var jsonResult = JSON.parse( data.responseText );

                if (jsonResult.result == 'SUCCESS') {
                  timelineInit();
                  var li = $('#' + saku.attr("item"));
                  li.empty();
                  var houseStatus = jsonResult.status;
                  var chain = '<div class="chain">';
                  if (jsonResult.displayChain != 99) {
                    var chaindata = houseStatus["chain"];
                    for (var ckey in chaindata) {
                      var data = chaindata[ckey];
                      if ((jsonResult.displayChain == 1) && (data.chain == 0)) {
                        continue;
                      }
                      if (data.end == 1) {
                        chain += '<span class="chainitem" style="background-color: #' + data.color +';">' + data.name + '</span>';
                      }
                      else {
                        chain += '<span class="chainitem">' + data.name + '</span>';
                      }
                    }
                  }
                  chain += '</div>';
                  var hinsyu = "";
                  var key = (inputjson["workKukaku"] + ".0").split(".");
//                  if (houseStatus["hinsyuName"] != "") {
//                    hinsyu = " ( " + houseStatus["hinsyuName"] + " )"
//                  }
                  li.append('<span class="field">' + houseStatus["kukakuName"] + '</span><span class="kukaku">(' + houseStatus["fieldName"] + ')</span><span class="kukaku">' + houseStatus["kukakuKind"] + '</span><i class="material-icons">chevron_right</i>');
                  li.append(chain);
                  li.append('<ul id="item' + key[0] + '-wrapper" class="item-div-wrapper"></ul>');
                  var wrapper = $("#item" + key[0] + "-wrapper");
                  wrapper.append('<li id="item' + key[0] + '-itemarea1" class="item-div-area"></li>');
                  var itemarea = $("#item" + key[0] + "-itemarea1");
                  itemarea.append('<div id="item' + key[0] + '-item1" class="item-div-item valign-wrapper"></div>');
                  var div = $("#item" + key[0] + "-item1");
                  div.append('<span class="sub">' + houseStatus["workYear"] + '年</span>');
                  div.append('<span class="item">' + houseStatus["rotationSpeedOfYear"] + '作目</span>');
                  div.append('<span class="sub">' + houseStatus["cropName"] + '</span>');
                  div.append('<span class="item">' + houseStatus["hinsyuName"] + '</span>');
                  itemarea.append('<div id="item' + key[0] + '-item2" class="item-div-item valign-wrapper"></div>');
                  div = $("#item" + key[0] + "-item2");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-hasyu.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(houseStatus["hashuDate"]) + '<span class="sub">' + houseStatus["hashuCount"] + '&nbsp;回</span></span>');
                  div.append('<span class="item bottom">' + houseStatus["seiikuDayCount"] + '日&nbsp;～&nbsp;' + houseStatus["seiikuDayCountEnd"] + '日</span>');
                  itemarea.append('<div id="item' + key[0] + '-item3" class="item-div-item valign-wrapper"></div>');
                  div = $("#item" + key[0] + "-item3");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-shukaku.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalShukakuNumber"]) + '<span class="sub">Kg</span></span>');
                  div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["shukakuStartDate"]) + '<span class="sub">&nbsp;～&nbsp</span>' + ConvertNullDate(houseStatus["shukakuEndDate"]) + '</span>');
                  wrapper.append('<li id="item' + key[0] + '-itemarea2" class="item-div-area"></li>');
                  itemarea = $("#item" + key[0] + "-itemarea2");
                  itemarea.append('<div id="item' + key[0] + '-item4" class="item-div-item valign-wrapper"></div>');
                  div = $("#item" + key[0] + "-item4");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-hiryo.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalTuihiNumber"]) + '<span class="sub">Kg</span></span>');
                  div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalTuihiDate"]) + '<span class="sub">' + houseStatus["tuihiCount"] + '&nbsp;日経過</span></span>');
                  itemarea.append('<div id="item' + key[0] + '-item5" class="item-div-item valign-wrapper"></div>');
                  div = $("#item" + key[0] + "-item5");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-kansui.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalKansuiNumber"]) + '<span class="sub">L</span></span>');
                  div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalKansuiDate"]) + '<span class="sub">' + houseStatus["kansuiCount"] + '&nbsp;日経過</span></span>');
                  itemarea.append('<div id="item' + key[0] + '-item6" class="item-div-item valign-wrapper"></div>');
                  div = $("#item" + key[0] + "-item6");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-syodoku.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(houseStatus["totalDisinfectionNumber"]) + '<span class="sub">L</span></span>');
                  div.append('<span class="item bottom">' + ConvertNullDate(houseStatus["finalDisinfectionDate"]) + '<span class="sub">' + houseStatus["disinfectionCount"] + '&nbsp;日経過</span></span>');
                  wrapper.append('<li id="item' + key[0] + '-itemarea3" class="item-div-area"></li>');
                  itemarea = $("#item" + key[0] + "-itemarea3");
                  itemarea.append('<div id="item' + key[0] + '-item7" class="item-div-item valign-wrapper"></div>');
                  div = $("#item" + key[0] + "-item7");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-sun.svg">');
                  div.append('<span class="item">' + ConvertNullDate(houseStatus["totalSolarRadiation"]) + '</span>');
                  div.append('<span class="sub">℃</span>');
                  div.append('<span class="sub">' + houseStatus["yosokuSolarRadiation"] + '&nbsp;℃</span>');
                  itemarea.append('<div id="item' + key[0] + '-item8" class="item-div-item valign-wrapper"></div>');
                  div = $("#item" + key[0] + "-item8");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-rain.svg">');
                  div.append('<span class="item">' + ConvertNullDate(houseStatus["rain"]) + '</span>');
                  div.append('<span class="sub">mm</span>');

                  li.append('<a class="waves-effect waves-light btn-flat saku-start" kukakuname="' + houseStatus["kukakuName"] + '" kukakuid="' + houseStatus["kukakuId"] + '" item="item' + key[0] + '">作</a>');
                  if (userinfo.contractPlan == 0 ||
                      userinfo.contractPlan >= 4) {
                    li.append('<i class="material-icons aica-analysis" style="color: #26a69a;"  kukakuid="' + houseStatus["kukakuId"] + '">analytics</i>');
                  }
                  $(".saku-start").unbind("click");
                  $(".saku-start").bind("click", sakuStart);
                  $(".group-item").unbind("click");
                  $(".group-item").bind("click", groupDisplay);
                  $(".field-item").unbind("click");
                  $(".field-item").bind("click", fieldDisplay);
                }

              },
              dataType:'json',
              contentType:'text/json',
              async: false
            });
          }

        },
        error:function(data, status, jqXHR){
          displayToast('作業記録の保存に失敗しました', 4000, 'rounded');
        },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
    }
    return false;
  }
  function groupDisplay() {
    var group = $(this);
    if (groupinfo[group.attr("fieldGroupId")]) {
      $(".g-" + group.attr("fieldGroupId")).hide();
      groupinfo[group.attr("fieldGroupId")] = false;
    }
    else {
      $(".g-" + group.attr("fieldGroupId")).show();
      groupinfo[group.attr("fieldGroupId")] = true;
    }
  }

  var oFieldInfo = {fieldId: 0};

  function fieldDisplay() {
    var field = $(this);
    var inputJson = {"field": 0};

    inputJson.field = field.attr("key");
    oFieldInfo.fieldId = inputJson.field;

    $.ajax({
      url:"/getFieldDetail",
      type:'POST',
      data:JSON.stringify(inputJson),               //入力用JSONデータ
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

        var fa = $("#fieldarea");
        fa.empty();

        fa.append('<div id="fielddetail" class="field-detail"></div>');

        var fd = $("#fielddetail");
        var fdj = jsonResult.fieldJson;

        //圃場情報
        fd.append('<div class="sub-menu"><i class="material-icons small left" id="fielddetailback">arrow_back</i><span class="title">圃場情報</span></div>');
        fd.append('<div class="card-panel detail-panel-color" id="fielddetail-1"></div>');
        var f1 = $("#fielddetail-1");

        f1.append('<span class="item-title">圃場名</span><span class="item">' + fdj["fieldName"] + '</span>');
        f1.append('</br><span class="item-title">郵便番号</span><span class="item">' + fdj["post"] + '</span>');
        f1.append('</br><span class="item-title">面積</span><span class="item">' + fdj["area"] + '&nbsp;' + fdj["areaUnit"] + '</span>');
        f1.append('</br></br><span class="item-title">直近の天気</span>');
        f1.append('<hr>');
        f1.append('<div class="row" id="todayweather"></div>');
        var tw = $("#todayweather");

        var wlj = fdj.weatherlist;
        for (var key in wlj) {
          var w = wlj[key];
          tw.append('<div class="col s2 center weather"><span class="date">' + w["date"] + '</span></br><span class="time">' + w["time"] + '</span></br><img class="icon" src="http://openweathermap.org/img/w/' + w["icon"] + '.png">' + '</br><span class="rain">' + w["rain"] + '</span><span class="unit">mm</span></div>');
        }
        f1.append('</br></br><span class="item-title">今後の天気</span>');
        f1.append('<hr>');
        f1.append('<div class="row" id="weekweather"></div>');
        var ww = $("#weekweather");

        var wdj = fdj.weatherlistd;
        for (var key in wdj) {
          var w = wdj[key];
          ww.append('<div class="col s2 center weather"><span class="date">' + w["date"] + '</span></br><span class="time">' + w["time"] + '</span></br><img class="icon" src="http://openweathermap.org/img/w/' + w["icon"] + '.png">' + '</br><span class="rain">' + w["rain"] + '</span><span class="unit">mm</span></div>');
        }

        fd.append('<span class="item-title">所属区画</span>');
        fd.append('<ul id="kukakulist" class="collection" style="border: 0px;">');
        var ul = $('#kukakulist');
        var kukakuList = jsonResult.datalist;
        for ( var kukakuKey in kukakuList ) {                     //対象圃場情報分処理を行う

          var kukakuStatus = kukakuList[kukakuKey];
          var key = kukakuKey.split(".");

          var code = fdj["fieldColor"];
          var red   = parseInt(code.substring(0,2), 16);
          var green = parseInt(code.substring(2,4), 16);
          var blue  = parseInt(code.substring(4,6), 16);

          ul.append('<li id="item-k' + key[0] + '" class="collection-item field-item" style="border-bottom: 0px;border-left: 8px solid #' + fdj["fieldColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJO + ');" key="' + kukakuKey +'"><span class="field">' + kukakuStatus["kukakuName"] + '</span></li>');
          var li = $('#item-k' + key[0]);
          var chain = '<div class="chain">';
          var chaindata = kukakuStatus["chain"];
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
          var hinsyu = "";
//          if (kukakuStatus["hinsyuName"] != "") {
//            hinsyu = " ( " + kukakuStatus["hinsyuName"] + " )"
//          }
          li.append(chain);
          li.append('<ul id="item' + key[0] + '-wrapper" class="item-div-wrapper"></ul>');
          var wrapper = $("#item" + key[0] + "-wrapper");
          wrapper.append('<li id="item' + key[0] + '-itemarea1" class="item-div-area" key=' + key[0] + '></li>');
          var itemarea = $("#item" + key[0] + "-itemarea1");
          itemarea.append('<div id="item' + key[0] + '-item1" class="item-div-item valign-wrapper"></div>');
          var div = $("#item" + key[0] + "-item1");
          div.append('<span class="sub">' + kukakuStatus["workYear"] + '年</span>');
          div.append('<span class="item">' + kukakuStatus["rotationSpeedOfYear"] + '作目</span>');
          div.append('<span class="sub">' + kukakuStatus["cropName"] + '</span>');
          div.append('<span class="item">' + kukakuStatus["hinsyuName"] + '</span>');
          itemarea.append('<div id="item' + key[0] + '-item2" class="item-div-item valign-wrapper"></div>');
          div = $("#item" + key[0] + "-item2");
          div.append('<img class="icon"  src="/assets/svg/field-icon-hasyu.svg">');
          div.append('<span class="item top">' + ConvertNullDate(kukakuStatus["hashuDate"]) + '<span class="sub">' + kukakuStatus["hashuCount"] + '&nbsp;回</span></span>');
          div.append('<span class="item bottom">' + kukakuStatus["seiikuDayCount"] + '日&nbsp;～&nbsp;' + kukakuStatus["seiikuDayCountEnd"] + '日</span>');
          itemarea.append('<div id="item' + key[0] + '-item3" class="item-div-item valign-wrapper"></div>');
          div = $("#item" + key[0] + "-item3");
          div.append('<img class="icon"  src="/assets/svg/field-icon-shukaku.svg">');
          div.append('<span class="item top">' + ConvertNullDate(kukakuStatus["totalShukakuNumber"]) + '<span class="sub">Kg</span></span>');
          div.append('<span class="item bottom">' + ConvertNullDate(kukakuStatus["shukakuStartDate"]) + '<span class="sub">&nbsp;～&nbsp;</span>' + ConvertNullDate(kukakuStatus["shukakuEndDate"]) + '</span>');
          wrapper.append('<li id="item' + key[0] + '-itemarea2" class="item-div-area"></li>');
          itemarea = $("#item" + key[0] + "-itemarea2");
          itemarea.append('<div id="item' + key[0] + '-item4" class="item-div-item valign-wrapper"></div>');
          div = $("#item" + key[0] + "-item4");
          div.append('<img class="icon"  src="/assets/svg/field-icon-hiryo.svg">');
          div.append('<span class="item top">' + ConvertNullDate(kukakuStatus["totalTuihiNumber"]) + '<span class="sub">Kg</span></span>');
          div.append('<span class="item bottom">' + ConvertNullDate(kukakuStatus["finalTuihiDate"]) + '<span class="sub">' + kukakuStatus["tuihiCount"] + '&nbsp;日経過</span></span>');
          itemarea.append('<div id="item' + key[0] + '-item5" class="item-div-item valign-wrapper"></div>');
          div = $("#item" + key[0] + "-item5");
          div.append('<img class="icon"  src="/assets/svg/field-icon-kansui.svg">');
          div.append('<span class="item top">' + ConvertNullDate(kukakuStatus["totalKansuiNumber"]) + '<span class="sub">L</span></span>');
          div.append('<span class="item bottom">' + ConvertNullDate(kukakuStatus["finalKansuiDate"]) + '<span class="sub">' + kukakuStatus["kansuiCount"] + '&nbsp;日経過</span></span>');
          itemarea.append('<div id="item' + key[0] + '-item6" class="item-div-item valign-wrapper"></div>');
          div = $("#item" + key[0] + "-item6");
          div.append('<img class="icon"  src="/assets/svg/field-icon-syodoku.svg">');
          div.append('<span class="item top">' + ConvertNullDate(kukakuStatus["totalDisinfectionNumber"]) + '<span class="sub">L</span></span>');
          div.append('<span class="item bottom">' + ConvertNullDate(kukakuStatus["finalDisinfectionDate"]) + '<span class="sub">' + kukakuStatus["disinfectionCount"] + '&nbsp;日経過</span></span>');
          wrapper.append('<li id="item' + key[0] + '-itemarea3" class="item-div-area"></li>');
          itemarea = $("#item" + key[0] + "-itemarea3");
          itemarea.append('<div id="item' + key[0] + '-item7" class="item-div-item valign-wrapper"></div>');
          div = $("#item" + key[0] + "-item7");
          div.append('<img class="icon"  src="/assets/svg/field-icon-sun.svg">');
          div.append('<span class="item">' + ConvertNullDate(kukakuStatus["totalSolarRadiation"]) + '</span>');
          div.append('<span class="sub">℃</span>');
          div.append('<span class="sub">' + kukakuStatus["yosokuSolarRadiation"] + '&nbsp;℃</span>');
          itemarea.append('<div id="item' + key[0] + '-item8" class="item-div-item valign-wrapper"></div>');
          div = $("#item" + key[0] + "-item8");
          div.append('<img class="icon"  src="/assets/svg/field-icon-rain.svg">');
          div.append('<span class="item">' + ConvertNullDate(kukakuStatus["rain"]) + '</span>');
          div.append('<span class="sub">mm</span>');

        }
        $("#fielddetailback").unbind("click");
        $("#fielddetailback").bind("click", fielddetailBack);

        $("#kukakulist li").unbind("click");
        $("#kukakulist li").bind("click", kukakuDisplay);

        var fa = $('#fieldarea');
        var ms = $("#mainsection");
        if (ms != undefined) {
          ms.fadeOut(0);
        }
        if (fa != undefined) {
          fa.fadeIn(500);
        }

        if  (localStorage.getItem("backMode") == "3") {
          $("#item-k" + localStorage.getItem("backKukakuId")).click();
        }

        if ((jsonResult.compartmentStatusSkip == 1 )
            && jsonResult.compartmentCount == 1) {
          $("#kukakulist li").click();
        }

    },
      dataType:'json',
      contentType:'text/json'
    });

  }
  function fielddetailBack() {
    var fa = $('#fieldarea');
    var ms = $("#mainsection");
    if (fa != undefined) {
      fa.fadeOut(0);
    }
    if (ms != undefined) {
      ms.fadeIn(500);
    }
  }

  var oKukakuInfo = {kukakuId: 0, cropId: 0, cropName:"", workchainId: 0, workchainName:""};

  //---------------------------------------------------------------------------
  //- 区画生産物選択
  //---------------------------------------------------------------------------
  function selectKukakuCrop() {

    if (oKukakuInfo.cropId != $("#G0002KukakuCropHidden").val()) {
      if (window.confirm('品目を変更します。\nよろしいですか？')) {
        var inputJson = {kukakuId: 0, cropId:0};
        inputJson.kukakuId  = oKukakuInfo.kukakuId;
        inputJson.cropId    = $("#G0002KukakuCropHidden").val();
        $.ajax({
          url:"/selectKukakuCrop",
          type:'POST',
          data:JSON.stringify(inputJson),               //入力用JSONデータ
          complete:function(data, status, jqXHR){           //処理成功時
            var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
            oKukakuInfo.cropId   = jsonResult["cropId"];
            oKukakuInfo.cropName = jsonResult["cropName"];
            oKukakuInfo.workchainId   = jsonResult["workChainId"];
            oKukakuInfo.workchainName = jsonResult["workChainName"];
            $("#G0002KukakuCropHidden").val(oKukakuInfo.cropId);
            $("#G0002KukakuCrop").text(oKukakuInfo.cropName);
            $("#G0002KukakuChainHidden").val(oKukakuInfo.workchainId);
            $("#G0002KukakuChain").text(oKukakuInfo.workchainName);
        },
          dataType:'json',
          contentType:'text/json'
        });
      }
      else {
        $("#G0002KukakuCropHidden").val(oKukakuInfo.cropId);
        $("#G0002KukakuCrop").text(oKukakuInfo.cropName);
      }
    }
  }
  //---------------------------------------------------------------------------
  //- 区画ワークチェーンを選択
  //---------------------------------------------------------------------------
  function selectKukakuWorkChain() {

    if (oKukakuInfo.workchainId != $("#G0002KukakuChainHidden").val()) {
      if (window.confirm('ワークチェーンを変更すると\n区画の作業進捗が初期化されますが\nよろしいですか？')) {
        var inputJson = {kukakuId: 0, workchainId:0};
        inputJson.kukakuId = oKukakuInfo.kukakuId;
        inputJson.workchainId = $("#G0002KukakuChainHidden").val();
        $.ajax({
          url:"/selectKukakuWorkChain",
          type:'POST',
          data:JSON.stringify(inputJson),               //入力用JSONデータ
          complete:function(data, status, jqXHR){           //処理成功時
            var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
            oKukakuInfo.workchainId   = jsonResult["workChainId"];
            oKukakuInfo.workchainName = jsonResult["workChainName"];
            $("#G0002KukakuChainHidden").val(oKukakuInfo.workchainId);
            $("#G0002KukakuChain").text(oKukakuInfo.workchainName);
        },
          dataType:'json',
          contentType:'text/json'
        });
      }
      else {
        $("#G0002KukakuChainHidden").val(oKukakuInfo.workchainId);
        $("#G0002KukakuChain").text(oKukakuInfo.workchainName);
      }
    }
  }
  function motochoSelect() {
    var inputJson = {"kukaku": 0, "year": 0, "rotation": 0};
    if ($("#G0002MotochoHistryHidden").val() == "") {
      return false;
    }
    var kukaku    = parseFloat($("#G0002MotochoHistryHidden").val().substr(0,8));
    var year      = parseInt($("#G0002MotochoHistryHidden").val().substr(8,4));
    var rotation  = parseInt($("#G0002MotochoHistryHidden").val().substr(12));

    inputJson.kukaku    = kukaku;
    inputJson.year      = year;
    inputJson.rotation  = rotation;

    $.ajax({
      url:"/getKukakuDetail",
      type:'POST',
      data:JSON.stringify(inputJson),               //入力用JSONデータ
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        kukakuDisplayEdit(jsonResult);
    },
      dataType:'json',
      contentType:'text/json'
    });

  }
  var areacontrol = {nouyaku: false, hiryou: false, total: false, timeline: false};
  function kukakuDisplayEdit(jsonResult) {
    var ka = $("#kukakuarea");
    ka.empty();

    ka.append('<div id="kukakudetail" class="kukaku-detail"></div>');

    var kd = $("#kukakudetail");
    var kdj = jsonResult.kukakuJson;

    kd.append('<div class="sub-menu"><i class="material-icons small left" id="kukakudetailback">arrow_back</i><span class="title">区画情報</span></div>');
    //------------------------------------------------------------------------------------------------------------------
    //- 区画可変情報の設定
    //------------------------------------------------------------------------------------------------------------------
    oKukakuInfo.kukakuId      = kdj["kukakuId"];
    oKukakuInfo.cropId        = kdj["cropId"];
    oKukakuInfo.cropName      = kdj["cropName"];
    oKukakuInfo.workchainId   = kdj["workChainId"];
    oKukakuInfo.workchainName = kdj["workChainName"];
    kd.append('<div class="card-panel detail-panel-color" id="kukakudetail-s"></div>');
    var ks = $("#kukakudetail-s");
    ks.append('<div class="row">');
    ks.append('<div class="col s12">');
    ks.append('<span class="selectmodal-trigger-title">品目</span><a href="#selectmodal"  class="selectmodal-trigger" title="品目一覧" data="'+ userinfo.farm + '/getCrop" displayspan="#G0002KukakuCrop" htext="#G0002KukakuCropHidden"><span id="G0002KukakuCrop" class="blockquote-input">' + kdj["cropName"] + '</span></a>');
    ks.append('<input type="hidden" id="G0002KukakuCropHidden" value="' + kdj["cropId"] + '">');
    ks.append('</div>');
    ks.append('</div>');
    ks.append('<div class="row">');
    ks.append('<div class="col s12">');
    ks.append('<span class="selectmodal-trigger-title">ワークチェーン</span><a href="#selectmodal"  class="selectmodal-trigger" title="ワークチェーン選択" data="getWorkChainOfFarm" displayspan="#G0002KukakuChain" htext="#G0002KukakuChainHidden"><span id="G0002KukakuChain" class="blockquote-input">' + kdj["workChainName"] + '</span></a>');
    ks.append('<input type="hidden" id="G0002KukakuChainHidden" value="' + kdj["workChainId"] + '">');
    ks.append('</div>');
    ks.append('</div>');
    $('#G0002KukakuCropHidden').unbind('change');
    $('#G0002KukakuCropHidden').bind('change', selectKukakuCrop);
    $('#G0002KukakuChainHidden').unbind('change');
    $('#G0002KukakuChainHidden').bind('change', selectKukakuWorkChain);

    //------------------------------------------------------------------------------------------------------------------
    //- 作付情報
    //------------------------------------------------------------------------------------------------------------------
    kd.append('<div class="card-panel detail-panel-color" id="motochohistry-s"></div>');
    var ms = $("#motochohistry-s");
    ms.append('<div class="row">');
    ms.append('<div class="col s12">');
    ms.append('<span class="selectmodal-trigger-title">作付回転数</span><a href="#selectmodal"  class="selectmodal-trigger" title="作付選択" data="' + oKukakuInfo.kukakuId + '/getMotochoHistry" displayspan="#G0002MotochoHistrySpan" htext="#G0002MotochoHistryHidden"><span id="G0002MotochoHistrySpan" class="blockquote-input">' + kdj["motochoname"] + '</span></a>');
    ms.append('<input type="hidden" id="G0002MotochoHistryHidden" value="' + kdj["motochoid"] + '">');
    ms.append('</div>');
    ms.append('</div>');
    selectDataGet("#G0002MotochoHistrySpan", oKukakuInfo.kukakuId + '/getMotochoHistry');
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);
    $('#G0002MotochoHistryHidden').unbind('change');
    $('#G0002MotochoHistryHidden').bind('change', motochoSelect);
    //------------------------------------------------------------------------------------------------------------------
    //- 区画状況の表示
    //------------------------------------------------------------------------------------------------------------------
    kd.append('<div class="card-panel detail-panel-color" id="kukakudetail-1"></div>');
    var k1 = $("#kukakudetail-1");

    //区画情報
    k1.append('<span class="item-title">区画名</span><span class="item">' + kdj["kukakuName"] + '</span>');
    k1.append('</br><span class="item-title">面積</span><span class="item">' + kdj["area"] + '&nbsp;' + kdj["areaUnit"] + '</span>');
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
    k1.append('</br><span class="item-title">年内回転数</span><span class="item">' + kdj["rotationSpeedOfYear"] + '作目</span>');
//----- V02R291 MOD START -----
//  k1.append('</br><span class="item-title">品目</span><span class="item">' + kdj["cropName"] + hinsyu + '</span>');
    k1.append('</br><span class="item-title">品目</span><span class="item">' + kdj["cropNameD"] + hinsyu + '</span>');
//----- V02R291 MOD END -----
    k1.append('</br><span class="item-title">播種日</span><span class="item">' + kdj["hashuDate"] + '</span>');
    k1.append('<span class="sub">播種回数</span><span class="item">' + kdj["hashuCount"] + '&nbsp;回</span>');
    k1.append('</br><span class="item-title">生育日数</span><span class="item">' + kdj["seiikuDayCount"] + '日</span><span class="item">～&nbsp;&nbsp;' + kdj["seiikuDayCountEnd"] + '日</span>');
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
    //積算温度
    //---------------------------------------------------------------------------------------------------------------------------------
    tga.append('</br><span class="item-title">積算温度</span><span class="item">' + kdj["totalSolarRadiation"] + '&nbsp;℃</span>');
    if (kdj["yosokuSolarDate"] != "") {
      tga.append('<span class="sub">' + kdj["yosokuSolarDate"] + 'の予想積算温度</span><span class="item">' + kdj["yosokuSolarRadiation"] + '&nbsp;℃</span>');
    }
    makeGraph(tga, 'tSolarRadiation', 'mix', kdj["solarRadiationLabel"], '平均気温', kdj["solarRadiationData"], 'rgba(255, 99, 132 , 0.2)', 'rgba(255, 99, 13 , 0.4)', '積算温度', kdj["solarRadiationMixData"], 'rgba(255, 99, 13 , 0)', 'rgba(255, 99, 13 , 1)');
    //---------------------------------------------------------------------------------------------------------------------------------
    //積算降水量
    //---------------------------------------------------------------------------------------------------------------------------------
    tga.append('</br><span class="item-title">積算降水量</span><span class="item">' + kdj["rain"] + '&nbsp;mm</span>');
    makeGraph(tga, 'tRain', 'mix', kdj["rainLabel"], '降水量', kdj["rainData"], 'rgba(83, 109, 354 , 0.2)', 'rgba(83, 109, 354 , 0.4)', '積算降水量', kdj["rainMixData"], 'rgba(83, 109, 354 , 0)', 'rgba(83, 109, 354 , 1)');
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
    //収穫
    //---------------------------------------------------------------------------------------------------------------------------------
    tga.append('</br><span class="item-title">収穫</span><span class="item">' + kdj["totalShukakuNumber"] + 'Kg</span><span class="item">（' + kdj["tanshu"] + 'Kg/10a）</span>');
    if (kdj["shukakuStartDate"] != "") {
      tga.append('</br><span class="item-title">期間</span><span class="item">' + kdj["shukakuStartDate"] + '</span><span class="item">～' + kdj["shukakuEndDate"] + '</span>');
    }
    //
    makeGraph(tga, 'tShukaku', 'mix', kdj["shukakuLabel"], '収穫量', kdj["shukakuData"], 'rgba(230, 74, 25 , 0.2)', 'rgba(230, 74, 25 , 0.4)', '合計収穫量', kdj["shukakuMixData"], 'rgba(230, 74, 25 , 0)', 'rgba(230, 74, 25 , 1)');
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
    var timelineString = timelineDisplay(jsonResult)
    tla.append(timelineString);
    $("#timelinearea .timelinelist").removeClass("kani");
    $(".timelinelist").addClass("motocho");
    $(".timelinelist").unbind("click");
    $(".timelinelist").bind("click", workDiaryEdit);
    //---------------------------------------------------------------------------------------------------------------------------------
    //タイムラインアコーディンの制御
    //---------------------------------------------------------------------------------------------------------------------------------
    tla.hide();
    areacontrol.timeline = false;
    $("#timelinebar").unbind("click");
    $("#timelinebar").bind("click", timelinebarClick);

    $("#kukakudetailback").unbind("click");
    $("#kukakudetailback").bind("click", kukakudetailBack);
    var fa = $('#fieldarea');
    var ka = $("#kukakuarea");
    if (fa != undefined) {
      fa.fadeOut(0);
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
    selectDataGet("#workReorder", oKukakuInfo.kukakuId + "/getWorkOfKukaku");
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
      if (localStorage.getItem("backWorkDiaryId") != "null") {
        $("#timelinebar").click();
      }

      localStorage.setItem("backMode"         , null);
      localStorage.setItem("backFieldId"      , null);
      localStorage.setItem("backKukakuId"     , null);
      localStorage.setItem("backWorkId"       , null);
      localStorage.setItem("backWorkDiaryId"  , null);
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
        localStorage.setItem("backFieldId"      , oFieldInfo.fieldId);
        localStorage.setItem("backKukakuId"     , kukaku);
        localStorage.setItem("backWorkId"       , work);
        localStorage.setItem("backWorkDiaryId"  , null);
        window.location.href = '/' + work + '/' + kukaku + '/workDiaryMove';
        return false;
      }
    }
  }
  function kukakuDisplay() {
    var kukaku = $(this);
    var inputJson = {"kukaku": 0, "year": 0, "rotation": 0};

    inputJson.kukaku = kukaku.attr("key");

    $.ajax({
      url:"/getKukakuDetail",
      type:'POST',
      data:JSON.stringify(inputJson),               //入力用JSONデータ
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        kukakuDisplayEdit(jsonResult);
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

  function kukakudetailBack() {
    var fa = $('#fieldarea');
    var ka = $("#kukakuarea");
    //元帳照会用タイムライン簡易切替ボタンを表示する
    $("#mtimeline-close").hide();
    $("#mtimeline-open").hide();
    $("#menu-close").show();
    $("#menu-open").show();
    if (ka != undefined) {
      ka.fadeOut(0);
    }
    if (fa != undefined) {
      fa.fadeIn(500);
    }
  }

  //------------------
  // 作業情報
  //------------------
  var workinfo = {open: false, data:{}};
  function GetWork() {

      $.ajax({
        url:"/getWork",												//メニュー初期処理
        type:'GET',
        complete:function(data, status, jqXHR){						//処理成功時
            var jsonResult 	= JSON.parse( data.responseText );			//戻り値用JSONデータの生成
            var workList 		= jsonResult.targetWork;				//作業対象リスト
            if (jsonResult.result == "REDIRECT") {
              window.location.href = '/move';
              return;
            }

            workinfo.data = workList;

            //----- ワークチェインの生成----
            var wkcarea = $("#G0002WorkChainArea")
            var htmlString = "";

            wkcarea.empty();

            htmlString  = '<div class="row">';
            htmlString += '<div class="col s12 sg">';
            htmlString += '<div class="title"><span class="mselectmodal-trigger-title">ワークチェーン</span><a href="#selectmodal"  class="selectmodal-trigger" title="ワークチェーン選択" data="getWorkChainOfFarm" displayspan="#G0002WorkChainSpan" htext="#G0002WorkChainHidden"><span id="G0002WorkChainSpan" class="blockquote-input">' + jsonResult.workchainname + '</span></a></div>';
            htmlString += '<input type="hidden" id="G0002WorkChainHidden" value="' + jsonResult.workchainid + '">';
            htmlString += '</div>';
            htmlString += '</div>';

            wkcarea.html(htmlString);

            $('.selectmodal-trigger').unbind('click');
            $('.selectmodal-trigger').bind('click', selectOpen);
            $('#G0002WorkChainHidden').unbind("change");
            $('#G0002WorkChainHidden').bind("change", changeWorkChain);

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
              if (work["workKukakuCount"] == 0) {

              }
              else if (work["workKukakuCount"] > 50) {
                  htmlString += '<span class="work-badge">+50</span>';
                  htmlString += '<span class="work-badge-title">対象区画：</span>';
              }
              else {
                  htmlString += '<span class="work-badge">' + work["workKukakuCount"] + '</span>';
                  htmlString += '<span class="work-badge-title">対象区画：</span>';
              }
              htmlString += '<span class="work-sub-text">' + work["workEnglish"] + '</span>';
              htmlString += '</div>';
              htmlString += '</div>';

              htmlString += '</div>';

            } // workList

            //作業記録日誌画面に遷移する為のフォームを埋め込む
            htmlString += '<form id="G0002Form" action="./workDiaryMove" method="GET" ><input type="hidden" id="G0002WorkId" name="workId"><input type="hidden" id="G0002kukakuId" name="kukakuId"></form>';

            $("#G0002WorkList").html(htmlString);					//可変HTML部分に反映する

            $('.work-card').unbind("click");
            $('.work-card').bind("click", WorkKukakuDisplay);

            if (localStorage.getItem("backMode") == "4") {
              //lockScreen(LOCK_ID);
              if (!(localStorage.getItem("backWorkId") == undefined || localStorage.getItem("backWorkId") == "null")) {
                var sKey = ("" + localStorage.getItem("backWorkId")).split(".");
                $("#work-" + sKey[0]).click();
              }
              else {
                //unlockScreen(LOCK_ID);
              }
            }

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
  var selectWork;
  function WorkKukakuDisplayExcute(workid, lat, lng) {

    var url = "/getKukakuOfWorkJson";

    var jsondata = {workId:workid, pLat:lat, pLng:lng};

    $("#G0002WorkList").hide();
    $("#G0002KukakuList").empty();
    $("#G0002KukakuList").append('<div class="progress"><div class="indeterminate"></div></div>');
    $("#G0002KukakuList").show();

    $.ajax({
      url: url,                                         //メニュー初期処理
      data:JSON.stringify(jsondata),                //入力用JSONデータ
      type:'POST',
      complete:function(data, status, jqXHR){           //処理成功時

          var jsonResult  = JSON.parse( data.responseText );      //戻り値用JSONデータの生成

          var work = selectWork;

          var kl = $('#G0002KukakuList');
          if (kl != null) {

            kl.empty();

            //----- IDが被るエリアを削除する -----
            var fa = $("#fieldarea");
            fa.empty();
            var ka = $("#kukakuarea");
            ka.empty();
            var kd = $("#kukakudetail");
            kd.empty();

            kl.append('<div class="col s12" style="padding: 0;"><a id="workback"><i class="material-icons">arrow_back</i></a></div>');
            kl.append('<div id="workkukakuarea" class="col s12" style="padding: 0;"></div>');
            var ka = $('#workkukakuarea');
            ka.append('<ul id="workkukakulist" class="collection" style="border: 0px;">');
            var ul = $('#workkukakulist');

            if (ul != null) {
              var wi = jsonResult.data;

              if (wi != null) {

                var aryJson = [];
                for (var keys in wi.workKukakuList) {
                  var item = wi.workKukakuList[keys];
                  aryJson.push(item);
                }

                aryJson.sort(workKukakuCompare);

                var oldGroupId = 0;
                var oldFieldId = 0;
                for (var keys in aryJson) {
                  var item = aryJson[keys];

                  var code = item["fieldGroupColor"];
                  var red   = parseInt(code.substring(0,2), 16);
                  var green = parseInt(code.substring(2,4), 16);
                  var blue  = parseInt(code.substring(4,6), 16);

                  if (oldGroupId != item["fieldGroupId"]) {
                    if (oldGroupId != 0) {
                      wgroupinfo[oldGroupId] = false;
                      $(".wg-" + oldGroupId).hide();
                    }
                    ul.append('<li class="collection-item wgroup-item" style="border-bottom: 0px;border-left: 8px solid #' + item["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJOG + ');" fieldGroupId="' + item["fieldGroupId"] +'"><span class="field">' + item["fieldGroupName"] + 'グループ</span></li>');
                  }
                  oldGroupId = item["fieldGroupId"];

                  var color = COLOR_WORKNAME;
                  if (item["target"] == "1") {
                    color = "#d32f2f";
                  }

                  if (jsonResult.workTargetDisplay == 1) { //圃場の場合
                    if (oldFieldId != item["fieldId"]) {
                      if (oldFieldId != 0) {
                        wfieldinfo[oldFieldId] = false;
                        $(".wf-" + oldFieldId).hide();
                      }
                      ul.append('<li id="wfield' + item["fieldId"] + '" workid="' + work.attr("workid") + '.0" workTemplateId="' + work.attr("workTemplateId") + '" fieldGroupId="' + item["fieldGroupId"] +'" fieldId="' + item["fieldId"] + '" rot="' + item["rotationSpeedOfYear"] + '" class="collection-item field-item wfield-item wg-' + item["fieldGroupId"] +'" style="border-bottom: 0px;border-left: 8px solid #' + item["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJO + ');" key="' + keys +'"><span class="field" style="color: ' + color + ';">' + item["fieldName"] + '</span></li>');
                    }
                    ul.append('<li id="witem' + item["kukakuId"] + '" workid="' + work.attr("workid") + '.0" workTemplateId="' + work.attr("workTemplateId") + '" fieldGroupId="' + item["fieldGroupId"] +'" kukakuid="' + item["kukakuId"] + '" fieldId="' + item["fieldId"] + '" rot="' + item["rotationSpeedOfYear"] + '" class="collection-item field-item working-trriger wf-' + item["fieldId"] + '" style="border-bottom: 0px;border-left: 8px solid #' + item["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJO + ');" key="' + keys +'"><span class="field" style="color: ' + color + ';">' + item["kukakuName"] + '</span><span class="kukaku" style="color: ' + color + ';">(' + item["fieldName"] + ')</span></li>');
                  }
                  else {
                    ul.append('<li id="witem' + item["kukakuId"] + '" workid="' + work.attr("workid") + '.0" workTemplateId="' + work.attr("workTemplateId") + '" fieldGroupId="' + item["fieldGroupId"] +'" kukakuid="' + item["kukakuId"] + '" fieldId="' + item["fieldId"] + '" rot="' + item["rotationSpeedOfYear"] + '" class="collection-item field-item working-trriger wg-' + item["fieldGroupId"] +' wf-' + item["fieldId"] + '" style="border-bottom: 0px;border-left: 8px solid #' + item["fieldGroupColor"] +'; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_HOJO + ');" key="' + keys +'"><span class="field" style="color: ' + color + ';">' + item["kukakuName"] + '</span><span class="kukaku" style="color: ' + color + ';">(' + item["fieldName"] + ')</span></li>');
                  }

                  oldFieldId = item["fieldId"];

                  var li = $('#witem' + item["kukakuId"]);
                  //----- ワークチェーン -----
                  var chain = '<div class="chain">';
                  if (jsonResult.displayChain != 99) {
                    var chaindata = item["chain"];
                    for (var ckey in chaindata) {
                      var data = chaindata[ckey];
                      if ((jsonResult.displayChain == 1) && (data.chain == 0)) {
                        continue;
                      }
                      if (data.end == 1) {
                        chain += '<span class="chainitem" style="background-color: #' + data.color +';">' + data.name + '</span>';
                      }
                      else {
                        chain += '<span class="chainitem">' + data.name + '</span>';
                      }
                    }
                  }
                  chain += '</div>';
                  li.append(chain);

                  var key = (item["kukakuId"] + ".0").split(".");
                  li.append('<ul id="witem' + key[0] + '-wrapper" class="item-div-wrapper"></ul>');
                  var wrapper = $("#witem" + key[0] + "-wrapper");
                  wrapper.append('<li id="witem' + key[0] + '-itemarea1" class="item-div-area"></li>');
                  var itemarea = $("#witem" + key[0] + "-itemarea1");
                  itemarea.append('<div id="witem' + key[0] + '-item1" class="item-div-item valign-wrapper"></div>');
                  var div = $("#witem" + key[0] + "-item1");
                  div.append('<span class="sub">' + item["workYear"] + '年</span>');
                  div.append('<span class="item">' + item["rotationSpeedOfYear"] + '作目</span>');
                  div.append('<span class="sub">' + item["cropName"] + '</span>');
                  div.append('<span class="item">' + item["hinsyuName"] + '</span>');
                  itemarea.append('<div id="witem' + key[0] + '-item2" class="item-div-item valign-wrapper"></div>');
                  div = $("#witem" + key[0] + "-item2");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-hasyu.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(item["hashuDate"]) + '<span class="sub">' + item["hashuCount"] + '&nbsp;回</span></span>');
                  div.append('<span class="item bottom">' + item["seiikuDayCount"] + '日<span class="sub">&nbsp;～&nbsp;</span>' + item["seiikuDayCountEnd"] + '日</span>');
                  itemarea.append('<div id="witem' + key[0] + '-item3" class="item-div-item valign-wrapper"></div>');
                  div = $("#witem" + key[0] + "-item3");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-shukaku.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(item["totalShukakuNumber"]) + '<span class="sub">Kg</span></span>');
                  div.append('<span class="item bottom">' + ConvertNullDate(item["shukakuStartDate"]) + '<span class="sub">&nbsp;～&nbsp</span>' + ConvertNullDate(item["shukakuEndDate"]) + '</span>');
                  wrapper.append('<li id="witem' + key[0] + '-itemarea2" class="item-div-area"></li>');
                  itemarea = $("#witem" + key[0] + "-itemarea2");
                  itemarea.append('<div id="witem' + key[0] + '-item4" class="item-div-item valign-wrapper"></div>');
                  div = $("#witem" + key[0] + "-item4");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-hiryo.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(item["totalTuihiNumber"]) + '<span class="sub">Kg</span></span>');
                  div.append('<span class="item bottom">' + ConvertNullDate(item["finalTuihiDate"]) + '<span class="sub">' + item["tuihiCount"] + '&nbsp;日経過</span></span>');
                  itemarea.append('<div id="witem' + key[0] + '-item5" class="item-div-item valign-wrapper"></div>');
                  div = $("#witem" + key[0] + "-item5");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-kansui.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(item["totalKansuiNumber"]) + '<span class="sub">L</span></span>');
                  div.append('<span class="item bottom">' + ConvertNullDate(item["finalKansuiDate"]) + '<span class="sub">' + item["kansuiCount"] + '&nbsp;日経過</span></span>');
                  itemarea.append('<div id="witem' + key[0] + '-item6" class="item-div-item valign-wrapper"></div>');
                  div = $("#witem" + key[0] + "-item6");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-syodoku.svg">');
                  div.append('<span class="item top">' + ConvertNullDate(item["totalDisinfectionNumber"]) + '<span class="sub">L</span></span>');
                  div.append('<span class="item bottom">' + ConvertNullDate(item["finalDisinfectionDate"]) + '<span class="sub">' + item["disinfectionCount"] + '&nbsp;日経過</span></span>');
                  wrapper.append('<li id="witem' + key[0] + '-itemarea3" class="item-div-area"></li>');
                  itemarea = $("#witem" + key[0] + "-itemarea3");
                  itemarea.append('<div id="witem' + key[0] + '-item7" class="item-div-item valign-wrapper"></div>');
                  div = $("#witem" + key[0] + "-item7");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-sun.svg">');
                  div.append('<span class="item">' + ConvertNullDate(item["totalSolarRadiation"]) + '</span>');
                  div.append('<span class="sub">℃</span>');
                  div.append('<span class="sub">' + item["yosokuSolarRadiation"] + '&nbsp;℃</span>');
                  itemarea.append('<div id="witem' + key[0] + '-item8" class="item-div-item valign-wrapper"></div>');
                  div = $("#witem" + key[0] + "-item8");
                  div.append('<img class="icon"  src="/assets/svg/field-icon-rain.svg">');
                  div.append('<span class="item">' + ConvertNullDate(item["rain"]) + '</span>');
                  div.append('<span class="sub">mm</span>');


//                  li.append('<span class="item-title rotation">年内回転数</span><span class="item">' + item["rotationSpeedOfYear"] + '作目</span>');
//                  li.append('</br><span class="item-title crop">生産物</span><span class="item">' + item["cropName"] + '</span>');
//                  li.append('</br><span class="item-title hinsyu">品　種</span><span class="item">' + item["hinsyuName"] + '</span>');
//                  li.append('</br><span class="item-title hashu">播種日</span><span class="item">' + ConvertNullDate(item["hashuDate"]) + '</span>');
//                  li.append('<span class="sub">播種回数</span><span class="item">' + item["hashuCount"] + '&nbsp;回</span>');
//                  li.append('</br><span class="item-title seiiku">生育日数</span><span class="item">' + item["seiikuDayCount"] + '日</span><span class="item">～&nbsp;&nbsp;' + item["seiikuDayCountEnd"] + '日</span>');
//                  li.append('</br><span class="item-title sekisan">積算温度</span><span class="item">' + item["totalSolarRadiation"] + '℃</span>');
//                  li.append('</br><span class="item-title rain">積算降水量</span><span class="item">' + item["rain"] + '℃</span>');
//                  li.append('</br><span class="item-title syodoku">消毒</span><span class="item">' + item["totalDisinfectionNumber"] + 'L</span><span class="item">' + item["finalDisinfectionDate"] + '</span><span class="sub">' + item["disinfectionCount"] + '&nbsp;日経過</span>');
//                  li.append('</br><span class="item-title kansui">潅水量</span><span class="item">' + item["totalKansuiNumber"] + 'L</span><span class="item">' + item["finalKansuiDate"] + '</span><span class="sub">' + item["kansuiCount"] + '&nbsp;日経過</span>');
//                  li.append('</br><span class="item-title tuihi">追肥</span><span class="item">' + item["totalTuihiNumber"] + 'Kg</span><span class="item">' + item["finalTuihiDate"] + '</span><span class="sub">' + item["tuihiCount"] + '&nbsp;日経過</span>');
//                  li.append('</br><span class="item-title shukaku">収穫量</span><span class="item">' + item["totalShukakuNumber"] + 'Kg</span><span class="item">（' + item["tanshu"] + 'Kg/10a）</span>');
//                  li.append('</br><span class="item-title shukaku">収穫期間</span><span class="item">' + item["shukakuStartDate"] + '</span><span class="item">～&nbsp;&nbsp;' + item["shukakuEndDate"] + '</span>');
//                  li.append('</br><span class="item-title working">作業中</span>');
//                  var workings = item["working"];
//                  for ( var workingKey in workings ) {
//                    var working = workings[workingKey];
//                    li.append('</br><span class="item">' + working.name + '</span><span class="item" style="color: #' + working.workcolor + ';">' + working.workname + '</span><span class="sub">' + working.start + '&nbsp;～</span>');
//
//                  }
                }
                if (oldGroupId != 0) {
                  wgroupinfo[oldGroupId] = false;
                  $(".wg-" + oldGroupId).hide();
                }
                if (oldFieldId != 0) {
                  wfieldinfo[oldFieldId] = false;
                  $(".wf-" + oldFieldId).hide();
                }
              }
            }
            $("#workback").unbind("click");
            $("#workback").bind("click", WorkKukakuBack);
            $(".working-trriger").unbind("click");
            $(".working-trriger").bind("click", WorkingMove);
            $(".wgroup-item").unbind("click");
            $(".wgroup-item").bind("click", wgroupDisplay);
            if (jsonResult.workTargetDisplay == 1) { //圃場の場合
              $(".wfield-item").unbind("click");
              $(".wfield-item").bind("click", wfieldDisplay);
              if (localStorage.getItem("backMode") == "4") {
                $(".wf-" + localStorage.getItem("backFieldId")).show();
                wfieldinfo[localStorage.getItem("backFieldId")] = true;
              }
            }
            if (localStorage.getItem("backMode") == "4") {
              $(".wg-" + localStorage.getItem("backFieldGroupId")).show();
              wgroupinfo[localStorage.getItem("backFieldGroupId")] = true;
              $("#witem" + localStorage.getItem("backKukakuId"))[0].scrollIntoView(true);

              localStorage.setItem("backMode"         , null);
              localStorage.setItem("backWorkId"       , null);
              localStorage.setItem("backKukakuId"     , null);
              //unlockScreen(LOCK_ID);
            }
          }
      },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function geolocationSuccess(position) {
    WorkKukakuDisplayExcute(selectWork.attr("workid"), position.coords.latitude, position.coords.longitude);
  }
  function geolocationError(position) {
    WorkKukakuDisplayExcute(selectWork.attr("workid"), 0, 0);
  }
  function WorkKukakuDisplay() {

    selectWork = $(this);

    if (navigator.geolocation &&
        (userinfo.contractPlan == 0 ||
         userinfo.contractPlan >= 4)) {
      navigator.geolocation.getCurrentPosition(geolocationSuccess, geolocationError);
    }
    else {
      WorkKukakuDisplayExcute(selectWork.attr("workid"), 0, 0);
    }

  }
  function wgroupDisplay() {
    var group = $(this);
    if (wgroupinfo[group.attr("fieldGroupId")]) {
      var nodes = $(".wg-" + group.attr("fieldGroupId"));
	  for (var node = 0; node < nodes.length; node++) {
	    var field = $(nodes[node]);
	    $(".wf-" + field.attr("fieldId")).hide();
	    field.css("background-color", "rgba(0, 0, 0, 0)");
	    wfieldinfo[field.attr("fieldId")] = false;
	  }
      $(".wg-" + group.attr("fieldGroupId")).hide();
      wgroupinfo[group.attr("fieldGroupId")] = false;
      localStorage.setItem("backFieldGroupId", null);
    }
    else {
      $(".wg-" + group.attr("fieldGroupId")).show();
      wgroupinfo[group.attr("fieldGroupId")] = true;
      localStorage.setItem("backFieldGroupId", group.attr("fieldGroupId"));
    }
  }
  function wfieldDisplay() {
    var field = $(this);
    if (wfieldinfo[field.attr("fieldId")]) {
      $(".wf-" + field.attr("fieldId")).hide();
      field.css("background-color", "rgba(0, 0, 0, 0)");
      wfieldinfo[field.attr("fieldId")] = false;
    }
    else {
      $(".wf-" + field.attr("fieldId")).show();
      field.css("background-color", "#fff59d");
      wfieldinfo[field.attr("fieldId")] = true;
    }
  }
  //--------------------------------------------------
  // 作業対象区画戻る
  //--------------------------------------------------
  function WorkKukakuBack() {

      $("#G0002WorkList").show();
      $("#G0002KukakuList").hide();

  }

  function WorkingMove() {
    //----- 作業中チェック -----
    var inputJson = {"workid":"", "kukakuid":"", "action":""};
    var info = $(this);
    //----- 作付開始の場合、直接作業入力画面に遷移する
    if (info.attr("workTemplateId") == templateIdEnd) {
      localStorage.setItem("backMode"         , "4");
      localStorage.setItem("backWorkId"       , info.attr("workid"));
      localStorage.setItem("backKukakuId"     , info.attr("kukakuid"));
      localStorage.setItem("backFieldGroupId" , info.attr("fieldGroupId"));
      localStorage.setItem("backFieldId"      , info.attr("fieldId"));
      window.location.href = '/' + info.attr("workid") + '/' + info.attr("kukakuid") + '/workDiaryMove';
      return;
    }
    else {
      //----- 作付開始実施チェック -----
      if (info.attr("rot") == 0) {
        window.alert('作付開始が実施されていない区画です。\n先に作付開始を行ってください。');
        return;
      }
    }
    if (userinfo.work != 0) {
//        confarmOpen('作業中遷移確認', 'あなたは現在作業中です。\n作業中画面を表示しますか？', '作業中画面へ', '作業記録画面へ', '閉じる', 1, 2, 0);
//        alert(confarmInfo.returnCode);
//        return;
      var result = window.confirm('あなたは現在作業中です。\n作業中画面を表示しますか？');
      if (result) {
//      inputJson.action = "display";
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
          localStorage.setItem("backKukakuId"     , info.attr("kukakuid"));
          localStorage.setItem("backFieldGroupId" , info.attr("fieldGroupId"));
          localStorage.setItem("backFieldId"      , info.attr("fieldId"));
          window.location.href = '/' + info.attr("workid") + '/' + info.attr("kukakuid") + '/workDiaryMove';
          return;
        }
        else {
          return;
        }
      }
    }
    else {
//    作業中画面への遷移を廃止
//      inputJson.action = "init";
//      inputJson.workid = info.attr("workid");
//      inputJson.kukakuid = info.attr("kukakuid");
      localStorage.setItem("backMode"         , "4");
      localStorage.setItem("backWorkId"       , info.attr("workid"));
      localStorage.setItem("backKukakuId"     , info.attr("kukakuid"));
      localStorage.setItem("backFieldGroupId" , info.attr("fieldGroupId"));
      localStorage.setItem("backFieldId"      , info.attr("fieldId"));
      window.location.href = '/' + info.attr("workid") + '/' + info.attr("kukakuid") + '/workDiaryMove';
      return;
    }
//  作業中画面への遷移を廃止
//    $.ajax({
//      url:"/initparam",
//      type:'POST',
//      data:JSON.stringify(inputJson),               //入力用JSONデータ
//      complete:function(data, status, jqXHR){           //処理成功時
//        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
//        window.location.href = "/workingmove";
//    },
//      dataType:'json',
//      contentType:'text/json'
//    });
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
          htmlString += '<span class="mselectmodal-trigger-title">表示条件</span><a id="#G0002modalMessageKind" href="#selectmodal"  class="selectmodal-trigger" title="表示条件一覧" data="getMessageKind" displayspan="#G0002MessageKind" htext="#G0002MessageKindHidden"><span id="G0002MessageKind" class="blockquote-input">' + jsonResult.mkindn + '</span></a>';
          htmlString += '<input type="hidden" id="G0002MessageKindHidden" value="' + jsonResult.mkind + '">';
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
          $("#G0002MessageKindHidden").unbind("change");
          $("#G0002MessageKindHidden").bind("change", changeMessageKind);
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
          htmlString += '<span class="mselectmodal-trigger-title">表示条件</span><a id="#G0002modalMessageKind" href="#selectmodal"  class="selectmodal-trigger" title="表示条件一覧" data="getMessageKind" displayspan="#G0002MessageKind" htext="#G0002MessageKindHidden"><span id="G0002MessageKind" class="blockquote-input">' + jsonResult.mkindn + '</span></a>';
          htmlString += '<input type="hidden" id="G0002MessageKindHidden" value="' + jsonResult.mkind + '">';
          htmlString += '</div>';
          htmlString += '</div>';
          htmlString += '<div class="row">';
          htmlString += '<div class="col s12 center">';
          htmlString += '<a id="systemmessage-alldeletebtn" class="waves-effect waves-light btn-small red darken-2 disabled"><i class="material-icons left">delete</i>一括削除(ALL DELETE)</a>';
          htmlString += '</div>';
          htmlString += '</div>';
          $("#systemMessagearea").html(htmlString);

          $("#G0002MessageKindHidden").unbind("change");
          $("#G0002MessageKindHidden").bind("change", changeMessageKind);
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
    var url = "/" + $("#G0002MessageKindHidden").val() + "/changeMessageKind";
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
  //---------------------------------------------------------------------------
  //- 圃場グループ選択変更
  //---------------------------------------------------------------------------
  function changeFieldGroup() {

    var inputJson = StringToJson('{"selectFieldGroup":"' + $('#G0002FieldGroupHidden').val() + '"}');
    $.ajax({
      url:"/changeFieldGroup",               //メニュー初期処理
      type:'POST',
      data:JSON.stringify(inputJson),               //入力用JSONデータ
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        GetFieldGroupSP();
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  //---------------------------------------------------------------------------
  //- ワークチェイン変更
  //---------------------------------------------------------------------------
  function changeWorkChain() {

    var inputJson = StringToJson('{"selectWorkChain":"' + $('#G0002WorkChainHidden').val() + '"}');
    $.ajax({
      url:"/changeWorkChain",
      type:'POST',
      data:JSON.stringify(inputJson),               //入力用JSONデータ
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        GetWork();
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 区画検索条件生成時
  //------------------------------------------------------------------------------------------------------------------
  function kukakusearch() {
    var ks = $('#kukakusearch');
    if (ks != undefined) {
      var htmlS = "";
      ks.empty();

      ks.append('<h6 class="">区画検索条件</h6>');     //コンテンツヘッダーを生成する
      ks.append('<div class="row">');
      ks.append('<div class="col s12 input-field">');
      ks.append('<span class="mselectmodal-trigger-title">状況照会</span><a href="#selectmodal"  class="selectmodal-trigger" title="状況照会"" data="getDisplayStatus" displayspan="#G0002DisplayStatus"><span id="G0002DisplayStatus" class="blockquote-input">未選択</span></a>');
      ks.append('</div>');
      ks.append('</div>');
      ks.append('<div class="row">');
      ks.append('<div class="col s12">');
      ks.append('<span class="mselectmodal-trigger-title impact-text">※状況照会で「圃場」を選択した場合、検索条件として有効な項目は「圃場グループ」のみとなります。</span>');
      ks.append('</div>');
      ks.append('</div>');
      ks.append('</br></br></br>');
      ks.append('<div class="row">');
      ks.append('<div class="col s12 input-field"><input type="text" placeholder="" id="G0002SskKukakuName" class="input-text-color" style=""><label for="G0002SskKukakuName">区画名</label></div>');
      ks.append('</div>');
      ks.append('<div class="row">');
      ks.append('<div class="col s12 input-field">');
      ks.append('<span class="mselectmodal-trigger-title">圃場グループ</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="圃場グループ一覧"" data="getFieldGroup" displayspan="#G0002FieldGroupSpan"><span id="G0002FieldGroupSpan" class="blockquote-input">未選択</span></a>');
      ks.append('</div>');
      ks.append('</div>');
      ks.append('<div class="row">');
      ks.append('<div class="col s12 input-field">');
      ks.append('<span class="mselectmodal-trigger-title">対象区画</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="対象区画一覧" data="getCompartmentOfFarm" displayspan="#G0002SskMultiKukaku"><span id="G0002SskMultiKukaku" class="blockquote-input">未選択</span></a>');
      ks.append('</div>');
      ks.append('</div>');
      ks.append('<div class="row">');
      ks.append('<div class="col s12 input-field">');
      ks.append('<span class="mselectmodal-trigger-title">対象品種</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="対象品種一覧" data="getHinsyu" displayspan="#G0002SskHinsyu"><span id="G0002SskHinsyu" class="blockquote-input">未選択</span></a>');
      ks.append('</div>');
      ks.append('</div>');
      ks.append('<div class="row">');
      htmlS  = '<div class="col s6 input-field">';
      htmlS += '<input type="text" placeholder="" id="G0002SskSeiikuF" class="right-align input-text-color" style="">';
      htmlS += '<label for="G0002SskSeiikuF">生育日数（自）</label>';
      htmlS += '</div>';
      ks.append(htmlS);
      htmlS  = '<div class="col s6 input-field">';
      htmlS += '<input type="text" placeholder="" id="G0002SskSeiikuT" class="right-align input-text-color" style="">';
      htmlS += '<label for="G0002SskSeiikuT">生育日数（至）</label>';
      htmlS += '</div>';
      ks.append(htmlS);
      ks.append('</div>');
      ks.append('<div class="row">');
      ks.append('<div class="col s12">');
      ks.append('<a href="#!" id="kukakusearchclear" class="waves-effect waves-green btn-flat left string-color" style="">クリア</a>');
      ks.append('<a href="#!" id="kukakusearchback" class="waves-effect waves-green btn-flat right string-color" style="">閉じる</a>');
      ks.append('<a href="#!" id="kukakusearchcommit" class="waves-effect waves-green btn-flat right string-color" style="">確　定</a>');
      ks.append('</div>');
      ks.append('</div>');

      $('#kukakusearchclear').unbind("click");
      $('#kukakusearchclear').bind("click", kukakusearchclear);
      $('#kukakusearchcommit').unbind("click");
      $('#kukakusearchcommit').bind("click", kukakusearchCommit);
      $('#kukakusearchback').unbind("click");
      $('#kukakusearchback').bind("click", kukakusearchClose);

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
      $('#kukakusearchtrigger').unbind('click');
      $('#kukakusearchtrigger').bind('click', kukakusearchModalOpen);

      ks.hide();

    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 区画検索条件取得時
  //------------------------------------------------------------------------------------------------------------------
  function getKukakusearchWhere() {
    var url = "/getAccountInfo";
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        if (jsonResult.result == 'SUCCESS') {
          //----- 状況照会 -----
          selectDataGet("#G0002DisplayStatus", "getDisplayStatus");
          var oJson = selectData(jsonResult.displayStatus);
          if (oJson != undefined) {
            oJson.select = true;
          }
          selectClose();
          //----- 区画名 -----
          $('#G0002SskKukakuName').val(jsonResult.sskKukakuName);

          //----- 圃場グループ -----
          mSelectDataGet("#G0002FieldGroupSpan", "getFieldGroup");
          var fgs = new String(jsonResult.selectFieldGroupId).split(",");

          for (var key in fgs) {
            var data = fgs[key];
            var oJson = mSelectData(data);
            if (oJson != undefined) {
              oJson.select = true;
            }
          }
          mSelectClose();
          //----- 対象区画 -----
          mSelectDataGet("#G0002SskMultiKukaku", "getCompartmentOfFarm");
          var mks = new String(jsonResult.sskMultiKukaku).split(",");

          for (var key in mks) {
            var data = mks[key];
            var oJson = mSelectData(data);
            if (oJson != undefined) {
              oJson.select = true;
            }
          }
          mSelectClose();
          //----- 対象品種 -----
          mSelectDataGet("#G0002SskHinsyu", "getHinsyu");
          var hss = new String(jsonResult.sskHinsyu).split(",");

          for (var key in hss) {
            var data = hss[key];
            var oJson = mSelectData(data);
            if (oJson != undefined) {
              oJson.select = true;
            }
          }
          mSelectClose();
          //----- 生育日数 -----
          $('#G0002SskSeiikuF').val(jsonResult.sskSeiikuF);
          $('#G0002SskSeiikuT').val(jsonResult.sskSeiikuT);
        }
      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 区画検索条件オープン時
  //------------------------------------------------------------------------------------------------------------------
  function kukakusearchModalOpen() {
    var ks = $('#kukakusearch');
    var ms = $("#mainsection");
    getKukakusearchWhere();     //区画検索条件取得
    if (ms != undefined) {
      ms.fadeOut(0);
    }
    if (ks != undefined) {
      ks.fadeIn(500);
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 区画検索条件閉じる時
  //------------------------------------------------------------------------------------------------------------------
  function kukakusearchClose() {
    var ks = $('#kukakusearch');
    var ms = $("#mainsection");
    if (ks != undefined) {
      ks.fadeOut(0);
    }
    if (ms != undefined) {
      ms.fadeIn(500);
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 区画検索条件クリア時
  //------------------------------------------------------------------------------------------------------------------
  function kukakusearchclear() {
    //----- 状況照会 -----
    //特に何も行わない
    //----- 区画名 -----
    $('#G0002SskKukakuName').val("");

    //----- 圃場グループ -----
    mSelectDataGet("#G0002FieldGroupSpan", "getFieldGroup");
    for (var idx = 0; idx < oMselect.data.length; idx++) {
      oMselect.data[idx].select = true;
    }
    mSelectClose();
    //----- 対象区画 -----
    mSelectDataGet("#G0002SskMultiKukaku", "getCompartmentOfFarm");
    for (var idx = 0; idx < oMselect.data.length; idx++) {
      oMselect.data[idx].select = false;
    }
    mSelectClose();
    //----- 対象品種 -----
    mSelectDataGet("#G0002SskHinsyu", "getHinsyu");
    for (var idx = 0; idx < oMselect.data.length; idx++) {
      oMselect.data[idx].select = false;
    }
    mSelectClose();
    //----- 生育日数 -----
    $('#G0002SskSeiikuF').val(0);
    $('#G0002SskSeiikuT').val(0);

    displayToast('検索条件を初期化しました。確定ボタンを押下して条件を反映させてください。', 4000, '');
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 区画検索条件確定時
  //------------------------------------------------------------------------------------------------------------------
  function kukakusearchCommit() {

    //入力チェック
    var from = $("#G0002SskSeiikuF").val();
    var to = $("#G0002SskSeiikuT").val();

    //----- 生育日数が未入力の場合、0に補正する
    if (from == "") {
      from = 0;
    }
    else {
      if (!NumberCheck("G0002SskSeiikuF", "生育日数（自）")) {
        return false;
      }
    }
    if (to == "") {
      to = 0;
    }
    else {
      if (!NumberCheck("G0002SskSeiikuT", "生育日数（至）")) {
        return false;
      }
    }

    from *= 1;
    to *= 1;

    if (to < from) {
      alert("生育日数（自）に生育日数（至）より大きい数値が設定されています。");
      return false;
    }

    var display = selectConvertJson("#G0002DisplayStatus");
    var name = $("#G0002SskKukakuName").val();
    var fg = mSelectConvertJson("#G0002FieldGroupSpan");
    var mk = mSelectConvertJson("#G0002SskMultiKukaku");
    var sh = mSelectConvertJson("#G0002SskHinsyu");

    var jsondata = {};

    jsondata["display"] = display;
    jsondata["from"]    = from;
    jsondata["to"]      = to;
    jsondata["name"]    = name;
    jsondata["fg"]      = fg;
    jsondata["mk"]      = mk;
    jsondata["sh"]      = sh;

    console.log("jsondata", jsondata);

    $.ajax({
      url:"/kukakusearchCommit",
      type:'POST',
      data:JSON.stringify(jsondata),                //入力用JSONデータ
      complete:function(data, status, jqXHR){           //処理成功時
    	GetFieldGroupSP();
        var ks = $('#kukakusearch');
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
    $("#G0002WorkList").scrollTop(0);
    $("#G0002HouseList").scrollTop(0);
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