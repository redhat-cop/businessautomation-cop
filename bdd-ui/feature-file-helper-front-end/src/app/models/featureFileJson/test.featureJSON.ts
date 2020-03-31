import { FeatureFile } from '../../models/models'

// input for unit tests

export const FEATUREFILE_JSON: FeatureFile = {
  "feature": "Retrieve Account Balance",
  "featureComment": 'This is a feature comment',
  "featureTag": "@tag1 @feature1",
  "scenarios": [
    {
      "scenario": "Manager should be able to access all Accounts up to Level 2",
      "scenarioComment": 'This is a Scenario Comment',
      "given": [
        {
          "text": "I am a manager",
          "dataTable": null
        }
      ],
      "when": [
        {
          "text": "I login with my credentials",
          "dataTable": [
            [
              "Username",
              "Password"
            ],
            [
              "Bill",
              "password123"
            ]
          ]
        },
        {
          "text": "I request balance for <accountnumber> from the accounts API",
          "dataTable": null
        }
      ],
      "then": [
        {
          "text": "I should get <balance> as the response",
          "dataTable": null
        }
      ],
      "keyVars": [
        "accountnumber",
        "balance"
      ],
      "examples": [
        [
          "accountnumber",
          "balance"
        ],
        [
          "11111",
          "99"
        ],
        [
          "22222",
          "999"
        ],
        [
          "33333",
          "9999"
        ],
        [
          "44444",
          "Account Not Found"
        ]
      ],
      "errorMessages": []
    },
    {
      "scenario": "Supervisor should be able to access all Accounts up to Level 1",
      "given": [
        {
          "text": "I am a supervisor",
          "dataTable": null
        }
      ],
      "when": [
        {
          "text": "I login with my credentials",
          "dataTable": [
            [
              "Username",
              "Password"
            ],
            [
              "Betty",
              "password177"
            ]
          ]
        },
        {
          "text": "I request balance for accountnumber \"1111\" from the accounts API",
          "dataTable": null
        }
      ],
      "then": [
        {
          "text": "I should get \"99\" as the response",
          "dataTable": null
        }
      ],
      "keyVars": [],
      "examples": null,
      "errorMessages": []
    },
    {
      "scenario": "Normal user should be able to access only Accounts at Level 0",
      "given": [
        {
          "text": "I am a normal user",
          "dataTable": null
        }
      ],
      "when": [
        {
          "text": "I login with my credentials",
          "dataTable": [
            [
              "Username",
              "Password"
            ],
            [
              "Tim",
              "password999"
            ]
          ]
        },
        {
          "text": "I request balance for <accountnumber> from the accounts API",
          "dataTable": null
        }
      ],
      "then": [
        {
          "text": "I should get <balance> as the response",
          "dataTable": null
        }
      ],
      "keyVars": [
        "accountnumber",
        "balance"
      ],
      "examples": [
        [
          "accountnumber",
          "balance"
        ],
        [
          "11111",
          "Not Authorised"
        ],
        [
          "22222",
          "Not Authorised"
        ],
        [
          "33333",
          "9999"
        ],
        [
          "44444",
          "Account Not Found"
        ]
      ],
      "errorMessages": []
    }
  ],
  "showErrors": true,
  "pathName": null,
  "fileName": null,
  "background": [
    {
      "text": "The following users are valid",
      "dataTable": [
        [
          "Username",
          "Password",
          "UserType"
        ],
        [
          "Bill",
          "password123",
          "manager"
        ],
        [
          "Betty",
          "password123",
          "supervisor"
        ],
        [
          "Bob",
          "password123",
          "user"
        ]
      ]
    },
    {
      "text": "these accounts exist",
      "dataTable": [
        [
          "AccountNum",
          "AccountLevel",
          "Balance"
        ],
        [
          "11111",
          "2",
          "99"
        ],
        [
          "22222",
          "1",
          "999"
        ],
        [
          "33333",
          "0",
          "9999"
        ]
      ]
    }
  ]
}