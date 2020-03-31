import { Injectable, Inject } from '@angular/core';
import { FeatureFile, VarUpdate, Step, Scenario, ExampleError, StepStatements, RemoteDataObj, RemoteRepo } from '../models/models'
import { LOCAL_STORAGE, StorageService, isStorageAvailable } from 'angular-webstorage-service';
import { STATEMENTS } from '../models/hardcodedStatements'
import _isArray from 'lodash/isArray'
import _isString from 'lodash/isString'
import _isNumber from 'lodash/isNumber'
import _forEach from 'lodash/forEach'

const KEY = 'feature_file_tool_'
const FEATURE_FILE_STORAGE = KEY + "FEATURE_FILE";
const STEPSTATEMENTS_STORAGE = KEY + "STEPSTATEMENTS";
const REMOTEPATHS_STORAGE = KEY + "REMOTEPATHS";
const REMOTESCENARIOS_STORAGE = KEY + "REMOTESCENARIOS";
const REMOTE_REPO = KEY + "REMOTEREPO";

@Injectable({
  providedIn: 'root'
})
export class FeatureStoreService {
  featureFile: FeatureFile;
  stepStatements: StepStatements;
  remoteScenarios: RemoteDataObj[];
  remotePaths: string[];
  remoteRepo: RemoteRepo;

  constructor(
    @Inject(LOCAL_STORAGE)
    private storage: StorageService
  ) {

    this.featureFile = this.storage.get(FEATURE_FILE_STORAGE) || {
      feature: "",
      showErrors: false,
      scenarios: [this.newScenario],
    }

    this.stepStatements = this.storage.get(STEPSTATEMENTS_STORAGE) || STATEMENTS;
    this.remotePaths = this.storage.get(REMOTEPATHS_STORAGE) || [];
    this.remoteScenarios = this.storage.get(REMOTESCENARIOS_STORAGE) || [];
    this.remoteRepo = this.storage.get(REMOTE_REPO) || null;
  }

  newScenario(): Scenario {
    return {
      scenario: "",
      given: [{ text: "", dataTable: null }],
      when: [{ text: "", dataTable: null }],
      then: [{ text: "", dataTable: null }],
      keyVars: []
    }
  }

  save() {
    this.storage.set(FEATURE_FILE_STORAGE, this.featureFile);
  }

  setShowErrors(val: boolean) {
    this.featureFile.showErrors = val;
    this.save();
  }

  getShowErrors(): boolean {
    return this.featureFile.showErrors;
  }

  getFeatureFile(): FeatureFile {
    return this.featureFile;
  }

  setFeature(feature: string): void {
    this.featureFile.feature = feature;
    this.save();
  }

  getFeature(): string {
    return this.featureFile.feature;
  }

  getScenarios(): Scenario[] {
    return this.featureFile.scenarios
  }

  setScenarios(scenarios: Scenario[]) {
    this.featureFile.scenarios = scenarios;
    this.save()
  }

  setScenarioText(scenario: string, idx: number): void {
    this.featureFile.scenarios[idx].scenario = scenario;
    this.checkIfValid(this.featureFile.scenarios);
    this.save();
  }

  getScenarioText(idx: number): string {
    return this.featureFile.scenarios[idx].scenario;
  }

  setScenarioComment(scenarioComment: string, idx: number): void {
    this.featureFile.scenarios[idx].scenarioComment = scenarioComment;
    this.save();
  }

  getScenarioComment(idx: number): string {
    return this.featureFile.scenarios[idx].scenarioComment;
  }

  setScenarioTag(scenarioTag: string, idx: number): void {
    this.featureFile.scenarios[idx].tag = scenarioTag;
    this.save();
  }

  getScenarioTag(idx: number): string {
    return this.featureFile.scenarios[idx].tag;
  }

  setFeatureComment(feature: string): void {
    this.featureFile.featureComment = feature;
    this.save();
  }

  getFeatureComment(): string {
    return this.featureFile.featureComment;
  }

  setFeatureTag(featureTag: string): void {
    this.featureFile.featureTag = featureTag;
    this.save();
  }

  getFeatureTag(): string {
    return this.featureFile.featureTag;
  }

  isValidSteps(type: string) {
    return ['scenario', 'given', 'when', 'then', 'background'].indexOf(type) > -1
  }

  setSteps(type: string, stepsArray: Step[], idx: number) {
    if (!this.isValidSteps(type.toLowerCase())) {
      return
    }
    if (type === "BACKGROUND") {
      this.featureFile.background = stepsArray;
    } else {
      this.featureFile.scenarios[idx][type.toLowerCase()] = stepsArray;
      // update errors
      this.checkIfValid(this.featureFile.scenarios);
    }
    this.save()
  }

  getSteps(type: string, idx: number): Step[] {
    if (!this.isValidSteps(type.toLowerCase())) {
      console.error('Error getting Given When Then steps ', type);
      return null;
    }
    if (type === "BACKGROUND") {
      return this.featureFile.background;
    }
    return this.featureFile.scenarios[idx][type.toLowerCase()];
  }

