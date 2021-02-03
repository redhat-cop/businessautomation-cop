var _this = this;
import * as tslib_1 from "tslib";
import { AppPage } from './app.po';
import { browser, logging } from 'protractor';
describe('workspace-project App', function () {
    var page;
    beforeEach(function () {
        page = new AppPage();
    });
    it('should display welcome message', function () {
        page.navigateTo();
        expect(page.getTitleText()).toEqual('Welcome to feature-tool!');
    });
    afterEach(function () { return tslib_1.__awaiter(_this, void 0, void 0, function () {
        var logs;
        return tslib_1.__generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, browser.manage().logs().get(logging.Type.BROWSER)];
                case 1:
                    logs = _a.sent();
                    expect(logs).not.toContain(jasmine.objectContaining({
                        level: logging.Level.SEVERE,
                    }));
                    return [2 /*return*/];
            }
        });
    }); });
});
//# sourceMappingURL=app.e2e-spec.js.map