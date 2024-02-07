package com.gs.tax.service;

import com.fasterxml.jackson.databind.JavaType;
import com.gs.tax.validation.Validator;
import com.gs.tax.validation.ValidatorFactory;
import org.apache.poi.ss.util.CellReference;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelUploadService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> processFiles(MultipartFile jsonFile, MultipartFile excelFile) throws IOException {
        List<Map<String, Object>> schema = objectMapper.readValue(jsonFile.getInputStream(), new TypeReference<List<Map<String, Object>>>(){});
        Workbook workbook = WorkbookFactory.create(excelFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<Map<String, Object>> validRows = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        Map<Integer, Map<String, String>> columnDetails = prepareColumnDetails(schema);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Assuming the first row is header

            Map<String, Object> rowData = new HashMap<>();
            boolean isValidRow = true;

            for (Cell cell : row) {
                int columnIndex = cell.getColumnIndex();
                if (columnDetails.containsKey(columnIndex)) {
                    Map<String, String> fieldDetails = columnDetails.get(columnIndex);
                    Object cellValue = getCellValue(cell);
                    String fieldName = fieldDetails.get("name");
                    String validationRule = fieldDetails.get("validation");

                    Validator validator = ValidatorFactory.getValidator(validationRule);
                    String validationError = validator.validate(cellValue, validationRule);

                    if (!validationError.isEmpty()) {
                        validationErrors.add(String.format("Row %d, '%s': %s", row.getRowNum() + 1, fieldName, validationError));
                        isValidRow = false;
                        break; // Optionally, stop processing this row on the first validation error
                    } else {
                        rowData.put(fieldName, cellValue);
                    }
                }
            }

            if (isValidRow) {
                validRows.add(rowData);
            }
        }

        Map<String, Object> response = new HashMap<>();
        if (validationErrors.isEmpty()) {
            response.put("data", validRows);
        } else {
            response.put("errors", validationErrors);
        }

        return response;
    }

    private Map<Integer, Map<String, String>> prepareColumnDetails(List<Map<String, Object>> schema) {
        Map<Integer, Map<String, String>> columnDetails = new HashMap<>();
        schema.forEach(field -> {
            String excelColumn = (String) field.get("excelColumn");
            String name = (String) field.get("name");
            String validation = (String) field.get("validation");
            int columnIndex = CellReference.convertColStringToIndex(excelColumn);

            Map<String, String> details = new HashMap<>();
            details.put("name", name);
            details.put("validation", validation);
            columnDetails.put(columnIndex, details);
        });
        return columnDetails;
    }

    private Object getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue(); // Returns as Date
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // Check if it can be treated as an integer
                    if ((numericValue == Math.floor(numericValue)) && !Double.isInfinite(numericValue)) {
                        return (int) numericValue; // Return as Integer if no decimal part
                    } else {
                        return numericValue; // Return as Double
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                // For formulas, you might want to evaluate them first
                // This requires having a FormulaEvaluator instance
                // Example: return evaluator.evaluate(cell).getNumberValue();
                return cell.getCellFormula();
            default:
                return ""; // Or some other default value for blank or error cells
        }
    }
}
