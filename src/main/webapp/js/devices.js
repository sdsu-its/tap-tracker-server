/**
 * Devices Functions
 *
 * Created by tpaulus on 7/19/16.
 */

window.onload = function () {
    checkLogin();

    loadDevices(false);
    readyUpdateTable();
};

function readyUpdateTable() {
    if (!sessionStorage.getItem("devices")) {
        setTimeout(function () {
            readyUpdateTable();
        }, 250);
    } else {
        updateTable(JSON.parse(sessionStorage.getItem("devices")));
    }
}

function updateTable(devicesJSON) {
    var table = document.getElementById("devices-table");
    var body = table.getElementsByTagName("tbody")[0];
    for (var d = 0; d < devicesJSON.length; d++) {
        var device = devicesJSON[d];
        var row = body.insertRow(table.rows.length - 1);
        row.id = "d-" + device.id;
        row.insertCell(0).innerText = device.id;
        var deviceName = row.insertCell(1);
        if (device.name != "") {
            deviceName.innerText = device.name;
        }
        else {
            deviceName.innerText = "No Name";
            deviceName.className += "noInfo";
        }

        var dateCell = row.insertCell(2);
        if (device.lastEvent != null) {
            dateCell.innerText = device.lastEvent;
            var date = moment(device.lastEvent);
            dateCell.setAttribute("sorttable_customkey", date.format("YYYYMMDDHHmmss"));
        } else {
            dateCell.innerText = "No Events Recorded";
            dateCell.className += "noInfo";
            dateCell.setAttribute("sorttable_customkey", '0');
        }
        row.insertCell(3).innerHTML = '<button type="button" class="btn btn-default btn-sm" onclick="showUpdateDevice(\'' + device.id + '\')"><span class="glyphicon glyphicon-pencil"></span> &nbsp; Update Device</button>';
    }
    sorttable.makeSortable(table);
}

function showUpdateDevice(deviceID) {
    var devicesJSON = JSON.parse(sessionStorage.getItem("devices"));
    var device_name = "";

    for (var d = 0; d < devicesJSON.length; d++) {
        var device = devicesJSON[d];
        if (device.id == deviceID) {
            device_name = device.name;
            break;
        }
    }
    $('#device_id_update').text(deviceID);
    $('#device_name_update').val(device_name);
    $('#updateDeviceModal').modal('show');
}

function updateDevice() {
    $('#device_update_error_alert').hide();

    var json = '{"id":' + $('#device_id_update').text() + ',"name":"' + $('#device_name_update').val() + '"}';

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 202) {
                var deviceIdUpdate = $('#device_id_update');
                var deviceNameUpdate = $('#device_name_update');

                $('#d-' + deviceIdUpdate.text()).find('td:eq(1)').text(deviceNameUpdate.val());

                $('#updateDeviceModal').modal('hide');
                $('#device_update_success_alert').show();
                window.setTimeout(function () {
                    $('#device_update_success_alert').hide();
                }, 7000);
                deviceIdUpdate.text('');
                deviceNameUpdate.val('');
                loadDevices(true);
            }
            else {
                $('#device_update_error_alert').show();
            }
        }
    };

    xmlHttp.open('post', "api/device/update");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);
}

function createDevice() {
    var devicesJSON = JSON.parse(sessionStorage.getItem("devices"));
    var deviceCreateOverlapAlert = $('#device_create_overlap_alert');
    deviceCreateOverlapAlert.hide();
    $('#device_create_error_alert').hide();

    if (confirm("Are you sure?")) {
        var deviceID = $('#create-device-id');


        for (var d = 0; d < devicesJSON.length; d++) {
            var device = devicesJSON[d];
            if (device.id == deviceID.val()) {
                deviceCreateOverlapAlert.show();
                deviceID.val('');
                return;
            }
        }


        var json = '{"id":' + deviceID.val() + ',"name":"' + $('#create-device-name').val() + '"}';

        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status == 201) {
                    var createDeviceID = $('#create-device-id');
                    var createDeviceName = $('#create-device-name');

                    updateTable([{"id": createDeviceID.val(), "name": createDeviceName.val()}]);

                    $('#device_create_success_alert').show();
                    showPage('overview');

                    window.setTimeout(function () {
                        $('#device_create_success_alert').hide();
                    }, 7000);
                    createDeviceID.val('');
                    createDeviceName.val('');
                    loadDevices(true);
                }
                else if (xmlHttp.status == 400) {
                    $('#create-device-id').val('');
                    deviceCreateOverlapAlert.show();
                }
                else {
                    $('#device_create_error_alert').show();
                }
            }
        };

        xmlHttp.open('post', "api/device/create");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.setRequestHeader("Content-type", "application/json");
        xmlHttp.send(json);
    }
}