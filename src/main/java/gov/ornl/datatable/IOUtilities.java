package gov.ornl.datatable;

import javafx.scene.image.Image;
import javafx.util.Pair;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;

public class IOUtilities {
    private static final Logger log = Logger.getLogger(IOUtilities.class.getName());

//	public static void readCSVSample(File f, DataTable dataModel,
//			double sampleFactor) throws IOException {
//		BufferedReader reader = new BufferedReader(new FileReader(f));
//		int totalLineCount = 0;
//		String line = null;
//		while ((line = reader.readLine()) != null) {
//			totalLineCount++;
//		}
//		totalLineCount -= 1; // remove header line
//		reader.close();
//
//		log.info("totalLineCount is " + totalLineCount);
//
//		int sampleSize = (int) (sampleFactor * totalLineCount);
//		log.info("sample size is " + sampleSize);
//
//		int sampleIndices[] = new int[sampleSize];
//		boolean sampleSelected[] = new boolean[totalLineCount];
//		Arrays.fill(sampleSelected, false);
//		Random rand = new Random();
//		for (int i = 0; i < sampleIndices.length; i++) {
//			int index = rand.nextInt(totalLineCount);
//			while (sampleSelected[index]) {
//				log.info("got a duplicate");
//				index = rand.nextInt(totalLineCount);
//			}
//			sampleSelected[index] = true;
//			sampleIndices[i] = index;
//		}
//
//		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
//		ArrayList<DoubleColumn> columns = new ArrayList<DoubleColumn>();
//		reader = new BufferedReader(new FileReader(f));
//
//		// Read the header line
//		line = reader.readLine();
//		int tokenCounter = 0;
//		StringTokenizer st = new StringTokenizer(line);
//		while (st.hasMoreTokens()) {
//			String token = st.nextToken(",");
//			DoubleColumn column = new DoubleColumn(token.trim());
////			column.setName(token.trim());
//			columns.add(column);
//			tokenCounter++;
//		}
//
//		// Read the data tuples
//		int lineCounter = 0;
//		boolean skipLine = false;
//		while ((line = reader.readLine()) != null) {
//			// is the current line selected to be read
//			if (sampleSelected[lineCounter]) {
//				// read the line as a tuple
//				Tuple tuple = new Tuple();
//				st = new StringTokenizer(line);
//				tokenCounter = 0;
//
//				skipLine = false;
//				while (st.hasMoreTokens()) {
//					String token = st.nextToken(",");
//					try {
//						double value = Double.parseDouble(token);
//
//						// data attribute
//						tuple.addElement(value);
//
//						tokenCounter++;
//					} catch (NumberFormatException ex) {
//						log.info("NumberFormatException caught so skipping record. "
//								+ ex.fillInStackTrace());
//						skipLine = true;
//						break;
//					}
//				}
//
//				if (!skipLine) {
//					// log.info("added tuple at index " + lineCounter);
//					tuples.add(tuple);
//				}
//
//				// line = reader.readLine();
//			}
//
//			lineCounter++;
//		}
//
//		reader.close();
//		dataModel.setData(tuples, columns);
//	}

