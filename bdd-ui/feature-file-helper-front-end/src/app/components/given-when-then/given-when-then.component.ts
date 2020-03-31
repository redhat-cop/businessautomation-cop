import { Component, OnInit, EventEmitter, Input, Output, Inject, Injectable } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { FeatureStoreService } from '../../services/feature-store.service'
import { Subscription } from 'rxjs'
import { UtilFunctionsService } from 'src/app/services/util-functions.service';
import { Step } from '../../models/models'
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-given-when-then',
  templateUrl: './given-when-then.component.html',
  styleUrls: ['./given-when-then.component.css']
})
@Injectable()
export class GivenWhenThenComponent implements OnInit {
  private statementsForm: Array<FormControl> = [];
  private showDataTable: boolean[] = [];
  private statements: Step[] = [];
  private tableVarsAll: Array<Array<any>> = []; //holds <var> TODO list of column titles?
  private initialiasFinished: boolean;
  private stepsStatements: Step[] = [];  //hardcoded steps options for deopdown
  filteredStepsStatements: Observable<Step[]>[] = [];

  @Input() stepTitle: string;
  @Input() scenarioIdx: number;
  @Output() stepOutput: any = new EventEmitter<any>();

  constructor(
    private featureStore: FeatureStoreService,
    private utils: UtilFunctionsService
  ) { }

  ngOnInit() {
    console.log("INITIALISE ", this.stepTitle)
    this.initialiasFinished = false;
    this.statementsForm = [];
    this.statements = [];
    this.tableVarsAll = [];
    this.showDataTable = [];
    const fromStore = this.featureStore.getSteps(this.stepTitle, this.scenarioIdx);

    if (fromStore && fromStore[0] && fromStore[0].text) {
      //initialise from existing storage
      for (let index = 0; index < fromStore.length; index++) {
        if (this.stepTitle === 'BACKGROUND') {
          this.statementsForm.push(new FormControl(fromStore[index].text))
        } else {
          this.statementsForm.push(new FormControl(fromStore[index].text, [
            Validators.required,
          ]))
        }
        // this.filteredStepsStatements = this.stepsFilter(this.statementsForm[index])
        // this.filteredStepsStatements.push(this.statementsForm[index].valueChanges
        //   .pipe(
        //     startWith(''),
        //     map(value => this._stepsFilter(value))
        //   ));
        this.addStepsFilter(this.statementsForm[index])
        this.statements.push(fromStore[index])
        this.tableVarsAll.push(this.utils.extractVars(fromStore[index].text, []));
        this.showDataTable.push(fromStore[index].dataTable !== null)
      }
    } else {
      // initialise with blank step
      if (this.stepTitle === 'BACKGROUND') {
        this.statementsForm.push(new FormControl(''))
      } else {
        this.statementsForm.push(new FormControl('', [
          Validators.required,
        ]))
      }
      this.addStepsFilter(this.statementsForm[0])
      this.tableVarsAll.push([])
      this.statements.push({ text: "", dataTable: null })
      this.showDataTable.push(false)
    }
    this.initialiseStepStatements(this.stepTitle)
    this.initialiasFinished = true;
    this.updateParentScenario()
  }

  // add filter to steps displayed in dropdown as user types
  addStepsFilter(formControl: FormControl) {
    this.filteredStepsStatements.push(formControl.valueChanges
      .pipe(
        startWith({ text: '', dataTable: null }),
        map(value => {
          return value
        }),
        map(value => typeof value === 'string' ? value : value.text),
        map(name => name ? this._stepsFilter(name) : this.stepsStatements.slice())
      ));
  }

  // filter steps as user types
  _stepsFilter(value: string): Step[] {
    if (this.stepsStatements.length < 0) {
      return [];
    }
    const filterValue = value.toLowerCase();
    return this.stepsStatements.filter(option => option.text.toLowerCase().includes(filterValue));
  }

  // function to control what is displayed in autocomplete input directive
  displayFn(step?: Step | string): string | undefined {
    if (typeof step === 'string') {
      return step
    }
    return step ? step.text : undefined;
  }

  // initialise Steps Statements from remote Repo for dropdown selection
  initialiseStepStatements(type: string) {
    if (type === 'BACKGROUND') {
      type = 'GIVEN';
    }
    const readStr: string = type.toLowerCase() + "Statements";
    this.stepsStatements = this.featureStore.getStepStatements()[readStr] || [];
  }

  // called from given-when-then.component.html to add an AND statement
  add(idx: number) {
    this.statementsForm.push(new FormControl('', [
      Validators.required
    ]))
    this.tableVarsAll.push([])
    this.statements.push({ text: "", dataTable: null })
    this.showDataTable.push(false)
    this.addStepsFilter(this.statementsForm[this.statementsForm.length - 1])
  }

  // show validation errors
  showErrors() {
    for (const formControl of this.statementsForm) {
      formControl.markAsTouched()
    }
  }

  // called from given-when-then.component.html to remove an AND statement
  remove(idx: number) {
    this.statementsForm.splice(idx, 1)
    this.statements.splice(idx, 1)
    this.tableVarsAll.splice(idx, 1)
    this.showDataTable.splice(idx, 1)
    this.filteredStepsStatements.splice(idx, 1)
    this.updateParentScenario();
    this.saveToStorage();
  }

  // called from given-when-then.component.html when step text is changed
  onChange(step: Step | string, idx: number) {
    // input may be string or Step  depending on whether user types or selects from dropdown
    if (this.initialiasFinished) {
      if (typeof step === 'string') {
        this.statements[idx].text = step;
        this.tableVarsAll[idx] = this.utils.extractVars(step, []);
      } else {
        this.statements[idx].text = step.text;
        if (step.dataTable) {
          this.statements[idx].dataTable = step.dataTable;
          this.showDataTable[idx] = true;
        }
        this.tableVarsAll[idx] = this.utils.extractVars(step.text, []);
      }
      this.updateParentScenario() // update varArray in parent
      this.saveToStorage();
    }
  }

  //save to storage when text is updated in UI
  saveToStorage() {
    this.featureStore.setSteps(this.stepTitle, this.statements, this.scenarioIdx)
  }

  // called after data change to update varArray and Example table in parent Scenario
  updateParentScenario() {
    this.stepOutput.emit({
      stepTitle: this.stepTitle,
      // statements: this.statements,
      keywords: [].concat.apply([], this.tableVarsAll) //flatten array
    })
  }

  // called from given-when-then.component.html to show datatable
  addDataTable(idx: number) {
    this.showDataTable[idx] = true;
  }

  // called from given-when-then.component.html to delete and remove datatable
  removeDataTable(idx: number) {
    this.showDataTable[idx] = false;
    this.statements[idx].dataTable = null;
    this.saveToStorage();
  }

  // bound to saveDataInParent event emitter in child DataTableComponent 
  // called from DataTableComponent child, to update and save child datatable changs to storage
  dataTableUpdate(dataTableData) {
    this.statements[dataTableData.tableDataIdx].dataTable = dataTableData.dataTableArr
    this.saveToStorage();
  }

}
