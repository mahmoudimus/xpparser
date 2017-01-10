// List of all standard functions, from the "Quick Contents" of
// the reference: https://www.w3.org/TR/xpath-functions-30
// These may be functions, function operators, or math functions,
// and will correspondingly be prefixed by fn:, op: or math:.
var all = [
  "abs",
  "acos",
  "add-dayTimeDurations",
  "add-dayTimeDuration-to-date",
  "add-dayTimeDuration-to-dateTime",
  "add-dayTimeDuration-to-time",
  "add-yearMonthDurations",
  "add-yearMonthDuration-to-date",
  "add-yearMonthDuration-to-dateTime",
  "adjust-dateTime-to-timezone",
  "adjust-date-to-timezone",
  "adjust-time-to-timezone",
  "analyze-string",
  "asin",
  "atan",
  "atan2",
  "available-environment-variables",
  "avg",
  "base64Binary-equal",
  "base-uri",
  "boolean",
  "boolean-equal",
  "boolean-greater-than",
  "boolean-less-than",
  "ceiling",
  "codepoint-equal",
  "codepoints-to-string",
  "collection",
  "compare",
  "concat",
  "concatenate",
  "contains",
  "cos",
  "count",
  "current-date",
  "current-dateTime",
  "current-time",
  "data",
  "date-equal",
  "date-greater-than",
  "date-less-than",
  "dateTime",
  "dateTime-equal",
  "dateTime-greater-than",
  "dateTime-less-than",
  "day-from-date",
  "day-from-dateTime",
  "days-from-duration",
  "dayTimeDuration-greater-than",
  "dayTimeDuration-less-than",
  "deep-equal",
  "default-collation",
  "distinct-values",
  "divide-dayTimeDuration",
  "divide-dayTimeDuration-by-dayTimeDuration",
  "divide-yearMonthDuration",
  "divide-yearMonthDuration-by-yearMonthDuration",
  "doc",
  "doc-available",
  "document-uri",
  "duration-equal",
  "element-with-id",
  "empty",
  "encode-for-uri",
  "ends-with",
  "environment-variable",
  "error",
  "escape-html-uri",
  "exactly-one",
  "except",
  "exists",
  "exp",
  "exp10",
  "false",
  "filter",
  "floor",
  "fold-left",
  "fold-right",
  "for-each",
  "for-each-pair",
  "format-date",
  "format-dateTime",
  "format-integer",
  "format-number",
  "format-time",
  "function-arity",
  "function-lookup",
  "function-name",
  "gDay-equal",
  "generate-id",
  "gMonthDay-equal",
  "gMonth-equal",
  "gYear-equal",
  "gYearMonth-equal",
  "has-children",
  "head",
  "hexBinary-equal",
  "hours-from-dateTime",
  "hours-from-duration",
  "hours-from-time",
  "id",
  "idref",
  "implicit-timezone",
  "index-of",
  "innermost",
  "in-scope-prefixes",
  "insert-before",
  "intersect",
  "iri-to-uri",
  "is-same-node",
  "lang",
  "last",
  "local-name",
  "local-name-from-QName",
  "log",
  "log10",
  "lower-case",
  "matches",
  "max",
  "min",
  "minutes-from-dateTime",
  "minutes-from-duration",
  "minutes-from-time",
  "month-from-date",
  "month-from-dateTime",
  "months-from-duration",
  "multiply-dayTimeDuration",
  "multiply-yearMonthDuration",
  "name",
  "namespace-uri",
  "namespace-uri-for-prefix",
  "namespace-uri-from-QName",
  "nilled",
  "node-after",
  "node-before",
  "node-name",
  "normalize-space",
  "normalize-unicode",
  "not",
  "NOTATION-equal",
  "number",
  "numeric-add",
  "numeric-divide",
  "numeric-equal",
  "numeric-greater-than",
  "numeric-integer-divide",
  "numeric-less-than",
  "numeric-mod",
  "numeric-multiply",
  "numeric-subtract",
  "numeric-unary-minus",
  "numeric-unary-plus",
  "one-or-more",
  "outermost",
  "parse-xml",
  "parse-xml-fragment",
  "path",
  "pi",
  "position",
  "pow",
  "prefix-from-QName",
  "QName",
  "QName-equal",
  "remove",
  "replace",
  "resolve-QName",
  "resolve-uri",
  "reverse",
  "root",
  "round",
  "round-half-to-even",
  "seconds-from-dateTime",
  "seconds-from-duration",
  "seconds-from-time",
  "serialize",
  "sin",
  "sqrt",
  "starts-with",
  "static-base-uri",
  "string",
  "string-join",
  "string-length",
  "string-to-codepoints",
  "subsequence",
  "substring",
  "substring-after",
  "substring-before",
  "subtract-dates",
  "subtract-dateTimes",
  "subtract-dayTimeDuration-from-date",
  "subtract-dayTimeDuration-from-dateTime",
  "subtract-dayTimeDuration-from-time",
  "subtract-dayTimeDurations",
  "subtract-times",
  "subtract-yearMonthDuration-from-date",
  "subtract-yearMonthDuration-from-dateTime",
  "subtract-yearMonthDurations",
  "sum",
  "tail",
  "tan",
  "time-equal",
  "time-greater-than",
  "time-less-than",
  "timezone-from-date",
  "timezone-from-dateTime",
  "timezone-from-time",
  "to",
  "tokenize",
  "trace",
  "translate",
  "true",
  "union",
  "unordered",
  "unparsed-text",
  "unparsed-text-available",
  "unparsed-text-lines",
  "upper-case",
  "uri-collection",
  "year-from-date",
  "year-from-dateTime",
  "yearMonthDuration-greater-than",
  "yearMonthDuration-less-than",
  "years-from-duration",
  "zero-or-one"
  ];


