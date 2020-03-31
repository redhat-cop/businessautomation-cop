import { Component, OnInit, Injectable, ViewChild, ViewChildren, ElementRef, QueryList } from '@angular/core';
import { FormBuilder, Validators, FormGroup, NgForm } from '@angular/forms';
import { FeatureStoreService } from '../../services/feature-store.service'
import { FeatureFileParserService } from '../../services/featurefile-parser.service'
import { FeatureJSONParserService } from '../../services/featureJSON-parser.service'
import { UtilFunctionsService } from '../../services/util-functions.service'
import { RemoteCallsService } from '../../services/remote-calls.service'
import { FileSaverService } from 'ngx-filesaver';
import { MatDialog } from '@angular/material';
import { DialogComponent } from '../../components/dialog/dialog.component'
import { DialogImportComponent } from '../../components/dialog-import/dialog-import.component'
import { DialogRemoteComponent } from '../../components/dialog-remote/dialog-remote.component'
import { GivenWhenThenComponent } from '../../components/given-when-then/given-when-then.component'
import { ScenarioComponent } from '../../components/scenario/scenario.component'
import _uniq from 'lodash/uniq';
import _filter from 'lodash/filter';
import { FeatureFile, Var, Scenario, RemoteFeatureFileCallObject, Step, RemoteDataObj, StepStatements, RemoteRepo } from 'src/app/models/models';
import { version } from '../../../../package.json';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
@Injectable()
export class HomeComponent implements OnInit {
  featureForm: FormGroup;
  varsArray: Array<Var> = [];  //vars array from steps
  updateNeeded: boolean = false;
  featureTitle: string;
  background: Step;
  featureComment: string;
  featureTag: string;
  showChips: boolean = true;
  duplicates: string[] = [];
  showTable: boolean = false;
  tableColDelete: string[];
  tableValidationError: string = "";
  panelOpenState: boolean[]
  bgPanelState: boolean = false
  featurePanelState: boolean = false
  scenarios: Scenario[] = []
  showErrors: boolean = false;
  dialogRef: any;
  remoteRepo: RemoteRepo;
  versionStr: string;

  @ViewChild('fileInput') fileInputRef: ElementRef;
  @ViewChild('formDirective') formDirective: NgForm;
  @ViewChild('background') backgroundComponent: GivenWhenThenComponent;
  @ViewChildren(ScenarioComponent) scenarioComponents: QueryList<ScenarioComponent>;

  constructor(
    private formBuilder: FormBuilder,
    private dialog: MatDialog,
    private featureStore: FeatureStoreService,
    private featurefileParser: FeatureFileParserService,
    private featureJSONParser: FeatureJSONParserService,
    private remoteCalls: RemoteCallsService,
    private fs: FileSaverService,
    private utils: UtilFunctionsService
  ) { }

  ngOnInit() {
    console.log("HOME INIT")
    this.versionStr = 'Version ' + version
    this.featureForm = this.formBuilder.group({
      featureTitle: [
        this.featureStore.getFeature() || "",
        Validators.compose([Validators.required])
      ],
      featureComment: [
        this.featureStore.getFeatureComment() || ""
      ],
      featureTag: [
        this.featureStore.getFeatureTag() || ""
      ]
    });
    this.scenarios = this.featureStore.getScenarios()
    this.showErrors = this.featureStore.getShowErrors();
    this.panelOpenState = Array(this.scenarios.length).fill(false)
    this.remoteRepo = this.featureStore.getRemoteRepo();
  }

  get f() { return this.featureForm.controls; }

  // saves change in feature title or comment in the UI.  
  onInputFieldChange(type: string) {
    if (type === "title") {
      this.featureStore.setFeature(this.featureForm.value.featureTitle)
    } else if (type === "tag") {
      this.featureStore.setFeatureTag(this.featureForm.value.featureTag)
    } else {
      this.featureStore.setFeatureComment(this.featureForm.value.featureComment)
    }
  }

  // called to clear UI when new featrure file is being laded 
  resetThis() {
    // reset validation errors on the FormGroup Directive 
    this.formDirective.resetForm();
    // reset background component if it is open
    if (this.backgroundComponent) {
      this.backgroundComponent.ngOnInit();
    }
    this.ngOnInit()
  }

