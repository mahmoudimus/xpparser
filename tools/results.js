function info (s) {
  d3.select("#info").text(s);
}

var stats = [];
var schemas = ["xpath-1.0-navigational.rnc","xpath-1.0.rnc",
               "xpath-3.0-leashed.rnc","xpath-3.0.rnc",
               "total"];
var index = {};
for (var i=0; i<schemas.length; i++)
  index[schemas[i]]=i;

function loadBench(bench,k) {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      info("Processing "+bench+"...");
      var entry = schemas.map(function (x) { return 0 });
      entry.name = bench;
      stats.push(entry);
      var xml = this.responseXML;
      var vals = xml.getElementsByTagName("validation");
      // TODO check that fragments are really included
      for (var i=0; i<vals.length; i++) {
        entry[index["total"]]++;
        var schema = vals[i].getAttribute("schema");
        if (vals[i].getAttribute("valid")=="yes")
          entry[index[schema]]++;
      }
      info("Done with "+bench+".");
      k();
    }
  };
  xhttp.open("GET",bench);
  xhttp.send();
}

function run() {
  info("Loading...");
  // Load some test XML, copied from ../benchmark for now.
  // Warning: can't load except from a subdirectory i.e. .. is not allowed!
  loadBench("test.xml",
      function () {
        info("Creating summary...");
        d3.select("body").append("ul").attr("id","summary");
        d3.select("#summary").
          selectAll("li").data(stats).enter()
          .append("li").text(function (d) { return d.name })
          .append("ul")
          .selectAll("li").data(function (d) { return d }).enter()
          .append("li").text(function (d) { return d });
        info("Done with summary.");
      });
}
