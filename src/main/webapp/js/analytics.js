/**
 * Analyse and Display Event and Station Data
 *
 * Created by tpaulus on 7/11/16.
 */

var start;
var end;
var type;

var aggregateLineChart;
var activityBarChart;

function updateAnalytics() {
    start = $('#analytics-start-date').val();
    end = $('#analytics-end-date').val();
    type = parseInt($('#analytics-breakdown-type').val());

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
    console.log(eventsJSON);

    drawAggregateLineGraph(eventsJSON);
    drawActivityBarGraph(eventsJSON);

}

/**
 * Get the Bucket number for a given event based on the Offset, and Aggregate type
 *
 * @param type {@type number} Bucket type (Hour, Day, Week)
 * @param offset {@type number} Offset in Hours, Days, or Weeks depending on the type type
 * @param event {@type Object} Event
 */
function getBucketNumber(type, offset, event) {
    var eventDate = new Date(event.time);
    var eventTime;

    switch (type) {
        case 1:
            // Hours
            eventDate.setMinutes(0, 0, 0); // Clear out All lower Units
            eventTime = Math.floor(eventDate.getTime() / 3600000);
            break;
        case 2:
            // Days
            eventDate.setHours(0, 0, 0, 0); // Clear out All lower Units
            eventTime = Math.floor(eventDate.getTime() / 86400000);
            break;
        case 3:
            // Weeks
            eventDate.setHours(0, 0, 0, 0); // Clear out All lower Units
            var day = eventDate.getDay(),
                diff = eventDate.getDate() - day + (day == 0 ? -6 : 1); // adjust when day is sunday
            eventDate.setDate(diff);

            eventTime = Math.floor(eventDate.getTime() / 604800000);
            break;
    }

    return eventTime - offset;
}

function parseDate(item, data) {
    var date = item[0]['xLabel'];
    var text;

    switch (type) {
        // See http://momentjs.com/docs/#/displaying/format/ for Formatting Tokens
        case 1:
            text = date.format("MMMM D, hA");
            break;
        case 2:
            text = date.format("MMMM D");
            break;
        case 3:
            text = date.format("[Week of] MMM, D (w)");
            break;
    }

    return text;
}

