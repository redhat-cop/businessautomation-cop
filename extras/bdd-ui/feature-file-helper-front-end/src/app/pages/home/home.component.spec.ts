import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeComponent } from './home.component';
import { GivenWhenThenComponent } from '../../components/given-when-then/given-when-then.component'
import { ScenarioComponent } from '../../components/scenario/scenario.component'

import { MaterialModule } from '../../material-module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ExampleTableComponent } from 'src/app/components/example-table/example-table.component';
import { DataTableComponent } from 'src/app/components/data-table/data-table.component';
import { StorageServiceModule } from 'angular-webstorage-service';
import { HotTableModule } from '@handsontable/angular';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        HomeComponent,
        ScenarioComponent,
        GivenWhenThenComponent,
        ExampleTableComponent,
        DataTableComponent
      ],
      imports: [
        MaterialModule,
        FormsModule,
        ReactiveFormsModule,
        HotTableModule.forRoot(),
        StorageServiceModule,
        HttpClientModule,
        BrowserAnimationsModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create Home Component', () => {
    expect(component).toBeTruthy();
  });

  // fit('should have feature panel', () => {
  //   let featurePanel: HTMLElement = fixture.nativeElement.querySelector('.openfile');
  //   expect(featurePanel.textContent).toContain('Fekkature', 'feature panel')
  // });

});
