import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;
public class CsvReader {
    ArrayList<String[]> nextLine = new ArrayList<>();

    public ArrayList<String[]> read(String path) throws IOException {
        //Init the CSV Reader (OpenCSV)
        CSVReader reader = new CSVReader(new FileReader(path));
        String []csvCell;
        //nextLine.add(reader.readNext());

        while ((csvCell=reader.readNext()) != null) {
            try {
                nextLine.add(csvCell);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return nextLine;
    }
}