    public static int exportDataToCSVFile(File csvFile, List<Column> columns, List<Tuple> tuples) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));

        // write header line with column names
        StringBuffer headerLine = new StringBuffer();
        for (int i = 0; i < columns.size(); i++) {
            headerLine.append(columns.get(i).getName());
            if ((i + 1) < columns.size()) {
                headerLine.append(",");
            }
        }
        writer.write(headerLine.toString().trim() + "\n");

        int tuplesWrittenCounter = 0;
        for (Tuple tuple : tuples) {
            StringBuffer lineBuffer = new StringBuffer();
            for (int i = 0; i < tuple.getElementCount(); i++) {
                lineBuffer.append(String.valueOf(tuple.getElement(i)));
                if ((i + 1) < tuple.getElementCount()) {
                    lineBuffer.append(",");
                }
            }
            writer.write(lineBuffer.toString().trim() + "\n");
            tuplesWrittenCounter++;
        }

        writer.close();
        return tuplesWrittenCounter;
    }

    public static int exportUnselectedFromDataTableToCSV(File csvFile, DataTable dataTable) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));

        // write header line with column names
        StringBuffer headerLine = new StringBuffer();
        for (int icol = 0; icol < dataTable.getColumnCount(); icol++) {
            Column column = dataTable.getColumn(icol);
            if (headerLine.length() == 0) {
                headerLine.append(column.getName());
            } else {
                headerLine.append("," + column.getName());
            }
        }

        writer.write(headerLine.toString().trim() + "\n");

        // get data
        Set<Tuple> tuples = dataTable.getActiveQuery().getNonQueriedTuples();

        // write to csv file
        int tupleCounter = 0;
        for (Tuple tuple : tuples) {
            StringBuffer lineBuffer = new StringBuffer();
            for (int i = 0; i < tuple.getElementCount(); i++) {
                if (lineBuffer.length() == 0) {
                    lineBuffer.append(tuple.getElement(i));
                } else {
                    lineBuffer.append(", " + tuple.getElement(i));
                }
            }

            if (tupleCounter == 0) {
                writer.write(lineBuffer.toString().trim());
            } else {
                writer.write("\n" + lineBuffer.toString().trim());
            }

            tupleCounter++;
        }

        writer.close();

        return tupleCounter;
    }

    public static int exportSelectedFromDataTableQueryToCSV(File csvFile, DataTable dataTable) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));

        // write header line with column names
        StringBuffer headerLine = new StringBuffer();
        for (int icol = 0; icol < dataTable.getColumnCount(); icol++) {
            Column column = dataTable.getColumn(icol);
            if (headerLine.length() == 0) {
                headerLine.append(column.getName());
            } else {
                headerLine.append("," + column.getName());
            }
        }

        writer.write(headerLine.toString().trim() + "\n");

        // get data
        Set<Tuple> tuples = dataTable.getActiveQuery().getQueriedTuples();

        // write to csv file
        int tupleCounter = 0;
        for (Tuple tuple : tuples) {
            StringBuffer lineBuffer = new StringBuffer();
            for (int i = 0; i < tuple.getElementCount(); i++) {
                if (lineBuffer.length() == 0) {
                    lineBuffer.append(tuple.getElement(i));
                } else {
                    lineBuffer.append(", " + tuple.getElement(i));
                }
            }

            if (tupleCounter == 0) {
                writer.write(lineBuffer.toString().trim());
            } else {
                writer.write("\n" + lineBuffer.toString().trim());
            }

            tupleCounter++;
        }

        writer.close();

        return tupleCounter;
    }

    public static List<String> readCSVLines(File f, int startLine, int endLine) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));

        ArrayList<String> lines = new ArrayList<>();

        int lineCounter = 0;
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (lineCounter >= startLine && lineCounter <= endLine) {
                if (lineCounter == 0) {
                    if (line.charAt(0) == '\uFEFF') {
                        line = line.substring(1);
                    }
                }

                lines.add(line);

                if (lineCounter + 1 > endLine) {
                    break;
                }
            }
            lineCounter++;
        }

        reader.close();
        return lines;
    }

    public static String[] readCSVHeader(File f) throws  IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String headerLine = reader.readLine();
        reader.close();

        if (headerLine.charAt(0) == '\uFEFF') {
            headerLine = headerLine.substring(1);
        }

        String columnNames[] = headerLine.trim().split(",");
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = columnNames[i].trim();
        }
        return columnNames;
    }

    public static int getCSVLineCount(File f) throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(f));
        lineNumberReader.skip(Long.MAX_VALUE);
        int numLines = lineNumberReader.getLineNumber();
        lineNumberReader.close();
        return numLines;
    }

    public static void readCSV(File f, ArrayList<String> ignoreColumnNames, ArrayList<String> categoricalColumnNames,
                               ArrayList<String> temporalColumnNames, String imageFilenameColumnName,
                               String imageFileDirectoryPath, ArrayList<DateTimeFormatter> temporalColumnFormatters,
                               DataTable dataTable) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));

        ArrayList<Tuple> tuples = new ArrayList<>();
        ArrayList<Column> columns = new ArrayList<>();

        int temporalColumnIndices[] = null;
        if (temporalColumnNames != null && !temporalColumnNames.isEmpty()) {
            temporalColumnIndices = new int[temporalColumnNames.size()];
            Arrays.fill(temporalColumnIndices, -1);
        }

        int categoricalColumnIndices[] = null;
        if (categoricalColumnNames != null && !categoricalColumnNames.isEmpty()) {
            categoricalColumnIndices = new int[categoricalColumnNames.size()];
            Arrays.fill(categoricalColumnIndices, -1);
        }

        int ignoreColumnIndices[] = null;
        if (ignoreColumnNames != null && !ignoreColumnNames.isEmpty()) {
            ignoreColumnIndices = new int[ignoreColumnNames.size()];
            Arrays.fill(ignoreColumnIndices, -1);
        }

        int imageFilenameColumnIndex = -1;
