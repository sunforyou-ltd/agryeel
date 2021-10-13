(function($){

  $(function(){
  });

  var workinginfo = {"action":"", "name":"", "nae":"", "time":"", "diff":"", "end":"", "workid": 0, "planid": 0};

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
      url:"/workingikubyoinit",
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );

        workinginfo.workid    = jsonResult.workid;
        workinginfo.planid    = jsonResult.planid;
        workinginfo.nae       = jsonResult.naeName;

        $("#workingname").text(jsonResult.workname);
        $("#workingnae").text(jsonResult.naeName);
        $("#workingtime").text(jsonResult.starttime);
        $("#workingdiff").text(jsonResult.difftime);
        $("#workingaica").html(jsonResult.aica);

        workinginfo.action = jsonResult.action;
        $("#workingstop").show();

      },
      dataType:'json',
      contentType:'text/json'
    });
  }

  function workingcommit() {

    var msg = "";

    if (confirm("作業を終了します。よろしいですか？")) {

      var param = {"workId":"", "worknae":"", "workDate":"", "workAccount":"", "mode":10, "planId":0 };

      param.workId      = userinfo.work;
      param.workAccount = userinfo.id;
      param.planId      = userinfo.plan;

      var url = "";

      if (userinfo.plan != 0) {	//育苗計画有りの場合
        url = "/planToDiaryIkubyo";
      }
      else {					//育苗計画なしの場合
        url = "/submitIkubyoDiary";
      }

      $.ajax({
          url:url,                        //育苗日誌保存処理
          type:'POST',
          data:JSON.stringify(param),              //入力用JSONデータ
          complete:function(data, status, jqXHR){         //処理成功時

            var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

            if (jsonResult.result == 'SUCCESS') {
              displayToast('育苗日誌を記録しました。', 4000, 'rounded');            //保存メッセージの表示
              window.location.href = '/ikubyoMove';                             //育苗メニュー画面に遷移する
            }

          },
          error:function(data, status, jqXHR){                //処理成功時

            displayToast('育苗記録の保存に失敗しました', 4000, 'rounded');

          },
          dataType:'json',
          contentType:'text/json',
          async: false
      });
    }

  }

  function workingstop() {
    if (confirm("作業を中断します。よろしいですか？")) {
      $.ajax({
        url:"/workingikubyostop",
        type:'GET',
        complete:function(data, status, jqXHR){           //処理成功時
          var jsonResult    = JSON.parse( data.responseText );

          window.location.href = '/ikubyoMove';

        },
        dataType:'json',
        contentType:'text/json'
      });
    }
  }

  function workinput() {
    window.location.href = '/' + workinginfo.workid + '/ikubyoDiaryMove';
  }

  function workingback() {
    window.location.href = '/ikubyoMove';
  }

})(jQuery);