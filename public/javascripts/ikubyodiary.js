/* AGRYEEL 育苗日誌画面 JQUERY */
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

  var workinfo = {workid: 0, worktemplateid: 0, naeNo: "", workdateautoset: 0, init: true};

  var oCropInfo = {cropId: 0, cropName: ""};

  var youkiUnit	= {"0":"", "1":"枚", "2":"穴", "3":"個"};								//容器単位種別
  var soilUnit	= {"0":"", "1":"Kg", "2":"L"};											//土単位種別

  var oInputJson = {};
  var ikubyoPlanId = 0;

  /* 初期処理時イベント */
  $(function(){

    /*----- 各ボタンにクリックイベントを押下する -----*/

    $(document).ready(function(){
      $('.collapsible').collapsible({
        accordion : false // A setting that changes the collapsible behavior to expandable instead of the default accordion style
      });

      $("#G1005WorkDate").val(workdate);

      /*----- 各ボタンにクリックイベントを押下する -----*/
      $('#G1005SubmitButton').bind('click', SubmitTap);				//日誌記録ボタン
      $('#G1005BackButton').bind('click', BackTap);                 //日誌記録ボタン
      $('.nae-value-change').bind('change', GetNaeZaikoInfo);       //苗選択時
      $('.youki-value').bind('change', GetYoukiInfo);               //容器選択時
      $('.baido-value').bind('change', GetBaidoInfo);               //培土選択時
      $('.fukudo-value').bind('change', GetFukudoInfo);             //覆土選択時
      $('.suryo-value').bind('change', CalKosu);                    //数量変更時
      $('.haikiryo-value').bind('change', CalHaikiSuryo);           //廃棄量変更時

      $('.crop-value-change').bind('change', ChangeHinsyuLink);		//生産物選択時

      $('.kiki-value-change').bind('change', ChangeAttachmentLink);	//機器選択時
      $('.nouhi-value-change').bind('change', GetNouhiValue);					//農肥選択時
      $('#G1005SanpuInfoAddBtn').bind('click', AddSunpuInfoEvent);				//散布情報追加時
      $('.info-delete').bind('click', DeleteInfoEvent);							//情報削除時
      $('.nouhi-commit-btn').bind('click', NouhiInfoCommitEvent);				//農肥確定イベント時
      $('#G1005DetailSettingKind').bind('change', ChangeG1005DetailSetting);	//詳細設定種別変更時

      /*------ コンビネーションの初期表示を手動にする -----*/
      $("#G1005CombiInfo").hide();

      /*------ フローティングアクションボタン制御 -----*/
      if (userinfo.workDiaryDiscription == 0) { //注釈表示しない
          $(".btn-description").hide();
      }

    });

      $.ajax({
        url:"/ikubyoDiaryInit", 											//育苗日誌初期処理
          type:'GET',
          complete:function(data, status, jqXHR){							//処理成功時
          var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
          var htmlString	= "";											//可変HTML文字列
          var systemDate = new Date( jQuery.now() ).toLocaleString();

          workinfo.workid           = jsonResult["workId"];
          if(jsonResult["ikubyoDiaryId"] == "") {
            workinfo.naeNo            = "new";
          }
          else {
            workinfo.naeNo            = jsonResult["naeNo"];
          }
          workinfo.worktemplateid   = jsonResult["workTemplateId"];
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

          case 6://潅水作業

              htmlString+= MakeKansuiItem(jsonResult);
              break;

          case 21: //苗播種作業

            htmlString+= MakeNaeHashuItem(jsonResult);
            break;

          case 22://刈り取り作業

            htmlString+= MakeKaritoriItem(jsonResult);
            break;

          case 23://廃棄作業

            htmlString+= MakeHaikiItem(jsonResult);
            break;

          }
          //----- 作業項目の生成 END -----

          //----- 共通項目フッタ部の生成 START -----
          htmlString+= '<div class="row">';
          //作業時間
          htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<span class="item-title">作業時間</span><span id="G1005WorkTimeSpan" class="item">' + jsonResult["workTime"] + '</span><span class="item">分</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005WorkTime">keyboard</i>';
          htmlString+= '<input type="hidden" id="G1005WorkTime" value="' + jsonResult["workTime"] + '"/>';
          htmlString+= '</div>';
          //作業開始日時の作成
          htmlString+= '<div class="col s12 wd">';
          htmlString+= '<span class="item-title">作業開始日時</span>';
          htmlString+= '</div>';
          htmlString+= '<div class="col s6 input-field wd">';
          htmlString+= '<input type="text" placeholder="開始日付" id="G1005StartDate" class="datepicker item input-text-color" style="" value="' + jsonResult["startDate"] + '">';
          htmlString+= '</div>';
          htmlString+= '<div class="col s6 input-field wd">';
          htmlString+= '<input type="text" placeholder="開始時間" id="G1005StartTime" class="timepicker item input-text-color" style="" value="' + jsonResult["startTime"] + '">';
          htmlString+= '</div>';
          //作業終了日時の作成
          htmlString+= '<div class="col s12 wd">';
          htmlString+= '<span class="item-title">作業終了日時</span>';
          htmlString+= '</div>';
          htmlString+= '<div class="col s6 input-field wd">';
          htmlString+= '<input type="text" placeholder="終了日付" id="G1005EndDate" class="datepicker item input-text-color" style="" value="' + jsonResult["endDate"] + '">';
          htmlString+= '</div>';
          htmlString+= '<div class="col s6 input-field wd">';
          htmlString+= '<input type="text" placeholder="終了時間" id="G1005EndTime" class="timepicker item input-text-color" style="" value="' + jsonResult["endTime"] + '">';
          htmlString+= '</div>';
          //担当者の生成
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<span class="selectmodal-trigger-title item-title">担当者</span><a href="#selectmodal"  class="selectmodal-trigger item" title="担当者選択" data="getAccountOfFarm" displayspan="#G1005WorkAccountSpan" htext="#G1005WorkAccountValue"><span id="G1005WorkAccountSpan">' + jsonResult["accountName"] + '</span></a>';
          htmlString+= '<input type="hidden" id="G1005WorkAccountValue" value="' + jsonResult["accountId"] + '"/>';
          htmlString+= '</div>';
          htmlString+= '</div>';
          htmlString+= '</div>';

          //作業IDの保存
          htmlString+= '<input type="hidden" id="G1005WorkId" value="' + jsonResult["workId"] + '"/>';

          //育苗記録ＩＤの保存
          htmlString+= '<input type="hidden" id="G1005IkubyoDiaryId" value="' + jsonResult["ikubyoDiaryId"] + '"/>';

          //----- 共通項目フッタ部の生成 END -----

          $("#G1005IkubyoDiary").html(htmlString);							//可変HTML部分に反映する

          CalcInit();														   //数値入力電卓初期化

          //------------------------------------------------------------------------------------------------------------------
          //- 削除ボタンの制御
          //------------------------------------------------------------------------------------------------------------------
          if(jsonResult["ikubyoDiaryId"] == "") {
            $('#G1005DeleteButton').hide();
          }
          else {
            $('#G1005DeleteButton').show();
          }
          $('#G1005DeleteButton').unbind('click');
          $('#G1005DeleteButton').bind('click', ikubyodiaryDelete);
          //------------------------------------------------------------------------------------------------------------------
          //- 複数区画反映
          //------------------------------------------------------------------------------------------------------------------
          //----- 区画-----
/*
          mSelectDataGet("#G1005WorkKukakuSpan", "getCompartmentOfFarm");
          var fgs = new String(jsonResult.kukakuId).split(",");
          for (var key in fgs) {
            var data = fgs[key];
            var oJson = mSelectData(data);
            if (oJson != undefined) {
              oJson.select = true;
            }
          }
          mSelectClose();
*/
          //------------------------------------------------------------------------------------------------------------------
          //- 複数品種情報反映
          //------------------------------------------------------------------------------------------------------------------
          //----- 圃場グループ -----
          //switch(jsonResult["workTemplateId"]) {
          //case 3: //播種作業
/*
            mSelectDataGet("#G1005HinsyuSpan", workinfo.kukakuid + "/getHinsyuOfCropJson");
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
*/
          //------------------------------------------------------------------------------------------------------------------
          //- 作業開始ボタンの制御
          //------------------------------------------------------------------------------------------------------------------
          if(jsonResult["ikubyoDiaryId"] == "") {
            $('#G1005StartButton').show();
          }
          else {
            $('#G1005StartButton').hide();
          }
          switch(jsonResult["workTemplateId"]) {
          case 7: //作付開始の場合

            $('#G1005StartButton').hide();
            break;

          }
          $('#G1005StartButton').unbind('click');
          $('#G1005StartButton').bind('click', workStart);
          //------------------------------------------------------------------------------------------------------------------
          //- 作業切替一覧の制御
          //------------------------------------------------------------------------------------------------------------------
          //----- 作業切替-----
          if (userinfo.change == 0) {
            selectDataGet("#G1005WorkReorder", "getWorkOfIkubyo");
          }
          else {
            selectDataGet("#G1005WorkReorder", "getWorkOfFarm");
          }
          $('#G1005WorkReorderValue').unbind('change');
          $('#G1005WorkReorderValue').bind('change', workChange);
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
          $('.mselectsubmodal-trigger').unbind('click');
          $('.mselectsubmodal-trigger').bind('click', mSelectSubOpen);
          $('.selectsubmodal-trigger').unbind('click');
          $('.selectsubmodal-trigger').bind('click', selectSubOpen);
          //------------------------------------------------------------------------------------------------------------------
          //- 作業時間を再度設定する
          //------------------------------------------------------------------------------------------------------------------
          $("#G1005WorkTimeSpan").text(jsonResult["workTime"]);
          $("#G1005WorkTime").val(jsonResult["workTime"]);
          //------------------------------------------------------------------------------------------------------------------
          //- 作業日付,作業開始日時、作業終了日時のイベント設定
          //------------------------------------------------------------------------------------------------------------------
          $('#G1005WorkDate').unbind('change');
          $('#G1005WorkDate').bind('change', autoWorktime);
          $('#G1005StartDate').unbind('change');
          $('#G1005StartDate').bind('change', autoWorktime);
          $('#G1005StartTime').unbind('change');
          $('#G1005StartTime').bind('change', autoWorktime);
          $('#G1005EndDate').unbind('change');
          $('#G1005EndDate').bind('change', autoWorktime);
          $('#G1005EndTime').unbind('change');
          $('#G1005EndTime').bind('change', autoWorktime);
          workinfo.init = false;
          //------------------------------------------------------------------------------------------------------------------
          //- 育苗計画ＩＤの保存
          //------------------------------------------------------------------------------------------------------------------
          if (jsonResult.ikubyoPlanId == "") {
            ikubyoPlanId = 0;
          }
          else {
            ikubyoPlanId = parseInt(jsonResult.ikubyoPlanId);
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
      oInputJson["ikubyoPlanType"] = 1; //作業記録からストップウォッチにて記録
      if (dataToJson() == false) {
        return false;
      }
      var url = "/checkNouhiIkubyo";

      $.ajax({
        url:url,                        //育苗日誌保存処理
        type:'POST',
        data:JSON.stringify(oInputJson),              //入力用JSONデータ
        complete:function(data, status, jqXHR){       //処理成功時

          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
          if (jsonResult.result != 0) {
            var result = window.confirm('下記の警告が発生しています。\n作業を開始しますか？\n' + jsonResult.message);
            if (result) {
              SubmitIkubyoPlan(oInputJson);                      //作業日誌保存処理
            }
          }
          else {
            SubmitIkubyoPlan(oInputJson);                      //作業日誌保存処理
          }
        },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
    }
  }
  /* 入力した内容を作業計画に保存する */
  function SubmitIkubyoPlan(jsondata){

    if (nowproc == true) {

          displayToast('現在、育苗記録の保存中です', 4000, 'rounded');
          return;

    }

    nowproc = true;

    jsondata["ikubyoPlanId"] = ikubyoPlanId;

    var url = "/submitIkubyoPlan";

    /* 登録アニメーション実行 */
    $('.workdiary-commit').addClass("commit");

    $.ajax({
        url:url,                        //育苗日誌保存処理
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
              window.location.href = "/ikubyoMove";
            }
          }
        },
        error:function(data, status, jqXHR){                //処理成功時

          nowproc = false;
          displayToast('育苗記録の開始に失敗しました', 4000, 'rounded');

        },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
  } // end of SubmitIkubyoDiary
  //------------------------------------------------------------------------------------------------------------------
  //- 作業切替を実行する
  //------------------------------------------------------------------------------------------------------------------
  function workChange() {
    var work = $('#G1005WorkReorderValue').val();
    if (work != 0) {
      //作業切替時はバックモードを0にする
      localStorage.setItem("backMode"         , "0");
      localStorage.setItem("backKukakuId"     , null);
      localStorage.setItem("backWorkId"       , work);
      localStorage.setItem("backFieldGroupId" , null);
      window.location.href = '/' + work + '/NONE/ikubyoDiaryMove';
      return false;
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
    if((obj.attr("id") != "G1005WorkDate")
        && (obj.attr("id") != "G1005StartDate")
        && (obj.attr("id") != "G1005EndDate")) {
      return;
    }
    switch (workinfo.workdateautoset) {
    case 1:
      //作業開始日付→作業日付、作業終了日付
      if(obj.attr("id") == "G1005StartDate") {
        $("#G1005WorkDate").val($("#G1005StartDate").val());
        $('#G1005WorkDate').datepicker('setDate', new Date($("#G1005WorkDate").val()));
        $("#G1005EndDate").val($("#G1005StartDate").val());
        $('#G1005EndDate').datepicker('setDate', new Date($("#G1005EndDate").val()));
        displayToast("「作業開始日付→作業日付、作業終了日付」にて自動設定しました。", 2000, '');
        return;
      }
      break;
    case 2:
      //作業日付→作業開始日付、作業終了日付
      if(obj.attr("id") == "G1005WorkDate") {
        $("#G1005StartDate").val($("#G1005WorkDate").val());
        $('#G1005StartDate').datepicker('setDate', new Date($("#G1005StartDate").val()));
        $("#G1005EndDate").val($("#G1005WorkDate").val());
        $('#G1005EndDate').datepicker('setDate', new Date($("#G1005EndDate").val()));
        displayToast("「作業日付→作業開始日付、作業終了日付」にて自動設定しました。", 2000, '');
        return;
      }
      break;
    case 3:
      //作業終了日付→作業開始日付
      if(obj.attr("id") == "G1005EndDate") {
        $("#G1005StartDate").val($("#G1005EndDate").val());
        $('#G1005StartDate').datepicker('setDate', new Date($("#G1005StartDate").val()));
        displayToast("「作業終了日付→作業開始日付」にて自動設定しました。", 2000, '');
        return;
      }
      break;
    case 4:
      //作業終了日付→作業日付、作業開始日付
      if(obj.attr("id") == "G1005EndDate") {
        $("#G1005WorkDate").val($("#G1005EndDate").val());
        $('#G1005WorkDate').datepicker('setDate', new Date($("#G1005WorkDate").val()));
        $("#G1005StartDate").val($("#G1005EndDate").val());
        $('#G1005StartDate').datepicker('setDate', new Date($("#G1005StartDate").val()));
        displayToast("「作業終了日付→作業日付、作業開始日付」にて自動設定しました。", 2000, '');
        return;
      }
      break;
    default:
      //作業開始日付→作業日付
      if(obj.attr("id") == "G1005StartDate") {
        $("#G1005WorkDate").val($("#G1005StartDate").val());
        $('#G1005WorkDate').datepicker('setDate', new Date($("#G1005WorkDate").val()));
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
        { "id" : "G1005WorkDate"        , "name" : "作業日付"     , "json" : "workDate"        , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005StartDate"       , "name" : "作業開始日付"  , "json" : "startDate"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005StartTime"       , "name" : "作業開始時間"  , "json" : "startTime"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005EndDate"         , "name" : "作業終了日付"  , "json" : "endDate"        , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005EndTime"         , "name" : "作業終了時間"  , "json" : "endTime"        , "check" : { "required"  : "1"}}
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
    var wStart = new Date($("#G1005StartDate").val() + " " + $("#G1005StartTime").val());
    var wEnd   = new Date($("#G1005EndDate").val() + " " + $("#G1005EndTime").val());

    console.log(wStart);
    console.log(wEnd);

    if (wEnd < wStart) {
      displayToast("作業終了日時が作業開始日時以前に設定されている為、\n作業時間の自動算出は行いません。", 4000, '');
      return;
    }

    //----- 作業時間算出-----
    var diff = wEnd - wStart;
    diff = Math.floor(diff / (1000 * 60))
    $("#G1005WorkTime").val(diff);
    $("#G1005WorkTimeSpan").text(diff);

  }

  function ikubyodiaryDelete() {
    if (confirm("この作業記録を削除しますがよろしいですか？")) {
      var url = "/" + $("#G1005IkubyoDiaryId").val() + "/ikubyoDiaryDelete";

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
              window.location.href = "/ikubyoMove";
            }

          },
          dataType:'json',
          contentType:'text/json'
        });
    }
  }

  function BackTap() {
    window.location.href = "/ikubyoMove";
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
      htmlString+= '<span class="item-title">作業日付</span><input type="text" placeholder="作業日付" id="G1005WorkDate" class="datepicker item input-text-color" style="" value="' + jsonResult["workDate"] + '">';
      htmlString+= '</div>';
      workdate   = jsonResult["workDate"];

      if (workinfo.worktemplateid != 21) {
        //苗の生成
        htmlString+= '<div class="col s12 input-field wd">';
        if (jsonResult["naeNo"] == "") {
            if (workinfo.worktemplateid == 23) {
              htmlString+= '<span class="selectmodal-trigger-title item-title">苗</span><a href="#selectsubmodal"  class="selectsubmodal-trigger item" title="苗選択" data="getNaeOfFarm" displayspan="#G1005WorkNaeSpan" htext="#G1005WorkNaeValue"><span id="G1005WorkNaeSpan">未選択</span></a>';
              htmlString+= '<input type="hidden" class="nae-value-change" id="G1005WorkNaeValue" />';
            }
            else {
              htmlString+= '<span class="selectmodal-trigger-title item-title">苗</span><a href="#mselecsubtmodal"  class="mselectsubmodal-trigger item" title="苗選択" data="getNaeOfFarm" displayspan="#G1005WorkNaeSpan" htext="#G1005WorkNaeValue"><span id="G1005WorkNaeSpan">未選択</span></a>';
              htmlString+= '<input type="hidden" id="G1005WorkNaeValue" />';
            }
        }
        else {
            if (workinfo.worktemplateid == 23) {
              htmlString+= '<span class="selectmodal-trigger-title item-title">苗</span><a href="#selectsubmodal"  class="selectsubmodal-trigger item" title="苗選択" data="getNaeOfFarm" displayspan="#G1005WorkNaeSpan" htext="#G1005WorkNaeValue"><span id="G1005WorkNaeSpan">' + jsonResult["naeName"] + '</span></a>';
              htmlString+= '<input type="hidden" class="nae-value-change" id="G1005WorkNaeValue" value="' + jsonResult["naeNo"] + '"/>';
            }
            else {
              htmlString+= '<span class="selectmodal-trigger-title item-title">苗</span><a href="#mselectsubmodal"  class="mselectsubmodal-trigger item" title="苗選択" data="getNaeOfFarm" displayspan="#G1005WorkNaeSpan" htext="#G1005WorkNaeValue"><span id="G1005WorkNaeSpan">' + jsonResult["naeName"] + '</span></a>';
              htmlString+= '<input type="hidden" id="G1005WorkNaeValue" value="' + jsonResult["naeNo"] + '"/>';
            }
        }
        htmlString+= '</div>';
      }
      htmlString+= '</div>';

      htmlString+= '</div>';

      return htmlString;

  }

  /* 初期表示農肥情報を生成する */
  function MakeInitNouhiInfo(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */
      var makeHistry	= false;

      //コンビネーションの生成
      htmlString+= '<div class="row" id="G1005CombiInfo">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4">';
      htmlString+= '<div class="card workdiary-commit">';
      htmlString+= '<div class="card-content">';
      htmlString+= '<blockquote><span>コンビネーション<a href="#G1005ModalCombi" id="G1005Combi" class="collection-item modal-trigger" combiId=""><span id="G1005CombiSpan" class="blockquote-input">未選択</span></a></blockquote>';
      htmlString+= '<input type="hidden" id="G1005CombiValue" />';
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
              htmlString+= '<div class="row sanpu-info" id="G1005SanpuInfo-' + maxSanpuIndex + '" sanpuIndex=' + maxSanpuIndex + '>';
              htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<input type="hidden" id="G1005Sanpu' + maxSanpuIndex + 'Value" value="' + workHistryBaseData.sanpuMethod + '" />';
              htmlString+= '<span class="selectmodal-trigger-title item-title">散布方法</span><a href="#selectmodal"  class="selectmodal-trigger item" title="散布方法選択" data="getSanpu" displayspan="#G1005Sanpu' + maxSanpuIndex + 'Span" htext="#G1005Sanpu' + maxSanpuIndex + 'Value"><span id="G1005Sanpu' + maxSanpuIndex + 'Span">' + workHistryBaseData.sanpuMethodName + '</span></a>';
              htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G1005SanpuInfo-' + maxSanpuIndex + '"><i class="material-icons large">clear</i></a>';
              htmlString+= '</div>';
              //機器情報の生成
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/36/getKikiOfWorkChainJson" displayspan="#G1005Kiki' + maxSanpuIndex + 'Span" htext="#G1005Kiki' + maxSanpuIndex + 'Value"><span id="G1005Kiki' + maxSanpuIndex + 'Span">' + workHistryBaseData.kikiName + '</span></a>';
              htmlString+= '<input type="hidden"  class="kiki-value-change" id="G1005Kiki' + maxSanpuIndex + 'Value" idindex="' + maxSanpuIndex + '" value="'  + workHistryBaseData.kikiId + '"/>';
              htmlString+= '</span>';
              htmlString+= '</div>';
              //----- アタッチメントは機器を選択するまで選択出来ない様にする -----
              var attachmentName = "未選択";
              if (workHistryBaseData.attachmentId != 0) {
                  attachmentName = workHistryBaseData.attachmentName;
              }
              htmlString+= '<div class="col s12 input-field wd">';
              htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G1005Attachment' + maxSanpuIndex + 'Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + workHistryBaseData.kikiId + '/getAttachmentOfKikiJson" displayspan="#G1005Attachment' + maxSanpuIndex + 'Span" htext="#G1005Attachment' + maxSanpuIndex + 'Value"><span id="G1005Attachment' + maxSanpuIndex + 'Span">' + attachmentName + '</span></a>';
              htmlString+= '<input type="hidden" id="G1005Attachment' + maxSanpuIndex + 'Value" value="' + workHistryBaseData.attachmentId  + '"/>';
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
          htmlString+= '<div class="row sanpu-info" id="G1005SanpuInfo-' + maxSanpuIndex + '" sanpuIndex=' + maxSanpuIndex + '>';
          htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<input type="hidden" id="G1005Sanpu' + maxSanpuIndex + 'Value" value="0" />';
          htmlString+= '<span class="selectmodal-trigger-title item-title">散布方法</span><a href="#selectmodal"  class="selectmodal-trigger item" title="散布方法選択" data="getSanpu" displayspan="#G1005Sanpu' + maxSanpuIndex + 'Span" htext="#G1005Sanpu' + maxSanpuIndex + 'Value"><span id="G1005Sanpu' + maxSanpuIndex + 'Span">未選択</span></a>';
          htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G1005SanpuInfo-' + maxSanpuIndex + '"><i class="material-icons large">clear</i></a>';
          htmlString+= '</div>';
          //機器情報の生成
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/36/getKikiOfWorkChainJson" displayspan="#G1005Kiki' + maxSanpuIndex + 'Span" htext="#G1005Kiki' + maxSanpuIndex + 'Value"><span id="G1005Kiki' + maxSanpuIndex + 'Span">未選択</span></a>';
          htmlString+= '<input type="hidden"  class="kiki-value-change" id="G1005Kiki' + maxSanpuIndex + 'Value" idindex="' + maxSanpuIndex + '" />';
          htmlString+= '</span>';
          htmlString+= '</div>';
          //----- アタッチメントは機器を選択するまで選択出来ない様にする -----
          htmlString+= '<div class="col s12 input-field wd">';
          htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G1005Attachment' + maxSanpuIndex + 'Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + 0 + '/getAttachmentOfKikiJson" displayspan="#G1005Attachment' + maxSanpuIndex + 'Span" htext="#G1005Attachment' + maxSanpuIndex + 'Value"><span id="G1005Attachment' + maxSanpuIndex + 'Span">未選択</span></a>';
          htmlString+= '<input type="hidden" id="G1005Attachment' + maxSanpuIndex + 'Value" />';
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
    var link = $("#G1005Attachment" + kiki.attr("idindex") + "Link");
    link.attr("data", kiki.val() + "/getAttachmentOfKikiJson");
    selectJsonRemove("#G1005Attachment" + kiki.attr("idindex") + "Span");

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

  /* 苗播種項目を生成する */
  function MakeNaeHashuItem(jsonResult) {

      var htmlString	=	"";		/* 共通項目マークアップ */

      /*----- 苗播種情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">苗播種情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //機器情報の生成
      htmlString+= '<div class="row" id="G1005NaeHashuInfo">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">品目</span><a href="#selectmodal"  class="selectmodal-trigger item" title="品目選択" data="'+ userinfo.farm + '/getCrop" displayspan="#G1005CropSpan" htext="#G1005CropValue"><span id="G1005CropSpan">' + jsonResult["cropSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" class="crop-value-change" id="G1005CropValue" value="' + jsonResult["cropId"] + '" />';
      htmlString+= '<input type="hidden" id="G1005WorkNaeValue" value="new"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">品種</span><a href="#selectmodal" id="G1005HinsyuLink"  class="selectmodal-trigger item" title="品種選択" data="' + jsonResult["cropId"] + '/getHinsyuOfCropToCropJson" displayspan="#G1005HinsyuSpan" htext="#G1005HinsyuValue"><span id="G1005HinsyuSpan">' + jsonResult["hinsyuSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" id="G1005HinsyuValue" value="' + jsonResult["hinsyuId"] + '" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">容器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="容器選択" data="getYoukiOfFarmJson" displayspan="#G1005YoukiSpan" htext="#G1005YoukiValue"><span id="G1005YoukiSpan">' + jsonResult["youkiSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" class="youki-value" id="G1005YoukiValue" value="' + jsonResult["youkiId"] + '" />';
      htmlString+= '<input type="hidden" id="G1005YoukiKosu" value="' + jsonResult["youkiKosu"] + '" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">数量</span><span id="G1005SuryoSpan" class="item">' + jsonResult["naeSuryo"] + '</span><span class="item" id="G1005YoukiUnitSpan">' + youkiUnit[jsonResult["youkiUnit"]] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005Suryo">keyboard</i><span class="item2 right" id="G1005KosuUnitSpan">個</span><span class="item2 right" id="G1005KosuSpan">' + jsonResult["kosu"] + '</span>';
      htmlString+= '<input type="hidden" class="suryo-value" id="G1005Suryo" value="' + jsonResult["naeSuryo"] + '"/>';
      htmlString+= '<input type="hidden" id="G1005Kosu" value="' + jsonResult["kosu"] + '"/>';
      htmlString+= '<input type="hidden" id="G1005YoukiUnit" value="' + youkiUnit[jsonResult["youkiUnit"]] + '" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">培土</span><a href="#selectmodal"  class="selectmodal-trigger item" title="培土選択" data="1/getSoilOfFarmJson" displayspan="#G1005BaidoSpan" htext="#G1005BaidoValue"><span id="G1005BaidoSpan">' + jsonResult["baidoSpan"] + '</span></a><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005BaidoSuryo">keyboard</i><span class="item2 right" id="G1005BaidoUnitSpan">' + soilUnit[jsonResult["baidoUnit"]] + '</span><span id="G1005BaidoSuryoSpan" class="item2 right">' + jsonResult["baidoSuryo"] + '</span>';
      htmlString+= '<input type="hidden" class="baido-value" id="G1005BaidoValue" value="' + jsonResult["baidoId"] + '" />';
      htmlString+= '<input type="hidden" id="G1005BaidoSuryo" value="' + jsonResult["baidoSuryo"] + '"/>';
      htmlString+= '<input type="hidden" id="G1005BaidoUnit" value="' + soilUnit[jsonResult["baidoUnit"]] + '" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">覆土</span><a href="#selectmodal"  class="selectmodal-trigger item" title="覆土選択" data="2/getSoilOfFarmJson" displayspan="#G1005FukudoSpan" htext="#G1005FukudoValue"><span id="G1005FukudoSpan">' + jsonResult["fukudoSpan"] + '</span></a><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005FukudoSuryo">keyboard</i><span class="item2 right" id="G1005FukudoUnitSpan">' + soilUnit[jsonResult["fukudoUnit"]] + '</span><span id="G1005FukudoSuryoSpan" class="item2 right">' + jsonResult["fukudoSuryo"] + '</span>';
      htmlString+= '<input type="hidden" class="fukudo-value" id="G1005FukudoValue" value="' + jsonResult["fukudoId"] + '" />';
      htmlString+= '<input type="hidden" id="G1005FukudoSuryo" value="' + jsonResult["fukudoSuryo"] + '"/>';
      htmlString+= '<input type="hidden" id="G1005FukudoUnit" value="' + soilUnit[jsonResult["fukudoUnit"]] + '" />';
      htmlString+= '</div>';

      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  function ChangeHinsyuLink() {

	    var crop = $(this);
	    var link = $("#G1005HinsyuLink");
	    link.attr("data", crop.val() + "/getHinsyuOfCropToCropJson");
	    selectJsonRemove("#G1005HinsyuSpan");
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
      htmlString+= '<span class="selectmodal-trigger-title item-title">潅水方法</span><a href="#selectmodal"  class="selectmodal-trigger item" title="潅水方法選択" data="getKansui" displayspan="#G1005KansuiSpan" htext="#G1005KansuiValue"><span id="G1005KansuiSpan">' + jsonResult["kansuiSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" id="G1005KansuiValue" value="' + jsonResult["kansuiMethod"] + '" />';
      htmlString+= '</div>';
      //機器情報の生成
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/36/getKikiOfWorkChainJson" displayspan="#G1005KikiSpan" htext="#G1005KikiValue"><span id="G1005KikiSpan">' + jsonResult["kikiSpan"] + '</span></a>';
      htmlString+= '<input type="hidden" class="kiki-value-change" id="G1005KikiValue" value="' + jsonResult["kikiId"] + '" idindex="1" />';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">潅水量</span><span id="G1005KansuiRyoSpan" class="item">' + jsonResult["kansuiryo"] + '</span><span class="item">L</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005KansuiRyo">keyboard</i>';
      htmlString+= '<input type="hidden" id="G1005KansuiRyo" value="' + jsonResult["kansuiryo"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  /* 刈り取り項目を生成する */
  function MakeKaritoriItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- マルチ情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">刈り取り情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">刈取高</span><span id="G1005SenteiHeightSpan" class="item">' + jsonResult["senteiHeight"] + '</span><span class="item">cm</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005SenteiHeight">keyboard</i>';
      htmlString+= '<input type="hidden" id="G1005SenteiHeight" value="' + jsonResult["senteiHeight"] + '"/>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  /* 廃棄項目を生成する */
  function MakeHaikiItem(jsonResult) {

      var htmlString  = "";   /* 共通項目マークアップ */

      /*----- 廃棄情報 -----*/
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 wd">';
      htmlString+= '<span class="title">廃棄情報</span>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">廃棄量</span><span id="G1005HaikiRyoSpan" class="item">' + jsonResult["haikiRyo"] + '</span><span class="item">個&nbsp;&nbsp;&nbsp;&nbsp;</span><span  id="G1005HaikiSuryoSpan" class="item2">(' + jsonResult["haikiSuryo"] + youkiUnit[jsonResult["haikiUnit"]] +')</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005HaikiRyo">keyboard</i>';
      htmlString+= '<input type="hidden" class="haikiryo-value" id="G1005HaikiRyo" value="' + jsonResult["haikiRyo"] + '"/>';
      htmlString+= '<input type="hidden" id="G1005HaikiSuryo" value="' + jsonResult["haikiSuryo"] + '"/>';
      htmlString+= '<input type="hidden" id="G1005ZaikoKosu" value="' + jsonResult["zaikoKosu"] + '"/>';
      htmlString+= '<input type="hidden" id="G1005HaikiUnit" value="' + youkiUnit[jsonResult["haikiUnit"]] + '" />';
      htmlString+= '<input type="hidden" id="G1005Kosu" value="' + jsonResult["kosu"] + '" />';
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
      htmlString += '<input type="checkbox" id="G1005DetailSettingKind">';
      htmlString += '<span class="lever"></span>';
      htmlString += 'コンビ';
      htmlString += '</label>';
      htmlString += '</div>';

      return htmlString;

  }

  /* 農肥散布情報表示マークアップ生成 */
  function MakeNouhiSanpuDisplay(nouhiIndex, nouhiId, nouhiName, bairitu, sanpuryo, unit) {

      var htmlString	=	"";		/* 農肥情報表示マークアップ */

//      htmlString+= '<div class="row nouhi-info" id="G1005NouhiInfo-' + nouhiIndex + '" nouhiIndex="' + nouhiIndex + '">';
//      htmlString+= '<div class="">';
//      htmlString+= '<div class="col s12 info-line">';
//      htmlString+= '<div class="col s12 input-field wd">';
//      htmlString+= '<span class="item-title">農薬/肥料情報</span><span id="G1005Nouhi' + nouhiIndex + 'Span" class="item commit">' + nouhiName + '</span>';
//      htmlString+= '<input type="hidden" id="G1005Nouhi' + nouhiIndex + 'Value" class="nouhi-value-change" idindex="' + nouhiIndex + '" value="' + nouhiId + '"/>';
//      htmlString+= '</div>';
//      htmlString+= '<div class="col s12 input-field wd">';
//      htmlString+= '<span class="item-title">倍率</span><span id="G1005Bairitu' + nouhiIndex + 'Span" class="item commit">' + bairitu + '倍</span>';
//      htmlString+= '<input type="hidden" id="G1005Bairitu' + nouhiIndex + '" value="' + bairitu + '" />';
//      htmlString+= '</div>';
//      htmlString+= '<div class="col s12 input-field wd">';
//      htmlString+= '<span class="item-title">散布量</span><span id="G1005Sanpuryo' + nouhiIndex + 'Span" class="item commit">' + sanpuryo + unit + '</span>';
//      htmlString+= '<input type="hidden" id="G1005Sanpuryo' + nouhiIndex + '" value="' + sanpuryo + '" />';
//      htmlString+= '<input type="hidden" id="G1005Unit' + nouhiIndex + '" value="' + unit + '" />';
//      htmlString+= '</div>';
//      htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G1005NouhiInfo-' + nouhiIndex + '"><i class="material-icons large">clear</i></a>';
//      htmlString+= '</div>';
//      htmlString+= '</div>';
//      htmlString+= '</div>';

      htmlString+= '<div class="row nouhi-info" id="G1005NouhiInfo-' + nouhiIndex + '" nouhiIndex="' + nouhiIndex + '">';
      htmlString+= '<div class="">';
      htmlString+= '<div class="col s12 info-line">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">農薬/肥料情報</span><a href="#selectmodal"  class="selectmodal-trigger item" title="農薬/肥料選択" data="' + workinfo.workid + '/36/getNouhiOfWorkChainJson" displayspan="#G1005Nouhi' + nouhiIndex + 'Span" htext="#G1005Nouhi' + nouhiIndex + 'Value"><span id="G1005Nouhi' + nouhiIndex + 'Span">'+ nouhiName + '</span></a>';
      htmlString+= '<input type="hidden" id="G1005Nouhi' + nouhiIndex + 'Value" class="nouhi-value-change" idindex="' + nouhiIndex + '" value="' + nouhiId + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">倍率</span><span id="G1005Bairitu' + nouhiIndex + 'Span" class="item">' + bairitu + '</span><span class="item">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005Bairitu' + nouhiIndex + '">keyboard</i>';
      htmlString+= '<input type="hidden" id="G1005Bairitu' + nouhiIndex + '" value="' + bairitu + '"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">散布量</span><span id="G1005Sanpuryo' + nouhiIndex + 'Span" class="item">' + sanpuryo + '</span><span class="item" id="G1005Unit' + nouhiIndex + 'Span">' + unit + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005Sanpuryo' + nouhiIndex + '">keyboard</i>';
      htmlString+= '<input type="hidden" id="G1005Sanpuryo' + nouhiIndex + '" value="' + sanpuryo + '"/>';
      htmlString+= '<input type="hidden" id="G1005Unit' + nouhiIndex + '" value="' + unit + '" />';
      htmlString+= '</div>';
      htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G1005NouhiInfo-' + nouhiIndex + '"><i class="material-icons large">clear</i></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  /* 農肥散布情報入力マークアップ生成 */
  function MakeNouhiSanpuInput(nouhiIndex) {

      var htmlString	=	"";		/* 農肥情報表示マークアップ */

      htmlString+= '<div class="row nouhi-info" id="G1005NouhiInfo-' + nouhiIndex + '" nouhiIndex="' + nouhiIndex + '">';
      htmlString+= '<div class="col s12 info-line">';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="selectmodal-trigger-title item-title">農薬/肥料情報</span><a href="#selectmodal"  class="selectmodal-trigger item" title="農薬/肥料選択" data="' + workinfo.workid + '/36/getNouhiOfWorkChainJson" displayspan="#G1005Nouhi' + nouhiIndex + 'Span" htext="#G1005Nouhi' + nouhiIndex + 'Value"><span id="G1005Nouhi' + nouhiIndex + 'Span">未選択</span></a>';
      htmlString+= '<input type="hidden" id="G1005Nouhi' + nouhiIndex + 'Value" class="nouhi-value-change" idindex="' + nouhiIndex + '" value="0"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">倍率</span><span id="G1005Bairitu' + nouhiIndex + 'Span" class="item">' + 0 + '</span><span class="item">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005Bairitu' + nouhiIndex + '">keyboard</i>';
      htmlString+= '<input type="hidden" id="G1005Bairitu' + nouhiIndex + '" value="0.00"/>';
      htmlString+= '</div>';
      htmlString+= '<div class="col s12 input-field wd">';
      htmlString+= '<span class="item-title">散布量</span><span id="G1005Sanpuryo' + nouhiIndex + 'Span" class="item">' + 0 + '</span><span class="item" id="G1005Unit' + nouhiIndex + 'Span"></span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1005Sanpuryo' + nouhiIndex + '">keyboard</i>';
      htmlString+= '<input type="hidden" id="G1005Sanpuryo' + nouhiIndex + '" value="0.00"/>';
      htmlString+= '<input type="hidden" id="G1005Unit' + nouhiIndex + '" value="" />';
      htmlString+= '</div>';
      htmlString+= '<a class="waves-effect waves-light btn-floating btn blue darken-1 nouhi-commit-btn right" idindex="' + nouhiIndex + '"><i class="material-icons large">add</i></a>';
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
      htmlString+= '<div class="row" id="G1005SanpuInfoAdd">';
      htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel wd">';
      //散布方法追加ボタン
      htmlString+= '<div class="col s12" style="padding-top: 16px;padding-bottom: 16px;">';
      htmlString+= '<span class="item-title" style="margin-top: 8px;display:inline-block;">散布方法追加</span><a class="waves-effect waves-light btn-floating btn red right" id="G1005SanpuInfoAddBtn"><i class="material-icons large">add</i></a></blockquote>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      return htmlString;

  }

  function dataToJson() {

    /* 当画面の入力項目情報定義 */
    var checktarget = [
        { "id" : "G1005WorkDate"        , "name" : "日付"          , "json" : "workDate"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005WorkTime"        , "name" : "片付け時間"    , "json" : "workTime"      , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005StartDate"       , "name" : "作業開始日付"  , "json" : "startDate"     , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005StartTime"       , "name" : "作業開始時間"  , "json" : "startTime"     , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005EndDate"         , "name" : "作業終了日付"  , "json" : "endDate"       , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005EndTime"         , "name" : "作業終了時間"  , "json" : "endTime"       , "check" : { "required"  : "1"}}
       ,{ "id" : "G1005WorkAccountValue", "name" : "担当者"        , "json" : "workAccount"   , "check" : { "select"    : "1"}}
       ,{ "id" : "G1005WorkId"          , "name" : "作業"          , "json" : "workId"        , "check" : {            }}
       ,{ "id" : "G1005IkubyoDiaryId"   , "name" : "育苗記録ID"    , "json" : "ikubyoDiaryId" , "check" : {            }}
    ];

    //----- 作業項目の生成 START -----
    switch(workinfo.worktemplateid) {
    case 1: //通常作業

      checktarget.push({ "id" : "G1005WorkTime" , "name" : "作業時間"  , "json" : "workTime" , "check" : { "required"  : "1"}});
      break;

    case 2: //散布作業

      checktarget.push({ "id" : "G1005WorkNaeValue"     , "name" : "苗"                , "json" : "workNae"    , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G1005KikiValue"        , "name" : "機器情報"          , "json" : "kiki"       , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G1005AttachmentValue"  , "name" : "アタッチメント情報", "json" : "attachment" , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G1005WorkTime"         , "name" : "散布時間"          , "json" : "workTime"   , "check" : { "required"  : "1"}});
      break;

    case 6://潅水作業

      checktarget.push({ "id" : "G1005WorkNaeValue", "name" : "苗"      , "json" : "workNae"  , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G1005KansuiValue" , "name" : "潅水方法", "json" : "kansui"   , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G1005KikiValue"   , "name" : "機器情報", "json" : "kiki"     , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G1005KansuiRyo"   , "name" : "潅水量"  , "json" : "kansuiryo", "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G1005WorkTime"    , "name" : "潅水時間", "json" : "workTime" , "check" : { "required"  : "1"}});
      break;

    case 21: //苗播種作業

      checktarget.push({ "id" : "G1005HinsyuValue"      , "name" : "種情報"        , "json" : "hinsyu"      , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G1005YoukiValue"       , "name" : "容器情報"      , "json" : "youki"       , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G1005BaidoValue"       , "name" : "培土情報"      , "json" : "baido"       , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G1005FukudoValue"      , "name" : "覆土情報"      , "json" : "fukudo"      , "check" : { "select"    : "0"}});
      checktarget.push({ "id" : "G1005Suryo"            , "name" : "苗数量"        , "json" : "naeSuryo"    , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G1005Kosu"             , "name" : "個数"          , "json" : "kosu"        , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G1005BaidoSuryo"       , "name" : "培土数量"      , "json" : "baidoSuryo"  , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G1005FukudoSuryo"      , "name" : "覆土数量"      , "json" : "fukudoSuryo" , "check" : { "required"  : "1"}});
      break;

    case 22://刈り取り作業

      checktarget.push({ "id" : "G1005WorkNaeValue"    , "name" : "苗"    , "json" : "workNae"       , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G1005SenteiHeight"    , "name" : "刈取高", "json" : "senteiHeight"  , "check" : { "required"  : "1"}});
      break;

    case 23://廃棄作業

      checktarget.push({ "id" : "G1005WorkNaeValue"    , "name" : "苗"        , "json" : "workNae"    , "check" : { "select"    : "1"}});
      checktarget.push({ "id" : "G1005HaikiRyo"        , "name" : "廃棄量"    , "json" : "haikiRyo"   , "check" : { "required"  : "1"}});
      checktarget.push({ "id" : "G1005HaikiSuryo"      , "name" : "廃棄数量"  , "json" : "haikiSuryo" , "check" : {                  }});
      break;

    }
    //----- 作業項目の生成 END -----

    /* 各入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    //----- 作業開始終了日時チェック -----
    var ikubyoPlanType = oInputJson.ikubyoPlanType;
    if (ikubyoPlanType != 2) { //作業指示の保存以外の場合
      var wStart = new Date($("#G1005StartDate").val() + " " + $("#G1005StartTime").val());
      var wEnd   = new Date($("#G1005EndDate").val() + " " + $("#G1005EndTime").val());

      console.log(wStart);
      console.log(wEnd);

      if (wEnd < wStart) {
        alert("作業終了日時が作業開始日時以前に設定されています。");
        return false;
      }
    }

    oInputJson   = InputDataToJson(checktarget);
    if (workinfo.worktemplateid == 21) {
      oInputJson["workNae"] = workinfo.naeNo;
    }
    oInputJson["mode"] = 1;
    oInputJson["ikubyoPlanType"] = ikubyoPlanType;

    /*----- 肥料散布情報をJSONデータとして格納する -----*/
    var nouhiDataList = new Array();                  //農肥情報リスト

    var nouhiList = $(".nouhi-info");                 //散布情報を全て取得する

    $.each(nouhiList, function (index, nouhiData) {
        var nouhiJson = new Object();

        if ($("#G1005Nouhi" + $(nouhiData).attr('nouhiIndex') + "Value").val() == 0) { /* 農肥情報が未選択は対象外 */

        }
        else {
          //var sanpuData = $("#" + nouhiData.id).parent("div").parent("div").parent("div");
          var sanpuData = $("#" + nouhiData.id).parents(".sanpu-info");

          nouhiJson["kiki"]   = $("#G1005Kiki" + $(sanpuData).attr('sanpuIndex') + "Value").val();
          nouhiJson["attachment"]   = $("#G1005Attachment" + $(sanpuData).attr('sanpuIndex') + "Value").val();

          nouhiJson["sanpuId"]  = $("#G1005Sanpu" + $(sanpuData).attr('sanpuIndex') + "Value").val();
          nouhiJson["sanpuName"]  = $("#G1005Sanpu" + $(sanpuData).attr('sanpuIndex') + "Span").html();
          nouhiJson["nouhiId"]  = $("#G1005Nouhi" + $(nouhiData).attr('nouhiIndex') + "Value").val();
          nouhiJson["bairitu"]  = $("#G1005Bairitu" + $(nouhiData).attr('nouhiIndex')).val();
          nouhiJson["sanpuryo"]   = $("#G1005Sanpuryo" + $(nouhiData).attr('nouhiIndex')).val();

          nouhiDataList.push(nouhiJson);
        }
    });

    oInputJson["nouhiInfo"] = nouhiDataList;

    //----- 作業詳細情報をJSON化する -----
    var detailDataList = new Array();

    for (var index = 1; index <= 1; index++) {
      var dj = new Object();
      dj["suryo"]   = 0;
      detailDataList.push(dj);
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
  function SubmitIkubyoDiary(jsondata){

  	if (nowproc == true) {

          displayToast('現在、作業記録の保存中です', 4000, 'rounded');
          return;

  	}

  	nowproc = true;

  	var url = "/submitIkubyoDiary";

  	if ($("#G1005IkubyoDiaryId").val() != "") {
  		url = "/" + $("#G1005IkubyoDiaryId").val() + "/submitIkubyoDiaryEdit";
  	}

    /* 登録アニメーション実行 */
    $('.workdiary-commit').addClass("commit");

    $.ajax({
        url:url,												//育苗日誌保存処理
        type:'POST',
        data:JSON.stringify(jsondata),							//入力用JSONデータ
        complete:function(data, status, jqXHR){					//処理成功時

          nowproc = false;

          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

          if (jsonResult.result == 'SUCCESS') {
          	displayToast('育苗日誌を記録しました。', 4000, 'rounded');    			  //保存メッセージの表示

          	var wca = jsonResult.workCommitAfter;  //作業記録後の取得

          	if (wca == 1) {
                //連続作業切替時はバックモードを0にする
                localStorage.setItem("backMode"         , "0");
                localStorage.setItem("backKukakuId"     ,  0);
                localStorage.setItem("backWorkId"       , workinfo.workid);

                window.location.href = '/' + workinfo.workid + '/NONE/ikubyoDiaryMove';
          	}
          	else if (wca == 2) {
              if (jsonResult.ikubyoDiaryId != 0) {
                window.location.href = "/" + jsonResult.ikubyoDiaryId + "/ikubyoDiaryEdit";
              }
          	}
          	else {
              if (localStorage.getItem("backMode") == "1") {
                window.location.href = "/workingaccountmove";
              }
              else {
                if (localStorage.getItem("backMode") == "4") {
                  localStorage.setItem("backMode"         , null);
                  localStorage.setItem("backWorkId"       , null);
                  localStorage.setItem("backKukakuId"     , null);
                  localStorage.setItem("backFieldGroupId" , null);
                }
                window.location.href = "/ikubyoMove";
              }
          	}
          }
        },
        error:function(data, status, jqXHR){								//処理成功時

          nowproc = false;
          displayToast('育苗記録の保存に失敗しました', 4000, 'rounded');

        },
        dataType:'json',
        contentType:'text/json',
        async: false
    });
  } // end of SubmitIkubyoDiary

  /* 農肥チェック */
  function NouhiCheck(jsondata){

    var url = "/checkNouhiIkubyo";

    $.ajax({
      url:url,                        //育苗日誌保存処理
      type:'POST',
      data:JSON.stringify(jsondata),              //入力用JSONデータ
      complete:function(data, status, jqXHR){         //処理成功時

        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        console.log('RESULT  : %d', jsonResult.result);
        console.log('MESSAGE : %s', jsonResult.message);
        if (jsonResult.result != 0) {
          var result = window.confirm('下記の警告が発生しています。\n作業を登録しますか？\n' + jsonResult.message);
          if (result) {
            SubmitIkubyoDiary(jsondata);                      //作業日誌保存処理
          }
        }
        else {
          SubmitIkubyoDiary(jsondata);                      //作業日誌保存処理
        }
      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
  } // end of SubmitIkubyoDiary

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

          $("#G1005Attachment" + targetKikiIndex + "Span").removeClass("grey-text").removeClass("text-darken-2").text("未選択");
          $("#G1005Attachment" + targetKikiIndex).click(OpenAttachmentModal);

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

      $('div').remove('#G1005ModalAttachment');
      $("#G1005SelectModal").append(MakeSelectModal('G1005ModalAttachment', 'アタッチメント', attachmentList, 'attachmentId', 'attachementName', 'G1005Attachment', 'attachmentId'));

      $('.select-modal-tap').unbind();								//農肥選択時

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

      $('.modal').modal();

      $($(this).attr("href")).attr("targetId", $(this).attr("targetId"));
      $($(this).attr("href")).attr("targetField", $(this).attr("targetField"));
      $($(this).attr("href")).modal('open');

  } // end of G0001Login

  /* 農肥前回情報を取得する */
  function GetNouhiValue(){

    /* 当画面の入力項目情報定義 */
    var checktarget = [
        { "id" : $(this).attr("id")		, "name" : "農肥情報"		, "json" : "nouhiId"	, "check" : { }}
    ];

    var jsondata   		= InputDataToJson(checktarget);			//JSONDATAに変換する

    targetNouhiIndex 	= $(this).attr("idindex");				//対象農肥情報インデックスを保存

    $.ajax({
        url:"/getNouhiValueIkubyo",									//作業日誌保存処理
        type:'POST',
        data:JSON.stringify(jsondata),							//入力用JSONデータ
        complete:function(data, status, jqXHR){					//処理成功時
          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

          /*----- 倍率を設定 -----*/
          $("#G1005Bairitu" + targetNouhiIndex + "Span").html(jsonResult["bairitu"]);
          $("#G1005Bairitu" + targetNouhiIndex).val(jsonResult["bairitu"]);
          $("#G1005Bairitu" + targetNouhiIndex).attr("min" ,jsonResult["lower"]);
          $("#G1005Bairitu" + targetNouhiIndex).attr("max" ,jsonResult["upper"]);
          $("#G1005Bairitu" + targetNouhiIndex).attr("step" ,"0.01");

          /*----- 散布量を設定 -----*/
          $("#G1005Sanpuryo" + targetNouhiIndex + "Span").html(jsonResult["sanpuryo"]);
          $("#G1005Sanpuryo" + targetNouhiIndex).val(jsonResult["sanpuryo"]);
          $("#G1005Sanpuryo" + targetNouhiIndex).attr("min" ,"0.00");
          $("#G1005Sanpuryo" + targetNouhiIndex).attr("max" ,"10000.00");
          $("#G1005Sanpuryo" + targetNouhiIndex).attr("step" ,"0.01");
          $("#G1005Unit" + targetNouhiIndex + "Span").html(unitKind[jsonResult["unit"]]);
          $("#G1005Unit" + targetNouhiIndex).val(unitKind[jsonResult["unit"]]);

        },
        dataType:'json',
        contentType:'text/json',
        async: true
    });
  } // end of G0001Login

  /* 散布情報追加イベント発生時 */
  function AddSunpuInfoEvent() {

    var addInfoRow	=	$("#G1005SanpuInfoAdd");		//散布情報追加位置を確保
    var htmlString	= "";								//可変HTML文字列

    //散布方法の生成
    maxSanpuIndex	=	$(".sanpu-info").length;		//現在の散布方法数を取得する
    maxSanpuIndex++;
    htmlString+= '<div class="row sanpu-info" id="G1005SanpuInfo-' + maxSanpuIndex + '" sanpuIndex=' + maxSanpuIndex + '>';
    htmlString+= '<div class="col s12 m8 offset-m2 l4 offset-l4 card-panel wd-panel">';
    htmlString+= '<div class="col s12 input-field wd">';
    htmlString+= '<input type="hidden" id="G1005Sanpu' + maxSanpuIndex + 'Value" value="0" />';
    htmlString+= '<span class="selectmodal-trigger-title item-title">散布方法</span><a href="#selectmodal"  class="selectmodal-trigger item" title="散布方法選択" data="getSanpu" displayspan="#G1005Sanpu' + maxSanpuIndex + 'Span" htext="#G1005Sanpu' + maxSanpuIndex + 'Value"><span id="G1005Sanpu' + maxSanpuIndex + 'Span">未選択</span></a>';
    htmlString+= '<a class="waves-effect waves-light btn-floating btn red info-delete right" deleteid="G1005SanpuInfo-' + maxSanpuIndex + '"><i class="material-icons large">clear</i></a>';
    htmlString+= '</div>';
    //機器情報の生成
    htmlString+= '<div class="col s12 input-field wd">';
    htmlString+= '<span class="selectmodal-trigger-title item-title">使用機器</span><a href="#selectmodal"  class="selectmodal-trigger item" title="機器選択" data="' + workinfo.workid + '/36/getKikiOfWorkChainJson" displayspan="#G1005Kiki' + maxSanpuIndex + 'Span" htext="#G1005Kiki' + maxSanpuIndex + 'Value"><span id="G1005Kiki' + maxSanpuIndex + 'Span">未選択</span></a>';
    htmlString+= '<input type="hidden"  class="kiki-value-change" id="G1005Kiki' + maxSanpuIndex + 'Value" idindex="' + maxSanpuIndex + '" />';
    htmlString+= '</span>';
    htmlString+= '</div>';
    //----- アタッチメントは機器を選択するまで選択出来ない様にする -----
    htmlString+= '<div class="col s12 input-field wd">';
    htmlString+= '<span class="selectmodal-trigger-title item-title">アタッチメント</span><a href="#selectmodal" id="G1005Attachment' + maxSanpuIndex + 'Link" class="selectmodal-trigger item" title="アタッチメント選択" data="' + 0 + '/getAttachmentOfKikiJson" displayspan="#G1005Attachment' + maxSanpuIndex + 'Span" htext="#G1005Attachment' + maxSanpuIndex + 'Value"><span id="G1005Attachment' + maxSanpuIndex + 'Span" class="grey-text text-darken-2">選択不可</span></a>';
    htmlString+= '<input type="hidden" id="G1005Attachment' + maxSanpuIndex + 'Value" />';
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
    var addNouhi = $("#G1005SanpuInfo-" + maxSanpuIndex);
    addInfoRow.remove();
    htmlString = MakeSanpuInfoAdd();

    addNouhi.after(htmlString);

    CalcInit();														//数値入力電卓初期化

    $('#G1005SanpuInfoAddBtn').unbind();							//散布情報追加時
    $('.nouhi-value-change').unbind();								//農肥選択時
    $('.kiki-value-change').unbind();								//機器選択時
    $('.info-delete').unbind();										//情報削除時
    $('.nouhi-commit-btn').unbind();								//農肥確定イベント時

    $('#G1005SanpuInfoAddBtn').bind('click', AddSunpuInfoEvent);	//散布情報追加時
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
    var targetDeleteRow		=	$("#G1005NouhiInfo-" + nouhiInfoIdIndex);
    var targetInfoRow		=	targetDeleteRow.parent();
    var htmlString			= "";									//可変HTML文字列

    /* 当画面の入力項目情報定義 */
    var checktarget = [
        { "id" : "G1005Nouhi" + nouhiInfoIdIndex + "Value"	, "name" : "農肥情報"	, "json" : "nouhiId"	, "check" : {}}
       ,{ "id" : "G1005Bairitu" + nouhiInfoIdIndex			, "name" : "倍率"		, "json" : "bairitu"	, "check" : {}}
       ,{ "id" : "G1005Sanpuryo" + nouhiInfoIdIndex			, "name" : "散布量"		, "json" : "sanpuryo"	, "check" : {}}
       ,{ "id" : "G1005Unit" + nouhiInfoIdIndex				, "name" : "単位種別"	, "json" : "tani"		, "check" : {}}
    ];

    /* 入力項目のチェック */
    if (InputDataManager(checktarget) == false) {
      return false;
    }

    var bairitu = $("#G1005Bairitu" + nouhiInfoIdIndex).val();
    var sanpu 	= $("#G1005Sanpuryo" + nouhiInfoIdIndex).val();
    var unit 	= $("#G1005Unit" + nouhiInfoIdIndex).val();


    htmlString+= MakeNouhiSanpuDisplay(nouhiInfoIdIndex, $("#G1005Nouhi" + nouhiInfoIdIndex + "Value").val(), $("#G1005Nouhi" + nouhiInfoIdIndex + "Span").html(), bairitu, sanpu, unit);

    /*----- 新規農肥情報を追加する -----*/
    maxNouhiIndex++;
    htmlString+= MakeNouhiSanpuInput(maxNouhiIndex);
    targetDeleteRow.remove();
    targetInfoRow.append(htmlString);

    CalcInit();														//数値入力電卓初期化
    RangeInit();													//数値入力Range初期化
    SelectModalInit();												//選択用モーダルイベント初期化
    MultiSelectModalInit();											//複数選択用モーダルイベント初期化

    $('#G1005SanpuInfoAddBtn').unbind();							//散布情報追加時
    $('.nouhi-value-change').unbind();								//農肥選択時
    $('.kiki-value-change').unbind();								//機器選択時
    $('.info-delete').unbind();										//情報削除時
    $('.nouhi-commit-btn').unbind();								//農肥確定イベント時

    $('#G1005SanpuInfoAddBtn').bind('click', AddSunpuInfoEvent);	//散布情報追加時
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
  function ChangeG1005DetailSetting() {

      if ($("#G1005DetailSettingKind").is(':checked')) {	//詳細設定情報種別がコンビネーションの場合
        $("#G1005CombiInfo").show();						//コンビネーション選択を表示する
      }
      else {
        $("#G1005CombiInfo").hide();						//コンビネーション選択を非表示にする
      }

  }
  function GetYoukiInfo() {
    var code = $(this).val();
    if (code == null || code == "" || isNaN(code)) {
      code = "0";
    }
    var url  = "/" + code + "/getYoukiInfo"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){                 //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        if (jsonResult.result == "SUCCESS") {
          $("#G1005YoukiUnitSpan").text(youkiUnit[jsonResult["unitKind"]]);
          $("#G1005YoukiKosu").val(jsonResult.kosu);
        }
        else {
          $("#G1005YoukiKosu").val(0);
        }
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function CalKosu() {
    var youkiKosu = $("#G1005YoukiKosu").val();
    var suryo = $(this).val();

    if (youkiKosu == 0) {
      displayToast('容器マスタの個数が0個の為、個数の自動算出を行いませんでした。', 4000, 'rounded');
    }
    else {
      var kosu = (youkiKosu * suryo);
      $("#G1005KosuSpan").text(kosu);
      $("#G1005Kosu").val(kosu);
    }
  }
  function GetBaidoInfo() {
    var code = $(this).val();
    if (code == null || code == "" || isNaN(code)) {
      code = "0";
    }
    var url  = "/" + code + "/getSoilInfo"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){                 //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

        $("#G1005BaidoUnitSpan").text(soilUnit[jsonResult["unitKind"]]);
        $("#G1005BaidoUnit").val(soilUnit[jsonResult["unitKind"]]);

    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function GetFukudoInfo() {
    var code = $(this).val();
    if (code == null || code == "" || isNaN(code)) {
      code = "0";
    }
    var url  = "/" + code + "/getSoilInfo"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){                 //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

        $("#G1005FukudoUnitSpan").text(soilUnit[jsonResult["unitKind"]]);
        $("#G1005FukudoUnit").val(soilUnit[jsonResult["unitKind"]]);

    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function GetNaeZaikoInfo() {
    var code = $(this).val();
    if (code == null || code == "") {
      code = "0";
    }
    var url  = "/" + code + "/getNaeZaikoInfo"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){                 //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
        if (jsonResult.result == "SUCCESS") {
          $("#G1005HaikiRyoSpan").text(jsonResult.zaikoKosu);
          $("#G1005HaikiSuryoSpan").text("(" + jsonResult.zaikoSuryo + youkiUnit[jsonResult.unitKind] + ")");
          $("#G1005HaikiRyo").val(jsonResult.zaikoKosu);
          $("#G1005HaikiSuryo").val(jsonResult.zaikoSuryo);
          $("#G1005ZaikoKosu").val(jsonResult.zaikoKosu);
          $("#G1005HaikiUnit").val(youkiUnit[jsonResult.unitKind]);
          $("#G1005Kosu").val(jsonResult.kosu);
        }
        else {
          $("#G1005HaikiRyo").val(0);
          $("#G1005HaikiSuryo").val(0);
          $("#G1005ZaikoKosu").val(0);
          $("#G1005HaikiUnit").val(1);
          $("#G1005Kosu").val(0);
        }
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function CalHaikiSuryo() {
    var zaikoKosu = $("#G1005ZaikoKosu").val();
    var youkiKosu = $("#G1005Kosu").val();
    var unit = $("#G1005HaikiUnit").val();
    var kosu = $(this).val();

    zaikoKosu *= 1;
    kosu *= 1;
    if (kosu > zaikoKosu) {
      displayToast('在庫数以上の個数は入力できません。', 4000, 'rounded');
    }
    else {
      if (youkiKosu == 0) {
        displayToast('容器マスタの個数が0個の為、個数の自動算出を行いませんでした。', 4000, 'rounded');
      }
      var suryo = (kosu / youkiKosu);
      $("#G1005HaikiSuryoSpan").text("(" + suryo + unit + ")");
      $("#G1005HaikiSuryo").val(suryo);
    }
  }

})(jQuery); // end of jQuery name space