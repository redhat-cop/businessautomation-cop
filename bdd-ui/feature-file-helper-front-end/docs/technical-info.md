# Feature File Maker Tool

## INFO
This App provides a UI for viewing, creating and editing .feature files in [Gherkin](https://docs.cucumber.io/gherkin/reference/) format

The App can work as a standalone component working on your local file system or, in conjunction with the Feature File Maker Backend, it can integrate with feature files in a remote git repository

In local mode, the App can read .feature files from anywhere on your local file system. However, because of browser security, it can only save .feature files to your browser downloads directory

In remote mode, the App can import feature files, scenarios, Given / When / Then steps with data tables from a remote repository. It can save new or update existing .feature files to the remote repo in a new commit

The tool supports the following Gherkin syntax / Keywords:

* Feature:

* Feature comment - free-form text between the Feature: line and the first Background: or Scenario: line is recorded as a feature comment

* Background:

* Scenario: 

* Scenario Outline:

* @ tags - a line immediately before either Feature: or Scenario: line, starting with @ is recorded as a tag

* Given / When / Then / And steps

* Data Tables - any line within a step starting with | is parsed as a data table. It is assumed the first line is column headings

* Examples - any line, between the the Example keyword and the end of the curent Scenario, starting with | is parsed as an example table

Note: # comments are not supported yet and are currently ignored

## Environment Info
[Angular 7](https://angular.io/docs) 

[Angular Material](https://material.angular.io) for design / UI Components 

[handsontable](https://handsontable.com/) plugin for Data Tables / Spreadsheet components

See README.md for more info



## PAGES

### 1. Home

This is effectively a single page app with *HomeComponent* (`src/app/pages/home/home.component.html`) as the home page component

This home page has a toolbar with action menus
* **New** - clears the UI and begins a new blank .feature file

* **View Feature File** - shows the current data in .feature file format in a dialog

* **Import**

  * **From Remote Repository** - displays list of remote .feature files which maybe imported

  * **From Local File System** - load .feature file from local file system

* **Export**

  * **To Remote Repository** - saves current .feature files to remote repo as new commit (context sensitive - only available when remote Repo has been downloaded)

  * **To Local File System** - saves current .feature file to local browser downloads folder

* **Remote**
  * **Import Remote Data** - imports lists of remote Scenarios / Given / When / Then / Datatables and makes them available in the UI

  * **Clear Imported Remote Data** - removes any downloaded Scenarios / Given / When / Then / Datatables (context sensitive - only available when remote Repo has been downloaded)

  * **Reset Remote Repo** - deletes the local copy of the remote repo from the backend causing it to be refreshed with a new git pull.  Use this if you are getting errors saving to the remote repo (context sensitive - only available when remote Repo has been downloaded)

* **Remote Repository**
    * when a remote repository has been downloaded, its name will be displayed in the header toolbar (context sensitive - only available when remote Repo has been downloaded)


The home page displays expansion panels for:
* the Feature File title and comment

* Background steps - has child **_GivenWhenThenComponent_** (`src/app/components/given-when-then/given-when-then.component.html`)

* Scenarios - has child **_ScenarioComponent_** (`src/app/components/scenario/scenario.component.html`) which has children **_GivenWhenThenComponent_** (`src/app/components/given-when-then/given-when-then.component.html`) and an optional child Example Table **_ExampleTableComponent_** component



## COMPONENTS

### 1. ScenarioComponent 
* Displays a Scenario, comprising of a title, three **_GivenWhenThenComponent_** components for Given / When / Then steps and an optional Example Table **_ExampleTableComponent_** component

* Each Feature file may have multiple Scenario Components

* child component of **_HomeComponent_**

### 2. GivenWhenThenComponent 
* Component to handle Given or When or Then and additional And steps. Also has optional child component **_DataTableComponent_** for Data Tables

* child component of **_ScenarioComponent_**

* child component of **_HomeComponent_** to handle Background sections

### 3. ExampleTableComponent 
* Component for Example Tables using the HandsOnTable plugin. 

* data table cells can be edited by user

* rows can be inserted and deleted by user

* columns can be removed by user

* insert and renaming columns is done programmatically from the parent  **_ScenarioComponent_**

* Child component of **_ScenarioComponent_**

### 4. DataTableComponent 
* Component for Data Tables using the HandsOnTable plugin. 

* data table cells can be edited by user

* rows can be inserted and deleted by user

* columns can be inserted and deleted by user

* column titles can be edited by the user

* Child component of **_GivenWhenThenComponent_**

### 5. DialogComponent 
* Utility Generic Dialog Component which can be configured, based on input title, message, feature file etc to handle:

    * error dialogs

    * confirm dialogs

    * file save / export dialog

    * file import dialog

    * view feature file dialog

* called from **_HomeComponent_**, **_ScenarioComponent_**

### 6. DialogImportComponent 
* Dialog Component used to import feature files and scenarios from remote repos. Lists and displays remote feature files and scenarios for import. 

* On initialising, remote http call pulls in list of remote repos from back end.  If there is only one repo, it automatically makes http call to download lists of feature files, scenarios, steps and paths.  If there sre multiple repos, user must select a repo first, then the http call is made

* When user selects specific file or scenario to import, remote http call is made to import the feature file contents

* called from **_HomeComponent_** and **_ScenarioComponent_**

### 7. DialogRemoteComponent 
* Dialog Component used to import data from remote repos. Lists remote repos, so user can selelct one

* On initialising, remote http call pulls in list of remote repos from back end.  

* When user selects specific repo, remote http call is made to import the steps form the repo

* called from **_HomeComponent_** and **_ScenarioComponent_**

### 8. DialogTableComponent 
* Dialog Component used to select and configure, rename or insert new colums in Data Tables

* called from  **_ScenarioComponent_**



## SERVICES
### 1. FeatureStoreService 
* Stores all current feature file details with getters and setters

* contains validation functions

* persists data to local storage using `angular-webstorage-service` plugin


### 1. FeatureFileParserService 
* Converts imported .feature file in text format into JSON structure

* `convertFeatureFiletoJSON` function converts imported files

* `convertScenarioToJSON` function converts remotely imported scenarios


### 3. FeatureJSONParserService 
* Converts JSON feature file data into text format for .feature file

* `convertJSONtoFeature` function generates feature text

### 4. RemoteCallsService 
* makes remote http calls using HttpClient

### 5. RemoteCallsService 
* makes remote http calls using HttpClient

## MODELS
### 1. `src/app/models/models.ts` contains typescript interface definitions used throughout the application
### 2. `src/app/models/featureFileJson/test.featureJSON.ts` contains JSON feature file objects used for unit tests
### 3. `src/app/models/featureFiles/test.featureFile.ts` containsformatted feature file strings used for unit tests


## PIPES
### 1. SearchRemotePipe

* used in  **_DialogImportComponent_** to filter list of displayed remote data based on item fileName and scenarioName 

### 2. SearchRepoPipe

* used in  **_DialogRemoteComponent_** to filter list of displayed remote repos based on name

### 3. RemoveHTMLPipe

* used in  **_DialogTableComponent_** to format displayed text by removing html elements




