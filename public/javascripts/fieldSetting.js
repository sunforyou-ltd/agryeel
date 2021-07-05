/* AGRYEEL 圃場情報設定画面 JQUERY */
(function($){

  var geographyList = {
	"1" : { "typeId" : "1", "typeName" : "畑" }
   ,"2" : { "typeId" : "2", "typeName" : "田んぼ" }
   ,"3" : { "typeId" : "3", "typeName" : "宅地" }
  };

  var soilQualityList = {
	"1" : { "typeId" : "1", "typeName" : "粘土" }
   ,"2" : { "typeId" : "2", "typeName" : "シルト" }
   ,"3" : { "typeId" : "3", "typeName" : "砂" }
   ,"4" : { "typeId" : "4", "typeName" : "礫" }
  };

  var contractTypeList = {
	"1" : { "typeId" : "1", "typeName" : "年" }
   ,"2" : { "typeId" : "2", "typeName" : "半年" }
   ,"3" : { "typeId" : "3", "typeName" : "月" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    	  $('.selectmodal-trigger').unbind('click');
    	  $('.selectmodal-trigger').bind('click', selectOpen);
    	  $('.mselectmodal-trigger').unbind('click');
    	  $('.mselectmodal-trigger').bind('click', mSelectOpen);
    });

    $.ajax({
      url:"/fieldSettingInit", 											//圃場情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var fieldId = jsonResult["fieldId"];								//圃場ID取得
      var geographyID = 0;												//地目ID
      var geography = "";												//地目種別
      var soilQualityID = 0;											//土質ID
      var soilQuality = "";												//土質種別
      var contractTypeID = 0;											//契約形態ID
      var contractType = "";											//契約形態

      //地目取得
      if(fieldId != 0){				//修正の場合
    	  if(jsonResult["geography"] == 1){
    		  geography = "畑";
    	  }else if(jsonResult["geography"] == 2){
    		  geography = "田んぼ";
    	  }else if(jsonResult["geography"] == 3){
    		  geography = "宅地";
    	  }else{
    		  geography = "なし";
    	  }
    	  geographyID = jsonResult["geography"];
      }else{
    	  geography = "なし";
	  }

      //土質取得
      if(fieldId != 0){				//修正の場合
    	  if(jsonResult["soilQuality"] == 1){
    		  soilQuality = "粘土";
    	  }else if(jsonResult["soilQuality"] == 2){
    		  soilQuality = "シルト";
    	  }else if(jsonResult["soilQuality"] == 3){
    		  soilQuality = "砂";
    	  }else if(jsonResult["soilQuality"] == 4){
    		  soilQuality = "礫";
    	  }else{
    		  soilQuality = "なし";
    	  }
    	  soilQualityID = jsonResult["soilQuality"];
      }else{
    	  soilQuality = "なし";
	  }

      //契約形態取得
      if(fieldId != 0){				//修正の場合
    	  if(jsonResult["contractType"] == 1){
    		  contractType = "年";
    	  }else if(jsonResult["contractType"] == 2){
    		  contractType = "半年";
    	  }else if(jsonResult["contractType"] == 3){
    		  contractType = "月";
    	  }else{
    		  contractType = "なし";
    	  }
    	  contractTypeID = jsonResult["contractType"];
      }else{
    	  contractType = "なし";
	  }

      /* 所属区画文字列生成 */
      var spanMsg = "";
      var msgCnt  = 0;
      var kukakuList = jsonResult["kukakuDataList"]; 						//jSONデータより生産物リストを取得
      if(fieldId != 0){
    	  //修正の場合
	      for ( var kukakuKey in kukakuList ) {									//所属区画件数分処理を行う

	          var kukakuData = kukakuList[kukakuKey];

	          if (msgCnt > 1) {
	              spanMsg += "，．．．";
	              break;
	          }
	          else if (msgCnt == 1) {
	              spanMsg += "，";
	          }
	          spanMsg += kukakuData["kukakuName"];
	          msgCnt++;
	      }
      }

      //選択用モーダルリストの生成
      //地目モーダルリストの作成
	  htmlString+= MakeSelectModal('G1401ModalGeography', '地目', geographyList, 'typeId', 'typeName', 'G1401Geography', 'geography');
      //土質モーダルリストの作成
	  htmlString+= MakeSelectModal('G1401ModalSoilQuality', '土質', soilQualityList, 'typeId', 'typeName', 'G1401SoilQuality', 'soilQuality');
      //契約形態モーダルリストの作成
	  htmlString+= MakeSelectModal('G1401ModalContractType', '契約形態', contractTypeList, 'typeId', 'typeName', 'G1401ContractType', 'contractType');
      $("#G1401SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(fieldId == 0){				//登録の場合
    	  htmlString+= '<span class="white-text" id="G1401Title" farmId="' + jsonResult["farmId"] + '" fieldId="' + jsonResult["fieldId"] + '">圃場情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G1401Title" farmId="' + jsonResult["farmId"] + '" fieldId="' + jsonResult["fieldId"] + '">圃場情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row" id="G1401FieldInfo">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 圃場名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(fieldId == 0){
          htmlString+= '<input id="G1401InFieldName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1401InFieldName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["fieldName"] + '">';
      }
      htmlString+= '<label for="G1401InFieldName">圃場名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 地主 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '地主<a href="#selectmodal"  class="selectmodal-trigger" title="地主一覧" data="' + jsonResult["farmId"] + '/' + jsonResult["landlordId"] + '/getLandlord" displayspan="#G1401Landlordspan"><span id="G1401Landlordspan" class="blockquote-input">' + jsonResult["landlordName"] + '</span></a>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //郵便番号
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(fieldId == 0){
    	  htmlString+= '<input id="G1401InPost" type="text" class="validate input-text-color" maxlength="7" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G1401InPost" type="text" class="validate input-text-color" maxlength="7" style="ime-mode: disabled;" value="' + jsonResult["postNo"] + '">';
      }
      htmlString+= '<label for="G1401InPost">郵便番号を入力してください（ハイフン入力なし）</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 都道府県 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(fieldId == 0){
          htmlString+= '<input id="G1401InPrefectures" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1401InPrefectures" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["prefectures"] + '">';
      }
      htmlString+= '<label for="G1401InPrefectures">都道府県を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 市町村など */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(fieldId == 0){
          htmlString+= '<input id="G1401InAddress" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1401InAddress" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["address"] + '">';
      }
      htmlString+= '<label for="G1401InAddress">市町村などを入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 地目 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '地目<a href="#G1401ModalGeography"  class="collection-item modal-trigger select-modal"><span id="G1401GeographySpan" class="blockquote-input">' + geography + '</span></a>';
      htmlString+= '<input type="hidden" id="G1401GeographyValue" value="' + geographyID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //面積
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(fieldId == 0){
    	  htmlString+= '面積<span id="G1401AreaSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">' + jsonResult["areaUnit"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1401Area">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1401Area" value="0">';
      }else{
    	  htmlString+= '面積<span id="G1401AreaSpan" class="blockquote-input">' + jsonResult["area"] + '</span><span class="blockquote-inputOption">' + jsonResult["areaUnit"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1401Area">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1401Area" value="' + jsonResult["area"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 土質 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '土質<a href="#G1401ModalSoilQuality"  class="collection-item modal-trigger select-modal"><span id="G1401SoilQualitySpan" class="blockquote-input">' + soilQuality + '</span></a>';
      htmlString+= '<input type="hidden" id="G1401SoilQualityValue" value="' + soilQualityID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //契約日の生成
      var contractDate = jsonResult["contractDate"];
	  htmlString+= '<div class="row">';
      htmlString += '<div class="input-field col s10 offset-s1 grey-text">';
      if(!contractDate){
    	  htmlString += '<input type="text" placeholder="契約日" id="G1401ContractDate" class="datepicker input-text-color" style="" value="">';
      }else{
          htmlString += '<input type="text" placeholder="契約日" id="G1401ContractDate" class="datepicker input-text-color" style="" value="' + contractDate.replace("-", "/").replace("-", "/") + '">';
      }
      htmlString+= '<label for="G1401ContractDate">契約日を入力してください</label>';
      htmlString += '</div>';
      htmlString+= '</div>';

      //契約終了日の生成
      var contractEndDate = jsonResult["contractEndDate"];
	  htmlString+= '<div class="row">';
      htmlString += '<div class="input-field col s10 offset-s1 grey-text">';
      if(!contractDate){
    	  htmlString += '<input type="text" placeholder="契約終了日" id="G1401ContractEndDate" class="datepicker input-text-color" style="" value="">';
      }else{
          htmlString += '<input type="text" placeholder="契約終了日" id="G1401ContractEndDate" class="datepicker input-text-color" style="" value="' + contractEndDate.replace("-", "/").replace("-", "/") + '">';
      }
      htmlString+= '<label for="G1401ContractEndDate">契約終了日を入力してください</label>';
      htmlString += '</div>';
      htmlString+= '</div>';

      /* 契約形態 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '契約形態<a href="#G1401ModalContractType"  class="collection-item modal-trigger select-modal"><span id="G1401ContractTypeSpan" class="blockquote-input">' + contractType + '</span></a>';
      htmlString+= '<input type="hidden" id="G1401ContractTypeValue" value="' + contractTypeID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //賃借料
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(fieldId == 0){
          htmlString+= '賃借料<span id="G1401RentSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1401Rent">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1401Rent" value="0">';
      }else{
          htmlString+= '賃借料<span id="G1401RentSpan" class="blockquote-input">' + jsonResult["rent"] + '</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1401Rent">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1401Rent" value="' + jsonResult["rent"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      if(fieldId != 0){
    	  /* 所属区画 */
	      htmlString+= '<div class="row">';
	      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
	      htmlString+= '所属区画<span style="color:red">（*）</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="区画一覧" data="' + jsonResult["farmId"] + '/' + jsonResult["fieldId"] + '/getCompartmentOfFieldSel" displayspan="#G1401Compartmentspan"><span id="G1401Compartmentspan" class="blockquote-input">' + spanMsg + '</span></a>';
	      htmlString+= '</div>';
	      htmlString+= '</div>';
      }

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(fieldId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1401Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1401Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1401Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1401Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1401Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G1401FieldSetting").html(htmlString);									//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  CalcInit();														        // 数値入力電卓初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化
	  selectDataSet("#G1401Landlordspan", jsonResult["farmId"] + '/' + jsonResult["landlordId"] + '/getLandlord');	//セレクトモーダル初期処理
      if(fieldId != 0){
    	  mSelectDataSet("#G1401Compartmentspan", jsonResult["farmId"] + '/' + jsonResult["fieldId"] + '/getCompartmentOfFieldSel');		//マルチセレクトモーダル初期処理
      }

      $("#G1401Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G1401Delete").bind('click', onDelete );     							//削除ボタン
      $("#G1401Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G1401Title").attr("farmId");
		var fieldId = $("#G1401Title").attr("fieldId");
		var contractDate =  $("#G1401ContractDate").val();
		var contractEndDate =  $("#G1401ContractEndDate").val();

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G1401InFieldName"			, "name" : "圃場名"						, "length" : "128"	, "json" : "fieldName"				, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1401InPost"				, "name" : "郵便番号"					, "length" : "7"	, "json" : "postNo"					, "check" : { "number" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1401InPrefectures"		, "name" : "都道府県"		    		, "length" : "32"	, "json" : "prefectures"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1401InAddress"			, "name" : "市町村など"		    		, "length" : "32"	, "json" : "address"				, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1401GeographyValue"		, "name" : "地目"						, "length" : "16"	, "json" : "geography"				, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1401Area"				, "name" : "面積"						, "length" : "32"	, "json" : "area"					, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1401SoilQualityValue"	, "name" : "土質"						, "length" : "16"	, "json" : "soilQuality"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1401ContractTypeValue"	, "name" : "契約形態"					, "length" : "16"	, "json" : "contractType"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1401Rent"				, "name" : "賃借料"						, "length" : "32"	, "json" : "rent"					, "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 地主IDの取得 */
        var landlordId = selectConvertJson("#G1401Landlordspan");

		/* 所属区画のチェック */
        var compartment = "";
        if(fieldId != 0){
            compartment = mSelectConvertJson("#G1401Compartmentspan");
            if (compartment == "") {
              displayToast('区画を選択してください。', 4000, 'rounded');
              return false;
            }
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["fieldId"] = fieldId;
	    jsondata["contractDate"] = contractDate;
	    jsondata["contractEndDate"] = contractEndDate;
	    if(landlordId == ""){
		    jsondata["landlordId"] = 0;
	    }else{
		    jsondata["landlordId"] = landlordId;
	    }
        if(fieldId != 0){
        	jsondata["compartment"] = compartment;
        }

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitField",
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

		var farmId = $("#G1401Title").attr("farmId");
		var fieldId = $("#G1401Title").attr("fieldId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"fieldId":"' + fieldId + '",';
	    result += '"farmId":"' + farmId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("圃場情報を削除します。所属区画も全て削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteField",
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
		var farmID	= $("#G1401Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/3/masterMntMove";

	}


})(jQuery); // end of jQuery name space