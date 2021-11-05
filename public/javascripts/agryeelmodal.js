//------------------------------------------------------------------------------------------------------------------
//----- セレクトモーダルのインスタンス変数 -----
var oSelect = {open: false, data: [], target: "", hidden:""};
var oSelectJson = [];
//----- マルチセレクトモーダルのインスタンス変数 -----
var oMselect = {open: false, data: [], target: "", hidden:""};
var oMselectJson = [];

$(document).ready(function(){

  //------------------------------------------------------------------------------------------------------------------
  //- モーダルの初期化
  //------------------------------------------------------------------------------------------------------------------
  $('.modal').modal();
  //------------------------------------------------------------------------------------------------------------------
  //- セレクトモーダルの初期化
  //------------------------------------------------------------------------------------------------------------------
  $('.selectmodal-trigger').unbind('click');
  $('.selectmodal-trigger').bind('click', selectOpen);
  //------------------------------------------------------------------------------------------------------------------
  //- マルチセレクトモーダルの初期化
  //------------------------------------------------------------------------------------------------------------------
  $('.mselectmodal-trigger').unbind('click');
  $('.mselectmodal-trigger').bind('click', mSelectOpen);
  //------------------------------------------------------------------------------------------------------------------
  //- セレクトモーダル（Sub）の初期化
  //------------------------------------------------------------------------------------------------------------------
  $('.selectsubmodal-trigger').unbind('click');
  $('.selectsubmodal-trigger').bind('click', selectSubOpen);
  //------------------------------------------------------------------------------------------------------------------
  //- マルチセレクトモーダル（Sub）の初期化
  //------------------------------------------------------------------------------------------------------------------
  $('.mselectsubmodal-trigger').unbind('click');
  $('.mselectsubmodal-trigger').bind('click', mSelectSubOpen);

  //------------------------------------------------------------------------------------------------------------------
  //- カラーピッカーの初期化
  //------------------------------------------------------------------------------------------------------------------
  $('.colormodal-trigger').unbind('click');
  $('.colormodal-trigger').bind('click', colorOpen);

}); //end of document.ready

