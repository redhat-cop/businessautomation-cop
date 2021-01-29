
/*
 * Log levels:
 *
 * 0 : error
 * 1 : warn
 * 2 : info
 * 3 : verbose
 * 4 : debug
 * 5 : silly
 *
 */

const { createLogger, format, transports } = require('winston');
const { combine, timestamp, printf, splat, simple, colorize, prettyPrint } = format;

const myFormat = printf(({ message, level, timestamp, ...meta }) => {
  return `${timestamp} ${level}: ${message}  ${(Object.entries(meta).length === 0 && meta.constructor === Object) ? '' : JSON.stringify(meta)}`;
});

const logger = createLogger({
  level: process.env.LOG_LEVEL || "info",
  format: combine(
    colorize(),
    timestamp(),
    splat(),
    myFormat
  ),
  transports: [
    new transports.Console()
  ]
});

function setLoggerLevel(logLevel) {
  logger.transports.console.level = logLevel
}

module.exports = {
  logger: logger,
  setLoggerLevel: setLoggerLevel,
};