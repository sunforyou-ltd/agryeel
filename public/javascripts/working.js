(function($){

  $(function(){
  });

  var workinginfo = {"action":"", "name":"", "kukaku":"", "time":"", "diff":"", "end":"", "workid": 0, "kukakuid": 0};

  $(document).ready(function(){

    getParam();

    $("#workingcommit").unbind("click");
    $("#workingcommit").bind("click",workingcommit);
    $("#workingstop").unbind("click");
    $("#workingstop").bind("click",workingstop);
    $("#workinput").unbind("click");
    $("#workinput").bind("click",workinput);
    $("#workingback").unbind("click");
    $("#workingback").bind("click",workingback);

  }); //end of document.ready

  function getParam() {
    $.ajax({
      url:"/workinginit",
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );

        workinginfo.workid    = jsonResult.workid;
        workinginfo.kukakuid  = jsonResult.kukakuid;

        $("#workingname").text(jsonResult.workname);
        $("#workingkukaku").text(jsonResult.kukakuname);
        $("#workingtime").text(jsonResult.starttime);
        $("#workingdiff").text(jsonResult.difftime);
        $("#workingaica").html(jsonResult.aica);

        workinginfo.action = jsonResult.action;
        //必ず作業中にのみ遷移してくる為、作業開始時の処理を削除
//        if (workinginfo.action == "init") {
//          $("#workingcommitspan").html('<i class="material-icons small left string-color">alarm_on</i>作業開始</span>');
//          $("#workingstop").hide();
//        }
//        else {
//        $("#workingcommitspan").html('<i class="material-icons small left string-color">alarm_off</i>作業終了</span>');
          $("#workingstop").show();
//        }

      },
      dataType:'json',
      contentType:'text/json'
    });
  }

  function workingcommit() {

    var msg = "";

    //必ず作業中にのみ遷移してくる為、作業開始時の処理を削除
//    if (workinginfo.action == "init") {
//      if (confirm("作業を開始します。よろしいですか？")) {
//        $.ajax({
//          url:"/workingcommit",
//          type:'GET',
//          complete:function(data, status, jqXHR){           //処理成功時
//            var jsonResult    = JSON.parse( data.responseText );
//
//            if (jsonResult.work != 0) {  //該当ユーザが作業中の場合
//
//              var inputJson = {"workid":"", "kukakuid":"", "action":""};
//              inputJson.action = "display";
//              inputJson.workid = jsonResult.work;
//              inputJson.kukakuid = jsonResult.field;
//              $.ajax({
//                url:"/initparam",
//                type:'POST',
//                data:JSON.stringify(inputJson),               //入力用JSONデータ
//                complete:function(data, status, jqXHR){           //処理成功時
//                  var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成
//                  window.location.href = "/workingmove";
//              },
//                dataType:'json',
//                contentType:'text/json'
//              });
//            }
//            else {
//              window.location.href = '/menuMove';                                  //メニュー画面に遷移する
//            }
//
//          },
//          dataType:'json',
//          contentType:'text/json'
//        });
//      }
//    }
//    else {
      if (confirm("作業を終了します。よろしいですか？")) {

        var param = {"workId":"", "workKukaku":"", "workDate":"", "workAccount":"", "mode":10, "planId":0 };

        param.workId = userinfo.work;
        param.workKukaku = userinfo.field;
        param.workAccount = userinfo.id;
        param.planId      = userinfo.plan;

        var url = "";

        if (userinfo.plan != 0) { //作業計画有りの場合
          url = "/planToDiary";
        }
        else { //作業計画なしの場合
          url = "/submitWorkDiary";
        }

        $.ajax({
            url:url,                        //作業日誌保存処理
            type:'POST',
            data:JSON.stringify(param),              //入力用JSONデータ
            complete:function(data, status, jqXHR){         //処理成功時

              var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

              if (jsonResult.result == 'SUCCESS') {
                displayToast('作業日誌を記録しました。', 4000, 'rounded');            //保存メッセージの表示
                window.location.href = '/menuMove';                             //メニュー画面に遷移する
              }

            },
            error:function(data, status, jqXHR){                //処理成功時

              displayToast('作業記録の保存に失敗しました', 4000, 'rounded');

            },
            dataType:'json',
            contentType:'text/json',
            async: false
        });
      }
//    }

  }

  function workingstop() {
    if (confirm("作業を中断します。よろしいですか？")) {
      $.ajax({
        url:"/workingstop",
        type:'GET',
        complete:function(data, status, jqXHR){           //処理成功時
          var jsonResult    = JSON.parse( data.responseText );

          window.location.href = '/menuMove';

        },
        dataType:'json',
        contentType:'text/json'
      });
    }
  }

  function workinput() {
    window.location.href = '/' + workinginfo.workid + '/' + workinginfo.kukakuid + '/workDiaryMove';
  }

  function workingback() {
    window.location.href = '/menuMove';
  }

})(jQuery);