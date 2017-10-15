initApp = function () {
    firebase.auth().onAuthStateChanged(function (user) {
        if (user) {
            // User is signed in.
                $('#sign-out').show();
        } else {
            // User is signed out.
                $('#sign-out').hide();
            disableLinks();
            if (window.location.pathname !== "/Login") {
                console.log("Redirect");
                window.location = '/Login';
            }
        }
    }, function (error) {
        console.log(error);
    });
};

window.addEventListener('load', function () {
    initApp();
    document.getElementById('sign-out').addEventListener('click', function () {
        firebase.auth().signOut();
    });
});

function disableLinks() {
    $("a[data-placement='right']").attr('href', '/Login');
}