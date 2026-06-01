@ui @end-to-end
Feature: InsiderOne Careers - End to End Test

  @end-to-end
  Scenario: Verify Insider homepage and Quality Assurance job application flow
    # Home page assertion
    Given Insider home page is opened
    
    # Careers Page and QA Job Positions Verification
    When Navigate to careers page
    And Click "See all teams" button
    And Select "Quality Assurance" team
    And All jobs contain "Quality Assurance, QA" position
    # And All jobs contain "Quality Assurance" department # Department locater is not existed on page
    And All jobs contain "Istanbul, Turkey" location
    
    # Application Form Redirect Verification
    When Click Apply button on first job
    And Click "Apply For This Job" button
    Then Lever application form page opens
    