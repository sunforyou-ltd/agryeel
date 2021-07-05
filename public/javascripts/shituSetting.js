/* AGRYEEL 質情報設定画面 JQUERY */
(function($){

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){


    });

    $.ajax({
      url:"/shituSettingInit", 											//質情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var shituId = jsonResult["shituId"];								//質ID取得

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(shituId == 0){					//登録の場合
    	  htmlString+= '<span class="white-text" id="G2402Title" farmId="' + jsonResult["farmId"] + '" shituId="' + jsonResult["shituId"] + '">等級・質情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G2402Title" farmId="' + jsonResult["farmId"] + '" shituId="' + jsonResult["shituId"] + '">等級・質情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 質名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(shituId == 0){
          htmlString+= '<input id="G2402InShituName" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G2402InShituName" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["shituName"] + '">';
      }
      htmlString+= '<label for="G2402InShituName">等級・質名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(shituId == 0){					//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2402Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G2402Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2402Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2402Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G2402Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G2401ShituSetting").html(htmlString);									//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化

      $("#G2402Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G2402Delete").bind('click', onDelete );     							//削除ボタン
      $("#G2402Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G2402Title").attr("farmId");
		var shituId = $("#G2402Title").attr("shituId");
		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G2402InShituName", "name" : "質名"	, "length" : "128"	, "json" : "shituName"	, "check" : { "required" : "1", "maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["shituId"] = shituId;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitShitu",
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

		var farmId = $("#G2402Title").attr("farmId");
		var shituId = $("#G2402Title").attr("shituId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"shituId":"' + shituId + '",';
	    result += '"farmId":"' + farmId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("質情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteShitu",
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
		var farmID	= $("#G2402Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/13/masterMntMove";

	}


})(jQuery); // end of jQuery name space