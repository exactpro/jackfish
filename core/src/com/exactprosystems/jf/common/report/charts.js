var createPieChart = function(diagramId, data) {
	var w = 300;
	var h = 300;
	var r = h/2;
	var color = d3.scale.category20();

	/*
	var data = [
		{"label":"Category A", "value":200},
		{"label":"Category B", "value":200},
		{"label":"Category C", "value":200},
		{"label":"Category E", "value":200},
		{"label":"Category F", "value":200}
	];
	*/

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

var createLineChart = function(diagramId) {

}

var createGanntChart = function(diagramId) {

}

var createBarChart = function(diagramId) {

}