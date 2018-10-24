////////////////////////////////////////////////////////////////////////////////
// Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
////////////////////////////////////////////////////////////////////////////////
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


var content =  $('#contentPart');

var resizerMenu = $('.menu-resizer');

function resizeDivs(){

  var contWidth = content.outerWidth(false);

  var width = mame.width() == 0? 0 : contWidth+Math.ceil(mame.width()-mame[0].clientWidth);

  var mb = $('.menu-button');

  if(contWidth < 30 || width == 0){
       if(mb.hasClass('menu-hide'))
           mb.removeClass('menu-hide').addClass('menu-show');
  } else {
       if(mb.hasClass('menu-show'))
           mb.removeClass('menu-show').addClass('menu-hide');
  }

  mame.width(width);

  resizerMenu.css("left", width);

  var width2 = $('#contentTable').closest('.row').outerWidth(true);

  width += resizerMenu.outerWidth(true);

  $('.helpViewer').css("margin-left", width+10);

  $('#helpViewerContainer').width(width2 - width);

}

$(document).ready(function () {

    var defaultWidth = getComputedStyle(content[0]).width;

   var body = $(document.body);

   var minWidth = parseFloat(content.css("min-width"));

   resizerMenu.mousedown(function(e){
       var ps = e.clientX;
       body.mousemove(function(e){
           if(e.buttons == 0 || e.which == 0){
               body.unbind("mousemove");
               return;
           }
           if(mame.width() == 0){
               mame.width(1);
               content.width(1);
           }
           var deff = e.clientX-ps;
           ps = e.clientX;
           content.width(content.width()+deff);
           resizeDivs();
       });
   });

   $('.menu-button').click(function(){
       body.unbind("mousemove");
       if(mame.width() < 30){
           content.width(defaultWidth);
           mame.width(1);
       } else
           mame.width(0);
       resizeDivs();
   })


	$.each($('.mParent'), function (i, val) {

		doMenu($(val));

	});

	$.each($('li[role="presentation"][class!="mParent"]'), function (i, val) {

		doLoad($(val));

	});

    content.css("display","inline-block");
    resizeDivs();

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

    resizeDivs();

});

$(document).ready(function() {
    var tableWrapElm = document.getElementById("allcontrolsdoublescrolljs");
    if(tableWrapElm){

        //fix 1st column
        var tableWrap = $(tableWrapElm);
        var firstCells =  tableWrap.find("tr > td:first-child,th:first-child");
        var maxWidth = 0;
        firstCells.each(function(){
            var el = $(this);
            var width = el.outerWidth();
            if(maxWidth < width) maxWidth = width;
            el.css({"position": "absolute", "height": el.outerHeight(),"width": function(){ return maxWidth;}, "margin-left" : function(){ return -maxWidth;}});
        });
        tableWrap.css({"margin-left": maxWidth,"overflow-x": "scroll", "overflow-y": "hidden" });

    }

});