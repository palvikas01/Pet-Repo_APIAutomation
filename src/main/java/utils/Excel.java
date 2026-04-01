package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

public final class Excel {
    private Excel() {}

    public static List<Map<String, String>> readSheetAsMaps(String resourcePath, String sheetName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IllegalArgumentException("Resource not found: " + resourcePath);

            try (Workbook wb = WorkbookFactory.create(is)) {
                // Sheet sheet = wb.getSheet(sheetName.trim());
                // if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);
                Sheet sheet = wb.getSheet(sheetName);
                 if (sheet == null) {
                     List names = new ArrayList<>();
                      for (int i = 0; i < wb.getNumberOfSheets(); i++)
                     names.add(wb.getSheetName(i)); 
                    throw new IllegalArgumentException("Sheet not found: '" + sheetName + "'. Available sheets: " + names);
                 }

                DataFormatter fmt = new DataFormatter();
                Iterator<Row> it = sheet.rowIterator();
                if (!it.hasNext()) return List.of();

                // headers
                Row headerRow = it.next();
                List<String> headers = new ArrayList<>();
                for (Cell c : headerRow) headers.add(fmt.formatCellValue(c).trim());

                // data
                List<Map<String, String>> out = new ArrayList<>();
                while (it.hasNext()) {
                    Row r = it.next();
                    if (isRowEmpty(r, fmt)) continue;

                    Map<String, String> m = new LinkedHashMap<>();
                    for (int i = 0; i < headers.size(); i++) {
                        Cell cell = r.getCell(i, MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        String v = (cell == null) ? "" : fmt.formatCellValue(cell).trim();
                        m.put(headers.get(i), v);
                    }
                    out.add(m);
                }
                return out;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Optional<Map<String, String>> firstBy(List<Map<String, String>> rows, String key, String value) {
        return rows.stream().filter(m -> Objects.equals(m.get(key), value)).findFirst();
    }

    private static boolean isRowEmpty(Row row, DataFormatter fmt) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && !fmt.formatCellValue(cell).isBlank()) return false;
        }
        return true;
    }
}

