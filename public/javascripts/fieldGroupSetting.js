/* AGRYEEL 圃場グループ情報設定画面 JQUERY */
(function($){

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    	  $('.modal').modal();
    	  $('.mselectmodal-trigger').unbind('click');
    	  $('.mselectmodal-trigger').bind('click', mSelectOpen);
    	  $('.colormodal-trigger').unbind('click');
    	  $('.colormodal-trigger').bind('click', colorOpen);
    	  $('.selectmodal-trigger').unbind('click');
    	  $('.selectmodal-trigger').bind('click', selectOpen);

    });

    $.ajax({
      url:"/fieldGroupSettingInit", 									//圃場グループ情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var fieldGroupId = jsonResult["fieldGroupId"];					//圃場グループID取得

      /* 所属圃場文字列生成 */
      var spanMsg = "";
      var msgCnt  = 0;
      var fieldList = jsonResult["groupDataList"]; 						//jSONデータより圃場リストを取得
      if(fieldGroupId == 0){
    	  //登録の場合
    	  spanMsg = "未選択";
      }else{
    	  //修正の場合
	      for ( var fieldKey in fieldList ) {							//所属圃場件数分処理を行う

	          var fieldData = fieldList[fieldKey];

	          if (msgCnt > 1) {
	              spanMsg += "，．．．";
	              break;
	          }
	          else if (msgCnt == 1) {
	              spanMsg += "，";
	          }
	          spanMsg += fieldData["fieldName"];
	          msgCnt++;
	      }
      }

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(fieldGroupId == 0){			//登録の場合
    	  htmlString+= '<span class="white-text" id="G1302Title" farmId="' + jsonResult["farmId"] + '" fieldGroupId="' + jsonResult["fieldGroupId"] + '">圃場グループ情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G1302Title" farmId="' + jsonResult["farmId"] + '" fieldGroupId="' + jsonResult["fieldGroupId"] + '">圃場グループ情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 圃場グループ名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(fieldGroupId == 0){
          htmlString+= '<input id="G1302InFieldGroupName" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1302InFieldGroupName" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["fieldGroupName"] + '">';
      }
      htmlString+= '<label for="G1302InFieldGroupName">圃場グループ名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 圃場グループカラー */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1  grey-text">';
      htmlString+= '<div class="color-picker"><span class="title mst-item-title">圃場グループカラー</span>';
      htmlString+= '<a href="#colormodal"  class="colormodal-trigger" title="圃場グループカラー" displayspan="#cropspan"><span id="cropspan" style="color: ' + jsonResult["fieldGroupColor"] + ';" color="' + jsonResult["fieldGroupColor"] + '">■</span></a></div>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 圃場グループ並び順 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(fieldGroupId == 0){
          htmlString+= '<input id="G1302InFieldGroupSequenceId" type="text" class="validate input-text-color" style="ime-mode: active;" value="0">';
      }else{
          htmlString+= '<input id="G1302InFieldGroupSequenceId" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["fieldGroupSequenceId"] + '">';
      }
      htmlString+= '<label for="G1302InFieldGroupSequenceId">圃場グループ並び順 を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '<i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1302InFieldGroupSequenceId">keyboard</i>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 所属圃場 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '所属圃場<span style="color:red">（*）</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="圃場一覧" data="' + jsonResult["farmId"] + '/' + jsonResult["fieldGroupId"] + '/getField" displayspan="#G1302Fieldspan"><span id="G1302Fieldspan" class="blockquote-input">' + spanMsg + '</span></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 作業指示自動生成 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '作業指示自動生成<a href="#selectmodal"  class="collection-item selectmodal-trigger" title="作業指示自動生成選択"" data="getWorkPlanAutoCreate" displayspan="#G1302PlanAutoCreatespan"><span id="G1302PlanAutoCreatespan" class="blockquote-input">未選択</span></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(fieldGroupId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1302Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1302Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1302Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1302Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1302Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G1301FieldGroupSetting").html(htmlString);							//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  mSelectDataSet("#G1302Fieldspan", jsonResult["farmId"] + '/' + jsonResult["fieldGroupId"] + '/getField');	//マルチセレクトモーダル初期処理
      CalcInit();                                   // 数値入力電卓初期化
  	  SelectModalInit();												  			//選択用モーダルイベント初期化

      $("#G1302Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G1302Delete").bind('click', onDelete );     							//削除ボタン
      $("#G1302Back").bind('click', onBack );     								//戻るボタン

      //------------------------------------------------------------------------------------------------------------------
      //- セレクトモーダルの初期選択反映
      //------------------------------------------------------------------------------------------------------------------
      //----- 作業指示自動生成 -----
      selectDataGet("#G1302PlanAutoCreatespan", "getWorkPlanAutoCreate");
      var oJson = selectData(jsonResult.workPlanAutoCreate);
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
	function onSubmit() {

		var farmId = $("#G1302Title").attr("farmId");
		var fieldGroupId = $("#G1302Title").attr("fieldGroupId");
		var color = $("#cropspan").attr("color").replace( '#', '' );
		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G1302InFieldGroupName"		    , "name" : "圃場グループ名"				, "length" : "128"	, "json" : "fieldGroupName"		, "check" : { "required" : "1", "maxlength" : "1"}}
       ,{ "id" : "G1302InFieldGroupSequenceId"  , "name" : "圃場グループ並び順"     , "length" : "3"   , "json" : "fieldGroupSequenceId"   , "check" : { "required" : "1", "maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 所属圃場のチェック */
        var field = mSelectConvertJson("#G1302Fieldspan");

        if (field == "") {
          displayToast('圃場を選択してください。', 4000, 'rounded');
          return false;
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
        var workPlanAutoCreate = selectConvertJson("#G1302PlanAutoCreatespan");
	    jsondata["farmId"] = farmId;
	    jsondata["fieldGroupId"] = fieldGroupId;
	    jsondata["fieldGroupColor"] = color;
	    jsondata["field"] = field;
        jsondata["workPlanAutoCreate"] = workPlanAutoCreate;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitFieldGroup",
			type:'POST',
	        data:JSON.stringify(jsondata),								//入力用JSONデータ
			complete:function(data, status, jqXHR){						//処理成功時

			onProcing(false);
//		    if(fieldGroupId == 0){
//        	  displayToast('圃場グループ情報を登録しました。', 4000, 'rounded');		  //登録メッセージの表示
//		    }else{
//	          displayToast('圃場グループ情報を保存しました。', 4000, 'rounded');		  //保存メッセージの表示
//		    }
		    onBack()

			},
		    dataType:'json',
		    contentType:'text/json',
		    async: true
		});

	}

    /* 削除ボタン押下処理 */
	function onDelete() {

		var fieldGroupId = $("#G1302Title").attr("fieldGroupId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"fieldGroupId":"' + fieldGroupId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("圃場グループ情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteFieldGroup",
				type:'POST',
		        data:JSON.stringify(jsondata),									//入力用JSONデータ
				complete:function(data, status, jqXHR){							//処理成功時

				onProcing(false);
	//			displayToast('圃場グループ情報を削除しました。', 4000, 'rounded');		  //削除メッセージの表示
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
		var farmID	= $("#G1302Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/2/masterMntMove";

	}


})(jQuery); // end of jQuery name space