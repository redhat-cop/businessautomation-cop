import { Pipe, PipeTransform } from '@angular/core';
import { RemoteDataObj } from '../models/models'
import _filter from 'lodash/filter'

@Pipe({
  name: 'searchRemote'
})
export class SearchRemotePipe implements PipeTransform {

  transform(remoteData: RemoteDataObj[], searchText?: string): any {
    if(!searchText){
      return remoteData;
    }
    return _filter(remoteData, (item:RemoteDataObj)=>{
      return item.fileName.includes(searchText) || (item.scenarioName && item.scenarioName.includes(searchText))
    });
  }
}
