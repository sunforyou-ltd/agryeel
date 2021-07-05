/* AGRYEEL 入力圃場選択画面 JQUERY */
(function($){

  /* 初期処理時イベント */
  $(function(){

    /*----- 各ボタンにクリックイベントを押下する -----*/

    $(document).ready(function(){
      $('.collapsible').collapsible({
        accordion : false // A setting that changes the collapsible behavior to expandable instead of the default accordion style
      });
    });

    var inputJson = StringToJson('{"accountId":"' + accountInfo.accountId + '", "farmId":"' + accountInfo.farmId + '"}');

  $.ajax({
    url:"/inputTargetInit", 											//入力圃場選択初期処理
      type:'POST',
      data:JSON.stringify(inputJson),								//入力用JSONデータ
      complete:function(data, status, jqXHR){						//処理成功時
      var jsonResult = JSON.parse( data.responseText );			//戻り値用JSONデータの生成
      var targetCompartmentList 	= jsonResult.targetCompartmentStatus;	//作業対象区画リスト
      var htmlString	= "";										//可変HTML文字列

      for ( var targetCompartmentKey in targetCompartmentList ) {	//作業対象区画件数分処理を行う

        var targetCompartment = targetCompartmentList[targetCompartmentKey]; //作業対象区画情報の取得
        var workId = targetCompartment["kukakuId"];			//対象区画ID

        //作業対象圃場を作成する
        htmlString += '<div class="col s12 m3 l2">';
        htmlString += '<div class="card-panel grey lighten-5 z-depth-1 work-target-house-card">';
        htmlString += '<span class="work-target-house-title">' + targetCompartment["kukakuName"] +'</span>';
        htmlString += '<span class="work-target-house-item-tip">' + targetCompartment["hinsyuName"] +'</span>';
        htmlString += '<span class="work-target-house-item-tip">' + targetCompartment["rotationSpeedOfYear"] +'作目</span>';
        htmlString += '<a class="btn-floating waves-effect waves-light blue darken-1 work-target-house-floationg" kukakuid="' + workId + '"><i class="material-icons">add</i></a>';
        htmlString += '</div></div>';

      } // targetCompartmentList

      $("#G0009targetCompartmentList").html(htmlString);					//可変HTML部分に反映する

      },
      dataType:'json',
      contentType:'text/json',
      async: false
    });

    $('.work-target-house-floationg').click(function() {
        /* フローティングボタン制御 */
        $(this).toggleClass('blue');                                                //クラスからblueを除去して背景色を変更する(redと差し替える)
        $(this).toggleClass('red');                                                 //クラスにredを追加して背景色を変更する(blueと差し替える)
        $(this).children('i').toggleClass('mdi-content-add');                       //クラスから＋を除去してアイコンを変更する(レと差し替える)
        $(this).children('i').toggleClass('mdi-action-done');                       //クラスにレを追加してアイコンを変更する(＋と差し替える)
        if($(this).hasClass('red')){
          $(this).html('<i class="material-icons">done</i>');
        }
        else {
          $(this).html('<i class="material-icons">add</i>');
        }
    });

    $('.input-target-done').click(function() {
      //フローティングボタンレ点状態のみ区画ID取得
          var result 		= "";
          var cnt = 0;
      $('.work-target-house-floationg').each(function(){
        if($(this).hasClass('red')){
          if(cnt == 0){
            result += "[{"
          }else{
            result += ",{"
          }
              result += '"kukakuId":"' + $(this).attr("kukakuid") + '"';				//入力項目をJSONDATAに出力
              result += "}";
              cnt++;
        }
      });
          result += "]";

      var jsondata   = StringToJson(result);	        //JSONDATAに変換する

      $.ajax({
          url:"/inputTargetSet", 									//入力圃場設定処理
        type:'POST',
        data:JSON.stringify(jsondata),							//入力用JSONデータ
        complete:function(data, status, jqXHR){					//処理成功時
          var jsonResult = JSON.parse( data.responseText );		//戻り値用JSONデータの生成

          if (jsonResult.result == 'SUCCESS') {
            window.location.href = './menuMove';	            //メニュー画面に遷移する
          }
        },
        dataType:'json',
        contentType:'text/json',
        async: false
      });
    });
  }); // end of document ready

})(jQuery); // end of jQuery name space