// parser code to extract Given / When / Then steps and data tab;les from feature files
const _ = require('lodash')

const log = require('./logger').logger

// check if line is a valid step
// checks 'line' string to see if first word is contained in array 'stepsA'
// @param line: string
// @param stepsA: array of string e.g. ['given', 'when']
function isStep(line, stepsA) {
  line = line.trim();
  const i = Math.max(line.indexOf(' '), line.indexOf(':'));
  const firstWord = line.substring(0, i);
  return stepsA.indexOf(firstWord.toLowerCase())
}

// remove initial Given/When/Then text from strIn and return string
function extractSubstring(strIn, removeString, steps) {
  // get first word on line
  const stepsAnd = [...steps, 'and']
  if (isStep(strIn, stepsAnd) > -1) {
    const start = strIn.indexOf(removeString.substring(1))
    return start > -1 ? strIn.substring(start + removeString.length - 1).trim() : null;
  }
  return null
}

// get Scenario text from strIn
// remove the intial 'Scenario:' or 'Scenario Outline:'
function extractScenario(strIn) {
  // get first word on line
  if (strIn.trim().startsWith('Scenario:') || strIn.trim().startsWith('scenario:')) {
    return strIn.trim().substring(9).trim()
  } else if (strIn.trim().startsWith('Scenario Outline:') || strIn.trim().startsWith('scenario outline:')) {
    return strIn.trim().substring(17).trim()
  }
  return '';
}

// iterate through feature file text and find next step
// lineArr: array of lines from feature file
// startPosn: line number to start iterations feom
// steps: array of steps to search for
function findNextStep(lineArr, startPosn, steps) {
  const cloneArr = [...lineArr];
  cloneArr.splice(0, startPosn)
  let lineNum = null;
  for (let index = 0; index < cloneArr.length; index++) {
    if (isStep(cloneArr[index], steps) > -1) {
      lineNum = index + startPosn;
      break;
    }
  }
  return lineNum;
}

// extract data table row as array ot strings
function extractDataTable(strIn) {
  // strIn:  | Dispute Total Amount | Partner |
  try {
    const strArr = strIn.split('|').map((item) => item.trim());
    // ["", "Dispute Total Amount", "Partner", ""]
    strArr.splice(0, 1)
    strArr.splice(strArr.length - 1, 1)
    return strArr;
  }
  catch (err) {
    log.error('Error in extractDataTable ', err)
    return [];
  }
}

// parse feature file and extract scenario titles and Given / When / Then statements/data tables
function extractStepsAndScenarios(fileText) {
  const stepsObj = {
    given: [],
    when: [],
    then: [],
    scenarios: []
  };
  const stepsArr = ['given', 'when', 'then']
  let lineNumberCurrent = 0;
  let lineNumberEnd
  let currentStepIdx;

  //split feature file into array of line and remove blank lines and comments
  let lineArr = fileText.split('\n')
  lineArr = _.filter(lineArr, (line) => line.trim() !== "" && !line.trim().startsWith('#'));

  // iterate through lines
  while (lineNumberCurrent < lineArr.length) {

    // check if current line is scenario
    if (extractScenario(lineArr[lineNumberCurrent])) {
      stepsObj.scenarios.push(extractScenario(lineArr[lineNumberCurrent]))
      lineNumberCurrent++
      continue;
    }
    // check if current line is a given when then statement
    currentStepIdx = isStep(lineArr[lineNumberCurrent], stepsArr);
    if (currentStepIdx > -1) {
      // add step statment to it's output array
      stepsObj[stepsArr[currentStepIdx]].push({ text: extractSubstring(lineArr[lineNumberCurrent], stepsArr[currentStepIdx], stepsArr), dataTable: null })

      // find the next G/W/T step OR an example table OR end of file
      lineNumberEnd = findNextStep(lineArr, lineNumberCurrent + 1, [...stepsArr, 'examples']) || lineArr.length - 1

      // go through interim lines and extraxt datatables and AND statements
      if (lineNumberEnd - lineNumberCurrent > 1) {
        lineNumberCurrent++
        // extract data tables and And statements
        let dataTable = [];
        for (let idx = lineNumberCurrent; idx < lineNumberEnd; idx++) {
          // check for scenario statement
          if (extractScenario(lineArr[idx])) {
            stepsObj.scenarios.push(extractScenario(lineArr[idx]))
            lineNumberCurrent++
            continue
          }
          // if line startes with | add to datatable
          if (lineArr[idx].trim().indexOf('|') === 0) {
            dataTable.push(extractDataTable(lineArr[idx]))
          } else {
            //check if theres a data table to be added from previous step
            if (dataTable.length > 0) {
              const lastIndex = stepsObj[stepsArr[currentStepIdx]].length - 1;
              stepsObj[stepsArr[currentStepIdx]][lastIndex].dataTable = dataTable
            }
            dataTable = [];
            // check if theres AND
            const andStr = extractSubstring(lineArr[idx], 'And', stepsArr);
            if (andStr) {
              stepsObj[stepsArr[currentStepIdx]].push({ text: andStr, dataTable: null })
            }
          }
        }
        if (dataTable.length > 0) {
          const lastIndex = stepsObj[stepsArr[currentStepIdx]].length - 1;
          stepsObj[stepsArr[currentStepIdx]][lastIndex].dataTable = dataTable
        }

        lineNumberCurrent = lineNumberEnd

      } else {
        lineNumberCurrent++
      }

    } else {
      lineNumberCurrent++
    }
  }

  return stepsObj
}

module.exports = {
  extractStepsAndScenarios
};


