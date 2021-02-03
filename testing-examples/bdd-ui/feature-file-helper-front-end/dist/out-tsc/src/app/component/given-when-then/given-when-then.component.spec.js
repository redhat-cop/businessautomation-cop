import { async, TestBed } from '@angular/core/testing';
import { GivenWhenThenComponent } from './given-when-then.component';
describe('GivenWhenThenComponent', function () {
    var component;
    var fixture;
    beforeEach(async(function () {
        TestBed.configureTestingModule({
            declarations: [GivenWhenThenComponent]
        })
            .compileComponents();
    }));
    beforeEach(function () {
        fixture = TestBed.createComponent(GivenWhenThenComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });
    it('should create', function () {
        expect(component).toBeTruthy();
    });
});
//# sourceMappingURL=given-when-then.component.spec.js.map