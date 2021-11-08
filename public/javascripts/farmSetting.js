/* AGRYEEL 生産者情報設定画面 JQUERY */
(function($){

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

	  //$('.modal').modal();
	  //$('.mselectmodal-trigger').unbind('click');
	  //$('.mselectmodal-trigger').bind('click', mSelectOpen);

    //------------------------------------------------------------------------------------------------------------------
    //- セレクトモーダルの初期化
    //------------------------------------------------------------------------------------------------------------------
    $('.selectmodal-trigger').unbind('click');
    $('.selectmodal-trigger').bind('click', selectOpen);


    });

    $.ajax({
        url:"/farmSettingInit", 										//生産者情報設定初期処理
        type:'GET',
        complete:function(data, status, jqXHR){							//処理成功時
        var jsonResult = JSON.parse( data.responseText );				//戻り値用JSONデータの生成
        var htmlString	= "";											//可変HTML文字列
        var farmId = jsonResult["farmId"];								//生産者ID取得
        var contractPlan = jsonResult["contractPlan"];					//契約プラン

        htmlString = '<div class="card mst-panel" style="">';
        htmlString+= '<div class="card-panel fixed-color" style="">';
        /* タイトル */
        htmlString+= '<span class="white-text" id="G2702Title" farmId="' + jsonResult["farmId"] + '" contractPlan="' + jsonResult["contractPlan"] + '">生産者情報設定</span>';
  	    htmlString+= '</div>';

        htmlString+= '<div class="row" id="G2702FarmInfo">';
        htmlString+= '<div class="card-action-center">';
        htmlString+= '<form>';

        /* 生産者名 */
	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1 mst-item-title">';
        htmlString+= '生産者名：<span class="mst-item" style="">' + jsonResult["farmName"] + '</span>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 代表者 */
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G2702InRepresentativeName" type="text" class="validate input-text-color" maxlength="32" style="ime-mode: active;" value="' + jsonResult["representativeName"] + '">';
        htmlString+= '<label for="G2702InAcountName">代表者を入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 郵便番号 */
	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G2702InPost" type="text" class="validate input-text-color" maxlength="7" style="ime-mode: disabled;" value="' + jsonResult["postNo"] + '">';
        htmlString+= '<label for="G2702InPost">郵便番号を入力してください（ハイフン入力なし）</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';
      
        /* 都道府県 */
	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G2702InPrefectures" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["prefectures"] + '">';
        htmlString+= '<label for="G2702InPrefectures">都道府県を入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';
      
        /* 市町村など */
	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G2702InAddress" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["address"] + '">';
        htmlString+= '<label for="G2702InAddress">市町村などを入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 電話番号 */
	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G2702InTel" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="' + jsonResult["tel"] + '">';
        htmlString+= '<label for="G2702InTel">電話番号を入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 責任者携帯番号 */
	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G2702InMobileTel" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="' + jsonResult["responsibleMobileTel"] + '">';
        htmlString+= '<label for="G2702InMobileTel">責任者携帯番号を入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* FAX */
	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
        htmlString+= '<input id="G2702InFax" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="' + jsonResult["fax"] + '">';
        htmlString+= '<label for="G2702InFax">FAXを入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        //メールアドレス（パソコン）
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
   	    htmlString+= '<input id="G2702MailAddressPc" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["mailAddressPC"] + '">';
        htmlString+= '<label for="G2702MailAddressPc">メールアドレス（パソコン）を入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        //メールアドレス（携帯）
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
   	    htmlString+= '<input id="G2702MailAddressMobile" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["mailAddressMobile"] + '">';
        htmlString+= '<label for="G2702MailAddressMobile">メールアドレス（携帯）を入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        //ホームページＵＲＬ
  	    htmlString+= '<div class="row">';
        htmlString+= '<div class="input-field col s10 offset-s1">';
   	    htmlString+= '<input id="G2702Url" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["url"] + '">';
        htmlString+= '<label for="G2702Url">ホームページＵＲＬを入力してください</label>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 面積単位 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '面積単位<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="面積単位選択"" data="getAreaUnit" displayspan="#G2702AreaUnitSpan"><span id="G2702AreaUnitSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 期初 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '期初<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="期初選択"" data="getKisyo" displayspan="#G2702KisyoSpan"><span id="G2702KisyoSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 農肥チェック */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '農肥チェック方法<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="農肥チェック方法選択"" data="getNouhiCheck" displayspan="#G2702NouhiCheckSpan"><span id="G2702NouhiCheckSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 作業指示自動移動 */
        if (contractPlan == 0 || contractPlan >= 4) {
          htmlString+= '<div class="row">';
          htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
          htmlString+= '作業指示自動移動<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業指示自動移動選択"" data="getWorkPlanAutoMove" displayspan="#G2702WorkPlanAutoMoveSpan"><span id="G2702WorkPlanAutoMoveSpan" class="blockquote-input">未選択</span></a>';
          htmlString+= '</div>';
          htmlString+= '</div>';
        }

        /* 履歴参照 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '履歴参照<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="履歴参照選択"" data="getHistoryReference" displayspan="#G2702HistoryReferenceSpan"><span id="G2702HistoryReferenceSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 収穫入力数 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '収穫入力数<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="収穫入力数選択"" data="getSyukauInputCount" displayspan="#G2702SyukakuInputCountSpan"><span id="G2702SyukakuInputCountSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        /* 育苗機能 */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
        htmlString+= '育苗機能<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="育苗機能選択"" data="getIkubyoFunction" displayspan="#G2702IkubyoFunctionSpan"><span id="G2702IkubyoFunctionSpan" class="blockquote-input">未選択</span></a>';
        htmlString+= '</div>';
        htmlString+= '</div>';

        htmlString+= '</form>';
        htmlString+= '</div>';
        htmlString+= '</div>';
        /* コマンドボタン */
        htmlString+= '<div class="row">';
        htmlString+= '<div class="col s12 m6" style="text-align: center;">';
        htmlString+= '<div id="G1002Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
        htmlString+= '</div>';
        htmlString+= '<div class="col s12 m6" style="text-align: center;">';
        htmlString+= '<div id="G1002Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
        htmlString+= '</div>';
        htmlString+= '</div>';
        htmlString+= '<div class="row"></div>';

        htmlString+= '</div>';
        $("#G2702FarmSetting").html(htmlString);									//可変HTML部分に反映する

        $("input").prop("autocomplete", "off");										//オートコンプリート無効化
        $('.modal').modal();                        								//選択用モーダル画面初期化
  	    CalcInit();														        	//数値入力電卓初期化
  	    SelectModalInit();												  			//選択用モーダルイベント初期化

        $("#G1002Submit").bind('click', onSubmit );     							//確定ボタン
        $("#G1002Back").bind('click', onBack );     								//戻るボタン

        //------------------------------------------------------------------------------------------------------------------
        //- セレクトモーダルの初期選択反映
        //------------------------------------------------------------------------------------------------------------------
        //----- 面積単位 -----
        selectDataGet("#G2702AreaUnitSpan", "getAreaUnit");
        var oJson = selectData(jsonResult.areaUnit);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 期初 -----
        selectDataGet("#G2702KisyoSpan", "getKisyo");
        var oJson = selectData(jsonResult.kisyo);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 農肥チェック -----
        selectDataGet("#G2702NouhiCheckSpan", "getNouhiCheck");
        var oJson = selectData(jsonResult.nouhiCheck);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 作業指示自動移動 -----
        if (contractPlan == 0 || contractPlan >= 4) {
          selectDataGet("#G2702WorkPlanAutoMoveSpan", "getWorkPlanAutoMove");
          var oJson = selectData(jsonResult.workPlanAutoMove);
          if (oJson != undefined) {
            oJson.select = true;
          }
          selectClose();
        }

        //----- 履歴参照 -----
        selectDataGet("#G2702HistoryReferenceSpan", "getHistoryReference");
        var oJson = selectData(jsonResult.historyReference);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 収穫入力数 -----
        selectDataGet("#G2702SyukakuInputCountSpan", "getSyukauInputCount");
        var oJson = selectData(jsonResult.syukakuInputCount);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        //----- 育苗機能 -----
        selectDataGet("#G2702IkubyoFunctionSpan", "getIkubyoFunction");
        var oJson = selectData(jsonResult.ikubyoFunction);
        if (oJson != undefined) {
          oJson.select = true;
        }
        selectClose();

        },
          dataType:'json',
          contentType:'text/json',
          async: false
        });

  }); // end of document ready

  /* 確定ボタン押下処理 */
  function onSubmit(){

		var farmId = $("#G2702Title").attr("farmId");
		var contractPlan = $("#G2702Title").attr("contractPlan");

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G2702InRepresentativeName"	, "name" : "代表者"						, "length" : "32"	, "json" : "representativeName"		, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2702InPost"					, "name" : "郵便番号"					, "length" : "7"	, "json" : "postNo"					, "check" : { "number" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2702InPrefectures"			, "name" : "都道府県"					, "length" : "32"	, "json" : "prefectures"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2702InAddress"				, "name" : "市町村など"					, "length" : "32"	, "json" : "address"				, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2702InTel"					, "name" : "電話番号"					, "length" : "15"	, "json" : "tel"					, "check" : { "telfax" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2702InMobileTel"				, "name" : "責任者携帯番号"				, "length" : "15"	, "json" : "responsibleMobileTel"	, "check" : { "telfax" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2702InFax"					, "name" : "FAX"						, "length" : "15"	, "json" : "fax"					, "check" : { "telfax" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2702MailAddressPc"			, "name" : "メールアドレス（パソコン）"	, "length" : "128"	, "json" : "mailAddressPC"			, "check" : { "malladdress" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2702MailAddressMobile"		, "name" : "メールアドレス（携帯）"		, "length" : "128"	, "json" : "mailAddressMobile"		, "check" : { "malladdress" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2702Url"						, "name" : "ホームページＵＲＬ"			, "length" : "128"	, "json" : "url"					, "check" : { "maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;

	    var areaUnit         = selectConvertJson("#G2702AreaUnitSpan");
        var kisyo            = selectConvertJson("#G2702KisyoSpan");
        var nouhiCheck       = selectConvertJson("#G2702NouhiCheckSpan");
        var workPlanAutoMove = 0;
        if (contractPlan == 0 || contractPlan >= 4) {
          workPlanAutoMove = selectConvertJson("#G2702WorkPlanAutoMoveSpan");
        } else {
          workPlanAutoMove = 0;
        }
        var historyReference = selectConvertJson("#G2702HistoryReferenceSpan");
        var syukakuInputCount = selectConvertJson("#G2702SyukakuInputCountSpan");
        var ikubyoFunction = selectConvertJson("#G2702IkubyoFunctionSpan");

        jsondata["areaUnit"]         = areaUnit;
        jsondata["kisyo"]            = kisyo;
        jsondata["nouhiCheck"]       = nouhiCheck;
        jsondata["workPlanAutoMove"] = workPlanAutoMove;
        jsondata["historyReference"] = historyReference;
        jsondata["syukakuInputCount"] = syukakuInputCount;
        jsondata["ikubyoFunction"] = ikubyoFunction;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitFarm",
			type:'POST',
	        data:JSON.stringify(jsondata),								//入力用JSONデータ
			complete:function(data, status, jqXHR){						//処理成功時

			onProcing(false);
		    onBack()

			},
		    dataType:'json',
		    contentType:'text/json',
		    async: true
		});

  } // end of SubmitTap

  /* 戻るボタン押下時イベント */
  function onBack(){

		var farmID	= $("#G2702Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/16/masterMntMove";

  } // end of DeleteTap

})(jQuery); // end of jQuery name space