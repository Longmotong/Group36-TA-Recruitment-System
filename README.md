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
* **李四** 
  * 学号: 202xxxx2
  * GitHub: [@lisi](https://github.com/lisi)

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
├── backlog
├── report
└── diagrams

data
├── users.json
├── profiles.json
├── jobs.json
└── applications.json
```

## Development Workflow

The project follows a **feature branch workflow**.

Main branch:main

Feature branches:
feature-auth-navigation
feature-ta-profile
feature-ta-application
feature-mo-job
feature-mo-review
feature-admin

The **main branch always contains the latest integrated and runnable version of the system**.

---

## How to Run the System

1. Clone the repository
```
git clone repository-url
```

3. Open the project using an IDE such as **IntelliJ IDEA** or **Eclipse**

4. Run the main program
```
MainApp.java
```

6. Follow the instructions displayed in the program.

---

## Future Improvements

Possible future enhancements include:

- AI-based job recommendation
- Skill gap analysis
- Automatic workload balancing
- Improved UI design

## Work Log

| Date | Member | Location/Branch | Task Description |
| :--- | :--- | :--- | :--- |
| 2026-03-14 | @Jingwei-Xu | Jingwei-Xu/docs_questionnaires | The questionnaire content for investigating user requirements has been uploaded |
| 2026-03-15 | @zhangsan | zhangsan/add_namelist | 你的工作内容描述 |
| 2026-03-18 | @lisi | lisi/ta_login_ui | 你的工作内容描述 |
