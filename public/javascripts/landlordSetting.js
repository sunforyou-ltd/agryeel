/* AGRYEEL 地主情報設定画面 JQUERY */
(function($){

  var accountTypes = {
	"1" : { "typeId" : "1", "typeName" : "普通" }
   ,"2" : { "typeId" : "2", "typeName" : "定期" }
   ,"3" : { "typeId" : "3", "typeName" : "当座" }
  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    	  //$('.modal').modal();
    });

    $.ajax({
      url:"/landlordSettingInit", 										//地主情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var landlordId = jsonResult["landlordId"];						//地主ID取得
      var accountTypeID = 0;											//振込先口座種別ID
      var accountType = "";												//振込先口座種別

      //口座種別取得
      if(landlordId != 0){				//修正の場合
    	  if(jsonResult["accountType"] == 1){
    		  accountType = "普通";
    	  }else if(jsonResult["accountType"] == 2){
    		  accountType = "定期";
    	  }else if(jsonResult["accountType"] == 3){
    		  accountType = "当座";
    	  }else{
    		  accountType = "なし";
    	  }
    	  accountTypeID = jsonResult["accountType"];
      }else{
		  accountType = "なし";
	  }

      //選択用モーダルリストの生成
      //振込先口座種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G1201ModalTypes', '振込先口座種別', accountTypes, 'typeId', 'typeName', 'G1201AccountType', 'accountType');
      $("#G1201SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(landlordId == 0){				//登録の場合
    	  htmlString+= '<span class="white-text" id="G1201Title" farmId="' + jsonResult["farmId"] + '" landlordId="' + jsonResult["landlordId"] + '">地主情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G1201Title" farmId="' + jsonResult["farmId"] + '" landlordId="' + jsonResult["landlordId"] + '">地主情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row" id="G1201LandlordInfo">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 地主名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
          htmlString+= '<input id="G1201InLandlordName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1201InLandlordName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["landlordName"] + '">';
      }
      htmlString+= '<label for="G1201InLandlordName">地主名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //郵便番号
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
    	  htmlString+= '<input id="G1201InPost" type="text" class="validate input-text-color" maxlength="7" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G1201InPost" type="text" class="validate input-text-color" maxlength="7" style="ime-mode: disabled;" value="' + jsonResult["postNo"] + '">';
      }
      htmlString+= '<label for="G1201InPost">郵便番号を入力してください（ハイフン入力なし）</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 都道府県 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
          htmlString+= '<input id="G1201InPrefectures" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1201InPrefectures" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["prefectures"] + '">';
      }
      htmlString+= '<label for="G1201InPrefectures">都道府県を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 市町村など */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
          htmlString+= '<input id="G1201InAddress" type="text" class="validate input-text-color" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1201InAddress" type="text" class="validate input-text-color" style="ime-mode: active;" value="' + jsonResult["address"] + '">';
      }
      htmlString+= '<label for="G1201InAddress">市町村などを入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //電話番号
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
    	  htmlString+= '<input id="G1201InTel" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G1201InTel" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="' + jsonResult["tel"] + '">';
      }
      htmlString+= '<label for="G1201InTel">電話番号を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //責任者携帯電話番号
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
    	  htmlString+= '<input id="G1201InMobileTel" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G1201InMobileTel" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="' + jsonResult["responsibleMobileTel"] + '">';
      }
      htmlString+= '<label for="G1201InMobileTel">責任者携帯電話番号を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //ＦＡＸ
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
    	  htmlString+= '<input id="G1201InFax" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G1201InFax" type="text" class="validate input-text-color" maxlength="15" style="ime-mode: disabled;" value="' + jsonResult["fax"] + '">';
      }
      htmlString+= '<label for="G1201InFax">ＦＡＸを入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //メールアドレス（パソコン）
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
    	  htmlString+= '<input id="G1201InMailAddressPC" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G1201InMailAddressPC" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["mailAddressPC"] + '">';
      }
      htmlString+= '<label for="G1201InMailAddressPC">メールアドレス（パソコン）を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //メールアドレス（携帯）
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
    	  htmlString+= '<input id="G1201InMailAddressMobile" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G1201InMailAddressMobile" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: disabled;" value="' + jsonResult["mailAddressMobile"] + '">';
      }
      htmlString+= '<label for="G1201InMailAddressMobile">メールアドレス（携帯）を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 振込先銀行名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
          htmlString+= '<input id="G1201InBankName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1201InBankName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["bankName"] + '">';
      }
      htmlString+= '<label for="G1201InBankName">振込先銀行名を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 振込先口座種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '振込先口座種別<a href="#G1201ModalTypes"  class="collection-item modal-trigger select-modal"><span id="G1201AccountTypeSpan" class="blockquote-input">' + accountType + '</span></a>';
      htmlString+= '<input type="hidden" id="G1201AccountTypeValue" value="' + accountTypeID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //振込先口座番号
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
    	  htmlString+= '<input id="G1201InAccountNumber" type="text" class="validate input-text-color" maxlength="32" style="ime-mode: disabled;" value="">';
      }else{
    	  htmlString+= '<input id="G1201InAccountNumber" type="text" class="validate input-text-color" maxlength="32" style="ime-mode: disabled;" value="' + jsonResult["accountNumber"] + '">';
      }
      htmlString+= '<label for="G1201InAccountNumber">振込先口座番号を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 支払日 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(landlordId == 0){
          htmlString+= '<input id="G1201InPaymentDate" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1201InPaymentDate" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["paymentDate"] + '">';
      }
      htmlString+= '<label for="G1201InPaymentDate">支払日を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(landlordId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1201Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1201Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1201Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1201Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1201Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G1201LandlordSetting").html(htmlString);								//可変HTML部分に反映する

      $('.modal').modal();                        								// 選択用モーダル画面初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化
	  $("input").prop("autocomplete", "off");									//オートコンプリート無効化

      $("#G1201Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G1201Delete").bind('click', onDelete );     							//削除ボタン
      $("#G1201Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G1201Title").attr("farmId");
		var landlordId = $("#G1201Title").attr("landlordId");
		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G1201InLandlordName"		, "name" : "地主名"						, "length" : "128"	, "json" : "landlordName"			, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1201InPost"				, "name" : "郵便番号"					, "length" : "7"	, "json" : "postNo"					, "check" : { "number" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1201InPrefectures"		, "name" : "都道府県"		    		, "length" : "32"	, "json" : "prefectures"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1201InAddress"			, "name" : "市町村など"		    		, "length" : "32"	, "json" : "address"				, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1201InTel"				, "name" : "電話番号"					, "length" : "15"	, "json" : "tel"					, "check" : { "telfax" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1201InMobileTel"			, "name" : "責任者携帯電話番号"			, "length" : "15"	, "json" : "responsibleMobileTel"	, "check" : { "telfax" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1201InFax"				, "name" : "ＦＡＸ"						, "length" : "15"	, "json" : "fax"					, "check" : { "telfax" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1201InMailAddressPC"		, "name" : "メールアドレス（パソコン）"	, "length" : "128"	, "json" : "mailAddressPC"			, "check" : { "malladdress" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1201InMailAddressMobile"	, "name" : "メールアドレス（携帯）"		, "length" : "128"	, "json" : "mailAddressMobile"		, "check" : { "malladdress" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1201InBankName"			, "name" : "振込先銀行名"		    	, "length" : "128"	, "json" : "bankName"				, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1201AccountTypeValue"	, "name" : "振込先口座種別"				, "length" : "16"	, "json" : "accountType"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1201InAccountNumber"		, "name" : "振込先口座番号"				, "length" : "15"	, "json" : "accountNumber"			, "check" : { "number" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1201InPaymentDate"		, "name" : "支払日"		    			, "length" : "128"	, "json" : "paymentDate"			, "check" : { "maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["landlordId"] = landlordId;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitLandlord",
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

		var landlordId = $("#G1201Title").attr("landlordId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"landlordId":"' + landlordId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("地主情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteLandlord",
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
		var farmID	= $("#G1201Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/1/masterMntMove";

	}


})(jQuery); // end of jQuery name space