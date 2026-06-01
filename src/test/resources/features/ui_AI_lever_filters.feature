@ui @ai-generated
Feature: AI-Generated Lever Job Listing Filter Validation


  @regression @ai-generated
  Scenario: Job listing filters refine results correctly on QA Istanbul page
    Given Insider home page is opened
    When Navigate to careers page
    And Click "See all teams" button
    And Select "Quality Assurance" team
    Then All four filter dropdowns are visible

    # ── Work type filter ──────────────────────────────────────────────────
    When Apply "Work type" filter with option "Full-Time (Remote)"
    Then The URL contains filter parameter "commitment=Full-Time (Remote)"
    And The job count is greater than zero
    When Reset "Work type" filter to default
    Then The URL does not contain "commitment"

    # ── Location type filter ──────────────────────────────────────────────
    When Apply "Location type" filter with option "Remote"
    Then The URL contains filter parameter "workplaceType=remote"
    And The job count is greater than zero
    When Reset "Location type" filter to default
    Then The URL does not contain "workplaceType"

    # ── Location filter ───────────────────────────────────────────────────
    When Apply "Location" filter with option "Istanbul"
    Then The URL contains filter parameter "location=Istanbul"
    And The job count is greater than zero
    When Reset "Location" filter to default
    Then The URL does not contain "location"

    # ── Team filter ───────────────────────────────────────────────────────
    When Apply "Team" filter with option "Quality Assurance"
    Then The URL contains filter parameter "team=Quality Assurance"
    And The job count is greater than zero
