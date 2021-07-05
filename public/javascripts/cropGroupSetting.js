/* AGRYEEL 生産物グループ情報設定画面 JQUERY */
(function($){

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    	  $('.modal').modal();
    	  $('.mselectmodal-trigger').unbind('click');
    	  $('.mselectmodal-trigger').bind('click', mSelectOpen);

    });

    $.ajax({
      url:"/cropGroupSettingInit", 										//生産物グループ情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var cropGroupId = jsonResult["cropGroupId"];						//生産物グループID取得

      /* 所属生産物文字列生成 */
      var spanMsg = "";
      var msgCnt  = 0;
      var cropList = jsonResult["groupDataList"]; 						//jSONデータより生産物リストを取得
      if(cropGroupId == 0){
    	  //登録の場合
    	  spanMsg = "未選択";
      }else{
    	  //修正の場合
	      for ( var cropKey in cropList ) {							//所属生産物件数分処理を行う

	          var cropData = cropList[cropKey];

	          if (msgCnt > 1) {
	              spanMsg += "，．．．";
	              break;
	          }
	          else if (msgCnt == 1) {
	              spanMsg += "，";
	          }
	          spanMsg += cropData["cropName"];
	          msgCnt++;
	      }
      }

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(cropGroupId == 0){			//登録の場合
    	  htmlString+= '<span class="white-text" id="G1602Title" farmId="' + jsonResult["farmId"] + '" cropGroupId="' + jsonResult["cropGroupId"] + '">品目グループ情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G1602Title" farmId="' + jsonResult["farmId"] + '" cropGroupId="' + jsonResult["cropGroupId"] + '">品目グループ情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 生産物グループ名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(cropGroupId == 0){
          htmlString+= '<input id="G1602InCropGroupName" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1602InCropGroupName" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["cropGroupName"] + '">';
      }
      htmlString+= '<label for="G1602InCropGroupName">品目グループ名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 所属生産物 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '所属品目<span style="color:red">（*）</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="品目一覧" data="' + jsonResult["farmId"] + '/' + jsonResult["cropGroupId"] + '/getCrop" displayspan="#G1602Cropspan"><span id="G1602Cropspan" class="blockquote-input">' + spanMsg + '</span></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(cropGroupId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1602Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1602Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1602Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1602Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1602Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G1601CropGroupSetting").html(htmlString);								//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
	  mSelectDataSet("#G1602Cropspan", jsonResult["farmId"] + '/' + jsonResult["cropGroupId"] + '/getCrop');	//マルチセレクトモーダル初期処理

      $("#G1602Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G1602Delete").bind('click', onDelete );     							//削除ボタン
      $("#G1602Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G1602Title").attr("farmId");
		var cropGroupId = $("#G1602Title").attr("cropGroupId");
		var color = $("#cropspan").attr("color");
		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G1602InCropGroupName"		, "name" : "品目グループ名"				, "length" : "128"	, "json" : "cropGroupName"		, "check" : { "required" : "1", "maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 所属生産物のチェック */
        var crop = mSelectConvertJson("#G1602Cropspan");

        if (crop == "") {
          displayToast('品目を選択してください。', 4000, 'rounded');
          return false;
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["cropGroupId"] = cropGroupId;
	    jsondata["crop"] = crop;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitCropGroup",
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

		var cropGroupId = $("#G1602Title").attr("cropGroupId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"cropGroupId":"' + cropGroupId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("品目グループ情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteCropGroup",
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
		var farmID	= $("#G1602Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/5/masterMntMove";

	}


})(jQuery); // end of jQuery name space