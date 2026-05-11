package com.taapp.ui.pages;

import com.taapp.data.MockData;
import com.taapp.model.Position;
import com.taapp.ui.AppLayout;
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
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
        this.progressList = buildProgressData(MockData.getMockPositions());
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

        JLabel h1 = new JLabel("Recruitment Progress Global Monitor");
        h1.setFont(UI.moFontBold(26));
        h1.setForeground(UI.palette().text());
        h1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Track hiring status, application flow, screening progress, and completion risk across all TA positions");
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
                BorderFactory.createLineBorder(new Color(0xE8EAEF)),
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
        panel.add(buildStatusOverview(), BorderLayout.NORTH);
        panel.add(buildProgressTable(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusOverview() {
        Map<String, Integer> counts = new TreeMap<>();
        counts.put("Pending Review", 0);
        counts.put("Recruiting", 0);
        counts.put("Deadline Closed", 0);
        counts.put("Hiring Completed", 0);
        counts.put("Ended", 0);

        for (PositionProgress p : progressList) {
            counts.put(p.stage, counts.getOrDefault(p.stage, 0) + 1);
        }

        JPanel row = new JPanel(new GridLayout(1, 5, 8, 8));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        row.add(stageCard("Pending Review", counts.get("Pending Review"), new Color(0xF59E0B)));
        row.add(stageCard("Recruiting", counts.get("Recruiting"), new Color(0x2563EB)));
        row.add(stageCard("Deadline Closed", counts.get("Deadline Closed"), new Color(0x7C3AED)));
        row.add(stageCard("Hiring Completed", counts.get("Hiring Completed"), new Color(0x16A34A)));
        row.add(stageCard("Ended", counts.get("Ended"), new Color(0x6B7280)));
        return row;
    }

    private Card stageCard(String label, int value, Color accent) {
        Card c = new Card();
        c.setLayout(new BorderLayout());
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        c.setBackground(new Color(0xFAFAFA));

        JLabel l = new JLabel(label);
        l.setFont(UI.moFontPlain(12));
        l.setForeground(UI.palette().textSecondary());

        JLabel v = new JLabel(String.valueOf(value));
        v.setFont(UI.moFontBold(30));
        v.setForeground(accent);

        c.add(l, BorderLayout.NORTH);
        c.add(v, BorderLayout.CENTER);
        return c;
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
        table.setGridColor(new Color(0xEEF1F4));
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(0x111827));
        table.setSelectionBackground(new Color(0xF3F4F6));
        table.setSelectionForeground(new Color(0x111827));
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UI.fontMedium(12));
                l.setForeground(new Color(0x4B5563));
                l.setBackground(Color.WHITE);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5E7EB)),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));
                boolean numericCol = column == 3;
                l.setHorizontalAlignment(numericCol ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return l;
            }
        });
        header.setPreferredSize(new Dimension(0, 42));

        Color rowAlt = new Color(0xFAFBFC);
        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UI.fontPlain(13));
                boolean numericCol = column == 3;
                l.setHorizontalAlignment(numericCol ? SwingConstants.RIGHT : SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0F2F5)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
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

        DefaultTableCellRenderer stageRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String s = String.valueOf(value);
                l.setFont(UI.fontMedium(12));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0F2F5)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                boolean isHover = row == hoverRow;
                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                } else if (isHover) {
                    l.setBackground(new Color(0xF3F4F6));
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
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0F2F5)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                boolean isHover = row == hoverRow;
                if (isSelected) {
                    l.setBackground(table.getSelectionBackground());
                    l.setForeground(table.getSelectionForeground());
                } else if (isHover) {
                    l.setBackground(new Color(0xF3F4F6));
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

        table.getColumnModel().getColumn(5).setCellRenderer(new ViewButtonRenderer(rowAlt, new Color(0xF0F2F5)));

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
                    reminderByStage(p.stage),
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

        JLabel title = new JLabel("Position Detail");
        title.setFont(UI.moFontBold(30));
        title.setForeground(new Color(0x0F172A));

        JLabel subtitle = new JLabel("<html><div style='width:900px;color:#6B7280;font-size:14px;font-family:sans-serif'>"
                + p.positionName + " • " + p.department + " • " + p.stage
                + "</div></html>");

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        head.add(backRow);
        head.add(Box.createVerticalStrut(10));
        head.add(title);
        head.add(Box.createVerticalStrut(4));
        head.add(subtitle);

        Card basic = new Card();
        basic.setLayout(new GridLayout(0, 2, 12, 10));
        basic.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        basic.add(labelKey("Position")); basic.add(labelVal(p.positionName));
        basic.add(labelKey("Department")); basic.add(labelVal(p.department));
        basic.add(labelKey("Stage")); basic.add(labelVal(p.stage));
        basic.add(labelKey("Reminder")); basic.add(labelVal(reminderByStage(p.stage)));

        JPanel metricGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        metricGrid.setOpaque(false);
        metricGrid.add(metricCard("Applications", String.valueOf(p.applicationCount), new Color(0x2563EB)));
        metricGrid.add(metricCard("Screened", String.valueOf(p.screenedCount), new Color(0x7C3AED)));
        metricGrid.add(metricCard("Hired", String.valueOf(p.hiredCount), new Color(0x16A34A)));
        metricGrid.add(metricCard("Max Seats", String.valueOf(p.maxSeats), new Color(0x374151)));
        metricGrid.add(metricCard("Screening Progress", p.screenedPercent + "%", new Color(0x1D4ED8)));
        metricGrid.add(metricCard("Hiring Progress", p.hiringPercent + "%", new Color(0x059669)));

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(basic, BorderLayout.NORTH);
        body.add(metricGrid, BorderLayout.CENTER);

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
        l.setForeground(new Color(0x475569));
        return l;
    }

    private static JLabel labelVal(String text) {
        JLabel l = new JLabel(text == null ? "-" : text);
        l.setFont(UI.moFontPlain(13));
        l.setForeground(new Color(0x0F172A));
        return l;
    }

    private static Card metricCard(String label, String value, Color accent) {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.setBackground(Color.WHITE);

        JLabel l = new JLabel(label);
        l.setFont(UI.moFontPlain(12));
        l.setForeground(new Color(0x64748B));

        JLabel v = new JLabel(value);
        v.setFont(UI.moFontBold(24));
        v.setForeground(accent);

        card.add(l, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private static String reminderByStage(String stage) {
        return ("Pending Review".equals(stage) || "Recruiting".equals(stage))
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
            case "Ended" -> new Color(0x4B5563);
            default -> new Color(0x374151);
        };
    }

    private List<PositionProgress> buildProgressData(List<Position> positions) {
        List<PositionProgress> list = new ArrayList<>();
        int idx = 0;
        for (Position p : positions) {
            String stage = mapStage(idx);

            int apps = p.getApplicationCount();
            int screened = switch (stage) {
                case "Pending Review" -> Math.max(0, (int) Math.round(apps * 0.15));
                case "Recruiting" -> Math.max(0, (int) Math.round(apps * 0.45));
                case "Deadline Closed" -> Math.max(0, (int) Math.round(apps * 0.75));
                case "Hiring Completed" -> apps;
                default -> apps;
            };

            int hired = switch (stage) {
                case "Pending Review", "Recruiting" -> Math.min(p.getAssignedTAs(), p.getMaxTAs() / 2);
                case "Deadline Closed" -> Math.min(p.getAssignedTAs(), p.getMaxTAs());
                case "Hiring Completed", "Ended" -> p.getAssignedTAs();
                default -> p.getAssignedTAs();
            };

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
            idx++;
        }
        return list;
    }

    private String mapStage(int index) {
        return switch (index % 5) {
            case 0 -> "Pending Review";
            case 1 -> "Recruiting";
            case 2 -> "Deadline Closed";
            case 3 -> "Hiring Completed";
            default -> "Ended";
        };
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
                setBackground(new Color(0xF3F4F6));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : rowAlt);
            }
            return this;
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
