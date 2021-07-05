/* AGRYEEL 区画情報設定画面 JQUERY */
(function($){

  var soilQualityList = {
	"1" : { "typeId" : "1", "typeName" : "粘土" }
   ,"2" : { "typeId" : "2", "typeName" : "シルト" }
   ,"3" : { "typeId" : "3", "typeName" : "砂" }
   ,"4" : { "typeId" : "4", "typeName" : "礫" }
  };
  var kansuiMethodList = {
			"1" : { "typeId" : "1", "typeName" : "手動" }
		   ,"2" : { "typeId" : "2", "typeName" : "半自動" }
		   ,"3" : { "typeId" : "3", "typeName" : "自動" }
       ,"4" : { "typeId" : "4", "typeName" : "自動（頭上）" }
       ,"5" : { "typeId" : "5", "typeName" : "自動（サイド）" }
       ,"6" : { "typeId" : "6", "typeName" : "自動（地べた）" }
       ,"7" : { "typeId" : "7", "typeName" : "自動（地べた＋サイド）" }
		  };
  var kukakuKindList = {
			"1" : { "typeId" : "1", "typeName" : "露地" }
		   ,"2" : { "typeId" : "2", "typeName" : "ハウス" }
		  };

  /* 初期処理時イベント */
  $(function(){

    $(document).ready(function(){

    });

    $.ajax({
      url:"/compartmentSettingInit", 									//区画情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var kukakuId = jsonResult["kukakuId"];							//区画ID取得
      var soilQualityID = 0;											//土質ID
      var soilQuality = "";												//土質種別
      var kansuiMethodID = 0;											//潅水方法ID
      var kansuiMethod = "";											//潅水方法
      var kukakuKindID = 0;												//区画種別ID
      var kukakuKind = "";												//区画種別

      //土質取得
      if(kukakuId != 0){				//修正の場合
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

      //潅水方法取得
      if(kukakuId != 0){				//修正の場合
    	  if(jsonResult["kansuiMethod"] == 1){
    		  kansuiMethod = "手動";
    	  }else if(jsonResult["kansuiMethod"] == 2){
    		  kansuiMethod = "半自動";
    	  }else if(jsonResult["kansuiMethod"] == 3){
    		  kansuiMethod = "自動";
    	  }else{
    		  kansuiMethod = "なし";
    	  }
    	  kansuiMethodID = jsonResult["kansuiMethod"];
      }else{
    	  kansuiMethod = "なし";
	  }

      //区画種別取得
      if(kukakuId != 0){				//修正の場合
    	  if(jsonResult["kukakuKind"] == 1){
    		  kukakuKind = "露地";
    	  }else if(jsonResult["kukakuKind"] == 2){
    		  kukakuKind = "ハウス";
    	  }else{
    		  kukakuKind = "なし";
    	  }
    	  kukakuKindID = jsonResult["kukakuKind"];
      }else{
    	  kukakuKind = "なし";
	  }

      //選択用モーダルリストの生成
      //土質モーダルリストの作成
	  htmlString+= MakeSelectModal('G1501ModalSoilQuality', '土質', soilQualityList, 'typeId', 'typeName', 'G1501SoilQuality', 'soilQuality');
      //潅水方法モーダルリストの作成
	  htmlString+= MakeSelectModal('G1501ModalKansuiMethod', '潅水方法', kansuiMethodList, 'typeId', 'typeName', 'G1501KansuiMethod', 'kansuiMethod');
      //区画種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G1501ModalKukakuKind', '区画種別', kukakuKindList, 'typeId', 'typeName', 'G1501KukakuKind', 'kukakuKind');
      $("#G1501SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(kukakuId == 0){				//登録の場合
    	  htmlString+= '<span class="white-text" id="G1501Title" farmId="' + jsonResult["farmId"] + '" fieldId="0" kukakuId="' + jsonResult["kukakuId"] + '">区画情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G1501Title" farmId="' + jsonResult["farmId"] + '" fieldId="' + jsonResult["fieldId"] + '" kukakuId="' + jsonResult["kukakuId"] + '">区画情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row" id="G1501CompartmentInfo">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 圃場名 */
      if(kukakuId != 0){
		  htmlString+= '<div class="row">';
	      htmlString+= '<div class="input-field col s10 offset-s1 mst-item-title">';
	      htmlString+= '所属圃場：<span class="mst-item" style="">' + jsonResult["fieldName"] + '</span>';
	      htmlString+= '</div>';
	      htmlString+= '</div>';
      }

      /* 区画名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(kukakuId == 0){
          htmlString+= '<input id="G1501InKukakuName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1501InKukakuName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["kukakuName"] + '">';
      }
      htmlString+= '<label for="G1501InKukakuName">区画名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //面積
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
    	  htmlString+= '面積<span id="G1501AreaSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">' + jsonResult["areaUnit"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Area">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Area" value="0">';
      }else{
    	  htmlString+= '面積<span id="G1501AreaSpan" class="blockquote-input">' + jsonResult["area"] + '</span><span class="blockquote-inputOption">' + jsonResult["areaUnit"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Area">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Area" value="' + jsonResult["area"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 並び順 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(kukakuId == 0){
          htmlString+= '<input id="G1501InKukakuSequenceId" type="text" class="validate input-text-color" maxlength="3" style="ime-mode: active;" value="0">';
      }else{
          htmlString+= '<input id="G1501InKukakuSequenceId" type="text" class="validate input-text-color" maxlength="3" style="ime-mode: active;" value="' + jsonResult["sequenceId"] + '">';
      }
      htmlString+= '<label for="G1501InKukakuSequenceId">並び順を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '<i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501InKukakuSequenceId">keyboard</i>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 土質 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '土質<a href="#G1501ModalSoilQuality"  class="collection-item modal-trigger select-modal"><span id="G1501SoilQualitySpan" class="blockquote-input">' + soilQuality + '</span></a>';
      htmlString+= '<input type="hidden" id="G1501SoilQualityValue" value="' + soilQualityID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //間口
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '間口<span id="G1501FrontageSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">ｍ</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Frontage">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Frontage" value="0">';
      }else{
          htmlString+= '間口<span id="G1501FrontageSpan" class="blockquote-input">' + jsonResult["frontage"] + '</span><span class="blockquote-inputOption">ｍ</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Frontage">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Frontage" value="' + jsonResult["frontage"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //奥行
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '奥行<span id="G1501DepthSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">ｍ</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Depth">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Depth" value="0">';
      }else{
          htmlString+= '奥行<span id="G1501DepthSpan" class="blockquote-input">' + jsonResult["depth"] + '</span><span class="blockquote-inputOption">ｍ</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Depth">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Depth" value="' + jsonResult["depth"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 潅水方法 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '潅水方法<a href="#G1501ModalKansuiMethod"  class="collection-item modal-trigger select-modal"><span id="G1501KansuiMethodSpan" class="blockquote-input">' + kansuiMethod + '</span></a>';
      htmlString+= '<input type="hidden" id="G1501KansuiMethodValue" value="' + kansuiMethodID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //潅水量
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '潅水量<span id="G1501KansuiRyoSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">Ｌ</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501KansuiRyo">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501KansuiRyo" value="0">';
      }else{
          htmlString+= '潅水量<span id="G1501KansuiRyoSpan" class="blockquote-input">' + jsonResult["kansuiRyo"] + '</span><span class="blockquote-inputOption">Ｌ</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501KansuiRyo">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501KansuiRyo" value="' + jsonResult["kansuiRyo"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //潅水時間
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '潅水時間<span id="G1501KansuiTimeSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">分</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501KansuiTime">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501KansuiTime" value="0">';
      }else{
          htmlString+= '潅水時間<span id="G1501KansuiTimeSpan" class="blockquote-input">' + jsonResult["kansuiTime"] + '</span><span class="blockquote-inputOption">分</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501KansuiTime">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501KansuiTime" value="' + jsonResult["kansuiTime"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //潅水順番
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '潅水順番<span id="G1501KansuiOrderSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">番目</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501KansuiOrder">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501KansuiOrder" value="0">';
      }else{
          htmlString+= '潅水順番<span id="G1501KansuiOrderSpan" class="blockquote-input">' + jsonResult["kansuiOrder"] + '</span><span class="blockquote-inputOption">番目</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501KansuiOrder">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501KansuiOrder" value="' + jsonResult["kansuiOrder"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 区画種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '区画種別<a href="#G1501ModalKukakuKind"  class="collection-item modal-trigger select-modal"><span id="G1501KukakuKindSpan" class="blockquote-input">' + kukakuKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G1501KukakuKindValue" value="' + kukakuKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* ハウス名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(kukakuId == 0){
          htmlString+= '<input id="G1501InHouseName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1501InHouseName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["houseName"] + '">';
      }
      htmlString+= '<label for="G1501InHouseName">ハウス名を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //金額
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '金額<span id="G1501KingakuSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Kingaku" value="0">';
      }else{
          htmlString+= '金額<span id="G1501KingakuSpan" class="blockquote-input">' + jsonResult["kingaku"] + '</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Kingaku" value="' + jsonResult["kingaku"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //購入日の生成
      var purchaseday = jsonResult["purchaseDate"];
	    htmlString+= '<div class="row">';
      htmlString += '<div class="input-field col s10 offset-s1 grey-text">';
      if(!purchaseday){
          htmlString += '<input type="text" placeholder="購入日" id="G1501PurchaseDate" class="datepicker input-text-color" style="" value="">';
      }else{
          htmlString += '<input type="text" placeholder="購入日" id="G1501PurchaseDate" class="datepicker input-text-color" style="" value="' + purchaseday.replace("-", "/").replace("-", "/") + '">';
      }
      htmlString+= '<label for="G1501PurchaseDate">購入日を入力してください</label>';
      htmlString += '</div>';
      htmlString+= '</div>';

      //耐用年数
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '耐用年数<span id="G1501ServiceLifeSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">年</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501ServiceLife">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501ServiceLife" value="0">';
      }else{
          htmlString+= '耐用年数<span id="G1501ServiceLifeSpan" class="blockquote-input">' + jsonResult["serviceLife"] + '</span><span class="blockquote-inputOption">年</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501ServiceLife">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501ServiceLife" value="' + jsonResult["serviceLife"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //緯度
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '緯度<span id="G1501LatSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Lat">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Lat" value="0">';
      }else{
          htmlString+= '緯度<span id="G1501LatSpan" class="blockquote-input">' + jsonResult["lat"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Lat">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Lat" value="' + jsonResult["lat"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //経度
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(kukakuId == 0){
          htmlString+= '経度<span id="G1501LngSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Lng">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Lng" value="0">';
      }else{
          htmlString+= '経度<span id="G1501LngSpan" class="blockquote-input">' + jsonResult["lng"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1501Lng">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1501Lng" value="' + jsonResult["lng"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(kukakuId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1501Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1501Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1501Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1501Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1501Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G1501CompartmentSetting").html(htmlString);							//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  CalcInit();														        // 数値入力電卓初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化

      $("#G1501Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G1501Delete").bind('click', onDelete );     							//削除ボタン
      $("#G1501Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G1501Title").attr("farmId");
		var fieldId = $("#G1501Title").attr("fieldId");
		var kukakuId = $("#G1501Title").attr("kukakuId");
		var purchaseday =  $("#G1501PurchaseDate").val();

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G1501InKukakuName"		, "name" : "区画名"						, "length" : "128"	, "json" : "kukakuName"				, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1501Area"				        , "name" : "面積"						, "length" : "32"	, "json" : "area"					, "check" : { "number" : "1","maxlength" : "1"}}
       ,{ "id" : "G1501InKukakuSequenceId"  , "name" : "並び順"       , "length" : "3"  , "json" : "sequenceId"       , "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1501SoilQualityValue"	, "name" : "土質"						, "length" : "16"	, "json" : "soilQuality"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1501Frontage"			, "name" : "間口"						, "length" : "32"	, "json" : "frontage"				, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1501Depth"				, "name" : "奥行"						, "length" : "32"	, "json" : "depth"					, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1501KansuiMethodValue"	, "name" : "潅水方法"					, "length" : "16"	, "json" : "kansuiMethod"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1501KansuiRyo"			, "name" : "潅水量"						, "length" : "32"	, "json" : "kansuiRyo"				, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1501KansuiTime"			, "name" : "潅水時間"					, "length" : "16"	, "json" : "kansuiTime"				, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1501KansuiOrder"			, "name" : "潅水順番"					, "length" : "3"	, "json" : "kansuiOrder"			, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1501KukakuKindValue"		, "name" : "区画種別"					, "length" : "16"	, "json" : "kukakuKind"				, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1501InHouseName"			, "name" : "ハウス名"					, "length" : "128"	, "json" : "houseName"				, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1501Kingaku"				, "name" : "金額"						, "length" : "32"	, "json" : "kingaku"				, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1501ServiceLife"			, "name" : "耐用年数"					, "length" : "2"	, "json" : "serviceLife"			, "check" : { "number" : "1","maxlength" : "1"}}
       ,{ "id" : "G1501Lat"           , "name" : "緯度"               , "length" : "32" , "json" : "lat"        , "check" : { "number" : "1","maxlength" : "1"}}
       ,{ "id" : "G1501Lng"           , "name" : "経度"               , "length" : "32" , "json" : "lng"        , "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["fieldId"] = fieldId;
	    jsondata["kukakuId"] = kukakuId;
	    jsondata["purchaseDate"] = purchaseday;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitCompartment",
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

		var kukakuId = $("#G1501Title").attr("kukakuId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"kukakuId":"' + kukakuId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("区画情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteCompartment",
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
		var farmID	= $("#G1501Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/4/masterMntMove";

	}


})(jQuery); // end of jQuery name space