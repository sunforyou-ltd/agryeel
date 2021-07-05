(function($){

  $(document).ready(function(){

    $("#editsection").hide();

    $("#PlanEditCropLink").attr("data", userinfo.farm + "/getCrop");
    $("#PlanEditHinsyuLink").attr("data", "getHinsyuOfFarmJson");

    $(".planEditTrigger").unbind("click");
    $(".planEditTrigger").bind("click", onClickEditEvent);

    $("#PlanEditBack").unbind("click");
    $("#PlanEditBack").bind("click", onClickCloseEvent);
    $("#PlanEditCommit").unbind("click");
    $("#PlanEditCommit").bind("click", onClickCommitEvent);
    $("#PlanExcute").unbind("click");
    $("#PlanExcute").bind("click", onClickExcuteEvent);

    CalcInit();                                   // 数値入力電卓初期化

  }); //end of document.ready

  var jokenList = []; //想定条件リスト
  var key;

  function onClickEditEvent() {
    var my  = $(this);
    key = my.attr("key");
    var sysdate = GetSystemDate();

    if (key == "add") {
      selectDataGet("#PlanEditCrop", userinfo.farm + "/getCrop");
      selectClose();
      selectDataGet("#PlanEditHinsyu", "getHinsyuOfFarmJson");
      selectClose();
      $('#PlanEditHashuDate').datepicker('setDate', new Date(sysdate));
      $('#PlanEditHashuDate').val(sysdate);
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
        $('#PlanEditHashuDate').datepicker('setDate', new Date(data.hashu));
        $('#PlanEditHashuDate').val(data.hashu);
        selectDataGet("#PlanEditHinsyu", "getHinsyuOfFarmJson");
        var oJson = selectData(data.hinsyuId);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();
        $("#PlanEditSakuma").val(data.sakuma);
      }
    }

    $("#editsection").show();

  }
  function onClickCloseEvent() {

    $("#editsection").hide();

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

  }
  function onClickExcuteEvent() {

    var url = "/makePlan";
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){
        var jsonResult = JSON.parse( data.responseText );

        var monthList = jsonResult.month;
        var header = $("#saibaiKeikakuHeader");
        header.append('<tr id="saibaiKeikakuHeader1" class="planTr"><th>月</th></tr>');
        var header1 = $("#saibaiKeikakuHeader1");

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
        var cropList = jsonResult.crop;
        var body = $("#saibaiKeikakuBody");

        for (var idx in cropList) {
          var crop = cropList[idx];
          body.append('<tr id="saibaiKeikakuBody-' + idx + '" class="planTr"><td class="title">' + crop.name + '</td></tr>');
          var tr = $('#saibaiKeikakuBody-' + idx);
          for (var week in crop.monthweek) {
            var data = crop.monthweek[week];
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
            tr.append('<td class="planWeek ' + modeColor + '">&nbsp;</td>');
          }
        }

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });

  }

})(jQuery);
function dragStart(event){
  event.dataTransfer.setData("text", event.target.getAttribute("key"));
}
