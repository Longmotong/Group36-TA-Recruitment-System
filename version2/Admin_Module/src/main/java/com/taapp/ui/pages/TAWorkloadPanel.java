package com.taapp.ui.pages;

import com.taapp.data.AuditLogService;
import com.taapp.data.DataStore;
import com.taapp.model.AssignedPosition;
import com.taapp.model.TA;
import com.taapp.ui.AppLayout;
import com.taapp.ui.Dialogs;
import com.taapp.ui.MainFrame;
import com.taapp.ui.UI;
import com.taapp.ui.components.Card;
import com.taapp.ui.components.Page;
import com.taapp.ui.components.RoundedActionButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TAWorkloadPanel extends Page {
    private static final String SEARCH_PLACEHOLDER = "Search by name or student ID...";

    private final Consumer<String> navigate;
    private final List<WorkRecord> allRecords = new ArrayList<>();
    private List<WorkRecord> filteredRecords = new ArrayList<>();

    private final JPanel viewCards = new JPanel(new java.awt.CardLayout());
    private static final String CARD_LIST = "list";
    private static final String CARD_DETAIL = "detail";

    private final JComboBox<String> program = new JComboBox<>();
    private final JComboBox<String> status = new JComboBox<>(new String[]{"All Status", "Active", "Inactive"});
    private final JTextField search = new JTextField(24);

    private final JLabel totalCount = new JLabel("0");
    private final JLabel activeCount = new JLabel("0");
    private final JLabel totalWorkload = new JLabel("0h");
    private final JLabel avgWorkload = new JLabel("0h");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JPanel detailPanel = new JPanel(new BorderLayout(0, 10));
    private JPanel listHeader;
    private int hoverRow = -1;

    public TAWorkloadPanel(Consumer<String> navigate) {
        super();
        this.navigate = navigate;

        loadRecords();

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(AppLayout.PAGE_INSET_TOP, 0, AppLayout.PAGE_INSET_BOTTOM, 0));

        listHeader = buildHeader();
        root.add(listHeader, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{
                "TA NAME / ID", "PROGRAM", "YEAR", "POSITIONS", "WORKLOAD", "STATUS", "DETAILS"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setAutoCreateRowSorter(true);
        table.setBackground(Color.WHITE);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xF3F4F6));
        table.setSelectionForeground(new Color(0x111827));

        table.getTableHeader().setReorderingAllowed(false);
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UI.moFontBold(12));
                l.setForeground(new Color(0x4B5563));
                l.setBackground(Color.WHITE);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5E7EB)),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
                boolean numericCol = column == 3 || column == 4;
                l.setHorizontalAlignment(numericCol ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return l;
            }
        });
        header.setPreferredSize(new Dimension(0, 42));

        final Color rowAlt = new Color(0xFAFBFC);
        final Color rowLine = new Color(0xF0F2F5);
        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UI.moFontPlain(13));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, rowLine),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                boolean numericCol = column == 3 || column == 4;
                l.setHorizontalAlignment(numericCol ? SwingConstants.RIGHT : SwingConstants.LEFT);
                boolean isHover = row == hoverRow;
                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                } else if (isHover) {
                    l.setBackground(new Color(0xF3F4F6));
                    l.setForeground(new Color(0x111827));
                } else {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : rowAlt);
                    l.setForeground(new Color(0x111827));
                }
                return l;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(baseRenderer);
        }

        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer(rowLine));
        table.getColumnModel().getColumn(6).setCellRenderer(new LinkRenderer(rowAlt, rowLine));

        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                hoverRow = table.rowAtPoint(e.getPoint());
                table.repaint();
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                hoverRow = -1;
                table.repaint();
            }

            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewRow >= 0 && viewCol == 6) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    if (modelRow >= 0 && modelRow < filteredRecords.size()) {
                        showDetailPage(filteredRecords.get(modelRow));
                    }
                }
            }
        });

        viewCards.add(buildListView(), CARD_LIST);
        viewCards.add(detailPanel, CARD_DETAIL);
        root.add(viewCards, BorderLayout.CENTER);

        content().add(AppLayout.wrapCentered(root), BorderLayout.CENTER);

        initFilters();
        bindEvents();
        refresh();
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(UI.createBackToHomeButton(() -> navigate.accept(MainFrame.ROUTE_DASHBOARD)));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JLabel title = new JLabel("TA Workload Management");
        title.setFont(UI.moFontBold(26));
        title.setForeground(UI.palette().text());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("View and manage TA workload distribution across all departments");
        sub.setFont(UI.moFontPlain(14));
        sub.setForeground(UI.palette().textSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(title);
        leftCol.add(Box.createVerticalStrut(6));
        leftCol.add(sub);

        JPanel headerCard = new JPanel(new BorderLayout(28, 0));
        headerCard.setOpaque(true);
        headerCard.setBackground(Color.WHITE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UI.palette().border()),
                new EmptyBorder(18, AppLayout.GUTTER, 20, AppLayout.GUTTER)
        ));
        RoundedActionButton export = new RoundedActionButton("Export CSV", RoundedActionButton.Scheme.ACCENT_BLUE);
        export.setFont(UI.moFontBold(13));
        export.setPreferredSize(new Dimension(200, 42));
        export.addActionListener(e -> exportCsv());

        JPanel east = new JPanel(new BorderLayout());
        east.setOpaque(false);
        east.add(export, BorderLayout.NORTH);

        headerCard.add(leftCol, BorderLayout.CENTER);
        headerCard.add(east, BorderLayout.EAST);

        wrap.add(backRow, BorderLayout.NORTH);
        wrap.add(headerCard, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildListView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(program);
        left.add(status);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(search);

        filterRow.add(left, BorderLayout.WEST);
        filterRow.add(right, BorderLayout.EAST);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 8, 0));
        metrics.setOpaque(false);
        metrics.add(metricCard("Total TAs/MOs", totalCount));
        metrics.add(metricCard("Active Records", activeCount));
        metrics.add(metricCard("Total Workload", totalWorkload));
        metrics.add(metricCard("Avg Workload", avgWorkload));

        Card tableCard = new Card();
        tableCard.setLayout(new BorderLayout());
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setMinimumSize(new Dimension(200, 280));
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(metrics, BorderLayout.NORTH);
        center.add(tableCard, BorderLayout.CENTER);

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private Card metricCard(String label, JLabel value) {
        Card c = new Card();
        c.setLayout(new BorderLayout());
        c.setBackground(new Color(0xF5F5F5));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD4D4D4)),
                BorderFactory.createEmptyBorder(14, 12, 16, 12)
        ));

        JPanel in = new JPanel(new BorderLayout(0, 4));
        in.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(UI.moFontBold(12));
        l.setForeground(new Color(0x404040));

        value.setFont(UI.moFontBold(34));
        value.setForeground(new Color(0x111111));

        in.add(l, BorderLayout.NORTH);
        in.add(value, BorderLayout.CENTER);
        c.add(in, BorderLayout.CENTER);
        return c;
    }

    private void initFilters() {
        program.setPreferredSize(new Dimension(190, 38));
        status.setPreferredSize(new Dimension(150, 38));
        search.setPreferredSize(new Dimension(280, 38));

        UI.styleField(program);
        UI.styleField(status);
        UI.styleField(search);

        program.addItem("All Programs");
        allRecords.stream().map(r -> r.program).filter(s -> s != null && !s.isBlank()).distinct().sorted().forEach(program::addItem);

        search.setText(SEARCH_PLACEHOLDER);
        search.setForeground(new Color(0x9CA3AF));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        search.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(search.getText())) {
                    search.setText("");
                    search.setForeground(UI.palette().text());
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (search.getText().isBlank()) {
                    search.setText(SEARCH_PLACEHOLDER);
                    search.setForeground(new Color(0x9CA3AF));
                }
            }
        });
    }

    private void bindEvents() {
        program.addActionListener(e -> refresh());
        status.addActionListener(e -> refresh());
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });
    }

    private void refresh() {
        String progSel = String.valueOf(program.getSelectedItem());
        String stSel = String.valueOf(status.getSelectedItem());

        String raw = search.getText() == null ? "" : search.getText().trim();
        String q = SEARCH_PLACEHOLDER.equals(raw) ? "" : raw.toLowerCase(Locale.ROOT);

        filteredRecords = allRecords.stream().filter(r -> {
            boolean progOk = "All Programs".equals(progSel) || progSel.equalsIgnoreCase(r.program);
            boolean stOk = "All Status".equals(stSel) || stSel.equalsIgnoreCase(cap(r.status));
            boolean searchOk = q.isBlank()
                    || r.name.toLowerCase(Locale.ROOT).contains(q)
                    || r.personId.toLowerCase(Locale.ROOT).contains(q);
            return progOk && stOk && searchOk;
        }).collect(Collectors.toList());

        tableModel.setRowCount(0);
        int active = 0;
        int total = 0;
        for (WorkRecord r : filteredRecords) {
            if ("active".equalsIgnoreCase(r.status)) active++;
            total += r.workloadHours;
            String nameId = "<html><div style='font-family:sans-serif'><b>" + r.name + "</b><br/><span style='color:#6B7280;font-size:10px;'>" + r.personId + " (" + r.role + ")</span></div></html>";

            tableModel.addRow(new Object[]{
                    nameId,
                    r.program,
                    r.year,
                    r.positionsCount,
                    r.workloadHours + " hours",
                    cap(r.status),
                    "View"
            });
        }

        totalCount.setText(String.valueOf(filteredRecords.size()));
        activeCount.setText(String.valueOf(active));
        totalWorkload.setText(total + "h");
        avgWorkload.setText(filteredRecords.isEmpty() ? "0h" : Math.round(total * 1f / filteredRecords.size()) + "h");
    }

    private void showDetailPage(WorkRecord r) {
        detailPanel.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        JButton back = new JButton("← Back to TA Workload");
        UI.styleBackButton(back);
        back.addActionListener(e -> {
            if (listHeader != null) listHeader.setVisible(true);
            ((java.awt.CardLayout) viewCards.getLayout()).show(viewCards, CARD_LIST);
        });

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(back);

        JLabel title = new JLabel("TA Detail: " + r.name);
        title.setFont(UI.moFontBold(26));

        JPanel titleWrap = new JPanel(new BorderLayout(0, 8));
        titleWrap.setOpaque(false);
        titleWrap.add(backRow, BorderLayout.NORTH);
        titleWrap.add(title, BorderLayout.CENTER);

        Card info = new Card();
        info.setLayout(new GridLayout(0, 2, 10, 8));
        info.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        info.add(labelKey("Name:")); info.add(labelVal(r.name));
        info.add(labelKey("Student ID:")); info.add(labelVal(r.personId));
        info.add(labelKey("TA ID:")); info.add(labelVal("TA".equalsIgnoreCase(r.role) ? "ta" + r.personId.replaceAll("\\D", "") : r.personId));
        info.add(labelKey("Email:")); info.add(labelVal(r.email));
        info.add(labelKey("Program:")); info.add(labelVal(r.program));
        info.add(labelKey("Year:")); info.add(labelVal(r.year));
        info.add(labelKey("Status:")); info.add(labelVal(cap(r.status)));
        info.add(labelKey("Assigned Positions:")); info.add(labelVal(String.valueOf(r.positionsCount)));
        info.add(labelKey("Total Workload:")); info.add(labelVal(r.workloadHours + " hours"));

        DefaultTableModel m = new DefaultTableModel(new Object[]{"Position", "Course", "Department", "Hours", "Period", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (AssignedPosition p : r.records) {
            String period = (p.getStartDate().isBlank() && p.getEndDate().isBlank()) ? "-" : p.getStartDate() + " to " + p.getEndDate();
            m.addRow(new Object[]{
                    p.getPositionTitle(),
                    p.getCourse(),
                    p.getDepartment(),
                    p.getHours() + "h",
                    period,
                    cap(p.getStatus())
            });
        }
        if (r.records.isEmpty()) {
            m.addRow(new Object[]{"No assigned positions", "-", "-", "-", "-", "-"});
        }

        JTable detailTable = new JTable(m);
        detailTable.setRowHeight(24);
        detailTable.getTableHeader().setReorderingAllowed(false);

        Card assigned = new Card();
        assigned.setLayout(new BorderLayout(0, 8));
        assigned.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel assignedTitle = new JLabel("Assigned Positions");
        assignedTitle.setFont(UI.moFontBold(18));
        assigned.add(assignedTitle, BorderLayout.NORTH);
        assigned.add(new JScrollPane(detailTable), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(info, BorderLayout.NORTH);

        JPanel centerStack = new JPanel();
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setOpaque(false);

        if (r.workloadHours > 150) {
            JPanel warning = new JPanel(new BorderLayout());
            warning.setOpaque(true);
            warning.setBackground(new Color(0xFEF3C7));
            warning.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xF59E0B), 1, true),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            JLabel warningText = new JLabel("Warning: workload is too high. Please rebalance assignments.");
            warningText.setFont(UI.moFontBold(13));
            warningText.setForeground(new Color(0x92400E));
            warning.add(warningText, BorderLayout.CENTER);
            warning.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerStack.add(warning);
            centerStack.add(Box.createVerticalStrut(10));
        }

        assigned.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerStack.add(assigned);

        body.add(centerStack, BorderLayout.CENTER);

        root.add(titleWrap, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);

        detailPanel.add(root, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
        if (listHeader != null) listHeader.setVisible(false);
        ((java.awt.CardLayout) viewCards.getLayout()).show(viewCards, CARD_DETAIL);

        AuditLogService.log("admin", "VIEW_PROFILE", r.personId, "Open detail page");
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export workload records");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        chooser.setSelectedFile(new File("workload_records_" + ts + ".csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File out = chooser.getSelectedFile();
        if (!out.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            out = new File(out.getAbsolutePath() + ".csv");
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(out))) {
            w.write("Name,PersonId,Role,Program,Year,Status,Positions,WorkloadHours,Email");
            w.newLine();
            for (WorkRecord r : filteredRecords) {
                w.write(csv(r.name)); w.write(',');
                w.write(csv(r.personId)); w.write(',');
                w.write(csv(r.role)); w.write(',');
                w.write(csv(r.program)); w.write(',');
                w.write(csv(r.year)); w.write(',');
                w.write(csv(cap(r.status))); w.write(',');
                w.write(String.valueOf(r.positionsCount)); w.write(',');
                w.write(String.valueOf(r.workloadHours)); w.write(',');
                w.write(csv(r.email));
                w.newLine();
            }
            AuditLogService.log("admin", "EXPORT_CSV", "WORKLOAD", "Exported rows=" + filteredRecords.size());
            Dialogs.showMessage(this, "Export Complete", "Saved to:\n" + out.getAbsolutePath(), SwingConstants.LEFT);
        } catch (IOException ex) {
            Dialogs.showMessage(this, "Export Failed", ex.getMessage(), SwingConstants.LEFT);
        }
    }

    private void loadRecords() {
        List<TA> tas = DataStore.defaultStore().getTAs();
        for (TA ta : tas) {
            allRecords.add(new WorkRecord(
                    ta.getName(),
                    ta.getStudentId(),
                    "TA",
                    ta.getProgram(),
                    ta.getYear(),
                    ta.getAssignedPositions(),
                    ta.getTotalWorkload(),
                    ta.getStatus(),
                    ta.getEmail(),
                    ta.getPositions()
            ));
        }

        allRecords.add(new WorkRecord(
                "Lin Mo Han", "MO2024001", "MO", "Academic Affairs", "Year 4", 4, 140, "active", "lin.mohan@university.edu",
                List.of(
                        new AssignedPosition("mo1", "Schedule Coordination", "Timetable Planning", "Academic Affairs", 40, "2024-09-01", "2024-10-31", "active"),
                        new AssignedPosition("mo2", "Recruitment Coordination", "TA Hiring Cycle", "Academic Affairs", 30, "2024-11-01", "2024-12-15", "active"),
                        new AssignedPosition("mo3", "Archive Maintenance", "Profile Archiving", "Academic Affairs", 35, "2024-09-15", "2024-12-20", "active"),
                        new AssignedPosition("mo4", "Compliance Report", "Semester Audit", "Academic Affairs", 35, "2024-12-01", "2024-12-31", "active")
                )
        ));

        allRecords.add(new WorkRecord(
                "Qiu Wen Jie", "MO2024002", "MO", "Computer Science", "Year 3", 2, 70, "active", "qiu.wenjie@university.edu",
                List.of(
                        new AssignedPosition("mo5", "Lab Resource Dispatch", "CS Lab Ops", "Computer Science", 30, "2024-09-01", "2024-11-15", "active"),
                        new AssignedPosition("mo6", "Issue Ticket Handling", "TA Support Desk", "Computer Science", 40, "2024-10-01", "2024-12-20", "active")
                )
        ));

        allRecords.add(new WorkRecord(
                "Sun Yi Lan", "MO2024003", "MO", "Business Administration", "Year 2", 0, 0, "inactive", "sun.yilan@university.edu", List.of()
        ));
    }

    private static JLabel labelKey(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UI.moFontBold(13));
        l.setForeground(new Color(0x374151));
        return l;
    }

    private static JLabel labelVal(String text) {
        JLabel l = new JLabel(text == null ? "-" : text);
        l.setFont(UI.moFontPlain(13));
        l.setForeground(new Color(0x111827));
        return l;
    }

    private static String cap(String s) {
        if (s == null || s.isBlank()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String csv(String s) {
        String v = s == null ? "" : s;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private static final class WorkRecord {
        final String name;
        final String personId;
        final String role;
        final String program;
        final String year;
        final int positionsCount;
        final int workloadHours;
        final String status;
        final String email;
        final List<AssignedPosition> records;

        WorkRecord(String name, String personId, String role, String program, String year,
                   int positionsCount, int workloadHours, String status, String email, List<AssignedPosition> records) {
            this.name = name;
            this.personId = personId;
            this.role = role;
            this.program = program;
            this.year = year;
            this.positionsCount = positionsCount;
            this.workloadHours = workloadHours;
            this.status = status;
            this.email = email;
            this.records = records == null ? List.of() : List.copyOf(records);
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        private final Color rowLine;

        StatusRenderer(Color rowLine) {
            this.rowLine = rowLine;
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String v = String.valueOf(value);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(UI.moFontBold(11));
            l.setOpaque(true);
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, rowLine),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            boolean isHover = row == hoverRow;
            if (isSelected) {
                l.setBackground(table.getSelectionBackground());
                l.setForeground(table.getSelectionForeground());
                l.setText("Active".equalsIgnoreCase(v) ? "Active" : "Inactive");
                return l;
            }
            if (isHover) {
                l.setBackground(new Color(0xF3F4F6));
                l.setForeground(new Color(0x111827));
                l.setText("Active".equalsIgnoreCase(v) ? "Active" : "Inactive");
                return l;
            }

            if ("Active".equalsIgnoreCase(v)) {
                l.setBackground(new Color(0xDCFCE7));
                l.setForeground(new Color(0x166534));
                l.setText("Active");
            } else {
                l.setBackground(new Color(0xFEE2E2));
                l.setForeground(new Color(0x991B1B));
                l.setText("Inactive");
            }
            return l;
        }
    }

    private class LinkRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final Color rowAlt;
        private final RoundedActionButton button = new RoundedActionButton("View", RoundedActionButton.Scheme.PRIMARY_BLACK);

        LinkRenderer(Color rowAlt, Color rowLine) {
            this.rowAlt = rowAlt;
            setLayout(new java.awt.GridBagLayout());
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, rowLine));
            button.setFocusable(false);
            button.setPreferredSize(new Dimension(56, 28));
            button.setFont(UI.moFontBold(11));
            add(button);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean isHover = row == hoverRow;
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else if (isHover) {
                setBackground(new Color(0xF3F4F6));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : rowAlt);
            }
            return this;
        }
    }
}
