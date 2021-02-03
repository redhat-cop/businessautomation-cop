import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from './material-module';
import { MatGridListModule, MatCardModule, MatMenuModule, MatIconModule, MatButtonModule } from '@angular/material';
import { LayoutModule } from '@angular/cdk/layout';
import { HomeComponent } from './pages/home/home.component';
import { GivenWhenThenComponent } from './components/given-when-then/given-when-then.component';
import { ExampleTableComponent } from './components/example-table/example-table.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FlexLayoutModule } from '@angular/flex-layout';
import { HttpClientModule } from '@angular/common/http';

import { FileSaverModule } from 'ngx-filesaver';
import { StorageServiceModule } from 'angular-webstorage-service';
import { HotTableModule } from '@handsontable/angular';
import { DialogComponent } from './components/dialog/dialog.component';
import { MAT_DIALOG_DEFAULT_OPTIONS } from '@angular/material';
import { DialogTableComponent } from './components/dialog-table/dialog-table.component';
import { RemoveHTMLPipe } from './pipes/remove-html.pipe';
import { DataTableComponent } from './components/data-table/data-table.component';
import { ScenarioComponent } from './components/scenario/scenario.component';
import { DialogImportComponent } from './components/dialog-import/dialog-import.component';
import { DialogRemoteComponent } from './components/dialog-remote/dialog-remote.component';
import { SearchRemotePipe } from './pipes/search-remote.pipe';
import { SearchRepoPipe } from './pipes/search-repo.pipe';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    GivenWhenThenComponent,
    ExampleTableComponent,
    DialogComponent,
    DialogTableComponent,
    RemoveHTMLPipe,
    DataTableComponent,
    ScenarioComponent,
    DialogImportComponent,
    DialogRemoteComponent,
    SearchRemotePipe,
    SearchRepoPipe
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MaterialModule,
    MatGridListModule,
    MatCardModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    LayoutModule,
    FormsModule,
    ReactiveFormsModule,
    FlexLayoutModule,
    FileSaverModule,
    StorageServiceModule,
    HotTableModule.forRoot()
  ],
  entryComponents: [HomeComponent, DialogComponent, DialogTableComponent, DialogImportComponent, DialogRemoteComponent],
  providers: [
    { provide: MAT_DIALOG_DEFAULT_OPTIONS, useValue: { hasBackdrop: true } },
    RemoveHTMLPipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
