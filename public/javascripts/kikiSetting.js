/* AGRYEEL 機器情報設定画面 JQUERY */
(function($){

  var kikiTypes = {
	 "1" : { "typeId" : "1",  "typeName" : "トラクター" }
   , "2" : { "typeId" : "2",  "typeName" : "テーラー" }
   , "3" : { "typeId" : "3",  "typeName" : "土上げ管理機" }
   , "4" : { "typeId" : "4",  "typeName" : "肥料散布機械" }
   , "5" : { "typeId" : "5",  "typeName" : "肥料散布械" }
   , "6" : { "typeId" : "6",  "typeName" : "消毒機械" }
   , "7" : { "typeId" : "7",  "typeName" : "消毒械" }
   , "8" : { "typeId" : "8",  "typeName" : "草刈機械" }
   , "9" : { "typeId" : "9",  "typeName" : "播種械" }
   ,"10" : { "typeId" : "10", "typeName" : "定植機" }
   ,"11" : { "typeId" : "11", "typeName" : "潅水機" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){
  	  $('.mselectmodal-trigger').unbind('click');
	  $('.mselectmodal-trigger').bind('click', mSelectOpen);
    });

    $.ajax({
      url:"/kikiSettingInit", 											//機器情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var kikiId = jsonResult["kikiId"];								//機器ID取得
      var kikiKindID = 0;												//機器種別ID
      var kikiKind = "";												//機器種別

      //機器種別取得
      if(kikiId != 0){				//修正の場合
    	  if(jsonResult["kikiKind"] == 1){
    		  kikiKind = "トラクター";
    	  }else if(jsonResult["kikiKind"] == 2){
    		  kikiKind = "テーラー";
    	  }else if(jsonResult["kikiKind"] == 3){
    		  kikiKind = "土上げ管理機";
    	  }else if(jsonResult["kikiKind"] == 4){
    		  kikiKind = "肥料散布機械";
    	  }else if(jsonResult["kikiKind"] == 5){
    		  kikiKind = "肥料散布械";
    	  }else if(jsonResult["kikiKind"] == 6){
    		  kikiKind = "消毒機械";
    	  }else if(jsonResult["kikiKind"] == 7){
    		  kikiKind = "消毒械";
    	  }else if(jsonResult["kikiKind"] == 8){
    		  kikiKind = "草刈機械";
    	  }else if(jsonResult["kikiKind"] == 9){
    		  kikiKind = "播種械";
    	  }else if(jsonResult["kikiKind"] == 10){
    		  kikiKind = "定植機";
        }else if(jsonResult["kikiKind"] == 11){
          kikiKind = "潅水機";
    	  }else{
    		  kikiKind = "なし";
    	  }
    	  kikiKindID = jsonResult["kikiKind"];
      }else{
    	  kikiKind = "なし";
	  }

      /* 使用可能アタッチメント文字列生成 */
      var spanMsg = "";
      var msgCnt  = 0;
      var attachList = jsonResult["attachDataList"]; 					//jSONデータよりアタッチメントリストを取得
      if(kikiId == 0){
    	  //登録の場合
    	  spanMsg = "未選択";
      }else{
    	  //修正の場合
	      for ( var attachKey in attachList ) {							//アタッチメント件数分処理を行う

	          var attachData = attachList[attachKey];

	          if (msgCnt > 1) {
	              spanMsg += "，．．．";
	              break;
	          }
	          else if (msgCnt == 1) {
	              spanMsg += "，";
	          }
	          spanMsg += attachData["attachementName"];
	          msgCnt++;
	      }
      }

      //選択用モーダルリストの生成
      //機器種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2101ModalTypes', '機器種別', kikiTypes, 'typeId', 'typeName', 'G2101KikiKind', 'kikiKind');
      $("#G2101SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(kikiId == 0){					//登録の場合
    	  htmlString+= '<span class="white-text" id="G2101Title" farmId="' + jsonResult["farmId"] + '" kikiId="' + jsonResult["kikiId"] + '">機器情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G2101Title" farmId="' + jsonResult["farmId"] + '" kikiId="' + jsonResult["kikiId"] + '">機器情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row" id="G2101KikiInfo">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 機器名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(kikiId == 0){
          htmlString+= '<input id="G2101InKikiName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G2101InKikiName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["kikiName"] + '">';
      }
      htmlString+= '<label for="G2201InKikiName">機器名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //型式
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(kikiId == 0){
    	  htmlString+= '<input id="G2101InKatasiki" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G2101InKatasiki" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["katasiki"] + '">';
      }
      htmlString+= '<label for="G2101InKatasiki">型式を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //メーカー
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(kikiId == 0){
    	  htmlString+= '<input id="G2101InMaker" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G2101InMaker" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["maker"] + '">';
      }
      htmlString+= '<label for="G2101InMaker">メーカーを入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 機器種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '機器種別<span style="color:red">（*）</span><a href="#G2101ModalTypes"  class="collection-item modal-trigger select-modal"><span id="G2101KikiKindSpan" class="blockquote-input">' + kikiKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2101KikiKindValue" value="' + kikiKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 使用可能アタッチメント */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '使用可能アタッチメント<a href="#mselectmodal"  class="mselectmodal-trigger" title="アタッチメント一覧" data="' + jsonResult["farmId"] + '/' + jsonResult["kikiId"] + '/getAttachmentOfFarmSel" displayspan="#G2102Attachmentspan"><span id="G2102Attachmentspan" class="blockquote-input">' + spanMsg + '</span></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //金額
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kikiId == 0){
          htmlString+= '金額<span id="G2101KingakuSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2101Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2101Kingaku" value="0">';
      }else{
          htmlString+= '金額<span id="G2101KingakuSpan" class="blockquote-input">' + jsonResult["kingaku"] + '</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2101Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2101Kingaku" value="' + jsonResult["kingaku"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //購入日の生成
      var purchaseday = jsonResult["purchaseDate"];
	    htmlString+= '<div class="row">';
      htmlString += '<div class="input-field col s10 offset-s1 grey-text">';
      if(!purchaseday){
          htmlString += '<input type="text" placeholder="購入日" id="G2101PurchaseDate" class="datepicker input-text-color" style="" value="">';
      }else{
          htmlString += '<input type="text" placeholder="購入日" id="G2101PurchaseDate" class="datepicker input-text-color" style="" value="' + purchaseday.replace("-", "/").replace("-", "/") + '">';
      }
      htmlString+= '<label for="G2101PurchaseDate">購入日を入力してください</label>';
      htmlString += '</div>';
      htmlString+= '</div>';

      //日数
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kikiId == 0){
          htmlString+= '日数<span id="G2101DaysSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">日</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2101Days">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2101Days" value="0">';
      }else{
          htmlString+= '日数<span id="G2101DaysSpan" class="blockquote-input">' + jsonResult["days"] + '</span><span class="blockquote-inputOption">日</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2101Days">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2101Days" value="' + jsonResult["days"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //耐用年数
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kikiId == 0){
          htmlString+= '耐用年数<span id="G2101ServiceLifeSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">年</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2101ServiceLife">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2101ServiceLife" value="0">';
      }else{
          htmlString+= '耐用年数<span id="G2101ServiceLifeSpan" class="blockquote-input">' + jsonResult["serviceLife"] + '</span><span class="blockquote-inputOption">年</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2101ServiceLife">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2101ServiceLife" value="' + jsonResult["serviceLife"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(kikiId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2101Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2101Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2101Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2101Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2101Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G2101KikiSetting").html(htmlString);									//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  CalcInit();														        //数値入力電卓初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化
	  mSelectDataSet("#G2102Attachmentspan", jsonResult["farmId"] + '/' + jsonResult["kikiId"] + '/getAttachmentOfFarmSel');	//マルチセレクトモーダル初期処理

      $("#G2101Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G2101Delete").bind('click', onDelete );     							//削除ボタン
      $("#G2101Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G2101Title").attr("farmId");
		var kikiId = $("#G2101Title").attr("kikiId");
		var purchaseday =  $("#G2101PurchaseDate").val();

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G2101InKikiName"		, "name" : "機器名"		, "length" : "128"	, "json" : "kikiName"	, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2101InKatasiki"		, "name" : "型式"		, "length" : "128"	, "json" : "katasiki"	, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2101InMaker"			, "name" : "メーカー"	, "length" : "128"	, "json" : "maker"		, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2101KikiKindValue"	, "name" : "機器種別"	, "length" : "16"	, "json" : "kikiKind"	, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2101Kingaku"			, "name" : "金額"		, "length" : "32"	, "json" : "kingaku"	, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G2101Days"			, "name" : "日数"		, "length" : "3"	, "json" : "days"		, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G2101ServiceLife"		, "name" : "耐用年数"	, "length" : "2"	, "json" : "serviceLife", "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 機器種別のチェック */
        var kind = $("#G2101KikiKindValue").val();

        if (kind == 0) {
          displayToast('機器種別を選択してください。', 4000, 'rounded');
          return false;
        }

		/* 使用可能アタッチメントの取得 */
        var attachment = mSelectConvertJson("#G2102Attachmentspan");

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["kikiId"] = kikiId;
	    jsondata["onUseAttachmentId"] = attachment;
	    jsondata["purchaseDate"] = purchaseday;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitKiki",
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

	}

    /* 削除ボタン押下処理 */
	function onDelete() {

		var farmId = $("#G2101Title").attr("farmId");
		var kikiId = $("#G2101Title").attr("kikiId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"kikiId":"' + kikiId + '",';
	    result += '"farmId":"' + farmId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("機器情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteKiki",
				type:'POST',
		        data:JSON.stringify(jsondata),									//入力用JSONデータ
				complete:function(data, status, jqXHR){							//処理成功時

				onProcing(false);
			    onBack()

				},
			    dataType:'json',
			    contentType:'text/json',
			    async: true
			});
		}
	}

    /* 戻るボタン押下処理 */
	function onBack() {
		var farmID	= $("#G2101Title").attr("farmId");						//生産者IDを格納

		window.location.href = "/" + farmID + "/10/masterMntMove";

	}


})(jQuery); // end of jQuery name space