function logHTML(html) {
  document.getElementById("working").innerHTML += html;
}
function log(txt) {
  logHTML("<p>"+txt+"</p>");
}

function process(document) {

  // Retrieve all fn:, op: and math: headers
  var arr =
    // Get H4 and H3 elements, as arrays, and concatenate them
    Array.prototype.slice.call(document.getElementsByTagName("h4"))
    .concat(Array.prototype.slice.call(document.getElementsByTagName("h3")))
    // Filter only those corresponding to the documentation of items
    .filter(function (x) {
      var text = x.innerText.replace("\n"," ").split(" ");
      if (text.length!=2) return false;
      return (text[1].includes("fn:")
           || text[1].includes("op:")
           || text[1].includes("math:"));
    });

  log("Got "+arr.length+" item headers.");

  // Check that all declaration headers have been found
  {
    // Get all names, without namespaces
    var names = arr.map(function (x) {
        var text = x.innerText.replace("\n"," ");
        return (text.split(" ")[1].split(":")[1]);
      });
    for (var i=0; i<all.length; i++) {
      if (names.indexOf(all[i])==-1) {
        log("Not found: "+all[i]);
      }
    }
  }

  // Display all items, by category
  function display(cat,arr) {
    var s = "<ul>";
    for (var i=0; i<arr.length; i++) {
      var node = arr[i];
      var header = node.innerText.replace("\n"," ").split(" ")[1].split(":");
      if (header[0]==cat) {
        s += "<li>"+cat+":"+header[1]+"</li>";
      }
    }
    log("Category "+cat+":");
    logHTML(s+"</ul>");
  }
  display("math",arr);
  display("fn",arr);
  display("op",arr);

  log("Extracting pure declarations in fn: and math:...");

  for (var i=0; i<arr.length; i++) {
    var node = arr[i];
    var header = node.innerText.replace("\n"," ").split(" ")[1].split(":");
    if (header[0]=="fn" || header[0]=="math") {
      process_decl(header[0]+":"+header[1],node);
    }
  }

  log("Inconditionally pure: "+pure.join(", "));
  log("Zero-ary pure: "+arity_pure[0].join(", "));
  log("One-ary pure: "+arity_pure[1].join(", "));
  log("Two-ary pure: "+arity_pure[2].join(", "));
  log("Three-ary pure: "+arity_pure[3].join(", "));

}

var pure = [];
var arity_pure = [ [], [], [], [] ];

function process_decl(name,node) {
  var dts = node.parentElement.getElementsByTagName("dl")[0].getElementsByTagName("dt");
  function toInt(s) {
    s = s.split("The ")[1];
    if (s=="zero") return 0;
    if (s=="one") return 1;
    if (s=="two") return 2;
    if (s=="three") return 3;
    alert(s)
  }
  function isPure(s) {
    return (s.includes("·deterministic·")
         && s.includes("·context-independent·")
         && s.includes("·focus-independent·"));
  }
  for (var i=0; i<dts.length; i++) {
    if (dts[i].innerText=="Properties") {
      node = dts[i];
      while (node.tagName!="dd") node = node.nextSibling;
      // node is now the DD element that contains the properties description
      var spec = node.innerText.split(".");
      for (var j=0; j<spec.length; j++) {
        if (spec[j].replace("\n","")!="")
          if (!(spec[j].includes("It depends on")))
          if (spec[j].includes("This function is")) {
            if (isPure(spec[j]))
              pure.push(name);
          } else {
            var split = spec[j].split("-argument form of this function is");
            if (split.length!=2)
              alert("Malformed entry "+name+"["+j+"]: "+spec[j]);
            if (isPure(split[1]))
              arity_pure[toInt(split[0])].push(name);
          }
      }
    }
  }
}

/* Work on reference page */

var test;
function run() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      log("Loaded reference document as XML.");
      test=process(this.responseXML);
    }
  };
  xhttp.open("GET", "xpath-functions-30.html", true);
  xhttp.send();
}
