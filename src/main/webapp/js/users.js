/**
 * User Management Functions
 *
 * Created by tpaulus on 7/19/16.
 */

window.onload = function () {
    checkLogin();

    loadDevices(false);
    loadUsers(false);
};

function loadUsers(forceRefresh) {
    if (!forceRefresh && sessionStorage.getItem("users") && sessionStorage.getItem("users") != "undefined") {
        doLoadUsers(JSON.parse(sessionStorage.getItem("users")));
    } else {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status == 200) {
                    doLoadUsers(JSON.parse(xmlHttp.responseText));
                }
            }
        };

        xmlHttp.open('get', "api/user/list");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.send();
    }
}

function doLoadUsers(usersJSON) {
    sessionStorage.setItem("users", JSON.stringify(usersJSON));

    var table = document.getElementById("users-table");
    var body = table.getElementsByTagName("tbody")[0];
    for (var u = 0; u < usersJSON.length; u++) {
        var user = usersJSON[u];
        var row = body.insertRow(table.rows.length - 1);
        row.insertCell(0).innerText = user.username;
        row.insertCell(1).innerHTML = '<button type="button" class="btn btn-default btn-sm" onclick="showUserProfile(\'' + user.username + '\')"><span class="glyphicon glyphicon-pencil"></span> &nbsp; Update User</button>';
    }
    sorttable.makeSortable(table);
}

function showUserProfile(username) {
    $('#profile-username').text(username);
    $('#profileModal').modal('show');
}

function createUser() {
    $('#user-created-error').hide();

    if (confirm("Are you sure?")) {
        var json = '{"username":"' + $('#create-username').val() + '","password":"' + $('#create-password').val() + '"}';


        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status == 201) {
                    var username_field = $('#create-username');
                    doLoadUsers({"username": username_field.val()});

                    $('#user-created-alert').show();
                    showPage('overview');

                    window.setTimeout(function () {
                        $('#user-created-alert').hide();
                    }, 7000);
                    username_field.val('');
                    $('#create-password').val('');
                }
                else if (xmlHttp.status == 400) {
                    $('#device_create_overlap_alert').show();
                    $('#')
                }
                else {
                    $('#device_update_error_alert').show();
                }
            }
        };

        xmlHttp.open('post', "api/user/create");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.setRequestHeader("Content-type", "application/json");
        xmlHttp.send(json);
    }
}