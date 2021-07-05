(function($){

	var hojoGroups = {
		"0" : { "typeId" : "0", "typeName" : "Ａ" }
	   ,"1" : { "typeId" : "1", "typeName" : "あ" }
	   ,"2" : { "typeId" : "2", "typeName" : "ア" }
	};

	$(function(){

		$(document).ready(function(){

			$("#inputFiled").show();
			$("#commitDisplay").hide();

			var htmlString	=	"";		/* 共通項目マークアップ */

			htmlString+= MakeSelectModal('G9999ModalGroups', '圃場グループタイプ', hojoGroups, 'typeId', 'typeName', 'G9999GroupsType', 'groupsType');
			$("#G9999SelectModal").html(htmlString);						//可変HTML部分に反映する

      $('.modal').modal();                        // 選択用モーダル画面初期化
			CalcInit();														      // 数値入力電卓初期化
			SelectModalInit();												  //選択用モーダルイベント初期化

			$("#G9999MailAddress").bind("change", changeMailAddress);
			$("#G9999GroupsTypeValue").bind("change", changeGroupsType);

			$("#G9999Submit").bind("click", onSubmit);
			$("#G9999Back").bind("click", onBack);

			onProcing(false);

		});

	});

	function changeMailAddress() {

		checkMailAddress();

	}
	function checkMailAddress() {
		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G9999MailAddress", "name" : "メールアドレス", "length" : "100", "json" : "mailAddress", "check" : { "required" : "1", "maxlength" : "1", "malladdress" : "1"}}
		];

		  /* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		var url = "/" + $("#G9999MailAddress").val() + "/" + "farmMakeCheckMailAddress";

		$.ajax({
			url:url,
			type:'GET',
			complete:function(data, status, jqXHR){						//処理成功時
				var jsonResult 	= JSON.parse( data.responseText );		//戻り値用JSONデータの生成

				if (jsonResult.result == -1) {

		        	displayToast('このメールアドレスは既に登録されています。', 4000, 'rounded');
		        	return false;
				}

				},
			dataType:'json',
			contentType:'text/json',
			async: false
		});

    	return true;

	}
	function changeGroupsType() {

		var type 			= $("#G9999GroupsTypeValue").val();
		var displayString 	= "";

		switch (type) {
		case "1":

			displayString 	= "あ";

			break;

		case "2":

			displayString 	= "ア";

			break;

		default:

			displayString 	= "Ａ";

			break;
		}

		$("#G9999TypeName").html(displayString);

	}
	function rangeCheck(item, data, min, max) {

		if (data < min || max < data) {
        	displayToast(item + 'は' + min + 'から' + max + 'の間で入力してください。', 4000, 'rounded');
        	return false;
		}

    	return true;

	}
	function onSubmit() {

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G9999MailAddress"		, "name" : "メールアドレス"		, "length" : "100"	, "json" : "mailAddress"	, "check" : { "required" : "1", "maxlength" : "1", "malladdress" : "1"}}
		   ,{ "id" : "G9999FarmName"		, "name" : "農場名"				, "length" : "255"	, "json" : "farmName"		, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G9999HojoGroups"		, "name" : "圃場グループ数"		, "length" : "3"	, "json" : "hojoGroups"		, "check" : { "required" : "1"}}
		   ,{ "id" : "G9999GroupsTypeValue"	, "name" : "グループ表記"		, "length" : "3"	, "json" : "groupsType"		, "check" : { "required" : "1"}}
		   ,{ "id" : "G9999Hojo"			, "name" : "圃場数"				, "length" : "3"	, "json" : "hojo"			, "check" : { "required" : "1"}}
		];

		  /* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		if (checkMailAddress() == false) {
			  return false;
		}

		if (rangeCheck("圃場グループ数", $("#G9999HojoGroups").val(), 1, 26) == false) {
			  return false;
		}
		if (rangeCheck("圃場数", $("#G9999Hojo").val(), 1, 20) == false) {
			  return false;
		}

    var crop = mSelectConvertJson("#G9999Cropspan");

    if (crop == "") {
      displayToast('品目を選択してください。', 4000, 'rounded');
      return false;
    }

		onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する

	    jsondata["crop"] = crop;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/farmMakeSubmit",
			type:'POST',
	        data:JSON.stringify(jsondata),								//入力用JSONデータ
			complete:function(data, status, jqXHR){						//処理成功時

				onProcing(false);

				$("#inputFiled").hide();
				$("#commitDisplay").show();

			},
		    dataType:'json',
		    contentType:'text/json',
		    async: true
		});

	}
	function onBack() {

		window.location = "/move";

	}

})(jQuery); // end of jQuery name space