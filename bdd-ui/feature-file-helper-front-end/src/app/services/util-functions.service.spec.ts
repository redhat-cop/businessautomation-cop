import { TestBed } from '@angular/core/testing';

import { UtilFunctionsService } from './util-functions.service';

describe('UtilFunctionsService', () => {
  let utils = new UtilFunctionsService()

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UtilFunctionsService],
    });
    utils = TestBed.get(UtilFunctionsService)
  });

  it('should create the FeatureJSONParserService service', () => {
    expect(utils).toBeTruthy();
  });

  it('#findInsertIndex should get insert posiiton', () => {
    const inputArray = [
      {
        value: 'aaa',
        update: false
      },
      {
        value: 'bbb',
        update: false
      },
      {
        value: 'ccc',
        update: false
      },
      {
        value: 'ddd',
        update: true
      }
    ]
    const insertNum = utils.findInsertIndex(inputArray, 3)
    expect(insertNum).toEqual(2)
  });

  it('#extractVars should extract <test> from input string \'This is a <test> string\'', () => {
    const inputString = 'This is a <test> string'
    const outputStringArray = utils.extractVars(inputString, [])
    expect(outputStringArray.length).toBe(1)
    expect(outputStringArray[0]).toBe('test')
  });

  it('#extractVars should extract <test1><test2> from input string \'This is a <><test1><test2> string\'', () => {
    const inputString = 'This is a <><test1><test2>'
    const outputStringArray = utils.extractVars(inputString, ['test3'])
    expect(outputStringArray.length).toBe(3)
    expect(outputStringArray[0]).toBe('test3')
    expect(outputStringArray[1]).toBe('test1')
    expect(outputStringArray[2]).toBe('test2')
  });

  it('#extractVars should extract <test1><test2> from input string \'This is a <>>><<te - ?? s*t1>>< t!  €est2> string\'', () => {
    const inputString = 'This is a <>>><<te - ?? s*t1>>< t  € est2> string'
    const outputStringArray = utils.extractVars(inputString, ['test3'])
    expect(outputStringArray.length).toBe(3)
    expect(outputStringArray[0]).toBe('test3')
    expect(outputStringArray[1]).toBe('test1')
    expect(outputStringArray[2]).toBe('test2')
  });

  it('#isNoEmptyCells should return true if no empty cells in input data table', () => {
    let inputArr = [
      ['1', '2', '0'],
      ['1', '2', '0']
    ]
    expect(utils.isNoEmptyCells(inputArr)).toBeTruthy('test 1');
    inputArr = [
      ['null', '2', '0'],
      ['1', null, '0']
    ]
    expect(utils.isNoEmptyCells(inputArr)).toBeFalsy('test 2');
    // isNoEmptyCells can be chnaged to prevent '', see comments in UtilFunctionsService
    inputArr = [
      ['w', '', ''],
      ['1', '2', '0']
    ]
    expect(utils.isNoEmptyCells(inputArr)).toBeTruthy('test 3');


  });



});
