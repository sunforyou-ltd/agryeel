<<<<<<< HEAD
﻿/* AGRYEEL マスタメンテナンスメイン画面 JQUERY */
(function($){
  var makeBtnClick = false;	//マスタ作成ボタンクリックフラグ

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

      /*----- 各ボタンにクリックイベントを押下する -----*/
      $('#G1102AddButton').bind('click', AddMasterTap);					//マスタ追加ボタン

    });

    GetMasterList();													//マスタ一覧取得

  }); // end of document ready

  /* マスタ一覧取得 */
  function GetMasterList() {

	  $("#G1101MasterMntList").empty();
      $.ajax({
          url:"/masterMntInit", 										//マスタメンテナンス初期処理
            type:'GET',
            complete:function(data, status, jqXHR){						//処理成功時
            var jsonResult = JSON.parse( data.responseText );			//戻り値用JSONデータの生成
            var htmlString	= "";										//可変HTML文字列
            var mstIdx = 0;
            var gmnId = jsonResult["gmnId"];							//画面ID

            htmlString+= '<div id="G1101MasterMnt">';
            htmlString+= '<ul class="collection with-header" style="border-left: 0px; border-right: 0px; border-top: 0px; border-bottom: 1px solid #222222;" id="G1103MstList" gmnId=' + jsonResult["gmnId"] + ' farmId=' + jsonResult["farmId"] + '>';
            htmlString+= '<li class="collection-header fixed-color"  style="color: #eeeeee; border-bottom: 1px solid #222222;"><h6>' + jsonResult["gmnName"] + '</h6></li>';
            var targetList = jsonResult["masterDataList"]; 				//jSONデータよりマスタ一覧を取得

            for ( var targetKey in targetList ) {						//対象マスタ件数分処理を行う

              var targetData = targetList[targetKey];

              mstIdx++;
              htmlString+= '<li class="collection-item mst-area"  style=""><div>' + targetData.masterName + '<a href="#!" class="secondary-content"><i class="material-icons mstedit" mstid="' + targetData.masterId + '">edit</i></a></div></li>';
            }
            htmlString+= '</ul>';
  	        htmlString+= '</div>';

            /* コマンドボタン */
            htmlString+= '<div class="row">';
  	        htmlString+= '<div class="col s12" style="text-align: center;">';
  	        htmlString+= '<div id="G1102Back" class="waves-effect waves-light btn-flat black-text"><i class="material-icons small left black-text">arrow_back</i>戻　る</div>';
  	        htmlString+= '</div>';
  	        htmlString+= '</div>';
  	        htmlString+= '<div class="row"></div>';

            $("#G1101MasterMntList").html(htmlString);					//可変HTML部分に反映する

            if(gmnId != 0 && 
               gmnId != 6 && 
               gmnId != 16){								//アカウント以外の場合
	            htmlString= '<div class="fixed-action-btn" style="bottom: 24px; right: 24px;">';
	            htmlString+= '<a class="btn-floating btn-large red" id="G1102AddButton">';
	            htmlString+= '<i class="material-icons large">add</i>';
	            htmlString+= '</a>';
	            htmlString+= '</div>';

	            $("#G1101ActionBtn").html(htmlString);					//可変HTML部分に反映する
            }

            $(".mstedit").bind('click', MasterMntEdit );      			//マスタ一覧選択時
            $("#G1102Back").bind('click', onBack );     				//戻るボタン

            },
            dataType:'json',
            contentType:'text/json',
            async: false
          });

  }

  /* マスタ追加釦選択 */
  function AddMasterTap() {

		var gmnID	= $("#G1103MstList").attr("gmnId");					//画面IDを格納
		var farmID	= $("#G1103MstList").attr("farmId");				//生産者IDを格納
		var mstID	= 0;												//マスターID=0(新規登録)

		// 各マスタ画面遷移
		MoveMst(gmnID, farmID, mstID);

  }

  /* マスタ編集釦選択 */
  function MasterMntEdit() {

		var gmnID	= $("#G1103MstList").attr("gmnId");					//画面IDを格納
		var farmID	= $("#G1103MstList").attr("farmId");				//生産者IDを格納
		var mstID	= $(this).attr("mstid");							//マスターIDを格納

		// 各マスタ画面遷移
		MoveMst(gmnID, farmID, mstID);

  }

  /* 各マスタ画面遷移 */
  function MoveMst(gmnID,farmID,mstID) {

		var url = "";

		// 遷移先の設定
        switch(Number(gmnID)) {
        case 0:	//アカウントの場合
          url = "/" + farmID + "/" + mstID + "/accountSettingMove";
          break;

        case 1:	//地主の場合
          url = "/" + farmID + "/" + mstID + "/landlordSettingMove";
          break;

        case 2:	//圃場グループの場合
          url = "/" + farmID + "/" + mstID + "/fieldGroupSettingMove";
          break;

        case 3:	//圃場の場合
            url = "/" + farmID + "/" + mstID + "/fieldSettingMove";
            break;

        case 4:	//区画の場合
            url = "/" + farmID + "/" + mstID + "/compartmentSettingMove";
            break;

        case 5:	//生産物グループの場合
            url = "/" + farmID + "/" + mstID + "/cropGroupSettingMove";
            break;

        case 6:	//生産物の場合
            url = "/" + farmID + "/" + mstID + "/cropSettingMove";
            break;

        case 7:	//農肥の場合
            url = "/" + farmID + "/" + mstID + "/nouhiSettingMove";
            break;

        case 8:	//種の場合
            url = "/" + farmID + "/" + mstID + "/hinsyuSettingMove";
            break;

        case 9:	//ベルトの場合
            url = "/" + farmID + "/" + mstID + "/beltoSettingMove";
            break;

        case 10:	//機器の場合
            url = "/" + farmID + "/" + mstID + "/kikiSettingMove";
            break;

        case 11:	//アタッチメントの場合
            url = "/" + farmID + "/" + mstID + "/attachmentSettingMove";
            break;

        case 12:	//荷姿の場合
            url = "/" + farmID + "/" + mstID + "/nisugataSettingMove";
            break;

        case 13:	//質の場合
            url = "/" + farmID + "/" + mstID + "/shituSettingMove";
            break;

        case 14:	//サイズの場合
            url = "/" + farmID + "/" + mstID + "/sizeSettingMove";
            break;

        case 15:	//資材の場合
            url = "/" + farmID + "/" + mstID + "/sizaiSettingMove";
            break;

        case 16:	//生産者の場合
            url = "/" + farmID + "/farmSettingMove";
            break;

        case 17:	//容器の場合
            url = "/" + farmID + "/" + mstID + "/youkiSettingMove";
            break;

        case 18:	//土の場合
            url = "/" + farmID + "/" + mstID + "/soilSettingMove";
            break;
        }

	    window.location.href = url;	                    				//各マスタ編集に遷移する

  }

  /* 戻るボタン押下処理 */
  function onBack() {
	var farmID	= $("#G1602Title").attr("farmId");				//生産者IDを格納

	window.location.href = "/masterSettingMove";

  }

