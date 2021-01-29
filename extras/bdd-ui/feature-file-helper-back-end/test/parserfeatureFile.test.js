var expect = require('chai').expect;
var parseFeatureFile = require('../lib/parseFeatureFile');
var fs = require('fs');
var _ = require('lodash')

describe('Test featurefile parser', function () {
  let featureFile;
  let stepsObj;

  before(function (done) {
    fs.readFile('./test/testData/manager-user.feature', 'utf8', function (err, fileContents) {
      if (err) throw err;
      featureFile = fileContents;
      stepsObj = parseFeatureFile.extractStepsAndScenarios(featureFile)
      done();
    });
  });

  describe('Extract Scanario and Scenario Outline statements', function () {
    it('should get 3 Scenarios Statements', function () {
      const scenario1 = 'Manager should be able to to login with correct credentials';
      const scenario2 = 'Manager should be able to access all Accounts up to Level 2'
      const scenario3 = 'Supervisor should not be able to to login with incorrect credentials'
      const scenarioNames = [scenario1, scenario2, scenario3]
      const scenarios = stepsObj.scenarios
      expect(scenarios.length).to.be.equal(3);
      expect(scenarioNames.includes(scenarios[0])).to.be.true
      expect(scenarioNames.includes(scenarios[1])).to.be.true
      expect(scenarioNames.includes(scenarios[2])).to.be.true
    });

  });

  describe('Extract Given statements', function () {

    it('should get 5 Given Statements', function () {
      expect(stepsObj.given.length).to.be.equal(5);
    });

    it('For Given Statement \'The following users are valid\' should have data table with 3 columns and 4 rows', function () {
      const given = _.find(stepsObj.given, { text: 'The following users are valid' });
      const rows = given.dataTable.length
      const columns = given.dataTable[0].length
      expect(columns).to.be.equal(3);
      expect(rows).to.be.equal(4);
    });

    it('For Given Statement \'The following users are valid\' the datatable headings should be UserName | Password | UserType', function () {
      const given = _.find(stepsObj.given, { text: 'The following users are valid' });
      const columnTitles = given.dataTable[0]
      expect(columnTitles[0]).to.be.equal('Username');
      expect(columnTitles[1]).to.be.equal('Password');
      expect(columnTitles[2]).to.be.equal('UserType');
    });

    it('For Given Statement \'The following users are valid\' the last Row values should be Bob | password123 | user', function () {
      const given = _.find(stepsObj.given, { text: 'The following users are valid' });
      const columnRow = given.dataTable[3]
      expect(columnRow[0]).to.be.equal('Bob');
      expect(columnRow[1]).to.be.equal('password123');
      expect(columnRow[2]).to.be.equal('user');
    });

    it('For Given Statement \'these accounts exist\' should have data table with 3 columns and 4 rows', function () {
      const given = _.find(stepsObj.given, { text: 'these accounts exist' });
      const rows = given.dataTable.length
      const columns = given.dataTable[0].length
      expect(columns).to.be.equal(3);
      expect(rows).to.be.equal(4);
    });

    it('For Given Statement \'these accounts exist\' the datatable headings should be AccountNum | AccountLevel | Balance', function () {
      const given = _.find(stepsObj.given, { text: 'these accounts exist' });
      const columnTitles = given.dataTable[0]
      expect(columnTitles[0]).to.be.equal('AccountNum');
      expect(columnTitles[1]).to.be.equal('AccountLevel');
      expect(columnTitles[2]).to.be.equal('Balance');
    });

    it('For Given Statement \'these accounts exist\' the last Row values should be 33333 | 0 | 9999', function () {
      const given = _.find(stepsObj.given, { text: 'these accounts exist' });
      const columnRow = given.dataTable[3]
      expect(columnRow[0]).to.be.equal('33333');
      expect(columnRow[1]).to.be.equal('0');
      expect(columnRow[2]).to.be.equal('9999');
    });

  });

  describe('Extract When statements', function () {

    it('should get 4 When Statements', function () {
      expect(stepsObj.when.length).to.be.equal(4);
    });

    it('For When Statement \'I login with my credentials\' should have data table with 2 columns and 2 rows', function () {
      const when = _.find(stepsObj.when, { text: 'I login with my credentials' });
      const rows = when.dataTable.length
      const columns = when.dataTable[0].length
      expect(columns).to.be.equal(2);
      expect(rows).to.be.equal(2);
    });

    it('For When Statement \'I login with my credentials\' the datatable headings should be UserName | Password ', function () {
      const when = _.find(stepsObj.when, { text: 'I login with my credentials' });
      const columnTitles = when.dataTable[0]
      expect(columnTitles[0]).to.be.equal('Username');
      expect(columnTitles[1]).to.be.equal('Password');
    });

    it('For When Statement \'I login with my credentials\' the last row values should be Bill | password123 ', function () {
      const when = _.find(stepsObj.when, { text: 'I login with my credentials' });
      const columnRow = when.dataTable[1]
      expect(columnRow[0]).to.be.equal('Bill');
      expect(columnRow[1]).to.be.equal('password123');
    });

  });

  describe('Extract Then statements', function () {

    it('should get 4 Then Statements', function () {
      expect(stepsObj.then.length).to.be.equal(4);
    });

  });

});
