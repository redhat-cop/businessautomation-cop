This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 7.2.2.

### Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

### Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

### Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

In dev run `npm run test-dev` to run tests with watcher


# Feature File Tool

## Angular 7 App / typescript

## plugins:
* ngx-fileserver - read and download files to local filesystem

* angular-webstorage-service - persistdata in browser local storage

* HandsOnTable - for spreadsheet components data table and example table.  Note, at time of creating the initial project the [HandsOnTable](https://handsontable.com/) plugin was opensource.  However, this has changed since March 2019 and HandsOnTable uses a non-commercial license.  So, this component will need to be replaced.

* angular/material - UI components

* angular/flex-layout - layout

* lodash

## Docs

docs are at `docs/technical-info.md` and `docs/user-info.md`

## How to run application

1. Local dev

    * use node 8.11.3 or higher

    * tested using node v12.18.0

    * run `npm install`

    * run `npm install -g @angular/cli`

    * run `ng serve`

    * open browser and goto `http://localhost:4200/home`

2. Configure Host

    * host name is set in `src/app/constants/index.ts`

    ```java
    public static BACKEND_HOST: string = 'http://localhost:3000';
    ```

3. Tests

There are unit teste for the parsers and utils services, run with `ng test`

4. Build
    * Run `ng build` to build the project.  Use the `--prod` flag for a production build.

    * Built application is in `./dist/feature-tool` directory.

    * With nodejs installed, app can be run, using simple http-server module for example, as follows:

        ```bash
        npx http-server dist/feature-tool -p 8100

        in browser, navigate to http://localhost:8100/home
        ```
    * the dist folder is checked into the Repo

5. Create war file 
    * run ng build

    * execute commands:

        ```bash
        cd ./dist/feature-tool
        jar cvf ../feature-tool-ui.war .
        ```
    
    * feature-tool-ui.war will be created in `./dist` directory