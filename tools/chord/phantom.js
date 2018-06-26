// extract SVG from chord graph webpage
// example usage: phantomjs phantom.js 'http://www.lsv.fr/~schmitz/xpparser/xpathmark/' > chord.svg
"use strict";
var system = require('system');
var args = system.args;

var page = require('webpage').create();

page.open(args[1], function (status) {
    // Check for page load success
    if (status !== "success") {
        console.log("Unable to access network");
        phantom.exit();
    } else {
        window.setTimeout(
            function(){
                var markup = page.evaluate(function(){
                    return document.getElementById('links').innerHTML;
                });
                console.log(markup);
                phantom.exit();
            }, 400)
    }
});
