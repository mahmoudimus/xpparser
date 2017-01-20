function status (s) {
  d3.select("#status").text(s);
}
function log (s) {
  d3.select("#log ul").append("li").text(s);
}
function assert(b,s) {
  if (!b) alert("PANIC: "+s);
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
  { long : "xpath-1.0-data.rnc", short : "data" },
  { long : "xpath-3.0-leashed.rnc", short : "leashed" },
  { long : "xpath-1.0.rnc", short : "1.0" },
  { long : "xpath-2.0.rnc", short : "2.0" },
  { long : "xpath-3.0.rnc", short : "3.0" }
];

function checkSchemaRelations(query,s) {
  function _assert(b,s) {
    assert (b,s+" "+query.getAttribute("filename")+":"+query.getAttribute("line")+","+query.getAttribute("column"));
  }
  _assert(s["downward"] <= s["vertical"],"downward <= vertical");
  _assert(s["downward"] <= s["forward"],"downward <= forward");
  _assert(s["vertical"] <= s["data"],"vertical <= data");
  _assert(s["forward"] <= s["data"],"forward <= data");
  _assert(s["data"] <= s["leashed"],"data <= leashed");
  _assert(s["leashed"] <= s["3.0"],"leashed <= 3.0");
  // _assert(s["core"] <= s["2.0-core"],"core <= 2.0-core");
  // _assert(s["2.0-core"] <= s["2.0"],"2.0-core <= 2.0");
  _assert(s["2.0-core"] <= s["leashed"],"2.0-core <= leashed");
  _assert(s["1.0"] <= s["2.0"] && s["2.0"] <= s["3.0"],"1.0 <= 2.0 <= 3.0");
  _assert((s["vertical"] && s["forward"]) <= s["downward"],"not (vertical and forward)");
}

// TODO signaler les trucs rares, genre 1.0 mais pas core,
//      data mais indécidable
// TODO mieux détecter les trucs rares, sans hardcoder

function meaningfulFragment(s) {
  var preference =
    [ "core", "2.0-core",
      "downward", "forward", "vertical",
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
  var queries = xml.getElementsByTagName("xpath");
  for (var i=0; i<queries.length; i++) {
    var s = {};
    var results = queries[i].getElementsByTagName("validation");
    // assert(results.length==schemas.length,"loadFromXml");
    for (var j=0; j<results.length; j++)
      s[schemas[results[j].getAttribute("schema")]] = (results[j].getAttribute("valid")=="yes");
    checkSchemaRelations(queries[i],s);
    var schema = meaningfulFragment(s);
    if (schema) entry[schema]++;
  }
  /*
  log("Entry "+entry.name+" ("+entry.total+"): "
    +data.columns.slice(1).map(function (k) { return entry[k] }));
  */
}

// Iterate f over all subsequences of l.
function iterSublists(l,f) {
  var acc = [];
  function aux(l) {
    if (l.length==0) 
      f(acc);
    else {
      var hd = l.shift();
      acc.push(hd);
      aux(l);
      acc.pop();
      aux(l);
      l.unshift(hd);
    }
  }
  return aux(l);
}

// Intersection counts, in venn.js format
var intersections = [];
function intersectionFromXml(bench,xml) {
  var entry = { name : bench, sets : {} };
  var queries = xml.getElementsByTagName("xpath");
  for (var i=0; i<queries.length; i++) {
    var l = [];
    var sets =
      Array.prototype.slice.call(queries[i].getElementsByTagName("validation"))
      .map(function (v) {
        if (v.getAttribute("valid")=="yes")
          return schemas[v.getAttribute("schema")];
        else
          return "";
      })
      .filter(function (x) { return (x=="forward" || x=="vertical" || x=="data" || x=="core" || x=="2.0-core"); })
      .filter(function (x) { return (x!=""); })
      .sort();
    iterSublists(sets,function (l) {
      if (l.length==0) return;
      if (entry.sets[l]==undefined)
        entry.sets[l]=1;
      else
        entry.sets[l]++;
    });
  }
  // Transform set to a list as venn.js expects
  var l = [];
  for (var k in entry.sets)
    l.push({ sets : k.split(","), size : entry.sets[k] });
  entry.sets = l;
  intersections.push(entry);
}

function loadBench(bench,k) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      status("Processing "+bench+"...");
      loadFromXml(bench,this.responseXML);
      intersectionFromXml(bench,this.responseXML);
      status("Done with "+bench+".");
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

  // TODO enhance this quick and dirty normalization,
  //   to avoid modifying data in place
  for (var i=0; i<data.length; i++) {
    for (var k in data[i])
      if (k!="name" && k!="total")
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
 * Main function
 *
 */
function run() {
  status("Loading...");
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
      status("Creating bar chart...");
      visualize();
      status("Visualization done.");
      var i=0;
      for (; i<intersections.length; i++)
        if (intersections[i].name=="xpathmark") break;
      var chart = venn.VennDiagram();
      d3.select("#venn")
        .append("h2").text("Venn diagram for "+intersections[i].name);
      d3.select("#venn")
        .append("p").text("Showing only data and decidable fragments, but not downward (the intersection of vertical and forward).");
      d3.select("#venn")
        .datum(intersections[i].sets).call(chart);
    });
}
