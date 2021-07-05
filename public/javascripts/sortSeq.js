(function($){

  var groupInfo = [];

  $(function(){
  });

  $(document).ready(function(){

    init();

  }); //end of document.ready

  function fieldGroupCompare(a, b) {
    var comp = 0;
    if (a.sequenceId > b.sequenceId) {
      comp = 1;
    }
    else if (a.sequenceId < b.sequenceId) {
      comp = -1;
    }
    return comp;
  }
  function kukakuCompare(a, b) {
    var comp = 0;
    if (a.sequenceId > b.sequenceId) {
      comp = 1;
    }
    else if (a.sequenceId < b.sequenceId) {
      comp = -1;
    }
    return comp;
  }
  function init() {
    $.ajax({
      url:"/sortSeqinit",
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );

        $("#sortSeqlist").empty();
        $("#sortSeqlist").append('<div id="sortSeqList" class="groupList"></div>');
        var list = $("#sortSeqList");
        var datalist = jsonResult.datalist;
        var idx = 0;
        var glists = [];
        for ( var key in datalist ) {                     //対象圃場情報分処理を行う
          glists.push(datalist[key]);
        }
        glists.sort(fieldGroupCompare);

        for (var gkey in glists) {
          var gdata = glists[gkey];
          var code  = gdata.color;
          var red   = parseInt(code.substring(0,2), 16);
          var green = parseInt(code.substring(2,4), 16);
          var blue  = parseInt(code.substring(4,6), 16);
          list.append('<div id="group-' + gdata.id + '" class="groupitem sortSeqlist-group-item card-panel" groupId="' + gdata.id + '" key="' + idx + '" style="background: rgba(' + red + ',' + green + ',' + blue + ', .8)"><span>' + gdata.name + '</span><i class="material-icons small right">arrow_downward</i></div>');
          var group = $('#group-' + gdata.id);
          var kukakus = gdata.kukakus;
          var lists = [];
          for ( var key in kukakus ) {                     //対象圃場情報分処理を行う
            lists.push(kukakus[key]);
          }
          lists.sort(kukakuCompare);
          group.append('<div id="sortSeqList-' + gdata.id + '" class="list" groupId="' + gdata.id + '"></div>');
          var glist = $('#sortSeqList-' + gdata.id);

          for (var kkey in lists) {
            var kdata = lists[kkey];
            glist.append('<div id="kukaku-' + kdata.id + '" class="item sortSeqlist-kukaku-item card-panel" kukakuId="' + kdata.id + '">' + kdata.name + '</div>');
          }
          //並び替えオブジェクト設定
          Sortable.create($('.list')[idx]
          , {
            draggable: ".item"
            ,group: {
              name: "fieldGroup",
            }
            ,onEnd: function (evt) {
             var my = $(evt.from);
             var kukakus = my.children(".sortSeqlist-kukaku-item");
             var sequenceId = 0;
             var jsonList   = [];
             for (var kkey = 0; kkey < kukakus.length; kkey++) {
               var kukaku = kukakus[kkey];
               sequenceId++;
               var json   = {kukakuId: kukaku.getAttribute("kukakuId"), sequenceId: sequenceId};
               jsonList.push(json);
             }

             var jsondata = {fieldGroupId:my.attr("groupId") ,datalist: jsonList};
             $.ajax({
               url:"/sortSequpdate",
               type:'POST',
               data:JSON.stringify(jsondata),              //入力用JSONデータ
               complete:function(data, status, jqXHR){         //処理成功時

                 var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

                 var my = $(evt.to);
                 var kukakus = my.children(".sortSeqlist-kukaku-item");
                 var sequenceId = 0;
                 var jsonList   = [];
                 for (var kkey = 0; kkey < kukakus.length; kkey++) {
                   var kukaku = kukakus[kkey];
                   sequenceId++;
                   var json   = {kukakuId: kukaku.getAttribute("kukakuId"), sequenceId: sequenceId};
                   jsonList.push(json);
                 }

                 var jsondata = {fieldGroupId:my.attr("groupId") ,datalist: jsonList};
                 $.ajax({
                   url:"/sortSequpdate",
                   type:'POST',
                   data:JSON.stringify(jsondata),              //入力用JSONデータ
                   complete:function(data, status, jqXHR){         //処理成功時

                     var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

                   },
                   dataType:'json',
                   contentType:'text/json',
                   async: false
                });

               },
               dataType:'json',
               contentType:'text/json',
               async: false
            });

            }
          }
          );
          var info = {idx: idx, id:gdata.id, open: false};
          groupInfo.push(info);
          $("#sortSeqList-" + gdata.id).hide();
          idx++;
        }
        $(".sortSeqlist-group-item").unbind("click");
        $(".sortSeqlist-group-item").bind("click", groupOpen);
        //並び替えオブジェクト設定
        Sortable.create($('.groupList')[0]
        , {
          draggable: ".groupitem"
          ,onEnd: function (evt) {
            var my = $(evt.from);
            var groups = my.children(".sortSeqlist-group-item");
            var sequenceId = 0;
            var jsonList   = [];
            for (var gkey = 0; gkey < groups.length; gkey++) {
              var group = groups[gkey];
              sequenceId++;
              var json   = {fieldGroupId: group.getAttribute("groupId"), sequenceId: sequenceId};
              jsonList.push(json);
            }

            var jsondata = {datalist: jsonList};
            $.ajax({
              url:"/fieldGroupUpdate",
              type:'POST',
              data:JSON.stringify(jsondata),              //入力用JSONデータ
              complete:function(data, status, jqXHR){         //処理成功時

                var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成

              },
              dataType:'json',
              contentType:'text/json',
              async: false
           });
          }
        }
        );
      },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function groupOpen() {
    var my = $(this);
    var key = my.attr("key");
    var info = groupInfo[key];
    var i = $("#group-" + info.id + " i");
    if (info.open) {
      $("#sortSeqList-" + info.id).hide();
      i.text("arrow_downward");
      info.open = false;
    }
    else {
      $("#sortSeqList-" + info.id).show();
      i.text("arrow_upward");
      info.open = true;
    }
  }
})(jQuery);