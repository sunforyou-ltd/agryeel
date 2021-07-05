/* AGRYEEL 資材情報設定画面 JQUERY */
(function($){

  var sizaiKindList = {
	"1"  : { "typeId" :  "1", "typeName" : "マルチ" }
   ,"2"  : { "typeId" :  "2", "typeName" : "資材" }
   ,"3"  : { "typeId" :  "3", "typeName" : "培土" }
   ,"4"  : { "typeId" :  "4", "typeName" : "ビニール" }
   ,"5"  : { "typeId" :  "5", "typeName" : "土壌消毒用ビニール" }
   ,"6"  : { "typeId" :  "6", "typeName" : "セルトレイ" }
   ,"7"  : { "typeId" :  "7", "typeName" : "苗箱" }
   ,"8"  : { "typeId" :  "8", "typeName" : "ペーパーポット" }
   ,"9"  : { "typeId" :  "9", "typeName" : "ポット" }
   ,"10" : { "typeId" : "10", "typeName" : "潅水チューブ" }
   ,"11" : { "typeId" : "11", "typeName" : "トンネル上ヘゴ" }
   ,"12" : { "typeId" : "12", "typeName" : "トンネル下ヘゴ" }
   ,"13" : { "typeId" : "13", "typeName" : "トンネルビニール" }
   ,"14" : { "typeId" : "14", "typeName" : "ロータリー爪" }
   ,"15" : { "typeId" : "15", "typeName" : "エンジンオイル" }
   ,"16" : { "typeId" : "16", "typeName" : "農具" }
   ,"17" : { "typeId" : "17", "typeName" : "農具消耗品" }
  };

  var unitKindList = {
	"1" : { "typeId" : "1", "typeName" : "Kg" }
   ,"2" : { "typeId" : "2", "typeName" : "l" }
   ,"3" : { "typeId" : "3", "typeName" : "g" }
   ,"4" : { "typeId" : "4", "typeName" : "mL" }
   ,"5" : { "typeId" : "5", "typeName" : "個" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){


    });

    $.ajax({
      url:"/sizaiSettingInit", 										//資材情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){						//処理成功時
      var jsonResult = JSON.parse( data.responseText );				//戻り値用JSONデータの生成
      var htmlString	= "";										//可変HTML文字列
      var sizaiId = jsonResult["sizaiId"];							//資材ID取得
      var sizaiKindID = 0;											//資材種別ID
      var sizaiKind = "";											//資材種別
      var unitKindID = 0;											//単位種別ID
      var unitKind = "";											//単位種別

      //資材種別取得
      if(sizaiId != 0){				//修正の場合
    	  if(jsonResult["sizaiKind"] == 1){
    		  sizaiKind = "マルチ";
    	  }else if(jsonResult["sizaiKind"] == 2){
    		  sizaiKind = "資材";
    	  }else if(jsonResult["sizaiKind"] == 3){
    		  sizaiKind = "培土";
    	  }else if(jsonResult["sizaiKind"] == 4){
    		  sizaiKind = "ビニール";
    	  }else if(jsonResult["sizaiKind"] == 5){
    		  sizaiKind = "土壌消毒用ビニール";
    	  }else if(jsonResult["sizaiKind"] == 6){
    		  sizaiKind = "セルトレイ";
    	  }else if(jsonResult["sizaiKind"] == 7){
    		  sizaiKind = "苗箱";
    	  }else if(jsonResult["sizaiKind"] == 8){
    		  sizaiKind = "ペーパーポット";
    	  }else if(jsonResult["sizaiKind"] == 9){
    		  sizaiKind = "ポット";
    	  }else if(jsonResult["sizaiKind"] == 10){
    		  sizaiKind = "潅水チューブ";
    	  }else if(jsonResult["sizaiKind"] == 11){
    		  sizaiKind = "トンネル上ヘゴ";
    	  }else if(jsonResult["sizaiKind"] == 12){
    		  sizaiKind = "トンネル下ヘゴ";
    	  }else if(jsonResult["sizaiKind"] == 13){
    		  sizaiKind = "トンネルビニール";
    	  }else if(jsonResult["sizaiKind"] == 14){
    		  sizaiKind = "ロータリー爪";
    	  }else if(jsonResult["sizaiKind"] == 15){
    		  sizaiKind = "エンジンオイル";
    	  }else if(jsonResult["sizaiKind"] == 16){
    		  sizaiKind = "農具";
    	  }else if(jsonResult["sizaiKind"] == 17){
    		  sizaiKind = "農具消耗品";
    	  }else{
    		  sizaiKind = "なし";
    	  }
    	  sizaiKindID = jsonResult["sizaiKind"];
      }else{
    	  sizaiKind = "なし";
	  }

      //単位種別取得
      if(sizaiId != 0){				//修正の場合
    	  if(jsonResult["unitKind"] == 1){
    		  unitKind = "Kg";
    	  }else if(jsonResult["unitKind"] == 2){
    		  unitKind = "l";
    	  }else if(jsonResult["unitKind"] == 3){
    		  unitKind = "g";
    	  }else if(jsonResult["unitKind"] == 4){
    		  unitKind = "mL";
    	  }else if(jsonResult["unitKind"] == 5){
    		  unitKind = "個";
    	  }else{
    		  unitKind = "なし";
    	  }
    	  unitKindID = jsonResult["unitKind"];
      }else{
    	  unitKind = "なし";
	  }

      //選択用モーダルリストの生成
      //資材種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2601ModalSizaiKind', '資材種別', sizaiKindList, 'typeId', 'typeName', 'G2601SizaiKind', 'sizaiKind');
      //単位種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G2601ModalUnitKind', '単位種別', unitKindList, 'typeId', 'typeName', 'G2601UnitKind', 'unitKind');
      $("#G2601SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(sizaiId == 0){					//登録の場合
    	  htmlString+= '<span class="white-text" id="G2602Title" farmId="' + jsonResult["farmId"] + '" sizaiId="' + jsonResult["sizaiId"] + '">資材情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G2602Title" farmId="' + jsonResult["farmId"] + '" sizaiId="' + jsonResult["sizaiId"] + '">資材情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 資材名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(sizaiId == 0){
          htmlString+= '<input id="G2602InSizaiName" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G2602InSizaiName" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["sizaiName"] + '">';
      }
      htmlString+= '<label for="G2602InSizaiName">資材名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 資材種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '資材種別<span style="color:red">（*）</span><a href="#G2601ModalSizaiKind"  class="collection-item modal-trigger select-modal"><span id="G2601SizaiKindSpan" class="blockquote-input">' + sizaiKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2601SizaiKindValue" value="' + sizaiKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 単位種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '単位<a href="#G2601ModalUnitKind"  class="collection-item modal-trigger select-modal"><span id="G2601UnitKindSpan" class="blockquote-input">' + unitKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G2601UnitKindValue" value="' + unitKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //量
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(sizaiId == 0){
          htmlString+= '量<span id="G2601RyoSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2601Ryo">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2601Ryo" value="0">';
      }else{
          htmlString+= '量<span id="G2601RyoSpan" class="blockquote-input">' + jsonResult["ryo"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2601Ryo">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2601Ryo" value="' + jsonResult["ryo"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //金額
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(sizaiId == 0){
          htmlString+= '金額<span id="G2601KingakuSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2601Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2601Kingaku" value="0">';
      }else{
          htmlString+= '金額<span id="G2601KingakuSpan" class="blockquote-input">' + jsonResult["kingaku"] + '</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2601Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2601Kingaku" value="' + jsonResult["kingaku"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //購入日の生成
      var purchaseday = jsonResult["purchaseDate"];
	    htmlString+= '<div class="row">';
      htmlString += '<div class="input-field col s10 offset-s1 grey-text">';
      if(!purchaseday){
          htmlString += '<input type="text" placeholder="購入日" id="G2601PurchaseDate" class="datepicker input-text-color" style="" value="">';
      }else{
          htmlString += '<input type="text" placeholder="購入日" id="G2601PurchaseDate" class="datepicker input-text-color" style="" value="' + purchaseday.replace("-", "/").replace("-", "/") + '">';
      }
      htmlString+= '<label for="G2601PurchaseDate">購入日を入力してください</label>';
      htmlString += '</div>';
      htmlString+= '</div>';

      //耐用年数
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(sizaiId == 0){
          htmlString+= '耐用年数<span id="G2601ServiceLifeSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">年</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2601ServiceLife">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2601ServiceLife" value="0">';
      }else{
          htmlString+= '耐用年数<span id="G2601ServiceLifeSpan" class="blockquote-input">' + jsonResult["serviceLife"] + '</span><span class="blockquote-inputOption">年</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G2601ServiceLife">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G2601ServiceLife" value="' + jsonResult["serviceLife"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(sizaiId == 0){					//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2602Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2602Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{								//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2602Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2602Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2602Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G2601SizaiSetting").html(htmlString);									//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  CalcInit();														        // 数値入力電卓初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化

      $("#G2602Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G2602Delete").bind('click', onDelete );     							//削除ボタン
      $("#G2602Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G2602Title").attr("farmId");
		var sizaiId = $("#G2602Title").attr("sizaiId");
		var purchaseday =  $("#G2601PurchaseDate").val();

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G2602InSizaiName"		, "name" : "資材名"		, "length" : "128"	, "json" : "sizaiName"	, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G2601SizaiKindValue"	, "name" : "資材種別"	, "length" : "16"	, "json" : "sizaiKind"	, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2601UnitKindValue"	, "name" : "単位種別"	, "length" : "16"	, "json" : "unitKind"	, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G2601Ryo"				, "name" : "量"			, "length" : "32"	, "json" : "ryo"		, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G2601Kingaku"			, "name" : "金額"		, "length" : "32"	, "json" : "kingaku"	, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G2601ServiceLife"		, "name" : "耐用年数"	, "length" : "2"	, "json" : "serviceLife", "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 資材種別のチェック */
        var kind = $("#G2601SizaiKindValue").val();

        if (kind == 0) {
          displayToast('資材種別を選択してください。', 4000, 'rounded');
          return false;
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["sizaiId"] = sizaiId;
	    jsondata["purchaseDate"] = purchaseday;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitSizai",
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

		var farmId = $("#G2602Title").attr("farmId");
		var sizaiId = $("#G2602Title").attr("sizaiId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"sizaiId":"' + sizaiId + '",';
	    result += '"farmId":"' + farmId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("資材情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteSizai",
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
		var farmID	= $("#G2602Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/15/masterMntMove";

	}


})(jQuery); // end of jQuery name space