import { Injectable } from '@angular/core';
import { Var, Step } from '../models/models'
import _findIndex from 'lodash/findIndex'
import _isString from 'lodash/isString'

@Injectable({
  providedIn: 'root'
})
export class UtilFunctionsService {

  constructor() { }

  // Recursive function to extract <???> from input string
  // @str: string to be parsed for <???>
  // @varArray: array of <???> already found, should be [] for intial function call
  extractVars(str: string, varArray: string[]): string[] {
    // work through input string and find <???>
    // only allow letters and numbers, no spaces or characters
    str = str.replace(/[^a-zA-Z<>0-9_"]+/g, '');

    // <?> is shortest allowable string, quit if shorter 
    if (str.length < 3) {
      return varArray
    }
    // find first <
    const start = str.indexOf('<')
    if (start > -1) {
      const strTmp = str.substring(start + 1)
      //find next >
      const end = strTmp.indexOf('>')

      //quit if no >
      if (end < 0) {
        return varArray
      }

      //check for  two <'s before next >, disregard the first one
      const next = strTmp.indexOf('<')
      if (next > -1 && next < end) {
        //found 2 <, start again after first <
        this.extractVars(strTmp.substring(next), varArray)
        return varArray
      }

      if (end > 0) {
        //boom, got one
        varArray.push(strTmp.substring(0, end))
      }
      // ...and recurse
      this.extractVars(strTmp.substring(end + 1), varArray)
    }
    //quit if no < found
    return varArray
  }

  // find place in tableColumns to insert new columns
  // should be to right of nearest valid existing column
  // @param: varsArray<{value: string, update: boolean} > update=false means it's valid
  // @param: idx - selected position of value to insert
  findInsertIndex(varsArray: Var[], idx: number): number {
    for (let index = idx - 1; index >= 0; index--) {
      if (!varsArray[index].update) {
        return index;
      }
    }
    return -1;  //not found
  }

  // find blank values in 2D array
  isNoEmptyCells(cellsValues: string[][]): boolean {
    for (let idx = 0; idx < cellsValues.length; idx++) {
      if (_findIndex(cellsValues[idx], (v) => !_isString(v)) > -1) { // doesn't reject ''
      // if (_findIndex(cellsValues[idx], (v) => !_isString(v) || (v === '')) > -1) { // also rejects ''
        return false;
      }
    }
    return true;
  }

}
