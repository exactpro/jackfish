var createPieChart = function(diagramId, data) {
	var w = 300;
	var h = 300;
	var r = h/2;
	var color = d3.scale.category20();

	var vis = d3.select('#'+diagramId)
		.append("svg:svg")
		.data([data])
		.attr("width", w)
		.attr("height", h)
		.append("svg:g")
		.attr("transform", "translate(" + r + "," + r + ")");
	
	var pie = d3.layout
		.pie()
		.value(function(d){return d.value;});
	
	// declare an arc generator function
	var arc = d3.svg
		.arc()
		.outerRadius(r)
	
	// select paths, use arc generator to draw
	var arcs = vis.selectAll("g.slice")
		.data(pie)
		.enter()
		.append("svg:g")
		.attr("class", "slice");
	
	arcs.append("svg:path")
		.attr("fill", function(d, i){ return color(i)})
		.attr("d", function (d) { return arc(d)});
	
	// add the text
	arcs.append("svg:text")
		.attr("transform", function(d){
			d.innerRadius = 0;
			d.outerRadius = r;
			var midAngle = d.endAngle < Math.PI ? d.startAngle/2 + d.endAngle/2 : d.startAngle/2  + d.endAngle/2 + Math.PI ;
			return "translate(" + arc.centroid(d)[0] + "," + arc.centroid(d)[1] + ") rotate(-90) rotate(" + (midAngle * 180/Math.PI) + ")";
		})
		.attr("text-anchor", "middle").text( function(d, i) {
			return data[i].label
		}
	);
}

var createBarChart = function(diagramId, data, yAxisDescription) {
	var margin = {top: 20, right: 30, bottom: 30, left: 40},
		width = 960 - margin.left - margin.right,
		height = 500 - margin.top - margin.bottom;

	console.log(data)

	var x0 = d3.scale.ordinal().rangeRoundBands([0, width], .1);
	var x1 = d3.scale.ordinal();
	var y = d3.scale.linear().range([height, 0]);
	var colorRange = d3.scale.category20();
	var color = d3.scale.ordinal().range(colorRange.range());

	var xAxis = d3.svg.axis()
		.scale(x0)
		.orient("bottom");

	var yAxis = d3.svg.axis()
		.scale(y)
		.orient("left");

	var divTooltip = d3.select("body")
		.append("div")
		.attr("class", "toolTip");


	var svg = d3.select("#"+diagramId).append("svg")
		.attr("width", width + margin.left + margin.right + 50)
		.attr("height", height + margin.top + margin.bottom)
		.append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");


	var dataset = data;

	var options = d3.keys(dataset[0]).filter(function(key) { return key !== "label"});

	dataset.forEach(function(d) {
		d.valores = options.map(function(name) { return {name: name, value: +d[name]}; });
	});

	x0.domain(dataset.map(function(d) { return d.label; }));
	x1.domain(options).rangeRoundBands([0, x0.rangeBand()]);
	y.domain([0, d3.max(dataset, function(d) {
		return d3.max(d.valores, function(d) {
			return d.value; });
		})
	]);

	svg.append("g")
		.attr("class", "x axis")
		.attr("transform", "translate(0," + height + ")")
		.call(xAxis);

	svg.append("g")
		.attr("class", "y axis")
		.call(yAxis)
		.append("text")
		.attr("transform", "rotate(-90)")
		.attr("y", 6)
		.attr("dy", ".71em")
		.style("text-anchor", "end")
		.text(yAxisDescription);

	var bar = svg.selectAll(".bar")
		.data(dataset)
		.enter().append("g")
		.attr("class", "rect")
		.attr("transform", function(d) { return "translate(" + x0(d.label) + ",0)"; });

	bar.selectAll("rect")
		.data(function(d) { return d.valores; })
		.enter()
		.append("rect")
		.attr("width", x1.rangeBand())
		.attr("x", function(d) { return x1(d.name); })
		.attr("y", function(d) { return y(d.value); })
		.attr("value", function(d){return d.name;})
		.attr("height", function(d) { return height - y(d.value); })
		.style("fill", function(d) { return color(d.name); });

	bar.on("mousemove", function(d){
			divTooltip.style("left", d3.event.pageX+10+"px");
			divTooltip.style("top", d3.event.pageY-25+"px");
			divTooltip.style("display", "inline-block");
			var x = d3.event.pageX, y = d3.event.pageY
			var elements = document.querySelectorAll(':hover');
			l = elements.length
			l = l-1
			elementData = elements[l].__data__
			divTooltip.html("Label : " + (d.label)+"<br>"+"Column : " + elementData.name+"<br>"+"Value : " + elementData.value);
		});
	bar.on("mouseout", function(d){
			divTooltip.style("display", "none");
		});


	var legend = svg.selectAll(".legend")
		.data(options.slice())
		.enter().append("g")
		.attr("class", "legend")
		.attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

	legend.append("rect")
		.attr("x", width - 18)
		.attr("width", 18)
		.attr("height", 18)
		.style("fill", color);

	legend.append("text")
		.attr("x", width - 24)
		.attr("y", 9)
		.attr("dy", ".35em")
		.style("text-anchor", "end")
		.text(function(d) { return d; });

}

