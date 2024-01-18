package com.bob.pdfbox_demo.controller;

import com.bob.pdfbox_demo.model.dto.ApiRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1CFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "pdf")
public class PdfController {


    @GetMapping("/test")
    public ApiRespDTO<Object> test() {
        ApiRespDTO<Object> resp = ApiRespDTO.builder().build();
        return resp;
    }

    @GetMapping("/draw")
    public ApiRespDTO<Object> sign() throws IOException {
        ApiRespDTO<Object> resp = ApiRespDTO.builder().build();
        // 底版 PDF
        File file = ResourceUtils.getFile("classpath:static/001.pdf");
        PDDocument pdfDoc = Loader.loadPDF(file);

        PDDocumentCatalog catalog = pdfDoc.getDocumentCatalog();
        PDAcroForm acroForm = catalog.getAcroForm();

        // 空白 PDF
        File fileEmptyPDF = ResourceUtils.getFile("classpath:static/001_empty.pdf");
        PDDocument pdfDocEmpty = Loader.loadPDF(fileEmptyPDF);

        PDDocumentCatalog catalogEmpty = pdfDocEmpty.getDocumentCatalog();
        PDAcroForm acroFormEmpty = catalogEmpty.getAcroForm();


        List<PDField> fields = acroForm.getFields();
        for (PDField field: fields) {
            String fieldName = field.getFullyQualifiedName();
            log.warn("Full Qualified Name : " + field.getFullyQualifiedName());
//            if ("YYY".equals(fieldName) || "MM".equals(fieldName) || "DD".equals(fieldName)) {
//                fieldsEmpty.add(field);
//            }

//            acroFormEmpty.setFields(fieldsEmpty);
//            log.warn("Full Qualified Name : " + field.getFullyQualifiedName());
//            log.warn("Partial Name : " + field.getPartialName());
//
//            // get field info
            COSDictionary fieldDict = field.getCOSObject();
            COSArray fieldAreaArray = (COSArray) fieldDict.getCOSArray(COSName.RECT);
//
            PDRectangle mediaBox = new PDRectangle((fieldAreaArray));
            log.warn("PDF Field {} lowerLeftX:{}, lowLeftY:{} ", field.getFullyQualifiedName(), mediaBox.getLowerLeftX(), mediaBox.getLowerLeftY());
            log.warn("PDF Field {} upperRightX:{}, upperRightY:{} ", field.getFullyQualifiedName(), mediaBox.getUpperRightX(), mediaBox.getUpperRightY());

//            // 塞文字
//
//            // 簽名圖檔
            File imgFile = ResourceUtils.getFile("classpath:static/001.jpg");
            PDImageXObject pdImage = PDImageXObject.createFromFileByContent(imgFile, pdfDocEmpty);

            float height = mediaBox.getHeight();;
            float width = mediaBox.getWidth();
            float x = mediaBox.getLowerLeftX();
            float y = mediaBox.getLowerLeftY();

//
            List<PDAnnotationWidget> widgets = ((PDTerminalField) field).getWidgets();
            for (PDAnnotationWidget widget: widgets) {
                PDPage page = widget.getPage();
                int pageIndex = getPdfFieldPageIndex(pdfDoc, widget, page);
                log.warn("PageIdex = {}", pageIndex);
                if (page != null) {
                    int textFontSize = 12;
                    // 寫文字
                    if ("YYY".equals(fieldName) || "MM".equals(fieldName) || "DD".equals(fieldName)) {
                        try (PDPageContentStream pdPageContentStream = new PDPageContentStream(pdfDocEmpty, pdfDocEmpty.getPage(pageIndex), PDPageContentStream.AppendMode.APPEND, true)) {

                            PDFont pdfFont = PDType0Font.load(pdfDocEmpty, ResourceUtils.getFile("classpath:kaiu.ttf"));
                            pdPageContentStream.setFont(pdfFont, textFontSize);
                            pdPageContentStream.beginText();
                            pdPageContentStream.newLineAtOffset(x, y);
                            pdPageContentStream.showText(fieldName);
                            pdPageContentStream.endText();
                        }
//
                    } else {
                        // 塞圖
                        PDAppearanceStream pdAppearanceStream = new PDAppearanceStream(pdfDocEmpty);
                        pdAppearanceStream.setResources(new PDResources());
                        try (PDPageContentStream pdPageContentStream = new PDPageContentStream(pdfDocEmpty, pdfDocEmpty.getPage(pageIndex), PDPageContentStream.AppendMode.APPEND, true)) {
                            pdPageContentStream.drawImage(pdImage, x, y, width, height);
                        }
                    }
                    // remove field
//                    List<PDAnnotation> annotations = page.getAnnotations();
//                    boolean removed = false;
//
//                    for (PDAnnotation annotation : annotations) {
//                        if (annotation.getCOSObject().equals(widget.getCOSObject())) {
//                            removed = annotations.remove(annotation);
//                            break;
//                        }
//                    }
                }
            }
        }

        // 產生新PDF
        pdfDocEmpty.getPage(0);
        pdfDocEmpty.save("src/main/resources/static/001_new.pdf");
        pdfDoc.close();
        pdfDocEmpty.close();


        return resp;
    }

    private int getPdfFieldPageIndex(PDDocument pdfDoc, PDAnnotationWidget widget, PDPage page) throws IOException {
        int result = 0;
        for (int p = 0; p < pdfDoc.getNumberOfPages(); ++p)
        {
            List<PDAnnotation> annotations = pdfDoc.getPage(p).getAnnotations();
            for (PDAnnotation ann : annotations)
            {
                if (ann.getCOSObject() == widget.getCOSObject())
                {
                    result = p;
                    break;
                }
            }
        }
        return result;
    }
}
