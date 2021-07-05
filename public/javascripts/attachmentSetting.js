/* AGRYEEL アタッチメント情報設定画面 JQUERY */
(function($){

  var accountTypes = {
	"1" : { "typeId" : "1", "typeName" : "ロータリー" }
   ,"2" : { "typeId" : "2", "typeName" : "肥料散布アタッチメント" }
   ,"3" : { "typeId" : "3", "typeName" : "トラクターマルチャー" }
   ,"4" : { "typeId" : "4", "typeName" : "管理機マルチャー" }
   ,"5" : { "typeId" : "5", "typeName" : "ロータリーマルチャー" }
   ,"6" : { "typeId" : "6", "typeName" : "土壌消毒機械" }
   ,"7" : { "typeId" : "7", "typeName" : "播種アタッチメント" }
   ,"8" : { "typeId" : "8", "typeName" : "粒剤散布アタッチメント" }
   ,"9" : { "typeId" : "9", "typeName" : "溝切アタッチメント" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    });

    $.ajax({
      url:"/attachmentSettingInit", 									//アタッチメント情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var attachmentId = jsonResult["attachmentId"];					//アタッチメントID取得
      var attachmentKindID = 0;											//アタッチメント種別ID
      var attachmentKind = "";											//アタッチメント種別

      //アタッチメント種別取得
      if(attachmentId != 0){				//修正の場合
    	  if(jsonResult["attachmentKind"] == 1){
    		  attachmentKind = "ロータリー";
    	  }else if(jsonResult["attachmentKind"] == 2){
    		  attachmentKind = "肥料散布アタッチメント";
    	  }else if(jsonResult["attachmentKind"] == 3){
    		  attachmentKind = "トラクターマルチャー";
    	  }else if(jsonResult["attachmentKind"] == 4){
    		  attachmentKind = "管理機マルチャー";
    	  }else if(jsonResult["attachmentKind"] == 5){
    		  attachmentKind = "ロータリーマルチャー";
    	  }else if(jsonResult["attachmentKind"] == 6){
    		  attachmentKind = "土壌消毒機械";
    	  }else if(jsonResult["attachmentKind"] == 7){
    		  attachmentKind = "播種アタッチメント";
    	  }else if(jsonResult["attachmentKind"] == 8){
    		  attachmentKind = "粒剤散布アタッチメント";
    	  }else if(jsonResult["attachmentKind"] == 9){
    		  attachmentKind = "溝切アタッチメント";
    	  }else{
    		  attachmentKind = "なし";
    	  }
    	  attachmentKindID = jsonResult["attachmentKind"];
      }else{
    	  attachmentKind = "なし";
	  }

      //選択用モーダルリストの生成
      //振込先口座種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2201ModalTypes', 'アタッチメント種別', accountTypes, 'typeId', 'typeName', 'G2201AttachmentKind', 'attachmentKind');
      $("#G2201SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(attachmentId == 0){				//登録の場合
    	  htmlString+= '<span class="white-text" id="G2201Title" farmId="' + jsonResult["farmId"] + '" attachmentId="' + jsonResult["attachmentId"] + '">アタッチメント情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G2201Title" farmId="' + jsonResult["farmId"] + '" attachmentId="' + jsonResult["attachmentId"] + '">アタッチメント情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* アタッチメント名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(attachmentId == 0){
          htmlString+= '<input id="G2201InAttachmentName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G2201InAttachmentName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["attachementName"] + '">';
      }
      htmlString+= '<label for="G2201InAttachmentName">アタッチメント名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //型式
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(attachmentId == 0){
    	  htmlString+= '<input id="G2201InKatasiki" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G2201InKatasiki" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["katasiki"] + '">';
      }
      htmlString+= '<label for="G2201InKatasiki">型式を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* アタッチメント種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= 'アタッチメント種別<span style="color:red">（*）</span><a href="#G2201ModalTypes"  class="collection-item modal-trigger select-modal"><span id="G2201AttachmentKindSpan" class="blockquote-input">' + attachmentKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2201AttachmentKindValue" value="' + attachmentKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(attachmentId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2201Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2201Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2201Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2201Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2201Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G2201AttachmentSetting").html(htmlString);							//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								// 選択用モーダル画面初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化

      $("#G2201Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G2201Delete").bind('click', onDelete );     							//削除ボタン
      $("#G2201Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G2201Title").attr("farmId");
		var attachmentId = $("#G2201Title").attr("attachmentId");
		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G2201InAttachmentName"	, "name" : "アタッチメント名"	, "length" : "128"	, "json" : "attachementName"	, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2201InKatasiki"			, "name" : "型式"				, "length" : "128"	, "json" : "katasiki"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2201AttachmentKindValue"	, "name" : "アタッチメント種別"	, "length" : "16"	, "json" : "attachmentKind"		, "check" : { "maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* アタッチメント種別のチェック */
        var kind = $("#G2201AttachmentKindValue").val();

        if (kind == 0) {
          displayToast('アタッチメント種別を選択してください。', 4000, 'rounded');
          return false;
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["attachmentId"] = attachmentId;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitAttachment",
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

		var farmId = $("#G2201Title").attr("farmId");
		var attachmentId = $("#G2201Title").attr("attachmentId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"attachmentId":"' + attachmentId + '",';
	    result += '"farmId":"' + farmId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);
		if (confirm("アッタチメント情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteAttachment",
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
		var farmID	= $("#G2201Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/11/masterMntMove";

	}


})(jQuery); // end of jQuery name space