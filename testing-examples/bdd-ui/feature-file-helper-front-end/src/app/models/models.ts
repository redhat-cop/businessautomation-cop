
export interface Step {
    text: string;
    dataTable: string[][];
}

export interface RemoteRepo {
    id: string;
    name: string;
}

export class FeatureFile {
    feature: string;
    featureComment?: string;
    scenarios: Scenario[];
    background?: Step[];
    showErrors: boolean;
    fileName?: string;
    pathName?: string;
    featureTag?:string;
}

export interface Scenario {
    scenario: string;
    scenarioComment?: string;
    given: Step[];
    when: Step[];
    then: Step[];
    keyVars: string[];
    examples?: string[][];
    errorMessages?: string[];
    tag?: string;
}

export interface Error {
    name: string;
    message: string;
}

export interface ScenarioOutput {
    title: string;
    given: string;
    when: string;
    then: string;
    examples?: string;
    comment?: string;
    tag?: string;
}

export interface ParseFileIn {
    ok: boolean;
    error?: Error;
    featureFile?: FeatureFile
}

export interface Var {
    value: string;
    update: boolean;
}

export interface VarUpdate {
    updateArr: boolean[];
    deleteArr: string[];
}

export interface ExampleError {
    error: boolean,
    message?: string,
    index?: number
}

export interface RemoteFeatureFileCallObject {
    featureFiles: RemoteDataObj[];
    givenSteps: Step[];
    whenSteps: Step[];
    thenSteps: Step[];
    scenarios: RemoteDataObj[];
}

export interface RemoteDataObj {
    scenarioName?: string;
    fileName: string;
    pathName: string;
    contents?: string;
}

export interface StepStatements {
    givenStatements: Step[];
    whenStatements: Step[];
    thenStatements: Step[];
}
