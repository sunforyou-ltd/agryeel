/* AGRYEEL 収量まとめ画面 JQUERY */
(function($){
	  var displayMode = 0;	/* 現在表示画面モード */
	  var placeMode = 0;	/* 現在検索条件場所モード */
	  var unitMode = 0;		/* 現在検索条件場所モード */
	  var nowPage = 1;		/* 現在ページ */
	  var pageCnt = 0;  	/* ページ数 */
	  var selYear = 0;  	/* 指定年 */
	  var selMonth = 0;  	/* 指定月 */
	  var selDay = 1;  		/* 指定日 */

	  /* 初期処理時イベント */
	  $(function(){

	    $('.collapsible').collapsible({
	      accordion : true // A setting that changes the collapsible behavior to expandable instead of the default accordion style
	    });

	    $('select').material_select();

  	    var lineChartDataClip = {
  	      labels : ["A-1","A-2","A-3","B-4","B-5","C-6","C-7","C-8","C-9","D-10"],
  	      datasets : [
  	        {
  	          fillColor : "rgba(174,213,129,0.3)",
  	          strokeColor : "rgba(174,213,129,0.5)",
  	          data : [432,600,782,400,400,400,400,400,400,400]
  	        }
  	      ]
  	    };
  	    var lineChartDataTotal = {
  	      labels : ["A-1","A-2","A-3","B-4","B-5","C-6","C-7","C-8","C-9","D-10"],
  	      datasets : [
  	        {
  	          fillColor : "rgba(229,115,115,0.3)",
  	          strokeColor : "rgba(229,115,115,0.5)",
  	          data : [3192,4072,4304,5620,5620,5620,5620,5620,5620,5620]
  	        }
  	      ]
  	    };

  	    //myLine = new Chart(document.getElementById("sample5").getContext("2d")).Bar(lineChartDataClip);
  	    myLine = new Chart(document.getElementById("sample6").getContext("2d")).Bar(lineChartDataTotal);

	    displayMode = 0;  		/* 初期表示を一覧表にする */
	    placeMode = 1;			/* 初期検索条件を場所にする */
	    unitMode = 1;			/* 初期検索条件を作付にする */
	    nowPage = 1;			/* 処理ページを1にする */
	    selDay = 1;				/* 処理指定日を「1～15」にする */
	    changeDetail();   		/* 初期表示詳細を切替える */
  	    changeDisplay();  		/* 初期表示画面を切替える */

	    /* 表示画面切替ナビクリック時 */
	    $('.display-change').click(function() {
	      displayMode = parseInt($(this).attr('displaymode'));  /* 表示画面を格納する */
		  placeMode = 1;			/* 初期検索条件を場所にする */
		  unitMode = 1;			/* 初期検索条件を作付にする */
		  nowPage = 1;			/* 処理ページを1にする */
		  selDay = 1;				/* 処理指定日を「1～15」にする */
	      changeDisplay();  		//表示画面を切替える
	      changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表場所：場所ラジオボタン選択時 */
	    $("#placeKukakuList").change( function() {
	    	placeMode = 1;			// 場所検索条件を場所にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表場所：畝ラジオボタン選択時 */
	    $("#placeUneList").change( function() {
	    	placeMode = 2;			// 場所検索条件を畝にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表場所：条ラジオボタン選択時 */
	    $("#placeJoList").change( function() {
	    	placeMode = 3;			// 場所検索条件を条にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表場所：株ラジオボタン選択時 */
	    $("#placeKabuList").change( function() {
	    	placeMode = 4;			// 場所検索条件を株にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表場所：担当者ラジオボタン選択時 */
	    $("#placeAccountList").change( function() {
	    	placeMode = 5;			// 場所検索条件を担当者にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表単位：作付ラジオボタン選択時 */
	    $("#unitSakuList").change( function() {
	    	unitMode = 1;			// 単位検索条件を作付にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表単位：年ラジオボタン選択時 */
	    $("#unitYearList").change( function() {
	    	unitMode = 2;			// 単位検索条件を年にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表単位：月ラジオボタン選択時 */
	    $("#unitMonthList").change( function() {
	    	unitMode = 3;			// 単位検索条件を月にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* 一覧表単位：日ラジオボタン選択時 */
	    $("#unitDayList").change( function() {
	    	unitMode = 4;			// 単位検索条件を日にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ場所：場所ラジオボタン選択時 */
	    $("#placeKukakuGraph").change( function() {
	    	placeMode = 1;			// 場所検索条件を場所にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ場所：畝ラジオボタン選択時 */
	    $("#placeUneGraph").change( function() {
	    	placeMode = 2;			// 場所検索条件を畝にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ場所：条ラジオボタン選択時 */
	    $("#placeJoGraph").change( function() {
	    	placeMode = 3;			// 場所検索条件を条にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ場所：株ラジオボタン選択時 */
	    $("#placeKabuGraph").change( function() {
	    	placeMode = 4;			// 場所検索条件を株にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ場所：担当者ラジオボタン選択時 */
	    $("#placeAccountGraph").change( function() {
	    	placeMode = 5;			// 場所検索条件を担当者にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ単位：作付ラジオボタン選択時 */
	    $("#unitSakuGraph").change( function() {
	    	unitMode = 1;			// 単位検索条件を作付にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ単位：年ラジオボタン選択時 */
	    $("#unitYearGraph").change( function() {
	    	unitMode = 2;			// 単位検索条件を年にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ単位：月ラジオボタン選択時 */
	    $("#unitMonthGraph").change( function() {
	    	unitMode = 3;			// 単位検索条件を月にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* グラフ単位：日ラジオボタン選択時 */
	    $("#unitDayGraph").change( function() {
	    	unitMode = 4;			// 単位検索条件を日にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ場所：場所ラジオボタン選択時 */
	    $("#placeKukakuClip").change( function() {
	    	placeMode = 1;			// 場所検索条件を場所にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ場所：畝ラジオボタン選択時 */
	    $("#placeUneClip").change( function() {
	    	placeMode = 2;			// 場所検索条件を畝にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ場所：条ラジオボタン選択時 */
	    $("#placeJoClip").change( function() {
	    	placeMode = 3;			// 場所検索条件を条にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ場所：株ラジオボタン選択時 */
	    $("#placeKabuClip").change( function() {
	    	placeMode = 4;			// 場所検索条件を株にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ場所：担当者ラジオボタン選択時 */
	    $("#placeAccountClip").change( function() {
	    	placeMode = 5;			// 場所検索条件を担当者にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ単位：作付ラジオボタン選択時 */
	    $("#unitSakuClip").change( function() {
	    	unitMode = 1;			// 単位検索条件を作付にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ単位：年ラジオボタン選択時 */
	    $("#unitYearClip").change( function() {
	    	unitMode = 2;			// 単位検索条件を年にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ単位：月ラジオボタン選択時 */
	    $("#unitMonthClip").change( function() {
	    	unitMode = 3;			// 単位検索条件を月にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });

	    /* クリップ単位：日ラジオボタン選択時 */
	    $("#unitDayClip").change( function() {
	    	unitMode = 4;			// 単位検索条件を日にする
		    nowPage = 1;			// 処理ページを1にする
	    	changeDetail();   		// 表示詳細を切替える
	    });
	  }); // end of document ready

	  /* 表示内容を切替る */
	  function changeDetail() {
        /* 収量まとめ情報取得 */
	    var inputJson = StringToJson('{"disp":"' + displayMode + '", "place":"' + placeMode + '", "unit":"' + unitMode + '", "selyear":"' + selYear + '", "selmonth":"' + selMonth + '", "selday":"' + selDay + '"}');

	    $.ajax({
	      url:"/syuryoSummaryGet", 										//収量まとめ情報取得処理
	        type:'POST',
	        data:JSON.stringify(inputJson),								//入力用JSONデータ
	        complete:function(data, status, jqXHR){						//処理成功時
	        var jsonResult = JSON.parse( data.responseText );			//戻り値用JSONデータの生成
	        var syuryoSummaryList 	= jsonResult.syuryoSummary;			//収量まとめリスト
	        var unitCnt			 	= jsonResult.unitCnt;				//単位数
		    var htmlString	= "";										//可変HTML文字列

	        /* HTML可変部作成 */
		    //タイトル
		    htmlString = '<div class="row">';
		    htmlString += '<div class="col s12">';
            var title = '収量まとめ';
            var headTitle = '';
            var unitOfPage = 0;											//1ページ最大単位
            var placeOfGraph = 0;										//1グラフ最大場所
            switch (displayMode) {
            case 0:
                /* 一覧表 */
                title += '一覧表';
                break;
            case 1:
                /* グラフ */
                title += 'グラフ';
                break;
            default:
                /* クリップ */
                title += 'クリップ';
                break;
            }
            switch (placeMode) {
            case 1:
                /* 場所別 */
            	title += '（場所別';
            	headTitle = '場所';
                break;
            case 2:
                /* 畝別 */
                title += '（畝別';
            	headTitle = '畝';
                break;
            case 3:
                /* 条別 */
                title += '（条別';
            	headTitle = '条';
                break;
            case 4:
                /* 株別 */
                title += '（株別';
            	headTitle = '株';
                break;
            default:
                /* 担当者 */
                title += '（担当者別';
        		headTitle = '担当者';
                break;
            }
            switch (unitMode) {
            case 1:
                /* 作付単位 */
            	title += '作付単位）';
            	/*★使用端末毎の１ページ表示サイズ処理は後で★*/
            	unitOfPage = 10;
            	placeOfGraph = 30;
                break;
            case 2:
                /* 年単位 */
                title += '年単位）';
            	/*★使用端末毎の１ページ表示サイズ処理は後で★*/
                unitOfPage = 8;
            	placeOfGraph = 30;
                break;
            case 3:
                /* 月単位 */
                title += '月単位）';
            	/*★使用端末毎の１ページ表示サイズ処理は後で★*/
                unitOfPage = 12;
            	placeOfGraph = 30;
                break;
            default:
                /* 日単位 */
                title += '日単位）';
	        	/*★使用端末毎の１ページ表示サイズ処理は後で★*/
            	unitOfPage = 15;
            	placeOfGraph = 30;
                break;
            }
		    htmlString += '<div class="card-panel green lighten-2 white-text">' + title + '</div>';
		    htmlString += '</div></div>';

		    /*---- 【一覧表】 ----*/
		    if(displayMode == 0){
		    	var htmlStringDetail	= "";										//可変HTML文字列(明細用)
		    	var htmlStringTotal		= "";										//可変HTML文字列(合計用)
		    	if(unitMode == 1 || unitMode == 2){
		    		//作付/年単位
				    //ページング上部
			    	if(unitCnt / unitOfPage != 0){
			    		pageCnt = unitCnt / unitOfPage;
			    		if(unitCnt % unitOfPage != 0){
			    			pageCnt++;
			    		}
					    htmlString += '<div class="row">';
					    htmlString += '<div class="right">';
					    htmlString += '<ul class="pagination">';
					    htmlString += '<li class="disabled"><a href="#!"><i class="mdi-navigation-chevron-left"></i></a></li>';
					    for(var i=1; i <= pageCnt; i++){
					    	if(i == nowPage){
					    		htmlString += '<li class="active"><a id="G0006PageBtn" class="page-change" page="' + i + '">' + i + '</a></li>';
					    	}else{
							    htmlString += '<li class="waves-effect"><a id="G0006PageBtn" class="page-change" page="' + i + '">' + i + '</a></li>';
					    	}
					    }
					    htmlString += '<li class="waves-effect"><a href="#!"><i class="mdi-navigation-chevron-right"></i></a></li>';
					    htmlString += '</ul></div></div>';
			    	}
		    	}else if(unitMode == 3){
		    		//月単位
		    		var minYear = jsonResult.unitMin;				//最小単位
		    		var maxYear = jsonResult.unitMax;				//最大単位
		    		var workYear = jsonResult.workYear;				//作業年
		    		if(selYear == 0){
		    			selYear = workYear;
		    		}

		    		// 年月コンボボックス作成
		    		htmlString += '<div class="row">';
		    		htmlString += '<div class="col s6 m6 l7">';
		    		htmlString += '<div class="right">';
		    		htmlString += '<div class="input-field col s6 m6 l8">';
		    		htmlString += '<select id="G0006WorkYear">';
		    		for(var i=minYear; i <= maxYear; i++){
	            		if(selYear == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div></div></div>';
		    	}else if(unitMode == 4){
		    		//日単位
		    		var minYearMonth = jsonResult.unitMin;				//最小単位
		    		var maxYearMonth = jsonResult.unitMax;				//最大単位
		    		var workYear = jsonResult.workYear;					//作業年
		    		var workMonth = jsonResult.workMonth;				//作業月
		    		var lastDay = jsonResult.lastDay;					//末日
		    		if(selYear == 0 || selMonth == 0){
		    			selYear = workYear;
		    			selMonth = workMonth;
		    		}

		    		//年月日コンボボックス作成
		    		htmlString += '<div class="row">';
		    		htmlString += '<div class="input-field col l5">';
		    		htmlString += '<div class="right">';
		    		htmlString += '<select id="G0006WorkYear">';
		    		var preYear = 0;
		    		var preMonth = 0;
		    		var minYear = minYearMonth.toString().substring(0,4);
		    		var maxYear = maxYearMonth.toString().substring(0,4);
		    		var minMonth = minYearMonth.toString().substring(4);
		    		var maxMonth = maxYearMonth.toString().substring(4);
		    		//年
		    		for(var i=minYear; i <= maxYear; i++){
	            		if(selYear == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div>';
		    		htmlString += '<div class="input-field col s3 m3 l1">';
		    		htmlString += '<div class="left">';
		    		htmlString += '<select id="G0006WorkMonth">';
		    		//月
		    		for(var i=1; i <= 12; i++){
		    			if((selYear == minYear && i < minMonth) ||
		    			   (selYear == maxYear && i > maxMonth)){
		    			    continue;
		    			}

	            		if(selMonth == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div>';
		    		htmlString += '<div class="input-field col s3 m3 l1">';
		    		htmlString += '<div class="left">';
		    		htmlString += '<select id="G0006WorkDay">';
		    		//日
            		if(selDay == 1){
            			htmlString += '<option value="1" selected>1～15</option>';
            		}else{
            			htmlString += '<option value="1">1～15</option>';
            		}
              		if(selDay == 2){
        				htmlString += '<option value="2" selected>16～' + lastDay + '</option>';
	        		}else{
	        			htmlString += '<option value="2">16～' + lastDay + '</option>';
	        		}
//            		if(lastDay == 31){
//                		if(selDay == 3){
//                			htmlString += '<option value="3" selected>31</option>';
//                		}else{
//                			htmlString += '<option value="3">31</option>';
//                		}
//            		}
		    		htmlString += '</select></div></div></div>';
		    	}
			    /* 一覧ヘッダ部 */
			    htmlString += '<div class="row">';
			    htmlString += '<div class="col s12">';
			    htmlString += '<table class="centered bordered">';
			    htmlString += '<thead>';
			    htmlString += '<tr class="grey lighten-5">';
			    htmlString += '<th data-field="id" width="100">' + headTitle + '</th>';
			    if(unitMode == 1){
			    	htmlString += '<th data-field="id" width="50">  </th>';
			    }
			    if(unitMode != 4){
			    	htmlString += '<th data-field="id" width="100">合計</th>';
			    }else{
				    htmlString += '<th data-field="id" width="67">合計</th>';
			    }
			    /* 一覧明細部 */
			    var preUnit = 0;
			    var unitcnt = 0;
			    var minunit = 0;
			    var maxunit = 0;
	            switch (unitMode) {
	            case 1:
	                /* ①作付単位 */
	            	minunit = (unitOfPage * nowPage) - (unitOfPage - 1);
	            	maxunit = unitOfPage * nowPage;
	            	for(var i=minunit; i <= maxunit; i++){
	            		htmlString += '<th data-field="id" width="100">' + i + '</th>';
	            	}

				    htmlString += '<tbody>';
				    for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var kaitensu = syuryoSummary["rotationSpeedOfYear"];

				    	if(kaitensu != 99 && (kaitensu < minunit || kaitensu > maxunit)){
				    		continue;
				    	}

				    	//前データとIDが違えば新規行作成
				    	if(preUnit != syuryoSummary["placeId"]){
				    		if(preUnit != 0){
				    			//1ページ単位数以下の場合、空列を追加
				    			if(unitcnt != unitOfPage){
				    				for(var i=unitcnt+1; i <= unitOfPage; i++){
				    					htmlStringDetail += '<td></td>';
				    				}
				    			}
				    			htmlString += htmlStringTotal;
				    			htmlString += htmlStringDetail;
							    htmlString += '</tr>';
							    unitcnt = 0;
							    htmlStringTotal = '';
							    htmlStringDetail = '';
				    		}
				    		htmlString += '<tr>';
						    htmlString += '<td><a href="#!" kukakuId=' + syuryoSummary["placeId"] + '>' + syuryoSummary["placeName"] + '</a></td>';
				            //クリップグループによるフローティングボタン色判定
				            var clipColor = '';					//クリップフローティングボタン背景色
				            switch (syuryoSummary["clipResult"]) {
				              case 1:
				                  /* 赤 */
				                clipColor = 'red';
				                break;
				              default:
				                /* 青 */
				                clipColor = 'blue';
				                break;
				            }
						    htmlString += '<td><a class="btn-floating waves-effect waves-light ' + clipColor + ' darken-1 syuryo-working-floationg-detail" kukakuId=' + syuryoSummary["placeId"] + '><i class="small mdi-editor-attach-file"></i></a></td>';
						    preUnit = syuryoSummary["placeId"];
				    	}

				    	//合計用収穫量保持
				    	if(syuryoSummary["rotationSpeedOfYear"] == 99){
				    		htmlStringTotal += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    	}else{
				    		htmlStringDetail += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    		unitcnt++;
				    	}
				    }
	    			//1ページ単位数以下の場合、空列を追加
	    			if(unitcnt != unitOfPage){
	    				for(var i=unitcnt+1; i <= unitOfPage; i++){
	    					htmlStringDetail += '<td></td>';
	    				}
	    			}
	    			htmlString += htmlStringTotal;
	    			htmlString += htmlStringDetail;
				    htmlString += '</tr>';
				    htmlString += '</tbody></table></div></div>';
	                break;
	            case 2:
	                /* ②年単位 */
	    	        var minYear = jsonResult.unitMin;				//最小単位
	            	minunit = (unitOfPage * nowPage) - (unitOfPage - 1) + (minYear - 1);
	            	maxunit = (unitOfPage * nowPage) + (minYear - 1);
	            	for(var i=minunit; i <= maxunit; i++){
	            		htmlString += '<th data-field="id" width="100">' + i + '</th>';
	            	}

				    htmlString += '<tbody>';
				    for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var year = syuryoSummary["workYear"];

				    	if(year != 2999 && (year < minunit || year > maxunit)){
				    		continue;
				    	}

				    	//前データとIDが違えば新規行作成
				    	if(preUnit != syuryoSummary["placeId"]){
				    		if(preUnit != 0){
				    			//1ページ単位数以下の場合、空列を追加
				    			if(unitcnt != unitOfPage){
				    				for(var i=unitcnt+1; i <= unitOfPage; i++){
				    					htmlStringDetail += '<td></td>';
				    				}
				    			}
				    			htmlString += htmlStringTotal;
				    			htmlString += htmlStringDetail;
							    htmlString += '</tr>';
							    unitcnt = 0;
							    htmlStringTotal = '';
							    htmlStringDetail = '';
				    		}
				    		htmlString += '<tr>';
						    htmlString += '<td><a href="#!" kukakuId=' + syuryoSummary["placeId"] + '>' + syuryoSummary["placeName"] + '</a></td>';
						    preUnit = syuryoSummary["placeId"];
				    	}

				    	//合計用収穫量保持
				    	if(syuryoSummary["workYear"] == 2999){
				    		htmlStringTotal += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    	}else{
				    		if((minunit + unitcnt) >= syuryoSummary["minYear"]){
				    			htmlStringDetail += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    		}else{
				    			// 最小年まで空白表示
				    			for(var i=minunit; i < syuryoSummary["minYear"]; i++){
				    				htmlStringDetail += '<td></td>';
				    				unitcnt++;
				    			}
				    			htmlStringDetail += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    		}
				    		unitcnt++;
				    	}
				    }
	    			//1ページ単位数以下の場合、空列を追加
	    			if(unitcnt != unitOfPage){
	    				for(var i=unitcnt+1; i <= unitOfPage; i++){
	    					htmlStringDetail += '<td></td>';
	    				}
	    			}
	    			htmlString += htmlStringTotal;
	    			htmlString += htmlStringDetail;
				    htmlString += '</tr>';
				    htmlString += '</tbody></table></div></div>';
	                break;
	            case 3:
	                /* ③月単位 */
	    	        var minYear = jsonResult.unitMin;				//最小単位
	            	minunit = 1;
	            	maxunit = 12;
	            	for(var i=minunit; i <= maxunit; i++){
	            		htmlString += '<th data-field="id" width="100">' + i + '</th>';
	            	}

				    htmlString += '<tbody>';
				    for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var yearmonth = syuryoSummary["workYearMonth"];
				    	var day = yearmonth.substring(6);

				    	//前データとIDが違えば新規行作成
				    	if(preUnit != syuryoSummary["placeId"]){
				    		if(preUnit != 0){
				    			//1ページ単位数以下の場合、空列を追加
				    			if(unitcnt != unitOfPage){
				    				for(var i=unitcnt+1; i <= unitOfPage; i++){
				    					htmlStringDetail += '<td></td>';
				    				}
				    			}
				    			htmlString += htmlStringTotal;
				    			htmlString += htmlStringDetail;
							    htmlString += '</tr>';
							    unitcnt = 0;
							    htmlStringTotal = '';
							    htmlStringDetail = '';
				    		}
				    		htmlString += '<tr>';
						    htmlString += '<td><a href="#!" kukakuId=' + syuryoSummary["placeId"] + '>' + syuryoSummary["placeName"] + '</a></td>';
						    preUnit = syuryoSummary["placeId"];
				    	}

				    	//合計用収穫量保持
				    	if(day != "01"){
				    		htmlStringTotal += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    	}else{
				    		htmlStringDetail += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    		unitcnt++;
				    	}
				    }
	    			//1ページ単位数以下の場合、空列を追加
	    			if(unitcnt != unitOfPage){
	    				for(var i=unitcnt+1; i <= unitOfPage; i++){
	    					htmlStringDetail += '<td></td>';
	    				}
	    			}
	    			htmlString += htmlStringTotal;
	    			htmlString += htmlStringDetail;
				    htmlString += '</tr>';
				    htmlString += '</tbody></table></div></div>';
	                break;
	            default:
	                /* ④日単位 */
		    		var minYearMonth = jsonResult.unitMin;				//最小単位
	            	var maxYearMonth = jsonResult.unitMax;				//最大単位
	            	var workYear = jsonResult.workYear;					//作業年
	            	var workMonth = jsonResult.workMonth;				//作業月
	            	var lastDay = jsonResult.lastDay;					//末日
	            	var minDay = 0										//最小日
	            	var maxDay = 0										//最大日

	            	minunit = 1;
	            	maxunit = 15;

	            	if(selDay == 1){		//1～15日
	            		minDay = 1;
	            		maxDay = 15;
	            	}else if(selDay == 2){	//16～末日
	            		minDay = 16;
	            		maxDay = lastDay;
		            	if(lastDay == 31){
		            		unitOfPage = 16;
		            		maxunit = 16;
		            	}
	            	}
	            	else{					//31日
	            		minDay = 31;
	            		maxDay = 31;
	            	}

	            	//ヘッダタイトル生成
	            	var dispDay = minDay;
	            	for(var i=minunit; i <= maxunit; i++){
	            		if(dispDay > maxDay){
	            			htmlString += '<th data-field="id" width="67"></th>';
	            		}else{
		            		htmlString += '<th data-field="id" width="67">' + dispDay + '</th>';
	            		}
	            		dispDay++;
	            	}

				    htmlString += '<tbody>';
				    for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var workDate = syuryoSummary["workDate"];
				    	var chkYear = workDate.substring(0,2);
				    	var chkDay = workDate.substring(6);

				    	// 表示対象日でない場合スキップ
				    	if(chkYear != "25" &&
				    	   (minDay > chkDay || maxDay < chkDay)){
				    		continue;
				    	}

				    	//前データとIDが違えば新規行作成
				    	if(preUnit != syuryoSummary["placeId"]){
				    		if(preUnit != 0){
				    			//1ページ単位数以下の場合、空列を追加
				    			if(unitcnt != unitOfPage){
				    				for(var i=unitcnt+1; i <= unitOfPage; i++){
				    					htmlStringDetail += '<td></td>';
				    				}
				    			}
				    			htmlString += htmlStringTotal;
				    			htmlString += htmlStringDetail;
							    htmlString += '</tr>';
							    unitcnt = 0;
							    htmlStringTotal = '';
							    htmlStringDetail = '';
				    		}
				    		htmlString += '<tr>';
						    htmlString += '<td><a href="#!" kukakuId=' + syuryoSummary["placeId"] + '>' + syuryoSummary["placeName"] + '</a></td>';
						    preUnit = syuryoSummary["placeId"];
				    	}

				    	//合計用収穫量保持
				    	if(chkYear == "25"){
				    		htmlStringTotal += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    	}else{
				    		htmlStringDetail += '<td>' + syuryoSummary["totalShukakuCount"] + '</td>';
				    		unitcnt++;
				    	}
				    }
	    			//1ページ単位数以下の場合、空列を追加
	    			if(unitcnt != unitOfPage){
	    				for(var i=unitcnt+1; i <= unitOfPage; i++){
	    					htmlStringDetail += '<td></td>';
	    				}
	    			}
	    			htmlString += htmlStringTotal;
	    			htmlString += htmlStringDetail;
				    htmlString += '</tr>';
				    htmlString += '</tbody></table></div></div>';
	                break;
	            }

			    //ページング下部
		    	if(unitMode == 1 || unitMode == 2){
			    	if(unitCnt / unitOfPage != 0){
			    		pageCnt = unitCnt / unitOfPage;
			    		if(unitCnt % unitOfPage != 0){
			    			pageCnt++;
			    		}
					    htmlString += '<div class="row">';
					    htmlString += '<div class="right">';
					    htmlString += '<ul class="pagination">';
					    htmlString += '<li class="disabled"><a href="#!"><i class="mdi-navigation-chevron-left"></i></a></li>';
					    for(var i=1; i <= pageCnt; i++){
					    	if(i == nowPage){
					    		htmlString += '<li class="active"><a id="G0006PageBtn" class="page-change" page="' + i + '">' + i + '</a></li>';
					    	}else{
							    htmlString += '<li class="waves-effect"><a id="G0006PageBtn" class="page-change" page="' + i + '">' + i + '</a></li>';
					    	}
					    }
					    htmlString += '<li class="waves-effect"><a href="#!"><i class="mdi-navigation-chevron-right"></i></a></li>';
					    htmlString += '</ul></div></div>';
			    	}
		    	}

				$("#G0006List").html(htmlString);					//可変HTML部分に反映する



	        }
		    /*---- 【グラフ】 ----*/
		    else if(displayMode == 1){
	            switch (unitMode) {
	            case 1:
	                /* ①作付単位 */
			    	//対象作付の場所数取得
			    	var placeCnt = 0;
			    	var prePlace = 0;
	                for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var kaitensu = syuryoSummary["rotationSpeedOfYear"];

				    	if(kaitensu == 99 || nowPage != syuryoSummary["rotationSpeedOfYear"]){
				    		continue;
				    	}

				    	if(prePlace != syuryoSummary["placeId"]){
				    		prePlace = syuryoSummary["placeId"];
				    		placeCnt++;
				    	}
	                }

	                //可変HTML部分作成
				    htmlString += '<div class="row">';
				    htmlString += '<div class="right">';
				    htmlString += '<ul class="pagination">';
				    htmlString += '<li class="disabled"><a href="#!"><i class="mdi-navigation-chevron-left"></i></a></li>';
				    for(var i=1; i <= unitCnt; i++){
				    	if(i == nowPage){
				    		htmlString += '<li class="active"><a id="G0006PageBtn" class="page-change" page="' + i + '">' + i + '</a></li>';
				    	}else{
						    htmlString += '<li class="waves-effect"><a id="G0006PageBtn" class="page-change" page="' + i + '">' + i + '</a></li>';
				    	}
				    }
				    htmlString += '<li class="waves-effect"><a href="#!"><i class="mdi-navigation-chevron-right"></i></a></li>';
				    htmlString += '</ul></div></div>';
			    	htmlString += '<div class="row">';
			    	htmlString += '<div class="col s12">';
			    	htmlString += '<div class="center">';
			    	htmlString += '<div>-- 作付' + nowPage + '回転 --</div>';

	                //1ページ当たりのグラフ数の取得
	                var graphcnt = Math.floor(placeCnt / placeOfGraph);									//グラフ数
	                if(placeCnt - (graphcnt * placeOfGraph) != 0){
	                	graphcnt++;
	                }

	                for(var i =0;i<graphcnt;i++){
	                	if(i % 2 == 0){
					    	htmlString += '<div class="row">';
	                	}
				    	htmlString += '<div class="col s12 m12 l6">';
				    	htmlString += '<canvas id="G0006GraphDetail' + (i + 1) + '" class="syuryo-canvas"></canvas><br>';
				    	htmlString += '</div>';
	                	if(i % 2 != 0){
					    	htmlString += '</div>';
	                	}
	                }

			  	    $("#G0006Graph").html(htmlString);					//可変HTML部分に反映する

			  	    //グラフデータの作成
                    var grafhData			= new Object();								//グラフデータ
                    var grafhDataList		= new Array();								//グラフデータリスト
                    var linegrafhData		= new Object();								//ライングラフデータ
                    var graphLabelList	 	= new Array();
                    var graphDataList	 	= new Array();
                    var totalcnt = 0;
                    var placecnt = 0;
                    graphcnt = 0;
				    for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var kaitensu = syuryoSummary["rotationSpeedOfYear"];

				    	if(kaitensu == 99 || nowPage != syuryoSummary["rotationSpeedOfYear"]){
				    		continue;
				    	}

				    	//サブタイトル
				    	if(totalcnt == 0){
					    	htmlString += '<div class="row">';
					    	htmlString += '<div class="col s12">';
					    	htmlString += '<div class="center">';
					    	htmlString += '<div>-- 作付' + kaitensu + '回転 --</div>';
				    	}

				    	//新規グラフの作成
				    	if(placecnt == 0){
				            linegrafhData	= new Object();								//ライングラフデータ
				    		grafhData		= new Object();								//グラフデータ
			                grafhData["fillColor"]		= "rgba(229,115,115,0.3)";		//描画色
			                grafhData["strokeColor"]	= "rgba(229,115,115,0.5)";		//背景色
				    	}
			    		graphLabelList.push(syuryoSummary["placeName"]);			//ラベルデータ
		                graphDataList.push(syuryoSummary["totalShukakuCount"]);		//収穫量

				    	totalcnt++;
				    	placecnt++;

				    	//1グラフ最大の場合グラフ出力
				    	if(placecnt == placeOfGraph){
				    		linegrafhData["labels"] = graphLabelList;		//ラベルを作成する
				    		grafhData["data"]		= graphDataList;		//収穫量データ
			                grafhDataList.push(grafhData);					//グラフデータリストに追加
			                linegrafhData["datasets"] = grafhDataList;		//データセットを格納する
			                var myChart  = new Chart(document.getElementById("G0006GraphDetail" + (graphcnt + 1)).getContext("2d")).Bar(linegrafhData);
			                graphcnt++;
			                placecnt = 0;
				    	}
				    }

	    			//残りのグラフ出力
	    			if(placecnt != 0){
	    				//1グラフ最大数に満たない分の空データ生成
	    				for(var i =0;i<(placeOfGraph - placecnt);i++){
				    		graphLabelList.push(" ");			//ラベルデータ
			                graphDataList.push(null);			//収穫量
	    				}

			    		linegrafhData["labels"] = graphLabelList;		//ラベルを作成する
			    		grafhData["data"]		= graphDataList;		//収穫量データ
		                grafhDataList.push(grafhData);					//グラフデータリストに追加
		                linegrafhData["datasets"] = grafhDataList;		//データセットを格納する
	    				var myChart  = new Chart(document.getElementById("G0006GraphDetail" + (graphcnt + 1)).getContext("2d")).Bar(linegrafhData);
	    			}
	                break;
	            case 2:
		    		/* ②年単位 */
		    		var minYear = jsonResult.unitMin;				//最小単位
		    		var maxYear = jsonResult.unitMax;				//最大単位
		    		var workYear = jsonResult.workYear;				//作業年
		    		if(selYear == 0){
		    			selYear = workYear;
		    		}

		    		// 年月コンボボックス作成
		    		htmlString += '<div class="row">';
		    		htmlString += '<div class="col s6 m6 l7">';
		    		htmlString += '<div class="right">';
		    		htmlString += '<div class="col s6 m6 l8">';
		    		htmlString += '<select id="G0006WorkYear">';
		    		for(var i=minYear; i <= maxYear; i++){
	            		if(selYear == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div></div></div>';

			    	//対象年の場所数取得
			    	var placeCnt = 0;
			    	var prePlace = 0;
	                for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var year = syuryoSummary["workYear"];

				    	if(year == 2999 || year != selYear){
				    		continue;
				    	}

				    	if(prePlace != syuryoSummary["placeId"]){
				    		prePlace = syuryoSummary["placeId"];
				    		placeCnt++;
				    	}
	                }

	                //可変HTML部分作成
	                //1ページ当たりのグラフ数の取得
	                var graphcnt = Math.floor(placeCnt / placeOfGraph);									//グラフ数
	                if(placeCnt - (graphcnt * placeOfGraph) != 0){
	                	graphcnt++;
	                }

	                for(var i =0;i<graphcnt;i++){
	                	if(i % 2 == 0){
					    	htmlString += '<div class="row">';
	                	}
				    	htmlString += '<div class="col s12 m12 l6">';
				    	htmlString += '<canvas id="G0006GraphDetail' + (i + 1) + '" class="syuryo-canvas"></canvas><br>';
				    	htmlString += '</div>';
	                	if(i % 2 != 0){
					    	htmlString += '</div>';
	                	}
	                }

			  	    $("#G0006Graph").html(htmlString);					//可変HTML部分に反映する

			  	    //グラフデータの作成
                    var grafhData			= new Object();								//グラフデータ
                    var grafhDataList		= new Array();								//グラフデータリスト
                    var linegrafhData		= new Object();								//ライングラフデータ
                    var graphLabelList	 	= new Array();
                    var graphDataList	 	= new Array();
                    var totalcnt = 0;
                    var placecnt = 0;
                    graphcnt = 0;
				    for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var year = syuryoSummary["workYear"];

				    	if(year == 2999 || year != selYear){
				    		continue;
				    	}

				    	//新規グラフの作成
				    	if(placecnt == 0){
				            linegrafhData	= new Object();								//ライングラフデータ
				    		grafhData		= new Object();								//グラフデータ
			                grafhData["fillColor"]		= "rgba(229,115,115,0.3)";		//描画色
			                grafhData["strokeColor"]	= "rgba(229,115,115,0.5)";		//背景色
				    	}
			    		graphLabelList.push(syuryoSummary["placeName"]);			//ラベルデータ
		                graphDataList.push(syuryoSummary["totalShukakuCount"]);		//収穫量

				    	totalcnt++;
				    	placecnt++;

				    	//1グラフ最大の場合グラフ出力
				    	if(placecnt == placeOfGraph){
				    		linegrafhData["labels"] = graphLabelList;		//ラベルを作成する
				    		grafhData["data"]		= graphDataList;		//収穫量データ
			                grafhDataList.push(grafhData);					//グラフデータリストに追加
			                linegrafhData["datasets"] = grafhDataList;		//データセットを格納する
			                var myChart  = new Chart(document.getElementById("G0006GraphDetail" + (graphcnt + 1)).getContext("2d")).Bar(linegrafhData);
			                graphcnt++;
			                placecnt = 0;
				    	}
				    }

	    			//残りのグラフ出力
	    			if(placecnt != 0){
	    				//1グラフ最大数に満たない分の空データ生成
	    				for(var i =0;i<(placeOfGraph - placecnt);i++){
				    		graphLabelList.push(" ");			//ラベルデータ
			                graphDataList.push(null);			//収穫量
	    				}

			    		linegrafhData["labels"] = graphLabelList;		//ラベルを作成する
			    		grafhData["data"]		= graphDataList;		//収穫量データ
		                grafhDataList.push(grafhData);					//グラフデータリストに追加
		                linegrafhData["datasets"] = grafhDataList;		//データセットを格納する
	    				var myChart  = new Chart(document.getElementById("G0006GraphDetail" + (graphcnt + 1)).getContext("2d")).Bar(linegrafhData);
	    			}
	                break;
	            case 3:
		    		/* ③月単位 */
		    		var minYearMonth = jsonResult.unitMin;				//最小単位
		    		var maxYearMonth = jsonResult.unitMax;				//最大単位
		    		var workYear = jsonResult.workYear;					//作業年
		    		var workMonth = jsonResult.workMonth;				//作業月
		    		if(selYear == 0 || selMonth == 0){
		    			selYear = workYear;
		    			selMonth = workMonth;
		    		}

		    		//年月コンボボックス作成
		    		htmlString += '<div class="row">';
		    		htmlString += '<div class="input-field col l5">';
		    		htmlString += '<div class="right">';
		    		htmlString += '<select id="G0006WorkYear">';
		    		var preYear = 0;
		    		var preMonth = 0;
		    		var minYear = minYearMonth.toString().substring(0,4);
		    		var maxYear = maxYearMonth.toString().substring(0,4);
		    		var minMonth = minYearMonth.toString().substring(4);
		    		var maxMonth = maxYearMonth.toString().substring(4);
		    		//年
		    		for(var i=minYear; i <= maxYear; i++){
	            		if(selYear == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div>';
		    		htmlString += '<div class="input-field col s3 m3 l1">';
		    		htmlString += '<div class="left">';
		    		htmlString += '<select id="G0006WorkMonth">';
		    		//月
		    		for(var i=1; i <= 12; i++){
		    			if((selYear == minYear && i < minMonth) ||
		    			   (selYear == maxYear && i > maxMonth)){
		    			    continue;
		    			}

	            		if(selMonth == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div></div>';

			    	//対象年月の場所数取得
			    	var placeCnt = 0;
			    	var prePlace = 0;
	                for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var year = syuryoSummary["workYearMonth"].toString().substring(0,4);
				    	var month = syuryoSummary["workYearMonth"].toString().substring(4);

				    	if(Number(year) != selYear || Number(month) != selMonth){
				    		continue;
				    	}

				    	if(prePlace != syuryoSummary["placeId"]){
				    		prePlace = syuryoSummary["placeId"];
				    		placeCnt++;
				    	}
	                }

	                //可変HTML部分作成
	                //1ページ当たりのグラフ数の取得
	                var graphcnt = Math.floor(placeCnt / placeOfGraph);									//グラフ数
	                if(placeCnt - (graphcnt * placeOfGraph) != 0){
	                	graphcnt++;
	                }

	                for(var i =0;i<graphcnt;i++){
	                	if(i % 2 == 0){
					    	htmlString += '<div class="row">';
	                	}
				    	htmlString += '<div class="col s12 m12 l6">';
				    	htmlString += '<canvas id="G0006GraphDetail' + (i + 1) + '" class="syuryo-canvas"></canvas><br>';
				    	htmlString += '</div>';
	                	if(i % 2 != 0){
					    	htmlString += '</div>';
	                	}
	                }

			  	    $("#G0006Graph").html(htmlString);					//可変HTML部分に反映する

			  	    //グラフデータの作成
                    var grafhData			= new Object();								//グラフデータ
                    var grafhDataList		= new Array();								//グラフデータリスト
                    var linegrafhData		= new Object();								//ライングラフデータ
                    var graphLabelList	 	= new Array();
                    var graphDataList	 	= new Array();
                    var totalcnt = 0;
                    var placecnt = 0;
                    graphcnt = 0;
				    for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var year = syuryoSummary["workYearMonth"].toString().substring(0,4);
				    	var month = syuryoSummary["workYearMonth"].toString().substring(4);

				    	if(Number(year) != selYear || Number(month) != selMonth){
				    		continue;
				    	}

				    	//新規グラフの作成
				    	if(placecnt == 0){
				            linegrafhData	= new Object();								//ライングラフデータ
				    		grafhData		= new Object();								//グラフデータ
			                grafhData["fillColor"]		= "rgba(229,115,115,0.3)";		//描画色
			                grafhData["strokeColor"]	= "rgba(229,115,115,0.5)";		//背景色
				    	}
			    		graphLabelList.push(syuryoSummary["placeName"]);			//ラベルデータ
		                graphDataList.push(syuryoSummary["totalShukakuCount"]);		//収穫量

				    	totalcnt++;
				    	placecnt++;

				    	//1グラフ最大の場合グラフ出力
				    	if(placecnt == placeOfGraph){
				    		linegrafhData["labels"] = graphLabelList;		//ラベルを作成する
				    		grafhData["data"]		= graphDataList;		//収穫量データ
			                grafhDataList.push(grafhData);					//グラフデータリストに追加
			                linegrafhData["datasets"] = grafhDataList;		//データセットを格納する
			                var myChart  = new Chart(document.getElementById("G0006GraphDetail" + (graphcnt + 1)).getContext("2d")).Bar(linegrafhData);
			                graphcnt++;
			                placecnt = 0;
				    	}
				    }

	    			//残りのグラフ出力
	    			if(placecnt != 0){
	    				//1グラフ最大数に満たない分の空データ生成
	    				for(var i =0;i<(placeOfGraph - placecnt);i++){
				    		graphLabelList.push(" ");			//ラベルデータ
			                graphDataList.push(null);			//収穫量
	    				}

			    		linegrafhData["labels"] = graphLabelList;		//ラベルを作成する
			    		grafhData["data"]		= graphDataList;		//収穫量データ
		                grafhDataList.push(grafhData);					//グラフデータリストに追加
		                linegrafhData["datasets"] = grafhDataList;		//データセットを格納する
	    				var myChart  = new Chart(document.getElementById("G0006GraphDetail" + (graphcnt + 1)).getContext("2d")).Bar(linegrafhData);
	    			}
	                break;
	            case 4:
	                /* ④日単位 */
		    		var minYearMonth = jsonResult.unitMin;				//最小単位
		    		var maxYearMonth = jsonResult.unitMax;				//最大単位
		    		var workYear = jsonResult.workYear;					//作業年
		    		var workMonth = jsonResult.workMonth;				//作業月
		    		var workDay = jsonResult.workDay;					//作業日
		    		var lastDay = jsonResult.lastDay;					//末日
		    		if(selYear == 0 || selMonth == 0 || selDay == 0){
		    			selYear = workYear;
		    			selMonth = workMonth;
		    			selDay = workDay;
		    		}

		    		//年月日コンボボックス作成
		    		htmlString += '<div class="row">';
		    		htmlString += '<div class="input-field col l5">';
		    		htmlString += '<div class="right">';
		    		htmlString += '<select id="G0006WorkYear">';
		    		var preYear = 0;
		    		var preMonth = 0;
		    		var minYear = minYearMonth.toString().substring(0,4);
		    		var maxYear = maxYearMonth.toString().substring(0,4);
		    		var minMonth = minYearMonth.toString().substring(4);
		    		var maxMonth = maxYearMonth.toString().substring(4);
		    		//年
		    		for(var i=minYear; i <= maxYear; i++){
	            		if(selYear == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div>';
		    		htmlString += '<div class="input-field col s3 m3 l1">';
		    		htmlString += '<div class="left">';
		    		htmlString += '<select id="G0006WorkMonth">';
		    		//月
		    		for(var i=1; i <= 12; i++){
		    			if((selYear == minYear && i < minMonth) ||
		    			   (selYear == maxYear && i > maxMonth)){
		    			    continue;
		    			}

	            		if(selMonth == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div>';
		    		htmlString += '<div class="input-field col s3 m3 l1">';
		    		htmlString += '<div class="left">';
		    		htmlString += '<select id="G0006WorkDay">';
		    		//日
		    		for(var i=1; i <= lastDay; i++){
	            		if(selDay == i){
	            			htmlString += '<option value="' + i + '" selected>' + i + '</option>';
	            		}else{
	            			htmlString += '<option value="' + i + '">' + i + '</option>';
	            		}
	            	}
		    		htmlString += '</select></div></div></div>';

			    	//対象年月の場所数取得
			    	var placeCnt = 0;
			    	var prePlace = 0;
	                for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var year = syuryoSummary["workDate"].toString().substring(0,4);
				    	var month = syuryoSummary["workDate"].toString().substring(4,6);
				    	var day = syuryoSummary["workDate"].toString().substring(6);

				    	if(Number(year) != selYear || Number(month) != selMonth || Number(day) != selDay){
				    		continue;
				    	}

				    	if(prePlace != syuryoSummary["placeId"]){
				    		prePlace = syuryoSummary["placeId"];
				    		placeCnt++;
				    	}
	                }

	                //可変HTML部分作成
	                //1ページ当たりのグラフ数の取得
	                var graphcnt = Math.floor(placeCnt / placeOfGraph);									//グラフ数
	                if(placeCnt - (graphcnt * placeOfGraph) != 0){
	                	graphcnt++;
	                }

	                for(var i =0;i<graphcnt;i++){
	                	if(i % 2 == 0){
					    	htmlString += '<div class="row">';
	                	}
				    	htmlString += '<div class="col s12 m12 l6">';
				    	htmlString += '<canvas id="G0006GraphDetail' + (i + 1) + '" class="syuryo-canvas"></canvas><br>';
				    	htmlString += '</div>';
	                	if(i % 2 != 0){
					    	htmlString += '</div>';
	                	}
	                }

			  	    $("#G0006Graph").html(htmlString);					//可変HTML部分に反映する

			  	    //グラフデータの作成
                    var grafhData			= new Object();								//グラフデータ
                    var grafhDataList		= new Array();								//グラフデータリスト
                    var linegrafhData		= new Object();								//ライングラフデータ
                    var graphLabelList	 	= new Array();
                    var graphDataList	 	= new Array();
                    var totalcnt = 0;
                    var placecnt = 0;
                    graphcnt = 0;
				    for ( var syuryoSummaryKey in syuryoSummaryList ) {								//収量まとめ件数分処理を行う
				    	var syuryoSummary = syuryoSummaryList[syuryoSummaryKey];	  			    //収量まとめ情報の取得
				    	var year = syuryoSummary["workDate"].toString().substring(0,4);
				    	var month = syuryoSummary["workDate"].toString().substring(4,6);
				    	var day = syuryoSummary["workDate"].toString().substring(6);

				    	if(Number(year) != selYear || Number(month) != selMonth || Number(day) != selDay){
				    		continue;
				    	}

				    	//新規グラフの作成
				    	if(placecnt == 0){
				            linegrafhData	= new Object();								//ライングラフデータ
				    		grafhData		= new Object();								//グラフデータ
			                grafhData["fillColor"]		= "rgba(229,115,115,0.3)";		//描画色
			                grafhData["strokeColor"]	= "rgba(229,115,115,0.5)";		//背景色
				    	}
			    		graphLabelList.push(syuryoSummary["placeName"]);			//ラベルデータ
		                graphDataList.push(syuryoSummary["totalShukakuCount"]);		//収穫量

				    	totalcnt++;
				    	placecnt++;

				    	//1グラフ最大の場合グラフ出力
				    	if(placecnt == placeOfGraph){
				    		linegrafhData["labels"] = graphLabelList;		//ラベルを作成する
				    		grafhData["data"]		= graphDataList;		//収穫量データ
			                grafhDataList.push(grafhData);					//グラフデータリストに追加
			                linegrafhData["datasets"] = grafhDataList;		//データセットを格納する
			                var myChart  = new Chart(document.getElementById("G0006GraphDetail" + (graphcnt + 1)).getContext("2d")).Bar(linegrafhData);
			                graphcnt++;
			                placecnt = 0;
				    	}
				    }

	    			//残りのグラフ出力
	    			if(placecnt != 0){
	    				//1グラフ最大数に満たない分の空データ生成
	    				for(var i =0;i<(placeOfGraph - placecnt);i++){
				    		graphLabelList.push(" ");			//ラベルデータ
			                graphDataList.push(null);			//収穫量
	    				}

			    		linegrafhData["labels"] = graphLabelList;		//ラベルを作成する
			    		grafhData["data"]		= graphDataList;		//収穫量データ
		                grafhDataList.push(grafhData);					//グラフデータリストに追加
		                linegrafhData["datasets"] = grafhDataList;		//データセットを格納する
	    				var myChart  = new Chart(document.getElementById("G0006GraphDetail" + (graphcnt + 1)).getContext("2d")).Bar(linegrafhData);
	    			}
	            	break;
	            default:
	                break;
	            }
		    }


	        },
	        dataType:'json',
	        contentType:'text/json',
	        async: false
	      });

	      $('select').material_select('update');

		    /* 年コンボボックス選択時 */
		    $("#G0006WorkYear").change( function() {
		    	selYear = $(this).val();
		    	changeDetail();   		// 表示詳細を切替える
		    });

		    /* 月コンボボックス選択時 */
		    $("#G0006WorkMonth").change( function() {
		    	selMonth = $(this).val();
		    	changeDetail();   		// 表示詳細を切替える
		    });

		    /* 日コンボボックス選択時 */
		    $("#G0006WorkDay").change( function() {
		    	selDay = $(this).val();
		    	changeDetail();   		// 表示詳細を切替える
		    });

	      /* ページボタンクリック時 */
	      $('.page-change').click(function() {
		    if(nowPage != $(this).attr("page")){
			    nowPage = $(this).attr("page");
			    changeDetail();   		/* 詳細を切替える */
		    }
	      });

          /* 収量まとめ一覧表クリップボタンタップ時イベント */
          $('.syuryo-working-floationg-detail').click(function() {
              /* 関連区画IDの取得 */
              var jsonData			= new Object();										//入力用JSONデータ
              jsonData["kukakuId"]	= $(this).attr("kukakuId");							//区画IDを格納

              var result 		= "";													//収集結果JSONDATA

              result += "{"
              result += '"kukakuId":"' + $(this).attr("kukakuId") + '"';				//入力項目をJSONDATAに出力
              result += "}"

              jsonData = new Function("return " + result)();							//Json変換

              floatingButton = $(this);													//フローティングオブジェクトを格納

              $.ajax({
                  url:"./addClip", 											//クリップ登録処理
                  type:'POST',
                  data:JSON.stringify(jsonData),							//入力用JSONデータ
                  complete:function(data, status, jqXHR){					//処理成功時
                    var jsonResult 	= JSON.parse( data.responseText );		//戻り値用JSONデータの生成

                    /* フローティングボタン制御 */
                    switch (jsonResult["clipResult"]) {
                      case 1:
                        /* 青から濃紫に変化する */
                        floatingButton.removeClass('blue');
                        floatingButton.addClass('red');
                        break;
                      default:
                        /* 赤から青に変化する */
                        floatingButton.removeClass('red');
                        floatingButton.addClass('blue');
                        break;
                    }
                  },
                  dataType:'json',
                  contentType:'text/json',
                  async: false
              });
          });
	  }

	  /* 表示画面を切替る */
	  function changeDisplay() {
	    switch (displayMode) {
	      case 1:
	        $("#subContentsList").hide();
	        $("#subContentsGraph").show();
	        $("#subContentsClip").hide();
	        $("#mainContentsList").hide();
	        $("#mainContentsGraph").show();
	        $("#mainContentsClip").hide();
	        break;
	      case 2:
	        $("#subContentsList").hide();
	        $("#subContentsGraph").hide();
	        $("#subContentsClip").show();
	        $("#mainContentsList").hide();
	        $("#mainContentsGraph").hide();
	        $("#mainContentsClip").show();
	        break;
	      default:
	        $("#subContentsList").show();
	        $("#subContentsGraph").hide();
	        $("#subContentsClip").hide();
	        $("#mainContentsList").show();
	        $("#mainContentsGraph").hide();
	        $("#mainContentsClip").hide();
	        break;
	    }
	  }

	  /* ページボタンクリック時イベント */
	  function PagingTap() {
		  if(nowPage != $(this).attr("page")){
			  nowPage = $(this).attr("page");
			  changeDetail();   		/* 詳細を切替える */
		  }

	  } // end of PagingTap

})(jQuery); // end of jQuery name space