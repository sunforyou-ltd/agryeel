var targetField;					//数値入力対象オブジェクト
var initFlag;						//初回入力フラグ

(function($){

  $(document).ready(function(){
    //------------------------------------------------------------------------------------------------------------------
    //- 電卓の自動生成
    //------------------------------------------------------------------------------------------------------------------
    //- 電卓エリアを取得する -
    var cm = $("#calcModal");
    if (cm != undefined ) { // 該当ページに電卓エリアが存在する
      cm.modal();
    }
  }); //end of document.ready

  /* 数値タップイベント */
  function pushCalcButton(value) {
    if (initFlag) { //初回数値入力時のみ
      $("#calc-text").text("");
    }
    $("#calc-text").text($("#calc-text").text() + value);
    initFlag = false;
  }
  /* クリアタップイベント */
  function pushClearButton() {
    $("#calc-text").text("");
    initFlag = false;
  }
  /* バックスペースタップイベント */
  function pushBackSpaceButton() {
    $("#calc-text").text($("#calc-text").text().slice(0,($("#calc-text").text().length - 1)));
    initFlag = false;
  }

  /* 数値入力用電卓初期表示イベント */
  $(function() {

    /* 各ボタンにイベントを割り当てる */
    $("#calc0").click(function(){
      pushCalcButton("0");
    } );
    $("#calc00").click(function(){
      pushCalcButton("00");
    } );
    $("#calcP").click(function(){
      pushCalcButton(".");
    } );
    $("#calc1").click(function(){
      pushCalcButton("1");
    } );
    $("#calc2").click(function(){
      pushCalcButton("2");
    } );
    $("#calc3").click(function(){
      pushCalcButton("3");
    } );
    $("#calc4").click(function(){
      pushCalcButton("4");
    } );
    $("#calc5").click(function(){
      pushCalcButton("5");
    } );
    $("#calc6").click(function(){
      pushCalcButton("6");
    } );
    $("#calc7").click(function(){
      pushCalcButton("7");
    } );
    $("#calc8").click(function(){
      pushCalcButton("8");
    } );
    $("#calc9").click(function(){
      pushCalcButton("9");
    } );

    $("#calcClear").click(function(){
      pushClearButton();
    } );
    $("#calcBS").click(function(){
      pushBackSpaceButton();
    } );
    $("#calcCommit").click(function(){
      if ($("#calc-text").text() == "") {
        targetField.val('0').change();
        $("#" + targetField.attr("id") + "Span").html('0');
      }
      else {
        targetField.val($("#calc-text").text()).change();
        $("#" + targetField.attr("id") + "Span").html($("#calc-text").text());
      }
      $("#calcModal").modal('close');
    } );

  });

})(jQuery); // end of jQuery name space

/* 電卓機能初期化イベント */
function CalcInit() {
  /* 電卓アイコンタップ時イベント */
  $('.calcTarget').click(function(){
      targetField = $("#" + $(this).attr("targetId"));											/* 対象Rangeを特定 */
      initFlag = true;																			/* 初回数値タップフラグをON */
      $("#calc-text").text(targetField.val());													/* 電卓機能に現在値を格納 */
      $("#calcModal").modal('open');																/* 電卓機能を表示 */
    });
}

