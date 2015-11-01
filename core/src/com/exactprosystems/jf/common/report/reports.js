$(document).ready(function(){
	$("tr.matrixSource").hide();
    $("body > div[title='Passed']").hide();
    $("body > div[title='Failed']").hide();
	$("a.showBody").parent().parent().parent().parent().next().hide();
	$("a.showChapter").next().hide();
    
    
	var plain = function() {
		$("a.filterTotal").css("font-weight", "normal");
		$("a.filterPassed").css("font-weight", "normal");
		$("a.filterFailed").css("font-weight", "normal");
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

	$("a.showBody").toggle(
			function(event) {
				$(this).parent().parent().parent().parent().next().show();
				event.preventDefault();
			}, 
			function(event) {
				$(this).parent().parent().parent().parent().next().hide();
				event.preventDefault();
			}
		);
		
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