  // called after import scenario dialog box closes, adds imported scenario and saves
  doImportScenario(scenario: string, idx?: number) {
    let lineArr: string[] = scenario.split('\n')
    lineArr = _filter(lineArr, (line) => line !== "");
    const scenarioImport: Scenario[] = this.featurefileParser.convertScenarioToJSON(0, lineArr)
    if (idx >= 0) {
      //delete current and insert
      this.scenarios.splice(idx, 1, scenarioImport[0])
      this.panelOpenState[idx] = false;
    } else {
      //add to end
      this.scenarios.push(scenarioImport[0])
      this.panelOpenState.push(false)
    }
    this.featureStore.setScenarios(this.scenarios)
  }

  // this is bound to scenarioAction output emitter from children ScenarioComponents
  // gets called from ScenarioComponent to add / remove / import Scenario
  // for add it updates UI and saves
  // for remove / update it presents  dialog to confirm action
  doActionScenario(evt) {
    // add or remove or import scenarios based nn
    // events emitted in scenario.component.ts and scenario.component.html 
    if (evt.action === 'add') {
      this.scenarios.push(this.featureStore.newScenario())
      this.panelOpenState.push(false)
      this.featureStore.setScenarios(this.scenarios)
    } else if (evt.action === 'remove') {
      this.openDialog("Delete Scenario", "OK to delete this Scenario and it\'s data?", null, this.removeScenario.bind(this, evt.idx))
    } else if (evt.action === 'import') {
      if (evt.idx >= 0 && this.featureStore.checkScenarioHasData(evt.idx)) {
        // overwrite existing - show confirm dialog
        this.openDialog(
          "Import Scenario",
          "OK to overwrite existing Scenario?",
          null,
          this.doImportScenario.bind(
            this,
            evt.scenario,
            evt.idx
          )
        )
      } else {
        this.doImportScenario(evt.scenario, evt.idx)
      }
    }
  }

  // called from home componnet, creates dialog to impport scenario
  importScenario() {
    if (this.dialog.openDialogs.length > 0) return//prevent multiple dialogs
    this.dialogRef = this.dialog.open(DialogImportComponent, {
      width: '90%',
      position: { top: '120px' },
      data: { title: 'Remote Scenarios', message: 'Choose File to Import', isScenarios: true }
    });

    this.dialogRef.afterClosed().subscribe(result => {
      if (result && result.scenario) {
        this.doImportScenario(result.scenario, -1)
      }
    });
  }

  // called after remove scenario dialog is closed, removes scenario and saves
  removeScenario(idx: number) {
    this.scenarios.splice(idx, 1)
    this.panelOpenState.splice(idx, 1)
    this.featureStore.setScenarios(this.scenarios)
  }

  // called from Home.html, opens local file import dialog
  openExisting() {
    this.fileInputRef.nativeElement.click()
  }

  // called after local file import dialog closes with file selected
  // presents confirm dialog if current data needs to be deleted
  onFileOpen(event: any) {
    console.log('File Open on change', event.target.files[0])
    if (this.featureStore.checkFeatureFileHasData()) {
      // TODO why doesn't this work after dialog
      this.openDialog("File Open", "OK to delete existing data?", null, this.readFile.bind(this, event))
    } else {
      this.readFile(event);
    }
  }

  // load new file from local filesystem
  readFile(event: any) {
    var reader = new FileReader();
    reader.readAsText(event.target.files[0]);
    //reset input value so it can be read again
    this.fileInputRef.nativeElement.value = ""
    reader.onload = (e) => {
      this.parseFile({
        contents: reader.result as string,
        fileName: null,
        pathName: null
      })
    }
    reader.onerror = (e) => {
      console.error('ah noooooo, error reading file ', e)
    }
  }

