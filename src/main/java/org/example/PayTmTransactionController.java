package org.example;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import javax.annotation.Resource;
import javax.jms.*;

@RestController
@RequestMapping("paytm")
@Slf4j
public class PayTmTransactionController {
    @Autowired
    @JMSConnectionFactory("java:jboss/exported/RemoteJmsXA")
    private JMSContext context;

    @Resource(lookup = "java:jboss/exported/queue/PaytmQueue")
    private Queue queue;

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadFileResponse> bulkUploadtransactions(@RequestParam("file") MultipartFile file) {
        log.info("about to upload file {}",file);
        UploadFileResponse response = null;
        try {
            response = this.uploadFile(file);
        }catch(UploadException ex) {
            response = new UploadFileResponse("error uploading file");
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("done upload file");
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    private UploadFileResponse uploadFile(MultipartFile processFile){
        CsvSchema txSchema = CsvSchema.builder()
                .setUseHeader(true)
                .addColumn("txDate", ColumnType.STRING)
                .addColumn("activity", ColumnType.STRING)
                .addColumn("sourceDestination", ColumnType.STRING)
                .addColumn("walletTxId", ColumnType.STRING)
                .addColumn("usr_comment", ColumnType.STRING)
                .addColumn("debit", ColumnType.STRING)
                .addColumn("credit", ColumnType.STRING)
                .addColumn("transaction_breakup", ColumnType.STRING)
                .addColumn("status", ColumnType.STRING)
                .build();
        ConvertUtils.register(new Converter() {
            @Override
            public <T> T convert(Class<T> aClass, Object dateStr) {
                try {
                    return (T) DateUtil.parseDate((String)dateStr);
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        },java.sql.Date.class);
        final Destination destination = queue;
        try(InputStream data = processFile.getInputStream()){
            CsvMapper csvMapper = new CsvMapper();
            MappingIterator<PayTmDTO> itr = csvMapper.readerFor(PayTmDTO.class).with(txSchema).readValues(data);
            PayTmDTO tx = null;
            while (itr.hasNext()){
                tx = itr.next();
                try {
                    ObjectMessage message = context.createObjectMessage(tx);
                    context.createProducer().send(destination, message);
                }catch(Exception ex){
                    log.error("Erroring {}",ex);
                    log.error("error parsing transaction date {}",tx.toString());
                }
            }
        } catch (IOException ex) {
            log.error("error uploading file",ex);
            throw new UploadException("error uploading file");
        }
        return new UploadFileResponse("Successfully uploaded file");
    }
}