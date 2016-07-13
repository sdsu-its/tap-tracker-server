/**
 * TODO Docs
 *
 * Created by tpaulus on 7/6/16.
 */

window.onload = function () {
    checkLoggedIn()
};

function checkLoggedIn() {
    var userInfo = Cookies.getJSON("user");
    var sessionToken = Cookies.get("session");
    if (sessionToken != null && sessionToken.length > 0 && userInfo != null) {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                if (xmlHttp.status == 200) {
                    console.log("Previous Session token is still valid, auto-logging in");
                    doLogin(userInfo);
                } else {
                    console.log("Previous session token is no longer valid. Please login again.")
                }
            }
        };

        xmlHttp.open('get', "api/session/verify");
        xmlHttp.setRequestHeader("session", sessionToken);
        xmlHttp.send();
    }
}

function login() {
    $('#signin_btn').prop("disabled", true).html('<i class="fa fa-circle-o-notch fa-spin" aria-hidden="true"></i>');
    var username = $("#inputUsername").val();
    var password = $('#inputPassword').val();

    var json = '{"username":"' + username + '","password":"' + password + '"}';

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status) {
                Cookies.set("session", xmlHttp.getResponseHeader("session"), {expires: 1})
            }
            doLogin(JSON.parse(xmlHttp.responseText));
        }
    };

    xmlHttp.open('POST', "api/ui/login");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);
}

function doLogin(userJSON) {
    if (userJSON == null) {
        $("#login-fail").show();
        $('#signin_btn').prop("disabled", false).html("Sign in");
    } else {
        Cookies.set("user", userJSON, {expires: 1});
        window.location = "dashboard.html";
    }
}