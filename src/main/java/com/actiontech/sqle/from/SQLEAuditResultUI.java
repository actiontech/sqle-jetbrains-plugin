package com.actiontech.sqle.from;

import com.actiontech.sqle.config.SQLEAuditResult;
import com.actiontech.sqle.config.SQLEAuditResultItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class SQLEAuditResultUI {
    private JPanel rootPanel;
    private JLabel auditLevel;
    private JLabel passRate;
    private JLabel score;
    private JPanel tableJPanel;
    private JScrollPane scrollPane;
    private JPanel titlePanel;
    private JPanel overviewPanel;
    private SQLEAuditResult result;


    public SQLEAuditResultUI(SQLEAuditResult result) {
        this.result = result;
        loadData(result);
    }

    public JComponent getRootPanel() {
        return rootPanel;
    }

    private void loadData(SQLEAuditResult result) {
        auditLevel.setText(result.getAuditLevel());
        passRate.setText(String.valueOf(result.getPassRate()));
        score.setText(String.valueOf(result.getScore()));

        // load table
        ArrayList<SQLEAuditResultItem> items = result.getSQLResults();

        String[] columnNames = {"序号", "审核SQL", "审核结果"};
        Object[][] data = new Object[items.size()][3];
        for (int i = 0; i < items.size(); i++) {
            Object[] item = new Object[3];
            item[0] = generateHtml(String.valueOf(items.get(i).getNumber()));
            item[1] = generateHtml(items.get(i).getExecSQL());
            item[2] = generateHtml(items.get(i).getAuditResult());
            data[i] = item;
        }
        JTable table = new JTable(data, columnNames);

        FitTableColumns(table);
        JTableHeader jTableHeader = table.getTableHeader();
        tableJPanel.add(jTableHeader, BorderLayout.NORTH);
        tableJPanel.add(table, BorderLayout.CENTER);

    }

    public void FitTableColumns(JTable myTable) {
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) myTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(myTable, column.getIdentifier()
                            , false, false, -1, col).getPreferredSize().getWidth();
            int fontSize = myTable.getFont().getSize();
            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,
                        myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);

                int line = appearNumber(myTable.getValueAt(row, col).toString(), "<br>");
                int height = (line + 3) * fontSize;
                myTable.setRowHeight(row, height);
            }
            header.setResizingColumn(column); // 此行很重要
            column.setWidth(width + myTable.getIntercellSpacing().width);
        }

        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.CENTER);
        myTable.setDefaultRenderer(Object.class, r);
        int rootPanelHeight = myTable.getHeight() + titlePanel.getHeight() + overviewPanel.getHeight();
        rootPanel.setPreferredSize(new Dimension(myTable.getWidth(), 300));
    }

    public static int appearNumber(String srcText, String findText) {
        if (findText.equals("")) {
            return 0;
        }
        int oldCount = srcText.length();
        int newCount = srcText.replaceAll(findText, "").length();
        return (oldCount - newCount) / findText.length();
    }


    public String generateHtml(String text) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><body>");
        buffer.append(text.replaceAll("\n", "<br>"));
        buffer.append("</body></html>");
        return buffer.toString();
    }
}
