var elementIsVisible = function(el) {
	var elemTop = el.getBoundingClientRect().top;
	var elemBottom = el.getBoundingClientRect().bottom;
	var isVisible = (elemTop >= 0) && (elemBottom <= window.innerHeight);
	return isVisible;
};

var myScrollFunction = function(elem) {
	elem.scrollIntoView(false);
	if (elementIsVisible(elem)) {
		return;
	}
	var ww = window.innerHeight/2;
	window.scrollBy(0, +ww);
};
if (!elementIsVisible(arguments[0])) {
	myScrollFunction(arguments[0]);
}