// https://stackoverflow.com/questions/18348437/how-do-i-edit-the-header-text-of-a-handsontable
//  https://jsfiddle.net/websiter/eLdkwju2/

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { HotTableRegisterer } from '@handsontable/angular';
import { FeatureStoreService } from '../../services/feature-store.service';
import * as Handsontable from 'handsontable';
import _findIndex from 'lodash/findIndex'
import _isString from 'lodash/isString'
import _isArray from 'lodash/isArray'


@Component({
  selector: 'app-data-table',
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.css']
})
export class DataTableComponent implements OnInit {
  private hotRegisterer = new HotTableRegisterer();
  private hotSettings: Handsontable.GridSettings
  private hot: Handsontable
  errMsg: string = 'feck'

  @Input() tableDataIn: string[][];
  @Input() tableID: string;
  @Input() tableDataIdx: number;
  @Output() saveDataInParent: any = new EventEmitter<any>();

  constructor(
    private featureStore: FeatureStoreService,
  ) { }

  ngOnInit() {
    this.hotSettings = {
      // colHeaders: true,
      // data: [['a','b'],['c','d']],
      preventOverflow: 'horizontal',
      colHeaders: true,
      rowHeaders: true,
      startRows: 3,
      startCols: 2,
      stretchH: 'all',
      height: 120,
      contextMenu: [
        'row_above',
        'row_below',
        'remove_row',
        'col_left',
        'col_right',
        'remove_col'
      ],
      afterChange: this.onCellChange.bind(this),
      afterCreateRow: this.onCellChange.bind(this),
      afterRemoveRow: this.onCellChange.bind(this),
      afterRemoveCol: this.onCellChange.bind(this),
      afterCreateCol: this.onCellChange.bind(this),
      afterOnCellMouseDown: this.renameColumn.bind(this)
    }

    if (this.tableDataIn) {
      setTimeout(() => { this.populateTable(); }, 0)
    }

  }

  // copied from SO 
  // TODO  - make this more Angular
  // called from hot datatable when user clicks on column title and changes the value
  renameColumn(hot, event, coords, th) {
    if (coords.row === -1) {
      let input = document.createElement('input'),
        rect = th.getBoundingClientRect(),
        addListeners = (events, headers, index) => {
          events.split(' ').forEach(e => {
            input.addEventListener(e, () => {
              headers[index] = input.value;
              hot.updateSettings({ colHeaders: headers });
              this.onCellChange(hot, true) // save update
              setTimeout(() => {
                if (input.parentNode) input.parentNode.removeChild(input)
              });
            })
          })
        },
        appendInput = () => {
          input.setAttribute('type', 'text');
          input.style.cssText = '' +
            'position:absolute;' +
            'left:' + rect.left + 'px;' +
            'top:' + rect.top + 'px;' +
            'width:' + (rect.width - 4) + 'px;' +
            'height:' + (rect.height - 4) + 'px;' +
            'z-index:1000;' +
            'background-color:yellow' +
            'text-align:center';
          document.body.appendChild(input);
        };
      input.value = th.querySelector('.colHeader').innerText;
      appendInput();
      setTimeout(() => {
        input.select();
        addListeners('change blur', hot['getColHeader'](), coords['col']);
      });
    }
  }

  // called when hot data table changes
  // calls dataTableUpdate function in Parent GivenWhenThenComponent to update 
  onCellChange(hot: any, changes: any) {
    // only save if something has changed
    if (!changes) {
      return
    }
    const dataArray: string[][] = hot.getData();
    const saveArr: string[][] = [];
    saveArr.push(hot.getColHeader())
    for (let idx = 0; idx < dataArray.length; idx++) {
      // only save rows which have data
      if (_findIndex(dataArray[idx], (v) => _isString(v)) > -1) {
        saveArr.push(dataArray[idx]);
      }
    }
    // call parent update
    this.saveDataInParent.emit({
      tableDataIdx: this.tableDataIdx,
      dataTableArr: saveArr
    })
  }

// called from ngOnInit to initialise table with data retrieved from storage
  populateTable() {
    if (!this.tableDataIn || this.tableDataIn.length < 1) {
      return;
    }
    this.hot = this.hotRegisterer.getInstance(this.tableID);
    const headers = [...this.tableDataIn[0]]
    const arraySize = headers.length;
    this.hot.updateSettings({ colHeaders: headers }, false)
    if (this.tableDataIn.length > 1) {
      const data = [...this.tableDataIn]
      data.splice(0, 1)
      // if necessarypad out to 3 lines of data to fill up space in UI
      const size = data.length
      if (data.length < 3) {
        for (let idx = 1; idx <3; idx++) {
          if (!data[idx]) {
            data[idx] = new Array(arraySize).fill(null);
          }
        }
      }
      if (data.length > 0) {
        this.hot.loadData(data);
      } else {
        this.hot.updateSettings({
          startRows: 3,
          startCols: 2,
        }, false)
      }
    }
  }

}
