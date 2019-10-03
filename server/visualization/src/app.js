var http = require("http");
var fs = require("fs");
var config = require('./config.js');

fs.readFile('./index.html', function (err, html) {
    if (err) {
        throw err;
    }
    http.createServer(function (request, response) {
        response.writeHeader(200, {"Content-Type": "text/html"});
        response.write(html);
        response.end();
    }).listen(config.port);
});

// Console will print the message
console.log('Server running at http://127.0.0.1:' + config.port);