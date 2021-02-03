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
export class FeatureJSONParserService {

  constructor(
    public utils: UtilFunctionsService,
  ) { }

  // return left and right strings of blank spaces to enable
  // alignment of data tables
  // @param {number} colWidth - The column width
  // @param {string} text - The input text 
  getSpacers(colWidth: number, text: string): any {
    const spacers = {
      spacerLeft: "",
      spacerRight: ""
    }
    text = text === null ? '' : text;
    const spacer: number = colWidth - text.length;
    if (spacer < 1) {
      return {
        spacerLeft: " ",
        spacerRight: " "
      }
    }

    const spacerLeft: number = 1;
    const spacerRight: number = spacer - 1;
    spacers.spacerLeft = ' '.repeat(spacerLeft);
    spacers.spacerRight = ' '.repeat(spacerRight);
    return spacers;
  }

  // iterate through cells and return array of he max widths for each column
  // @param {string[][]} rows - The data table
  // @param {string[][]} rows - the header titles row
  getColumnWidths(rows: string[][], vars: string[]): number[] {
    const colWidths = [];
    for (let idx = 0; idx < vars.length; idx++) {
      colWidths.push(vars[idx].length + 2)
    }
    if (rows) {
      for (let out_idx = 0; out_idx < rows.length; out_idx++) {
        for (let idx = 0; idx < rows[out_idx].length; idx++) {
          // colWidths[idx] = rows[out_idx][idx] === null ? 6 : Math.max(colWidths[idx], rows[out_idx][idx].length + 2);
          colWidths[idx] = rows[out_idx][idx] === null ? colWidths[idx] : Math.max(colWidths[idx], rows[out_idx][idx].length + 2);
        }
      }
    }
    return colWidths;
  }

  // iterate through cells and return array of he max widths for each column
  // @param {string[][]} rows - The data table
  getColumnWidthsDataTable(rows: string[][]): number[] {
    const colWidths = [...rows[0].map((i) => i ? i.length : 2)]
    for (let out_idx = 0; out_idx < rows.length; out_idx++) {
      for (let idx = 0; idx < rows[out_idx].length; idx++) {
        colWidths[idx] = rows[out_idx][idx] === null ? colWidths[idx] : Math.max(colWidths[idx], rows[out_idx][idx].length + 2);
      }
    }
    return colWidths;
  }

  // removes rows where all values are null
  // replaces nulls with ""s in each row
  // @param {string[][]} arr - the input table
  replaceNulls(arr: string[][]): string[][] {
    //remove rows of all null values
    const arrOut = _filter(arr, (row) => {
      return _findIndex(row, v => _isString(v) || _isNumber(v)) > -1;
    })
    return arrOut.map(subArr => subArr.map(val => val === null ? '' : val))
  }

  // convert JSON scenarios into lines of text 
  extractScenariosText(feature: FeatureFile): ScenarioOutput[] {
    // loop throught scenarios
    // extract steps and data tables
    let scenariosOutput: ScenarioOutput[] = [];
    _forEach(feature.scenarios, (scenario) => {

      const gwt: ScenarioOutput = {
        given: '',
        when: '',
        then: '',
        title: '',
      };
      for (const step of ['Given', 'When', 'Then']) {
        const stepLC = step.toLowerCase();
        if (scenario[stepLC]) {
          for (let idx = 0; idx < scenario[stepLC].length; idx++) {
            // step text
            gwt[stepLC] = gwt[stepLC] + (idx === 0 ? `${step} ${scenario[stepLC][idx].text}` : `\n    And ${scenario[stepLC][idx].text}`)
            // STEP DATA TABLE
            if (scenario[stepLC][idx].dataTable) {
              const dataTableArr = this.replaceNulls(scenario[stepLC][idx].dataTable);
              const colWidths: number[] = this.getColumnWidthsDataTable(dataTableArr);
              for (let idxIn = 0; idxIn < dataTableArr.length; idxIn++) {
                let line: string = '|';
                for (let idxIn2 = 0; idxIn2 < dataTableArr[idxIn].length; idxIn2++) {
                  const spacers = this.getSpacers(colWidths[idxIn2], dataTableArr[idxIn][idxIn2])
                  line = line + `${spacers.spacerLeft}${dataTableArr[idxIn][idxIn2]}${spacers.spacerRight}|`

                }
                gwt[stepLC] = `${gwt[stepLC]}
      ${line}`
              }
            }
          }
        }
      }

      // Example Tables
      let exampleTitle: string = '';
      if (scenario.keyVars && scenario.keyVars.length > 0) {
        const exampleObj = this.extractExampleTableText(scenario)
        exampleTitle = exampleObj.exampleTitle;
        gwt.examples = `${exampleObj.exampleTitle}
      ${exampleObj.examples}`

      }

      const scenarioTitle = exampleTitle.length > 0 ? 'Scenario Outline' : 'Scenario'
      gwt.title = `${scenarioTitle}: ${scenario.scenario}`
      if (scenario.scenarioComment) {
        gwt.comment = scenario.scenarioComment;
      }
      if (scenario.tag) {
        gwt.tag = scenario.tag;
      }
      scenariosOutput.push(gwt)
    })
    return scenariosOutput;
  }

