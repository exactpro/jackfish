$(document).ready(function(){
	var comment = function(arr, func) {
		arr.map(function(index, element) {
			return $(element)
		})
		.filter(function(index, element) {
			return element.hasClass('comment')
		})
		.each(function(index, element) {
			func(element);
		})
	}
	var hideComment = function(arr) {
		comment(arr, function(e) {
			e.hide();
		})
	}
	var showComment = function(arr) {
		comment(arr, function(e) {
			e.show();
		})
	}

	var hideTable = function() {
		$(".repLog > tbody > tr.danger").hide();
		$(".repLog > tbody > tr.success").hide();

		$(".repLog tr[style*='table-row']").hide();

		hideComment($(".repLog > tbody > tr.danger").prev())
		hideComment($(".repLog > tbody > tr.success").prev())
	}

	var animateScrollTo = function() {
		if (!$("table.repLog").is(":visible")) {
			$('html, body').animate({
				scrollTop: $("table.repLog").offset().top
			}, 500);
		}
	}

	$("tr.matrixSource").hide();

	//hide main table
	hideTable();

    // hide all inner actions
    $("a.showBody").parent().parent().next().hide();
	$("a.showChapter").next().hide();

	//function for move reports/images/charts/etc
	$("div.movable").each(function(index) {
		var me = $(this);
        $("#TC_" + ('' + me.data("moveto")).replace(" ", "\\ "))[0].appendChild(me[0]);
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
			hideTable();
			$(".repLog > tbody > tr.danger").show();
			$(".repLog > tbody > tr.success").show();

			showComment($(".repLog > tbody > tr.danger").prev())
			showComment($(".repLog > tbody > tr.success").prev())

			$('.filterPassed, .filterFailed').removeClass('active');

			animateScrollTo();

			event.preventDefault();
		}
	);
	$("button.filterPassed").click(
		function(event) {
			hideTable();

			$(".repLog > tbody > tr.success").show();
			showComment($(".repLog > tbody > tr.success").prev());

			$('.filterFailed').removeClass('active');
			$(".filterPassed").addClass('active');

			animateScrollTo();

			event.preventDefault();
		}
	);
	$("button.filterFailed").click(
		function(event) {
			hideTable();

			$(".repLog > tbody > tr.danger").show();
			showComment($(".repLog > tbody > tr.danger").prev());

			animateScrollTo();

			$('.filterPassed').removeClass('active');
			$(".filterFailed").addClass('active');

			event.preventDefault();
		}
	);

	$("button.filterExpandAllFailed").click(function(event) {
		var failedTr = $("tr.danger");
		failedTr.show();
		failedTr.parent().show();
		failedTr.next().show();
	});
	$("button.filterCollapseAll").click(function(event) {
		hideTable();
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