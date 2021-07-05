/* AGRYEEL 作業対象圃場画面 JQUERY */
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
    url:"/workTargetInit", 												//作業対象圃場初期処理
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
        var status = targetCompartment["workTarget"];
        if (status == 0){
          htmlString += '<a class="btn-floating waves-effect waves-light blue darken-1 work-target-house-floationg" kukakuid="' + workId + '"><i class="material-icons">add</i></a>';
        }else{
          htmlString += '<a class="btn-floating waves-effect waves-light red darken-1 work-target-house-floationg" kukakuid="' + workId + '"><i class="material-icons">done</i></a>';
        }
        htmlString += '</div></div>';

      } // targetCompartmentList

      $("#G0003targetCompartmentList").html(htmlString);					//可変HTML部分に反映する

      $('.work-target-house-floationg').click(function() {
          /* フローティングボタン制御 */
          $(this).toggleClass('blue');                                                //クラスからblueを除去して背景色を変更する(redと差し替える)
          $(this).toggleClass('red');                                                 //クラスにredを追加して背景色を変更する(blueと差し替える)
          if($(this).hasClass('red')){
            $(this).html('<i class="material-icons">done</i>');
          }
          else {
            $(this).html('<i class="material-icons">add</i>');
          }
          $(this).attr("kukakuid");

          var inputJson = StringToJson('{"kukakuId":"' + $(this).attr("kukakuid") + '"}');

          //対象区画の作業対象フラグ更新
        $.ajax({
            url:"/workTargetUpdate",												//作業対象圃場更新処理
              type:'POST',
              data:JSON.stringify(inputJson),										//入力用JSONデータ
              complete:function(data, status, jqXHR){								//処理成功時
              var jsonResult = JSON.parse( data.responseText );					//戻り値用JSONデータの生成
              var targetCompartmentList 	= jsonResult.targetCompartmentStatus;	//作業対象区画リスト
              var htmlString	= "";												//可変HTML文字列

              displayToast('作業対象区画情報を更新しました。', 1000, 'rounded');      //エラーメッセージの表示

              },
              dataType:'json',
              contentType:'text/json',
              async: true
            });
      });
      },
      dataType:'json',
      contentType:'text/json',
      async: true
    });

  }); // end of document ready

})(jQuery); // end of jQuery name space