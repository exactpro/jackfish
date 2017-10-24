var mame = $('.mainMenu');


function doMenu(obj) {

	var hashTag = obj.attr('id') + '_child';

	obj.on('click', function () {

		$(document.getElementById(hashTag)).toggle();

		return false;

	});

}

function doLoad(obj) {

	obj.on('click', function () {

		$('.mainMenu .active').removeClass('active');

		obj.addClass('active');

	});

}


$(document).ready(function () {

	$.each($('.mParent'), function (i, val) {

		doMenu($(val));

	});

	$.each($('li[role="presentation"][class!="mParent"]'), function (i, val) {

		doLoad($(val));

	});

	mame.width($('.menuCont').width());

	mame.height($(window).height() - mame[0].getBoundingClientRect().top);

	$.each($("a[href^='#']"),function(i,val) {

		var hr = $(val).attr('href');

		var id = hr.substring(1);

		if (id !== "") {

			$(val).click(function(event) {

				$('html, body').animate({

					scrollTop: $(document.getElementById($(val).attr("href").substring(1))).offset().top-50

				}, 500);

			})

		}

	});
	f();
});

function f(){
var str = '';
var currentIndex = 0;
var allIns = [];
var foundDivs = [];
var scrollToDiv = function(div) {
	$('html, body').animate({
		scrollTop: $(div).offset().top-50
	}, 500);
}

var scrollToElem = function(elem) {
	$('html, body').animate({
		scrollTop: $(elem).offset().top-50
	}, 100);
}

jQuery.expr[":"].Contains = jQuery.expr.createPseudo(function(arg) {
    return function( elem ) {
		return jQuery(elem).text().toUpperCase().indexOf(arg.toUpperCase()) >= 0;
    };
});


//go forward
function goF() {
    if(foundDivs.length === 0){return}
    $(allIns[currentIndex]).toggleClass('currentFoundElement foundElement');
    if(currentIndex+1 === allIns.length){
        currentIndex = 0;
        scrollToElem(allIns[currentIndex]);
    }else{
        scrollToElem(allIns[++ currentIndex])
    }
    $('.searchLabel').text(1 + currentIndex +' of '+ allIns.length);
    $(allIns[currentIndex]).toggleClass('currentFoundElement foundElement');
}

//go back
function goB() {
    if(foundDivs.length === 0){return}
    $(allIns[currentIndex]).toggleClass('currentFoundElement foundElement');
    if(currentIndex === 0){
        currentIndex = allIns.length-1;
        scrollToElem(allIns[currentIndex]);
    }else{
        scrollToElem(allIns[--currentIndex]);
    }
    $('.searchLabel').text(1+currentIndex +' of '+ allIns.length);
    $(allIns[currentIndex]).toggleClass('currentFoundElement foundElement');
}

function markAllElements(array, str) {
    var re = new RegExp('(' + str + ')', 'ig');
    $(array).each(function(i,elem){
        elem.innerHTML = this.innerHTML.replace(re, '<ins>$1</ins>');
    });
    allIns = $('ins');
    $('.searchLabel').text('1 of '+allIns.length);
    scrollToElem(allIns[0]);
    currentIndex++;
    $(allIns[0]).toggleClass('currentFoundElement');

}

function clear() {

    currentIndex = -1;
    $('ins').contents().unwrap();
    foundDivs = [];
    allIns = [];
    $('.searchLabel').text('');
    $("#btnPrev").toggleClass('activeBtn nonActiveBtn');
    $("#btnNext").toggleClass('activeBtn nonActiveBtn');
}


$(".searchInput").on("keypress paste", function(e) {

if(e.which == 13) {

	if( $(this).val() === str){
	    goF();
	}else{

    clear();
    str = $(this).val();
    if(str.includes(' | ')){str = str.replace('|','\\|')}
    if(str.includes(' ^ ')){str = str.replace('^','\\^')}

    if(str.length === 0){
        clear();
        return;
    }

    $("#btnPrev").toggleClass('activeBtn nonActiveBtn');
    $("#btnNext").toggleClass('activeBtn nonActiveBtn');

    if($(this).val().includes('|') || $(this).val().includes('^')){
    	foundDivs = $('h3:contains('+str+'), h2:contains('+str+'), p:contains('+str+'), td:contains('+str+'), font:contains('+str+'), pre:contains('+str+')');
    }else{
    	foundDivs = $('h3:contains('+str+'), h2:Contains('+str+'), p:Contains('+str+'), td:Contains('+str+'), font:Contains('+str+'), pre:Contains('+str+')');
    }

    if(foundDivs.length === 0){
        $('.searchLabel').text(0);
        return;
    }
    markAllElements(foundDivs, str);
    }

    }
});

$("#btnNext").click(function(e){goF();});

$("#btnPrev").click(function(e){goB();});
}

$(window).resize(function () {

	mame.width($('.menuCont').width());

	$('.menuCont').height(mame.height());

	mame.height($(window).height() - mame[0].getBoundingClientRect().top);

});


function DoubleScroll(element) {

	var scrollbar = document.createElement('div');

	scrollbar.appendChild(document.createElement('div'));

	scrollbar.style.overflow = 'auto';

	scrollbar.style.overflowY = 'hidden';

	scrollbar.className += " dobscrollDiv";

	scrollbar.firstChild.style.width = element.scrollWidth + 'px';

	//scrollbar.firstChild.style.paddingTop= '1px';

	scrollbar.firstChild.appendChild(document.createTextNode('\xA0'));

	scrollbar.onscroll = function () {

		element.scrollLeft = scrollbar.scrollLeft;

	};

	element.onscroll = function () {

		scrollbar.scrollLeft = element.scrollLeft;

	};

	element.parentNode.insertBefore(scrollbar, element);

}


DoubleScroll(document.getElementById('doublescroll'));

$('#co-1Table').height($('.dobscrollDiv').height());