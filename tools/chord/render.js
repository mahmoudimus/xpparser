var formatPercent = d3.format(".1%");

function matrixgraph(fragments, matrix, maxentries) {
    
    var mx = [], n = fragments.length;

    var x = d3.scaleBand().range([0, matrixw]),
        z = d3.scaleLinear().range([0.09,1]).clamp(true);
    
    // Convert matrix; count character occurrences.
    var maxdiff = 0;
    for (var i = 0; i < n; i++)
        for (var j = 0; j < n; j++) {
            maxdiff = (maxdiff < matrix[i][j].z)?
                matrix[i][j].z: maxdiff;
            matrix[i][j].x = j;
            matrix[i][j].y = i;
        }
    z.domain([0, maxdiff]);

    // Precompute the orders.
    var orders = {
        name: d3.range(n).sort(function(a, b) { return d3.ascending(fragments[a].name, fragments[b].name); }),
        count: d3.range(n).sort(function(a, b) { return parseInt(fragments[b].entries) - parseInt(fragments[a].entries); }),
        group: d3.range(n).sort(function(a, b) { return d3.ascending(fragments[a].color, fragments[b].color); })
    };

    // The default sort order.
    x.domain(orders.count);
    
    matrixsvg.append("rect")
        .attr("class", "background")
        .attr("width", matrixw)
        .attr("height", matrixh)
        .style("fill", "#fbfbfb");
    
    var row = matrixsvg.selectAll(".row")
        .data(matrix)
        .enter().append("g")
        .attr("class", "row")
        .attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
        .each(row);
    
    row.append("line")
        .attr("x2", matrixw);
    
    var rtext = row.append("text")
        .attr("x", -6)
        .attr("y", x.bandwidth() / 2)
        .attr("dy", ".32em")
        .attr("text-anchor", "end")
        .on("mouseover", mouseoverrow)
        .on("mouseout", mouseout)
        .text(function(d, i) { return fragments[i].name; });
    rtext.append("title").text(function(d, i) {
        return fragments[i].name + ": " + fragments[i].entries + " entries" + " ("
            + formatPercent(parseInt(fragments[i].entries) / maxentries)
            + ")";
    });
    
    var column = matrixsvg.selectAll(".column")
        .data(matrix)
        .enter().append("g")
        .attr("class", "column")
        .attr("transform", function(d, i) { return "translate(" + x(i) + ")rotate(-90)"; });
    
    column.append("line")
        .attr("x1", -matrixw);

    var cpercent = column.append("rect")
        .attr("x", 60)
        .attr("width", function(d, i) { return 100 * parseInt(fragments[i].entries) / maxentries; })
        .attr("height", x.bandwidth()-2)
        .style("fill", function(d, i) { return fragments[i].color; })
        .on("mouseover", mouseoverpercent)
        .on("mouseout", mouseout)
        .append("title").text(function(d, i) {
            return fragments[i].name + ": " + fragments[i].entries + " entries" + " ("
                + formatPercent(parseInt(fragments[i].entries) / maxentries)
                + ")"});
    
    var ctext = column.append("text")
        .attr("x", 6)
        .attr("y", x.bandwidth() / 2)
        .attr("dy", ".32em")
        .attr("text-anchor", "start")
        .on("mouseover", mouseoverpercent)
        .on("mouseout", mouseout)
        .text(function(d, i) { return fragments[i].name; });
    ctext.append("title").text(function(d, i) {
        return fragments[i].name + ": " + fragments[i].entries + " entries" + " ("
            + formatPercent(parseInt(fragments[i].entries) / maxentries)
            + ")";
    });
    
    function row(row) {
        var cell = d3.select(this).selectAll(".cell")
            .data(row.filter(function(d) { return d.z; }))
            .enter().append("rect")
            .attr("class", "cell")
            .attr("x", function(d) { return x(d.x); })
            .attr("width", x.bandwidth())
            .attr("height", x.bandwidth())
            .style("fill-opacity", function(d) { return z(d.z); })
            .style("fill", function(d) { return (parseInt(fragments[d.x].entries) > parseInt(fragments[d.y].entries)) ? fragments[d.x].color : fragments[d.y].color; })
            .on("mouseover", mouseovercell)
            .on("mouseout", mouseout)
            .on("mouseup", mouseclick);
        cell.append("title").text(function(d) {
            return "| " + fragments[d.y].name
                + " \\ " + fragments[d.x].name
                + " | = " + d.z + " entr" + ((d.z > 1)? "ies": "y");
        });
    }

    function mouseclick(p) {
        var log = d3.select("#log")
            .style("opacity", 1);
        log.selectAll("#log div").remove();
        log.selectAll("#log div").data([p])
            .enter()
            .append("div")
            .each(logexamples);
    }
    
    function logexamples(p) {
        var div = d3.select(this);
        var text = p.examples.length + " entr"
            +  ((p.examples.length > 1)? "ies": "y");
        if (p.examples.length < p.z)
            text += " out of "+ p.z;
        text += " in <span class=\"fragment\" style=\"color: "+fragments[p.y].color+"\">"+ fragments[p.y].name
            +"</span> but not in <span class=\"fragment\" style=\"color: "+fragments[p.x].color+"\">"+ fragments[p.x].name
            +"</span>:";
        div.on("mouseover", mouseovercell)
            .on("mouseout", mouseout);
        div.append("p")
            .html(text);
        var list = div.append("ul")
        p.examples.forEach(function (d) {
            list.append("li")
                .append("code")
                .attr("class", "prettyprint lang-xpath")
                .text(d.replace("&quot;","'"));
        });
        PR.prettyPrint();
    }
    
    function mouseovercell(p) {
        d3.selectAll(".row text").classed("active", function(d, i) { return i == p.y; });
        d3.selectAll(".column text").classed("active", function(d, i) { return i == p.x; });
        chordsvg.selectAll(".chord").classed("inactive", function(d, i) {
            return (d.source.index != p.x || d.target.index != p.y)
                && (d.source.index != p.y || d.target.index != p.x);
        });
    }
    
    function mouseout() {
        d3.selectAll("text").classed("active", false);
        chordsvg.selectAll(".chord").classed("inactive", false);
    }
    
    function mouseoverrow(p) {
        d3.selectAll(".row text").classed("active", function(d) { return d === p; });
        chordsvg.selectAll(".chord").classed("inactive", function(d, i) {
            return (d.source.index != p[0].y && d.target.index != p[0].y);
        });
    }
    
    function mouseoverpercent(p) {
        d3.selectAll(".column text").classed("active", function(d) { return d === p; });
        chordsvg.selectAll(".chord").classed("inactive", function(d, i) {
            return (d.source.index != p[0].y && d.target.index != p[0].y);
        });
    }
    
    d3.select("#order").on("change", function() {
        order(this.value);
    });
    
    function order(value) {
        x.domain(orders[value]);
        
        var t = matrixsvg.transition().duration(2500);
        
        t.selectAll(".row")
            .delay(function(d, i) { return x(i) * 4; })
            .attr("transform", function(d, i) { return "translate(0," + x(i) + ")"; })
            .selectAll(".cell")
            .delay(function(d) { return x(d.x) * 4; })
            .attr("x", function(d) { return x(d.x); });
        
        t.selectAll(".column")
            .delay(function(d, i) { return x(i) * 4; })
            .attr("transform", function(d, i) {
                return "translate(" + x(i) + ")rotate(-90)";
            });
    }
}

