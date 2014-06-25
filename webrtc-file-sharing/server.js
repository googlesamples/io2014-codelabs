'use strict';

var nodeStatic = require('node-static');
var http = require('http');
var file = new(nodeStatic.Server)();
var app = http.createServer(function(req, res) {
  file.serve(req, res);
}).listen(2014);
