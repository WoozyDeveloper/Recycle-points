const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    //credential: admin.credential.refreshToken(refreshToken),
    databaseURL: 'https://actionlab-48265.firebaseio.com'
});

var db = admin.database();


exports.getrole = functions.https.onCall((data,context) => {
    var email = context.auth.token.email;
    var username = email.split('@').map( (val) => {
        return String(val);
    })[0];
    var role;

    var ref = db.ref('roles/' + username);
    ref.on("value", (snapshot) => {
        role = snapshot.val();
    }, (errorObject) => {
        console.log("The read failed: " + errorObject.code);
    });
    return role;
});


exports.setupnames = functions.https.onCall((data, context) => {
    var ref = db.ref ('institutions/');
    var name = null;
    var id = null;
    var map = [];
    ref.on("value", (snapshot) => {
        snapshot.forEach ((element) => {
            name = element.child('name').val();
            id = element.key;
            var n = map.length;
            map[n] = {"name": name, "id": id};
        });
    });
    return map;
});

exports.getinfo = functions.https.onCall((data,context) => {
    var ref = db.ref ('institutions/');
    var map = [];
    ref.on("value", (snapshot) => {
        snapshot.forEach ((element) => {
            var id = element.key;
            var lat = element.child('coordinates').child('latitude').val();
            var lng = element.child('coordinates').child('longitude').val();
            var name = element.child('name').val();
            var percents = element.child('percents').val();
            var email = element.child('contact').child('email').val();
            var person = element.child('contact').child('person').val();
            var phone = element.child('contact').child('phone').val();

            var n = map.length;
            map[n] = {"id": id, "name": name, "latitude": lat, "longitude":lng, "percents": percents,
                        "email": email, "person": person, "phone": phone};
        });
    });
    return map;
});

//formă finală
exports.getvalues = functions.https.onCall((data,context) => {
    var email = context.auth.token.email;
    var username = email.split('@').map( (val) => {
        return String(val);
    })[0];
    var ref = db.ref ('institutions/' + username + '/percents');
    var percents = [];
    ref.on("value", (snapshot) => {
            snapshot.val().forEach((element) => {
                percents[percents.length] = element;
            });
    }, (errorObject) => {
        console.log("The read failed: " + errorObject.code);
    });
    return {"percent0": percents[0], "percent1": percents[1], "percent2": percents[2]};
});



//formă finală
exports.updatevalues = functions.https.onCall((data, context) => {
    var email = context.auth.token.email;
    var username = email.split('@').map( (val) => {
        return String(val);
    })[0];

    var ref = db.ref('institutions/' + username);
    var percents = [data.percent0,data.percent1,data.percent2];
    
    return ref.update({
        percents: percents
    }, (error) => {
        console.log(error);
    });
});


//formă finală
exports.getinstitutionname = functions.https.onCall((data, context) => {
    var email = context.auth.token.email;
    var username = email.split('@').map((val) => {
        return String(val);
    })[0];

    var ref = db.ref('institutions/' + username + '/name');
    let name;

    ref.on("value", (snapshot) => {
        name = snapshot.val();
    }, (errorObject) => {
        console.log("The read failed: " + errorObject.code);
    });
    console.log(name);
    return {"name": name};
});

