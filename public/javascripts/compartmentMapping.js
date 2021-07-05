//-------------------------------------------------------------------------------------
// GoogleMAP 初期制御
//-------------------------------------------------------------------------------------
var map;
function initMap() {
  navigator.geolocation.getCurrentPosition(function(position) {
    map = new google.maps.Map(document.getElementById('gmap'), {
      center: {lat: position.coords.latitude, lng: position.coords.longitude},
      zoom: 14
    });
  });
}
(function($){

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
      url:"/compartmentmappingInit",
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult    = JSON.parse( data.responseText );

        var list = $("#kukakuList");
        list.empty();
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
          list.append('<li id="group-' + gdata.id + '" class="groupdata" groupId="' + gdata.id + '" key="' + idx + '" status="off" style="background: rgba(' + red + ',' + green + ',' + blue + ', .8)"><span>' + gdata.name + '</span></li>');
          var kukakus = gdata.kukakus;
          var lists = [];
          for ( var key in kukakus ) {                     //対象圃場情報分処理を行う
            lists.push(kukakus[key]);
          }
          lists.sort(kukakuCompare);
          for (var kkey in lists) {
            var kdata = lists[kkey];
            var end   = "";
            if (kdata.lat != "0" && kdata.lng != "0") {
              end   = "設定済み";
            }
            list.append('<li id="kukaku-' + kdata.id + '" class="kukakudata group-' + gdata.id + '" kukakuId="' + kdata.id + '" kname="' + kdata.name + '" lat="' + kdata.lat + '" lng="' + kdata.lng + '"><span>' + kdata.name + '</span><span class="end">' + end + '</span></li>');
          }
        }
        $(".kukakudata").hide();
        $(".groupdata").unbind("click");
        $(".groupdata").bind("click", onClickGroupData);
        $(".kukakudata").unbind("click");
        $(".kukakudata").bind("click", onClickKukakuData);
        $("#btnSetting").unbind("click");
        $("#btnSetting").bind("click", onClickBtnSetting);
        $("#btnDelete").unbind("click");
        $("#btnDelete").bind("click", onClickBtnDelete);
        $("#btnClear").unbind("click");
        $("#btnClear").bind("click", onClickBtnClear);
      },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function onClickGroupData() {
    var my = $(this);
    if (my.attr("status") == "off") {
      $(".group-" + my.attr("groupId")).show();
      my.attr("status", "on");
    }
    else {
      $(".group-" + my.attr("groupId")).hide();
      my.attr("status", "off");
    }
  }
  var markerList = [];
  function onClickKukakuData() {
    var my = $(this);
    var list = $("#selectList");
    list.append('<li id="select-' + my.attr("kukakuId") + '" class="selectdata" kukakuId="' + my.attr("kukakuId") + '" kname="' + my.attr("kname") + '"><span>' + my.attr("kname") + '</span></li>');
    var lat = parseFloat(my.attr("lat"));
    var lng = parseFloat(my.attr("lng"));
    if (lat != 0 && lng != 0) {
      var marker = new google.maps.Marker({
        position: {lat: lat, lng: lng},
        map: map,
        animation: google.maps.Animation.DROP
      });
      var infoWindow = new google.maps.InfoWindow({
        content: my.attr("kname")
      });
      //マーカーのイベント
      google.maps.event.addListener(marker, 'click', function() {
        infoWindow.open(map, marker);
      });
      var markerInfo = {id:my.attr("kukakuId"), marker:marker};
      markerList.push(markerInfo);
    }
    $(".selectdata").unbind("click");
    $(".selectdata").bind("click", onClickSelectData);
    my.hide();
  }
  function onClickSelectData() {
    var my = $(this);
    for (var key in markerList) {
      var markerInfo = markerList[key];
      if (my.attr("kukakuId") == markerInfo.id) {
        markerInfo.marker.setMap(null);
        markerList.splice(key, 1);
        break;
      }
    }
    $("#kukaku-" + my.attr("kukakuId")).show();
    my.remove();


  }
  var selectList;
  var selectKey=0;
  var selectinfo;
  function onClickBtnSetting() {
    var my = $(this);
    var selectList = $("#selectList").children("li");

    if (selectList.length == 0) {
      displayToast('設定対象区画が選択されていません。', 2000, 'rounded');
      return false;
    }
    //マップのイベント
    google.maps.event.addListener(map, 'click', function(e) {
      var marker = new google.maps.Marker({
        position: e.latLng,  //イベントの発生した緯度・経度（位置）
        map: this,  //this は map を意味します
        animation: google.maps.Animation.DROP
      });
      var infoWindow = new google.maps.InfoWindow({
        content: selectinfo.attr("kname")
      });

      //マーカーのイベント
      google.maps.event.addListener(marker, 'click', function() {
        infoWindow.open(map, marker);
      });
      var jsondata = { kukakuId:selectinfo.attr("kukakuId"), lat: e.latLng.lat(), lng: e.latLng.lng()};
      $.ajax({
        url:"/compartmentmappingUpdate",
        type:'POST',
        data:JSON.stringify(jsondata),              //入力用JSONデータ
        complete:function(data, status, jqXHR){         //処理成功時

          var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
          $("#kukaku-" + jsonResult.kukakuId).children(".end").text("設定済み");

        },
        dataType:'json',
        contentType:'text/json',
        async: false
     });
      var markerInfo = {id:selectinfo.attr("kukakuId"), marker:marker};
      markerList.push(markerInfo);
      selectKey++;
      if (selectKey < selectList.length) {
        selectinfo = $(selectList[selectKey]);
        selectinfo = $(selectList[selectKey]);
        displayToast(selectinfo.attr("kname") + 'の位置座標を選択してください', 2000, 'rounded');
      }
      else {
        google.maps.event.clearListeners(map, 'click');
        displayToast('全ての位置座標が設定されました', 2000, 'rounded');
      }
    });
    for (var key = 0; key < markerList.length; key++) {
      var markerInfo = markerList[key];
      if (markerInfo.marker != undefined) {
        markerInfo.marker.setMap(null);
      }
    }
    markerInfo = [];
    selectKey=0;
    selectinfo = $(selectList[selectKey]);
    displayToast(selectinfo.attr("kname") + 'の位置座標を選択してください', 2000, 'rounded');
  }
  function onClickBtnDelete() {
    var my = $(this);
    var selectList = $("#selectList").children("li");

    if (selectList.length == 0) {
      displayToast('削除対象区画が選択されていません。', 2000, 'rounded');
      return false;
    }
    var datalist = [];
    for (var key = 0; key < selectList.length; key++) {
      var selectinfo = $(selectList[key]);
      var json = {kukakuId: selectinfo.attr("kukakuId")}
      datalist.push(json);
    }
    var jsondata = { datalist:datalist};
    $.ajax({
      url:"/compartmentmappingDelete",
      type:'POST',
      data:JSON.stringify(jsondata),              //入力用JSONデータ
      complete:function(data, status, jqXHR){         //処理成功時

        var jsonResult = JSON.parse( data.responseText );   //戻り値用JSONデータの生成
        displayToast('全ての位置座標が削除されました', 2000, 'rounded');
        for (var key = 0; key < selectList.length; key++) {
          var selectinfo = $(selectList[key]);
          $("#kukaku-" + selectinfo.attr("kukakuId")).children(".end").text("");
        }

      },
      dataType:'json',
      contentType:'text/json',
      async: false
   });
  }
  function onClickBtnClear() {
    var selectList = $("#selectList").children("li");
    var selectCount = selectList.length;
    for (var idx = 0; idx < selectCount; idx++) {
      var selectinfo = $(selectList[idx]);
      for (var key in markerList) {
        var markerInfo = markerList[key];
        if (selectinfo.attr("kukakuId") == markerInfo.id) {
          markerInfo.marker.setMap(null);
          markerList.splice(0, 1);
          break;
        }
      }
      $("#kukaku-" + selectinfo.attr("kukakuId")).show();
      selectinfo.remove();
    }
  }
})(jQuery);