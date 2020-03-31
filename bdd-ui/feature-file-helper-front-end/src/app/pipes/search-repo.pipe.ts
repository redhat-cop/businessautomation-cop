import { Pipe, PipeTransform } from '@angular/core';
import { RemoteRepo } from '../models/models'
import _filter from 'lodash/filter'

@Pipe({
  name: 'searchRepo'
})
export class SearchRepoPipe implements PipeTransform {

  transform(repoData: RemoteRepo[], searchText?: string): any {
    if(!searchText){
      return repoData;
    }
    return _filter(repoData, (item:RemoteRepo)=>{
      return item.name.includes(searchText) 
    });
  }
}
