import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { HotTableRegisterer } from '@handsontable/angular';
import * as Handsontable from 'handsontable';
import { FeatureStoreService } from '../../services/feature-store.service';
import { UtilFunctionsService } from '../../services/util-functions.service';
import { RemoveHTMLPipe } from '../../pipes/remove-html.pipe'
import { MatDialog } from '@angular/material';
import _map from 'lodash/map'
import _isArray from 'lodash/isArray'
import _findIndex from 'lodash/findIndex'
import _isString from 'lodash/isString'

@Component({
  selector: 'app-example-table',
  templateUrl: './example-table.component.html',
  styleUrls: ['./example-table.component.css']
})
export class ExampleTableComponent implements OnInit {
  panelOpenState = false;
  dataSource: any = [];
  displayedColumns: string[];
  private hotRegisterer = new HotTableRegisterer();
  private hotSettings: Handsontable.GridSettings
  private hot: Handsontable
  private id: string;
  errMsg: string = '';
  colNames: string[];

  // @Input() colNames: Array<string>
  @Input() scenarioIdx: number;
  @Output() updateExTableParent: any = new EventEmitter<any>();

  constructor(
    private featureStore: FeatureStoreService,
    private utils: UtilFunctionsService,
    private dialog: MatDialog,
    private removeHTML: RemoveHTMLPipe
  ) { }

  ngOnInit() {
    this.id = `exampleTable${this.scenarioIdx}`;
    this.colNames = this.featureStore.getVars(this.scenarioIdx);

    this.hotSettings = {
      // colHeaders: true,
      data: null,
      preventOverflow: 'horizontal',
      colHeaders: [...this.colNames],
      rowHeaders: true,
      startRows: 5,
      startCols: this.colNames.length,
      stretchH: 'all',
      height: 160,
      contextMenu: [
        'row_above',
        'row_below',
        'remove_row',
        // 'col_left',
        // 'col_right',
        'remove_col'
      ],
      afterChange: this.onCellChange.bind(this),
      afterCreateRow: this.onCellChange.bind(this),
      afterRemoveRow: this.onCellChange.bind(this),
      afterRemoveCol: this.onColDelete.bind(this),
      // afterOnCellMouseDown: this.renameColumn.bind(this)
    }
    console.log('INIT EXAMPLE TABLE ')
  }

  //called from parent scenario to turn on errors
  displayError(errMsg: string) {
    this.errMsg = errMsg;
  }

  hideError() {
    this.errMsg = '';
  }

  //called from parent scenario to remove data and reset table
  resetData() {
    this.errMsg = '';
    this.hot = this.hotRegisterer.getInstance(this.id);
    if (this.hot) {
      this.hot.updateSettings(this.hotSettings, true);
    }
  }

  // called from parent scenario to initialise table with new data retrieved from storage
  populateTable() {
    this.hot = this.hotRegisterer.getInstance(this.id);
    let data = [...this.featureStore.getExamples(this.scenarioIdx)];
    data.splice(0, 1)// remove colnames row
    if (data && data[0] && _isArray(data[0]) && this.hot) {
      this.hot.loadData(data)
    }
  }

  // called when data in table body is changed, persists changes to storage
  onCellChange(hot: any, changes: any) {
    // only save if something has changed
    if (!changes) {
      return
    }
    const dataArray: string[][] = hot.getData();
    const saveArr: string[][] = [];
    for (let idx = 0; idx < dataArray.length; idx++) {
      // only save rows which have data
      if (_findIndex(dataArray[idx], (v) => _isString(v)) > -1) {
        saveArr.push(dataArray[idx]);
      }
    }
    // if error message is viisble, turn it off when all cells are valid
    // if (this.showError && this.utils.isNoEmptyCells(saveArr)) {
    //   this.showError = false;
    // }
    // this.featureStore.setExamples(saveArr, this.scenarioIdx);
    this.saveTable(saveArr, hot.getColHeader() as string[])
  }

  // called when table column is deleted, perists change to storage
  // calls to parent to notify that Example Table has changed
  onColDelete(hot: any, idx: number, amount: number, deleteCols: string[]) {
    this.errMsg = '';
    this.colNames.splice(idx, amount)
    setTimeout(() => {
      hot.updateSettings({ colHeaders: [...this.colNames] }, true)
      // this.featureStore.setExamples(hot.getData(), this.scenarioIdx);
      this.saveTable(hot.getData(), this.hot.getColHeader() as string[])
      this.updateExTableParent.emit("deleteCol");
    }, 0)
  }

  saveTable(dataTable: string[][], colNames: string[]) {
    const _dataTable = [_map(colNames, (n)=>this.removeHTML.transform(n)), ...dataTable]
    this.featureStore.setExamples(_dataTable, this.scenarioIdx);
  }

  // called from parent scenatio to insert Column into example table
  insertCol(idx: number, varName: string) {
    this.errMsg = '';
    this.hot = this.hotRegisterer.getInstance(this.id);
    this.colNames = this.hot.getColHeader() as string[];
    this.hot.alter('insert_col', idx, 1);

    setTimeout(() => {
      this.colNames.splice(idx, 0, varName)
      this.hot.updateSettings({ colHeaders: [...this.colNames] }, false)
      // this.featureStore.setExamples(this.hot.getData(), this.scenarioIdx)
      this.saveTable(this.hot.getData(), this.hot.getColHeader() as string[])
      this.updateExTableParent.emit("insertCol");
    }, 50)
  }

  // called from parent scenatio to rename existing in example table
  renameCol(idx: number, newName: string) {
    this.errMsg = '';
    this.hot = this.hotRegisterer.getInstance(this.id);
    this.colNames[idx] = newName
    this.hot.updateSettings({ colHeaders: [...this.colNames] }, false)
    this.saveTable(this.hot.getData(), this.hot.getColHeader() as string[])
  }

  // called from parent scenatio to get list of column names
  getColNames(): string[] {
    this.hot = this.hotRegisterer.getInstance(this.id);
    return this.hot ? this.hot.getColHeader() as string[] : null;
  }

  // called from parent scenatio highlights column names in red to indicate mismatch with Vars array in parent scenario
  highlightCols(colNames: string[]) {
    this.hot = this.hotRegisterer.getInstance(this.id);
    const highlightHeaders: string[] = _map(this.hot.getColHeader(), (colName) => {
      if (colNames.indexOf(colName) > -1) {
        if (colName.indexOf('<b>') > -1 && colName.indexOf('</b>') > -1) {
          // already highlighted
          return colName;
        }
        return `<span class="red-text"><b>${colName}</b></span>`
      }
      return colName
    })
    const colNamesNoHTML = _map(colNames, col => ` "${this.removeHTML.transform(col)}"`);
    this.errMsg = `Error, Column ${colNames.length > 1 ? 'names' : 'name'} ${[...colNamesNoHTML]} ${colNames.length > 1 ? 'do' : 'does'} not match <???> tags above.  Click on tag above to resolve.`
    this.hot.updateSettings({ colHeaders: highlightHeaders }, false)
  }

}
