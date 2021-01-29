var createError = require('http-errors');
var express = require('express');
var cors = require('cors');
var path = require('path');
var cookieParser = require('cookie-parser');
var morganLoger = require('morgan');

var indexRouter = require('./routes/index');
var remoteRepo = require('./routes/remoteRepo');
var log = require('./lib/logger').logger


var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'pug');

// log emdpoint hits
if (process.env.APP_ENV && process.env.APP_ENV.toLowerCase() == 'prod') {
  app.use(morganLoger('common', { skip: function(req, res) { return res.statusCode < 400 }}));
} else {
  app.use(morganLoger(':date[iso] :method :url :status :response-time ms - :res[content-length]'));
}

app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(cors());
app.use(express.static(path.join(__dirname, 'public')));

app.use(function(req, res, next) {
  next();
});

app.use('/', indexRouter);
app.use('/remoteRepo', remoteRepo);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

var port = 3000;
app.set('port', port);
var http = require('http');
var server = http.createServer(app);
server.listen(port, function () {
  log.info('Feature File Back End App started on ' + port);
});

module.exports = app;