  //TODO declare return
  extractExampleTableText(scenario: Scenario): any {
    let exampleTitle: string = '';
    let examples: string = '';
    const colWidths: number[] = this.getColumnWidths(scenario.examples, scenario.keyVars);
    exampleTitle = `Examples:`;
    let line = '|'
    for (let idx = 0; idx < scenario.keyVars.length; idx++) {
      const spacers = this.getSpacers(colWidths[idx], scenario.keyVars[idx]);
      line = line + `${spacers.spacerLeft}${scenario.keyVars[idx]}${spacers.spacerRight}|`
    }
    exampleTitle = `${exampleTitle}
      ${line}`

    if (scenario.examples && scenario.examples[0] && scenario.examples[0].length > 0) {
      const examplesArr = this.replaceNulls(scenario.examples)
      //skip first which is titles
      // use keyVars for title
      for (let out_idx = 1; out_idx < examplesArr.length; out_idx++) {
        let line: string = '|';
        for (let idx = 0; idx < examplesArr[out_idx].length; idx++) {
          const spacers = this.getSpacers(colWidths[idx], examplesArr[out_idx][idx]);
          line = line + `${spacers.spacerLeft}${examplesArr[out_idx][idx]}${spacers.spacerRight}|`
        }
        examples = examples ? `${examples}
      ${line}` : `${line}`
      }
    }

    return { exampleTitle, examples }
  }

  extractBackgroundText(feature: FeatureFile): string {
    let background = `Background:
    `
    for (let idx = 0; idx < feature.background.length; idx++) {
      background = background + (idx === 0 ? `Given ${feature.background[idx].text}` : `\n    And ${feature.background[idx].text}`)
      if (feature.background[idx].dataTable) {
        const dataTableArr = this.replaceNulls(feature.background[idx].dataTable);
        const colWidths: number[] = this.getColumnWidthsDataTable(dataTableArr);
        for (let idxIn = 0; idxIn < dataTableArr.length; idxIn++) {
          let line: string = '|';
          for (let idxIn2 = 0; idxIn2 < dataTableArr[idxIn].length; idxIn2++) {
            const spacers = this.getSpacers(colWidths[idxIn2], dataTableArr[idxIn][idxIn2])
            line = line + `${spacers.spacerLeft}${dataTableArr[idxIn][idxIn2]}${spacers.spacerRight}|`

          }
          background = `${background}
      ${line}`
        }
      }
    }
    return background
  }

  // make sure tags start with @
  parseTag(str: string): string {
    if (str.trim().startsWith('@')) {
      return str.trim()
    } else {
      return `@${str.trim()}`
    }
  }

  convertJSONtoFeature(feature: FeatureFile): string {
    let _scenariosOutput: ScenarioOutput[] = this.extractScenariosText(feature);
    let background: string;
    if (feature.background && feature.background[0] && feature.background[0].text && feature.background[0].text.length > 1) {
      background = this.extractBackgroundText(feature)
    }
    // Build Feature File

    let featureFileText: string;

    if (feature.featureTag) {
      featureFileText = `${this.parseTag(feature.featureTag)}
Feature: ${feature.feature}`
    } else {
      featureFileText = `Feature: ${feature.feature}`
    }


    if (feature.featureComment) {
      featureFileText = `${featureFileText}
${feature.featureComment}`
    }

    if (background) {
      featureFileText = `${featureFileText}
  ${background}`
    }

    _forEach(_scenariosOutput, (scenario) => {
      featureFileText = `${featureFileText}
`

      if (scenario.tag) {
        featureFileText = `${featureFileText}
  ${this.parseTag(scenario.tag)}
  ${scenario.title}`
      } else {
        featureFileText = `${featureFileText}
  ${scenario.title}`
      }


      if (scenario.comment) {
        featureFileText = `${featureFileText}
  ${scenario.comment}`
      }

      featureFileText = `${featureFileText}
    ${scenario.given}
    ${scenario.when}
    ${scenario.then}`

      if (scenario.examples) {
        featureFileText = `${featureFileText}

    ${scenario.examples}`
      }
    })
    return featureFileText
  }



}
