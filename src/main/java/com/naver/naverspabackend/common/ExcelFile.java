package com.naver.naverspabackend.common;

import com.beust.jcommander.internal.Lists;
import com.google.gson.reflect.TypeToken;
import com.naver.naverspabackend.annotation.ExcelColumnName;
import com.naver.naverspabackend.util.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Handler;
import org.apache.commons.collections.IteratorUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelFile< T> {

    private static final int ROW_START_INDEX = 0;
    private static final int COLUMN_START_INDEX = 0;

    public SXSSFWorkbook renderExcel(List<T> data, Class<?> cls, String type) {

        SXSSFWorkbook wb = new SXSSFWorkbook();

        Sheet sheet = wb.createSheet();

        renderHeaders(sheet, cls, type);

        if (data.isEmpty()) {
            return wb;
        }

        // Render Body
        int rowIndex = ROW_START_INDEX + 1;
        for (Object renderedData : data) {
            renderBody(sheet, renderedData, rowIndex++, type);
        }

        return wb;
    }

    private void renderHeaders(Sheet sheet, Class<?> cls, String type) {
        Row row = sheet.createRow(ROW_START_INDEX);
        int columnIndex = COLUMN_START_INDEX;

        for (Field field : cls.getDeclaredFields()) {
            ExcelColumnName excelColumn = field.getDeclaredAnnotation(ExcelColumnName.class);

            if(excelColumn!=null && excelColumn.type()!=null){
                String[] types = excelColumn.type().split(",");
                List<String> typeList = Arrays.asList(types);
                if(field.isAnnotationPresent(ExcelColumnName.class)&&(typeList.indexOf("common")>-1||typeList.indexOf(type)>-1)) {
                    if (field.isAnnotationPresent(ExcelColumnName.class) && excelColumn.download()) {
                        Cell cell = row.createCell(columnIndex++);
                        cell.setCellValue(excelColumn.headerName());

                        sheet.setColumnWidth(cell.getColumnIndex(), 20 * excelColumn.width());
                    }
                }
            }else{
                if(field.isAnnotationPresent(ExcelColumnName.class)&&(excelColumn.type().equals("common")||excelColumn.type().equals(type))) {
                    if (field.isAnnotationPresent(ExcelColumnName.class) && excelColumn.download()) {
                        Cell cell = row.createCell(columnIndex++);
                        cell.setCellValue(excelColumn.headerName());

                        sheet.setColumnWidth(cell.getColumnIndex(), 20 * excelColumn.width());
                    }
                }

            }


        }
    }

    private void renderBody(Sheet sheet, Object data, int rowIndex, String type) {
        Row row = sheet.createRow(rowIndex);
        int columnIndex = COLUMN_START_INDEX;

        for (Field field : data.getClass().getDeclaredFields()) {

            try {
                ExcelColumnName excelColumn = field.getDeclaredAnnotation(ExcelColumnName.class);


                if(excelColumn!=null && excelColumn.type()!=null) {
                    String[] types = excelColumn.type().split(",");
                    List<String> typeList = Arrays.asList(types);

                    if(field.isAnnotationPresent(ExcelColumnName.class)&&(typeList.indexOf("common")>-1||typeList.indexOf(type)>-1)) {
                        if (field.isAnnotationPresent(ExcelColumnName.class) && excelColumn.download()) {
                            Cell cell = row.createCell(columnIndex++);
                            field.setAccessible(true);
                            renderCellValue(cell, field.get(data));
                        }
                    }
                }else{

                    if(field.isAnnotationPresent(ExcelColumnName.class)&&(excelColumn.type().equals("common")||excelColumn.type().equals(type))) {
                        if (field.isAnnotationPresent(ExcelColumnName.class) && excelColumn.download()) {
                            Cell cell = row.createCell(columnIndex++);
                            field.setAccessible(true);
                            renderCellValue(cell, field.get(data));
                        }
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

    }

    private void renderCellValue(Cell cell, Object cellValue) {
        if (cellValue instanceof Number) {
            Number numberValue = (Number) cellValue;
            cell.setCellValue(numberValue.doubleValue());
            return;
        } else if (cellValue instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) cellValue;
            String value = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            cell.setCellValue(value);
            return;
        }
        cell.setCellValue(cellValue == null ? "" : cellValue.toString());
    }

    public List<Map<String,Object>> uploadExcel(InputStream inputStream, Class<?> cls) throws IOException {
        Workbook wb = new XSSFWorkbook(inputStream);
        try{

            Sheet sheet = wb.getSheetAt(ROW_START_INDEX);

            List<Map<String,Object>> items = new ArrayList<>();
            Row headerRow = sheet.getRow(0);

            Iterator<Row> rows = sheet.iterator();
            int cellsInRowIndex = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                if(cellsInRowIndex>0){
                    Iterator<Cell> cellsInRow = currentRow.iterator();
                    List<Cell> cellsInRowList = IteratorUtils.toList(cellsInRow);
                    Map<String,Object> item = new HashMap<>();

                    for (Field field : cls.getDeclaredFields()) {
                        Iterator<Cell> headerRows = headerRow.iterator();
                        List<Cell> headerRowsList = IteratorUtils.toList(headerRows);

                        ExcelColumnName excelColumn = field.getDeclaredAnnotation(ExcelColumnName.class);
                        if(excelColumn!=null && excelColumn.type()!=null){
                            for (int i=0;i<headerRowsList.size();i++){
                                Cell currentHeaderCell =  headerRowsList.get(i);
                                if(excelColumn.headerName().equals(currentHeaderCell.getStringCellValue())){
                                    if(currentHeaderCell.getStringCellValue().equals("bulkOpenDt")){
                                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
                                        field.getType();
                                        String value = null;
                                        try{
                                            value = sf.format(cellsInRowList.get(i).getDateCellValue());
                                        }catch (Exception e){
                                            try{
                                                value = cellsInRowList.get(i).getStringCellValue();
                                            }catch (Exception e2){
                                                value = Long.toString((long) cellsInRowList.get(i).getNumericCellValue());
                                            }
                                        }
                                        item.put(field.getName(),value);
                                    }
                                    else if (field.getType() == Integer.class) {
                                        int value = (int) cellsInRowList.get(i).getNumericCellValue();
                                        item.put(field.getName(),value);
                                    } else if (field.getType() == Long.class) {
                                        Long value = (long) cellsInRowList.get(i).getNumericCellValue();
                                        item.put(field.getName(),value);
                                    } else if (field.getType() == String.class) {
                                        String value = null;
                                        try{
                                            value = cellsInRowList.get(i).getStringCellValue();
                                        }catch (Exception e){
                                           try{

                                               value = Long.toString((long) cellsInRowList.get(i).getNumericCellValue());
                                           }catch (Exception e2){
                                               e2.printStackTrace();
                                               value = "";
                                           }
                                        }
                                        item.put(field.getName(),value);
                                    } else if (field.getType() == Boolean.class) {
                                        Boolean value = cellsInRowList.get(i).getBooleanCellValue();
                                        item.put(field.getName(),value);
                                    }

                                    break;
                                }
                            }
                        }
                    }
                    items.add(item);
                }


                cellsInRowIndex++;
            }

            return items;
        }finally {
            wb.close();

        }

    }
}
