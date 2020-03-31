
export class FEATURE_FILE {

    jsonFeature: any = {
            "featllure": {
                "location": {
                    "line": 1,
                    "column": 1
                },
                "language": "en",
                "keyword": "Feature",
                "name": "Is it Friday yet?",
                "description": "  Everybody wants to know when it's Friday",
                "children": [
                    {
                        "scenario": {
                            "location": {
                                "line": 4,
                                "column": 3
                            },
                            "keyword": "Scenario",
                            "name": "Sunday isn't Friday",
                            "steps": [
                                {
                                    "location": {
                                        "line": 5,
                                        "column": 5
                                    },
                                    "keyword": "Given ",
                                    "text": "today is Sunday"
                                },
                                {
                                    "location": {
                                        "line": 6,
                                        "column": 5
                                    },
                                    "keyword": "When ",
                                    "text": "I ask whether it's Friday yet"
                                },
                                {
                                    "location": {
                                        "line": 7,
                                        "column": 5
                                    },
                                    "keyword": "Then ",
                                    "text": "I should be told \"Nope\""
                                }
                            ]
                        }
                    },
                    {
                        "scenario": {
                            "location": {
                                "line": 9,
                                "column": 3
                            },
                            "keyword": "Scenario Outline",
                            "name": "Friday is Friday",
                            "steps": [
                                {
                                    "location": {
                                        "line": 10,
                                        "column": 5
                                    },
                                    "keyword": "Given ",
                                    "text": "today is <day1>"
                                },
                                {
                                    "location": {
                                        "line": 11,
                                        "column": 5
                                    },
                                    "keyword": "When ",
                                    "text": "I ask whether it's <day2> yet"
                                },
                                {
                                    "location": {
                                        "line": 12,
                                        "column": 5
                                    },
                                    "keyword": "Then ",
                                    "text": "I should be told \"TGIF\""
                                }
                            ],
                            "examples": [
                                {
                                    "location": {
                                        "line": 14,
                                        "column": 5
                                    },
                                    "keyword": "Examples",
                                    "tableHeader": {
                                        "location": {
                                            "line": 16,
                                            "column": 7
                                        },
                                        "cells": [
                                            {
                                                "location": {
                                                    "line": 16,
                                                    "column": 9
                                                },
                                                "value": "day1"
                                            },
                                            {
                                                "location": {
                                                    "line": 16,
                                                    "column": 16
                                                },
                                                "value": "day2"
                                            }
                                        ]
                                    },
                                    "tableBody": [
                                        {
                                            "location": {
                                                "line": 17,
                                                "column": 7
                                            },
                                            "cells": [
                                                {
                                                    "location": {
                                                        "line": 17,
                                                        "column": 9
                                                    },
                                                    "value": "mon"
                                                },
                                                {
                                                    "location": {
                                                        "line": 17,
                                                        "column": 16
                                                    },
                                                    "value": "tue"
                                                }
                                            ]
                                        },
                                        {
                                            "location": {
                                                "line": 18,
                                                "column": 7
                                            },
                                            "cells": [
                                                {
                                                    "location": {
                                                        "line": 18,
                                                        "column": 9
                                                    },
                                                    "value": "wed"
                                                },
                                                {
                                                    "location": {
                                                        "line": 18,
                                                        "column": 16
                                                    },
                                                    "value": "thu"
                                                }
                                            ]
                                        },
                                        {
                                            "location": {
                                                "line": 19,
                                                "column": 7
                                            },
                                            "cells": [
                                                {
                                                    "location": {
                                                        "line": 19,
                                                        "column": 9
                                                    },
                                                    "value": "wer"
                                                },
                                                {
                                                    "location": {
                                                        "line": 19,
                                                        "column": 16
                                                    },
                                                    "value": "dfg"
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                ]
            }
        }
    

}