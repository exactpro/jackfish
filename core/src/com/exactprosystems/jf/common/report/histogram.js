var createHistogram = function(a, maxValue, interval, intervalCount) {
	var height = 300,
	width = 1000,
	margin=30,
	padding = 2,
	data = [];
	for(var i = 0; i<a.length; i++) {
		data.push({x:i, y:a[i]})
	}

var svg = d3.select("div.container").append("svg")
		.attr("class", "axis")
		.attr("width", width)
		.attr("height", height);

// длина оси X= ширина контейнера svg - отступ слева и справа
var xAxisLength = width - 2 * margin;

// длина оси Y = высота контейнера svg - отступ сверху и снизу
var yAxisLength = height - 2 * margin;

// функция интерполяции значений на ось Х
var scaleX = d3.scale.linear()
			.domain([0, intervalCount])
			.range([0, xAxisLength]);

// функция интерполяции значений на ось Y
var scaleY = d3.scale.linear()
			.domain([maxValue + 20, 0])
			.range([0, yAxisLength]);

// создаем ось X
var xAxis = d3.svg.axis()
			 .scale(scaleX)
			 .tickFormat(function(d) { return d*interval})
			 .tickValues(d3.range(0,intervalCount*interval,5))
			 .orient("bottom");
// создаем ось Y
var yAxis = d3.svg.axis()
			 .scale(scaleY)
			 .orient("left");

 // отрисовка оси Х
svg.append("g")
	 .attr("class", "x-axis")
	 .attr("transform",  // сдвиг оси вниз и вправо
		 "translate(" + margin + "," + (height - margin) + ")")
	.call(xAxis);

 // отрисовка оси Y
svg.append("g")
	.attr("class", "y-axis")
	.attr("transform", // сдвиг оси вниз и вправо на margin
			"translate(" + margin + "," + margin + ")")
	.call(yAxis);

// рисуем горизонтальные линии
d3.selectAll("g.y-axis g.tick")
	.append("line")
	.classed("grid-line", true)
	.attr("x1", 0)
	.attr("y1", 0)
	.attr("x2", xAxisLength)
	.attr("y2", 0);

// создаем объект g для прямоугольников
var g =svg.append("g")
	.attr("class", "body")
	.attr("transform",  // сдвиг объекта вправо
		 "translate(" + margin + ", 0 )");

// связываем данные с прямоугольниками
g.selectAll("rect.bar")
	.data(data)
	.enter()
	.append("rect")
	.attr("class", "bar");
// устанавливаем параметры прямоугольников
g.selectAll("rect.bar")
	.data(data)
	.attr("x", function (d) {
		return scaleX(d.x);
	})
	.attr("y", function (d) {
		return scaleY(d.y) + margin;
	})
	.attr("height", function (d) {
		return yAxisLength - scaleY(d.y);
	})
	.attr("width", function(d){
		return Math.floor(xAxisLength / data.length) - padding;
	})
	.on("mouseover", function(d,i) {
		var range;
		if (data[i+1] === undefined) {
			range = "[ " + data[i].x*interval + " - +∞ )" ;
		} else {
			range = "[ " + data[i].x*interval + " - " + data[i+1].x*interval + " )";
		};
		document.getElementById("hstTimeRange").textContent = "Range : " + range;
		document.getElementById("hstTimeCount").textContent = "Count : " + d.y;
	})
	.on("mouseout", function(d,i) {
		document.getElementById("hstTimeRange").textContent = "Range : ";
		document.getElementById("hstTimeCount").textContent = "Count : ";
	})
}