import { TestBed } from '@angular/core/testing';

import { FeatureFileParserService } from './featurefile-parser.service';
import { UtilFunctionsService } from './util-functions.service';
import _filter from 'lodash/filter';

import { FEATUREFILE1, FEATUREFILE2, FEATUREFILE3, FEATUREFILE_FAIL1, FEATUREFILE_FAIL2, FEATUREFILE_FAIL3 } from '../models/featureFiles/test.featureFile'

describe('FeatureFileParserService', () => {

  let parser: FeatureFileParserService;

  // beforeEach(() => {
  //   parser = new FeatureFileParserService(new UtilFunctionsService());
  // });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FeatureFileParserService, UtilFunctionsService],
    });
    parser = TestBed.get(FeatureFileParserService)
  });

  it('should create the FeatureFileParserService service', () => {
    expect(parser).toBeTruthy();
  });


  it('#findNextScenario should find next Scenario: or Scenario Outline statement', () => {
    const testInput = `Feature: feature
  Scenario: Scenario 1
    Given alskdjalkdj
  Scenario: Scenario 2

  Scenario Outline: Scenario 2
    Given asdfsadfsdf
  Scenario false
  Scenario:
  `
    let lineArr: string[] = testInput.split('\n')
    //remove blank lines
    lineArr = _filter(lineArr, (line) => line !== "");
    expect(parser.findNextScenario(lineArr, 0)).toBe(1, 'should be line 1')
    expect(parser.findNextScenario(lineArr, 1)).toBe(1, 'should be line 1')
    expect(parser.findNextScenario(lineArr, 2)).toBe(3, 'should be line 3')
    expect(parser.findNextScenario(lineArr, 4)).toBe(4, 'should be line 4')
    expect(parser.findNextScenario(lineArr, 5)).toBe(7, 'should be line 7')
  });


  it('#extractSubstring should remove <removeString> from <inputString>', () => {
    let inputString = '       Given   This is a Test   '
    let removeString = 'Given'
    expect(parser.extractSubstring(inputString, removeString)).toEqual('This is a Test')
    inputString = 'Given This is a Test'
    expect(parser.extractSubstring(inputString, removeString)).toEqual('This is a Test')
    inputString = '       Given   This is a Test   '
    removeString = 'Givghjgjen'
    expect(parser.extractSubstring(inputString, removeString)).toEqual(null)
  });

  it('#findLineByString should search input string array and find linenumber of line containing input string', () => {
    const testInput = `Feature: feature
  Scenario: Scenario 1
    Given alskdjalkdj
  Scenario: Scenario 2

  Scenario Outline: Scenario 2
    Given testString
  Scenario false
  Scenario:
  `
    let lineArr: string[] = testInput.split('\n')
    expect(parser.findLineByString(lineArr, 'Given', 0)).toBe(2, 'start: 0')
    expect(parser.findLineByString(lineArr, 'Given', 3, 6)).toBe(6, 'start: 3 end: 6')
    expect(parser.findLineByString(lineArr, 'Given', 7)).toBe(null, 'start: 7')
    expect(parser.findLineByString(lineArr, 'Given', -2)).toBe(null, 'start: -2')
    expect(parser.findLineByString(lineArr, 'Scenario:', 7)).toBe(8, 'start: 7')
    expect(parser.findLineByString(lineArr, 'Feature:', 0)).toBe(0, 'start: 0')
    expect(parser.findLineByString(lineArr, 'Feature:', 0, 0)).toBe(0, 'start: 0 end:0')
    expect(parser.findLineByString(lineArr, 'Feature:', 0, -7)).toBe(null, 'start: 0 end -7')
  });

  it('#extractComment should extract array of strings from | <string> | <string> |', () => {
    let testString = [
      'Scenario: test',
      'This is a Comment',
      'Given test'
    ]
    expect(parser.extractComment(testString, 1, 2)).toEqual('This is a Comment')
     testString = [
      'Scenario: test',
      'This is a Comment',
      'On 2 lines',
      'Given test'
    ]
    expect(parser.extractComment(testString, 1, 3)).toEqual('This is a Comment On 2 lines ')
  });

  it('#extractDataTable should extract array of strings from | <string> | <string> |', () => {
    const testString = '      | Bill     |password123|        manager     |  c|     '
    expect(parser.extractDataTable(testString)).toEqual(['Bill', 'password123', 'manager', 'c'])
  });

  it('#featureFileToLinesArray should econvert feature file to array of ines', () => {
    const testFile = `line 1
    line 2
        # line 3
        
        line 5
        
        line 7
      #line 8
      
    `
    expect(parser.featureFileToLinesArray(testFile).length).toBe(4, 'should be 4 lines')

  });

  it('#extractScenario should extract scenario from feature file text format', () => {
    let scenarioTitle = 'Supervisor should be able to access all Accounts up to Level 1'
    let output = parser.extractScenario(FEATUREFILE1, scenarioTitle)
    let lineArr: string[] = output.split('\n')
    expect(lineArr.length).toBe(16, 'number of lines in scenario')
    expect(lineArr[14]).toEqual('      | 44444         | Account Not Found |', 'last line in scenario')
  });

  it('#convertScenarioToJSON should convert Scenario text to JSON structure', () => {

    let lineArr: string[] = FEATUREFILE1.split('\n')
    lineArr = _filter(lineArr, (line) => line !== "");
    let firstScenario = parser.findNextScenario(lineArr, 0)
    expect(firstScenario).toBeDefined('find first sceanrio')
    expect(firstScenario).toBeGreaterThanOrEqual(0, 'find first scenario line number')
    let scenarios = parser.convertScenarioToJSON(firstScenario, lineArr);
    expect(scenarios.length).toBe(3, 'number of scenarios')
    expect(scenarios[0].scenario).toBe('Manager should be able to access all Accounts up to Level 2', 'scenario 1 title')
    expect(scenarios[1].scenario).toBe('Supervisor should be able to access all Accounts up to Level 1', 'scenario 2 title')
    expect(scenarios[2].scenario).toBe('Normal user should be able to access only Accounts at Level 0', 'scenario 3 title')
    expect(scenarios[0].tag).toBe('@scenarioTag11', ' scenarioTag11')

    lineArr = FEATUREFILE2.split('\n')
    lineArr = _filter(lineArr, (line) => line !== "");
    firstScenario = parser.findNextScenario(lineArr, 0)
    expect(scenarios.length).toBe(3, 'number of scenarios')
    expect(scenarios[0].scenario).toBe('Manager should be able to access all Accounts up to Level 2', 'scenario 1 title')
    expect(scenarios[1].scenario).toBe('Supervisor should be able to access all Accounts up to Level 1', 'scenario 2 title')
    expect(scenarios[2].scenario).toBe('Normal user should be able to access only Accounts at Level 0', 'scenario 3 title')

    const testScenario = scenarios[1];
    expect(testScenario.scenarioComment).toBe('This is a Scenario comment on 2 lines ')
    expect(testScenario.given.length).toBe(1, 'given steps length')
    expect(testScenario.given[0].text).toBe('I am a supervisor', 'given step text')
    expect(testScenario.given[0].dataTable).toBe(null, 'given datatable')
    expect(testScenario.when.length).toBe(2, 'when steps length')
    expect(testScenario.when[0].text).toBe('I login with my credentials', 'when step text')
    expect(testScenario.when[0].dataTable).toEqual([['Username', 'Password'], ['Betty', 'password123']], 'when datatable')
    expect(testScenario.then.length).toBe(1, 'then steps length')
    expect(testScenario.then[0].text).toBe('I should get <balance> as the response', 'then step text')
    expect(testScenario.then[0].dataTable).toBe(null, 'then datatable')
    expect(testScenario.examples).toEqual([['accountnumber', 'balance'], ['11111', 'Not Authorised'], ['22222', '999'], ['33333', '9999'], ['44444', 'Account Not Found']], 'Examples Table')

    lineArr = FEATUREFILE2.split('\n')
    lineArr = _filter(lineArr, (line) => line !== "");
    firstScenario = parser.findNextScenario(lineArr, 0)
    expect(firstScenario).toBeDefined('find first scenario')
    expect(firstScenario).toBeGreaterThanOrEqual(0, 'find first scenario line number')
    scenarios = parser.convertScenarioToJSON(firstScenario, lineArr);
    expect(scenarios.length).toBe(2, 'number of scenarios FEATUREFILE2')
    expect(scenarios[0].scenario).toBe('Normal user should be able to access only Accounts at Level 0', 'scenario 1 title FEATUREFILE2')
    expect(scenarios[1].scenario).toBe('Manager should be able to access all Accounts up to Level 2', 'scenario 2 title FEATUREFILE2')
    expect(scenarios[1].examples).toEqual([['accountnumber', 'balance'], ['11111', '9'], ['22222', '99'], ['33333', '999'], ['44444', 'Account Not Found']], 'Examples Table FEATUREFILE2')

    lineArr = FEATUREFILE3.split('\n')
    lineArr = _filter(lineArr, (line) => line !== "");
    firstScenario = parser.findNextScenario(lineArr, 0)
    expect(firstScenario).toBeDefined('find first sceanrio')
    expect(firstScenario).toBeGreaterThanOrEqual(0, 'find first scenario line number')
    scenarios = parser.convertScenarioToJSON(firstScenario, lineArr);
    expect(scenarios.length).toBe(3, 'number of scenarios FEATUREFILE3')
    expect(scenarios[0].scenario).toBe('Normal user should be able to access only Accounts at Level 0', 'scenario 1 title FEATUREFILE3')
    expect(scenarios[1].scenario).toBe('Manager should be able to access all Accounts up to Level 2', 'scenario 2 title FEATUREFILE3')
    expect(scenarios[2].scenario).toBe('Supervisor should be able to access all Accounts up to Level 1', 'scenario 2 title FEATUREFILE3')

  })

  it('#convertFeatureFiletoJSON should convert Featurefile text to JSON structure', () => {

    let featureJSON = parser.convertFeatureFiletoJSON(FEATUREFILE1, 'fileName.feature', 'src/test/path')
    expect(featureJSON).toBeDefined()
    expect(featureJSON.ok).toBeTruthy()

    let featureFile = featureJSON.featureFile
    expect(featureFile.fileName).toBe('fileName.feature')
    expect(featureFile.pathName).toBe('src/test/path')
    expect(featureFile.feature).toBe('Retrieve Account Balance')
    expect(featureFile.featureComment).toBe('This is a Feature comment')
    expect(featureFile.featureTag).toBe('@featureTag1', 'tag 1')

    let background = featureFile.background
    expect(background.length).toBe(2)
    expect(background[1].text).toBe('these accounts exist')
    expect(background[0].text).toBe('The following users are valid')
    expect(background[0].dataTable.length).toBe(4, 'background dataTable rows')
    expect(background[0].dataTable[0].length).toBe(3, 'background dataTable columns')
    expect(background[0].dataTable[0][0]).toBe('Username', 'background dataTable column Name 0')
    expect(background[0].dataTable[0][1]).toBe('Password', 'background dataTable column Name 0')
    expect(background[0].dataTable[0][2]).toBe('UserType', 'background dataTable column Name 0')
    expect(background[0].dataTable[3][0]).toBe('Bob', 'background dataTable row 4 value 0')
    expect(background[0].dataTable[3][1]).toBe('password123', 'background dataTable row 4 value 1')
    expect(background[0].dataTable[3][2]).toBe('user', 'background dataTable row 4 value 2')

    featureJSON = parser.convertFeatureFiletoJSON(FEATUREFILE3, 'fileName.feature', 'src/test/path')
    featureFile = featureJSON.featureFile
    expect(featureFile.feature).toBe('Retrieve Another Account Balance')
    expect(featureFile.featureComment).toBe('This is a Feature Comment on 2 lines ', 'feature comment 2')
    expect(featureFile.featureTag).toBe('@featureTag3', 'feature tag 2')
    //test fail no scenario
    featureJSON = parser.convertFeatureFiletoJSON(FEATUREFILE_FAIL1, 'fileName.feature', 'src/test/path')
    expect(featureJSON.ok).toBeFalsy()
    expect(featureJSON.error).toBeDefined()
    expect(featureJSON.error.message).toBe('Unable to find Scenario statement in Feature File', 'fail with no scenario')

    featureJSON = parser.convertFeatureFiletoJSON(FEATUREFILE_FAIL2, 'fileName.feature', 'src/test/path')
    expect(featureJSON.ok).toBeFalsy()
    expect(featureJSON.error).toBeDefined()
    expect(featureJSON.error.message).toBe('Unable to read Given statement in Feature File Scenario #1', 'fail with no given step')

    featureJSON = parser.convertFeatureFiletoJSON(FEATUREFILE_FAIL3, 'fileName.feature', 'src/test/path')
    expect(featureJSON.ok).toBeFalsy()
    expect(featureJSON.error).toBeDefined()
    expect(featureJSON.error.message).toBe('Unable to read Then Scenario #1', 'fail with then step')

  });

});
