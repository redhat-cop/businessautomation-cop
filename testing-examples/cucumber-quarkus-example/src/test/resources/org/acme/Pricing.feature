Feature: Pricing DMN Tests

    # Basic Test
    Scenario: Basic Pricing Test
        Given the drivers age is 47
        And no previous incidents
        When I execute the pricing model
        Then I expect the base price to be 500

    # Complex testing example
    Scenario Outline: Complex Outline Example
        Given the drivers age is <Drivers Age>
        And <Previous Incidents> previous incidents
        When I execute the pricing model
        Then I expect the base price to be <Base Price>

        Examples:
            | Drivers Age | Previous Incidents | Base Price |
            | 17          | no                 | 800        |
            | 17          | has                | 1000       |
            | 47          | no                 | 500        |
            | 47          | has                | 600        |

