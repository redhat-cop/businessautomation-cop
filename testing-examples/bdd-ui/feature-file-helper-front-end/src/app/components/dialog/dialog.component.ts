import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { FeatureStoreService } from '../../services/feature-store.service'

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.css']
})
export class DialogComponent {
  fileName: string = '';
  showYesNo: boolean = false;
  showOKCancel: boolean = false;
  showOK: boolean = false;
  remotePathNames: string[];

  constructor(
    public dialogRef: MatDialogRef<DialogComponent>,
    private featureStore: FeatureStoreService,
    @Inject(MAT_DIALOG_DATA)
    public data: any
  ) {

    // TODO too messy, refactor this
    this.showOK = (data.featureFile ||
      data.title === "Error Reading Feature File" ||
      data.title.indexOf('File Save') > -1 ||
      data.message === 'Error: Unable to Save FeatureFile') && (
        data.message !== "Enter File Name" &&
        data.message !== 'Saving...')

    this.showYesNo = data.message &&
      data.message !== 'Enter File Name' &&
      data.message.indexOf('Error') < 0 &&
      data.message !== 'Saving...' &&
      data.message !== 'Error: Unable to Save FeatureFile' &&
      data.title !== "Error Reading Feature File";

    if (data.title.indexOf('Remote') > -1) {
      this.remotePathNames = this.featureStore.getRemotePaths();
    }
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  // TODO regex to make sure filename / pathnames are valid
  removeSpaces(field: string) {
    this.data[field] = this.data[field].replace(/\s/g, '');
  }

  addFeature(field: string) {
    this.data[field] = this.data[field].replace(/.feature/g, '')+ '.feature';
  }

}
