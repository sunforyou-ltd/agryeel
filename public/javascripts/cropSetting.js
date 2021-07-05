/* AGRYEEL 生産物情報設定画面 JQUERY */
(function($){

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    	  $('.modal').modal();
    	  $('.mselectmodal-trigger').unbind('click');
    	  $('.mselectmodal-trigger').bind('click', mSelectOpen);
    	  $('.colormodal-trigger').unbind('click');
    	  $('.colormodal-trigger').bind('click', colorOpen);

    });

    $.ajax({
      url:"/cropSettingInit", 											//生産物情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var cropId = jsonResult["cropId"];								//生産物ID取得

      /* 所属品種文字列生成 */
      var spanMsg = "";
      var msgCnt  = 0;
      var hinsyuList = jsonResult["hinsyuDataList"]; 					//jSONデータより種リストを取得
      if(cropId == 0){
    	  //登録の場合
    	  spanMsg = "未選択";
      }else{
    	  //修正の場合
	      for ( var hinsyuKey in hinsyuList ) {							//所属圃場件数分処理を行う

	          var hinsyuData = hinsyuList[hinsyuKey];

	          if (msgCnt > 1) {
	              spanMsg += "，．．．";
	              break;
	          }
	          else if (msgCnt == 1) {
	              spanMsg += "，";
	          }
	          spanMsg += hinsyuData["hinsyuName"];
	          msgCnt++;
	      }
      }

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(cropId == 0){					//登録の場合
    	  htmlString+= '<span class="white-text" id="G1702Title" farmId="' + jsonResult["farmId"] + '" cropId="' + jsonResult["cropId"] + '" cropName="" cropColor="">品目情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G1702Title" farmId="' + jsonResult["farmId"] + '" cropId="' + jsonResult["cropId"] + '" cropName="' + jsonResult["cropName"] + '"  cropColor="' + jsonResult["cropColor"] + '">品目情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 生産物名 */
	  htmlString+= '<div class="row">';
      if(cropId == 0){
          htmlString+= '<div class="input-field col s10 offset-s1">';
          htmlString+= '<input id="G1702InCropName" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
          htmlString+= '<label for="G1702InCropName">品目名を入力してください<span style="color:red">（*）</span></label>';
      }else{
          htmlString+= '<div class="input-field col s10 offset-s1 mst-item-title">';
          htmlString+= '品目名：<span class="mst-item" style="">' + jsonResult["cropName"] + '</span>';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 生産物カラー */
      if(cropId == 0){
          htmlString+= '<div class="row">';
          htmlString+= '<div class="col s10 offset-s1">';
          htmlString+= '<div class="color-picker"><span class="title mst-item-title">品目カラー</span>';
          htmlString+= '<a href="#colormodal"  class="colormodal-trigger" title="品目カラー" displayspan="#cropspan"><span id="cropspan" style="color: ' + jsonResult["cropColor"] + ';" color="' + jsonResult["cropColor"] + '">■</span></a></div>';
          htmlString+= '</div>';
          htmlString+= '</div>';
      }

      /* 所属品種 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '所属品種<span style="color:red">（*）</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="品種一覧" data="' + jsonResult["farmId"] + '/' + jsonResult["cropId"] + '/getHinsyu" displayspan="#G1702Cropspan"><span id="G1702Cropspan" class="blockquote-input">' + spanMsg + '</span></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(cropId == 0){					//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1702Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1702Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1702Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1702Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G1701CropSetting").html(htmlString);									//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
	  mSelectDataSet("#G1702Cropspan", jsonResult["farmId"] + '/' + jsonResult["cropId"] + '/getHinsyu');	//マルチセレクトモーダル初期処理

      $("#G1702Submit").bind('click', onSubmit );     							//確定ボタン
      //$("#G1702Delete").bind('click', onDelete );     							//削除ボタン
      $("#G1702Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G1702Title").attr("farmId");
		var cropId = $("#G1702Title").attr("cropId");
		var cropName = $("#G1702Title").attr("cropName");
		var color = ""
		var cropColor = $("#G1702Title").attr("cropColor");
		if (cropName == "") {
			color = $("#cropspan").attr("color").replace( '#', '' );
		} else {
			color = cropColor.replace( '#', '' );
		}

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G1702InCropName", "name" : "品目名", "length" : "128"	, "json" : "cropName"	, "check" : { "required" : "1", "maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (cropName == "") {
			if (InputDataManager(checktarget) == false) {
			  return false;
			}
		}

		/* 所属品種のチェック */
        var hinsyu = mSelectConvertJson("#G1702Cropspan");

        if (hinsyu == "") {
          displayToast('品種を選択してください。', 4000, 'rounded');
          return false;
        }

		onProcing(true);

	    var jsondata   = null;					  //JSONDATAに変換する
		if (cropName == "") {
	    	jsondata   = InputDataToJson(checktarget);						//JSONDATAに変換する
		} else {
	    	jsondata   = StringToJson('{"cropName":"' + cropName + '"}');	//JSONDATAに変換する
		}
	    jsondata["farmId"] = farmId;
	    jsondata["cropId"] = cropId;
	    jsondata["cropColor"] = color;
	    jsondata["hinsyu"] = hinsyu;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitCrop",
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

    /* 戻るボタン押下処理 */
	function onBack() {
		var farmID	= $("#G1702Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/6/masterMntMove";

	}


})(jQuery); // end of jQuery name space