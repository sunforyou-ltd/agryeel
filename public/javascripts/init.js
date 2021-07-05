(function($){
  $(function(){

    //***** ローディング表示 *****
    var windowHeight = $(window).height();                                  // 画面の高さ

    $('#LoadingArea').height(windowHeight).css('display','block');  		// ローディング画面を表示

  });
  $(document).ready(function(){
  }); // end of document ready

  //**********************************************************************
  //* ローディング完了時
  //**********************************************************************
  $(window).load(function () {


    $('#MainContents').css('display','none');
    $('.agryeel-loading').delay(600).fadeOut(500);
    $('.agryeel-loading-action').delay(900).fadeOut(500);
    $('#MainContents').delay(1500).fadeIn(1000);

  });


})(jQuery); // end of jQuery name space