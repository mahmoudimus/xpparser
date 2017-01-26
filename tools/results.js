// TODO signaler les trucs rares, genre 1.0 mais pas core,
//      data mais indécidable
// TODO mieux détecter les trucs rares, sans hardcoder

function status (s) {
  d3.select("#status").text(s);
}
function log (s) {
  d3.select("#log ul").append("li").text(s);
}
var assert_alert=true;
function assert(b,s) {
  if (!b) {
    if (assert_alert) {
      assert_alert=false;
      // alert("WARNING: some assertions failed; check the log.");
    }
    log("ASSERT FAILURE: "+s);
  }
}

/*
 *
 * Schemas of interest, given as filenames with a short name.
 * The order is used for displaying stacked bars.
 *
 * The function checkSchemaRelations may then be used to check
 * properties such as inclusion or disjointness.
 *
 * The function meaningfulFragment should be used to indicate,
 * for a given query, in which fragment it should be counted.
 * This is used for the stacked bar visualization.
 *
 */
var _schemas = [
  { long : "xpath-1.0-core.rnc", short : "core" },
  { long : "xpath-2.0-core.rnc", short : "2.0-core" },
  { long : "xpath-1.0-downward.rnc", short : "downward" },
  { long : "xpath-1.0-forward.rnc", short : "forward" },
  { long : "xpath-1.0-vertical.rnc", short : "vertical" },
  { long : "xpath-modal.rnc", short : "modal" },
  { long : "xpath-1.0-data.rnc", short : "data" },
  { long : "xpath-3.0-leashed.rnc", short : "leashed" },
  { long : "xpath-1.0.rnc", short : "1.0" },
  { long : "xpath-2.0.rnc", short : "2.0" },
  { long : "xpath-3.0.rnc", short : "3.0" }
];

function checkSchemaRelations(query,s) {
  function _assert(b,s) {
    assert (b,s+" false in "+
      query.getAttribute("filename")+":"+query.getAttribute("line")+","+query.getAttribute("column"));
  }
  _assert(s["downward"] <= s["vertical"],"downward <= vertical");
  _assert(s["downward"] <= s["forward"],"downward <= forward");
  _assert(s["vertical"] <= s["data"],"vertical <= data");
  _assert(s["forward"] <= s["data"],"forward <= data");
  _assert(s["data"] <= s["leashed"],"data <= leashed");
  _assert(s["leashed"] <= s["3.0"],"leashed <= 3.0");
  _assert(s["core"] <= s["2.0-core"],"core <= 2.0-core");
  _assert(s["2.0-core"] <= s["2.0"],"2.0-core <= 2.0");
  _assert(s["1.0"] <= s["2.0"] && s["2.0"] <= s["3.0"],"1.0 <= 2.0 <= 3.0");
  _assert((s["vertical"] && s["forward"]) <= s["downward"],"not (vertical and forward)");
}

function meaningfulFragment(s) {
  var preference =
      [ "core", "2.0-core", 
      "downward", "forward", "vertical", "modal",
      "data", "leashed",
      "1.0", "2.0", "3.0" ];
  for (var i=0; i<preference.length; i++)
    if (s[preference[i]]) return preference[i];
  return undefined;
}

/*
 *
 * Computation of derived representations of _schemas
 *
 */
var schemas = {};
for (var i=0; i<_schemas.length; i++)
  schemas[_schemas[i].long] = _schemas[i].short;
var columns = ["name"].concat(_schemas.map(function (x) { return x.short }));

/*
 *
 * Statistics will be collected in the data array,
 * with one entry per benchmark.
 *
 * Each entry will have the following fields:
 *   name  : string -- the name of the benchmark
 *   total : int    -- the number of tests in it
 * and, for each schema S, a field S indicating how
 * many tests satisfy that schema.
 *
 * Currently, the number of entries for schema S
 * does not include the number of entries for
 * schemas S' included in S, i.e. there is no
 * double counting.
 *
 */
var data = benchmarks.map(function (s) { return s; });
data.columns = columns;

/* Stuff precomputed for venn, TODO clean later */
var vschemas = ["forward","vertical","data"];
var max = 1<<vschemas.length;
var intToSets = [];
for (var mask=0; mask<max; mask++) {
  intToSets[mask]=[];
  for (var j=0; j<vschemas.length; j++)
    if ((mask & (1<<j)) != 0) {
      intToSets[mask].push(vschemas[j]);
    }
}

/*
 *
 * Extraction of data from benchmarks
 *
 */
