import { Component, OnInit, EventEmitter, AfterViewInit, Output, ViewChild, ViewChildren, ElementRef, QueryList, Input } from '@angular/core';
import { FormBuilder, Validators, FormGroup, NgForm } from '@angular/forms';
import { DialogComponent } from '../../components/dialog/dialog.component'
import { DialogTableComponent } from '../../components/dialog-table/dialog-table.component'
import { MatDialog } from '@angular/material';
import { FeatureStoreService } from '../../services/feature-store.service'
import { FeatureFileParserService } from '../../services/featurefile-parser.service'
import { UtilFunctionsService } from '../../services/util-functions.service'
import { FileSaverService } from 'ngx-filesaver';
import { GivenWhenThenComponent } from '../../components/given-when-then/given-when-then.component'
import { ExampleTableComponent } from '../../components/example-table/example-table.component'
import { DialogImportComponent } from '../../components/dialog-import/dialog-import.component'
import _map from 'lodash/map';
import _uniq from 'lodash/uniq';
import { FeatureFile, Var, VarUpdate } from 'src/app/models/models';

@Component({
  selector: 'app-scenario',
  templateUrl: './scenario.component.html',
  styleUrls: ['./scenario.component.css']
})
export class ScenarioComponent implements OnInit, AfterViewInit {
  scenarioForm: FormGroup;
  varsArray: Array<Var> = [];  //vars array of tags in <> from steps
  updateNeeded: boolean = false;
  givenVars: Array<any>;
  whenVars: Array<any>;
  thenVars: Array<any>;
  featureTitle: string;
  scenarioComment: string;
  scenarioTag: string;
  showChips: boolean = true;
  duplicates: string[] = [];
  showTable: boolean = false;
  tableColDelete: string[];
  tableValidationError: string = "";
  dialogRef: any;

  @Input() scenarioIdx: number
  @Input() isRemoteData: boolean
  @Input() totalScenarios: number
  @Output() scenarioAction: any = new EventEmitter<any>();

  @ViewChild(ExampleTableComponent) tableComponent: ExampleTableComponent;
  @ViewChild('formScenario') formDirective: NgForm;
  @ViewChildren(GivenWhenThenComponent) stepComponents: QueryList<GivenWhenThenComponent>;

  constructor(
    private formBuilder: FormBuilder,
    private dialog: MatDialog,
    private featureStore: FeatureStoreService,
    private featureParser: FeatureFileParserService,
    private fs: FileSaverService,
    private utils: UtilFunctionsService,
  ) { }

  ngOnInit() {
    console.log('SCENARIO INIT')
    this.givenVars = [];
    this.whenVars = [];
    this.thenVars = [];
    this.scenarioForm = this.formBuilder.group({
      scenario: [
        this.featureStore.getScenarioText(this.scenarioIdx) || "",
        Validators.compose([Validators.required])
      ],
      scenarioComment: [
        this.featureStore.getScenarioComment(this.scenarioIdx) || ""
      ],
      scenarioTag: [
        this.featureStore.getScenarioTag(this.scenarioIdx) || ""
      ]
    });
  }

  ngAfterViewInit() {
    //timeouts needed to kake sure handsontable component are created before adding data
    setTimeout(() => {
      const varsIn: string[] = this.featureStore.getVars(this.scenarioIdx) || [];
      if (this.featureStore.getShowErrors()) {
        this.showErrors()
      }
      if (varsIn && varsIn.length > 0) {
        this.varsArray = _map(varsIn, (item) => { return { value: item, update: false } })
        this.showTable = true;
        setTimeout(() => {
          this.tableComponent.populateTable()
          this.checkUpdateNeeded(varsIn)
        }, 0);
      }
    }, 0)
  }

  // utility function to return scenarioForm in scenario.component.html
  get f() { return this.scenarioForm.controls; }

  // save scenario title after update in UI
  onInputFieldChange(type: string) {
    if (type === "scenario") {
      this.featureStore.setScenarioText(this.scenarioForm.value.scenario, this.scenarioIdx)
    } else if (type === "tag") {
      this.featureStore.setScenarioTag(this.scenarioForm.value.scenarioTag, this.scenarioIdx)
    } else {
      this.featureStore.setScenarioComment(this.scenarioForm.value.scenarioComment, this.scenarioIdx)
    }
  }