  // handles importing feature files and other data from remote repos
  // converts imcoming data in .feature file format into a JSON object that can be stored and used in th UI  
  // from remote imports saves scenarios, given when then steps and lists of remote filenames and pathnames
  parseFile(file: RemoteDataObj, stepStatements?: StepStatements, remotePaths?: string[], remoteRepo?: RemoteRepo) {
    const fileInConvert = this.featurefileParser.convertFeatureFiletoJSON(file.contents, file.fileName, file.pathName)
    if (fileInConvert.ok) {
      this.loadNewFeatureFile(fileInConvert.featureFile)
      // save step statments from remote repo it they are there
      if (stepStatements) {
        this.featureStore.setStepStatements(stepStatements);
      }
      // save pahts and filenames from remote repo
      if (remotePaths) {
        this.featureStore.setRemotePaths(remotePaths);
      }
      // saves references to scenarios from remote repo
      if (remoteRepo) {
        this.featureStore.setRemoteRepo(remoteRepo);
        this.remoteRepo = remoteRepo;
      }
    } else {
      this.openDialog("Error Reading Feature File", fileInConvert.error.message, null, () => { });
    }
  }

  // called from home.html, validates feature file and shows dialod to fix error os enter filename to save
  fileSave(isRemote: boolean) {
    const title: string = isRemote ? `File Save (Remote Repository: ${this.remoteRepo.name})` : 'File Save (Local)';
    const fn = isRemote ? this.doFileSaveRemote : this.doFileSaveLocal;
    this.featureStore.setShowErrors(true)
    this.showErrors = true;
    if (this.featureStore.isFeatureFileValid()) {
      const featureFileJson: FeatureFile = this.featureStore.getFeatureFile()
      const featureFileText: string = this.featureJSONParser.convertJSONtoFeature(featureFileJson)
      this.openDialog(title, "Enter File Name", featureFileText, fn.bind(this), featureFileJson.fileName, featureFileJson.pathName)
    } else {
      this.featureForm.controls.featureTitle.markAsTouched(); // turns on error messageds in this page
      this.scenarioComponents.forEach((s) => { s.showErrors() })  // rurn on error messages in scenario
      // const tableError: ExampleError = this.featureStore.isVarsAndTableValid()
      // if (tableError.error) {
      // this.tableComponent.displayError(tableError);
      //TODO display errors seomwhere in UI
      //   console.error("Error in Example data ", tableError.message)
      // }
      this.openDialog(title, "Error: Invalid Feature File, please complete missing fields", null, () => { })
    }
  }

  // called afer fileSave dialog closes, saves file locally
  doFileSaveLocal(dataFromDialog: any) {
    const fileSave = this.featureJSONParser.convertJSONtoFeature(this.featureStore.getFeatureFile())
    var blob = new Blob([fileSave], { type: "text/plain;charset=utf-8" });
    this.fs.save(blob, `${dataFromDialog.fileName}`)
  }

  // called afer fileSave dialog closes, saves file to remote repo
  doFileSaveRemote(dataFromDialog: any) {
    //update filename and path and upload to repo
    const featureFileJson: FeatureFile = this.featureStore.getFeatureFile()
    let isNew: boolean;
    if (dataFromDialog.fileName !== featureFileJson.fileName || dataFromDialog.pathName !== featureFileJson.pathName) {
      isNew = true;
      featureFileJson.fileName = dataFromDialog.fileName
      featureFileJson.pathName = dataFromDialog.pathName;
      this.featureStore.loadNewFeatureFile(featureFileJson);
    } else {
      isNew = false;
    }
    const title = `File Save (Remote)`

    //open spinner for saving
    this.openDialog(title, "Saving...", null, () => { })

    this.remoteCalls.saveFeatureFile(
      featureFileJson.fileName,
      featureFileJson.pathName,
      this.remoteRepo.id,
      this.featureJSONParser.convertJSONtoFeature(this.featureStore.getFeatureFile()),
      isNew
    )
      .subscribe(
        (data: any) => {
          this.dialogRef.close();
        },
        err => {
          console.error('Error posting featurefile ', err)
          this.dialogRef.close();
          setTimeout(() => this.openDialog(title, "Error: Unable to Save FeatureFile", null, () => { }), 200)
        });
  }

  // opens generic, configurable DialogComponent.  must pass in callback function to execute if dialog closes with a returned result
  // @param title: dialog title
  // @param message: dialog message
  // @param featureFile: featureFile string to display
  // @param fn: function to execute when dialog closes after 'yes' 
  // @param fileName: feature file filename for remote save' 
  // @param pathName: feature file pathName for remote save' 
  openDialog(title: string, message: string, featureFile: string, fn: Function, fileName?: string, pathName?: string): void {
    //check not already opened
    if (this.dialog.openDialogs.length > 0) return
    const data = title.indexOf('Remote') > -1 ? { title, message, featureFile, fileName, pathName } : { title, message, featureFile }
    this.dialogRef = this.dialog.open(DialogComponent, {
      width: featureFile ? '90%' : '300px',
      data
    });

    this.dialogRef.afterClosed().subscribe(result => {
      if (result) {
        fn(result)
      }
    });
  }

