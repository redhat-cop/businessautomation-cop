import * as tslib_1 from "tslib";
import { Component } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
var GivenWhenThenComponent = /** @class */ (function () {
    function GivenWhenThenComponent() {
        this.statements = [];
    }
    GivenWhenThenComponent.prototype.ngOnInit = function () {
        this.statements.push(new FormControl('', [
            Validators.required,
            Validators.email,
        ]));
    };
    GivenWhenThenComponent = tslib_1.__decorate([
        Component({
            selector: 'app-given-when-then',
            templateUrl: './given-when-then.component.html',
            styleUrls: ['./given-when-then.component.css']
        }),
        tslib_1.__metadata("design:paramtypes", [])
    ], GivenWhenThenComponent);
    return GivenWhenThenComponent;
}());
export { GivenWhenThenComponent };
//# sourceMappingURL=given-when-then.component.js.map