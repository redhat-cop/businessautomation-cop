import { StepStatements, Step } from '../models/models'

// const GIVEN_STATEMENTS: string[] = [
//     "the authentication method <auth method>",
//     "an authentication method answer is provided by an API",
//     "that an API has sent an authentication answer to the app ",
//     "<auth method> requires data selection presented to customer",
//     "<auth method> requires multiple answer selections to be presented to customer",
//     "<auth method> requires answer input presented to customer",
//     "4 answer selections must be simulated (start with same character)",
//     "selection of simulated answers should be randomized",
// ];

// const WHEN_STATEMENTS: string[] = [
//     "identifying sending question to an calling system",
//     "identifying answer selections to return",
//     "the app determines the Authentication Method for service catalog requests",
//     "the app evaluates the answer that was provided by an API",
//     "the auth policy does not match the answer correctly",
//     "the auth policy still has eligible authentication questions to be asked",
//     "the API has responded with the authentication method Authentication answer",
//     "the response provided from the API does match the value of authentication method answer",
//     "the response provided from the API does not match the value of authentication method answer",
//     "there are additional authentication methods available to be asked"
// ];

// const THEN_STATEMENTS: string[] = [
//     "tell the API to deny service",
//     "provide instructions",
//     "the App returns proceed with Service response to the API",
//     "the Component is updated with a calculated score of X",
//     "the App returns question to the API",
//     "the Decision Engine returns how to present the question to the API",
//     "<auth method>, <Data>  will be excluded",
//     "retrieve the actual authentication answer",
//     "identify 4 simulated authentication answers using <simulation_criteria>",
//     "return 4 simulated authentication answers and 1 actual authentication answer",
//     "require an input and selection of answer to be provided by a calling system",
//     "provide <instructions> on how to solicit answer"
// ];

// const GIVEN_STATEMENTS: string[] = [
//     "The following users are valid",
//     "these accounts exist",
//     "I am a manager",
//     "I am a supervisor",
//     "I am a normal user"
// ];

// const WHEN_STATEMENTS: string[] = [
//     "I login with my credentials",
//     "I request balance for <accountnumber> from the accounts API",

// ];

// const THEN_STATEMENTS: string[] = [
//     "I should get <balance> as the response",
// ];



const GIVEN_STATEMENTS: Step[] = [];

const WHEN_STATEMENTS: Step[] = [];

const THEN_STATEMENTS: Step[] = [];


export const STATEMENTS: StepStatements = {
    givenStatements: GIVEN_STATEMENTS,
    whenStatements: WHEN_STATEMENTS,
    thenStatements: THEN_STATEMENTS
}
