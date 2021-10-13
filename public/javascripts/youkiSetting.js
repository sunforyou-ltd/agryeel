/* AGRYEEL 容器情報設定画面 JQUERY */
(function($){

  var youkiKindList = {
	"0" : { "typeId" : "0", "typeName" : "なし" }
   ,"1" : { "typeId" : "1", "typeName" : "トレイ" }
   ,"2" : { "typeId" : "2", "typeName" : "セルトレイ" }
   ,"3" : { "typeId" : "3", "typeName" : "ポット" }
   ,"4" : { "typeId" : "4", "typeName" : "ペーパーポット" }
   ,"5" : { "typeId" : "5", "typeName" : "苗箱" }
  };

  var unitKindList = {
	"1" : { "typeId" : "1", "typeName" : "枚" }
   ,"2" : { "typeId" : "2", "typeName" : "穴" }
   ,"3" : { "typeId" : "3", "typeName" : "個" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    });

    $.ajax({
      url:"/youkiSettingInit", 											//容器情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var youkiId = jsonResult["youkiId"];								//容器ID取得
      var youkiKindID = 0;												//容器種別ID
      var youkiKind = "";												//容器種別
      var unitKindID = 0;												//単位種別ID
      var unitKind = "";												//単位種別

      //容器種別取得
      if(youkiId != 0){				//修正の場合
    	  if(jsonResult["youkiKind"] == 0){
    		  youkiKind = "なし";
    	  }else if(jsonResult["youkiKind"] == 1){
    		  youkiKind = "トレイ";
    	  }else if(jsonResult["youkiKind"] == 2){
    		  youkiKind = "セルトレイ";
    	  }else if(jsonResult["youkiKind"] == 3){
    		  youkiKind = "ポット";
    	  }else if(jsonResult["youkiKind"] == 4){
    		  youkiKind = "ペーパーポット";
    	  }else if(jsonResult["youkiKind"] == 5){
    		  youkiKind = "苗箱";
    	  }else{
    		  youkiKind = "なし";
    	  }
    	  youkiKindID = jsonResult["youkiKind"];
      }else{
    	  youkiKind = "なし";
	  }

      //単位種別取得
      if(youkiId != 0){				//修正の場合
    	  if(jsonResult["unitKind"] == 1){
    		  unitKind = "枚";
    	  }else if(jsonResult["unitKind"] == 2){
    		  unitKind = "穴";
    	  }else if(jsonResult["unitKind"] == 3){
    		  unitKind = "個";
    	  }else{
    		  unitKind = "なし";
    	  }
    	  unitKindID = jsonResult["unitKind"];
      }else{
    	  unitKind = "なし";
	  }

      //選択用モーダルリストの生成
      //容器種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2701ModalYoukiKind', '容器種別', youkiKindList, 'typeId', 'typeName', 'G2701YoukiKind', 'youkiKind');
      //単位種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2701ModalUnitKind', '単位種別', unitKindList, 'typeId', 'typeName', 'G2701UnitKind', 'unitKind');
      $("#G2701SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(youkiId == 0){					//登録の場合
    	  htmlString+= '<span class="white-text" id="G2701Title" farmId="' + jsonResult["farmId"] + '" youkiId="' + jsonResult["youkiId"] + '">容器情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G2701Title" farmId="' + jsonResult["farmId"] + '" youkiId="' + jsonResult["youkiId"] + '">容器情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row" id="G2701YoukiInfo">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 容器名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(youkiId == 0){
          htmlString+= '<input id="G2701InYoukiName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active; color: #eeeeee;" value="">';
      }else{
          htmlString+= '<input id="G2701InYoukiName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active; color: #eeeeee;" value="' + jsonResult["youkiName"] + '">';
      }
      htmlString+= '<label for="G2701InYoukiName">容器名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 容器種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '容器種別<span style="color:red">（*）</span><a href="#G2701ModalYoukiKind"  class="collection-item modal-trigger select-modal"><span id="G2701YoukiKindSpan" class="blockquote-input">' + youkiKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2701YoukiKindValue" value="' + youkiKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 単位種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '単位<a href="#G2701ModalUnitKind"  class="collection-item modal-trigger select-modal"><span id="G2701UnitKindSpan" class="blockquote-input">' + unitKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2701UnitKindValue" value="' + unitKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //個数
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(youkiId == 0){
    	  htmlString+= '個数<span id="G2701KosuSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2701Kosu">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2701Kosu" value="0">';
      }else{
    	  htmlString+= '個数<span id="G2701KosuSpan" class="blockquote-input">' + jsonResult["kosu"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2701Kosu">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2701Kosu" value="' + jsonResult["kosu"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //金額
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(youkiId == 0){
          htmlString+= '金額<span id="G2701KingakuSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2701Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2701Kingaku" value="0">';
      }else{
          htmlString+= '金額<span id="G2701KingakuSpan" class="blockquote-input">' + jsonResult["kingaku"] + '</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2701Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2701Kingaku" value="' + jsonResult["kingaku"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(youkiId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2701Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2701Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2701Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2701Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2701Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G2701YoukiSetting").html(htmlString);									//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  CalcInit();														        //数値入力電卓初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化

      $("#G2701Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G2701Delete").bind('click', onDelete );     							//削除ボタン
      $("#G2701Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G2701Title").attr("farmId");
		var youkiId = $("#G2701Title").attr("youkiId");
		var youkiKind = $("#G2701YoukiKindValue").val();
		var unitKind = $("#G2701UnitKindValue").val();

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G2701InYoukiName"			, "name" : "容器名"						, "length" : "128"	, "json" : "youkiName"			, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2701YoukiKindValue"		, "name" : "容器種別"					, "length" : "16"	, "json" : "youkiKind"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2701UnitKindValue"		, "name" : "単位種別"					, "length" : "16"	, "json" : "unitKind"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2701Kosu		"			, "name" : "個数"						, "length" : "32"	, "json" : "kosu"				, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G2701Kingaku"				, "name" : "金額"						, "length" : "32"	, "json" : "kingaku"			, "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 容器種別のチェック */
        if (youkiKind == 0) {
          displayToast('容器種別を選択してください。', 4000, 'rounded');
          return false;
        }

		/* 単位種別のチェック */
        if (unitKind == 0) {
          displayToast('単位種別を選択してください。', 4000, 'rounded');
          return false;
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["youkiId"] = youkiId;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitYouki",
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

		var youkiId = $("#G2701Title").attr("youkiId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"youkiId":"' + youkiId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("容器情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteYouki",
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
	}

    /* 戻るボタン押下処理 */
	function onBack() {
		var farmID	= $("#G2701Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/17/masterMntMove";

	}


})(jQuery); // end of jQuery name space