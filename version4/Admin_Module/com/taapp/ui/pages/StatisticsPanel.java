package Admin_Module.com.taapp.ui.pages;

import TA_Job_Application_Module.pages.jobs.JobsPortalUi;

import Admin_Module.com.taapp.data.DataStore;
import Admin_Module.com.taapp.model.Position;
import Admin_Module.com.taapp.model.TA;
import Admin_Module.com.taapp.ui.AppLayout;
import Admin_Module.com.taapp.ui.MainFrame;
import Admin_Module.com.taapp.ui.UI;
import Admin_Module.com.taapp.ui.components.Card;
import Admin_Module.com.taapp.ui.components.Page;
import Admin_Module.com.taapp.ui.components.RoundedActionButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StatisticsPanel extends Page {
    private static final String CARD_LIST = "list";
    private static final String CARD_DETAIL = "detail";

    private final List<PositionProgress> progressList;
    private List<PositionProgress> filteredList = new ArrayList<>();

    private final JPanel viewCards = new JPanel(new CardLayout());
    private final JPanel detailPanel = new JPanel(new BorderLayout());
    private JPanel listHeader;
    private int hoverRow = -1;

    private final JComboBox<String> positionFilter = new JComboBox<>();
    private final JComboBox<String> departmentFilter = new JComboBox<>();
    private DefaultTableModel tableModel;
    private JTable table;

    public StatisticsPanel(Consumer<String> navigate) {
        super();
        this.progressList = buildProgressData(DataStore.defaultStore().getPositions());
        this.filteredList = new ArrayList<>(this.progressList);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(AppLayout.PAGE_INSET_TOP, 0, AppLayout.PAGE_INSET_BOTTOM, 0));

        listHeader = buildHeader(navigate);
        root.add(listHeader, BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);

        content().add(AppLayout.wrapCentered(root), BorderLayout.CENTER);

        initFilters();
        bindFilterEvents();
        refreshTable();
    }

    private JPanel buildHeader(Consumer<String> navigate) {
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(UI.createBackToHomeButton(() -> navigate.accept(MainFrame.ROUTE_DASHBOARD)));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JLabel h1 = new JLabel("Recruitment Progress Monitor");
        h1.setFont(UI.moFontBold(26));
        h1.setForeground(UI.palette().text());
        h1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Track live hiring status, application flow, screening progress, and completion risk across positions");
        sub.setFont(UI.moFontPlain(14));
        sub.setForeground(UI.palette().textSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(h1);
        leftCol.add(Box.createVerticalStrut(6));
        leftCol.add(sub);

        JPanel headerCard = new JPanel(new BorderLayout(0, 0));
        headerCard.setOpaque(true);
        headerCard.setBackground(Color.WHITE);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER),
                new EmptyBorder(18, 22, 18, 22)
        ));
        headerCard.add(leftCol, BorderLayout.CENTER);

        header.add(backRow, BorderLayout.NORTH);
        header.add(headerCard, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        viewCards.setOpaque(false);
        detailPanel.setOpaque(false);

        viewCards.add(buildListView(), CARD_LIST);
        viewCards.add(detailPanel, CARD_DETAIL);

        center.add(viewCards, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildListView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(buildProgressTable(), BorderLayout.CENTER);
        panel.add(buildWorkloadInsights(), BorderLayout.SOUTH);
        return panel;
    }

    private Card buildWorkloadInsights() {
        WorkloadInsights insights = collectWorkloadInsights();

        Card card = new Card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel secHead = new JPanel(new BorderLayout(0, 0));
        secHead.setOpaque(false);
        secHead.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0x059669)),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JLabel title = new JLabel("TA Workload Insights");
        title.setFont(UI.moFontBold(17));
        title.setForeground(UI.palette().text());
        secHead.add(title, BorderLayout.CENTER);
        card.add(secHead, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(1, 3, 10, 0));
        charts.setOpaque(false);
        charts.add(buildTopTaWorkloadCard(insights));
        charts.add(buildWorkloadBucketCard(insights));
        charts.add(buildProgramHoursCard(insights));
        card.add(charts, BorderLayout.CENTER);
        return card;
    }

    private Card buildTopTaWorkloadCard(WorkloadInsights i) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 8));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel t = new JLabel("Top TA Workload");
        t.setFont(UI.moFontBold(13));
        t.setForeground(JobsPortalUi.TEXT_GRAY);
        c.add(t, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3, 1, 0, 6));
        rows.setOpaque(false);
        int max = Math.max(1, i.topTaHours.stream().mapToInt(Map.Entry::getValue).max().orElse(1));
        List<Map.Entry<String, Integer>> top = new ArrayList<>(i.topTaHours);
        while (top.size() < 3) {
            top.add(Map.entry("-", 0));
        }
        for (Map.Entry<String, Integer> e : top) {
            rows.add(bucketRow(e.getKey(), e.getValue(), max, new Color(0x2563EB)));
        }
        c.add(rows, BorderLayout.CENTER);
        return c;
    }

    private Card buildWorkloadBucketCard(WorkloadInsights i) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 8));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel t = new JLabel("Weekly Hours Distribution");
        t.setFont(UI.moFontBold(13));
        t.setForeground(JobsPortalUi.TEXT_GRAY);
        c.add(t, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3, 1, 0, 6));
        rows.setOpaque(false);
        int max = Math.max(1, Math.max(i.lightHoursCount, Math.max(i.mediumHoursCount, i.heavyHoursCount)));
        rows.add(bucketRow("0-5h", i.lightHoursCount, max, new Color(0x60A5FA)));
        rows.add(bucketRow("6-10h", i.mediumHoursCount, max, new Color(0x2563EB)));
        rows.add(bucketRow("11h+", i.heavyHoursCount, max, new Color(0x1D4ED8)));
        c.add(rows, BorderLayout.CENTER);
        return c;
    }

    private Card buildProgramHoursCard(WorkloadInsights i) {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 8));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel t = new JLabel("Top Programs by Workload");
        t.setFont(UI.moFontBold(13));
        t.setForeground(JobsPortalUi.TEXT_GRAY);
        c.add(t, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3, 1, 0, 6));
        rows.setOpaque(false);

        List<Map.Entry<String, Integer>> top = i.programHours.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .collect(Collectors.toList());
        while (top.size() < 3) {
            top.add(Map.entry("-", 0));
        }
        int max = Math.max(1, top.stream().mapToInt(Map.Entry::getValue).max().orElse(1));
        for (Map.Entry<String, Integer> e : top) {
            rows.add(bucketRow(e.getKey(), e.getValue(), max, new Color(0x0EA5E9)));
        }
        c.add(rows, BorderLayout.CENTER);
        return c;
    }

    private JPanel bucketRow(String label, int value, int maxValue, Color color) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(UI.moFontPlain(12));
        l.setForeground(JobsPortalUi.MUTED_TEXT);
        JLabel v = new JLabel(String.valueOf(value));
        v.setFont(UI.moFontBold(12));
        v.setForeground(JobsPortalUi.DARK_TEXT);
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(l, BorderLayout.WEST);
        row.add(new SimpleBar(value, maxValue, color), BorderLayout.CENTER);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private WorkloadInsights collectWorkloadInsights() {
        List<TA> tas = DataStore.defaultStore().getTAs();
        int light = 0;
        int medium = 0;
        int heavy = 0;
        Map<String, Integer> programHours = new HashMap<>();
        List<Map.Entry<String, Integer>> taHours = new ArrayList<>();
        for (TA ta : tas) {
            int hours = ta.getTotalWorkload();
            if (hours <= 5) light++;
            else if (hours <= 10) medium++;
            else heavy++;
            String program = (ta.getProgram() == null || ta.getProgram().isBlank()) ? "Unknown" : ta.getProgram();
            programHours.merge(program, hours, Integer::sum);
            String taName = (ta.getName() == null || ta.getName().isBlank()) ? ta.getStudentId() : ta.getName();
            taHours.add(Map.entry(taName, hours));
        }
        List<Map.Entry<String, Integer>> topTaHours = taHours.stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .collect(Collectors.toList());
        return new WorkloadInsights(light, medium, heavy, topTaHours, programHours);
    }

    private Card buildProgressTable() {
        Card c = new Card();
        c.setLayout(new BorderLayout(0, 8));
        c.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel secHead = new JPanel(new BorderLayout(0, 0));
        secHead.setOpaque(false);
        secHead.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(0x2563EB)),
                new EmptyBorder(0, 14, 0, 0)
        ));
        JLabel title = new JLabel("Position-level Recruitment Progress");
        title.setFont(UI.moFontBold(17));
        title.setForeground(UI.palette().text());
        secHead.add(title, BorderLayout.CENTER);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);
        positionFilter.setPreferredSize(new Dimension(320, 38));
        departmentFilter.setPreferredSize(new Dimension(220, 38));
        UI.styleField(positionFilter);
        UI.styleField(departmentFilter);
        filterRow.add(positionFilter);
        filterRow.add(departmentFilter);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(secHead, BorderLayout.NORTH);
        top.add(filterRow, BorderLayout.CENTER);
        c.add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{
                "Position", "Department", "Stage", "Applications", "Reminder", "View"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setGridColor(JobsPortalUi.LIGHT_PURPLE_BORDER);
        table.setBackground(Color.WHITE);
        table.setForeground(JobsPortalUi.DARK_TEXT);
        table.setSelectionBackground(JobsPortalUi.LAVENDER);
        table.setSelectionForeground(JobsPortalUi.DARK_TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UI.fontMedium(12));
                l.setForeground(JobsPortalUi.TEXT_GRAY);
                l.setBackground(Color.WHITE);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, JobsPortalUi.LIGHT_PURPLE_BORDER),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
                boolean numericCol = column == 3;
                l.setHorizontalAlignment(numericCol ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return l;
            }
        });
        header.setPreferredSize(new Dimension(0, 42));

        Color rowAlt = new Color(0xFAFAFF);
        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UI.fontPlain(13));
                boolean numericCol = column == 3;
                l.setHorizontalAlignment(numericCol ? SwingConstants.RIGHT : SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEDE8FF)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                boolean isHover = row == hoverRow;
                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                } else if (isHover) {
                    l.setBackground(JobsPortalUi.LAVENDER);
                    l.setForeground(JobsPortalUi.DARK_TEXT);
                } else {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : rowAlt);
                    l.setForeground(JobsPortalUi.DARK_TEXT);
                }
                return l;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(baseRenderer);
        }

        DefaultTableCellRenderer stageRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String s = String.valueOf(value);
                l.setFont(UI.fontMedium(12));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEDE8FF)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                boolean isHover = row == hoverRow;
                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                } else if (isHover) {
                    l.setBackground(JobsPortalUi.LAVENDER);
                    l.setForeground(stageTextColor(s));
                } else {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : rowAlt);
                    l.setForeground(stageTextColor(s));
                }
                return l;
            }
        };
        table.getColumnModel().getColumn(2).setCellRenderer(stageRenderer);

        DefaultTableCellRenderer reminderRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String s = String.valueOf(value);
                l.setFont(UI.fontPlain(13));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEDE8FF)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                boolean isHover = row == hoverRow;
                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                } else if (isHover) {
                    l.setBackground(JobsPortalUi.LAVENDER);
                    boolean followUp = s.contains("Follow");
                    l.setForeground(followUp ? new Color(0xB45309) : new Color(0x047857));
                } else {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : rowAlt);
                    boolean followUp = s.contains("Follow");
                    l.setForeground(followUp ? new Color(0xB45309) : new Color(0x047857));
                }
                return l;
            }
        };
        table.getColumnModel().getColumn(4).setCellRenderer(reminderRenderer);

        table.getColumnModel().getColumn(5).setCellRenderer(new ViewButtonRenderer(rowAlt, new Color(0xEDE8FF)));

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
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return;
                int modelRow = table.convertRowIndexToModel(row);
                if (col == 5 && modelRow >= 0 && modelRow < filteredList.size()) {
                    showDetailPage(filteredList.get(modelRow));
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(340);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        sp.setPreferredSize(new Dimension(900, 300));
        c.add(sp, BorderLayout.CENTER);
        return c;
    }

    private void initFilters() {
        positionFilter.addItem("All Positions");
        progressList.stream()
                .map(p -> p.positionName)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .forEach(positionFilter::addItem);

        departmentFilter.addItem("All Departments");
        progressList.stream()
                .map(p -> p.department)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .forEach(departmentFilter::addItem);
    }

    private void bindFilterEvents() {
        positionFilter.addActionListener(e -> refreshTable());
        departmentFilter.addActionListener(e -> refreshTable());
    }

    private void refreshTable() {
        if (tableModel == null) return;

        String posSel = String.valueOf(positionFilter.getSelectedItem());
        String depSel = String.valueOf(departmentFilter.getSelectedItem());

        filteredList = progressList.stream().filter(p -> {
            boolean posOk = "All Positions".equals(posSel) || posSel.equals(p.positionName);
            boolean depOk = "All Departments".equals(depSel) || depSel.equals(p.department);
            return posOk && depOk;
        }).collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (PositionProgress p : filteredList) {
            tableModel.addRow(new Object[]{
                    p.positionName,
                    p.department,
                    p.stage,
                    p.applicationCount,
                    reminderFor(p),
                    "View"
            });
        }
    }

    private void showDetailPage(PositionProgress p) {
        detailPanel.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(2, 0, 8, 0));

        JButton back = new JButton("← Back to Statistics");
        UI.styleBackButton(back);
        back.addActionListener(e -> {
            if (listHeader != null) listHeader.setVisible(true);
            ((CardLayout) viewCards.getLayout()).show(viewCards, CARD_LIST);
        });

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(back);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(backRow, BorderLayout.WEST);

        Card basic = new Card();
        basic.setLayout(new GridLayout(0, 2, 12, 10));
        basic.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        basic.add(labelKey("Position")); basic.add(labelVal(p.positionName));
        basic.add(labelKey("Department")); basic.add(labelVal(p.department));
        basic.add(labelKey("Stage")); basic.add(labelVal(p.stage));
        basic.add(labelKey("Reminder")); basic.add(labelVal(reminderFor(p)));

        JPanel metricGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        metricGrid.setOpaque(false);
        metricGrid.add(metricCard("Applications", String.valueOf(p.applicationCount), new Color(0x2563EB)));
        metricGrid.add(metricCard("Assigned", String.valueOf(p.screenedCount), new Color(0x7C3AED)));
        metricGrid.add(metricCard("Hired", String.valueOf(p.hiredCount), new Color(0x16A34A)));
        metricGrid.add(metricCard("Max Seats", String.valueOf(p.maxSeats), JobsPortalUi.TEXT_GRAY));
        metricGrid.add(metricCard("Assignment Progress", p.screenedPercent + "%", new Color(0x1D4ED8)));
        metricGrid.add(metricCard("Hiring Progress", p.hiringPercent + "%", new Color(0x059669)));

        Card progressCharts = new Card();
        progressCharts.setLayout(new BorderLayout(0, 10));
        progressCharts.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        progressCharts.setBackground(Color.WHITE);

        JLabel chartsTitle = new JLabel("Progress Overview");
        chartsTitle.setFont(UI.moFontBold(15));
        chartsTitle.setForeground(JobsPortalUi.DARK_TEXT);
        progressCharts.add(chartsTitle, BorderLayout.NORTH);

        JPanel chartRows = new JPanel(new GridLayout(3, 1, 0, 10));
        chartRows.setOpaque(false);
        chartRows.add(buildDetailProgressRow("Assignment Completion", p.screenedPercent, new Color(0x2563EB),
                p.screenedCount + " / " + p.applicationCount + " applicants assigned"));
        chartRows.add(buildDetailProgressRow("Hiring Completion", p.hiringPercent, new Color(0x059669),
                p.hiredCount + " / " + p.maxSeats + " seats filled"));
        int seatUsagePercent = p.maxSeats == 0 ? 0 : (int) Math.round(p.hiredCount * 100.0 / p.maxSeats);
        chartRows.add(buildDetailProgressRow("Seat Utilization", seatUsagePercent, new Color(0x7C3AED),
                p.hiredCount + " assigned, capacity " + p.maxSeats));
        progressCharts.add(chartRows, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        basic.setAlignmentX(Component.LEFT_ALIGNMENT);
        metricGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressCharts.setAlignmentX(Component.LEFT_ALIGNMENT);
        metricGrid.setPreferredSize(new Dimension(900, 180));
        metricGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        progressCharts.setPreferredSize(new Dimension(900, 220));
        progressCharts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        body.add(basic);
        body.add(Box.createVerticalStrut(12));
        body.add(metricGrid);
        body.add(Box.createVerticalStrut(12));
        body.add(progressCharts);

        root.add(head, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);

        detailPanel.add(root, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
        if (listHeader != null) listHeader.setVisible(false);
        ((CardLayout) viewCards.getLayout()).show(viewCards, CARD_DETAIL);
    }

    private static JLabel labelKey(String text) {
        JLabel l = new JLabel(text + ":");
        l.setFont(UI.moFontBold(13));
        l.setForeground(JobsPortalUi.MUTED_TEXT);
        return l;
    }

    private static JLabel labelVal(String text) {
        JLabel l = new JLabel(text == null ? "-" : text);
        l.setFont(UI.moFontPlain(13));
        l.setForeground(JobsPortalUi.DARK_TEXT);
        return l;
    }

    private static JPanel buildDetailProgressRow(String label, int percent, Color accent, String note) {
        JPanel row = new JPanel(new BorderLayout(10, 6));
        row.setOpaque(false);

        JLabel left = new JLabel(label);
        left.setFont(UI.moFontBold(13));
        left.setForeground(JobsPortalUi.TEXT_GRAY);

        int clamped = Math.max(0, Math.min(100, percent));
        JLabel right = new JLabel(clamped + "%");
        right.setFont(UI.moFontBold(13));
        right.setForeground(accent);
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(left, BorderLayout.WEST);
        head.add(right, BorderLayout.EAST);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 8));
        bar.add(new SimpleBar(clamped, 100, accent), BorderLayout.CENTER);

        JLabel foot = new JLabel(note);
        foot.setFont(UI.moFontPlain(12));
        foot.setForeground(JobsPortalUi.MUTED_TEXT);

        row.add(head, BorderLayout.NORTH);
        row.add(bar, BorderLayout.CENTER);
        row.add(foot, BorderLayout.SOUTH);
        return row;
    }

    private static Card metricCard(String label, String value, Color accent) {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JobsPortalUi.LIGHT_PURPLE_BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(280, 82));
        card.setMinimumSize(new Dimension(220, 82));

        JLabel l = new JLabel(label);
        l.setFont(UI.moFontPlain(12));
        l.setForeground(JobsPortalUi.MUTED_TEXT);

        JLabel v = new JLabel(value);
        v.setFont(UI.moFontBold(30));
        v.setForeground(accent);

        card.add(l, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private static String reminderFor(PositionProgress p) {
        if (p == null) return "On track";
        if (p.maxSeats > 0 && p.hiredCount >= p.maxSeats) return "Completed";
        return ("Pending Review".equals(p.stage) || "Recruiting".equals(p.stage))
                ? "Follow-up required"
                : "On track";
    }

    /** Muted, readable colors for stage labels (no full-cell tint). */
    private static Color stageTextColor(String stage) {
        return switch (stage) {
            case "Pending Review" -> new Color(0x92400E);
            case "Recruiting" -> new Color(0x1E40AF);
            case "Deadline Closed" -> new Color(0x5B21B6);
            case "Hiring Completed" -> new Color(0x166534);
            case "Ended" -> JobsPortalUi.TEXT_GRAY;
            default -> JobsPortalUi.TEXT_GRAY;
        };
    }

    private List<PositionProgress> buildProgressData(List<Position> positions) {
        List<PositionProgress> list = new ArrayList<>();
        for (Position p : positions) {
            String stage = mapStageFromStatus(p.getStatus());

            int apps = Math.max(0, p.getApplicationCount());
            int hired = Math.max(0, p.getAssignedTAs());
            // JSON schema has no dedicated "screened" field; use confirmed assigned count instead of synthetic ratios.
            int screened = hired;

            int screenedPercent = apps == 0 ? 0 : (int) Math.round(screened * 100.0 / apps);
            int hiringPercent = p.getMaxTAs() == 0 ? 0 : (int) Math.round(hired * 100.0 / p.getMaxTAs());

            list.add(new PositionProgress(
                    p.getTitle() + " - " + p.getCourse(),
                    p.getDepartment(),
                    stage,
                    apps,
                    screened,
                    hired,
                    p.getMaxTAs(),
                    screenedPercent,
                    hiringPercent
            ));
        }
        return list;
    }

    private String mapStageFromStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) return "Recruiting";
        String s = rawStatus.trim().toLowerCase();
        if ("open".equals(s) || "recruiting".equals(s) || "active".equals(s)) return "Recruiting";
        if ("pending".equals(s) || "pending_review".equals(s) || "pending review".equals(s)) return "Pending Review";
        if ("closed".equals(s) || "deadline_closed".equals(s) || "deadline closed".equals(s)) return "Deadline Closed";
        if ("filled".equals(s) || "hiring_completed".equals(s) || "hiring completed".equals(s)) return "Hiring Completed";
        if ("ended".equals(s) || "archived".equals(s)) return "Ended";
        return "Recruiting";
    }

    private class ViewButtonRenderer extends JPanel implements TableCellRenderer {
        private final Color rowAlt;
        private final RoundedActionButton button = new RoundedActionButton("View", RoundedActionButton.Scheme.PRIMARY_BLACK);

        ViewButtonRenderer(Color rowAlt, Color rowLine) {
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean isHover = row == hoverRow;
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else if (isHover) {
                setBackground(JobsPortalUi.LAVENDER);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : rowAlt);
            }
            return this;
        }
    }

    private static final class WorkloadInsights {
        final int lightHoursCount;
        final int mediumHoursCount;
        final int heavyHoursCount;
        final List<Map.Entry<String, Integer>> topTaHours;
        final Map<String, Integer> programHours;

        WorkloadInsights(int lightHoursCount, int mediumHoursCount, int heavyHoursCount,
                         List<Map.Entry<String, Integer>> topTaHours, Map<String, Integer> programHours) {
            this.lightHoursCount = lightHoursCount;
            this.mediumHoursCount = mediumHoursCount;
            this.heavyHoursCount = heavyHoursCount;
            this.topTaHours = topTaHours;
            this.programHours = programHours;
        }
    }

    private static final class SimpleBar extends JPanel {
        private final int value;
        private final int maxValue;
        private final Color color;

        SimpleBar(int value, int maxValue, Color color) {
            this.value = value;
            this.maxValue = Math.max(1, maxValue);
            this.color = color;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = Math.max(6, getHeight() - 2);
            int y = (getHeight() - h) / 2;
            g2.setColor(JobsPortalUi.LIGHT_PURPLE_BORDER);
            g2.fillRoundRect(0, y, w, h, h, h);
            int fill = (int) Math.round((value * 1.0 / maxValue) * w);
            g2.setColor(color);
            g2.fillRoundRect(0, y, Math.max(0, fill), h, h, h);
            g2.dispose();
        }
    }

    private static final class PositionProgress {
        final String positionName;
        final String department;
        final String stage;
        final int applicationCount;
        final int screenedCount;
        final int hiredCount;
        final int maxSeats;
        final int screenedPercent;
        final int hiringPercent;

        PositionProgress(String positionName, String department, String stage,
                         int applicationCount, int screenedCount, int hiredCount, int maxSeats,
                         int screenedPercent, int hiringPercent) {
            this.positionName = positionName;
            this.department = department;
            this.stage = stage;
            this.applicationCount = applicationCount;
            this.screenedCount = screenedCount;
            this.hiredCount = hiredCount;
            this.maxSeats = maxSeats;
            this.screenedPercent = screenedPercent;
            this.hiringPercent = hiringPercent;
        }
    }
}
