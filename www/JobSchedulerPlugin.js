"use strict";

var exec = require("cordova/exec");
var MODULE = "JobSchedulerPlugin";

var noop = function() {};

module.exports = {
  STATUS_RESTRICTED: 0,
  STATUS_DENIED: 1,
  STATUS_AVAILABLE: 2,

  configure: function(callback, failure, config) {
    if (typeof callback !== "function") {
      throw "JobSchedulerPlugin configure error:  You must provide a callback function as 1st argument";
    }

    config = config || {};
    failure = failure || noop;

    exec(callback, failure, MODULE, "configure", [config]);
  },
  finish: function() {
    exec(noop, noop, MODULE, "finish", []);
  },
  start: function(success, failure) {
    success = success || noop;
    failure = failure || noop;
    exec(success, failure, MODULE, "start", []);
  },
  stop: function(success, failure) {
    success = success || noop;
    failure = failure || noop;
    exec(success, failure, MODULE, "stop", []);
  },
  status: function(success, failure) {
    success = success || noop;
    failure = failure || noop;
    exec(success, failure, MODULE, "status", []);
  }
};