  // called from home.html toolbar to initiate creating new feature file - displays dialog
  doNew() {
    this.openDialog("Create New Feature File", "OK to delete all current data?", null, this.loadNewFeatureFile.bind(this));
  }

  // loads dummy data for testing / display
  loadDummy() {
    this.loadNewFeatureFile(this.featureStore.loadDummyFeatureFile())
  }

  // loads new feature file from storage and intiates reset to load into UI
  loadNewFeatureFile(featureFile: FeatureFile) {
    this.featureStore.loadNewFeatureFile(featureFile);
    //update home component
    this.resetThis();
  }

  // called from home.html toolbar to show currnet data in .feature file format in a dialog
  viewFeatureFile() {
    const featureFileText: string = this.featureJSONParser.convertJSONtoFeature(this.featureStore.getFeatureFile())
    this.openDialog('Feature File Format', '', featureFileText, () => { });
  }

  // called from home.html toolbar to open dialog to import remote feature file and repo
  openRemote() {
    if (this.dialog.openDialogs.length > 0) return
    this.dialogRef = this.dialog.open(DialogImportComponent, {
      width: '90%',
      position: { top: '120px' },
      minHeight: '360px',
      data: { title: 'Remote Feature Files', message: 'Choose File to Import', isFeatureFiles: true }
    });

    this.dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // if there is existing data, show confirm dialog to delete it
        if (this.featureStore.checkFeatureFileHasData()) {
          this.openDialog(
            "File Open",
            "OK to delete existing data?",
            null,
            this.parseFile.bind(
              this,
              result.featureFile as RemoteDataObj,
              result.stepStatements as StepStatements,
              result.remotePaths,
              result.remoteRepo as RemoteRepo
            )
          )
        } else {
          this.parseFile(
            result.featureFile as RemoteDataObj,
            result.stepStatements as StepStatements,
            result.remotePaths,
            result.remoteRepo as RemoteRepo
          )
        }
      }
    });
  }

  // called from home.html toolbar to clear downloaded  data from remote repo
  // TODO need a dialog box here to confirm?
  clearRemoteData() {
    this.featureStore.setStepStatements({
      givenStatements: [],
      whenStatements: [],
      thenStatements: []
    });
    this.featureStore.setRemotePaths([]);
    this.featureStore.setRemoteRepo(null);
    this.remoteRepo = null;
    this.featureStore.resetFilePathNames();
    this.ngOnInit()
  }

  // called from home.html toolbar to import data from remote repo
  // TODO need a dialog box here to confirm?
  importRemoteData() {

    if (this.dialog.openDialogs.length > 0) return
    this.dialogRef = this.dialog.open(DialogRemoteComponent, {
      width: '60%',
      position: { top: '120px' },
      data: { title: 'Import Steps from Remote Repository', message: 'Choose File to Import', isImportRemote: true }
    });

    this.dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const stepsData: StepStatements = result.stepStatements
        this.featureStore.setStepStatements({
          givenStatements: stepsData.givenStatements,
          whenStatements: stepsData.whenStatements,
          thenStatements: stepsData.thenStatements,
        });
        this.featureStore.setRemotePaths(result.remotePaths);
        this.featureStore.setRemoteRepo(result.remoteRepo as RemoteRepo);
        this.remoteRepo = result.remoteRepo
        this.ngOnInit()
      }
    });

  }

  // called from home.html toolbar to reset remote repo if there are issues/errors with saving to remote repo
  forceResetRemoteRepo() {
    this.remoteCalls.forceResetRemoteRepo(this.remoteRepo.id)
      .subscribe(
        (data: RemoteFeatureFileCallObject) => {
          console.log('forceResetRemoteRepo completed successfully: ', data)
        },
        err => {
          console.error('Error from forceResetRemoteRepo ', err)
        }
      );
  }

}
