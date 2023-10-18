package com.actiontech.sqle.from;

import com.actiontech.sqle.config.*;
import com.actiontech.sqle.util.*;
import com.github.vertical_blank.sqlformatter.SqlFormatter;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SQLEAuditResultUI {
    private JPanel rootPanel;
    private JPanel tableJPanel;
    private JScrollPane scrollPane;
    private JPanel tableMetaDataJpanel;
    private JPanel sqlDetailPanel;
    private JPanel sqlAuditResultPanel;
    private SQLEAuditResult result;

    private List<SQLESQLAnalysisResult> analysisResult;


    public SQLEAuditResultUI(SQLEAuditResult result, List<SQLESQLAnalysisResult> analysisResult) {
        this.result = result;
        this.analysisResult = analysisResult;
        loadData(result);
        loadListener();
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    JTable sqlAuditResultTable = new JTable();

    LinkedList<AuditResult> auditResultList;

    JTable table = new JTable();
    DefaultTableModel model = (DefaultTableModel) table.getModel();

    private void loadData(SQLEAuditResult result) {
        // load table
        ArrayList<SQLEAuditResultItem> items = result.getSQLResults();

        String[] columnNames = {"序号", "审核SQL", "审核等级"};
        model.setColumnIdentifiers(columnNames);

        for (int i = 0; i < items.size(); i++) {
            Object[] item = new Object[3];
            item[0] = generateHtml(String.valueOf(items.get(i).getNumber()));
            String execSQL = items.get(i).getExecSQL();
            String newExecSQL = execSQL.replaceAll("\n", " ");
            item[1] = generateHtml(Util.getEllipsisString(newExecSQL, 50));
            item[2] = generateHtml(items.get(i).getAuditLevel());
            model.addRow(item);
        }

        FitTableSize(table);
        JTableHeader jTableHeader = table.getTableHeader();
        tableJPanel.add(jTableHeader, BorderLayout.NORTH);
        tableJPanel.add(table, BorderLayout.CENTER);


        tableMetaDataJpanel.removeAll();
        tableMetaDataJpanel.setBorder(new TitledBorder(new EtchedBorder(), "SQL分析"));

        sqlDetailPanel.removeAll();
        sqlDetailPanel.setBorder(new TitledBorder(new EtchedBorder(), "SQL详情"));

        sqlAuditResultPanel.removeAll();
        sqlAuditResultPanel.setBorder(new TitledBorder(new EtchedBorder(), "SQL审核结果"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableJPanel, createRightPanel(sqlDetailPanel, sqlAuditResultPanel, tableMetaDataJpanel));
        JScrollPane jScrollPane = new JScrollPane(splitPane);
        getRootPanel().add(jScrollPane);


        if (result.getSQLResults().size() > 0) {
            showSQLAuditDetail(result, 0);
        }
    }

    private JPanel createRightPanel(JPanel rightPanel1, JPanel rightPanel2, JPanel rightPanel3) {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 1));
        topPanel.add(rightPanel1);
        topPanel.add(rightPanel2);

        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(rightPanel3, BorderLayout.CENTER);

        return rightPanel;
    }

    private void showSQLAuditDetail(SQLEAuditResult result, int row) {
        JTable sqlTable = new JTable();
        DefaultTableModel sqlTableModel = (DefaultTableModel) sqlTable.getModel();
        sqlTableModel.setColumnIdentifiers(new String[]{""});
        String sqlDetail = SqlFormatter.format(result.getSQLResults().get(row).getExecSQL());
        String sqlDetailHtml = generateHtml(sqlDetail);
        sqlTableModel.addRow((new String[]{sqlDetailHtml}));
        JTableHeader sqlTableTableHeader = sqlTable.getTableHeader();
        sqlDetailPanel.add(sqlTableTableHeader, BorderLayout.NORTH);
        sqlDetailPanel.add(sqlTable, BorderLayout.CENTER);
        FitTableSize(sqlTable);

        List<SQLEAuditResultItem> sqlResults = result.getSQLResults();
        auditResultList = sqlResults.get(row).getAuditResult();

        String[] columnNames = {"规则", "知识库"};

        Object[][] data = new Object[auditResultList.size()][columnNames.length];
        for (int i = 0; i < auditResultList.size(); i++) {
            AuditResult auditResult = auditResultList.get(i);
            String auditStr = String.format("[%s]%s", auditResult.getLevel(), auditResult.getMessage());
            data[i][0] = generateHtml(auditStr);
            data[i][1] = generateHtml("点击查看知识库");
        }

        sqlAuditResultTable = new JTable(data, columnNames);

        sqlAuditResultTable.getColumnModel().getColumn(0).setCellRenderer(new StringRenderer());
        sqlAuditResultTable.getColumnModel().getColumn(1).setCellRenderer(new ButtonRenderer());
        sqlAuditResultTable.getColumnModel().getColumn(1).setCellEditor(new ButtonEditor());

        JTableHeader sqlAuditResultTableHeader = sqlAuditResultTable.getTableHeader();
        sqlAuditResultPanel.add(sqlAuditResultTableHeader, BorderLayout.NORTH);
        sqlAuditResultPanel.add(sqlAuditResultTable, BorderLayout.CENTER);
        FitTableSize(sqlAuditResultTable);

        if (analysisResult.size() > 0) {
            JTable jTable = new JTable();
            DefaultTableModel model = (DefaultTableModel) jTable.getModel();

            SQLESQLAnalysisResult sqlAnalysisResult = analysisResult.get(row);
            SQLExplain explain = sqlAnalysisResult.getSqlExplain();
            List<TableMetaItemHeadResV1> headList = explain.getClassicResult().getHead();
            String[] headNameList = new String[headList.size()];
            for (int i = 0; i < headNameList.length; i++) {
                headNameList[i] = headList.get(i).getFieldName();
            }

            model.setColumnIdentifiers(headNameList);

            List<Map<String, String>> rowMapList = explain.getClassicResult().getRows();
            for (int i = 0; i < rowMapList.size(); i++) {
                Object[] item = new Object[headList.size()];
                for (int j = 0; j < headList.size(); j++) {
                    Object itemHtml = generateHtml(rowMapList.get(i).get(headList.get(j).getFieldName()));
                    item[j] = itemHtml;
                }
                model.addRow(item);
            }

            JTableHeader jTableHeader1 = jTable.getTableHeader();
            tableMetaDataJpanel.add(jTableHeader1, BorderLayout.NORTH);
            tableMetaDataJpanel.add(jTable, BorderLayout.CENTER);
            FitTableSize(jTable);
        }
    }

    public void FitTableSize(JTable myTable) {
        FitTableWidth(myTable);
        FitTableHeight(myTable);
    }

    public void FitTableHeight(JTable myTable) {
        for (int row = 0; row < myTable.getRowCount(); row++) {
            int rowHeight = myTable.getRowHeight();
            for (int column = 0; column < myTable.getColumnCount(); column++) {
                Component comp = myTable.prepareRenderer(myTable.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            myTable.setRowHeight(row, rowHeight + myTable.getFont().getSize() + (int) (myTable.getIntercellSpacing().getHeight()));
        }
    }

    public void FitTableWidth(JTable myTable) {
        final TableColumnModel columnModel = myTable.getColumnModel();
        for (int column = 0; column < myTable.getColumnCount(); column++) {
            int width = myTable.getTableHeader().getColumnModel().getColumn(column).getWidth();
            for (int row = 0; row < myTable.getRowCount(); row++) {
                TableCellRenderer renderer = myTable.getCellRenderer(row, column);
                Component comp = myTable.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width, width);
            }

            DefaultTableCellRenderer r = new DefaultTableCellRenderer();
            if (column == 0) {
                r.setHorizontalAlignment(JLabel.LEFT);
                columnModel.getColumn(column).setCellRenderer(r);
            } else {
                width += myTable.getFont().getSize() + myTable.getIntercellSpacing().getWidth();
            }

            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    public String generateHtml(String text) {
        text = text.trim();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        String s = text.replaceAll("\n", "<br>");
        String s1 = s.replaceAll(" ", "&nbsp;");
        sb.append(s1);
        sb.append("</body></html>");
        return sb.toString();
    }

    private void loadListener() {
        table.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setToolTipText("<html>" + "鼠标左击显示审核详情" + "</html>");
                } else {
                    table.setToolTipText("");
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    tableMetaDataJpanel.removeAll();
                    tableMetaDataJpanel.setBorder(new TitledBorder(new EtchedBorder(), "SQL分析"));

                    sqlDetailPanel.removeAll();
                    sqlDetailPanel.setBorder(new TitledBorder(new EtchedBorder(), "SQL详情"));

                    sqlAuditResultPanel.removeAll();
                    sqlAuditResultPanel.setBorder(new TitledBorder(new EtchedBorder(), "SQL审核结果"));

                    int row = table.rowAtPoint(event.getPoint());
                    if (row >= 0) {
                        showSQLAuditDetail(result, row);
                    }
                }
            }
        });
    }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(false);  // 设置按钮为透明
            setBorderPainted(false);  // 设置不绘制按钮的边框
            setFocusPainted(false);  // 设置不绘制按钮的焦点样式
            setContentAreaFilled(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value != null) ? value.toString() : "");
            return this;
        }
    }

    class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton button;

        public ButtonEditor() {
            sqlAuditResultTable.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    button = new JButton("点击查看知识库");
                    int rowPoint = sqlAuditResultTable.rowAtPoint(e.getPoint());
                    button.addActionListener(e1 -> {
                        String ruleName = auditResultList.get(rowPoint).getRuleName();

                        SQLESettings settings = SQLESettings.getInstance();
                        HttpClientUtil client = new HttpClientUtil(settings);
                        String knowledge;
                        try {
                            knowledge = client.GetRuleKnowledge(settings.getDBType(), ruleName);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }

                        NewWindow newWindow = new NewWindow();
                        JLabel label = new JLabel(generateHtml(knowledge));

                        label.setVerticalAlignment(JLabel.TOP);
                        label.setHorizontalAlignment(JLabel.LEFT);

                        JScrollPane scrollPane = new JScrollPane(label);

                        newWindow.setContentPane(scrollPane);
                        newWindow.setVisible(true);

                        fireEditingStopped();
                    });
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return button;
        }

        public Object getCellEditorValue() {
            return button.getText();
        }
    }
}
