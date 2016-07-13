/**
 * TODO Docs
 *
 * Created by tpaulus on 7/11/16.
 */

var start;
var end;
var type;

google.charts.load('current', {'packages': ['corechart']});

function updateAnalytics() {
    start = $('#analytics-start-date').val();
    end = $('#analytics-end-date').val();
    type = $('#analytics-breakdown-type').val();

    var breakdown_text = "";
    switch (type) {
        case 1:
            breakdown_text = "By Hour";
            break;

        case 2:
            breakdown_text = "By Day";
            break;

        case 3:
            breakdown_text = "By Week";
            break;

        case 4:
            breakdown_text = "By Month";
            break;
    }

    $('.by-breakdown').text(breakdown_text);

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                doUpdate(JSON.parse(xmlHttp.responseText));
            }
        }
    };

    xmlHttp.open('get', "api/ui/events?start=" + start + "&end=" + end);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function doUpdate(eventsJSON) {
    drawCumulativeBreakdown(eventsJSON);
}

function drawCumulativeBreakdown(d) {
    switch (type) {
        case 1:
            var data = google.visualization.arrayToDataTable([
                ['Year', 'Sales', 'Expenses'],
                ['2004', 1000, 400],
                ['2005', 1170, 460],
                ['2006', 660, 1120],
                ['2007', 1030, 540]
            ]);

            var options = {
                title: 'Company Performance',
                curveType: 'function',
                legend: {position: 'bottom'}
            };

            var chart = new google.visualization.LineChart(document.getElementById('curve_chart'));

            chart.draw(data, options);
    }

    console.log(data);
}