function loadFromXml(bench,xml) {

  var t0 = performance.now();

  var entry = { name : bench, total : xml.getElementsByTagName("xpath").length, sets : [] };
  for (var s in schemas) entry[schemas[s]]=0;
  var queries = xml.getElementsByTagName("xpath");

  for (var i=0; i<queries.length; i++) {

    // Compute increments for each schema (used for stacked bars)

    var s = {};
    var results = queries[i].getElementsByTagName("validation");
    for (var j=0; j<results.length; j++)
      s[schemas[results[j].getAttribute("schema")]] = (results[j].getAttribute("valid")=="yes");
    checkSchemaRelations(queries[i],s);
    var schema = meaningfulFragment(s);
    if (schema) entry[schema]++;

    if (true) {

      // Compute cardinals and cardinals of intersections (for venn diagrams)

      var vals = queries[i].getElementsByTagName("validation");
      var mask = 0;
      for(var j=0; j<vals.length; j++) {
        var v = vals[j];
        if (v.getAttribute("valid")=="yes") {
          var s = schemas[v.getAttribute("schema")];
          if (vschemas.indexOf(s)!=-1)
            mask += 1<<vschemas.indexOf(s);
        }
      }
      for (var k=1; k<=mask; k++) {
        if ((k & mask) == k) { // k is included in mask
          if (entry.sets[k]==undefined)
            entry.sets[k]=1;
          else
            entry.sets[k]++;
        }
      }

    }

  }

  // Transform set to a list as venn.js expects
  var l = [];
  for (var k=0; k<entry.sets.length; k++)
    if (entry.sets[k]!=undefined)
      l.push({ sets : intToSets[k], size : entry.sets[k] });
  entry.sets = l;

  var t1 = performance.now();
  log(Math.floor(t1-t0)+"ms for loading "+bench);

  return entry;
}

/*
 *
 * Data visualization as stacked bars
 * Adapted from http://bl.ocks.org/mbostock/3886208
 *
 */
function chartbars() {

  // TODO enhance this quick and dirty normalization,
  //   to avoid modifying data in place
  for (var i=0; i<data.length; i++) {
    for (var k in data[i])
      if (k!="name" && k!="total" && k!="sets")
        data[i][k] = (100.*data[i][k])/data[i].total;
    data[i].total = 100;
  }

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
    .range(["#ff7200", "#ffff0a", "#c9ff0a", "#18ba00", "#0093e2", "#1600e2", "#7c13c6", "#9d13c6", "#c6136d", "#c61313"]);

  var keys = data.columns.slice(1);

  data.sort(function(a, b) { return b.total - a.total; });
  x.domain(data.map(function(d) { return d.name; }));
  y.domain([0, d3.max(data, function(d) { return d.total; })]).nice();
  z.domain(keys);

  // Create stack data matrix and draw it
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
      .attr("height", function (d) { return y(d[0]) - y(d[1]); })
      .attr("onclick", "alert(this.__data__)")
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
    .text("%");

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
 * Render page once all data has been processed
 *
 */
function visualize() {

  var t0 = performance.now();

  status("Creating summary...");
  d3.select("#summary ul").
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

  var t1 = performance.now();
  log(Math.floor(t1-t0)+"ms for summary");

  status("Creating bar chart...");
  chartbars();

  var t2 = performance.now();
  log(Math.floor(t2-t1)+"ms for bar chart");

  status("Visualization done.");

  var i=data.findIndex(function(elt) { return (elt.name=="xpathmark"); });
  if (i==-1) i=0;
  var chart = venn.VennDiagram();
  d3.select("#venn")
	.append("h2").text("Venn diagram for "+data[i].name);
  d3.select("#venn")
	.append("p").text("Showing only data and decidable fragments, but not downward (the intersection of vertical and forward).");
  d3.select("#venn")
	.datum(data[i].sets).call(chart);

  var t3 = performance.now();
  log(Math.floor(t3-t2)+"ms for venn");

}

/*
 *
 * Main function: load data, then visualize
 *
 */
function run() {
  load(visualize);
}
function load(k) {
  status("Loading...");
  var total = 0;
  function process(i,bench) {
	return function () {
	  if (this.readyState == 4 && this.status == 200) {
		log("Processing "+bench+"...");
		data[i] = loadFromXml(bench,this.responseXML);
		total++;
		status("Loading ("+total+"/"+benchmarks.length+")...");
		log("Done with "+bench+" ("+total+"/"+benchmarks.length+").");
		if (total==benchmarks.length) k();
	  }
	}
  }
  for (var i=0; i<benchmarks.length; i++) {
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = process(i,benchmarks[i]);
	xhttp.open("GET","benchmark/"+benchmarks[i]+".xml");
	xhttp.send();
  }
}
