/* AGRYEEL メニュー画面 JQUERY */
(function($){
  'use strict';

  var floatingButton;				/* 該当フローティングボタン */
  var motochoButton;				/* 該当元帳照会ボタン */
  var kukakuJoukyouList;			/* 区画状況照会一覧 */
  var objSly;
  var grafhObject = new Object();	/* グラフオブジェクト */

  /* 初期処理時イベント */
  $(function(){

    /*----- 各ボタンにクリックイベントを押下する -----*/

    $(document).ready(function(){
      $('.collapsible').collapsible({
        accordion : false // A setting that changes the collapsible behavior to expandable instead of the default accordion style
      });

      $("#G0002HouseWorkingTab").bind("click", GetCompartment);
      $("#G0002WorkTab").bind("click", GetWork);
      $("#G0002TimeLineTab").bind("click", GetTimeLine);
      $('#G0002FarmValue').bind('change', ChangeFarm);


//      $('.initanime1').addClass("ondisplay");
//      $('.initanime2').addClass("ondisplay");

      //PushCommitService();

    });

    GetGroupFarmList();
    //GetCompartment();
    GetWork();
    //GetTimeLine();


  }); // end of document ready

  function ChangeFarm() {
      var inputJson = StringToJson('{"farmId":"' + $('#G0002FarmValue').val() + '"}');

      $.ajax({
          url:"/farmChange",										//メニュー初期処理
          type:'POST',
          data:JSON.stringify(inputJson),								//入力用JSONデータ
          complete:function(data, status, jqXHR){						//処理成功時

              GetCompartment();
              GetWork();
              GetTimeLine();

        },
        dataType:'json',
        contentType:'text/json'
      });
  }
  function GetCompartment() {
    var inputJson = StringToJson('{"accountId":"' + accountInfo.accountId + '", "farmId":"' + accountInfo.farmId + '"}');

    grafhObject = new Object();	/* グラフオブジェクトを初期化する */

      $.ajax({
        url:"/getCompartment",										//メニュー初期処理
        type:'POST',
        data:JSON.stringify(inputJson),								//入力用JSONデータ
        complete:function(data, status, jqXHR){						//処理成功時
            var jsonResult 	= JSON.parse( data.responseText );			//戻り値用JSONデータの生成
            var kukakuJoukyouList = jsonResult.targetCompartmentStatus;	//ハウス状況対象リスト
            var htmlString	= "";										//可変HTML文字列
            var htmlLiString= "";										//区画ナビゲーションHTML文字列
            var iCount		= 0;										//偶奇判定件数
            var sPattern		= "";										//パターンスタイル

            $(".house-working-statusgraph").remove();

            htmlString	= "";											//可変HTML文字列初期化

            //----- ここからハウス状況照会の編集 ----
            for ( var houseKey in kukakuJoukyouList ) {                    	//対象圃場情報分処理を行う

                var houseStatus	= kukakuJoukyouList[houseKey];	           	//ハウス状況情報の取得

                htmlString += '<div class="col s12 m12 l4">';
                /* パターン判定 */
                if ( (iCount % 2) == 0) {
                  sPattern		= "initanime1";
                }
                else {
                  sPattern		= "initanime2";
                }

                htmlString += '<div class="card-panel grey lighten-5 z-depth-1 house-working-tap ' + sPattern + '" style="border-left: 8px solid #' + houseStatus["kukakuGroupColor"] +';">';
                iCount++;													//件数カウントアップ
                htmlString += '<canvas id="G0002HouseStatusG' + houseKey + '" class="house-working-statusgraph" key=' + houseKey + '></canvas>';
                if (houseStatus["nowWorkMode"] == 0) { //現在作業モードが｢作付｣の場合
                  if (null != houseStatus["nowEndWork"] && "" != houseStatus["nowEndWork"]) {
                      //進捗状況を表示する
                      htmlString += '<span class="house-working-status" style="background-color:#' + houseStatus["workColor"] + '; color: #ffffff;">' + houseStatus["nowEndWork"].substring(0, 1) + '</span>';
                  }
                }
                htmlString += '<span class="house-working-title">' + houseStatus["kukakuName"] + '</span><br>';
                htmlString += '<span class="house-working-item">年内回転数</span><span class="house-working-item-tip">' + houseStatus["rotationSpeedOfYear"] + '作目</span><br>';
                htmlString += '<span class="house-working-item">品種</span><span class="house-working-item-tip">' + houseStatus["hinsyuName"] + '</span><br>';
                htmlString += '<span class="house-working-item">播種日</span><span class="house-working-item-tip">' + houseStatus["hashuDate"] + '</span><br>';
                htmlString += '<span class="house-working-item">生育日数</span><span class="house-working-item-tip">' + houseStatus["seiikuDayCount"] + '日</span><br><br><br><br>';
                //管理状況を表示する
                htmlString += '<span class="house-working-item">最終消毒日</span><span class="house-working-item-tip">' + houseStatus["finalDisinfectionDate"] + '</span><span class="house-working-item-tip sub">' + GetSystemDateDiff(houseStatus["finalDisinfectionDate"]) + '日経過</span><br>';
                htmlString += '<span class="house-working-item">最終潅水日</span><span class="house-working-item-tip">' + houseStatus["finalKansuiDate"] + '</span><br>';
                htmlString += '<span class="house-working-item">最終追肥日</span><span class="house-working-item-tip">' + houseStatus["finalTuihiDate"] + '</span><span class="house-working-item-tip sub">' + GetSystemDateDiff(houseStatus["finalTuihiDate"]) + '日経過</span><br>';
                //収穫情報を表示する
                htmlString += '<span class="house-working-item">収穫開始日</span><span class="house-working-item-tip">' + houseStatus["shukakuStartDate"] + '</span><span class="house-working-item-tip sub">' + houseStatus["totalShukakuCount"] + 'ｇ</span><br>';
                htmlString += '<span class="house-working-item">収穫終了日</span><span class="house-working-item-tip">' + houseStatus["shukakuEndDate"] + '</span><br>';
                //元帳照会フローティングボタンを表示する
                htmlString += '<div class="btn-floating btn-large waves-effect waves-light blue darken-1 house-working-floationg motocho-button" kukakuId=' + houseKey + '><i class="material-icons large">assignment</i></div>';

                //クリップグループによるフローティングボタン色判定
                var clipColor = '';					//クリップフローティングボタン背景色
                switch (houseStatus["clipResult"]) {
                  case 1:
                      /* 赤 */
                    clipColor = 'red';
                    break;
                  default:
                    /* 青 */
                    clipColor = 'blue';
                    break;
                }

                htmlString += '<a class="btn-floating btn-large waves-effect waves-light ' + clipColor + ' darken-1 house-working-floationg-detail" kukakuId=' + houseKey + '><i class="material-icons large">attach_file</i></a>';
                htmlString += '<a class="btn-floating btn-large waves-effect waves-light blue darken-1 house-working-floationg-tap"><i class="material-icons large">expand_more</i></a>';
                htmlString += '</div>';
                htmlString += '</div>';


            }

            $("#G0002HouseList").html(htmlString);									//可変HTML部分に反映する

            /* 元帳照会参照ボタンタップ時イベント */
            $(".motocho-button").click(function(){
                if ($(this).hasClass("active")) {
                  e.stopPropagation();
                }
                $(this).toggleClass("active").toggleClass("waves-effect").toggleClass("waves-light");
                $(this).parents('div').toggleClass("motocho");
                if ($(this).hasClass("active")) {
                    var jsonData	= new Object();											//入力用JSONデータ
                    var result 		= "";													//収集結果JSONDATA
                    result 			+= "{"
                    result 			+= '"kukakuId":"' + $(this).attr("kukakuId") + '"';		//入力項目をJSONDATAに出力
                    result 			+= "}"
                    jsonData 		= new Function("return " + result)();					//Json変換

                    motochoButton	= $(this);

                    $.ajax({
                        url:"/getMotocho",													//元帳照会情報取得
                        type:'POST',
                        data:JSON.stringify(jsonData),										//入力用JSONデータ
                        complete:function(data, status, jqXHR){								//処理成功時
                            var jsonResult 			= JSON.parse( data.responseText );		//戻り値用JSONデータの生成
                            var motochoBaseDataList = jsonResult.motochoBaseDataList;		//タイムラインリスト
                            var htmlString	= "";											//可変HTML文字列
                            var iIndex		= 0;

                            var motochoBase		= motochoBaseDataList[jsonResult.initKey];  //元帳基本情報の取得
                            var htmlString		= "";										//編集HTML文字列
                            var nouyakuDate		= "";										//農薬日

                            //----- 元帳照会ヘッダ -----
                            htmlString += '<div class="row">';
                            htmlString += '<div class="col s12 m12 l4">';
                            htmlString += '<div class="card">';
                            htmlString += '<div class="card-content motochoinfo">';
                            htmlString += '<span class="icon" style="background-color:#' + motochoBase["kukakuGroupColor"] + '; color: #ffffff;">' + motochoBase["kukakuGroupName"] + '</span>';
                            htmlString += '<span class="kukaku">' + motochoBase["kukakuName"] + '</span>';
                            htmlString += '<span class="crop">' + motochoBase["cropName"] + '</span>';
                            htmlString += '<span class="countofyear">年内回転数：' + motochoBase["rotationSpeedOfYear"] + '</span>';
                            htmlString += '</div>';
                            htmlString += '</div>';
                            htmlString += '</div>';
                            htmlString += '</div>';

                            htmlString += '<div class="fixed-action-btn" style="top: 48px; right: 24px;">';
                            htmlString += '<a id="MotochoBack" class="waves-effect waves-light btn-floating btn-large blue"><i class="material-icons large">arrow_back</i></a>'
                            htmlString += '</div>';

                            //----- モバイル用確認アイコン -----
                            htmlString += '<div class="row hide-on-large-only">';
                            htmlString += '<div class="col s3 warpmenu center">';
                            htmlString += '<a href="#commoninfo"><span class="icon teal darken-2 white-text" >基</span><a>';
                            htmlString += '</div>';
                            htmlString += '<div class="col s3 warpmenu center">';
                            htmlString += '<a href="#nouyakuinfo"><span class="icon indigo darken-2 white-text" >農</span><a>';
                            htmlString += '</div>';
                            htmlString += '<div class="col s3 warpmenu center">';
                            htmlString += '<a href="#hiryoinfo"><span class="icon amber darken-2 white-text" >肥</span><a>';
                            htmlString += '</div>';
                            htmlString += '<div class="col s3 warpmenu center">';
                            htmlString += '<a href="#timeinfo"><span class="icon purple darken-2 white-text" >タ</span><a>';
                            htmlString += '</div>';
                            htmlString += '</div>';

                            //----- 基本情報 -----
                            htmlString += '<div class="row">'
                            htmlString += '<div class="col s12 m12 l3" id="commoninfo">'
                            htmlString += '<div class="card">'
                            htmlString += '<div class="card-content teal darken-2"">'
                            htmlString += '<span class="white-text">基&nbsp;本&nbsp;情&nbsp;報</span>'
                            htmlString += '</div>'
                            htmlString += '<div class="card-action">'
                            htmlString += '<ul class="info">'
                            htmlString += '<li class="infolist"><span class="title">場所</span><span class="item">' + motochoBase["kukakuName"] + '</span></li>'
                            htmlString += '<li class="infolist"><span class="title">品種</span><span class="item">' + motochoBase["hinsyuName"] + '</span></li>'
                            htmlString += '<li class="infolist"><span class="title">播種日</span><span class="item">' + motochoBase["hashuDate"] + '</span></li>'
                            htmlString += '<li class="infolist"><span class="title">生育日数</span><span class="item">' + motochoBase["seiikuDayCount"] + '</span></li>'
                            htmlString += '<li class="infolist"><span class="title">収穫量</span><span class="item">' + motochoBase["shukakuRyo"] + 'g</span></li>'
                            htmlString += '<li class="infolist"><span class="title">収穫開始日</span><span class="item">' + motochoBase["shukakuStartDate"] + '</span></li>'
                            htmlString += '<li class="infolist"><span class="title">収穫終了日</span><span class="item">' + motochoBase["shukakuEndDate"] + '</span></li>'
                            htmlString += '</ul>'
                            htmlString += '</div>'
                            htmlString += '</div>'
                            htmlString += '</div>'

//                            //----- 農薬 -----
//                            htmlString += '<div class="col s12 m12 l3" id="nouyakuinfo">'
//                            htmlString += '<div class="card">'
//                            htmlString += '<div class="card-content indigo darken-2 white-text">'
//                            htmlString += '<span class="white-text">農&nbsp;&nbsp;&nbsp;&nbsp;薬</span>'
//                            htmlString += '</div>'
//                            htmlString += '<div class="card-action">'
//                            htmlString += '<div  style="border-left: 4px solid #303f9f;">'
//
//                            var motochoNouyaku		= motochoBase["nouyakuList"];  //元帳農薬情報の取得
//
//                            for (var motochoNouyakuKey in motochoNouyaku) {
//
//                              var motochoNouyakuData = motochoNouyaku[motochoNouyakuKey];
//
//                              if ((nouyakuDate != "") && (nouyakuDate != motochoNouyakuData["sanpuDate"])) {
//                                  htmlString += '</ul>'
//                              }
//
//                              if (nouyakuDate != motochoNouyakuData["sanpuDate"]) {
//                                  htmlString += '<ul class="info" style="border-left: 4px solid #00796b;">'
//                              }
//
//                              htmlString += '<li class="infolist nouhi">'
//                              htmlString += '<span class="nouhi"><span class="kind">' + motochoNouyakuData["nouyakuNo"] + '</span>' + motochoNouyakuData["nouhiName"] + '</span><br>'
//                              htmlString += '<span class="nouhi"><span class="kind">倍率</span>' + motochoNouyakuData["bairitu"] + '倍</span><br>'
//                              htmlString += '<span class="nouhi"><span class="kind">日付</span>' + motochoNouyakuData["sanpuDate"] + '<span class="daycount">' + motochoNouyakuData["bairitu"] + '日経過</span><span class="kind">量</span>' + motochoNouyakuData["sanpuryo"] + 'L</span><br>'
//                              htmlString += '<span class="kind">散布方法</span>' + motochoNouyakuData["sanpuMethod"] + '</span><br>'
//                              htmlString += '</li>'
//
//                              nouyakuDate = motochoNouyakuData["sanpuDate"];
//
//                            }
//
//                            htmlString += '</ul>'
//                            htmlString += '</div>'
//                            htmlString += '</div>'
//                            htmlString += '</div>'
//                            htmlString += '</div>'

                            motochoButton.html(htmlString);

                            $("#MotochoBack").click(function(e){
                              motochoButton.parents('div').toggleClass("motocho");
                              motochoButton.toggleClass("active").toggleClass("waves-effect").toggleClass("waves-light");
                              motochoButton.html('<i class="mdi-action-assignment"></i>');

                              e.stopPropagation();

                            });
                        },
                        error:function(data, status, jqXHR){								//処理成功時
                            motochoButton.parents('div').toggleClass("motocho");
                            motochoButton.toggleClass("active").toggleClass("waves-effect").toggleClass("waves-light");
                            motochoButton.html('<i class="mdi-action-assignment"></i>');

                            toast('表示可能な元帳が存在しません', 1000, 'rounded');

                            e.stopPropagation();

                        },
                        dataType:'json',
                        contentType:'text/json'
                      });

                    $(this).html('<div class="progress"><div class="indeterminate"></div></div>');

                }
              });

            /* ハウス状況照会クリップボタンタップ時イベント */
            $('.house-working-floationg-detail').click(function() {
                /* 関連区画IDの取得 */
                var jsonData			= new Object();										//入力用JSONデータ
                jsonData["kukakuId"]	= $(this).attr("kukakuId");							//区画IDを格納

                var result 		= "";													//収集結果JSONDATA

                result += "{"
                result += '"kukakuId":"' + $(this).attr("kukakuId") + '"';				//入力項目をJSONDATAに出力
                result += "}"

                jsonData = new Function("return " + result)();							//Json変換

                floatingButton = $(this);													//フローティングオブジェクトを格納

                $.ajax({
                    url:"/addClip", 											//メニュー初期処理
                    type:'POST',
                    data:JSON.stringify(jsonData),							//入力用JSONデータ
                    complete:function(data, status, jqXHR){					//処理成功時
                      var jsonResult 	= JSON.parse( data.responseText );		//戻り値用JSONデータの生成

                      /* フローティングボタン制御 */
                      switch (jsonResult["clipResult"]) {
                        case 1:
                          /* 青から濃紫に変化する */
                          floatingButton.removeClass('blue');
                          floatingButton.addClass('red');
                          break;
                        default:
                          /* 赤から青に変化する */
                          floatingButton.removeClass('red');
                          floatingButton.addClass('blue');
                          break;
                      }
                    },
                    dataType:'json',
                    contentType:'text/json',
                    async: true
                });
            });


            /* ハウス状況照会フローティングボタンタップ時イベント */
            $('.house-working-floationg-tap').click(function() {
              /* ハウス状況照会パネル制御 */
              $(this).parents('div').toggleClass('active');                               //クラスにactiveを追加してパネルの高さを変更する
              $(this).parents('div').toggleClass('grey');                                 //クラスからgreyを除去して背景色を変更する(CSS側で背景色を定義)
              $(this).parents('div').toggleClass('lighten-5');                            //クラスからlighten-5を除去して背景色の輝度を変更する(CSS側で背景色を定義)

              /* フローティングボタン制御 */
              $(this).toggleClass('blue');                                                //クラスからblueを除去して背景色を変更する(redと差し替える)
              $(this).toggleClass('red');                                                 //クラスにredを追加して背景色を変更する(blueと差し替える)
              $(this).children('i').toggleClass('mdi-navigation-expand-more');            //クラスから↓を除去してアイコンを変更する(↑と差し替える)
              $(this).children('i').toggleClass('mdi-navigation-expand-less');            //クラスに↑を追加してアイコンを変更する(↓と差し替える)
              if($(this).hasClass('red')){
                $(this).html('<i class="material-icons large">expand_less</i>');
              }
              else {
                $(this).html('<i class="material-icons large">expand_more</i>');
              }



            });

            var scrollStopEvent = new $.Event("scrollstop");	/* スクロールストップイベント */
            var delay = 100;									/* スクロールイベント遅延時間 */
            var timer;											/* タイマーオブジェクト */

            /*----- スクロールイベント -----*/
            function scrollStopEventTrigger(){
              if (timer) {
                clearTimeout(timer);
              }
              timer = setTimeout(function(){$(window).trigger(scrollStopEvent)}, delay);
            }

            $(window).on("scroll", scrollStopEventTrigger);
            $("body").on("touchmove", scrollStopEventTrigger);

            var setElm = $('.house-working-statusgraph'),
            delayHeight = 0;

            $(window).on('scrollstop',function(){
                setElm.each(function(){
                    var setThis = $(this),
                    elmTop = setThis.offset().top,
                    elmHeight = setThis.height(),
                    scrTop = $(window).scrollTop(),
                    winHeight = $(window).height();
                    if ((scrTop < elmTop) && ((scrTop + winHeight) > elmTop) || (scrTop < (elmTop + elmHeight)) && ((scrTop + winHeight) > (elmTop + elmHeight))){
                        displayGrafh(setThis);
                    }
                });
            });

            $(window).trigger(scrollStopEvent)

            /*----- グラフ表示イベント -----*/
            function displayGrafh(setThis){
              //----- ここからハウス状況グラフの編集 ----
              var keyString			= setThis.attr("key");							//キー文字列
              var houseStatus		= kukakuJoukyouList[keyString];			  		//ハウス状況情報の取得
              var grafhDataList		= new Array();									//グラフデータリスト
              var linegrafhData		= new Object();									//ライングラフデータ

              if (houseStatus["nowWorkMode"] == 0) { //現在作業モードが｢作付｣の場合

                  var workStatusList	= houseStatus["compartmentWorkStatus"]; 		//ハウス作業状況情報の取得

                  for ( var workStatus in workStatusList ) {               			//作業状況分処理を行う
                      if (houseStatus["nowWorkMode"] != workStatusList[workStatus]["workMode"]) {		//作業モードが違う場合は処理は生成しない
                          continue;
                      }

                      var grafhData		= new Object();												//グラフデータ
                      grafhData["label"]	= workStatusList[workStatus]["workName"];				//作業名称
                      grafhData["value"]	= 10;													//値は全て10%扱いとする
                      if (workStatusList[workStatus]["workEndFlag"] == 0) {							//作業完了済の場合
                          grafhData["color"]	= "#eceff1";										//色を灰色にする
                      }
                      else {
                          grafhData["color"]	= "#" + workStatusList[workStatus]["workColor"];	//色を進捗色とする
                      }

                      grafhDataList.push(grafhData);												//グラフデータリストに追加

                  }

                  if (grafhObject[keyString] == null) {
                      var myChart  = new Chart(document.getElementById("G0002HouseStatusG" + keyString).getContext("2d")).Doughnut(grafhDataList);
                      grafhObject[keyString] = myChart;
                  }

              }
              else if (houseStatus["nowWorkMode"] == 1) { 		//現在作業モードが｢管理｣の場合

                  linegrafhData["labels"] = ["消","潅","肥"];	//ラベルを作成する

                  //----- 昨年度データの作成 -----
                  var grafhData		= new Object();								//グラフデータ

                  grafhData["fillColor"]		= "rgba(220,220,220,0.5)";			//描画色
                  grafhData["strokeColor"]	= "rgba(220,220,220,1)";			//背景色
                  grafhData["data"]			= [houseStatus["oldDisinfectionCount"]
                  , houseStatus["oldKansuiCount"]
                  , houseStatus["oldTuihiCount"]];	//昨年度データ
                  grafhDataList.push(grafhData);									//グラフデータリストに追加

                  //----- 今年度データの作成 -----
                  grafhData		= new Object();									//グラフデータ

                  grafhData["fillColor"]		= "rgba(151,187,205,0.5)";			//描画色
                  grafhData["strokeColor"]	= "rgba(151,187,205,1)";			//背景色
                  grafhData["data"]			= [houseStatus["totalDisinfectionCount"]
                  , houseStatus["totalKansuiCount"]
                  , houseStatus["totalTuihiCount"]];	//本年度データ
                  grafhDataList.push(grafhData);									//グラフデータリストに追加

                  linegrafhData["datasets"] = grafhDataList;						//データセットを格納する

                  if (grafhObject[keyString] == null) {
                      var myChart  = new Chart(document.getElementById("G0002HouseStatusG" + keyString).getContext("2d")).Bar(linegrafhData);
                      grafhObject[keyString] = myChart;
                  }

              }
              else if (houseStatus["nowWorkMode"] == 2) { 		//現在作業モードが｢収穫｣の場合

                  linegrafhData["labels"] = ["収穫量"];			//ラベルを作成する

                  //----- 昨年度データの作成 -----
                  var grafhData		= new Object();									//グラフデータ

                  grafhData["fillColor"]		= "rgba(220,220,220,0.5)";			//描画色
                  grafhData["strokeColor"]	= "rgba(220,220,220,1)";				//背景色
                  grafhData["data"]			= [houseStatus["oldShukakuCount"]];		//昨年度データ
                  grafhDataList.push(grafhData);									//グラフデータリストに追加

                  //----- 今年度データの作成 -----
                  grafhData		= new Object();										//グラフデータ

                  grafhData["fillColor"]		= "rgba(151,187,205,0.5)";			//描画色
                  grafhData["strokeColor"]	= "rgba(151,187,205,1)";				//背景色
                  grafhData["data"]			= [houseStatus["totalShukakuCount"]];	//本年度データ
                  grafhDataList.push(grafhData);									//グラフデータリストに追加

                  linegrafhData["datasets"] = grafhDataList;						//データセットを格納する


                  if (grafhObject[keyString] == null) {
                      var myChart  = new Chart(document.getElementById("G0002HouseStatusG" + keyString).getContext("2d")).Bar(linegrafhData);
                      grafhObject[keyString] = myChart;
                  }
              }

              setThis.parents('div').addClass("ondisplay");

            }
        },
        dataType:'json',
        contentType:'text/json'
      });
  }

  function GetWork() {
      var inputJson = StringToJson('{"accountId":"' + accountInfo.accountId + '", "farmId":"' + accountInfo.farmId + '"}');

      $.ajax({
        url:"/getWork",												//メニュー初期処理
        type:'POST',
        data:JSON.stringify(inputJson),								//入力用JSONデータ
        complete:function(data, status, jqXHR){						//処理成功時
            var jsonResult 	= JSON.parse( data.responseText );			//戻り値用JSONデータの生成
            var workList 		= jsonResult.targetWork;				//作業対象リスト
            var htmlString	= "";										//可変HTML文字列

            //----- ここから作業タブの編集 ----
            for ( var wrokKey in workList ) {                       //作業対象件数分処理を行う

              var work 			= workList[wrokKey];                //作業情報の取得
              var workModeColor	= "";
              htmlString	= "";									//可変HTML文字列初期化

              //作業項目一覧を作成する
              htmlString += '<div class="col s12 m6 l3">';
              htmlString += '<a class="white-text">';
              htmlString += '<div class="work-img z-depth-2">';
              //htmlString += '<img src="/assets/image/work' + work["workId"] + '.jpg" class="responsive-img">';
              htmlString += '<img src="/assets/image/_work1.jpg" class="responsive-img">';
              if (work["workMode"] == 0) { 			//現在作業モードが｢作付｣の場合
                  workModeColor	= "box-red";
              }
              else if (work["workMode"] == 1) { 		//現在作業モードが｢管理｣の場合
                  workModeColor	= "box-blue";
              }
              else { 									//現在作業モードが｢収穫｣の場合
                  workModeColor	= "box-purple";
              }
              htmlString += '<div class="detailbox ' + workModeColor + '">';
              if (work["workKukakuCount"] == 0) {
                  htmlString += '<div class="white-text worktitle workmove" workId="' + work["workId"] + '" kukakuId="0">' + work["workName"] +'</div>';
              }
              else {
                  htmlString += '<div class="white-text worktitle workmove" workId="' + work["workId"] + '" kukakuId="0">' + work["workName"] +'<div class="worktitlebadge">' + work["workKukakuCount"] + '</div></div>';
              }

              var workKukakuList = work["workKukakuList"];				//作業対象区画情報を取得する
              for ( var workKukakuKey in workKukakuList ) {       		//作業対象区画件数分処理を行う
                  var workKukaku		= workKukakuList[workKukakuKey];    //作業情報の取得
                  htmlString += '<div class="house line workmove" workId="' + work["workId"] + '" kukakuId="' + workKukaku["kukakuId"] +'">' + workKukaku["kukakuName"] +'<i class="material-icons sendicon right">send</i></div>';
              }

              htmlString += '</div></div></a></div>';

              $("#G0002WorkList").append(htmlString);					//可変HTML部分に反映する

            } // workList

            //作業記録日誌画面に遷移する為のフォームを埋め込む
            htmlString = '<form id="G0002Form" action="./workDiaryMove" method="GET" ><input type="hidden" id="G0002WorkId" name="workId"><input type="hidden" id="G0002kukakuId" name="kukakuId"></form>';

            $("#G0002WorkList").append(htmlString);					//可変HTML部分に反映する


            /* 作業タップ時イベント */
            $('.workmove').click(function() {

              $('#G0002WorkId').val($(this).attr('workId'));
              $('#G0002kukakuId').val($(this).attr('kukakuId'));
              $('#G0002Form').submit();

            });

            $("#house-nav").hide();

        },
        dataType:'json',
        contentType:'text/json'
      });
  }

  function GetTimeLine() {

    $.ajax({
        url:"/getTimeLine",											//タイムライン取得処理
        type:'GET',
        complete:function(data, status, jqXHR){						//処理成功時
            var jsonResult 	= JSON.parse( data.responseText );			//戻り値用JSONデータの生成
            var timeLineList 	= jsonResult.targetTimeLine;			//タイムラインリスト
            var htmlString	= "";										//可変HTML文字列

            //----- ここからタイムラインの編集 ----
            htmlString += '<div class="col s12 m8 offset-m2 l4 offset-l4">';
            htmlString += '<ul class="collapsible popout" data-collapsible="accordion">';
            for ( var timeLineKey in timeLineList ) {             //タイムライン件数分処理を行う

              var timeLine		= timeLineList[timeLineKey];    //タイムライン情報の取得
              var timeLineColor	= "";

              //タイムラインを作成する
              htmlString += '<li style="border-left: 4px solid #' + timeLine["timeLineColor"] + ';">';
              htmlString += '<div class="collapsible-header timeline">';
              htmlString += '<i class="material-icons right">subject</i><span class="house-working-item-tip">'+ timeLine["updateTime"] + '</span>';
              htmlString += '&nbsp;&nbsp;&nbsp;&nbsp;<a href="#">'+ timeLine["kukakuName"] + '</a><br>';
              htmlString += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="house-working-item-tip">' + timeLine["workName"] + '</span>&nbsp;&nbsp;&nbsp;&nbsp;' + timeLine["accountName"] +'';
              htmlString += '</div>';
              htmlString += '<div class="collapsible-body">';
              htmlString += '<p>'+ timeLine["accountName"] + 'が' + timeLine["kukakuName"] + 'で' + timeLine["workName"] +'を行いました。';
              if (timeLine["message"] != "") {
                  htmlString += '<br><br>' + timeLine["message"];
              }
              htmlString += '</p>';
              htmlString += '</div>';
              htmlString += '</li>';

            } // timeLineList
            htmlString += '</ul>';
            htmlString += '</div>';

            $("#G0002TimeLine").html(htmlString);					//可変HTML部分に反映する

            $('.collapsible').collapsible({
                accordion : false // A setting that changes the collapsible behavior to expandable instead of the default accordion style
              });

            $("#house-nav").hide();

        },
        dataType:'json',
        contentType:'text/json'
      });
    }

  function GetGroupFarmList() {

    $.ajax({
        url:"/getGroupFarmList",									//グループ農場取得処理
        type:'GET',
        complete:function(data, status, jqXHR){						//処理成功時

            var jsonResult 		= JSON.parse( data.responseText );	//戻り値用JSONデータの生成
            var farmList 		= jsonResult["farmList"]; 			//グループ農場リスト
            var htmlString	= "";										//可変HTML文字列

            if (farmList.length == 0 ) {
              $("#G0002FarmChange").hide();
            }
            else {

                //作業場所モーダルリストの作成
                htmlString+= MakeSelectModal('G0002ModalFarm', '農場', farmList, 'farmId', 'farmName', 'G0002Farm', 'farmId');
                $("#G0002SelectModal").html(htmlString);							//可変HTML部分に反映する

                $('.modal-trigger').leanModal();									// 選択用モーダル画面初期化

                $("#G0002FarmChange").show();

                SelectModalInit();												//選択用モーダルイベント初期化
            }


        },
        dataType:'json',
        contentType:'text/json'
      });
    }

  var $frame = $('#forcecentered');
  var $wrap  = $frame.parent();

  // Call Sly on frame
  $frame.sly({
    horizontal: 1,
    itemNav: 'forceCentered',
    smart: 1,
    activateMiddle: 1,
    activateOn: 'click',
    mouseDragging: 1,
    touchDragging: 1,
    releaseSwing: 1,
    startAt: 0,
    scrollBar: $wrap.find('.scrollbar'),
    scrollBy: 1,
    speed: 300,
    elasticBounds: 1,
    easing: 'easeOutExpo',
    dragHandle: 1,
    dynamicHandle: 1,
    clickBar: 1,

    // Buttons
    prev: $wrap.find('.prev'),
    next: $wrap.find('.next')
  });

})(jQuery); // end of jQuery name space