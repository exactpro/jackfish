function addAllListeners() {
	var elementArray = document.getElementsByClassName("add_listener");
	for(var i =0; i<elementArray.length;i++) {
		var elem = elementArray[i];
		console.log("add listener to " + elem.getAttribute("kindname"));
		addListeners(elem, elem.getAttribute("kindname"), elem.getAttribute("func"));
	}
}

function addListeners(element, name, func) {
	if (func.indexOf(1) != -1 ) { onclick(element, name); }
	if (func.indexOf(2) != -1 ) { ondblclick(element, name); }
	if (func.indexOf(3) != -1 ) { onmousemove(element, name); }
	if (func.indexOf(4) != -1 ) { onkeydown(element, name); }
	if (func.indexOf(5) != -1 ) { onkeyup(element, name); }
	if (func.indexOf(6) != -1 ) { onkeypress(element, name); }
}

function changeSelected(rb) {
	document.getElementById('centralLabel').textContent = 'RadioButton1_' + (rb.checked ? '' : 'un') + "checked";
}

var onclick = function(element, name) {
	element.onclick = function(e) {
		document.getElementById("centralLabel").textContent = name + "_click";
		document.getElementById("moveLabel").textContent = '';
	}
}
var ondblclick = function(element, name) {
	element.ondblclick = function(e) {
		document.getElementById("centralLabel").textContent = name + "_double_click";
		document.getElementById("moveLabel").textContent = '';

	}
}
var onmousemove = function(element, name) {
	element.onmousemove = function(e) {
		document.getElementById("moveLabel").textContent = name+"_move";
	}
}
var onkeydown = function(element, name) {
	element.onkeydown = function(e) {
		console.log("begin key down");
		console.log("key : " + getKey(e));
		if (getKey(e) === "Control") {
			document.getElementById("centralLabel").textContent = name+"_down_Control";
		}
		console.log("exit key down");
		console.log("");
	}
}
var onkeyup = function(element, name) {
	element.onkeyup = function(e) {
		console.log("begin key up");
		console.log(e.keyIdentifier)
		if (getKey(e) === "Control") {
			document.getElementById("centralLabel").textContent = name+"_up_Control";
		}
		console.log("exit key up");
		console.log("");
	}
}
var onkeypress = function(element, name) {
	element.onkeypress = function(e) {
		//U+0031 this is Keyboard.DIG1 chrome
		console.log("begin key press");
		console.log(e)
			//chrome                    //firefox
		if (e.keyIdentifier === "U+0031" || e.key==="1") {
			document.getElementById("centralLabel").textContent = name + "_press_1";
		}
		if (e.keyIdentifier === "U+001B" || e.keyCode===27) {
			document.getElementById("centralLabel").textContent = name + "_press_Escape";
		}
		console.log("end key press");
		console.log("");
	}
}

var getKey = function(e) {
	if (navigator.userAgent.indexOf('Chrome') != -1) {
		return e.keyIdentifier;
	} else {
		return e.key;
	}
}