function drawAggregateLineGraph(eventJSON) {
    const dsTemplate = {
        label: "My First dataset",
        fill: false,
        lineTension: 0.1,
        backgroundColor: "rgba(75,192,192,0.4)",  // Generate Random bright color for each line
        borderColor: "rgba(75,192,192,1)",
        borderCapStyle: 'butt',
        borderDash: [],
        borderDashOffset: 0.0,
        borderJoinStyle: 'miter',
        pointBorderColor: "rgba(75,192,192,1)",
        pointBackgroundColor: "#fff",
        pointBorderWidth: 1,
        pointHoverRadius: 5,
        pointHoverBackgroundColor: "rgba(75,192,192,1)",
        pointHoverBorderColor: "rgba(220,220,220,1)",
        pointHoverBorderWidth: 2,
        pointRadius: 1,
        pointHitRadius: 10,
        data: [65, 59, 80, 81, 56, 55, 40],
        spanGaps: false
    };

    var deviceJSON = JSON.parse(sessionStorage.getItem('devices'));

    var startDate = moment(start, "YYYY-MM-DD");
    var endDate = moment(end, "YYYY-MM-DD");

    var offset = getBucketNumber(type, 0, {time: startDate});
    var numBuckets = getBucketNumber(type, offset, {time: endDate});

    var datasets = {"Aggregate": []};
    for (var d = 0; d < deviceJSON.length; d++) {
        var device = deviceJSON[d];
        datasets[device.name + " (ID:" + device.id + ")"] = [];
    }

    var labels = [];

    for (var bucket = 0; bucket <= numBuckets; bucket++) {
        var bucketDate;
        switch (type) {
            // See http://momentjs.com/docs/#/displaying/format/ for Formatting Tokens
            case 1:
                bucketDate = moment(startDate).add(bucket, 'hours');
                break;
            case 2:
                bucketDate = moment(startDate).add(bucket, 'days');
                break;
            case 3:
                bucketDate = moment(startDate).add(bucket, 'weeks');
                break;
        }
        labels.push(bucketDate);

        for (var ds in datasets) {
            if (datasets.hasOwnProperty(ds)) {
                datasets[ds].push(0);
            }
        }
    }

    for (var e = 0; e < eventJSON.length; e++) {
        var event = eventJSON[e];
        var bucketNum = getBucketNumber(type, offset, event);
        datasets['Aggregate'][bucketNum] += 1;
        datasets[event.deviceName + " (ID:" + event.deviceID + ")"][bucketNum] += 1;
    }

    var dataSetArray = [];

    for (var setName in datasets) {
        if (datasets.hasOwnProperty(setName)) {
            // Convert Dataset with Data into a Chart Dataset
            var chartDS = jQuery.extend(true, {}, dsTemplate);
            chartDS.label = setName;

            var color = Colors.get();
            chartDS.backgroundColor = Colors.setAlpha(color, 0.4);
            chartDS.borderColor = Colors.setAlpha(color, 1);
            chartDS.pointBorderColor = Colors.setAlpha(color, 1);
            chartDS.pointHoverBackgroundColor = Colors.setAlpha(color, 1);

            chartDS.data = datasets[setName];
            dataSetArray.push(chartDS);
        }
    }

    if (aggregateLineChart == null) {
        aggregateLineChart = new Chart($("#aggregateLineChart"), {
            type: 'line',
            data: {
                labels: labels,
                datasets: dataSetArray
            },
            options: {
                maintainAspectRatio: true,
                scales: {
                    xAxes: [{
                        type: 'time',
                        time: {
                            displayFormats: {
                                hour: "MMM D, hA",
                                day: "MMM D",
                                week: "[Week of] MMM, D (w)",
                                month: "MMM YY"
                            }
                        }
                    }],
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: "Number of Taps (Events)"
                        },
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                },
                tooltips: {
                    callbacks: {
                        title: parseDate

                    }
                }
            }
        });
    } else {
        aggregateLineChart.data.labels = labels;
        aggregateLineChart.data.datasets = dataSetArray;
        aggregateLineChart.update();
    }
}

function drawActivityBarGraph(eventData) {
    var deviceJSON = JSON.parse(sessionStorage.getItem('devices'));
    var devices = {};
    var labels = [];
    var barData = [];
    var backgroundColors = [];
    var borderColors = [];

    for (var d = 0; d < deviceJSON.length; d++) {
        var device = deviceJSON[d];
        devices[device.id] = 0;
        labels.push(device.name + " (" + device.id + ")");

        var barColor = Colors.get();
        backgroundColors.push(Colors.setAlpha(barColor, 0.2));
        borderColors.push(Colors.setAlpha(barColor, 1));
    }

    for (var e = 0; e < eventData.length; e++) {
        var event = eventData[e];
        devices[event.deviceID] += 1;
    }

    for (var dev in devices) {
        if (devices.hasOwnProperty(dev)) {
            barData.push(devices[dev]);
        }
    }

    if (activityBarChart == null) {
        activityBarChart = new Chart($("#activityBarChart"), {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: "Primary",  // Required for tooltips
                        backgroundColor: backgroundColors,
                        borderColor: borderColors,
                        borderWidth: 1,
                        data: barData
                    }
                ]
            },
            options: {
                maintainAspectRatio: true,
                legend: {
                    display: false
                },
                gridLines: {
                    display: false
                },
                scales: {
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: "Number of Taps (Events)"
                        },
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                }
            }
        });
    } else {
        activityBarChart.data.labels = labels;
        activityBarChart.data.datasets[0].data = barData;
        activityBarChart.update();
    }
}