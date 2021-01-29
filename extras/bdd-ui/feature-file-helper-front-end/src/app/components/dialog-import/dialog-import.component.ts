import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RemoteCallsService } from '../../services/remote-calls.service'
import { FeatureFileParserService } from '../../services/featurefile-parser.service'
import { RemoteDataObj, RemoteFeatureFileCallObject, StepStatements, RemoteRepo, FeatureFile } from '../../models/models'
import { FeatureStoreService } from '../../services/feature-store.service'
import _findIndex from 'lodash/findIndex'
import _uniq from 'lodash/uniq';

@Component({
  selector: 'app-dialog-import',
  templateUrl: './dialog-import.component.html',
  styleUrls: ['./dialog-import.component.css']
})
export class DialogImportComponent implements OnInit {
  private remoteData: RemoteDataObj[];
  private stepStatements: StepStatements;
  // private remoteScenarios: RemoteDataObj[];
  private remoteRepoList: RemoteRepo[];
  private errMsg: string;
  private showSpinner: boolean = false;
  private showDownloadSpinner = [];
  private remotePaths: string[];
  private remoteRepo: RemoteRepo;
  open: boolean[];

  constructor(
    private remoteCalls: RemoteCallsService,
    public dialogRef: MatDialogRef<DialogImportComponent>,
    private featureStore: FeatureStoreService,
    private parserService: FeatureFileParserService,
    @Inject(MAT_DIALOG_DATA)
    public data: any
  ) { }

  ngOnInit() {
    this.remoteRepo = this.featureStore.getRemoteRepo();
    this.getRemoteRepos()
  }

  onRepoSelected() {
    this.importRemoteRepo(this.remoteRepo.id)
  }

  // make remote http call to retrieve remote repo data
  getRemoteRepos() {
    this.errMsg = '';
    this.remoteCalls.listRemoteRepo()
      .subscribe(
        (data: RemoteRepo[]) => {
          this.remoteRepoList = data;

          if (this.remoteRepoList.length === 1) {
            // only one repo go ahead and make the import call
            // no need for user to choose repo
            this.remoteRepo = this.remoteRepoList[0]
            return this.importRemoteRepo(this.remoteRepoList[0].id)
          }

          // if called from Remote- Import Remote stop here
          // and wait for repo selection
          if (this.data.isImportRemote) return


          // if there is current repo loaded, try and find it and import
          if (this.remoteRepo && this.remoteRepo.id) {
            const repoNum = _findIndex(this.remoteRepoList, this.remoteRepo);
            if (repoNum > -1) {
              this.remoteRepo = this.remoteRepoList[repoNum]
              this.importRemoteRepo(this.remoteRepo.id)
            }
          }
        },
        err => {
          this.remoteRepoList = [];
          console.error('Error getting listof remote Repos ', err)
          this.errMsg = 'Error retrieving list of remote Repos';
        }
      );
  }

  // make remote http call to retrieve remote repo data
  importRemoteRepo(repoId: string) {
    this.errMsg = '';
    this.remoteData = [];
    this.showSpinner = true;
    this.remoteCalls.importRemoteRepo(repoId)
      .subscribe(
        (data: RemoteFeatureFileCallObject) => {


          this.stepStatements = {
            givenStatements: data.givenSteps,
            whenStatements: data.whenSteps,
            thenStatements: data.thenSteps,
          }
          // add path names
          this.remotePaths = _uniq(data.featureFiles.map((ff) => ff.pathName))

          // handle Remote- Import Remote option - straight import, close dialog and retutn data
          if (this.data.isImportRemote) {
            const noSave = -1;
            setTimeout(() => {
              //timeout to show spinner before closing so user knows something happened
              this.showSpinner = false;
              return this.doImport(noSave)
            }, 500)

          } else {
            this.showSpinner = false;
            // initialise download spinners
            this.showDownloadSpinner = Array(this.remoteData.length).fill(false);
            // handle scerios vs files
            if (this.data.isScenarios) {
              this.remoteData = data.scenarios;
            } else {
              this.remoteData = data.featureFiles;
            }
            this.open = Array(this.remoteData.length).fill(false)
          }
        },
        err => {
          this.showSpinner = false;
          this.remoteData = [];
          console.error('Error importing remote project details ', err)
          this.errMsg = 'Error: unable to retrieve remote Feature Files';
        }
      );
  }

  // close dialog after import complete
  doImport(idx: number) {
    if (this.data.isScenarios) {
      return this.dialogRef.close({
        scenario: this.remoteData[idx].contents
      })
    }
    this.dialogRef.close(
      {
        featureFile: idx >= 0 ? this.remoteData[idx] : null,
        stepStatements: this.stepStatements,
        remotePaths: this.remotePaths,
        remoteRepo: this.remoteRepo
      });
  }

  // called from html, 
  // calls importFeatureFile to make remote http call and supplys function reference
  // to doImport which closes dialog when import complete
  import(idx: number) {
    this.showDownloadSpinner[idx] = true;
    this.errMsg = '';
    this.importFeatureFile(idx, this.doImport.bind(this))
  }

  // imports a specific feature file from remote repo if it hasn't already been downloaded
  importFeatureFile(idx: number, cbFn?: Function) {
    this.errMsg = '';
    if (this.remoteData[idx].contents) {
      // already have contents
      return cbFn ? cbFn(idx) : null;
    }
    this.remoteCalls.getFeatureFile(this.remoteData[idx].fileName, this.remoteData[idx].pathName, this.remoteRepo.id)
      .subscribe(
        (fileText: any) => {
          if (this.data.isScenarios) {
            this.showDownloadSpinner[idx] = false;
            this.remoteData[idx].contents = this.extractScenario(fileText, this.remoteData[idx].scenarioName)
          } else {
            this.remoteData[idx].contents = fileText;
          }
          return cbFn ? cbFn(idx) : null;
        },
        err => {
          this.showDownloadSpinner[idx] = false;
          console.error('Error getting featurefile text', err)
          this.errMsg = 'Error getting feature file text';
        }
      );
  }

  // extracts specific scenario from imported feature file text
  extractScenario(fileText: string, scenarioTitle: string): string {
    return this.parserService.extractScenario(fileText, scenarioTitle)
  }

  // called from html to expand or close a listed feature file or scenario
  // calls importFeatureFile to download feature file if necessary
  toggleView(idx: number) {
    this.importFeatureFile(idx)
    const currentVal = this.open[idx];
    this.open = Array(this.remoteData.length).fill(false)
    this.open[idx] = !currentVal;
  }

  //cancels dialog
  onNoClick(): void {
    this.dialogRef.close();
  }

}
