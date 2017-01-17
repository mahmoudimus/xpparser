function info (s) {
  d3.select("#info").text(s);
}
function log (s) {
  d3.select("#log").append("li").text(s);
}

/*
 *
 * Dictionary of schemas of interest, associating paths to short names.
 * The order of schemas is fixed by the columns array.
 * The stacked bar visualization assumes that each schema
 * is included in the next one.
 *
 */
var schemas = {
  "xpath-1.0-core.rnc" : "x10c",
  "xpath-1.0.rnc" : "x10",
  "xpath-3.0-leashed.rnc" : "x30l",
  "xpath-3.0.rnc" : "x30"
};
var columns = ["name","x10c","x10","x30l","x30"];

/*
 *
 * Statistics will be collected in the data array,
 * with one entry per benchmark.
 * Each entry will have the following fields:
 *   name  : string -- the name of the benchmark
 *   total : int    -- the number of tests in it
 * and, for each schema S, a field S indicating how
 * many tests satisfy that schema.
 *
 */
var data = [];
data.columns = columns;

/*
 *
 * Extraction of data from benchmarks
 *
 */
function loadFromXml(bench,xml) {
  var entry = { name : bench, total : xml.getElementsByTagName("xpath").length };
  for (var s in schemas) entry[schemas[s]]=0;
  data.push(entry);
  var vals = xml.getElementsByTagName("validation");
  for (var i=0; i<vals.length; i++) {
    var schema = vals[i].getAttribute("schema");
    if (vals[i].getAttribute("valid")=="yes")
      entry[schemas[schema]]++;
  }
  log("Raw entry "+entry.name+" ("+entry.total+"): "
    +data.columns.slice(1).map(function (k) { return entry[k] }));
  // TODO check that fragments are really included
  for (var i=data.columns.length-1; i>1; i--) {
    entry[data.columns[i]] -= entry[data.columns[i-1]];
  }
}
function loadBench(bench,k) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      info("Processing "+bench+"...");
      loadFromXml(bench,this.responseXML);
      info("Done with "+bench+".");
      k();
    }
  };
  xhttp.open("GET","benchmark/"+bench+".xml");
  xhttp.send();
}

/*
 *
 * Data visualization as stacked bars
 * Adapted from http://bl.ocks.org/mbostock/3886208
 *
 */
function visualize() {

  var svg = d3.select("svg"),
    margin = {top: 20, right: 20, bottom: 30, left: 40},
    width = +svg.attr("width") - margin.left - margin.right,
    height = +svg.attr("height") - margin.top - margin.bottom,
    g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  var x = d3.scaleBand()
    .rangeRound([0, width])
    .paddingInner(0.05)
    .align(0.1);

  var y = d3.scaleLinear()
    .rangeRound([height, 0]);

  var z = d3.scaleOrdinal()
    .range(["#7b6888", "#6b486b", "#a05d56", "#d0743c"]);

  var keys = data.columns.slice(1);

  data.sort(function(a, b) { return b.total - a.total; });
  x.domain(data.map(function(d) { return d.name; }));
  y.domain([0, d3.max(data, function(d) { return d.total; })]).nice();
  z.domain(keys);

  g.append("g")
    .selectAll("g")
    .data(d3.stack().keys(keys)(data))
    .enter().append("g")
      .attr("fill", function(d) { return z(d.key); })
    .selectAll("rect")
    .data(function(d) { return d; })
    .enter().append("rect")
      .attr("x", function(d) { return x(d.data.name); })
      .attr("y", function(d) { return y(d[1]); })
      .attr("height", function(d) { return y(d[0]) - y(d[1]); })
      .attr("width", x.bandwidth());

  g.append("g")
      .attr("class", "axis")
      .attr("transform", "translate(0," + height + ")")
    .call(d3.axisBottom(x));

  g.append("g")
      .attr("class", "axis")
    .call(d3.axisLeft(y).ticks(null, "s"))
    .append("text")
      .attr("x", 2)
      .attr("y", y(y.ticks().pop()) + 0.5)
      .attr("dy", "0.32em")
      .attr("fill", "#000")
      .attr("font-weight", "bold")
      .attr("text-anchor", "start")
    .text("Number");

  var legend = g.append("g")
      .attr("font-family", "sans-serif")
      .attr("font-size", 10)
      .attr("text-anchor", "end")
    .selectAll("g")
    .data(keys.slice().reverse())
    .enter().append("g")
      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

  legend.append("rect")
    .attr("x", width - 19)
    .attr("width", 19)
    .attr("height", 19)
    .attr("fill", z);

  legend.append("text")
    .attr("x", width - 24)
    .attr("y", 9.5)
    .attr("dy", "0.32em")
    .text(function(d) { return d; });

}

/*
 *
 * Main function
 *
 */
function run() {
  info("Loading...");
  // Load some test XML, copied from ../benchmark for now.
  // Warning: can't load except from a subdirectory i.e. .. is not allowed!
  function load(i,k) {
    if (i<benchmarks.length)
      loadBench(benchmarks[i],function(){load(i+1,k)});
    else
      k();
  }
  load(0,
    function () {
      info("Creating summary...");
      d3.select("#summary").
        selectAll("li").data(data).enter()
        .append("li").text(function (d) { return (d.name+" ("+d.total+")") })
        .append("ul")
        .selectAll("li").data(
          function (d) {
            return (data.columns.slice(1).map(
              function (k) { return d[k]; }));
          })
        .enter()
        .append("li").text(function (d,i) { return data.columns[i+1]+": +"+d });
      info("Creating bar chart...");
      visualize();
      info("Visualization done.");
    });
}
