/* AGRYEEL 荷姿情報設定画面 JQUERY */
(function($){

  var nisugataKindList = {
	"1" : { "typeId" : "1", "typeName" : "袋" }
   ,"2" : { "typeId" : "2", "typeName" : "箱" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){


    });

    $.ajax({
      url:"/nisugataSettingInit", 										//荷姿情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var nisugataId = jsonResult["nisugataId"];						//荷姿ID取得
      var nisugataKindID = 0;											//荷姿種別ID
      var nisugataKind = "";											//荷姿種別

      //荷姿種別取得
      if(nisugataId != 0){				//修正の場合
    	  if(jsonResult["nisugataKind"] == 1){
    		  nisugataKind = "袋";
    	  }else if(jsonResult["nisugataKind"] == 2){
    		  nisugataKind = "箱";
    	  }else{
    		  nisugataKind = "なし";
    	  }
    	  nisugataKindID = jsonResult["nisugataKind"];
      }else{
    	  nisugataKind = "なし";
	  }

      //選択用モーダルリストの生成
      //荷姿種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2301ModalNisugataKind', '荷姿種別', nisugataKindList, 'typeId', 'typeName', 'G2301NisugataKind', 'nisugataKind');
      $("#G2301SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(nisugataId == 0){					//登録の場合
    	  htmlString+= '<span class="white-text" id="G2302Title" farmId="' + jsonResult["farmId"] + '" nisugataId="' + jsonResult["nisugataId"] + '">荷姿情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G2302Title" farmId="' + jsonResult["farmId"] + '" nisugataId="' + jsonResult["nisugataId"] + '">荷姿情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 荷姿名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(nisugataId == 0){
          htmlString+= '<input id="G2302InNisugataName" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G2302InNisugataName" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["nisugataName"] + '">';
      }
      htmlString+= '<label for="G2302InNisugataName">荷姿名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 荷姿種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '荷姿種別<span style="color:red">（*）</span><a href="#G2301ModalNisugataKind"  class="collection-item modal-trigger select-modal"><span id="G2301NisugataKindSpan" class="blockquote-input">' + nisugataKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2301NisugataKindValue" value="' + nisugataKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //内容量
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nisugataId == 0){
          htmlString+= '内容量<span id="G2301CapacitySpan" class="blockquote-input">0</span><span class="blockquote-inputOption">ｇ</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2301Capacity">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2301Capacity" value="0">';
      }else{
          htmlString+= '内容量<span id="G2301CapacitySpan" class="blockquote-input">' + jsonResult["capacity"] + '</span><span class="blockquote-inputOption">ｇ</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2301Capacity">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2301Capacity" value="' + jsonResult["capacity"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //金額
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nisugataId == 0){
          htmlString+= '金額<span id="G2301KingakuSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2301Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2301Kingaku" value="0">';
      }else{
          htmlString+= '金額<span id="G2301KingakuSpan" class="blockquote-input">' + jsonResult["kingaku"] + '</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2301Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2301Kingaku" value="' + jsonResult["kingaku"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(nisugataId == 0){					//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2302Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2302Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{								//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2302Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2302Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2302Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G2301NisugataSetting").html(htmlString);								//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  CalcInit();														        // 数値入力電卓初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化

      $("#G2302Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G2302Delete").bind('click', onDelete );     							//削除ボタン
      $("#G2302Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G2302Title").attr("farmId");
		var nisugataId = $("#G2302Title").attr("nisugataId");
		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G2302InNisugataName"		, "name" : "荷姿名"		, "length" : "128"	, "json" : "nisugataName"	, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2301NisugataKindValue"	, "name" : "区画種別"	, "length" : "16"	, "json" : "nisugataKind"	, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2301Capacity"			, "name" : "内容量"		, "length" : "32"	, "json" : "capacity"		, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G2301Kingaku"				, "name" : "金額"		, "length" : "32"	, "json" : "kingaku"		, "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 荷姿種別のチェック */
        var kind = $("#G2301NisugataKindValue").val();

        if (kind == 0) {
          displayToast('荷姿種別を選択してください。', 4000, 'rounded');
          return false;
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["nisugataId"] = nisugataId;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitNisugata",
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

		var farmId = $("#G2302Title").attr("farmId");
		var nisugataId = $("#G2302Title").attr("nisugataId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"nisugataId":"' + nisugataId + '",';
	    result += '"farmId":"' + farmId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("荷姿情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteNisugata",
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
		var farmID	= $("#G2302Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/12/masterMntMove";

	}


})(jQuery); // end of jQuery name space