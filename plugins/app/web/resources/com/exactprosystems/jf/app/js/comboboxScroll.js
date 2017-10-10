var parent = arguments[0].parentElement;
var size = parent.getAttribute("size");
if(size != null)
{
    var parentHeight = parent.clientHeight - 1;
    var childHeight = parentHeight/size;
    parent.scrollTop = arguments[0].index * childHeight;
}