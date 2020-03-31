# Feature File Maker Tool

This App provides a UI for viewing, creating and editing .feature files in [Gherkin](https://docs.cucumber.io/gherkin/reference/) format

The App can work as a standalone component working on your local file system or, in conjunction with the Feature File Maker Backend, it can integrate with feature files in a remote git repository

In local mode, the App can read .feature files from anywhere on your local file system. However, because of browser security, it can only save .feature files to your browser downloads directory

In remote mode, the App can import feature files, scenarios, Given / When / Then steps with data tables from a remote repo. It can save new or update existing .feature files to the remote repo in a new commit.

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



## MAIN SCREEN USER INTERFACE (UI)

The screen features collapsible panels for Feature details, Background Details, and Scenario Details. Click the &or; and &and; to expand and collapse panels.  There is a single Feature Panel and a single Background panel.  There can be multiple Scenario panels, only one of which may be open/editable at a time. 

### 1. FEATURE PANEL

* Enter the Feature File title in the first line. The title is required, optional comments and tags may be entered on lines 2 and 3. In Gherkin language, a  Tags line should start with @, e.g. @test99.  An @ will be added on saving if not there already.

### 2. BACKGROUND PANEL

* The background section is optional, it is not necessary for a valid feature file.  Typically the background is used for intial setup steps, common to all Scenarios, to ensure a consistent environment for example.

* Given and multiple And steps may be entered.  The "add AND step" option will become available from the menu on the right, once the first Given step has been entered.  And steps may also be deleted from the right menu

* to enter the Given/AND title, you can either type it in or, when a remote repository has been downloaded, select from a dropdown list of remote steps.  In the latter case typing will also search/filter the dropdown list

* A data table can be added to each step.  See DATA TABLE section for details

### 3. SCENARIO

Scenario panel has sub panels for Scenario details and Given, When and Then steps.  There can be multiple Scenarios.  Use the "Add Scenario" button to add additional scenarios.  When a remote repository has been downloaded, there will also be a an option to import remote Scenarios using the "Import Scenario" button

* In the Scenario sub panel, enter the Scenario title in the first line. The title is required, optional comments and tags may be entered on lines 2 and 3. In Gherkin language, a  Tags line should start with @, e.g. @test99.  An @ will be added on saving if not there already.

* For each Given / When / Then sub panel, the title and multiple AND steps may be entered.  The "add AND step" option will become available from the menu on the right, once the step title has been entered.  AND steps may also be deleted from the right menu

* to enter the step title, you can either type it in or, when a remote repository has been downloaded, select from a dropdown list of remote steps.  In the latter case typing will also search/filter the dropdown list

* A data table can be added to each step.  See DATA TABLE section for details

* To initiate creation of an Example Tables for a Scenario, type the column titles inside \< \> tags, e.g. \<username\> in the step title.  Once column tags are added they will be listed and there will be an option to create an example table using the "Create Example Table" button. IT is best to enter all column tags before creating the Example Table. See EXAMPLE TABLE section for more details.

### 4. DATA TABLES

Data tables can be added to any Given / When / Then step by selecting the "add Data Table" option form the right menu

* The Data Table operates with spreadsheet like functionality

* The initial data table created has 2 columns named A and B. Click on a column title to rename it.

* Type value in cell to enter data

* Additional rows and columns may be added, or deleted, by right clicking on any cell and selecting relevant option from the menu

### 5. EXAMPLE TABLES

Each Scenario may have one optional Example Table

* The Example Table operates with spreadsheet like functionality

* Type value in cell to enter data

* the Column Titles and number of columns will match the column < > tags entered in the Given / When / Then steps

* Additional rows and columns may be added, or deleted, by right clicking on any cell and selecting relevant option from the menu

* Once the Example Table has been created, if you add, delete, or edit names of < > column tags in the Steps, there will be a mismatch bewteeen the < > tags and the column titles in the Example Table. These mismatches will be highlighted in red or with an error message.  You need to resolve these mismatches manually.  If you click on the Red tag buttons above the table, there will be option to update the Example Table be inserting a new column or renaming an existing column.  Alternatively, you may need to delete columns in the Example Table manually be right clicking.

* click on "Clear All Data" button to delete all data in the Example Table

* click on "Delete Table" button to permanently delete the Example Table

## MENU OPTIONS

If a remote Repository has been downloaded and its Steps / Scenario data is present in the UI, the name of the current Remote Repository will be displayed in the menu header bar at the top right

### 1. New

* Clears all current data in the UI and initiates creation of a new Feature File. There will be a dialog box displayed to confirm it is ok to delete any existing data.

### 2 View Feature File

* Displays the current feature file in .feature format in a dialog box

### 3.1  Import / From Remote Repository
Shows a dialog box which is used to import a Feature File, Steps, Data Tables and Scenarios from a remote Repository.  

* If there is more than one remote Repository available from the back end, there will be an initial drop down selection box for the user to select, by name, which repository to import. When the Repository is selected, feature file data will be downloaded and a list of remote feature files listed in the dialog box

    * If there is only one remote repository, the remote files will be downloaded and presented automatically.

    * If there are multiple remote repositories, and one has been downloaded already, the UI will default to showing the current downloaded repository data.

    * If there are multiple remote repositories, the dropdown selection box can be used to switch between them.

* Once a list of feature files has been downloaded and losted, click on any feature file or its magnifying class to view the feature file before downloading that specific file

* The search box may be used to refine/filter the list of feature files, based on the file name.

* click on the red cloud download button to download a specific feature file into the UI

### 3.2  Import / From Local File System

* Imports .feature Feature File from local file system

* should get file explorer window, navigate to file and click open

### 4.1  Export / To Remote Repository

* Exports Feature File, in .feature format, to remote repository where it is pushed as a new Git commit.

* The .feature file format is displayed in the dialog for review before saving

* By default the filename and pathname of the original imported feature file is used but new values can be entered to create a new file.

* The pathname may be typed in or selected from existing pathnames using the dropdown list

* By default the export will be to the current downloded remote Repository, as named inthe header bar.  To export to a different remote repository, use the _Remote / Import Remote Repository Data_ menu action to switch to the new repository and then Export

* The current feature file in the UI must be in a valid format.  If the file is invalid, it is not possible to save and an context sensitive error message should be displayed in the UI

### 4.2  Export / To Local File System

* Exports Feature File, in .feature format, to local browser Downloads directory.

* The .feature file format is displayed in the dialog for review before saving

* a filename must be entered before saving

* The current feature file in the UI must be in a valid format.  If the file is invalid, it is not possible to save and an context sensitive error message should be displayed in the UI

### 5.1 Remote / Import Remote Data

* This imports a list of Steps and Scenarios from a remote Repository. Unlike the Import menu action, it will not download a feature file.  Use this if you wish to import the remote Steps / Scenario lists without actually importing a specific feature file or if you want to export the current UI feature file to a different remote Repository

    * If there are multiple remote Repositories available, one must be selected from the dropdown to intiiate the dropdown.

### 5.2 Remote / Clear Imported Remote Data

* This removes any Steps and Scenario Lists which have been downloaded from the remote Repository. 


### 5.3 Reset Remote Repo

* This forces the deletion of the local copy of the remote repository stored in the Feature File Maker Backend and initiates a fresh Git pull to create a new copy.  Use this option if there are errors trying to save to the remote repository