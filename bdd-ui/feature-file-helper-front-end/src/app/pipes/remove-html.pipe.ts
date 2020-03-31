import { Pipe, PipeTransform } from '@angular/core';
import { validateSuffixes } from '@angular/flex-layout/core/typings/breakpoints/breakpoint-tools';

@Pipe({
  name: 'removeHTML'
})
export class RemoveHTMLPipe implements PipeTransform {

  transform(value: any, args?: any): any {

    //return text between <>b tags
    const left = value.indexOf('<b>')
    const right = value.indexOf('</b>')
    if(left>-1 && right > -1){
      return value.substring(left+3, right)
    }
    return value;
  }

}
