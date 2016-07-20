/**
 * Nav Bar Functions
 *
 * Created by tpaulus on 7/19/16.
 */

function checkLogin() {
    var sessionToken = Cookies.get("session");
    if (sessionToken != null && sessionToken.length > 0) {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status != 200) window.location.replace("index.html");
            }
        };

        xmlHttp.open('get', "api/session/verify");
        xmlHttp.setRequestHeader("session", sessionToken);
        xmlHttp.send();
    } else {
        window.location.replace("index.html");
    }
}

var currentPage = "overview";
function showPage(name) {
    $("#" + currentPage + "-menu").removeClass("active");
    $("#" + currentPage).hide();
    $("#" + name + "-menu").addClass("active");
    $("#" + name).show();

    currentPage = name;
}

function showProfile() {
    $('#profile-username').text(Cookies.getJSON("user").user.username);
    $('#profileModal').modal('show');
}

function updateProfile() {
    var username = $('#profile-username').text();
    var update_password1 = $('#updatePassword1');
    var update_password2 = $('#updatePassword2');

    if (update_password1.val() != update_password2.val()) {
        $('#newPassMismatch').show();
        update_password1.val('');
        update_password2.val('');
        update_password1.focus();
    } else {
        var xmlHttp = new XMLHttpRequest();

        var json = '{"username":"' + username + '","password":"' + update_password1.val() + '"}';

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status != 200) {
                    var updateProfileError = $('#updateProfileError');
                    if (xmlHttp != 500) {
                        updateProfileError.text(JSON.parse(xmlHttp.responseText).message + " Please try again.");
                    } else {
                        updateProfileError.text("An error occurred processing your request.");
                    }
                    updateProfileError.show();
                }
                else {
                    hideProfile();
                    $('#updateSuccessModal').modal('show');
                }
            }
        };

        xmlHttp.open('post', "api/user/update");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.setRequestHeader("Content-type", "application/json");

        xmlHttp.send(json);

    }
}

function hideProfile() {
    $('#profileModal').modal('hide');
    $('#updateProfileForm')[0].reset();
    $('#updateProfileError').hide();
    $('#newPassMismatch').hide();
    $('#newPassMatch').hide();
}

function logout() {
    Cookies.remove("session");
    Cookies.remove("user");

    window.location = "index.html#logged-out";
}

function loadDevices(forceRefresh) {
    if (!forceRefresh && sessionStorage.getItem("devices")) {
        doLoadDevices(JSON.parse(sessionStorage.getItem("devices")));
    }
    else {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status == 200) doLoadDevices(JSON.parse(xmlHttp.responseText));
            }
        };

        xmlHttp.open('get', "api/ui/devices");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.send();
    }
}

function doLoadDevices(devicesJSON) {
    $("#device-count-badge").text(devicesJSON.length);
    sessionStorage.setItem("devices", JSON.stringify(devicesJSON));
}