/**
 * Nav Bar Functions
 *
 * Created by tpaulus on 7/19/16.
 */

function showProfile() {
    $('#profile-username').text(Cookies.getJSON("user").user.username);
    $('#profileModal').modal('show');
}

function updateProfile() {
    var username = $('#profile-username').text();
    var current_password = $('#currentPassword');
    var update_password1 = $('#updatePassword1');
    var update_password2 = $('#updatePassword2');

    if (update_password1.val() != update_password2.val()) {
        $('#newPassMismatch').show();
        update_password1.val('');
        update_password2.val('');
        update_password1.focus();
    } else if (update_password1.val() == current_password.val()) {
        $('#newPassMatch').show();
        update_password1.val('');
        update_password2.val('');
        update_password1.focus();
    } else {
        var xmlHttp = new XMLHttpRequest();

        var json = '{"username":"' + username + '","currentPassword":"' + current_password.val() + '","newPassword":"' + update_password1.val() + '"}';

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