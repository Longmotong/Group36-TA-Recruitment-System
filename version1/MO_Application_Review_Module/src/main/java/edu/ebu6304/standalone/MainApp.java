package edu.ebu6304.standalone;

import edu.ebu6304.standalone.model.ApplicationItem;
import edu.ebu6304.standalone.model.DashboardMetrics;
import edu.ebu6304.standalone.service.DataImportService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class MainApp extends javafx.application.Application {
    private final DataImportService dataService = new DataImportService(Path.of("..", "data"));
    private final ObservableList<ApplicationItem> applicationTableData = FXCollections.observableArrayList();

    private List<ApplicationItem> allApplications;
    private DashboardMetrics metrics;

    private BorderPane root;
    private BorderPane mainFrame;

    private Button navHomeBtn;
    private Button navJobBtn;
    private Button navReviewBtn;

    private Label managedJobsLabel;
    private Label totalAppsLabel;
    private Label pendingLabel;
    private Label approvedLabel;
    private Label rejectedLabel;

    private TextField searchField;
    private ComboBox<String> courseFilter;
    private ComboBox<String> statusFilter;

    @Override
    public void start(Stage stage) {
        loadData();

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f6f8fb;");

        mainFrame = new BorderPane();
        mainFrame.setTop(buildTopNav());
        mainFrame.setCenter(buildDashboardView());

        root.setCenter(mainFrame);

        Scene scene = new Scene(root, 1320, 840);
        stage.setTitle("TA Management Stand-alone Application");
        stage.setScene(scene);
        stage.show();
    }

    private void loadData() {
        allApplications = dataService.loadApplications();
        metrics = dataService.buildDashboardMetrics("u_mo_001", allApplications);
        applicationTableData.setAll(allApplications);
    }

    private VBox buildTopNav() {
        VBox wrapper = new VBox();
        wrapper.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        wrapper.setPadding(new Insets(14, 20, 14, 20));

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("MO System");
        title.setStyle("-fx-font-size: 19px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        navHomeBtn = createTopNavButton("Home");
        navHomeBtn.setOnAction(e -> {
            setActiveNav("home");
            mainFrame.setCenter(buildDashboardView());
        });

        navJobBtn = createTopNavButton("Job Management");
        navJobBtn.setOnAction(e -> {
            setActiveNav("job");
            mainFrame.setCenter(buildJobModulePlaceholder());
        });

        navReviewBtn = createTopNavButton("Application Review");
        navReviewBtn.setOnAction(e -> {
            setActiveNav("review");
            mainFrame.setCenter(buildApplicationReviewView());
        });

        row.getChildren().addAll(title, spacer, navHomeBtn, navJobBtn, navReviewBtn);
        wrapper.getChildren().add(row);

        setActiveNav("home");
        return wrapper;
    }

    private Button createTopNavButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 9; -fx-background-radius: 9; -fx-padding: 10 18; -fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #374151;");
        return btn;
    }

    private void setActiveNav(String key) {
        setButtonActive(navHomeBtn, "home".equals(key));
        setButtonActive(navJobBtn, "job".equals(key));
        setButtonActive(navReviewBtn, "review".equals(key));
    }

    private void setButtonActive(Button button, boolean active) {
        if (button == null) return;
        if (active) {
            button.setStyle("-fx-background-color: #111827; -fx-text-fill: white; -fx-border-color: #111827; -fx-border-radius: 9; -fx-background-radius: 9; -fx-padding: 10 18; -fx-font-size: 14px; -fx-font-weight: 800;");
        } else {
            button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 9; -fx-background-radius: 9; -fx-padding: 10 18; -fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #374151;");
        }
    }

    private VBox buildDashboardView() {
        VBox page = createPageContainer();
        page.setSpacing(30);

        Label header = new Label("Dashboard");
        header.setStyle("-fx-font-size: 36px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Label subtitle = new Label("Overview for current recruitment cycle (from real data)");
        subtitle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 15px;");

        VBox headText = new VBox(4, header, subtitle);
        Button logoutBtn = ghostButton("Log out");
        logoutBtn.setPrefHeight(42);
        logoutBtn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #b91c1c; -fx-font-size: 14px; -fx-font-weight: 700; -fx-border-color: #fca5a5; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 8 14;");
        logoutBtn.setOnAction(e -> mainFrame.setCenter(buildLoggedOutView()));

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(headText, headerSpacer, logoutBtn);

        HBox moduleRow = new HBox(28);
        VBox jobCard = buildJobManagementCard();
        VBox reviewCard = buildApplicationReviewCard();
        HBox.setHgrow(jobCard, Priority.ALWAYS);
        HBox.setHgrow(reviewCard, Priority.ALWAYS);
        jobCard.setMaxWidth(Double.MAX_VALUE);
        reviewCard.setMaxWidth(Double.MAX_VALUE);
        moduleRow.getChildren().addAll(jobCard, reviewCard);

        HBox quickOverview = new HBox(28);
        VBox card1 = smallCard("Managed Jobs", String.valueOf(metrics.managedJobs()), "#111827");
        VBox card2 = smallCard("Total Applications", String.valueOf(metrics.totalApplications()), "#2563eb");
        VBox card3 = smallCard("Pending Reviews", String.valueOf(metrics.pendingReviews()), "#ca8a04");
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        card1.setMaxWidth(Double.MAX_VALUE);
        card2.setMaxWidth(Double.MAX_VALUE);
        card3.setMaxWidth(Double.MAX_VALUE);
        quickOverview.getChildren().addAll(card1, card2, card3);

        Region dashboardSpacer = new Region();
        dashboardSpacer.setMinHeight(2);

        Label quickTitle = sectionTitle("Quick Overview");
        quickTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Region bottomWhitespace = new Region();
        bottomWhitespace.setMinHeight(28);

        page.getChildren().addAll(headerRow, moduleRow, dashboardSpacer, quickTitle, quickOverview, bottomWhitespace);
        return page;
    }

    private VBox buildJobManagementCard() {
        VBox moduleCard = new VBox(18);
        moduleCard.setPadding(new Insets(30));
        moduleCard.setPrefWidth(620);
        moduleCard.setMinHeight(330);
        moduleCard.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 14; -fx-background-radius: 14;");

        Label moduleTitle = new Label("Job Management Module");
        moduleTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        VBox moduleDesc = new VBox(6);

        Label moduleDescMain = new Label("Manage course info, requirements, and job postings");
        moduleDescMain.setWrapText(true);
        moduleDescMain.setStyle("-fx-text-fill: #374151; -fx-font-size: 17px; -fx-font-weight: 700;");

        Label moduleDescBullets = new Label("""
                · Create and edit course information
                · Set TA requirements and qualifications
                · Post and manage job openings
                """);
        moduleDescBullets.setWrapText(true);
        moduleDescBullets.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 15px; -fx-line-spacing: 4;");

        moduleDesc.getChildren().addAll(moduleDescMain, moduleDescBullets);

        managedJobsLabel = new Label("Managed Jobs: " + metrics.managedJobs());
        managedJobsLabel.setStyle("-fx-font-weight: 800; -fx-text-fill: #111827; -fx-font-size: 20px;");

        Region pushToBottom = new Region();
        VBox.setVgrow(pushToBottom, Priority.ALWAYS);

        Button open = ghostButton("Go to Job Management");
        open.setMaxWidth(Double.MAX_VALUE);
        open.setPrefHeight(48);
        open.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #374151; -fx-font-size: 15px; -fx-font-weight: 700; -fx-border-color: #d1d5db; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 10 16;");
        open.setOnAction(e -> {
            setActiveNav("job");
            mainFrame.setCenter(buildJobModulePlaceholder());
        });

        moduleCard.getChildren().addAll(moduleTitle, moduleDesc, managedJobsLabel, pushToBottom, open);
        return moduleCard;
    }

    private VBox buildApplicationReviewCard() {
        VBox moduleCard = new VBox(18);
        moduleCard.setPadding(new Insets(30));
        moduleCard.setPrefWidth(620);
        moduleCard.setMinHeight(330);
        moduleCard.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 14; -fx-background-radius: 14;");

        Label moduleTitle = new Label("Application Review Module");
        moduleTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        VBox moduleDesc = new VBox(6);

        Label moduleDescMain = new Label("View TA applications, review, and check allocation results");
        moduleDescMain.setWrapText(true);
        moduleDescMain.setStyle("-fx-text-fill: #374151; -fx-font-size: 17px; -fx-font-weight: 700;");

        Label moduleDescBullets = new Label("""
                · Browse and filter TA applications
                · Review and approve/reject applications
                · View TA allocation results
                """);
        moduleDescBullets.setWrapText(true);
        moduleDescBullets.setStyle("-fx-text-fill: #4b5563; -fx-font-size: 15px; -fx-line-spacing: 4;");

        moduleDesc.getChildren().addAll(moduleDescMain, moduleDescBullets);

        HBox counters = new HBox(12);
        totalAppsLabel = metricPill("Total " + metrics.totalApplications(), "#dbeafe", "#1e40af");
        pendingLabel = metricPill("Pending " + metrics.pendingReviews(), "#fef3c7", "#92400e");
        approvedLabel = metricPill("Approved " + metrics.approvedCount(), "#dcfce7", "#166534");
        rejectedLabel = metricPill("Rejected " + metrics.rejectedCount(), "#fee2e2", "#991b1b");
        counters.getChildren().addAll(totalAppsLabel, pendingLabel, approvedLabel, rejectedLabel);

        HBox actions = new HBox(12);
        Button openReview = primaryButton("Go to Application Review");
        openReview.setMaxWidth(Double.MAX_VALUE);
        openReview.setPrefHeight(48);
        openReview.setStyle("-fx-background-color: #111827; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 16;");
        openReview.setOnAction(e -> {
            setActiveNav("review");
            mainFrame.setCenter(buildApplicationReviewView());
        });

        Button viewRecords = ghostButton("My Review Records");
        viewRecords.setMaxWidth(Double.MAX_VALUE);
        viewRecords.setPrefHeight(48);
        viewRecords.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #374151; -fx-font-size: 15px; -fx-font-weight: 700; -fx-border-color: #d1d5db; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 10 16;");
        viewRecords.setOnAction(e -> {
            setActiveNav("review");
            mainFrame.setCenter(buildMyReviewRecordsView());
        });

        HBox.setHgrow(openReview, Priority.ALWAYS);
        HBox.setHgrow(viewRecords, Priority.ALWAYS);
        actions.getChildren().addAll(openReview, viewRecords);

        Region pushToBottom = new Region();
        VBox.setVgrow(pushToBottom, Priority.ALWAYS);

        moduleCard.getChildren().addAll(moduleTitle, moduleDesc, counters, pushToBottom, actions);
        return moduleCard;
    }

    private Label metricPill(String text, String bg, String fg) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-background-radius: 999; -fx-padding: 8 14; -fx-font-size: 14px; -fx-font-weight: 800;");
        return label;
    }

    private VBox buildApplicationReviewView() {
        VBox page = createPageContainer();

        HBox topBar = new HBox(10);
        Button backHomeBtn = ghostButton("← Back to Home");
        backHomeBtn.setOnAction(e -> {
            setActiveNav("home");
            mainFrame.setCenter(buildDashboardView());
        });
        topBar.getChildren().add(backHomeBtn);

        Label header = new Label("TA Applications");
        header.setStyle("-fx-font-size: 30px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        Label subtitle = new Label("Review and manage Teaching Assistant applications");
        subtitle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

        HBox summary = buildReviewSummaryCards();

        HBox filters = new HBox(10);
        filters.setPadding(new Insets(14));
        filters.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12;");

        searchField = new TextField();
        searchField.setPromptText("Search by applicant name or student ID");
        searchField.setPrefWidth(320);

        courseFilter = new ComboBox<>();
        courseFilter.getItems().addAll("", "CS101", "CS201", "MATH205");
        courseFilter.setValue("");
        courseFilter.setPromptText("Course");
        courseFilter.setPrefWidth(170);

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("", "pending", "approved", "rejected");
        statusFilter.setValue("");
        statusFilter.setPromptText("Status");
        statusFilter.setPrefWidth(170);

        Button filterBtn = primaryButton("Apply Filter");
        filterBtn.setOnAction(e -> applyFilters());

        Button resetBtn = ghostButton("Reset");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            courseFilter.setValue("");
            statusFilter.setValue("");
            applicationTableData.setAll(allApplications);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button recordsBtn = ghostButton("My Review Records");
        recordsBtn.setOnAction(e -> mainFrame.setCenter(buildMyReviewRecordsView()));

        filters.getChildren().addAll(searchField, courseFilter, statusFilter, filterBtn, resetBtn, spacer, recordsBtn);

        TableView<ApplicationItem> table = buildApplicationTable();
        table.setItems(applicationTableData);
        table.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e5e7eb;");

        page.getChildren().addAll(topBar, header, subtitle, summary, filters, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return page;
    }

    private HBox buildReviewSummaryCards() {
        int total = allApplications.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;
        for (ApplicationItem item : allApplications) {
            String status = resolveStatus(item);
            if ("pending".equals(status)) pending++;
            if ("approved".equals(status)) approved++;
            if ("rejected".equals(status)) rejected++;
        }

        HBox row = new HBox(14);
        VBox c1 = smallCard("Total", String.valueOf(total), "#111827");
        VBox c2 = smallCard("Pending", String.valueOf(pending), "#ca8a04");
        VBox c3 = smallCard("Approved", String.valueOf(approved), "#15803d");
        VBox c4 = smallCard("Rejected", String.valueOf(rejected), "#dc2626");

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        HBox.setHgrow(c4, Priority.ALWAYS);
        c1.setMaxWidth(Double.MAX_VALUE);
        c2.setMaxWidth(Double.MAX_VALUE);
        c3.setMaxWidth(Double.MAX_VALUE);
        c4.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().addAll(c1, c2, c3, c4);
        return row;
    }

    private VBox smallCard(String label, String value, String color) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setMinWidth(180);
        card.setPrefHeight(150);
        card.setMinHeight(150);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label l = new Label(label);
        l.setAlignment(Pos.CENTER);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 18px; -fx-font-weight: 800;");

        Label v = new Label(value);
        v.setAlignment(Pos.CENTER);
        v.setMaxWidth(Double.MAX_VALUE);
        v.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 44px; -fx-font-weight: 900;");

        card.getChildren().addAll(l, v);
        return card;
    }

    private TableView<ApplicationItem> buildApplicationTable() {
        TableView<ApplicationItem> table = new TableView<>();

        TableColumn<ApplicationItem, String> appIdCol = new TableColumn<>("Application ID");
        appIdCol.setCellValueFactory(new PropertyValueFactory<>("applicationId"));

        TableColumn<ApplicationItem, String> nameCol = new TableColumn<>("TA Name");
        nameCol.setCellValueFactory(cell -> Bindings.createStringBinding(
                () -> cell.getValue().getApplicantSnapshot() == null ? "" : safe(cell.getValue().getApplicantSnapshot().getFullName())
        ));

        TableColumn<ApplicationItem, String> studentIdCol = new TableColumn<>("Student ID");
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<ApplicationItem, String> courseCol = new TableColumn<>("Applied Course");
        courseCol.setCellValueFactory(cell -> Bindings.createStringBinding(
                () -> cell.getValue().getJobSnapshot() == null ? "" : safe(cell.getValue().getJobSnapshot().getCourseCode())
        ));

        TableColumn<ApplicationItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new SimpleStringProperty(resolveStatus(cell.getValue())));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null || status.isBlank()) {
                    setGraphic(null);
                    return;
                }
                setGraphic(statusBadge(status));
            }
        });

        TableColumn<ApplicationItem, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final HBox box = new HBox(6);
            private final Button detailBtn = iconButton("⌕", "Detail", false);
            private final Button reviewBtn = iconButton("✎", "Review", true);
            private final Button quickApproveBtn = iconButton("✓", "Approve", true);
            private final Button quickRejectBtn = iconButton("✕", "Reject", false);

            {
                detailBtn.setOnAction(e -> {
                    ApplicationItem item = getTableView().getItems().get(getIndex());
                    mainFrame.setCenter(buildApplicationDetailView(item));
                });
                reviewBtn.setOnAction(e -> {
                    ApplicationItem item = getTableView().getItems().get(getIndex());
                    mainFrame.setCenter(buildReviewApplicationView(item));
                });
                quickApproveBtn.setOnAction(e -> {
                    ApplicationItem rowItem = getTableView().getItems().get(getIndex());
                    dataService.submitReview(rowItem, "approved", "Quick approved from list");
                    loadData();
                    refreshDashboardCards();
                    applyFilters();
                });
                quickRejectBtn.setOnAction(e -> {
                    ApplicationItem rowItem = getTableView().getItems().get(getIndex());
                    dataService.submitReview(rowItem, "rejected", "Quick rejected from list");
                    loadData();
                    refreshDashboardCards();
                    applyFilters();
                });
                box.getChildren().addAll(detailBtn, reviewBtn, quickApproveBtn, quickRejectBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                ApplicationItem rowItem = getTableView().getItems().get(getIndex());
                String status = resolveStatus(rowItem);
                quickApproveBtn.setDisable("approved".equals(status));
                quickRejectBtn.setDisable("rejected".equals(status));

                setGraphic(box);
            }
        });

        table.getColumns().add(appIdCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(studentIdCol);
        table.getColumns().add(courseCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    private VBox buildApplicationDetailView(ApplicationItem item) {
        VBox page = createPageContainer();

        HBox head = new HBox(10);
        Button backBtn = ghostButton("← Back to Applications");
        backBtn.setOnAction(e -> mainFrame.setCenter(buildApplicationReviewView()));
        Button toReviewBtn = primaryButton("Go to Review Page");
        toReviewBtn.setOnAction(e -> mainFrame.setCenter(buildReviewApplicationView(item)));
        head.getChildren().addAll(backBtn, toReviewBtn);

        Label title = new Label("TA Application Detail");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        GridPane info = new GridPane();
        info.setPadding(new Insets(16));
        info.setHgap(24);
        info.setVgap(10);
        info.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12;");

        addInfoRow(info, 0, "Applicant", safe(getApplicantName(item)));
        addInfoRow(info, 1, "Student ID", safe(item.getStudentId()));
        addInfoRow(info, 2, "Email", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getEmail()));
        addInfoRow(info, 3, "Phone", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getPhoneNumber()));
        addInfoRow(info, 4, "Major", item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getProgramMajor()));
        addInfoRow(info, 5, "GPA", item.getApplicantSnapshot() == null || item.getApplicantSnapshot().getGpa() == null ? "" : String.valueOf(item.getApplicantSnapshot().getGpa()));
        addInfoRow(info, 6, "Course", getCourseText(item));
        addInfoRow(info, 7, "Current Status", resolveStatus(item));

        VBox skillsBlock = new VBox(8);
        skillsBlock.setPadding(new Insets(16));
        skillsBlock.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12;");
        Label skillsTitle = sectionTitle("Relevant Skills");
        Label skills = new Label(getSkillsText(item));
        skills.setWrapText(true);
        skillsBlock.getChildren().addAll(skillsTitle, skills);

        VBox expBlock = textBlock("Relevant Experience", item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getRelevantExperience()));
        VBox motivationBlock = textBlock("Motivation Cover Letter", item.getApplicationForm() == null ? "" : safe(item.getApplicationForm().getMotivationCoverLetter()));

        page.getChildren().addAll(head, title, info, skillsBlock, expBlock, motivationBlock);
        return page;
    }

    private VBox buildReviewApplicationView(ApplicationItem item) {
        VBox page = createPageContainer();

        HBox top = new HBox(10);
        Button backBtn = ghostButton("← Back to Applications");
        backBtn.setOnAction(e -> mainFrame.setCenter(buildApplicationReviewView()));
        Button detailBtn = iconButton("⌕", "View Detail", false);
        detailBtn.setOnAction(e -> mainFrame.setCenter(buildApplicationDetailView(item)));
        top.getChildren().addAll(backBtn, detailBtn);

        Label title = new Label("Review TA Application");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        VBox summary = new VBox(6);
        summary.setPadding(new Insets(16));
        summary.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12;");
        summary.getChildren().addAll(
                sectionTitle("Application Summary"),
                new Label("Application ID: " + safe(item.getApplicationId())),
                new Label("Applicant: " + safe(getApplicantName(item))),
                new Label("Course: " + getCourseText(item)),
                new Label("Current Status: " + resolveStatus(item))
        );

        VBox decisionCard = new VBox(10);
        decisionCard.setPadding(new Insets(16));
        decisionCard.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12;");

        Label decisionTitle = sectionTitle("Decision");
        ToggleGroup group = new ToggleGroup();
        RadioButton approve = new RadioButton("Approve");
        approve.setUserData("approved");
        approve.setToggleGroup(group);
        approve.setSelected(true);
        RadioButton reject = new RadioButton("Reject");
        reject.setUserData("rejected");
        reject.setToggleGroup(group);

        HBox choice = new HBox(20, approve, reject);
        choice.setAlignment(Pos.CENTER_LEFT);

        Label notesLabel = new Label("Review Notes");
        notesLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #374151;");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter review notes (reason, strengths, concerns...)");
        notesArea.setPrefRowCount(6);
        notesArea.setWrapText(true);

        Button submit = primaryButton("Submit Review Decision");
        submit.setOnAction(e -> {
            String decision = group.getSelectedToggle() == null ? "approved" : String.valueOf(group.getSelectedToggle().getUserData());
            dataService.submitReview(item, decision, notesArea.getText());
            loadData();
            refreshDashboardCards();
            mainFrame.setCenter(buildApplicationReviewView());
        });

        decisionCard.getChildren().addAll(decisionTitle, choice, notesLabel, notesArea, submit);

        page.getChildren().addAll(top, title, summary, decisionCard);
        return page;
    }

    private VBox buildMyReviewRecordsView() {
        VBox page = createPageContainer();

        HBox top = new HBox(10);
        Button backBtn = ghostButton("← Back to Application Review");
        backBtn.setOnAction(e -> mainFrame.setCenter(buildApplicationReviewView()));
        top.getChildren().add(backBtn);

        Label title = new Label("My Review Records");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        Label subtitle = new Label("History derived from real reviewed application data");
        subtitle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");

        ObservableList<ApplicationItem> reviewedList = FXCollections.observableArrayList(
                allApplications.stream().filter(this::isReviewed).toList()
        );

        int approved = (int) reviewedList.stream().filter(a -> "approved".equals(resolveStatus(a))).count();
        int rejected = (int) reviewedList.stream().filter(a -> "rejected".equals(resolveStatus(a))).count();

        HBox stats = new HBox(10,
                smallCard("Total Reviews", String.valueOf(reviewedList.size()), "#111827"),
                smallCard("Approved", String.valueOf(approved), "#15803d"),
                smallCard("Rejected", String.valueOf(rejected), "#dc2626")
        );

        TableView<ApplicationItem> recordsTable = new TableView<>();
        recordsTable.setItems(reviewedList);

        TableColumn<ApplicationItem, String> appId = new TableColumn<>("Application ID");
        appId.setCellValueFactory(new PropertyValueFactory<>("applicationId"));

        TableColumn<ApplicationItem, String> course = new TableColumn<>("Course");
        course.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getJobSnapshot() == null ? "" : safe(cell.getValue().getJobSnapshot().getCourseCode())
        ));

        TableColumn<ApplicationItem, String> taName = new TableColumn<>("TA Name");
        taName.setCellValueFactory(cell -> new SimpleStringProperty(getApplicantName(cell.getValue())));

        TableColumn<ApplicationItem, String> reviewDate = new TableColumn<>("Review Date");
        reviewDate.setCellValueFactory(cell -> new SimpleStringProperty(getReviewDate(cell.getValue())));

        TableColumn<ApplicationItem, String> result = new TableColumn<>("Result");
        result.setCellValueFactory(cell -> new SimpleStringProperty(resolveStatus(cell.getValue())));
        result.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null || status.isBlank()) {
                    setGraphic(null);
                    return;
                }
                setGraphic(statusBadge(status));
            }
        });

        TableColumn<ApplicationItem, String> reviewer = new TableColumn<>("Reviewer");
        reviewer.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getReview() == null ? "" : safe(cell.getValue().getReview().getReviewedBy())
        ));

        TableColumn<ApplicationItem, Void> actions = new TableColumn<>("Actions");
        actions.setCellFactory(col -> new TableCell<>() {
            private final Button detailBtn = iconButton("⌕", "Detail", false);

            {
                detailBtn.setOnAction(e -> {
                    ApplicationItem item = getTableView().getItems().get(getIndex());
                    mainFrame.setCenter(buildApplicationDetailView(item));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : detailBtn);
            }
        });

        recordsTable.getColumns().add(appId);
        recordsTable.getColumns().add(course);
        recordsTable.getColumns().add(taName);
        recordsTable.getColumns().add(reviewDate);
        recordsTable.getColumns().add(result);
        recordsTable.getColumns().add(reviewer);
        recordsTable.getColumns().add(actions);
        recordsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        page.getChildren().addAll(top, title, subtitle, stats, recordsTable);
        VBox.setVgrow(recordsTable, Priority.ALWAYS);
        return page;
    }

    private VBox buildLoggedOutView() {
        VBox page = createPageContainer();
        page.setAlignment(Pos.CENTER);
        page.setSpacing(14);

        Label title = new Label("Logged out");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        Label desc = new Label("You have logged out of MO account.");
        desc.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 15px;");

        Button loginAgain = primaryButton("Back to Dashboard");
        loginAgain.setPrefHeight(46);
        loginAgain.setOnAction(e -> {
            setActiveNav("home");
            mainFrame.setCenter(buildDashboardView());
        });

        page.getChildren().addAll(title, desc, loginAgain);
        return page;
    }

    private VBox buildJobModulePlaceholder() {
        VBox page = createPageContainer();
        Label title = new Label("Job Management Module");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        Label desc = new Label("Not in current scope. As requested, this module is not implemented yet.");
        desc.setStyle("-fx-text-fill: #6b7280;");
        page.getChildren().addAll(title, desc);
        return page;
    }

    private VBox createPageContainer() {
        VBox page = new VBox(16);
        page.setPadding(new Insets(24));
        return page;
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #111827; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 8 14;");
        return b;
    }

    private Button ghostButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: white; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-border-color: #d1d5db; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 8 14;");
        return b;
    }

    private Button iconButton(String icon, String label, boolean primary) {
        Button b = new Button(icon + "  " + label);
        if (primary) {
            b.setStyle("-fx-background-color: #111827; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 5 10; -fx-font-size: 11px;");
        } else {
            b.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #374151; -fx-font-weight: 700; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5 10; -fx-font-size: 11px;");
        }
        return b;
    }

    private Label statusBadge(String status) {
        String normalized = status.toLowerCase(Locale.ROOT);
        String bg = "#e5e7eb";
        String fg = "#374151";
        String text = normalized;

        switch (normalized) {
            case "pending" -> {
                bg = "#fef3c7";
                fg = "#92400e";
                text = "Pending";
            }
            case "approved" -> {
                bg = "#dcfce7";
                fg = "#166534";
                text = "Approved";
            }
            case "rejected" -> {
                bg = "#fee2e2";
                fg = "#991b1b";
                text = "Rejected";
            }
            default -> {
            }
        }

        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-background-radius: 999; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: 700;");
        return badge;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        return l;
    }

    private VBox textBlock(String title, String content) {
        VBox block = new VBox(8);
        block.setPadding(new Insets(16));
        block.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-background-radius: 12;");
        Label t = sectionTitle(title);
        TextArea area = new TextArea(content);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(4);
        block.getChildren().addAll(t, area);
        return block;
    }

    private void addInfoRow(GridPane grid, int row, String key, String value) {
        Label k = new Label(key + ":");
        k.setStyle("-fx-text-fill: #6b7280; -fx-font-weight: 600;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #111827; -fx-font-weight: 500;");
        grid.add(k, 0, row);
        grid.add(v, 1, row);
    }

    private String resolveStatus(ApplicationItem item) {
        String status = item.getStatus() == null ? "" : safe(item.getStatus().getCurrent()).toLowerCase(Locale.ROOT);
        if ("under_review".equals(status)) return "pending";
        if (status.isBlank() && item.getReview() != null) {
            String decision = safe(item.getReview().getDecision()).toLowerCase(Locale.ROOT);
            if ("approved".equals(decision) || "rejected".equals(decision)) return decision;
            return "pending";
        }
        return status.isBlank() ? "pending" : status;
    }

    private boolean isReviewed(ApplicationItem item) {
        String status = resolveStatus(item);
        if ("approved".equals(status) || "rejected".equals(status)) return true;
        if (item.getReview() == null) return false;
        String decision = safe(item.getReview().getDecision()).toLowerCase(Locale.ROOT);
        return "approved".equals(decision) || "rejected".equals(decision);
    }

    private String getReviewDate(ApplicationItem item) {
        if (item.getReview() != null && !safe(item.getReview().getReviewedAt()).isBlank()) {
            return item.getReview().getReviewedAt();
        }
        if (item.getMeta() != null && !safe(item.getMeta().getUpdatedAt()).isBlank()) {
            return item.getMeta().getUpdatedAt();
        }
        return "";
    }

    private String getApplicantName(ApplicationItem item) {
        return item.getApplicantSnapshot() == null ? "" : safe(item.getApplicantSnapshot().getFullName());
    }

    private String getCourseText(ApplicationItem item) {
        if (item.getJobSnapshot() == null) return "";
        return safe(item.getJobSnapshot().getCourseCode()) + " - " + safe(item.getJobSnapshot().getCourseName());
    }

    private String getSkillsText(ApplicationItem item) {
        if (item.getApplicationForm() == null || item.getApplicationForm().getRelevantSkills() == null) {
            return "";
        }
        return String.join(", ", item.getApplicationForm().getRelevantSkills());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void applyFilters() {
        List<ApplicationItem> filtered = dataService.filterApplications(
                allApplications,
                searchField.getText(),
                courseFilter.getValue(),
                statusFilter.getValue()
        );
        applicationTableData.setAll(filtered);
    }

    private void refreshDashboardCards() {
        metrics = dataService.buildDashboardMetrics("u_mo_001", allApplications);
        if (managedJobsLabel != null) managedJobsLabel.setText("Managed Jobs: " + metrics.managedJobs());
        if (totalAppsLabel != null) totalAppsLabel.setText("Total " + metrics.totalApplications());
        if (pendingLabel != null) pendingLabel.setText("Pending " + metrics.pendingReviews());
        if (approvedLabel != null) approvedLabel.setText("Approved " + metrics.approvedCount());
        if (rejectedLabel != null) rejectedLabel.setText("Rejected " + metrics.rejectedCount());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
