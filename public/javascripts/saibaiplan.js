(function($){

  $(document).ready(function(){

    $("#saibaiKeikaku").hide();
    $("#editsection").hide();

    $("#PlanEditCropLink").attr("data", userinfo.farm + "/getCrop");
    $("#PlanEditHinsyuLink").attr("data", "getHinsyuOfFarmJson");

    $(".planEditTrigger").unbind("click");
    $(".planEditTrigger").bind("click", onClickEditEvent);

    $("#PlanEditRinsaku").unbind("click");
    $("#PlanEditRinsaku").bind("click", onClickRinsakuEvent);
    $("#PlanEditDelete").unbind("click");
    $("#PlanEditDelete").bind("click", onClickDeleteEvent);
    $("#PlanEditBack").unbind("click");
    $("#PlanEditBack").bind("click", onClickCloseEvent);
    $("#PlanEditCommit").unbind("click");
    $("#PlanEditCommit").bind("click", onClickCommitEvent);
    $("#PlanExcute").unbind("click");
    $("#PlanExcute").bind("click", onClickExcuteEvent);

    $("#PlanEditCropValue").unbind("change");
    $("#PlanEditCropValue").bind("change", hinsyuUpdate);
    $("#PlanEditHashuDate").unbind("change");
    $("#PlanEditHashuDate").bind("change", hinsyuUpdate);

    CalcInit();                                   // 数値入力電卓初期化

  }); //end of document.ready

  var jokenList = []; //想定条件リスト
  var key;
  var init;

  function onClickEditEvent() {
    init = true;
    var my  = $(this);
    key = my.attr("key");
    var sysdate = GetSystemDate();
    $("#editsection").show();

    if (key == "add") {
      selectDataGet("#PlanEditCrop", userinfo.farm + "/getCrop");
      selectClose();
      selectDataGet("#PlanEditHinsyu", "getHinsyuOfFarmJson");
      selectClose();
      $('#PlanEditHashuDate').val(sysdate);
      $('#PlanEditHashuDate').datepicker('setDate', new Date(sysdate));
      $('#PlanEditHashuDateLabel').addClass("active");
      $('#PlanEditDelete').hide();
    }
    else {
      var data = jokenList[key - 1];
      if (data != undefined) {
        selectDataGet("#PlanEditCrop", userinfo.farm + "/getCrop");
        var oJson = selectData(data.cropId);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();
        $('#PlanEditHashuDate').val(data.hashu);
        $('#PlanEditHashuDate').datepicker('setDate', new Date(data.hashu));
        var hashuDate = data.hashu;
        hashuDate = hashuDate.replace(/\//g, '');
        selectDataGet("#PlanEditHinsyu", data.cropId + "/" + hashuDate + "/getHinsyuOfCropHashuJson");
        var oJson = selectData(data.hinsyuId);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();
        $("#PlanEditSakuma").val(data.sakuma);
        $('#PlanEditDelete').show();
      }
    }

    $("#listsection").hide();
    init = false;

  }
  function onClickDeleteEvent() {

    delete jokenList[key - 1];

    var edittr = $("#jsondata" + key);
    edittr.remove();

    $(".planEditTrigger").unbind("click");
    $(".planEditTrigger").bind("click", onClickEditEvent);

    $("#editsection").hide();
    $("#listsection").show();

  }
  function onClickCloseEvent() {

    $("#editsection").hide();
    $("#listsection").show();

  }
  function onClickRinsakuEvent() {

    if (key == "add") {
      var idx = jokenList.length;
      idx++;
      json = {};

      var crop            = selectConvertJson("#PlanEditCrop");
      json["cropId"]      = crop;
      json["cropName"]    = $("#PlanEditCrop").text();
      var hashu           = $("#PlanEditHashuDate").val();
      json["hashu"]       = hashu;
      var hinsyu          = selectConvertJson("#PlanEditHinsyu");
      json["hinsyuId"]    = hinsyu;
      json["hinsyuName"]  = $("#PlanEditHinsyu").text();
      var sakuma          = $("#PlanEditSakuma").val();
      json["sakuma"]      = sakuma;
      json["rinsaku"]     = 1;
      json["idx"]         = idx;
      jokenList.push(json);

      var addBtn = $("#planEditAddBtn");
      const hashus = new String(json.hashu).split("/");
      addBtn.before('<tr id="jsondata' + idx + '" key="' + idx + '" class="planEditTrigger item"><td>(輪作)' + json["cropName"] + '</td><td>' + hashus[1] + '月' + hashus[2] + '日' + '</td><td>' + json["hinsyuName"] + '</td><td>' + json["sakuma"] + '日</td></tr>');

    }
    {
      var json = jokenList[key - 1];
      if (json != undefined) {
        var crop            = selectConvertJson("#PlanEditCrop");
        json["cropId"]      = crop;
        json["cropName"]    = $("#PlanEditCrop").text();
        var hashu           = $("#PlanEditHashuDate").val();
        json["hashu"]       = hashu;
        var hinsyu          = selectConvertJson("#PlanEditHinsyu");
        json["hinsyuId"]    = hinsyu;
        json["hinsyuName"]  = $("#PlanEditHinsyu").text();
        var sakuma          = $("#PlanEditSakuma").val();
        json["sakuma"]      = sakuma;
        json["rinsaku"]     = 1;
        json["idx"]         = idx;
        const hashus = new String(json.hashu).split("/");
        var edittr = $("#jsondata" + key);
        edittr.empty();
        edittr.append('<td>(輪作)' + json["cropName"] + '</td><td>' + hashus[1] + '月' + hashus[2] + '日' + '</td><td>' + json["hinsyuName"] + '</td><td>' + json["sakuma"] + '日</td>');
      }
    }

    $(".planEditTrigger").unbind("click");
    $(".planEditTrigger").bind("click", onClickEditEvent);

    $("#editsection").hide();
    $("#listsection").show();

  }
  function onClickCommitEvent() {

    if (key == "add") {
      var idx = jokenList.length;
      idx++;
      json = {};

      var crop            = selectConvertJson("#PlanEditCrop");
      json["cropId"]      = crop;
      json["cropName"]    = $("#PlanEditCrop").text();
      var hashu           = $("#PlanEditHashuDate").val();
      json["hashu"]       = hashu;
      var hinsyu          = selectConvertJson("#PlanEditHinsyu");
      json["hinsyuId"]    = hinsyu;
      json["hinsyuName"]  = $("#PlanEditHinsyu").text();
      var sakuma          = $("#PlanEditSakuma").val();
      json["sakuma"]      = sakuma;
      json["rinsaku"]     = 0;
      json["idx"]         = idx;
      jokenList.push(json);

      var addBtn = $("#planEditAddBtn");
      const hashus = new String(json.hashu).split("/");
      addBtn.before('<tr id="jsondata' + idx + '" key="' + idx + '" class="planEditTrigger item"><td>' + json["cropName"] + '</td><td>' + hashus[1] + '月' + hashus[2] + '日' + '</td><td>' + json["hinsyuName"] + '</td><td>' + json["sakuma"] + '日</td></tr>');

    }
    {
      var json = jokenList[key - 1];
      if (json != undefined) {
        var crop            = selectConvertJson("#PlanEditCrop");
        json["cropId"]      = crop;
        json["cropName"]    = $("#PlanEditCrop").text();
        var hashu           = $("#PlanEditHashuDate").val();
        json["hashu"]       = hashu;
        var hinsyu          = selectConvertJson("#PlanEditHinsyu");
        json["hinsyuId"]    = hinsyu;
        json["hinsyuName"]  = $("#PlanEditHinsyu").text();
        var sakuma          = $("#PlanEditSakuma").val();
        json["sakuma"]      = sakuma;
        json["rinsaku"]     = 0;
        json["idx"]         = idx;
        const hashus = new String(json.hashu).split("/");
        var edittr = $("#jsondata" + key);
        edittr.empty();
        edittr.append('<td>' + json["cropName"] + '</td><td>' + hashus[1] + '月' + hashus[2] + '日' + '</td><td>' + json["hinsyuName"] + '</td><td>' + json["sakuma"] + '日</td>');
      }
    }

    $(".planEditTrigger").unbind("click");
    $(".planEditTrigger").bind("click", onClickEditEvent);

    $("#editsection").hide();
    $("#listsection").show();

  }
  var cropList;   //生産物栽培計画リスト
  var goalList;   //収穫目標リスト
  var tYear = 0;  //計画対象年
  function onClickExcuteEvent() {

    lockScreen(LOCK_ID);

    var rinsaku = false;

    for (var idx = 0; idx < jokenList.length; idx++) {
      var joken = jokenList[idx];
      if (joken.rinsaku == 1) {
        rinsaku = true;
        break;
      }
    }

    if (rinsaku) {
      alert("輪作条件は直近の播種日を採用し、栽培計画を立てます。");
    }

    var jsondata = {datalist: jokenList};

    var url = "/makePlan";
    $.ajax({
      url:url,
      type:'POST',
      data:JSON.stringify(jsondata),              //入力用JSONデータ
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        if (jsonResult.result == "SUCCESS") {
          cropList = jsonResult.crop;
          goalList = jsonResult.goal;
          tYear    = jsonResult.year;

          var monthList = jsonResult.month;
          var header = $("#saibaiKeikakuHeader");
          header.empty();
          header.append('<tr id="saibaiKeikakuHeader1" class="planTr"></tr>');
          var header1 = $("#saibaiKeikakuHeader1");
          header1.append('<th>月</th>');

          for (var idx in monthList) {
            var month = monthList[idx];
            header1.append('<th colspan="' + month.weekcount +'">' + month.month + '</th>');
          }
          header.append('<tr id="saibaiKeikakuHeader2" class="planTr"><th>週</th></tr>');
          var header2 = $("#saibaiKeikakuHeader2");
          for (var idx in monthList) {
            var month = monthList[idx];
            for (var week = 1; week<=month.weekcount; week++) {
              header2.append('<th>' + week + '</th>');
            }
          }
          var body = $("#saibaiKeikakuBody");
          body.empty();

          //----- 品目の栽培計画 -----
          for (var key in cropList) {
            var crop = cropList[key];
            rotationList = crop.rotationList;
            var init = true;
            for (var idx in rotationList) {
              var rotation = rotationList[idx];
              if (init) {
                body.append('<tr id="saibaiKeikakuBody-' + key + '-' + idx + '" class="planTr"><td class="title" rowspan="' + crop.rotation + '">' + crop.name + '</td></tr>');
              }
              else {
                body.append('<tr id="saibaiKeikakuBody-' + key + '-' + idx + '" class="planTr"></tr>');
              }
              var tr = $('#saibaiKeikakuBody-' + key + '-' + idx);
              for (var week in rotation.monthweek) {
                var data = rotation.monthweek[week];
                var modeColor = "";
                switch (data.mode) {
                case 1:
                  modeColor = "pSaku";
                  break;

                case 2:
                  modeColor = "jSaku";
                  break;

                case 3:
                  modeColor = "pKanri";
                  break;

                case 4:
                  modeColor = "jKanri";
                  break;

                case 5:
                  modeColor = "pShukaku";
                  break;

                case 6:
                  modeColor = "jShukaku";
                  break;

                default:
                  break;
                }
                tr.append('<td class="planWeek workDisplay-trriger ' + modeColor + '" key="' + key + '" rotation="' + idx + '">&nbsp;</td>');
              }
              init = false;
            }
          }
          //----- 収穫目標 -----
          for (var idx in goalList) {
            var goal = goalList[idx];
            body.append('<tr id="saibaiKeikakuGoal-' + idx + '" class="planTr"><td class="title">' + goal.name + '</td></tr>');
            var tr = $('#saibaiKeikakuGoal-' + idx);
            tr.append('<td colspan="' + goal.s01 + '" class="goalEditTrriger" key="' + idx + '" month="01">' + goal.r01 + '<br><br><br><span class="planGoalShukaku">' + goal.m01 + '</span></td>');
            tr.append('<td colspan="' + goal.s02 + '" class="goalEditTrriger" key="' + idx + '" month="02">' + goal.r02 + '<br><br><br><span class="planGoalShukaku">' + goal.m02 + '</span></td>');
            tr.append('<td colspan="' + goal.s03 + '" class="goalEditTrriger" key="' + idx + '" month="03">' + goal.r03 + '<br><br><br><span class="planGoalShukaku">' + goal.m03 + '</span></td>');
            tr.append('<td colspan="' + goal.s04 + '" class="goalEditTrriger" key="' + idx + '" month="04">' + goal.r04 + '<br><br><br><span class="planGoalShukaku">' + goal.m04 + '</span></td>');
            tr.append('<td colspan="' + goal.s05 + '" class="goalEditTrriger" key="' + idx + '" month="05">' + goal.r05 + '<br><br><br><span class="planGoalShukaku">' + goal.m05 + '</span></td>');
            tr.append('<td colspan="' + goal.s06 + '" class="goalEditTrriger" key="' + idx + '" month="06">' + goal.r06 + '<br><br><br><span class="planGoalShukaku">' + goal.m06 + '</span></td>');
            tr.append('<td colspan="' + goal.s07 + '" class="goalEditTrriger" key="' + idx + '" month="07">' + goal.r07 + '<br><br><br><span class="planGoalShukaku">' + goal.m07 + '</span></td>');
            tr.append('<td colspan="' + goal.s08 + '" class="goalEditTrriger" key="' + idx + '" month="08">' + goal.r08 + '<br><br><br><span class="planGoalShukaku">' + goal.m08 + '</span></td>');
            tr.append('<td colspan="' + goal.s09 + '" class="goalEditTrriger" key="' + idx + '" month="09">' + goal.r09 + '<br><br><br><span class="planGoalShukaku">' + goal.m09 + '</span></td>');
            tr.append('<td colspan="' + goal.s10 + '" class="goalEditTrriger" key="' + idx + '" month="10">' + goal.r10 + '<br><br><br><span class="planGoalShukaku">' + goal.m10 + '</span></td>');
            tr.append('<td colspan="' + goal.s11 + '" class="goalEditTrriger" key="' + idx + '" month="11">' + goal.r11 + '<br><br><br><span class="planGoalShukaku">' + goal.m11 + '</span></td>');
            tr.append('<td colspan="' + goal.s12 + '" class="goalEditTrriger" key="' + idx + '" month="12">' + goal.r12 + '<br><br><br><span class="planGoalShukaku">' + goal.m12 + '</span></td>');
          }

          $(".goalEditTrriger").unbind("click");
          $(".goalEditTrriger").bind("click", onClickgoalShukakuEvent);
          $(".workDisplay-trriger").unbind("click");
          $(".workDisplay-trriger").bind("click", onClickWorkDisplayEvent);
          $("#saibaiKeikaku").show();

        }
        else {
          displayToast('栽培計画に必要なデータがありません。', 2000, 'rounded');
        }

        unlockScreen(LOCK_ID);

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });

  }
  var egsData = {id: 0, name: "", shukakuryo: 0, goalshukaku: 0, year: 0, month: 0};
  function onClickgoalShukakuEvent() {

    lockScreen(LOCK_ID);

    var my    = $(this);
    var idx   = my.attr("key");
    var month = my.attr("month");
    var goal  = goalList[idx];

    egsData.id            = goal["id"];
    egsData.name          = goal["name"];
    egsData.shukakuryo    = Number.parseFloat(goal["r" + month]);
    egsData.goalshukaku   = goal["m" + month];
    egsData.year          = tYear;
    egsData.month         = Number.parseInt(month);

    var divTag = $('<div />').attr("id", "editGoalArea");
    divTag.addClass("editGoalArea");

    $('body').append(divTag);
    var area = $("#editGoalArea");
    area.append('<span class="title">目標設定</span>')
    area.append('<span class="crop">' + egsData.name + '</span>')
    area.append('<span class="shukakuryo">収穫量：' + egsData.shukakuryo + '</span>')
    area.append('<div class="input-field goalShukaku"><input id="editGoalShukaku" type="text" class="validate input-text-color" maxlength="6" style="text-align: right;" value="' + egsData.goalshukaku + '"><label for="editGoalShukaku">目標収穫量を入力してください</label><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="editGoalShukaku">keyboard</i></div>')
    area.append('<a id="editGoalChancel" class="waves-effect waves-teal btn-flat cancel">戻&nbsp;&nbsp;&nbsp;&nbsp;る</a>')
    area.append('<a id="editGoalCommit" class="waves-effect waves-teal btn-flat commit">確&nbsp;&nbsp;&nbsp;&nbsp;定</a>')

    CalcInit();                                   // 数値入力電卓初期化

    $("#editGoalChancel").unbind("click");
    $("#editGoalChancel").bind("click", onClickEditGoalCancel);
    $("#editGoalCommit").unbind("click");
    $("#editGoalCommit").bind("click", onClickEditGoalCommit);

  }
  function onClickEditGoalCancel() {
    var area = $("#editGoalArea");
    area.remove();
    unlockScreen(LOCK_ID);
  }
  function onClickEditGoalCommit() {

    if (isFinite($("#editGoalShukaku").val())) {
      egsData.goalshukaku   = Number.parseFloat($("#editGoalShukaku").val());
      var url = "/commitGoalPlan";
      $.ajax({
        url:url,
        type:'POST',
        data:JSON.stringify(egsData),
        complete:function(data, status, jqXHR){
          var jsonResult = JSON.parse( data.responseText );
          var area = $("#editGoalArea");
          area.remove();
          unlockScreen(LOCK_ID);
        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });
    }
    else {

    }

  }
  var planWorkKey = 0;
  var planWorkIdx = 0;
  function onClickWorkDisplayEvent() {

    lockScreen(LOCK_ID);

    var my          = $(this);
    var key         = my.attr("key");
    var rotationIdx = my.attr("rotation");
    var crop        = cropList[key];
    var rotation    = crop.rotationList[rotationIdx];

    planWorkKey = key;
    planWorkIdx = rotationIdx;

    var divTag = $('<div />').attr("id", "planWorkArea");
    divTag.addClass("planWorkArea");

    $('body').append(divTag);
    var area = $("#planWorkArea");
    area.append('<span class="title">想定作業内容</span>')
    area.append('<span class="crop">' + rotation.name + '(' + rotation.hinsyu + ')</span>')
    area.append('<span class="seiiku">生育日数：' + rotation.seiiku + '日</span>')
    area.append('<table class="planTable"><thead class="planHeader" id="planWorkHeader" /><tbody class="planBody list workList" id="planWorkBody" /></table>')
    var header = $("#planWorkHeader");
    header.append('<tr id="" class="planTr"><th>作業内容</th><th>作業日</th></tr>');

    var workList = rotation.work;
    var body = $("#planWorkBody");
    for (var workKey in workList) {
      var work = workList[workKey];
      body.append('<tr class="planTr"><td class="">' + work.name + '</td><td class="">' + work.date + '</td></tr>');
    }

    area.append('<br /><table class="planTable"><thead class="planHeader" id="planTimeHeader" /><tbody class="planBody workList" id="planTimeBody" /></table>')
    header = $("#planTimeHeader");
    header.append('<tr id="" class="planTr"><th colspan="2">想定作業時間</th></tr>');
    body = $("#planTimeBody");
    body.append('<tr class="planTr"><td class="">作&nbsp;&nbsp;付</td><td class="">' + rotation.sakuduke + '&nbsp;&nbsp;分</td></tr>');
    body.append('<tr class="planTr"><td class="">管&nbsp;&nbsp;理</td><td class="">' + rotation.kanri + '&nbsp;&nbsp;分</td></tr>');
    body.append('<tr class="planTr"><td class="">収&nbsp;&nbsp;穫</td><td class="">' + rotation.shukaku + '&nbsp;&nbsp;分</td></tr>');

    //----- 区画一覧 -----
    area.append('<span class="selectmodal-trigger-title">栽培計画の反映先</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="栽培計画の反映先" data="getCompartmentOfFarm" displayspan="#planTimeKukaku"><span id="planTimeKukaku" class="blockquote-input">未選択</span></a>');
    selectDataGet("#planTimeKukaku", "getCompartmentOfFarm");
    $('.mselectmodal-trigger').unbind('click');
    $('.mselectmodal-trigger').bind('click', mSelectOpen);

    area.append('<a id="planWorkChancel" class="waves-effect waves-teal btn-flat cancel">戻&nbsp;&nbsp;&nbsp;&nbsp;る</a>')
    area.append('<a id="planWorkCommit" class="waves-effect waves-teal btn-flat commit">計画を反映する</a>')

    $("#planWorkChancel").unbind("click");
    $("#planWorkChancel").bind("click", onClickplanWorkCancel);
    $("#planWorkCommit").unbind("click");
    $("#planWorkCommit").bind("click", onClickplanWorkCommit);

  }
  function onClickplanWorkCancel() {
    var area = $("#planWorkArea");
    area.remove();
    unlockScreen(LOCK_ID);
  }
  function onClickplanWorkCommit() {

    var jsonData = {};

    jsonData["kukaku"]  = mSelectConvertJson("#planTimeKukaku");
    jsonData["work"]    = cropList[planWorkKey].rotationList[planWorkIdx].work;

    if (jsonData.kukaku == "") {
      displayToast('反映先区画を選択してください。', 2000, 'rounded');
      return false;
    }

    var url = "/commitPlanWork";
    $.ajax({
      url:url,
      type:'POST',
      data:JSON.stringify(jsonData),
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );
        displayToast('栽培計画の内容を指示書に反映しました', 2000, 'rounded');
      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });

  }
  function hinsyuUpdate() {
    if (init == false) {
      var crop = $("#PlanEditCropValue").val();
      var hashuDate = $("#PlanEditHashuDate").val();
      hashuDate = hashuDate.replace(/\//g, '');
      if (crop == "" || hashuDate == "") {
        return false;
      }
      selectDataGet("#PlanEditHinsyu", crop + "/" + hashuDate + "/getHinsyuOfCropHashuJson");

      $("#PlanEditHinsyu").text("未選択");
      displayToast('品種一覧が更新されました', 2000, 'rounded');
    }
  }
})(jQuery);