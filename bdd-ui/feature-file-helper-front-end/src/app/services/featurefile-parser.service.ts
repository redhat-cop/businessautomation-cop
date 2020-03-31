import { Injectable } from '@angular/core';
import { FeatureFile, ParseFileIn, Step, Scenario, ScenarioOutput } from '../models/models'
import { UtilFunctionsService } from 'src/app/services/util-functions.service';
import _filter from 'lodash/filter';
import _uniq from 'lodash/uniq';
import _findIndex from 'lodash/findIndex';
import _forEach from 'lodash/forEach';
import _isString from 'lodash/isString';
import _isNumber from 'lodash/isNumber';

@Injectable({
  providedIn: 'root'
})
export class FeatureFileParserService {

  constructor(
    public utils: UtilFunctionsService,
  ) { }


  // search for next Scenario: or Scenario Outline, return null if not found
  findNextScenario(_lineArr: string[], _lineNumberCurrent: number): number | null {
    const min = [this.findLineByString(_lineArr, 'Scenario:', _lineNumberCurrent), this.findLineByString(_lineArr, 'Scenario Outline:', _lineNumberCurrent)].reduce(function (m, o) {
      return (o != null && o < m) ? o : m;
    }, Infinity);
    return min === Infinity ? null : min;
  }

  extractSubstring(strIn: string, removeString: string): string {
    const start = strIn.indexOf(removeString)
    return start > -1 ? strIn.substring(start + removeString.length).trim() : null;
  }

  findLineByString(arr: string[], strToFind: string, startPosn: number, endPosn?: number): number {
    if (startPosn < 0 || endPosn < 0) {
      return null;
    }
    const cloneArr = [...arr];
    cloneArr.splice(0, startPosn)
    if (endPosn) {
      cloneArr.splice(endPosn - startPosn + 1, arr.length - endPosn)
    }
    const lineNum = _findIndex(cloneArr, (line) => line.indexOf(strToFind) > -1);
    return lineNum > -1 ? lineNum + startPosn : null;
  }

  // extract feature and scenario comments
  // check for and exclude @tags
  extractComment(arr: string[], lineNumberStart: number, lineNumberStop: number): string {
    const numOfLines = lineNumberStop - lineNumberStart;
    if (numOfLines === 1) {
      return arr[lineNumberStart].trim().startsWith('@') ? '' : arr[lineNumberStart].trim();
    } else { //multiple lines of comments
      let str = '';
      for (let idx = lineNumberStart; idx < lineNumberStart + numOfLines; idx++) {
        str = str + (arr[idx].trim().startsWith('@') ? '' : arr[idx].trim() + " ");
      }
      return str
    }
  }

  extractDataTable(strIn: string): string[] {
    // strIn:  | Dispute Total Amount | Partner |
    try {
      const strArr = strIn.split('|').map((item: string) => item.trim());
      // ["", "Dispute Total Amount", "Partner", ""]
      strArr.splice(0, 1)
      strArr.splice(strArr.length - 1, 1)
      return strArr;
    }
    catch (err) {
      console.error('ERROR parsing data table ', err)
      return [];
    }
  }

  // convert feature file into array of lines, omitting comments and blanks
  featureFileToLinesArray(file: string): string[] {
    let lineArr: string[] = file.split('\n')
    return _filter(lineArr, (line) => line.trim() !== "" && !line.trim().startsWith('#'));
  }

  // extract scenarios from feature file text format
  // get start and end lines of scenaario and return as string separted by \n
  extractScenario(featureFile: string, scenarioTitle: string): string {
    let lineArr = this.featureFileToLinesArray(featureFile)
    const start = this.findLineByString(lineArr, scenarioTitle, 0)
    if (!start) {
      return "Scenario not found"
    }
    const end1 = this.findLineByString(lineArr, "Scenario:", start + 1)
    const end2 = this.findLineByString(lineArr, "Scenario Outline:", start + 1)
    const end = Math.min(end1 || lineArr.length - 1, end2 || lineArr.length - 1)
    return lineArr.splice(start, end - start).join('\n');
  }

  // checks if line before Feature or Scenario line starts with @
  checkForTag(arr: string[], current: number): string | null {
    if (current <= 0) return null;
    return arr[current - 1].trim().startsWith('@') ? arr[current - 1].trim() : null
  }

