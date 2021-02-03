import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RemoteCallsService } from '../../services/remote-calls.service'
import { FeatureFileParserService } from '../../services/featurefile-parser.service'
import { RemoteFeatureFileCallObject, StepStatements, RemoteRepo } from '../../models/models'
import { FeatureStoreService } from '../../services/feature-store.service'
import _findIndex from 'lodash/findIndex'
import _uniq from 'lodash/uniq';
import { THIS_EXPR } from '@angular/compiler/src/output/output_ast';

@Component({
  selector: 'app-dialog-remote',
  templateUrl: './dialog-remote.component.html',
  styleUrls: ['./dialog-remote.component.css']
})
export class DialogRemoteComponent implements OnInit {
  private stepStatements: StepStatements;
  private remoteRepoList: RemoteRepo[];
  private errMsg: string;
  private showSpinner: boolean = false;
  private showDownloadSpinner = [];
  private remotePaths: string[];
  private remoteRepo: RemoteRepo;
  open: boolean[];

  constructor(
    private remoteCalls: RemoteCallsService,
    public dialogRef: MatDialogRef<DialogRemoteComponent>,
    private featureStore: FeatureStoreService,
    private parserService: FeatureFileParserService,
    @Inject(MAT_DIALOG_DATA)
    public data: any
  ) { }

  ngOnInit() {
    this.remoteRepo = this.featureStore.getRemoteRepo();
    this.getRemoteRepos()
  }

  // make remote http call to retrieve remote repo data
  getRemoteRepos() {
    this.errMsg = '';
    this.showSpinner = true;
    this.remoteCalls.listRemoteRepo()
      .subscribe(
        (data: RemoteRepo[]) => {
          this.remoteRepoList = data;
          this.showDownloadSpinner = Array(this.remoteRepoList.length).fill(false);
          this.showSpinner = false;
        },
        err => {
          this.remoteRepoList = [];
          this.showDownloadSpinner=[];
          console.error('Error getting listof remote Repos ', err)
          this.errMsg = 'Error retrieving list of remote Repos';
          this.showSpinner = false;
        }
      );
  }

  // make remote http call to retrieve remote repo data
  importRemoteRepo(repoId: string, idx:number) {
    this.errMsg = '';
    this.showDownloadSpinner[idx]=true;
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

          this.remoteRepo = this.remoteRepoList[idx];

          setTimeout(() => {
            //timeout to show spinner before closing so user knows something happened
            return this.dialogRef.close(
              {
                stepStatements: this.stepStatements,
                remotePaths: this.remotePaths,
                remoteRepo: this.remoteRepo
              });
          }, 500)

        },
        err => {
          this.showDownloadSpinner[idx]=false;
          console.error('Error importing remote project details ', err)
          this.errMsg = 'Error: unable to retrieve remote Feature Files';
        }
      );
  }

  //cancels dialog
  onNoClick(): void {
    this.dialogRef.close();
  }

}
