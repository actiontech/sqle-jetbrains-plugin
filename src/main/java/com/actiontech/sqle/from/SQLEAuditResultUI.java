package com.actiontech.sqle.from;

import com.actiontech.sqle.config.SQLEAuditResult;
import com.actiontech.sqle.config.SQLEAuditResultItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;

public class SQLEAuditResultUI {
    private JPanel rootPanel;
    private JLabel auditLevel;
    private JLabel passRate;
    private JLabel score;
    private JPanel tableJPanel;
    private JPanel tableMetaDataJpanel;
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
}
