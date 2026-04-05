# TA Recruitment System

## Project Overview

This project implements a **Teaching Assistant Recruitment System** designed for BUPT International School.  
The system aims to streamline the recruitment process by allowing students to apply for teaching assistant positions while enabling module organisers to manage job postings and review applications.

The project is implemented as a **stand-alone Java application** and developed using **Agile methods**, including iterative development, version control using GitHub, and modular system design.

---

## Group Name-list

* **Jingwei Xu** 
  * QMUL ID: 231221249
  * GitHub: [@Jingwei-Xu](https://github.com/Jingwei-Xu)
* **Motong Long** 
  * QMUL ID: 231222095
  * GitHub: [@Longmotong](https://github.com/Longmotong)
* **Zihan Guo** 
  * QMUL ID: 231220219
  * GitHub: [@GuoZihan429](https://github.com/GuoZihan429) 
* **Yuxin Wang**
  * QMUL ID: 231220770
  * Github: [@Yuxin-Wang5](https://github.com/Yuxin-Wang5)
* **Yifan Cao** 
  * QMUL ID: 231220987
  * GitHub: [@rei129482](https://github.com/rei129482)
* **Yifan Lang** 
  * QMUL ID: 221167711
  * GitHub: [@ahuo-ahuo](https://github.com/ahuo-ahuo)

## System Users

The system supports three types of users:

### TA (Teaching Assistant Applicants)
Students who want to apply for TA positions.

### MO (Module Organisers)
Teachers who publish TA job positions and review applications.

### Admin
System administrator who monitors recruitment statistics and TA workload.

---

## System Features

### TA Functions
- Register and login to the system
- Create and edit personal profile
- Upload CV
- View available job positions
- View job details
- Apply for TA positions
- Check application status

### MO Functions
- Publish new job positions
- Edit job information
- Delete job postings
- View job information
- Review applicant list
- Accept or reject applications

### Admin Functions
- View TA workload
- View recruitment statistics
- Display system data overview
- Entry point for future AI-based analysis or recommendation

---

## System Architecture

The system is divided into several modules based on user roles.

```
System
│
├── Authentication Module
│
├── TA Module
│   ├── Profile Management
│   ├── CV Upload
│   └── Job Application
│
├── MO Module
│   ├── Job Management
│   └── Application Review
│
└── Admin Module
    ├── Statistics
    └── Workload Analysis
```

## Team Responsibilities

### Yifan Lang – Authentication and Navigation
Responsible for system authentication and entry.

Functions:
- User registration
- User login
- Role identification (TA / MO / Admin)
- System entry
- Page navigation

Branch:

---

### Jingwei Xu – TA Profile Management

Functions:
- Create and edit TA profile
- Manage skill information
- Upload CV

Branch:

---

### Motong Long – TA Job Application

Functions:
- View available jobs
- View job details
- Apply for jobs
- Check application status

Branch:

---

### Zihan Guo – MO Job Management

Functions:
- Publish job positions
- Edit job information
- Delete job postings
- View job details

Branch:

---

### Yuxing Wang – MO Application Review

Functions:
- View applicant list
- Review applications
- Accept or reject applicants
- Update application status

Branch:

---

### Yifan Cao – Admin Statistics and System Management

Functions:
- View TA workload
- Display recruitment statistics
- Provide entry point for AI-based recommendation and analysis

Branch:

---

## Project Structure

TA-Recruitment-System

README.md

```
src
├── auth
├── ta_profile
├── ta_application
├── mo_job
├── mo_review
└── admin

docs
├── prototype
├── product_backlog
├── report
└── Questionnaire

data
├── users.json
├── profiles.json
├── jobs.json
└── applications.json
```

## Iteration Progress

### Preparation（3.12-3.22）
- Reviewed the project brief and clarified the overall system scope.
- Collected background information on recruitment systems and common applicant-tracking functions.
- Studied the typical responsibilities of TAs in university settings.
- Conducted observation to understand the real TA workflow in the school context.
- Distributed questionnaires to TAs, MOs, and administrators and summarised the key findings.
- Identified the main user roles and analysed the current recruitment process.
- Converted the findings into user stories and organised them into the product backlog.
- Assigned priorities and estimates to backlog items.
- Prepared the initial prototype and basic system design.

### Iteration 1（3.23-3.29）
#### Planned goals
- Build the basic user interface for the first working version.
- Implement file-based data storage and handling.
- Implement the core workflow for browsing TA jobs.
- Implement job application submission.
- Implement application status tracking.
- Implement job posting for MO users.
- Implement applicant selection.
- Implement the function for checking overall TA workload.

#### Completed features
- Completed the first integrated version of the system and initially unified the implementation structure.
- Implemented the basic login and registration functions, including user login, user registration, and basic entry design for different user types.
- Implemented the core TA-side functions, including profile completion, CV upload status confirmation, job browsing, job application, and application progress tracking.
- Implemented the core MO-side functions, including posting new jobs, viewing applications, and handling applicant-related actions.
- Implemented the basic Admin-side functions, including overall TA workload checking and initial report-related support.
- Updated and enriched the shared data files so that testing scenarios across different users and modules became more complete.
- Organised the project folder structure and resource layout more clearly to support later integration and maintenance.

#### Issues and reflections
- The integration exposed consistency problems across modules, including mixed use of Java Swing and JavaFX, inconsistent UI sizes, and non-unified button styles.
- Some functions only worked correctly under specific hard-coded user IDs, which showed that data handling and user-state management were not robust enough.
- Data coverage was initially too limited, so some functions could not be fully tested until richer sample data was added.
- The login and registration module still had validation and user-type handling issues, such as weak input checks and unclear registration rules.
- On the TA side, some interface details and navigation behaviours were inconsistent, and application progress display still needed improvement.
- On the MO side, job posting, application review, logout behaviour, and page jumping still had logic and integration problems.
- On the Admin side, workload measurement rules and the reports function were not yet clear or complete.
- Overall, Iteration 1 showed that completing individual module functions was not enough; stronger integration standards, more consistent UI design, and more reliable shared data handling were needed for later iterations.

### Iteration 2
#### Planned goals
- Refactor and unify the codebase structure based on the integration problems found in Iteration 1, especially reducing inconsistency between modules and standardising the use of Java Swing.
- Improve the overall UI consistency, including window size, button style, navigation behaviour, and back-button design across different modules.
- Fix the user-state and data-handling issues identified in Iteration 1, so that functions no longer rely on specific hard-coded user IDs and can work correctly for different users.
- Improve input validation and user-type handling in the login and registration module, making registration rules and form checks clearer and more reliable.
- Refine the TA-side workflow by improving profile-related pages, CV handling, application progress display, and page transitions.
- Refine the MO-side workflow by improving job posting, applicant handling, logout behaviour, and page-jump logic.
- Improve the Admin-side functions by clarifying workload measurement rules and further developing report-related support.
- Expand and standardise the shared data files so that more realistic test cases can be covered across TA, MO, and Admin scenarios.
- Add the planned support functions from the backlog, including viewing course and MO details, working-hour calculation, exporting the final hiring list, and selected AI-supported features such as skill matching and missing-skill identification.
- Strengthen overall module integration so that the second iteration delivers a more stable, consistent, and complete working version of the system.
#### Completed features
#### Issues and reflections

### Iteration 3
#### Planned goals
#### Completed features
#### Issues and reflections

### Iteration 4
#### Planned goals
#### Completed features
#### Issues and reflections

---


## Work Log

### Jingwei Xu
| Date | Member | Location/Branch | Task Description |
| :--- | :--- | :--- | :--- |
| 2026-03-14 | @Jingwei-Xu | Jingwei-Xu/product-backlog | The questionnaire content for investigating user requirements has been uploaded |
| 2026-03-16 | @Jingwei-Xu | Jingwei-Xu/TA_prototype | Complete the prototype design of the image version |
| 2026-03-17 | @Jingwei-Xu | Jingwei-Xu/TA_prototype | Complete the prototype by adding text workflow |
| 2026-03-20 | @Jingwei-Xu | Jingwei-Xu/TA_prototype | Complete and integrate the first version of the group's overall prototype document and modify the format, adjusting the overall content |
| 2026-03-20 | @Jingwei-Xu | Jingwei-Xu/product-backlog | Integrate questionnaire results and group survey end user requirements, and complete the TA and MO sections of the Excel document |
| 2026-03-21 | @Jingwei-Xu | Jingwei-Xu/product-backlog | Complete the Admin sections of the Excel document |
| 2026-03-22 | @Jingwei-Xu | Jingwei-Xu/product-backlog | Update the final version of Excel document |





### Motong Long
| Date | Member | Location/Branch | Task Description |
| :--- | :--- | :--- | :--- |
| 2026-03-16 | @Longmotong |Motong-Long/TA_prototype  |Complete the prototype design of the image version  |
| 2026-03-17 | @Longmotong |Motong-Long/TA_prototype  |Complete the prototype by adding text workflow  |
| 2026-03-18 | @Longmotong |data  |upload the initial version of the data storage file that the application requires |
| 2026-03-20 | @Longmotong |Motong-Long/report  |complete the first version of the report writing |
| 2026-03-20 | @Longmotong |Motong-Long/TA_prototype  |Complete the format modification of the integrated prototype design and the adjustment of the public dashboard section |
| 2026-03-21 | @Longmotong |Motong-Long/report  |Complete the revision of the report and improve and supplement the supporting materials |
| 2026-03-22 | @Longmotong |Motong-Long/TA_job_application_module  |The initial state of the module has been created, including the work overview page, work details page, and application page |
| 2026-03-23 | @Longmotong |Motong-Long/TA_job_application_module  |I have added a detailed page for each application, strengthened the connection between each page, and updated the UI design of the dashboard and existing pages |
| 2026-03-24 | @Longmotong |Motong-Long/TA_job_application_module  |The specific application functions have been improved, enabling applications to actually access and save data in the data folder |
| 2026-03-28 | @Longmotong |Motong-Long/TA_job_application_module  |Perform initial integration on the MO terminal, remove one of the dashboards, and establish initial connections |
| 2026-03-29 | @Longmotong |Motong-Long/TA_job_application_module and data |Continue to complete the integration of the mo terminal, and establish basic connectivity with the login and registration system and also create more data examples |
| 2026-03-30 | @Longmotong |Motong-Long/modify_readme|Summarize the issues found in integrating the mo terminal and registration/login aspects, and update the specific iteration plan in the readme |





### Zihan Guo
| Date | Member | Location/Branch | Task Description |
| :--- | :--- | :--- | :--- |
| 2026-03-17 | @GuoZihan429 | Zihan-Guo/MO_prototype | Complete the first version of the MO prototype |
| 2026-03-18 | @GuoZihan429 | Zihan-Guo/MO_prototype | Improve the first version prototype and add user manual to the image |
| 2026-03-19 | @GuoZihan429 | Zihan-Guo/MO_prototype | Integrate with another MO teammate and then design the MO dashboard |
| 2026-03-20 | @GuoZihan429 | Zihan-Guo/MO_prototype | Refine the final version based on the questionnaire results and merge it into main |
| 2026-03-23 | @GuoZihan429 | Zihan-Guo/MO_Job_Management | Complete the initial structure design of the MO Job Management Module in Version 1, and build the dashboard page together with the basic navigation framework |
| 2026-03-24 | @GuoZihan429 | Zihan-Guo/MO_Job_Management | Add the job detail page, create job page, and edit job page in Version 1, and update the UI design of the dashboard and related pages for better consistency |
| 2026-03-25 | @GuoZihan429 | Zihan-Guo/MO_Job_Management | Add job detail, create job, and edit job pages in Version 1, and updates the UI design of the dashboard and related pages |
| 2026-03-26 | @GuoZihan429 | Zihan-Guo/MO_Job_Management | Improve the business logic of the Version 1 MO module, enabling job publishing, job editing, status switching, and JSON data access and saving functions |

### Yifan Cao
| Date | Member | Location/Branch | Task Description |
| :--- | :--- | :--- | :--- |
| 2026-03-17 | @rei129482 |Yifan-Cao/Admin_prototype  |Complete the first version of the interface design  |
| 2026-03-19 | @rei129482 |Yifan-Cao/Admin_prototype  |Complete the second version of the interface design  |
| 2026-03-19 | @rei129482 |Yifan-Cao/Admin_prototype  |Complete the final version of the prototype interface design and text description  |
| 2026-03-24 | @rei129482 |Yifan-Cao/Admin_module  |Completed the initial structure design of the admin module in version 1  |
| 2026-03-30 | @rei129482 |Yifan-Cao/Admin_module  |Updated the detailed implementation code for TA workload and report|
| 2026-03-31 | @rei129482 |Yifan-Cao/Admin_module  |Updated and modified the folder structure and content|

### Yuxin Wang
| Date | Member | Location/Branch | Task Description |
| :--- | :--- | :--- | :--- |
| 2026-03-17 | @Yuxin-Wang5 | Yuxin-Wang/MO_prototype | Complete prototype design ———— prototype-version1.pdf |
| 2026-03-18 | @Yuxin-Wang5 | Yuxin-Wang/MO_prototype | Add more comprehensive features in prototype design, including: rating TA, one-click quick review, etc. ———— prototype-version2.pdf |
| 2026-03-19 | @Yuxin-Wang5 | Yuxin-Wang/MO_prototype | Collaborated with another MO team leader to finalize the overall design modifications for the MO system prototype, and completed the drafting and formatting adjustments of the user manual. ———— MO_Application_review_prototype.pdf |
| 2026-03-22 | @Yuxin-Wang5 | Yuxin-Wang/modify_readme | Proofread the Application Review Module section in the "prototype.pdf" and revise the relevant parts in the readme file. |
| 2026-03-23 | @Yuxin-Wang5 | Yuxin-Wang/MO_application_review_module | Only the Dashboard page design has been implemented. |
| 2026-03-24 | @Yuxin-Wang5 | Yuxin-Wang/MO_application_review_module | Only basic page interactions for the MO client system were implemented. |
| 2026-03-25 | @Yuxin-Wang5 | Yuxin-Wang/MO_application_review_module | The dashboard page and the page/function components in the Application Review Module have been implemented, but the design shows significant differences from the prototype. |
| 2026-03-26 | @Yuxin-Wang5 | Yuxin-Wang/MO_application_review_module | The first iteration version has implemented features such as one-click approval. However, it cannot yet automatically evaluate resumes or display working hours. |

## How to Run the System

1. Clone the repository
```
git clone repository-url
```

2. Open the project using an IDE such as **IntelliJ IDEA** or **Eclipse**

3. Run the main program



## Future Improvements

Possible future enhancements include:

- AI-based job recommendation
- Skill gap analysis
- Automatic workload balancing
- Improved UI design