//------------------------------------------------------------------------------------------------------------------
//- セレクト用データの取得
//------------------------------------------------------------------------------------------------------------------
function selectDataGet(displayspan, data) {

  oSelect.data   = [];
  oSelect.target = displayspan;

  var url = "/" + data;
  $.ajax({
    url:url,
    type:'GET',
    complete:function(data, status, jqXHR){
      var jsonResult = JSON.parse( data.responseText );

      if (jsonResult.result == 'SUCCESS') {
        var dl = jsonResult.datalist;
        for (var key in dl) {
          var data  = dl[key];
          var msd   = {id:"", name: "", select: false};
          msd.id    = data.id;
          msd.name  = data.name;
          if (jsonResult.flag == 0) {
            msd.select  = false;
          }
          else {
            if (data.flag == 0) {
              msd.select  = false;
            }
            else {
              msd.select  = true;
            }
          }
          oSelect.data.push(msd);
        }
      }
      selectJsonSet(oSelect.target, oSelect.data);
    },
    dataType:'json',
    contentType:'text/json',
    async: false
  });
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクト用データの取得
//------------------------------------------------------------------------------------------------------------------
function mSelectDataGet(displayspan, data) {

  oMselect.data   = [];
  oMselect.target = displayspan;

  var url = "/" + data;
  $.ajax({
    url:url,
    type:'GET',
    complete:function(data, status, jqXHR){
      var jsonResult = JSON.parse( data.responseText );

      if (jsonResult.result == 'SUCCESS') {
        var dl = jsonResult.datalist;
        for (var key in dl) {
          var data  = dl[key];
          var msd   = {id:"", name: "", select: false};
          msd.id    = data.id;
          msd.name  = data.name;
          if (jsonResult.flag == 0) {
            msd.select  = false;
          }
          else {
            if (data.flag == 0) {
              msd.select  = false;
            }
            else {
              msd.select  = true;
            }
          }
          oMselect.data.push(msd);
        }
      }
      mSelectJsonSet(oMselect.target, oMselect.data);
    },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
}
//------------------------------------------------------------------------------------------------------------------
//- セレクト用(Sub)データの取得
//------------------------------------------------------------------------------------------------------------------
function selectSubDataGet(displayspan, data) {

  oSelect.data   = [];
  oSelect.target = displayspan;

  var url = "/" + data;
  $.ajax({
    url:url,
    type:'GET',
    complete:function(data, status, jqXHR){
      var jsonResult = JSON.parse( data.responseText );

      if (jsonResult.result == 'SUCCESS') {
        var dl = jsonResult.datalist;
        for (var key in dl) {
          var data  = dl[key];
          var msd   = {id:"", name: "", sub: "", select: false};
          msd.id    = data.id;
          msd.name  = data.name;
          msd.sub   = data.sub;
          if (jsonResult.flag == 0) {
            msd.select  = false;
          }
          else {
            if (data.flag == 0) {
              msd.select  = false;
            }
            else {
              msd.select  = true;
            }
          }
          oSelect.data.push(msd);
        }
      }
      selectJsonSet(oSelect.target, oSelect.data);
    },
    dataType:'json',
    contentType:'text/json',
    async: false
  });
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクト用(Sub)データの取得
//------------------------------------------------------------------------------------------------------------------
function mSelectSubDataGet(displayspan, data) {

  oMselect.data   = [];
  oMselect.target = displayspan;

  var url = "/" + data;
  $.ajax({
    url:url,
    type:'GET',
    complete:function(data, status, jqXHR){
      var jsonResult = JSON.parse( data.responseText );

      if (jsonResult.result == 'SUCCESS') {
        var dl = jsonResult.datalist;
        for (var key in dl) {
          var data  = dl[key];
          var msd   = {id:"", name: "", sub: "", select: false};
          msd.id    = data.id;
          msd.name  = data.name;
          msd.sub   = data.sub;
          if (jsonResult.flag == 0) {
            msd.select  = false;
          }
          else {
            if (data.flag == 0) {
              msd.select  = false;
            }
            else {
              msd.select  = true;
            }
          }
          oMselect.data.push(msd);
        }
      }
      mSelectJsonSet(oMselect.target, oMselect.data);
    },
      dataType:'json',
      contentType:'text/json',
      async: false
    });
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトデータセット
//------------------------------------------------------------------------------------------------------------------
function selectDataSet(displayspan, data) {

  oSelect.data = selectJsonGet(displayspan);

  if (oSelect.data == undefined) {
    selectDataGet(displayspan, data);
  }
  oSelect.target = displayspan;

  var spanMsg = "";
  var msgCnt  = 0;
  for (var idx = 0; idx < oSelect.data.length; idx++) {
    if (oSelect.data[idx].select == true) {
      if (msgCnt > 1) {
        spanMsg += "，．．．";
        break;
      }
      else if (msgCnt == 1) {
        spanMsg += "，";
      }
      spanMsg += oSelect.data[idx].name;
      msgCnt++;
    }
  }
  if (msgCnt == 0) {
    spanMsg = "未選択"
  }
  selectJsonSet(oSelect.target, oSelect.data);
  var span = $(oSelect.target);
  if (span != undefined) {
    if (span.is(':hidden')) {
    }
    else {
      span.text(spanMsg);
    }
  }
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトデータセット
//------------------------------------------------------------------------------------------------------------------
function mSelectDataSet(displayspan, data) {

  oMselect.data = mSelectJsonGet(displayspan);

  if (oMselect.data == undefined) {
    mSelectDataGet(displayspan, data);
  }
  oMselect.target = displayspan;

  var spanMsg = "";
  var msgCnt  = 0;
  for (var idx = 0; idx < oMselect.data.length; idx++) {
    if (oMselect.data[idx].select == true) {
      if (msgCnt > 1) {
        spanMsg += "，．．．";
        break;
      }
      else if (msgCnt == 1) {
        spanMsg += "，";
      }
      spanMsg += oMselect.data[idx].name;
      msgCnt++;
    }
  }
  if (msgCnt == 0) {
    spanMsg = "未選択"
  }
  mSelectJsonSet(oMselect.target, oMselect.data);
  var span = $(oMselect.target);
  if (span != undefined) {
    if (span.is(':hidden')) {
    }
    else {
      span.text(spanMsg);
    }
  }
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダルオープン時
//------------------------------------------------------------------------------------------------------------------
function selectOpen() {

  var ms = $('#selectmodal');
  if (ms != undefined) {
    ms.empty();
    ms.append('<div id="selectarea" class="modal-content">'); //モーダルコンテンツ領域を生成
    var ma = $('#selectarea');

    if (ma != undefined) {

      ma.append('<h6>' + $(this).attr("title") +'</h6>');     //コンテンツヘッダーを生成する

      var data = $(this).attr("data");
      oSelect.target = $(this).attr("displayspan");
      var hidden = $(this).attr("htext");
      if (hidden != undefined) {
        oSelect.hidden = hidden;
      }
      else {
        oSelect.hidden = "";
      }

      oSelect.data = selectJsonGet(oSelect.target);

      if (oSelect.data == undefined) {
        selectDataGet(oSelect.target, data);
      }

      ma.append('<ul id="selectlist" class="collection">');  //メニューリストを生成する
      var ml = $("#selectlist");                             //メニューリストを取得する
      if (ml != undefined ) {                                 //メニューリストが存在する場合
        for (var key in oSelect.data) {

          var data  = oSelect.data[key];
          ml.append('<li class="collection-item select-item" dataid="' + data.id +'"><span>' + data.name + '</span></li>');

        }
      }
      $('.select-item').unbind("click");
      $('.select-item').bind("click", selectChange);
    }
    else {

    }
    ms.append('<div id="selectfooter" class="modal-footer">');
    var mf = $('#selectfooter');
    if (mf != undefined) {
      mf.append('<a href="#!" id="selectback" class="waves-effect waves-green btn-flat">閉じる</a>');
      $('#selectback').unbind("click");
      $('#selectback').bind("click", selectClose);
    }
    ms.modal('open');
  }
  return false;
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダルオープン時
//------------------------------------------------------------------------------------------------------------------
function mSelectOpen() {

  var ms = $('#mselectmodal');
  if (ms != undefined) {
    ms.empty();
    ms.append('<div id="mselectarea" class="modal-content">'); //モーダルコンテンツ領域を生成
    var ma = $('#mselectarea');

    if (ma != undefined) {

      ma.append('<h6>' + $(this).attr("title") +'</h6>');     //コンテンツヘッダーを生成する

      var data = $(this).attr("data");
      oMselect.target = $(this).attr("displayspan");
      var hidden = $(this).attr("htext");
      if (hidden != undefined) {
        oMselect.hidden = hidden;
      }
      else {
        oMselect.hidden = "";
      }

      oMselect.data = mSelectJsonGet(oMselect.target);

      if (oMselect.data == undefined) {
        mSelectDataGet(oMselect.target, data);
      }

      ma.append('<ul id="mselectlist" class="collection">');  //メニューリストを生成する
      var ml = $("#mselectlist");                             //メニューリストを取得する
      if (ml != undefined ) {                                 //メニューリストが存在する場合
        for (var key in oMselect.data) {

          var data  = oMselect.data[key];
          if (data.select == true) {
            ml.append('<li class="collection-item mselect-item" dataid="' + data.id +'"><span>' + data.name + '</span><i class="material-icons status done">done</i></li>');
          }
          else {
            ml.append('<li class="collection-item mselect-item" dataid="' + data.id +'"><span>' + data.name + '</span><i class="material-icons status add">add</i></li>');
          }
        }
      }
      $('.mselect-item').unbind("click");
      $('.mselect-item').bind("click", mSelectChange);
    }
    else {

    }
    ms.append('<div id="mselectfooter" class="modal-footer">');
    var mf = $('#mselectfooter');
    if (mf != undefined) {
      mf.append('<a href="#!" id="mselectadd" class="waves-effect waves-green btn-flat left">一括選択</a>');
      mf.append('<a href="#!" id="mselectdel" class="waves-effect waves-green btn-flat left">一括解除</a>');
      mf.append('<a href="#!" id="mselectback" class="waves-effect waves-green btn-flat">閉じる</a>');
      $('#mselectadd').unbind("click");
      $('#mselectadd').bind("click", mSelectAdd);
      $('#mselectdel').unbind("click");
      $('#mselectdel').bind("click", mSelectDel);
      $('#mselectback').unbind("click");
      $('#mselectback').bind("click", mSelectClose);
    }
    ms.modal('open');
  }
  return false;
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダル(Sub)オープン時
//------------------------------------------------------------------------------------------------------------------
function selectSubOpen() {

  var ms = $('#selectsubmodal');
  if (ms != undefined) {
    ms.empty();
    ms.append('<div id="selectarea" class="modal-content">'); //モーダルコンテンツ領域を生成
    var ma = $('#selectarea');

    if (ma != undefined) {

      ma.append('<h6>' + $(this).attr("title") +'</h6>');     //コンテンツヘッダーを生成する

      var data = $(this).attr("data");
      oSelect.target = $(this).attr("displayspan");
      var hidden = $(this).attr("htext");
      if (hidden != undefined) {
        oSelect.hidden = hidden;
      }
      else {
        oSelect.hidden = "";
      }

      oSelect.data = selectJsonGet(oSelect.target);

      if (oSelect.data == undefined) {
        selectSubDataGet(oSelect.target, data);
      }

      ma.append('<ul id="selectlist" class="collection">');  //メニューリストを生成する
      var ml = $("#selectlist");                             //メニューリストを取得する
      if (ml != undefined ) {                                 //メニューリストが存在する場合
        for (var key in oSelect.data) {

          var data  = oSelect.data[key];
          ml.append('<li class="collection-item select-item" dataid="' + data.id +'"><span>' + data.name + '</span><span class="right">' + data.sub + '</span></li>');

        }
      }
      $('.select-item').unbind("click");
      $('.select-item').bind("click", selectSubChange);
    }
    else {

    }
    ms.append('<div id="selectfooter" class="modal-footer">');
    var mf = $('#selectfooter');
    if (mf != undefined) {
      mf.append('<a href="#!" id="selectback" class="waves-effect waves-green btn-flat">閉じる</a>');
      $('#selectback').unbind("click");
      $('#selectback').bind("click", selectSubClose);
    }
    ms.modal('open');
  }
  return false;
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダルオープン時
//------------------------------------------------------------------------------------------------------------------
function mSelectSubOpen() {

  var ms = $('#mselectsubmodal');
  if (ms != undefined) {
    ms.empty();
    ms.append('<div id="mselectarea" class="modal-content">'); //モーダルコンテンツ領域を生成
    var ma = $('#mselectarea');

    if (ma != undefined) {

      ma.append('<h6>' + $(this).attr("title") +'</h6>');     //コンテンツヘッダーを生成する

      var data = $(this).attr("data");
      oMselect.target = $(this).attr("displayspan");
      var hidden = $(this).attr("htext");
      if (hidden != undefined) {
        oMselect.hidden = hidden;
      }
      else {
        oMselect.hidden = "";
      }

      oMselect.data = mSelectJsonGet(oMselect.target);

      if (oMselect.data == undefined) {
        mSelectSubDataGet(oMselect.target, data);
      }

      ma.append('<ul id="mselectlist" class="collection">');  //メニューリストを生成する
      var ml = $("#mselectlist");                             //メニューリストを取得する
      if (ml != undefined ) {                                 //メニューリストが存在する場合
        for (var key in oMselect.data) {

          var data  = oMselect.data[key];
          if (data.select == true) {
            ml.append('<li class="collection-item mselect-item" dataid="' + data.id +'"><span>' + data.name + '</span><i class="material-icons status done">done</i><span class="subitem">' + data.sub + '</span></li>');
          }
          else {
            ml.append('<li class="collection-item mselect-item" dataid="' + data.id +'"><span>' + data.name + '</span><i class="material-icons status add">add</i><span class="subitem">' + data.sub + '</span></li>');
          }
        }
      }
      $('.mselect-item').unbind("click");
      $('.mselect-item').bind("click", mSelectChange);
    }
    else {

    }
    ms.append('<div id="mselectfooter" class="modal-footer">');
    var mf = $('#mselectfooter');
    if (mf != undefined) {
      mf.append('<a href="#!" id="mselectadd" class="waves-effect waves-green btn-flat left">一括選択</a>');
      mf.append('<a href="#!" id="mselectdel" class="waves-effect waves-green btn-flat left">一括解除</a>');
      mf.append('<a href="#!" id="mselectback" class="waves-effect waves-green btn-flat">閉じる</a>');
      $('#mselectadd').unbind("click");
      $('#mselectadd').bind("click", mSelectAdd);
      $('#mselectdel').unbind("click");
      $('#mselectdel').bind("click", mSelectDel);
      $('#mselectback').unbind("click");
      $('#mselectback').bind("click", mSelectSubClose);
    }
    ms.modal('open');
  }
  return false;
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダルクローズ時
//------------------------------------------------------------------------------------------------------------------
function selectClose() {
  var spanMsg = "";
  var hiddenMsg = "";
  var msgCnt  = 0;
  for (var idx = 0; idx < oSelect.data.length; idx++) {
    if (oSelect.data[idx].select == true) {
      if (msgCnt > 1) {
        spanMsg += "，．．．";
        break;
      }
      else if (msgCnt == 1) {
        spanMsg += "，";
      }
      spanMsg += oSelect.data[idx].name;
      msgCnt++;
    }
  }
  if (msgCnt == 0) {
    spanMsg = "未選択"
  }
  msgCnt  = 0;
  for (var idx = 0; idx < oSelect.data.length; idx++) {
    if (oSelect.data[idx].select == true) {
      if (msgCnt >= 1) {
        hiddenMsg += ",";
      }
      hiddenMsg += oSelect.data[idx].id;
      msgCnt++;
    }
  }
  mSelectJsonSet(oSelect.target, oSelect.data);
  var span = $(oSelect.target);
  if (span != undefined) {
    span.text(spanMsg);
  }
  var hidden = $(oSelect.hidden);
  if (hidden != undefined) {
    hidden.val(hiddenMsg).change();
  }
  var ms = $('#selectmodal');
  if (ms != undefined) {
    ms.modal('close');
  }
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダルクローズ時
//------------------------------------------------------------------------------------------------------------------
function mSelectClose() {
  var spanMsg = "";
  var hiddenMsg = "";
  var msgCnt  = 0;
  for (var idx = 0; idx < oMselect.data.length; idx++) {
    if (oMselect.data[idx].select == true) {
      if (msgCnt > 1) {
        spanMsg += "，．．．";
        break;
      }
      else if (msgCnt == 1) {
        spanMsg += "，";
      }
      spanMsg += oMselect.data[idx].name;
      msgCnt++;
    }
  }
  if (msgCnt == 0) {
    spanMsg = "未選択"
  }
  msgCnt  = 0;
  for (var idx = 0; idx < oMselect.data.length; idx++) {
    if (oMselect.data[idx].select == true) {
      if (msgCnt >= 1) {
        hiddenMsg += ",";
      }
      hiddenMsg += oMselect.data[idx].id;
      msgCnt++;
    }
  }
  mSelectJsonSet(oMselect.target, oMselect.data);
  var span = $(oMselect.target);
  if (span != undefined) {
    span.text(spanMsg);
  }
  var hidden = $(oMselect.hidden);
  if (hidden != undefined) {
    hidden.val(hiddenMsg).change();
  }
  var ms = $('#mselectmodal');
  if (ms != undefined) {
    ms.modal('close');
  }
  return true;
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダル(Sub)クローズ時
//------------------------------------------------------------------------------------------------------------------
function selectSubClose() {
  var spanMsg = "";
  var hiddenMsg = "";
  var msgCnt  = 0;
  for (var idx = 0; idx < oSelect.data.length; idx++) {
    if (oSelect.data[idx].select == true) {
      if (msgCnt > 1) {
        spanMsg += "，．．．";
        break;
      }
      else if (msgCnt == 1) {
        spanMsg += "，";
      }
      spanMsg += oSelect.data[idx].name;
      msgCnt++;
    }
  }
  if (msgCnt == 0) {
    spanMsg = "未選択"
  }
  msgCnt  = 0;
  for (var idx = 0; idx < oSelect.data.length; idx++) {
    if (oSelect.data[idx].select == true) {
      if (msgCnt >= 1) {
        hiddenMsg += ",";
      }
      hiddenMsg += oSelect.data[idx].id;
      msgCnt++;
    }
  }
  mSelectJsonSet(oSelect.target, oSelect.data);
  var span = $(oSelect.target);
  if (span != undefined) {
    span.text(spanMsg);
  }
  var hidden = $(oSelect.hidden);
  if (hidden != undefined) {
    hidden.val(hiddenMsg).change();
  }
  var ms = $('#selectsubmodal');
  if (ms != undefined) {
    ms.modal('close');
  }
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダル(Sub)クローズ時
//------------------------------------------------------------------------------------------------------------------
function mSelectSubClose() {
  var spanMsg = "";
  var hiddenMsg = "";
  var msgCnt  = 0;
  for (var idx = 0; idx < oMselect.data.length; idx++) {
    if (oMselect.data[idx].select == true) {
      if (msgCnt > 1) {
        spanMsg += "，．．．";
        break;
      }
      else if (msgCnt == 1) {
        spanMsg += "，";
      }
      spanMsg += oMselect.data[idx].name;
      msgCnt++;
    }
  }
  if (msgCnt == 0) {
    spanMsg = "未選択"
  }
  msgCnt  = 0;
  for (var idx = 0; idx < oMselect.data.length; idx++) {
    if (oMselect.data[idx].select == true) {
      if (msgCnt >= 1) {
        hiddenMsg += ",";
      }
      hiddenMsg += oMselect.data[idx].id;
      msgCnt++;
    }
  }
  mSelectJsonSet(oMselect.target, oMselect.data);
  var span = $(oMselect.target);
  if (span != undefined) {
    span.text(spanMsg);
  }
  var hidden = $(oMselect.hidden);
  if (hidden != undefined) {
    hidden.val(hiddenMsg).change();
  }
  var ms = $('#mselectsubmodal');
  if (ms != undefined) {
    ms.modal('close');
  }
  return true;
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダルコンバート
//------------------------------------------------------------------------------------------------------------------
function selectConvertJson(id) {
  for (var idx in oSelectJson) {
    if (id == oSelectJson[idx].id) {
      var dataJson = "";
      var datas = oSelectJson[idx].data;

      for (var idx = 0; idx < datas.length; idx++) {
        var data = datas[idx];
        if (data.select == true) {
          if (dataJson != "") {
            dataJson += ",";
          }
          dataJson += data.id;
        }
      }

      return dataJson;
    }
  }
  return "";
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダルコンバート
//------------------------------------------------------------------------------------------------------------------
function mSelectConvertJson(id) {
  for (var idx = 0; idx < oMselectJson.length; idx++) {
    if (id == oMselectJson[idx].id) {
      var dataJson = "";
      var datas = oMselectJson[idx].data;

      for (var idx = 0; idx < datas.length; idx++) {
        var data = datas[idx];
        if (data.select == true) {
          if (dataJson != "") {
            dataJson += ",";
          }
          dataJson += data.id;
        }
      }

      return dataJson;
    }
  }
  return "";
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダルJSON削除
//------------------------------------------------------------------------------------------------------------------
function selectJsonRemove(id) {
  for (var idx in oSelectJson) {
    if (id == oSelectJson[idx].id) {
      delete oSelectJson[idx];
      return;
    }
  }
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダルJSON取得
//------------------------------------------------------------------------------------------------------------------
function selectJsonGet(id) {
  for (var idx in oSelectJson) {
    if (id == oSelectJson[idx].id) {
      return oSelectJson[idx].data;
    }
  }
  return undefined;
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダルJSON取得
//------------------------------------------------------------------------------------------------------------------
function mSelectJsonGet(id) {
  for (var idx = 0; idx < oMselectJson.length; idx++) {
    if (id == oMselectJson[idx].id) {
      return oMselectJson[idx].data;
    }
  }
  return undefined;
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダルJSON更新
//------------------------------------------------------------------------------------------------------------------
function selectJsonSet(id, data) {
  for (var idx in oSelectJson) {
    if (id == oSelectJson[idx].id) {
      oSelectJson[idx].data = data;
      return;
    }
  }
  var jd = {id: id, data: data};
  oSelectJson.push(jd);
  return;
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダルJSON更新
//------------------------------------------------------------------------------------------------------------------
function mSelectJsonSet(id, data) {
  for (var idx = 0; idx < oMselectJson.length; idx++) {
    if (id == oMselectJson[idx].id) {
      oMselectJson[idx].data = data;
      return;
    }
  }
  var jd = {id: id, data: data};
  oMselectJson.push(jd);
  return;
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダルデータ取得
//------------------------------------------------------------------------------------------------------------------
function selectData(id) {
  for (var idx = 0; idx < oSelect.data.length; idx++) {
    if (id == oSelect.data[idx].id) {
      return oSelect.data[idx];
    }
  }
  return undefined;
}
//------------------------------------------------------------------------------------------------------------------
//- 指定ID以外の選択情報を未選択にする
//------------------------------------------------------------------------------------------------------------------
function unselectData(id) {
  for (var idx = 0; idx < oSelect.data.length; idx++) {
    if (id != oSelect.data[idx].id) {
      oSelect.data[idx].select = false;
    }
  }
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダルデータ取得
//------------------------------------------------------------------------------------------------------------------
function mSelectData(id) {
  for (var idx = 0; idx < oMselect.data.length; idx++) {
    if (id == oMselect.data[idx].id) {
      return oMselect.data[idx];
    }
  }
  return undefined;
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダル明細選択時
//------------------------------------------------------------------------------------------------------------------
function selectChange() {
  var item = $(this);
  var data = selectData(item.attr("dataid"));
  if (data != undefined) {
    data.select = true;
  }
  unselectData(item.attr("dataid"));
  selectClose();
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダル明細選択時
//------------------------------------------------------------------------------------------------------------------
function mSelectChange() {
  var item = $(this);
  var data = mSelectData(item.attr("dataid"));
  if (data != undefined) {
    var icon = item.children('i');
    if (data.select == true) {
      icon.removeClass('done');
      icon.text('add');
      icon.addClass('add');
      data.select = false;
    }
    else {
      icon.removeClass('add');
      icon.text('done');
      icon.addClass('done');
      data.select = true;
    }
  }
}
//------------------------------------------------------------------------------------------------------------------
//- セレクトモーダル(Sub)明細選択時
//------------------------------------------------------------------------------------------------------------------
function selectSubChange() {
  var item = $(this);
  var data = selectData(item.attr("dataid"));
  if (data != undefined) {
    data.select = true;
  }
  unselectData(item.attr("dataid"));
  selectSubClose();
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダル一括選択
//------------------------------------------------------------------------------------------------------------------
function mSelectAdd() {
  for (var idx = 0; idx < oMselect.data.length; idx++) {
    oMselect.data[idx].select = true;
  }
  var mitem = $(".mselect-item").children('i');
  mitem.removeClass('add');
  mitem.text('done');
  mitem.addClass('done');
}
//------------------------------------------------------------------------------------------------------------------
//- マルチセレクトモーダル一括解除
//------------------------------------------------------------------------------------------------------------------
function mSelectDel() {
  for (var idx = 0; idx < oMselect.data.length; idx++) {
    oMselect.data[idx].select = false;
  }
  var mitem = $(".mselect-item").children('i');
  mitem.removeClass('done');
  mitem.text('add');
  mitem.addClass('add');
}
//------------------------------------------------------------------------------------------------------------------
//----- カラーピッカーのインスタンス変数 -----
var oColor = {color: "", target: ""};
var oColorJson = [];
var oColorInfo = [
     {color: "#e53935"}
    ,{color: "#d81b60"}
    ,{color: "#8e24aa"}
    ,{color: "#5e35b1"}
    ,{color: "#3949ab"}
    ,{color: "#1e88e5"}
    ,{color: "#039be5"}
    ,{color: "#00acc1"}
    ,{color: "#00897b"}
    ,{color: "#43a047"}
    ,{color: "#7cb342"}
    ,{color: "#c0ca33"}
    ,{color: "#fdd835"}
    ,{color: "#ffb300"}
    ,{color: "#fb8c00"}
    ,{color: "#f4511e"}
    ,{color: "#6d4c41"}
    ,{color: "#757575"}
    ,{color: "#546e7a"}
    ];
//------------------------------------------------------------------------------------------------------------------
//- カラーピッカーオープン時
//------------------------------------------------------------------------------------------------------------------
function colorOpen() {
  var cm = $('#colormodal');
  if (cm != undefined) {
    cm.empty();
    cm.append('<div id="colorarea" class="modal-content">');  //モーダルコンテンツ領域を生成
    var ca = $('#colorarea');

    if (ca != undefined) {

      ca.append('<h6>' + $(this).attr("title") +'</h6>');     //コンテンツヘッダーを生成する

      for (var key in oColorInfo) {
        var color = oColorInfo[key];
        ca.append('<span class="color-item" style="background-color: ' + color.color + ';" color="' + color.color + '"></span>');
      }
      $(".color-item").unbind("click");
      $(".color-item").bind("click", colorChange);
      oColor.target = $(this).attr("displayspan");

      cm.append('<div id="colorfooter" class="modal-footer">');
      var mf = $('#colorfooter');
      if (mf != undefined) {
        mf.append('<a href="#!" id="colorback" class="waves-effect waves-green btn-flat" style="color: #eeeeee;">閉じる</a>');
        $('#colorback').unbind("click");
        $('#colorback').bind("click", colorClose);
      }

    }// end of init
    cm.modal('open');
  }
}
//------------------------------------------------------------------------------------------------------------------
//- カラーピッカー色選択時
//------------------------------------------------------------------------------------------------------------------
function colorChange() {
  var cg = $(this);

  oColor.color = cg.attr("color");
  var span = $(oColor.target);
  if (span != undefined) {
    span.attr("style", "color: " + oColor.color + ";");
    span.attr("color", oColor.color);
  }
  $(".color-item").removeClass("active");
  cg.addClass("active");

}
//------------------------------------------------------------------------------------------------------------------
//- カラーピッカー閉じる時
//------------------------------------------------------------------------------------------------------------------
function colorClose() {
  var cm = $('#colormodal');
  if (cm != undefined) {
    cm.modal('close');
  }
}
//------------------------------------------------------------------------------------------------------------------
//- 確認モーダル
//------------------------------------------------------------------------------------------------------------------
var confarmInfo = { returnCode: 0 };
//------------------------------------------------------------------------------------------------------------------
//- 確認モーダルオープン時
//------------------------------------------------------------------------------------------------------------------
function confarmOpen(title, message, main, sub, close, vmain, vsub, vclose) {

var ms = $('#confarmmodal');
  if (ms != undefined) {
    ms.empty();
    ms.append('<div id="comfarmmessagearea" class="modal-content">'); //モーダルコンテンツ領域を生成
    var ma = $('#comfarmmessagearea');

    if (ma != undefined) {

      ma.append('<h6>' + title +'</h6>');     //コンテンツヘッダーを生成する
      ma.append('<p>' + message +'</p>');     //コンテンツヘッダーを生成する
    }
    else {

    }
    ms.append('<div id="comfarmfooter" class="modal-footer">');
    var mf = $('#comfarmfooter');
    if (mf != undefined) {
      mf.append('<a href="#!" id="comfarmmain" class="comfarmback waves-effect waves-green btn-flat left" returnCode="'+ vmain +'">' + main +'</a>');
      mf.append('<a href="#!" id="comfarmsub"  class="comfarmback waves-effect waves-green btn-flat left" returnCode="'+ vsub +'">' + sub +'</a>');
      mf.append('<a href="#!" id="comfarmback" class="comfarmback waves-effect waves-green btn-flat" returnCode="'+ vclose +'">' + close +'</a>');
      $('.comfarmback').unbind("click");
      $('.comfarmback').bind("click", comfarmback);
    }
    confarmInfo.returnCode = 0;
    ms.modal('open');
    return false;
  }
}
//------------------------------------------------------------------------------------------------------------------
//- 確認モーダル閉じる時
//------------------------------------------------------------------------------------------------------------------
function comfarmback() {
  var ms = $('#confarmmodal');
  if (ms != undefined) {
    confarmInfo.returnCode = $(this).attr("returnCode");
    ms.modal('close');
  }
}
//------------------------------------------------------------------------------------------------------------------
