$(document).ready(function(){
	$("tr.matrixSource").hide();

	//hide main table
	$("body > table[class*='repLog']").hide();

    // hide all inner actions
    $("a.showBody").parent().parent().next().hide();
	$("a.showChapter").next().hide();

	//function for move reports/images/charts/etc
	$("div.movable").each(function(index) {
		var me = $(this);
        $("#TC_" + me.data("moveto").replace(" ", "\\ "))[0].appendChild(me[0]);
		me.attr('class','moved');
	})

	//show matrix source
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

	$("button.filterTotal").click(
		function(event) {
			$("table.repLog").show();

			$(".repLog > tbody > tr.danger").show();
			$(".repLog > tbody > tr.success").show();

			$('.filterPassed, .filterFailed').removeClass('active');

			$('html, body').animate({
				scrollTop: $("table.repLog").offset().top
			}, 1000);

			event.preventDefault();
		}
	);
	$("button.filterPassed").click(
		function(event) {
			$("table.repLog").show();

			$(".repLog > tbody > tr.danger").hide();
			$(".repLog > tbody > tr.success").show();

			$('.filterFailed').removeClass('active');
			$(".filterPassed").addClass('active');

			$('html, body').animate({
				scrollTop: $("table.repLog").offset().top
			}, 1000);

			event.preventDefault();
		}
	);
	$("button.filterFailed").click(
		function(event) {
			$(".repLog").show();

			$(".repLog > tbody > tr.danger").show();
			$(".repLog > tbody > tr.success").hide();

			$('html, body').animate({
				scrollTop: $("table.repLog").offset().top
			}, 1000);

			$('.filterPassed').removeClass('active');
			$(".filterFailed").addClass('active');

			event.preventDefault();
		}
	);

	$("button.filterExpandAllFailed").click(function(event) {
		$(".repLog").show();

		var failedTr = $("tr.danger");
		failedTr.show();
		failedTr.parent().show();
		failedTr.next().show();
	});
	$("button.filterCollapseAll").click(function(event) {
		$("table.repLog").hide();

	});

	$("a.showBody").click(function(event) {
		var tbl = $(this).parent().parent().next();
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
});