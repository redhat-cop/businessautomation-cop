import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CONSTANTS } from '../constants'


@Injectable({
  providedIn: 'root'
})
export class RemoteCallsService {

  constructor(
    private http: HttpClient
  ) { }


    // downloads details from remote repo
  // lists of remote feature file, scenarios, steps, data tables
  listRemoteRepo() {
    const url = `${CONSTANTS.BACKEND_HOST}/remoteRepo/listRepos`
    return this.http.get(url);
  }


  // downloads details from remote repo
  // lists of remote feature file, scenarios, steps, data tables
  importRemoteRepo(repoId:string) {
    const url = `${CONSTANTS.BACKEND_HOST}/remoteRepo/importRemoteRepo?repoId=${repoId}`
    return this.http.get(url);
  }

  // downloads contents of specific feature file
  getFeatureFile(fileName:string, pathName:string, repoId:string) {
    const url = `${CONSTANTS.BACKEND_HOST}/remoteRepo/getFeatureFile?fileName=${fileName}&pathName=${pathName}&repoId=${repoId}`
    return this.http.get(url, {responseType: 'text'});
  }

  // uploads feature file
  saveFeatureFile(fileName: string, pathName: string, repoId:string, featureFile: string, isNew: boolean) {
    const url = `${CONSTANTS.BACKEND_HOST}/remoteRepo/saveFeatureFile`
    const body = {
      fileName,
      pathName,
      repoId,
      contents: featureFile,
      isNew
    }
    return this.http.post(url, body);
  }

  // pings endpoint to delete local copy of remote repo in backend 
  // this forces a new git pull nest time remote is accessed
  // for use if there are errors caused by git merge conflicts
  forceResetRemoteRepo(repoId:string) {
    const url = `${CONSTANTS.BACKEND_HOST}/remoteRepo/forceGitReset?repoId=${repoId}`
    const body = {reset:true}
    return this.http.post(url, body);
  }

}