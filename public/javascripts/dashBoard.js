(function($){

  //---------------------------------------------------------------------------------------------------------------------------------
  // ダッシュボード
  //---------------------------------------------------------------------------------------------------------------------------------
  var boarddata = [];
  var boardinfo = {date:"", kukaku:0, init:true};

  $(function(){
  });

  $(document).ready(function(){

      console.log("DASHBOARD INIT STRAT");

      sleep(0.1, makeDashSearch());
      sleep(0.1, displayDashboard());

  }); //end of document.ready

  //---------------------------------------------------------------------------------------------------------------------------------
  // ダッシュボードエリアの生成
  //---------------------------------------------------------------------------------------------------------------------------------
  function makeDashSearch() {
    if (userinfo.manager == 0) {
      var mboard = $("#G0002MainDashBoard");
      mboard.empty();
      mboard.append('<i class="material-icons" style="color: #888888">block</i><span class="contents-none">閲覧権限がありません。</span>');
    }
    else {
      var mboard = $("#G0002MainDashBoard");
      var start = GetSystemDatePrevMonth(2);
      var end = GetSystemDate8();

      //----- 検索条件 -----
      mboard.append('<div id="G0002BoardSearch" class="row card-panel"></div>');
      var search = $("#G0002BoardSearch");
      search.append('<span class="title">絞込み条件</span>');
      //----- 期間指定 -----
      search.append('<div class="col s6 input-field">');
      search.append('<input type="text" placeholder="検索期間開始" id="G0002BoardF" class="datepicker input-text-color" style="">');
      search.append('</div>');
      search.append('<div class="col s6 input-field">');
      search.append('<input type="text" placeholder="検索期間終了" id="G0002BoardT" class="datepicker input-text-color" style="">');
      search.append('</div>');
      startDateP();
      var startstr = start.substr(0,4) + "/" + start.substr(4,2) + "/" + start.substr(6,2);
      $('#G0002BoardF').val(startstr);
      $('#G0002BoardF').datepicker('setDate', new Date(startstr));
      var endstr = end.substr(0,4) + "/" + end.substr(4,2) + "/" + end.substr(6,2);
      $('#G0002BoardT').val(endstr);
      $('#G0002BoardT').datepicker('setDate', new Date(endstr));
      $('#G0002BoardF').unbind("change");
      $('#G0002BoardF').bind("change", changeBoradDate);
      $('#G0002BoardT').unbind("change");
      $('#G0002BoardT').bind("change", changeBoradDate);
      //----- 生産物一覧 -----
      search.append('<div class="row">');
      search.append('<div class="col s12">');
      search.append('<span class="mselectmodal-trigger-title">品目</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="品目一覧" data="'+ userinfo.farm + '/getCrop" displayspan="#G0002BoardCrop" htext="#G0002BoardCropValue"><span id="G0002BoardCrop" class="blockquote-input">未選択</span></a>');
      search.append('<input type="hidden" id="G0002BoardCropValue" value="">');
      search.append('</div>');
      search.append('</div>');
      mSelectDataGet("#G0002BoardCrop", userinfo.farm + "/getCrop");
      mSelectClose();

      $('#G0002BoardCropValue').unbind("change");
      $('#G0002BoardCropValue').bind("change", changeBoradCrop);

      mboard.append('<div id="G0002BoardTotal" class="row card-panel"></div>');

      mboard.append('<div id="G0002BoardAccountUnit" class="row card-panel"></div>');
      displayDashboardAccount();

      mboard.append('<div id="G0002BoardMain" class="row card-panel"></div>');
      mboard.append('<div id="G0002BoardSub" class="row card-panel"></div>');

      mboard.append('<div id="G0002BoardSearchWork" class="row card-panel"></div>');
      var searchW = $("#G0002BoardSearchWork");
      searchW.append('<span class="title">絞込み条件(作業)</span>');

      //----- 担当者一覧 -----
      searchW.append('<div class="row">');
      searchW.append('<div class="col s12">');
      searchW.append('<span class="mselectmodal-trigger-title">担当者</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="担当者一覧" data="getAccountOfFarm" displayspan="#G0002BoardAccount" htext="#G0002BoardAccountValue"><span id="G0002BoardAccount" class="blockquote-input">未選択</span></a>');
      search.append('<input type="hidden" id="G0002BoardAccountValue" value="">');
      searchW.append('</div>');
      searchW.append('</div>');
      mSelectDataGet("#G0002BoardAccount", "getAccountOfFarm");
      mSelectClose();
      //----- 作業一覧 -----
      searchW.append('<div class="row">');
      searchW.append('<div class="col s12">');
      searchW.append('<span class="mselectmodal-trigger-title">作業</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="作業一覧" data="getWorkOfFarm" displayspan="#G0002BoardWork" htext="#G0002BoardWorkValue"><span id="G0002BoardWork" class="blockquote-input">未選択</span></a>');
      search.append('<input type="hidden" id="G0002BoardWorkValue" value="">');
      searchW.append('</div>');
      searchW.append('</div>');
      mSelectDataGet("#G0002BoardWork", "getWorkOfFarm");
      mSelectClose();
      //----- 生産物一覧 -----
      searchW.append('<div class="row">');
      searchW.append('<div class="col s12">');
      searchW.append('<span class="mselectmodal-trigger-title">品目</span><a href="#mselectmodal"  class="mselectmodal-trigger" title="品目一覧" data="'+ userinfo.farm + '/getCrop" displayspan="#G0002BoardWorkCrop" htext="#G0002BoardWorkCropValue"><span id="G0002BoardWorkCrop" class="blockquote-input">未選択</span></a>');
      search.append('<input type="hidden" id="G0002BoardWorkCropValue" value="">');
      searchW.append('</div>');
      searchW.append('</div>');
      mSelectDataGet("#G0002BoardWorkCrop", userinfo.farm + "/getCrop");
      mSelectClose();

      mboard.append('<div id="G0002BoardWorkG" class="row card-panel"></div>');

      $('.mselectmodal-trigger').unbind('click');
      $('.mselectmodal-trigger').bind('click', mSelectOpen);

      $('#G0002BoardAccountValue').unbind("change");
      $('#G0002BoardAccountValue').bind("change", boardWorkDisplay);
      $('#G0002BoardWorkValue').unbind("change");
      $('#G0002BoardWorkValue').bind("change", boardWorkDisplay);
      $('#G0002BoardWorkCropValue').unbind("change");
      $('#G0002BoardWorkCropValue').bind("change", boardWorkDisplay);
    }
  }
  //---------------------------------------------------------------------------------------------------------------------------------
  // ダッシュボード範囲指定日付の変更イベント
  //---------------------------------------------------------------------------------------------------------------------------------
  function changeBoradDate() {
    if (boardinfo.init) {
      return;
    }
    var start = $("#G0002BoardF").val();
    var end = $("#G0002BoardT").val();

    var diff = dateDiff(start, end);

    if (62 < diff) {
      displayToast('指定期間は62日以内を指定してください', 4000, '');
      return false;
    }

    displayDashboard();
    displayDashboardAccount();
    return true;

  }
  //---------------------------------------------------------------------------------------------------------------------------------
  // ダッシュボード生産物の変更イベント
  //---------------------------------------------------------------------------------------------------------------------------------
  function changeBoradCrop() {
    if (boardinfo.init) {
      return;
    }
    displayDashboard();
    return true;

  }
  //---------------------------------------------------------------------------------------------------------------------------------
  // 総収穫量の表示
  //---------------------------------------------------------------------------------------------------------------------------------
  function displayDashboardTotal() {
    //検索条件の確定
    var start = $("#G0002BoardF").val();
    start = start.replace(/\//g,'');
    var end = $("#G0002BoardT").val();
    end = end.replace(/\//g,'');

    var board = $("#G0002BoardTotal");
    board.empty();
    board.append('<span class="analysis">ただいま分析中...</span>');

    var url = "/" + userinfo.farm +"/" + start + "/" + end + "/TotalShukakuDataOutput"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

        board.empty();
        board.append('<span class="title">品目別収穫量</span>');
        board.append('<ul id="G0002BoardTotalList" class="totallist"></ul>');
        var boardt = $("#G0002BoardTotalList");
        var out = jsonResult.out;
        boardt.append('<li class="totallist-li"><div class="area" style="border-top: 4px solid rgba( 34, 34, 34, 0.4)"><span class="name">総収穫量</span></br><span class="shukakuRyo">' + jsonResult.shukakuRyo + 'Kg</span></div></li>');
        for (var key in out) {
          var data = out[key];
          var code  = data.color;
          var red   = parseInt(code.substring(0,2), 16);
          var green = parseInt(code.substring(2,4), 16);
          var blue  = parseInt(code.substring(4,6), 16);
          boardt.append('<li class="totallist-li"><div class="area" style="border-top: 4px solid rgba(' + red + ',' + green + ',' + blue + ' ,0.4)"><span class="name">' + data.name + '<span></br><span class="shukakuRyo">' + data.shukakuRyo + 'Kg<span></div></li>');
        }

    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  //---------------------------------------------------------------------------------------------------------------------------------
  // 日別収穫量表示
  //---------------------------------------------------------------------------------------------------------------------------------
  function displayDashboard() {
    if (userinfo.manager == 0) {
    }
    else {
      displayDashboardTotal();
      boardWorkDisplay();
      //検索条件の確定
      var start = $("#G0002BoardF").val();
      start = start.replace(/\//g,'');
      var end = $("#G0002BoardT").val();
      end = end.replace(/\//g,'');
      var crop   = mSelectConvertJson("#G0002BoardCrop");
      if (crop == null || crop == "") {
        crop = "NONE";
      }

      var board = $("#G0002BoardMain");
      board.empty();
      board.append('<span class="analysis">ただいま分析中...</span>');
      var boards = $("#G0002BoardSub");
      boards.empty();
      boards.append('<span class="analysis">ただいま分析中...</span>');

      var url = "/" + userinfo.farm +"/" + start + "/" + end + "/"  + crop +  "/ShukakuDataOutput"
      $.ajax({
        url:url,
        type:'GET',
        complete:function(data, status, jqXHR){           //処理成功時
          var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

          board.empty();
          board.append('<span class="title">日別収穫量</span>');
          board.append('<canvas id="dashboardcanvas" width="80%" height="60%"></canvas>');

          var labelJson   = [];
          var sdataJson    = [];
          var kdataJson   = [];
          var ndataJson   = [];
          var rdataJson   = [];
          var smax         = -1;
          var smin         = 0x7FFFFFFF;
          var kmax        = -1;
          var kmin        = 0x7FFFFFFF;
          var nmax        = -1;
          var nmin        = 0x7FFFFFFF;
          var rmax        = -1;
          var rmin        = 0x7FFFFFFF;
          var backStyle       = [];
          var borderStyle     = [];

          var out = jsonResult.out;
          boarddata = out;
          for (var key in out) {
            var data = out[key];
            labelJson.push(data.date);
            backStyle.push('rgba(230, 74, 25 , 0.2)');
            borderStyle.push('rgba(230, 74, 25 , 0.4)');
            sdataJson.push(data.shukakuryo);
            if (smax < data.shukakuryo) {
              smax = data.shukakuryo;
            }
            if (data.shukakuryo < smin) {
              smin = data.shukakuryo;
            }
            kdataJson.push(data.kion);
            if (kmax < data.kion) {
              kmax = data.kion;
            }
            if (data.kion < kmin) {
              kmin = data.kion;
            }
            ndataJson.push(data.seiiku);
            if (nmax < data.seiiku) {
              nmax = data.seiiku;
            }
            if (data.seiiku < nmin) {
              nmin = data.seiiku;
            }
            rdataJson.push(data.rain);
            if (rmax < data.rain) {
              rmax = data.rain;
            }
            if (data.rain < rmin) {
              rmin = data.rain;
            }
          }
          if (smax < kmax) {
            smax = kmax;
          }
          if (kmin < smin) {
            smin = kmin;
          }
          if (nmax < rmax) {
            nmax = rmax;
          }
          if (rmin < nmin) {
            nmin = rmin;
          }

          var ctx = document.getElementById('dashboardcanvas').getContext('2d');
          var myChart = new Chart(ctx, {
            type: 'bar',
            options: {
            scales: {
              yAxes: [{
                  id: "ya1",
                  type: "linear",
                  position: "left",
                  ticks: {
                    max: smax,
                    min: smin,
                    stepSize: (round((smax - smin) / 5, 1))
                  },
                },
                {
                  id: "ya2",
                  type: "linear",
                  position: "right",
                  ticks: {
                    max: nmax,
                    min: nmin,
                    stepSize: (round((nmax - nmin) / 5, 1))
                  },
                }],
              }
              },
              data: {
                labels: labelJson,
                datasets: [{
                  label: '収穫量',
                  data: sdataJson,
                  backgroundColor: 'rgba(230, 74, 25 , 0.1)',
                  borderColor: 'rgba(230, 74, 25 , 0.2)',
                  borderWidth: 1,
                  pointBorderColor: [
                      'rgba(128, 128, 128, 0.4)'
                  ],
                  yAxisID: "ya1",
                }
                ,{
                  label: '積算気温',
                  data: kdataJson,
                  backgroundColor: 'rgba(0, 0, 0 , 0)',
                  borderColor: 'rgba(245,124,0 ,0.4)',
                  borderWidth: 2,
                  pointBackgroundColor: 'rgba(245,124,0 ,0.4)',
                  pointBorderColor: 'rgba(245,124,0 ,0.4',
                  pointBorderWidth: 2,
                  pointStyle: 'rect',
                  yAxisID: "ya1"
                  ,type :'line'
                }
                ,{
                  label: '積算降水量',
                  data: rdataJson,
                  backgroundColor: 'rgba(0, 0, 0 , 0)',
                  borderColor: 'rgba(25,118,210 ,0.4)',
                  borderWidth: 2,
                  pointBackgroundColor: 'rgba(25,118,210 ,0.4)',
                  pointBorderColor: 'rgba(25,118,210 ,0.4)',
                  pointBorderWidth: 2,
                  pointStyle: 'triangle',
                  yAxisID: "ya2"
                  ,type :'line'
                }
               ,{
                  label: '生育日数',
                  data: ndataJson,
                  backgroundColor: 'rgba(0, 0, 0 , 0)',
                  borderColor: 'rgba(56,142,60 ,0.4)',
                  borderWidth: 2,
                  pointBackgroundColor: 'rgba(56,142,60 ,0.4)',
                  pointBorderColor: 'rgba(56,142,60 ,0.4)',
                  pointBorderWidth: 2,
                  pointStyle: 'circle',
                  yAxisID: "ya2"
                  ,type :'line'
              }]
            }
          });
          board.append('<span class="title">選択日詳細情報</span>');
          board.append('<ul id="dateselect" class="dateselect"></ul>');
          var ds = $("#dateselect");
          for (var key in out) {
            var data = out[key];
            ds.append('<li class="item" key=' + key + '>' + data.date + '&nbsp;&nbsp;(' + data.shukakuryo + ')</li>');
          }
          $("#dateselect li").unbind('click');
          $("#dateselect li").bind('click', boardDataSelect);
          board.append('<div id="dashrow"class="row"></div>');
          var ds = $("#dashrow");
          ds.append('<div id="dashshukaku" class="col s6 datainfo"><img class="icon"  src="/assets/image/shukaku_none.png"><span class="value" id="dashshukakuv">--</span>Kg</div>');
          ds.append('<div id="dashseiiku" class="col s6 datainfo"><img class="icon"  src="/assets/image/seiiku_none.png"><span class="value" id="dashseiikuv">--</span>日</div>');
          ds.append('<div id="dashsun" class="col s6 datainfo"><img class="icon"  src="/assets/image/sun_none.png"><span class="value" id="dashsunv">--</span>℃</div>');
          ds.append('<div id="dashrain" class="col s6 datainfo"><img class="icon"  src="/assets/image/rain_none.png"><span class="value" id="dashrainv">--</span>mm</div>');
          ds.append('<div id="dashsunave" class="col s6 datainfo"><img class="icon"  src="/assets/image/sunave_none.png"><span class="value" id="dashsunavev">--</span>℃</div>');
          boards.empty();
          boards.append('<span class="title">選択区画詳細情報</span>');
          boards.append('<ul id="kukakuselect" class="dateselect"></ul>');
          boards.append('<div id="dashrow-k"class="row"></div>');
          var dsk = $("#dashrow-k");
          dsk.append('<div id="dashshukaku-k" class="col s6 datainfo"><img class="icon"  src="/assets/image/shukaku_none.png"><span class="value" id="dashshukakuv-k">--</span>Kg</div>');
          dsk.append('<div id="dashseiiku-k" class="col s6 datainfo"><img class="icon"  src="/assets/image/seiiku_none.png"><span class="value" id="dashseiikuv-k">--</span>日</div>');
          dsk.append('<div id="dashsun-k" class="col s6 datainfo"><img class="icon"  src="/assets/image/sun_none.png"><span class="value" id="dashsunv-k">--</span>℃</div>');
          dsk.append('<div id="dashrain-k" class="col s6 datainfo"><img class="icon"  src="/assets/image/rain_none.png"><span class="value" id="dashrainv-k">--</span>mm</div>');
          dsk.append('<div id="dashsunave-k" class="col s6 datainfo"><img class="icon"  src="/assets/image/sunave_none.png"><span class="value" id="dashsunavev-k">--</span>℃</div>');
          boards.append('<canvas id="dashboardcanvas-k" width="80%" height="60%"></canvas>');
          boardinfo.init = false;
      },
        dataType:'json',
        contentType:'text/json'
      });
    }
  }
  function boardDataSelect() {
    var t = $(this);
    var key = t.attr("key");
    var data = boarddata[key];
    boardinfo.date = key;
    $("#dateselect li").removeClass('select');
    t.addClass('select');
    $("#dashshukakuv").text(data.shukakuryo);
    $("#dashseiikuv").text(data.seiiku);
    $("#dashsunv").text(data.kion);
    $("#dashrainv").text(data.rain);
    $("#dashsunavev").text(data.kionave);
    $("#dashshukakuv-k").text("--");
    $("#dashseiikuv-k").text("--");
    $("#dashsunv-k").text("--");
    $("#dashrainv-k").text("--");
    $("#dashsunavev-k").text("--");
    var ks = $("#kukakuselect");
    ks.empty();
    var kukakus = data.kukaku;
    for (var key in kukakus) {
      var data = kukakus[key];
      ks.append('<li class="item kukaku" key=' + key + '>' + data.name + '&nbsp;&nbsp;(' + data.shukakuryo + ')</li>');
    }
    $("#kukakuselect li").unbind('click');
    $("#kukakuselect li").bind('click', boardKukakuSelect);
  }
  function boardKukakuSelect() {
    var t = $(this);
    var key = t.attr("key");
    boardinfo.kukaku = key;
    var data = boarddata[boardinfo.date].kukaku[key];
    $("#kukakuselect li").removeClass('select');
    t.addClass('select');
    $("#dashshukakuv-k").text(data.shukakuryo);
    $("#dashseiikuv-k").text(data.seiiku);
    var dbs = $("#dashboardcanvas-k");
    if (dbs != undefined) {
      dbs.remove();
    }
    var board = $("#G0002BoardSub");
    board.append('<canvas id="dashboardcanvas-k" width="80%" height="60%"></canvas>');
    var url = "/" + boardinfo.kukaku + "/" + boardinfo.date + "/ShukakuKukakuDataOutput"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

        var labelJson   = [];
        var sdataJson    = [];
        var kdataJson   = [];
        var ndataJson   = [];
        var rdataJson   = [];
        var smax         = -1;
        var smin         = 0x7FFFFFFF;
        var kmax        = -1;
        var kmin        = 0x7FFFFFFF;
        var nmax        = -1;
        var nmin        = 0x7FFFFFFF;
        var rmax        = -1;
        var rmin        = 0x7FFFFFFF;
        var backStyle       = [];
        var borderStyle     = [];

        var out = jsonResult.out;
        $("#dashsunv-k").text(jsonResult.kion);
        $("#dashrainv-k").text(jsonResult.rain);
        $("#dashsunavev-k").text(jsonResult.kionave);
        for (var key in out) {
          var data = out[key];
          labelJson.push(data.date);
          backStyle.push('rgba(230, 74, 25 , 0.2)');
          borderStyle.push('rgba(230, 74, 25 , 0.4)');
          sdataJson.push(data.shukakuryo);
          if (smax < data.shukakuryo) {
            smax = data.shukakuryo;
          }
          if (data.shukakuryo < smin) {
            smin = data.shukakuryo;
          }
          kdataJson.push(data.kion);
          if (kmax < data.kion) {
            kmax = data.kion;
          }
          if (data.kion < kmin) {
            kmin = data.kion;
          }
          ndataJson.push(data.seiiku);
          if (nmax < data.seiiku) {
            nmax = data.seiiku;
          }
          if (data.seiiku < nmin) {
            nmin = data.seiiku;
          }
          rdataJson.push(data.rain);
          if (rmax < data.rain) {
            rmax = data.rain;
          }
          if (data.rain < rmin) {
            rmin = data.rain;
          }
        }
        if (smax < kmax) {
          smax = kmax;
        }
        if (kmin < smin) {
          smin = kmin;
        }
        if (nmax < rmax) {
          nmax = rmax;
        }
        if (rmin < nmin) {
          nmin = rmin;
        }

        var ctx = document.getElementById('dashboardcanvas-k').getContext('2d');
        var myChart = new Chart(ctx, {
          type: 'bar',
          options: {
          scales: {
            yAxes: [{
                id: "ya1",
                type: "linear",
                position: "left",
                ticks: {
                  max: smax,
                  min: smin,
                  stepSize: (round((smax - smin) / 5, 1))
                },
              },
              {
                id: "ya2",
                type: "linear",
                position: "right",
                ticks: {
                  max: nmax,
                  min: nmin,
                  stepSize: (round((nmax - nmin) / 5, 1))
                },
              }],
            }
            },
            data: {
              labels: labelJson,
              datasets: [{
                label: '収穫量',
                data: sdataJson,
                backgroundColor: 'rgba(230, 74, 25 , 0.1)',
                borderColor: 'rgba(230, 74, 25 , 0.2)',
                borderWidth: 1,
                pointBorderColor: [
                    'rgba(128, 128, 128, 0.4)'
                ],
                yAxisID: "ya1",
              }
              ,{
                label: '積算気温',
                data: kdataJson,
                backgroundColor: 'rgba(0, 0, 0 , 0)',
                borderColor: 'rgba(245,124,0 ,0.4)',
                borderWidth: 2,
                pointBackgroundColor: 'rgba(245,124,0 ,0.4)',
                pointBorderColor: 'rgba(245,124,0 ,0.4',
                pointBorderWidth: 2,
                pointStyle: 'rect',
                yAxisID: "ya1"
                ,type :'line'
              }
              ,{
                label: '積算降水量',
                data: rdataJson,
                backgroundColor: 'rgba(0, 0, 0 , 0)',
                borderColor: 'rgba(25,118,210 ,0.4)',
                borderWidth: 2,
                pointBackgroundColor: 'rgba(25,118,210 ,0.4)',
                pointBorderColor: 'rgba(25,118,210 ,0.4)',
                pointBorderWidth: 2,
                pointStyle: 'triangle',
                yAxisID: "ya2"
                ,type :'line'
              }
             ,{
                label: '生育日数',
                data: ndataJson,
                backgroundColor: 'rgba(0, 0, 0 , 0)',
                borderColor: 'rgba(56,142,60 ,0.4)',
                borderWidth: 2,
                pointBackgroundColor: 'rgba(56,142,60 ,0.4)',
                pointBorderColor: 'rgba(56,142,60 ,0.4)',
                pointBorderWidth: 2,
                pointStyle: 'circle',
                yAxisID: "ya2"
                ,type :'line'
             }]
          }
        });
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function boardWorkDisplay() {
    //検索条件の確定
    var start = $("#G0002BoardF").val();
    start = start.replace(/\//g,'');
    var end = $("#G0002BoardT").val();
    end = end.replace(/\//g,'');
    var account   = mSelectConvertJson("#G0002BoardAccount");
    if (account == null || account == "") {
      account = "NONE";
    }
    var work   = mSelectConvertJson("#G0002BoardWork");
    if (work == null || work == "") {
      work = "NONE";
    }
    var crop   = mSelectConvertJson("#G0002BoardWorkCrop");
    if (crop == null || crop == "") {
      crop = "NONE";
    }

    var board = $("#G0002BoardWorkG");
    board.empty();
    board.append('<span class="analysis">ただいま分析中...</span>');
    var url = "/" + userinfo.farm + "/" + start + "/" + end + "/" + account + "/" + work + "/" + crop + "/TotalWorkDataOutput"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

        board.empty();
        board.append('<canvas id="dashboardcanvas-w" width="80%" height="60%"></canvas>');

        var labelJson   = [];
        var sdataJson    = [];
        var smax         = -1;
        var smin         = 0x7FFFFFFF;
        var backStyle    = [];
        var borderStyle  = [];

        var out = jsonResult.out;

        //作業別時間の配列要素を作成する
        var workList = [];
        for (var key in out) {
          var data = out[key];
          var works = data.work;
          for (var keyW in works) {
            var work = works[keyW];
            var workJson = {"id": work.id, "name": work.name, "color": work.color, "time": []};
            workList.push(workJson);
          }
          break;
        }

        for (var key in out) {
          var data = out[key];
          labelJson.push(data.month);
          backStyle.push('rgba(230, 74, 25 , 0.2)');
          borderStyle.push('rgba(230, 74, 25 , 0.4)');
          sdataJson.push(data.time);
          if (smax < data.time) {
            smax = data.time;
          }
          if (data.time < smin) {
            smin = data.time;
          }
          //作業別の作業時間を集計
          var works = data.work;
          for (var keyW in works) {
            var work = works[keyW];
            workList.forEach(function( item ) {
              if (item.id == work.id) {
                item.time.push(work.time);
              }
            });
          }
        }
        //stackデータの生成
        var stackData = [];
        workList.forEach(function( item ) {
          var stackJson = { label: item.name, data: item.time, backgroundColor: '#' + item.color};
          stackData.push(stackJson);
        });

        var ctx = document.getElementById('dashboardcanvas-w').getContext('2d');
        var myChart = new Chart(ctx, {
          type: 'bar',
          options: {
          scales: {
            xAxes: [{
              stacked: true
            }],
            yAxes: [{
                stacked: true,
                id: "ya1",
                type: "linear",
                position: "left",
                ticks: {
                  max: smax,
                  min: smin,
                  stepSize: (round((smax - smin) / 5, 1))
                },
              }],
            }
            },
            data: {
              labels: labelJson,
              datasets: stackData
          }
        });
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  //---------------------------------------------------------------------------------------------------------------------------------
  // アカウント別能力指数表示
  //---------------------------------------------------------------------------------------------------------------------------------
  var accountUnitData;
  var accountCropData;
  function displayDashboardAccountDetailData() {
    var my = $(this);
    var data = accountUnitData[my.attr("key")];

    if (data.shukakuRyo == 0) {
      displayToast("収穫がありません。", 2000, "");
      return false;
    }

    var divTag = $('<div class="accountDetailData" />').attr("id", "G0002AccountDetailData");

    /*
     * スタイルを設定
     */
    divTag.css("z-index", "999")
          .css("position"         , "absolute")
          .css("width"            , "100%")
          .css("height"           , "100%")
          .css("top"              , "0px")
          .css("left"             , "0px")
          .css("right"            , "0px")
          .css("bottom"           , "0px")
          .css("background-color" , "rgba(34, 34, 34, 0.6)")
          .css("opacity"          , "1.0");

    divTag.append('<div class="row"><div class="col s12 m8 offset-m2 l6 offset-l3"><div class="card-panel accountDetailDataArea" id="G0002AccountDetailDataArea"></div></div></div>')
    $('body').append(divTag);

    scrollTo(0, 0);

    var area = $("#G0002AccountDetailDataArea");

    area.append('<span class="name">' + data.name + '</span><span class="date">(' + data.start + '～' + data.end + ')</span><span class="worktime">' + data.worktime + '&nbsp;時間</span>')
    area.append('<ul id="G0002AccountDetailDataList" class="collection"></ul>');
    var list = $("#G0002AccountDetailDataList");

    var crops = "";
    for (var cropKey in data.crops) {
      if (crops != "") {
        crops += ",";
      }
      crops += cropKey;
      var crop = data.crops[cropKey];
      var code  = crop.color;
      var red   = parseInt(code.substring(0,2), 16);
      var green = parseInt(code.substring(2,4), 16);
      var blue  = parseInt(code.substring(4,6), 16);
      var tcrop = getTotalCropData(crop.id, accountCropData);
      var point = Math.floor(((crop.unitSyukaku / tcrop.unitSyukaku) * 100));
      if (100 < point) {
        point = 100;
      }
      var skillc = "#1a237e";
      if (100 <= round(((crop.unitSyukaku / tcrop.unitSyukaku) * 100), 1)) {
        skillc = "#b71c1c";
      }
      list.append('<li class="collection-item accountDetailDataList-item" key="' + cropKey + '"><div class="area"><span class="crop" style="color: rgba(' + red + ',' + green + ',' + blue + ' ,0.8)" >' + crop.name + '</span><span class="shukakuRyo">' + crop.shukakuRyo + '&nbspKg</span><span class="worktime">' + crop.time + '&nbsp時間</span><span class="unitsyukaku">&nbsp;' + crop.unitSyukaku + '&nbsp;Kg/h&nbsp;</span><span class="skill" style="color: ' + skillc + '">&nbsp;' + round(((crop.unitSyukaku / tcrop.unitSyukaku) * 100), 1) + '&nbsp;Pt&nbsp;<div class="skillbar" style="background: rgba(' + red + ',' + green + ',' + blue + ' ,0.8); width: ' + point + '%;">&nbsp;</div></span></div></li>');
    }

    var start = $("#G0002BoardF").val();
    start = start.replace(/\//g,'');
    var end = $("#G0002BoardT").val();
    end = end.replace(/\//g,'');

    displayDashboardAccountDetailGrafh(my.attr("key"), crops, start, end);

  }
  function displayDashboardAccountDetailGrafh(accountId, cropId, start, end) {
    var board = $("#G0002AccountDetailDataArea");
    var url = "/" + accountId + "/" + cropId + "/" + start + "/" + end + "/AccountDateDataOutput"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

        board.append('<canvas id="dashboardacountdetailcanvas" width="80%" height="60%"></canvas>');

        var labelJson   = [];
        var tdataJson    = [];
        var smax         = -1;
        var smin         = 0x7FFFFFFF;
        var tmax         = -1;
        var tmin         = 0x7FFFFFFF;

        var out = jsonResult.out;

        var cropList = [];
        for (var key in out) {
          var data = out[key];
          var crops = data.crop;
          for (var keyC in crops) {
            var crop = crops[keyC];
            var cropJson = {"id": crop.id, "name": crop.name, "color": crop.color, "shukakuRyo": []};
            cropList.push(cropJson);
          }
          break;
        }

        for (var key in out) {
          var data = out[key];
          labelJson.push(data.date);
          if (smax < data.shukakuRyo) {
            smax = data.shukakuRyo;
          }
          if (data.shukakuRyo < smin) {
            smin = data.shukakuRyo;
          }
          tdataJson.push(data.time);
          if (tmax < data.time) {
            tmax = data.time;
          }
          if (data.time < tmin) {
            tmin = data.time;
          }
          //作業別の作業時間を集計
          var crops = data.crop;
          for (var keyC in crops) {
            var crop = crops[keyC];
            cropList.forEach(function( item ) {
              if (item.id == crop.id) {
                item.shukakuRyo.push(crop.shukakuRyo);
              }
            });
          }
        }
        smax = smax * 1.1;
        tmax = tmax * 1.1;
        //stackデータの生成
        var stackData = [];
        cropList.forEach(function( item ) {
          var code  = item.color;
          var red   = parseInt(code.substring(0,2), 16);
          var green = parseInt(code.substring(2,4), 16);
          var blue  = parseInt(code.substring(4,6), 16);
          var stackJson = { label: item.name, data: item.shukakuRyo, backgroundColor: 'rgba(' + red + ', ' + green + ', ' + blue + ', 0.6)', yAxisID: "ya1"};
          stackData.push(stackJson);
        });

        var lineJson = { type: 'line'
                       , label: "作業時間"
                       , data: tdataJson
                       , lineTension: 0
                       , borderColor: "rgba(25, 118, 210, 0.6)"
                       , borderWidth: "3"
                       , backgroundColor: "rgba(25, 118, 210, 0)"
                       , fill: false
                       , yAxisID: "ya2"
                       };

        stackData.push(lineJson);

        var ctx = document.getElementById('dashboardacountdetailcanvas').getContext('2d');
        var myChart = new Chart(ctx, {
          type: 'bar',
          options: {
          scales: {
            xAxes: [{
              stacked: true
            }],
            yAxes:
              [
               {
                stacked: true,
                id: "ya1",
                type: "linear",
                position: "left",
                ticks: {
                  max: smax,
                  min: smin,
                  stepSize: (round((smax - smin) / 5, 1))
                },
               }
               ,{
               id: "ya2",
               type: "linear",
               position: "right",
               ticks: {
                 max: tmax,
                 min: tmin,
                 stepSize: (round((tmax - tmin) / 5, 1))
               },
              }
              ],
            }
            },
            data: {
              labels: labelJson,
              datasets: stackData
          }
        });
        board.append('</br><a class="waves-effect waves-light btn-flat closebtn" id="G0002AccountDetailDataClose">閉じる</a>')
        $("#G0002AccountDetailDataClose").unbind("click");
        $("#G0002AccountDetailDataClose").bind("click", closeAccountDetailDataArea);
    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function closeAccountDetailDataArea() {
    $("#G0002AccountDetailData").remove();
  }
  function displayDashboardAccount() {
    //検索条件の確定
    var start = $("#G0002BoardF").val();
    start = start.replace(/\//g,'');
    var end = $("#G0002BoardT").val();
    end = end.replace(/\//g,'');

    var board = $("#G0002BoardAccountUnit");
    board.empty();
    board.append('<span class="analysis">ただいま分析中...</span>');

    accountUnitData = null;

    var url = "/" + userinfo.farm +"/" + start + "/" + end + "/AccountUnitDataOutput"
    $.ajax({
      url:url,
      type:'GET',
      complete:function(data, status, jqXHR){           //処理成功時
        var jsonResult  = JSON.parse( data.responseText );    //戻り値用JSONデータの生成

        board.empty();
        board.append('<span class="title">担当者別収穫量</span>');
        board.append('<ul id="G0002BoardAccountList" class="accountunit"></ul>');
        var boardt = $("#G0002BoardAccountList");
        var out = jsonResult.out;
        accountUnitData = jsonResult.out;
        accountCropData = jsonResult.crops;
        for (var key in out) {
          var data = out[key];
          var cropString = "";
          for (var cropKey in data.crops) {
            var crop = data.crops[cropKey];
            var code  = crop.color;
            var red   = parseInt(code.substring(0,2), 16);
            var green = parseInt(code.substring(2,4), 16);
            var blue  = parseInt(code.substring(4,6), 16);
            var tcrop = getTotalCropData(crop.id, jsonResult.crops);
            var skillc = "#1a237e";
            if (100 <= round(((crop.unitSyukaku / tcrop.unitSyukaku) * 100), 1)) {
              skillc = "#b71c1c";
            }

            cropString += '<div class="subarea"><span class="crop" style="color: rgba(' + red + ',' + green + ',' + blue + ' ,0.8)" >' + crop.name + '</span><span class="shukakuRyo">' + crop.shukakuRyo + '&nbsp;Kg</span><span class="unitsyukaku">' + crop.unitSyukaku + '&nbsp;Kg/h</span><span class="skill" style="color: ' + skillc + '">' + round(((crop.unitSyukaku / tcrop.unitSyukaku) * 100), 1)  + '&nbsp;Pt</span></div>'
          }
          boardt.append('<li class="accountunit-li" key="' + key + '"><div class="area"><span class="name">' + data.name + '<span>' + cropString + '</div></li>');
        }

        $(".accountunit-li").unbind( "click");
        $(".accountunit-li").bind( "click", displayDashboardAccountDetailData);

    },
      dataType:'json',
      contentType:'text/json'
    });
  }
  function getTotalCropData(id, list) {
    var result = null;
    for (var idx in list) {
      var data = list[idx];
      if (data.id == id) {
        result = data;
        break;
      }
    }
    return result;
  }
})(jQuery);
