/**
 * Dashboard Functions
 *
 * Created by tpaulus on 7/6/16.
 */

window.onload = function () {
    checkLogin();
    loadRecent(false);
    if (window.location.hash.substr(1) != "" || window.location.hash.substr(1) == currentPage) {
        showPage(window.location.hash.substr(1));
    }
    loadDevices(false);
    loadDeviceIDExport();
};

function loadRecent(forceRefresh) {
    if (!forceRefresh && sessionStorage.getItem("events") && sessionStorage.getItem("events") != "undefined") {
        doLoadRecent(JSON.parse(sessionStorage.getItem("events")));
    } else {


        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status == 200) doLoadRecent(JSON.parse(xmlHttp.responseText));
            }
        };

        xmlHttp.open('get', "api/ui/recent_events?count=25");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.send();
    }
}

function doLoadRecent(eventsJSON) {
    sessionStorage.setItem("events", JSON.stringify(eventsJSON));

    var table = document.getElementById("events-table");
    var body = table.getElementsByTagName("tbody")[0];
    for (var e = 0; e < eventsJSON.length; e++) {
        var event = eventsJSON[e];
        var row = body.insertRow(table.rows.length - 1);
        row.insertCell(0).innerText = event.id;
        var deviceCell = row.insertCell(1);
        deviceCell.setAttribute("sorttable_customkey", event.deviceID);
        deviceCell.innerText = event.deviceName + " (" + event.deviceID + ")";
        row.insertCell(2).innerText = event.type;
        row.insertCell(3).innerText = event.time;

    }
    sorttable.makeSortable(table);
}

function loadDeviceIDExport() {
    var devices = JSON.parse(sessionStorage.getItem("devices"));
    var deviceSelectField = $('#export-IDRestrictionsSelect');

    for (var d = 0; d < devices.length; d++) {
        var device = devices[d];
        deviceSelectField.append($('<option>', {value: device.id, text: device.id + " (" + device.name + ")"}))
    }
}

function updateExportFormRestrictions() {
    if ($('#export-RestrictionTypeSelect').val() == "1") {
        $('#export-IDRestrictions').show();
    } else {
        $('#export-IDRestrictions').hide();
    }
}

function doDownload() {
    var url = "api/ui/csv_export?";
    url += "start=" + $('#export-startDate').val();
    url += "&end=" + $('#export-endDate').val();


    if ($('#export-RestrictionTypeSelect').val() == "1") {
        url += "&ids=" + JSON.stringify($('#export-IDRestrictionsSelect').val().map(Number));
    }

    var iframe = document.getElementById('download');
    iframe.src = url;

    $('#exportForm')[0].reset();
}