=======
﻿/* AGRYEEL マスタメンテナンスメイン画面 JQUERY */
(function($){
  var makeBtnClick = false;	//マスタ作成ボタンクリックフラグ

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

      /*----- 各ボタンにクリックイベントを押下する -----*/
      $('#G1102AddButton').bind('click', AddMasterTap);					//マスタ追加ボタン

    });

    GetMasterList();													//マスタ一覧取得

  }); // end of document ready

  /* マスタ一覧取得 */
  function GetMasterList() {

	  $("#G1101MasterMntList").empty();
      $.ajax({
          url:"/masterMntInit", 										//マスタメンテナンス初期処理
            type:'GET',
            complete:function(data, status, jqXHR){						//処理成功時
            var jsonResult = JSON.parse( data.responseText );			//戻り値用JSONデータの生成
            var htmlString	= "";										//可変HTML文字列
            var mstIdx = 0;
            var gmnId = jsonResult["gmnId"];							//画面ID

            htmlString+= '<div id="G1101MasterMnt">';
            htmlString+= '<ul class="collection with-header" style="border-left: 0px; border-right: 0px; border-top: 0px; border-bottom: 1px solid #222222;" id="G1103MstList" gmnId=' + jsonResult["gmnId"] + ' farmId=' + jsonResult["farmId"] + '>';
            htmlString+= '<li class="collection-header fixed-color"  style="color: #eeeeee; border-bottom: 1px solid #222222;"><h6>' + jsonResult["gmnName"] + '</h6></li>';
            var targetList = jsonResult["masterDataList"]; 				//jSONデータよりマスタ一覧を取得

            for ( var targetKey in targetList ) {						//対象マスタ件数分処理を行う

              var targetData = targetList[targetKey];

              mstIdx++;
              htmlString+= '<li class="collection-item mst-area"  style=""><div>' + targetData.masterName + '<a href="#!" class="secondary-content"><i class="material-icons mstedit" mstid="' + targetData.masterId + '">edit</i></a></div></li>';
            }
            htmlString+= '</ul>';
  	        htmlString+= '</div>';

            /* コマンドボタン */
            htmlString+= '<div class="row">';
  	        htmlString+= '<div class="col s12" style="text-align: center;">';
  	        htmlString+= '<div id="G1102Back" class="waves-effect waves-light btn-flat black-text"><i class="material-icons small left black-text">arrow_back</i>戻　る</div>';
  	        htmlString+= '</div>';
  	        htmlString+= '</div>';
  	        htmlString+= '<div class="row"></div>';

            $("#G1101MasterMntList").html(htmlString);					//可変HTML部分に反映する

            if(gmnId != 0 && 
               gmnId != 6 && 
               gmnId != 16){								//アカウント以外の場合
	            htmlString= '<div class="fixed-action-btn" style="bottom: 24px; right: 24px;">';
	            htmlString+= '<a class="btn-floating btn-large red" id="G1102AddButton">';
	            htmlString+= '<i class="material-icons large">add</i>';
	            htmlString+= '</a>';
	            htmlString+= '</div>';

	            $("#G1101ActionBtn").html(htmlString);					//可変HTML部分に反映する
            }

            $(".mstedit").bind('click', MasterMntEdit );      			//マスタ一覧選択時
            $("#G1102Back").bind('click', onBack );     				//戻るボタン

            },
            dataType:'json',
            contentType:'text/json',
            async: false
          });

  }

  /* マスタ追加釦選択 */
  function AddMasterTap() {

		var gmnID	= $("#G1103MstList").attr("gmnId");					//画面IDを格納
		var farmID	= $("#G1103MstList").attr("farmId");				//生産者IDを格納
		var mstID	= 0;												//マスターID=0(新規登録)

		// 各マスタ画面遷移
		MoveMst(gmnID, farmID, mstID);

  }

  /* マスタ編集釦選択 */
  function MasterMntEdit() {

		var gmnID	= $("#G1103MstList").attr("gmnId");					//画面IDを格納
		var farmID	= $("#G1103MstList").attr("farmId");				//生産者IDを格納
		var mstID	= $(this).attr("mstid");							//マスターIDを格納

		// 各マスタ画面遷移
		MoveMst(gmnID, farmID, mstID);

  }

  /* 各マスタ画面遷移 */
  function MoveMst(gmnID,farmID,mstID) {

		var url = "";

		// 遷移先の設定
        switch(Number(gmnID)) {
        case 0:	//アカウントの場合
          url = "/" + farmID + "/" + mstID + "/accountSettingMove";
          break;

        case 1:	//地主の場合
          url = "/" + farmID + "/" + mstID + "/landlordSettingMove";
          break;

        case 2:	//圃場グループの場合
          url = "/" + farmID + "/" + mstID + "/fieldGroupSettingMove";
          break;

        case 3:	//圃場の場合
            url = "/" + farmID + "/" + mstID + "/fieldSettingMove";
            break;

        case 4:	//区画の場合
            url = "/" + farmID + "/" + mstID + "/compartmentSettingMove";
            break;

        case 5:	//生産物グループの場合
            url = "/" + farmID + "/" + mstID + "/cropGroupSettingMove";
            break;

        case 6:	//生産物の場合
            url = "/" + farmID + "/" + mstID + "/cropSettingMove";
            break;

        case 7:	//農肥の場合
            url = "/" + farmID + "/" + mstID + "/nouhiSettingMove";
            break;

        case 8:	//種の場合
            url = "/" + farmID + "/" + mstID + "/hinsyuSettingMove";
            break;

        case 9:	//ベルトの場合
            url = "/" + farmID + "/" + mstID + "/beltoSettingMove";
            break;

        case 10:	//機器の場合
            url = "/" + farmID + "/" + mstID + "/kikiSettingMove";
            break;

        case 11:	//アタッチメントの場合
            url = "/" + farmID + "/" + mstID + "/attachmentSettingMove";
            break;

        case 12:	//荷姿の場合
            url = "/" + farmID + "/" + mstID + "/nisugataSettingMove";
            break;

        case 13:	//質の場合
            url = "/" + farmID + "/" + mstID + "/shituSettingMove";
            break;

        case 14:	//サイズの場合
            url = "/" + farmID + "/" + mstID + "/sizeSettingMove";
            break;

        case 15:	//資材の場合
            url = "/" + farmID + "/" + mstID + "/sizaiSettingMove";
            break;

        case 16:	//生産者の場合
            url = "/" + farmID + "/farmSettingMove";
            break;

        case 17:	//容器の場合
            url = "/" + farmID + "/" + mstID + "/youkiSettingMove";
            break;

        case 18:	//土の場合
            url = "/" + farmID + "/" + mstID + "/soilSettingMove";
            break;
        }

	    window.location.href = url;	                    				//各マスタ編集に遷移する

  }

  /* 戻るボタン押下処理 */
  function onBack() {
	var farmID	= $("#G1602Title").attr("farmId");				//生産者IDを格納

	window.location.href = "/masterSettingMove";

  }

>>>>>>> e747d9c9b47c3d59e92c6b368bbb246cc6564120
})(jQuery); // end of jQuery name space