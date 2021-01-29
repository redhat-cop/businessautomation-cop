import { TestBed } from '@angular/core/testing';

import { FeatureJSONParserService } from './featureJSON-parser.service';
import { UtilFunctionsService } from './util-functions.service';
import _filter from 'lodash/filter';

import { FEATUREFILE_JSON } from '../models/featureFileJson/test.featureJSON'
import { ScenarioOutput, Scenario, FeatureFile } from '../models/models';

describe('FeatureJSONParserService', () => {
  let parser: FeatureJSONParserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FeatureJSONParserService, UtilFunctionsService],
    });
    parser = TestBed.get(FeatureJSONParserService)
  });

  it('should create the FeatureJSONParserService service', () => {
    expect(parser).toBeTruthy();
  });

  it('#getSpacers should return left and right spacers for input column width and input string', () => {
    let colWidth = 11;
    let text = 'test';
    expect(parser.getSpacers(colWidth, text).spacerLeft.length).toBe(1, 'left space 1')
    expect(parser.getSpacers(colWidth, text).spacerRight.length).toBe(6, 'right space 6')
    colWidth = 5;
    text = 'test';
    expect(parser.getSpacers(colWidth, text).spacerLeft.length).toBe(1, 'left space 1')
    expect(parser.getSpacers(colWidth, text).spacerRight.length).toBe(0, 'right space 0')
    colWidth = 3;
    text = 'test';
    expect(parser.getSpacers(colWidth, text).spacerLeft.length).toBe(1, 'left space 1')
    expect(parser.getSpacers(colWidth, text).spacerRight.length).toBe(1, 'right space 1')
    colWidth = 3;
    text = null;
    expect(parser.getSpacers(colWidth, text).spacerLeft.length).toBe(1, 'left space 1')
    expect(parser.getSpacers(colWidth, text).spacerRight.length).toBe(2, 'right space 2')
  });

  it('#getColumnWidths should return array of column widths', () => {
    const vars = ['col1', 'col 2', '3', 'column 4']
    const dataTable = [
      ['1234', '12', '123456789', 'null'],
      ['123456', '123', '', null],
      ['1234', '', null, '12']
    ]
    expect(parser.getColumnWidths(dataTable, vars)).toEqual([8, 7, 11, 10])
  });

  it('#getColumnWidthsDataTable should return array of column widths', () => {
    const dataTable = [
      ['1234', '12', '123456789', 'null', '', null],
      ['123456', '123', '', null, '', null],
      ['1234', '', null, '12', '', null]
    ]
    expect(parser.getColumnWidthsDataTable(dataTable)).toEqual([8, 5, 11, 6, 2, 2])
  });

  it('#replaceNulls should remove rows of null and replace nulls with \"\" in input 2D table ', () => {
    const table = [
      [null, null, null, null],
      ['1234', '12', '123456789', 'null'],
      ['123456', '123', '', null],
      [null, null, null, null],
      ['1234', '', null, '12'],
      [null, null, null, null],
      [null, null, null, null],
    ]
    expect(parser.replaceNulls(table)).toEqual([
      ['1234', '12', '123456789', 'null'],
      ['123456', '123', '', ''],
      ['1234', '', '', '12'],
    ])
  });

  it('#extractScenariosText should extract scenarios from json feature file', () => {
    const scenariosOutput: ScenarioOutput[] = parser.extractScenariosText(FEATUREFILE_JSON)
    expect(scenariosOutput.length).toBe(3, 'number of scenarios')
    expect(scenariosOutput[0].title).toBe('Scenario Outline: Manager should be able to access all Accounts up to Level 2', 'scenario 1 title')
    expect(scenariosOutput[0].given).toBe('Given I am a manager', 'scenario 1 given')
    expect(scenariosOutput[0].when.includes('| Bill     | password123 |')).toBeTruthy('scenario 1 whenn')
    expect(scenariosOutput[0].when.includes('When I login with my credentials')).toBeTruthy('scenario 1 when')
    expect(scenariosOutput[0].when.includes('And I request balance for <accountnumber> from the accounts API')).toBeTruthy('scenario 1 when')
    expect(scenariosOutput[0].then).toBe('Then I should get <balance> as the response', 'scenario 1 then')
    expect(scenariosOutput[1].title).toBe('Scenario: Supervisor should be able to access all Accounts up to Level 1', 'scenario 2 title')
    expect(scenariosOutput[1].given).toBe('Given I am a supervisor', 'scenario 2 given')
    expect(scenariosOutput[1].when.includes('| Betty    | password177 |')).toBeTruthy('scenario 2 whenn')
    expect(scenariosOutput[1].when.includes('When I login with my credentials')).toBeTruthy('scenario 2 when')
    expect(scenariosOutput[1].then).toBe('Then I should get \"99\" as the response', 'scenario 2 then')
    expect(scenariosOutput[2].title).toBe('Scenario Outline: Normal user should be able to access only Accounts at Level 0', 'scenario 3 title')
    expect(scenariosOutput[2].given).toBe('Given I am a normal user', 'scenario 3 given')
    expect(scenariosOutput[2].when.includes('| Tim      | password999 |')).toBeTruthy('scenario 3 when')
    expect(scenariosOutput[2].when.includes('When I login with my credentials')).toBeTruthy('scenario 3 when')
    expect(scenariosOutput[2].then).toBe('Then I should get <balance> as the response', 'scenario 3 then')
  });

  it('#extractExampleTableText should extract Example Tables from json feature file', () => {
    const scenarios: Scenario[] = FEATUREFILE_JSON.scenarios
    const exampleObj = parser.extractExampleTableText(scenarios[0])
    expect(exampleObj.exampleTitle.includes('| accountnumber | balance           |')).toBeTruthy('example table title')
    expect(exampleObj.examples.includes('| 22222         | 999               |')).toBeTruthy('example table values 1')
    expect(exampleObj.examples.includes('| 44444         | Account Not Found |')).toBeTruthy('example table values 2')
  });

  it('#extractBackgroundText should extract Background from json feature file', () => {
    const background = parser.extractBackgroundText(FEATUREFILE_JSON)
    expect(background.includes('Given The following users are valid')).toBeTruthy('background values 1')
    expect(background.includes('And these accounts exist')).toBeTruthy('background values 2')
    expect(background.includes('| Bob      | password123 | user       |')).toBeTruthy('background values 3')
    expect(background.includes('| 33333      | 0            | 9999    |')).toBeTruthy('background values 4')

  });

  it('#parseTag should ensure tags start with @', () => {
    let str = 'test string @ test'
    expect(parser.parseTag(str)).toEqual('@' + str, 'add @ to tag string')
    str = '@test string @ test'
    expect(parser.parseTag(str)).toEqual(str, 'should not add @ to string')
  });

  it('#convertJSONtoFeature should create feature file text from json feature file', () => {
    let featureJson = parser.convertJSONtoFeature(FEATUREFILE_JSON)
    expect(featureJson).toMatch('Feature: Retrieve Account Balance', "match Feature title")
    expect(featureJson).toMatch('Background:', "match background")
    expect(featureJson).toMatch('This is a feature comment', "match feature comment")
    expect(featureJson.match(/Scenario Outline:/g).length).toBe(2, "match 2 Scenario Outlines")
    expect(featureJson.match(/Scenario:/g).length).toBe(1, "match 1 Scenario")
    expect(featureJson).toMatch('This is a Scenario Comment', "match Scenario comment")
    expect(featureJson.match(/Examples:/g).length).toBe(2, "match 2 Example Tables")
    expect(featureJson.match(/Given/g).length).toBe(4, "match 4 Given Steps")
    expect(featureJson.match(/When/g).length).toBe(3, "match 2 When Steps")
    expect(featureJson.match(/Then/g).length).toBe(3, "match 3 Then Steps")
    expect(featureJson.match(/@/g).length).toBe(2, "match @ in tags")
    expect(featureJson).toMatch('@tag1 @feature1', 'match tag text')

    //no background
    const featureFile: FeatureFile = FEATUREFILE_JSON
    featureFile.background = []
    featureJson = parser.convertJSONtoFeature(featureFile)
    expect(featureJson).toMatch('Feature: Retrieve Account Balance', "match Feature title no background")
    expect(featureJson).not.toMatch('Background:', "match background no background")
    expect(featureJson.match(/Scenario Outline:/g).length).toBe(2, "match 2 Scenario Outlines no background")
    expect(featureJson.match(/Scenario:/g).length).toBe(1, "match 1 Scenario no background")
    expect(featureJson.match(/Examples:/g).length).toBe(2, "match 2 Example Tables no background")
    expect(featureJson.match(/Given/g).length).toBe(3, "match 4 Given Steps no background")
    expect(featureJson.match(/When/g).length).toBe(3, "match 2 When Steps no background")
    expect(featureJson.match(/Then/g).length).toBe(3, "match 3 Then Steps no background")
  });

});
