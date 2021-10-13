/* AGRYEEL 土情報設定画面 JQUERY */
(function($){

  var soilKindList = {
	"0" : { "typeId" : "0", "typeName" : "なし" }
   ,"1" : { "typeId" : "1", "typeName" : "培土" }
   ,"2" : { "typeId" : "2", "typeName" : "覆土" }
  };

  var unitKindList = {
	"1" : { "typeId" : "1", "typeName" : "Kg" }
   ,"2" : { "typeId" : "2", "typeName" : "L" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    });

    $.ajax({
      url:"/soilSettingInit", 											//土情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var soilId = jsonResult["soilId"];								//土ID取得
      var soilKindID = 0;												//土種別ID
      var soilKind = "";												//土種別
      var unitKindID = 0;												//単位種別ID
      var unitKind = "";												//単位種別

      //土種別取得
      if(soilId != 0){				//修正の場合
    	  if(jsonResult["soilKind"] == 0){
    		  soilKind = "なし";
    	  }else if(jsonResult["soilKind"] == 1){
    		  soilKind = "培土";
    	  }else if(jsonResult["soilKind"] == 2){
    		  soilKind = "覆土";
    	  }else{
    		  soilKind = "なし";
    	  }
    	  soilKindID = jsonResult["soilKind"];
      }else{
    	  soilKind = "なし";
	  }

      //単位種別取得
      if(soilId != 0){				//修正の場合
    	  if(jsonResult["unitKind"] == 1){
    		  unitKind = "Kg";
    	  }else if(jsonResult["unitKind"] == 2){
    		  unitKind = "L";
    	  }else{
    		  unitKind = "なし";
    	  }
    	  unitKindID = jsonResult["unitKind"];
      }else{
    	  unitKind = "なし";
	  }

      //選択用モーダルリストの生成
      //土種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2801ModalSoilKind', '土種別', soilKindList, 'typeId', 'typeName', 'G2801SoilKind', 'soilKind');
      //単位種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2801ModalUnitKind', '単位種別', unitKindList, 'typeId', 'typeName', 'G2801UnitKind', 'unitKind');
      $("#G2801SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(soilId == 0){					//登録の場合
    	  htmlString+= '<span class="white-text" id="G2801Title" farmId="' + jsonResult["farmId"] + '" soilId="' + jsonResult["soilId"] + '">土情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G2801Title" farmId="' + jsonResult["farmId"] + '" soilId="' + jsonResult["soilId"] + '">土情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row" id="G2801SoilInfo">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 土名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(soilId == 0){
          htmlString+= '<input id="G2801InSoilName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active; color: #eeeeee;" value="">';
      }else{
          htmlString+= '<input id="G2801InSoilName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active; color: #eeeeee;" value="' + jsonResult["soilName"] + '">';
      }
      htmlString+= '<label for="G2801InSoilName">土名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 土種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '土種別<span style="color:red">（*）</span><a href="#G2801ModalSoilKind"  class="collection-item modal-trigger select-modal"><span id="G2801SoilKindSpan" class="blockquote-input">' + soilKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2801SoilKindValue" value="' + soilKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 単位種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '単位<a href="#G2801ModalUnitKind"  class="collection-item modal-trigger select-modal"><span id="G2801UnitKindSpan" class="blockquote-input">' + unitKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2801UnitKindValue" value="' + unitKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //金額
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(soilId == 0){
          htmlString+= '金額<span id="G2801KingakuSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2801Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2801Kingaku" value="0">';
      }else{
          htmlString+= '金額<span id="G2801KingakuSpan" class="blockquote-input">' + jsonResult["kingaku"] + '</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2801Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2801Kingaku" value="' + jsonResult["kingaku"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(soilId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2801Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2801Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2801Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2801Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2801Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G2801SoilSetting").html(htmlString);									//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  CalcInit();														        //数値入力電卓初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化

      $("#G2801Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G2801Delete").bind('click', onDelete );     							//削除ボタン
      $("#G2801Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G2801Title").attr("farmId");
		var soilId = $("#G2801Title").attr("soilId");
		var soilKind = $("#G2801SoilKindValue").val();
		var unitKind = $("#G2801UnitKindValue").val();

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G2801InSoilName"			, "name" : "土器名"						, "length" : "128"	, "json" : "soilName"			, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2801SoilKindValue"		, "name" : "土器種別"					, "length" : "16"	, "json" : "soilKind"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2801UnitKindValue"		, "name" : "単位種別"					, "length" : "16"	, "json" : "unitKind"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2801Kingaku"				, "name" : "金額"						, "length" : "32"	, "json" : "kingaku"			, "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 土種別のチェック */
        if (soilKind == 0) {
          displayToast('土種別を選択してください。', 4000, 'rounded');
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
	    jsondata["soilId"] = soilId;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitSoil",
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

		var soilId = $("#G2801Title").attr("soilId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"soilId":"' + soilId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("土情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteSoil",
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
		var farmID	= $("#G2801Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/18/masterMntMove";

	}


})(jQuery); // end of jQuery name space