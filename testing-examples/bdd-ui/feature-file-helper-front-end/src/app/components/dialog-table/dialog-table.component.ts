import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { FormControl, Validators } from '@angular/forms';

@Component({
  selector: 'app-dialog-table',
  templateUrl: './dialog-table.component.html',
  styleUrls: ['./dialog-table.component.css']
})
export class DialogTableComponent {
  fileName: string = '';
  showYesNo: boolean = false;
  showOKCancel: boolean = false;
  actions: string[] = ['Rename Existing Column', 'Insert New Column'];
  colName: FormControl;


  constructor(
    public dialogRef: MatDialogRef<DialogTableComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: any
  ) {
    this.colName = new FormControl('', Validators.required);
  }

  onNameSelected(colIdx){
    this.dialogRef.close({colIdx});
  }

  insertColumn(){
    this.dialogRef.close("insert");
  }

  onNoClick(): void {
    this.dialogRef.close();
  }


}