  convertFeatureFiletoJSON(fileIn: string, fileName: string, pathName: string): ParseFileIn {
    const featureFile: FeatureFile = {
      feature: "",
      scenarios: [],
      showErrors: false,
      pathName: pathName,
      fileName: fileName
    }
    const name = "Parse File Error";

    try {

      let lineArr = this.featureFileToLinesArray(fileIn)

      let lineNumberNext: number;
      let lineNumberEnd: number;

      // get Feature Title
      let lineNumberCurrent = this.findLineByString(lineArr, 'Feature:', 0)
      if (lineNumberCurrent > -1 && this.extractSubstring(lineArr[lineNumberCurrent], 'Feature:')) {
        featureFile.feature = this.extractSubstring(lineArr[lineNumberCurrent], 'Feature:')
        // check line above for tag
        let checkTag = this.checkForTag(lineArr, lineNumberCurrent)
        if (checkTag) {
          featureFile.featureTag = checkTag;
        }
      } else {
        throw { name: "Parse File Error", message: "Unable to read Feature statement in Feature File" }
      }

      // check for background
      lineNumberCurrent++;
      lineNumberNext = this.findLineByString(lineArr, 'Background:', lineNumberCurrent)
      // check for feature comment
      if (lineNumberNext > lineNumberCurrent) {
        featureFile.featureComment = this.extractComment(lineArr, lineNumberCurrent, lineNumberNext)
      }

      // Parse background section
      if (lineNumberNext) {
        // found background, parse it

        // first  find scenario to mark end of the backgroound serction
        lineNumberEnd = this.findNextScenario(lineArr, lineNumberCurrent)
        if (!lineNumberEnd) {
          throw { name, message: "Unable to find Scenario statement in Feature File" }
        }

        // find given and And statements in background
        lineNumberCurrent = this.findLineByString(lineArr, 'Given', lineNumberCurrent, lineNumberEnd)
        // find given statement
        if (lineNumberCurrent > -1 && this.extractSubstring(lineArr[lineNumberCurrent], 'Given')) {
          featureFile.background = [{ text: this.extractSubstring(lineArr[lineNumberCurrent], 'Given'), dataTable: null }]
        } else {
          throw { name, message: `Unable to read Background statement in Feature File, there is no Given step` }
        }

        // get Ands in background
        lineNumberCurrent++ // move onto next line

        // check these lines for more And or Given Statements
        if (lineNumberEnd > lineNumberCurrent) {
          let dataTable: string[][] = [];
          for (let idxIn = lineNumberCurrent; idxIn < lineNumberEnd; idxIn++) {
            if (lineArr[idxIn].trim().startsWith('|')) {
              dataTable.push(this.extractDataTable(lineArr[idxIn]))
            } else {
              //check if theres a data table to be added from previous step
              if (dataTable.length > 0) {
                const lastIndex = featureFile.background.length - 1;
                featureFile.background[lastIndex].dataTable = dataTable
              }
              dataTable = [];
              const andStr = this.extractSubstring(lineArr[idxIn], 'And') || this.extractSubstring(lineArr[idxIn], 'Given');
              if (andStr) {
                featureFile.background.push({ text: andStr, dataTable: null })
              }
            }
          }
          if (dataTable.length > 0) {
            const lastIndex = featureFile.background.length - 1;
            featureFile.background[lastIndex].dataTable = dataTable
          }
        }
        lineNumberNext = lineNumberCurrent = lineNumberEnd  // now at first scenario
      } else {
        // no background section move onto scanario
        lineNumberNext = this.findNextScenario(lineArr, lineNumberCurrent)
      }

      if (!lineNumberNext) {
        throw { name, message: "Unable to read Scenario statement in Feature File" }
      }

      //check for feature comment
      // TODO check is this comment bit necessary  
      if (lineNumberNext > lineNumberCurrent) {
        featureFile.featureComment = this.extractComment(lineArr, lineNumberCurrent, lineNumberNext)
      }

      lineNumberCurrent = lineNumberNext;  //set current line to first scenario

      // parse for multiple Scenario 
      featureFile.scenarios = this.convertScenarioToJSON(lineNumberCurrent, lineArr)

    } catch (err) {
      console.error('ERROR in convertFeatureFiletoJSON', err)
      return { ok: false, error: err }
    }

    return { ok: true, featureFile }
  }

