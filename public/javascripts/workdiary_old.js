﻿/* AGRYEEL 作業対象圃場画面 JQUERY */
(function($){

  var maxSanpuIndex 		= 0;														//最大散布インデックス
  var maxNouhiIndex    		= 0;														//最大農肥インデックス
  var targetSanpuIndex 		= 0;														//処理対象散布インデックス
  var targetNouhiIndex 		= 0;														//処理対象農肥インデックス
  var targetKikiIndex 		= 0;														//処理対象機器インデックス

  var kikiDataList      	= [];														//機器一覧データリスト
  var attachmentDataList	= {};														//アタッチメントデータリスト一覧
  var nowproc				= false;													//処理中フラグ
  var workdate              = "";

  var workinfo = {workid: 0, worktemplateid: 0, kukakuid: 0, workdateautoset: 0, init: true};

  var oCropInfo = {kukakuId:0, cropId: 0, cropName: ""};

  var oInputJson = {};
  var workPlanId = 0;

  /* 初期処理時イベント */
  $(function(){

    /*----- 各ボタンにクリックイベントを押下する -----*/

    $(document).ready(function(){
      $('.collapsible').collapsible({
        accordion : false // A setting that changes the collapsible behavior to expandable instead of the default accordion style
      });

      //$('.datepicker').pickadate({selectMonths: true, selectYears: 15});  //MaterializeVerUP.
      $("#G0005WorkDate").val(workdate);

      /*----- 各ボタンにクリックイベントを押下する -----*/
      $('#G0005SubmitButton').bind('click', SubmitTap);							//日誌記録ボタン
      $('#G0005BackButton').bind('click', BackTap);             //日誌記録ボタン
      $('.kiki-value-change').bind('change', ChangeAttachmentLink);	//機器選択時
      $('.nouhi-value-change').bind('change', GetNouhiValue);					//農肥選択時
      $('#G0005SanpuInfoAddBtn').bind('click', AddSunpuInfoEvent);				//散布情報追加時
      $('.info-delete').bind('click', DeleteInfoEvent);							//情報削除時
      $('.nouhi-commit-btn').bind('click', NouhiInfoCommitEvent);				//農肥確定イベント時
      $('#G0005DetailSettingKind').bind('change', ChangeG0005DetailSetting);	//詳細設定種別変更時
      $('.nisugata-value').bind('change', GetNisugataInfo);             //荷姿選択時
      $('.kosu-value').bind('change', CalShukakuryo);                   //個数変更時
      $('.shukaku-clear').bind('click', onShukakuClear);                //収穫情報クリアボタン押下時
      $('.all-shukaku-clear').bind('click', onShukakuAllClear);          //全ての収穫情報クリアボタン押下時

      /*------ コンビネーションの初期表示を手動にする -----*/
      $("#G0005CombiInfo").hide();

    });

//  var inputJson = StringToJson('{"accountId":"' + accountInfo.accountId + '", "farmId":"' + accountInfo.farmId + '"}');

      $.ajax({
        url:"/workDiaryInit", 												//作業対象圃場初期処理
          type:'GET',
//        data:JSON.stringify(inputJson),									//入力用JSONデータ
          complete:function(data, status, jqXHR){							//処理成功時
          var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
          var htmlString	= "";											//可変HTML文字列
          var systemDate = new Date( jQuery.now() ).toLocaleString();

          workinfo.workid           = jsonResult["workId"];
          workinfo.worktemplateid   = jsonResult["workTemplateId"];
          workinfo.kukakuid         = jsonResult["kukakuId"];
          workinfo.workdateautoset  = jsonResult["workdateautoset"];

          //----- 共通項目ヘッダ部の生成 START -----
          htmlString	= "";												//可変HTML文字列を初期化

          htmlString+= MakeCommonHeaderItem(jsonResult);					//共通項目ヘッダの生成

          //----- 共通項目ヘッダ部の生成 END -----

          //----- 作業項目の生成 START -----
          switch(jsonResult["workTemplateId"]) {
          case 1: //通常作業

            htmlString+= MakeKatadukeItem(jsonResult);
            break;

          case 2: //散布作業

            htmlString+= MakeHiryoSanpuItem(jsonResult);
            break;

          case 3: //播種作業

            htmlString+= MakeHashuItem(jsonResult);
            break;

          case 4://収穫作業

            htmlString+= MakeShukakuItem(jsonResult);
            break;

          case 5: //農耕作業

            htmlString+= MakeTagayasuItem(jsonResult);
            break;

          case 6://潅水作業

              htmlString+= MakeKansuiItem(jsonResult);
              break;

          case 8://回収作業

            htmlString+= MakeKaishuItem(jsonResult);
            break;

          case 9://脱着作業

            htmlString+= MakeDachakuItem(jsonResult);
            break;

          case 10://コメント作業

            htmlString+= MakeCommentItem(jsonResult);
            break;

          case 11://マルチ作業

            htmlString+= MakeMultiItem(jsonResult);
            break;

          case 12://定植作業

            htmlString+= MakeTeishokuItem(jsonResult);
            break;

          case 13://苗播種作業

            htmlString+= MakeNaehashuItem(jsonResult);
            break;

          case 15://剪定作業

            htmlString+= MakeSenteiItem(jsonResult);
            break;

          }
          //----- 作業項目の生成 END -----

          //----- 共通項目フッタ部の生成 START -----
          htmlString+= '<div class="row">';
          //作業時間
          htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<span class="item-title">作業時間</span><span id="G0005WorkTimeSpan" class="item">' + jsonResult["workTime"] + '</span><span class="item">分</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005WorkTime">keyboard</i>';
          htmlString+= '<input type="hidden" id="G0005WorkTime" value="' + jsonResult["workTime"] + '"/>';
          htmlString+= '</div>';
          //作業開始日時の作成
          htmlString+= '<div class="col s12 wd">';
          htmlString+= '<span class="item-title">作業開始日時</span>';
          htmlString+= '</div>';
          htmlString+= '<div class="col s6 input-field wd">';
          htmlString+= '<input type="text" placeholder="開始日付" id="G0005StartDate" class="datepicker item input-text-color" style="" value="' + jsonResult["startDate"] + '">';
          htmlString+= '</div>';
          htmlString+= '<div class="col s6 input-field wd">';
          htmlString+= '<input type="text" placeholder="開始時間" id="G0005StartTime" class="timepicker item input-text-color" style="" value="' + jsonResult["startTime"] + '">';
          htmlString+= '</div>';
          //作業終了日時の作成
          htmlString+= '<div class="col s12 wd">';
          htmlString+= '<span class="item-title">作業終了日時</span>';
          htmlString+= '</div>';
          htmlString+= '<div class="col s6 input-field wd">';
          htmlString+= '<input type="text" placeholder="終了日付" id="G0005EndDate" class="datepicker item input-text-color" style="" value="' + jsonResult["endDate"] + '">';
          htmlString+= '</div>';
          htmlString+= '<div class="col s6 input-field wd">';
          htmlString+= '<input type="text" placeholder="終了時間" id="G0005EndTime" class="timepicker item input-text-color" style="" value="' + jsonResult["endTime"] + '">';
          htmlString+= '</div>';
          //担当者の生成
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<span class="selectmodal-trigger-title item-title">担当者</span><a href="#selectmodal"  class="selectmodal-trigger item" title="担当者選択" data="getAccountOfFarm" displayspan="#G0005WorkAccountSpan" htext="#G0005WorkAccountValue"><span id="G0005WorkAccountSpan">' + jsonResult["accountName"] + '</span></a>';
          htmlString+= '<input type="hidden" id="G0005WorkAccountValue" value="' + jsonResult["accountId"] + '"/>';
          htmlString+= '</div>';
          htmlString+= '</div>';
          htmlString+= '</div>';

          //作業IDの保存
          htmlString+= '<input type="hidden" id="G0005WorkId" value="' + jsonResult["workId"] + '"/>';

          //作業記録ＩＤの保存
          htmlString+= '<input type="hidden" id="G0005WorkDiaryId" value="' + jsonResult["workDiaryId"] + '"/>';

          //----- 共通項目フッタ部の生成 END -----

          $("#G0005WorkDiary").html(htmlString);							//可変HTML部分に反映する

          CalcInit();														   //数値入力電卓初期化

          //------------------------------------------------------------------------------------------------------------------
          //- 生産物変更時（播種入力専用）
          //------------------------------------------------------------------------------------------------------------------
          var crop = $("#G0005CropValue");
          if(crop != undefined) {
            crop.unbind('change');
            crop.bind('change', selectKukakuCrop);
          }
          //------------------------------------------------------------------------------------------------------------------
          //- 削除ボタンの制御
          //------------------------------------------------------------------------------------------------------------------
          if(jsonResult["workDiaryId"] == "") {
            $('#G0005DeleteButton').hide();
          }
          else {
            $('#G0005DeleteButton').show();
          }
          $('#G0005DeleteButton').unbind('click');
          $('#G0005DeleteButton').bind('click', workdiaryDelete);
          //------------------------------------------------------------------------------------------------------------------
          //- 複数区画反映
          //------------------------------------------------------------------------------------------------------------------
          //----- 区画-----
          mSelectDataGet("#G0005WorkKukakuSpan", "getCompartmentOfFarm");
          var fgs = new String(jsonResult.kukakuId).split(",");
          for (var key in fgs) {
            var data = fgs[key];
            var oJson = mSelectData(data);
            if (oJson != undefined) {
              oJson.select = true;
            }
          }
          mSelectClose();
          //------------------------------------------------------------------------------------------------------------------
          //- 複数品種情報反映
          //------------------------------------------------------------------------------------------------------------------
          //----- 圃場グループ -----
          switch(jsonResult["workTemplateId"]) {
          case 3: //播種作業

            mSelectDataGet("#G0005HinsyuSpan", workinfo.kukakuid + "/getHinsyuOfCropJson");
            var fgs = new String(jsonResult.hinsyuId).split(",");
            for (var key in fgs) {
              var data = fgs[key];
              var oJson = mSelectData(data);
              if (oJson != undefined) {
                oJson.select = true;
              }
            }
            mSelectClose();

          }
          //------------------------------------------------------------------------------------------------------------------
          //- 作業開始ボタンの制御
          //------------------------------------------------------------------------------------------------------------------
          if(jsonResult["workDiaryId"] == "") {
            $('#G0005StartButton').show();
          }
          else {
            $('#G0005StartButton').hide();
          }
          switch(jsonResult["workTemplateId"]) {
          case 7: //作付開始の場合

            $('#G0005StartButton').hide();
            break;

          }
          $('#G0005StartButton').unbind('click');
          $('#G0005StartButton').bind('click', workStart);
          //------------------------------------------------------------------------------------------------------------------
          //- 作業計画ボタンの制御
          //------------------------------------------------------------------------------------------------------------------
          if(jsonResult["workDiaryId"] == "") {
            $('#G0005PlanButton').show();
          }
          else {
            $('#G0005PlanButton').hide();
          }
          $('#G0005PlanButton').unbind('click');
          $('#G0005PlanButton').bind('click', workPlanCommit);
          //------------------------------------------------------------------------------------------------------------------
          //- 作業切替一覧の制御
          //------------------------------------------------------------------------------------------------------------------
          //----- 作業切替-----
          if (userinfo.change == 0) {
            selectDataGet("#G0005WorkReorder", workinfo.kukakuid + "/getWorkOfKukaku");
          }
          else {
            selectDataGet("#G0005WorkReorder", "getWorkOfFarm");
          }
          $('#G0005WorkReorderValue').unbind('change');
          $('#G0005WorkReorderValue').bind('change', workChange);
          //------------------------------------------------------------------------------------------------------------------
          //- モーダルの初期化
          //------------------------------------------------------------------------------------------------------------------
          $('.modal').modal();
          //------------------------------------------------------------------------------------------------------------------
          //- セレクトモーダルの初期化
          //------------------------------------------------------------------------------------------------------------------
          $('.mselectmodal-trigger').unbind('click');
          $('.mselectmodal-trigger').bind('click', mSelectOpen);
          $('.selectmodal-trigger').unbind('click');
          $('.selectmodal-trigger').bind('click', selectOpen);
          //------------------------------------------------------------------------------------------------------------------
          //- 作業時間を再度設定する
          //------------------------------------------------------------------------------------------------------------------
          $("#G0005WorkTimeSpan").text(jsonResult["workTime"]);
          $("#G0005WorkTime").val(jsonResult["workTime"]);
          //------------------------------------------------------------------------------------------------------------------
          //- 作業日付,作業開始日時、作業終了日時のイベント設定
          //------------------------------------------------------------------------------------------------------------------
          $('#G0005WorkDate').unbind('change');
          $('#G0005WorkDate').bind('change', autoWorktime);
          $('#G0005StartDate').unbind('change');
          $('#G0005StartDate').bind('change', autoWorktime);
          $('#G0005StartTime').unbind('change');
          $('#G0005StartTime').bind('change', autoWorktime);
          $('#G0005EndDate').unbind('change');
          $('#G0005EndDate').bind('change', autoWorktime);
          $('#G0005EndTime').unbind('change');
          $('#G0005EndTime').bind('change', autoWorktime);
          workinfo.init = false;
          //------------------------------------------------------------------------------------------------------------------
          //- 作業計画ＩＤの保存
          //------------------------------------------------------------------------------------------------------------------
          if (jsonResult.workPlanId == "") {
            workPlanId = 0;
          }
          else {
            workPlanId = parseInt(jsonResult.workPlanId);
          }
          //------------------------------------------------------------------------------------------------------------------
          //- 遷移後イベント
          //------------------------------------------------------------------------------------------------------------------
          if (localStorage.getItem("backMode") == "0") {
            localStorage.setItem("backMode"         , null);
            localStorage.setItem("backKukakuId"     , null);
            localStorage.setItem("backWorkId"       , null);
          }
          },
          dataType:'json',
          contentType:'text/json',
          async: false
        });

  }); // end of document ready

  //------------------------------------------------------------------------------------------------------------------
  //- 作業開始を実行する
  //------------------------------------------------------------------------------------------------------------------
  function workStart() {
    if (confirm("作業を開始します。よろしいですか？")) {
      oInputJson = {};
      oInputJson["workPlanType"] = 1; //作業記録からストップウォッチにて記録
      if (dataToJson() == false) {
        return false;
      }
      var url = "/checkNouhi";

      $.ajax({
        url:url,                        //作業日誌保存処理
        type:'POST',
        data:JSON.stringify(oInputJson),              //入力用JSONデータ
        complete:function(data, status, jqXHR){       //処理成功時

          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
          if (jsonResult.result != 0) {
            var result = window.confirm('下記の警告が発生しています。\n作業を開始しますか？\n' + jsonResult.message);
            if (result) {
              SubmitWorkPlan(oInputJson);                      //作業日誌保存処理
            }
          }
          else {
            SubmitWorkPlan(oInputJson);                      //作業日誌保存処理
          }
        },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
    }
  }
  /* 入力した内容を作業計画に保存する */
  function SubmitWorkPlan(jsondata){

    if (nowproc == true) {

          displayToast('現在、作業記録の保存中です', 4000, 'rounded');
          return;

    }

    nowproc = true;

    jsondata["workPlanId"] = workPlanId;

    var url = "/submitWorkPlan";

    /* 登録アニメーション実行 */
    $('.workdiary-commit').addClass("commit");

    $.ajax({
        url:url,                        //作業日誌保存処理
        type:'POST',
        data:JSON.stringify(jsondata),              //入力用JSONデータ
        complete:function(data, status, jqXHR){         //処理成功時

          nowproc = false;

          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

          if (jsonResult.result == 'SUCCESS') {
            if (localStorage.getItem("backMode") == "5") {
              window.location.href = "/workPlanMove";
            }
            else if (localStorage.getItem("backMode") == "6") {
              window.location.href = "/workPlanAccountMove";
            }
            else {
              localStorage.setItem("backMode"         , null);
              localStorage.setItem("backWorkId"       , null);
              localStorage.setItem("backKukakuId"     , null);
              localStorage.setItem("backFieldGroupId" , null);
              window.location.href = "/menuMove";
            }
          }
        },
        error:function(data, status, jqXHR){                //処理成功時

          nowproc = false;
          displayToast('作業記録の開始に失敗しました', 4000, 'rounded');

        },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
  } // end of SubmitWorkDiary
  //------------------------------------------------------------------------------------------------------------------
  //- 作業計画を保存する
  //------------------------------------------------------------------------------------------------------------------
  function workPlanCommit() {
    if (confirm("作業指示を保存します。よろしいですか？")) {
      oInputJson = {};
      oInputJson["workPlanType"] = 2; //作業記録から作業計画として記録
      if (dataToJson() == false) {
        return false;
      }
      SubmitWorkPlan(oInputJson);     //作業日誌保存処理
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 作業切替を実行する
  //------------------------------------------------------------------------------------------------------------------
  function workChange() {
    var work = $('#G0005WorkReorderValue').val();
    if (work != 0) {
      var kukaku = $("#G0005WorkKukakuValue").val();
      if (kukaku != "") {
        //作業切替時はバックモードを0にする
        localStorage.setItem("backMode"         , "0");
        localStorage.setItem("backKukakuId"     , kukaku);
        localStorage.setItem("backWorkId"       , work);
        localStorage.setItem("backFieldGroupId" , null);
        window.location.href = '/' + work + '/' + kukaku + '/workDiaryMove';
        return false;
//        var kukakus = kukaku.split(",");
//        for (var key in kukakus) {
//          var data = kukakus[key];
//          window.location.href = '/' + work + '/' + data + '/workDiaryMove';
//          return false;
//        }
      }
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 作業日付自動設定
  //------------------------------------------------------------------------------------------------------------------
  function dateAutoSet(obj) {
    //初期処理中は何もしない
    if (workinfo.init) {
      return;
    }
    //作業日付、作業開始日付、作業終了日付以外は処理対象外
    if((obj.attr("id") != "G0005WorkDate")
        && (obj.attr("id") != "G0005StartDate")
        && (obj.attr("id") != "G0005EndDate")) {
      return;
    }
    switch (workinfo.workdateautoset) {
    case 1:
      //作業開始日付→作業日付、作業終了日付
      if(obj.attr("id") == "G0005StartDate") {
        $("#G0005WorkDate").val($("#G0005StartDate").val());
        $('#G0005WorkDate').datepicker('setDate', new Date($("#G0005WorkDate").val()));
        $("#G0005EndDate").val($("#G0005StartDate").val());
        $('#G0005EndDate').datepicker('setDate', new Date($("#G0005EndDate").val()));
        displayToast("「作業開始日付→作業日付、作業終了日付」にて自動設定しました。", 2000, '');
        return;
      }
      break;
    case 2:
      //作業日付→作業開始日付、作業終了日付
      if(obj.attr("id") == "G0005WorkDate") {
        $("#G0005StartDate").val($("#G0005WorkDate").val());
        $('#G0005StartDate').datepicker('setDate', new Date($("#G0005StartDate").val()));
        $("#G0005EndDate").val($("#G0005WorkDate").val());
        $('#G0005EndDate').datepicker('setDate', new Date($("#G0005EndDate").val()));
        displayToast("「作業日付→作業開始日付、作業終了日付」にて自動設定しました。", 2000, '');
        return;
      }
      break;
    case 3:
      //作業終了日付→作業開始日付
      if(obj.attr("id") == "G0005EndDate") {
        $("#G0005StartDate").val($("#G0005EndDate").val());
        $('#G0005StartDate').datepicker('setDate', new Date($("#G0005StartDate").val()));
        displayToast("「作業終了日付→作業開始日付」にて自動設定しました。", 2000, '');
        return;
      }
      break;
    case 4:
      //作業終了日付→作業日付、作業開始日付
      if(obj.attr("id") == "G0005EndDate") {
        $("#G0005WorkDate").val($("#G0005EndDate").val());
        $('#G0005WorkDate').datepicker('setDate', new Date($("#G0005WorkDate").val()));
        $("#G0005StartDate").val($("#G0005EndDate").val());
        $('#G0005StartDate').datepicker('setDate', new Date($("#G0005StartDate").val()));
        displayToast("「作業終了日付→作業日付、作業開始日付」にて自動設定しました。", 2000, '');
        return;
      }
      break;
    default:
      //作業開始日付→作業日付
      if(obj.attr("id") == "G0005StartDate") {
        $("#G0005WorkDate").val($("#G0005StartDate").val());
        $('#G0005WorkDate').datepicker('setDate', new Date($("#G0005WorkDate").val()));
        displayToast("「作業開始日付→作業日付」にて自動設定しました。", 2000, '');
      }
      break;
    }
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 作業開始、作業終了日時から作業時間を自動算出する
  //------------------------------------------------------------------------------------------------------------------
  var initFlag = 3;
  function autoWorktime() {
    //----- 当画面の入力項目情報定義 -----
    var checktarget = [
        { "id" : "G0005WorkDate"        , "name" : "作業日付"     , "json" : "workDate"       , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005StartDate"       , "name" : "作業開始日付"  , "json" : "startDate"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005StartTime"       , "name" : "作業開始時間"  , "json" : "startTime"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005EndDate"         , "name" : "作業終了日付"  , "json" : "endDate"        , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005EndTime"         , "name" : "作業終了時間"  , "json" : "endTime"        , "check" : { "required"  : "1"}}
    ];
    if (initFlag > 0) {
      initFlag--;
      return false;
    }
    //----- 単項目チェックにて必須チェック -----
    if (InputDataManager(checktarget) == false) {
      return;
    }

    //----- イベント対象が作業開始日付の場合 -----
    dateAutoSet($(this));

    //----- FromToチェック-----
    var wStart = new Date($("#G0005StartDate").val() + " " + $("#G0005StartTime").val());
    var wEnd   = new Date($("#G0005EndDate").val() + " " + $("#G0005EndTime").val());

    console.log(wStart);
    console.log(wEnd);

    if (wEnd < wStart) {
      displayToast("作業終了日時が作業開始日時以前に設定されている為、\n作業時間の自動算出は行いません。", 4000, '');
      return;
    }

    //----- 作業時間算出-----
    var diff = wEnd - wStart;
    diff = Math.floor(diff / (1000 * 60))
    $("#G0005WorkTime").val(diff);
    $("#G0005WorkTimeSpan").text(diff);

  }

  function workdiaryDelete() {
    if (confirm("この作業記録を削除しますがよろしいですか？")) {
      var url = "/" + $("#G0005WorkDiaryId").val() + "/workDiaryDelete";

      $.ajax({
          url:url,                          //グループ農場取得処理
          type:'GET',
          complete:function(data, status, jqXHR){           //処理成功時

            if (localStorage.getItem("backMode") == "1") {
              //担当者別作業一覧に遷移
              window.location.href = "/workingaccountmove";
            }
            else if (localStorage.getItem("backMode") == "5") {
              //作業指示画面に遷移
              window.location.href = "/workPlanMove";
            }
            else if (localStorage.getItem("backMode") == "6") {
              window.location.href = "/workPlanAccountMove";
            }
            else {
              window.location.href = "/menuMove";
            }

          },
          dataType:'json',
          contentType:'text/json'
        });
    }
  }

  function BackTap() {
    if (localStorage.getItem("backMode") == "1") {
      //担当者別作業一覧に遷移
      window.location.href = "/workingaccountmove";
    }
    else if (localStorage.getItem("backMode") == "5") {
      //作業指示画面に遷移
      window.location.href = "/workPlanMove";
    }
    else if (localStorage.getItem("backMode") == "6") {
      window.location.href = "/workPlanAccountMove";
    }
    else {
      window.location.href = "/menuMove";
    }
  }

  /* 共通項目ヘッダを生成する */
  function MakeCommonHeaderItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      //作業記録日誌タイトルの生成
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12">';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">'+ jsonResult["workName"] +'日誌</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      //日付の生成
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">作業日付</span><input type="text" placeholder="作業日付" id="G0005WorkDate" class="datepicker item input-text-color" style="" value="' + jsonResult["workDate"] + '">';
      htmlString+= '</div>';
      workdate   = jsonResult["workDate"];


      //場所の生成
      htmlString+= '<div class="col s12 input-field wd">';
      if (jsonResult["kukakuId"] == 0) {
          htmlString+= '<span class="selectmodal-trigger-title item-title">区画</span><a href="#mselectmodal"  class="mselectmodal-trigger item" title="区画選択" data="getCompartmentOfFarm" displayspan="#G0005WorkKukakuSpan" htext="#G0005WorkKukakuValue"><span id="G0005WorkKukakuSpan">未選択</span></a>';
          htmlString+= '<input type="hidden" id="G0005WorkKukakuValue" />';
      }
      else {
          htmlString+= '<span class="selectmodal-trigger-title item-title">区画</span><a href="#mselectmodal"  class="mselectmodal-trigger item" title="区画選択" data="getCompartmentOfFarm" displayspan="#G0005WorkKukakuSpan" htext="#G0005WorkKukakuValue"><span id="G0005WorkKukakuSpan">' + jsonResult["kukakuName"] + '</span></a>';
          htmlString+= '<input type="hidden" id="G0005WorkKukakuValue" value="' + jsonResult["kukakuId"] + '"/>';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</div>';

      return htmlString;

  }

  /* 初期表示農肥情報を生成する */
  function MakeInitNouhiInfo(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */
      var makeHistry	= false;

      //コンビネーションの生成
      htmlString+= '<div class="row" id="G0005CombiInfo">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4">';
      htmlString+= '<div class="card workdiary-commit">';
      htmlString+= '<div class="card-content">';
      htmlString+= '<blockquote><span>コンビネーション<a href="#G0005ModalCombi" id="G0005Combi" class="collection-item modal-trigger" combiId=""><span id="G0005CombiSpan" class="blockquote-input">未選択</span></a></blockquote>';
      htmlString+= '<input type="hidden" id="G0005CombiValue" />';
      htmlString+= '</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      var workHistryBase = jsonResult["workHistryBase"]; 				//jSONデータより前回作業履歴共通を取得

      if (workHistryBase != undefined) {
        maxSanpuIndex = 0;
        for ( var workHistryBaseKey in workHistryBase ) {				//前回作業履歴

              var workHistryBaseData = workHistryBase[workHistryBaseKey];

              maxSanpuIndex++;
              htmlString+= '<div class="row sanpu-info" id="G0005SanpuInfo-' + maxSanpuIndex + '" sanpuIndex=' + maxSanpuIndex + '>';
              htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<input type="hidden" id="G0005Sanpu' + maxSanpuIndex + 'Value" value="' + workHistryBaseData.sanpuMethod + '" />';
              htmlString+= '<span class="selectmodal-trigger-title item-title">散布方法</span><a href="#selectmodal"  class="selectmodal-trigger item" title="散布方法選択" data="getSanpu" displayspan="#G0005Sanpu' + maxSanpuIndex + 'Span" htext="#G0005Sanpu' + maxSanpuIndex + 'Value"><span id="G0005Sanpu' + maxSanpuIndex + 'Span">' + workHistryBaseData.sanpuMethodName + '</span></a>';
              htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G0005SanpuInfo-' + maxSanpuIndex + '"><i class="material-icons large">clear</i></a>';
              htmlString+= '</div>';
              //機器情報の生成
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/' + workinfo.kukakuid + '/getKikiOfWorkJson" displayspan="#G0005Kiki' + maxSanpuIndex + 'Span" htext="#G0005Kiki' + maxSanpuIndex + 'Value"><span id="G0005Kiki' + maxSanpuIndex + 'Span">' + workHistryBaseData.kikiName + '</span></a>';
              htmlString+= '<input type="hidden"  class="kiki-value-change" id="G0005Kiki' + maxSanpuIndex + 'Value" idindex="' + maxSanpuIndex + '" value="'  + workHistryBaseData.kikiId + '"/>';
              htmlString+= '</span>';
              htmlString+= '</div>';
              //----- アタッチメントは機器を選択するまで選択出来ない様にする -----
              var attachmentName = "未選択";
              if (workHistryBaseData.attachmentId != 0) {
                  attachmentName = workHistryBaseData.attachmentName;
              }
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G0005Attachment' + maxSanpuIndex + 'Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + workHistryBaseData.kikiId + '/getAttachmentOfKikiJson" displayspan="#G0005Attachment' + maxSanpuIndex + 'Span" htext="#G0005Attachment' + maxSanpuIndex + 'Value"><span id="G0005Attachment' + maxSanpuIndex + 'Span">' + attachmentName + '</span></a>';
              htmlString+= '<input type="hidden" id="G0005Attachment' + maxSanpuIndex + 'Value" value="' + workHistryBaseData.attachmentId  + '"/>';
              htmlString+= '</span>';
              htmlString+= '</div>';
              //------------------------------------------------------------------

              var nouhi = workHistryBaseData.nouhiList;

              if (nouhi != undefined) {
                  for ( var nouhiKey in nouhi ) {				//農肥情報

                	  var nouhiData	= nouhi[nouhiKey];
                	  maxNouhiIndex++;							//農肥インデックスをカウントアップする
                	  htmlString+= MakeNouhiSanpuDisplay(maxNouhiIndex, nouhiData["nouhiId"], nouhiData["nouhiName"], nouhiData["bairitu"], nouhiData["sanpuryo"], unitKind[nouhiData["unitKind"]]);

                  }
              }

              maxNouhiIndex++;							//農肥インデックスをカウントアップする
              htmlString+= MakeNouhiSanpuInput(maxNouhiIndex);

              /*----- 散布方法情報カード締め位置 -----*/
              htmlString+= '</div>';
              htmlString+= '</div>';

              makeHistry = true;

          }
      }

      if (!makeHistry) {

          //散布方法の生成
          maxSanpuIndex++;
          htmlString+= '<div class="row sanpu-info" id="G0005SanpuInfo-' + maxSanpuIndex + '" sanpuIndex=' + maxSanpuIndex + '>';
          htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<input type="hidden" id="G0005Sanpu' + maxSanpuIndex + 'Value" value="0" />';
          htmlString+= '<span class="selectmodal-trigger-title item-title">散布方法</span><a href="#selectmodal"  class="selectmodal-trigger item" title="散布方法選択" data="getSanpu" displayspan="#G0005Sanpu' + maxSanpuIndex + 'Span" htext="#G0005Sanpu' + maxSanpuIndex + 'Value"><span id="G0005Sanpu' + maxSanpuIndex + 'Span">未選択</span></a>';
//          htmlString+= '<a href="#G0005ModalSanpu" id="G0005Sanpu' + maxSanpuIndex + '" class="collection-item modal-trigger select-modal" sanpuId=""'  + ' targetId="G0005Sanpu' + maxSanpuIndex + '" targetField="sanpuId"><span id="G0005Sanpu' + maxSanpuIndex + 'Span" class="blockquote-input">未選択</span></a>';
          htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G0005SanpuInfo-' + maxSanpuIndex + '"><i class="material-icons large">clear</i></a>';
          htmlString+= '</div>';
          //機器情報の生成
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/' + workinfo.kukakuid + '/getKikiOfWorkJson" displayspan="#G0005Kiki' + maxSanpuIndex + 'Span" htext="#G0005Kiki' + maxSanpuIndex + 'Value"><span id="G0005Kiki' + maxSanpuIndex + 'Span">未選択</span></a>';
//          htmlString+= '<blockquote><span>機器<a href="#G0005ModalKiki" id="G0005Kiki' + maxSanpuIndex + '" class="collection-item modal-trigger select-modal" kikiId=""'  + ' targetId="G0005Kiki' + maxSanpuIndex + '" targetField="kikiId"><span id="G0005Kiki' + maxSanpuIndex + 'Span" class="blockquote-input">未選択</span></a></blockquote>';
          htmlString+= '<input type="hidden"  class="kiki-value-change" id="G0005Kiki' + maxSanpuIndex + 'Value" idindex="' + maxSanpuIndex + '" />';
          htmlString+= '</span>';
          htmlString+= '</div>';
          //----- アタッチメントは機器を選択するまで選択出来ない様にする -----
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G0005Attachment' + maxSanpuIndex + 'Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + 0 + '/getAttachmentOfKikiJson" displayspan="#G0005Attachment' + maxSanpuIndex + 'Span" htext="#G0005Attachment' + maxSanpuIndex + 'Value"><span id="G0005Attachment' + maxSanpuIndex + 'Span">未選択</span></a>';
//          htmlString+= '<blockquote><span>アタッチメント<a href="#G0005ModalAttachment" id="G0005Attachment' + maxSanpuIndex + '" idindex="' + maxSanpuIndex + '" class="collection-item" attachmentId=""'  + ' targetId="G0005Attachment' + maxSanpuIndex + '" targetField="attachmentId"><span id="G0005Attachment' + maxSanpuIndex + 'Span" class="blockquote-input grey-text text-darken-2">選択不可</span></a></blockquote>';
          htmlString+= '<input type="hidden" id="G0005Attachment' + maxSanpuIndex + 'Value" />';
          htmlString+= '</span>';
          //------------------------------------------------------------------
          htmlString+= '</div>';

      	  maxNouhiIndex	=	$(".nouhi-info").length;//現在の農肥情報数を取得する
      	  maxNouhiIndex++;							//農肥インデックスをカウントアップする
          htmlString+= MakeNouhiSanpuInput(maxNouhiIndex);

          /*----- 散布方法情報カード締め位置 -----*/
          htmlString+= '</div>';
          htmlString+= '</div>';

      }


      htmlString+= MakeSanpuInfoAdd();

      return htmlString;

  }
  function ChangeAttachmentLink() {

    var kiki = $(this);
    var link = $("#G0005Attachment" + kiki.attr("idindex") + "Link");
    link.attr("data", kiki.val() + "/getAttachmentOfKikiJson");
    selectJsonRemove("#G0005Attachment" + kiki.attr("idindex") + "Span");

  }

  /* 片付け項目を生成する */
  function MakeKatadukeItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      htmlString+= '<input type="hidden" id="G0005NouhiValue" value="0"/>';
      htmlString+= '<input type="hidden" id="G0005Bairitu" value="0"/>';
      htmlString+= '<input type="hidden" id="G0005Sanpuryo" value="0"/>';

      return htmlString;

  }

  /* 散布項目を生成する */
  function MakeHiryoSanpuItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      /*----- 肥料情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">散布情報</span>';
      htmlString+= '</div>';

      //htmlString+= MakeDetailSettingKind();							//詳細設定種別を作成する

      htmlString+= '</div>';

      htmlString+= MakeInitNouhiInfo(jsonResult);					//農肥情報を作成する

      return htmlString;

  }

  /* 耕す項目を生成する */
  function MakeTagayasuItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      /*----- 耕す情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">農耕情報</span>';

      //htmlString+= MakeDetailSettingKind();							//詳細設定種別を作成する

      htmlString+= '</div>';
      htmlString+= '</div>';

      //コンビネーションの生成
      htmlString+= '<div class="row" id="G0005CombiInfo">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4">';
      htmlString+= '<div class="card workdiary-commit">';
      htmlString+= '<div class="card-content">';
      htmlString+= '<blockquote><span>コンビネーション<a href="#G0005ModalCombi" id="G0005Combi" class="collection-item modal-trigger" combiId=""><span id="G0005CombiSpan" class="blockquote-input">未選択</span></a></blockquote>';
      htmlString+= '<input type="hidden" id="G0005CombiValue" />';
      htmlString+= '</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //機器情報の生成
      htmlString+= '<div class="row" id="G0005KikiInfo">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/' + workinfo.kukakuid + '/getKikiOfWorkJson" displayspan="#G0005KikiSpan" htext="#G0005KikiValue"><span id="G0005KikiSpan">' + jsonResult["kikiSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" class="kiki-value-change" id="G0005KikiValue" value="' + jsonResult["kikiId"] + '" idindex="1" />';
      htmlString+= '</div>';
      //----- アタッチメントは機器を選択するまで選択出来ない様にする -----
      htmlString+= '<div class="col s12 input-field wd">';
      if (jsonResult["kikiId"] == "0") {
          htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G0005Attachment1Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + 0 + '/getAttachmentOfKikiJson" displayspan="#G0005AttachmentSpan" htext="#G0005Attachment1Value"><span id="G0005AttachmentSpan" class="">未選択</span></a>';
      }
      else {
          htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G0005Attachment1Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + jsonResult["kikiId"] + '/getAttachmentOfKikiJson" displayspan="#G0005AttachmentSpan" htext="#G0005Attachment1Value"><span id="G0005AttachmentSpan" class="">' + jsonResult["attachmentSpan"] + '</span></a>';
      }
      htmlString+= '<input type="hidden" id="G0005Attachment1Value" value="' + jsonResult["attachmentId"] + '" idindex="1" />';
      htmlString+= '</div>';
      //------------------------------------------------------------------
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  /* 播種項目を生成する */
  function MakeHashuItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      /*----- 播種情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">播種情報</span>';

      //htmlString+= MakeDetailSettingKind();							//詳細設定種別を作成する

      htmlString+= '</div>';
      htmlString+= '</div>';

      //コンビネーションの生成
      htmlString+= '<div class="row" id="G0005CombiInfo">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4">';
      htmlString+= '<div class="card workdiary-commit">';
      htmlString+= '<div class="card-content">';
      htmlString+= '<blockquote><span>コンビネーション<a href="#G0005ModalCombi" id="G0005Combi" class="collection-item modal-trigger" combiId=""><span id="G0005CombiSpan" class="blockquote-input">未選択</span></a></blockquote>';
      htmlString+= '<input type="hidden" id="G0005CombiValue" />';
      htmlString+= '</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //機器情報の生成
      htmlString+= '<div class="row" id="G0005KikiInfo">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">品目</span><a href="#selectmodal"  class="selectmodal-trigger item" title="品目選択" data="'+ userinfo.farm + '/getCrop" displayspan="#G0005CropSpan" htext="#G0005CropValue"><span id="G0005CropSpan">' + jsonResult["cropSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" id="G0005CropValue" value="' + jsonResult["cropId"] + '" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">品種</span><a href="#mselectmodal"  class="mselectmodal-trigger item" title="品種選択" data="' + workinfo.kukakuid + '/getHinsyuOfCropJson" displayspan="#G0005HinsyuSpan" htext="#G0005HinsyuValue"><span id="G0005HinsyuSpan">' + jsonResult["hinsyuSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" id="G0005HinsyuValue" value="' + jsonResult["hinsyuId"] + '" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/' + workinfo.kukakuid + '/getKikiOfWorkJson" displayspan="#G0005KikiSpan" htext="#G0005KikiValue"><span id="G0005KikiSpan">' + jsonResult["kikiSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" class="kiki-value-change" id="G0005KikiValue" value="' + jsonResult["kikiId"] + '" idindex="1" />';
      htmlString+= '</div>';
      //----- アタッチメントは機器を選択するまで選択出来ない様にする -----
      htmlString+= '<div class="col s12 input-field wd">';
      if (jsonResult["kikiId"] == "0") {
          htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G0005Attachment1Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + 0 + '/getAttachmentOfKikiJson" displayspan="#G0005Attachment1Span" htext="#G0005Attachment1Value"><span id="G0005Attachment1Span" class="">未選択</span></a>';
      }
      else {
          htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G0005Attachment1Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + jsonResult["kikiId"] + '/getAttachmentOfKikiJson" displayspan="#G0005Attachment1Span" htext="#G0005Attachment1Value"><span id="G0005Attachment1Span" class="">' + jsonResult["attachmentSpan"] + '</span></a>';
      }
      htmlString+= '<input type="hidden" id="G0005Attachment1Value" value="' + jsonResult["attachmentId"] + '" idindex="1" />';
      htmlString+= '</div>';
      //------------------------------------------------------------------
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">ベルト</span><a href="#selectmodal"  class="selectmodal-trigger item" title="ベルト選択" data="getBeltoOfFarmJson" displayspan="#G0005BeltoSpan" htext="#G0005BeltoValue"><span id="G0005BeltoSpan">' + jsonResult["beltoSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" id="G0005BeltoValue" value="' + jsonResult["beltoId"] + '" />';
      htmlString+= '</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">株間</span><span id="G0005KabumaSpan" class="item">' + jsonResult["kabuma"] + '</span><span class="item">cm</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Kabuma">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Kabuma" value="' + jsonResult["kabuma"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">条間</span><span id="G0005JoukanSpan" class="item">' + jsonResult["joukan"] + '</span><span class="item">cm</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Joukan">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Joukan" value="' + jsonResult["joukan"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">条数</span><span id="G0005JousuSpan" class="item">' + jsonResult["jousu"] + '</span><span class="item">cm</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Jousu">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Jousu" value="' + jsonResult["jousu"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">深さ</span><span id="G0005HukasaSpan" class="item">' + jsonResult["hukasa"] + '</span><span class="item">cm</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Hukasa">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Hukasa" value="' + jsonResult["hukasa"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  /* 除草剤散布項目を生成する */
  function MakeJosouzaiSanpuItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      /*----- 除草剤散布情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4">';
      htmlString+= '<div class="card-panel light-green darken-2">';
      htmlString+= '<span class="white-text">除草剤情報</span>';

      //htmlString+= MakeDetailSettingKind();							//詳細設定種別を作成する

      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= MakeInitNouhiInfo(jsonResult);					//農肥情報を作成する

      return htmlString;

  }

  /* 潅水項目を生成する */
  function MakeKansuiItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      /*----- 潅水情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">潅水情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //潅水方法の生成
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">潅水方法</span><a href="#selectmodal"  class="selectmodal-trigger item" title="潅水方法選択" data="getKansui" displayspan="#G0005KansuiSpan" htext="#G0005KansuiValue"><span id="G0005KansuiSpan">' + jsonResult["kansuiSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" id="G0005KansuiValue" value="' + jsonResult["kansuiMethod"] + '" />';
      htmlString+= '</div>';
      //機器情報の生成
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/' + workinfo.kukakuid + '/getKikiOfWorkJson" displayspan="#G0005KikiSpan" htext="#G0005KikiValue"><span id="G0005KikiSpan">' + jsonResult["kikiSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" class="kiki-value-change" id="G0005KikiValue" value="' + jsonResult["kikiId"] + '" idindex="1" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">潅水量</span><span id="G0005KansuiRyoSpan" class="item">' + jsonResult["kansuiryo"] + '</span><span class="item">L</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005KansuiRyo">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005KansuiRyo" value="' + jsonResult["kansuiryo"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  /* 収穫項目を生成する */
  function MakeShukakuItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      /*----- 収穫情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">収穫情報</span>';
      htmlString+= '<div class="col s12 btn-area">';
      htmlString+= '<a class="waves-effect btn-flat right all-shukaku-clear"><i class="material-icons left">remove_circle_outline</i>all clear</a>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //収穫方法の生成
      var workHistryBase = jsonResult["workHistryBase"];
      var index=0;
      if (workHistryBase != undefined) {
          for ( var workHistryBaseKey in workHistryBase ) {

              var workHistryBaseData = workHistryBase[workHistryBaseKey];
              index++;

              htmlString+= '<div class="row">';
              htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="selectmodal-trigger-title item-title">荷姿</span><a href="#selectmodal"  class="selectmodal-trigger item" title="荷姿選択" data="getNisugata" displayspan="#G0005Nisugata' + index + 'Span" htext="#G0005Nisugata' + index + 'Value"><span id="G0005Nisugata' + index + 'Span">' + workHistryBaseData["nisugataSpan"] + '</span></a>';
              htmlString+= '<br><span class="item-sub substr-color" id="G0005Capa' + index + 'Span">この荷姿の内容量は' + workHistryBaseData["capa"] + 'gです</span>';
              htmlString+= '<input type="hidden" class="nisugata-value" id="G0005Nisugata' + index + 'Value" value="' + workHistryBaseData["nisugata"] + '" key="' + index + '" />';
              htmlString+= '<input type="hidden" id="G0005Capa' + index + '" value="' + workHistryBaseData["capa"] + '" />';
              htmlString+= '</div>';
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="selectmodal-trigger-title item-title">質</span><a href="#selectmodal"  class="selectmodal-trigger item" title="質選択" data="getShitu" displayspan="#G0005Shitu' + index + 'Span" htext="#G0005Shitu' + index + 'Value"><span id="G0005Shitu' + index + 'Span">' + workHistryBaseData["sitsuSpan"] + '</span></a>';
              htmlString+= '<input type="hidden" id="G0005Shitu' + index + 'Value" value="' + workHistryBaseData["sitsu"] + '" />';
              htmlString+= '</div>';
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="selectmodal-trigger-title item-title">サイズ</span><a href="#selectmodal"  class="selectmodal-trigger item" title="サイズ選択" data="getSize" displayspan="#G0005Size' + index + 'Span" htext="#G0005Size' + index + 'Value"><span id="G0005Size' + index + 'Span">' + workHistryBaseData["sizeSpan"] + '</span></a>';
              htmlString+= '<input type="hidden" id="G0005Size' + index + 'Value" value="' + workHistryBaseData["size"] + '" />';
              htmlString+= '</div>';
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="item-title">個数</span><span id="G0005Kosu' + index + 'Span" class="item">' + workHistryBaseData["kosu"] + '</span><span class="item">個</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Kosu' + index + '">keyboard</i>';
              htmlString+= '<input type="hidden" class="kosu-value" id="G0005Kosu' + index + '" value="' + workHistryBaseData["kosu"] + '"/ key="' + index + '">';
              htmlString+= '</div>';
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="item-title">収穫量</span><span id="G0005ShukakuRyo' + index + 'Span" class="item">' + workHistryBaseData["shukakuRyo"] + '</span><span class="item">Kg</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005ShukakuRyo' + index + '">keyboard</i>';
              htmlString+= '<input type="hidden" id="G0005ShukakuRyo' + index + '" value="' + workHistryBaseData["shukakuRyo"] + '"/>';
              htmlString+= '</div>';
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<a class="waves-effect btn-flat right shukaku-clear" key="' + index + '"><i class="material-icons left">remove_circle_outline</i>clear</a>';
              htmlString+= '</div>';
              htmlString+= '</div>';
              htmlString+= '</div>';
              htmlString+= '</div>';

          }
      }
//      htmlString+= '<div class="row">';
//      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
//      htmlString+= '<div class="col s12 input-field wd">';
//      htmlString+= '<span class="selectmodal-trigger-title item-title">荷姿</span><a href="#selectmodal"  class="selectmodal-trigger item" title="荷姿選択" data="getNisugata" displayspan="#G0005NisugataSpan" htext="#G0005NisugataValue"><span id="G0005NisugataSpan">' + jsonResult["nisugataSpan"] + '</span></a>';
//      htmlString+= '<input type="hidden" id="G0005NisugataValue" value="' + jsonResult["nisugata"] + '" />';
//      htmlString+= '</div>';
//      htmlString+= '<div class="col s12 input-field wd">';
//      htmlString+= '<span class="selectmodal-trigger-title item-title">質</span><a href="#selectmodal"  class="selectmodal-trigger item" title="質選択" data="getShitu" displayspan="#G0005ShituSpan" htext="#G0005ShituValue"><span id="G0005ShituSpan">' + jsonResult["sitsuSpan"] + '</span></a>';
//      htmlString+= '<input type="hidden" id="G0005ShituValue" value="' + jsonResult["sitsu"] + '" />';
//      htmlString+= '</div>';
//      htmlString+= '<div class="col s12 input-field wd">';
//      htmlString+= '<span class="selectmodal-trigger-title item-title">サイズ</span><a href="#selectmodal"  class="selectmodal-trigger item" title="サイズ選択" data="getSize" displayspan="#G0005SizeSpan" htext="#G0005SizeValue"><span id="G0005SizeSpan">' + jsonResult["sizeSpan"] + '</span></a>';
//      htmlString+= '<input type="hidden" id="G0005SizeValue" value="' + jsonResult["size"] + '" />';
//      htmlString+= '</div>';
//      htmlString+= '<div class="col s12 input-field wd">';
//      htmlString+= '<span class="item-title">収穫量</span><span id="G0005ShukakuRyoSpan" class="item">' + jsonResult["shukakuRyo"] + '</span><span class="item">Kg</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005ShukakuRyo">keyboard</i>';
//      htmlString+= '<input type="hidden" id="G0005ShukakuRyo" value="' + jsonResult["shukakuRyo"] + '"/>';
//      htmlString+= '</div>';
//      htmlString+= '</div>';
//      htmlString+= '</div>';
//      htmlString+= '</div>';
//      htmlString+= '</div>';

      return htmlString;

  }
  /* 回収項目を生成する */
  function MakeKaishuItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- 回収情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">回収情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';

      var workHistryBase = jsonResult["workHistryBase"];
      var index=0;
      if (workHistryBase != undefined) {
          for ( var workHistryBaseKey in workHistryBase ) {

              var workHistryBaseData = workHistryBase[workHistryBaseKey];
              index++;

              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="item-title">数量' + index + '</span><span id="G0005Suryo' + index + 'Span" class="item">' + workHistryBaseData["suryo"] + '</span><span class="item">個</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Suryo' + index + '">keyboard</i>';
              htmlString+= '<input type="hidden" id="G0005Suryo' + index + '" value="' + workHistryBaseData["suryo"] + '"/>';
              htmlString+= '</div>';

          }
      }

      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }
  /* 脱着項目を生成する */
  function MakeDachakuItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- 回収情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">脱着情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';

      var workHistryBase = jsonResult["workHistryBase"];
      var index=0;
      if (workHistryBase != undefined) {
          for ( var workHistryBaseKey in workHistryBase ) {

              var workHistryBaseData = workHistryBase[workHistryBaseKey];
              index++;

              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="selectmodal-trigger-title item-title">資材' + index + '</span><a href="#selectmodal"  class="selectmodal-trigger item" title="資材選択" data="getSizai" displayspan="#G0005Sizai' + index + 'Span" htext="#G0005Sizai' + index + 'Value"><span id="G0005Sizai' + index + 'Span">' + workHistryBaseData["sizaiSpan"] + '</span></a>';
              htmlString+= '<input type="hidden" id="G0005Sizai' + index + 'Value" value="' + workHistryBaseData["sizaiId"] + '" />';
              htmlString+= '</div>';

          }
      }

      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }
  /* コメント項目を生成する */
  function MakeCommentItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- コメント情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">コメント情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';

      var workHistryBase = jsonResult["workHistryBase"];
      var index=0;
      if (workHistryBase != undefined) {
          for ( var workHistryBaseKey in workHistryBase ) {

              var workHistryBaseData = workHistryBase[workHistryBaseKey];
              index++;

              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<textarea id="G0005Comment' + index + '" class="materialize-textarea input-text-color" style="">' + workHistryBaseData["comment"] + '</textarea><label for="G0005Comment' + index + '">コメント' + index + '</label>';
              htmlString+= '';
              htmlString+= '</div>';

          }
      }

      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }
  /* マルチ項目を生成する */
  function MakeMultiItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- マルチ情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">マルチ情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">使用マルチ</span><a href="#selectmodal"  class="selectmodal-trigger item" title="使用マルチ選択" data="getMulti" displayspan="#G0005MultiSpan" htext="#G0005MultiValue"><span id="G0005MultiSpan">' + jsonResult["useMultiSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" id="G0005MultiValue" value="' + jsonResult["useMulti"] + '" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">列数</span><span id="G0005RetusuSpan" class="item">' + jsonResult["retusu"] + '</span><span class="item">列</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Retusu">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Retusu" value="' + jsonResult["retusu"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }
  /* 定植項目を生成する */
  function MakeTeishokuItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- 定植情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">定植情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">使用苗枚数</span><span id="G0005NaemaisuSpan" class="item">' + jsonResult["naemaisu"] + '</span><span class="item">枚</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Naemaisu">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Naemaisu" value="' + jsonResult["naemaisu"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">列数</span><span id="G0005RetusuSpan" class="item">' + jsonResult["retusu"] + '</span><span class="item">列</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Retusu">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Retusu" value="' + jsonResult["retusu"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }
  /* 苗播種項目を生成する */
  function MakeNaehashuItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- マルチ情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">苗播種情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">使用穴数</span><span id="G0005UseHoleSpan" class="item">' + jsonResult["useHole"] + '</span><span class="item">穴</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005UseHole">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005UseHole" value="' + jsonResult["useHole"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">枚数</span><span id="G0005MaisuSpan" class="item">' + jsonResult["maisu"] + '</span><span class="item">枚</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Maisu">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Maisu" value="' + jsonResult["maisu"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">使用培土</span><a href="#selectmodal"  class="selectmodal-trigger item" title="使用培土選択" data="getUseBaido" displayspan="#G0005UseBaidoSpan" htext="#G0005UseBaidoValue"><span id="G0005UseBaidoSpan">' + jsonResult["useBaidoSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" id="G0005UseBaidoValue" value="' + jsonResult["useBaido"] + '" />';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }
  /* 剪定項目を生成する */
  function MakeSenteiItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- マルチ情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">剪定情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">剪定高</span><span id="G0005SenteiHeightSpan" class="item">' + jsonResult["senteiHeight"] + '</span><span class="item">cm</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005SenteiHeight">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005SenteiHeight" value="' + jsonResult["senteiHeight"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }


  /* 詳細情報設定種別を生成する */
  function MakeDetailSettingKind() {

      var htmlString	=	"";		/* 詳細情報設定種別マークアップ */

      htmlString += '<div class="switch right">';
      htmlString += '<label class="white-text">';
      htmlString += '手動';
      htmlString += '<input type="checkbox" id="G0005DetailSettingKind">';
      htmlString += '<span class="lever"></span>';
      htmlString += 'コンビ';
      htmlString += '</label>';
      htmlString += '</div>';

      return htmlString;

  }

  /* 農肥散布情報表示マークアップ生成 */
  function MakeNouhiSanpuDisplay(nouhiIndex, nouhiId, nouhiName, bairitu, sanpuryo, unit) {

      var htmlString	=	"";		/* 農肥情報表示マークアップ */

      htmlString+= '<div class="row nouhi-info" id="G0005NouhiInfo-' + nouhiIndex + '" nouhiIndex="' + nouhiIndex + '">';
      htmlString+= '<div class="">';
      htmlString+= '<div class="col s12 info-line">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">農薬/肥料情報</span><span id="G0005Nouhi' + nouhiIndex + 'Span" class="item commit">' + nouhiName + '</span>';
      htmlString+= '<input type="hidden" id="G0005Nouhi' + nouhiIndex + 'Value" class="nouhi-value-change" idindex="' + nouhiIndex + '" value="' + nouhiId + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">倍率</span><span id="G0005Bairitu' + nouhiIndex + 'Span" class="item commit">' + bairitu + '倍</span>';
      htmlString+= '<input type="hidden" id="G0005Bairitu' + nouhiIndex + '" value="' + bairitu + '" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">散布量</span><span id="G0005Sanpuryo' + nouhiIndex + 'Span" class="item commit">' + sanpuryo + unit + '</span>';
      htmlString+= '<input type="hidden" id="G0005Sanpuryo' + nouhiIndex + '" value="' + sanpuryo + '" />';
      htmlString+= '<input type="hidden" id="G0005Unit' + nouhiIndex + '" value="' + unit + '" />';
      htmlString+= '</div>';
      htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G0005NouhiInfo-' + nouhiIndex + '"><i class="material-icons large">clear</i></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  /* 農肥散布情報入力マークアップ生成 */
  function MakeNouhiSanpuInput(nouhiIndex) {

      var htmlString	=	"";		/* 農肥情報表示マークアップ */

      htmlString+= '<div class="row nouhi-info" id="G0005NouhiInfo-' + nouhiIndex + '" nouhiIndex="' + nouhiIndex + '">';
      htmlString+= '<div class="col s12 info-line">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">農薬/肥料情報</span><a href="#selectmodal"  class="selectmodal-trigger item" title="農薬/肥料選択" data="' + workinfo.workid + '/' + workinfo.kukakuid + '/getNouhiOfWorkJson" displayspan="#G0005Nouhi' + nouhiIndex + 'Span" htext="#G0005Nouhi' + nouhiIndex + 'Value"><span id="G0005Nouhi' + nouhiIndex + 'Span">未選択</span></a>';
      htmlString+= '<input type="hidden" id="G0005Nouhi' + nouhiIndex + 'Value" class="nouhi-value-change" idindex="' + nouhiIndex + '" value="0"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">倍率</span><span id="G0005Bairitu' + nouhiIndex + 'Span" class="item">' + 0 + '</span><span class="item">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Bairitu' + nouhiIndex + '">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Bairitu' + nouhiIndex + '" value="0.00"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">散布量</span><span id="G0005Sanpuryo' + nouhiIndex + 'Span" class="item">' + 0 + '</span><span class="item" id="G0005Unit' + nouhiIndex + 'Span"></span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G0005Sanpuryo' + nouhiIndex + '">keyboard</i>';
      htmlString+= '<input type="hidden" id="G0005Sanpuryo' + nouhiIndex + '" value="0.00"/>';
      htmlString+= '<input type="hidden" id="G0005Unit' + nouhiIndex + '" value="" />';
      htmlString+= '</div>';
      htmlString+= '<a class="waves-effect waves-light btn-floating btn blue darken-1 nouhi-commit-btn right" idindex="' + nouhiIndex + '"><i class="material-icons large">done</i></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  /* 散布情報追加マークアップ生成 */
  function MakeSanpuInfoAdd() {

      var htmlString	=	"";		/* 農肥情報表示マークアップ */

      /*----- 散布方法情報カード追加位置 -----*/
      htmlString+= '<div class="row" id="G0005SanpuInfoAdd">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel wd">';
      //散布方法追加ボタン
      htmlString+= '<div class="col s12" style="padding-top: 16px;padding-bottom: 16px;">';
      htmlString+= '<span class="item-title" style="margin-top: 8px;display:inline-block;">散布方法追加</span><a class="waves-effect waves-light btn-floating btn red right" id="G0005SanpuInfoAddBtn"><i class="material-icons large">add</i></a></blockquote>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  function dataToJson() {

    /* 当画面の入力項目情報定義 */
    var checktarget = [
        { "id" : "G0005WorkDate"        , "name" : "日付"        , "json" : "workDate"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005WorkKukakuValue" , "name" : "圃場"        , "json" : "workKukaku"    , "check" : { "select"    : "1"}}
       ,{ "id" : "G0005WorkTime"        , "name" : "片付け時間"    , "json" : "workTime"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005StartDate"       , "name" : "作業開始日付"  , "json" : "startDate"     , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005StartTime"       , "name" : "作業開始時間"  , "json" : "startTime"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005EndDate"         , "name" : "作業終了日付"  , "json" : "endDate"        , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005EndTime"         , "name" : "作業終了時間"  , "json" : "endTime"        , "check" : { "required"  : "1"}}
       ,{ "id" : "G0005WorkAccountValue", "name" : "担当者"      , "json" : "workAccount"  , "check" : { "select"    : "1"}}
       ,{ "id" : "G0005WorkId"          , "name" : "作業"     , "json" : "workId"       , "check" : {            }}
       ,{ "id" : "G0005WorkDiaryId"     , "name" : "作業記録ID" , "json" : "workDiaryId"  , "check" : {            }}
    ];

    //----- 作業項目の生成 START -----
    switch(workinfo.worktemplateid) {
    case 1: //通常作業

      checktarget.push({ "id" : "G0005WorkTime" , "name" : "作業時間"  , "json" : "workTime" , "check" : { "required"  : "1"}});
      break;

    case 2: //散布作業

      checktarget.push({ "id" : "G0005KikiValue"        , "name" : "機器情報"       , "json" : "kiki"       , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G0005AttachmentValue"  , "name" : "アタッチメント情報"  , "json" : "attachment" , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G0005WorkTime"         , "name" : "散布時間"       , "json" : "workTime"   , "check" : { "required"  : "1"}});
      break;

    case 3: //播種作業

      checktarget.push({ "id" : "G0005KikiValue"        , "name" : "機器情報"     , "json" : "kiki"   , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005Attachment1Value" , "name" : "アタッチメント情報"  , "json" : "attachment" , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005HinsyuValue"      , "name" : "種情報"        , "json" : "hinsyu"   , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G0005BeltoValue"       , "name" : "ベルト情報"      , "json" : "belto"    , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005Kabuma"           , "name" : "株間"         , "json" : "kabuma"   , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005Joukan"           , "name" : "条間"         , "json" : "joukan"   , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005Jousu"            , "name" : "条数"         , "json" : "jousu"    , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005Hukasa"           , "name" : "深さ"         , "json" : "hukasa"   , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005WorkTime"         , "name" : "播種時間"     , "json" : "workTime" , "check" : { "required"  : "1"}});
      break;

    case 4://収穫作業

      checktarget.push({ "id" : "G0005Nisugata1Value"  , "name" : "荷姿情報" , "json" : "nisugata" , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005Shitu1Value"   , "name" : "質情報"    , "json" : "shitu"    , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005Size1Value"    , "name" : "サイズ情報"  , "json" : "size"   , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005Kosu1Value"    , "name" : "個数情報"  , "json" : "kosu"   , "check" : { "required"    : "1"}});
      checktarget.push({ "id" : "G0005ShukakuRyo1"   , "name" : "収穫量"    , "json" : "shukakuRyo" , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005WorkTime"   , "name" : "収穫時間" , "json" : "workTime" , "check" : { "required"  : "1"}});
      break;

    case 5: //農耕作業

      checktarget.push({ "id" : "G0005KikiValue"      , "name" : "機器情報"     , "json" : "kiki"   , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005Attachment1Value" , "name" : "アタッチメント情報"  , "json" : "attachment" , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005WorkTime" , "name" : "耕す時間" , "json" : "workTime" , "check" : { "required"  : "1"}});
      break;

    case 6://潅水作業

      checktarget.push({ "id" : "G0005KansuiValue", "name" : "潅水方法" , "json" : "kansui"   , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005KikiValue"  , "name" : "機器情報" , "json" : "kiki"   , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005KansuiRyo"  , "name" : "潅水量"    , "json" : "kansuiryo"  , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005WorkTime" , "name" : "潅水時間" , "json" : "workTime" , "check" : { "required"  : "1"}});
      break;

    case 11://マルチ作業

      checktarget.push({ "id" : "G0005MultiValue" , "name" : "使用マルチ" , "json" : "useMulti"   , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G0005Retusu"     , "name" : "列数"     , "json" : "retusu"     , "check" : { "required"  : "1"}});
      break;

    case 12://定植作業

      checktarget.push({ "id" : "G0005Naemaisu"   , "name" : "苗枚数"   , "json" : "naemaisu"    , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005Retusu"     , "name" : "列数"     , "json" : "retusu"      , "check" : { "required"  : "1"}});
      break;

    case 13://苗播種作業

      checktarget.push({ "id" : "G0005UseHole"        , "name" : "使用穴数"   , "json" : "useHole"    , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005Maisu"          , "name" : "枚数"      , "json" : "maisu"      , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G0005UseBaidoValue"  , "name" : "使用培土"   , "json" : "useBaido"   , "check" : { "select"    : "0"}});
      break;

    case 15://剪定作業

      checktarget.push({ "id" : "G0005SenteiHeight"   , "name" : "剪定高"   , "json" : "senteiHeight"    , "check" : { "required"  : "1"}});
      break;

    }
    //----- 作業項目の生成 END -----

    /* 各入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    //----- 作業開始終了日時チェック -----
    var workPlanType = oInputJson.workPlanType;
    if (workPlanType != 2) { //作業指示の保存以外の場合
      var wStart = new Date($("#G0005StartDate").val() + " " + $("#G0005StartTime").val());
      var wEnd   = new Date($("#G0005EndDate").val() + " " + $("#G0005EndTime").val());

      console.log(wStart);
      console.log(wEnd);

      if (wEnd < wStart) {
        alert("作業終了日時が作業開始日時以前に設定されています。");
        return false;
      }
    }

    oInputJson   = InputDataToJson(checktarget);
    oInputJson["mode"] = 1;
    oInputJson["workPlanType"] = workPlanType;

    /*----- 肥料散布情報をJSONデータとして格納する -----*/
    var nouhiDataList = new Array();                  //農肥情報リスト

    var nouhiList = $(".nouhi-info");                 //散布情報を全て取得する

    $.each(nouhiList, function (index, nouhiData) {
        var nouhiJson = new Object();

        if ($("#G0005Nouhi" + $(nouhiData).attr('nouhiIndex') + "Value").val() == 0) { /* 農肥情報が未選択は対象外 */

        }
        else {
          //var sanpuData = $("#" + nouhiData.id).parent("div").parent("div").parent("div");
          var sanpuData = $("#" + nouhiData.id).parents(".sanpu-info");

          nouhiJson["kiki"]   = $("#G0005Kiki" + $(sanpuData).attr('sanpuIndex') + "Value").val();
          nouhiJson["attachment"]   = $("#G0005Attachment" + $(sanpuData).attr('sanpuIndex') + "Value").val();

          nouhiJson["sanpuId"]  = $("#G0005Sanpu" + $(sanpuData).attr('sanpuIndex') + "Value").val();
          nouhiJson["sanpuName"]  = $("#G0005Sanpu" + $(sanpuData).attr('sanpuIndex') + "Span").html();
          nouhiJson["nouhiId"]  = $("#G0005Nouhi" + $(nouhiData).attr('nouhiIndex') + "Value").val();
          nouhiJson["bairitu"]  = $("#G0005Bairitu" + $(nouhiData).attr('nouhiIndex')).val();
          nouhiJson["sanpuryo"]   = $("#G0005Sanpuryo" + $(nouhiData).attr('nouhiIndex')).val();

          nouhiDataList.push(nouhiJson);
        }
    });

    oInputJson["nouhiInfo"] = nouhiDataList;

    //----- 作業詳細情報をJSON化する -----
    var detailDataList = new Array();

    if (workinfo.worktemplateid == 4) { //収穫の場合
      var init = true;
      for (var index = 1; index <= 10; index++) {
        var dj = new Object();
        dj["suryo"]   = 0;
        dj["sizaiId"] = 0;
        dj["comment"] = "";

        dj["nisugata"]   = $("#G0005Nisugata" + index + "Value").val();
        if (dj["nisugata"] == null || dj["nisugata"] == "" || isNaN(dj["nisugata"])) {
          dj["nisugata"] = "0";
        }
        dj["sitsu"]      = $("#G0005Shitu" + index + "Value").val();
        if (dj["sitsu"] == null || dj["sitsu"] == "" || isNaN(dj["sitsu"])) {
          dj["sitsu"] = "0";
        }
        dj["size"]       = $("#G0005Size" + index + "Value").val();
        if (dj["size"] == null || dj["size"] == "" || isNaN(dj["size"])) {
          dj["size"] = "0";
        }
        dj["kosu"]       = $("#G0005Kosu" + index + "").val();
        if (dj["kosu"] == null || dj["kosu"] == "" || isNaN(dj["kosu"])) {
          dj["kosu"] = "0";
        }
        dj["shukakuRyo"] = $("#G0005ShukakuRyo" + index + "").val();
        if (dj["shukakuRyo"] == null || dj["shukakuRyo"] == "" || isNaN(dj["shukakuRyo"])) {
          dj["shukakuRyo"] = "0";
        }

        if (init) {
          oInputJson["nisugata"]    = dj["nisugata"];
          oInputJson["shitu"]       = dj["sitsu"];
          oInputJson["size"]        = dj["size"];
          oInputJson["kosu"]        = dj["kosu"];
          oInputJson["shukakuRyo"]  = dj["shukakuRyo"];
          init = false;
        }

        detailDataList.push(dj);
      }
    }
    else {
      for (var index = 1; index <= 5; index++) {
        var dj = new Object();
        dj["suryo"]   = 0;
        dj["sizaiId"] = 0;
        dj["comment"] = "";

        switch(workinfo.worktemplateid) {
        case 8://回収作業
          dj["suryo"]   = $("#G0005Suryo" + index).val();
          break;

        case 9://脱着作業
          dj["sizaiId"]   = $("#G0005Sizai" + index + "Value").val();
          break;

        case 10://コメント作業
          dj["comment"]   = $("#G0005Comment" + index).val();
          break;

        }
        detailDataList.push(dj);
      }
    }
    oInputJson["detailInfo"] = detailDataList;
    return true;

  }

  /* 日誌記録ボタンタップ時イベント */
  function SubmitTap() {

    oInputJson = {};
    if (dataToJson() == false) {
      return false;
    }
    NouhiCheck(oInputJson);                      //作業日誌保存処理

  }

  /* 入力した内容を作業日誌に保存する */
  function SubmitWorkDiary(jsondata){

  	if (nowproc == true) {

          displayToast('現在、作業記録の保存中です', 4000, 'rounded');
          return;

  	}

  	nowproc = true;

  	var url = "/submitWorkDiary";

  	if ($("#G0005WorkDiaryId").val() != "") {
  		url = "/" + $("#G0005WorkDiaryId").val() + "/submitWorkDiaryEdit";
  	}

    /* 登録アニメーション実行 */
    $('.workdiary-commit').addClass("commit");

    $.ajax({
        url:url,												//作業日誌保存処理
        type:'POST',
        data:JSON.stringify(jsondata),							//入力用JSONデータ
        complete:function(data, status, jqXHR){					//処理成功時

          nowproc = false;

          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

          if (jsonResult.result == 'SUCCESS') {
          	displayToast('作業日誌を記録しました。', 4000, 'rounded');    			  //保存メッセージの表示

          	var wca = jsonResult.workCommitAfter;  //作業記録後の取得

          	if (wca == 1) {
//              window.location.href = '/' + workinfo.workid + '/' + workinfo.kukakuid + '/workDiaryMove';
                //連続作業切替時はバックモードを0にする
                localStorage.setItem("backMode"         , "0");
                localStorage.setItem("backKukakuId"     , jsonResult.kukakus);
                localStorage.setItem("backWorkId"       , workinfo.workid);

                window.location.href = '/' + workinfo.workid + '/' + jsonResult.kukakus + '/workDiaryMove';
          	}
          	else if (wca == 2) {
              if (jsonResult.workDiaryId != 0) {
                window.location.href = "/" + jsonResult.workDiaryId + "/workDiaryEdit";
              }
          	}
          	else {
              if (localStorage.getItem("backMode") == "1") {
                window.location.href = "/workingaccountmove";
              }
              else if (localStorage.getItem("backMode") == "5") {
                //作業指示画面に遷移
                window.location.href = "/workPlanMove";
              }
              else if (localStorage.getItem("backMode") == "6") {
                window.location.href = "/workPlanAccountMove";
              }
              else {
                if (localStorage.getItem("backMode") == "4") {
                  localStorage.setItem("backMode"         , null);
                  localStorage.setItem("backWorkId"       , null);
                  localStorage.setItem("backKukakuId"     , null);
                  localStorage.setItem("backFieldGroupId" , null);
                }
                window.location.href = "/menuMove";
              }
          	}
          }
        },
        error:function(data, status, jqXHR){								//処理成功時

          nowproc = false;
          displayToast('作業記録の保存に失敗しました', 4000, 'rounded');

        },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
  } // end of SubmitWorkDiary

  /* 農肥チェック */
  function NouhiCheck(jsondata){

    var url = "/checkNouhi";

    $.ajax({
      url:url,                        //作業日誌保存処理
      type:'POST',
      data:JSON.stringify(jsondata),              //入力用JSONデータ
      complete:function(data, status, jqXHR){         //処理成功時

        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        console.log('RESULT  : %d', jsonResult.result);
        console.log('MESSAGE : %s', jsonResult.message);
        if (jsonResult.result != 0) {
          var result = window.confirm('下記の警告が発生しています。\n作業を登録しますか？\n' + jsonResult.message);
          if (result) {
            SubmitWorkDiary(jsondata);                      //作業日誌保存処理
          }
        }
        else {
          SubmitWorkDiary(jsondata);                      //作業日誌保存処理
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  } // end of SubmitWorkDiary

  /* アタッチメント一覧を取得する */
  function GetAttachment(){

    /* 当画面の入力項目情報定義 */
    var checktarget = [
        { "id" : $(this).attr("id")		, "name" : "機器情報"		, "json" : "kikiId"	, "check" : { }}
    ];

    var jsondata   		= InputDataToJson(checktarget);			//JSONDATAに変換する
    targetKikiIndex 	= $(this).attr("idindex");				//対象機器情報インデックスを保存

    $.ajax({
        url:"/getAttachmentList",								//アタッチメント取得処理
        type:'POST',
        data:JSON.stringify(jsondata),							//入力用JSONデータ
        complete:function(data, status, jqXHR){					//処理成功時
          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成


          var attachmentList	= jsonResult["attachmentDataList"];

          attachmentDataList[targetKikiIndex] = attachmentList;

          $("#G0005Attachment" + targetKikiIndex + "Span").removeClass("grey-text").removeClass("text-darken-2").text("未選択");
          $("#G0005Attachment" + targetKikiIndex).click(OpenAttachmentModal);

        },
        dataType:'json',
        contentType:'text/json',
        async: true
    });
  } // end of G0001Login

  /* アタッチメント一覧モーダルを開く*/
  function OpenAttachmentModal(){

	  var leanmodal = $(".lean-overlay");

	  if (leanmodal.length > 0) {
		  for (var leanKey in leanmodal) {
			  var lean = leanmodal.eq(leanKey);
			  lean.remove();
		  }
	  }

      var attachmentList = attachmentDataList[$(this).attr("idindex")];

      $('div').remove('#G0005ModalAttachment');
      $("#G0005SelectModal").append(MakeSelectModal('G0005ModalAttachment', 'アタッチメント', attachmentList, 'attachmentId', 'attachementName', 'G0005Attachment', 'attachmentId'));

      $('.select-modal-tap').unbind();								//農肥選択時

      $('.select-modal-tap').click(function() {

        var targetId 		= $('#' + $(this).attr("targetModal")).attr("targetId");
        var targetField 	= $('#' + $(this).attr("targetModal")).attr("targetField");

        $('#' + targetId).attr(targetField, $(this).attr("itemKey"));

        var targetSpanId = '#' + targetId + 'Span';
        $(targetSpanId).html($(this).attr("itemName"));

        var targetValueId = '#' + targetId + 'Value';
        $(targetValueId).val($(this).attr("itemKey"));

        //materialize VerUP
        //$('#' + $(this).attr("targetModal")).closeModal();
        $('#' + $(this).attr("targetModal")).modal('close');
        $(targetValueId).change();

      });

      //materialize VerUP
      $('.modal').modal();

      $($(this).attr("href")).attr("targetId", $(this).attr("targetId"));
      $($(this).attr("href")).attr("targetField", $(this).attr("targetField"));
      //materialize VerUP
      //$($(this).attr("href")).openModal();
      $($(this).attr("href")).modal('open');

  } // end of G0001Login

  /* 農肥前回情報を取得する */
  function GetNouhiValue(){

    /* 当画面の入力項目情報定義 */
    var checktarget = [
        { "id" : $(this).attr("id")		, "name" : "農肥情報"		, "json" : "nouhiId"	, "check" : { }}
    ];

    var jsondata   		= InputDataToJson(checktarget);			//JSONDATAに変換する

    jsondata["kukakuId"]  = $("#G0005WorkKukakuValue").val();

    targetNouhiIndex 	= $(this).attr("idindex");				//対象農肥情報インデックスを保存

    $.ajax({
        url:"/getNouhiValue",									//作業日誌保存処理
        type:'POST',
        data:JSON.stringify(jsondata),							//入力用JSONデータ
        complete:function(data, status, jqXHR){					//処理成功時
          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

          /*----- 倍率を設定 -----*/
          $("#G0005Bairitu" + targetNouhiIndex + "Span").html(jsonResult["bairitu"]);
          $("#G0005Bairitu" + targetNouhiIndex).val(jsonResult["bairitu"]);
          $("#G0005Bairitu" + targetNouhiIndex).attr("min" ,jsonResult["lower"]);
          $("#G0005Bairitu" + targetNouhiIndex).attr("max" ,jsonResult["upper"]);
          $("#G0005Bairitu" + targetNouhiIndex).attr("step" ,"0.01");

          /*----- 散布量を設定 -----*/
          $("#G0005Sanpuryo" + targetNouhiIndex + "Span").html(jsonResult["sanpuryo"]);
          $("#G0005Sanpuryo" + targetNouhiIndex).val(jsonResult["sanpuryo"]);
          $("#G0005Sanpuryo" + targetNouhiIndex).attr("min" ,"0.00");
          $("#G0005Sanpuryo" + targetNouhiIndex).attr("max" ,"10000.00");
          $("#G0005Sanpuryo" + targetNouhiIndex).attr("step" ,"0.01");
          $("#G0005Unit" + targetNouhiIndex + "Span").html(unitKind[jsonResult["unit"]]);
          $("#G0005Unit" + targetNouhiIndex).val(unitKind[jsonResult["unit"]]);

        },
        dataType:'json',
        contentType:'text/json',
        async: true
    });
  } // end of G0001Login

  /* 散布情報追加イベント発生時 */
  function AddSunpuInfoEvent() {

    var addInfoRow	=	$("#G0005SanpuInfoAdd");		//散布情報追加位置を確保
    var htmlString	= "";								//可変HTML文字列

    //散布方法の生成
    maxSanpuIndex	=	$(".sanpu-info").length;		//現在の散布方法数を取得する
    maxSanpuIndex++;
    htmlString+= '<div class="row sanpu-info" id="G0005SanpuInfo-' + maxSanpuIndex + '" sanpuIndex=' + maxSanpuIndex + '>';
    htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
    htmlString+= '<div class="col s12 input-field wd">';
    htmlString+= '<input type="hidden" id="G0005Sanpu' + maxSanpuIndex + 'Value" value="0" />';
    htmlString+= '<span class="selectmodal-trigger-title item-title">散布方法</span><a href="#selectmodal"  class="selectmodal-trigger item" title="散布方法選択" data="getSanpu" displayspan="#G0005Sanpu' + maxSanpuIndex + 'Span" htext="#G0005Sanpu' + maxSanpuIndex + 'Value"><span id="G0005Sanpu' + maxSanpuIndex + 'Span">未選択</span></a>';
    htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G0005SanpuInfo-' + maxSanpuIndex + '"><i class="material-icons large">clear</i></a>';
    htmlString+= '</div>';
    //機器情報の生成
    htmlString+= '<div class="col s12 input-field wd">';
    htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/' + workinfo.kukakuid + '/getKikiOfWorkJson" displayspan="#G0005Kiki' + maxSanpuIndex + 'Span" htext="#G0005Kiki' + maxSanpuIndex + 'Value"><span id="G0005Kiki' + maxSanpuIndex + 'Span">未選択</span></a>';
    htmlString+= '<input type="hidden"  class="kiki-value-change" id="G0005Kiki' + maxSanpuIndex + 'Value" idindex="' + maxSanpuIndex + '" />';
    htmlString+= '</span>';
    htmlString+= '</div>';
    //----- アタッチメントは機器を選択するまで選択出来ない様にする -----
    htmlString+= '<div class="col s12 input-field wd">';
    htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G0005Attachment' + maxSanpuIndex + 'Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + 0 + '/getAttachmentOfKikiJson" displayspan="#G0005Attachment' + maxSanpuIndex + 'Span" htext="#G0005Attachment' + maxSanpuIndex + 'Value"><span id="G0005Attachment' + maxSanpuIndex + 'Span" class="grey-text text-darken-2">選択不可</span></a>';
    htmlString+= '<input type="hidden" id="G0005Attachment' + maxSanpuIndex + 'Value" />';
    htmlString+= '</span>';
    //------------------------------------------------------------------
    htmlString+= '</div>';

    //農肥情報の生成
    maxNouhiIndex	=	$(".nouhi-info").length;		//現在の散布方法数を取得する
    maxNouhiIndex++;
    htmlString+= MakeNouhiSanpuInput(maxNouhiIndex);

    /*----- 散布方法情報カード締め位置 -----*/
    htmlString+= '</div>';
    htmlString+= '</div>';

    /*----- 現在の散布方法情報位置を削除する -----*/
    addInfoRow.before(htmlString);
    var addNouhi = $("#G0005SanpuInfo-" + maxSanpuIndex);
    addInfoRow.remove();
    htmlString = MakeSanpuInfoAdd();

    addNouhi.after(htmlString);

    CalcInit();														//数値入力電卓初期化

    $('#G0005SanpuInfoAddBtn').unbind();							//散布情報追加時
    $('.nouhi-value-change').unbind();								//農肥選択時
    $('.kiki-value-change').unbind();								//機器選択時
    $('.info-delete').unbind();										//情報削除時
    $('.nouhi-commit-btn').unbind();								//農肥確定イベント時

    $('#G0005SanpuInfoAddBtn').bind('click', AddSunpuInfoEvent);	//散布情報追加時
    $('.nouhi-value-change').bind('change', GetNouhiValue);			//農肥選択時
    $('.kiki-value-change').bind('change', ChangeAttachmentLink);			//機器選択時
    $('.info-delete').bind('click', DeleteInfoEvent);				//情報削除時
    $('.nouhi-commit-btn').bind('click', NouhiInfoCommitEvent);		//農肥確定イベント時

    //------------------------------------------------------------------------------------------------------------------
    //- セレクトモーダルの初期化
    //------------------------------------------------------------------------------------------------------------------
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);

  }

  /* 農肥情報確定イベント発生時 */
  function NouhiInfoCommitEvent() {

    var nouhiInfoIdIndex	=	$(this).attr("idIndex");
    var targetDeleteRow		=	$("#G0005NouhiInfo-" + nouhiInfoIdIndex);
    var targetInfoRow		=	targetDeleteRow.parent();
    var htmlString			= "";									//可変HTML文字列

    /* 当画面の入力項目情報定義 */
    var checktarget = [
        { "id" : "G0005Nouhi" + nouhiInfoIdIndex + "Value"	, "name" : "農肥情報"	, "json" : "nouhiId"	, "check" : { "select" 		: "1"}}
       ,{ "id" : "G0005Bairitu" + nouhiInfoIdIndex			, "name" : "倍率"		, "json" : "bairitu"	, "check" : {					 }}
       ,{ "id" : "G0005Sanpuryo" + nouhiInfoIdIndex			, "name" : "散布量"		, "json" : "sanpuryo"	, "check" : {					 }}
       ,{ "id" : "G0005Unit" + nouhiInfoIdIndex				, "name" : "単位種別"	, "json" : "tani"		, "check" : {					 }}
    ];

    /* 入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    var bairitu = $("#G0005Bairitu" + nouhiInfoIdIndex).val();
    var sanpu 	= $("#G0005Sanpuryo" + nouhiInfoIdIndex).val();
    var unit 	= $("#G0005Unit" + nouhiInfoIdIndex).val();


    htmlString+= MakeNouhiSanpuDisplay(nouhiInfoIdIndex, $("#G0005Nouhi" + nouhiInfoIdIndex + "Value").val(), $("#G0005Nouhi" + nouhiInfoIdIndex + "Span").html(), bairitu, sanpu, unit);

    /*----- 新規農肥情報を追加する -----*/
    maxNouhiIndex++;
    htmlString+= MakeNouhiSanpuInput(maxNouhiIndex);
    targetDeleteRow.remove();
    targetInfoRow.append(htmlString);

    CalcInit();														//数値入力電卓初期化
    RangeInit();													//数値入力Range初期化
    SelectModalInit();												//選択用モーダルイベント初期化
    MultiSelectModalInit();											//複数選択用モーダルイベント初期化

    $('#G0005SanpuInfoAddBtn').unbind();							//散布情報追加時
    $('.nouhi-value-change').unbind();								//農肥選択時
    $('.kiki-value-change').unbind();								//機器選択時
    $('.info-delete').unbind();										//情報削除時
    $('.nouhi-commit-btn').unbind();								//農肥確定イベント時

    $('#G0005SanpuInfoAddBtn').bind('click', AddSunpuInfoEvent);	//散布情報追加時
    $('.nouhi-value-change').bind('change', GetNouhiValue);			//農肥選択時
    $('.kiki-value-change').bind('change', ChangeAttachmentLink);			//機器選択時
    $('.info-delete').bind('click', DeleteInfoEvent);				//情報削除時
    $('.nouhi-commit-btn').bind('click', NouhiInfoCommitEvent);		//農肥確定イベント時

    //------------------------------------------------------------------------------------------------------------------
    //- セレクトモーダルの初期化
    //------------------------------------------------------------------------------------------------------------------
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);
  }

  /* 情報削除イベント発生時 */
  function DeleteInfoEvent() {

    var deleteRow = $(this).parent("div").parent("div").parent("div");		//所属している情報カードを検索する

    deleteRow.remove();																							//カードを削除する

  }

  /* 詳細設定情報種別変更イベント時 */
  function ChangeG0005DetailSetting() {

      if ($("#G0005DetailSettingKind").is(':checked')) {	//詳細設定情報種別がコンビネーションの場合
        $("#G0005CombiInfo").show();						//コンビネーション選択を表示する
      }
      else {
        $("#G0005CombiInfo").hide();						//コンビネーション選択を非表示にする
      }

  }

  //---------------------------------------------------------------------------
  //- 区画生産物選択
  //---------------------------------------------------------------------------
  function selectKukakuCrop() {

    if (oCropInfo.cropId != $("#G0005CropValue").val()) {
      if (window.confirm('現在選択中の全区画に対して品目を変更します。\nよろしいですか？')) {
        var inputJson = {kukakuId: "", cropId:0};
        inputJson.kukakuId  = $("#G0005WorkKukakuValue").val();
        inputJson.cropId    = $("#G0005CropValue").val();
        $.ajax({
          url:"/selectMultiKukakuCrop",
          type:'POST',
          data:JSON.stringify(inputJson),               //入力用JSONデータ
          complete:function(data, status, jqXHR){           //処理成功時
            var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
            oCropInfo.kukakuId = jsonResult["kukakuId"];
            oCropInfo.cropId   = jsonResult["cropId"];
            oCropInfo.cropName = jsonResult["cropName"];
            $("#G0005CropValue").val(oCropInfo.cropId);
            $("#G0005CropSpan").text(oCropInfo.cropName);
            mSelectDataGet("#G0005HinsyuSpan", oCropInfo.kukakuId + '/getHinsyuOfCropJson');
            $("#G0005HinsyuValue").val(0);
            $("#G0005HinsyuSpan").text("未選択");
        },
          dataType:'json',
          contentType:'text/json'
        });
      }
      else {
        $("#G0005CropValue").val(oCropInfo.cropId);
        $("#G0005CropSpan").val(oCropInfo.cropName);
      }
    }
  }
  function onShukakuAllClear() {
    for (var key=1; key<=10; key++) {
      ClearShukakuInfo(key);
    }
  }
  function onShukakuClear() {
    var key  = $(this).attr("key");
    ClearShukakuInfo(key);
  }
  function ClearShukakuInfo(key) {
    $("#G0005Nisugata" + key + "Value").val("0");
    $("#G0005Shitu" + key + "Value").val("0");
    $("#G0005Size" + key + "Value").val("0");
    $("#G0005Kosu" + key + "").val("0");
    $("#G0005ShukakuRyo" + key + "").val("0");
    $("#G0005Nisugata" + key + "Span").text("未選択");
    $("#G0005Shitu" + key + "Span").text("未選択");
    $("#G0005Size" + key + "Span").text("未選択");
    $("#G0005Kosu" + key + "Span").text("0");
    $("#G0005ShukakuRyo" + key + "Span").text("0");

    $("#G0005Capa" + key).val(0);
    $('#G0005Capa' + key + 'Span').text("この荷姿の内容量は" + 0 + "gです");
  }
  function GetNisugataInfo() {
    var key  = $(this).attr("key");
    var code = $(this).val();
    if (code == null || code == "" || isNaN(code)) {
      code = "0";
    }
    var url  = "/" + code + "/getNisugataInfo"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        if (jsonResult.result == "SUCCESS") {
          $("#G0005Capa" + key).val(jsonResult.capacity);
          $('#G0005Capa' + key + 'Span').text("この荷姿の内容量は" + jsonResult.capacity + "gです");
        }
        else {
          $("#G0005Capa" + key).val(0);
          $('#G0005Capa' + key + 'Span').text("この荷姿の内容量は" + 0 + "gです");
        }
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function CalShukakuryo() {
    var key  = $(this).attr("key");
    var capa = $("#G0005Capa" + key).val();
    var kosu = $(this).val();

    if (capa == 0) {
      displayToast('内容量が0gの為、収穫量の自動算出を行いませんでした。', 4000, 'rounded');
    }
    else {
      var shukakuryo = (capa * kosu) / 1000;
      $("#G0005ShukakuRyo" + key + "Span").text(shukakuryo);
      $("#G0005ShukakuRyo" + key).val(shukakuryo);
    }
  }
})(jQuery); // end of jQuery name space