//		if (imageFilenameColumnName != null && imageFileDirectory != null) {
//			imageFilenameColumnIndex =
//		}

        String line = reader.readLine();
        int lineCounter = 0;
        int numLinesIgnored = 0;

        boolean skip_line = false;
        while (line != null) {
            if (lineCounter == 0) {
                // The first line contains the column headers.
                // check for Excel CSV File and remove \uFEFF character
                if (line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }

                int tokenCounter = 0;
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken(",");

                    Column column = null;

                    if (ignoreColumnNames != null) {
                        boolean isIgnoreColumn = false;
                        for (int i = 0; i < ignoreColumnNames.size(); i++) {
                            if (token.trim().equals(ignoreColumnNames.get(i))) {
                                ignoreColumnIndices[i] = tokenCounter;
                                tokenCounter++;
                                isIgnoreColumn = true;
                                break;
                            }
                        }
                        if (isIgnoreColumn) {
                            continue;
                        }
                    }

                    if (temporalColumnNames != null) {
                        for (int i = 0; i < temporalColumnNames.size(); i++) {
                            if (token.trim().equals(temporalColumnNames.get(i))) {
                                temporalColumnIndices[i] = tokenCounter;
                                column = new TemporalColumn(token.trim());
                            }
                        }
                    }

                    if (categoricalColumnNames != null) {
                        for (int i = 0; i < categoricalColumnNames.size(); i++) {
                            if (token.trim().equals(categoricalColumnNames.get(i))) {
                                categoricalColumnIndices[i] = tokenCounter;
                                column = new CategoricalColumn(token.trim(), null);
                            }
                        }
                    }

                    if (imageFilenameColumnName != null && imageFileDirectoryPath != null && imageFilenameColumnIndex == -1) {
                        if (token.trim().equals(imageFilenameColumnName)) {
                            imageFilenameColumnIndex = tokenCounter;
                            column = new ImageColumn(token.trim());
                        }
                    }

                    if (column == null) {
                        column = new DoubleColumn(token.trim());
                    }

                    columns.add(column);

//					if (temporalColumnName != null && token.equals(temporalColumnName)) {
//						temporalColumnIndex = tokenCounter;
//						temporalColumn = new TemporalColumn(token.trim());
//					} else {
//						DoubleColumn column = new DoubleColumn(token.trim());
//						columns.add(column);
//					}

                    tokenCounter++;
                }

                lineCounter++;
                line = reader.readLine();
                continue;
            }

            Tuple tuple = new Tuple(dataTable);
            StringTokenizer st = new StringTokenizer(line);

            int tokenCounter = 0;

            skip_line = false;
            while (st.hasMoreTokens()) {
                String token = st.nextToken(",").trim();

                if (ignoreColumnIndices != null) {
                    boolean ignoreColumn = false;
                    // is this a column to ignore
                    for (int i = 0; i < ignoreColumnIndices.length; i++) {
                        if (tokenCounter == ignoreColumnIndices[i]) {
                            tokenCounter++;
                            ignoreColumn = true;
                            break;
                        }
                    }
                    if (ignoreColumn) {
                        continue;
                    }
                }

                if (temporalColumnIndices != null) {
                    Instant instant = null;

                    // is this a temporal column
                    for (int i = 0; i < temporalColumnIndices.length; i++) {
                        if (tokenCounter == temporalColumnIndices[i]) {
//							LocalDateTime test = LocalDateTime.parse(token, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            LocalDateTime localDateTime = null;
                            try {
                                localDateTime = LocalDateTime.parse(token, temporalColumnFormatters.get(i));
                                instant = localDateTime.toInstant(ZoneOffset.UTC);
                            } catch (DateTimeParseException ex) {
                                instant = Instant.parse(token);
                            }

//                            instant = localDateTime.toInstant(ZoneOffset.UTC);
//                            tuple.addElement(instant);
//                            tokenCounter++;
                            break;
//                            tuple.setInstant(instant);
                        }
                    }

                    if (instant != null) {
                        tuple.addElement(instant);
                        tokenCounter++;
                        continue;
                    }
                }

                if (categoricalColumnIndices != null) {
                    String category = null;

                    // is this a categorical column
                    for (int i = 0; i < categoricalColumnIndices.length; i++) {
                        if (tokenCounter == categoricalColumnIndices[i]) {
                            category = token.trim();
//							((CategoricalColumn)columns.get(tokenCounter)).addCategory(category);
                            ((CategoricalColumn)columns.get(tuple.getElementCount())).addCategory(category);
                            break;
                        }
                    }

                    if (category != null) {
                        tuple.addElement(category);
                        tokenCounter++;
                        continue;
                    }
                }

                if ((imageFilenameColumnIndex != -1) && (tokenCounter == imageFilenameColumnIndex)) {
                    File imageFile = new File(imageFileDirectoryPath, token.trim());
                    Image image = new Image(new FileInputStream(imageFile));
                    Pair<File,Image> imagePair = new Pair<>(imageFile, image);
//					Object imageInfo[] = new Object[2];
//					imageInfo[0] = imageFile;
//					imageInfo[1] = image;
//					tuple.addElement(imageInfo);
                    tuple.addElement(imagePair);
                    tokenCounter++;
                    continue;
                }

                try {
                    double value = Double.parseDouble(token);

//                    if (token_counter == 0) {
//                        log.info("token=" + token + " value=" + ((long)value) + " dvalue=" + (long)dvalue);
//                    }

                    if (Double.isNaN(value)) {
                        skip_line = true;
                        break;
                    }
                    // data attribute
                    tuple.addElement(value);
                    tokenCounter++;
                } catch (NumberFormatException ex) {
                    System.out.println("DataSet.readCSV(): NumberFormatException caught so skipping record. "
                            + ex.fillInStackTrace());
                    skip_line = true;
                    numLinesIgnored++;
                    break;
                }
            }

            if (tuple.getElementCount() != columns.size()) {
                log.info("Row ignored because it has "
                        + (columns.size() - tuple.getElementCount())
                        + " column values missing.");
                numLinesIgnored++;
                skip_line = true;
            }

            if (!skip_line) {
                tuples.add(tuple);
            }

            lineCounter++;
            line = reader.readLine();
        }

        reader.close();

        log.info("Finished reading CSV file '" + f.getName() + "': Read " + tuples.size() + " rows with " + columns.size() + " columns; " + numLinesIgnored + " rows ignored.");

        long start = System.currentTimeMillis();
        dataTable.setData(tuples, columns);
        long elapsed = System.currentTimeMillis() - start;

        log.info("Finished setting data in datamodel (it took " + elapsed + " ms");
    }

    public static void main (String args[]) throws IOException {
        DataTable dataTable = new DataTable();

        ArrayList<String> categoricalColumnNames = new ArrayList<>();
        categoricalColumnNames.add("Origin");

        IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames,
                null, null, null,
                null, dataTable);

        log.info("Finished");
//	    ArrayList<String> temporalColumnNames = new ArrayList<>();
//	    temporalColumnNames.add("Date");
//	    ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
//	    temporalColumnFormatters.add(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
//
//	    IOUtilities.readCSV(new File("data/csv/titan-performance.csv"), null, null,
//                temporalColumnNames, temporalColumnFormatters, dataModel);
    }
}
