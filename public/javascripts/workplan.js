(function($){

  var oDLK        = new Array(7);   /* 日付キーリスト */
  var oDataList   = new Array(7);   /* JSONDataリスト */
  var actionflg   = 0;              /* アクションフラグ */

  $(document).ready(function(){

    init();

  }); //end of document.ready
  function removeJokenEvent() {
    $('#WorkPlanFromDate').unbind("change");
    $('#WorkPlanAccountValue').unbind("change");
    $('#WorkPlanCropValue').unbind("change");
    $('#WorkPlanWorkValue').unbind("change");
    $('#WorkPlanKukakuValue').unbind("change");
    $('#WorkPlanAddBtn').unbind("click");
    $('.workPlanTitle').unbind("click");
    $('.navi-workdate').unbind("click");
    $('#switchend').unbind("click");
    $('#switchmode').unbind("click");
  }
  function addJokenEvent() {
    $('#WorkPlanFromDate').bind("change", changeWhere);
    $('#WorkPlanAccountValue').bind("change", changeWhere);
    $('#WorkPlanCropValue').bind("change", changeWhere);
    $('#WorkPlanWorkValue').bind("change", changeWhere);
    $('#WorkPlanKukakuValue').bind("change", changeWhere);
    $('#WorkPlanAddBtn').bind("click", onClickWorkPlanMenuEvent);
    $('.workPlanTitle').bind("click", jkenChange);
    $('.navi-workdate').bind("click", onClickNaviWorkDate);
    $('#switchend').bind("click", endDisplay);
    $('#switchmode').bind("click", switchMode);
  }
  function init() {

	    var url = '/workPlanInit';

	    $.ajax({
	      url:url,
	      type:'GET',
	      complete:function(data, status, jqXHR){
	        var jsonResult = JSON.parse( data.responseText );

	        removeJokenEvent();

	        //----- 作業日を入力する -----
	        var sysdate = GetSystemDate();
	        $('#WorkPlanFromDate').val(sysdate);

	        //----- 担当者一覧を選択する -----
	        $('#WorkPlanAccountValue').val(jsonResult.accountId);
	        mSelectDataGet("#WorkPlanAccount", "getAccountOfFarmAll");
	        var accounts = new String(jsonResult.accountId).split(",");
	        for (var key in accounts) {
	          var data = accounts[key];
	          var oJson = mSelectData(data);
	          if (oJson != undefined) {
	            oJson.select = true;
	          }
	        }
	        mSelectClose();

	        //----- 作業一覧を選択する -----
	        $('#WorkPlanWorkValue').val(jsonResult.workId);
	        mSelectDataGet("#WorkPlanWork", "getWorkOfFarm");
	        var works = new String(jsonResult.workId).split(",");
	        for (var key in works) {
	          var data = works[key];
	          var oJson = mSelectData(data);
	          if (oJson != undefined) {
	            oJson.select = true;
	          }
	        }
	        mSelectClose();

	        //----- 区画一覧を選択する -----
	        $('#WorkPlanKukakuValue').val(jsonResult.kukakuId);
	        mSelectDataGet("#WorkPlanKukaku", "getCompartmentOfFarm");
	        var kukakus = new String(jsonResult.kukakuId).split(",");
	        for (var key in kukakus) {
	          var data = kukakus[key];
	          var oJson = mSelectData(data);
	          if (oJson != undefined) {
	            oJson.select = true;
	          }
	        }
	        mSelectClose();

	        getWorkPlan();

	      },
	      dataType:'json',
	      contentType:'text/json',
	      async: false
	    });

  }
  function switchMode() {
    var my=$(this);
    if(my.attr("stts") == "on") {
      actionflg = 1;
      my.text("管理")
      my.attr("stts", "off");
      my.addClass("off");
    }
    else {
      actionflg = 0;
      my.attr("stts", "on");
      my.text("作業")
      my.removeClass("off");
    }
  }
  function endDisplay() {
    var my=$(this);
    if(my.attr("stts") == "on") {
      $(".workEnd").hide();
      my.attr("stts", "off");
      my.addClass("off");
    }
    else {
      $(".workEnd").show();
      my.attr("stts", "on");
      my.removeClass("off");
    }
  }
  function jkenChange() {
	  if($("#jkenAreaArea").hasClass("close")) {
		  $("#jkenArea").show();
		  $("#jkenAreaArea").removeClass("close");
		  $("#joukenSection").removeClass("close");
		  $(".onedate").removeClass("close");
		  $(".dateList").removeClass("close");
	  }
	  else {
		  $("#jkenArea").hide();
		  $("#jkenAreaArea").addClass("close");
		  $("#joukenSection").addClass("close");
		  $(".onedate").addClass("close");
		  $(".dateList").addClass("close");
	  }
  }
  function getWorkPlan() {

    removeJokenEvent();

    //----- 作業日を入力する -----
    var sysdate = GetSystemDate();
	//【前回検索条件に変更する】
	//	    if (localStorage.getItem("backMode") == "5" || localStorage.getItem("backMode") == "7") {
	//	      sysdate = localStorage.getItem("backWorkDate");
	//	    }
    if(localStorage.getItem("backWorkDate") != undefined && localStorage.getItem("backWorkDate") != null && localStorage.getItem("backWorkDate") != "null") {
      sysdate = localStorage.getItem("backWorkDate");
    }
    $('#WorkPlanFromDate').val(sysdate);

    //----- 担当者一覧を選択する -----
    //----- アカウントステータスから取得の為、当処理は廃止する -----
    /*
    var accountId = userinfo.id;
    if (userinfo.initId == 1) {
      accountId = userinfo.ids;
    }
    */
	//【前回検索条件に変更する】
	//	    if (localStorage.getItem("backMode") == "5") {
	//	      accountId = localStorage.getItem("backTantouId");
	//	    }
    /*
    if(localStorage.getItem("backTantouId") != undefined && localStorage.getItem("backTantouId") != null && localStorage.getItem("backTantouId") != "null") {
      accountId = localStorage.getItem("backTantouId");
    }
    $('#WorkPlanAccountValue').val(accountId);
    mSelectDataGet("#WorkPlanAccount", "getAccountOfFarmAll");
    var accounts = new String(accountId).split(",");
    for (var key in accounts) {
      var data = accounts[key];
      var oJson = mSelectData(data);
      if (oJson != undefined) {
        oJson.select = true;
      }
    }
    mSelectClose();
    */
    //----- 生産物一覧を選択する -----
    var cropId = "0";
	//【前回検索条件に変更する】
	//	    if (localStorage.getItem("backMode") == "5") {
	//	      cropId = localStorage.getItem("backCropId");
	//	    }
    if(localStorage.getItem("backCropId") != undefined && localStorage.getItem("backCropId") != null && localStorage.getItem("backCropId") != "null") {
      cropId = localStorage.getItem("backCropId");
    }
    $('#WorkPlanCropValue').val(cropId);
    selectDataGet("#WorkPlanCrop", userinfo.farm + "/getCropOfFarmAll");
    var oJson = selectData(cropId);
    if (oJson != undefined) {
      oJson.select = true;
    }
    selectClose();

    addJokenEvent();

    localStorage.setItem("backMode"         , null);
    localStorage.setItem("backWorkDate"     , null);
    localStorage.setItem("backTantouId"     , null);
    localStorage.setItem("backCropId"       , null);

  }
  function getDLK(keydate){
    var dlk = null;
    for (var idx in oDLK) {
      var data = oDLK[idx];
      if (data.key == keydate) {
        dlk = data;
        break;
      }
    }
    return dlk;
  }
  function addCardEvent() {
    $(".workPlanCard").unbind("click");
    $(".workPlanCard").bind("click", workingStart);
    $(".workingCard").unbind("click");
    $(".workingCard").bind("click", workingEnd);
    $('.mselectmodal-trigger').unbind('click');
    $('.mselectmodal-trigger').bind('click', mSelectOpen);
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);
    $(".edit-trigger").unbind("click");
    $(".edit-trigger").bind("click", workPlanEdit);
    $(".commit-trigger").unbind("click");
    $(".commit-trigger").bind("click", workPlanCommit);
    $(".copyall-trigger").unbind("click");
    $(".copyall-trigger").bind("click", planCopyAll);
    $('.delete-trigger').unbind('click');
    $('.delete-trigger').bind('click', workPlanDelete);
    $('.stop-trigger').unbind('click');
    $('.stop-trigger').bind('click', workingStop);
    $(".tantouChange").unbind("change");
    $(".tantouChange").bind("change", tantouChange);
    $(".planCopy").unbind("change");
    $(".planCopy").bind("change", planCopy);
  }
  function setcardread() {
    $('.cardread').on('inview', function(event, isInView, visiblePartX, visiblePartY) {
      if (isInView) {
        var vc = $(this);
        vc.on('inview',null);
        vc.removeClass("cardread");
        var dlk = getDLK(vc.attr("keydate"));
        var key = vc.attr("keyIdx");
        for (var i = 0; i < 10; i++) {
          if (dlk.count <= dlk.view) {
            break;
          }
          var data = oDataList[dlk.idx][dlk.view];
          dlk.view++;
          viewDataCard(key, data, dlk);
        }
        var my = $("#ondate" + vc.attr("keydate")).children(".folding-trigger");
        if (my != undefined) {
            if (my.attr("status") == "open") {
    	        my.closest(".onedate").find(".message").show();
    	        my.closest(".onedate").find(".card-icon").show();
    	        my.closest(".onedate").find(".fusen").addClass("up");
    	    }
    	    else {
                my.closest(".onedate").find(".message").hide();
                my.closest(".onedate").find(".card-icon").hide();
                my.closest(".onedate").find(".fusen").removeClass("up");
    	    }
        }
        addCardEvent();
        worktimewrite();
        setcardread();
      }
    });
  }
  function viewDataCard(key, data, dlk) {
    var workList = $("#datework" + data.keydate);
    var code  = data["timeLineColor"];
    var red   = parseInt(code.substring(0,2), 16);
    var green = parseInt(code.substring(2,4), 16);
    var blue  = parseInt(code.substring(4,6), 16);
    var textColor = "nomal";
    var eventString = "";
    if ((dlk.view % 10) == 0) {
      eventString = "cardread ";
    }
    if (data.end == 1) {
      textColor = "end";
      eventString += "workEnd";
      workList.append('<li keyIdx="'+ key +'" keydate="' + data.keydate + '" view="' + dlk.view + '" class="' + eventString + '" key="' + data.workPlanId + '" time="' + data.worktime + '"><div class="workplandetail fusen ' + textColor + '" style="border-left: 4px solid #' + code + '"><span class="workName">' + data.workName + '(' + data.kukakuName + ')</span><span class="workTime">' + data.worktime + '&nbsp;分</span><span class="accountName">' + data.accountName + '</span><span class="message">' + data.message + '</span></div></li>');
    }
    else {
      if (data.workPlanFlag == 1 || data.workPlanFlag == 4 || data.workPlanFlag == 5) {
        textColor = " working";
        eventString += "workingCard";
        workList.append('<li keyIdx="'+ key +'" keydate="' + data.keydate + '" view="' + dlk.view + '" class="' + eventString + '" key="' + data.workPlanId + '" tantou="' + data.accountId + '" time="' + data.worktime + '"><div class="workplandetail fusen ' + textColor + '" style="border-left: 4px solid #' + code + '"><span class="workName">' + data.workName + '(' + data.kukakuName + ')</span><span class="workTime">' + data.worktime + '&nbsp;分経過</span><span class="accountName">' + data.accountName + '</span><span class="message">' + data.message + '</span><a href="#"  class="stop-trigger"><i class="material-icons stop small" style="color:#1976d2;">cancel</i></a></div></li>');
      }
      else {
        eventString += "workPlanCard";
        //workList.append('<li class="' + eventString + ' item" key="' + data.workPlanId + '" tantou="' + data.accountId + '"><div class="workplandetail fusen ' + textColor + '" style="border-left: 4px solid #' + code + '"><span class="workName">' + data.workName + '(' + data.kukakuName + ')</span><span class="accountName">' + data.accountName + '</span><span class="message">' + data.message + '</span><a href="#"  class="delete-trigger" key="' + data.workPlanId + '"><i class="material-icons delete small" style="color:#ffcdd2;">delete</i></a><a><i class="material-icons event small" style="color:#eeeeee;">event</i></a><a href="#selectmodal"  class="selectmodal-trigger" title="担当者選択" data="getAccountOfFarm" displayspan="#WorkPlanFusenAccountSpan' + parseInt(key) + '" htext="#WorkPlanFusenAccount' + parseInt(key) + '"><input type="hidden" class="tantouChange" id="WorkPlanFusenAccount' + parseInt(key) + '"  key="' + data.workPlanId + '" value=""/><i class="material-icons tantou small" style="color:#b2dfdb;">face</i></a></div></li>');
        var htmlString = '<li keyIdx="'+ key +'" keydate="' + data.keydate + '" view="' + dlk.view + '" class="' + eventString + ' item" key="' + data.workPlanId + '" tantou="' + data.accountId + '" time="' + data.worktime + '">';
        htmlString += '<div class="workplandetail fusen ' + textColor + '" style="border-left: 4px solid #' + code + '"><span class="workName">' + data.workName + '(' + data.kukakuName + ')</span><span class="workTime">' + data.worktime + '&nbsp;分</span><span class="accountName">' + data.accountName + '</span><span class="message">' + data.message + '</span>';
        htmlString += '<a href="#" class="card-icon edit-trigger" key="' + data.workPlanId + '"><i class="material-icons edit small" style="color:#ffe0b2;">create</i></a>';
        htmlString += '<a href="#" class="card-icon commit-trigger" key="' + data.workPlanId + '"><i class="material-icons commit small" style="color:#c5cae9;">done</i></a>';
        htmlString += '<a href="#" class="card-icon copyall-trigger" key="' + data.workPlanId + '" kukakuId="' + data.kukakuId + '"><i class="material-icons copyall small" style="color:#bbdefb;">file_copy</i></a>';
        htmlString += '<a href="#selectmodal"  class="card-icon selectmodal-trigger" title="コピー先区画選択" data="getCompartmentOfFarm" displayspan="#WorkPlanFusenKukakuSpan' + parseInt(key) + '" htext="#WorkPlanFusenKukaku' + parseInt(key) + '"><input type="hidden" class="planCopy" id="WorkPlanFusenKukaku' + parseInt(key) + '"  key="' + data.workPlanId + '" value=""/><i class="material-icons copy small" style="color:#b2dfdb;">file_copy</i></a>';
        htmlString += '<a href="#"  class="card-icon delete-trigger" key="' + data.workPlanId + '"><i class="material-icons delete small" style="color:#ffcdd2;">delete</i></a>';
        htmlString += '<a href="#selectmodal"  class="card-icon selectmodal-trigger" title="担当者選択" data="getAccountOfFarm" displayspan="#WorkPlanFusenAccountSpan' + parseInt(key) + '" htext="#WorkPlanFusenAccount' + parseInt(key) + '"><input type="hidden" class="tantouChange" id="WorkPlanFusenAccount' + parseInt(key) + '"  key="' + data.workPlanId + '" value=""/><i class="material-icons tantou small" style="color:#b2dfdb;">face</i></a>';
        htmlString += '</div>';
        workList.append(htmlString);
        selectDataGet("#WorkPlanFusenAccountSpan" + key, "getAccountOfFarm");
        var oJson = selectData(userinfo.id);
        if (oJson != undefined) {
          oJson.select = true;
        }
      }
    }
  }
  function changeWhere() {
    var fromDate  = $('#WorkPlanFromDate').val();
    var account   = $('#WorkPlanAccountValue').val();
    var work      = $('#WorkPlanWorkValue').val();
    var kukaku    = $('#WorkPlanKukakuValue').val();
    var crop      = $('#WorkPlanCropValue').val();
    fromDate = fromDate.replace(/\//g, '');
    if (work == "") {
    	work = "*";
    }
    if (kukaku == "") {
    	kukaku = "*";
    }

    var url = '/' + fromDate + '/' + account + '/' + work + '/' + kukaku + '/' + crop + '/getWorkPlanS';

    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );
        var dateList   = jsonResult.dateList;
        var planList   = jsonResult.planList;

        var oDateList  = $("#WorkPlanDateList");
        oDateList.empty();
        $('.date').unbind("click");

        oDLK        = new Array(7);   //初期化
        oDataList   = new Array(7); //初期化
        var dlkIdx  = 0;    //初期化

        for (var key in dateList) {
          var data = dateList[key];
          oDateList.append('<li id="ondate' + data.keydate + '" class="onedate" key="' + data.keydate + '"><a href="#" class="folding-trigger folding-icon" status="close"><i class="material-icons">unfold_less</i></a><span class="date" key="' + data.keydate + '">' + data.date  + '&nbsp;(' + data.dtwk + ')</span><span class="time" id="time' + data.keydate + '"></span><ul id="datework' + data.keydate + '" class="list date-group" key="' + data.keydate + '"></ul></li>');
          var json = { "idx":dlkIdx, "key":data.keydate, "view":0, "count":0}; //JSON生成
          oDLK.push(json);
          oDataList[dlkIdx] = [];
          dlkIdx++;
        }

        for (var key in planList) {
          var data = planList[key];
          var dlk = getDLK(data.keydate);
          dlk.count++;
          oDataList[dlk.idx].push(data);
          if ( 10 < dlk.count) {
            continue;
          }
          dlk.view++;
          viewDataCard(key, data, dlk);
        }

        setcardread();

        $(".message").hide();
        $(".card-icon").hide();
        $(".folding-trigger").unbind("click");
        $(".folding-trigger").bind("click", foldingMessage);

        addCardEvent();

        $('.date').bind("click", onClickOndate);
        //並び替えオブジェクト設定
        for (var idx=0; idx < 7;idx++) {
          var obj = $('.list')[idx];
          Sortable.create(obj
          , {
            draggable: ".item"
            ,sort :false
            ,delay: 50
            ,preventOnFilter: false
            ,group: {
              name: "date-group",
            }
            ,onEnd: function (evt) {
               var url = "/" + evt.item.getAttribute("key") + "/" + evt.to.getAttribute("key") + "/workPlanDateChange";
               $.ajax({

                 url:url,
                 type:'GET',
                 complete:function(data, status, jqXHR){
                   var jsonResult = JSON.parse( data.responseText );
                   worktimewrite();
                   actionflg =0;
                 },
                 dataType:'json',
                 contentType:'text/json',
                 async: false
               });
            }
          });
          worktimewrite();
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
    return false;
  }
  function foldingMessage() {
    var my = $(this);

    if (my.attr("status") == "open") {
      my.closest(".onedate").find(".message").hide();
      my.closest(".onedate").find(".card-icon").hide();
      my.closest(".onedate").find(".fusen").removeClass("up");
      my.attr("status", "close");
    }
    else {
      my.closest(".onedate").find(".message").show();
      my.closest(".onedate").find(".card-icon").show();
      my.closest(".onedate").find(".fusen").addClass("up");
      my.attr("status", "open");
    }
  }
  function worktimewrite() {
    var datelist = $(".onedate");
    for (var key = 0; key < datelist.length; key++) {
      var time = 0;
      var task = 0;
      var dates = datelist[key];
      var works = $("#datework" + dates.getAttribute("key")).find('li');
      for (var wkey = 0; wkey < works.length; wkey++) {
        var work = works[wkey];
        time += parseInt(work.getAttribute("time"));
        task++; //タスク数カウントアップ
      }
      $("#time" + dates.getAttribute("key")).text(" " + time + " 分(" + task + ")");
    }
  }
  function workingStart() {
    if (actionflg == 1) { //管理モード
      preventDefault();
      return;
    }
    var my = $(this);

    if (userinfo.work != 0) {
      displayToast('既に別の作業を行っています。', 2000, 'rounded');
      return false;
    }
    else if (userinfo.id != my.attr("tantou")) {
      displayToast('別の担当者が割り当てられている作業です。', 2000, 'rounded');
      return false;
    }

    if (userinfo.prompt == 1) { //作業開始確認ありの場合
      if (!confirm("作業を開始します。よろしいですか？")) {
        return false;
      }
    }

    var url = "/" + my.attr("key") + "/startWorkPlan";
    $.ajax({

      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );
        var child = my.children(".workplandetail");
        child.removeClass("nomal");
        child.addClass("working");
        child.children(".message").html(jsonResult.message);
        child.children("a").remove();
        my.removeClass("workPlanCard");
        my.addClass("workingCard");
        my.unbind("click");
        my.bind("click", workingEnd);
        changeWhere();

        getAccountInfo();

      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  }
  function workingStop() {
    var my = $(this);

    var url = "/workingstop";
    $.ajax({

      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        changeWhere();
        getAccountInfo();

      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
    return false;
  }
  function workingEnd() {
    var my = $(this);
    var param = {"workId":"", "workKukaku":"", "workDate":"", "workAccount":"", "mode":10, "planId":0 };

    param.workId      = userinfo.work;
    param.workKukaku  = userinfo.field;
    param.workAccount = userinfo.id;
    param.planId      = userinfo.plan;

    var url = "/planToDiary";

    $.ajax({
        url:url,                        //作業日誌保存処理
        type:'POST',
        data:JSON.stringify(param),              //入力用JSONデータ
        complete:function(data, status, jqXHR){         //処理成功時

          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
          var child = my.children(".workplandetail");
          child.removeClass("working");
          child.addClass("end");
          child.children(".workTime").html(jsonResult.workTime + "&nbsp;分");
          child.children(".message").html(jsonResult.message);
          child.children("a").remove();
          my.removeClass("workingCard");
          my.unbind("click");

          getAccountInfo();

        },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
  }
  function workPlanCommit() {
    var my = $(this);
    var param = {"workId":"", "workKukaku":"", "workDate":"", "workAccount":"", "mode":10, "planId":0 };

    param.workId      = userinfo.work;
    param.workKukaku  = userinfo.field;
    param.workAccount = userinfo.id;
    param.planId      = my.attr("key");

    var url = "/planToDiaryTimeCommit";

    $.ajax({
        url:url,                        //作業日誌保存処理
        type:'POST',
        data:JSON.stringify(param),              //入力用JSONデータ
        complete:function(data, status, jqXHR){         //処理成功時

          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
          var child = my.closest(".workplandetail");
          child.removeClass("working");
          child.addClass("end");
          child.children(".workTime").html(jsonResult.workTime + "&nbsp;分");
          child.children(".message").html(jsonResult.message);
          child.children("a").remove();
          my.removeClass("workingCard");
          my.unbind("click");

          getAccountInfo();

        },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
    return false;
  }
  function tantouChange() {
    var my = $(this);
    var url = "/" + my.attr("key") + "/" + my.val() + "/workPlanTantouChange";
    $.ajax({

      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        if (jsonResult.result == "SUCCESS") {
          var tantou = my.closest("div").children(".accountName");
          tantou.text(jsonResult.name);
          var fusen = my.closest("li");
          fusen.attr("tantou", jsonResult.id);
          var tantous = $('#WorkPlanAccountValue').val().split(',');
          var exists = false;
          for (var tantou in tantous) {
            if (jsonResult.id == tantous[tantou]) {
              exists = true;
              break;
            }
          }
          if (exists == false) {
            fusen.remove();
          }
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  }
  function planCopy() {
    var my = $(this);
    var url = "/" + my.attr("key") + "/" + my.val() + "/workPlanCopy";
    $.ajax({

      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        if (jsonResult.result == "SUCCESS") {
          displayToast('作業指示をコピーしました', 2000, 'rounded');
          changeWhere();
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  }
  function planCopyAll() {
    var my = $(this);
    var url = "/" + my.attr("key") + "/" + my.attr("kukakuId") + "/workPlanCopy";
    $.ajax({

      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        if (jsonResult.result == "SUCCESS") {
          displayToast('作業指示をコピーしました', 2000, 'rounded');
          changeWhere();
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  }
  function workPlanEdit() {
    var my = $(this);
    var url = "/" + my.attr("key") + "/workPlanEditMove";

    var fromDate  = $('#WorkPlanFromDate').val();
    var account   = $('#WorkPlanAccountValue').val();
    var crop      = $('#WorkPlanCropValue').val();

    localStorage.setItem("backMode"         , "5");
    localStorage.setItem("backWorkDate"     , fromDate);
    localStorage.setItem("backTantouId"     , account);
    localStorage.setItem("backCropId"       , crop);

    window.location.href = url;

    return false;
  }
  function workPlanDelete() {
    var my = $(this);
    var pr = my.closest('.workplandetail');

    pr.removeClass("nomal");
    pr.addClass("prompt");
    setTimeout(function() {
      if (!confirm("この作業指示を削除します。よろしいですか？")) {
        pr.addClass("nomal");
        pr.removeClass("prompt");
        return false;
      }
      var url = "/" + my.attr("key") + "/workPlanDelete";
      $.ajax({

        url:url,
        type:'GET',
        complete:function(data, status, jqXHR){
          var jsonResult = JSON.parse( data.responseText );

          if (jsonResult.result == "SUCCESS") {
            var fusen = my.closest("li");
            fusen.remove();
            displayToast('作業指示を削除しました', 2000, 'rounded');
          }
        },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
    }, 0);

    return false;
  }
  function onClickWorkPlanMenuEvent() {

    lockScreen(LOCK_ID);

    //----- 一時エリアの作成 -----
    var divTag = $('<div />').attr("id", "WorkPlanMenuArea");
    divTag.addClass("WorkPlanMenuArea");

    $('body').append(divTag);
    var area = $("#WorkPlanMenuArea");

    //----- 作業指示作成 -----
    area.append('<div class="selectitem top waves-effect waves-teal " id="WorkPlanSingleAdd"><span class="">作業指示作成</span></div>');
    //----- 作業指示一括作成 -----
    area.append('<div class="selectitem waves-effect waves-teal " id="WorkPlanChainAdd"><span class="">作業指示一括作成</span></div>');

    area.append('<a id="WorkPlanMenuCancel" class="waves-effect waves-teal cancel">戻&nbsp;&nbsp;&nbsp;&nbsp;る</a>')

    $("#WorkPlanSingleAdd").unbind("click");
    $("#WorkPlanSingleAdd").bind("click", onClickWorkPlanSingleAdd);
    $("#WorkPlanChainAdd").unbind("click");
    $("#WorkPlanChainAdd").bind("click", onClickWorkPlanChainAdd);
    $("#WorkPlanMenuCancel").unbind("click");
    $("#WorkPlanMenuCancel").bind("click", onClickWorkPlanMenuCancel);

  }
  function onClickWorkPlanSingleAdd() {
    var area = $("#WorkPlanMenuArea");
    area.hide();
    onClickWorkPlanAddEvent();
  }
  function onClickWorkPlanChainAdd() {
    var area = $("#WorkPlanMenuArea");
    area.hide();
    onClickWorkPlanChainEvent();
  }
  function onClickWorkPlanMenuCancel() {
    var area = $("#WorkPlanMenuArea");
    area.remove();
    unlockScreen(LOCK_ID);
  }
  function onClickWorkPlanAddEvent() {

    //----- 一時エリアの作成 -----
    var divTag = $('<div />').attr("id", "WorkPlanAddArea");
    divTag.addClass("WorkPlanAddArea");

    $('body').append(divTag);
    var area = $("#WorkPlanAddArea");

    //----- 作業一覧 -----
    area.append('<div class="selectitem"><span class="selectmodal-trigger-title">作業</span><a href="#selectmodal"  class="selectmodal-trigger" title="作業選択" data="getCompartmentOfFarm" displayspan="#WorkPlanAddWork"><span id="WorkPlanAddWork" class="blockquote-input">未選択</span></a></div>');
    selectDataGet("#WorkPlanAddWork", "getWorkOfFarm");
    //----- 区画一覧 -----
    area.append('<div class="selectitem"><span class="selectmodal-trigger-title">区画</span><a href="#selectmodal"  class="selectmodal-trigger" title="区画選択" data="getCompartmentOfFarm" displayspan="#WorkPlanAddKukaku"><span id="WorkPlanAddKukaku" class="blockquote-input">未選択</span></a></div>');
    selectDataGet("#WorkPlanAddKukaku", "getCompartmentOfFarm");
    $('.modal').modal();
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);

    area.append('<a id="WorkPlanAddCancel" class="waves-effect waves-teal btn-flat cancel">戻&nbsp;&nbsp;&nbsp;&nbsp;る</a>')
    area.append('<a id="WorkPlanAddCommit" class="waves-effect waves-teal btn-flat commit">決&nbsp;&nbsp;&nbsp;&nbsp;定</a>')

    $("#WorkPlanAddCancel").unbind("click");
    $("#WorkPlanAddCancel").bind("click", onClickWorkPlanAddCancel);
    $("#WorkPlanAddCommit").unbind("click");
    $("#WorkPlanAddCommit").bind("click", onClickWorkPlanAddCommit);

  }
  function onClickWorkPlanAddCancel() {
    var area = $("#WorkPlanAddArea");
    area.remove();
    var menu = $("#WorkPlanMenuArea");
    menu.show();
  }
  function onClickWorkPlanAddCommit() {

    var work    = selectConvertJson("#WorkPlanAddWork");
    var kukaku  = selectConvertJson("#WorkPlanAddKukaku");

    if (work == "") {
      displayToast('作業が選択されていません。', 2000, 'rounded');
      return false;
    }
    if (kukaku == "") {
      displayToast('区画が選択されていません。', 2000, 'rounded');
      return false;
    }

    var fromDate  = $('#WorkPlanFromDate').val();
    var account   = $('#WorkPlanAccountValue').val();
    var crop      = $('#WorkPlanCropValue').val();

    localStorage.setItem("backMode"         , "5");
    localStorage.setItem("backWorkDate"     , fromDate);
    localStorage.setItem("backTantouId"     , account);
    localStorage.setItem("backCropId"       , crop);

    var area = $("#WorkPlanAddArea");
    area.remove();
    unlockScreen(LOCK_ID);

    window.location.href = '/' + work + '/' + kukaku + '/workDiaryMove';

  }
  function onClickWorkPlanChainEvent() {

    //----- 一時エリアの作成 -----
    var divTag = $('<div />').attr("id", "WorkPlanChainArea");
    divTag.addClass("WorkPlanChainArea");

    $('body').append(divTag);
    var area = $("#WorkPlanChainArea");

    area.append('<div class="row">');
    //----- 作業予定日 -----
    area.append('<div class="col s12 input-field">');
    area.append('<span class="selectmodal-trigger-title" style="margin-top: 1.0rem;margin-left: 1.0rem;">作業予定日</span>');
    area.append('<input type="text" placeholder="作業予定日" id="WorkPlanChainDate" class="datepicker input-text-color" style="">');
    area.append('</div>');
    //----- 作業一覧 -----
    area.append('<div class="selectitem"><span class="selectmodal-trigger-title">ワークチェーン</span><a href="#selectmodal"  class="selectmodal-trigger" title="ワークチェーン選択" data="getWorkChainOfFarm" displayspan="#WorkPlanChainWork"><span id="WorkPlanChainWork" class="blockquote-input">未選択</span></a></div>');
    selectDataGet("#WorkPlanChainWork", "getWorkChainOfFarm");
    //----- 区画一覧 -----
    area.append('<div class="selectitem"><span class="selectmodal-trigger-title">区画</span><a href="#selectmodal"  class="selectmodal-trigger" title="区画選択" data="getCompartmentOfFarm" displayspan="#WorkPlanChainKukaku"><span id="WorkPlanChainKukaku" class="blockquote-input">未選択</span></a></div>');
    selectDataGet("#WorkPlanChainKukaku", "getCompartmentOfFarm");
    //----- 担当者一覧 -----
    area.append('<div class="selectitem"><span class="selectmodal-trigger-title">担当者</span><a href="#selectmodal"  class="selectmodal-trigger" title="担当者選択" data="getAccountOfFarm" displayspan="#WorkPlanChainTanto"><span id="WorkPlanChainTanto" class="blockquote-input">未選択</span></a></div>');
    selectDataGet("#WorkPlanChainTanto", "getAccountOfFarm");
    //----- コピー数 -----
    area.append('<div class="col s12 input-field">');
    area.append('<span class="selectmodal-trigger-title" style="margin-top: 1.0rem;margin-left: 1.0rem;">コピー数</span>');
    area.append('<input type="text" placeholder="コピー数" id="WorkPlanCopyCount" class="input-text-color" style="">');
    area.append('</div>');
    area.append('</div>'); //row

    startDateP();

    var systemdate = GetSystemDate();
    $('#WorkPlanChainDate').val(systemdate);
    $('#WorkPlanChainDate').datepicker('setDate', new Date(systemdate));

    $('#WorkPlanCopyCount').val("1");

    $('.modal').modal();
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);

    area.append('<a id="WorkPlanChainCancel" class="waves-effect waves-teal btn-flat cancel">戻&nbsp;&nbsp;&nbsp;&nbsp;る</a>')
    area.append('<a id="WorkPlanChainCommit" class="waves-effect waves-teal btn-flat commit">決&nbsp;&nbsp;&nbsp;&nbsp;定</a>')

    $("#WorkPlanChainCancel").unbind("click");
    $("#WorkPlanChainCancel").bind("click", onClickWorkPlanChainCancel);
    $("#WorkPlanChainCommit").unbind("click");
    $("#WorkPlanChainCommit").bind("click", onClickWorkPlanChainCommit);

  }
  function onClickWorkPlanChainCancel() {
    var area = $("#WorkPlanChainArea");
    area.remove();
    var menu = $("#WorkPlanMenuArea");
    menu.show();
  }
  function onClickWorkPlanChainCommit() {

    var workdate = $("#WorkPlanChainDate").val();
    var work     = selectConvertJson("#WorkPlanChainWork");
    var kukaku   = selectConvertJson("#WorkPlanChainKukaku");
    var tanto    = selectConvertJson("#WorkPlanChainTanto");
    var copy     = $("#WorkPlanCopyCount").val();

    if (workdate == "") {
      displayToast('作業予定日が入力されていません。', 2000, 'rounded');
      return false;
    }
    if (work == "") {
      displayToast('ワークチェーンが選択されていません。', 2000, 'rounded');
      return false;
    }
    if (kukaku == "") {
      displayToast('区画が選択されていません。', 2000, 'rounded');
      return false;
    }
    if (copy == "") {
      displayToast('コピー数が入力されていません。', 2000, 'rounded');
      return false;
    }
    if (!NumberCheck( "WorkPlanCopyCount", "コピー数")) {
      return false;
    }

    if (tanto == "") {
      tanto = "NONE";
    }

    workdate = workdate.replace(/\//g, '');

    var url = "/" + work + "/" + kukaku + "/" + tanto + "/" + workdate + "/" + copy + "/makeWorkPlanChain";
    $.ajax({

      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        if (jsonResult.result == "SUCCESS") {
          var area = $("#WorkPlanChainArea");
          area.remove();
          var menu = $("#WorkPlanMenuArea");
          menu.remove();
          changeWhere();
          unlockScreen(LOCK_ID);
          displayToast('作業指示の一括作成が完了しました。', 2000, 'rounded');
        }
        else {
          displayToast('作業指示の一括作成に失敗しました。', 2000, 'rounded');
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });

  }
  function onClickNaviWorkDate() {
    var my = $(this);
    var navi = parseInt(my.attr("dayshift"));
    var workdate = new Date(Date.parse($('#WorkPlanFromDate').val()));

    workdate.setDate(workdate.getDate() + navi);
    $('#WorkPlanFromDate').val(dateFormat(workdate)).change();
  }
  function onClickOndate() {
    var my = $(this);

    localStorage.setItem("targetOneDate"     , my.attr("key").substring(0, 4) + "/" + my.attr("key").substring(4, 6) + "/" + my.attr("key").substring(6));

    var account   = $('#WorkPlanAccountValue').val();
    var crop      = $('#WorkPlanCropValue').val();

    localStorage.setItem("backMode"         , "5");
    localStorage.setItem("backTantouId"     , account);
    localStorage.setItem("backCropId"       , crop);

    window.location.href = '/workPlanAccountMove';

  }
})(jQuery);