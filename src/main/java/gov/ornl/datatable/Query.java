package gov.ornl.datatable;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class Query {
    private final static Logger log = Logger.getLogger(Query.class.getName());

    private String id;

    private ListProperty<ColumnSelection> columnSelections;
    private HashMap<Column, ColumnSummaryStats> columnQuerySummaryStatsMap;
    private HashMap<Column, ColumnSummaryStats> columnNonquerySummaryStatsMap;
    private DataTable dataModel;

    private HashSet<Tuple> queriedTuples;
    private HashSet<Tuple> nonQueriedTuples;

    public Query(String id, DataTable dataModel) {
        this.id = id;
        this.dataModel = dataModel;
        columnSelections = new SimpleListProperty<>(FXCollections.observableArrayList());
        columnQuerySummaryStatsMap = new HashMap<>();
        columnNonquerySummaryStatsMap = new HashMap<>();
        queriedTuples = new HashSet<>();
        nonQueriedTuples = new HashSet<>();
    }

    public Set<Tuple> getQueriedTuples() {
        return queriedTuples;
    }

    public int getQueriedTupleCount() {
        return queriedTuples.size();
    }

    public int getNonQueriedTupleCount() {
        return nonQueriedTuples.size();
    }

    public Set<Tuple> getNonQueriedTuples() {
        return nonQueriedTuples;
    }

    public void setQueriedTuples() {
        queriedTuples.clear();
        nonQueriedTuples.clear();

        if (dataModel.getTupleCount() == 0) {
            return;
        }

        if (hasColumnSelections()) {
            for (Tuple tuple : dataModel.getTuples()) {
                tuple.setQueryFlag(true);

                for (int icol = 0; icol < dataModel.getColumnCount(); icol++) {
                    Column column = dataModel.getColumn(icol);
                    ArrayList<ColumnSelection> columnSelections = getColumnSelections(column);
                    if (columnSelections != null && (!columnSelections.isEmpty())) {
                        boolean inSelection = false;

                        if (column instanceof DoubleColumn) {
                            for (ColumnSelection columnSelection : columnSelections) {
                                if ((((Double)tuple.getElement(icol)) <= ((DoubleColumnSelectionRange)columnSelection).getMaxValue()) &&
                                        (((Double)tuple.getElement(icol)) >= ((DoubleColumnSelectionRange)columnSelection).getMinValue())) {
                                    inSelection = true;
                                    break;
                                }
                            }
                        } else if (column instanceof TemporalColumn) {
                            for (ColumnSelection columnSelection : columnSelections) {
                                if (!((((Instant)tuple.getElement(icol)).isBefore(((TemporalColumnSelectionRange)columnSelection).getStartInstant())) ||
                                        ((Instant)tuple.getElement(icol)).isAfter(((TemporalColumnSelectionRange)columnSelection).getEndInstant()))) {
                                    inSelection = true;
                                    break;
							    }
                            }
                        } else if (column instanceof CategoricalColumn) {
                            for (ColumnSelection columnSelection : columnSelections) {
                                if (((CategoricalColumnSelection)columnSelection).getSelectedCategories().contains(tuple.getElement(icol))) {
                                    inSelection = true;
                                    break;
                                }
                            }
                        } else if (column instanceof ImageColumn) {
                            for (ColumnSelection columnSelection : columnSelections) {
                                if (((ImageColumnSelection)columnSelection).getSelectedImagePairs().contains(tuple.getElement(icol))) {
                                    inSelection = true;
                                    break;
                                }
                            }
                        }

                        if (!inSelection) {
                            tuple.setQueryFlag(false);
                            break;
                        }
                    }
                }

                if (tuple.getQueryFlag()) {
                    queriedTuples.add(tuple);
                } else {
                    nonQueriedTuples.add(tuple);
                }
            }

            calculateStatistics();
        } else {
            for (Tuple tuple : dataModel.getTuples()) {
                tuple.setQueryFlag(false);
                nonQueriedTuples.add(tuple);
            }
            columnQuerySummaryStatsMap.clear();
            columnNonquerySummaryStatsMap.clear();
        }
    }

    public void setNumHistogramBins(int numBins) {
        for (ColumnSummaryStats summaryStats : columnQuerySummaryStatsMap.values()) {
            summaryStats.setNumHistogramBins(numBins);
        }
    }

    public void calculateStatistics() {
        long start = System.currentTimeMillis();

        for (int icolumn = 0; icolumn < dataModel.getColumnCount(); icolumn++) {
            Column column = dataModel.getColumn(icolumn);
            ColumnSummaryStats queryColumnSummaryStats = columnQuerySummaryStatsMap.get(column);
            ColumnSummaryStats nonqueryColumnSummaryStats = columnNonquerySummaryStatsMap.get(column);

            if (column instanceof TemporalColumn) {
                Instant queriedValues[] = ((TemporalColumn)column).getQueriedValues();
                if (queryColumnSummaryStats == null) {
                    queryColumnSummaryStats = new TemporalColumnSummaryStats(column, dataModel.getNumHistogramBins(), this);
                    columnQuerySummaryStatsMap.put(column, queryColumnSummaryStats);
                }
                ((TemporalColumnSummaryStats)queryColumnSummaryStats).setValues(queriedValues);

                Instant nonqueriedValues[] = ((TemporalColumn)column).getNonqueriedValues();
                if (nonqueryColumnSummaryStats == null) {
                    nonqueryColumnSummaryStats = new TemporalColumnSummaryStats(column, dataModel.getNumHistogramBins(), this);
                    columnNonquerySummaryStatsMap.put(column, nonqueryColumnSummaryStats);
                }
                ((TemporalColumnSummaryStats)nonqueryColumnSummaryStats).setValues(nonqueriedValues);
            } else if (column instanceof DoubleColumn) {
                if (dataModel.getCalculateQueryStatistics()) {
                    double queriedValues[] = ((DoubleColumn) column).getQueriedValues();
                    if (queryColumnSummaryStats == null) {
                        queryColumnSummaryStats = new DoubleColumnSummaryStats(column, dataModel.getNumHistogramBins(), this);
                        columnQuerySummaryStatsMap.put(column, queryColumnSummaryStats);
                    }
                    ((DoubleColumnSummaryStats) queryColumnSummaryStats).setValues(queriedValues);
                } else {
                    columnQuerySummaryStatsMap.remove(column);
                }

                if (dataModel.getCalculateNonQueryStatistics()) {
                    double nonqueriedValues[] = ((DoubleColumn) column).getNonqueriedValues();
                    if (nonqueryColumnSummaryStats == null) {
                        nonqueryColumnSummaryStats = new DoubleColumnSummaryStats(column, dataModel.getNumHistogramBins(), this);
                        columnNonquerySummaryStatsMap.put(column, nonqueryColumnSummaryStats);
                    }
                    ((DoubleColumnSummaryStats) nonqueryColumnSummaryStats).setValues(nonqueriedValues);
                } else {
                    columnNonquerySummaryStatsMap.remove(column);
                }
            } else if (column instanceof CategoricalColumn) {
                String queriedValues[] = ((CategoricalColumn)column).getQueriedValues();
                if (queryColumnSummaryStats == null) {
                    queryColumnSummaryStats = new CategoricalColumnSummaryStats(column, this);
                    columnQuerySummaryStatsMap.put(column, queryColumnSummaryStats);
                }
                ((CategoricalColumnSummaryStats)queryColumnSummaryStats).setValues(queriedValues);

                String nonqueriedValues[] = ((CategoricalColumn)column).getNonqueriedValues();
                if (nonqueryColumnSummaryStats == null) {
                    nonqueryColumnSummaryStats = new CategoricalColumnSummaryStats(column, this);
                    columnNonquerySummaryStatsMap.put(column, nonqueryColumnSummaryStats);
                }
                ((CategoricalColumnSummaryStats)nonqueryColumnSummaryStats).setValues(nonqueriedValues);
            }
        }
        long elapsed = System.currentTimeMillis() - start;
//        log.info("calculateStatistics() took " + elapsed + "ms");
    }

    public final ObservableList<ColumnSelection> getColumnSelections() { return columnSelections.get(); }

    public ListProperty<ColumnSelection> columnSelectionsProperty() { return columnSelections; }

    public boolean hasColumnSelections() {
        if (columnSelections.isEmpty()) {
            return false;
        }
        return true;
    }

    public ColumnSummaryStats getColumnQuerySummaryStats(Column column) {
        return columnQuerySummaryStatsMap.get(column);
    }

    public void setColumnQuerySummaryStats(Column column, ColumnSummaryStats querySummaryStats) {
        columnQuerySummaryStatsMap.put(column, querySummaryStats);
    }

    public ColumnSummaryStats getColumnNonquerySummaryStats(Column column) {
        return columnNonquerySummaryStatsMap.get(column);
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void clear () {
        columnSelections.clear();
        columnQuerySummaryStatsMap.clear();
        columnNonquerySummaryStatsMap.clear();
    }

    public ArrayList<ColumnSelection> getColumnSelections(Column column) {
        ArrayList<ColumnSelection> rangeList = new ArrayList<>();

        for (ColumnSelection columnSelectionRange : columnSelections) {
            if (columnSelectionRange.getColumn() == column) {
                rangeList.add(columnSelectionRange);
            }
        }
        return rangeList;
    }

    public List<ColumnSelection> getAllColumnSelectionRanges() {
        return columnSelections;
    }

    public void addColumnSelection(ColumnSelection columnSelection) {
        //TODO: See if an identical range selection exists or if this selection overlaps with another
        // ignore if identical exists and merge if overlapping selection exists
        columnSelections.add(columnSelection);
    }

    protected boolean removeColumnSelection(ColumnSelection columnSelection) {
        if (!columnSelections.isEmpty()) {
            return columnSelections.remove(columnSelection);
        }
        return false;
    }

    protected boolean removeColumnSelections(List<ColumnSelection> columnSelectionsToRemove) {
        if (!columnSelections.isEmpty()) {
            return columnSelections.removeAll(columnSelectionsToRemove);
        }
        return false;
    }

    protected ArrayList<ColumnSelection> removeColumnSelections(Column column) {
        if (!columnSelections.isEmpty()) {
            ArrayList<ColumnSelection> removedRanges = new ArrayList<>();

            for (ColumnSelection rangeSelection : columnSelections) {
                if (rangeSelection.getColumn() == column) {
                    removedRanges.add(rangeSelection);
                }
            }

            columnSelections.removeAll(removedRanges);
            return removedRanges;
        }

        return null;
    }
}