  convertScenarioToJSON(lineNumberCurrent: number, lineArr: string[]): Scenario[] {

    let scenarioIdx = 1
    let lineNumberNextStep: number;
    let isAnotherScenario = true;
    const scenarios: Scenario[] = [];
    while (isAnotherScenario) {
      const scenario: Scenario = {
        scenario: "",
        given: [{ text: "", dataTable: null }],
        when: [{ text: "", dataTable: null }],
        then: [{ text: "", dataTable: null }],
        keyVars: []
      }

      //check for scenario tag
      let checkTag = this.checkForTag(lineArr, lineNumberCurrent)
      if (checkTag) {
        scenario.tag = checkTag;
      }

      // find next scenario, if none set to end of file
      let nextScenario = this.findNextScenario(lineArr, lineNumberCurrent + 1);
      if (!nextScenario) {
        nextScenario = lineArr.length; // set to last line number
        isAnotherScenario = false;
      }

      // if there is gap between scenario and given, extract it as a comment
      const lineNumberGiven = this.findLineByString(lineArr, 'Given', lineNumberCurrent, nextScenario)
      if (lineNumberGiven - lineNumberCurrent > 1) {
        scenario.scenarioComment = this.extractComment(lineArr, lineNumberCurrent + 1, lineNumberGiven)
      }

      // extract scenario statement
      scenario.scenario = this.extractSubstring(lineArr[lineNumberCurrent], 'Scenario Outline:') || this.extractSubstring(lineArr[lineNumberCurrent], 'Scenario:')

      //get Given / When / Then Steps
      const steps = ['Given', 'When', 'Then', 'Examples']
      for (let idx = 0; idx < steps.length - 1; idx++) {

        //get Given When Then Statement 
        lineNumberCurrent = this.findLineByString(lineArr, steps[idx], lineNumberCurrent, nextScenario)
        if (lineNumberCurrent && this.extractSubstring(lineArr[lineNumberCurrent], steps[idx])) {
          scenario[steps[idx].toLowerCase()] = [{ text: this.extractSubstring(lineArr[lineNumberCurrent], steps[idx]), dataTable: null }]
        } else {
          throw { name, message: `Unable to read ${steps[idx]} statement in Feature File Scenario #${scenarioIdx}` }
        }

        //get Ands
        lineNumberCurrent++
        lineNumberNextStep = this.findLineByString(lineArr, steps[idx + 1], lineNumberCurrent, nextScenario)

        // if there's no Examples Table, set to check to end of file
        if (!lineNumberNextStep && steps[idx + 1] === 'Examples') {
          lineNumberNextStep = nextScenario;
        }
        if (!lineNumberNextStep) throw { name, message: `Unable to read ${steps[idx + 1]} Scenario #${scenarioIdx}` }

        // extract data tables and  Ands
        // TODO duplicate code put this in function
        if (lineNumberNextStep > lineNumberCurrent) { //there's table or AND
          let dataTable: string[][] = [];
          for (let idxIn = lineNumberCurrent; idxIn < lineNumberNextStep; idxIn++) {
            if (lineArr[idxIn].trim().startsWith('|')) {
              dataTable.push(this.extractDataTable(lineArr[idxIn]))
            } else {
              //check if theres a data table to be added from previous step
              if (dataTable.length > 0) {
                const lastIndex = scenario[steps[idx].toLowerCase()].length - 1;
                scenario[steps[idx].toLowerCase()][lastIndex].dataTable = dataTable
              }
              dataTable = [];
              const andStr = this.extractSubstring(lineArr[idxIn], 'And');
              if (andStr) {
                scenario[steps[idx].toLowerCase()].push({ text: andStr, dataTable: null })
              }
            }
          }
          if (dataTable.length > 0) {
            const lastIndex = scenario[steps[idx].toLowerCase()].length - 1;
            scenario[steps[idx].toLowerCase()][lastIndex].dataTable = dataTable
          }
        }
      } //end of parse steps

      //get example table
      lineNumberCurrent = this.findLineByString(lineArr, 'Examples:', lineNumberCurrent, nextScenario)
      if (lineNumberCurrent) {
        const examples: string[][] = [];
        lineNumberCurrent++
        for (let idx = lineNumberCurrent; idx < nextScenario; idx++) {
          if (lineArr[idx].trim().startsWith('|')) {
            examples.push(this.extractDataTable(lineArr[idx]))
          }
        }
        scenario.examples = examples
      }

      // create varsArray
      let keyVars = []
      for (const stepsArr of [scenario.given, scenario.when, scenario.then]) {
        for (const step of stepsArr) {
          keyVars = keyVars.concat(this.utils.extractVars(step.text, []));
        }
      }
      scenario.keyVars = _uniq(keyVars);

      scenarioIdx++

      lineNumberCurrent = nextScenario; // set lineNumberCurrent to start of next scenarios for next loop iteration
      scenarios.push(scenario)

    }

    return scenarios

  }

}
