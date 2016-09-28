$(document).ready(function(){
	$("tr.matrixSource").hide();
    $("body > div[title='Passed']").hide();
    $("body > div[title='Failed']").hide();
	$("a.showBody").parent().parent().parent().parent().next().hide();
	$("a.showChapter").next().hide();

	$("div.movable").each(function(index) {
		var me = $(this);
        $("#TC_" + me.data("moveto"))[0].appendChild(me[0]);
		me.attr('class','moved');
	})

	var plain = function() {
		$("a.filterTotal").css("font-weight", "normal");
		$("a.filterPassed").css("font-weight", "normal");
		$("a.filterFailed").css("font-weight", "normal");
		$("a.filterExpandAllFailed").css("font-weight", "normal");
	}
	
	$("a.showSource").toggle(
			function(event) {
				$("tr.matrixSource").show();
				event.preventDefault();
			}, 
			function(event) {
				$("tr.matrixSource").hide();
				event.preventDefault();
			}
		);
	
	$("a.filterTotal").click(
			function(event) {
				plain();
				$(this).css("font-weight", "bold");
                $("body > div[title='Passed']").show();
                $("body > div[title='Failed']").show();
				event.preventDefault();
			}
		);

	$("a.filterPassed").click(
			function(event) {
				plain();
				$(this).css("font-weight", "bold");
                $("body > div[title='Passed']").show();
                $("body > div[title='Failed']").hide();
				event.preventDefault();
			}
		);
	
	$("a.filterFailed").click(
			function(event) {
				plain();
				$(this).css("font-weight", "bold");
                $("body > div[title='Passed']").hide();
                $("body > div[title='Failed']").show();
				event.preventDefault();
			}
		);

	$("a.showBody").click(function(event) {
		var tbl = $(this).parent().parent().parent().parent().next();
		if (tbl.is(":visible")) {
			tbl.hide();
		} else {
			tbl.show();
		}
	});

	$("a.showChapter").toggle(
			function(event) {
				$(this).next().show();
				event.preventDefault();
			}, 
			function(event) {
				$(this).next().hide();
				event.preventDefault();
			}
		);

	$("a.filterExpandAllFailed").click(function(event) {
		plain();
		$(this).css("font-weight", "bold");
		$("div[title='Failed']").show();
		$("div[title='Failed']").parent().show();
		$("div[title='Failed'] > div[class=body]").show();
	});

	$("a.filterCollapseAll").click(function(event) {
		$("body > div[class='tree']").hide();
	});
});