var createLineChart = function(diagramId, data, yAxisDescription) {

	var margin = {top: 20, right: 80, bottom: 30, left: 50},
		width = 900 - margin.left - margin.right,
		height = 300 - margin.top - margin.bottom;
	var x = d3.scale.linear().range([0, width]);
	var y = d3.scale.linear().range([height, 0]);
	var color = d3.scale.category20();
	var labelColumn = "label"

	//add information for data
	var tickCountStr = "tickCountStr";
	while(data[0][tickCountStr] !== undefined) {
		tickCountStr+=1
	}
	for(var i = 0; i < data.length; i++) {
		data[i][tickCountStr] = i;
	}
	
	var xAxis = d3.svg.axis()
		.scale(x)
		.ticks(Math.min(data.length, 10))
		.tickFormat(function(d,i) {
			return data[i].label
		})
		.orient("bottom");
	
	var yAxis = d3.svg.axis()
		.scale(y)
		.orient("left");
	
	var line = d3.svg
		.line()
		//.interpolate("cardinal")
		.x(function(d) {
			return x(d.tickCountStr)
		})
		.y(function(d) {
			return y(d.value)
		});
	
	var svg = d3.select("#"+diagramId)
		.append("svg")
		.data([data])
		.attr("width", width + margin.left + margin.right + 100)
		.attr("height", height + margin.top + margin.bottom + 100)
		.append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
	color.domain(d3.keys(data[0]).filter(function(key) {
		return key !== "label" && key !== tickCountStr;
	}))
	
	var lines = color.domain().map(function(name) {
		console.log(name)
		return {
			name: name,
			values: data.map(function(d) {
				return {
					tickCountStr: d.tickCountStr,
					value: +d[name]
				};
			})
		}
	});
	x.domain(d3.extent(data, function(d) {
			return d.tickCountStr;
		})
	);
	y.domain([
		d3.min(lines, function(c) {
			return d3.min(c.values, function(v) {
				return v.value;
			});
		}),
		d3.max(lines, function(c) {
			return d3.max(c.values, function(v) {
				return v.value;
			});
		})
	]);
	svg.append("g")
		.attr("class", "x axis")
		.attr("transform", "translate(0," + height + ")")
		.call(xAxis);
	
	svg.append("g")
		.attr("class", "y axis")
		.call(yAxis)
		.append("text")
		.attr("transform", "rotate(-90)")
		.attr("y", 6)
		.attr("dy", ".71em")
		.style("text-anchor", "end")
		.text(yAxisDescription);
	
	var myLine = svg.selectAll(".myLine")
		.data(lines)
		.enter().append("g")
		.attr("class", "myLine");
	
	myLine.append("path")
		.attr("class", "line")
		.attr("d", function(d) {
			return line(d.values)
		})
		.style("stroke", function(d) {
			return color(d.name);
		});
	
	myLine.append("text")
		.datum(function(d) {
			return {
				name: d.name,
				value: d.values[d.values.length - 1]
			};
		})
		.attr("transform", function(d) {
			return "translate(" + x(d.value.tickCountStr) + "," + y(d.value.value) + ")";
		})
		.attr("x", 3)
		.attr("dy", ".35em")
		.text(function(d) {
			return d.name
		});
}

var createGanntChart = function(diagramId) {

}