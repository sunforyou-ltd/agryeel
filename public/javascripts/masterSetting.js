(function($){

  var groupInfo = [];

  $(function(){
  });

  $(document).ready(function(){

    init();

  }); //end of document.ready

  function init() {
    var ms = $("#masterSettinglist");
    ms.empty();
    ms.append('<ul id="masterlist" class="collection" style="border: 1px;">'); //マスタリストを生成する
    var ml = $("#masterlist");                                                 //マスタリストを取得する
    ml.append('<li class="collection-item menuitem master-trriger" gmnid="0" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">アカウント設定</span></li>');
    if (userinfo.manager != 0) {
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="16" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">生産者設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="1" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">地主設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="2" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">圃場グループ設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="3" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">圃場設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="4" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">区画設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="5" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">品目グループ設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="6" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">品目設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="7" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">農肥設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="8" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">品種設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="9" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">ベルト設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="10" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">機器設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="11" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">アタッチメント設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="12" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">荷姿設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="13" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">等級・質設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="14" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">階級・サイズ設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="15" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">資材設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="17" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">容器設定</span></li>');
      ml.append('<li class="collection-item menuitem master-trriger" gmnid="18" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#e1bee7;">settings</i><span style="color:#fafafa;">土設定</span></li>');
    }
    ml.append('<li id="masterclose" class="collection-item menuitem" style="border-left-color:#1a237e; background-color: #1a237e !important;"><i class="material-icons" style="color:#b2dfdb;">close</i><span style="color:#fafafa;">閉じる</span></li>');
    //- クローズメニューにイベントを設定する
    var cml = $('#masterclose');
    if (cml != undefined ) { //クローズメニューが存在する場合
      cml.unbind('click');
      cml.bind('click', ctlMainmenu);
    }
    var mst = $(".master-trriger");
    mst.unbind('click');
    mst.bind('click', menumove);
  }
  //------------------------------------------------------------------------------------------------------------------
  //- 画面遷移
  //------------------------------------------------------------------------------------------------------------------
  function menumove() {
    var item = $(this);
    var url = "/" + userinfo.farm + "/" + item.attr("gmnid") + "/masterMntMove";

    window.location.href = url;
  }
})(jQuery);