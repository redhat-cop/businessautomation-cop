import { TestBed, inject } from '@angular/core/testing';

import { FeatureStoreService } from './feature-store.service';
import { FEATUREFILE_JSON } from '../models/featureFileJson/test.featureJSON'
import { StorageServiceModule } from 'angular-webstorage-service';
import { Scenario } from '../models/models';

describe('FeatureStoreService', () => {
  let storageService: FeatureStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FeatureStoreService],
      imports: [StorageServiceModule]
    });
    storageService = TestBed.get(FeatureStoreService)
  });

  it('should create the storage service', () => {
    expect(storageService).toBeTruthy();
  });

  it('#checkUpdateNeeded should detect differences between <tags> array and Example Table column names', () => {
    let varArray = ['111', '222', '333', '444']
    let colNames = ['111', '222', '333', '444']
    expect(storageService.checkUpdateNeeded(varArray, colNames).updateArr).toEqual([false, false, false, false], 'no varArray updates')
    expect(storageService.checkUpdateNeeded(varArray, colNames).deleteArr).toEqual([], 'no colName updates')
    varArray = ['111', '222', '333', '444']
    colNames = ['666', '111', '333', '444', '555']
    expect(storageService.checkUpdateNeeded(varArray, colNames).updateArr).toEqual([false, true, false, false], 'updates required 1')
    expect(storageService.checkUpdateNeeded(varArray, colNames).deleteArr).toEqual(['666', '555'], 'colName updates required 1')
    varArray = []
    colNames = ['111', '222']
    expect(storageService.checkUpdateNeeded(varArray, colNames).updateArr).toEqual([], 'no varArray updates 2')
    expect(storageService.checkUpdateNeeded(varArray, colNames).deleteArr).toEqual(['111', '222'], 'colName updates required 2')
  });

  it('#checkIfValid should detect validation errors in scenarios', () => {
    let scenarios: Scenario[] = Object.assign({}, FEATUREFILE_JSON.scenarios)
    expect(storageService.checkIfValid(scenarios)).toBeTruthy('valid scenarios')
    let scenario: Scenario = scenarios[0]
    let saveText = scenario.given[0].text;
    scenario.given[0].text = ''
    expect(storageService.checkIfValid(scenarios)).toBeFalsy('missing given text')
    expect(scenario.errorMessages).toEqual(['Error: Missing Given / When / Then Statement'], 'missing given text')
    scenario.given[0].text = saveText;
    saveText = scenario.scenario
    scenario.scenario = ''
    expect(storageService.checkIfValid(scenarios)).toBeFalsy('missing Scenario statement')
    expect(scenario.errorMessages).toEqual(['Error: Missing Scenario statement'], 'missing Scenario statement')
    scenario.scenario = saveText
    saveText = scenario.keyVars.splice(0, 1)[0];
    expect(storageService.checkIfValid(scenarios)).toBeFalsy('varsArray mismatch')
    expect(scenario.errorMessages).toEqual(['Error: Number of columns in Scenario Examples Table does not match Tags from Given / When / Then'])
    scenario.keyVars.splice(0, 0, saveText)
  });

  it('#anyMissingValues should detect missing values in data table', () => {
    let arr:(string | number)[][] = [
      ['qqq','wer',3],
      ['www', '2',1],
      [2,2,2]
    ]
    expect(storageService.anyMissingValues(arr)).toBeFalsy('no missing values')
    arr = [
      ['qqq',null,3],
      ['www', '2',1],
      [2,2,2]
    ]
    expect(storageService.anyMissingValues(arr)).toBeTruthy('includes null')
    arr = [
      ['qqq',4,'g'],
      ['', '2',1],
      [2,2,2]
    ]
    expect(storageService.anyMissingValues(arr)).toBeTruthy('includes blank')
    arr = [
      ['qqq',4,'g'],
      ['', '2',1],
      [2,,2]
    ]
    expect(storageService.anyMissingValues(arr)).toBeTruthy('includes undefined')
  });


});
