package com.actiontech.sqle.from;

import com.actiontech.sqle.config.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLEAuditResultUI {
    private JPanel rootPanel;
    private JLabel auditLevel;
    private JLabel passRate;
    private JLabel score;
    private JPanel tableJPanel;
    private JScrollPane scrollPane;
    private JPanel titlePanel;
    private JPanel overviewPanel;
    private JPanel tableMetaDataJpanel;
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

    JTable table = new JTable();
    DefaultTableModel model = (DefaultTableModel) table.getModel();

    private void loadData(SQLEAuditResult result) {
        auditLevel.setText(result.getAuditLevel());
        passRate.setText(String.valueOf(result.getPassRate()));
        score.setText(String.valueOf(result.getScore()));

        // load table
        ArrayList<SQLEAuditResultItem> items = result.getSQLResults();

        String[] columnNames = {"序号", "审核SQL", "审核结果"};
        model.setColumnIdentifiers(columnNames);

        for (int i = 0; i < items.size(); i++) {
            Object[] item = new Object[3];
            item[0] = generateHtml(String.valueOf(items.get(i).getNumber()));
            item[1] = generateHtml(items.get(i).getExecSQL());
            item[2] = generateHtml(items.get(i).getAuditResult());
            model.addRow(item);
        }

        FitTableSize(table);
        JTableHeader jTableHeader = table.getTableHeader();
        tableJPanel.add(jTableHeader, BorderLayout.NORTH);
        tableJPanel.add(table, BorderLayout.CENTER);
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
                r.setHorizontalAlignment(JLabel.CENTER);
                columnModel.getColumn(column).setCellRenderer(r);
            } else {
                width += myTable.getFont().getSize() + myTable.getIntercellSpacing().getWidth();
            }

            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    public String generateHtml(String text) {
        text = text.trim();
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><body>");
        buffer.append(text.replaceAll("\n", "<br>"));
        buffer.append("</body></html>");
        return buffer.toString();
    }

    private void loadListener() {
        table.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setToolTipText("<html>" + "鼠标左击显示SQL分析内容" + "</html>");
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
                    int row = table.rowAtPoint(event.getPoint());
                    if (row >= 0) {
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
            }
        });
    }
}