function chordgraph(fragments, matrix, maxentries) {
    // total number of entries
    var nentries = 0;
    fragments.forEach(function (f) { nentries += parseInt(f.entries); });
    
    layout.matrix = matrix;
    layout.n = matrix.length;
    layout.padding = .04;
    // Redefine the chord layout algorithm
    var subgroups = {}, groupSums = [], groupIndex = d3.range(layout.n),
        subgroupIndex = [], k, x, x0, i, j, l;
    layout.chords = [];
    layout.groups = [];
    k = 0, i = -1;
    while (++i < layout.n) {
        x = 0, j = -1;
        while (++j <  layout.n) {
            x += layout.matrix[i][j].z;
        }
        groupSums.push(x);
        subgroupIndex.push(d3.range(layout.n));
        k += x;
    }
    if (layout.sortGroups) {
        groupIndex.sort(function(a, b) {
            return layout.sortGroups(groupSums[a], groupSums[b]);
        });
    }
    if (layout.sortSubgroups) {
        subgroupIndex.forEach(function(d, i) {
            d.sort(function(a, b) {
                return layout.sortSubgroups(matrix[i][a].z, matrix[i][b].z);
            });
        });
    }
    // angle for one entry
    k = (2 * Math.PI - layout.padding * layout.n) / nentries;
    l = 1, i = -1;
    while (++i < layout.n) {
        var di = groupIndex[i], min = k * fragments[di].entries / groupSums[di];
        l = (min < l)? min: l;
    }
    x = 0, i = -1;
    while (++i < layout.n) {
        x0 = x, j = -1;
        while (++j < layout.n) {
            var di = groupIndex[i],
                dj = subgroupIndex[di][j],
                v = layout.matrix[di][dj].z,
                a0 = x,
                a1 = x += v * l;
            subgroups[di + "-" + dj] = {
                index: di,
                subindex: dj,
                startAngle: a0,
                endAngle: a1,
                value: v
            };
        }
        layout.groups[di] = {
            index: di,
            startAngle: x0,
            endAngle: x = x0 + fragments[di].entries * k,
            value: fragments[di].entries * k
        };
        x += layout.padding;
    }
    i = -1;
    while (++i < layout.n) {
        j = i - 1;
        while (++j < layout.n) {
            var source = subgroups[i + "-" + j],
                target = subgroups[j + "-" + i];
            if (source.value || target.value) {
                layout.chords.push(source.value < target.value ? {
                    source: target,
                    target: source
                }: {
                    source: source,
                    target: target
                });
            }
        }
    }
    if (layout.sortChords)
        layout.chords.sort(function(a, b) {
            return layout.sortChords((a.source.value + a.target.value) / 2,
                                     (b.source.value + b.target.value) / 2);
        });

    // Add a group per neighborhood.
    var group = chordsvg.selectAll(".group")
        .data(layout.groups)
        .enter().append("g")
        .attr("class", "group")
        .on("mouseover", mouseover);

    // Add a mouseover title.
    group.append("title").text(function(d, i) {
        return fragments[i].name + ": " + fragments[i].entries + " entries" + " ("
            + formatPercent(parseInt(fragments[i].entries) / maxentries)
            + ")";
    });

    // Add the group arc.
    var groupPath = group.append("path")
        .attr("id", function(d, i) { return "group" + i; })
        .attr("d", arc)
        .style("fill", function(d, i) { return fragments[i].color; });

    // Add a text label.
    var groupText = group.append("text")
        .attr("x", 6)
        .attr("dy", 16);

    groupText.append("textPath")
        .attr("xlink:href", function(d, i) { return "#group" + i; })
        .text(function(d, i) { return "\u2009"+fragments[i].name; });

    // Remove the labels that don't fit. :(
    //groupText.filter(function(d, i) { return groupPath[0][i].getTotalLength() / 2 - 16 < this.getComputedTextLength(); }).remove();

    // Add the chords.
    var chord = chordsvg.selectAll(".chord")
        .data(layout.chords)
        .enter().append("path")
        .attr("class", "chord")
        .style("fill", function(d) { return fragments[d.source.index].color; })
        .attr("d", path)
        .on("mouseup", mouseclick);

    // Add an elaborate mouseover title for each chord.
    chord.append("title").text(function(d) {
        return "| " + fragments[d.source.index].name
            + " \\ " + fragments[d.target.index].name
            + " | = " + d.source.value
            + " entries"
            + "\n| " + fragments[d.target.index].name
            + " \\ " + fragments[d.source.index].name
            + " | = " + d.target.value
            + " entries";
    });

    function mouseover(d, i) {
        chord.classed("fade", function(p) {
            return p.source.index != i
                && p.target.index != i;
        });
    }
    
    function mouseclick(p) {
        var log = d3.select("#log")
            .style("opacity", 1);
        log.selectAll("#log div").remove();
        log.selectAll("#log div").data([matrix[p.target.index][p.source.index],
                                        matrix[p.source.index][p.target.index]])
            .enter()
            .append("div")
            .each(logexamples);
    }
    function logexamples(p) {
        var div = d3.select(this);
        if (p.z > 0) {
            var text = p.examples.length + " entr"
                +  ((p.examples.length > 1)? "ies": "y");
            if (p.examples.length < p.z)
                text += " out of "+ p.z;
            text += " in <span class=\"fragment\" style=\"color: "
                +fragments[p.y].color+"\">"+ fragments[p.y].name
                +"</span> but not in <span class=\"fragment\" style=\"color: "
                +fragments[p.x].color+"\">"+ fragments[p.x].name
                +"</span>:";
            div.on("mouseover", mouseovercell)
                .on("mouseout", mouseout);
            div.append("p")
                .html(text);
            var list = div.append("ul")
            p.examples.forEach(function (d) {
                list.append("li")
                    .append("code")
                    .attr("class", "prettyprint lang-xpath")
                    .text(d.replace("&quot;","'"));
            });
            PR.prettyPrint();
        }
    }
    
    function mouseovercell(p) {
        d3.selectAll(".row text").classed("active", function(d, i) { return i == p.y; });
        d3.selectAll(".column text").classed("active", function(d, i) { return i == p.x; });
        chordsvg.selectAll(".chord").classed("inactive", function(d, i) {
            return (d.source.index != p.x || d.target.index != p.y)
                && (d.source.index != p.y || d.target.index != p.x);
        });
    }
    
    function mouseout() {
        d3.selectAll("text").classed("active", false);
        chordsvg.selectAll(".chord").classed("inactive", false);
    }
}
