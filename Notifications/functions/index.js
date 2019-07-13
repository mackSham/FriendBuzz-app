'use strict'
const functions = require('firebase-functions');
const admin = require('firebase-admin');

exports.sendBlockNotifications = functions.database.ref('/Block/{reciever_id}/{sender_id}').onWrite((snapshot, context) => {
      // Grab the current value of what was written to the Realtime Database.
      const original = snapshot.val();
      console.log('The id is', context.params.reciever_id, original);
      const uppercase = original.toUpperCase();
      // You must return a Promise when performing asynchronous tasks inside a Functions such as
      // writing to the Firebase Realtime Database.
      // Setting an "uppercase" sibling in the Realtime Database returns a Promise.
      //return snapshot.ref.parent.child('uppercase').set(uppercase);
    });

admin.initializeApp(functions.config().firebase);