  setVars(varsArray: string[], idx: number) {
    this.featureFile.scenarios[idx].keyVars = varsArray;
    this.save()
  }

  getVars(idx: number): string[] {
    return this.featureFile.scenarios[idx].keyVars;
  }

  setExamples(exArray: string[][], idx: number) {
    this.featureFile.scenarios[idx].examples = exArray;
    this.checkIfValid(this.featureFile.scenarios);
    this.save()
  }

  getExamples(idx: number): string[][] {
    return this.featureFile.scenarios[idx].examples;
  }

  loadNewFeatureFile(featureFile: FeatureFile) {
    if (featureFile && featureFile.feature) {
      this.featureFile = featureFile;
    } else {
      this.featureFile = {
        feature: "",
        showErrors: false,
        scenarios: [this.newScenario()],
        fileName: null,
        pathName: null
      }
    }
    this.save();
  }

  resetTableData(idx: number) {
    this.setExamples(null, idx);
    this.save();
  }

  resetFilePathNames() {
    this.featureFile.fileName = null;
    this.featureFile.pathName = null;
    this.save()
  }

  // called from HomeComponent - checks if data has been added to feature file in UI
  checkFeatureFileHasData(): boolean {
    return (this.getFeature() && this.getFeature().length > 1) ||
      (this.getScenarioText[0] && this.getScenarioText[0].length > 0) ||
      (this.getSteps('Given', 0) && this.getSteps('Given', 0)[0].text.length > 1) ||
      (this.getSteps('When', 0) && this.getSteps('When', 0)[0].text.length > 1) ||
      (this.getSteps('Then', 0) && this.getSteps('Then', 0)[0].text.length > 1)
  }

  // called from HomeComponent - checks if data has been added to scenario in UI
  checkScenarioHasData(idx: number): boolean {
    return (this.getScenarioText[idx] && this.getScenarioText[idx].length > 0) ||
      (this.getSteps('Given', idx) && this.getSteps('Given', idx)[0].text.length > 1) ||
      (this.getSteps('When', idx) && this.getSteps('When', idx)[0].text.length > 1) ||
      (this.getSteps('Then', idx) && this.getSteps('Then', idx)[0].text.length > 1)
  }

  isFeatureFileValid(): boolean {
    const scenariosOK = this.checkIfValid(this.featureFile.scenarios);

    return this.featureFile.feature.length > 0 &&
      _isArray(this.featureFile.scenarios) && this.featureFile.scenarios.length > 0 &&
      scenariosOK;

  }

  // validate scenarios and add errorMessages array to each scenario
  checkIfValid(_scenarios: Scenario[]): boolean {
    let isValid = true;
    //check scenario
    _forEach(_scenarios, scenario => {
      scenario.errorMessages = []
      // check thre is a scenario statement
      if (!scenario.scenario || scenario.scenario.length < 1) {
        scenario.errorMessages.push("Error: Missing Scenario statement")
        isValid = false;
      }

      //checks vars and example tabls
      if (scenario.keyVars && scenario.keyVars.length > 0) {
        if (!scenario.examples) {
          scenario.errorMessages.push("Error: Tags present but no values entered in Scenario Examples Table")
          isValid = false;
        } else if (scenario.examples[0].length !== scenario.keyVars.length) {
          scenario.errorMessages.push("Error: Number of columns in Scenario Examples Table does not match Tags from Given / When / Then")
          isValid = false;
        } else if (this.anyMissingValues(scenario.examples)) {
          scenario.errorMessages.push("Error: Missing values in Scenario Examples Table, every cell must contain text or a number")
          isValid = false;
        }
      }
    })

    // check given/when/then
    _forEach(_scenarios, scenario => {

      const stepTypes = ['given', 'when', 'then']

      //check there's text for each step
      _forEach(stepTypes, stepType => {
        _forEach(scenario[stepType], step => {
          if (!step.text || step.text.length < 1) {
            scenario.errorMessages.push("Error: Missing Given / When / Then Statement")
            isValid = false;
            return false;//break forEach loop
          }
        })
        if (scenario.errorMessages.indexOf('Error: Missing Given / When / Then Statement') > -1) return false;//break forEach loop
      })
    })

    return isValid;

  }

  // isStepValid(steps: Step[]) {
  //   for (const step of steps) {
  //     if (!step.text || step.text.length < 1) return false;
  //   }
  //   return true;
  // }
  // TODO add unit tests
  isVarsAndTableValid(): ExampleError {
    const scenarios: Scenario[] = this.featureFile.scenarios
    // TODO detect and return all errors for all scenarios instead of just returning
    // after first error
    for (let idx = 0; idx < scenarios.length; idx++) {
      if (scenarios[idx].keyVars && scenarios[idx].keyVars.length > 0) {
        if (!scenarios[idx].examples) {
          return {
            error: true,
            message: 'Error: Tags present but no values entered in Scenario Example Table',
            index: idx
          }
        }
        if (scenarios[idx].examples[0].length !== scenarios[idx].keyVars.length) {
          return {
            error: true,
            message: 'Error: Number of columns in Scenario Example Table does not match Tags from Given / When / Then statements',
            index: idx
          }
        }
        if (this.anyMissingValues(scenarios[idx].examples)) {
          return {
            error: true,
            message: 'Error: Missing values in Scenario Examples Table, every cell must contain text or a number',
            index: idx
          }
        }
      }
    }
    return { error: false };
  }

