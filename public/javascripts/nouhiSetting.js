/* AGRYEEL 区画情報設定画面 JQUERY */
(function($){

  var nouhiKindList = {
	"0" : { "typeId" : "0", "typeName" : "なし" }
   ,"1" : { "typeId" : "1", "typeName" : "農薬" }
   ,"2" : { "typeId" : "2", "typeName" : "肥料" }
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
      url:"/nouhiSettingInit", 											//農肥情報設定初期処理
      type:'GET',
      complete:function(data, status, jqXHR){							//処理成功時
      var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
      var htmlString	= "";											//可変HTML文字列
      var nouhiId = jsonResult["nouhiId"];								//農肥ID取得
      var nouhiKindID = 0;												//農肥種別ID
      var nouhiKind = "";												//農肥種別
      var unitKindID = 0;												//単位種別ID
      var unitKind = "";												//単位種別

      //農肥種別取得
      if(nouhiId != 0){				//修正の場合
    	  if(jsonResult["nouhiKind"] == 0){
    		  nouhiKind = "なし";
    	  }else if(jsonResult["nouhiKind"] == 1){
    		  nouhiKind = "農薬";
    	  }else if(jsonResult["nouhiKind"] == 2){
    		  nouhiKind = "肥料";
    	  }else{
    		  nouhiKind = "なし";
    	  }
    	  nouhiKindID = jsonResult["nouhiKind"];
      }else{
    	  nouhiKind = "なし";
	  }

      //単位種別取得
      if(nouhiId != 0){				//修正の場合
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
      //農肥種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G1801ModalNouhiKind', '農肥種別', nouhiKindList, 'typeId', 'typeName', 'G1801NouhiKind', 'nouhiKind');
      //単位種別モーダルリストの作成
	  htmlString+= MakeSelectModal('G1801ModalUnitKind', '単位種別', unitKindList, 'typeId', 'typeName', 'G1801UnitKind', 'unitKind');
      $("#G1801SelectModal").html(htmlString);							//可変HTML部分に反映する

      htmlString = '<div class="card mst-panel" style="">';
      htmlString+= '<div class="card-panel fixed-color" style="">';
      /* タイトル */
      if(nouhiId == 0){					//登録の場合
    	  htmlString+= '<span class="white-text" id="G1801Title" farmId="' + jsonResult["farmId"] + '" nouhiId="' + jsonResult["nouhiId"] + '">農肥情報登録</span>';
      }else{							//修正の場合
          htmlString+= '<span class="white-text" id="G1801Title" farmId="' + jsonResult["farmId"] + '" nouhiId="' + jsonResult["nouhiId"] + '">農肥情報設定</span>';
      }
	  htmlString+= '</div>';

      htmlString+= '<div class="row" id="G1801NouhiInfo">';
      htmlString+= '<div class="card-action-center">';
      htmlString+= '<form>';

      /* 農肥名 */
	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(nouhiId == 0){
          htmlString+= '<input id="G1801InNouhiName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active; color: #eeeeee;" value="">';
      }else{
          htmlString+= '<input id="G1801InNouhiName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active; color: #eeeeee;" value="' + jsonResult["nouhiName"] + '">';
      }
      htmlString+= '<label for="G1801InNouhiName">農肥名を入力してください<span style="color:red">（*）</span></label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 農肥正式名 */
  	  htmlString+= '<div class="row">';
      htmlString+= '<div class="input-field col s10 offset-s1">';
      if(nouhiId == 0){
          htmlString+= '<input id="G1801InNouhiOfficialName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="">';
      }else{
          htmlString+= '<input id="G1801InNouhiOfficialName" type="text" class="validate input-text-color" maxlength="128" style="ime-mode: active;" value="' + jsonResult["nouhiOfficialName"] + '">';
      }
      htmlString+= '<label for="G1801InNouhiOfficialName">農肥正式名を入力してください</label>';
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 農肥種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '農肥種別<span style="color:red">（*）</span><a href="#G1801ModalNouhiKind"  class="collection-item modal-trigger select-modal"><span id="G1801NouhiKindSpan" class="blockquote-input">' + nouhiKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G1801NouhiKindValue" value="' + nouhiKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //倍率
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= '倍率<span id="G1801BairituSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Bairitu">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Bairitu" value="0">';
      }else{
    	  htmlString+= '倍率<span id="G1801BairituSpan" class="blockquote-input">' + jsonResult["bairitu"] + '</span><span class="blockquote-inputOption">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Bairitu">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Bairitu" value="' + jsonResult["bairitu"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //散布量
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= '散布量<span id="G1801SanpuryoSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Sanpuryo">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Sanpuryo" value="0">';
      }else{
    	  htmlString+= '散布量<span id="G1801SanpuryoSpan" class="blockquote-input">' + jsonResult["sanpuryo"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Sanpuryo">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Sanpuryo" value="' + jsonResult["sanpuryo"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      /* 単位種別 */
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      htmlString+= '単位<a href="#G1801ModalUnitKind"  class="collection-item modal-trigger select-modal"><span id="G1801UnitKindSpan" class="blockquote-input">' + unitKind + '</span></a>';
      htmlString+= '<input type="hidden" id="G1801UnitKindValue" value="' + unitKindID + '">';
      htmlString+= '</div>';
      htmlString+= '</div>';

      //N
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= 'N<span id="G1801NSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801N">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801N" value="0">';
      }else{
    	  htmlString+= 'N<span id="G1801NSpan" class="blockquote-input">' + jsonResult["n"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801N">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801N" value="' + jsonResult["n"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //P
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= 'P<span id="G1801PSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801P">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801P" value="0">';
      }else{
    	  htmlString+= 'P<span id="G1801PSpan" class="blockquote-input">' + jsonResult["p"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801P">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801P" value="' + jsonResult["p"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //K
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= 'K<span id="G1801KSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801K">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801K" value="0">';
      }else{
    	  htmlString+= 'K<span id="G1801KSpan" class="blockquote-input">' + jsonResult["k"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801K">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801K" value="' + jsonResult["k"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //Mg
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
        htmlString+= 'Mg<span id="G1801MGSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801MG">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801MG" value="0">';
      }else{
        htmlString+= 'Mg<span id="G1801MGSpan" class="blockquote-input">' + jsonResult["mg"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801MG">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801MG" value="' + jsonResult["mg"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //倍率下限
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= '倍率下限<span id="G1801LowerSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Lower">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Lower" value="0">';
      }else{
    	  htmlString+= '倍率下限<span id="G1801LowerSpan" class="blockquote-input">' + jsonResult["lower"] + '</span><span class="blockquote-inputOption">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801G1801Lower">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Lower" value="' + jsonResult["lower"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //倍率上限
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= '倍率上限<span id="G1801UpperSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Upper">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Upper" value="0">';
      }else{
    	  htmlString+= '倍率上限<span id="G1801UpperSpan" class="blockquote-input">' + jsonResult["upper"] + '</span><span class="blockquote-inputOption">倍</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Upper">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Upper" value="' + jsonResult["upper"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //最終経過日数
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= '最終経過日数<span id="G1801FinalDaySpan" class="blockquote-input">0</span><span class="blockquote-inputOption">日前</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801FinalDay">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801FinalDay" value="0">';
      }else{
    	  htmlString+= '最終経過日数<span id="G1801FinalDaySpan" class="blockquote-input">' + jsonResult["finalDay"] + '</span><span class="blockquote-inputOption">日前</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801FinalDay">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801FinalDay" value="' + jsonResult["finalDay"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //散布回数
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= '散布回数<span id="G1801SanpuCountSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">回</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801SanpuCount">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801SanpuCount" value="0">';
      }else{
    	  htmlString+= '散布回数<span id="G1801SanpuCountSpan" class="blockquote-input">' + jsonResult["sanpuCount"] + '</span><span class="blockquote-inputOption">回</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801SanpuCount">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801SanpuCount" value="' + jsonResult["sanpuCount"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //使用時期
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= '使用時期<span id="G1801UseWhenSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">日後</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801UseWhen">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801UseWhen" value="0">';
      }else{
    	  htmlString+= '使用時期<span id="G1801UseWhenSpan" class="blockquote-input">' + jsonResult["useWhen"] + '</span><span class="blockquote-inputOption">日後</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801UseWhen">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801UseWhen" value="' + jsonResult["useWhen"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //金額
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
          htmlString+= '金額<span id="G1801KingakuSpan" class="blockquote-input">0</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Kingaku" value="0">';
      }else{
          htmlString+= '金額<span id="G1801KingakuSpan" class="blockquote-input">' + jsonResult["kingaku"] + '</span><span class="blockquote-inputOption">円</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801Kingaku">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801Kingaku" value="' + jsonResult["kingaku"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      //登録番号
      htmlString+= '<div class="row">';
      htmlString+= '<div class="col s10 offset-s1 mst-item-title">';
      if(nouhiId == 0){
    	  htmlString+= '登録番号<span id="G1801RegistNumberSpan" class="blockquote-input">0</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801RegistNumber">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801RegistNumber" value="0">';
      }else{
    	  htmlString+= '登録番号<span id="G1801RegistNumberSpan" class="blockquote-input">' + jsonResult["registNumber"] + '</span><i class="material-icons orange-text text-lighten-1 small right calcTarget" targetId="G1801RegistNumber">keyboard</i><br />';
          htmlString+= '<input type="hidden" id="G1801RegistNumber" value="' + jsonResult["registNumber"] + '">';
      }
      htmlString+= '</div>';
      htmlString+= '</div>';

      htmlString+= '</form>';
      htmlString+= '</div>';
      htmlString+= '</div>';
      /* コマンドボタン */
      htmlString+= '<div class="row">';
      if(nouhiId == 0){			//登録の場合
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1801Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m6" style="text-align: center;">';
	      htmlString+= '<div id="G1801Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>登　録</div>';
	      htmlString+= '</div>';
      }else{							//修正の場合
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1801Delete" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">delete</i>削　除</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1801Back" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">arrow_back</i>戻　る</div>';
	      htmlString+= '</div>';
	      htmlString+= '<div class="col s12 m4" style="text-align: center;">';
	      htmlString+= '<div id="G1801Submit" class="waves-effect waves-light btn-flat string-color"><i class="material-icons small left string-color">done</i>確　定</div>';
	      htmlString+= '</div>';
      }
      htmlString+= '</div>';
      htmlString+= '<div class="row"></div>';

      htmlString+= '</div>';
      $("#G1801NouhiSetting").html(htmlString);									//可変HTML部分に反映する

      $("input").prop("autocomplete", "off");									//オートコンプリート無効化
      $('.modal').modal();                        								//選択用モーダル画面初期化
	  CalcInit();														        // 数値入力電卓初期化
	  SelectModalInit();												  		//選択用モーダルイベント初期化

      $("#G1801Submit").bind('click', onSubmit );     							//確定ボタン
      $("#G1801Delete").bind('click', onDelete );     							//削除ボタン
      $("#G1801Back").bind('click', onBack );     								//戻るボタン

      },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
  }); // end of document ready

    /* 確定ボタン押下処理 */
	function onSubmit() {

		var farmId = $("#G1801Title").attr("farmId");
		var nouhiId = $("#G1801Title").attr("nouhiId");
		var nouhiKind = $("#G1801NouhiKindValue").val();
		var unitKind = $("#G1801UnitKindValue").val();

		/* 入力項目情報定義 */
		var checktarget = [
		    { "id" : "G1801InNouhiName"			, "name" : "農肥名"						, "length" : "128"	, "json" : "nouhiName"			, "check" : { "required" : "1", "maxlength" : "1"}}
		   ,{ "id" : "G1801InNouhiOfficialName" , "name" : "農肥正式名"					, "length" : "128"	, "json" : "nouhiOfficialName"	, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1801NouhiKindValue"		, "name" : "農肥種別"					, "length" : "16"	, "json" : "nouhiKind"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1801Bairitu"				, "name" : "倍率"						, "length" : "5"	, "json" : "bairitu"			, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801Sanpuryo"			, "name" : "散布量"						, "length" : "32"	, "json" : "sanpuryo"			, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801UnitKindValue"		, "name" : "単位種別"					, "length" : "16"	, "json" : "unitKind"			, "check" : { "maxlength" : "1"}}
		   ,{ "id" : "G1801N"					, "name" : "N"							, "length" : "32"	, "json" : "n"					, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801P"					, "name" : "P"							, "length" : "32"	, "json" : "p"					, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801K"					, "name" : "K"							, "length" : "32"	, "json" : "k"					, "check" : { "number" : "1","maxlength" : "1"}}
       ,{ "id" : "G1801MG"        , "name" : "Mg"             , "length" : "32" , "json" : "mg"         , "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801Lower"				, "name" : "倍率下限"					, "length" : "5"	, "json" : "lower"				, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801Upper"				, "name" : "倍率上限"					, "length" : "5"	, "json" : "upper"				, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801FinalDay"			, "name" : "最終経過日数"				, "length" : "3"	, "json" : "finalDay"			, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801SanpuCount"			, "name" : "散布回数"					, "length" : "2"	, "json" : "sanpuCount"			, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801UseWhen"				, "name" : "使用時期"					, "length" : "3"	, "json" : "useWhen"			, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801Kingaku"				, "name" : "金額"						, "length" : "32"	, "json" : "kingaku"			, "check" : { "number" : "1","maxlength" : "1"}}
		   ,{ "id" : "G1801RegistNumber"		, "name" : "登録番号"					, "length" : "16"	, "json" : "registNumber"		, "check" : { "number" : "1","maxlength" : "1"}}
		];

		/* 入力項目のチェック */
		if (InputDataManager(checktarget) == false) {
		  return false;
		}

		/* 農肥種別のチェック */
        if (nouhiKind == 0) {
          displayToast('農肥種別を選択してください。', 4000, 'rounded');
          return false;
        }

		/* 単位種別のチェック */
        if (unitKind == 0) {
          displayToast('単位種別を選択してください。', 4000, 'rounded');
          return false;
        }

        onProcing(true);

	    var jsondata   = InputDataToJson(checktarget);					  //JSONDATAに変換する
	    jsondata["farmId"] = farmId;
	    jsondata["nouhiId"] = nouhiId;

	    console.log("jsondata", jsondata);

		$.ajax({
			url:"/submitNouhi",
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

		var nouhiId = $("#G1801Title").attr("nouhiId");
	    //JSONDATA変換用文字列の作成
	    var result = '{"nouhiId":"' + nouhiId + '"}';
	    var jsondata = StringToJson(result);	        					//JSONDATAに変換する

	    console.log("jsondata", jsondata);

		if (confirm("農肥情報を削除します。よろしいですか？")) {
			$.ajax({
				url:"/deleteNouhi",
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
		var farmID	= $("#G1801Title").attr("farmId");				//生産者IDを格納

		window.location.href = "/" + farmID + "/7/masterMntMove";

	}


})(jQuery); // end of jQuery name space