  // opens generic, configurable DialogComponent.  must pass in callback function to execute if dialog closes with a returned result
  // @param title: dialog title
  // @param message: dialog message
  // @param featureFile: featureFile string to display
  // @param fn: function to execute when dialog closes after 'yes'
  openDialog(title: string, message: string, featureFile: string, fn: any): void {
    //check not already opened
    if (this.dialog.openDialogs.length > 0) return
    const dialogRef = this.dialog.open(DialogComponent, {
      width: featureFile ? '90%' : '300px',
      data: { title, message, featureFile }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        fn(result)
      }
    });
  }

  // show validation errors in UI
  showErrors() {
    this.scenarioForm.controls.scenario.markAsTouched()
    this.stepComponents.forEach((s) => { s.showErrors() })
    // setTimeout(() => {
    //   this.tableComponent.displayError('Error')
    // }, 0); 
  }

  // called from resetTable() after confirm dialog, clears data from example table and saves
  doResetTable() {
    this.featureStore.resetTableData(this.scenarioIdx);
    this.tableComponent.resetData();
    this.checkUpdateNeeded(_map(this.varsArray, (v) => v.value))
  }

  // called from scenario.component.html. shows confrim dialog to remove data from example table
  resetTable() {
    this.openDialog("Reset Table", "OK to remove all data in table?", null, this.doResetTable.bind(this));
  }

  // called from deleteTable() after dialog, deletes example table
  doDeleteTable() {
    this.featureStore.resetTableData(this.scenarioIdx);
    this.showTable = false;
    this.checkUpdateNeeded(_map(this.varsArray, (v) => v.value))
  }

  // called from scenario.component.html. shows confrim dialog to remove data from example table
  deleteTable() {
    this.openDialog("Delete Table", "OK to delete table? All data will be removed. ", null, this.doDeleteTable.bind(this));
  }

  // called from scenario.component.html 
  createTable() {
    this.showTable = true;
  }

  // bound to stepOuput event emitter in GivenWhenThenComponent 
  // called from GivenWhenThenComponent children to update and save varsArray
  // varArray contans array of <> tags from GWT steps
  updateVarsList(input: any, idx: number) {
    if (input.stepTitle === "GIVEN") {
      this.givenVars = input.keywords;
    } else if (input.stepTitle === "WHEN") {
      this.whenVars = input.keywords;
    } else if (input.stepTitle === "THEN") {
      this.thenVars = input.keywords;
    }
    const vars: string[] = _uniq((this.givenVars).concat(this.whenVars).concat(this.thenVars));
    this.varsArray = _map(vars, (item) => { return { value: item, update: false } })
    this.featureStore.setVars(vars, idx);
    if (this.showTable) {
      //if table exists check if column update needed
      this.checkUpdateNeeded(vars)
    }
  }

  // @Params - vars is list of <???> in feature file
  // varsArray is in form {value, update}
  // this function compares the values in vars Array with the example table column headers
  // if there is a mismatch update it set to true 
  checkUpdateNeeded(vars: string[]) {
    if (!this.showTable) {
      // add var values to varArray and reset to all false before running compare check with eable columns
      this.varsArray = _map(vars, (item, idx) => { return { value: item, update: false } })
      return
    }
    const varUpdate: VarUpdate = this.featureStore.checkUpdateNeeded(vars, this.tableComponent.getColNames())
    const updateVars: boolean[] = varUpdate.updateArr
    this.varsArray = _map(vars, (item, idx) => { return { value: item, update: updateVars[idx] } })
    this.tableColDelete = varUpdate.deleteArr
    if (this.tableColDelete.length > 0) {
      this.tableComponent.highlightCols(this.tableColDelete);
    }
  }

  // called from scenario.component.html
  // opens dialog with options to rename or insert columns in example table
  openTableDialog(colNames: string[], idx: number, name: string): void {
    //check not already opened
    if (this.dialog.openDialogs.length > 0) return
    const dialogRef = this.dialog.open(DialogTableComponent, {
      width: '500px',
      data: { colNames, name }
    });

    dialogRef.afterClosed().subscribe(selected => {
      if (selected === 'insert') {
        const insertIdx = this.utils.findInsertIndex(this.varsArray, idx)
        this.tableComponent.insertCol(this.utils.findInsertIndex(this.varsArray, idx) + 1, this.varsArray[idx].value)
      } else if (selected && selected.colIdx > -1) {
        this.tableComponent.renameCol(selected.colIdx, name)
      }

      // timeout to allow table column names to update
      setTimeout(() => this.checkUpdateNeeded(_map(this.varsArray, (v) => v.value)), 100)
    });
  }

  // handle click on varsArray chips in scenario.component.html
  // calls openTableDialog to open DialogTableComponent dialog
  handleTableColsUpdate(idx: number) {
    if (this.varsArray[idx].update) {
      this.openTableDialog(this.tableComponent.getColNames(), idx, this.varsArray[idx].value);
    }
  }

  // bound to updateExTableParent event emitter in ExampleTableComponent 
  // called from ExampleTableComponent child, calls checkUpdateNeeded() to compare values
  // from datatable columns with values in varsArry to see if there is mismatch which needs to be handled
  // varArray contans array of <> tags from GWT steps
  onTableChanged(updateType: string) {
    this.checkUpdateNeeded(_map(this.varsArray, (v) => v.value))
  }

  // called from scenario.component.html
  // emits scenarioAction output event to add or remove or import sceanrio
  // event is handled by parent HomeComponent to carry out the action 
  doScenarioAction(type: string, idx: number) {
    if (type === 'import') {
      if (this.dialog.openDialogs.length > 0) return//prevent multiple dialogs
      this.dialogRef = this.dialog.open(DialogImportComponent, {
        width: '90%',
        position: { top: '120px' },
        data: { title: 'Remote Scenarios', message: 'Choose File to Import', isScenarios: true }
      });

      this.dialogRef.afterClosed().subscribe(result => {
        if (result && result.scenario) {
          this.scenarioAction.emit({
            action: type,
            idx: idx,
            scenario: result.scenario
          })
        }
      });
    } else {
      this.scenarioAction.emit({
        action: type,
        idx: idx
      })
    }
  }

}