  // validates data tbale has valid values
  anyMissingValues(tableValues: (string | number)[][]) {
    for (let idx = 0; idx < tableValues.length; idx++) {
      for (let idxin = 0; idxin < tableValues[idx].length; idxin++) {
        const value: string | number = tableValues[idx][idxin]
        if ((!_isString(value) && !_isNumber(value)) || (_isString(value) && value.length === 0)) {
          return true;
        }
      }
    }
    return false;
  }

  // called from ScenarioComponent
  // compares varsArray of <tags> with Example Table column names
  // returns {updateArr,deleteArr}
  //    - updateArr contains <tags> that do not have a corresponding column name
  //    - deleteArr contains column name that do not have a corresponding <tag>
  checkUpdateNeeded(currentVars: string[], tableCols: string[]): VarUpdate {
    const varUpdate: VarUpdate = { updateArr: [], deleteArr: [] }
    const updateArr: boolean[] = []
    const deleteArr: string[] = []
    // get vars which are not in the table columns
    _forEach(currentVars, (v, idx) => {
      updateArr.push(tableCols.indexOf(v) < 0)
    })
    varUpdate.updateArr = updateArr
    // get table columns which are not in vars
    _forEach(tableCols, (v, idx) => {
      if (currentVars.indexOf(v) < 0) {
        deleteArr.push(v)
      }
    })
    varUpdate.deleteArr = deleteArr
    return varUpdate;
  }

  setStepStatements(stepStatments: StepStatements) {
    this.stepStatements = stepStatments;
    this.storage.set(STEPSTATEMENTS_STORAGE, this.stepStatements);
  }

  getStepStatements(): StepStatements {
    return this.stepStatements
  }

  setRemotePaths(remotePaths: string[]) {
    this.remotePaths = remotePaths;
    this.storage.set(REMOTEPATHS_STORAGE, this.remotePaths);
  }

  getRemotePaths(): string[] {
    return this.remotePaths
  }

  setRemoteRepo(remoteRepo: RemoteRepo | null) {
    this.remoteRepo = remoteRepo;
    if (remoteRepo) {
      this.storage.set(REMOTE_REPO, this.remoteRepo);
    } else {
      this.storage.remove(REMOTE_REPO)
    }

  }

  getRemoteRepo(): RemoteRepo {
    return this.remoteRepo
  }

  //fummy feature file used for testing
  loadDummyFeatureFile(): FeatureFile {
    return {
      "feature": "This is a feature",
      "scenarios": [
        {
          "scenario": "This is scenario Number 1",
          "given": [
            { text: "This is GIVEN statement", dataTable: [['id1', 'id2'], ['123', '345']] },
            { text: "This is another GIVEN <var_Given> statement", dataTable: null }
          ],
          "when": [
            { text: "This is WHEN statement", dataTable: null },
            { text: "This is another WHEN statement <var_When>", dataTable: null },
            { text: "This is When statement", dataTable: [['id1', 'id2'], ['123', '345']] }
          ],
          "then": [
            { text: "This is THEN <var_Then1> statement ", dataTable: null },
            { text: "This is another THEN statement <var_Then2>", dataTable: null },
            { text: "This is another \"THEN\" statement <var_Then3>", dataTable: [['id1', 'id2'], ['123', '345']] }
          ],
          "examples": [
            [
              "34",
              "asd",
              "df",
              "sdfsdfsd",
              "111"
            ],
            [
              "3",
              "weer",
              "er",
              "wer",
              "234"
            ],
            [
              "2",
              "werwe",
              "werw",
              "wer",
              "345"
            ],
            [
              "23",
              "rwerwer",
              "werwer",
              "werwer",
              "456"
            ]
          ],
          "keyVars": [
            "var_Given",
            "var_When",
            "var_Then1",
            "var_Then2",
            "var_Then3"
          ]
        },
        {
          "scenario": "This is scenario Number 2",
          "given": [
            { text: "This is GIVEN statement in scenario 2", dataTable: null },
          ],
          "when": [
            { text: "This is WHEN statement in scenario 2", dataTable: null },

          ],
          "then": [
            { text: "This is THEN statement in scenario 2", dataTable: null },
          ],
          "keyVars": [],
          "examples": null
        }
      ],
      "featureComment": "This is a feature comment",
      "showErrors": false,
      "pathName": null,
      "fileName": null
    }
  }

}
