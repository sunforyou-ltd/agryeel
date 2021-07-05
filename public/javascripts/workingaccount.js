(function($){

  var aryData = [];
  var oBackInfo = {back: "", accountid: "", workdate: ""};

  $(function(){
  });

  $(document).ready(function(){

    $('#WorkingDate').unbind("change");
    $('.navi-workdate').unbind("click");
    var sysDate = GetSystemDate();
    $('#WorkingDate').val(sysDate);
    var dDate = GetSystemDate8();
    init(dDate);
    $("#accountworkinglist").show();
    $("#accountinfo").hide();
    $('#WorkingDate').bind("change", changeWorkingDate);
    $('.navi-workdate').bind("click", onClickNaviWorkDate);

  }); //end of document.ready

  function onClickNaviWorkDate() {
    var my = $(this);
    var navi = parseInt(my.attr("dayshift"));
    var workdate = new Date(Date.parse($('#WorkingDate').val()));

    workdate.setDate(workdate.getDate() + navi);
    $('#WorkingDate').val(dateFormat(workdate)).change();
  }
  function changeWorkingDate() {
    var d = $(this);
    var dd = d.val();
    dd = dd.replace(/\//g, '');
    init(dd);
  }

  function init(dDate) {
    var url = "/" + dDate + "/workingaccountinit";
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );

        $("#accountworkinglist").empty();

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
          var checkColor = "222222";
          if (timeLine["check"] == 1) {
            checkColor = "b71c1c";
          }

          htmlString += '<li class="timelinelist" id="list-' + timeLine["accountId"] + '" key="' + timeLine["accountId"] + '" style="border-left: 8px solid #' + timeLine["timeLineColor"] + '; background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + TRANS_TIMELINE + '); border-bottom: 0px;">';
          htmlString += '<div class="work-title">';
          if (timeLine["workId"] != 0) {
            htmlString += icontag(timeLine["workId"], timeLine["timeLineColor"], timeLine["workName"], "timeline");
          }
          htmlString += '<span class="account-text">' + timeLine["accountName"] + '</span>';
          htmlString += '<span class="work-workdate-text">'  + timeLine["workdate"] + '</span>';
          if (timeLine["workId"] != 0) {
            htmlString += '<span class="work-text">' + timeLine["kukakuName"] + '&nbsp;&nbsp;&nbsp;&nbsp;' + timeLine["workName"] + '&nbsp;&nbsp;(' + timeLine["worktime"] + '分)&nbsp;&nbsp;&nbsp;&nbsp;</span>';
          }
          htmlString += '<span class="work-update-text">'  + timeLine["updateTime"] + '</span>';
          htmlString += '<span class="work-daytime-text" style="color:#' + checkColor + '">'  + timeLine["today"] + '</span>';
          htmlString += '</div>';

          var messageIcon = "";

          if (timeLine["msgIcon"] == 2) { //注意の場合
            messageIcon = " note";
          }
          else if (timeLine["msgIcon"] == 3) { //危険の場合
            messageIcon = " danger";
          }
          htmlString += '<div class="message' + messageIcon + '">';
          htmlString += '<span>' + timeLine["message"] + '</span>';
          htmlString += '</div>';
          htmlString += '</li>';

        } // timeLineList
        htmlString += '</ul>';
        htmlString += '</div>';

        $("#accountworkinglist").html(htmlString);              //可変HTML部分に反映する

        $(".timelinelist").unbind("click");
        $(".timelinelist").bind("click", displayAccount);

        if (localStorage.getItem("backMode") == "1") {
          //lockScreen(LOCK_ID);
          oBackInfo.back      = localStorage.getItem("backMode");
          oBackInfo.accountid = localStorage.getItem("backAccountId");
          oBackInfo.workdate  = localStorage.getItem("backWorkDate");
          $("#list-" + oBackInfo.accountid).click();
        }
        else {
          oBackInfo.back      = "";
          oBackInfo.accountid = "";
          oBackInfo.workdate  = "";
        }

        localStorage.setItem("backMode"       , null);
        localStorage.setItem("backAccountId"  , null);
        localStorage.setItem("backWorkDate"   , null);
        localStorage.setItem("backWorkDiaryId", null);

      },
      dataType:'json',
      contentType:'text/json'
    });
  }

  var selectAccount = { accountId:"", workdate: ""};

  function displayAccount() {

    $("#workingdate-area").hide();
    $("#accountworkinglist").hide();
    $("#accountinfo").show();
    $("#accountinfo").empty();
    $("#accountinfo").append('<div class="progress"><div class="indeterminate"></div></div>');

    var ac = $(this);
    var dd = $('#WorkingDate').val();
    dd = dd.replace(/\//g, '');
    var url="/" + ac.attr("key") + "/" + dd + "/workingaccouninfo";
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );

        var ai = $("#accountinfo");
        ai.empty();

        //圃場情報
        ai.append('<div class="sub-menu"><i class="material-icons small left" id="timelinelistback">arrow_back</i><span class="title">担当者情報</span></div>');
        ai.append('<div class="card-panel account-working" id="accountinfo-l"></div>');
        var a1 = $("#accountinfo-l");

        a1.append('<span class="item-title">担当者名</span><span class="item">' + jsonResult["name"] + '</span>');
        a1.append('</br><span class="item-title">年間作業時間</span><span class="item time-trigger" month="0">' + jsonResult["yeartime"] + '</span><span class="sub">時間</span>');
        a1.append('</br><span class="item-title">月別作業時間</span>');
        for (var i = 1; i <= 12; i++) {
          var mnt = "0" + i;
          if ((i % 2) == 1) {
            a1.append('</br><span class="item-title">' + mnt.slice(-2) + '月</span><span class="item time-trigger" month="' + i + '">' + jsonResult["monthtime" + i] + '</span><span class="sub">時間</span>');
          }
          else {
            a1.append('&nbsp;&nbsp;<span class="item-title">' + mnt.slice(-2) + '月</span><span class="item time-trigger" month="' + i + '">' + jsonResult["monthtime" + i] + '</span><span class="sub">時間</span>');
          }
        }
        a1.append('</br></br><span class="sub">※各作業時間をタップすると、作業別時間を確認できます。</span>');

        //----- 作業日 -----
        ai.append('<div class="row">');
        ai.append('<div class="col s12 input-field">');
        ai.append('<input type="text" placeholder="作業日" id="select-workdate" class="datepicker input-text-color" style="">');
        ai.append('</div>');
        ai.append('<canvas id="workline"></canvas>');

        startDateP();

        //----- 担当者情報を保存 -----
        selectAccount.accountId = jsonResult["id"];

        $('#select-workdate').unbind("change");
        $('#select-workdate').bind("change", getAccountWorkdata);

        if (oBackInfo.back == "1") {
          var date = oBackInfo.workdate.substr(0,4) + "/" + oBackInfo.workdate.substr(4,2) + "/" + oBackInfo.workdate.substr(6,2);
          $('#select-workdate').datepicker('setDate', new Date(date));
          $('#select-workdate').val(date).change();
          oBackInfo.back      = "";
          oBackInfo.accountid = "";
          oBackInfo.workdate  = "";
          //unlockScreen(LOCK_ID);
        }
        else {
          $('#select-workdate').datepicker('setDate', new Date(jsonResult["workdate"]));
          $('#select-workdate').val(jsonResult["workdate"]).change();
        }

        $('#timelinelistback').unbind("click");
        $('#timelinelistback').bind("click", backList);

        //----- 作業時間表示イベントの追加 -----
        $('.time-trigger').unbind("click");
        $('.time-trigger').bind("click", displayWorkTime);

        //----- 全担当者の場合、作業日付とワークラインは非表示にする -----
        if (jsonResult["id"] == "aaaaaa") {
          $("#select-workdate").hide();
          $("#workline").hide();
        }
        else {
          $("#select-workdate").show();
          $("#workline").show();
        }

      },
      dataType:'json',
      contentType:'text/json'
    });

  }

  function displayWorkTime() {
    var url = "";
    var td = $(this);
    var sysdate = GetSystemDate();
    if (td.attr("month") == 0) {  //年間
      url = "/" + selectAccount.accountId + "/" + sysdate.substr(0, 4) + "/getWorkTimeOfYear";
    }
    else {                        //月別
      url = "/" + selectAccount.accountId + "/" + sysdate.substr(0, 4) + "/" + td.attr("month") + "/getWorkTimeOfMonth";
    }
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );

        var wm = $("#worktimemodal");
        if (wm != undefined) {
          wm.empty();
          wm.append('<div id="worktimearea" class="modal-content awtime">'); //モーダルコンテンツ領域を生成
          var wa = $("#worktimearea");

          wa.append('<div class="row"><div class="col s12"><span class="item-title">総作業時間</span><span class="item">' + jsonResult.totaltime + '</span><span class="sub">時間</span></div></div>');

          var datalist = jsonResult.datalist;
          wa.append('<ul id="worktimelist" class="collection">');
          var wl = $("#worktimelist");

          for (var key in datalist) {
            var data = datalist[key];
            var code  = data.workColor;
            var red   = parseInt(code.substring(0,2), 16);
            var green = parseInt(code.substring(2,4), 16);
            var blue  = parseInt(code.substring(4,6), 16);
            var alfa  = (data.workTime / jsonResult.totaltime) * 10;
            var per   = Math.round(((data.workTime / jsonResult.totaltime) * 100));
            wl.append('<li class="collection-item"><span class="item-title">' + data.workName + '</span><span class="item">' + data.workTime + '</span><span class="sub">時間</span><div class="grafhdata" style="background-color: rgba(' + red + ',' + green + ',' + blue + ', ' + alfa + '); width:' + (per / 4) + '%">&nbsp;</div><span class="per">' + per + '%</span></li>');
          }

          wm.append('<div id="worktimefooter" class="modal-footer">');
          var wf = $('#worktimefooter');
          if (wf != undefined) {
            wf.append('<a href="#!" id="worktimeback" class="waves-effect waves-green btn-flat">閉じる</a>');
            $('#worktimeback').unbind("click");
            $('#worktimeback').bind("click", closeWorkTime);
          }

          wm.modal('open');
        }
      },
      dataType:'json',
      contentType:'text/json'
    });
  }

  function closeWorkTime() {
    var wm = $('#worktimemodal');
    if (wm != undefined) {
      wm.modal('close');
    }
  }

  function backList() {
    $("#workingdate-area").show();
    $("#accountworkinglist").show();
    $("#accountinfo").hide();
  }


  function onClick(e) {
    var rect = e.target.getBoundingClientRect();
    x = e.clientX - rect.left;
    y = e.clientY - rect.top;

    var key = getWorkDiaryId(x, y);

    if (key != 0) {
      if (key == -1) {
        displayToast('現在作業中です。', 4000, '');
        return false;
      }
      var cworkdate = selectAccount.workdate.replace("\/", "");
      cworkdate = cworkdate.replace("\/", "");
      //遷移時パラメータの設定
      localStorage.setItem("backMode"         , "1");
      localStorage.setItem("backAccountId"    , selectAccount.accountId);
      localStorage.setItem("backWorkDate"     , cworkdate);
      localStorage.setItem("backWorkDiaryId"  , key);
      window.location.href = "/" + key + "/workDiaryEdit";
    }
  }

  function getWorkDiaryId(x, y) {
    for (var idx in aryData) {
      var data = aryData[idx];
      if (data.sx <= x && x <= data.ex && data.sy <= y && y <= data.ey) {
        return data.id;
      }
    }
    return 0;
  }

  function getAccountWorkdata() {

    $("#workline").empty();

    var ai = $("#accountinfo");

    selectAccount.workdate = $("#select-workdate").val();

    var canvas  = document.getElementById("workline");
    var context = canvas.getContext('2d');

    var pw = ai.width() - 4;
    var ph = 1160;
    $("#workline").attr("width", pw);
    $("#workline").attr("height", ph);
    var w = $("#workline").width();

    context.font = "10px 'メイリオ'";

    var hh = 0;
    var mn = 0;
    for (var i=0; i<48;i++) {
      context.beginPath();
      context.moveTo(32,(i*24+8));
      context.lineTo(w,(i*24+8));
      context.strokeStyle = COLOR_AWTIMELINE;
      context.stroke();
      if ((i % 2) == 0) {
        mn = 0;
      }
      else {
        mn = 30;
      }
      if ((i != 0) && ((i % 2) == 0)) {
        hh++;
      }
      var sh = "";
      var sm = "";
      if (hh < 10) {
        sh = "0" + hh;
      }
      else {
        sh =hh;
      }
      if (mn < 10) {
        sm = "0" + mn;
      }
      else {
        sm = mn;
      }
      context.fillStyle = COLOR_AWTIME;
      context.fillText(sh + ":" + sm, 0, (i*24+12));
      console.log(sh + ":" + sm + "->" + (i*24+8));
    }

    var cworkdate = selectAccount.workdate.replace("\/", "");
    cworkdate = cworkdate.replace("\/", "");

    var url="/" + selectAccount.accountId + "/" + cworkdate + "/getworkdata";
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );

        timeLineList  = jsonResult.targetTimeLine;
        var init = true;
        aryData = [];
        for ( var timeLineKey in timeLineList ) {             //タイムライン件数分処理を行う

          var timeLine    = timeLineList[timeLineKey];    //タイムライン情報の取得
          var timeLineColor = "";

          var code  = timeLine["timeLineColor"];
          var red   = parseInt(code.substring(0,2), 16);
          var green = parseInt(code.substring(2,4), 16);
          var blue  = parseInt(code.substring(4,6), 16);

          const arrivalTime = new String(timeLine["start"]);
          const leavingTime = new String(timeLine["end"]);
          const arrivalHour = arrivalTime.substr(0,2);
          const arrivalMin = arrivalTime.substr(2,2);
          const leavingHour = leavingTime.substr(0,2);
          const leavingMin = leavingTime.substr(2,2);
          const roundedArrTime = new Date(2018,0,1,arrivalHour,Math.floor(arrivalMin/5)*5);
          const truncationLvgTime =  new Date(2018,0,1,leavingHour,Math.floor(leavingMin/5)*5);

          var tkTop = (roundedArrTime.getHours() * 48 + ((roundedArrTime.getMinutes() / 5) * 4)) + 8;
          var tkbtm = (truncationLvgTime.getHours() * 48 + ((truncationLvgTime.getMinutes() / 5) * 4)) + 8;
          var tkHeight = tkbtm - tkTop;

          context.fillStyle = "rgba( " + red + ", " + green + ", " + blue + ", " + TRANS_AWAREA + ")";
          context.fillRect(32, tkTop, w, tkHeight)
          context.beginPath();
          context.moveTo(32,tkTop);
          context.lineTo(w,tkTop);
          context.strokeStyle = "rgba( " + red + ", " + green + ", " + blue + ", 1.0)";
          context.lineWidth = 2;
          context.stroke();
          console.log("SX:32 SY:" + tkTop + " EX" + w + " EY" + (tkTop + tkHeight));
          var jd = {"id":0, "sx":0, "sy": 0, "ex": 0, "ey": 0};
          jd.id = timeLine.workDiaryId;
          jd.sx = 32;
          jd.sy = tkTop;
          jd.ex = w;
          jd.ey = (tkTop + tkHeight);
          aryData.push(jd);
          if (tkHeight > 30) {
            context.font = "12px 'メイリオ'";
            context.fillStyle = COLOR_AWITEM;
            context.fillText(timeLine["workName"], 36, tkTop + 14);
            context.fillStyle = COLOR_AWSUB;
            context.fillText("(" + timeLine["kukakuName"] + ")", 36, tkTop + 28);
          }
          else if (tkHeight > 12) {
            context.font = "12px 'メイリオ'";
            context.fillStyle = COLOR_AWITEM;
            context.fillText(timeLine["workName"] + " (" + timeLine["kukakuName"] + ")", 36, tkTop + 14);
          }
          else if (tkHeight > 8) {
            context.font = "8px 'メイリオ'";
            context.fillStyle = COLOR_AWITEM;
            context.fillText(timeLine["workName"] + " (" + timeLine["kukakuName"] + ")", 36, tkTop + 9);
          }
          console.log(timeLine["workName"] + " (" + timeLine["kukakuName"] + ") -> START " + roundedArrTime + "(" + tkTop + ") END " + truncationLvgTime + "(" + tkbtm + ")");
        } // timeLineList
        canvas.addEventListener('click', onClick, false);
    },
      dataType:'json',
      contentType:'text/json'
    });
  }

})(jQuery);